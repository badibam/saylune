# Le modèle d'activité

Conçu le 2026-09-01, élagué le même jour de ce qui est construit, repris le 2026-09-01 après une deuxième passe qui a défait deux fausses pièces, puis le 2026-09-02 par la passe qui a descendu la note sur la mesure et écrit la grille, par celle qui a écrit ce que vaut une feuille pour toute l'élocution, puis le 2026-09-03 par celle qui a posé le tour interrompu, par celle qui a étendu le silence aux deux bords, par celle qui a écrit le remplissage et les reprises, et par celle qui a refondu la correction et la pertinence autour d'un seul marquage — la première absolue, la seconde seule à porter la consigne — puis par celle qui a écrit la compréhension et arrêté les cinq noms d'aptitude, et par celle qui a dit ce qui fait un levier et sorti les poids des réglages, puis par celle qui a énuméré les leviers de la conversation et pesé la compréhension sur la difficulté, par celle qui a dit quand une règle se déclenche, par celle qui a écrit la fin d'une séance et son issue, et par celle qui a fait des définitions de la donnée posé l'histoire comme une campagne à mémoire déclarée, ouvert les règles au modèle des deux côtés, et donné aux consignes une durée et trois lecteurs, puis le 2026-09-04 par celle qui a inventorié l'état d'une séance en trois natures et écrit ce que le modèle reçoit, et par celle qui a logé les définitions dans des fichiers, fait de l'ouverture un paquet d'effets et donné aux règles de quoi faire parler l'IA, par celle qui a ouvert les conditions à tout l'arbre et aux deux moitiés de l'échelle, et par celle qui a retiré la notion de format. Ce qui reste ici est la part du modèle d'activité qui n'a pas encore de code, plus ce qu'elle laisse ouvert.

Ce qui est parti et où le lire : l'**énoncé**, l'**activité** et **ce qui se stocke** sont dans `../reference.md` pour la règle et dans le code pour la forme (`activity/Activity.kt`, `conversation/TurnPipeline.kt`, `store/`). La suppression de la **session** et de la **parenthèse** est actée dans `../reference.md`. Les commits sont la carte.

Ce qui est écrit ici est tranché, sauf mention contraire. Ce qui reste ouvert est en fin de doc, nommé.

## Un nom d'usage se pose sur des axes, il ne les remplace jamais

Deux fois de suite, une notion familière s'est révélée n'être qu'un nom posé sur une combinaison.

Le **curseur d'aptitude** d'abord : ce qui se règle, ce sont des leviers, et « élocution 2 » est un nom sur un jeu de positions. Le **mode** ensuite : ce qui se décrit, c'est d'où viennent les réglages, s'il y a une rampe, et ce qui met fin ; « arcade » est un nom sur une combinaison de ces trois-là.

Une troisième fois depuis, et sur le premier des deux : le curseur d'aptitude n'est pas la forme générale des raccourcis, c'en est un. *Easy* et *hard* en arcade en sont d'autres, et chaque mode aura les siens.

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

Une **définition** est une activité écrite d'avance, et **c'est de la donnée, pas du code**. On la rejoue autant qu'on veut.

Elle porte : une **identité** et une **version** ; son **contenu** — ce qui ouvre la séance, le `brief`, les personnages ; ses **consignes**, au plus deux, une par marquage jugé ; ses **positions de leviers de départ**, sensibilités comprises ; son **arbre des poids**, constant pour toute la séance ; ses **règles** ; et ses **questions**, auxquelles le modèle répond en fin de séance (« L'histoire »).

**Ce qui reste du code, c'est le catalogue** — les leviers avec leurs positions, l'arbre des feuilles, les sortes de déclencheurs, les sortes d'effets. Rien de ce qu'une définition contient n'est de la logique : des positions déclarées, des poids sur un arbre déclaré, du texte libre, et des règles faites de sortes énumérées. Une histoire de vingt scènes écrite en Kotlin serait du contenu qu'on ne peut ni corriger, ni traduire, ni partager, et il faudrait recompiler l'app pour changer une réplique. Et le doc veut déjà qu'un modèle puisse écrire une définition contre le catalogue, ce qui n'a aucun sens si sa sortie doit être compilée.

**Cette donnée vit dans des fichiers livrés avec l'app**, autoportants, et l'app les lit directement. Ce qui est acquis de toute façon : les réglages d'une exécution sont copiés sur sa ligne, donc une définition qui change ne réécrit jamais le passé ; et l'**origine** doit nommer la définition **et sa version**, sans quoi une mise à jour qui corrige une scène rend incomparables deux parties qui se croient les mêmes.

Trois raisons, dont deux sont l'argument ci-dessus retourné d'un cran. **La version arrive gratuitement** : un fichier livré hérite de celle de la release, quand une ligne créée à l'exécution n'en a aucune de naturelle et demande qu'on la frappe et qu'on lui écrive une migration. **Corriger, traduire et partager** restent possibles, ce qu'une table perd à moitié — rien ne traduit une base, et partager y veut dire exporter. Et **ce que le modèle fabrique à la volée n'est pas une définition** : un personnage inventé pendant une séance remplit les champs d'une activité, donc une ligne et pas un gabarit. Il ne devient une définition que le jour où on veut le **rejouer**, et c'est ce jour-là, nommé, qui ouvrira la table — à côté des fichiers, le lecteur unifiant les deux à la lecture. Une interface, deux sources, zéro copie.

**Importer les fichiers en base est écarté** : la table serait la copie d'une source déjà sur le disque, lisible et versionnée, donc la deuxième source qui se décale que le projet refuse partout, plus une réconciliation à chaque mise à jour de l'app.

**Un fichier porte ses traductions, en table langue → texte.** Une définition mélange deux langues par construction : le `brief` et la mise en scène qui partent au modèle sont en anglais, langue de la conversation et du critère du juge ; ce que l'apprenant lit est dans sa langue d'interface. Un fichier par langue dupliquerait la prose anglaise et la ferait diverger d'une copie à l'autre ; des clés vers `res/` casseraient l'autoportance, donc l'argument du partage. C'est un **écart délibéré à la facette `android`**, déclaré au manifeste : sa norme d'i18n a été écrite pour le texte d'**interface**, et la prose d'une scène est du **contenu** — aucun jeu ne livre ses dialogues en ressources de plateforme. Le prix est réel : une plateforme de traduction ne lit pas ce format.

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

Par rapport à ce qui est écrit (`activity/Activity.kt`), il en manque six, et un septième change de type.

- **La consigne** — du texte libre, posé au départ et injecté dans le prompt, jamais réécrit ensuite. À ne pas confondre avec la **matière**, qui dit de quoi ça parle et que l'IA peut écrire après coup : une consigne « pousse-le sur le passé, il l'évite » peut donner une conversation dont la matière finit par être « son déménagement ». Les confondre ferait qu'un titre écrit par l'IA écrase la consigne. Nom proposé : `brief` — `seed` évoque une graine de tirage aléatoire, ce que ce n'est pas.
- **Les règles** — une liste, qui absorbe la rampe, les conditions de fin et les conditions branchées sur l'arbre des notes (« Les règles »).
- **Le journal des changements appliqués**, sans quoi une séance dont un tirage ou l'IA a modifié les réglages ne se recalcule plus.
- **L'origine**, ci-dessus.
- **Les consignes par marquage jugé** — du texte libre, une par marquage au plus, distinct du `brief` de l'activité (« Ce qui est jugé, ce qui est calculé »).
- **L'arbre des poids** — ce sur quoi la séance regarde, fixé à l'écriture. Les sensibilités, elles, ne sont pas un champ neuf : ce sont des positions de leviers, donc des réglages (« Ce qui fait un levier »).
- **Le résultat doit pouvoir porter un nombre.** Il porte aujourd'hui un verdict, un juge, une date et du texte libre ; un score d'arcade est un nombre, et le ranger dans du texte libre le rendrait inexploitable.

Et **`mode` n'est pas un champ** : « arcade », « campagne », « défi », « custom » sont des noms d'usage sur des combinaisons de ces axes-là.

## L'état d'une séance

Ce qui existe à un instant donné dans une activité donnée. L'inventaire sert deux choses à la fois : savoir ce qu'une reprise doit pouvoir reconstituer, et savoir ce que le prompt a le droit de dire.

**Trois natures, et il n'y en a pas de quatrième.** C'est le critère du projet appliqué à l'état entier — on stocke ce qui dépend de quelque chose qui ne se retrouvera pas, on recalcule ce qui ne dépend que des lignes (`../reference.md`).

- **Déclaré** — l'entrée figée, jamais réécrite. De la définition : l'origine, ce qui ouvre la séance, le `brief`, la distribution, les consignes de départ, les positions de leviers de départ, l'arbre des poids, les règles, les questions. De l'apprenant et non de la définition : son **nom**, sans lequel aucune scène ne peut le désigner ; l'accent, donc la voix de référence ; et les fournisseurs choisis par maillon, qui décident quelles briques sont allumées.
- **Enregistré** — ce qui ne se recalcule pas : les énoncés et leur audio, les mesures de chaque tentative, le journal de ce qu'un tirage ou l'IA a choisi, et les faits portés par chaque tour — quelle position de capture était en vigueur, comment il s'est fini et par laquelle des deux horloges.
- **Dérivé** — recalculable à tout instant depuis les deux autres : les positions effectives des leviers, les consignes en vigueur et ce qui reste de leur durée, les feuilles, les notes, le compte de passages et de tentatives, le blocage, la porte du son, l'avancement, l'issue, et les deux horloges qui tournent, le temps écoulé n'étant que maintenant moins le départ.

**Une quatrième nature a été essayée et ne tient pas.** « Éphémère » devait ranger ce qui n'existe que le temps d'un tour — les horloges, le menu, le message qu'une règle vient de poser —, et aucun des trois ne s'y range : les horloges laissent une trace enregistrée, le menu se recalcule depuis les règles et l'état, le message est écrit dans la règle donc il est déclaré. Durer peu n'est pas une nature ; c'est une propriété à l'intérieur du dérivé, dont certaines parties se recalculent par passage et d'autres par tentative.

**La position déclarée d'un levier et sa position effective sont deux choses**, et les confondre est ce qui fait paraître contradictoire que les réglages soient fixes pour toute la durée pendant que les vies se perdent. La définition écrit la première, qui ne bouge jamais ; les patchs déplacent la seconde, qui est ce que l'apprenant vit.

## Les réglages

**Ce qui se stocke est une liste ouverte de positions de leviers, et rien d'autre.** Le critère est qu'elles répondent toutes à la même question : *où en est ce paramètre en ce moment*. Une rampe n'y répond pas, elle dit comment ça va changer ; une origine non plus, elle dit d'où les réglages viennent. Ces choses-là ont leurs champs à elles.

Deux raisons concrètes de tenir cette homogénéité. L'arcade doit **annoncer chaque changement en une phrase**, ce qui exige que toute entrée porte une phrase lisible pour chacune de ses positions — une rampe n'en a pas. Et l'état effectif se calcule depuis les réglages et les règles : mettre une règle dans les réglages ferait chercher l'entrée du calcul là où il range sa sortie.

**Ce qui se règle sans passer par un levier est un préréglage** — un nom donné à un paquet : des positions, et éventuellement des fragments de consigne et des règles. C'est le mode custom qui le prouve, puisqu'il expose chaque levier séparément : si les préréglages étaient le modèle, il faudrait une deuxième façon de décrire la même séance, et les tenir d'accord.

**Le curseur d'aptitude est un préréglage parmi d'autres, pas la forme générale.** *Élocution 2* en est un ; *easy* et *hard* en arcade en sont d'autres, qui nomment une combinaison entière ; une brique de lieu ou de personnage en est un troisième, qui apporte aussi de la consigne et des règles. **Quels préréglages chaque mode offre reste à écrire**, et rien n'oblige un mode à en avoir un par aptitude.

Un préréglage peut déplacer des **sensibilités** en plus des leviers d'aides — c'est ce qui rend *durcir* exprimable, et c'est possible depuis que la sensibilité est un levier. Il peut aussi poser des **poids**, ce que la brique *Règles* fait déjà (« Les briques ») ; ce qui reste interdit est qu'un **patch** en déplace un pendant la séance. Il est un **gabarit appliqué à la création**, comme une définition : ce qui reste sur la ligne ensuite, ce sont les positions.

**Un curseur d'aptitude, lui, ne pose que de la pression** — les aides qu'on retire et la sévérité —, jamais le poids de son aptitude. Ce sont deux questions : *combien j'exige* et *sur quoi on regarde*, et on peut vouloir une élocution qui compte beaucoup mais jugée avec indulgence. Le poids se pose dans l'arbre.

**Une activité est une conversation, et « format » n'est pas un axe.** Ce qu'on rangeait sous d'autres formats existe déjà comme des moments *dans* la conversation : la lecture à voix haute **est** redire, un texte de référence connu d'avance que la conversation libre ne peut pas offrir ; la répétition après un modèle **est** le remède écrit de toute faute sonore, entendre puis redire autant qu'on veut, que le cache des synthèses sert déjà. Un format neuf aurait dupliqué cette machinerie pour ce que le geste de redire porte.

Un champ dont toutes les lignes portent la même valeur ne décrit pas le modèle, il décrit une intention : il est donc retiré, comme `mode` l'est déjà. **Les leviers sont les leviers d'une activité, point** — plus d'appartenance à trancher à chaque ligne. Ce qui se garde de la phrase est ailleurs et suffit : une note ne se lit jamais sans la combinaison qui l'a produite. Le jour où une vraie deuxième forme d'échange se présente, c'est une migration, manuelle et explicite, ce que la facette `android` demande de toute façon.

**Chaque levier se déclare** : sa clé, ses positions possibles, sa valeur par défaut, et la phrase lisible de chacune de ses positions. Sans cette déclaration, une clé absente devient un défaut silencieux.

**Les réglages sont fixes pour toute la durée.** Ce qui bouge pendant une séance est l'**état effectif**, calculé depuis les réglages, les règles, le journal et l'avancement. L'arcade n'est donc pas une exception : sa rampe est une règle, une donnée fixe qui décrit une variation.

**Les vies sont un levier** : trois vies pressent l'apprenant comme un seuil de silence court. Et **sa position est le nombre restant**, pas une allocation posée à côté d'un compteur — la définition écrit sa position de départ, un patch la déplace d'un cran, zéro met fin. C'est ce qui fait qu'il n'y a pas d'objet compteur de vies, donc pas de sorte d'effet « retirer une vie » à côté de « poser un patch » (« L'état d'une séance »).

**Le curseur de difficulté de l'arcade nomme une combinaison entière**, il n'en fait pas partie. Sa rampe est donc une règle qui monte **un cran entier**, pas une suite de changements levier par levier.

Ce qui est écrit en code aujourd'hui — une position par aptitude — est exactement ce que ce doc dit de ne pas stocker, et c'est à refaire.

### Ce qui fait un levier

**Des positions déclarées et fermées, et un côté dur.** Les deux ensemble : sans positions déclarées il n'y a ni phrase à afficher ni valeur à valider, et sans côté dur la notification ne peut pas annoncer *ça se durcit* ou *ça s'allège*.

Le test écarte deux choses qu'on rangerait là par réflexe.

**La voix n'en est pas un.** Entre deux voix il n'y a ni dur ni facile, et ses positions ne sont pas déclarées — elles arrivent du catalogue du fournisseur, qui change avec la clé. Comment un personnage obtient sa voix reste ouvert (« Le personnage ») ; quel que soit le mécanisme retenu, ce ne sera pas un levier.

**Le registre non plus.** Il est ordonné — familier, courant, soutenu — mais aucun bout n'est le difficile : le soutenu demande du vocabulaire et des tournures, le familier des idiomes et des réductions qu'aucun manuel ne donne, et c'est le milieu qui est facile. Il vit donc dans la consigne de pertinence, avec la longueur imposée et le mot interdit.

**Ce qui ne se règle pas par un levier doit se dire là où on serait allé chercher le levier.** L'écran custom montre des leviers : qui veut imposer un registre y cherchera un curseur, ne le trouvera pas, et conclura que ça ne se fait pas. Le catalogue donné au modèle pour qu'il écrive une définition a le même trou en pire — il inventera un levier. Il faut donc une entrée pour la consigne elle-même, disant ce qu'on peut y mettre.

**Deux formes, et la seconde n'est pas une version dégradée de la première.**

- **À marches** — une liste fermée de positions nommées, chacune avec sa phrase écrite d'avance. L'écho, l'avance, la capture.
- **À nombre** — une unité, un minimum, un maximum, un pas, et une phrase à trou : « tu as {n} vies », « le tour part après {n} secondes de silence ». Les vies, les tentatives, les seuils, les durées.

Forcer les nombres en marches coûte tout de suite. « Vies : peu / normal / beaucoup » interdit à un défi d'en demander quatre, et ment à l'écran, où l'apprenant voit trois cœurs et pas le mot *normal*. Rien ne se perd en échange : un nombre est ordonné tout seul, donc la direction se calcule pareil dans les deux formes, et le menu envoyé au modèle ne change pas — il choisit entre des patchs tout faits, jamais une valeur.

**Le maximum d'un levier à nombre peut être « pas de maximum ».** Les écoutes et les tentatives se laissent volontiers illimitées, et un très grand nombre à la place mentirait à l'écran comme la marche nommée mentait plus haut.

**Un levier peut en appeler un autre, et la coupe suit une seule règle : zéro éteint le levier quand c'est vrai, sinon deux leviers.** Zéro réécoute *est* la réécoute interdite, zéro reformulation permise *est* le petit bouton qui n'apparaît pas : un levier suffit. Le seuil de silence, lui, n'a aucune valeur qui voudrait dire « pas de seuil » — zéro enverrait le tour aussitôt — et ce qui sépare la position 2 de la position 3 n'est pas x mais le fait que le tour parte tout seul. Deux leviers, donc, et le seuil est **sans objet** aux positions 1 et 2.

**Sans objet ne veut pas dire absent.** La valeur reste sur la ligne, elle n'est pas lue, et elle est là si la capture remonte en 3. Elle s'affiche, grisée, en portant sa raison — la règle du projet pour toute option éteinte (`../reference.md`). Un patch qui déplace un levier sans objet ne change rien à l'écran et annonce un durcissement qui n'a pas lieu : c'est une erreur d'écriture du défi, pas un cas à traiter.

**Chaque levier déclare aussi qui le tient : l'app, ou le modèle.** Les vies, le seuil de silence, la capture, les tentatives permises, la durée du tour sont **exécutés** — l'app les fait, donc ils sont vrais. La longueur et la complexité du tour de l'IA, l'écho, l'explication de la faute sont **demandés** : l'app les écrit dans le prompt et rien ne vérifie qu'ils sont tenus, ce dont le projet a déjà la preuve sous les yeux avec `intended`, qui arrive en texte libre sans toujours respecter sa consigne (`../../TODO.md`).

Trois choses en découlent, et aucune n'est un détail. Une **condition ne peut pas lire un levier demandé**, faute de fait à lire. Le **banc ne peut pas l'éprouver**. Et sa **notification annonce quelque chose qui pourrait ne pas arriver** — *« il va parler plus dense »*, et il parle pareil ; on l'affiche quand même, se taire laisserait l'apprenant sans rien pour comprendre ce qui a été tenté. Surtout, ça dit la vérité à qui écrit un défi : monter la complexité n'est pas une garantie, c'est une demande.

**La sensibilité est un levier, le poids n'en est pas un.** Aller vers sévère durcit, toujours et pour tout le monde ; monter le poids de la mélodie durcit la séance de qui l'a mauvaise et allège celle de qui l'a bonne, donc sa direction dépend de l'apprenant, que le levier ne connaît pas et n'a pas à connaître. C'est cohérent avec ce que chacun fait : la sensibilité dit *combien on exige*, le poids dit *sur quoi on regarde*, et viser autre chose n'est ni plus dur ni plus facile. Ça corrige une phrase que ce doc portait, où les deux étaient des positions de leviers.

Il y a donc **une sensibilité par feuille**, déclarée dans l'unité de cette feuille — indulgent, normal et sévère ne posent pas les mêmes bornes sur un pourcentage de sons et sur des demi-tons. Et **l'arbre des poids est un champ à part**, ce qu'il voulait être de toute façon : une structure à branches dont les poids se multiplient en descendant ne rentre pas dans une liste plate de positions sans clés bricolées.

**Les poids sont constants pour toute la séance, et aucun patch n'en déplace.** Ce qu'on vise se décide à l'écriture du défi et ne bouge plus. Sinon la note de fin serait une moyenne de mesures prises sous des règles différentes, illisible pour l'apprenant comme pour un classement — c'est l'argument servi partout ici : une note ne se lit pas sans la combinaison qui l'a produite, donc il faut qu'il y en ait une.

**Le prix est réel** : un défi qui ouvre ses exigences une par une — « fais-toi comprendre », puis « et la grammaire », puis « et ne t'arrête plus » — ne peut pas ne noter que ce qui était demandé au moment où on parlait. L'exercice à étages reste écrivable, mais par les **conditions**, qui ne passent pas par le poids : « à partir du tour 4, une faute de grammaire te fait reprendre ta phrase » ne touche aucun poids. Ce qui s'ouvre en cours de route est ce qui bloque, pas ce qui compte.

**À vérifier quand l'arcade s'écrira** : sa rampe monte un cran entier en cours de partie. Si passer d'*easy* à *hard* veut dire regarder plus de choses, elle déplace des poids, ce qui est interdit ici — les quatre crans ne feraient alors varier que la sévérité et les aides.

**L'écran d'avant-partie tombe de tout ça sans rien coûter.** Chaque position déclare sa phrase lisible — écrite pour les notifications de l'arcade —, donc l'écran qui montre ce qui attend l'apprenant avant qu'il lance **se génère** : ce qu'on lui demande, ce qu'on lui enlève, ce qui lui fera perdre, ce qui met fin, et les consignes. Zéro texte à écrire par activité, et ça rejoint le fait qu'accepter et lancer sont deux gestes. **Ce qu'il montre exactement reste ouvert** : « ce qui s'écarte du défaut » ne marche pas, une conversation libre ayant ses leviers là où l'apprenant les a laissés et une scène ayant les siens — il n'y a pas d'ordinaire à quoi comparer.

### Le catalogue des leviers

**Brouillon, non tranché.** Ce tableau applique à chaque levier nommé ailleurs dans ce doc la déclaration exigée juste au-dessus. Les **phrases lisibles par position**, dont tombent la notification d'arcade et l'écran d'avant-partie, sont une seconde passe. Le **défaut** est celui d'une conversation libre : ce que l'app fait quand personne n'a rien demandé.

**Quatre choses à ne pas confondre, et la confusion est facile.** Une **feuille** est une mesure — ce que l'app observe de ce que l'apprenant a fait, et que personne ne règle. Un **levier** est un réglage de la séance, qui ne mesure rien. Une **sensibilité** est un levier attaché à une feuille, qui ne touche pas la mesure mais déplace ses bornes A–E : *40 % de sons ratés* est la mesure, que ce soit un C ou un E est la sensibilité. Un **poids** est lui aussi attaché à une feuille et **n'est pas un levier**, sa direction dépendant de l'apprenant ; il vit dans l'arbre des poids. Il y a donc autant de sensibilités que de feuilles, et la table des sensibilités ressemble à une liste de feuilles pour cette seule raison.

**Les positions d'un levier à marches se déclarent de la plus facile à la plus dure**, donc le côté dur n'est pas un champ : c'est la dernière. Un levier **à nombre** garde le champ, son ordre étant arithmétique et non réglable — tous ceux d'aujourd'hui ont le bas pour côté dur, moins de vies et moins de temps étant plus durs, mais le débit d'un personnage aura le haut.

**Le groupement par aptitude est de la présentation**, pour l'écran custom et pour ce doc. Un levier ne déclare aucune appartenance.

**Élocution**

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `ecoutes-modele` | nombre | 0 à sans maximum ; 0 vaut *de mémoire* | sans maximum | l'app |
| `cadence` | marches | libre, imposée | libre | l'app |
| `cadence.valeur` | nombre | % de la durée du modèle | — | l'app |
| `redites-permises` | nombre | 0 à sans maximum | sans maximum | l'app |

`cadence.valeur` est **sans objet** quand `cadence` est libre.

**Compréhension**

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `tour-ia.longueur` | marches | courte, moyenne, longue | moyenne | le modèle |
| `tour-ia.complexite` | marches | basse, moyenne, élevée | moyenne | le modèle |
| `tour-ia.affichage` | marches | le texte, seulement qui parle, rien | le texte | l'app |
| `reecoute` | nombre | 0 à sans maximum ; 0 vaut *interdite* | sans maximum | l'app |
| `bruit` | marches | aucun, présent, fort | aucun | l'app |
| `filtre` | marches | aucun, léger, marqué | aucun | l'app |

Les positions de `tour-ia.complexite` sont **volontairement génériques** : c'est un levier demandé, donc le modèle l'interprète selon la scène, et des noms prescriptifs lui retireraient cette souplesse sans rien garantir en échange.

**Le bruit et le filtre étaient un seul levier et en font deux**, parce qu'ils sont indépendants : une pièce calme sur une mauvaise ligne, un café bruyant sur une ligne nette. Le **bruit** est un fond sonore, donc un fichier, donc du contenu que la brique *Lieu* possède déjà ; le **filtre** est un traitement du signal — la bande passante d'un téléphone, un hachage périodique. Les deux s'appliquent **à la lecture**, jamais au rendu mis en cache, le même fichier servant d'étalon à la mesure.

**Ce qui reste ouvert est la standardisation, et une moitié seulement est solide.** Une même position doit valoir une difficulté comparable d'un fond à l'autre, sinon le levier ne veut rien dire. Le **rapport signal/bruit en décibels** standardise le niveau : il se calcule et il est comparable. Il **ne standardise pas la difficulté** — un fond de conversations est bien plus dur que du bruit rose au même rapport, parce que c'est de la parole concurrente, et rien ici ne mesure cet écart. Le filtre, lui, demande sa propre liste fermée d'effets nommés portant chacun son intensité déclarée ; comment elle s'encode n'est pas décidé.

**Correction**

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `echo` | marches | explicite, indirect, absent | indirect | le modèle |
| `explication` | marches | la règle et la phrase, la règle, aucune | aucune | le modèle |
| `avance` | marches | poursuit, attend | poursuit | l'app |
| `reformulations-permises` | nombre | 0 à sans maximum | sans maximum | l'app |

`echo` et `explication` sont des **aides**, donc leurs positions se lisent à l'envers de l'intuition : c'est l'absence qui est le cran dur. Elles se déclarent quand même du facile au dur, comme tout le monde.

**`avance` en « attend » est le cran dur parce qu'on ne sort pas d'un passage sans l'avoir réparé** : le gros bouton n'est pas disponible, donc on ne peut pas s'échapper en disant simplement autre chose. Trois choses à tenir séparées autour de là, et elles sont écrites ailleurs : perdre une vie n'est jamais automatique, c'est une condition qu'un défi écrit ; continuer ou attendre est ce levier-ci, indépendant de la première ; et la seule sortie d'un passage bloqué est l'épuisement des tentatives. La combinaison *attend* avec zéro tentative permise n'est donc pas un cul-de-sac : les tentatives sont épuisées d'emblée, le passage est raté et se ferme aussitôt, et « attend » n'attend jamais.

**Fluidité**

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `capture` | marches | doigt, armée, armée et envoi au silence | doigt | l'app |
| `seuil-silence` | nombre | secondes | — | l'app |
| `duree-tour` | nombre | secondes, maximum 30 | 30 | l'app |
| `preparation` | nombre | secondes | 0 | l'app |

`seuil-silence` et `preparation` sont **sans objet** en capture au doigt, où c'est le pouce qui arme. Le maximum de `duree-tour` est le plafond technique, et il remonte quand le fenêtrage de l'analyse arrive.

**`preparation` est le temps entre la fin de la réponse de l'IA et l'armement du micro.** Elle était nommée une fois en passant, parmi les aides qu'un curseur retire, sans levier ni définition. Elle en a une maintenant, et c'est celle-là plutôt qu'un temps de réflexion accordé *à l'intérieur* du tour : ce second sens serait une ligne interne à la mesure de fluidité, et une ligne interne ne se règle jamais — c'est déjà le sort du délai de grâce d'une seconde. Vivant hors du tour, elle ne touche aucune mesure.

**Pertinence** — aucun levier, et ce n'est pas un oubli. Le registre, la longueur imposée, le mot interdit n'ont pas de côté dur, donc ils ne sont pas des leviers : ils s'écrivent dans la **consigne**, que cette aptitude est la seule à porter. Il lui reste ce que toute feuille a, sa sensibilité et son poids.

**L'activité** — ne pressent aucune aptitude en particulier.

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `vies` | marches | pas de vies, comptées | pas de vies | l'app |
| `vies.restantes` | nombre | 0 à sans maximum ; 0 met fin | — | l'app |
| `condition.<nom>.active` | marches | inactive, active | écrit par le défi | l'app |

**Ce qui est un levier est l'interrupteur d'une condition, pas la condition.** Celle-ci est une règle ; son interrupteur est ce qu'un patch déplace, avec sa phrase lisible. Il y en a un par condition qu'un défi écrit, donc il ne se déclare pas au catalogue mais avec sa règle, et sa position de départ est celle que la définition pose.

**Ce que l'exercice a trouvé.**

**Trois leviers nommés au singulier en sont deux chacun**, et la règle de coupe déjà écrite les découpe sans qu'on ait à en inventer une : zéro éteint le levier quand c'est vrai, sinon deux leviers. Le seuil de silence l'était déjà ; la **cadence** et les **vies** le deviennent. Dans les deux cas, aucune valeur du nombre ne veut dire *pas de contrainte* — une cadence à 0 % exigerait l'instantané, zéro vie met fin, donc rien n'exprime « cette activité n'a pas de vies ». Trois cas sur trois est un signe que la règle est la bonne.

**L'explication de la faute manquait sa position d'absence.** Le doc en nommait deux, la règle seule et la règle plus la phrase ; sans une troisième qui dit *aucune*, une conversation ordinaire, où l'app n'explique rien, n'était pas exprimable.

**La sensibilité est inerte sur une feuille binaire.** Le tour interrompu vaut 0 ou 1 sur un passage, donc toutes les bornes possibles rendent la même paire de lettres et bouger le curseur ne change rien. Ce n'est pas un défaut à réparer : cette feuille travaille par sa **condition** et par son **poids**, et le doc le dit déjà — une feuille peut n'exister que pour les conditions. Ce qu'il faut en tirer est que la sensibilité d'une feuille binaire ne se règle pas, et que l'écran custom ne doit pas offrir un curseur qui ne fait rien.

**Le côté dur cesse d'être un champ sur les leviers à marches**, par la convention d'ordre posée en tête. Il ne survit que sur les leviers à nombre, dont l'ordre est arithmétique.

**Et la voix quitte définitivement la liste.** « Le personnage » disait qu'elle devenait une position de levier sur l'activité ; le test la refuse — pas de positions déclarées, pas de côté dur. C'est un **champ de l'activité**, que la distribution porte déjà, et qu'aucun patch ne déplace.

## Les règles

Ce qui change pendant une séance, et quand. La version la plus bête est une liste `passage → changement` ; elle ne suffit pas pour deux raisons distinctes — tous les déclencheurs ne sont pas des numéros de passage, et le décideur n'est pas toujours la donnée écrite.

Ces deux manques se règlent en séparant trois choses que le mot « rampe » tenait collées : **quand**, **quoi**, **qui choisit**.

```
règle
  quand   : { sorte, moment, paramètres }  # quatre sortes, trois moments
  choix   : [ paquet, paquet, ... ]        # un seul élément = pas de choix
  qui     : écrit | hasard | IA

paquet    : [ effet, effet, ... ]          # un effet : patch, fin, ou message au modèle
```

**Un patch porte des positions de leviers, et éventuellement des consignes.** Les positions ont exactement la forme des réglages — des clés et des positions — et héritent donc des phrases lisibles déclarées avec chaque levier. Appliquer, c'est superposer ; annoncer, c'est lire la phrase ; proposer à l'IA, c'est envoyer les phrases et attendre une clé.

**Annoncer dit aussi le sens du changement.** Lire « deux tentatives permises » ne dit pas si on vient de monter ou de descendre, et ce n'est pas la même nouvelle. Les positions d'un levier sont ordonnées et chaque levier sait de quel côté est le dur — c'est déjà ce qui fait qu'un curseur qu'on monte retire une aide ou durcit un jugement —, donc un patch connaît sa direction par comparaison et la notification annonce *ça se durcit* ou *ça s'allège*, plus la phrase.

**Sauf la consigne, qui s'annonce sans direction.** C'est du texte libre : rien ne peut comparer deux consignes et dire laquelle est plus dure. Elle s'annonce donc en distinguant seulement les deux cas, *consigne modifiée* avec son texte, ou *consigne retirée*. Ne rien dire serait pire — une exigence qui apparaît ou disparaît en silence rend la note incompréhensible.

**Un patch porte une seconde phrase, de mise en scène, écrite d'avance.** La phrase mécanique est déclarée avec le levier, donc une seule fois pour toute l'app : « cinq secondes de silence » ne peut pas se dire *le barman s'impatiente* dans un pub et *le recruteur attend* dans un entretien. La face qui joue la scène dépend de la scène, donc elle vit sur le patch, dans la règle qui l'écrit.

**La mécanique se lit après la réplique, la narrative dit laquelle des deux places elle prend.** Elles ne font pas le même travail : *« il te reste une vie »* constate ce qui vient d'arriver, donc c'est un reçu et il vient après ; *« tu te fais bousculer par un passant »* plante le décor du tour qui suit, donc lu après la réplique il fait tomber l'excuse du passant de nulle part. La phrase de mise en scène porte donc un drapeau **avant / après**, par défaut après, sur le patch où elle vit déjà. La mécanique, elle, est toujours après : elle n'a rien à mettre en place.

**Les deux s'affichent, et la narrative ne remplace jamais la mécanique.** Qui ne lit que *« le barman semble pressé »* ne sait pas que son tour part maintenant tout seul au bout de cinq secondes, et croira à un bug la première fois que ça lui arrive — or toute la valeur de la phrase mécanique est qu'il puisse reconstruire pourquoi sa note a bougé. La mécanique est donc obligatoire, et une conversation libre n'affiche qu'elle, n'ayant pas de fiction ; la narrative est facultative, et une définition qui ne l'écrit pas est sèche, pas cassée. Les deux vont dans le même sens : *le barman se détend* posé sur un patch qui durcit est un mensonge, et la direction, que la mécanique connaît, permet de le voir à l'écriture.

**Elle n'est pas produite par le modèle.** Elle s'affiche à côté d'une affirmation mécanique, donc un texte inventé au moment où ça tombe peut la contredire ; et les patchs sont déclarés d'avance de toute façon, y compris quand c'est le modèle qui choisit lequel s'applique. Il garde tout le reste : il **joue** le changement dans son tour, où il est libre et où c'est gratuit.

**Ce qui manque se voit, mais on ne peut pas dire ce qui pourrait manquer.** Le catalogue ne propose aucune liste complète des occasions de parler, et il ne le peut pas : une condition sur un élément de feuille est un espace paramétré — quelle feuille, laquelle des trois lectures, quelle valeur — et le déclencheur *le modèle juge que oui* est de la prose libre. Ce qui se dénombre, ce sont les règles qu'une définition a **écrites**, et c'est précisément ce qui sert un auteur : l'écran d'écriture affiche ses douze règles et dit lesquelles n'ont pas de phrase de mise en scène. Pas « voici tout ce qui peut arriver », mais « voici ce que tu as écrit, et voilà ce qui est nu ». Un oubli ne casse rien, la phrase mécanique tombant seule, et les rares événements génériques — perdre une vie, finir, bloquer — arrivent tout faits par une brique (« Les briques »).

Ça donne à l'arcade ce qui lui manquait. Elle devait annoncer chaque changement en une phrase ; elle en a deux, une qui dit la règle et une qui dit le monde, sans quoi monter d'un cran ne ressemble qu'à un compteur.

**Une règle ne se retire pas ; ce qu'elle a fait se défait.** Un patch déplace un levier dans les deux sens, donc alléger est un patch comme un autre. Un patch qui porterait sur les règles elles-mêmes ferait un second étage sans phrase lisible, et plus personne ne saurait ce qu'une définition fait sans l'exécuter. Une règle qui ne doit plus s'appliquer est une règle dont le déclencheur ne se déclenche plus.

Trois choses tombent de cette forme.

**La rampe cesse d'être un champ** : c'est une règle dont le quand est « tous les N passages », le quoi un cran de plus, le qui « écrit ». **Et les conditions aussi** : « une feuille sous la barre → une vie » est une règle dont le patch retire une vie. Deux champs se replient en une liste.

**Et une condition s'allume et s'éteint comme n'importe quoi d'autre, parce que c'est un levier à deux positions** : active — « un silence de plus de cinq secondes coûte une vie » — et inactive — « les silences ne coûtent plus rien ». Deux textes plutôt que la négation du premier, qui donnerait du français bancal alors que tout l'intérêt de ces phrases est de se lire. Un patch la déplace, la notification lit la phrase, et la direction se sait : active est le côté dur.

**Le menu d'un tour est calculé, pas maintenu** : c'est l'ensemble des choix offerts par les règles qui se déclenchent maintenant. La plupart des tours il est vide, et on n'envoie rien.

**Ce qu'un tirage ou l'IA a choisi s'écrit sur l'activité.** C'est le prix de ces deux décideurs et il suit le critère du projet : on stocke ce qui dépend de quelque chose qui ne se retrouvera pas. Sans ce journal, l'état effectif d'une séance ne se recalcule plus, et une séance ne se compare plus à elle-même trois semaines après. Garder une graine de tirage coûterait moins cher et ne rejouerait juste que si le code n'a pas bougé.

**Les sortes de déclencheurs sont une liste fermée et courte**, déclarées comme les positions d'un levier. La pente est d'y glisser un mini-langage de conditions ; ce jour-là, plus personne ne sait ce qu'une définition fait sans l'exécuter.

**Une règle se résout avant que l'IA réponde**, parce qu'elle doit pouvoir fabriquer l'occasion de la contrainte qu'on vient de poser. Le cycle d'un passage est donc : il se ferme, les règles se déclenchent, l'IA répond en connaissant déjà ce qui a changé, son tour est dit, la notification affiche le changement quelques secondes hors du temps de parole, le micro s'arme. Quand c'est l'IA qui choisit, **elle choisit et répond dans le même appel** — deux sorties, pas deux allers-retours, ce qui compte quand la latence est le premier défaut du projet. Contrepartie : elle choisit en sachant ce qu'elle a envie de dire, et le menu est ce qui borne ça.

### Quand une règle se déclenche

**Trois moments, et ils ne se distinguent pas par le goût mais par ce qui est calculé à cet instant.**

- **Pendant l'enregistrement.** Deux horloges tournent, visibles toutes les deux : le temps d'enregistrement écoulé et le silence en cours. Aucune feuille n'existe encore, la personne est en train de parler — donc une règle de ce moment ne peut lire qu'une horloge. Ce n'est pas une restriction posée, c'est un fait sur ce qui existe.
- **À la fin d'une tentative.** Le tour est parti, le modèle a répondu, l'analyse a tourné. Se décide là ce qui concerne cette tentative : la porte du son, et si la conversation attend ou poursuit.
- **À la fermeture du passage.** La note du passage est celle de la dernière tentative et le compte des tentatives est connu. Tombe là tout le reste : les patchs, la rampe, les vies, la fin de la séance.

**Un passage se ferme au gros bouton, pas quand un tour part.** Un passage est un énoncé et toutes ses redites, donc il contient autant de tentatives qu'on en fait, et en « attend » il ne peut pas se fermer du tout. C'est pourquoi le blocage ne peut pas attendre la fermeture : la règle qui décide d'attendre est précisément ce qui l'empêche.

**Cinq sortes de déclencheur, et la liste est fermée.**

- **une horloge atteint sa valeur** — laquelle des deux, et la valeur. Les deux sont le **temps maximal d'enregistrement**, dont les 30 s ne sont que le plafond technique et jamais la valeur réglée, et le **seuil de silence** de la troisième position de capture.
- **un nœud de l'arbre dit quelque chose** — lequel, laquelle des lectures qu'il offre (un élément, le chiffre, la note — les deux premières sur une feuille seulement), et une valeur.
- **un compte de passages** — à tel passage, ou tous les N.
- **le modèle juge que oui** — une phrase en prose, *« s'il dépasse les limites de la politesse »*, et il répond oui ou non.
- **un levier a bougé** — lequel, et dans quel sens. Il lit un **changement**, jamais une position, donc il ne rouvre pas la porte que ferme « La fin d'une séance et son issue » ; et il reste fermé et déclaré, puisque les leviers le sont. Son moment est celui du patch qui a bougé le levier.

La cinquième existe pour que la porte de devant soit **composable**. Réagir à la perte d'une vie, sans elle, oblige à coller le même message sur chaque règle qui en retire une, donc à le réécrire autant de fois qu'il y a de façons d'en perdre — l'exhaustivité qu'un auteur ne peut pas tenir. Avec elle, la réaction s'écrit une fois pour la scène, quelle que soit la règle qui l'a causée. Elle coûte une garde : **un déclencheur de levier n'en déclenche pas un autre**, un seul saut, sinon deux patchs se relancent l'un l'autre et plus personne ne sait ce qu'une définition fait sans l'exécuter.

**Le quatrième ne coûte pas d'appel** : le modèle répond dans celui qu'on fait déjà, en un champ énuméré — le contrat le moins cher qui soit. Il porte le drapeau **demandé** avec tout ce qu'il implique : personne ne le vérifie, il ne se rejoue pas, le banc ne l'éprouve pas. Et il peut parfaitement parler de langue, c'est une app de langue ; ce qui le borne est ailleurs et suffit — **une décision du modèle ne produit jamais une marque ni une note**, les trois effets ne touchant aucun marquage. *« Elle se braque parce qu'il est trop familier »* est une conséquence d'histoire, pendant que l'empan est marqué *à côté* à l'écran : deux choses déclenchées par le même comportement, pas deux verdicts concurrents. Reste un coût d'auteur — les deux peuvent sembler se contredire à l'écran, et c'est à la scène de les accorder.

La première sorte n'a que le premier moment, la troisième que le dernier : il se déduit. **La deuxième doit dire lequel des deux**, et c'est une vraie distinction — « la correction est sous B » veut dire *bloque maintenant* à la fin d'une tentative, et *perds une vie* à la fermeture du passage. Deux règles différentes qui lisent la même feuille.

À l'intérieur de la fin de tentative, l'instant exact **se déduit de la feuille** et ne se déclare pas : une feuille de son n'existe pas avant que l'analyse ait fini, une feuille de langue existe dès le retour de l'appel. C'est l'ordre des deux portes que le doc écrit déjà — les mots d'abord, le son ensuite.

**Le début d'une séance n'est pas un déclencheur.** Un déclencheur existe pour *éprouver* quelque chose à un moment qui revient ; le début arrive une fois et sans condition, il n'y a rien à tester. Ce qui ouvre la séance est donc un champ de la définition. La fin, elle, se déclenche bien — elle est l'effet d'une règle (« La fin d'une séance et son issue »).

**Mais ce champ porte un paquet d'effets, pas un texte.** Ce qui manquait au début n'était pas son *quand*, c'était son *quoi* : ouvrir une scène demande souvent deux choses à la fois — un message au modèle, *« tu bouscules Simon et tu t'excuses platement »*, et une phrase affichée à l'apprenant, *« tu te fais bousculer par un passant »*. Un champ de texte n'en porte qu'une. Le paquet est celui qu'une règle porte déjà, donc zéro type neuf et toute l'expressivité des règles : l'ouverture peut poser des leviers, parler au modèle, écrire à l'écran. Les trois façons d'ouvrir — l'apprenant parle en premier, le personnage dit une réplique écrite, le modèle improvise sur consigne — sont trois façons de remplir ce paquet, pas trois champs.

Rien ne pousse vers l'une des trois pour la latence : à l'ouverture, l'apprenant vient d'appuyer sur *lancer* et n'attend pas sa propre réponse. Trois secondes après un bouton sont un chargement de jeu, pas la médiane de 11,0 s qui est le défaut du projet.

## Ce que l'app exécute, ce que l'IA interprète

Le dosage ne se décide pas sur ce qu'un modèle sait faire, mais sur **ce que l'app doit relire ensuite**.

- Ce que **personne ne relit** est du texte libre, sans limite : comment le personnage parle, ce qu'il raconte, sur quoi il rebondit, comment il réagit. Zéro champ, zéro validation, coût nul. C'est là qu'il faut être généreux — c'est gratuit, et c'est ce qui fait vivre la scène.
- Ce que **l'app doit exécuter** — poser un levier, retirer une vie, finir, imposer une contrainte — coûte un **champ de retour**, donc un contrat de plus, une validation de plus, et un trou silencieux s'il n'est pas validé. Le projet a déjà le cas sous les yeux : `intended` arrive en texte libre et rien ne vérifie qu'il tient ses consignes (`../../TODO.md`).

D'où la forme qui rend la liberté bon marché : **prose libre à l'aller, clés énumérées au retour.** L'app déclare ce qui est disponible — les leviers ont déjà leurs positions et leur phrase lisible —, l'IA **choisit une clé**, elle n'invente pas. Un champ, validé par appartenance. Et c'est ce que les modèles font le mieux : choisir dans une liste est fiable, calibrer une contrainte neuve ne l'est pas.

**La borne n'est donc pas la liberté de l'IA, c'est la déclaration** : rien d'indéclaré ne peut être choisi, faute d'exécutant. Un menu peut couvrir tous les leviers si on veut. **Non mesuré** : choisir dans cinq options est probablement plus fiable que dans soixante, et rien ne le dit ici — offrir une liste courte est une prudence, pas un résultat.

## Ce que le modèle reçoit

**Un seul appel fait tous les métiers** — jouer le personnage, reconstruire `intended`, marquer les empans, juger le suivi, rendre la difficulté de son tour, et choisir dans le menu quand une règle le lui offre. Ce n'est pas le prix qui tranche : un second appel ne coûterait rien en latence, le jugement ne servant qu'à afficher des marques pendant que la réponse se synthétise et se joue, ni en argent, le modèle de langue se comptant en millièmes d'un tour à trois centimes (`character-voices.md`). Ce qui tranche est qu'un prompt bien structuré tient ses frontières. On regarde donc au cas par cas ce qui déteint, et une frontière un peu floue peut même servir la scène ; on ne cloisonne pas d'avance contre un loup qu'on n'a pas vu.

**L'ordre des champs de retour est la cloison qui reste gratuite.** Le modèle écrit sa réponse en séquence et chaque champ écrit conditionne le suivant, donc `intended` rédigé avant que la voix du personnage soit prise vaut mieux que le contraire. D'où le contrat : `intended`, le marquage des empans, le suivi, `spoken`, la difficulté du tour qu'il vient d'écrire, et `title` s'il y a lieu. Le marquage absorbe `faulty`, qui était un booléen sur le tour entier (« Ce que ça change au marquage »). Le coût en latence est borné par une mesure déjà au dossier : le modèle achève son objet 0,16 s après sa première phrase (`../reference.md`), et l'ordre des champs se joue à l'intérieur de cet intervalle.

**L'état n'atteint le modèle que par la porte de devant.** Il reçoit en permanence les leviers qu'il tient — les *demandés*, qui n'existent que comme instruction — et rien d'autre. Le reste, une vie perdue, un passage raté, un seuil qui se raccourcit, ne lui parvient que si une règle a décidé de le lui dire, par un message au modèle, dans les mots d'un auteur.

C'est ce qui donne à l'auteur le contrôle de ce que son personnage sait. Sans règle, la faute ne change rien à la scène ; avec elle, le réceptionniste soupire et doute qu'on ait vraiment réservé. Coller l'état au prompt en permanence le ferait au contraire réagir toujours, dans toutes les scènes, sans que personne l'ait voulu — et un réceptionniste qui lâche « il vous reste une chance » casse sa propre fiction.

Ça retire un champ qu'on avait posé puis repris : un drapeau *se dit au modèle* sur chaque levier. Il ne passe pas le test qui range les phrases — ce qui a une seule bonne réponse pour toute l'app est au catalogue, ce qui dépend de la scène est sur l'objet de la scène —, puisque justement la réponse dépend de la scène. Et la porte de devant fait déjà le travail, sans champ neuf.

**Des tours passés, on renvoie les répliques et jamais les marques.** L'API est sans mémoire, donc toute la conversation repart à chaque appel, et ce que chaque tour passé porte est une décision. Les marques restent dehors pour la raison déjà écrite au sujet de `faulty` : un modèle qui voit ses vingt derniers verdicts devient cohérent avec eux plutôt qu'avec le tour qu'il lit. Les redites restent dehors aussi, et ce qu'on renvoie est la dernière tentative, comme l'écran — le personnage n'a pas à savoir que la phrase a été dite trois fois, et l'entendre bégayer trois fois le pousse à en parler. Contrepartie assumée : il ne rebondira pas de lui-même sur une faute qui revient, et une règle est ce qu'il faut pour ça.

**Ce n'est pas la taille qui tranche**, et le chiffre se pose ici pour qu'on ne le rejoue pas : un tour dépense environ 300 caractères entre la réponse et `intended`, donc trente tours tiennent dans 2 000 à 2 500 jetons, et les empans marqués ajouteraient à peu près la moitié. C'est petit dans les deux cas. Ce qui tranche est l'ancrage, et il se mesure : repasser le même tour dans deux historiques très différents, et regarder si le marquage change.

**Le détail par son ne se pose même pas.** Un tour de quinze mots fait une cinquantaine de sons, chacun avec son écart et ses lettres, soit plusieurs fois la taille du tour : le prompt s'y noierait. Et tenir compte des sons pour répondre n'est pas le travail du personnage — c'est celui du prescripteur progression, qui lit ce qui est dû et n'existe pas encore.

### Les quatre parties

Ordonnées par fréquence de changement, ce qui est aussi l'ordre où une instruction est le mieux suivie : ce qui gouverne le tour est le plus près de lui.

1. **Le contexte de l'app** — permanent, identique pour toute activité et tout utilisateur : ce qu'est l'app, le contrat de sortie, et les invariants d'`intended` — ne jamais réparer la grammaire, les nombres en toutes lettres, ne jamais compléter un tour interrompu. C'est la constante déjà en service (`providers/ConversationPrompt.kt`).
2. **Le contexte de l'activité** — figé au lancement : le `brief`, la distribution, les réponses des scènes précédentes, et les questions auxquelles il répondra à la fin.
3. **L'historique** — les tours passés, répliques seules. C'est le seul morceau stable en tête et variable en queue : il ne se réécrit jamais, il s'allonge par le bout.
4. **Le présent** — reconstruit à chaque tour : les consignes en vigueur, l'état des leviers qu'il tient, le message qu'une règle vient de poser, le menu s'il y en a un, et les faits du tour qu'on lui donne — sa transcription, et comment il s'est fini.

**Les consignes sont en 4 et non en 3**, alors qu'elles ne changent qu'une poignée de fois par séance et qu'elles ont donc l'air d'appartenir au semi-stable. Posées avant l'historique, elles seraient enterrées sous trente tours au moment précis où elles doivent gouverner le tour suivant. Ce qu'on perd est un préfixe mis en cache, qui se compte en millièmes ; ce qu'on gagne est une instruction lue là où elle s'applique.

**Le `brief` se coupe en deux, et pas plus** : la **situation**, vraie pour tout le monde et lisible aussi à l'écran d'avant-partie, et la **mise en scène**, qui ne s'adresse qu'au personnage et ne s'affiche jamais, sous peine de se saboter. Aller plus loin empiéterait sur les consignes, qui sont déjà un champ à part avec leurs lecteurs et leur durée. Le prix de l'appel unique est ici, et il se dit une fois : la mise en scène est dans le contexte du juge, où elle n'a rien à faire, et ce qui l'en tient à distance est une phrase disant que son critère est la consigne. Ça se vérifie au banc, ça ne se prouve pas.

## La marque est invariante, ce qu'on en fait ne l'est pas

**La même faute produit la même marque, à n'importe quel moment, quels que soient les réglages.** Aucun levier ne touche jamais une marque. C'est l'exigence de `../reference.md` tenue jusqu'au bout : une marque dont la couleur ou la présence dépendrait du réglage du jour ne transporte plus rien, puisque son absence ne se distingue pas d'une approbation.

Il y a donc **trois étages, et un seul où les réglages entrent**.

- **La mesure** — l'écart au modèle pour le son, le cran de correction pour un groupe de mots. Aucun réglage ne la touche.
- **La marque** — elle affiche la mesure telle quelle. Invariante, c'est le repère stable de l'apprenant.
- **La note** — elle agrège les mesures du passage, puis de la séance. C'est **là et nulle part ailleurs** que les réglages agissent : la sévérité décide à quel niveau de fautes on passe de A à B, de B à C.

Ce que les réglages gouvernaient — faut-il redire, est-ce que ça compte dans la note — n'est donc plus deux choses. Compter dans la note **est** l'effet du réglage, et redire se lit sur la note qui en sort plutôt que sur la mesure brute. Une seule lecture réglée, tout le reste en découle.

**Une seule barre, la même partout : A ou B, ça va.** Sur un passage elle décide s'il faut redire ; sur une activité, si c'est réussi et si le niveau suivant s'ouvre. L'apprenant apprend la règle une fois et elle vaut partout.

**Et elle ne se règle pas.** Si la sévérité et la barre bougeaient toutes les deux, deux boutons feraient la même chose et plus rien ne dirait lequel a rendu une séance dure. Ce qui se règle est la sévérité ; ce qu'il faut atteindre ne bouge jamais.

Ça retire une chose qui était écrite ici : il n'y a **pas de barre de redire par échelle**, les sensibilités ne servant qu'à la note. Et **le nombre d'essais permis n'est pas un levier de l'élocution** : il dit combien de tentatives un passage accepte, jusqu'à une seule (« Le passage »). Depuis qu'il y en a deux, un par sorte de réparation, chacun se range avec l'aptitude qu'il répare — les reformulations avec la correction, les redites avec l'élocution (« Le catalogue des leviers »).

Deux choses restent acquises : la correction se souligne **toujours** dès qu'il y a un cran à montrer, et la rampe des sons est la même partout et tout le temps — la bande de bruit de ±5 n'est pas un réglage mais une propriété mesurée de la machine.

**Perte assumée** : il n'y a plus de conversation sans aucune marque grammaticale. C'est plutôt un retour au texte d'origine, qui dit que sans la trace la discrétion se retourne — on corrige et personne ne l'apprend.

## Les notes

**Une échelle unique, A–E.** Elle vaut pour tout ce qui se note, à n'importe quel niveau, sans avoir à retenir plusieurs échelles.

**La note ne vit pas sur l'aptitude, elle vit sur la mesure.** Un défi qui ne noterait que l'accent tonique rendrait sinon une note « élocution » qui ne veut pas dire la même chose que celle du défi d'à côté, sans que rien à l'écran ne le dise. C'est la troisième application de la règle qui ouvre ce doc : « élocution B » est un nom sur un jeu de mesures, comme « élocution 2 » était un nom sur un jeu de leviers. L'aptitude reste un tiroir — pour choisir, pour afficher — et n'est plus l'unité de la note.

**La lettre est toujours une note, jamais une mesure.** Elle est contextuelle par construction, puisque la sévérité déplace ses bornes ; une mesure, elle, ne bouge pas. Les deux ne doivent donc jamais porter les mêmes noms — c'est la confusion que ce doc portait, où les crans de correction étaient écrits en A–E comme la note. **On garde les nombres et les crans en base, jamais les lettres** : les bornes pourront bouger sans abîmer les vieilles séances.

### L'arbre des poids

**Ce qui se note est un arbre, à profondeur libre.** Aptitude, mesure, découpage plus fin ne sont pas trois natures : ce sont des nœuds, et la profondeur dit seulement à quel grain on peut peser. La question « est-ce une mesure ou une catégorie de mesure ? » ne se pose donc jamais, et démultiplier revient à creuser une branche, jamais à changer de mécanisme. **Rien n'oblige un nœud de premier niveau à être une aptitude** — l'arbre ne connaît que des poids.

Deux réglages par nœud, et ils ne font pas la même chose :

- **la sensibilité** — où tombent les bornes A–E de cette note ;
- **le poids** — combien cette note pèse dans celle du dessus. À 0, le nœud ne compte pas.

Un défi qui ne note que l'accent tonique est donc un poids à 1 et des poids à 0, pas un mécanisme à part. C'est ce qui permet de viser sans ajouter de champ : quoi qu'on note, l'information est déjà là.

### Ce qu'une feuille déclare

**Un défi s'écrit contre une liste, jamais contre le code.** Sans ça, poser une condition — la feuille *part silencieuse*, forme élément, cinq secondes — demande de savoir que cette feuille existe, comment elle s'appelle, et que ses éléments sont des secondes et non des pourcentages. C'est-à-dire de lire le calcul.

**L'arbre des feuilles est donc déclaré en un endroit**, et chaque feuille y dit cinq choses :

- son **nom** et sa place dans l'arbre ;
- l'**unité de son chiffre** — un pourcentage de sons, des demi-tons, des mots par seconde, un entier ;
- l'**unité de ses éléments**, presque jamais la même — des secondes, un cran, un vrai/faux, des points ;
- la **liste ordonnée de ses crans**, quand ses éléments sont des crans ;
- si elle **prend une consigne**, c'est-à-dire si elle est jugée ou calculée.

Un défi se réduit alors à quatre choses posées sur cette liste : des poids sur les nœuds, une sensibilité par feuille, des consignes sur les feuilles jugées, des conditions. Rien n'y est un branchement de code neuf, et une définition écrite par un modèle devient possible sans lui donner le code — on lui donne le catalogue, il rend ces quatre choses.

**Ce que le catalogue ne donne pas** : de quoi écrire un défi *sensé*. Savoir qu'une feuille existe ne dit pas ce qu'un chiffre y vaut, et où chaque sensibilité pose ses bornes reste à écrire.

### Des marques au chiffre d'une feuille

**Une feuille est la moyenne de ses éléments.** Une part et une moyenne sont la même chose — une proportion, c'est la moyenne d'un 0/1 —, donc il n'y a qu'une recette. Ce qui change d'une feuille à l'autre n'est pas la formule mais **ce que vaut un élément** : 0 ou 1 quand la question est fermée, une quantité brute quand elle est graduée. Et une feuille qui ne rend qu'un chiffre pour tout le passage — le débit, le délai avant de parler — est le même cas avec un seul élément.

**Jamais un nombre d'occurrences.** Deux gros ratés dans *Where is it?* et deux dans *I was thinking about going to the market* ne valent pas la même chose : sur quatre mots dont deux abîmés, personne ne rattrape le sens ; sur neuf, le contexte répare. Et la raison mécanique est déjà écrite plus haut — chaque feuille pèse par la longueur du passage, donc un nombre ferait compter la longueur deux fois, une fois parce qu'une phrase longue a mécaniquement plus de ratés, une fois par le poids. Une proportion multipliée par la longueur redonne le nombre.

**Le dénominateur est ce que la feuille lit**, pas tout le passage : sur un défi qui ne pèse que les *th*, un raté sur les quatre *th* de la phrase fait 25 %, pas 3 %. Le poids choisit les colonnes, le dénominateur suit le poids.

**Les comptes sont hors du poids par longueur.** Le nombre de tentatives, le nombre d'écoutes, les faux départs ne sont ni une part ni une moyenne : ce sont des entiers. Les peser par la longueur ferait que deux redites sur une phrase longue comptent plus que deux redites sur une courte, alors que redire deux fois est redire deux fois — et si la longueur jouait, elle jouerait à l'envers, la phrase courte étant la plus facile à reprendre. Un passage compte donc une fois pour ces feuilles-là, quelle que soit sa longueur.

**Le nombre ne disparaît pas pour autant, il change d'endroit.** « Zéro faute franche » compte des occurrences : c'est une condition branchée sur la feuille, pas une note. Les deux lisent la même chose et n'en font pas le même usage.

**Le chiffre d'une feuille est dans son unité à elle** — un pourcentage pour les gros ratés, des demi-tons pour la mélodie, des mots par seconde pour le débit, un entier pour les tentatives. Rien ne les rend comparables avant le passage en A–E, donc **c'est la sensibilité qui rend les feuilles comparables**. Elle n'est pas un curseur de 1 à 5 partagé : c'est un jeu de bornes propre à chaque feuille, dans son unité. « Ce défi est plus sévère » se traduit feuille par feuille.

**Et la sensibilité déplace les bornes A–E, jamais une ligne interne à la mesure.** Si les deux se réglaient, durcir la ligne ferait monter la proportion et baisser la borne A ferait tomber la note : deux boutons, un seul effet, et plus rien ne dirait lequel a rendu une séance dure. C'est l'argument déjà servi pour refuser que la barre « A ou B » se règle en même temps que la sévérité. Une ligne appartient à la mesure, comme la bande de bruit.

#### Les sons

**La branche se coupe en deux feuilles**, parce que deux défis veulent des choses opposées. « Fais-toi comprendre » ne compte que ce qui change le mot ; « gomme ton accent » compte tout, et un apprenant parfaitement compréhensible à l'accent épais réussit le premier et échoue le second. Un seul chiffre servant les deux serait réglé par un curseur qui déciderait laquelle des deux questions on pose, c'est-à-dire une feuille à deux sens, ce que la grille interdit.

- **Les gros ratés** — la part des sons où le mot a changé. **Binaire par son** : un son à 95 ne compte pas plus qu'un son à 40, la question étant fermée. Graduer reprendrait le travail de l'autre feuille, et les deux diraient la même chose en moins net.
- **La masse des écarts** — la moyenne des écarts sur tous les sons, gros ratés compris. Aucune ligne, donc aucun seuil à trouver.

**La ligne du gros raté est celle où la rampe sature, 30 points, et c'est la même pour la feuille et pour l'écran.** Ailleurs, deux lettres peintes du même rouge plein compteraient différemment dans la note, et l'apprenant n'aurait aucun moyen de voir la différence sur laquelle la note agit. Avec cette ligne, rouge plein veut dire gros raté, et ça se lit à l'œil sans explication.

Ce que le banc pin, et ce qu'il ne pin pas : sur le jeu étiqueté, les témoins sont à 0,3 point, les demi-fautes sous 25, les fautes franches au-dessus de 93 (`../analysis.md`). La ligne est donc quelque part entre 26 et 92, où n'importe quelle valeur sépare aussi bien les deux populations. **30 n'est pas mesuré** : il vient de l'écran, où il avait été posé pour que l'image cesse de changer là où la différence cesse de vouloir dire quelque chose. Ce qui resserrerait l'intervalle, ce sont des fautes vraiment intermédiaires, que le jeu d'essai n'a pas — il n'a que des franches et des demies.

Effet à connaître : `09-walkin`, le /ŋ/ de *walking* dit /n/, tombe sous la ligne. Il compte dans la masse des écarts et pas dans l'intelligibilité, ce qui est le bon comportement — on comprend *walkin'*.

**Un son ajouté ou manquant est un gros raté par nature, pas par franchissement de ligne.** Il n'a aucun point, n'ayant rien en face de lui à comparer (`embedded/Marks.kt`), et l'écran le peint déjà au bout saturé de la rampe : ce n'est pas un degré de faux, c'est une chose qui est là et ne devrait pas y être, ou l'inverse.

#### La mélodie

Deux feuilles, sur le même modèle que les sons.

- **La distance** — à chaque syllabe, l'écart de hauteur entre les deux courbes, chaque côté ramené d'abord à sa propre médiane, et la **moyenne simple** de ces écarts, en demi-tons.
- **La grosse divergence** — la part des transitions où l'apprenant ne fait pas le mouvement du modèle. D'une syllabe à la suivante : si le modèle bouge moins que la bande de bruit, la transition n'est pas lue du tout ; sinon on lit `r`, le mouvement de l'apprenant divisé par celui du modèle, avec son signe. `r = 1`, il fait le mouvement exact ; `r = 0`, il est plat ; `r` négatif, il fait l'inverse.

Trois méthodes écartées, chacune par un cas du banc plutôt que par goût. **La corrélation** ignore l'amplitude, donc qui fait la bonne forme deux fois trop petite obtient un score parfait — or le cas 22 du banc, la phrase dite plate à la française, est étiqueté comme une faute : la seule faute de mélodie que le projet ait étiquetée disqualifie toute méthode qui normalise l'amplitude. **La moyenne des carrés** laisse une divergence isolée écraser le reste, ce qui est le travail de la feuille voisine. **Le recalage temporel élastique** sert à comparer deux courbes décalées dans le temps ; ici elles sont déjà alignées syllabe par syllabe, même texte — et il pardonnerait à qui met sa montée sur la mauvaise syllabe, ce qui est justement une faute.

Pourquoi `r` plutôt qu'un seuil en demi-tons : trois demi-tons sont énormes là où le modèle ne bouge pas et négligeables là où il monte de quinze. `r` est sans unité et ses repères se lisent en français — zéro, il ne bouge pas ; négatif, il fait l'inverse. Les deux prises du banc tombent chacune d'un côté : le 22 donne `r` proche de zéro, le 21, montant là où le modèle descend, donne `r` négatif.

**Portée : toute la phrase, et ce n'est pas mesuré.** La brique 10 ne lit aujourd'hui que la région voisée finale et n'en rend qu'une pente (`../analysis.md`) ; le contour par syllabe existe dans le chemin d'affichage (`marking/TurnMarking.kt`) et n'a jamais été confronté à des étiquettes. On l'étend quand même : une échelle qui ne parle que là où elle est sûre laisse passer les fautes qu'elle est là pour attraper, et l'emphase — *I said it IS important* — ne vit pas à la fin de la phrase.

**Gardé de côté** : `r` proche de zéro et `r` négatif ne disent pas la même chose — l'aplatissement est le trait systématique du francophone, l'inversion change le sens et elle est rare. En faire deux feuilles reste possible, c'est ce que l'arbre permet ; ça n'a pas été tranché.

#### L'accent lexical et les tentatives

**L'accent** — élément le mot, binaire : l'appui est tombé sur la bonne syllabe ou ailleurs. Le dénominateur n'est pas « les mots » : un monosyllabe n'a pas de choix d'accent, et un mot outil n'a pas d'appui net même chez le modèle, ce qui demande le seuil « le modèle a-t-il un accent net » que `../analysis.md` réclame déjà. Ce sont donc **les mots de plus d'une syllabe que le modèle accentue nettement**. La brique n'étant pas en service, la feuille est **absente** et sort de la somme au lieu de valoir zéro.

**Les tentatives** — un entier par passage, hors du poids par longueur. Ce sont des **tentatives** et non des redites : la feuille vaut au moins 1 et existe sur tous les passages. Compter des redites la mettrait à zéro presque partout, et une feuille à zéro partout tire la moyenne vers le haut sans rien dire.

**Il y en a deux, et elles se séparent par ce qui doit changer dans la phrase** : les **reformulations**, où les mots changent, et les **redites**, où ils ne changent pas et où c'est la façon de dire qui est reprise. Un blocage sur le débit ou sur un silence fait redire la même phrase, donc compte en redite — la liste est fermée par construction, une phrase changeant ses mots ou ne les changeant pas.

Prix assumé : la feuille des redites mélange ce qu'a causé le son et ce qu'ont causé le débit ou un silence. Un défi qui veut peser « bonne prononciation du premier coup » compte donc aussi les redites de débit.

#### La correction

Le juge marque des groupes de mots, et rien n'énumère ceux qui vont bien : il n'y a donc pas de « tous les empans du passage » comme il y a « tous les sons du passage ». **Le dénominateur est les mots retenus du passage**, comptés par l'app — le remplissage et les morceaux abandonnés en sortent (« Le remplissage et les reprises »).

Faire découper toute la phrase par le juge est écarté : couper du correct n'est vérifiable par personne — *to the market* fait un morceau ou trois selon l'habitude de coupe, et le chiffre bougerait avec elle. Marquer *I go* comme groupe verbal mal formé, ça, se vérifie. Le juge garde donc le travail qu'il fait déjà pour l'écran, l'app compte, et elle compte dans l'unité qui sert déjà au poids par longueur.

En mots, deux fautes dans *Where is it?* et deux dans une phrase de vingt ne rendent pas le même chiffre — le même argument que pour les sons. Et deux empans qui se recouvrent ne comptent pas deux fois : **un mot prend le pire cran qu'il porte**, et il compte une fois.

**Prix assumé : les sous-feuilles par nature d'empan partagent ce dénominateur.** La sous-feuille « groupe verbal » lit la part des mots du passage tombant dans un groupe verbal marqué, et non un taux de réussite sur les groupes verbaux — l'app ne connaît pas ceux qui vont bien, et les connaître demanderait le découpage qu'on vient d'écarter.

**La correction pose une seule question — est-ce que c'est de l'anglais ? — et rend une seule feuille**, qui porte le nom de l'aptitude : la part des mots retenus dans un empan marqué. Binaire par mot, la question étant fermée.

**C'est un jugement absolu, et elle ne prend donc aucune consigne.** « Est-ce de l'anglais » est un fait sur la langue, vrai indépendamment de qui parle à qui : aucune situation ne le déplace. Une consigne, elle, est toujours une exigence de situation — elle appartient donc entièrement à la pertinence. *« Parle au passé »* ne fait pas de *I'll go there* une phrase mal formée, c'est de l'anglais irréprochable qui ne convient pas ici.

Ça règle une contorsion que le doc portait : il fallait poser la consigne sur la correction, marquer *I'll go there*, puis expliquer que la marque montrait la consigne « au lieu de mentir sur la langue ». Il n'y a plus rien à expliquer.

Et ça donne une propriété qui vaut cher : **la correction est toujours à son critère par défaut, donc toujours vérifiable au banc**, sur des phrases isolées. Le doc note ailleurs qu'une feuille sous consigne échappe au banc ; celle-ci n'y échappe jamais. C'est exactement ce dont le banc grammatical a besoin (`../../TODO.md`, chantier 2).

**Elle ne se découpe pas non plus par nature d'empan.** Le groupe verbal, le groupe nominal, la préposition, le circonstanciel, la proposition entière : cette liste fermée disparaît. Sans consigne, ces sous-feuilles seraient le seul moyen de viser quelque chose dans la correction — or viser est toujours un acte de situation. *« Travaille tes prépositions »* est donc une consigne côté pertinence, et une préposition fautive se retrouve marquée des deux côtés, ce qui est déjà permis. Une liste fermée de moins à tenir, et une étiquette de moins à demander au juge.

Ce qui laisse la correction avec **un seul chiffre, absolu, qu'on ne peut que rendre plus ou moins sévère.** Tout ce qu'un défi veut viser, exiger ou souligner vit dans l'autre aptitude.

**Deux étiquettes, qui pèsent pareil dans la feuille et ne déclenchent pas la même chose.**

- **mal formé** — la phrase n'est pas bien montée : *I go there yesterday*, *I make my homework*.
- **ne se dit pas** — ça n'existe pas dans la langue.

**Pas deux feuilles pour autant.** *Ne se dit pas* est rare, donc une feuille à elle vaudrait 0 % partout — et ici 0 % est le bon bout, donc tout le monde y serait en A tout le temps, ce qui ne fait que diluer les autres. Et elle n'a pas besoin d'un poids : elle a besoin de son effet, qui ne voyage pas dans une note. Un défi qui veut qu'elle coûte plus cher branche une **condition** sur l'élément.

**Ce qui a quitté la correction, c'est le choix.** *Maladroit* et *natif* n'étaient pas des degrés de correction, c'étaient des jugements sur ce qui **convient**, et ils sont maintenant la pertinence.

**Et la règle qui conditionnait le côté positif disparaît.** Elle existait quand justesse et naturel étaient deux feuilles d'une même aptitude tirées d'un même marquage : sans elle, une tournure brillante compensait un verbe cassé à l'intérieur d'un seul chiffre. Deux aptitudes séparées font passer cette compensation par les poids, que quelqu'un règle et qui se lisent — ce que le doc accepte partout ailleurs (« entre feuilles, la moyenne décide seule »).

#### La pertinence

**La pertinence pose l'autre question — as-tu visé juste ?** Juste par rapport à la situation, à qui écoute, à ce qui a été demandé, et à ce qu'on voulait dire soi-même. Même marquage par groupes de mots, même dénominateur, trois étiquettes, et **trois feuilles, une par étiquette**, chacune binaire par mot.

- **à côté** — ne vise pas ce qu'il fallait : le ton, la situation, la consigne. *Hey mate* à un client, *I entered the shop* en bavardant, un présent quand le passé était demandé.
- **plat** — vise bon mais mollement : vague, basique, ou repris alors qu'un autre mot était disponible. *I did a thing*, *nice* pour la troisième fois.
- **juste** — vise et touche : la tournure que le natif aurait choisie. *I popped into the shop*.

**Trois feuilles plutôt qu'une feuille signée**, parce qu'une feuille signée obligerait à inventer une pondération entre les étiquettes — un demi pour l'une, un pour l'autre — que personne ne saurait justifier, et le doc a déjà refusé ça. Avec trois feuilles, un défi qui veut que *à côté* compte trois fois plus que *plat* met 3 et 1, et celui qui ne veut rien récompenser met 0 sur *juste*. Rien de tout ça n'était écrivable autrement.

**Le poids dit combien la feuille compte, les bornes disent combien on en tolère**, et ce sont deux questions différentes. *Plat* est bien négatif, seulement moins que *à côté* — et ce « moins » s'écrit en bornes A–E plus larges, une fois, comme défaut du projet. Pas en valeur, et pas dans un poids qu'un défi devrait penser à monter.

**Un mot porte au plus une des trois étiquettes**, et une précédence fixe suffit à le garantir : *à côté* devant *plat* devant *juste*. C'est la règle déjà écrite pour la correction — un mot prend le pire cran qu'il porte et compte une fois. Le juge n'arbitre pas, l'app applique. Le cas qui décide est le groupe idiomatique mais hors ton : *crack on with it* dit à un client est de l'anglais précis, et faux ici ; l'exclusion le range en *à côté*, ce qui est le bon verdict.

**Prix assumé : un mot peut s'échapper de la feuille qu'un défi pèse.** Un défi qui ne vise que la platitude verra *stuff*, vague **et** trop familier, étiqueté *à côté*, donc absent de sa feuille. C'est le bon compromis quand même : l'étiquetage ne dépend jamais des réglages, ce qui est l'invariant de fond du projet.

**Les bornes de *juste* ne se posent pas comme les deux autres.** Elle vaut zéro sur presque tous les passages — on ne place pas une tournure remarquable à chaque phrase. Posées comme celles de *plat*, ses bornes mettraient tout le monde en E tout le temps et plomberaient la pertinence pour rien : zéro doit y être une note correcte, et la moindre trouvaille faire monter. C'est un cas concret pour « où chaque sensibilité pose ses bornes », qui reste à écrire.

**La consigne se pose sur le marquage, pas sur une feuille.** Les trois ne sont pas trois jugements mais trois lectures d'un seul passage du juge, qui marque une fois ; il n'y a donc rien à quoi une consigne *par feuille* s'attacherait. Elles s'écrivent comme quelqu'un les écrirait : *« tu parles à un client, tiens-toi au passé, préfère les verbes à particule »* — et rien n'empêche qu'il y en ait plusieurs sur ce marquage, chacune avec sa durée (« Ce qui est jugé, ce qui est calculé »). Le doc le disait déjà de la correction sans en tirer la conséquence — *« leurs consignes lui sont donc données ensemble »*.

Deux choses en tombent. **La consigne ne touche jamais la correction**, bien qu'elle partage le marquage, ses deux étiquettes étant absolues. Et **elle ne peut qu'endurcir** : la précédence va de *à côté* vers *juste*, donc une consigne fait passer des groupes vers l'étiquette plus sévère, jamais vers la plus douce. « Ignore les répétitions » reste inécrivable, ce qui est la règle.

**Le registre, la variété, la rareté et la complexité de la phrase n'ont pas de feuille à elles.** Le registre est ce que *à côté* dit ; la répétition, la vaguerie et la platitude de construction sont ce que *plat* dit. Une feuille par notion aurait compté deux fois les mêmes mots.

**Un même mot peut être marqué par la correction et par la pertinence**, et son poids s'ajoute dans les deux. *« I make a thing »* est mal formé **et** plat : ce sont deux défauts réels, pas un compté deux fois. C'est la seule exception au principe qu'aucun mot n'entre dans deux feuilles dont les poids s'additionnent, et elle est assumée.

#### La compréhension

C'est la seule aptitude qui ne porte pas sur ce que l'apprenant produit. Les quatre autres mesurent des sons, des mots, des silences ; comprendre est ce qui **entre**, et l'app n'a aucune fenêtre dessus — elle ne peut que déduire de la réponse. Deux feuilles, une jugée et une calculée.

**Le suivi** — un cran par passage, jugé, sur trois positions ordonnées et **sans bon côté** : *clairement en rapport*, *incertain*, *à côté*. Il n'y a qu'une chose à faire, prendre ce qui a été dit ; rien au-dessus, donc *clairement en rapport* est le cas normal et pas un exploit.

**Le cran *incertain* est ce qui fait tenir la feuille.** Sans lui, la réponse plausible qui n'engage rien — *« Did you manage to get the tickets? » — « Yeah, exactly »* — devait être comptée bonne ou fausse, et les deux étaient faux. Quelqu'un peut tenir une conversation entière sur *yeah*, *right*, *I think so*, et sortir avec un A. Ce cran est le seul moyen de le voir.

**Ce qui est jugé n'est pas la qualité de la réponse mais ce qu'elle montre d'avoir pris.** *« Yes, I go yesterday at the shop and I buy two »* est mal dit et parfaitement en rapport ; *« I like the cinema very much »* est correct et à côté. Confondre les deux ferait de cette feuille un doublon de la correction. Et ce qui se marque n'est jamais la brièveté : *« Did you get the tickets? » — « Yeah »* est spécifique à ce qui a été demandé, donc en rapport.

**L'objet est le dernier tour de l'IA ; le contexte est la conversation.** Deux choses différentes. Ce qu'il fallait prendre, c'est ce qui vient d'être dit — répondre à la question d'il y a deux tours est *à côté*, même si le thème général tient encore. Mais le juge a besoin du fil pour résoudre les pronoms et les ellipses : *« And did she like it? » — « She loved it »* ne se juge pas sans savoir qui est *she*. C'est la règle déjà écrite pour toute mesure — lire ce dans quoi l'apprenant se trouve est permis, ce n'est pas juger dessus.

**Le suivi pèse sur la difficulté du tour de l'IA, que le modèle rend avec sa réponse.** La longueur seule était grossière : *« Fancy a cuppa? »* est plus dur que quarante mots simples. Et la position du levier de complexité ne la remplace pas — elle demande un niveau, elle ne promet pas que chaque phrase soit dure, et la variation à l'intérieur d'une séance est réelle. C'est donc une réponse par phrase.

**La difficulté remplace la longueur, et la longueur fait partie de ce que le modèle doit peser en la rendant.** Garder les deux compterait deux fois la même chose, un tour long étant noté plus dur précisément parce qu'il est long. D'où une symétrie utile : les mêmes trois dimensions des deux côtés — longueur, vocabulaire, structure demandées par les leviers, le même trio rendu par le cran.

Quatre choses à tenir avec lui. C'est un **poids, jamais une feuille** : il dit ce qu'on a envoyé, pas ce que l'apprenant a fait, et en feuille il noterait quelqu'un sur la difficulté de ce qu'il a reçu. C'est un **cran**, le retour le moins cher qui soit, une clé énumérée dans un appel qu'on fait déjà. Il **se stocke avec la version de ce qui l'a produit**, étant un jugement que rien ne rejoue à l'identique. Et ses limites s'écrivent avec lui, sinon elles se redécouvrent : personne ne le vérifie, il ne se rejoue pas à l'identique, et c'est le modèle qui note ce qu'il vient d'écrire.

Une porte signalée sans être franchie : comparer le cran rendu à la position des leviers dirait si le modèle a obéi, ce que le drapeau *demandé* dit justement qu'on ne sait pas. **Non mesuré**, et rien ne s'en décide aujourd'hui.

Deux cas de bord. Un tour de l'IA qui contient plusieurs choses — une remarque et une question — demande au juge de peser s'il a pris **ce qui appelait une réponse** ; c'est un jugement, pas une règle. Et un passage sans tour de l'IA devant lui — l'apprenant qui parle en premier — n'a rien à avoir compris : **la feuille est absente**.

**Le marquage est une pastille en marge**, devant la phrase. C'est la seule mesure qui rende un verdict sur tout le passage, et la position le dit sans qu'on ait à l'apprendre : toutes les autres marquent des mots. Rien quand c'est clairement en rapport — le correct est l'absence de marque, comme partout —, **jaune** pour *incertain*, **rouge** pour *à côté*. Pas de vert : il est réservé à l'étiquette *juste*, seule chose du projet qui ait un bon côté, et le peindre ici en ferait une récompense pour un tour ordinaire. Un **cercle vide** dit *non mesuré*, là où l'absence de pastille dirait *il a compris*.

**Le nombre d'écoutes** — un entier par passage, hors du poids par longueur, exactement comme les tentatives. Comprendre à la première écoute et comprendre à la troisième ne sont pas la même chose, et rien d'autre ne porte cette distinction.

Le levier « réécoute autorisée, et combien de fois » ne remplace pas cette feuille, il la borne : c'est le même couple que les tentatives permises et le nombre de tentatives. **Mais elle est absente quand la réécoute n'est pas permise**, et quand le texte affiché la rend sans objet : tout le monde y est alors à 1, et une feuille constante partout tire la moyenne sans rien dire. Rien ne s'agrège entre ces réglages.

**Le suivi, lui, n'est jamais absent pour cause de texte affiché.** Voir le tour de l'IA est une aide en moins à trouver, pas une mesure qui disparaît : il reste à comprendre la langue, et quelqu'un qui lit la question et répond à côté ne l'a pas comprise. Une note ne se lit jamais sans la combinaison qui l'a produite, et ça suffit.

#### La fluidité — le débit et les silences

**Le débit se mesure sur le temps de parole**, silences exclus : les mots retenus divisés par le temps où la bouche articule. Ce n'est pas une version approchée du débit sur la durée du tour, c'est une autre mesure — l'une dit à quelle vitesse le tour avance, l'autre à quelle vitesse on enchaîne. C'est la seconde qui parle d'anglais oral, un francophone lent étant souvent quelqu'un qui détache ses mots au lieu de les lier.

Elle existe donc **aux trois positions de capture**, et sort de la liste des mesures réservées à la capture automatique, qui n'en garde qu'une : la part silencieuse. Ça compte, l'app étant en position 1 aujourd'hui : sans ça la fluidité n'aurait que ce que le juge lit dans le texte, et rien qui se mesure dans le temps.

**Le silence, de bout en bout, fait une seule feuille, la part silencieuse** — le temps de silence divisé par la durée du tour entier, de l'armement du micro à l'envoi. Elle couvre les trois moments où l'app garde du silence : avant le premier mot, entre les mots, après le dernier. Un tour de 21 s portant 6 s de silence vaut 29 %, que ce soit un blanc unique ou douze petits, au début, au milieu ou à la fin. L'app ne prétend pas savoir lequel est le pire : parler haché, chercher un mot une fois, tarder à démarrer ou tarder à rendre la parole sont des défauts différents, et aucun n'est clairement plus grave.

**Un délai de grâce d'une seconde s'applique aux deux bords, jamais à l'intérieur.** Sans lui, le temps normal de réagir et de cliquer — le même pour tout le monde, qu'on ait dit un mot ou vingt — pèserait proportionnellement bien plus sur un tour court que sur un tour long : deux secondes de réflexe sur *« Yes, I did »* font 50 %, les mêmes deux secondes devant une réponse de vingt secondes ne font presque rien, pour un comportement identique. Le silence initial et le silence final comptent donc chacun leur durée moins une seconde, jamais moins que zéro ; un silence intérieur, lui, compte en entier — c'est justement le temps qu'on cherche à voir. Un tour où personne ne traîne, ni pour démarrer ni pour rendre la parole, vaut alors 0 %, quelle que soit sa longueur. La seconde n'est pas mesurée : comme la ligne des 30 points sur les gros ratés, elle vient d'un jugement plutôt que d'un banc, posée à hauteur d'un temps de réaction ordinaire.

**La grâce ne touche pas ce qu'une condition lit.** Les éléments de la feuille restent les silences avec leur durée réelle, non réduite — un silence de 6 s reste un silence de 6 s pour qui cherche un gros blanc. Elle ne joue que dans le chiffre de la feuille elle-même.

Prendre le silence comme élément et sa durée comme valeur donnerait une moyenne de durées, et **un tour sans aucun silence n'aurait pas d'élément, donc pas de feuille** : le tour le plus fluide possible ne serait pas noté. Disqualifiant.

Couper en deux feuilles comme les sons demanderait deux choses qui manquent. Le critère de coupe est que deux défis veuillent des choses **opposées** — « fais-toi comprendre » et « gomme ton accent » se contredisent —, alors que « ne t'arrête pas longtemps » et « ne t'arrête pas du tout » vont dans le même sens. Et la ligne du gros blanc ne viendrait de nulle part, là où celle des gros ratés était déjà à l'écran.

**Le gros blanc reste lisible par une condition**, qui va voir les éléments de la feuille — les silences avec leurs durées. Sa ligne est alors un choix de défi, cinq secondes pour l'un et trois pour l'autre, et non une propriété de la langue gravée dans la mesure.

**Le dénominateur est la durée du tour entière, de l'armement du micro à l'envoi.** Une seule feuille couvre tout le silence du tour, donc aucun silence n'est jamais compté par deux feuilles dont les poids s'additionneraient.

**Le silence final compte, et c'est voulu : rendre la parole est un acte.** Dans une conversation réelle, on signale qu'on a fini — la voix qui retombe, le silence qu'on laisse à l'autre — et tarder à le faire est un vrai défaut, pas du bruit à retirer. Le clic en est le geste ; la grâce d'une seconde absorbe le temps normal de l'appuyer, et au-delà, le temps qui reste compte pareil qu'un silence intérieur.

**En position 3, une hésitation qui dépasse x se voit à trois endroits.** Le silence est compté dans la part silencieuse, plafonné à x comme tout silence de cette position ; le tour est marqué interrompu ; et la phrase tronquée, souvent incomplète, se fait marquer par le juge de correction.

**Le tour interrompu est une feuille**, et elle se lit sur un passage : ce tour a-t-il été envoyé, oui ou non. Un seul élément, vrai ou faux. La part des tours interrompus d'une séance n'est pas la feuille, c'est ce que l'agrégation en fait — une feuille se calcule toujours sur un passage, et confondre les deux plans se paie vite.

Deux causes d'interruption, une seule feuille : le silence de plus de x en position 3, et le plafond de durée du tour, qui existe aux trois positions. C'est pourquoi elle est mesurable partout, et vaut « non » presque toujours là où aucun temps n'est imposé — presque, et non par construction. **Elle est hors du poids par longueur**, comme les tentatives : être interrompu est un fait, il ne compte pas double parce que la phrase était longue.

Et ce qui compte comme silence ne demande aucun chiffre neuf : c'est ce que le découpage en segments appelle déjà silence, un tour étant une liste de segments dont les silences sont gardés comme durées (`../reference.md`).

#### Le remplissage et les reprises

**Les répétitions et les faux départs font une seule feuille.** Répéter, c'est repartir avec les mêmes mots : même geste, une phrase commencée puis reprise. Les séparer demanderait de dire à partir de quel mot changé on quitte l'une pour l'autre, et aucun défi ne veut peser cette différence-là.

**Les deux feuilles sont jugées, jamais calculées sur une liste de mots.** *euh* et *um* n'ont pas d'autre emploi, mais *I mean*, *like*, *well*, *actually* sont tous de vrais mots — « I mean what I say » et « it was, I mean, hard » ne diffèrent que par l'emploi. Ce n'est donc pas le mot qui décide, et seul le juge voit l'emploi. Elles prennent par là une consigne, ce qu'une feuille calculée ne peut pas prendre : « aucun mot du genre *I mean* » s'écrit telle quelle.

**Le juge souligne, l'app compte** — la même répartition que pour la correction, et dans la même passe : deux sortes de marque de plus sur une phrase qu'il lit déjà, pas un appel de plus. Un cran par phrase, « il bafouille beaucoup », est écarté pour la raison habituelle : personne ne peut le vérifier, là où « ces quatre mots-là » se regarde.

Sur *« It was, like, um, I went to the— I was going to the store »* : quatorze mots prononcés, deux de remplissage, quatre abandonnés, huit retenus.

- **Le remplissage** — la part des mots prononcés employés comme remplissage. Binaire par mot, ici 2/14.
- **Les reprises** — la part des mots prononcés appartenant à un morceau abandonné. Binaire par mot, ici 4/14.

Compter les mots abandonnés plutôt que le nombre de reprises suit la règle générale — jamais un nombre d'occurrences — et gradue ce que le nombre écrase : se reprendre après trois mots n'est pas se reprendre après huit.

**Deux dénominateurs, et c'est l'application directe de « le dénominateur est ce que la feuille lit ».** Ces deux feuilles lisent tout ce qui est sorti de la bouche, les quatorze ; la correction, le poids par longueur et le débit lisent les mots retenus, les huit. Les mélanger casse dans les deux sens. La grammaire sur les quatorze ferait qu'hésiter améliore sa note — *« Yesterday I go to the market »* donne deux fautes sur six mots, 33 %, et la même phrase avec deux *um* donne deux sur huit, 25 %. Le remplissage sur les huit pourrait dépasser 100 %, *« um um um um I went »* donnant quatre mots de remplissage pour deux retenus, et une part qui dépasse 100 % n'est plus une part.

Le débit se compte donc sur les mots retenus, rapportés à tout le temps de parole, hésitations comprises : chercher ses mots à voix haute ralentit le débit, et c'est exactement ce que la fluidité doit voir. Le silence, lui, ne bouge pas — un *um* est de la parole. Quelqu'un qui remplit ses blancs a une part silencieuse basse et un remplissage haut, deux feuilles qui disent chacune quelque chose de vrai.

**La voix modèle ne dit que les mots retenus.** Lui faire dire *« It was, like, um, I went to the... »* est exclu : le modèle est ce qu'on donne à imiter, et tout le montage repose sur le fait qu'il est la vérité.

**L'apprenant, lui, s'aligne sur tout ce qu'il a dit.** Sans les hésitations dans le texte, ces bouts d'audio n'ont aucune lettre en face et deviennent des sons en trop, donc des gros ratés par nature : hésiter coûterait une note de prononciation. **On aligne sur les quatorze, on compare sur les huit.**

**Découper l'audio de l'apprenant pour n'en garder que les huit mots est refusé** par un invariant déjà écrit : rien ne se fait à un seul des deux audios (`../reference.md`). Et ça empirerait le problème de couture ci-dessous, en collant bout à bout deux morceaux que personne n'a prononcés à la suite.

**La couture de l'analyse gagne donc un paramètre** — `examine(said, model, text, kept)`. `text` reste la chaîne affichée, celle où toutes les marques s'indexent, et elle porte maintenant les hésitations ; `kept` dit les morceaux qui comptent, ceux sur lesquels le modèle a été synthétisé. Sans hésitation, `kept` couvre tout le texte et le comportement est celui d'aujourd'hui. C'est le seul endroit où « deux enregistrements de la même phrase » (`analysis/Analysis.kt`) cesse d'être exact : les deux audios ne portent plus le même texte, l'un contenant l'autre.

**Prix assumé : aux coutures, un ou deux sons sont comparés hors de leur contexte.** Le *I* retenu qui suit un *the* abandonné n'a pas devant lui ce que le modèle a devant le sien, et un son est influencé par celui qui le précède. Ça ne se propage pas — une faute ne contamine pas la suite de la phrase (`../analysis.md`, brique 11) — et la redite l'annule, une phrase dite d'un trait n'ayant plus de couture. C'est une raison de plus de faire redire un tour hésitant.

**Le bafouillage ne ferme pas la porte de reformulation.** Elle existe parce que la phrase va être *réécrite* : les mots changent, donc l'analyse porterait sur du texte mort. Ici les mots retenus sont les bons, ils ont seulement été dits en trébuchant. L'analyse tourne, au prix des coutures.

**Ce qui est écarté reste affiché, grisé et entre crochets** — `I am [um] twenty five years old`. Les crochets disent ce que la pâleur seule ne dit pas : ce morceau n'est pas dans la phrase. C'est la vérité du calcul — ni analysé, ni lu par la correction, ni à redire — et c'est ce qui explique pourquoi ces lettres n'ont aucune couleur. Sans eux, l'apprenant voit un trou sans sa raison.

Rien à régler pour les redites : le fil ne montre que la dernière tentative (`../reference.md`), donc une redite propre n'affiche aucun crochet et une redite hésitante affiche les siens. Les crochets appartiennent à la tentative, pas au passage.

### L'agrégation se fait une fois, à plat

**Les poids se multiplient en descendant, et la note se calcule une seule fois sur les feuilles réellement présentes.** Les lettres d'aptitude et de passage sont la même formule restreinte à un sous-arbre : des lectures, pas des étapes de calcul.

La raison est qu'une cascade de moyennes redistribue en silence des poids que personne n'a réglés, dès qu'une feuille manque — et il en manque tout le temps : la fluidité n'a rien à lire en capture manuelle, un passage dont la porte de reformulation s'est fermée n'a aucune mesure de son.

Avec élocution 2 (sons 1, mélodie 1) et correction 1, sur deux passages dont le second a la porte fermée — passage 1 : sons 40, mélodie 80, correction 90 ; passage 2 : correction 50. En cascade, le passage 1 vaut 70, le passage 2 vaut 50 puisque sa moyenne se renormalise sur ce qui reste, et la séance 60. À plat, (40 + 80 + 90 + 50) / 4 = 65. L'écart n'est pas l'arrondi : dans la cascade, la correction a fini par peser deux tiers de la séance et l'élocution un tiers, l'inverse exact du 2:1 demandé, parce qu'au passage 2 elle était seule et a pris tout le passage pour elle.

**Une feuille absente sort de la somme, elle ne vaut jamais zéro.** C'était déjà la règle pour la fluidité ; elle vaut pour toutes.

**Chaque feuille pèse par la longueur du passage** — sauf le suivi, qui pèse sur la difficulté du tour de l'IA, la matière qu'il couvre (« La compréhension »). Proportionnel à la longueur est un peu arbitraire, un contour mélodique étant un contour qu'il soit long ou court ; c'est uniforme, et le tenir sur une longue phrase est effectivement plus de travail. La longueur se compte en **mots retenus** plutôt qu'en sons : un passage dont la porte a coupé l'analyse n'a pas de sons et a toujours des mots.

**Entre feuilles, la moyenne décide seule** : pas de plancher qui plafonnerait la note dès qu'une feuille comptée passe sous la barre. Une bonne feuille peut donc en masquer une mauvaise, et c'est accepté — dans un défi, peu de feuilles comptent, et la sensibilité de chacune dit à quel point elle est facile à tenir.

### Le passage

**Le passage est l'unité de la note : un énoncé et toutes ses redites.** Le mot est neuf parce que « tour » désigne déjà un tour de parole — un enregistrement, un énoncé, une position de capture — et confondre les deux se paierait au premier commit.

**La note du passage est celle de la dernière tentative**, et les tentatives permises sont un levier qui peut valoir 1. **Deux leviers en fait, un par compteur** — tant de reformulations, tant de redites — et ils ne se volent rien : un défi qui ne vise que la prononciation garde ses redites intactes quoi qu'il arrive du côté des mots. La dernière est ce qu'on sait dire maintenant. Conséquence à dire à l'apprenant plutôt qu'à lui laisser découvrir : une tentative de trop, après une réussite, peut faire baisser la note.

Les trois autres lectures sont pires. La **première** rend la redite sans effet, donc sans intérêt. La **meilleure** laisse l'obstination atteindre A. La **moyenne des tentatives** fait baisser la note à chaque essai, c'est-à-dire punit exactement le geste que l'app existe pour provoquer.

**Proposé, non tranché : la dernière tentative ne porterait que les feuilles que la redite sert à corriger** — l'élocution et la correction —, la fluidité et la pertinence lisant la **première**, parce qu'une phrase répétée n'est plus de la parole spontanée et que c'est la parole spontanée qu'elles mesurent. C'est une lecture précise de la règle déjà écrite plus bas, qui dit qu'une redite ne compte que pour l'élocution ; elle n'a pas été éprouvée.

**Une redite remplace à l'affichage, jamais en base.** Le fil ne montre que la dernière tentative. Mais les tentatives restent toutes sous le passage : une tentative effacée est une mesure perdue, et le compte des essais est une feuille. Ça tranche ce que `../reference.md` laissait ouvert sur le sort de la phrase initiale.

**Deux boutons, et c'est eux qui ferment le passage.** Le gros bouton dit une chose neuve et fait avancer la conversation ; un petit bouton posé sur la phrase la reprend, exactement comme pour la prononciation. **Le passage se ferme à l'appui sur le gros bouton** — l'app n'a rien à deviner de ce qui vient d'être dit, c'est un fait d'interface. Le budget épuisé fait disparaître le petit bouton, et il n'y a pas de reprise qui ne compterait pas : les tentatives plafonnent les reprises, un point c'est tout, et qui veut la liberté ne contraint pas les tentatives.

**En « attend », le gros bouton n'est pas disponible.** Sinon on sortirait d'un passage bloqué en disant simplement autre chose, et les tentatives cesseraient d'être la seule sortie.

**Et redire ne veut pas dire la même chose selon l'avance.** En « attend », l'IA n'a pas encore parlé : sa réponse se fabrique sur la version corrigée, et redire **corrige l'échange**. En « poursuit », elle a parlé, et refaire sa réponse serait la dédire, ce que le projet refuse partout : redire est alors un **exercice**, qui n'appelle aucune réponse neuve. Ce qu'il rapporte reste réel — la note du passage s'améliore, et la porte du son s'ouvre, donc l'analyse de prononciation devient possible là où un tour mal formé ne l'aurait jamais eue.

Reste le cas où la phrase corrigée dit autre chose que ce que l'IA avait compris, sa réponse devenant absurde. La redite ne fabrique pas ce risque, elle le révèle : l'app répond à l'intention, et se tromper d'intention est le cas déjà écrit de `../reference.md`. Le remède est celui de n'importe quelle conversation — le dire au tour suivant.

**Le nombre de tentatives est lui-même une feuille** : réussir du premier coup et réussir au troisième ne sont pas la même chose. Poids à 0 le plus souvent, monté par un défi qui veut la réussite d'emblée. Reformuler et redire ne se comptent pas ensemble — deux feuilles, une par compteur, du même découpage que les leviers.

### Les étiquettes de l'empan

Le juge marque **par groupe de mots**, une seule fois pour les deux aptitudes, et chaque empan porte **une** étiquette. Deux vont à la correction, trois à la pertinence, et le neutre ne se marque pas :

- **ne se dit pas** — hors de la langue. *(correction)*
- **mal formé** — la phrase n'est pas bien montée. *(correction)*
- **à côté** — ne vise pas ce qu'il fallait. *(pertinence)*
- **plat** — vague, basique, ou repris. *(pertinence)*
- *(rien)*.
- **juste** — vise et touche. *(pertinence)*

Les deux de la correction sont **absolues** : aucune consigne ne les déplace. Les trois de la pertinence sont **situationnelles**, et la consigne façonne où passe leur frontière.

**C'est la seule mesure du projet qui ait un bon côté**, et la raison tient à la nature des deux : pour le son, le modèle est la vérité, donc on ne peut pas faire mieux que lui — être dessus est l'attendu, et sous la bande de bruit rien ne se distingue. Pour une phrase il n'y a pas de modèle unique, donc on peut dépasser le simplement correct. Le cran haut est ce qui donne un sens à A : sans lui, A et B disaient tous deux « rien à signaler » et la différence n'était observable nulle part.

**Le juge rend le cran, et rien de plus fin.** Sur trois mots, « haut de plat » n'est pas un jugement que quelqu'un pourrait vérifier. La finesse ne se perd pas, elle change d'endroit : elle vient du comptage — combien de groupes marqués, à quel cran, sur quelle longueur de phrase. Un passage se note donc finement en agrégeant beaucoup de jugements grossiers, exactement comme le fait déjà l'élocution. Coût assumé : un passage de trois mots portant un seul groupe marqué a une note très grossière. C'est une grossièreté vraie, pas une fausse précision.

**Le cran haut ne vaut que s'il est rare.** C'est l'argument déjà fait contre le marquage tout-ou-rien : quand tout est colorié, plus rien n'est signalé. Un juge qui en donne un passage sur deux le rend décoratif, et la consigne doit donc être exigeante. **Non mesuré** : personne ne sait à quelle fréquence ce juge-là en donnera.

**Le calcul est écrit** en « La correction » et « La pertinence » : une feuille pour la correction, trois pour la pertinence, une par étiquette.

Ça absorbe un chantier qui traînait à part (`../../TODO.md`, point 6) : le verdict grammatical était un booléen sur le tour entier, ce qui écrasait le fait qu'un passage puisse porter plusieurs fautes et empêchait de marquer la portion concernée. Un cran par groupe de mots règle les deux.

### Ce que ça change au marquage

**La forme dit de quelle échelle il s'agit, la couleur dit de quel côté et à quelle distance du neutre.** C'est l'amendement d'une phrase de `../reference.md`, qui disait que la couleur ne porte que l'alarme : le cran haut porte l'inverse d'une alarme, donc la couleur porte aussi le côté. Le principe change de portée sans se perdre, la forme restant seule à dire l'échelle.

Les deux aptitudes issues du même marquage prennent **deux formes distinctes**, parce qu'elles répondent à deux questions.

**La correction se souligne en vaguelette — jaune pour *mal formé*, rouge pour *ne se dit pas*.** La couleur y porte la distance au neutre, comme partout, et elle dit quelque chose de réel : un tour *mal formé* peut passer la porte au réglage lâche, un tour *ne se dit pas* n'a jamais d'analyse du son quoi qu'on règle. Un soulignement était nécessaire — les sons prennent déjà **un filet sous le mot** pour l'accent lexical, et un trait droit tomberait au même endroit ; la vaguelette se lit comme une erreur de langue sans qu'on ait à l'apprendre.

**La pertinence s'encadre** — rouge pour *à côté*, jaune pour *plat*, vert pour *juste*. Pas de collision avec le jaune de la vaguelette : la forme sépare les deux échelles, un cadre n'étant pas un soulignement. Un contour est une forme neuve qui n'occupe ni l'intérieur des lettres ni la ligne de base, donc un mot peut être encadré et porter ses lettres teintées sans qu'on confonde les deux échelles. Le vert vit là et nulle part ailleurs, puisque c'est la seule mesure du projet qui ait un bon côté.

### La porte de reformulation

**Elle se ferme sur ce qui va être réécrit**, plus sur le marquage. La raison de la porte a toujours été qu'on ne travaille pas la prononciation d'une phrase qu'on s'apprête à réécrire ; tant que marquage et réécriture allaient ensemble, « marqué » était un bon substitut. Ce n'est plus le cas depuis que la correction a des leviers séparés.

**Elle n'est pas câblée sur la correction, et elle ne s'appelle plus grammaticale.** C'est une condition ordinaire, de la troisième forme — elle lit la note d'une feuille à la barre A–B —, donc **quelle feuille la ferme est écrit par les règles du défi**. Le registre en fait partie de plein droit : un tour à refaire parce que le ton est à côté est un tour dont les mots vont changer. Par défaut, hors défi, c'est la correction.

**Ce qui la ferme est toujours de la même famille : les marques qui appellent une reformulation**, où les mots changent. Celles qui appellent une redite — les sons, l'accent, la mélodie — ne peuvent pas la fermer, par construction : la phrase reste la même, il n'y a rien à réécrire.

**Elle lit la même barre que tout le reste** : un passage dont la note ne passe pas est un passage à refaire, donc son son ne s'analyse pas. En défi, ne pas passer force le nouvel essai ; en conversation libre ça ne force rien, mais l'app tient quand même la phrase pour une phrase à refaire — elle marque, propose de reformuler, et n'analyse pas le son. Une seule barre, deux conséquences selon le contexte.

**Rien de ce qui est jugé ne s'éteint quand elle se ferme.** Toutes les feuilles jugées se calculent, puisque ce sont elles qui décident si elle se ferme — l'éteindre par sa propre décision serait circulaire. Ce qui s'éteint est l'analyse du son, et elle seule.

**Et *ne se dit pas* n'est pas une porte, c'est une absence de sol.** Les deux se ressemblent à l'écran et n'ont rien à voir :

|  | la porte de reformulation | *ne se dit pas* |
|---|---|---|
| nature | une décision — on ne travaille pas une phrase qu'on va réécrire | une impossibilité — il n'y a pas de modèle à comparer |
| ce qui est lu | la **note** d'une feuille, à la barre A–B | un **élément** : un seul empan suffit |
| qui décide | les règles du défi, et laquelle des feuilles la ferme | personne, c'est un fait |
| négociable | oui, entièrement | non, il n'y a rien à négocier |
| ce qui se passe | l'analyse du son ne tourne pas | l'analyse du son n'a pas d'objet |

Dans le premier cas la phrase **pourrait** être synthétisée et on choisit de ne pas le faire ; dans le second elle **ne peut pas** l'être, et la faire dire au modèle donnerait à imiter une non-phrase. Tout le montage repose sur le fait que le modèle est la vérité.

**On ne peut pas retirer l'empan et synthétiser le reste**, comme on retire un *um* du texte de référence. Un bafouillage est **hors** de la phrase ; un empan qui ne se dit pas est **dedans**, et demande à être remplacé. Le retirer laisserait *« I have [ ] years »*, que le modèle dirait *« I have years »* — une autre phrase, cassée elle aussi.

**Et rien d'autre ne s'arrête.** L'IA répond, le fil continue, la marque s'affiche et la reformulation est proposée. Le canal du son reste vide en portant sa raison, pour cette tentative-là seulement : la reformulation est un énoncé neuf, dont le modèle se synthétise et dont les sons s'analysent normalement.

**Point ouvert : ce qui tombe d'un côté ou de l'autre dépend d'une décision du modèle de langue.** *I walk to school yesterday.* Le STT transcrit la bouche, donc *walk* ; le modèle décide l'intention, et avec *yesterday* il peut écrire *walked*. S'il écrit *walked*, la grammaire est correcte, la porte est ouverte, et l'analyse voit un /t/ manquant : faute de prononciation. S'il écrit *walk*, la grammaire est fautive, la porte se ferme, et rien du son ne s'analyse : faute de correction. Le même énoncé, deux traitements opposés, et ce qui tranche n'est contrôlé par personne.

**La marque porte sur un élément, la note agrège sur le passage.** C'est vrai des trois échelles du son comme des crans de correction : l'accent d'un mot est au bon endroit ou pas, mais un passage en contient plusieurs et la note les compte.

## La grille des mesures

Trois conditions pour qu'une feuille existe, et elles tiennent ensemble :

- elle **se calcule toujours**, quelle que soit l'activité — sinon la configuration d'un défi déciderait quels champs existent, et deux séances ne porteraient pas la même matière ;
- elle **veut dire quelque chose sans aucun défi**, dans une conversation ordinaire — sinon c'est un cas particulier déguisé en mesure ;
- quelqu'un pourrait vouloir la **noter seule**.

**Aucune feuille n'a deux sens.** Une feuille dont la direction devrait s'inverser selon l'activité est la mauvaise feuille : le défi « explique ça à un enfant de huit ans » ne demande pas la pertinence du lexique à l'envers, il demande de s'adapter à qui écoute — c'est ce que l'étiquette *à côté* dit déjà, un mot trop savant ne convenant pas à qui écoute.

Aucune branche n'est close.

**Élocution** — les gros ratés et la masse des écarts, deux feuilles sur les mêmes sons ; la distance des courbes et la part des mouvements non faits, deux feuilles sur la mélodie ; l'accent lexical, par mot ; le nombre de redites. Ce que vaut un élément et ce qui fait le dénominateur, feuille par feuille, est en « Des marques au chiffre d'une feuille ». *Candidate* : le rythme, les durées relatives comparées au modèle. La branche creuse gratuitement : la grille des sons est un inventaire fermé déjà attaché à chaque marque, donc un défi « travaille tes *th* » est un poids posé sur deux colonnes, sans juge et sans liste à inventer.

**Correction** — une feuille, sans découpage et sans consigne : un jugement absolu sur la langue. Plus le nombre de reformulations.

Aucun découpage par cause — temps, accord, préposition — parce qu'une liste de causes n'est jamais complète : elle finit avec un tiroir « autre » qui ne nomme rien et qu'on ne saurait pas peser. Et aucun découpage par nature d'empan non plus : viser une partie de la phrase est un acte de situation, donc ça se dit en consigne côté pertinence. Coût assumé, c'est grossier — « I go there yesterday » et « I goed there » tombent au même endroit.

**Fluidité** — la part silencieuse du tour ; le débit ; le tour interrompu ; le remplissage ; les reprises. La première n'existe qu'en capture automatique ; le débit se mesure sur le temps de parole, et le tour interrompu se lit aux trois positions, le plafond de durée existant partout.

Les deux dernières **ne survivent qu'à une reconnaissance verbatim** : un moteur qui nettoie les *euh* et les bégaiements les rend muettes sans jamais le dire, et la fluidité paraîtra excellente. Et depuis qu'on aligne l'apprenant sur tout ce qu'il a dit, un nettoyage silencieux fait aussi rater le placement des sons dans un audio qui, lui, contient les hésitations. Le critère de fidélité verbatim du banc (`../../TODO.md`, chantier 2) ne pesait jusqu'ici que sur la grammaire ; il pèse maintenant sur la mesure des sons.

**Pertinence** — trois feuilles sur le même marquage que la correction, une par étiquette : **à côté**, **plat**, **juste**. C'est elle qui porte la consigne, donc tout ce qu'un défi veut exiger. La variété, la rareté, le registre et la complexité de la phrase n'ont pas de feuille à elles : elles sont ce que ces trois étiquettes disent (« La pertinence »).

**Compréhension** — le **suivi**, un cran par passage ; le **nombre d'écoutes** du tour de l'IA, absent quand la réécoute n'est pas permise.

Deux collisions écartées, à ne pas rouvrir. **Le délai avant de parler ne se lit qu'une fois**, par la fluidité : en faire aussi une mesure de compréhension compterait deux fois le même silence, avec deux poids qui s'additionnent sans que personne l'ait voulu. Et **l'allongement de syllabe** — « I waaaant », une hésitation portée par la durée — tomberait sur le même signal que le rythme de l'élocution ; il reste dehors tant que rien ne dit lequel des deux le lit.

## Ce qui est jugé, ce qui est calculé

**Deux jugements viennent du modèle de langue** — le marquage des empans, dont l'app tire six feuilles, et le suivi de ce qui a été dit. Tout le reste se calcule : un écart de répartition, un silence, un débit, un rapport.

**Le juge rend un cran, jamais un pourcentage.** « 72 % de suivi » n'est vérifiable par personne, et la finesse vient du comptage, comme pour les sons. Les échelles qui suivent sont tranchées.

- **empan** — un groupe de mots et son étiquette, une seule : *ne se dit pas*, *mal formé*, *à côté*, *plat*, *juste*, du remplissage, ou un morceau abandonné. Un mot n'en porte jamais deux (« Les étiquettes de l'empan »).
- **suivi** — un cran par passage, et **pas de bon côté** : *clairement en rapport* / *incertain* / *à côté*. Il n'y a qu'une chose à faire, prendre ce qui a été dit ; rien au-dessus (« La compréhension »).

**Un troisième cran revient du modèle et n'est pas une mesure : la difficulté de son propre tour**, qui sert de poids au suivi (« La compréhension »). Il ne dit rien de l'apprenant, donc il n'a pas de feuille, pas de sensibilité et pas de consigne.

**Seul un marquage jugé prend des consignes** — du texte libre qui entre dans le critère que le juge lit, et non une feuille : plusieurs feuilles se lisent d'un même passage du juge, et il n'y a rien à quoi des consignes par feuille s'attacheraient. Il n'y a donc que deux endroits où elles s'accrochent, le marquage des empans et le suivi.

**Un marquage en porte plusieurs à la fois, chacune avec sa durée** — éternelle, ou un nombre de passages. Un patch en pose et en retire, donc une consigne arrive en cours de séance : *« au passage 4, tu lui mens sur tes intentions, pour trois passages »*. Une seule place remplacée à chaque fois ne marcherait pas dès que deux consignes n'ont pas la même durée — il faudrait réécrire le texte fusionné à chaque expiration. Le juge les reçoit toutes ensemble, et elles ne peuvent qu'endurcir, chacune comme l'ensemble.

**Une consigne a trois lecteurs, et c'est ce qui la distingue des autres effets** : l'apprenant, qui doit la lire pour la suivre ; le personnage, qui doit la connaître pour jouer avec ; le juge, qui s'en sert pour noter. Le patch parle à l'app et à l'apprenant, le message au modèle ne va qu'au modèle. **Une consigne est donc toujours affichée**, celle du départ comprise : être noté sur un critère qu'on ne t'a pas dit donne une note qu'on ne peut pas relire. Et le journal des changements dit laquelle était debout à quel passage, sans quoi la note de pertinence des passages 4 à 8 ne se relit plus.

Ce qui est posé ne vaut que pour la **suite** : un passage déjà dit n'est jamais rejugé. Un marquage calculé n'en prend pas : il n'y a pas de phrase à reformuler dans un écart de répartition. **Le poids choisit parmi ce qui est mesuré, la consigne reformule ce qui est jugé.** Un défi « prononce tous les -ed finaux » ne s'écrit donc pas en consigne sur les sons, que l'écart au modèle voit déjà, mais en creusant l'arbre pour ne peser que ces sons-là.

**La consigne fait partie de la situation, pas des réglages.** C'est ce qui la rend compatible avec l'invariance : deux personnes dans la même activité reçoivent le même traitement, et c'est ça que l'invariance protège. Elle n'a ni positions ni phrase lisible par position, donc elle ne va pas dans la liste des leviers — c'est un champ à part, un `brief` par marquage jugé.

**Une consigne n'est qu'additive.** Pour qu'une feuille compte moins ou pas du tout, il y a le poids, et lui seul. Une consigne « ignore les temps » est le cas interdit : elle ferait adoucir un verdict par un texte, ce que le module n'a pas le droit de faire.

**Une consigne est ce qu'un juge lit, et rien d'autre ne porte ce nom.** Deux choses lui ressemblent et n'en sont pas. L'**objectif** — *« obtiens le prix de la chambre »* — n'entre dans le critère d'aucun juge : il se dit dans le `brief`, se constate par le déclencheur du modèle, et se conclut par une fin ; il a deux des trois lecteurs et pas le troisième. Et les **deux phrases d'un patch** annoncent un changement au lieu de poser un critère — ce qui brouille les deux est qu'un patch peut porter une consigne, donc annoncer et installer du même geste. Sans cette ligne, la moitié du `brief` finirait en consigne, et le juge marquerait des choses que personne ne lui demandait.

**Une feuille sous consigne n'est pas vérifiée par le banc**, qui éprouve le critère par défaut. Coût connu, pas un défaut à réparer.

Trois défis que ça écrit sans champ neuf. « 100 % passé » est une consigne sur le marquage, et pèse la feuille *à côté* : *I'll go there* s'y range, ce qui ne ment pas sur la langue — la phrase est de l'anglais parfait qui ne convient pas ici, et la correction ne la voit pas. Le **mot interdit** est une consigne du même marquage : il n'a pas de feuille à lui, une feuille qui n'existe que si quelqu'un la configure ne passant pas les trois conditions. Et le **registre** — *« tu parles à un client »* — est de la même famille, comme la longueur imposée.

**C'est donc la consigne de pertinence qui porte tout ce qu'un défi exige de la situation**, et il faut le dire là où on serait allé chercher un levier : l'écran custom et le catalogue donné au modèle (« Ce qui fait un levier »).

### Les conditions attachées à une feuille

Ce qui rend une contrainte dure n'est pas la note — noyée dans une moyenne pondérée par la longueur, une occurrence coûte quelques centièmes de lettre — mais une **condition branchée sur la feuille**, qui se déclenche sur-le-champ.

**Une condition est une règle**, pas un mécanisme à part : son déclencheur lit l'arbre des notes, et son effet est un patch, c'est-à-dire n'importe quel levier déplacé. Il n'y a donc pas d'effets à énumérer — retirer une vie est une position de levier comme une autre, et une vie perdue et une fin sont la même chose vue deux fois, la fin étant zéro vie.

**Perdre une vie n'est jamais automatique.** Rater un passage ne coûte rien par soi-même : ça coûte une vie parce qu'un défi a écrit la règle qui le dit. Un autre en demandera trois, un autre rien. Et les vies n'existent que là où il y a un enjeu — une conversation libre n'en a pas, donc un passage raté y est un fait enregistré et rien d'autre.

Une condition **lit le résultat d'un nœud, elle ne change pas ce qu'il mesure**. Elle se branche donc aussi bien sur une feuille calculée : un silence de plus de cinq secondes coûte une vie.

**Trois formes, et il n'y en a pas d'autre.** Une condition lit :

- **un élément** — au moins un élément atteint ou dépasse une valeur : un silence de plus de cinq secondes, un empan au cran « ne se dit pas », un son au-delà de la ligne du gros raté ;
- **le chiffre de la feuille** — plus de 30 % du tour passé en silence ;
- **la note**, à la lettre que la condition nomme, **dans les deux sens** — tombé en D, ou atteint A.

Les deux premières portent leur seuil et ne bougent pas quand le défi durcit. La troisième **suit la sensibilité**, qui est précisément ce qui déplace les bornes A–E : monter la sévérité rend la condition plus fréquente sans qu'on la touche, et c'est un service — un défi dit « plus dur » d'un seul geste. La porte de reformulation est de cette troisième forme, et quelle feuille elle lit est écrit par le défi.

**La barre A–B ne borne pas les conditions, et l'y avoir enfermées était une erreur de portée.** L'invariant qui fixe la barre a été écrit pour la **lecture d'un résultat** — l'activité est-elle réussie, le niveau suivant s'ouvre-t-il —, où deux boutons qui bougent rendraient le résultat illisible. Une condition n'est pas un résultat : *« quand il tombe en D, le barman fronce les sourcils »* ne se compare à rien, ne débloque rien, n'entre dans aucun classement. Son seuil est de la même famille que les cinq secondes de silence, une valeur que l'auteur choisit. **Ce qui reste à la barre est le verdict** : ce qui décide qu'un passage se refait ou qu'une activité est réussie. Au-delà, la barre est une convention plutôt qu'une règle.

**Et lire les deux moitiés de l'échelle est ce qui rend une récompense écrivable.** « Sous la barre » n'existait que du côté de l'échec, donc aucun défi ne pouvait réagir à une réussite — alors que tout le reste était déjà là : un patch déplace un levier dans les deux sens, donc gagner une vie est un patch ; une fin porte l'issue *réussi* ; un message fait sourire le barman. Il ne manquait que de pouvoir lire la bonne moitié.

**Une condition lit n'importe quel nœud de l'arbre, pas seulement une feuille.** Une note existe à chaque nœud, et rien n'oblige un nœud de premier niveau à être une aptitude, donc lire *« la compréhension est en E »* est la même opération que lire une feuille. La seule différence est étroite et se déclare : un nœud n'a **ni éléments ni chiffre brut**, son nombre étant une moyenne pondérée de ses enfants, dans aucune unité. **Sur un nœud, la note est donc la seule des trois lectures disponible** ; les trois ne le sont que sur une feuille.

**Une condition ne lit jamais autre chose que l'arbre.** Un fait porté par le tour — sa position de capture, comment il s'est fini — n'est pas lisible tel quel : il faut la feuille qui le lit, et c'est ce qui permet d'écrire un défi contre le catalogue plutôt que contre le code. La feuille du tour interrompu existe pour cette raison.

**Le poids gouverne la note, la condition lit sans passer par lui.** Une feuille à 0 n'est pas éteinte : elle se calcule, et une condition la lit. Une feuille peut donc n'exister que pour les conditions et n'entrer dans aucune note nulle part — le tour interrompu pèse 0 en conversation libre, où aucun temps n'est imposé, et le défi qui veut de la réactivité lui met un poids, lui branche une condition, ou les deux.

**Une condition lit un passage et se déclenche sur-le-champ ; l'accumulation vit dans l'effet, pas dans la lecture.** « Un tour interrompu coûte une vie » n'a besoin de compter jusqu'à trois nulle part : au troisième, le compteur de vies est à zéro. Ce qui a besoin de voir la séance entière est d'une autre nature — son issue (« La fin d'une séance et son issue »).

**Rien ne s'écrit par feuille pour autant.** Une feuille déclare **deux unités**, celle de son chiffre et celle de ses éléments, et elles diffèrent presque toujours : la part silencieuse rend un pourcentage du tour et ses éléments sont des secondes ; la correction rend un pourcentage de mots et ses éléments sont des étiquettes. Une condition se dit alors partout pareil — quel nœud, laquelle des formes disponibles sur lui, et une valeur dans l'unité concernée.

## Le blocage

**Bloquer, c'est une règle dont l'effet est que le passage ne se ferme pas.** Pas un mécanisme neuf, et donc branchable sur n'importe quelle feuille : un défi peut bloquer sur la prononciation comme sur la correction.

**« Ne pas passer » se dit de trois choses**, à ne pas confondre. Une **feuille** ne passe pas quand sa note est sous la barre A–B : c'est une lecture, il ne s'ensuit rien. Un **passage** ne passe pas quand une règle l'a déclaré à refaire — quelle feuille elle lit est écrit par l'activité, la correction hors défi. Une **activité** ne passe pas quand elle se termine sans être réussie : une fin sèche en raté, ou une fin ordinaire dont la note ne passe pas (« La fin d'une séance et son issue »).

Concrètement, trois choses : la réponse **n'ajoute rien** — elle ne répond pas au fond et ne pose pas de question neuve ; le passage **reste ouvert**, ce qu'on attend ensuite étant une reprise de la même chose ; et rien n'avance tant qu'il ne se ferme pas.

**Ce n'est pas l'interruption que le projet refuse.** On ne part pas travailler ailleurs, le sujet ne change pas, l'écran ne change pas : c'est la conversation qui s'arrête sur une phrase. Le geste fondateur reste tenu.

**Deux axes, pas un.** L'**écho** — absent, indication indirecte, reprise explicite — est le levier déjà écrit. L'**avance** est le second : la réponse poursuit, ou elle attend. C'est un levier comme un autre, disponible partout — une conversation libre peut attendre sur une phrase, un défi peut poursuivre. Le geste fondateur du projet est la combinaison (indirect, poursuit) : *« Ah, you're 25! And where... »*. **Toutes les combinaisons s'offrent**, certaines étant seulement plus austères. Ce qui est garanti est ailleurs : **rien n'attend jamais sans qu'une raison soit visible**, et ce qui la porte est la marque, toujours là et invariante, plus la notification quand c'est le son qui bloque. Jamais l'écho, dont la position reste donc libre.

Ça éclaire ce que la porte de reformulation est vraiment, et surtout ce qu'elle n'est pas. **Sa portée est l'analyse du son, rien d'autre** : « la porte se ferme » veut dire « on ne mesure pas la prononciation de cette phrase-là », jamais « la conversation s'arrête ». Trois choses se déclenchent autour d'un passage à refaire, et elles n'ont ni la même portée ni le même décideur :

| ce qui se passe | portée | qui décide |
|---|---|---|
| la porte se ferme | l'analyse du son de cette tentative | automatique, dès que le passage est déclaré à refaire |
| une reprise est proposée | l'écran | automatique |
| la conversation attend | le fil — le passage ne se ferme pas, l'IA n'avance pas | le levier d'avance, posé par une règle |

**Les deux dernières sont indépendantes**, et ça se voit dans les deux sens. Porte fermée sans attendre est la conversation libre ordinaire : la phrase est marquée, ses sons ne sont pas analysés, l'IA répond et le fil continue. Attendre sans fermer la porte est le blocage sur la prononciation : les mots ne changent pas, donc l'analyse a tourné — c'est même elle qui a rendu le verdict — et c'est la réponse qui est retenue.

**Et *ne se dit pas* se lit sur les deux plans sans les confondre.** L'absence d'analyse y est un fait, non négociable, puisqu'il n'y a pas de modèle à comparer. Refuser de continuer, en revanche, reste une décision : un défi l'écrit avec une condition sur l'élément, dont le patch met l'avance sur « attend ». En conversation libre, rien ne bloque — le geste fondateur du projet est que rien n'interrompt.

### Deux branches pour la correction, une notification pour le son

**Le verdict de correction naît dans le même appel que la réponse ; le verdict de son naît après**, puisque l'analyse a besoin d'`intended`, qui vient du modèle. Les deux blocages n'ont donc pas les mêmes moyens, et ce n'est pas une incohérence : c'est ce que chaque verdict rend possible au moment où il tombe.

**Correction** : l'appel rend **une réponse plus une ligne courte** — la continuation, et l'écho de reprise (« Ah, you mean you ARE 25 »). L'app calcule la note dès le retour et joue l'une ou l'autre. Trois conséquences : **plus rien n'est jamais remplacé**, une seule étant jouée ; c'est littéralement « l'IA joue, l'app décide », puisqu'elle fournit la matière des deux issues sans trancher ; et la ligne courte **n'est produite que si l'IA a marqué quelque chose**, ce qui la rend gratuite sur un passage propre. Un champ de retour de plus, une chaîne, sans énumération à valider — le moins cher des contrats.

**Prononciation** : quand l'écart au modèle arrive, l'appel est fini et la réponse existe. Rien ne peut fournir un écho en personnage sans un second appel, écarté pour la latence. Donc la réponse est **retenue**, et une notification dit quoi reprendre — en donnant à **écouter** le modèle, qui est synthétisé de toute façon, et non en expliquant, le remède d'une faute sonore n'ayant jamais été une consigne écrite.

### La sortie d'un passage bloqué

**Les tentatives s'épuisent, et c'est la seule sortie.** Pas de geste d'abandon à part : les tentatives permises sont déjà un levier, donc la sortie est déjà réglable, et un deuxième mécanisme ne ferait que doubler celui-là.

**En « poursuit », les règles se déclenchent à l'appui sur le gros bouton**, qui ferme le passage et commence le tour suivant du même geste. Elles tombent donc juste avant la réponse de l'IA à ce tour-là, ce que le doc exige d'une règle. Un tour de retard, jamais au mauvais moment.

**Deux portes, dans cet ordre.** Celle des mots d'abord — on n'analyse pas le son d'une phrase dont les mots vont changer — puis celle du son. Les deux moments où l'app peut agir sont exactement ceux-là : au retour de l'appel, elle connaît le verdict de correction et tout ce qui se calcule sur l'audio et le texte ; à la fin de l'analyse, elle connaît le reste. Si les reformulations s'épuisent, le passage est raté et les redites ne sont jamais entamées.

L'IA repart alors du sens qu'elle avait compris — elle l'a toujours compris — et le passage est enregistré comme raté. C'est ce qui alimente les règles : perdre une vie, durcir, ouvrir ou non le niveau suivant.

## La fin d'une séance et son issue

**Un effet de règle est un patch, la fin, ou un message au modèle.** Trois, et la liste est fermée. Finir ne peut pas être un levier : il faudrait un côté dur, et finir n'est ni plus dur ni plus facile que continuer — c'est une porte qu'on franchit une fois, pas une position. Et le faire passer par les vies obligerait un défi « dix passages et c'est fini » à s'inventer une vie unique, donc à afficher un cœur à quelqu'un qui n'en a pas, alors que les vies n'existent que là où il y a un enjeu.

**Les vies à zéro mettent fin, et c'est une propriété déclarée du levier, pas une règle.** Aucun des trois déclencheurs ne lit une position de levier — ils lisent une horloge, une feuille ou un compte de passages — donc « quand il ne reste plus de vie » n'est pas écrivable en règle, et n'a pas à l'être.

**L'effet « finir » porte l'issue qu'il ouvre** : *réussi*, *raté*, ou *la note décide*. Zéro vie finit en raté. « Tu as obtenu la clé » finit en réussi, l'objectif atteint suffisant dans un jeu. « Au passage 10 » laisse la note décider, puisqu'on est allé au bout et qu'il reste à savoir comment.

**L'issue se lit alors dans cet ordre.**

1. **Aucune fin n'est tombée** — l'apprenant a laissé en route. Pas d'issue du tout : c'est une activité à reprendre, l'état ordinaire de toutes sauf celle qu'on a. La note se calcule et ne conclut rien.
2. **Une fin sèche est tombée** — réussi ou raté, c'est dit. La note s'affiche et ne décide pas.
3. **Une fin ordinaire est tombée** — la note de la séance à la barre A–B décide, comme partout.

**Ça bouche un trou que la note seule ne voyait pas : la quantité.** Un A sur deux passages puis on ferme, c'est une note excellente et un défi qui n'a rien prouvé. « Assez de passages » n'est donc pas un critère de réussite mais une **condition de fin**, et n'atteindre aucune fin, c'est n'avoir aucune issue. Un défi dit combien il en veut en écrivant sa règle de fin, comme il dit tout le reste.

**Il n'y a donc pas de champ « critère de réussite ».** Il se dissout en deux choses déjà là : la barre A–B, qui ne se règle jamais, et ce que les règles de fin déclarent. Un défi plus exigeant monte ses sensibilités, il ne déplace pas la barre — c'est ce que le doc dit déjà du déverrouillage d'un niveau.

**Pas de champ ne veut pas dire pas de mécanisme.** Une réussite scriptée — *« la chambre a été réservée »* — s'écrit comme n'importe quelle fin : un déclencheur de la quatrième sorte, *le modèle juge que oui*, et l'effet *finir* portant l'issue *réussi*. Il fallait l'écrire, la phrase ci-dessus se lisant comme si seule la note pouvait conclure. Le prix est celui du déclencheur : il est **demandé**, donc personne ne vérifie que l'objectif a vraiment été atteint, et le banc ne l'éprouve pas.

**Le message au modèle est de la prose injectée dans le prompt** — *« le barman a compris que tu lui as menti »*. C'est là où le doc dit d'être généreux : ce que personne ne relit est gratuit. Il ne double pas la phrase de mise en scène du patch — celle-là s'affiche à **l'apprenant**, celle-ci part au **modèle**, et une règle peut vouloir l'une, l'autre, ou les deux.

**Il déclare s'il attend le tour suivant ou s'il provoque un tour tout de suite.** Le tour suivant n'arrive qu'après que l'apprenant a parlé, donc sans ce second cas rien ne peut faire parler l'IA d'elle-même — or l'ouverture *est* exactement ce cas, un tour d'IA sans tour d'apprenant devant lui, que la chaîne doit savoir faire de toute façon. Ça donne les événements de scène : l'alarme qui sonne, le passant qui bouscule, le personnage qui relance qui se tait. Une seule borne suffit à tenir l'invariant de capture — **un tour provoqué ne tombe qu'à la fermeture d'un passage ou à l'ouverture**, jamais pendant qu'on enregistre. Rien ne coupe la parole de quelqu'un qui parle encore.

**L'arcade ne réussit ni ne rate**, elle rend un score : sa fin est zéro vie, et ce qui compte est le nombre que le résultat porte. Comment ce score se calcule reste à écrire.

## La pression

**La pression est ce que l'apprenant vit, pas un réglage.** Rien ne porte ce nom dans le code. Elle est produite depuis plusieurs endroits qui ne se connaissent pas entre eux, et le mot sert à parler de leur somme — quand on conçoit une activité, ou quand on dit ce qu'on a ressenti.

Trois mots à ne pas confondre :

- la **pression**, l'effet chez l'apprenant ;
- le **levier**, un moyen concret de la moduler : le texte affiché ou caché, le silence toléré avant qu'on relance, la durée d'un tour ;
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

Ce que chaque aptitude fait mesurer est dans « La grille des mesures » ; ici ne sont que les **leviers** qui modulent la pression sur elle, et leur déclaration complète est au « Catalogue des leviers ». Aucune de ces listes n'est close. Un levier commun à toutes ne se répète pas : **chaque feuille a sa sensibilité et son poids**, par construction de l'arbre.

**Élocution**
- *Leviers* : les **écoutes du modèle** avant de redire, un nombre dont zéro veut dire *de mémoire* et le haut *illimité* ; la **cadence imposée**, un interrupteur et un pourcentage de la durée du modèle.

Voir ou non le texte de sa propre phrase n'est pas un levier : quand on redit, l'analyse est à l'écran et ses marques sont posées sur les lettres, donc cacher le texte cacherait les marques. L'aide qui se retire vraiment ici est d'entendre le modèle, qui est le remède de toute faute sonore.

La cadence porte sur **tout tour analysé**, pas seulement sur une redite : le modèle est synthétisé pour toute phrase qu'on analyse. Ce qui change est ce que l'apprenant en voit — sur un tour spontané le modèle n'existe qu'après coup, donc c'est un verdict *« trop lent, refais »* ; sur une redite il est déjà là, et un décompte est possible. Le nom évite une collision : **le débit** est une feuille de fluidité, ce qu'on fait spontanément, là où la cadence est une exigence de coller à la vitesse du modèle.

**Compréhension**
- *Leviers* : la **longueur** du tour de l'IA et sa **complexité** — vocabulaire, structure — deux leviers, tous deux *demandés* ; le tour de l'IA en trois marches, **texte affiché**, **seulement qui parle**, **rien** ; la **réécoute**, un nombre dont zéro veut dire interdite ; le **bruit** et le **filtre** du canal, jusqu'à simuler un mauvais réseau qui coupe des mots — deux leviers et non un (« Le catalogue des leviers »).

La marche du milieu prend son sens à plusieurs personnages : on sait que c'est Vera qui parle sans lire ce qu'elle dit.

Les deux s'appliquent **à la lecture**, jamais au rendu mis en cache : le même fichier sert d'étalon à la mesure, et le bruiter fausserait l'écart.

**Correction**
- *Leviers* : **l'écho** de la faute dans la réponse de l'IA, en trois niveaux — absent (erreur ignorée, réponse normale), indication indirecte (la reprise dans sa réponse, ce que le code incite aujourd'hui), ou reformulation explicite dite comme un coach reprend son élève ; **l'avance**, la réponse poursuivant ou attendant (« Le blocage ») ; l'explication de la faute en notification, à deux niveaux — la règle à utiliser seule, ou la règle plus la phrase correcte.

Le marquage a quitté cette liste : il est invariant, donc il n'est plus un levier. Les deux qui restent sont bien **deux** et non deux crans d'un seul, parce que **où** et **quoi** ne sont pas deux quantités de la même information : on peut donner l'un sans l'autre, dans les deux sens. C'est la distinction que l'analyse fait déjà pour le son, où le marquage dit où et nommer le son produit est un enrichissement séparé. Et tout en haut, ils se recouvrent : une reformulation explicite de l'IA donne déjà la phrase correcte à voix haute, que l'explication redonnerait par écrit.

**Fluidité**
- *Leviers* : la capture, en trois positions (« La capture ») ; la **préparation**, le temps entre la fin de la réponse de l'IA et l'armement du micro.

**Pertinence**
- *Leviers* : aucun, et ce n'est pas un trou. Le registre, la longueur imposée, le mot interdit n'ont pas de côté dur, donc ils ne sont pas des leviers (« Ce qui fait un levier ») : ils s'écrivent dans la **consigne**, qui est justement ce que cette aptitude porte. C'est là qu'elle se règle. Il lui reste ce que toute feuille a, sa sensibilité et son poids.

### Comment les curseurs se composent

Monter un curseur fait deux choses, qui n'ont pas le même plafond. **Retirer une aide** converge vers le réel : pas de texte, pas de réécoute, pas de temps de préparation, c'est la vie ordinaire. **Durcir un jugement** dépasse le réel : personne, dans une vraie conversation, ne marque les sons au centième ni ne refuse une tournure correcte mais maladroite. Les crans hauts ne se conçoivent donc pas de la même façon selon qu'ils enlèvent ou qu'ils exigent.

**Les effets se cumulent d'une aptitude à l'autre, et c'est le principe.** Chercher le mot précis, tenir une forme imposée, traiter un tour dégradé : tout cela produit des silences, donc fait baisser la note de fluidité sans que l'apprenant soit moins fluide. La fluidité seule est facile, en combinaison elle est dure. Une note ne se lit donc jamais sans la combinaison qui l'a produite — la façon d'en tenir compte reste à définir, et plusieurs mécanismes sont possibles.

**Un curseur qui impose un réglage à toute la séance est le fonctionnement, pas une collision.** La fluidité possède la capture — il lui faut l'armement automatique pour que les silences veuillent dire quelque chose —, donc la monter change l'ergonomie de la séance entière.

Le test qui sépare une vraie collision du fonctionnement : **une combinaison n'est un problème que si elle change ce qu'une mesure veut dire, pas si elle rend seulement la tâche plus dure.**

Ce test donne une contrainte dure. Le bruit et le canal dégradé de la compréhension portent sur ce qui est **écouté**, jamais sur la prise de l'apprenant : l'analyse compare deux enregistrements traités symétriquement, et bruiter un seul côté rendrait l'écart mesuré en partie fabriqué (`../reference.md`).

Et **une redite ne compte que pour ce qu'elle sert à corriger**, l'élocution et la correction : c'est une reprise d'une phrase identique, pas de la parole spontanée. Ce que ça implique sur la tentative que chaque feuille lit est proposé et non tranché (« Le passage »).

## L'enjeu

Quatre crans, en gradation, qui portent sur la **séance entière** et pas sur une aptitude : il y a une note ; la note reste ; la note est comparée aux autres ; la série des notes est traitée — courbe, niveau, diplôme.

**C'est un concept pour parler d'une activité, pas une pièce du modèle.** Rien ne porte ces crans, et ce qu'ils décrivent tombe de la combinaison choisie : un défi rend une note, une campagne la compare aux précédentes, l'arcade la range dans un classement.

## L'histoire

Un mode histoire tire sur tout ce que le modèle a de mou, donc il sert de banc d'essai. **Une histoire est un bloc à accès ordonné — une campagne — et chaque scène est une définition.**

Pas une activité unique dont les scènes seraient des étapes : le `brief` est posé au départ et aucun patch ne le touche, donc une activité ne peut changer ni de lieu, ni de personnage, ni d'objectif. La rendre modifiable défairait ce qui garantit qu'une séance a une seule situation, donc une seule note lisible. Et une scène **est** une définition terme à terme — un lieu, des gens, une consigne, des règles, une condition de fin : ce n'est pas qu'on fait entrer l'histoire dans le modèle, c'est que le modèle décrivait déjà une scène sans le savoir.

Le fil se coupe donc entre deux scènes, et c'est plutôt souhaitable : dans un jeu, une scène se termine.

### Ce qui se souvient

**Une définition déclare des questions ; le modèle y répond en fin de séance, et les scènes suivantes reçoivent les réponses.** Sans ça rien ne passe d'une scène à l'autre que réussi ou raté, et le barman de la scène 4 ne peut pas demander *« alors, tu l'as retrouvée, la meunière ? »*.

Zéro objet neuf : l'exécution porte son origine, donc on sait de quel bloc elle vient, et son résultat porte déjà du texte.

**Le texte libre du résultat devient ces réponses, et rien d'autre.** Un défi qui veut un commentaire de fin déclare la question — *« qu'est-ce qui a marché, qu'est-ce qui a coincé ? »* — et une activité qui n'en déclare aucune n'a pas de texte. Un mécanisme au lieu d'un champ fourre-tout.

**Une question déclare son format** : texte libre, oui/non, ou une liste fermée. Le texte libre nourrit le prompt des scènes suivantes ; **une réponse fermée se lit par du code**, donc elle branche — *« a-t-il la clé ? oui »* ouvre la scène 5, sinon la 5 bis. C'est le fait testable obtenu sans effet neuf ni objet neuf : ce qui le lit est l'ordre du bloc, qui dérive déjà de ce que les scènes ont rendu. Une même scène pose ses questions fermées pour brancher et ses questions ouvertes pour que le barman sache de quoi il parle.

**La question est déclarée par la scène qui la produit**, jamais par celle qui en a besoin : seul le modèle qui était là peut y répondre. Le prix est un prix d'auteur — en écrivant la scène 2, il faut prévoir ce qui comptera à la scène 5 — et c'est le travail normal quand on écrit une histoire.

Deux limites à connaître. **Le modèle écrit sa propre mémoire**, et rien ne la vérifie : même famille que `intended`. Les questions bornent la dérive sans la supprimer — il répond à ce qu'on lui demande, donc la clé ne disparaît pas parce qu'il l'a jugée secondaire. Et **ça grossit** : à la scène 12 on transporte les réponses de onze scènes. Seules les questions déclarées voyagent, ce qui borne, mais pas toujours assez.

## Le personnage

Rencontrer quelqu'un plutôt que choisir un thème. La version la plus bête est qu'**un personnage est un `brief`** — du texte dans le prompt, zéro pièce neuve. Elle manque peu : un personnage porte aussi une **voix** et des **positions de leviers** (débit, vocabulaire, complexité du tour, c'est-à-dire les leviers de compréhension). Or un `brief` plus une voix plus des positions, c'est **une définition**. Le personnage n'est donc pas un objet neuf : une rencontre est le lancement d'une définition, un recueil de personnages est un bloc à accès libre.

**La voix d'un personnage ne sert jamais d'étalon, et c'est ce qui rend l'idée gratuite.** Le personnage dit *ses* tours ; le modèle à imiter dit *la phrase de l'apprenant*. Ce sont deux énoncés différents, donc les deux voix se séparent sans rien casser : un personnage peut avoir n'importe quelle voix, y compris une qui échoue à l'étalonnage, puisqu'elle ne mesure rien. Une voix difficile à suivre devient alors un levier de compréhension, ce qui est l'intérêt même. En échange, **la voix de conversation cesse d'être un réglage global** et devient un champ de l'activité, que la distribution porte ; la voix de référence, elle, reste le réglage de l'apprenant. Un champ et non une position de levier : le test l'a refusée, faute de positions déclarées et de côté dur (« Le catalogue des leviers »).

**Ce qui est tranché est l'invariant, pas le mécanisme.** Un défi n'écrit jamais `eleven-gb-daniel` en dur : ce qu'un fournisseur expose ne peut pas devenir une condition, sinon le défi meurt le jour où on change de clé.

**Comment un personnage obtient sa voix se lit dans `character-voices.md`**, qui porte le mécanisme vérifié et les plafonds chiffrés ; ce qui reste ouvert y est listé. Une piste parmi celles qu'il pèse : il déclare ce qu'il lui faut — rapide, régionale, âgée, difficile à suivre — et l'app résout contre le catalogue du fournisseur choisi, qu'elle récupère déjà à chaque ouverture de l'écran ; rien ne correspond, l'option s'éteint en portant sa raison. D'autres formes tiennent l'invariant aussi bien : une voix nommée avec un repli, ou un choix fait une fois par l'apprenant pour chaque personnage. Quelle que soit celle qui gagne, **ce n'est pas un levier** — entre deux voix il n'y a ni dur ni facile (« Ce qui fait un levier »).

Si c'est la piste de la propriété qui est retenue, d'où vient cette propriété reste ouvert à son tour. Deux sources : une **table par fournisseur** écrite à la main, et un chiffre **mesuré** — le test de voix calcule la divergence d'une voix aux autres, 11,5 % pour `eleven-us-sarah` contre 18 % pour `eleven-gb-daniel` (`../../TODO.md`). Portée de ce chiffre : il dit à quel point le réseau acoustique lit cette voix comme atypique, **pas** à quel point un humain peine à la suivre. C'est un candidat de proxy, non mesuré contre la difficulté réelle.

**Plusieurs personnages.** L'activité pointe une **distribution**, pas un interlocuteur ; chaque énoncé porte **qui parle**, le champ locuteur cessant de valoir apprenant-ou-IA pour devenir une identité ; la synthèse choisit une voix **par énoncé**, ce que le cache encaisse déjà puisqu'il est indexé par texte et par voix. Côté modèle, l'IA rend la clé du personnage qui parle : le retour énuméré le moins cher qui soit.

**La persona ne doit jamais atteindre la reconstruction d'`intended`.** Le même appel fait deux travaux — répondre en personnage, et reconstruire ce que l'apprenant voulait dire. Si le personnage déteint sur le second, la phrase de l'apprenant ressort en dialecte et c'est l'étalon de toute la mesure qui bouge. C'est la même famille de fuite que celle déjà mesurée, où le modèle réparait la grammaire de l'apprenant (`../../TODO.md`).

**Un personnage jetable ne coûte rien, un personnage qu'on retrouve coûte le stockage.** Fabriqué par l'IA à la volée, il ne fait que remplir les champs d'une activité, ce qu'un prescripteur fait déjà. Revoir Vera trois séances plus tard exige de la garder, et c'est le revirement que le modèle annonce : le jour où des définitions s'écrivent dans l'app, la séparation définition/exécution revient en base. La porte reste ouverte, elle n'est pas franchie.

### Les briques

Construire une activité en choisissant des briques — un lieu, un personnage, une situation —, chacune avec son texte libre. Aucune pièce neuve là non plus : une brique apporte des fragments de `brief`, des positions de leviers et des règles, donc c'est un **préréglage nommé**, comme le curseur d'aptitude et comme le mode. Quatrième fois qu'un nom rond se pose sur les mêmes axes.

Ce qui compte est le **périmètre**, et il se définit par ce que chaque brique possède. Cette coupe-là est **une première proposition, pas une décision** :

- **Lieu** — où, quand, ce qu'on entend autour. Possède le bruit et la qualité du canal.
- **Personnage** — qui parle : identité, tempérament, façon de parler. Possède l'exigence de voix et les leviers de sa parole.
- **Situation** — ce qui se joue, ce qu'il faut obtenir. Possède les conditions de fin.
- **Règles** — ce que l'app fait respecter : feuilles notées, poids, sensibilités, règles.

**Deux briques qui voudraient le même levier signalent que la coupe est fausse**, pas qu'il faut une règle de priorité. Leurs textes libres, eux, n'entrent en conflit avec rien : ils ne se lisent pas, ils se comprennent.

### L'habillage rétro

Le rétro touche aussi le marquage, qui est aujourd'hui un instrument de précision — une rampe ambre-rouge à nombreuses nuances, trois canaux graphiques superposés, une vaguelette qui doit se distinguer d'un filet droit au même endroit. Ça se refait **une fois**, en gardant la lisibilité ; ce n'est pas une contrainte permanente sur la conception.

## La capture

La capture est **un levier de fluidité, à trois positions**, et l'échelle gradue exactement ce que la fluidité peut lire.

1. **Maintien du doigt, envoi manuel.** Ce que fait l'app aujourd'hui : on appuie pour parler, on relâche pour réfléchir, on réappuie pour continuer, on envoie quand c'est dit. Aucune mesure de silence n'est possible — entre deux segments, l'écart mesure le pouce.
2. **Armement automatique, envoi manuel.** Le micro s'ouvre dès que l'IA a fini et reste ouvert jusqu'à l'envoi. Le **délai avant de parler** et les **silences intérieurs** deviennent mesurables.
3. **Armement automatique, envoi au clic ou sur un silence de plus de x**, silence du début compris. Le clic reste le geste normal ; un tour que personne n'envoie est **interrompu**, et c'est ce que cette position demande, de la réactivité. Aucune mesure neuve ne s'y ajoute, et aucun silence n'y dépasse x, puisqu'à x le tour est déjà parti.

Un tour interrompu est **tronqué et envoyé tel quel** : ce qui restait à dire n'est jamais capté, le micro ne se rouvrant qu'après la réponse de l'IA. Il n'est pas coupé en deux tours.

**Chaque tour porte comment il s'est fini** — envoyé, ou interrompu, et par laquelle des deux horloges. Ce n'est pas une mesure mais un fait sur l'enregistrement, à côté de la position de capture. Deux lecteurs : le modèle de langue, à qui il interdit de compléter une phrase inachevée (`../reference.md`), et la feuille du tour interrompu.

**Les deux décomptes sont visibles, toujours** — celui du silence de x et celui de la durée maximale du tour. Deux temps qui s'épuisent, montrés de la même façon. Ce n'est pas un levier.

Le micro **ne s'arme jamais avant la fin de la réponse de l'IA**. Un symbole est visible dès que ça enregistre : il n'informe pas seulement, il fait partie de la pression — savoir que ça tourne change la façon dont on parle.

**Chaque tour porte sa position de capture.** C'est ce qui dit si ses silences sont significatifs, et rien ne s'agrège entre positions : agréger un tour capté au doigt avec un tour capté automatiquement produirait un chiffre qui ressemble à de la fluidité sans en être.

**La durée d'un tour est une seule variable, et le plafond technique en est la valeur maximale admissible.** Imposer de répondre en cinq secondes et supporter trente secondes au plus sont la même chose réglée différemment, avec le même comportement : un avertissement quand le temps s'épuise, puis l'envoi. Ce qui se passe à zéro — envoyer, ou compter la tentative comme ratée — est une règle, déclenchée par l'horloge du **temps maximal d'enregistrement** pendant qu'on parle (« Quand une règle se déclenche »).

Le plafond, lui, est technique : la mémoire d'une passe d'analyse croît comme le **carré** de la durée du tour, d'où 30 s aujourd'hui. Envoyer plutôt que jeter, parce que jeter perdrait de la parole. Le plafond remonte quand le fenêtrage de l'analyse arrive (`../../TODO.md`) ; le levier, lui, reste.

## L'audio

**Un tour est une liste de segments** : une durée de silence, ou de l'audio. Le silence n'est jamais stocké en échantillons — il ne coûte alors que sa durée, là où le garder ferait passer une conversation de vingt minutes à des dizaines de mégaoctets. On reconstruit l'audio d'origine quand on en a besoin.

Chaque segment de parole garde une **marge de vrai audio** de part et d'autre. Ce qui identifie une occlusive vit dans la transition, et couper au ras de la parole abîmerait la mesure.

**On n'envoie pas de silence au réseau.** Ce que chaque mesure lit exactement — l'audio brut, les segments, la reconstruction — est à préciser mesure par mesure.

**Ce qu'on déduit de l'audio se stocke.** La purge efface l'audio ; une mesure qui ne se recalculerait plus après doit donc exister ailleurs. C'est le critère du projet appliqué ici : on stocke ce qui dépend de quelque chose qui ne se retrouvera pas.

**L'audio n'est pas purgé par défaut, et la purge est à écrire.** À ne pas confondre avec le balayage des enregistrements que plus aucun énoncé ne nomme, qui est fait et n'est pas une purge (`../reference.md`).

## Ce qui reste à spécifier

- **Où chaque sensibilité pose ses bornes A–E**, dans l'unité propre à chaque feuille. Écrit nulle part, et c'est ce qui rend les feuilles comparables entre elles.
- **Les deux valeurs de la mélodie** : la bande de bruit sous laquelle un mouvement n'en est pas un, et la ligne sur `r`. Plus l'extension de la brique 10 de la région voisée finale à chaque syllabe, qu'aucune étiquette du banc ne couvre.
- **Comment un personnage obtient sa voix.** L'invariant seul est tranché : aucun nom de voix écrit en dur. Le mécanisme, les plafonds et ce qui reste à décider sont dans `character-voices.md` ; la question qui commande tout le reste y est BYOK ou clé hébergée.
- **Ce que le prompt fait tenir des frontières qu'il ne prouve pas.** La forme est écrite (« Ce que le modèle reçoit ») ; ce qui reste est de le vérifier au banc, sur les deux fuites nommées — la persona qui atteindrait `intended`, et un historique qui déplacerait le marquage.
- **Le rythme de montée de la rampe** d'arcade, maintenant qu'on sait qu'elle monte des crans entiers — et **ce que ces crans font varier**, les poids étant constants pendant une partie.
- **Le degré de détail de l'écran d'avant-partie** (« Ce qui fait un levier »).
- **Ce qu'on fait des réponses quand elles s'accumulent** — à la scène 12, onze scènes ont répondu. Les questions déclarées bornent, pas toujours assez (« L'histoire »).
- **La part du prompt qui fabrique les occasions.** Un curseur haut ne sert à rien si la conversation ne place jamais l'apprenant devant la difficulté qu'il a demandée. Reste à partager entre ce qui passe par la parole de l'IA et ce qui passerait par une consigne hors parole (« notification » — autre nom à trouver).
- **Les phrases lisibles de chaque position de levier**, qu'exigent la notification d'arcade et l'écran d'avant-partie. Le reste de la déclaration est écrit (« Le catalogue des leviers »), qui reste un brouillon et dont aucune liste n'est close.
- **Comment le bruit et le filtre du canal se standardisent**, pour qu'une même position vaille une difficulté comparable. Le rapport signal/bruit standardise le niveau et non la difficulté, une parole concurrente étant plus dure que du bruit rose au même rapport ; et le filtre demande une liste fermée d'effets nommés dont l'encodage de l'intensité n'est pas décidé (« Le catalogue des leviers »).
- **Quels préréglages chaque mode offre**, et ce que chacun pose. Le curseur d'aptitude n'en est qu'un.
- **Le score** : propre à l'arcade ou pas, et à quoi ressemble son écran.
- **Garder le nom du préréglage** d'une séance réglée à la main. Aucun lecteur n'en a besoin aujourd'hui — l'origine suffit là où ça compte — donc pas de champ pour l'instant.
- **Le déroulé de chaque module**, et son écran. Le cadre est commun — l'activité, ses champs, ses statuts, son résultat — le déroulé ne l'est pas.
- **Les déclencheurs de suggestion** pendant une conversation.
- **La purge**, et la durée de vie des audios.
- **Ce que chaque mesure lit** de l'audio, segment par segment.
- **La liste de ce qui doit être agrégeable.**
