#!/usr/bin/env python3
"""Can the sounds say how many syllables a word has?

Brick 8 cuts syllables on the sounds, never on the letters, because English
spelling lies about how many were spoken -- `picked` is written in two and said
in one. The price is that a syllable exists only if its vowel was decoded, and
the grid loses about one sound in eight.

Two questions, and the second is the one that decides:

    which sounds go missing -- a lost consonant leaves the syllable standing,
    a lost vowel removes it;

    how many nuclei each word ends up with, against what the bench annotation
    says was spoken. `join.partition` cuts a sound sequence into words without
    consulting the clock, so the same cut is applied to both sequences.

Both read `expected.SOUNDS`, written by hand, so this runs on the calibration
set alone.

    python3 syllables.py            # les deux comptages
    python3 syllables.py -v         # avec le détail par phrase
"""

import argparse
from pathlib import Path

import numpy as np

import expected
import join
import matrix
import phrases

HERE = Path(__file__).resolve().parent
VOICE = "eleven-us-eric"
RENDERS = HERE / "out" / "renders" / VOICE / "sentences"
MATRICES = HERE / "out" / "matrices" / matrix.SLUG / f"sentences-{VOICE}"

# Words that carry no lexical stress worth marking. A reduced `to` or `at` is
# what connected speech does, so its vowel going missing costs nothing; the
# mark lands on content words.
FUNCTION = {
    "a", "am", "an", "and", "are", "as", "at", "be", "but", "by", "can", "do",
    "for", "from", "has", "have", "he", "her", "his", "i", "if", "in", "is",
    "it", "me", "my", "no", "not", "of", "on", "or", "she", "so", "that",
    "the", "there", "this", "to", "was", "we", "will", "would", "you", "your",
}


def matched(one, two):
    """Which of `one` the longest common subsequence with `two` keeps."""
    length = np.zeros((len(one) + 1, len(two) + 1), dtype=int)
    for i in range(len(one) - 1, -1, -1):
        for j in range(len(two) - 1, -1, -1):
            length[i, j] = (1 + length[i + 1, j + 1] if one[i] == two[j]
                            else max(length[i + 1, j], length[i, j + 1]))
    kept, i, j = set(), 0, 0
    while i < len(one) and j < len(two):
        if one[i] == two[j]:
            kept.add(i)
            i, j = i + 1, j + 1
        elif length[i + 1, j] >= length[i, j + 1]:
            i += 1
        else:
            j += 1
    return kept


def nuclei(symbols):
    return sum(1 for symbol in symbols if symbol in matrix.VOWELS)


def readings():
    """For each annotated sentence: the sounds wanted, and the grid decoded."""
    table = matrix.symbols()
    texts = dict(phrases.CALIBRATION)
    for slug, wanted in expected.SOUNDS.items():
        spread = matrix.probabilities(RENDERS / f"{slug}.wav",
                                      cache=MATRICES / f"{slug}.npz")
        got = [table[index] for index, _, _ in matrix.grid(spread)]
        yield slug, texts[slug], wanted.split(), got


def lost(verbose):
    """Which sounds the grid does not carry, vowels held apart from consonants.

    A vowel the network writes with another vowel counts as lost here and is
    not a lost syllable: the count of nuclei below is what answers that.
    """
    kept_vowel = kept_consonant = 0
    gone_vowel, gone_consonant = [], []

    if verbose:
        print(f"\n  {'phrase':<18}{'voyelles perdues':<26}consonnes perdues")
    for slug, _, wanted, got in readings():
        kept = matched([expected.alike(s) for s in wanted],
                       [expected.alike(s) for s in got])
        vowels = [s for i, s in enumerate(wanted)
                  if i not in kept and s in matrix.VOWELS]
        consonants = [s for i, s in enumerate(wanted)
                      if i not in kept and s not in matrix.VOWELS]
        kept_vowel += sum(1 for s in wanted if s in matrix.VOWELS)
        kept_consonant += sum(1 for s in wanted if s not in matrix.VOWELS)
        gone_vowel += vowels
        gone_consonant += consonants
        if verbose:
            print(f"  {slug:<18}{' '.join(vowels) or '-':<26}"
                  f"{' '.join(consonants) or '-'}")

    print(f"\n  Sons attendus que la grille ne porte pas ({matrix.SLUG})\n")
    print(f"    voyelles   : {len(gone_vowel):>3} sur {kept_vowel:>3}"
          f"   ({len(gone_vowel) / kept_vowel:.0%})")
    print(f"    consonnes  : {len(gone_consonant):>3} sur {kept_consonant:>3}"
          f"   ({len(gone_consonant) / kept_consonant:.0%})")


def counted(verbose):
    """How many nuclei each word gets, against how many were spoken."""
    rows = []
    wrong = {True: 0, False: 0}
    total = {True: 0, False: 0}

    for slug, text, wanted, got in readings():
        words = join.spoken(text)
        spelled = [(word, [text[position] for position in positions])
                   for word, positions in words]
        want_cuts = join.partition(spelled, wanted)
        got_cuts = join.partition(spelled, got)
        for rank, (word, _) in enumerate(words):
            ought = nuclei(wanted[want_cuts[rank]:want_cuts[rank + 1]])
            has = nuclei(got[got_cuts[rank]:got_cuts[rank + 1]])
            outil = word.strip(".,!?'").lower() in FUNCTION
            total[outil] += 1
            wrong[outil] += ought != has
            if ought != has:
                rows.append((slug, word, ought, has, outil))

    print(f"\n  Syllabes par mot : ce que l'annotation dit, ce que la grille "
          f"rend\n")
    if verbose or rows:
        print(f"    {'phrase':<18}{'mot':<14}{'dit':>5}{'grille':>8}   nature")
        for slug, word, ought, has, outil in rows:
            print(f"    {slug:<18}{word:<14}{ought:>5}{has:>8}   "
                  f"{'outil' if outil else 'plein'}")
        print()
    for outil, name in ((False, "mots pleins"), (True, "mots outils")):
        print(f"    {name:<13}: {total[outil] - wrong[outil]:>3} justes sur "
              f"{total[outil]:<3} ({1 - wrong[outil] / total[outil]:.0%})")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-v", "--verbose", action="store_true",
                        help="le détail par phrase")
    options = parser.parse_args(argv)
    lost(options.verbose)
    counted(options.verbose)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
