# TODO

## Chantier 1 — le moteur d'analyse : tranché

**SpeechAce, pour la v1.** Le jeu d'essai complet est passé sur les deux candidats, à la même méthode, et l'écart n'est pas discutable :

| | phonème, sans fausse alerte | accent | mélodie |
|---|---|---|---|
| SpeechAce | 6 / 8, témoins à ±1 | vu et localisé | 17 demi-tons d'écart |
| Azure | 3 / 8, témoins jusqu'à −23 | score de phrase | **inversé** |

Azure est **écarté**, pas mis en réserve. Sa fiche reste comme base de comparaison (`docs/design/azure-speech.md`).

Ce qui reste ouvert de ce chantier, et qui n'attend pas :

- **La fragilité de l'accent.** Le témoin correct de la prise 19 déclenche une marque, alors que la mesure sur les échantillons montre l'appui au bon endroit. Le discriminant retenu — la syllabe qui *gagne* l'accent plutôt que celle qui le perd — ne tient que sur un cas et ne s'était pas allumé en synthèse. Deux ou trois prises de plus le confirment ou l'enterrent. **À faire avant de coder le marquage de l'accent**, sinon on implémente une règle inventée.
- **Reloger les sondes.** `tmp/bench/speechace.py`, `tmp/bench/tts.py` et `tmp/bench/audio_probe.py` portent des mécanismes de l'app — sonde de capacités, étalonnage d'une voix, contrôle indépendant d'une mesure. Le moteur étant choisi, la raison d'attendre a disparu.
- **Le plancher à 40 $/mois** est le coût assumé du choix. Il ne se résout pas dans ce chantier : c'est la piste embarquée qui le supprimerait, ou rien.

La piste **embarquée** (reconnaissance phonétique sur l'appareil, éventuellement libre) reste la seule alternative envisagée, et pas pour la v1. Elle retirerait d'un coup le coût par tour, le BYOK et l'anti-feature `NonFreeNet` — donc rien de ce qui s'écrit d'ici là ne doit lui fermer la porte. Argument supplémentaire en sa faveur : ne recevant aucun texte de référence, elle ne peut pas commettre la faute des services, qui est d'acquiescer au texte qu'on leur souffle.

## Chantier 1 bis — le marquage d'un seul mouvement

Les trois échelles du son (le son, le mot, la phrase) s'ancrent aux mêmes caractères du texte affiché, et doivent se marquer **en un seul geste visuel** sans que l'écran devienne un sapin de Noël. La forme reste entièrement à trouver.

Deux irrégularités contraignent le dessin : une lettre peut porter deux sons, une lettre peut n'en porter aucun.

## Chantier 2 — trancher le montage de la conversation

L'orientation est la **chaîne STT → LLM → TTS**, pour la modularité de chaque maillon. Reste à vérifier que la latence cumulée est acceptable — ça se mesure. Le chantier 1 étant tranché, plus rien ne retient cette mesure.

- Deux arguments déjà acquis contre l'API voix-à-voix : la reconstruction du texte de référence voyage gratuitement dans l'appel LLM de la chaîne, alors qu'elle exigerait un appel dédié par tour en voix-à-voix ; et la fin de tour étant manuelle, la latence native du temps réel perd une partie de son intérêt.
- Contrainte commune : l'audio de chaque tour est **conservé localement**, sinon l'analyse n'a rien à examiner.
- Contrainte de fidélité : le TTS sert de **modèle à imiter** et d'**étalon de mesure**, donc qualité, **stabilité** (le même mot sonne pareil d'une fois sur l'autre) et réussite à l'étalonnage.
- Le TTS doit exposer **le choix de la voix** et accepter un rendu en haute qualité — le débit d'échantillonnage étant indifférent au moteur d'analyse, un seul rendu sert à l'écoute et à la mesure.

## Chantier 3 — incarnation

- Porter le mécanisme de persona de `parcours` : persona = paramètre, catalogue de personas nommés, séparation cadre / voix. Le principe est provider-agnostique, l'injection ne l'est pas (`--system-prompt` du CLI chez `parcours` ; champ `system` du payload ou instructions de session ici).
- Une fois implémenté ici, il y a deux implémentations d'un même principe : matière à moisson vers une facette de sagesse commune.

## Reste

- **Les sondes du bench ne sont plus jetables.** `tmp/bench/speechace.py` et `tmp/bench/tts.py` portent deux mécanismes de l'app, pas du bench : la sonde de capacités (un appel par fonction optionnelle, lecture du refus) et l'étalonnage d'une voix de synthèse. Ils vivent dans `tmp/`, gitignoré. À reloger quand le moteur sera tranché — pas avant, ce serait figer un choix.
- **Icône de l'app** — aucune pour l'instant, l'app porte l'icône par défaut d'Android. `fdroid` exige par ailleurs un `icon.png` et un `featureGraphic.png` dans la fiche.
- **Premier `./run build`** — le squelette Gradle est repris de `posebell` ; ses versions (AGP 9.3.1, `compileSdk` 37, BOM Compose) n'ont pas été vérifiées pour ce projet.
- **Drift sagesse** — `android`, `cli-interactif`, `fdroid` à 1 commit de retard. `/update pull` avant le premier vrai code Android.
