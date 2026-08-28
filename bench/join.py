#!/usr/bin/env python3
"""Brick 4, run and shown: which letters does each sound cover?

The last step before a mark exists. The analysis says a sound is wrong; only
this says which letters to colour. It reads the grid the model produced and the
text that was said, and joins them by order and spelling alone -- no clock on
either side, no second network, nothing but the sounds, the words and a table of
a few kilobytes.

What qualifies it is that a human can read it: `SH` must fall on `sh` and `IY`
on `ee`. A join that puts `SH` on `es` is wrong and looks wrong, with no
millisecond needed to see it. `expected.py` holds that judgement, written once by
hand, and `-s` marks the join against it.

    python3 join.py sheep-field
"""

import argparse
import json
from collections import namedtuple
import re
import sys
from pathlib import Path

import expected
import matrix
import phrases

HERE = Path(__file__).resolve().parent
RENDERS = HERE / "out" / "renders"
MATRICES = HERE / "out" / "matrices" / matrix.SLUG

SETS = {"calibration": phrases.CALIBRATION, "heldout": phrases.HELDOUT}

# Which sounds a letter takes part in writing, weighted 0 to 3. Not a
# pronunciation dictionary and never asked how a word is said: it answers
# whether `s` takes part in /ʃ/, which is the whole of what decides here. It was
# generated blind, by a session that had never seen where the join failed, so
# that it could not be fitted to those failures.
# One sound of the model's grid, and everything a mark needs of it. `letters`
# reads; `spots` is what a mark is actually drawn on, offsets into the text, and
# `borrowed` the offsets of a neighbour's letter when this sound holds none of
# its own. Empty `spots` and empty `borrowed` together mean the gutter.
Sound = namedtuple("Sound", "symbol low high word letters spots borrowed")

AFFINITY = json.loads((HERE / "affinity.json").read_text(encoding="utf-8"))

# The same question asked of groups of letters that write one sound between
# them: `sh` writes /ʃ/, and the per-letter table cannot say so -- it can only
# say that `s` and `h` each take part in it, which leaves `sch` free to hand its
# `c` to the /s/. Generated blind like the table beside it, and by its own
# session, so that neither could be fitted to where the join failed.
#
# It earns its place only beside the rule below, and that is why it was long
# thought to buy nothing: while every letter had to land somewhere, a group
# could only change *which* neighbour a silent letter was posted to, never
# spare it the trip. Once a letter can hold nothing, the group is what keeps
# the ones that belong -- `kn` on /n/, `mb` on /m/, `wr` on /ɹ/, `dge` on /ʤ/.
GROUPS = json.loads((HERE / "affinity-groups.json").read_text(encoding="utf-8"))

# How many letters a single sound may be given at once. Four covers the longest
# graphemes English writes -- `ough`, `eigh` -- and every group longer than that
# is two graphemes running.
LONGEST = 4

# What it costs to step over a sound and leave it with no letter at all.
# Without it the objective pays for letters and never for sounds, so nothing
# stops a vowel from swallowing its neighbour's letters -- `important` giving
# `orta` to its /ɑ/ and nothing to its /ɹ/. A sound with no letter is a sound no
# mark can be drawn on, which is exactly the defect to price. Read off the
# answer key: 217 sounds of 236 at 0, 221 from 0.7 to 3.0, so the value sits on
# a plateau broad enough not to be a knife edge.
HUNGER = 1.0


def widened(spans):
    """Peaks turned into a covering of the time, the gaps split down the middle.

    The network is peaky: it says "here" on one or two frames and leaves the
    rest to the blank, so a sound arrives as an instant when what is wanted is a
    stretch -- an extract to replay, a syllable to underline. Giving every peak
    the silence up to half-way to its neighbour turns the grid into a covering.
    Nothing in the join reads these bounds; they are the sound's own geometry.
    """
    widened = []
    for position, (low, high) in enumerate(spans):
        if position:
            low = (spans[position - 1][1] + low) / 2
        if position < len(spans) - 1:
            high = (high + spans[position + 1][0]) / 2
        widened.append((low, high))
    return widened


def unspellable(text):
    """The characters that are spoken and that the table cannot write.

    Punctuation is not among them: a comma is silent, and a silent character
    asks nothing of the join. A digit does -- `25` is two characters and four
    syllables -- and so is any letter the table does not name.
    """
    return sorted({character for character in text
                   if character.isalnum() and character.lower() not in AFFINITY})


def checked():
    """The table and the model must name the same sounds, or nothing is joined.

    A table written for another alphabet does not fail: every lookup misses, the
    affinity falls to zero everywhere, and the join returns whatever order alone
    can do, with nothing to say so.
    """
    symbols = set(matrix.symbols())
    unknown = sorted({sound for table in (AFFINITY, GROUPS)
                      for row in table.values() for sound in row} - symbols)
    if unknown:
        raise SystemExit(
            f"affinity.json nomme des sons que {matrix.SLUG} ne rend pas "
            f"({' '.join(unknown)}) — la table est écrite pour un autre "
            "alphabet, elle ne se branche pas ici")


def inner(characters, symbols):
    """A word's letters onto a group of sounds, the assignment never going back.

    Returns what the match is worth and, beside it, the sound each letter chose.
    Letters are taken one to `LONGEST` at a time: a group the table names is
    read as the single grapheme it is, and paid for every letter it carries, so
    that spelling `ch` as one /k/ outweighs spelling `c` and `h` apart. A group
    too small to hold the letters is not refused -- several letters sharing a
    sound is the ordinary case, and a letter split across two would colour
    neither.
    """
    count = len(characters)
    if not symbols:
        return 0.0, [None] * count
    best = [[float("-inf")] * len(symbols) for _ in range(count + 1)]
    back = [[None] * len(symbols) for _ in range(count + 1)]
    for index in range(count):
        if index and all(score == float("-inf") for score in best[index]):
            continue
        # Stepping from `sound` to a later one costs one hunger per sound
        # skipped, so the best predecessor maximises `best[sound] + HUNGER *
        # sound` -- a running maximum, the skipped stretch being the same for
        # all of them.
        running, argmax = float("-inf"), 0
        reached = []
        for sound in range(len(symbols)):
            if index:
                if sound:
                    stepped = running - HUNGER * (sound - 1)
                    if best[index][sound] > stepped:
                        reached.append((best[index][sound], sound))
                    else:
                        reached.append((stepped, argmax))
                else:
                    reached.append((best[index][0], 0))
                candidate = best[index][sound] + HUNGER * sound
                if candidate > running:
                    running, argmax = candidate, sound
            else:
                reached.append((-HUNGER * sound, None))
        for length in range(1, min(LONGEST, count - index) + 1):
            group = "".join(characters[index:index + length]).lower()
            weights = GROUPS.get(group) if length > 1 else AFFINITY.get(group)
            if weights is None:
                continue
            for sound, symbol in enumerate(symbols):
                previous, chosen = reached[sound]
                score = previous + length * weights.get(symbol, 0) / 3
                if score > best[index + length][sound]:
                    best[index + length][sound] = score
                    back[index + length][sound] = (length, chosen)
    last = max(range(len(symbols)),
               key=lambda sound: best[count][sound]
               - HUNGER * (len(symbols) - sound - 1))
    total = best[count][last] - HUNGER * (len(symbols) - last - 1)
    if total == float("-inf"):
        # Not one letter of the word is in the table: it takes no sound rather
        # than taking them all for nothing.
        return 0.0, [None] * count
    chosen = [None] * count
    worth = [False] * count
    index = count
    while index:
        length, previous = back[index][last]
        group = "".join(characters[index - length:index]).lower()
        weights = GROUPS.get(group) if length > 1 else AFFINITY.get(group)
        for position in range(index - length, index):
            chosen[position] = last
            worth[position] = bool(weights.get(symbols[last], 0))
        index, last = index - length, previous
    return total, trimmed(chosen, worth)


def trimmed(chosen, worth):
    """Each sound's letters cut back to the ones it is paid for, at the ends.

    A letter worth nothing on the sound it landed on was posted there by the
    order of the walk and by nothing else, so where it sits says nothing: the
    silent `t` of `listen` is worth zero on every sound that word offers. It
    holds nothing instead. What saves the silent letters that do belong is the
    group table, which pays `kn` on /n/ where neither `k` nor `n` alone would.

    Only at the ends: a letter worth nothing in the middle of a run is held
    inside a spelling, and dropping it would leave the sound two letters with a
    hole between them -- `take` painted as `a`..`e`. What the trim removes is
    the silent tail and the silent head, which is where English keeps them.
    """
    kept = list(chosen)
    for sound in {value for value in chosen if value is not None}:
        run = [position for position, value in enumerate(chosen)
               if value == sound]
        low, high = 0, len(run) - 1
        while low <= high and not worth[run[low]]:
            kept[run[low]] = None
            low += 1
        while high > low and not worth[run[high]]:
            kept[run[high]] = None
            high -= 1
    return kept


def partition(words, symbols):
    """Where each word's sounds start and stop, the cut chosen by spelling.

    This is what keeps a letter from reaching across a word boundary, and it
    asks the audio for nothing: the sound sequence is cut into as many contiguous
    groups as the text has words, the cut taken to be the one that spells the
    whole sentence best. A word may be given no sound at all -- an elided
    function word is real, and forcing one on it would steal it from a neighbour.
    """
    count = len(symbols)
    cost = {}
    for rank, (_, characters) in enumerate(words):
        for start in range(count + 1):
            for stop in range(start, count + 1):
                cost[rank, start, stop] = inner(characters,
                                                symbols[start:stop])[0]
    best = [[float("-inf")] * (count + 1) for _ in range(len(words) + 1)]
    back = [[0] * (count + 1) for _ in range(len(words) + 1)]
    best[0][0] = 0.0
    for rank in range(len(words)):
        for start in range(count + 1):
            if best[rank][start] == float("-inf"):
                continue
            for stop in range(start, count + 1):
                score = best[rank][start] + cost[rank, start, stop]
                if score > best[rank + 1][stop]:
                    best[rank + 1][stop] = score
                    back[rank + 1][stop] = start
    cuts = [count]
    for rank in range(len(words), 0, -1):
        cuts.append(back[rank][cuts[-1]])
    return list(reversed(cuts))


def spoken(text):
    """The words of `text`, each with its spelling and its writable letters."""
    words = []
    for match in re.finditer(r"\S+", text):
        positions = [position for position in range(*match.span())
                     if text[position].lower() in AFFINITY]
        if positions:
            words.append((match.group(), positions))
    return words


def cache_for(voice, slug):
    """Where a render's matrix is filed -- the same place `overlap` reads it.

    Two readings of the same audio have to be the one reading: a join computed
    fresh beside a comparison read from cache pairs sounds with the letters of
    their neighbours as soon as the two grids differ by one.
    """
    return MATRICES / f"sentences-{voice}" / f"{slug}.npz"


def joined(wav, text, cache=None):
    """Every sound the voice produced, and the letters spoken inside it.

    The sounds come from free decoding -- what is there, not what the word
    should hold -- and the words come from the text. Words first: the sound
    sequence is partitioned between them, then each word's letters are matched
    inside its own group. Every letter the table can write lands at most once,
    and a letter the table pays nothing for lands nowhere at all.
    """
    checked()
    spread = matrix.probabilities(wav, cache=cache)
    step = matrix.seconds_per_frame()
    sounds = matrix.grid(spread)
    if not sounds:
        raise SystemExit("le décodage libre ne rend aucun son")
    stretches = widened([(start * step, stop * step)
                         for _, start, stop in sounds])
    symbols = [matrix.symbols()[index] for index, _, _ in sounds]

    words = spoken(text)
    cuts = partition([(word, [text[position] for position in positions])
                      for word, positions in words], symbols)

    covered = ["" for _ in sounds]
    spots = [[] for _ in sounds]
    held = [None] * len(sounds)
    for rank, (word, positions) in enumerate(words):
        start, stop = cuts[rank], cuts[rank + 1]
        for index in range(start, stop):
            held[index] = word
        _, chosen = inner([text[position] for position in positions],
                          symbols[start:stop])
        for position, sound in zip(positions, chosen):
            if sound is not None:
                covered[start + sound] += text[position]
                spots[start + sound].append(position)

    return [Sound(symbol, low, high, word or "—", letters_held,
                  tuple(where), borrowed)
            for symbol, (low, high), word, letters_held, where, borrowed
            in zip(symbols, stretches, held, covered, spots,
                   lent(symbols, held, covered, spots, text))]


def lent(symbols, held, covered, spots, text):
    """For a sound holding no letter, the letter it would light anyway.

    A letter lands on one sound and no more, which is what keeps the match
    honest -- but it is a rule of the match, not of the screen. `x` writes the
    /k/ of `boxes` and then its /s/, one of the two gets the letter, and the
    other would have nothing to colour although the very same `x` is where it
    is written. So the sound borrows rather than holds: two sounds may light one
    letter, neither owns it.

    Only a sound that touches it can lend, and only inside the same word. Those
    two sounds are adjacent in time and their letters adjacent in the text,
    which is the whole reason the loan reads as pointing at the right place; a
    letter fetched from across the word would mark somewhere the sound is not.

    Empty where the word offers no letter the table pays for that sound, which
    is the other case entirely -- the schwa of `doesn't`, that English writes
    with nothing. Nothing here can invent a letter for it; the screen marks the
    gutter between its neighbours instead.
    """
    out = [()] * len(symbols)
    for index, letters in enumerate(covered):
        if letters.strip() or held[index] is None:
            continue
        word = held[index]
        # The letters of this word, wherever they landed, ranked by what the
        # table pays them for *this* sound; ties go to the nearest sound.
        best, score = None, 0
        for other in (index - 1, index + 1):
            if not 0 <= other < len(held):
                continue
            if held[other] is not word or not covered[other].strip():
                continue
            for position in spots[other]:
                weight = AFFINITY.get(text[position].lower(),
                                      {}).get(symbols[index], 0)
                if weight > score:
                    best, score = position, weight
        out[index] = () if best is None else (best,)
    return out


def scored(candidate, material):
    """The join marked against the letters a human said each sound should hold.

    Whether the letters are the *right* ones is a question about English
    spelling, and `expected.py` is where it was answered, by hand, once --
    indexed by the sounds the network actually decoded, so it marks the model it
    was written against and no other.
    """
    right = counted = 0
    for slug, text in material:
        answer = expected.COVERED.get(slug)
        wav = RENDERS / candidate / "sentences" / f"{slug}.wav"
        if answer is None or unspellable(text) or not wav.is_file():
            continue
        read = ["".join(sound.letters.split())
                for sound in joined(wav, text,
                                    cache=cache_for(candidate, slug))]
        if len(read) != len(answer):
            # A grid of another shape than the one annotated: marking it would
            # compare sounds that are not the same sounds.
            print(f"  {slug:<18}{len(read)} sons contre {len(answer)} annotés")
            continue
        wrong = [f"{ought or '∅'}→{held or '∅'}"
                 for held, ought in zip(read, answer)
                 if held.lower() != "".join(ought.split()).lower()]
        right += len(answer) - len(wrong)
        counted += len(answer)
        print(f"  {slug:<18}{len(answer) - len(wrong):>3} / {len(answer):<4}"
              + "  ".join(wrong))
    if not counted:
        raise SystemExit("aucune phrase annotée pour cette grille")
    print(f"\n  {right} / {counted} sons portent les bonnes lettres "
          f"({100 * right / counted:.0f} %)")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("slug", nargs="?", help="une phrase du jeu")
    parser.add_argument("-s", "--score", action="store_true",
                        help="la note contre l'annotation d'expected.py")
    parser.add_argument("-j", "--jeu", default="calibration", choices=sorted(SETS),
                        help="calibration (celui qui a réglé) ou heldout "
                             "(celui qui n'a rien réglé)")
    parser.add_argument("-c", "--candidate", default="eleven-us-eric")
    options = parser.parse_args(argv)
    material = SETS[options.jeu]

    if options.score:
        scored(options.candidate, material)
        return 0
    if options.slug is None:
        raise SystemExit("nomme une phrase du jeu : "
                         + ", ".join(slug for slug, _ in material))

    text = dict(material).get(options.slug)
    if text is None:
        raise SystemExit(f"{options.slug!r} n'est pas une phrase du jeu")
    missing = unspellable(text)
    if missing:
        raise SystemExit(f"{options.slug!r} porte des caractères que la table "
                         f"n'écrit pas ({' '.join(missing)})")
    wav = RENDERS / options.candidate / "sentences" / f"{options.slug}.wav"
    if not wav.is_file():
        raise SystemExit(f"{wav} manque — rends d'abord le matériel du banc")

    print(f"\n{text}\n")
    print(f"  {'son':<8}{'de':>8}{'à':>8}   {'mot':<14}lettres")
    for sound in joined(wav, text, cache=cache_for(options.candidate,
                                                   options.slug)):
        # A borrowed letter is shown in brackets: nothing is held there, but
        # that is where a mark would be drawn.
        loan = "".join(text[at] for at in sound.borrowed)
        shown = sound.letters or (f"({loan})" if loan else "— intervalle")
        print(f"  {sound.symbol:<8}{sound.low:>8.2f}{sound.high:>8.2f}"
              f"   {sound.word:<14}{shown}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
