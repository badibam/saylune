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
import os
import re
import sys
from pathlib import Path

import expected
import matrix
import phrases

HERE = Path(__file__).resolve().parent
RENDERS = HERE / "out" / "renders"

SETS = {"calibration": phrases.CALIBRATION, "heldout": phrases.HELDOUT}

# Which sounds a letter takes part in writing, weighted 0 to 3. Not a
# pronunciation dictionary and never asked how a word is said: it answers
# whether `s` takes part in /ʃ/, which is the whole of what decides here. It was
# generated blind, by a session that had never seen where the join failed, so
# that it could not be fitted to those failures.
AFFINITY = json.loads((HERE / "affinity.json").read_text(encoding="utf-8"))

# The same question asked of groups of letters that write one sound between
# them: `sh` writes /ʃ/, and the per-letter table cannot say so -- it can only
# say that `s` and `h` each take part in it, which leaves `sch` free to hand its
# `c` to the /s/. Generated blind like the table beside it, and by its own
# session, so that neither could be fitted to where the join failed.
#
# Read only under `JOIN_GROUPS=1`, and the reason is measured: it costs two
# sounds, 219 against 221. A group has to out-score the sum of its letters, and
# the per-letter weights are generous by construction -- a letter takes 3 for
# its main sound across every spelling it appears in -- so `ch` at 2 on /k/
# loses to `c` at 3 on /s/ plus `h` at 2 on /k/. What is missing is not either
# table but an arbiter between them, and a weighting chosen now would be chosen
# against these fifteen sentences. It waits for the wider corpus.
GROUPS = (json.loads((HERE / "affinity-groups.json").read_text(encoding="utf-8"))
          if os.environ.get("JOIN_GROUPS") == "1" else {})

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
    index = count
    while index:
        length, previous = back[index][last]
        for position in range(index - length, index):
            chosen[position] = last
        index, last = index - length, previous
    return total, chosen


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


def joined(wav, text):
    """Every sound the voice produced, and the letters spoken inside it.

    The sounds come from free decoding -- what is there, not what the word
    should hold -- and the words come from the text. Words first: the sound
    sequence is partitioned between them, then each word's letters are matched
    inside its own group. Every letter the table can write lands somewhere and
    lands once.
    """
    checked()
    spread = matrix.probabilities(wav)
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

    return [(symbol, low, high, word or "—", letters_held)
            for symbol, (low, high), word, letters_held
            in zip(symbols, stretches, held, covered)]


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
        read = ["".join(held.split()) for *_, held in joined(wav, text)]
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
    for symbol, low, high, word, covered in joined(wav, text):
        print(f"  {symbol:<8}{low:>8.2f}{high:>8.2f}   {word:<14}"
              + (covered or "—"))
    return 0


if __name__ == "__main__":
    sys.exit(main())
