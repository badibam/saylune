# TODO

## Chantier 1 — l'analyse : ce qu'il reste à écrire

L'analyse tourne sur l'appareil ; ce qu'elle fait et ce qui a été mesuré sont dans `docs/analysis.md`, la façon de le vérifier dans `docs/qualification.md`. Ce qui suit est ce qui manque.

- **Deux briques ne sont pas écrites** : l'**accent lexical** (7) et la **syllabification** (8). Sans elles, l'échelle du mot n'existe pas, et deux canaux du tour analysé sortent vides — l'accent et la mélodie passent tous deux par `syllables`, que rien ne calcule.
- **Pour la brique 7, lire le rythme de l'alignement existant** : la déformation locale de la correspondance temporelle modèle ↔ apprenant (l'un traîne sur une syllabe, avale la suivante), une fois la pente d'ensemble — le débit — retirée, est un signal de durée pour l'accent lexical. Les positions des sons des deux côtés sortent déjà de la matrice : c'est une lecture de plus, pas un calcul de plus.
- **L'arithmétique au-dessus de la matrice n'est pas écrite en Kotlin**, donc pas mesurée sur l'appareil ; au poste elle est négligeable devant la passe du réseau. La jointure (4) est dans le même cas.

### Le modèle de sons — le chantier d'affinage est en report

**On garde `timit-ipa`, et c'est un report, pas un verdict.** Le régime à oreille gelée est condamné par la mesure ; l'affinage complet sur `xls-r-300m` (marche 3) atteint la parité sur la séparation fautes/témoins — pire témoin à 0,001 contre 0,002 pour le sortant — mais **recule sur la jointure**, qui est ce que l'app consomme : 8 à 10 % de sons sans aucune lettre contre 7 %. Le poids de prior `0,1` est jugé sur ses trois époques et ne renverse rien ; le poids `0,3` n'a pas tourné, donc le terme de Huang et al. lui-même n'est pas jugé. Le plan et le diagnostic vivent dans `docs/design/dense-grid.md`, l'atelier dans `train/README.md`.

Trois raisons au report : la composition n'est pas mesurée (ci-dessous) ; l'avantage du candidat repose sur **une** faute, `09-walkin`, la seule de la zone grise, donc un `n = 1` ; et le sortant est celui qui tourne, donc le garder ne coûte rien quand en changer coûte une requalification. **À rouvrir quand le corpus L2 existe, pas avant et pas sur autre chose.**

- **Mesurer la composition des trois instruments — le critère qui déciderait, et qui n'existe pas.** Une marque n'arrive à l'écran que si le son existe dans la grille (`missing_mass.py`), que l'écart le sépare du témoin (`faults.py`) et qu'il porte les bonnes lettres (`join.py -s`). Les trois se mesurent sur du matériel qui partage les mêmes phrases — les cas de `faults.py` nomment le son exact et son occurrence — donc la composition est calculable. **Ni montée ni chiffrée.**
- **L'écart de PER n'est pas interprétable en l'état.** Nos checkpoints sont à 10 % quand `timit-ipa` est à 6,7 %, très bas pour TIMIT en 39 classes (état de l'art 13 à 16 %), ce qui fait soupçonner une **fuite de split** chez `vitouphy` — invérifiable, c'est un modèle tiers. Nos runs n'ont vu que `TRAIN`. À trancher seulement si l'écart doit peser sur une décision : réentraîner l'étalon nous-mêmes, ou mesurer les deux sur un corpus qu'aucun des deux n'a vu.
- **Un fait non expliqué à garder en tête** : sur TIMIT le décodage libre n'omet presque rien (0,7 % chez l'étalon, 1,3 à 1,5 % chez nous) alors que sur le jeu d'essai la grille perd un son sur huit. Parole lue et proche de l'entraînement d'un côté, synthèse ElevenLabs de l'autre — la différence n'est pas mesurée, et elle nuance « les poids ne voient pas ces sons » en « ils ne les voient pas *là* ».

### La jointure — ce qu'il lui reste, dans l'ordre

La brique 4 tourne au banc et se lit hors du jeu qui l'a réglée : **311 sons sur 326, soit 95 %** (`bench/join.py -j heldout -s`), contre 94 % en calibration.

- **Une lettre pour plusieurs sons — six des neuf désaccords restants, et les seuls échecs francs.** `x` de `six` et de `boxes`, `u` de `use` et de `music`. Chaque lettre choisit **un** son ; réparer l'appariement demande un changement de modèle.
- **Trois arbitrages de frontière, isolés.** Le /k/ unique entre `drink` et `coffee`, qui appartient au mot d'avant ou d'après sans que rien ne tranche ; le /ɝ/ de `chair` qui part sur le `i` de `is` ; le `s` de `question` donné au /ʃ/ alors qu'il écrit un /s/ que la grille n'a pas lu.
- **`walk` reste faux, et volontairement.** Le groupe `al` sur /ɑ/ répare `walk` et casse `called`, où le `l` sonne : la table est sans contexte et ne peut pas voir que c'est la lettre suivante qui décide. Refusé par le jeu tenu à l'écart — un son gagné d'un côté, zéro net de l'autre.
- **Régler le prix du son vide sur autre chose que son propre étalon.** L'un des deux jeux règle, l'autre juge, jamais les deux.
- **Lire une seconde voix.** `eleven-gb-daniel` change l'accent donc la grille. Coûte une seconde annotation à la main, indexée sur ses propres sons.
- **L'apostrophe de `doesn't` n'a jamais été traitée.**

### Le marquage et son seuil

- **Toutes les fautes se marquent, aucune ne s'élit.** Le tour entier peint des fautes que le jeu d'essai n'avait jamais notées — quatre des cinq relevées sur une prise dite témoin sont de la **réduction** que le modèle fait et que la prise ne fait pas (`have to` dévoisé, `to` et `at` non réduits, le `t` battu de `right` articulé), jugées cohérentes à l'écoute par leur auteur (`bench/review.py`). Rien ne hiérarchise ça sous une faute segmentale : le but est de dire pareil, et un anglais qui ne réduit jamais sonne étranger d'un bout à l'autre là où un `th` dit `z` se comprend aussitôt.
- **Le seuil tient mieux qu'annoncé** — les sons calmes valent 0,01 à 2,2 points et la plus faible marque en vaut 25, donc la bande de bruit à 5 tombe au milieu d'un vide franc. Ce qui reste ouvert est **la zone grise** : `09-walkin` (/ŋ/ à mi-chemin) est manqué par toutes les lectures, et cette zone bouge de 0,1 entre poste et appareil, donc elle se calibre sur la lecture de l'appareil et pas seulement sur les poids.
- **Épaissir la matière du seuil avec un corpus L2 annoté** (L2-ARCTIC, SpeechOcean762) : des milliers de fautes de vrais apprenants étiquetées au phonème, y compris la zone grise que le jeu maison n'a pas. Le montage est celui de l'app — synthétiser le modèle pour les prompts du corpus, dérouler le pipeline, regarder si l'écart sépare les phones annotés fautifs des corrects. Ça calibre à grande échelle sans enregistrer une prise, et ça dit si `09-walkin` est un cas isolé ou une famille.
- **Le taux de fausse alerte de l'accent sur un tour spontané.** Réglé pour le régime d'imitation (bloc F : 2 fautes sur 2, aucune fausse alerte), il reste inconnu là où l'apprenant n'a pas entendu le modèle — et il ne se mesure pas, étiqueter une prise spontanée exigeant ce modèle. Deux garde-fous à tenir au moment de coder : l'emphase de sens divergera toujours d'un modèle neutre, et la parenthèse doit rendre une fausse alerte peu coûteuse.
- **Le seuil de la brique 11**, à exprimer relativement à la prise plutôt qu'en constante.
- **Aucune voix modèle n'est qualifiée.** Le test qui existait jugeait une voix aux notes d'un service dont l'app ne dépend plus ; il est à redéfinir contre la matrice — grille nette, pas de son écrasé, pas de zone où la répartition s'effondre. Un quatrième critère est apparu, et c'est le seul extérieur à la voix : **de combien elle diverge des autres**. Sur les mêmes seize phrases, deux voix s'écartent de 11,5 % des sons pour `eleven-us-sarah`, 13 % pour `azure-us-jenny`, 18 % pour `eleven-gb-daniel` (`tmp/voices.py`). Si la voix modèle est la source de vérité, son idiosyncrasie devient la norme qu'on fait imiter — donc un étalon devrait être une voix ordinaire.
- **La stabilité de la grille** : deux rendus du même texte par la même voix doivent donner la même suite de sons, sinon la mesure n'est pas reproductible. Le cache de synthèse neutralise en partie la question, jamais entièrement.

### L'appareil

**Le coût du tour long est mesuré, et c'est la mémoire qui monte : 4316 Mo sur une minute d'audio.** Le temps croît comme la longueur puissance 1,3 (×0,32 sur 3 s, ×0,86 sur 60 s), la mémoire comme son carré — c'est l'attention. Un tour d'une minute plante un appareil à 4 Go. Trois issues, non départagées par la mesure et à trancher :

- **Fenêtrer la passe** — une dizaine de secondes avec recouvrement rend le coût linéaire et borne l'empreinte à celle d'une fenêtre. Ce que la couture déplace se mesure au banc gratuitement, en comparant les mêmes prises entières et fenêtrées : l'attention étant globale, une trame près d'un bord ne voit pas le même contexte.
- **Borner franchement la durée analysable** et le dire, un tour trop long portant sa raison comme n'importe quel tour non analysé.
- **Tenir que le tour d'une minute n'est pas un cas à servir.**

Indépendamment de l'issue, **les plages vides se retirent de l'audio** — de bord comme intérieures — ce qui abrège la passe plus que proportionnellement. La coupe est sûre par son **seuil de durée** : une occlusive est du silence, mais elle dure 50 à 120 ms, donc ne retirer que les plages de l'ordre de la demi-seconde place la coupe hors du domaine des phonèmes. Le seuil de **niveau**, lui, reste à poser, et il demande des tours spontanés hésitants que le banc n'a pas.

**Mesurer sur un second appareil.** Tout est mesuré sur un seul téléphone, arm64 avec instructions de produit scalaire. Rien ne dit ce que fait un appareil à 4 Go, ni un jeu d'instructions plus pauvre — et le seuil de marquage dépendant de la lecture de l'appareil, c'est une question de conception autant que de compatibilité.

**Le runtime natif chez F-Droid est le seul point non instruit.** `onnxruntime-android` est distribué en AAR pré-compilé, ce que F-Droid n'accepte pas : leur exigence est de bâtir depuis les sources. Rien n'est fermé — ONNX Runtime est en MIT et son build Android est documenté (`./build.sh --android --build_java` produit l'AAR) — mais aucun précédent d'application F-Droid qui le compile dans sa recette n'a été trouvé. Trois issues :

- **Compiler ORT dans la recette F-Droid.** Propre, mais gros build, et la reproductibilité impose d'épingler la version exacte du NDK — le point délicat avec du natif.
- **Le faire entrer comme bibliothèque partagée**, compilée une fois chez eux : le mécanisme existe, il se négocie.
- **Un build minimal d'ORT**, réduit aux seuls opérateurs de notre modèle. L'issue la plus prometteuse pour notre cas, puisqu'on ne fait tourner qu'un seul modèle : la taille et le temps de compilation tombent tous les deux, et le `.onnx` exporté par `bench/export.py` est ce dont ORT tire la liste des opérateurs à conserver. Ordre de grandeur à réduire : `libonnxruntime.so` pèse 17,5 Mo pour arm64 seul.

En attendant, la sonde est tenue au build `debug` et l'AAR pré-compilé n'entre dans aucune release.

**Finir de reloger les sondes.** Restent dans `tmp/bench/`, non relogés : le contrôle de f0 par autocorrélation (`audio_probe.py`, que le protocole nomme), la latence (`latency.py`) et la reconstruction (`reconstruct.py`, qui sert au chantier 2).

## Chantier 2 — choisir les fournisseurs de conversation et de synthèse

Le montage est tranché (cf. `docs/reference.md`) ; reste à choisir qui tient chaque maillon — Azure Speech et DeepSeek ont servi à mesurer, pas à décider.

- **La méthode de choix est un banc, pas un tableau de prix** — un banc par maillon, jamais le couple en boîte noire. Les énoncés des deux bancs sont écrits (`docs/design/grammar-test-set.md`) : banc du juge (LLM, 25 énoncés étiquetés, fausses alertes sur l'informel correct, frontière des crans, qualité d'`intended`) et banc de l'oreille (STT, fidélité verbatim sur la faute, disfluences coupées tolérées mais mots jamais réparés, ponctuation des questions — quatre prises existantes réutilisées, deux à enregistrer). **Ni l'un ni l'autre n'a commencé : ce sont les deux tiers restants du chantier.**
- Contrainte commune : l'audio de chaque tour est **conservé localement**, sinon l'analyse n'a rien à examiner.
- Contrainte de fidélité : le TTS sert de **modèle à imiter** et d'**étalon de mesure**, donc qualité, **stabilité** (le même mot sonne pareil d'une fois sur l'autre) et réussite à l'étalonnage — lequel reste à redéfinir (cf. chantier 1).
- Le TTS doit exposer **le choix de la voix** et accepter un rendu en haute qualité — le débit d'échantillonnage étant indifférent à l'analyse, un seul rendu sert à l'écoute et à la mesure.

## Chantier 3 — incarnation

- Porter le mécanisme de persona de `parcours` : persona = paramètre, catalogue de personas nommés, séparation cadre / voix. Le principe est provider-agnostique, l'injection ne l'est pas (`--system-prompt` du CLI chez `parcours` ; champ `system` du payload ou instructions de session ici).
- Une fois implémenté ici, il y a deux implémentations d'un même principe : matière à moisson vers une facette de sagesse commune.

## Reste

- **Icône de l'app** — aucune pour l'instant, l'app porte l'icône par défaut d'Android. `fdroid` exige par ailleurs un `icon.png` et un `featureGraphic.png` dans la fiche.
