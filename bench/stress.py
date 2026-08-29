#!/usr/bin/env python3
"""How much longer is the stressed syllable? The truth alone, no network.

Brick 7 was going to read lexical stress off duration, and the decision was
written before the number so it could not be negotiated in front of it
(`../TODO.md`): if the lengthening clears the instrument's error at the ninth
decile, duration serves as designed; if it does not, brick 7 gets rewritten
around vowel reduction, and duration only corroborates.

This measures the left-hand side of that comparison, and it touches no acoustic
model at all -- TIMIT's boundaries are hand-placed and its dictionary marks the
stressed vowel, so what comes out is the signal an instrument would have to
see. `boundaries.py` measures the other side, on the same utterances.

Two readings of the same words, because the answer moves between them:

- **nucleus to nucleus** is the duration `boundaries.py` measures and the one
  brick 7 would consume -- but the last syllable of a word takes every coda
  consonant with it, so it inflates whichever syllable happens to be last.
- **the nucleus alone** is the vowel's own span, free of that, and it is the
  reading that flatters duration most. It is here as the ceiling: whatever
  duration cannot do on this reading, it cannot do at all.

A nucleus is a vowel or a syllabic consonant -- `able` is two syllables, and
dropping its `el` would silently throw away every word like it.

    cd bench && python3 stress.py
    cd bench && python3 stress.py -n 0      # every utterance of the split
"""

import argparse
import statistics
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from train import timit  # noqa: E402  (path has to be set first)

SAMPLE_RATE = 16000
DICTIONARY = timit.CORPUS / "DOC" / "TIMITDIC.TXT"

# Nuclei on TIMIT's own symbols rather than the folded IPA: the folding sends
# the syllabic consonants to their plain counterparts (`el` to `l`), which is
# right for a phone inventory and wrong for counting syllables.
VOWELS = frozenset("aa ae ah ao aw ax ax-h axr ay eh er ey ih ix iy ow oy "
                   "uh uw ux".split())
SYLLABIC = frozenset("el em en eng".split())
NUCLEI = VOWELS | SYLLABIC

# Function words are excluded because their whole syllable structure is subject
# to reduction, which is a different phenomenon from the one being measured and
# would sit in the numbers as noise. The list is short and blunt on purpose: it
# never leaves the bench, and a word wrongly kept costs one sample.
FUNCTION = frozenset("""
a an the this that these those and or but nor so yet for of to in on at by
with from into onto upon about above below under over between through during
is are was were be been being am do does did done have has had having
will would shall should can could may might must
i you he she it we they me him her us them my your his its our their
there here what which who whom whose when where why how
not no yes if then than as too very just only also even still
""".split())


# The vowels English reduces to. TIMIT tells them apart from their full
# counterparts, which is what makes this measurable at all; the syllabic
# consonants carry no vowel and are unstressed by construction.
REDUCED = frozenset("ax ax-h ix axr".split()) | SYLLABIC

# The same question in two inventories that are ours, and they are not the same
# one. The vocabulary the model in service writes holds `ə` beside `ʌ` and `ɚ`
# beside `ɝ`, so it keeps the reduced vowel apart from its full counterpart.
# Our own fine-tuning targets Lee & Hon's 39 classes, which merge both pairs
# and leave only the schwa standing. Neither tells `ix` from `ih`, so `ɪ` --
# reduced in `roses`, full in `sink` -- says nothing either way.
SPOKEN = {"ah": "ʌ", "ax": "ə", "ax-h": "ə", "er": "ɝ", "axr": "ɚ"}
SPOKEN_REDUCED = frozenset(["ə", "ɚ"]) | SYLLABIC
FOLDED_REDUCED = frozenset(["ə"]) | SYLLABIC


def folded(symbol):
    from train.timit import FOLD
    return FOLD.get(symbol, symbol)


def spoken_as(symbol):
    """The symbol as the vocabulary in service writes it."""
    if symbol in SPOKEN:
        return SPOKEN[symbol]
    return folded(symbol)


def dictionary():
    """word -> the stress of each of its nuclei, read from TIMITDIC.

    A stress is the digit TIMIT attaches to the nucleus: 1 primary, 2
    secondary, absent unstressed. Only the positions are kept -- what the
    nucleus *is* never enters this measure.
    """
    table = {}
    for line in DICTIONARY.read_text(encoding="latin-1").splitlines():
        if line.startswith(";") or "/" not in line:
            continue
        word, _, rest = line.partition("/")
        stresses = []
        for phone in rest.rstrip("/ ").split():
            digit = phone[-1] if phone[-1].isdigit() else ""
            if (phone[:-1] if digit else phone) in NUCLEI:
                stresses.append(int(digit) if digit else 0)
        if stresses:
            table[word.strip()] = stresses
    return table


def words(wrd):
    for line in Path(wrd).read_text(encoding="utf-8").splitlines():
        start, stop, text = line.split()
        yield text, int(start) / SAMPLE_RATE, int(stop) / SAMPLE_RATE


def phones(phn):
    rows = []
    for line in Path(phn).read_text(encoding="utf-8").splitlines():
        start, stop, symbol = line.split()
        rows.append((symbol, int(start) / SAMPLE_RATE, int(stop) / SAMPLE_RATE))
    return rows


def durations(rows, start, stop):
    """One word's syllables, both readings, on the hand-placed boundaries.

    Returns None when the word carries a pause: a duration that steps over one
    is not a syllable, and nothing here is worth guessing at.
    """
    inside = [row for row in rows if row[1] >= start and row[2] <= stop]
    if any(symbol in timit.PAUSES for symbol, _, _ in inside):
        return None
    nuclei = [row for row in inside if row[0] in NUCLEI]
    if not nuclei:
        return None
    edges = [begin for _, begin, _ in nuclei] + [stop]
    spans = [second - first for first, second in zip(edges, edges[1:])]
    own = [end - begin for _, begin, end in nuclei]
    return spans, own


def rates(spans, mark):
    """The stressed syllable against its rivals: the mean, then the longest."""
    here = spans[mark]
    others = [span for rank, span in enumerate(spans) if rank != mark]
    if not others or min(others) <= 0:
        return None
    return here / statistics.mean(others), here / max(others)


def picked(nuclei, spans, reduced):
    """Which syllable a rule reading reduction alone would call the strong one.

    Returns the rank it picks, or None when it cannot: no full vowel at all, or
    several, in which case duration is asked to break the tie -- which is the
    corroborating role the measure above left it, tried here rather than
    assumed. The two answers are kept apart so the tie-break can be priced.
    """
    full = [rank for rank, symbol in enumerate(nuclei) if symbol not in reduced]
    if len(full) == 1:
        return full[0], "seule"
    if not full:
        return None, "aucune"
    return max(full, key=lambda rank: spans[rank]), "départagée"


def measured(utterance, table):
    """Per word: the two readings, and whether the stress falls last."""
    phn = Path(utterance).with_suffix(".PHN")
    wrd = Path(utterance).with_suffix(".WRD")
    if not (phn.is_file() and wrd.is_file()):
        return [], 0
    rows = phones(phn)
    found, skipped = [], 0
    for text, start, stop in words(wrd):
        stresses = table.get(text)
        if stresses is None or len(stresses) < 2 or 1 not in stresses:
            continue
        if text in FUNCTION:
            continue
        read = durations(rows, start, stop)
        if read is None:
            continue
        spans, own = read
        # The realisation dropped or added a nucleus: nothing lines the two
        # sequences up any more, and guessing which one went missing would be
        # inventing the measurement.
        if len(spans) != len(stresses):
            skipped += 1
            continue
        mark = stresses.index(1)
        between, inside = rates(spans, mark), rates(own, mark)
        if between is None or inside is None:
            continue
        nuclei = [symbol for symbol, _, _ in
                  [row for row in rows if start <= row[1] and row[2] <= stop
                   and row[0] in NUCLEI]]
        reduction = {
            "timit": picked(nuclei, own, REDUCED),
            "en service": picked([spoken_as(s) for s in nuclei], own,
                                 SPOKEN_REDUCED),
            "notre repli": picked([folded(s) for s in nuclei], own,
                                  FOLDED_REDUCED),
        }
        found.append((between, inside, mark == len(stresses) - 1,
                      mark, reduction))
    return found, skipped


def quantile(values, q):
    return sorted(values)[int(q * (len(values) - 1))]


def line(name, values):
    print(f"      {name:<26}médiane {statistics.median(values):5.2f}×"
          f"   1er décile {quantile(values, 0.1):5.2f}×"
          f"   9e décile {quantile(values, 0.9):5.2f}×"
          f"   plus longue {100 * sum(1 for v in values if v > 1) / len(values):5.1f} %")


def report(found, skipped, utterances):
    print(f"\n=== {utterances} énoncés, {len(found)} mots pleins de deux "
          f"syllabes ou plus")
    print(f"    {skipped} écartés : la réalisation n'a pas le nombre de "
          f"noyaux du dictionnaire")
    final = sum(1 for row in found if row[2])
    print(f"    accent sur la dernière syllabe dans "
          f"{100 * final / len(found):.1f} % des mots")
    for rank, reading in ((0, "noyau à noyau"), (1, "noyau seul")):
        print(f"\n    {reading}")
        line("contre la moyenne", [row[rank][0] for row in found])
        line("contre la plus longue", [row[rank][1] for row in found])

    print("\n    la réduction vocalique : la syllabe forte est celle qui "
          "n'est pas réduite")
    for inventory in ("timit", "en service", "notre repli"):
        cases = [(row[4][inventory], row[3]) for row in found]
        right = sum(1 for (guess, _), mark in cases if guess == mark)
        alone = [(guess, why, mark) for (guess, why), mark in cases
                 if why == "seule"]
        split = [(guess, why, mark) for (guess, why), mark in cases
                 if why == "départagée"]
        none = sum(1 for (_, why), _ in cases if why == "aucune")
        print(f"      {inventory:<13}{100 * right / len(cases):5.1f} % justes"
              f"   —   une seule voyelle pleine : {len(alone)} mots, "
              f"{100 * sum(1 for g, _, m in alone if g == m) / max(len(alone), 1):.1f} % justes")
        print(f"      {'':<13}départagée par la durée : {len(split)} mots, "
              f"{100 * sum(1 for g, _, m in split if g == m) / max(len(split), 1):.1f} % justes"
              f"   —   aucune voyelle pleine : {none} mots")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-n", "--utterances", type=int, default=200,
                        help="how many of the split to read; 0 reads all")
    parser.add_argument("-s", "--split", default="TEST")
    args = parser.parse_args(argv)

    every = list(timit.utterances(args.split))
    if not every:
        raise SystemExit(f"aucun énoncé sous {timit.CORPUS / args.split}")
    if args.utterances and args.utterances < len(every):
        # The same evenly spaced draw as `boundaries.py`, so the two sides of
        # the comparison are read off the same utterances.
        stride = len(every) / args.utterances
        every = [every[int(i * stride)] for i in range(args.utterances)]

    table = dictionary()
    found, skipped = [], 0
    for _, wav, _ in every:
        rows, missed = measured(wav, table)
        found += rows
        skipped += missed
    if not found:
        raise SystemExit("aucun mot mesurable — le dictionnaire n'a rien rendu")
    report(found, skipped, len(every))
    return 0


if __name__ == "__main__":
    sys.exit(main())
