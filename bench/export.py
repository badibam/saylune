#!/usr/bin/env python3
"""Turn the chosen candidate into the one file a phone has to carry.

The desktop reads the network through PyTorch, which no phone will run. What
runs there is ONNX Runtime over a graph exported once, so the graph has to be
produced here and proved to say the same thing before anything is measured on a
device -- otherwise a difference seen on the phone could be the phone, the
runtime, or the export, with no way to tell which.

What the graph carries is deliberately the whole brick 2 contract, waveform in,
matrix out: the softmax is inside, so the file answers "the spread over every
sound, every 20 ms" rather than "logits", and the Android side has nothing to
reimplement but the preparation.

    python3 export.py            # the chosen candidate, float then 8-bit
"""

import argparse
import sys
from pathlib import Path

import numpy as np
import soundfile as sf

import matrix

HERE = Path(__file__).resolve().parent

# One second of connected speech is enough to trace the graph: the sample axis
# is exported as dynamic, so the length used here decides nothing.
TRACED = HERE / "out" / "renders" / "azure-gb-sonia" / "sentences" / "think.wav"

# The exported graph must not merely run, it must say what PyTorch says. This is
# the tolerance under which the two are the same reading: the comparison lives
# on what surrounds the peak, and a thousandth is already ten times finer than
# the spread between two synthetic voices the montage was founded on.
SAME_READING = 1e-3


def wrapped(net, torch):
    """The network plus its softmax, so the file's output is the matrix itself."""

    class Matrix(torch.nn.Module):
        def __init__(self):
            super().__init__()
            self.net = net

        def forward(self, input_values):
            return torch.softmax(self.net(input_values).logits, dim=-1)

    return Matrix().eval()


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.parse_args(argv)

    try:
        from onnxruntime.quantization import QuantType, quantize_dynamic
    except ImportError:
        raise SystemExit(
            "onnx et onnxruntime sont absents :\n"
            "  pip install onnx onnxruntime")

    if not TRACED.is_file():
        raise SystemExit(f"{TRACED} manque — rends d'abord le matériel du banc")

    extractor, net, _, torch = matrix.loaded()
    audio, rate = sf.read(TRACED)

    # The preparation is the one piece the phone reimplements, so it is checked
    # here rather than trusted: the numpy form the readings use has to be the
    # form the network was trained to receive.
    reference = extractor(audio, sampling_rate=rate,
                          return_tensors="pt").input_values.numpy()
    prepared = matrix.prepared(audio)
    drift = float(np.abs(reference - prepared).max())
    print(f"préparation numpy vs extracteur : {drift:.2e}")
    if drift > SAME_READING:
        raise SystemExit("la préparation diverge — le reste ne veut rien dire")

    matrix.ONNX_WEIGHTS.parent.mkdir(parents=True, exist_ok=True)
    plain = matrix.ONNX_WEIGHTS.parent / f"{matrix.CHOSEN}.onnx"
    values = torch.from_numpy(prepared)
    expected = wrapped(net, torch)(values).numpy()[0]

    print(f"\nexport de {matrix.MODEL}")
    torch.onnx.export(
        wrapped(net, torch), (values,), str(plain),
        input_names=["input_values"], output_names=["probabilities"],
        dynamic_axes={"input_values": {1: "samples"},
                      "probabilities": {1: "frames"}},
        opset_version=17)
    print(f"  {plain.name}  {plain.stat().st_size / 1e6:.0f} Mo")

    quantised = plain.parent / f"{matrix.CHOSEN}-int8.onnx"
    quantize_dynamic(str(plain), str(quantised), weight_type=QuantType.QInt8)
    print(f"  {quantised.name}  {quantised.stat().st_size / 1e6:.0f} Mo")

    print("\nécart à la lecture PyTorch, sur le fichier tracé :")
    for path in (plain, quantised):
        import onnxruntime
        session = onnxruntime.InferenceSession(
            str(path), providers=["CPUExecutionProvider"])
        got = session.run(None, {"input_values": prepared})[0][0]
        if got.shape != expected.shape:
            raise SystemExit(f"{path.name} rend {got.shape}, "
                             f"attendu {expected.shape}")
        gap = float(np.abs(got - expected).max())
        verdict = "même lecture" if gap <= SAME_READING else "DIVERGE"
        print(f"  {path.name:<28}{gap:.2e}  {verdict}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
