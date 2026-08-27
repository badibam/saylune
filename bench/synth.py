#!/usr/bin/env python3
"""Render a sentence with any candidate voice, in the one format the bench uses.

Every rendered file leaves here as 16 kHz, 16-bit, mono wav -- the exact shape of
a real take. Feeding the engine two different formats would put a difference in
the measurement that has nothing to do with the voice.

The dialect is deliberately *not* derived from the voice. Step 3 of the
qualification exists to score a US voice against the GB lexicon and watch the
calibration fail, so voice and dialect have to be settable apart.
"""

import argparse
import os
import sys
import wave
from collections import namedtuple
from pathlib import Path

import requests

TIMEOUT_SECONDS = 30
SAMPLE_RATE = 16000

# Azure renders a riff container directly; ElevenLabs hands back headerless pcm.
AZURE_FORMAT = "riff-16khz-16bit-mono-pcm"
ELEVENLABS_FORMAT = "pcm_16000"
ELEVENLABS_MODEL = "eleven_multilingual_v2"

Candidate = namedtuple("Candidate", "name provider voice dialect")

# The field of voices under test. Dialect here is the one each voice *claims*;
# the deliberate mismatch is passed on the command line, not listed.
CANDIDATES = (
    Candidate("azure-us-jenny", "azure", "en-US-JennyNeural", "en-us"),
    Candidate("azure-gb-sonia", "azure", "en-GB-SoniaNeural", "en-gb"),
    Candidate("eleven-us-eric", "elevenlabs", "cjVigY5qzO86Huf0OWal", "en-us"),
    Candidate("eleven-us-sarah", "elevenlabs", "EXAVITQu4vr4xnSDxMaL", "en-us"),
    Candidate("eleven-gb-daniel", "elevenlabs", "onwK4e9ZLuTAKqWW03F9", "en-gb"),
    Candidate("eleven-gb-alice", "elevenlabs", "Xb7hH8MSUJpSbSDYk0k2", "en-gb"),
)

BY_NAME = {candidate.name: candidate for candidate in CANDIDATES}


def env(*names):
    """Read the variables or name the missing ones; never a silent default."""
    values = [os.environ.get(name) for name in names]
    missing = [name for name, value in zip(names, values) if not value]
    if missing:
        raise SystemExit("Missing environment variable(s): " + ", ".join(missing))
    return values


def write_wav(path, pcm):
    path.parent.mkdir(parents=True, exist_ok=True)
    with wave.open(str(path), "wb") as handle:
        handle.setnchannels(1)
        handle.setsampwidth(2)
        handle.setframerate(SAMPLE_RATE)
        handle.writeframes(pcm)


def azure(text, voice):
    key, region = env("AZURE_SPEECH_KEY", "AZURE_SPEECH_REGION")
    issued = requests.post(
        f"https://{region}.api.cognitive.microsoft.com/sts/v1.0/issueToken",
        headers={"Ocp-Apim-Subscription-Key": key},
        timeout=TIMEOUT_SECONDS,
    )
    issued.raise_for_status()
    locale = "-".join(voice.split("-")[:2])
    ssml = (
        f"<speak version='1.0' xml:lang='{locale}'>"
        f"<voice xml:lang='{locale}' name='{voice}'>{text}</voice></speak>"
    )
    response = requests.post(
        f"https://{region}.tts.speech.microsoft.com/cognitiveservices/v1",
        headers={
            "Authorization": f"Bearer {issued.text}",
            "Content-Type": "application/ssml+xml",
            "X-Microsoft-OutputFormat": AZURE_FORMAT,
            "User-Agent": "speakup-bench",
        },
        data=ssml.encode("utf-8"),
        timeout=TIMEOUT_SECONDS,
    )
    if response.status_code != 200:
        raise SystemExit(f"Azure TTS returned {response.status_code}: {response.text}")
    return response.content, "riff"


def elevenlabs(text, voice):
    """The render, from the bare endpoint: audio is all the bench asks for."""
    (key,) = env("ELEVENLABS_KEY")
    response = requests.post(
        f"https://api.elevenlabs.io/v1/text-to-speech/{voice}",
        params={"output_format": ELEVENLABS_FORMAT},
        headers={"xi-api-key": key, "Content-Type": "application/json"},
        json={"text": text, "model_id": ELEVENLABS_MODEL},
        timeout=TIMEOUT_SECONDS,
    )
    if response.status_code != 200:
        raise SystemExit(
            f"ElevenLabs returned {response.status_code}: {response.text[:300]}"
        )
    return response.content, "pcm"


PROVIDERS = {"azure": azure, "elevenlabs": elevenlabs}


def render(text, candidate, path, force=False):
    """Render to `path`, reusing what is already there unless told otherwise.

    Renders are cache, not source: they cost characters against a monthly
    allowance and regenerate from this script alone.
    """
    if path.is_file() and not force:
        return path
    provider = PROVIDERS.get(candidate.provider)
    if provider is None:
        raise SystemExit(f"Unknown provider {candidate.provider!r}")
    payload, shape = provider(text, candidate.voice)
    if shape == "riff":
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(payload)
    else:
        write_wav(path, payload)
    return path


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("text")
    parser.add_argument("destination", type=Path)
    parser.add_argument("-c", "--candidate", default="azure-us-jenny",
                        help="one of: " + ", ".join(BY_NAME))
    parser.add_argument("-f", "--force", action="store_true")
    args = parser.parse_args(argv)

    if args.candidate not in BY_NAME:
        raise SystemExit(f"Unknown candidate {args.candidate!r}. "
                         "Known: " + ", ".join(BY_NAME))
    path = render(args.text, BY_NAME[args.candidate], args.destination, args.force)
    print(f"{path} ({path.stat().st_size} bytes)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
