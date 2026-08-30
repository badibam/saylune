#!/usr/bin/env python3
"""Word anchoring, tried on real takes. **Exploration -- none of this is in the app.**

The flat edit distance the app uses knows nothing of words, so it reattributes
sounds across the whole sentence to save itself an edit -- measured, it called
the `u` and the `r` of a correctly said `you're` insertions. Anchoring gives it
the structure it was missing: the learner's freely decoded sounds are partitioned
between the words of the model, cuts chosen to agree best, and what is left over
inside a word stays inside that word. Same shape as `join.partition`, one level
up and on the learner's side.

What it settles and what it does not is written out, table by table, in
`../docs/design/word-anchoring.md`. Short version: it stops the leakage between
words, and it does **not** separate a word said differently from a word said in
addition.

Reads take files alone -- no acoustic model, no audio. Everything it needs the
phone already wrote into `files/takes/<stamp>/turn.json`.

    python3 anchor.py <turn.json>...          la version retenue
    python3 anchor.py <turn.json>... -1.5     avec un bloc intercalé à ce prix
"""

import json
import re
import sys
from pathlib import Path


def words_of(text, sounds):
    """Each word of the text, and the model sounds that spell it."""
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


def common(one, two):
    """Length of the longest common subsequence -- how much two runs agree."""
    best = [[0] * (len(two) + 1) for _ in range(len(one) + 1)]
    for i in range(1, len(one) + 1):
        for j in range(1, len(two) + 1):
            best[i][j] = (best[i - 1][j - 1] + 1 if one[i - 1] == two[j - 1]
                          else max(best[i - 1][j], best[i][j - 1]))
    return best[len(one)][len(two)]


# What one of the learner's sounds is worth to a word whose aligned window holds
# it, and costs to a word whose window does not. Symbols alone leave the cut
# undecided -- measured, two cuts of one sentence tied exactly, and the tie was
# broken by nothing. Time decides it, and time is the axis the alignment is
# trusted on.
PLACE = 1

# What one of the learner's sounds costs when it is put in no word at all.
#
# Off by default, and that is a result rather than a default: swept over the real
# takes, no price works. Cheap enough to let an inserted word out and two plain
# substitutions go out with it; dear enough to keep the substitutions and the
# inserted word never leaves. The two have the same shape in symbols, and time
# does not separate them either, the alignment having stretched the model's one
# sound over the inserted word. Kept behind a flag so the sweep can be rerun.
BETWEEN = float("inf")


def score(word, block, times, window):
    """How well a block of the learner's sounds renders a word of the model.

    Twice what they share, less what each brings alone: nought when they agree
    exactly, negative for every sound one has and the other has not. A word may
    be given no block at all -- that is what a word not said looks like.

    Plus where the sounds were said. A block whose sounds fall inside the word's
    own stretch of the recording belongs to it; one fetched from elsewhere in the
    sentence does not, whatever it sounds like.
    """
    low, high = window
    # Placement rewards a sound that **matched** and was said in the word's own
    # stretch. Rewarding any sound inside the window paid a word for swallowing
    # material that had nothing to do with it -- measured, `very` came free.
    matched = common(word, block)
    inside = sum(1 for at in times if low <= at < high)
    # Rewarded only for a sound that matched **and** was said in the word's own
    # stretch; charged for every sound fetched from outside it. Rewarding any
    # sound inside the window paid a word for swallowing material that had
    # nothing to do with it, and dropping the charge let a word swallow material
    # from the far end of the sentence for free. Both were measured.
    return (2 * matched - len(word) - len(block)
            + PLACE * min(matched, inside) - PLACE * (len(times) - inside))


def partition(model_words, said, when, windows, between=BETWEEN):
    """The learner's sounds cut between the model's words, best agreement wins.

    A run may be given to **no word**, which is what an inserted word looks like:
    every sound had to belong to some word before, so `very` was torn in half
    between its two neighbours. The mirror of `join.partition`, where a word may
    be given no sound.

    Returns, per word, the block it holds and the run left in front of it, plus
    the run left after the last word.
    """
    count = len(said)
    rows = len(model_words) + 1
    best = [[float("-inf")] * (count + 1) for _ in range(rows)]
    back = [[None] * (count + 1) for _ in range(rows)]
    best[0][0] = 0
    for rank, word in enumerate(model_words):
        for start in range(count + 1):
            if best[rank][start] == float("-inf"):
                continue
            for gap in range(start, count + 1):
                # Written out rather than multiplied: `inf * 0` is a nan, and the
                # off switch is exactly a price of infinity for nothing left over.
                loose = 0 if gap == start else between * (gap - start)
                spare = best[rank][start] - loose
                for stop in range(gap, count + 1):
                    value = spare + score(
                        word, said[gap:stop], when[gap:stop], windows[rank])
                    if value > best[rank + 1][stop]:
                        best[rank + 1][stop] = value
                        back[rank + 1][stop] = (start, gap)

    # The tail may be left over too, at the same price.
    end, total = count, best[rows - 1][count]
    for stop in range(count + 1):
        value = best[rows - 1][stop] - (0 if stop == count else between * (count - stop))
        if value > total:
            total, end = value, stop

    blocks, spare, at = [], [], end
    for rank in range(len(model_words), 0, -1):
        start, gap = back[rank][at]
        blocks.append((gap, at))
        spare.append((start, gap))
        at = start
    return blocks[::-1], spare[::-1], (end, count)


def leftover(word, block):
    """Inside one word: what was added, what was dropped, what was said otherwise.

    A proper edit distance and not a walk: a walk cannot tell a sound said
    differently from a sound said in addition, and calls both extra. `you're` said
    /jɑ/ where the model says /jɝ/ adds nothing -- it substitutes.
    """
    rows, columns = len(word) + 1, len(block) + 1
    cost = [[0] * columns for _ in range(rows)]
    for r in range(rows):
        cost[r][0] = r
    for c in range(columns):
        cost[0][c] = c
    for r in range(1, rows):
        for c in range(1, columns):
            same = word[r - 1] == block[c - 1]
            cost[r][c] = min(cost[r - 1][c - 1] + (0 if same else 1),
                             cost[r - 1][c] + 1, cost[r][c - 1] + 1)
    added, gone, other, r, c = [], [], [], len(word), len(block)
    while r > 0 or c > 0:
        if r > 0 and c > 0:
            same = word[r - 1] == block[c - 1]
            if cost[r][c] == cost[r - 1][c - 1] + (0 if same else 1):
                if not same:
                    other.append(f"{word[r - 1]}>{block[c - 1]}")
                r, c = r - 1, c - 1
                continue
        if c > 0 and cost[r][c] == cost[r][c - 1] + 1:
            added.append(block[c - 1])
            c -= 1
            continue
        gone.append(word[r - 1])
        r -= 1
    return added[::-1], gone[::-1], other[::-1]


def report(path, between=BETWEEN):
    take = json.loads(Path(path).read_text(encoding="utf-8"))
    text = take["text"]
    sounds = take["sounds"]
    said = [h["symbol"] for h in take["freely"]]
    when = [h["at"][0] for h in take["freely"]]
    spans, grouped = words_of(text, sounds)
    model_words = [[sounds[r]["symbol"] for r in ranks] for ranks in grouped]
    # Where each word was said, read off the alignment: from the start of its
    # first sound to the end of its last. A word the join gave no sound gets an
    # empty window and can hold nothing.
    windows = [(min(sounds[r]["saidMs"][0] for r in ranks),
                max(sounds[r]["saidMs"][1] for r in ranks)) if ranks else (0, 0)
               for ranks in grouped]
    blocks, spare, tail = partition(model_words, said, when, windows, between)

    red = {(w["start"], w["end"]) for w in take.get("words", [])}
    print(f"== attempt {take.get('attempt')}  {text!r}")
    print("   %-10s %-14s %-16s %-9s %-9s %-9s %s" % (
        "mot", "modèle", "apprenant", "en trop", "manque", "autrement", "rouge"))
    for rank, (start, stop, word) in enumerate(spans):
        loose = said[spare[rank][0]:spare[rank][1]]
        if loose:
            print("   %-10s %-14s %-16s %-9s" % (
                "(intercalé)", "—", " ".join(loose), " ".join(loose)))
        block = said[blocks[rank][0]:blocks[rank][1]]
        added, gone, other = leftover(model_words[rank], block)
        print("   %-10s %-14s %-16s %-9s %-9s %-9s %s" % (
            word, " ".join(model_words[rank]), " ".join(block) or "—",
            " ".join(added), " ".join(gone), " ".join(other),
            "oui" if (start, stop) in red else ""))
    if said[tail[0]:tail[1]]:
        print("   %-10s %-14s %-16s %-9s" % (
            "(fin)", "—", " ".join(said[tail[0]:tail[1]]),
            " ".join(said[tail[0]:tail[1]])))
    print()


if __name__ == "__main__":
    paths = [a for a in sys.argv[1:] if not a.startswith("-")]
    sweep = [float(a[1:]) for a in sys.argv[1:] if a.startswith("-")]
    for value in (sweep or [BETWEEN]):
        if sweep:
            print(f"######## coût d'un son intercalé : {value}")
        BETWEEN = value
        for path in sorted(paths):
            report(path, value)
