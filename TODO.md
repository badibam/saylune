# TODO

## Chantier 1 — banc de détection (répondu)

Les trois questions du banc ont leur réponse ; elles vivent dans `docs/reference.md`. Les quinze cas sont enregistrés (sauf le 5) et passés en `en-US` ; `tmp/bench/` est jetable et peut partir dès que plus rien ne s'y relit.

Ce qui reste ouvert, et qui ne se tranche plus sur un banc hors ligne :

1. **La ligne de base par phonème est une décision, pas encore une mesure.** Elle demande du volume de parole pour se remplir — donc elle se valide dans l'app, en usage, pas sur quinze prises. Question précise à garder en tête : au bout de combien de tours une ligne de base est-elle assez peuplée pour qu'un écart veuille dire quelque chose.
2. **La voyelle est peut-être hors de portée.** Huit points séparent le `/iː/` fauté de son témoin, contre 171 sur l'occlusive. Si l'écart ne se creuse pas avec une ligne de base, il faut l'admettre : le marquage ne couvre pas les contrastes vocaliques, et le dire vaut mieux que marquer au hasard.
3. **Cas 5** (`The sink is broken`, contexte `sink.txt`) jamais enregistré — le seul qui dise si le contexte sert vraiment, ou si la reconstruction corrige dès qu'un mot voisin existe. Court, et le banc tient encore debout pour le prendre.

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
