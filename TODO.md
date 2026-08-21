# TODO

## Chantier 1 — choisir le moteur d'analyse (en cours)

Azure a été instruit et mesuré ; tout ce qu'on en sait vit dans `docs/design/azure-speech.md`, incertitudes comprises. Il fonctionne, mais son verdict de conformité est inutilisable et rien n'oblige à le garder. Le contrat que l'app exige de n'importe quel moteur est dans `docs/reference.md`, « Fournisseurs ».

1. **Instruire SpeechAce.** Une question d'abord, avant toute autre : rend-il un jugement sur le **son produit**, ou seulement une note de conformité au texte attendu ? Si c'est la seconde, le point 2 du contrat tombe et le reste ne vaut pas la lecture. Ensuite seulement : niveau syllabe, prosodie, alphabet, tarification à l'abonnement, et surtout **ce qu'il offre de plus** — c'est la raison de le regarder.
2. **Rejouer le jeu d'essai** contre le candidat, avec les mêmes prises (`docs/design/pronunciation-test-set.md`). Enregistrer le cas 5 avant : il n'existe toujours pas, et c'est lui qui dit si le contexte de conversation sert vraiment.
3. **Éprouver le contrat en le lisant contre un deuxième moteur.** Noter ce qui se déforme : c'est le seul moyen de savoir si c'est une abstraction ou un moulage d'Azure. Même chose pour la déclaration de capacités — elle ne vaut que si un fournisseur réel en active et en éteint.
4. **La ligne de base par session** reste une décision de conception jamais éprouvée, quel que soit le moteur retenu : combien de tours faut-il avant qu'un écart veuille dire quelque chose, et que fait-on d'un son vu deux fois dans une conversation.

La piste **embarquée** (reconnaissance phonétique sur l'appareil) est la troisième voie et n'est pas pour maintenant. Elle retirerait le coût par tour, le BYOK et l'anti-feature `NonFreeNet` d'un seul geste — donc rien de ce qui s'écrit d'ici là ne doit lui fermer la porte.

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
