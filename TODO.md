# TODO

## Chantier 1 — banc de détection (prochaine session, bloquant)

Banc jetable **côté portable**, Python, dans `tmp/bench/` — zéro Android, zéro Kotlin. Android est le substrat cher (Gradle, appareil, `adb`, permissions micro) : valider des contrats REST à travers cette couche, c'est payer plein tarif pour une réponse qu'un script rend en une soirée. Ce code est à jeter ; ce qu'on garde, ce sont les réponses.

Portée volontairement étroite : **la détection seule** (cf. `docs/reference.md`, « Les deux tuyaux »). Je dis une phrase, le banc répond : faute de grammaire, phonème sous le seuil, les deux, ou rien. Rien d'autre — pas de drill, pas de conversation. Tout le reste en dépend, et c'est la seule couche qui tourne sans qu'on l'ait demandée : une fausse marque envoie travailler pour rien, une marque manquée laisse ignorant.

- `record.py` — capture un wav 16 kHz mono (le format exact du tuyau A, pour que ce qui marche ici marche là-bas).
- `assess.py` — wav → Azure STT, puis Pronunciation Assessment en mode scripté avec `NBestPhonemes`, locale en paramètre. Sortie : le JSON brut, puis un tableau phonème / score / produit.
- Reconstruction du texte visé par le LLM : **historique de conversation plausible collé à la main** dans le prompt (wizard-of-oz). Hors contexte, le LLM devine dans le vide. Sortie attendue : `{heard, intended, repaired, severity}` — le texte réparé est un champ de plus, pas un appel de plus.

**Le jeu d'essai est adversarial, pas propre.** Il vise là où faute phonétique et faute grammaticale se confondent, parce que c'est là que la reconstruction casse :
- « I sink you are right » — `think` mal prononcé, pas un mot faux.
- « He don't know » — faute de grammaire, prononciation parfaite.
- « I have 25 years » — les deux textes divergent.

**Question absorbée : les phonèmes IPA remontent-ils hors `en-US` ?** Mêmes phrases passées en `en-US` puis `en-GB`. Ce que la doc promet (niveau phonème sur toutes les locales supportées) est contredit par des rapports terrain de phonèmes manquants en `en-GB`, `fr-FR`, `de-DE`, `es-ES`, question restée sans réponse publique côté Microsoft. Tant que ce n'est pas mesuré, la promesse « choix de l'accent » n'est pas tenable. Issue si les phonèmes ne tombent qu'en `en-US` : soit v1 américaine seulement, soit second moteur pour les autres accents.

Confirmé par ailleurs et non à revérifier : l'évaluation est accessible en **REST** (pas de SDK propriétaire à embarquer, donc F-Droid reste jouable) ; la réponse porte `NBestPhonemes`, qui donne le phonème **réellement produit** face à l'attendu, pas seulement un score.

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
