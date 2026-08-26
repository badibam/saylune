#!/usr/bin/env python3
"""Where does the network put each sound in time, against a truth we own?

Requirement 1 of the analysis -- locate each sound, position and duration --
has never been measured for the acoustic model. `anchor.py` does not measure
it: that brick reads the letters network, which the choice of acoustic model
does not touch.

TIMIT is annotated phone by phone with sample-accurate boundaries; the corpus
was bought for its phone sequences and its timings were never read. This forces
the expected sequence onto the test split and compares every boundary to the
`.PHN` file beside the audio.

Two numbers come out, and they answer different questions:

- **Onset error** says whether the sound is found in the right place. It is
  bounded below by the frame: at 20 ms per row, a perfect network still lands
  within half a frame.
- **Covered duration** says whether the sound is given its extent rather than a
  spike. This is the one peakiness destroys, and the one a label prior is meant
  to restore -- a more direct trial of the prior than counting mass, because it
  measures what the app actually consumes.

    cd bench && ACOUSTIC_MODEL=timit-ipa python3 boundaries.py
    cd bench && ACOUSTIC_MODEL=v3-pw0.0-e9 python3 boundaries.py --utterances 0

Reads no synthesis and no key: TIMIT audio and the network, nothing else.
"""

import argparse
import statistics
import sys
from pathlib import Path

import matrix

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from train import timit  # noqa: E402  (path has to be set first)


def truth(phn):
    """Return [(ipa, start second, stop second)] for the kept phones of one file.

    Dropped symbols -- closures, pauses, the glottal stop -- are not targets,
    exactly as in training: silence belongs to the blank.
    """
    spans = []
    for line in Path(phn).read_text(encoding="utf-8").splitlines():
        start, stop, symbol = line.split()
        if symbol in timit.DROPPED:
            continue
        spans.append((timit.FOLD[symbol],
                      int(start) / matrix.SAMPLE_RATE,
                      int(stop) / matrix.SAMPLE_RATE))
    return spans


def columns(phones):
    """Map IPA symbols onto matrix columns, failing hard on an unknown one.

    The fine-tuned networks carry vitouphy's vocabulary verbatim, so a miss here
    means the alphabet has drifted -- never something to paper over.
    """
    table = matrix.symbols()
    phones = list(phones)
    missing = sorted({p for p in phones if p not in table})
    if missing:
        raise SystemExit(f"symboles absents du vocabulaire : {' '.join(missing)}")
    return [table.index(p) for p in phones]


def measure(wav, phn):
    """Return per-phone (onset error, covered fraction) for one utterance."""
    spans = truth(phn)
    if not spans:
        return []
    probabilities = matrix.probabilities(wav)
    step = matrix.seconds_per_frame()
    try:
        aligned = matrix.align(probabilities, columns(p for p, _, _ in spans))
    except ValueError:
        # Fewer frames than the sequence needs: the utterance cannot be spelled
        # at all. Counted as a failure rather than silently skipped.
        return None

    rows = []
    for (_, start, stop), got in zip(spans, aligned):
        if got is None:
            rows.append(None)
            continue
        first, last = got[0] * step, got[1] * step
        overlap = max(0.0, min(last, stop) - max(first, start))
        rows.append((abs(first - start), overlap / (stop - start)))
    return rows


def quantile(values, q):
    ordered = sorted(values)
    return ordered[int(q * (len(ordered) - 1))]


def report(name, onsets, covered, unplaced, failed, utterances):
    print(f"\n=== {name} — {utterances} énoncés, {len(onsets)} sons placés")
    print(f"    départ, erreur médiane   {statistics.median(onsets) * 1000:6.1f} ms"
          f"   (9e décile {quantile(onsets, 0.9) * 1000:6.1f} ms)")
    print(f"    durée couverte, médiane  {statistics.median(covered) * 100:6.1f} %"
          f"   (1er décile {quantile(covered, 0.1) * 100:6.1f} %)")
    print(f"    sons sans place dans le treillis : {unplaced}")
    print(f"    énoncés que le treillis refuse   : {failed}")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-n", "--utterances", type=int, default=200,
                        help="how many of the test split to read; 0 reads all")
    parser.add_argument("-s", "--split", default="TEST")
    args = parser.parse_args(argv)

    every = list(timit.utterances(args.split))
    if not every:
        raise SystemExit(f"aucun énoncé sous {timit.CORPUS / args.split}")
    if args.utterances and args.utterances < len(every):
        # Evenly spaced rather than the first N, which would be one dialect
        # region and a handful of speakers.
        stride = len(every) / args.utterances
        every = [every[int(i * stride)] for i in range(args.utterances)]

    onsets, covered, unplaced, failed = [], [], 0, 0
    for index, (_, wav, _) in enumerate(every, 1):
        rows = measure(wav, Path(wav).with_suffix(".PHN"))
        if rows is None:
            failed += 1
            continue
        for row in rows:
            if row is None:
                unplaced += 1
                continue
            onsets.append(row[0])
            covered.append(row[1])
        if index % 25 == 0:
            print(f"  {index}/{len(every)}", file=sys.stderr, flush=True)

    if not onsets:
        raise SystemExit("aucun son placé — rien à rapporter")
    report(matrix.SLUG, onsets, covered, unplaced, failed, len(every))
    return 0


if __name__ == "__main__":
    sys.exit(main())
