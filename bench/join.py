#!/usr/bin/env python3
"""Brick 4, run and shown: which letters does each sound cover?

The two networks read the same audio on the same 20 ms grid -- one spreads over
sounds, the other over letters -- and this is where they meet. It is the last
step before a mark exists: the analysis says a sound is wrong, and only this
says which letters to colour.

Nothing here is compared to a provider. Both maps are ours, and what qualifies
the join is that a human can read it: `SH` must fall on `sh` and `IY` on `ee`.
A join that puts `SH` on `es` is wrong and looks wrong, with no millisecond
needed to see it.

    python3 join.py sheep-field
"""

import argparse
import re
import sys
from pathlib import Path

import expected
import letters
import matrix
import phrases

HERE = Path(__file__).resolve().parent
RENDERS = HERE / "out" / "renders"

BY_SLUG = dict(phrases.CALIBRATION)


def widened(spans):
    """Peaks turned into a covering of the time, the gaps split down the middle.

    Both networks are peaky: each says "here" on one or two frames and leaves
    the rest to the blank. Two such maps of the same audio rarely point at the
    same frames, so intersecting them directly finds nothing -- which is what
    joining a point to a point amounts to. Giving every peak the silence up to
    half-way to its neighbour turns each map into a covering, and the join
    becomes an overlap between two stretches rather than a coincidence between
    two instants.
    """
    widened = []
    for position, (low, high) in enumerate(spans):
        if position:
            low = (spans[position - 1][1] + low) / 2
        if position < len(spans) - 1:
            high = (high + spans[position + 1][0]) / 2
        widened.append((low, high))
    return widened


def spoken(text, anchored):
    """The words of `text`, each with its spelling and the characters that have
    a time.

    A word is a run of non-space characters, and the space between two of them
    stops here being a character that can land on a sound: the letter network
    aligns the separator like any other symbol, so where the words part is
    something the audio was asked rather than something guessed -- an answer
    worth more as a boundary than as a letter.
    """
    words = []
    for match in re.finditer(r"\S+", text):
        timed = [position for position in range(*match.span())
                 if anchored[position] is not None]
        if timed:
            words.append((match.group(), timed))
    return words


def owners(windows, stretches):
    """Which word each sound belongs to, by the time the two maps share.

    Widened, both maps cover the whole audio, so every sound falls in some word
    and falls in one. This partition is what keeps a letter from reaching across
    a boundary -- the leak the pure-overlap geometry left open, and the shape
    behind the empty counts.
    """
    placed = []
    for start, stop in stretches:
        shared = [min(stop, high) - max(start, low) for low, high in windows]
        best = max(range(len(shared)), key=lambda index: shared[index])
        if shared[best] <= 0:
            middle = (start + stop) / 2
            best = min(range(len(windows)), key=lambda index: min(
                abs(middle - windows[index][0]),
                abs(middle - windows[index][1])))
        placed.append(best)
    return placed


def nearest(low, high, spans):
    """The span sharing the most time with `low`-`high`, the closest if none."""
    shared = [min(high, stop) - max(low, start) for start, stop in spans]
    best = max(range(len(shared)), key=lambda index: shared[index])
    if shared[best] > 0:
        return best
    middle = (low + high) / 2
    return min(range(len(spans)), key=lambda index: min(
        abs(middle - spans[index][0]), abs(middle - spans[index][1])))


def joined(wav, text):
    """Every sound the voice produced, and the letters spoken inside it.

    The sounds come from free decoding -- what is there, not what the word
    should hold -- and the words come from the letter map, which knows the text.
    Words first: each sound is placed in one word, and a letter may only reach
    the sounds of its own. Inside the word a letter goes to the sound it shares
    the most time with, the nearest when it shares none, so every letter of a
    word that was heard lands somewhere and lands once: a letter split across
    two sounds is the ordinary case, and halving it would colour neither.
    """
    spread = matrix.probabilities(wav)
    step = matrix.seconds_per_frame()
    sounds = matrix.grid(spread)
    if not sounds:
        raise SystemExit("le décodage libre ne rend aucun son")
    stretches = widened([(start * step, stop * step)
                         for _, start, stop in sounds])

    anchored = letters.anchors(wav, text)
    words = spoken(text, anchored)
    timed = [position for _, positions in words for position in positions]
    spelt = dict(zip(timed, widened([anchored[position]
                                     for position in timed])))
    windows = [(spelt[positions[0]][0], spelt[positions[-1]][1])
               for _, positions in words]

    held = owners(windows, stretches)
    covered = [[] for _ in sounds]
    for rank, (_, positions) in enumerate(words):
        reachable = [index for index, owner in enumerate(held) if owner == rank]
        if not reachable:
            # A word no sound was placed in: its letters stay unattached, and
            # the tally counts them, no mark being able to reach them.
            continue
        for position in positions:
            low, high = spelt[position]
            inside = nearest(low, high, [stretches[index]
                                         for index in reachable])
            covered[reachable[inside]].append(text[position])

    return [(matrix.symbols()[index], low, high, words[owner][0],
             "".join(letters_held))
            for (index, _, _), (low, high), owner, letters_held
            in zip(sounds, stretches, held, covered)]


def tally(candidate):
    """What can be counted without knowing the answer.

    A sound that comes back with no letter is a sound no mark could ever be
    drawn from, and a letter attached to no sound is a letter no mark could
    ever reach. Both are defects a machine can see. Whether the letters a sound
    did receive are the *right* ones is not among them -- that needs someone who
    knows how the word is written, which is the whole reason this montage
    refuses a grapheme-to-phoneme table.
    """
    sounds = empty = spelt = orphan = 0
    for slug, text in phrases.CALIBRATION:
        if letters.unspellable(text):
            continue
        wav = RENDERS / candidate / "sentences" / f"{slug}.wav"
        if not wav.is_file():
            continue
        read = joined(wav, text)
        held = "".join(covered for *_, covered in read)
        sounds += len(read)
        empty += sum(1 for *_, covered in read if not covered)
        spelt += sum(1 for character in text if character.strip())
        orphan += sum(1 for character in text if character.strip()) - len(held)
        print(f"  {slug:<18}{len(read):>4} sons"
              f"{sum(1 for *_, covered in read if not covered):>4} vides")
    print(f"\n  {sounds} sons, {empty} sans aucune lettre "
          f"({100 * empty / sounds:.0f} %)")
    print(f"  {spelt} lettres, {orphan} rattachées à aucun son "
          f"({100 * orphan / spelt:.0f} %)")


def scored(candidate):
    """The join marked against the letters a human said each sound should hold.

    The two counters of `tally` see a hole, never a misplacement, and they miss
    most of what is wrong: on the outgoing model they report eight defects where
    the answer key finds eighty-nine. Whether the letters are the *right* ones
    is a question about English spelling, and `expected.py` is where it was
    answered, by hand, once -- indexed by the sounds the network actually
    decoded, so it marks the model it was written against and no other.
    """
    right = counted = 0
    for slug, text in phrases.CALIBRATION:
        answer = expected.COVERED.get(slug)
        wav = RENDERS / candidate / "sentences" / f"{slug}.wav"
        if answer is None or letters.unspellable(text) or not wav.is_file():
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
    parser.add_argument("slug", nargs="?", help="une phrase du jeu : "
                                               + ", ".join(sorted(BY_SLUG)))
    parser.add_argument("-a", "--all", action="store_true",
                        help="le compte sur tout le jeu")
    parser.add_argument("-s", "--score", action="store_true",
                        help="la note contre l'annotation d'expected.py")
    parser.add_argument("-c", "--candidate", default="eleven-us-eric")
    options = parser.parse_args(argv)

    if options.score:
        scored(options.candidate)
        return 0
    if options.all:
        tally(options.candidate)
        return 0
    if options.slug is None:
        raise SystemExit("nomme une phrase, ou -a pour le compte")

    text = BY_SLUG.get(options.slug)
    if text is None:
        raise SystemExit(f"{options.slug!r} n'est pas une phrase du jeu")
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
