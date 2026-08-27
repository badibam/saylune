#!/usr/bin/env python3
"""Le modèle tourne-t-il sur un téléphone, et rend-il les mêmes chiffres ?

The one question the desktop cannot answer. Two of its three unknowns were
settled here -- the runtime costs nothing in float, and the rounding was put
back on a perimeter that preserves the reading -- and what is left needs the
device: how much memory it takes to hold the weights, and how long a pass costs
on a real turn of speech.

This script is the whole loop. It pushes the weights and the bench's own audio,
starts the probe, follows what it says, pulls the matrices back and writes them
into the cache under their own reading, so that the last word belongs to
`concord.py` rather than to an impression:

    python3 phone.py                              # pousse, mesure, rapatrie
    python3 concord.py -r timit-ipa-onnx-int8 -a phone-int8
    READING=phone-int8 python3 faults.py          # le verdict, sur l'appareil

The audio is the bench's own, and it comes back filed exactly as the bench files
its own matrices: what the device produces is a reading like any other, so every
brick can be run on it without knowing where it was computed. A phone that
agreed with the desktop about different sounds would prove nothing.
"""

import argparse
import subprocess
import sys
import time
from pathlib import Path

import numpy as np

HERE = Path(__file__).resolve().parent
PACKAGE = "app.speakup.debug"
PROBE = f"{PACKAGE}/app.speakup.embedded.ProbeActivity"
# Not the app's external directory: since Android 11 what the shell pushes
# there belongs to the shell and the app is refused entry. This path both can
# reach -- pushed here, then opened by name -- and it is a debug arrangement.
REMOTE = "/data/local/tmp/speakup-probe"

# What comes back leaves by the app's own directory: SELinux lets an app read
# the path above and never write to it.
DUMPS = f"/sdcard/Android/data/{PACKAGE}/files/matrices"
TAG = "speakup.probe"

# A phone reads a flat directory, the bench files a matrix under the tag it was
# read for. The two are bridged by the name on the device, which carries both.
JOIN = "__"

# A pass over a turn of speech has to fit inside a turn of speech. Nothing here
# enforces it -- it is what the numbers are read against.
WATCHED = 900


def adb(*arguments, capture=True):
    return subprocess.run(["adb", *arguments], capture_output=capture,
                          text=True, check=False)


def device():
    probe = adb("devices")
    ready = [line.split()[0] for line in probe.stdout.splitlines()[1:]
             if len(line.split()) >= 2 and line.split()[1] == "device"]
    if len(ready) != 1:
        raise SystemExit("il faut exactement un appareil prêt — ./run devices")
    return ready[0]


def pushed(local, remote):
    """Push only what is not already there, byte for byte.

    The weights are a third of a gigabyte over USB: sending them again at every
    run would make the measure something one avoids repeating.
    """
    size = adb("shell", "stat", "-c", "%s", remote).stdout.strip()
    if size.isdigit() and int(size) == local.stat().st_size:
        return False
    adb("push", str(local), remote, capture=False)
    return True


def follow(seconds):
    """What the probe says, until it says it is done."""
    started = time.time()
    watcher = subprocess.Popen(
        ["adb", "logcat", "-s", f"{TAG}:I", "-v", "raw"],
        stdout=subprocess.PIPE, text=True)
    lines = []
    try:
        for line in watcher.stdout:
            line = line.rstrip()
            if not line:
                continue
            print(f"    {line}")
            lines.append(line)
            if line.startswith("FIN"):
                return lines
            if time.time() - started > seconds:
                print("    (le temps imparti est écoulé)")
                return lines
    finally:
        watcher.terminate()
    return lines


def material(models):
    """Every file the labelled set reads, under the tag the bench files it by.

    Taken from `faults.py` rather than from a directory, so that what the phone
    reads is exactly what the verdict is made of -- the takes and the model
    renders they are compared to, nothing beside.
    """
    import faults

    wanted = {}
    for take, slug, *_ in faults.CASES:
        wanted[(f"set-{take}", slug)] = faults.TAKES / f"{take}.wav"
        for model in models:
            wanted[(f"sentences-{model}", slug)] = (
                faults.RENDERS / model / "sentences" / f"{slug}.wav")
    return {key: wav for key, wav in sorted(wanted.items()) if wav.is_file()}


def gathered(destination):
    """Bring the matrices back and file them where the bench keeps its own."""
    pulled = HERE / "out" / "phone"
    for stale in pulled.glob("*.mat"):
        stale.unlink()
    pulled.mkdir(parents=True, exist_ok=True)
    adb("pull", f"{DUMPS}/.", str(pulled), capture=False)

    written = 0
    for dump in sorted(pulled.glob("*.mat")):
        raw = dump.read_bytes()
        frames, symbols = np.frombuffer(raw, dtype="<i4", count=2)
        values = np.frombuffer(raw, dtype="<f4", offset=8).reshape(frames, symbols)
        tag, _, slug = dump.stem.partition(JOIN)
        cache = destination / tag / f"{slug}.npz"
        cache.parent.mkdir(parents=True, exist_ok=True)
        np.savez_compressed(cache, probabilities=values)
        written += 1
    return written


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-m", "--model", action="append", default=None,
                        help="répétable ; par défaut les deux accents")
    parser.add_argument("--threads", type=int, default=4,
                        help="fils d'inférence sur l'appareil")
    parser.add_argument("--slug", default=None,
                        help="sous quel nom ranger la lecture de l'appareil")
    parser.add_argument("--float", dest="plain", action="store_true",
                        help="les poids en flottant — sépare ce que les noyaux "
                             "8 bits déplacent de ce que l'appareil déplace")
    options = parser.parse_args(argv)

    import matrix
    precision = "" if options.plain else f"-{matrix.ROUNDED}"
    slug = options.slug or f"phone{precision}"
    weights = matrix.ONNX_WEIGHTS.parent / f"{matrix.CHOSEN}{precision}.onnx"
    if not weights.is_file():
        raise SystemExit(f"{weights} manque — python3 export.py")
    models = options.model or ["eleven-gb-daniel", "eleven-us-eric"]
    wanted = material(models)
    if not wanted:
        raise SystemExit("le banc n'a rien rendu ni enregistré à lire")

    print(f"\nappareil {device()}")
    adb("shell", "rm", "-rf", f"{REMOTE}/audio", DUMPS)
    adb("shell", "mkdir", "-p", f"{REMOTE}/audio")
    # Under their own name, sidecar included: the graph looks the sidecar up by
    # the name it was exported with, and renaming the pair breaks it.
    beside = weights.parent / f"{weights.name}.data"
    total = weights.stat().st_size + (beside.stat().st_size
                                      if beside.is_file() else 0)
    print(f"poids   {weights.name}, {total / 1e6:.0f} Mo")
    for stale in adb("shell", "ls", REMOTE).stdout.split():
        if stale.endswith(".onnx") and stale != weights.name:
            adb("shell", "rm", "-f", f"{REMOTE}/{stale}", f"{REMOTE}/{stale}.data")
    fresh = pushed(weights, f"{REMOTE}/{weights.name}")
    if beside.is_file():
        fresh |= pushed(beside, f"{REMOTE}/{beside.name}")
    print("        poussés" if fresh else "        déjà sur l'appareil")
    for (tag, name), wav in wanted.items():
        pushed(wav, f"{REMOTE}/audio/{tag}{JOIN}{name}.wav")
    print(f"audio   {len(wanted)} fichiers")
    # What the shell pushes is the shell's; the app opens it by name and has to
    # be allowed to. The dump directory is the one place it writes.
    adb("shell", "chmod", "-R", "a+rX", REMOTE)

    adb("shell", "am", "force-stop", PACKAGE)
    adb("logcat", "-c")
    adb("shell", "am", "start", "-n", PROBE, "--ei", "threads",
        str(options.threads), "--es", "root", REMOTE)
    print("\nsonde :")
    follow(WATCHED)

    # The device's reading goes into the cache under its own name, and the
    # desktop's counterpart under the same tag, so that the two face each other
    # file by file rather than by hand.
    written = gathered(HERE / "out" / "matrices" / slug)
    print(f"\n{written} matrices rapatriées")

    # The desktop reads the same files under its own reading, so the two face
    # each other file by file rather than by hand.
    reference = HERE / "out" / "matrices" / f"{matrix.CHOSEN}-onnx{precision}"
    for (tag, name), wav in wanted.items():
        matrix.probabilities(wav, cache=reference / tag / f"{name}.npz")
    print(f"le poste a lu les mêmes fichiers sous {reference.name}\n")
    print(f"    python3 concord.py -r {reference.name} -a {slug}")
    print(f"    READING={slug} python3 faults.py")
    return 0


if __name__ == "__main__":
    sys.exit(main())
