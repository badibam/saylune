#!/usr/bin/env python3
"""The app's FLAC encoder against the reference decoder.

`FlacTest` writes each case twice into `app/build/flac/`: as the app encodes it,
and as raw samples. This decodes the first with libFLAC -- through soundfile,
the very reader the server uses -- and compares it to the second, sample for
sample. A decoder written beside the encoder would share its misreadings; this
one was not written here.

`--stage` puts the test set's takes and the two voices' renders there first,
as raw samples, so the ratio read is this encoder's on real audio and not
libFLAC's. Each staged file is named after its group, which the summary sums by.

    cd bench && python3 flac.py --stage
    ./gradlew :app:testDebugUnitTest --tests 'app.saylune.capture.FlacTest'
    cd bench && python3 flac.py
"""

import argparse
import io
import sys
from collections import defaultdict
from pathlib import Path

import numpy as np
import soundfile as sf

BENCH = Path(__file__).resolve().parent
HOME = BENCH.parent / "app" / "build" / "flac"
STAGED = BENCH.parent / "app" / "build" / "flac-sources"
GROUPS = {
    "prise": BENCH / "out" / "takes" / "set",
    "daniel": BENCH / "out" / "renders" / "eleven-gb-daniel" / "sentences",
    "eric": BENCH / "out" / "renders" / "eleven-us-eric" / "sentences",
}


def stage():
    STAGED.mkdir(parents=True, exist_ok=True)
    count = 0
    for group, folder in GROUPS.items():
        for wav in sorted(folder.glob("*.wav")):
            samples, rate = sf.read(wav, dtype="int16")
            if rate != 16000 or samples.ndim > 1:
                continue
            (STAGED / f"{group}--{wav.stem}.raw").write_bytes(
                samples.astype("<i2").tobytes())
            count += 1
    print(f"{count} fichiers déposés dans {STAGED}")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--stage", action="store_true")
    if parser.parse_args(argv).stage:
        return stage()
    cases = sorted(HOME.glob("*.flac"))
    if not cases:
        raise SystemExit(f"rien dans {HOME} — lancer FlacTest d'abord")
    wrong = 0
    sizes = defaultdict(lambda: [0, 0, 0])
    for flac in cases:
        raw = np.frombuffer(flac.with_suffix(".raw").read_bytes(), dtype="<i2")
        try:
            decoded, rate = sf.read(io.BytesIO(flac.read_bytes()), dtype="int16")
        except Exception as trouble:
            print(f"  {flac.stem:<20} illisible : {trouble}")
            wrong += 1
            continue
        same = rate == 16000 and decoded.ndim == 1 and np.array_equal(decoded, raw)
        wrong += not same
        group = flac.stem.split("--")[0] if "--" in flac.stem else None
        if group:
            held = sizes[group]
            held[0] += 1
            held[1] += flac.stat().st_size
            held[2] += raw.size * 2
            if not same:
                print(f"  {flac.stem:<30} DIFFÉRENT")
            continue
        ratio = flac.stat().st_size / max(raw.size * 2, 1)
        print(f"  {flac.stem:<20} {raw.size:>7} échantillons  {ratio:6.1%}  "
              + ("identique" if same else "DIFFÉRENT"))
    for group, (count, packed, raw) in sorted(sizes.items()):
        print(f"  {group:<20} {count:>3} fichiers réels  {packed / raw:6.1%}")
    print(f"{len(cases) - wrong} sur {len(cases)} identiques")
    return 1 if wrong else 0


if __name__ == "__main__":
    sys.exit(main())
