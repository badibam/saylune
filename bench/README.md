# Le banc

Ce qui qualifie un moteur ou une voix, exécutable. La procédure et les critères vivent dans `docs/design/engine-qualification.md` ; ici il n'y a que de quoi les dérouler.

Rien de ce dossier ne part dans l'app : c'est l'instrument, pas le produit.

## Ce qu'il faut

Les clés, en variables d'environnement — absentes, les scripts échouent franchement plutôt que de faire semblant :

- `SPEECHACE_KEY` + `SPEECHACE_ENDPOINT` — le moteur d'analyse.
- `AZURE_SPEECH_KEY` + `AZURE_SPEECH_REGION` — voix candidates Azure.
- `ELEVENLABS_KEY` — voix candidates ElevenLabs.

## Les briques

| Fichier | Rôle |
|---|---|
| `phrases.py` | les phrases du jeu, dans leur forme correcte — le modèle est toujours ce qui aurait dû être dit |
| `synth.py` | rendu d'une phrase par n'importe quelle voix candidate, toujours en wav 16 kHz mono |
| `engine.py` | l'appel au moteur : audio + texte + dialecte → phonèmes localisés et notés |
| `calibrate.py` | étape zéro — cette voix est-elle un étalon valide ? |
| `take.py` | une session d'enregistrement guidée : naturel, calque GB, calque US |
| `compare.py` | lit une prise par son écart au modèle, dictionnaire par dictionnaire |
| `matrix.py` | la piste embarquée : un audio → sa répartition sur les sons toutes les 20 ms, sa grille, l'alignement d'une seconde prise dessus |
| `overlap.py` | le recouvrement de deux répartitions, son par son — de combien deux voix s'écartent |
| `faults.py` | le jeu d'essai étiqueté relu sans aucun service : l'écart tombe-t-il sur le son fautif |
| `export.py` | le modèle en un fichier ONNX, forme sous laquelle il tournera sur le téléphone |

Chaque brique s'utilise seule.

## La piste embarquée

`matrix.py` et `overlap.py` ne parlent à aucun service : ils font tourner un modèle acoustique en local. Deux choses à poser d'abord, hors du dépôt puisque l'une pèse 1,2 Go :

```
python3 -m venv --system-site-packages tmp/venv && source tmp/venv/bin/activate
pip install --index-url https://download.pytorch.org/whl/cpu torch
pip install transformers soundfile
export HF_HOME="$PWD/tmp/hf"          # jamais le défaut : /home est un tmpfs ici
cd bench && python3 pull.py           # tire les poids et dit ce qu'ils valent
```

`HF_HOME` est lu par `matrix.py`, qui échoue franchement s'il est absent. Le tokenizer d'un modèle n'est jamais chargé — il ne sert qu'à transformer du texte en phonèmes, seul sens que ce pipeline refuse de prendre, et l'appeler réclamerait `phonemizer` et `espeak-ng` pour rien.

Trois variables d'environnement pilotent la lecture, et chaque combinaison a son propre cache — deux lectures qu'on veut comparer ne doivent jamais se recouvrir :

- `ACOUSTIC_MODEL` — le candidat, par son nom court dans la table de `matrix.py` (`timit-ipa` par défaut retenu, `espeak`, `charsiu`, `gruut`, `timit`). Les cinq ont été mesurés ; ce qui les départage est dans `../docs/design/embedded-analysis.md`.
- `QUANTISED=1` — les mêmes poids arrondis en entiers 8 bits, c'est-à-dire ce qui tournera sur un téléphone.
- `RUNTIME=onnx` — la lecture par ONNX Runtime au lieu de PyTorch, c'est-à-dire la machine qui tournera sur le téléphone. Elle ne charge pas PyTorch du tout et demande le fichier exporté :

```
pip install onnx onnxruntime
python3 export.py                       # le graphe, en flottant puis en 8 bits
RUNTIME=onnx python3 faults.py -v       # le même jeu, lu par l'autre machine
```

`export.py` met le softmax **dans** le graphe : le fichier rend la matrice elle-même, pas des logits, et le côté Android n'a plus à réimplémenter que la préparation du signal — moyenne nulle, variance unité, tenue à celle de l'extracteur par `export.py`.

```
python3 overlap.py -s sentences -v      # deux voix se recouvrent-elles
python3 faults.py -v                    # l'écart tombe-t-il sur le son fautif
```

Les vingt-sept prises étiquetées des blocs A à F vivent dans `out/takes/set/`. Elles ne se régénèrent pas : **si elles comptent, elles se sauvegardent hors du dépôt.**

## Étalonner

```
python3 calibrate.py                       # tout le champ, chaque voix à son dialecte
python3 calibrate.py -c eleven-gb-daniel -v
python3 calibrate.py -c azure-us-jenny -d en-gb   # l'accord de dialecte doit ÉCHOUER
```

Le dialecte n'est **pas** déduit de la voix, et c'est délibéré : l'étape 3 de la qualification note exprès une voix US au référentiel GB pour vérifier que l'étalonnage attrape l'incohérence au lieu de la laisser inverser toute la mesure.

Les rendus vivent dans `out/`, gitignoré : c'est du cache, régénérable par `-f`. Le matériel synthétique se régénère par script, il ne vit pas en fichiers versionnés.

## Enregistrer

```
python3 take.py                     # les six phrases, trois prises chacune
python3 take.py --gb azure-gb-sonia # changer de voix modèle
python3 compare.py -v
```

L'ordre de `take.py` n'est pas cosmétique : chaque phrase se dit **à froid** avant qu'aucun modèle ne soit entendu. Un témoin d'accent est le calque d'un modèle entendu, jamais une prise qu'on a demandé de dire « correctement » — le moteur compare des réalisations, pas des normes.

Les prises sont dans `out/takes/`, donc gitignorées : c'est une voix, et publier une voix ne se défait pas. Mais elles ne se régénèrent pas non plus — **si elles comptent, elles se sauvegardent hors du dépôt.**
