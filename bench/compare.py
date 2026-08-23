#!/usr/bin/env python3
"""Read a take by its gap to the model, which is the only reading the app makes.

A raw score means nothing on its own: each sound has its own normal on this
engine, and a low note can be a quirk of the tool rather than a fault. What the
app marks is the distance between the learner and a model of the very same
sentence, scored by the same engine -- the tool's own bias sits on both sides
and cancels.

Measured before (`docs/design/speechace.md`): control takes land within a point
of the model, detected faults fifteen points or more below, and nothing in
between. That empty band is what this prints, per scoring lexicon, so that the
question "does the gap survive a lexicon that does not match the voice" gets an
answer instead of an argument.
"""

import argparse
import statistics
import sys
from pathlib import Path

import engine
import synth
from phrases import ACCENT_TRIAL

HERE = Path(__file__).resolve().parent
TAKES = HERE / "out" / "takes"
RENDERS = HERE / "out" / "renders"
READINGS = HERE / "out" / "readings"

FAULT_GAP = -15.0


def reading(wav, text, dialect, tag, slug):
    if not wav.is_file():
        raise SystemExit(f"Missing take: {wav}")
    return engine.score(wav, text, dialect=dialect,
                        cache=READINGS / f"{tag}@{dialect}" / f"{slug}.json")


def gaps(model, take):
    """Pair phones by position and keep the pairs both sides could read.

    Same text and same lexicon give the same expected sequence, so position is
    the pairing. A dropout on either side is dropped rather than read as a gap:
    it would charge the speaker for the aligner losing the thread.
    """
    paired = []
    for reference, attempt in zip(model.phones, take.phones):
        if reference.phone != attempt.phone:
            continue
        if engine.degenerate(reference) or engine.degenerate(attempt):
            continue
        if reference.quality is None or attempt.quality is None:
            continue
        paired.append((reference, attempt, attempt.quality - reference.quality))
    return paired


def run(candidate, dialect, labels, show):
    print(f"\n  modèle {candidate.name}, dictionnaire {dialect}")
    print(f"    {'prise':<14}{'écart moyen':>12}{'pire':>8}"
          f"{f'  sons < {FAULT_GAP:.0f}':>14}{'  sons lus':>10}")

    for label in labels:
        every = []
        worst_of = []
        for slug, text in ACCENT_TRIAL:
            model = reading(RENDERS / candidate.name / f"{slug}.wav", text,
                            dialect, f"model-{candidate.name}", slug)
            take = reading(TAKES / label / f"{slug}.wav", text,
                           dialect, f"take-{label}", slug)
            paired = gaps(model, take)
            every.extend(value for _, _, value in paired)
            worst_of.extend((value, reference.phone, reference.word, slug)
                            for reference, _, value in paired)

        if not every:
            print(f"    {label:<14}  aucune paire lisible")
            continue
        failed = [value for value in every if value <= FAULT_GAP]
        print(f"    {label:<14}{statistics.mean(every):>12.1f}"
              f"{min(every):>8.1f}{len(failed):>14}{len(every):>10}")
        if show:
            worst_of.sort()
            for value, phone, word, slug in worst_of[:6]:
                print(f"        {value:>7.1f}  /{phone}/ dans {word!r} ({slug})")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--gb", default="eleven-gb-daniel")
    parser.add_argument("--us", default="eleven-us-eric")
    parser.add_argument("-d", "--dialect", action="append", default=None,
                        help="repeatable; default compares en-gb and en-us")
    parser.add_argument("-v", "--verbose", action="store_true")
    args = parser.parse_args(argv)

    # Each copy is read against the model it was copied from; the cold take is
    # read against both, since it is the same audio either way.
    for accent, name in (("gb", args.gb), ("us", args.us)):
        if name not in synth.BY_NAME:
            raise SystemExit(f"Voix inconnue {name!r}")
        candidate = synth.BY_NAME[name]
        labels = ["spontaneous", f"copy-{accent}"]
        print(f"\n=== modèle {accent.upper()} : {candidate.name}   "
              f"{len(ACCENT_TRIAL)} phrases")
        for dialect in args.dialect or ["en-gb", "en-us"]:
            run(candidate, dialect, labels, args.verbose)
    return 0


if __name__ == "__main__":
    sys.exit(main())
