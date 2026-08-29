#!/usr/bin/env python3
"""Freeze one analysed turn so the Kotlin port can be held against this one.

The arithmetic above the matrix now exists twice -- here in Python and on the
device in Kotlin -- and two implementations that agree about different numbers
would be worse than one that failed outright (`concord.py` makes the same
argument about the network pass). `concord.py` covers the pass itself, so what
is left unchecked is exactly what this freezes: the grid, the alignment, the
comparison and the join.

The matrices are written out rather than recomputed on the other side, which is
the point: it isolates the ported arithmetic from the engine, so a difference
can only come from the port.

    ACOUSTIC_MODEL=timit-ipa python3 fixture.py 01-sink --model think

Writes into `../app/src/testDebug/resources/fixture/<prise>-<phrase>/`, and the
Kotlin test runs every directory it finds there -- so covering a case the port
gets wrong is a matter of freezing one more pair, not of editing the test.
"""

import argparse
import json
import struct
import sys
from pathlib import Path

import matrix
import phrases
import turn

HERE = Path(__file__).resolve().parent
OUT = HERE.parent / "app" / "src" / "testDebug" / "resources" / "fixture"


def freeze(into, name, probabilities):
    """Shape then rows, little-endian: the plainest thing both sides can read."""
    frames, width = probabilities.shape
    with open(into / name, "wb") as handle:
        handle.write(struct.pack("<ii", frames, width))
        handle.write(probabilities.astype("<f4").tobytes())
    return frames, width


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("take", help="une prise de out/takes/set, sans .wav")
    parser.add_argument("-m", "--model", required=True)
    parser.add_argument("-c", "--candidate", default="eleven-us-eric")
    options = parser.parse_args(argv)

    text = dict(phrases.CALIBRATION + phrases.HELDOUT)[options.model]
    learner = turn.TAKES / f"{options.take}.wav"
    model = turn.RENDERS / options.candidate / "sentences" / f"{options.model}.wav"
    for wav in (learner, model):
        if not wav.is_file():
            raise SystemExit(f"{wav} manque")

    into = OUT / f"{options.take}-{options.model}"
    into.mkdir(parents=True, exist_ok=True)
    shapes = {
        "model.matrix": freeze(into, "model.matrix", matrix.probabilities(model)),
        "said.matrix": freeze(into, "said.matrix", matrix.probabilities(learner)),
    }
    (into / "vocab.json").write_text(
        json.dumps({name: index for index, name in enumerate(matrix.symbols())},
                   indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    expected = turn.read(options.take, options.model, options.candidate)
    if expected is None:
        raise SystemExit("turn.py n'a rien rendu sur cette paire")
    (into / "expected.json").write_text(
        json.dumps(expected, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8")

    print(f"  {into}")
    for name, (frames, width) in shapes.items():
        print(f"  {name}: {frames} trames x {width} sons")
    print(f"  {len(expected['phonemes'])} marques, "
          f"{len(expected['gutters'])} gouttières sur « {text} »")
    return 0


if __name__ == "__main__":
    sys.exit(main())
