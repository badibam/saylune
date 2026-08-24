#!/usr/bin/env python3
"""Do two different voices produce comparable spreads?

The first thing the embedded pipeline has to answer, and the one everything else
waits on (`docs/analysis.md`). If the network keeps a trace of
who is speaking in the shape of its distributions, two flawless takes will
overlap badly and the app will mark thin air.

So the same sentence is read three ways against one model voice, from the purest
case to the noisiest: another synthetic voice saying it correctly, the learner
copying the model he has just heard, and the learner cold, before hearing
anything. The first isolates the voice alone, the second adds a human mouth, the
third adds real faults. The montage lives if the first two sit low and the third
separates from them.

No API call: every render and every take this reads already exists on disk.
"""

import argparse
import statistics
import sys
from collections import namedtuple
from pathlib import Path

import numpy as np
import soundfile as sf

import matrix
import phrases
import synth

HERE = Path(__file__).resolve().parent
TAKES = HERE / "out" / "takes"
RENDERS = HERE / "out" / "renders"
# Keyed by the acoustic model: two models read the same file differently, and
# a cache that forgot which one produced a matrix would compare across them.
MATRICES = HERE / "out" / "matrices" / matrix.SLUG

# Where the model puts a sound, and how far the other recording sits from it.
Gap = namedtuple("Gap", "value symbol seconds")


# An empty capture is skipped by name rather than sent to the network: there is
# no speech in it to spread over anything.
MIN_SECONDS = 0.5

# A sound whose frames are mostly silence on either side has nothing to compare:
# the spread would be read off the gaps rather than off the speech.
EMPTY_MASS = 0.9


def spread(probabilities, span):
    """The mean spread over a span, silence dropped and the rest renormalised."""
    rows = probabilities[span[0]:span[1]]
    mean = rows.mean(axis=0)
    empty = mean[matrix.blank()]
    speech = mean[matrix.spoken()]
    total = speech.sum()
    if total <= 0:
        return None, empty
    return speech / total, empty


def divergence(first, second):
    """Jensen-Shannon between two spreads, in bits: 0 identical, 1 disjoint."""
    middle = 0.5 * (first + second)
    def relative(part):
        mask = part > 0
        return float(np.sum(part[mask] * np.log2(part[mask] / middle[mask])))
    return 0.5 * relative(first) + 0.5 * relative(second)


def sounds(model_wav, other_wav, model_tag, other_tag, slug):
    """Every sound of the model's grid, and how far the other voice sits from it.

    The model is decoded freely -- these are the sounds it really produced --
    and the other recording is forced onto that same sequence, so a row of one
    faces the row of the other at the moment they mean the same thing.
    """
    model = matrix.probabilities(
        model_wav, cache=MATRICES / model_tag / f"{slug}.npz")
    other = matrix.probabilities(
        other_wav, cache=MATRICES / other_tag / f"{slug}.npz")

    segments = matrix.grid(model)
    ids = [index for index, _, _ in segments]
    spans = matrix.align(other, ids)

    read = []
    for (index, start, stop), span in zip(segments, spans):
        if span is None:
            continue
        here, empty_here = spread(model, (start, stop))
        there, empty_there = spread(other, span)
        if here is None or there is None:
            continue
        if empty_here > EMPTY_MASS or empty_there > EMPTY_MASS:
            continue
        read.append(Gap(divergence(here, there), matrix.symbols()[index],
                        start * matrix.seconds_per_frame()))
    return read


def readable(wav):
    return wav.is_file() and sf.info(wav).duration >= MIN_SECONDS


def column(values):
    return (f"{statistics.mean(values):>10.3f}"
            f"{statistics.median(values):>10.3f}"
            f"{max(values):>10.3f}{len(values):>8}")


def run(model_name, others, labels, chosen, which, show):
    print(f"\n=== modèle {model_name}   {len(chosen)} énoncés")
    print(f"    {'comparé à':<22}{'moyen':>10}{'médian':>10}"
          f"{'pire':>10}{'sons':>8}")

    for name, tag, source in [*others, *labels]:
        read = []
        for slug, _ in chosen:
            model_wav = RENDERS / model_name / which / f"{slug}.wav"
            other_wav = source / f"{slug}.wav"
            if not readable(model_wav) or not readable(other_wav):
                continue
            read.extend(sounds(model_wav, other_wav, f"{which}-{model_name}",
                               f"{which}-{tag}", slug))
        if not read:
            print(f"    {name:<22}  rien à lire")
            continue
        print(f"    {name:<22}{column([gap.value for gap in read])}")
        if show:
            for gap in sorted(read, reverse=True)[:5]:
                print(f"        {gap.value:>7.3f}  /{gap.symbol}/")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--gb", default="eleven-gb-daniel")
    parser.add_argument("--us", default="eleven-us-eric")
    parser.add_argument("-s", "--set", dest="which", default="sentences",
                        choices=sorted(phrases.SETS))
    parser.add_argument("-v", "--verbose", action="store_true")
    args = parser.parse_args(argv)
    chosen = phrases.SETS[args.which]

    for accent, name in (("gb", args.gb), ("us", args.us)):
        if name not in synth.BY_NAME:
            raise SystemExit(f"Voix inconnue {name!r}")
        # Other voices of the same claimed accent: the difference between them
        # and the model is a difference of voice and nothing else.
        others = [(f"voix {other.name}", other.name,
                   RENDERS / other.name / args.which)
                  for other in synth.CANDIDATES
                  if other.dialect == f"en-{accent}" and other.name != name]
        labels = [(f"calque {accent}", f"copy-{accent}",
                   TAKES / args.which / f"copy-{accent}"),
                  ("à froid", "spontaneous",
                   TAKES / args.which / "spontaneous")]
        run(name, others, labels, chosen, args.which, args.verbose)
    return 0


if __name__ == "__main__":
    sys.exit(main())
