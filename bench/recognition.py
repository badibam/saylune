#!/usr/bin/env python3
"""Does free decoding name the right sounds?

The one question the rest of the bench does not ask. `faults.py` compares
spreads and says outright that the label may be wrong without harm;
`boundaries.py` forces the expected sequence, so it never exercises free
decoding at all; `missing_mass.py` reads mass at positions it was told to look
at. None of them would notice a network that hears the wrong phone.

That matters beyond a sanity check: brick 3 of the analysis -- the model's grid,
which everything else anchors to -- *is* a free decoding. A network whose
decoding drifts gives a worse grid, and the drift would show up nowhere else.

The phone error rate is the edit distance to TIMIT's own transcription over the
number of expected sounds. Its split matters as much as its total: peakiness
loses sounds, so it shows up as **deletions** rather than substitutions.

    cd bench && ACOUSTIC_MODEL=timit-ipa python3 recognition.py

A caveat to carry: this reads the same matrix as every other brick, so it is a
fourth question rather than an outside witness. What it cannot do is tell a
well-placed wrong sound from a well-placed right one -- which is precisely what
nothing else can do either.
"""

import argparse
import sys
from pathlib import Path

import matrix

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from train import timit  # noqa: E402  (path has to be set first)


def decoded(probabilities):
    """The greedy CTC decoding: the sounds the network actually put there."""
    table = matrix.symbols()
    return [table[index] for index, _, _ in matrix.grid(probabilities)]


def distance(reference, hypothesis):
    """Return (substitutions, deletions, insertions) of the best alignment.

    Deletions are counted against the reference: a sound that was said and that
    the network did not produce. That is the column peakiness fills.
    """
    rows, cols = len(reference) + 1, len(hypothesis) + 1
    cost = [[0] * cols for _ in range(rows)]
    trace = [[None] * cols for _ in range(rows)]
    for i in range(1, rows):
        cost[i][0], trace[i][0] = i, "d"
    for j in range(1, cols):
        cost[0][j], trace[0][j] = j, "i"
    for i in range(1, rows):
        for j in range(1, cols):
            if reference[i - 1] == hypothesis[j - 1]:
                cost[i][j], trace[i][j] = cost[i - 1][j - 1], "="
                continue
            options = ((cost[i - 1][j - 1] + 1, "s"),
                       (cost[i - 1][j] + 1, "d"),
                       (cost[i][j - 1] + 1, "i"))
            cost[i][j], trace[i][j] = min(options)

    counts = {"s": 0, "d": 0, "i": 0, "=": 0}
    i, j = len(reference), len(hypothesis)
    while i or j:
        move = trace[i][j]
        counts[move] += 1
        if move in ("=", "s"):
            i, j = i - 1, j - 1
        elif move == "d":
            i -= 1
        else:
            j -= 1
    return counts["s"], counts["d"], counts["i"]


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-n", "--utterances", type=int, default=200,
                        help="how many of the test split to read; 0 reads all")
    parser.add_argument("-s", "--split", default="TEST")
    args = parser.parse_args(argv)

    every = list(timit.utterances(args.split))
    if not every:
        raise SystemExit(f"aucun énoncé sous {timit.CORPUS / args.split}")
    if args.utterances and args.utterances < len(every):
        stride = len(every) / args.utterances
        every = [every[int(i * stride)] for i in range(args.utterances)]

    expected = substituted = deleted = inserted = 0
    for index, (_, wav, phones) in enumerate(every, 1):
        if not phones:
            continue
        s, d, i = distance(phones, decoded(matrix.probabilities(wav)))
        expected += len(phones)
        substituted, deleted, inserted = substituted + s, deleted + d, inserted + i
        if index % 25 == 0:
            print(f"  {index}/{len(every)}", file=sys.stderr, flush=True)

    errors = substituted + deleted + inserted
    print(f"\n=== {matrix.SLUG} — {len(every)} énoncés, {expected} sons attendus")
    print(f"    PER                      {errors / expected * 100:6.1f} %")
    print(f"    substitutions            {substituted / expected * 100:6.1f} %"
          f"   ({substituted})")
    print(f"    omissions                {deleted / expected * 100:6.1f} %"
          f"   ({deleted})   <- la colonne que la peakiness remplit")
    print(f"    insertions               {inserted / expected * 100:6.1f} %"
          f"   ({inserted})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
