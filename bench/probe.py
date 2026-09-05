#!/usr/bin/env python3
"""Does the network already know where the stress is, and can a probe read it?

Every acoustic cue computed by hand plateaus at 86.7 % of designation on TIMIT,
and the four montages of brick 7 are mute (`../docs/analysis.md`). A linear
probe on one layer of the model **already in service** designates at 96.7 % --
so the information is there, and what was poor was the reading.

That first probe was fitted on TIMIT: native American English, read cleanly,
with nucleus bounds placed by hand. It is judged on learner speech whose bounds
the network drew. This file closes that gap: it fits on the **learner** corpus,
whose annotation carries the stressed nucleus in its ARPAbet digits (`Z IH1 R
OW0`), with the bounds the app itself would use.

Only correctly stressed words train it. On a word the corpus calls wrong the
annotation gives the *canonical* position while the speaker stressed another, so
training on it would teach the probe to point at a syllable the audio does not
carry. Those words are what it is judged on instead.

    cd bench && python3 probe.py --extract -s train    # long, and it is the cost
    cd bench && python3 probe.py --extract -s test
    cd bench && python3 probe.py                       # every layer, judged on test
    cd bench && python3 probe.py --fit 19              # freeze one, for accent.py

**The corpus underneath is abandoned** (2026-09-01, `../TODO.md`): what this file does
stands, what it reads does not. SpeechOcean pins its scores to no instant, so the model
voice had to be synthesised for the syllable cut alone; L2-ARCTIC carries bounds placed by
hand and that bill goes away with the corpus. Before any of it, one number is owed and it
is free: `out/probe/test.npz` holds 17 words where the half carries 2259 with a marked
stress, and the filter that lost them -- the grid and the annotation must count the same
syllables -- will apply to the next corpus unchanged. Re-run the test extraction and read
the yield before hoping anything of a new corpus.
"""

import argparse
import json
import sys
from pathlib import Path

import numpy as np
import soundfile as sf

import atomic

import join
import learners
import matrix
import overlap
import syllables

HERE = Path(__file__).resolve().parent
STORE = HERE / "out" / "probe"

# The probe the app will carry, in the shape `freeze` writes it. Frozen on
# TIMIT, where the nuclei bounds are hand-placed -- which is why it is this one
# and not the L2 probe, whose corpus was dropped along with the montage that
# read it (`../TODO.md`).
FROZEN = STORE / "probe-timit-19.npz"

# The eligibility bar of the app, on the model side: a word whose elected
# syllable does not lead the runner-up by this much receives no mark at all.
# Set by measure, not by taste -- between 0.80 and 0.94 the sweep of
# `hear.py --couples` renders the same thing, and 0.95 would throw away a word
# an ear heard as plainly stressed, for no mark gained (`docs/analysis.md`).
BAR = 0.90

VOICE = "azure-us-jenny"

# The nuclei of the corpus's own alphabet. Its digits are what say which syllable
# carries the stress, and they are the whole reason this corpus can train anything.
ARPA_NUCLEI = frozenset(
    "AA AE AH AO AW AY EH ER EY IH IY OW OY UH UW".split()
)


def marked(word):
    """Which nucleus of a word the corpus calls the strong one, or None."""
    nuclei = [p for p in word.phones
              if p[-1:].isdigit() and p[:-1] in ARPA_NUCLEI]
    if len(nuclei) < 2:
        return None
    for rank, phone in enumerate(nuclei):
        if phone.endswith("1"):
            return rank, len(nuclei)
    return None


def states(wav, net, torch):
    """One recording, every layer, at the frame rate the matrix uses."""
    matrix.heard(wav)
    samples, _ = sf.read(wav, dtype="float64")
    out = net(torch.from_numpy(matrix.prepared(samples)), output_hidden_states=True)
    return [one[0].numpy().astype(np.float32) for one in out.hidden_states]

def frozen():
    """The frozen linear probe: standardisation, weights, bias.

    The probe travels with this file -- fit, frozen, read through the exported
    graph -- and every other instrument borrows it from here.
    """
    if not FROZEN.is_file():
        raise SystemExit(f"{FROZEN} manque — la sonde figée est ce qui est "
                         "jugé partout, et écouter sans elle ne mesurerait rien")
    held = np.load(FROZEN)
    return (held["mean"], held["deviation"], held["weight"],
            float(held["bias"][0]), int(held["layer"]))


def hidden(wav, layer):
    """One recording's hidden layer, at the frame rate the matrix uses."""
    _, net, _, torch = matrix.loaded()
    matrix.heard(wav)
    samples, _ = sf.read(wav, dtype="float64")
    out = net(torch.from_numpy(matrix.prepared(samples)),
              output_hidden_states=True)
    return out.hidden_states[layer][0].numpy().astype(np.float32)


def parts(hidden_states, places, tools):
    """The probe's part of the stress on each nucleus span, softmaxed over the word.

    A span is a (first, last) pair of frames, as every reading hands them out:
    the mean of the layer over the span, standardised with the frozen moments,
    dotted with the frozen weight. The softmax turns the scores of one word
    into parts summing to one -- what "the elected syllable" and the margin are
    read off. None when a span runs past the layer: the caller drops the word
    rather than reading a short syllable.
    """
    mean, deviation, weight, bias, _ = tools
    scores = []
    for low, high in places:
        high = max(high, low + 1)
        if high > len(hidden_states):
            return None
        frame = hidden_states[low:high].mean(axis=0)
        scores.append(float(np.dot((frame - mean) / deviation, weight) + bias))
    odds = np.exp(np.array(scores) - max(scores))
    return (odds / odds.sum()).tolist()


def extract(split, budget, out):
    _, net, _, torch = matrix.loaded()
    takes, _ = learners.chosen(budget, split, 0)
    learners.extract(takes, split)

    rows, labels, words, stress, kept = [], [], [], [], 0
    for index, take in enumerate(takes, 1):
        model = HERE / "out" / "renders" / VOICE / "l2" / f"{take.uid}.wav"
        learner = learners.wav(take)
        if not (overlap.readable(model) and overlap.readable(learner)):
            continue
        model_cache = overlap.MATRICES / f"l2-{VOICE}" / f"{take.uid}.npz"
        take_cache = overlap.MATRICES / f"l2-{take.uid}" / f"{take.uid}.npz"

        theirs = matrix.probabilities(model, cache=model_cache)
        segments = matrix.grid(theirs)
        if not segments:
            continue
        mine = matrix.probabilities(learner, cache=take_cache)
        aligned = matrix.align(mine, [i for i, _, _ in segments])
        sounds = join.joined(model, take.text, cache=model_cache)
        cuts = syllables.cut(sounds)

        wanted = {}
        for written in take.words:
            found = marked(written)
            if found:
                wanted[written.text.lower()] = (found, written.stress)
        if not wanted:
            continue

        layers = None
        for start, stop in join.runs(sounds):
            key = sounds[start].word.strip(".,!?'").lower()
            if key not in wanted or key in syllables.FUNCTION:
                continue
            (rank, count), verdict = wanted[key]
            pieces = [p for p in cuts
                      if start <= p.sounds[0] and p.sounds[1] <= stop]
            # The grid must find the same number of syllables the annotation
            # names, or "the third nucleus" means two different things on the
            # two sides and the label lands on the wrong one.
            if len(pieces) != count:
                continue
            at = []
            for piece in pieces:
                low, high = piece.sounds
                heads = matrix.nuclei([s.symbol for s in sounds[low:high]])
                if not heads or aligned[low + heads[0]] is None:
                    at = []
                    break
                at.append(low + heads[0])
            if not at:
                continue
            if layers is None:
                layers = states(learner, net, torch)
            for place, sound in enumerate(at):
                low, high = aligned[sound]
                high = max(high, low + 1)
                rows.append(np.stack([one[low:high].mean(axis=0) for one in layers])
                            .astype(np.float16))
                labels.append(1 if place == rank else 0)
                words.append(kept)
                stress.append(verdict)
            kept += 1
        if index % 50 == 0:
            print(f"  {index}/{len(takes)} — {kept} mots", file=sys.stderr, flush=True)

    if not rows:
        raise SystemExit(
            f"aucun mot lisible dans {split} — les rendus modèles manquent-ils ? "
            f"`python3 alarms.py -s {split} -b 0 -c {VOICE} -p` le dit, et `-r -y` "
            "les synthétise, et c'est là que les caractères se dépensent")

    with atomic.opened(out) as handle:
        np.savez_compressed(handle, states=np.stack(rows),
                            labels=np.array(labels), words=np.array(words),
                            stress=np.array(stress))
    print(f"{split} : {kept} mots, {len(rows)} syllabes → {out}")


def fit(train_x, train_y, torch, steps=400):
    """One logistic regression, full batch, weight decay -- nothing tuned."""
    x = torch.from_numpy(train_x)
    y = torch.from_numpy(train_y.astype(np.float32))
    net = torch.nn.Linear(x.shape[1], 1)
    optimiser = torch.optim.Adam(net.parameters(), lr=0.01, weight_decay=1e-3)
    loss = torch.nn.BCEWithLogitsLoss()
    with torch.enable_grad():
        for _ in range(steps):
            optimiser.zero_grad()
            value = loss(net(x)[:, 0], y)
            value.backward()
            optimiser.step()
    return net


def prepared(held, layer, clean_only):
    """One layer's syllables, kept to the words the labels are true of."""
    keep = held["stress"] == 10 if clean_only else np.ones(len(held["stress"]), bool)
    return (held["states"][keep][:, layer].astype(np.float32),
            held["labels"][keep], held["words"][keep])


def report(layers_wanted):
    import torch
    train = np.load(STORE / "train.npz")
    test = np.load(STORE / "test.npz")
    layers = layers_wanted or range(train["states"].shape[1])
    print(f"\n=== la sonde sur la parole d'apprenant — "
          f"{len(np.unique(train['words']))} mots d'entraînement, "
          f"{len(np.unique(test['words']))} de test")
    print("\n      couche   syllabe      le mot")
    print("      repères, sur TIMIT : 96,7 % (sonde native), 86,7 % (meilleure règle)")
    for layer in layers:
        x, y, _ = prepared(train, layer, clean_only=True)
        mean, deviation = x.mean(axis=0), x.std(axis=0) + 1e-6
        net = fit((x - mean) / deviation, y, torch)
        tx, ty, tw = prepared(test, layer, clean_only=True)
        with torch.no_grad():
            scores = net(torch.from_numpy((tx - mean) / deviation))[:, 0].numpy()
        right = total = 0
        for word in np.unique(tw):
            spot = np.nonzero(tw == word)[0]
            total += 1
            right += int(ty[spot][int(scores[spot].argmax())] == 1)
        print(f"      {layer:>6} {100 * ((scores > 0).astype(int) == ty).mean():>8.1f} %"
              f" {100 * right / total:>10.1f} %")


def portable():
    """The frozen probe as JSON, the shape the app reads beside its weights.

    The app carries no numpy: the three float32 vectors and the bias leave the
    npz for one JSON file, printed at full precision so the Kotlin side reads
    back exactly the numbers the bench judged. Lands beside the exported graph,
    since a probe belongs to the weights it was fitted on -- pushing one
    without the other would read stress off a model that never produced the
    states it was trained on.
    """
    mean, deviation, weight, bias, layer = frozen()
    out = matrix.ONNX_WEIGHTS.parent / "probe.json"
    data = {"layer": layer, "mean": mean.tolist(),
            "deviation": deviation.tolist(), "weight": weight.tolist(),
            "bias": bias}
    atomic.write_text(out, json.dumps(data, ensure_ascii=False) + "\n")
    print(f"couche {layer}, {len(mean)} poids → {out}")
    return 0


def freeze(layer):
    import torch
    train = np.load(STORE / "train.npz")
    x, y, _ = prepared(train, layer, clean_only=True)
    mean, deviation = x.mean(axis=0), x.std(axis=0) + 1e-6
    net = fit((x - mean) / deviation, y, torch)
    out = STORE / f"probe-l2-{layer}.npz"
    with atomic.opened(out) as handle:
        np.savez(handle, layer=layer, mean=mean, deviation=deviation,
                 weight=net.weight.detach().numpy()[0],
                 bias=net.bias.detach().numpy())
    print(f"couche {layer} figée sur {len(x)} syllabes d'apprenant → {out}")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--extract", action="store_true")
    parser.add_argument("-s", "--split", default="train")
    parser.add_argument("-b", "--budget", type=int, default=0,
                        help="caractères de modèle du tirage ; 0 = tout le jeu")
    parser.add_argument("--fit", type=int, help="fige cette couche pour accent.py")
    parser.add_argument("--json", action="store_true",
                        help="la sonde figée en JSON, la forme que l'app lit "
                             "à côté de ses poids")
    parser.add_argument("-l", "--layer", type=int, action="append",
                        help="ne juger que cette couche (répétable)")
    args = parser.parse_args(argv)
    if args.extract:
        extract(args.split, args.budget, STORE / f"{args.split}.npz")
        return 0
    if args.fit is not None:
        return freeze(args.fit) or 0
    if args.json:
        return portable()
    return report(args.layer) or 0


if __name__ == "__main__":
    sys.exit(main())
