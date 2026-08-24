#!/usr/bin/env python3
"""The acoustic matrix, and the two ways of reading it.

One pass of a phoneme-level wav2vec2 over an audio file gives, every 20 ms, the
spread of resemblance over every sound the network knows. Everything the local
pipeline does is a reading of that table -- free decoding on one side, forced
alignment on the other -- so it is computed once per audio and cached.

Nothing here consults a dictionary, a dialect lexicon or a grapheme-to-phoneme
table: the only question ever asked of the network is what a recording sounds
like, never what a word should sound like.
"""

import json
import os
from collections import namedtuple
from pathlib import Path

import numpy as np
import soundfile as sf

# The acoustic model is a parameter, because which one to run is exactly what
# the bench is measuring. Anything with frame-level logits over a phone
# inventory fits: what is read is the spread, never the label.
#
# What differs between candidates is where the inventory lives and how the
# waveform is prepared, and a checkpoint that omits those files still has to be
# describable. `vocabulary` names the repo holding vocab.json when it is not the
# model's own; `extractor` says whether the repo carries its own preparation or
# the standard one applies.
Candidate = namedtuple("Candidate", "model vocabulary extractor")

CANDIDATES = {
    "espeak": Candidate("facebook/wav2vec2-lv-60-espeak-cv-ft", None, "repo"),
    "gruut": Candidate("bookbot/wav2vec2-ljspeech-gruut", None, "repo"),
    "charsiu": Candidate("charsiu/en_w2v2_fc_10ms",
                         "charsiu/tokenizer_en_cmu", "standard"),
    "timit-ipa": Candidate("vitouphy/wav2vec2-xls-r-300m-timit-phoneme",
                           None, "repo"),
    "timit": Candidate("excalibur12/wav2vec2-large-lv60_phoneme-timit"
                       "_english_timit-4k_simplified", None, "repo"),
}

CHOSEN = os.environ.get("ACOUSTIC_MODEL", "espeak")
if CHOSEN not in CANDIDATES:
    raise SystemExit(f"Unknown model {CHOSEN!r}. Known: "
                     + ", ".join(CANDIDATES))
CANDIDATE = CANDIDATES[CHOSEN]
MODEL = CANDIDATE.model

# Weights stored as 8-bit integers instead of 32-bit floats: a quarter of the
# size, and what will actually run on a phone. This reading is the one most
# exposed to it -- it lives on what surrounds the peak, which is the first thing
# rounding takes -- so the bench has to be able to run both and compare.
QUANTISED = os.environ.get("QUANTISED") == "1"
SLUG = CHOSEN + ("-int8" if QUANTISED else "")
SAMPLE_RATE = 16000

# What each model calls "nothing is being pronounced here". A frame classifier
# names silence outright and keeps a padding token beside it that means nothing
# acoustic, so the explicit silence is tried first. This symbol is the one the
# readings must recognise: the grid drops it, the alignment threads through it.
PAD = ("[SIL]", "<pad>", "[PAD]")

# Not sounds. A word separator, and the several names annotators give to
# silence -- a pause between words, the closure before a plosive, the boundary
# of an utterance. Letting any of them into the grid would set the comparison
# to work on emptiness, and make the alignment thread through a boundary as if
# it had been spoken.
NOT_A_SOUND = ("|", "h#", "pau", "epi", " ")

_loaded = None


def loaded():
    """Feature extractor, network and symbol table, loaded once.

    The tokenizer is deliberately not loaded: it exists to turn text into
    phonemes, which is the one direction this pipeline refuses to travel, and
    asking for it drags in a phonemizer backend the montage has no use for.
    """
    global _loaded
    if _loaded is None:
        if not os.environ.get("HF_HOME"):
            raise SystemExit(
                "Missing environment variable: HF_HOME (the weights cache)")
        import torch
        from huggingface_hub import hf_hub_download
        from transformers import AutoFeatureExtractor, AutoModelForCTC

        torch.set_grad_enabled(False)
        if CANDIDATE.extractor == "repo":
            extractor = AutoFeatureExtractor.from_pretrained(MODEL)
        else:
            from transformers import Wav2Vec2FeatureExtractor
            extractor = Wav2Vec2FeatureExtractor(
                feature_size=1, sampling_rate=SAMPLE_RATE, padding_value=0.0,
                do_normalize=True, return_attention_mask=False)
        net = AutoModelForCTC.from_pretrained(MODEL).eval()
        if QUANTISED:
            net = torch.ao.quantization.quantize_dynamic(
                net, {torch.nn.Linear}, dtype=torch.qint8)
        vocab = json.load(open(
            hf_hub_download(CANDIDATE.vocabulary or MODEL, "vocab.json"),
            encoding="utf-8"))
        symbols = [None] * len(vocab)
        for token, index in vocab.items():
            symbols[index] = token
        _loaded = (extractor, net, symbols, torch)
    return _loaded


def seconds_per_frame():
    """Read off the convolutions rather than declared: the stack decimates the
    waveform by the product of its strides, and a candidate that halves the last
    one doubles the resolution."""
    strides = loaded()[1].config.conv_stride
    product = 1
    for stride in strides:
        product *= stride
    return product / SAMPLE_RATE


def symbols():
    return loaded()[2]


def blank():
    for name in PAD:
        if name in symbols():
            return symbols().index(name)
    raise SystemExit(f"No blank symbol among {PAD} in {MODEL}")


def probabilities(wav, cache=None):
    """The matrix of `wav`: one row per 20 ms, one column per sound, summing to 1."""
    wav = Path(wav)
    if cache is not None and Path(cache).is_file():
        return np.load(cache)["probabilities"]

    audio, rate = sf.read(wav)
    if rate != SAMPLE_RATE:
        raise SystemExit(f"{wav} is at {rate} Hz, expected {SAMPLE_RATE}")
    extractor, net, _, torch = loaded()
    values = extractor(audio, sampling_rate=rate,
                       return_tensors="pt").input_values
    logits = net(values).logits[0]
    probabilities = torch.softmax(logits, dim=-1).numpy().astype(np.float32)

    if cache is not None:
        Path(cache).parent.mkdir(parents=True, exist_ok=True)
        np.savez_compressed(cache, probabilities=probabilities)
    return probabilities


def spoken():
    """The columns that stand for a sound: silence and notation excluded."""
    empty = blank()
    return [index for index, name in enumerate(symbols())
            if index != empty and name not in NOT_A_SOUND]


def grid(probabilities):
    """Free decoding: the sounds this voice actually produced, as frame spans.

    Not the "correct" sounds of the words -- the ones that are there. The label
    may be wrong without harm, since it never enters the comparison; only the
    spans matter, because they are what tells two matrices which rows face which.
    """
    best = probabilities.argmax(axis=-1)
    ignored = {blank()}
    ignored.update(symbols().index(name) for name in NOT_A_SOUND
                   if name in symbols())
    segments = []
    start = 0
    for frame in range(1, len(best) + 1):
        if frame == len(best) or best[frame] != best[start]:
            if best[start] not in ignored:
                segments.append((int(best[start]), start, frame))
            start = frame
    return segments


def align(probabilities, ids):
    """Forced alignment: where the learner says each sound of the model's grid.

    The correspondence between the two recordings passes through a shared
    symbolic landmark, never through the resemblance of the two signals -- which
    is what keeps the difference of voice out of it.
    """
    log = np.log(np.maximum(probabilities, 1e-12))
    empty = blank()
    extended = [empty]
    for index in ids:
        extended += [index, empty]

    width, length = len(extended), len(log)
    if length < width:
        raise ValueError(f"{length} frames cannot spell {len(ids)} sounds")

    score = np.full((length, width), -np.inf)
    back = np.zeros((length, width), dtype=np.int8)
    score[0, 0] = log[0, extended[0]]
    score[0, 1] = log[0, extended[1]]
    for frame in range(1, length):
        for step in range(width):
            best, origin = score[frame - 1, step], 0
            if step > 0 and score[frame - 1, step - 1] > best:
                best, origin = score[frame - 1, step - 1], 1
            skippable = (step > 1 and extended[step] != empty
                         and extended[step] != extended[step - 2])
            if skippable and score[frame - 1, step - 2] > best:
                best, origin = score[frame - 1, step - 2], 2
            score[frame, step] = best + log[frame, extended[step]]
            back[frame, step] = origin

    step = width - 1
    if score[-1, width - 2] > score[-1, width - 1]:
        step = width - 2
    path = np.empty(length, dtype=int)
    for frame in range(length - 1, -1, -1):
        path[frame] = step
        step -= back[frame, step]

    spans = []
    for position in range(len(ids)):
        frames = np.nonzero(path == 2 * position + 1)[0]
        spans.append((int(frames[0]), int(frames[-1]) + 1)
                     if len(frames) else None)
    return spans
