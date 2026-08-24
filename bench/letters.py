#!/usr/bin/env python3
"""Where each letter of a known text is spoken, computed on our own machine.

The marks the app draws sit on characters of the displayed text, and until now
the only thing that knew where a character was pronounced was the synthesis
provider -- which is what ties the whole montage to a single one of them
(`../docs/reference.md`). This brick is the way out: a second CTC network whose
output is letters instead of phonemes, forced onto the text, which for a model
render is known exactly. Letters and sounds then join *by time*, on the same
audio, with no grapheme-to-phoneme table anywhere.

The text being known is what makes this cheap and safe: nothing is recognised
here. The trellis is constrained to spell the sentence we asked for, so the
question asked of the network is only *when*, never *what*.

    python3 letters.py out/renders/eleven-us-eric/sentences/think.wav \\
        "I think this ship is cheap"
"""

import argparse
import json
import sys
from pathlib import Path

import numpy as np
import soundfile as sf

import matrix

HERE = Path(__file__).resolve().parent

# The letter network, chosen for its licence as much as for its accuracy: the
# card is Apache-2.0 and the fairseq weights upstream are MIT, so it can be
# carried in a release without an `NonFreeAssets` anti-feature.
MODEL = "facebook/wav2vec2-base-960h"

# Its own reading, never mixed with the phoneme matrices: same audio, different
# network, and a cache that confused the two would align letters on sounds.
SLUG = "letters"
MATRICES = HERE / "out" / "matrices" / SLUG

# What this vocabulary calls the empty column and the space between words. The
# separator is aligned like any other symbol -- it is where the network says the
# words part, which is exactly the boundary a mark needs.
BLANK = "<pad>"
SEPARATOR = "|"

_loaded = None
_symbols = None
_config = None


def configured():
    global _config
    if _config is None:
        _config = json.load(open(
            matrix.cached()(MODEL, "config.json"), encoding="utf-8"))
    return _config


def symbols():
    """The letter table, read from the vocabulary file rather than a network."""
    global _symbols
    if _symbols is None:
        vocab = json.load(open(
            matrix.cached()(MODEL, "vocab.json"), encoding="utf-8"))
        _symbols = {token: index for token, index in vocab.items()}
    return _symbols


def seconds_per_frame():
    product = 1
    for stride in configured()["conv_stride"]:
        product *= stride
    return product / matrix.SAMPLE_RATE


def loaded():
    global _loaded
    if _loaded is None:
        matrix.cached()
        import torch
        from transformers import AutoModelForCTC

        torch.set_grad_enabled(False)
        _loaded = (AutoModelForCTC.from_pretrained(MODEL).eval(), torch)
    return _loaded


def probabilities(wav, cache=None):
    """The letter matrix of `wav`: one row per frame, one column per letter."""
    wav = Path(wav)
    if cache is not None and Path(cache).is_file():
        return np.load(cache)["probabilities"]

    audio, rate = sf.read(wav)
    if rate != matrix.SAMPLE_RATE:
        raise SystemExit(f"{wav} is at {rate} Hz, "
                         f"expected {matrix.SAMPLE_RATE}")
    net, torch = loaded()
    logits = net(torch.from_numpy(matrix.prepared(audio))).logits[0]
    spread = torch.softmax(logits, dim=-1).numpy().astype(np.float32)

    if cache is not None:
        Path(cache).parent.mkdir(parents=True, exist_ok=True)
        np.savez_compressed(cache, probabilities=spread)
    return spread


def spelled(text):
    """The text as this vocabulary spells it, each token keeping its position.

    Returns the token ids and, beside each, the index of the character it came
    from -- the anchor is on the displayed string, so the correspondence back to
    it has to survive the whole calculation.

    A character the vocabulary cannot spell -- punctuation, a digit -- yields no
    token: it is silent in the audio too. A digit is another matter and belongs
    upstream: the synthesis says "twenty-five" for `25`, and a text that reaches
    here in figures is a text the model and the aligner do not read alike.
    """
    table = symbols()
    ids, positions = [], []
    for position, character in enumerate(text):
        token = SEPARATOR if character.isspace() else character.upper()
        index = table.get(token)
        if index is None:
            continue
        # Two separators running (a space after a comma) would ask the trellis
        # for a boundary that was never uttered twice.
        if token == SEPARATOR and ids and ids[-1] == index:
            continue
        ids.append(index)
        positions.append(position)
    if not ids:
        raise SystemExit(f"nothing spellable in {text!r}")
    return ids, positions


def anchors(wav, text, cache=None):
    """Each character of `text`, and the seconds of audio it is spoken in.

    One entry per character of the string as given, `None` where the character
    carries no sound of its own. Characters that share a frame span are the
    ordinary case, not a defect: a letter can carry two sounds and a sound can
    span two letters, and the trellis says where the network changed its mind,
    not where a dictionary would cut.
    """
    spread = probabilities(wav, cache=cache)
    ids, positions = spelled(text)
    spans = matrix.align(spread, ids, empty=symbols()[BLANK])

    step = seconds_per_frame()
    found = [None] * len(text)
    for position, span in zip(positions, spans):
        if span is not None:
            found[position] = (span[0] * step, span[1] * step)
    return found


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("wav", type=Path)
    parser.add_argument("text")
    args = parser.parse_args(argv)

    if not args.wav.is_file():
        raise SystemExit(f"{args.wav} manque")

    for character, span in zip(args.text, anchors(args.wav, args.text)):
        shown = repr(character) if character.isspace() else character
        if span is None:
            print(f"  {shown:<4}        —")
        else:
            print(f"  {shown:<4}  {span[0]:7.3f} → {span[1]:7.3f}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
