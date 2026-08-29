#!/usr/bin/env python3
"""The labelled learner corpus: SpeechOcean762, its words and their verdicts.

Material, not measurement -- what `phrases.py` is to the rest of the bench,
this is to `alarms.py`. Nothing here decides anything.

Five thousand sentences read by non-native speakers of English, all with
Mandarin as their first language and half of them children, scored by five
experts at the phoneme, the word and the sentence. Apache-2.0, so it can be
named and its numbers published. It lives outside git, like TIMIT:

    mkdir -p tmp/speechocean762 && cd tmp/speechocean762 && \\
    for f in train-00000-of-00001 test-00000-of-00001; do curl -L -C - -o "$f.parquet" \\
      "https://huggingface.co/datasets/mispeech/speechocean762/resolve/main/data/$f.parquet"; done

Three things about it decide how it is read here, and each is measured rather
than assumed:

- **The texts do not repeat** -- 4947 distinct ones for 5000 takes. A model has
  to be synthesised per take, so the character budget is the whole cost of the
  chantier and every selection is made under one.
- **The takes are filed by speaker.** Slicing the first N takes gives one
  speaker; a subset is always drawn, never cut.
- **The two halves share no speaker** (125 each, 0 in common). `test` is read
  here, which leaves `train` untouched should it ever be trained on.

A word is `propre` when nothing was written against it -- no mispronunciation,
every phone at 2.0. It is `fautif` when the annotation names a phone that was
said as another one. In between sit the words carrying a phone scored 1, "right
but heavily accented", which is neither a fault to find nor a control to stay
off: the grey zone, kept apart rather than folded into either side.
"""

import io
import random
import sys
from collections import namedtuple
from pathlib import Path

import soundfile as sf

HERE = Path(__file__).resolve().parent
CORPUS = HERE.parent / "tmp" / "speechocean762"
AUDIO = HERE / "out" / "l2"

SAMPLE_RATE = 16000

Word = namedtuple("Word", "text state stress")
Take = namedtuple("Take", "uid text speaker words")

LABELS = ("propre", "entre", "fautif")


def parquet(split):
    path = CORPUS / f"{split}-00000-of-00001.parquet"
    if not path.is_file():
        raise SystemExit(f"{path} manque — le corpus se télécharge, "
                         "voir l'entête de ce fichier")
    return path


def state(word):
    if word["mispronunciations"]:
        return "fautif"
    scores = word["phones-accuracy"]
    if scores and min(scores) >= 2.0:
        return "propre"
    return "entre"


def spoken(text):
    """The text as it goes to the synthesiser, and to the join with it.

    The corpus writes its prompts in capitals and without punctuation. Capitals
    make a synthesiser spell letters out, and a sentence with no final stop gets
    a hanging contour, so both are put back -- the corpus carries no casing to
    lose and the model has to be an ordinary reading of an ordinary sentence.
    """
    lowered = text.strip().lower().rstrip(".!? ")
    return lowered[:1].upper() + lowered[1:] + "."


def catalogue(split):
    """Every take of the split, in file order, without touching the audio."""
    import pyarrow.parquet as pq

    table = pq.read_table(parquet(split),
                          columns=["text", "words", "speaker"]).to_pylist()
    takes = []
    for rank, row in enumerate(table):
        words = tuple(Word(word["text"], state(word), word["stress"])
                      for word in row["words"])
        takes.append(Take(f"{split}-{rank:04d}", spoken(row["text"]),
                          row["speaker"], words))
    return takes


def chosen(budget, split="test", seed=0):
    """A draw of takes fitting the character budget, the same one every time.

    Drawn and not cut, because the file is filed by speaker; and taken in the
    drawn order until one no longer fits rather than stopping at the first
    miss, so the budget is spent rather than left on the table.

    Draws nest: the same seed with a larger budget keeps every take the smaller
    one held and adds to them, so a run extends instead of starting over and
    the caches of the first stage all still count. A budget of 0 is the whole
    split.
    """
    every = catalogue(split)
    order = random.Random(seed).sample(range(len(every)), len(every))
    if not budget:
        return sorted(every, key=lambda take: take.uid), sum(
            len(take.text) for take in every)
    picked, spent = [], 0
    for rank in order:
        cost = len(every[rank].text)
        if spent + cost > budget:
            continue
        picked.append(every[rank])
        spent += cost
    return sorted(picked, key=lambda take: take.uid), spent


def wav(take):
    return AUDIO / take.uid.split("-")[0] / f"{take.uid}.wav"


def extract(takes, split):
    """Write the audio of the chosen takes, in one pass over the file.

    The corpus already holds 16 kHz mono wav, the bench's own format, so the
    bytes are written through untouched -- anything else would be a resampling
    nobody asked for. A file already there is left alone.
    """
    import pyarrow.parquet as pq

    wanted = {int(take.uid.rsplit("-", 1)[1]): take for take in takes
              if not wav(take).is_file()}
    if not wanted:
        return 0
    (AUDIO / split).mkdir(parents=True, exist_ok=True)
    written, rank = 0, 0
    for batch in pq.ParquetFile(parquet(split)).iter_batches(
            batch_size=64, columns=["audio"]):
        for row in batch.to_pylist():
            take = wanted.get(rank)
            rank += 1
            if take is None:
                continue
            data = row["audio"]["bytes"]
            info = sf.info(io.BytesIO(data))
            if info.samplerate != SAMPLE_RATE or info.channels != 1:
                raise SystemExit(
                    f"{take.uid} : {info.samplerate} Hz {info.channels} canaux, "
                    f"le banc lit du {SAMPLE_RATE} Hz mono")
            wav(take).write_bytes(data)
            written += 1
    return written


def census(takes):
    """How many words of each verdict the draw carries."""
    counts = dict.fromkeys(LABELS, 0)
    wrong_stress = 0
    for take in takes:
        for word in take.words:
            counts[word.state] += 1
            wrong_stress += word.stress == 5
    return counts, wrong_stress


def main(argv=None):
    import argparse

    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("-b", "--budget", type=int, default=2500,
                        help="caractères de synthèse par voix")
    parser.add_argument("-s", "--split", default="test")
    parser.add_argument("--seed", type=int, default=0)
    args = parser.parse_args(argv)

    takes, spent = chosen(args.budget, args.split, args.seed)
    counts, wrong_stress = census(takes)
    speakers = {take.speaker for take in takes}
    print(f"\n=== {len(takes)} prises, {spent} caractères, "
          f"{len(speakers)} locuteurs")
    print(f"    {sum(counts.values())} mots : "
          + ", ".join(f"{counts[label]} {label}" for label in LABELS))
    print(f"    {wrong_stress} mots à l'accent noté faux")
    return 0


if __name__ == "__main__":
    sys.exit(main())
