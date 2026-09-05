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

The syllable channel is filled for real now. Brick 8 cuts the syllables
(`syllables.cut`), brick 10 reads a pitch on each, and brick 7 flags which
syllable the stress sits on, on each side: the frozen probe's parts, read on
the **nucleus** span -- the model's own segment, and where that same sound sits
in the take. A word is eligible only when the model's elected syllable leads
the runner-up by the measured bar (`probe.BAR`, `../docs/analysis.md`): a word
the model does not stress clearly cannot be got wrong, and no mark is drawn on
it.
"""

import argparse
import json
import sys
from pathlib import Path

import join
import matrix
import melody
import overlap
import phrases
import placed
import probe
import syllables

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
    step_s = matrix.seconds_per_frame()
    model_pitch, model_step = melody.track(model)
    take_pitch, take_step = melody.track(learner)
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
    # Brick 7: which syllable carries the stress, on each side, by the frozen
    # probe. Read on the nucleus alone, not the whole syllable -- the model's
    # own segment on its side, and where that same sound sits in the take on
    # the other, which the aligned montage already knows. A word is eligible
    # when the model's elected syllable leads the runner-up by BAR, and a word
    # of one syllable or a function word has no stress choice to be wrong
    # about.
    tools = probe.frozen()
    layer = int(tools[4])
    model_hidden = probe.hidden(model, layer)
    take_hidden = probe.hidden(learner, layer)

    def stress_flags():
        out = {}
        runs = []
        for piece in cuts:
            if runs and runs[-1][0].word == piece.word:
                runs[-1].append(piece)
            else:
                runs.append([piece])
        for group in runs:
            word = (group[0].word or "").strip(".,!?'").lower()
            if word in syllables.FUNCTION or len(group) < 2:
                continue
            model_places, take_places = [], []
            for piece in group:
                low, high = piece.sounds
                heads = matrix.nuclei([s.symbol for s in sounds[low:high]])
                if not heads or low + heads[0] not in placed_at:
                    model_places = []
                    break
                rank = low + heads[0]
                model_places.append(model_grid[rank][1:])
                take_places.append(placed_at[rank][1])
            if not model_places:
                continue
            theirs = probe.parts(model_hidden, model_places, tools)
            mine = probe.parts(take_hidden, take_places, tools)
            if theirs is None or mine is None:
                continue
            ranked = sorted(theirs)[::-1]
            if ranked[0] - ranked[1] < probe.BAR:
                continue
            model_at = max(range(len(theirs)), key=theirs.__getitem__)
            take_at = max(range(len(mine)), key=mine.__getitem__)
            for place, piece in enumerate(group):
                out[piece] = (place == model_at, place == take_at)
        return out

    # Brick 10: one pitch per syllable, on either side, in semitones centred on
    # each side's own middle. The learner is read on the **model's** syllables --
    # the model counts them, and the aligned montage already says where each of
    # its sounds sits in the learner's recording. `Pitch.kt` is the same in
    # Kotlin, and `Syllables.kt` cuts the same syllables.
    placed_at = {gap.rank: (model_grid[gap.rank][1:], gap.span) for gap in gaps}
    cuts, mine, theirs = [], [], []
    for piece in syllables.cut(sounds):
        if not piece.spots:
            continue
        held = [placed_at[rank] for rank in range(*piece.sounds)
                if rank in placed_at]
        if not held:
            continue
        cuts.append(piece)
        mine.append(melody.over(model_pitch, model_step,
                                min(h[0][0] for h in held) * step_s,
                                max(h[0][1] for h in held) * step_s))
        theirs.append(melody.over(take_pitch, take_step,
                                  min(h[1][0] for h in held) * step_s,
                                  max(h[1][1] for h in held) * step_s))
    flags = stress_flags()
    model_line = melody.centred(mine)
    take_line = melody.centred(theirs)
    tuned = [{"start": min(piece.spots), "end": max(piece.spots) + 1,
              "modelPitch": round(here, 3),
              "learnerPitch": None if there is None else round(there, 3),
              "modelStressed": flags.get(piece, (False, False))[0],
              "learnerStressed": flags.get(piece, (False, False))[1]}
             for piece, here, there in zip(cuts, model_line, take_line)
             if here is not None]

    # The two audios these points were read off, by their bytes: a fixture
    # regenerated from a drifted render would otherwise change what the screen
    # draws without saying so.
    return {"text": text, "take": take, "model": model_slug, "voice": voice,
            "digests": {"model": matrix.fingerprint(model),
                        "take": matrix.fingerprint(learner)},
            "syllables": tuned, "phonemes": phonemes, "gutters": gutters,
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
