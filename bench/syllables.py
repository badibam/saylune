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
from collections import namedtuple
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


# What can begin an English syllable. Every consonant but the velar nasal
# stands alone; the clusters are the ones English actually allows, which is
# what stops `extra` being cut before `xtr`. Written in the symbols of the
# model's own vocabulary, since that is what the grid hands over. The flap is
# admitted: it never opens a word, but it opens the second syllable of
# `butter`, which is the only place this rule is ever asked about.
CONSONANTS = "b ʧ d ð ɾ f g h ʤ k l m n p ɹ s ʃ t θ v w j z".split()
CLUSTERS = """
pl pɹ pj bl bɹ bj tɹ tw tj dɹ dw dj kl kɹ kw kj gl gɹ gw gj
fl fɹ fj θɹ θw ʃɹ hj mj nj lj vj
sp st sk sf sm sn sl sw sj
spl spɹ spj stɹ stj skɹ skw skj skl
"""

Syllable = namedtuple("Syllable", "sounds low high word letters spots")


def legal():
    """Every onset the cut may hand to a syllable, the empty one included.

    A syllable may open on a vowel, so nothing is not an onset -- and it has to
    be in the table, because it is the answer whenever a cluster is entirely
    coda.
    """
    onsets = {(), *((symbol,) for symbol in CONSONANTS)}
    for cluster in CLUSTERS.split():
        group, rest = [], cluster
        while rest:
            # Symbols are not one character each, so a cluster is read against
            # the inventory rather than sliced.
            for symbol in sorted(CONSONANTS, key=len, reverse=True):
                if rest.startswith(symbol):
                    group.append(symbol)
                    rest = rest[len(symbol):]
                    break
            else:
                raise SystemExit(f"attaque illisible : {cluster!r}")
        onsets.add(tuple(group))
    return frozenset(onsets)


ONSETS = legal()


def opening(symbols):
    """How many of the consonants between two nuclei open the second syllable.

    The maximal onset principle, bounded by what English allows: the longest
    tail of the cluster that is a legal onset goes to the syllable ahead, and
    whatever is left falls back as the coda of the one behind. `extra` cuts
    `ek·stɹə` rather than `e·kstɹə`, because `kstɹ` opens nothing.
    """
    for size in range(len(symbols), -1, -1):
        if tuple(symbols[len(symbols) - size:]) in ONSETS:
            return size
    return 0


def cut(sounds):
    """The syllables of a joined sentence: brick 8, on the sounds themselves.

    No dictionary and no letters decide anything here -- English spelling lies
    about how many syllables were said, and a lexicon would judge by a norm the
    design keeps out. What comes in is the grid as `join.joined` left it, so a
    syllable inherits an exact stretch of time and an exact stretch of text,
    which is what the marking needs (`../docs/analysis.md`).

    A word whose grid carries no vowel yields no syllable at all. That is not a
    failure to paper over: it is the reduced word, and what to make of it is
    brick 7's question, not this one's.
    """
    out = []
    for start, stop in join.runs(sounds):
        symbols = [sound.symbol for sound in sounds[start:stop]]
        nuclei = [rank for rank, symbol in enumerate(symbols)
                  if symbol in matrix.VOWELS]
        if not nuclei:
            continue
        edges = [0]
        for here, then in zip(nuclei, nuclei[1:]):
            edges.append(then - opening(symbols[here + 1:then]))
        edges.append(len(symbols))
        for low, high in zip(edges, edges[1:]):
            group = sounds[start + low:start + high]
            spots = sorted({spot for sound in group
                            for spot in tuple(sound.spots) + tuple(sound.borrowed)})
            out.append(Syllable((start + low, start + high),
                                group[0].low, group[-1].high, group[0].word,
                                "".join(sound.letters for sound in group),
                                tuple(spots)))
    return out


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
                   for word, _, positions in words]
        want_cuts = join.partition(spelled, wanted)
        got_cuts = join.partition(spelled, got)
        for rank, (word, _, _) in enumerate(words):
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


def sliced(verbose):
    """The cut itself, laid out word by word.

    What this shows and what it measures are not the same thing. The *number*
    of syllables is scored just above, against the annotation: the cut yields
    exactly one per nucleus, so that count answers for it. Where the boundary
    falls between two nuclei is scored against nothing -- it would take an
    annotation nobody has written, and the rule that places it consults no
    audio, so a wrong cut is a wrong rule and not a misread. Read below.
    """
    texts = dict(phrases.CALIBRATION)
    total = mute = 0
    print(f"\n  La coupe, mot par mot ({VOICE})\n")
    for slug in expected.SOUNDS:
        wav = RENDERS / f"{slug}.wav"
        if not wav.is_file():
            continue
        sounds = join.joined(wav, texts[slug],
                             cache=join.cache_for(VOICE, slug))
        pieces = cut(sounds)
        for start, stop in join.runs(sounds):
            word = sounds[start].word
            mine = [piece for piece in pieces
                    if start <= piece.sounds[0] and piece.sounds[1] <= stop]
            total += len(mine)
            mute += not mine
            if verbose:
                shown = " · ".join("".join(sounds[i].symbol
                                           for i in range(*piece.sounds))
                                   for piece in mine)
                letters = " · ".join(piece.letters or "-" for piece in mine)
                print(f"    {slug:<16}{word:<14}{shown or '—':<24}"
                      f"{letters or '—'}")
    print(f"    {total} syllabes coupées, {mute} mot"
          f"{'s' if mute > 1 else ''} sans noyau dans la grille")
    print("    où tombe la frontière n'est comparé à rien : la règle ne "
          "consulte aucun audio,")
    print("    donc une coupe fausse serait une règle fausse, pas une "
          "mauvaise lecture.")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-v", "--verbose", action="store_true",
                        help="le détail par phrase")
    options = parser.parse_args(argv)
    lost(options.verbose)
    counted(options.verbose)
    sliced(options.verbose)
    print(f"\n{matrix.audios()}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
