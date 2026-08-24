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
import sys
from pathlib import Path

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


def joined(wav, text):
    """Every sound the voice produced, and the letters spoken inside it.

    The sounds come from free decoding -- what is there, not what the word
    should hold. A letter goes to the sound it shares the most time with, so
    every letter lands somewhere and lands once: a letter split across two
    sounds is the ordinary case, and halving it would colour neither.
    """
    spread = matrix.probabilities(wav)
    step = matrix.seconds_per_frame()
    sounds = matrix.grid(spread)
    if not sounds:
        raise SystemExit("le décodage libre ne rend aucun son")
    stretches = widened([(start * step, stop * step)
                         for _, start, stop in sounds])

    anchored = letters.anchors(wav, text)
    timed = [(position, span) for position, span in enumerate(anchored)
             if span is not None]
    spelt = widened([span for _, span in timed])

    covered = [[] for _ in sounds]
    for (position, _), (low, high) in zip(timed, spelt):
        shared = [min(high, stop) - max(low, start)
                  for start, stop in stretches]
        best = max(range(len(shared)), key=lambda index: shared[index])
        if shared[best] > 0:
            covered[best].append(text[position])

    return [(matrix.symbols()[index], low, high, "".join(held))
            for (index, _, _), (low, high), held
            in zip(sounds, stretches, covered)]


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
        held = "".join(covered for _, _, _, covered in read)
        sounds += len(read)
        empty += sum(1 for _, _, _, covered in read if not covered.strip())
        spelt += sum(1 for character in text if character.strip())
        orphan += sum(1 for character in text if character.strip()) - sum(
            1 for character in held if character.strip())
        print(f"  {slug:<18}{len(read):>4} sons"
              f"{sum(1 for _, _, _, c in read if not c.strip()):>4} vides")
    print(f"\n  {sounds} sons, {empty} sans aucune lettre "
          f"({100 * empty / sounds:.0f} %)")
    print(f"  {spelt} lettres, {orphan} rattachées à aucun son "
          f"({100 * orphan / spelt:.0f} %)")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("slug", nargs="?", help="une phrase du jeu : "
                                               + ", ".join(sorted(BY_SLUG)))
    parser.add_argument("-a", "--all", action="store_true",
                        help="le compte sur tout le jeu")
    parser.add_argument("-c", "--candidate", default="eleven-us-eric")
    options = parser.parse_args(argv)

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
    print(f"  {'son':<8}{'de':>8}{'à':>8}   lettres")
    for symbol, low, high, covered in joined(wav, text):
        print(f"  {symbol:<8}{low:>8.2f}{high:>8.2f}   "
              + (covered or "—"))
    return 0


if __name__ == "__main__":
    sys.exit(main())
