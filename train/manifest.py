"""Build the training manifest and prove the corpus decodes.

Walks both splits, decodes every audio file, folds every transcription,
and writes tmp/train/manifest.json (regenerable, gitignored). This is the
free local rehearsal the runbook demands before renting anything: any
surprise — SPHERE decode, unknown symbol, truncated file — surfaces here,
at CPU prices.
"""

import json
from collections import Counter
from pathlib import Path

import soundfile

from timit import CORPUS, ROOT, utterances, vocabulary

OUT = ROOT / "tmp" / "train" / "manifest.json"


def main():
    if not CORPUS.is_dir():
        raise SystemExit(f"corpus absent: {CORPUS}")
    vocab = vocabulary()
    manifest = {}
    phone_counts = Counter()
    for split in ("TRAIN", "TEST"):
        entries = []
        for uid, wav, phones in utterances(split):
            audio, rate = soundfile.read(wav)
            if rate != 16000:
                raise SystemExit(f"{uid}: unexpected rate {rate}")
            if audio.ndim != 1 or len(audio) < rate // 10:
                raise SystemExit(f"{uid}: suspicious audio shape {audio.shape}")
            phone_counts.update(phones)
            entries.append({
                "id": uid,
                "wav": str(wav.relative_to(ROOT)),
                "seconds": round(len(audio) / rate, 3),
                "phones": phones,
            })
        manifest[split.lower()] = entries
        hours = sum(e["seconds"] for e in entries) / 3600
        print(f"{split}: {len(entries)} utterances, {hours:.2f} h")
    unseen = set(vocab) - set(phone_counts) - {"|", " ", "[UNK]", "[PAD]"}
    if unseen:
        raise SystemExit(f"vocabulary symbols never produced by the corpus: {unseen}")
    print(f"{sum(phone_counts.values())} phone targets over {len(phone_counts)} classes,"
          f" rarest: {', '.join(f'{p} ×{n}' for p, n in phone_counts.most_common()[-3:])}")
    OUT.parent.mkdir(parents=True, exist_ok=True)
    tmp = OUT.with_suffix(".part")
    tmp.write_text(json.dumps(manifest, ensure_ascii=False), encoding="utf-8")
    tmp.replace(OUT)
    print(f"wrote {OUT.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
