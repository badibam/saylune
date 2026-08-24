#!/usr/bin/env python3
"""Deux lectures des mêmes fichiers disent-elles la même chose ?

The question the embedded track has left, and the only one a desktop cannot
answer alone: what the phone reads has to be what the bench read, or nothing
measured here transfers. It is asked twice with the same instrument -- ONNX
Runtime against PyTorch here, then the phone against the desktop -- because a
difference seen on the device would otherwise have three possible causes and no
way to separate them.

What it compares is always two machines reading the same weights. Two different
roundings are not that: asked to hold 8-bit ONNX against 8-bit PyTorch it would
be measuring which quantiser rounded where, a difference that means nothing and
that no phone will ever exhibit. What rounding costs is a question for
`faults.py`, which reads it where it lands -- on the verdict.

Three readings of the difference, from the rawest to the one that decides:

  matrice  the largest gap on any cell -- what the runtime does to the numbers
  grille   the same sounds, at the same frames -- what free decoding makes of it
  écart    the gap the app actually marks on -- what survives all the way up

The last one is the verdict. The first two explain it when it fails.

    python3 concord.py                              # onnx contre torch
    python3 concord.py -r timit-ipa-onnx-int8 -a phone
"""

import argparse
import os
import sys
from pathlib import Path

import numpy as np

HERE = Path(__file__).resolve().parent
MATRICES = HERE / "out" / "matrices"

# Under this, two readings are the same reading. Ten times finer than the gap
# between two synthetic voices saying the same word, which is the spread the
# whole montage was founded on being able to ignore.
SAME_READING = 1e-3

# The gap the app marks on is a Jensen-Shannon divergence in bits, and a frank
# fault sits above 0.95 while a control sits at zero. A runtime that moves it by
# a hundredth moves no verdict.
SAME_VERDICT = 0.01


def readings(slug):
    """Every matrix cached under a reading, keyed by the audio it read."""
    root = MATRICES / slug
    if not root.is_dir():
        raise SystemExit(f"{root} manque — rien n'a été lu sous {slug}")
    return {path.relative_to(root): path for path in root.rglob("*.npz")}


def compared(reference, candidate, show):
    """Both readings on every file they share, worst case first."""
    import matrix
    import overlap

    here, there = readings(reference), readings(candidate)
    shared = sorted(set(here) & set(there), key=str)
    if not shared:
        raise SystemExit(f"aucun fichier lu à la fois sous {reference} "
                         f"et sous {candidate}")

    print(f"\n=== {candidate} contre {reference}"
          f"   ({len(shared)} fichiers)")
    print(f"    {'fichier':<44}{'matrice':>10}{'grille':>9}{'écart':>10}")

    cells, grids, gaps = [], [], []
    for name in shared:
        first = np.load(here[name])["probabilities"]
        second = np.load(there[name])["probabilities"]
        if first.shape != second.shape:
            print(f"    {str(name):<44}{'formes différentes':>29}")
            grids.append(False)
            continue

        cell = float(np.abs(first - second).max())
        one, two = matrix.grid(first), matrix.grid(second)
        same = ([sound for sound, _, _ in one] == [sound for sound, _, _ in two]
                and all(abs(a[1] - b[1]) <= 1 and abs(a[2] - b[2]) <= 1
                        for a, b in zip(one, two)))

        # The reading that decides: the model's own sounds, forced onto the
        # other matrix, and how far the two spreads sit apart. This is the
        # number the marking is made of, so this is the one a runtime may not
        # move.
        ids = [sound for sound, _, _ in one]
        worst = 0.0
        for span, (_, start, end) in zip(matrix.align(second, ids), one):
            if span is None:
                continue
            mine, _ = overlap.spread(first, (start, end))
            yours, _ = overlap.spread(second, span)
            if mine is not None and yours is not None:
                worst = max(worst, overlap.divergence(mine, yours))

        cells.append(cell)
        grids.append(same)
        gaps.append(worst)
        if show or cell > SAME_READING or not same or worst > SAME_VERDICT:
            print(f"    {str(name):<44}{cell:>10.2e}"
                  f"{'=' if same else '≠':>9}{worst:>10.3f}")

    print(f"\n    matrice, pire cellule    {max(cells):.2e}"
          f"   (même lecture sous {SAME_READING:.0e})")
    print(f"    grille identique         {sum(grids)}/{len(grids)}")
    print(f"    écart, pire son          {max(gaps):.3f}"
          f"   (même verdict sous {SAME_VERDICT})")

    passed = (max(cells) <= SAME_READING and all(grids)
              and max(gaps) <= SAME_VERDICT)
    print(f"\n    {'les deux lectures concordent' if passed else 'ELLES DIVERGENT'}")
    return 0 if passed else 1


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-r", "--reference", help="la lecture qui fait foi "
                        "(par défaut PyTorch en flottant)")
    parser.add_argument("-a", "--against", help="la lecture à confronter "
                        "(par défaut ONNX Runtime en flottant)")
    parser.add_argument("-v", "--verbose", action="store_true",
                        help="toutes les lignes, pas seulement les écarts")
    options = parser.parse_args(argv)

    # By default, the only pair that exists at rest: PyTorch in float, which
    # every measure written down so far was made with, against the same weights
    # read by the runtime a phone will run.
    os.environ.pop("RUNTIME", None)
    os.environ.pop("QUANTISED", None)
    import matrix
    return compared(options.reference or matrix.CHOSEN,
                    options.against or f"{matrix.CHOSEN}-onnx",
                    options.verbose)


if __name__ == "__main__":
    sys.exit(main())
