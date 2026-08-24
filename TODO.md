# TODO

## Chantier 1 — le moteur d'analyse : en bifurcation

**La décision est rouverte, et elle penche.** SpeechAce tient la v1 par défaut, faute d'avoir été remplacé ; l'analyse embarquée a passé depuis tout ce qu'un poste peut mesurer, et fait mieux que lui sur le jeu d'essai. Un seul verrou reste avant de trancher : est-ce que ça tourne sur un téléphone. **Si le PoC embarqué répond oui, c'est lui le moteur de la v1**, et probablement le seul — pas une alternative gardée en réserve. Ce qui suit décrit l'état des deux branches, en commençant par la mesure qui a fondé le choix initial.

**SpeechAce, mesuré pour la v1.** Le jeu d'essai complet est passé sur les deux candidats, à la même méthode, et l'écart n'est pas discutable :

| | phonème, sans fausse alerte | accent | mélodie |
|---|---|---|---|
| SpeechAce | 6 / 8, témoins à ±1 | 2 / 2 par comparaison au modèle | 17 demi-tons d'écart |
| Azure | 3 / 8, témoins jusqu'à −23 | score de phrase | **inversé** |

Azure est **écarté**, pas mis en réserve. Sa fiche reste comme base de comparaison (`docs/design/azure-speech.md`).

Ce qui reste ouvert de ce chantier, et qui n'attend pas :

- **Le taux de fausse alerte de l'accent sur un tour spontané.** Réglé pour le régime d'imitation (bloc F : 2 fautes sur 2, aucune fausse alerte), il reste inconnu là où l'apprenant n'a pas entendu le modèle — et il ne se mesure pas, étiqueter une prise spontanée exigeant ce modèle. À défaut de chiffre, deux garde-fous à tenir en tête au moment de coder : l'emphase de sens divergera toujours d'un modèle neutre, et la parenthèse doit rendre une fausse alerte peu coûteuse.
- **Finir de reloger les sondes.** `bench/` porte l'étalonnage, la capture, l'écart au modèle, l'analyse locale, et depuis peu les vingt-sept prises étiquetées (`out/takes/set/`). Restent dans `tmp/bench/`, non relogés : le contrôle de f0 par autocorrélation (`audio_probe.py`, que le protocole nomme), la latence (`latency.py`) et la reconstruction (`reconstruct.py`, qui sert au chantier 2).
- **Le choix de SpeechAce se rouvre partiellement.** Rien ne le disqualifie en `en-us`. Mais ce que l'app lui prend s'est réduit — la mélodie se mesure mieux en local, le verdict d'accent est faux en US et absent en GB, `sound_most_like` est inutilisable — et il ne reste que la note au phonème et l'ancrage aux lettres, pour 40 $/mois de plancher. Deux pistes :
  - **L'analyse embarquée**, dont le montage est instruit de bout en bout (`docs/design/embedded-analysis.md`) et dont la mesure fondatrice est **passée** : deux voix de synthèse différentes se recouvrent à deux millièmes de médiane, donc le locuteur ne survit pas dans la répartition et la comparaison de formes tient. **La mesure qui décide est passée aussi**, et le moteur est nommé : `vitouphy/wav2vec2-xls-r-300m-timit-phoneme`, Apache-2.0, préféré à quatre autres candidats mesurés au même jeu. Sept fautes sur huit vues sur le son étiqueté, témoins à zéro — mieux que SpeechAce, sans dictionnaire ni appel. La quantification en entiers 8 bits est mesurée et passe, y compris sur l'arrondisseur qui tournera vraiment : les fautes franches ne bougent pas, seule la zone grise s'écrase de moitié — le seuil de marquage se calibrera donc sur les poids qui tournent. Reste, dans l'ordre : **le PoC sur l'appareil**, dont la part poste est faite (cf. ci-dessous) ; le seuil de la brique 11, à exprimer relativement à la prise ; la stabilité de la grille ; et le seuil de marquage, à tirer des prises.

  **Le PoC embarqué est en cours, et c'est lui qui tranche le chantier.** Il portait trois inconnues ; **la troisième est tombée au poste** : le réseau exporté en ONNX et relu par ONNX Runtime rend la lecture du banc à l'identique en flottant, jeu d'essai étiqueté compris, ligne pour ligne. Le changement de moteur d'exécution ne coûte rien.

  Ce qui a coûté, en revanche, c'est l'arrondi. L'outil d'ONNX quantifie plus large que celui de PyTorch et abîme les convolutions d'entrée : un témoin correct monte à 0,851, en plein territoire de faute. Ramené au périmètre de PyTorch, avec une échelle par canal, la lecture revient exactement — pour 39 Mo de fichier en plus, 359 au lieu de 320. Leçon à retenir ailleurs : **une mesure d'arrondi ne vaut que pour l'arrondisseur qui l'a faite.**

  Restent les deux inconnues qui exigent l'appareil : **la mémoire** pour charger 359 Mo de poids et **le temps** d'une passe sur un tour réel. Le contrôle décisif reste numérique — les mêmes fichiers du banc, la même matrice qu'au poste, par `bench/concord.py`.

  **Le runtime natif est le seul point non instruit, et il devient une question de v1.** `onnxruntime-android` est distribué en AAR pré-compilé, ce que F-Droid n'accepte pas : leur exigence est de bâtir depuis les sources. Rien n'est fermé — ONNX Runtime est en MIT et son build Android est documenté (`./build.sh --android --build_java` produit l'AAR) — mais aucun précédent d'application F-Droid qui le compile dans sa recette n'a été trouvé, donc la voie n'est pas balisée. Trois issues, à instruire avant de s'engager sur du code :

  - **Compiler ORT dans la recette F-Droid.** Propre, mais gros build, et la reproductibilité impose d'épingler la version exacte du NDK — le point délicat avec du natif.
  - **Le faire entrer comme bibliothèque partagée**, compilée une fois chez eux : le mécanisme existe, il se négocie.
  - **Un build minimal d'ORT**, réduit aux seuls opérateurs de notre modèle. C'est l'issue la plus prometteuse pour notre cas précis, puisqu'on ne fait tourner qu'un seul modèle : la taille et le temps de compilation tombent tous les deux.

  Pour le PoC lui-même, la question ne se pose pas : on prend l'AAR pré-compilé, on mesure, on décide ensuite.
  - **Élargir le champ des services distants**, jamais fait : deux mesurés, aucun troisième regardé. Pistes non vérifiées — Language Confidence, SpeechSuper, ELSA. Un candidat se branche par une fonction dans `bench/engine.py`.
- **L'accent GB est diminué et fragile.** Pas d'échelle du mot (`predicted_stress_level` absent), et une seule voix mesurée qui s'étalonne, chez un seul fournisseur. Issue non mesurée : reconstruire l'accent depuis la durée et la hauteur par syllabe, que `en-gb` rend. À noter que la fragilité est un verdict du lexique `en-gb` de SpeechAce et non une propriété des voix : elle disparaît si l'analyse embarquée aboutit, le dialecte n'y étant plus un référentiel à choisir mais une conséquence du modèle.

L'**analyse embarquée** n'est plus une alternative en réserve : c'est la branche qui mène, et le PoC dira si elle devient le moteur de la v1. Montage écrit dans `docs/design/embedded-analysis.md`, qualification par `docs/design/engine-qualification.md`. Elle retirerait le poste à abonnement plancher, pas le BYOK ni `NonFreeNet`, qui tiennent à la conversation et à la synthèse restées distantes — et elle ajouterait en échange une dépendance native à faire accepter par F-Droid. Rien de ce qui s'écrit d'ici là ne doit lui fermer la porte, ni présumer qu'elle est acquise.

`docs/reference.md` porte encore la formulation d'avant la bifurcation — SpeechAce retenu, embarqué « pas un engagement de la v1 ». À reprendre quand le PoC aura répondu, pas avant : c'est le doc des décisions, pas des paris.

## Chantier 2 — choisir les fournisseurs de conversation et de synthèse

Le montage est tranché : **chaîne STT → LLM → TTS**, reconnaissance par fichier, synthèse non pipelinée, 2,6 s jusqu'au premier son (cf. `docs/design/conversation-chain.md`). Reste à choisir qui tient chaque maillon — Azure Speech et DeepSeek ont servi à mesurer, pas à décider.

- **La synthèse est à moitié tranchée.** Mesuré : Jenny (`en-us`) et Sonia (`en-gb`), toutes deux chez Azure, s'étalonnent sans un trou et n'inversent jamais ; aucune voix ElevenLabs ne tient l'accent britannique, ses voix américaines si. Mais Azure ne rend l'ancrage horodaté que par un SDK propriétaire, incompatible F-Droid, là où ElevenLabs le donne en REST au caractère. **Non vérifié** : l'API de synthèse par lot d'Azure rend-elle ces métadonnées en REST pur ? Si oui, l'arbitrage disparaît.
- **La méthode de choix est un banc, pas un tableau de prix** — un banc par maillon, jamais le couple en boîte noire. Les énoncés des deux bancs sont écrits (`docs/design/grammar-test-set.md`) : banc du juge (LLM, 25 énoncés étiquetés, fausses alertes sur l'informel correct, frontière des crans, qualité d'`intended`) et banc de l'oreille (STT, fidélité verbatim sur la faute, disfluences coupées tolérées mais mots jamais réparés, ponctuation des questions — quatre prises existantes réutilisées, deux à enregistrer).
- Contrainte commune : l'audio de chaque tour est **conservé localement**, sinon l'analyse n'a rien à examiner.
- Contrainte de fidélité : le TTS sert de **modèle à imiter** et d'**étalon de mesure**, donc qualité, **stabilité** (le même mot sonne pareil d'une fois sur l'autre) et réussite à l'étalonnage.
- Le TTS doit exposer **le choix de la voix** et accepter un rendu en haute qualité — le débit d'échantillonnage étant indifférent au moteur d'analyse, un seul rendu sert à l'écoute et à la mesure.
- La reconnaissance par fichier coûte un sixième de la durée du tour. Assumé : le tour long est l'exception. À rouvrir seulement si l'usage montre que ça gêne.

Ni le banc du juge ni celui de l'oreille n'ont commencé. Ce sont les deux tiers restants du chantier.

## Chantier 3 — incarnation

- Porter le mécanisme de persona de `parcours` : persona = paramètre, catalogue de personas nommés, séparation cadre / voix. Le principe est provider-agnostique, l'injection ne l'est pas (`--system-prompt` du CLI chez `parcours` ; champ `system` du payload ou instructions de session ici).
- Une fois implémenté ici, il y a deux implémentations d'un même principe : matière à moisson vers une facette de sagesse commune.

## Reste

- **Icône de l'app** — aucune pour l'instant, l'app porte l'icône par défaut d'Android. `fdroid` exige par ailleurs un `icon.png` et un `featureGraphic.png` dans la fiche.
- **Drift sagesse** — `android`, `cli-interactif`, `fdroid` à 1 commit de retard. `/update pull` avant le premier vrai code Android.
