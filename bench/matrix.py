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
from pathlib import Path

import numpy as np
import soundfile as sf

MODEL = "facebook/wav2vec2-lv-60-espeak-cv-ft"
SAMPLE_RATE = 16000
PAD = "<pad>"

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
        extractor = AutoFeatureExtractor.from_pretrained(MODEL)
        net = AutoModelForCTC.from_pretrained(MODEL).eval()
        vocab = json.load(
            open(hf_hub_download(MODEL, "vocab.json"), encoding="utf-8"))
        symbols = [None] * len(vocab)
        for token, index in vocab.items():
            symbols[index] = token
        _loaded = (extractor, net, symbols, torch)
    return _loaded


def symbols():
    return loaded()[2]


def blank():
    return symbols().index(PAD)


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


def grid(probabilities):
    """Free decoding: the sounds this voice actually produced, as frame spans.

    Not the "correct" sounds of the words -- the ones that are there. The label
    may be wrong without harm, since it never enters the comparison; only the
    spans matter, because they are what tells two matrices which rows face which.
    """
    best = probabilities.argmax(axis=-1)
    empty = blank()
    segments = []
    start = 0
    for frame in range(1, len(best) + 1):
        if frame == len(best) or best[frame] != best[start]:
            if best[start] != empty:
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
