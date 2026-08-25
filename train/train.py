"""Fine-tune the phoneme head with label priors (Huang et al., ICASSP 2024).

The correction is one line of arithmetic on top of standard CTC: the log
prior of each label, estimated from the model's own averaged posteriors,
is subtracted from the frame log-probabilities before the loss. The blank
stops being free, and the network spreads each phone over its real
duration — dense — while still training on sequences, never on canonical
frame labels — acoustic.

Step 1 of the staircase (the shared ear): the wav2vec2-base-960h encoder
is frozen and only the head learns, so the letter head keeps an ear it
knows how to read. --unfreeze trains everything (V2).

Smoke test, CPU, no GPU rented:
    HF_HOME=tmp/hf tmp/venv/bin/python train/train.py --steps 2 --utterances 8
"""

import argparse
import json
from pathlib import Path

import torch
import soundfile
from torch.nn.functional import ctc_loss, log_softmax
from transformers import Wav2Vec2ForCTC

from timit import ROOT, vocabulary

ENCODER = "facebook/wav2vec2-base-960h"
MANIFEST = ROOT / "tmp" / "train" / "manifest.json"


def batches(entries, size):
    ordered = sorted(entries, key=lambda e: e["seconds"])
    for lot in (ordered[i:i + size] for i in range(0, len(ordered), size)):
        audio = [torch.tensor(soundfile.read(ROOT / e["wav"])[0], dtype=torch.float32)
                 for e in lot]
        # Same signal contract as the exported graph: zero mean, unit variance.
        audio = [(a - a.mean()) / (a.std() + 1e-7) for a in audio]
        yield lot, torch.nn.utils.rnn.pad_sequence(audio, batch_first=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--steps", type=int, help="stop after N steps (smoke test)")
    parser.add_argument("--utterances", type=int, help="truncate the corpus (smoke test)")
    parser.add_argument("--epochs", type=int, default=30)
    parser.add_argument("--batch", type=int, default=8)
    parser.add_argument("--lr", type=float, default=3e-4)
    parser.add_argument("--prior-weight", type=float, default=0.3,
                        help="weight on the subtracted log prior (to sweep on GPU)")
    parser.add_argument("--unfreeze", action="store_true",
                        help="train the encoder too (V2); default is head only (V1)")
    parser.add_argument("--out", type=Path, default=ROOT / "tmp" / "train" / "checkpoints")
    args = parser.parse_args()

    vocab = vocabulary()
    table = {symbol: index for symbol, index in vocab.items()}
    blank = table["[PAD]"]
    entries = json.loads(MANIFEST.read_text(encoding="utf-8"))["train"]
    if args.utterances:
        entries = entries[:args.utterances]

    model = Wav2Vec2ForCTC.from_pretrained(
        ENCODER, vocab_size=len(vocab), pad_token_id=blank,
        ctc_loss_reduction="mean", ignore_mismatched_sizes=True)
    # masked_spec_embed is absent from the base-960h checkpoint, and
    # transformers fills missing parameters with NaN; SpecAugment then
    # poisons every train-mode forward. Give it its intended init.
    with torch.no_grad():
        model.wav2vec2.masked_spec_embed.uniform_()
    poisoned = [n for n, p in model.named_parameters() if p.isnan().any()]
    if poisoned:
        raise SystemExit(f"NaN parameters after loading: {poisoned}")
    model.freeze_feature_encoder()
    if not args.unfreeze:
        for parameter in model.wav2vec2.parameters():
            parameter.requires_grad = False
    trainable = sum(p.numel() for p in model.parameters() if p.requires_grad)
    device = "cuda" if torch.cuda.is_available() else "cpu"
    model.to(device).train()
    print(f"{ENCODER} + fresh head of {len(vocab)}: {trainable:,} trainable parameters, {device}")

    optimiser = torch.optim.AdamW(
        [p for p in model.parameters() if p.requires_grad], lr=args.lr)
    # Running estimate of the label distribution, from the model's own
    # posteriors (EMA); uniform start so the first steps are plain CTC-ish.
    prior = torch.full((len(vocab),), 1 / len(vocab), device=device)

    step = 0
    args.out.mkdir(parents=True, exist_ok=True)
    for epoch in range(args.epochs):
        for lot, audio in batches(entries, args.batch):
            logits = model(audio.to(device)).logits
            frames = log_softmax(logits, dim=-1)
            with torch.no_grad():
                prior = 0.999 * prior + 0.001 * frames.exp().mean(dim=(0, 1))
            penalised = frames - args.prior_weight * prior.clamp_min(1e-8).log()
            penalised = log_softmax(penalised, dim=-1)
            targets = [torch.tensor([table[p] for p in e["phones"]]) for e in lot]
            loss = ctc_loss(
                penalised.transpose(0, 1),
                torch.nn.utils.rnn.pad_sequence(targets, batch_first=True).to(device),
                input_lengths=torch.full((len(lot),), penalised.shape[1]),
                target_lengths=torch.tensor([len(t) for t in targets]),
                blank=blank, zero_infinity=True)
            optimiser.zero_grad()
            loss.backward()
            optimiser.step()
            step += 1
            print(f"epoch {epoch} step {step} loss {loss.item():.3f}")
            if args.steps and step >= args.steps:
                print("smoke test done")
                return
        model.save_pretrained(args.out / f"epoch-{epoch:03d}")
        torch.save(prior, args.out / f"epoch-{epoch:03d}" / "prior.pt")


if __name__ == "__main__":
    main()
