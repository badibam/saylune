#!/usr/bin/env python3
"""Which syllable an ear hears as the strong one, against which one the probe elects.

Brick 7 has never had a reference. Every number written about it so far was read
off a corpus that scores stress by its own lights -- SpeechOcean762, since
abandoned -- and the one figure that mattered could not be read at all: two
speakers of the same word, both scored correct, are elected different syllables
by the probe **18.9 %** of the time, and nothing in the bench says whether that
is the probe misreading a human or two humans genuinely stressing differently.
An ear separates them, and no corpus we can download does.

So this is a listening pass, and it produces the missing reference: for each
word of more than one syllable, the syllable a person hears as strong.

Three things about the protocol are deliberate, and each buys a number that a
looser one would lose.

- **The judgment is blind.** The probe's election is revealed only after the
  answer. Shown first it would prime the ear into agreeing, and a validation
  pass that agrees because it was told what to hear measures nothing. It costs
  no extra gesture: a digit instead of a yes.
- **Ambiguity is an answer.** `?` says the word carries no syllable the ear
  picks out. Part of the 18.9 % may be exactly those words, and forcing a binary
  would bury the one thing the listening pass is for.
- **A bad cut is its own answer.** `x` says the syllable split itself is wrong,
  so the question does not arise. That is brick 8 failing, not brick 7, and the
  two are worth telling apart before either is judged.

The material crosses the sources on the *same texts*, which is the whole reason
to spend an evening on it: five TIMIT sentences, each read by two natives and
rendered by two synthetic voices from two different providers. What differs
between four readings of `Military personnel are expected to obey government
orders.` is the mouth and nothing else -- so the probe's accuracy per source is
readable directly, and the question "does it read the machine and not the human"
gets an answer instead of an inference.

    ACOUSTIC_MODEL=timit-ipa python3 hear.py --render   # synthesise, once, and it costs
    ACOUSTIC_MODEL=timit-ipa python3 hear.py            # the listening pass
    ACOUSTIC_MODEL=timit-ipa python3 hear.py --report   # the tally, without listening
    ACOUSTIC_MODEL=timit-ipa python3 hear.py --graph    # the probe through the exported graphs

Verdicts land in `reviews/hear-stress.json`, versioned, like every other thing
in this bench written once by hand and regenerable by nothing. The pass resumes
where it stopped: a word already judged is never asked again.
"""

import argparse
import json
import os
import random
import sys
from pathlib import Path

import numpy as np

import atomic

import join
import probe
from probe import BAR, frozen as probe, hidden as states, parts
import matrix
import overlap
import review
import syllables
import synth

HERE = Path(__file__).resolve().parent
CORPUS = HERE.parent / "tmp" / "TIMIT" / "lisa" / "data" / "timit" / "raw" / "TIMIT"
RENDERS = HERE / "out" / "renders"
VERDICTS = HERE / "reviews" / "hear-stress.json"

# Five SX sentences of the TEST half. SX prompts are read by seven speakers
# each, which is what makes the native side a choice rather than a single
# reading; SI prompts are read by one and would give the ear no way to tell a
# speaker apart from a text. Picked for the count of words carrying more than
# one syllable -- a sentence of monosyllables asks the ear nothing -- and for
# plain spelling: an apostrophe or a hyphen sends `join.spoken` down a path that
# is a question of its own, and not this pass's.
TEXTS = (
    ("SX205", "Military personnel are expected to obey government orders."),
    ("SX283", "Planned parenthood organizations promote birth control."),
    ("SX36", "Only the most accomplished artists obtain popularity."),
    ("SX199", "Young children should avoid exposure to contagious diseases."),
    ("SX409", "Eating spinach nightly increases strength miraculously."),
)

# One voice per provider, and one accent each, so a disagreement between them
# cannot be read as either alone. Both are voices the bench has already measured
# elsewhere, which is why they and not the four others.
VOICES = ("azure-us-jenny", "eleven-gb-daniel")

# How many TIMIT speakers per text. Two, one of each recorded sex where the
# corpus offers both: the native side is here to show what a human mouth does to
# the probe, and a single speaker would confound that with one person's habits.
SPEAKERS = 2


def prompts(stem, wanted=SPEAKERS):
    """Every TEST recording of one prompt, as (speaker, wav), sorted.

    TIMIT names a speaker directory by sex then initials (`FAKS0`, `MDAB0`), so
    the letter is the only thing available to balance on -- and balancing is
    worth a line here because two readings are all the native side gets.
    """
    found = sorted(CORPUS.joinpath("TEST").rglob(f"{stem}.WAV"))
    women = [p for p in found if p.parent.name.startswith("F")]
    men = [p for p in found if p.parent.name.startswith("M")]
    picked = []
    for pool in (women, men):
        if pool:
            picked.append(pool[0])
    for path in found:
        if len(picked) >= wanted:
            break
        if path not in picked:
            picked.append(path)
    return [(path.parent.name, path) for path in picked[:wanted]]


def readings(voices, speakers):
    """Every recording this pass judges, each with what it takes to read it.

    A tag names the reading and is what the verdict file is keyed on, so it has
    to survive a re-run unchanged -- hence text and speaker rather than a path,
    which moves the day the corpus is unpacked somewhere else.
    """
    out = []
    for stem, text in TEXTS:
        for speaker, wav in prompts(stem, speakers):
            out.append((f"{stem}/{speaker}", "natif", speaker, stem, text, wav))
        for voice in voices:
            wav = RENDERS / voice / "timit" / f"{stem}.wav"
            out.append((f"{stem}/{voice}", "synthèse", voice, stem, text, wav))
    return out


def rendered(voices, force):
    """Synthesise what the crossing needs, after saying what it will spend.

    Synthesis is the one thing here that costs money and cannot be undone, so it
    is a separate run behind one confirmation rather than a surprise in the
    middle of a listening pass. `synth.render` keeps a file already on disk, so
    a second run of this spends nothing.
    """
    wanted = [(voice, stem, text,
               RENDERS / voice / "timit" / f"{stem}.wav")
              for stem, text in TEXTS for voice in voices]
    missing = [one for one in wanted if not one[3].is_file() or force]
    if not missing:
        print("tous les rendus sont déjà là — rien à dépenser")
        return 0
    characters = sum(len(text) for _, _, text, _ in missing)
    print(f"\n{len(missing)} rendus à synthétiser, {characters} caractères :\n")
    for voice, stem, text, _ in missing:
        print(f"  {voice:<20} {stem:<8} {text}")
    answer = review.ask("\nsynthétiser ? [o/N] ")
    if answer != "o":
        print("annulé — rien n'a été dépensé")
        return 1
    for voice, stem, text, path in missing:
        path.parent.mkdir(parents=True, exist_ok=True)
        synth.render(text, synth.BY_NAME[voice], path, force=force)
        print(f"  {voice:<20} {stem:<8} rendu")
    return 0


def scored(wav, text, tag, tools, source=None):
    """One recording's words: their syllables, their spans, the probe's shares.

    The recording is read alone -- its own grid, its own syllables, its own
    hidden states. Nothing here compares two sides: what the listening pass is
    for is the accuracy of *one* reading against an ear, and the comparison of
    two readings is arithmetic to be done afterwards, on numbers this produces.

    `source` replaces where the hidden states come from, which is what lets the
    same reading run on the exported graphs instead of PyTorch: the words, the
    spans, the bounds all stay exactly as the ear pass saw them, and only the
    states under them change.
    """
    layer = tools[4]
    cache = overlap.MATRICES / "hear" / f"{tag.replace('/', '-')}.npz"
    spread = matrix.probabilities(wav, cache=cache)
    segments = matrix.grid(spread)
    if not segments:
        return []
    sounds = join.joined(wav, text, cache=cache)
    cuts = syllables.cut(sounds)
    hidden = source(wav) if source else states(wav, layer)

    out = []
    for start, stop in join.runs(sounds):
        word = sounds[start].word.strip(".,!?'").lower()
        if word in syllables.FUNCTION:
            continue
        pieces = [piece for piece in cuts
                  if start <= piece.sounds[0] and piece.sounds[1] <= stop]
        # A word the grid gives one syllable carries no stress mark at all
        # (brick 9), so asking about it would ask about something the screen
        # will never draw.
        if len(pieces) < 2:
            continue
        spans, places, broken = [], [], False
        for piece in pieces:
            low, high = piece.sounds
            heads = matrix.nuclei([s.symbol for s in sounds[low:high]])
            if not heads:
                broken = True
                break
            places.append(segments[low + heads[0]][1:])
            spans.append((piece.low, piece.high))
        if broken or len(spans) != len(pieces):
            continue
        got = parts(hidden, places, tools)
        if got is None:
            continue
        out.append({"word": word, "rank": start,
                    "letters": [piece.letters for piece in pieces],
                    "spans": spans,
                    "part": np.round(got, 4).tolist()})
    return out


def stored():
    if not VERDICTS.is_file():
        return {}
    return json.loads(VERDICTS.read_text(encoding="utf-8"))


def save(held):
    atomic.write_text(VERDICTS, json.dumps(held, indent=2, ensure_ascii=False))


def asked(wav, one, player, pad, slow):
    """One word put to the ear, and the key that comes back.

    Split out because the revision pass has to ask **exactly** the same
    question as the first one -- same lines, same keys, nothing about what was
    answered before or what the probe thinks. A second question phrased even
    slightly differently would make the two answers incomparable, which is the
    whole thing a revision is for.

    Returns (answer, player) with `answer` None when the pass is being left.
    """
    low, high = one["spans"][0][0], one["spans"][-1][1]
    shown = "   ".join(f"{rank + 1}. {letters}"
                       for rank, letters in enumerate(one["letters"]))
    print(f"\n  {one['word']}   {shown}")
    player = review.play(wav, low, high, player, pad=pad)
    while True:
        key = review.ask("  > ")
        if key is None or key == "q":
            return None, player
        if key in (" ", ""):
            player = review.play(wav, low, high, player, pad=pad)
        elif key == "p":
            player = review.play(wav, 0.0, 1e6, player, pad=0.0)
        elif key == "s":
            player = review.play(wav, low, high, player, pad=pad, slow=slow)
        elif key == "x":
            return "découpe", player
        elif key == "?":
            return "aucune", player
        elif key.isdigit() and 1 <= int(key) <= len(one["spans"]):
            return int(key) - 1, player


def revealed(one, answer):
    """What the probe elected, said after the answer and never before."""
    elected = int(np.argmax(one["part"]))
    tail = ""
    if isinstance(answer, int):
        tail = " — d'accord" if answer == elected else " — EN DÉSACCORD"
    print(f"    sonde : {one['letters'][elected]} "
          f"({one['part'][elected]:.2f}){tail}")
    return elected


def heading(family, source, text):
    os.system("clear")
    print(f"\n  {family} — {source}\n  {text}\n")
    print("  1..9 la syllabe forte    ? aucune ne ressort    "
          "x la découpe est fausse")
    print("  espace réécouter    p la phrase entière    "
          "s au ralenti    q quitter\n")


def listen(order, tools, pad, slow):
    """The pass itself: one word, one key, and the election shown after.

    Everything the terminal shows before the answer is the question -- the word,
    its syllables, their numbers -- and nothing of what the probe thinks. The
    reveal comes after the key, where it can no longer move the ear.
    """
    held = stored()
    player = None
    done = skipped = 0
    for tag, family, source, stem, text, wav in order:
        if not overlap.readable(wav):
            print(f"  {tag:<28} absent — passé")
            skipped += 1
            continue
        words = scored(wav, text, tag, tools)
        if all(f"{tag}#{one['rank']}" in held for one in words):
            continue
        heading(family, source, text)
        for one in words:
            key_of = f"{tag}#{one['rank']}"
            if key_of in held:
                continue
            answer, player = asked(wav, one, player, pad, slow)
            if answer is None:
                save(held)
                print(f"\n  {done} jugés, {len(held)} au total → {VERDICTS}")
                return
            held[key_of] = {"tag": tag, "famille": family, "source": source,
                            "texte": stem, "word": one["word"],
                            "syllabes": one["letters"], "oreille": answer,
                            "sonde": revealed(one, answer), "part": one["part"]}
            done += 1
            save(held)
    save(held)
    print(f"\n  fini — {done} jugés cette fois, {len(held)} au total")
    if skipped:
        print(f"  {skipped} lectures absentes — `--render` les synthétise")


def sampled(held, seed):
    """The words a revision pass replays: every disagreement, and as many agreements.

    Replaying the disagreements alone would only ever move the number one way --
    an ear that erred on a word the probe happened to match is never asked
    again, so every correction found would raise the probe's score and none
    could lower it. Drawing an equal number of agreements makes the revision
    symmetric, and shuffling them together means the ear cannot tell which kind
    it is hearing. The cost is twice the listening, and it buys the only thing a
    revision is worth: a corrected number that is still a measurement.
    """
    clear = [key for key, one in held.items() if isinstance(one["oreille"], int)]
    apart = [key for key in clear if held[key]["oreille"] != held[key]["sonde"]]
    same = [key for key in clear if held[key]["oreille"] == held[key]["sonde"]]
    dice = random.Random(seed)
    picked = apart + dice.sample(same, min(len(apart), len(same)))
    dice.shuffle(picked)
    return picked


def again(order, tools, pad, slow, seed):
    """Ask a second time, blind, and keep both answers.

    The first answer is not shown and not overwritten: it moves to `oreille 1`,
    so a revision that changed a verdict can always be read as a revision rather
    than passing for what was heard the first time.
    """
    held = stored()
    if not held:
        raise SystemExit(f"{VERDICTS} est vide — rien à réécouter")
    picked = sampled(held, seed)
    if not picked:
        print("aucun désaccord à revoir")
        return
    by_tag = {}
    for key in picked:
        by_tag.setdefault(held[key]["tag"], []).append(key)
    known = {tag: (family, source, stem, text, wav)
             for tag, family, source, stem, text, wav in order}
    player = None
    done = changed = 0
    print(f"\n  {len(picked)} mots à réécouter, mélangés — "
          "rien ne dit lesquels étaient en désaccord\n")
    for tag in [tag for tag in {held[key]["tag"]: None for key in picked}]:
        if tag not in known:
            continue
        family, source, stem, text, wav = known[tag]
        if not overlap.readable(wav):
            continue
        words = {f"{tag}#{one['rank']}": one
                 for one in scored(wav, text, tag, tools)}
        heading(family, source, text)
        for key in by_tag[tag]:
            one = words.get(key)
            if one is None:
                continue
            answer, player = asked(wav, one, player, pad, slow)
            if answer is None:
                save(held)
                print(f"\n  {done} revus, {changed} changés → {VERDICTS}")
                return
            before = held[key]["oreille"]
            held[key]["oreille 1"] = before
            held[key]["oreille"] = answer
            revealed(one, answer)
            done += 1
            if answer != before:
                changed += 1
                print(f"    (première écoute : "
                      f"{one['letters'][before] if isinstance(before, int) else before})")
            save(held)
    save(held)
    print(f"\n  fini — {done} revus, {changed} changés")


def report():
    """What the ear says about the probe, by family and by source.

    The rate that decides everything is the last column: how often the probe
    elects the syllable the ear picked, on words the ear could pick one for. A
    word the ear called ambiguous is not a probe failure and does not count
    against it -- it is counted apart, because its size is a finding of its own.
    """
    held = stored()
    if not held:
        raise SystemExit(f"{VERDICTS} est vide — rien n'a encore été écouté")
    families = {}
    for one in held.values():
        for key in (one["famille"], f"  {one['source']}"):
            slot = families.setdefault(key, {"net": 0, "juste": 0,
                                             "aucune": 0, "découpe": 0})
            if one["oreille"] == "aucune":
                slot["aucune"] += 1
            elif one["oreille"] == "découpe":
                slot["découpe"] += 1
            else:
                slot["net"] += 1
                slot["juste"] += int(one["oreille"] == one["sonde"])
    print(f"\n=== la sonde contre l'oreille — {len(held)} mots jugés\n")
    print(f"  {'':<24}{'tranchés':>9}{'ambigus':>9}{'découpe':>9}"
          f"{'sonde juste':>13}")
    for name, slot in families.items():
        rate = (f"{100 * slot['juste'] / slot['net']:.1f} %"
                if slot["net"] else "—")
        print(f"  {name:<24}{slot['net']:>9}{slot['aucune']:>9}"
              f"{slot['découpe']:>9}{rate:>13}")
    revised = [one for one in held.values() if "oreille 1" in one]
    if revised:
        moved = sum(1 for one in revised if one["oreille"] != one["oreille 1"])
        print(f"\n  {len(revised)} mots réécoutés à l'aveugle, {moved} verdicts "
              "changés — la première\n  écoute reste dans le fichier sous "
              "« oreille 1 ».")
    print("\n  « tranchés » = mots où l'oreille a désigné une syllabe ; c'est "
          "la seule\n  base sur laquelle la sonde est jugée. Les ambigus et les "
          "découpes fausses\n  ne sont pas des erreurs de la sonde et se "
          "comptent à part.")


def paired(model_wav, learner_wav, text, tags, tools, cache={}):
    """Both sides read on the **model's** grid, which is what the app would do.

    Reading each recording alone, as the listening pass does, is the right way
    to put a claim to an ear -- there is one recording in the room. It is not
    how the mark is computed: the montage in place aligns the learner onto the
    model's grid and lets him supply only where he was and how loud, so the two
    sides are indexed by the same syllables and a probe that misplaces a nucleus
    on its own decoding never gets the chance. What that changes is not
    arguable, so it is measured here rather than asserted.
    """
    layer = tools[4]
    model_tag, learner_tag = tags
    model_cache = overlap.MATRICES / "hear" / f"{model_tag.replace('/', '-')}.npz"
    learner_cache = overlap.MATRICES / "hear" / f"{learner_tag.replace('/', '-')}.npz"
    theirs = matrix.probabilities(model_wav, cache=model_cache)
    segments = matrix.grid(theirs)
    if not segments:
        return []
    mine = matrix.probabilities(learner_wav, cache=learner_cache)
    spans = matrix.align(mine, [index for index, _, _ in segments])
    sounds = join.joined(model_wav, text, cache=model_cache)
    cuts = syllables.cut(sounds)
    for key, wav in ((model_tag, model_wav), (learner_tag, learner_wav)):
        if key not in cache:
            cache[key] = states(wav, layer)

    out = []
    for start, stop in join.runs(sounds):
        word = sounds[start].word.strip(".,!?'").lower()
        if word in syllables.FUNCTION:
            continue
        pieces = [piece for piece in cuts
                  if start <= piece.sounds[0] and piece.sounds[1] <= stop]
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
        here = parts(cache[model_tag], [segments[one][1:] for one in at], tools)
        there = parts(cache[learner_tag], [spans[one] for one in at], tools)
        if here is None or there is None:
            continue
        out.append({"word": word, "syllabes": [p.letters for p in pieces],
                    "modèle": here, "apprenant": there})
    return out


def couples(order, tools):
    """Every native against every model voice, on the montage the app uses.

    Nothing here is compared to an ear: what it counts is how often the two
    sides elect different syllables on speech that carries no stress fault, which
    is the rate at which the brick would paint a mark on someone who did nothing
    wrong.

    It also answers, on the same words and for free, whether dropping the ones
    the model does not stress clearly would lower that rate -- the filter
    `../docs/design/activity-model.md` asks for by name.
    """
    by_text = {}
    for tag, family, source, stem, text, wav in order:
        if overlap.readable(wav):
            by_text.setdefault(stem, {"text": text, "natif": [],
                                      "synthèse": []})[family].append((tag, wav))
    tally = {}
    # The model's own clarity on each word, paired with whether that word drew a
    # false mark. Collected here rather than measured apart because the question
    # it answers -- would dropping the words the model does not stress clearly
    # buy anything -- is only worth asking of these very words.
    clarity = []
    for stem, held in sorted(by_text.items()):
        for model_tag, model_wav in held["synthèse"]:
            voice = model_tag.split("/")[-1]
            for learner_tag, learner_wav in held["natif"]:
                rows = paired(model_wav, learner_wav, held["text"],
                              (model_tag, learner_tag), tools)
                slot = tally.setdefault(voice, {"mots": 0, "écarts": 0,
                                                "lesquels": []})
                for one in rows:
                    slot["mots"] += 1
                    here = np.sort(np.array(one["modèle"]))[::-1]
                    apart = (int(np.argmax(one["modèle"]))
                             != int(np.argmax(one["apprenant"])))
                    clarity.append((float(here[0] - here[1]), apart))
                    if apart:
                        slot["écarts"] += 1
                        slot["lesquels"].append(
                            f"{one['word']} ({learner_tag.split('/')[-1]}, "
                            f"marge {here[0] - here[1]:.2f})")
        print(f"  {stem} lu", file=sys.stderr, flush=True)
    print("\n=== le montage du produit : l'apprenant aligné sur la grille du modèle")
    print("    (parole native sans faute d'accent — tout écart est une fausse marque)\n")
    total = apart = 0
    for voice, slot in sorted(tally.items()):
        total += slot["mots"]
        apart += slot["écarts"]
        print(f"  {voice:<20}{slot['écarts']:>4}/{slot['mots']:<5} = "
              f"{100 * slot['écarts'] / slot['mots']:.1f} %")
        for name in sorted(set(slot["lesquels"])):
            print(f"      {name}")
    print(f"\n  {'ensemble':<20}{apart:>4}/{total:<5} = "
          f"{100 * apart / total:.1f} %")
    clarity.sort()
    print("\n=== et si on n'écoutait que les mots que le modèle accentue nettement")
    print("    (« marge » = la part de la syllabe élue moins celle de la suivante)\n")
    print(f"  {'marge ≥':>9}{'mots gardés':>14}{'fausses marques':>18}{'taux':>9}")
    for bar in (0.0, 0.5, 0.7, 0.8, 0.9):
        kept = [one for one in clarity if one[0] >= bar]
        if not kept:
            break
        bad = sum(one[1] for one in kept)
        print(f"  {bar:>9.2f}{len(kept):>8}/{len(clarity):<5}{bad:>14}"
              f"{100 * bad / len(kept):>8.1f} %")


def graph(speakers):
    """The probe through the exported graphs, against the reading it was judged on.

    The device runs the exported graph, never PyTorch, and the export's 8-bit
    rounding moves the hidden layer the probe reads 3.9 where it moves the
    matrix 0.045 (`export.py`'s own check). Whether that moves the probe's
    *elections* is what decides which file the phone carries, and the matrix
    says nothing of it -- so the same recordings the ear passes read are put
    through both graphs, word by word: which syllable is elected, and how far
    the margin moves. The margin is the eligibility bar of the app, so its
    drift is not cosmetic.
    """
    import onnxruntime
    import soundfile as sf
    tools = probe()
    layer = int(tools[4])
    order = readings(VOICES, speakers)

    sessions = {}
    for name, precision in (("flottant", ""), ("8 bits", "-int8")):
        path = matrix.ONNX_WEIGHTS.parent / f"{matrix.CHOSEN}{precision}.onnx"
        if not path.is_file():
            raise SystemExit(f"{path} manque — python3 export.py")
        sessions[name] = onnxruntime.InferenceSession(
            str(path), providers=["CPUExecutionProvider"])

    def through(session):
        def read(wav):
            matrix.heard(wav)
            samples, _ = sf.read(wav, dtype="float64")
            got = session.run(None, {"input_values": matrix.prepared(samples)})
            return got[1].astype(np.float32)
        return read

    def margin(part):
        ranked = sorted(part)[::-1]
        return ranked[0] - ranked[1]

    tally = {name: {"mots": 0, "élues": 0, "écart": 0.0, "max": 0.0,
                    "barrés": 0, "bascules": [], "lesquels": []}
             for name in sessions}
    for tag, family, source, stem, text, wav in order:
        if not overlap.readable(wav):
            continue
        reference = scored(wav, text, tag, tools)
        by_word = {f"{one['word']}#{one['rank']}": one for one in reference}
        for name, session in sessions.items():
            read = scored(wav, text, tag, tools, source=through(session))
            found = {f"{one['word']}#{one['rank']}": one for one in read}
            slot = tally[name]
            for key, one in by_word.items():
                other = found.get(key)
                if other is None:
                    # The graph's layer ran short of the torch one: a word the
                    # reading cannot place is worse than a moved election and
                    # is counted apart, because parts() drops it silently.
                    slot["barrés"] += 1
                    continue
                slot["mots"] += 1
                if int(np.argmax(one["part"])) == int(np.argmax(other["part"])):
                    slot["élues"] += 1
                else:
                    slot["lesquels"].append(
                        (key, one["part"], other["part"]))
                drift = abs(margin(one["part"]) - margin(other["part"]))
                slot["écart"] += drift
                slot["max"] = max(slot["max"], drift)
                # The bar decides eligibility, so a drift that crosses it is a
                # word the screen treats differently -- the one thing a mean
                # cannot show, because the two sides cancel.
                if (margin(one["part"]) >= BAR) != (margin(other["part"]) >= BAR):
                    slot["bascules"].append((key, margin(one["part"]),
                                             margin(other["part"])))
        print(f"  {tag} lu", file=sys.stderr, flush=True)

    print("\n=== la sonde à travers les graphes exportés, contre la lecture PyTorch\n")
    print(f"  {'':<10}{'mots':>7}{'élue pareille':>15}{'écart de marge':>16}"
          f"{'au pire':>9}{'bascules de barre':>19}{'mots barrés':>13}")
    for name, slot in tally.items():
        rate = (f"{100 * slot['élues'] / slot['mots']:.1f} %"
                if slot["mots"] else "—")
        print(f"  {name:<10}{slot['mots']:>7}{rate:>15}"
              f"{slot['écart'] / slot['mots']:>16.3f}{slot['max']:>9.3f}"
              f"{len(slot['bascules']):>19}{slot['barrés']:>13}")
        for key, here, there in slot["bascules"]:
            print(f"      barre franchie : {key.split('#')[0]:<16}"
                  f"marge {here:.3f} → {there:.3f}")
    print("\n  « écart de marge » = moyenne de |marge du graphe − marge PyTorch| ; "
          "la barre d'éligibilité\n  de l'app est à 0,90, donc un écart qui la "
          "franchit change ce qui est marqué.")
    for name, slot in tally.items():
        if slot["lesquels"]:
            print(f"\n  {name} — mots où l'élue change "
                  f"({len(slot['lesquels'])}):")
            for key, torch_part, graph_part in slot["lesquels"]:
                word, _ = key.split("#")
                print(f"      {word:<18}PyTorch {np.round(torch_part, 2).tolist()}"
                      f"   graphe {np.round(graph_part, 2).tolist()}")
    return 0


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--render", action="store_true",
                        help="synthétiser les voix modèles, avec confirmation")
    parser.add_argument("--report", action="store_true",
                        help="le décompte, sans écouter")
    parser.add_argument("--couples", action="store_true",
                        help="le désaccord sur le montage du produit, sans "
                             "écouter : l'apprenant aligné sur le modèle")
    parser.add_argument("--graph", action="store_true",
                        help="la sonde lue à travers les graphes exportés, "
                             "flottant et 8 bits, contre PyTorch")
    parser.add_argument("--again", action="store_true",
                        help="réécouter les désaccords, mêlés à autant "
                             "d'accords tirés au sort — à l'aveugle")
    parser.add_argument("-v", "--voice", action="append", default=None,
                        help="une voix de synthèse (répétable)")
    parser.add_argument("-n", "--speakers", type=int, default=SPEAKERS,
                        help=f"locuteurs TIMIT par phrase (défaut {SPEAKERS})")
    parser.add_argument("-p", "--pad", type=float, default=review.PAD,
                        help=f"l'air autour du mot, en secondes "
                             f"(défaut {review.PAD})")
    parser.add_argument("-s", "--slow", type=float, default=0.5,
                        help="la vitesse de la touche `s`, hauteur conservée")
    parser.add_argument("--force", action="store_true",
                        help="resynthétiser ce qui est déjà rendu")
    parser.add_argument("--seed", type=int, default=0,
                        help="l'ordre des lectures ; le mélange évite que la "
                             "fatigue tombe toujours sur la même source")
    args = parser.parse_args(argv)
    voices = tuple(args.voice) if args.voice else VOICES

    if args.report:
        return report() or 0
    if args.render:
        return rendered(voices, args.force)
    if not CORPUS.is_dir():
        raise SystemExit(f"{CORPUS} manque — TIMIT porte le côté natif du "
                         "croisement, et sans lui il ne reste que des machines")

    order = readings(voices, args.speakers)
    if args.graph:
        return graph(args.speakers)
    if args.couples:
        return couples(order, probe()) or 0
    if args.again:
        again(order, probe(), args.pad, args.slow, args.seed)
        return report() or 0
    # Shuffled, because judging every native then every voice would let the ear
    # settle into one kind of mouth and hear the next as a change of task.
    random.Random(args.seed).shuffle(order)
    listen(order, probe(), args.pad, args.slow)
    report()
    return 0



if __name__ == "__main__":
    sys.exit(main())
