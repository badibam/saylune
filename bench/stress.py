#!/usr/bin/env python3
"""Read stress the only way it can be read: the engine on the learner against the
engine on the model.

The engine also announces the stress its lexicon expects, and that number is not
usable -- on flawless synthetic audio it contradicts its own lexicon on 41% of
polysyllabic words, and its reading of one word shifts with what happens
elsewhere in the sentence. Comparing its reading of the learner to its reading of
the model puts the same bias on both sides, where it cancels.

Run under both lexicons, this answers what calibration alone could not: whether
the comparison still separates a copy from a cold take when the scoring lexicon
disagrees with the voice's accent.
"""

import argparse
import sys
from pathlib import Path

import engine
import synth
import phrases

HERE = Path(__file__).resolve().parent
TAKES = HERE / "out" / "takes"
RENDERS = HERE / "out" / "renders"
READINGS = HERE / "out" / "readings"

MIN_TAKE_SECONDS = 0.5


def reading(wav, text, dialect, tag, slug):
    if not wav.is_file() or engine.duration_seconds(wav) < MIN_TAKE_SECONDS:
        return None
    return engine.score(wav, text, dialect=dialect, extras=engine.INTONATION,
                        cache=READINGS / f"{tag}@{dialect}+into" / f"{slug}.json")


def stressed(syllables, word):
    """Which syllable of `word` the engine read as the strong one."""
    own = [s for s in syllables if s.word.lower() == word.lower()]
    if len(own) < 2:
        return None
    marked = [i for i, s in enumerate(own) if s.predicted == 1]
    if len(marked) != 1:
        # Neither a verdict nor an absence of one: the engine marked every
        # syllable or none. Counting it as agreement would invent a result.
        return ("ambigu", len(marked), len(own))
    return ("syllabe", marked[0], len(own))


def polysyllabic(model):
    seen = []
    for syllable in model.syllables:
        if syllable.word.lower() not in seen:
            seen.append(syllable.word.lower())
    return [w for w in seen
            if len([s for s in model.syllables if s.word.lower() == w]) > 1]


def run(candidate, dialect, labels, show, chosen, takes, which):
    print(f"\n  modèle {candidate.name}, dictionnaire {dialect}")
    for label in labels:
        agree = disagree = ambiguous = 0
        detail = []
        for slug, text in chosen:
            take = reading(takes / label / f"{slug}.wav", text, dialect,
                           f"take-{which}-{label}", slug)
            if take is None:
                continue
            # The model is cache, not a recording: render it if this voice has
            # not served yet, rather than refusing to compare against it.
            render = RENDERS / candidate.name / which / f"{slug}.wav"
            synth.render(text, candidate, render)
            model = reading(render, text, dialect,
                            f"model-{which}-{candidate.name}", slug)
            for word in polysyllabic(model):
                left, right = stressed(model.syllables, word), stressed(take.syllables, word)
                if left is None or right is None:
                    continue
                if left[0] == "ambigu" or right[0] == "ambigu":
                    ambiguous += 1
                    continue
                if left[1] == right[1]:
                    agree += 1
                else:
                    disagree += 1
                    detail.append(f"{word} ({slug}) modèle syll.{left[1] + 1} "
                                  f"-> prise syll.{right[1] + 1}")
        total = agree + disagree
        rate = f"{disagree}/{total}" if total else "-"
        print(f"    {label:<14}accord {agree:>3}   désaccord {disagree:>3}   "
              f"({rate})   indécis {ambiguous:>3}")
        if show:
            for line in detail:
                print(f"        {line}")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--gb", default="eleven-gb-daniel")
    parser.add_argument("--us", default="eleven-us-eric")
    parser.add_argument("-d", "--dialect", action="append", default=None)
    parser.add_argument("-s", "--set", dest="which", default="words",
                        choices=sorted(phrases.SETS))
    parser.add_argument("-v", "--verbose", action="store_true")
    args = parser.parse_args(argv)
    chosen = phrases.SETS[args.which]
    takes = TAKES / args.which

    for accent, name in (("gb", args.gb), ("us", args.us)):
        if name not in synth.BY_NAME:
            raise SystemExit(f"Voix inconnue {name!r}")
        candidate = synth.BY_NAME[name]
        print(f"\n=== modèle {accent.upper()} : {candidate.name}")
        for dialect in args.dialect or ["en-gb", "en-us"]:
            run(candidate, dialect, ["spontaneous", f"copy-{accent}"],
                args.verbose, chosen, takes, args.which)
    return 0


if __name__ == "__main__":
    sys.exit(main())
