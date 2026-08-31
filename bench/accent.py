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

    -m concurrent   competing hypotheses (`../TODO.md`, mesure 4). Neither side
                is decoded freely and neither side is forced onto one sequence:
                for a word of n nuclei, n stress patterns are built out of the
                model's own sounds -- one nucleus full, the others reduced, in
                classes -- and each is scored on each side by the same trellis
                the alignment walks. The reduction gets its labels back, which
                the aligned montage takes away, without ever asking the learner
                what he said.

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

# How `attached` folds several learner syllables into one of the model's.
MERGE = "somme"


def rms(samples, rate, low, high):
    first = int(low * rate)
    seg = samples[first:max(int(high * rate), first + 1)]
    return float(np.sqrt(np.mean(seg ** 2))) if len(seg) else 0.0


def reduced_mass(spread, columns, low, high, step):
    """How much of a nucleus the network spends on a reduced vowel.

    A **class** of sounds and not one phone, read as mass rather than as a
    label: under the aligned montage the learner wears the model's symbols, so
    asking "is this nucleus a schwa" reads the same answer twice. The frames
    underneath are still the learner's own, and what they put on the reduced
    vowels is his (`../TODO.md`, mesure 2). Read the same way on both sides so
    that nothing distinguishes them but the voice.
    """
    first = max(int(low / step), 0)
    last = min(int(high / step) + 1, len(spread))
    if last <= first:
        return 0.0
    return float(spread[first:last, columns].sum(axis=1).mean())


def measured(wav, text, cache):
    """Per word: its syllables' nuclei, and everything read off one of them.

    `spots` is what lets two sides be put in front of each other when they do
    not have the same number of syllables: a syllable knows the characters of
    the text it covers, on either side, so an inserted syllable falls inside
    the letters of a model syllable instead of shifting every count after it.
    """
    sounds = join.joined(wav, text, cache=cache)
    cuts = syllables.cut(sounds)
    matrix.heard(wav)
    samples, rate = sf.read(wav, dtype="float64")
    spread = matrix.probabilities(wav, cache=cache)
    table = matrix.symbols()
    columns = [i for i, symbol in enumerate(table) if symbol in REDUCED]
    step = matrix.seconds_per_frame()
    out = {}
    for start, stop in join.runs(sounds):
        mine = [piece for piece in cuts
                if start <= piece.sounds[0] and piece.sounds[1] <= stop]
        if not mine:
            continue
        symbols, spans, power, spots, reduction = [], [], [], [], []
        for piece in mine:
            low, high = piece.sounds
            found = matrix.nuclei([s.symbol for s in sounds[low:high]])
            nucleus = sounds[low + found[0]] if found else None
            symbols.append(nucleus.symbol if nucleus else "")
            spans.append(piece.high - piece.low)
            power.append(rms(samples, rate, nucleus.low, nucleus.high)
                         if nucleus else 0.0)
            spots.append(frozenset(piece.spots))
            reduction.append(
                reduced_mass(spread, columns, nucleus.low, nucleus.high, step)
                if nucleus and columns else 0.0)
        out[start] = (sounds[start].word, symbols, spans, power,
                      spots, reduction)
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


def attached(model_spots, learner_spots, learner_values):
    """Every learner syllable handed back to a model syllable, by their letters.

    **The model counts the syllables, and the learner is read against that
    count.** Dropping a word whose two sides disagree is what the montage did
    before, and it is silence on a quarter of the words that can carry a stress
    -- which the design refuses elsewhere. What replaces it is not a repaired
    count but a mapping: the model says how many syllables the word has, and
    each learner syllable joins the model syllable it shares the most letters
    with. A syllable he inserted lands inside the one it was inserted into,
    rather than shifting every syllable after it by one.

    The letters are what makes this possible and they are new: both readings
    are joined to the text now, the learner's at 94 % against the model's 95 %
    (`../docs/analysis.md`, brique 12). Nothing here consults a clock.

    Surjective by construction on the model's side: a model syllable with no
    learner syllable is one he swallowed, and it keeps its place with nothing
    in it -- which is itself what a swallowed syllable looks like.
    """
    held = [[] for _ in model_spots]
    for spots, value in zip(learner_spots, learner_values):
        overlaps = [len(spots & theirs) for theirs in model_spots]
        best = max(range(len(model_spots)), key=lambda i: overlaps[i])
        if overlaps[best]:
            held[best].append(value)
    # A syllable that took several keeps their total by default, not their
    # average: two nuclei said where the model said one is more of whatever
    # they carry, and averaging would hide the insertion the mapping exists to
    # see. But an inserted vowel is a schwa, so summing also inflates the very
    # channel that reads schwa -- which is a reason to measure both rather than
    # to pick one. `--moyenne` is the other reading.
    if MERGE == "moyenne":
        return [sum(group) / len(group) if group else 0.0 for group in held]
    return [sum(group) if group else 0.0 for group in held]


def compared(left, right, written, word):
    """One word, both verdicts -- or the syllable counts differing, which is
    not a case to drop: a syllable added or swallowed is itself the signal."""
    _, ms, md, mp, mspots, mred = left
    _, ts, td, tp, tspots, tred = right
    row = {"word": written.text, "stress": written.stress,
           "syllabes": [len(ms), len(ts)]}

    # The reduction, read on the model's own count of syllables whatever the
    # learner's is. This is the channel the stress measures rank first when it
    # speaks, and the only one of the three never read as a profile.
    theirs = attached(mspots, tspots, tred)
    a, b = shape(mred), shape(theirs)
    row["profil réduction"] = (None if a is None or b is None
                               else overlap.divergence(a, b))
    row["réduction muette"] = a is None or b is None

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
    if montage == "concurrent":
        words = {entry[0].strip(".,!?'").lower(): entry
                 for entry in contest(take, voice)}
        return [weighed(words[written.text.lower()], written, take)
                for written in take.words
                if written.text.lower() in words
                and written.text.lower() not in syllables.FUNCTION]

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
        # The model decides how many syllables the word has, so it decides
        # whether the word can carry a stress at all. A word it gives one
        # syllable never carries an accent mark (brique 9), and counting those
        # was inflating every disagreement rate written down until now.
        if len(pair["m"][1]) < 2:
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
            low, high = piece.sounds
            found = matrix.nuclei([s.symbol for s in sounds[low:high]])
            nucleus = low + found[0] if found else None
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


def classes():
    """The two vowel classes a hypothesis is written in.

    Classes and not phones: a hypothesis says "a reduced vowel sits here", never
    which one. Naming the phone would ask the network for the label it is least
    good at (`../docs/analysis.md`), and the stress pattern does not need it.
    """
    table = matrix.symbols()
    full = [i for i, symbol in enumerate(table)
            if symbol in matrix.VOWELS and symbol not in REDUCED]
    reduced = [i for i, symbol in enumerate(table) if symbol in REDUCED]
    return full, reduced


def cost(spread, slots, empty):
    """The best path through one hypothesis, as a log-probability.

    The trellis `matrix.align` walks, with the one difference that is the whole
    point: a slot may stand for a **class** of sounds, whose mass is summed
    before the log. Only the score comes back -- where each sound sat is not
    asked, since the hypotheses are compared to each other and not to a clock.
    """
    width = 2 * len(slots) + 1
    if len(spread) < width:
        return None
    blank = np.log(np.maximum(spread[:, empty], 1e-12))
    rows, keys = [blank], [None]
    for columns in slots:
        rows += [np.log(np.maximum(spread[:, columns].sum(axis=1), 1e-12)), blank]
        keys += [tuple(columns), None]

    score = np.full((len(spread), width), -np.inf)
    score[0, 0], score[0, 1] = rows[0][0], rows[1][0]
    # A hypothesis may skip the blank between two slots, unless they stand for
    # the same class -- two identical classes in a row need the blank to be
    # told apart, exactly as two identical phones do.
    skippable = [step > 1 and keys[step] is not None
                 and keys[step] != keys[step - 2] for step in range(width)]
    for frame in range(1, len(spread)):
        before = score[frame - 1]
        best = np.maximum(before, np.concatenate(([-np.inf], before[:-1])))
        twice = np.concatenate(([-np.inf, -np.inf], before[:-2]))
        best = np.where(skippable, np.maximum(best, twice), best)
        score[frame] = best + np.array([row[frame] for row in rows])
    return float(max(score[-1, width - 1], score[-1, width - 2]))


def posterior(scores, frames):
    """The n costs of a word, as a share over the stress positions.

    Normalised by the frames they were read on, without which a long word
    saturates the share to a single position and the montage falls back to
    comparing two winners -- the very form the ceiling condemned.
    """
    values = np.array(scores) / max(frames, 1)
    weights = np.exp(values - values.max())
    return weights / weights.sum()


def contest(take, voice):
    """One take, every word confronted with the n hypotheses of its own model.

    The model's sounds and the model's syllable count decide what the
    hypotheses are; both matrices are then read against the same n. Nothing is
    forced onto a sequence and nothing is decoded freely.
    """
    model = RENDERS / voice / "l2" / f"{take.uid}.wav"
    learner = learners.wav(take)
    if not (overlap.readable(model) and overlap.readable(learner)):
        return []
    model_cache = overlap.MATRICES / f"l2-{voice}" / f"{take.uid}.npz"
    take_cache = overlap.MATRICES / f"l2-{take.uid}" / f"{take.uid}.npz"

    theirs = matrix.probabilities(model, cache=model_cache)
    segments = matrix.grid(theirs)
    if not segments:
        return []
    mine = matrix.probabilities(learner, cache=take_cache)
    spans = matrix.align(mine, [index for index, _, _ in segments])
    step = matrix.seconds_per_frame()
    matrix.heard(model)
    model_samples, model_rate = sf.read(model, dtype="float64")
    matrix.heard(learner)
    learner_samples, learner_rate = sf.read(learner, dtype="float64")
    sounds = join.joined(model, take.text, cache=model_cache)
    cuts = syllables.cut(sounds)
    full, reduced = classes()
    empty = matrix.blank()

    found = {}
    for start, stop in join.runs(sounds):
        pieces = [piece for piece in cuts
                  if start <= piece.sounds[0] and piece.sounds[1] <= stop]
        if len(pieces) < 2:
            continue
        at = []
        for piece in pieces:
            low, high = piece.sounds
            heads = matrix.nuclei([s.symbol for s in sounds[low:high]])
            if not heads:
                at = []
                break
            at.append(low + heads[0] - start)
        if not at:
            continue

        base = [[segments[index][0]] for index in range(start, stop)]
        variants = []
        for chosen in range(len(at)):
            slots = [list(one) for one in base]
            for rank, nucleus in enumerate(at):
                slots[nucleus] = full if rank == chosen else reduced
            variants.append(slots)

        here = (segments[start][1], segments[stop - 1][2])
        drawn = [spans[index] for index in range(start, stop)
                 if spans[index] is not None]
        if not drawn:
            continue
        there = (drawn[0][0], drawn[-1][1])
        left = [cost(theirs[here[0]:here[1]], one, empty) for one in variants]
        right = [cost(mine[there[0]:there[1]], one, empty) for one in variants]

        # What a hypothesis predicts besides the sounds: that its nucleus is
        # the loud one and the long one. Read on each side with its own
        # recording, never across the two.
        model_nuclei = [segments[start + one] for one in at]
        learner_nuclei = [spans[start + one] for one in at]
        if any(one is None for one in learner_nuclei):
            continue
        loud_left = [rms(model_samples, model_rate, low * step, high * step)
                     for _, low, high in model_nuclei]
        loud_right = [rms(learner_samples, learner_rate, low * step, high * step)
                      for low, high in learner_nuclei]
        long_left = [(high - low) * step for _, low, high in model_nuclei]
        long_right = [(high - low) * step for low, high in learner_nuclei]
        found[start] = (sounds[start].word, left, right,
                        here[1] - here[0], there[1] - there[0],
                        loud_left, loud_right, long_left, long_right)
    return [found[key] for key in sorted(found)]


def weighed(word, written, take):
    """One word's two sets of costs, read three ways.

    The three differ in what they take as the reference, and the difference is
    the open question. `marge` is the statistic of the family (a GOP-like
    likelihood margin): how much more the learner's own matrix wants some other
    pattern than the one the model settles on. It is read on his matrix alone,
    so what the network thinks of this word in general does not cancel -- it
    enters only through the reference the model designates.

    `marge symétrique` adds the same quantity with the sides swapped, so a word
    the network is simply unsure about costs the same on both sides and lands
    near zero. `divergence` is the house form -- two shares compared whole, by
    brick 3's tool -- and it carries a temperature the other two do not.
    """
    (_, left, right, model_frames, learner_frames,
     loud_left, loud_right, long_left, long_right) = word
    row = {"word": written.text, "stress": written.stress,
           "syllabes": len(left), "take": take.uid, "speaker": take.speaker,
           "acoustique": [left, right], "trames": [model_frames, learner_frames],
           "intensité": [loud_left, loud_right],
           "durée": [long_left, long_right]}
    if any(value is None for value in left + right):
        row["muet"] = True
        return row
    row["muet"] = False
    theirs, mine = np.array(left), np.array(right)
    his, its = int(mine.argmax()), int(theirs.argmax())
    row["élue"] = [its, his]
    row["élue diverge"] = his != its
    row["marge"] = float(mine.max() - mine[its]) / max(learner_frames, 1)
    row["marge symétrique"] = row["marge"] + float(
        theirs.max() - theirs[his]) / max(model_frames, 1)
    row["divergence"] = overlap.divergence(posterior(left, model_frames),
                                           posterior(right, learner_frames))
    return row


def share(values):
    """One dimension of a word, as the log of each nucleus's share of it.

    A hypothesis says its nucleus is the loud one and the long one, and this is
    what that prediction is worth in the same unit as the path: the log of the
    share, so a nucleus holding all of it costs nothing and one holding none
    costs everything. No scale is fitted -- what a share is worth against the
    sounds is the weight the sweep tries, and it is never chosen here.
    """
    total = sum(values)
    if total <= 0:
        return None
    return [float(np.log(max(value / total, 1e-6))) for value in values]


def combined(row, loud, long_):
    """The n costs of a word once the other two dimensions have their say.

    Every term is read on one side with that side's own recording; the two
    sides meet only in the statistics below, never inside a score.
    """
    out = []
    for side in (0, 1):
        scores = np.array(row["acoustique"][side], dtype=float)
        for weight, name in ((loud, "intensité"), (long_, "durée")):
            if not weight:
                continue
            parts = share(row[name][side])
            if parts is None or len(parts) != len(scores):
                return None
            scores = scores + weight * np.array(parts)
        out.append(scores)
    return out


def statistics_of(row, loud, long_):
    """The three readings of one word, at one pair of weights."""
    pair = combined(row, loud, long_)
    if pair is None:
        return None
    theirs, mine = pair
    model_frames, learner_frames = row["trames"]
    his, its = int(mine.argmax()), int(theirs.argmax())
    marge = float(mine.max() - mine[its]) / max(learner_frames, 1)
    return {
        "élue diverge": his != its,
        "marge": marge,
        "marge symétrique": marge + float(
            theirs.max() - theirs[his]) / max(model_frames, 1),
        "divergence": overlap.divergence(posterior(theirs, model_frames),
                                         posterior(mine, learner_frames)),
    }


def quantile(values, q):
    return sorted(values)[int(q * (len(values) - 1))]


def caught(rows, key):
    """What a channel sees, at a false alarm rate rather than at a threshold.

    A threshold read off the correctly stressed words themselves, so the two
    channels are comparable and no number is chosen. Nothing is stored: the
    brick decides no threshold, it says what one would cost.
    """
    ok = sorted(r[key] for r in rows if r["stress"] == 10 and r.get(key) is not None)
    bad = [r[key] for r in rows if r["stress"] == 5 and r.get(key) is not None]
    if not ok or not bad:
        return None
    out = []
    for rate in (0.05, 0.10, 0.20):
        bar = ok[int((1 - rate) * (len(ok) - 1))]
        seen = sum(1 for value in bad if value > bar)
        out.append(f"{seen:>3}/{len(bad)} à {rate * 100:.0f} %")
    return f"{len(ok)} propres, {len(bad)} fautes — " + "   ".join(out)


def rival(rows):
    """What the competing hypotheses see, swept over the weight of each cue.

    The weight is not chosen here and no number below is a threshold: the sweep
    says what each combination would cost, and the brick decides nothing. A
    weight of 0 on both is the acoustic path alone, which is the montage as it
    was first measured -- so the first row of the sweep is the number to beat.
    """
    read = [row for row in rows if not row["muet"]]
    print(f"\n=== montage concurrent — {len(rows)} mots pleins, "
          f"{len(rows) - len(read)} muets (trop peu de trames)")
    if not read:
        return
    clean = sum(1 for row in read if row["stress"] == 10)
    faulty = sum(1 for row in read if row["stress"] == 5)
    print(f"    {clean} à l'accent correct, {faulty} faux")
    print("\n    poids : ce que vaut une part d'intensité (i) ou de durée (d) "
          "contre le chemin acoustique")
    print(f"\n      {'i':>6} {'d':>6}  {'élues ≠':>9}"
          f"  {'marge':>16}  {'marge sym.':>16}  {'divergence':>16}")
    for loud, long_ in ((0, 0), (5, 0), (20, 0), (50, 0), (100, 0), (300, 0),
                        (1000, 0), (0, 20), (0, 100), (20, 20), (100, 100),
                        (300, 100)):
        held = []
        for row in read:
            found = statistics_of(row, loud, long_)
            if found is not None:
                held.append(dict(row, **found))
        if not held:
            continue
        ok = [r for r in held if r["stress"] == 10]
        lit = (100 * sum(1 for r in ok if r["élue diverge"]) / len(ok)
               if ok else float("nan"))
        cells = []
        for channel in ("marge", "marge symétrique", "divergence"):
            seen = seen_at(held, channel, 0.05)
            cells.append("      —" if seen is None else f"{seen}")
        print(f"      {loud:>6} {long_:>6}  {lit:>8.1f} %"
              + "".join(f"  {cell:>16}" for cell in cells))
    print("\n      colonnes : fausse alerte des deux élues, puis fautes vues "
          "à 5 % de fausse alerte")


def seen_at(rows, key, rate):
    """Faults caught when the bar is set on the correct words at `rate`."""
    ok = sorted(r[key] for r in rows if r["stress"] == 10 and r.get(key) is not None)
    bad = [r[key] for r in rows if r["stress"] == 5 and r.get(key) is not None]
    if not ok or not bad:
        return None
    bar = ok[int((1 - rate) * (len(ok) - 1))]
    return f"{sum(1 for value in bad if value > bar)}/{len(bad)}"


def report(rows, montage):
    if montage == "concurrent":
        return rival(rows)
    same = [row for row in rows if row["branche"] == "même compte"]
    apart = [row for row in rows if row["branche"] == "compte différent"]
    print(f"\n=== montage {montage} — {len(rows)} mots pleins")

    # First, and on every word: the reduction is read on the model's count of
    # syllables, so a word whose counts differ is measured like any other.
    mute = sum(1 for row in rows if row.get("réduction muette"))
    line = caught(rows, "profil réduction")
    print(f"\n    profil de réduction — sur les {len(rows)} mots, "
          f"{mute} muets (aucune réduction d'un côté)")
    if line:
        print(f"      {line}")
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
    hits = sum(1 for row in faulty if row["élue diverge"])
    print(f"\n    l'élue : {100 * lit / len(clean):.1f} % de fausse alerte"
          + (f", {hits}/{len(faulty)} fautes vues" if faulty else ""))
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
                        choices=("aligné", "libre", "concurrent"))
    parser.add_argument("-s", "--split", default="test")
    parser.add_argument("-p", "--probe", default="probe-l2-19",
                        help="la sonde figée à lire, dans out/probe/")
    parser.add_argument("--seed", type=int, default=0)
    parser.add_argument("--moyenne", action="store_true",
                        help="moyenner les syllabes rattachées au lieu de les sommer")
    args = parser.parse_args(argv)
    global MERGE
    MERGE = "moyenne" if args.moyenne else "somme"

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
