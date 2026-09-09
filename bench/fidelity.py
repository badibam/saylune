#!/usr/bin/env python3
"""How much of a hesitation a recogniser writes back.

The one question the rest of the bench does not ask. Word error rate answers a
different one -- it counts every word alike, so a model that silently drops
every `uh` scores *better* than one that writes them, which is exactly backwards
here: three fluency sheets read those tokens, and the sound analysis aligns the
learner on every word said, so a swallowed hesitation turns that stretch of
audio into sounds with no letters.

    python3 fidelity.py ../tmp/prefill.json ../tmp/baseline.json

Scored per kind, because they do not fail the same way. A filled pause is a
token to write or not. A cut-off word is a fragment no vocabulary contains, so
the failure mode is a whole wrong word. A repetition is a token a language model
is under pressure to delete as an error.

Rules, written here so they are not bent in front of a number:

- **Any filler token counts.** A French speaker hesitates with a French vowel,
  so `EUH`, `EH` or `AH` where the reference says `[UH]` has heard it. Writing
  nothing, or writing a content word, is the miss.
- **A fragment counts as rendered** when what lands in its place shares its
  opening and is not the whole word. `COMF` for `COMF-` is right; `COMFORTABLE`
  is the failure this measures.
- **A repetition counts** when the doubling survives.
- **Invented hesitations cost what missed ones cost.** A filler written where
  none was said is counted and reported, never netted off.

Punctuation and case are normalised away: a letter model writes neither, and
scoring them would measure the output format instead of what was heard.
"""

import argparse
import json
import re
import sys
from collections import Counter
from pathlib import Path

import stumbles

HERE = Path(__file__).resolve().parent
ANSWERS = HERE / "out" / "takes" / "answers"

# Every shape a filled pause comes back in, ours and the models'. Folded to one
# token so that scoring asks whether the hesitation was heard, not how it was
# spelled.
FILLERS = {"UH", "UM", "UHM", "ERM", "ER", "EUH", "EH", "AH", "HM", "HMM", "MM", "MHM"}
FILLER = "[UH]"


def tokens(text):
    """Words, upper-cased, fillers folded, the fragment hyphen kept."""
    words = []
    for raw in text.split():
        word = re.sub(r"[^A-Za-z'-]", "", raw.strip("[]")).upper().strip("'")
        if not word:
            continue
        words.append(FILLER if word.rstrip("-") in FILLERS else word)
    return words


def aligned(reference, hypothesis):
    """Levenshtein backtrace: pairs of (reference token, hypothesis token).

    None on either side is a deletion or an insertion. Written out rather than
    taken from difflib, whose matcher optimises for readable diffs and not for
    the smallest edit count -- the counts here have to be the real ones.
    """
    rows, columns = len(reference), len(hypothesis)
    cost = [[0] * (columns + 1) for _ in range(rows + 1)]
    for i in range(rows + 1):
        cost[i][0] = i
    for j in range(columns + 1):
        cost[0][j] = j
    for i in range(1, rows + 1):
        for j in range(1, columns + 1):
            same = reference[i - 1] == hypothesis[j - 1]
            cost[i][j] = min(cost[i - 1][j - 1] + (0 if same else 1),
                             cost[i - 1][j] + 1, cost[i][j - 1] + 1)
    pairs, i, j = [], rows, columns
    while i or j:
        if i and j and cost[i][j] == cost[i - 1][j - 1] + (
                0 if reference[i - 1] == hypothesis[j - 1] else 1):
            pairs.append((reference[i - 1], hypothesis[j - 1]))
            i, j = i - 1, j - 1
        elif i and cost[i][j] == cost[i - 1][j] + 1:
            pairs.append((reference[i - 1], None))
            i -= 1
        else:
            pairs.append((None, hypothesis[j - 1]))
            j -= 1
    return list(reversed(pairs))


def scored(reference, hypothesis):
    """Per-kind hits and misses, invented fillers, and the edit counts."""
    said, heard = tokens(reference), tokens(hypothesis)
    pairs = aligned(said, heard)
    tally = Counter()

    at = 0  # where we are in the reference, which the pairs do not carry
    for ref, hyp in pairs:
        if ref is None:
            if hyp == FILLER:
                tally["filler-invented"] += 1
            continue
        if ref == FILLER:
            tally["filler-total"] += 1
            tally["filler-rendered"] += hyp == FILLER
        elif ref.endswith("-"):
            # The whole word follows the fragment in the reference; writing it
            # instead of the fragment is the failure this line measures.
            stem = ref.rstrip("-")
            whole = said[at + 1] if at + 1 < len(said) else None
            written = hyp.rstrip("-") if hyp else None
            tally["fragment-total"] += 1
            tally["fragment-rendered"] += bool(
                written and written != whole and written.startswith(stem[:2]))
        elif at and ref == said[at - 1]:
            tally["repetition-total"] += 1
            tally["repetition-rendered"] += hyp == ref
        at += 1

    tally["words"] = len(said)
    tally["edits"] = sum(1 for ref, hyp in pairs if ref != hyp)
    return tally


def report(path, only):
    rows = json.loads(path.read_text(encoding="utf-8"))
    # The written references live in `stumbles.py` and nowhere else; the copy
    # that travelled to the notebook is a copy, and it ages.
    said = {slug: (text, kind) for slug, text, kind in stumbles.STUMBLES}
    written = {}
    if (ANSWERS / "references.json").is_file():
        written = json.loads((ANSWERS / "references.json").read_text(encoding="utf-8"))

    total, by_kind = Counter(), {}
    for row in rows:
        if only and row["set"] != only:
            continue
        if row["slug"] in said:
            reference, kind = said[row["slug"]]
        elif row["slug"] in written:
            reference, kind = written[row["slug"]], "spontaneous"
        else:
            continue
        tally = scored(reference, row["verbatim"])
        total += tally
        by_kind.setdefault(kind, Counter()).update(tally)

    print(f"\n{path.name}")
    for kind in ("filler", "fragment", "repetition"):
        seen, rendered = total[f"{kind}-total"], total[f"{kind}-rendered"]
        if seen:
            print(f"  {kind:<11} {rendered}/{seen} rendus"
                  + (f", {total['filler-invented']} inventés" if kind == "filler" else ""))
    print("  " + "-" * 44)
    # Per kind of take, and not only per kind of token: a false start or a held
    # syllable has no token of its own to count, so the edit rate is the only
    # thing that sees it at all.
    for kind, tally in sorted(by_kind.items()):
        print(f"  {kind:<11} {tally['edits']:>3} écarts sur {tally['words']:>3} mots"
              f" ({100 * tally['edits'] / tally['words']:.1f} %)")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("results", nargs="+", type=Path,
                        help="les fichiers rapportés des notebooks")
    parser.add_argument("-s", "--set", dest="only", choices=("stumbles", "answers"),
                        help="ne noter qu'un des deux blocs")
    args = parser.parse_args(argv)
    for path in args.results:
        report(path, args.only)
    return 0


if __name__ == "__main__":
    sys.exit(main())
