#!/usr/bin/env python3
"""Listen to what was marked, and say whether it is a fault.

The labelled set answers for one sound per take -- the one it was built around
-- and says nothing of the thirteen others. So a take called a control is a
control on that sound alone, and reading its other marks as false alarms is a
guess. Nobody can settle it from the numbers: the only person who can is the one
who recorded the take, by listening.

This walks the marks of one take, plays the word as the model says it and then
as the take says it, and writes down the verdict:

    python3 review.py 15-right-clean -m turn-right-long

Verdicts land in `reviews/<take>.json`, versioned, because they are the same
kind of thing as `expected.py` -- written once by hand, regenerable by nothing.
"""

import argparse
import json
import subprocess
import sys
import tempfile
from pathlib import Path

import soundfile as sf

import join
import matrix
import overlap
import phrases

HERE = Path(__file__).resolve().parent
TAKES = HERE / "out" / "takes" / "set"
RENDERS = HERE / "out" / "renders"
REVIEWS = HERE / "reviews"

# The ramp of `MarkingColors.kt` ignores anything under 5 points, and points are
# the gap times a hundred. Marking what the screen would not draw would ask
# about something nobody will ever see.
NOISE = 0.05

# A phoneme lasts sixty milliseconds and is not judgeable alone; the word is.
PAD = 0.06


# Whichever of these the machine has. Any of them plays the file and none
# changes what is heard, so the order is about honesty rather than quality:
# `paplay` and `aplay` fail loudly when no sound card opens, `ffplay` reports
# success either way, which makes it useless as a witness and fine as a last
# resort.
PLAYERS = (["paplay"], ["aplay", "-q"], ["ffplay", "-nodisp", "-autoexit",
                                         "-loglevel", "quiet"])


def play(wav, low, high, chosen):
    """The stretch, on the speakers, through a file rather than a pipe.

    The player is settled on the first stretch actually played and remembered
    after that: asking a machine whether it can play, without playing, does not
    work -- every one of these fails on an empty file whether or not the sound
    card opens.

    Judging a mark without hearing it is worse than not judging it, since the
    verdict would be a guess written down as an answer. So a stretch that plays
    nowhere stops the session rather than letting it run.
    """
    audio, rate = sf.read(wav)
    first = max(0, int((low - PAD) * rate))
    last = min(len(audio), int((high + PAD) * rate))
    if last <= first:
        return chosen
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as handle:
        sf.write(handle.name, audio[first:last], rate)
        for command in ([chosen] if chosen else PLAYERS):
            done = subprocess.run(command + [handle.name],
                                  capture_output=True, check=False)
            if done.returncode == 0:
                Path(handle.name).unlink(missing_ok=True)
                return command
    Path(handle.name).unlink(missing_ok=True)
    raise SystemExit("aucun lecteur audio n'a joué (paplay, aplay, ffplay) — "
                     "juger une marque sans l'entendre serait une supposition "
                     "écrite comme une réponse")


def words(sounds):
    """Each sound's word, as the run of sounds that share it."""
    runs, start = [], 0
    for index in range(1, len(sounds) + 1):
        if index == len(sounds) or sounds[index].word is not sounds[start].word:
            runs.append((start, index))
            start = index
    return {index: run for run in runs for index in range(*run)}


def ask(prompt):
    """A verdict, or None on Ctrl+C -- never a traceback (cf. `cli-interactif`)."""
    try:
        return input(prompt).strip().lower()
    except (EOFError, KeyboardInterrupt):
        print()
        return None


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("take")
    parser.add_argument("-m", "--model", required=True)
    parser.add_argument("-c", "--candidate", default="eleven-us-eric")
    options = parser.parse_args(argv)

    text = dict(phrases.CALIBRATION + phrases.HELDOUT)[options.model]
    learner = TAKES / f"{options.take}.wav"
    model = RENDERS / options.candidate / "sentences" / f"{options.model}.wav"
    for wav in (learner, model):
        if not overlap.readable(wav):
            raise SystemExit(f"{wav} manque ou est trop court")

    gaps = overlap.sounds(model, learner, f"sentences-{options.candidate}",
                          options.take, options.model)
    sounds = join.joined(model, text)
    if len(gaps) != len(sounds):
        raise SystemExit(f"{len(gaps)} sons comparés contre {len(sounds)} "
                         "joints — la prise n'est pas lisible ainsi")

    speaker = None
    step = matrix.seconds_per_frame()
    grouped = words(sounds)
    marked = [index for index, gap in enumerate(gaps) if gap.value > NOISE]
    print(f"\n{text}\n{len(marked)} marques sur {len(gaps)} sons\n")
    print("  f = faute   c = correct   ? = je ne sais pas   "
          "r = réécouter   q = quitter\n")

    verdicts = {}
    for index in marked:
        gap, sound = gaps[index], sounds[index]
        low, high = grouped[index]
        letters = "".join(text[at] for at in sound.spots) or "—"
        # The word, on both sides: the model's stretch from its own grid, the
        # take's from where the alignment put those same sounds.
        model_span = (sounds[low].low, sounds[high - 1].high)
        frames = [gaps[i].span for i in range(low, high) if gaps[i].span]
        take_span = (min(s[0] for s in frames) * step,
                     max(s[1] for s in frames) * step) if frames else None
        while True:
            print(f"  /{sound.symbol}/ « {letters} » dans « {sound.word} »"
                  f"   {gap.value * 100:.1f} points")
            print("     modèle…", flush=True)
            speaker = play(model, *model_span, speaker)
            if take_span:
                print("     ta prise…", flush=True)
                speaker = play(learner, *take_span, speaker)
            answer = ask("     ? ")
            if answer is None or answer == "q":
                break
            if answer == "r":
                continue
            verdicts[str(index)] = {"symbol": sound.symbol, "letters": letters,
                                    "word": sound.word,
                                    "points": round(gap.value * 100, 2),
                                    "verdict": {"f": "faute", "c": "correct"}
                                    .get(answer, "incertain")}
            break
        if answer is None or answer == "q":
            break

    if not verdicts:
        print("\n  rien de jugé, rien d'écrit")
        return 0
    REVIEWS.mkdir(exist_ok=True)
    out = REVIEWS / f"{options.take}.json"
    out.write_text(json.dumps({"take": options.take, "model": options.model,
                               "voice": options.candidate, "text": text,
                               "marks": verdicts},
                              indent=2, ensure_ascii=False) + "\n",
                   encoding="utf-8")
    counts = {}
    for mark in verdicts.values():
        counts[mark["verdict"]] = counts.get(mark["verdict"], 0) + 1
    print(f"\n  {out}")
    print("  " + "   ".join(f"{name} {n}" for name, n in sorted(counts.items())))
    return 0


if __name__ == "__main__":
    sys.exit(main())
