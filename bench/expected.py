#!/usr/bin/env python3
"""Which letters each sound of the grid ought to cover.

Run it to get the worksheet for a set of sentences -- the sounds the network
decoded, in order, beside the text -- which is the one form an answer can be
written in, since the annotation is indexed by those sounds and not by the word:

    python3 expected.py -j heldout

The one thing no machine in this montage can supply. The join produces letters
for every sound; whether they are the right letters is a question about English
spelling, and the pipeline deliberately holds no grapheme-to-phoneme table. So
it is written here, by hand, once -- as the labelled set of `qualification.md`
is written by hand.

Two things this is not. It is not a pronunciation dictionary: it is indexed by
the sounds the network *actually decoded* on these very renders, free decoding
and all, and a different voice or a different model produces a different grid
that this annotation no longer describes. And it never enters the app: it lives
in the bench, beside the takes, as the answer a test is marked against.

A sound holds the whole group that writes it -- `igh` for the vowel of `right`,
`kn` for the /n/ of `know`, `mb` for the /m/ of `climb`. A silent letter inside
such a group belongs to it: if that sound is marked, the group is what has to
light up, and colouring `k`+`n`+`ow` with the `k` left dark points at the wrong
place.

A letter whose sound the grid never decoded holds nothing at all. The `ck` of
`picked` where no /k/ is read, the `o` of `-tion` where the vowel is lost: there
is nothing measured there to mark, so there is nothing to light. Where several
sounds share one letter the letter is repeated. Spaces are ignored.
"""

from pathlib import Path

HERE = Path(__file__).resolve().parent

# slug -> the letters of each sound of the grid, in order.
COVERED = {
    "think": ["I", "th", "i", "n", "k", "y", "ou", "are", "r", "igh", "t"],
    "sink-broken": ["Th", "e", "s", "i", "n", "k", "i", "s", "b", "r", "o",
                    "k", "e", "n"],
    "sheep-field": ["Th", "e", "sh", "ee", "p", "i", "s", "i", "n", "th", "e",
                    "f", "ie", "l", "d"],
    "pear-tree": ["I", "p", "i", "d", "a", "p", "ea", "r", "f", "m", "th", "e",
                  "t", "r", "ee"],
    "turn-right": ["T", "ur", "n", "r", "igh", "t", "a", "th", "e", "c", "o",
                   "r", "n", "er"],
    "walking-office": ["I", "a", "m", "w", "al", "k", "i", "ng", "t", "o",
                       "th", "e", "o", "ff", "i", "c"],
    "comfortable": ["Th", "e", "ch", "ai", "r", "s", "v", "er", "y", "c", "o",
                    "m", "f", "t", "a", "b", "le"],
    # Nothing writes the vowel of `-n't`, and the one /n/ the grid reads is
    # the one `know` starts with, so the `n` of `doesn't` writes nothing
    # either.
    "doesnt-know": ["H", "e", "d", "oe", "s", "", "kn", "ow"],
    "school": ["I", "g", "o", "t", "o", "s", "ch", "oo", "l", "e", "v", "r",
               "y", "d", "ay"],
    "market": ["Y", "e", "s", "t", "er", "d", "ay", "I", "w", "e", "n", "t",
               "o", "th", "e", "m", "a", "r", "k", "e"],
    "think-sheep": ["I", "th", "i", "n", "th", "e", "sh", "ee", "p", "are",
                    "i", "n", "th", "e", "f", "ie", "l", "d"],
    "turn-right-long": ["Y", "ou", "h", "a", "v", "t", "o", "t", "ur", "n",
                        "r", "igh", "t", "a", "th", "e", "c", "o", "r", "n",
                        "er"],
    "going-office": ["Y", "ou", "a", "r", "g", "o", "i", "ng", "t", "o", "th",
                     "e", "o", "ff", "i", "c"],
    "important": ["I", "t", "i", "s", "i", "m", "p", "o", "r", "n", "f", "or",
                  "m", "e"],
    "interesting": ["Th", "i", "s", "l", "e", "ss", "o", "n", "i", "s", "i",
                    "n", "t", "e", "s", "t", "i", "ng", "f", "or", "m", "e"],

    # The set held out from the fitting. `ten-minutes` is absent on purpose:
    # its digit is a character the table cannot write, so the join declines the
    # sentence before an answer is consulted, which is the limit it was chosen
    # to show.
    "weather-nice": ["Th", "e", "w", "ea", "th", "er", "i", "s", "n", "i",
                     "c", "t", "o", "d", "ay"],
    "friend-called": ["M", "y", "f", "r", "ie", "n", "c", "a", "ll", "m", "e",
                      "y", "e", "s", "t", "er", "d", "ay"],
    "book-table": ["Th", "e", "b", "oo", "k", "i", "s", "o", "n", "th", "e",
                   "t", "a", "b", "le"],
    "coffee-morning": ["I", "d", "r", "i", "n", "k", "o", "ff", "ee", "i", "n",
                       "th", "e", "m", "o", "r", "n", "i", "ng"],
    # `x` writes two sounds: the letter is repeated, once per sound.
    "six-boxes": ["Th", "e", "are", "s", "i", "x", "x", "b", "o", "x", "x",
                  "e", "s", "h", "e", "re"],
    # `u` of `use` and of `music` writes two sounds as well.
    "use-music": ["I", "u", "u", "s", "m", "u", "u", "s", "i", "t", "o", "r",
                  "e", "l", "a", "x", "x"],
    "question-easy": ["Th", "a", "q", "u", "e", "ti", "n", "w", "a", "s", "ea",
                      "s", "y"],
    "enough-time": ["W", "e", "d", "o", "n", "o", "h", "a", "v", "e", "n",
                    "ou", "gh", "t", "i", "m"],
    "thought-night": ["I", "th", "ough", "t", "a", "b", "ou", "t", "i", "l",
                      "a", "s", "n", "igh", "t"],
    "watch-match": ["I", "w", "a", "tch", "th", "e", "m", "a", "tch", "o",
                    "n", "S", "u", "n", "d", "ay"],
    "phone-pocket": ["M", "y", "ph", "o", "n", "i", "s", "i", "n", "m", "y",
                     "p", "o", "ck", "e"],
    "write-letter": ["I", "w", "a", "n", "o", "wr", "i", "t", "a", "l", "e",
                     "tt", "er"],
    "bridge-river": ["Th", "e", "b", "r", "i", "dge", "c", "r", "o", "ss", "e",
                     "s", "th", "e", "r", "i", "v", "er"],
    "station-far": ["Th", "e", "s", "t", "a", "ti", "n", "i", "s", "f", "a",
                    "r", "f", "m", "h", "e", "re"],
    "climb-wall": ["H", "e", "c", "n", "c", "l", "i", "mb", "th", "e", "w",
                   "a", "ll"],
    "listen-carefully": ["P", "l", "ea", "s", "l", "i", "s", "n", "c", "a",
                         "r", "f", "ll", "y"],
    "island-summer": ["W", "e", "v", "i", "s", "i", "th", "e", "i", "l", "a",
                      "n", "i", "n", "s", "u", "mm", "er"],
    "wont-come": ["I", "w", "o", "n", "b", "e", "a", "b", "le", "t", "o", "c",
                  "o", "m"],
    "long-station": ["I", "f", "y", "ou", "a", "v", "t", "i", "m", "t", "o",
                     "m", "o", "rr", "ow", "w", "e", "c", "ou", "w", "al", "t",
                     "o", "th", "e", "s", "t", "a", "ti", "o", "n", "a", "n",
                     "t", "a", "th", "e", "ear", "l", "y", "t", "r", "ai",
                     "n"],
}


# What each sentence ought to contain, in the alphabet the network itself uses.
# Written to be compared with the free decoding, which is where sounds go
# missing: two stops running in connected speech often leave one peak, and a
# sound absent from the model's grid is a sound no mark can ever be drawn on.
SOUNDS = {
    "think": "aɪ θ ɪ ŋ k j u ɝ ɹ aɪ t",
    "sink-broken": "ð ə s ɪ ŋ k ɪ z b ɹ oʊ k ə n",
    "sheep-field": "ð ə ʃ i p ɪ z ɪ n ð ə f i l d",
    "pear-tree": "aɪ p ɪ k t ə p ɛ ɹ f ɹ ə m ð ə t ɹ i",
    "turn-right": "t ɝ n ɹ aɪ t æ t ð ə k ɑ ɹ n ɝ",
    "walking-office": "aɪ æ m w ɑ k ɪ ŋ t u ð ə ɑ f ɪ s",
    "comfortable": "ð ə ʧ ɛ ɝ ɪ z v ɝ i k ə m f ɝ t ə b ə l",
    "doesnt-know": "h i d ə z ə n t n oʊ",
    "school": "aɪ g oʊ t u s k u l ɛ v ɹ i d eɪ",
    "market": "j ɛ s t ɝ d eɪ aɪ w ɛ n t t u ð ə m ɑ ɹ k ɪ t",
    "think-sheep": "aɪ θ ɪ ŋ k ð ə ʃ i p ɝ ɪ n ð ə f i l d",
    "turn-right-long": "j u h æ v t u t ɝ n ɹ aɪ t æ t ð ə k ɑ ɹ n ɝ",
    "going-office": "j u ɑ ɹ g oʊ ɪ ŋ t u ð ə ɑ f ɪ s",
    "important": "ɪ t ɪ z ɪ m p ɑ ɹ t ə n t f ɝ m i",
    "interesting": "ð ɪ s l ɛ s ə n ɪ z ɪ n t ɹ ɛ s t ɪ ŋ f ɝ m i",
}


# What each symbol sounds like, for the person being asked to judge by ear.
# The letters that write the sound are bracketed, because naming a sound in the
# abstract asks for phonetics where what is wanted is recognition.
#
# This is a legend, not a norm: it enters no comparison, no score and no app --
# it only lets a human read the label a network wrote. The words are American,
# like the model voice the bench renders with.
LIKE = {
    "ɑ": "l[o]ck", "æ": "c[a]t", "ə": "[a]bout", "aʊ": "h[ou]se",
    "aɪ": "t[i]me", "b": "[b]ig", "ʧ": "[ch]air", "d": "[d]og",
    "ð": "[th]is", "ɾ": "wa[t]er", "ɛ": "b[e]d", "ɝ": "b[ir]d",
    "eɪ": "d[ay]", "f": "[f]ish", "g": "[g]o", "h": "[h]at",
    "ɪ": "sh[i]p", "i": "sh[ee]p", "ʤ": "[j]ump", "k": "[k]ey",
    "l": "[l]ike", "m": "[m]an", "n": "[n]o", "ŋ": "si[ng]",
    "oʊ": "g[o]", "ɔɪ": "b[oy]", "p": "[p]en", "ɹ": "[r]ed",
    "s": "[s]un", "ʃ": "[sh]oe", "t": "[t]op", "θ": "[th]ink",
    "ʊ": "b[oo]k", "u": "f[oo]d", "v": "[v]an", "w": "[w]in",
    "j": "[y]es", "z": "[z]oo",
}


def like(symbol):
    """The symbol with a word that carries the sound, or bare if unknown."""
    example = LIKE.get(symbol)
    return f"{symbol} ({example})" if example else symbol


# Differences that are not a missing sound but the same sound written twice:
# the flapped `t` of American speech, and the unstressed vowels a transcription
# calls schwa and a network calls whatever is nearest. Held apart from the real
# gaps, which are sounds the grid does not have at all.
SAME = {"ɾ": "t", "ɪ": "ə", "ɛ": "ə", "ʌ": "ə", "ɔ": "ɑ", "ɚ": "ɝ", "i": "ɪ"}


# The ARPAbet a second inventory writes in, brought to the alphabet used above.
# Two inventories that name the same sounds differently would otherwise count
# every sound as missing.
ARPABET = {
    "AA": "ɑ", "AE": "æ", "AH": "ə", "AO": "ɔ", "AW": "aʊ", "AY": "aɪ",
    "B": "b", "CH": "ʧ", "D": "d", "DH": "ð", "EH": "ɛ", "ER": "ɝ",
    "EY": "eɪ", "F": "f", "G": "g", "HH": "h", "IH": "ɪ", "IY": "i",
    "JH": "ʤ", "K": "k", "L": "l", "M": "m", "N": "n", "NG": "ŋ",
    "OW": "oʊ", "OY": "ɔɪ", "P": "p", "R": "ɹ", "S": "s", "SH": "ʃ",
    "T": "t", "TH": "θ", "UH": "ʊ", "UW": "u", "V": "v", "W": "w",
    "Y": "j", "Z": "z", "ZH": "ʒ",
}


def alike(symbol):
    """The symbol reduced until it stops moving.

    Applied once, a chain like i -> ɪ -> ə leaves `i` and `ɪ` on different rungs
    and counts them as different sounds, which is the opposite of what the
    table is for.
    """
    symbol = ARPABET.get(symbol, symbol)
    seen = set()
    while symbol in SAME and symbol not in seen:
        seen.add(symbol)
        symbol = SAME[symbol]
    return symbol


def worksheet(material, candidate):
    """The grid to be annotated, one line per sentence, ready to be filled in.

    Deliberately shows the text and the decoded sounds and nothing else. Showing
    what a join produced would make the answer an edit of the join's output
    rather than a judgement about English spelling, and the two are not the same
    document.
    """
    import matrix
    renders = HERE / "out" / "renders" / candidate / "sentences"
    for slug, text in material:
        wav = renders / f"{slug}.wav"
        if not wav.is_file():
            print(f'    # "{slug}" — {wav.name} manque, phrase non rendue')
            continue
        sounds = matrix.grid(matrix.probabilities(wav))
        symbols = [matrix.symbols()[index] for index, _, _ in sounds]
        print(f'    # {text}')
        print(f'    #   {"  ".join(symbols)}')
        print(f'    "{slug}": [{", ".join(chr(34) * 2 for _ in symbols)}],')


def main(argv=None):
    import argparse
    import phrases

    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-j", "--jeu", default="heldout",
                        choices=("calibration", "heldout"))
    parser.add_argument("-c", "--candidate", default="eleven-us-eric")
    options = parser.parse_args(argv)
    worksheet({"calibration": phrases.CALIBRATION,
               "heldout": phrases.HELDOUT}[options.jeu], options.candidate)
    return 0


if __name__ == "__main__":
    import sys
    sys.exit(main())
