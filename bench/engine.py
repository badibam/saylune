#!/usr/bin/env python3
"""The analysis engine, behind the one call the bench is allowed to make.

audio + text + dialect -> located, scored phones and syllables. Everything above
this line is written against that shape, so a second engine -- a local one, say
-- joins the bench by adding a function here and nothing else.

SpeechAce is the engine retained for v1; `score` is its adapter.
"""

import json
import os
import wave
from collections import namedtuple
from pathlib import Path

import requests

TIMEOUT_SECONDS = 60

# Extents come in units of 10 ms. Below this the aligner has dropped out and
# labelled the segment at random -- scores of 0 to 42 on sounds that were
# pronounced correctly. Measured in `docs/design/speechace.md`; everything is
# normal again by 30 ms.
DEGENERATE_MS = 10

Phone = namedtuple("Phone", "word phone heard quality extent")
Syllable = namedtuple("Syllable",
                      "word letters quality extent stress predicted "
                      "stress_score intonation pitch_range")

# Stress and melody are only reported when asked for, and the lexicon's own
# expectation is not usable in their place: on flawless synthetic audio the
# engine contradicts its own lexicon on 41% of polysyllabic words. What is
# compared is its reading of the learner against its reading of the model.
INTONATION = {"include_intonation": "1"}
Reading = namedtuple("Reading", "phones syllables raw")


def credentials():
    """Key and endpoint, failing loudly: the endpoint is tied to the key's region
    and calling the wrong one answers like a bad key."""
    pairs = (
        ("SPEECHACE_KEY", os.environ.get("SPEECHACE_KEY")),
        ("SPEECHACE_ENDPOINT", os.environ.get("SPEECHACE_ENDPOINT")),
    )
    missing = [name for name, value in pairs if not value]
    if missing:
        raise SystemExit("Missing environment variable(s): " + ", ".join(missing))
    return pairs[0][1], pairs[1][1].rstrip("/")


def duration_seconds(wav):
    with wave.open(str(wav), "rb") as handle:
        return handle.getnframes() / handle.getframerate()


def score(wav, text, dialect="en-us", extras=None, cache=None):
    """Read `wav` as an attempt at `text`, judged against `dialect`.

    A cached reading is served rather than bought again: every call is billed,
    and the engine is deterministic, so a second call would spend money to be
    told the same thing.
    """
    if cache is not None and cache.is_file():
        return read(json.loads(cache.read_text(encoding="utf-8")))
    key, endpoint = credentials()
    with open(wav, "rb") as audio:
        response = requests.post(
            f"{endpoint}/api/scoring/text/v9/json",
            params={"key": key, "dialect": dialect},
            data={"text": text, **(extras or {})},
            files={"user_audio_file": (Path(wav).name, audio, "audio/wav")},
            timeout=TIMEOUT_SECONDS,
        )
    if response.status_code != 200:
        raise SystemExit(
            f"SpeechAce returned {response.status_code}: {response.text[:300]}"
        )
    payload = response.json()
    if payload.get("status") != "success":
        raise SystemExit("SpeechAce refused the take:\n"
                         + json.dumps(payload, indent=2, ensure_ascii=False))
    if cache is not None:
        cache.parent.mkdir(parents=True, exist_ok=True)
        cache.write_text(json.dumps(payload, indent=2, ensure_ascii=False),
                         encoding="utf-8")
    return read(payload)


def read(payload):
    """Flatten the response into the shape the bench reasons about."""
    phones, syllables = [], []
    for word in payload["text_score"].get("word_score_list", []):
        name = word.get("word")
        for phone in word.get("phone_score_list", []):
            phones.append(Phone(
                word=name,
                phone=phone.get("phone"),
                heard=phone.get("sound_most_like"),
                quality=phone.get("quality_score"),
                extent=tuple(phone.get("extent", (0, 0))),
            ))
        for syllable in word.get("syllable_score_list", []):
            syllables.append(Syllable(
                word=name,
                letters=syllable.get("letters"),
                quality=syllable.get("quality_score"),
                extent=tuple(syllable.get("extent", (0, 0))),
                stress=syllable.get("stress_level"),
                predicted=syllable.get("predicted_stress_level"),
                stress_score=syllable.get("stress_score"),
                intonation=syllable.get("intonation"),
                pitch_range=syllable.get("pitch_range"),
            ))
    return Reading(phones=phones, syllables=syllables, raw=payload)


def milliseconds(extent):
    begin, end = extent
    return (end - begin) * 10


def degenerate(phone):
    """A segment too short to be a sound: the aligner lost the thread here.

    Reading such a phone as a fault would charge the learner with the engine's
    own dropout, which is why it is filtered before anything is decided.
    """
    return milliseconds(phone.extent) <= DEGENERATE_MS


def sound(phones):
    """The phones a verdict may rest on: scored, and not a dropout."""
    return [p for p in phones if p.quality is not None and not degenerate(p)]
