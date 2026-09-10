# Un modèle acoustique plus petit

Doc transitoire, ouvert le 2026-09-10. Il s'élague quand l'essai a rendu son verdict — l'entraînement qu'il décrit se fait une fois, et ce qui en reste vit dans `../analysis.md` et dans les commits.

## Ce qui ouvre la question

Le temps que met la passe du réseau sur le téléphone. `../analysis.md` conclut que l'analyse « disparaît en pratique » ; cette phrase est adossée à **un tour de six secondes**, qui coûte 2,4 s pendant que l'IA répond. La même page mesure aussi qu'un tour de 60 s coûte 51,5 s, et que **le temps croît comme la longueur puissance 1,3**.

Prolonger ces deux mesures — c'est du calcul sur des chiffres mesurés, pas une mesure de plus — donne environ 8 s pour un tour de 15 s, 12 s pour un tour de 20 s, 20 s pour un tour de 30 s, qui est le plafond. **Un tour de conversation ordinaire ne fait pas six secondes**, et l'usage rapporte une attente franchement gênante. L'usage rend une question, pas un chiffre ; la question est réglée par les mesures déjà au dossier.

Le réseau en service est `vitouphy/wav2vec2-xls-r-300m-timit-phoneme` : **315 millions de paramètres pour rendre 39 classes de sons**.

## Ce que les mesures existantes disent, et ce qu'elles ne disent pas

Deux des cinq candidats déjà mesurés sont de taille `base`, environ 95 millions de paramètres : `bookbot/wav2vec2-ljspeech-gruut` et `charsiu/en_w2v2_fc_10ms`. Ils sont derniers du tableau — 3 fautes vues sur 7, et 4 sur 8.

**Leur échec a une cause nommée, et ce n'est pas la taille.** Gruut est disqualifié sur sa certitude, 0,976 : toute sa masse sur un seul son, donc plus rien autour à comparer, et la cause est écrite — affiné sur **une seule voix lisant proprement**, il n'a jamais eu de raison d'hésiter. Charsiu laisse ses témoins monter à 0,796, et la cause est écrite aussi — entraîné sur des **alignements de dictionnaire**, il a appris à ramener une prononciation approximative vers le son canonique, donc il pardonne exactement la faute qu'il doit rapporter.

Ce sont deux défauts d'entraînement. La mesure dit que ces deux modèles-là sont impropres ; elle ne dit rien d'un petit modèle en général. **Aucun modèle de taille `base` entraîné sur de la transcription phonétique faite à la main n'a jamais été lu ici** — c'est-à-dire aucun sur la recette qui fait gagner le sortant.

**L'étagère en porte pourtant, et c'est un oubli plutôt qu'un écart.** Quatre affinages de taille `base` sur TIMIT existent chez Hugging Face. Deux tombent sur les critères de pièces déjà écrits (`../analysis.md`) : `mostafaashahin/…-arpa-39` ne déclare aucune licence, et `hslleo/…` non plus — son dépôt ne porte même pas de poids, seulement des fichiers de tokenizer. Restent `oosawy/wav2vec2-base-timit-phoneme` et `bihungba1101/…-demo-google-colab`, Apache-2.0 toutes deux, `facebook/wav2vec2-base` affiné sur TIMIT, inventaire TIMIT 61 brut — des sons entiers, `ay` et `ow` non coupés, donc rien qui les écarte. Elles sont enregistrées dans `bench/matrix.py` sous `base-oosawy` et `base-colab`.

**Elles se lisent avant de louer un GPU**, et une seule des deux barrières est gratuite. La certitude de `pull.py` lit la forme du pic et jamais l'étiquette, donc elle traverse un alphabet étranger sans rien demander : trois secondes disent si un encodeur de 95 M garde une répartition autour de son pic ou s'effondre comme gruut. Le second garde-fou, lui, se paie : `faults.py` ancre chaque cas sur un son de la grille, donc il faut d'abord replier la répartition 61 → 39 — `train/timit.py` porte la table, écrite pour des cibles et non pour une répartition, dont les colonnes se somment et dont les silences vont au blanc. Ce repliement ne vaut d'être écrit que si la certitude laisse un candidat debout. Et **ces deux affinages sont des démonstrations de recette inconnue** : ils répondent à la question de la taille, ils ne classent rien.

## Deux essais récents qui n'en sont pas

`train/train.py` porte `facebook/wav2vec2-base-960h`, et c'est la **tête à lettres** du chantier de reconnaissance locale (`local-recognition.md`) : la transcription, pas l'analyse des sons. Ses deux affinages sur AMI sont revenus pires que le modèle de départ. Rien de tout ça ne touche la brique 2.

`bench/out/onnx/` porte `letters-fp16.onnx` à côté de `letters.onnx`, 188 Mo contre 377 Mo. L'export en 16 bits existe donc, pour cette même tête à lettres, et **aucune lecture de banc n'a été faite contre lui**.

## L'entraînement à faire

**Affiner un encodeur de taille `base` sur TIMIT, avec la recette maison, tout le reste tenu identique.** C'est un isolat : même corpus, même alphabet, même préparation que les générations `v1`, `v1b` et `v3` — seul l'encodeur change, et c'est ce qui rend la lecture avant/après comparable.

Trois points de forme, qui viennent tous d'une leçon déjà payée.

- **Affinage complet, pas d'oreille gelée.** C'est ce qui fait de la génération `v3` l'isolat le plus propre du dossier, et un encodeur qu'on change est précisément la partie qu'il faut laisser bouger.
- **Le vocabulaire reste celui du sortant.** Comparer à alphabet identique est ce qui permet de lire le résultat contre les chiffres déjà au dossier ; un alphabet à soi rendrait la comparaison sans objet.
- **Le corpus est TIMIT et rien d'autre.** L'annotation dit ce qui a été *prononcé* et non ce qui était attendu, et c'est cette propriété seule qui sépare le sortant de charsiu.

## Ce qui décide, posé avant le chiffre

Deux garde-fous éliminatoires passent d'abord, `bench/pull.py` les imprimant déjà, trois secondes chacun : **la certitude sous 0,95** — au-delà il ne reste rien à comparer autour du pic —, et **les témoins qui restent dans la bande de bruit**. Un modèle qui échoue à l'un des deux n'a pas besoin d'aller plus loin.

Le risque est nommable d'avance : un petit modèle affiné sur un corpus étroit est exactement le profil de gruut. Le corpus phonétique est ce qui l'en sépare, et rien ne garantit que ça suffise.

**Critère proposé, à valider avant de lancer :** le candidat garde la place s'il voit au moins autant de fautes que le sortant sur le jeu d'essai étiqueté — 7 sur 8 — avec le pire témoin sous 0,01. En dessous, le 300 M reste en service et la question du temps se règle ailleurs.

## Ce que ça achèterait

Environ trois fois moins de paramètres. En prolongeant les mêmes mesures : un tour de 20 s tomberait de ~12 s à ~4 s, et l'empreinte cesserait d'être ce qui plante un appareil à 4 Go. **Et ça pourrait fermer la question de l'analyse déportée** : un modèle qui tient la mesure et va trois fois plus vite retire la raison d'envoyer quoi que ce soit à une machine distante.

## Ce qui reste dû si le candidat passe

- **Le seuil de marquage se recalibre.** `../analysis.md` pose la règle : il se calibre sur les poids qui tourneront, pas sur ceux qui ont été entraînés, faute de quoi il est trop haut d'un facteur deux dans la zone grise. Le seuil actuel ne se transporte pas.
- **La recette de quantification se refait.** Les entiers 8 bits sont à la fois plus rapides et plus légers que les poids complets, donc ils restent la cible ; mais la recette qualifiée — produits matriciels seuls, échelle par canal — a été mesurée sur *ce* modèle-ci. Une mesure d'arrondi ne vaut que pour l'arrondisseur qui l'a faite, et pas davantage pour un autre réseau. Un petit modèle a moins de marge, donc l'érosion de la zone grise est à remesurer et non à supposer.
- **Le 16 bits reste écarté**, faute d'argument : sur ce processeur les entiers 8 bits profitent d'instructions dédiées que le 16 bits n'a pas, et il coûterait une troisième recette à qualifier pour se placer au mieux entre deux états déjà connus.
