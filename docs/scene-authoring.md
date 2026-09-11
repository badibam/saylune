# Écrire une scène

Guide d'écriture, fait pour être suivi tel quel par une session d'IA — `./run scene` en lance une dessus — et lisible par un humain qui écrit une scène à la main.

Une scène est une **définition d'activité livrée avec l'app** : un fichier de contenu, jamais du code. Elle est offerte par l'une des quatre portes — Libre, défi, arcade, histoire (`activity.md`) —, et ce que la porte permet décide de ce qu'elle a le droit d'écrire. Ce guide a une partie commune, puis une section par porte.

## À lire avant d'écrire une ligne

- `design/scene-state.md`, tant qu'il existe — le modèle d'une scène : des cases, qui les écrit, des événements qui les lisent, des textes pour le meneur, pour l'apprenant ou pour les deux, les questions et les trois portes d'un passage. Le **meneur** est l'IA qui fait parler tous les personnages et le narrateur ; le **juge** est celle qui marque la phrase de l'apprenant, et il ne lit que ce que l'apprenant lit.
- `activity.md` — les portes, la définition, la fin et son issue.
- `design/scene-vectors.md`, tant qu'il existe — trente-six sortes de scènes, chacune avec un exemple : une réserve d'idées, pas une liste à transcrire.

## Le schéma ne se recopie jamais ici

**Le code se lit, à chaque fois, avant d'écrire le fichier** : ce qui charge les définitions dit exactement quels champs sont lus, le catalogue des leviers (`levers/Levers.kt`) quelles valeurs un levier prend, celui des avertissements (`activity/Cautions.kt`) quelles clés existent, l'arbre des feuilles quels chemins de poids. On écrit ceux-là et aucun autre, et une scène déjà livrée se lit comme exemple de forme.

Un guide qui recopierait le schéma serait une seconde source qui dérive du code, et la scène suivante ne chargerait plus. **Si le code ne lit pas un champ, il ne s'écrit pas** : une règle de ce guide qui n'a pas encore de champ reste hors du fichier, et on le dit à la fin.

## Ce que toute scène tient

- **Un moteur de parole, pas une situation utile.** La scène doit tenir jusqu'à sa fin — quarante passages en Libre, où il n'y en a pas — sans que la fiction craque. Une scène transactionnelle — commander, réserver, acheter — meurt au sixième tour.
- **Le personnage s'écrit contre la pente du modèle, qui est d'être agréable.** Il porte six choses : qui il est concrètement, ce qu'il veut de cette conversation, son défaut qui coûte quelque chose à l'apprenant, ce qu'il ne fera pas, comment il parle, et ce qu'il a déjà décidé sur l'apprenant. Un personnage chaleureux et curieux qui finit par *« and what about you? »* est un échec. Ce qu'il a de difficile à suivre — un débit, un accent, des phrases longues — s'écrit dans sa description, jamais en levier.
- **La situation et la description ne se mélangent pas.** La situation place l'apprenant, à la deuxième personne, dans sa langue, et se lit à l'écran. La description place le personnage, en anglais, part au meneur, ne s'affiche jamais. Toutes deux sont déclarées par la fiche et ne changent pas pendant la partie ; ce qui change passe par un événement.
- **Un personnage ne corrige jamais l'anglais de l'apprenant** : c'est le travail de l'app. Il réagit à ce qu'il comprend, avec son propre jugement.
- **Le meneur ne voit aucune note.** Pour qu'un personnage réagisse à une phrase mal formée, un événement lit la note et lui envoie un texte, et il réagit au tour suivant, jamais dans la réplique qui répondait à la phrase notée.
- **Un trou demande une ancre vraie sur l'apprenant** — *un savoir-faire à transmettre*, pas *le métier tenu dans cette scène*. Sur ce qui lui appartient, l'apprenant a quarante tours de matière ; sur une fiction qui n'est pas la sienne, il s'arrête au huitième. Un trou est une invitation, deux sont un formulaire.
- **Une case que le meneur écrit vaut mieux qu'une question.** Pour savoir si Dana a dit ce qui la blesse, on demande la case au meneur à chaque passage, d'après ce qui a été dit. Une case porte une courte description en anglais — *« the barman's patience »* —, et se cache au meneur si elle est un compteur technique ou un secret qu'il ne doit pas trahir.
- **Une question est un choix de joueur**, à réserver à ce qui doit être sans ambiguïté parce que la scène bifurque dessus. Sa liste nomme ce qu'un apprenant dira vraiment. L'auteur dit combien de fois elle se repose avant que « pas de réponse » soit la réponse : zéro, un chemin tout de suite ; quelques fois, puis le chemin ; sans limite, un blocage, à réserver aux vrais embranchements, parce qu'il peut enfermer qui ne comprend pas la question. Posée par le narrateur, elle se rejoue mot pour mot ; par un personnage, il réagit à ce qui a été dit et la redemande autrement.
- **Le point faible d'un personnage se joue sur l'histoire, pas sur les marques** : un texte pour le meneur seul — *« The dragon softens only when they speak of its lost treasure »* —, une case qu'il écrit, un événement qui en tire la suite. Aucune marque ne dépend jamais d'un critère caché à l'apprenant.
- **Une consigne ne fait que durcir** — *raconte au passé*, jamais *ignore les temps* —, dure un nombre de passages ou la scène, et se dit à l'apprenant avant d'agir. Pour les deux par défaut : le meneur fabrique alors l'occasion — *« So what happened last night? »* — et ne la sabote pas. Cachée aux personnages, elle fait un jeu — glisser un mot sans que personne le repère — et l'app ajoute d'elle-même *« Les personnages n'ont pas cette information »*.
- **Faire parler un personnage sans attendre l'apprenant** : une annonce s'écrit d'avance ; une scène qui doit tenir compte de ce qui s'est dit — un voyageur qui arrive et se dispute avec Lou — s'écrit en direction au meneur, qui la joue.
- **Un texte qui cite une case sans valeur de départ dit quoi écrire si elle est vide** : `{wish | nothing at all}`.
- **Réagir à chaque fois se teste sur l'état, pas sur le changement.** Un tirage qui retombe sur la même valeur ne change rien, et une condition sur un cran fréquent — au moins un mot marqué — part presque à chaque passage.
- **Une fiche déclare ce qu'elle est faite pour faire**, avec les clés de `activity/Cautions.kt` et aucune autre — une clé inconnue fait échouer le chargement. Deux familles : ce que le personnage **fait** (conflit, hostilité, mensonge, pression, examen, intimité) et ce dont il est **question** (deuil, violence, corps, discrimination, substances, isolement). L'auteur répond oui ou non sur sa propre scène sans rien supposer de qui la jouera, donc du cadre et pas de la séance. Vide veut dire *rien de déclaré*, jamais *rien à signaler*. Rien ne filtre là-dessus.
- **Le texte français d'une fiche tutoie l'apprenant** : c'est Saylune qui lui parle — *« Ce que Dana te reproche »*, jamais *« Le reproche de Dana »*. L'anglais dit `you`.
- **Sans genre fixé, un nom non genré**, et plutôt un nom qui porte un âge qu'un nom à la mode.

## Par porte

La vérification au chargement tient ces règles : une fiche qui en enfreint une ne se charge pas, et ne se livre donc pas.

### Libre

**Aucun enjeu.** Rien ne s'y gagne ni ne s'y perd, et en Libre les réglages appartiennent à l'apprenant.

- Aucun levier, ni au départ ni par un événement — aucun n'échappe à la pression, et une fiche qui en poserait un prendrait un réglage à l'apprenant.
- Aucune consigne, cachée ou non : le juge y juge déjà la pertinence contre la situation et la conversation.
- Aucun test sur la façon dont l'apprenant parle — ni note, ni porte, ni silence. Seul le numéro du passage se lit, qui rythme l'histoire sans rien juger : le troisième voyageur arrive au passage 8.
- Pas de poids, l'app mettant 1 partout, et jamais de fin.
- Un seul trou, sur une ancre vraie.
- Les cases que le meneur écrit, les questions et les secrets restent permis : Lou qui s'ouvre quand l'apprenant la fait rire est jugée par le meneur, avec l'histoire, pas avec une note.

### Défi

**Un objectif annoncé, et une fin** : réussi, raté, ou la note décide.

- La quantité est une condition de fin, pas un critère de réussite : un A sur deux passages ne prouve rien, donc *assez de passages* se lit dans ce qui termine la scène.
- La pression peut venir des leviers — vies, durée du tour —, de l'humeur d'un personnage, ou de consignes.
- Une réussite que le meneur juge — *le barman accepte* — est demandée, donc personne ne vérifie qu'elle a vraiment eu lieu : l'écrire en sachant qu'elle repose sur lui.

### Arcade

**Quatre définitions, d'*easy* à *extreme*, rejouées sans fin**, une rampe qui durcit en cours de partie, la fin à zéro vie, et un score par difficulté plutôt qu'une réussite. Comment le score se calcule reste à écrire (`activity.md`).

### Histoire

**Un fichier d'histoire, puis une fiche par scène.**

- Le fichier de l'histoire déclare l'ordre des scènes, les embranchements, les personnages une seule fois — nom, genre, voix, description —, et ce qui doit vivre toute l'histoire : ses cases, ses leviers, ses événements, qui tournent dans chacune de ses scènes. Une scène ne redéclare rien de l'histoire et cite ses personnages par leur clé.
- L'issue de chaque scène est une case de l'histoire, lue par un embranchement. **Une scène qui peut finir ratée oblige l'histoire à dire ce qui suit** : un embranchement, la rejouer, ou la fin.
- Ce qui doit attendre un choix se déclenche sur la réponse, pas sur un numéro de passage.

## Combien, et avec qui

**Une session ne porte pas sur une scène.** Elle commence par lire les définitions livrées, dire ce qui existe par porte, et **demander combien on en fait dans cette passe, et pour quelle porte**. Ne jamais le supposer.

## Ce qui se demande et ce qui se propose

On interprète comme le ferait un collègue attentif, et on ne s'arrête que là où deux lectures donneraient un travail différent.

**Se propose, et on continue** — le métier à l'intérieur d'un cadre décidé : le nom, l'âge, le défaut, ce qu'il refuse, sa façon de parler, la formulation de la situation, la tournure de l'ancre, les phrases des textes. Ça se montre en quelques lignes avant d'écrire le fichier — *qui / ce qu'il veut / son défaut / ce qu'il ne fera pas / comment il parle / ce qu'il a déjà décidé sur toi*, et pour une scène qui bouge, ses cases et ses événements en une ligne chacun — pour qu'une phrase suffise à réorienter. Puis on écrit.

**Se demande, et on s'arrête** — tout ce qui déplace le cadre :

- la porte d'une scène, et ce qui la fait bouger : son enjeu, sa fin ;
- une scène dont le moteur fait double emploi avec une déjà écrite ;
- une règle de sa porte qu'il faudrait enfreindre ;
- un champ que le code ne lit pas ;
- un `id` ou un nom court qui entre en collision avec un existant.

**Écrire une scène neuve est de la conception, pas de la transcription**, et ça suit la méthode de brainstorming du projet : énoncer d'abord la version la plus bête qui pourrait marcher et pourquoi elle ne suffit pas, puis **une seule question à la fois, avec un avis à chaque fois**, du global au spécifique.

## Le geste, scène par scène

1. Lire le code qui charge les définitions et les catalogues.
2. Proposer le personnage, et ce qui fait bouger la scène, en quelques lignes ; attendre le feu vert.
3. Écrire le fichier dans `app/src/main/assets/definitions/`. Son nom et son `id` coïncident, le chargement le vérifie.
4. Committer — **une scène par commit**, message en anglais.

Puis, une fois la passe finie : `./gradlew :app:testDebugUnitTest --tests '*DefinitionTest*'`. Ce test charge **toutes** les définitions livrées, depuis le dossier même que l'app empaquette, sans appareil ni réseau.

## Les fautes à ne pas faire

- Un personnage qui corrige l'anglais, ou qui réagit à une note dans la réplique même.
- Une marque qui dépend d'un critère caché à l'apprenant.
- Une consigne qui adoucit.
- Un blocage sur un choix qui n'est pas un vrai embranchement.
- En Libre : un levier, une consigne, un test sur la façon de parler, deux trous, une fin.
- Une scène d'histoire qui peut rater sans que l'histoire dise ce qui suit.
- Inventer un chemin de poids, un nom de cran, une clé d'avertissement — tout se lit dans le code.
- Un nom genré quand la fiche ne fixe pas le genre, un nom court de plus de dix caractères.

## À la fin

Dire quelles scènes ont été écrites et pour quelle porte, lesquelles restent à faire, ce que le guide demandait et qu'aucun champ ne porte encore, et donner le résultat du test.
