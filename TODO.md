# TODO

## Chantier 1 — banc de détection (en cours)

Le banc est en place dans `tmp/bench/` (jetable, gitignoré) : `record.py`, `assess.py`, `reconstruct.py`, `detect.py`, le jeu d'essai adversarial dans `phrases.md` et ses contextes wizard-of-oz. Le contrat REST y est vérifié contre la doc du fournisseur, la logique de verdict testée hors ligne.

La ressource Azure est créée (`francecentral`, palier gratuit F0) et le banc tourne. La reconstruction passe par DeepSeek, le fournisseur étant un paramètre.

Réponses obtenues :
- **Locales : tranché.** Le phonème n'est nommé qu'en `en-US` ; `en-GB` note et classe sans nommer (cf. `docs/reference.md`, « Fournisseurs »).
- **Forme de la réponse REST : les scores sont à plat sur l'entrée**, pas nichés sous `PronunciationAssessment` comme le montre l'exemple de la doc. À porter tel quel dans le code Android.

- **Signal de marquage : la marge, pas le score absolu**, et à titre provisoire (cf. `docs/reference.md`, « Les trois curseurs »).

Reste à obtenir :
1. **La reconstruction sur les cas ambigus**, et le taux de fausses marques. Non mesurée : la reconnaissance a rendu `think` sur les deux prises, donc le piège du texte de référence ne s'est jamais déclenché. C'est déjà une information — le modèle de langue normalise vers le mot plausible — mais les cas 2, 3 et 5 de `phrases.md` (`He don't know`, `I have 25 years`, `The sink is broken`) attaquent d'autres angles et restent à enregistrer.
2. **Affiner la règle de marquage** sur ces cas : la marge tient sur un seul contraste et trois prises. Reste à trouver un seuil chiffré, et à voir si la règle survit aux voyelles et aux consonnes non fricatives, dont les concurrents ne se répartissent pas de la même façon.
3. **Le coût réel d'un tour détecté**, puisque c'est l'utilisateur qui paie.

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
