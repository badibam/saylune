#!/usr/bin/env python3
"""One recording per sound of the alphabet -- fetched, credited, and checked by ear.

The readout names sounds in IPA, and a symbol nobody can hear is a name for
something the reader has never met. So each symbol gets a recording of itself,
which is a **legend** and not a model: it says what `ʃ` means, never how the
learner should have said this word. What the learner should have said is the
synthesised model at that place in the phrase, which the app already holds --
that channel is elsewhere and this one does not compete with it.

Three steps, and the middle one is a person:

    python3 reference.py --fetch     # download, and record who made each file
    python3 reference.py --check     # listen to each, and say whether it is right
    python3 reference.py --ship      # copy the checked ones into the app

**The middle step is not optional and cannot be automated.** A name is not proof:
picking files by matching words against the article title gave `Voiceless alveolar
approximant` for /s/ and `Nasal palatal approximant` for /j/ -- neighbours in the
chart, different sounds entirely. Shipping one of those would make the app assert
one symbol while playing another, which is worse than saying nothing.

**Licences are recorded per file, because they differ.** The Commons recordings
are CC BY-SA 3.0, which is free and obliges attribution; the diphthongs are CC0,
which obliges nothing. Both are fine for F-Droid. What is not fine, and was
turned down: a CC BY-NC pack, since NonCommercial is not a free licence, and the
UCLA phonetics recordings, whose own site says permission for use outside it
cannot be given.
"""

import argparse
import json
import re
import subprocess
import time
import sys
import urllib.parse
import urllib.request
from pathlib import Path

import atomic

import review

HERE = Path(__file__).resolve().parent
OUT = HERE / "out" / "reference"
VERDICTS = OUT / "verdicts.json"
PROVENANCE = OUT / "provenance.json"
AGENT = "saylune-dev/0.1 (badibam@proton.me)"

# Seconds between downloads. Commons rate-limits a burst outright, and being
# asked to slow down is a reason to slow down rather than to retry harder.
PAUSE = 1.5

# Where each sound comes from. A Commons file title, or a URL for the ones
# Commons does not have. Named by hand, one by one, from the standard phonetic
# description of each sound -- never by a script matching words, for the reason
# in the docstring above.
SOURCES = {
    "i":  "Close front unrounded vowel.ogg",
    "ɪ":  "Near-close near-front unrounded vowel.ogg",
    "ɛ":  "Open-mid front unrounded vowel.ogg",
    "æ":  "Near-open front unrounded vowel.ogg",
    "ə":  "Mid-central vowel.ogg",
    "ɝ":  None,                       # no free recording found
    "ɑ":  "Open back unrounded vowel.ogg",
    "ʊ":  "Near-close near-back rounded vowel.ogg",
    "u":  "Close back rounded vowel.ogg",
    "aɪ": "https://cdn.freesound.org/previews/341/341345_5871007-hq.ogg",
    "aʊ": "https://cdn.freesound.org/previews/341/341347_5871007-hq.ogg",
    "eɪ": None,                       # no free recording found
    "oʊ": None,                       # no free recording found
    "ɔɪ": "https://cdn.freesound.org/previews/341/341346_5871007-hq.ogg",
    "p":  "Voiceless bilabial plosive.ogg",
    "b":  "Voiced bilabial plosive.ogg",
    "t":  "Voiceless alveolar plosive.ogg",
    "d":  "Voiced alveolar plosive.ogg",
    "k":  "Voiceless velar plosive.ogg",
    "g":  "Voiced velar plosive.ogg",
    "ʧ":  "Voiceless palato-alveolar affricate.ogg",
    "ʤ":  "Voiced palato-alveolar affricate.ogg",
    "f":  "Voiceless labiodental fricative.ogg",
    "v":  "Voiced labiodental fricative.ogg",
    "θ":  "Voiceless dental fricative.ogg",
    "ð":  "Voiced dental fricative.ogg",
    "s":  "Voiceless alveolar sibilant.ogg",
    "z":  "Voiced alveolar sibilant.ogg",
    "ʃ":  "Voiceless palato-alveolar sibilant.ogg",
    "h":  "Voiceless glottal fricative.ogg",
    "m":  "Bilabial nasal.ogg",
    "n":  "Alveolar nasal.ogg",
    "ŋ":  "Velar nasal.ogg",
    "l":  "Alveolar lateral approximant.ogg",
    "ɹ":  "Alveolar approximant.ogg",
    "j":  "Palatal approximant.ogg",
    "w":  "Voiced labio-velar approximant.ogg",
    "ɾ":  "Alveolar tap.ogg",
}

# An English word for each sound, and the letters in it that carry the sound.
#
# Without this the check cannot be made at all: a file plays, and the listener has no way
# to know what it was supposed to be. The word is the question -- "is this the sound in the
# middle of *ship*?" -- and the file is the answer. General American, since that is what the
# acoustic model was trained on and what its symbols mean here.
EXAMPLES = {
    "i":  ("see", "ee"),        "ɪ":  ("ship", "i"),
    "ɛ":  ("bed", "e"),         "æ":  ("cat", "a"),
    "ə":  ("about", "a"),       "ɝ":  ("bird", "ir"),
    "ɑ":  ("father", "a"),      "ʊ":  ("book", "oo"),
    "u":  ("food", "oo"),
    "aɪ": ("time", "i"),        "aʊ": ("now", "ow"),
    "eɪ": ("say", "ay"),        "oʊ": ("go", "o"),
    "ɔɪ": ("boy", "oy"),
    "p":  ("pen", "p"),         "b":  ("bad", "b"),
    "t":  ("tea", "t"),         "d":  ("day", "d"),
    "k":  ("cat", "c"),         "g":  ("go", "g"),
    "ʧ":  ("church", "ch"),     "ʤ":  ("judge", "j"),
    "f":  ("fat", "f"),         "v":  ("van", "v"),
    "θ":  ("think", "th"),      "ð":  ("this", "th"),
    "s":  ("see", "s"),         "z":  ("zoo", "z"),
    "ʃ":  ("ship", "sh"),       "h":  ("hat", "h"),
    "m":  ("man", "m"),         "n":  ("no", "n"),
    "ŋ":  ("sing", "ng"),       "l":  ("leg", "l"),
    "ɹ":  ("red", "r"),         "j":  ("yes", "y"),
    "w":  ("wet", "w"),         "ɾ":  ("better", "tt"),
}

# What the Freesound pack of diphthongs is, since a URL says none of it.
FREESOUND = {
    "author": "dpsa", "licence": "CC0 1.0",
    "page": "https://freesound.org/people/dpsa/packs/19326/",
}


def fetched(url, into):
    request = urllib.request.Request(url, headers={"User-Agent": AGENT})
    with urllib.request.urlopen(request, timeout=60) as handle:
        into.write_bytes(handle.read())


def commons(titles):
    """URL, licence and author of each Commons file, in one round trip per 20."""
    out = {}
    for start in range(0, len(titles), 20):
        query = urllib.parse.urlencode({
            "action": "query", "format": "json", "prop": "imageinfo",
            "iiprop": "url|extmetadata|user",
            "titles": "|".join(f"File:{t}" for t in titles[start:start + 20])})
        request = urllib.request.Request(
            "https://commons.wikimedia.org/w/api.php?" + query,
            headers={"User-Agent": AGENT})
        with urllib.request.urlopen(request, timeout=60) as handle:
            pages = json.load(handle)["query"]["pages"]
        for page in pages.values():
            if "imageinfo" not in page:
                continue
            info = page["imageinfo"][0]
            meta = info.get("extmetadata", {})
            plain = lambda key: re.sub(
                r"<[^>]+>", "", meta.get(key, {}).get("value", "")).strip()
            out[page["title"][len("File:"):]] = {
                "url": info["url"].split("?")[0],
                "licence": plain("LicenseShortName") or "?",
                "author": named(plain("Artist"), plain("Credit"), info.get("user")),
                "page": info.get("descriptionurl", ""),
            }
    return out


def named(artist, credit, uploader):
    """Who to credit, which CC BY-SA obliges and the API often leaves blank.

    Three fallbacks, in the order Commons itself means them: the stated author, then the
    stated source, then whoever uploaded it -- the last being what Commons credits when a
    file carries nothing else. An empty field is not an option: a licence that requires
    attribution and a credits line reading "?" would be an obligation printed as a shrug.

    The boilerplate is unwrapped too. "No machine-readable author provided. Denelson83
    assumed (based on copyright claims)." is Commons saying the author is Denelson83 in
    the only way an old transfer could; printing the sentence would credit nobody.
    """
    for value in (artist, credit):
        if not value:
            continue
        assumed = re.search(r"([^\s.]+) assumed \(based on copyright claims\)", value)
        if assumed:
            return assumed.group(1)
        uploaded = re.search(r"[Tt]he original uploader was ([^\s]+)", value)
        if uploaded:
            return uploaded.group(1)
        if "No machine-readable" not in value:
            return value
    return uploader or "?"


def fetch():
    OUT.mkdir(parents=True, exist_ok=True)
    titles = [s for s in SOURCES.values() if s and not s.startswith("http")]
    meta = commons(titles)
    provenance = {}
    for symbol, source in SOURCES.items():
        if source is None:
            print(f"  /{symbol:<3}/ — pas de source libre connue")
            continue
        if source.startswith("http"):
            row = dict(FREESOUND, url=source, title=Path(source).name)
        else:
            row = meta.get(source)
            if row is None:
                print(f"  /{symbol:<3}/ ÉCHEC — {source} introuvable")
                continue
            row = dict(row, title=source)
        target = OUT / f"{symbol}{Path(row['url']).suffix}"
        provenance[symbol] = dict(row, file=target.name)
        if target.is_file() and target.stat().st_size > 0:
            print(f"  /{symbol:<3}/ {target.name:<10}{'déjà là':>10}")
            continue
        fetched(row["url"], target)
        # Commons answers 429 to a burst, and a download that stops half way
        # through leaves a set nobody can tell apart from a complete one.
        time.sleep(PAUSE)
        print(f"  /{symbol:<3}/ {target.name:<10}{target.stat().st_size:>8} o  "
              f"{row['licence']}")
    atomic.write_text(
        PROVENANCE, json.dumps(provenance, indent=2, ensure_ascii=False) + "\n")
    print(f"\n  {len(provenance)} fichiers, provenance dans {PROVENANCE}")
    missing = [s for s, v in SOURCES.items() if v is None]
    print(f"  sans source : {' '.join(missing)}")


def check(only=None, slow=1.0):
    """Each recording played, and a verdict written down for it.

    Kept between runs, so a session can be stopped and picked up: a sound already
    judged is skipped unless it is named outright.
    """
    provenance = json.loads(PROVENANCE.read_text(encoding="utf-8"))
    verdicts = (json.loads(VERDICTS.read_text(encoding="utf-8"))
                if VERDICTS.is_file() else {})
    player = None
    for symbol, row in provenance.items():
        if only and symbol not in only:
            continue
        if not only and symbol in verdicts:
            continue
        path = OUT / row["file"]
        word, letters = EXAMPLES.get(symbol, ("?", "?"))
        while True:
            print(f"\n  /{symbol}/  comme le « {letters} » de « {word} »")
            print(f"       {row['title']}   ({row['licence']})")
            player = play(path, player, slow)
            # Ctrl+C and end of input are a clean way out, never a traceback: a
            # session of thirty-eight is one somebody stops in the middle, and the
            # verdicts already given have to survive it.
            try:
                answer = input("     juste ? [o]ui / [n]on / [r]éécouter / "
                               "[p]asser / [q]uitter : ").strip().lower()
            except (EOFError, KeyboardInterrupt):
                print("\n  interrompu — les verdicts déjà donnés sont gardés")
                save(verdicts)
                return
            if answer in ("r", ""):
                continue
            if answer == "q":
                save(verdicts)
                return
            if answer == "p":
                break
            verdicts[symbol] = {"ok": answer == "o", "title": row["title"]}
            save(verdicts)
            break
    save(verdicts)
    good = sum(1 for v in verdicts.values() if v["ok"])
    print(f"\n  {good} justes, {len(verdicts) - good} à refaire, "
          f"{len(provenance) - len(verdicts)} non jugés")


def play(path, player, slow):
    """The file itself, whole -- it is already one sound and nothing is cut."""
    heard = review.stretched(str(path), slow) if slow != 1 else str(path)
    for command in ([player] if player else review.PLAYERS):
        if subprocess.run(command + [heard], capture_output=True,
                          check=False).returncode == 0:
            if heard != str(path):
                Path(heard).unlink(missing_ok=True)
            return command
    raise SystemExit("aucun lecteur audio n'a joué (paplay, aplay, ffplay) — "
                     "juger un son sans l'entendre serait une supposition")


def ship():
    """The checked recordings into the app, with what the licences oblige beside them.

    **Only the ones a person passed.** An unchecked file is not shipped, and neither is one
    judged wrong: the whole value of this channel is that the symbol and the sound agree,
    and a file nobody listened to is a claim nobody tested.

    The credits travel with them rather than being written by hand somewhere else. CC BY-SA
    obliges attribution -- author, licence, and a link to the original -- and an obligation
    kept in a second place is an obligation that drifts out of date the first time the set
    changes.
    """
    if not VERDICTS.is_file():
        raise SystemExit("aucun verdict — écoute d'abord : reference.py --check")
    verdicts = json.loads(VERDICTS.read_text(encoding="utf-8"))
    provenance = json.loads(PROVENANCE.read_text(encoding="utf-8"))
    into = HERE.parent / "app" / "src" / "debug" / "assets" / "reference"
    into.mkdir(parents=True, exist_ok=True)
    for stale in into.iterdir():
        stale.unlink()

    credits = {}
    for symbol, verdict in verdicts.items():
        if not verdict["ok"]:
            continue
        row = provenance[symbol]
        atomic.write_bytes(into / f"{symbol}.ogg", (OUT / row["file"]).read_bytes())
        credits[symbol] = {"title": row["title"], "author": row["author"],
                           "licence": row["licence"], "page": row["page"]}
    atomic.write_text(into / "credits.json",
                      json.dumps(credits, indent=2, ensure_ascii=False) + "\n")

    unchecked = [s for s in provenance if s not in verdicts]
    refused = [s for s, v in verdicts.items() if not v["ok"]]
    print(f"  {len(credits)} sons embarqués dans {into}")
    if refused:
        print(f"  écartés (jugés faux) : {' '.join(refused)}")
    if unchecked:
        print(f"  non embarqués (pas écoutés) : {' '.join(unchecked)}")
    missing = [s for s, v in SOURCES.items() if v is None]
    print(f"  sans source libre : {' '.join(missing)}")
    print(f"  licences : {sorted({r['licence'] for r in credits.values()})}")


def save(verdicts):
    atomic.write_text(VERDICTS,
                      json.dumps(verdicts, indent=2, ensure_ascii=False) + "\n")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--fetch", action="store_true", help="télécharger et créditer")
    parser.add_argument("--check", action="store_true", help="écouter et juger")
    parser.add_argument("--ship", action="store_true",
                        help="embarquer les sons validés dans l'app")
    parser.add_argument("-s", "--symbol", action="append",
                        help="ne juger que celui-ci (répétable)")
    parser.add_argument("--slow", type=float, default=1.0,
                        help="ralentir, comme review.py (0.5, 0.33)")
    options = parser.parse_args(argv)
    if options.fetch:
        fetch()
    if options.check or options.symbol:
        check(only=options.symbol, slow=options.slow)
    if options.ship:
        ship()
    if not (options.fetch or options.check or options.symbol or options.ship):
        parser.print_help()
    return 0


if __name__ == "__main__":
    sys.exit(main())
