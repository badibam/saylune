# Le modèle d'activité

Comment une séance est décrite, réglée, jouée et close.

**Ce qui est écrit ici et ce qui est écrit dans le code.** Le code porte **ce qui est** — les leviers avec leurs positions et leurs défauts (`levers/Levers.kt`), les sortes de règle et leur résolution (`rules/`), les deux portes (`conversation/Gates.kt`), les quatre parties du prompt (`providers/ConversationPrompt.kt`) —, et chaque fichier le dit dans son en-tête. Ce doc porte ce qu'un fichier ne peut pas dire : **ce qui traverse plusieurs pièces**, **ce qui n'a pas encore de code**, et **les portes fermées** — les formes essayées puis écartées, qu'un commentaire n'a pas à raconter et qui coûtent une journée chaque fois qu'on les rouvre. Là où le code fait autorité, ce doc énonce la règle et nomme le fichier ; il ne recopie ni une liste ni une valeur.

Le reste est dans `reference.md` pour l'énoncé, l'activité et ce qui se stocke ; dans `measures.md` pour ce qui se mesure et ce qui en fait une note.

**Un nom d'usage se pose sur des axes, il ne les remplace jamais.** Le curseur d'aptitude est un nom sur un jeu de positions de leviers ; « arcade » est un nom sur une combinaison d'axes ; une brique de scène aussi. À chaque fois, prendre le nom pour le modèle obligeait à décrire deux fois la même séance, et rendait impossible une combinaison légitime — une campagne ne pouvait pas contenir un niveau d'arcade, les deux étant des valeurs du même champ. C'est la règle à appliquer au prochain paquet qui se présentera sous un nom rond.

## La définition, l'exécution, le bloc

Une **définition** est une activité écrite d'avance, et c'est de la **donnée** : un fichier livré avec l'app, autoportant, lu tel quel (`activity/Definition.kt`, `Definitions.kt`, qui portent la forme et les trois raisons de ce choix). Une **exécution** est une ligne en base, la seule des trois qui se stocke. Un **bloc** est un groupe nommé de définitions, avec une seule propriété : l'accès y est **dans l'ordre**, c'est une campagne, ou **libre**, c'est un recueil de défis. Une forme, un drapeau — mais **deux portes distinctes dans l'app**, « poursuivre la campagne » et « choisir un défi » n'étant pas le même geste.

**Un fichier porte ses traductions, en table langue → texte.** Une définition mélange deux langues par construction : le `brief` et la mise en scène partent au modèle en anglais, ce que l'apprenant lit est dans sa langue. Un fichier par langue dupliquerait la prose anglaise et la ferait diverger ; des clés vers `res/` casseraient l'autoportance, donc l'argument du partage. C'est un **écart délibéré à la facette `android`**, déclaré au manifeste : sa norme d'i18n vise le texte d'**interface**, et la prose d'une scène est du **contenu** — aucun jeu ne livre ses dialogues en ressources de plateforme. Le prix est réel : une plateforme de traduction ne lit pas ce format.

**Trois champs d'affichage s'ajoutent, et ils viennent d'une contrainte d'écran** : le **nom** de l'activité, le même **coupé à dix caractères** pour la ligne d'état, et un **nom court** par personnage. Les deux premiers sont deux lecteurs à deux budgets, pas un champ et son repli — les deux se tronquent de toute façon, et les déclarer laisse l'auteur choisir ce qui survit à la coupe (`ui.md`).

**Une définition est un gabarit appliqué à la création, pas une dépendance gardée ensuite.** Les positions de leviers sont donc **toujours sur la ligne**, y compris pour une exécution issue d'une définition ; l'alternative — un pointeur ici, des valeurs là — ferait d'un même champ tantôt l'un tantôt l'autre. **L'origine est un champ à part** — quelle définition et quelle version — et elle ne sert qu'à grouper : ouvrir le niveau suivant, ranger un score. Sans la version, une mise à jour qui corrige une scène rendrait incomparables deux parties qui se croient les mêmes.

**Rien de tout ça n'est en base** : ni table de blocs, ni table de campagnes, ni table d'avancement. **L'avancement se dérive des résultats** — un niveau est ouvert si les résultats des précédents le disent ; stocké, il serait une deuxième source qui se décale. Le déverrouillage lit la note de la séance au seuil de partout, A ou B : un bloc qui veut être plus dur monte la sévérité de ses réglages, il ne déplace pas la barre.

**Une exécution issue d'un bloc n'est pas jetable** : son résultat porte l'avancement, donc la supprimer reverrouille un niveau ou vide une entrée de classement. Une conversation libre se jette sans conséquence. C'est le prix du choix de dériver l'avancement, et il se dit à l'utilisateur au lieu de se faire en douce.

## Les quatre portes

**« Mode » n'est pas un champ** : c'est une **couche d'accès, écrite en dur, côté écran** — la façon d'arriver à une définition. Le même moteur tourne derrière toutes. L'app s'ouvre sur un écran-titre qui les présente.

**La définition dit quand même par quelle porte elle est offerte, et ça ne contredit pas la phrase ci-dessus.** Cet invariant a été écrit contre un champ **de comportement** — un nom rond pris pour le modèle, qui obligeait à décrire deux fois la même séance et rendait impossible qu'une campagne contienne un niveau d'arcade, les deux étant des valeurs du même champ. Sa portée est le comportement. Le champ `door` n'en est pas : le moteur n'en fait rien, aucune condition ni aucun effet ne le lit, c'est un fait de **livraison** — quelle grille affiche ce fichier. Il s'appelle `door` et pas `mode` délibérément, ce doc ayant réservé ce mot et l'ayant refusé. Une porte est un endroit par où l'on entre, pas une façon de jouer.

- **Histoire** — un bloc à accès **ordonné**. Chaque scène est une définition, les questions déclarées portent la mémoire d'une scène à la suivante.
- **Défis** — un bloc à accès **libre**, chacun avec ses poids, ses consignes, ses règles, et une issue.
- **Arcade** — quatre définitions d'*easy* à *extreme*, rejouées sans fin, une **rampe** qui durcit en cours de partie, la fin à zéro vie, un **score** par difficulté plutôt qu'une réussite. Un score ne se compare qu'à ceux de la même difficulté.
- **Libre** — **aucun enjeu** : pas de vies, pas de règle de fin, pas d'issue, aucune lettre. Ce n'est pas *sans réglages* : ses leviers sont réglables et posés par des préréglages ; c'est qu'aucun **écran de configuration** ne les expose en V1. L'entrée de la barre d'actions reste, grisée en portant sa raison, pour que la barre ne change pas de forme ensuite.

**Ce qui fait qu'une conversation est « libre » est l'absence d'enjeu, pas l'absence de mécanique.** Elle laisse tous ses leviers ouverts à l'apprenant, son `brief` vient de lui, et elle peut porter des règles comme n'importe quelle autre définition — et c'en est une, livrée avec l'app. Ça retire un « par défaut » qu'il aurait fallu câbler ailleurs, et surtout le besoin de dire « hors défi » : **« défi » n'est pas un terme du modèle**, c'est un nom d'usage. Ce qui distingue deux activités est leurs réglages, leur arbre et leurs règles, rien d'autre.

**Ce que la couche coupe, ce sont les lettres, jamais les mesures.** Un mode qui ne montre pas les notes montre quand même *2,3 demi-tons*, *47 sons sur 50*, *un mot mal formé* — des faits qui ne dépendent d'aucune sensibilité. **Et c'est là, et pas dans un levier, que se décide si les notes se montrent** : voir sa note n'est ni plus dur ni plus facile, donc le test du levier le refuse, et ce n'est pas non plus un champ d'activité, c'est de l'écran. En conversation libre, le défaut est de n'afficher que les marques.

**Ce n'est pas purement cosmétique pour autant** : la couche décide **quelle définition part**, et pour une campagne elle dérive quel niveau est ouvert. Peu de code, mais pas un drapeau.

**Et les libertés d'un mode ne sont pas un mécanisme à lui.** La liste des libertés d'un mode est la liste des leviers que sa définition laisse ouverts, plus les gestes qui ne sont pas des leviers.

**L'appui sur le texte de l'IA en est un cas, et il ne passe pas par l'exposition d'un levier mais par une position de plus.** `ai-turn.display` a cinq crans, et le second est *brouillé, découvrable au doigt* — le défaut. Découvrir à la main est exactement une aide entre le texte qu'on lit sans le demander, qui préempte l'écoute, et le brouillé derrière lequel on ne peut pas passer : l'oreille reste le canal, et l'œil coûte un geste voulu. Un défi qui veut de la vraie compréhension orale pose le cran d'en dessous, et il n'y a rien à fermer à côté. La découverte vaut **pour un tour**, jamais pour la séance : elle ne déplace pas le levier, sinon un appui changerait le régime de tout ce qui suit.

### Les thèmes et les trous

**Derrière la porte « Libre », des thèmes — et un thème est une définition.** *« À la fin d'une conférence, tu engages la discussion avec l'intervenant »* est une situation, qui est l'intervenant est une mise en scène, tous les leviers restent ouverts. Quelques situations préenregistrées sont donc quelques fichiers, et rien de neuf. Le choix *personnalisé laissé vide* est la définition qu'on livre déjà, dont la situation est vide.

**Ce qui est neuf est le trou.** Une définition peut déclarer des **emplacements** que l'apprenant remplit au lancement — une clé, une question dans sa langue, deux ou trois mots. Trois choses les gouvernent. Le **même trou apparaît des deux côtés du `brief`** et une seule réponse les remplit ensemble : écrits séparément, la situation et la mise en scène dérivent dès le premier lancement — une conférence sur la neurophysique et une sur la théorie du genre ne font pas le même intervenant. La réponse est **substituée à la création** et copiée sur la ligne, donc la séance se lit seule. Et elle se tape dans **la langue que l'apprenant veut** : ce qui compte est qu'il décrive ce qui l'intéresse, pas ce qu'il sait écrire en anglais.

**Le genre se demande de la même façon, et sans rien déclarer.** L'écran de lancement d'une libre porte toujours le champ court et le **genre**, dont le défaut est *peu importe* — qui n'est pas un troisième genre mais l'absence de contrainte, et fait **tirer au hasard** parmi les génériques disponibles. Rejouer un thème ne redonne donc pas la même personne, ce qui sert la rencontre ; le tirage entre au journal et la voix tirée est copiée sur la ligne. **Ce qui dit qui décide est la présence du champ** : une définition qui déclare un genre l'impose, une qui n'en déclare pas se le fait demander. Pas de drapeau *demandé* à écrire.

**Ce que le trou ne porte pas, le modèle le remplit.** La mise en scène fixe porte le **rôle** — un conférencier qui vient de parler, fatigué, flatté qu'on s'intéresse — et le trou porte le **sujet**. Le registre qui va avec est de la prose que personne ne relit, donc gratuite. Ce qui reste au travail de l'auteur est d'écrire la mise en scène pour que le trou porte assez.

## Les prescripteurs

Un **prescripteur** est ce qui remplit une activité — sa matière et ses réglages. Il y en a trois : **la conversation**, à partir de ce qui vient de s'y passer ; **l'apprenant**, par choix ou consigne libre, la voie par laquelle de la matière neuve entre ; **la progression**, pour ce qui est dû, jamais réussi, ou sur le point d'être oublié. Un quatrième viendra peut-être, le contexte global ; ajouter un prescripteur n'est qu'une façon de plus de remplir les mêmes champs.

Un prescripteur ne décide rien pendant : il remplit des champs avant, puis se tait. Les trois sont nommés en code et **seul l'apprenant en est un pour l'instant** ; les deux autres attendent le catalogue des activités.

## L'état d'une séance

**Trois natures, et il n'y en a pas de quatrième** — le critère du projet appliqué à l'état entier.

- **Déclaré** — l'entrée figée. De la définition : l'origine, le `brief`, la distribution, les consignes de départ, les positions de départ, l'arbre des poids, les règles, les questions. De l'apprenant : son **nom**, sans lequel aucune scène ne peut le désigner ; l'accent, donc la voix de référence ; les fournisseurs choisis par maillon.
- **Enregistré** — les énoncés et leur audio, les mesures de chaque tentative, le journal de ce qu'un tirage ou l'IA a choisi, et les faits portés par chaque tour.
- **Dérivé** — les positions effectives, les consignes en vigueur, les feuilles, les notes, les comptes, le blocage, les deux portes, l'avancement, l'issue, les deux horloges.

**Une quatrième a été essayée et ne tient pas.** « Éphémère » devait ranger ce qui n'existe que le temps d'un tour ; aucun candidat ne s'y range — les horloges laissent une trace enregistrée, le menu se recalcule, le message d'une règle est écrit dans la règle donc déclaré. Durer peu n'est pas une nature.

**La position déclarée d'un levier et sa position effective sont deux choses**, et les confondre fait paraître contradictoire que les réglages soient fixes pendant que les vies se perdent. La définition écrit la première ; les patchs déplacent la seconde, qui est ce que l'apprenant vit.

## Les réglages

Ce qui se stocke, ce qui fait un levier et le catalogue complet sont dans `levers/` — `Positions.kt` pour ce qu'une séance garde, `Lever.kt` pour le test qui range ou écarte, `Levers.kt` pour la liste. Ce qui vaut d'être dit ici est ce que ces fichiers ne portent pas.

**Ce qui se règle sans passer par un levier est un préréglage** — un nom donné à un paquet : des positions, et éventuellement des fragments de consigne et des règles. C'est le mode custom qui le prouve, puisqu'il expose chaque levier séparément : si les préréglages étaient le modèle, il faudrait une deuxième façon de décrire la même séance. Un préréglage est un **gabarit appliqué à la création**, comme une définition ; ce qui reste sur la ligne, ce sont les positions.

**Le curseur d'aptitude est un préréglage parmi d'autres, pas la forme générale.** *Élocution 2* en est un ; *easy* et *hard* en arcade en sont d'autres, qui nomment une combinaison entière ; une brique de lieu ou de personnage en est un troisième, qui apporte aussi de la consigne et des règles. Un préréglage peut déplacer des **sensibilités** en plus des aides — c'est ce qui rend *durcir* exprimable — et poser des **poids** ; ce qui reste interdit est qu'un **patch** en déplace un pendant la séance. **Quels préréglages chaque mode offre reste à écrire**, et rien n'oblige un mode à en avoir un par aptitude.

**Un curseur d'aptitude ne pose que de la pression** — les aides retirées et la sévérité —, jamais le poids de son aptitude. Ce sont deux questions : *combien j'exige* et *sur quoi on regarde*, et on peut vouloir une élocution qui compte beaucoup mais jugée avec indulgence.

**Une activité est une conversation, et « format » n'est pas un axe.** Ce qu'on rangeait sous d'autres formats existe comme des moments *dans* la conversation : la lecture à voix haute **est** redire, un texte de référence connu d'avance ; la répétition après un modèle **est** le remède de toute faute sonore. Un format neuf aurait dupliqué cette machinerie. Le jour où une vraie deuxième forme d'échange se présente, c'est une migration, manuelle et explicite.

**Ce qui ne se règle pas par un levier doit se dire là où on serait allé chercher le levier.** L'écran custom montre des leviers : qui veut imposer un registre y cherchera un curseur, ne le trouvera pas, et conclura que ça ne se fait pas. Le catalogue donné au modèle a le même trou en pire — il inventera un levier. Il faut donc une entrée pour la consigne elle-même, disant ce qu'on peut y mettre.

**Trois découpes qui ont déjà eu lieu, et la règle qui les a faites** : zéro éteint le levier quand c'est vrai, sinon deux leviers. Zéro réécoute *est* la réécoute interdite. Mais aucune valeur du seuil de silence ne veut dire « pas de seuil », aucune cadence ne veut dire « pas de cadence », zéro vie met fin — donc chacun de ces trois s'écrit en deux leviers, un interrupteur et un nombre. Trois cas sur trois est un signe que la règle est la bonne.

**Chaque levier déclare qui le tient : l'app, ou le modèle.** Les *exécutés* sont vrais parce que l'app les fait ; les *demandés* sont écrits dans le prompt et rien ne vérifie qu'ils sont tenus. Trois conséquences, et aucune n'est un détail : une **condition ne peut pas lire un levier demandé**, faute de fait à lire ; le **banc ne peut pas l'éprouver** ; et sa **notification annonce quelque chose qui pourrait ne pas arriver** — on l'affiche quand même, se taire laisserait l'apprenant sans rien pour comprendre ce qui a été tenté, et ça dit la vérité à qui écrit un défi : monter la complexité n'est pas une garantie, c'est une demande.

**L'écran d'avant-partie tombe de tout ça sans rien coûter.** Chaque position déclare sa phrase lisible, donc l'écran qui montre ce qui attend l'apprenant **se génère** : ce qu'on lui demande, ce qu'on lui enlève, ce qui lui fera perdre, ce qui met fin, les consignes. Zéro texte par activité. **Ce qu'il montre exactement reste ouvert** : « ce qui s'écarte du défaut » ne marche pas, il n'y a pas d'ordinaire à quoi comparer.

## Les règles

La forme — **quand**, **quoi**, **qui choisit** —, les huit sortes de déclencheur et les trois sortes d'effet sont dans `rules/Rule.kt` ; la résolution par vagues et sa preuve de terminaison dans `rules/Engine.kt` ; ce qu'une condition a le droit de lire dans `conversation/World.kt`. Ce qui suit est ce qui traverse.

**Ce que la forme a replié.** La **rampe** cesse d'être un champ : c'est une règle dont le quand est « tous les N passages » et le quoi un cran de plus. Les **conditions** aussi : « une feuille sous la barre → une vie » est une règle dont le patch retire une vie. Deux champs deviennent une liste.

**Ce qu'un tirage ou l'IA a choisi s'écrit sur l'activité.** Sans ce journal, l'état effectif ne se recalcule plus, et une séance ne se compare plus à elle-même trois semaines après. Garder une graine coûterait moins cher et ne rejouerait juste que si le code n'a pas bougé.

**Un patch porte des positions ou des déplacements, et les deux formes sont nécessaires.** `lives.left ← 1` de la mort scriptée ne s'écrit qu'en position ; « perdre une vie » ne s'écrit qu'en déplacement, l'auteur ne sachant pas combien il en reste quand la règle part. **Un déplacement s'arrête à la borne du levier**, ce qui n'est pas une erreur, et **ce qui n'a rien déplacé ne notifie rien**.

**Annoncer dit aussi le sens du changement.** « Deux tentatives permises » ne dit pas si on vient de monter ou de descendre, et ce n'est pas la même nouvelle.

### Les deux phrases d'un patch

**La phrase mécanique est déclarée avec le levier**, donc une seule fois pour toute l'app. **La phrase de mise en scène vit sur le patch**, parce que « cinq secondes de silence » ne peut pas se dire *le barman s'impatiente* dans un pub et *le recruteur attend* dans un entretien : la face qui joue la scène dépend de la scène.

**La mécanique se lit après la réplique, la narrative dit laquelle des deux places elle prend.** *« Il te reste une vie »* constate, donc c'est un reçu et il vient après ; *« tu te fais bousculer par un passant »* plante le décor du tour qui suit, donc lu après la réplique il fait tomber l'excuse de nulle part. D'où un drapeau **avant / après** sur la phrase de mise en scène, par défaut après.

**Les deux s'affichent, et la narrative ne remplace jamais la mécanique.** Qui ne lit que *« le barman semble pressé »* ne sait pas que son tour part maintenant tout seul au bout de cinq secondes, et croira à un bug la première fois. La mécanique est obligatoire — une conversation libre n'affiche qu'elle, n'ayant pas de fiction — la narrative est facultative. Les deux vont dans le même sens : *le barman se détend* sur un patch qui durcit est un mensonge, et la direction permet de le voir à l'écriture.

**Elle n'est pas produite par le modèle** : elle s'affiche à côté d'une affirmation mécanique, donc un texte inventé au moment où ça tombe peut la contredire. Le modèle garde tout le reste — il **joue** le changement dans son tour, où il est libre et où c'est gratuit, et ce qui doit changer d'une partie à l'autre s'écrit en **message au modèle**.

**Ce qui manque se voit, mais on ne peut pas dire ce qui pourrait manquer.** Le catalogue ne propose aucune liste complète des occasions de parler, et il ne le peut pas — une condition sur un élément est un espace paramétré, et le déclencheur *le modèle juge que oui* est de la prose libre. Ce qui se dénombre est ce qu'une définition a **écrit**, et c'est précisément ce qui sert un auteur : l'écran d'écriture affiche ses douze règles et dit lesquelles n'ont pas de phrase de mise en scène. Pas « voici tout ce qui peut arriver », mais « voici ce que tu as écrit, et voilà ce qui est nu ».

### Les trois familles, et le drapeau qui s'est replié

**Ce qu'un patch pose a deux propriétés indépendantes, une direction et une phrase.** Trois familles en sortent, et la quatrième combinaison n'existe pas.

| | direction | phrase | ce que c'est |
|---|---|---|---|
| **levier** | oui | oui | la notification dit *ça se durcit* et lit la phrase |
| **consigne**, **interrupteur de condition** | non | oui | la notification lit la phrase, sans direction |
| **drapeau** | non | non | rien ne s'affiche ; ça se pose et ça se lit |

**Un interrupteur de condition n'est pas un levier** : « active est le côté dur » est faux depuis qu'une condition lit les deux moitiés de l'échelle — *« atteint A → gagne une vie »* est une condition dont l'activation **allège**. Il garde sa phrase, parce que *« les silences ne coûtent plus rien »* est une nouvelle qu'il faut donner, et il perd sa direction, que personne ne peut qualifier.

**Trois objets à ne pas coller.** L'**interrupteur** porte deux positions et chaque position porte sa phrase ; le **patch** est ce qui le déplace ; la **notification** lit la phrase de la position où on vient d'arriver. Il y a donc deux règles en jeu : celle dont le patch déplace l'interrupteur, et celle que l'interrupteur arme ou désarme.

**Le drapeau se replie sur l'armement.** Il devait être un événement nommé qu'un patch pose et qu'un déclencheur lit — mais **aucune des huit sortes n'en lit un**, et la liste est fermée. L'armement fait déjà le travail. Ce qui reste à dire est si la famille garde un nom à elle (`../TODO.md`).

**Un drapeau ne se compte pas** : compter un événement qui n'a pas son levier — *la troisième fois qu'il mentionne le dragon* — s'écrit en **chaîne de drapeaux**, une règle par cran, la première armant la deuxième. Verbeux, et écrivable ; un compteur générique attend d'avoir été demandé par une scène réelle.

**Une règle ne se retire pas ; ce qu'elle a fait se défait.** Alléger est un patch comme un autre, et une règle qui ne doit plus s'appliquer se **désarme par son interrupteur**, qui porte sa phrase — pas un second étage muet où plus personne ne saurait ce qu'une définition fait sans l'exécuter. **C'est aussi ce qui écrit la surcharge** : une même occasion qui produit une chose la première fois et une autre ensuite s'écrit en deux règles dont la première désarme l'une et arme l'autre. Ni plafond de déclenchements, ni ordre de déclaration qui aurait un sens.

```
R1  (armée au départ)
    quand lives.left atteint 0
    → [ message au modèle : la fausse mort,
        patch : lives.left ← 1, R1 ← désarmée, R2 ← armée ]

R2  (désarmée au départ)
    quand lives.left atteint 0
    → [ finir, issue = raté ]
```

### Les cinq moments

Ils ne se distinguent pas par le goût mais par **ce qui est calculé à cet instant**. Trois arrivent pendant la séance et reviennent ; les deux autres la bornent et n'arrivent qu'une fois chacun.

- **À l'ouverture.** La séance vient d'être ouverte et rien n'a été dit. Rien n'est calculé : aucune horloge ne tourne, aucune feuille n'a été lue, aucun passage n'est fermé.
- **Pendant l'enregistrement.** Deux horloges tournent, le temps écoulé et le silence en cours. Aucune feuille n'existe encore — donc une règle de ce moment ne peut lire qu'une horloge. Ce n'est pas une restriction posée, c'est un fait sur ce qui existe.
- **À la fin d'une tentative.** Le tour est parti, le modèle a répondu, l'analyse a tourné. Se décide là ce qui concerne cette tentative : les deux portes, et si la conversation attend.
- **À la fermeture du passage.** La note du passage est celle de la dernière tentative et le compte est connu. Tombe là tout le reste : les patchs, la rampe, les vies, la fin.
- **À la clôture.** La séance est finie et tout est arrêté. **Une coda, pas un sursis** : ce qui s'y déclenche peut ajouter un dernier mot et faire répondre aux questions restées ouvertes, et ne peut plus dé-finir.

**Aucune sorte de déclencheur ne lit une horloge hors de l'enregistrement, et c'est une borne choisie.** *« Après deux minutes de conversation »* n'est pas écrivable : le système est par tours, donc une durée de séance ne gouverne rien qui puisse se déclencher entre deux paroles.

**Les deux déclencheurs de borne ne testent rien**, et c'est ce qui les distingue des six autres : atteindre le moment est tout ce qu'ils disent. Ils n'ont pas de paramètre, et leur moment se déduit comme celui de l'horloge. Une règle *à l'ouverture* n'a pas besoin de se désarmer, l'ouverture n'arrivant qu'une fois.

**Ce qui ouvre une séance est donc une règle et non un champ** (tranché le 2026-09-07). Ce doc a longtemps dit l'inverse, et l'argument tenait entièrement à l'existence du champ : `Definition.opening` portait un paquet d'effets, donc un déclencheur d'ouverture aurait été une seconde façon d'écrire la même chose. Le champ n'a jamais été joué et il est **supprimé** au lieu d'être implémenté — et plusieurs règles peuvent contribuer à une ouverture là où un champ unique ne portait qu'un paquet. Les trois façons d'ouvrir — l'apprenant parle en premier, le personnage dit une réplique écrite, le modèle improvise sur consigne — sont trois façons d'écrire cette règle. Rien ne pousse vers l'une des trois pour la latence : l'apprenant vient d'appuyer sur *lancer* et n'attend pas sa propre réponse.

**Une règle se résout avant que l'IA réponde**, parce qu'elle doit pouvoir fabriquer l'occasion de la contrainte qu'on vient de poser. Le cycle est donc : le passage se ferme, les règles se déclenchent, l'IA répond en connaissant ce qui a changé, son tour est dit, la notification s'affiche hors du temps de parole, le micro s'arme. Quand c'est l'IA qui choisit, **elle choisit et répond dans le même appel** — deux sorties, pas deux allers-retours. Contrepartie : elle choisit en sachant ce qu'elle a envie de dire, et le menu est ce qui borne ça.

### Ce qu'une condition lit

**Une condition est une règle**, pas un mécanisme à part : son déclencheur lit l'arbre des notes, son effet est un patch. **C'est donc un terme étroit**, une des huit sortes, et il faut s'y tenir : « les conditions de fin » sont des règles et pas des conditions, et confondre les deux fait affirmer d'une règle quelconque ce qui n'est vrai que de celle-ci.

**La lecture par élément n'a pas de dénominateur, et c'est voulu** : c'est le seul endroit du projet où un fait absolu se lit. « Au moins un mot `à côté` » se déclenche pareil sur trois mots et sur trente ; qui veut la proportion lit le chiffre. **Sur un marquage, l'élément est le mot et jamais l'empan** — un empan a toujours au moins un mot, donc les deux se déclenchent dans les mêmes cas, et sa **longueur** n'est pas lisible, dépendant de l'habitude de coupe du juge.

**Une condition sur un cran fréquent se déclenche presque toujours.** « Au moins un mot `plat` » est vrai à chaque passage ou presque. C'est un conseil à qui écrit un défi, pas un interdit.

**La lecture par note suit la sensibilité**, quand les deux autres portent leur seuil et ne bougent pas : monter la sévérité rend la condition plus fréquente sans qu'on la touche, et c'est un service — un défi dit « plus dur » d'un seul geste.

**La barre A–B ne borne pas les conditions, et l'y avoir enfermées était une erreur de portée.** L'invariant qui fixe la barre a été écrit pour la **lecture d'un résultat** — l'activité est-elle réussie, le niveau s'ouvre-t-il. Une condition n'est pas un résultat : *« quand il tombe en D, le barman fronce les sourcils »* ne se compare à rien et ne débloque rien. Son seuil est de la même famille que les cinq secondes de silence. **Ce qui reste à la barre est le verdict.** Et **lire les deux moitiés de l'échelle est ce qui rend une récompense écrivable** : gagner une vie est un patch, une fin porte l'issue *réussi*, il ne manquait que de pouvoir lire la bonne moitié.

**L'accumulation vit dans l'effet, pas dans la lecture.** « Un tour interrompu coûte une vie » n'a besoin de compter jusqu'à trois nulle part : au troisième, le compteur est à zéro.

**Perdre une vie n'est jamais automatique.** Un passage non réparé ne coûte rien par soi-même : ça coûte une vie parce qu'une définition a écrit la règle qui le dit. Et les vies n'existent que là où il y a un enjeu.

## Ce que le modèle reçoit

Les quatre parties du prompt et leur ordre sont dans `providers/ConversationPrompt.kt`. Ce qui les gouverne est ici.

**Le dosage ne se décide pas sur ce qu'un modèle sait faire, mais sur ce que l'app doit relire ensuite.** Ce que **personne ne relit** est du texte libre, sans limite — comment le personnage parle, ce qu'il raconte, comment il réagit : zéro champ, zéro validation, coût nul, et c'est là qu'il faut être généreux. Ce que **l'app doit exécuter** coûte un **champ de retour**, donc un contrat de plus, une validation de plus, et un trou silencieux s'il n'est pas validé.

D'où la forme qui rend la liberté bon marché : **prose libre à l'aller, clés énumérées au retour.** L'app déclare ce qui est disponible, l'IA **choisit une clé**, elle n'invente pas — un champ, validé par appartenance, et c'est ce que les modèles font le mieux. **La borne n'est donc pas la liberté de l'IA, c'est la déclaration** : rien d'indéclaré ne peut être choisi, faute d'exécutant. **Non mesuré** : choisir dans cinq options est probablement plus fiable que dans soixante — offrir une liste courte est une prudence, pas un résultat.

**Un seul appel fait tous les métiers.** Ce n'est pas le prix qui tranche : un second appel ne coûterait rien en latence, le jugement ne servant qu'à afficher des marques pendant que la réponse se joue, ni en argent. Ce qui tranche est qu'un prompt bien structuré tient ses frontières. On regarde donc au cas par cas ce qui déteint ; on ne cloisonne pas d'avance contre un loup qu'on n'a pas vu. **L'ordre des champs de retour est la cloison qui reste gratuite** : le modèle écrit en séquence et chaque champ conditionne le suivant, donc `intended` rédigé avant que la voix du personnage soit prise vaut mieux que le contraire.

**Zéro mot retenu doit être énonçable au retour.** Sur un tour entièrement fait de remplissage et de morceaux abandonnés, il n'y a pas de phrase à reconstruire, et un `intended` en texte libre en inventerait une — le pire des retours, puisque rien ne le distingue d'une vraie.

**Le coût en latence n'est pas borné**, et il est mesuré : le contrat enrichi a triplé le maillon du modèle de langue (`providers.md`). C'est aussi ce chiffre qui dira si le second appel reste gratuit, l'argument qui l'écarte s'y adossant.

**L'état n'atteint le modèle que par la porte de devant.** Il reçoit en permanence les leviers qu'il tient — les *demandés*, qui n'existent que comme instruction — et rien d'autre. Une vie perdue, un passage raté, un seuil qui se raccourcit ne lui parviennent que si une règle a décidé de le lui dire, par un message au modèle, dans les mots d'un auteur. C'est ce qui donne à l'auteur le contrôle de ce que son personnage sait : sans règle, la faute ne change rien à la scène ; avec elle, le réceptionniste soupire. Coller l'état au prompt en permanence le ferait réagir toujours, et un réceptionniste qui lâche « il vous reste une chance » casse sa propre fiction.

**Des tours passés, on renvoie les répliques et jamais les marques.** Un modèle qui voit ses vingt derniers verdicts devient cohérent avec eux plutôt qu'avec le tour qu'il lit. Les redites restent dehors aussi : on renvoie la dernière tentative, comme l'écran. Contrepartie assumée : il ne rebondira pas de lui-même sur une faute qui revient, et une règle est ce qu'il faut pour ça. **Ce n'est pas la taille qui tranche** — trente tours tiennent dans 2 000 à 2 500 jetons, et les empans ajouteraient la moitié. Ce qui tranche est l'ancrage, et il se mesure : repasser le même tour dans deux historiques très différents, et regarder si le marquage change.

**Le détail par son ne se pose même pas** : un tour de quinze mots fait une cinquantaine de sons, plusieurs fois la taille du tour. Et tenir compte des sons pour répondre n'est pas le travail du personnage, c'est celui du prescripteur progression.

**Le `brief` se coupe en deux, et pas plus** : la **situation**, que l'apprenant lit, et la **mise en scène**, qui ne s'adresse qu'au personnage et ne s'affiche jamais. **Ce qui les sépare est qui elles situent** — la situation situe **l'apprenant, à la deuxième personne**, la mise en scène situe le **personnage**. Aller plus loin empiéterait sur les consignes. Le prix de l'appel unique est ici, et il se dit une fois : la mise en scène est dans le contexte du juge, où elle n'a rien à faire, et ce qui l'en tient à distance est une phrase disant que son critère est la consigne. Ça se vérifie au banc, ça ne se prouve pas.

## Le passage

`conversation/Passage.kt` porte ce qu'un passage est et pourquoi il se dérive au lieu de se stocker ; `TurnPipeline.kt`, où un tour en est.

**La note du passage est celle de la dernière tentative**, qui est ce qu'on sait dire maintenant. Les trois autres lectures sont pires : la **première** rend la redite sans effet, la **meilleure** laisse l'obstination atteindre A, la **moyenne** punit exactement le geste que l'app existe pour provoquer. Conséquence à dire à l'apprenant : une tentative de trop, après une réussite, peut faire baisser la note.

**C'est la dernière pour toutes les feuilles, sans exception.** Une proposition traînait ici — que la fluidité et la pertinence lisent la première, une phrase répétée n'étant plus de la parole spontanée. L'argument est juste et le prix est accepté : deux règles de lecture au lieu d'une coûtent plus qu'elles ne rapportent. **Et rien n'est figé par ce choix** : toutes les tentatives restant sous le passage, quelle tentative chaque feuille lit est un **calcul** et non un stockage.

**Une redite remplace à l'affichage, jamais en base**, et **aucun écran ne montre les tentatives précédentes** — ni le fil, ni le bilan. Elles restent en base pour les mesures et pour un banc : une tentative effacée est une mesure perdue. Ce qui dit à l'apprenant que ça va mieux est la marque elle-même, lue sur la prise du moment.

**Deux boutons, et c'est eux qui ferment le passage.** Le gros bouton dit une chose neuve ; un petit bouton posé sur la phrase la reprend. **Le passage se ferme à l'appui sur le gros bouton** — l'app n'a rien à deviner de ce qui vient d'être dit, c'est un fait d'interface. Le budget épuisé fait disparaître le petit bouton, et il n'y a pas de reprise qui ne compterait pas.

**Les tentatives s'arrêtent à la clôture**, les deux sortes. Ce n'est pas une privation, parce que **rien d'autre que ce geste ne ferme un passage** — ni la réponse de l'IA, ni le temps qui passe. Qui veut retravailler sa phrase n'a qu'à ne pas passer à la suite, et c'est le meilleur moment : la marque est à l'écran, le modèle vient d'être synthétisé. Une raison par sorte de tentative : une **redite** ajoutée à un passage clos déplacerait sa note, donc celle de la séance, sous des règles de fermeture qui l'ont déjà lue ; une **reformulation** ferait pire, elle referait cinq tours trop tard une réponse sur laquelle la conversation a déjà bâti. Le premier point vaut même là où rien n'est en jeu, donc la règle n'a pas d'exception en conversation libre. Le travail hors du fil est un autre mode, qui n'écrit rien (`../TODO.md`).

Conséquence de forme : le petit bouton n'existe que sur le **passage ouvert**, le dernier du fil. Un budget non épuisé qu'on abandonne en fermant est perdu — on était satisfait, c'est le sens du geste.

**En « attend », le gros bouton n'est pas disponible**, sinon on sortirait d'un passage bloqué en disant simplement autre chose. **Il revient quand les tentatives s'épuisent**, sinon rien n'avance — et ce qui se dit alors franchement : **« attend » ne garantit pas la réparation, il garantit qu'on dépense ses tentatives.**

**Une reformulation relance l'échange, dans les deux avances.** En « attend », l'IA n'a joué qu'un écho et sa réponse se fabrique sur la version corrigée. En « poursuit », elle a parlé : l'app **refait l'appel comme si c'était la première tentative**, la nouvelle réponse se joue, et la précédente disparaît du fil. Ce qui se paie est un tour entier de chaîne et le fait d'entendre deux réponses à une phrase presque identique ; ce qui s'achète est un fil qui ne se contredit jamais.

**Une redite ne relance rien**, ses mots étant les mêmes : il n'y a rien de neuf à répondre. Elle reste un **exercice** — la note du passage s'améliore, et l'analyse du son se remet à tourner, donc la prononciation se mesure là où un tour mal formé ne l'aurait jamais eue.

**Ça retire au projet le refus général de dédire, et le remplace par quelque chose de plus étroit.** Ce que l'app ne fait toujours pas, c'est jouer une réponse puis la contredire sur la même phrase : dans une tentative, l'appel rend la continuation et l'écho ensemble et l'app **compose**, l'écho ouvrant ce que la continuation poursuit. Ce qu'elle fait maintenant, c'est refaire l'échange quand la phrase à laquelle il répondait n'existe plus. L'une est un dédit sans cause, l'autre la conséquence d'un fait neuf. **Et ça ne fait pas doublon avec « attend »** : « attend » **force** la réparation, « poursuit » l'**offre**.

**Le nombre de tentatives se compte et ne donne pas de note** : un compte monte à chaque essai, donc pesé dans une aptitude qui fait refaire il enferme l'apprenant. Un défi qui veut la réussite d'emblée l'écrit en **condition**.

## Le blocage et les deux portes

Les deux portes, ce que chacune lit et ce qui coupe l'analyse du son sont dans `conversation/Gates.kt`. Ce qui vaut d'être dit ici est ce qui les entoure.

**Ce qui reste au choix d'une activité est de quelles aptitudes elle fait refaire, jamais de quel côté ça tombe.** Le côté est un fait sur la phrase ; rouvrir ce choix rendrait possible de faire redire une phrase qu'on va réécrire, ce que la porte existe pour empêcher. En conversation libre, une seule aptitude est à *oui* : la correction.

**Les deux lisent une note, jamais une feuille désignée par une définition.** Rien ne s'y perd : un défi qui ne veut viser que la mélodie met un poids sur la mélodie et zéro sur le reste, et « élocution sous la barre » *devient* « mélodie sous la barre ». Ça évite d'écrire deux fois le même ciblage, une fois dans l'arbre et une fois dans la porte.

**`mal formé` ne coupe pas l'analyse du son par l'élément**, et l'y mettre a été essayé : une seule faute de grammaire dans un tour de trente mots tuerait toute l'analyse, et un apprenant en fait à presque chaque tour. Il ferme par la **note**, comme les autres, avec le coût accepté — au réglage le plus lâche, le modèle dira d'une voix native une tournure qu'on a choisi de ne pas reprendre.

**On ne peut pas retirer l'empan qui ne se dit pas et synthétiser le reste**, comme on retire un *um*. Un bafouillage est **hors** de la phrase ; un empan qui ne se dit pas est **dedans** — le retirer laisserait *« I have [ ] years »*, une autre phrase, cassée elle aussi.

**Bloquer, c'est une règle dont l'effet est que le passage ne se ferme pas.** Pas un mécanisme neuf, donc branchable sur n'importe quelle feuille. **« Ne pas passer » se dit de trois choses**, à ne pas confondre : une **feuille** ne passe pas quand sa note est sous la barre, et il ne s'ensuit rien ; un **passage** ne passe pas quand une aptitude dont l'activité fait refaire tombe sous la barre ; une **activité** ne passe pas quand elle se termine sans être réussie.

**Ce n'est pas l'interruption que le projet refuse.** On ne part pas travailler ailleurs, le sujet ne change pas, l'écran ne change pas : c'est la conversation qui s'arrête sur une phrase.

**Deux axes, pas un.** L'**écho** — absent, indirect, explicite — et l'**avance** — poursuit, ou attend. Le geste fondateur du projet est la combinaison (indirect, poursuit) : *« Ah, you're 25! And where... »*. **Toutes les combinaisons s'offrent**, certaines étant seulement plus austères. Ce qui est garanti est ailleurs : **rien n'attend jamais sans qu'une raison soit visible**, et ce qui la porte est la marque, toujours là et invariante, plus la notification quand c'est le son qui bloque. Jamais l'écho, dont la position reste libre.

**Trois choses se déclenchent autour d'un passage à refaire, et elles n'ont ni la même portée ni le même décideur.**

| ce qui se passe | portée | qui décide |
|---|---|---|
| l'analyse du son ne tourne pas | la prononciation de cette tentative | automatique, dès que le passage est à reformuler |
| une reprise est proposée | l'écran | automatique |
| la conversation attend | le fil | `advance.words` ou `advance.sound` |

**Les deux dernières sont indépendantes, et ça se voit dans les deux sens.** Porte des mots fermée sans attendre est la conversation libre ordinaire. Attendre sans fermer la porte des mots est le blocage sur la prononciation : les mots ne changent pas, donc l'analyse a tourné — c'est même elle qui a rendu le verdict.

### Deux branches pour la correction, une notification pour le son

**Le verdict de correction naît dans le même appel que la réponse ; le verdict de son naît après**, l'analyse ayant besoin d'`intended`. Les deux blocages n'ont donc pas les mêmes moyens, et ce n'est pas une incohérence : c'est ce que chaque verdict rend possible au moment où il tombe.

**Correction** : l'appel rend **une réponse en deux morceaux** — l'écho de reprise (« Ah, you mean you ARE 25 ») et la continuation qu'il ouvre. Ce ne sont pas deux répliques concurrentes mais un seul énoncé coupé en deux, et le geste fondateur du projet est leur jonction : *« Ah, you're 25! And where… »*. L'app calcule la note dès le retour et **compose** ; elle ne joue l'écho **seul** que dans un cas, le passage à reformuler sous « attend ». Trois conséquences : **rien n'est jamais contredit dans une tentative** ; c'est littéralement « l'IA joue, l'app décide », puisqu'elle fournit la matière des deux issues sans trancher ; et la ligne courte **n'est produite que si l'IA a marqué quelque chose**, ce qui la rend gratuite sur un passage propre.

**Prononciation** : quand l'écart au modèle arrive, l'appel est fini. Rien ne peut fournir un écho en personnage sans un second appel, écarté pour la latence. La réponse se joue donc, et **c'est le passage qui reste ouvert** ; une notification dit de reprendre, en donnant à **écouter** le modèle. **Elle ne nomme rien** : la porte du son étant câblée sur deux aptitudes, ce qu'elle nommerait serait le même mot à chaque fois, et nommer la pire des marques serait une élection, que le projet ne fait nulle part.

**La sortie d'un passage bloqué est l'épuisement des tentatives, et c'est la seule.** Pas de geste d'abandon à part : les tentatives sont déjà un levier, donc la sortie est déjà réglable. Et l'IA **n'a rien à refabriquer** — l'appel avait rendu les deux morceaux ensemble, donc l'app joue la paire qu'elle tenait, celle de la **dernière** tentative et jamais de la première, qui répondait à une phrase qui n'existe plus. Aucun second appel, rien de rétracté. **L'écho repasse devant plutôt que d'être jeté** : la continuation nue faisait repartir le personnage comme si la conversation ne venait pas de s'arrêter sur une phrase, alors que l'écho reconnaît la sortie avant qu'on la franchisse, et il est déjà là.

## La fin d'une séance et son issue

**Finir ne peut pas être un levier** : il faudrait un côté dur, et finir n'est ni plus dur ni plus facile que continuer — c'est une porte qu'on franchit une fois, pas une position. Et le faire passer par les vies obligerait un défi « dix passages et c'est fini » à s'inventer une vie unique, donc à afficher un cœur à quelqu'un qui n'en a pas.

**Les vies à zéro mettent fin, et c'est une propriété déclarée du levier, pas une règle.** Elle s'évalue une fois par moment, après toutes les vagues, sur l'état stabilisé — ce qui laisse une règle remplir les vies dans le même moment sans que la fin tombe.

**Finir se fait en deux temps.** L'effet marque la séance comme finissante, une **vague de clôture** tourne, puis c'est fini. Ce n'est pas une nouveauté mais une généralisation : la fin par vies à zéro se comportait déjà comme ça, s'évaluant après toutes les vagues sur l'état stabilisé, ce qui laisse une règle remplir les vies dans le même moment sans que la fin tombe ; l'effet, lui, terminait sur l'instant, donc l'app avait deux sortes de fin qui ne se comportaient pas pareil. Les deux se règlent maintenant au même endroit, et ce qu'une règle déclare franchement l'emporte sur ce que disent les vies.

**La vague de clôture ne peut pas annuler la fin.** C'est une coda, pas un sursis : l'état porte déjà l'issue et rien ne la reprend. Laisser la coda dé-finir rendrait *est-ce fini ?* indéterminé pendant sa propre vague. **L'appel de clôture n'existe que si quelque chose l'a demandé** — un message qui provoque un tour, ou des questions restées ouvertes à balayer. Le dernier mot d'un personnage n'est donc jamais un comportement de l'app : c'est une règle qu'un auteur écrit.

**Une séance abandonnée n'atteint jamais la clôture**, donc ses questions restent sans réponse — et c'est sans conséquence : sans fin il n'y a pas d'issue, donc aucune scène suivante ne lit quoi que ce soit.

**L'effet « finir » porte l'issue qu'il ouvre** : *réussi*, *raté*, ou *la note décide*. **L'issue se lit dans cet ordre.** 1. **Aucune fin n'est tombée** — l'apprenant a laissé en route : pas d'issue du tout, c'est une activité à reprendre, et la note se calcule sans rien conclure. 2. **Une fin sèche est tombée** — c'est dit ; la note s'affiche et ne décide pas. 3. **Une fin ordinaire** — la note à la barre A–B décide.

**Ça bouche un trou que la note seule ne voyait pas : la quantité.** Un A sur deux passages puis on ferme, c'est une note excellente et un défi qui n'a rien prouvé. « Assez de passages » n'est donc pas un critère de réussite mais une **condition de fin**.

**Il n'y a donc pas de champ « critère de réussite »** : il se dissout en la barre A–B, qui ne se règle jamais, et ce que les règles de fin déclarent. **Pas de champ ne veut pas dire pas de mécanisme** : une réussite scriptée s'écrit comme n'importe quelle fin, un déclencheur *le modèle juge que oui* et l'effet *finir* portant *réussi*. Le prix est celui du déclencheur, qui est **demandé** — personne ne vérifie que l'objectif a vraiment été atteint.

**Un message au modèle déclare s'il attend le tour suivant ou s'il provoque un tour tout de suite.** Sans ce second cas, rien ne peut faire parler l'IA d'elle-même — or l'ouverture *est* exactement ce cas. Ça donne les événements de scène : l'alarme qui sonne, le passant qui bouscule, le personnage qui relance qui se tait. Une seule borne suffit à tenir l'invariant de capture — **un tour provoqué ne tombe qu'à l'ouverture, à la fermeture d'un passage ou dans la coda**, jamais pendant qu'on enregistre et jamais à la fin d'une tentative, où le personnage vient de répondre.

**L'arcade ne réussit ni ne rate**, elle rend un score : sa fin est zéro vie. Comment ce score se calcule reste à écrire.

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

**Monter un curseur fait deux choses qui n'ont pas le même plafond.** **Retirer une aide** converge vers le réel : pas de texte, pas de réécoute, pas de préparation, c'est la vie ordinaire. **Durcir un jugement** dépasse le réel : personne, dans une vraie conversation, ne marque les sons au centième.

**Les effets se cumulent d'une aptitude à l'autre, et c'est le principe.** Chercher le mot précis, tenir une forme imposée, traiter un tour dégradé produit des silences, donc fait baisser la fluidité sans que l'apprenant soit moins fluide. Une note ne se lit donc jamais sans la combinaison qui l'a produite.

**Un curseur qui impose un réglage à toute la séance est le fonctionnement, pas une collision** : la fluidité possède la capture, donc la monter change l'ergonomie de la séance entière. Le test qui sépare : **une combinaison n'est un problème que si elle change ce qu'une mesure veut dire, pas si elle rend seulement la tâche plus dure.** Ce test donne une contrainte dure — le bruit et le filtre portent sur ce qui est **écouté**, jamais sur la prise de l'apprenant, l'analyse comparant deux enregistrements traités symétriquement.

## L'histoire

Un mode histoire tire sur tout ce que le modèle a de mou, donc il sert de banc d'essai. **Une histoire est un bloc à accès ordonné, et chaque scène est une définition.**

Pas une activité unique dont les scènes seraient des étapes : le `brief` est posé au départ et aucun patch ne le touche, donc une activité ne peut changer ni de lieu, ni de personnage, ni d'objectif — et la rendre modifiable défairait ce qui garantit qu'une séance a une seule situation, donc une seule note lisible. Et une scène **est** une définition terme à terme : ce n'est pas qu'on fait entrer l'histoire dans le modèle, c'est que le modèle décrivait déjà une scène sans le savoir. Le fil se coupe donc entre deux scènes, ce qui est plutôt souhaitable — dans un jeu, une scène se termine.

### Les questions

**Une question est un trou de la fiction que le modèle remplit, à un moment que l'auteur a écrit.** *Comment sa conférence s'est-elle passée ? Est-ce qu'il a retrouvé la meunière ? La reine est-elle en sûreté ?* La réponse devient un **fait de la séance** : elle revient au modèle à chaque tour suivant, elle peut s'afficher à l'apprenant, et une scène suivante la lit. Sans ça rien ne passe d'une scène à l'autre que réussi ou raté, et le barman de la scène 4 ne peut rien demander.

**C'est aussi ce qui rend une tuile rejouable.** Une mise en scène fixe ne varie pas ; un menu de règles ne varie qu'entre les branches que l'auteur a énumérées. Une question à réponse libre répondue à l'ouverture fait écrire au modèle un fait que personne n'a énuméré, et le **gèle** — il s'engage une fois au lieu de ré-improviser son personnage à chaque tour. La variabilité cesse d'être de la dérive. Le gel ne demande aucune pièce : il tient à ce que la question n'a qu'un moment.

**L'app sert la question, le modèle répond sur-le-champ.** C'est l'inversion qui porte tout le reste : pas un menu de champs vides que le modèle remplit quand il veut, mais une question posée au moment dit, dont la réponse est un champ **obligatoire de ce tour-là**. Le jugement reste cru sur parole ; la **présence** d'une réponse se contrôle comme n'importe quel champ de retour, l'app ayant choisi le moment.

**Rien de tout ça n'est mécanique.** Une réponse est de la prose que le prompt relit : aucune condition ne la lit, aucun patch ne s'y accroche. Ce que l'app doit exécuter passe par les règles, dont le menu est fermé. Faire choisir l'IA hors de la mécanique est le seul endroit où on peut être généreux sans rien payer.

**Ses moments sont des déclencheurs**, au même titre que ceux d'une règle, et plusieurs sont permis. L'auteur écrit *quand*, l'app sert la question à cet instant, ce qui retire au modèle toute discrétion sur le calendrier. Une question **ride sur le premier appel qui suit** le moment qui l'a posée, exactement comme un message de règle : celle qui tombe à la fin d'une tentative a vu l'appel de cette tentative partir sans elle, et le suivant est le plus tôt qu'il y ait. La clôture est le seul endroit où une question seule fait un tour, n'ayant pas d'appel suivant sur quoi monter.

**Une question déclare son format** : texte libre, ou liste fermée — le oui/non en est une à deux entrées. Le texte libre nourrit le prompt des scènes suivantes ; **une réponse fermée se lit par du code**, donc elle branche — *« a-t-il la clé ? oui »* ouvre la scène 5, sinon la 5 bis. Ce qui le lit est l'ordre du bloc, qui dérive déjà de ce que les scènes ont rendu. Les options d'une liste fermée sont du **texte d'auteur**, donc une table langue → texte comme le titre : l'anglais est obligatoire, c'est la clé qui part au prompt et sur laquelle la validation se fait par appartenance.

**Trois crans, un escalier avec un plancher et pas trois modes.** Ce que la discussion établit est **toujours prioritaire** ; les crans disent seulement ce qui se passe quand elle ne tranche pas. *Vérité de la discussion* : répondre uniquement d'après ce qui s'est dit, et sinon **je ne sais pas**, qui fait partie du menu à ce cran et à lui seul. *Extrapolation permise* : à défaut de certitude, conclure. *Invention permise* : à défaut de quoi extrapoler, décider. À écrire comme un escalier et pas comme trois étiquettes, sinon l'invention écrase la vérité. **« Je ne sais pas » plutôt qu'un champ absent**, parce qu'un champ absent est indistinguable d'un modèle qui a oublié et d'un parsing raté.

**Une question à plusieurs moments porte une suite de réponses, chacune datée de son passage, et rien ne s'écrase.** Une réponse tardive **succède** à la précédente, elle ne la corrige pas : *la reine était en sûreté au passage 5 et ne l'est plus au 15* est une histoire, pas une erreur rattrapée. Une scène qui lit une question dit donc laquelle elle lit — la dernière réponse, ou la suite entière.

**Une réponse vit sur le tour qui l'a établie**, et tout le reste s'en dérive : la suite est les tours dans l'ordre, le passage qui date chacune est l'endroit du fil où son tour se trouve. C'est aussi ce que veut le prompt — **le fait se pose là où il a été établi**, ce qui porte sa date gratuitement (posé en tête, un fait du douzième passage se lirait comme un donné de départ et le modèle jouerait quelqu'un qui l'a toujours su) et ne casse pas le préfixe caché, l'historique grossissant par la queue là qu'une section de tête grossirait au milieu. Dans l'objet de retour, ce qui est établi vient **avant** ce que le personnage dit : il parle en sachant ce qui vient d'être établi, jamais l'inverse.

**Un seul drapeau d'affichage par question, et il commande les deux surfaces.** Levé : l'apprenant est mis au courant quand le fait est établi, et l'écran de reprise le lui rappelle. Baissé : c'est de la plomberie pour le modèle, jamais affichée. Un seul drapeau parce que les deux ensembles sont forcément les mêmes — l'écran de reprise ne peut montrer que ce que l'apprenant a **déjà appris**, sinon rouvrir une conversation révèle ce que la jouer n'avait pas révélé. C'est un vrai choix de mise en scène : *le conférencier est vexé, sa salle était à moitié vide* au drapeau levé est un **décor** ; au drapeau baissé, c'est une chose qu'on **découvre en parlant**.

**Une réponse s'écrit comme la situation s'écrit** : l'apprenant est **tu**, tout le reste est nommé ou à la troisième personne. Elle est relue par quelqu'un d'autre que celui qui l'a écrite — le barman de la scène 4 lit ce que le conférencier de la scène 2 a établi, et un *« je suis encore vexé »* n'y a plus de sujet. Et **la question se pose *sur* le personnage, jamais *à* lui** : *« How did the speaker's talk go? »*, pas *« How did your talk go? »* — la posture est donnée par le texte de l'auteur, qui ne peut pas rater, plutôt que par une consigne de forme, qui peut.

**La langue.** La question et la réponse partent au modèle en anglais, toujours. Une réponse en texte libre s'affiche telle quelle, en anglais : on apprend l'anglais, et une ligne d'anglais n'est pas une punition. Une liste fermée affiche la traduction si le fichier en porte une.

**La question est déclarée par la scène qui la produit**, jamais par celle qui en a besoin : seul le modèle qui était là peut y répondre. Le prix est un prix d'auteur, et c'est le travail normal quand on écrit une histoire.

**Les réponses ne sont pas dans le résultat**, et ce n'en est plus le texte libre. Ce sont des faits de la **séance** et non de la façon dont elle s'est passée : elles arrivent pendant qu'elle tourne, et une séance abandonnée en porte sans avoir d'issue du tout. Ce que l'issue ajoute est qu'une scène suivante ait le droit de les lire.

Deux limites. **Le modèle écrit sa propre mémoire**, et rien ne la vérifie : même famille que `intended`. Les questions bornent la dérive sans la supprimer. Et **ça grossit** : à la scène 12 on transporte les réponses de onze scènes.

## Le personnage

Rencontrer quelqu'un plutôt que choisir un thème. La version la plus bête est qu'un personnage est un `brief` — du texte dans le prompt, zéro pièce neuve. Elle manque peu : un personnage porte aussi une **voix** et des **positions de leviers**. Or un `brief` plus une voix plus des positions, **c'est une définition**. Le personnage n'est donc pas un objet neuf : une rencontre est le lancement d'une définition, un recueil de personnages est un bloc à accès libre.

**Plusieurs personnages.** L'activité pointe une **distribution**, pas un interlocuteur ; chaque énoncé porte **qui parle**, le champ locuteur cessant de valoir apprenant-ou-IA pour devenir une identité ; la synthèse choisit une voix **par énoncé**, ce que le cache encaisse déjà, étant indexé par texte et par voix. Côté modèle, l'IA rend la clé du personnage qui parle.

**La persona ne doit jamais atteindre la reconstruction d'`intended`.** Si le personnage déteint, la phrase de l'apprenant ressort en dialecte et c'est l'étalon de toute la mesure qui bouge. Même famille de fuite que celle déjà mesurée, où le modèle réparait la grammaire.

**Un seul personnage porte la tuile, et il se déclare là où il est.** Une fiche marque un membre de sa distribution comme le principal ; c'est son nom court que la tuile affiche sous le titre, parce qu'on rencontre quelqu'un plutôt qu'on lance un sujet. Le drapeau est **sur le personnage** et jamais une clé posée en tête de fichier : une clé pointe dans le vide le jour où on renomme un personnage, et elle le fait en silence — une tuile sans nom est exactement ce qu'une définition sans distribution donne. Une distribution d'égaux n'en marque aucun, et sa tuile est son titre seul.

**Un personnage jetable ne coûte rien, un personnage qu'on retrouve coûte le stockage.** Fabriqué à la volée, il ne fait que remplir les champs d'une activité. Revoir Vera trois séances plus tard exige de la garder : c'est le jour où la séparation définition/exécution revient en base. La porte reste ouverte, elle n'est pas franchie.

### La voix

**Une voix est un fournisseur et un slug** (`eleven-us-eric`). Rien d'autre n'est nécessaire pour la nommer.

**Deux rôles, deux listes, et ils ne se croisent jamais.** La **voix de référence** — celle qui dit la phrase de l'apprenant, donc l'étalon de toute la mesure — est choisie de son côté et doit passer l'étalonnage (`reference.md`). Les **voix de personnages** ne passent aucun test, puisqu'elles ne mesurent rien : le personnage dit *ses* tours, le modèle à imiter dit *la phrase de l'apprenant*. Un personnage peut donc avoir n'importe quelle voix, y compris une qui échoue à l'étalonnage, et une voix difficile à suivre devient un levier de compréhension. En échange, **la voix de conversation cesse d'être un réglage global** et devient un champ de l'activité, que la distribution porte.

**Deux sortes de voix, et deux modes qui coexistent.** Les **génériques** — quatre en première implémentation, deux d'homme et deux de femme, choisies par l'apprenant chez son fournisseur — sont **interchangeables** : une définition générique demande *une générique*, éventuellement d'un genre, et n'en nomme aucune. Les **spécifiques** viennent du projet et se nomment par leur slug. Les deux modes sont (a) l'apprenant met sa clé et n'a que le générique, (b) il achète des crédits et a tout ; les plafonds et les coûts sont dans `providers.md`.

**C'est ce qui tient l'invariant sans machinerie.** Un défi n'écrit jamais `eleven-gb-daniel` en dur : ce qu'un fournisseur expose ne peut pas devenir une condition, sinon le défi meurt le jour où on change de clé. Une définition générique ne portant aucun nom, il n'y a rien qui puisse mourir avec une clé, et aucun catalogue à interroger. Et **l'invariant ne s'applique pas aux spécifiques** — son problème était le roulement d'un tiers, et un slug sur un fournisseur que le projet tient est stable par construction.

**Ce n'est pas un levier** : entre deux voix il n'y a ni dur ni facile, et ses positions arrivent d'un catalogue qui change avec la clé. C'est un champ, qu'aucun patch ne déplace.

### Les briques

Construire une activité en choisissant des briques — un lieu, un personnage, une situation —, chacune avec son texte libre. Aucune pièce neuve : une brique apporte des fragments de `brief`, des positions de leviers et des règles, donc c'est un **préréglage nommé**.

Ce qui compte est le **périmètre**, et cette coupe est **une première proposition, pas une décision** : **Lieu** possède le bruit et la qualité du canal ; **Personnage** l'exigence de voix et les leviers de sa parole ; **Situation** les conditions de fin ; **Règles** les feuilles notées, les poids, les sensibilités et les règles.

**Deux briques qui voudraient le même levier signalent que la coupe est fausse**, pas qu'il faut une règle de priorité. Leurs textes libres n'entrent en conflit avec rien : ils ne se lisent pas, ils se comprennent.

## La capture

Le geste, les trois positions et ce que chacune permet de lire sont dans `capture/TurnRecorder.kt`. Trois choses tiennent au-dessus.

**L'échelle gradue exactement ce que la fluidité peut lire**, et ce qui distingue les positions n'est pas le geste — tout s'ouvre et s'envoie d'un appui aux trois — mais **deux faits** : qui ouvre le micro, et si la pause existe. La pause est ce qui interdit de lire les silences, donc elle vit là où ils ne se lisent pas, et nulle part ailleurs. La première position se tenait au doigt maintenu, ce qui la faisait diverger sur le geste et non sur ce qu'elle mesure, et obligeait chaque bouton servant dans les deux régimes à porter deux comportements que rien à l'écran n'annonçait.

**Le silence final d'un tour que l'horloge a envoyé vaut exactement x, et il compte.** Sur la même parole de quatre secondes, la continuité rend 23,8 à seuil 3 s, 39,7 à 5 s et 59,5 à 10 s, contre −4,8 quand la main envoie. Ce n'est pas un artefact à retirer : le levier demande précisément de **ne pas aller jusqu'à x**, donc s'y rendre est un fait sur celui qui parle — et la position ne s'agrège de toute façon avec aucune autre.

**Le micro ne s'arme jamais tout seul tant qu'un passage attend une reprise.** Le passage se ferme à l'appui sur le gros bouton, *l'app n'ayant rien à deviner de ce qui vient d'être dit* — or l'armement automatique supprime l'appui, donc le fait. En « poursuit », l'app recevrait une prise sans savoir si c'est la tentative suivante ou un tour neuf, et rien ne trancherait. Suspendre l'armement recrée l'instant de la décision, et seulement là où il avait disparu : en « attend », tout ce qui se dit est déjà une tentative. Le silence du micro est le signal, et il n'a pas besoin d'être doublé par une fenêtre à écarter.

**Une prise se jette avant d'être envoyée, et c'est un levier.** Sans lui, la seule sortie d'un raclement de gorge serait de l'envoyer, ce qui dépense une tentative. Mais librement offert, il rend le compte des tentatives contournable : dans un défi qui n'en donne qu'une, on recommence dix fois en jetant chaque prise. Un défi doit donc pouvoir le fermer, ce qui est exactement la définition d'un levier.

**La durée d'un tour est une seule variable, et le plafond technique en est la valeur maximale admissible.** Imposer de répondre en cinq secondes et supporter trente secondes au plus sont la même chose réglée différemment. Le plafond est technique — la mémoire d'une passe d'analyse croît comme le **carré** de la durée (`analysis.md`) — et il remonte quand le fenêtrage arrive ; le levier reste. Dans les deux cas le tour se ferme et envoie ce qui a été dit : jeter perdrait de la parole.

## L'audio

**Un tour est un enregistrement d'un seul tenant**, gardé et envoyé tel que le micro l'a entendu (`capture/TurnRecorder.kt`). Il était découpé en segments, le silence gardé comme durée et jamais comme échantillons ; ce découpage reposait sur une barre de volume posée à la main, et il est abandonné (2026-09-10, `reference.md`).

**Ce qu'on déduit de l'audio se stocke.** La purge efface l'audio ; une mesure qui ne se recalculerait plus après doit donc exister ailleurs. **L'audio n'est pas purgé par défaut, et la purge est à écrire** — à ne pas confondre avec le balayage des enregistrements que plus aucun énoncé ne nomme, qui est fait et n'est pas une purge (`reference.md`).

**Ce que chaque mesure lit exactement** est à préciser mesure par mesure ; toutes lisent le même fichier depuis que le tour n'est plus découpé.

## L'habillage rétro

Le rétro touche aussi le marquage, qui est un instrument de précision — une rampe à nombreuses nuances, trois canaux superposés, une vaguelette qui doit se distinguer d'un filet droit au même endroit. Ça se refait **une fois**, en gardant la lisibilité ; ce n'est pas une contrainte permanente sur la conception (`ui.md`).
