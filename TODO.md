# TODO

## Chantier 1 — l'analyse : ce qu'il reste à écrire

L'analyse tourne sur l'appareil ; ce qu'elle fait et ce qui a été mesuré sont dans `docs/analysis.md`, la façon de le vérifier dans `docs/qualification.md`. Ce qui suit est ce qui manque.

- **Trois briques ne sont pas écrites**, et ce sont celles qui transforment des chiffres en marques : la **jointure lettres ↔ sons** (brique 4), l'**accent lexical** (7) et la **syllabification** (8). Sans la 4 il n'y a pas de marque, seulement des chiffres ; sans les 7 et 8, l'échelle du mot n'existe pas.
- **Pour la brique 7, lire le rythme de l'alignement existant** : la déformation locale de la correspondance temporelle modèle ↔ apprenant (l'un traîne sur une syllabe, avale la suivante), une fois la pente d'ensemble — le débit — retirée, est un signal de durée pour l'accent lexical. Les positions des sons des deux côtés sortent déjà de la matrice : c'est une lecture de plus, pas un calcul de plus.
- **La jointure attache l'app à un seul fournisseur de synthèse.** L'ancrage aux lettres vient des horodatages au caractère du TTS, et ElevenLabs est aujourd'hui le seul à les rendre en REST nu — Azure ne les donne que par un SDK propriétaire, incompatible F-Droid. C'est l'attache la plus étroite du montage, et elle contredit le principe de ne pas dépendre d'une instance de service. **La direction pour la dissoudre est tranchée** : l'ancrage se calcule sur l'appareil (cf. chantier 2, l'aligneur caractères).
- **Le seuil de marquage n'est pas posé, et la matière est mince.** Les fautes franches se séparent superbement, la zone grise non : sur le modèle US la bande vide entre le pire témoin et la plus faible faute vue est de 0,002 à 0,004, et `09-walkin` (/ŋ/ à mi-chemin) est manqué par toutes les lectures. Ajouter que cette zone bouge de 0,1 entre poste et appareil rend le seuil dépendant du téléphone : il se calibre sur **la lecture de l'appareil**, pas seulement sur les poids qui tournent.
- **Épaissir la matière du seuil avec un corpus L2 annoté** (L2-ARCTIC, SpeechOcean762) : des milliers de fautes de vrais apprenants étiquetées au phonème, y compris la zone grise que le jeu maison n'a pas. Le montage est celui de l'app — synthétiser le modèle pour les prompts du corpus, dérouler le pipeline, regarder si l'écart sépare les phones annotés fautifs des corrects. Ça calibre le seuil à grande échelle sans enregistrer une prise, et ça dit si `09-walkin` est un cas isolé ou une famille.
- **Aucune voix modèle n'est qualifiée.** Le principe tient — un modèle n'est un étalon que s'il s'étalonne, d'autant qu'il est cru aveuglément — mais le test qui existait jugeait une voix aux notes d'un service. Il est à redéfinir contre la matrice : grille nette, pas de son écrasé, pas de zone où la répartition s'effondre.
- **Le seuil de la brique 11**, à exprimer relativement à la prise plutôt qu'en constante.
- **La stabilité de la grille** : deux rendus du même texte par la même voix doivent donner la même suite de sons, sinon la mesure n'est pas reproductible. Le cache de synthèse neutralise en partie la question, jamais entièrement.
- **Le taux de fausse alerte de l'accent sur un tour spontané.** Réglé pour le régime d'imitation (bloc F : 2 fautes sur 2, aucune fausse alerte), il reste inconnu là où l'apprenant n'a pas entendu le modèle — et il ne se mesure pas, étiqueter une prise spontanée exigeant ce modèle. Deux garde-fous à tenir en tête au moment de coder : l'emphase de sens divergera toujours d'un modèle neutre, et la parenthèse doit rendre une fausse alerte peu coûteuse.
- **L'arithmétique au-dessus de la matrice n'est pas écrite en Kotlin**, donc pas mesurée sur l'appareil ; au poste elle est négligeable devant la passe du réseau.
- **Finir de reloger les sondes.** Restent dans `tmp/bench/`, non relogés : le contrôle de f0 par autocorrélation (`audio_probe.py`, que le protocole nomme), la latence (`latency.py`) et la reconstruction (`reconstruct.py`, qui sert au chantier 2).

**Le coût du tour long est mesuré, et c'est la mémoire qui monte : 4316 Mo sur une minute d'audio.** Le temps croît comme la longueur puissance 1,3 (×0,32 sur 3 s, ×0,86 sur 60 s), la mémoire comme son carré — c'est l'attention. Un tour d'une minute plante un appareil à 4 Go. Trois issues, non départagées par la mesure et à trancher :

- **Fenêtrer la passe** — une dizaine de secondes avec recouvrement rend le coût linéaire et borne l'empreinte à celle d'une fenêtre. Ce que la couture déplace se mesure au banc gratuitement, en comparant les mêmes prises entières et fenêtrées : l'attention étant globale, une trame près d'un bord ne voit pas le même contexte.
- **Borner franchement la durée analysable** et le dire, un tour trop long portant sa raison comme n'importe quel tour non analysé.
- **Tenir que le tour d'une minute n'est pas un cas à servir.**

Indépendamment de l'issue, **les plages vides se retirent de l'audio** — de bord comme intérieures — ce qui abrège la passe plus que proportionnellement. La coupe est sûre par son **seuil de durée** : une occlusive est du silence, mais elle dure 50 à 120 ms, donc ne retirer que les plages de l'ordre de la demi-seconde place la coupe hors du domaine des phonèmes. Le seuil de **niveau**, lui, reste à poser, et il demande des tours spontanés hésitants que le banc n'a pas.

**Le runtime natif chez F-Droid est le seul point non instruit.** `onnxruntime-android` est distribué en AAR pré-compilé, ce que F-Droid n'accepte pas : leur exigence est de bâtir depuis les sources. Rien n'est fermé — ONNX Runtime est en MIT et son build Android est documenté (`./build.sh --android --build_java` produit l'AAR) — mais aucun précédent d'application F-Droid qui le compile dans sa recette n'a été trouvé, donc la voie n'est pas balisée. Trois issues :

- **Compiler ORT dans la recette F-Droid.** Propre, mais gros build, et la reproductibilité impose d'épingler la version exacte du NDK — le point délicat avec du natif.
- **Le faire entrer comme bibliothèque partagée**, compilée une fois chez eux : le mécanisme existe, il se négocie.
- **Un build minimal d'ORT**, réduit aux seuls opérateurs de notre modèle. C'est l'issue la plus prometteuse pour notre cas précis, puisqu'on ne fait tourner qu'un seul modèle : la taille et le temps de compilation tombent tous les deux, et le fichier `.onnx` exporté par `bench/export.py` est ce dont ORT tire la liste des opérateurs à conserver. Ordre de grandeur à réduire : `libonnxruntime.so` pèse 17,5 Mo pour arm64 seul.

En attendant, la sonde est tenue au build `debug` et l'AAR pré-compilé n'entre dans aucune release.

**Mesurer sur un second appareil.** Tout est mesuré sur un seul téléphone, arm64 avec instructions de produit scalaire. Rien ne dit ce que fait un appareil à 4 Go, ni un jeu d'instructions plus pauvre — et le seuil de marquage dépendant de la lecture de l'appareil, c'est une question de conception autant que de compatibilité.

## Chantier 2 — choisir les fournisseurs de conversation et de synthèse

Le montage est tranché : **chaîne STT → LLM → TTS**, reconnaissance par fichier, synthèse non pipelinée, 2,6 s jusqu'au premier son (cf. `docs/conversation-chain.md`). Reste à choisir qui tient chaque maillon — Azure Speech et DeepSeek ont servi à mesurer, pas à décider.

- **Tranché : l'ancrage aux lettres vise l'appareil, pas le fournisseur.** Un second modèle CTC à sortie caractères, sur le même runtime ONNX, aligne le texte — connu exactement — sur l'audio de synthèse ; lettres et sons se joignent **par le temps**, sans table graphème-phonème. S'il se qualifie, il est l'**unique** chemin d'ancrage : l'alignement fournisseur n'est plus consommé — pas d'option « utiliser si fourni », deux sources d'ancrage seraient deux marquages à qualifier — et tout TTS devient candidat, y compris les libres. Tant qu'il n'est pas qualifié, la contrainte actuelle tient (ElevenLabs seul en REST nu ; la question de l'API par lot d'Azure ne vaut plus que comme repli si l'aligneur échoue).
- **Instruire l'aligneur caractères, premiers pas** : licence du modèle (wav2vec2-base-960h annoncé Apache-2.0, à vérifier sur la carte ; MMS_FA exclu, CC-BY-NC 4.0), export ONNX par la voie de `bench/export.py`, puis précision d'ancrage mesurée sur les synthèses du jeu d'essai **contre les timestamps ElevenLabs déjà en main** — leur dernier office avant de sortir du runtime. À trancher au banc : la quantification (~100 Mo au lieu de ~360) suffit-elle — exclue pour l'analyse des phonèmes, mais l'alignement ne lit que la **position des pics** dans un treillis contraint par le texte connu, pas la forme fine des répartitions. Coûts en gros : +100-360 Mo sur l'opt-in de poids, ~+400 Mo de RAM si co-chargé (sinon charge-passe-décharge), passe courte sur le seul audio du modèle, calculée une fois par entrée du cache de synthèse.
- **Aucune voix n'est aujourd'hui qualifiée comme étalon** : le test qui existait notait une voix chez un service dont l'app ne dépend plus, il est à refaire contre la matrice (cf. chantier 1).
- **La méthode de choix est un banc, pas un tableau de prix** — un banc par maillon, jamais le couple en boîte noire. Les énoncés des deux bancs sont écrits (`docs/design/grammar-test-set.md`) : banc du juge (LLM, 25 énoncés étiquetés, fausses alertes sur l'informel correct, frontière des crans, qualité d'`intended`) et banc de l'oreille (STT, fidélité verbatim sur la faute, disfluences coupées tolérées mais mots jamais réparés, ponctuation des questions — quatre prises existantes réutilisées, deux à enregistrer).
- Contrainte commune : l'audio de chaque tour est **conservé localement**, sinon l'analyse n'a rien à examiner.
- Contrainte de fidélité : le TTS sert de **modèle à imiter** et d'**étalon de mesure**, donc qualité, **stabilité** (le même mot sonne pareil d'une fois sur l'autre) et réussite à l'étalonnage.
- Le TTS doit exposer **le choix de la voix** et accepter un rendu en haute qualité — le débit d'échantillonnage étant indifférent à l'analyse, un seul rendu sert à l'écoute et à la mesure.
- La reconnaissance par fichier coûte un sixième de la durée du tour. Assumé : le tour long est l'exception. À rouvrir seulement si l'usage montre que ça gêne.

Ni le banc du juge ni celui de l'oreille n'ont commencé. Ce sont les deux tiers restants du chantier.

## Chantier 3 — incarnation

- Porter le mécanisme de persona de `parcours` : persona = paramètre, catalogue de personas nommés, séparation cadre / voix. Le principe est provider-agnostique, l'injection ne l'est pas (`--system-prompt` du CLI chez `parcours` ; champ `system` du payload ou instructions de session ici).
- Une fois implémenté ici, il y a deux implémentations d'un même principe : matière à moisson vers une facette de sagesse commune.

## Reste

- **Icône de l'app** — aucune pour l'instant, l'app porte l'icône par défaut d'Android. `fdroid` exige par ailleurs un `icon.png` et un `featureGraphic.png` dans la fiche.
