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

    python3 export.py                # the chosen candidate, float then 8-bit
    python3 export.py -n letters     # the letter network, same treatment
"""

import argparse
import sys
from pathlib import Path

import numpy as np
import soundfile as sf

import letters
import matrix

# Two networks travel to the phone, and they travel the same way: waveform in,
# matrix out, softmax inside the graph. The one that spreads over sounds, and
# the one that spreads over letters and says where each is spoken.
NETWORKS = {"sounds": matrix, "letters": letters}

HERE = Path(__file__).resolve().parent

# One second of connected speech is enough to trace the graph: the sample axis
# is exported as dynamic, so the length used here decides nothing.
TRACED = HERE / "out" / "renders" / "azure-gb-sonia" / "sentences" / "think.wav"

# The exported graph must not merely run, it must say what PyTorch says. This is
# the tolerance under which the two are the same reading: the comparison lives
# on what surrounds the peak, and a thousandth is already ten times finer than
# the spread between two synthetic voices the montage was founded on.
SAME_READING = 1e-3


def weight(path):
    """What the file really costs: the graph plus whatever it keeps beside it.

    Past a size, the exporter puts the tensors in a sidecar rather than in the
    graph, and reading only the graph would report four megabytes for a model
    of one and a quarter gigabytes.
    """
    sidecar = path.parent / f"{path.name}.data"
    return path.stat().st_size + (sidecar.stat().st_size
                                  if sidecar.is_file() else 0)


def folded(net, torch):
    """Compute the weight-normalised weights once, and keep them.

    The positional convolution carries its weight as a norm and a direction,
    multiplied at every forward pass. Exported as such, the convolution has no
    constant weight to quantise -- it has an expression -- and rounding to 8
    bits has nothing to take hold of. Folding is exact: the same product, done
    once instead of at every call.
    """
    for module in net.modules():
        if torch.nn.utils.parametrize.is_parametrized(module):
            for name in list(module.parametrizations):
                torch.nn.utils.parametrize.remove_parametrizations(
                    module, name, leave_parametrized=True)
    return net


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
    parser.add_argument("-n", "--network", default="sounds",
                        choices=sorted(NETWORKS))
    options = parser.parse_args(argv)
    network = NETWORKS[options.network]

    try:
        import onnx
        from onnxruntime.quantization import QuantType, quantize_dynamic
        from onnxruntime.transformers.float16 import convert_float_to_float16
    except ImportError:
        raise SystemExit(
            "la chaîne d'export est absente :\n"
            "  pip install onnx onnxruntime onnxscript")

    if not TRACED.is_file():
        raise SystemExit(f"{TRACED} manque — rends d'abord le matériel du banc")

    extractor, net, _, torch = network.loaded()
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

    network.ONNX_WEIGHTS.parent.mkdir(parents=True, exist_ok=True)
    plain = network.ONNX_WEIGHTS.parent / f"{network.CHOSEN}.onnx"
    values = torch.from_numpy(prepared)
    # Measured before anything is touched: this is the reading every number
    # written down so far was made against, so it is what the exported graph --
    # weight norm folded and all -- has to reproduce.
    expected = wrapped(net, torch)(values).numpy()[0]

    print(f"\nexport de {network.MODEL}")
    # Traced through the dynamo exporter rather than the TorchScript one, which
    # is deprecated: the graph a phone will carry is not built on a path due to
    # be removed. The sample axis is the only free one -- a turn of speech has
    # no fixed length -- and the opset is pinned so the file does not depend on
    # which version of the exporter happened to run. Pinned to what the exporter
    # actually implements: asking for less makes it emit 18 and then convert
    # down, which is a translation nobody needs standing between the weights and
    # the phone.
    torch.onnx.export(
        wrapped(folded(net, torch), torch), (values,), str(plain), dynamo=True,
        input_names=["input_values"], output_names=["probabilities"],
        dynamic_shapes={"input_values": {1: torch.export.Dim("samples",
                                                             min=4000)}},
        opset_version=18)
    print(f"  {plain.name}  {weight(plain) / 1e6:.0f} Mo")

    # Each network is rounded the way its own reading survives, and the bench
    # decided which by measuring, not by taste.
    rounded = plain.parent / f"{network.CHOSEN}-{network.ROUNDED}.onnx"
    if network.ROUNDED == "int8":
        # Rounded on the same perimeter PyTorch rounds on, and no wider. Left to
        # itself the tool also rounds the convolutions -- the feature extractor,
        # which is where the signal is still a signal -- and measured that way a
        # clean control climbs to 0.851, which is fault territory: the reading
        # stops being the reading the bench qualified. Per-channel because one
        # scale for a whole weight matrix is decided by its largest column, and
        # every other column pays for it.
        quantize_dynamic(str(plain), str(rounded), weight_type=QuantType.QInt8,
                         op_types_to_quantize=["MatMul"], per_channel=True)
    else:
        # Sixteen-bit floats, because eight-bit integers cost this network its
        # anchoring outright: a word opened 144 ms from where the provider opens
        # it instead of 8, and the softness was in every word, not in one. The
        # input and the output stay in 32 bits -- the phone hands over a waveform
        # and reads a spread, neither side knowing how the weights are stored.
        half = convert_float_to_float16(onnx.load(str(plain)),
                                        keep_io_types=True)
        onnx.save(half, str(rounded), save_as_external_data=True,
                  all_tensors_to_one_file=True,
                  location=f"{rounded.name}.data")
    print(f"  {rounded.name}  {weight(rounded) / 1e6:.0f} Mo")

    print("\nécart à la lecture PyTorch en flottant, sur le fichier tracé :")
    import onnxruntime
    for path in (plain, rounded):
        session = onnxruntime.InferenceSession(
            str(path), providers=["CPUExecutionProvider"])
        got = session.run(None, {"input_values": prepared})[0][0]
        if got.shape != expected.shape:
            raise SystemExit(f"{path.name} rend {got.shape}, "
                             f"attendu {expected.shape}")
        gap = float(np.abs(got - expected).max())
        print(f"  {path.name:<28}{gap:.2e}")

        # Only the 32-bit graph is held to the reference here. The rounded one
        # is meant to differ -- rounding is the whole point of it -- and how
        # much of that difference survives to the verdict is not a question a
        # single file answers.
        if path is plain and gap > SAME_READING:
            raise SystemExit("  l'export n'est pas fidèle — rien au-dessus "
                             "ne veut dire quoi que ce soit")
    verdict = ("QUANTISED=1 RUNTIME=onnx python3 faults.py"
               if network is matrix else
               "LETTERS_QUANTISED=1 LETTERS_RUNTIME=onnx python3 anchor.py")

    print("\n  l'arrondi déplace la matrice ; ce qu'il déplace du verdict :"
          f"\n  {verdict}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
