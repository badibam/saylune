#!/usr/bin/env python3
"""Step zero of a qualification: is this voice fit to be the yardstick?

The model to imitate is scored by the same engine as the learner, and the mark
is born of the gap between the two. A voice the engine reads badly would charge
the learner with a fault the machine committed -- so before any measurement, the
voice renders the test set, is scored against its own text, and has to come back
nearly perfect.

The bar (`docs/design/engine-qualification.md`): median >= 97 and no phone below
90, on SpeechAce's scale.

The same run answers step 3. Passing `--dialect` against the voice's own accent
must *fail* -- that failure is what proves calibration catches a mismatched
accent instead of letting it invert every later measurement.
"""

import argparse
import statistics
import sys
from pathlib import Path

import engine
import synth
from phrases import CALIBRATION

HERE = Path(__file__).resolve().parent
RENDERS = HERE / "out" / "renders"
READINGS = HERE / "out" / "readings"

MEDIAN_FLOOR = 97.0
PHONE_FLOOR = 90.0


def calibrate(candidate, dialect, force=False, verbose=False):
    """Render and score every phrase; return the phones that came back."""
    phones = []
    for slug, text in CALIBRATION:
        wav = RENDERS / candidate.name / f"{slug}.wav"
        synth.render(text, candidate, wav, force=force)
        cache = READINGS / f"{candidate.name}@{dialect}" / f"{slug}.json"
        if force and cache.is_file():
            cache.unlink()
        reading = engine.score(wav, text, dialect=dialect, cache=cache)
        phones.extend(reading.phones)
        if verbose:
            worst = min(engine.sound(reading.phones),
                        key=lambda p: p.quality, default=None)
            shown = "-" if worst is None else f"{worst.phone} {worst.quality:.0f}"
            print(f"    {slug:<18}{len(reading.phones):>3} phones   worst {shown}")
    return phones


def report(candidate, dialect, phones):
    scored = engine.sound(phones)
    dropped = len(phones) - len(scored)
    if not scored:
        print(f"  {candidate.name} @ {dialect}: no phone came back scored")
        return False

    values = [p.quality for p in scored]
    median = statistics.median(values)
    below = sorted((p for p in scored if p.quality < PHONE_FLOOR),
                   key=lambda p: p.quality)
    passed = median >= MEDIAN_FLOOR and not below

    verdict = "PASS" if passed else "FAIL"
    print(f"  {candidate.name:<18} @ {dialect}   median {median:5.1f}   "
          f"min {min(values):5.1f}   {len(below):>2} under {PHONE_FLOOR:.0f}   "
          f"{len(scored)} phones (-{dropped} dropout)   {verdict}")
    for phone in below[:8]:
        print(f"      {phone.quality:5.1f}  {engine.milliseconds(phone.extent):>4} ms"
              f"  /{phone.phone}/ in {phone.word!r}")
    if len(below) > 8:
        print(f"      ... and {len(below) - 8} more below {PHONE_FLOOR:.0f}")
    return passed


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-c", "--candidate", default=None,
                        help="one voice; default runs the whole field")
    parser.add_argument("-d", "--dialect", default=None,
                        help="override the voice's own dialect (step 3)")
    parser.add_argument("-f", "--force", action="store_true",
                        help="re-render instead of reusing cached audio")
    parser.add_argument("-v", "--verbose", action="store_true")
    args = parser.parse_args(argv)

    if args.candidate is None:
        field = synth.CANDIDATES
    elif args.candidate in synth.BY_NAME:
        field = (synth.BY_NAME[args.candidate],)
    else:
        raise SystemExit(f"Unknown candidate {args.candidate!r}. "
                         "Known: " + ", ".join(synth.BY_NAME))

    print(f"Calibrating {len(field)} voice(s) on {len(CALIBRATION)} phrases "
          f"(median >= {MEDIAN_FLOOR:.0f}, no phone < {PHONE_FLOOR:.0f})\n")
    failures = 0
    for candidate in field:
        dialect = args.dialect or candidate.dialect
        if args.verbose:
            print(f"  {candidate.name} @ {dialect}")
        phones = calibrate(candidate, dialect, args.force, args.verbose)
        if not report(candidate, dialect, phones):
            failures += 1
    return 1 if failures and args.dialect is None else 0


if __name__ == "__main__":
    sys.exit(main())
