# Le modèle d'activité

Conçu le 2026-09-01. Ce doc porte la forme que prend le travail dans l'app : ce qu'est une unité de matière, ce qu'est une activité, et ce que l'app garde. Il remplace les notions de session et de parenthèse, qui n'existent plus.

Ce qui est écrit ici est tranché. Ce qui reste ouvert est en fin de doc, nommé.

## L'énoncé

L'**énoncé** est l'unité de matière : un texte, un locuteur, l'audio, l'analyse. **Toute parole dans l'app est un énoncé** — la réponse de l'IA comme la phrase de l'apprenant.

Le locuteur est l'apprenant, l'IA, ou un modèle humain. Ce dernier cas est à traiter plus tard et ne change rien à la forme.

Un énoncé peut pointer vers celui qu'il reprend. C'est ce qui rattache une phrase redite à celle qu'elle rejoue, sans qu'aucune notion nouvelle soit nécessaire.

La plupart des énoncés ne portent aucune analyse — ceux de l'IA n'en ont pas, et c'est voulu : garder une seule table pour tout ce qui se dit donne le fil de la conversation gratuitement, comme simple suite ordonnée.

## L'activité

Une **activité est atomique** : elle est un seul objet, qui porte son format, sa matière, ses réglages, son statut, ses horodatages et son résultat. Il n'y a pas d'objet séparé pour la portée ni pour l'exécution — les deux ne se distinguaient que par une indirection dont rien n'avait l'usage.

Les énoncés s'accrochent à une activité, et **toujours à un seul parent**. C'est ce qui garde chaque requête sur la matière simple, quelle que soit l'activité d'où elle vient.

**La matière est du texte libre**, plus des pointeurs structurés là où ils sont gratuits — les sons, que l'analyse rend déjà codés. Rapprocher deux activités portant sur la même chose est un travail de lecture, que le modèle de langue fait ; ce n'est pas un travail de schéma, et exiger un vocabulaire fermé coûterait bien plus qu'il ne rapporte.

**Reprendre, c'est continuer une activité non terminée.** Recommencer à l'identique ou faire varier un réglage sont des fonctionnalités possibles plus tard ; rien n'est à anticiper pour elles.

**Statut** : suggérée, écartée, en cours, terminée, abandonnée. Une suggestion est une activité qui n'a jamais commencé, et l'écarter est un geste qui compte — sans lui, une suggestion refusée revient.

**Résultat** : une issue, le juge, sa date, et un texte libre. Il est **stocké et non recalculé**, parce qu'il repose sur un jugement — d'une IA ou d'un humain — que rien ne reproduit à l'identique. Savoir qui a jugé et quand est ce qui permet de comparer deux résultats séparés dans le temps ; sans cette attribution, toute agrégation mélange des juges sans le dire. La forme minimale est celle-ci, et elle s'enrichira.

## La conversation

La conversation **est une activité**, dans la même table, avec **le même schéma**. Tous les champs ont un sens pour elle : sa matière est ce dont on parle, ses réglages sont ceux de la séance, son statut dit si elle est encore ouverte, son résultat s'agrège comme les autres.

Ce qui la distingue tient à son comportement, jamais à sa forme :

- son fil est simplement la suite ordonnée de ses énoncés, donc aucun champ ne le porte ;
- elle se reprend en l'état, ce qui est une politique sur son statut et non une structure ;
- elle est aussi prescriptrice, ce qui est dit par l'origine des activités qu'elle crée.

**Elle peut travailler les cinq directions.** C'est ce qui rend les activités séparées non prioritaires : elles restent dans le modèle, prêtes à être remplies, non construites.

## Ce qui n'existe pas

**Pas de session.** Les réglages du jour appartiennent à la conversation en cours, et la purge — la seule autre chose qu'une session portait — se règle autrement.

**Pas de parenthèse.** Une chose remarquée a deux sorts, et deux seulement :

- **redire sur place**, ce qui produit un énoncé de plus dans la même conversation, pointant vers celui qu'il reprend ;
- **devenir une activité suggérée**, faite plus tard si on veut.

Interrompre pour partir travailler ailleurs n'existe plus. Une interruption ne se justifiait que par une direction à travailler, et une direction se règle dans la conversation elle-même — sortir pour faire la même chose ailleurs se marchait sur les pieds.

## Les prescripteurs

Un **prescripteur** est ce qui remplit une activité — sa matière et ses réglages. Il y en a trois :

- **la conversation**, à partir de ce qui vient de s'y passer ;
- **l'apprenant**, par choix ou par consigne libre, ce qui est la voie par laquelle de la matière neuve entre ;
- **la mémoire**, pour ce qui est dû, jamais réussi, ou sur le point d'être oublié.

Un quatrième viendra peut-être — le contexte global, qui suggérerait de lui-même. Il n'a rien à préparer : ajouter un prescripteur n'est qu'une façon de plus de remplir les mêmes champs.

## Les cinq directions

Élocution, compréhension, formulation, fluidité, richesse. Elles sont indépendantes : on peut être intelligible et lent, correct et pauvre, fluide et faux.

- **Élocution** — les sons, le rythme, l'accent des mots, la mélodie. Ce qui décide qu'on est compris.
- **Compréhension** — suivre quelqu'un à sa vitesse, avec ses réductions, sans texte.
- **Formulation** — la phrase bien formée, et la règle appliquée en parlant plutôt que sue.
- **Fluidité** — trouver ses mots assez vite, enchaîner, ne pas s'arrêter au milieu.
- **Richesse** — le mot précis, le registre, la nuance. C'est la seule dont l'échec est invisible : rien ne signale qu'on vient de dire une version pauvre de son idée.

## Les réglages

**La forme des réglages dépend de l'activité.** Ce qui suit vaut pour la conversation.

Ils sont **fixés pour toute sa durée** et se règlent **par direction**. Ils infléchissent le contenu par le prompt, et peuvent aussi infléchir la forme : une prononciation à zéro éteint l'analyse de son, plutôt que de la calculer pour ne rien en montrer.

Le détail de ce que chaque réglage produit est à préciser.

## La capture

La capture est l'un des axes que les réglages gouvernent. Elle n'est pas spécifiée, et ce qui suit est ce qui est acquis.

**Plusieurs modes sont possibles, aucun n'est arrêté.** Deux ont été travaillés : l'**armement manuel**, et l'**automatique** où le tour s'ouvre et se ferme seul, la fermeture sur silence étant elle-même un levier réglé.

Le micro **ne s'arme jamais avant la fin de la réponse de l'IA**. Un symbole est visible dès que ça enregistre : il n'informe pas seulement, il fait partie de la pression — savoir que ça tourne change la façon dont on parle.

**Chaque tour porte son mode de capture.** C'est ce qui dit si ses silences sont significatifs, et rien ne s'agrège entre modes. Sous armement, le délai avant de parler mesure le geste et non l'hésitation ; agréger ces tours avec des tours captés automatiquement produirait un chiffre qui ressemble à de la fluidité sans en être.

## L'audio

**Un tour est une liste de segments** : une durée de silence, ou de l'audio. Le silence n'est jamais stocké en échantillons — il ne coûte alors que sa durée, là où le garder ferait passer une conversation de vingt minutes à des dizaines de mégaoctets. On reconstruit l'audio d'origine quand on en a besoin.

Chaque segment de parole garde une **marge de vrai audio** de part et d'autre. Ce qui identifie une occlusive vit dans la transition, et couper au ras de la parole abîmerait la mesure.

**On n'envoie pas de silence au réseau.** Ce que chaque mesure lit exactement — l'audio brut, les segments, la reconstruction — est à préciser mesure par mesure.

**L'audio n'est pas purgé par défaut.** La purge est à redéfinir : la règle du doc principal, qui l'attachait à la fermeture d'une session, n'a plus d'objet.

## Ce qui se stocke

**On stocke ce qui dépend de quelque chose qui ne se retrouvera pas** — l'audio d'un moment, le jugement d'un juge, une version de modèle. **On recalcule tout ce qui ne dépend que des lignes** : le nombre d'énoncés, leur ordre, les durées, tout ce qu'un compte suffit à produire.

**Tout ce qui est stocké porte la version de ce qui l'a produit.** Une analyse refaite avec un modèle différent ne rend pas les mêmes chiffres ; sans cette marque, deux époques de mesure s'agrègent en silence et la comparaison est fausse sans que rien ne le dise.

La liste de ce qui doit être agrégeable reste à préciser.

## Ce qui reste à spécifier

- **La pression.** Ce n'est pas un mécanisme mais un concept qui circule par plusieurs voies — le degré de liberté laissé à la production, la contrainte imposée, le temps, l'étayage retiré, l'imprévu, l'exigence de marquage. Entièrement à définir.
- **Le détail des effets des réglages**, sur le contenu comme sur la forme.
- **Le catalogue des activités et leur déroulé.** Aucune n'est nommée à part la conversation.
- **Les déclencheurs de suggestion** pendant une conversation.
- **Le prescripteur mémoire** : sur quoi il s'appuie, comment il choisit ce qui est dû.
- **Le remplacement d'une phrase fautive** dans le fil.
- **La purge**, et la durée de vie des audios.
- **Ce que chaque mesure lit** de l'audio, segment par segment.
- **La liste de ce qui doit être agrégeable.**
