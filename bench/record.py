#!/usr/bin/env python3
"""Record the hesitation sets: read the written ones, answer the cold ones.

A sibling of `take.py` rather than a mode of it. `take.py` guides three passes
around a model voice, which is the geometry of a copy; here there is one pass
and no model at all -- the take *is* the reference, or the reference gets
written from it later.

    python3 record.py stumbles     # the written hesitations, read as printed
    python3 record.py answers      # questions shown one at a time, answered cold

Nothing about an answer is known in advance, so that set writes what it asked
beside what it recorded (`asked.json`). The verbatim reference is written
against that afterwards, and it is the expensive step of the whole set.

The questions are printed one at a time and only at the moment of recording:
read ahead and the hesitation is rehearsed away, which is the one thing this
set exists to catch.
"""

import argparse
import json
import sys
from pathlib import Path

import atomic
import stumbles
from take import Cancelled, capture, play, ask

HERE = Path(__file__).resolve().parent
TAKES = HERE / "out" / "takes"


def one_take(path):
    """Record, then keep, redo, or play back on demand.

    Playing every take back is what `take.py` does, and it is right there: a
    copy is judged against the model that was just heard. Here nothing is being
    matched, and a spoken answer runs half a minute -- hearing all of them back
    doubles the session for a check that is almost always 'yes'. So the replay
    stays one keypress away instead of being the way through.
    """
    while True:
        capture(path)
        while True:
            answer = ask("        [Entrée] garder, r refaire, e écouter ")
            if answer != "e":
                break
            play(path)
        if answer != "r":
            return


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("which", choices=("stumbles", "answers"),
                        help="les hésitations écrites, ou les réponses à froid")
    parser.add_argument("-f", "--force", action="store_true",
                        help="reprendre les prises déjà faites")
    args = parser.parse_args(argv)

    takes = TAKES / args.which
    if args.which == "stumbles":
        entries = [(slug, text, stumbles.DIRECTIONS.get(slug))
                   for slug, text, _ in stumbles.STUMBLES]
        print(f"\n{len(entries)} prises. Dis la phrase **telle qu'elle est écrite**, "
              "hésitation comprise.")
    else:
        entries = [(slug, question, None) for slug, question in stumbles.QUESTIONS]
        print(f"\n{len(entries)} questions. Réponds à voix haute, en anglais, "
              "sans préparer — vingt à quarante secondes.")
    print("Attends une seconde après [Entrée] avant de parler — la première "
          "seconde est coupée.\n")

    asked = {}
    try:
        for index, (slug, line, direction) in enumerate(entries, start=1):
            path = takes / f"{slug}.wav"
            asked[slug] = line
            if path.is_file() and not args.force:
                print(f"[{index}/{len(entries)}]  {slug} — déjà pris")
                continue
            print(f"\n[{index}/{len(entries)}]  « {line} »")
            if direction:
                print(f"     — {direction}")
            one_take(path)
    except Cancelled:
        print("\n\nInterrompu — les prises gardées restent en place.")
        return 1
    finally:
        if args.which == "answers" and takes.is_dir():
            with atomic.opened(takes / "asked.json", "w", encoding="utf-8") as out:
                json.dump(asked, out, ensure_ascii=False, indent=2)

    print(f"\nTerminé. Prises dans {takes.relative_to(HERE.parent)}/")
    return 0


if __name__ == "__main__":
    sys.exit(main())
