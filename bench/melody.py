#!/usr/bin/env python3
"""Brick 10 -- the melody, read as a contour and not as one number.

The third scale of the sound, and the last one still empty. `../docs/analysis.md`
specified it as a slope in semitones over the final voiced region: one number for
the whole sentence. That number cannot be painted. `../docs/reference.md` holds
that the three scales "anchor to the same characters of the displayed text" and
that the melody "sits on those same groups", and a terminal slope sits on nothing
-- so a fault anywhere but at the end is invisible, and even at the end there is
no stretch of letters to colour.

So: one pitch per **syllable**, both sides, and the mark is where the two
contours part. The syllables are the ones brick 8 already cuts, and each knows
its letters, so the melody is anchored for free and marks like the other two.

Two decisions, and the second is the one that could have been got wrong.

**Each side is centred on its own median pitch**, in semitones. A synthetic voice
and a learner do not share a register, and comparing hertz would call every man
imitating a woman's voice wrong at every syllable.

**Neither side is scaled.** Dividing by the spread would make the two contours
the same size before comparing them -- and a learner speaking flat where the
model swings is exactly the fault this brick exists to catch (the French
flattening, case 22 of `../docs/qualification.md`). Centring removes the register;
scaling would remove the fault.

One artefact is handled rather than met: an autocorrelation peak sometimes locks
onto a harmonic, which puts a syllable an octave off. `../docs/qualification.md`
names it and says its melody label is then false. It is folded back by octaves
instead of dropped -- an unreadable syllable would be silence, which the design
refuses.

    cd bench && python3 melody.py            # les prises étiquetées
    cd bench && python3 melody.py -v         # syllabe par syllabe
"""

import argparse
import statistics
import sys
from pathlib import Path

import numpy as np
import soundfile as sf

import join
import matrix
import overlap
import phrases
import syllables

HERE = Path(__file__).resolve().parent
TAKES = HERE / "out" / "takes" / "set"
RENDERS = HERE / "out" / "renders"

VOICE = "eleven-us-eric"

# The band a human voice speaks in. Outside it a peak is not a period.
F0_MIN, F0_MAX = 70.0, 400.0

# Below this the autocorrelation peak is noise, and naming a pitch there would
# invent the value the brick is trying to read.
VOICED = 0.30

# One pitch every 10 ms, over a window long enough to hold two periods of the
# lowest voice the band admits.
STEP_MS = 10
WINDOW_MS = 40

# A syllable further than this from the utterance's own median is a harmonic
# hook rather than a pitch, and gets folded back by octaves.
OCTAVE_BAND = 2.0

# take, the sentence it says, role, and what was done to the melody.
#
# **Both office takes are manipulations, and neither is a control.** Block E of
# `../docs/qualification.md` called 21 a control, and it was one *for that
# block*, which compared the two takes to each other to see whether a rising
# contour could be told from a flat one at all. Read against a model instead,
# the sentence is rendered as a statement -- it carries no question mark -- so a
# take said as a question departs from it and so does a flattened one. Keeping
# block E's label here would have read the measurement backwards.
#
# The controls are the calques: the same speaker, having heard the model,
# copying it. What the set has no control for is `going-office` itself, said
# plainly against its own model. That is a recording session, not a
# calculation (`../TODO.md`).
CASES = (
    ("21-office-question", "going-office", "manipulée", "question montante"),
    ("22-office-flat", "going-office", "manipulée", "aplatissement français"),
    ("24-important-calque", "important", "témoin", "calque du modèle"),
    ("26-interesting-calque", "interesting", "témoin", "calque du modèle"),
    ("23-important-spontane", "important", "spontané", "sans avoir entendu"),
    ("25-interesting-spontane", "interesting", "spontané", "sans avoir entendu"),
)


def track(wav):
    """The pitch of `wav` every 10 ms: hertz where voiced, 0 where not."""
    samples, rate = sf.read(wav, dtype="float64")
    if samples.ndim > 1:
        samples = samples.mean(axis=1)
    hop = int(rate * STEP_MS / 1000)
    size = int(rate * WINDOW_MS / 1000)
    low, high = int(rate / F0_MAX), int(rate / F0_MIN)
    out = []
    for start in range(0, max(len(samples) - size, 0) + 1, hop):
        window = samples[start:start + size]
        window = (window - window.mean()) * np.hanning(window.size)
        correlation = np.correlate(window, window, mode="full")[window.size - 1:]
        if correlation[0] <= 0 or high >= correlation.size:
            out.append(0.0)
            continue
        correlation = correlation / correlation[0]
        band = correlation[low:high]
        lag = int(np.argmax(band)) + low
        out.append(rate / lag if band.max() >= VOICED else 0.0)
    return np.array(out), STEP_MS / 1000


def folded(values):
    """Harmonic hooks brought back beside their neighbours, by octaves.

    Doubling or halving is what a hook does, so undoing it is exact where
    dropping the syllable would be silence. The reference is the median of the
    utterance, which no single hook can move.
    """
    voiced = [v for v in values if v]
    if not voiced:
        return values
    middle = statistics.median(voiced)
    out = []
    for value in values:
        while value and value > middle * OCTAVE_BAND:
            value /= 2
        while value and value < middle / OCTAVE_BAND:
            value *= 2
        out.append(value)
    return out


def over(pitch, step, low, high):
    """The pitch of one stretch in hertz, or zero if nothing in it was voiced.

    The median rather than the mean, over the voiced frames alone: a syllable
    is a fifth of a second of speech with a consonant at each end, and an
    unvoiced frame reported as zero would drag any average to the floor.
    """
    first, last = int(low / step), min(int(high / step) + 1, len(pitch))
    frames = [v for v in pitch[first:last] if v > 0]
    return statistics.median(frames) if frames else 0.0


def centred(heard):
    """A line of hertz turned into semitones around its own middle.

    None where nothing was voiced, which is **not** the same as flat and must
    not be drawn alike.
    """
    heard = folded(heard)
    voiced = [v for v in heard if v]
    if not voiced:
        return [None] * len(heard)
    middle = statistics.median(voiced)
    return [None if not v else 12 * np.log2(v / middle) for v in heard]


def contour(pitch, step, spans):
    """One pitch per syllable, in semitones from this side's own median."""
    return centred([over(pitch, step, low, high) for low, high in spans])


def read(model_wav, take_wav, text, model_tag, take_tag, slug):
    """The two contours of one utterance, syllable against syllable.

    The learner is read on the **model's** syllables: the model is the source of
    truth, and the aligned montage already says where each of its sounds sits in
    the learner's recording. The melody needs nothing else from it -- it reads
    positions, never labels -- so it costs no montage of its own.
    """
    cache = overlap.MATRICES / model_tag / f"{slug}.npz"
    sounds = join.joined(model_wav, text, cache=cache)
    cuts = syllables.cut(sounds)
    gaps = overlap.sounds(model_wav, take_wav, model_tag, take_tag, slug)
    if not cuts or not gaps:
        return None
    span_of = {gap.rank: gap.span for gap in gaps}
    step = matrix.seconds_per_frame()

    mine, theirs, letters = [], [], []
    for piece in cuts:
        low, high = piece.sounds
        held = [span_of[rank] for rank in range(low, high) if rank in span_of]
        if not held:
            continue
        mine.append((piece.low, piece.high))
        theirs.append((min(s[0] for s in held) * step,
                       max(s[1] for s in held) * step))
        letters.append(piece.letters)
    if not mine:
        return None

    model_pitch, model_step = track(model_wav)
    take_pitch, take_step = track(take_wav)
    return (letters,
            contour(model_pitch, model_step, mine),
            contour(take_pitch, take_step, theirs))


def run(voice, show):
    print(f"\n=== mélodie — modèle {voice}")
    print(f"    {'prise':<24}{'rôle':<10}{'syll':>6}{'pire':>8}"
          f"{'médian':>9}  ce qui a été fait")
    texts = dict(phrases.CALIBRATION) | dict(getattr(phrases, "HELDOUT", ()))

    for take, slug, role, what in CASES:
        wav = TAKES / f"{take}.wav"
        model_wav = RENDERS / voice / "sentences" / f"{slug}.wav"
        if not overlap.readable(wav) or not overlap.readable(model_wav):
            print(f"    {take:<24}{role:<10}  absent")
            continue
        found = read(model_wav, wav, texts[slug], f"sentences-{voice}",
                     f"set-{take}", slug)
        if not found:
            print(f"    {take:<24}{role:<10}  rien à lire")
            continue
        letters, model, theirs = found
        pairs = [(a, b, text) for a, b, text in zip(model, theirs, letters)
                 if a is not None and b is not None]
        if not pairs:
            print(f"    {take:<24}{role:<10}  aucune syllabe voisée des deux côtés")
            continue
        gaps = [abs(a - b) for a, b, _ in pairs]
        print(f"    {take:<24}{role:<10}{len(pairs):>6}{max(gaps):>8.1f}"
              f"{statistics.median(gaps):>9.1f}  {what}")
        if show:
            print(f"      {'syllabe':<12}{'modèle':>9}{'prise':>9}{'écart':>9}")
            for (a, b, text), gap in zip(pairs, gaps):
                print(f"      {text:<12}{a:>9.1f}{b:>9.1f}{gap:>9.1f}")

    print("\n    écarts en demi-tons, chaque côté centré sur sa propre médiane.")
    print("    aucun seuil n'est posé ici : la brique dit l'écart, pas le verdict.")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-c", "--candidate", default=VOICE)
    parser.add_argument("-v", "--verbose", action="store_true")
    args = parser.parse_args(argv)
    run(args.candidate, args.verbose)
    print(f"\n{matrix.audios()}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
