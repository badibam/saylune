# Le banc

Ce qui qualifie l'analyse et les voix modèles, exécutable. La procédure et les critères vivent dans `../docs/qualification.md` ; ici il n'y a que de quoi les dérouler.

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
| `boundaries.py` | où le réseau place chaque son dans le temps, contre les bornes de TIMIT — position et durée, la seule mesure adossée à une vérité terrain |
| `recognition.py` | le décodage libre nomme-t-il les bons sons — PER contre la transcription de TIMIT, décomposé en substitutions / omissions / insertions |
| `syllables.py` | la grille sait-elle combien de syllabes a un mot — les sons qu'elle perd, et le nombre de noyaux par mot |
| `learners.py` | le corpus d'apprenants étiqueté (SpeechOcean762) : ses mots et leur verdict, et le tirage sous budget de caractères |
| `alarms.py` | à quelle fréquence une marque tombe sur un mot que rien n'accusait — la jointure se fait au mot, aucun seuil n'est posé |
| `stress.py` | ce que l'accent fait à une syllabe — durée, réduction, intensité, sur les bornes de TIMIT posées à la main, sans réseau |
| `accent.py` | la brique 7 confrontée au corpus qui note l'accent : fausse alerte et détection, dans les deux montages |
| `join.py` | quelles lettres chaque son couvre — les mots, l'ordre et l'orthographe, notés contre l'annotation (`-s`) |
| `expected.py` | l'annotation à la main : les lettres que chaque son devrait porter. N'entre jamais dans l'app |
| `affinity.json` | à quels sons une lettre participe, de 0 à 3 — avec `affinity-groups.json`, la seule donnée linguistique du montage |
| `affinity-groups.json` | la même question pour les groupes qui écrivent un son (`sh`, `ough`, `kn`) — c'est elle qui retient les muettes qui appartiennent au son |
| `export.py` | le modèle en un fichier ONNX, forme sous laquelle il tourne sur le téléphone |
| `concord.py` | deux machines lisant les mêmes poids disent-elles la même chose |
| `phone.py` | pousse, mesure et rapatrie : l'appareil devient une lecture comme une autre |
| `turn.py` | un tour analysé au format que l'écran de marquage consomme — le tuyau du banc vers l'app |
| `review.py` | écouter ce qui a été marqué, modèle puis prise, et écrire si c'est une faute |
| `divergence.py` | écouter là où deux modèles ne nomment pas le même son, et écrire lequel a raison |

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

- `ACOUSTIC_MODEL` — le modèle, par son nom court dans la table de `matrix.py` (`timit-ipa` retenu, `espeak`, `charsiu`, `gruut`, `timit`). Les cinq ont été mesurés ; ce qui les départage est dans `../docs/analysis.md`.
- `QUANTISED=1` — les mêmes poids arrondis en entiers 8 bits, c'est-à-dire ce qui tourne sur un téléphone.
- `RUNTIME=onnx` — la lecture par ONNX Runtime au lieu de PyTorch, c'est-à-dire la machine qui tourne sur le téléphone. Elle ne charge pas PyTorch du tout et demande le fichier exporté.
- `READING=<nom>` — une lecture calculée **ailleurs**, déjà rangée dans le cache. C'est ainsi que l'appareil se relit : `READING=phone-int8 python3 faults.py` fait tourner le jeu d'essai sur ce que le téléphone a produit, sans que rien du banc ait à savoir d'où viennent les chiffres. Une lecture empruntée ne se calcule jamais ici — un cache manquant échoue franchement plutôt que de se remplir tout seul.

## Qualifier

```
python3 faults.py -v                          # l'écart tombe-t-il sur le son fautif
python3 overlap.py -s sentences -v            # deux voix se recouvrent-elles
ACOUSTIC_MODEL=<nom> python3 join.py -s       # les sons portent-ils les bonnes lettres
ACOUSTIC_MODEL=<nom> python3 boundaries.py    # position et durée des sons, contre TIMIT
ACOUSTIC_MODEL=<nom> python3 recognition.py   # le décodage libre nomme-t-il les bons sons
ACOUSTIC_MODEL=<nom> python3 syllables.py      # les syllabes se comptent-elles sur les sons
```

## Regarder

```
python3 turn.py 01-sink -m think -o ../tmp/turn.json
```

`turn.py` rend **un tour analysé** au format que l'écran de marquage consomme : le texte, et pour chaque son de la grille l'écart au modèle et les caractères qu'il couvre. C'est le tuyau entre le banc et l'app, dont l'écran est sinon alimenté par des données en dur (`SampleTurns.kt`). L'arithmétique reste en Python et le dessin en Kotlin.

Deux canaux sortent vides et le resteront jusqu'aux briques 7 et 8 : l'accent et la mélodie passent tous deux par `syllables`, qu'aucun code ne calcule encore.

## Juger

```
source ../tmp/venv/bin/activate
export HF_HOME="$(cd .. && pwd)/tmp/hf" ACOUSTIC_MODEL=timit-ipa
python3 review.py 15-right-clean -m turn-right-long
```

Plusieurs témoins ne vont pas avec la phrase qu'on croit — `13-field-clean` est celui de `think-sheep`, `15-right-clean` celui de `turn-right-long`. La table des cas de `faults.py` fait foi.

```
python3 divergence.py -r timit-ipa -r v3-pw0.1-e29 -n     # combien de désaccords, et où
python3 divergence.py -r timit-ipa -r v3-pw0.1-e29        # les juger, sur les rendus
python3 divergence.py -r timit-ipa -r v3-pw0.1-e29 -t     # sur les prises
```

Le jugement est **à l'aveugle** : lequel des deux modèles est A se tire au sort à chaque cas, les noms ne sont jamais affichés, et le tirage part dans le verdict. Le mot arrive à mi-vitesse, hauteur conservée ; `5`, `3` et `1` le rejouent à 0,5, 0,33 ou vitesse pleine. Le tableau des désaccords ne sort que sous `-n`, où l'ordre des colonnes dirait qui est qui.

`divergence.py` ne calcule aucune des deux lectures : remplir l'une depuis l'autre comparerait un modèle avec lui-même. Un cache absent ou périmé est fatal, et la commande qui le remplit est nommée. La même commande relancée reprend où elle s'est arrêtée.

`review.py` parcourt les marques d'une prise, joue le mot comme le modèle le dit puis comme la prise le dit, et écrit le verdict dans `reviews/<prise>.json`, versionné. Ce que chaque instrument répond, et ce qu'il ne répond pas, est dans `../docs/qualification.md`.

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

Les rendus vivent dans `out/`, gitignoré. **`-f` les refait, mais ne rend pas le même fichier** : la synthèse n'est pas reproductible, et tout chiffre du banc bouge avec l'audio qu'il a lu. Un rendu se **sauvegarde** comme une prise, il ne se régénère pas.

Les prises, elles, sont dans `out/takes/` — les vingt-sept prises étiquetées des blocs A à F sous `out/takes/set/` — donc gitignorées aussi : c'est une voix, et publier une voix ne se défait pas. Mais elles ne se régénèrent pas non plus, donc elles sont **copiées dans `/mnt/data/BAK/speakup-takes/`** (miroir du dossier entier, vérifié par empreinte). La copie est une photo : une prise neuve n'y est pas tant qu'on ne la refait pas.
