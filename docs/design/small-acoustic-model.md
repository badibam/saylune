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

**Lues le 2026-09-10, et une seule des deux a rendu une mesure.** `base-oosawy` rend une certitude de **0,026**, à peine au-dessus du hasard sur 64 symboles, qui est 0,016. Ce n'est pas un modèle qui hésite, c'est un modèle qui n'a rien appris : sa propre fiche annonce un taux d'erreur phonémique de **0,95** après trois époques. Elle l'annonçait avant le téléchargement — **la fiche d'un affinage de démonstration porte son chiffre, et il se lit en premier**, avant 378 Mo et six minutes de ligne. `base-colab` rend **0,872**, sous la barre de 0,95, et sa fiche annonce un taux d'erreur phonémique de **0,135** sur l'inventaire TIMIT 61 : un affinage qui a convergé.

**Ce que ce chiffre mesure, et rien de plus : un encodeur de 94 M affiné sur de la transcription phonétique faite à la main garde une répartition autour de son pic.** Le garde-fou qui aurait pu écarter la taille gratuitement ne l'écarte pas. Trois choses qu'il ne couvre pas, et aucune ne se déduit de lui : le second garde-fou, les témoins, qui attend le repliement ; les fautes vues, que rien n'a comptées ; et le fait qu'**une certitude basse n'est pas une qualité** — la barre exclut, elle ne classe pas, et 0,872 est aussi le chiffre d'`espeak`, qui voit 5 fautes sur 7.

**Le second garde-fou est écrit, et `base-colab` tombe dessus** (2026-09-10). `bench/matrix.py` sait maintenant replier la répartition d'un candidat annoté en TIMIT 61 vers l'alphabet du sortant : la table Lee & Hon est lue depuis `train/timit.py` plutôt que recopiée, les colonnes qui se replient ensemble se somment — la masse sur `ih` et celle sur `ix` sont la masse sur un seul /ɪ/ —, et ce que la table laisse tomber part au blanc, les closures et les pauses parce qu'elles sont du silence, le coup de glotte parce que sa réalisation en est aussi. Un fait rencontré en l'écrivant : **un réseau est plus large que son vocabulaire**, `vocab_size` dépassant de deux les symboles que `vocab.json` nomme, sur ce candidat comme sur le sortant. Les lectures ne l'avaient jamais vu, indexant leurs colonnes par symbole sans jamais atteindre les dernières.

| sur le jeu d'essai étiqueté | fautes vues | pire témoin |
|---|---|---|
| `timit-ipa`, voix `eleven-gb-daniel` | 6 / 7 | 0,002 |
| `timit-ipa`, voix `eleven-us-eric` | 7 / 7 | 0,002 |
| `base-colab`, voix `eleven-gb-daniel` | 0 / 4 | 0,863 |
| `base-colab`, voix `eleven-us-eric` | 1 / 4 | 0,859 |

**Un témoin à 0,863 est une prise étiquetée propre que l'app marquerait.** C'est l'échec de `charsiu`, en pire — et ce n'est pas un alignement cassé : sur `13-field-clean` la phrase entière a un médian de 0,056, donc elle s'aligne, et c'est le son déclaré propre qui diverge. Les dénominateurs, eux, tombent de 7 à 4 : trois à cinq prises par voix passent au-dessus de la barre de 0,2 de la brique 11, donc le décodage libre du candidat ne se pose pas où se pose celui de la voix modèle sur près d'une prise sur trois. Le repliement est une transformation que le sortant ne subit pas, et il fusionne des colonnes, donc il rapproche deux répartitions au lieu de les écarter — il ne peut pas avoir fabriqué l'écart. Ce qu'il déplace est le décodage libre, et sa part dans les prises hors comparaison n'est pas mesurée.

**Ce que ça ne dit toujours pas est que la taille soit la cause.** Trois modèles de 94 M ont maintenant échoué ici, et les trois fois la cause écrite est une recette : une voix unique pour gruut, des alignements de dictionnaire pour charsiu, et pour celui-ci six époques de démonstration sans terme de prior, sur des cibles en TIMIT 61. L'isolat reste la seule chose qui répond à la question posée.

## Deux essais récents qui n'en sont pas

`train/train.py` porte `facebook/wav2vec2-base-960h`, et c'est la **tête à lettres** du chantier de reconnaissance locale (`local-recognition.md`) : la transcription, pas l'analyse des sons. Ses deux affinages sur AMI sont revenus pires que le modèle de départ. Rien de tout ça ne touche la brique 2.

`bench/out/onnx/` porte `letters-fp16.onnx` à côté de `letters.onnx`, 188 Mo contre 377 Mo. L'export en 16 bits existe donc, pour cette même tête à lettres, et **aucune lecture de banc n'a été faite contre lui**.

## L'entraînement à faire

**Affiner un encodeur de taille `base` sur TIMIT, avec la recette maison, tout le reste tenu identique.** C'est un isolat : même corpus, même alphabet, même préparation que les générations `v1`, `v1b` et `v3` — seul l'encodeur change, et c'est ce qui rend la lecture avant/après comparable.

Trois points de forme, qui viennent tous d'une leçon déjà payée.

- **Affinage complet, pas d'oreille gelée.** C'est ce qui fait de la génération `v3` l'isolat le plus propre du dossier, et un encodeur qu'on change est précisément la partie qu'il faut laisser bouger.
- **Le vocabulaire reste celui du sortant.** Comparer à alphabet identique est ce qui permet de lire le résultat contre les chiffres déjà au dossier ; un alphabet à soi rendrait la comparaison sans objet.
- **Le corpus est TIMIT et rien d'autre.** L'annotation dit ce qui a été *prononcé* et non ce qui était attendu, et c'est cette propriété seule qui sépare le sortant de charsiu.

### Les quatre encodeurs

Le sortant part de `facebook/wav2vec2-xls-r-300m` : auto-supervisé, multilingue, jamais affiné à une tâche. Quatre remplaçants de taille `base` se tiennent, et **on commence par le premier**. Les trois autres restent nommés ici pour que le choix suivant se fasse contre ce qui est écrit plutôt que rejoué de zéro ; lequel prendre et quand se décidera au fil des lectures.

- **`facebook/wav2vec2-base`** — 95 M, Apache-2.0, auto-supervisé seul sur LibriSpeech 960 h. C'est l'isolat le plus strict : même famille d'architecture, même nature de pré-entraînement, aucune tâche déjà apprise. Et c'est l'encodeur dont `base-oosawy` et `base-colab` sont tirés, donc les deux lectures d'étagère portent sur exactement lui et deviennent un préalable au run plutôt qu'un voisinage. **La réserve, à écrire plutôt qu'à taire** : xls-r est multilingue et celui-ci est anglais seul, donc la taille et le corpus de pré-entraînement bougent ensemble. C'est inhérent à la question et rien ne l'évite — aucun encodeur de 95 M n'a été pré-entraîné sur le corpus de xls-r.
- **`facebook/wav2vec2-base-960h`** — le même encodeur, mais déjà affiné à écrire des lettres. Ce n'est plus l'isolat, deux choses changeant à la fois, et il achète ce que rien d'autre n'achète : `local-recognition.md` désigne cette famille pour la tête à lettres, donc **une seule oreille porterait les deux têtes** — une passe pour les sons et les mots au lieu de deux réseaux dont un seul peut être résident. C'est déjà le défaut de `train/train.py`, dont le commentaire le dit. Prix : ce doc-là a explicitement découplé les deux têtes pour ne pas enchaîner la reconnaissance à une tâche coincée, et un encodeur poussé vers l'orthographe est un suspect plausible que rien n'a mesuré.
- **`microsoft/wavlm-base-plus`** — 94 M, MIT, réputé le plus fort de sa taille sur les tâches lues à la trame. C'est le plus susceptible de passer le critère, et c'est exactement ce qui abîme la lecture : s'il passe, rien ne dira si la taille n'était pas le problème ou si WavLM compense. Coûte aussi une ligne à `train/train.py`, qui importe `Wav2Vec2ForCTC` en dur là où `bench/matrix.py` passe déjà par `AutoModelForCTC`.
- **`facebook/hubert-base-ls960`** — 95 M, Apache-2.0, auto-supervisé seul, même corpus que le premier. Aucune raison de le préférer ici : il change la famille sans rien acheter, et il perd le lien avec les deux lectures d'étagère.

**Le pas à blanc est passé** (2026-09-10, processeur, deux pas) : 90,2 M de paramètres entraînables contre 315 M au sortant, une tête neuve de 42 sons sur le vocabulaire du sortant, et une perte qui descend de 8,3 à 6,0. Les clés que le chargement déclare inattendues — `quantizer`, `project_q`, `project_hid` — sont les têtes de pré-entraînement auto-supervisé : leur présence est la preuve que ce point de départ n'a jamais été affiné à une tâche, ce qui est exactement ce qui le sépare de `wav2vec2-base-960h`.

**Et une limite à poser avant le chiffre, parce que la session qui l'a écrite venait d'en cataloguer trois exemples.** Les trois modèles de 94 M mesurés ici ont échoué, et les trois fois la cause écrite est une recette mal accordée plutôt qu'un nombre de paramètres. Or l'isolat exige que **tout le reste soit tenu identique**, taux d'apprentissage compris, et rien ne dit qu'un réseau trois fois plus petit veuille le taux d'un réseau trois fois plus gros. Les deux disciplines tirent en sens contraire, et on choisit l'isolat en connaissance de cause : **si le candidat échoue au régime du sortant, la lecture est « ce régime-là ne lui va pas », pas « la taille ne suffit pas »** — et le balayage du taux devient la première chose à faire avant d'écarter la piste. C'est écrit maintenant pour ne pas se réinventer devant un mauvais chiffre.

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
