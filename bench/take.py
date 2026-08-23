#!/usr/bin/env python3
"""Guide one recording session: say it, hear a model, copy it, hear the other, copy that.

A control take is a *copy of a model that was heard*, never a take someone was
asked to say "correctly" -- the engine compares realisations, not norms, and
"correctly" is not an instruction anyone can follow measurably. So each phrase
is spoken cold first: once a model has been heard it cannot be unheard.

Two models per phrase, one per accent, from the same provider and register so
that what differs between them is the accent and not the voice. That is what
lets the reading afterwards separate a lexicon question from a voice question.

Recording runs from one keypress to the next rather than for a fixed span --
nobody knows in advance how long they will take over a sentence, and a take cut
short is a take to redo.

Speech must not start at sample zero. The capture device lets an opening
transient through that saturates the first half-second, and trimming it takes
the attack of the first word with it if the speaker came in early -- the aligner
then returns zero-length segments across the whole opening.
"""

import argparse
import signal
import subprocess
import sys
import wave
from pathlib import Path

import synth
from phrases import ACCENT_TRIAL

HERE = Path(__file__).resolve().parent
TAKES = HERE / "out" / "takes"
RENDERS = HERE / "out" / "renders"

SAMPLE_RATE = 16000
LEAD_SECONDS = 1

# label -> the model this take is copied from; None means spoken cold.
PASSES = (("spontaneous", None), ("copy-gb", "gb"), ("copy-us", "us"))


class Cancelled(Exception):
    """Ctrl+C or EOF anywhere in the session: stop cleanly, keep what is done."""


def ask(message=""):
    try:
        return input(message).strip().lower()
    except (EOFError, KeyboardInterrupt):
        raise Cancelled


def trim_lead(path):
    with wave.open(str(path), "rb") as handle:
        rate = handle.getframerate()
        frames = handle.readframes(handle.getnframes())
    with wave.open(str(path), "wb") as handle:
        handle.setnchannels(1)
        handle.setsampwidth(2)
        handle.setframerate(rate)
        handle.writeframes(frames[LEAD_SECONDS * rate * 2:])


def capture(destination):
    """Record from this keypress to the next, then drop the opening second."""
    destination.parent.mkdir(parents=True, exist_ok=True)
    ask("        [Entrée] démarrer ")
    process = subprocess.Popen(
        ["arecord", "-t", "wav", "-f", "S16_LE", "-r", str(SAMPLE_RATE),
         "-c", "1", "-q", str(destination)]
    )
    try:
        ask("        ... enregistre — [Entrée] arrêter ")
    finally:
        # arecord takes the interrupt and still writes its wav header.
        process.send_signal(signal.SIGINT)
        process.wait()

    if not destination.is_file() or destination.stat().st_size == 0:
        raise SystemExit(f"Rien n'a été enregistré dans {destination}.")
    trim_lead(destination)
    return destination


def play(path):
    subprocess.run(["aplay", "-q", str(path)], check=False)


def listen(model, name):
    while True:
        answer = ask(f"        [Entrée] écouter {name} (r pour réécouter ensuite) ")
        play(model)
        if ask("        [Entrée] enchaîner, r pour réécouter ") != "r":
            return


def one_take(path, model, name):
    while True:
        if model is not None:
            listen(model, name)
        capture(path)
        play(path)
        if ask("        [Entrée] garder, r pour refaire ") != "r":
            return


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--gb", default="eleven-gb-daniel")
    parser.add_argument("--us", default="eleven-us-eric")
    parser.add_argument("-f", "--force", action="store_true",
                        help="reprendre les prises déjà faites")
    args = parser.parse_args(argv)

    models = {}
    for accent, name in (("gb", args.gb), ("us", args.us)):
        if name not in synth.BY_NAME:
            raise SystemExit(f"Voix inconnue {name!r}. "
                             "Connues : " + ", ".join(synth.BY_NAME))
        models[accent] = synth.BY_NAME[name]

    print(f"\n{len(ACCENT_TRIAL)} phrases, trois prises chacune : "
          f"naturel, calque {models['gb'].name}, calque {models['us'].name}.")
    print("Attends une seconde après [Entrée] avant de parler — la première "
          "seconde est coupée.\n")

    try:
        for index, (slug, text) in enumerate(ACCENT_TRIAL, start=1):
            print(f"\n[{index}/{len(ACCENT_TRIAL)}]  « {text} »")
            for label, accent in PASSES:
                path = TAKES / label / f"{slug}.wav"
                if path.is_file() and not args.force:
                    print(f"     {label} — déjà pris")
                    continue
                if accent is None:
                    print("     naturel — dis-la comme elle te vient")
                    one_take(path, None, None)
                else:
                    candidate = models[accent]
                    model = RENDERS / candidate.name / f"{slug}.wav"
                    synth.render(text, candidate, model)
                    print(f"     calque {accent.upper()} ({candidate.name})")
                    one_take(path, model, f"le modèle {accent.upper()}")
    except Cancelled:
        print("\n\nInterrompu — les prises gardées restent en place.")
        return 1

    print(f"\nTerminé. Prises dans {TAKES.relative_to(HERE.parent)}/")
    return 0


if __name__ == "__main__":
    sys.exit(main())
