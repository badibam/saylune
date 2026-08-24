#!/usr/bin/env python3
"""How far our own anchoring sits from the one the provider hands over.

The verdict `letters.py` waits on. If a letter aligner running on our side puts
the characters where ElevenLabs puts them, the provider's alignment stops being
consumed, the app stops depending on the one engine that returns it in plain
REST, and every TTS becomes a candidate (`../docs/reference.md`).

What is compared is not a number against a number. The provider gives each
character a stretch of audio; a CTC trellis gives it the instant the network
pointed at it, the blank owning everything in between. So a character is well
anchored when its instant falls inside the stretch, and a word is well anchored
when its edges land where the provider's edges are -- which is the one that
decides, since a mark is drawn on a word, never on a letter.

    ELEVENLABS_KEY=... python3 anchor.py
"""

import argparse
import json
import statistics
import sys
from pathlib import Path

import letters
import phrases
import synth

HERE = Path(__file__).resolve().parent
RENDERS = HERE / "out" / "renders"
MATRICES = HERE / "out" / "matrices" / letters.SLUG


def provided(path, text):
    """The provider's timing, one span per character of the text we sent.

    The normalised alignment is deliberately not read: it describes what the
    engine decided to say, and what a mark is drawn on is what the user sees.
    A text whose characters do not survive the trip is out of this measurement
    rather than quietly patched -- `25` is two characters and four syllables.
    """
    payload = json.loads(path.read_text(encoding="utf-8"))
    alignment = payload["alignment"]
    characters = alignment["characters"]
    if "".join(characters) != text:
        return None
    return list(zip(alignment["character_start_times_seconds"],
                    alignment["character_end_times_seconds"]))


def words(text):
    """The spans of the text that a mark can be drawn on, as character ranges."""
    spans, start = [], None
    for position, character in enumerate(text):
        if character.isspace():
            if start is not None:
                spans.append((start, position))
                start = None
        elif start is None:
            start = position
    if start is not None:
        spans.append((start, len(text)))
    return spans


def edges(anchored, span):
    """Where a stretch of text begins and ends, from whatever carries a time."""
    timed = [found for found in anchored[span[0]:span[1]] if found is not None]
    if not timed:
        return None
    return timed[0][0], timed[-1][1]


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-c", "--candidate", default="eleven-us-eric",
                        help="one of: " + ", ".join(
                            name for name, candidate in synth.BY_NAME.items()
                            if candidate.provider == "elevenlabs"))
    parser.add_argument("-v", "--verbose", action="store_true")
    args = parser.parse_args(argv)

    candidate = synth.BY_NAME.get(args.candidate)
    if candidate is None or candidate.provider != "elevenlabs":
        raise SystemExit(f"{args.candidate!r} ne rend pas d'horodatage — "
                         "la mesure n'a rien contre quoi se tenir")

    inside, instants, starts, ends, skipped = [], [], [], [], []
    # The first character of an utterance and the last are the two places where
    # the two sides are not describing the same thing: the provider stretches
    # them over the leading and trailing silence of the file, the trellis puts
    # the letter where the voice actually starts and stops. Held apart rather
    # than averaged in, since no mark is ever drawn on silence.
    edging = []
    for slug, text in phrases.CALIBRATION:
        # A text the network cannot spell is out of the measurement, not
        # averaged into it: what it would report is the cost of a text that
        # should have been normalised before it was ever synthesised.
        cannot = letters.unspellable(text)
        if cannot:
            skipped.append(f"{slug} ({''.join(cannot)})")
            continue
        wav = RENDERS / candidate.name / "sentences" / f"{slug}.wav"
        synth.render(text, candidate, wav)
        theirs = provided(synth.alignment_path(wav), text)
        if theirs is None:
            skipped.append(slug)
            continue
        ours = letters.anchors(wav, text,
                               cache=MATRICES / candidate.name / f"{slug}.npz")

        for found, (start, stop) in zip(ours, theirs):
            if found is None:
                continue
            inside.append(start <= found[0] <= stop)
            instants.append(abs(found[0] - start))
        spans = words(text)
        for position, span in enumerate(spans):
            ours_edges = edges(ours, span)
            theirs_edges = edges(theirs, span)
            if ours_edges is None or theirs_edges is None:
                continue
            opening = ours_edges[0] - theirs_edges[0]
            closing = ours_edges[1] - theirs_edges[1]
            (edging if position == 0 else starts).append(abs(opening))
            (edging if position == len(spans) - 1
             else ends).append(abs(closing))
            if args.verbose:
                print(f"  {text[span[0]:span[1]]:<14}"
                      f"{opening * 1000:>8.0f}{closing * 1000:>8.0f}  ms")

    if not starts:
        raise SystemExit("aucun énoncé mesurable")

    print(f"\n=== {candidate.name}   {len(starts)} mots, "
          f"{len(instants)} caractères")
    print(f"    instant dans la plage du fournisseur : "
          f"{100 * sum(inside) / len(inside):.0f} %")
    for name, values in (("écart d'instant", instants),
                         ("début de mot", starts), ("fin de mot", ends),
                         ("bords de l'énoncé", edging)):
        print(f"    {name:<22}médian {1000 * statistics.median(values):6.0f} ms"
              f"   pire {1000 * max(values):6.0f} ms")
    if skipped:
        # Named rather than counted: what it costs is a phrase out of the field.
        print("\n    hors mesure : " + ", ".join(skipped))
    return 0


if __name__ == "__main__":
    sys.exit(main())
