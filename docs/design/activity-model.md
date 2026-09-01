# Le modèle d'activité

Conçu le 2026-09-01, élagué le même jour de ce qui est construit, repris le 2026-09-01 après une deuxième passe qui a défait deux fausses pièces. Ce qui reste ici est la part du modèle d'activité qui n'a pas encore de code, plus ce qu'elle laisse ouvert.

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

**Le déverrouillage lit la moyenne des notes** de la séance, sur les aptitudes qui en ont une : la fluidité n'en a pas sous capture manuelle, et elle ne compte alors pas comme un zéro. Le seuil est une **donnée du bloc**, pas une règle générale — le premier bloc qui veut autre chose ne doit pas obliger à rouvrir le code commun.

Trois conséquences à ne pas redécouvrir plus tard.

**Une définition est un gabarit appliqué à la création, pas une dépendance gardée ensuite.** Les positions de leviers sont donc **toujours sur la ligne**, y compris pour une exécution issue d'une définition. L'alternative — un pointeur pour les séances de bloc, des valeurs pour les autres — ferait d'un même champ tantôt un pointeur tantôt des valeurs, et obligerait chaque lecteur à trancher le cas avant de lire. La duplication ne peut pas se décaler, puisque la définition est fixe.

**L'origine est un champ à part** — quelle définition, ou rien — et elle ne sert qu'à grouper : ouvrir le niveau suivant, ranger un score. Elle n'est jamais consultée pour savoir comment la séance était réglée.

**Une exécution issue d'un bloc n'est pas jetable.** Son résultat porte l'avancement, donc la supprimer reverrouille un niveau ou vide une entrée de classement. Une conversation libre, elle, se jette sans conséquence. C'est le prix du choix de dériver l'avancement plutôt que de le stocker, et il se dit à l'utilisateur au lieu de se faire en douce.

Ce que ça change à l'atomicité de l'activité, qui reste vraie mais pour une autre raison, est écrit dans `../reference.md`.

## Les champs qui manquent

Par rapport à ce qui est écrit (`activity/Activity.kt`), il en manque quatre, et un cinquième change de type.

- **La consigne** — du texte libre, posé au départ et injecté dans le prompt, jamais réécrit ensuite. À ne pas confondre avec la **matière**, qui dit de quoi ça parle et que l'IA peut écrire après coup : une consigne « pousse-le sur le passé, il l'évite » peut donner une conversation dont la matière finit par être « son déménagement ». Les confondre ferait qu'un titre écrit par l'IA écrase la consigne. Nom proposé : `brief` — `seed` évoque une graine de tirage aléatoire, ce que ce n'est pas.
- **La rampe**, ou rien.
- **Les conditions de fin**, qui sont une liste : on peut avoir les vies **et** un critère de réussite, ce qui donne un gagné et un perdu au lieu d'un seul.
- **L'origine**, ci-dessus.
- **Le résultat doit pouvoir porter un nombre.** Il porte aujourd'hui un verdict, un juge, une date et du texte libre ; un score d'arcade est un nombre, et le ranger dans du texte libre le rendrait inexploitable.

Et **`mode` n'est pas un champ** : « arcade », « campagne », « défi », « custom » sont des noms d'usage sur des combinaisons de ces axes-là.

## Les réglages

**Ce qui se stocke est une liste ouverte de positions de leviers, et rien d'autre.** Le critère est qu'elles répondent toutes à la même question : *où en est ce paramètre en ce moment*. Une rampe n'y répond pas, elle dit comment ça va changer ; une origine non plus, elle dit d'où les réglages viennent. Ces choses-là ont leurs champs à elles.

Deux raisons concrètes de tenir cette homogénéité. L'arcade doit **annoncer chaque changement en une phrase**, ce qui exige que toute entrée porte une phrase lisible pour chacune de ses positions — une rampe n'en a pas. Et l'état effectif se calcule depuis les réglages et l'avancement : mettre la rampe dans les réglages ferait chercher l'entrée du calcul là où il range sa sortie.

**Un curseur d'aptitude n'est pas un paramètre, c'est un préréglage** — un nom donné à un ensemble de positions. C'est le mode custom qui le prouve, puisqu'il expose chaque levier séparément : si les curseurs étaient le modèle, il faudrait une deuxième façon de décrire la même séance, et les tenir d'accord.

**Les leviers sont propres à un format.** Ceux listés plus bas sont ceux de la conversation ; un jeu de tuiles apporte les siens. Un curseur est donc un préréglage sur les leviers **d'un format**, et « élocution 2 » ne nomme pas la même chose d'un format à l'autre — deux notes venues de formats différents ne se comparent pas par la position d'un curseur.

**Chaque levier se déclare** : sa clé, ses positions possibles, sa valeur par défaut, et la phrase lisible de chacune de ses positions. Sans cette déclaration, une clé absente devient un défaut silencieux.

**Les réglages sont fixes pour toute la durée.** Ce qui bouge pendant une séance est l'**état effectif**, calculé depuis les réglages, la rampe et l'avancement. L'arcade n'est donc pas une exception : sa rampe est une donnée fixe qui décrit une variation.

**Les vies sont un levier** : trois vies pressent l'apprenant comme un seuil de silence court.

**Le curseur de difficulté de l'arcade nomme une combinaison entière**, il n'en fait pas partie. Sa rampe est donc une **montée de crans**, pas une suite de changements levier par levier.

Ce qui est écrit en code aujourd'hui — une position par aptitude — est exactement ce que ce doc dit de ne pas stocker, et c'est à refaire.

Le détail de ce que chaque levier produit est à préciser.

## La marque est invariante, ce qu'on en fait ne l'est pas

**La même faute produit la même marque, à n'importe quel moment, quels que soient les réglages.** Aucun levier ne touche jamais une marque. C'est l'exigence de `../reference.md` tenue jusqu'au bout : une marque dont la couleur ou la présence dépendrait du réglage du jour ne transporte plus rien, puisque son absence ne se distingue pas d'une approbation.

Ce que les réglages gouvernent, c'est ce qu'on **fait** de la marque : est-ce qu'il faut redire, est-ce que ça compte dans la note. **Le mécanisme reste à spécifier** — on sait ce qui doit varier, pas encore par quoi.

Deux choses en découlent et sont acquises : le rang de formulation se souligne **toujours**, et la rampe des sons est la même partout et tout le temps — la bande de bruit de ±5 n'est pas un réglage mais une propriété mesurée de la machine.

**Perte assumée** : il n'y a plus de conversation sans aucune marque grammaticale. C'est plutôt un retour au texte d'origine, qui dit que sans la trace la discrétion se retourne — on corrige et personne ne l'apprend.

## Les notes

**Une échelle unique, A–E, pour les cinq aptitudes.** Une séance se lit en cinq lettres, sans avoir à retenir cinq échelles différentes.

Les cinq n'y arrivent pas par le même chemin, et c'est une distinction qui compte. Pour la **formulation**, la lettre **est** la mesure : le modèle rend un rang. Pour l'**élocution**, la mesure est un nombre — l'écart au modèle — et la lettre est un **affichage** dont les bornes ne sont pas mesurées. On garde donc le nombre en base : les bornes pourront bouger sans abîmer les vieilles séances.

**La formulation se note en rangs**, et le modèle les rend au lieu du booléen actuel :

- **A** — ce qu'un natif dirait
- **B** — correct et naturel
- **C** — correct mais maladroit
- **D** — fautif, mais on comprend
- **E** — ne se dit pas

Le rang se calcule et se souligne **toujours**, d'une couleur qui vient du rang et de rien d'autre : A et B rien, C jaune, D orange, E rouge.

Ça absorbe un chantier qui traînait à part (`../../TODO.md`, point 6) : le verdict grammatical était un booléen sur le tour entier, ce qui écrasait le fait qu'un tour puisse porter plusieurs fautes et empêchait de marquer la portion concernée. Un rang par portion règle les deux.

**La porte grammaticale se ferme sur ce qui va être réécrit**, plus sur le marquage. La raison de la porte a toujours été qu'on ne travaille pas la prononciation d'une phrase qu'on s'apprête à réécrire ; tant que marquage et réécriture allaient ensemble, « marqué » était un bon substitut. Ce n'est plus le cas depuis que la formulation a des leviers séparés.

**Et le rang E ferme la porte quel que soit le réglage.** Une phrase qui ne se dit pas, l'app devrait la synthétiser pour l'analyser, donc la faire entendre comme modèle à imiter. Tout le montage repose sur le fait que le modèle est la vérité ; un modèle qui prononce une non-phrase empoisonne ça.

**La marque porte sur un élément, la note agrège sur le tour.** C'est vrai des trois échelles du son comme du rang : l'accent d'un mot est au bon endroit ou pas, mais un tour en contient plusieurs et la note les compte.

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

### Ce qui mesure chaque aptitude, et les leviers qui la pressent

Deux choses par aptitude, et pas plus : le **critère** qui permet de l'évaluer, et les **leviers** qui modulent la pression sur elle. Ce sont les leviers du **format conversation** ; un autre format apporte les siens. Aucune de ces listes n'est close.

**Élocution**
- *Mesure* : l'écart au modèle, aux trois échelles — les sons, l'accent du mot, la mélodie de la phrase.
- *Leviers* : voir le texte, ou redire sa phrase de tête ; un temps limité, posé en pourcentage de la durée du texte synthétisé, et dépasser oblige à réessayer ; le nombre de reprises permises ; **une sensibilité par échelle**.

Les trois sensibilités sont distinctes parce que les trois échelles ne se mesurent pas dans la même unité — des points d'écart, des demi-tons, une position de syllabe — et qu'un seul chiffre ne peut pas les gouverner toutes. Elles ne font pas non plus la même chose : la mélodie n'est jamais teintée à l'écran, les deux courbes étant toujours tracées, donc sa sensibilité ne sert qu'à la note. Un seul bruit de fond est mesuré, celui des sons.

**Compréhension**
- *Mesure* : la réponse montre qu'on a compris, ou révèle qu'on n'a pas compris.
- *Leviers* : la complexité du tour de l'IA — longueur, vocabulaire, structure ; le texte affiché ou flouté ; la réécoute, autorisée ou non, et combien de fois ; le bruit et la qualité du canal, jusqu'à simuler un mauvais réseau qui coupe des mots.

Le bruit s'applique **à la lecture**, jamais au rendu mis en cache : le même fichier sert d'étalon à la mesure, et le bruiter fausserait l'écart.

**Formulation**
- *Mesure* : le rang A–E, ci-dessus.
- *Leviers* : la réponse de l'IA sur un tour fautif, en trois niveaux — absente (erreur ignorée, réponse normale), indication indirecte (la reprise dans sa réponse, ce que le code incite aujourd'hui), ou reformulation explicite dite comme un coach reprend son élève ; l'explication de la faute en notification, à deux niveaux — la règle à utiliser seule, ou la règle plus la phrase correcte.

Le marquage a quitté cette liste : il est invariant, donc il n'est plus un levier. Les deux qui restent sont bien **deux** et non deux crans d'un seul, parce que **où** et **quoi** ne sont pas deux quantités de la même information : on peut donner l'un sans l'autre, dans les deux sens. C'est la distinction que l'analyse fait déjà pour le son, où le marquage dit où et nommer le son produit est un enrichissement séparé. Et tout en haut, ils se recouvrent : une reformulation explicite de l'IA donne déjà la phrase correcte à voix haute, que l'explication redonnerait par écrit.

**Fluidité**
- *Mesure* : le débit et les arrêts — le délai avant de commencer, le nombre et la longueur des silences.
- *Leviers* : la capture, en trois positions (« La capture »).

**Richesse**
- *Mesure* : la précision et la variété du lexique, l'adéquation du registre, la longueur et la complexité de la phrase.
- *Leviers* : le registre imposé, ou une contrainte du même genre ; la longueur imposée.

### Comment les curseurs se composent

Monter un curseur fait deux choses, qui n'ont pas le même plafond. **Retirer une aide** converge vers le réel : pas de texte, pas de réécoute, pas de préparation, c'est la vie ordinaire. **Durcir un jugement** dépasse le réel : personne, dans une vraie conversation, ne marque les sons au centième ni ne refuse une tournure correcte mais maladroite. Les crans hauts ne se conçoivent donc pas de la même façon selon qu'ils enlèvent ou qu'ils exigent.

**Les effets se cumulent d'une aptitude à l'autre, et c'est le principe.** Chercher le mot précis, tenir une forme imposée, traiter un tour dégradé : tout cela produit des silences, donc fait baisser la note de fluidité sans que l'apprenant soit moins fluide. La fluidité seule est facile, en combinaison elle est dure. Une note ne se lit donc jamais sans la combinaison qui l'a produite — la façon d'en tenir compte reste à définir, et plusieurs mécanismes sont possibles.

**Un curseur qui impose un réglage à toute la séance est le fonctionnement, pas une collision.** La fluidité possède la capture — il lui faut l'armement automatique pour que les silences veuillent dire quelque chose —, donc la monter change l'ergonomie de la séance entière.

Le test qui sépare une vraie collision du fonctionnement : **une combinaison n'est un problème que si elle change ce qu'une mesure veut dire, pas si elle rend seulement la tâche plus dure.**

Ce test donne une contrainte dure. Le bruit et le canal dégradé de la compréhension portent sur ce qui est **écouté**, jamais sur la prise de l'apprenant : l'analyse compare deux enregistrements traités symétriquement, et bruiter un seul côté rendrait l'écart mesuré en partie fabriqué (`../reference.md`).

Et **une redite ne compte que pour l'élocution**. Ce sont des tours d'élocution sur une phrase identique, pas de la parole spontanée.

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

- **Ce qui décide qu'il faut redire, et ce qui compte dans la note.** On sait que ce sont les deux choses que les réglages gouvernent, la marque étant invariante ; par quoi elles sont gouvernées n'est pas écrit.
- **Le calcul des notes.** La note par tour, la note de séance, et surtout **comment les trois échelles de l'élocution donnent une seule lettre**. Plusieurs mécanismes sont possibles, aucun n'est choisi.
- **Le rythme de montée de la rampe** d'arcade, maintenant qu'on sait qu'elle monte des crans entiers.
- **Le critère de réussite d'un défi**, et ce qui met fin à une séance mode par mode.
- **La part du prompt qui fabrique les occasions.** Un curseur haut ne sert à rien si la conversation ne place jamais l'apprenant devant la difficulté qu'il a demandée. Reste à partager entre ce qui passe par la parole de l'IA et ce qui passerait par une consigne hors parole (« notification » — autre nom à trouver).
- **Ce que devient la phrase initiale** quand on la redit : son remplacement dans le fil, ou son maintien. Jusque-là, redire ajoute et rien ne remplace.
- **Le mécanisme de re-tentative grammaticale**, analogue à celui de la prononciation : l'apprenant cherche sa correction, et le levier d'explication dit ce qu'on lui donne pour ça.
- **La liste des leviers de chaque format**, close pour aucun, et le détail de ce que chaque position produit — y compris sa formulation lisible, qu'exige le mode arcade.
- **Le score** : propre à l'arcade ou pas, et à quoi ressemble son écran.
- **Garder le nom du préréglage** d'une séance réglée à la main. Aucun lecteur n'en a besoin aujourd'hui — l'origine suffit là où ça compte — donc pas de champ pour l'instant.
- **Le déroulé de chaque module**, et son écran. Le cadre est commun — l'activité, ses champs, ses statuts, son résultat — le déroulé ne l'est pas.
- **Les déclencheurs de suggestion** pendant une conversation.
- **La purge**, et la durée de vie des audios.
- **Ce que chaque mesure lit** de l'audio, segment par segment.
- **La liste de ce qui doit être agrégeable.**
