#!/usr/bin/env python3
"""What compressing the upload does to the reading.

Deporting the acoustic pass is dominated by the upload, not the arithmetic:
measured on the device, the pass costs a tenth of the audio's length and the
transfer costs the rest. So the question is how few bytes a turn can travel in
without the reading moving -- and that is a measurement, not a judgement call.

There is a bar for it already, and it is not invented here. Two machines running
these very weights disagree a little, and the montage tolerates that for a
written reason: both audios of a turn go through the same pass, so the machine's
own bias is the same twice and cancels (`../docs/reference.md`). A codec that
moves the reading less than the machines already move it adds no risk that has
not already been accepted -- and one that moves it more has to answer for
itself.

Read on both kinds of audio, because a codec is not neutral just for being
applied to both sides: a render is clean and a learner's take is a room with a
microphone in it, and the same encoder does not do the same thing to them
(`../docs/reference.md`, on symmetry of gesture against symmetry of effect).

    ACOUSTIC_MODEL=timit-ipa python3 squeeze.py
    ACOUSTIC_MODEL=timit-ipa python3 squeeze.py --rates 24 16
"""

import argparse
import subprocess
import sys
import tempfile
from pathlib import Path

import numpy as np
import soundfile as sf

import matrix

HERE = Path(__file__).resolve().parent

# One of each: what the model sounds like, and what a person in a room sounds
# like. The bar is read on the worse of the two, never on their average.
SOURCES = {
    "rendu": HERE / "out" / "renders" / "azure-gb-sonia" / "sentences" / "think.wav",
    "prise": HERE / "out" / "takes" / "answers" / "dark-sky.wav",
}

# What the machines already move, measured on a 26 s turn between an Intel
# laptop and an AMD server (`../docs/design/remote-analysis.md`). The bar.
MACHINES = dict(median=6.7e-07, peaks=12 / 1306)


def coded(wav, rate):
    """The audio through an encoder and back, as samples.

    Opus is the only lossy candidate worth the trouble at this size, and it
    resamples to 48 kHz on the way in whatever it is handed; brought back to 16
    it is the same length, which is what lets the two matrices face each other
    row for row. `flac` is here as the lossless control: it has to come back
    identical, and if it does not the harness is wrong rather than the codec.
    """
    with tempfile.TemporaryDirectory() as room:
        room = Path(room)
        squeezed = room / ("a.opus" if rate else "a.flac")
        back = room / "b.wav"
        encode = (["-c:a", "libopus", "-b:a", f"{rate}k", "-application", "voip"]
                  if rate else ["-c:a", "flac"])
        run(["ffmpeg", "-y", "-i", str(wav), *encode, str(squeezed)])
        run(["ffmpeg", "-y", "-i", str(squeezed), "-ar", str(matrix.SAMPLE_RATE),
             "-ac", "1", str(back)])
        audio, got = sf.read(back, dtype="float32")
        if got != matrix.SAMPLE_RATE:
            raise SystemExit(f"{back} came back at {got} Hz")
        return audio, squeezed.stat().st_size


def run(command):
    done = subprocess.run(command, capture_output=True, text=True)
    if done.returncode != 0:
        raise SystemExit(f"{command[0]} a échoué :\n{done.stderr[-800:]}")


def read(session, audio):
    return session.run(None, {"input_values": matrix.prepared(audio)})[0][0]


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--rates", type=int, nargs="*", default=[32, 24, 16, 12],
                        help="débits Opus en kbit/s ; 0 pour le FLAC sans perte")
    parser.add_argument("--threads", type=int, default=4)
    options = parser.parse_args(argv)

    import onnxruntime

    weights = matrix.ONNX_WEIGHTS.parent / f"{matrix.CHOSEN}-{matrix.ROUNDED}.onnx"
    if not weights.is_file():
        raise SystemExit(f"{weights} manque — python3 export.py")
    settings = onnxruntime.SessionOptions()
    settings.intra_op_num_threads = options.threads
    settings.inter_op_num_threads = 1
    session = onnxruntime.InferenceSession(
        str(weights), settings, providers=["CPUExecutionProvider"])

    print(f"\nla barre, ce que deux machines déplacent déjà : "
          f"médiane {MACHINES['median']:.1e}, "
          f"pics déplacés {100 * MACHINES['peaks']:.2f} %\n")

    for name, wav in SOURCES.items():
        if not wav.is_file():
            print(f"{name} : {wav} manque, sauté\n")
            continue
        plain, plain_rate = sf.read(wav, dtype="float32")
        if plain.ndim > 1:
            plain = plain.mean(axis=1)
        seconds = len(plain) / matrix.SAMPLE_RATE
        theirs = read(session, plain)
        raw = wav.stat().st_size
        print(f"{name} — {wav.name}, {seconds:.1f} s, {raw / 1024:.0f} Ko en PCM")
        print(f"{'débit':>8} {'octets':>9} {'gain':>6} "
              f"{'médiane':>10} {'max':>8} {'pics déplacés':>14}")
        for rate in [0] + [r for r in options.rates if r]:
            audio, size = coded(wav, rate)
            # Trimmed to the shorter of the two: a codec may hand back a frame
            # more, and comparing a matrix to a longer one compares nothing.
            keep = min(len(audio), len(plain))
            mine = read(session, audio[:keep])
            rows = min(len(mine), len(theirs))
            gap = np.abs(mine[:rows] - theirs[:rows])
            moved = (mine[:rows].argmax(1) != theirs[:rows].argmax(1)).mean()
            label = "flac" if rate == 0 else f"{rate}k"
            print(f"{label:>8} {size / 1024:>7.0f} Ko {raw / size:>5.1f}× "
                  f"{np.median(gap):>10.1e} {gap.max():>8.3f} "
                  f"{100 * moved:>13.2f} %")
        print()
    return 0


if __name__ == "__main__":
    sys.exit(main())
