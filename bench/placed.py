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
    thing that happened.

    **Where it sits is settled by the order, and by nothing else.** A mark falls
    strictly between the last letter claimed before it and the first letter
    claimed after it -- so it can never be drawn beside a letter that was said on
    the other side of it. Inside that window it goes as late as it may:

    - when the next placed sound is in **another word**, the run sat between two
      words, and the mark goes to the **end of the word before it**. That is the
      ordinary case of matter said in addition, and it is what keeps a wedge from
      splitting `ng` in two.
    - when the next placed sound is in the **same word**, the run sat inside that
      word, and the mark stays on the last letter claimed. There is no boundary
      to push it to: pushing it to the end of the word would put it after letters
      that were said before it.

    The second case is not hypothetical: `trimmed` empties a sound after the walk
    when the letters it was given turn out to be worth nothing on it, and a sound
    that can borrow none from a neighbour is then loose in the middle of a word.
    Measured on 95 renders of the bench, four words are spelt that way.

    `gaps` says which sounds of the model's grid were compared, so a mark can
    name the readout line it follows. `Added.kt` is the same thing in Kotlin, and
    the port test holds the two against each other.
    """
    out, run, claimed, word = [], [], -1, None

    def close(nextwords, nextclaim):
        """The seam for the open run, and the proof that it is in order."""
        after = word[1] - 1 if word is not None and nextwords != word else claimed
        assert claimed <= after, (
            f"la marque {' '.join(run)} recule : {after} avant la lettre {claimed}")
        assert nextclaim is None or after < nextclaim, (
            f"la marque {' '.join(run)} passe la lettre {nextclaim}, dite après elle")
        out.append(mark(sounds, gaps, after, list(run)))
        run.clear()

    for sound in said:
        if not (sound.spots or sound.borrowed):
            run.append(sound.symbol)
            continue
        if run:
            close(sound.word_at, min(sound.spots) if sound.spots else None)
        if sound.spots:
            claimed, word = max(sound.spots), sound.word_at
    if run:
        close(None, None)
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
