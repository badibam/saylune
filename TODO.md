# TODO

## Chantier 1 — spike Azure (bloquant)

- **Valider que les phonèmes IPA remontent hors `en-US`.** Clé Azure Speech, trois phrases enregistrées, comparaison du JSON retourné en `en-US` puis `en-GB`. Ce que la doc promet (niveau phonème sur toutes les locales supportées) est contredit par des rapports terrain de phonèmes manquants en `en-GB`, `fr-FR`, `de-DE`, `es-ES`, question restée sans réponse publique côté Microsoft. Tant que ce n'est pas mesuré, la promesse « choix de l'accent » n'est pas tenable.
- Issue si les phonèmes ne tombent qu'en `en-US` : soit v1 américaine seulement, soit second moteur pour les autres accents.
- Confirmé par ailleurs et non à revérifier : l'évaluation est accessible en **REST** (pas de SDK propriétaire à embarquer, donc F-Droid reste jouable) ; la réponse porte `NBestPhonemes`, qui donne le phonème **réellement produit** face à l'attendu, pas seulement un score.

## Chantier 2 — trancher le montage de la conversation

- **API voice-to-voice temps réel** (latence native, transcription de l'entrée fournie) *ou* **chaîne STT → LLM → TTS** (maillons substituables, latence cumulée). Arbitrage latence contre contrôle ; ne se tranche pas sans mesure.
- Contrainte commune aux deux : l'audio de chaque tour est **conservé localement**, sinon l'analyse à la demande n'a rien à analyser.
- Contrainte de fidélité : le TTS sert de **modèle à imiter**, donc qualité et surtout **stabilité** (le même mot sonne pareil d'une fois sur l'autre).

## Chantier 3 — incarnation

- Porter le mécanisme de persona de `parcours` : persona = paramètre, catalogue de personas nommés, séparation cadre / voix. Le principe est provider-agnostique, l'injection ne l'est pas (`--system-prompt` du CLI chez `parcours` ; champ `system` du payload ou instructions de session ici).
- Une fois implémenté ici, il y a deux implémentations d'un même principe : matière à moisson vers une facette de sagesse commune.

## Reste

- **Icône de l'app** — aucune pour l'instant, l'app porte l'icône par défaut d'Android. `fdroid` exige par ailleurs un `icon.png` et un `featureGraphic.png` dans la fiche.
- **Premier `./run build`** — le squelette Gradle est repris de `posebell` ; ses versions (AGP 9.3.1, `compileSdk` 37, BOM Compose) n'ont pas été vérifiées pour ce projet.
