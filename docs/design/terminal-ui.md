# L'interface — registre terminal

Ce que l'app doit avoir l'air d'être, et ce que ça décide dans le code. Conçu le 2026-09-03, rien d'écrit encore : l'esthétique ne prend aucun soin tant que la boucle de conversation n'est pas finie (`../../TODO.md`, chantier 0), et ce doc existe pour que le jour où elle en prend, les décisions qui coûtent cher rétroactivement soient déjà prises.

Il remplace une fiche technique antérieure qui listait des recettes Compose pour un rendu « jeu rétro » — police pixel, palette de quatre teintes, boutons en relief dessinés au `Canvas`, shader CRT. Ce qu'elle proposait est en grande partie écarté, et la fin du doc dit pourquoi, pour que ces pistes ne se reproposent pas.

## Ce qui borne tout le reste : la couleur porte une mesure

Sur le tour de l'apprenant, la teinte d'une lettre **est** l'écart au modèle, le filet sous une syllabe est l'accent, la ligne derrière le texte est la mélodie, la vaguelette est la correction et le cadre est la pertinence (`../reference.md`). Ce ne sont pas des couleurs de décor : ce sont les seules sorties visibles de l'analyse, et `reference.md` interdit qu'un réglage ou un habillage les déplace.

Le registre rétro couvre **tout** l'app, cette surface comprise — même police, même grille, même palette, même fond. Ce qu'il n'y fait pas, c'est dépenser le canal de la teinte pour du décor, et poser un filtre par-dessus. Partout ailleurs il fait ce qu'il veut.

## Le registre : terminal, pas console

Deux registres rétro existent et ils ne se mélangent pas. La **console** — sprites, cadres en relief, boîte de dialogue de trois lignes en bas d'écran : *Dragon Quest*, *Pokémon* première époque, *Undertale*, et la bibliothèque `8bitcn-ui` qui les reproduit. Le **terminal** — grille de caractères, monospace, cadres en traits, phosphore : les roguelikes (*Cogmind*, *Caves of Qud*), les interfaces DOS (Norton Commander, l'IDE Borland), les jeux de BBS (*Legend of the Red Dragon*).

**Retenu : le terminal, variante roguelike coloré** — dense et lisible, pas l'austérité monochrome. Trois raisons.

L'écran central de l'app est un **fil de texte long et annoté au caractère près**. Le vocabulaire console est fait pour trois lignes à la fois ; il faudrait soit le trahir sur cet écran, soit inventer quelque chose sans précédent.

Le registre console demande des **images** : sprites, cadres, curseurs, carte. C'est du dessin à produire et une licence libre à vérifier à l'ajout pour F-Droid. Le registre terminal se fait avec une police et une palette, et couvre toute l'app sans un seul fichier image.

Et **le monospace est une grille, qui est celle dont le marquage a besoin** — développé plus bas.

Ce qu'on paie : le terminal porte moins visiblement l'idée de rencontrer un personnage, qu'une console montrerait par un portrait dans une fenêtre. Les jeux de BBS et les jeux à texte prouvent que ça se porte quand même, et les portraits restent possibles (voir « Les images »).

## La palette

**Construite pour le projet, indexée, fond sombre unique.**

*Indexée* est le vrai geste rétro, plus que « peu de couleurs » : un jeu fixe, nommé, fini, dans lequel une valeur tombe. Le code calcule aujourd'hui une rampe continue (`Color.hsl(55f - 47f*t, …)` dans `ui/MarkingColors.kt`), soit une teinte par valeur d'écart. La quantifier en quatre ou cinq crans est **plus honnête**, pas moins : `reference.md` pose une bande de bruit sous laquelle rien n'est teinté parce que l'écart propre de la machine est de cette largeur, donc une rampe continue promet une précision qui n'existe pas.

*Construite pour le projet* plutôt qu'empruntée à une palette de pixel art connue (DB32 et les autres). Leurs rampes font varier la clarté, parce qu'elles servent à ombrer des sprites. Le marquage demande l'inverse — une rampe ambre→rouge **à clarté constante**, pour qu'elle se lise comme une seule famille et non comme un assombrissement. Une palette empruntée se battrait contre ça à chaque cran.

*Fond sombre unique*, sans thème clair. Le registre le demande. Deux palettes voudraient dire régler deux fois chaque rampe et vérifier deux fois la clarté constante, qui est la contrainte la plus délicate de l'ensemble. Et `MarkingColors.surface` — le fond dont le halo est découpé derrière les glyphes, qui doit correspondre à ce qu'il y a réellement derrière le texte — devient une constante au lieu d'une variable à faire suivre.

Le budget tient dans une vingtaine d'entrées, parce que dans ce projet **la forme porte le canal et la couleur ne porte que l'alarme et le côté** : les cinq choses marquées partagent la même rampe et se distinguent au trait.

- Trois fonds — l'écran, un panneau, un cadre.
- Trois encres — neutre, atténuée (les tours de l'IA, un élément inactif), vive (curseur, sélection).
- La rampe d'alarme, quatre ou cinq crans d'ambre à rouge, à clarté constante.
- Deux verts — la cible d'accent, et l'étiquette *juste* de la pertinence, seule mesure du projet qui ait un bon côté.
- Deux bleus — le contour du modèle, sourd, et celui de l'apprenant, vif ; ils se superposent, donc ils doivent se distinguer croisés.
- Trois ou quatre teintes de décor — bordures, touches, séparateurs.

## La police

**Departure Mono**, en licence SIL OFL, **embarquée dans `res/font/`**.

Jamais par le fournisseur Google Fonts téléchargeable : il est adossé aux services Google Play, donc une dépendance propriétaire, que la facette `fdroid` interdit. En OFL et embarquée, il n'y a aucun `NonFreeAssets` à déclarer.

Choisie **à traits épais**. Une police fine de type phosphore (VT323) est plus juste d'ambiance et donne à peu près un pixel de couleur par trait, où la différence entre ambre et rouge est invisible. La teinte a besoin de matière, donc le glyphe doit être dense.

Elle porte les **caractères de dessin de cadre** (traits, coins, intersections, éléments de bloc) depuis sa version 1.500, ainsi que le latin complet, le cyrillique et le grec — 1 186 glyphes. Les cadres se dessinent donc en texte, pas au `Canvas`.

**Sa taille de dessin est de 11 px, et elle ne se rend proprement qu'à ses multiples entiers** (son auteur le documente). Entre deux, les pixels sortent de largeurs inégales et l'effet s'effondre. Deux choses poussent contre ça sur Android : les `sp` suivent le réglage de taille de texte du système, et les `dp` dépendent de la densité de l'écran. La taille se calcule donc **depuis la densité pour atterrir sur un entier**, et si l'utilisateur peut agrandir, ce sera par crans discrets, jamais continûment.

## La grille

**Tout se cale sur la cellule de caractère** : cadres, marges, boutons, images. Pas seulement le texte.

Ce n'est pas d'abord de l'ambiance. `ui/MarkedTurn.kt` mesure aujourd'hui au sous-pixel pour savoir où poser le filet sous une syllabe et où glisser les coins des sons qu'aucune lettre ne porte. En monospace à échelle entière, la boîte d'un caractère vaut `colonne × largeur, ligne × hauteur` : le filet devient un nombre entier de cellules, les coins tombent sur des bornes exactes, et une classe entière d'arrondis disparaît. **La grille de pixels du registre et la grille d'ancrage du marquage sont la même grille.**

Le facteur d'échelle est un seul entier, et **quatre critères tirent dessus sans aller dans le même sens** — il se règle sur des mesures, pas au goût :

1. la finesse du contour de mélodie, qui dispose de `11 × facteur` crans verticaux par ligne ;
2. la surface de glyphe nécessaire pour lire une teinte graduée ;
3. le nombre de colonnes ;
4. la taille d'une cible tactile.

Non mesuré, à faire avant de trancher : la largeur d'avance réelle de la police. En la supposant autour de 6 px, un écran de 1080 px donnerait grossièrement 60 colonnes à 3x, 45 à 4x, 36 à 5x — ordre de grandeur, pas mesure.

**La densité varie d'un appareil à l'autre, donc le facteur ne peut pas être une constante** et le nombre de colonnes non plus. La mise en page doit tenir dans une plage, entre 36 et 72 colonnes environ, jamais viser un nombre précis.

## Le doigt

**Tout est directement tactile.** L'élément qu'on touche est celui qui se sélectionne ; le curseur existe toujours mais il marque ce qu'on vient de toucher au lieu de servir à naviguer. La barre du bas affiche les actions disponibles, et ses entrées sont de vrais boutons qu'on presse.

Ce n'est pas moins fidèle que le curseur qu'on déplace : les barres de touches de fonction des programmes DOS étaient déjà cliquables à la souris, Norton Commander et l'IDE Borland le faisaient tous les deux. Ce qui trahit le registre est une barre `F1 Aide  F2 Menu` que personne ne peut presser.

Conséquence mécanique, pas un choix : **une entrée touchable occupe environ trois lignes de grille**, remplies ou vides. La recommandation Android est une cible de 48 dp, et une ligne de texte à 5x sur un appareil à densité 3 fait environ 18 dp — deux entrées collées auraient des cibles qui se chevauchent. Agrandir la police ne règle rien (il faudrait un facteur autour de 13, soit une douzaine de colonnes). Ce n'est pas propre au rétro : dans n'importe quelle app Android une ligne de texte fait une vingtaine de dp et les lignes de liste sont rembourrées pour cette raison. La seule différence est qu'ici le rembourrage est visible, en cellules vides — ce à quoi ressemblent les menus de terminal de toute façon.

## Les images

Le registre n'interdit pas les images : il exige qu'elles soient sur la grille. Leur place est l'écran-titre, les portraits de personnages, les icônes d'activités et de résultats, l'icône de l'app et les images de la fiche F-Droid. Leur non-place est le fil de conversation et le tour marqué, où la couleur et la forme portent une mesure.

Quatre règles :

- **Le même pixel que le reste.** Une illustration à pixels de 5 px à côté d'une police qui dessine sur 3 px sonne faux immédiatement, et c'est le plus facile à rater.
- **La palette indexée**, sans aucune couleur en dehors.
- **Échelle par facteur entier**, avec `FilterQuality.None`. Ce dernier seul ne suffit pas, contrairement à ce qu'on lit partout : la taille doit se calculer depuis la densité, un `Modifier.size(128.dp)` ne tombant sur un multiple entier de la source sur presque aucun écran.
- **Licence libre vérifiée à l'ajout**, pas à la soumission.

`isCrunchPngs = false` est déjà posé sur le build de release pour la reproductibilité F-Droid, et c'est heureux : la recompression PNG est ce qui abîme le pixel art.

## Le thème

**Un thème propre au projet, exposé par `CompositionLocalProvider`, hors de Material.** Trois axes, dont le troisième n'est pas dans les conventions habituelles :

- les **couleurs**, la palette indexée ci-dessus ;
- la **typographie**, qui se réduit ici à une police et à quelques tailles, toutes multiples de 11 px ;
- la **grille** — facteur d'échelle dérivé de la densité, largeur et hauteur de cellule. Ce n'est ni une couleur ni une police, et si ça ne vit pas dans le `CompositionLocal`, chaque composable le recalcule et ils divergeront.

C'est la première chose à faire, et la moins chère maintenant : l'UI fait aujourd'hui neuf fichiers et environ 2 200 lignes, et `MaterialTheme` y est lu aux points porteurs — `MaterialTheme.typography.headlineSmall` pour le style du tour marqué, `MaterialTheme.colorScheme.surface` pour le fond dont le halo est découpé.

## Ce qui est écarté, et pourquoi

Pour que ces questions ne se reposent pas.

- **Sortir de Compose** — WebView, moteur de jeu, Compose Multiplatform. `ui/MarkedTurn.kt` n'est pas un écran de widgets mais un moteur de mise en page : il mesure la même chaîne deux fois sous les mêmes contraintes, peint dans un ordre imposé (règles d'accent, contours, halo au trait, glyphes, coins), et se sert du résultat de mise en page comme carte de collision. Aucune sortie ne rend ça moins cher ; un pont JS mettrait la couche la plus dure du côté le plus fragile, un moteur de jeu demanderait de réécrire la mise en page du texte, et le multiplateforme n'a pas de cible réelle (`android` : natif par défaut ; `fdroid` : iOS hors d'atteinte).
- **La palette de quatre teintes façon Game Boy** — elle détruit la gradation du marquage, qui existe précisément parce qu'un marquage binaire ne transporte plus rien quand la majorité des mots portent quelque chose.
- **Le composant `PixelButton`/`PixelContainer` dessiné au `Canvas`** — présenté ailleurs comme la brique de base d'un design system rétro, il est remplacé par la police : un cadre est du texte, il s'aligne tout seul et coûte une chaîne. Ce qui en survit est une idée : l'état pressé se signale par **inversion** de l'encre et du fond des cellules, geste de terminal, plutôt que par un relief inversé.
- **Le shader CRT (scanlines, aberration, vignettage) en AGSL** — deux raisons. `RuntimeShader` est API 33+ alors que `minSdk` vaut 26, ce qui demanderait une porte, et `universel` interdit qu'elle soit silencieuse. Surtout, un shader qui soustrait du rgb déplace la lecture de la teinte partout où il passe, donc il ne pourrait jamais couvrir la surface de marquage. Resterait une décoration d'écran-titre, pour un coût réel, alors que la police et la palette font déjà le travail d'ambiance.
- **La police par le fournisseur Google Fonts** — dépendance aux services Play, interdite par `fdroid`.

## Ce qui reste ouvert

- **La charpente persistante.** Aujourd'hui `MainActivity.Root` porte une rangée de boutons en haut qui dit à la fois où on est et où aller, et une zone de contenu qui change. Le registre a deux objets distincts pour ça : une **ligne d'état** en haut, une **barre d'actions** en bas. Les séparer mettrait les actions là où est le pouce, et donnerait un endroit à ce que `reference.md` exige à plusieurs reprises — qu'une chose indisponible **porte sa raison** (analyse éteinte faute de poids chargés, canal du son vide en attente d'une reformulation, source de micro effectivement obtenue). Coût : deux lignes de grille en permanence sur une trentaine. Non tranché.
- **La valeur du facteur d'échelle**, à régler sur les quatre critères ci-dessus une fois la largeur d'avance mesurée.
- **La quantification du contour de mélodie.** Une courbe dessinée en marches de quelques pixels est très juste dans ce registre, mais la mélodie se juge sur l'écart entre deux contours et une quantification grossière pourrait effacer des écarts réels. À mesurer avant de décider ; c'est aussi l'un des critères qui fixent le facteur d'échelle.
- **Le texte de l'IA qui apparaît caractère par caractère.** Très juste dans le registre et utile à la rencontre de personnage. Mais les tours de l'IA sont floutables et l'audio est le canal principal : un défilement qui devance ou traîne derrière la voix serait pire que pas de défilement.
- **La rencontre de personnage** (`../../NOTES.md`) — ce qui fait qu'on rencontre quelqu'un plutôt qu'on lance un thème. C'est une grammaire d'interaction, pas un habillage, et rien ici ne la décide.
- **La pile de navigation.** Les quatre écrans actuels sont un interrupteur à quatre positions dont aucun ne mène à un autre ; le modèle d'activité (`activity-model.md`) amènera des écrans qui descendent les uns dans les autres. La charpente ne doit pas bloquer ça.

## L'ordre de travail

1. Le thème, avec la grille dedans.
2. La police embarquée, et l'échelle entière calculée depuis la densité.
3. La palette indexée, en remplaçant la rampe continue de `MarkingColors`.
4. Les cadres et les listes en caractères.
5. Les transitions en coupure plutôt qu'en fondu.
