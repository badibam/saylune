# TODO

## Chantier 1 — le moteur d'analyse : tranché

**SpeechAce, pour la v1.** Le jeu d'essai complet est passé sur les deux candidats, à la même méthode, et l'écart n'est pas discutable :

| | phonème, sans fausse alerte | accent | mélodie |
|---|---|---|---|
| SpeechAce | 6 / 8, témoins à ±1 | 2 / 2 par comparaison au modèle | 17 demi-tons d'écart |
| Azure | 3 / 8, témoins jusqu'à −23 | score de phrase | **inversé** |

Azure est **écarté**, pas mis en réserve. Sa fiche reste comme base de comparaison (`docs/design/azure-speech.md`).

Ce qui reste ouvert de ce chantier, et qui n'attend pas :

- **Le taux de fausse alerte de l'accent sur un tour spontané.** Réglé pour le régime d'imitation (bloc F : 2 fautes sur 2, aucune fausse alerte), il reste inconnu là où l'apprenant n'a pas entendu le modèle — et il ne se mesure pas, étiqueter une prise spontanée exigeant ce modèle. À défaut de chiffre, deux garde-fous à tenir en tête au moment de coder : l'emphase de sens divergera toujours d'un modèle neutre, et la parenthèse doit rendre une fausse alerte peu coûteuse.
- **Reloger les sondes en banc de qualification.** `tmp/bench/speechace.py`, `tmp/bench/tts.py` et `tmp/bench/audio_probe.py` deviennent `bench/`, versionné, derrière une commande unique — c'est le banc décrit par `docs/design/engine-qualification.md`, et le matériel synthétique s'y régénère par script au lieu de vivre en fichiers.
- **Le plancher à 40 $/mois** est le coût assumé du choix. Il ne se résout pas dans ce chantier : c'est la piste embarquée qui le supprimerait, ou rien.

La piste **locale/libre** reste la seule alternative envisagée, et pas pour la v1 — démarche écrite dans `docs/design/local-engine.md`, qualification par `docs/design/engine-qualification.md`. Elle retirerait le poste à abonnement plancher (l'analyse), pas le BYOK ni `NonFreeNet`, qui tiennent aux maillons restés distants. Rien de ce qui s'écrit d'ici là ne doit lui fermer la porte. La mélodie en est la brique détachable la plus mûre, envisageable dès la v1 en complément du service.

## Chantier 2 — choisir les fournisseurs de conversation et de synthèse

Le montage est tranché : **chaîne STT → LLM → TTS**, reconnaissance par fichier, synthèse non pipelinée, 2,6 s jusqu'au premier son (cf. `docs/design/conversation-chain.md`). Reste à choisir qui tient chaque maillon — Azure Speech et DeepSeek ont servi à mesurer, pas à décider.

- **La méthode de choix est un banc, pas un tableau de prix** — un banc par maillon, jamais le couple en boîte noire. Les énoncés des deux bancs sont écrits (`docs/design/grammar-test-set.md`) : banc du juge (LLM, 25 énoncés étiquetés, fausses alertes sur l'informel correct, frontière des crans, qualité d'`intended`) et banc de l'oreille (STT, fidélité verbatim sur la faute, disfluences coupées tolérées mais mots jamais réparés, ponctuation des questions — quatre prises existantes réutilisées, deux à enregistrer).
- Contrainte commune : l'audio de chaque tour est **conservé localement**, sinon l'analyse n'a rien à examiner.
- Contrainte de fidélité : le TTS sert de **modèle à imiter** et d'**étalon de mesure**, donc qualité, **stabilité** (le même mot sonne pareil d'une fois sur l'autre) et réussite à l'étalonnage.
- Le TTS doit exposer **le choix de la voix** et accepter un rendu en haute qualité — le débit d'échantillonnage étant indifférent au moteur d'analyse, un seul rendu sert à l'écoute et à la mesure.
- La reconnaissance par fichier coûte un sixième de la durée du tour. Assumé : le tour long est l'exception. À rouvrir seulement si l'usage montre que ça gêne.

## Chantier 3 — incarnation

- Porter le mécanisme de persona de `parcours` : persona = paramètre, catalogue de personas nommés, séparation cadre / voix. Le principe est provider-agnostique, l'injection ne l'est pas (`--system-prompt` du CLI chez `parcours` ; champ `system` du payload ou instructions de session ici).
- Une fois implémenté ici, il y a deux implémentations d'un même principe : matière à moisson vers une facette de sagesse commune.

## Reste

- **Icône de l'app** — aucune pour l'instant, l'app porte l'icône par défaut d'Android. `fdroid` exige par ailleurs un `icon.png` et un `featureGraphic.png` dans la fiche.
- **Drift sagesse** — `android`, `cli-interactif`, `fdroid` à 1 commit de retard. `/update pull` avant le premier vrai code Android.
