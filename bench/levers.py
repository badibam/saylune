#!/usr/bin/env python3
"""The two levers Piper hides, opened, and what they buy.

The cast of the Free door wants characters that act, and no published engine
carries all three of a clean sound, per-phoneme control and many voices
(`../docs/design/local-chain.md`). This is the way round that wall which costs
no download: the model already computes what we want, it simply keeps it to
itself, and an ONNX graph is editable.

Two internal tensors become inputs. `w_ceil`, the frames each phoneme is given,
which is rhythm. And the row `sid` fetches out of the 904 x 512 speaker table,
which is timbre -- a point one can move rather than an index one can only pick.
Both keep their predicted value as an output, so a first pass reports what the
model decided and a second sends it back changed: the design rule is that we
modulate, never overwrite, since numbers invented from nothing take the model
out of its domain.

Both draws get a seed, and every render opens its own session -- onnxruntime
seeds when the session opens and advances from there, so within one session the
second call already draws different noise and no A against B would mean
anything.

    python3 levers.py patch        write the model with both levers
    python3 levers.py rhythm       the round trip, then one word held
    python3 levers.py act          the five characters' rhythm on the line
    python3 levers.py space        what the 904 speaker rows look like
    python3 levers.py voices       placements in the speaker space
    python3 levers.py cast         where the wall is, and random directions
    python3 levers.py varied       twelve draws across the usable radius
    python3 levers.py creatures    three of them through the filter's presets

Judged by ear, no key, offline.
"""

import argparse
import json
import sys
from pathlib import Path

import numpy as np
import onnx
import soundfile as sf
from onnx import helper, numpy_helper, TensorProto

import atomic
import creature

HERE = Path(__file__).resolve().parent
SOURCE = HERE.parent / "tmp" / "piper" / "en_US-libritts_r-medium.onnx"
LEVERS = HERE.parent / "tmp" / "piper" / "levers.onnx"
OUT = HERE / "out" / "levers"

DURATION = "/Ceil_output_0"          # frames per phoneme, [batch, 1, phonemes]
ROW = "/emb_g/Gather_output_0"       # the speaker's 512 numbers
TABLE = "emb_g.weight"
SEED = 1234.0
HOP = 256                            # samples per frame, read off a render
ANCHOR = 0

# The line the marking test used, so a rendering here and a marking there are
# read against each other (`../bench/marking/`). It is also the second half of
# the creature bench's line.
LINE = "I asked you twice, and you said nothing."
MARKS = HERE / "marking" / "answers" / "numbers"


# ------------------------------------------------------------ the surgery

def patch():
    model = onnx.load(str(SOURCE))
    graph = model.graph
    for node in graph.node:
        if node.op_type == "RandomNormalLike":
            node.attribute.extend([helper.make_attribute("seed", SEED)])

    def opened(name, kind, shape, tensor):
        """One hidden tensor, switchable between predicted and given."""
        graph.input.extend([
            helper.make_tensor_value_info(name, TensorProto.FLOAT, shape),
            helper.make_tensor_value_info(kind, TensorProto.BOOL, [1]),
        ])
        chosen = f"/{name}_chosen"
        where = helper.make_node("Where", [kind, name, tensor], [chosen],
                                 name=f"/{name}_where")
        for node in graph.node:
            for i, taken in enumerate(node.input):
                if taken == tensor:
                    node.input[i] = chosen
        after = next(i for i, n in enumerate(graph.node) if tensor in n.output)
        graph.node.insert(after + 1, where)

    opened("w_ceil", "use_w", ["batch_size", 1, "phonemes"], DURATION)
    opened("g", "use_g", [1, 512], ROW)

    value = next((v for v in graph.value_info if v.name == DURATION), None)
    graph.output.append(value if value is not None else helper.make_tensor_value_info(
        DURATION, TensorProto.FLOAT, ["batch_size", 1, "phonemes"]))

    onnx.checker.check_model(model)
    with atomic.opened(LEVERS, "wb") as handle:
        handle.write(model.SerializeToString())
    LEVERS.with_suffix(".onnx.json").write_bytes(
        SOURCE.with_suffix(".onnx.json").read_bytes())
    print(f"written {LEVERS} ({LEVERS.stat().st_size / 1e6:.0f} MB)")


def speakers():
    """The 904 rows, straight out of the file."""
    model = onnx.load(str(SOURCE), load_external_data=False)
    weight = next(i for i in model.graph.initializer if i.name == TABLE)
    return numpy_helper.to_array(weight)


class Voice:
    """The patched model. A session per render, for the seed to hold."""

    def __init__(self, path=LEVERS):
        if not path.exists():
            raise SystemExit(f"No patched model at {path} -- run `levers.py patch` first.")
        self.path = str(path)
        config = json.loads(Path(str(path) + ".json").read_text(encoding="utf-8"))
        self.rate = config["audio"]["sample_rate"]
        self.id_map = {k: v for k, v in config["phoneme_id_map"].items()}
        from piper.phonemize_espeak import EspeakPhonemizer
        self.phonemizer = EspeakPhonemizer()

    def symbols(self, text):
        return self.phonemizer.phonemize("en-us", text)[0]

    def say(self, symbols, held=None, vector=None, sid=ANCHOR):
        import onnxruntime as ort
        from piper.phoneme_ids import phonemes_to_ids
        ids = phonemes_to_ids(symbols, self.id_map)
        feed = {
            "input": np.array([ids], dtype=np.int64),
            "input_lengths": np.array([len(ids)], dtype=np.int64),
            "scales": np.array([0.333, 1.0, 0.333], dtype=np.float32),
            "sid": np.array([sid], dtype=np.int64),
            "w_ceil": (np.zeros((1, 1, len(ids)), dtype=np.float32)
                       if held is None else held.astype(np.float32)),
            "use_w": np.array([held is not None]),
            "g": (np.zeros((1, 512), dtype=np.float32) if vector is None
                  else vector.reshape(1, 512).astype(np.float32)),
            "use_g": np.array([vector is not None]),
        }
        session = ort.InferenceSession(self.path, providers=["CPUExecutionProvider"])
        audio, widths = session.run(None, feed)
        return audio.squeeze().astype(np.float32), widths


def write(path, x, rate, normalise=True):
    peak = float(np.max(np.abs(x)))
    if peak == 0:
        raise SystemExit(f"Silence came out of {path.name}.")
    if normalise:
        x = x / peak * 0.89
    path.parent.mkdir(parents=True, exist_ok=True)
    with atomic.opened(path) as handle:
        sf.write(handle, x.astype(np.float32), rate, subtype="PCM_16", format="WAV")
    print(f"  {path.relative_to(HERE.parent)}  {len(x) / rate:.2f} s")


# ------------------------------------------------------------ the rhythm

def bare(word):
    return word.strip(".,!?;:").lower()


def runs_of(symbols):
    """Each word's stretch of phonemes. The ids run BOS, pad, phoneme, pad, ...
    so phoneme i owns slots 2 + 2i and 3 + 2i."""
    out, start = [], 0
    for i, symbol in enumerate(symbols):
        if symbol == " ":
            if i > start:
                out.append((start, i))
            start = i + 1
    if len(symbols) > start:
        out.append((start, len(symbols)))
    return out


def slots_of(symbols):
    return [[s for i in range(lo, hi) for s in (2 + 2 * i, 3 + 2 * i)]
            for lo, hi in runs_of(symbols)]


def held(widths, slots, factor):
    """The word's frames multiplied, the remainder spread over its phonemes.

    Rounding each phoneme up on its own turns a one-frame phoneme into two --
    x2 where x1.6 was asked. The factor belongs to the word, so the word's
    total is what gets rounded and the largest fractions take the remainder.
    """
    live = [s for s in slots if s < widths.shape[-1]]
    values = [float(widths[0, 0, s]) for s in live]
    total = sum(values)
    if total <= 0:
        return
    target = max(1, int(round(total * factor)))
    wanted = [v * target / total for v in values]
    given = [max(int(np.floor(w)), 1) if v > 0 else 0 for w, v in zip(wanted, values)]
    order = sorted(range(len(live)), key=lambda i: wanted[i] - given[i], reverse=True)
    for i in order[:max(0, target - sum(given))]:
        given[i] += 1
    for s, g in zip(live, given):
        widths[0, 0, s] = float(g)


def rhythm(args):
    """The control, then one word held -- the two facts the lever rests on."""
    voice = Voice()
    line = "The chair is very comfortable, and the coffee is getting cold."
    symbols = voice.symbols(line)
    plain, widths = voice.say(symbols)
    again, widths2 = voice.say(symbols)
    print(f"same twice: durations {np.array_equal(widths, widths2)}, "
          f"audio {np.array_equal(plain, again)}")
    back, _ = voice.say(symbols, widths)
    print(f"round trip: identical {np.array_equal(plain, back)}")

    words = [bare(w) for w in line.split()]
    stretched = widths.copy()
    held(stretched, slots_of(symbols)[words.index("very")], 1.6)
    longer, _ = voice.say(symbols, stretched)
    print(f"'very' x1.6: {len(plain) / voice.rate:.2f} s -> "
          f"{len(longer) / voice.rate:.2f} s")
    write(OUT / "rhythm" / "plain.wav", plain, voice.rate, normalise=False)
    write(OUT / "rhythm" / "round-trip.wav", back, voice.rate, normalise=False)
    write(OUT / "rhythm" / "stretched.wav", longer, voice.rate, normalise=False)


def act(args):
    """The five characters, rhythm only, from the marking test's own numbers.

    Their `rate`, the `weight` they hung on a word and their pauses all reach
    `w_ceil`. Their `range`, `level`, `lift` and `volume` do not: VITS holds no
    pitch, so those stay with signal work afterwards, and mixing them in here
    would hide what the rhythm does on its own.

    The pause is a cut in the wave, at the quietest frame of the run-up rather
    than at the boundary the alignment declares -- the nasal murmur starts
    before that boundary, so silence dropped there is heard inside the word.
    """
    voice = Voice()
    symbols = voice.symbols(LINE)
    words = [bare(w) for w in LINE.split()]
    slots = slots_of(symbols)
    plain, widths = voice.say(symbols)
    write(OUT / "act" / "control.wav", plain, voice.rate, normalise=False)

    for path in sorted(MARKS.glob("*.json")):
        sheet = json.loads(path.read_text(encoding="utf-8"))
        weights = {bare(w["word"]): w["weight"] for w in sheet.get("words", [])}
        driven = widths.copy()
        for word, own in zip(words, slots):
            held(driven, own, sheet["rate"] * weights.get(word, 1.0))
        audio, _ = voice.say(symbols, driven)

        pauses = {bare(p["before"]): p["seconds"] for p in sheet.get("pauses", [])}
        pieces, cut = [], 0
        for word, own in zip(words, slots):
            if word not in pauses or not own:
                continue
            declared = int(np.sum(driven[0, 0, :own[0]]))
            loudness = lambda f: float(np.sqrt(np.mean(
                audio[f * HOP:(f + 1) * HOP] ** 2)))
            at = min(range(declared - 8, declared + 1), key=loudness) * HOP
            pieces.append(audio[cut:at])
            pieces.append(np.zeros(int(pauses[word] * voice.rate), dtype=np.float32))
            cut = at
        pieces.append(audio[cut:])
        write(OUT / "act" / f"{sheet['character']}.wav",
              np.concatenate(pieces), voice.rate, normalise=False)


# ------------------------------------------------------------ the timbre

def space(args):
    """Where the 904 sit, which says what moving among them means."""
    rows = speakers()
    mean = rows.mean(axis=0)
    spread = np.linalg.norm(rows - mean, axis=1)
    print(f"{rows.shape[0]} rows of {rows.shape[1]}")
    print(f"  from the average: min {spread.min():.2f}  "
          f"median {np.median(spread):.2f}  max {spread.max():.2f}")
    values = np.linalg.svd(rows - mean, compute_uv=False)
    energy = np.cumsum(values ** 2) / np.sum(values ** 2)
    for k in (1, 10, 100):
        print(f"  first {k:3d} directions hold {energy[k - 1] * 100:5.1f} %")


def voices(args):
    """Placements: the anchor, the average nobody occupies, and outwards."""
    rows, voice = speakers(), Voice()
    mean = rows.mean(axis=0)
    anchor = rows[ANCHOR]
    far = int(np.argmax(np.linalg.norm(rows - mean, axis=1)))
    made = {
        "a-by-number": None,
        "b-by-vector": anchor,
        "c-halfway-to-far": (anchor + rows[far]) / 2.0,
        "d-the-average-voice": mean,
        "e-twice-as-far": mean + 2.0 * (anchor - mean),
        "f-four-times-as-far": mean + 4.0 * (anchor - mean),
        "g-away-from-everyone": mean - 3.0 * (anchor - mean),
        "h-longer-vector": anchor * 1.6,
        "i-shorter-vector": anchor * 0.5,
    }
    symbols = voice.symbols(LINE)
    for name, vector in made.items():
        audio, _ = voice.say(symbols, vector=vector)
        write(OUT / "voices" / f"{name}.wav", audio, voice.rate)
    control, _ = voice.say(symbols)
    same, _ = voice.say(symbols, vector=anchor)
    print(f"control: the number and its own vector agree: "
          f"{np.array_equal(control, same)}")


def drawn(count=12, seed=11):
    """The draws the varied listen judged, redrawn from their seed."""
    rows = speakers()
    mean = rows.mean(axis=0)
    rng = np.random.default_rng(seed)
    radii = [4.5, 4.5, 7.0, 7.0, 7.0, 10.5, 10.5, 10.5, 10.5, 13.0, 13.0, 13.0]
    out = []
    for radius in radii[:count]:
        step = rng.normal(size=512)
        out.append(mean + step * radius / np.linalg.norm(step))
    return out


def cast(args):
    """Two questions: how far out a mouth survives, and whether direction alone
    makes a different voice."""
    rows, voice = speakers(), Voice()
    mean = rows.mean(axis=0)
    anchor = rows[ANCHOR] - mean
    reach = float(np.linalg.norm(anchor))
    symbols = voice.symbols(LINE)
    for k in (2.5, 3.0, 3.5, 4.0):
        audio, _ = voice.say(symbols, vector=mean + k * anchor)
        write(OUT / "cast" / f"wall-{k:.1f}x.wav", audio, voice.rate)
    rng = np.random.default_rng(7)
    for n in range(5):
        step = rng.normal(size=512)
        step *= 2.0 * reach / np.linalg.norm(step)
        audio, _ = voice.say(symbols, vector=mean + step)
        write(OUT / "cast" / f"direction-{n + 1}.wav", audio, voice.rate)


def varied(args):
    """Twelve draws across the radius the wall left usable."""
    voice = Voice()
    symbols = voice.symbols(LINE)
    for n, vector in enumerate(drawn(), 1):
        audio, _ = voice.say(symbols, vector=vector)
        write(OUT / "varied" / f"v{n:02d}.wav", audio, voice.rate)


def creatures(args):
    """Three of the draws through the same presets, grouped by preset.

    The question is not whether the creatures are good -- `creature.py` asked
    that. It is whether the filter keeps what separated the mouths, or flattens
    three voices into one goblin.
    """
    voice = Voice()
    symbols = voice.symbols(LINE)
    vectors = drawn()
    bases = {"v01": 1, "v06": 6, "v07": 7}
    for name, place in bases.items():
        audio, _ = voice.say(symbols, vector=vectors[place - 1])
        write(OUT / "creatures" / f"dry-{name}.wav", audio, voice.rate)
    for preset in ("goblin", "ogre", "wraith"):
        for name, place in bases.items():
            audio, _ = voice.say(symbols, vector=vectors[place - 1])
            made = creature.apply(audio, voice.rate, creature.CAST[preset])
            write(OUT / "creatures" / f"{preset}-{name}.wav", made, voice.rate)


MODES = {"patch": lambda a: patch(), "rhythm": rhythm, "act": act, "space": space,
         "voices": voices, "cast": cast, "varied": varied, "creatures": creatures}


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("mode", choices=tuple(MODES))
    args = parser.parse_args()
    MODES[args.mode](args)


if __name__ == "__main__":
    main()
