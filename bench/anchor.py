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
`../docs/design/added-sounds.md`. Short version: it stops the leakage between
words, and each run of sounds a word did not ask for is then read as one of two
things -- the word said more fully than the model says it, or foreign matter
sitting between two words.

That reading is the last state, and it rests on two signals the symbols alone do
not carry. **Spelling** first: a fuller reading of a word is made of sounds its
own letters write, so a sound no letter of the word can pay for came from
somewhere else. **Time** second, from the free decoding and never from the forced
alignment: the hole the run occupies, against the model's word stretched to the
learner's own pace.

Reads take files alone -- no acoustic model, no audio. Everything it needs the
phone already wrote into `files/takes/<stamp>/turn.json`, plus the letter-to-sound
table `affinity.json`.

    python3 anchor.py <turn.json>...          la version retenue
    python3 anchor.py <turn.json>... -1.5     avec un bloc intercalé à ce prix
"""

import json
import re
import sys
from pathlib import Path

# Letter -> {sound: strength}, the same table the marking uses to know which
# letters to paint. It says where a sound may be written, never whether it is
# right, which is the whole reason it is allowed in this chain at all.
AFFINITY = json.loads(
    (Path(__file__).resolve().parent / "affinity.json").read_text(encoding="utf-8"))


def payable(symbol, letters):
    """Can any of these letters write this sound?

    A word said more fully is made of sounds its own spelling accounts for: the
    `ʊ` and the `ɹ` of an uncontracted `you're` are paid by its `o`/`u` and its
    `r`. An inserted word brings at least one sound no neighbouring letter pays
    for -- the `v` of `very` wants a letter `v`, which neither `you're` nor
    `right` has. Any strength above nought counts; the table's grades rank
    candidates elsewhere, and here there is nothing to rank.
    """
    return any(symbol in AFFINITY.get(letter.lower(), {}) for letter in letters)


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


# What two sounds facing each other are worth to the cut: the full mark when they
# are the same symbol, half when one letter of the model's word writes both.
#
# Half and not a tenth, and not a match either. `right` said with a `t` where the
# model flaps a `ɾ` is the same slot of the word, spelt by the same `t` -- the
# comparison of symbols alone made that sound extra to whichever word took it,
# so `right` and `at` tied exactly and the cut was decided by the order of the
# walk. And not a full mark, because the two are not the same sound: the model
# stays the norm, and the divergence is still there to be marked.
SAME = 2
AKIN = 1


def agreement(word, spelling, block):
    """How much a word and a run of the learner's sounds agree, and on how many.

    A longest common subsequence that grades its pairs rather than counting them,
    so that a sound written by the same letter as the model's counts for
    something. Returns what they are worth together and how many sounds were
    paired at all -- the second is what placement is rewarded on, a pair being
    where the two recordings say the same thing whatever it is worth.
    """
    rows, columns = len(word) + 1, len(block) + 1
    best = [[(0, 0)] * columns for _ in range(rows)]
    for i in range(1, rows):
        for j in range(1, columns):
            if word[i - 1] == block[j - 1]:
                pair = SAME
            elif payable(block[j - 1], spelling[i - 1]):
                pair = AKIN
            else:
                pair = 0
            best[i][j] = max(best[i - 1][j], best[i][j - 1])
            if pair:
                held, count = best[i - 1][j - 1]
                best[i][j] = max(best[i][j], (held + pair, count + 1))
    return best[len(word)][len(block)]


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


def score(word, spelling, block, times, window):
    """How well a block of the learner's sounds renders a word of the model.

    What they are worth together, less what each brings alone: nought when they
    agree exactly, negative for every sound one has and the other has not. A word
    may be given no block at all -- that is what a word not said looks like.

    Plus where the sounds were said. A block whose sounds fall inside the word's
    own stretch of the recording belongs to it; one fetched from elsewhere in the
    sentence does not, whatever it sounds like.
    """
    low, high = window
    held, paired = agreement(word, spelling, block)
    inside = sum(1 for at in times if low <= at < high)
    # Rewarded only for a sound that was paired **and** said in the word's own
    # stretch; charged for every sound fetched from outside it. Rewarding any
    # sound inside the window paid a word for swallowing material that had
    # nothing to do with it, and dropping the charge let a word swallow material
    # from the far end of the sentence for free. Both were measured.
    return (held - len(word) - len(block)
            + PLACE * min(paired, inside) - PLACE * (len(times) - inside))


def partition(model_words, spellings, said, when, windows, between=BETWEEN):
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
                        word, spellings[rank], said[gap:stop], when[gap:stop],
                        windows[rank])
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


def align(word, block):
    """One edit distance between a word of the model and the block said for it.

    A proper edit distance and not a walk: a walk cannot tell a sound said
    differently from a sound said in addition, and calls both extra. `you're` said
    /jɑ/ where the model says /jɝ/ adds nothing -- it substitutes.

    Returns a verdict for **each sound of the block**, in order -- `match`,
    `added`, or the `model>said` label of a substitution -- and the model sounds
    nothing rendered. Per-sound rather than three heaps, because the runs of added
    sounds have to be found back in the recording, and a heap has lost where each
    sound was.
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
    kind, gone, r, c = [None] * len(block), [], len(word), len(block)
    while r > 0 or c > 0:
        if r > 0 and c > 0:
            same = word[r - 1] == block[c - 1]
            if cost[r][c] == cost[r - 1][c - 1] + (0 if same else 1):
                kind[c - 1] = "match" if same else f"{word[r - 1]}>{block[c - 1]}"
                r, c = r - 1, c - 1
                continue
        if c > 0 and cost[r][c] == cost[r][c - 1] + 1:
            kind[c - 1] = "added"
            c -= 1
            continue
        gone.append(word[r - 1])
        r -= 1
    return kind, gone[::-1]


def leftover(word, block):
    """The same alignment as three heaps: added, dropped, said otherwise."""
    kind, gone = align(word, block)
    return ([sound for sound, k in zip(block, kind) if k == "added"], gone,
            [k for k in kind if k not in ("match", "added")])


def runs_of(kind):
    """The stretches of added sounds, closed only by a sound that matched.

    Two stretches parted by a substitution alone are **one** event: `very` said
    inside `you're right` comes out as `ʊ ɹ v` and `ɹ i` around a substituted `ɛ`,
    and one word was inserted once. A word boundary does not close a run either --
    the partition chose where to cut, the speaker did not.
    """
    runs, first, last = [], None, None
    for at, k in enumerate(list(kind) + ["match"]):
        if k == "added":
            first = at if first is None else first
            last = at
        elif k == "match" and first is not None:
            runs.append((first, last + 1))
            first = last = None
    return runs


def verdict(symbols, letters, hole, word_ms):
    """What a run of added sounds is: matter between the words, or the word itself
    said more fully than the model says it.

    Three branches, in order. A sound no letter can write is foreign, whatever the
    clock says. Otherwise the clock decides: a run that took longer than the whole
    word it sits in is not that word being drawn out, it is something else said
    inside it -- which is what catches a `hmm` made of sounds the spelling happens
    to pay for.

    **The bar is the word's own length, and that is not a tuned number.** It is
    the one value that is not a knob, taken because five takes of one sentence
    cannot say where a bar belongs. Those five sit far either side of it -- 464 ms
    against a 276 ms word, 141 against 308 -- so they say the branches separate,
    not where the boundary really runs.
    """
    foreign = [s for s in symbols if not payable(s, letters)]
    if foreign:
        return "intercalé", f"étranger: {' '.join(foreign)}"
    if hole > word_ms:
        return "intercalé", f"trou {hole} ms > mot {word_ms} ms"
    return "réalisation", f"trou {hole} ms ≤ mot {word_ms} ms"


def report(path, between=BETWEEN):
    take = json.loads(Path(path).read_text(encoding="utf-8"))
    text = take["text"]
    sounds = take["sounds"]
    said = [h["symbol"] for h in take["freely"]]
    when = [h["at"][0] for h in take["freely"]]
    spans, grouped = words_of(text, sounds)
    model_words = [[sounds[r]["symbol"] for r in ranks] for ranks in grouped]
    # The letters each sound of the model is written with, kept beside it: they
    # are what says the learner's `t` and the model's flapped `ɾ` are one slot of
    # `right`, which the symbols alone cannot say.
    spellings = [[sounds[r]["letters"] for r in ranks] for ranks in grouped]
    # Where each word was said, read off the alignment: from the start of its
    # first sound to the end of its last. A word the join gave no sound gets an
    # empty window and can hold nothing.
    windows = [(min(sounds[r]["saidMs"][0] for r in ranks),
                max(sounds[r]["saidMs"][1] for r in ranks)) if ranks else (0, 0)
               for ranks in grouped]
    blocks, spare, tail = partition(
        model_words, spellings, said, when, windows, between)

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

    for lo, hi, held, hole, kind, why in readings(take, spans, grouped, blocks):
        print("   %-10s %-14s %-16s %-9s %s" % (
            "run", "+".join(spans[r][2] for r in held), " ".join(said[lo:hi]),
            kind, why))
    print()


def readings(take, spans, grouped, blocks):
    """Every run of added sounds in the take, and what each one is.

    The times are read off the **free decoding**, never off the forced alignment:
    the alignment has only as many slots as the model has sounds, so it stretched
    the model's single `ɝ` over an inserted word and put it inside `you're`. The
    hole runs from the end of the last sound that matched to the start of the
    first that matches again, and the run of added sounds is what is in it.
    """
    sounds, freely = take["sounds"], take["freely"]
    said = [h["symbol"] for h in freely]
    model_words = [[sounds[r]["symbol"] for r in ranks] for ranks in grouped]

    kind = ["loose"] * len(said)
    owner = [None] * len(said)
    for rank, (lo, hi) in enumerate(blocks):
        inside, _ = align(model_words[rank], said[lo:hi])
        for at, verdicted in enumerate(inside):
            kind[lo + at], owner[lo + at] = verdicted, rank

    # The model's words at the learner's own pace: everything else would compare a
    # slow speaker's hole to a brisk synthesis and call every one of them foreign.
    stretch = ((max(s["saidMs"][1] for s in sounds) - min(s["saidMs"][0] for s in sounds))
               / (max(s["modelMs"][1] for s in sounds) - min(s["modelMs"][0] for s in sounds)))
    word_ms = [int(stretch * (max(sounds[r]["modelMs"][1] for r in ranks)
                              - min(sounds[r]["modelMs"][0] for r in ranks)))
               if ranks else 0 for ranks in grouped]

    out = []
    for lo, hi in runs_of(kind):
        held = sorted({owner[at] for at in range(lo, hi) if owner[at] is not None})
        before = freely[lo - 1]["at"][1] if lo > 0 else freely[lo]["at"][0]
        after = freely[hi]["at"][0] if hi < len(said) else freely[hi - 1]["at"][1]
        told, why = verdict(said[lo:hi], "".join(spans[r][2] for r in held),
                            after - before, sum(word_ms[r] for r in held))
        out.append((lo, hi, held, after - before, told, why))
    return out


if __name__ == "__main__":
    paths = [a for a in sys.argv[1:] if not a.startswith("-")]
    sweep = [float(a[1:]) for a in sys.argv[1:] if a.startswith("-")]
    for value in (sweep or [BETWEEN]):
        if sweep:
            print(f"######## coût d'un son intercalé : {value}")
        BETWEEN = value
        for path in sorted(paths):
            report(path, value)
