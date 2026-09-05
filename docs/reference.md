# speakup — référence

App Android de pratique de l'anglais oral : conversation libre avec une IA, jamais interrompue, doublée d'un travail de la grammaire et de la prononciation à la demande. Ce document est le point d'entrée ; il porte les décisions **propres au projet**, celles qu'aucune facette de sagesse ne couvre. Le craft transverse vit dans les modules abonnés (cf. `../manifest.md`).

Les autres docs, à ouvrir au besoin. Ceux d'à côté portent **ce qui est vrai et se consulte** :

- `analysis.md` — l'analyse, brique par brique : ce qu'elle lit, ce qu'elle rend, ce qui est mesuré et ce qui reste à écrire.
- `qualification.md` — comment on la vérifie : la procédure, et le jeu d'essai étiqueté qu'elle déroule. Rejouable par un tiers.
- `conversation-chain.md` — la latence mesurée de la chaîne reconnaissance (STT) → modèle de langue (LLM) → synthèse (TTS), chez Azure comme chez Replicate.
- `chatterbox-parameters.md` — ce que les modèles chatterbox acceptent et ce qu'ils font sans le demander : les paramètres de chaque variante, les balises entre crochets, la normalisation de ponctuation.

`design/` porte **ce qui est à faire et sera élagué une fois le code en place** :

- `design/activity-model.md` — **ce qui reste du modèle d'activité à écrire** : la définition, l'exécution et le bloc, les réglages comme positions de leviers, les règles qui les font changer, les prescripteurs, la pression et ses leviers, la grille des mesures et l'arbre de poids qui en fait des notes A–E, le blocage, le personnage, la capture, l'audio en segments. Élagué le 2026-09-01 de ce qui est construit — l'énoncé, l'activité et ce qui se stocke sont ci-dessous et dans le code.
- `design/implementation-plan.md` — **comment le chantier 0 s'écrit** : les dix-huit étapes et l'ordre qui les commande, ce qui reste dehors, ce qui se prouve par des tests et ce qui se règle au banc de calibration, et ce qui se décide en session plutôt que seul.
- `design/grammar-test-set.md` — les deux bancs du chantier 2, juge grammatical et fidélité du STT.
- `design/pixel-ui.md` — l'interface en pixel doux de console : le registre, la police et sa grille, la taille des lettres, les deux palettes prune et la règle qui les gouverne, les cadres, le dessin complet du tour marqué, le doigt, les images, le thème, et ce qui est écarté.
- `design/character-voices.md` — comment un personnage obtient sa voix : le couple public qui se fige au codage, les plafonds du BYOK, les modifieurs de livraison, et ce que l'hébergement d'une clé change — coûts au caractère, jeton éphémère, et le compteur qu'il retire.
- `design/added-sounds.md` — voir un son que l'apprenant ajoute : les trois portes fermées en chemin et pourquoi aucune ne se rouvre. La voie retenue est en service et se lit en brique 12 d'`analysis.md`.

## Le geste

L'utilisateur parle anglais à voix haute. L'IA lui répond en voix, dans l'accent choisi. Rien ne l'interrompt.

Quand il se trompe, l'IA reprend **dans sa réponse** au lieu de s'arrêter — à qui vient de dire *I have 25 years*, elle répond « Ah, you're 25! And where... ». La reprise passe dans le fil ; le tour fautif porte en plus une **marque discrète** (la portion concernée, colorée). Sans cette trace, la discrétion se retourne : on corrige, et personne ne l'apprend.

Une chose remarquée a **deux sorts, et deux seulement** : redire sur place, ce qui produit un énoncé de plus dans la même conversation ; ou devenir une activité suggérée, faite plus tard si on veut. On n'interrompt jamais pour partir travailler ailleurs.

Un réglage peut faire **attendre** la conversation sur une phrase à reprendre, et ce n'est pas cette interruption-là : on ne part nulle part, le sujet ne change pas, l'écran ne change pas. Ce que l'app dit alors n'est pas une réponse mise de côté puis reprise — l'appel au modèle rend **la continuation et un écho de reprise**, et l'app joue celui que la note désigne, donc rien n'est jamais rétracté (`design/activity-model.md`).

Le contrôle du moment appartient à l'utilisateur, jamais à l'app. Ce qui peut déclencher une suggestion pendant une conversation reste à spécifier.

## L'énoncé et l'activité

**L'énoncé est l'unité de matière** : un texte, un locuteur, l'audio, l'analyse. Toute parole dans l'app en est un — la réponse de l'IA comme la phrase de l'apprenant. La plupart n'en portent aucune analyse, et c'est voulu : une seule table pour tout ce qui se dit donne le fil de la conversation gratuitement, comme simple suite ordonnée, et aucun champ ne le porte. Un énoncé peut **pointer vers celui qu'il reprend**, par son identité et jamais par sa place, une place ne survivant pas à l'écriture sur disque. C'est tout ce qu'il faut pour redire.

**Une activité est atomique** : un seul objet, qui porte sa matière, ses réglages, son statut, ses horodatages et son résultat. Rien de séparé pour la portée, rien de séparé pour l'exécution. Ce n'est pas que la séparation serait inutile — rejouer s'en sert : une **définition** écrite d'avance se joue autant de fois qu'on veut, et chaque partie est une ligne. C'est qu'elle n'a pas à être modélisée comme une activité, la définition étant de la **donnée** et la partie une ligne en base ; ce que la ligne garde de sa définition, ce sont son identité et sa version, jamais un pointeur qu'il faudrait suivre pour lire ses réglages (`design/activity-model.md`). Les énoncés s'accrochent à l'activité, **toujours à un seul parent**, ce qui garde chaque requête sur la matière simple.

**Une activité issue d'une définition n'est pas jetable.** Son résultat porte l'avancement, qui se dérive et ne se stocke pas, donc la supprimer reverrouille un niveau ou vide une entrée de classement. Une conversation libre, elle, se jette sans conséquence. La différence se dit à l'utilisateur au lieu de se faire en douce.

**La conversation est une activité**, dans la même table et avec le même schéma, et tous les champs ont un sens pour elle : sa matière est ce dont on parle — le titre que l'IA lui donne —, ses réglages ceux de la séance, son statut dit si elle est encore ouverte. Ce qui la distingue tient à son comportement : son fil est la suite de ses énoncés, et elle se reprend en l'état. **Rien n'a à être terminé pour en commencer une autre** ; une activité non terminée est simplement une activité à reprendre, ce qui est l'état ordinaire de toutes sauf celle qu'on a.

**Le statut** : suggérée, acceptée, écartée, en cours, terminée, abandonnée. Une suggestion est une activité qui n'a jamais commencé, et l'écarter est un geste qui compte — sans lui, une suggestion refusée revient. **Accepter et lancer sont deux gestes** : une suggestion acceptée est retenue et n'a pas encore commencé.

Une suggestion apparaît pendant un tour, et l'ignorer ne la perd pas : elle reste en attente et se retrouve dans l'écran de l'activité qu'elle propose, où elle se valide ou se refuse plus tard. Le sélecteur d'activités et la file des suggestions sont donc le même écran vu deux fois.

**Le résultat** — une issue, le juge, sa date, et les réponses aux questions que la définition déclare — est **stocké et non recalculé**, parce qu'il repose sur un jugement que rien ne reproduit à l'identique. Savoir qui a jugé et quand est ce qui permet de comparer deux résultats séparés dans le temps ; sans cette attribution, toute agrégation mélange des juges sans le dire.

**La matière est du texte libre**, plus des pointeurs structurés là où ils sont gratuits — les sons, que l'analyse rend déjà codés. Rapprocher deux activités portant sur la même chose est un travail de lecture, que le modèle de langue fait ; exiger un vocabulaire fermé coûterait bien plus que ça ne rapporte.

## La même faute, la même marque

Une marque qui apparaîtrait parce que *le même son a déjà été raté trois fois* est incompréhensible pour qui la reçoit. **La même faute doit produire la même marque, à n'importe quel moment**, et c'est une exigence sur la lisibilité du marquage, pas sur ce dont l'app a le droit de se souvenir. Ce qu'elle interdit tient en une ligne : rien de ce qui a été mesuré ailleurs n'entre dans la lecture d'un tour.

Elle ne borne rien d'autre. Se souvenir, agréger, suggérer plus tard à partir de ce qui est dû sont le travail des prescripteurs (`design/activity-model.md`), et ils ne touchent jamais à la mesure.

**Et elle n'interdit pas de lire la situation.** Une mesure a le droit de lire tout ce dans quoi l'apprenant se trouve — la conversation en cours, et la consigne de l'activité s'il y en a une. *« I go there yesterday »* n'est fautif que sachant qu'on parle d'hier, et *« I'll go there »* est de l'anglais parfait qu'un défi « parle au passé » rend hors sujet. Ce qu'une mesure ne lit jamais, ce sont **les réglages** et **le passé de l'apprenant**. La règle porte sur la faute, pas sur la suite de mots : la même phrase dans deux situations différentes n'est pas la même faute. Le prix se paie à la vérification — un juge qui lit le contexte s'éprouve sur des couples (contexte, phrase), jamais sur des phrases seules.

## Les cinq aptitudes

Élocution, compréhension, correction, fluidité, pertinence. Elles sont **indépendantes** : on peut être intelligible et lent, correct et pauvre, fluide et faux.

- **Élocution** — les sons, l'accent des mots, la mélodie. Ce qui décide qu'on est compris.
- **Compréhension** — prendre ce qu'on vient de vous dire. À l'oral d'abord, à la vitesse de l'autre et avec ses réductions ; mais aussi la langue elle-même, qui reste à comprendre quand le texte est sous les yeux. Voir le texte est une aide en moins à trouver, pas une mesure qui disparaît.
- **Correction** — la phrase bien formée, et la règle appliquée en parlant plutôt que sue.
- **Fluidité** — trouver ses mots assez vite, enchaîner, ne pas s'arrêter au milieu.
- **Pertinence** — avoir visé juste : le mot précis, le registre, la nuance, et ce que la situation demandait. La seule dont l'échec est invisible : rien ne signale qu'on vient de dire une version pauvre de son idée. Elle se juge sur le même marquage que la correction, qui dit si c'est de l'anglais là où la pertinence dit si c'était l'anglais qu'il fallait — et c'est elle, jamais la correction, qui porte les exigences d'une activité.

Les réglages sont **fixés pour toute la durée** de la conversation. Ce ne sont pas des modes entre lesquels on bascule — **les régler est la façon de déclarer l'intention de la conversation**, sans changer ni d'écran ni de mode, et c'est le mécanisme qui tisse les cinq dans une seule conversation.

Un curseur d'aptitude n'est pas un paramètre : c'est un **préréglage** sur des **leviers**, qui sont les vrais paramètres et ce qui se stocke. Et ce n'est qu'un préréglage parmi d'autres — *easy* et *hard* en arcade en sont, chaque mode aura les siens. Ce que chaque aptitude fait mesurer et par quels leviers elle se presse est écrit dans `design/activity-model.md`, avec qui a le droit d'en décider — l'apprenant, le module, l'IA — et ce que chacun ne décide jamais.

**Aucun réglage ne touche une marque.** La même faute produit la même marque quels que soient les réglages ; ce qu'ils gouvernent, c'est ce qu'on en **fait** — s'il faut redire, si ça compte dans la note. La raison est qu'une marque dont l'absence dépendrait du réglage du jour ne transporte plus rien : rien ne la distingue alors d'une approbation.

Le mécanisme tient en trois étages : la **mesure**, que rien ne règle ; la **marque**, qui l'affiche telle quelle ; la **note**, qui l'agrège et où les réglages entrent seuls, la sévérité décidant où tombent les bornes A–E. **La note vit sur la mesure, pas sur l'aptitude** : une aptitude est un regroupement de mesures, chacune avec sa sensibilité et son poids, ce qui est la seule façon qu'un défi ne note que l'accent tonique sans que « élocution B » veuille dire deux choses (`design/activity-model.md`).

**Une note est un nombre de 0 à 1 et la lettre en est l'affichage**, les cinq lettres découpant l'échelle en cinquièmes égaux. Le reste se lit dessus : **A ou B, ça va** — sur un tour c'est ce qui décide s'il faut redire, sur une activité si c'est réussi. Cette barre vaut donc 0,60 par construction, elle ne se règle pas, et ce qui se règle est ce qu'il faut faire pour l'atteindre (`design/activity-model.md`).

**Correction et pertinence sortent d'un seul marquage par groupe de mots**, et chacune est **une feuille** dont les crans sont ses étiquettes — un mot en porte une par échelle, et n'en porter aucune est le cran `ok`, pas une absence. Les étiquettes d'une même échelle se partagent les mêmes mots : ce sont des tranches d'un gâteau, pas des mesures indépendantes, et c'est pourquoi elles ne font pas une feuille chacune. La correction demande *est-ce que c'est de l'anglais* — `mal formé`, `ne se dit pas`. C'est un jugement absolu contre une norme **fixée par l'app et identique pour toutes les activités** — pas une norme absolue, il n'y en a pas : *I ain't got none* est fautif en anglais standard et bien formé ailleurs. C'est cette invariance qui le rend vérifiable au banc sur des phrases isolées. La pertinence demande *as-tu visé juste* — `à côté`, `plat`, `juste` — et c'est elle qui porte la consigne, donc tout ce qu'une activité veut exiger : parler au passé, tenir un registre, éviter un mot. C'est la seule mesure du projet qui ait un bon côté : il n'y a pas de modèle unique de la bonne phrase, donc on peut dépasser le simplement correct, là où pour le son être sur le modèle est déjà le mieux qu'on puisse faire. La correction se souligne en vaguelette, la pertinence se prend entre crochets (`design/activity-model.md`).

**Monter un curseur retire une aide, ou durcit un jugement**, et les deux n'ont pas le même plafond : retirer une aide converge vers le réel, durcir un jugement le dépasse. Les effets se cumulent d'une aptitude à l'autre, et c'est voulu — une note ne se lit jamais sans la combinaison qui l'a produite.

## Les deux portes

**Ce qui va être réécrit et la prononciation ne se marquent pas côte à côte : l'analyse sonore ne tourne pas sur une phrase qu'on va reformuler.**

- Rien à reformuler → l'analyse sonore est visible immédiatement.
- Une reformulation attendue → la marque seule, **aucune analyse sonore**.
- Une fois la phrase reformulée et redite → l'analyse sonore apparaît, sur ce nouvel énoncé.

La raison est qu'on ne travaille pas la prononciation d'une phrase qu'on s'apprête à réécrire : elle va disparaître. La conséquence technique suit — **sur un tour à reformuler, l'analyse sonore ne tourne pas du tout.** Elle n'est pas cachée, elle n'est pas calculée.

**Il y a une porte par sorte de réparation.** La **porte des mots** se lit au retour de l'appel et dit que le passage est **à reformuler** ; la **porte du son** se lit à la fin de l'analyse et dit qu'il est **à redire**. Toutes deux lisent la **note d'une aptitude** à la barre A–B, le ciblage plus fin passant par les poids plutôt que par un réglage à elles.

**Ce qui range chaque aptitude d'un côté est câblé** : celles dont l'échec fait changer les mots — la correction, la pertinence, la compréhension — ferment la porte des mots ; celles dont l'échec laisse la phrase intacte — l'élocution, la fluidité — ferment celle du son. Une activité choisit **de quelles aptitudes elle fait refaire**, jamais de quel côté ça tombe (`design/activity-model.md`). Coût assumé au réglage le plus lâche : le modèle dira d'une voix native une tournure qu'on a choisi de ne pas reprendre.

**Ce qui coupe l'analyse du son n'est pas une troisième porte** : elle ne tourne pas si la porte des mots s'est fermée, ou si un mot est marqué `ne se dit pas`.

**Et ce second cas n'est pas une décision, c'est une absence de sol.** Là où la porte des mots choisit de ne pas travailler une phrase qui va disparaître, une phrase qui n'existe pas dans la langue **ne peut pas** être synthétisée : la faire dire au modèle donnerait à imiter une non-phrase. On ne peut pas non plus retirer l'empan pour synthétiser le reste, comme on retire une hésitation : un bafouillage est hors de la phrase, une non-phrase est dedans et demande à être remplacée.

Rien d'autre ne s'arrête dans aucun de ces cas : l'IA répond, le fil continue, la marque s'affiche, la reformulation est proposée. Seul le canal du son reste vide, en portant sa raison, et pour cette tentative-là seulement.

L'analyse tourne donc sur une phrase **jugée assez correcte au réglage courant**, et rien de plus : son texte de référence est aussi sûr ou incertain que celui de n'importe quel autre tour. La phrase corrigée vient de l'apprenant, qui la cherche lui-même — sauf à la position où le levier d'explication lui donne la phrase correcte (`design/activity-model.md`).

## Ce que le réseau entend est la source de vérité

**Question close, à ne pas rouvrir.** La suite de sons que le réseau rend d'un enregistrement **est** ce qui a été dit. Aucune brique n'a le droit de la déclarer fausse, de la corriger vers ce que l'orthographe attendait, ni de traiter un désaccord avec l'orthographe comme une erreur de la machine.

Quand une lettre ne sait pas écrire le son qui a été produit, **c'est la table qui est courte, jamais le réseau qui s'est trompé.** `I'm` dit vite rend `ɑ n` — la voyelle réduite, et le `m` qui s'assimile au `t` qui suit. Ce sont des réductions ordinaires de l'anglais parlé, et la table d'affinité qui ne les couvre pas a un trou. Le réflexe inverse — « le réseau a mal étiqueté » — est faux et il coûte à chaque fois la même demi-journée.

Le signe qui tranche quand le doute revient : **les deux enregistrements lisent pareil.** Une voix de synthèse et un apprenant qui rendent tous deux `ɑ n` au même endroit ne partagent pas une erreur, ils partagent une prononciation.

Cette règle n'est pas en contradiction avec le fait que **nommer le son produit ne sert jamais de verdict** (« Écouter plutôt que se faire expliquer »). Les deux disent des choses différentes et se complètent : l'étiquette ne dit pas si c'était *correct* — ça, c'est l'écart au modèle qui le dit —, et elle dit ce qui a été *produit*, ce que rien n'a le droit de récrire.

## Les trois échelles du son

Sous « prononciation » il y a trois choses de portée différente, à bien distinguer.

| échelle | ce que ça décrit | comment ça se juge |
|---|---|---|
| **le son** | un phonème dans un mot | recouvrement de deux répartitions |
| **le mot** | l'accent lexical — quelle syllabe est la forte | comparaison au modèle |
| **la phrase** | la mélodie — le contour de hauteur sur l'énoncé | comparaison à un modèle |

La syllabe **porte** l'accent et la hauteur, mais elle n'est la portée d'aucun des deux : l'accent est une propriété du mot, fixée par la langue indépendamment de la phrase ; la mélodie n'existe qu'à l'échelle de l'énoncé, une montée sur une syllabe isolée ne signifiant rien.

**Les trois se jugent par comparaison au modèle, jamais dans l'absolu.** Ce n'est pas une précaution, c'est la forme même de l'analyse : rien d'extérieur aux deux enregistrements n'est jamais consulté — ni dictionnaire de prononciation, ni lexique de dialecte, ni référentiel de justesse. La seule question posée est *en quoi cette prise s'écarte-t-elle de celle-là*, et ce qu'on compare est la lecture de l'apprenant à la lecture du modèle, par la même machine, dont le biais est donc le même des deux côtés et s'annule.

**Les trois s'ancrent aux mêmes caractères du texte affiché** — chaque son connaît les lettres qu'il couvre, chaque syllabe sa sous-chaîne, et la mélodie se pose sur ces mêmes groupes. Ce sont trois propriétés d'une seule chaîne, pas trois analyses à superposer. C'est ce qui rend possible de les marquer **d'un seul mouvement**, et la forme retenue le fait : **la couleur ne porte jamais l'échelle**, qui est portée par la forme — les lettres teintées pour le son, un filet sous la syllabe pour l'accent, une bande au-dessus de la ligne pour la mélodie. Trois échelles lues d'un coup d'œil sans qu'aucune ne se déguise en une autre. La couleur porte le côté et la distance au neutre : l'alarme partout, et le vert des deux crans hauts du projet — *juste* pour la pertinence, *entre les lignes* pour le suivi de la compréhension. La correction se souligne alors **en vaguelette**, pour ne pas se confondre avec le filet droit de l'accent, et la pertinence se prend **entre crochets** — une enceinte, mais qui ne court pas le long du texte, puisque *juste* se marque aussi et que presque tous les groupes portent donc une étiquette. Le dessin complet est dans `design/pixel-ui.md`.

Trois irrégularités à prévoir : une lettre peut porter deux sons, une lettre peut n'en porter aucun, et **un son peut ne porter aucune lettre**. La troisième est mesurée à 1,4 % des sons du banc, et se sépare en deux causes qui n'appellent pas le même geste — la lettre déjà prise par le son voisin (le `x` de `boxes`, qui écrit /k/ puis /s/), et la voyelle que l'orthographe n'écrit pas du tout (le schwa de `doesn't`). Le marquage doit servir les deux, sans quoi une faute trouvée reste invisible.

**Les trois échelles sont disponibles partout où le modèle l'est** : elles se lisent toutes de la même matrice, et aucune ne dépend d'un lexique qui existerait dans un dialecte et pas dans l'autre. L'accent choisi change la voix du modèle, jamais ce qui est mesurable.

## Marquer par écart au modèle, pas par note absolue

Une note de prononciation ne veut rien dire seule : chaque son a sa note « normale » propre à la machine qui écoute, et une note basse peut être une particularité de l'outil plutôt qu'une faute.

**Décision** : le modèle à imiter est synthétisé pour la phrase que l'apprenant vient de dire, passé dans la même analyse, et on compare son par son. La marque naît de l'**écart entre l'humain et le modèle**, jamais d'un seuil sur une note absolue.

Et ce qu'on compare n'est jamais deux notes, mais **deux formes** : à chaque instant, la répartition de la ressemblance sur tous les sons de l'anglais. `R 0,90 / W 0,10` et `R 0,90 / ER 0,10` ont le même pic et ne disent pas la même chose ; comparer les répartitions entières plutôt que leur maximum est ce qui rend la mesure honnête.

Mesuré (cf. `analysis.md`) : sur le jeu d'essai étiqueté, les prises témoins restent à 0,003 d'écart du modèle et les fautes franches montent au-dessus de 0,93. L'étalon annule le bruit propre de la machine, ce qui supprime tout besoin de calibrer son par son.

Deux propriétés qui comptent autant que la précision : la comparaison est **interne au tour**, elle n'accumule rien ; et le modèle est de toute façon nécessaire, puisque c'est lui qu'on fait entendre.

**Rien ne se fait à un seul des deux audios.** C'est la conséquence directe du fait que le biais de la machine s'annule parce qu'il est le même des deux côtés : un gain automatique, un débruitage, une normalisation appliqués à la prise et pas au modèle rendraient l'écart mesuré en partie fabriqué. Tout traitement est donc **symétrique ou inexistant**, et son point d'application est unique — entre la capture et l'analyse, sur les deux enregistrements — jamais caché derrière la reconnaissance ou la synthèse, qui ne voient qu'un côté par construction.

**Et la symétrie d'application ne suffit pas à rendre un traitement neutre.** Les deux signaux ne sont pas de même nature : un micro dans une pièce d'un côté, une synthèse propre de l'autre. Un débruiteur appliqué aux deux ne ferait presque rien sur le modèle et beaucoup sur la prise — symétrie du geste, asymétrie de l'effet. C'est un argument pour en faire le moins possible, pas pour en faire des deux côtés.

Ce qui suit n'est pas du traitement mais le contrat d'entrée : 16 kHz, mono, PCM 16 bits, ce que le modèle acoustique consomme des deux côtés. Ce qui est refusé nommément : gain automatique, suppression de bruit, annulation d'écho. La capture demande donc au système sa source **sans traitement** quand l'appareil déclare en avoir une, la source de reconnaissance sinon — et elle **dit laquelle elle a obtenue**, plutôt que de garder la différence pour elle.

**Le seuil est une exigence, pas un quota.** Une barre unique sur l'écart, la même pour chaque son, partout et tout le temps. Jamais « marquer les n pires », jamais un budget par mot ou par tour, jamais un classement : **toutes les fautes se marquent, aucune ne s'élit**. Un quota serait une élection, et il ferait dépendre une marque de ce qu'il y a d'autre dans la phrase — or la même faute doit produire la même marque à n'importe quel moment.

La barre est absolue, mais **ce qu'elle mesure est relatif** : l'écart de l'apprenant à son modèle. Exigence absolue sur une mesure relative.

**Et cette barre ne se règle pas.** La rampe est la même pour tout le monde et tout le temps, et la bande de bruit sous laquelle rien n'est teinté est une propriété mesurée de la machine, pas un réglage. Le réglage d'élocution existe toujours, mais il gouverne ce qu'on **fait** d'une marque — redire, compter dans la note — jamais son apparition. Les trois échelles du son ont chacune la sienne, leurs unités n'étant pas les mêmes. Celle de la mélodie est **géométrique** : le contour de l'apprenant est peint sous celui du modèle, à la même épaisseur, donc un écart plus petit que le trait ne laisse rien dépasser et la bande de bruit est cette épaisseur même (`design/pixel-ui.md`).

**Se taire n'est jamais une issue.** Quand un repère est trop bruyant, la réponse est de le réparer ou d'en trouver un autre — jamais de restreindre la marque aux cas faciles ni de retirer une échelle. Une échelle qui ne parle que là où elle est sûre laisse passer les fautes qu'elle est là pour attraper, et l'apprenant n'a aucun moyen de savoir que le silence n'est pas une approbation. Décision de projet, à ne pas rouvrir : les compromis qui achètent la fiabilité en renonçant à des marques sont écartés d'avance. Sa portée, notée pour que la question ne se repose pas : il a été écrit contre l'idée de restreindre une échelle **en service** à ses cas faciles ; il n'ordonne pas de retenir l'app derrière une échelle qui n'est pas encore écrite. L'accent lexical (brique 7) est resté de la recherche tant qu'il n'était pas fiable, ses canaux vides à l'écran ; sa fiabilité est maintenant mesurée (`analysis.md`, passe d'oreille) et la brique est dans l'app entière, sans restriction aux cas faciles — avec le seuil d'éligibilité qu'une oreille a validé, la marge de la sonde côté modèle à 0,90. La première version marquait le son et la mélodie, qui sont écrits et en service.

**Une marque est une proposition d'aller voir, pas un verdict.** L'écart au modèle est validé là où l'apprenant a **entendu** ce modèle avant de parler : deux fautes d'accent sur deux vues, aucune fausse alerte sur les calques (cf. `qualification.md`, bloc F). Sur un tour de conversation il n'a rien entendu, et l'écart s'affiche quand même.

**La règle vaut aux trois échelles, sans exception : le modèle est la source de vérité, et s'en écarter se marque.** On vise l'identique, au seuil près — un mot dit moins réduit que la synthèse, une syllabe accentuée ailleurs, une phrase qui monte là où le modèle descend. Aucune des trois n'a de régime à part.

Ça n'interdit rien à l'apprenant. Une phrase spontanée admet plusieurs mélodies justes, et qui veut mettre l'emphase sur un mot — « I said it **IS** important » — la met : sa courbe divergera de celle du modèle neutre, il la verra diverger, et il en fait ce qu'il veut. L'app montre l'écart ; elle ne prétend pas savoir ce que l'apprenant voulait dire. C'est le sens de « proposition d'aller voir » : la marque désigne, elle ne condamne pas.

**Question close, à ne pas rouvrir.** Se faire marquer souvent, c'est parler autrement que le modèle, ce que la mesure existe pour dire — ce n'est pas un symptôme à interpréter. La règle n'a pas de porte de sortie : une voix modèle qui ne conviendrait pas se **remplace**, et la voix suivante est la source de vérité exactement comme la précédente. Changer de norme ne revient jamais à s'en passer.

Ce qui rend l'écart tenable à l'usage est qu'on peut redire sur place : une marque qu'on juge injuste mène à écouter le modèle et à redire, le calque concorde, et l'affaire se ferme sur une réussite. Un détour court, jamais une leçon fausse.

**Un modèle n'est un étalon que s'il s'étalonne.** Une voix de synthèse que l'analyse lit mal accuserait l'apprenant d'une faute commise par la machine — le principe se paie ici précisément parce que le modèle est cru aveuglément : rien d'extérieur ne viendra dire qu'il était bâclé. Toute voix promue modèle passe donc un test, et une voix qui échoue est écartée quelle que soit sa beauté (cf. « L'accent », où ce test se choisit).

## Écouter plutôt que se faire expliquer

Le remède d'une faute sonore est **d'entendre le modèle et de redire**, aux trois échelles : le mot, la syllabe, la phrase. Pas une consigne d'articulation écrite.

Ça a une conséquence sur ce qu'on exige de l'analyse. Une consigne (« la langue passe entre les dents ») a besoin de savoir **quel** son a été produit ; jouer un modèle n'a besoin que de savoir **où** ça cloche. Identifier le son produit devient donc un enrichissement, pas une condition — ce qui est heureux, puisque c'est la partie la moins fiable de ce qu'une machine acoustique rend.

La position se lit des deux côtés gratuitement : la grille dit où le modèle prononce chaque son, l'alignement dit où l'apprenant le prononce. Le modèle à jouer et **l'enregistrement de l'apprenant au même endroit**, à faire entendre juste après, tombent du même calcul.

**Les audios de synthèse sont gardés en cache**, indexés par le texte, la voix et le dialecte. Une même synthèse sert alors trois fois : d'étalon pour la mesure, de modèle à écouter, et de modèle à réécouter autant de fois qu'on redit le mot ou la phrase.

## Redire sur place

Redire produit **un énoncé de plus dans la même conversation**, pointant vers celui qu'il reprend. Rien ne s'interrompt, rien ne part travailler ailleurs, et rien n'a à se refermer.

Ce qui s'y dit est noté contre un texte de référence **connu d'avance** — celui de l'énoncé repris — ce que la conversation libre ne peut pas offrir.

**Le nombre de marques ne multiplie pas le travail.** L'unité de réparation est la phrase, le mot au plus ; jamais le son. Trois sons marqués dans un mot font un seul mot à redire. Les marques ne sont donc pas des corvées à liquider une à une : elles disent **où porter l'attention** en redisant. C'est ce qui rend tenable de tout marquer sans rien élire.

  Le prix se paie ailleurs, sur l'**affichage** et non sur la charge : mesuré sur de la parole d'apprenant réelle, la majorité des mots portent quelque chose (`../TODO.md`). Une marque binaire ne transporte alors plus rien — tout est colorié, donc rien n'est signalé. Le marquage **gradue** donc au lieu de colorier ou non, et c'est en service : une rampe d'ambre à rouge à mesure que l'écart grandit, à teinte claire fixe pour qu'elle se lise comme une seule famille. Sous la bande de bruit la lettre garde l'encre neutre — une teinte y serait un mensonge de précision, le bruit propre de la machine étant lui-même de cette largeur.

**La redite remplace à l'affichage, jamais en base.** Le fil ne montre que la dernière tentative. Les tentatives restent toutes sous le passage : une tentative effacée est une mesure perdue (`design/activity-model.md`).

**La réponse de l'IA ne se refait que si elle n'a pas encore été dite.** Quand la conversation attend sur la phrase, l'IA n'a joué qu'un écho, et sa vraie réponse se fabrique sur la version corrigée. Quand elle a poursuivi, elle a parlé : redire est alors un exercice, qui ouvre l'analyse du son sans rien rejouer du fil. Refaire une réponse déjà prononcée serait la dédire.

## Les deux tuyaux

Deux circuits distincts partagent une seule ressource : le fichier audio du tour de parole.

**Tuyau A — la conversation.** Micro → tampon local (PCM 16 kHz mono, **conservé**) → fin de parole → envoi au fournisseur de conversation → lecture de la réponse audio. Le tampon local est la condition d'existence du tuyau B : aucun service distant ne rend l'audio envoyé. Sa forme est décidée par le design — **un tour est une liste de segments**, le silence gardé comme durée et jamais comme échantillons, chaque segment de parole entouré d'une marge de vrai audio — et **on n'envoie pas de silence au réseau**.

**Ce qui est décidé de la capture est un invariant, pas un mode.** Deux règles, et elles ne bougent pas :

- **Rien ne coupe la parole de quelqu'un qui parle encore.** Couper qui hésite, cherche un mot ou reprend sa phrase, c'est couper précisément qui apprend.
- **L'audio du tour est conservé localement**, sinon le tuyau B n'a rien à examiner.

**La capture est un levier de fluidité** (`design/activity-model.md`), et trois choses y sont décidées quelle que soit sa position : le micro **ne s'arme jamais avant la fin de la réponse de l'IA** ; un symbole est visible dès que ça enregistre, qui ne fait pas qu'informer — savoir que ça tourne change la façon dont on parle ; et **chaque tour porte sa position de capture**, sans quoi rien ne dit si ses silences sont significatifs, et rien ne s'agrège entre positions.

**Trois positions**, et l'échelle gradue exactement ce que la fluidité peut lire :

1. **Maintien du doigt, envoi manuel.** Ce que fait l'app : on appuie pour parler, on relâche pour réfléchir, on réappuie pour continuer, on envoie quand c'est dit. Aucune mesure de silence n'est possible — entre deux segments, l'écart mesure le pouce.
2. **Armement automatique, envoi manuel.** Le micro s'ouvre dès que l'IA a fini et reste ouvert jusqu'à l'envoi. Le silence devient mesurable de bout en bout — avant le premier mot, entre les mots, après le dernier.
3. **Armement automatique, envoi au clic ou sur un silence de plus de x**, silence du début compris. Le clic reste le geste normal : un tour que personne n'envoie est **interrompu**, et c'est ce que cette position demande, de la réactivité. Aucune mesure neuve ne s'y ajoute, et aucun silence n'y dépasse x, puisqu'à x le tour est déjà parti.

**La portée de l'invariant.** Il a été écrit contre l'app qui coupe dans le dos de quelqu'un en conversation ordinaire ; il vaut donc là où rien n'a été déclaré. Une pression annoncée est un autre problème : la position 3 coupe, sous une règle connue d'avance et choisie. Elle ne contredit pas l'invariant, elle est hors de sa portée.

**L'autre chose qui coupe** : la durée d'un tour, une seule variable dont le plafond technique est la valeur maximale admissible — 30 s aujourd'hui, parce que la mémoire d'une passe d'analyse croît comme le **carré** de la durée (`analysis.md`). Imposer de répondre en cinq secondes est la même variable réglée plus bas. Dans les deux cas le tour se ferme et envoie ce qui a été dit — jeter perdrait de la parole. Le plafond remonte quand le fenêtrage de l'analyse arrive ; le levier reste.

**Tuyau B — analyser.** Le tour est examiné, **sur l'appareil**, pour savoir s'il y a un problème et où. Se fait sur l'enregistrement existant, sans jamais rien redemander.

L'audio d'un tour ne quitte donc l'appareil **qu'une fois**, pour la conversation. L'analyse ne l'envoie nulle part.

## La panne

La conversation dépend de services distants, et chacun peut manquer — réseau coupé, quota épuisé, fournisseur en panne, clé expirée. L'analyse, elle, ne dépend que de l'appareil. La règle se décline par brique, pas globalement :

- **La chaîne de conversation est bloquante, mais réparable par construction** : le fichier conservé fait qu'un envoi raté se **réessaie sans redire la phrase**. L'échec coûte un bouton, jamais une parole perdue.
- **L'analyse ne dépend d'aucun réseau**, ce qui retire d'un coup le quota, la clé expirée et le fournisseur en panne. Ce qui reste est d'un autre genre et se règle **une fois pour la conversation, pas à chaque tour** : les poids ne sont pas encore téléchargés, ou l'appareil ne parvient pas à les charger. Dans ce cas les marques sonores sont éteintes de bout en bout, et l'option éteinte **porte sa raison** comme toute autre. Un tour reste analysable tant que la conversation a démarré avec son modèle chargé.
- **Redire a besoin de la synthèse, pas du réseau d'analyse.** Redire sans modèle à entendre n'est pas un exercice dégradé, c'est de l'auto-évaluation à l'oreille — ce que l'architecture refuse partout ailleurs. Si la synthèse manque et que le cache ne porte pas la phrase, l'app le dit franchement plutôt que de laisser redire à vide.
- **Ni file hors-ligne, ni réanalyse différée** : analyser un tour trois tours plus tard poserait des marques sur du passé, devant quelqu'un qui parle d'autre chose. Ce qui n'a pas été analysé sur le moment ne le sera pas.

## Le texte de référence

L'analyse compare l'audio de l'apprenant au modèle synthétisé pour un **texte donné**. En conversation libre, ce texte n'est pas connu d'avance, et c'est la seule contrainte de la chaîne qu'on ne peut pas contourner : les mesures fines n'existent que là où le texte est écrit d'avance.

La porte des mots en retire déjà la moitié du problème — sur un tour à réécrire, il n'y a pas d'analyse, et la phrase corrigée est redite contre un texte certain. Reste le tour qu'on garde tel quel.

**Décision** : le texte vient de la transcription, que le LLM peut corriger à partir du contexte de la conversation. La répartition des rôles est une règle : **le STT transcrit la bouche, le LLM décide l'intention.** La normalisation appartient au LLM, qui a le contexte et une instruction — jamais au STT, qui la ferait en silence et sans contexte ; un STT qui répare la grammaire d'office efface le signal d'apprentissage avant tout jugement, et sa fidélité verbatim est un critère de choix (cf. `../TODO.md`, chantier 2).

**Un tour interrompu ne se complète jamais.** L'app sait comment chaque tour s'est fini et le dit au modèle : devant *« I went to the »*, reconstruire *« I went to the market »* ferait prononcer à la voix modèle un mot que personne n'a dit, et toutes les marques du tour tomberaient à côté.

La ponctuation d'`intended` fait partie de cette tâche : le LLM ponctue selon l'intention à laquelle il répond — s'il répond à une question, il a lu une question — et le contour du modèle TTS en dépend.

**Et le texte ne porte jamais de chiffres.** Nombres, dates et montants s'écrivent en toutes lettres — `twenty five`, jamais `25`. Ce n'est pas une préférence d'affichage : l'analyse pose chaque son sur les lettres qui l'écrivent, et un chiffre n'en a aucune. Mesuré : sur `I am 25 years old`, la voix dit *twenty five* et le mot `25`, n'ayant pas une lettre, sort entièrement du découpage — ses huit sons sont récupérés par `years`, dont toutes les marques tombent alors à côté. Ce seul mot faisait **24 des 53 sons sans lettre** des 95 rendus du banc ; en toutes lettres il en reste 18, soit 1,2 % au lieu de 3,4 %. La règle vaut partout où un texte de référence se fabrique : la consigne au LLM pour `intended`, et les phrases du banc. Deux faits mesurés encadrent ce choix.

Le piège se déclenche moins souvent qu'on le craignait : sur *« I sink »* comme sur *« I am walkin »*, la reconnaissance a rendu `think` et `walking` d'elle-même — le modèle de langue normalise vers le mot plausible, et il n'y avait rien à corriger.

Mais **une reconstruction fausse coûte**. *« Turn light at the corner »* reconstruit en `left` au lieu de `right`, le contexte admettant les deux : noté contre `left`, le mot rend quatre sons aberrants d'un coup.

Ce coût avait été écrit plus grand qu'il n'est. Le doc annonçait que le mot fautif « entraîne le suivant avec lui », donc une **rafale** ; mesuré depuis, une faute ne se propage pas — le reste de la phrase reste aussi propre qu'un témoin (`analysis.md`, brique 11). Le cas propre du texte faux n'est pas mesuré pour autant, aucune prise du jeu n'en portant.

**Le garde-fou qui en découlait est à revoir.** Il devait écarter un tour où beaucoup de sons s'effondrent ensemble, au motif que ça signale plus probablement un mauvais texte qu'un mauvais locuteur. Cette inférence ne tient pas : un apprenant très accentué s'écarte lui aussi partout, et rien dans le test ne l'en distingue — mesuré sur une prise du jeu, où sept différences relevées, une seule est la faute voulue et six sont des traits d'accent. Le garde-fou éteint alors l'analyse sur ceux qui en ont le plus besoin, **et sans le dire**, ce que la règle du silence interdit par ailleurs.

Ce qui le remplacera n'est pas décidé. Une piste : **marquer le mot** — un texte faux donne un mot très écarté, l'écran affiche le texte transcrit, donc l'erreur est bornée à un mot et lisible par qui la reçoit. Resterait à contrôler l'**alignement dégénéré** (exigence 1 ci-dessous), qui se lit à des durées absurdes sans rien inférer sur le texte.

## L'affichage

La conversation s'affiche en texte : on ne colorie pas une portion de son.

Les tours de l'IA sont **floutables** — non pas un réglage d'écoute à part, mais un **levier de compréhension à trois marches** : texte affiché, seulement qui parle, rien (`design/activity-model.md`). Les lire au lieu de les écouter fait sortir la compréhension orale par la fenêtre.

Le flou ne gêne pas la reprise : la marque est sur le tour de **l'utilisateur**, et on redit dessus sans jamais avoir à déflouter la réponse de l'IA.

## Ce qui se garde

**On stocke ce qui dépend de quelque chose qui ne se retrouvera pas** — l'audio d'un moment, le jugement d'un juge, une version de modèle. **On recalcule tout ce qui ne dépend que des lignes** : le nombre d'énoncés, leur ordre, les durées, tout ce qu'un compte suffit à produire. Ni les lectures d'un tour, ni le modèle à imiter, ni le cran de la prise suivante n'ont donc de colonne.

**Tout ce qui est stocké porte la version de ce qui l'a produit.** Une analyse refaite avec un modèle différent ne rend pas les mêmes chiffres ; sans cette marque, deux époques de mesure s'agrègent en silence et la comparaison est fausse sans que rien ne le dise.

La liste de ce qui doit être agrégeable reste à préciser.

Matière par matière :

- **La voix de l'apprenant** n'est **pas purgée par défaut**, et la purge reste à écrire (`design/activity-model.md`). Un enregistrement qu'aucun énoncé ne nomme, lui, est effacé : c'est du rebut et non de l'audio gardé, il vient des deux tours qui finissent sans énoncé — celui où la reconnaissance n'a rien entendu, celui dont la chaîne a cassé sans réessai. Le balayage se fait **au démarrage et nulle part ailleurs**, seul moment où rien n'est en vol et où un fichier que la base ne nomme pas ne sera jamais nommé.
- **Le cache des synthèses** survit, sous **plafond réglable avec éviction du moins récemment demandé** : ce n'est qu'un cache, régénérable au prix d'un appel, et ce qu'il vaut d'en garder dépend du téléphone qui le garde. Une synthèse rendue est touchée à chaque fois qu'on la redemande, sans quoi la date dirait quand elle a été fabriquée et la phrase voulue tous les jours sortirait avant une phrase que personne ne réclame plus.
- **La trace écrite** — les énoncés, qui parle, le texte, les marques avec leurs ancres — est archivée **telle que l'écran l'affiche**, sans modélisation ajoutée : des faits bruts, dont les usages futurs se dériveront s'ils adviennent. Elle **se relit** : une conversation se reprend là où elle en était. Le prescripteur progression sera l'autre lecteur, quand il existera.

## L'accent

Réglage **global unique**, exposé à l'utilisateur. Il gouverne deux choses, qui sont en réalité la même : la voix qui parle, et la voix du modèle à imiter.

**Il n'y a pas de troisième chose à aligner.** L'analyse ne consulte aucun référentiel de dialecte : elle compare l'apprenant au modèle, et le modèle est la seule norme. Si le modèle est britannique, tout ce qui en dérive l'est — mécaniquement, sans lexique à choisir ni accord à vérifier. L'accent cesse donc d'être un paramètre de mesure pour n'être plus qu'un choix de voix, et le désaccord d'accent, qui inversait la mesure quand un référentiel extérieur existait, n'a plus de lieu où se produire.

**La voix se choisit.** Une cascade — le fournisseur de synthèse, puis son modèle quand il en expose plusieurs, puis la voix — et un **étalonnage à la demande** qui avertit si elle échoue. Le modèle est dans la cascade et pas seulement le fournisseur : `chatterbox` et `chatterbox-turbo` offrent les mêmes noms de voix et ne rendent pas la même voix. Rien n'est imposé et rien n'est deviné : une voix qui ne s'étalonne pas reste utilisable pour parler, mais elle est signalée comme impropre à servir de modèle. **Ce que l'étalonnage vérifie est à redéfinir**, et **aucune voix n'est aujourd'hui qualifiée** : le test qui existait jugeait une voix aux notes d'un service dont l'app ne dépend plus. Ce qui le remplacera se lit de la matrice — une grille nette, pas de son écrasé, pas de zone où la répartition s'effondre (cf. `../TODO.md`). L'oreille ne peut pas rendre ce verdict : elle juge la voix comme modèle à imiter, pas comme étalon de mesure — une voix naturellement relâchée sonne d'autant mieux qu'elle étalonne mal, et le même modèle acoustique voit une faute sur une voix et la manque sur une autre (`../TODO.md`, tableau de `faults.py`). Les deux aptitudes se vérifient chacune par son juge.

Par défaut, **le modèle à imiter est la voix de la conversation** — c'est celle qu'on entend déjà, et rien ne justifie d'en présenter une autre. Les dissocier reste possible pour qui le veut : les briques *conversation* et *synthèse* sont indépendantes, unifiées par le seul paramètre d'accent.

**Une activité peut porter sa propre voix de conversation, et ça ne touche pas la mesure.** Un personnage dit *ses* tours ; le modèle à imiter dit *la phrase de l'apprenant*. Deux énoncés différents, donc deux voix qui se séparent sans conflit : la voix de référence reste le réglage global, et **la voix d'un personnage ne sert jamais d'étalon**, donc elle n'a aucun test à passer. Une voix difficile à suivre devient alors un levier de compréhension. Ce qu'un fournisseur expose ne peut pas devenir une condition, donc un personnage n'est jamais attaché en dur à un nom de voix ; comment il en obtient une reste ouvert (`design/activity-model.md`).

## Les clés d'API

L'app est un **client vide** : l'utilisateur apporte ses propres clés (BYOK) pour la conversation et la synthèse, qui ne partent qu'aux fournisseurs concernés. Aucun serveur, aucun compte, aucune consommation à la charge du projet. **L'analyse n'a pas de clé** : elle tourne sur l'appareil.

Ce choix n'est pas qu'économique : il est la seule issue compatible avec la publication sur F-Droid, qui interdit toute clé embarquée dans une release — une clé dans le binaire est une clé publiée avec les sources.

Ce qui en découle et se décide au premier commit :

- Stockage chiffré adossé au Keystore Android. Jamais en clair, jamais dans un log, jamais dans une sauvegarde système (`android:allowBackup="false"`).
- Rien dans le build : pas de champ `BuildConfig` alimenté par un secret, pas de `local.properties` versionné, pas de secret en `gradle.properties`.
- Anti-feature **`NonFreeNet`** à déclarer à la soumission.
- Écran de configuration guidé, avec un bouton **« tester la clé »** qui valide immédiatement. C'est le vrai coût du BYOK : créer une ressource chez un fournisseur est pénible, et sans validation immédiate toute panne ultérieure sera imputée à l'app.
- **La sonde de capacités se fait là aussi**, une fois : un appel par fonction optionnelle, et l'app allume ou éteint les briques selon les réponses. Elle n'a pas à deviner d'après le plan souscrit, dont on a mesuré qu'il ne correspond pas à ce que le fournisseur annonce.
- **Le catalogue se récupère à chaque ouverture de l'écran, sans cache — et c'est lui, la sonde.** Demander à un fournisseur les voix qu'il propose valide la clé du même geste : une liste qui revient est une clé qui marche, et la liste revenue est ce que *cette* clé débloque vraiment, non ce que le fournisseur annonce. Un catalogue mis en cache, lui, périt en silence. Le fetch ne bloque pas : seul le sélecteur qui attend dit qu'il attend.
- L'utilisateur paie sa consommation : l'app doit pouvoir dire ce qu'elle consomme. Restent au compteur la reconnaissance, le modèle de langue et la synthèse, qui se paient aux centimes.

Limite connue et acceptée : le BYOK reste un mur d'adoption — créer une ressource chez trois fournisseurs demande de la patience. Le mur est tenable parce que **l'app est d'abord pour son auteur** : la publication F-Droid est une générosité et une discipline, pas une stratégie d'adoption.

**L'analyse, elle, se paie autrement** : un téléchargement unique de 359 Mo de poids acoustiques, en **opt-in explicite** au premier usage — jamais au premier lancement, jamais en silence. C'est un mur d'un autre genre, franchi une fois et non tous les mois, et les poids sont sous licence libre (Apache-2.0), sans quoi ils seraient un `NonFreeAssets`.

## L'analyse est à l'app, les autres briques sont à des fournisseurs

**L'analyse tourne sur l'appareil, et c'est la colonne vertébrale de l'app** (`analysis.md`). **Elle ne consulte aucune norme qui juge** : ni dictionnaire de prononciation, ni lexique de dialecte, ni référentiel de justesse. Ce qui décide si une prise est fautive reste l'écart au modèle, et rien d'autre.

Deux fichiers extérieurs dans toute la chaîne : les poids d'un modèle acoustique libre, et une **table d'affinité graphème↔phonème** de quelques kilo-octets — laquelle ne juge rien. Elle sert à la seule brique du marquage : savoir si `s` participe à /ʃ/, donc sur quelles lettres poser une couleur déjà décidée ailleurs. La distinction porte tout le principe : une norme extérieure qui dirait ce qui est *correct* est refusée ; une table qui dit où *peindre* ne l'est pas, et sans elle la marque tombe à côté près de deux fois sur cinq (`analysis.md`).

Ce montage ne ressemble pas à l'approche habituelle, et pour une raison de situation plus que d'astuce. Qui n'a que l'audio d'un apprenant et un texte a besoin d'un dictionnaire pour se donner une norme. L'app, elle, possède **deux enregistrements du même énoncé** — elle synthétise le modèle de toute façon, puisque c'est lui qu'elle fait entendre. Le modèle est donc la source de vérité, cru aveuglément, et rien d'extérieur aux deux enregistrements ne juge quoi que ce soit.

Mesuré : les fautes du jeu d'essai se trouvent sans qu'aucun témoin ne se déclenche, et la chaîne **tourne sur un téléphone de 2019** (chiffres dans `analysis.md`).

Ce que l'analyse doit rendre, et qui se vérifie brique par brique (`qualification.md`) :

1. **Localiser** chaque son dans l'audio — position et durée — et permettre de repérer quand elle n'y parvient pas. Les alignements dégénérés existent et se trahissent par des durées absurdes ; une aberration qu'on ne voit pas passe pour une faute de l'apprenant.
2. **Ancrer ses mesures au texte** : quelles lettres porte ce son, quelles lettres forme cette syllabe. Sans cet ancrage il n'y a pas de marque, seulement des chiffres.
3. Rendre son jugement aux **trois échelles** — le son, le mot, la phrase.
4. Être **déterministe** : sans ça, comparer un humain à un modèle mesure le bruit de la machine autant que l'écart réel. Vérifié sur l'appareil, deux lectures du même fichier rendant les mêmes octets.

**Dire quel son a été produit** est un enrichissement, non une exigence : il corrobore une détection et ouvrirait la porte à une consigne d'articulation. C'est la partie la moins fiable de ce qu'une machine acoustique rend, et l'app n'en dépend pas.

**Conversation et synthèse restent distantes, et substituables** — `NonFreeNet` reste déclarée à ce titre.

**Le montage est la chaîne STT → LLM → TTS**, tranchée, plutôt qu'une API voix-à-voix. Trois raisons, dont la dernière est mesurée : chaque maillon reste substituable ; la reconstruction du texte de référence voyage dans l'appel LLM qu'on fait de toute façon, là où le voix-à-voix exigerait un appel supplémentaire par tour rien que pour l'obtenir ; et la latence se mesure maillon par maillon. Le banc donnait **2,6 s jusqu'au premier son** (`conversation-chain.md`) ; sur l'appareil, huit tours réels donnent une **médiane de 11,0 s**, de 8,4 à 18,4 (`../TODO.md`). Le montage tranché garde sa raison d'être — chaque maillon reste substituable, et c'est ce qui permet de voir lequel coûte.

Deux points de montage réglés par la même mesure. La synthèse **n'est pas pipelinée** sur la première phrase du modèle : le gain est de 0,16 s, parce que le modèle achève son objet un septième de seconde après sa première phrase. Et la reconnaissance se fait **par fichier, pas en flux**. Le banc lui donnait un coût proportionnel — un sixième de la durée de l'audio, pesant sur le tour long. **L'appareil dit autre chose** : 1,8 s d'audio a coûté 10,9 s, 4,8 s en a coûté 7,2 et 2,2 s en a coûté 0,9. Ce qu'on paie n'est pas la longueur, c'est l'aller-retour, et sa variance est le vrai défaut. Ça ne condamne pas le fichier contre le flux ; ça retire l'argument de la durée du débat, et ça renforce la destination locale de ce maillon, qui la supprimerait entièrement.

**La synthèse ne doit rendre qu'un audio.** L'exigence 2 ci-dessus — ancrer les marques au texte — se calcule entièrement sur l'appareil, par l'ordre et l'orthographe : la suite de sons est partitionnée entre les mots, et dans chaque mot les lettres se posent sur les sons qu'elles participent à écrire. Rien n'est demandé au fournisseur qu'un wav. **Tout moteur de synthèse est donc candidat**, les libres compris, et le montage n'a plus d'attache étroite nulle part.

**Les fournisseurs de conversation et de synthèse restent à choisir** (cf. `../TODO.md`, chantier 2). Ceux du banc — Azure Speech, DeepSeek — ont servi à mesurer, pas à décider.

**Ce qui est tranché, c'est que le choix appartient à l'utilisateur, brique par brique.** Chaque maillon a son sélecteur de fournisseur, et les mélanger est l'état normal plutôt qu'un cas limite. Une même clé peut servir deux maillons — Replicate porte la reconnaissance et la synthèse — et le mur du BYOK baisse d'autant : un compte au lieu de deux. Un fournisseur n'est offert à un maillon que si sa clé est saisie ; c'est un filtre local, sans aucun appel, ce qui laisse l'écran se dessiner avant que le réseau ait répondu.

Deux conséquences qui se paient dans le code. Le **modèle est un paramètre par maillon**, jamais un attribut de la clé, puisque la même clé sert deux tâches. Et **la disponibilité se demande par maillon** : la clé Azure est indispensable à qui reconnaît chez Azure et sans objet à qui reconnaît chez Replicate — une question posée pour toute la chaîne d'un coup n'a plus de réponse juste.

**Ce qu'un fournisseur rend en plus ne peut jamais devenir une condition.** La reconnaissance en est le cas concret : whisperx rend les bornes de chaque mot, whisper et Azure ne les rendent pas, et l'app ne les exige nulle part — elle répartit les sons entre les mots par l'ordre et l'orthographe, à 95 % (`analysis.md`). Les bornes enrichissent. Elles ne conditionnent pas, et ne le peuvent pas : une brique qui les exigerait s'éteindrait le jour où la reconnaissance devient locale, ce qui est sa destination.

**La reconnaissance a une destination préférée : l'appareil.** Elle est distante comme les deux autres, mais c'est le seul des trois maillons qui puisse cesser de l'être — `sherpa-onnx` fait tourner un modèle de reconnaissance par l'ONNX Runtime que l'app embarque déjà pour l'analyse, donc sans runtime nouveau. Y arriver retirerait un fournisseur du mur BYOK, un motif au `NonFreeNet`, et ferait que l'audio d'un tour **ne quitte plus jamais l'appareil** — seul le texte partirait. Sa justesse sur de la parole d'apprenant accentuée n'est pas mesurée, et elle décide seule.

**Ce qui est acté sans attendre cette mesure, c'est la forme de la couture** : l'interface de reconnaissance prend un fichier et rend des mots avec leurs bornes — ni clé dans sa signature, ni panne réseau dans son contrat, ni latence supposée. Une couture qui présume le distant se paie en réécriture le jour où le local gagne, et elle ne coûte rien à poser aujourd'hui.

### Capacités déclarées, pas plus petit dénominateur commun

Il y a deux manières d'être agnostique et elles sont opposées. La première n'expose que ce que *tous* les fournisseurs savent faire : on n'exploite alors jamais ce que le meilleur a de mieux, et le progrès de l'un ne profite à personne. La seconde, retenue : **chaque fournisseur déclare ce qu'il sait faire, et l'app allume ou éteint les briques en conséquence.**

Contrepartie assumée : le jeu de fonctionnalités **dépend du fournisseur choisi**. Un utilisateur verra des options éteintes qu'un autre a. Une option indisponible doit donc **porter sa raison** dans l'interface — sinon elle passe pour un bug, et c'est l'app qu'on accusera, pas le service.

**Et ce n'est pas seulement le jeu d'options qui en dépend : ce que valent les chiffres en dépend aussi.** Un jugement rendu par le modèle de langue — un marquage, un cran — n'est pas le même d'un fournisseur à l'autre, donc deux apprenants sur deux clés différentes n'ont pas des notes strictement comparables. **C'est admis, et ce n'est pas une objection recevable contre une brique.** La règle qui protège la lecture est ailleurs et suffit : une note ne se lit jamais sans la combinaison qui l'a produite, et tout ce qui est stocké porte la version de ce qui l'a produit.

## Indexer des chemins nommés, jamais `git add -A`

Ces deux formes indexent ce qui **se trouve** dans l'arbre, y compris ce que personne n'y a mis — et un intrus qui n'est pas un fichier ordinaire fait échouer l'indexation entière, avec un message qui nomme l'intrus sans dire d'où il vient (« ne peut ajouter que des fichiers normaux, des liens symboliques ou des répertoires »).

Cas rencontré : le bac à sable des commandes de l'agent neutralise les fichiers de configuration qu'il refuse de laisser lire — `.bash_profile`, `.gitconfig`, `.mcp.json`, `.vscode` et d'autres — en montant `/dev/null` par-dessus, **dans le dossier courant**. Ils apparaissent donc à la racine du projet comme périphériques caractère, et `git status` les voit non suivis. Ils n'existent que dans la vue du bac à sable : le dépôt sur le disque est propre, et il ne faut donc **pas** les gitignorer — ce serait mettre dans le projet une ligne qui parle d'un outil, pas de lui.

## Une recherche aveuglée par le bac à sable rend zéro résultat, pas la preuve d'une absence

Le bac à sable des commandes de l'agent **interdit la lecture de `/home/simon`**. Un `find / -name adb` y rend donc une liste vide alors que `adb` est dans `~/adb/platform-tools/`, en PATH — et `which adb` échoue pour la même raison. Le vide ne dit pas « ça n'existe pas », il dit « je n'ai pas pu regarder là », et les deux se ressemblent exactement.

C'est la même forme que la règle de `dev_base` sur les tâches de fond, où un `pgrep` rend une liste vide que la tâche tourne ou non. La conséquence est ici : **ce qui vit dans le home ne se cherche jamais depuis le bac à sable** — `adb` et le SDK Android, et tout ce que l'utilisateur y installe. On relance avec le bac à sable désactivé avant de conclure quoi que ce soit.

Les outils du projet lui-même, eux, vivent dans le dépôt et se cherchent normalement — avec une exception à connaître : le venv du banc est dans `tmp/`, gitignoré, donc absent de tout parcours de l'arbre versionné (`../bench/README.md`).

## Le build a besoin du réseau, le bac à sable le lui refuse

`./gradlew` — donc `./run build`, `install`, `release` — va chercher la distribution Gradle, puis les dépendances, sur le réseau. Sous le bac à sable des commandes de l'agent, il échoue en `UnknownHostException: services.gradle.org`, une panne de nom qui ne ressemble en rien à une restriction. **Ces commandes se lancent avec le bac à sable désactivé**, sans passer par la boucle « essayer, lire l'erreur, réessayer ».

## Construire en debug, sauf quand la doublure de release change

`./gradlew :app:assembleDebug` suffit pour tout le travail courant, et `assembleRelease` est long — R8, le rétrécissement des ressources, un APK complet — pour ne rien apprendre la plupart du temps.

La seule raison de toucher au release est que `app/src/release/` existe : la doublure `Analyses` qui répond qu'aucun moteur d'analyse n'est embarqué. **Ce source set n'est jamais compilé par le build debug**, donc une erreur y dort jusqu'à la RC. Il fait vingt lignes et ne bouge quasiment pas.

D'où la règle : debug par défaut ; compiler le release **seulement** quand `app/src/release/` ou la couture `Analysis` change, et avant une RC. Et alors `:app:compileReleaseKotlin` plutôt qu'`assembleRelease` — c'est la compilation qui manque, pas l'APK.

## Hors périmètre

Écarté délibérément de la première version, non par oubli :

- La consigne d'articulation écrite, remplacée par l'écoute d'un modèle.

Et un extrait de son ne se coupe jamais à ses bornes exactes : ce qui identifie une occlusive vit dans la transition vers le son suivant, donc on joue une marge de part et d'autre — large autour d'un son, étroite autour d'un mot, où ce qui déborde est le mot voisin. Ralentir garde la hauteur : rééchantillonner descendrait les formants et changerait la voyelle entendue.

**Tranché le 2026-08-31 : le son isolé est un niveau d'écoute.** L'objection tenait à la production — un son réussi seul se rate encore dans le mot, parce que ce qui coince est la transition — et elle ne porte pas sur l'écoute, qui ne demande rien à la bouche. Il s'entend à deux titres, et la distinction compte : **le son du modèle à sa place dans la phrase**, qui dit comment il fallait dire *ici* et sort du calcul déjà fait ; et **un enregistrement du symbole seul**, qui dit ce que `ʃ` veut dire en général. Le second est une légende et ne juge rien — c'est la même classe de fichier extérieur que la table d'affinité, qui dit où peindre et non ce qui est correct. Il n'entre nulle part dans la mesure.
