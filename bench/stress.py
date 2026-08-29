#!/usr/bin/env python3
"""How much longer is the stressed syllable? The truth alone, no network.

Brick 7 was going to read lexical stress off duration, and the decision was
written before the number so it could not be negotiated in front of it
(`../TODO.md`): if the lengthening clears the instrument's error at the ninth
decile, duration serves as designed; if it does not, brick 7 gets rewritten
around vowel reduction, and duration only corroborates.

This measures the left-hand side of that comparison, and it touches no acoustic
model at all -- TIMIT's boundaries are hand-placed and its dictionary marks the
stressed vowel, so what comes out is the signal an instrument would have to
see. `boundaries.py` measures the other side, on the same utterances.

Two readings of the same words, because the answer moves between them:

- **nucleus to nucleus** is the duration `boundaries.py` measures and the one
  brick 7 would consume -- but the last syllable of a word takes every coda
  consonant with it, so it inflates whichever syllable happens to be last.
- **the nucleus alone** is the vowel's own span, free of that, and it is the
  reading that flatters duration most. It is here as the ceiling: whatever
  duration cannot do on this reading, it cannot do at all.

A nucleus is a vowel or a syllabic consonant -- `able` is two syllables, and
dropping its `el` would silently throw away every word like it.

    cd bench && python3 stress.py
    cd bench && python3 stress.py -n 0      # every utterance of the split
"""

import argparse
import statistics
import sys
from pathlib import Path

import numpy as np
import soundfile as sf

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
from train import timit  # noqa: E402  (path has to be set first)

SAMPLE_RATE = 16000
DICTIONARY = timit.CORPUS / "DOC" / "TIMITDIC.TXT"

# Nuclei on TIMIT's own symbols rather than the folded IPA: the folding sends
# the syllabic consonants to their plain counterparts (`el` to `l`), which is
# right for a phone inventory and wrong for counting syllables.
VOWELS = frozenset("aa ae ah ao aw ax ax-h axr ay eh er ey ih ix iy ow oy "
                   "uh uw ux".split())
SYLLABIC = frozenset("el em en eng".split())
NUCLEI = VOWELS | SYLLABIC

# Function words are excluded because their whole syllable structure is subject
# to reduction, which is a different phenomenon from the one being measured and
# would sit in the numbers as noise. The list is short and blunt on purpose: it
# never leaves the bench, and a word wrongly kept costs one sample.
FUNCTION = frozenset("""
a an the this that these those and or but nor so yet for of to in on at by
with from into onto upon about above below under over between through during
is are was were be been being am do does did done have has had having
will would shall should can could may might must
i you he she it we they me him her us them my your his its our their
there here what which who whom whose when where why how
not no yes if then than as too very just only also even still
""".split())


# The vowels English reduces to. TIMIT tells them apart from their full
# counterparts, which is what makes this measurable at all; the syllabic
# consonants carry no vowel and are unstressed by construction.
REDUCED = frozenset("ax ax-h ix axr".split()) | SYLLABIC

# The same question in two inventories that are ours, and they are not the same
# one. The vocabulary the model in service writes holds `ə` beside `ʌ` and `ɚ`
# beside `ɝ`, so it keeps the reduced vowel apart from its full counterpart.
# Our own fine-tuning targets Lee & Hon's 39 classes, which merge both pairs
# and leave only the schwa standing. Neither tells `ix` from `ih`, so `ɪ` --
# reduced in `roses`, full in `sink` -- says nothing either way.
SPOKEN = {"ah": "ʌ", "ax": "ə", "ax-h": "ə", "er": "ɝ", "axr": "ɚ"}
SPOKEN_REDUCED = frozenset(["ə", "ɚ"]) | SYLLABIC
FOLDED_REDUCED = frozenset(["ə"]) | SYLLABIC


def folded(symbol):
    from train.timit import FOLD
    return FOLD.get(symbol, symbol)


def spoken_as(symbol):
    """The symbol as the vocabulary in service writes it."""
    if symbol in SPOKEN:
        return SPOKEN[symbol]
    return folded(symbol)


def dictionary():
    """word -> the stress of each of its nuclei, read from TIMITDIC.

    A stress is the digit TIMIT attaches to the nucleus: 1 primary, 2
    secondary, absent unstressed. Only the positions are kept -- what the
    nucleus *is* never enters this measure.
    """
    table = {}
    for line in DICTIONARY.read_text(encoding="latin-1").splitlines():
        if line.startswith(";") or "/" not in line:
            continue
        word, _, rest = line.partition("/")
        stresses = []
        for phone in rest.rstrip("/ ").split():
            digit = phone[-1] if phone[-1].isdigit() else ""
            if (phone[:-1] if digit else phone) in NUCLEI:
                stresses.append(int(digit) if digit else 0)
        if stresses:
            table[word.strip()] = stresses
    return table


def words(wrd):
    for line in Path(wrd).read_text(encoding="utf-8").splitlines():
        start, stop, text = line.split()
        yield text, int(start) / SAMPLE_RATE, int(stop) / SAMPLE_RATE


def phones(phn):
    rows = []
    for line in Path(phn).read_text(encoding="utf-8").splitlines():
        start, stop, symbol = line.split()
        rows.append((symbol, int(start) / SAMPLE_RATE, int(stop) / SAMPLE_RATE))
    return rows


def durations(rows, start, stop):
    """One word's syllables, both readings, on the hand-placed boundaries.

    Returns None when the word carries a pause: a duration that steps over one
    is not a syllable, and nothing here is worth guessing at.
    """
    inside = [row for row in rows if row[1] >= start and row[2] <= stop]
    if any(symbol in timit.PAUSES for symbol, _, _ in inside):
        return None
    nuclei = [row for row in inside if row[0] in NUCLEI]
    if not nuclei:
        return None
    edges = [begin for _, begin, _ in nuclei] + [stop]
    spans = [second - first for first, second in zip(edges, edges[1:])]
    own = [end - begin for _, begin, end in nuclei]
    return spans, own, nuclei


def audio(wav):
    """The samples of one utterance. TIMIT's .WAV files are NIST SPHERE."""
    import matrix  # imported here: most of this brick runs with no network
    matrix.heard(wav)
    samples, rate = sf.read(wav, dtype="float64")
    if rate != SAMPLE_RATE:
        raise SystemExit(f"{wav} : {rate} Hz, le banc lit du {SAMPLE_RATE} Hz")
    return samples


def loudness(samples, nuclei):
    """The mean power of each nucleus -- the other thing stress is said to do.

    A stressed syllable is said louder, which no measure of ours has ever
    looked at. Taken raw: the pick is a maximum *within one word*, so whatever
    the recording level was cancels on its own and normalising would only
    invent a step.

    What does not cancel is that vowels are not equally loud to begin with --
    an open `ɑ` carries more power than a close `i` whatever the stress. That
    sits in this number and nothing here removes it.
    """
    out = []
    for _, begin, end in nuclei:
        segment = samples[int(begin * SAMPLE_RATE):int(end * SAMPLE_RATE)]
        out.append(float(np.sqrt(np.mean(segment ** 2))) if len(segment)
                   else 0.0)
    return out


def rates(spans, mark):
    """The stressed syllable against its rivals: the mean, then the longest."""
    here = spans[mark]
    others = [span for rank, span in enumerate(spans) if rank != mark]
    if not others or min(others) <= 0:
        return None
    return here / statistics.mean(others), here / max(others)


def picked(nuclei, spans, reduced):
    """Which syllable a rule reading reduction alone would call the strong one.

    Returns the rank it picks, or None when it cannot: no full vowel at all, or
    several, in which case duration is asked to break the tie -- which is the
    corroborating role the measure above left it, tried here rather than
    assumed. The two answers are kept apart so the tie-break can be priced.
    """
    full = [rank for rank, symbol in enumerate(nuclei) if symbol not in reduced]
    if len(full) == 1:
        return full[0], "seule"
    if not full:
        return None, "aucune"
    # A tie between nuclei that are the same vowel is the honest trial of
    # loudness: whatever an open vowel carries over a close one carries equally
    # on both sides and cancels, so what is left is stress alone.
    same = len({nuclei[rank] for rank in full}) == 1
    return (max(full, key=lambda rank: spans[rank]),
            "départagée, même voyelle" if same else "départagée")


def measured(utterance, table):
    """Per word: the two readings, and whether the stress falls last."""
    phn = Path(utterance).with_suffix(".PHN")
    wrd = Path(utterance).with_suffix(".WRD")
    if not (phn.is_file() and wrd.is_file()):
        return [], 0
    rows = phones(phn)
    samples = audio(Path(utterance))
    found, skipped = [], 0
    for text, start, stop in words(wrd):
        stresses = table.get(text)
        if stresses is None or len(stresses) < 2 or 1 not in stresses:
            continue
        if text in FUNCTION:
            continue
        read = durations(rows, start, stop)
        if read is None:
            continue
        spans, own, nuclei_rows = read
        # The realisation dropped or added a nucleus: nothing lines the two
        # sequences up any more, and guessing which one went missing would be
        # inventing the measurement.
        if len(spans) != len(stresses):
            skipped += 1
            continue
        mark = stresses.index(1)
        between, inside = rates(spans, mark), rates(own, mark)
        if between is None or inside is None:
            continue
        nuclei = [symbol for symbol, _, _ in nuclei_rows]
        power = loudness(samples, nuclei_rows)
        seen = [spoken_as(symbol) for symbol in nuclei]
        reduction = {
            ("timit", "durée"): picked(nuclei, own, REDUCED),
            ("en service", "durée"): picked(seen, own, SPOKEN_REDUCED),
            ("en service", "intensité"): picked(seen, power, SPOKEN_REDUCED),
            ("notre repli", "durée"): picked([folded(s) for s in nuclei], own,
                                             FOLDED_REDUCED),
        }
        found.append((between, inside, mark == len(stresses) - 1,
                      mark, reduction))
    return found, skipped


def shared(table):
    """The rule's pick for every reading of the two sentences all speakers read.

    Two families of TIMIT sentences are read by more than one mouth. Every
    speaker reads `SA1` and `SA2` -- around 630 readings each, but only four
    content words of two syllables between them. Each of the 450 `SX` sentences
    is read by seven speakers, which is where the breadth is. The rest of the
    bench throws the `SA` pair out, since they skew the phone distribution and
    cross the two halves; nothing is trained here, and "the same words in the
    same places, by different mouths" is the one thing the corpus gives nowhere
    else.

    Keyed by sentence, position and spelling, so what is compared is one word
    against itself.
    """
    picks = {}
    for phn in sorted(list(timit.CORPUS.rglob("SA*.PHN"))
                      + list(timit.CORPUS.rglob("SX*.PHN"))):
        wrd = phn.with_suffix(".WRD")
        if not wrd.is_file():
            continue
        rows = phones(phn)
        samples = audio(phn.with_suffix(".WAV"))
        for rank, (text, start, stop) in enumerate(words(wrd)):
            stresses = table.get(text)
            if stresses is None or len(stresses) < 2 or 1 not in stresses:
                continue
            if text in FUNCTION:
                continue
            read = durations(rows, start, stop)
            if read is None:
                continue
            spans, own, nuclei_rows = read
            if len(spans) != len(stresses):
                continue
            seen = [spoken_as(symbol) for symbol, _, _ in nuclei_rows]
            power = loudness(samples, nuclei_rows)
            for arbitrator, weights in (("la durée", own), ("l'intensité", power)):
                guess, _ = picked(seen, weights, SPOKEN_REDUCED)
                if guess is None:
                    continue
                picks.setdefault(arbitrator, {}).setdefault(
                    (phn.stem, rank, text), []).append(guess)
    return picks


def disagreement(picks):
    """How often two readings of one word land the rule on different syllables.

    Every one of these speakers is a native reading correctly, so a
    disagreement here is a mark the app would paint on speech that carries no
    fault -- brick 7's false alarm, measured where nothing is wrong.

    Read on TIMIT's own hand-made transcriptions, so it is what the *rule*
    costs with a perfect reading of the audio. The network adds its own
    disagreement on top, and that is not measured here.
    """
    print(f"\n    la règle relue : deux lectures du même mot la posent-elles "
          f"au même endroit")
    for arbitrator, table in picks.items():
        rates, pooled_pairs, pooled_clashes = [], 0, 0
        for (_, _, text), guesses in table.items():
            pairs = len(guesses) * (len(guesses) - 1) // 2
            if not pairs:
                continue
            counts = {}
            for guess in guesses:
                counts[guess] = counts.get(guess, 0) + 1
            agree = sum(n * (n - 1) // 2 for n in counts.values())
            pooled_pairs += pairs
            pooled_clashes += pairs - agree
            rates.append(((pairs - agree) / pairs, len(guesses), text))
        if not rates:
            continue
        # Averaged over words and not over pairs: the two sentences every
        # speaker reads carry four words between them and 97% of the pairs, so
        # a pooled rate would be those four words wearing the corpus as a
        # disguise.
        shares = [share for share, _, _ in rates]
        steady = sum(1 for share in shares if share == 0)
        solid = [share for share, readings, _ in rates if readings >= 7]
        print(f"      départagée par {arbitrator} : {len(rates)} mots, "
              f"**{100 * statistics.mean(shares):.1f} % de désaccord moyen**"
              f"   —   {100 * steady / len(rates):.0f} % de mots qui ne "
              f"bougent jamais")
        if solid:
            print(f"      {'':<24}sur les {len(solid)} mots lus 7 fois ou "
                  f"plus : {100 * statistics.mean(solid):.1f} %")


# What the network writes when a vowel has lost its colour. The two are read
# as a quantity and not as a label: a vowel halfway to a schwa keeps its peak
# elsewhere and still lays mass here, which is exactly what the categorical
# test throws away.
CENTRAL = ("ə", "ɚ")


def central(spread, names):
    """How much of one nucleus's shape sits on the reduced vowels."""
    return sum(float(spread[rank]) for rank, name in enumerate(names)
               if name in CENTRAL)


def read_matrix(wav, uid):
    """The network's reading of one TIMIT utterance, cached like every other."""
    import matrix
    import overlap
    return matrix.probabilities(
        wav, cache=overlap.MATRICES / "timit-shared" / f"{uid}.npz")


def shapes(wav, uid, nuclei):
    """The central mass under each nucleus, on the hand-placed spans.

    The spans come from TIMIT and not from the network: what is being tried is
    whether the *shape* separates the syllables, not whether the network also
    finds them, which `boundaries.py` measures on its own.
    """
    import matrix
    import overlap

    spread = read_matrix(wav, uid)
    names = [matrix.symbols()[column] for column in matrix.spoken()]
    step = matrix.seconds_per_frame()
    out = []
    for _, begin, end in nuclei:
        span = (int(begin / step), max(int(end / step), int(begin / step) + 1))
        shape, _ = overlap.spread(spread, span)
        out.append(None if shape is None else central(shape, names))
    return out


def repeated(limit):
    """The `SX` sentences read by seven mouths, `limit` of them."""
    families = {}
    for phn in sorted(timit.CORPUS.rglob("SX*.PHN")):
        families.setdefault(phn.stem, []).append(phn)
    full = [phn for stem in sorted(families, key=lambda s: int(s[2:]))
            for phn in families[stem] if len(families[stem]) >= 2]
    stems, kept = [], []
    for phn in full:
        if phn.stem not in stems:
            if len(stems) >= limit:
                continue
            stems.append(phn.stem)
        kept.append(phn)
    return kept


def centralisation(limit):
    """All three arbitrators on one base, the network's reading included.

    The two above are read off TIMIT's transcription alone; this one needs the
    network, so everything is recomputed on the same sample rather than set
    beside numbers taken on another one.
    """
    table = dictionary()
    files = repeated(limit)
    print(f"\n=== la centralisation — {len(files)} lectures de "
          f"{len({phn.stem for phn in files})} phrases partagées")

    cases, picks = [], {}
    for index, phn in enumerate(files, 1):
        wrd = phn.with_suffix(".WRD")
        wav = phn.with_suffix(".WAV")
        if not (wrd.is_file() and wav.is_file()):
            continue
        rows = phones(phn)
        samples = audio(wav)
        uid = "-".join(phn.parts[-3:]).removesuffix(".PHN")
        masses = None
        for rank, (text, start, stop) in enumerate(words(wrd)):
            stresses = table.get(text)
            if stresses is None or len(stresses) < 2 or 1 not in stresses:
                continue
            if text in FUNCTION:
                continue
            read = durations(rows, start, stop)
            if read is None:
                continue
            spans, own, nuclei_rows = read
            if len(spans) != len(stresses):
                continue
            if masses is None:
                masses = shapes(wav, uid, [row for row in rows
                                           if row[0] in NUCLEI])
            here = [masses[i] for i, row in
                    enumerate([r for r in rows if r[0] in NUCLEI])
                    if start <= row[1] and row[2] <= stop]
            if len(here) != len(nuclei_rows) or any(m is None for m in here):
                continue
            seen = [spoken_as(symbol) for symbol, _, _ in nuclei_rows]
            mark = stresses.index(1)
            trials = {
                "durée": picked(seen, own, SPOKEN_REDUCED),
                "intensité": picked(seen, loudness(samples, nuclei_rows),
                                    SPOKEN_REDUCED),
                # Least central wins, so the weight is the mass negated.
                "centralisation": picked(seen, [-m for m in here],
                                         SPOKEN_REDUCED),
                # No label at all: the shape decides on its own, every nucleus
                # in the running.
                "la forme seule": (min(range(len(here)), key=lambda i: here[i]),
                                   "sans étiquette"),
            }
            cases.append((mark, trials))
            for name, (guess, _) in trials.items():
                picks.setdefault(name, {}).setdefault(
                    (phn.stem, rank, text), []).append(guess)
        if index % 100 == 0:
            print(f"  {index}/{len(files)}", file=sys.stderr, flush=True)

    if not cases:
        raise SystemExit("aucun mot mesurable")
    print(f"    {len(cases)} mots pleins de deux syllabes ou plus\n")
    for name in ("durée", "intensité", "centralisation", "la forme seule"):
        right = sum(1 for mark, trials in cases if trials[name][0] == mark)
        split = [(mark, trials) for mark, trials in cases
                 if trials[name][1].startswith("départagée")]
        won = sum(1 for mark, trials in split if trials[name][0] == mark)
        detail = (f"   —   sur les {len(split)} à départager : "
                  f"{100 * won / len(split):.1f} %") if split else ""
        print(f"      {name:<16}{100 * right / len(cases):5.1f} % justes{detail}")
    disagreement(picks)


def quantile(values, q):
    return sorted(values)[int(q * (len(values) - 1))]


def line(name, values):
    print(f"      {name:<26}médiane {statistics.median(values):5.2f}×"
          f"   1er décile {quantile(values, 0.1):5.2f}×"
          f"   9e décile {quantile(values, 0.9):5.2f}×"
          f"   plus longue {100 * sum(1 for v in values if v > 1) / len(values):5.1f} %")


def report(found, skipped, utterances):
    print(f"\n=== {utterances} énoncés, {len(found)} mots pleins de deux "
          f"syllabes ou plus")
    print(f"    {skipped} écartés : la réalisation n'a pas le nombre de "
          f"noyaux du dictionnaire")
    final = sum(1 for row in found if row[2])
    print(f"    accent sur la dernière syllabe dans "
          f"{100 * final / len(found):.1f} % des mots")
    for rank, reading in ((0, "noyau à noyau"), (1, "noyau seul")):
        print(f"\n    {reading}")
        line("contre la moyenne", [row[rank][0] for row in found])
        line("contre la plus longue", [row[rank][1] for row in found])

    print("\n    la réduction vocalique : la syllabe forte est celle qui "
          "n'est pas réduite")
    for inventory in (("timit", "durée"), ("en service", "durée"),
                      ("en service", "intensité"), ("notre repli", "durée")):
        cases = [(row[4][inventory], row[3]) for row in found]
        right = sum(1 for (guess, _), mark in cases if guess == mark)
        alone = [(guess, why, mark) for (guess, why), mark in cases
                 if why == "seule"]
        split = [(guess, why, mark) for (guess, why), mark in cases
                 if why.startswith("départagée")]
        twins = [(guess, why, mark) for (guess, why), mark in cases
                 if why == "départagée, même voyelle"]
        none = sum(1 for (_, why), _ in cases if why == "aucune")
        name = f"{inventory[0]} / {inventory[1]}"
        print(f"      {name:<24}{100 * right / len(cases):5.1f} % justes"
              f"   —   une seule voyelle pleine : {len(alone)} mots, "
              f"{100 * sum(1 for g, _, m in alone if g == m) / max(len(alone), 1):.1f} % justes")
        print(f"      {'':<24}départagée : {len(split)} mots, "
              f"{100 * sum(1 for g, _, m in split if g == m) / max(len(split), 1):.1f} % justes"
              f"   —   aucune voyelle pleine : {none} mots")
        print(f"      {'':<24}dont la même voyelle des deux côtés : "
              f"{len(twins)} mots, "
              f"{100 * sum(1 for g, _, m in twins if g == m) / max(len(twins), 1):.1f} % justes")


def audios():
    """The ledger line, asked of the matrix module without importing it above:
    most of this brick runs on hand-placed boundaries and no network."""
    import matrix
    return matrix.audios()


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-n", "--utterances", type=int, default=200,
                        help="how many of the split to read; 0 reads all")
    parser.add_argument("-s", "--split", default="TEST")
    parser.add_argument("-m", "--matrix", type=int, default=0, metavar="N",
                        help="lire aussi la centralisation, sur N phrases "
                             "partagées — demande le réseau")
    args = parser.parse_args(argv)

    if args.matrix:
        centralisation(args.matrix)
        print(f"\n{audios()}")
        return 0

    every = list(timit.utterances(args.split))
    if not every:
        raise SystemExit(f"aucun énoncé sous {timit.CORPUS / args.split}")
    if args.utterances and args.utterances < len(every):
        # The same evenly spaced draw as `boundaries.py`, so the two sides of
        # the comparison are read off the same utterances.
        stride = len(every) / args.utterances
        every = [every[int(i * stride)] for i in range(args.utterances)]

    table = dictionary()
    found, skipped = [], 0
    for _, wav, _ in every:
        rows, missed = measured(wav, table)
        found += rows
        skipped += missed
    if not found:
        raise SystemExit("aucun mot mesurable — le dictionnaire n'a rien rendu")
    report(found, skipped, len(every))
    disagreement(shared(table))
    print(f"\n{audios()}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
