#!/usr/bin/env python3
"""Bake one page a native speaker can open on their own machine and record into.

The legends shipping today were fetched from Commons and Freesound, one speaker per
file, and three of the thirty-eight have no free recording anywhere (`reference.py`).
Having them all said once by the same mouth fills those three and makes the set one
voice instead of a dozen. That is a recording session, and a session needs a booth.

**A file, not a page on the web.** The obvious form -- a hosted page, takes flowing
back on their own -- was built and does not work: the frame a published artifact runs
in does not delegate the microphone, so `getUserMedia` is refused in zero milliseconds
and no browser ever asks the speaker anything. Nothing written in the page changes
that. So the booth is one self-contained file: the sound table, the shipped recordings
inlined as data URIs, and a zip written in the browser at the end. What it costs is
that the takes come back by hand, once, instead of arriving as they are made.

**The sounds and their example words come from `reference.py`**, which already had to
name them to be checked at all -- read out of its source rather than imported, since
importing it drags in the audio stack that its listening pass needs and a page
generator does not. Grouping them by articulation family is this file's
own business, since what the speaker does with their mouth is a property of the family
and is said once per family rather than on every row. The two tables are checked
against each other rather than trusted: a symbol in one and not the other stops the
build, because the failure it would otherwise cause is a sound silently absent from
the session and noticed only when the recordings come back.

    python3 build.py        # -> ../out/booth.html, to send to whoever is recording
"""

import ast
import base64
import json
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
BENCH = HERE.parent
ROOT = BENCH.parent
SHIPPED = ROOT / "app" / "src" / "main" / "assets" / "reference"
OUT = BENCH / "out" / "booth.html"

# Articulation families in chart order, each with the one instruction that applies to
# everything in it. The examples spell out what the shipped Commons legends do, since
# that is what the speaker hears just before recording and what he is matching.
FAMILIES = (
    ("Vowels", "Hold it on its own, about two seconds.",
     ("i", "ɪ", "ɛ", "æ", "ə", "ɝ", "ɑ", "ʊ", "u")),
    ("Diphthongs", "One clean glide, once through. Don't hold it.",
     ("aɪ", "aʊ", "eɪ", "oʊ", "ɔɪ")),
    ("Plosives", "Frame it between two a's, twice through — pa, apa.",
     ("p", "b", "t", "d", "k", "g")),
    ("Affricates", "Frame it between two a's, twice through — cha, acha.",
     ("ʧ", "ʤ")),
    ("Fricatives", "Hold it, then frame it — sss, assa.",
     ("f", "v", "θ", "ð", "s", "z", "ʃ", "h")),
    ("Nasals", "Hold it, then frame it — mmm, amma.",
     ("m", "n", "ŋ")),
    ("Approximants", "Hold it, then frame it — lll, alla.",
     ("l", "ɹ", "j", "w")),
    ("Tap", "The quick flap in the middle of better — better, atta, utter.",
     ("ɾ",)),
)


def examples():
    """`EXAMPLES` out of `reference.py`, by reading the module rather than running it."""
    source = (BENCH / "reference.py").read_text(encoding="utf-8")
    for node in ast.parse(source).body:
        targets = getattr(node, "targets", [])
        if targets and getattr(targets[0], "id", None) == "EXAMPLES":
            return ast.literal_eval(node.value)
    raise SystemExit("reference.py no longer declares EXAMPLES")


def table():
    """Every sound, in session order, with the recording that ships for it today."""
    known = examples()
    grouped = [symbol for _, _, symbols in FAMILIES for symbol in symbols]
    missing = set(known) - set(grouped)
    extra = set(grouped) - set(known)
    if missing or extra:
        raise SystemExit(
            f"the families and reference.py disagree — "
            f"absent here: {sorted(missing)}, unknown there: {sorted(extra)}")

    sounds = []
    for family, how, symbols in FAMILIES:
        for symbol in symbols:
            word, carrier = known[symbol]
            at = word.index(carrier)
            ogg = SHIPPED / f"{symbol}.ogg"
            sounds.append({
                "id": f"s{len(sounds):02d}",
                "symbol": symbol,
                "family": family,
                "how": how,
                "word": word,
                "carrier": [at, at + len(carrier)],
                "reference": (base64.b64encode(ogg.read_bytes()).decode()
                              if ogg.is_file() else None),
            })
    return sounds


def main():
    sounds = table()
    page = (HERE / "page.template.html").read_text(encoding="utf-8")
    if "/*__SOUNDS__*/" not in page:
        raise SystemExit("the template has no place to put the sounds")
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(
        page.replace("/*__SOUNDS__*/", json.dumps(sounds, ensure_ascii=False)),
        encoding="utf-8")

    silent = [sound["symbol"] for sound in sounds if not sound["reference"]]
    print(f"{len(sounds)} sounds, {len(silent)} with nothing to play ({' '.join(silent)})")
    print(f"{OUT} — {OUT.stat().st_size / 1024:.0f} KB")
    return 0


if __name__ == "__main__":
    sys.exit(main())
