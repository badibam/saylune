# Le modèle d'activité

Conçu le 2026-09-01, élagué le même jour de ce qui est construit, repris le 2026-09-01 après une deuxième passe qui a défait deux fausses pièces, puis le 2026-09-02 par la passe qui a descendu la note sur la mesure et écrit la grille. Ce qui reste ici est la part du modèle d'activité qui n'a pas encore de code, plus ce qu'elle laisse ouvert.

Ce qui est parti et où le lire : l'**énoncé**, l'**activité** et **ce qui se stocke** sont dans `../reference.md` pour la règle et dans le code pour la forme (`activity/Activity.kt`, `conversation/TurnPipeline.kt`, `store/`). La suppression de la **session** et de la **parenthèse** est actée dans `../reference.md`. Les commits sont la carte.

Ce qui est écrit ici est tranché, sauf mention contraire. Ce qui reste ouvert est en fin de doc, nommé.

## Un nom d'usage se pose sur des axes, il ne les remplace jamais

Deux fois de suite, une notion familière s'est révélée n'être qu'un nom posé sur une combinaison.

Le **curseur d'aptitude** d'abord : ce qui se règle, ce sont des leviers, et « élocution 2 » est un nom sur un jeu de positions. Le **mode** ensuite : ce qui se décrit, c'est d'où viennent les réglages, s'il y a une rampe, et ce qui met fin ; « arcade » est un nom sur une combinaison de ces trois-là.

À chaque fois, prendre le nom pour le modèle obligeait à décrire deux fois la même séance et à tenir les deux descriptions d'accord. Et à chaque fois le symptôme était le même : une combinaison légitime devenait impossible à exprimer — une campagne ne pouvait pas contenir un niveau d'arcade, parce que les deux étaient des valeurs du même champ.

C'est la règle à appliquer au prochain paquet qui se présentera sous un nom rond.

## Les prescripteurs

Un **prescripteur** est ce qui remplit une activité — sa matière et ses réglages. Il y en a trois :

- **la conversation**, à partir de ce qui vient de s'y passer ;
- **l'apprenant**, par choix ou par consigne libre, ce qui est la voie par laquelle de la matière neuve entre ;
- **la progression**, pour ce qui est dû, jamais réussi, ou sur le point d'être oublié.

Un quatrième viendra peut-être — le contexte global, qui suggérerait de lui-même. Il n'a rien à préparer : ajouter un prescripteur n'est qu'une façon de plus de remplir les mêmes champs.

Un prescripteur ne décide rien pendant : il remplit des champs avant, puis se tait (« Qui décide de la pression »).

Les trois sont nommés en code (`Prescriber`) et **seul l'apprenant en est un pour l'instant** : ouvrir l'app est l'apprenant qui demande une conversation, lancer un niveau de campagne aussi. Les deux autres attendent le catalogue des activités.

## La définition, l'exécution, le bloc

Trois choses que la première passe confondait en une.

Une **définition** est une activité écrite d'avance : son format, ses réglages de départ, sa rampe s'il y en a une, ses conditions de fin, son critère de réussite. Elle est **écrite en dur dans le code, et fixe**. On la rejoue autant qu'on veut.

Une **exécution** est une ligne en base : une partie jouée, un essai, une conversation. C'est ce que le code appelle une activité, et c'est la seule des trois qui se stocke.

Un **bloc** est un groupe nommé de définitions, avec une seule propriété qui le distingue : l'accès y est **dans l'ordre**, c'est une campagne, ou **libre**, c'est un recueil de défis. Une forme, un drapeau — mais **deux portes distinctes dans l'app** : « poursuivre la campagne » et « choisir un défi » ne sont pas le même geste.

**L'arcade** est quatre définitions, d'easy à extreme, rejouées sans fin, chacune avec son classement. Un score ne se compare qu'à ceux de la même difficulté, ce qui est la version concrète d'une règle déjà écrite : une note ne se lit jamais sans la combinaison qui l'a produite.

**Rien de tout ça n'est en base** : ni table de blocs, ni table de campagnes, ni table d'avancement. Les définitions vivent dans le code, et **l'avancement se dérive des résultats** — un niveau est ouvert si les résultats des précédents le disent. Un avancement stocké serait une deuxième source qui se décale du résultat.

**Le déverrouillage lit la note de la séance**, calculée sur les feuilles réellement présentes — la fluidité n'a rien à lire sous capture manuelle, et son absence ne compte pas comme un zéro (« Les notes »). Le seuil est celui de partout — A ou B (« La marque est invariante »). Un bloc qui veut être plus dur monte la sévérité de ses réglages ; il ne déplace pas la barre.

Trois conséquences à ne pas redécouvrir plus tard.

**Une définition est un gabarit appliqué à la création, pas une dépendance gardée ensuite.** Les positions de leviers sont donc **toujours sur la ligne**, y compris pour une exécution issue d'une définition. L'alternative — un pointeur pour les séances de bloc, des valeurs pour les autres — ferait d'un même champ tantôt un pointeur tantôt des valeurs, et obligerait chaque lecteur à trancher le cas avant de lire. La duplication ne peut pas se décaler, puisque la définition est fixe.

**L'origine est un champ à part** — quelle définition, ou rien — et elle ne sert qu'à grouper : ouvrir le niveau suivant, ranger un score. Elle n'est jamais consultée pour savoir comment la séance était réglée.

**Une exécution issue d'un bloc n'est pas jetable.** Son résultat porte l'avancement, donc la supprimer reverrouille un niveau ou vide une entrée de classement. Une conversation libre, elle, se jette sans conséquence. C'est le prix du choix de dériver l'avancement plutôt que de le stocker, et il se dit à l'utilisateur au lieu de se faire en douce.

Ce que ça change à l'atomicité de l'activité, qui reste vraie mais pour une autre raison, est écrit dans `../reference.md`.

## Les champs qui manquent

Par rapport à ce qui est écrit (`activity/Activity.kt`), il en manque cinq, et un sixième change de type.

- **La consigne** — du texte libre, posé au départ et injecté dans le prompt, jamais réécrit ensuite. À ne pas confondre avec la **matière**, qui dit de quoi ça parle et que l'IA peut écrire après coup : une consigne « pousse-le sur le passé, il l'évite » peut donner une conversation dont la matière finit par être « son déménagement ». Les confondre ferait qu'un titre écrit par l'IA écrase la consigne. Nom proposé : `brief` — `seed` évoque une graine de tirage aléatoire, ce que ce n'est pas.
- **Les règles** — une liste, qui absorbe la rampe, les conditions de fin et les conditions branchées sur une feuille (« Les règles »).
- **Le journal des changements appliqués**, sans quoi une séance dont un tirage ou l'IA a modifié les réglages ne se recalcule plus.
- **L'origine**, ci-dessus.
- **Les consignes par feuille jugée** — du texte libre, une par feuille au plus, distinct du `brief` de l'activité (« Ce qui est jugé, ce qui est calculé »). Les poids et les sensibilités, eux, ne sont pas un champ neuf : ce sont des positions de leviers, donc des réglages.
- **Le résultat doit pouvoir porter un nombre.** Il porte aujourd'hui un verdict, un juge, une date et du texte libre ; un score d'arcade est un nombre, et le ranger dans du texte libre le rendrait inexploitable.

Et **`mode` n'est pas un champ** : « arcade », « campagne », « défi », « custom » sont des noms d'usage sur des combinaisons de ces axes-là.

## Les réglages

**Ce qui se stocke est une liste ouverte de positions de leviers, et rien d'autre.** Le critère est qu'elles répondent toutes à la même question : *où en est ce paramètre en ce moment*. Une rampe n'y répond pas, elle dit comment ça va changer ; une origine non plus, elle dit d'où les réglages viennent. Ces choses-là ont leurs champs à elles.

Deux raisons concrètes de tenir cette homogénéité. L'arcade doit **annoncer chaque changement en une phrase**, ce qui exige que toute entrée porte une phrase lisible pour chacune de ses positions — une rampe n'en a pas. Et l'état effectif se calcule depuis les réglages et l'avancement : mettre la rampe dans les réglages ferait chercher l'entrée du calcul là où il range sa sortie.

**Un curseur d'aptitude n'est pas un paramètre, c'est un préréglage** — un nom donné à un ensemble de positions. C'est le mode custom qui le prouve, puisqu'il expose chaque levier séparément : si les curseurs étaient le modèle, il faudrait une deuxième façon de décrire la même séance, et les tenir d'accord.

**Les leviers sont propres à un format.** Ceux listés plus bas sont ceux de la conversation ; un jeu de tuiles apporte les siens. Un curseur est donc un préréglage sur les leviers **d'un format**, et « élocution 2 » ne nomme pas la même chose d'un format à l'autre — deux notes venues de formats différents ne se comparent pas par la position d'un curseur.

**Chaque levier se déclare** : sa clé, ses positions possibles, sa valeur par défaut, et la phrase lisible de chacune de ses positions. Sans cette déclaration, une clé absente devient un défaut silencieux.

**Les réglages sont fixes pour toute la durée.** Ce qui bouge pendant une séance est l'**état effectif**, calculé depuis les réglages, les règles, le journal et l'avancement. L'arcade n'est donc pas une exception : sa rampe est une règle, une donnée fixe qui décrit une variation.

**Les vies sont un levier** : trois vies pressent l'apprenant comme un seuil de silence court.

**Le curseur de difficulté de l'arcade nomme une combinaison entière**, il n'en fait pas partie. Sa rampe est donc une règle qui monte **un cran entier**, pas une suite de changements levier par levier.

Ce qui est écrit en code aujourd'hui — une position par aptitude — est exactement ce que ce doc dit de ne pas stocker, et c'est à refaire.

Le détail de ce que chaque levier produit est à préciser.

## Les règles

Ce qui change pendant une séance, et quand. La version la plus bête est une liste `passage → changement` ; elle ne suffit pas pour deux raisons distinctes — tous les déclencheurs ne sont pas des numéros de passage, et le décideur n'est pas toujours la donnée écrite.

Ces deux manques se règlent en séparant trois choses que le mot « rampe » tenait collées : **quand**, **quoi**, **qui choisit**.

```
règle
  quand   : { sorte, paramètres }          # liste fermée de sortes
  choix   : [ patch, patch, ... ]          # un seul élément = pas de choix
  qui     : écrit | hasard | IA
```

**Un patch a exactement la forme des réglages** — des clés et des positions — et hérite donc des phrases lisibles déclarées avec chaque levier. Appliquer, c'est superposer ; annoncer, c'est lire la phrase ; proposer à l'IA, c'est envoyer les phrases et attendre une clé.

Trois choses tombent de cette forme.

**La rampe cesse d'être un champ** : c'est une règle dont le quand est « tous les N passages », le quoi un cran de plus, le qui « écrit ». **Et les conditions aussi** : « une feuille sous la barre → une vie » est une règle dont le patch retire une vie. Deux champs se replient en une liste.

**Le menu d'un tour est calculé, pas maintenu** : c'est l'ensemble des choix offerts par les règles qui se déclenchent maintenant. La plupart des tours il est vide, et on n'envoie rien.

**Ce qu'un tirage ou l'IA a choisi s'écrit sur l'activité.** C'est le prix de ces deux décideurs et il suit le critère du projet : on stocke ce qui dépend de quelque chose qui ne se retrouvera pas. Sans ce journal, l'état effectif d'une séance ne se recalcule plus, et une séance ne se compare plus à elle-même trois semaines après. Garder une graine de tirage coûterait moins cher et ne rejouerait juste que si le code n'a pas bougé.

**Les sortes de déclencheurs sont une liste fermée et courte**, déclarées comme les positions d'un levier. La pente est d'y glisser un mini-langage de conditions ; ce jour-là, plus personne ne sait ce qu'une définition fait sans l'exécuter.

**Une règle se résout avant que l'IA réponde**, parce qu'elle doit pouvoir fabriquer l'occasion de la contrainte qu'on vient de poser. Le cycle d'un passage est donc : il se ferme, les règles se déclenchent, l'IA répond en connaissant déjà ce qui a changé, son tour est dit, la notification affiche le changement quelques secondes hors du temps de parole, le micro s'arme. Quand c'est l'IA qui choisit, **elle choisit et répond dans le même appel** — deux sorties, pas deux allers-retours, ce qui compte quand la latence est le premier défaut du projet. Contrepartie : elle choisit en sachant ce qu'elle a envie de dire, et le menu est ce qui borne ça.

## Ce que l'app exécute, ce que l'IA interprète

Le dosage ne se décide pas sur ce qu'un modèle sait faire, mais sur **ce que l'app doit relire ensuite**.

- Ce que **personne ne relit** est du texte libre, sans limite : comment le personnage parle, ce qu'il raconte, sur quoi il rebondit, comment il réagit. Zéro champ, zéro validation, coût nul. C'est là qu'il faut être généreux — c'est gratuit, et c'est ce qui fait vivre la scène.
- Ce que **l'app doit exécuter** — poser un levier, retirer une vie, finir, imposer une contrainte — coûte un **champ de retour**, donc un contrat de plus, une validation de plus, et un trou silencieux s'il n'est pas validé. Le projet a déjà le cas sous les yeux : `intended` arrive en texte libre et rien ne vérifie qu'il tient ses consignes (`../../TODO.md`).

D'où la forme qui rend la liberté bon marché : **prose libre à l'aller, clés énumérées au retour.** L'app déclare ce qui est disponible — les leviers ont déjà leurs positions et leur phrase lisible —, l'IA **choisit une clé**, elle n'invente pas. Un champ, validé par appartenance. Et c'est ce que les modèles font le mieux : choisir dans une liste est fiable, calibrer une contrainte neuve ne l'est pas.

**La borne n'est donc pas la liberté de l'IA, c'est la déclaration** : rien d'indéclaré ne peut être choisi, faute d'exécutant. Un menu peut couvrir tous les leviers si on veut. **Non mesuré** : choisir dans cinq options est probablement plus fiable que dans soixante, et rien ne le dit ici — offrir une liste courte est une prudence, pas un résultat.

## La marque est invariante, ce qu'on en fait ne l'est pas

**La même faute produit la même marque, à n'importe quel moment, quels que soient les réglages.** Aucun levier ne touche jamais une marque. C'est l'exigence de `../reference.md` tenue jusqu'au bout : une marque dont la couleur ou la présence dépendrait du réglage du jour ne transporte plus rien, puisque son absence ne se distingue pas d'une approbation.

Il y a donc **trois étages, et un seul où les réglages entrent**.

- **La mesure** — l'écart au modèle pour le son, le cran de formulation pour un groupe de mots. Aucun réglage ne la touche.
- **La marque** — elle affiche la mesure telle quelle. Invariante, c'est le repère stable de l'apprenant.
- **La note** — elle agrège les mesures du passage, puis de la séance. C'est **là et nulle part ailleurs** que les réglages agissent : la sévérité décide à quel niveau de fautes on passe de A à B, de B à C.

Ce que les réglages gouvernaient — faut-il redire, est-ce que ça compte dans la note — n'est donc plus deux choses. Compter dans la note **est** l'effet du réglage, et redire se lit sur la note qui en sort plutôt que sur la mesure brute. Une seule lecture réglée, tout le reste en découle.

**Une seule barre, la même partout : A ou B, ça va.** Sur un passage elle décide s'il faut redire ; sur une activité, si c'est réussi et si le niveau suivant s'ouvre. L'apprenant apprend la règle une fois et elle vaut partout.

**Et elle ne se règle pas.** Si la sévérité et la barre bougeaient toutes les deux, deux boutons feraient la même chose et plus rien ne dirait lequel a rendu une séance dure. Ce qui se règle est la sévérité ; ce qu'il faut atteindre ne bouge jamais.

Ça retire une chose qui était écrite ici : il n'y a **pas de barre de redire par échelle**, les sensibilités ne servant qu'à la note. Et **le nombre d'essais permis est un levier du format**, pas de l'élocution : il dit combien de tentatives un passage accepte, jusqu'à une seule (« Le passage »).

Deux choses restent acquises : la formulation se souligne **toujours** dès qu'il y a un cran à montrer, et la rampe des sons est la même partout et tout le temps — la bande de bruit de ±5 n'est pas un réglage mais une propriété mesurée de la machine.

**Perte assumée** : il n'y a plus de conversation sans aucune marque grammaticale. C'est plutôt un retour au texte d'origine, qui dit que sans la trace la discrétion se retourne — on corrige et personne ne l'apprend.

## Les notes

**Une échelle unique, A–E.** Elle vaut pour tout ce qui se note, à n'importe quel niveau, sans avoir à retenir plusieurs échelles.

**La note ne vit pas sur l'aptitude, elle vit sur la mesure.** Un défi qui ne noterait que l'accent tonique rendrait sinon une note « élocution » qui ne veut pas dire la même chose que celle du défi d'à côté, sans que rien à l'écran ne le dise. C'est la troisième application de la règle qui ouvre ce doc : « élocution B » est un nom sur un jeu de mesures, comme « élocution 2 » était un nom sur un jeu de leviers. L'aptitude reste un tiroir — pour choisir, pour afficher — et n'est plus l'unité de la note.

**La lettre est toujours une note, jamais une mesure.** Elle est contextuelle par construction, puisque la sévérité déplace ses bornes ; une mesure, elle, ne bouge pas. Les deux ne doivent donc jamais porter les mêmes noms — c'est la confusion que ce doc portait, où les crans de formulation étaient écrits en A–E comme la note. **On garde les nombres et les crans en base, jamais les lettres** : les bornes pourront bouger sans abîmer les vieilles séances.

### L'arbre des poids

**Ce qui se note est un arbre, à profondeur libre.** Aptitude, mesure, découpage plus fin ne sont pas trois natures : ce sont des nœuds, et la profondeur dit seulement à quel grain on peut peser. La question « est-ce une mesure ou une catégorie de mesure ? » ne se pose donc jamais, et démultiplier revient à creuser une branche, jamais à changer de mécanisme. **Rien n'oblige un nœud de premier niveau à être une aptitude** — l'arbre ne connaît que des poids.

Deux réglages par nœud, et ils ne font pas la même chose :

- **la sensibilité** — où tombent les bornes A–E de cette note ;
- **le poids** — combien cette note pèse dans celle du dessus. À 0, le nœud ne compte pas.

Un défi qui ne note que l'accent tonique est donc un poids à 1 et des poids à 0, pas un mécanisme à part. C'est ce qui permet de viser sans ajouter de champ : quoi qu'on note, l'information est déjà là.

### L'agrégation se fait une fois, à plat

**Les poids se multiplient en descendant, et la note se calcule une seule fois sur les feuilles réellement présentes.** Les lettres d'aptitude et de passage sont la même formule restreinte à un sous-arbre : des lectures, pas des étapes de calcul.

La raison est qu'une cascade de moyennes redistribue en silence des poids que personne n'a réglés, dès qu'une feuille manque — et il en manque tout le temps : la fluidité n'a rien à lire en capture manuelle, un passage dont la porte grammaticale s'est fermée n'a aucune mesure de son.

Avec élocution 2 (sons 1, mélodie 1) et formulation 1, sur deux passages dont le second a la porte fermée — passage 1 : sons 40, mélodie 80, formulation 90 ; passage 2 : formulation 50. En cascade, le passage 1 vaut 70, le passage 2 vaut 50 puisque sa moyenne se renormalise sur ce qui reste, et la séance 60. À plat, (40 + 80 + 90 + 50) / 4 = 65. L'écart n'est pas l'arrondi : dans la cascade, la formulation a fini par peser deux tiers de la séance et l'élocution un tiers, l'inverse exact du 2:1 demandé, parce qu'au passage 2 elle était seule et a pris tout le passage pour elle.

**Une feuille absente sort de la somme, elle ne vaut jamais zéro.** C'était déjà la règle pour la fluidité ; elle vaut pour toutes.

**Chaque feuille pèse par la longueur du passage** — et pour la compréhension, par celle du tour de l'IA, qui est la matière qu'elle couvre. Proportionnel à la longueur est un peu arbitraire, un contour mélodique étant un contour qu'il soit long ou court ; c'est uniforme, et le tenir sur une longue phrase est effectivement plus de travail. La longueur se compte en **mots** plutôt qu'en sons : un passage dont la porte a coupé l'analyse n'a pas de sons et a toujours des mots.

**Entre feuilles, la moyenne décide seule** : pas de plancher qui plafonnerait la note dès qu'une feuille comptée passe sous la barre. Une bonne feuille peut donc en masquer une mauvaise, et c'est accepté — dans un défi, peu de feuilles comptent, et la sensibilité de chacune dit à quel point elle est facile à tenir. Ça ne touche pas le plafond posé *à l'intérieur* de la formulation, où les fautes bornent le cran haut.

### Le passage

**Le passage est l'unité de la note : un énoncé et toutes ses redites.** Le mot est neuf parce que « tour » désigne déjà un tour de parole — un enregistrement, un énoncé, une position de capture — et confondre les deux se paierait au premier commit.

**La note du passage est celle de la dernière tentative**, et le nombre de tentatives permises est un levier du format, qui peut valoir 1. La dernière est ce qu'on sait dire maintenant. Conséquence à dire à l'apprenant plutôt qu'à lui laisser découvrir : une tentative de trop, après une réussite, peut faire baisser la note.

Les trois autres lectures sont pires. La **première** rend la redite sans effet, donc sans intérêt. La **meilleure** laisse l'obstination atteindre A. La **moyenne des tentatives** fait baisser la note à chaque essai, c'est-à-dire punit exactement le geste que l'app existe pour provoquer.

**La dernière tentative ne porte que les feuilles que la redite sert à corriger** — l'élocution et la formulation. La fluidité et la richesse lisent la **première**, parce qu'une phrase répétée n'est plus de la parole spontanée, et que c'est la parole spontanée qu'elles mesurent. C'est la version précise de la règle déjà écrite plus bas, qui disait qu'une redite ne compte que pour l'élocution.

**Une redite remplace à l'affichage, jamais en base.** Le fil ne montre que la dernière tentative, et la réponse de l'IA se refait sur elle — sans quoi la conversation garderait une réponse à une phrase que personne ne lit plus. Mais les tentatives restent toutes sous le passage : une tentative effacée est une mesure perdue, et le compte des essais est une feuille. Ça tranche ce que `../reference.md` laissait ouvert sur le sort de la phrase initiale.

**Le nombre de tentatives est lui-même une feuille**, dans la branche où la redite a eu lieu : réussir du premier coup et réussir au troisième ne sont pas la même chose. Poids à 0 le plus souvent, monté par un défi qui veut la justesse d'emblée. Redire une prononciation et reformuler une phrase ne se comptent pas ensemble — deux feuilles, une par branche.

### Les crans de formulation

La formulation se juge **par groupe de mots**, et le fait d'être situé fait tomber le haut de l'échelle : « ce qu'un natif dirait » et « correct et naturel » ne se distinguent pas quand il n'y a rien à montrer.

Quatre crans, et le neutre au milieu ne se marque pas :

- **natif** — idiomatique, ce qu'un apprenant ne produit pas spontanément.
- *(rien)* — correct, il n'y a rien à pointer.
- **maladroit** — correct, mais on ne dit pas comme ça.
- **fautif** — incorrect, mais on comprend.
- **ne se dit pas**.

**C'est la seule mesure du projet qui va dans les deux sens**, et la raison tient à la nature des deux : pour le son, le modèle est la vérité, donc on ne peut pas faire mieux que lui — être dessus est l'attendu, et sous la bande de bruit rien ne se distingue. Pour la formulation il n'y a pas de modèle unique, donc on peut dépasser le simplement correct. Le cran haut est ce qui donne un sens à A : sans lui, A et B disaient tous deux « rien à signaler » et la différence n'était observable nulle part.

**Le juge rend le cran, et rien de plus fin.** Sur trois mots, « haut de maladroit » n'est pas un jugement que quelqu'un pourrait vérifier. La finesse ne se perd pas, elle change d'endroit : elle vient du comptage — combien de groupes marqués, à quel cran, sur quelle longueur de phrase. Un passage se note donc finement en agrégeant beaucoup de jugements grossiers, exactement comme le fait déjà l'élocution. Coût assumé : un passage de trois mots portant un seul groupe marqué a une note très grossière. C'est une grossièreté vraie, pas une fausse précision.

**Le cran haut ne vaut que s'il est rare.** C'est l'argument déjà fait contre le marquage tout-ou-rien : quand tout est colorié, plus rien n'est signalé. Un juge qui en donne un passage sur deux le rend décoratif, et la consigne doit donc être exigeante. **Non mesuré** : personne ne sait à quelle fréquence ce juge-là en donnera.

**Piste pour le calcul**, à détailler quand on l'écrira : les **fautes fixent le plafond**, le cran haut monte à l'intérieur de ce qui reste — n'avoir aucune faute est nécessaire pour atteindre A, et c'est le cran haut qui sépare A de B. Un cran haut ne compense pas une faute : une tournure brillante à côté d'un verbe cassé ne rend pas le verbe moins cassé, et si les deux se compensaient on couvrirait ses fautes en en accumulant.

Ça absorbe un chantier qui traînait à part (`../../TODO.md`, point 6) : le verdict grammatical était un booléen sur le tour entier, ce qui écrasait le fait qu'un passage puisse porter plusieurs fautes et empêchait de marquer la portion concernée. Un cran par groupe de mots règle les deux.

### Ce que ça change au marquage

**La forme dit de quelle échelle il s'agit, la couleur dit de quel côté et à quelle distance du neutre.** C'est l'amendement d'une phrase de `../reference.md`, qui disait que la couleur ne porte que l'alarme : le cran haut porte l'inverse d'une alarme, donc la couleur porte aussi le côté. Le principe change de portée sans se perdre, la forme restant seule à dire l'échelle.

La formulation partage donc la famille de couleurs des sons — le vert pour le cran haut, la rampe ambre-rouge pour les trois autres — et se distingue par sa forme. Ça pose un problème concret : les sons prennent déjà **un filet sous le mot** pour l'accent lexical, et un soulignement de formulation tomberait au même endroit. **La formulation se souligne en vaguelette, l'accent garde le trait droit.** La vaguelette se lit comme une erreur de langue sans qu'on ait à l'apprendre, et elle se distingue d'un trait droit au même endroit et dans la même couleur.

### La porte grammaticale

**Elle se ferme sur ce qui va être réécrit**, plus sur le marquage. La raison de la porte a toujours été qu'on ne travaille pas la prononciation d'une phrase qu'on s'apprête à réécrire ; tant que marquage et réécriture allaient ensemble, « marqué » était un bon substitut. Ce n'est plus le cas depuis que la formulation a des leviers séparés.

**Elle lit la même barre que tout le reste** : un passage dont la note de formulation ne passe pas est un passage à refaire, donc son son ne s'analyse pas. En défi, ne pas passer force le nouvel essai ; en conversation libre ça ne force rien, mais l'app tient quand même la phrase pour une phrase à refaire — elle marque la formulation, propose de redire, et n'analyse pas le son. Une seule barre, deux conséquences selon le contexte.

**Et le cran « ne se dit pas » ferme la porte quel que soit le réglage**, sans passer par la note. Une phrase qui ne se dit pas, l'app devrait la synthétiser pour l'analyser, donc la faire entendre comme modèle à imiter. Tout le montage repose sur le fait que le modèle est la vérité ; un modèle qui prononce une non-phrase empoisonne ça.

**La marque porte sur un élément, la note agrège sur le passage.** C'est vrai des trois échelles du son comme des crans de formulation : l'accent d'un mot est au bon endroit ou pas, mais un passage en contient plusieurs et la note les compte.

## La grille des mesures

Trois conditions pour qu'une feuille existe, et elles tiennent ensemble :

- elle **se calcule toujours**, quelle que soit l'activité — sinon la configuration d'un défi déciderait quels champs existent, et deux séances ne porteraient pas la même matière ;
- elle **veut dire quelque chose sans aucun défi**, dans une conversation ordinaire — sinon c'est un cas particulier déguisé en mesure ;
- quelqu'un pourrait vouloir la **noter seule**.

**Aucune feuille n'a deux sens.** Une feuille dont la direction devrait s'inverser selon l'activité est la mauvaise feuille : le défi « explique ça à un enfant de huit ans » ne demande pas la rareté du lexique à l'envers, il demande de s'adapter à qui écoute — c'est du registre, et la rareté passe simplement à 0.

Ce qui suit est la grille du **format conversation** ; un autre format apporte la sienne. Aucune branche n'est close.

**Élocution** — les sons (par son) ; l'accent lexical (par mot) ; la mélodie (par passage) ; le nombre de tentatives de prononciation. *Candidate* : le rythme, les durées relatives comparées au modèle. La branche creuse gratuitement : la grille des sons est un inventaire fermé déjà attaché à chaque marque, donc un défi « travaille tes *th* » est un poids posé sur deux colonnes, sans juge et sans liste à inventer.

**Formulation** — le cran par groupe de mots, découpé par **la nature de l'empan marqué** : groupe verbal, groupe nominal, préposition ou particule, circonstanciel, proposition entière. Plus le nombre de tentatives de reformulation.

Ce découpage-là plutôt qu'une liste de causes — temps, accord, préposition — parce qu'une liste de causes n'est jamais complète : elle finit avec un tiroir « autre » qui ne nomme rien et qu'on ne saurait pas peser. La nature de l'empan est **fermée par construction**, tout empan étant une pièce de la phrase, et bien plus stable à juger : dire qu'un empan est un groupe verbal se vérifie, dire qu'une faute est d'aspect plutôt que de temps se discute. Coût assumé, c'est plus grossier — « I go there yesterday » et « I goed there » tombent au même endroit. La finesse est ailleurs, dans la consigne.

**Fluidité** — le délai avant de parler ; le nombre et la longueur des silences intérieurs ; le silence final ; le débit ; les mots de remplissage ; les répétitions ; les reprises et faux départs. Les quatre premières n'existent qu'en capture automatique.

Les trois dernières **ne survivent qu'à une reconnaissance verbatim** : un moteur qui nettoie les *euh* et les bégaiements les rend muettes sans jamais le dire, et la fluidité paraîtra excellente. C'est un critère de choix de plus pour le banc de fidélité (`../../TODO.md`, chantier 2), qui ne le devait jusqu'ici qu'à la grammaire.

**Richesse** — la variété du lexique ; sa rareté ; l'adéquation du registre ; la complexité de la phrase.

**Compréhension** — la pertinence de la réponse ; le nombre d'écoutes du tour de l'IA.

Deux collisions écartées, à ne pas rouvrir. **Le délai avant de parler ne se lit qu'une fois**, par la fluidité : en faire aussi une mesure de compréhension compterait deux fois le même silence, avec deux poids qui s'additionnent sans que personne l'ait voulu. Et **l'allongement de syllabe** — « I waaaant », une hésitation portée par la durée — tomberait sur le même signal que le rythme de l'élocution ; il reste dehors tant que rien ne dit lequel des deux le lit.

## Ce qui est jugé, ce qui est calculé

**Trois feuilles sont jugées par le modèle de langue** — l'empan de formulation, l'adéquation du registre, la pertinence de la réponse. Tout le reste se calcule : un écart de répartition, un silence, un débit, un rapport, une table de fréquence.

**Le juge rend un cran, jamais un pourcentage.** « 72 % de pertinence » n'est vérifiable par personne, et la finesse vient du comptage, comme pour les sons.

- **empan de formulation** — les quatre crans ci-dessus, par groupe de mots.
- **registre** — un cran par passage, avec un bon côté comme la formulation : remarquablement dans le ton / rien / approximatif / hors registre. Le bon côté se justifie pour la même raison que là-bas : il n'y a pas un seul ton correct, donc on peut dépasser l'acceptable.
- **pertinence** — un cran par passage, et **pas de bon côté** : répond / répond à moitié / à côté. Contrairement au ton, il n'y a qu'une chose à faire, répondre à ce qui a été dit ; rien au-dessus.

**Seule une feuille jugée prend une consigne** — du texte libre qui entre dans le critère que le juge lit. Une feuille calculée n'en prend pas : il n'y a pas de phrase à reformuler dans un écart de répartition. **Le poids choisit parmi ce qui est mesuré, la consigne reformule ce qui est jugé.** Un défi « prononce tous les -ed finaux » ne s'écrit donc pas en consigne sur les sons, que l'écart au modèle voit déjà, mais en creusant l'arbre pour ne peser que ces sons-là.

**La consigne fait partie de la situation, pas des réglages.** C'est ce qui la rend compatible avec l'invariance : deux personnes dans la même activité reçoivent le même traitement, et c'est ça que l'invariance protège. Elle n'a ni positions ni phrase lisible par position, donc elle ne va pas dans la liste des leviers — c'est un champ à part, un `brief` par feuille jugée.

**Une consigne n'est qu'additive.** Pour qu'une feuille compte moins ou pas du tout, il y a le poids, et lui seul. Une consigne « ignore les temps » est le cas interdit : elle ferait adoucir un verdict par un texte, ce que le module n'a pas le droit de faire.

**Une feuille sous consigne n'est pas vérifiée par le banc**, qui éprouve le critère par défaut. Coût connu, pas un défaut à réparer.

Deux défis que ça écrit sans champ neuf. « 100 % passé » pèse le groupe verbal et pose sa consigne dessus ; la marque sur « I'll go there » montre alors la consigne — le passé était demandé — au lieu de mentir sur la langue, puisque la phrase est de l'anglais parfait. Et le **mot interdit** est une consigne sur le registre : il n'a pas de feuille à lui, une feuille qui n'existe que si quelqu'un la configure ne passant pas les trois conditions.

### Les conditions attachées à une feuille

Ce qui rend une contrainte dure n'est pas la note — noyée dans une moyenne pondérée par la longueur, une occurrence coûte quelques centièmes de lettre — mais une **condition branchée sur la feuille**, qui fait perdre une vie ou refuse le passage sur-le-champ. Une vie perdue et une condition de fin sont la même chose vue deux fois : la fin, c'est zéro vie.

Une condition **lit le résultat d'une feuille, elle ne change pas ce que la feuille mesure**. Elle se branche donc aussi bien sur une feuille calculée : un silence de plus de cinq secondes coûte une vie.

**Le mécanisme reste à préciser** : porte-t-elle sur la mesure, sur la note, ou sur les deux. Deux formes se dessinent — la note de la feuille sous la barre, ou une position atteinte au moins une fois : un empan au cran « ne se dit pas », un passage jugé hors registre.

## Le blocage

**Bloquer, c'est une règle dont l'effet est que le passage ne se ferme pas.** Pas un mécanisme neuf, et donc branchable sur n'importe quelle feuille : un défi peut bloquer sur la prononciation comme sur la formulation.

Concrètement, trois choses : la réponse **n'ajoute rien** — elle ne répond pas au fond et ne pose pas de question neuve ; le passage **reste ouvert**, ce qu'on attend ensuite étant une reprise de la même chose ; et rien n'avance tant qu'il ne se ferme pas.

**Ce n'est pas l'interruption que le projet refuse.** On ne part pas travailler ailleurs, le sujet ne change pas, l'écran ne change pas : c'est la conversation qui s'arrête sur une phrase. Le geste fondateur reste tenu.

**Deux axes, pas un.** L'**écho** — absent, indication indirecte, reprise explicite — est le levier déjà écrit. L'**avance** est le second : la réponse poursuit, ou elle attend. Le geste fondateur du projet est la combinaison (indirect, poursuit) : *« Ah, you're 25! And where... »*. Une seule combinaison ne s'offre pas, (absent, attend), qui ferait attendre sans dire pourquoi.

Ça éclaire ce que la porte grammaticale est vraiment : **une seule barre, trois conséquences** — le son ne s'analyse pas, une redite est proposée, et la conversation attend si les réglages le disent.

### Deux branches pour la formulation, une notification pour le son

**Le verdict de formulation naît dans le même appel que la réponse ; le verdict de son naît après**, puisque l'analyse a besoin d'`intended`, qui vient du modèle. Les deux blocages n'ont donc pas les mêmes moyens, et ce n'est pas une incohérence : c'est ce que chaque verdict rend possible au moment où il tombe.

**Formulation** : l'appel rend **une réponse plus une ligne courte** — la continuation, et l'écho de reprise (« Ah, you mean you ARE 25 »). L'app calcule la note dès le retour et joue l'une ou l'autre. Trois conséquences : **plus rien n'est jamais remplacé**, une seule étant jouée ; c'est littéralement « l'IA joue, l'app décide », puisqu'elle fournit la matière des deux issues sans trancher ; et la ligne courte **n'est produite que si l'IA a marqué quelque chose**, ce qui la rend gratuite sur un passage propre. Un champ de retour de plus, une chaîne, sans énumération à valider — le moins cher des contrats.

**Prononciation** : quand l'écart au modèle arrive, l'appel est fini et la réponse existe. Rien ne peut fournir un écho en personnage sans un second appel, écarté pour la latence. Donc la réponse est **retenue**, et une notification dit quoi reprendre — en donnant à **écouter** le modèle, qui est synthétisé de toute façon, et non en expliquant, le remède d'une faute sonore n'ayant jamais été une consigne écrite.

### La sortie d'un passage bloqué

Deux, et elles ne sont pas concurrentes : **les tentatives s'épuisent**, ce qui est la sortie automatique, ou **l'apprenant abandonne**, ce qui est la même sortie déclenchée à la main. Sans l'une des deux, la conversation meurt sur une phrase.

Dans les deux cas l'IA repart du sens qu'elle avait compris — elle l'a toujours compris — et le passage est enregistré comme raté. C'est ce qui alimente les règles : perdre une vie, durcir, ouvrir ou non le niveau suivant.

## La pression

**La pression est ce que l'apprenant vit, pas un réglage.** Rien ne porte ce nom dans le code. Elle est produite depuis plusieurs endroits qui ne se connaissent pas entre eux, et le mot sert à parler de leur somme — quand on conçoit une activité, ou quand on dit ce qu'on a ressenti.

Trois mots à ne pas confondre :

- la **pression**, l'effet chez l'apprenant ;
- le **levier**, un moyen concret de la moduler : le texte affiché ou caché, le silence toléré avant qu'on relance, un mot interdit ;
- le **curseur**, un préréglage qui pose plusieurs leviers d'un coup sous un nom lisible.

### Qui décide de la pression

Trois décideurs. Ce qui les sépare est **qui tient la décision et quand elle est prise** : le module, c'est le concepteur, figé d'avance dans du code ; l'apprenant, c'est un humain qui tranche sur le moment ; l'IA, c'est une machine qui tranche sur le moment sans que ce soit écrit nulle part. Aucune paire ne fusionne — le module borne l'IA sans pouvoir la prédire, et l'apprenant règle sans écrire le déroulé.

| décideur | ce qu'il décide | ce qu'il ne décide jamais |
|---|---|---|
| **l'apprenant** | l'exigence par aptitude ; ce qu'il ouvre, accepte, écarte ; quand il redit | ce qui est vrai — il pose une barre, pas un verdict |
| **le module** | son déroulé, ce qu'il donne et retire à chaque étape, quels leviers il expose | ce qui est vrai — il ne peut pas adoucir une faute pour encourager |
| **l'IA** | tour par tour, dans les bornes reçues : relancer ou laisser, ouvrir ou fermer | le moment — elle ne part jamais travailler ailleurs |

La troisième colonne est celle qui porte, et la plupart de ses lignes sont déjà tranchées ailleurs : l'IA qui ne décide pas du moment, c'est « on n'interrompt jamais pour partir travailler ailleurs » ; l'apprenant qui pose une barre et non un verdict, c'est « exigence absolue sur une mesure relative » (`../reference.md`).

Deux lignes sont neuves. **Le module ne peut pas adoucir un verdict** — un module « débutant » qui marquerait moins pour ne pas décourager casserait *la même faute doit produire la même marque*, et rien d'autre ne fermait cette porte. Et **le module décide quels leviers il expose**, ce qui n'était écrit que comme un fait d'implémentation et devient ici un pouvoir qu'on lui accorde.

**Deux choses ressemblent à des décideurs et n'en sont pas.** La **mesure** n'a aucune discrétion : elle applique la barre qu'on lui donne, elle ne la choisit pas, et un décideur sans discrétion est une fonction. Le **prescripteur** n'est pas un décideur d'un autre genre, c'est le **siège** que l'apprenant occupe et que la conversation ou la progression occuperont à sa place — ce qui s'y décide est le même, matière et réglages, donc les mêmes interdictions s'y appliquent sans qu'il faille les réécrire.

### Les leviers qui pressent chaque aptitude

Ce que chaque aptitude fait mesurer est dans « La grille des mesures » ; ici ne sont que les **leviers** qui modulent la pression sur elle, ceux du **format conversation**. Un autre format apporte les siens, et aucune de ces listes n'est close. Un levier commun à toutes ne se répète pas : **chaque feuille a sa sensibilité et son poids**, par construction de l'arbre.

**Élocution**
- *Leviers* : voir le texte, ou redire sa phrase de tête ; un temps limité, posé en pourcentage de la durée du texte synthétisé, et dépasser oblige à réessayer.

**Compréhension**
- *Leviers* : la complexité du tour de l'IA — longueur, vocabulaire, structure ; le texte affiché ou flouté ; la réécoute, autorisée ou non, et combien de fois ; le bruit et la qualité du canal, jusqu'à simuler un mauvais réseau qui coupe des mots.

Le bruit s'applique **à la lecture**, jamais au rendu mis en cache : le même fichier sert d'étalon à la mesure, et le bruiter fausserait l'écart.

**Formulation**
- *Leviers* : la réponse de l'IA sur un tour fautif, en trois niveaux — absente (erreur ignorée, réponse normale), indication indirecte (la reprise dans sa réponse, ce que le code incite aujourd'hui), ou reformulation explicite dite comme un coach reprend son élève ; l'explication de la faute en notification, à deux niveaux — la règle à utiliser seule, ou la règle plus la phrase correcte.

Le marquage a quitté cette liste : il est invariant, donc il n'est plus un levier. Les deux qui restent sont bien **deux** et non deux crans d'un seul, parce que **où** et **quoi** ne sont pas deux quantités de la même information : on peut donner l'un sans l'autre, dans les deux sens. C'est la distinction que l'analyse fait déjà pour le son, où le marquage dit où et nommer le son produit est un enrichissement séparé. Et tout en haut, ils se recouvrent : une reformulation explicite de l'IA donne déjà la phrase correcte à voix haute, que l'explication redonnerait par écrit.

**Fluidité**
- *Leviers* : la capture, en trois positions (« La capture »).

**Richesse**
- *Leviers* : le registre imposé, ou une contrainte du même genre ; la longueur imposée.

### Comment les curseurs se composent

Monter un curseur fait deux choses, qui n'ont pas le même plafond. **Retirer une aide** converge vers le réel : pas de texte, pas de réécoute, pas de préparation, c'est la vie ordinaire. **Durcir un jugement** dépasse le réel : personne, dans une vraie conversation, ne marque les sons au centième ni ne refuse une tournure correcte mais maladroite. Les crans hauts ne se conçoivent donc pas de la même façon selon qu'ils enlèvent ou qu'ils exigent.

**Les effets se cumulent d'une aptitude à l'autre, et c'est le principe.** Chercher le mot précis, tenir une forme imposée, traiter un tour dégradé : tout cela produit des silences, donc fait baisser la note de fluidité sans que l'apprenant soit moins fluide. La fluidité seule est facile, en combinaison elle est dure. Une note ne se lit donc jamais sans la combinaison qui l'a produite — la façon d'en tenir compte reste à définir, et plusieurs mécanismes sont possibles.

**Un curseur qui impose un réglage à toute la séance est le fonctionnement, pas une collision.** La fluidité possède la capture — il lui faut l'armement automatique pour que les silences veuillent dire quelque chose —, donc la monter change l'ergonomie de la séance entière.

Le test qui sépare une vraie collision du fonctionnement : **une combinaison n'est un problème que si elle change ce qu'une mesure veut dire, pas si elle rend seulement la tâche plus dure.**

Ce test donne une contrainte dure. Le bruit et le canal dégradé de la compréhension portent sur ce qui est **écouté**, jamais sur la prise de l'apprenant : l'analyse compare deux enregistrements traités symétriquement, et bruiter un seul côté rendrait l'écart mesuré en partie fabriqué (`../reference.md`).

Et **une redite ne compte que pour ce qu'elle sert à corriger**, l'élocution et la formulation : c'est une reprise d'une phrase identique, pas de la parole spontanée (« Le passage »).

## L'enjeu

Quatre crans, en gradation, qui portent sur la **séance entière** et pas sur une aptitude : il y a une note ; la note reste ; la note est comparée aux autres ; la série des notes est traitée — courbe, niveau, diplôme.

**C'est un concept pour parler d'une activité, pas une pièce du modèle.** Rien ne porte ces crans, et ce qu'ils décrivent tombe de la combinaison choisie : un défi rend une note, une campagne la compare aux précédentes, l'arcade la range dans un classement.

## La capture

La capture est **un levier de fluidité, à trois positions**, et l'échelle gradue exactement ce que la fluidité peut lire.

1. **Maintien du doigt, envoi manuel.** Ce que fait l'app aujourd'hui : on appuie pour parler, on relâche pour réfléchir, on réappuie pour continuer, on envoie quand c'est dit. Aucune mesure de silence n'est possible — entre deux segments, l'écart mesure le pouce.
2. **Armement automatique, envoi manuel.** Le micro s'ouvre dès que l'IA a fini et reste ouvert jusqu'à l'envoi. Le **délai avant de parler** et les **silences intérieurs** deviennent mesurables.
3. **Armement automatique, envoi sur un silence de plus de x**, silence du début compris. Le **silence final** s'y ajoute.

Le micro **ne s'arme jamais avant la fin de la réponse de l'IA**. Un symbole est visible dès que ça enregistre : il n'informe pas seulement, il fait partie de la pression — savoir que ça tourne change la façon dont on parle.

**Chaque tour porte sa position de capture.** C'est ce qui dit si ses silences sont significatifs, et rien ne s'agrège entre positions : agréger un tour capté au doigt avec un tour capté automatiquement produirait un chiffre qui ressemble à de la fluidité sans en être.

**La durée maximale du tour n'est pas un levier.** C'est une contrainte de l'app, aujourd'hui 30 s, posée pour une raison technique : la mémoire d'une passe d'analyse croît comme le **carré** de la durée du tour. Le tour se ferme et envoie ce qui a été dit — jeter perdrait de la parole. Elle disparaît quand le fenêtrage de l'analyse arrive (`../../TODO.md`).

## L'audio

**Un tour est une liste de segments** : une durée de silence, ou de l'audio. Le silence n'est jamais stocké en échantillons — il ne coûte alors que sa durée, là où le garder ferait passer une conversation de vingt minutes à des dizaines de mégaoctets. On reconstruit l'audio d'origine quand on en a besoin.

Chaque segment de parole garde une **marge de vrai audio** de part et d'autre. Ce qui identifie une occlusive vit dans la transition, et couper au ras de la parole abîmerait la mesure.

**On n'envoie pas de silence au réseau.** Ce que chaque mesure lit exactement — l'audio brut, les segments, la reconstruction — est à préciser mesure par mesure.

**Ce qu'on déduit de l'audio se stocke.** La purge efface l'audio ; une mesure qui ne se recalculerait plus après doit donc exister ailleurs. C'est le critère du projet appliqué ici : on stocke ce qui dépend de quelque chose qui ne se retrouvera pas.

**L'audio n'est pas purgé par défaut, et la purge est à écrire.** À ne pas confondre avec le balayage des enregistrements que plus aucun énoncé ne nomme, qui est fait et n'est pas une purge (`../reference.md`).

## Ce qui reste à spécifier

- **Ce que vaut une feuille avant d'être une lettre.** La forme de l'agrégation est écrite ; ce qui ne l'est pas est comment on passe des marques d'une feuille à son chiffre — combien de crans sur quelle longueur —, et où chaque sensibilité pose ses bornes A–E.
- **La liste fermée des sortes de déclencheurs** d'une règle, et si un déclencheur lit une mesure, une note, ou les deux.
- **Le rythme de montée de la rampe** d'arcade, maintenant qu'on sait qu'elle monte des crans entiers.
- **Le critère de réussite d'un défi**, et ce qui met fin à une séance mode par mode.
- **La part du prompt qui fabrique les occasions.** Un curseur haut ne sert à rien si la conversation ne place jamais l'apprenant devant la difficulté qu'il a demandée. Reste à partager entre ce qui passe par la parole de l'IA et ce qui passerait par une consigne hors parole (« notification » — autre nom à trouver).
- **La liste des leviers de chaque format**, close pour aucun, et le détail de ce que chaque position produit — y compris sa formulation lisible, qu'exige le mode arcade.
- **Le score** : propre à l'arcade ou pas, et à quoi ressemble son écran.
- **Garder le nom du préréglage** d'une séance réglée à la main. Aucun lecteur n'en a besoin aujourd'hui — l'origine suffit là où ça compte — donc pas de champ pour l'instant.
- **Le déroulé de chaque module**, et son écran. Le cadre est commun — l'activité, ses champs, ses statuts, son résultat — le déroulé ne l'est pas.
- **Les déclencheurs de suggestion** pendant une conversation.
- **La purge**, et la durée de vie des audios.
- **Ce que chaque mesure lit** de l'audio, segment par segment.
- **La liste de ce qui doit être agrégeable.**
