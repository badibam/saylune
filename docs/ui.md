# L'interface — pixel doux

Ce que l'app a l'air d'être, et ce que ça décide.

**Ce qui est écrit ici et ce qui est écrit dans le code.** Chaque écran porte sa forme et sa raison dans son en-tête : la charpente (`ui/Scaffold.kt`), la rangée de commandes (`Commands.kt`), les boutons du bas (`Buttons.kt`), le bilan du passage (`PassageNotes.kt`), le tour de l'IA et son brouillage (`AiTurn.kt`), les paramètres d'affichage (`DisplaySettingsScreen.kt`) et les canaux de marques qu'ils commandent (`Channels.kt`), la liste des conversations (`ConversationsScreen.kt`), la ligne qui nomme le tour (`TurnLabel.kt`), les cadres (`Frames.kt`), le relevé son par son (`AnalysisReadout.kt`), l'ordre de peinture du tour marqué (`MarkedTurn.kt`). Le thème porte les valeurs : la grille (`theme/Grid.kt`), les sept nombres d'un registre (`theme/Palette.kt`), les règles de calcul de couleur (`theme/Oklab.kt`), les rythmes du marquage (`theme/Rhythm.kt`), la police (`theme/Typography.kt`). La boîte de la police et sa zone privée sont dans `../font/README.md`.

Ce doc porte le reste : **le registre**, **ce qui n'est pas écrit**, **ce qui est écarté** et **ce qui reste ouvert**.

Les valeurs chiffrées ont été réglées à l'œil sur **`../bench/pixel-ui.html`**, un banc qui dessine un écran complet à la résolution réelle d'un téléphone, police embarquée et palette réglable. Il reste l'instrument de tout ce qui se juge en regardant plutôt qu'en raisonnant — la rechange du daltonisme l'attend. Il est versionné : écrit à la main, il ne se régénère pas, donc le perdre serait perdre la source des couleurs.

## Ce qui borne tout le reste : la couleur porte une mesure

Sur le tour de l'apprenant, la teinte d'une lettre **est** l'écart au modèle, le filet sous une syllabe est l'accent, la bande au-dessus est la mélodie, la vaguelette est la correction, les crochets sont la pertinence. Ce ne sont pas des couleurs de décor : ce sont les seules sorties visibles de l'analyse, et rien — réglage ou habillage — n'a le droit de les déplacer.

Le registre couvre **tout** l'app, cette surface comprise : même police, même grille, même palette, même fond. Ce qu'il n'y fait pas, c'est dépenser le canal de la teinte pour du décor, ou poser un filtre par-dessus. Partout ailleurs il fait ce qu'il veut.

## Le registre : la console, en doux

**Pixel art de console, chaleureux, à fond prune.** La référence est le sous-écran d'une Zelda 8 ou 16 bits — un panneau dense, encadré d'une bordure épaisse à deux tons, sur une grille de tuiles.

Ce que ça n'est pas, et qui se propose tout seul si on ne le dit pas : **ce n'est pas un lieu**. Pas de carte, pas de monde, pas de personnage qu'on croise dans un décor. L'app reste une app ; c'est son ambiance qui vient de là, pas sa grammaire d'interaction. Le registre s'obtient avec une police, une palette et des cadres, **sans un seul fichier image obligatoire**.

Le pixel doit être **doux, et cette douceur est peinte, jamais filtrée** : tramage tendre, crans voisins peu contrastés, dégradés cuits au pixel près, halos dessinés à la main. Aucun flou, aucune transparence appliquée par-dessus une surface qui porte une mesure. **La chroma de la rampe reste haute** — c'est elle qui porte la lisibilité : la douceur vient du fond, des cadres et des formes, jamais d'affamer la rampe.

## Trois décisions de dessin qui ne se rouvrent pas

Ce sont des choix de police que ni `theme/Typography.kt` ni `../font/README.md` n'ont de raison de porter, et qu'on refera si personne ne les écrit.

- **Les descendantes descendent vraiment.** `g j p q y` étaient tassés dans la hauteur d'x et le `g` s'y lisait `9`. Aucun redessin ne le répare : à trait de 2 pixels, un bol fermé prend quatre rangées et la queue les quatre qui restent, et un bol au-dessus d'une queue *est* la silhouette d'un `9`. C'est ce qui a fait grandir la boîte de 11 × 11 à 11 × 15 — vers le haut et vers le bas seulement, donc aucun glyphe d'origine n'a bougé, l'avance n'a pas changé, et le nombre de colonnes comme l'ancrage horizontal du marquage sont intacts. Ce que ça coûte est le pas de ligne.
- **Les deux ligatures d'affriquée sont volontairement absentes.** Dans une police à chasse fixe une ligature doit tenir dans une cellule de toute façon : y écraser un `d` et un `ʒ` à trait de 2 pixels est strictement pire que d'en prendre deux. L'écran écrit `dʒ` et `tʃ`, qui est de l'API tout aussi standard et la notation la plus courante des dictionnaires, et les diphtongues occupent déjà deux cellules. C'est une correspondance à l'affichage : **la donnée garde `ʤ` et `ʧ`**, que le modèle émet et que tous les tours du disque portent.
- **Deux paires de phonèmes ne se distinguent pas, et l'inventaire les sépare quand même.** `ə` diffère de `a` d'**un pixel** et `ɑ` de `o` de **deux** — le `a` de Mono10 *est* un `e` retourné, son `o` *est* le bol du `d` — donc un schwa fidèle est le glyphe d'à côté, et aucun dessin ne les sépare à dix pixels de large. Mais `a e o ɔ` n'apparaissent **jamais seules**, uniquement en tête de diphtongue, et `ə` et `ɑ`, qui apparaissent seuls, n'ont pas de diphtongue : les deux membres de chaque paire ne tombent jamais à la même position, et les distordre coûterait la fidélité pour réparer une confusion qui ne peut pas se produire. **Ce que ça laisse dû** : ça cesse d'être vrai le jour où un phonème s'écrit à l'intérieur d'une phrase ordinaire, où un `ə` se lira `a`. Rien ne le fait aujourd'hui — la notification de faute de son ne nomme rien.

Trois choix de fond gouvernent le reste du jeu de glyphes. **Un glyphe ne remplace un mot que là où la place manque** — la rangée de commandes d'un passage et les entrées de la charpente sont les seules qui en manquent ; les boutons du bas gardent leurs mots, sauf la pause, qui est le seul contrôle de l'app à montrer un état plutôt qu'à le dire (les deux barres pendant qu'il enregistre, le disque pendant qu'il est tenu). **Une famille se dessine entière** : le triangle et le disque appelaient la pause et l'arrêt, `×½` appelait `×¼`, et il coûte moins de dessiner un membre que de le découvrir absent devant un écran. Et **la prose de l'IA a sa liste fermée**, sans rien derrière : un caractère absent rend un carré vide, ce qui est bruyant et vrai, là où un repli silencieux masquerait le trou (`universel`, no-fallback). Le brouillage, lui, a un repli, mais **pour une lettre seulement** — la ponctuation reste claire, étant le support qui fait qu'un panneau brouillé se lit comme une phrase.

## Ce que la grille coûte, et à qui

Le facteur d'échelle est un **entier**, et c'est la seule contrainte dure ; le calcul est dans `theme/Grid.kt`. Deux conséquences valent d'être dites une fois.

**Le pire écran est celui dont la densité tombe mal, pas le plus étroit.** À 420 dpi la densité vaut 2,625 ; le facteur devant être entier il monte à 3, les lettres sortent à 11,4 dp au lieu de 10 et la cellule à 12,6 — **28 colonnes** sur 360 dp, contre 32 sur un écran ordinaire. C'est ce nombre qui commande les mises en page, et il est **calculé et non mesuré** : le vérifier demande de poser la police sur un vrai téléphone.

**Un dessin fin n'oblige pas à une cible tactile fine.** Conséquence mécanique, pas un choix : **une entrée touchable occupe environ trois lignes de grille**, remplies ou vides — la recommandation Android est de 48 dp et une ligne fait ici environ 11 dp. Ce n'est pas propre au registre ; la seule différence est qu'ici le rembourrage est visible, en cellules vides.

**Mais la marge invisible ne suffisait pas, et c'est un fait d'usage.** Un glyphe à la taille ordinaire fait onze pixels de dessin : quelle que soit la cible autour, il se lit comme une illustration et pas comme une chose qu'on appuie. Deux réponses, prises ensemble. **Le registre a une seconde taille**, le même dessin au double du facteur — un multiple entier d'un entier reste entier, donc la seule contrainte dure tient, et c'est aussi pourquoi il n'y a pas de troisième taille entre les deux. Et **tout bouton en glyphe est encadré** : le cadre est le mot du registre pour *ceci est un objet*, et il fixe la largeur quoi qu'on dessine dedans. Une entrée coûte alors **quatre colonnes sur quatre lignes** — deux cellules d'encre, une de bordure de chaque côté — et c'est la même mesure partout, du haut de la charpente à la rangée de commandes d'un passage.

**Les meubles, eux, sont dessinés pour cette taille et non doublés.** Le doublement est littéral — un pixel d'encre devient un carré de quatre —, et une lettre n'a pas le choix : il y a un alphabet et il est dessiné une fois. Un glyphe qui tient lieu de bouton, si : `font/big.py` redessine les meubles dans une boîte de 22 × 22, donc la forme a quatre fois plus de pixels pour se faire et chacun d'eux est **de la taille de ceux du texte**. Même place sur la grille, même place dans un cadre, même caractère demandé ; seule la famille change. **Ce que ça coûte est que les deux tailles du registre n'ont plus un seul pixel entre elles** : une lettre de note à la seconde taille à côté d'un bouton montre deux tailles de pixel sur un écran, ce que ce doc refuse d'une illustration. Acheté en connaissance de cause.

**La seconde taille est pour ce qui se lit, pas pour ce qui se parcourt** : une note, qui est une lettre et le sujet de l'écran où elle est, et le glyphe d'un bouton. Une mesure chiffrée reste à la taille ordinaire — c'est le cran où l'on descend quand la lettre a dit où regarder, et doubler les deux écraserait les deux niveaux en un.

## Le daltonisme

**Une palette de rechange, en préférence utilisateur.** Le registre garde ses couleurs pour tout le monde ; qui ne les sépare pas en change. Corriger la palette de base à la place taxerait le défaut d'une esthétique choisie pour une perception que la plupart n'ont pas.

**Le cas qui l'exige est précis, et c'est le pire du doc.** *Juste* et *à côté* sont **la même forme** — des crochets — et **deux verdicts opposés**, séparés par la seule couleur, verte contre rouge. En deutéranopie les deux convergent : la marque d'une réussite devient celle d'une faute. Le doc interdit ailleurs qu'une marque absente se confonde avec une approbation ; ici c'est la présence qui se confond avec son contraire. La pastille du suivi a le même défaut sur quatre valeurs, et la rampe à quatre crans peu contrastés y ajoute la sienne.

**Ce n'est donc pas une permutation de teintes** : la rechange doit satisfaire **la même règle de marquage** dans les deux registres, et trouver pour les crochets deux teintes qui restent séparées sous la perception visée. Elle se règle au banc comme le reste de la palette. Le premier jet existe et **rien n'y est mesuré** (`../TODO.md`) ; ce qu'elle couvre n'est pas tranché non plus — les confusions rouge-vert sont le cas à traiter d'abord, la tritanopie toucherait le bleu du contour de mélodie.

## Le tour marqué

C'est l'écran le plus dense de l'app et le seul dont chaque pixel porte une mesure. **Le tour est une tranche du fil, qui défile au glissement** — jamais un écran à tour unique, donc pas de compteur de tour. L'ordre de peinture et le partage de l'air sont dans `ui/MarkedTurn.kt`, la mise en page dans `TurnLayout.kt`, les couleurs dans `MarkingColors.kt`. Ce qui suit est ce que le dessin doit à la mesure.

**Ce qui dépasse est l'erreur.** Le contour de l'apprenant est peint dessous, sur le bout rouge de la rampe ; celui du modèle par-dessus, à la même épaisseur, dans un bleu calme. Là où les deux coïncident, le rouge est entièrement recouvert : **bien parler fait disparaître la couleur au lieu de la changer**, et la quantité de rouge visible est la quantité d'écart. La mélodie n'a donc pas de couleur à elle, elle emprunte la rampe commune. **Son seuil de sensibilité est géométrique** : c'est l'épaisseur du trait plus la largeur du halo, et en dessous les deux courbes se recouvrent.

**Elle a sa propre bande, au-dessus de la ligne de texte.** Sa verticale est une grandeur continue, dont la finesse n'a aucune raison d'être plafonnée par la hauteur d'une cellule ; son horizontale, elle, reste calée sur les caractères — c'est là qu'est son ancrage.

**La courbe est peinte comme une surface, pas comme une suite de segments** : pour chaque colonne de pixels on calcule l'étendue verticale que la courbe occupe, unie au raccord vers la précédente. Cette étendue est d'un seul tenant par construction, donc ni moignon ni pixel isolé ne peut apparaître dans un virage. Et **le halo est à l'extérieur du trait, jamais creusé dedans** : un trait de 2 pixels dont on atténuerait les bords n'aurait plus aucun pixel en pleine couleur, et il paraîtrait plus mince et plus terne qu'il n'est.

**La forme dit l'échelle, la couleur dit le côté et la distance au neutre.** D'où deux choix qui se seraient pris à l'envers sans ça : la correction se souligne **en vaguelette**, parce que le filet droit de l'accent tombe au même endroit et qu'une vaguelette se lit comme une erreur de langue sans qu'on ait à l'apprendre ; et la pertinence se prend **entre crochets** et non dans un cadre complet, parce que *juste* se marque aussi, donc presque tous les groupes portent une étiquette et une enceinte qui court le long du texte ferait de la ligne une chaîne de boîtes.

**Aucune couleur sur les points de pause.** Ce qui fait qu'une pause est trop longue dépend du réglage, et aucun réglage ne touche une marque. Teinter *le plus long* serait pire : la même pause de 2 s serait teintée dans un tour calme et grise dans un tour où traîne un blanc de 3 s, donc la même faute ne produirait plus la même marque. Et ce sont des **points médians** : trois points bas veulent dire *du texte a été retiré*, ce que disent déjà les crochets gris du remplissage.

**Une pause tient dans le blanc qui existe déjà**, jamais dans une colonne de plus : la courbe de mélodie est ancrée aux caractères et ne doit pas être décalée. À une frontière de groupe, la colonne laisse les deux bras des crochets tenir dans le même blanc, donc leur écart horizontal est fixé au pixel et n'est pas un réglage.

**Un groupe porte son identité, pas son étiquette.** Deux groupes voisins peuvent mériter la même étiquette sans être le même groupe, et un groupe coupé par un retour à la ligne **ouvre d'un côté et ferme de l'autre**, au lieu de se dédoubler en deux enceintes complètes.

**Les marques n'arrivent pas d'un coup, et ce n'est pas un choix** : les marquages jugés tombent au retour de l'appel, les marques du son à la fin de l'analyse, deux à trois secondes plus tard. La révélation est donc canal par canal par construction — et ça tombe bien, les jugées étant à l'écran avant que l'IA parle.

**Les marques des mots restent une fois la porte des mots passée, et c'est le défaut.** Un passage qui passe porte quand même des marques, la porte lisant une note et non l'absence de marque. La ligne devient dense quand les marques du son arrivent par-dessus, et c'est le prix accepté — un `plat` sur un passage qui passe reste une chose vraie qu'on a le droit de voir. Les retirer est une préférence d'affichage, jamais de mesure.

## La porte « Libre » : des tuiles, une par thème

Écrit le 2026-09-07, et ce qui suit est ce qui est à l'écran.

**Deux colonnes de tuiles en portrait, sauf la première qui prend toute la largeur.** La première est la conversation sans thème ; elle est toujours affichée en tête et n'est pas un thème parmi d'autres — c'est le seul endroit où l'apprenant apporte la situation lui-même —, donc elle a la largeur plutôt qu'un rang. Les suivantes sont les thèmes livrés, deux par ligne.

**Une tuile porte un nom, un visage et un compte.** Le nom est le thème, déclaré dans son fichier ; le visage est le **personnage principal** que le fichier marque (`activity.md`) ; le compte est le nombre de passages de la séance derrière elle — un compte de lignes, donc rien de stocké. Une tuile que personne n'a ouverte ne montre **pas** de compte plutôt qu'un zéro : ce qu'elle dirait est *pas commencée*, et un zéro dit *commencée et vide*.

**La surface est de taille fixe, et c'est ce qui la sépare d'un catalogue** : les tuiles sont les définitions livrées, donc leur nombre est décidé par l'app et jamais par l'usage. La liste d'une ligne par séance, qui grandissait sans fin, est partie avec la PoC dont elle venait. Les tuiles ne bougent pas de place, sauf que celles qui sont commencées passent devant, la première restant la première.

**Toucher une tuile ouvre l'écran de situation, un seul écran à deux états.** Rempli, il montre la situation complétée, **ce que l'apprenant a apporté en couleur** — le bleu calme et jamais la rampe, qui est l'alarme : la teinte du marquage sur un mot qu'on vient de taper dirait ce que le marquage n'a pas dit. Ça ne coûte rien : le gabarit est dans le fichier et le texte fini est sur la ligne, donc où était le trou se relit en posant l'un sur l'autre. Vide, il montre la situation avec ses trous, une question par trou, et le choix du **genre** là où le fichier le laisse ouvert — *peu importe* par défaut, qui n'est pas un troisième genre mais l'absence de contrainte, et tire au sort. **Les trous se remplissent à la frappe**, dans la couleur qu'ils garderont : ce dans quoi on va arriver se lit avant d'appuyer, pas après.

**Deux sorties, dont une qui demande.** *Poursuivre* prend la séance telle qu'elle est, et s'appelle *commencer* tant qu'il n'y a rien à poursuivre — le seul écart à ce que ce doc disait, deux boutons aux mots figés en laissant un mentir sur une tuile que personne n'a ouverte. *Recommencer* est la seule chose de l'app qui rende une séance inatteignable : l'ancienne reste en base, et tant qu'aucun écran d'historique n'existe, hors d'atteinte vaut perdue à l'usage. Elle demande donc, défaut sur non, **dans le registre plutôt que dans une boîte de dialogue** : les deux boutons deviennent la question, et la réponse est là où la main est déjà.

## Les images

Le registre ne les interdit pas, il exige qu'elles soient sur la grille — et il n'en a besoin d'aucune pour exister. Leur place est l'écran-titre, les portraits, les icônes d'activités et de résultats, l'icône de l'app et la fiche F-Droid. Leur non-place est le fil et le tour marqué, où la couleur et la forme portent une mesure.

- **Le même pixel que le reste** — une illustration à pixels de 5 px à côté d'une police qui dessine sur 3 px sonne faux immédiatement, et c'est le plus facile à rater.
- **La palette du registre**, sans aucune couleur en dehors.
- **Échelle par facteur entier**, avec `FilterQuality.None`. Ce dernier seul ne suffit pas : la taille doit se calculer depuis la densité, un `Modifier.size(128.dp)` ne tombant sur un multiple entier de la source sur presque aucun écran.
- **Licence libre vérifiée à l'ajout**, pas à la soumission. Vaut aussi pour les sons, le jour où il y en aura.

`isCrunchPngs = false` est posé sur le build de release pour la reproductibilité F-Droid, et c'est heureux : la recompression PNG est ce qui abîme le pixel art.

## Ce qui est écarté, et pourquoi

- **Sortir de Compose** — WebView, moteur de jeu, Compose Multiplatform. `ui/MarkedTurn.kt` n'est pas un écran de widgets mais un moteur de mise en page : il mesure la même chaîne deux fois sous les mêmes contraintes, peint dans un ordre imposé, et se sert du résultat de mise en page comme carte de collision. Aucune sortie ne rend ça moins cher ; un pont JS mettrait la couche la plus dure du côté le plus fragile, un moteur de jeu demanderait de réécrire la mise en page du texte, et le multiplateforme n'a pas de cible réelle (`android` : natif par défaut ; `fdroid` : iOS hors d'atteinte).
- **Une palette de quatre teintes façon Game Boy** — elle détruit la gradation du marquage, qui existe précisément parce qu'un marquage binaire ne transporte plus rien quand la majorité des mots portent quelque chose.
- **Des cadres peints au `Canvas`** — un cadre qui est du texte s'aligne tout seul et ne se mesure pas. Ce qui reste au `Canvas` est ce qui ne peut pas être une cellule : les marques du tour, qui se posent sur des groupes de largeur arbitraire.
- **Un shader CRT en AGSL** — `RuntimeShader` est API 33+ alors que `minSdk` vaut 26, ce qui demanderait une porte qu'`universel` interdit silencieuse. Surtout, un shader qui soustrait du rgb déplace la lecture de la teinte partout où il passe, donc il ne pourrait jamais couvrir la surface de marquage.
- **Un halo de mélodie en alpha** — il laisserait transparaître le rouge de l'apprenant sous le modèle, donc le recouvrement cesserait d'être total au moment même où il doit l'être.
- **La police par le fournisseur Google Fonts** — dépendance aux services Play, interdite par `fdroid`.
- **Un fond de parchemin chaud** en registre clair — il est dans la famille de l'ambre, donc l'alarme deviendrait une nuance du fond au lieu d'une intrusion.
- **Le texte de l'IA qui apparaît caractère par caractère** — très juste dans le registre, mais les tours de l'IA sont brouillés et l'audio est le canal principal : un défilement qui devance ou traîne derrière la voix serait pire que pas de défilement.

## Ce qui reste ouvert

- **Les poids se règlent sur un polygone, curseurs concentriques** — un axe par enfant du nœud, le curseur court du centre au sommet, et le polygone que les curseurs dessinent se lit d'un coup. Un poids étant une **part entre frères**, c'est littéralement le partage de la voix du nœud qu'on regarde. Le nombre de côtés vient du nœud et non de l'arbre, qui est à profondeur libre : cinq au premier niveau. Le reste de l'écran custom est hors périmètre.
- **La séparation des quatre crans en registre clair** — la gamme des couleurs disponibles n'est pas la même en sombre qu'en clair : une mesure à refaire, pas un réglage à recopier.
- **Les sons de l'app.** Rien n'est décidé. Le canal principal *est* l'audio — la voix du modèle, et le micro ouvert pendant la réponse — donc une ambiance continue entre en concurrence avec ce qu'on écoute et se fait capter par le micro. Un babil de texte est exclu d'avance, le tour de l'IA étant réellement parlé.
- **Le lecteur d'écran — hors v1, et dit plutôt que tu.** `ui/MarkedTurn.kt` est un `Canvas`, donc toute la sémantique du marquage est invisible à TalkBack. Ce n'est pas une porte fermée — un mot porte déjà son cran et sa feuille, il n'y a rien à mesurer de plus, seulement à décrire — c'est un travail qui ne se fait pas maintenant. Pour une app destinée à un dépôt public, le silence vaudrait décision par défaut.
- **La rencontre de personnage** (`../NOTES.md`) — ce qui fait qu'on rencontre quelqu'un plutôt qu'on lance un thème. C'est une grammaire d'interaction, pas un habillage, et rien ici ne la décide.
- **La pile de navigation.** Les écrans actuels ne mènent pas les uns aux autres ; le modèle d'activité en amènera qui descendent les uns dans les autres. **Ce qui est arrêté est sa racine** : l'app s'ouvre sur un écran-titre présentant les quatre modes, et tout descend de là. Ce que chacun montre derrière n'est décidé que pour Libre. **Ce que la barre d'actions porte écran par écran** s'en déduit et se décide avec : une barre ne peut pas mener où rien ne va.
- **La forme exacte de chaque mesure brute** sur l'écran de bilan. Sa largeur est arrêtée, six colonnes ; pas son écriture — `2,3 st` ou `2,3` avec l'unité au titre, `47/50` ou `47 sur 50`. Onze décisions minuscules qui se prennent d'un coup en regardant l'écran monté.
- **Où se posent les deux décomptes et le symbole d'enregistrement.** Ils doivent être visibles en permanence, la police porte déjà les blocs de jauge et un disque, et leur place n'est décidée nulle part.
