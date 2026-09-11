# L'état d'une scène

Doc transitoire, ouvert le 2026-09-11. Il remplace la mécanique qui fait bouger une scène — règles, déclencheurs, décideurs, paquets, patchs, questions, conditions jugées, drapeaux, messages, tours scriptés — par un seul modèle : **des cases, qui les écrit, des événements qui les lisent, et des textes lus par le meneur, par l'apprenant, ou par les deux.** Il s'élague quand le moteur est réécrit et qu'`activity.md` a repris ce qui lui revient.

**Le meneur** est l'IA qui parle : il fait parler tous les personnages de la distribution et le narrateur, comme le meneur d'un jeu de rôle, et ne se confond avec aucun d'eux. **Le juge** est l'IA qui marque la phrase de l'apprenant. Les deux sont deux appels distincts depuis le 2026-09-10 (`premium-chain.md`).

## Pourquoi

Une vingtaine de notions font aujourd'hui bouger une scène, chacune ajoutée pour un cas précis, et chacune avec sa façon d'être gardée, affichée et lue. Ça s'est vu en essayant de faire réagir Dana (`the-grudge`) au moment où l'apprenant nomme ce qui la blesse :

- la question et la condition jugée font le même travail de deux façons — `activity.md` dit qu'aucune condition ne lit une réponse, puis qu'une réponse fermée sert à choisir la scène suivante ;
- le oui d'une condition jugée n'est gardé nulle part, là où la réponse à une question l'est ;
- `staging` nomme à la fois la description du personnage envoyée au modèle et la phrase qu'une règle affiche à l'apprenant ;
- un personnage n'existe qu'à l'intérieur d'une scène, donc dans une histoire sa voix et son genre seraient tirés à nouveau à chaque scène.

## Les cases

Tout ce qui peut changer pendant une scène est une case : un nom et une valeur.

**Trois familles, distinguées par qui les déclare, et par leur nom.**

- **Les leviers** (`lever.turn-length`, `lever.lives`) sont déclarés par l'app, dans son catalogue (`levers/Levers.kt`). L'app sait ce qu'ils veulent dire et les applique : couper le tour à 10 s, brouiller le texte des répliques, finir à zéro vie. Chacun porte ses valeurs, le sens de sa difficulté et sa phrase mécanique ; certains envoient d'eux-mêmes un texte au meneur, l'écho à *indirect* disant *« recast it inside your own reply »*. Une fiche choisit leurs valeurs, elle ne les invente pas.
- **Les cases de l'app** (`app.passage`, `app.silence`, `app.correctness.note`, `app.sound-gate`) sont déclarées et écrites par l'app seule : les mesures, les notes, les portes, le numéro du passage, les horloges. Elles ne s'appellent pas *mesures* parce qu'elles n'en sont pas toutes au sens de `../reference.md`, qui sépare la mesure, que rien ne règle, de la note, où la sévérité entre : la porte du son lit une note et un levier. Ce qui les réunit est leur écrivain.
- **Les cases de la fiche** (`patience`, `named`, `trust`), sans préfixe, sont déclarées par la fiche. L'app ne sait pas ce qu'elles veulent dire : elle les garde, les teste, les cite, et rien d'automatique ne s'y rattache. Leur sens n'existe que dans l'histoire. Le préfixe des deux autres familles empêche qu'une case de la fiche ressemble à un levier.

**Les cases de l'app traduisent ce que le moteur lit déjà** (`conversation/World.kt`), sans rien y ajouter que la séance :

- `app.passage`, le numéro du passage ;
- `app.turn-time` et `app.silence`, les deux horloges, lisibles pendant l'enregistrement seulement ;
- pour chaque nœud de l'arbre (`sheets/Sheets.kt`), aptitude ou feuille, sa **note** — `app.fluency.note`, une liste ordonnée A → E ;
- pour chaque feuille, son **chiffre** dans son unité — `app.fluency/pace.figure` —, et le **compte de ses éléments par cran** — `app.correctness.not-said.count`, dont le test « ≥ 1 » redit l'ancien *au moins un mot `ne se dit pas`* ;
- les deux portes, `app.words-gate` et `app.sound-gate`, vrai/faux.

**Un élément se compte par cran, et une proportion se lit par le chiffre de la feuille**, jamais par un compte rapporté : *au moins un* est ce que l'élément dit, *quelle part* ce que dit le chiffre (`World.kt`), et une part par cran doublerait des chiffres existants — celui de l'intelligibilité est déjà, à peu de chose près, la part des sons qui n'ont pas changé le mot.

**Les sons se comptent sur trois crans fixes**, ceux que l'écran montre (`ui/MarkingColors.kt`) : **dans le bruit** sous 5 points, où la lettre garde l'encre neutre ; **écart** de 5 à 30, teinté de l'ambre au rouge ; **gros raté** à 30 et plus, rouge plein, la ligne où *le mot a changé* et que `measures.md` veut la même pour l'écran et pour la feuille. *Au moins un gros raté* s'écrit `app.pronunciation/proximity.gross.count` ≥ 1. Un seuil libre, 40 points, ferait partir une règle sur un son que l'écran montre du même rouge qu'un son à 31 ; et la barre des sons ne se règle pas (`../reference.md`). **Les silences gardent leur chiffre en secondes** — `app.fluency/longest-silence.figure` ≥ 5 dit *au moins un silence de plus de 5 s* —, aucun seuil n'y ayant de sens déclaré.

**Tentative et passage partagent un nom, le moment disant lequel on lit** : à la fin d'une tentative, la case vaut la tentative ; à la fermeture du passage, le passage, qui vaut sa dernière tentative (`activity.md`). **La séance a ses propres cases** — `app.sitting.correctness.note` —, lisibles à tout moment : *si la correction de la séance tombe en C* n'est pas *si celle de ce passage tombe en C*, et la fin *la note décide* comme un bilan dit à la clôture en ont besoin.

**Quatre sortes de valeurs.**

- **Vrai ou faux** — Lou a ri, le voyageur est arrivé, un événement est actif. Une liste ne peut pas se réduire à *oui / non* : c'est alors un vrai/faux, qui ne s'écrit que d'une façon.
- **Nombre**, avec ses limites — `patience` de 0 à 3, `score` à partir de 0 sans maximum. Un déplacement s'arrête à la limite.
- **Liste**, une valeur parmi des valeurs écrites d'avance — ce que fait Val, l'humeur tirée. Une liste peut être **ordonnée**, et dit alors dans quel sens elle va : A est le haut des notes ; pour un levier, le bout le plus dur n'est pas toujours le dernier cran, l'écho étant le plus dur à *none* (`HardSide`).
- **Texte** — ce que Lou a confié, le trou de l'apprenant. Aucun événement ne le teste, aucun code ne comprenant *« the flat in Lyon »* ; il est lu par le meneur et par l'écran, et cité dans d'autres textes.

**La sorte ne dépend pas de l'écrivain**, à une limite près : le hasard n'écrit pas de texte, ne sachant que choisir parmi des valeurs données. Le meneur peut écrire un nombre (*combien de verres le barman a servis*), l'auteur un texte (*« the platform »*, cité ensuite).

**Toute case peut être vide** : pas encore demandée, rien de mesuré (la note de prononciation d'un passage dont l'analyse n'a pas tourné), laissée vide par l'apprenant, ou sans valeur de départ jusqu'à ce qu'un événement la remplisse. La raison du vide n'est pas gardée, aucun cas n'ayant besoin de la lire.

**« Je ne sais pas » est une réponse du meneur qui laisse la case telle qu'elle était.** On demande une case au meneur en lui disant jusqu'où il peut aller — seulement ce qui a été dit, déduire, inventer — et au premier cran il peut ne pas savoir. Sa réponse est gardée en base, ce qui la distingue d'un oubli ou d'une réponse mal lue ; la case, elle, ne bouge pas, ne pas savoir n'apportant rien de neuf. Au colis, `robin-opened` demandé à la clôture de la scène 2 et resté vide dit en scène 3 qu'on ne sait pas : vrai, Nico est furieux ; faux, soulagé ; vide, inquiet. Et un `trust` à 7 que le meneur ne sait plus évaluer reste à 7.

**Un texte peut citer une case** ; l'app y met la valeur du moment où il part, comme elle le fait déjà de `{anchor}`.

**Une case de la fiche porte une courte description en anglais** — *« the barman's patience »*, *« Val's mood »* —, qui sert à fabriquer les lignes que le meneur lit quand elle change (plus bas).

**Une case vit aussi longtemps que ce qui la déclare** : la fiche de la scène, ou le fichier de l'histoire. Un levier comme une case de la fiche — l'enquêteur qui, après trois phrases fautives, ne laisse plus que 10 s jusqu'à la dernière scène tient la durée du tour déclarée dans l'histoire, et les événements de chaque scène la lisent et la déplacent ; un effet qui la recopierait à chaque ouverture de scène ferait la même chose, et une scène qui l'oublie remettrait la pression à zéro sans rien dire. Les activations et la fin vivent à la scène. Les cases de l'app aussi, jamais au-delà, rien de ce qui a été mesuré ailleurs n'entrant dans la lecture d'un tour (`../reference.md`) — sauf l'issue de chaque scène, que l'app déclare dans l'histoire (plus bas, la fiche et l'histoire).

**La voix et le genre d'un personnage ne sont pas des cases** : ce sont ses attributs, fixés par la fiche, sinon demandés à l'apprenant, sinon tirés, une fois, et gardés avec lui — donc toute l'histoire dans une histoire. Aucun événement ne les teste, et une voix vient du catalogue d'un fournisseur que l'auteur ne connaît pas.

## Qui écrit

**Six écrivains** : l'auteur (la valeur de départ), l'apprenant, l'app, le meneur, le hasard, un événement. Le juge n'en est pas un : il rend des marques, et c'est l'app qui en tire ses cases.

**Deux verrous, là où ils protègent quelque chose.** Une case de l'app n'est écrite que par l'app, sinon une fiche pourrait adoucir une note. Un levier n'est écrit que par l'auteur et ses événements, jamais par le meneur ni par le juge : c'est l'auteur qui décide de la pression. Pour que le meneur pèse sur la mécanique, l'auteur lui demande une case de la fiche dans les mots de l'histoire — *« How does your trust in them change? »* — et un événement décide de ce qu'elle coûte.

**Une case de la fiche peut avoir plusieurs écrivains, et rien ne les déclare** : ils se lisent dans la fiche. Le meneur écrit une case si un événement la lui demande, le hasard si un événement la tire, un événement s'il la change, l'apprenant si un événement la lui demande. Le déclarer à part serait une seconde source qui dérive. `trust`, un nombre de 0 à 10 : l'auteur le met à 5, le meneur le règle à chaque passage, un événement le baisse quand la porte des mots se ferme. Qui a eu le dernier mot se relit toujours : ce qui ne se retrouve pas est gardé avec son écrivain, le reste se refait (plus bas, rouvrir une séance).

**Le meneur n'écrit que les cases qu'on lui demande, et au moment où on les lui demande.** Il ne déclenche rien : ce sont les événements, écrits par l'auteur, qui tirent les conséquences. S'il pouvait écrire une case quand il veut, un événement partirait à un moment que personne n'a prévu.

## Les événements

Un événement dépend de trois choses.

1. **Un moment** : le lancement (avant l'ouverture, sur l'écran de situation), l'ouverture, pendant l'enregistrement, la réponse à une question (plus bas), la fin d'une tentative, la fermeture d'un passage, la clôture. Chaque moment ne voit que ce qui existe à cet instant : pendant l'enregistrement, seulement les horloges.
2. **Être actif.** Un événement peut être actif ou non au départ, et un autre événement l'active ou le désactive.
3. **Un test sur une case**, d'une de deux sortes :
   - **un état** — `lever.lives` = 0, `app.correctness.note` ≤ C, `app.silence` ≥ 5 s, `app.passage` multiple de 5, `app.sound-gate` vrai à la fermeture du passage (*le passage s'est terminé porte du son fermée*). Il repart à chaque moment où il est vrai : chaque mauvaise phrase coûte une vie ;
   - **un changement** — `named` devient vrai, `lever.lives` diminue, un levier devient plus dur. Il ne part qu'une fois par changement.

**Un test sur une case vide est toujours faux**, sauf le test fait pour ça, « est vide ». Sans quoi « note de prononciation ≤ C → une vie en moins » coûterait une vie pour une analyse qui n'a pas tourné.

**Un seul test par événement, et l'activation sert de « et ».** La fausse mort s'écrit ainsi : A (actif) — quand `lever.lives` = 0, remettre une vie, désactiver A, activer B ; B (inactif) — quand `lever.lives` = 0, terminer. Combiner deux tests ouvrirait la voie au « ou », puis aux parenthèses, donc au langage de conditions que `rules/Rule.kt` refuse parce qu'on ne pourrait plus dire ce que fait une fiche sans la jouer. Décidé pour cette version ; un cas réel qui ne s'écrirait pas avec l'activation rouvrirait la question.

Certains événements ne testent rien que le moment : *à l'ouverture, Nico dit ton nom.*

## Les effets

**Changer une case** de la fiche ou un levier : lui donner une valeur (`patience` ← 3), ou la déplacer (`lever.lives` − 1) — un déplacement s'arrête à la limite sans erreur. Activer ou désactiver un événement. Terminer la scène, avec son issue : réussi, raté, ou la note décide.

**Quand un levier change, l'app affiche d'elle-même une notification** avec sa phrase mécanique (*« Il te reste 2 vies »*, *« Durée du tour : 10 s »*) ; si l'événement envoie aussi un texte à l'apprenant, il s'affiche dans la même notification (*« Le barman s'impatiente »*). Sans texte de l'auteur, la phrase mécanique suffit. Une case de la fiche qui change n'annonce rien.

**Envoyer un texte.** Il répond à deux questions.

- **Qui le lit : le meneur, l'apprenant, ou les deux.** Le meneur seul, c'est un secret — la description de Dana, *« Reproach them »*, *« The package holds a stolen watch »*. L'apprenant seul, le meneur n'en sait rien — *« Il te reste une vie »* : un réceptionniste qui dirait *« il vous reste une chance »* casserait sa fiction (`activity.md`). Les deux — la situation, une consigne, une réplique écrite d'avance.
- **Si l'apprenant le lit, sous quelle forme : dans le fil**, dit par un personnage ou le narrateur, **ou en notification**, l'écran qui suspend la scène. La barre d'état n'est pas une forme d'auteur : l'app y met ce qu'elle veut.

Un texte d'événement pour le meneur n'a pas de durée : il se place dans la conversation au passage où il part, et y reste (plus bas, le prompt du meneur).

**Le juge n'est pas un lecteur qu'on choisit : il lit ce que lit l'apprenant**, et rien d'autre. Un texte pour le juge seul ne peut donc pas s'écrire, et c'est voulu : au comptoir, *« Penalise any sentence without "sir" »* marquerait l'apprenant sur un critère qu'on ne lui a jamais dit.

**Une consigne** est un texte pour les deux — ou pour l'apprenant seul (plus bas) —, rattaché à un marquage jugé — les empans, le bafouillage ou la compréhension (`Instructing.marking`) —, avec une durée. Elle ne peut que durcir, jamais adoucir : *« ignore les temps »* reste interdit. L'apprenant la reçoit en notification quand elle tombe, et quand elle cesse (*« Tu peux de nouveau raconter comme tu veux »*) ; le meneur et le juge la reçoivent à chaque tour tant qu'elle dure. Le juge la lit pour juger avec, le meneur pour jouer avec : pour **fabriquer l'occasion** — sous *raconter au passé*, demander *« So what happened last night? »* plutôt que *« What are you doing this weekend? »*, qui n'appelle que le futur — et pour **ne pas la saboter** : un barman qui ignore *poli sans dire please* lance *« Say please! »*. Elle porte une table de langues, l'anglais obligatoire : l'apprenant la lit dans sa langue, le meneur et le juge en anglais, et le prompt du juge la présente pour ce qu'elle est — *« The learner has been given this instruction: … »* — pour que son `you` désigne l'apprenant.

**Une consigne peut être cachée au meneur, jamais à l'apprenant.** Glisser un mot sans que les animateurs le repèrent est une exigence que le juge doit connaître et le meneur ignorer : la consigne est alors pour l'apprenant seul, le juge le suivant, et le meneur perd ce qu'elle lui apportait — fabriquer l'occasion, ne pas la saboter —, ce qui est le jeu. Pour les deux par défaut ; cachée, l'app ajoute d'elle-même à sa notification *« Les personnages n'ont pas cette information »*. Cachée à l'apprenant, elle serait cachée au juge et ne toucherait plus aucune marque : ce qui y ressemble, le point faible d'un boss — *« The dragon softens only when they speak of its lost treasure »* —, s'écrit en un texte pour le meneur, une case qu'il écrit et un événement, et se joue sur l'histoire.

**Ce qui distingue une consigne d'un levier** : un levier change ce que l'app fait (elle coupe ton tour à 10 s), une consigne change ce que le juge demande (raconter au passé). L'un est une valeur que du code lit, l'autre une phrase que seul un modèle comprend. Et c'est la seule chose qu'on doit dire à l'apprenant avant qu'elle agisse, parce qu'elle change ce qui est juste ; une règle qui retire une vie ne change aucune marque, elle en tire une conséquence, notifiée quand elle arrive.

**Faire parler un personnage**, sans attendre l'apprenant : à l'ouverture, à la fermeture d'un passage, à la clôture. Jamais pendant un enregistrement ni à la fin d'une tentative. À la fermeture d'un passage, c'est une seconde réplique après celle qui vient de répondre à l'apprenant : l'annonce du quai au passage 10, le troisième voyageur qui se dispute avec Lou au passage 8.

**Sauf quand la chaîne d'événements est partie d'une case de l'app qui lit le tour** — une note, une porte, une mesure —, directement ou non : la porte des mots se ferme, `patience` baisse, `patience` atteint 0. Le meneur vient alors de répondre sans connaître le jugement, et un barman qui répond comme s'il avait compris puis dit *« Wait, what? »* contredirait le fil. **L'app retient la réplique**, sachant d'où la chaîne est partie, et son texte part avec l'appel suivant : le barman fait son reproche au tour d'après. Le reste de la chaîne ne se retarde pas — les cases, les leviers et la notification se font tout de suite, sans quoi la durée du tour changerait sans qu'on l'ait dit avant le micro. La notification ne contredit pas le fil, n'étant pas une réplique : c'est l'app qui décrit ce qui se passe.

**Demander une case au meneur** : *à la fermeture de chaque passage, demander `named`* ; et un second événement, *quand `named` devient vrai, désactiver le premier*. La case ne dit que ce qu'elle est ; quand elle est demandée, et jusqu'à quand, est toujours écrit dans un événement, avec les mêmes tests et la même activation que partout.

**Ce que le meneur écrit, les événements le lisent à la fermeture du passage.** Il l'écrit dans son appel, qui n'est pas un moment, et c'est le premier moment où c'est définitif : une tentative refaite emporte ses écritures avant que rien ne les ait lues, donc il n'y a rien à défaire. Au poste-frontière, le garde rassuré dans son appel l'est dans sa réplique même, et le tour s'allonge avant le micro suivant. La réponse à une question fait exception, ayant son propre moment (plus bas).

**Demander une case à l'apprenant** : *au lancement, demander `anchor` — « Ce que Dana te reproche »*. C'est ce que font les trous aujourd'hui, avec la même forme que le reste. **Au lancement seulement** : en cours de partie, une question à l'apprenant passe par le dialogue (plus bas).

**Tirer une case au hasard** : *à l'ouverture, tirer `mood`.*

**Ce qui est affiché au moment d'un événement est écrit par l'auteur.** Un texte que l'IA écrirait devrait exister à la fermeture du passage, et aucun appel n'est à la fois fini à ce moment et fait pour ça : le meneur a fini avant de connaître le jugement, le juge ne fait pas de fiction, et un appel de plus ferait attendre le micro. La phrase de l'auteur s'affiche en outre à côté de la phrase mécanique, qu'un texte improvisé peut contredire (`activity.md`). La liberté du meneur est sa réplique suivante, où il joue la réaction comme il veut.

## L'ordre, dans un moment

1. Les événements du moment partent.
2. Ceux qu'un changement vient de déclencher partent à leur tour, jusqu'à ce que plus rien ne bouge — la patience baisse, elle atteint 0, la durée du tour passe à 10 s. Le moteur actuel le fait par vagues et prouve que ça s'arrête ; la même garantie est due.
3. Ensuite seulement, l'app regarde si la scène est finie. C'est ce qui permet la fausse mort.
4. Puis les textes arrivent : la notification s'affiche, le fil joue les répliques écrites, et les textes pour le meneur partent avec l'appel suivant.

## Les questions à l'apprenant

**Une question se pose dans le dialogue, et l'apprenant y répond en parlant.** Pas de fenêtre ni de boutons : c'est une app de conversation, et répondre à une question du jeu est encore parler anglais — le juge marque la réponse comme toute autre phrase. La question est posée par le narrateur ou par un personnage, écrite d'avance ou improvisée par le meneur sur consigne : *« The tunnel splits. Left or right? »*, *« So — what do we call her? »*

**Le meneur évalue la réponse, et en choisit toujours une.** La case est une liste — *left / right* — ou un texte — un nom —, et **elle a toujours une valeur « pas de réponse »**, marquée comme telle dans la fiche : *ni l'un ni l'autre*, *n'a pas répondu*. Le propre d'une question est de ne pas laisser le choix de ne pas répondre : *« I'll go back »* et *« Hmm »* y tombent tous deux, et une réponse qui tombe à côté n'est jamais ignorée ni forcée vers une option. Le meneur la lit au cran « déduire » — *« Maybe… yeah, why not »* vaut oui —, jamais « inventer », qui déciderait à la place de l'apprenant ; « je ne sais pas » n'a pas lieu d'être, la valeur « pas de réponse » en tenant lieu.

**La fiche dit ce que devient « pas de réponse » : un chemin comme les autres, ou un blocage.** Un chemin, l'auteur l'écrit comme n'importe quel événement — au colis, ne pas répondre à *« Do you open it? »* laisse le colis fermé. Un blocage retient l'histoire : elle n'avance pas au-delà du choix tant qu'il n'est pas fait. **Jamais de repli qui tranche à la place de l'apprenant** : *« Robin revient avant que tu te décides »* ferait dire à l'histoire une chose que personne n'a décidée, et tomberait au milieu du fil.

**Un tour de réponse se fait en deux appels.**

1. L'apprenant répond : *« Yes, I open it. »*
2. **Un appel court au meneur, qui ne rend que la réponse** : `opened` = vrai. Aucune réplique.
3. **Le moment « réponse à une question »** : les événements qui lisent la réponse partent — le narrateur dira *« Inside, wrapped in newspaper: a gold watch »*, le meneur recevra *« They have seen the watch »*. Ce moment ne voit ni les notes ni les portes, le juge n'ayant pas encore rendu son verdict.
4. **L'appel ordinaire du meneur**, qui répond en sachant tout ce qui en découle : Robin, depuis la porte, *« What are you doing? »*

Avec un seul appel, le meneur répondrait avant que les événements tournent, et raconterait une conséquence que l'auteur a écrite autrement — *« some old letters »* là où la fiche dit une montre. Le prix est un appel de plus avant la voix, sur ces tours seulement ; l'appel est court et sa latence n'est pas mesurée (`providers.md`).

**Quand ça bloque, qui a posé la question décide de la suite.**

- **Le narrateur** : l'app rejoue sa phrase telle quelle, avec l'audio déjà synthétisé, sans appeler le meneur et sans ligne de plus dans le fil, puis le micro se rouvre. Une question du narrateur vient du jeu et n'interpelle personne dans la scène : le jeu la repose. Le prix est qu'aucun personnage ne répond à *« I'll go back »* ; l'auteur qui le veut fait poser la question par un personnage.
- **Un personnage** : l'app envoie au meneur *« They have not answered Lou's question. As Lou, react to what they said, then ask again in other words. Do not move the story past this choice. »* — Lou : *« Back? The rocks are down behind us. Left or right — pick one. »*

**Une réponse reformulée refait les deux appels, et les écritures de la tentative refaite disparaissent avec sa réplique — avec tout ce qu'elles ont déclenché au moment de la réponse**, qui se recalcule comme à la réouverture : la montre révélée quitte le fil comme la réplique, et un levier qui revient s'annonce comme tout levier qui change. Ce qui vient des mesures reste, la tentative ayant eu lieu. C'est la règle générale, pas une règle des questions : **ce que le meneur écrit appartient à la tentative.** `activity.md` refuse de garder une réplique qui répondait à une phrase qui n'existe plus ; garder ce qu'elle a établi reviendrait au même par un autre chemin. Les écritures du meneur étant gardées avec leur tour, l'app sait lesquelles retirer.

**Un blocage peut enfermer qui ne comprend pas la question** — texte des répliques brouillé, accent difficile, reconnaissance qui déforme la réponse. La phrase redite et la question reformulée aident, rien ne le garantit : le blocage est à réserver aux vrais embranchements.

**Le gabarit.** Une question, c'est une case et quatre ou cinq événements — la poser, la faire évaluer, lire la réponse, bloquer ou suivre le chemin. La fiche l'écrit en une fois :

```
question  direction
  posée par      : le narrateur          (ou un personnage, ou le meneur qui l'improvise)
  quand          : fermeture du passage 7
  texte          : "The tunnel splits. Left or right?"
  réponses       : left / right / neither (pas de réponse)
  pas de réponse : bloque                (ou un chemin, traité par un événement)
```

L'app le déplie au chargement en cases et en événements ; le moteur ne connaît rien d'autre. **Ce n'est pas le raccourci refusé plus bas** : celui-là donnait une seconde façon de dire une chose qu'un seul événement dit déjà, celui-ci nomme un motif de plusieurs pièces qui ne se lit pas d'un coup d'œil déplié. Une erreur de vérification dit de quelle question vient l'événement fautif, et la fiche dépliée s'affiche sur demande.

**Pour le guide d'écriture** : une question à l'apprenant est un choix de joueur, à réserver à ce qui doit être sans ambiguïté parce qu'une histoire bifurque dessus ; pour le reste, un personnage demande dans le fil et le meneur écrit une case à partir de ce qui a été dit. Et une liste gagne à nommer ce qu'un apprenant dira vraiment.

## Rouvrir une séance

**On stocke ce qui ne se retrouve pas, on recalcule le reste** — la règle de `../reference.md`, appliquée aux cases.

- **Se stocke**, avec son écrivain et son tour : ce que le meneur a écrit, qu'on ne redemande pas sans risquer une autre réponse ; ce que le hasard a tiré ; ce que l'apprenant a rempli ; et **ce qui est parti pendant l'enregistrement**, les horloges étant lues en direct et le silence en cours ne se lisant pas comme les silences qu'on calcule après coup.
- **Se recalcule** : les cases de l'app, depuis les mesures de chaque tentative, déjà gardées, et les poids et réglages copiés sur la séance ; tout ce que les événements ont écrit, les leviers, les activations.

**Rouvrir, c'est rejouer** : à chaque moment passé, l'app remet ce qui est stocké, recalcule le reste, fait tourner les événements, et arrive à l'état où la séance a été quittée — les lignes automatiques de la conversation du meneur se reconstruisent au passage. Stocker aussi les écritures des événements garderait deux fois la même chose, l'écriture et de quoi la refaire, et il faudrait choisir laquelle croire le jour où elles divergent. Une séance dont le moteur a changé de version ne se reprend pas, rejouer pouvant alors rendre autre chose (`store/Archive.kt`) ; c'est déjà la règle.

Aujourd'hui une séance rouverte repart des positions déclarées, faute d'avoir gardé ce que les règles ont lu (`TurnPipeline.kt`, `../../TODO.md`).

## La vérification

**Au chargement, et tout problème fait échouer le chargement.** `DefinitionTest` charge toutes les fiches livrées sans appareil, donc une fiche cassée ne passe pas le test et ne se livre pas ; et un avertissement n'est jamais lu, c'est alors l'apprenant qui découvre l'erreur en jouant.

Ce qui se vérifie :

- **les cases** — un événement qui change une case de l'app ; une case que personne n'écrit et qui n'a pas de valeur de départ ; une liste réduite à *oui / non* ; une case cachée au meneur qu'un événement lui demande ou qu'un texte pour lui cite ;
- **les tests** — sur un texte ; sur une valeur hors de la liste ou hors des limites ; sur une case à un moment où elle n'existe pas, `app.silence` à la fermeture du passage ;
- **les effets** — activer un événement qui n'existe pas ; citer une case inconnue ; faire parler pendant un enregistrement ou à la fin d'une tentative ; une consigne sur un marquage inconnu ;
- **Libre** — une fiche dont la porte est `free` ne touche aucun levier, ni au départ ni par un événement, ne déclare pas de poids, l'app mettant 1 partout, n'envoie aucune consigne, ne teste aucune case de l'app sauf `app.passage`, et ne termine jamais ;
- **l'histoire** — une scène qui cite un personnage que l'histoire ne déclare pas ; un embranchement qui lit une case qui ne vit pas toute l'histoire ; une scène qui peut finir ratée sans que l'histoire dise ce qui suit ; une scène qui redéclare une case de l'histoire.

Restent invérifiables : qu'une consigne durcisse vraiment, et ce que le meneur fait de ce qu'on lui envoie.

**Libre n'a aucun levier, pas seulement aucun levier de pression**, faute d'en trouver un qui n'en soit pas : les vies, la durée du tour, la capture et les essais évidemment ; le texte des répliques, leur longueur et leur complexité, retirés de Libre ensemble ; l'écho et l'avance, qui décident de ce qu'on exige ; le bruit et le filtre, qui durcissent l'écoute. Et en Libre les leviers sont à l'apprenant (`activity.md`) : une fiche qui en poserait un lui prendrait un réglage. `tile-authoring.md`, qui autorise *un levier qui décrit la personne rencontrée*, est à corriger.

**Libre n'a pas de consigne non plus.** Le juge y juge déjà la pertinence contre la situation et la conversation ; une consigne n'ajoute qu'une exigence de plus, donc elle déplace une marque que l'apprenant n'a pas choisie, comme un levier posé par la fiche lui prendrait un réglage. Sous *« raconte au passé »*, *« I'll take the next train »* sur le quai serait marqué, et redit si l'apprenant se fait reprendre sur la pertinence. Une consigne qui fait jeu — *parle une minute sans dire « I »* — est une pression annoncée, donc un défi.

**Ni d'événement qui lise la façon dont l'apprenant parle.** Un voisin plus soupçonneux à chaque silence de 5 s, une Lou qui se ferme quand la correction tombe en C, c'est la même pression par un autre chemin. Le numéro du passage reste, qui rythme l'histoire sans rien juger — le troisième voyageur au passage 8 —, et les cases que le meneur écrit, qui jugent avec l'histoire et pas avec une note. Une relance après un long silence est une aide, et en Libre l'aide est à l'apprenant aussi : s'il la faut, c'est un réglage qu'il allume.

**La vérification lit la porte ; le moteur, toujours pas.** `activity.md` écrit que *« mode » n'est pas un champ* et que rien ne lit `door`. Ce principe a été écrit contre un champ qui change le comportement — un mode que le moteur lirait, qui ferait décrire deux fois la même séance et interdirait un niveau d'arcade dans une campagne. Ici rien ne se joue autrement selon la porte : la vérification contrôle qu'une fiche respecte les règles d'écriture de la porte par laquelle elle est offerte, et une fiche Libre valide se joue comme toute autre. Le principe ne s'applique pas. La même lecture pourra plus tard exiger des vies d'une fiche d'arcade, une fin d'un défi.

## Ce que fait l'IA

Le **meneur** écrit ses répliques, entièrement libres, que rien de mécanique ne lit ; l'écho et `intended`, que l'app fixe et non la fiche ; et les cases que la fiche lui demande. Il les écrit **avant** ses répliques, donc il parle en sachant ce qu'il vient d'établir : quand il écrit que `named` est vrai, Dana réagit dans la même réplique sans que rien le lui demande.

Sur un tour de réponse à une question, le meneur est d'abord appelé seul pour évaluer la réponse, sans réplique (plus haut).

Le **juge** écrit les marques. Rien d'autre.

## Ce qui circule

**Une case de l'histoire est connue avant la réplique ; ce que l'app lit du tour, après.** L'une peut changer la réplique du tour même, l'autre n'agit que sur la suite. Ce n'est pas un choix mais l'ordre dans lequel les choses arrivent.

**Le meneur ne voit une case de l'app que si un événement la lui dit**, par un texte, au tour suivant — réagir juste après la réplique casserait le fil. C'est la règle actuelle : l'état n'atteint le modèle que par la porte de devant. Ce qui en devient impossible, et c'est voulu :

- réagir à une note dans la réplique qui répond à la phrase notée ;
- réagir à la prononciation, le meneur ne recevant que le texte transcrit ;
- faire dépendre un fait de l'histoire d'une note — *« Val cède si c'était convaincant »* passe par un événement qui prévient le meneur, qui en tient compte au tour suivant ;
- un personnage qui juge l'anglais comme l'app le juge : il réagit à ce qu'il lit, avec son propre jugement ;
- un bilan dit par un personnage sans qu'un événement de clôture lui transmette la note.

**Une case de la fiche est connue du meneur par défaut**, et l'auteur marque celles qu'il veut lui cacher : un compteur technique, un secret que le meneur ne doit pas trahir. Un levier ne l'est pas, sauf ceux qui lui envoient déjà un texte ; une case de l'app, jamais. C'est un choix d'auteur, qui ne se déduit de rien dans la fiche, donc le déclarer ne fait pas de seconde source.

**Quand un moment se termine, chaque case connue du meneur qui a changé lui laisse une ligne dans la conversation**, avec sa valeur à la fin et au début du moment : *(state) The barman's trust in them: 5 out of 10 (was 7).* Une ligne par case, quel que soit le nombre d'écritures et leurs écrivains ; aucune si la case est revenue à sa valeur de départ. Le meneur voit ainsi deux choses symétriques : ce qu'il a fait, dans sa réponse relue telle qu'il l'a écrite, et l'état du monde, dans ces lignes — au prix de quelques lignes redondantes quand il est le seul à avoir écrit. Un texte de l'auteur s'y ajoute s'il veut que le changement soit joué comme un moment (*« He cheers up: they made him laugh »*), par un événement : le mettre sur la case dirait *quand elle change, envoie ceci*, ce qu'un événement dit déjà.

**Le juge voit ce que l'apprenant peut savoir, et rien de ce qui lui est caché** : la situation, la conversation, les consignes, les textes montrés à l'apprenant. Pas la description des personnages, ni les cases que l'écran n'a pas montrées, ni les textes pour le meneur seul. C'est le sens de la règle de `../reference.md` — *une mesure a le droit de lire tout ce dans quoi l'apprenant se trouve* —, et l'apprenant se trouve dans ce qu'il voit et entend, pas dans les secrets des personnages. Au colis, Robin demande ce que contient le paquet et l'apprenant répond *« Books, I think »* : un juge qui saurait la montre volée le marquerait sur une information qu'on lui a cachée. Restent exclus, pour leurs propres raisons, les réglages, le passé de l'apprenant, et ce qu'il veut éviter.

## La fiche et l'histoire

**Deux sortes de textes.** Ceux que la fiche **déclare**, qui décrivent la scène et ses personnages — la situation, la description de Dana — et ne changent jamais pendant la partie. Et ceux qu'un **événement** envoie, à un moment. Faire passer les premiers pour des événements envoyés à l'ouverture *pour toute la partie* cachait l'ancien `brief` sous une durée inventée pour lui.

**Une fiche de scène déclare donc** ce qui ne bouge pas — le titre, le nom court, la situation, les avertissements, et dans une tuile Libre les personnages —, puis ses cases et ses événements.

**Une histoire a son fichier**, que le bloc d'`activity.md` n'a jamais eu : l'ordre des scènes, les embranchements (un événement au niveau de l'histoire, *après la scène 4, si `has-key` est vrai, scène 5, sinon 5 bis*), les cases qui vivent toute l'histoire, et **les personnages, déclarés une seule fois** — nom, genre, voix, description. Une scène cite ceux qui sont présents par leur clé. La description d'un personnage lui appartient et part au meneur chaque fois qu'il est présent.

**L'issue de chaque scène est une case de l'histoire**, `app.outcome.<scène>`, que l'app écrit quand la scène se termine : réussi, raté, ou vide si elle a été abandonnée. L'auteur ne la déclare pas, et un embranchement la lit comme toute case — *après la crevasse, si elle est ratée, le campement des blessés ; sinon, le sommet*. C'est la seule case de l'app qui passe la scène, n'étant pas une mesure mais le résultat, que `../reference.md` fait lire à la scène suivante.

**Une scène qui peut finir ratée oblige l'histoire à dire ce qui suit** : un embranchement, la rejouer, ou la fin de l'histoire. Aucune suite par défaut : continuer viderait le raté de son sens, rejouer peut tourner en rond sur une scène trop dure, tout arrêter punit une longue histoire pour une scène. Ce que l'échec veut dire est à l'auteur.

## Le prompt du meneur

Trois parties, qui existent déjà (`providers/ConversationPrompt.kt`) et reçoivent chacune une seule sorte de chose.

- **La tête** : ce qui ne change jamais pendant la partie — les règles de l'app, ce que l'apprenant veut éviter, et les textes que la fiche déclare, situation et personnages. Elle ne bouge pas, donc le fournisseur la garde en cache d'un tour à l'autre.
- **La conversation** : les répliques, les réponses du meneur relues telles qu'il les a écrites, cases comprises — il imite ses propres réponses passées, et les relire en prose a déjà fait revenir un tour vide (`premium-chain.md`) —, **les textes d'événement pour le meneur, au passage où ils sont partis**, et les lignes des cases qui ont changé. Un événement se place là où il a eu lieu, comme les faits établis le font déjà : posé en tête, il se lirait comme un donné de départ, et le meneur jouerait quelqu'un qui l'a toujours su. Un barman qui change quatre fois d'humeur laisse quatre traces datées, et le meneur peut jouer la trajectoire, pas seulement l'état.
- **« For this turn »** : ce que l'app calcule pour le tour qui vient — le numéro du passage, les cases à écrire maintenant, les leviers qui lui envoient un texte, et **les consignes en vigueur, répétées** tant qu'elles durent : elles ont une fin et doivent être suivies dans la réplique qui vient, et enfouies sous trente tours elles ne gouverneraient plus rien.

## Les surfaces

- **L'écran avant la discussion** : la situation, et les cases demandées à l'apprenant au lancement — les trous, le genre quand la fiche le laisse ouvert.
- **Le fil** : les répliques du meneur et de l'apprenant, et les textes que l'apprenant lit dans le fil.
- **La notification** : les textes que l'apprenant lit en notification, et les phrases mécaniques des leviers.
- **La barre d'état** : ce que l'app y met, le nom court et le décompte.
- **Le bilan d'un passage** : les notes du passage.
- **Le prompt du meneur** : plus haut.
- **Le prompt du juge** : ce que l'apprenant peut savoir, et la phrase à juger.
- **La base** : ce qui a été dit, les mesures de chaque tentative, et les écritures qui ne se retrouvent pas. Ce qui se recalcule — les notes, ce que les événements ont écrit, quels événements sont actifs, où sont les leviers — n'y est pas.

## Ce que ça remplace

| aujourd'hui | ici |
|---|---|
| question | une case de la fiche, et un effet qui la demande au meneur |
| condition jugée | une case vrai/faux demandée au meneur, et un événement qui la lit |
| choix du modèle dans un menu | une case à liste demandée au meneur, et un événement par valeur |
| décideur « hasard » | une case tirée au hasard, et un événement par valeur |
| trou (`slots`) | une case demandée à l'apprenant au lancement |
| horloge, condition sur une note, passage N, levier qui bouge ou qui atteint | un test sur une case |
| patch | un événement qui change des cases |
| interrupteur, drapeau, fin | une case |
| vies à zéro qui terminent, propriété du levier | un événement écrit |
| levier tenu par le modèle | un levier dont la valeur envoie d'elle-même un texte au meneur |
| message au modèle | un texte pour le meneur |
| consigne | un texte pour les deux, rattaché à un marquage |
| phrase de mise en scène d'un patch | un texte pour l'apprenant, notifié avec la phrase mécanique |
| message qui fait parler tout de suite | l'effet « faire parler un personnage » |
| tour scripté | un texte pour les deux, dans le fil |

Rien de ce qui s'écrivait ne devient impossible. Un auteur écrit un événement par valeur là où l'ancien menu portait ses effets dans chaque option ; aucun raccourci ne le remplace, qui serait une seconde façon de dire *quand cette case vaut ceci, fais cela*. Si c'est pénible, c'est à l'outil d'écriture d'aider, pas au format.

## Le jeu d'épreuve

Quatre scènes inventées pour mettre le modèle à l'épreuve, les fiches livrées se servant de trop peu de choses pour le faire.

- **Le quai (Libre).** Lou et l'apprenant sur un quai, la nuit ; un narrateur fait les annonces. Au passage 8, un troisième voyageur arrive et se dispute avec Lou sans que l'apprenant parle. S'il fait rire Lou, elle lui confie quelque chose. Aucun enjeu.
- **Le comptoir (défi).** Obtenir une table avant la fermeture. Le barman a une patience de 3 ; chaque phrase mal formée lui en retire un ; à zéro, il ne laisse plus que 10 s pour parler. Consigne : être poli sans dire *please*. Réussi quand le barman accepte, raté à zéro vie.
- **La rafale (arcade).** Des questions de plus en plus rapides ; tous les 5 passages, le débit monte d'un cran et le texte des répliques se brouille un peu plus. Trois vies, un score.
- **Le colis (histoire, trois scènes).** Nico confie un colis sans dire ce qu'il contient ; Robin, douanière, interroge l'apprenant, qui peut mentir ; l'apprenant retrouve Nico, dans deux versions selon la douane.

**Le barman, en détail.** Au tour 7, l'apprenant répond *« Yes there is »* à *« Is it so? »*. Le meneur répond normalement ; le juge marque, la porte des mots se ferme. À la fermeture du passage, l'événement « porte des mots fermée » retire une vie, fait baisser `patience` de 1 et envoie au meneur, une fois, *« Reproach them. »* `patience` atteint 0 : un second événement passe `lever.turn-length` à 10 s et envoie à l'apprenant *« Le barman s'impatiente, il ne te laisse plus parler longtemps »*, affiché avec *« Durée du tour : 10 s »*. Au tour 8, le barman fait son reproche. `patience` étant connue du meneur, la conversation porte à la fermeture du passage 7 *(state) The barman's patience: 0 out of 3 (was 1).*

**Dana.** Case `named`, vrai/faux, vide au départ. Un événement actif la demande au meneur à la fermeture de chaque passage. Quand `named` devient vrai : à l'apprenant, en notification, *« Dana a dit ce qui l'a blessée »*, et l'événement qui demande se désactive.

## Ouvert

- **Annoncer les règles d'un défi avant de commencer** (*« chaque phrase mal formée coûte une vie »*) : une question d'écran pour la porte des défis, hors de ce modèle.

## La méthode

Le modèle se met à l'épreuve ici, sur papier, avant le code : chaque point ouvert se tranche sur le jeu d'épreuve, puis les treize fiches livrées se réécrivent dedans. S'il tient, le moteur se réécrit ; s'il casse, on l'aura vu avant.
