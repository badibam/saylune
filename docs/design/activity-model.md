# Le modèle d'activité

Ce qui reste ici est la part du modèle d'activité qui n'a pas encore de code, plus ce qu'elle laisse ouvert.

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

Elle porte : une **identité** et une **version** ; son **contenu** — ce qui ouvre la séance, le `brief`, les personnages ; ses **consignes**, au plus une par marquage jugé ; ses **positions de leviers de départ**, sensibilités comprises ; son **arbre des poids**, constant pour toute la séance ; ses **règles** ; et ses **questions**, auxquelles le modèle répond en fin de séance (« L'histoire »).

**Deux champs courts s'y ajoutent, et ils viennent d'une contrainte d'écran** (2026-09-06) : un **titre court** de dix caractères au plus, que la ligne d'état affiche, et un **nom court** par personnage, que porte la ligne qui nomme chaque tour. Les deux se tronquent de toute façon à l'affichage ; les déclarer laisse l'auteur choisir ce qui survit à la troncature au lieu de la subir. Le détail est dans `pixel-ui.md` (« La charpente », « La ligne qui nomme le tour »).

**Ce qui reste du code, c'est le catalogue** — les leviers avec leurs positions, l'arbre des feuilles, les sortes de déclencheurs, les sortes d'effets. Rien de ce qu'une définition contient n'est de la logique : des positions déclarées, des poids sur un arbre déclaré, du texte libre, et des règles faites de sortes énumérées. Une histoire de vingt scènes écrite en Kotlin serait du contenu qu'on ne peut ni corriger, ni traduire, ni partager, et il faudrait recompiler l'app pour changer une réplique. Et le doc veut déjà qu'un modèle puisse écrire une définition contre le catalogue, ce qui n'a aucun sens si sa sortie doit être compilée.

**Cette donnée vit dans des fichiers livrés avec l'app**, autoportants, et l'app les lit directement. Ce qui est acquis de toute façon : les réglages d'une exécution sont copiés sur sa ligne, donc une définition qui change ne réécrit jamais le passé ; et l'**origine** doit nommer la définition **et sa version**, sans quoi une mise à jour qui corrige une scène rend incomparables deux parties qui se croient les mêmes.

Trois raisons, dont deux sont l'argument ci-dessus retourné d'un cran. **La version arrive gratuitement** : un fichier livré hérite de celle de la release, quand une ligne créée à l'exécution n'en a aucune de naturelle et demande qu'on la frappe et qu'on lui écrive une migration. **Corriger, traduire et partager** restent possibles, ce qu'une table perd à moitié — rien ne traduit une base, et partager y veut dire exporter. Et **ce que le modèle fabrique à la volée n'est pas une définition** : un personnage inventé pendant une séance remplit les champs d'une activité, donc une ligne et pas un gabarit. Il ne devient une définition que le jour où on veut le **rejouer**, et c'est ce jour-là, nommé, qui ouvrira la table — à côté des fichiers, le lecteur unifiant les deux à la lecture. Une interface, deux sources, zéro copie.

**Importer les fichiers en base est écarté** : la table serait la copie d'une source déjà sur le disque, lisible et versionnée, donc la deuxième source qui se décale que le projet refuse partout, plus une réconciliation à chaque mise à jour de l'app.

**Un fichier porte ses traductions, en table langue → texte.** Une définition mélange deux langues par construction : le `brief` et la mise en scène qui partent au modèle sont en anglais, langue de la conversation et du critère du juge ; ce que l'apprenant lit est dans sa langue d'interface. Un fichier par langue dupliquerait la prose anglaise et la ferait diverger d'une copie à l'autre ; des clés vers `res/` casseraient l'autoportance, donc l'argument du partage. C'est un **écart délibéré à la facette `android`**, déclaré au manifeste : sa norme d'i18n a été écrite pour le texte d'**interface**, et la prose d'une scène est du **contenu** — aucun jeu ne livre ses dialogues en ressources de plateforme. Le prix est réel : une plateforme de traduction ne lit pas ce format.

**Ce qui fait qu'une conversation est « libre » est l'absence d'enjeu, pas l'absence de mécanique** (tranché le 2026-09-06). Elle ne pose ni vies, ni règle de fin, ni issue ; elle n'affiche **aucune lettre** ; elle laisse **tous ses leviers ouverts** à l'apprenant ; et son `brief` vient de lui — saisi, préréglé ou suggéré — au lieu du fichier. Elle peut porter des règles comme n'importe quelle autre définition, et « pas de règles » n'a jamais fait partie de sa définition : ce serait confondre ce qui la caractérise avec ce que sa version livrée contient aujourd'hui.

**La conversation libre est elle-même une définition livrée avec l'app.** Une activité est toujours une conversation, et les définitions sont déjà des fichiers livrés : celle-là porte donc ses positions de leviers de départ, son arbre des poids et ses règles comme n'importe quelle autre. Ça retire un « par défaut » qu'il aurait fallu câbler ailleurs, et ça retire surtout le besoin de dire « hors défi » — **« défi » n'est pas un terme du modèle**, c'est un nom d'usage sur une combinaison, comme « arcade » et comme « mode ». Ce qui distingue deux activités est leurs réglages, leur arbre des poids et leurs règles, et rien d'autre.

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

Par rapport à ce qui est écrit (`activity/Activity.kt`), il en manque sept, et un huitième change de type.

- **La consigne** — du texte libre, posé au départ et injecté dans le prompt, jamais réécrit ensuite. À ne pas confondre avec la **matière**, qui dit de quoi ça parle et que l'IA peut écrire après coup : une consigne « pousse-le sur le passé, il l'évite » peut donner une conversation dont la matière finit par être « son déménagement ». Les confondre ferait qu'un titre écrit par l'IA écrase la consigne. Nom proposé : `brief` — `seed` évoque une graine de tirage aléatoire, ce que ce n'est pas.
- **Les règles** — une liste, qui absorbe la rampe, les conditions de fin et les conditions branchées sur l'arbre des notes (« Les règles »).
- **Le journal des changements appliqués**, sans quoi une séance dont un tirage ou l'IA a modifié les réglages ne se recalcule plus.
- **L'origine**, ci-dessus.
- **Les consignes par marquage jugé** — du texte libre, sur les trois marquages que le modèle rend, distinct du `brief` de l'activité (« Ce qui est jugé, ce qui est calculé »).
- **L'arbre des poids** — ce sur quoi la séance regarde, fixé à l'écriture. Les sensibilités, elles, ne sont pas un champ neuf : ce sont des positions de leviers, donc des réglages (« Ce qui fait un levier »).
- **La version du moteur de règles.** L'état effectif se recalcule depuis les réglages, les règles et le journal ; rejouer ce journal sous une sémantique de règles qui a changé — une release plus tard — rend un autre état, et une séance cesse de se comparer à elle-même. C'est la règle du projet appliquée à l'exécution : tout ce qui est stocké porte la version de ce qui l'a produit. Le doc voyait ce problème pour la graine de tirage et pas pour lui-même. **Et ce qu'on en fait est la même réponse que pour l'éviction du cache : un changement de moteur retire la reprise**, la séance restant lisible sans pouvoir se poursuivre. Rejouer un journal sous une sémantique qui a changé rendrait un autre état sans le dire, ce qui est pire que de s'arrêter.
- **Le résultat doit pouvoir porter un nombre.** Il porte aujourd'hui un verdict, un juge, une date et du texte libre ; un score d'arcade est un nombre, et le ranger dans du texte libre le rendrait inexploitable.

Et **`mode` n'est pas un champ** : « arcade », « campagne », « défi », « custom » sont des noms d'usage sur des combinaisons de ces axes-là.

**Ce qu'un mode est, en revanche, et qui manquait ici : une couche d'accès, écrite en dur, côté écran.** C'est la façon d'arriver à une définition — un menu, une carte, une liste de niveaux —, et le même moteur tourne derrière toutes.

**Ce que la couche coupe, ce sont les lettres, jamais les mesures** (tranché le 2026-09-06). C'est la distinction que tout ce doc tient par ailleurs : la mesure est invariante, la note est une lecture réglée. Un mode qui ne montre pas les notes montre donc quand même *2,3 demi-tons*, *47 sons sur 50*, *22 % plus lent*, *un mot mal formé* — des faits sur ce qui vient d'être dit, qui ne dépendent d'aucune sensibilité et dont on a besoin même sans enjeu. Ce qui disparaît est la lettre, partout : sur les feuilles, sur les aptitudes, sur la séance.

**Et c'est là, et pas dans un levier, que se décide si les notes se montrent.** Le score et les notes se calculent de toute façon ; ce qui change d'un mode à l'autre est qu'ils s'affichent. Ce n'en fait pas un levier — voir sa note n'est ni plus dur ni plus facile, donc le test le refuse (« Ce qui fait un levier ») — et pas non plus un champ d'activité : c'est de l'écran. **En conversation libre, le défaut est de n'afficher que les marques**, et l'apprenant allume les notes s'il les veut. La marque est le repère invariant ; la note est une lecture réglée dont on n'a rien à faire tant que rien n'est en jeu.

**Ce n'est pas purement cosmétique pour autant** : la couche décide **quelle définition part**, et pour une campagne elle dérive quel niveau est ouvert depuis les résultats. C'est ce que le bloc annonce déjà en disant que « poursuivre la campagne » et « choisir un défi » sont deux gestes distincts dans l'app. Peu de code, mais pas un drapeau.

**Et les libertés d'un mode ne sont pas un mécanisme à lui.** Qu'en conversation libre un appui sur le texte de l'IA bascule brouillé/net, et qu'un défi ne le permette pas, est l'application de ce qui est déjà écrit — **le module décide quels leviers il expose** (« Qui décide de la pression »). Cliquer, c'est l'apprenant qui déplace `tour-ia.affichage` ; un défi qui le pose ne l'expose pas. La liste des libertés d'un mode est donc la liste des leviers que sa définition laisse ouverts, plus les gestes qui ne sont pas des leviers — rien à inventer, et ça se règle définition par définition plutôt que d'être câblé au mode.

## L'état d'une séance

Ce qui existe à un instant donné dans une activité donnée. L'inventaire sert deux choses à la fois : savoir ce qu'une reprise doit pouvoir reconstituer, et savoir ce que le prompt a le droit de dire.

**Trois natures, et il n'y en a pas de quatrième.** C'est le critère du projet appliqué à l'état entier — on stocke ce qui dépend de quelque chose qui ne se retrouvera pas, on recalcule ce qui ne dépend que des lignes (`../reference.md`).

- **Déclaré** — l'entrée figée, jamais réécrite. De la définition : l'origine, ce qui ouvre la séance, le `brief`, la distribution, les consignes de départ, les positions de leviers de départ, l'arbre des poids, les règles, les questions. De l'apprenant et non de la définition : son **nom**, sans lequel aucune scène ne peut le désigner ; l'accent, donc la voix de référence ; et les fournisseurs choisis par maillon, qui décident quelles briques sont allumées.
- **Enregistré** — ce qui ne se recalcule pas : les énoncés et leur audio, les mesures de chaque tentative, le journal de ce qu'un tirage ou l'IA a choisi, et les faits portés par chaque tour — quelle position de capture était en vigueur, comment il s'est fini et par laquelle des deux horloges.
- **Dérivé** — recalculable à tout instant depuis les deux autres : les positions effectives des leviers, les consignes en vigueur et ce qui reste de leur durée, les feuilles, les notes, le compte de passages et de tentatives, le blocage, les deux portes, l'avancement, l'issue, et les deux horloges qui tournent, le temps écoulé n'étant que maintenant moins le départ.

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
| `elocution.fait-refaire` | marches | non, oui | non | l'app |

`cadence.valeur` est **sans objet** quand `cadence` est libre.

**Compréhension**

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `tour-ia.longueur` | marches | courte, moyenne, longue | moyenne | le modèle |
| `tour-ia.complexite` | marches | basse, moyenne, élevée | moyenne | le modèle |
| `tour-ia.affichage` | marches | le texte, le texte brouillé, seulement qui parle, rien | le texte brouillé | l'app |
| `reecoute` | nombre | 0 à sans maximum ; 0 vaut *interdite* | sans maximum | l'app |
| `bruit` | marches | aucun, présent, fort | aucun | l'app |
| `filtre` | marches | aucun, léger, marqué | aucun | l'app |
| `comprehension.fait-refaire` | marches | non, oui | non | l'app |

Les positions de `tour-ia.complexite` sont **volontairement génériques** : c'est un levier demandé, donc le modèle l'interprète selon la scène, et des noms prescriptifs lui retireraient cette souplesse sans rien garantir en échange.

**Le bruit et le filtre étaient un seul levier et en font deux**, parce qu'ils sont indépendants : une pièce calme sur une mauvaise ligne, un café bruyant sur une ligne nette. Le **bruit** est un fond sonore, donc un fichier, donc du contenu que la brique *Lieu* possède déjà ; le **filtre** est un traitement du signal — la bande passante d'un téléphone, un hachage périodique. Les deux s'appliquent **à la lecture**, jamais au rendu mis en cache, le même fichier servant d'étalon à la mesure.

**Ce qui reste ouvert est la standardisation, et une moitié seulement est solide.** Une même position doit valoir une difficulté comparable d'un fond à l'autre, sinon le levier ne veut rien dire. Le **rapport signal/bruit en décibels** standardise le niveau : il se calcule et il est comparable. Il **ne standardise pas la difficulté** — un fond de conversations est bien plus dur que du bruit rose au même rapport, parce que c'est de la parole concurrente, et rien ici ne mesure cet écart. Le filtre, lui, demande sa propre liste fermée d'effets nommés portant chacun son intensité déclarée ; comment elle s'encode n'est pas décidé.

**Correction**

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `echo` | marches | explicite, indirect, absent | indirect | le modèle |
| `explication` | marches | la règle et la phrase, la règle, aucune | aucune | le modèle |
| `reformulations-permises` | nombre | 0 à sans maximum | sans maximum | l'app |
| `correction.fait-refaire` | marches | non, oui | oui | l'app |

`echo` et `explication` sont des **aides**, donc leurs positions se lisent à l'envers de l'intuition : c'est l'absence qui est le cran dur. Elles se déclarent quand même du facile au dur, comme tout le monde.

**Pertinence**

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `pertinence.fait-refaire` | marches | non, oui | non | l'app |

C'est le seul levier de cette aptitude, et il ne dit rien de ce qu'elle exige : tout ce qu'elle exige vit dans la **consigne**, qu'elle est la seule à porter. Le registre, la longueur imposée, le mot interdit n'ont pas de côté dur, donc ils ne sont pas des leviers. Il lui reste par ailleurs ce que toute feuille a, sa sensibilité et son poids.

**Fluidité**

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `capture` | marches | doigt, armée, armée et envoi au silence | doigt | l'app |
| `seuil-silence` | nombre | secondes | — | l'app |
| `duree-tour` | nombre | secondes, maximum 30 | 30 | l'app |
| `preparation` | nombre | secondes | 0 | l'app |
| `jeter-la-prise` | marches | permis, interdit | permis | l'app |
| `fluidite.fait-refaire` | marches | non, oui | non | l'app |

`seuil-silence` et `preparation` sont **sans objet** en capture au doigt, où c'est le pouce qui arme. `jeter-la-prise` l'est en capture armée avec envoi au silence, pour la raison inverse — c'est la seule position où l'apprenant n'est pas seul à envoyer (« La capture »). Le maximum de `duree-tour` est le plafond technique, et il remonte quand le fenêtrage de l'analyse arrive.

**`preparation` est le temps entre la fin de la réponse de l'IA et l'armement du micro.** Elle était nommée une fois en passant, parmi les aides qu'un curseur retire, sans levier ni définition. Elle en a une maintenant, et c'est celle-là plutôt qu'un temps de réflexion accordé *à l'intérieur* du tour : ce second sens serait une ligne interne à la mesure de fluidité, et une ligne interne ne se règle jamais — c'est déjà le sort du délai de grâce d'une seconde. Vivant hors du tour, elle ne touche aucune mesure.

**L'activité** — ne pressent aucune aptitude en particulier.

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `vies` | marches | pas de vies, comptées | pas de vies | l'app |
| `vies.restantes` | nombre | 0 à sans maximum ; 0 met fin | — | l'app |
| `avance.mots` | marches | poursuit, attend | poursuit | l'app |
| `avance.son` | marches | poursuit, attend | poursuit | l'app |

**Les cinq `fait-refaire` et les deux `avance` sont sur deux axes différents.** Le premier dit **si le passage est à refaire** quand une aptitude ne passe pas ; le second dit **si la conversation attend** pendant qu'on le refait. On peut vouloir marquer sans bloquer, ce qui est la conversation libre ordinaire.

**`avance` en fait deux, et pour la raison qui a déjà coupé les tentatives permises** : un défi qui ne vise que la prononciation garde ses redites intactes quoi qu'il arrive du côté des mots. Bloquer sur la grammaire en laissant filer la prononciation est l'ordinaire ; l'inverse est un exercice de prononciation. Avec un seul levier, aucun des deux ne s'écrit.

**Et « attendre » ne veut pas la même chose des deux côtés.** Côté mots, le verdict arrive avec la réponse, donc attendre veut dire *jouer l'écho au lieu de la continuation* — un choix entre deux choses déjà rendues, gratuit. Côté son, le verdict arrive après l'appel : retenir vraiment la réponse mettrait l'analyse dans le chemin critique de **chaque tour**, en plus des 11,0 s de médiane qui sont le premier défaut du projet. Donc côté son, attendre veut dire seulement que **le passage ne se ferme pas** — la réponse se joue, le gros bouton reste indisponible tant qu'une redite n'a pas été faite. C'est cohérent avec ce que le doc dit déjà de la redite quand l'IA a parlé : un exercice, qui ne rejoue rien du fil.

**`attend` est le cran dur parce qu'on ne sort pas d'un passage sans l'avoir refait** : le gros bouton n'est pas disponible, donc on ne peut pas s'échapper en disant simplement autre chose. La combinaison *attend* avec zéro tentative permise n'est pas un cul-de-sac : les tentatives sont épuisées d'emblée, le passage se clôt non réparé aussitôt, et « attend » n'attend jamais.

**Ce que l'exercice a trouvé.**

**Trois leviers nommés au singulier en sont deux chacun**, et la règle de coupe déjà écrite les découpe sans qu'on ait à en inventer une : zéro éteint le levier quand c'est vrai, sinon deux leviers. Le seuil de silence l'était déjà ; la **cadence** et les **vies** le deviennent. Dans les deux cas, aucune valeur du nombre ne veut dire *pas de contrainte* — une cadence à 0 % exigerait l'instantané, zéro vie met fin, donc rien n'exprime « cette activité n'a pas de vies ». Trois cas sur trois est un signe que la règle est la bonne.

**L'explication de la faute manquait sa position d'absence.** Le doc en nommait deux, la règle seule et la règle plus la phrase ; sans une troisième qui dit *aucune*, une conversation ordinaire, où l'app n'explique rien, n'était pas exprimable.

**Une feuille binaire n'a pas de sensibilité du tout.** Le tour interrompu vaut 0 ou 1 sur un passage : toutes les bornes possibles rendent la même paire de lettres, et un chiffre à deux valeurs ferait de toute façon un mauvais membre de moyenne — tout ou rien, aucune gradation. Elle **ne donne donc pas de note** et travaille par ses conditions seules (« Ce qui ne donne pas de note »). Ni sensibilité ni poids : deux champs qui n'ont pas d'objet plutôt que d'être déclarés inertes, et l'écran custom n'a aucun curseur à y offrir.

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

**Un patch porte des positions de leviers ou des déplacements, et éventuellement des consignes.** Une **position** a exactement la forme des réglages — une clé et une position — et hérite donc de la phrase lisible déclarée avec le levier ; un **déplacement** dit de combien de crans on bouge et dans quel sens. Appliquer, c'est superposer ; annoncer, c'est lire la phrase ; proposer à l'IA, c'est envoyer les phrases et attendre une clé.

**Les deux formes sont nécessaires et aucune ne remplace l'autre.** `vies.restantes ← 1` de la mort scriptée ne s'écrit qu'en position ; « perdre une vie » ne s'écrit qu'en déplacement, l'auteur de la règle ne sachant pas combien il en reste quand elle part — et il n'y a pas de sorte d'effet dédiée où le loger, la liste étant fermée. Ce n'est pas non plus une affaire de sorte de levier : la rampe d'arcade est un déplacement sur un levier à marches, un cran de plus. **Un déplacement s'arrête à la borne du levier**, ce qui n'est pas une erreur — la rampe atteint son cran haut par conception —, et **ce qui n'a rien déplacé ne notifie rien**, annoncer *ça se durcit* sans changement étant un mensonge.

**Annoncer dit aussi le sens du changement.** Lire « deux tentatives permises » ne dit pas si on vient de monter ou de descendre, et ce n'est pas la même nouvelle. Les positions d'un levier sont ordonnées et chaque levier sait de quel côté est le dur — c'est déjà ce qui fait qu'un curseur qu'on monte retire une aide ou durcit un jugement —, donc un patch connaît sa direction — un déplacement la porte, une position la trouve par comparaison — et la notification annonce *ça se durcit* ou *ça s'allège*, plus la phrase.

**Sauf la consigne, qui s'annonce sans direction.** C'est du texte libre : rien ne peut comparer deux consignes et dire laquelle est plus dure. Elle s'annonce donc en distinguant seulement les deux cas, *consigne modifiée* avec son texte, ou *consigne retirée*. Ne rien dire serait pire — une exigence qui apparaît ou disparaît en silence rend la note incompréhensible.

#### Les trois familles

**La consigne n'est pas une exception, c'est une case : ce qu'un patch pose a deux propriétés indépendantes, une direction et une phrase.** Trois familles en sortent, et la quatrième combinaison n'existe pas.

| | direction | phrase | ce que c'est |
|---|---|---|---|
| **levier** | oui | oui | la notification dit *ça se durcit* et lit la phrase |
| **consigne**, **interrupteur de condition** | non | oui | la notification lit la phrase, sans direction |
| **drapeau** | non | non | rien ne s'affiche ; ça se pose et ça se lit |

**Un interrupteur de condition n'est donc pas un levier**, et l'affirmation « active est le côté dur » qui traînait ici est fausse depuis qu'une condition lit les deux moitiés de l'échelle : *« atteint A → gagne une vie »* est une condition dont l'activation **allège**. Il garde sa phrase, parce que *« les silences ne coûtent plus rien »* est une nouvelle qu'il faut donner, et il perd sa direction, que personne ne peut qualifier.

**Trois objets à ne pas coller, et c'est facile de les coller.** L'**interrupteur** porte deux positions et chaque position porte sa phrase ; le **patch** est ce qui le déplace ; la **notification** lit la phrase de la position où on vient d'arriver. Il y a donc deux règles en jeu : celle dont le patch déplace l'interrupteur, et celle que l'interrupteur arme ou désarme.

**Un drapeau est un événement nommé**, qu'un patch pose et qu'un déclencheur lit, sans rien afficher. C'est ce qui rend les embranchements d'une scène écrivables à l'intérieur d'une séance, là où les questions déclarées ne passent que d'une scène à la suivante (« Ce qui se souvient »).

**Un drapeau ne se compte pas**, et une définition ne peut pas inventer un levier, le catalogue étant du code. Compter un événement qui n'a pas déjà son levier — *la troisième fois qu'il mentionne le dragon* — s'écrit donc en **chaîne de drapeaux**, une règle par cran, la première armant la deuxième. Verbeux, et écrivable ; un compteur générique attend d'avoir été demandé par une scène réelle.

**Un patch porte une seconde phrase, de mise en scène, écrite d'avance.** La phrase mécanique est déclarée avec le levier, donc une seule fois pour toute l'app : « cinq secondes de silence » ne peut pas se dire *le barman s'impatiente* dans un pub et *le recruteur attend* dans un entretien. La face qui joue la scène dépend de la scène, donc elle vit sur le patch, dans la règle qui l'écrit.

**La mécanique se lit après la réplique, la narrative dit laquelle des deux places elle prend.** Elles ne font pas le même travail : *« il te reste une vie »* constate ce qui vient d'arriver, donc c'est un reçu et il vient après ; *« tu te fais bousculer par un passant »* plante le décor du tour qui suit, donc lu après la réplique il fait tomber l'excuse du passant de nulle part. La phrase de mise en scène porte donc un drapeau **avant / après**, par défaut après, sur le patch où elle vit déjà. La mécanique, elle, est toujours après : elle n'a rien à mettre en place.

**Les deux s'affichent, et la narrative ne remplace jamais la mécanique.** Qui ne lit que *« le barman semble pressé »* ne sait pas que son tour part maintenant tout seul au bout de cinq secondes, et croira à un bug la première fois que ça lui arrive — or toute la valeur de la phrase mécanique est qu'il puisse reconstruire pourquoi sa note a bougé. La mécanique est donc obligatoire, et une conversation libre n'affiche qu'elle, n'ayant pas de fiction ; la narrative est facultative, et une définition qui ne l'écrit pas est sèche, pas cassée. Les deux vont dans le même sens : *le barman se détend* posé sur un patch qui durcit est un mensonge, et la direction, que la mécanique connaît, permet de le voir à l'écriture.

**Elle n'est pas produite par le modèle.** Elle s'affiche à côté d'une affirmation mécanique, donc un texte inventé au moment où ça tombe peut la contredire ; et les patchs sont déclarés d'avance de toute façon, y compris quand c'est le modèle qui choisit lequel s'applique. Il garde tout le reste : il **joue** le changement dans son tour, où il est libre et où c'est gratuit.

**Ce qui est fixe est donc le texte affiché, et la variation vit du côté du modèle.** Une scène rejouée sans fin — l'arcade en est quatre — ne redit pas les mêmes mots pour autant : ce qui doit changer d'une partie à l'autre s'écrit en **message au modèle**, prose libre qu'il joue à sa façon et qui peut provoquer un tour tout de suite. La phrase de mise en scène, elle, a intérêt à ne pas bouger : elle s'affiche à côté d'une affirmation mécanique, et c'est un reçu.

**Ce qui manque se voit, mais on ne peut pas dire ce qui pourrait manquer.** Le catalogue ne propose aucune liste complète des occasions de parler, et il ne le peut pas : une condition sur un élément de feuille est un espace paramétré — quelle feuille, laquelle des trois lectures, quelle valeur — et le déclencheur *le modèle juge que oui* est de la prose libre. Ce qui se dénombre, ce sont les règles qu'une définition a **écrites**, et c'est précisément ce qui sert un auteur : l'écran d'écriture affiche ses douze règles et dit lesquelles n'ont pas de phrase de mise en scène. Pas « voici tout ce qui peut arriver », mais « voici ce que tu as écrit, et voilà ce qui est nu ». Un oubli ne casse rien, la phrase mécanique tombant seule, et les rares événements génériques — perdre une vie, finir, bloquer — arrivent tout faits par une brique (« Les briques »).

Ça donne à l'arcade ce qui lui manquait. Elle devait annoncer chaque changement en une phrase ; elle en a deux, une qui dit la règle et une qui dit le monde, sans quoi monter d'un cran ne ressemble qu'à un compteur.

**Une règle ne se retire pas ; ce qu'elle a fait se défait.** Un patch déplace un levier dans les deux sens, donc alléger est un patch comme un autre. Une règle qui ne doit plus s'appliquer est une règle qu'on **désarme par son interrupteur**, qui porte sa phrase — pas un second étage muet où plus personne ne saurait ce qu'une définition fait sans l'exécuter.

**C'est aussi ce qui écrit la surcharge, et ça évite deux mécanismes.** Une même occasion qui doit produire une chose la première fois et une autre ensuite s'écrit en deux règles dont la première désarme l'une et arme l'autre. Ni plafond de déclenchements, ni ordre de déclaration qui aurait un sens : à tout instant, une seule des deux est armée.

```
R1  (armée au départ)
    quand vies.restantes atteint 0
    → [ message au modèle : la fausse mort,
        patch : vies.restantes ← 1,
        patch : R1 ← désarmée,
        patch : R2 ← armée ]

R2  (désarmée au départ)
    quand vies.restantes atteint 0
    → [ finir, issue = raté ]
```

Trois choses tombent de cette forme.

**La rampe cesse d'être un champ** : c'est une règle dont le quand est « tous les N passages », le quoi un cran de plus, le qui « écrit ». **Et les conditions aussi** : « une feuille sous la barre → une vie » est une règle dont le patch retire une vie. Deux champs se replient en une liste.

**Et une condition s'allume et s'éteint comme n'importe quoi d'autre, par un interrupteur à deux positions** : active — « un silence de plus de cinq secondes coûte une vie » — et inactive — « les silences ne coûtent plus rien ». Deux textes plutôt que la négation du premier, qui donnerait du français bancal alors que tout l'intérêt de ces phrases est de se lire. Un patch le déplace et la notification lit la phrase — mais sans direction, l'interrupteur n'étant pas un levier (« Les trois familles »).

**Le menu d'un tour est calculé, pas maintenu** : c'est l'ensemble des choix offerts par les règles qui se déclenchent maintenant. La plupart des tours il est vide, et on n'envoie rien.

**Ce qu'un tirage ou l'IA a choisi s'écrit sur l'activité.** C'est le prix de ces deux décideurs et il suit le critère du projet : on stocke ce qui dépend de quelque chose qui ne se retrouvera pas. Sans ce journal, l'état effectif d'une séance ne se recalcule plus, et une séance ne se compare plus à elle-même trois semaines après. Garder une graine de tirage coûterait moins cher et ne rejouerait juste que si le code n'a pas bougé.

**Les sortes de déclencheurs sont une liste fermée et courte**, déclarées comme les positions d'un levier. La pente est d'y glisser un mini-langage de conditions ; ce jour-là, plus personne ne sait ce qu'une définition fait sans l'exécuter.

### Comment les règles d'un même moment se résolvent

**Par vagues.** Tous les déclencheurs sont évalués contre le **même instantané**, celui du début du moment ; puis tous les effets s'appliquent ensemble. Ce que ces effets viennent de rendre vrai ouvre la vague suivante, et ainsi de suite **jusqu'à ce que plus rien de neuf ne se déclenche**.

**Une règle ne se déclenche qu'une fois par moment**, et c'est cette borne-là qui fait que ça termine : il y a un nombre fini de règles, chacune part au plus une fois, donc la cascade s'arrête, même si deux règles se relancent l'une l'autre. Une règle qui partirait deux fois dans le même instant serait de toute façon un bug d'écriture.

**Ça remplace la garde d'un seul saut** que le doc posait pour les déclencheurs de levier. Elle était là contre les boucles, et cette borne-ci le fait mieux : elle termine pour une raison qu'on peut prouver au lieu d'un plafond de profondeur choisi, et elle rend écrivable la chaîne de drapeaux qu'un saut unique interdisait.

**À l'intérieur d'une vague, l'ordre ne change rien**, donc l'ordre de déclaration n'a aucun sens à porter. Reste un seul cas où il mordrait : **deux patchs d'une même vague sur la même clé**. Ce n'est pas un cas à arbitrer par une priorité, c'est une erreur d'écriture — **sauf s'ils posent la même position absolue**, le seul cas où la coïncidence se voit sans exécuter. Deux déplacements, ou un déplacement et une position, ne se lisent pas : leur composition dépend d'où on partait. Que ce conflit se voie à l'écriture est tout l'intérêt de cette forme ; **qui le regarde reste à écrire** (« Ce qui reste à spécifier »).

**Ce que ça coûte, dit franchement** : les systèmes d'auteur font plutôt l'inverse, en séquence, où poser une valeur puis la lire marche dans le même souffle. Ce qu'on perd est une chaîne de trois choses dans un même instant ; ce qu'on gagne est qu'une définition se lit sans être exécutée, qui est la peur écrite de ce doc. Et les beats d'une scène sont séparés par des tours de parole de toute façon, donc par des moments distincts.

**La vérification terminale vient après les vagues.** Zéro vie met fin, et ça s'évalue **une fois, sur l'état stabilisé**, jamais sur la transition — sinon la fin gagnerait toujours la course contre la règle qui remplit les vies, et la mort scriptée serait inécrivable.

**Une règle se résout avant que l'IA réponde**, parce qu'elle doit pouvoir fabriquer l'occasion de la contrainte qu'on vient de poser. Le cycle d'un passage est donc : il se ferme, les règles se déclenchent, l'IA répond en connaissant déjà ce qui a changé, son tour est dit, la notification affiche le changement quelques secondes hors du temps de parole, le micro s'arme. Quand c'est l'IA qui choisit, **elle choisit et répond dans le même appel** — deux sorties, pas deux allers-retours, ce qui compte quand la latence est le premier défaut du projet. Contrepartie : elle choisit en sachant ce qu'elle a envie de dire, et le menu est ce qui borne ça.

### Quand une règle se déclenche

**Trois moments, et ils ne se distinguent pas par le goût mais par ce qui est calculé à cet instant.**

- **Pendant l'enregistrement.** Deux horloges tournent, visibles toutes les deux : le temps d'enregistrement écoulé et le silence en cours. Aucune feuille n'existe encore, la personne est en train de parler — donc une règle de ce moment ne peut lire qu'une horloge. Ce n'est pas une restriction posée, c'est un fait sur ce qui existe.
- **À la fin d'une tentative.** Le tour est parti, le modèle a répondu, l'analyse a tourné. Se décide là ce qui concerne cette tentative : les deux portes, et si la conversation attend ou poursuit.
- **À la fermeture du passage.** La note du passage est celle de la dernière tentative et le compte des tentatives est connu. Tombe là tout le reste : les patchs, la rampe, les vies, la fin de la séance.

**Un passage se ferme au gros bouton, pas quand un tour part.** Un passage est un énoncé et toutes ses redites, donc il contient autant de tentatives qu'on en fait, et en « attend » il ne peut pas se fermer du tout. C'est pourquoi le blocage ne peut pas attendre la fermeture : la règle qui décide d'attendre est précisément ce qui l'empêche.

**Six sortes de déclencheur, et la liste est fermée.**

- **une horloge atteint sa valeur** — laquelle des deux, et la valeur. Les deux sont le **temps maximal d'enregistrement**, dont les 30 s ne sont que le plafond technique et jamais la valeur réglée, et le **seuil de silence** de la troisième position de capture.
- **un nœud de l'arbre dit quelque chose** — lequel, laquelle des lectures qu'il offre (un élément, le chiffre, la note — les deux premières sur une feuille seulement), et une valeur.
- **un compte de passages** — à tel passage, ou tous les N.
- **le modèle juge que oui** — une phrase en prose, *« s'il dépasse les limites de la politesse »*, et il répond oui ou non.
- **un levier a bougé** — lequel, et dans quel sens. Il lit un **changement**, jamais une position. Son moment est celui du patch qui a bougé le levier.
- **un levier atteint une valeur** — lequel, et laquelle. Le jumeau exact de la première sorte, qui lit une horloge de la même façon.

La cinquième existe pour que la porte de devant soit **composable**. Réagir à la perte d'une vie, sans elle, oblige à coller le même message sur chaque règle qui en retire une, donc à le réécrire autant de fois qu'il y a de façons d'en perdre — l'exhaustivité qu'un auteur ne peut pas tenir. Avec elle, la réaction s'écrit une fois pour la scène, quelle que soit la règle qui l'a causée.

**La sixième a été ajoutée pour un cas que rien d'autre n'écrivait : la mort scriptée.** Un boss qu'on ne peut pas vaincre, dont l'issue est une fausse mort qu'on traverse — zéro vie, une scène, puis une vie qui revient. Sans elle, « quand les vies tombent à zéro » n'est pas écrivable : la cinquième se déclenche à *chaque* vie perdue, et aucune autre ne lit une position. Le doc affirmait qu'aucun déclencheur ne lit une position de levier ; c'était vrai quand il y en avait trois, et ça ne l'est plus.

**Aucune sorte ne lit une horloge hors de l'enregistrement, et c'est une borne choisie.** *« Après deux minutes de conversation »* n'est pas écrivable : les seules horloges sont celles du tour. Le système est par tours, donc une durée de séance ne gouverne rien qui puisse se déclencher entre deux paroles. La liste étant déclarée fermée, l'absence se lirait comme un oubli si elle n'était pas nommée.

**Le quatrième ne coûte pas d'appel** : le modèle répond dans celui qu'on fait déjà, en un champ énuméré — le contrat le moins cher qui soit. Il porte le drapeau **demandé** avec tout ce qu'il implique : personne ne le vérifie, il ne se rejoue pas, le banc ne l'éprouve pas. Et il peut parfaitement parler de langue, c'est une app de langue ; ce qui le borne est ailleurs et suffit — **l'effet d'une règle ne produit jamais une marque ni une note**, les trois sortes ne touchant aucun marquage. *« Elle se braque parce qu'il est trop familier »* est une conséquence d'histoire, pendant que l'empan est marqué *à côté* à l'écran : deux choses déclenchées par le même comportement, pas deux verdicts concurrents. Reste un coût d'auteur — les deux peuvent sembler se contredire à l'écran, et c'est à la scène de les accorder.

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

**L'ordre des champs de retour est la cloison qui reste gratuite.** Le modèle écrit sa réponse en séquence et chaque champ écrit conditionne le suivant, donc `intended` rédigé avant que la voix du personnage soit prise vaut mieux que le contraire. D'où le contrat : `intended`, les trois marquages — les empans de langue, le bafouillage, le suivi —, `spoken`, la difficulté du tour qu'il vient d'écrire, et `title` s'il y a lieu. Le marquage absorbe `faulty`, qui était un booléen sur le tour entier (« Ce que ça change au marquage »).

**Zéro mot retenu doit être énonçable au retour.** Sur un tour entièrement fait de remplissage et de morceaux abandonnés, il n'y a pas de phrase à reconstruire, et un `intended` en texte libre en inventerait une — le pire des retours, puisque rien ne le distingue d'une vraie. L'instruction vit dans le contexte permanent, à côté de celle qui interdit de compléter un tour interrompu, et ce que ce cas devient ensuite est écrit aux « deux portes ».

**Le coût en latence n'est pas borné.** La mesure au dossier — le modèle achève son objet 0,16 s après sa première phrase (`../reference.md`) — a été prise pour décider du pipelinage de la synthèse, sur le contrat d'alors : une réponse, `intended`, un booléen. Le contrat ci-dessus ajoute trois marquages, la difficulté, l'écho de reprise et le choix de menu, dont les empans qui pèsent à peu près la moitié de l'historique ; en sortie, séquentielle par nature, c'est de la latence directe, sur le défaut déjà premier du projet. **À re-mesurer quand le contrat enrichi existe, avant d'empiler dessus** (`../../TODO.md`) — et c'est la même mesure qui dira si le second appel reste gratuit, l'argument qui l'écarte s'adossant au même chiffre.

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

**Cet invariant a été écrit contre les réglages, et sa portée s'arrête là.** Il dit qu'aucun levier ne déplace une marque ; il ne dit pas que le marquage est reproductible. Les trois marquages jugés viennent d'un modèle de langue non déterministe : la même phrase resoumise peut rendre des empans différents, et aucune architecture ne l'empêche. Ce n'est pas une violation, c'est une garantie que l'invariant n'a jamais donnée — à ne pas le citer un jour comme s'il l'avait donnée. Ce que cette variance vaut réellement est une inconnue nommée, et elle se lit à l'usage (`../../TODO.md`).

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

**Une note est un nombre de 0 à 1 ; la lettre en est l'affichage.** Les lettres découpent l'échelle en cinquièmes égaux — E jusqu'à 0,20, D jusqu'à 0,40, C jusqu'à 0,60, B jusqu'à 0,80, A au-dessus —, donc **la barre A/B vaut 0,60**, par construction et non par mesure. Chaque bande se lit en quarts : le quart bas donne le modifieur `−`, le quart haut le `+`, la moitié centrale la lettre nue. Quinze valeurs affichables, ce qui est un bon grain pour un écran sans devenir un chiffre déguisé.

**Le modifieur est d'affichage seulement** : rien ne le stocke, et une **condition nomme une lettre**, jamais un `B+`. Qui veut plus fin lit le chiffre de la feuille, qui est fait pour ça.

Moyenner des lettres a été essayé et ne tient pas. Deux passages, l'un à 8 % de sons ratés et l'autre à 1 %, tombent tous deux en A et rendraient la même note ; et un dixième de pourcent de plus ferait basculer la note du passage d'un demi-cran. Des paliers plats séparés par des falaises. C'est aussi ce qui fait tenir la phrase de ce doc qui dit que **c'est la sensibilité qui rend les feuilles comparables** — traduire « 8 % de sons ratés » et « 3 demi-tons d'écart » en un même repère demande mieux que cinq valeurs. Le prix est réel : l'apprenant ne peut plus recalculer sa note de tête depuis les lettres affichées, un A cachant où il se situe dans le A. C'est du même genre que ce que le doc accepte déjà — une note ne se lit pas sans la combinaison qui l'a produite.

**La lettre est toujours une note, jamais une mesure.** Elle est contextuelle par construction, puisque la sensibilité déplace ses bornes ; une mesure, elle, ne bouge pas. Les deux ne doivent donc jamais porter les mêmes noms. **On garde les nombres et les crans en base, jamais les lettres** : les bornes pourront bouger sans abîmer les vieilles séances.

### L'arbre des poids

**Ce qui se note est un arbre, à profondeur libre.** Aptitude, mesure, découpage plus fin ne sont pas trois natures : ce sont des nœuds, et la profondeur dit seulement à quel grain on peut peser. La question « est-ce une mesure ou une catégorie de mesure ? » ne se pose donc jamais, et démultiplier revient à creuser une branche, jamais à changer de mécanisme. **Rien n'oblige un nœud de premier niveau à être une aptitude** — l'arbre ne connaît que des poids.

**La note d'un nœud est la moyenne pondérée de ses enfants présents**, et rien d'autre : pas de plancher, pas de bonus, pas de formule propre à une aptitude.

Deux réglages par nœud, et ils ne font pas la même chose :

- **la sensibilité** — où tombent les bornes A–E de cette note ;
- **le poids** — combien cette note pèse dans celle du dessus. À 0, le nœud ne compte pas.

Un défi qui ne note que l'accent tonique est donc un poids à 1 et des poids à 0, pas un mécanisme à part. C'est ce qui permet de viser sans ajouter de champ : quoi qu'on note, l'information est déjà là.

**Peser une feuille qui rend presque toujours la même valeur déplace toute l'échelle du nœud**, et ça se dit à qui écrit un arbre plutôt que de s'interdire. Une feuille qui vaut 1,00 sur presque tous les passages est un 20/20 permanent : à poids égal avec une feuille qui bouge, la note du nœud ne descend plus sous la moitié, donc la barre à 0,60 ne se franchit presque plus par le bas. Une feuille qui vaut 0 presque toujours fait l'inverse, et un passage parfait plafonne aux deux tiers. Dans les deux cas c'est rattrapable en resserrant les séries des autres feuilles, mais les lettres affichées veulent alors dire autre chose qu'ailleurs.

### Ce qu'une feuille déclare

**Un défi s'écrit contre une liste, jamais contre le code.** Sans ça, poser une condition — la feuille *le plus long silence*, cinq secondes — demande de savoir que cette feuille existe, comment elle s'appelle, et que son chiffre est en secondes et non en pourcentage. C'est-à-dire de lire le calcul.

**L'arbre des feuilles est donc déclaré en un endroit**, et chaque feuille y dit :

- son **nom** et sa place dans l'arbre ;
- ses **éléments** — ce qu'elle lit : les sons du passage, les mots retenus, les mots prononcés, le temps du tour, ou le passage entier quand elle n'a qu'un élément ;
- **comment un élément prend sa valeur** — une colonne, une rampe, un vrai/faux, ou rien quand il n'y a qu'un élément ;
- l'**unité de son chiffre** — une part, des demi-tons, des secondes, un pourcentage d'écart ;
- sa **direction**, quand le chiffre garde une unité brute ;
- sa **série**, sur laquelle la sensibilité pioche ;
- si elle **prend une consigne**, c'est-à-dire si elle est jugée ou calculée ;
- si elle **donne une note** — certaines n'existent que pour les conditions.

Un défi se réduit alors à quatre choses posées sur cette liste : des poids sur les nœuds, une position de sensibilité par feuille, des consignes sur les marquages jugés, des conditions. Rien n'y est un branchement de code neuf, et une définition écrite par un modèle devient possible sans lui donner le code — on lui donne le catalogue, il rend ces quatre choses.

### Une feuille, c'est trois choses

Un **jeu d'éléments**, une **façon de donner une valeur à un élément**, une **série**. La troisième est toujours la même ; c'est la deuxième qui change de forme.

- **colonne** — une valeur par cran, pour les marquages jugés : `plat` vaut 0,50, `ok` vaut 0,90.
- **rampe** — une courbe sur une quantité, pour les sons : 0 point d'écart vaut 1,00, 30 points valent 0.
- **vrai/faux** — pour les binaires : l'appui est au bon endroit, ou il ne l'est pas.

**Le chiffre est la moyenne des éléments.** Une part et une moyenne sont la même chose — une proportion, c'est la moyenne d'un 0/1 —, donc il n'y a qu'une recette.

**Quand la feuille n'a qu'un seul élément, il n'y a rien à moyenner** : le chiffre garde son unité brute, et la série la lit directement. Une rampe ne sert qu'à ramener des éléments hétérogènes sur une échelle commune avant de les moyenner ; sans moyenne, elle n'a pas d'objet. C'est le cas du plus long silence, du débit et du suivi.

**La valeur d'un élément est toujours une qualité entre 0 et 1, le haut étant le bon bout.** D'où le fait que la **direction** ne se déclare que pour les feuilles dont le chiffre garde une unité brute — la mélodie, la continuité, le plus long silence et le débit, où bas est bon.

**Jamais un nombre d'occurrences dans une note.** Deux gros ratés dans *Where is it?* et deux dans *I was thinking about going to the market* ne valent pas la même chose : sur quatre mots dont deux abîmés, personne ne rattrape le sens ; sur neuf, le contexte répare. Le nombre ne disparaît pas pour autant, il change d'endroit — « zéro faute franche » est une **condition** branchée sur les éléments.

**Le dénominateur est ce que la feuille lit**, pas tout le passage : sur un défi qui ne pèse que les *th*, un raté sur les quatre *th* de la phrase fait 25 %, pas 3 %. Le poids choisit les colonnes, le dénominateur suit le poids.

### La sensibilité est une série, et une position est une fenêtre

**Ce que l'app lit pour transformer un chiffre en note est une table de quatre bornes**, dans l'unité de la feuille, lue telle quelle. Aucun calcul entre feuilles : chaque borne en jeu est une valeur écrite à la main.

Une transformation unique — « sévère, c'est les bornes divisées par deux » — a été essayée et ne tient pas. Le même geste durcit sainement les gros ratés, où 8 % deviennent 4 %, et casse la mélodie, où 2 demi-tons deviennent 1, c'est-à-dire sous la bande de bruit de la machine : personne ne peut plus y atteindre A, et pour une raison qui n'a rien à voir avec sa mélodie. Les deux ne parlent pas la même langue au moment où la transformation s'applique — c'est exactement ce que ce doc dit en écrivant que rien ne rend les feuilles comparables avant le passage en A–E.

**Mais les tables ne s'écrivent pas séparément : une feuille déclare une série, et une position de sensibilité est une fenêtre de quatre bornes consécutives.**

```
intelligibilité (part des sons intelligibles)
  série :  0,80   0,88   0,93   0,96   0,98   0,99
    indulgente → (0,80  0,88  0,93  0,96)
    normale    → (0,88  0,93  0,96  0,98)
    sévère     → (0,93  0,96  0,98  0,99)
```

Trois choses en découlent.

**« Sévère » veut dire la même chose partout : un cran de sensibilité vaut une lettre.** Ce qui valait B en normale vaut C en sévère, sur les sons comme sur le silence comme sur le débit. C'est sans unité par construction, là où « divisé par deux » ne l'était pas : on ne calcule rien entre deux unités, on décale d'un rang dans une liste.

**Rien n'est calculé, tout est écrit**, donc un bord impossible se voit **en écrivant la série**, pas à l'exécution sur un apprenant qui ne comprend pas pourquoi il n'atteint jamais A.

**Une position de plus coûte un nombre, pas quatre.** La question « combien de positions » cesse d'être un arbitrage entre expressivité et travail ; elle se décidera quand les séries seront écrites.

**Colonnes et rampes ne sont pas la sensibilité.** Elles appartiennent à la feuille et ne se règlent pas — c'est la même règle que pour la ligne des 30 points sur les gros ratés. Si les deux se réglaient, durcir la rampe et baisser la borne A feraient la même chose, et plus rien ne dirait lequel a rendu une séance dure. Un défi qui veut sa propre ligne la pose en **condition**, qui lit les éléments avec leur valeur réelle.

**L'effet de la série change selon le nombre de valeurs que le chiffre peut prendre**, mais pas son mécanisme. Sur une feuille qui moyenne des dizaines d'éléments, le chiffre est continu et les quatre bornes découpent des bandes. Sur une feuille à un seul élément, elles reviennent à poser une lettre sur des valeurs — *« à partir de trois secondes, c'est un C »*.

**Deux feuilles sur les mêmes éléments ne se justifient que si deux défis veulent l'ordre inverse.** Une série est monotone : elle rééchelonne, elle ne réordonne jamais. Donc si aucun couple d'apprenants ne se classe à l'envers d'une lecture à l'autre, une seule feuille et une sensibilité suffisent. Ce test est ce qui décide, branche par branche, ci-dessous.

#### Les sons

**Deux feuilles, sur les mêmes éléments, lues par deux rampes.** C'est le test d'inversion qui les sépare, et il passe. Deux apprenants, cinquante sons chacun : **A** a un accent épais mais reste parfaitement compréhensible, tous ses sons à une vingtaine de points d'écart et aucun au-delà de la ligne du gros raté ; **B** a un accent propre mais deux sons complètement faux qui changent le mot, quarante-huit sons à trois points et deux à quatre-vingt-quinze. Sur la moyenne des écarts, A vaut 0,80 et B vaut 0,93 ; sur la marche, A vaut 1,00 et B vaut 0,96. L'ordre s'inverse, donc aucun réglage d'une feuille unique ne rend les deux verdicts.

- **L'intelligibilité** — rampe en marche d'escalier à 30 points : le mot a changé, ou il n'a pas changé. Le chiffre est la part des sons intelligibles. Graduer reprendrait le travail de l'autre feuille, et les deux diraient la même chose en moins net. Sa série est **tassée en haut**, un gros raté étant rare et grave.
- **La proximité** — rampe continue sur l'écart, gros ratés compris. Aucune ligne, donc aucun seuil à trouver, et sa série est **étalée**, l'écart moyen bougeant sur toute la plage d'un apprenant à l'autre.

Ce sont les deux défis opposés du projet : *« fais-toi comprendre »* ne compte que ce qui change le mot, *« gomme ton accent »* compte tout.

**« Points » est l'écart ramené sur 100.** À chaque son, l'analyse compare deux répartitions — la ressemblance à tous les sons de l'anglais telle que le réseau la lit chez l'apprenant, et la même chez le modèle. L'écart est la distance entre ces deux formes. Ce n'est pas une note de prononciation, c'est une distance entre deux lectures de la même machine.

**La ligne du gros raté est celle où la rampe de l'écran sature, 30 points, et c'est la même pour la feuille et pour l'écran.** Ailleurs, deux lettres peintes du même rouge plein compteraient différemment dans la note, et l'apprenant n'aurait aucun moyen de voir la différence sur laquelle la note agit.

Ce que le banc épingle, et ce qu'il n'épingle pas : sur le jeu étiqueté, les témoins sont à 0,3 point, les demi-fautes sous 25, les fautes franches au-dessus de 93 (`../analysis.md`). La ligne est donc quelque part entre 26 et 92, où n'importe quelle valeur sépare aussi bien les deux populations. **30 n'est pas mesuré** : il vient de l'écran, où il avait été posé pour que l'image cesse de changer là où la différence cesse de vouloir dire quelque chose. Ce qui resserrerait l'intervalle, ce sont des fautes vraiment intermédiaires, que le jeu d'essai n'a pas.

Effet à connaître : `09-walkin`, le /ŋ/ de *walking* dit /n/, tombe sous la ligne. Il compte dans la proximité et pas dans l'intelligibilité, ce qui est le bon comportement — on comprend *walkin'*.

**Un son ajouté ou manquant vaut zéro dans les deux feuilles, par nature et pas par franchissement de ligne.** Il n'a aucun point, n'ayant rien en face de lui à comparer (`embedded/Marks.kt`), et l'écran le peint déjà au bout saturé de la rampe : ce n'est pas un degré de faux, c'est une chose qui est là et ne devrait pas y être, ou l'inverse.

#### La mélodie

**Une seule feuille : la distance.** À chaque syllabe, l'écart de hauteur entre les deux courbes, chaque côté ramené d'abord à sa propre médiane, et la **moyenne simple** de ces écarts, en demi-tons. Le chiffre garde donc son unité et la série se lit en demi-tons.

Trois méthodes écartées, chacune par un cas du banc plutôt que par goût. **La corrélation** ignore l'amplitude, donc qui fait la bonne forme deux fois trop petite obtient un score parfait — or le cas 22 du banc, la phrase dite plate à la française, est étiqueté comme une faute : la seule faute de mélodie que le projet ait étiquetée disqualifie toute méthode qui normalise l'amplitude. **La moyenne des carrés** laisse une divergence isolée écraser le reste. **Le recalage temporel élastique** sert à comparer deux courbes décalées dans le temps ; ici elles sont déjà alignées syllabe par syllabe, même texte — et il pardonnerait à qui met sa montée sur la mauvaise syllabe, ce qui est justement une faute.

**Portée : toute la phrase, et ce n'est pas mesuré.** La brique 10 ne lit aujourd'hui que la région voisée finale et n'en rend qu'une pente (`../analysis.md`) ; le contour par syllabe existe dans le chemin d'affichage (`marking/TurnMarking.kt`) et n'a jamais été confronté à des étiquettes. On l'étend quand même : une échelle qui ne parle que là où elle est sûre laisse passer les fautes qu'elle est là pour attraper, et l'emphase — *I said it IS important* — ne vit pas à la fin de la phrase.

**Une seconde feuille reste candidate et n'est pas livrée : les mouvements.** Elle serait à la mélodie ce que l'intelligibilité est aux sons — une lecture en marche d'escalier, le mouvement fait ou non — et le test d'inversion passe au moins sur le papier : qui fait tous les mouvements à moitié d'amplitude passe devant qui est parfait sur cinq et plat sur trois selon la part des mouvements faits, et derrière selon la moyenne des écarts. Deux défis opposés, *« colle au contour »* et *« fais les mouvements »*.

Deux raisons de ne pas la livrer. Elle **ne lit pas les mêmes éléments** que la distance, contrairement aux deux feuilles des sons, donc le parallèle est plus faible. Et **rien n'est mesuré** : le banc porte deux prises étiquetées de mélodie en tout, contre un jeu entier côté sons. Livrer deux feuilles là-dessus, c'est décider avant de mesurer.

**La piste retenue pour elle, si elle s'ouvre : la forme générale de la phrase.** Réduire la mélodie à un petit nombre de mouvements plutôt qu'un par syllabe, ce qui est la façon dont l'intonation fonctionne — quelques mouvements porteurs par énoncé. Les **plages sont celles que délimitent les accents du modèle**, une hauteur représentative par plage, et on compare les mouvements d'une plage à la suivante. Un point de conception décide le reste : **on découpe sur le modèle et on lit l'apprenant sur les mêmes plages** — découper les deux séparément donnerait des découpes incomparables, et pardonnerait de mettre sa montée sur la mauvaise syllabe. La largeur des plages porte la tolérance, sans qu'on ait à la régler comme telle. Et cette feuille mesurerait les **mouvements généraux**, jamais où sont tombés les appuis, qui est le travail de l'accent lexical.

**Le rythme est retiré, y compris des candidates.** Le défaut qu'il visait — ne pas comprimer les syllabes inaccentuées, le trait syllabique du français — **se voit déjà dans les sons** : un schwa dit en voyelle pleine est un autre son, donc un écart au modèle. Ce qu'il ajouterait est le contraste de durée sans changement de phonème, ce qui est marginal, et il chevaucherait l'accent lexical, l'appui se signalant par la durée autant que par la hauteur.

#### L'accent lexical

**Un marquage au niveau du mot, binaire : l'appui est tombé sur la bonne syllabe ou ailleurs.** Le dénominateur n'est pas « les mots » : un monosyllabe n'a pas de choix d'accent, et un mot outil n'a pas d'appui net même chez le modèle. Ce sont donc **les mots de plus d'une syllabe que le modèle accentue nettement**, ce qui demande le seuil « le modèle a-t-il un accent net » que `../analysis.md` réclame déjà. Ce seuil existe et une oreille l'a validé : la marge de la sonde sur le modèle sépare les mots dont l'appui s'entend de ceux dont il ne s'entend pas (`../analysis.md`, brique 7). **La barre est posée à 0,90** : entre 0,80 et 0,94 le balayage rend la même chose, et 0,95 jetterait en plus un mot que l'oreille entend franc, sans retirer une marque de plus. Ce qu'il ne fera pas, mesure faite, c'est baisser le taux de fausse marque — il n'en retire qu'une sur deux, et les autres tombent sur des mots que le modèle accentue au maximum.

**La feuille est actée** (2026-09-05) : elle entre dans la somme, et l'usage jugera ce que les deux inconnues de la brique 7 ne mesurent pas (`../analysis.md`). La brique, elle, tourne dans l'app ; ce qui manque à la feuille est son branchement dans la somme, qui n'est pas dans ce chantier. Tant qu'il n'est pas fait, elle sort de la somme au lieu de valoir zéro — un état d'implémentation, plus une question ouverte.

#### La correction

**Un marquage jugé, donc une feuille, et les crans sont les étiquettes.** Le juge marque des groupes de mots ; l'app déplie chaque empan en ses mots et fait la moyenne. Le dénominateur est **les mots retenus du passage** — le remplissage et les morceaux abandonnés en sortent.

| cran | ce qui le décide |
|---|---|
| `ok` | rien à signaler |
| `mal formé` | la phrase n'est pas bien montée — *I go there yesterday*, *I make my homework* |
| `ne se dit pas` | ça n'existe pas dans la langue |

**Une seule feuille et non une par étiquette, parce que les étiquettes se partagent les mêmes mots.** Un mot en porte une au plus, donc leurs parts sont des tranches d'un même gâteau et non des mesures indépendantes. En faire des feuilles séparées obligeait chacune à rendre un chiffre même quand rien n'était marqué de sa couleur — et une étiquette rare se retrouvait à sa valeur extrême sur presque tous les passages, déplaçant toute l'échelle du nœud. La colonne dit ce que chaque cran vaut, et une tranche vide ne vaut plus rien du tout : elle n'existe simplement pas.

**La colonne s'ancre par une phrase** : *un passage entièrement fait de ça vaudrait…* C'est ce qui la rend écrivable et mesurable, là où une pondération posée dans le vide ne le serait pas.

Faire découper toute la phrase par le juge est écarté : couper du correct n'est vérifiable par personne — *to the market* fait un morceau ou trois selon l'habitude de coupe, et le chiffre bougerait avec elle. Marquer *I go* comme groupe verbal mal formé, ça, se vérifie. Le juge garde donc le travail qu'il fait déjà pour l'écran, l'app compte, et elle compte dans l'unité qui sert déjà au poids par longueur.

**Deux empans qui se recouvrent ne comptent pas deux fois** : un mot prend le pire cran qu'il porte, et il compte une fois. La précédence est `ne se dit pas`, puis `mal formé`, puis `ok`.

**Son nom d'écran est `Grammar` / `Grammaire`, et `correctness` reste le nom du code** (tranché le 2026-09-06). Le terme du code est large et exact — il couvre la syntaxe et l'idiome — mais il se lit à l'écran comme le geste de corriger, ce qui est faux : *« en cause : la Correction »* se comprend *« on t'a corrigé »*. `Langue` a été écarté pour l'inverse : exact et fade, tout étant la langue dans une app de langue. Ce que `Grammar` coûte est borné — quand la faute est idiomatique plutôt que syntaxique, le nom trompe sur le *pourquoi*, et la marque, elle, dit toujours *quels mots*.

**La correction pose une seule question — est-ce que c'est de l'anglais ? — et c'est un jugement absolu, donc elle ne prend aucune consigne.** Une consigne est toujours une exigence de situation ; elle appartient donc entièrement à la pertinence. *« Parle au passé »* ne fait pas de *I'll go there* une phrase mal formée, c'est de l'anglais irréprochable qui ne convient pas ici.

**Mais « absolu » ne veut pas dire indépendant de toute norme.** Rien n'est correct dans l'abstrait : *I ain't got none* est fautif en anglais standard et bien formé dans plusieurs dialectes, *Going out later?* n'a ni sujet ni auxiliaire et c'est de l'oral normal, et *I'm doing good* a déjà été laissé passer par le juge *au motif que c'est familier* (`../../TODO.md`). Ce qui est vrai, et qui est ce dont le projet a besoin, c'est que **la norme est fixée par l'app et identique pour toutes les activités** : aucune consigne, aucune scène, aucun défi ne la déplace. C'est cette invariance-là — et pas une indépendance à toute norme — qui rend la correction **vérifiable au banc sur des phrases isolées**, ce dont le banc grammatical a besoin (`../../TODO.md`, chantier 2).

Cette norme n'est **écrite nulle part** aujourd'hui, et c'est un trou : elle est celle que le modèle se donne tout seul. Elle doit vivre dans le contexte permanent du prompt, et deux choses au moins s'y tranchent — la **variété** (au plus simple, celle de l'accent choisi, qui décide déjà la voix) et le fait que **l'oral n'est pas de l'écrit**.

**Pas de cran au-dessus d'`ok`.** *Une construction difficile montée juste* — *« If I'd known, I would have told you »* contre *« I didn't know, so I didn't tell you »* — est une notion réelle et n'a **aucune norme fixe** : difficile dépend de qui parle, et l'app n'a pas de niveau d'apprenant. L'étiquette casserait donc la propriété qui fait exister cette aptitude. Une construction ambitieuse et réussie se fait marquer `juste`, du côté de la pertinence, où le critère est situationnel par construction.

**Aucun découpage par cause** — temps, accord, préposition — parce qu'une liste de causes n'est jamais complète : elle finit avec un tiroir « autre » qui ne nomme rien et qu'on ne saurait pas peser. Et **aucun découpage par nature d'empan** non plus : viser une partie de la phrase est un acte de situation, donc ça se dit en consigne côté pertinence, et une préposition fautive se retrouve marquée des deux côtés, ce qui est déjà permis. Coût assumé, c'est grossier — *« I go there yesterday »* et *« I goed there »* tombent au même endroit.

**`ne se dit pas` a un effet qui ne transite par aucune note** : au moins un mot de ce cran et l'analyse du son ne tourne pas, quels que soient les poids et les sensibilités — il n'y a pas de modèle à comparer (« Ce qui coupe l'analyse du son »).

#### La pertinence

**L'autre question — as-tu visé juste ?** Juste par rapport à la situation, à qui écoute, à ce qui a été demandé, et à ce qu'on voulait dire soi-même. Même marquage, même dénominateur, même forme : **une feuille, dont les crans sont les étiquettes.**

| cran | ce qui le décide | valeur d'exemple |
|---|---|---|
| `juste` | vise et touche : la tournure que le natif aurait choisie — *I popped into the shop* | 1,00 |
| `ok` | correct, sans plus | 0,90 |
| `plat` | vise bon mais mollement : vague, basique, ou repris — *I did a thing*, *nice* pour la troisième fois | 0,50 |
| `à côté` | ne vise pas ce qu'il fallait : le ton, la situation, la consigne — *Hey mate* à un client | 0,10 |

Sur *« Yeah, I went there last summer. It was nice »* — neuf mots, rien à côté, un mot mou : `(8 × 0,90 + 0,50) / 9 = 0,86`. Sur un passage où une tournure heureuse porte quatre mots sur dix : `(6 × 0,90 + 4 × 1,00) / 10 = 0,94`.

**Une feuille signée avait été refusée, et la raison a changé.** L'objection était qu'il faudrait inventer une pondération entre les étiquettes que personne ne saurait justifier. Elle valait quand rien ne permettait de la poser ; la colonne est ce mécanisme, elle s'ancre par une phrase et elle se mesure comme le reste. Et ce qui oblige à rouvrir est plus fort que l'objection : **les étiquettes sont des parts d'un même gâteau**. Trois feuilles séparées forçaient `juste` à rendre 0 % quand rien n'était marqué — donc, son bon bout étant le haut, la pire note possible, alors qu'il ne s'était rien passé de mauvais. Toute une machinerie a été construite puis démontée pour contourner ça : un bonus qui n'entrait qu'au numérateur, un coefficient par nœud, des poids nuls par défaut, une direction par feuille. **Six pièces disparaissent avec la colonne.**

**Un mot porte au plus un cran**, et une précédence fixe suffit à le garantir : `à côté` devant `plat` devant `juste`. Le cas qui décide est le groupe idiomatique mais hors ton : *crack on with it* dit à un client est de l'anglais précis, et faux ici ; l'exclusion le range en `à côté`, ce qui est le bon verdict. Prix assumé : *stuff*, vague **et** trop familier, se range en `à côté` et n'apparaît pas comme mollesse. C'est le bon compromis quand même, l'étiquetage ne dépendant jamais des réglages.

**C'est elle qui porte la consigne**, donc tout ce qu'un défi veut exiger. La consigne se pose sur le **marquage** et non sur une feuille, et il peut y en avoir plusieurs à la fois, chacune avec sa durée (« Ce qui est jugé, ce qui est calculé »). Elle ne peut **qu'endurcir** : la précédence va de `à côté` vers `juste`, donc une consigne fait passer des groupes vers le cran plus sévère, jamais vers le plus doux. « Ignore les répétitions » reste inécrivable, ce qui est la règle.

**C'est la seule mesure du projet qui ait un bon côté**, et la raison tient à la nature des choses : pour le son, le modèle est la vérité, donc on ne peut pas faire mieux que lui. Pour une phrase il n'y a pas de modèle unique, donc on peut dépasser le simplement correct. Le cran haut est ce qui donne un sens à A.

**Le cran haut ne vaut que s'il est rare.** C'est l'argument déjà fait contre le marquage tout-ou-rien : quand tout est colorié, plus rien n'est signalé. Un juge qui en donne un passage sur deux le rend décoratif, et la consigne doit donc être exigeante. **Non mesuré** : personne ne sait à quelle fréquence ce juge-là en donnera.

**Le registre, la variété, la rareté et la complexité de la phrase n'ont pas de cran à eux.** Le registre est ce que `à côté` dit ; la répétition, la vaguerie et la platitude de construction sont ce que `plat` dit. Un cran par notion aurait compté deux fois les mêmes mots.

**Un même mot peut être marqué par la correction et par la pertinence**, et son poids s'ajoute dans les deux. *« I make a thing »* est mal formé **et** plat : ce sont deux défauts réels, pas un compté deux fois. C'est la seule exception au principe qu'aucun mot n'entre dans deux feuilles dont les poids s'additionnent, et elle est assumée.

#### La compréhension

C'est la seule aptitude qui ne porte pas sur ce que l'apprenant produit. Les quatre autres mesurent des sons, des mots, des silences ; comprendre est ce qui **entre**, et l'app n'a aucune fenêtre dessus — elle ne peut que déduire de la réponse.

**Une feuille, le suivi : un cran par passage, jugé.** C'est la seule feuille du projet dont la finesse ne peut pas venir du comptage — toutes les autres moyennent des dizaines d'éléments, donc un jugement grossier répété donne un chiffre fin. Ici il y a un seul élément, donc la précision *est* celle du cran. C'est pourquoi il en porte six, là où trois suffisaient partout ailleurs.

**Un seul axe : ce que la réponse prouve d'avoir pris.** Sur le tour *« I finally got the tickets, but only for the Sunday matinée »* :

| cran | ce qui le décide | exemple |
|---|---|---|
| `entre les lignes` | répond à ce qui était sous-entendu, pas dit | *« Don't worry, I'll drive you home »* sur *« …though the last train's at eleven »* |
| `précis` | reprend un élément que seul quelqu'un qui a entendu pouvait reprendre | *« Sunday? I thought you wanted the evening one »* |
| `en rapport` | répond à ce que le tour disait, sans reprendre de détail | *« Oh nice, are you going? »* |
| `sur le sujet` | accroche le thème, pas le tour | *« Yeah, tickets are so expensive these days »* |
| `vague` | aurait marché quoi qu'on lui dise | *« Yeah, exactly »* |
| `à côté` | preuve négative : répond à autre chose | *« I went to the cinema last week »* |

**Un axe et un seul.** Un cran *partiel* — a pris une part du tour, raté l'autre — a été essayé et retiré : il mesure **combien du tour a été couvert**, pas la force de la preuve, donc il mettait deux axes dans une échelle qui ne se lisait plus. Le cas qu'il servait, le tour de l'IA qui porte une remarque *et* une question, revient à ce qu'il était : un **jugement** du juge, qui pèse si l'apprenant a pris ce qui appelait une réponse.

**Le cran `vague` est ce qui fait tenir la feuille.** Sans lui, la réponse plausible qui n'engage rien devait être comptée bonne ou fausse, et les deux étaient faux. Quelqu'un peut tenir une conversation entière sur *yeah*, *right*, *I think so*, et sortir avec un A.

**Le haut est offert sur tous les tours**, ce qui compte : tout tour porte du contenu précis, donc `précis` est atteignable partout. Un cran du **milieu** parfois indisponible ne gênerait personne ; c'est seulement un cran du **haut** indisponible qui punirait tout le monde pour une occasion que personne ne lui a donnée. C'est aussi pourquoi `entre les lignes` se pose **juste au-dessus** de `précis` et non loin devant : un tour sans sous-entendu plafonne à `précis`, et ce manque doit coûter presque rien.

**Ce qui est jugé n'est pas la qualité de la réponse mais ce qu'elle montre d'avoir pris.** *« Yes, I go yesterday at the shop and I buy two »* est mal dit et parfaitement en rapport ; *« I like the cinema very much »* est correct et à côté. Confondre les deux ferait de cette feuille un doublon de la correction. Et ce qui se marque n'est jamais la brièveté : *« Did you get the tickets? » — « Yeah »* est spécifique à ce qui a été demandé.

**L'objet est le dernier tour de l'IA ; le contexte est la conversation.** Répondre à la question d'il y a deux tours est `à côté`, même si le thème général tient encore. Mais le juge a besoin du fil pour résoudre les pronoms et les ellipses : *« And did she like it? » — « She loved it »* ne se juge pas sans savoir qui est *she*.

**Le suivi pèse sur la difficulté du tour de l'IA, que le modèle rend avec sa réponse.** La longueur seule était grossière : *« Fancy a cuppa? »* est plus dur que quarante mots simples. Et la position du levier de complexité ne la remplace pas — elle demande un niveau, elle ne promet pas que chaque phrase soit dure. C'est donc une réponse par phrase.

**La difficulté remplace la longueur, et la longueur fait partie de ce que le modèle doit peser en la rendant.** Garder les deux compterait deux fois la même chose. D'où une symétrie utile : les mêmes trois dimensions des deux côtés — longueur, vocabulaire, structure demandées par les leviers, le même trio rendu par le cran.

Quatre choses à tenir avec lui. C'est un **poids, jamais une feuille** : il dit ce qu'on a envoyé, pas ce que l'apprenant a fait. C'est un **cran**, le retour le moins cher qui soit. Il **se stocke avec la version de ce qui l'a produit**. Et ses limites s'écrivent avec lui : personne ne le vérifie, il ne se rejoue pas à l'identique, et c'est le modèle qui note ce qu'il vient d'écrire.

Deux cas de bord. Un passage **sans tour de l'IA devant lui** — l'apprenant qui parle en premier — n'a rien à avoir compris : la feuille est **absente**. Et le **nombre d'écoutes** du tour de l'IA ne donne pas de note : c'est un compte, il vit dans les conditions (« Ce qui ne donne pas de note »).

**Le marquage du suivi est une pastille au bout de la ligne qui nomme le tour**, calée à droite. C'est la seule mesure qui rende un verdict sur tout le passage, et la place le dit sans qu'on ait à l'apprendre : elle est hors du texte, donc elle ne marque aucun mot. **Vert** pour `entre les lignes`, **encre neutre** pour `précis` et `en rapport`, **jaune** pour `sur le sujet` et pour `vague`, **rouge** pour `à côté`.

**Cette ligne est un tableau de bord, et un emplacement vide y dit *non mesuré*.** C'est la seule exception à la règle du projet, où l'absence de marque dit *rien à signaler*, et elle se paie parce que l'emplacement est fixe : un emplacement vide ne se distingue pas d'un emplacement qu'on n'a pas dessiné, donc y lire une approbation serait lire une approbation dans un trou. Le suivi correct montre une pastille neutre plutôt que rien, et le débit, qui partage la ligne, suit la même règle.

**Le vert marque le bon côté partout où il existe** — le cran `juste` de la pertinence et `entre les lignes` ici. C'est un amendement à la règle qui le réservait à `juste`, écrite quand la pertinence était la seule mesure à avoir un haut.

**Le suivi n'est jamais absent pour cause de texte affiché.** Voir le tour de l'IA est une aide en moins à trouver, pas une mesure qui disparaît : il reste à comprendre la langue, et quelqu'un qui lit la question et répond à côté ne l'a pas comprise.

#### La fluidité

Quatre feuilles qui donnent une note, et **rien n'y est compté deux fois** : `continuité` possède tout le silence **entre les mots**, `le plus long silence` possède le blocage, le `débit` ne lit que le temps où la bouche articule, et `le remplissage et les reprises` possède les hésitations.

**La continuité** — la part du tour passée en silence, **moins celle du modèle sur la même phrase**. Le chiffre est en points de pourcentage, et il peut être négatif.

Comparer au modèle fait un vrai travail : ce qui reste chez lui n'est pas rien, ce sont les **pauses prosodiques légitimes**, à la virgule, en fin de proposition. Le modèle s'arrête là, l'apprenant a le droit aussi. Et l'écart s'écrit en **différence** et non en rapport, pour la raison qui a déjà arrêté `r` en mélodie : la part silencieuse du modèle peut tomber à un ou deux pour cent sur une phrase courte, et un rapport y devient hypersensible — le même apprenant à 20 % donnerait un rapport de 10 ou de 40 selon le dénominateur.

Se taire **moins** que le modèle n'est pas une faute de fluidité, et rien n'a besoin d'être écrêté pour ça : un écart négatif tombe au-dessus de la borne A, donc dans la même bande que zéro. C'est une asymétrie voulue avec le débit, où les deux côtés comptent — ne pas faire la pause de la virgule est une affaire de prosodie, pas de fluidité.

**Une pause est un silence entre deux mots qui dure au moins 200 ms.** Deux conditions, et chacune fait son travail. La frontière des mots écarte ce qui se passe à l'intérieur d'un mot — une occlusion, une tenue, un bégaiement tenu ne sont pas des pauses, et la continuité ne les voit pas. Le seuil écarte les micro-blancs qui tombent entre les mots : le /t/ de *to stop* ferme la bouche 50 à 150 ms dans le blanc qui précède *stop*, et sans seuil une phrase riche en /p t k/ paraîtrait moins continue qu'une phrase pleine de voyelles — **pour son texte, pas pour son locuteur**, et comme cette feuille se lit dans l'absolu, ce biais ne s'annulerait nulle part. **Ce que la frontière écarte ne se perd pas** : un silence mi-mot reste dans le temps de parole, donc dans le débit — la continuité possède le silence **autour** des mots, le débit tout ce qui les allonge de l'intérieur. Le seuil est partagé avec lui : il exclut exactement ce qu'elle compte.

**Un délai de grâce d'une seconde s'applique aux deux bords, jamais à l'intérieur.** Sans lui, le temps normal de réagir et de cliquer — le même pour tout le monde, qu'on ait dit un mot ou vingt — pèserait bien plus sur un tour court que sur un tour long : deux secondes de réflexe sur *« Yes, I did »* font 50 %, les mêmes deux secondes devant une réponse de vingt secondes ne font presque rien, pour un comportement identique. La seconde n'est pas mesurée : comme la ligne des 30 points, elle vient d'un jugement, posée à hauteur d'un temps de réaction ordinaire. Et **elle ne touche pas ce qu'une condition lit** : un silence de 6 s reste un silence de 6 s pour qui cherche un gros blanc.

**Le silence final compte, et c'est voulu : rendre la parole est un acte.** Dans une conversation réelle, on signale qu'on a fini — la voix qui retombe, le silence qu'on laisse à l'autre — et tarder à le faire est un vrai défaut, pas du bruit à retirer.

**Le plus long silence** — un seul élément, sa durée en secondes, et la série se lit en secondes : *« à partir de trois secondes, c'est un C »*.

Elle se sépare de la continuité par le test d'inversion, et il passe : **A** passe 40 % du tour en silence mais que des petites pauses, jamais plus de 0,8 s ; **B** en passe 25 % dont un blanc de 5 s d'un seul tenant. La continuité met A derrière, le plus long silence met B derrière. Deux défis opposés : *« ne laisse pas de trous »* et *« ne reste jamais bloqué »*. Le doc affirmait que ces deux-là allaient dans le même sens ; c'était trop vite dit.

Prendre les silences comme éléments et faire leur moyenne ne marcherait pas : un tour sans aucun silence n'aurait pas d'élément, donc pas de feuille, et le tour le plus fluide possible ne serait pas noté. Une part du temps passée dans un blanc ne marcherait pas non plus, parce qu'elle serait diluée par la longueur du tour — le même blanc de 5 s ferait 50 % dans un tour de 10 s et 17 % dans un tour de 30 s, pour le même comportement. Le pire silence, lui, ne dépend de rien d'autre que de lui-même.

Ce qu'elle ignore, assumé : deux blancs de 5 s valent un seul. C'est la continuité qui porte l'accumulation.

**Les deux se marquent d'un seul dessin : une colonne de points empilés dans le blanc entre les mots** — `I went · to the shop`. **Trois crans, à 0,2 / 0,5 / 3 secondes**, un point par cran. Le premier cran commence au seuil lui-même, donc toute pause comptée porte au moins un point. Le plus long silence est la plus haute colonne : il se voit parce qu'il est le plus long, on n'a pas à le désigner.

**Les points tiennent dans le blanc qui existe déjà, sans déplacer le texte.** C'est ce qui les rend compatibles avec la courbe de mélodie, ancrée aux caractères : elle reste d'un seul tenant au-dessus des pauses, puisqu'aucune colonne ne s'insère entre les mots. Les crans servent deux feuilles qui ne regardent pas la même plage : la coupe à 0,5 s sépare le détachement, que compte la continuité, de la vraie pause, et la coupe à 3 s garde distincts un blocage de 3 s et un de 5 s, qui font tout le métier du plus long silence. Au-delà de trois points l'image sature — c'est le renoncement de la rampe, au même endroit : un blocage de 4 s et un de 8 s se ressemblent, et la feuille porte la différence. **Les silences des bords se marquent pareil**, dans la colonne vide qui borde le tour : l'initiale avant le premier mot, la finale après le dernier, qui compte délibérément (« rendre la parole est un acte »).

**Aucune couleur sur les points.** Ce qui fait qu'une pause est trop longue dépend du réglage, et aucun réglage ne touche une marque. Teinter *le plus long* serait pire : la même pause de 2 s serait teintée dans un tour calme et grise dans un tour où traîne un blanc de 3 s, donc la même faute ne produirait plus la même marque. Les points portent la durée, rien d'autre.

**Des points médians, pas des points de ligne de base.** Trois points bas veulent dire *du texte a été retiré* — c'est déjà ce que disent les crochets gris du remplissage (« Le remplissage et les reprises »).

**Le débit** — l'écart de vitesse au modèle, en pourcentage, sur les seuls mots retenus.

```
v     = temps de parole de l'apprenant ÷ temps de parole du modèle
écart = (le plus grand de v et 1/v) − 1
```

**Comparé au modèle et non dans l'absolu**, ce qui remet le débit sous la règle générale du projet — *le modèle est la vérité, s'en écarter se marque* — à laquelle il n'avait aucune raison d'échapper. Le texte étant le même des deux côtés, il n'y a **rien à compter**, ni mots ni syllabes : l'unité s'annule, et la question « combien de mots par seconde parle un bon apprenant », qui n'a pas de réponse indépendante du texte, disparaît.

**Symétrique par construction** : deux fois plus lent et deux fois plus rapide donnent tous les deux 100 %. **Savoir de quel côté on s'écarte ne se perd pas.** La note ne le voit pas : parler 30 % trop lentement et 30 % trop vite donnent le même chiffre, donc la même lettre. Ce qui le lit est la **marque** — les chevrons ci-dessous — et une **condition** de défi, où *« ne parle jamais plus de 20 % plus lentement que le modèle »* se distingue de *« pas plus vite »*. Ce qu'un retour écrit en dirait n'est pas décidé : rien n'est encore fixé sur ce qui s'affiche hors des marques. **Hypothèse à vérifier** : que les deux côtés soient également gênants. La lenteur est le défaut du francophone, l'excès de vitesse est rare. Si la mesure dit qu'ils ne se valent pas, la série devient asymétrique — deux listes au lieu d'une, une extension et non une refonte.

**Le temps de parole exclut les silences des deux côtés**, donc le silence n'est jamais compté deux fois. Ce qui veut dire que **détacher ses mots au lieu de les lier** — le défaut francophone que le doc voulait voir ici — se fait voir par la **continuité**, sous forme de petites pauses, ce qui est plus juste : détacher ses mots, c'est littéralement en fabriquer. Le débit garde ce qu'il est seul à voir : **articuler lentement à l'intérieur des mots**, voyelles étirées et consonnes sur-prononcées — et les tenues fermées aussi, qui produisent un silence mais allongent le mot, puisqu'une pause est un silence entre deux mots.

**Et il ne lit que les mots retenus.** Le temps de parole brut de l'apprenant contient ses *um* et ses morceaux abandonnés, que le modèle ne dit pas : un tour hésitant aurait donc un temps gonflé, un gros écart de vitesse, et serait pénalisé sur le débit **en plus** de sa propre feuille. Le doc voulait ça quand rien d'autre ne voyait les hésitations ; maintenant qu'elles ont leur feuille, la raison est servie une fois. L'alignement sait où tombe chaque mot, donc la durée des seuls mots retenus se calcule sans découper l'audio.

**Le débit se marque sur la ligne qui nomme le tour, à côté de la pastille du suivi** — `<->` puis `<-->` du côté lent, `><` puis `>><<` du côté rapide, et `=` quand le tour est dans la bande. Les pointes qui rentrent disent le mot comprimé, celles qui s'écartent le mot étiré : la forme porte le côté, donc la couleur ne porte que la distance — les deux crans de la rampe, l'encre neutre pour le `=`. **Deux crans par côté, à seuils fixes**, écrits une fois pour toutes comme la rampe des sons et jamais tirés de la sensibilité — sinon la marque bougerait avec le réglage.

**Le `=` n'est pas une félicitation** : il dit que la mesure a eu lieu et qu'elle ne trouve rien, ce qu'exige la règle du tableau de bord ci-dessus. Les cinq positions se lisent alors comme une aiguille, et le *non mesuré* n'a pas de signe à lui — c'est l'emplacement vide.

**Une question qui ne se pose pas, notée pour qu'elle ne se repose pas.** Le débit et la continuité prennent le modèle pour référence, donc une voix rapide rend l'apprenant lent et une voix lente le rend rapide. Ce n'est pas un défaut à mesurer, c'est la règle du projet : le modèle est la source de vérité, une voix qui ne convient pas se remplace, et la suivante est la vérité exactement comme la précédente (`../reference.md`, question close). Et l'argument du biais de machine, qui vaut pour les sons, ne s'applique pas ici : une durée est une durée, mesurée pareil des deux côtés, il n'y a aucun biais à annuler.

#### Le remplissage et les reprises

**Une feuille, dont les crans partagent les mots prononcés.** Même forme que les marquages de la correction et de la pertinence, et pour la même raison : un mot prononcé est retenu, ou abandonné, ou du remplissage — trois tranches d'un même gâteau.

| cran | ce qui le décide |
|---|---|
| `retenu` | fait partie de la phrase |
| `abandonné` | appartient à un morceau repris — *I went to the—* |
| `remplissage` | *um*, *like*, *I mean* employés comme béquille |

Sur *« It was, like, um, I went to the— I was going to the store »* : quatorze mots prononcés, deux de remplissage, quatre abandonnés, huit retenus.

**Ça règle les deux dénominateurs.** Le doc devait expliquer pourquoi le remplissage se comptait sur les quatorze et la grammaire sur les huit, et pourquoi une part pouvait dépasser 100 % si on se trompait de dénominateur. Ici il n'y a qu'une feuille, sur les quatorze, et rien à réconcilier.

**Répéter et repartir sont un seul cran.** Répéter, c'est repartir avec les mêmes mots : même geste. Les séparer demanderait de dire à partir de quel mot changé on quitte l'un pour l'autre, et aucun défi ne veut peser cette différence-là.

**Le juge souligne, l'app compte** — la même répartition que pour la correction, et dans la même passe : des marques de plus sur une phrase qu'il lit déjà, pas un appel de plus. Un cran par phrase, « il bafouille beaucoup », est écarté pour la raison habituelle : personne ne peut le vérifier, là où « ces quatre mots-là » se regarde.

**C'est jugé et jamais calculé sur une liste de mots.** *euh* et *um* n'ont pas d'autre emploi, mais *I mean*, *like*, *well*, *actually* sont tous de vrais mots — *« I mean what I say »* et *« it was, I mean, hard »* ne diffèrent que par l'emploi. Ce n'est donc pas le mot qui décide, et seul le juge voit l'emploi. La feuille prend par là une **consigne**, ce qu'une feuille calculée ne peut pas prendre : *« aucun mot du genre I mean »* s'écrit telle quelle.

**La voix modèle ne dit que les mots retenus.** Lui faire dire *« It was, like, um, I went to the… »* est exclu : le modèle est ce qu'on donne à imiter, et tout le montage repose sur le fait qu'il est la vérité.

**L'apprenant, lui, s'aligne sur tout ce qu'il a dit.** Sans les hésitations dans le texte, ces bouts d'audio n'ont aucune lettre en face et deviennent des sons en trop, donc des gros ratés par nature : hésiter coûterait une note de prononciation. **On aligne sur les quatorze, on compare sur les huit.**

**Découper l'audio de l'apprenant pour n'en garder que les huit mots est refusé** par un invariant déjà écrit : rien ne se fait à un seul des deux audios (`../reference.md`). Et ça empirerait le problème de couture ci-dessous.

**La couture de l'analyse gagne donc un paramètre** — `examine(said, model, text, kept)`. `text` reste la chaîne affichée, celle où toutes les marques s'indexent, et elle porte maintenant les hésitations ; `kept` dit les morceaux qui comptent, ceux sur lesquels le modèle a été synthétisé. Sans hésitation, `kept` couvre tout le texte et le comportement est celui d'aujourd'hui. C'est le seul endroit où « deux enregistrements de la même phrase » (`analysis/Analysis.kt`) cesse d'être exact : les deux audios ne portent plus le même texte, l'un contenant l'autre.

**Prix assumé : aux coutures, un ou deux sons sont comparés hors de leur contexte.** Le *I* retenu qui suit un *the* abandonné n'a pas devant lui ce que le modèle a devant le sien, et un son est influencé par celui qui le précède. Ça ne se propage pas — une faute ne contamine pas la suite de la phrase (`../analysis.md`, brique 11) — et la redite l'annule, une phrase dite d'un trait n'ayant plus de couture. C'est une raison de plus de faire redire un tour hésitant.

**Le bafouillage ne coupe pas l'analyse du son.** Ce qui la coupe est que la phrase va être *réécrite* : les mots changent, donc l'analyse porterait sur du texte mort. Ici les mots retenus sont les bons, ils ont seulement été dits en trébuchant. L'analyse tourne, au prix des coutures.

**Ce qui est écarté reste affiché, grisé et entre crochets** — `I am [um] twenty five years old`. Les crochets disent ce que la pâleur seule ne dit pas : ce morceau n'est pas dans la phrase. C'est la vérité du calcul — ni analysé, ni lu par la correction, ni à redire — et c'est ce qui explique pourquoi ces lettres n'ont aucune couleur. Rien à régler pour les redites : le fil ne montre que la dernière tentative, donc une redite propre n'affiche aucun crochet.

**Ces crans ne survivent qu'à une reconnaissance verbatim.** Un moteur qui nettoie les *euh* et les bégaiements les rend muets sans jamais le dire, et la fluidité paraîtra excellente. Et depuis qu'on aligne l'apprenant sur tout ce qu'il a dit, un nettoyage silencieux fait aussi rater le placement des sons dans un audio qui, lui, contient les hésitations. Le critère de fidélité verbatim du banc (`../../TODO.md`, chantier 2) ne pesait jusqu'ici que sur la grammaire ; il pèse maintenant sur la mesure des sons.

#### Ce qui ne donne pas de note

**Certaines feuilles se calculent et n'entrent dans aucune note.** Elles existent pour les **conditions**, qui les lisent comme n'importe quelle autre feuille. Ni sensibilité, ni poids : deux champs qui n'ont pas d'objet, plutôt que d'être déclarés inertes.

- **Le tour interrompu** — ce tour a-t-il été envoyé, oui ou non. Un seul élément, vrai ou faux. Deux causes d'interruption et une seule feuille : le silence de plus de x en troisième position de capture, et le plafond de durée du tour, qui existe aux trois positions. Un chiffre à deux valeurs ferait un mauvais membre de moyenne — tout ou rien, aucune gradation, et son poids ferait basculer l'aptitude entière d'un coup.
- **Les comptes** — les redites, les reformulations, le nombre d'écoutes du tour de l'IA. Ce sont des entiers, ils ne se normalisent pas, et surtout ils **montent à chaque tentative** : pesés dans une aptitude qui fait refaire, la note ne peut plus repasser la barre et l'apprenant brûle ses tentatives sans issue. Un défi qui veut la réussite d'emblée l'écrit en **condition** — *« au-delà de deux tentatives, perds une vie »* — et pas en poids.

**La part des tours interrompus d'une séance n'est pas la feuille**, c'est ce qu'une lecture en fait : une feuille se calcule toujours sur un passage, et confondre les deux plans se paie vite.

### L'agrégation se fait une fois, à plat

**Les poids se multiplient en descendant, et la note se calcule une seule fois sur les feuilles réellement présentes.** Les lettres d'aptitude et de passage sont la même formule restreinte à un sous-arbre : des lectures, pas des étapes de calcul.

La raison est qu'une cascade de moyennes redistribue en silence des poids que personne n'a réglés, dès qu'une feuille manque — et il en manque tout le temps : la feuille de l'accent lexical n'est pas branchée, un passage dont la porte des mots s'est fermée n'a aucune mesure de son.

Avec élocution 2 (sons 1, mélodie 1) et correction 1, sur deux passages dont le second a la porte fermée — passage 1 : sons 40, mélodie 80, correction 90 ; passage 2 : correction 50. En cascade, le passage 1 vaut 70, le passage 2 vaut 50 puisque sa moyenne se renormalise sur ce qui reste, et la séance 60. À plat, (2 × 40 + 2 × 80 + 90 + 50) / 6 = 63,3 — les poids se multipliant en descendant, chacune des deux feuilles d'élocution pèse 2 contre 1 à la correction. L'écart n'est pas l'arrondi : dans la cascade, la correction a fini par peser deux tiers de la séance et l'élocution un tiers, l'inverse exact du 2:1 demandé.

**Une feuille absente sort de la somme, elle ne vaut jamais zéro.**

**Chaque feuille pèse par la longueur du passage** — sauf le suivi, qui pèse sur la difficulté du tour de l'IA, la matière qu'il couvre. Proportionnel à la longueur est un peu arbitraire, un contour mélodique étant un contour qu'il soit long ou court ; c'est uniforme, et le tenir sur une longue phrase est effectivement plus de travail. La longueur se compte en **mots retenus** plutôt qu'en sons : un passage dont la porte a coupé l'analyse n'a pas de sons et a toujours des mots.

**Entre feuilles, la moyenne décide seule** : pas de plancher qui plafonnerait la note dès qu'une feuille comptée passe sous la barre. Une bonne feuille peut donc en masquer une mauvaise, et c'est accepté — dans un défi, peu de feuilles comptent, et la sensibilité de chacune dit à quel point elle est facile à tenir.

### Le passage

**Le passage est l'unité de la note : un énoncé et toutes ses redites.** Le mot est neuf parce que « tour » désigne déjà un tour de parole — un enregistrement, un énoncé, une position de capture — et confondre les deux se paierait au premier commit. C'est aussi pourquoi « le tour ne passe pas » ne veut rien dire ici : ce qui passe ou ne passe pas est un passage.

**Quatre états, et il n'y en a pas d'autre.** Un passage est **ouvert** quand rien n'est à refaire, **à reformuler** quand les mots doivent changer, **à redire** quand ils ne changent pas et que c'est la façon de dire qui est reprise, ou **clos**. Et clos de deux façons : **réparé**, la dernière tentative passant la barre, ou **non réparé**, les tentatives s'étant épuisées avant.

**« Raté » n'est pas un état de passage.** C'est un terme d'issue d'activité, à côté de *réussi* et de *la note décide*, et l'employer ici mélange deux plans. Ce qui se dit d'un passage est *non réparé*, et ce n'est pas non plus un état stocké : il se dérive de la note du nœud lu et du compte des tentatives, tous deux déjà là. On stocke ce qui dépend de quelque chose qui ne se retrouvera pas, et ça ne dépend que des lignes.

**La note du passage est celle de la dernière tentative**, et les tentatives permises sont un levier qui peut valoir 1. **Deux leviers en fait, un par compteur** — tant de reformulations, tant de redites — et ils ne se volent rien : un défi qui ne vise que la prononciation garde ses redites intactes quoi qu'il arrive du côté des mots. La dernière est ce qu'on sait dire maintenant. Conséquence à dire à l'apprenant plutôt qu'à lui laisser découvrir : une tentative de trop, après une réussite, peut faire baisser la note.

Les trois autres lectures sont pires. La **première** rend la redite sans effet, donc sans intérêt. La **meilleure** laisse l'obstination atteindre A. La **moyenne des tentatives** fait baisser la note à chaque essai, c'est-à-dire punit exactement le geste que l'app existe pour provoquer.

**C'est la dernière pour toutes les feuilles, sans exception.** Une proposition traînait ici — que la fluidité et la pertinence lisent la **première**, une phrase répétée n'étant plus de la parole spontanée et la parole spontanée étant ce qu'elles mesurent. L'argument est juste et le prix est accepté : deux règles de lecture au lieu d'une, sur un doc qui répète qu'une note ne se lit pas sans sa combinaison, coûtent plus qu'elles ne rapportent.

**Et rien n'est figé par ce choix** : toutes les tentatives restent sous le passage, donc quelle tentative chaque feuille lit est un **calcul** et non un stockage. En changer un jour est une lecture différente des mêmes lignes, jamais une migration — la question n'a donc pas à être tranchée avant quoi que ce soit d'autre.

**Une redite remplace à l'affichage, jamais en base.** Le fil ne montre que la dernière tentative. Mais les tentatives restent toutes sous le passage : une tentative effacée est une mesure perdue, et le compte des essais se lit par les conditions. Ça tranche ce que `../reference.md` laissait ouvert sur le sort de la phrase initiale.

**Et aucun écran ne montre les tentatives précédentes** (tranché le 2026-09-06). Ni le fil, ni le bilan du passage : ce qu'on regarde est toujours la dernière, qui est ce qu'on sait dire maintenant. Elles restent en base pour les mesures et pour un banc, pas pour l'apprenant. Ce qui lui dit que ça va mieux est la marque elle-même, lue sur la prise du moment.

**Deux boutons, et c'est eux qui ferment le passage.** Le gros bouton dit une chose neuve et fait avancer la conversation ; un petit bouton posé sur la phrase la reprend, exactement comme pour la prononciation. **Le passage se ferme à l'appui sur le gros bouton** — l'app n'a rien à deviner de ce qui vient d'être dit, c'est un fait d'interface. Le budget épuisé fait disparaître le petit bouton, et il n'y a pas de reprise qui ne compterait pas : les tentatives plafonnent les reprises, un point c'est tout, et qui veut la liberté ne contraint pas les tentatives.

**Les tentatives s'arrêtent à la clôture du passage** (tranché le 2026-09-06), les deux sortes : ni redire, ni reformuler. Une fois le gros bouton appuyé, le passage ne se retouche plus — il se réécoute et se relit pour toujours. Ce n'est pas une privation, parce que **rien d'autre que ce geste ne ferme un passage** — ni la réponse de l'IA, qui arrive avant en « poursuit », ni le temps qui passe. Qui veut retravailler sa phrase n'a qu'à ne pas passer à la suite, et c'est le meilleur moment pour le faire : la marque est à l'écran, le modèle vient d'être synthétisé.

Ce que ça ferme, et qui n'avait pas de bonne réponse autrement, une raison par sorte de tentative. Une **redite** ajoutée à un passage clos déplacerait sa note, donc celle de la séance, sous des règles de fermeture qui l'ont déjà lue — une vie perdue, un cran de rampe monté. Une **reformulation** ferait pire : elle relance l'échange, donc elle referait cinq tours trop tard une réponse sur laquelle la conversation a déjà bâti, et il faudrait défaire tout ce qui a suivi. Le premier point vaut même là où rien n'est en jeu, donc la règle n'a pas d'exception en conversation libre. Le travail hors du fil a sa place, et c'est un mode à part qui n'écrit rien (`../../TODO.md`).

Conséquence de forme : le petit bouton n'existe que sur le **passage ouvert**, c'est-à-dire le dernier du fil. Les passages plus hauts gardent de quoi écouter, relire et ouvrir leurs mesures, jamais de quoi reprendre. Un budget de tentatives non épuisé qu'on abandonne en fermant est simplement perdu — on était satisfait, c'est le sens du geste.

**En « attend », le gros bouton n'est pas disponible.** Sinon on sortirait d'un passage bloqué en disant simplement autre chose, et les tentatives cesseraient d'être la seule sortie.

**Il revient quand les tentatives s'épuisent**, sinon rien n'avance. Ce qui se dit alors franchement plutôt que de se découvrir à l'usage : **« attend » ne garantit pas la réparation, il garantit qu'on dépense ses tentatives.** Un passage à reformuler peut se clore sans avoir été reformulé.

**Une reformulation relance l'échange, dans les deux avances** (tranché le 2026-09-06). En « attend », l'IA n'a joué qu'un écho et sa réponse se fabrique sur la version corrigée. En « poursuit », elle a parlé : l'app **refait l'appel comme si c'était la première tentative** — le prompt ne porte rien des formulations précédentes, hors les compteurs —, la nouvelle réponse se joue, et la précédente disparaît du fil. Ce qui se paie est un tour entier de chaîne par reformulation, et le fait d'entendre deux réponses à une phrase presque identique ; ce qui s'achète est un fil qui ne se contredit jamais.

**Une redite ne relance rien**, ses mots étant les mêmes : il n'y a rien de neuf à répondre. Elle reste ce qu'elle était, un **exercice** — la note du passage s'améliore, et l'analyse du son se remet à tourner, donc la prononciation se mesure là où un tour mal formé ne l'aurait jamais eue.

**Ça retire au projet le refus général de dédire, et le remplace par quelque chose de plus étroit.** Ce que l'app ne fait toujours pas, c'est jouer une réponse puis la contredire sur la même phrase : dans une tentative, l'appel rend la continuation et l'écho ensemble et l'app en joue **une** (« Deux branches pour la correction »). Ce qu'elle fait maintenant, c'est refaire l'échange quand la phrase à laquelle il répondait n'existe plus. Les deux ne se confondent pas — l'une est un dédit sans cause, l'autre la conséquence d'un fait neuf.

**Et ça ne fait pas doublon avec « attend »**, dont c'était le soupçon. « Attend » **force** la réparation : le gros bouton est indisponible, on ne sort pas du passage sans dépenser ses tentatives, et on n'entend qu'un écho en attendant. « Poursuit » l'**offre** : la conversation avance quoi qu'il arrive, et l'échange ne se corrige que si l'apprenant choisit de reformuler. Aucune position d'`avance.mots` n'exprimait ça.

Le cas de la phrase corrigée qui dit autre chose que ce que l'IA avait compris **disparaît donc pour une reformulation**, puisque la réponse se refait dessus. Il reste pour un passage clos sans réparation : l'app répond à l'intention, se tromper d'intention est le cas déjà écrit de `../reference.md`, et le remède est celui de n'importe quelle conversation — le dire au tour suivant.

**Le nombre de tentatives se compte, et il ne donne pas de note** : réussir du premier coup et réussir au troisième ne sont pas la même chose, mais un compte monte à chaque essai, donc pesé dans une aptitude qui fait refaire il enferme l'apprenant — plus il répare, plus il s'enfonce, sans sortie (« Ce qui ne donne pas de note »). Un défi qui veut la réussite d'emblée l'écrit en **condition**. Reformuler et redire se comptent séparément, du même découpage que les leviers.

### Les étiquettes de l'empan

Le juge marque **par groupe de mots**, une seule fois pour les deux aptitudes, et chaque empan porte **une** étiquette par aptitude. L'app déplie ensuite chaque empan en ses mots, et un mot qui ne porte rien vaut `ok`, qui est un cran comme les autres et non une absence :

- correction — `ne se dit pas`, `mal formé`, `ok`. **Absolues** : aucune consigne ne les déplace.
- pertinence — `à côté`, `plat`, `ok`, `juste`. **Situationnelles**, et la consigne façonne où passe leur frontière.

**Deux échelles, deux feuilles, et pas une feuille par étiquette.** Les étiquettes d'une même échelle se partagent les mêmes mots : ce sont des tranches d'un gâteau, pas des mesures indépendantes. Le calcul est écrit en « La correction » et « La pertinence ».

**Le juge rend le cran, et rien de plus fin.** Sur trois mots, « haut de plat » n'est pas un jugement que quelqu'un pourrait vérifier. La finesse ne se perd pas, elle change d'endroit : elle vient du comptage — combien de mots à quel cran, sur quelle longueur de phrase. Un passage se note donc finement en agrégeant beaucoup de jugements grossiers, exactement comme le fait déjà l'élocution. Coût assumé : un passage de trois mots portant un seul groupe marqué a une note très grossière. C'est une grossièreté vraie, pas une fausse précision.

Ça absorbe un chantier qui traînait à part (`../../TODO.md`, point 6) : le verdict grammatical était un booléen sur le tour entier, ce qui écrasait le fait qu'un passage puisse porter plusieurs fautes et empêchait de marquer la portion concernée. Un cran par groupe de mots règle les deux.

### Ce que ça change au marquage

**La forme dit de quelle échelle il s'agit, la couleur dit de quel côté et à quelle distance du neutre.** C'est l'amendement d'une phrase de `../reference.md`, qui disait que la couleur ne porte que l'alarme : le cran haut porte l'inverse d'une alarme, donc la couleur porte aussi le côté. Le principe change de portée sans se perdre, la forme restant seule à dire l'échelle.

Les deux aptitudes issues du même marquage prennent **deux formes distinctes**, parce qu'elles répondent à deux questions.

**La correction se souligne en vaguelette — jaune pour *mal formé*, rouge pour *ne se dit pas*.** La couleur y porte la distance au neutre, comme partout, et elle dit quelque chose de réel : un tour *mal formé* peut passer la porte au réglage lâche, un tour *ne se dit pas* n'a jamais d'analyse du son quoi qu'on règle. Un soulignement était nécessaire — les sons prennent déjà **un filet sous le mot** pour l'accent lexical, et un trait droit tomberait au même endroit ; la vaguelette se lit comme une erreur de langue sans qu'on ait à l'apprendre.

**La pertinence se prend entre crochets** — rouge pour *à côté*, jaune pour *plat*, vert pour *juste*. Pas de collision avec le jaune de la vaguelette : la forme sépare les deux échelles, une enceinte n'étant pas un soulignement. Elle n'occupe ni l'intérieur des lettres ni la ligne de base, donc un mot peut être pris entre crochets et porter ses lettres teintées sans qu'on confonde les deux échelles. Les crochets tiennent les deux bouts du groupe sans courir le long du texte, ce qu'exige le fait que *juste* se marque aussi : presque tous les groupes portent une étiquette (`pixel-ui.md`). Le vert vit là et nulle part ailleurs, puisque c'est la seule mesure du projet qui ait un bon côté.

### Les deux portes

**Une porte fermée est un échec, une porte ouverte laisse passer.** Fermer, c'est déclarer le passage à refaire — à reformuler pour les mots, à redire pour le son. Ouvrir, c'est ne rien déclarer. Le sens se relit à l'envers une fois sur deux, alors il est écrit ici : ce qui **ferme** est ce qui **ne passe pas**.

**Il y a une porte par sorte de réparation, et elles se lisent à deux moments différents.**

| | quand elle se lit | ce qu'elle produit | ce qu'elle lit |
|---|---|---|---|
| **la porte des mots** | au retour de l'appel | le passage est **à reformuler** | correction, pertinence, compréhension |
| **la porte du son** | à la fin de l'analyse | le passage est **à redire** | élocution, fluidité |

**Ce qui range chaque aptitude d'un côté ou de l'autre est déjà écrit : est-ce que les mots changent.** Appliqué aux cinq, ça ne laisse rien à décider :

| ce qui ne passe pas | ce que ça produit | pourquoi |
|---|---|---|
| **correction** | à reformuler | les mots changent |
| **pertinence** | à reformuler | les mots changent |
| **compréhension** | à reformuler | il faut répondre à autre chose |
| **élocution** | à redire | la façon de dire change |
| **fluidité** | à redire | la phrase est la même, dite autrement |

**Les deux lisent une note, à la barre A–B**, jamais une feuille désignée par une définition. Rien ne s'y perd : un défi qui ne veut viser que la mélodie met un poids sur la mélodie et zéro sur le reste de sa branche, et « élocution sous la barre » *devient* « mélodie sous la barre ». C'est ce que le doc dit déjà des poids — c'est ce qui permet de viser sans ajouter de champ —, et ça évite d'écrire deux fois le même ciblage, une fois dans l'arbre et une fois dans la porte.

**Mais deux faits ferment la porte des mots sans lire aucune note.** Un **tour tronqué** : il n'y a pas de phrase complète, et le laisser passer reviendrait à faire redire un fragment sans droit de le finir. Et un **tour sans aucun mot retenu** : il n'y a pas de phrase du tout, et correction comme pertinence n'ont pas d'éléments, donc il n'y a même pas de note à lire. Dans les deux cas c'est une **absence de matière du côté des mots**, comme `ne se dit pas` en est une du côté du son — donc non négociable, et jamais un verdict de correction : rien de tout ça ne se marque comme une faute de langue. Le passage est **à reformuler** et jamais à redire, redire supposant une phrase à répéter.

**Ce qu'un tour sans mots retenus mesure quand même.** *Le remplissage et les reprises* a tous ses éléments — les mots **prononcés**, pas les retenus — et c'est la feuille écrite pour ce tour-là ; *le plus long silence* a le sien, une durée. *La continuité* et *le débit* sont **absentes** : l'une comparerait à un modèle qu'on n'a pas synthétisé, l'autre diviserait par le temps de parole des mots retenus, c'est-à-dire par zéro. Absentes, elles sortent de la somme et ne valent jamais zéro. Le suivi et le tour interrompu se lisent normalement.

**Le juge ne marque donc pas l'inachèvement.** Il reçoit déjà comment le tour s'est fini, ce qui lui interdit de le compléter (« La capture ») ; la même instruction lui interdit d'en faire une faute. *« I went to the »* est coupé, pas mal formé.

**En « poursuit », l'IA répond au fragment, et une reformulation refait cette réponse** (« Le passage ») : elle a répondu à une phrase dont elle ne pouvait pas connaître la fin, et la phrase finie est une phrase neuve à laquelle elle répond neuf. Qui ne reformule pas garde la réplique faite au fragment, et se faire couper puis reprendre au tour suivant est ce que fait n'importe quelle conversation.

### Ce qui coupe l'analyse du son

**Ce n'est pas une troisième porte, c'est une conséquence des deux premières, plus un cas.**

> L'analyse du son ne tourne pas si la **porte des mots** s'est fermée, ou si un mot est marqué **`ne se dit pas`**.

Le premier cas est la raison d'origine : on ne travaille pas la prononciation d'une phrase qu'on s'apprête à réécrire, elle va disparaître. Il a une conséquence d'ordre qui tombe toute seule — la porte des mots se lit **avant** que l'analyse tourne, donc quand elle se ferme, la porte du son n'a jamais l'occasion de parler.

Le second n'est pas une décision mais **une absence de sol** : une phrase qui n'existe pas dans la langue ne peut pas être synthétisée, et la faire dire au modèle donnerait à imiter une non-phrase. Là où la porte des mots se règle — une activité choisit de quelles aptitudes elle fait refaire —, celui-ci ne se négocie pas.

**`mal formé` n'est pas dans ce cas-là**, et l'y mettre a été essayé. Une seule faute de grammaire dans un tour de trente mots tuerait toute l'analyse du son, et un apprenant en fait à presque chaque tour : la prononciation ne se mesurerait quasiment jamais. Il ferme donc par la **note de correction**, comme les autres, avec le coût que `../reference.md` accepte déjà — au réglage le plus lâche, le modèle dira d'une voix native une tournure qu'on a choisi de ne pas reprendre.

**Ce qui reste au choix d'une activité est de quelles aptitudes elle fait refaire, jamais de quel côté ça tombe.** Le côté est un fait sur la phrase ; rouvrir ce choix rendrait possible de faire redire une phrase qu'on va réécrire, ce que la porte existe pour empêcher. Le choix se pose donc en un levier par aptitude (« Le catalogue des leviers »), et une seule est à *oui* en conversation libre : la correction.

**La porte des mots est fermée dès qu'une aptitude des mots ne passe pas**, plusieurs pouvant le dire à la fois. Elles ne se disputent pas : le registre et la grammaire sont deux façons pour les mots de changer, pas deux critères concurrents.

**Elle lit la même barre que tout le reste** : un passage dont la note ne passe pas est un passage à refaire, donc son son ne s'analyse pas. Ne pas passer force le nouvel essai quand `avance.mots` est sur « attend » ; sinon ça ne force rien, mais l'app tient quand même la phrase pour une phrase à refaire — elle marque, propose de reformuler, et n'analyse pas le son. Une seule barre, deux conséquences selon l'avance.

**Rien de ce qui est jugé ne s'éteint quand elle se ferme.** Toutes les feuilles jugées se calculent, puisque ce sont elles qui décident si elle se ferme — l'éteindre par sa propre décision serait circulaire. Ce qui s'éteint est l'analyse du son, et elle seule.

**Deux choses se ressemblent à l'écran et n'ont rien à voir.**

|  | la porte des mots | `ne se dit pas` |
|---|---|---|
| nature | une décision — on ne travaille pas une phrase qu'on va réécrire | une impossibilité — il n'y a pas de modèle à comparer |
| ce qui est lu | la **note** d'un nœud d'aptitude, à la barre A–B | un **élément** : un seul mot suffit |
| qui décide | l'activité, en disant de quelles aptitudes elle fait refaire | personne, c'est un fait |
| négociable | oui | non |
| ce qui se passe | l'analyse du son ne tourne pas | l'analyse du son n'a pas d'objet |

Dans le premier cas la phrase **pourrait** être synthétisée et on choisit de ne pas le faire ; dans le second elle **ne peut pas** l'être, et la faire dire au modèle donnerait à imiter une non-phrase. Tout le montage repose sur le fait que le modèle est la vérité.

**On ne peut pas retirer l'empan et synthétiser le reste**, comme on retire un *um* du texte de référence. Un bafouillage est **hors** de la phrase ; un empan qui ne se dit pas est **dedans**, et demande à être remplacé. Le retirer laisserait *« I have [ ] years »*, que le modèle dirait *« I have years »* — une autre phrase, cassée elle aussi.

**Et rien d'autre ne s'arrête.** L'IA répond, le fil continue, la marque s'affiche et la reformulation est proposée. Le canal du son reste vide en portant sa raison, pour cette tentative-là seulement : la reformulation est un énoncé neuf, dont le modèle se synthétise et dont les sons s'analysent normalement.

**Point ouvert : ce qui tombe d'un côté ou de l'autre dépend d'une décision du modèle de langue.** *I walk to school yesterday.* Le STT transcrit la bouche, donc *walk* ; le modèle décide l'intention, et avec *yesterday* il peut écrire *walked*. S'il écrit *walked*, la grammaire est correcte, la porte est ouverte, et l'analyse voit un /t/ manquant : faute de prononciation. S'il écrit *walk*, la grammaire est fautive, la porte se ferme, et rien du son ne s'analyse : faute de correction. Le même énoncé, deux traitements opposés, et ce qui tranche n'est contrôlé par personne.

**Et la portée est plus large que cet aiguillage : le juge juge sa propre reconstruction.** `intended` est écrit par le même appel qui pose les empans, donc un modèle qui répare en reconstruisant a effacé la faute **avant** de la juger — et rien en aval ne peut le voir, ni l'écran, ni la note, ni une condition. Chaque note de correction est donc conditionnelle à une fidélité que personne ne vérifie. La fuite est vue une fois, et se lit à l'usage (`../../TODO.md`).

**La marque porte sur un élément, la note agrège sur le passage.** C'est vrai des trois échelles du son comme des crans de correction : l'accent d'un mot est au bon endroit ou pas, mais un passage en contient plusieurs et la note les compte.

## La grille des mesures

Trois conditions pour qu'une feuille existe, et elles tiennent ensemble :

- elle **se calcule toujours**, quelle que soit l'activité — sinon la configuration d'un défi déciderait quels champs existent, et deux séances ne porteraient pas la même matière ;
- elle **veut dire quelque chose sans aucun défi**, dans une conversation ordinaire — sinon c'est un cas particulier déguisé en mesure ;
- quelqu'un pourrait vouloir la **noter seule**.

**Aucune feuille n'a deux sens.** Une feuille dont la direction devrait s'inverser selon l'activité est la mauvaise feuille : le défi « explique ça à un enfant de huit ans » ne demande pas la pertinence du lexique à l'envers, il demande de s'adapter à qui écoute — c'est ce que l'étiquette *à côté* dit déjà, un mot trop savant ne convenant pas à qui écoute.

Aucune branche n'est close.

**Chaque ligne dit ce que la feuille lit, comment un élément prend sa valeur, l'unité de son chiffre, et l'échelle où sa série se place.** Les valeurs des échelles sont des ordres de grandeur, à mesurer.

**Élocution**

| feuille | éléments | valeur d'un élément | chiffre | échelle de la série |
|---|---|---|---|---|
| intelligibilité | les sons | marche à 30 points | part des sons intelligibles | 0,80 → 1,00, tassée en haut |
| proximité | les sons | rampe continue, 0 → 100 points | moyenne | 0,65 → 1,00, étalée |
| mélodie | les syllabes | — | écart moyen, en **demi-tons** | 0 → ~6 *(bas est bon)* |
| accent lexical | les mots de plus d'une syllabe que le modèle accentue nettement | vrai/faux | part des mots bien accentués | 0,70 → 1,00 |

La branche creuse gratuitement : la grille des sons est un inventaire fermé déjà attaché à chaque marque, donc un défi « travaille tes *th* » est un poids posé sur deux colonnes, sans juge et sans liste à inventer. *Candidate, non livrée* : les mouvements de la mélodie (« La mélodie »).

**Correction et pertinence** — deux feuilles sur un seul marquage jugé.

| feuille | éléments | valeur d'un élément | chiffre | échelle de la série |
|---|---|---|---|---|
| correction | les mots retenus | colonne : `ok` / `mal formé` / `ne se dit pas` | moyenne | 0,80 → 1,00, tassée en haut |
| pertinence | les mots retenus | colonne : `juste` / `ok` / `plat` / `à côté` | moyenne | 0,40 → 1,00 |

La correction est un jugement **absolu** contre une norme fixée par l'app, sans découpage et sans consigne ; la pertinence est **situationnelle** et porte tout ce qu'un défi veut exiger. La variété, la rareté, le registre et la complexité de la phrase n'ont pas de cran à eux : elles sont ce que `plat` et `à côté` disent.

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

La dernière **ne survit qu'à une reconnaissance verbatim** : un moteur qui nettoie les *euh* et les bégaiements la rend muette sans jamais le dire, et la fluidité paraîtra excellente. Et depuis qu'on aligne l'apprenant sur tout ce qu'il a dit, un nettoyage silencieux fait aussi rater le placement des sons dans un audio qui, lui, contient les hésitations. Le critère de fidélité verbatim du banc (`../../TODO.md`, chantier 2) ne pesait jusqu'ici que sur la grammaire ; il pèse maintenant sur la mesure des sons.

**Ce qui se calcule sans donner de note** — le tour interrompu, le nombre de redites, de reformulations et d'écoutes. Les conditions les lisent (« Ce qui ne donne pas de note »).

Deux collisions écartées, à ne pas rouvrir. **Le délai avant de parler ne se lit qu'une fois**, par la continuité : en faire aussi une mesure de compréhension compterait deux fois le même silence, avec deux poids qui s'additionnent sans que personne l'ait voulu. Et **l'allongement de syllabe** — « I waaaant », une hésitation portée par la durée — n'a plus de feuille pour le lire depuis que le rythme est retiré ; il reste dehors.

## Ce qui est jugé, ce qui est calculé

**Trois marquages viennent du modèle de langue** — les empans de langue, dont l'app tire la correction et la pertinence ; le bafouillage, dont elle tire le remplissage et les reprises ; et le suivi de ce qui a été dit. Tout le reste se calcule : un écart de répartition, un silence, un débit, un rapport.

**Le juge rend un cran, jamais un pourcentage.** « 72 % de suivi » n'est vérifiable par personne, et la finesse vient du comptage, comme pour les sons. Les échelles qui suivent sont tranchées.

- **empan de langue** — un groupe de mots, une étiquette de correction (`ne se dit pas`, `mal formé`, `ok`) et une de pertinence (`à côté`, `plat`, `ok`, `juste`). Un mot ne porte jamais deux étiquettes de la même échelle (« Les étiquettes de l'empan »).
- **bafouillage** — chaque mot prononcé est `retenu`, `abandonné` ou `remplissage` (« Le remplissage et les reprises »).
- **suivi** — un cran par passage, sur un seul axe, *ce que la réponse prouve d'avoir pris* : `entre les lignes` / `précis` / `en rapport` / `sur le sujet` / `vague` / `à côté` (« La compréhension »).

**Un cran de plus revient du modèle et n'est pas une mesure : la difficulté de son propre tour**, qui sert de poids au suivi (« La compréhension »). Il ne dit rien de l'apprenant, donc il n'a pas de feuille, pas de sensibilité et pas de consigne.

**Seul un marquage jugé prend des consignes** — du texte libre qui entre dans le critère que le juge lit, et non une feuille : deux feuilles se lisent d'un même passage du juge sur les empans, et il n'y a rien à quoi une consigne par feuille s'attacherait. Il y a donc trois endroits où elles s'accrochent, les empans de langue, le bafouillage et le suivi. Sur les empans, une consigne ne touche que la **pertinence** : les crans de correction sont absolus.

**Un marquage en porte plusieurs à la fois, chacune avec sa durée** — éternelle, ou un nombre de passages. Un patch en pose et en retire, donc une consigne arrive en cours de séance : *« au passage 4, tu lui mens sur tes intentions, pour trois passages »*. Une seule place remplacée à chaque fois ne marcherait pas dès que deux consignes n'ont pas la même durée — il faudrait réécrire le texte fusionné à chaque expiration. Le juge les reçoit toutes ensemble, et elles ne peuvent qu'endurcir, chacune comme l'ensemble. **Non mesuré, et impossible à mesurer au banc** : que deux consignes posées ensemble — *« parle au passé »* et *« registre soutenu »* — durcissent bien au lieu de se gêner est une propriété de leur combinaison, et il y en a autant que d'auteurs. C'est l'usage qui le dira.

**Une consigne a trois lecteurs, et c'est ce qui la distingue des autres effets** : l'apprenant, qui doit la lire pour la suivre ; le personnage, qui doit la connaître pour jouer avec ; le juge, qui s'en sert pour noter. Le patch parle à l'app et à l'apprenant, le message au modèle ne va qu'au modèle. **Une consigne est donc toujours affichée**, celle du départ comprise : être noté sur un critère qu'on ne t'a pas dit donne une note qu'on ne peut pas relire. Et le journal des changements dit laquelle était debout à quel passage, sans quoi la note de pertinence des passages 4 à 8 ne se relit plus.

Ce qui est posé ne vaut que pour la **suite** : un passage déjà dit n'est jamais rejugé. Un marquage calculé n'en prend pas : il n'y a pas de phrase à reformuler dans un écart de répartition. **Le poids choisit parmi ce qui est mesuré, la consigne reformule ce qui est jugé.** Un défi « prononce tous les -ed finaux » ne s'écrit donc pas en consigne sur les sons, que l'écart au modèle voit déjà, mais en creusant l'arbre pour ne peser que ces sons-là.

**La consigne fait partie de la situation, pas des réglages.** C'est ce qui la rend compatible avec l'invariance : deux personnes dans la même activité reçoivent le même traitement, et c'est ça que l'invariance protège. Elle n'a ni positions ni phrase lisible par position, donc elle ne va pas dans la liste des leviers — c'est un champ à part, un `brief` par marquage jugé.

**Une consigne n'est qu'additive.** Pour qu'une feuille compte moins ou pas du tout, il y a le poids, et lui seul. Une consigne « ignore les temps » est le cas interdit : elle ferait adoucir un verdict par un texte, ce que le module n'a pas le droit de faire.

**Une consigne est ce qu'un juge lit, et rien d'autre ne porte ce nom.** Deux choses lui ressemblent et n'en sont pas. L'**objectif** — *« obtiens le prix de la chambre »* — n'entre dans le critère d'aucun juge : il se dit dans le `brief`, se constate par le déclencheur du modèle, et se conclut par une fin ; il a deux des trois lecteurs et pas le troisième. Et les **deux phrases d'un patch** annoncent un changement au lieu de poser un critère — ce qui brouille les deux est qu'un patch peut porter une consigne, donc annoncer et installer du même geste. Sans cette ligne, la moitié du `brief` finirait en consigne, et le juge marquerait des choses que personne ne lui demandait.

**Une feuille sous consigne n'est pas vérifiée par le banc**, qui éprouve le critère par défaut. Coût connu, pas un défaut à réparer.

Trois défis que ça écrit sans champ neuf. « 100 % passé » est une consigne sur le marquage, et fait tomber les mots concernés au cran `à côté` : *I'll go there* s'y range, ce qui ne ment pas sur la langue — la phrase est de l'anglais parfait qui ne convient pas ici, et la correction ne la voit pas. Le **mot interdit** est une consigne du même marquage : il n'a pas de feuille à lui, une feuille qui n'existe que si quelqu'un la configure ne passant pas les trois conditions. Et le **registre** — *« tu parles à un client »* — est de la même famille, comme la longueur imposée.

**C'est donc la consigne de pertinence qui porte tout ce qu'un défi exige de la situation**, et il faut le dire là où on serait allé chercher un levier : l'écran custom et le catalogue donné au modèle (« Ce qui fait un levier »).

### Les conditions attachées à une feuille

Ce qui rend une contrainte dure n'est pas la note — noyée dans une moyenne pondérée par la longueur, une occurrence coûte quelques centièmes de lettre — mais une **condition branchée sur la feuille**, qui se déclenche sur-le-champ.

**Une condition est une règle**, pas un mécanisme à part : son déclencheur lit l'arbre des notes, et son effet est un patch, c'est-à-dire n'importe quel levier déplacé.

**C'est donc un terme étroit** — une des six sortes de règle, celle qui lit l'arbre — et il faut s'y tenir. Le doc l'emploie ailleurs au sens large, « les conditions de fin », qui sont des règles et pas des conditions. Confondre les deux fait affirmer d'une règle quelconque ce qui n'est vrai que de celle-ci, à commencer par « elle ne lit jamais autre chose que l'arbre ». Il n'y a donc pas d'effets à énumérer — retirer une vie est une position de levier comme une autre, et une vie perdue et une fin sont la même chose vue deux fois, la fin étant zéro vie.

**Perdre une vie n'est jamais automatique.** Un passage non réparé ne coûte rien par soi-même : ça coûte une vie parce qu'une définition a écrit la règle qui le dit. Une autre en demandera trois, une autre rien. Et les vies n'existent que là où il y a un enjeu — une conversation libre n'en a pas, donc un passage non réparé y est un fait enregistré et rien d'autre.

Une condition **lit le résultat d'un nœud, elle ne change pas ce qu'il mesure**. Elle se branche donc aussi bien sur une feuille calculée : un silence de plus de cinq secondes coûte une vie.

**Trois formes, et il n'y en a pas d'autre.** Une condition lit :

- **un élément** — au moins un élément atteint ou dépasse une valeur : un silence de plus de cinq secondes, un mot au cran `ne se dit pas`, un son au-delà de la ligne du gros raté ;
- **le chiffre de la feuille** — plus de 30 % du tour passé en silence ;
- **la note**, à la lettre que la condition nomme, **dans les deux sens** — tombé en D, ou atteint A.

**Ce que la première forme peut dire dépend de la nature de l'élément**, et il n'y a rien de plus à déclarer, la feuille le disant déjà : un élément **vrai/faux** ou porteur d'un **cran** ne laisse rien à choisir — « au moins un mot `ne se dit pas` », sans valeur ; un élément qui porte une **quantité** prend un seuil — « un silence de plus de cinq secondes », « un son au-delà de la ligne du gros raté ».

**La lecture par élément n'a pas de dénominateur, et c'est voulu** : c'est le seul endroit du projet où un fait absolu se lit. « Au moins un mot `à côté` » se déclenche pareil sur un passage de trois mots et sur un de trente ; qui veut la proportion lit le **chiffre**. C'est la règle déjà écrite — jamais un nombre d'occurrences dans une note, et le nombre ne disparaît pas pour autant, il vit dans la condition.

**Sur un marquage, l'élément est le mot et jamais l'empan.** Un empan n'apporte rien de plus pour l'existence — il a toujours au moins un mot, donc « au moins un empan » et « au moins un mot » se déclenchent dans les mêmes cas — et sa **longueur** n'est pas lisible, parce qu'elle dépend de l'habitude de coupe du juge : *to the market* fait un empan de trois mots ou trois empans d'un mot, et une condition qui lirait la longueur serait vraie dans un cas et fausse dans l'autre.

**Une condition sur un cran fréquent se déclenche presque toujours.** « Au moins un mot `plat` » est vrai à chaque passage ou presque, donc elle ne dit rien. C'est un conseil à qui écrit un défi, pas un interdit : un défi très strict peut vouloir exactement ça.

Les deux premières formes portent leur seuil et ne bougent pas quand le défi durcit. La troisième **suit la sensibilité**, qui est précisément ce qui déplace les bornes A–E : monter la sévérité rend la condition plus fréquente sans qu'on la touche, et c'est un service — un défi dit « plus dur » d'un seul geste. Les deux portes sont de cette troisième forme, et quels nœuds d'aptitude elles lisent vient de ce que le défi fait refaire. Ce qui coupe l'analyse du son sur un mot `ne se dit pas`, en revanche, est de la première forme.

**La barre A–B ne borne pas les conditions, et l'y avoir enfermées était une erreur de portée.** L'invariant qui fixe la barre a été écrit pour la **lecture d'un résultat** — l'activité est-elle réussie, le niveau suivant s'ouvre-t-il —, où deux boutons qui bougent rendraient le résultat illisible. Une condition n'est pas un résultat : *« quand il tombe en D, le barman fronce les sourcils »* ne se compare à rien, ne débloque rien, n'entre dans aucun classement. Son seuil est de la même famille que les cinq secondes de silence, une valeur que l'auteur choisit. **Ce qui reste à la barre est le verdict** : ce qui décide qu'un passage se refait ou qu'une activité est réussie. Au-delà, la barre est une convention plutôt qu'une règle.

**Et lire les deux moitiés de l'échelle est ce qui rend une récompense écrivable.** « Sous la barre » n'existait que du côté de l'échec, donc aucun défi ne pouvait réagir à une réussite — alors que tout le reste était déjà là : un patch déplace un levier dans les deux sens, donc gagner une vie est un patch ; une fin porte l'issue *réussi* ; un message fait sourire le barman. Il ne manquait que de pouvoir lire la bonne moitié.

**Une condition lit n'importe quel nœud de l'arbre, pas seulement une feuille.** Une note existe à chaque nœud, et rien n'oblige un nœud de premier niveau à être une aptitude, donc lire *« la compréhension est en E »* est la même opération que lire une feuille. La seule différence est étroite et se déclare : un nœud n'a **ni éléments ni chiffre brut**, son nombre étant une moyenne pondérée de ses enfants, dans aucune unité. **Sur un nœud, la note est donc la seule des trois lectures disponible** ; les trois ne le sont que sur une feuille.

**Une condition ne lit jamais autre chose que l'arbre.** Un fait porté par le tour — sa position de capture, comment il s'est fini — n'est pas lisible tel quel : il faut la feuille qui le lit, et c'est ce qui permet d'écrire un défi contre le catalogue plutôt que contre le code. La feuille du tour interrompu existe pour cette raison.

**Le poids gouverne la note, la condition lit sans passer par lui.** Une feuille à 0 n'est pas éteinte : elle se calcule, et une condition la lit. Certaines feuilles ne donnent d'ailleurs **aucune note** et n'existent que pour ça — le tour interrompu et les comptes (« Ce qui ne donne pas de note »).

**Une condition lit un passage et se déclenche sur-le-champ ; l'accumulation vit dans l'effet, pas dans la lecture.** « Un tour interrompu coûte une vie » n'a besoin de compter jusqu'à trois nulle part : au troisième, le compteur de vies est à zéro. Ce qui a besoin de voir la séance entière est d'une autre nature — son issue (« La fin d'une séance et son issue »).

**Rien ne s'écrit par feuille pour autant.** Une feuille déclare ce que ses éléments sont et l'unité de son chiffre, et les deux diffèrent presque toujours : la continuité rend des points de pourcentage et ses éléments sont du temps ; la correction rend une moyenne et ses éléments sont des mots portant un cran. Une condition se dit alors partout pareil — quel nœud, laquelle des formes disponibles sur lui, et une valeur dans l'unité concernée.

## Le blocage

**Bloquer, c'est une règle dont l'effet est que le passage ne se ferme pas.** Pas un mécanisme neuf, et donc branchable sur n'importe quelle feuille : un défi peut bloquer sur la prononciation comme sur la correction.

**« Ne pas passer » se dit de trois choses**, à ne pas confondre. Une **feuille** ne passe pas quand sa note est sous la barre A–B : c'est une lecture, il ne s'ensuit rien. Un **passage** ne passe pas quand une aptitude dont l'activité fait refaire tombe sous la barre — la correction seule en conversation libre. Une **activité** ne passe pas quand elle se termine sans être réussie : une fin sèche en raté, ou une fin ordinaire dont la note ne passe pas (« La fin d'une séance et son issue »).

Concrètement, trois choses : la réponse **n'ajoute rien** — elle ne répond pas au fond et ne pose pas de question neuve ; le passage **reste ouvert**, ce qu'on attend ensuite étant une reprise de la même chose ; et rien n'avance tant qu'il ne se ferme pas.

**Ce n'est pas l'interruption que le projet refuse.** On ne part pas travailler ailleurs, le sujet ne change pas, l'écran ne change pas : c'est la conversation qui s'arrête sur une phrase. Le geste fondateur reste tenu.

**Deux axes, pas un.** L'**écho** — absent, indication indirecte, reprise explicite — est le levier déjà écrit. L'**avance** est le second : la réponse poursuit, ou elle attend. C'est un levier comme un autre, disponible partout — une conversation libre peut attendre sur une phrase, un défi peut poursuivre. Le geste fondateur du projet est la combinaison (indirect, poursuit) : *« Ah, you're 25! And where... »*. **Toutes les combinaisons s'offrent**, certaines étant seulement plus austères. Ce qui est garanti est ailleurs : **rien n'attend jamais sans qu'une raison soit visible**, et ce qui la porte est la marque, toujours là et invariante, plus la notification quand c'est le son qui bloque. Jamais l'écho, dont la position reste donc libre.

Ça éclaire ce que la porte des mots est vraiment, et surtout ce qu'elle n'est pas. **Elle coupe l'analyse du son, rien d'autre** : « la porte se ferme » veut dire « on ne mesure pas la prononciation de cette phrase-là », jamais « la conversation s'arrête ». Trois choses se déclenchent autour d'un passage à refaire, et elles n'ont ni la même portée ni le même décideur :

| ce qui se passe | portée | qui décide |
|---|---|---|
| l'analyse du son ne tourne pas | la prononciation de cette tentative | automatique, dès que le passage est déclaré à reformuler |
| une reprise est proposée | l'écran | automatique |
| la conversation attend | le fil — le passage ne se ferme pas | `avance.mots` ou `avance.son`, selon le côté |

**Les deux dernières sont indépendantes**, et ça se voit dans les deux sens. Porte des mots fermée sans attendre est la conversation libre ordinaire : la phrase est marquée, ses sons ne sont pas analysés, l'IA répond et le fil continue. Attendre sans fermer la porte des mots est le blocage sur la prononciation : les mots ne changent pas, donc l'analyse a tourné — c'est même elle qui a rendu le verdict — et c'est la réponse qui est retenue.

**Et *ne se dit pas* se lit sur les deux plans sans les confondre.** L'absence d'analyse y est un fait, non négociable, puisqu'il n'y a pas de modèle à comparer. Refuser de continuer, en revanche, reste une décision : un défi l'écrit avec une condition sur l'élément, dont le patch met l'avance sur « attend ». En conversation libre, rien ne bloque — le geste fondateur du projet est que rien n'interrompt.

### Deux branches pour la correction, une notification pour le son

**Le verdict de correction naît dans le même appel que la réponse ; le verdict de son naît après**, puisque l'analyse a besoin d'`intended`, qui vient du modèle. Les deux blocages n'ont donc pas les mêmes moyens, et ce n'est pas une incohérence : c'est ce que chaque verdict rend possible au moment où il tombe.

**Correction** : l'appel rend **une réponse plus une ligne courte** — la continuation, et l'écho de reprise (« Ah, you mean you ARE 25 »). L'app calcule la note dès le retour et joue l'une ou l'autre. Trois conséquences : **rien n'est jamais contredit dans une tentative**, une seule des deux étant jouée — ce qui se refait quand une reformulation arrive est un autre geste, sur un fait neuf (« Le passage ») ; c'est littéralement « l'IA joue, l'app décide », puisqu'elle fournit la matière des deux issues sans trancher ; et la ligne courte **n'est produite que si l'IA a marqué quelque chose**, ce qui la rend gratuite sur un passage propre. Un champ de retour de plus, une chaîne, sans énumération à valider — le moins cher des contrats.

**Prononciation** : quand l'écart au modèle arrive, l'appel est fini et la réponse existe. Rien ne peut fournir un écho en personnage sans un second appel, écarté pour la latence. La réponse se joue donc, et **c'est le passage qui reste ouvert** ; une notification dit de reprendre, en donnant à **écouter** le modèle, qui est synthétisé de toute façon, et non en expliquant, le remède d'une faute sonore n'ayant jamais été une consigne écrite.

**Elle ne nomme rien.** La porte du son étant câblée sur l'élocution et la fluidité seules, ce qu'elle nommerait serait le même mot à chaque fois, donc une constante, donc rien. Ce qui montre où porter l'attention est déjà à l'écran — les marques, invariantes, et la forme de chacune disant son échelle. Et nommer la pire des marques serait une élection, que le projet ne fait nulle part.

### La sortie d'un passage bloqué

**Les tentatives s'épuisent, et c'est la seule sortie.** Pas de geste d'abandon à part : les tentatives permises sont déjà un levier, donc la sortie est déjà réglable, et un deuxième mécanisme ne ferait que doubler celui-là.

**En « poursuit », les règles se déclenchent à l'appui sur le gros bouton**, qui ferme le passage et commence le tour suivant du même geste. Elles tombent donc juste avant la réponse de l'IA à ce tour-là, ce que le doc exige d'une règle. Un tour de retard, jamais au mauvais moment.

**Deux portes, dans cet ordre.** Celle des mots d'abord — on n'analyse pas le son d'une phrase dont les mots vont changer — puis celle du son. Les deux moments où l'app peut agir sont exactement ceux-là : au retour de l'appel, elle connaît les verdicts des mots et tout ce qui se calcule sur l'audio et le texte ; à la fin de l'analyse, elle connaît le reste. Si les reformulations s'épuisent, le passage se clôt non réparé et les redites ne sont jamais entamées.

L'IA repart alors du sens qu'elle avait compris — elle l'a toujours compris — et le passage se clôt **non réparé**. C'est ce qui alimente les règles : perdre une vie, durcir, ouvrir ou non le niveau suivant.

**Et elle n'a rien à refabriquer pour ça.** L'appel avait rendu la continuation et l'écho de reprise ensemble ; en « attend », seul l'écho avait été joué. Quand les tentatives s'épuisent, l'app joue la continuation qu'elle tenait. Aucun second appel, rien de rétracté — c'est ce qui rend cette sortie gratuite.

### Le déroulé d'un passage

Ce que les sections précédentes disent chacune de son côté, mis bout à bout. Rien de neuf ici, sauf de voir dans quel ordre ça tombe.

```
PASSAGE ouvert.  tentative ← 1

boucle sur les tentatives :

  - le micro s'arme, après `preparation` s'il y en a (sauf en capture au doigt)
  - l'apprenant parle → un TOUR
  - le tour part : gros bouton, seuil de silence, ou plafond de durée
  - reconnaissance → transcription
  - UN SEUL APPEL au modèle
      → intended, marquage des empans, suivi, continuation,
        difficulté du tour, écho de reprise si quelque chose est marqué,
        choix dans le menu si une règle en offre un

  ── fin de tentative, premier temps : au retour de l'appel ──
  - l'app calcule les feuilles jugées et celles qui se lisent sur l'audio
    et le texte : correction, pertinence, suivi, remplissage et reprises,
    continuité, plus long silence, débit, tour interrompu
  - les règles de ce moment qui lisent ces feuilles se déclenchent
  - PORTE DES MOTS : à reformuler si une aptitude des mots ne passe pas
  - l'analyse du son NE TOURNE PAS si le passage est à reformuler,
    ou si un mot est marqué `ne se dit pas`
  - ce que l'app joue : la continuation, ou l'écho si le passage est
    à reformuler et que `avance.mots` est sur « attend »

  ── second temps : à la fin de l'analyse, si elle a tourné ──
  - l'app calcule les feuilles du son : intelligibilité, proximité,
    mélodie, accent lexical
  - les règles de ce moment qui lisent ces feuilles se déclenchent
  - À REDIRE si une aptitude de la façon de dire ne passe pas
    → notification : écoute le modèle et redis

  ── sortie de tentative ──
  - à refaire, et le compteur correspondant n'est pas épuisé
      → petit bouton, tentative++
  - à reformuler et reformulations épuisées → l'app joue la continuation
      qu'elle tenait ; le gros bouton revient ; les redites ne sont
      jamais entamées ; le passage se clora NON RÉPARÉ
  - à redire et redites épuisées → le gros bouton revient ;
      le passage se clora NON RÉPARÉ
  - rien à refaire → le gros bouton est disponible ; RÉPARÉ

FERMETURE, au gros bouton :
  - note du passage ← celle de la dernière tentative
  - moment « fermeture du passage » : patchs, rampe, vies, fin de séance
  - l'IA répondra au tour suivant en connaissant déjà ce qui a changé
```

**Il y a donc deux épuisements et non un**, celui des reformulations et celui des redites, et ils ne tombent pas au même endroit : le premier avant que l'analyse du son ait tourné, le second après. Le doc n'écrivait que le premier.

## La fin d'une séance et son issue

**Un effet de règle est un patch, la fin, ou un message au modèle.** Trois, et la liste est fermée. Finir ne peut pas être un levier : il faudrait un côté dur, et finir n'est ni plus dur ni plus facile que continuer — c'est une porte qu'on franchit une fois, pas une position. Et le faire passer par les vies obligerait un défi « dix passages et c'est fini » à s'inventer une vie unique, donc à afficher un cœur à quelqu'un qui n'en a pas, alors que les vies n'existent que là où il y a un enjeu.

**Les vies à zéro mettent fin, et c'est une propriété déclarée du levier, pas une règle.** Elle s'évalue **une fois par moment, après que toutes les vagues de règles ont fini**, sur l'état stabilisé (« Comment les règles d'un même moment se résolvent »). C'est ce qui laisse une règle remplir les vies dans le même moment sans que la fin tombe — la mort scriptée qu'on traverse.

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
- *Leviers* : la **longueur** du tour de l'IA et sa **complexité** — vocabulaire, structure — deux leviers, tous deux *demandés* ; le tour de l'IA en quatre marches, **texte affiché**, **texte brouillé** — le support de la phrase sans son contenu, sa longueur, sa ponctuation, le rythme de ses ascendantes (`pixel-ui.md`) —, **seulement qui parle**, **rien** ; la **réécoute**, un nombre dont zéro veut dire interdite ; le **bruit** et le **filtre** du canal, jusqu'à simuler un mauvais réseau qui coupe des mots — deux leviers et non un (« Le catalogue des leviers »).

La marche du milieu prend son sens à plusieurs personnages : on sait que c'est Vera qui parle sans lire ce qu'elle dit.

Les deux s'appliquent **à la lecture**, jamais au rendu mis en cache : le même fichier sert d'étalon à la mesure, et le bruiter fausserait l'écart.

**Correction**
- *Leviers* : **l'écho** de la faute dans la réponse de l'IA, en trois niveaux — absent (erreur ignorée, réponse normale), indication indirecte (la reprise dans sa réponse, ce que le code incite aujourd'hui), ou reformulation explicite dite comme un coach reprend son élève ; l'explication de la faute en notification, à trois niveaux — aucune, la règle seule, ou la règle plus la phrase correcte.

Le marquage a quitté cette liste : il est invariant, donc il n'est plus un levier. Les deux qui restent sont bien **deux** et non deux crans d'un seul, parce que **où** et **quoi** ne sont pas deux quantités de la même information : on peut donner l'un sans l'autre, dans les deux sens. C'est la distinction que l'analyse fait déjà pour le son, où le marquage dit où et nommer le son produit est un enrichissement séparé. Et tout en haut, ils se recouvrent : une reformulation explicite de l'IA donne déjà la phrase correcte à voix haute, que l'explication redonnerait par écrit.

**Fluidité**
- *Leviers* : la capture, en trois positions (« La capture ») ; la **préparation**, le temps entre la fin de la réponse de l'IA et l'armement du micro.

**Pertinence**
- *Leviers* : aucun qui la presse, et ce n'est pas un trou. Le registre, la longueur imposée, le mot interdit n'ont pas de côté dur, donc ils ne sont pas des leviers (« Ce qui fait un levier ») : ils s'écrivent dans la **consigne**, qui est justement ce que cette aptitude porte. C'est là qu'elle se règle. Il lui reste ce que toute feuille a, sa sensibilité et son poids.

**Et chaque aptitude porte en plus son `fait-refaire`**, qui dit si ne pas passer met le passage à refaire ; les deux `avance` disent, à côté, si la conversation attend pendant ce temps (« Le catalogue des leviers »). Ce ne sont pas des leviers de pression sur une aptitude en particulier — ils gouvernent le fil.

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

**Comment un personnage obtient sa voix est tranché, et se lit dans `character-voices.md`.** Une voix est un fournisseur et un slug, et il y en a deux sortes. Les **génériques** — quatre en première implémentation, deux d'homme et deux de femme, choisies par l'apprenant chez son fournisseur — sont **interchangeables** : une définition générique demande *une générique*, éventuellement d'un genre, et n'en nomme aucune. Les **spécifiques** viennent du projet et se nomment par leur slug. Deux modes coexistent, l'apprenant qui met sa clé et n'a que le générique, ou celui qui achète des crédits et a tout. **Ce n'est pas un levier** — entre deux voix il n'y a ni dur ni facile (« Ce qui fait un levier »).

C'est ce qui tient l'invariant sans machinerie : une définition générique ne portant aucun nom, il n'y a rien qui puisse mourir avec une clé, et aucun catalogue à interroger. Et l'invariant **ne s'applique pas** aux voix spécifiques : son problème était le roulement d'un tiers, et un slug sur un fournisseur que le projet tient est stable par construction.

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

1. **Ouverture à la main, avec pause, envoi manuel.** On appuie pour parler, on rappuie pour mettre en pause et réfléchir, on rappuie pour continuer, on envoie quand c'est dit. Aucune mesure de silence n'est possible — entre deux segments, l'écart mesure le pouce.
2. **Armement automatique, sans pause, envoi manuel.** Le micro s'ouvre dès que l'IA a fini et reste ouvert jusqu'à l'envoi. Le **délai avant de parler** et les **silences intérieurs** deviennent mesurables, ce qui est exactement ce que retirer la pause achète.
3. **Armement automatique, sans pause, envoi au clic ou sur un silence de plus de x**, silence du début compris. Le clic reste le geste normal ; un tour que personne n'envoie est **interrompu**, et c'est ce que cette position demande, de la réactivité. Aucune mesure neuve ne s'y ajoute, et aucun silence n'y dépasse x, puisqu'à x le tour est déjà parti.

**Tout s'ouvre et s'envoie du même geste aux trois positions : un appui** (tranché le 2026-09-06). La première position se tenait au doigt maintenu, ce qui la faisait diverger des deux autres sur le geste et non sur ce qu'elle mesure — et obligeait, chaque fois qu'un bouton devait servir dans les deux régimes, à lui donner deux comportements que rien à l'écran n'annonçait. La bascule garde les segments à l'identique, puisque la position 1 séparait déjà des segments de parole par des pauses commandées au pouce ; elle change qui les délimite, pas ce qu'ils sont.

**Ce qui distingue alors les positions n'est plus le geste mais deux faits** : qui ouvre le micro, et si la pause existe. La pause est ce qui interdit de lire les silences, donc elle vit là où ils ne se lisent pas, et nulle part ailleurs.

Un tour interrompu est **tronqué et envoyé tel quel** : ce qui restait à dire n'est jamais capté, le micro ne se rouvrant qu'après la réponse de l'IA. Il n'est pas coupé en deux tours.

**Chaque tour porte comment il s'est fini** — envoyé, ou interrompu, et par laquelle des deux horloges. Ce n'est pas une mesure mais un fait sur l'enregistrement, à côté de la position de capture. Deux lecteurs : le modèle de langue, à qui il interdit de compléter une phrase inachevée (`../reference.md`), et la feuille du tour interrompu.

**Les deux décomptes sont visibles, toujours** — celui du silence de x et celui de la durée maximale du tour. Deux temps qui s'épuisent, montrés de la même façon. Ce n'est pas un levier.

Le micro **ne s'arme jamais avant la fin de la réponse de l'IA**. Un symbole est visible dès que ça enregistre : il n'informe pas seulement, il fait partie de la pression — savoir que ça tourne change la façon dont on parle.

**Et il ne s'arme jamais tout seul tant qu'un passage attend une reprise** (tranché le 2026-09-06). Ce que ça répare : le passage se ferme à l'appui sur le gros bouton, *l'app n'ayant rien à deviner de ce qui vient d'être dit, c'est un fait d'interface* — or l'armement automatique supprime l'appui, donc le fait. En « poursuit », où le gros bouton est disponible, l'app recevrait une prise sans savoir si c'est la tentative suivante ou un tour neuf qui clôt le passage non réparé, et rien ne trancherait, les tentatives pouvant être sans maximum. Suspendre l'armement recrée l'instant de la décision, et seulement là où il avait disparu : en « attend », le gros bouton étant indisponible, tout ce qui se dit est déjà une tentative et rien n'est ambigu.

Ce qui se voit alors : le flux s'arrête, la ligne d'état porte les deux issues et le compte restant — *reformule-la, ou lance un tour neuf ; il te reste deux reformulations* —, le petit bouton reprend, le gros passe. Le silence du micro est le signal, et il n'a pas besoin d'être doublé par une fenêtre à écarter.

**Une prise se jette avant d'être envoyée, et c'est un levier** — `jeter-la-prise`, tranché le 2026-09-06. Le geste existe dans l'app depuis le début : on parle, on relâche, et on jette au lieu d'envoyer. Rien n'est parti, rien n'a été mesuré, aucune tentative n'est dépensée. Sans lui, la seule sortie d'une phrase ratée serait de l'envoyer, ce qui dépense une tentative pour un raclement de gorge. Mais librement offert, il rend le compte des tentatives contournable : dans un défi qui n'en donne qu'une, on recommence dix fois en jetant chaque prise, et la tentative reste intacte. Un défi doit donc pouvoir le fermer, ce qui est exactement la définition d'un levier — il y a un côté facile.

Il est **sans objet en capture armée avec envoi au silence**, la seule position où une horloge envoie aussi : le silence pendant lequel on hésite à jeter est ce qui envoie la prise, donc le bouton y serait une course contre la pendule, perdue par qui réfléchit. Ce qui sépare cette position des deux autres n'est pas l'envoi au clic, qui y reste le geste normal, mais le fait que l'apprenant ne soit plus seul à pouvoir envoyer.

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

- **La série de chaque feuille** — la liste ordonnée de valeurs, dans son unité, dont les fenêtres sont les positions de sensibilité. La forme est écrite et l'échelle de chacune est posée (« La grille des mesures ») ; les valeurs, non. C'est ce qui rend les feuilles comparables entre elles, et ça se trouve au **banc de calibration**, feuille par feuille, sur des prises enregistrées selon des critères. **Combien de positions** s'y décide aussi et pas avant — une position de plus ne coûte plus qu'un nombre, donc la question a cessé d'être un arbitrage.
- **Les colonnes** — ce que vaut chaque cran, pour les trois marquages jugés. Chacune s'ancre par une phrase : *un passage entièrement fait de ça vaudrait…*
- **Le seuil de silence à 200 ms**, partagé par la continuité et par le débit, à placer au-dessus de la plus longue fermeture d'occlusive.
- **La fréquence des crans hauts** — `juste` et `entre les lignes`. Un juge qui en donne un passage sur deux les rend décoratifs.
- **La symétrie du débit** : trop lent et trop rapide sont-ils également gênants. Sinon la série devient asymétrique, deux listes au lieu d'une.
- **La frontière d'`intended`**, et le cas de l'apprenant qui ne parle pas anglais du tout. Que reconstruit-il quand la bouche a dit du français ? La machinerie l'absorbe à moitié — tout se marquerait `ne se dit pas`, ce qui coupe l'analyse du son par absence de sol —, mais rien ne dit ce que le champ rend ni si le juge doit reconnaître qu'il ne lit pas de l'anglais. Se tranche en écrivant le prompt, ce qui est une étape du chantier d'implémentation.
- **La norme de la correction**, qui n'est écrite nulle part — d'où *I'm doing good*. Elle vit dans le contexte permanent du prompt, et deux choses au moins s'y tranchent : la **variété** (au plus simple, celle de l'accent choisi) et le fait que **l'oral n'est pas de l'écrit**.
- **L'arbre des poids de la conversation libre**, à écrire : c'est une définition livrée comme les autres.
- **Les valeurs de la mélodie**, si la feuille des mouvements s'ouvre : la bande de bruit sous laquelle un mouvement n'en est pas un, et ce qui compte comme mouvement fait. Plus l'extension de la brique 10 de la région voisée finale à toute la phrase, qu'aucune étiquette du banc ne couvre.
- **Ce que le prompt fait tenir des frontières qu'il ne prouve pas.** La forme est écrite (« Ce que le modèle reçoit ») ; ce qui reste est de le vérifier au banc, sur les deux fuites nommées — la persona qui atteindrait `intended`, et un historique qui déplacerait le marquage.
- **Le rythme de montée de la rampe** d'arcade, maintenant qu'on sait qu'elle monte des crans entiers — et **ce que ces crans font varier**, les poids étant constants pendant une partie.
- **Le degré de détail de l'écran d'avant-partie** (« Ce qui fait un levier »), et l'**écran custom** : poids et sensibilités s'y règlent en positions crantées, pas en nombres.
- **Ce qu'on fait des réponses quand elles s'accumulent** — à la scène 12, onze scènes ont répondu. Les questions déclarées bornent, pas toujours assez (« L'histoire »).
- **La part du prompt qui fabrique les occasions.** Un curseur haut ne sert à rien si la conversation ne place jamais l'apprenant devant la difficulté qu'il a demandée. Reste à partager entre ce qui passe par la parole de l'IA et ce qui passerait par une consigne hors parole (« notification » — autre nom à trouver).
- **Les phrases lisibles de chaque position de levier**, qu'exigent la notification d'arcade et l'écran d'avant-partie. Le reste de la déclaration est écrit (« Le catalogue des leviers »), qui reste un brouillon et dont aucune liste n'est close.
- **Comment le bruit et le filtre du canal se standardisent**, pour qu'une même position vaille une difficulté comparable. Le rapport signal/bruit standardise le niveau et non la difficulté, une parole concurrente étant plus dure que du bruit rose au même rapport ; et le filtre demande une liste fermée d'effets nommés dont l'encodage de l'intensité n'est pas décidé (« Le catalogue des leviers »).
- **Quels préréglages chaque mode offre**, et ce que chacun pose. Le curseur d'aptitude n'en est qu'un.
- **Le score** : propre à l'arcade ou pas, et à quoi ressemble son écran.
- **L'écran de résultat**, distinct du score. C'est lui qui doit porter la **combinaison qui a produit la note** — les réglages, les poids, les sensibilités, les consignes en vigueur —, sans quoi la phrase qui absout la moitié des choix de ce doc, « une note ne se lit jamais sans la combinaison qui l'a produite », n'a nulle part où se tenir. L'écran d'avant-partie est spécifié et se génère tout seul ; celui-là ne l'est pas.
- **Garder le nom du préréglage** d'une séance réglée à la main. Aucun lecteur n'en a besoin aujourd'hui — l'origine suffit là où ça compte — donc pas de champ pour l'instant.
- **L'écran par lequel on entre en conversation libre.** Son `brief` vient de l'apprenant — saisi, préréglé ou suggéré par le système —, ce qui suppose un écran avant le lancement dont rien n'est décidé : ce qu'il propose, s'il garde les derniers pitchs, et si « lancer sans rien écrire » est un geste à part.
- **Le déroulé de chaque module**, et son écran. Le cadre est commun — l'activité, ses champs, ses statuts, son résultat — le déroulé ne l'est pas.
- **Les déclencheurs de suggestion** pendant une conversation.
- **La purge**, et la durée de vie des audios.
- **Ce que chaque mesure lit** de l'audio, segment par segment.
- **La liste de ce qui doit être agrégeable.**
- **Qui valide une définition.** Le doc répète qu'une chose « se voit à l'écriture, sans exécuter » — deux patchs d'une même vague sur la même clé, une phrase de mise en scène manquante, une série à bords impossibles, une direction narrative qui contredit la mécanique — et rien ne dit qui regarde. Aucun validateur, aucun écran d'auteur n'est spécifié, ni quand il passerait : à l'écriture, au chargement, ou jamais. Sans lui, toutes ces vérifications « à l'écriture » se font à l'exécution, sur un apprenant.
