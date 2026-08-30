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

Two channels beside the marks go to no screen: where the learner freely said each
sound, and where each sound of the grid sits in each of the two recordings. They
are the shape the app already writes beside its own turns -- without them the
labelled set cannot be put through the same reading as a take recorded on the
phone, and a take read back wrong cannot be told from a take read back right.

Two channels of `TurnMarking` are left empty on purpose. Brick 8 now cuts the
syllables (`syllables.cut`), so their extents exist -- but a syllable the app
draws carries four more fields, and every one of them is brick 7 or brick 10:
which syllable the stress sits on, on each side, and the pitch of each. Filling
the extents and inventing the rest would put made-up stress on the screen, so
the channel stays empty until there is something true to put in it.
"""

import argparse
import json
import sys
from pathlib import Path

import join
import matrix
import overlap
import phrases
import placed

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

# Points of deviation below which the engine's own spread cannot be told from a
# fault -- `NOISE_BAND` in `MarkingColors.kt`, the bar the screen already draws
# to. A word verdict reuses it rather than bringing a second number to tune.
BAND = 5


def spans(text, sound):
    """The stretch of text a mark covers, or None when it covers none.

    A sound that holds no letter borrows a neighbour's; a sound that can borrow
    none marks the gutter, which no character range can express, so it is left
    out here and named separately.
    """
    where = sound.spots or sound.borrowed
    return (min(where), max(where) + 1) if where else None


def faulty(gaps, sounds):
    """The words no sound of which came through, each as its whole span.

    Binary where the per-sound ramp grades, because a word every sound of which
    is at fault is not a little off. It is also the only mark that reaches a
    word's silent letters, which carry no sound and so can never be tinted.

    No threshold of its own: the same band the sounds already answer to. Only
    sounds that were compared count, which here is all of them -- `read` gives
    up on a take whose two lists do not face each other one for one.
    """
    verdicts = {}
    for gap, sound in zip(gaps, sounds):
        if sound.word_at is None:
            continue
        clean = gap.value * POINTS <= BAND
        verdicts[sound.word_at] = verdicts.get(sound.word_at, True) and not clean
    return [{"start": start, "end": end}
            for (start, end), wrong in sorted(verdicts.items()) if wrong]


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

    words = faulty(gaps, sounds)
    # The one thing the grid cannot hold: it is the model's, so a sound the
    # learner added has no slot in it. The learner's own recording gets the same
    # reading the model's did -- which letters does each sound you produced
    # write -- and a sound no letter of any word can write belongs to no word.
    # `Added.kt` is the same in Kotlin.
    learner_cache = overlap.MATRICES / take / f"{model_slug}.npz"
    heard = matrix.grid(matrix.probabilities(learner, cache=learner_cache))
    added = placed.found(sounds, join.joined(learner, text, cache=learner_cache))

    # The same two readings again, but kept as times rather than spent on the
    # marks: where the learner freely said each sound, and where each sound of
    # the grid sits in both recordings. Nothing here feeds the screen -- it is
    # the shape the app already writes beside its own turns, and what says which
    # sound a line of the readout is talking about.
    scale = matrix.seconds_per_frame() * 1000
    stamp = lambda pair: [round(pair[0] * scale), round(pair[1] * scale)]
    model_grid = matrix.grid(matrix.probabilities(
        model, cache=overlap.MATRICES / f"sentences-{voice}" / f"{model_slug}.npz"))
    freely = [{"symbol": matrix.symbols()[index], "at": stamp((start, stop))}
              for index, start, stop in heard]
    grid = [{"symbol": sound.symbol, "letters": sound.letters,
             "borrowed": bool(sound.borrowed),
             "modelMs": stamp(model_grid[gap.rank][1:]),
             "saidMs": stamp(gap.span)}
            for gap, sound in zip(gaps, sounds)]
    phonemes, gutters = [], []
    # Where the last letter anyone claimed was. A gutter has none of its own, so
    # this running position is the only thing that says where in the phrase it
    # falls -- reading its own spots gave every gutter the front of the sentence.
    # Negative before the first letter: a gutter can precede all of them.
    claimed = -1
    for gap, sound in zip(gaps, sounds):
        stretch = spans(text, sound)
        if stretch is None:
            gutters.append({"symbol": sound.symbol,
                            "after": claimed,
                            "points": round(gap.value * POINTS, 2)})
            continue
        start, end = stretch
        phonemes.append({"start": start, "end": end,
                         "points": round(gap.value * POINTS, 2),
                         "symbol": sound.symbol,
                         "borrowed": bool(sound.borrowed)})
        # A borrowed letter belongs to the neighbour that took it, so it does not
        # move the anchor: the sound itself claimed nothing.
        if sound.spots:
            claimed = max(sound.spots)
    # The two audios these points were read off, by their bytes: a fixture
    # regenerated from a drifted render would otherwise change what the screen
    # draws without saying so.
    return {"text": text, "take": take, "model": model_slug, "voice": voice,
            "digests": {"model": matrix.fingerprint(model),
                        "take": matrix.fingerprint(learner)},
            "syllables": [], "phonemes": phonemes, "gutters": gutters,
            "words": words, "added": added, "freely": freely, "sounds": grid}


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
