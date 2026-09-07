# Les mesures et les notes

Ce que l'app observe d'un passage, comment ça devient une note, et ce que chaque feuille déclare. La règle qui gouverne tout le doc est dans `reference.md` : la mesure ne se règle pas, la marque l'affiche telle quelle, la note seule est une lecture réglée.

Quatre choses à ne pas confondre. Une **feuille** est une mesure, que personne ne règle. Un **levier** est un réglage de la séance, qui ne mesure rien (`activity.md`). Une **sensibilité** est un levier attaché à une feuille : elle ne touche pas la mesure, elle déplace ses bornes A–E — *40 % de sons ratés* est la mesure, que ce soit un C ou un E est la sensibilité. Un **poids** est attaché à une feuille et n'est pas un levier, sa direction dépendant de l'apprenant.

## Les notes

**Une échelle unique, A–E**, pour tout ce qui se note à n'importe quel niveau.

**Une note est un nombre de 0 à 1 ; la lettre en est l'affichage.** Les lettres découpent l'échelle en cinquièmes égaux — E jusqu'à 0,20, D jusqu'à 0,40, C jusqu'à 0,60, B jusqu'à 0,80, A au-dessus — donc **la barre A/B vaut 0,60**, par construction et non par mesure. Chaque bande se lit en quarts : le quart bas donne le modifieur `−`, le quart haut le `+`, la moitié centrale la lettre nue. Quinze valeurs affichables.

**Le modifieur est d'affichage seulement** : rien ne le stocke, et une condition nomme une lettre, jamais un `B+`. Qui veut plus fin lit le chiffre de la feuille.

**La note vit sur la mesure, pas sur l'aptitude.** Une aptitude est un regroupement de mesures, chacune avec sa sensibilité et son poids ; sans quoi un défi qui ne note que l'accent tonique rendrait une note « élocution » qui ne veut pas dire la même chose que celle du défi d'à côté.

**Moyenner des lettres ne tient pas.** Deux passages à 8 % et à 1 % de sons ratés tombent tous deux en A et rendraient la même note, pendant qu'un dixième de pourcent ferait basculer un demi-cran ailleurs : des paliers plats séparés par des falaises. C'est aussi ce qui fait que **la sensibilité est ce qui rend les feuilles comparables** — traduire « 8 % de sons ratés » et « 3 demi-tons » en un même repère demande mieux que cinq valeurs. Prix : l'apprenant ne recalcule pas sa note de tête depuis les lettres affichées.

**La lettre est toujours une note, jamais une mesure**, puisque la sensibilité déplace ses bornes. Les deux ne portent donc jamais les mêmes noms, et **on garde les nombres et les crans en base, jamais les lettres** : les bornes pourront bouger sans abîmer les vieilles séances.

## L'arbre des poids

**Ce qui se note est un arbre, à profondeur libre.** Aptitude, mesure et découpage plus fin sont des nœuds ; la profondeur dit seulement à quel grain on peut peser. La question « est-ce une mesure ou une catégorie de mesure ? » ne se pose jamais, et **rien n'oblige un nœud de premier niveau à être une aptitude**.

**La note d'un nœud est la moyenne pondérée de ses enfants présents**, et rien d'autre : pas de plancher, pas de bonus, pas de formule propre à une aptitude.

**Un poids est une part entre frères, pas un multiplicateur absolu.** À chaque branche, le poids déclaré d'un enfant se divise par la somme de ses frères, et les parts se multiplient en descendant. C'est ce qui fait que le même poids partout donne le même mot à chaque aptitude. En absolu, une branche pesait plus pour avoir plus de feuilles sous elle : `pronunciation 1` contre `correctness 1` donnait à la prononciation quatre fois la voix de la correction, ce que personne n'avait choisi.

**Les parts se calculent sur l'arbre déclaré, une fois, jamais sur ce qu'un passage a mesuré.** Une feuille absente sort de la somme, et rien ne se redistribue dans le dos de l'auteur.

**L'agrégation se fait une fois, à plat, sur les feuilles réellement présentes.** Les lettres d'aptitude et de passage sont la même formule restreinte à un sous-arbre : des lectures, pas des étapes de calcul. Une cascade de moyennes redistribue en silence dès qu'une feuille manque — et il en manque tout le temps. Sur deux passages dont le second a la porte des mots fermée, avec élocution 2 (sons 1, mélodie 1) et correction 1 : en cascade la correction finit par peser deux tiers de la séance, l'inverse exact du 2:1 demandé ; à plat, `(40 + 80 + 90 + 50) / 4 = 65` rend bien le 2:1.

**Une feuille absente sort de la somme, elle ne vaut jamais zéro.**

**Chaque feuille pèse par la longueur du passage**, comptée en **mots retenus** — un passage dont la porte a coupé l'analyse n'a pas de sons et a toujours des mots. Sauf le suivi, qui pèse sur la difficulté du tour de l'IA, la matière qu'il couvre.

**Entre feuilles, la moyenne décide seule** : pas de plancher qui plafonnerait la note dès qu'une feuille passe sous la barre. Une bonne feuille peut masquer une mauvaise, et c'est accepté — dans un défi, peu de feuilles comptent.

**Les poids sont constants pour toute la séance, et aucun patch n'en déplace.** Sinon la note de fin serait une moyenne de mesures prises sous des règles différentes. Prix réel : un défi qui ouvre ses exigences une par une ne peut pas ne noter que ce qui était demandé au moment où on parlait. L'exercice à étages s'écrit par les **conditions**, qui ne touchent aucun poids : ce qui s'ouvre en cours de route est ce qui bloque, pas ce qui compte.

**Peser une feuille qui rend presque toujours la même valeur déplace toute l'échelle du nœud**, et ça se dit à qui écrit un arbre plutôt que de s'interdire. Une feuille qui vaut 1,00 sur presque tous les passages est un 20/20 permanent : à poids égal avec une feuille qui bouge, la note du nœud ne descend plus sous la moitié.

## Ce qu'une feuille déclare

**Un défi s'écrit contre une liste, jamais contre le code** : sans elle, poser une condition demanderait de savoir qu'une feuille existe, comment elle s'appelle et si son chiffre est en secondes ou en pourcentage.

L'arbre des feuilles est déclaré en un endroit, et chaque feuille y dit : son **nom** et sa place dans l'arbre ; ses **éléments** — les sons du passage, les mots retenus, les mots prononcés, le temps du tour, ou le passage entier quand elle n'a qu'un élément ; **comment un élément prend sa valeur** ; l'**unité de son chiffre** ; sa **direction**, quand le chiffre garde une unité brute ; sa **série** ; si elle **prend une consigne** ; si elle **donne une note**.

Un défi se réduit alors à quatre choses posées sur cette liste : des poids sur les nœuds, une position de sensibilité par feuille, des consignes sur les marquages jugés, des conditions. Rien n'y est un branchement de code neuf, et une définition écrite par un modèle devient possible sans lui donner le code.

**Trois façons de donner sa valeur à un élément :**

- **colonne** — une valeur par cran, pour les marquages jugés : `plat` vaut 0,50, `ok` vaut 0,90.
- **rampe** — une courbe sur une quantité, pour les sons : 0 point d'écart vaut 1,00, 30 points valent 0.
- **vrai/faux** — pour les binaires.

**Le chiffre est la moyenne des éléments** : une part et une moyenne sont la même chose, donc il n'y a qu'une recette. **Quand la feuille n'a qu'un seul élément, il n'y a rien à moyenner** — le chiffre garde son unité brute et la série la lit directement ; une rampe n'a d'objet que pour ramener des éléments hétérogènes sur une échelle commune avant de les moyenner.

**La valeur d'un élément est toujours une qualité entre 0 et 1, le haut étant le bon bout.** D'où le fait que la **direction** ne se déclare que pour les feuilles dont le chiffre garde une unité brute.

**Jamais un nombre d'occurrences dans une note.** Deux gros ratés dans *Where is it?* et deux dans *I was thinking about going to the market* ne valent pas la même chose : sur quatre mots dont deux abîmés personne ne rattrape le sens, sur neuf le contexte répare. Le nombre ne disparaît pas, il change d'endroit — « zéro faute franche » est une **condition**.

**Le dénominateur est ce que la feuille lit**, pas tout le passage : sur un défi qui ne pèse que les *th*, un raté sur quatre *th* fait 25 %, pas 3 %.

## La sensibilité est une série, une position est une fenêtre

**Ce que l'app lit pour transformer un chiffre en note est une table de quatre bornes**, dans l'unité de la feuille, écrites à la main et lues telles quelles. Aucun calcul entre feuilles.

Une transformation unique — « sévère, c'est les bornes divisées par deux » — ne tient pas : le même geste durcit sainement les gros ratés, où 8 % deviennent 4 %, et casse la mélodie, où 2 demi-tons deviennent 1, sous la bande de bruit de la machine. Rien ne rend les feuilles comparables avant le passage en A–E.

Les tables ne s'écrivent pas séparément pour autant : **une feuille déclare une série, et une position de sensibilité est une fenêtre de quatre bornes consécutives.**

```
intelligibilité (part des sons intelligibles)
  série :  0,80   0,88   0,93   0,96   0,98   0,99
    indulgente → (0,80  0,88  0,93  0,96)
    normale    → (0,88  0,93  0,96  0,98)
    sévère     → (0,93  0,96  0,98  0,99)
```

**« Sévère » veut donc dire la même chose partout : un cran de sensibilité vaut une lettre**, sur les sons comme sur le silence comme sur le débit. C'est sans unité par construction — on ne calcule rien entre deux unités, on décale d'un rang dans une liste. Et **une position de plus coûte un nombre, pas quatre**.

**Rien n'est calculé, tout est écrit**, donc un bord impossible se voit en écrivant la série plutôt qu'à l'exécution, sur un apprenant qui ne comprend pas pourquoi il n'atteint jamais A.

**Colonnes et rampes ne sont pas la sensibilité** : elles appartiennent à la feuille et ne se règlent pas. Si les deux se réglaient, durcir la rampe et baisser la borne A feraient la même chose, et plus rien ne dirait lequel a rendu une séance dure. Un défi qui veut sa propre ligne la pose en **condition**.

**Deux feuilles sur les mêmes éléments ne se justifient que si deux défis veulent l'ordre inverse.** Une série est monotone : elle rééchelonne, elle ne réordonne jamais. Ce test d'inversion décide branche par branche, ci-dessous.

**Une feuille binaire n'a pas de sensibilité du tout** : toutes les bornes possibles rendent la même paire de lettres. Ni sensibilité ni poids — deux champs sans objet plutôt que déclarés inertes.

**Sur une feuille à un seul élément, les cinq lettres ne sont pas toutes atteignables, et c'est très bien** (acté le 2026-09-06). Le suivi a six crans, donc son chiffre ne prend que six valeurs, groupées en haut et étalées en bas : aux positions 0, 2 et 4 le C est hors d'atteinte, aux positions 1 et 3 le D et le B le sont. Aucune série ne le répare — huit valeurs dans cinq intervalles en laissent forcément un vide. Ce qui tient : sur une feuille continue les cinq lettres sont atteignables, sur une feuille à crans les deux bouts le sont.

## Élocution

### Les sons — deux feuilles, mêmes éléments, deux rampes

Le test d'inversion passe. Deux apprenants, cinquante sons : **A** a un accent épais mais reste compréhensible, tous ses sons à une vingtaine de points ; **B** a un accent propre et deux sons complètement faux qui changent le mot. Sur la moyenne des écarts A vaut 0,80 et B 0,93 ; sur la marche A vaut 1,00 et B 0,96. L'ordre s'inverse, donc aucun réglage d'une feuille unique ne rend les deux verdicts.

- **L'intelligibilité** — rampe en marche d'escalier à 30 points : le mot a changé, ou il n'a pas changé. Le chiffre est la part des sons intelligibles, la série **tassée en haut**, un gros raté étant rare et grave.
- **La proximité** — rampe continue sur l'écart, gros ratés compris. Aucun seuil à trouver, série **étalée**, l'écart moyen bougeant sur toute la plage.

Ce sont les deux défis opposés du projet : *« fais-toi comprendre »* ne compte que ce qui change le mot, *« gomme ton accent »* compte tout.

**« Points » est l'écart ramené sur 100** : la distance entre deux répartitions de ressemblance, celle de l'apprenant et celle du modèle. Ce n'est pas une note de prononciation, c'est une distance entre deux lectures de la même machine.

**La ligne du gros raté est celle où la rampe de l'écran sature, 30 points, et c'est la même pour la feuille et pour l'écran** — ailleurs, deux lettres du même rouge plein compteraient différemment sans que rien ne le montre. **30 n'est pas mesuré** : sur le jeu étiqueté les témoins sont à 0,3 point, les demi-fautes sous 25, les fautes franches au-dessus de 93 (`analysis.md`), donc la ligne est quelque part entre 26 et 92. Ce qui resserrerait l'intervalle est des fautes vraiment intermédiaires, que le jeu n'a pas.

Effet à connaître : `09-walkin`, le /ŋ/ de *walking* dit /n/, tombe sous la ligne — il compte dans la proximité et pas dans l'intelligibilité, ce qui est le bon comportement, on comprend *walkin'*.

**Un son ajouté ou manquant vaut zéro dans les deux feuilles, par nature** : il n'a aucun point, n'ayant rien en face de lui à comparer (`embedded/Marks.kt`).

### La mélodie — une feuille, la distance

À chaque syllabe, l'écart de hauteur entre les deux courbes, chaque côté ramené d'abord à sa propre médiane, et la **moyenne simple** de ces écarts en demi-tons.

Trois méthodes écartées, chacune par un cas du banc. **La corrélation** ignore l'amplitude, donc qui fait la bonne forme deux fois trop petite obtient un score parfait — or le cas 22, la phrase dite plate à la française, est étiqueté comme une faute. **La moyenne des carrés** laisse une divergence isolée écraser le reste. **Le recalage temporel élastique** compare deux courbes décalées dans le temps, alors qu'elles sont déjà alignées syllabe par syllabe — et il pardonnerait à qui met sa montée sur la mauvaise syllabe.

**Portée : toute la phrase, et ce n'est pas mesuré.** La brique 10 ne lit aujourd'hui que la région voisée finale (`analysis.md`), et le contour par syllabe n'a jamais été confronté à des étiquettes. On l'étend quand même : une échelle qui ne parle que là où elle est sûre laisse passer les fautes qu'elle est là pour attraper, et l'emphase ne vit pas à la fin de la phrase.

**Une seconde feuille reste candidate et n'est pas livrée : les mouvements.** Elle serait à la mélodie ce que l'intelligibilité est aux sons, et le test d'inversion passe sur le papier — *« colle au contour »* contre *« fais les mouvements »*. Deux raisons de ne pas la livrer : elle **ne lit pas les mêmes éléments** que la distance, et le banc ne porte que deux prises étiquetées de mélodie.

**Sa piste, si elle s'ouvre : la forme générale de la phrase.** Quelques mouvements porteurs par énoncé plutôt qu'un par syllabe. Les **plages sont celles que délimitent les accents du modèle**, une hauteur représentative par plage, et on compare les mouvements d'une plage à la suivante — **on découpe sur le modèle et on lit l'apprenant sur les mêmes plages**, sans quoi les découpes seraient incomparables et l'on pardonnerait de mettre sa montée sur la mauvaise syllabe. La largeur des plages porte la tolérance.

**Le rythme est retiré, y compris des candidates.** Le défaut qu'il visait — ne pas comprimer les syllabes inaccentuées — **se voit déjà dans les sons** : un schwa dit en voyelle pleine est un autre son. Ce qu'il ajouterait est le contraste de durée sans changement de phonème, marginal, et il chevaucherait l'accent lexical.

### L'accent lexical — un marquage au mot, binaire

L'appui est tombé sur la bonne syllabe ou ailleurs. Le dénominateur est **les mots de plus d'une syllabe que le modèle accentue nettement** : un monosyllabe n'a pas de choix d'accent, un mot outil n'a pas d'appui net même chez le modèle. **La barre est posée à 0,90** sur la marge de la sonde côté modèle — entre 0,80 et 0,94 le balayage rend la même chose, et 0,95 jetterait en plus un mot que l'oreille entend franc (`analysis.md`, brique 7).

La brique tourne dans l'app ; **le branchement de la feuille dans la somme reste dû**. Tant qu'il n'est pas fait, elle sort de la somme au lieu de valoir zéro.

## Correction et pertinence — deux feuilles, un seul marquage

Le juge marque **par groupe de mots**, une seule fois pour les deux aptitudes, et chaque empan porte **une** étiquette par aptitude. L'app déplie chaque empan en ses mots ; un mot qui ne porte rien vaut `ok`, qui est un cran et non une absence. Le dénominateur est **les mots retenus du passage**.

**Une seule feuille par échelle, et non une par étiquette, parce que les étiquettes se partagent les mêmes mots** : ce sont des tranches d'un gâteau, pas des mesures indépendantes. En feuilles séparées, chacune devait rendre un chiffre même quand rien n'était marqué de sa couleur, et une étiquette rare se retrouvait à sa valeur extrême sur presque tous les passages, déplaçant l'échelle du nœud. **La colonne s'ancre par une phrase** : *un passage entièrement fait de ça vaudrait…*

**Le juge rend le cran, et rien de plus fin.** Sur trois mots, « haut de plat » n'est pas vérifiable. La finesse vient du comptage — combien de mots à quel cran, sur quelle longueur. Coût assumé : un passage de trois mots portant un seul groupe marqué a une note très grossière. C'est une grossièreté vraie, pas une fausse précision.

**Deux empans qui se recouvrent ne comptent pas deux fois** : un mot prend le pire cran qu'il porte, et il compte une fois.

Faire découper toute la phrase par le juge est écarté : couper du correct n'est vérifiable par personne — *to the market* fait un morceau ou trois selon l'habitude de coupe, et le chiffre bougerait avec elle. Marquer *I go* comme groupe verbal mal formé, ça, se vérifie.

### La correction — est-ce que c'est de l'anglais ?

| cran | ce qui le décide |
|---|---|
| `ok` | rien à signaler |
| `mal formé` | la phrase n'est pas bien montée — *I go there yesterday*, *I make my homework* |
| `ne se dit pas` | ça n'existe pas dans la langue |

Précédence : `ne se dit pas`, puis `mal formé`, puis `ok`.

**C'est un jugement absolu, donc elle ne prend aucune consigne.** Une consigne est toujours une exigence de situation : *« parle au passé »* ne fait pas de *I'll go there* une phrase mal formée.

**« Absolu » ne veut pas dire indépendant de toute norme.** Rien n'est correct dans l'abstrait — *I ain't got none* est fautif en anglais standard et bien formé dans plusieurs dialectes, *Going out later?* est de l'oral normal. Ce qui est vrai est que **la norme est fixée par l'app et identique pour toutes les activités** : aucune consigne, aucune scène, aucun défi ne la déplace. C'est cette invariance qui la rend vérifiable au banc sur des phrases isolées. **Cette norme n'est écrite nulle part aujourd'hui** — elle est celle que le modèle se donne seul (`../TODO.md`).

**Pas de cran au-dessus d'`ok`.** *Une construction difficile montée juste* n'a aucune norme fixe : difficile dépend de qui parle, et l'app n'a pas de niveau d'apprenant. Une construction ambitieuse et réussie se fait marquer `juste`, du côté de la pertinence, où le critère est situationnel par construction.

**Aucun découpage par cause** — temps, accord, préposition —, une liste de causes n'étant jamais complète et finissant par un tiroir « autre » qu'on ne saurait pas peser. Et **aucun découpage par nature d'empan** : viser une partie de la phrase est un acte de situation, donc ça se dit en consigne côté pertinence. Coût assumé, c'est grossier — *I go there yesterday* et *I goed there* tombent au même endroit.

**`ne se dit pas` a un effet qui ne transite par aucune note** : au moins un mot de ce cran et l'analyse du son ne tourne pas, quels que soient les poids (`activity.md`).

### La pertinence — as-tu visé juste ?

| cran | ce qui le décide | valeur d'exemple |
|---|---|---|
| `juste` | vise et touche : la tournure que le natif aurait choisie — *I popped into the shop* | 1,00 |
| `ok` | correct, sans plus | 0,90 |
| `plat` | vise bon mais mollement : vague, basique, ou repris — *I did a thing*, *nice* pour la troisième fois | 0,50 |
| `à côté` | ne vise pas ce qu'il fallait : le ton, la situation, la consigne — *Hey mate* à un client | 0,10 |

Sur *« Yeah, I went there last summer. It was nice »* — neuf mots, un mot mou : `(8 × 0,90 + 0,50) / 9 = 0,86`.

Précédence : `à côté` devant `plat` devant `juste`. Le cas qui décide est le groupe idiomatique mais hors ton — *crack on with it* dit à un client est de l'anglais précis et faux ici, et l'exclusion le range en `à côté`. Prix assumé : *stuff*, vague **et** trop familier, n'apparaît pas comme mollesse.

**C'est elle qui porte la consigne**, donc tout ce qu'un défi veut exiger. La consigne se pose sur le **marquage** et non sur la feuille, et elle ne peut **qu'endurcir** : la précédence va de `à côté` vers `juste`, donc une consigne fait passer des groupes vers le cran plus sévère. « Ignore les répétitions » reste inécrivable.

**C'est la seule mesure du projet qui ait un bon côté** : pour le son le modèle est la vérité, donc on ne peut pas faire mieux que lui ; pour une phrase il n'y a pas de modèle unique, donc on peut dépasser le simplement correct. Le cran haut est ce qui donne un sens à A. **Il ne vaut que s'il est rare** — un juge qui en donne un passage sur deux le rend décoratif, et **personne ne sait à quelle fréquence ce juge-là en donnera**.

**Le registre, la variété, la rareté et la complexité de la phrase n'ont pas de cran à eux** : le registre est ce que `à côté` dit, la répétition et la platitude ce que `plat` dit. Un cran par notion aurait compté deux fois les mêmes mots.

**Un même mot peut être marqué par la correction et par la pertinence**, et son poids s'ajoute dans les deux. *« I make a thing »* est mal formé **et** plat : deux défauts réels. C'est la seule exception au principe qu'aucun mot n'entre dans deux feuilles dont les poids s'additionnent.

## Compréhension — le suivi

C'est la seule aptitude qui ne porte pas sur ce que l'apprenant produit : comprendre est ce qui **entre**, et l'app ne peut que déduire de la réponse.

**Une feuille, un cran par passage, jugé.** C'est la seule feuille dont la finesse ne peut pas venir du comptage — un seul élément, donc la précision *est* celle du cran. D'où six crans là où trois suffisent ailleurs. Un seul axe : **ce que la réponse prouve d'avoir pris**. Sur le tour *« I finally got the tickets, but only for the Sunday matinée »* :

| cran | ce qui le décide | exemple |
|---|---|---|
| `entre les lignes` | répond à ce qui était sous-entendu, pas dit | *« Don't worry, I'll drive you home »* |
| `précis` | reprend un élément que seul quelqu'un qui a entendu pouvait reprendre | *« Sunday? I thought you wanted the evening one »* |
| `en rapport` | répond à ce que le tour disait, sans reprendre de détail | *« Oh nice, are you going? »* |
| `sur le sujet` | accroche le thème, pas le tour | *« Yeah, tickets are so expensive these days »* |
| `vague` | aurait marché quoi qu'on lui dise | *« Yeah, exactly »* |
| `à côté` | preuve négative : répond à autre chose | *« I went to the cinema last week »* |

**Un axe et un seul.** Un cran *partiel* mesure combien du tour a été couvert, pas la force de la preuve : deux axes dans une échelle qui ne se lit plus. Le cas qu'il servait — le tour qui porte une remarque *et* une question — est un **jugement** du juge, qui pèse si l'apprenant a pris ce qui appelait une réponse.

**Le cran `vague` est ce qui fait tenir la feuille.** Sans lui, la réponse plausible qui n'engage rien devait être comptée bonne ou fausse, et les deux étaient faux : on peut tenir une conversation entière sur *yeah*, *right*, *I think so*.

**Le haut est offert sur tous les tours** : tout tour porte du contenu précis, donc `précis` est atteignable partout. Un cran du milieu parfois indisponible ne gênerait personne ; un cran du **haut** indisponible punirait tout le monde pour une occasion que personne n'a donnée. C'est pourquoi `entre les lignes` se pose **juste au-dessus** de `précis` : un tour sans sous-entendu plafonne à `précis`, et ce manque doit coûter presque rien.

**Ce qui est jugé n'est pas la qualité de la réponse mais ce qu'elle montre d'avoir pris.** *« Yes, I go yesterday at the shop »* est mal dit et parfaitement en rapport ; *« I like the cinema very much »* est correct et à côté. Et la brièveté ne se marque jamais : *« Did you get the tickets? » — « Yeah »* est spécifique à ce qui a été demandé.

**L'objet est le dernier tour de l'IA ; le contexte est la conversation.** Répondre à la question d'il y a deux tours est `à côté` — mais le juge a besoin du fil pour résoudre les pronoms et les ellipses.

**Le suivi pèse sur la difficulté du tour de l'IA, que le modèle rend avec sa réponse.** La longueur seule était grossière — *« Fancy a cuppa? »* est plus dur que quarante mots simples — et la position du levier de complexité ne la remplace pas : elle demande un niveau, elle ne promet pas que chaque phrase soit dure. La difficulté **remplace** la longueur, qui fait partie de ce que le modèle pèse en la rendant ; garder les deux compterait deux fois la même chose. C'est un **poids, jamais une feuille** — il dit ce qu'on a envoyé, pas ce que l'apprenant a fait — il se stocke avec la version de ce qui l'a produit, et ses limites vont avec : personne ne le vérifie, il ne se rejoue pas, c'est le modèle qui note ce qu'il vient d'écrire.

Deux cas de bord. Un passage **sans tour de l'IA devant lui** n'a rien à avoir compris : la feuille est **absente**. Et le **nombre d'écoutes** ne donne pas de note, c'est un compte.

**Le suivi n'est jamais absent pour cause de texte affiché.** Voir le tour de l'IA est une aide en moins à trouver, pas une mesure qui disparaît : il reste à comprendre la langue.

## Fluidité — quatre feuilles

**Rien n'y est compté deux fois** : `continuité` possède tout le silence **entre les mots**, `le plus long silence` possède le blocage, le `débit` ne lit que le temps où la bouche articule, `le remplissage et les reprises` possède les hésitations.

### La continuité

La part du tour passée en silence, **moins celle du modèle sur la même phrase**, en points de pourcentage, et le chiffre peut être négatif.

Comparer au modèle fait un vrai travail : ce qui reste chez lui est les **pauses prosodiques légitimes**, à la virgule, en fin de proposition — le modèle s'arrête là, l'apprenant a le droit aussi. L'écart s'écrit en **différence** et non en rapport, la part silencieuse du modèle pouvant tomber à un ou deux pour cent sur une phrase courte, où un rapport devient hypersensible.

Se taire **moins** que le modèle n'est pas une faute et rien n'a besoin d'être écrêté : un écart négatif tombe au-dessus de la borne A, dans la même bande que zéro.

**Une pause est un silence entre deux mots qui dure au moins 200 ms**, et chaque condition fait son travail. La frontière des mots écarte ce qui se passe **à l'intérieur** d'un mot — une occlusion, une tenue, un bégaiement tenu ne sont pas des pauses. Le seuil écarte les micro-blancs entre les mots : le /t/ de *to stop* ferme la bouche 50 à 150 ms, et sans seuil une phrase riche en /p t k/ paraîtrait moins continue qu'une phrase pleine de voyelles — pour son texte, pas pour son locuteur, et cette feuille se lisant dans l'absolu, le biais ne s'annulerait nulle part. **Ce que la frontière écarte ne se perd pas** : un silence mi-mot reste dans le temps de parole, donc dans le débit. Le seuil est partagé avec lui : il exclut exactement ce qu'elle compte.

**Un délai de grâce d'une seconde s'applique aux deux bords, jamais à l'intérieur.** Sans lui, le temps de réagir et de cliquer — le même pour tout le monde — pèserait bien plus sur un tour court : deux secondes sur *« Yes, I did »* font 50 %, les mêmes deux secondes sur vingt ne font presque rien, pour un comportement identique. La seconde n'est pas mesurée. Et **elle ne touche pas ce qu'une condition lit** : un silence de 6 s reste un silence de 6 s.

**Le silence final compte, et c'est voulu : rendre la parole est un acte.** Dans une conversation réelle on signale qu'on a fini, et tarder à le faire est un vrai défaut.

### Le plus long silence

Un seul élément, sa durée en secondes, et la série se lit en secondes : *« à partir de trois secondes, c'est un C »*.

Le test d'inversion la sépare de la continuité : **A** passe 40 % du tour en silence mais jamais plus de 0,8 s d'affilée ; **B** en passe 25 % dont un blanc de 5 s. La continuité met A derrière, le plus long silence met B derrière — *« ne laisse pas de trous »* contre *« ne reste jamais bloqué »*.

Deux formes écartées : moyenner les silences comme éléments laisse le tour le plus fluide sans aucun élément, donc sans feuille ; et une part du temps passée dans un blanc serait diluée par la longueur du tour — le même blanc de 5 s ferait 50 % dans un tour de 10 s et 17 % dans un de 30 s.

Ce qu'elle ignore, assumé : deux blancs de 5 s valent un seul. C'est la continuité qui porte l'accumulation.

### Le débit

L'écart de vitesse au modèle, en pourcentage, sur les seuls mots retenus.

```
v     = temps de parole de l'apprenant ÷ temps de parole du modèle
écart = (le plus grand de v et 1/v) − 1
```

**Comparé au modèle et non dans l'absolu**, ce qui le remet sous la règle générale du projet. Le texte étant le même des deux côtés, il n'y a **rien à compter**, ni mots ni syllabes : l'unité s'annule, et la question « combien de mots par seconde parle un bon apprenant », qui n'a pas de réponse indépendante du texte, disparaît.

**Symétrique par construction** : deux fois plus lent et deux fois plus rapide donnent tous deux 100 %. Savoir de quel côté on s'écarte ne se perd pas pour autant — la **marque** le lit (les chevrons), et une **condition** distingue *« jamais plus de 20 % plus lentement »* de *« pas plus vite »*. **Hypothèse à vérifier** : que les deux côtés soient également gênants. La lenteur est le défaut du francophone, l'excès de vitesse est rare ; si la mesure dit qu'ils ne se valent pas, la série devient deux listes au lieu d'une.

**Le temps de parole exclut les silences des deux côtés**, donc le silence n'est jamais compté deux fois. **Détacher ses mots au lieu de les lier** se fait donc voir par la **continuité**, sous forme de petites pauses — ce qui est plus juste, détacher ses mots étant littéralement en fabriquer. Le débit garde ce qu'il est seul à voir : **articuler lentement à l'intérieur des mots**, voyelles étirées et consonnes sur-prononcées, tenues fermées comprises.

**Et il ne lit que les mots retenus.** Le temps brut contient les *um* et les morceaux abandonnés que le modèle ne dit pas : un tour hésitant aurait un gros écart de vitesse et serait pénalisé **en plus** de sa propre feuille.

### Le remplissage et les reprises

**Une feuille dont les crans partagent les mots prononcés** — un mot prononcé est retenu, ou abandonné, ou du remplissage.

| cran | ce qui le décide |
|---|---|
| `retenu` | fait partie de la phrase |
| `abandonné` | appartient à un morceau repris — *I went to the—* |
| `remplissage` | *um*, *like*, *I mean* employés comme béquille |

Sur *« It was, like, um, I went to the— I was going to the store »* : quatorze mots prononcés, deux de remplissage, quatre abandonnés, huit retenus. **Ça règle les deux dénominateurs** — une seule feuille, sur les quatorze, rien à réconcilier.

**Répéter et repartir sont un seul cran** : répéter, c'est repartir avec les mêmes mots. Les séparer demanderait de dire à partir de quel mot changé on quitte l'un pour l'autre.

**C'est jugé et jamais calculé sur une liste de mots.** *euh* et *um* n'ont pas d'autre emploi, mais *I mean*, *like*, *well*, *actually* sont de vrais mots — *« I mean what I say »* et *« it was, I mean, hard »* ne diffèrent que par l'emploi, et seul le juge le voit. La feuille prend par là une **consigne**, ce qu'une feuille calculée ne peut pas prendre : *« aucun mot du genre I mean »* s'écrit telle quelle.

**La voix modèle ne dit que les mots retenus** : lui faire dire les hésitations est exclu, le modèle étant ce qu'on donne à imiter. **L'apprenant, lui, s'aligne sur tout ce qu'il a dit** — sans les hésitations dans le texte, ces bouts d'audio n'auraient aucune lettre en face et deviendraient des sons en trop, donc des gros ratés : hésiter coûterait une note de prononciation. **On aligne sur les quatorze, on compare sur les huit.** Découper l'audio de l'apprenant pour n'en garder que les huit mots est refusé par l'invariant du projet — rien ne se fait à un seul des deux audios.

**Prix assumé : aux coutures, un ou deux sons sont comparés hors de leur contexte.** Le *I* retenu qui suit un *the* abandonné n'a pas devant lui ce que le modèle a devant le sien. Ça ne se propage pas (`analysis.md`, brique 11) et la redite l'annule — une raison de plus de faire redire un tour hésitant.

**Le bafouillage ne coupe pas l'analyse du son** : ce qui la coupe est que la phrase va être *réécrite*. Ici les mots retenus sont les bons, ils ont seulement été dits en trébuchant.

**Ces crans ne survivent qu'à une reconnaissance verbatim.** Un moteur qui nettoie les *euh* les rend muets sans le dire, et la fluidité paraîtra excellente ; et depuis qu'on aligne l'apprenant sur tout ce qu'il a dit, un nettoyage silencieux fait aussi rater le placement des sons. Le critère de fidélité verbatim du banc pèse donc aussi sur la mesure des sons (`../TODO.md`).

## Ce qui ne donne pas de note

Certaines feuilles se calculent et n'entrent dans aucune note : elles existent pour les **conditions**, qui les lisent comme n'importe quelle autre. Ni sensibilité, ni poids.

- **Le tour interrompu** — ce tour a-t-il été envoyé. Un seul élément, vrai ou faux. Deux causes et une seule feuille : le silence de plus de x en troisième position de capture, et le plafond de durée, qui existe aux trois. Un chiffre à deux valeurs ferait un mauvais membre de moyenne, et son poids ferait basculer l'aptitude entière d'un coup.
- **Les comptes** — les redites, les reformulations, le nombre d'écoutes du tour de l'IA. Des entiers, qui ne se normalisent pas, et surtout qui **montent à chaque tentative** : pesés dans une aptitude qui fait refaire, la note ne peut plus repasser la barre et l'apprenant brûle ses tentatives sans issue. Un défi qui veut la réussite d'emblée l'écrit en **condition**.

**La part des tours interrompus d'une séance n'est pas la feuille**, c'est ce qu'une lecture en fait : une feuille se calcule toujours sur un passage.

## Ce qui est jugé, ce qui est calculé

**Trois marquages viennent du modèle de langue** — les empans de langue, dont l'app tire la correction et la pertinence ; le bafouillage, dont elle tire le remplissage et les reprises ; le suivi. Tout le reste se calcule.

**Le juge rend un cran, jamais un pourcentage** : « 72 % de suivi » n'est vérifiable par personne, et la finesse vient du comptage.

**Un cran de plus revient du modèle et n'est pas une mesure** : la difficulté de son propre tour, qui sert de poids au suivi. Il ne dit rien de l'apprenant, donc pas de feuille, pas de sensibilité, pas de consigne.

**Seul un marquage jugé prend des consignes** — du texte libre qui entre dans le critère que le juge lit. Il y a donc trois endroits où elles s'accrochent, et sur les empans une consigne ne touche que la **pertinence**.

**Un marquage en porte plusieurs à la fois, chacune avec sa durée** — éternelle, ou un nombre de passages. Un patch en pose et en retire : *« au passage 4, tu lui mens sur tes intentions, pour trois passages »*. Une seule place remplacée ne marcherait pas dès que deux consignes n'ont pas la même durée. Le juge les reçoit toutes ensemble, et elles ne peuvent qu'endurcir, chacune comme l'ensemble. **Non mesuré, et impossible à mesurer au banc** : que deux consignes posées ensemble durcissent au lieu de se gêner est une propriété de leur combinaison.

**Une consigne a trois lecteurs, et c'est ce qui la distingue des autres effets** : l'apprenant, qui doit la lire pour la suivre ; le personnage, qui joue avec ; le juge, qui note. **Elle est donc toujours affichée**, celle du départ comprise — être noté sur un critère qu'on ne t'a pas dit donne une note qu'on ne peut pas relire. Et le journal dit laquelle était debout à quel passage.

Ce qui est posé ne vaut que pour la **suite** : un passage déjà dit n'est jamais rejugé.

**Le poids choisit parmi ce qui est mesuré, la consigne reformule ce qui est jugé.** Un défi « prononce tous les -ed finaux » ne s'écrit donc pas en consigne sur les sons, que l'écart au modèle voit déjà, mais en creusant l'arbre pour ne peser que ces sons-là.

**La consigne fait partie de la situation, pas des réglages** : deux personnes dans la même activité reçoivent le même traitement, et c'est ça que l'invariance protège. Elle n'a ni positions ni phrase lisible, donc elle n'est pas un levier — c'est un `brief` par marquage jugé.

**Une consigne n'est qu'additive.** Pour qu'une feuille compte moins, il y a le poids, et lui seul. « Ignore les temps » ferait adoucir un verdict par un texte.

**Une consigne est ce qu'un juge lit, et rien d'autre ne porte ce nom.** L'**objectif** — *« obtiens le prix de la chambre »* — n'entre dans le critère d'aucun juge : il se dit dans le `brief`, se constate par un déclencheur, se conclut par une fin. Et les **deux phrases d'un patch** annoncent un changement au lieu de poser un critère.

**Une feuille sous consigne n'est pas vérifiée par le banc**, qui éprouve le critère par défaut. Coût connu.

Trois défis que ça écrit sans champ neuf. « 100 % passé » est une consigne sur le marquage et fait tomber les mots concernés en `à côté` — *I'll go there* s'y range, ce qui ne ment pas sur la langue. Le **mot interdit** est une consigne du même marquage. Le **registre** aussi, comme la longueur imposée.

## La grille des mesures

Trois conditions pour qu'une feuille existe : elle **se calcule toujours**, quelle que soit l'activité, sinon la configuration d'un défi déciderait quels champs existent ; elle **veut dire quelque chose sans aucun défi** ; quelqu'un pourrait vouloir la **noter seule**.

**Aucune feuille n'a deux sens.** Une feuille dont la direction devrait s'inverser selon l'activité est la mauvaise feuille : *« explique ça à un enfant de huit ans »* ne demande pas la pertinence à l'envers, il demande de s'adapter à qui écoute, ce que `à côté` dit déjà.

Aucune branche n'est close. **Les valeurs des échelles sont des ordres de grandeur, à mesurer.**

**Élocution**

| feuille | éléments | valeur d'un élément | chiffre | échelle de la série |
|---|---|---|---|---|
| intelligibilité | les sons | marche à 30 points | part des sons intelligibles | 0,80 → 1,00, tassée en haut |
| proximité | les sons | rampe continue, 0 → 100 points | moyenne | 0,65 → 1,00, étalée |
| mélodie | les syllabes | — | écart moyen, en **demi-tons** | 0 → ~6 *(bas est bon)* |
| accent lexical | les mots de plus d'une syllabe que le modèle accentue nettement | vrai/faux | part des mots bien accentués | 0,70 → 1,00 |

La branche creuse gratuitement : la grille des sons est un inventaire fermé déjà attaché à chaque marque, donc « travaille tes *th* » est un poids posé sur deux colonnes, sans juge et sans liste à inventer.

**Correction et pertinence** — deux feuilles sur un seul marquage jugé.

| feuille | éléments | valeur d'un élément | chiffre | échelle de la série |
|---|---|---|---|---|
| correction | les mots retenus | colonne : `ok` / `mal formé` / `ne se dit pas` | moyenne | 0,80 → 1,00, tassée en haut |
| pertinence | les mots retenus | colonne : `juste` / `ok` / `plat` / `à côté` | moyenne | 0,40 → 1,00 |

**Compréhension**

| feuille | éléments | valeur d'un élément | chiffre | échelle de la série |
|---|---|---|---|---|
| suivi | le passage (un seul) | colonne : six crans | la valeur du cran | 0 → 1,00 |

**Fluidité**

| feuille | éléments | valeur d'un élément | chiffre | échelle de la série |
|---|---|---|---|---|
| continuité | le temps du tour | silence entre les mots, ou parole — seuil à 200 ms | écart au modèle, en **points de %** | −5 → ~40 *(bas est bon)* |
| le plus long silence | un seul | — | durée, en **secondes** | 1 → ~8 *(bas est bon)* |
| débit | un seul | — | écart de vitesse au modèle, en **%** | 8 → ~85 *(bas est bon)* |
| remplissage et reprises | les mots prononcés | colonne : `retenu` / `abandonné` / `remplissage` | moyenne | 0,70 → 1,00 |

**Ce qui se calcule sans donner de note** — le tour interrompu, le nombre de redites, de reformulations et d'écoutes.

Deux collisions écartées, à ne pas rouvrir. **Le délai avant de parler ne se lit qu'une fois**, par la continuité ; en faire aussi une mesure de compréhension compterait deux fois le même silence. Et **l'allongement de syllabe** — « I waaaant » — n'a plus de feuille pour le lire depuis que le rythme est retiré ; il reste dehors.

## Les noms d'écran

Ce qu'un écran affiche n'est pas la clé de la feuille mais un nom court, sans plafond de largeur — la mesure se cale contre la lettre et court vers la gauche autant que le nom le laisse (`ui.md`). Le critère est comment ça se lit à l'écran, jamais l'exactitude du terme.

| feuille | EN | FR |
|---|---|---|
| `intelligibility` | Clarity | Clarté |
| `proximity` | Precision | Précision |
| `melody` | Melody | Mélodie |
| `lexical-stress` | Stress | Accentuation |
| `correctness` | Form | Forme |
| `relevance` | Choice | Choix |
| `uptake` | Reply | Réponse |
| `continuity` | Continuity | Continuité |
| `longest-silence` | Longest gap | Silence max |
| `pace` | Pace | Débit |
| `stumbling` | Hesitations | Hésitations |

| branche | EN | FR |
|---|---|---|
| `pronunciation` | Pronunciation | Prononciation |
| `understanding` | Understanding | Compréhension |
| `correctness` | Grammar | Grammaire |
| `relevance` | Relevance | Pertinence |
| `fluency` | Fluency | Fluidité |

**Trois branches n'ont qu'une feuille, et c'est ce qui rend leurs noms difficiles** : le titre a déjà pris le nom du domaine, donc la feuille doit dire autre chose. Et pour deux d'entre elles, la correction et la pertinence sortent d'un seul marquage sur les mêmes éléments — leur nom ne peut donc pas être l'unité, les deux diraient *Mots*, il doit être la **question**. D'où `Forme` contre `Choix` : *est-ce que c'est de l'anglais*, puis *était-ce l'anglais qu'il fallait*.

Cinq choix à ne pas rouvrir.

- **`Grammar` / `Grammaire` pour `correctness`.** Le terme du code est large et exact, il couvre la syntaxe et l'idiome, mais il se lit à l'écran comme le geste de corriger — *« en cause : la Correction »* se comprend *« on t'a corrigé »*. `Langue` a été écarté pour l'inverse, exact et fade. Coût borné : quand la faute est idiomatique, le nom trompe sur le *pourquoi*, et la marque dit toujours *quels mots*.
- **`Elocution` est un faux ami** : en anglais il désigne l'art de parler en public. `Pronunciation` couvre exactement la branche, prosodie comprise. Le français prend `Prononciation` parce que c'est le mot que l'apprenant connaît ; `élocution` reste celui de la conception.
- **`Stress` / `Accentuation`**, pas `Accent` des deux côtés : en français, `Accent` nu se lit *accent régional*, le contre-sens même que `Grammar` évitait.
- **`Continuity` et `Reply` sont fades exprès.** `Flow` / `Fluidité` appartient à la branche au-dessus, et une feuille ne porte pas le nom de sa branche sur l'écran qui les empile. `Preuve` aurait tenu le registre du doc mais il est froid ; le risque assumé est que `Réponse` ne dit pas que ce qui est jugé n'est pas la qualité de la réponse. La liste des six crans, elle, le dit.
- **`Relevance` est le maillon faible, gardé sciemment.** En anglais il tire vers *hors sujet*, quand la feuille marque aussi un mot **plat qui est dans le sujet**. Deux choses le rattrapent : dans une liste de cinq il se lit *as-tu dit ce qu'il fallait*, et la feuille juste dessous dit `Choice`. `Aptness` serait exact et se lit mal.

**Les noms mélangent la dimension et la faute, et c'est assumé.** `Clarity`, `Melody`, `Pace` nomment une dimension ; `Hesitations` et `Longest gap` nomment ce qui a raté. Tout convertir en dimensions échoue sur la fluidité, dont les quatre feuilles parlent du même tissu et où le vocabulaire manque. L'unité de l'écran de bilan se tient par la mise en page, pas par les mots.

## Les jetons de code

**Les clés du code sont en anglais, la prose de conception reste en français** — la règle de `dev_base` telle quelle. Les **clés** sont donc écrites en anglais dans les docs aussi, un nom entre accents graves désignant un identifiant : `pronunciation/melody`, `correctness.sends-back`.

Les **crans** sont l'inverse : nommés en français dans la conception, portés par un jeton anglais dans le code. Les positions de leviers se traduisent d'elles-mêmes, les crans non :

| cran | jeton |
|---|---|
| `ne se dit pas` | `not-said` |
| `mal formé` | `malformed` |
| `à côté` | `off-target` |
| `plat` | `flat` |
| `juste` | `apt` |
| `retenu` | `kept` |
| `abandonné` | `abandoned` |
| `remplissage` | `filler` |
| `entre les lignes` | `implied` |
| `précis` | `precise` |
| `en rapport` | `on-point` |
| `sur le sujet` | `on-topic` |

**`cadence` devient `tempo` et pas `pace`** : la feuille `pace` occupe déjà le mot. Le débit est ce qu'on fait spontanément, la cadence une exigence de tenir un temps donné.
