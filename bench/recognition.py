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


def canonical(take):
    """La suite de sons que le rendu du modèle est censé dire, en IPA.

    Les phonèmes viennent de l'annotation du corpus L2, en ARPAbet : ce sont les
    sons ATTENDUS, jamais ceux qui ont été dits. C'est ce qui les disqualifie
    pour juger un apprenant, et c'est ce qui les qualifie ici -- le rendu est
    une synthèse de ce texte, donc il dit le canonique ou la voix est fautive.

    Le repli de Lee & Hon est celui du vocabulaire lui-même (39 symboles, ni
    `ɔ` ni `ʌ` ni `ɚ`), donc traduire n'invente aucune distinction que le
    réseau pourrait rater sans tort.
    """
    out = []
    for word in take.words:
        for phone in word.phones:
            plain = phone.lower().rstrip("0123456789")
            if plain in timit.DROPPED:
                continue
            if plain not in timit.FOLD:
                return None
            out.append(timit.FOLD[plain])
    return out


def renders(voice, split, count):
    """Les rendus du modèle sur le corpus L2, et ce qu'ils devraient dire."""
    import alarms
    import learners

    every = []
    for take in learners.catalogue(split):
        wav = alarms.render_path(voice, take)
        phones = canonical(take)
        if wav.is_file() and phones:
            every.append((take.uid, wav, phones))
    if count and count < len(every):
        stride = len(every) / count
        every = [every[int(i * stride)] for i in range(count)]
    return every


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
    parser.add_argument("--source", default="timit", choices=("timit", "l2"),
                        help="timit : de la parole lue, vérité terrain à la "
                             "main. l2 : les RENDUS du modèle sur le corpus "
                             "L2, contre les phonèmes attendus du corpus — "
                             "ça juge la voix modèle, pas un apprenant")
    parser.add_argument("-c", "--candidate", default="azure-us-jenny",
                        help="la voix modèle, pour --source l2")
    args = parser.parse_args(argv)

    if args.source == "l2":
        split = "test" if args.split == "TEST" else args.split
        every = renders(args.candidate, split, args.utterances)
        if not every:
            raise SystemExit("aucun rendu — lance d'abord alarms.py --render")
        print(f"\n=== rendus {args.candidate} sur le corpus L2 ({split})")
    else:
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
    print(f"\n{matrix.audios()}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
