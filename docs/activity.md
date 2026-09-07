# Le modèle d'activité

Comment une séance est décrite, réglée, jouée et close. Ce que l'énoncé et l'activité sont, et ce qui se stocke, est dans `reference.md` ; ce qui se mesure et comment ça devient une note est dans `measures.md`.

**Un nom d'usage se pose sur des axes, il ne les remplace jamais.** Le curseur d'aptitude est un nom sur un jeu de positions de leviers ; « arcade » est un nom sur une combinaison d'axes ; une brique de scène aussi. À chaque fois, prendre le nom pour le modèle obligeait à décrire deux fois la même séance, et rendait impossible une combinaison légitime — une campagne ne pouvait pas contenir un niveau d'arcade, les deux étant des valeurs du même champ. C'est la règle à appliquer au prochain paquet qui se présentera sous un nom rond.

## La définition, l'exécution, le bloc

Une **définition** est une activité écrite d'avance, et **c'est de la donnée, pas du code**. On la rejoue autant qu'on veut. Elle porte : une **identité** et une **version** ; son **contenu** — ce qui ouvre la séance, le `brief`, les personnages ; ses **consignes**, au plus une par marquage jugé ; ses **positions de leviers de départ**, sensibilités comprises ; son **arbre des poids** ; ses **règles** ; ses **questions**, auxquelles le modèle répond en fin de séance.

**Trois champs d'affichage s'y ajoutent, et ils viennent d'une contrainte d'écran** : le **nom** de l'activité, que porte une tuile et un titre ; le même **coupé à dix caractères** pour la ligne d'état ; et un **nom court** par personnage. Les deux premiers sont deux lecteurs à deux budgets, pas un champ et son repli — les deux se tronquent de toute façon, et les déclarer laisse l'auteur choisir ce qui survit à la coupe (`ui.md`).

Une **exécution** est une ligne en base : une partie jouée, un essai, une conversation. C'est ce que le code appelle une activité, et c'est la seule des trois qui se stocke.

Un **bloc** est un groupe nommé de définitions, avec une seule propriété : l'accès y est **dans l'ordre**, c'est une campagne, ou **libre**, c'est un recueil de défis. Une forme, un drapeau — mais **deux portes distinctes dans l'app**, « poursuivre la campagne » et « choisir un défi » n'étant pas le même geste.

**Ce qui reste du code, c'est le catalogue** — les leviers avec leurs positions, l'arbre des feuilles, les sortes de déclencheurs, les sortes d'effets. Rien de ce qu'une définition contient n'est de la logique : des positions déclarées, des poids sur un arbre déclaré, du texte libre, des règles faites de sortes énumérées. Une histoire de vingt scènes écrite en Kotlin serait du contenu qu'on ne peut ni corriger, ni traduire, ni partager, et il faudrait recompiler pour changer une réplique — sans compter qu'un modèle doit pouvoir écrire une définition contre le catalogue, ce qui n'a aucun sens si sa sortie doit être compilée.

**Cette donnée vit dans des fichiers livrés avec l'app**, autoportants, lus directement. Trois raisons. **La version arrive gratuitement** : un fichier hérite de celle de la release, quand une ligne créée à l'exécution demande qu'on la frappe et qu'on lui écrive une migration. **Corriger, traduire et partager** restent possibles, ce qu'une table perd à moitié — rien ne traduit une base, et partager y veut dire exporter. Et **ce que le modèle fabrique à la volée n'est pas une définition** : un personnage inventé pendant une séance remplit les champs d'une activité, donc une ligne. Il ne devient une définition que le jour où on veut le **rejouer**, et c'est ce jour-là, nommé, qui ouvrira la table — à côté des fichiers, un lecteur unique les unifiant. Une interface, deux sources, zéro copie.

**Importer les fichiers en base est écarté** : la table serait la copie d'une source déjà sur le disque, lisible et versionnée, donc la deuxième source qui se décale que le projet refuse partout, plus une réconciliation à chaque mise à jour.

**Un fichier porte ses traductions, en table langue → texte.** Une définition mélange deux langues par construction : le `brief` et la mise en scène partent au modèle en anglais, ce que l'apprenant lit est dans sa langue. Un fichier par langue dupliquerait la prose anglaise et la ferait diverger ; des clés vers `res/` casseraient l'autoportance. C'est un **écart délibéré à la facette `android`**, déclaré au manifeste : sa norme d'i18n vise le texte d'**interface**, et la prose d'une scène est du **contenu**. Le prix est réel — une plateforme de traduction ne lit pas ce format.

### Ce qui se copie, ce qui se pointe

**Une définition est un gabarit appliqué à la création, pas une dépendance gardée ensuite.** Les positions de leviers sont donc **toujours sur la ligne**, y compris pour une exécution issue d'une définition. L'alternative — un pointeur ici, des valeurs là — ferait d'un même champ tantôt l'un tantôt l'autre, et obligerait chaque lecteur à trancher le cas avant de lire. La duplication ne peut pas se décaler, la définition étant fixe.

**L'origine est un champ à part** — quelle définition et quelle version, ou rien — et elle ne sert qu'à grouper : ouvrir le niveau suivant, ranger un score. Elle n'est jamais consultée pour savoir comment la séance était réglée. Sans la version, une mise à jour qui corrige une scène rendrait incomparables deux parties qui se croient les mêmes.

**Rien de tout ça n'est en base** : ni table de blocs, ni table de campagnes, ni table d'avancement. **L'avancement se dérive des résultats** — un niveau est ouvert si les résultats des précédents le disent ; stocké, il serait une deuxième source qui se décale. **Le déverrouillage lit la note de la séance**, calculée sur les feuilles réellement présentes, au seuil de partout, A ou B. Un bloc qui veut être plus dur monte la sévérité de ses réglages ; il ne déplace pas la barre.

**Une exécution issue d'un bloc n'est pas jetable** : son résultat porte l'avancement, donc la supprimer reverrouille un niveau ou vide une entrée de classement. Une conversation libre se jette sans conséquence. C'est le prix du choix de dériver l'avancement, et il se dit à l'utilisateur au lieu de se faire en douce.

### Les quatre portes

**« Mode » n'est pas un champ** : c'est une **couche d'accès, écrite en dur, côté écran** — la façon d'arriver à une définition. Le même moteur tourne derrière toutes. L'app s'ouvre sur un écran-titre qui les présente.

- **Histoire** — un bloc à accès **ordonné**. Chaque scène est une définition, les questions déclarées portent la mémoire d'une scène à la suivante.
- **Défis** — un bloc à accès **libre**, chacun avec ses poids, ses consignes, ses règles, et une issue.
- **Arcade** — quatre définitions d'*easy* à *extreme*, rejouées sans fin, une **rampe** qui durcit en cours de partie, la fin à zéro vie, un **score** par difficulté plutôt qu'une réussite. Un score ne se compare qu'à ceux de la même difficulté.
- **Libre** — **aucun enjeu** : pas de vies, pas de règle de fin, pas d'issue, aucune lettre. Ce n'est pas *sans réglages* : ses leviers sont réglables et posés par des préréglages ; c'est qu'aucun **écran de configuration** ne les expose en V1. L'entrée de la barre d'actions reste, grisée en portant sa raison, pour que la barre ne change pas de forme ensuite.

**Ce qui fait qu'une conversation est « libre » est l'absence d'enjeu, pas l'absence de mécanique.** Elle laisse **tous ses leviers ouverts** à l'apprenant, et son `brief` vient de lui. Elle peut porter des règles comme n'importe quelle autre définition — et c'en est une, livrée avec l'app comme les autres, avec ses positions de départ, son arbre et ses règles. Ça retire un « par défaut » qu'il aurait fallu câbler ailleurs, et surtout le besoin de dire « hors défi » : **« défi » n'est pas un terme du modèle**, c'est un nom d'usage. Ce qui distingue deux activités est leurs réglages, leur arbre et leurs règles, rien d'autre.

**Ce que la couche coupe, ce sont les lettres, jamais les mesures.** Un mode qui ne montre pas les notes montre quand même *2,3 demi-tons*, *47 sons sur 50*, *un mot mal formé* — des faits qui ne dépendent d'aucune sensibilité. **Et c'est là, et pas dans un levier, que se décide si les notes se montrent** : voir sa note n'est ni plus dur ni plus facile, donc le test du levier le refuse, et ce n'est pas non plus un champ d'activité, c'est de l'écran. En conversation libre, le défaut est de n'afficher que les marques.

**Ce n'est pas purement cosmétique pour autant** : la couche décide **quelle définition part**, et pour une campagne elle dérive quel niveau est ouvert. Peu de code, mais pas un drapeau.

**Et les libertés d'un mode ne sont pas un mécanisme à lui.** Qu'en libre un appui sur le texte de l'IA bascule brouillé/net et qu'un défi ne le permette pas est l'application de ce qui est déjà écrit — le module décide quels leviers il expose. La liste des libertés d'un mode est la liste des leviers que sa définition laisse ouverts, plus les gestes qui ne sont pas des leviers.

### Les thèmes et les trous

**Derrière la porte « Libre », des thèmes — et un thème est une définition.** *« À la fin d'une conférence, tu engages la discussion avec l'intervenant »* est une situation, qui est l'intervenant est une mise en scène, tous les leviers restent ouverts. Quelques situations préenregistrées sont donc quelques fichiers, et rien de neuf. Le choix *personnalisé laissé vide* est la définition qu'on livre déjà, dont la situation est vide.

**Ce qui est neuf est le trou.** Une définition peut déclarer des **emplacements** que l'apprenant remplit au lancement — une clé, une question dans sa langue, deux ou trois mots. Trois choses les gouvernent. Le **même trou apparaît des deux côtés du `brief`** et une seule réponse les remplit ensemble : écrits séparément, la situation et la mise en scène dérivent dès le premier lancement — une conférence sur la neurophysique et une sur la théorie du genre ne font pas le même intervenant. La réponse est **substituée à la création** et copiée sur la ligne, donc la séance se lit seule. Et elle se tape dans **la langue que l'apprenant veut** : ce qui compte est qu'il décrive ce qui l'intéresse, pas ce qu'il sait écrire en anglais.

**Le genre se demande de la même façon, et sans rien déclarer.** L'écran de lancement d'une libre porte toujours le champ court et le **genre**, dont le défaut est *peu importe* — qui n'est pas un troisième genre mais l'absence de contrainte, et fait **tirer au hasard** parmi les génériques disponibles. Rejouer un thème ne redonne donc pas la même personne, ce qui sert la rencontre ; le tirage entre au journal et la voix tirée est copiée sur la ligne. **Ce qui dit qui décide est la présence du champ** : une définition qui déclare un genre l'impose, une qui n'en déclare pas se le fait demander. Pas de drapeau *demandé* à écrire.

**Ce que le trou ne porte pas, le modèle le remplit.** La mise en scène fixe porte le **rôle** — un conférencier qui vient de parler, fatigué, flatté qu'on s'intéresse — et le trou porte le **sujet**. Le registre qui va avec est de la prose que personne ne relit, donc gratuite. Ce qui reste au travail de l'auteur est d'écrire la mise en scène pour que le trou porte assez.

## Les prescripteurs

Un **prescripteur** est ce qui remplit une activité — sa matière et ses réglages. Il y en a trois : **la conversation**, à partir de ce qui vient de s'y passer ; **l'apprenant**, par choix ou consigne libre, la voie par laquelle de la matière neuve entre ; **la progression**, pour ce qui est dû, jamais réussi, ou sur le point d'être oublié. Un quatrième viendra peut-être, le contexte global. Ajouter un prescripteur n'est qu'une façon de plus de remplir les mêmes champs.

Un prescripteur ne décide rien pendant : il remplit des champs avant, puis se tait. Les trois sont nommés en code (`Prescriber`) et **seul l'apprenant en est un pour l'instant** ; les deux autres attendent le catalogue des activités.

## L'état d'une séance

**Trois natures, et il n'y en a pas de quatrième** — le critère du projet appliqué à l'état entier.

- **Déclaré** — l'entrée figée, jamais réécrite. De la définition : l'origine, l'ouverture, le `brief`, la distribution, les consignes de départ, les positions de départ, l'arbre des poids, les règles, les questions. De l'apprenant : son **nom**, sans lequel aucune scène ne peut le désigner ; l'accent, donc la voix de référence ; les fournisseurs choisis par maillon.
- **Enregistré** — ce qui ne se recalcule pas : les énoncés et leur audio, les mesures de chaque tentative, le journal de ce qu'un tirage ou l'IA a choisi, et les faits portés par chaque tour — position de capture, comment il s'est fini, par laquelle des deux horloges.
- **Dérivé** — les positions effectives des leviers, les consignes en vigueur et ce qui reste de leur durée, les feuilles, les notes, les comptes, le blocage, les deux portes, l'avancement, l'issue, et les deux horloges.

**Une quatrième nature a été essayée et ne tient pas.** « Éphémère » devait ranger ce qui n'existe que le temps d'un tour ; aucun candidat ne s'y range — les horloges laissent une trace enregistrée, le menu se recalcule, le message d'une règle est écrit dans la règle donc déclaré. Durer peu n'est pas une nature.

**La position déclarée d'un levier et sa position effective sont deux choses**, et les confondre fait paraître contradictoire que les réglages soient fixes pendant que les vies se perdent. La définition écrit la première, qui ne bouge jamais ; les patchs déplacent la seconde, qui est ce que l'apprenant vit.

## Les réglages

**Ce qui se stocke est une liste ouverte de positions de leviers, et rien d'autre.** Le critère est qu'elles répondent toutes à la même question : *où en est ce paramètre en ce moment*. Une rampe n'y répond pas, elle dit comment ça va changer ; une origine non plus. Deux raisons concrètes de tenir cette homogénéité : l'arcade doit **annoncer chaque changement en une phrase**, ce qui exige que toute entrée porte une phrase lisible par position — une rampe n'en a pas ; et l'état effectif se calcule depuis les réglages et les règles, donc mettre une règle dans les réglages ferait chercher l'entrée du calcul là où il range sa sortie.

**Les réglages sont fixes pour toute la durée.** Ce qui bouge est l'**état effectif**, calculé depuis les réglages, les règles, le journal et l'avancement. L'arcade n'est pas une exception : sa rampe est une règle.

**Chaque levier se déclare** : sa clé, ses positions possibles, sa valeur par défaut, et la phrase lisible de chacune de ses positions. Sans cette déclaration, une clé absente devient un défaut silencieux.

**Ce qui se règle sans passer par un levier est un préréglage** — un nom donné à un paquet : des positions, et éventuellement des fragments de consigne et des règles. C'est le mode custom qui le prouve, puisqu'il expose chaque levier séparément : si les préréglages étaient le modèle, il faudrait une deuxième façon de décrire la même séance. Un préréglage est un **gabarit appliqué à la création**, comme une définition ; ce qui reste sur la ligne, ce sont les positions.

**Le curseur d'aptitude est un préréglage parmi d'autres, pas la forme générale.** *Élocution 2* en est un ; *easy* et *hard* en arcade en sont d'autres, qui nomment une combinaison entière ; une brique de lieu ou de personnage en est un troisième, qui apporte aussi de la consigne et des règles. Un préréglage peut déplacer des **sensibilités** en plus des aides — c'est ce qui rend *durcir* exprimable — et poser des **poids** ; ce qui reste interdit est qu'un **patch** en déplace un pendant la séance. **Quels préréglages chaque mode offre reste à écrire**, et rien n'oblige un mode à en avoir un par aptitude.

**Un curseur d'aptitude ne pose que de la pression** — les aides retirées et la sévérité —, jamais le poids de son aptitude. Ce sont deux questions : *combien j'exige* et *sur quoi on regarde*, et on peut vouloir une élocution qui compte beaucoup mais jugée avec indulgence.

**Une activité est une conversation, et « format » n'est pas un axe.** Ce qu'on rangeait sous d'autres formats existe comme des moments *dans* la conversation : la lecture à voix haute **est** redire, un texte de référence connu d'avance ; la répétition après un modèle **est** le remède de toute faute sonore. Un format neuf aurait dupliqué cette machinerie. Le jour où une vraie deuxième forme d'échange se présente, c'est une migration, manuelle et explicite.

**Les vies sont un levier** : trois vies pressent comme un seuil de silence court. Et **sa position est le nombre restant**, pas une allocation posée à côté d'un compteur — la définition écrit sa position de départ, un patch la déplace, zéro met fin. C'est ce qui fait qu'il n'y a pas d'objet compteur de vies, donc pas de sorte d'effet « retirer une vie » à côté de « poser un patch ».

**Le curseur de difficulté de l'arcade nomme une combinaison entière**, il n'en fait pas partie ; sa rampe monte donc **un cran entier**, pas une suite de changements levier par levier.

### Ce qui fait un levier

**Des positions déclarées et fermées, et un côté dur.** Les deux ensemble : sans positions déclarées il n'y a ni phrase à afficher ni valeur à valider, et sans côté dur la notification ne peut pas annoncer *ça se durcit*.

Le test écarte deux choses qu'on rangerait là par réflexe. **La voix n'en est pas une** : entre deux voix il n'y a ni dur ni facile, et ses positions arrivent du catalogue du fournisseur, qui change avec la clé. C'est un **champ de l'activité**, que la distribution porte. **Le registre non plus** : il est ordonné — familier, courant, soutenu — mais aucun bout n'est le difficile, le soutenu demandant du vocabulaire et le familier des idiomes qu'aucun manuel ne donne. Il vit dans la consigne de pertinence, avec la longueur imposée et le mot interdit.

**Ce qui ne se règle pas par un levier doit se dire là où on serait allé chercher le levier.** L'écran custom montre des leviers : qui veut imposer un registre y cherchera un curseur et conclura que ça ne se fait pas. Le catalogue donné au modèle a le même trou en pire — il inventera un levier. Il faut donc une entrée pour la consigne elle-même, disant ce qu'on peut y mettre.

**Deux formes, et la seconde n'est pas une version dégradée de la première.** **À marches** — une liste fermée de positions nommées, chacune avec sa phrase. **À nombre** — une unité, un minimum, un maximum, un pas, et une phrase à trou : « tu as {n} vies ». Forcer les nombres en marches coûte tout de suite : « vies : peu / normal / beaucoup » interdit à un défi d'en demander quatre, et ment à l'écran où l'apprenant voit trois cœurs. Rien ne se perd en échange — un nombre est ordonné tout seul, et le menu envoyé au modèle ne change pas, il choisit entre des patchs tout faits. **Le maximum peut être « pas de maximum »** : les écoutes et les tentatives se laissent volontiers illimitées, et un très grand nombre à la place mentirait à l'écran.

**Les positions d'un levier à marches se déclarent de la plus facile à la plus dure**, donc le côté dur n'est pas un champ : c'est la dernière. Un levier **à nombre** garde le champ, son ordre étant arithmétique.

**Un levier peut en appeler un autre, et la coupe suit une seule règle : zéro éteint le levier quand c'est vrai, sinon deux leviers.** Zéro réécoute *est* la réécoute interdite, zéro reformulation *est* le petit bouton qui n'apparaît pas. Le seuil de silence, lui, n'a aucune valeur qui voudrait dire « pas de seuil » — zéro enverrait le tour aussitôt — et ce qui sépare la position 2 de la position 3 n'est pas x mais le fait que le tour parte tout seul. Trois leviers ont été coupés ainsi : le seuil de silence, la cadence, les vies.

**Sans objet ne veut pas dire absent.** La valeur reste sur la ligne, elle n'est pas lue, et elle est là si le levier maître remonte. Elle s'affiche, grisée, en portant sa raison. Un patch qui déplace un levier sans objet annonce un durcissement qui n'a pas lieu : erreur d'écriture, pas un cas à traiter.

**Chaque levier déclare aussi qui le tient : l'app, ou le modèle.** Les vies, le seuil de silence, la capture, les tentatives, la durée du tour sont **exécutés** — l'app les fait, donc ils sont vrais. La longueur et la complexité du tour de l'IA, l'écho, l'explication sont **demandés** : l'app les écrit dans le prompt et rien ne vérifie qu'ils sont tenus. Trois conséquences, et aucune n'est un détail : une **condition ne peut pas lire un levier demandé**, faute de fait à lire ; le **banc ne peut pas l'éprouver** ; et sa **notification annonce quelque chose qui pourrait ne pas arriver** — on l'affiche quand même, se taire laisserait l'apprenant sans rien pour comprendre ce qui a été tenté, et ça dit la vérité à qui écrit un défi.

**La sensibilité est un levier, le poids n'en est pas un.** Aller vers sévère durcit pour tout le monde ; monter le poids de la mélodie durcit la séance de qui l'a mauvaise et allège celle de qui l'a bonne, donc sa direction dépend de l'apprenant, que le levier ne connaît pas. Il y a donc **une sensibilité par feuille**, déclarée dans l'unité de cette feuille, et **l'arbre des poids est un champ à part** — une structure à branches ne rentre pas dans une liste plate de positions sans clés bricolées.

**L'écran d'avant-partie tombe de tout ça sans rien coûter.** Chaque position déclare sa phrase lisible, donc l'écran qui montre ce qui attend l'apprenant **se génère** : ce qu'on lui demande, ce qu'on lui enlève, ce qui lui fera perdre, ce qui met fin, les consignes. Zéro texte par activité. **Ce qu'il montre exactement reste ouvert** : « ce qui s'écarte du défaut » ne marche pas, il n'y a pas d'ordinaire à quoi comparer.

### Le catalogue des leviers

**Brouillon, aucune liste n'est close.** Les **phrases lisibles par position** sont une seconde passe. Le **défaut** est celui d'une conversation libre. Le **groupement par aptitude est de la présentation** : un levier ne déclare aucune appartenance.

**Élocution**

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `model-listens` | nombre | 0 à sans maximum ; 0 vaut *de mémoire* | sans maximum | l'app |
| `tempo` | marches | libre, imposée | libre | l'app |
| `tempo.value` | nombre | % de la durée du modèle | — | l'app |
| `retakes-allowed` | nombre | 0 à sans maximum | sans maximum | l'app |
| `pronunciation.sends-back` | marches | non, oui | non | l'app |

`tempo.value` est **sans objet** quand `tempo` est libre. La cadence porte sur **tout tour analysé**, le modèle étant synthétisé pour toute phrase qu'on analyse ; ce qui change est ce que l'apprenant en voit — sur un tour spontané, un verdict après coup ; sur une redite, un décompte possible.

**Compréhension**

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `ai-turn.length` | marches | courte, moyenne, longue | moyenne | le modèle |
| `ai-turn.complexity` | marches | basse, moyenne, élevée | moyenne | le modèle |
| `ai-turn.display` | marches | le texte, le texte brouillé, seulement qui parle, rien | le texte brouillé | l'app |
| `replays` | nombre | 0 à sans maximum ; 0 vaut *interdite* | sans maximum | l'app |
| `noise` | marches | aucun, présent, fort | aucun | l'app |
| `filter` | marches | aucun, léger, marqué | aucun | l'app |
| `understanding.sends-back` | marches | non, oui | non | l'app |

Les positions de `ai-turn.complexity` sont **volontairement génériques** : c'est un levier demandé, donc le modèle l'interprète selon la scène, et des noms prescriptifs lui retireraient cette souplesse sans rien garantir. La marche du milieu d'`ai-turn.display` prend son sens à plusieurs personnages : on sait que c'est Vera qui parle sans lire ce qu'elle dit.

**Le bruit et le filtre étaient un seul levier et en font deux**, parce qu'ils sont indépendants : une pièce calme sur une mauvaise ligne, un café bruyant sur une ligne nette. Le **bruit** est un fond sonore, donc un fichier, donc du contenu ; le **filtre** est un traitement du signal. Les deux s'appliquent **à la lecture**, jamais au rendu mis en cache, le même fichier servant d'étalon à la mesure. **La standardisation reste ouverte, et une moitié seulement est solide** : le rapport signal/bruit standardise le niveau, il **ne standardise pas la difficulté** — un fond de conversations est bien plus dur que du bruit rose au même rapport, parce que c'est de la parole concurrente. Le filtre demande sa propre liste fermée d'effets nommés, dont l'encodage de l'intensité n'est pas décidé.

**Correction**

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `echo` | marches | explicite, indirect, absent | indirect | le modèle |
| `explanation` | marches | la règle et la phrase, la règle, aucune | aucune | le modèle |
| `rewordings-allowed` | nombre | 0 à sans maximum | sans maximum | l'app |
| `correctness.sends-back` | marches | non, oui | oui | l'app |

`echo` et `explanation` sont des **aides**, donc leurs positions se lisent à l'envers de l'intuition : c'est l'absence qui est le cran dur. Elles sont bien **deux** et non deux crans d'un seul, parce que **où** et **quoi** ne sont pas deux quantités de la même information — on peut donner l'un sans l'autre, dans les deux sens. Tout en haut elles se recouvrent : une reformulation explicite donne déjà la phrase correcte à voix haute.

**Pertinence**

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `relevance.sends-back` | marches | non, oui | non | l'app |

C'est son seul levier, et il ne dit rien de ce qu'elle exige : tout vit dans la **consigne**, qu'elle est la seule à porter. Ce n'est pas un trou — le registre, la longueur imposée, le mot interdit n'ont pas de côté dur.

**Fluidité**

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `capture` | marches | doigt, armée, armée et envoi au silence | doigt | l'app |
| `silence-threshold` | nombre | secondes | — | l'app |
| `turn-length` | nombre | secondes, maximum 30 | 30 | l'app |
| `preparation` | nombre | secondes | 0 | l'app |
| `discard-take` | marches | permis, interdit | permis | l'app |
| `fluency.sends-back` | marches | non, oui | non | l'app |

`silence-threshold` et `preparation` sont **sans objet** en capture au doigt, où c'est le pouce qui arme ; `discard-take` l'est en envoi au silence, pour la raison inverse. Le maximum de `turn-length` est le plafond technique.

**`preparation` est le temps entre la fin de la réponse de l'IA et l'armement du micro**, et non un temps de réflexion accordé *à l'intérieur* du tour : ce second sens serait une ligne interne à la mesure de fluidité, et une ligne interne ne se règle jamais. Vivant hors du tour, elle ne touche aucune mesure.

Voir ou non le texte de sa propre phrase n'est pas un levier : quand on redit, les marques sont posées sur les lettres, donc cacher le texte cacherait les marques.

**L'activité** — ne presse aucune aptitude en particulier.

| clé | forme | positions | défaut | tenu par |
|---|---|---|---|---|
| `lives` | marches | pas de vies, comptées | pas de vies | l'app |
| `lives.left` | nombre | 0 à sans maximum ; 0 met fin | — | l'app |
| `advance.words` | marches | poursuit, attend | poursuit | l'app |
| `advance.sound` | marches | poursuit, attend | poursuit | l'app |

**Les cinq `sends-back` et les deux `advance` sont sur deux axes différents.** Le premier dit **si le passage est à refaire** quand une aptitude ne passe pas ; le second dit **si la conversation attend** pendant qu'on le refait. On peut vouloir marquer sans bloquer, ce qui est la conversation libre ordinaire.

**`advance` en fait deux, pour la raison qui a déjà coupé les tentatives** : un défi qui ne vise que la prononciation garde ses redites intactes quoi qu'il arrive du côté des mots. Bloquer sur la grammaire en laissant filer la prononciation est l'ordinaire ; l'inverse est un exercice de prononciation.

**Et « attendre » ne veut pas la même chose des deux côtés.** Côté mots, le verdict arrive avec la réponse, donc attendre veut dire *jouer l'écho au lieu de la continuation* — un choix entre deux choses déjà rendues, gratuit. Côté son, le verdict arrive après l'appel : retenir la réponse mettrait l'analyse dans le chemin critique de **chaque tour**. Donc côté son, attendre veut dire seulement que **le passage ne se ferme pas**.

**`attend` est le cran dur parce qu'on ne sort pas d'un passage sans l'avoir refait** : le gros bouton n'est pas disponible. La combinaison *attend* avec zéro tentative n'est pas un cul-de-sac — les tentatives sont épuisées d'emblée, le passage se clôt non réparé aussitôt.

## Les règles

Ce qui change pendant une séance, et quand. Une liste `passage → changement` ne suffit pas pour deux raisons distinctes : tous les déclencheurs ne sont pas des numéros de passage, et le décideur n'est pas toujours la donnée écrite. D'où trois choses séparées — **quand**, **quoi**, **qui choisit**.

```
règle
  quand   : { sorte, moment, paramètres }  # six sortes, trois moments
  choix   : [ paquet, paquet, ... ]        # un seul élément = pas de choix
  qui     : écrit | hasard | IA

paquet    : [ effet, effet, ... ]          # un effet : patch, fin, ou message au modèle
```

**La rampe cesse d'être un champ** : c'est une règle dont le quand est « tous les N passages » et le quoi un cran de plus. **Et les conditions aussi** : « une feuille sous la barre → une vie » est une règle dont le patch retire une vie. Deux champs se replient en une liste.

**Ce qu'un tirage ou l'IA a choisi s'écrit sur l'activité.** Sans ce journal, l'état effectif ne se recalcule plus, et une séance ne se compare plus à elle-même trois semaines après. Garder une graine coûterait moins cher et ne rejouerait juste que si le code n'a pas bougé.

**Les sortes de déclencheurs sont une liste fermée et courte.** La pente est d'y glisser un mini-langage de conditions ; ce jour-là, plus personne ne sait ce qu'une définition fait sans l'exécuter.

### Le patch et ses deux phrases

**Un patch porte des positions de leviers ou des déplacements, et éventuellement des consignes.** Une **position** a la forme des réglages et hérite de la phrase déclarée avec le levier ; un **déplacement** dit de combien de crans on bouge et dans quel sens. Appliquer, c'est superposer ; annoncer, c'est lire la phrase ; proposer à l'IA, c'est envoyer les phrases et attendre une clé.

**Les deux formes sont nécessaires.** `lives.left ← 1` de la mort scriptée ne s'écrit qu'en position ; « perdre une vie » ne s'écrit qu'en déplacement, l'auteur ne sachant pas combien il en reste quand la règle part. **Un déplacement s'arrête à la borne du levier**, ce qui n'est pas une erreur, et **ce qui n'a rien déplacé ne notifie rien**.

**Annoncer dit aussi le sens du changement.** « Deux tentatives permises » ne dit pas si on vient de monter ou de descendre, et ce n'est pas la même nouvelle. Chaque levier sait de quel côté est le dur, donc un patch connaît sa direction et la notification annonce *ça se durcit* ou *ça s'allège*, plus la phrase.

**Un patch porte une seconde phrase, de mise en scène, écrite d'avance.** La phrase mécanique est déclarée avec le levier, donc une seule fois pour toute l'app : « cinq secondes de silence » ne peut pas se dire *le barman s'impatiente* dans un pub et *le recruteur attend* dans un entretien. La face qui joue la scène vit donc sur le patch.

**La mécanique se lit après la réplique, la narrative dit laquelle des deux places elle prend.** *« Il te reste une vie »* constate, donc c'est un reçu et il vient après ; *« tu te fais bousculer par un passant »* plante le décor du tour qui suit, donc lu après la réplique il fait tomber l'excuse de nulle part. La phrase de mise en scène porte donc un drapeau **avant / après**, par défaut après.

**Les deux s'affichent, et la narrative ne remplace jamais la mécanique.** Qui ne lit que *« le barman semble pressé »* ne sait pas que son tour part maintenant tout seul au bout de cinq secondes, et croira à un bug. La mécanique est obligatoire — une conversation libre n'affiche qu'elle, n'ayant pas de fiction — la narrative est facultative. Les deux vont dans le même sens : *le barman se détend* sur un patch qui durcit est un mensonge, et la direction permet de le voir à l'écriture.

**Elle n'est pas produite par le modèle** : elle s'affiche à côté d'une affirmation mécanique, donc un texte inventé au moment où ça tombe peut la contredire. Le modèle garde tout le reste — il **joue** le changement dans son tour, où il est libre et où c'est gratuit. Ce qui doit changer d'une partie à l'autre s'écrit en **message au modèle**, prose libre qu'il joue à sa façon.

**Ce qui manque se voit, mais on ne peut pas dire ce qui pourrait manquer.** Le catalogue ne propose aucune liste complète des occasions de parler, et il ne le peut pas — une condition sur un élément est un espace paramétré, et le déclencheur *le modèle juge que oui* est de la prose libre. Ce qui se dénombre est ce qu'une définition a **écrit**, et c'est précisément ce qui sert un auteur : l'écran d'écriture affiche ses douze règles et dit lesquelles n'ont pas de phrase de mise en scène. Un oubli ne casse rien.

### Les trois familles

**Ce qu'un patch pose a deux propriétés indépendantes, une direction et une phrase.** Trois familles en sortent, et la quatrième combinaison n'existe pas.

| | direction | phrase | ce que c'est |
|---|---|---|---|
| **levier** | oui | oui | la notification dit *ça se durcit* et lit la phrase |
| **consigne**, **interrupteur de condition** | non | oui | la notification lit la phrase, sans direction |
| **drapeau** | non | non | rien ne s'affiche ; ça se pose et ça se lit |

**Un interrupteur de condition n'est pas un levier** : « active est le côté dur » est faux depuis qu'une condition lit les deux moitiés de l'échelle — *« atteint A → gagne une vie »* est une condition dont l'activation **allège**. Il garde sa phrase, parce que *« les silences ne coûtent plus rien »* est une nouvelle qu'il faut donner, et il perd sa direction.

**Trois objets à ne pas coller.** L'**interrupteur** porte deux positions et chaque position porte sa phrase ; le **patch** est ce qui le déplace ; la **notification** lit la phrase de la position où on vient d'arriver. Il y a donc deux règles en jeu : celle dont le patch déplace l'interrupteur, et celle que l'interrupteur arme ou désarme.

**Un drapeau est un événement nommé**, qu'un patch pose et qu'un déclencheur lit. **Aucune des six sortes de déclencheur n'en lit un**, et la liste est fermée : l'armement fait déjà le travail, et le code replie donc le drapeau sur l'armement. Ce qui reste à dire est si la famille garde un nom à elle (`../TODO.md`). **Un drapeau ne se compte pas** — compter un événement qui n'a pas son levier, *la troisième fois qu'il mentionne le dragon*, s'écrit en **chaîne de drapeaux**, une règle par cran, la première armant la deuxième. Verbeux, et écrivable ; un compteur générique attend d'avoir été demandé par une scène réelle.

**Une règle ne se retire pas ; ce qu'elle a fait se défait.** Alléger est un patch comme un autre, et une règle qui ne doit plus s'appliquer se **désarme par son interrupteur**, qui porte sa phrase — pas un second étage muet où plus personne ne saurait ce qu'une définition fait sans l'exécuter.

**C'est aussi ce qui écrit la surcharge, et ça évite deux mécanismes.** Une même occasion qui doit produire une chose la première fois et une autre ensuite s'écrit en deux règles dont la première désarme l'une et arme l'autre. Ni plafond de déclenchements, ni ordre de déclaration : à tout instant, une seule des deux est armée.

```
R1  (armée au départ)
    quand lives.left atteint 0
    → [ message au modèle : la fausse mort,
        patch : lives.left ← 1,
        patch : R1 ← désarmée,
        patch : R2 ← armée ]

R2  (désarmée au départ)
    quand lives.left atteint 0
    → [ finir, issue = raté ]
```

### Comment les règles d'un même moment se résolvent

**Par vagues.** Tous les déclencheurs sont évalués contre le **même instantané**, celui du début du moment ; puis tous les effets s'appliquent ensemble. Ce que ces effets viennent de rendre vrai ouvre la vague suivante, jusqu'à ce que plus rien de neuf ne se déclenche.

**L'armement n'ouvre pas de vague.** Une vague s'ouvre sur ce que les effets ont rendu **vrai** — une vie perdue, un seuil atteint — et armer ne change aucun fait du monde, ça change quelles règles regardent. Sans cette séparation, la règle qu'on vient d'armer part dans le même souffle que celle qui l'a armée. Ce n'est pas revenir à une vague unique : deux déclencheurs lisent l'état et se font rendre vrais par un patch du moment même, donc sans vagues, réagir à une vie perdue arriverait un passage trop tard.

**Une règle ne se déclenche qu'une fois par moment**, et c'est cette borne qui fait que ça termine : un nombre fini de règles, chacune au plus une fois. Ça remplace la garde d'un seul saut, et mieux — elle termine pour une raison qu'on peut prouver, et elle rend écrivable la chaîne de drapeaux qu'un saut unique interdisait.

**À l'intérieur d'une vague, l'ordre ne change rien.** Reste un seul cas où il mordrait : **deux patchs d'une même vague sur la même clé**. Ce n'est pas à arbitrer par une priorité, c'est une erreur d'écriture — **sauf s'ils posent la même position absolue**, le seul cas où la coïncidence se voit sans exécuter. **Qui le regarde reste à écrire.**

**Ce que ça coûte, dit franchement** : les systèmes d'auteur font plutôt l'inverse, en séquence, où poser une valeur puis la lire marche dans le même souffle. Ce qu'on perd est une chaîne de trois choses dans un même instant ; ce qu'on gagne est qu'une définition se lit sans être exécutée. Et les beats d'une scène sont séparés par des tours de parole, donc par des moments distincts.

**La vérification terminale vient après les vagues.** Zéro vie met fin, et ça s'évalue une fois, sur l'état stabilisé — sinon la fin gagnerait toujours la course contre la règle qui remplit les vies, et la mort scriptée serait inécrivable.

**Une règle se résout avant que l'IA réponde**, parce qu'elle doit pouvoir fabriquer l'occasion de la contrainte qu'on vient de poser. Le cycle est donc : le passage se ferme, les règles se déclenchent, l'IA répond en connaissant ce qui a changé, son tour est dit, la notification s'affiche hors du temps de parole, le micro s'arme. Quand c'est l'IA qui choisit, **elle choisit et répond dans le même appel** — deux sorties, pas deux allers-retours. Contrepartie : elle choisit en sachant ce qu'elle a envie de dire, et le menu est ce qui borne ça.

**Le menu d'un tour est calculé, pas maintenu** : l'ensemble des choix offerts par les règles qui se déclenchent maintenant. La plupart des tours il est vide, et on n'envoie rien.

### Quand une règle se déclenche

**Trois moments, distingués par ce qui est calculé à cet instant.**

- **Pendant l'enregistrement.** Deux horloges tournent, le temps écoulé et le silence en cours. Aucune feuille n'existe encore — donc une règle de ce moment ne peut lire qu'une horloge. Ce n'est pas une restriction posée, c'est un fait sur ce qui existe.
- **À la fin d'une tentative.** Le tour est parti, le modèle a répondu, l'analyse a tourné. Se décide là ce qui concerne cette tentative : les deux portes, et si la conversation attend.
- **À la fermeture du passage.** La note du passage est celle de la dernière tentative et le compte est connu. Tombe là tout le reste : les patchs, la rampe, les vies, la fin.

**Un passage se ferme au gros bouton, pas quand un tour part.** Il contient autant de tentatives qu'on en fait, et en « attend » il ne peut pas se fermer du tout — c'est pourquoi le blocage ne peut pas attendre la fermeture.

**Six sortes de déclencheur, et la liste est fermée.**

- **une horloge atteint sa valeur** — laquelle des deux, et la valeur. Les deux sont le temps maximal d'enregistrement et le seuil de silence.
- **un nœud de l'arbre dit quelque chose** — lequel, laquelle des lectures qu'il offre, et une valeur.
- **un compte de passages** — à tel passage, ou tous les N.
- **le modèle juge que oui** — une phrase en prose, *« s'il dépasse les limites de la politesse »*, et il répond oui ou non.
- **un levier a bougé** — lequel, et dans quel sens. Il lit un **changement**, jamais une position.
- **un levier atteint une valeur** — le jumeau exact de la première sorte.

La cinquième existe pour que la porte de devant soit **composable** : réagir à la perte d'une vie oblige sinon à coller le même message sur chaque règle qui en retire une, donc à le réécrire autant de fois qu'il y a de façons d'en perdre. La sixième a été ajoutée pour **la mort scriptée** — un boss qu'on ne peut pas vaincre, dont l'issue est une fausse mort qu'on traverse : sans elle, « quand les vies tombent à zéro » n'est pas écrivable, la cinquième se déclenchant à *chaque* vie perdue.

**Aucune sorte ne lit une horloge hors de l'enregistrement, et c'est une borne choisie.** *« Après deux minutes de conversation »* n'est pas écrivable : le système est par tours, donc une durée de séance ne gouverne rien qui puisse se déclencher entre deux paroles.

**Le quatrième ne coûte pas d'appel** : le modèle répond dans celui qu'on fait déjà, en un champ énuméré. Il porte le drapeau **demandé** avec tout ce qu'il implique. Il peut parfaitement parler de langue, c'est une app de langue ; ce qui le borne est ailleurs et suffit — **l'effet d'une règle ne produit jamais une marque ni une note**. *« Elle se braque parce qu'il est trop familier »* est une conséquence d'histoire, pendant que l'empan est marqué *à côté* : deux choses déclenchées par le même comportement, pas deux verdicts concurrents. Reste un coût d'auteur, les deux pouvant sembler se contredire à l'écran.

La première sorte n'a que le premier moment, la troisième que le dernier. **La deuxième doit dire lequel des deux** : « la correction est sous B » veut dire *bloque maintenant* à la fin d'une tentative, et *perds une vie* à la fermeture. À l'intérieur de la fin de tentative, l'instant exact **se déduit de la feuille** — une feuille de son n'existe pas avant que l'analyse ait fini.

**Le début d'une séance n'est pas un déclencheur.** Un déclencheur existe pour éprouver quelque chose à un moment qui revient ; le début arrive une fois et sans condition. Ce qui ouvre la séance est donc un champ de la définition — **mais ce champ porte un paquet d'effets, pas un texte** : ouvrir une scène demande souvent un message au modèle et une phrase affichée à l'apprenant, et un champ de texte n'en porte qu'une. Le paquet est celui qu'une règle porte déjà, donc zéro type neuf. Les trois façons d'ouvrir — l'apprenant parle en premier, le personnage dit une réplique écrite, le modèle improvise sur consigne — sont trois façons de remplir ce paquet, pas trois champs. Rien ne pousse vers l'une des trois pour la latence : l'apprenant vient d'appuyer sur *lancer* et n'attend pas sa propre réponse.

### Les conditions attachées à une feuille

Ce qui rend une contrainte dure n'est pas la note — noyée dans une moyenne pondérée par la longueur, une occurrence coûte quelques centièmes de lettre — mais une **condition branchée sur la feuille**, qui se déclenche sur-le-champ.

**Une condition est une règle**, pas un mécanisme à part : son déclencheur lit l'arbre des notes, son effet est un patch. **C'est donc un terme étroit**, une des six sortes, et il faut s'y tenir : « les conditions de fin » sont des règles et pas des conditions, et confondre les deux fait affirmer d'une règle quelconque ce qui n'est vrai que de celle-ci.

**Trois formes, et il n'y en a pas d'autre.** Une condition lit :

- **un élément** — au moins un élément atteint une valeur : un silence de plus de cinq secondes, un mot au cran `ne se dit pas`, un son au-delà de la ligne du gros raté ;
- **le chiffre de la feuille** — plus de 30 % du tour passé en silence ;
- **la note**, à la lettre nommée, **dans les deux sens** — tombé en D, ou atteint A.

**Ce que la première forme peut dire dépend de la nature de l'élément**, et la feuille le déclare déjà : un élément vrai/faux ou porteur d'un cran ne laisse rien à choisir ; un élément qui porte une quantité prend un seuil.

**La lecture par élément n'a pas de dénominateur, et c'est voulu** : c'est le seul endroit du projet où un fait absolu se lit. « Au moins un mot `à côté` » se déclenche pareil sur trois mots et sur trente ; qui veut la proportion lit le chiffre. **Sur un marquage, l'élément est le mot et jamais l'empan** — un empan a toujours au moins un mot, donc les deux se déclenchent dans les mêmes cas, et sa **longueur** n'est pas lisible, dépendant de l'habitude de coupe du juge.

**Une condition sur un cran fréquent se déclenche presque toujours.** « Au moins un mot `plat` » est vrai à chaque passage ou presque. C'est un conseil à qui écrit un défi, pas un interdit.

Les deux premières formes portent leur seuil et ne bougent pas quand le défi durcit. La troisième **suit la sensibilité** : monter la sévérité rend la condition plus fréquente sans qu'on la touche, et c'est un service.

**La barre A–B ne borne pas les conditions.** L'invariant qui fixe la barre a été écrit pour la **lecture d'un résultat** — l'activité est-elle réussie, le niveau s'ouvre-t-il. Une condition n'est pas un résultat : *« quand il tombe en D, le barman fronce les sourcils »* ne se compare à rien et ne débloque rien. Son seuil est de la même famille que les cinq secondes de silence. **Ce qui reste à la barre est le verdict.** Et **lire les deux moitiés de l'échelle est ce qui rend une récompense écrivable** : gagner une vie est un patch, une fin porte l'issue *réussi*, il ne manquait que de pouvoir lire la bonne moitié.

**Une condition lit n'importe quel nœud de l'arbre, pas seulement une feuille.** La seule différence est étroite : un nœud n'a **ni éléments ni chiffre brut**, son nombre étant une moyenne dans aucune unité — **sur un nœud, la note est donc la seule des trois lectures disponible**.

**Une condition ne lit jamais autre chose que l'arbre.** Un fait porté par le tour — sa position de capture, comment il s'est fini — n'est pas lisible tel quel : il faut la feuille qui le lit, et c'est ce qui permet d'écrire un défi contre le catalogue plutôt que contre le code.

**Le poids gouverne la note, la condition lit sans passer par lui** : une feuille à 0 n'est pas éteinte, elle se calcule.

**Une condition lit un passage et se déclenche sur-le-champ ; l'accumulation vit dans l'effet, pas dans la lecture.** « Un tour interrompu coûte une vie » n'a besoin de compter jusqu'à trois nulle part : au troisième, le compteur est à zéro.

**Perdre une vie n'est jamais automatique.** Un passage non réparé ne coûte rien par soi-même : ça coûte une vie parce qu'une définition a écrit la règle qui le dit. Et les vies n'existent que là où il y a un enjeu.

## Ce que le modèle reçoit

**Le dosage ne se décide pas sur ce qu'un modèle sait faire, mais sur ce que l'app doit relire ensuite.** Ce que **personne ne relit** est du texte libre, sans limite — comment le personnage parle, ce qu'il raconte, comment il réagit : zéro champ, zéro validation, coût nul, et c'est là qu'il faut être généreux. Ce que **l'app doit exécuter** coûte un **champ de retour**, donc un contrat de plus, une validation de plus, et un trou silencieux s'il n'est pas validé.

D'où la forme qui rend la liberté bon marché : **prose libre à l'aller, clés énumérées au retour.** L'app déclare ce qui est disponible, l'IA **choisit une clé**, elle n'invente pas. Un champ, validé par appartenance — et c'est ce que les modèles font le mieux. **La borne n'est donc pas la liberté de l'IA, c'est la déclaration** : rien d'indéclaré ne peut être choisi, faute d'exécutant. **Non mesuré** : choisir dans cinq options est probablement plus fiable que dans soixante — offrir une liste courte est une prudence, pas un résultat.

**Un seul appel fait tous les métiers** — jouer le personnage, reconstruire `intended`, marquer les empans, juger le suivi, rendre la difficulté de son tour, choisir dans le menu. Ce n'est pas le prix qui tranche : un second appel ne coûterait rien en latence, le jugement ne servant qu'à afficher des marques pendant que la réponse se joue, ni en argent. Ce qui tranche est qu'un prompt bien structuré tient ses frontières. On regarde donc au cas par cas ce qui déteint ; on ne cloisonne pas d'avance contre un loup qu'on n'a pas vu.

**L'ordre des champs de retour est la cloison qui reste gratuite.** Le modèle écrit en séquence et chaque champ conditionne le suivant, donc `intended` rédigé avant que la voix du personnage soit prise vaut mieux que le contraire. D'où le contrat : `intended`, les trois marquages, `spoken`, la difficulté du tour, et le reste s'il y a lieu.

**Zéro mot retenu doit être énonçable au retour.** Sur un tour entièrement fait de remplissage et de morceaux abandonnés, il n'y a pas de phrase à reconstruire, et un `intended` en texte libre en inventerait une — le pire des retours, puisque rien ne le distingue d'une vraie.

**Le coût en latence n'est pas borné**, et il est mesuré : le contrat enrichi a triplé le maillon du modèle de langue (`providers.md`). C'est aussi ce chiffre qui dira si le second appel reste gratuit, l'argument qui l'écarte s'y adossant.

**L'état n'atteint le modèle que par la porte de devant.** Il reçoit en permanence les leviers qu'il tient — les *demandés*, qui n'existent que comme instruction — et rien d'autre. Une vie perdue, un passage raté, un seuil qui se raccourcit ne lui parviennent que si une règle a décidé de le lui dire, par un message au modèle, dans les mots d'un auteur. C'est ce qui donne à l'auteur le contrôle de ce que son personnage sait : sans règle, la faute ne change rien à la scène ; avec elle, le réceptionniste soupire. Coller l'état au prompt en permanence le ferait réagir toujours, et un réceptionniste qui lâche « il vous reste une chance » casse sa propre fiction.

**Des tours passés, on renvoie les répliques et jamais les marques.** Un modèle qui voit ses vingt derniers verdicts devient cohérent avec eux plutôt qu'avec le tour qu'il lit. Les redites restent dehors aussi : on renvoie la dernière tentative, comme l'écran — le personnage n'a pas à savoir que la phrase a été dite trois fois. Contrepartie assumée : il ne rebondira pas de lui-même sur une faute qui revient, et une règle est ce qu'il faut pour ça. **Ce n'est pas la taille qui tranche** — un tour dépense environ 300 caractères, donc trente tours tiennent dans 2 000 à 2 500 jetons, et les empans ajouteraient la moitié. Ce qui tranche est l'ancrage, et il se mesure : repasser le même tour dans deux historiques très différents, et regarder si le marquage change.

**Le détail par son ne se pose même pas** : un tour de quinze mots fait une cinquantaine de sons, plusieurs fois la taille du tour. Et tenir compte des sons pour répondre n'est pas le travail du personnage, c'est celui du prescripteur progression.

### Les quatre parties

Ordonnées par fréquence de changement, ce qui est aussi l'ordre où une instruction est le mieux suivie : ce qui gouverne le tour est le plus près de lui.

1. **Le contexte de l'app** — permanent, identique pour tous : ce qu'est l'app, le contrat de sortie, les invariants d'`intended` — ne jamais réparer la grammaire, les nombres en toutes lettres, ne jamais compléter un tour interrompu (`providers/ConversationPrompt.kt`).
2. **Le contexte de l'activité** — figé au lancement : le `brief`, la distribution, les réponses des scènes précédentes, les questions de fin.
3. **L'historique** — les tours passés, répliques seules. Le seul morceau stable en tête et variable en queue.
4. **Le présent** — les consignes en vigueur, l'état des leviers qu'il tient, le message qu'une règle vient de poser, le menu s'il y en a un, et les faits du tour.

**Les consignes sont en 4 et non en 3**, alors qu'elles ne changent qu'une poignée de fois par séance : posées avant l'historique, elles seraient enterrées sous trente tours au moment précis où elles doivent gouverner le tour suivant. Ce qu'on perd est un préfixe mis en cache, qui se compte en millièmes.

**Le `brief` se coupe en deux, et pas plus** : la **situation**, que l'apprenant lit, et la **mise en scène**, qui ne s'adresse qu'au personnage et ne s'affiche jamais. **Ce qui les sépare est qui elles situent** — la situation situe **l'apprenant, à la deuxième personne** (*« à la fin d'une conférence, tu engages la discussion avec l'intervenant »*), la mise en scène situe le **personnage** (*« tu es le chercheur qui vient de donner cette conférence »*). Aller plus loin empiéterait sur les consignes. Le prix de l'appel unique est ici : la mise en scène est dans le contexte du juge, où elle n'a rien à faire, et ce qui l'en tient à distance est une phrase disant que son critère est la consigne. Ça se vérifie au banc, ça ne se prouve pas.

## Le passage

**Le passage est l'unité de la note : un énoncé et toutes ses redites.** Le mot est neuf parce que « tour » désigne déjà un tour de parole — un enregistrement, un énoncé, une position de capture. C'est aussi pourquoi « le tour ne passe pas » ne veut rien dire : ce qui passe ou ne passe pas est un passage.

**Quatre états, et il n'y en a pas d'autre.** Un passage est **ouvert** quand rien n'est à refaire, **à reformuler** quand les mots doivent changer, **à redire** quand c'est la façon de dire qui est reprise, ou **clos** — de deux façons, **réparé** si la dernière tentative passe la barre, **non réparé** si les tentatives se sont épuisées avant.

**« Raté » n'est pas un état de passage** : c'est un terme d'issue d'activité. Ce qui se dit d'un passage est *non réparé*, et ce n'est pas stocké — ça se dérive de la note et du compte des tentatives.

**La note du passage est celle de la dernière tentative**, qui est ce qu'on sait dire maintenant. Les trois autres lectures sont pires : la **première** rend la redite sans effet, la **meilleure** laisse l'obstination atteindre A, la **moyenne** punit exactement le geste que l'app existe pour provoquer. Conséquence à dire à l'apprenant : une tentative de trop, après une réussite, peut faire baisser la note.

**C'est la dernière pour toutes les feuilles, sans exception.** Une proposition traînait ici — que la fluidité et la pertinence lisent la première, une phrase répétée n'étant plus de la parole spontanée. L'argument est juste et le prix est accepté : deux règles de lecture au lieu d'une coûtent plus qu'elles ne rapportent. **Et rien n'est figé par ce choix** : toutes les tentatives restant sous le passage, quelle tentative chaque feuille lit est un **calcul** et non un stockage.

**Une redite remplace à l'affichage, jamais en base.** Le fil ne montre que la dernière tentative, et **aucun écran ne montre les précédentes** — ni le fil, ni le bilan. Elles restent en base pour les mesures et pour un banc : une tentative effacée est une mesure perdue. Ce qui dit à l'apprenant que ça va mieux est la marque elle-même, lue sur la prise du moment.

**Les tentatives permises sont un levier qui peut valoir 1, et il y en a deux, un par compteur** — tant de reformulations, tant de redites — et ils ne se volent rien.

### Les deux boutons, et la clôture

**Le gros bouton dit une chose neuve et fait avancer la conversation ; un petit bouton posé sur la phrase la reprend.** **Le passage se ferme à l'appui sur le gros bouton** — l'app n'a rien à deviner de ce qui vient d'être dit, c'est un fait d'interface. Le budget épuisé fait disparaître le petit bouton, et il n'y a pas de reprise qui ne compterait pas.

**Les tentatives s'arrêtent à la clôture du passage**, les deux sortes. Une fois le gros bouton appuyé, le passage ne se retouche plus : il se réécoute et se relit pour toujours. Ce n'est pas une privation, parce que **rien d'autre que ce geste ne ferme un passage** — ni la réponse de l'IA, ni le temps qui passe. Qui veut retravailler sa phrase n'a qu'à ne pas passer à la suite, et c'est le meilleur moment : la marque est à l'écran, le modèle vient d'être synthétisé.

Une raison par sorte de tentative. Une **redite** ajoutée à un passage clos déplacerait sa note, donc celle de la séance, sous des règles de fermeture qui l'ont déjà lue — une vie perdue, un cran de rampe monté. Une **reformulation** ferait pire : elle relance l'échange, donc referait cinq tours trop tard une réponse sur laquelle la conversation a déjà bâti. Le premier point vaut même là où rien n'est en jeu, donc la règle n'a pas d'exception en conversation libre. Le travail hors du fil est un autre mode, qui n'écrit rien (`../TODO.md`).

Conséquence de forme : le petit bouton n'existe que sur le **passage ouvert**, le dernier du fil. Les passages plus hauts gardent de quoi écouter, relire et ouvrir leurs mesures. Un budget non épuisé qu'on abandonne en fermant est perdu — on était satisfait, c'est le sens du geste.

**En « attend », le gros bouton n'est pas disponible**, sinon on sortirait d'un passage bloqué en disant simplement autre chose. **Il revient quand les tentatives s'épuisent**, sinon rien n'avance — et ce qui se dit alors franchement : **« attend » ne garantit pas la réparation, il garantit qu'on dépense ses tentatives.**

### Reformuler, redire

**Une reformulation relance l'échange, dans les deux avances.** En « attend », l'IA n'a joué qu'un écho et sa réponse se fabrique sur la version corrigée. En « poursuit », elle a parlé : l'app **refait l'appel comme si c'était la première tentative** — le prompt ne porte rien des formulations précédentes, hors les compteurs —, la nouvelle réponse se joue, et la précédente disparaît du fil. Ce qui se paie est un tour entier de chaîne et le fait d'entendre deux réponses à une phrase presque identique ; ce qui s'achète est un fil qui ne se contredit jamais.

**Une redite ne relance rien**, ses mots étant les mêmes : il n'y a rien de neuf à répondre. Elle reste un **exercice** — la note du passage s'améliore, et l'analyse du son se remet à tourner, donc la prononciation se mesure là où un tour mal formé ne l'aurait jamais eue.

**Ça retire au projet le refus général de dédire, et le remplace par quelque chose de plus étroit.** Ce que l'app ne fait toujours pas, c'est jouer une réponse puis la contredire sur la même phrase : dans une tentative, l'appel rend la continuation et l'écho ensemble et l'app en joue **une**. Ce qu'elle fait maintenant, c'est refaire l'échange quand la phrase à laquelle il répondait n'existe plus. L'une est un dédit sans cause, l'autre la conséquence d'un fait neuf.

**Et ça ne fait pas doublon avec « attend ».** « Attend » **force** la réparation : le gros bouton est indisponible, et on n'entend qu'un écho en attendant. « Poursuit » l'**offre** : la conversation avance quoi qu'il arrive, et l'échange ne se corrige que si l'apprenant choisit de reformuler.

Le cas de la phrase corrigée qui dit autre chose que ce que l'IA avait compris **disparaît pour une reformulation**, la réponse se refaisant dessus. Il reste pour un passage clos sans réparation, et le remède est celui de n'importe quelle conversation — le dire au tour suivant.

**Le nombre de tentatives se compte et ne donne pas de note** : réussir du premier coup et au troisième ne sont pas la même chose, mais un compte monte à chaque essai, donc pesé dans une aptitude qui fait refaire il enferme l'apprenant. Un défi qui veut la réussite d'emblée l'écrit en **condition**.

## Les deux portes

**Une porte fermée est un échec, une porte ouverte laisse passer.** Fermer, c'est déclarer le passage à refaire ; ouvrir, c'est ne rien déclarer. Le sens se relit à l'envers une fois sur deux, alors il est écrit : ce qui **ferme** est ce qui **ne passe pas**.

| | quand elle se lit | ce qu'elle produit | ce qu'elle lit |
|---|---|---|---|
| **la porte des mots** | au retour de l'appel | le passage est **à reformuler** | correction, pertinence, compréhension |
| **la porte du son** | à la fin de l'analyse | le passage est **à redire** | élocution, fluidité |

**Ce qui range chaque aptitude d'un côté est déjà écrit : est-ce que les mots changent.** La correction, la pertinence et la compréhension font changer les mots ; l'élocution et la fluidité laissent la phrase intacte, dite autrement. **Ce qui reste au choix d'une activité est de quelles aptitudes elle fait refaire, jamais de quel côté ça tombe** — le côté est un fait sur la phrase, et rouvrir ce choix rendrait possible de faire redire une phrase qu'on va réécrire. En conversation libre, une seule aptitude est à *oui* : la correction.

**Les deux lisent une note, à la barre A–B**, jamais une feuille désignée par une définition. Rien ne s'y perd : un défi qui ne veut viser que la mélodie met un poids sur la mélodie et zéro sur le reste, et « élocution sous la barre » *devient* « mélodie sous la barre ». Ça évite d'écrire deux fois le même ciblage.

**La porte des mots est fermée dès qu'une aptitude des mots ne passe pas**, plusieurs pouvant le dire à la fois : le registre et la grammaire sont deux façons pour les mots de changer, pas deux critères concurrents.

**Mais deux faits la ferment sans lire aucune note.** Un **tour tronqué** : il n'y a pas de phrase complète, et le laisser passer reviendrait à faire redire un fragment sans droit de le finir. Et un **tour sans aucun mot retenu** : correction et pertinence n'ont pas d'éléments, donc il n'y a même pas de note à lire. Dans les deux cas c'est une **absence de matière du côté des mots**, donc non négociable, et jamais un verdict de correction — *« I went to the »* est coupé, pas mal formé, et le juge reçoit déjà comment le tour s'est fini, ce qui lui interdit d'en faire une faute.

**Ce qu'un tour sans mots retenus mesure quand même** : *le remplissage et les reprises* a tous ses éléments, et *le plus long silence* a le sien. *La continuité* et *le débit* sont **absentes** — l'une comparerait à un modèle qu'on n'a pas synthétisé, l'autre diviserait par zéro. Le suivi et le tour interrompu se lisent normalement.

**Rien de ce qui est jugé ne s'éteint quand elle se ferme** : les feuilles jugées décident si elle se ferme, donc l'éteindre par sa propre décision serait circulaire.

**En « poursuit », l'IA répond au fragment, et une reformulation refait cette réponse** : elle a répondu à une phrase dont elle ne pouvait pas connaître la fin. Qui ne reformule pas garde la réplique faite au fragment, et se faire couper puis reprendre au tour suivant est ce que fait n'importe quelle conversation.

### Ce qui coupe l'analyse du son

> L'analyse du son ne tourne pas si la **porte des mots** s'est fermée, ou si un mot est marqué **`ne se dit pas`**.

Ce n'est pas une troisième porte. Le premier cas est la raison d'origine — on ne travaille pas la prononciation d'une phrase qu'on s'apprête à réécrire — et il a une conséquence d'ordre qui tombe toute seule : la porte des mots se lit **avant** que l'analyse tourne.

Le second n'est pas une décision mais **une absence de sol** : une phrase qui n'existe pas dans la langue ne peut pas être synthétisée, et la faire dire au modèle donnerait à imiter une non-phrase.

|  | la porte des mots | `ne se dit pas` |
|---|---|---|
| nature | une décision | une impossibilité |
| ce qui est lu | la **note** d'un nœud, à la barre A–B | un **élément** : un seul mot suffit |
| qui décide | l'activité, en disant de quelles aptitudes elle fait refaire | personne, c'est un fait |
| négociable | oui | non |

**`mal formé` n'est pas dans ce cas-là**, et l'y mettre a été essayé : une seule faute de grammaire dans un tour de trente mots tuerait toute l'analyse du son, et un apprenant en fait à presque chaque tour. Il ferme donc par la **note de correction**, avec le coût accepté — au réglage le plus lâche, le modèle dira d'une voix native une tournure qu'on a choisi de ne pas reprendre.

**On ne peut pas retirer l'empan et synthétiser le reste**, comme on retire un *um*. Un bafouillage est **hors** de la phrase ; un empan qui ne se dit pas est **dedans**, et demande à être remplacé — le retirer laisserait *« I have [ ] years »*, une autre phrase, cassée elle aussi.

**Et rien d'autre ne s'arrête** : l'IA répond, le fil continue, la marque s'affiche, la reformulation est proposée. Le canal du son reste vide en portant sa raison, pour cette tentative-là seulement.

**Point ouvert : ce qui tombe d'un côté ou de l'autre dépend d'une décision du modèle de langue.** *I walk to school yesterday.* Le STT transcrit la bouche, donc *walk* ; le modèle décide l'intention, et avec *yesterday* il peut écrire *walked*. S'il écrit *walked*, la porte est ouverte et l'analyse voit un /t/ manquant : faute de prononciation. S'il écrit *walk*, la porte se ferme : faute de correction. Le même énoncé, deux traitements opposés, et ce qui tranche n'est contrôlé par personne. **Et la portée est plus large : le juge juge sa propre reconstruction** — un modèle qui répare en reconstruisant a effacé la faute **avant** de la juger, et rien en aval ne peut le voir. Chaque note de correction est donc conditionnelle à une fidélité que personne ne vérifie (`../TODO.md`).

## Le blocage

**Bloquer, c'est une règle dont l'effet est que le passage ne se ferme pas.** Pas un mécanisme neuf, donc branchable sur n'importe quelle feuille.

**« Ne pas passer » se dit de trois choses**, à ne pas confondre. Une **feuille** ne passe pas quand sa note est sous la barre : c'est une lecture, il ne s'ensuit rien. Un **passage** ne passe pas quand une aptitude dont l'activité fait refaire tombe sous la barre. Une **activité** ne passe pas quand elle se termine sans être réussie.

Concrètement : la réponse **n'ajoute rien** — elle ne répond pas au fond et ne pose pas de question neuve ; le passage **reste ouvert** ; rien n'avance tant qu'il ne se ferme pas.

**Ce n'est pas l'interruption que le projet refuse.** On ne part pas travailler ailleurs, le sujet ne change pas, l'écran ne change pas : c'est la conversation qui s'arrête sur une phrase.

**Deux axes, pas un.** L'**écho** — absent, indirect, explicite — et l'**avance** — poursuit, ou attend. Le geste fondateur du projet est la combinaison (indirect, poursuit) : *« Ah, you're 25! And where... »*. **Toutes les combinaisons s'offrent**, certaines étant seulement plus austères. Ce qui est garanti est ailleurs : **rien n'attend jamais sans qu'une raison soit visible**, et ce qui la porte est la marque, toujours là et invariante, plus la notification quand c'est le son qui bloque. Jamais l'écho, dont la position reste libre.

**La porte des mots coupe l'analyse du son, rien d'autre.** Trois choses se déclenchent autour d'un passage à refaire, et elles n'ont ni la même portée ni le même décideur :

| ce qui se passe | portée | qui décide |
|---|---|---|
| l'analyse du son ne tourne pas | la prononciation de cette tentative | automatique, dès que le passage est à reformuler |
| une reprise est proposée | l'écran | automatique |
| la conversation attend | le fil | `advance.words` ou `advance.sound` |

**Les deux dernières sont indépendantes, et ça se voit dans les deux sens.** Porte des mots fermée sans attendre est la conversation libre ordinaire. Attendre sans fermer la porte des mots est le blocage sur la prononciation : les mots ne changent pas, donc l'analyse a tourné — c'est même elle qui a rendu le verdict.

**Et *ne se dit pas* se lit sur les deux plans sans les confondre** : l'absence d'analyse y est un fait, refuser de continuer reste une décision, qu'un défi écrit avec une condition sur l'élément.

### Deux branches pour la correction, une notification pour le son

**Le verdict de correction naît dans le même appel que la réponse ; le verdict de son naît après**, l'analyse ayant besoin d'`intended`. Les deux blocages n'ont donc pas les mêmes moyens.

**Correction** : l'appel rend **une réponse plus une ligne courte** — la continuation, et l'écho de reprise (« Ah, you mean you ARE 25 »). L'app calcule la note dès le retour et joue l'une ou l'autre. Trois conséquences : **rien n'est jamais contredit dans une tentative** ; c'est littéralement « l'IA joue, l'app décide », puisqu'elle fournit la matière des deux issues sans trancher ; et la ligne courte **n'est produite que si l'IA a marqué quelque chose**, ce qui la rend gratuite sur un passage propre.

**Prononciation** : quand l'écart au modèle arrive, l'appel est fini. Rien ne peut fournir un écho en personnage sans un second appel, écarté pour la latence. La réponse se joue donc, et **c'est le passage qui reste ouvert** ; une notification dit de reprendre, en donnant à **écouter** le modèle, qui est synthétisé de toute façon.

**Elle ne nomme rien.** La porte du son étant câblée sur l'élocution et la fluidité seules, ce qu'elle nommerait serait le même mot à chaque fois, donc rien. Ce qui montre où porter l'attention est déjà à l'écran, et nommer la pire des marques serait une élection, que le projet ne fait nulle part.

### La sortie d'un passage bloqué

**Les tentatives s'épuisent, et c'est la seule sortie.** Pas de geste d'abandon à part : les tentatives sont déjà un levier, donc la sortie est déjà réglable.

**En « poursuit », les règles se déclenchent à l'appui sur le gros bouton**, qui ferme le passage et commence le tour suivant du même geste. Un tour de retard, jamais au mauvais moment.

Si les reformulations s'épuisent, le passage se clôt non réparé et les redites ne sont jamais entamées. **Et l'IA n'a rien à refabriquer** : l'appel avait rendu la continuation et l'écho ensemble ; en « attend », seul l'écho avait été joué, donc l'app joue la continuation qu'elle tenait. Aucun second appel, rien de rétracté.

### Le déroulé d'un passage

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
    à reformuler et que `advance.words` est sur « attend »

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

**Il y a donc deux épuisements et non un**, celui des reformulations et celui des redites, et ils ne tombent pas au même endroit : le premier avant que l'analyse du son ait tourné, le second après.

## La fin d'une séance et son issue

**Un effet de règle est un patch, la fin, ou un message au modèle.** Trois, et la liste est fermée. Finir ne peut pas être un levier : il faudrait un côté dur, et finir n'est ni plus dur ni plus facile que continuer. Le faire passer par les vies obligerait un défi « dix passages et c'est fini » à s'inventer une vie unique, donc à afficher un cœur à quelqu'un qui n'en a pas.

**Les vies à zéro mettent fin, et c'est une propriété déclarée du levier, pas une règle.** Elle s'évalue une fois par moment, après toutes les vagues, sur l'état stabilisé — ce qui laisse une règle remplir les vies dans le même moment sans que la fin tombe.

**L'effet « finir » porte l'issue qu'il ouvre** : *réussi*, *raté*, ou *la note décide*. Zéro vie finit en raté. « Tu as obtenu la clé » finit en réussi, l'objectif atteint suffisant dans un jeu. « Au passage 10 » laisse la note décider.

**L'issue se lit dans cet ordre.** 1. **Aucune fin n'est tombée** — l'apprenant a laissé en route : pas d'issue du tout, c'est une activité à reprendre, et la note se calcule sans rien conclure. 2. **Une fin sèche est tombée** — réussi ou raté, c'est dit ; la note s'affiche et ne décide pas. 3. **Une fin ordinaire** — la note à la barre A–B décide.

**Ça bouche un trou que la note seule ne voyait pas : la quantité.** Un A sur deux passages puis on ferme, c'est une note excellente et un défi qui n'a rien prouvé. « Assez de passages » n'est donc pas un critère de réussite mais une **condition de fin**.

**Il n'y a donc pas de champ « critère de réussite »** : il se dissout en la barre A–B, qui ne se règle jamais, et ce que les règles de fin déclarent. **Pas de champ ne veut pas dire pas de mécanisme** : une réussite scriptée s'écrit comme n'importe quelle fin, un déclencheur *le modèle juge que oui* et l'effet *finir* portant *réussi*. Le prix est celui du déclencheur, qui est **demandé** — personne ne vérifie que l'objectif a vraiment été atteint.

**Le message au modèle est de la prose injectée dans le prompt** — *« le barman a compris que tu lui as menti »*. Il ne double pas la phrase de mise en scène : celle-là s'affiche à **l'apprenant**, celle-ci part au **modèle**, et une règle peut vouloir l'une, l'autre, ou les deux.

**Il déclare s'il attend le tour suivant ou s'il provoque un tour tout de suite.** Sans ce second cas, rien ne peut faire parler l'IA d'elle-même — or l'ouverture *est* exactement ce cas. Ça donne les événements de scène : l'alarme qui sonne, le passant qui bouscule, le personnage qui relance qui se tait. Une seule borne suffit à tenir l'invariant de capture — **un tour provoqué ne tombe qu'à la fermeture d'un passage ou à l'ouverture**, jamais pendant qu'on enregistre.

**L'arcade ne réussit ni ne rate**, elle rend un score : sa fin est zéro vie, et ce qui compte est le nombre que le résultat porte. Comment ce score se calcule reste à écrire.

**L'enjeu, en quatre crans** — il y a une note ; la note reste ; la note est comparée aux autres ; la série des notes est traitée. C'est un concept pour parler d'une activité, pas une pièce du modèle : rien ne porte ces crans, ils tombent de la combinaison choisie.

## Qui décide de la pression

**La pression est ce que l'apprenant vit, pas un réglage.** Rien ne porte ce nom dans le code : elle est produite depuis plusieurs endroits qui ne se connaissent pas, et le mot sert à parler de leur somme. À distinguer du **levier**, moyen concret de la moduler, et du **curseur**, préréglage qui en pose plusieurs sous un nom lisible.

Trois décideurs, séparés par **qui tient la décision et quand elle est prise** : le module, c'est le concepteur, figé d'avance ; l'apprenant, un humain qui tranche sur le moment ; l'IA, une machine qui tranche sur le moment sans que ce soit écrit nulle part. Aucune paire ne fusionne.

| décideur | ce qu'il décide | ce qu'il ne décide jamais |
|---|---|---|
| **l'apprenant** | l'exigence par aptitude ; ce qu'il ouvre, accepte, écarte ; quand il redit | ce qui est vrai — il pose une barre, pas un verdict |
| **le module** | son déroulé, ce qu'il donne et retire, quels leviers il expose | ce qui est vrai — il ne peut pas adoucir une faute pour encourager |
| **l'IA** | tour par tour, dans les bornes reçues : relancer ou laisser, ouvrir ou fermer | le moment — elle ne part jamais travailler ailleurs |

**Le module ne peut pas adoucir un verdict** — un module « débutant » qui marquerait moins pour ne pas décourager casserait *la même faute doit produire la même marque*. Et **le module décide quels leviers il expose**, ce qui est un pouvoir qu'on lui accorde et pas un fait d'implémentation.

**Deux choses ressemblent à des décideurs et n'en sont pas.** La **mesure** n'a aucune discrétion : elle applique la barre qu'on lui donne, et un décideur sans discrétion est une fonction. Le **prescripteur** n'est pas un décideur d'un autre genre, c'est le **siège** que l'apprenant occupe et que la conversation ou la progression occuperont — les mêmes interdictions s'y appliquent sans qu'il faille les réécrire.

**Monter un curseur fait deux choses qui n'ont pas le même plafond.** **Retirer une aide** converge vers le réel : pas de texte, pas de réécoute, pas de préparation, c'est la vie ordinaire. **Durcir un jugement** dépasse le réel : personne, dans une vraie conversation, ne marque les sons au centième. Les crans hauts ne se conçoivent donc pas de la même façon.

**Les effets se cumulent d'une aptitude à l'autre, et c'est le principe.** Chercher le mot précis, tenir une forme imposée, traiter un tour dégradé produit des silences, donc fait baisser la fluidité sans que l'apprenant soit moins fluide. Une note ne se lit donc jamais sans la combinaison qui l'a produite.

**Un curseur qui impose un réglage à toute la séance est le fonctionnement, pas une collision** : la fluidité possède la capture, donc la monter change l'ergonomie de la séance entière. Le test qui sépare une vraie collision du fonctionnement : **une combinaison n'est un problème que si elle change ce qu'une mesure veut dire, pas si elle rend seulement la tâche plus dure.** Ce test donne une contrainte dure — le bruit et le filtre portent sur ce qui est **écouté**, jamais sur la prise de l'apprenant, l'analyse comparant deux enregistrements traités symétriquement.

## L'histoire

Un mode histoire tire sur tout ce que le modèle a de mou, donc il sert de banc d'essai. **Une histoire est un bloc à accès ordonné, et chaque scène est une définition.**

Pas une activité unique dont les scènes seraient des étapes : le `brief` est posé au départ et aucun patch ne le touche, donc une activité ne peut changer ni de lieu, ni de personnage, ni d'objectif — et la rendre modifiable défairait ce qui garantit qu'une séance a une seule situation, donc une seule note lisible. Et une scène **est** une définition terme à terme : ce n'est pas qu'on fait entrer l'histoire dans le modèle, c'est que le modèle décrivait déjà une scène sans le savoir. Le fil se coupe donc entre deux scènes, ce qui est plutôt souhaitable — dans un jeu, une scène se termine.

**Une définition déclare des questions ; le modèle y répond en fin de séance, et les scènes suivantes reçoivent les réponses.** Sans ça rien ne passe d'une scène à l'autre que réussi ou raté, et le barman de la scène 4 ne peut pas demander *« alors, tu l'as retrouvée, la meunière ? »*. Zéro objet neuf : l'exécution porte son origine, et son résultat porte déjà du texte.

**Le texte libre du résultat devient ces réponses, et rien d'autre.** Un défi qui veut un commentaire de fin déclare la question ; une activité qui n'en déclare aucune n'a pas de texte. Un mécanisme au lieu d'un champ fourre-tout.

**Une question déclare son format** : texte libre, oui/non, ou liste fermée. Le texte libre nourrit le prompt des scènes suivantes ; **une réponse fermée se lit par du code**, donc elle branche — *« a-t-il la clé ? oui »* ouvre la scène 5, sinon la 5 bis. Ce qui le lit est l'ordre du bloc, qui dérive déjà de ce que les scènes ont rendu.

**La question est déclarée par la scène qui la produit**, jamais par celle qui en a besoin : seul le modèle qui était là peut y répondre. Le prix est un prix d'auteur, et c'est le travail normal quand on écrit une histoire.

Deux limites. **Le modèle écrit sa propre mémoire**, et rien ne la vérifie : même famille que `intended`. Les questions bornent la dérive sans la supprimer. Et **ça grossit** : à la scène 12 on transporte les réponses de onze scènes.

## Le personnage

Rencontrer quelqu'un plutôt que choisir un thème. La version la plus bête est qu'un personnage est un `brief` — du texte dans le prompt, zéro pièce neuve. Elle manque peu : un personnage porte aussi une **voix** et des **positions de leviers**. Or un `brief` plus une voix plus des positions, **c'est une définition**. Le personnage n'est donc pas un objet neuf : une rencontre est le lancement d'une définition, un recueil de personnages est un bloc à accès libre.

**Plusieurs personnages.** L'activité pointe une **distribution**, pas un interlocuteur ; chaque énoncé porte **qui parle**, le champ locuteur cessant de valoir apprenant-ou-IA pour devenir une identité ; la synthèse choisit une voix **par énoncé**, ce que le cache encaisse déjà, étant indexé par texte et par voix. Côté modèle, l'IA rend la clé du personnage qui parle.

**La persona ne doit jamais atteindre la reconstruction d'`intended`.** Si le personnage déteint, la phrase de l'apprenant ressort en dialecte et c'est l'étalon de toute la mesure qui bouge. Même famille de fuite que celle déjà mesurée, où le modèle réparait la grammaire.

**Un personnage jetable ne coûte rien, un personnage qu'on retrouve coûte le stockage.** Fabriqué à la volée, il ne fait que remplir les champs d'une activité. Revoir Vera trois séances plus tard exige de la garder : c'est le jour où la séparation définition/exécution revient en base. La porte reste ouverte, elle n'est pas franchie.

### La voix

**Une voix est un fournisseur et un slug** (`eleven-us-eric`, `azure-us-jenny`). Rien d'autre n'est nécessaire pour la nommer.

**Deux rôles, deux listes, et ils ne se croisent jamais.** La **voix de référence** — celle qui dit la phrase de l'apprenant, donc l'étalon de toute la mesure — est choisie de son côté et doit passer l'étalonnage (`reference.md`). Les **voix de personnages** ne passent aucun test, puisqu'elles ne mesurent rien : le personnage dit *ses* tours, le modèle à imiter dit *la phrase de l'apprenant*. Un personnage peut donc avoir n'importe quelle voix, y compris une qui échoue à l'étalonnage, et une voix difficile à suivre devient un levier de compréhension. En échange, **la voix de conversation cesse d'être un réglage global** et devient un champ de l'activité, que la distribution porte.

**Deux sortes de voix, et deux modes qui coexistent.** Les **génériques** — quatre en première implémentation, deux d'homme et deux de femme, choisies par l'apprenant chez son fournisseur — sont **interchangeables** : une définition générique demande *une générique*, éventuellement d'un genre, et n'en nomme aucune. Les **spécifiques** viennent du projet et se nomment par leur slug. Les deux modes sont (a) l'apprenant met sa clé et n'a que le générique, (b) il achète des crédits et a tout.

**C'est ce qui tient l'invariant sans machinerie.** Un défi n'écrit jamais `eleven-gb-daniel` en dur : ce qu'un fournisseur expose ne peut pas devenir une condition, sinon le défi meurt le jour où on change de clé. Une définition générique ne portant aucun nom, il n'y a rien qui puisse mourir avec une clé, et aucun catalogue à interroger. Et **l'invariant ne s'applique pas aux spécifiques** — son problème était le roulement d'un tiers, et un slug sur un fournisseur que le projet tient est stable par construction.

**Ce n'est pas un levier** : entre deux voix il n'y a ni dur ni facile, et ses positions arrivent d'un catalogue qui change avec la clé. C'est un champ, qu'aucun patch ne déplace.

### Les briques

Construire une activité en choisissant des briques — un lieu, un personnage, une situation —, chacune avec son texte libre. Aucune pièce neuve : une brique apporte des fragments de `brief`, des positions de leviers et des règles, donc c'est un **préréglage nommé**.

Ce qui compte est le **périmètre**, et cette coupe est **une première proposition, pas une décision** : **Lieu** possède le bruit et la qualité du canal ; **Personnage** l'exigence de voix et les leviers de sa parole ; **Situation** les conditions de fin ; **Règles** les feuilles notées, les poids, les sensibilités et les règles.

**Deux briques qui voudraient le même levier signalent que la coupe est fausse**, pas qu'il faut une règle de priorité. Leurs textes libres n'entrent en conflit avec rien : ils ne se lisent pas, ils se comprennent.

## La capture

**La capture est un levier de fluidité, à trois positions**, et l'échelle gradue exactement ce que la fluidité peut lire.

1. **Ouverture à la main, avec pause, envoi manuel.** On appuie pour parler ; la pause et l'envoi sont deux boutons à part, en bas (`ui.md`). Aucune mesure de silence n'est possible — entre deux segments, l'écart mesure le pouce.
2. **Armement automatique, sans pause, envoi manuel.** Le micro s'ouvre dès que l'IA a fini et reste ouvert jusqu'à l'envoi. Le **délai avant de parler** et les **silences intérieurs** deviennent mesurables, ce qui est exactement ce que retirer la pause achète.
3. **Armement automatique, sans pause, envoi au clic ou sur un silence de plus de x**, silence du début compris. Le clic reste le geste normal ; un tour que personne n'envoie est **interrompu**, et c'est ce que cette position demande, de la réactivité. Aucune mesure neuve, et aucun silence n'y dépasse x.

**Tout s'ouvre et s'envoie du même geste aux trois positions : un appui.** La première se tenait au doigt maintenu, ce qui la faisait diverger des deux autres sur le geste et non sur ce qu'elle mesure, et obligeait chaque bouton servant dans les deux régimes à porter deux comportements que rien à l'écran n'annonçait. La bascule garde les segments à l'identique — la position 1 séparait déjà des segments par des pauses commandées au pouce — elle change qui les délimite. **Ce qui distingue les positions n'est donc plus le geste mais deux faits** : qui ouvre le micro, et si la pause existe. La pause est ce qui interdit de lire les silences, donc elle vit là où ils ne se lisent pas, et nulle part ailleurs.

**Le silence final d'un tour que l'horloge a envoyé vaut exactement x, et il compte.** Sur la même parole de quatre secondes, la continuité rend 23,8 à seuil 3 s, 39,7 à 5 s et 59,5 à 10 s, contre −4,8 quand la main envoie, et `le plus long silence` vaut le seuil à chaque tour. Ce n'est pas un artefact à retirer : le levier demande précisément de **ne pas aller jusqu'à x**, donc s'y rendre est un fait sur celui qui parle — et la position ne s'agrège de toute façon avec aucune autre.

**Un tour interrompu est tronqué et envoyé tel quel** : ce qui restait à dire n'est jamais capté, le micro ne se rouvrant qu'après la réponse de l'IA. Il n'est pas coupé en deux tours.

**Chaque tour porte sa position de capture** — sans quoi rien ne dit si ses silences sont significatifs — et **comment il s'est fini**, envoyé ou interrompu et par laquelle des deux horloges. Ce second fait a deux lecteurs : le modèle de langue, à qui il interdit de compléter une phrase inachevée, et la feuille du tour interrompu.

**Le micro ne s'arme jamais avant la fin de la réponse de l'IA**, et un symbole est visible dès que ça enregistre — il n'informe pas seulement, savoir que ça tourne change la façon dont on parle. **Les deux décomptes sont visibles, toujours**, celui du silence et celui de la durée maximale ; ce n'est pas un levier.

**Et il ne s'arme jamais tout seul tant qu'un passage attend une reprise.** Le passage se ferme à l'appui sur le gros bouton, *l'app n'ayant rien à deviner de ce qui vient d'être dit* — or l'armement automatique supprime l'appui, donc le fait. En « poursuit », l'app recevrait une prise sans savoir si c'est la tentative suivante ou un tour neuf, et rien ne trancherait. Suspendre l'armement recrée l'instant de la décision, et seulement là où il avait disparu : en « attend », tout ce qui se dit est déjà une tentative. Ce qui se voit alors : le flux s'arrête, la ligne d'état porte les deux issues et le compte restant, le petit bouton reprend, le gros passe. Le silence du micro est le signal, et il n'a pas besoin d'être doublé par une fenêtre à écarter.

**Une prise se jette avant d'être envoyée, et c'est un levier.** Le geste existe depuis le début : on parle, on relâche, on jette au lieu d'envoyer. Rien n'est parti, rien n'a été mesuré, aucune tentative n'est dépensée — sans lui, la seule sortie d'un raclement de gorge serait de l'envoyer. Mais librement offert, il rend le compte des tentatives contournable : dans un défi qui n'en donne qu'une, on recommence dix fois en jetant chaque prise. Un défi doit donc pouvoir le fermer, ce qui est exactement la définition d'un levier. Il est **sans objet en envoi au silence**, la seule position où une horloge envoie aussi : le silence pendant lequel on hésite à jeter est ce qui envoie la prise.

**La durée d'un tour est une seule variable, et le plafond technique en est la valeur maximale admissible.** Imposer de répondre en cinq secondes et supporter trente secondes au plus sont la même chose réglée différemment. Le plafond est technique — la mémoire d'une passe d'analyse croît comme le **carré** de la durée, d'où 30 s (`analysis.md`) — et il remonte quand le fenêtrage arrive ; le levier reste. Dans les deux cas le tour se ferme et envoie ce qui a été dit : jeter perdrait de la parole.

## L'audio

**Un tour est une liste de segments** : une durée de silence, ou de l'audio. Le silence n'est jamais stocké en échantillons — il ne coûte alors que sa durée, là où le garder ferait passer une conversation de vingt minutes à des dizaines de mégaoctets. On reconstruit l'audio d'origine quand on en a besoin.

Chaque segment de parole garde une **marge de vrai audio** de part et d'autre : ce qui identifie une occlusive vit dans la transition, et couper au ras de la parole abîmerait la mesure.

**On n'envoie pas de silence au réseau.** Ce que chaque mesure lit exactement — l'audio brut, les segments, la reconstruction — est à préciser mesure par mesure.

**Ce qu'on déduit de l'audio se stocke.** La purge efface l'audio ; une mesure qui ne se recalculerait plus après doit donc exister ailleurs.

**L'audio n'est pas purgé par défaut, et la purge est à écrire.** À ne pas confondre avec le balayage des enregistrements que plus aucun énoncé ne nomme, qui est fait et n'est pas une purge (`reference.md`).

## L'habillage rétro

Le rétro touche aussi le marquage, qui est un instrument de précision — une rampe à nombreuses nuances, trois canaux graphiques superposés, une vaguelette qui doit se distinguer d'un filet droit au même endroit. Ça se refait **une fois**, en gardant la lisibilité ; ce n'est pas une contrainte permanente sur la conception (`ui.md`).
