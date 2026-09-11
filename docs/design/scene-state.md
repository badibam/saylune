# L'état d'une scène

Doc transitoire, ouvert le 2026-09-11. Il remplace la mécanique qui fait bouger une scène — règles, déclencheurs, décideurs, paquets, patchs, questions, conditions jugées, drapeaux, messages, tours scriptés — par un seul modèle : **des cases, qui les écrit, des événements qui les lisent, et des textes envoyés à quatre destinataires.** Il s'élague quand le moteur est réécrit et qu'`activity.md` a repris ce qui lui revient.

## Pourquoi

Une vingtaine de notions font aujourd'hui bouger une scène, chacune ajoutée pour un cas précis, et chacune avec sa façon d'être gardée, affichée et lue. Ça s'est vu en essayant de faire réagir Dana (`the-grudge`) au moment où l'apprenant nomme ce qui la blesse :

- la question et la condition jugée font le même travail de deux façons — `activity.md` dit qu'aucune condition ne lit une réponse, puis qu'une réponse fermée sert à choisir la scène suivante ;
- le oui d'une condition jugée n'est gardé nulle part, là où la réponse à une question l'est ;
- `staging` nomme à la fois la description du personnage envoyée au modèle et la phrase qu'une règle affiche à l'apprenant ;
- un personnage n'existe qu'à l'intérieur d'une scène, donc dans une histoire sa voix et son genre seraient tirés à nouveau à chaque scène.

## Les cases

Tout ce qui peut changer pendant une scène est une case : un nom et une valeur. La position d'un levier, les vies, la patience du barman, un événement actif ou non, ce que le modèle a établi, ce que la mesure a lu, un tirage, la fin.

**Quatre sortes de valeurs.**

- **Vrai ou faux** — Lou a ri, le voyageur est arrivé, un événement est actif. Une liste ne peut pas se réduire à *oui / non* : c'est alors un vrai/faux, qui ne s'écrit que d'une façon.
- **Nombre**, avec ses limites — `patience` de 0 à 3, `score` à partir de 0 sans maximum. Un déplacement s'arrête à la limite.
- **Liste**, une valeur parmi des valeurs écrites d'avance — ce que fait Val, l'humeur tirée. Une liste peut être **ordonnée**, et dit alors dans quel sens elle va : A est le haut des notes ; pour un levier, le bout le plus dur n'est pas toujours le dernier cran, l'écho étant le plus dur à *none* (`HardSide`, `levers/Levers.kt`).
- **Texte** — ce que Lou a confié, le trou de l'apprenant. Aucun événement ne le teste, aucun code ne comprenant *« the flat in Lyon »* ; il est lu par le modèle et par l'écran, et cité dans d'autres textes.

**La sorte ne dépend pas de l'écrivain**, à deux limites près : le hasard n'écrit pas de texte, ne sachant que choisir parmi des valeurs données ; et les cases de la mesure sont déclarées par l'app, pas par la fiche. Le modèle peut écrire un nombre (*combien de verres le barman a servis*), l'auteur un texte (*« the platform »*, cité ensuite).

**Toute case peut être vide** : pas encore demandée au modèle, rien de mesuré (la note de prononciation d'un passage dont l'analyse n'a pas tourné), laissée vide par l'apprenant, ou sans valeur de départ jusqu'à ce qu'un événement la remplisse. La raison du vide n'est pas gardée, aucun cas n'ayant besoin de la lire. *« Je ne sais pas »* n'est pas un vide mais une réponse du modèle (plus bas, ouvert).

**La voix et le genre tirés d'un personnage ne sont pas des cases** : ce sont ses attributs, tirés une fois et gardés avec lui, donc toute l'histoire dans une histoire. Aucun événement ne les teste, et leurs valeurs viennent du catalogue d'un fournisseur que l'auteur ne connaît pas.

**Six écrivains.** L'auteur, l'apprenant, la mesure, le modèle qui parle, le hasard, un événement. Le juge n'en est pas un : il rend des marques, et c'est la mesure qui en tire les cases de note.

**Une case, un seul écrivain pendant la partie**, fixé par sa sorte. Une case de la mesure n'est écrite que par la mesure, sinon une fiche pourrait adoucir une note ; une case du modèle n'est écrite que par lui, ce qui a été établi dans l'histoire ayant été dit ; un événement ne change que les cases de l'auteur. La **valeur de départ** peut en revanche venir de plusieurs sources dans un ordre fixé : le genre d'un personnage vient de la fiche, sinon de l'apprenant, sinon du hasard.

**Une case vit aussi longtemps que ce à quoi elle appartient.** À la scène : les leviers, les activations, la fin, et les cases de la mesure — celles-ci jamais au-delà, rien de ce qui a été mesuré ailleurs n'entrant dans la lecture d'un tour (`../reference.md`). À l'histoire : les faits que l'auteur veut garder d'une scène à l'autre.

**Un texte peut citer une case**, et l'app le réécrit à chaque envoi avec la valeur du moment : *« Your patience with them: {patience} out of 3. »* C'est ce que fait déjà `{anchor}`.

## Les événements

Un événement dépend de trois choses.

1. **Un moment** : l'ouverture, pendant l'enregistrement, la fin d'une tentative, la fermeture d'un passage, la clôture. Chaque moment ne voit que ce qui existe à cet instant : pendant l'enregistrement, seulement les horloges.
2. **Être actif.** Un événement peut être actif ou non au départ, et un autre événement l'active ou le désactive.
3. **Un test sur une case**, d'une de deux sortes :
   - **un état** — `lives` = 0, note de correction ≤ C, silence ≥ 5 s, passage multiple de 5. Il repart à chaque moment où il est vrai : chaque mauvaise phrase coûte une vie ;
   - **un changement** — `named` devient vrai, `lives` diminue, un levier devient plus dur. Il ne part qu'une fois par changement.

**Un test sur une case vide est toujours faux**, sauf le test fait pour ça, « est vide ». Sans quoi « note de prononciation ≤ C → une vie en moins » coûterait une vie pour une analyse qui n'a pas tourné.

**Un seul test par événement, et l'activation sert de « et ».** La fausse mort s'écrit ainsi : A (actif) — quand `lives` = 0, remettre une vie, désactiver A, activer B ; B (inactif) — quand `lives` = 0, terminer. Combiner deux tests ouvrirait la voie au « ou », puis aux parenthèses, donc au langage de conditions que `rules/Rule.kt` refuse parce qu'on ne pourrait plus dire ce que fait une fiche sans la jouer. Décidé pour cette version ; un cas réel qui ne s'écrirait pas avec l'activation rouvrirait la question.

Certains événements ne testent rien que le moment : *à l'ouverture, Nico dit ton nom.*

## Les effets

**Changer une case de l'auteur** : lui donner une valeur (`patience` ← 3), ou la déplacer (`lives` − 1, `tempo` + 1 cran) — un déplacement s'arrête à la limite de la case sans erreur. Activer ou désactiver un événement. Terminer la scène, avec son issue : réussi, raté, ou la note décide. Quand une case de levier change, **l'app ajoute d'elle-même la phrase mécanique** (*« Durée du tour : 10 s »*) : l'auteur n'a pas à l'écrire et ne peut pas l'oublier.

**Envoyer un texte**, qui a un destinataire, une durée — une fois, N passages, toute la partie — et un contenu.

- **Au modèle qui parle**, dans la partie de son prompt reconstruite à chaque tour.
- **Au juge**, qui le lit en jugeant.
- **À l'apprenant** : une fois, c'est une notification ; avec une durée, il reste affiché.
- **Au fil** : une réplique écrite d'avance, avec son locuteur, personnage ou narrateur. Toujours une fois, puisqu'elle est dite.

Une consigne (*« Tell it in the past tense »*) est le même texte envoyé à trois destinataires : l'apprenant, le modèle qui parle et le juge.

**Faire parler le personnage**, sans attendre l'apprenant : à l'ouverture, à la fermeture d'un passage, à la clôture. Jamais pendant un enregistrement ni à la fin d'une tentative.

**Demander une case au modèle** : *à la fermeture de chaque passage, demander `named`* ; et un second événement, *quand `named` devient vrai, désactiver le premier*. La case ne dit que ce qu'elle est ; quand elle est demandée, et jusqu'à quand, est toujours écrit dans un événement, avec les mêmes tests et la même activation que partout.

**Tirer une case au hasard** : *à l'ouverture, tirer `mood`.*

**Ce qui est affiché au moment d'un événement est écrit par l'auteur.** Un texte que l'IA écrirait devrait exister à la fermeture du passage, et aucun appel n'est à la fois fini à ce moment et fait pour ça : le modèle qui parle a fini avant de savoir le jugement, le juge ne fait pas de fiction, et un appel de plus ferait attendre le micro. La phrase narrative s'affiche en outre à côté de la phrase mécanique, qu'un texte improvisé peut contredire (`activity.md`). La liberté du modèle est sa réplique suivante, où il joue la réaction comme il veut.

## L'ordre, dans un moment

1. Les événements du moment partent.
2. Ceux qu'un changement vient de déclencher partent à leur tour, jusqu'à ce que plus rien ne bouge — la patience baisse, elle atteint 0, la durée du tour passe à 10 s. Le moteur actuel le fait par vagues et prouve que ça s'arrête ; la même garantie est due.
3. Ensuite seulement, l'app regarde si la scène est finie. C'est ce qui permet la fausse mort.
4. Puis les textes arrivent : la notification s'affiche, le fil joue les répliques écrites, et les textes pour le modèle partent avec l'appel suivant.

## Ce que fait l'IA

**Elle ne déclenche rien : elle écrit des cases, seulement celles qu'on lui demande, et au moment où on les lui demande.** Ce sont les événements, écrits par l'auteur, qui en tirent des conséquences. Si le modèle pouvait écrire une case quand il veut, un événement partirait à un moment que personne n'a prévu.

Le **modèle qui parle** écrit sa réplique, entièrement libre, que rien de mécanique ne lit ; l'écho et `intended`, que l'app fixe et non la fiche ; et les cases que la fiche lui demande, en texte libre (un souvenir, relu par lui et par l'écran) ou en liste (un souvenir que les événements peuvent aussi lire). Il les écrit **avant** sa réplique, donc il parle en sachant ce qu'il vient d'établir : quand il écrit `named` = oui, Dana réagit dans la même réplique sans que rien le lui demande.

Le **juge** écrit les marques. Rien d'autre.

**Le modèle ne voit jamais un levier.** Pour qu'il pèse sur la mécanique, l'auteur lui demande une case dans les mots de l'histoire — *« How does your trust in them change? grows / stays / drops »* — et c'est un événement qui décide que *drops* retire une vie. Le modèle décide de ce que le personnage ressent, l'auteur de ce que ça coûte.

## Ce qui circule

**Une case de l'histoire est connue avant la réplique ; une case de note, après.** L'une peut changer la réplique du tour même, l'autre n'agit que sur la suite. Ce n'est pas un choix mais l'ordre dans lequel les choses arrivent.

**Le modèle qui parle ne voit une case de la mesure que si un événement la lui dit**, par un texte, et au tour suivant — réagir juste après la réplique casserait le fil. C'est la règle actuelle : l'état n'atteint le modèle que par la porte de devant. Ce qui en devient impossible, et c'est voulu :

- réagir à une note dans la réplique qui répond à la phrase notée ;
- réagir à la prononciation, le modèle ne recevant que le texte transcrit ;
- faire dépendre un fait de l'histoire d'une note — *« Val cède si c'était convaincant »* passe par un événement qui prévient le modèle, qui en tient compte au tour suivant ;
- un personnage qui juge l'anglais comme l'app le juge : il réagit à ce qu'il lit, avec son propre jugement ;
- un bilan dit par le personnage sans qu'un événement de clôture lui transmette la note.

**Les faits que le modèle a établis lui reviennent**, dans la conversation, à l'endroit où il les a établis. C'est la seule circulation sans événement.

**Le juge voit l'histoire**, parce que la pertinence se juge contre elle : *« I'll go there »* est de l'anglais correct et hors sujet si Robin demande ce qui s'est passé hier. Ce qu'une mesure ne lit jamais reste ce que `../reference.md` écrit : les réglages et le passé de l'apprenant.

## La fiche et l'histoire

**Une fiche de scène a deux parties : les cases et les événements.** Un texte n'existe jamais seul, il part toujours à un moment : la situation et la description du personnage sont des textes envoyés à l'ouverture pour toute la partie. S'y ajoutent ce qui ne bouge pas — le titre, le nom court, les avertissements — et, dans une tuile Libre, les personnages.

**Une histoire a son fichier**, que le bloc d'`activity.md` n'a jamais eu : l'ordre des scènes, les embranchements (un événement au niveau de l'histoire, *après la scène 4, si `has-key` = oui, scène 5, sinon 5 bis*), les cases qui vivent toute l'histoire, et **les personnages, déclarés une seule fois** — nom, genre, voix, description. Une scène cite ceux qui sont présents par leur clé. La description d'un personnage lui appartient et part au modèle chaque fois qu'il est présent.

## Les surfaces

**Chaque surface lit un destinataire.** Un auteur choisit à qui il parle, et l'endroit où ça s'affiche en découle.

- **L'écran avant la discussion** : le texte de situation, et les cases que l'apprenant écrit — les trous, le genre quand la fiche le laisse ouvert. C'est le seul endroit où l'apprenant écrit une case.
- **Le fil** : ce qui a le fil pour destinataire, plus les répliques du modèle et de l'apprenant.
- **La notification** : les textes pour l'apprenant envoyés une fois, et les phrases mécaniques.
- **L'affichage durable** : les textes pour l'apprenant qui ont une durée, dont les consignes.
- **Le bilan d'un passage** : les cases de note du passage.
- **Le prompt du modèle qui parle** : les textes qui lui sont destinés, la conversation avec ses faits, et les cases à écrire à ce tour.
- **Le prompt du juge** : l'histoire, les textes qui lui sont destinés, la phrase à juger.
- **La base** : ce qui a été dit, et toutes les cases écrites pendant la partie, sur le tour où elles l'ont été. Ce qui se recalcule — quels événements sont actifs, où sont les leviers — n'y est pas.

## Ce que ça remplace

| aujourd'hui | ici |
|---|---|
| question | une case écrite par le modèle, et un effet qui la demande |
| condition jugée | une case oui/non écrite par le modèle, et un événement qui la lit |
| choix du modèle dans un menu | une case à liste écrite par le modèle, et un événement par valeur |
| décideur « hasard » | une case écrite par le hasard, un effet qui la tire, un événement par valeur |
| horloge, condition sur une note, passage N, levier qui bouge ou qui atteint | un test sur une case |
| patch | un événement qui change des cases |
| interrupteur, drapeau, fin | une case |
| vies à zéro qui terminent, propriété du levier | un événement écrit |
| message au modèle, consigne, levier tenu par le modèle | un texte vers le modèle |
| phrase de mise en scène d'un patch, réponse affichée | un texte vers l'apprenant |
| message qui fait parler tout de suite | l'effet « faire parler le personnage » |
| tour scripté | un texte vers le fil |

Rien de ce qui s'écrivait ne devient impossible. Un auteur écrit un événement par valeur là où l'ancien menu portait ses effets dans chaque option ; aucun raccourci ne le remplace, qui serait une seconde façon de dire *quand cette case vaut ceci, fais cela*. Si c'est pénible, c'est à l'outil d'écriture d'aider, pas au format.

## Exemples

**Dana.** Case `named`, vrai/faux, écrite par le modèle, vide au départ. Un événement actif demande `named` à la fermeture de chaque passage. Quand `named` devient vrai : à l'apprenant, une fois, *« Dana a dit ce qui l'a blessée »*, et l'événement qui demande se désactive.

**Le barman.** Au tour 7, l'apprenant répond *« Yes there is »* à *« Is it so? »*. Le modèle répond normalement ; le juge marque, la porte des mots se ferme. À la fermeture du passage, l'événement « porte des mots fermée » retire une vie, fait baisser `patience` de 1 et envoie au modèle, une fois, *« Reproach them. »* `patience` atteint 0, un second événement passe la durée du tour à 10 s et envoie à l'apprenant *« Le barman s'impatiente, il ne te laisse plus parler longtemps. »* Au tour 8, le barman fait son reproche. Un texte permanent pour le modèle cite `{patience}`, donc il la connaît à chaque tour.

**La confiance.** Case `trust`, liste *grows / stays / drops*, demandée au modèle à chaque passage. Quand `trust` = drops : une vie en moins. Quand `trust` = grows : une vie en plus.

**Le quai.** Case `lou-laughed`, vrai/faux, demandée à chaque passage jusqu'à ce qu'elle devienne vraie. Alors : au fil, le narrateur, *« The replacement bus will leave at 1:40. »*

## Ouvert

- **La description du personnage chez le juge.** Elle lui a été retirée pour qu'elle ne déteigne pas sur `intended`, que le modèle qui parle écrit désormais ; la raison ne tient plus, c'est à revérifier. Et ce que l'apprenant veut éviter, qui n'est pas un réglage de mesure mais n'a rien à faire dans un jugement.
- **Une réplique provoquée à la fermeture d'un passage.** Refusée pour réagir à une note, parce qu'elle casse le fil ; pas tranchée pour le reste — l'annonce du quai en est une.
- **« Je ne sais pas »**, valeur possible d'une case du modèle quand il n'a le droit ni de déduire ni d'inventer. Un événement qui lit la case doit savoir qu'elle peut valoir ça.
- **Les cases de la mesure** : lesquelles, et à quelle finesse — par tentative, par passage, par mot. L'app les déclare, pas la fiche ; la liste n'est pas faite.
- **Rouvrir une séance recalcule tout depuis ce qui est gardé.** C'est l'objectif ; il reste à vérifier que tout ce qu'un événement lit est gardé, les horloges de l'enregistrement comprises. Aujourd'hui une séance rouverte repart des positions déclarées (`../../TODO.md`).
- **La vérification au chargement.** Le format rend vérifiable ce que l'actuel ne permet pas : un événement qui change une case qui n'est pas à l'auteur, un texte qui cite une case inconnue, un test sur une case en texte libre. `Definitions.validate` est vide.
- **Libre, dans ce langage** : aucun événement ne touche un levier de pression, aucun ne termine, les poids restent à 1.

## La méthode

Le modèle se met à l'épreuve ici, sur papier, avant le code : Dana, Val, le quai, le barman, la fausse mort, la rampe, un embranchement d'histoire, et les treize fiches livrées. S'il tient, le moteur se réécrit ; s'il casse, on l'aura vu avant.
