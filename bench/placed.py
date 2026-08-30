#!/usr/bin/env python3
"""The learner's own sounds, put on the letters of the text. **The retained way.**

`anchor.py` cuts the learner's sounds against the model's *sounds*, and no amount
of scoring makes that cut right: a fuller reading of a word and a word said in
addition have the same shape in symbols, and time does not separate them either.
This does not cut against the model at all. It asks of the learner's recording
exactly what the bench already asks of the model's -- which letters does each
sound you produced write? -- and the words fall out of the answer.

Nothing new is written here. `join.joined` takes an audio and a text and does
this; the bench has only ever pointed it at `model.wav`. Pointed at `said.wav` it
places the learner. The letters then hold both readings at once, which is what
lets them face each other without a clock: the model's contracted `ɝ` covers
`ou're`, and the learner who does not contract puts a `ʊ` on the `o` and a `ɹ` on
the `r`, in the same word.

A sound no letter of any word can write, at its place in the order, belongs to no
word -- that is what matter said in addition looks like, and it is the whole
detection. Measured on `I think you're right.` said five ways: `very` falls out,
`you're` said uncontracted does not, and a substituted `I'm` leaves its `m`
outside. No duration is read anywhere.

What it is known to get wrong, and what is left to a later day: a sound that does
belong to a word but that none of its letters can write -- `have` said with an
`f` for its `v`. It falls out and reads as added matter. The word carries a mark
either way, and the reading names the `f`, so a person can see what happened.

    python3 placed.py <said.wav> <turn.json> ...        (by pairs)

The take file is read for the text and for the model's own sounds, so the two
readings can be shown side by side. Nothing else of it is used.
"""

import json
import re
import sys
from pathlib import Path

import anchor
import join


def placed(wav, text):
    """Each word of the text, and the learner's sounds that landed on its letters,
    plus the sounds that landed on no word at all."""
    spans = [(m.start(), m.end(), m.group()) for m in re.finditer(r"\S+", text)]
    theirs, loose = {i: [] for i in range(len(spans))}, []
    for sound in join.joined(Path(wav), text):
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
    _, grouped = anchor.words_of(text, sounds)
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
