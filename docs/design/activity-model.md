# Le modèle d'activité

Conçu le 2026-09-01, **élagué le 2026-09-01 de ce qui est construit**. Ce qui reste ici est la part du modèle d'activité qui n'a pas encore de code, plus ce qu'elle laisse ouvert.

Ce qui est parti et où le lire : l'**énoncé**, l'**activité** et **ce qui se stocke** sont dans `../reference.md` pour la règle et dans le code pour la forme (`activity/Activity.kt`, `conversation/TurnPipeline.kt`, `store/`). La suppression de la **session** et de la **parenthèse** est actée dans `../reference.md`. Les commits sont la carte.

Ce qui est écrit ici est tranché, sauf mention contraire. Ce qui reste ouvert est en fin de doc, nommé.

## Les prescripteurs

Un **prescripteur** est ce qui remplit une activité — sa matière et ses réglages. Il y en a trois :

- **la conversation**, à partir de ce qui vient de s'y passer ;
- **l'apprenant**, par choix ou par consigne libre, ce qui est la voie par laquelle de la matière neuve entre ;
- **la progression**, pour ce qui est dû, jamais réussi, ou sur le point d'être oublié.

Un quatrième viendra peut-être — le contexte global, qui suggérerait de lui-même. Il n'a rien à préparer : ajouter un prescripteur n'est qu'une façon de plus de remplir les mêmes champs.

Un prescripteur ne décide rien pendant : il remplit des champs avant, puis se tait (« Qui décide de la pression »).

Les trois sont nommés en code (`Prescriber`) et **seul l'apprenant en est un pour l'instant** : ouvrir l'app est l'apprenant qui demande une conversation. Les deux autres attendent le catalogue des activités.

## Les réglages

**La forme des réglages dépend de l'activité.** Ce qui suit vaut pour la conversation.

Ils sont **fixés pour toute sa durée** et se règlent **par aptitude**. Ils infléchissent le contenu par le prompt, et peuvent aussi infléchir la forme : une prononciation à zéro éteint l'analyse de son, plutôt que de la calculer pour ne rien en montrer.

Le détail de ce que chaque réglage produit est à préciser.

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

Deux lignes sont neuves. **Le module ne peut pas adoucir un verdict** — un module « débutant » qui marquerait moins pour ne pas décourager casserait *la même faute doit produire la même marque*, et rien d'autre ne fermait cette porte. Et **le module décide quels leviers il expose**, ce qui n'était écrit que comme un fait d'implémentation — la forme des réglages dépend de l'activité — et devient ici un pouvoir qu'on lui accorde.

**Deux choses ressemblent à des décideurs et n'en sont pas.** La **mesure** n'a aucune discrétion : elle applique la barre qu'on lui donne, elle ne la choisit pas, et un décideur sans discrétion est une fonction. Le **prescripteur** n'est pas un décideur d'un autre genre, c'est le **siège** que l'apprenant occupe et que la conversation ou la progression occuperont à sa place — ce qui s'y décide est le même, matière et réglages, donc les mêmes interdictions s'y appliquent sans qu'il faille les réécrire.

### Ce qui mesure chaque aptitude, et les leviers qui la pressent

Deux choses par aptitude, et pas plus : le **critère** qui permet de l'évaluer, et les **leviers** qui modulent la pression sur elle. Aucune de ces listes n'est close.

**Élocution**
- *Mesure* : l'écart au modèle, aux trois échelles — les sons, l'accent du mot, la mélodie de la phrase.
- *Leviers* : voir le texte, ou redire sa phrase de tête ; un temps limité, posé en pourcentage de la durée du texte synthétisé, et dépasser oblige à réessayer ; la sensibilité du marquage, qui module la note d'écart ; le nombre de reprises permises.

**Compréhension**
- *Mesure* : la réponse montre qu'on a compris, ou révèle qu'on n'a pas compris.
- *Leviers* : la complexité du tour de l'IA — longueur, vocabulaire, structure ; le texte affiché ou flouté ; la réécoute, autorisée ou non, et combien de fois ; le bruit et la qualité du canal, jusqu'à simuler un mauvais réseau qui coupe des mots.

**Formulation**
- *Mesure* : le rang atteint sur une échelle allant de l'incorrect à l'exemplaire, à quantifier plus finement pour en tirer une note.
- *Leviers* : la réponse de l'IA sur un tour fautif, en trois niveaux — absente, indication indirecte (la reprise dans sa réponse, ce que le code incite aujourd'hui), ou reformulation explicite dite comme un coach reprend son élève ; le marquage dans la phrase, ou pas ; l'explication de la faute en notification, à deux niveaux — la règle à utiliser seule, ou la règle plus la phrase correcte.

**Fluidité**
- *Mesure* : le débit et les arrêts — le délai avant de commencer, le nombre et la longueur des silences.
- *Leviers* : le mode d'enregistrement, qui passe en automatique dès le premier cran pour que la mesure du silence soit possible.

**Richesse**
- *Mesure* : la précision et la variété du lexique, l'adéquation du registre, la longueur et la complexité de la phrase.
- *Leviers* : le registre imposé, ou une contrainte du même genre ; la longueur imposée.

Deux remarques sur les leviers de formulation. Le marquage et l'explication sont **deux leviers et non deux crans d'un seul**, parce que **où** et **quoi** ne sont pas deux quantités de la même information : on peut donner l'un sans l'autre, dans les deux sens. C'est la distinction que l'analyse fait déjà pour le son, où le marquage dit où et nommer le son produit est un enrichissement séparé (`../reference.md`). Et tout en haut, ces leviers se recouvrent : une reformulation explicite de l'IA donne déjà la phrase correcte à voix haute, que l'explication redonnerait par écrit.

## La capture

La capture est l'un des axes que les réglages gouvernent. Elle n'est pas spécifiée, et ce qui suit est ce qui est acquis.

**Plusieurs modes sont possibles, aucun n'est arrêté.** Deux ont été travaillés : l'**armement manuel**, et l'**automatique** où le tour s'ouvre et se ferme seul, la fermeture sur silence étant elle-même un levier réglé.

Le micro **ne s'arme jamais avant la fin de la réponse de l'IA**. Un symbole est visible dès que ça enregistre : il n'informe pas seulement, il fait partie de la pression — savoir que ça tourne change la façon dont on parle.

**Chaque tour porte son mode de capture.** C'est ce qui dit si ses silences sont significatifs, et rien ne s'agrège entre modes. Sous armement, le délai avant de parler mesure le geste et non l'hésitation ; agréger ces tours avec des tours captés automatiquement produirait un chiffre qui ressemble à de la fluidité sans en être.

## L'audio

**Un tour est une liste de segments** : une durée de silence, ou de l'audio. Le silence n'est jamais stocké en échantillons — il ne coûte alors que sa durée, là où le garder ferait passer une conversation de vingt minutes à des dizaines de mégaoctets. On reconstruit l'audio d'origine quand on en a besoin.

Chaque segment de parole garde une **marge de vrai audio** de part et d'autre. Ce qui identifie une occlusive vit dans la transition, et couper au ras de la parole abîmerait la mesure.

**On n'envoie pas de silence au réseau.** Ce que chaque mesure lit exactement — l'audio brut, les segments, la reconstruction — est à préciser mesure par mesure.

**L'audio n'est pas purgé par défaut, et la purge est à écrire.** À ne pas confondre avec le balayage des enregistrements que plus aucun énoncé ne nomme, qui est fait et n'est pas une purge (`../reference.md`).

## Ce qui reste à spécifier

- **La pression.** Ce n'est pas un mécanisme mais un concept qui circule par plusieurs voies — le degré de liberté laissé à la production, la contrainte imposée, le temps, l'étayage retiré, l'imprévu, l'exigence de marquage. Entièrement à définir.
- **Le détail des effets des réglages**, sur le contenu comme sur la forme.
- **Le catalogue des activités et leur déroulé.** Aucune n'est nommée à part la conversation.
- **Les déclencheurs de suggestion** pendant une conversation.
- **Le prescripteur progression** : sur quoi il s'appuie, comment il choisit ce qui est dû.
- **Le remplacement d'une phrase fautive** dans le fil.
- **La purge**, et la durée de vie des audios.
- **Ce que chaque mesure lit** de l'audio, segment par segment.
- **La liste de ce qui doit être agrégeable.**
