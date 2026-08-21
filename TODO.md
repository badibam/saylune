# TODO

## Chantier 1 — banc de détection (écrit, bloqué sur les clés)

Le banc est en place dans `tmp/bench/` (jetable, gitignoré) : `record.py`, `assess.py`, `reconstruct.py`, `detect.py`, le jeu d'essai adversarial dans `phrases.md` et ses contextes wizard-of-oz. Le contrat REST y est vérifié contre la doc du fournisseur, la logique de verdict testée hors ligne.

Reste à le **faire tourner**, ce qui demande `AZURE_SPEECH_KEY` + `AZURE_SPEECH_REGION` et `ANTHROPIC_API_KEY`. Créer la ressource Azure est le vrai coût, et c'est le même mur que rencontrera l'utilisateur au BYOK : le traverser une fois informe l'écran de configuration guidé.

Trois réponses attendues, et c'est tout ce qu'on garde du banc :
1. La reconstruction tient-elle sur les cas ambigus, et à quel taux de fausses marques.
2. Dans lequel des quatre états les phonèmes remontent hors `en-US` (`phrases.md` détaille : absent / symboles vides / symboles sans `NBestPhonemes` / complet). Seul le dernier tient la promesse du choix de l'accent. Issue sinon : v1 américaine seulement, ou second moteur pour les autres accents.
3. Le coût réel d'un tour détecté, puisque c'est l'utilisateur qui paie.

## Chantier 2 — trancher le montage de la conversation

- **API voice-to-voice temps réel** (latence native, transcription de l'entrée fournie) *ou* **chaîne STT → LLM → TTS** (maillons substituables, latence cumulée). Arbitrage latence contre contrôle ; ne se tranche pas sans mesure. Ne se mesure utilement qu'**après** le chantier 1 : une latence de conversation dont on ignore si la détection tient est une optimisation avant l'existence.
- Contrainte commune aux deux : l'audio de chaque tour est **conservé localement**, sinon la détection n'a rien à analyser.
- Contrainte de fidélité : le TTS sert de **modèle à imiter**, donc qualité et surtout **stabilité** (le même mot sonne pareil d'une fois sur l'autre).

## Chantier 3 — incarnation

- Porter le mécanisme de persona de `parcours` : persona = paramètre, catalogue de personas nommés, séparation cadre / voix. Le principe est provider-agnostique, l'injection ne l'est pas (`--system-prompt` du CLI chez `parcours` ; champ `system` du payload ou instructions de session ici).
- Une fois implémenté ici, il y a deux implémentations d'un même principe : matière à moisson vers une facette de sagesse commune.

## Reste

- **Icône de l'app** — aucune pour l'instant, l'app porte l'icône par défaut d'Android. `fdroid` exige par ailleurs un `icon.png` et un `featureGraphic.png` dans la fiche.
- **Premier `./run build`** — le squelette Gradle est repris de `posebell` ; ses versions (AGP 9.3.1, `compileSdk` 37, BOM Compose) n'ont pas été vérifiées pour ce projet.
- **Drift sagesse** — `android`, `cli-interactif`, `fdroid` à 1 commit de retard. `/update pull` avant le premier vrai code Android.
