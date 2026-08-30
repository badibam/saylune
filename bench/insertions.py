#!/usr/bin/env python3
"""Does the learner add sounds the model never made, and can a letter carry them?

The analysis decodes the grid from the **model alone** and forces the learner
onto it, so there are exactly as many slots as the model has sounds. A sound the
learner adds is therefore not badly scored -- it is not seen at all. Its only
trace is the time it steals inside a neighbour's span, which inflates that
neighbour: the mark lands on the wrong letter.

The way in costs nothing new. Free decoding of the learner is the argmax of a
matrix already computed for the forced alignment, and lining the two symbol
sequences up by edit distance drops the insertions out. What is open is whether
they can be **anchored**: an insertion between two sounds of one word is offered
the letters of that word no sound claimed -- the silent ones -- and kept only if
the affinity table pays one of them for that symbol. No letter, no mark on a
letter; the mark would then belong between two letters, like the gutter.

That filter is the whole question, and this measures it. Free decoding on
learner speech is verified nowhere -- the 95% of brick 4 is read off synthesis
renders -- and it consumes the **label**, the least reliable thing the network
renders and the one thing no mark consumes today. So this counts, per take:
how many insertions the free decoding claims, and how many of them a silent
letter of the right word can carry.

What it does not do, and cannot: say whether an insertion is real. Nothing in
the labelled set says which sounds a take added, so an insertion here is what
the decoding claims, never a fault confirmed. A rate of anchoring is a rate of
anchoring.

    ACOUSTIC_MODEL=timit-ipa python3 insertions.py
"""

import argparse
import sys
from pathlib import Path

import join
import matrix
import phrases

HERE = Path(__file__).resolve().parent
TAKES = HERE / "out" / "takes" / "set"
RENDERS = HERE / "out" / "renders"
MATRICES = HERE / "out" / "matrices" / matrix.SLUG

TEXTS = dict(phrases.CALIBRATION + phrases.HELDOUT)


def pairs(voice):
    """Every (take, phrase) the cache can answer for, without touching audio.

    The phrase of a take comes from the labelled set, never from the cache: a
    take's folder can hold the matrix of a phrase it was compared to in passing,
    and pairing on what is cached puts a learner's sentence against the model of
    a different one -- two texts, so every sound after the first is an insertion
    by construction. Takes the labelled set does not name are paired with the one
    phrase they hold, which is theirs.
    """
    # Imported here and not at the top: `faults` pulls the synthesiser, which the
    # measurement never needs, and `turn.py` consumes the two functions below.
    import faults

    canonical = {take: slug for take, slug, *_ in faults.CASES}
    found = set()
    for folder in sorted(MATRICES.iterdir()):
        name = folder.name
        take = name[4:] if name.startswith("set-") else name
        if not (TAKES / f"{take}.wav").is_file():
            continue
        cached = [held.stem for held in sorted(folder.glob("*.npz"))
                  if held.stem in TEXTS
                  and (MATRICES / f"sentences-{voice}" / f"{held.stem}.npz").is_file()]
        if not cached:
            continue
        wanted = canonical.get(take)
        if wanted is not None:
            if wanted in cached:
                found.add((take, wanted))
        elif len(cached) == 1:
            found.add((take, cached[0]))
        else:
            print(f"    {take}: {len(cached)} phrases en cache et aucune "
                  "au jeu étiqueté — écartée", file=sys.stderr)
    return sorted(found)


def lined(model, said):
    """The learner's sounds against the model's, as an edit-distance path.

    Yields `(rank, symbol)` for each sound of the learner that faces nothing on
    the model's side -- `rank` being the model sound it comes after, -1 before
    the first. Substitutions and omissions are not returned: they are what the
    forced alignment already measures, and only the insertion has no slot.
    """
    rows, columns = len(model) + 1, len(said) + 1
    cost = [[0] * columns for _ in range(rows)]
    for row in range(rows):
        cost[row][0] = row
    for column in range(columns):
        cost[0][column] = column
    for row in range(1, rows):
        for column in range(1, columns):
            same = model[row - 1] == said[column - 1]
            cost[row][column] = min(cost[row - 1][column - 1] + (0 if same else 1),
                                    cost[row - 1][column] + 1,
                                    cost[row][column - 1] + 1)

    added, row, column = [], len(model), len(said)
    while row > 0 or column > 0:
        if row > 0 and column > 0:
            same = model[row - 1] == said[column - 1]
            if cost[row][column] == cost[row - 1][column - 1] + (0 if same else 1):
                row, column = row - 1, column - 1
                continue
        if column > 0 and cost[row][column] == cost[row][column - 1] + 1:
            added.append((row - 1, said[column - 1]))
            column -= 1
            continue
        row -= 1
    return list(reversed(added))


def found(text, sounds, model_syms, said_syms, gaps):
    """Every insertion, anchored where it can be -- what `turn.json` carries.

    `gaps` says which sounds of the grid were compared, so an insertion can name
    the readout line it follows. `Added.kt` is the same thing in Kotlin, and the
    port test holds the two against each other.
    """
    claimed = {position for sound in sounds for position in sound.spots}
    out = []
    for rank, symbol in lined(model_syms, said_syms):
        before = [position for sound in sounds[:rank + 1]
                  for position in sound.spots]
        out.append({
            "symbol": symbol,
            "at": carrier(text, sounds, claimed, rank, symbol),
            "after": max(before) if before else -1,
            "afterSound": max((index for index, gap in enumerate(gaps)
                               if gap.rank <= rank), default=-1),
        })
    return out


def carrier(text, sounds, claimed, rank, symbol, anywhere=False):
    """The letter an insertion would light, or None when none can.

    Only inside a word, and only a letter **no sound claimed** -- which is the
    set of silent letters, since every letter that carries a sound was taken by
    it. Across a word boundary there is nothing to offer: the mark belongs
    between the two words, where the gutter's kind of mark goes.

    And only a letter lying **between the two sounds it fell between**, which is
    the rule `join.lent` already keeps for a borrowed letter: a sound marks where
    it is written, not wherever in the word the table pays best. Without it the
    schwa the learner adds in the middle of a word can light a letter at its far
    end. `anywhere` drops that bound, to say what it costs.
    """
    left = sounds[rank].word_at if 0 <= rank < len(sounds) else None
    right = sounds[rank + 1].word_at if rank + 1 < len(sounds) else None
    if left is None or left != right:
        return None
    low, high = left
    if not anywhere:
        before = sounds[rank].spots
        after = sounds[rank + 1].spots
        if before:
            low = max(low, max(before) + 1)
        if after:
            high = min(high, min(after))
    best, weight = None, 0
    for position in range(low, high):
        if position in claimed or not text[position].isalpha():
            continue
        paid = join.AFFINITY.get(text[position].lower(), {}).get(symbol, 0)
        if paid > weight:
            best, weight = position, paid
    return best


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-c", "--candidate", default="eleven-us-eric")
    parser.add_argument("-v", "--verbose", action="store_true",
                        help="chaque insertion, avec la lettre qui la porte")
    options = parser.parse_args(argv)

    todo = pairs(options.candidate)
    if not todo:
        raise SystemExit(f"aucune matrice en cache pour {options.candidate}")

    print(f"=== insertions, modèle {matrix.SLUG}, voix {options.candidate}")
    print(f"    {'prise':<20}{'phrase':<18}{'grille':>7}{'libre':>7}"
          f"{'insérés':>9}{'portés':>8}")

    totals = [0, 0, 0, 0]
    for take, slug in todo:
        text = TEXTS[slug]
        model_wav = RENDERS / options.candidate / "sentences" / f"{slug}.wav"
        take_wav = TAKES / f"{take}.wav"
        folder = "set-" + take
        if not (MATRICES / folder / f"{slug}.npz").is_file():
            folder = take

        names = matrix.symbols()
        model_grid = matrix.grid(matrix.probabilities(
            model_wav, cache=MATRICES / f"sentences-{options.candidate}" / f"{slug}.npz"))
        said_grid = matrix.grid(matrix.probabilities(
            take_wav, cache=MATRICES / folder / f"{slug}.npz"))
        model_syms = [names[index] for index, _, _ in model_grid]
        said_syms = [names[index] for index, _, _ in said_grid]

        sounds = join.joined(
            model_wav, text,
            cache=join.cache_for(options.candidate, slug))
        claimed = {position for sound in sounds for position in sound.spots}

        added = lined(model_syms, said_syms)
        carried = [(rank, symbol, carrier(text, sounds, claimed, rank, symbol))
                   for rank, symbol in added]
        held = [row for row in carried if row[2] is not None]
        loose = [1 for rank, symbol in added
                 if carrier(text, sounds, claimed, rank, symbol, True) is not None]

        totals[0] += len(model_syms)
        totals[1] += len(added)
        totals[2] += len(held)
        totals[3] += len(loose)
        print(f"    {take:<20}{slug:<18}{len(model_syms):>7}{len(said_syms):>7}"
              f"{len(added):>9}{len(held):>8}")
        if options.verbose:
            for rank, symbol, position in carried:
                where = (f"« {text[position]} » de « "
                         f"{text[slice(*sounds[rank].word_at)]} »"
                         if position is not None else "entre deux lettres")
                after = sounds[rank].symbol if 0 <= rank < len(sounds) else "début"
                print(f"        {symbol:<5} après {after:<5} → {where}")

    print(f"    {'':<38}{'—' * 24}")
    print(f"    {len(todo)} prises, {totals[0]} sons de grille, "
          f"{totals[1]} insérés, {totals[2]} portés par une lettre")
    if totals[1]:
        print(f"    {totals[1] / len(todo):.1f} insertions par prise, "
              f"{100 * totals[2] / totals[1]:.0f} % ancrées "
              f"({totals[3]} sans la borne de position)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
