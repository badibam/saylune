# Chatterbox TTS (Resemble AI) — Paramètres par modèle

Source : dépôt officiel [`resemble-ai/chatterbox`](https://github.com/resemble-ai/chatterbox) (README + code source `tts.py`, `tts_turbo.py`, `mtl_tts.py`).

---

## 1. Chatterbox classique — `ChatterboxTTS` (anglais, 500M)

```python
model.generate(
    text,
    audio_prompt_path=None,     # wav de référence pour clonage de voix (zero-shot)
    exaggeration=0.5,           # 0–2+, intensité émotionnelle / prosodique
    cfg_weight=0.5,             # 0–1, guidance ; influe sur le rythme
    temperature=0.8,            # aléa du sampling
    repetition_penalty=1.2,
    min_p=0.05,
    top_p=1.0,
)
```

- `exaggeration` ↑ → débit plus rapide / plus expressif. `cfg_weight` ↓ → débit plus lent, plus posé. Les deux se compensent.
- Réglage recommandé pour discours dramatique : `cfg_weight≈0.3`, `exaggeration≈0.7+`.
- Astuce officielle : si le clip de référence n'est pas dans la langue du texte, mettre `cfg_weight=0` pour éviter que l'accent du clip contamine la sortie.
- `exaggeration` est fixé via `prepare_conditionals()` (lié à l'audio de référence) mais recalculable à la volée dans `generate()`.

---

## 2. Chatterbox-Turbo — `ChatterboxTurboTTS` (anglais, 350M)

```python
model.generate(
    text,
    audio_prompt_path=None,
    temperature=0.8,
    top_p=0.95,
    top_k=1000,
    repetition_penalty=1.2,
    norm_loudness=True,         # normalise à -27 LUFS
)
```

- **Pas de `exaggeration` ni `cfg_weight`** : Turbo désactive `emotion_adv` et `use_perceiver_resampler` pour gagner en vitesse.
- Le contrôle expressif passe entièrement par les **tags textuels** (voir plus bas).
- Variante **Nano** : même classe, chargée avec `nano=True` ; mêmes paramètres de génération.

---

## 3. Chatterbox-Multilingual — `ChatterboxMultilingualTTS` (23+ langues)

```python
ChatterboxMultilingualTTS.from_pretrained(device=device, t3_model="v3")  # ou "v2" legacy

model.generate(
    text,
    language_id="fr",           # obligatoire, code ISO 639-1, insensible à la casse
    audio_prompt_path=None,
    exaggeration=0.5,
    cfg_weight=0.5,
    temperature=0.8,
)
```

- Langues : ar, da, de, el, en, es, fi, fr, he, hi, it, ja, ko, ms, nl, no, pl, pt, ru, sv, sw, tr, zh.
- ⚠️ Bug connu (issue GitHub #355) : `exaggeration` a un effet quasi nul sur les checkpoints multilingues v1/v2 (couche `emotion_adv_fc` sous-dimensionnée). Non confirmé réparé sur V3.
- **Single Language Pack** : finetunes dédiés (zh, es-mx, pt-br, es-es, pt-pt, hi), chargés comme modèles à part, mêmes paramètres de génération.

---

## Marquage au niveau mot / convention de balisage

Pas de SSML, pas de contrôle fin par mot (pause, pitch, vitesse). Seule convention supportée : des **tags paralinguistiques entre crochets insérés dans le texte**, natifs sur Turbo :

```
[clear throat]  [sigh]  [shush]  [cough]  [groan]  [sniff]  [gasp]  [chuckle]  [laugh]
```

Exemple :
```
"Hi there, Sarah here from MochaFone calling you back [chuckle], have you got one minute to chat?"
```

Par ailleurs, une fonction interne `punc_norm()` nettoie automatiquement la ponctuation avant synthèse (remplace `...`, tirets, guillemets typographiques, ajoute un point final si absent) — non paramétrable, mais explique certains ajustements silencieux du texte source.
