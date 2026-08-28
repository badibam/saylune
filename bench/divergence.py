#!/usr/bin/env python3
"""Which model hears what was really said, where two models disagree.

The criterion for an acoustic model is fidelity: rendering the sounds actually
produced. It is not the same as rendering a *plausible* sequence -- a model
trained on dictionary alignments renders an excellent one, because it guesses
the sound the word ought to hold, and that model is precisely the one blind to
the substitution the app exists to report.

Fidelity cannot be marked against a canonical transcription for that reason,
and no ground truth exists for a synthesised voice. But two models agree most
of the time, so only their disagreements have to be heard. That is what this
does: it aligns two readings of the same audio in time, keeps the places where
they name a different sound, plays the **whole word** -- sixty milliseconds are
not judgeable alone, the lesson of `review.py` -- and counts who was right.

Sounds that are simply *missing* need no ear: `syllables.py` counts them.

    python3 divergence.py -r timit-ipa -r v3-pw0.1-e29 -n     # combien, et où
    python3 divergence.py -r timit-ipa -r v3-pw0.1-e29        # les juger
    python3 divergence.py -r timit-ipa -r v3-pw0.1-e29 -t     # sur les prises

Neither reading is ever computed here. A reading is a way of calculating the
matrix, and filling one from another would compare a model with itself; a
missing or stale cache is fatal, and says which script fills it.

Verdicts land in `divergences/<a>--<b>.json`, versioned, and the same command
resumes where it stopped: they are the same matter as `expected.py` and
`reviews/`, written once by ear and regenerable by nothing.
"""

import argparse
import json
from pathlib import Path

import numpy as np

import join
import matrix
import phrases
import review

HERE = Path(__file__).resolve().parent
TAKES = HERE / "out" / "takes" / "set"
RENDERS = HERE / "out" / "renders"
MATRICES = HERE / "out" / "matrices"
VERDICTS = HERE / "divergences"

# Two peaks of the same sound rarely overlap: CTC gives each symbol one or two
# frames, so two readings place the same sound side by side as often as on top
# of each other. Time still tells them apart, but as a distance between middles
# rather than a shared stretch, and this is how far apart two peaks may sit and
# still be one sound.
NEAR = 0.08

# Aligning the two grids: naming the same sound is worth most, naming another
# sound at the same moment is still an answer to the same question, and leaving
# a sound unfaced costs -- otherwise every substitution would read as one
# insertion plus one deletion.
SAME, OTHER, ALONE = 2.0, 0.5, -0.6

ANSWERS = {"a": None, "b": None, "n": "aucun", "?": "incertain"}


def reading(wav, name, tag, slug):
    """One reading's matrix, from its own cache, never computed here."""
    cache = MATRICES / name / tag / f"{slug}.npz"
    if not cache.is_file():
        return None
    held = np.load(cache)
    if matrix.stale(wav, held):
        raise SystemExit(f"{cache} a été calculé sur un autre audio que {wav} "
                         f"— la lecture {name} est périmée")
    return held["probabilities"]


def sounded(probabilities):
    """A reading's grid: each sound with its stretch in seconds."""
    step = matrix.seconds_per_frame()
    table = matrix.symbols()
    return [(table[index], start * step, stop * step)
            for index, start, stop in matrix.grid(probabilities)]


def paired(one, two):
    """The two grids laid side by side, sound facing sound.

    A global alignment rather than an overlap: both readings walk the same
    audio, so their order is the same and only what they name differs. Time
    enters as the distance between two middles, which is what keeps a repeated
    symbol from facing the wrong occurrence.
    """
    def score(i, j):
        if one[i][0] == two[j][0]:
            return SAME
        middle = abs((one[i][1] + one[i][2]) / 2 - (two[j][1] + two[j][2]) / 2)
        return OTHER if middle <= NEAR else 2 * ALONE

    best = np.full((len(one) + 1, len(two) + 1), -np.inf)
    best[0, 0] = 0.0
    for i in range(len(one) + 1):
        for j in range(len(two) + 1):
            if i:
                best[i, j] = max(best[i, j], best[i - 1, j] + ALONE)
            if j:
                best[i, j] = max(best[i, j], best[i, j - 1] + ALONE)
            if i and j:
                best[i, j] = max(best[i, j], best[i - 1, j - 1] + score(i - 1, j - 1))

    facing, i, j = [], len(one), len(two)
    while i or j:
        if i and j and best[i, j] == best[i - 1, j - 1] + score(i - 1, j - 1):
            facing.append((i - 1, j - 1))
            i, j = i - 1, j - 1
        elif i and best[i, j] == best[i - 1, j] + ALONE:
            facing.append((i - 1, None))
            i -= 1
        else:
            facing.append((None, j - 1))
            j -= 1
    return list(reversed(facing))


def disagreements(grids, words):
    """Where the two readings do not name the same sound.

    Two shapes, and only the first is played: a sound both readings have and
    name differently. A sound one reading has alone is a missing sound, which
    `syllables.py` already counts without an ear.
    """
    first, second = grids
    found = []
    for rank, other in paired(first, second):
        if rank is None:
            found.append(("seul", None, other, "—", second[other][0],
                          words[1][other]))
        elif other is None:
            found.append(("seul", rank, None, first[rank][0], "—",
                          words[0][rank]))
        elif first[rank][0] != second[other][0]:
            found.append(("autre", rank, other, first[rank][0],
                          second[other][0], words[0][rank]))
    return found


def grouped(symbols, text):
    """Each sound's word, cut by spelling alone -- no clock, like the join."""
    spoken = join.spoken(text)
    spelled = [(word, [text[position] for position in positions])
               for word, positions in spoken]
    cuts = join.partition(spelled, symbols)
    held = ["—"] * len(symbols)
    for rank, (word, _) in enumerate(spoken):
        for index in range(cuts[rank], cuts[rank + 1]):
            held[index] = word
    return held


def stretch(grid, words, rank, word):
    """The word's stretch in the audio: its first sound to its last."""
    inside = [index for index, held in enumerate(words) if held is word]
    if not inside:
        return grid[rank][1], grid[rank][2]
    return grid[min(inside)][1], grid[max(inside)][2]


def material(takes):
    """What to read: the model's renders, or the takes, with their text."""
    texts = dict(phrases.CALIBRATION + phrases.HELDOUT)
    if not takes:
        for slug, text in phrases.CALIBRATION + phrases.HELDOUT:
            wav = RENDERS / "eleven-us-eric" / "sentences" / f"{slug}.wav"
            if wav.is_file() and not join.unspellable(text):
                yield slug, wav, text, "sentences-eleven-us-eric", slug
        return
    import faults
    # A take answers for several sounds and appears once per case; it is one
    # recording, and it is read once.
    seen = set()
    for take, model, _, _, _ in faults.CASES:
        if take in seen:
            continue
        seen.add(take)
        wav = TAKES / f"{take}.wav"
        if wav.is_file() and not join.unspellable(texts[model]):
            # The tag `faults.py` files a take's matrix under.
            yield take, wav, texts[model], f"set-{take}", model


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-r", "--reading", action="append", required=True,
                        help="une lecture, deux fois (a puis b)")
    parser.add_argument("-t", "--takes", action="store_true",
                        help="les prises humaines au lieu des rendus")
    parser.add_argument("-n", "--dry-run", action="store_true",
                        help="compter et situer, sans rien jouer")
    parser.add_argument("-p", "--pad", type=float, default=review.PAD)
    options = parser.parse_args(argv)
    if len(options.reading) != 2:
        raise SystemExit("deux lectures, ni plus ni moins : -r <a> -r <b>")
    first, second = options.reading

    VERDICTS.mkdir(exist_ok=True)
    book = VERDICTS / f"{first}--{second}.json"
    given = json.loads(book.read_text(encoding="utf-8")) if book.is_file() else {}

    chosen, seen, alone, absent = None, 0, 0, []
    for slug, wav, text, tag, name in material(options.takes):
        held = [reading(wav, one, tag, name) for one in (first, second)]
        if any(spread is None for spread in held):
            absent.append(slug)
            continue
        grids = [sounded(spread) for spread in held]
        words = [grouped([symbol for symbol, _, _ in grid], text)
                 for grid in grids]
        rows = disagreements(grids, words)
        if not rows:
            continue
        heard = [row for row in rows if row[0] == "autre"]
        alone += len(rows) - len(heard)
        seen += len(heard)
        print(f"\n{text}   ({slug})")
        for kind, rank, other, one, two, word in rows:
            mark = "seul" if kind == "seul" else ""
            print(f"  {word:<14}{one:>4} | {two:<4}  {mark}")
        if options.dry_run:
            continue
        for kind, rank, other, one, two, word in heard:
            key = f"{slug}:{rank}"
            if key in given:
                continue
            low, high = stretch(grids[0], words[0], rank, word)
            print(f"\n  mot « {word} » — {first} entend {one}, "
                  f"{second} entend {two}")
            while True:
                chosen = review.play(wav, low, high, chosen, options.pad)
                answer = review.ask(f"  a={first}  b={second}  "
                                    f"n=aucun  ?=incertain  r=réécouter  q  ")
                if answer is None or answer == "q":
                    book.write_text(json.dumps(given, ensure_ascii=False,
                                               indent=2), encoding="utf-8")
                    return 0
                if answer == "r":
                    continue
                if answer in ANSWERS:
                    given[key] = {"mot": word, first: one, second: two,
                                  "juste": {"a": first, "b": second}.get(
                                      answer, ANSWERS[answer])}
                    book.write_text(json.dumps(given, ensure_ascii=False,
                                               indent=2), encoding="utf-8")
                    break

    if absent:
        print(f"\n  {len(absent)} audios qu'une des deux lectures ne porte pas, "
              f"laissés de côté : {', '.join(absent)}")
    print(f"\n  {seen} désaccords à juger, {alone} sons qu'une seule lecture "
          f"porte (comptés par syllables.py, jamais joués)")
    if given:
        tally = {}
        for entry in given.values():
            tally[entry["juste"]] = tally.get(entry["juste"], 0) + 1
        print(f"  jugés : " + "  ".join(f"{who} {count}"
                                        for who, count in sorted(tally.items())))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
