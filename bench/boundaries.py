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

Several numbers come out, and they answer different questions:

- **Onset error** says whether the sound is found in the right place. It is
  bounded below by the frame: at 20 ms per row, a perfect network still lands
  within half a frame.
- **Syllable error** says whether the *interval* between two vowel onsets is
  right, which is what a syllable's duration is and what brick 7 will read to
  judge lexical stress. It does not follow from the onset error, and the two
  bound each other from opposite sides: an alignment that slides as a block
  errs on every onset and on no interval, while onsets erring independently
  cost roughly half as much again as one of them. Reported in milliseconds and
  as a share of the interval, since stress is read off proportions within a
  word rather than off absolute durations.
- **Covered duration** says whether the sound is given its extent rather than a
  spike. This is the one peakiness destroys, and the one a label prior is meant
  to restore -- a more direct trial of the prior than counting mass, because it
  measures what the app actually consumes.
- **Held duration** says how long the alignment holds the symbol, in frames of
  its own. Covered duration alone cannot tell a sound held one frame in the
  right place from a sound held its full length in the wrong one, and it is
  bounded by the true durations of TIMIT rather than by the network. Held
  duration is the network's answer with the corpus taken out of it.

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
    """Return [(ipa, start, stop, follows a pause)] for the kept phones of one file.

    Dropped symbols -- closures, pauses, the glottal stop -- are not targets,
    exactly as in training: silence belongs to the blank. A pause is dropped
    like the rest but remembered on the phone that follows it, so that an
    interval stepping over one can be refused: two vowels either side of a
    pause are two syllables of two words, not one duration.
    """
    spans, broken = [], False
    for line in Path(phn).read_text(encoding="utf-8").splitlines():
        start, stop, symbol = line.split()
        if symbol in timit.DROPPED:
            broken = broken or symbol in timit.PAUSES
            continue
        spans.append((timit.FOLD[symbol],
                      int(start) / matrix.SAMPLE_RATE,
                      int(stop) / matrix.SAMPLE_RATE,
                      broken))
        broken = False
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


def syllables(spans, onsets):
    """The error on each syllable duration: the span between two vowel onsets.

    This is where an onset error either cancels or accumulates, and nothing
    but the measurement says which. A network that lays the whole utterance
    down 20 ms early is wrong on every onset and right on every interval; one
    that errs on each onset independently is wrong here by more than it is on
    either end. The judgement of stress rides entirely on this number.
    """
    nuclei = [i for i, span in enumerate(spans) if span[0] in matrix.VOWELS]
    errors = []
    for first, second in zip(nuclei, nuclei[1:]):
        if onsets[first] is None or onsets[second] is None:
            continue
        if any(spans[i][3] for i in range(first + 1, second + 1)):
            continue
        wanted = spans[second][1] - spans[first][1]
        error = abs((onsets[second] - onsets[first]) - wanted)
        errors.append((error, error / wanted))
    return errors


def measure(wav, phn):
    """Return (per-phone rows, syllable errors) for one utterance.

    A row is (onset error, covered fraction, held frames).
    """
    spans = truth(phn)
    if not spans:
        return [], []
    probabilities = matrix.probabilities(wav)
    step = matrix.seconds_per_frame()
    try:
        aligned = matrix.align(probabilities, columns(p for p, _, _, _ in spans))
    except ValueError:
        # Fewer frames than the sequence needs: the utterance cannot be spelled
        # at all. Counted as a failure rather than silently skipped.
        return None

    rows, onsets = [], []
    for (_, start, stop, _), got in zip(spans, aligned):
        if got is None:
            rows.append(None)
            onsets.append(None)
            continue
        first, last = got[0] * step, got[1] * step
        overlap = max(0.0, min(last, stop) - max(first, start))
        rows.append((abs(first - start), overlap / (stop - start), got[1] - got[0]))
        onsets.append(first)
    return rows, syllables(spans, onsets)


def quantile(values, q):
    ordered = sorted(values)
    return ordered[int(q * (len(ordered) - 1))]


def report(name, onsets, covered, held, spans, unplaced, failed, utterances):
    print(f"\n=== {name} — {utterances} énoncés, {len(onsets)} sons placés,"
          f" {len(spans)} syllabes")
    print(f"    départ, erreur médiane   {statistics.median(onsets) * 1000:6.1f} ms"
          f"   (9e décile {quantile(onsets, 0.9) * 1000:6.1f} ms)")
    absolute = [error for error, _ in spans]
    share = [part for _, part in spans]
    print(f"    syllabe, erreur médiane  {statistics.median(absolute) * 1000:6.1f} ms"
          f"   (9e décile {quantile(absolute, 0.9) * 1000:6.1f} ms)")
    print(f"    syllabe, part de sa durée{statistics.median(share) * 100:6.1f} %"
          f"    (9e décile {quantile(share, 0.9) * 100:6.1f} %)")
    print(f"    durée couverte, médiane  {statistics.median(covered) * 100:6.1f} %"
          f"   (1er décile {quantile(covered, 0.1) * 100:6.1f} %)")
    one = sum(1 for f in held if f == 1) / len(held)
    print(f"    tenue, médiane           {statistics.median(held):6.0f} trames"
          f"   ({one * 100:.1f} % sur une seule)")
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

    onsets, covered, held, spans, unplaced, failed = [], [], [], [], 0, 0
    for index, (_, wav, _) in enumerate(every, 1):
        read = measure(wav, Path(wav).with_suffix(".PHN"))
        if read is None:
            failed += 1
            continue
        rows, intervals = read
        spans += intervals
        for row in rows:
            if row is None:
                unplaced += 1
                continue
            onsets.append(row[0])
            covered.append(row[1])
            held.append(row[2])
        if index % 25 == 0:
            print(f"  {index}/{len(every)}", file=sys.stderr, flush=True)

    if not onsets:
        raise SystemExit("aucun son placé — rien à rapporter")
    report(matrix.SLUG, onsets, covered, held, spans, unplaced, failed, len(every))
    return 0


if __name__ == "__main__":
    sys.exit(main())
