"""Fine-tune the phoneme head with label priors (Huang et al., ICASSP 2024).

The correction is one line of arithmetic on top of standard CTC: the log
prior of each label, estimated from the model's own averaged posteriors,
is subtracted from the frame log-probabilities before the loss. The blank
stops being free, and the network spreads each phone over its real
duration — dense — while still training on sequences, never on canonical
frame labels — acoustic.

By default the encoder is frozen and only the head learns. --unfreeze
fine-tunes the whole network (feature extractor always kept frozen) — the
regime the paper's recipe assumes; --encoder picks the pre-trained ear.

Smoke test, CPU, no GPU rented:
    HF_HOME=tmp/hf tmp/venv/bin/python train/train.py --steps 2 --utterances 8
"""

import argparse
import json
import math
import random
from pathlib import Path

import torch
import soundfile
from torch.nn.functional import ctc_loss, log_softmax
from transformers import Wav2Vec2ForCTC

from timit import ROOT, vocabulary

# The shared ear: an encoder whose letter head already exists on the shelf, so
# one pass can serve both alphabets. `large` is the next one up, and carries
# wav2vec2-large-960h as its letter head.
ENCODER = "facebook/wav2vec2-base-960h"
MANIFEST = ROOT / "tmp" / "train" / "manifest.json"


def lots(entries, size):
    """Group by length: padding a batch to its longest member is cheapest when
    the members are of a kind. The groups are then shuffled per epoch, so the
    saving stays and the fixed short-to-long curriculum does not."""
    ordered = sorted(entries, key=lambda e: e["seconds"])
    return [ordered[i:i + size] for i in range(0, len(ordered), size)]


def loaded(lot):
    audio = [torch.tensor(soundfile.read(ROOT / e["wav"])[0], dtype=torch.float32)
             for e in lot]
    # Same signal contract as the exported graph: zero mean, unit variance.
    audio = [(a - a.mean()) / (a.std() + 1e-7) for a in audio]
    samples = torch.tensor([len(a) for a in audio])
    return torch.nn.utils.rnn.pad_sequence(audio, batch_first=True), samples


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--steps", type=int, help="stop after N steps (smoke test)")
    parser.add_argument("--utterances", type=int, help="truncate the corpus (smoke test)")
    parser.add_argument("--epochs", type=int, default=30)
    parser.add_argument("--batch", type=int, default=8)
    parser.add_argument("--lr", type=float, default=3e-4)
    parser.add_argument("--warmup", type=float, default=0.1,
                        help="fraction of the optimiser steps spent ramping the lr up; linear decay to zero after")
    parser.add_argument("--prior-weight", type=float, default=0.3,
                        help="weight on the subtracted log prior (to sweep on GPU)")
    parser.add_argument("--unfreeze", action="store_true",
                        help="train the encoder too; default is head only")
    parser.add_argument("--accumulate", type=int, default=1,
                        help="gradient accumulation: optimiser steps every N batches")
    parser.add_argument("--checkpointing", action="store_true",
                        help="gradient checkpointing, trades compute for VRAM (only useful with --unfreeze)")
    parser.add_argument("--save-every", type=int, default=1,
                        help="write a checkpoint every N epochs (the last one always); full fine-tunes write ~1.2 GB per checkpoint")
    parser.add_argument("--clip", type=float, default=1.0,
                        help="max gradient norm; 0 disables clipping")
    parser.add_argument("--amp", action="store_true",
                        help="mixed precision on CUDA; the loss stays in fp32. Never turn this on without comparing a few hundred steps against an fp32 run")
    parser.add_argument("--seed", type=int, default=0,
                        help="seeds the head init and the batch order")
    parser.add_argument("--encoder", default=ENCODER,
                        help="the pre-trained ear to build the head on")
    parser.add_argument("--out", type=Path, default=ROOT / "tmp" / "train" / "checkpoints")
    args = parser.parse_args()

    torch.manual_seed(args.seed)
    vocab = vocabulary()
    table = {symbol: index for symbol, index in vocab.items()}
    blank = table["[PAD]"]
    entries = json.loads(MANIFEST.read_text(encoding="utf-8"))["train"]
    if args.utterances:
        entries = entries[:args.utterances]

    model = Wav2Vec2ForCTC.from_pretrained(
        args.encoder, vocab_size=len(vocab), pad_token_id=blank,
        ctc_loss_reduction="mean", ignore_mismatched_sizes=True,
        # LayerDrop skips whole encoder layers at random. Zero is what the
        # outgoing timit-ipa was trained with, and this run is meant to be an
        # isolate against it. Dropout and SpecAugment stay on, so a run is not
        # thereby deterministic — one source of noise less, not none.
        layerdrop=0.0)
    # masked_spec_embed is absent from the base-960h checkpoint, and
    # transformers fills missing parameters with NaN; SpecAugment then
    # poisons every train-mode forward. Give it its intended init.
    with torch.no_grad():
        model.wav2vec2.masked_spec_embed.uniform_()
    poisoned = [n for n, p in model.named_parameters() if p.isnan().any()]
    if poisoned:
        raise SystemExit(f"NaN parameters after loading: {poisoned}")
    # The convolutional feature extractor stays frozen in every regime — the
    # recipe wav2vec2 was released with, and it is what makes the signal
    # contract of the exported graph still hold.
    model.freeze_feature_encoder()
    if not args.unfreeze:
        for parameter in model.wav2vec2.parameters():
            parameter.requires_grad = False
    if args.checkpointing:
        model.gradient_checkpointing_enable()
    trainable = sum(p.numel() for p in model.parameters() if p.requires_grad)
    device = "cuda" if torch.cuda.is_available() else "cpu"
    if args.amp and device != "cuda":
        raise SystemExit("--amp needs CUDA")
    model.to(device).train()
    print(f"{args.encoder} + fresh head of {len(vocab)}: {trainable:,} trainable parameters, {device}")

    optimiser = torch.optim.AdamW(
        [p for p in model.parameters() if p.requires_grad], lr=args.lr)
    scaler = torch.amp.GradScaler("cuda", enabled=args.amp)
    # Running estimate of the label distribution, from the model's own
    # posteriors (EMA); uniform start so the first steps are plain CTC-ish.
    prior = torch.full((len(vocab),), 1 / len(vocab), device=device)

    # One directory per run, enforced: mixed checkpoints from two runs are
    # indistinguishable at qualification time. No resume logic exists here —
    # a run always starts from the pre-trained encoder, never from tmp state.
    if any(args.out.glob("epoch-*")):
        raise SystemExit(f"{args.out} already holds checkpoints — name one output directory per run (--out)")
    step = 0
    args.out.mkdir(parents=True, exist_ok=True)
    # Encoders whose feature extractor is layer-normed (large) were trained
    # with an attention mask; group-normed ones (base) were not and must not
    # receive one.
    attends = model.config.feat_extract_norm == "layer"

    groups = lots(entries, args.batch)
    # A full fine-tune at 3e-4 with a cold head diverges in the first hundred
    # steps; the ramp lets the head find a scale before the encoder moves.
    updates = math.ceil(len(groups) / args.accumulate) * args.epochs
    ramp = max(1, round(args.warmup * updates))
    schedule = torch.optim.lr_scheduler.LambdaLR(
        optimiser,
        lambda u: (u + 1) / ramp if u < ramp else max(0.0, (updates - u) / max(1, updates - ramp)))
    print(f"{len(groups)} batches per epoch, {updates} optimiser steps, {ramp} of them warming up")

    for epoch in range(args.epochs):
        random.Random(args.seed + epoch).shuffle(groups)
        for taken, lot in enumerate(groups, start=1):
            audio, samples = loaded(lot)
            with torch.autocast("cuda", dtype=torch.float16, enabled=args.amp):
                if attends:
                    heard = (torch.arange(audio.shape[1])[None, :] < samples[:, None])
                    logits = model(audio.to(device), attention_mask=heard.to(device)).logits
                else:
                    logits = model(audio.to(device)).logits
            # Everything from here down stays in fp32, autocast or not: a CTC
            # loss over log-probabilities is where reduced precision turns into
            # a silent NaN rather than an error.
            frames = log_softmax(logits.float(), dim=-1)
            lengths = model._get_feat_extract_output_lengths(samples).to(torch.long)
            valid = (torch.arange(frames.shape[1])[None, :] < lengths[:, None]).to(device)
            with torch.no_grad():
                seen = (frames.exp() * valid[..., None]).sum(dim=(0, 1)) / valid.sum()
                prior = 0.999 * prior + 0.001 * seen
            # The penalised scores go to the loss unnormalised, as in the paper:
            # a renormalising log_softmax here would let the head bias absorb
            # w*log(prior) at zero cost, and the penalty with it. The floor
            # bounds the boost a collapsed class can receive to ~9 nats.
            penalised = frames - args.prior_weight * prior.clamp_min(1e-4).log()
            targets = [torch.tensor([table[p] for p in e["phones"]]) for e in lot]
            loss = ctc_loss(
                penalised.transpose(0, 1),
                torch.nn.utils.rnn.pad_sequence(targets, batch_first=True).to(device),
                input_lengths=lengths,
                target_lengths=torch.tensor([len(t) for t in targets]),
                blank=blank, zero_infinity=True)
            scaler.scale(loss / args.accumulate).backward()
            step += 1
            if taken % args.accumulate == 0 or taken == len(groups):
                if args.clip:
                    scaler.unscale_(optimiser)
                    torch.nn.utils.clip_grad_norm_(
                        [p for p in model.parameters() if p.requires_grad], args.clip)
                scaler.step(optimiser)
                scaler.update()
                schedule.step()
                optimiser.zero_grad(set_to_none=True)
            print(f"epoch {epoch} step {step} loss {loss.item():.3f} lr {schedule.get_last_lr()[0]:.2e}")
            if args.steps and step >= args.steps:
                print("smoke test done")
                return
        if (epoch + 1) % args.save_every and epoch + 1 != args.epochs:
            continue
        model.save_pretrained(args.out / f"epoch-{epoch:03d}")
        torch.save(prior, args.out / f"epoch-{epoch:03d}" / "prior.pt")


if __name__ == "__main__":
    main()
