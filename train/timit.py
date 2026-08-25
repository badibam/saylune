"""Reading the TIMIT corpus: utterances, phone targets, audio.

The purchased corpus lives outside git (tmp/TIMIT/, LDC licence — never
redistributed, never uploaded anywhere public). Files named .WAV are NIST
SPHERE, which libsndfile decodes transparently through soundfile.

Targets are folded from the 61 transcription symbols to the 39-class
inventory of Lee & Hon, rendered in IPA — exactly the vocabulary of the
current timit-ipa model, so the whole bench reads the fine-tuned network
unchanged. Silence symbols (closures, pauses, h#, epi) and the glottal
stop q are dropped from targets: silence belongs to the CTC blank.
"""

import json
import os
from pathlib import Path

# TIMIT symbol -> IPA of the timit-ipa vocabulary. Lee & Hon folding:
# aa+ao, ah+ax+ax-h, er+axr, hh+hv, ih+ix, l+el, m+em, n+en+nx, ng+eng,
# sh+zh, uw+ux; dx stays its own class (flap); q and silences are dropped.
FOLD = {
    "aa": "ɑ", "ao": "ɑ",
    "ae": "æ",
    "ah": "ə", "ax": "ə", "ax-h": "ə",
    "aw": "aʊ",
    "ay": "aɪ",
    "b": "b",
    "ch": "ʧ",  # single-codepoint ligature U+02A7, as in the vocabulary
    "d": "d",
    "dh": "ð",
    "dx": "ɾ", "nx": "ɾ",
    "eh": "ɛ",
    "er": "ɝ", "axr": "ɝ",
    "ey": "eɪ",
    "f": "f",
    "g": "g",
    "hh": "h", "hv": "h",
    "ih": "ɪ", "ix": "ɪ",
    "iy": "i",
    "jh": "ʤ",  # single-codepoint ligature U+02A4
    "k": "k",
    "l": "l", "el": "l",
    "m": "m", "em": "m",
    "n": "n", "en": "n",
    "ng": "ŋ", "eng": "ŋ",
    "ow": "oʊ",
    "oy": "ɔɪ",
    "p": "p",
    "r": "ɹ",
    "s": "s",
    "sh": "ʃ", "zh": "ʃ",
    "t": "t",
    "th": "θ",
    "uh": "ʊ",
    "uw": "u", "ux": "u",
    "v": "v",
    "w": "w",
    "y": "j",
    "z": "z",
}
DROPPED = {"q", "pau", "epi", "h#", "bcl", "dcl", "gcl", "pcl", "tcl", "kcl"}

ROOT = Path(__file__).resolve().parent.parent
CORPUS = ROOT / "tmp" / "TIMIT" / "lisa" / "data" / "timit" / "raw" / "TIMIT"


def utterances(split):
    """Yield (utterance id, wav path, phone sequence) for TRAIN or TEST.

    The two SA sentences are excluded, as is standard: every speaker reads
    the same two, which skews the phone distribution and leaks across the
    train/test split.
    """
    for phn in sorted((CORPUS / split).rglob("*.PHN")):
        if phn.stem.startswith("SA"):
            continue
        phones = []
        for line in phn.read_text(encoding="utf-8").splitlines():
            symbol = line.split()[2]
            if symbol in DROPPED:
                continue
            phones.append(FOLD[symbol])
        wav = phn.with_suffix(".WAV")
        uid = "/".join(wav.parts[-3:]).removesuffix(".WAV")
        yield uid, wav, phones


def vocabulary():
    """The symbol table of the timit-ipa model, from its cached vocab.json.

    Reusing it verbatim is what lets matrix.py read the fine-tuned network
    without change. HF_HOME must point at the bench cache (fail hard, as
    everywhere in the bench).
    """
    home = os.environ.get("HF_HOME")
    if not home:
        raise SystemExit("HF_HOME is not set — point it at the bench cache (tmp/hf)")
    hits = sorted(Path(home).glob(
        "hub/models--vitouphy--wav2vec2-xls-r-300m-timit-phoneme/snapshots/*/vocab.json"))
    if not hits:
        raise SystemExit("timit-ipa vocab.json absent from the cache — run bench/pull.py first")
    vocab = json.loads(hits[-1].read_text(encoding="utf-8"))
    missing = set(FOLD.values()) - set(vocab)
    if missing:
        raise SystemExit(f"folding produces symbols outside the vocabulary: {missing}")
    return vocab
