# Les briques d'analyse en local

Démarche pour obtenir les trois échelles d'analyse — mélodie, accent, phonème — en local et/ou libre, sans service à abonnement. Ce n'est pas un engagement de la v1 : c'est la carte de la seule alternative ouverte, écrite pour qu'un contributeur (ou une session future) puisse s'en saisir. Toute brique produite ici se juge au protocole de `engine-qualification.md`, jamais sur sa promesse.

## Pourquoi c'est plus faisable que ça n'en a l'air

La barre n'est pas « égaler SpeechAce en absolu ». La mesure retenue par l'app est l'**écart entre l'humain et le modèle passés dans le même moteur** : un biais systématique du moteur s'annule des deux côtés. Une brique locale médiocre en calibrage absolu — ce qui est l'état de tout le libre en notation de prononciation — reste utilisable en différentiel, pourvu qu'elle tienne trois choses : le **déterminisme**, la **localisation** des sons, et l'**ancrage au texte**. Ce sont des propriétés d'ingénierie, pas de performance.

Deux voies existent pour le phonème, aux défauts opposés :

- **Conditionnée par le texte** (alignement forcé + score type GOP) : précise et localisée, mais elle hérite du piège des services — elle acquiesce au texte qu'on lui fournit. Le garde-fou de la rafale (beaucoup de sons qui s'effondrent ensemble = mauvais texte) reste nécessaire.
- **Reconnaissance phonémique libre** (transcrire les sons sans texte, puis aligner sur l'attendu) : elle ne peut pas acquiescer, donc elle voit le cas du mauvais texte — mais son alignement est plus bruité.

La bonne cible n'est pas l'une ou l'autre : la première **note**, la seconde **corrobore** — c'est l'équivalent local de `sound_most_like`, en mieux, puisqu'elle est réellement indépendante du texte.

## Les briques, de la plus facile à la plus dure

**1. La mélodie — DSP pur, déjà à moitié prouvée.** Un tracker de f0 local (l'autocorrélation de `audio_probe.py`, ou pYIN) + la pente en demi-tons sur la fin d'énoncé, comparée au même calcul sur le modèle. C'est exactement la mesure retenue, et le contrôle indépendant a déjà montré que l'autocorrélation **bat le tracker de SpeechAce** sur nos prises : elle concorde partout où il est sain et ne commet pas ses accrochages harmoniques. Aucun modèle ML, aucun poids à distribuer, déterminisme trivial. Elle n'exige pas d'alignement fin : la région voisée finale suffit à la pente. C'est la brique candidate à exister **en premier**, éventuellement dès la v1 en complément du service.

**2. L'accent lexical — des traits sur un alignement.** Par noyau vocalique de syllabe : durée, énergie RMS, pic de f0, normalisés dans le mot ; le motif de l'humain se compare au motif du modèle, régime du bloc F. L'attendu et la syllabification viennent de **CMUdict**, qui porte les marques d'accent. La barre à battre est basse : le verdict absolu de SpeechAce contredit son propre lexique sur 41 % des mots. Dépendance : des bornes de syllabes, donc la brique 3 en amont — ou, en attendant, les bornes rendues par le service.

**3. Le phonème — le vrai chantier.** Pipeline :

- **G2P** : CMUdict d'abord, phonémiseur (espeak-ng) en repli pour les mots hors lexique → séquence de phonèmes attendue.
- **Modèle acoustique** : un wav2vec2 affiné en reconnaissance phonémique CTC, poids sous licence libre, exporté ONNX et quantifié (~100 Mo, quelques secondes d'inférence par tour sur un téléphone récent).
- **Alignement forcé CTC** sur la séquence attendue → `extent` par phonème + postérieurs.
- **Score type GOP** : postérieur du phonème attendu contre son meilleur concurrent.
- **Différentiel** : le même pipeline sur le rendu du modèle TTS, la marque naît de l'écart — inchangé par rapport au service.
- **Corroboration** : décodage libre (sans texte) du même audio ; une divergence franche entre le décodage libre et l'attendu est l'équivalent d'un `sound_most_like` divergent, et détecte le mauvais texte de référence.
- **Ancrage aux lettres** : alignement graphème-phonème (l'appariement CMUdict ↔ orthographe est le morceau ingrat ; les aligneurs standards type m2m/Phonetisaurus le font). C'est ce que SpeechAce donne gratuitement et qu'il faut reconstruire.
- **Filtres** : les mêmes qu'au protocole — durée absurde = alignement décroché, à écarter.

## Empaquetage Android

- **ONNX Runtime** (MIT) en inférence CPU — le CPU est déterministe, le GPU ne l'est pas toujours : le déterminisme étant un critère de qualification, l'inférence reste CPU.
- **Poids sous licence libre uniquement** — un poids non libre est un `NonFreeAssets`, et il tuerait l'intérêt même de la brique.
- **Téléchargement des poids en opt-in explicite** au premier usage, jamais au premier lancement ni en silence (norme `fdroid`) ; embarqué dans l'APK seulement si le quantifié reste raisonnable.
- La brique expose le **même adaptateur** que le service (audio + texte + dialecte → sons localisés, notés, ancrés) et se déclare par capacités — mélodie seule d'abord, le reste quand ça qualifie.

## Ce qui reste distant, et pourquoi ça suffit

La conversation reste un LLM distant — aucun modèle local ne tient la qualité conversationnelle sur téléphone — en BYOK multi-fournisseurs, au choix de l'utilisateur. Le modèle à imiter reste un TTS, distant par défaut ; un TTS local (Piper/VITS) est envisageable et se jugera comme n'importe quelle voix, à l'étalonnage. Le gain visé n'est donc pas « tout local » : c'est la disparition du **poste à abonnement plancher** — l'analyse — pendant que les postes restants se paient aux centimes. `NonFreeNet` reste déclarée tant qu'un maillon distant subsiste.
