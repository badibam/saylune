#!/usr/bin/env python3
"""Which letters each sound of the grid ought to cover.

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

Where a sound is written by several letters the whole group is given (`igh` for
the vowel of `right`); where several sounds share one letter the letter is
repeated. Spaces are ignored in the comparison.
"""

# slug -> the letters of each sound of the grid, in order.
COVERED = {
    "think": ["I", "th", "i", "n", "k", "y", "ou", "are", "r", "igh", "t"],
    "sink-broken": ["Th", "e", "s", "i", "n", "k", "i", "s", "b", "r", "o",
                    "k", "e", "n"],
    "sheep-field": ["Th", "e", "sh", "ee", "p", "i", "s", "i", "n", "th", "e",
                    "f", "ie", "l", "d"],
    "pear-tree": ["I", "p", "i", "cked", "a", "p", "ea", "r", "fro", "m",
                  "th", "e", "t", "r", "ee"],
    "turn-right": ["T", "ur", "n", "r", "igh", "t", "at", "th", "e", "c", "o",
                   "r", "n", "er"],
    "walking-office": ["I", "a", "m", "w", "al", "k", "i", "ng", "t", "o",
                       "th", "e", "o", "ff", "i", "ce"],
    "comfortable": ["Th", "e", "ch", "ai", "r", "is", "v", "er", "y", "c",
                    "o", "m", "fo", "rt", "a", "b", "le"],
    "doesnt-know": ["H", "e", "d", "oe", "s", "n", "'tkn", "ow"],
    "school": ["I", "g", "o", "t", "o", "s", "ch", "oo", "l", "e", "ve", "r",
               "y", "d", "ay"],
    "market": ["Y", "e", "s", "t", "er", "d", "ay", "I", "w", "e", "nt", "t",
               "o", "th", "e", "m", "a", "r", "k", "et"],
    "think-sheep": ["I", "th", "i", "nk", "th", "e", "sh", "ee", "p", "are",
                    "i", "n", "th", "e", "f", "ie", "l", "d"],
    "turn-right-long": ["Y", "ou", "h", "a", "ve", "t", "o", "t", "ur", "n",
                        "r", "igh", "t", "at", "th", "e", "c", "o", "r", "n",
                        "er"],
    "going-office": ["Y", "ou", "a", "re", "g", "o", "i", "ng", "t", "o",
                     "th", "e", "o", "ff", "i", "ce"],
    "important": ["I", "t", "i", "s", "i", "m", "p", "o", "r", "tant", "f",
                  "or", "m", "e"],
    "interesting": ["Th", "i", "s", "l", "e", "ss", "o", "n", "i", "s", "i",
                    "n", "te", "re", "s", "t", "i", "ng", "f", "or", "m", "e"],
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
