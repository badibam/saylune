#!/usr/bin/env python3
"""The learner's own sounds, put on the letters of the text.

This asks of the learner's recording exactly what the bench already asks of the
model's -- which letters does each sound you produced write? -- and the words
fall out of the answer. Nothing new is computed: `join.joined` takes an audio and
a text and does this, and pointing it at `said.wav` places the learner.

The letters then hold both readings at once, which is what lets them face each
other without a clock: the model's contracted `ɝ` covers `ou're`, and the learner
who does not contract puts a `ʊ` on the `o` and a `ɹ` on the `r`, in the same
word. **A sound no letter of any word can write, at its place in the order,
belongs to no word** -- that is what matter said in addition looks like, and it
is the whole detection. No duration is read anywhere.

Two jobs in one file, on purpose. `found` is what `turn.py` writes into a turn
and what `Added.kt` mirrors on the device; `report` shows the two readings side
by side, which is how the cut was judged in the first place and how it stays
judgeable.

    python3 placed.py <said.wav> <turn.json> ...        (by pairs)

The take file is read for the text and for the model's own sounds, so the two
readings can be shown side by side. Nothing else of it is used.
"""

import json
import re
import sys
from pathlib import Path

import join


def found(sounds, said, gaps):
    """Every stretch the learner said that belongs to no word -- what `turn.json`
    carries and what the screen draws a wedge for.

    A run of neighbouring loose sounds is **one** mark and not one each: a whole
    word said in addition is a burst of them at one word boundary, and it is one
    thing that happened. Its place is the seam just after the last letter any of
    the learner's sounds claimed, the way a gutter's is.

    `gaps` says which sounds of the model's grid were compared, so a mark can
    name the readout line it follows. `Added.kt` is the same thing in Kotlin, and
    the port test holds the two against each other.
    """
    out, run, opened = [], [], -1
    # Where the last letter the learner claimed was. A borrowed letter belongs to
    # the neighbour that took it, so it does not move this -- the same rule the
    # gutters already keep on the model's side.
    claimed = -1
    for sound in said:
        if not (sound.spots or sound.borrowed):
            if not run:
                opened = claimed
            run.append(sound.symbol)
            continue
        if run:
            out.append(mark(sounds, gaps, opened, run))
            run = []
        if sound.spots:
            claimed = max(sound.spots)
    if run:
        out.append(mark(sounds, gaps, opened, run))
    return out


def mark(sounds, gaps, after, run):
    """One loose stretch, placed on the text and on the readout.

    `afterSound` is the last compared sound whose own letters end at or before
    the seam, and -1 before the first of them. Sounds holding no letter are
    stepped over rather than counted: they say nothing about where in the text
    they sit, so a mark lands before them.
    """
    line = -1
    for index, gap in enumerate(gaps):
        spots = sounds[gap.rank].spots
        if spots and max(spots) <= after:
            line = index
    return {"symbol": " ".join(run), "after": after, "afterSound": line}


def words_of(text, sounds):
    """Each word of the text, and the model sounds that spell it.

    Reads the serialised sounds of a `turn.json`, where a sound carries the
    letters it covers rather than their offsets -- so the word is found by
    walking the text forward, never by searching it from the start.
    """
    spans = [(m.start(), m.end(), m.group()) for m in re.finditer(r"\S+", text)]
    owner, cursor = [], 0
    for sound in sounds:
        letters = sound["letters"]
        if not letters or sound.get("borrowed"):
            owner.append(None)
            continue
        at = text.find(letters, cursor)
        if at < 0:
            owner.append(None)
            continue
        cursor = at + len(letters)
        owner.append(next((i for i, (a, b, _) in enumerate(spans) if a <= at < b), None))
    grouped = [[] for _ in spans]
    for rank, word in enumerate(owner):
        if word is not None:
            grouped[word].append(rank)
    return spans, grouped


def placed(wav, text, cache=None):
    """Each word of the text, and the learner's sounds that landed on its letters,
    plus the sounds that landed on no word at all."""
    spans = [(m.start(), m.end(), m.group()) for m in re.finditer(r"\S+", text)]
    theirs, loose = {i: [] for i in range(len(spans))}, []
    for sound in join.joined(Path(wav), text, cache=cache):
        where = sound.spots or sound.borrowed
        rank = None
        if where:
            at = min(where)
            rank = next((i for i, (a, b, _) in enumerate(spans) if a <= at < b), None)
        (theirs[rank] if rank is not None else loose).append(sound.symbol)
    return spans, theirs, loose


def report(wav, path):
    take = json.loads(Path(path).read_text(encoding="utf-8"))
    text, sounds = take["text"], take["sounds"]
    _, grouped = words_of(text, sounds)
    model = [[sounds[r]["symbol"] for r in ranks] for ranks in grouped]
    spans, theirs, loose = placed(wav, text)

    print(f"== {Path(path).parent.name if Path(path).name == 'turn.json' else Path(path).stem}"
          f"  {text!r}")
    print("   %-14s %-20s %s" % ("mot", "modèle", "apprenant, sur les mêmes lettres"))
    for rank, (_, _, word) in enumerate(spans):
        print("   %-14s %-20s %s" % (
            word, " ".join(model[rank]), " ".join(theirs[rank]) or "—"))
    if loose:
        print("   %-14s %-20s %s" % ("(hors mot)", "—", " ".join(loose)))
    print()


if __name__ == "__main__":
    args = sys.argv[1:]
    if len(args) % 2:
        raise SystemExit("des paires : <said.wav> <turn.json> ...")
    for wav, path in zip(args[::2], args[1::2]):
        report(wav, path)
