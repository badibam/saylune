#!/usr/bin/env python3
"""Write the verbatim reference of each spoken answer, ear against proposal.

The proposal comes from a model (`train/kaggle/prefill.ipynb`), and a model is
not a reference: it is a draft that saves the typing. What makes the reference
true is that a person hears the take before validating its line -- so the take
plays first, every time, and nothing can be accepted unheard.

    python3 verbatim.py                 # resume where it stopped
    python3 verbatim.py -f              # go over lines already written

Convention, taken from the model rather than invented: a filled pause is
`[UH]` or `[UM]`, a cut-off word keeps a trailing hyphen (`comf-`). It is the
one the nyra verbatim benchmark scores against, so our numbers compare to
theirs without a translation step. Punctuation stays as written; the scoring
normalises it away, since a letter model writes none.

The known risk of working from a draft is accepting what is proposed instead of
hearing what was said. What holds against it is the replay and nothing else:
if the line does not match what you just heard, it is the line that is wrong.
"""

import argparse
import json
import os
import subprocess
import sys
import tempfile
from pathlib import Path

import atomic
from take import Cancelled, ask, play

HERE = Path(__file__).resolve().parent
ANSWERS = HERE / "out" / "takes" / "answers"
REFERENCES = ANSWERS / "references.json"


def edited(text):
    """Hand the line to $EDITOR and take back what comes out."""
    editor = os.environ.get("EDITOR", "nano")
    with tempfile.NamedTemporaryFile("w+", suffix=".txt", encoding="utf-8",
                                     delete=False) as handle:
        handle.write(text + "\n")
        path = Path(handle.name)
    try:
        subprocess.run([editor, str(path)], check=False)
        return path.read_text(encoding="utf-8").strip()
    finally:
        path.unlink(missing_ok=True)


def one_reference(wav, proposal):
    """Play, then accept, edit, or replay. Returns the line, or None to skip."""
    play(wav)
    while True:
        answer = ask("      [Entrée] valider, e éditer, r réécouter, s sauter ")
        if answer == "r":
            play(wav)
        elif answer == "e":
            proposal = edited(proposal)
            print(f"      {proposal}")
        elif answer == "s":
            return None
        else:
            return proposal


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("prefill", nargs="?", type=Path,
                        default=HERE.parent / "tmp" / "prefill.json",
                        help="le brouillon rapporté du notebook")
    parser.add_argument("-f", "--force", action="store_true",
                        help="revenir sur les lignes déjà écrites")
    args = parser.parse_args(argv)

    if not args.prefill.is_file():
        raise SystemExit(f"brouillon absent : {args.prefill}")
    drafts = [row for row in json.loads(args.prefill.read_text(encoding="utf-8"))
              if row["set"] == "answers"]
    written = {}
    if REFERENCES.is_file():
        written = json.loads(REFERENCES.read_text(encoding="utf-8"))

    print(f"\n{len(drafts)} réponses. La prise joue, puis tu valides ou tu corriges.")
    print("Ce que tu entends fait foi — la proposition n'est qu'un brouillon.\n")

    try:
        for index, row in enumerate(drafts, start=1):
            slug = row["slug"]
            if slug in written and not args.force:
                print(f"[{index}/{len(drafts)}]  {slug} — déjà écrite")
                continue
            print(f"\n[{index}/{len(drafts)}]  {slug} — « {row['asked']} »")
            print(f"      {written.get(slug, row['verbatim'])}")
            line = one_reference(ANSWERS / f"{slug}.wav",
                                 written.get(slug, row["verbatim"]))
            if line is None:
                continue
            written[slug] = line
            with atomic.opened(REFERENCES, "w", encoding="utf-8") as out:
                json.dump(written, out, ensure_ascii=False, indent=2, sort_keys=True)
    except Cancelled:
        print("\n\nInterrompu — les lignes écrites restent en place.")
        return 1

    missing = [row["slug"] for row in drafts if row["slug"] not in written]
    print(f"\n{len(written)}/{len(drafts)} références écrites"
          + (f", il manque {', '.join(missing)}" if missing else "")
          + f"\n{REFERENCES.relative_to(HERE.parent)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
