#!/usr/bin/env python3
"""One analysed turn, in the shape the app draws.

The bench already measures everything a phoneme mark needs -- how far a sound
sits from the model (`overlap.py`), and which letters that sound covers
(`join.py`). What was missing is the pipe: the app's marking screen is fed by
`SampleTurns.kt`, whose own comment calls itself a stand-in for the engine. This
writes the real thing, so a take recorded by a learner can be looked at on the
real screen instead of invented numbers.

    python3 turn.py 01-sink --model think

The arithmetic stays in Python and the drawing is in Kotlin, which changes
nothing of what is seen: same audio, same weights, same grid, same marks.
Porting it to Kotlin is a separate job (`../TODO.md`).

Two channels of `TurnMarking` are left empty on purpose. Stress and melody both
travel through `syllables`, which needs bricks 7 and 8, and neither is written.
A turn from here therefore carries phoneme marks and nothing else.
"""

import argparse
import json
import sys
from pathlib import Path

import join
import matrix
import overlap
import phrases

HERE = Path(__file__).resolve().parent
TAKES = HERE / "out" / "takes" / "set"
RENDERS = HERE / "out" / "renders"

# The gap is a divergence between two spreads, in [0, 1]; the ramp of
# `MarkingColors.kt` reads points, ignores anything under 5 and saturates at 30.
# A hundred is the plainest transform there is, and it is deliberately not tuned
# to make the picture pretty: the labelled set puts controls at 0.003 and faults
# above 0.93, so this will saturate every fault and that is a fact to look at,
# not to hide. The scale, like the marking threshold itself, is still open.
POINTS = 100


def spans(text, sound):
    """The stretch of text a mark covers, or None when it covers none.

    A sound that holds no letter borrows a neighbour's; a sound that can borrow
    none marks the gutter, which no character range can express, so it is left
    out here and named separately.
    """
    where = sound.spots or sound.borrowed
    return (min(where), max(where) + 1) if where else None


def read(take, model_slug, voice):
    text = dict(phrases.CALIBRATION + phrases.HELDOUT)[model_slug]
    learner = TAKES / f"{take}.wav"
    model = RENDERS / voice / "sentences" / f"{model_slug}.wav"
    for wav in (learner, model):
        if not overlap.readable(wav):
            raise SystemExit(f"{wav} manque ou est trop court")

    # Both readings walk the same grid, decoded once from the model, so the two
    # lists face each other sound for sound.
    gaps = overlap.sounds(model, learner, f"sentences-{voice}", take,
                          model_slug)
    sounds = join.joined(model, text, cache=join.cache_for(voice, model_slug))
    if len(gaps) != len(sounds):
        # `overlap` drops a sound it cannot align or whose spread is mostly
        # silence; a mark cannot be placed on what was not compared.
        print(f"  {len(gaps)} sons comparés contre {len(sounds)} joints — "
              "l'écart n'est pas lisible sur cette prise", file=sys.stderr)
        return None

    phonemes, gutters = [], []
    for gap, sound in zip(gaps, sounds):
        stretch = spans(text, sound)
        if stretch is None:
            gutters.append({"symbol": sound.symbol,
                            "after": max(sound.spots or [0], default=0),
                            "points": round(gap.value * POINTS, 2)})
            continue
        start, end = stretch
        phonemes.append({"start": start, "end": end,
                         "points": round(gap.value * POINTS, 2),
                         "symbol": sound.symbol,
                         "borrowed": bool(sound.borrowed)})
    return {"text": text, "take": take, "model": model_slug, "voice": voice,
            "syllables": [], "phonemes": phonemes, "gutters": gutters}


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("take", help="une prise de out/takes/set, sans .wav")
    parser.add_argument("-m", "--model", required=True,
                        help="la phrase que la prise tente")
    parser.add_argument("-c", "--candidate", default="eleven-us-eric")
    parser.add_argument("-o", "--out", type=Path,
                        help="où écrire le JSON (défaut : la sortie standard)")
    options = parser.parse_args(argv)

    turn = read(options.take, options.model, options.candidate)
    if turn is None:
        return 1
    rendered = json.dumps(turn, indent=2, ensure_ascii=False)
    if options.out:
        options.out.parent.mkdir(parents=True, exist_ok=True)
        options.out.write_text(rendered + "\n", encoding="utf-8")
        print(f"  {options.out}")
    else:
        print(rendered)
    return 0


if __name__ == "__main__":
    sys.exit(main())
