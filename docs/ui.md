# L'interface — pixel doux

Ce que l'app a l'air d'être, et ce que ça décide dans le code.

Les valeurs chiffrées ont été réglées à l'œil sur **`../bench/pixel-ui.html`**, un banc qui dessine un écran complet à la résolution réelle d'un téléphone, police embarquée et palette réglable. Il reste l'instrument de tout ce qui se juge en regardant plutôt qu'en raisonnant — la rechange du daltonisme l'attend. Il est versionné : écrit à la main, il ne se régénère pas, donc le perdre serait perdre la source des couleurs.

## Ce qui borne tout le reste : la couleur porte une mesure

Sur le tour de l'apprenant, la teinte d'une lettre **est** l'écart au modèle, le filet sous une syllabe est l'accent, la bande au-dessus est la mélodie, la vaguelette est la correction, les crochets sont la pertinence (`reference.md`). Ce ne sont pas des couleurs de décor : ce sont les seules sorties visibles de l'analyse, et rien — réglage ou habillage — n'a le droit de les déplacer.

Le registre couvre **tout** l'app, cette surface comprise : même police, même grille, même palette, même fond. Ce qu'il n'y fait pas, c'est dépenser le canal de la teinte pour du décor, ou poser un filtre par-dessus. Partout ailleurs il fait ce qu'il veut.

## Le registre : la console, en doux

**Pixel art de console, chaleureux, à fond prune.** La référence est le sous-écran d'une Zelda 8 ou 16 bits — un panneau dense, encadré d'une bordure épaisse à deux tons, sur une grille de tuiles.

Ce que ça n'est pas, et qui se propose tout seul si on ne le dit pas : **ce n'est pas un lieu**. Pas de carte, pas de monde, pas de personnage qu'on croise dans un décor. L'app reste une app ; c'est son ambiance qui vient de là, pas sa grammaire d'interaction. Le registre s'obtient avec une police, une palette et des cadres, **sans un seul fichier image obligatoire**.

Le pixel doit être **doux, et cette douceur est peinte, jamais filtrée** : tramage tendre, crans voisins peu contrastés, dégradés cuits au pixel près, halos dessinés à la main. Aucun flou, aucune transparence appliquée par-dessus une surface qui porte une mesure.

## La police

**Mono10**, en licence SIL OFL 1.1 sans nom réservé, modifiée, renommée **Speakup Tile**, **embarquée dans `res/font/`** — jamais par le fournisseur Google Fonts, adossé aux services Play, donc une dépendance propriétaire que la facette `fdroid` interdit.

Elle est **écrite et non plus empruntée** : sa source de vérité est un jeu de cartes de pixels en texte, dans `../font/`, que `build.py` compile en TTF ; le TTF se commite à côté, aucun outil n'étant appelé au build. `../font/README.md` dit la boîte, les scripts et la carte de la zone privée.

Cadratin de 1024 unités, une unité de dessin valant 64, avance de 11 pixels. **La boîte fait 11 × 15 pixels** : les capitales tiennent les rangées 0 à 9, les accents les rangées 10 et 11, les descendantes les rangées −1 et −2, et une rangée sépare deux lignes. Hauteur de capitale 10, hauteur d'x 8. La colonne 10 est l'interlettre et reste vide, sauf pour les pièces de cadre, qu'une fente d'un pixel trahirait. La graisse *Regular* a un trait de **2 pixels**, la *Thin* de 1 ; les deux sont offertes en préférence utilisateur, et la rampe reste lisible en *Thin*.

**La boîte de Mono10 faisait 11 × 11 et ne pouvait pas porter le français.** Elle a grandi vers le haut et vers le bas seulement, donc aucun glyphe d'origine n'a bougé et l'avance n'a pas changé — le nombre de colonnes, la tuile de cadre et l'ancrage horizontal du marquage sont intacts. Ce que ça coûte est le pas de ligne, 15 au lieu de 11.

**Carrée et charnue, c'est-à-dire une police de tuiles et non de terminal.** Les deux familles sont à largeur fixe et se confondent : une police de terminal est haute, étroite et à trait fin, parce qu'elle sert à empiler du code ; une police de tuiles est carrée et épaisse, parce qu'elle sert à parler dans une boîte de dialogue. Le marquage a besoin de la seconde — la teinte n'a de la matière à occuper que si le glyphe est dense.

**214 glyphes par-dessus les 110 de Mono10, dont 5 redessinés** : les accents (44, plus `« » … × ½ ⅓ ¼ ° — –`), les descendantes, 16 pièces de cadre, 25 meubles, 14 phonèmes, 107 lettres brouillées. Trois choix de fond derrière le lot.

- **Un glyphe ne remplace un mot que là où la place manque.** La rangée de commandes est la seule qui en manque, donc elle est en glyphes ; `MON TOUR`, `PAUSE`, `ENVOYER` restent des mots, parce qu'ils commandent sous pression et qu'`ENVOYER` dépense une tentative.
- **Une famille se dessine entière.** Le triangle et le disque appelaient la pause et l'arrêt, `×½` appelait `×¼` : un membre manquant se remarque, et il coûte moins de le dessiner que de le découvrir absent devant un écran.
- **La prose de l'IA a sa liste fermée** — `— – °` et les huit lettres à accent aigu. Rien derrière : un caractère absent rend un carré vide, ce qui est bruyant et vrai, là où un repli silencieux masquerait le trou (`universel`, no-fallback). Le brouillage a un repli, mais **pour une lettre seulement**.

Trois décisions de dessin qui ne se rouvrent pas.

- **Les descendantes descendent vraiment.** `g j p q y` étaient tassés dans la hauteur d'x et le `g` s'y lisait `9`. Aucun redessin ne le répare : à trait de 2 pixels, un bol fermé prend quatre rangées et la queue les quatre qui restent, et un bol au-dessus d'une queue *est* la silhouette d'un `9`.
- **Les deux ligatures d'affriquée sont volontairement absentes.** Dans une police à chasse fixe, une ligature doit tenir dans une cellule : y écraser un `d` et un `ʒ` à trait de 2 pixels est strictement pire que d'en prendre deux. L'écran écrit `dʒ` et `tʃ`, qui est de l'API tout aussi standard et la notation la plus courante des dictionnaires ; les diphtongues occupent déjà deux cellules. C'est une correspondance à l'affichage : la donnée garde `ʤ` et `ʧ`.
- **Deux paires de phonèmes ne se distinguent pas, et l'inventaire les sépare quand même.** `ə` diffère de `a` d'un pixel et `ɑ` de `o` de deux — le `a` de Mono10 *est* un `e` retourné —, et aucun dessin ne les sépare à dix pixels de large. Mais `a e o ɔ` n'apparaissent **jamais seules**, uniquement en tête de diphtongue, et `ə` et `ɑ`, qui apparaissent seuls, n'ont pas de diphtongue : les deux membres de chaque paire ne tombent jamais à la même position. **Ce que ça laisse dû** : ça cesse d'être vrai le jour où un phonème s'écrit à l'intérieur d'une phrase ordinaire, où un `ə` se lira `a`. Rien ne le fait aujourd'hui.

## La taille des lettres et la grille

**Facteur 3.** Chaque pixel de dessin devient un carré de 3 × 3 pixels d'écran, ce qui donne une capitale de **10 dp** — la taille du texte ordinaire d'une app Android.

Le facteur est un **entier**, et c'est la seule contrainte dure : à 3,5 certains pixels feraient 3 pixels de large et d'autres 4, les traits sortiraient d'épaisseurs inégales et l'effet s'effondrerait. Il se calcule donc depuis la densité, et si l'utilisateur peut agrandir, ce sera par crans entiers. Il n'y a **pas de critère calculable** derrière sa valeur : c'est l'arbitrage entre la lisibilité et le nombre de colonnes, et il se tranche en regardant.

**Le nombre de colonnes se compte en dp et pas en pixels.** Le facteur suit la densité, donc une cellule fait toujours à peu près **11 dp**, et le nombre de colonnes est la largeur en dp divisée par 11 : **29** sur un petit téléphone de 320 dp, **32** sur un ordinaire de 360, **37** sur un grand de 411. Un écran de 720 pixels n'en donne pas moitié moins qu'un 1440 — sa densité est moitié moindre, il prend le facteur 2, et rend les mêmes 32 colonnes.

**Le pire écran est celui dont la densité tombe mal, pas le plus étroit.** À 420 dpi la densité vaut 2,625 ; le facteur devant être entier il monte à 3, les lettres sortent à 11,4 dp et la cellule à 12,6 — **28 colonnes** sur 360 dp. C'est ce nombre qui commande les mises en page. Il est **calculé et non mesuré**.

**La grille est horizontale.** Ce qu'elle protège est l'ancrage du marquage aux caractères : le filet couvre un nombre entier de cellules, les coins des sons qu'aucune lettre ne porte tombent sur des bornes exactes, et une classe entière d'arrondis disparaît. Ce que l'ancrage entier demande n'est d'ailleurs pas le monospace mais la **police pixel à échelle entière** — dans une police pixel les avances sont entières par construction. Le monospace vient du registre, pas du marquage.

**En vertical, rien n'est ancré** : la hauteur à laquelle une ligne se pose dans sa boîte est libre, du moment qu'elle est un nombre entier de pixels de dessin. C'est ce qui permet de donner de l'air au marquage sans rien casser. Les cadres, les panneaux et les marges se calent sur la cellule dans les deux directions, c'est ce qui les fait composer.

**Deux pas verticaux, déclarés par le thème et jamais mélangés dans une même chaîne** : une ligne de texte se pose à **15**, une rangée de cadre à **11**, qui est la tuile carrée. Le pas de texte ne se laisse jamais au défaut de la police, qui vaut 14 et collerait une descendante à l'accent de la ligne suivante.

## La palette

**Deux registres, une seule teinte.** Le prune, teinte 301, en nuit ou en pâle. Seule la clarté bascule, et les tons de décor courent en sens inverse : sur fond sombre un panneau s'éclaircit, sur fond clair il s'assombrit.

Le prune parce que **c'est la seule teinte froide qu'aucune mesure n'a réservée** : l'ambre et le rouge sont l'alarme, le vert sert deux fois (la cible d'accent, l'étiquette *juste*), et le bleu porte le contour du modèle de mélodie. Un fond dans une de ces familles ferait passer une mesure pour une nuance du fond.

| | prune de nuit | prune pâle |
|---|---|---|
| fond — clarté, chroma | 0,20 · 0,045 | 0,94 · 0,075 |
| encre neutre — clarté | 0,64 | 0,40 |
| rampe — clarté, chroma | 0,78 · 0,130 | 0,43 · 0,140 |
| teinte de fin de rampe | 32 | 29 |

**Le registre clair est mi-clair, pas blanc**, et c'est la rampe qui l'exige : plus le fond monte, plus la rampe doit descendre pour s'en détacher, et sous 0,40 un ambre est un brun. L'arc ambre → rouge y perdrait sa moitié basse.

**La règle du marquage.** Une lettre marquée doit être assez loin d'une lettre nue, et cette distance se paie en clarté, en chroma, ou dans les deux. Elle se mesure en distance perceptuelle OKLab sur le **premier cran**, le cas le plus difficile : elle vaut 0,202 en registre sombre et 0,158 en clair. C'est la seule formulation qui vaut dans les deux registres — en nuit la distance est portée surtout par la clarté, une lettre marquée **s'allume** ; en pâle par la chroma, l'encre neutre étant à peu près incolore, une lettre marquée **se colore**.

**La chroma de la rampe reste haute** : c'est elle qui porte la lisibilité. La douceur du registre vient du fond, des cadres et des formes, jamais d'affamer la rampe.

**Deux règles de calcul.** L'**écrêtage de gamut réduit la chroma** jusqu'à ce que la couleur existe en sRGB, au lieu de rogner chaque canal séparément — ce dernier déforme la teinte en plus de délaver, et c'est ce qui rendait le bout rouge de la rampe rose. Et **tous les mélanges se font en OKLab**, jamais en sRGB.

**Le budget : une trentaine d'entrées**, parce que la forme porte le canal et la couleur ne porte que l'alarme et le côté. Trois fonds (l'écran, un panneau, un cadre), trois encres (neutre, atténuée, vive), la rampe d'alarme en quatre crans d'ambre à rouge à clarté constante, deux verts (la cible d'accent, l'étiquette *juste*), un bleu (le contour du modèle de mélodie), trois ou quatre teintes de décor dont les deux tons du cadre, et les teintes de halo. Les marques du suivi, du débit, des pauses et du remplissage n'ajoutent rien : elles se peignent avec ce qui est déjà compté.

### Le daltonisme

**Une palette de rechange, en préférence utilisateur.** Le registre garde ses couleurs pour tout le monde ; qui ne les sépare pas en change. Corriger la palette de base taxerait le défaut d'une esthétique choisie pour une perception que la plupart n'ont pas.

**Le cas qui l'exige est précis, et c'est le pire du doc.** *Juste* et *à côté* sont **la même forme** — des crochets — et **deux verdicts opposés**, séparés par la seule couleur, verte contre rouge. En deutéranopie les deux convergent : la marque d'une réussite devient celle d'une faute. Le doc interdit ailleurs qu'une marque absente se confonde avec une approbation ; ici c'est la présence qui se confond avec son contraire. La pastille du suivi a le même défaut sur quatre valeurs.

**Ce n'est donc pas une permutation de teintes** : la rechange doit satisfaire **la même règle de marquage** dans les deux registres, et trouver pour les crochets deux teintes qui restent séparées sous la perception visée. Les confusions rouge-vert sont le cas à traiter d'abord ; la tritanopie toucherait le bleu du contour de mélodie, et **ce qu'elle couvre n'est pas tranché**.

## Le texte de l'IA

**Le tour de l'IA s'affiche brouillé par défaut.** L'oreille est le canal principal, et un texte lisible préempterait l'écoute.

**Le brouillage est une police, pas un effet.** Chaque lettre a sa jumelle brouillée, au codet de la lettre **plus `0xE100`** : la table de l'app est une addition, et elle n'a jamais à décider ce qu'est une lettre — un caractère se brouille si son codet décalé existe dans la police. **Une lettre sans jumelle tombe sur une jumelle générique**, à `U+E100`, le filet de la lettre qu'on ajoutera un jour en oubliant de la brouiller. La **ponctuation n'a pas de repli** et reste claire : c'est elle, avec les retours à la ligne, qui fait qu'un panneau brouillé se lit comme une phrase.

**Le brouillage recouvre l'espace de chaque lettre de carrés de 2 pixels disposés au hasard** — l'épaisseur du trait, sur la même grille que tout le reste. Ce qui survit est le support : la hauteur et la largeur de la lettre, la longueur des mots, la ponctuation et les retours à la ligne. On sent qu'il y avait une phrase, on ne déchiffre rien.

**Le brouillage est fixe** — la même lettre se couvre toujours pareil, comme une marque, le tirage venant d'une graine qui est la lettre elle-même. Les carrés font 2 pixels **dans les deux graisses** : couvrir la *Thin* de pixels isolés rend un grésillement au lieu d'une phrase.

**Ce n'est pas une préférence, c'est une position du levier `ai-turn.display`**, dont c'est le défaut. Elle tombe entre le texte net et *seulement qui parle*, le support laissé étant une aide de moins que le texte et une de plus que rien. Une préférence vaudrait partout et aucun défi ne pourrait la reprendre.

## Le tour marqué

C'est l'écran le plus dense de l'app, et le seul dont chaque pixel porte une mesure. **Le tour est une tranche du fil, qui défile au glissement** — jamais un écran à tour unique, donc pas de compteur de tour.

### La mélodie

**Elle a sa propre bande, de 22 pixels, au-dessus de la ligne de texte.** Sa verticale est une grandeur continue, dont la finesse n'a aucune raison d'être plafonnée par la hauteur d'une cellule ; la bande lui donne deux fois plus de crans. Son horizontale reste calée sur les caractères — c'est là qu'est son ancrage. **La courbe ne se coupe qu'aux fragments écartés** ; aux pauses elle reste d'un seul tenant.

**Ce qui dépasse est l'erreur.** Le contour de l'apprenant est peint dessous, sur le bout rouge de la rampe ; celui du modèle par-dessus, à la même épaisseur, dans un bleu calme. Là où les deux coïncident, le rouge est entièrement recouvert : **bien parler fait disparaître la couleur au lieu de la changer**, et la quantité de rouge visible est la quantité d'écart. La mélodie n'a donc pas de couleur à elle.

**Son seuil de sensibilité est géométrique** : l'épaisseur du trait plus la largeur du halo. En dessous, les deux courbes se recouvrent et rien n'apparaît ; l'erreur se manifeste ensuite par le halo avant le trait plein. Trait de **2 pixels**, halo de **2 pixels** de chaque côté, force **0,50**, **opaque**, dans les deux registres.

**La courbe est peinte comme une surface, pas comme une suite de segments** : pour chaque colonne de pixels on calcule l'étendue verticale que la courbe occupe, unie au raccord vers la précédente. Cette étendue est d'un seul tenant par construction, donc ni moignon ni pixel isolé ne peut apparaître dans un virage.

**Le halo est à l'extérieur du trait, jamais creusé dedans** : un trait de 2 pixels dont on atténuerait les bords n'aurait plus aucun pixel en pleine couleur. Étant opaque et mélangé vers le fond, il ne trahit pas le recouvrement.

### Les autres marques

- **La teinte** est sur les lettres, une par cran de la rampe.
- **Le filet d'accent tient une syllabe.** Une erreur en montre deux : la syllabe cible en vert, celle qui a réellement porté l'accent sur la rampe.
- **La vaguelette de correction tient un groupe de mots**, et ondule d'autant de pixels qu'elle est épaisse, sinon elle rend comme une bande floue. Sa couleur dit le cran — jaune pour *mal formé*, rouge pour *ne se dit pas*. Un soulignement était nécessaire, et il est ondulé parce que le filet droit de l'accent tombe au même endroit ; la vaguelette se lit comme une erreur de langue sans qu'on ait à l'apprendre.
- **La pertinence se marque par des crochets aux deux bouts du groupe**, pas par un cadre complet : *juste* se marque aussi, donc presque tous les groupes portent une étiquette, et une enceinte qui court le long du texte ferait de la ligne une chaîne de boîtes. Rouge pour *à côté*, jaune pour *plat*, vert pour *juste* — pas de collision avec le jaune de la vaguelette, la forme séparant les deux échelles.
- **Une pause est une colonne de points empilés dans le blanc entre les mots** — le blanc d'une cellule qui existe déjà, jamais une colonne de plus, la courbe de mélodie étant ancrée aux caractères. Trois crans, un point de 2 × 2 px par cran, un pixel d'air entre deux : la colonne tient dans 2 pixels des onze. **Les silences des bords se posent dans la colonne vide qui borde le tour.** **À une frontière de groupe, la colonne laisse les deux bras tenir dans le même blanc**, donc l'écart horizontal des crochets est fixé au pixel ; **la hauteur reste le seul paramètre, posée à 5 px.**
- **Ce qui est écarté de la phrase s'écrit en encre atténuée entre crochets** — `[um]`. Ce sont de vrais caractères, dans leur cellule et à hauteur de lettre : c'est ce qui les sépare de l'enceinte de pertinence, qui est peinte et déborde en hauteur.

**Aucune couleur sur les points de pause.** Ce qui fait qu'une pause est trop longue dépend du réglage, et aucun réglage ne touche une marque. Teinter *le plus long* serait pire : la même pause de 2 s serait teintée dans un tour calme et grise dans un tour où traîne un blanc de 3 s. Et ce sont des **points médians** : trois points bas veulent dire *du texte a été retiré*, ce que disent déjà les crochets gris.

**Le filet et la vaguelette ont chacun leur rangée** : à 2 pixels d'épaisseur, les mêler ne tient plus, et un même groupe porte souvent les deux. **Les traits des marques ont l'épaisseur des lettres**, 2 pixels, crochets compris.

**Un groupe porte son identité, pas son étiquette.** Deux groupes voisins peuvent mériter la même étiquette sans être le même groupe, et un groupe coupé par un retour à la ligne **ouvre d'un côté et ferme de l'autre**.

### L'air

**Les marques vivent dans l'interligne, pas dans la cellule.** Onze pixels ne tiennent pas à la fois les lettres, leur enceinte, le filet et la vaguelette. Les lettres gardent leur cellule ; tout le reste s'étale dans la place ajoutée en dessous.

Réglages retenus : **5 pixels d'air** entre les lettres et leur enceinte, **1 pixel** avant les marques, **un interligne** d'une ligne de grille, le texte posé à **8 pixels** dans sa ligne. Les cinq pixels se comptent **sous les descendantes** et non sous la ligne de base, et les accents demandent deux pixels de plus au-dessus : la ligne grandit de 4 pixels et rien ne se serre. Le partage n'est pas symétrique — au-dessus des lettres il n'y a que le bord de l'enceinte, en dessous il y a l'enceinte, le filet et la vaguelette.

**Les lettres se peignent par-dessus les crochets.** Ordre complet : la mélodie, la pertinence, les lettres — fragments écartés et colonne de points compris —, la vaguelette, le filet.

### Ce qui se montre, et quand

**Les marques n'arrivent pas d'un coup, et ce n'est pas un choix** : les marquages jugés tombent au retour de l'appel, les marques du son à la fin de l'analyse, deux à trois secondes plus tard. La révélation est donc canal par canal par construction — et ça tombe bien, les jugées étant à l'écran avant que l'IA parle.

**Les marques des mots restent une fois la porte des mots passée, et c'est le défaut.** Un passage qui passe porte quand même des marques, la porte lisant une note et non l'absence de marque. La ligne devient dense quand les marques du son arrivent par-dessus, et c'est le prix accepté — un `plat` sur un passage qui passe reste une chose vraie qu'on a le droit de voir. Les retirer est une préférence d'affichage, jamais de mesure.

**Un menu de conversation dit quelles marques s'affichent.** La densité mesurée — la majorité des mots portent quelque chose — fait qu'un apprenant qui travaille sa mélodie voudra éteindre le reste. Éteindre un canal n'éteint que son **affichage** : la note ne bouge pas, et rouvrir le canal remontre ce qui était là. **Huit canaux, un par marque** : la teinte des lettres, le filet d'accent, la bande de mélodie, la vaguelette, les crochets, les points de pause, la pastille du suivi, les chevrons du débit.

**Un canal éteint retire son emplacement au lieu de le vider.** Les deux derniers vivent sur la ligne qui nomme le tour, où un emplacement vide dit *non mesuré* : le laisser vide ferait mentir la ligne. Pas d'emplacement, pas de canal ; emplacement vide, non mesuré.

**Les fragments écartés ne sont pas un canal** : `[um]` est du texte et non une marque peinte, donc l'éteindre changerait la chaîne affichée, donc l'ancrage horizontal de tout le reste.

**Ce menu est du côté de l'apprenant, et n'est jamais un levier.** L'invariant l'exige — une marque dont la présence dépendrait du réglage du jour ne transporte plus rien. C'est l'inverse exact du brouillage du tour de l'IA, qui est bien un levier : là il s'agit d'une aide sur ce que l'IA dit, ici de la sortie d'une mesure. Une activité peut retirer une aide ; aucune ne cache une marque.

### La ligne qui nomme le tour

**Les deux marques qui valent pour le tour entier se posent au bout de sa ligne d'étiquette**, calées à droite : la pastille du suivi, puis le débit. Ça ne prend aucune colonne au texte et n'ajoute aucune ligne, l'étiquette existant déjà et sa moitié droite étant vide. En contrepartie, cette ligne devient structurelle : elle ne peut plus disparaître de la charpente.

- **La pastille est un disque de neuf pixels, halo d'un pixel compris**, et le tout occupe les onze pixels de la cellule. **Vert** pour `entre les lignes`, **encre neutre** pour `précis` et `en rapport`, **jaune** pour `sur le sujet` et `vague`, **rouge** pour `à côté`.
- **Le débit s'écrit en caractères** — `>><<` `><` `=` `<->` `<-->` — centré dans un champ de quatre colonnes, pour que la pastille ne bouge pas quand le cran change. Les pointes qui rentrent disent le mot comprimé, celles qui s'écartent le mot étiré : la forme porte le côté, donc la couleur ne porte que la distance. **Deux crans par côté, à seuils fixes**, écrits une fois pour toutes comme la rampe des sons et jamais tirés de la sensibilité.
- **Un emplacement vide dit *non mesuré***, et non *rien à signaler*. C'est la seule exception à la règle du projet, et elle se paie parce que l'emplacement est fixe : un emplacement vide ne se distingue pas d'un emplacement qu'on n'a pas dessiné, donc y lire une approbation serait lire une approbation dans un trou. Le `=` du débit et la pastille neutre disent donc que la mesure a eu lieu et ne trouve rien.

**Le vert marque le bon côté partout où il existe** — le cran `juste` de la pertinence et `entre les lignes` ici.

Compté sur le pire écran : l'étiquette porte un nom de locuteur, donc `Le barman` 9, un blanc, la pastille 1, un blanc, le débit 4 font **quinze colonnes sur vingt-huit**. **Quand un nom déborde, le nom se tronque et jamais les marques** : les marques portent une mesure, l'étiquette une identité qu'on connaît déjà. Une définition déclare donc un **nom court** par personnage, et l'app tronque de toute façon, l'ellipse étant dans la police.

## Le doigt

**Tout est directement tactile.** L'élément qu'on touche est celui qui se sélectionne ; le curseur existe toujours mais il marque ce qu'on vient de toucher au lieu de servir à naviguer.

**Un dessin fin n'oblige pas à une cible tactile fine.** La barre d'actions se dessine sur une ligne et sa zone tactile déborde vers le haut sur trois ; un glyphe d'une colonne reçoit la même marge invisible. Conséquence mécanique, pas un choix : **une entrée touchable occupe environ trois lignes de grille**, remplies ou vides — la recommandation Android est de 48 dp et une ligne fait ici environ 11 dp. Ce n'est pas propre au registre ; la seule différence est qu'ici le rembourrage est visible, en cellules vides.

### La rangée de commandes

**Elle vit dans le fil, sous chaque passage** — pas fixée en bas de l'écran. Six entrées, au padding égal du texte de l'IA, un filet de grille autour, et **des glyphes dès que possible** :

| entrée | largeur |
|---|---|
| le petit bouton, le **micro**, dans un cadre | 3 |
| l'écoute, le triangle | 1 |
| le côté que l'écoute atteint, `ME` / `AI`, une seule étiquette qui bascule | 2 |
| la vitesse, `×1` `×½` `×⅓`, une seule étiquette qui cycle | 2 |
| les notes du passage | 1 |
| l'analyse des sons, la loupe | 1 |

Dix colonnes plus cinq séparations font **quinze sur vingt-huit**. **Le petit bouton garde ses trois colonnes en devenant un glyphe** : un micro dans un cadre, là où les cinq autres entrées sont nues. Le cadre le distingue d'une rangée où tout le reste écoute ou règle — il est le seul à ouvrir le micro. Et le glyphe évite une largeur qui dépendrait de l'état : une étiquette qui dirait la porte du moment ferait 3 colonnes en `DIS` et 9 en `REFORMULE`, et **décalerait les cinq autres entrées** à chaque fois qu'une porte se ferme. Une rangée dont la géométrie dépend de l'état est ce que la grille existe pour empêcher.

**Le petit bouton se comporte comme le gros** : un appui lance, il se montre actif, et ce qui suit — la pause, l'envoi — est en bas. Un seul comportement à apprendre pour les deux. Il n'apparaît que sur le passage ouvert, les tentatives s'arrêtant à la clôture ; les cinq autres entrées valent sur tout passage qui porte un enregistrement.

**Les notes et les sons sont deux entrées et non une** : l'une rend le bilan du passage feuille par feuille, l'autre descend d'un cran dans l'inventaire son par son.

### Le bas de l'écran

**Quatre lignes de grille.** Sur trois, **trois boutons** — le **gros bouton** `MON TOUR`, `PAUSE`, `ENVOYER`. Sur une, la **barre d'actions**. Leur partage en largeur reste à régler au banc.

**Chacun dit une chose et une seule, et disparaît là où il n'a pas de travail.** C'est ce qui remplace un objet unique qui aurait changé de sens en route — `MON TOUR` devenant pause pendant qu'on parle puis grisé selon la position de capture ferait trois personnalités sur un bouton, et un changement visible ne dispense pas de l'apprendre. `PAUSE` n'existe donc qu'en capture à la main, la seule position qui en a une ; son absence est la forme juste à l'écran, et la phrase du levier reste la forme juste au moment où on choisit la position.

**Le gros bouton dit un tour de parole et pas une page suivante** : il ferme le passage précédent et ouvre le mien. Il forme un couple lisible avec le petit — le petit reprend la même phrase, `MON TOUR` en dit une neuve. `CONTINUER` a été écarté, qui laisserait croire qu'on saute un tour.

**Grisé, il ne dit rien de plus, et c'est la ligne d'état qui porte la raison.** Il portait *reformule d'abord* ; ça contredisait la règle plus forte d'un cran au-dessus, chacun dit une chose et une seule. La règle du projet, qu'une chose indisponible porte sa raison, reste tenue par la ligne d'état — et sur un écran où le gros bouton fait trois lignes en bas et la ligne d'état deux en haut, les deux se voient ensemble. **Le partage vaut pour tous les boutons** : ce qu'on peut faire est dit par le grisage, ce qu'il faut faire par la ligne d'état.

**`ENVOYER` existe aux trois positions de capture**, le clic restant le geste normal même en troisième, où le silence n'est que le filet qui rattrape un tour que personne n'envoie. Ce qui change d'une position à l'autre est ce qui **arme** le micro, jamais ce qui envoie.

**Le bas commande tout ce qui enregistre, quelle que soit son origine.** Le gros bouton lance un tour neuf, le petit lance une reprise, et dans les deux cas ce sont les mêmes `PAUSE` et `ENVOYER` qui suivent. Les redoubler dans la rangée de commandes tiendrait en largeur, et ce n'est pas la place qui l'écarte : ce serait deux `ENVOYER` faisant le même travail à deux endroits. **Ce qui rend ce partage sûr est que la ligne d'état nomme ce qui tourne** : pas *enregistrement en cours* mais *tour en cours*, *redite en cours*, *reformulation en pause*.

| quand | ce que portent les boutons |
|---|---|
| un enregistrement tourne, d'où qu'il vienne | `PAUSE` et `ENVOYER` vivants ; `MON TOUR` grisé |
| rien n'enregistre | `PAUSE` grisé ; `ENVOYER` absent, faute de prise à envoyer |
| une reprise tourne | le petit bouton du passage ouvert se montre actif |
| une reprise attend d'être décidée | `MON TOUR` et le petit bouton vivants tous les deux — sauf en « attend », où `MON TOUR` reste grisé jusqu'à l'épuisement des tentatives |

## La charpente

Deux objets distincts, et une règle les partage : **en haut ce qui est vrai, en bas ce qu'on peut faire.** Elle tranche les cas futurs sans se rediscuter, et elle met les gestes là où est le pouce.

**En haut, deux lignes.** La première est la **ligne d'état** : le **titre court** à gauche, et à droite, calés, les champs que le mode utilise — les **vies**, le **score**, la **note**. Chaque champ est absent quand le mode ne s'en sert pas, dans un ordre fixe pour que rien ne bouge quand une valeur change. Aucun bouton : quatre points d'entrée y prendraient 192 dp sur 360, et ils seraient loin du pouce.

- Le **titre court** est déclaré par la définition, plafonné à **dix caractères**. C'est la seconde des deux longueurs qu'une définition déclare. **Toute définition en déclare un**, celle qui n'a pas de thème comprise, qui s'appelle *Libre* — donc rien n'est anonyme.
- Les **vies** tiennent dans **trois colonnes, toujours** : jusqu'à trois, des cœurs, qu'on lit sans compter ; au-delà, un cœur et le nombre, `♥15`. L'affichage plafonne à 99, et un défi qui donne plus de 99 vies n'a pas de vies, il a un décor.
- La **note** est celle de la séance en cours, l'agrégation à plat des passages clos. Elle saute pendant les trois premiers, le dénominateur étant petit, et ce n'est pas un défaut à corriger — la masquer dirait *non mesuré*, ce qui serait faux.

La seconde est la **ligne d'état du tour**, dans son propre cadre, **toujours pleine et à jour**. Ce n'est pas une boîte d'alerte mais le narrateur du cycle : *à toi de parler*, *Speakup répond…*, *reformule-la — en cause : Grammar*, *écoute le modèle et redis*, *il te reste deux redites*. Elle occupe aussi les secondes d'attente sans rien coûter, et elle est l'endroit où une chose indisponible porte sa raison. **Quand une reprise attend, elle porte les deux issues et pas seulement l'obligation** — *reformule-la, ou lance un tour neuf ; il te reste deux reformulations*. C'est ce qui rend inutile la fenêtre qu'on aurait mise là : le micro ne s'arme pas tant que la reprise attend, donc le flux s'arrête visiblement, cette ligne dit quoi et combien il reste, et les deux boutons portent les issues.

**En bas, la barre d'actions**, d'une seule ligne, en glyphes : l'**œil** (ce qui s'affiche), les **leviers ouverts** de cette séance, le **retour**. Les leviers ouverts sont **grisés en V1**, portant leur raison : aucun écran de configuration n'existe encore, et l'entrée reste pour que la barre ne change pas de forme le jour où il existera. Elle porte les points d'entrée, constants pour un écran ; le gros bouton juste au-dessus porte l'action du moment. Les mélanger ferait bouger une entrée de place selon l'état du passage.

**Les leviers ouverts et les réglages de l'app sont deux choses.** Ce qui vaut pour cette séance — le tour de l'IA net ou brouillé, les écoutes, la capture — est ce que la définition laisse ouvert, et l'écran **se génère** : chaque position déclare déjà sa phrase lisible, donc zéro texte par activité, et un levier sans objet s'y affiche grisé en portant sa raison. Ce qui vaut pour toute l'app — la graisse, le registre, la palette de rechange, l'accent — vit ailleurs, et on n'y touche pas pendant une conversation.

**Trois notifications, et non deux.** Celle d'une **règle** qui change quelque chose est un reçu : pop-up, quelques secondes, puis elle s'efface. Celle de la **porte des mots** et celle de la **porte du son** sont des états : elles vivent dans la ligne d'état tant qu'elles sont vraies. La porte des mots **nomme la ou les aptitudes en cause**, ce que la porte du son ne fait pas — même raisonnement : la porte du son est câblée sur deux aptitudes, donc ce qu'elle nommerait serait une constante. Nommer **toutes** celles qui ferment, jamais la pire, respecte l'interdit contre l'élection d'une marque.

## La porte « Libre » : des tuiles, une par thème

**Deux colonnes de tuiles en portrait** : la première est la conversation sans thème, les suivantes sont les thèmes livrés. Une tuile ne montre que le **nombre de passages** qu'elle contient — un compte de lignes, donc rien de stocké. Les tuiles ne bougent pas de place, sauf que celles qui sont commencées passent devant, la première restant la première.

**La surface est de taille fixe, et c'est ce qui la sépare d'un catalogue** : les tuiles sont les définitions livrées, donc leur nombre est décidé par l'app et jamais par l'usage. On garde plusieurs conversations vivantes sans avoir de liste à administrer.

**Et rien n'a besoin d'être nommé.** Une tuile est identifiée par son thème, déclaré dans son fichier ; celle qui n'en a pas est identifiée par sa place. C'est ce qui retire le nom écrit par le modèle, et avec lui le champ de contrat et la règle qui l'empêchait de renommer.

**Toucher une tuile ouvre l'écran de situation, un seul écran à deux états.** Rempli, il montre la situation complétée, **le texte du champ court en couleur** pour qu'on voie d'un coup ce qu'on a apporté soi-même — ça ne coûte rien, la substitution sachant où était le trou. Vide, il montre la situation, sa question, et le choix du **genre**, *peu importe* par défaut. Deux boutons dans les deux cas : **poursuivre** et **recommencer**. La première tuile a le même écran, avec une question facultative qu'on peut laisser vide.

**« Recommencer » demande confirmation, défaut sur non.** C'est la seule chose qui rende une séance inatteignable : l'ancienne reste en base, mais tant qu'aucun écran d'historique n'existe, hors d'atteinte vaut perdue à l'usage. La règle d'`universel` sur les actions lourdes s'applique, et elle est dans le registre — un jeu demande avant d'écraser une sauvegarde.

## L'écran de bilan du passage

Un seul écran avec deux portes : poussé entre deux passages quand l'apprenant l'a réglé ainsi, ouvert à la demande par les notes de n'importe quel passage du fil. Un seul contenu à concevoir, une seule lecture à apprendre.

Il porte, **par aptitude, chaque feuille** : sa mesure brute, et sa lettre quand le mode utilise les notes. La lettre de l'aptitude se pose sur sa ligne de titre, la note du passage au-dessus. Il défile, et son `OK` reste visible quel que soit le défilement — c'est pourquoi l'`OK` vit dans la barre du bas et non dans le flux.

Pour les trois feuilles à colonne, ce qui s'affiche est le **compte par cran** et non le chiffre de la feuille, qui est une moyenne ne disant rien à personne. C'est la même donnée lue autrement, pas une seconde source.

**Il se dessine en barres horizontales, avec les blocs de jauge de la police.** Une colonne verticale n'est pas faisable : la jauge pave parce qu'elle occupe les onze colonnes, gouttière comprise, et il n'y a pas d'équivalent vertical — une rangée vide sépare toujours deux cellules empilées. La part vide d'une barre est une **couleur** et non une forme : la jauge peinte dans l'encre atténuée. **Et cette barre reprend la rampe du marquage** — le rouge qui a souligné `ne se dit pas` dans le tour compte ses mots ici. Ce n'est pas dépenser la teinte pour du décor, c'est la même mesure lue deux fois.

**Sa disposition tient dans vingt-huit colonnes.** Une ligne de feuille porte trois champs : le **nom** à gauche, la **mesure** calée à droite, la **lettre** en dernière colonne. **La mesure est calée contre la lettre et court vers la gauche autant que le nom le permet** — c'est ce qui fait tenir `entre les lignes`, seize colonnes, sur une ligne dont le nom en fait sept, et donc ce qui évite d'inventer des noms courts pour les six crans du suivi.

```
 Passage 4                 B
 PRONONCIATION             B
  Clarté             47/50 B
  Précision             72 C
  Mélodie           2,3 ½t B
  Accentuation        8/11 A
 COMPRÉHENSION             A
  Réponse entre les lignes A
 GRAMMAIRE                 C
  Forme               ▓▓░░ C
 PERTINENCE                C
  Choix              ▓▓▓░░ C
 FLUIDITÉ                  B
  Continuité       +18 pts B
  Silence max        3,2 s A
  Débit              -22 % B
  Hésitations         ▓▓░░ C
        [   OK   ]
```

**Trois règles derrière les unités.** Une **fraction plutôt qu'un pourcentage** quand la feuille compte des éléments : `47/50` dit ce que `94 %` cache, que le tour faisait cinquante sons — sur un tour de trois, le pourcentage serait un mensonge de précision. Un **signe partout où la mesure est un écart**, donc `-22 %` se lit *22 % plus lent*. Et **pas d'unité là où elle n'apprend rien** : les « points » de la précision sont une unité interne.

**Aucune tendance dedans.** Une flèche *« +1 depuis le passage précédent »* comparerait deux lectures prises sous des règles qui n'ont aucune raison d'être les mêmes. Il ne montre qu'une seule prise, la dernière du passage, comme le fil.

**Ce que le mode décide, ce sont les lettres ; ce que l'apprenant règle, c'est si l'écran est poussé.** Poussé par défaut, parce que la note d'un passage est prête pile quand l'attente commence. Sans lettres, l'écran existe quand même et porte les mesures brutes, qui sont des faits ne dépendant d'aucun réglage.

**Le pop-up d'une règle est un autre objet**, et il tombe après : la fenêtre se retire au doigt, puis viennent les phrases marquées *avant*, puis l'audio de l'IA, puis les *après*, puis le micro s'arme. Le bilan est disponible immédiatement à la fermeture, la scène attend la réponse du modèle.

## L'analyse des sons

Une ligne par caractère du tour, son ou pas, pour que la phrase se lise en descendant la colonne (`ui/AnalysisReadout.kt`).

**Un son par ligne, et la ligne n'a qu'une seule cible : ouvrir.** Le rang porte les lettres sur 6, `modèle → toi` sur 8, les points sur 5 et la barre sur 6, soit vingt-sept des vingt-huit colonnes. Il en portait trois — écouter le modèle, s'écouter, déplier — et trois boutons dans les onze pixels d'une ligne se ratent. **Les deux écoutes descendent donc dans le bloc ouvert.** Ce qui se perd est un geste ; ce qui se garde est la raison d'être de l'écran, puisqu'à trois lignes par son on en voit dix et la phrase disparaît.

**Le bloc s'ouvre dans la liste et n'est jamais un pop-up.** Une seule ligne ouverte à la fois : on retape pour fermer, on tape ailleurs pour déplacer l'ouverture, donc aucun geste d'écartement à inventer et rien ne recouvre la phrase qu'on lit. **Quand une ligne s'ouvre, la liste remonte pour la caler en haut de l'écran** — sinon vingt lignes se déplient sous une ligne du bas et personne ne les voit.

**Deux natures d'écoute vivent dans le bloc, et le cadre les sépare.** Encadrés, `MODÈLE` et `TOI` jouent **cet enregistrement-ci**, à cet endroit de la phrase, et toute la ligne est la cible. Nus, les symboles des deux répartitions jouent le **son de référence** enregistré en dur : ce que ce son est, pas ce qu'on en a fait. Sans cette différence visible on croit que toucher `d` rejoue son `d` à lui.

**Les symboles des répartitions ont leurs trois lignes**, comme tout ce qui se touche : le bloc ouvert fait une vingtaine de lignes, et ça ne coûte rien, la hauteur n'étant précieuse qu'au niveau replié.

```
f      f → f       2
o      ɑ → ɑ       4
r      ɹ → w      31 ▓▓▓▓▓▓
th     ð → d      18 ▓▓▓░░░
 ┌───────────┐┌───────────┐
 │ MODÈLE   ▶││ TOI      ▶│
 └───────────┘└───────────┘
     ð   72       d   61
     d   19       ð   22
     z    9       z   17
i      ɪ → ɪ       2
n      n → n       1
```

**La flèche, le triangle et la barre sont des meubles, pas de l'Unicode** : `arrow.right` à U+E018, `play` à U+E010, les quatre blocs de jauge à partir de U+E012. La police n'a ni `→` ni `▶` ni les blocs de trame, et une chaîne qui les écrirait en Unicode rendrait du vide.

**Les trois gestes d'écoute de l'app ne se recouvrent pas** : toucher un mot du tour marqué le joue, l'écoute de la rangée de commandes joue la phrase entière, et ici on descend au son.

## Les cadres et les images

**Les cadres sont dessinés par nous, dans la police.** Un cadre dans une grille de caractères *est* du texte : on pose un coin, des bords, un autre coin, et il s'aligne tout seul pour le prix d'une chaîne. Unicode ne porte des coins arrondis qu'en trait fin, ce qui interdit d'y trouver un cadre à la fois arrondi et charnu ; **la tuile de 11 pixels porte donc une bordure de 4 pixels, à coins de rayon 4**.

**Deux tons, par superposition de deux couches de texte** exactement calées — les deux pixels extérieurs de la bordure dans le ton clair, les deux intérieurs dans le sombre. C'est ce qui donne le relief doux d'une bordure de console plutôt qu'un bandeau plat. Les deux jeux se dessinent **d'un seul travail**, le second étant le complément du premier dans une silhouette qu'on trace de toute façon.

**Un cadre dit « objet »** : un menu, une boîte de dialogue, une carte d'activité, l'élément sélectionné. Jamais l'écran entier — deux colonnes de chaque côté sur les 32 disponibles, payées le plus cher sur le plus petit écran — et jamais le fil ni le tour marqué, qui ont besoin de toute la largeur. La règle se vérifie en écrivant un écran : si une chose est encadrée, on doit pouvoir dire ce qu'on fait avec ; sinon c'est un aplat.

**Un cadre fait deux rangées au minimum, et une ligne de texte encadrée en coûte donc deux et pas trois** : quatre pixels de bordure, la boîte de quatorze rangées, quatre pixels de bordure — vingt-deux, la tuile deux fois. En largeur, le contenu est décalé d'une cellule entière de chaque côté, la bordure laissant sept pixels d'air à l'intérieur.

**Un panneau peut n'être qu'un aplat**, un pavé de fond plus clair calé sur les cellules : la forme la plus douce dont on dispose, puisqu'un aplat n'a pas d'arête à adoucir.

**Le registre n'interdit pas les images, il exige qu'elles soient sur la grille** — et il n'en a besoin d'aucune pour exister. Leur place est l'écran-titre, les portraits, les icônes d'activités et de résultats, l'icône de l'app et la fiche F-Droid. Leur non-place est le fil et le tour marqué, où la couleur et la forme portent une mesure.

- **Le même pixel que le reste** — une illustration à pixels de 5 px à côté d'une police qui dessine sur 3 px sonne faux immédiatement, et c'est le plus facile à rater.
- **La palette du registre**, sans aucune couleur en dehors.
- **Échelle par facteur entier**, avec `FilterQuality.None`. Ce dernier seul ne suffit pas : la taille doit se calculer depuis la densité, un `Modifier.size(128.dp)` ne tombant sur un multiple entier de la source sur presque aucun écran.
- **Licence libre vérifiée à l'ajout**, pas à la soumission. Vaut aussi pour les sons.

`isCrunchPngs = false` est posé sur le build de release pour la reproductibilité F-Droid, et c'est heureux : la recompression PNG est ce qui abîme le pixel art.

## Le thème

**Un thème propre au projet, exposé par `CompositionLocalProvider`, hors de Material.** Quatre axes : les **couleurs**, dans le registre courant ; la **typographie**, qui se réduit à une police, deux graisses et quelques tailles, toutes multiples de 11 px ; la **grille** — facteur d'échelle dérivé de la densité, largeur et hauteur de cellule, sans quoi chaque composable le recalcule et ils divergeront ; et les **rythmes du marquage** — épaisseurs de trait, halo, air, interligne, qui sont des nombres partagés entre le texte et ce qui se peint autour.

## Ce qui est écarté, et pourquoi

- **Sortir de Compose** — WebView, moteur de jeu, Compose Multiplatform. `ui/MarkedTurn.kt` n'est pas un écran de widgets mais un moteur de mise en page : il mesure la même chaîne deux fois sous les mêmes contraintes, peint dans un ordre imposé, et se sert du résultat de mise en page comme carte de collision. Aucune sortie ne rend ça moins cher ; un pont JS mettrait la couche la plus dure du côté le plus fragile, un moteur de jeu demanderait de réécrire la mise en page du texte, et le multiplateforme n'a pas de cible réelle.
- **Une palette de quatre teintes façon Game Boy** — elle détruit la gradation du marquage, qui existe précisément parce qu'un marquage binaire ne transporte plus rien quand la majorité des mots portent quelque chose.
- **Des cadres peints au `Canvas`** — un cadre qui est du texte s'aligne tout seul et ne se mesure pas. Ce qui reste au `Canvas` est ce qui ne peut pas être une cellule : les marques du tour, qui se posent sur des groupes de largeur arbitraire.
- **Un shader CRT en AGSL** — `RuntimeShader` est API 33+ alors que `minSdk` vaut 26, ce qui demanderait une porte qu'`universel` interdit silencieuse. Surtout, un shader qui soustrait du rgb déplace la lecture de la teinte partout où il passe.
- **Un halo de mélodie en alpha** — il laisserait transparaître le rouge de l'apprenant sous le modèle, donc le recouvrement cesserait d'être total au moment même où il doit l'être.
- **La police par le fournisseur Google Fonts** — dépendance aux services Play, interdite par `fdroid`.
- **Un fond de parchemin chaud** en registre clair — il est dans la famille de l'ambre, donc l'alarme deviendrait une nuance du fond.
- **Le texte de l'IA qui apparaît caractère par caractère** — très juste dans le registre, mais les tours de l'IA sont brouillés et l'audio est le canal principal : un défilement qui devance ou traîne derrière la voix serait pire que pas de défilement.

## Ce qui reste ouvert

- **Les poids se règlent sur un polygone, curseurs concentriques** — un axe par enfant du nœud, le curseur court du centre au sommet, et le polygone que les curseurs dessinent se lit d'un coup. Un poids étant une **part entre frères**, c'est littéralement le partage de la voix du nœud qu'on regarde. Le nombre de côtés vient du nœud et non de l'arbre : cinq au premier niveau. Le reste de l'écran custom est hors périmètre.
- **La séparation des quatre crans en registre clair** — la gamme des couleurs disponibles n'est pas la même en sombre qu'en clair : une mesure à refaire, pas un réglage à recopier.
- **Les sons de l'app.** Rien n'est décidé. Le canal principal *est* l'audio — la voix du modèle, et le micro ouvert pendant la réponse — donc une ambiance continue entre en concurrence avec ce qu'on écoute et se fait capter par le micro. Un babil de texte est exclu d'avance, le tour de l'IA étant réellement parlé.
- **Le lecteur d'écran — hors v1, et dit plutôt que tu.** `ui/MarkedTurn.kt` est un `Canvas`, donc toute la sémantique du marquage est invisible à TalkBack. Ce n'est pas une porte fermée — un mot porte déjà son cran et sa feuille, il n'y a rien à mesurer de plus, seulement à décrire — c'est un travail qui ne se fait pas maintenant. Pour une app destinée à un dépôt public, le silence vaudrait décision par défaut.
- **La rencontre de personnage** (`../NOTES.md`) — ce qui fait qu'on rencontre quelqu'un plutôt qu'on lance un thème. C'est une grammaire d'interaction, pas un habillage, et rien ici ne la décide.
- **La pile de navigation.** Les écrans actuels ne mènent pas les uns aux autres ; le modèle d'activité en amènera qui descendent les uns dans les autres. **Ce qui est arrêté est sa racine** : l'app s'ouvre sur un écran-titre présentant les quatre modes, et tout descend de là. Ce que chacun montre derrière n'est décidé que pour Libre. **Ce que la barre d'actions porte écran par écran** s'en déduit et se décide avec : une barre ne peut pas mener où rien ne va.
- **La forme exacte de chaque mesure brute** sur l'écran de bilan. Sa largeur est arrêtée, six colonnes ; pas son écriture — `2,3 st` ou `2,3` avec l'unité au titre, `47/50` ou `47 sur 50`. Onze décisions minuscules qui se prennent d'un coup en regardant l'écran monté.
- **Où se posent les deux décomptes et le symbole d'enregistrement.** Ils doivent être visibles en permanence, la police porte déjà les blocs de jauge et un disque, et leur place n'est décidée nulle part.
