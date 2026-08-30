# speakup — référence

App Android de pratique de l'anglais oral : conversation libre avec une IA, jamais interrompue, doublée d'un travail de la grammaire et de la prononciation à la demande. Ce document est le point d'entrée ; il porte les décisions **propres au projet**, celles qu'aucune facette de sagesse ne couvre. Le craft transverse vit dans les modules abonnés (cf. `../manifest.md`).

Les autres docs, à ouvrir au besoin. Ceux d'à côté portent **ce qui est vrai et se consulte** :

- `analysis.md` — l'analyse, brique par brique : ce qu'elle lit, ce qu'elle rend, ce qui est mesuré et ce qui reste à écrire.
- `qualification.md` — comment on la vérifie : la procédure, et le jeu d'essai étiqueté qu'elle déroule. Rejouable par un tiers.
- `conversation-chain.md` — la latence mesurée de la chaîne reconnaissance (STT) → modèle de langue (LLM) → synthèse (TTS), chez Azure comme chez Replicate.
- `chatterbox-parameters.md` — ce que les modèles chatterbox acceptent et ce qu'ils font sans le demander : les paramètres de chaque variante, les balises entre crochets, la normalisation de ponctuation.

`design/` porte **ce qui est à faire et sera élagué une fois le code en place** :

- `design/ui-flow.md` — le flux et l'écran, de bout en bout : posture, micro, marquage, chorégraphie du tour.
- `design/grammar-test-set.md` — les deux bancs du chantier 2, juge grammatical et fidélité du STT.
- `design/added-sounds.md` — voir un son que l'apprenant ajoute : le fil entier d'une journée, ce qui est mesuré, ce qui est passé dans l'app, ce qui en a été retiré, les deux découpes contre les sons du modèle abandonnées, et la découpe par les lettres retenue et non encore écrite.

## Le geste

L'utilisateur parle anglais à voix haute. L'IA lui répond en voix, dans l'accent choisi. Rien ne l'interrompt.

Quand il se trompe, l'IA reprend **dans sa réponse** au lieu de s'arrêter — à qui vient de dire *I have 25 years*, elle répond « Ah, you're 25! And where... ». La reprise passe dans le fil ; le tour fautif porte en plus une **marque discrète** (la portion concernée, colorée). Sans cette trace, la discrétion se retourne : on corrige, et personne ne l'apprend.

Un appui sur la marque ouvre une **parenthèse** de travail sur l'erreur. Un appui la referme, et la conversation reprend où elle en était.

Le contrôle du moment appartient à l'utilisateur, jamais à l'app. Seule exception, explicitement optionnelle : les seuils, qui peuvent ouvrir la parenthèse d'eux-mêmes.

## Chaque tour est indépendant

Aucune analyse ne s'accumule d'un tour au suivant. Un tour est examiné avec ce qu'il contient, jugé, marqué, et rien de ce jugement ne survit pour peser sur le tour d'après.

Ce n'est pas une simplification d'implémentation, c'est ce qui rend le marquage lisible : une marque qui apparaît parce que *le même son a déjà été raté trois fois* est incompréhensible pour qui la reçoit. La même faute doit produire la même marque, à n'importe quel moment de la session.

Ça vaut aussi comme borne : ni suivi longitudinal, ni tendance affichée, ni pilotage de la conversation par les faiblesses accumulées.

La règle porte sur **la conversation et son marquage**. Une parenthèse est un épisode fermé, avec son propre déroulé — un drill dont la difficulté monte progresse à l'intérieur de la parenthèse et meurt avec elle, sans rien laisser derrière.

## Les trois curseurs

Fluidité, grammaire, prononciation ne sont pas trois modes entre lesquels on bascule : ce sont trois axes réglables, et **les régler est la façon de déclarer l'intention d'une session** sans changer ni d'écran ni de mode. C'est le mécanisme qui tisse les trois dans une seule conversation.

- **Pression conversationnelle** — de « l'IA te laisse mener » à « elle relance dès que tu t'arrêtes et refuse les réponses en trois mots ». La fluidité a un contenu propre : sans ce curseur, elle ne serait qu'un mot pour « on ne t'embête pas ».
- **Sévérité grammaticale** — trois crans : ne rien marquer / marquer les fautes / marquer aussi les tournures correctes mais maladroites. Pas un pourcentage : une phrase est fautive ou ne l'est pas.
- **Seuil de prononciation** — sensibilité du marquage. Facultatif : le poser bas, c'est déclarer qu'on travaille cet aspect-là aujourd'hui.

Un curseur peut déclencher la parenthèse automatiquement au lieu de seulement marquer. C'est un choix de l'utilisateur, et l'unique endroit où l'app prend l'initiative du moment.

## La porte grammaticale

**Grammaire et prononciation ne se marquent pas côte à côte : la grammaire est une porte devant l'analyse sonore.**

- Grammaire correcte → l'analyse sonore est visible immédiatement.
- Grammaire fautive → la marque grammaticale seule, **aucune analyse sonore**.
- Une fois la grammaire corrigée et la phrase redite → l'analyse sonore apparaît, sur ce nouvel énoncé.

La raison est qu'on ne travaille pas la prononciation d'une phrase qu'on s'apprête à réécrire : elle va disparaître. La conséquence technique suit — **sur un tour grammaticalement fautif, l'analyse sonore ne tourne pas du tout.** Elle n'est pas cachée, elle n'est pas calculée.

**La porte suit le marquage, pas la grammaire absolue** : un tour est « fautif » pour la porte si et seulement s'il est marqué au cran de sévérité courant. Au cran 3, une tournure maladroite ferme la porte comme une faute — elle va être réécrite. Au cran 2, elle passe, et la prononciation se travaille sur elle : régler la sévérité, c'est déclarer que l'idiomatique n'est pas le sujet du jour, et rien ne va réécrire cette phrase. Coût assumé : le modèle dira la tournure maladroite d'une voix native.

Effet de bord précieux : la branche corrigée s'analyse contre un texte **certain**, celui que l'IA vient de proposer. Toute l'incertitude du texte de référence disparaît de cette branche.

## Les trois échelles du son

Sous « prononciation » il y a trois choses de portée différente, à bien distinguer.

| échelle | ce que ça décrit | comment ça se juge |
|---|---|---|
| **le son** | un phonème dans un mot | recouvrement de deux répartitions |
| **le mot** | l'accent lexical — quelle syllabe est la forte | comparaison au modèle |
| **la phrase** | la mélodie — le contour de hauteur sur l'énoncé | comparaison à un modèle |

La syllabe **porte** l'accent et la hauteur, mais elle n'est la portée d'aucun des deux : l'accent est une propriété du mot, fixée par la langue indépendamment de la phrase ; la mélodie n'existe qu'à l'échelle de l'énoncé, une montée sur une syllabe isolée ne signifiant rien.

**Les trois se jugent par comparaison au modèle, jamais dans l'absolu.** Ce n'est pas une précaution, c'est la forme même de l'analyse : rien d'extérieur aux deux enregistrements n'est jamais consulté — ni dictionnaire de prononciation, ni lexique de dialecte, ni référentiel de justesse. La seule question posée est *en quoi cette prise s'écarte-t-elle de celle-là*, et ce qu'on compare est la lecture de l'apprenant à la lecture du modèle, par la même machine, dont le biais est donc le même des deux côtés et s'annule.

**Les trois s'ancrent aux mêmes caractères du texte affiché** — chaque son connaît les lettres qu'il couvre, chaque syllabe sa sous-chaîne, et la mélodie se pose sur ces mêmes groupes. Ce sont trois propriétés d'une seule chaîne, pas trois analyses à superposer. C'est ce qui rend possible de les marquer **d'un seul mouvement**, et la forme de ce marquage reste à trouver.

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

**Le seuil est une exigence, pas un quota.** Une barre unique sur l'écart, la même pour chaque son, partout et tout le temps. Jamais « marquer les n pires », jamais un budget par mot ou par tour, jamais un classement : **toutes les fautes se marquent, aucune ne s'élit**. Un quota serait une élection, et il ferait dépendre une marque de ce qu'il y a d'autre dans la phrase, ce que l'indépendance des tours interdit — la même faute doit produire la même marque à n'importe quel moment.

La barre est absolue, mais **ce qu'elle mesure est relatif** : l'écart de l'apprenant à son modèle. Exigence absolue sur une mesure relative — c'est ce qui permet au curseur d'exister. Le bouger déclare un niveau d'exigence pour la séance ; il ne change jamais la façon de mesurer.

**Se taire n'est jamais une issue.** Quand un repère est trop bruyant, la réponse est de le réparer ou d'en trouver un autre — jamais de restreindre la marque aux cas faciles ni de retirer une échelle. Une échelle qui ne parle que là où elle est sûre laisse passer les fautes qu'elle est là pour attraper, et l'apprenant n'a aucun moyen de savoir que le silence n'est pas une approbation. Décision de projet, à ne pas rouvrir : les compromis qui achètent la fiabilité en renonçant à des marques sont écartés d'avance. Sa portée, notée pour que la question ne se repose pas : il a été écrit contre l'idée de restreindre une échelle **en service** à ses cas faciles ; il n'ordonne pas de retenir l'app derrière une échelle qui n'est pas encore écrite. L'accent lexical (brique 7) reste de la recherche tant qu'il n'est pas fiable, ses canaux vides à l'écran, et la première version marque au niveau du son.

**Une marque est une proposition d'aller voir, pas un verdict.** L'écart au modèle est validé là où l'apprenant a **entendu** ce modèle avant de parler : deux fautes d'accent sur deux vues, aucune fausse alerte sur les calques (cf. `qualification.md`, bloc F). Sur un tour de conversation il n'a rien entendu, et l'écart s'affiche quand même.

**La règle vaut aux trois échelles, sans exception : le modèle est la source de vérité, et s'en écarter se marque.** On vise l'identique, au seuil près — un mot dit moins réduit que la synthèse, une syllabe accentuée ailleurs, une phrase qui monte là où le modèle descend. Aucune des trois n'a de régime à part.

Ça n'interdit rien à l'apprenant. Une phrase spontanée admet plusieurs mélodies justes, et qui veut mettre l'emphase sur un mot — « I said it **IS** important » — la met : sa courbe divergera de celle du modèle neutre, il la verra diverger, et il en fait ce qu'il veut. L'app montre l'écart ; elle ne prétend pas savoir ce que l'apprenant voulait dire. C'est le sens de « proposition d'aller voir » : la marque désigne, elle ne condamne pas.

**Question close, à ne pas rouvrir.** Se faire marquer souvent, c'est parler autrement que le modèle, ce que la mesure existe pour dire — ce n'est pas un symptôme à interpréter. La règle n'a pas de porte de sortie : une voix modèle qui ne conviendrait pas se **remplace**, et la voix suivante est la source de vérité exactement comme la précédente. Changer de norme ne revient jamais à s'en passer.

Ce qui rend l'écart tenable à l'usage est la forme de la parenthèse : une marque qu'on juge injuste mène à écouter le modèle et à redire, le calque concorde, et la parenthèse se referme sur une réussite. Un détour court, jamais une leçon fausse.

**Un modèle n'est un étalon que s'il s'étalonne.** Une voix de synthèse que l'analyse lit mal accuserait l'apprenant d'une faute commise par la machine — le principe se paie ici précisément parce que le modèle est cru aveuglément : rien d'extérieur ne viendra dire qu'il était bâclé. Toute voix promue modèle passe donc un test, et une voix qui échoue est écartée quelle que soit sa beauté (cf. « L'accent », où ce test se choisit).

## Écouter plutôt que se faire expliquer

Le remède d'une faute sonore est **d'entendre le modèle et de redire**, aux trois échelles : le mot, la syllabe, la phrase. Pas une consigne d'articulation écrite.

Ça a une conséquence sur ce qu'on exige de l'analyse. Une consigne (« la langue passe entre les dents ») a besoin de savoir **quel** son a été produit ; jouer un modèle n'a besoin que de savoir **où** ça cloche. Identifier le son produit devient donc un enrichissement, pas une condition — ce qui est heureux, puisque c'est la partie la moins fiable de ce qu'une machine acoustique rend.

La position se lit des deux côtés gratuitement : la grille dit où le modèle prononce chaque son, l'alignement dit où l'apprenant le prononce. Le modèle à jouer et **l'enregistrement de l'apprenant au même endroit**, à faire entendre juste après, tombent du même calcul.

**Les audios de synthèse sont gardés en cache**, indexés par le texte, la voix et le dialecte. Une même synthèse sert alors trois fois : d'étalon pour la mesure, de modèle à écouter, et de modèle à réécouter autant de fois qu'on redit le mot ou la phrase.

## La parenthèse

Une seule primitive, récursive : conversation → phrase → mot. Même geste à chaque étage, même bouton de sortie.

- **Ouverture** par la marque ou par un seuil. Le genre de la marque tient à la **nature** de la faute, jamais à sa gravité — une faute de prononciation est sévère sans être grammaticale, et confondre les deux ouvre la mauvaise parenthèse.
- **Prononciation à l'échelle de la phrase.** Un mot peut être zoomé — parenthèse dans la parenthèse, le temps de l'améliorer, puis retour à la phrase.
- **Le nombre de marques ne multiplie pas le travail.** L'unité de réparation est la phrase, le mot au plus ; jamais le son. Un tour ouvre **une** parenthèse, quel qu'y soit le nombre de marques, et trois sons marqués dans un mot font un seul mot à redire. Les marques n'y sont donc pas des corvées à liquider une à une : elles disent **où porter l'attention** en redisant. C'est ce qui rend tenable de tout marquer sans rien élire.

  Le prix se paie ailleurs, sur l'**affichage** et non sur la charge : mesuré sur de la parole d'apprenant réelle, la majorité des mots portent quelque chose (`../TODO.md`). Une marque binaire ne transporte alors plus rien — tout est colorié, donc rien n'est signalé. La forme du marquage, qui reste à trouver, doit donc **graduer** plutôt que colorier ou non.
- **Sortie** disponible à tout moment en un appui.
- L'IA sait que ces tours sont une **parenthèse et non du contenu** : elle reprend le sujet d'avant, elle n'enchaîne pas sur la grammaire.

Tout ce qui s'y dit est **neuf** — la phrase réparée, les variations, le mot zoomé — et chaque énoncé est noté contre un texte de référence **connu d'avance**, puisque c'est l'IA qui l'a proposé.

### Deux temps, qui s'enchaînent

**Temps 1 — corriger.** L'IA introduit en **une phrase** la manière correcte de dire, puis rend la parole. L'utilisateur formule alors une phrase **libre**, et elle doit satisfaire deux conditions : respecter la structure corrigée, et rester **cohérente avec la conversation**. Si elle les satisfait, le temps 1 est clos ; sinon il se poursuit.

*Libre* qualifie la formulation, pas le terrain. La phrase d'introduction de l'IA doit rendre la structure **inévitable** — sinon l'utilisateur, sans le vouloir, contourne l'endroit qui coince : corrigé sur `He don't know`, il répond *« I like pizza »*, la règle est respectée à vide et le temps 1 se clôt sans que rien n'ait été travaillé.

**La phrase produite remplace la phrase initiale** dans le fil, à la fermeture de la parenthèse. C'est la raison de la condition de cohérence : elle doit pouvoir tenir la place de ce qui avait été dit. C'est aussi ce qui donne au temps 1 son enjeu — on ne récite pas une correction, on réécrit son propre tour.

**Temps 2 — driller.** Proposé, jamais imposé : à la fin du temps 1, une indication visuelle offre de poursuivre la conversation, ce qui referme la parenthèse, ou de driller la structure travaillée, ce qui la laisse ouverte.

Les drills sont introduits par des phrases qui **imposent d'employer la structure**, et leur difficulté croît. Les variations sont formulées par l'utilisateur, jamais récitées : redire une fois la phrase qu'on vient de lui souffler n'apprend rien. Le drill est ainsi fait de la même matière que la conversation.

*Piste, non tranchée* : la difficulté monte d'elle-même à chaque réussite, et deux boutons permettent de la corriger. Le `-` est le vrai des deux — qui décroche ne saura pas nommer ce qui le dépasse, il voudra seulement que ça redescende.

### Sortir sans avoir réussi

Il n'y a **pas de sortie forcée**, seulement une sortie proposée. Un locuteur peut réparer la structure en cassant autre chose, indéfiniment, et rien n'oblige à en venir à bout.

Sortir avant d'avoir produit une phrase correcte et cohérente est donc légitime, et sans conséquence : **la phrase initiale reste telle qu'elle a été dite**, et tout se passe comme si l'on avait ignoré la faute dès le départ. La parenthèse est une occasion de réécrire son tour ; l'abandonner laisse le fil intact.

## Les deux tuyaux

Deux circuits distincts partagent une seule ressource : le fichier audio du tour de parole.

**Tuyau A — la conversation.** Micro → tampon local (PCM 16 kHz mono, un fichier par tour, **conservé**) → fin de parole → envoi au fournisseur de conversation → lecture de la réponse audio. Le fichier local est la condition d'existence du tuyau B : aucun service distant ne rend l'audio envoyé.

**Ce qui est décidé de la capture est un invariant, pas un mode.** Deux règles, et elles ne bougent pas :

- **Rien ne coupe la parole de quelqu'un qui parle encore.** Couper qui hésite, cherche un mot ou reprend sa phrase, c'est couper précisément qui apprend.
- **L'audio du tour est conservé localement**, sinon le tuyau B n'a rien à examiner.

**Le mode de capture, lui, est ouvert** : l'invariant ne le désigne pas, plusieurs gestes l'honorent. Quatre pistes, aucune écartée :

- **Armement automatique, un tap pour clore.** Ce que `design/ui-flow.md` a retenu : le micro s'ouvre dès que l'IA a fini, un geste par tour, faisable à l'aveugle. De l'air mort à la fin si l'on tarde à clore.
- **Appui / appui.** Deux gestes, mais aucun enregistrement non voulu.
- **Appui maintenu, relâchement = fin, puis validation.** Un seul geste, pas d'air mort, un réessai offert sans appel — mais tenir un bouton pendant qu'on cherche ses mots est une charge de plus, et le tour long y devient pénible.
- **Déclenchement au seuil, arrêt après un silence long.** Le mode que l'invariant menace le plus, et pas nécessairement au point de l'écarter : avec un seuil de fin généreux, ce qui se ferait couper n'est plus l'hésitation mais la pause de réflexion vraiment longue. À mesurer plutôt qu'à trancher.

Un critère pèse sur ce choix et ne relève pas de l'ergonomie : la mémoire d'une passe d'analyse croît comme le **carré** de la durée du tour (`analysis.md`). Un mode qui borne naturellement la durée vaut donc mieux qu'un mode qui la laisse filer — ça ne désigne pas de gagnant, ça interdit de choisir sur le seul confort.

**Tuyau B — analyser.** Le tour est examiné, **sur l'appareil**, pour savoir s'il y a un problème et où. Se fait sur l'enregistrement existant, sans jamais rien redemander.

L'audio d'un tour ne quitte donc l'appareil **qu'une fois**, pour la conversation. L'analyse ne l'envoie nulle part.

## La panne

La conversation dépend de services distants, et chacun peut manquer — réseau coupé, quota épuisé, fournisseur en panne, clé expirée. L'analyse, elle, ne dépend que de l'appareil. La règle se décline par brique, pas globalement :

- **La chaîne de conversation est bloquante, mais réparable par construction** : le fichier conservé fait qu'un envoi raté se **réessaie sans redire la phrase**. L'échec coûte un bouton, jamais une parole perdue. La parenthèse grammaticale, qui ne consomme que cette chaîne, hérite du même traitement.
- **L'analyse ne dépend d'aucun réseau**, ce qui retire d'un coup le quota, la clé expirée et le fournisseur en panne. Ce qui reste est d'un autre genre et se règle **une fois pour la session, pas à chaque tour** : les poids ne sont pas encore téléchargés, ou l'appareil ne parvient pas à les charger. Dans ce cas les marques sonores sont éteintes de bout en bout, et l'option éteinte **porte sa raison** comme toute autre. Un tour reste analysable tant que la session a démarré avec son modèle chargé.
- **La parenthèse de prononciation a besoin de la synthèse, pas du réseau d'analyse.** Redire sans modèle à entendre n'est pas un exercice dégradé, c'est de l'auto-évaluation à l'oreille — ce que l'architecture refuse partout ailleurs. Si la synthèse manque en cours de parenthèse et que le cache ne porte pas la phrase, l'app le dit franchement et propose la sortie ; la phrase initiale reste telle qu'elle a été dite, comme pour toute sortie sans réussite.
- **Ni file hors-ligne, ni réanalyse différée** : analyser un tour trois tours plus tard produirait des marques sur du passé, ce que l'indépendance des tours interdit. Ce qui n'a pas été analysé sur le moment ne le sera pas.

## Le texte de référence

L'analyse compare l'audio de l'apprenant au modèle synthétisé pour un **texte donné**. En conversation libre, ce texte n'est pas connu d'avance, et c'est la seule contrainte de la chaîne qu'on ne peut pas contourner : les mesures fines n'existent que là où le texte est écrit d'avance.

La porte grammaticale en retire déjà la moitié du problème — sur un tour fautif, il n'y a pas d'analyse, et la phrase corrigée est redite contre un texte certain. Reste le tour grammaticalement propre.

**Décision** : le texte vient de la transcription, que le LLM peut corriger à partir du contexte de la conversation. La répartition des rôles est une règle : **le STT transcrit la bouche, le LLM décide l'intention.** La normalisation appartient au LLM, qui a le contexte et une instruction — jamais au STT, qui la ferait en silence et sans contexte ; un STT qui répare la grammaire d'office efface le signal d'apprentissage avant tout jugement, et sa fidélité verbatim est un critère de choix (cf. `../TODO.md`, chantier 2). La ponctuation d'`intended` fait partie de cette tâche : le LLM ponctue selon l'intention à laquelle il répond — s'il répond à une question, il a lu une question — et le contour du modèle TTS en dépend. Deux faits mesurés encadrent ce choix.

Le piège se déclenche moins souvent qu'on le craignait : sur *« I sink »* comme sur *« I am walkin »*, la reconnaissance a rendu `think` et `walking` d'elle-même — le modèle de langue normalise vers le mot plausible, et il n'y avait rien à corriger.

Mais **une reconstruction fausse coûte**. *« Turn light at the corner »* reconstruit en `left` au lieu de `right`, le contexte admettant les deux : noté contre `left`, le mot rend quatre sons aberrants d'un coup.

Ce coût avait été écrit plus grand qu'il n'est. Le doc annonçait que le mot fautif « entraîne le suivant avec lui », donc une **rafale** ; mesuré depuis, une faute ne se propage pas — le reste de la phrase reste aussi propre qu'un témoin (`analysis.md`, brique 11). Le cas propre du texte faux n'est pas mesuré pour autant, aucune prise du jeu n'en portant.

**Le garde-fou qui en découlait est à revoir.** Il devait écarter un tour où beaucoup de sons s'effondrent ensemble, au motif que ça signale plus probablement un mauvais texte qu'un mauvais locuteur. Cette inférence ne tient pas : un apprenant très accentué s'écarte lui aussi partout, et rien dans le test ne l'en distingue — mesuré sur une prise du jeu, où sept différences relevées, une seule est la faute voulue et six sont des traits d'accent. Le garde-fou éteint alors l'analyse sur ceux qui en ont le plus besoin, **et sans le dire**, ce que la règle du silence interdit par ailleurs.

Ce qui le remplacera n'est pas décidé. Une piste : **marquer le mot** — un texte faux donne un mot très écarté, l'écran affiche le texte transcrit, donc l'erreur est bornée à un mot et lisible par qui la reçoit. Resterait à contrôler l'**alignement dégénéré** (exigence 1 ci-dessous), qui se lit à des durées absurdes sans rien inférer sur le texte.

## L'affichage

La conversation s'affiche en texte : on ne colorie pas une portion de son.

Les tours de l'IA sont **floutables** — un réglage d'écoute, indépendant de tout le reste. Les lire au lieu de les écouter fait sortir la compréhension orale par la fenêtre.

Le flou ne gêne pas la correction : la marque est sur le tour de **l'utilisateur**, et la parenthèse s'ouvre dessus sans jamais avoir à déflouter la réponse de l'IA.

## Ce qui survit à la session

Trois matières, trois sorts — la ligne de partage est le coût de reconstruction, pas la nature du fichier :

- **La voix de l'apprenant** (un fichier par tour, micro) est **purgée à la fermeture de la session**. Elle n'a aucun consommateur au-delà : l'analyse est interne au tour, la parenthèse rejoue l'extrait sur le moment, et l'indépendance des tours interdit tout usage ultérieur. Aucun stock de voix ne dort sur l'appareil.
- **Le cache des synthèses** survit, sous **plafond de taille avec éviction du moins récent** : ce n'est qu'un cache, régénérable au prix d'un appel, et les phrases de modèle reviennent d'une session à l'autre.
- **La trace écrite** — les tours, qui parle, le texte, les marques avec leurs ancres, les événements de parenthèse — est **archivée telle que l'écran l'affiche**, sans modélisation ajoutée : des faits bruts, dont les usages futurs (historique, révision, progression) se dériveront s'ils adviennent. En v1 l'archive est **en écriture seule** : rien ne la lit — l'indépendance des tours n'interdit pas de se souvenir, elle interdit que le souvenir pèse sur l'analyse et le marquage.

## L'accent

Réglage **global unique**, exposé à l'utilisateur. Il gouverne deux choses, qui sont en réalité la même : la voix qui parle, et la voix du modèle à imiter.

**Il n'y a pas de troisième chose à aligner.** L'analyse ne consulte aucun référentiel de dialecte : elle compare l'apprenant au modèle, et le modèle est la seule norme. Si le modèle est britannique, tout ce qui en dérive l'est — mécaniquement, sans lexique à choisir ni accord à vérifier. L'accent cesse donc d'être un paramètre de mesure pour n'être plus qu'un choix de voix, et le désaccord d'accent, qui inversait la mesure quand un référentiel extérieur existait, n'a plus de lieu où se produire.

**La voix se choisit.** Une cascade — le fournisseur de synthèse, puis son modèle quand il en expose plusieurs, puis la voix — et un **étalonnage à la demande** qui avertit si elle échoue. Le modèle est dans la cascade et pas seulement le fournisseur : `chatterbox` et `chatterbox-turbo` offrent les mêmes noms de voix et ne rendent pas la même voix. Rien n'est imposé et rien n'est deviné : une voix qui ne s'étalonne pas reste utilisable pour parler, mais elle est signalée comme impropre à servir de modèle. **Ce que l'étalonnage vérifie est à redéfinir**, et **aucune voix n'est aujourd'hui qualifiée** : le test qui existait jugeait une voix aux notes d'un service dont l'app ne dépend plus. Ce qui le remplacera se lit de la matrice — une grille nette, pas de son écrasé, pas de zone où la répartition s'effondre (cf. `../TODO.md`). L'oreille ne peut pas rendre ce verdict : elle juge la voix comme modèle à imiter, pas comme étalon de mesure — une voix naturellement relâchée sonne d'autant mieux qu'elle étalonne mal, et le même modèle acoustique voit une faute sur une voix et la manque sur une autre (`../TODO.md`, tableau de `faults.py`). Les deux aptitudes se vérifient chacune par son juge.

Par défaut, **le modèle à imiter est la voix de la conversation** — c'est celle qu'on entend déjà, et rien ne justifie d'en présenter une autre. Les dissocier reste possible pour qui le veut : les briques *conversation* et *synthèse* sont indépendantes, unifiées par le seul paramètre d'accent.

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

**Le montage est la chaîne STT → LLM → TTS**, tranchée, plutôt qu'une API voix-à-voix. Trois raisons, dont la dernière est mesurée : chaque maillon reste substituable ; la reconstruction du texte de référence voyage dans l'appel LLM qu'on fait de toute façon, là où le voix-à-voix exigerait un appel supplémentaire par tour rien que pour l'obtenir ; et la latence est bonne — **2,6 s jusqu'au premier son** sur un tour court, de bout en bout (cf. `conversation-chain.md`).

Deux points de montage réglés par la même mesure. La synthèse **n'est pas pipelinée** sur la première phrase du modèle : le gain est de 0,16 s, parce que le modèle achève son objet un septième de seconde après sa première phrase. Et la reconnaissance se fait **par fichier, pas en flux** : elle coûte un sixième de la durée de l'audio, ce qui pèse sur le tour long — qui est l'exception, pas le régime nominal. Rien ne se complique tant que l'usage n'a pas montré que ça gêne.

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

## Indexer des chemins nommés, jamais `git add -A`

Ces deux formes indexent ce qui **se trouve** dans l'arbre, y compris ce que personne n'y a mis — et un intrus qui n'est pas un fichier ordinaire fait échouer l'indexation entière, avec un message qui nomme l'intrus sans dire d'où il vient (« ne peut ajouter que des fichiers normaux, des liens symboliques ou des répertoires »).

Cas rencontré : le bac à sable des commandes de l'agent neutralise les fichiers de configuration qu'il refuse de laisser lire — `.bash_profile`, `.gitconfig`, `.mcp.json`, `.vscode` et d'autres — en montant `/dev/null` par-dessus, **dans le dossier courant**. Ils apparaissent donc à la racine du projet comme périphériques caractère, et `git status` les voit non suivis. Ils n'existent que dans la vue du bac à sable : le dépôt sur le disque est propre, et il ne faut donc **pas** les gitignorer — ce serait mettre dans le projet une ligne qui parle d'un outil, pas de lui.

## Le build a besoin du réseau, le bac à sable le lui refuse

`./gradlew` — donc `./run build`, `install`, `release` — va chercher la distribution Gradle, puis les dépendances, sur le réseau. Sous le bac à sable des commandes de l'agent, il échoue en `UnknownHostException: services.gradle.org`, une panne de nom qui ne ressemble en rien à une restriction. **Ces commandes se lancent avec le bac à sable désactivé**, sans passer par la boucle « essayer, lire l'erreur, réessayer ».

## Construire en debug, sauf quand la doublure de release change

`./gradlew :app:assembleDebug` suffit pour tout le travail courant, et `assembleRelease` est long — R8, le rétrécissement des ressources, un APK complet — pour ne rien apprendre la plupart du temps.

La seule raison de toucher au release est que `app/src/release/` existe : la doublure `Analyses` qui répond qu'aucun moteur d'analyse n'est embarqué. **Ce source set n'est jamais compilé par le build debug**, donc une erreur y dort jusqu'à la RC. Il fait vingt lignes et ne bouge quasiment pas.

D'où la règle : debug par défaut ; compiler le release **seulement** quand `app/src/release/` ou la couture `Analysis` change, et avant une RC. Et alors `:app:compileReleaseKotlin` plutôt qu'`assembleRelease` — c'est la compilation qui manque, pas l'APK.

## Hors périmètre

Écarté délibérément de la première version, non par oubli :

- Tout ce qui **s'accumule** : ligne de base par session, motif récurrent, tendance, suivi longitudinal (cf. « Chaque tour est indépendant »).
- Pilotage de la conversation par les faiblesses phonétiques de l'utilisateur.
- La consigne d'articulation écrite, remplacée par l'écoute d'un modèle.

Ouvert, à trancher : **le son isolé comme niveau d'écoute**. L'objection tenait à la production — un son réussi seul se rate encore dans le mot, parce que ce qui coince est la transition. Elle ne s'applique pas forcément à l'écoute, qui ne demande rien à la bouche.
