# L'interface — pixel doux

Ce que l'app doit avoir l'air d'être, et ce que ça décide dans le code. Rien d'écrit encore : l'esthétique ne prend aucun soin tant que la boucle de conversation n'est pas finie (`../../TODO.md`, chantier 0), et ce doc existe pour que le jour où elle en prend, les décisions qui coûtent cher rétroactivement soient déjà prises.

Les valeurs chiffrées qui suivent ont été réglées à l'œil sur un banc d'essai qui dessine un écran complet à la résolution réelle d'un téléphone, police embarquée et palette réglable : **https://claude.ai/code/artifact/52cd81d9-d11e-4ad0-b571-2ea2a4984471**. Il reste l'instrument pour tout ce qui se juge en regardant plutôt qu'en raisonnant.

## Ce qui borne tout le reste : la couleur porte une mesure

Sur le tour de l'apprenant, la teinte d'une lettre **est** l'écart au modèle, le filet sous une syllabe est l'accent, la bande au-dessus de la ligne est la mélodie, la vaguelette est la correction et les crochets sont la pertinence (`../reference.md`). Ce ne sont pas des couleurs de décor : ce sont les seules sorties visibles de l'analyse, et `reference.md` interdit qu'un réglage ou un habillage les déplace.

Le registre couvre **tout** l'app, cette surface comprise — même police, même grille, même palette, même fond. Ce qu'il n'y fait pas, c'est dépenser le canal de la teinte pour du décor, et poser un filtre par-dessus. Partout ailleurs il fait ce qu'il veut.

## Le registre : la console, en doux

**Pixel art de console, chaleureux, à fond prune.** La référence est le sous-écran d'une Zelda 8 ou 16 bits — un panneau dense, encadré d'une bordure épaisse à deux tons, sur une grille de tuiles.

Ce que ça n'est pas, et qui se propose tout seul si on ne le dit pas : **ce n'est pas un lieu**. Pas de carte, pas de monde, pas de personnage qu'on croise dans un décor. L'app reste une app ; c'est son ambiance qui vient de là, pas sa grammaire d'interaction. Le registre s'obtient avec une police, une palette et des cadres, **sans un seul fichier image obligatoire**.

Le pixel doit être **doux, et cette douceur est peinte, jamais filtrée** : tramage tendre, crans voisins peu contrastés, dégradés cuits au pixel près, halos dessinés à la main. Aucun flou, aucune transparence appliquée par-dessus une surface qui porte une mesure — un traitement qui touche au rgb déplacerait la lecture de la teinte.

## La police

**Mono10**, en licence SIL OFL 1.1 **sans nom réservé déclaré**, **embarquée dans `res/font/`**. Jamais par le fournisseur Google Fonts téléchargeable, adossé aux services Google Play, donc une dépendance propriétaire que la facette `fdroid` interdit.

Mesuré sur le fichier : cadratin de 1024 unités, une unité de dessin valant 64 unités. **La cellule fait 11 × 11 pixels** — un glyphe de 10 × 10 plus un pixel d'avance. Hauteur de capitale 10, hauteur d'x 8, descente 1. La graisse *Regular* a un trait de **2 pixels**, la *Thin* de 1.

**Carrée et charnue, c'est-à-dire une police de tuiles et non de terminal.** Les deux familles sont à largeur fixe et se confondent facilement : une police de terminal est haute, étroite et à trait fin, parce qu'elle sert à empiler du code ; une police de tuiles est carrée et épaisse, parce qu'elle sert à parler dans une boîte de dialogue. Le marquage a besoin de la seconde — la teinte n'a de la matière à occuper que si le glyphe est dense.

**Les deux graisses sont offertes en préférence utilisateur.** Vérifié au banc : la rampe reste lisible en *Thin*.

### Ce qu'il faut lui ajouter

La police est modifiée et **renommée**. Trois chantiers, dont la liste exacte de glyphes reste à préciser :

- **Les accents.** Mono10 contient 110 caractères et aucun accent. Le français en demande une trentaine, minuscules et capitales. Les minuscules tiennent — il reste 2 pixels au-dessus de la hauteur d'x. Les capitales non : la hauteur de capitale égale l'ascendante, donc une capitale accentuée se dessine un pixel plus courte.
- **Les cadres.** Voir plus bas ; ils vivent dans la zone à usage privé, à partir de U+E000, jamais sur les codets Unicode de dessin de cadre, pour que rien ne casse si la police de base change.
- **Les descendantes.** La descente vaut 1 pixel, donc `g j p q y` sont tassés dans la hauteur d'x et le `g` se lit `s`. Dans une app où l'apprenant relit ses propres mots pour y voir ses fautes, une lettre ambiguë est un défaut à réparer.

## La taille des lettres

**Facteur 3.** Chaque pixel de dessin devient un carré de 3 × 3 pixels d'écran, ce qui donne une capitale de **10 dp** — la taille du texte ordinaire d'une app Android — et **32 colonnes** sur un écran de 1080.

Le facteur est un **entier**, et c'est la seule contrainte dure : à 3,5 certains pixels de dessin feraient 3 pixels de large et d'autres 4, les traits sortiraient d'épaisseurs inégales et l'effet s'effondrerait. Il se calcule donc depuis la densité de l'écran, et si l'utilisateur peut agrandir, ce sera par crans entiers.

Il n'y a **pas de critère calculable** derrière sa valeur : c'est l'arbitrage entre la lisibilité des lettres et le nombre de colonnes, et il se tranche en regardant. Le repère utile est la hauteur de capitale en dp, pas le facteur.

**La densité varie d'un appareil à l'autre, donc le nombre de colonnes aussi** : à facteur constant, un écran de 720 en donne moitié moins qu'un 1440. La mise en page doit tenir dans une plage, jamais viser un nombre précis.

## La grille

**La grille est horizontale.** Ce qu'elle protège est l'ancrage du marquage aux caractères : le filet couvre un nombre entier de cellules, les coins des sons qu'aucune lettre ne porte tombent sur des bornes exactes, et une classe entière d'arrondis disparaît de `ui/MarkedTurn.kt`, qui mesure aujourd'hui au sous-pixel.

Ce que l'ancrage entier demande n'est d'ailleurs pas le monospace mais la **police pixel à échelle entière** : dans une police pixel les avances sont des nombres entiers de pixels par construction, et multipliées par un facteur entier elles le restent. Le monospace vient du registre, pas du marquage.

**En vertical, rien n'est ancré.** La hauteur à laquelle une ligne de texte se pose dans sa boîte est libre, du moment qu'elle est un nombre entier de pixels de dessin. C'est ce qui permet de donner de l'air au marquage sans rien casser.

Les cadres, les panneaux et les marges se calent sur la cellule dans les deux directions — c'est ce qui les fait composer entre eux.

## La palette

**Deux registres, une seule teinte.** Le prune, teinte 301, en nuit ou en pâle. Seule la clarté bascule, et les tons de décor courent en sens inverse : sur fond sombre un panneau s'éclaircit, sur fond clair il s'assombrit.

Le prune parce que **c'est la seule teinte froide qu'aucune mesure n'a réservée** : l'ambre et le rouge sont l'alarme, le vert sert deux fois (la cible d'accent, l'étiquette *juste*), et le bleu porte le contour du modèle de mélodie. Un fond dans une de ces familles ferait passer une mesure pour une nuance du fond.

| | prune de nuit | prune pâle |
|---|---|---|
| fond — clarté, chroma | 0,20 · 0,045 | 0,90 · 0,075 |
| encre neutre — clarté | 0,64 | 0,40 |
| rampe — clarté, chroma | 0,78 · 0,130 | 0,43 · 0,140 |
| teinte de fin de rampe | 32 | 29 |

**Le registre clair est mi-clair, pas blanc**, et c'est la rampe qui l'exige : plus le fond monte, plus la rampe doit descendre pour s'en détacher, et sous 0,40 un ambre est un brun. L'arc ambre → rouge y perdrait sa moitié basse.

### La règle du marquage

**Une lettre marquée doit être assez loin d'une lettre nue, et cette distance se paie en clarté, en chroma, ou dans les deux.** Elle se mesure en distance perceptuelle OKLab sur le **premier cran** — le cas le plus difficile, celui qui frôle l'encre neutre. Elle vaut 0,202 en registre sombre et 0,158 en clair.

C'est la seule formulation qui vaut dans les deux registres. En nuit, la distance est portée surtout par la clarté : une lettre marquée **s'allume**. En pâle, elle est portée par la chroma, l'encre neutre étant à peu près incolore : une lettre marquée **se colore**.

**La chroma de la rampe reste haute** — c'est elle qui porte la lisibilité, et l'adoucir la coûterait. La douceur du registre vient du fond, des cadres et des formes, jamais d'affamer la rampe.

### Le daltonisme

**Une palette de rechange, en préférence utilisateur.** Le registre garde ses couleurs pour tout le monde ; qui ne les sépare pas en change. Corriger la palette de base à la place taxerait le défaut d'une esthétique choisie pour une perception que la plupart n'ont pas.

**Le cas qui l'exige est précis, et c'est le pire du doc.** *Juste* et *à côté* sont **la même forme** — des crochets — et **deux verdicts opposés**, séparés par la seule couleur, verte contre rouge. En deutéranopie les deux convergent : la marque d'une réussite devient celle d'une faute. Le doc interdit ailleurs qu'une marque absente se confonde avec une approbation ; ici c'est la présence qui se confond avec son contraire. La pastille du suivi a le même défaut sur quatre valeurs, et la rampe à quatre crans peu contrastés y ajoute la sienne.

**Ce n'est donc pas une permutation de teintes.** La palette de rechange doit satisfaire **la même règle de marquage** — la distance OKLab sur le premier cran —, dans les deux registres, et trouver pour les crochets deux teintes qui restent séparées sous la perception visée. Elle se règle au banc comme le reste de la palette. **Portée : les confusions rouge-vert**, deutéranopie et protanopie ; la tritanopie, qui toucherait le bleu du contour de mélodie, est hors périmètre et se dit.

**Et le moment est maintenant** : `ui/MarkingColors.kt` porte encore la rampe continue que l'étape 3 remplace. Déplacer une teinte coûte une heure aujourd'hui, et tout le doc plus tard.

### Deux règles de calcul

- **L'écrêtage de gamut réduit la chroma** jusqu'à ce que la couleur existe en sRGB, au lieu de rogner chaque canal séparément — ce dernier déforme la teinte en plus de la délaver, et c'est ce qui rendait le bout rouge de la rampe rose.
- **Tous les mélanges se font en OKLab**, jamais en sRGB.

### Le budget

Une trentaine d'entrées, parce que **la forme porte le canal et la couleur ne porte que l'alarme et le côté** : les choses marquées partagent la même rampe et se distinguent au trait.

- Trois fonds — l'écran, un panneau, un cadre.
- Trois encres — neutre, atténuée, vive.
- La rampe d'alarme, quatre crans d'ambre à rouge, à clarté constante.
- Deux verts — la cible d'accent, et l'étiquette *juste*.
- Un bleu — le contour du modèle de mélodie.
- Trois ou quatre teintes de décor, dont les deux tons du cadre.
- Les teintes de halo, une par couleur de trait adouci.

Les quatre marques du suivi, du débit, des pauses et du remplissage n'ajoutent rien à ce budget : elles se peignent avec le vert, la rampe, l'encre neutre et l'encre atténuée déjà comptés.

## Les cadres de mise en page

**Dessinés par nous, dans la police.** Un cadre dans une grille de caractères *est* du texte : on pose un coin, des bords, un autre coin, et il s'aligne tout seul pour le prix d'une chaîne. Unicode ne porte des coins arrondis qu'en trait fin, ce qui interdit d'y trouver un cadre à la fois arrondi et charnu ; on les dessine donc, et **la cellule de 11 pixels est exactement l'épaisseur d'une bordure de menu de console** — une tuile.

**Deux tons, par superposition de deux couches de texte** exactement calées, dans deux teintes de décor. C'est ce qui donne le relief doux d'une bordure de console plutôt qu'un bandeau plat. Les deux jeux se dessinent **d'un seul travail** : le second est le complément du premier dans une silhouette qu'on trace de toute façon, alors que rajouter un ton après coup obligerait à redécouper chaque glyphe.

**Un cadre dit « objet ».** Un menu, une boîte de dialogue, une carte d'activité, l'élément sélectionné. Jamais l'écran entier — deux colonnes de chaque côté sur les 32 disponibles, payées le plus cher sur le plus petit écran — et jamais le fil de conversation ni le tour marqué, qui ont besoin de toute la largeur. La règle se vérifie en écrivant un écran : si une chose est encadrée, on doit pouvoir dire ce qu'on fait avec ; sinon c'est un aplat.

**Un panneau peut n'être qu'un aplat**, un pavé de fond plus clair calé sur les cellules. C'est la forme la plus douce dont on dispose, puisqu'un aplat n'a pas d'arête à adoucir.

## Le texte de l'IA

**Le tour de l'IA s'affiche brouillé par défaut.** L'oreille est le canal principal, et un texte lisible préempterait l'écoute — c'est le flou déjà prévu (« les tours de l'IA sont floutables ») qui devient un brouillage.

**Le brouillage recouvre l'espace de chaque lettre de carrés de 2 pixels disposés au hasard** — l'épaisseur du trait, sur la même grille que tout le reste. Ce qui survit est le support : la hauteur et la largeur de la lettre — les ascendantes restent hautes, les descendantes basses —, la longueur des mots, la ponctuation et les retours à la ligne. Le panneau se lit comme du texte sans qu'aucune lettre ne se lise : on sent qu'il y avait une phrase, on ne déchiffre rien. Les lettres seules se couvrent, les espaces et la ponctuation ne bougent pas.

**Le brouillage est fixe** — la même lettre se couvre toujours pareil, donc le même texte se brouille pareil partout et tout le temps, comme une marque.

**Ce n'est pas une préférence, c'est une position du levier `tour-ia.affichage`** (`activity-model.md`), dont c'est le défaut. Elle tombe entre le texte net et *seulement qui parle*, le support laissé étant une aide de moins que le texte et une de plus que rien. En conversation libre l'apprenant la déplace lui-même et elle reste où il l'a laissée ; une activité qui veut imposer le net, ou n'en rien montrer, la pose comme n'importe quel autre levier. Une préférence vaudrait partout et aucun défi ne pourrait la reprendre.

## Le tour marqué

C'est l'écran le plus dense de l'app, et le seul dont chaque pixel porte une mesure. **Le tour est une tranche du fil, qui défile au glissement** — jamais un écran à tour unique, donc pas de compteur de tour.

### La mélodie

**Elle a sa propre bande, de 22 pixels, au-dessus de la ligne de texte.** Sa verticale est une grandeur continue, dont la finesse n'a aucune raison d'être plafonnée par la hauteur d'une cellule de texte ; la bande lui donne deux fois plus de crans que la ligne n'en offrirait. Son horizontale, elle, reste calée sur les caractères — c'est là qu'est son ancrage. **La courbe ne se coupe qu'aux fragments écartés**, là où la comparaison n'existe pas ; aux pauses elle reste d'un seul tenant, les points ne déplaçant rien.

**Ce qui dépasse est l'erreur.** Le contour de l'apprenant est peint dessous, sur le bout rouge de la rampe ; celui du modèle par-dessus, à la même épaisseur, dans un bleu calme. Là où les deux coïncident, le rouge est entièrement recouvert. **Bien parler fait donc disparaître la couleur au lieu de la changer**, et la quantité de rouge visible est la quantité d'écart. La mélodie n'a plus de couleur à elle : elle emprunte la rampe commune.

**Son seuil de sensibilité est géométrique** : c'est l'épaisseur du trait plus la largeur du halo. En dessous, les deux courbes se recouvrent et rien n'apparaît ; l'erreur se manifeste ensuite progressivement, par le halo avant le trait plein. Trait de **2 pixels**, halo de **2 pixels** de chaque côté, force **0,50**, **opaque**, dans les deux registres.

**La courbe est peinte comme une surface, pas comme une suite de segments.** Pour chaque colonne de pixels on calcule l'étendue verticale que la courbe occupe — la marche, unie au raccord vers la marche précédente. Cette étendue est d'un seul tenant par construction, donc ni moignon ni pixel isolé ne peut apparaître dans un virage.

**Le halo est à l'extérieur du trait, jamais creusé dedans.** Un trait de 2 pixels dont on atténuerait les bords n'aurait plus aucun pixel en pleine couleur ; il paraîtrait plus mince et plus terne qu'il n'est. Le halo étant opaque et mélangé vers le fond, il ne trahit pas le recouvrement.

### Les autres marques

- **La teinte** est sur les lettres, une par cran de la rampe.
- **Le filet d'accent tient une syllabe.** Une erreur d'accent en montre deux : la syllabe cible en vert, celle qui a réellement porté l'accent sur la rampe.
- **La vaguelette de correction tient un groupe de mots**, et ondule d'autant de pixels qu'elle est épaisse — sinon elle rend comme une bande floue. **Sa couleur dit le cran** — jaune pour *mal formé*, rouge pour *ne se dit pas* (`activity-model.md`).
- **La pertinence se marque par des crochets aux deux bouts du groupe**, pas par un cadre complet. *Juste* se marque aussi, puisque c'est la seule mesure du projet qui ait un bon côté : presque tous les groupes portent donc une étiquette, et une enceinte qui court le long du texte ferait de la ligne une chaîne de boîtes. Les crochets restent une enceinte, donc distincts du filet droit et de la vaguelette.

- **Une pause est une colonne de points empilés dans le blanc entre les mots** — le blanc d'une cellule qui existe déjà, jamais une colonne de plus, puisque la courbe de mélodie est ancrée aux caractères et ne doit pas être décalée. Trois crans, un point de 2 × 2 px par cran, un pixel d'air entre deux (`activity-model.md`) : la colonne tient dans 2 pixels des onze. **Les silences des bords se posent dans la colonne vide qui borde le tour**, l'initiale en colonne 0, la finale à la suite du dernier mot. **À une frontière de groupe, la colonne laisse les deux bras tenir dans le même blanc** — les deux bras et la colonne dans les onze pixels, donc l'écart horizontal des crochets est fixé au pixel, pas un réglage. **La hauteur — combien ils débordent au-dessus et en dessous des lettres — reste le seul paramètre, posée à 5 px.**
- **Ce qui est écarté de la phrase s'écrit en encre atténuée entre crochets** — `[um]`. Les crochets sont de vrais caractères, dans leur cellule et à hauteur de lettre : c'est ce qui les sépare de l'enceinte de pertinence, qui est peinte et déborde en hauteur.

**Le filet et la vaguelette ont chacun leur rangée** : à 2 pixels d'épaisseur, les mêler ne tient plus, et un même groupe porte souvent les deux.

**Un groupe porte son identité, pas son étiquette.** Deux groupes voisins peuvent mériter la même étiquette sans être le même groupe, et un groupe coupé par un retour à la ligne **ouvre d'un côté et ferme de l'autre**, au lieu de se dédoubler en deux enceintes complètes.

**Les traits des marques ont l'épaisseur des lettres**, 2 pixels, crochets compris.

### L'air

**Les marques vivent dans l'interligne, pas dans la cellule.** Onze pixels ne tiennent pas à la fois les lettres, leur enceinte, le filet et la vaguelette. Les lettres gardent leur cellule ; tout le reste s'étale dans la place ajoutée en dessous.

Réglages retenus : **5 pixels d'air** entre les lettres et leur enceinte, **1 pixel** avant les marques, **un interligne** d'une ligne de grille, et le texte posé à **8 pixels** dans sa ligne. Le partage n'est pas symétrique : au-dessus des lettres il n'y a que le bord de l'enceinte, en dessous il y a l'enceinte, le filet et la vaguelette.

**Les lettres se peignent par-dessus les crochets.** Ordre complet : la mélodie, la pertinence, les lettres — fragments écartés et colonne de points compris, qui vivent dans les cellules —, la vaguelette, le filet.

### Ce qui se montre, et quand

**Les marques n'arrivent pas d'un coup, et ce n'est pas un choix.** Les marquages jugés tombent au retour de l'appel, les marques du son à la fin de l'analyse, deux à trois secondes plus tard (`activity-model.md`, « Le déroulé d'un passage »). La révélation est donc canal par canal par construction — et ça tombe bien, les jugées étant à l'écran avant que l'IA parle.

**Ce qui reste à choisir est si les marques des mots restent une fois la porte des mots passée.** Un passage qui passe porte quand même des marques : la porte lit une note à la barre, pas l'absence de marque. Quand les marques du son arrivent par-dessus, la ligne devient dense pour rien si l'apprenant en a fini avec ses mots. **Les garder ou les retirer est une préférence utilisateur** — d'affichage, jamais de mesure.

**Un menu de conversation dit quelles marques s'affichent.** La densité mesurée — la majorité des mots portent quelque chose — fait qu'un apprenant qui travaille sa mélodie voudra éteindre le reste. Éteindre un canal n'éteint que son **affichage**, jamais sa mesure : la note ne bouge pas, et rouvrir le canal remontre ce qui était là.

**Ce menu est du côté de l'apprenant, et n'est jamais un levier.** L'invariant l'exige — une marque dont la présence dépendrait du réglage du jour ne transporte plus rien (`activity-model.md`). C'est l'inverse exact du brouillage du tour de l'IA, qui est bien un levier : là il s'agit d'une aide sur ce que l'IA dit, ici de la sortie d'une mesure. Une activité peut retirer une aide ; aucune ne cache une marque.

### La ligne qui nomme le tour

**Les deux marques qui valent pour le tour entier se posent au bout de sa ligne d'étiquette**, calées à droite : la pastille du suivi, puis le débit. Ça ne prend aucune colonne au texte et n'ajoute aucune ligne, l'étiquette existant déjà et sa moitié droite étant vide. En contrepartie, cette ligne devient structurelle : elle ne peut plus disparaître de la charpente.

- **La pastille est un disque de neuf pixels, halo d'un pixel compris** — le halo tient de la même recette que celui de la mélodie, mélangé vers le fond — et le tout occupe les onze pixels de la cellule.
- **Le débit s'écrit en caractères** — `>><<` `><` `=` `<->` `<-->` — centré dans un champ de quatre colonnes, pour que la pastille ne bouge pas quand le cran change.
- **Un emplacement vide dit *non mesuré***, et non *rien à signaler* (`activity-model.md`).

À regarder au banc : sur seize colonnes, l'étiquette en prend huit et les deux marques six, donc rien ne les sépare. Soit l'étiquette raccourcit, soit on l'accepte. **Et l'écran qui produit ce cas existe** : un 720 en donne vingt-et-une à facteur constant, contre trente-deux sur un 1080 — c'est là que le tour marqué et cette ligne se vérifient, pas seulement à trente-deux.

## Le doigt

**Tout est directement tactile.** L'élément qu'on touche est celui qui se sélectionne ; le curseur existe toujours mais il marque ce qu'on vient de toucher au lieu de servir à naviguer. La barre du bas affiche les actions disponibles, et ses entrées sont de vrais boutons qu'on presse. Sous le tour, une rangée de petites commandes sur la phrase, toutes au padding égal du texte de l'IA — un filet de grille tout autour : le petit bouton, dont l'étiquette dit la porte du moment — *redire* ou *reformuler* —, l'écoute (le triangle), le côté que l'écoute atteint (modèle/vous, une seule étiquette qui bascule), et la vitesse (×1, ×0,5, ×0,33, une seule étiquette qui cycle). Le gros bouton, lui, prend toute la largeur sur trois lignes.

Conséquence mécanique, pas un choix : **une entrée touchable occupe environ trois lignes de grille**, remplies ou vides. La recommandation Android est une cible de 48 dp et une ligne fait ici environ 11 dp. Ce n'est pas propre au registre — dans n'importe quelle app les lignes de liste sont rembourrées pour cette raison ; la seule différence est qu'ici le rembourrage est visible, en cellules vides.

## Les images

Le registre n'interdit pas les images, il exige qu'elles soient sur la grille — et il n'en a besoin d'aucune pour exister. Leur place est l'écran-titre, les portraits de personnages, les icônes d'activités et de résultats, l'icône de l'app et les images de la fiche F-Droid. Leur non-place est le fil de conversation et le tour marqué, où la couleur et la forme portent une mesure.

- **Le même pixel que le reste** — une illustration à pixels de 5 px à côté d'une police qui dessine sur 3 px sonne faux immédiatement, et c'est le plus facile à rater.
- **La palette du registre**, sans aucune couleur en dehors.
- **Échelle par facteur entier**, avec `FilterQuality.None`. Ce dernier seul ne suffit pas : la taille doit se calculer depuis la densité, un `Modifier.size(128.dp)` ne tombant sur un multiple entier de la source sur presque aucun écran.
- **Licence libre vérifiée à l'ajout**, pas à la soumission. Vaut aussi pour les sons, le jour où il y en aura.

`isCrunchPngs = false` est déjà posé sur le build de release pour la reproductibilité F-Droid, et c'est heureux : la recompression PNG est ce qui abîme le pixel art.

## Le thème

**Un thème propre au projet, exposé par `CompositionLocalProvider`, hors de Material.** Quatre axes :

- les **couleurs**, la palette ci-dessus, dans le registre courant ;
- la **typographie**, qui se réduit ici à une police, deux graisses et quelques tailles, toutes multiples de 11 px ;
- la **grille** — facteur d'échelle dérivé de la densité, largeur et hauteur de cellule. Si ça ne vit pas dans le `CompositionLocal`, chaque composable le recalcule et ils divergeront ;
- les **rythmes du marquage** — épaisseurs de trait, halo, air, interligne. Mêmes raisons : ce sont des nombres partagés entre le texte et ce qui se peint autour.

C'est la première chose à faire, et la moins chère maintenant : l'UI fait aujourd'hui neuf fichiers et environ 2 200 lignes, et `MaterialTheme` y est lu aux points porteurs — `MaterialTheme.typography.headlineSmall` pour le style du tour marqué, `MaterialTheme.colorScheme.surface` pour le fond dont le halo est découpé.

## Ce qui est écarté, et pourquoi

Pour que ces questions ne se reposent pas.

- **Sortir de Compose** — WebView, moteur de jeu, Compose Multiplatform. `ui/MarkedTurn.kt` n'est pas un écran de widgets mais un moteur de mise en page : il mesure la même chaîne deux fois sous les mêmes contraintes, peint dans un ordre imposé, et se sert du résultat de mise en page comme carte de collision. Aucune sortie ne rend ça moins cher ; un pont JS mettrait la couche la plus dure du côté le plus fragile, un moteur de jeu demanderait de réécrire la mise en page du texte, et le multiplateforme n'a pas de cible réelle (`android` : natif par défaut ; `fdroid` : iOS hors d'atteinte).
- **Une palette de quatre teintes façon Game Boy** — elle détruit la gradation du marquage, qui existe précisément parce qu'un marquage binaire ne transporte plus rien quand la majorité des mots portent quelque chose.
- **Des cadres peints au `Canvas`** — un cadre qui est du texte s'aligne tout seul et ne se mesure pas. Ce qui reste au `Canvas` est ce qui ne peut pas être une cellule : les marques du tour, qui se posent sur des groupes de largeur arbitraire.
- **Un shader CRT en AGSL** — `RuntimeShader` est API 33+ alors que `minSdk` vaut 26, ce qui demanderait une porte, et `universel` interdit qu'elle soit silencieuse. Surtout, un shader qui soustrait du rgb déplace la lecture de la teinte partout où il passe, donc il ne pourrait jamais couvrir la surface de marquage.
- **Un halo de mélodie en alpha** — il laisserait transparaître le rouge de l'apprenant sous le modèle, donc le recouvrement cesserait d'être total au moment même où il doit l'être. Le halo est opaque, mélangé vers le fond.
- **La police par le fournisseur Google Fonts** — dépendance aux services Play, interdite par `fdroid`.
- **Un fond de parchemin chaud** en registre clair — il est dans la famille de l'ambre, donc l'alarme deviendrait une nuance du fond au lieu d'une intrusion.

## Ce qui reste ouvert

- **La liste exacte des glyphes à dessiner** — accents, cadres, descendantes.
- **La séparation des quatre crans en registre clair.** La gamme des couleurs disponibles n'est pas la même en sombre qu'en clair : c'est une mesure à refaire, pas un réglage à recopier.
- **Le débordement d'un interligne sur l'autre.** À interligne 1, la vaguelette d'une ligne descend deux pixels plus bas que le sommet de la ligne suivante. Se règle en ajustant l'air, à faire.
- **Les sons.** Rien n'est décidé. Le canal principal de l'app *est* l'audio — la voix du modèle, et le micro ouvert pendant la réponse — donc une ambiance continue entre en concurrence avec ce qu'on écoute et se fait capter par le micro. Un babil de texte est exclu d'avance, le tour de l'IA étant réellement parlé.
- **La charpente persistante.** Aujourd'hui `MainActivity.Root` porte une rangée de boutons en haut qui dit à la fois où on est et où aller. Le registre a deux objets distincts : une **ligne d'état** en haut, une **barre d'actions** en bas. Les séparer mettrait les actions là où est le pouce, et donnerait un endroit à ce que `reference.md` exige à plusieurs reprises — qu'une chose indisponible **porte sa raison**. Coût : deux lignes de grille en permanence.
- **Le texte de l'IA qui apparaît caractère par caractère.** Très juste dans le registre, mais les tours de l'IA sont brouillés (« Le texte de l'IA ») et l'audio est le canal principal : un défilement qui devance ou traîne derrière la voix serait pire que pas de défilement.
- **Le lecteur d'écran — hors v1, et dit plutôt que tu.** `ui/MarkedTurn.kt` est un `Canvas`, donc toute la sémantique du marquage est invisible à TalkBack : les crans, les empans et les échelles existent en mémoire et rien ne les expose. Ce n'est pas une porte fermée — un mot porte déjà son cran et sa feuille, il n'y a rien à mesurer de plus, seulement à décrire —, c'est un travail qui ne se fait pas maintenant. Pour une app destinée à un dépôt public, le silence vaudrait décision par défaut.
- **Ce que l'écran fait pendant l'attente.** Onze secondes de médiane jusqu'au premier son, et rien n'est spécifié de ce qu'on regarde pendant. La piste : **les notes du passage précédent s'y affichent**, par feuille et rangées par aptitude. Le calendrier tombe juste et ne coûte rien — la note d'un passage se décide à sa fermeture, c'est-à-dire au moment où le tour suivant part, donc elle est prête pile quand l'attente commence. À tenir en dessinant : la **marque** reste le repère stable et la note en est une lecture réglée (`activity-model.md`), donc la note remplit un blanc, elle ne prend pas le premier plan.
- **La rencontre de personnage** (`../../NOTES.md`) — ce qui fait qu'on rencontre quelqu'un plutôt qu'on lance un thème. C'est une grammaire d'interaction, pas un habillage, et rien ici ne la décide.
- **La pile de navigation.** Les quatre écrans actuels sont un interrupteur à quatre positions dont aucun ne mène à un autre ; le modèle d'activité (`activity-model.md`) amènera des écrans qui descendent les uns dans les autres. La charpente ne doit pas bloquer ça.

## L'ordre de travail

1. Le thème, avec la grille et les rythmes du marquage dedans.
2. La police embarquée, et l'échelle entière calculée depuis la densité.
3. La palette des deux registres, en remplaçant la rampe continue de `ui/MarkingColors.kt` — et, dans la même passe, la palette de rechange du daltonisme, qui coûte une heure ici et tout le doc plus tard.
4. Le tour marqué : la bande de mélodie et son recouvrement, puis les marques et leur air.
5. Les glyphes ajoutés à la police, et les cadres et listes en caractères.
6. Les transitions en coupure plutôt qu'en fondu.
