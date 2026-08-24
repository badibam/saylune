# TODO

## Chantier 1 — l'analyse embarquée : acquise, et ce qu'il reste à écrire

**L'analyse tourne sur l'appareil, et c'est la colonne vertébrale de l'app.** Le montage est écrit brique par brique dans `docs/design/embedded-analysis.md`, la qualification dans `docs/design/analysis-qualification.md`. Ce qui est mesuré et tenu :

- deux voix de synthèse différentes se recouvrent à deux millièmes de médiane, donc le locuteur ne survit pas dans la répartition et la comparaison de formes tient ;
- le modèle acoustique est nommé — `vitouphy/wav2vec2-xls-r-300m-timit-phoneme`, Apache-2.0, préféré à quatre autres candidats mesurés au même jeu — et il trouve les fautes du jeu d'essai étiqueté sans marquer les témoins, sans dictionnaire ni appel ;
- la quantification en entiers 8 bits passe, sur l'arrondisseur qui tourne vraiment ;
- **le tout tourne sur un Galaxy S10+ de 2019** : poids chargés en 0,8 s, empreinte à 930 Mo, une passe à quatre dixièmes de la durée du tour, deux lectures du même fichier rendant les mêmes octets ;
- l'appareil ne rend pas les mêmes octets que le poste — les noyaux 8 bits diffèrent entre ARM et x86, 0,74 % des trames changent de son gagnant par bascule entre quasi-ex æquo — mais **le verdict tient** : pire témoin 0,004 contre 0,003, mêmes fautes vues, même manquée.

Ce qui reste, dans l'ordre où ça mord :

- **Trois briques ne sont pas écrites**, et ce sont celles qui transforment des chiffres en marques : la **jointure lettres ↔ sons** (brique 4), l'**accent lexical** (7) et la **syllabification** (8). Sans la 4 il n'y a pas de marque, seulement des chiffres ; sans les 7 et 8, l'échelle du mot n'existe pas.
- **La jointure attache l'app à un seul fournisseur de synthèse.** L'ancrage aux lettres vient des horodatages au caractère du TTS, et ElevenLabs est aujourd'hui le seul à les rendre en REST nu — Azure ne les donne que par un SDK propriétaire, incompatible F-Droid. C'est l'attache la plus étroite du montage, et elle contredit le principe de ne pas dépendre d'une instance de service.
- **Le seuil de marquage n'est pas posé, et la matière est mince.** Les fautes franches se séparent superbement, la zone grise non : sur le modèle US la bande vide entre le pire témoin et la plus faible faute vue est de 0,002 à 0,004, et `09-walkin` (/ŋ/ à mi-chemin) est manqué par toutes les lectures. Ajouter que cette zone bouge de 0,1 entre poste et appareil rend le seuil dépendant du téléphone : il se calibre sur **la lecture de l'appareil**, pas seulement sur les poids qui tournent.
- **Aucune voix modèle n'est qualifiée.** Le principe tient — un modèle n'est un étalon que s'il s'étalonne, d'autant qu'il est cru aveuglément — mais le test qui existait jugeait une voix aux notes d'un service. Il est à redéfinir contre la matrice : grille nette, pas de son écrasé, pas de zone où la répartition s'effondre.
- **Le seuil de la brique 11**, à exprimer relativement à la prise plutôt qu'en constante.
- **La stabilité de la grille** : deux rendus du même texte par la même voix doivent donner la même suite de sons, sinon la mesure n'est pas reproductible. Le cache de synthèse neutralise en partie la question, jamais entièrement.
- **Le taux de fausse alerte de l'accent sur un tour spontané.** Réglé pour le régime d'imitation (bloc F : 2 fautes sur 2, aucune fausse alerte), il reste inconnu là où l'apprenant n'a pas entendu le modèle — et il ne se mesure pas, étiqueter une prise spontanée exigeant ce modèle. Deux garde-fous à tenir en tête au moment de coder : l'emphase de sens divergera toujours d'un modèle neutre, et la parenthèse doit rendre une fausse alerte peu coûteuse.
- **L'arithmétique au-dessus de la matrice n'est pas écrite en Kotlin**, donc pas mesurée sur l'appareil ; au poste elle est négligeable devant la passe réseau.
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

**Un appareil, une architecture.** Tout ce qui précède est mesuré sur arm64 avec instructions de produit scalaire, et sur un seul téléphone. Rien ne dit ce que fait un appareil à 4 Go, ni un jeu d'instructions plus pauvre.

## Chantier 2 — choisir les fournisseurs de conversation et de synthèse

Le montage est tranché : **chaîne STT → LLM → TTS**, reconnaissance par fichier, synthèse non pipelinée, 2,6 s jusqu'au premier son (cf. `docs/design/conversation-chain.md`). Reste à choisir qui tient chaque maillon — Azure Speech et DeepSeek ont servi à mesurer, pas à décider.

- **La synthèse est contrainte avant d'être choisie.** L'ancrage horodaté au caractère est structurel — c'est de lui que vient l'ancrage aux lettres — et ElevenLabs est le seul à le rendre en REST nu ; Azure ne le donne que par un SDK propriétaire, incompatible F-Droid. **Non vérifié** : l'API de synthèse par lot d'Azure rend-elle ces métadonnées en REST pur ? Si oui, l'arbitrage s'ouvre. Et **aucune voix n'est aujourd'hui qualifiée comme étalon** : le test qui existait notait une voix chez un service dont l'app ne dépend plus, il est à refaire contre la matrice (cf. chantier 1).
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
