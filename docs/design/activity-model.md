# Le modèle d'activité

Conçu le 2026-09-01, **élagué le 2026-09-01 de ce qui est construit**. Ce qui reste ici est la part du modèle d'activité qui n'a pas encore de code, plus ce qu'elle laisse ouvert.

Ce qui est parti et où le lire : l'**énoncé**, l'**activité** et **ce qui se stocke** sont dans `../reference.md` pour la règle et dans le code pour la forme (`activity/Activity.kt`, `conversation/TurnPipeline.kt`, `store/`). La suppression de la **session** et de la **parenthèse** est actée dans `../reference.md`. Les commits sont la carte.

Ce qui est écrit ici est tranché, sauf mention contraire. Ce qui reste ouvert est en fin de doc, nommé.

## Les prescripteurs

Un **prescripteur** est ce qui remplit une activité — sa matière et ses réglages. Il y en a trois :

- **la conversation**, à partir de ce qui vient de s'y passer ;
- **l'apprenant**, par choix ou par consigne libre, ce qui est la voie par laquelle de la matière neuve entre ;
- **la mémoire**, pour ce qui est dû, jamais réussi, ou sur le point d'être oublié.

Un quatrième viendra peut-être — le contexte global, qui suggérerait de lui-même. Il n'a rien à préparer : ajouter un prescripteur n'est qu'une façon de plus de remplir les mêmes champs.

Les trois sont nommés en code (`Prescriber`) et **seul l'apprenant en est un pour l'instant** : ouvrir l'app est l'apprenant qui demande une conversation. Les deux autres attendent le catalogue des activités.

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

**L'audio n'est pas purgé par défaut, et la purge est à écrire.** À ne pas confondre avec le balayage des enregistrements que plus aucun énoncé ne nomme, qui est fait et n'est pas une purge (`../reference.md`).

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
