# Les mesures et les notes

Ce que l'app observe d'un passage, et comment ça devient une note.

**Ce qui est écrit ici et ce qui est écrit dans le code.** `sheets/Sheets.kt` est l'arbre des feuilles : chacune y déclare ses éléments, sa lecture, son unité, sa direction et sa série, et les valeurs sont là et nulle part ailleurs. `sheets/Sheet.kt` dit ce qu'être une feuille demande, `notes/Note.kt` l'échelle A–E, `notes/Sensitivity.kt` la série et sa fenêtre, `notes/Aggregate.kt` les poids, `notes/Reading.kt` ce qu'une tentative fait de chaque feuille. Ce doc porte ce qu'un fichier ne peut pas dire : ce qui traverse, et **pourquoi chaque feuille a la forme qu'elle a** — les formes essayées puis écartées, qui coûtent une journée chaque fois qu'on les rouvre.

La règle qui gouverne tout : la mesure ne se règle pas, la marque l'affiche telle quelle, la note seule est une lecture réglée (`reference.md`).

## Ce qui traverse

**La note vit sur la mesure, pas sur l'aptitude.** Un défi qui ne noterait que l'accent tonique rendrait sinon une note « élocution » qui ne veut pas dire la même chose que celle du défi d'à côté, sans que rien à l'écran ne le dise. L'aptitude reste un tiroir — pour choisir, pour afficher — et n'est plus l'unité de la note.

**La lettre est toujours une note, jamais une mesure**, puisque la sensibilité déplace ses bornes. Les deux ne portent donc jamais les mêmes noms, et **on garde les nombres et les crans en base, jamais les lettres** : les bornes pourront bouger sans abîmer les vieilles séances. Le modifieur `+`/`−` est d'affichage seulement : rien ne le stocke, et une condition nomme une lettre.

**L'agrégation se fait une fois, à plat, sur les feuilles réellement présentes — et « à plat » porte sur la somme, jamais sur les poids.** Un poids est une **part parmi ses frères**, et les parts se multiplient en descendant (`notes/Aggregate.kt`) : le même poids partout veut donc dire que chaque aptitude compte pareil, et non que chaque feuille compte pareil. Sans ça une branche pesait plus pour avoir plus de feuilles sous elle — `pronunciation 1` contre `correctness 1` donnait quatre fois la parole à la prononciation, que personne n'avait choisie. Une feuille de plus sous une branche partage donc la part de cette branche et **ne touche aucune autre aptitude**. Les lettres d'aptitude et de passage sont la même formule restreinte à un sous-arbre : des lectures, pas des étapes de calcul. Une cascade de moyennes redistribue en silence dès qu'une feuille manque — et il en manque tout le temps. Sur deux passages dont le second a la porte des mots fermée, avec élocution 2 (sons 1, mélodie 1) et correction 1 : en cascade la correction finit par peser deux tiers de la séance, l'inverse exact du 2:1 demandé ; à plat, `(40 + 80 + 90 + 50) / 4 = 65` rend bien le 2:1.

**Chaque feuille pèse par la longueur du passage**, comptée en **mots retenus** — un passage dont la porte a coupé l'analyse n'a pas de sons et a toujours des mots. Sauf le suivi, qui pèse sur la difficulté du tour de l'IA, la matière qu'il couvre.

**Entre feuilles, la moyenne décide seule** : pas de plancher qui plafonnerait la note dès qu'une feuille passe sous la barre. Une bonne feuille peut masquer une mauvaise, et c'est accepté — dans un défi, peu de feuilles comptent, et la sensibilité de chacune dit à quel point elle est facile à tenir.

**Les poids sont constants pour toute la séance.** Prix réel : un défi qui ouvre ses exigences une par une ne peut pas ne noter que ce qui était demandé au moment où on parlait. L'exercice à étages s'écrit par les **conditions**, qui ne touchent aucun poids : ce qui s'ouvre en cours de route est ce qui bloque, pas ce qui compte.

**Peser une feuille qui rend presque toujours la même valeur déplace toute l'échelle du nœud**, et ça se dit à qui écrit un arbre plutôt que de s'interdire. Une feuille qui vaut 1,00 sur presque tous les passages est un 20/20 permanent : à poids égal avec une feuille qui bouge, la note du nœud ne descend plus sous la moitié, donc la barre à 0,60 ne se franchit presque plus par le bas.

**Jamais un nombre d'occurrences dans une note.** Deux gros ratés dans *Where is it?* et deux dans *I was thinking about going to the market* ne valent pas la même chose : sur quatre mots dont deux abîmés personne ne rattrape le sens, sur neuf le contexte répare. Le nombre ne disparaît pas, il change d'endroit — « zéro faute franche » est une **condition**.

**Deux feuilles sur les mêmes éléments ne se justifient que si deux défis veulent l'ordre inverse.** Une série est monotone : elle rééchelonne, elle ne réordonne jamais. Ce test d'inversion décide branche par branche, ci-dessous.

**Sur une feuille à un seul élément, les cinq lettres ne sont pas toutes atteignables, et c'est très bien** (acté le 2026-09-06). Le suivi a six crans, donc son chiffre ne prend que six valeurs, groupées en haut et étalées en bas : aux positions 0, 2 et 4 le C est hors d'atteinte, aux positions 1 et 3 le D et le B le sont. Aucune série ne le répare — huit valeurs dans cinq intervalles en laissent forcément un vide. Ce qui tient : sur une feuille continue les cinq lettres sont atteignables, sur une feuille à crans les deux bouts le sont.

## Élocution

### Les sons — deux feuilles, mêmes éléments, deux rampes

Le test d'inversion passe. Deux apprenants, cinquante sons : **A** a un accent épais mais reste compréhensible, tous ses sons à une vingtaine de points ; **B** a un accent propre et deux sons complètement faux qui changent le mot. Sur la moyenne des écarts A vaut 0,80 et B 0,93 ; sur la marche A vaut 1,00 et B 0,96. L'ordre s'inverse, donc aucun réglage d'une feuille unique ne rend les deux verdicts.

**L'intelligibilité** lit en marche d'escalier — le mot a changé, ou il n'a pas changé — et graduer reprendrait le travail de l'autre feuille, les deux disant la même chose en moins net. **La proximité** lit en rampe continue, gros ratés compris, donc sans aucun seuil à trouver. Ce sont les deux défis opposés du projet : *« fais-toi comprendre »* ne compte que ce qui change le mot, *« gomme ton accent »* compte tout.

**« Points » est l'écart ramené sur 100** : la distance entre deux répartitions de ressemblance, celle de l'apprenant et celle du modèle. Ce n'est pas une note de prononciation, c'est une distance entre deux lectures de la même machine.

**La ligne du gros raté est celle où la rampe de l'écran sature, 30 points, et c'est la même pour la feuille et pour l'écran** — ailleurs, deux lettres du même rouge plein compteraient différemment sans que l'apprenant ait aucun moyen de voir la différence. **30 n'est pas mesuré** : sur le jeu étiqueté les témoins sont à 0,3 point, les demi-fautes sous 25, les fautes franches au-dessus de 93 (`analysis.md`), donc n'importe quelle valeur entre 26 et 92 sépare aussi bien. Ce qui resserrerait l'intervalle est des fautes vraiment intermédiaires, que le jeu n'a pas.

Effet à connaître : `09-walkin`, le /ŋ/ de *walking* dit /n/, tombe sous la ligne — il compte dans la proximité et pas dans l'intelligibilité, ce qui est le bon comportement, on comprend *walkin'*.

**Un son ajouté ou manquant vaut zéro dans les deux feuilles, par nature et non par franchissement de ligne** : il n'a aucun point, n'ayant rien en face de lui à comparer.

### La mélodie — une feuille, la distance

Trois méthodes écartées, chacune par un cas du banc plutôt que par goût. **La corrélation** ignore l'amplitude, donc qui fait la bonne forme deux fois trop petite obtient un score parfait — or le cas 22, la phrase dite plate à la française, est étiqueté comme une faute : la seule faute de mélodie que le projet ait étiquetée disqualifie toute méthode qui normalise l'amplitude. **La moyenne des carrés** laisse une divergence isolée écraser le reste. **Le recalage temporel élastique** compare deux courbes décalées dans le temps, alors qu'elles sont déjà alignées syllabe par syllabe — et il pardonnerait à qui met sa montée sur la mauvaise syllabe, ce qui est justement une faute.

**Portée : toute la phrase, et ce n'est pas mesuré.** La brique 10 ne lit aujourd'hui que la région voisée finale (`analysis.md`), et le contour par syllabe n'a jamais été confronté à des étiquettes. On l'étend quand même : une échelle qui ne parle que là où elle est sûre laisse passer les fautes qu'elle est là pour attraper, et l'emphase ne vit pas à la fin de la phrase.

**Une seconde feuille reste candidate et n'est pas livrée : les mouvements.** Elle serait à la mélodie ce que l'intelligibilité est aux sons, et le test d'inversion passe sur le papier — *« colle au contour »* contre *« fais les mouvements »*. Deux raisons de ne pas la livrer : elle **ne lit pas les mêmes éléments** que la distance, donc le parallèle est plus faible ; et le banc ne porte que deux prises étiquetées de mélodie, donc la livrer serait décider avant de mesurer.

**Sa piste, si elle s'ouvre : la forme générale de la phrase.** Quelques mouvements porteurs par énoncé plutôt qu'un par syllabe, ce qui est la façon dont l'intonation fonctionne. Les **plages sont celles que délimitent les accents du modèle**, une hauteur représentative par plage, et on compare les mouvements d'une plage à la suivante — **on découpe sur le modèle et on lit l'apprenant sur les mêmes plages**, sans quoi les découpes seraient incomparables et l'on pardonnerait de mettre sa montée sur la mauvaise syllabe. La largeur des plages porte la tolérance, sans qu'on ait à la régler comme telle. Et elle mesurerait les **mouvements généraux**, jamais où sont tombés les appuis, qui est le travail de l'accent lexical.

**Le rythme est retiré, y compris des candidates.** Le défaut qu'il visait — ne pas comprimer les syllabes inaccentuées, le trait syllabique du français — **se voit déjà dans les sons** : un schwa dit en voyelle pleine est un autre son, donc un écart au modèle. Ce qu'il ajouterait est le contraste de durée sans changement de phonème, marginal, et il chevaucherait l'accent lexical, l'appui se signalant par la durée autant que par la hauteur.

### L'accent lexical

**Un marquage au mot, binaire** : l'appui est tombé sur la bonne syllabe ou ailleurs. Le dénominateur n'est pas « les mots » — un monosyllabe n'a pas de choix d'accent, un mot outil n'a pas d'appui net même chez le modèle — mais **les mots de plus d'une syllabe que le modèle accentue nettement**, à la barre d'éligibilité qu'une oreille a validée (`analysis.md`, brique 7).

**La marque est binaire** et 86 % des lectures saturent : il n'existe pas de « x % les plus marqués » à écrémer, un seuil ne ferait que reproduire la comparaison des deux élues.

La brique tourne dans l'app ; **le branchement de la feuille dans la somme reste dû** (`../TODO.md`). Tant qu'il n'est pas fait, elle sort de la somme au lieu de valoir zéro.

## Correction et pertinence — deux feuilles, un seul marquage

Le juge marque **par groupe de mots**, une seule fois pour les deux aptitudes, et chaque empan porte **une** étiquette par aptitude. L'app déplie chaque empan en ses mots ; un mot qui ne porte rien vaut `ok`, qui est un cran et non une absence.

**Une seule feuille par échelle, et non une par étiquette, parce que les étiquettes se partagent les mêmes mots** : ce sont des tranches d'un gâteau, pas des mesures indépendantes. En feuilles séparées, chacune devait rendre un chiffre même quand rien n'était marqué de sa couleur, et une étiquette rare se retrouvait à sa valeur extrême sur presque tous les passages, déplaçant l'échelle du nœud. Toute une machinerie a été construite puis démontée pour contourner ça — un bonus au seul numérateur, un coefficient par nœud, des poids nuls par défaut, une direction par feuille : **six pièces disparaissent avec la colonne.**

**La colonne s'ancre par une phrase** : *un passage entièrement fait de ça vaudrait…* C'est ce qui la rend écrivable et mesurable, là où une pondération posée dans le vide ne le serait pas.

**Le juge rend le cran, et rien de plus fin.** Sur trois mots, « haut de plat » n'est pas un jugement que quelqu'un pourrait vérifier. La finesse ne se perd pas, elle change d'endroit : elle vient du comptage. Coût assumé : un passage de trois mots portant un seul groupe marqué a une note très grossière. C'est une grossièreté vraie, pas une fausse précision.

**Deux empans qui se recouvrent ne comptent pas deux fois** : un mot prend le pire cran qu'il porte, et il compte une fois.

**Faire découper toute la phrase par le juge est écarté** : couper du correct n'est vérifiable par personne — *to the market* fait un morceau ou trois selon l'habitude de coupe, et le chiffre bougerait avec elle. Marquer *I go* comme groupe verbal mal formé, ça, se vérifie.

### La correction — est-ce que c'est de l'anglais ?

Trois crans, `ok` / `mal formé` / `ne se dit pas`, dans cette précédence à l'envers.

Qu'elle soit un jugement absolu, qu'elle ne prenne donc aucune consigne, et que la norme soit fixée par l'app plutôt qu'absolue dans le vide, est dans `sheets/Sheets.kt`. Ce qui n'y est pas : **cette norme n'est écrite nulle part**, elle est celle que le modèle se donne seul — d'où *I'm doing good* laissé passer *au motif que c'est familier*. Deux choses au moins s'y trancheront, la **variété** et le fait que **l'oral n'est pas de l'écrit** (`../TODO.md`).

**Pas de cran au-dessus d'`ok`.** *Une construction difficile montée juste* — *« If I'd known, I would have told you »* contre *« I didn't know, so I didn't tell you »* — est une notion réelle et n'a **aucune norme fixe** : difficile dépend de qui parle, et l'app n'a pas de niveau d'apprenant. L'étiquette casserait donc la propriété qui fait exister cette aptitude. Une construction ambitieuse et réussie se fait marquer `juste`, du côté de la pertinence, où le critère est situationnel par construction.

**Aucun découpage par cause** — temps, accord, préposition —, une liste de causes n'étant jamais complète et finissant par un tiroir « autre » qu'on ne saurait pas peser. Et **aucun découpage par nature d'empan** : viser une partie de la phrase est un acte de situation, donc ça se dit en consigne côté pertinence, et une préposition fautive se retrouve marquée des deux côtés, ce qui est déjà permis. Coût assumé, c'est grossier — *I go there yesterday* et *I goed there* tombent au même endroit.

**`ne se dit pas` a un effet qui ne transite par aucune note** : au moins un mot de ce cran et l'analyse du son ne tourne pas, quels que soient les poids (`activity.md`).

### La pertinence — as-tu visé juste ?

Quatre crans — `à côté`, `plat`, `ok`, `juste` — dans cette précédence. Le cas qui la décide est le groupe idiomatique mais hors ton : *crack on with it* dit à un client est de l'anglais précis, et faux ici ; l'exclusion le range en `à côté`, ce qui est le bon verdict. Prix assumé : *stuff*, vague **et** trop familier, n'apparaît pas comme mollesse.

**C'est elle qui porte la consigne**, donc tout ce qu'un défi veut exiger, et une consigne ne peut **qu'endurcir** : la précédence allant de `à côté` vers `juste`, elle fait passer des groupes vers le cran plus sévère. « Ignore les répétitions » reste inécrivable, ce qui est la règle.

**C'est la seule mesure du projet qui ait un bon côté**, et la raison tient à la nature des choses : pour le son, le modèle est la vérité, donc on ne peut pas faire mieux que lui ; pour une phrase il n'y a pas de modèle unique, donc on peut dépasser le simplement correct. Le cran haut est ce qui donne un sens à A. **Il ne vaut que s'il est rare** — c'est l'argument déjà fait contre le marquage tout-ou-rien : quand tout est colorié, plus rien n'est signalé. **Non mesuré** : personne ne sait à quelle fréquence ce juge-là en donnera.

**Le registre, la variété, la rareté et la complexité de la phrase n'ont pas de cran à eux** : le registre est ce que `à côté` dit, la répétition et la platitude ce que `plat` dit. Un cran par notion aurait compté deux fois les mêmes mots.

### L'étoffe — as-tu construit une phrase ?

**Le trou qu'elle bouche** : on peut traverser toute l'app en répondant *Yeah*, *I go shop*, *is good* et sortir avec des A partout. La correction demande *est-ce que c'est de l'anglais* et `Yeah` en est ; le suivi demande *as-tu pris ce qu'on t'a dit* et `Yeah` à une question fermée est parfaitement spécifique, la brièveté ne s'y marquant jamais ; la fluidité compte des blancs qu'une phrase de trois mots n'a pas ; et la pertinence marque le **mot** plat, pas la **phrase** basse. Le suivi a un cran `vague` parce que le doc a vu ce trou du côté de ce qui **entre** ; l'étoffe est le même cran du côté de ce qui **sort**.

**Un cran par passage, jugé, un seul élément**, de la même famille que le suivi et non du marquage par groupe de mots. Cinq crans, chacun ancré par sa phrase, sur le même tour d'IA que le suivi — *« I finally got the tickets, but only for the Sunday matinée »* :

| cran | ce qui le décide | exemple |
|---|---|---|
| `construit` | la structure porte le sens : une hypothèse, une subordonnée, des temps empilés | *« If you'd told me earlier, I could have swapped my shift »* |
| `étoffé` | deux propositions liées, ou un temps autre que le présent tenu jusqu'au bout | *« That's a shame, because I was hoping for the evening one »* |
| `simple` | une proposition, un temps, sujet-verbe-complément | *« Sunday is fine for me »* |
| `court` | un fragment qui répond sans faire de phrase | *« Oh, Sunday »* |
| `minimal` | un mot, une interjection, une formule figée | *« Oh, nice »* |

**C'est une description et pas un niveau.** Le doc a refusé un cran au-dessus d'`ok` côté correction — *une construction difficile montée juste* — au motif que « difficile » demande un niveau d'apprenant que l'app n'a pas. Ce refus visait une **étiquette au mot sur une norme absolue** et ne porte pas ici : dire combien de propositions sont sorties, et si un temps a été tenu, ne demande aucun niveau.

**La sensibilité dit ce que vaut une phrase ordinaire.** `simple` descend d'exactement une lettre par position — A, B, C, D, E — ce qui est la propriété que `notes/Sensitivity.kt` promet, lue sur le cran qu'un apprenant produit toute la journée. Au réglage `normal` ça met la phrase simple en C et la phrase liée en B : par défaut l'app demande qu'on lie deux idées, et une phrase liée passe.

**`construit` se pose juste au-dessus d'`étoffé`**, même geste qu'`entre les lignes` au-dessus de `précis` : tous les tours n'admettent pas une subordonnée, et un tour qui n'en offre pas l'occasion doit plafonner sans que ça coûte grand-chose. La série le laisse donc à A à **toutes** les positions — les deux bouts de la colonne restent atteignables partout, ce que ce doc exige d'une feuille à crans — et c'est `étoffé` qui cède une lettre par cran à partir du milieu.

**Le test d'inversion passe, et c'est ce qui justifie une seconde feuille sur la branche** : *vise juste* contre *déploie-toi*. La réponse courte et exacte est au plafond sur `Choix` et au sol sur l'étoffe ; la longue phrase ambitieuse qui rate le registre fait l'inverse. Aucun réglage d'une feuille unique ne rend les deux verdicts.

**Elle ne prend aucune consigne, et ce n'est pas un oubli.** Une consigne ne peut que durcir, et il n'y a rien à durcir : le cran décrit la structure sortie de la bouche. Ce qu'un défi veut en demander se dit par la sensibilité, qui est le levier, ou dans la mise en scène, qui est de la fiction et n'atteint jamais le critère.

**Deux prix, nommés plutôt que découverts.** Elle lit d'autres éléments que `Choix` — le passage entier contre les groupes de mots —, donc le parallèle est plus faible que celui des deux feuilles du son, la faiblesse même qui retient la seconde feuille de mélodie. Et elle frôle `plat` : l'étoffe lit la **structure**, la pertinence le **choix des mots**, donc *« I make a thing »* est marqué des deux côtés. C'est un second double-compte sur les mêmes mots, après celui de la correction et de la pertinence, et il est assumé.

**Un même mot peut être marqué par la correction et par la pertinence**, et son poids s'ajoute dans les deux. *« I make a thing »* est mal formé **et** plat : deux défauts réels, pas un compté deux fois. C'est la seule exception au principe qu'aucun mot n'entre dans deux feuilles dont les poids s'additionnent, et elle est assumée.

## Compréhension — le suivi

C'est la seule aptitude qui ne porte pas sur ce que l'apprenant produit : comprendre est ce qui **entre**, et l'app n'a aucune fenêtre dessus — elle ne peut que déduire de la réponse.

**Un cran par passage, jugé, sur un seul axe : ce que la réponse prouve d'avoir pris.** C'est la seule feuille dont la finesse ne peut pas venir du comptage — un seul élément, donc la précision *est* celle du cran. D'où six crans là où trois suffisent ailleurs. Sur le tour *« I finally got the tickets, but only for the Sunday matinée »* :

| cran | ce qui le décide | exemple |
|---|---|---|
| `entre les lignes` | répond à ce qui était sous-entendu, pas dit | *« Don't worry, I'll drive you home »* |
| `précis` | reprend un élément que seul quelqu'un qui a entendu pouvait reprendre | *« Sunday? I thought you wanted the evening one »* |
| `en rapport` | répond à ce que le tour disait, sans reprendre de détail | *« Oh nice, are you going? »* |
| `sur le sujet` | accroche le thème, pas le tour | *« Yeah, tickets are so expensive these days »* |
| `vague` | aurait marché quoi qu'on lui dise | *« Yeah, exactly »* |
| `à côté` | preuve négative : répond à autre chose | *« I went to the cinema last week »* |

**Un axe et un seul.** Un cran *partiel* — a pris une part du tour, raté l'autre — a été essayé et retiré : il mesure **combien du tour a été couvert**, pas la force de la preuve, donc il mettait deux axes dans une échelle qui ne se lisait plus. Le cas qu'il servait, le tour qui porte une remarque *et* une question, revient à ce qu'il était : un **jugement** du juge, qui pèse si l'apprenant a pris ce qui appelait une réponse.

**Le cran `vague` est ce qui fait tenir la feuille.** Sans lui, la réponse plausible qui n'engage rien devait être comptée bonne ou fausse, et les deux étaient faux : on peut tenir une conversation entière sur *yeah*, *right*, *I think so*, et sortir avec un A.

**Le haut est offert sur tous les tours** : tout tour porte du contenu précis, donc `précis` est atteignable partout. Un cran du **milieu** parfois indisponible ne gênerait personne ; c'est seulement un cran du **haut** indisponible qui punirait tout le monde pour une occasion que personne ne lui a donnée. C'est aussi pourquoi `entre les lignes` se pose **juste au-dessus** de `précis` et non loin devant : un tour sans sous-entendu plafonne à `précis`, et ce manque doit coûter presque rien.

**Ce qui est jugé n'est pas la qualité de la réponse mais ce qu'elle montre d'avoir pris.** *« Yes, I go yesterday at the shop »* est mal dit et parfaitement en rapport ; *« I like the cinema very much »* est correct et à côté. Confondre les deux ferait de cette feuille un doublon de la correction. Et la brièveté ne se marque jamais : *« Did you get the tickets? » — « Yeah »* est spécifique à ce qui a été demandé.

**L'objet est le dernier tour de l'IA ; le contexte est la conversation.** Répondre à la question d'il y a deux tours est `à côté`, même si le thème général tient encore — mais le juge a besoin du fil pour résoudre les pronoms et les ellipses.

**Le suivi pèse sur la difficulté du tour de l'IA, que le modèle rend avec sa réponse.** La longueur seule était grossière — *« Fancy a cuppa? »* est plus dur que quarante mots simples — et la position du levier de complexité ne la remplace pas : elle demande un niveau, elle ne promet pas que chaque phrase soit dure. La difficulté **remplace** la longueur, qui fait partie de ce que le modèle pèse en la rendant ; garder les deux compterait deux fois la même chose. D'où une symétrie utile : les mêmes trois dimensions des deux côtés — longueur, vocabulaire, structure demandées par les leviers, le même trio rendu par le cran.

Quatre choses à tenir avec lui : c'est un **poids, jamais une feuille**, il dit ce qu'on a envoyé et pas ce que l'apprenant a fait ; c'est un **cran**, le retour le moins cher qui soit ; il **se stocke avec la version de ce qui l'a produit** ; et ses limites s'écrivent avec lui — personne ne le vérifie, il ne se rejoue pas, et c'est le modèle qui note ce qu'il vient d'écrire.

Deux cas de bord. Un passage **sans tour de l'IA devant lui** n'a rien à avoir compris : la feuille est **absente**. Et le **nombre d'écoutes** ne donne pas de note, c'est un compte.

**Le suivi n'est jamais absent pour cause de texte affiché.** Voir le tour de l'IA est une aide en moins à trouver, pas une mesure qui disparaît : il reste à comprendre la langue, et quelqu'un qui lit la question et répond à côté ne l'a pas comprise.

## Fluidité — quatre feuilles

Qu'aucune des quatre ne compte ce qu'une autre compte, et pourquoi la continuité se lit en différence et non en rapport, est dans `sheets/Sheets.kt`.

### La continuité

**Comparer au modèle fait un vrai travail** : ce qui reste chez lui n'est pas rien, ce sont les **pauses prosodiques légitimes**, à la virgule, en fin de proposition. Le modèle s'arrête là, l'apprenant a le droit aussi. Ne pas faire la pause de la virgule est d'ailleurs une affaire de prosodie et pas de fluidité, d'où l'asymétrie voulue avec le débit, où les deux côtés comptent.

**Une pause est un silence entre deux mots qui dure au moins 200 ms**, et chaque condition fait son travail. La frontière des mots écarte ce qui se passe **à l'intérieur** d'un mot — une occlusion, une tenue, un bégaiement tenu ne sont pas des pauses. Le seuil écarte les micro-blancs entre les mots : le /t/ de *to stop* ferme la bouche 50 à 150 ms, et sans seuil une phrase riche en /p t k/ paraîtrait moins continue qu'une phrase pleine de voyelles — **pour son texte, pas pour son locuteur**, et comme cette feuille se lit dans l'absolu, ce biais ne s'annulerait nulle part. **Ce que la frontière écarte ne se perd pas** : un silence mi-mot reste dans le temps de parole, donc dans le débit. Le seuil est partagé avec lui : il exclut exactement ce qu'elle compte.

**Un délai de grâce d'une seconde s'applique aux deux bords, jamais à l'intérieur.** Sans lui, le temps de réagir et de cliquer — le même pour tout le monde, qu'on ait dit un mot ou vingt — pèserait bien plus sur un tour court : deux secondes sur *« Yes, I did »* font 50 %, les mêmes deux secondes devant vingt secondes ne font presque rien, pour un comportement identique. La seconde n'est pas mesurée. Et **elle ne touche pas ce qu'une condition lit** : un silence de 6 s reste un silence de 6 s pour qui cherche un gros blanc.

**Le silence final compte, et c'est voulu : rendre la parole est un acte.** Dans une conversation réelle on signale qu'on a fini — la voix qui retombe, le silence qu'on laisse à l'autre — et tarder à le faire est un vrai défaut, pas du bruit à retirer.

### Le plus long silence

Le test d'inversion la sépare de la continuité, et il passe : **A** passe 40 % du tour en silence mais jamais plus de 0,8 s d'affilée ; **B** en passe 25 % dont un blanc de 5 s d'un seul tenant. La continuité met A derrière, le plus long silence met B derrière — *« ne laisse pas de trous »* contre *« ne reste jamais bloqué »*. Le doc affirmait que ces deux-là allaient dans le même sens ; c'était trop vite dit.

Les deux formes écartées — moyenner les silences, ou lire une part du temps passé dans un blanc — sont dans `sheets/Sheets.kt`. Ce qu'elle ignore, assumé : deux blancs de 5 s valent un seul. C'est la continuité qui porte l'accumulation.

### Le débit

**Comparé au modèle et non dans l'absolu**, ce qui le remet sous la règle générale du projet — *le modèle est la vérité, s'en écarter se marque* — à laquelle il n'avait aucune raison d'échapper. Le texte étant le même des deux côtés, il n'y a **rien à compter**, ni mots ni syllabes : l'unité s'annule, et la question « combien de mots par seconde parle un bon apprenant », qui n'a pas de réponse indépendante du texte, disparaît.

**Symétrique par construction**, et savoir de quel côté on s'écarte ne se perd pas pour autant : la **marque** le lit, et une **condition** distingue *« jamais plus de 20 % plus lentement »* de *« pas plus vite »*.

**Le temps de parole exclut les silences des deux côtés**, donc le silence n'est jamais compté deux fois. **Détacher ses mots au lieu de les lier** — le défaut francophone que le doc voulait voir ici — se fait donc voir par la **continuité**, sous forme de petites pauses, ce qui est plus juste : détacher ses mots, c'est littéralement en fabriquer. Le débit garde ce qu'il est seul à voir : **articuler lentement à l'intérieur des mots**, voyelles étirées et consonnes sur-prononcées, tenues fermées comprises.

**Et il ne lit que les mots retenus.** Le temps brut contient les *um* et les morceaux abandonnés que le modèle ne dit pas : un tour hésitant aurait un temps gonflé, un gros écart de vitesse, et serait pénalisé **en plus** de sa propre feuille. Le doc voulait ça quand rien d'autre ne voyait les hésitations ; maintenant qu'elles ont leur feuille, la raison est servie une fois.

### Le remplissage et les reprises

**Une feuille dont les crans partagent les mots prononcés** — `retenu`, `abandonné`, `remplissage`. Sur *« It was, like, um, I went to the— I was going to the store »* : quatorze mots prononcés, deux de remplissage, quatre abandonnés, huit retenus. **Ça règle les deux dénominateurs** : une seule feuille, sur les quatorze, rien à réconcilier avec les huit de la grammaire.

**Répéter et repartir sont un seul cran** : répéter, c'est repartir avec les mêmes mots. Les séparer demanderait de dire à partir de quel mot changé on quitte l'un pour l'autre, et aucun défi ne veut peser cette différence-là.

**Elle est jugée et jamais calculée sur une liste de mots** (`sheets/Sheets.kt`), et elle prend par là une **consigne**, ce qu'une feuille calculée ne peut pas prendre. Un cran par phrase, « il bafouille beaucoup », est écarté pour la raison habituelle : personne ne peut le vérifier, là où « ces quatre mots-là » se regarde.

**La voix modèle ne dit que les mots retenus** : lui faire dire les hésitations est exclu, le modèle étant ce qu'on donne à imiter. **L'apprenant, lui, s'aligne sur tout ce qu'il a dit** — sans les hésitations dans le texte, ces bouts d'audio n'auraient aucune lettre en face et deviendraient des sons en trop, donc des gros ratés par nature : hésiter coûterait une note de prononciation. **On aligne sur les quatorze, on compare sur les huit.** Découper l'audio de l'apprenant pour n'en garder que les huit mots est refusé par un invariant déjà écrit : rien ne se fait à un seul des deux audios.

**Prix assumé : aux coutures, un ou deux sons sont comparés hors de leur contexte.** Le *I* retenu qui suit un *the* abandonné n'a pas devant lui ce que le modèle a devant le sien, et un son est influencé par celui qui le précède. Ça ne se propage pas (`analysis.md`, brique 11) et la redite l'annule, une phrase dite d'un trait n'ayant plus de couture — une raison de plus de faire redire un tour hésitant.

**Le bafouillage ne coupe pas l'analyse du son.** Ce qui la coupe est que la phrase va être *réécrite* : les mots changent, donc l'analyse porterait sur du texte mort. Ici les mots retenus sont les bons, ils ont seulement été dits en trébuchant.

**Ces crans ne survivent qu'à une reconnaissance verbatim**, et depuis qu'on aligne l'apprenant sur tout ce qu'il a dit, un nettoyage silencieux fait aussi rater le placement des sons. Le critère de fidélité verbatim du banc ne pesait jusqu'ici que sur la grammaire ; il pèse maintenant sur la mesure des sons (`../TODO.md`).

## Ce qui ne donne pas de note

Le tour interrompu et les trois comptes se calculent et n'entrent dans aucune note : ils existent pour les **conditions**, qui les lisent comme n'importe quelle feuille. Pourquoi chacun ne peut pas en donner est dans `sheets/Sheets.kt`.

**La part des tours interrompus d'une séance n'est pas la feuille**, c'est ce qu'une lecture en fait : une feuille se calcule toujours sur un passage, et confondre les deux plans se paie vite.

## Ce qui est jugé, ce qui est calculé

**Trois marquages viennent du modèle de langue** — les empans de langue, dont l'app tire la correction et la pertinence ; le bafouillage, dont elle tire le remplissage et les reprises ; le suivi. Tout le reste se calcule : un écart de répartition, un silence, un débit, un rapport.

**Le juge rend un cran, jamais un pourcentage** : « 72 % de suivi » n'est vérifiable par personne, et la finesse vient du comptage.

**Un cran de plus revient du modèle et n'est pas une mesure** : la difficulté de son propre tour, qui sert de poids au suivi. Il ne dit rien de l'apprenant, donc pas de feuille, pas de sensibilité, pas de consigne.

**Seul un marquage jugé prend des consignes** — du texte libre qui entre dans le critère que le juge lit, et non une feuille : deux feuilles se lisent d'un même passage du juge sur les empans, et il n'y a rien à quoi une consigne par feuille s'attacherait. Trois endroits où elles s'accrochent, donc, et sur les empans une consigne ne touche que la **pertinence**.

**Un marquage en porte plusieurs à la fois, chacune avec sa durée** — éternelle, ou un nombre de passages. Un patch en pose et en retire : *« au passage 4, tu lui mens sur tes intentions, pour trois passages »*. Une seule place remplacée ne marcherait pas dès que deux consignes n'ont pas la même durée — il faudrait réécrire le texte fusionné à chaque expiration. Le juge les reçoit toutes ensemble, et elles ne peuvent qu'endurcir, chacune comme l'ensemble. **Non mesuré, et impossible à mesurer au banc** : que deux consignes posées ensemble — *« parle au passé »* et *« registre soutenu »* — durcissent bien au lieu de se gêner est une propriété de leur combinaison, et il y en a autant que d'auteurs.

**Une consigne a trois lecteurs, et c'est ce qui la distingue des autres effets** : l'apprenant, qui doit la lire pour la suivre ; le personnage, qui joue avec ; le juge, qui note. Le patch parle à l'app et à l'apprenant, le message au modèle ne va qu'au modèle. **Une consigne est donc toujours affichée**, celle du départ comprise : être noté sur un critère qu'on ne t'a pas dit donne une note qu'on ne peut pas relire. Et le journal dit laquelle était debout à quel passage, sans quoi la note de pertinence des passages 4 à 8 ne se relit plus.

Ce qui est posé ne vaut que pour la **suite** : un passage déjà dit n'est jamais rejugé.

**Le poids choisit parmi ce qui est mesuré, la consigne reformule ce qui est jugé.** Un défi « prononce tous les -ed finaux » ne s'écrit donc pas en consigne sur les sons, que l'écart au modèle voit déjà, mais en creusant l'arbre pour ne peser que ces sons-là.

**La consigne fait partie de la situation, pas des réglages** : deux personnes dans la même activité reçoivent le même traitement, et c'est ça que l'invariance protège. Elle n'a ni positions ni phrase lisible, donc elle n'est pas un levier — c'est un `brief` par marquage jugé.

**Une consigne n'est qu'additive.** Pour qu'une feuille compte moins, il y a le poids, et lui seul. « Ignore les temps » est le cas interdit : elle ferait adoucir un verdict par un texte.

**Une consigne est ce qu'un juge lit, et rien d'autre ne porte ce nom.** L'**objectif** — *« obtiens le prix de la chambre »* — n'entre dans le critère d'aucun juge : il se dit dans le `brief`, se constate par un déclencheur, se conclut par une fin ; il a deux des trois lecteurs et pas le troisième. Et les **deux phrases d'un patch** annoncent un changement au lieu de poser un critère — ce qui brouille les deux est qu'un patch peut porter une consigne, donc annoncer et installer du même geste. Sans cette ligne, la moitié du `brief` finirait en consigne.

**Une feuille sous consigne n'est pas vérifiée par le banc**, qui éprouve le critère par défaut. Coût connu, pas un défaut à réparer.

Trois défis que ça écrit sans champ neuf. « 100 % passé » est une consigne sur le marquage et fait tomber les mots concernés en `à côté` — *I'll go there* s'y range, ce qui ne ment pas sur la langue. Le **mot interdit** est une consigne du même marquage : il n'a pas de feuille à lui, une feuille qui n'existe que si quelqu'un la configure ne passant pas les trois conditions d'existence. Et le **registre** est de la même famille, comme la longueur imposée.

**C'est donc la consigne de pertinence qui porte tout ce qu'un défi exige de la situation**, et il faut le dire là où on serait allé chercher un levier : l'écran custom et le catalogue donné au modèle.

## Deux collisions écartées, à ne pas rouvrir

**Le délai avant de parler ne se lit qu'une fois**, par la continuité : en faire aussi une mesure de compréhension compterait deux fois le même silence, avec deux poids qui s'additionnent sans que personne l'ait voulu. Et **l'allongement de syllabe** — « I waaaant », une hésitation portée par la durée — n'a plus de feuille pour le lire depuis que le rythme est retiré ; il reste dehors.

## Les noms d'écran

Ce qu'un écran affiche n'est pas la clé de la feuille mais un nom court, sans plafond de largeur — la mesure se cale contre la lettre et court vers la gauche autant que le nom le laisse (`ui.md`). Le critère est comment ça se lit à l'écran, jamais l'exactitude du terme. Aucune feuille ne porte encore ce nom en code (`../TODO.md`).

| feuille | EN | FR |  | branche | EN | FR |
|---|---|---|---|---|---|---|
| `intelligibility` | Clarity | Clarté | | `pronunciation` | Pronunciation | Prononciation |
| `proximity` | Precision | Précision | | `understanding` | Understanding | Compréhension |
| `melody` | Melody | Mélodie | | `correctness` | Grammar | Grammaire |
| `lexical-stress` | Stress | Accentuation | | `relevance` | Relevance | Pertinence |
| `correctness` | Form | Forme | | `fluency` | Fluency | Fluidité |
| `relevance` | Choice | Choix | | | | |
| `reach` | Reach | Étoffe | | | | |
| `uptake` | Reply | Réponse | | | | |
| `continuity` | Continuity | Continuité | | | | |
| `longest-silence` | Longest gap | Silence max | | | | |
| `pace` | Pace | Débit | | | | |
| `stumbling` | Hesitations | Hésitations | | | | |

**Deux branches n'ont qu'une feuille, et c'est ce qui rend leurs noms difficiles** : le titre a déjà pris le nom du domaine, donc la feuille doit dire autre chose. Et la correction et le choix sortent d'un seul marquage sur les mêmes éléments — leur nom ne peut donc pas être l'unité, les deux diraient *Mots*, il doit être la **question**. D'où `Forme` contre `Choix` : *est-ce que c'est de l'anglais*, puis *était-ce l'anglais qu'il fallait*. `Étoffe` est la troisième question de la même famille, *as-tu construit une phrase*, et elle échappe au problème : elle lit le passage entier, donc son nom peut dire ce qu'elle regarde.

Six choix à ne pas rouvrir.

- **`Grammar` / `Grammaire` pour `correctness`.** Le terme du code est large et exact, il couvre la syntaxe et l'idiome, mais il se lit à l'écran comme le geste de corriger — *« en cause : la Correction »* se comprend *« on t'a corrigé »*. `Langue` a été écarté pour l'inverse, exact et fade, tout étant la langue dans une app de langue. Coût borné : quand la faute est idiomatique, le nom trompe sur le *pourquoi*, et la marque dit toujours *quels mots*.
- **`Elocution` est un faux ami**, le seul de la table : en anglais il désigne l'art de parler en public. Posé tel quel il annoncerait un cours d'éloquence au-dessus de quatre feuilles qui comptent des `th` ratés. `Pronunciation` couvre exactement la branche, prosodie comprise.
- **`Stress` / `Accentuation`**, pas `Accent` des deux côtés : en français, `Accent` nu se lit *accent régional*, le contre-sens même que `Grammar` évitait.
- **`Continuity` et `Reply` sont fades exprès.** `Flow` / `Fluidité` appartient à la branche au-dessus, et une feuille ne porte pas le nom de sa branche sur l'écran qui les empile. `Preuve` aurait tenu le registre — c'est le mot du doc — mais il est froid, et la lisibilité passe devant l'homogénéité sur une ligne qu'on lit en passant. Le risque est connu : ce qui est jugé n'est pas la qualité de la réponse, et le nom ne le dit pas. La liste des six crans, elle, le dit.
- **`Reach` / `Étoffe`, et le français porte mieux que l'anglais.** `Étoffe` dit exactement ce qui est jugé, la matière de la phrase, sans dire *long* ni *savant*. `Reach` a été pris faute de mieux : `Ambition` fait un jugement de personne, `Range` sonne comme du vocabulaire, `Complexity` promet une échelle de difficulté que la feuille n'est pas. Le risque connu est qu'il se lise *portée* ; ce qui le rattrape est la ligne juste au-dessus, `Choice`, et la liste des cinq crans.
- **`Relevance` est le maillon faible, gardé sciemment.** En anglais il tire vers *hors sujet*, quand la feuille marque aussi un mot **plat qui est dans le sujet** — l'objection même qui l'avait écarté comme nom de feuille. Deux choses le rattrapent : dans une liste de cinq il se lit *as-tu dit ce qu'il fallait*, et la feuille juste dessous dit `Choice`. `Aptness` serait exact et se lit mal.

**Les noms mélangent la dimension et la faute, et c'est assumé.** `Clarity`, `Melody`, `Pace` nomment une dimension ; `Hesitations` et `Longest gap` nomment ce qui a raté. Tout convertir en dimensions échoue sur la fluidité, dont les quatre feuilles parlent du même tissu et où le vocabulaire manque. L'unité de l'écran de bilan se tient par la mise en page, pas par les mots.

## Les jetons de code

**Les clés du code sont en anglais, la prose de conception reste en français** — la règle de `dev_base` telle quelle. Les **clés** sont donc écrites en anglais dans les docs aussi, un nom entre accents graves désignant un identifiant. Les **crans** sont l'inverse : nommés en français dans la conception, portés par un jeton anglais dans le code. Les positions de leviers se traduisent d'elles-mêmes, les crans non :

| cran | jeton |  | cran | jeton |
|---|---|---|---|---|
| `ne se dit pas` | `not-said` | | `remplissage` | `filler` |
| `mal formé` | `malformed` | | `entre les lignes` | `implied` |
| `à côté` | `off-target` | | `précis` | `precise` |
| `plat` | `flat` | | `en rapport` | `on-point` |
| `juste` | `apt` | | `sur le sujet` | `on-topic` |
| `retenu` | `kept` | | | |
| `abandonné` | `abandoned` | | | |

**`cadence` devient `tempo` et pas `pace`** : la feuille `pace` occupe déjà le mot. Le débit est ce qu'on fait spontanément, la cadence une exigence de tenir un temps donné. En français les deux mots existaient déjà — la feuille dit *Débit*, le levier dit *rythme* —, c'est l'anglais qui les confondait.
