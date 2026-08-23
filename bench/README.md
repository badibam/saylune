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

Chaque brique s'utilise seule.

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
