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
import difflib
import statistics
import sys
from pathlib import Path

import engine
import synth
import phrases

HERE = Path(__file__).resolve().parent
TAKES = HERE / "out" / "takes"
RENDERS = HERE / "out" / "renders"
READINGS = HERE / "out" / "readings"

FAULT_GAP = -15.0


MIN_TAKE_SECONDS = 0.5


def reading(wav, text, dialect, tag, slug):
    """Score the take, or return None when there is no take to score.

    An empty capture is skipped by name rather than sent to the engine: a wav
    with no speech in it comes back as a refusal or, worse, as a row of zeros
    that would read as a catastrophic gap.
    """
    if not wav.is_file() or engine.duration_seconds(wav) < MIN_TAKE_SECONDS:
        return None
    return engine.score(wav, text, dialect=dialect,
                        cache=READINGS / f"{tag}@{dialect}" / f"{slug}.json")


def gaps(model, take):
    """Align the two phone sequences, then read the gap where they correspond.

    Position is not the pairing: the engine returns the sequence it aligned, not
    the lexicon's expansion of the text, so a take can carry an insertion or a
    deletion the model does not. Pairing by index survives until the first such
    divergence and silently discards the rest of the sentence -- which is how a
    broken comparison can still look like a full one.

    Sounds the two sides do not share are not a gap to be measured. They are a
    different fault, of a kind this reading has nothing to say about.
    """
    reference_phones = [p.phone for p in model.phones]
    attempt_phones = [p.phone for p in take.phones]
    matcher = difflib.SequenceMatcher(a=reference_phones, b=attempt_phones,
                                      autojunk=False)

    paired = []
    for start_a, start_b, size in matcher.get_matching_blocks():
        for offset in range(size):
            reference = model.phones[start_a + offset]
            attempt = take.phones[start_b + offset]
            if engine.degenerate(reference) or engine.degenerate(attempt):
                continue
            if reference.quality is None or attempt.quality is None:
                continue
            paired.append((reference, attempt,
                           attempt.quality - reference.quality))
    return paired


def run(candidate, dialect, labels, show, chosen, takes, which):
    print(f"\n  modèle {candidate.name}, dictionnaire {dialect}")
    print(f"    {'prise':<14}{'écart moyen':>12}{'pire':>8}"
          f"{f'  sons < {FAULT_GAP:.0f}':>14}{'  sons lus':>10}")

    for label in labels:
        every = []
        worst_of = []
        skipped = []
        for slug, text in chosen:
            take = reading(takes / label / f"{slug}.wav", text,
                           dialect, f"take-{which}-{label}", slug)
            if take is None:
                skipped.append(slug)
                continue
            # The model is cache, not a recording: render it if this voice has
            # not served yet, rather than refusing to compare against it.
            render = RENDERS / candidate.name / which / f"{slug}.wav"
            synth.render(text, candidate, render)
            model = reading(render, text, dialect,
                            f"model-{which}-{candidate.name}", slug)
            paired = gaps(model, take)
            every.extend(value for _, _, value in paired)
            worst_of.extend((value, reference.phone, reference.word, slug)
                            for reference, _, value in paired)

        if not every:
            print(f"    {label:<14}  aucune paire lisible")
            continue
        failed = [value for value in every if value <= FAULT_GAP]
        note = f"   (sans {', '.join(skipped)})" if skipped else ""
        print(f"    {label:<14}{statistics.mean(every):>12.1f}"
              f"{min(every):>8.1f}{len(failed):>14}{len(every):>10}{note}")
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
    parser.add_argument("-s", "--set", dest="which", default="words",
                        choices=sorted(phrases.SETS))
    parser.add_argument("-v", "--verbose", action="store_true")
    args = parser.parse_args(argv)
    chosen = phrases.SETS[args.which]
    takes = TAKES / args.which

    # Each copy is read against the model it was copied from; the cold take is
    # read against both, since it is the same audio either way.
    for accent, name in (("gb", args.gb), ("us", args.us)):
        if name not in synth.BY_NAME:
            raise SystemExit(f"Voix inconnue {name!r}")
        candidate = synth.BY_NAME[name]
        labels = ["spontaneous", f"copy-{accent}"]
        print(f"\n=== modèle {accent.upper()} : {candidate.name}   "
              f"{len(chosen)} phrases")
        for dialect in args.dialect or ["en-gb", "en-us"]:
            run(candidate, dialect, labels, args.verbose, chosen, takes, args.which)
    return 0


if __name__ == "__main__":
    sys.exit(main())
