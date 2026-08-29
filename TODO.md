# TODO

## Ordre acté (2026-08-29) — le fil, pour ne plus le reperdre

1. **Acheter le chiffre derrière lequel tout fait la queue** : rendre la moitié `test` du corpus L2 et lancer `bench/alarms.py` — le taux de fausse alerte, pour tous les candidats de modèle à la fois.
2. **La brique 7 est de la recherche, hors du chemin de la v1** : la v1 marque au niveau du son, canaux d'accent vides comme aujourd'hui. Acté durablement dans `docs/reference.md` (portée de « Se taire n'est jamais une issue »).
3. **Revenir au produit** : chantier 2, deux bancs spécifiés, zéro ligne écrite.

## Chantier 1 — l'analyse : ce qu'il reste à écrire

L'analyse tourne sur l'appareil ; ce qu'elle fait et ce qui a été mesuré sont dans `docs/analysis.md`, la façon de le vérifier dans `docs/qualification.md`. Ce qui suit est ce qui manque.

- **Une brique n'est pas écrite : l'accent lexical (7).** La syllabification (8) l'est — `syllables.cut` coupe sur la grille, et sa conception est dans `docs/analysis.md`. Les deux canaux du tour analysé sortent quand même toujours vides : une syllabe que l'écran dessine porte quatre champs de plus que son étendue — quelle syllabe est accentuée de chaque côté, et la hauteur de chacune — et tous relèvent des briques 7 et 10. Remplir l'étendue en inventant le reste mettrait un accent faux à l'écran, donc `turn.py` tient le canal vide jusqu'à ce qu'il y ait du vrai à y mettre.
- **Éprouver que la syllabe qui résiste à la réduction est l'accentuée.** Éprouvé sur TIMIT, et c'est vrai : quand un seul noyau du mot porte une voyelle pleine, la syllabe forte est celle-là dans **97,5 %** des mots (`bench/stress.py`). Ce qui n'est pas éprouvé est la règle telle que la brique 7 l'appliquerait, sur un mot dont la grille perd un noyau — le bloc F du jeu d'essai en porte deux cas.

- **D'où vient le compte de syllabes d'un mot : les lettres, ou un dictionnaire.** Question d'ingénierie — un compte ne juge rien et ne varie pas d'un dialecte à l'autre, c'est la classe de la table d'affinité, déjà acceptée. Les lettres sur-comptent : `picked` (1 syllabe, 2 groupes de voyelles), `every` et `chocolate` (syncopés), `table` (`-le` syllabique), `station` (`-ion`), `-e` final muet. Un argument pèse contre elles : ces familles — `-ed` du passé, `-e` muet, mots syncopés — sont exactement celles où un apprenant ajoute une syllabe, donc la méthode se tromperait là où elle doit trancher. CMUdict est libre, couvre 134 000 mots et permet de compter ; il est écarté ailleurs pour *placer* les frontières, ce qui est un autre usage (`docs/analysis.md`, brique 8). **Rien n'est décidé sur la source.** Une frontière l'est, à tenir quand les briques 7 et 8 s'écriront : quel que soit le fournisseur du compte, **il n'entre jamais dans le calcul de la marque** — la marque naît de l'écart au modèle, le compte n'arbitre que l'interprétation, c'est-à-dire lequel des deux s'est écarté du mot. C'est ce qui rend tolérable un dictionnaire américain seul : sur la petite classe de mots dont le compte varie par dialecte (`medicine`, 2 syllabes gb, 3 us), le pire cas est une attribution fausse, jamais une fausse marque — et la règle du noyau survivant reste sûre, la syncope dialectale supprimant elle aussi une voyelle non accentuée.
- **La syllabe que l'apprenant ajoute — sa piste est distincte, et elle est mesurée.** Décoder librement l'apprenant et lui passer la jointure comme au modèle : la matrice est déjà calculée pour l'alignement forcé, le décodage libre n'en est que le maximum de chaque ligne, et la jointure ne consulte pas d'horloge. C'est une lecture de plus, pas un calcul de plus, et elle situe l'écart **sur un mot**.

  Mesuré sur les trois prises de `The chair is very comfortable`, noyaux dans `comfortable` : modèle 2, **prise 10 (la faute) 4**, témoins 19 et 20 à 3 — soit **+2 pour la faute contre +1 pour les témoins**, les quatre autres mots à +0. La comparaison de formes, elle, ne séparait pas : 0,899 contre 0,718.

  Trois réserves à ne pas lisser. La marge est d'**une unité sur un seul cas**. Les témoins ne sont pas à zéro, la grille du modèle sous-comptant `comfortable` — donc la ligne de base n'est pas le compte du mot, ce qui renvoie à la question ci-dessus. Et `chair` sort à +1 sur les deux témoins par l'artefact du compteur : `ɛ`+`ɝ` est un seul noyau, et une règle sur les voyelles rhotiques est à écrire avant de se servir de ce compte.

  Les deux pistes ne se remplacent pas : grille contre grille dit que l'apprenant **diverge du modèle** et attrape la syllabe en trop ; le compte du mot dit combien le mot en porte, et c'est lui qui arbitre lequel des deux s'est écarté. Réserve : cette piste consomme la jointure sur la voix d'apprenant, où elle n'est pas mesurée (les 95 % de la brique 4 sont lus sur des rendus de synthèse) — la brique 7 réorientée, elle, ne la consomme pas.
- **L'arithmétique au-dessus de la matrice n'est pas écrite en Kotlin**, donc pas mesurée sur l'appareil ; au poste elle est négligeable devant la passe du réseau. La jointure (4) est dans le même cas.

### La brique 7 — ce qui est mesuré

Une journée de mesures, toutes sur TIMIT (bornes posées à la main, aucun réseau) sauf la dernière. Instruments : `bench/stress.py` pour le signal lui-même, `bench/accent.py` pour la brique confrontée au corpus L2 qui note l'accent de chaque mot.

- **La durée ne porte pas l'accent.** La syllabe accentuée vaut 1,39 fois la moyenne de ses voisines mais n'est la plus longue que dans **65,0 %** des mots. La comparaison prévue à l'erreur de l'instrument n'a pas eu lieu : la durée échoue sur la vérité terrain, avant que l'instrument entre en jeu.
- **La réduction vocalique porte l'accent, mais ne parle pas souvent.** 97,5 % quand un seul noyau est plein — et ce cas ne couvre que **15 %** des mots avec l'alphabet du modèle en service, 45 % avec l'inventaire complet de TIMIT. Le repli de notre affinage (`train/timit.py`) fusionne `ə` avec `ʌ` et `ɝ` avec `ɚ`, ce qui fait tomber la justesse à 59,9 % : **un état affiné par ce repli ne peut pas lire l'accent par la réduction**, quelle que soit sa précision. C'est le premier argument mesuré qui sépare les candidats de modèle (cf. « Le modèle de sons »).
- **L'intensité tranche là où la réduction se tait, et bien.** 81,7 % contre 64,8 % à la durée sur 2 149 mots ; 79,4 % contre 60,0 % sur les seuls cas à départager. L'objection de la loudeur intrinsèque des voyelles ouvertes est levée : sur les mots dont les syllabes en concurrence portent la même voyelle, elle tient 84,3 % contre 65,3 %. C'est un trait que `docs/analysis.md` ne mentionnait nulle part.
- **La centralisation lue comme une quantité ne marche pas — pour désigner.** Lire la masse de schwa sous chaque noyau au lieu de l'étiquette binaire : **63,1 %**, et 58,1 % sur les cas à départager — sous la durée. Sans étiquette du tout, la forme seule fait 58,2 %. (Erreur de raisonnement à ne pas refaire : la règle « comparer des formes, pas des maximums » a été écrite pour comparer **l'apprenant au modèle sur la même chose** ; ici on comparait deux sons différents d'un même côté, hors de sa portée.) Bien lire la portée du chiffre : il condamne la masse comme **désignateur absolu d'un seul côté** ; la même masse lue en profil et **comparée au modèle** n'a jamais été mesurée — c'est la mesure 2 de la réorientation ci-dessous.
- **Et la brique entière rate.** Sur le corpus L2, deux lectures d'un mot dont l'accent est noté correct désignent des syllabes différentes dans **28 à 36 %** des cas — c'est-à-dire autant de marques peintes sur de la parole sans faute. Le plancher lu sur TIMIT entre deux natifs était de 15,7 % ; le réseau double ça.

Deux causes se cumulent — et le diagnostic de la première, tel qu'il avait d'abord été écrit, était trop large :

- **Le montage aligné prive d'étiquettes, pas de masse.** L'apprenant forcé sur la suite de sons du modèle a des plages mais pas de symboles, donc le test de réduction **par étiquettes** (schwa ou pas) lit la même chose des deux côtés et rend forcément le même verdict. Mais l'alignement force le **chemin**, pas les **répartitions** : sous chaque plage de noyau, les trames de l'apprenant portent toujours sa propre masse de schwa ou de voyelle pleine, lisible sans symbole. La réduction reste donc mesurable sur ce montage — comme masse, pas comme étiquette. C'est ce qui fonde la mesure 2 de la réorientation ci-dessous.
- **Le montage à deux décodages fait tourner la jointure sur la voix d'apprenant**, où elle n'a jamais été mesurée — les 95 % de la brique 4 sont lus sur des rendus de synthèse. Ça se voit à l'œil : `like` reçoit `ɝ n l aɪ k h`, dont le début appartient au mot d'avant et la fin au suivant. Conséquence chiffrée : sur 306 mots, **157 n'ont pas le même compte de syllabes des deux côtés**, et 154 d'entre eux ont l'accent noté correct — cette branche marquerait à 98 % à tort. **Abandonné pour la brique 7** ; le montage de repli est celui des hypothèses concurrentes (mesure 4).

### La brique 7 — réorientée : comparer des profils, ne jamais désigner (acté 2026-08-29)

À lire avant tout travail sur la brique — c'est la décision qui gouverne la suite.

**Ce qui est abandonné, et pourquoi c'est sans appel.** La forme essayée jusqu'ici désignait la syllabe accentuée de chaque côté (meilleure règle : réduction quand elle parle, intensité sinon — 84,1 % de justesse dans l'absolu), puis comparait les deux désignations. Cette forme a un plafond arithmétique : deux désignateurs justes à 84 %, lisant deux voix différentes, ne tombent d'accord que dans environ 84 % × 84 % ≈ 71 % des cas — le désaccord mesuré sur les mots corrects du corpus L2, 28 à 36 %, est ce que cette forme produit mécaniquement, pas un défaut réparable des indices. Pour tenir 5 % de fausses marques par ce chemin, il faudrait ~97,5 % de justesse par côté ; aucun indice de plus ne comble un tel écart. **Ne pas reprendre le travail de désignation : ni un nouvel indice, ni un meilleur départage réduction/intensité, ni la hauteur** (l'argument est dans `docs/analysis.md` : un pic de hauteur mesure le relief du mot dans la phrase, pas l'accent lexical), **ni le rythme de l'alignement** (un signal de durée, le trait le plus faible mesuré). Les chiffres de justesse absolue gardent un seul usage : classer les dimensions du profil, du plus porteur (réduction, 97,5 % quand elle parle) au plus faible (durée, 65 %).

**La forme retenue** est celle qui fait marcher le niveau du son (`docs/reference.md`) : ne jamais comparer deux maximums, comparer des formes entières — et toujours l'apprenant au modèle, jamais un côté dans l'absolu. Chaque côté rend un **profil** sur les syllabes du mot (une valeur par noyau, normalisée dans le mot), et la marque naît de la **divergence des deux profils**, l'outil de la brique 3. Les échecs mesurés ne condamnent pas cette forme : durée 65 % et centralisation 63,1 % étaient des désignations absolues d'un seul côté ; l'essai de profils de `bench/accent.py` portait sur trois fautes, trop peu pour conclure.

**À mesurer, dans cet ordre :**

1. **La règle des voyelles rhotiques dans le compteur de noyaux.** `ɪ`+`ɝ` (`here`), `ɛ`+`ɝ` (`chair`) sont **un seul** noyau et en valent deux aujourd'hui. Petit, et il assainit tous les comptes dont les lectures suivantes dépendent — la brique 8 utilise le même compteur.
2. **Le profil de masse de réduction, sous le montage aligné — la mesure clé, jamais faite.** Par noyau, lire la masse des voyelles réduites dans la matrice de l'apprenant sous la plage du noyau (une **classe** de sons, pas un phone précis) ; par mot, en faire un profil ; comparer au profil du modèle par la divergence. Ne pas confondre avec l'essai « centralisation » à 63,1 % : lui lisait un seul côté, dans l'absolu, pour désigner — trois choses que la forme retenue interdit. Se juge avec deux instruments : les fautes d'accent du bloc F doivent diverger, et le désaccord sur les mots corrects du corpus L2 doit tomber loin sous 28 %.
3. **Les profils de durée et d'intensité relatives — mêmes mots, même juge.** Dimensions d'appoint du même profil, déjà esquissées dans `bench/accent.py` sur trois fautes ; la moitié `test` du corpus L2 en porte 127.
4. **Si les profils ne séparent pas : le montage à hypothèses concurrentes.** Entre « tout forcé » et « deux décodages libres », il existe un troisième montage, standard en évaluation de prononciation, jamais essayé ici : pour un mot à n noyaux, construire n variantes de la suite de sons du modèle — dans chaque variante, un noyau différent porte la voyelle pleine, les autres sont réduits, en classes de sons — et noter chaque variante sur la matrice de l'apprenant, par le même calcul de chemin que l'alignement. Les n coûts font une **répartition sur les positions d'accent**, de chaque côté ; la marque est la divergence des deux répartitions. Ce montage rend à la réduction son pouvoir sur **tous** les mots (les hypothèses portent des étiquettes différentes), ne décode jamais l'apprenant librement (la jointure reste sur son terrain mesuré), et ne consulte aucun dictionnaire : les hypothèses sont des permutations de la lecture du modèle lui-même.

**Ce qui tient au-dessus de ces mesures :**

- Un alphabet qui fond `ə` dans `ʌ` ne peut lire aucune d'elles (justesse de la réduction mesurée à 59,9 % avec ce repli) — l'argument contre le repli de l'affinage garde tout son poids.
- **La dynamique de la voix de synthèse reste à confirmer.** Intensité et réduction n'ont de sens comme dimensions que si la synthèse les porte comme une bouche. Première lecture, 9 mots seulement : contraste d'intensité 2,03× côté modèle contre 1,14× côté prise — la synthèse est plus contrastée, l'inverse de la crainte ; soit elle marque vraiment plus, soit les prises du banc sont plates (lues avec application par une seule personne). Se tranche sur le corpus L2, 125 locuteurs.
- Le compte de syllabes n'entre jamais dans le calcul de la marque (déjà acté plus haut) : il n'arbitre que l'interprétation.
- **L'écran nomme encore une syllabe, et ce n'est pas une contradiction.** `design/ui-flow.md` met la graisse sur la syllabe que le modèle accentue et pose la réglette verte sur une destination : c'est de l'**affichage**, pas de la marque. La marque — décider s'il y a faute — naît de la divergence des profils et ne désigne rien ; l'affichage, lui, peut lire la syllabe forte du **modèle seul** (une désignation d'un seul côté, sans le plafond du double désignateur, et sur une voix de synthèse, plus contrastée que la parole du banc). Ne jamais faire remonter cette désignation d'affichage dans le calcul de la marque.

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

- **Entraîner sur un corpus L2 en plus de TIMIT — réglé pour SpeechOcean762 (non), acté pour L2-ARCTIC : instrument d'abord, entraînement peut-être, jamais avant les vérifications ci-dessous.** Jamais *à la place* de TIMIT : lui seul porte les bornes de chaque son posées à la main, sur quoi s'adosse `boundaries.py`, donc la brique 1 ; SpeechOcean762 n'attache ses notes à aucun instant, et reste écarté de l'entraînement pour une raison de fond : ses suites de phonèmes sont celles **attendues** (tous les sons de la zone grise gardent l'étiquette canonique) — s'entraîner dessus apprend au réseau à rendre le son voulu quand l'apprenant en a dit un autre, donc à rapprocher les deux lectures là où la marque naît de leur écart. L2-ARCTIC annote, lui, le son **réellement dit** : bornes de mots et de sons corrigées à la main, plus substitutions, omissions et ajouts, sur ~150 énoncés par locuteur, 24 locuteurs, six langues maternelles. **À demander maintenant** — formulaire, lent, gratuit. À réception, l'ordre est acté :

  1. **Vérifier l'inventaire de sons — la vérification qui peut clore la question à elle seule.** La réorientation de la brique 7 fait de la masse de réduction le signal clé, et c'est mesuré chez nous : un alphabet qui fond `ə` dans `ʌ` fait tomber la lecture de la réduction de 97,5 % à 59,9 %, quel que soit l'entraînement. L2-ARCTIC est annoté en ARPAbet, dont la convention courante note `AH` pour les deux. Si ses étiquettes font cette fusion, s'entraîner dessus telles quelles pousse le réseau vers exactement le repli qui a déjà disqualifié nos états affinés — soit on remappe les étiquettes avant, soit on n'entraîne pas. Dix minutes sur les fichiers du corpus.
  2. **S'en servir comme instrument, avant toute idée d'entraînement.** Deux mesures qu'aucune autre donnée ne permet : étendre `boundaries.py` à de la vraie parole d'apprenant (la brique 1 n'est vérifiée que sur du natif, et le domaine pèse — presque aucune omission sur TIMIT, un son sur huit perdu sur la synthèse, fait non expliqué) ; et donner à `faults.py` des centaines de fautes étiquetées sur de vraies bouches, contre sept sur le jeu maison. Utilisé ainsi, il multiplie les juges sans toucher au jugé.
  3. **N'entraîner que si ces mesures nomment un déficit** — par exemple des bornes qui se dégradent franchement sur la parole accentuée. Entraîner avant, c'est entraîner sans savoir quoi réparer ni comment juger le résultat. Et une réserve à garder même alors : un annotateur humain force une catégorie sur un son *entre deux* (un /s/ à mi-chemin du /θ/ reçoit l'une des deux étiquettes, tranchée) ; s'entraîner sur ces étiquettes apprend au réseau à être décidé précisément sur les sons ambigus, or l'hésitation de la répartition dans la zone grise est un signal que l'app consomme (le flou devient tolérance, la marge est écrite dans chaque verdict). C'est la version atténuée du défaut qui écarte SpeechOcean, et aucun benchmark standard ne la voit — seuls nos instruments la verraient.

  Tout ceci passe après la mesure de fausse alerte, pour la raison qui a déjà fait reculer `v3-pw0.3`.

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

### Qualifier l'étalon — un seul chantier, deux mesures

Le modèle est cru aveuglément, et c'est le maillon le moins vérifié de la chaîne : aucune voix n'est qualifiée, la synthèse n'est pas reproductible (ci-dessus), et les voix divergent entre elles de 11 à 18 % des sons — l'idiosyncrasie de la voix choisie devient la norme qu'on fait imiter. Tous les chiffres du banc reposent dessus ; les deux pièces sont petites et tiennent sur l'outillage existant. L'empreinte des audios, elle, est posée : chaque brique termine par la ligne des fichiers qu'elle a lus (`docs/qualification.md`).

- **Une première pièce du test existe : le rendu dit-il ce qu'il devrait ?** `recognition.py --source l2` confronte la grille décodée de chaque rendu du modèle aux phonèmes que le corpus L2 note attendus. Ces phonèmes sont ceux **attendus**, jamais ceux produits — ce qui les disqualifie pour juger un apprenant et les qualifie ici, un rendu étant une synthèse censée dire le canonique. Sur les 2 500 rendus d'`azure-us-jenny` : **20,5 %** de sons faux (13,7 % de substitutions, 5,7 % d'omissions, 1,2 % d'insertions), contre 6,7 % pour le même réseau sur de la parole lue.

  **Ce chiffre ne dit pas ce qu'on voulait lui faire dire — la référence est mauvaise.** Deux défauts mesurés sur les 1 869 mots distincts de la moitié `test` :

  - **Le lexique du corpus est non-rhotique, donc britannique**, alors que la voix modèle est américaine : `CAR = K AA0`, `ARM = AA0 M`, `GARDEN = G AA0 D N`, `HERE = HH IH AH0`, `PORK = P AO0 K`. **7,9 %** des mots distincts portent un R d'orthographe sans `R` ni `ER` dans leurs phonèmes. Chaque R post-vocalique est alors un désaccord garanti, quel que soit le rendu.
  - **Des transcriptions franchement fausses** : `KATE = K EH0 T`, `SOCKS = S AH0 K S`, `TOM'S = T AH0 M S` (et `S` pour un /z/), `ZERO = Z IH AH1 OW0`. Les accents sont à `0` presque partout. **6 %** des mots de plus de trois lettres n'ont `AH` pour seule voyelle notée.

  S'y ajoute ce que tout dictionnaire ignore et qu'une bouche fait : le battement (`better` en `ɾ`) et la réduction des mots outils. Le vidage brut (`tmp/edits.py`, 200 rendus) le montre — les mots les plus touchés sont `TO` (56), `THE` (32), `AND` (29), `NOT` (28), `A` (22), et **54 %** des substitutions voyelle→voyelle tombent dans un mot outil.

  Le partage qui ne demande aucun jugement — une voyelle lue comme une consonne ou l'inverse, ce qu'aucune variante de réalisation ne produit — vaut **0,98 %** des sons attendus (37 sur 3 779). Mais il est contaminé lui aussi : la moitié en est de la rhoticité (`ɹ`→`ɝ`, 12 cas), donc du désaccord de dialecte, ou une glissante lue en voyelle (`j`→`i`).

  **Ce qu'on peut en dire honnêtement** : rien ne permet de chiffrer la fidélité du rendu avec cette référence. Le vrai taux de ratés est quelque part sous 1 %, sans qu'on sache où. Ce qui est établi et ne dépend pas de la référence : **les erreurs ne se concentrent pas** — médiane de 4 opérations par rendu, maximum 11, les dix pires rendus ne portant que 11 % du total, et ce sont les phrases les plus longues. Il n'y a pas de famille de rendus cassés.

  **Les deux voix confrontées, mêmes 200 prises, mêmes 3 779 sons attendus, seule la voix change** — `azure-gb-sonia` contre `azure-us-jenny` :

  | | PER | substitutions | omissions | insertions |
  |---|---|---|---|---|
  | `azure-gb-sonia` | 15,0 % | 10,5 % | 3,8 % | 0,8 % |
  | `azure-us-jenny` | 20,0 % | 13,2 % | 5,6 % | 1,2 % |

  Cinq points d'écart, mais **pas là où on l'attendait**. La famille rhotique bouge à peine (42 cas contre 54) : le désaccord de dialecte n'était pas le moteur principal, contrairement à ce qui avait été avancé. Ce qui baisse est le battement (`t`→`ɾ`, 39 cas côté us, absent côté gb — le britannique ne bat pas), les omissions de consonnes (4,6 → 3,0 %) et le croisement voyelle/consonne (1,0 → **0,4 %**).

  Ce que la confrontation montre en plus : **le lexique du corpus est incohérent avec lui-même sur la rhoticité.** `CAR = K AA0` est non-rhotique, `SHIRT = SH ER0 T` emploie `ER`, qui est r-coloré. Aucune voix ne peut satisfaire les deux — l'américaine échoue sur les premiers (elle ajoute la coloration : `ə`→`ɝ` 23, `ɑ`→`ɝ` 13), la britannique sur les seconds (elle l'enlève : `ɝ`→`ə`, 31).

  **Ce qui domine des deux côtés est ailleurs** : les mots outils, 5,9 % des sons côté gb et 7,3 % côté us, avec les mêmes paires (`ə`→`ɪ` pour `THE` et `A`, `u`→`ɪ` pour `TO`, `ə`→`ɑ` pour `NOT`). Ni la voix, ni le dialecte, ni le réseau : un dictionnaire qui donne la forme forte de mots qu'aucune bouche ne dit ainsi.

  **Le bornage le moins contestable qu'on ait** : sur la voix britannique, où ni le battement ni la coloration américaine ne polluent, le croisement voyelle/consonne vaut **0,4 %** des sons attendus. C'est un plafond pour ce qu'aucune variante de réalisation ne peut expliquer, pas une mesure du taux de ratés — la référence reste fausse par endroits, et une erreur du réseau qui tombe entre deux voyelles y échappe.

  **Ce qui reste à faire pour mesurer vraiment la fidélité d'un rendu** : une référence fiable. Écarter les mots à R post-vocalique en retirerait une part, sans toucher aux transcriptions franchement fausses. Rien n'est fait.

  Un cas franc existe et est documenté : sur `test-1723`, le réseau lit `m ɪ l` là où sa propre voix dit `k n oʊ`, et la prise dit `k` — la marque accuse l'apprenant d'une erreur de la machine. C'est le risque que `docs/reference.md` nomme (« une voix de synthèse que l'analyse lit mal accuserait l'apprenant »), pour la première fois observé et borné.

  Ce que ça ne dit pas : combien de marques cela produit. Un ordre de grandeur, non mesuré, à partir d'un mot de trois sons — 1 % de sons faux toucherait environ 3 % des mots, soit trois points des 61 % ci-dessous. Le calcul ignore que les erreurs se groupent, et n'a pas été vérifié.

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
- **Épaissir la matière du seuil avec un corpus L2 annoté — le banc est écrit, il attend des caractères.** `bench/alarms.py` déroule le montage de l'app sur SpeechOcean762 (`bench/learners.py`, le corpus et son tirage) : modèle synthétisé pour le texte du corpus, chaîne complète, et pour chaque mot le plus grand écart de ses sons. **Aucun seuil n'est posé ni rangé** — il bouge avec le poids de la pénalité et encore entre le poste et l'appareil, donc un taux calculé à 0,05 serait illisible le jour où il bouge, là où le fait brut survit ; le taux à n'importe quel seuil s'en dérive en une ligne. La jointure se fait **au mot** : le corpus indexe ses notes sur une suite de phonèmes de dictionnaire, la chaîne lit l'écart sur la grille du modèle, rien n'apparie les deux, et la brique 4 partitionne déjà les sons entre les mots à 95 %. Ce que ce maximum coûte était donné pour nul ; c'est mesuré depuis, et ça ne l'est pas : sur la moitié `test`, le plus grand écart d'un mot vaut **13,9 fois** la médiane de ses sons quand le mot est noté propre, et **2,1 fois** quand il est noté fautif. Le maximum efface donc la forme qui distingue les deux. Ce que ça fait au chiffre que cette brique rend n'est pas tranché.

  Ce que le corpus tient, mesuré : **4 947 textes distincts pour 5 000 prises**, donc un rendu par prise et c'est là toute la facture ; les deux moitiés **ne partagent aucun locuteur** (125 chacune), donc `test` sert d'instrument sans toucher `train` ; les prises sont **rangées par locuteur**, donc un sous-ensemble se tire et ne se tranche jamais. Et le milieu est large : sur un tirage à 2 500 caractères, 528 mots se répartissent en 333 propres, **175 « entre les deux »** — au moins un phonème noté « juste mais fort accent » — et 20 fautifs. Dans ce corpus la zone grise n'est pas une famille rare, c'est un tiers des mots.

  La moitié `test` est rendue en `azure-us-jenny` — 2 500 fichiers, 74 855 caractères, dépense faite. Pour une autre voix, `--plan` chiffre et `--render` achète ; l'offre ElevenLabs est à court.

  **Le chiffre, mesuré** (2 500 prises, 125 locuteurs, 13 232 mots lus, 454 prises écartées par la brique 11 ; voix `azure-us-jenny`, lecture `timit-ipa`). Part des mots marqués, par verdict du corpus :

  | seuil | propre | entre | fautif |
  |---|---|---|---|
  | 0,02 | 68,2 % | 83,7 % | 98,0 % |
  | 0,05 | 61,3 % | 79,3 % | 95,8 % |
  | 0,20 | 49,9 % | 71,1 % | 94,6 % |

  Médianes de l'écart par mot : 0,198 / 0,757 / 0,982. Le seuil ne déplace presque rien — un facteur dix ne fait bouger les propres que de 68,2 à 49,9 %. Sur le jeu maison, les témoins plafonnent à 2,2 points et la plus faible faute vaut 25 : le seuil s'y posait dans un vide franc. Ici le milieu est peuplé, et le seuil devient un arbitrage au lieu d'un endroit vide.

  **Par locuteur** : sur les 121 locuteurs à 20 mots propres ou plus, la médiane s'étale sans trou de 0,015 à 0,877 (quartiles 0,104 / 0,242 / 0,507). Le plus calme a encore 40 % de ses mots propres marqués. Pas deux populations, un continuum. (Une lecture antérieure sur 14 locuteurs annonçait une coupe en deux : bruit d'échantillon, elle ne survit pas aux 121.)

  **Son par son**, le mot mis de côté : la médiane d'un son de mot propre est à **0,004** — le plancher des témoins du jeu maison — contre 0,016 pour « entre » et 0,336 pour « fautif ». 32,0 % des sons des mots propres dépassent 0,05, contre 57,9 % des fautifs.

  **La forme dans le mot** : le plus grand écart d'un mot vaut 13,9 fois la médiane de ses sons quand le mot est propre, 2,1 fois quand il est fautif — un mot propre marqué est calme avec un pic, un mot fautif est haut partout. Et la part du mot couverte par la marque ne sépare pas : 50 / 50 / 60 % aux trois verdicts.

  **Ce que l'oreille en dit, sur dix cas.** Mots propres à pic isolé (pic > 0,90, plus de 8 fois la médiane du mot), jugés à l'écoute avec le modèle puis la prise (`tmp/peaks.py`, verdicts dans `bench/reviews/`) : **9 vraies différences, 1 raté de la machine**. Un seul auditeur, dix cas, la bande la plus facile — c'est un indice, pas une mesure du taux.

  **Ce qui n'est pas tranché.** Le corpus note « propre » ce dont le phonème reste reconnaissable, accent toléré ; le projet a acté que l'écart d'accent est à marquer. Les deux répondent à des questions différentes, et rien ne dit quelle part des 61 % relève de l'une ou de l'autre. Une voix, un modèle acoustique, des locuteurs de langue maternelle chinoise seulement : rien ne dit ce que donnerait un autre triplet.
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
