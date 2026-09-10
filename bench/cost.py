#!/usr/bin/env python3
"""What the acoustic pass costs on this machine, by turn length.

`analysis.md` holds the same sweep for the phone, and `phone.py` is what put it
there. This one asks the question of whatever machine it is run on -- a desk, a
rented server -- over the very graph the phone runs, so the two tables face each
other and the only thing that differs is the hardware.

Nothing here reads a matrix or judges anything: it times a pass and watches what
it holds. Memory is reported as the peak of the whole process, so the weights
are in it; what a second simultaneous request would add is the part above them.

    ACOUSTIC_MODEL=timit-ipa python3 cost.py                    # int8, four threads
    ACOUSTIC_MODEL=timit-ipa python3 cost.py --precision float  # what the rounding buys
    ACOUSTIC_MODEL=timit-ipa python3 cost.py --threads 1        # the per-core figure
    ACOUSTIC_MODEL=timit-ipa python3 cost.py --audio some.wav   # off a machine with no corpus

Threads are the interesting knob on a server: logical threads over shared cores
slow the pass down rather than speed it up, so what is being sized is physical
cores.
"""

import argparse
import gc
import resource
import statistics
import time
from pathlib import Path

import numpy as np
import soundfile as sf

import matrix

# The lengths `analysis.md` already holds for the phone, so the two tables can
# be read against each other row by row.
LENGTHS = [3, 6, 12, 20, 30]
TAKES = Path(__file__).resolve().parent / "out" / "takes"


def sources(where):
    """The recordings to build a turn out of: a directory of them, or one file.

    A rented machine carries neither the bench's corpus nor a way to record, so
    what it is pointed at is whatever wav was sent up with the scripts.
    """
    where = Path(where)
    if where.is_dir():
        return sorted(where.rglob("*.wav"))
    if where.is_file():
        return [where]
    raise SystemExit(f"{where} n'existe pas — --audio veut un wav ou un dossier")


def speech(seconds, where):
    """Real speech of the wanted length, from the recordings at hand.

    Silence would time the same -- the network spends the same arithmetic on
    every frame -- but a pass fed with real speech is one less thing to explain
    when a number surprises. A single short file is repeated to reach the
    length, which changes nothing to the arithmetic and keeps the input speech.
    """
    wanted = int(seconds * matrix.SAMPLE_RATE)
    chunks, held = [], 0
    for wav in sources(where):
        audio, rate = sf.read(wav, dtype="float32")
        if rate != matrix.SAMPLE_RATE:
            continue
        if audio.ndim > 1:
            audio = audio.mean(axis=1)
        chunks.append(audio)
        held += len(audio)
        if held >= wanted:
            break
    if not chunks:
        raise SystemExit(f"aucun wav à {matrix.SAMPLE_RATE} Hz sous {where}")
    gathered = list(chunks)
    turn = 0
    while held < wanted:
        again = gathered[turn % len(gathered)]
        chunks.append(again)
        held += len(again)
        turn += 1
    return np.concatenate(chunks)[:wanted]


def peak_megabytes():
    return resource.getrusage(resource.RUSAGE_SELF).ru_maxrss / 1024


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--precision", choices=["int8", "float"], default="int8",
                        help="les poids arrondis, ou ceux dont ils sortent")
    parser.add_argument("--threads", type=int, default=4,
                        help="fils d'inférence — des cœurs physiques, pas des vCPU")
    parser.add_argument("--repeats", type=int, default=3)
    parser.add_argument("--lengths", type=int, nargs="*", default=LENGTHS,
                        help="durées de tour à mesurer, en secondes")
    parser.add_argument("--audio", default=TAKES,
                        help="où prendre la parole — un wav ou un dossier")
    options = parser.parse_args(argv)

    import onnxruntime

    suffix = f"-{matrix.ROUNDED}" if options.precision == "int8" else ""
    weights = matrix.ONNX_WEIGHTS.parent / f"{matrix.CHOSEN}{suffix}.onnx"
    if not weights.is_file():
        raise SystemExit(f"{weights} manque — python3 export.py")
    # The graph looks its sidecar up by name; both count towards what has to be
    # held in memory before a single frame is read.
    beside = weights.parent / f"{weights.name}.data"
    size = weights.stat().st_size + (beside.stat().st_size
                                     if beside.is_file() else 0)

    settings = onnxruntime.SessionOptions()
    settings.intra_op_num_threads = options.threads
    settings.inter_op_num_threads = 1
    started = time.perf_counter()
    session = onnxruntime.InferenceSession(
        str(weights), settings, providers=["CPUExecutionProvider"])
    load = time.perf_counter() - started

    print(f"\n{weights.name}, {size / 1e6:.0f} Mo, {options.threads} fil(s)")
    print(f"chargement {load:.2f} s\n")
    print(f"{'tour':>6} {'passe':>9} {'rapport':>9} {'pic':>10}")
    for seconds in options.lengths:
        values = matrix.prepared(speech(seconds, options.audio))
        gc.collect()
        times = []
        for _ in range(options.repeats):
            started = time.perf_counter()
            session.run(None, {"input_values": values})
            times.append(time.perf_counter() - started)
        pass_time = statistics.median(times)
        print(f"{seconds:>4} s {pass_time:>8.2f}s {pass_time / seconds:>8.2f}x"
              f" {peak_megabytes():>7.0f} Mo")
    print()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
