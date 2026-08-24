# Le banc

Ce qui qualifie l'analyse et les voix modèles, exécutable. La procédure et les critères vivent dans `docs/design/analysis-qualification.md` ; ici il n'y a que de quoi les dérouler.

Rien de ce dossier ne part dans l'app : c'est l'instrument, pas le produit.

## Ce qu'il faut

Les clés de synthèse, en variables d'environnement — absentes, les scripts échouent franchement plutôt que de faire semblant :

- `AZURE_SPEECH_KEY` + `AZURE_SPEECH_REGION` — voix candidates Azure.
- `ELEVENLABS_KEY` — voix candidates ElevenLabs.

L'analyse, elle, n'en demande aucune : elle tourne en local, du poids acoustique jusqu'au verdict.

## Les briques

| Fichier | Rôle |
|---|---|
| `phrases.py` | les phrases du jeu, dans leur forme correcte — le modèle est toujours ce qui aurait dû être dit |
| `synth.py` | rendu d'une phrase par n'importe quelle voix candidate, toujours en wav 16 kHz mono |
| `take.py` | une session d'enregistrement guidée : naturel, calque GB, calque US |
| `pull.py` | tire les poids acoustiques et dit ce qu'ils valent — la seule brique qui touche le réseau |
| `matrix.py` | un audio → sa répartition sur les sons toutes les 20 ms, sa grille, l'alignement d'une seconde prise dessus |
| `overlap.py` | le recouvrement de deux répartitions, son par son — de combien deux voix s'écartent |
| `faults.py` | le jeu d'essai étiqueté : l'écart tombe-t-il sur le son fautif, et reste-t-il à zéro sur le témoin |
| `export.py` | le modèle en un fichier ONNX, forme sous laquelle il tourne sur le téléphone |
| `concord.py` | deux machines lisant les mêmes poids disent-elles la même chose |
| `phone.py` | pousse, mesure et rapatrie : l'appareil devient une lecture comme une autre |

Chaque brique s'utilise seule.

## Poser le modèle acoustique

Deux choses à poser d'abord, hors du dépôt puisque l'une pèse 1,2 Go :

```
python3 -m venv --system-site-packages tmp/venv && source tmp/venv/bin/activate
pip install --index-url https://download.pytorch.org/whl/cpu torch
pip install transformers soundfile onnx onnxruntime onnxscript
export HF_HOME="$PWD/tmp/hf"          # jamais le défaut : /home est un tmpfs ici
cd bench && python3 pull.py           # tire les poids et dit ce qu'ils valent
```

`HF_HOME` est lu par `matrix.py`, qui échoue franchement s'il est absent. Le tokenizer d'un modèle n'est jamais chargé — il ne sert qu'à transformer du texte en phonèmes, seul sens que ce pipeline refuse de prendre, et l'appeler réclamerait `phonemizer` et `espeak-ng` pour rien.

## Les lectures

Une **lecture** est une manière de calculer la matrice, et chacune a son propre cache — deux lectures qu'on veut comparer ne doivent jamais se recouvrir. Quatre variables d'environnement la nomment :

- `ACOUSTIC_MODEL` — le modèle, par son nom court dans la table de `matrix.py` (`timit-ipa` retenu, `espeak`, `charsiu`, `gruut`, `timit`). Les cinq ont été mesurés ; ce qui les départage est dans `../docs/design/embedded-analysis.md`.
- `QUANTISED=1` — les mêmes poids arrondis en entiers 8 bits, c'est-à-dire ce qui tourne sur un téléphone.
- `RUNTIME=onnx` — la lecture par ONNX Runtime au lieu de PyTorch, c'est-à-dire la machine qui tourne sur le téléphone. Elle ne charge pas PyTorch du tout et demande le fichier exporté.
- `READING=<nom>` — une lecture calculée **ailleurs**, déjà rangée dans le cache. C'est ainsi que l'appareil se relit : `READING=phone-int8 python3 faults.py` fait tourner le jeu d'essai sur ce que le téléphone a produit, sans que rien du banc ait à savoir d'où viennent les chiffres. Une lecture empruntée ne se calcule jamais ici — un cache manquant échoue franchement plutôt que de se remplir tout seul.

## Qualifier

```
python3 faults.py -v                    # l'écart tombe-t-il sur le son fautif
python3 overlap.py -s sentences -v      # deux voix se recouvrent-elles
```

Les vingt-sept prises étiquetées des blocs A à F vivent dans `out/takes/set/`. Elles ne se régénèrent pas : **si elles comptent, elles se sauvegardent hors du dépôt.**

## Porter sur le téléphone

```
python3 export.py                              # le graphe, en flottant puis en 8 bits
python3 concord.py                             # les deux machines disent-elles pareil
python3 phone.py                               # pousse, mesure, rapatrie
python3 concord.py -r timit-ipa-onnx-int8 -a phone-int8
READING=phone-int8 python3 faults.py           # le verdict, sur l'appareil
```

`export.py` met le softmax **dans** le graphe : le fichier rend la matrice elle-même, pas des logits, et le côté Android n'a plus à réimplémenter que la préparation du signal — moyenne nulle, variance unité, tenue à celle de l'extracteur par `export.py`.

`concord.py` confronte **deux machines lisant les mêmes poids**, jamais deux arrondis : tenir le 8 bits d'ONNX contre celui de PyTorch mesurerait quel quantificateur a arrondi où, ce qu'aucun téléphone ne présentera jamais. Ce que l'arrondi coûte se lit là où il tombe, sur le verdict, donc par `faults.py`.

## Enregistrer

```
python3 take.py                     # les six phrases, trois prises chacune
python3 take.py --gb azure-gb-sonia # changer de voix modèle
```

L'ordre de `take.py` n'est pas cosmétique : chaque phrase se dit **à froid** avant qu'aucun modèle ne soit entendu. Un témoin d'accent est le calque d'un modèle entendu, jamais une prise qu'on a demandé de dire « correctement » — l'analyse compare des réalisations, pas des normes.

Les rendus vivent dans `out/`, gitignoré : c'est du cache, régénérable par `-f`. Le matériel synthétique se régénère par script, il ne vit pas en fichiers versionnés.

Les prises, elles, sont dans `out/takes/`, donc gitignorées aussi : c'est une voix, et publier une voix ne se défait pas. Mais elles ne se régénèrent pas non plus — **si elles comptent, elles se sauvegardent hors du dépôt.**
