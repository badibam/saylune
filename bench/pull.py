#!/usr/bin/env python3
"""Fetch a candidate acoustic model, and say what it is.

The only step of the embedded track that touches the network. It prints what
decides whether a candidate is worth measuring at all: its size, because the
weights have to fit on a phone; its inventory, because a multilingual one drifts
out of the language on anything less than clean speech; and how sure of itself
it is on the bench's own audio.

That last number is the cheap filter. The comparison reads shapes, not labels,
and the tolerance lives in what surrounds the peak -- a model that puts all its
mass on one sound turns the reading back into a label test, which is the very
thing the montage refuses. Measured before anything else is run.

    python3 pull.py charsiu
"""

import argparse
import sys
from pathlib import Path

import matrix

HERE = Path(__file__).resolve().parent

# Connected English by three different voices, two of them synthetic: enough to
# see whether a candidate ever hesitates, and it is already on disk.
SURE_OF_ITSELF = (
    "out/renders/eleven-gb-daniel/sentences/think.wav",
    "out/renders/azure-gb-sonia/sentences/think.wav",
    "out/takes/set/04-th-franc.wav",
)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.parse_args(argv)

    net = matrix.loaded()[1]
    parameters = sum(tensor.numel() for tensor in net.parameters())
    print(f"\n{matrix.MODEL}")
    print(f"  {parameters / 1e6:.0f} M paramètres"
          f"  ({parameters * 4 / 1e6:.0f} Mo en flottant,"
          f" {parameters / 1e6:.0f} Mo en entiers 8 bits)")
    print(f"  trames de {1000 * matrix.seconds_per_frame():.0f} ms")
    print(f"  {len(matrix.symbols())} symboles, silence /{matrix.symbols()[matrix.blank()]}/")
    print("  " + " ".join(matrix.symbols()))

    peaks = []
    for name in SURE_OF_ITSELF:
        wav = HERE / name
        if not wav.is_file():
            continue
        probabilities = matrix.probabilities(wav)
        speech = probabilities[probabilities.argmax(axis=-1) != matrix.blank()]
        if len(speech):
            peaks.append(float(speech.max(axis=-1).mean()))
    if peaks:
        print(f"\n  certitude moyenne sur un son : {sum(peaks) / len(peaks):.3f}")
        print("  (au-delà de 0,95 il ne reste plus de forme à comparer)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
