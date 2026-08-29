#!/usr/bin/env python3
"""How often does a mark land on a word nobody wrote anything against?

The unknown that blocks the choice of acoustic model and the placing of the
screen threshold (`../TODO.md`). The home test set cannot answer it: it labels
one sound per take, so every other mark it lights has no judge. This reads a
corpus of real learners where every word carries a verdict (`learners.py`).

**The join is at the word, not at the phone.** The corpus indexes its scores on
a canonical phone sequence out of a lexicon; the chain reads the gap off the
grid of the synthesised model, and nothing pairs one sequence with the other.
The word is where they already meet: brick 4 partitions the sounds between the
words and is measured at 95% off the set that tuned it, so the join costs
nothing new and puts no unqualified instrument between us and the number. What
it gives up is *which* sound inside the word carried the mark -- a question
`faults.py` already answers on the home set.

**No threshold is applied and none is stored.** What is written per word is the
largest gap among its sounds, and nothing else. The screen threshold moves with
the prior weight and again between the workstation and the phone, so a rate
computed at 0.05 today would be unreadable the day it moves; the rate at any
threshold is one line away from the raw fact, and the raw fact survives. What
this prints below the facts is a derivation, marked as one.

    cd bench && python3 alarms.py --plan                 # what it would cost
    cd bench && python3 alarms.py --render               # spend the characters
    cd bench && python3 alarms.py                        # read what was spent

The renders are no more reproducible here than anywhere else in the bench, so
neither are these numbers: what comes out of `out/alarms/` was read off audio
that a second call would not reproduce.
"""

import argparse
import json
import statistics
import sys
from pathlib import Path

import faults
import join
import matrix
import learners
import overlap
import synth

HERE = Path(__file__).resolve().parent
RENDERS = HERE / "out" / "renders"
FACTS = HERE / "out" / "alarms"

VOICES = ("eleven-us-eric", "eleven-gb-daniel")

# Only a derivation of the facts, printed under them: where a threshold might
# be put, never where one is.
SHOWN_THRESHOLDS = (0.02, 0.05, 0.10, 0.20)


def render_path(voice, take):
    return RENDERS / voice / "l2" / f"{take.uid}.wav"


def missing(voice, takes):
    return [take for take in takes if not render_path(voice, take).is_file()]


def confirmed(question):
    """One prompt for the whole batch, defaulting to no. Ctrl+C is a no."""
    try:
        return input(f"{question} [o/N] ").strip().lower() in ("o", "oui")
    except (EOFError, KeyboardInterrupt):
        print()
        return False


def render(voices, takes, agreed=False):
    todo = {voice: missing(voice, takes) for voice in voices}
    cost = sum(len(take.text) for voice in voices for take in todo[voice])
    if not cost:
        print("  tout est déjà rendu")
        return 0
    for voice in voices:
        print(f"  {voice:<20}{len(todo[voice]):>4} rendus, "
              f"{sum(len(t.text) for t in todo[voice]):>6} caractères")
    print(f"  {'total':<20}{'':>4}        {cost:>6} caractères")
    if not agreed and not confirmed("Synthétiser ?"):
        print("  rien de dépensé")
        return 0
    for voice in voices:
        for index, take in enumerate(todo[voice], 1):
            synth.render(take.text, synth.BY_NAME[voice],
                         render_path(voice, take))
            print(f"  {voice} {index}/{len(todo[voice])}", file=sys.stderr,
                  flush=True)
    return cost


def read(voice, take):
    """Per word of one take: the largest gap among its sounds.

    Returns None when the take is out of comparison -- brick 11's case, where
    the grid has been forced onto speech that does not carry it and every sound
    is off at once, which is not a fault detected.
    """
    model, learner = render_path(voice, take), learners.wav(take)
    if not (overlap.readable(model) and overlap.readable(learner)):
        return None
    tag = f"l2-{voice}"
    gaps = overlap.sounds(model, learner, tag, f"l2-{take.uid}", take.uid)
    if not gaps:
        return None
    if statistics.median(gap.value for gap in gaps) > faults.BROKEN_MEDIAN:
        return None

    sounds = join.joined(model, take.text,
                         cache=overlap.MATRICES / tag / f"{take.uid}.npz")
    spoken = join.spoken(take.text)
    # The corpus lists one entry per written word; the join splits the same
    # text the same way. A disagreement means the two are not talking about the
    # same words at all, and no row of it is worth keeping.
    if len(spoken) != len(take.words):
        return None

    order = [word for word, _ in spoken]
    spread = {}
    for gap in gaps:
        spread.setdefault(sounds[gap.rank].word, []).append(gap.value)
    # Every sound of the word, not only the largest: the maximum says a mark
    # would fall, and cannot say whether one sound came loose or the whole word
    # sits apart -- which is the difference between a fault and a voice that
    # never matches this model anywhere.
    return [{"word": written.text, "state": written.state,
             "stress": written.stress,
             "gap": max(spread[name]) if name in spread else None,
             "sounds": [round(value, 4) for value in spread.get(name, ())]}
            for name, written in zip(order, take.words)]


def measure(voice, takes):
    rows, refused = [], 0
    for index, take in enumerate(takes, 1):
        found = read(voice, take)
        if found is None:
            refused += 1
            continue
        for row in found:
            rows.append(dict(row, take=take.uid, speaker=take.speaker))
        if index % 20 == 0:
            print(f"  {voice} {index}/{len(takes)}", file=sys.stderr,
                  flush=True)
    return rows, refused


def quantile(values, q):
    return sorted(values)[int(q * (len(values) - 1))]


def localised(seen, threshold=0.05):
    """Of a marked word's sounds, how many are marked too.

    A word is one number above -- the largest gap among its sounds -- and that
    number cannot tell a sound that came loose from a word that sits apart from
    the model everywhere. This reads the same rows again and asks how much of
    the word the mark covers. A threshold has to be named to ask the question
    at all; it is this reading's own, and nothing else in the bench is held to
    it.

    Words of a single sound are left out: the share is 1 by construction.
    """
    print(f"\n    marque localisée ou mot entier ? (mots au-dessus de "
          f"{threshold}, 2 sons ou plus)")
    print(f"    {'verdict':<10}{'mots':>6}{'sons/mot':>10}"
          f"{'part du mot marquée':>22}{'mot entier':>13}")
    for label in learners.LABELS:
        chosen = [row for row in seen
                  if row["state"] == label and row["gap"] > threshold
                  and len(row["sounds"]) >= 2]
        if not chosen:
            continue
        shares = [sum(1 for v in row["sounds"] if v > threshold)
                  / len(row["sounds"]) for row in chosen]
        whole = sum(1 for share in shares if share == 1.0) / len(shares)
        print(f"    {label:<10}{len(chosen):>6}"
              f"{statistics.median(len(r['sounds']) for r in chosen):>10.1f}"
              f"{100 * statistics.median(shares):>21.1f}%"
              f"{100 * whole:>12.1f}%")


def report(voice, rows, refused):
    seen = [row for row in rows if row["gap"] is not None]
    silent = len(rows) - len(seen)
    print(f"\n=== {voice} — {len(rows)} mots lus, {refused} prises hors "
          f"comparaison, {silent} mots sans son comparable")
    print(f"    {'verdict':<10}{'mots':>6}{'médiane':>10}{'9e déc':>9}"
          f"{'max':>9}")
    for label in learners.LABELS:
        values = [row["gap"] for row in seen if row["state"] == label]
        if not values:
            continue
        print(f"    {label:<10}{len(values):>6}"
              f"{statistics.median(values):>10.3f}"
              f"{quantile(values, 0.9):>9.3f}{max(values):>9.3f}")

    localised(seen)

    print(f"\n    dérivé des chiffres ci-dessus, aucun seuil n'est posé :")
    print(f"    {'seuil':<10}" + "".join(f"{label:>10}"
                                         for label in learners.LABELS))
    for threshold in SHOWN_THRESHOLDS:
        marked = []
        for label in learners.LABELS:
            values = [row["gap"] for row in seen if row["state"] == label]
            share = (100 * sum(1 for v in values if v > threshold) / len(values)
                     if values else 0.0)
            marked.append(f"{share:>9.1f}%")
        print(f"    {threshold:<10.2f}" + "".join(marked))


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-b", "--budget", type=int, default=2500,
                        help="caractères de synthèse par voix ; 0 = tout le jeu")
    parser.add_argument("-c", "--candidate", action="append", default=None,
                        help="repeatable; défaut : les deux voix de faults.py")
    parser.add_argument("-s", "--split", default="test")
    parser.add_argument("--seed", type=int, default=0)
    parser.add_argument("-p", "--plan", action="store_true",
                        help="ce que ça coûterait, sans rien toucher")
    parser.add_argument("-r", "--render", action="store_true",
                        help="synthétiser les modèles manquants")
    parser.add_argument("-y", "--yes", action="store_true",
                        help="la dépense est déjà accordée, ne pas redemander")
    args = parser.parse_args(argv)

    voices = args.candidate or list(VOICES)
    for voice in voices:
        if voice not in synth.BY_NAME:
            raise SystemExit(f"Voix inconnue {voice!r}")

    takes, spent = learners.chosen(args.budget, args.split, args.seed)
    counts, wrong_stress = learners.census(takes)
    print(f"\n=== {len(takes)} prises tirées, {spent} caractères par voix, "
          f"{len({t.speaker for t in takes})} locuteurs")
    print(f"    {sum(counts.values())} mots : "
          + ", ".join(f"{counts[label]} {label}" for label in learners.LABELS)
          + f", dont {wrong_stress} à l'accent noté faux")
    if args.plan:
        for voice in voices:
            absent = missing(voice, takes)
            print(f"    {voice:<20}{len(absent):>4} à rendre, "
                  f"{sum(len(t.text) for t in absent):>6} caractères")
        return 0

    if args.render:
        render(voices, takes, args.yes)
        return 0

    absent = {voice: missing(voice, takes) for voice in voices}
    empty = [voice for voice, rows in absent.items() if rows]
    if empty:
        raise SystemExit(
            "modèles manquants pour " + ", ".join(empty)
            + " — `python3 alarms.py --render` les synthétise, et c'est là que "
              "les caractères se dépensent")

    written = learners.extract(takes, args.split)
    if written:
        print(f"    {written} audios d'apprenants extraits")

    FACTS.mkdir(parents=True, exist_ok=True)
    for voice in voices:
        rows, refused = measure(voice, takes)
        if not rows:
            print(f"\n=== {voice} — rien de lisible")
            continue
        path = FACTS / f"{args.split}-{voice}.json"
        path.write_text(json.dumps(rows, indent=2, ensure_ascii=False),
                        encoding="utf-8")
        report(voice, rows, refused)
        print(f"\n    les faits : {path}")
    print(f"\n{matrix.audios()}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
