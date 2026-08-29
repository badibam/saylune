# TODO

## Chantier 1 — l'analyse : ce qu'il reste à écrire

L'analyse tourne sur l'appareil ; ce qu'elle fait et ce qui a été mesuré sont dans `docs/analysis.md`, la façon de le vérifier dans `docs/qualification.md`. Ce qui suit est ce qui manque.

- **Deux briques ne sont pas écrites** : l'**accent lexical** (7) et la **syllabification** (8). Sans elles, l'échelle du mot n'existe pas, et deux canaux du tour analysé sortent vides — l'accent et la mélodie passent tous deux par `syllables`, que rien ne calcule.
- **Éprouver que la syllabe qui résiste à la réduction est l'accentuée.** La brique 7 en dépend entièrement : c'est ce qui rend jugeable un mot dont la grille ne rend qu'un noyau (`docs/analysis.md`, brique 7). Solide en linguistique, jamais vérifié sur nos données. Le bloc F du jeu d'essai porte des fautes d'accent étiquetées et permet de le faire.
- **D'où vient le compte de syllabes d'un mot : les lettres, ou un dictionnaire.** Question d'ingénierie — un compte ne juge rien et ne varie pas d'un dialecte à l'autre, c'est la classe de la table d'affinité, déjà acceptée. Les lettres sur-comptent : `picked` (1 syllabe, 2 groupes de voyelles), `every` et `chocolate` (syncopés), `table` (`-le` syllabique), `station` (`-ion`), `-e` final muet. Un argument pèse contre elles : ces familles — `-ed` du passé, `-e` muet, mots syncopés — sont exactement celles où un apprenant ajoute une syllabe, donc la méthode se tromperait là où elle doit trancher. CMUdict est libre, couvre 134 000 mots et permet de compter ; il est écarté ailleurs pour *placer* les frontières, ce qui est un autre usage (`docs/analysis.md`, brique 8). **Rien n'est décidé sur la source.** Une frontière l'est, à tenir quand les briques 7 et 8 s'écriront : quel que soit le fournisseur du compte, **il n'entre jamais dans le calcul de la marque** — la marque naît de l'écart au modèle, le compte n'arbitre que l'interprétation, c'est-à-dire lequel des deux s'est écarté du mot. C'est ce qui rend tolérable un dictionnaire américain seul : sur la petite classe de mots dont le compte varie par dialecte (`medicine`, 2 syllabes gb, 3 us), le pire cas est une attribution fausse, jamais une fausse marque — et la règle du noyau survivant reste sûre, la syncope dialectale supprimant elle aussi une voyelle non accentuée.
- **De combien l'accent allonge une syllabe — mesuré, et la durée est reléguée.** Sur les 3 601 mots pleins de deux syllabes ou plus du jeu de test de TIMIT, bornes posées à la main, la syllabe accentuée vaut 1,39 fois la moyenne de ses voisines mais n'est **la plus longue que dans 65,0 % des mots** (`bench/stress.py`). Le chiffre est lu sur la vérité terrain, sans réseau : la durée se trompe une fois sur trois avant même que l'instrument entre en jeu, donc la comparaison prévue à son erreur n'a pas eu lieu. La branche écrite d'avance s'applique — **la brique 7 s'écrit autour de la réduction vocalique comme trait premier**, gratuite puisque c'est la grille qui l'écrit, la durée ne restant qu'en corroboration.
- **La syllabe que l'apprenant ajoute — sa piste est distincte, et elle est mesurée.** Décoder librement l'apprenant et lui passer la jointure comme au modèle : la matrice est déjà calculée pour l'alignement forcé, le décodage libre n'en est que le maximum de chaque ligne, et la jointure ne consulte pas d'horloge. C'est une lecture de plus, pas un calcul de plus, et elle situe l'écart **sur un mot**.

  Mesuré sur les trois prises de `The chair is very comfortable`, noyaux dans `comfortable` : modèle 2, **prise 10 (la faute) 4**, témoins 19 et 20 à 3 — soit **+2 pour la faute contre +1 pour les témoins**, les quatre autres mots à +0. La comparaison de formes, elle, ne séparait pas : 0,899 contre 0,718.

  Trois réserves à ne pas lisser. La marge est d'**une unité sur un seul cas**. Les témoins ne sont pas à zéro, la grille du modèle sous-comptant `comfortable` — donc la ligne de base n'est pas le compte du mot, ce qui renvoie à la question ci-dessus. Et `chair` sort à +1 sur les deux témoins par l'artefact du compteur : `ɛ`+`ɝ` est un seul noyau, et une règle sur les voyelles rhotiques est à écrire avant de se servir de ce compte.

  Les deux pistes ne se remplacent pas : grille contre grille dit que l'apprenant **diverge du modèle** et attrape la syllabe en trop ; le compte du mot dit combien le mot en porte, et c'est lui qui arbitre lequel des deux s'est écarté.
- **Pour la brique 7, lire le rythme de l'alignement existant** : la déformation locale de la correspondance temporelle modèle ↔ apprenant (l'un traîne sur une syllabe, avale la suivante), une fois la pente d'ensemble — le débit — retirée, est un signal de durée pour l'accent lexical. Les positions des sons des deux côtés sortent déjà de la matrice : c'est une lecture de plus, pas un calcul de plus.
- **L'arithmétique au-dessus de la matrice n'est pas écrite en Kotlin**, donc pas mesurée sur l'appareil ; au poste elle est négligeable devant la passe du réseau. La jointure (4) est dans le même cas.

### Le modèle de sons — le choix reste ouvert

**Le seul instrument qui mesure la marque sépare les candidats, et il donne le candidat.** `faults.py` — l'écart sur un son étiqueté fautif contre le même son étiqueté propre — n'avait jamais été lancé sur les états de l'affinage. Lancé, il montre que ce qui sépare n'est pas le réentraînement mais **le poids de la pénalité de fréquence** — l'affinage retranche à chaque son sa fréquence moyenne, ce qui retire au `∅` sa gratuité et étale chaque son sur sa durée, le poids disant de combien —, et que le pas de 0,0 à 0,1 est raide (voir plus bas). Les autres instruments — la jointure, le compte de syllabes, l'oreille à l'aveugle — ne les distinguent toujours pas.

Rien n'est tranché pour autant : la séparation tient à **un cas sur sept**, et le taux de fausse alerte hors des sons étiquetés n'est pas mesuré — le jeu d'essai ne peut pas le dire, il n'étiquette qu'un son par prise. On tourne sur `timit-ipa` en attendant. **Discussion à reprendre en début de session.**

Sur ce qui se mesurait avant `faults.py`, les cinq lectures — le sortant et les quatre états de l'affinage complet — restent **indiscernables** :

| lecture | lettres justes (`join`) | mots au bon nombre de syllabes |
|---|---|---|
| `timit-ipa` | 164 / 166 | 34 / 37 |
| `v3-pw0.0-e9` | 164 / 166 | 33 / 37 |
| `v3-pw0.0-e29` | 164 / 166 | 34 / 37 |
| `v3-pw0.1-e9` | 162 / 166 | 33 / 37 |
| `v3-pw0.1-e29` | 163 / 166 | 32 / 37 |

Lettres justes lues sur les onze phrases de calibration où les cinq rendent la même forme de grille que l'annotation. Deux sons d'écart sur 166 entre le meilleur et le pire.

**Ce que `faults.py` voit, lui**, à encodeur et données identiques — le poids de la pénalité est alors la seule variable, sur les deux voix modèles :

| lecture | `09-walkin` (zone grise) | `01-sink` | `18-walkin-full` | bande vide | fautes vues |
|---|---|---|---|---|---|
| `timit-ipa` — voix us | 0,020 | 0,247 | 0,122 | 0,018 | 7 / 7 |
| `timit-ipa` — voix gb | **0,001 manquée** | 0,246 | 0,236 | 0,234 | 6 / 7 |
| `v3-pw0.0-e29` — us | **0,000 manquée** | 0,059 | 0,113 | 0,058 | 6 / 7 |
| `v3-pw0.0-e29` — gb | **0,000 manquée** | 0,059 | 0,114 | 0,058 | 6 / 7 |
| `v3-pw0.1-e29` — us | 0,123 | 0,979 | 0,875 | 0,123 | 7 / 7 |
| `v3-pw0.1-e29` — gb | 0,126 | 0,980 | 0,880 | 0,126 | 7 / 7 |

Six des sept fautes étiquetées sont vues fort partout (au-dessus de 0,9) ; ce qui sépare est `09-walkin`. Sans pénalité elle est à 0,000 et manquée, avec la pénalité à 0,1 elle est à 0,123 — donc **au-dessus du seuil d'écran de 0,05**, dessinée. Les témoins restent à 0,000 des deux côtés. `08-light-right` est hors comparaison partout (décodage divergent, brique 11).

Deux réserves qui empêchent d'en faire une décision. Les répartitions du candidat sont plus piquées — sa « pire phrase » est à 0,99 presque partout contre 0,9 chez le sortant — et **un modèle plus tranchant sépare mieux et marque plus** ; ce que ça donne en fausses alertes hors des sons étiquetés n'est pas mesuré. Et la pénalité a un coût déjà visible à 0,1, qui va dans l'autre sens : mots au bon nombre de syllabes 34 → 32 sur 37, lettres justes à la jointure 164 → 163 sur 166. Une grille plus dense sur-segmente (`pear` prend deux noyaux pour un). **La pénalité achète la détection et vend la jointure**, donc un état se juge sur le triplet `faults.py` + `syllables.py` + `join.py`, jamais sur un seul.

**Ce que ça ne dit pas, et qui est l'essentiel** : les candidats n'ont été lus que sur onze à seize phrases d'une seule voix, avec des instruments dont on vient de voir qu'ils mesurent mal (l'étiquette n'est pas consommée, l'étendue non plus). Que rien ne les sépare **ici** ne dit pas qu'ils se valent. Deux choses rouvriraient franchement : un matériel plus large et le corpus L2 — l'instrument qui mesure ce que l'app consomme existait déjà, c'était `faults.py`, et il n'avait pas été lancé.

- **Entraîner `v3-pw0.3` — après le corpus L2, pas avant.** L'inconnue qui bloque toute décision est le taux de fausse alerte hors des sons étiquetés, qu'aucun état de plus ne mesure et que le corpus L2 (cf. « Le marquage et son seuil ») mesure pour tous les candidats à la fois ; entraîner d'abord affinerait une courbe dont on ne sait pas encore lire l'axe. Sur la courbe elle-même : deux points font une direction, pas une courbe — rien ne dit que le gain continue au-delà de 0,1, et la pénalité soustraite a forcément un point de rupture où la grille s'écrase. Un pas intermédiaire à **0,2** dirait s'il y a pente ou plateau avant de payer un entraînement de plus. À juger sur le triplet, jamais sur `faults.py` seul.

- **Entraîner sur un corpus L2 plutôt que sur TIMIT seul — réglé pour SpeechOcean762, ouvert pour L2-ARCTIC.** Jamais *à la place* de TIMIT : lui seul porte les bornes de chaque son posées à la main, sur quoi s'adosse `boundaries.py`, donc la brique 1 ; SpeechOcean762 n'attache ses notes à aucun instant. En plus de TIMIT, il reste écarté pour une raison de fond : ses suites de phonèmes sont celles **attendues**, le son réellement dit n'étant donné que sous 0,5 — tous les sons notés 1 (« juste mais fort accent »), c'est-à-dire notre zone grise, gardent l'étiquette canonique. S'entraîner dessus apprend au réseau à rendre le son voulu quand l'apprenant en a dit un autre, donc à rapprocher les deux lectures là où la marque naît de leur écart. L2-ARCTIC, lui, annote à la main les bornes de mots et de sons **corrigées** plus les substitutions, omissions et ajouts, sur ~150 énoncés par locuteur, 24 locuteurs, six langues maternelles : le seul des deux qui puisse entraîner et, en prime, étendre `boundaries.py` hors de la parole native. À demander maintenant — formulaire, lent, gratuit — mais quoi qu'on en fasse, il passe après la mesure de fausse alerte, pour la raison qui a déjà fait reculer `v3-pw0.3`.

Le coût du changement, à peser le moment venu : l'annotation d'`expected.py` est indexée sur les sons du sortant, donc en changer demande de la réécrire.

**Et la fidélité ne se juge pas à l'étiquette.** `divergence.py` a été écrit pour faire trancher à l'oreille les endroits où deux modèles ne nomment pas le même son. Neuf des dix désaccords des rendus sont des **voyelles voisines d'un même continuum** (`ɪ` / `i`, `ə` / `ɪ`, `ɝ` / `ə`), donc deux pics posés à deux endroits d'une même pente, et non deux réponses à une question fermée. Or l'étiquette est précisément ce dont la conception dit qu'elle ne dépend pas : elle n'entre dans aucune comparaison, seules les plages comptent. Elle n'est consommée qu'à la **jointure**, qui se mesure sans oreille — c'est le tableau ci-dessus.

- **À l'aveugle, l'oreille ne les sépare pas non plus : 5 pour le sortant, 5 pour le candidat** sur les dix désaccords des rendus. La première passe, donnée en sachant lequel des deux était le sortant, disait 6-4 ; elle est retirée (git la garde). Trois mesures accompagnent le décompte et n'en sont pas des conséquences :
  - **3 verdicts sur 10 ont basculé** entre les deux passes — même oreille, même audio, mêmes mots. Deux choses ont changé à la fois, l'aveugle et la vitesse ×0,33 : le chiffre **majore** l'instabilité de l'instrument, il ne l'isole pas.
  - **L'oreille a choisi la seconde ligne 8 fois sur 10**, quel que soit le modèle qui s'y trouvait (A est tombé 5 fois sur chacun). C'est le tirage qui annule ce penchant dans le décompte ; sans lui, un 8-2 de position se serait lu comme un écart entre modèles.
  - **La marge ne prédit pas le verdict** : la lecture la plus décidée l'emporte 6 fois sur 10. La barre qu'on n'a pas posée n'aurait pas trié les cas que l'oreille tranche.
- **29 désaccords sur les prises restent à juger** (`ACOUSTIC_MODEL=timit-ipa python3 divergence.py -r timit-ipa -r v3-pw0.1-e29 -t`). Ce qu'on en attend est à peser d'abord : trois fois plus de cas, mais un instrument dont on vient de mesurer qu'il rend un verdict différent une fois sur trois.
- **Le filtre annoncé n'a rien à couper, mesuré.** L'idée était d'écarter les cas où seul le maximum a basculé alors que les deux répartitions sont quasi identiques ; sur les dix cas, la Jensen-Shannon entre les deux lectures va de 0,04 à 0,94, et un seul passe sous la bande de bruit du marquage. Deux réseaux différents rendent des répartitions différentes partout, pas seulement là où l'étiquette bascule : cette distance mesure l'idiosyncrasie d'un modèle, pas le basculement. Ce que l'intuition visait est **par lecture** — une lecture qui met 0,49 sur un symbole et 0,45 sur l'autre n'en revendique aucun — et cette marge est désormais **écrite dans chaque verdict**, la moins décidée des deux allant de 0,04 à 0,99 sur les dix. Aucune barre n'est posée : rien ne dit où commence une revendication, et se recouper après coup ne coûte rien.
- **Mesurer la composition des trois instruments.** Une marque n'arrive à l'écran que si le son existe dans la grille, que l'écart le sépare du témoin, et qu'il porte les bonnes lettres. Calculable, jamais calculé.
- **L'écart de taux d'erreur phonémique n'est pas interprétable en l'état** — la part de sons faux au décodage libre. Nos états sont à 10 % quand `timit-ipa` est à 6,7 %, très bas pour TIMIT en 39 classes (état de l'art 13 à 16 %), ce qui fait soupçonner que des phrases du jeu de test aient servi à l'entraînement chez `vitouphy` — invérifiable, c'est un modèle tiers. À trancher seulement si l'écart doit peser sur une décision.
- **Un fait non expliqué à garder en tête** : sur TIMIT le décodage libre n'omet presque rien (0,7 % chez l'étalon, 1,3 à 1,5 % chez nous) alors que sur le jeu d'essai la grille perd un son sur huit. Parole lue d'un côté, synthèse ElevenLabs de l'autre — la différence n'est pas mesurée.

### Les rendus du modèle ne sont pas régénérables

**La synthèse n'est pas reproductible** : le même texte par la même voix rend un fichier différent d'un appel à l'autre, et tout chiffre du banc bouge avec lui. Mesuré en réparant le cache : sur les seize rendus de calibration, neuf n'avaient plus la durée de la matrice qu'on lisait pour eux, et quatre décodaient une autre grille. Sur les rendus refaits, `timit-ipa` voit **7 fautes sur 7** en voix `eleven-us-eric` là où le jeu en enregistrait 6 sur 7.

- `bench/out/renders/` est traité comme un cache régénérable, et il ne l'est pas. **Décidé : on ne sauvegarde pas** — rien ne prévoit de les supprimer. Ce qu'on accepte en le décidant : `git clean -xdf` retire les dossiers gitignorés, `synth.py -f` les remplace, et le disque est unique. Les rendus se refont au prix de tous les chiffres ; les vingt-sept prises étiquetées, elles, ne se refont pas du tout.
- **Refaire la lecture de l'appareil** (`phone.py`) : `READING=phone-int8` est refusé depuis, ses matrices ayant été calculées sur les anciens rendus.

### Qualifier l'étalon — un seul chantier, trois mesures

Le modèle est cru aveuglément, et c'est le maillon le moins vérifié de la chaîne : aucune voix n'est qualifiée, la synthèse n'est pas reproductible (ci-dessus), et les voix divergent entre elles de 11 à 18 % des sons — l'idiosyncrasie de la voix choisie devient la norme qu'on fait imiter. Tous les chiffres du banc reposent dessus ; les trois pièces sont petites et tiennent sur l'outillage existant.

- **L'empreinte des audios dans chaque mesure.** Chaque script du banc écrit, à côté de ses chiffres, le hash des audios qu'il a lus — une fonction utilitaire, une passe sur les scripts, et la clé dans le cache des matrices. Sans elle, la dérive des rendus peut se reproduire sans être vue, et deux chiffres séparés dans le temps ne parlent pas du même son.
- **Le test de voix, écrit.** Pour une voix, sur un jeu de phrases fixe : la netteté moyenne des pics de la grille, le pire son (répartition écrasée), les zones où la répartition s'effondre, et la divergence aux autres voix — le seul critère extérieur à la voix : 11,5 % des sons pour `eleven-us-sarah`, 13 % pour `azure-us-jenny`, 18 % pour `eleven-gb-daniel` (`tmp/voices.py`, à promouvoir) — la voix modèle étant la source de vérité, son idiosyncrasie devient la norme imitée, donc un étalon devrait être une voix ordinaire. Les seuils de passage se calibrent sur les voix avec lesquelles le banc a été mesuré, connues bonnes ; le test qui existait, lui, jugeait aux notes d'un service dont l'app ne dépend plus. L'oreille ne rend pas ce verdict, et `docs/reference.md` (« L'accent ») dit pourquoi.
- **La stabilité, mesurée une fois.** Deux rendus du même texte par la même voix : comparer les grilles, et l'écart de marquage entre elles. Que le fichier diffère est mesuré ; de combien la norme bouge ne l'est pas. Grilles quasi identiques → le cache fait le reste ; divergence sensible → critère de choix de fournisseur pour le chantier 2, à connaître avant de choisir.

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
- **Le seuil tient mieux qu'annoncé** — les sons calmes valent 0,01 à 2,2 points et la plus faible marque en vaut 25, donc la bande de bruit à 5 tombe au milieu d'un vide franc. Ce qui reste ouvert est **la zone grise** : `09-walkin` (/ŋ/ à mi-chemin) est manqué par le sortant et par `v3-pw0.0`, mais **vu à 0,123 par `v3-pw0.1`** — donc elle dépend d'abord des poids, et le poids de la pénalité la déplace. Elle bouge en outre de 0,1 entre poste et appareil, donc elle se calibre aussi sur la lecture de l'appareil.
- **Épaissir la matière du seuil avec un corpus L2 annoté — le banc est écrit, il attend des caractères.** `bench/alarms.py` déroule le montage de l'app sur SpeechOcean762 (`bench/learners.py`, le corpus et son tirage) : modèle synthétisé pour le texte du corpus, chaîne complète, et pour chaque mot le plus grand écart de ses sons. **Aucun seuil n'est posé ni rangé** — il bouge avec le poids de la pénalité et encore entre le poste et l'appareil, donc un taux calculé à 0,05 serait illisible le jour où il bouge, là où le fait brut survit ; le taux à n'importe quel seuil s'en dérive en une ligne. La jointure se fait **au mot** : le corpus indexe ses notes sur une suite de phonèmes de dictionnaire, la chaîne lit l'écart sur la grille du modèle, rien n'apparie les deux, et la brique 4 partitionne déjà les sons entre les mots à 95 %. Ce à quoi on renonce — lequel des sons du mot portait la marque — `faults.py` le mesure déjà sur le jeu maison.

  Ce que le corpus tient, mesuré : **4 947 textes distincts pour 5 000 prises**, donc un rendu par prise et c'est là toute la facture ; les deux moitiés **ne partagent aucun locuteur** (125 chacune), donc `test` sert d'instrument sans toucher `train` ; les prises sont **rangées par locuteur**, donc un sous-ensemble se tire et ne se tranche jamais. Et le milieu est large : sur un tirage à 2 500 caractères, 528 mots se répartissent en 333 propres, **175 « entre les deux »** — au moins un phonème noté « juste mais fort accent » — et 20 fautifs. Dans ce corpus la zone grise n'est pas une famille rare, c'est un tiers des mots.

  Reste à dépenser : `--plan` chiffre, `--render` achète. L'offre ElevenLabs est à court ; le palier gratuit d'Azure couvrirait la moitié `test` entière (72 600 caractères) et reste à vérifier.
- **Le taux de fausse alerte de l'accent sur un tour spontané.** Réglé pour le régime d'imitation (bloc F : 2 fautes sur 2, aucune fausse alerte), il reste inconnu là où l'apprenant n'a pas entendu le modèle — et il ne se mesure pas, étiqueter une prise spontanée exigeant ce modèle. Deux garde-fous à tenir au moment de coder : l'emphase de sens divergera toujours d'un modèle neutre, et la parenthèse doit rendre une fausse alerte peu coûteuse.
- **Le seuil de la brique 11**, à exprimer relativement à la prise plutôt qu'en constante.
- La qualification des voix modèles et la stabilité de la grille sont regroupées sous « Qualifier l'étalon » ci-dessus.

### L'appareil

**Le coût du tour long est mesuré, et c'est la mémoire qui monte : 4316 Mo sur une minute d'audio.** Le temps croît comme la longueur puissance 1,3 (×0,32 sur 3 s, ×0,86 sur 60 s), la mémoire comme son carré — c'est l'attention. Un tour d'une minute plante un appareil à 4 Go. Trois issues, non départagées par la mesure et à trancher :

- **Fenêtrer la passe** — une dizaine de secondes avec recouvrement rend le coût linéaire et borne l'empreinte à celle d'une fenêtre. Ce que la couture déplace se mesure au banc gratuitement, en comparant les mêmes prises entières et fenêtrées : l'attention étant globale, une trame près d'un bord ne voit pas le même contexte.
- **Borner franchement la durée analysable** et le dire, un tour trop long portant sa raison comme n'importe quel tour non analysé.
- **Tenir que le tour d'une minute n'est pas un cas à servir.**

Indépendamment de l'issue, **les plages vides se retirent de l'audio** — de bord comme intérieures — ce qui abrège la passe plus que proportionnellement. La coupe est sûre par son **seuil de durée** : une occlusive est du silence, mais elle dure 50 à 120 ms, donc ne retirer que les plages de l'ordre de la demi-seconde place la coupe hors du domaine des phonèmes. Le seuil de **niveau**, lui, reste à poser, et il demande des tours spontanés hésitants que le banc n'a pas.

**Mesurer sur un second appareil.** Tout est mesuré sur un seul téléphone, arm64 avec instructions de produit scalaire. Rien ne dit ce que fait un appareil à 4 Go, ni un jeu d'instructions plus pauvre — et le seuil de marquage dépendant de la lecture de l'appareil, c'est une question de conception autant que de compatibilité.

**Le runtime natif chez F-Droid est le seul point non instruit.** `onnxruntime-android` est distribué en archive Android (`.aar`) déjà compilée, ce que F-Droid n'accepte pas : leur exigence est de bâtir depuis les sources. Rien n'est fermé — ONNX Runtime est en MIT et son build Android est documenté (`./build.sh --android --build_java` produit cette archive) — mais aucun précédent d'application F-Droid qui le compile dans sa recette n'a été trouvé. Trois issues :

- **Compiler ONNX Runtime dans la recette F-Droid.** Propre, mais gros build, et la reproductibilité impose d'épingler la version exacte du NDK, la chaîne de compilation native d'Android — le point délicat avec du natif.
- **Le faire entrer comme bibliothèque partagée**, compilée une fois chez eux : le mécanisme existe, il se négocie.
- **Un build minimal d'ONNX Runtime**, réduit aux seuls opérateurs de notre modèle. L'issue la plus prometteuse pour notre cas, puisqu'on ne fait tourner qu'un seul modèle : la taille et le temps de compilation tombent tous les deux, et le `.onnx` exporté par `bench/export.py` est ce dont ONNX Runtime tire la liste des opérateurs à conserver. Ordre de grandeur à réduire : `libonnxruntime.so` pèse 17,5 Mo pour arm64 seul.

En attendant, la sonde est tenue au build `debug` et l'archive pré-compilée n'entre dans aucune release.

**Finir de reloger les sondes.** Restent dans `tmp/bench/`, non relogés : le contrôle de f0 par autocorrélation (`audio_probe.py`, que le protocole nomme), la latence (`latency.py`) et la reconstruction (`reconstruct.py`, qui sert au chantier 2).

## Chantier 2 — choisir les fournisseurs de conversation et de synthèse

Le montage est tranché (cf. `docs/reference.md`) ; reste à choisir qui tient chaque maillon — Azure Speech et DeepSeek ont servi à mesurer, pas à décider.

- **La méthode de choix est un banc, pas un tableau de prix** — un banc par maillon, jamais le couple en boîte noire. Les énoncés des deux bancs sont écrits (`docs/design/grammar-test-set.md`) : banc du juge (LLM, 25 énoncés étiquetés, fausses alertes sur l'informel correct, frontière des crans, qualité d'`intended`) et banc de l'oreille (STT, fidélité verbatim sur la faute, disfluences coupées tolérées mais mots jamais réparés, ponctuation des questions — quatre prises existantes réutilisées, deux à enregistrer). **Ni l'un ni l'autre n'a commencé : ce sont les deux tiers restants du chantier.**
- Contrainte commune : l'audio de chaque tour est **conservé localement**, sinon l'analyse n'a rien à examiner.
- Contrainte de fidélité : le TTS sert de **modèle à imiter** et d'**étalon de mesure**, donc qualité, **stabilité** (le même mot sonne pareil d'une fois sur l'autre) et réussite à l'étalonnage — lequel reste à redéfinir (cf. chantier 1).
- Le TTS doit exposer **le choix de la voix** et accepter un rendu en haute qualité — le débit d'échantillonnage étant indifférent à l'analyse, un seul rendu sert à l'écoute et à la mesure.

## Chantier 3 — incarnation

- Porter le mécanisme de persona de `parcours`, un autre projet du parc : persona = paramètre, catalogue de personas nommés, séparation cadre / voix. Le principe ne dépend pas du fournisseur, l'injection si (`--system-prompt` du CLI chez `parcours` ; champ `system` de la requête ou instructions de session ici).
- Une fois implémenté ici, il y a deux implémentations d'un même principe : matière à moisson vers une facette de sagesse commune.

## Reste

- **Icône de l'app** — aucune pour l'instant, l'app porte l'icône par défaut d'Android. `fdroid` exige par ailleurs un `icon.png` et un `featureGraphic.png` dans la fiche.
