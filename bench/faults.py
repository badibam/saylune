#!/usr/bin/env python3
"""Does the gap find the faults of the test set without marking the controls?

The measure that decides the embedded track (`docs/design/embedded-analysis.md`,
third question). The same takes that separated the remote services are read
again, this time by two matrices and no service at all, so the result compares
directly to `speechace.md`.

A fault is one sound, so the reading is anchored: each case names the sound of
the model's grid where the fault was made, and the control of that same sound is
read against the same model. What is compared is a labelled sound to a labelled
sound, never a sentence average.

The worst sound of the whole utterance is printed beside it, and the two do not
say the same thing. A learner saying "you're" where the model says "you are"
diverges hugely and legitimately, on a clean take as much as on a faulty one --
which is why the sentence maximum is not a marking policy.

Blocks A, C and D only. The grammar block asks a different question, and prosody
and stress are other scales -- bricks 7 and 10, not this one.
"""

import argparse
import statistics
import sys
from pathlib import Path

import overlap
import synth

# Above this median gap, the two recordings are not two readings of the same
# text: the grid has been forced onto speech that does not contain it, and every
# sound of the utterance is off at once. That is brick 11's job, and a take it
# catches is out of the comparison rather than a fault detected. Every take that
# aligns at all sits at 0.022 or below.
BROKEN_MEDIAN = 0.2

HERE = Path(__file__).resolve().parent
TAKES = HERE / "out" / "takes" / "set"
RENDERS = HERE / "out" / "renders"

# One sound, spelled differently from one inventory to the next. A case names
# the sound it turns on; the bench resolves it against the model in hand.
ALIASES = {"iː": ("iː", "i", "IY", "iy"), "ɹ": ("ɹ", "r", "R"),
           "θ": ("θ", "TH", "th"), "p": ("p", "P"), "ŋ": ("ŋ", "NG", "ng")}

# take, the correct text it attempts, role, the model sound it turns on
# (symbol and which occurrence of it), and what was done to that sound.
# A control is a take of the same block, same voice, same day, read against the
# same model: a gap on it is noise that would be read everywhere else.
CASES = (
    ("01-sink", "think", "faute", ("θ", 0), "/θ/ dit /s/, à mi-chemin"),
    ("17-sink-full", "think", "franche", ("θ", 0), "/s/ franc"),
    ("06-ship-sheep", "sheep-field", "faute", ("iː", 0), "/iː/ raccourci"),
    ("16-ship-lax", "sheep-field", "franche", ("iː", 0), "/iː/ relâché"),
    ("07-bear-pear", "pear-tree", "faute", ("p", 1), "/p/ voisé en /b/"),
    ("08-light-right", "turn-right", "faute", ("ɹ", 0), "/r/ dit /l/"),
    ("09-walkin", "walking-office", "faute", ("ŋ", 0), "/ŋ/ dit /n/, à mi-chemin"),
    ("18-walkin-full", "walking-office", "franche", ("ŋ", 0), "/n/ franc"),
    ("10-comfortable", "comfortable", "faute", None, "syllabe insérée"),
    ("04-th-franc", "think", "témoin", ("θ", 0), "/θ/ propre"),
    ("13-field-clean", "think-sheep", "témoin", ("θ", 0), "/θ/ propre"),
    ("13-field-clean", "think-sheep", "témoin", ("iː", 0), "/iː/ propre"),
    ("14-pear-clean", "pear-tree", "témoin", ("p", 1), "/p/ propre"),
    ("15-right-clean", "turn-right-long", "témoin", ("ɹ", 0), "/r/ propre"),
    ("05-sink-broken", "sink-broken", "témoin", None, "rien à voir"),
)


def anchored(read, anchor):
    """The gap on the named occurrence of a sound in the model's grid."""
    symbol, occurrence = anchor
    spellings = ALIASES.get(symbol, (symbol,))
    found = [gap for gap in read if gap.symbol in spellings]
    return found[occurrence] if occurrence < len(found) else None


def run(model_name, show):
    print(f"\n=== modèle {model_name}")
    print(f"    {'prise':<18}{'rôle':<10}{'son':<7}{'écart':>8}"
          f"{'médian':>9}{'pire phrase':>13}  {'sur':<9}la faute")

    anchors = {}
    for take, slug, role, anchor, where in CASES:
        wav = TAKES / f"{take}.wav"
        model_wav = RENDERS / model_name / "sentences" / f"{slug}.wav"
        if not overlap.readable(wav) or not overlap.readable(model_wav):
            print(f"    {take:<18}{role:<10}  absent")
            continue
        read = overlap.sounds(model_wav, wav, f"sentences-{model_name}",
                              f"set-{take}", slug)
        if not read:
            print(f"    {take:<18}{role:<10}  rien à lire")
            continue

        loudest = max(read)
        median = statistics.median(gap.value for gap in read)
        gap = anchored(read, anchor) if anchor else None
        if gap is not None and median <= BROKEN_MEDIAN:
            anchors.setdefault(role, []).append((gap.value, take))
        shown = f"{gap.value:>8.3f}" if gap is not None else f"{'--':>8}"
        name = f"/{anchor[0]}/" if anchor else "--"
        broken = "  hors comparaison, décodage divergent" if median > BROKEN_MEDIAN else ""
        print(f"    {take:<18}{role:<10}{name:<7}{shown}{median:>9.3f}"
              f"{loudest.value:>13.3f}  /{loudest.symbol + '/':<8}{where}{broken}")
        if show:
            print(f"    {'':<28}" + " ".join(f"{g.symbol}:{g.value:.2f}"
                                             for g in read))

    controls = anchors.get("témoin", [])
    faults = [row for role in ("faute", "franche")
              for row in anchors.get(role, [])]
    if not controls or not faults:
        return

    # A fault below the worst control is a fault this reading did not see. It is
    # a miss, not an overlap: what the band has to answer is whether anything
    # sits *between* the controls and the faults that were seen.
    highest, noisiest = max(controls)
    seen = [row for row in faults if row[0] > highest]
    missed = [take for value, take in faults if value <= highest]
    print(f"\n    sur le son étiqueté : pire témoin {highest:.3f} ({noisiest})")
    if seen:
        lowest, quietest = min(seen)
        print(f"    plus faible faute vue {lowest:.3f} ({quietest})"
              f"   ->   bande vide de {lowest - highest:.3f}")
    print(f"    {len(seen)} fautes sur {len(faults)} vues"
          + (f", manquées : {', '.join(missed)}" if missed else ""))


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-m", "--model", action="append", default=None,
                        help="repeatable; default reads both accents")
    parser.add_argument("-v", "--verbose", action="store_true")
    args = parser.parse_args(argv)

    for name in args.model or ["eleven-gb-daniel", "eleven-us-eric"]:
        if name not in synth.BY_NAME:
            raise SystemExit(f"Voix inconnue {name!r}")
        run(name, args.verbose)
    return 0


if __name__ == "__main__":
    sys.exit(main())
