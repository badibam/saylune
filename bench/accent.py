#!/usr/bin/env python3
"""Brick 7 tried against a corpus that scores stress -- two readings of it.

The corpus scores the stress of every word (`learners.py`), so a disagreement
between the two sides is a false alarm on a word it calls correct and a catch
on a word it calls wrong. Nothing else in the bench can tell those apart.

Two montages, because they are not the same question:

    -m aligné   the montage in place: the model is decoded freely and the
                learner forced onto that same sequence. The learner has spans
                but no symbols, so the reduction cue reads one set of labels
                twice and cannot, by construction, detect anything. Only
                duration and loudness can differ.

    -m libre    each side decoded freely and joined to the words on its own.
                The reduction cue becomes usable -- at the price of running
                brick 4 on learner speech, which has never been measured
                anywhere.

Two verdicts per montage, because reducing a side to one winning syllable is
itself in question:

    l'élue      reduction where it decides, loudness elsewhere: each side names
                a syllable, and a mark is the two names differing.
    le profil   nothing is named. Each side gives the word two shapes over its
                syllables -- relative durations, relative loudness of the
                nuclei -- and the two sides are compared shape to shape by the
                divergence brick 3 uses on phonemes.

    cd bench && python3 accent.py -b 6000            # sur ce qui est en cache
    cd bench && python3 accent.py -b 0 -m libre      # tout le jeu

No threshold is applied or stored: what comes out is the divergence per word
beside the corpus's own verdict, and any rate is derived from that afterwards.
"""

import argparse
import json
import statistics
import sys
from pathlib import Path

import numpy as np
import soundfile as sf

import join
import learners
import matrix
import overlap
import syllables

HERE = Path(__file__).resolve().parent
RENDERS = HERE / "out" / "renders"
FACTS = HERE / "out" / "accent"

VOICE = "azure-us-jenny"

# The reduced vowels the vocabulary in service can write. `ɪ` is not among
# them: it spells the reduced vowel of `roses` and the full one of `sink`, and
# the alphabet does not tell them apart.
REDUCED = ("ə", "ɚ")


def rms(samples, rate, low, high):
    first = int(low * rate)
    seg = samples[first:max(int(high * rate), first + 1)]
    return float(np.sqrt(np.mean(seg ** 2))) if len(seg) else 0.0


def measured(wav, text, cache):
    """Per word: its syllables' symbols, durations and loudness, from one side."""
    sounds = join.joined(wav, text, cache=cache)
    cuts = syllables.cut(sounds)
    matrix.heard(wav)
    samples, rate = sf.read(wav, dtype="float64")
    out = {}
    for start, stop in join.runs(sounds):
        mine = [piece for piece in cuts
                if start <= piece.sounds[0] and piece.sounds[1] <= stop]
        if not mine:
            continue
        symbols, spans, power = [], [], []
        for piece in mine:
            nucleus = next((sounds[i] for i in range(*piece.sounds)
                            if sounds[i].symbol in matrix.VOWELS), None)
            symbols.append(nucleus.symbol if nucleus else "")
            spans.append(piece.high - piece.low)
            power.append(rms(samples, rate, nucleus.low, nucleus.high)
                         if nucleus else 0.0)
        out[start] = (sounds[start].word, symbols, spans, power)
    return [out[key] for key in sorted(out)]


def elected(symbols, spans, power):
    """The syllable this side calls strong: reduction if it decides, else loud."""
    full = [i for i, symbol in enumerate(symbols) if symbol not in REDUCED]
    if len(full) == 1:
        return full[0], "réduction"
    pool = full or list(range(len(symbols)))
    return max(pool, key=lambda i: power[i]), "intensité"


def shape(values):
    total = sum(values)
    return np.array([v / total for v in values]) if total > 0 else None


def compared(left, right, written, word):
    """One word, both verdicts -- or the syllable counts differing, which is
    not a case to drop: a syllable added or swallowed is itself the signal."""
    _, ms, md, mp = left
    _, ts, td, tp = right
    row = {"word": written.text, "stress": written.stress,
           "syllabes": [len(ms), len(ts)]}
    if len(ms) != len(ts):
        row["branche"] = "compte différent"
        return row
    row["branche"] = "même compte"
    here, why_here = elected(ms, md, mp)
    there, why_there = elected(ts, td, tp)
    row.update({"élue": [here, there], "tranché": [why_here, why_there],
                "élue diverge": here != there})
    for name, first, second in (("durée", md, td), ("intensité", mp, tp)):
        a, b = shape(first), shape(second)
        row[f"profil {name}"] = (None if a is None or b is None
                                 else overlap.divergence(a, b))
    return row


def read(take, voice, montage):
    model = RENDERS / voice / "l2" / f"{take.uid}.wav"
    learner = learners.wav(take)
    if not (overlap.readable(model) and overlap.readable(learner)):
        return []
    model_cache = overlap.MATRICES / f"l2-{voice}" / f"{take.uid}.npz"
    take_cache = overlap.MATRICES / f"l2-{take.uid}" / f"{take.uid}.npz"
    left = measured(model, take.text, model_cache)
    if montage == "libre":
        right = measured(learner, take.text, take_cache)
    else:
        # The learner wears the model's symbols, which is what the montage in
        # place does; only its spans and its loudness are its own.
        gaps = overlap.sounds(model, learner, f"l2-{voice}",
                              f"l2-{take.uid}", take.uid)
        if not gaps:
            return []
        right = borrowed(model, learner, take.text, model_cache, gaps)

    sides = {}
    for entry in left:
        sides.setdefault(entry[0].strip(".,!?'").lower(), {})["m"] = entry
    for entry in right:
        sides.setdefault(entry[0].strip(".,!?'").lower(), {})["t"] = entry

    rows = []
    for written in take.words:
        key = written.text.lower()
        pair = sides.get(key)
        if not pair or len(pair) != 2 or key in syllables.FUNCTION:
            continue
        if len(pair["m"][1]) < 2 and len(pair["t"][1]) < 2:
            continue
        rows.append(dict(compared(pair["m"], pair["t"], written, key),
                         take=take.uid, speaker=take.speaker))
    return rows


def borrowed(model, learner, text, cache, gaps):
    """The learner read through the model's own grid: the aligned montage.

    Each syllable of the model keeps its symbols; what the learner supplies is
    where it sat and how loud it was there.
    """
    sounds = join.joined(model, text, cache=cache)
    cuts = syllables.cut(sounds)
    step = matrix.seconds_per_frame()
    span_of = {gap.rank: gap.span for gap in gaps}
    matrix.heard(learner)
    samples, rate = sf.read(learner, dtype="float64")
    out = {}
    for start, stop in join.runs(sounds):
        mine = [piece for piece in cuts
                if start <= piece.sounds[0] and piece.sounds[1] <= stop]
        if not mine:
            continue
        symbols, spans, power = [], [], []
        for piece in mine:
            nucleus = next((i for i in range(*piece.sounds)
                            if sounds[i].symbol in matrix.VOWELS), None)
            if nucleus is None or nucleus not in span_of:
                symbols, spans, power = [], [], []
                break
            low, high = (span_of[nucleus][0] * step, span_of[nucleus][1] * step)
            symbols.append(sounds[nucleus].symbol)
            spans.append(high - low)
            power.append(rms(samples, rate, low, high))
        if symbols:
            out[start] = (sounds[start].word, symbols, spans, power)
    return [out[key] for key in sorted(out)]


def quantile(values, q):
    return sorted(values)[int(q * (len(values) - 1))]


def report(rows, montage):
    same = [row for row in rows if row["branche"] == "même compte"]
    apart = [row for row in rows if row["branche"] == "compte différent"]
    print(f"\n=== montage {montage} — {len(rows)} mots pleins")
    if apart:
        wrong = sum(1 for row in apart if row["stress"] == 5)
        print(f"    compte de syllabes différent : {len(apart)} mots, dont "
              f"{wrong} à l'accent noté faux "
              f"({100 * (len(apart) - wrong) / len(apart):.1f} % de fausse "
              f"alerte si cette branche marque)")
    if not same:
        return
    clean = [row for row in same if row["stress"] == 10]
    faulty = [row for row in same if row["stress"] == 5]
    print(f"    même compte : {len(same)} mots, {len(clean)} à l'accent "
          f"correct, {len(faulty)} faux")

    lit = sum(1 for row in clean if row["élue diverge"])
    caught = sum(1 for row in faulty if row["élue diverge"])
    print(f"\n    l'élue : {100 * lit / len(clean):.1f} % de fausse alerte"
          + (f", {caught}/{len(faulty)} fautes vues" if faulty else ""))
    for why in ("réduction", "intensité"):
        n = sum(1 for row in same if row["tranché"][0] == why)
        print(f"      côté modèle, tranché par {why:<11}"
              f"{100 * n / len(same):5.1f} %")

    for channel in ("durée", "intensité"):
        key = f"profil {channel}"
        ok = [row[key] for row in clean if row.get(key) is not None]
        bad = [row[key] for row in faulty if row.get(key) is not None]
        if not ok:
            continue
        print(f"\n    profil de {channel} — accent correct : médiane "
              f"{statistics.median(ok):.3f}, 9e décile {quantile(ok, 0.9):.3f}"
              f", max {max(ok):.3f}")
        if bad:
            print(f"      accent faux ({len(bad)}) : "
                  + ", ".join(f"{v:.3f}" for v in sorted(bad, reverse=True)))


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-b", "--budget", type=int, default=6000,
                        help="caractères de modèle du tirage ; 0 = tout le jeu")
    parser.add_argument("-c", "--candidate", default=VOICE)
    parser.add_argument("-m", "--montage", default="libre",
                        choices=("aligné", "libre"))
    parser.add_argument("-s", "--split", default="test")
    parser.add_argument("--seed", type=int, default=0)
    args = parser.parse_args(argv)

    takes, _ = learners.chosen(args.budget, args.split, args.seed)
    learners.extract(takes, args.split)
    rows, skipped = [], 0
    for index, take in enumerate(takes, 1):
        found = read(take, args.candidate, args.montage)
        if not found:
            skipped += 1
        rows += found
        if index % 50 == 0:
            print(f"  {index}/{len(takes)}", file=sys.stderr, flush=True)
    if not rows:
        raise SystemExit("aucun mot lisible — les rendus manquent-ils ?")

    FACTS.mkdir(parents=True, exist_ok=True)
    path = FACTS / f"{args.split}-{args.candidate}-{args.montage}.json"
    path.write_text(json.dumps(rows, indent=2, ensure_ascii=False),
                    encoding="utf-8")
    report(rows, args.montage)
    print(f"\n    {skipped} prises sans rien de lisible")
    print(f"    les faits : {path}")
    print(f"\n{matrix.audios()}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
