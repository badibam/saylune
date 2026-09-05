#!/usr/bin/env python3
"""Does the probe's own confidence match how marked a stress actually sounds?

`hear.py --couples` counts the marks brick 7 would paint on native speech that
carries no stress fault, and sweeps a threshold over the model's confidence --
keep only the words whose elected syllable leads the runner-up by enough. The
sweep says what that threshold costs in marks. It cannot say whether it is
*measuring the right thing*: the probe's confidence judging the probe's
confidence is one instrument grading itself.

So the words it drops are cut out of the model renders and put to an ear, mixed
with words it keeps. The word and its syllables are shown, because the answer
needs them; the probe's shares and which side of the bar the clip falls on are
revealed only after the answer, for the reason the listening pass of `hear.py`
gives -- shown first, they tell the ear what to hear.

    ACOUSTIC_MODEL=timit-ipa python3 clarity.py --cut    # the clips, from the renders
    ACOUSTIC_MODEL=timit-ipa python3 clarity.py          # the listening pass
    ACOUSTIC_MODEL=timit-ipa python3 clarity.py --report # the tally, without listening

A threshold that keeps a false mark is the other half of the question, and it
needs a different pass: both recordings of each surviving disagreement, asked
which syllable carries the stress. `--pairs` cuts and asks those.

    ACOUSTIC_MODEL=timit-ipa python3 clarity.py --pairs --cut
    ACOUSTIC_MODEL=timit-ipa python3 clarity.py --pairs
    ACOUSTIC_MODEL=timit-ipa python3 clarity.py --pairs --report

Verdicts land in `reviews/hear-clarity.json`, versioned: written once by hand and
regenerable by nothing. The clips are not -- they come back from the renders.
"""

import argparse
import json
import random
import shutil
import sys
from pathlib import Path

import numpy as np
import soundfile as sf

import atomic

import hear
import join
import matrix
import overlap
import review
import syllables

HERE = Path(__file__).resolve().parent
CLIPS = HERE / "out" / "clarity"
VERDICTS = HERE / "reviews" / "hear-clarity.json"
# The second question, and its own material: the false marks the threshold keeps.
PAIRS = HERE / "out" / "clarity-pairs"
PAIR_VERDICTS = HERE / "reviews" / "hear-pairs.json"

# The bar the sweep of `hear.py --couples` puts the question at. Not a setting of
# the app: what is being asked is whether a bar in this region separates what an
# ear separates, and 0.95 is where the sweep's two readings agree.
BAR = 0.95

# A word with a little air on either side, as everywhere else in the bench. Wider
# than `review.PAD` because these words run to five syllables and the ear needs
# to hear the whole shape arrive and leave.
PAD = 0.20

# Controls are drawn from the words the bar keeps, and they have to be the same
# shape as the ones it drops -- those are the long words, and a two-syllable
# witness would compare nothing.
CONTROLS = 4
LONG = 3

SCALE = {"1": "aucun appui net", "2": "appui audible", "3": "appui franc"}


def words(voice, stem, text, tools):
    """Every word of one model render: its syllables, its shares, its span.

    Read on the model alone. The margin the threshold reads is a property of the
    model's own recording -- what the learner did with the word does not enter
    it -- so there is one reading per (voice, word) and not one per pairing.
    """
    mean, deviation, weight, bias, layer = tools
    wav = hear.RENDERS / voice / "timit" / f"{stem}.wav"
    if not overlap.readable(wav):
        return []
    tag = f"{stem}/{voice}"
    cache = overlap.MATRICES / "hear" / f"{tag.replace('/', '-')}.npz"
    segments = matrix.grid(matrix.probabilities(wav, cache=cache))
    if not segments:
        return []
    sounds = join.joined(wav, text, cache=cache)
    cuts = syllables.cut(sounds)
    hidden = hear.states(wav, layer)
    step = matrix.seconds_per_frame()

    out = []
    for start, stop in join.runs(sounds):
        word = sounds[start].word.strip(".,!?'").lower()
        if word in syllables.FUNCTION:
            continue
        pieces = [p for p in cuts if start <= p.sounds[0] and p.sounds[1] <= stop]
        if len(pieces) < 2:
            continue
        shares, broken = [], False
        for piece in pieces:
            low, high = piece.sounds
            heads = matrix.nuclei([s.symbol for s in sounds[low:high]])
            if not heads:
                broken = True
                break
            _, first, last = segments[low + heads[0]]
            last = max(last, first + 1)
            if last > len(hidden):
                broken = True
                break
            shares.append(float(np.dot(
                (hidden[first:last].mean(axis=0) - mean) / deviation, weight) + bias))
        if broken:
            continue
        odds = np.exp(np.array(shares) - max(shares))
        part = odds / odds.sum()
        ranked = np.sort(part)[::-1]
        out.append({"word": word, "voice": voice, "wav": wav,
                    "syllabes": [p.letters for p in pieces],
                    "part": part.round(2).tolist(),
                    "élue": int(np.argmax(part)),
                    "marge": float(ranked[0] - ranked[1]),
                    "from": segments[start][1] * step,
                    "to": segments[stop - 1][2] * step})
    return out


def cut(tools, seed):
    """The clips, and the order they will be asked in.

    Shuffled once and written down rather than shuffled at each run: a pass
    resumed tomorrow has to ask in the same order, or its two halves are not one
    measurement.
    """
    found = []
    for stem, text in hear.TEXTS:
        for voice in hear.VOICES:
            found.extend(words(voice, stem, text, tools))
        print(f"  {stem} lu", file=sys.stderr, flush=True)
    if not found:
        raise SystemExit("aucun rendu modèle lisible — `hear.py --render` les fait")

    # Ordered on the margin at full precision, not on the two decimals shown:
    # a dozen words sit at a rounded 1.00 and rounding first would leave their
    # order to chance, so which four became the controls could not be regrown.
    dropped = sorted((one for one in found if one["marge"] < BAR),
                     key=lambda one: one["marge"])
    kept = sorted((one for one in found if one["marge"] >= BAR),
                  key=lambda one: (-one["marge"], -len(one["syllabes"])))
    controls = [one for one in kept if len(one["syllabes"]) >= LONG][:CONTROLS]

    if CLIPS.exists():
        shutil.rmtree(CLIPS)
    CLIPS.mkdir(parents=True)
    index = []
    for family, rows in (("jeté", dropped), ("témoin", controls)):
        for number, one in enumerate(rows, 1):
            name = (f"{'jete' if family == 'jeté' else 'temoin'}-{number:02d}_"
                    f"{one['word']}_{one['voice']}.wav")
            audio, rate = sf.read(one["wav"])
            low = max(0, int((one["from"] - PAD) * rate))
            high = min(len(audio), int((one["to"] + PAD) * rate))
            sf.write(CLIPS / name, audio[low:high], rate)
            index.append({"clip": name, "word": one["word"],
                          "voice": one["voice"], "syllabes": one["syllabes"],
                          "part": one["part"], "marge": round(one["marge"], 6),
                          "élue": one["syllabes"][one["élue"]],
                          "famille": family})
    random.Random(seed).shuffle(index)
    atomic.write_text(CLIPS / "index.json",
                      json.dumps(index, indent=2, ensure_ascii=False) + "\n")
    print(f"\n{len(dropped)} jetés sous {BAR}, {len(controls)} témoins, "
          f"mêlés → {CLIPS}/index.json")
    return 0


def listed():
    path = CLIPS / "index.json"
    if not path.is_file():
        raise SystemExit(f"{path} manque — `--cut` découpe les mots des rendus")
    return json.loads(path.read_text(encoding="utf-8"))


def stored():
    if not VERDICTS.is_file():
        return {}
    return json.loads(VERDICTS.read_text(encoding="utf-8"))


def save(held):
    atomic.write_text(VERDICTS, json.dumps(held, indent=2, ensure_ascii=False) + "\n")


def listen(pad, slow):
    index = listed()
    held = stored()
    player = None
    print(f"\n  {len(index)} mots. Le mot joue, tu dis à quel point l'appui "
          "s'entend.\n")
    for key, name in SCALE.items():
        print(f"    {key}  {name}")
    print("    r  rejouer      s  au ralenti      q  quitter\n")
    for one in index:
        if one["clip"] in held:
            continue
        wav = CLIPS / one["clip"]
        print(f"  {one['word']}    {' - '.join(one['syllabes'])}"
              f"    ({one['voice']})")
        player = review.play(wav, 0.0, 1e6, player, pad=pad)
        while True:
            key = review.ask("    ton oreille [1/2/3, r, s, q] : ")
            if key is None or key == "q":
                save(held)
                print(f"\n  {len(held)} jugés → {VERDICTS}")
                return 0
            if key in ("r", "s"):
                player = review.play(wav, 0.0, 1e6, player, pad=pad,
                                     slow=slow if key == "s" else 1.0)
                continue
            if key in SCALE:
                held[one["clip"]] = key
                save(held)
                parts = "  ".join(f"{s}[{p:.2f}]" for s, p
                                  in zip(one["syllabes"], one["part"]))
                print(f"    sonde : {one['élue']}  marge {one['marge']:.2f}"
                      f"    ({one['famille']})\n    {parts}\n")
                break
    return report()


def report():
    index, held = listed(), stored()
    if not held:
        raise SystemExit(f"{VERDICTS} est vide — rien n'a encore été écouté")
    print(f"\n=== la marge du modèle contre l'oreille — "
          f"{len(held)} mots jugés sur {len(index)}\n")
    print(f"  {'':<10}{'aucun':>8}{'audible':>10}{'franc':>8}")
    for family in ("jeté", "témoin"):
        counts = {key: 0 for key in SCALE}
        for one in index:
            if one["famille"] == family and one["clip"] in held:
                counts[held[one["clip"]]] += 1
        print(f"  {family:<10}{counts['1']:>8}{counts['2']:>10}{counts['3']:>8}")
    print()
    for one in sorted(index, key=lambda one: one["marge"]):
        if one["clip"] in held:
            print(f"  {one['word']:<15}{one['voice']:<18}"
                  f"marge {one['marge']:.2f}  {one['famille']:<8}"
                  f"élue {one['élue']:<7}oreille : {SCALE[held[one['clip']]]}")
    print(f"\n  « jeté » = sous la marge de {BAR}, donc écarté par le seuil ; "
          "« témoin » = gardé\n  par lui, tiré parmi les mots aussi longs pour "
          "que la comparaison porte.")
    return 0


def surviving(tools, seed):
    """The disagreements the threshold does not catch, cut from both sides.

    A disagreement under the bar has an explanation the ear has confirmed: the
    word carries no audible stress, so each side elects on nothing and they
    differ. Above the bar that explanation is gone, and what is left is
    unmeasured -- hence both recordings, asked apart. The clips are shuffled
    together and say nothing of which side they are: knowing would make the
    second answer a comparison instead of a hearing.
    """
    mean, deviation, weight, bias, layer = tools
    step = matrix.seconds_per_frame()
    cases = []
    for stem, text in hear.TEXTS:
        natives = hear.prompts(stem)
        for voice in hear.VOICES:
            model_wav = hear.RENDERS / voice / "timit" / f"{stem}.wav"
            if not overlap.readable(model_wav):
                continue
            model_cache = overlap.MATRICES / "hear" / f"{stem}-{voice}.npz"
            segments = matrix.grid(matrix.probabilities(model_wav, cache=model_cache))
            if not segments:
                continue
            sounds = join.joined(model_wav, text, cache=model_cache)
            cuts = syllables.cut(sounds)
            model_hidden = hear.states(model_wav, layer)
            for speaker, learner_wav in natives:
                learner_cache = overlap.MATRICES / "hear" / f"{stem}-{speaker}.npz"
                spans = matrix.align(
                    matrix.probabilities(learner_wav, cache=learner_cache),
                    [index for index, _, _ in segments])
                learner_hidden = hear.states(learner_wav, layer)

                def share(hidden, places):
                    got = []
                    for low, high in places:
                        high = max(high, low + 1)
                        if high > len(hidden):
                            return None
                        got.append(float(np.dot(
                            (hidden[low:high].mean(axis=0) - mean) / deviation,
                            weight) + bias))
                    odds = np.exp(np.array(got) - max(got))
                    return odds / odds.sum()

                for start, stop in join.runs(sounds):
                    word = sounds[start].word.strip(".,!?\'").lower()
                    if word in syllables.FUNCTION:
                        continue
                    pieces = [p for p in cuts
                              if start <= p.sounds[0] and p.sounds[1] <= stop]
                    if len(pieces) < 2:
                        continue
                    at, broken = [], False
                    for piece in pieces:
                        low, high = piece.sounds
                        heads = matrix.nuclei([s.symbol for s in sounds[low:high]])
                        if not heads or spans[low + heads[0]] is None:
                            broken = True
                            break
                        at.append(low + heads[0])
                    if broken:
                        continue
                    here = share(model_hidden, [segments[one][1:] for one in at])
                    there = share(learner_hidden, [spans[one] for one in at])
                    if here is None or there is None:
                        continue
                    ranked = np.sort(here)[::-1]
                    margin = float(ranked[0] - ranked[1])
                    if (int(np.argmax(here)) == int(np.argmax(there))
                            or margin < BAR):
                        continue
                    cases.append({
                        "word": word, "voice": voice, "speaker": speaker,
                        "syllabes": [p.letters for p in pieces],
                        "marge": round(margin, 6),
                        "modèle": (model_wav, segments[start][1] * step,
                                   segments[stop - 1][2] * step,
                                   int(np.argmax(here)), here.round(2).tolist()),
                        "apprenant": (learner_wav,
                                      min(spans[one][0] for one in at) * step,
                                      max(spans[one][1] for one in at) * step,
                                      int(np.argmax(there)),
                                      there.round(2).tolist())})
        print(f"  {stem} lu", file=sys.stderr, flush=True)

    if PAIRS.exists():
        shutil.rmtree(PAIRS)
    PAIRS.mkdir(parents=True)
    index = []
    for number, case in enumerate(cases, 1):
        for side in ("modèle", "apprenant"):
            wav, low_s, high_s, elected, part = case[side]
            audio, rate = sf.read(wav)
            low = max(0, int((low_s - PAD) * rate))
            high = min(len(audio), int((high_s + PAD) * rate))
            name = (f"cas-{number}-{'a' if side == 'modèle' else 'b'}_"
                    f"{case['word']}.wav")
            sf.write(PAIRS / name, audio[low:high], rate)
            index.append({"clip": name, "word": case["word"], "cas": number,
                          "syllabes": case["syllabes"], "côté": side,
                          "source": (case["voice"] if side == "modèle"
                                     else case["speaker"]),
                          "marge": case["marge"], "élue": elected,
                          "part": part})
    random.Random(seed).shuffle(index)
    atomic.write_text(PAIRS / "index.json",
                      json.dumps(index, indent=2, ensure_ascii=False) + "\n")
    print(f"\n{len(cases)} désaccords à marge ≥ {BAR}, {len(index)} clips "
          f"→ {PAIRS}/index.json")
    return 0


def pair_listen(pad, slow):
    path = PAIRS / "index.json"
    if not path.is_file():
        raise SystemExit(f"{path} manque — `--pairs --cut` découpe les deux côtés")
    index = json.loads(path.read_text(encoding="utf-8"))
    held = (json.loads(PAIR_VERDICTS.read_text(encoding="utf-8"))
            if PAIR_VERDICTS.is_file() else {})
    player = None
    print(f"\n  {len(index)} enregistrements. Sur chacun : quelle syllabe "
          "porte l'appui ?\n")
    print("    un chiffre  la syllabe, dans l'ordre affiché")
    print("    ?           aucune ne ressort")
    print("    x           la découpe en syllabes est fausse")
    print("    r  rejouer      s  au ralenti      q  quitter\n")
    for one in index:
        if one["clip"] in held:
            continue
        wav = PAIRS / one["clip"]
        listed = "   ".join(f"{i + 1}. {s}"
                            for i, s in enumerate(one["syllabes"]))
        print(f"  {one['word']}    {listed}")
        player = review.play(wav, 0.0, 1e6, player, pad=pad)
        keys = {str(i + 1) for i in range(len(one["syllabes"]))}
        while True:
            key = review.ask(f"    l'appui [1-{len(one['syllabes'])}, ?, x, "
                             "r, s, q] : ")
            if key is None or key == "q":
                atomic.write_text(PAIR_VERDICTS,
                                  json.dumps(held, indent=2, ensure_ascii=False) + "\n")
                print(f"\n  {len(held)} jugés → {PAIR_VERDICTS}")
                return 0
            if key in ("r", "s"):
                player = review.play(wav, 0.0, 1e6, player, pad=pad,
                                     slow=slow if key == "s" else 1.0)
                continue
            if key in keys or key in ("?", "x"):
                held[one["clip"]] = key
                atomic.write_text(PAIR_VERDICTS,
                                  json.dumps(held, indent=2, ensure_ascii=False) + "\n")
                parts = "  ".join(f"{s}[{p:.2f}]" for s, p
                                  in zip(one["syllabes"], one["part"]))
                print(f"    sonde : {one['syllabes'][one['élue']]}"
                      f"    ({one['côté']}, {one['source']})\n    {parts}\n")
                break
    return pair_report()


def pair_report():
    path = PAIRS / "index.json"
    if not path.is_file():
        raise SystemExit(f"{path} manque — `--pairs --cut` découpe les deux côtés")
    index = json.loads(path.read_text(encoding="utf-8"))
    held = (json.loads(PAIR_VERDICTS.read_text(encoding="utf-8"))
            if PAIR_VERDICTS.is_file() else {})
    if not held:
        raise SystemExit(f"{PAIR_VERDICTS} est vide — rien n'a encore été écouté")
    print("\n=== les fausses marques que le seuil garde, côté par côté\n")
    for number in sorted({one["cas"] for one in index}):
        pair = [one for one in index if one["cas"] == number]
        print(f"  cas {number} — {pair[0]['word']}   "
              f"({'   '.join(pair[0]['syllabes'])})   "
              f"marge du modèle {pair[0]['marge']:.2f}")
        for one in sorted(pair, key=lambda one: one["côté"]):
            answer = held.get(one["clip"])
            if answer is None:
                continue
            said = (one["syllabes"][int(answer) - 1] if answer.isdigit()
                    else {"?": "aucune ne ressort",
                          "x": "découpe fausse"}[answer])
            print(f"      {one['côté']:<11}{one['source']:<18}"
                  f"sonde : {one['syllabes'][one['élue']]:<8}oreille : {said}")
        print()
    return 0


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--cut", action="store_true",
                        help="redécouper les clips depuis les rendus")
    parser.add_argument("--pairs", action="store_true",
                        help="les fausses marques que le seuil garde, "
                             "les deux côtés de chacune")
    parser.add_argument("--report", action="store_true",
                        help="le décompte, sans écouter")
    parser.add_argument("-p", "--pad", type=float, default=PAD,
                        help=f"l'air autour du mot, en secondes (défaut {PAD})")
    parser.add_argument("-s", "--slow", type=float, default=0.5,
                        help="la vitesse de la touche `s`, hauteur conservée")
    parser.add_argument("--seed", type=int, default=0,
                        help="l'ordre des clips ; il est écrit dans l'index")
    args = parser.parse_args(argv)
    if args.pairs:
        if args.report:
            return pair_report()
        if args.cut:
            return surviving(hear.probe(), args.seed)
        return pair_listen(args.pad, args.slow)
    if args.report:
        return report()
    if args.cut:
        return cut(hear.probe(), args.seed)
    return listen(args.pad, args.slow)


if __name__ == "__main__":
    sys.exit(main())
