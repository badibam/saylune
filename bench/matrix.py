#!/usr/bin/env python3
"""The acoustic matrix, and the two ways of reading it.

One pass of a phoneme-level wav2vec2 over an audio file gives, every 20 ms, the
spread of resemblance over every sound the network knows. Everything the local
pipeline does is a reading of that table -- free decoding on one side, forced
alignment on the other -- so it is computed once per audio and cached.

Nothing here consults a dictionary, a dialect lexicon or a grapheme-to-phoneme
table: the only question ever asked of the network is what a recording sounds
like, never what a word should sound like.
"""

import json
import os
from collections import namedtuple
from pathlib import Path

import numpy as np
import soundfile as sf

# The acoustic model is a parameter, because which one to run is exactly what
# the bench is measuring. Anything with frame-level logits over a phone
# inventory fits: what is read is the spread, never the label.
#
# What differs between candidates is where the inventory lives and how the
# waveform is prepared, and a checkpoint that omits those files still has to be
# describable. `vocabulary` names the repo holding vocab.json when it is not the
# model's own; `extractor` says whether the repo carries its own preparation or
# the standard one applies.
ROOT = Path(__file__).resolve().parent.parent

Candidate = namedtuple("Candidate", "model vocabulary extractor")

CANDIDATES = {
    "espeak": Candidate("facebook/wav2vec2-lv-60-espeak-cv-ft", None, "repo"),
    "gruut": Candidate("bookbot/wav2vec2-ljspeech-gruut", None, "repo"),
    "charsiu": Candidate("charsiu/en_w2v2_fc_10ms",
                         "charsiu/tokenizer_en_cmu", "standard"),
    "timit-ipa": Candidate("vitouphy/wav2vec2-xls-r-300m-timit-phoneme",
                           None, "repo"),
    "timit": Candidate("excalibur12/wav2vec2-large-lv60_phoneme-timit"
                       "_english_timit-4k_simplified", None, "repo"),
    # Fine-tuned here: a directory rather than a repo. The weights are ours, the
    # vocabulary stays vitouphy's -- that is what makes the reading before and
    # after a comparison at iso-alphabet -- and the checkpoint carries no
    # preparation of its own, the standard one applying.
    **{f"v1-pw0.1-e{epoch}": Candidate(
        str(ROOT / f"tmp/train/runs/v1-pw0.1/epoch-{epoch:03d}"),
        "vitouphy/wav2vec2-xls-r-300m-timit-phoneme", "standard")
       for epoch in (9, 19, 29)},
}

CHOSEN = os.environ.get("ACOUSTIC_MODEL", "espeak")
if CHOSEN not in CANDIDATES:
    raise SystemExit(f"Unknown model {CHOSEN!r}. Known: "
                     + ", ".join(CANDIDATES))
CANDIDATE = CANDIDATES[CHOSEN]
MODEL = CANDIDATE.model

# Weights stored as 8-bit integers instead of 32-bit floats: a quarter of the
# size, and what will actually run on a phone. This reading is the one most
# exposed to it -- it lives on what surrounds the peak, which is the first thing
# rounding takes -- so the bench has to be able to run both and compare.
QUANTISED = os.environ.get("QUANTISED") == "1"

# How this network is rounded for the phone. Named per network rather than
# assumed, because the two do not survive the same rounding: what the spread
# around the peak can lose is not what the position of the peak can.
ROUNDED = "int8"

# Which machine reads the network. PyTorch is the desktop reference and no
# phone runs it; ONNX Runtime is what will, so the bench has to be able to read
# the same files through it and be held to the same numbers. The two readings
# never share a cache: telling them apart is the whole point.
ONNX = os.environ.get("RUNTIME") == "onnx"

# A reading does not have to have been computed here. A phone writes its
# matrices into the cache like any other, and naming that reading is what lets
# every brick of the bench run on it unchanged -- the question "does the verdict
# hold on the device" is then asked by the same script that asks it here.
BORROWED = os.environ.get("READING")
SLUG = BORROWED or (CHOSEN + ("-onnx" if ONNX else "")
                    + ("-int8" if QUANTISED else ""))
SAMPLE_RATE = 16000

HERE = Path(__file__).resolve().parent
ONNX_WEIGHTS = (HERE / "out" / "onnx"
                / f"{CHOSEN}{'-int8' if QUANTISED else ''}.onnx")

# What each model calls "nothing is being pronounced here". A frame classifier
# names silence outright and keeps a padding token beside it that means nothing
# acoustic, so the explicit silence is tried first. This symbol is the one the
# readings must recognise: the grid drops it, the alignment threads through it.
PAD = ("[SIL]", "<pad>", "[PAD]")

# Not sounds. A word separator, and the several names annotators give to
# silence -- a pause between words, the closure before a plosive, the boundary
# of an utterance. Letting any of them into the grid would set the comparison
# to work on emptiness, and make the alignment thread through a boundary as if
# it had been spoken.
NOT_A_SOUND = ("|", "h#", "pau", "epi", " ")

_loaded = None
_configured = None
_symbols = None
_session = None


def cached():
    """The weights cache, never the default one: /home is a tmpfs here."""
    if not os.environ.get("HF_HOME"):
        raise SystemExit(
            "Missing environment variable: HF_HOME (the weights cache)")
    from huggingface_hub import hf_hub_download

    def fetch(repo, filename):
        if not Path(repo).is_dir():
            return hf_hub_download(repo, filename)
        local = Path(repo) / filename
        if not local.is_file():
            raise SystemExit(f"{filename} absent from {repo}")
        return str(local)
    return fetch


def loaded():
    """Feature extractor, network and symbol table, loaded once.

    The tokenizer is deliberately not loaded: it exists to turn text into
    phonemes, which is the one direction this pipeline refuses to travel, and
    asking for it drags in a phonemizer backend the montage has no use for.
    """
    global _loaded
    if _loaded is None:
        cached()
        import torch
        from transformers import AutoFeatureExtractor, AutoModelForCTC

        torch.set_grad_enabled(False)
        if CANDIDATE.extractor == "repo":
            extractor = AutoFeatureExtractor.from_pretrained(MODEL)
        else:
            from transformers import Wav2Vec2FeatureExtractor
            extractor = Wav2Vec2FeatureExtractor(
                feature_size=1, sampling_rate=SAMPLE_RATE, padding_value=0.0,
                do_normalize=True, return_attention_mask=False)
        net = AutoModelForCTC.from_pretrained(MODEL).eval()
        if QUANTISED and not ONNX:
            net = torch.ao.quantization.quantize_dynamic(
                net, {torch.nn.Linear}, dtype=torch.qint8)
        _loaded = (extractor, net, symbols(), torch)
    return _loaded


def configured():
    """The checkpoint's own description, weights untouched.

    Read from the config file rather than from a loaded network, because what
    is asked of it -- the frame rate -- is needed by every reading, including
    the ones that never load PyTorch at all.
    """
    global _configured
    if _configured is None:
        _configured = json.load(open(
            cached()(MODEL, "config.json"), encoding="utf-8"))
    return _configured


def prepared(audio):
    """The waveform as the network expects it: zero mean, unit variance.

    Written out rather than delegated to the feature extractor because this is
    the one step of the pipeline the phone has to reimplement, and a bench that
    hid it behind a library would never have said what to reimplement. All the
    candidates normalise; `export.py` holds the numpy form to the extractor's.
    """
    audio = np.asarray(audio, dtype=np.float32)
    return ((audio - audio.mean())
            / np.sqrt(audio.var() + 1e-7)).astype(np.float32)[None, :]


def seconds_per_frame():
    """Read off the convolutions rather than declared: the stack decimates the
    waveform by the product of its strides, and a candidate that halves the last
    one doubles the resolution."""
    strides = configured()["conv_stride"]
    product = 1
    for stride in strides:
        product *= stride
    return product / SAMPLE_RATE


def session():
    """The exported graph, loaded once. No PyTorch anywhere in this reading."""
    global _session
    if _session is None:
        import onnxruntime
        if not ONNX_WEIGHTS.is_file():
            raise SystemExit(f"{ONNX_WEIGHTS} manque — python3 export.py")
        _session = onnxruntime.InferenceSession(
            str(ONNX_WEIGHTS), providers=["CPUExecutionProvider"])
    return _session


def symbols():
    """The symbol table, from the vocabulary file rather than from a network."""
    global _symbols
    if _symbols is None:
        vocab = json.load(open(
            cached()(CANDIDATE.vocabulary or MODEL, "vocab.json"),
            encoding="utf-8"))
        table = [None] * len(vocab)
        for token, index in vocab.items():
            table[index] = token
        _symbols = table
    return _symbols


def blank():
    for name in PAD:
        if name in symbols():
            return symbols().index(name)
    raise SystemExit(f"No blank symbol among {PAD} in {MODEL}")


def probabilities(wav, cache=None):
    """The matrix of `wav`: one row per 20 ms, one column per sound, summing to 1."""
    wav = Path(wav)
    if cache is not None and Path(cache).is_file():
        return np.load(cache)["probabilities"]
    if BORROWED:
        # Computing here would quietly fill someone else's reading with ours,
        # and the comparison would then be with itself.
        raise SystemExit(f"{cache} manque dans la lecture {BORROWED} — "
                         "elle ne se calcule pas ici")

    audio, rate = sf.read(wav)
    if rate != SAMPLE_RATE:
        raise SystemExit(f"{wav} is at {rate} Hz, expected {SAMPLE_RATE}")
    values = prepared(audio)
    if ONNX:
        # The softmax lives inside the exported graph, so what comes out is the
        # matrix itself -- the same contract the phone will be held to.
        probabilities = session().run(
            None, {"input_values": values})[0][0].astype(np.float32)
    else:
        _, net, _, torch = loaded()
        logits = net(torch.from_numpy(values)).logits[0]
        probabilities = torch.softmax(logits, dim=-1).numpy().astype(np.float32)

    if cache is not None:
        Path(cache).parent.mkdir(parents=True, exist_ok=True)
        np.savez_compressed(cache, probabilities=probabilities)
    return probabilities


def spoken():
    """The columns that stand for a sound: silence and notation excluded."""
    empty = blank()
    return [index for index, name in enumerate(symbols())
            if index != empty and name not in NOT_A_SOUND]


def grid(probabilities):
    """Free decoding: the sounds this voice actually produced, as frame spans.

    Not the "correct" sounds of the words -- the ones that are there. The label
    may be wrong without harm, since it never enters the comparison; only the
    spans matter, because they are what tells two matrices which rows face which.
    """
    best = probabilities.argmax(axis=-1)
    ignored = {blank()}
    ignored.update(symbols().index(name) for name in NOT_A_SOUND
                   if name in symbols())
    segments = []
    start = 0
    for frame in range(1, len(best) + 1):
        if frame == len(best) or best[frame] != best[start]:
            if best[start] not in ignored:
                segments.append((int(best[start]), start, frame))
            start = frame
    return segments


def align(probabilities, ids, empty=None):
    """Forced alignment: where the learner says each sound of the model's grid.

    The correspondence between the two recordings passes through a shared
    symbolic landmark, never through the resemblance of the two signals -- which
    is what keeps the difference of voice out of it.

    `empty` names the blank column, and is asked for rather than looked up when
    the matrix comes from another network than the chosen one: the trellis is
    the same whatever the symbols stand for, sounds or letters.
    """
    log = np.log(np.maximum(probabilities, 1e-12))
    if empty is None:
        empty = blank()
    extended = [empty]
    for index in ids:
        extended += [index, empty]

    width, length = len(extended), len(log)
    if length < width:
        raise ValueError(f"{length} frames cannot spell {len(ids)} sounds")

    score = np.full((length, width), -np.inf)
    back = np.zeros((length, width), dtype=np.int8)
    score[0, 0] = log[0, extended[0]]
    score[0, 1] = log[0, extended[1]]
    for frame in range(1, length):
        for step in range(width):
            best, origin = score[frame - 1, step], 0
            if step > 0 and score[frame - 1, step - 1] > best:
                best, origin = score[frame - 1, step - 1], 1
            skippable = (step > 1 and extended[step] != empty
                         and extended[step] != extended[step - 2])
            if skippable and score[frame - 1, step - 2] > best:
                best, origin = score[frame - 1, step - 2], 2
            score[frame, step] = best + log[frame, extended[step]]
            back[frame, step] = origin

    step = width - 1
    if score[-1, width - 2] > score[-1, width - 1]:
        step = width - 2
    path = np.empty(length, dtype=int)
    for frame in range(length - 1, -1, -1):
        path[frame] = step
        step -= back[frame, step]

    spans = []
    for position in range(len(ids)):
        frames = np.nonzero(path == 2 * position + 1)[0]
        spans.append((int(frames[0]), int(frames[-1]) + 1)
                     if len(frames) else None)
    return spans
