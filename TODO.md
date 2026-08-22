# TODO

## Chantier 1 — choisir le moteur d'analyse (en cours)

Azure et SpeechAce sont tous deux instruits et mesurés sur le même jeu de prises — `docs/design/azure-speech.md` et `docs/design/speechace.md`, incertitudes comprises. Le contrat qu'ils doivent honorer est dans `docs/reference.md`, « Fournisseurs » ; il a été déformé par la lecture du second, ce qui était son objet.

Ce qui reste avant de trancher :

1. **Diagnostiquer la qualité des prises.** Le suivi de hauteur est parfait sur l'audio synthétique et faux sur 19 % des valeurs de nos enregistrements. Tant que ce n'est pas expliqué, toute mesure prosodique mesure le micro. À faire **avant** le point 2.
2. **Enregistrer le bloc E** (`pronunciation-test-set.md`) et le passer aux deux moteurs. La prosodie répond proprement en synthèse ; rien ne dit qu'elle sépare une faute humaine.
3. **Enregistrer le cas 5**, toujours inexistant. C'est le seul qui dise si le contexte de conversation sert vraiment.
4. **Rejouer le jeu d'essai contre Azure avec la méthode de l'écart au modèle.** Azure a été jugé au seuil absolu, SpeechAce à l'écart au modèle synthétique : la comparaison entre les deux n'est donc pas encore à armes égales.
5. **Trancher**, en pesant ce qui ne se lit pas dans les mesures : Azure se facture à l'usage, SpeechAce impose un abonnement plancher de 40 $/mois quel que soit l'usage.

La piste **embarquée** (reconnaissance phonétique sur l'appareil) est la troisième voie et n'est pas pour maintenant. Elle retirerait le coût par tour, le BYOK et l'anti-feature `NonFreeNet` d'un seul geste — donc rien de ce qui s'écrit d'ici là ne doit lui fermer la porte. Un argument neuf en sa faveur : ne recevant aucun texte de référence, elle ne peut pas commettre la faute des deux autres, qui est d'acquiescer au texte qu'on leur souffle.

## Chantier 1 bis — le marquage d'un seul mouvement

Les trois échelles du son (le son, le mot, la phrase) s'ancrent aux mêmes caractères du texte affiché, et doivent se marquer **en un seul geste visuel** sans que l'écran devienne un sapin de Noël. La forme reste entièrement à trouver.

Deux irrégularités contraignent le dessin : une lettre peut porter deux sons, une lettre peut n'en porter aucun.

## Chantier 2 — trancher le montage de la conversation

- **API voice-to-voice temps réel** (latence native, transcription de l'entrée fournie) *ou* **chaîne STT → LLM → TTS** (maillons substituables, latence cumulée). Arbitrage latence contre contrôle ; ne se tranche pas sans mesure. Ne se mesure utilement qu'**après** le chantier 1 : une latence de conversation dont on ignore si la détection tient est une optimisation avant l'existence.
- Contrainte commune aux deux : l'audio de chaque tour est **conservé localement**, sinon la détection n'a rien à analyser.
- Contrainte de fidélité : le TTS sert de **modèle à imiter**, donc qualité et surtout **stabilité** (le même mot sonne pareil d'une fois sur l'autre).

## Chantier 3 — incarnation

- Porter le mécanisme de persona de `parcours` : persona = paramètre, catalogue de personas nommés, séparation cadre / voix. Le principe est provider-agnostique, l'injection ne l'est pas (`--system-prompt` du CLI chez `parcours` ; champ `system` du payload ou instructions de session ici).
- Une fois implémenté ici, il y a deux implémentations d'un même principe : matière à moisson vers une facette de sagesse commune.

## Reste

- **Les sondes du bench ne sont plus jetables.** `tmp/bench/speechace.py` et `tmp/bench/tts.py` portent deux mécanismes de l'app, pas du bench : la sonde de capacités (un appel par fonction optionnelle, lecture du refus) et l'étalonnage d'une voix de synthèse. Ils vivent dans `tmp/`, gitignoré. À reloger quand le moteur sera tranché — pas avant, ce serait figer un choix.
- **Icône de l'app** — aucune pour l'instant, l'app porte l'icône par défaut d'Android. `fdroid` exige par ailleurs un `icon.png` et un `featureGraphic.png` dans la fiche.
- **Premier `./run build`** — le squelette Gradle est repris de `posebell` ; ses versions (AGP 9.3.1, `compileSdk` 37, BOM Compose) n'ont pas été vérifiées pour ce projet.
- **Drift sagesse** — `android`, `cli-interactif`, `fdroid` à 1 commit de retard. `/update pull` avant le premier vrai code Android.
