#!/usr/bin/env python3
"""Make a character voice out of an ordinary one, and listen to where it breaks.

The cast of the Free door wants marked voices, non-human ones included, and the
design bets that ordinary signal work gets them -- no weights, no licence to
check, and it runs while the voice plays (`../docs/design/local-chain.md`). The
bet is unmeasured. This is the ear bench that measures it.

Two outputs, because they answer two different criteria and one cannot stand for
the other. **Sweeps** move a single knob across its range: they say where each
one stops sounding like a mouth, which is criterion 2 and is unreadable once
several knobs are turned at once. **Blends** are named creatures: they say
whether a cast comes out at all, which is criterion 4.

Rendered at the engine's own rate rather than the 16 kHz the rest of the bench
uses. Everything else here feeds the matrix, where the contract is the take's
shape; this feeds an ear, and 16 kHz throws away the very brightness a metallic
artefact lives in. A character voice is never the yardstick a measurement is
taken against (`../docs/reference.md`), so nothing downstream depends on it.

Peak-normalised for the same reason: told apart at different loudnesses, the
louder one wins, and the judgement stops being about timbre.
"""

import argparse
import math
import sys
from pathlib import Path

import numpy as np
import soundfile as sf
from scipy import signal

import atomic

VOICE = Path(__file__).resolve().parent.parent / "tmp" / "piper" / "en_US-libritts_r-medium.onnx"
OUT = Path(__file__).resolve().parent / "out" / "creature"

# Long enough to carry a contour and a few consonants, short enough to listen to
# forty times. Sibilants and a plosive on purpose -- they are what a filter eats
# first, so an unintelligible take shows up here rather than three presets later.
LINE = "So you finally came back. I asked you twice, and you said nothing."

# Praat needs a search range for the pitch it is about to move. Wide enough for
# an already-shifted voice, and it fails loudly when the take carries no pitch
# at all rather than guessing a median.
PITCH_FLOOR = 60.0
PITCH_CEILING = 600.0


def load_voice(path):
    """The engine, or a named reason. Never a substitute voice."""
    if not path.exists():
        raise SystemExit(
            f"Missing voice model: {path}\n"
            "Pull it beside its .json config (see ../docs/design/local-chain.md)."
        )
    from piper import PiperVoice

    return PiperVoice.load(str(path))


def say(voice, text, speaker, **engine):
    """One line, one speaker, as float32 mono.

    `engine` reaches the model instead of the signal coming out of it, which is
    why it is the only thing here that touches delivery: a filter works on
    speech already spoken, and no amount of it stops a narrator reading like a
    narrator.
    """
    from piper import SynthesisConfig

    config = SynthesisConfig(speaker_id=speaker, **engine)
    chunks = list(voice.synthesize(text, syn_config=config))
    if not chunks:
        raise SystemExit(f"The engine rendered nothing for speaker {speaker}.")
    audio = np.concatenate([chunk.audio_float_array for chunk in chunks])
    return audio.astype(np.float32), chunks[0].sample_rate


# ---------------------------------------------------------------- the knobs


def _praat(x, rate):
    import parselmouth

    return parselmouth.Sound(np.asarray(x, dtype=np.float64), sampling_frequency=rate)


def _median_pitch(sound):
    import parselmouth
    from parselmouth.praat import call

    pitch = sound.to_pitch(pitch_floor=PITCH_FLOOR, pitch_ceiling=PITCH_CEILING)
    median = call(pitch, "Get quantile", 0, 0, 0.5, "Hertz")
    if not math.isfinite(median) or median <= 0:
        raise SystemExit("No pitch found in the take -- nothing to shift it from.")
    return median


def _change_gender(x, rate, formant_ratio, new_median):
    """Praat's own resample-then-restore, which moves formants and pitch apart.

    Resampling alone drags the formants and the pitch together; Praat then puts
    the pitch back where it is told, which is what makes a formant-only shift --
    a bigger body at the same note -- possible at all. `new_median` of 0 keeps
    the pitch the take already had.
    """
    from parselmouth.praat import call

    sound = _praat(x, rate)
    shifted = call(
        sound, "Change gender",
        PITCH_FLOOR, PITCH_CEILING, formant_ratio, new_median, 1.0, 1.0,
    )
    return np.asarray(shifted.values[0], dtype=np.float32)


def pitch(x, rate, semitones):
    """Move the note, leave the body alone."""
    if semitones == 0:
        return x
    target = _median_pitch(_praat(x, rate)) * 2.0 ** (semitones / 12.0)
    return _change_gender(x, rate, 1.0, target)


def formants(x, rate, ratio):
    """Move the body, leave the note alone -- the giant and the imp knob."""
    if ratio == 1.0:
        return x
    return _change_gender(x, rate, ratio, 0.0)


def double(x, rate, cents, mix, delay_ms=11.0):
    """The voice against itself, slightly out of tune -- the demon recipe.

    The delay is the point as much as the detune: laid exactly on top, two near
    copies cancel in places instead of thickening.
    """
    if mix == 0:
        return x
    other = pitch(x, rate, cents / 100.0)
    lag = int(rate * delay_ms / 1000.0)
    other = np.concatenate([np.zeros(lag, dtype=np.float32), other])
    length = max(len(x), len(other))
    a = np.pad(x, (0, length - len(x)))
    b = np.pad(other, (0, length - len(other)))
    return a + mix * b


def ring(x, rate, hz, mix):
    """Multiplication by a tone -- metal, machine, the thing that is not alive."""
    if mix == 0:
        return x
    t = np.arange(len(x), dtype=np.float32) / rate
    return (1.0 - mix) * x + mix * x * np.sin(2.0 * np.pi * hz * t).astype(np.float32)


def saturate(x, rate, drive, mix):
    """Rasp and torn throat, by squashing the peaks rather than clipping them.

    Sample by sample, so the rate is unread -- it stays in the signature because
    every knob is called the same way.
    """
    if mix == 0 or drive <= 0:
        return x
    shaped = np.tanh(drive * x) / np.tanh(drive)
    return (1.0 - mix) * x + mix * shaped.astype(np.float32)


def breath(x, rate, amount, cutoff=1200.0):
    """Noise that follows the speech envelope -- the ghost and the whisper.

    Following the envelope is what separates breath from hiss: hiss is there in
    the silences too, and the ear reads it as a broken recording rather than as
    a voice.
    """
    if amount == 0:
        return x
    b, a = signal.butter(2, cutoff / (rate / 2.0), btype="low")
    envelope = signal.filtfilt(b, a, np.abs(x)).astype(np.float32)
    noise = np.random.default_rng(0).standard_normal(len(x)).astype(np.float32)
    hb, ha = signal.butter(2, cutoff / (rate / 2.0), btype="high")
    noise = signal.filtfilt(hb, ha, noise).astype(np.float32)
    return x + amount * envelope * noise


def reverb(x, rate, seconds, mix):
    """A decaying noise tail -- the cave, the hall, the place the voice is in."""
    if mix == 0 or seconds <= 0:
        return x
    n = int(rate * seconds)
    tail = np.random.default_rng(1).standard_normal(n).astype(np.float32)
    tail *= np.exp(-np.linspace(0.0, 7.0, n, dtype=np.float32))
    wet = signal.fftconvolve(x, tail)[: len(x)].astype(np.float32)
    wet /= max(float(np.max(np.abs(wet))), 1e-9)
    return (1.0 - mix) * x + mix * wet * float(np.max(np.abs(x)))


# ------------------------------------------------------- what gets rendered

# One knob at a time, across the range where it stops being a mouth. Read for
# the value at which the line stops transcribing without effort, not for taste.
SWEEPS = {
    "pitch": [("pitch", dict(semitones=s)) for s in (-12, -9, -6, -3, 3, 6, 9)],
    "formants": [("formants", dict(ratio=r)) for r in (0.72, 0.8, 0.9, 1.1, 1.25, 1.4)],
    "double": [("double", dict(cents=c, mix=0.7)) for c in (7, 15, 30, 60)],
    "ring": [("ring", dict(hz=h, mix=0.5)) for h in (30, 60, 110, 200)],
    "saturate": [("saturate", dict(drive=d, mix=0.8)) for d in (2, 5, 12, 30)],
    "breath": [("breath", dict(amount=a)) for a in (0.15, 0.35, 0.7)],
    "reverb": [("reverb", dict(seconds=0.6, mix=m)) for m in (0.2, 0.4, 0.7)],
}

# Whole creatures. Each is a guess written before hearing it, so that what comes
# back is a verdict on the guess rather than a knob turned until it pleased.
CAST = {
    # Big body, low note, a torn throat. The one the design keeps naming.
    "goblin": [
        ("formants", dict(ratio=0.78)),
        ("pitch", dict(semitones=-5)),
        ("saturate", dict(drive=6, mix=0.6)),
    ],
    # Bigger and slower still, and a room around it.
    "ogre": [
        ("formants", dict(ratio=0.68)),
        ("pitch", dict(semitones=-8)),
        ("saturate", dict(drive=4, mix=0.5)),
        ("reverb", dict(seconds=0.8, mix=0.25)),
    ],
    # Small body, high note, no rasp -- the test of whether up breaks before down.
    "imp": [
        ("formants", dict(ratio=1.3)),
        ("pitch", dict(semitones=6)),
    ],
    # No body at all: breath and a tail, the note barely moved.
    "wraith": [
        ("breath", dict(amount=0.6)),
        ("pitch", dict(semitones=-2)),
        ("reverb", dict(seconds=1.2, mix=0.45)),
    ],
    # The one thing a mouth cannot do, kept in on purpose as the far edge.
    "automaton": [
        ("ring", dict(hz=90, mix=0.55)),
        ("formants", dict(ratio=0.92)),
    ],
    # Two of it, out of tune, in a hall.
    "chorus": [
        ("double", dict(cents=22, mix=0.8)),
        ("pitch", dict(semitones=-3)),
        ("reverb", dict(seconds=0.7, mix=0.3)),
    ],
}

# The model's own three, read off this voice's config: rate, how much the voice
# itself wavers, and how uneven the phoneme durations come out. They are swept
# apart from the filters because they are not filters -- they change what is
# said before anything is done to it, and they are the only lever on delivery.
ENGINE = {
    "rate": [("length_scale", v) for v in (0.65, 0.8, 1.0, 1.3, 1.7, 2.2)],
    "waver": [("noise_scale", v) for v in (0.0, 0.15, 0.333, 0.6, 1.0)],
    "uneven": [("noise_w_scale", v) for v in (0.0, 0.15, 0.333, 0.7, 1.2)],
}

KNOBS = {
    "pitch": pitch, "formants": formants, "double": double,
    "ring": ring, "saturate": saturate, "breath": breath, "reverb": reverb,
}


def apply(x, rate, chain):
    for name, kwargs in chain:
        x = KNOBS[name](x, rate, **kwargs)
    return x


def label(name, kwargs):
    bits = "-".join(f"{k}{v}" for k, v in kwargs.items())
    return f"{name}-{bits}"


def write(path, x, rate):
    peak = float(np.max(np.abs(x)))
    if peak == 0:
        raise SystemExit(f"Silence came out of {path.name} -- a knob ate the take.")
    x = (x / peak * 0.89).astype(np.float32)
    with atomic.opened(path) as handle:
        sf.write(handle, x, rate, subtype="PCM_16", format="WAV")
    print(f"  {path.relative_to(OUT.parent.parent)}")


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("mode", choices=("voices", "engine", "sweep", "cast"))
    parser.add_argument("-s", "--speaker", type=int, default=0, help="speaker id of the engine")
    parser.add_argument("-n", "--count", type=int, default=12, help="how many speakers, for `voices`")
    parser.add_argument("-t", "--text", default=LINE)
    parser.add_argument("-k", "--knob", help="one sweep instead of all")
    args = parser.parse_args()

    voice = load_voice(VOICE)

    if args.mode == "voices":
        # The raw cast, before any filter: what the engine gives on its own, and
        # whether any of its speakers is already odd enough to be a creature.
        out = OUT / "voices"
        print(f"{args.count} speakers, plain:")
        for speaker in range(args.count):
            audio, rate = say(voice, args.text, speaker)
            write(out / f"speaker-{speaker:03d}.wav", audio, rate)
        return

    audio, rate = say(voice, args.text, args.speaker)
    out = OUT / f"speaker-{args.speaker:03d}"
    write(out / "plain.wav", audio, rate)

    if args.mode == "engine":
        knobs = [args.knob] if args.knob else sorted(ENGINE)
        for knob in knobs:
            print(f"{knob}:")
            for field, value in ENGINE[knob]:
                said, rate = say(voice, args.text, args.speaker, **{field: value})
                write(out / "engine" / f"{knob}-{value}.wav", said, rate)
        return

    if args.mode == "sweep":
        knobs = [args.knob] if args.knob else sorted(SWEEPS)
        for knob in knobs:
            print(f"{knob}:")
            for name, kwargs in SWEEPS[knob]:
                write(out / knob / f"{label(name, kwargs)}.wav", apply(audio, rate, [(name, kwargs)]), rate)
        return

    print("cast:")
    for name, chain in CAST.items():
        write(out / "cast" / f"{name}.wav", apply(audio, rate, chain), rate)


if __name__ == "__main__":
    sys.exit(main())
