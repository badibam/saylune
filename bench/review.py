#!/usr/bin/env python3
"""What the analysis claims about one take, put to the person who recorded it.

The labelled set answers for one sound per take -- the one it was built around
-- and says nothing of the other thirteen. So a take called a control is a
control on that sound alone, and reading its other marks as false alarms is a
guess. No number settles it.

What is asked here is not whether a sound was well said, which would need a norm
this montage refuses to hold. It is whether the claim **holds together**: the
model says this, the network heard that, here is the word on both sides -- does
that read as one thing?

    python3 review.py 15-right-clean -m turn-right-long

Verdicts land in `reviews/<take>.json`, versioned, because they are the same
matter as `expected.py`: written once by hand, regenerable by nothing.
"""

import argparse
import json
import os
import select
import subprocess
import sys
import tempfile
import termios
import tty
from pathlib import Path

import atomic

import soundfile as sf

import expected
import join
import matrix
import overlap
import phrases

HERE = Path(__file__).resolve().parent
TAKES = HERE / "out" / "takes" / "set"
RENDERS = HERE / "out" / "renders"
# Through `overlap`, so the path carries the model's name: a matrix filed
# where the reading is not named is read back under whatever model runs next.
MATRICES = overlap.MATRICES
REVIEWS = HERE / "reviews"

# The ramp of `MarkingColors.kt` ignores anything under 5 points, and points are
# the gap times a hundred: asking about what the screen would not draw would ask
# about something nobody will ever see.
NOISE = 0.05

# A phoneme lasts sixty milliseconds and is not judgeable alone; the word is,
# with a little air on either side so it is heard arriving and leaving rather
# than cut out of the sentence. Kept short on purpose: a wide margin drags in
# the neighbouring word, and then what is being judged is no longer this one.
# `--pad` moves it, because how much air reads as natural is a thing to hear.
PAD = 0.15

# How much of a spread to show. Past three the tail is noise, and the claim is
# in the head of it.
SHOWN = 3

# Any of these plays the file and none changes what is heard, so the order is
# about honesty: `paplay` and `aplay` fail loudly when no sound card opens,
# `ffplay` reports success either way -- a poor witness, an acceptable last one.
PLAYERS = (["paplay"], ["aplay", "-q"], ["ffplay", "-nodisp", "-autoexit",
                                         "-loglevel", "quiet"])


# Half speed makes a vowel easy to place and is not free: a stretched word is a
# processed signal, and the stretcher decides some of what is heard. `sox tempo`
# keeps the pitch, which is the whole point -- writing the file at half the
# sample rate would drop the formants an octave and turn `ɪ` into something
# nobody said. The unstretched word stays the reference: `--slow 1` gives it.
STRETCHERS = (["sox", "{in}", "{out}", "tempo", "-s", "{rate}"],
              ["ffmpeg", "-y", "-loglevel", "quiet", "-i", "{in}",
               "-filter:a", "{chain}", "{out}"])


def chained(rate):
    """`atempo` refuses anything below half speed, so it is applied twice.

    Stages multiply, so a third of speed is a half followed by two thirds. The
    same filter run twice, not another one: what is heard is what the tool
    would have done had it accepted the number.
    """
    stages = []
    while rate < 0.5:
        stages.append(0.5)
        rate /= 0.5
    stages.append(rate)
    return ",".join(f"atempo={stage:g}" for stage in stages)


def stretched(path, rate):
    """The same word, slower, at the same pitch. In place, through a tool."""
    if rate == 1:
        return path
    out = path.replace(".wav", "-slow.wav")
    for command in STRETCHERS:
        filled = [part.format(**{"in": path, "out": out, "rate": rate,
                                 "chain": chained(rate)})
                  for part in command]
        if subprocess.run(filled, capture_output=True,
                          check=False).returncode == 0:
            Path(path).unlink(missing_ok=True)
            return out
    raise SystemExit(f"ralentir demande sox ou ffmpeg, qui manquent — "
                     f"jouer à vitesse pleine sans le dire serait mentir "
                     f"sur ce qui est entendu")


def play(wav, low, high, chosen, pad=PAD, slow=1.0):
    """The stretch, on the speakers, through a file rather than a pipe.

    The player is settled on the first stretch actually played: asking a machine
    whether it can play, without playing, does not work -- every one of these
    fails on an empty file whether or not the sound card opens. And a stretch
    that plays nowhere is fatal, since a verdict given without hearing would be
    a guess written into the file as an answer.
    """
    audio, rate = sf.read(wav)
    first = max(0, int((low - pad) * rate))
    last = min(len(audio), int((high + pad) * rate))
    if last <= first:
        return chosen
    with tempfile.NamedTemporaryFile(suffix=".wav", delete=False) as handle:
        sf.write(handle.name, audio[first:last], rate)
    heard = stretched(handle.name, slow)
    for command in ([chosen] if chosen else PLAYERS):
        done = subprocess.run(command + [heard], capture_output=True,
                              check=False)
        if done.returncode == 0:
            Path(heard).unlink(missing_ok=True)
            return command
    Path(heard).unlink(missing_ok=True)
    raise SystemExit("aucun lecteur audio n'a joué (paplay, aplay, ffplay) — "
                     "juger une marque sans l'entendre serait une supposition "
                     "écrite comme une réponse")


def heard(probabilities, span):
    """The head of what the network heard over a stretch, as it writes it.

    Naming the sound produced is the least reliable thing an acoustic machine
    renders, and the analysis deliberately does not depend on it. It is shown
    here and nowhere else, because a claim one cannot read is a claim one cannot
    contradict.
    """
    spread, _ = overlap.spread(probabilities, span)
    if spread is None:
        return "—"
    # `spread` is indexed over the spoken columns alone, silence and notation
    # dropped, so a position in it is not a position in the alphabet.
    names = [matrix.symbols()[column] for column in matrix.spoken()]
    order = sorted(range(len(spread)), key=lambda i: -spread[i])[:SHOWN]
    return "  ".join(f"{names[i]} {spread[i]:.2f}"
                     for i in order if spread[i] >= 0.01)


def shown(text, spots):
    """The sentence with the marked letters raised, so the claim has a place."""
    if not spots:
        return text
    low, high = min(spots), max(spots) + 1
    return f"{text[:low]}[{text[low:high]}]{text[high:]}"


def ask(prompt):
    """A verdict on one key, or None on Ctrl+C -- never a traceback.

    Every answer here is one character, and requiring Enter after each doubles
    the gesture on a listening pass that is nothing but gestures. Raw mode has
    to hand back what the cooked terminal gave for free: Ctrl+C and Ctrl+D are
    bytes rather than exceptions, and the key is echoed, because a session one
    cannot reread afterwards is a session one cannot check.

    A stdin that is not a terminal -- a pipe, a test -- reads a line instead.
    """
    print(prompt, end="", flush=True)
    if not sys.stdin.isatty():
        try:
            return input().strip().lower()
        except (EOFError, KeyboardInterrupt):
            print()
            return None
    # The descriptor, never `sys.stdin`: a buffered text stream asked for one
    # character takes a chunk of the descriptor with it, and what it holds back
    # is then invisible to `select` and answers the next question.
    handle = sys.stdin.fileno()
    settings = termios.tcgetattr(handle)
    try:
        tty.setraw(handle)
        key = os.read(handle, 1).decode("utf-8", "replace")
        if key == "\x1b":
            # An arrow sends three bytes and the last of them is a letter, so
            # read as three answers `Escape [ A` casts a vote. Drained whole,
            # and answered with nothing.
            while select.select([handle], [], [], 0)[0]:
                os.read(handle, 1)
            key = ""
    finally:
        termios.tcsetattr(handle, termios.TCSADRAIN, settings)
    if key in ("\x03", "\x04"):
        print()
        return None
    print(key)
    return key.strip().lower()


VERDICTS = {"o": "cohérent", "n": "incohérent", "?": "incertain"}


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("take")
    parser.add_argument("-m", "--model", required=True)
    parser.add_argument("-c", "--candidate", default="eleven-us-eric")
    parser.add_argument("-p", "--pad", type=float, default=PAD,
                        help="l'air autour du mot, en secondes "
                             f"(défaut {PAD})")
    parser.add_argument("-s", "--slow", type=float, default=1.0,
                        help="la vitesse de lecture, hauteur conservée "
                             "(1 = telle quelle, 0.5 = deux fois plus lent)")
    options = parser.parse_args(argv)

    text = dict(phrases.CALIBRATION + phrases.HELDOUT)[options.model]
    learner = TAKES / f"{options.take}.wav"
    model = RENDERS / options.candidate / "sentences" / f"{options.model}.wav"
    for wav in (learner, model):
        if not overlap.readable(wav):
            raise SystemExit(f"{wav} manque ou est trop court")

    tag = f"sentences-{options.candidate}"
    gaps = overlap.sounds(model, learner, tag, options.take, options.model)
    sounds = join.joined(model, text,
                         cache=join.cache_for(options.candidate, options.model))
    if len(gaps) != len(sounds):
        raise SystemExit(f"{len(gaps)} sons comparés contre {len(sounds)} "
                         "joints — la prise n'est pas lisible ainsi")

    # The same two matrices `overlap` just read, from the same caches: what it
    # reduced to one number is shown here in full.
    model_spread = matrix.probabilities(
        model, cache=MATRICES / tag / f"{options.model}.npz")
    take_spread = matrix.probabilities(
        learner, cache=MATRICES / options.take / f"{options.model}.npz")
    grid = matrix.grid(model_spread)

    step = matrix.seconds_per_frame()
    grouped = {index: run for run in join.runs(sounds)
               for index in range(*run)}
    marked = [index for index, gap in enumerate(gaps) if gap.value > NOISE]
    print(f"\n{text}\n{len(marked)} marques sur {len(gaps)} sons\n")
    print("  o = cohérent   n = incohérent   ? = incertain   "
          "r = réécouter   q = quitter\n")

    verdicts, speaker = {}, None
    for index in marked:
        gap, sound = gaps[index], sounds[index]
        low, high = grouped[index]
        model_span = (sounds[low].low, sounds[high - 1].high)
        frames = [gaps[i].span for i in range(low, high) if gaps[i].span]
        take_span = (min(s[0] for s in frames) * step,
                     max(s[1] for s in frames) * step) if frames else None
        _, start, stop = grid[index]
        while True:
            print(f"  {shown(text, sound.spots)}")
            print(f"     le modèle dit   {heard(model_spread, (start, stop))}")
            print(f"     ta prise dit    {heard(take_spread, gap.span)}")
            print(f"     mot « {sound.word} », son {expected.like(sound.symbol)}, "
                  f"{gap.value * 100:.1f} points")
            print("     modèle…", flush=True)
            speaker = play(model, *model_span, speaker, options.pad,
                           options.slow)
            if take_span:
                print("     ta prise…", flush=True)
                speaker = play(learner, *take_span, speaker, options.pad,
                               options.slow)
            answer = ask("     ? ")
            if answer is None or answer == "q" or answer in VERDICTS:
                break
        if answer is None or answer == "q":
            break
        verdicts[str(index)] = {
            "symbol": sound.symbol, "word": sound.word,
            "letters": "".join(text[at] for at in sound.spots),
            "points": round(gap.value * 100, 2),
            "model": heard(model_spread, (start, stop)),
            "take": heard(take_spread, gap.span),
            "verdict": VERDICTS.get(answer, "incertain"),
        }
        print()

    if not verdicts:
        print("\n  rien de jugé, rien d'écrit")
        return 0
    REVIEWS.mkdir(exist_ok=True)
    out = REVIEWS / f"{options.take}.json"
    # The two audios the verdicts were given on, by their bytes: synthesis is
    # not reproducible, and a judgement filed under a phrase's name would
    # otherwise outlive the sounds it was about.
    atomic.write_text(out, json.dumps({"take": options.take, "model": options.model,
                               "voice": options.candidate, "text": text,
                               "digests": {"model": matrix.fingerprint(model),
                                           "take": matrix.fingerprint(learner)},
                               "marks": verdicts},
                              indent=2, ensure_ascii=False) + "\n",
                   encoding="utf-8")
    counts = {}
    for mark in verdicts.values():
        counts[mark["verdict"]] = counts.get(mark["verdict"], 0) + 1
    print(f"  {out}")
    print("  " + "   ".join(f"{name} {n}" for name, n in sorted(counts.items())))
    return 0


if __name__ == "__main__":
    sys.exit(main())
