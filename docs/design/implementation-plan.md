# Le plan d'implémentation

Ce que le chantier 0 fait, dans quel ordre, et pourquoi cet ordre-là. Écrit le 2026-09-06, contre `activity-model.md` (le modèle d'activité, conçu et non écrit), `pixel-ui.md` (l'interface en pixel doux, conçue et non écrite), `../reference.md` (ce qui est vrai et se consulte) et `../../TODO.md` (l'agenda). Doc de conception transitoire : il s'élague à mesure que le code arrive, et les commits deviennent la carte.

Il ne redit rien de ce que ces docs disent. Une étape nomme ce qu'elle fait, pourquoi elle tombe là, ce qu'elle prouve, ce qui s'y décide avec l'humain, et ce qu'elle laisse dû.

**Une étape écrite sort d'ici** — le code et les commits la portent, et ce qu'elle laisse dû passe au TODO. Les numéros ne se resserrent pas derrière elle : ils désignent une étape, ils ne la comptent pas.

## Ce qui a été acté en ouverture du chantier (2026-09-06)

Cinq choses, tranchées en session avant d'écrire une ligne.

- **Le périmètre est complet, chantier 0 et chantier 4 entrelacés**, jusqu'au tour marqué redessiné. La frontière entre le moteur et l'habillage était déjà percée dans les faits, le TODO faisant entrer les glyphes de la police et la charpente persistante dans la première implémentation.
- **L'ordre est commandé par ce qui coûte cher si ça passe après**, avec deux exceptions au fil logique du pur vers le branché : le contrat enrichi monte juste après les déclarations parce qu'une mesure l'attend à cet endroit précis, et le socle de l'écran monte avant le marquage par empans.
- **La capture entre**, ses trois positions et l'audio en segments. Ne garder que la position au doigt rendrait muettes trois feuilles sur onze, priverait deux des six sortes de déclencheur de leur horloge, et poserait l'arbre des poids de la conversation libre sur une branche vide.
- **Un seul appel au modèle de langue**, pas de bifurcation. La mesure de latence reste au plan et rend un chiffre ; si le chiffre est mauvais, les leviers sont un modèle sans raisonnement pour ce maillon ou un prompt plus court, jamais couper l'appel en deux.
- **La portée des tests** : un test porte sur une propriété qui reste vraie quand les valeurs changent. Les valeurs y entrent comme matériau, jamais comme ce qu'il affirme. Le réglage fin sur critère de langue appartient au banc de calibration hors de l'app, feuille par feuille, avant le dépôt public.

Deux lignes du TODO tombent du même coup, et ce n'est pas un oubli. Les « deux choses à discuter avant de commencer » n'en bloquent aucune : le **remplacement de la phrase dans le fil** est tranché depuis par `activity-model.md` (« Le passage ») — une redite remplace à l'affichage et jamais en base, toutes les tentatives restent sous le passage —, et les **déclencheurs de suggestion** attendent des prescripteurs qui sont hors périmètre, seul l'apprenant en étant un aujourd'hui.

## Ce qui reste dehors

Repris de la décision du 2026-09-05, plus ce que cette session y ajoute. Le critère n'est pas « la conversation libre s'en sert-elle » mais « est-ce défini, et sinon combien coûte de le définir ».

- **Les modules autres que la conversation libre**, et les écrans qui tombent avec eux : l'avant-partie, le résultat, le custom, le score et son écran.
- **Le bruit et le filtre du canal** : les deux leviers se déclarent, ils ne s'utilisent pas — ni fichier de fond, ni liste d'effets, tant qu'une position ne vaut pas une difficulté comparable d'un fond à l'autre.
- **Le mode payant et les voix spécifiques.** Seules les quatre voix génériques existent.
- **Les sons de l'app**, dont rien n'est décidé.
- **La purge de l'audio**, indépendante de tout sauf de la couture cache/base déjà tranchée.
- **Le validateur de définitions**, écrit en stub : le point d'appel existe et ne vérifie rien.
- **Les prescripteurs conversation et progression**, qui attendent le catalogue des activités.
- **Le lecteur d'écran**, hors v1 et dit plutôt que tu.
- **La liste de ce qui doit être agrégeable** : question ouverte de `reference.md` qui ne bloque aucune étape et se répondra quand un prescripteur progression existera.
- **Le fenêtrage de la passe d'analyse**, qui est au chantier 1 : le plafond de 30 s reste où il est.

## Ce qui se décide avec l'humain, et ce qui ne le demande pas

Le critère : ce qui relève d'un jugement de langue, d'un goût visuel, ou d'une valeur qu'un banc réglera plus tard passe par lui ; la forme du code, l'arithmétique et ce que les docs ont déjà tranché, non. Chaque étape porte sa liste ; voici la règle qui les gouverne.

**Avec lui** : la norme de la correction et la frontière d'`intended` ; les colonnes des trois marquages jugés ; les séries de chaque feuille et le nombre de positions de sensibilité ; l'arbre des poids de la conversation libre ; les phrases lisibles de chaque position de levier ; les valeurs des deux palettes et de la rechange daltonisme ; le facteur d'échelle ; la liste des glyphes et leur dessin ; la forme de la charpente ; ce que la fenêtre d'attente porte en conversation libre ; ce que chaque mesure lit de l'audio ; le format d'un fichier de définition ; le levier à tirer si la latence est mauvaise.

**Sans lui** : la forme des types, le schéma Room et ses migrations, le parsing du contrat, toute l'arithmétique, la résolution par vagues, quels tests s'écrivent, la mécanique du thème, le rendu du marquage et toutes les valeurs que `pixel-ui.md` a déjà réglées à l'œil, l'emplacement des fichiers, l'ordre des commits dans une étape, et la notation des provisoires au TODO.

**Deux cas s'arrêtent et remontent, même hors de la première liste** : quand l'application révèle que deux choses tranchées se contredisent, et quand une porte que le doc a fermée devrait rouvrir.

## Trois choses qui traversent toutes les étapes

- **Toutes les valeurs vivent au catalogue et nulle part ailleurs.** Aucune borne, aucune colonne, aucun seuil en dur dans un calcul. Le banc de calibration doit pouvoir déplacer un nombre sans rouvrir une ligne de logique.
- **Un provisoire se note au TODO à l'instant où il s'écrit**, jamais après. Écrit après coup, il ne s'écrit pas.
- **La persistance n'est pas une étape, c'est une couture** : un champ neuf s'écrit en base dans l'étape qui le crée. Trois migrations en sortent, aux étapes 3, 10 et 11, manuelles et conservées indéfiniment.

## Les étapes

### 7. La rechange daltonisme

Les deux registres sont écrits, portés par les sept nombres du banc et dérivés d'eux. Ce qui reste est la moitié qui n'a aucune valeur nulle part.

**Ce que ça fait.** Une palette de rechange, en préférence utilisateur, qui tient **la même règle de marquage** — la distance perceptuelle OKLab sur le premier cran, dans les deux registres — et qui trouve pour les crochets deux teintes restant séparées sous la perception visée.

**Pourquoi avant l'étape 16.** Le cas qui l'exige est le pire du doc : *juste* et *à côté* sont la même forme et deux verdicts opposés, séparés par la seule couleur. En deutéranopie la marque d'une réussite devient celle d'une faute. Si les crochets naissent d'abord, ils se posent sur une rampe qu'on remplace ensuite.

**Avec l'humain.** Les valeurs, au banc, qui ne les porte pas encore. Et **ce qu'elle couvre** : les confusions rouge-vert d'abord, la tritanopie — qui toucherait le bleu du contour de mélodie — couverte ou déclarée hors périmètre.

### 9. La couture d'analyse

Les quatre feuilles calculées sont écrites (`fluency/Fluency.kt`), et la traduction d'un offset du texte retenu vers le tour entier aussi (`judged/Kept.kt`). Ce qui reste est de les brancher.

**Ce que ça fait.** `examine(said, model, text, kept)` : `text` reste la chaîne affichée où toutes les marques s'indexent et porte les hésitations, `kept` dit les morceaux sur lesquels le modèle a été synthétisé. Le modèle ne se synthétise plus que sur le texte retenu ; sa grille se joint à ce texte-là, et ses offsets reviennent au tour entier **avant** `Added.found`, qui compare les deux jonctions et exige qu'elles soient dans le même repère — la jonction de l'apprenant, elle, se fait sur le texte entier, puisqu'il a tout dit. Puis les **temps par mot** se tirent des sons (`saidMs`, `modelMs` et leurs offsets de caractères) pour remplir `Fluency.Turn`.

**Ce qui se prouve.** Que les marques restent sur leurs lettres quand le tour porte des hésitations — c'est-à-dire que le remap est bien posé avant ce qui compare les deux jonctions.

**Avec l'humain.** Le **seuil de la pause**, que le doc pose à 200 ms et veut au-dessus de la plus longue fermeture d'occlusive. Il est mesurable sur les prises gardées du téléphone, qui portent les temps par son : à faire plutôt qu'à laisser posé à la main.

### 10. La capture en trois positions et l'audio en segments

**Ce que ça fait.** Un tour devient une **liste de segments** — une durée de silence, ou de l'audio, le silence jamais stocké en échantillons —, chaque segment de parole gardant sa marge de vrai audio, et rien de silencieux ne part au réseau. Les trois positions : le doigt, l'armement automatique, l'armement avec envoi au silence de plus de x. La préparation, les deux décomptes visibles, le symbole d'enregistrement. Le levier `jeter-la-prise`, qui gouverne enfin le bouton *Redo* que `ui/ConversationScreen.kt` porte déjà sans que rien ne le règle, et qui est sans objet à la troisième position. Chaque tour porte **sa position de capture** et **comment il s'est fini**, envoyé ou interrompu et par laquelle des deux horloges. Le retrait des plages vides. Migration Room sur `utterances`.

**Pourquoi ici.** Le TODO le différait exprès ; la décision d'implémenter le système complet le ramène, et pas par respect de la lettre : sans lui trois feuilles sur onze sont muettes, deux des six sortes de déclencheur n'ont pas d'horloge, la feuille du tour interrompu perd une de ses deux causes, et l'arbre des poids de la conversation libre se pose sur une branche vide. Il vient après les feuilles calculées parce que ce sont elles qui disent ce que les segments doivent rendre.

**Ce qui se prouve.** Que le micro ne s'arme jamais avant la fin de la réponse de l'IA. Qu'un tour interrompu est tronqué et envoyé tel quel, jamais coupé en deux tours. Qu'aucun silence ne dépasse x en troisième position. Que rien ne s'agrège entre positions de capture.

**Avec l'humain.** **Ce que chaque mesure lit** de l'audio — le brut, les segments, la reconstruction —, mesure par mesure, à écrire avant d'implémenter et non différé. Le **seuil de niveau** du retrait des plages vides, qui demande des tours spontanés hésitants que le banc n'a pas ; le seuil de durée, lui, est sûr à l'ordre de la demi-seconde, hors du domaine des phonèmes.

### 11. L'activité change de forme

**Ce que ça fait.** Les sept champs qui manquent et le huitième qui change de type : le `brief`, les règles, le journal des changements appliqués, l'origine — la définition **et sa version** —, les consignes par marquage jugé, l'arbre des poids, la version du moteur de règles, et le résultat qui peut porter un nombre. Les réglages cessent d'être une position par aptitude pour devenir une **liste de positions de leviers**, qui est exactement ce que le doc dit de stocker. L'énoncé porte **qui parle** comme une identité et non plus apprenant-ou-IA. Migration Room sur `activities`.

**Pourquoi ici.** Parce que l'étape suivante produit du journal et des positions effectives, et qu'un champ non persisté à l'instant où il existe est une séance qui ne se recalcule plus après un redémarrage. C'est le seul endroit où ce plan s'écarte de la colonne vertébrale posée en session, qui rangeait la persistance plus tard ; la règle « un champ se persiste dès qu'il existe » l'emporte.

**Ce qui se prouve.** Qu'une exécution porte toujours ses positions **sur sa ligne**, y compris issue d'une définition — jamais un pointeur qu'il faudrait suivre. Que l'origine ne sert qu'à grouper et n'est jamais consultée pour savoir comment la séance était réglée. Qu'un changement de version du moteur de règles **retire la reprise** au lieu de rejouer le journal sous une autre sémantique.

**Ce que ça laisse dû.** L'identité du locuteur est en place et une seule voix la remplit : la distribution et les personnages multiples sont écrits comme champ et n'ont pas d'écran.

### 12. Le moteur de règles

**Ce que ça fait.** Son propre module, hors de `TurnPipeline`. Les six sortes de déclencheur et les trois moments. Les trois sortes d'effet, et pas une de plus : un patch, la fin, un message au modèle. Un patch porte des positions ou des déplacements, et éventuellement des consignes ; il porte sa phrase de mise en scène avec son drapeau avant/après, la phrase mécanique étant déclarée avec le levier. Les trois familles — levier, consigne ou interrupteur, drapeau. La résolution **par vagues** contre un même instantané. La vérification terminale des vies **après** les vagues, sur l'état stabilisé. Le menu calculé et non maintenu.

**Pourquoi ici.** C'est de la logique pure qui tourne en JVM, et le TODO en fait un module à part dès le départ : `TurnPipeline` fait déjà 575 lignes et tout y converge.

**Ce qui se prouve.** La **terminaison** : une règle ne part qu'une fois par moment, il y a un nombre fini de règles, donc la cascade s'arrête même si deux règles se relancent. La détection de **deux patchs d'une même vague sur la même clé**, qui est une erreur d'écriture sauf s'ils posent la même position absolue. La **mort scriptée**, R1 qui se désarme et arme R2, qui est le cas exact où la fin ne doit pas gagner la course. Qu'un déplacement borné ne notifie rien. Que la surcharge s'écrit en deux règles dont une seule est armée à tout instant, sans plafond ni ordre de déclaration.

**Ce que ça laisse dû.** Qui regarde les conflits visibles à l'écriture : le validateur est en stub, son point d'appel existe.

### 13. Le passage, les tentatives, les deux portes

**Ce que ça fait.** Le déroulé écrit du doc, porté dans le pipeline, en **deux temps** : au retour de l'appel, puis à la fin de l'analyse. Le passage est un énoncé et toutes ses redites — **dérivé de la suite** par ce que `repeats` dit déjà, sans table à lui. Ses quatre états. Les deux compteurs de tentatives, reformulations et redites, qui ne se volent rien. **L'historique envoyé au modèle se retourne** : `TurnPipeline.history()` écarte aujourd'hui les redites, ce qui est juste tant qu'une redite porte le même texte, et devient faux dès qu'une reformulation en est une tentative avec d'autres mots — il faut alors la dernière tentative de chaque passage, qui est ce que le doc décrit. Le gros bouton ferme le passage ; en « attend » il n'est pas disponible et revient à l'épuisement. La continuation et l'écho arrivent ensemble et l'app joue l'un des deux, donc rien n'est jamais rétracté. Les deux portes à la barre A–B, plus les deux faits qui ferment celle des mots sans lire de note — le tour tronqué et le tour sans un mot retenu. L'analyse du son ne tourne pas si la porte des mots s'est fermée ou si un mot est marqué `ne se dit pas`.

**Ce qui se prouve.** L'ordre des deux portes, celle des mots avant que l'analyse ait l'occasion de parler. Que la note du passage est celle de la **dernière** tentative, pour toutes les feuilles. Que les deux épuisements ne tombent pas au même endroit, celui des reformulations avant l'analyse du son, celui des redites après. Que « attend » avec zéro tentative permise n'est pas un cul-de-sac. Que rien de ce qui est jugé ne s'éteint quand la porte se ferme, l'éteindre par sa propre décision étant circulaire.

### 14. Les définitions livrées, et la conversation libre écrite comme définition

**Ce que ça fait.** Une définition est de la **donnée dans un fichier livré avec l'app**, autoportant, lu directement — jamais importé en base, qui serait la copie d'une source déjà sur le disque. Son identité, sa version héritée de la release, son contenu, ses consignes, ses positions de départ, son arbre des poids, ses règles, ses questions. Plus les deux champs d'affichage que la session d'UI a déclarés : le **titre court**, plafonné à dix caractères, que porte la ligne d'état ; et un **nom court par personnage**, que porte la ligne qui nomme le tour. Un fichier porte ses traductions en table langue vers texte, ce qui est l'écart délibéré déclaré au manifeste contre la norme d'i18n de la facette `android`. Le lecteur, et le validateur en stub au chargement. Puis **la conversation libre écrite comme une définition livrée**, qui est le test du format et retire le « par défaut » qu'il aurait fallu câbler ailleurs.

**Pourquoi ici.** Parce que tout ce qu'une définition déclare existe maintenant : les leviers, l'arbre, les règles, les consignes.

**Ce qui se prouve.** Qu'une définition qui change ne réécrit jamais le passé, les positions étant copiées sur la ligne. Que le lecteur unifie une source unique aujourd'hui et deux demain sans copie.

**Avec l'humain.** Le **format du fichier** — c'est son ergonomie d'auteur, il écrira dedans. L'**arbre des poids de la conversation libre**, et ses consignes de départ s'il y en a.

### 15. La charpente, les cadres, les descendantes

**Ce que ça fait.** Deux objets distincts remplacent la rangée de boutons de `MainActivity.Root` : une **ligne d'état** en haut, une **barre d'actions** en bas, deux lignes de grille en permanence, les actions là où est le pouce, et un endroit pour ce que `reference.md` exige partout — qu'une chose indisponible porte sa raison. Le narrateur d'état existe déjà : c'est le `Text` de `ui/ConversationScreen.kt` qui dit *hearing*, *thinking*, *speaking*. Il monte dans la seconde ligne du haut, dans son cadre, toujours pleine, et il gagne les raisons. La pile de navigation qui descend, les quatre écrans actuels étant un interrupteur à quatre positions. Les cadres, écrits en caractères : la police porte déjà ses seize pièces à partir de U+E000, en deux tons par superposition de deux couches de texte, à poser sur un pas de rangée de 11.

**Ce qui se prouve.** La règle du cadre, vérifiée en écrivant un écran : si une chose est encadrée, on doit pouvoir dire ce qu'on fait avec ; sinon c'est un aplat. Jamais l'écran entier, jamais le fil ni le tour marqué.

**Tranché le 2026-09-06**, et écrit dans `pixel-ui.md` (« La charpente ») : la règle de partage — en haut ce qui est vrai, en bas ce qu'on peut faire —, les deux lignes du haut, la barre d'actions d'une ligne en bas, et le fait qu'un dessin fin n'oblige pas à une cible tactile fine.

### 16. Le tour marqué redessiné

**Ce que ça fait.** La **bande de mélodie** de 22 pixels au-dessus de la ligne, horizontale calée sur les caractères, la courbe peinte comme une surface et non comme une suite de segments, l'apprenant dessous sur le bout rouge de la rampe et le modèle par-dessus dans un bleu calme, à la même épaisseur : bien parler fait disparaître la couleur. Halo extérieur et opaque, mélangé vers le fond. Puis les marques : la teinte sur les lettres, le filet d'accent sur une syllabe, la **vaguelette** de correction, les **crochets** de pertinence, la **colonne de points** des pauses dans le blanc qui existe déjà, et les fragments écartés en encre atténuée entre crochets de vrais caractères. L'air, l'ordre de peinture, le filet et la vaguelette chacun dans sa rangée. La **ligne qui nomme le tour** devient structurelle : la pastille du suivi, puis le débit en caractères, calés à droite, un emplacement vide disant *non mesuré*. Le **menu de conversation** qui dit quelles marques s'affichent, du côté de l'apprenant et jamais un levier.

**Ce qui se prouve.** Qu'un groupe porte son identité et non son étiquette, donc qu'un groupe coupé par un retour à la ligne ouvre d'un côté et ferme de l'autre. Que la courbe ne se coupe qu'aux fragments écartés et reste d'un seul tenant aux pauses. Qu'éteindre un canal n'éteint que son affichage et jamais sa mesure.

**Tranché le 2026-09-06** : les marques des mots **restent** une fois la porte passée, et les retirer est une préférence ; l'écran de 720 se détend au recompte, quinze colonnes sur vingt-et-une, avec un nom court par personnage et la règle que le nom se tronque et jamais les marques. **Reste avec l'humain** : le débordement d'un interligne sur l'autre, à régler en ajustant l'air.

### 17. L'écran de conversation sous le nouveau déroulé

**Ce que ça fait.** Ce que le TODO disait impossible à écrire avant le déroulé d'un module, et qui l'est maintenant. Le tour de l'IA **brouillé par défaut**, qui est la position de défaut du levier `tour-ia.affichage` et non une préférence, par carrés de 2 pixels posés au hasard mais fixes. Sous le tour, la rangée de commandes au padding égal, **six entrées** : le petit bouton dont l'étiquette dit la porte du moment, *redire* ou *reformuler*, et qui ne paraît que sur le passage ouvert ; l'écoute ; le côté, une seule étiquette qui bascule ; la vitesse ; les **notes du passage**, qui ouvrent le bilan ; et la **loupe**, qui ouvre `ui/AnalysisReadout.kt` — lequel cesse d'être gardé par `Trace.on` et devient une pièce de l'app. Le gros bouton sur toute la largeur et trois lignes. **Trois notifications et non deux** (2026-09-06) : celle d'une règle qui change quelque chose, mécanique obligatoire et narrative facultative, qui est un **pop-up** éphémère ; celle qui dit de reprendre après une faute de son, qui donne à **écouter** le modèle et ne nomme rien ; et celle de la **porte des mots**, qui **nomme la ou les aptitudes en cause**. Les deux dernières sont des états et vivent dans la ligne d'état du haut tant qu'elles sont vraies.

**Tranché le 2026-09-06**, et écrit dans `pixel-ui.md` (« Ce qui se passe entre deux tours ») : c'est l'**écran de bilan du passage**, un seul écran à deux portes — poussé entre deux passages, ou ouvert à la demande depuis n'importe quel passage du fil. Ce que le mode coupe, ce sont les **lettres** et jamais les mesures, donc en conversation libre l'écran existe et porte les mesures brutes. Il est poussé par défaut, ce que l'apprenant règle.


## Ce que le plan laisse dû quand il est fini

À reverser au TODO plutôt qu'à laisser ici, une fois chaque pièce écrite.

- Ce qui reste dehors, listé plus haut, dont la purge et le validateur réel.
- Les valeurs que le **banc de calibration** reprendra : les séries de chaque feuille, les colonnes, la ligne du gros raté, le délai de grâce, le seuil de la pause.
- Les **trois inconnues du juge sous le contrat enrichi** — sa répétabilité, la fidélité d'`intended`, la puissance qu'il faut — qui ne se mesurent qu'à l'usage et se recomptent en resoumettant des tours gardés.
- Les **deux frontières du prompt** que rien ne prouve : la persona qui atteindrait `intended`, et un historique qui déplacerait le marquage. Elles se vérifient au banc du chantier 2.
- La **fréquence des crans hauts**, `juste` et `entre les lignes`, qu'un juge trop généreux rendrait décoratifs.
- La **symétrie du débit** : si trop lent et trop rapide ne se valent pas, la série devient deux listes au lieu d'une.
