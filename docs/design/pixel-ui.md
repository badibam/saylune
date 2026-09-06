# L'interface — pixel doux

Ce que l'app doit avoir l'air d'être, et ce que ça décide dans le code. Une seule pièce est écrite, la police et ses glyphes (`../../font/`) ; tout le reste attend que la boucle de conversation soit finie (`../../TODO.md`, chantier 0), et ce doc existe pour que le jour où l'esthétique prend soin, les décisions qui coûtent cher rétroactivement soient déjà prises.

Les valeurs chiffrées qui suivent ont été réglées à l'œil sur un banc d'essai qui dessine un écran complet à la résolution réelle d'un téléphone, police embarquée et palette réglable : **`../../bench/pixel-ui.html`**, à ouvrir dans un navigateur. Il reste l'instrument pour tout ce qui se juge en regardant plutôt qu'en raisonnant — la rechange du daltonisme l'attend. Il est versionné avec le reste du banc : il est écrit à la main et ne se régénère pas, donc le perdre serait perdre la source des couleurs.

## Ce qui borne tout le reste : la couleur porte une mesure

Sur le tour de l'apprenant, la teinte d'une lettre **est** l'écart au modèle, le filet sous une syllabe est l'accent, la bande au-dessus de la ligne est la mélodie, la vaguelette est la correction et les crochets sont la pertinence (`../reference.md`). Ce ne sont pas des couleurs de décor : ce sont les seules sorties visibles de l'analyse, et `reference.md` interdit qu'un réglage ou un habillage les déplace.

Le registre couvre **tout** l'app, cette surface comprise — même police, même grille, même palette, même fond. Ce qu'il n'y fait pas, c'est dépenser le canal de la teinte pour du décor, et poser un filtre par-dessus. Partout ailleurs il fait ce qu'il veut.

## Le registre : la console, en doux

**Pixel art de console, chaleureux, à fond prune.** La référence est le sous-écran d'une Zelda 8 ou 16 bits — un panneau dense, encadré d'une bordure épaisse à deux tons, sur une grille de tuiles.

Ce que ça n'est pas, et qui se propose tout seul si on ne le dit pas : **ce n'est pas un lieu**. Pas de carte, pas de monde, pas de personnage qu'on croise dans un décor. L'app reste une app ; c'est son ambiance qui vient de là, pas sa grammaire d'interaction. Le registre s'obtient avec une police, une palette et des cadres, **sans un seul fichier image obligatoire**.

Le pixel doit être **doux, et cette douceur est peinte, jamais filtrée** : tramage tendre, crans voisins peu contrastés, dégradés cuits au pixel près, halos dessinés à la main. Aucun flou, aucune transparence appliquée par-dessus une surface qui porte une mesure — un traitement qui touche au rgb déplacerait la lecture de la teinte.

## La police

**Mono10**, en licence SIL OFL 1.1 **sans nom réservé déclaré**, modifiée, renommée **Speakup Tile** et **embarquée dans `res/font/`**. Jamais par le fournisseur Google Fonts téléchargeable, adossé aux services Google Play, donc une dépendance propriétaire que la facette `fdroid` interdit.

Elle est **écrite et non plus empruntée** : sa source de vérité est un jeu de cartes de pixels en texte, dans `../../font/`, que `build.py` compile en TTF ; le TTF se commite à côté, aucun outil n'étant appelé au build. `font/README.md` dit la boîte, les scripts et la carte de la zone privée.

Cadratin de 1024 unités, une unité de dessin valant 64 unités, avance de 11 pixels. **La boîte fait 11 × 15 pixels** : les capitales tiennent les rangées 0 à 9, les accents les rangées 10 et 11, les descendantes les rangées −1 et −2, et une rangée sépare deux lignes. Hauteur de capitale 10, hauteur d'x 8. La colonne 10 est l'interlettre et reste vide, sauf pour les pièces de cadre, qu'une fente d'un pixel trahirait. La graisse *Regular* a un trait de **2 pixels**, la *Thin* de 1.

**La boîte de Mono10 faisait 11 × 11 et ne pouvait pas porter le français.** Elle a grandi vers le haut et vers le bas seulement, donc aucun glyphe d'origine n'a bougé et l'avance n'a pas changé — le nombre de colonnes, la tuile de cadre et l'ancrage horizontal du marquage sont intacts. Ce que ça coûte est le pas de ligne, 15 au lieu de 11.

**Carrée et charnue, c'est-à-dire une police de tuiles et non de terminal.** Les deux familles sont à largeur fixe et se confondent facilement : une police de terminal est haute, étroite et à trait fin, parce qu'elle sert à empiler du code ; une police de tuiles est carrée et épaisse, parce qu'elle sert à parler dans une boîte de dialogue. Le marquage a besoin de la seconde — la teinte n'a de la matière à occuper que si le glyphe est dense.

**Les deux graisses sont offertes en préférence utilisateur.** Vérifié au banc : la rampe reste lisible en *Thin*.

### Ce qu'on lui a ajouté

214 glyphes par-dessus les 110 de Mono10, dont 5 redessinés.

- **Les accents**, 44, plus `« » … × ½ ⅓ ¼ ° — –`. Quatre marques dessinées une fois par graisse et posées sur les lettres de base : rangées 9 et 8 d'une minuscule, 11 et 10 d'une capitale. **Une capitale accentuée garde donc sa pleine hauteur** — le doc annonçait un raccourcissement d'un pixel, la boîte haute le rend inutile. La cédille tient dans les rangées qu'ont ouvertes les descendantes, donc le `ç` garde un `c` entier.
- **Les descendantes.** `g j p q y` étaient tassés dans la hauteur d'x et le `g` s'y lisait `9`. Aucun redessin à l'intérieur de la hauteur d'x ne le répare : à trait de 2 pixels, un bol fermé prend quatre rangées et la queue les quatre qui restent, et un bol au-dessus d'une queue *est* la silhouette d'un `9`. Ils descendent donc de deux rangées, bol de six rangées et crochet plat, trait plein partout.
- **Les cadres**, 16 pièces dans la zone à usage privé à partir de U+E000, jamais sur les codets Unicode de dessin de cadre, pour que rien ne casse si la police de base change.
- **Les meubles**, 25 : la famille du transport entière — le triangle de l'écoute, la pause, l'arrêt, le disque d'enregistrement —, quatre blocs de jauge pour les deux décomptes, quatre flèches, une coche, une croix, un cœur pour les vies, le micro, la loupe, l'œil, le cadenas, l'histogramme, les curseurs des leviers, le retour, la flèche circulaire, et un point plein et un point creux pour lire la position d'un levier à marches.
- **Les phonèmes**, 14. L'analyse rend son inventaire en API (`../reference.md`) : trente-huit symboles sur trente-sept caractères, dont vingt-deux étaient déjà des lettres ordinaires. Trois des autres sont une lettre tournée ou en miroir, sept une lettre plus une barre ou un crochet, quatre des dessins neufs. Ce partage est ce qui rend la seconde graisse gratuite : un glyphe composé hérite du trait de la lettre dont il est fait.
- **Les lettres brouillées**, 107 — voir « Le texte de l'IA ».

**Les deux ligatures d'affriquée sont volontairement absentes.** Dans une police à chasse fixe, une ligature doit tenir dans une cellule de toute façon — y écraser un `d` et un `ʒ` à trait de 2 pixels est donc strictement pire que d'en prendre deux. L'écran écrit `dʒ` et `tʃ`, qui est de l'API tout aussi standard et la notation la plus courante des dictionnaires, et les diphtongues occupent déjà deux cellules, donc `ui/AnalysisReadout.kt` aligne des symboles de largeur variable de toute manière. C'est une correspondance à l'affichage : la donnée garde `ʤ` et `ʧ`, que le modèle émet et que tous les tours du disque portent. Un seul glyphe en tombe, l'ezh.

**Deux paires de phonèmes ne se distinguent pas, et l'inventaire les sépare quand même.** `ə` diffère de `a` d'**un pixel** et `ɑ` de `o` de **deux** — le `a` de Mono10 *est* un `e` retourné, son `o` *est* le bol du `d`, donc un schwa fidèle et un ɑ fidèle sont le glyphe d'à côté, et aucun dessin ne les sépare à dix pixels de large. Mais les quatre lettres nues `a e o ɔ` n'apparaissent **jamais seules** : uniquement en tête de diphtongue, toujours suivies d'un `ɪ` ou d'un `ʊ`. Et `ə` et `ɑ`, qui apparaissent seuls, n'ont pas de diphtongue. Les deux membres de chaque paire ne tombent donc jamais à la même position, et les distordre coûterait la fidélité pour réparer une confusion qui ne peut pas se produire. **Ce que ça laisse dû** : ça cesse d'être vrai le jour où un phonème s'écrit à l'intérieur d'une phrase ordinaire, où un `ə` au milieu de mots se lira `a`. Rien ne le fait aujourd'hui — la notification de faute de son ne nomme rien.

**Il n'en manque plus** (relevé et dessiné le 2026-09-06). Les notes du passage sont un **histogramme**, jeter la prise une **flèche circulaire**. Trois choix de fond derrière le lot :

- **Un glyphe ne remplace un mot que là où la place manque.** La rangée de commandes est la seule qui manque de place — quinze colonnes sur vingt-huit, donc elle est en glyphes ; `MON TOUR`, `PAUSE`, `ENVOYER` restent des mots, parce qu'ils commandent sous pression et qu'`ENVOYER` dépense une tentative. Les glyphes de la capture — micro, pause, arrêt, flèche circulaire — servent donc les états et l'écran des leviers, pas les boutons du bas.
- **Une famille se dessine entière.** Le triangle et le disque appelaient la pause et l'arrêt, `×½` appelait `×¼` : un membre manquant se remarque, et il coûte moins de le dessiner maintenant que de le découvrir absent devant un écran.
- **La prose de l'IA a sa liste fermée** — `— – °` et les huit lettres à accent aigu, que la machinerie des accents composait déjà pour rien. Rien derrière : un caractère absent rend un carré vide, ce qui est bruyant et vrai, là où un repli silencieux masquerait le trou (`universel`, no-fallback). Le brouillage, lui, a un repli, mais **pour une lettre seulement** — la ponctuation reste claire, étant le support qui fait qu'un panneau brouillé se lit comme une phrase.

## La taille des lettres

**Facteur 3.** Chaque pixel de dessin devient un carré de 3 × 3 pixels d'écran, ce qui donne une capitale de **10 dp** — la taille du texte ordinaire d'une app Android — et **32 colonnes** sur un écran de 1080.

Le facteur est un **entier**, et c'est la seule contrainte dure : à 3,5 certains pixels de dessin feraient 3 pixels de large et d'autres 4, les traits sortiraient d'épaisseurs inégales et l'effet s'effondrerait. Il se calcule donc depuis la densité de l'écran, et si l'utilisateur peut agrandir, ce sera par crans entiers.

Il n'y a **pas de critère calculable** derrière sa valeur : c'est l'arbitrage entre la lisibilité des lettres et le nombre de colonnes, et il se tranche en regardant. Le repère utile est la hauteur de capitale en dp, pas le facteur.

**Le nombre de colonnes se compte en dp et pas en pixels** — recompté le 2026-09-06, et ce doc disait l'inverse. Le facteur suit la densité, donc une cellule fait toujours à peu près **11 dp**, et le nombre de colonnes est la largeur de l'écran en dp divisée par 11 : **29** sur un petit téléphone de 320 dp, **32** sur un ordinaire de 360, **37** sur un grand de 411. Un écran de 720 pixels n'en donne pas moitié moins qu'un 1440 : sa densité est moitié moindre elle aussi, il prend le facteur 2, et il rend les mêmes 32 colonnes avec les mêmes lettres de 10 dp. Pour que 720 pixels ne fassent vraiment que 21 colonnes, il faudrait un écran de 240 dp de large, plus étroit qu'aucun téléphone.

**Le pire écran est donc celui dont la densité tombe mal, pas le plus étroit.** À 420 dpi la densité vaut 2,625 ; le facteur devant être entier il monte à 3, les lettres sortent à 11,4 dp au lieu de 10 et la cellule à 12,6 dp — **28 colonnes** sur 360 dp. C'est ce nombre qui commande les mises en page. Il est **calculé et non mesuré** : rien n'écrit encore le facteur, le thème étant l'étape 1 de l'ordre de travail, et le vérifier demande de poser la police sur un vrai téléphone.

## La grille

**La grille est horizontale.** Ce qu'elle protège est l'ancrage du marquage aux caractères : le filet couvre un nombre entier de cellules, les coins des sons qu'aucune lettre ne porte tombent sur des bornes exactes, et une classe entière d'arrondis disparaît de `ui/MarkedTurn.kt`, qui mesure aujourd'hui au sous-pixel.

Ce que l'ancrage entier demande n'est d'ailleurs pas le monospace mais la **police pixel à échelle entière** : dans une police pixel les avances sont des nombres entiers de pixels par construction, et multipliées par un facteur entier elles le restent. Le monospace vient du registre, pas du marquage.

**En vertical, rien n'est ancré.** La hauteur à laquelle une ligne de texte se pose dans sa boîte est libre, du moment qu'elle est un nombre entier de pixels de dessin. C'est ce qui permet de donner de l'air au marquage sans rien casser.

Les cadres, les panneaux et les marges se calent sur la cellule dans les deux directions — c'est ce qui les fait composer entre eux.

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

### La règle du marquage

**Une lettre marquée doit être assez loin d'une lettre nue, et cette distance se paie en clarté, en chroma, ou dans les deux.** Elle se mesure en distance perceptuelle OKLab sur le **premier cran** — le cas le plus difficile, celui qui frôle l'encre neutre. Elle vaut 0,202 en registre sombre et 0,158 en clair.

C'est la seule formulation qui vaut dans les deux registres. En nuit, la distance est portée surtout par la clarté : une lettre marquée **s'allume**. En pâle, elle est portée par la chroma, l'encre neutre étant à peu près incolore : une lettre marquée **se colore**.

**La chroma de la rampe reste haute** — c'est elle qui porte la lisibilité, et l'adoucir la coûterait. La douceur du registre vient du fond, des cadres et des formes, jamais d'affamer la rampe.

### Le daltonisme

**Une palette de rechange, en préférence utilisateur.** Le registre garde ses couleurs pour tout le monde ; qui ne les sépare pas en change. Corriger la palette de base à la place taxerait le défaut d'une esthétique choisie pour une perception que la plupart n'ont pas.

**Le cas qui l'exige est précis, et c'est le pire du doc.** *Juste* et *à côté* sont **la même forme** — des crochets — et **deux verdicts opposés**, séparés par la seule couleur, verte contre rouge. En deutéranopie les deux convergent : la marque d'une réussite devient celle d'une faute. Le doc interdit ailleurs qu'une marque absente se confonde avec une approbation ; ici c'est la présence qui se confond avec son contraire. La pastille du suivi a le même défaut sur quatre valeurs, et la rampe à quatre crans peu contrastés y ajoute la sienne.

**Ce n'est donc pas une permutation de teintes.** La palette de rechange doit satisfaire **la même règle de marquage** — la distance OKLab sur le premier cran —, dans les deux registres, et trouver pour les crochets deux teintes qui restent séparées sous la perception visée. Elle se règle au banc comme le reste de la palette. **Ce qu'elle couvre n'est pas tranché.** Les confusions rouge-vert — deutéranopie, protanopie — sont ce qui casse les crochets, donc elles sont le cas à traiter d'abord. La tritanopie toucherait le bleu du contour de mélodie, et la couvrir ou la déclarer hors périmètre se décide en écrivant la palette.

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

**Dessinés par nous, dans la police.** Un cadre dans une grille de caractères *est* du texte : on pose un coin, des bords, un autre coin, et il s'aligne tout seul pour le prix d'une chaîne. Unicode ne porte des coins arrondis qu'en trait fin, ce qui interdit d'y trouver un cadre à la fois arrondi et charnu ; on les dessine donc, et **la tuile de 11 pixels porte une bordure de 4 pixels d'épaisseur, à coins de rayon 4** — les valeurs du banc, reprises telles quelles.

**Deux tons, par superposition de deux couches de texte** exactement calées, dans deux teintes de décor — les deux pixels extérieurs de la bordure dans le ton clair, les deux intérieurs dans le sombre. C'est ce qui donne le relief doux d'une bordure de console plutôt qu'un bandeau plat. Les deux jeux se dessinent **d'un seul travail** : le second est le complément du premier dans une silhouette qu'on trace de toute façon, alors que rajouter un ton après coup obligerait à redécouper chaque glyphe.

**Un cadre dit « objet ».** Un menu, une boîte de dialogue, une carte d'activité, l'élément sélectionné. Jamais l'écran entier — deux colonnes de chaque côté sur les 32 disponibles, payées le plus cher sur le plus petit écran — et jamais le fil de conversation ni le tour marqué, qui ont besoin de toute la largeur. La règle se vérifie en écrivant un écran : si une chose est encadrée, on doit pouvoir dire ce qu'on fait avec ; sinon c'est un aplat.

**Un panneau peut n'être qu'un aplat**, un pavé de fond plus clair calé sur les cellules. C'est la forme la plus douce dont on dispose, puisqu'un aplat n'a pas d'arête à adoucir.

## Le texte de l'IA

**Le tour de l'IA s'affiche brouillé par défaut.** L'oreille est le canal principal, et un texte lisible préempterait l'écoute — c'est le flou déjà prévu (« les tours de l'IA sont floutables ») qui devient un brouillage.

**Le brouillage est une police, pas un effet.** Chaque lettre a sa jumelle brouillée, au codet de la lettre **plus `0xE100`** : la table de l'app est une addition, et elle n'a jamais à décider ce qu'est une lettre — un caractère se brouille si son codet décalé existe dans la police.

**Une lettre sans jumelle tombe sur une jumelle générique**, à `U+E100` — le décalage lui-même, qui est le brouillage d'aucun caractère. Elle ne sert jamais tant que la liste est à jour ; c'est le filet de la lettre qu'on ajoutera un jour en oubliant de la brouiller. La **ponctuation n'a pas de repli** et reste claire : c'est elle, avec les retours à la ligne, qui fait qu'un panneau brouillé se lit comme une phrase.

**Le brouillage recouvre l'espace de chaque lettre de carrés de 2 pixels disposés au hasard** — l'épaisseur du trait, sur la même grille que tout le reste. Ce qui survit est le support : la hauteur et la largeur de la lettre — les ascendantes restent hautes, les descendantes basses —, la longueur des mots, la ponctuation et les retours à la ligne. Le panneau se lit comme du texte sans qu'aucune lettre ne se lise : on sent qu'il y avait une phrase, on ne déchiffre rien. Les lettres seules se couvrent, les espaces et la ponctuation ne bougent pas.

**Le brouillage est fixe** — la même lettre se couvre toujours pareil, donc le même texte se brouille pareil partout et tout le temps, comme une marque. Le tirage vient d'une graine qui est la lettre elle-même, et les carrés font 2 pixels **dans les deux graisses** : couvrir la *Thin* de pixels isolés a été essayé et rend un grésillement au lieu d'une phrase, la *Thin* recevant simplement moins de carrés puisqu'elle a moins d'encre.

**Ce n'est pas une préférence, c'est une position du levier `ai-turn.display`** (`activity-model.md`), dont c'est le défaut. Elle tombe entre le texte net et *seulement qui parle*, le support laissé étant une aide de moins que le texte et une de plus que rien. En conversation libre l'apprenant la déplace lui-même et elle reste où il l'a laissée ; une activité qui veut imposer le net, ou n'en rien montrer, la pose comme n'importe quel autre levier. Une préférence vaudrait partout et aucun défi ne pourrait la reprendre.

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

Réglages retenus : **5 pixels d'air** entre les lettres et leur enceinte, **1 pixel** avant les marques, **un interligne** d'une ligne de grille, et le texte posé à **8 pixels** dans sa ligne. Ces cinq pixels se comptent désormais **sous les descendantes** et non sous la ligne de base : la pile entière descend de deux, et les accents demandent deux pixels de plus au-dessus, avant le bord haut de l'enceinte. La ligne grandit donc de 4 pixels et rien ne se serre. Le partage n'est pas symétrique : au-dessus des lettres il n'y a que le bord de l'enceinte, en dessous il y a l'enceinte, le filet et la vaguelette.

**Les lettres se peignent par-dessus les crochets.** Ordre complet : la mélodie, la pertinence, les lettres — fragments écartés et colonne de points compris, qui vivent dans les cellules —, la vaguelette, le filet.

### Ce qui se montre, et quand

**Les marques n'arrivent pas d'un coup, et ce n'est pas un choix.** Les marquages jugés tombent au retour de l'appel, les marques du son à la fin de l'analyse, deux à trois secondes plus tard (`activity-model.md`, « Le déroulé d'un passage »). La révélation est donc canal par canal par construction — et ça tombe bien, les jugées étant à l'écran avant que l'IA parle.

**Les marques des mots restent une fois la porte des mots passée, et c'est le défaut** (tranché le 2026-09-06). Un passage qui passe porte quand même des marques : la porte lit une note à la barre, pas l'absence de marque. La ligne devient dense quand les marques du son arrivent par-dessus, et c'est le prix accepté — un `plat` sur un passage qui passe reste une chose vraie qu'on a le droit de voir. Les retirer est une préférence utilisateur, d'affichage et jamais de mesure.

**Un menu de conversation dit quelles marques s'affichent.** La densité mesurée — la majorité des mots portent quelque chose — fait qu'un apprenant qui travaille sa mélodie voudra éteindre le reste. Éteindre un canal n'éteint que son **affichage**, jamais sa mesure : la note ne bouge pas, et rouvrir le canal remontre ce qui était là.

**Huit canaux, un par marque** : la teinte des lettres, le filet d'accent, la bande de mélodie, la vaguelette, les crochets, les points de pause, la pastille du suivi et les chevrons du débit. Plus le réglage ci-dessus, les marques des mots une fois la porte passée.

**Un canal éteint retire son emplacement au lieu de le vider.** Les deux derniers vivent sur la ligne qui nomme le tour, où un emplacement vide dit *non mesuré* : le laisser vide ferait donc mentir la ligne. La ligne se resserre — pas d'emplacement, pas de canal ; emplacement vide, non mesuré.

**Les fragments écartés ne sont pas un canal.** `[um]` est du texte et non une marque peinte : l'éteindre changerait la chaîne affichée, donc l'ancrage horizontal de tout le reste.

**Ce menu est du côté de l'apprenant, et n'est jamais un levier.** L'invariant l'exige — une marque dont la présence dépendrait du réglage du jour ne transporte plus rien (`activity-model.md`). C'est l'inverse exact du brouillage du tour de l'IA, qui est bien un levier : là il s'agit d'une aide sur ce que l'IA dit, ici de la sortie d'une mesure. Une activité peut retirer une aide ; aucune ne cache une marque.

### La ligne qui nomme le tour

**Les deux marques qui valent pour le tour entier se posent au bout de sa ligne d'étiquette**, calées à droite : la pastille du suivi, puis le débit. Ça ne prend aucune colonne au texte et n'ajoute aucune ligne, l'étiquette existant déjà et sa moitié droite étant vide. En contrepartie, cette ligne devient structurelle : elle ne peut plus disparaître de la charpente.

- **La pastille est un disque de neuf pixels, halo d'un pixel compris** — le halo tient de la même recette que celui de la mélodie, mélangé vers le fond — et le tout occupe les onze pixels de la cellule.
- **Le débit s'écrit en caractères** — `>><<` `><` `=` `<->` `<-->` — centré dans un champ de quatre colonnes, pour que la pastille ne bouge pas quand le cran change.
- **Un emplacement vide dit *non mesuré***, et non *rien à signaler* (`activity-model.md`).

Recompté le 2026-09-06 sur le pire écran, et le cas se détend : l'étiquette porte un nom de locuteur — `You` / `Speakup` aujourd'hui, le nom du personnage demain — donc `Le barman` 9, un blanc, la pastille 1, un blanc, le débit 4 font **quinze colonnes sur les vingt-huit** du pire écran. Il en reste treize de marge, là où le doc craignait de n'en avoir aucune sur seize.

**Quand un nom déborde quand même, le nom se tronque et jamais les marques.** Les marques portent une mesure ; l'étiquette porte une identité qu'on connaît déjà, deux ou trois locuteurs alternant dans une conversation. Une définition déclare donc un **nom court** par personnage, comme elle déclare un titre court, et l'app tronque de toute façon — l'ellipse est dans la police. C'est là que le tour marqué se vérifie, pas seulement à trente-deux colonnes.

## Le doigt

**Tout est directement tactile.** L'élément qu'on touche est celui qui se sélectionne ; le curseur existe toujours mais il marque ce qu'on vient de toucher au lieu de servir à naviguer.

**La rangée de commandes vit dans le fil, sous chaque passage** — pas fixée en bas de l'écran. Six entrées, au padding égal du texte de l'IA, un filet de grille tout autour, et **des glyphes dès que possible** (arrêté le 2026-09-06) :

| entrée | largeur |
|---|---|
| le petit bouton, le **micro**, dans un cadre | 3 |
| l'écoute, le triangle | 1 |
| le côté que l'écoute atteint, `ME` / `AI`, une seule étiquette qui bascule | 2 |
| la vitesse, `×1` `×½` `×⅓`, une seule étiquette qui cycle | 2 |
| les notes du passage | 1 |
| l'analyse des sons, la loupe | 1 |

Dix colonnes plus cinq séparations font **quinze sur les vingt-huit** du pire écran. **Le petit bouton garde ses trois colonnes en devenant un glyphe** (tranché le 2026-09-06) : un micro dans un cadre, là où les cinq autres entrées sont nues. Ce que le cadre achète est de le distinguer d'une rangée où tout le reste écoute ou règle — il est le seul à ouvrir le micro. Et ce que le glyphe évite est une largeur qui dépend de l'état : une étiquette qui aurait dit la porte du moment ferait 3 colonnes en `DIS` et 9 en `REFORMULE`, et surtout elle **décalerait les cinq autres entrées** à chaque fois qu'une porte se ferme. Une rangée dont la géométrie dépend de l'état est ce que la grille existe pour empêcher. La flèche circulaire n'était pas libre : elle recommence l'enregistrement en capture à la main. Le petit bouton n'apparaît que sur le passage ouvert, les tentatives s'arrêtant à la clôture (`activity-model.md`) ; les cinq autres entrées valent sur tout passage qui porte un enregistrement.

**Le petit bouton se comporte comme le gros** : un appui lance, il se montre actif, et ce qui suit — la pause, l'envoi — est en bas. Un seul comportement à apprendre pour les deux, ce qui est tout l'intérêt d'avoir mis les trois positions de capture au même geste.

**Les notes et les sons sont deux entrées et non une**, les deux ne parlant pas de la même chose : l'une rend le bilan du passage, feuille par feuille, l'autre descend d'un cran dans l'inventaire son par son que `ui/AnalysisReadout.kt` dessine déjà.

**Le bas de l'écran fait quatre lignes de grille.** Sur trois, **trois boutons** — le **gros bouton** `MON TOUR` / `MY TURN`, `PAUSE`, et **`ENVOYER`**. Sur une, la **barre d'actions**. Leur partage en largeur reste à régler au banc : le gros bouton tenait les deux tiers quand ils n'étaient que deux.

**Chacun dit une chose et une seule, et disparaît là où il n'a pas de travail.** C'est ce qui remplace un objet unique qui aurait changé de sens en route — `MON TOUR` devenant pause pendant qu'on parle, puis grisé selon la position de capture, ferait trois personnalités sur un bouton, et un changement visible ne dispense pas de l'apprendre. `PAUSE` n'existe donc qu'en capture à la main, la seule position qui en a une (`activity-model.md`) ; son absence est la forme juste à l'écran, et la phrase du levier reste la forme juste au moment où on choisit la position, avant de jouer.

**Tout s'ouvre d'un appui**, aux trois positions de capture, la première ayant cessé de se tenir au doigt maintenu (`activity-model.md`, tranché le 2026-09-06). C'est ce qui permet à `MON TOUR` d'exister partout du même geste : sans ça, un bouton qui sert dans deux régimes de capture aurait deux comportements que rien à l'écran n'annonce.

**Le gros bouton dit un tour de parole et pas une page suivante** : il ferme le passage précédent et ouvre le mien, ce qui est exactement ce qui se passe. Il forme un couple lisible avec le petit — le petit reprend la même phrase, `MON TOUR` en dit une neuve. `CONTINUER` a été écarté, qui laisserait croire qu'on saute un tour.

**Grisé, il ne dit rien de plus, et c'est la ligne d'état qui porte la raison** (tranché le 2026-09-06). Il portait *reformule d'abord* ; ça contredisait la règle plus forte et plus répétée d'un cran au-dessus — **chacun dit une chose et une seule** —, celle-là même qui a sorti `PAUSE` du gros bouton. Un `MON TOUR` grisé qui affiche autre chose est une seconde personnalité. La règle du projet, qu'une chose indisponible porte sa raison, reste tenue : la ligne d'état la porte, et sur un écran où le gros bouton fait trois lignes en bas et la ligne d'état deux en haut, les deux se voient ensemble. **Le partage vaut pour tous les boutons** : ce qu'on peut faire est dit par le grisage, ce qu'il faut faire par la ligne d'état — la porte du moment, *reformule-la* ou *redis-la*, et le compte restant.

**`ENVOYER` existe aux trois positions de capture**, et pas seulement aux deux à envoi manuel : le doc du modèle d'activité dit de la troisième que « le clic reste le geste normal », le silence n'y étant que le filet qui rattrape un tour que personne n'envoie. Ce qui change d'une position à l'autre est ce qui **arme** le micro, jamais ce qui envoie.

**Le bas commande tout ce qui enregistre, quelle que soit son origine.** Le gros bouton lance un tour neuf, le petit bouton d'un passage lance une reprise, et dans les deux cas ce sont les mêmes `PAUSE` et `ENVOYER` qui suivent. Les redoubler dans la rangée de commandes tiendrait — deux glyphes de plus font dix-neuf colonnes sur vingt-huit — et ce n'est pas la place qui l'écarte : ce serait deux `ENVOYER` faisant le même travail à deux endroits, celui qu'on presse dépendant de ce qu'on a lancé. C'est aussi ce que le partage de la charpente dit déjà, la barre d'actions portant les points d'entrée constants et ce qui est au-dessus l'action du moment — un enregistrement en cours **est** l'action du moment.

**Ce qui rend ce partage sûr est que la ligne d'état nomme ce qui tourne** : pas *enregistrement en cours* mais *tour en cours*, *redite en cours*, *reformulation en pause*. Sans ce nom, deux boutons partagés seraient ambigus ; avec lui, il n'y a jamais qu'un enregistrement et l'écran dit lequel.

**Les états des boutons en découlent, et ils tiennent en quatre lignes.**

| quand | ce que portent les boutons |
|---|---|
| un enregistrement tourne, d'où qu'il vienne | `PAUSE` et `ENVOYER` vivants ; `MON TOUR` grisé — on ne commence pas un tour neuf en parlant |
| rien n'enregistre | `PAUSE` grisé ; `ENVOYER` absent, faute de prise à envoyer |
| une reprise tourne | le petit bouton du passage ouvert se montre actif ; les autres passages n'en ont pas |
| une reprise attend d'être décidée | `MON TOUR` et le petit bouton vivants tous les deux — sauf en « attend », où `MON TOUR` reste grisé jusqu'à l'épuisement des tentatives (`activity-model.md`) |

**Un dessin fin n'oblige pas à une cible tactile fine.** La barre d'actions se dessine sur une ligne et sa zone tactile déborde vers le haut sur trois ; un glyphe d'une colonne reçoit la même marge invisible. C'est ce qui réconcilie la recommandation des 48 dp avec une charpente qui n'en dépense qu'une.

Conséquence mécanique, pas un choix : **une entrée touchable occupe environ trois lignes de grille**, remplies ou vides. La recommandation Android est une cible de 48 dp et une ligne fait ici environ 11 dp. Ce n'est pas propre au registre — dans n'importe quelle app les lignes de liste sont rembourrées pour cette raison ; la seule différence est qu'ici le rembourrage est visible, en cellules vides.

## La charpente

Deux objets distincts remplacent la rangée de boutons de `MainActivity.Root`, et une règle les partage : **en haut ce qui est vrai, en bas ce qu'on peut faire** (arrêté le 2026-09-06). Elle tranche les cas futurs sans se rediscuter, et elle met les gestes là où est le pouce.

**En haut, deux lignes.**

La première est la **ligne d'état** : le **titre court** à gauche, et à droite, calés, les champs que le mode utilise — les **vies**, le **score**, la **note**. Chaque champ est simplement absent quand le mode ne s'en sert pas, dans un ordre fixe pour que rien ne bouge quand une valeur change. Aucun bouton : quatre points d'entrée y prendraient 192 dp sur les 360 de l'écran, et ils seraient loin du pouce.

- Le **titre court** est un champ déclaré par la définition, plafonné à **dix caractères** — ce que la ligne laisse quand la droite est pleine — et tronqué de toute façon à l'affichage. En conversation libre il n'y a pas de définition à nommer : il porte alors **qui on a en face**, ce qui est la porte d'entrée de la rencontre de personnage (`../../NOTES.md`) obtenue sans champ neuf.
- Les **vies** tiennent dans un champ de **trois colonnes, toujours** : jusqu'à trois, des cœurs, qu'on lit d'un coup sans compter ; au-delà, un cœur et le nombre, `♥15`. L'affichage plafonne à 99, et un défi qui donne plus de 99 vies n'a pas de vies, il a un décor.
- La **note** est celle de la séance en cours, qui n'a pas de formule à elle : c'est l'agrégation à plat des passages déjà clos (`activity-model.md`). Elle saute pendant les trois premiers, le dénominateur étant petit, et ce n'est pas un défaut à corriger — la masquer dirait *non mesuré*, ce qui serait faux.

La seconde est la **ligne d'état du tour**, dans son propre cadre, **toujours pleine et à jour**. Ce n'est pas une boîte d'alerte mais le narrateur du cycle : *à toi de parler*, *Speakup répond…*, *reformule-la — en cause : Grammar*, *écoute le modèle et redis*, *il te reste deux redites*. Elle occupe aussi les onze secondes d'attente sans rien coûter, et elle est l'endroit que `../reference.md` exige partout — celui où une chose indisponible porte sa raison.

**Quand une reprise attend, elle porte les deux issues et pas seulement l'obligation** — *reformule-la, ou lance un tour neuf ; il te reste deux reformulations*. C'est ce qui rend inutile la fenêtre qu'on aurait mise là : le micro ne s'arme pas tant que la reprise attend (`activity-model.md`), donc le flux s'arrête visiblement, cette ligne dit quoi et combien il reste, et les deux boutons portent les issues. Un pop-up ajouterait un second objet disant la même chose et un geste à écarter sur chaque passage marqué.

**En bas, la barre d'actions**, d'une seule ligne, en glyphes : l'**œil** (ce qui s'affiche), les **leviers ouverts** de cette séance, le **retour**. Les leviers ouverts sont **grisés en V1**, portant leur raison comme toute option éteinte (arrêté le 2026-09-06) : aucun écran de configuration n'existe encore, ni avant ni pendant, et l'entrée reste pour que la barre ne change pas de forme le jour où il existera. Elle porte les points d'entrée, qui sont constants pour un écran ; le gros bouton juste au-dessus porte l'action du moment, qui change d'un instant à l'autre. Les mélanger ferait bouger une entrée de place selon l'état du passage.

**Les leviers ouverts et les réglages de l'app sont deux choses.** Ce qui vaut pour cette séance — le tour de l'IA net ou brouillé, les écoutes, la capture — est ce que la définition laisse ouvert, et l'écran **se génère** : chaque position déclare déjà sa phrase lisible pour la notification d'arcade et l'écran d'avant-partie, donc zéro texte par activité, et un levier sans objet s'y affiche grisé en portant sa raison. Ce qui vaut pour toute l'app — la graisse, le registre, la palette de rechange, l'accent — vit ailleurs, et on n'y touche pas pendant une conversation.

**Trois notifications, et non deux.** Celle d'une **règle** qui change quelque chose est un reçu : elle s'affiche en pop-up, quelques secondes, et s'efface. Celle de la **porte des mots** et celle de la **porte du son** sont des états : elles vivent dans la ligne d'état du haut tant qu'elles sont vraies. La porte des mots **nomme la ou les aptitudes en cause**, ce que la porte du son ne fait pas — et ce n'est pas une incohérence, c'est le même raisonnement : la porte du son est câblée sur l'élocution et la fluidité seules, donc ce qu'elle nommerait serait une constante, quand la porte des mots a trois aptitudes derrière elle. Nommer **toutes** celles qui ferment, jamais la pire, respecte l'interdit du doc contre l'élection d'une marque.

## Ce qui se passe entre deux tours

**L'écran de bilan du passage**, et c'est un seul écran avec deux portes : poussé entre deux passages quand l'apprenant l'a réglé ainsi, ouvert à la demande par les notes de n'importe quel passage du fil. Un seul contenu à concevoir, une seule lecture à apprendre.

Il porte, **par aptitude, chaque feuille** : sa mesure brute, et sa lettre quand le mode utilise les notes — *2,3 demi-tons*, *47 sons sur 50*, *22 % plus lent*, *un mal formé*. La lettre de l'aptitude se pose sur sa ligne de titre, la note du passage au-dessus. Il défile, et son `OK` reste visible quel que soit le défilement.

Pour les trois feuilles à colonne — la grammaire, la pertinence, le remplissage — ce qui s'affiche est le **compte par cran** et non le chiffre de la feuille, qui est une moyenne entre 0 et 1 ne disant rien à personne. C'est la même donnée lue autrement, pas une seconde source.

**Il se dessine en barres horizontales, avec les blocs de jauge de la police.** Une colonne verticale n'est pas faisable : la jauge pave parce qu'elle occupe les onze colonnes, gouttière comprise, et il n'y a pas d'équivalent vertical — la boîte fait quatorze rangées et le pas de ligne quinze, donc une rangée vide sépare toujours deux cellules empilées et une colonne se casserait en tronçons. La part vide d'une barre est une **couleur** et non une forme : la jauge peinte dans l'encre atténuée, aucun creux à dessiner.

**Et cette barre reprend la rampe du marquage.** Le rouge qui a souligné `ne se dit pas` dans le tour compte ses mots ici. Ce n'est pas dépenser la teinte pour du décor — c'est la même mesure lue deux fois —, donc le bilan et le tour marqué parlent la même langue de couleur, sans rien à apprendre en passant de l'un à l'autre.

**Sa disposition tient dans vingt-huit colonnes**, le budget du pire écran (repris le 2026-09-06, sur une arithmétique corrigée — la première l'avait posé à vingt et une). Une ligne de feuille porte trois champs : le **nom** à gauche, la **mesure** calée à droite, la **lettre** en dernière colonne.

**La mesure est calée contre la lettre et court vers la gauche autant que le nom le permet.** C'est ce qui fait tenir `entre les lignes`, seize colonnes, sur la ligne dont le nom n'en fait que sept — et donc ce qui évite d'inventer des noms courts pour les six crans du suivi, qui gardent les mots du doc. Aucun nom de feuille n'a plus de plafond non plus : les douze colonnes qui en tenaient lieu étaient une conséquence du mauvais budget.

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

Cinq titres d'aptitude et onze feuilles font **seize lignes de contenu** : l'écran ne défile pas sur un écran haut, et défile en paysage. C'est exactement pourquoi l'`OK` vit dans la barre du bas et non dans le flux.

**Trois règles derrière les unités.** Une **fraction plutôt qu'un pourcentage** quand la feuille compte des éléments : `47/50` dit ce que `94 %` cache, que le tour faisait cinquante sons — sur un tour de trois, le pourcentage serait un mensonge de précision. Un **signe partout où la mesure est un écart**, `+` pour plus que le modèle et `-` pour moins, donc `-22 %` se lit *22 % plus lent*. Et **pas d'unité là où elle n'apprend rien** : les « points » de la précision sont une unité interne, l'écrire ne dit rien à personne. Le demi-ton s'écrit `½t`, ce que le glyphe de la fraction rend possible depuis le même jour.

**Aucune tendance dedans.** Une flèche *« +1 depuis le passage précédent »* comparerait deux lectures prises sous des règles qui n'ont aucune raison d'être les mêmes, et une note ne se lit jamais sans la combinaison qui l'a produite (`activity-model.md`). Il ne montre qu'une seule prise, la dernière du passage, comme le fil.

**Ce que le mode décide, ce sont les lettres ; ce que l'apprenant règle, c'est si l'écran est poussé.** Poussé par défaut, parce que la note d'un passage est prête pile quand l'attente commence et que remplir ces secondes vaut mieux que les regarder passer. Sans lettres — en conversation libre — l'écran existe quand même et porte les mesures brutes, qui sont des faits sur ce qu'on vient de dire et ne dépendent d'aucun réglage.

**Le pop-up d'une règle est un autre objet**, et il tombe après : la fenêtre se retire au doigt, puis viennent les phrases marquées *avant*, puis l'audio de l'IA, puis les *après*, puis le micro s'arme. Les deux ne se confondent pas — le bilan est disponible immédiatement à la fermeture, la scène attend la réponse du modèle.

## L'analyse des sons

Ce que l'écran montre est trouvé et vit dans `ui/AnalysisReadout.kt` — une ligne par caractère du tour, son ou pas, pour que la phrase se lise en descendant la colonne. Ce qui suit est sa disposition, arrêtée le 2026-09-06.

**Un son par ligne, et la ligne n'a qu'une seule cible : ouvrir.** Le rang porte les lettres sur 6, `modèle → toi` sur 8, les points sur 5 et la barre sur 6, soit vingt-sept des vingt-huit colonnes. Il en portait trois — écouter le modèle, s'écouter, déplier — et trois boutons dans les onze pixels d'une ligne se ratent. **Les deux écoutes descendent donc dans le bloc ouvert**, où la place existe. Ce qui se perd est un geste, deux appuis au lieu d'un pour entendre un son ; ce qui se garde est la raison d'être de l'écran, puisqu'à trois lignes par son on en voit dix et la phrase disparaît. Quand on veut écouter, on s'est de toute façon arrêté sur une ligne.

**Le bloc s'ouvre dans la liste et n'est jamais un pop-up.** Une seule ligne ouverte à la fois : on retape pour fermer, on tape ailleurs pour déplacer l'ouverture, donc il n'y a aucun geste d'écartement à inventer et rien ne recouvre la phrase qu'on est en train de lire. Le pop-up reste ce que ce doc en a fait ailleurs, le reçu d'une règle qui s'efface. **Quand une ligne s'ouvre, la liste remonte pour la caler en haut de l'écran** — sinon vingt lignes se déplient sous une ligne du bas et personne ne les voit.

**Deux natures d'écoute vivent dans le bloc, et le cadre les sépare.** Encadrés, `MODÈLE` et `TOI` jouent **cet enregistrement-ci**, à cet endroit de la phrase, et toute la ligne est la cible et pas la seule flèche. Nus, les symboles des deux répartitions jouent le **son de référence** enregistré en dur : ce que ce son est, pas ce qu'on en a fait. Sans cette différence visible on croit que toucher `d` rejoue son `d` à lui. Le cadre passe d'ailleurs la règle qui le gouverne — on peut dire ce qu'on en fait, on appuie et ça joue.

**Les symboles des répartitions ont leurs trois lignes**, comme tout ce qui se touche. Le bloc ouvert fait donc une vingtaine de lignes, et ça ne coûte rien : la hauteur n'est précieuse qu'au niveau replié, où on veut la phrase entière.

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

**La flèche, le triangle et la barre sont des meubles, pas de l'Unicode.** `arrow.right` à U+E018, `play` à U+E010, les quatre blocs de jauge à partir de U+E012 : la police n'a ni `→` ni `▶` ni les blocs de trame, et une chaîne qui les écrirait en Unicode rendrait du vide. La part non remplie de la barre est la jauge peinte dans l'encre atténuée, comme sur l'écran de bilan.

**Les trois gestes d'écoute de l'app ne se recouvrent pas** : toucher un mot du tour marqué le joue, l'écoute de la rangée de commandes joue la phrase entière, et ici on descend au son.

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

- **Les poids se règlent sur un polygone, curseurs concentriques** (arrêté le 2026-09-06) — un axe par enfant du nœud, le curseur court du centre au sommet, et le polygone que les curseurs dessinent se lit d'un coup. Un poids étant une **part entre frères** (`activity-model.md`), c'est littéralement le partage de la voix du nœud qu'on regarde. Le nombre de côtés vient du nœud et non de l'arbre, qui est à profondeur libre : cinq au premier niveau parce qu'il y a cinq aptitudes. Le reste de l'écran custom est hors périmètre.
- **La séparation des quatre crans en registre clair.** La gamme des couleurs disponibles n'est pas la même en sombre qu'en clair : c'est une mesure à refaire, pas un réglage à recopier.
- **Le débordement d'un interligne sur l'autre.** À interligne 1, la vaguelette d'une ligne descend deux pixels plus bas que le sommet de la ligne suivante. Se règle en ajustant l'air, à faire.
- **Les sons.** Rien n'est décidé. Le canal principal de l'app *est* l'audio — la voix du modèle, et le micro ouvert pendant la réponse — donc une ambiance continue entre en concurrence avec ce qu'on écoute et se fait capter par le micro. Un babil de texte est exclu d'avance, le tour de l'IA étant réellement parlé.
- **Le texte de l'IA qui apparaît caractère par caractère.** Très juste dans le registre, mais les tours de l'IA sont brouillés (« Le texte de l'IA ») et l'audio est le canal principal : un défilement qui devance ou traîne derrière la voix serait pire que pas de défilement.
- **Le lecteur d'écran — hors v1, et dit plutôt que tu.** `ui/MarkedTurn.kt` est un `Canvas`, donc toute la sémantique du marquage est invisible à TalkBack : les crans, les empans et les échelles existent en mémoire et rien ne les expose. Ce n'est pas une porte fermée — un mot porte déjà son cran et sa feuille, il n'y a rien à mesurer de plus, seulement à décrire —, c'est un travail qui ne se fait pas maintenant. Pour une app destinée à un dépôt public, le silence vaudrait décision par défaut.
- **La rencontre de personnage** (`../../NOTES.md`) — ce qui fait qu'on rencontre quelqu'un plutôt qu'on lance un thème. C'est une grammaire d'interaction, pas un habillage, et rien ici ne la décide.
- **La pile de navigation.** Les quatre écrans actuels sont un interrupteur à quatre positions dont aucun ne mène à un autre ; le modèle d'activité (`activity-model.md`) amènera des écrans qui descendent les uns dans les autres. La charpente ne doit pas bloquer ça. **Ce qui est arrêté est sa racine** (2026-09-06) : l'app s'ouvre sur un **écran-titre présentant les quatre modes** — Histoire, Défis, Arcade, Libre —, et tout descend de là. Ce que chacun montre derrière n'est décidé que pour Libre, qui offre des thèmes.
- **Ce que la barre d'actions porte, écran par écran.** Proposé pour la conversation — l'œil, les leviers ouverts, le retour — et rien pour les autres. Se décide avec la pile de navigation, dont il dépend : une barre ne peut pas mener où rien ne va.
- **La forme exacte de chaque mesure brute** sur l'écran de bilan. Sa largeur est arrêtée — six colonnes —, pas son écriture : `2,3 st` ou `2,3` avec l'unité au titre, `-22 %` ou `-22`, `47/50` ou `47 sur 50`. Onze décisions minuscules qui se prennent d'un coup en regardant l'écran monté.
- **Où se posent les deux décomptes et le symbole d'enregistrement.** Le doc du modèle les veut visibles en permanence — le silence en cours et la durée du tour — et la police porte déjà quatre blocs de jauge et un disque ; leur place à l'écran n'est décidée nulle part, et la capture en trois positions en a besoin.

## L'ordre de travail

1. Le thème, avec la grille et les rythmes du marquage dedans.
2. La police embarquée, et l'échelle entière calculée depuis la densité. **Les glyphes sont faits** (`../../font/`) : il reste à poser les deux TTF dans `res/font/` et à déclarer les deux pas verticaux, 15 pour une ligne de texte et 11 pour une rangée de cadre.
3. La palette des deux registres, en remplaçant la rampe continue de `ui/MarkingColors.kt` — et, dans la même passe, la palette de rechange du daltonisme, qui coûte une heure ici et tout le doc plus tard.
4. Le tour marqué : la bande de mélodie et son recouvrement, puis les marques et leur air.
5. Les cadres et les listes écrits en caractères, avec les pièces que la police porte déjà.
6. Les transitions en coupure plutôt qu'en fondu.
