#!/usr/bin/env python3
"""The acoustic pass, served over HTTP.

One brick of the analysis is expensive -- the network pass (brick 2 of
`docs/analysis.md`) -- and everything else is arithmetic over what it renders.
This serves that brick and nothing else: an audio comes in, a matrix goes out.
No text, no reference, no threshold, no judgement ever reaches this machine, so
what the app deports is a computation and never a verdict.

The pass renders a second output the phone needs: layer 19, which the stress
probe reads. Sent as such it is a thousand times the matrix -- 1.22 MB against
0.05 for a six second turn -- so it is not sent. The probe is a linear read, and
averaging commutes with a linear map, so the projection is applied here, one
scalar per frame, and the device averages those over its own syllable spans and
adds the bias. Same arithmetic, in the other order.

    SAYLUNE_TOKEN=... python3 serve.py

Provisional by construction: it holds a port, it does not manage anything. What
it is for is measuring what the round trip costs, which no amount of design can
answer.
"""

import hashlib
import io
import json
import os
import re
import struct
import sys
import threading
import time
import urllib.parse
import urllib.request
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

import numpy as np
import soundfile as sf

SAMPLE_RATE = 16000

# The very files the app downloads, from the release that serves them, checked
# against the very digests `Weights.kt` holds. Both sides run the same bytes
# rather than two exports of the same weights.
RELEASE = ("https://github.com/badibam/saylune/releases/download/"
           "weights-timit-ipa-1")
PIECES = {
    "timit-ipa-int8.onnx":
        "ec2208f4fd224b04502ece9259fbcfc211657e721910cfba212162592c5128da",
    "vocab.json":
        "12b7de6be6a1bd3132ffdef8d8fe191c8e98f2d70639666e35d9cf6c9d2af77a",
    "probe.json":
        "d904a7229f35fb0983a624f56df8a1adec25f4e16d7369f5c90b3b0ef0ba2707",
}

# A pass holds roughly 1.8 GB above the weights for a thirty second turn, and
# the attention grows as the square of the length. The cap is what keeps a
# request from taking the machine down rather than being refused.
MAX_SECONDS = float(os.environ.get("SAYLUNE_MAX_SECONDS", "35"))

# What the wire carries. Little-endian throughout; the matrix in 16 bits
# because it is read as a spread and never to the last digit, the per-frame
# probe in 32 because the device averages it before anything reads it.
MAGIC = b"SAYM"
VERSION = 1

# A take sent while it is said is held here until it is closed. One nobody
# closes -- thrown away, or its phone gone -- is forgotten after this long, and
# no more than this many are held at once.
TAKE_TTL = float(os.environ.get("SAYLUNE_TAKE_TTL", "600"))
OPEN_TAKES = int(os.environ.get("SAYLUNE_OPEN_TAKES", "16"))
NAMED = re.compile(r"^/take/([0-9a-f-]{8,64})(/end|/drop)?$")


def held(home, name, digest):
    """The piece on disk, fetched from the release if it is not there yet."""
    path = home / name
    if path.is_file() and sha256(path) == digest:
        return path
    print(f"  {name} — téléchargement", flush=True)
    partial = path.with_suffix(path.suffix + ".part")
    with urllib.request.urlopen(f"{RELEASE}/{name}") as source, \
            open(partial, "wb") as sink:
        while chunk := source.read(1 << 20):
            sink.write(chunk)
    got = sha256(partial)
    if got != digest:
        partial.unlink()
        raise SystemExit(f"{name} : empreinte {got}, attendue {digest}")
    partial.replace(path)
    return path


def sha256(path):
    digest = hashlib.sha256()
    with open(path, "rb") as handle:
        while chunk := handle.read(1 << 20):
            digest.update(chunk)
    return digest.hexdigest()


def prepared(audio):
    """Zero mean, unit variance -- the form the network was trained to receive.

    Written out rather than delegated, and identical to `bench/matrix.py` and to
    what the app reimplements: three copies of four lines, because it is the one
    step every side has to agree on exactly.
    """
    audio = np.asarray(audio, dtype=np.float32)
    return ((audio - audio.mean())
            / np.sqrt(audio.var() + 1e-7)).astype(np.float32)[None, :]


class Probe:
    """The stress probe, folded into one vector and one offset.

    `Stress.parts` standardises the mean of the layer over a span, dots it with
    the frozen weight and adds the bias. Standardise-then-dot is linear, so the
    same number comes out of dotting each frame first and averaging after --
    which is what lets one scalar per frame travel instead of 1024.

    The bias is deliberately left behind: the device adds it once per span,
    exactly where `Stress.parts` adds it today.
    """

    def __init__(self, path):
        held = json.loads(Path(path).read_text(encoding="utf-8"))
        mean = np.asarray(held["mean"], dtype=np.float64)
        deviation = np.asarray(held["deviation"], dtype=np.float64)
        weight = np.asarray(held["weight"], dtype=np.float64)
        self.layer = held["layer"]
        self.bias = float(held["bias"])
        self.direction = weight / deviation
        self.offset = float(-(mean * self.direction).sum())

    def perFrame(self, hidden):
        """One scalar per frame, in double, as the device accumulates in double."""
        return (hidden.astype(np.float64) @ self.direction) + self.offset


class Engine:
    """The session, loaded once, and the lock that keeps the machine standing."""

    def __init__(self, home, threads, concurrency):
        import onnxruntime

        weights = held(home, "timit-ipa-int8.onnx", PIECES["timit-ipa-int8.onnx"])
        held(home, "vocab.json", PIECES["vocab.json"])
        self.probe = Probe(held(home, "probe.json", PIECES["probe.json"]))
        settings = onnxruntime.SessionOptions()
        # Stated rather than left to the machine, for the same reason the phone
        # states it: the reading this is held against is deterministic.
        settings.intra_op_num_threads = threads
        settings.inter_op_num_threads = 1
        started = time.perf_counter()
        self.session = onnxruntime.InferenceSession(
            str(weights), settings, providers=["CPUExecutionProvider"])
        self.load = time.perf_counter() - started
        # Not a queue for fairness: memory. Three long turns at once is more
        # than this machine holds, and being refused beats being killed.
        self.room = threading.Semaphore(concurrency)

    def read(self, audio):
        with self.room:
            started = time.perf_counter()
            probabilities, hidden = self.session.run(
                None, {"input_values": prepared(audio)})
            spent = time.perf_counter() - started
        return probabilities[0], self.probe.perFrame(hidden), spent


class Takes:
    """Takes arriving while they are said, a piece at a time.

    Each piece names where it starts. One that starts past what is held would
    leave a hole, so nothing of it is kept, and the answer -- how much is held --
    tells the phone where to send from. One that overlaps what is held adds only
    what is new, so a piece sent twice changes nothing.
    """

    def __init__(self):
        self.lock = threading.Lock()
        self.held = {}

    def sweep(self, now):
        for name in [name for name, (_, touched) in self.held.items()
                     if now - touched > TAKE_TTL]:
            del self.held[name]

    def piece(self, name, at, body):
        """How much is held once the piece is in, or None when there is no room."""
        with self.lock:
            now = time.monotonic()
            self.sweep(now)
            if name not in self.held:
                if len(self.held) >= OPEN_TAKES:
                    return None
                self.held[name] = (bytearray(), now)
            data, _ = self.held[name]
            if at <= len(data) < at + len(body):
                data.extend(body[len(data) - at:])
            self.held[name] = (data, now)
            return len(data)

    def taken(self, name):
        """The take, no longer held, or None if it is unknown or forgotten."""
        with self.lock:
            self.sweep(time.monotonic())
            data, _ = self.held.pop(name, (None, 0))
            return data


def rendered(matrix, probe, seconds, millis):
    """The answer on the wire: a fixed header, the matrix, then the probe."""
    frames, symbols = matrix.shape
    head = struct.pack("<4sHIHfI", MAGIC, VERSION, frames, symbols,
                       seconds, millis)
    return (head
            + matrix.astype("<f2").tobytes()
            + probe.astype("<f4").tobytes())


class Handler(BaseHTTPRequestHandler):
    engine = None
    token = None
    takes = Takes()

    protocol_version = "HTTP/1.1"

    def log_message(self, form, *arguments):
        # One line per request, on stdout, without the client address: what is
        # useful here is what a pass cost, and nothing about who asked.
        print(f"  {form % arguments}", flush=True)

    def fail(self, code, saying):
        body = saying.encode("utf-8")
        self.send_response(code)
        self.send_header("Content-Type", "text/plain; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        if self.path != "/health":
            return self.fail(404, "seules /matrix et /take répondent\n")
        self.fail(200, "ok\n")

    def do_POST(self):
        if self.headers.get("Authorization") != f"Bearer {self.token}":
            # Not a wall against a determined visitor: the point is that an
            # unauthenticated endpoint running a 315 M network on a public
            # address is a free compute service for whoever scans.
            return self.fail(401, "jeton absent ou faux\n")
        address = urllib.parse.urlsplit(self.path)
        length = int(self.headers.get("Content-Length") or 0)
        body = self.rfile.read(length) if length > 0 else b""
        if address.path == "/matrix":
            return self.whole(body)
        named = NAMED.match(address.path)
        if not named:
            return self.fail(404, "seules /matrix et /take répondent\n")
        name, verb = named.groups()
        query = urllib.parse.parse_qs(address.query)
        if verb == "/drop":
            self.takes.taken(name)
            return self.fail(200, "ok\n")
        if verb == "/end":
            return self.end(name, query)
        return self.piece(name, query, body)

    def whole(self, body):
        """A take sent in one go, as a wav."""
        if not body:
            return self.fail(400, "corps vide\n")
        try:
            audio, rate = sf.read(io.BytesIO(body), dtype="float32")
        except Exception as trouble:
            return self.fail(400, f"audio illisible : {trouble}\n")
        if rate != SAMPLE_RATE:
            return self.fail(400, f"{rate} Hz, attendu {SAMPLE_RATE}\n")
        if audio.ndim > 1:
            return self.fail(400, "mono attendu\n")
        self.answer(audio)

    def piece(self, name, query, body):
        """A piece of a take being said, in FLAC, whose samples start at byte `at`.

        Decoded here and held as raw 16-bit samples: `at`, the answer and the
        digest of the close all count bytes of samples, never of what travelled.
        """
        try:
            at = int(query["at"][0])
        except (KeyError, ValueError):
            return self.fail(400, "at manque\n")
        try:
            samples, rate = sf.read(io.BytesIO(body), dtype="int16")
        except Exception as trouble:
            return self.fail(400, f"morceau illisible : {trouble}\n")
        if rate != SAMPLE_RATE or samples.ndim > 1:
            return self.fail(400, f"{rate} Hz sur {samples.ndim} canaux, "
                                  f"attendu {SAMPLE_RATE} Hz mono\n")
        body = samples.astype("<i2").tobytes()
        if at < 0:
            return self.fail(400, "at négatif\n")
        if at + len(body) > MAX_SECONDS * SAMPLE_RATE * 2:
            return self.fail(413, f"plafond {MAX_SECONDS} s\n")
        held = self.takes.piece(name, at, body)
        if held is None:
            return self.fail(503, "trop de prises ouvertes\n")
        answer = json.dumps({"held": held}).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(answer)))
        self.end_headers()
        self.wfile.write(answer)

    def end(self, name, query):
        """The take closed: read it, if it is exactly what the phone holds."""
        data = self.takes.taken(name)
        if data is None:
            return self.fail(404, "prise inconnue ou oubliée\n")
        try:
            size = int(query["bytes"][0])
            digest = query["sha256"][0]
        except (KeyError, ValueError):
            return self.fail(400, "bytes et sha256 manquent\n")
        if len(data) != size or hashlib.sha256(data).hexdigest() != digest:
            # Read anyway, this would put marks on an audio nobody said.
            return self.fail(409, f"{len(data)} octets tenus, {size} annoncés, "
                                  "ou pas les mêmes\n")
        # The same numbers soundfile gives a 16-bit wav: each sample over 32768.
        audio = np.frombuffer(bytes(data), dtype="<i2").astype(np.float32) / 32768
        self.answer(audio)

    def answer(self, audio):
        seconds = len(audio) / SAMPLE_RATE
        if seconds > MAX_SECONDS:
            return self.fail(413, f"{seconds:.1f} s, plafond {MAX_SECONDS} s\n")

        matrix, probe, spent = self.engine.read(audio)
        answer = rendered(matrix, probe, seconds, int(spent * 1000))
        self.send_response(200)
        self.send_header("Content-Type", "application/octet-stream")
        self.send_header("Content-Length", str(len(answer)))
        self.end_headers()
        self.wfile.write(answer)
        print(f"  {seconds:.1f} s → {spent:.2f} s, {len(answer)/1024:.0f} Ko",
              flush=True)


def main():
    token = os.environ.get("SAYLUNE_TOKEN")
    if not token:
        raise SystemExit("SAYLUNE_TOKEN manque — le port ne s'ouvre pas sans")
    home = Path(os.environ.get("SAYLUNE_HOME", "weights")).expanduser()
    home.mkdir(parents=True, exist_ok=True)
    port = int(os.environ.get("PORT", "8080"))
    threads = int(os.environ.get("SAYLUNE_THREADS", "4"))
    concurrency = int(os.environ.get("SAYLUNE_CONCURRENCY", "2"))

    print(f"poids dans {home.resolve()}", flush=True)
    Handler.engine = Engine(home, threads, concurrency)
    Handler.token = token
    print(f"chargé en {Handler.engine.load:.2f} s, "
          f"{threads} fils, {concurrency} passes à la fois", flush=True)
    print(f"écoute sur 0.0.0.0:{port}", flush=True)
    ThreadingHTTPServer(("0.0.0.0", port), Handler).serve_forever()
    return 0


if __name__ == "__main__":
    sys.exit(main())
