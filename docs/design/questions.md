# Les questions — ce que le modèle établit

Ce que le modèle décide de la fiction, quand on le lui demande, ce qu'il en revient, et où ça se relit.

**Conception du 2026-09-07, rien n'est implémenté.** Mesuré en séance : `Definition.questions` se charge depuis le JSON et **aucune pièce ne le joue** — `providers/ConversationPrompt.kt` n'a ni champ de retour pour les réponses ni injection de ce que les scènes passées ont répondu, malgré ce qu'annonce le commentaire de sa partie 2, et `Outcome.says` est une chaîne unique. Il n'y a donc pas de migration : la forme ci-dessous s'écrit à neuf. Ce doc porte la spec ; il s'élague quand le code est en place, et ce qu'il corrige dans `activity.md` s'y écrit à ce moment-là.

## Ce que c'est

**Une question est un trou de la fiction que le modèle remplit, à un moment que l'auteur a écrit.** *Comment sa conférence s'est-elle passée ? Est-ce qu'il a retrouvé la meunière ? La reine est-elle en sûreté ?* La réponse devient un **fait de la séance** : elle revient au modèle à chaque tour suivant, elle peut s'afficher à l'apprenant, et une scène suivante la lit.

**C'est ce qui rend une tuile rejouable.** Une mise en scène fixe ne varie pas ; un menu de règles ne varie qu'entre les branches que l'auteur a énumérées. Une question à réponse libre répondue à l'ouverture fait écrire au modèle un fait que personne n'a énuméré, et le **gèle** — il s'engage une fois au lieu de ré-improviser son personnage à chaque tour. La variabilité cesse d'être de la dérive.

**L'app sert la question, le modèle répond tout de suite.** C'est l'inversion qui porte tout le reste : pas un menu de champs vides que le modèle remplit quand il veut, mais une question posée au moment dit, dont la réponse est un champ **obligatoire de ce tour-là**. Le jugement reste cru sur parole ; la **présence** d'une réponse, elle, se contrôle comme n'importe quel champ de retour. On passe de *tout est demandé* à *seul le contenu est demandé*.

**Rien de tout ça n'est mécanique.** Une réponse est de la prose que le prompt relit ; aucune condition ne la lit, aucun patch ne s'y accroche. Ce que l'app doit exécuter passe par les règles, dont le menu est fermé et validé. Faire choisir l'IA hors de la mécanique est le seul endroit où on peut être généreux sans rien payer : ce que personne ne relit ne coûte ni contrat ni garde-fou.

## La forme

Une question porte cinq choses.

- **Sa clé**, qui la nomme dans le retour et dans le résultat.
- **Son texte**, posé au modèle, donc en anglais comme tout ce qu'il lit.
- **Son format** — texte libre, ou liste fermée (le oui/non en est une à deux entrées).
- **Ses moments**, qui sont des déclencheurs. Plusieurs sont permis.
- **Son cran**, qui dit jusqu'où le modèle a le droit d'aller.
- **Son drapeau d'affichage**, qui dit si l'apprenant est mis au courant.

## Les trois crans

**Un escalier avec un plancher, pas trois modes.** Ce que la discussion établit est **toujours prioritaire** ; les crans disent seulement ce qui se passe quand elle ne tranche pas.

1. **Vérité de la discussion** — répondre uniquement d'après ce qui s'est dit. Si rien ne l'établit, la réponse est **je ne sais pas**, qui fait partie du menu au premier cran et à lui seul, que le format soit libre ou fermé.
2. **Extrapolation permise** — à défaut de certitude, conclure de ce qui s'est dit.
3. **Invention permise** — à défaut de quoi extrapoler, décider.

**À écrire comme un escalier et pas comme trois étiquettes**, sinon l'invention écrasera la vérité et une réponse contredira ce qui vient de se passer.

**« Je ne sais pas » plutôt qu'un champ absent**, parce qu'un champ absent est indistinguable d'un modèle qui a oublié et d'un parsing raté. C'est la règle no-fallback d'`universel` : ne jamais masquer un manque par un silence. Aux deux autres crans le modèle ne peut pas manquer de réponse, donc la question ne se pose pas.

## Les moments, et les deux déclencheurs qui manquent

**Un moment est un déclencheur, au même titre que pour une règle.** L'auteur écrit *quand*, l'app sert la question à cet instant. Ça retire au modèle toute discrétion sur le calendrier, et ça ne lui demande rien de plus que ce que le projet lui demande déjà : un déclencheur `Judged` — *quand il a obtenu le rendez-vous* — est le même jugement qu'un modèle laissé libre de remplir un champ quand bon lui semble, à ceci près qu'il est **écrit par l'auteur**, donc lisible dans la définition et consigné au journal. Même confiance, visible au lieu d'implicite.

**Deux sortes manquent, et la liste passe de six à huit** : `à l'ouverture` et `à la fermeture`. Les six existantes **testent quelque chose qui arrive pendant** la séance — une note, une horloge, un numéro de passage, un levier déplacé, un jugement du modèle. Les deux nouvelles **nomment les instants qui la bornent**, et ne testent rien. Elles n'ont pas de paramètre et déduisent leur moment, comme `Clock` déduit l'enregistrement et `Passages` la fermeture d'un passage.

**Elles servent aux règles autant qu'aux questions**, et c'est ce qui les rend légitimes plutôt que taillées pour un besoin :

```
règle "entrée"
  quand : à l'ouverture
  → [ message au modèle, provoque un tour :
      "Tu viens de finir ta conférence. Quelqu'un s'approche. Salue-le." ]

règle "au revoir"
  quand : à la fermeture
  → [ message au modèle, provoque un tour :
      "La conversation se termine. Prends congé en une phrase." ]
```

Une règle *à l'ouverture* n'a pas besoin de se désarmer : l'ouverture n'arrive qu'une fois.

**Pourquoi ce n'était pas écrivable avant.** `activity.md` refuse un déclencheur de début, et son argument tenait à ce que `Definition.opening` **existait** comme champ portant un paquet d'effets : un déclencheur aurait été une seconde façon d'écrire la même chose. Une question n'a pas de champ d'ouverture à elle, donc le déclencheur cesse d'être un doublon et devient le seul chemin — et le champ se **supprime** au lieu de s'implémenter. Pour la fermeture, la raison est plus simple : `Effect.Finish` terminait tout de suite, donc il n'existait aucun instant où accrocher quoi que ce soit.

## La fin en deux temps

**`Finish` marque la séance comme finissante, une vague de clôture tourne, puis c'est fini.** Ce n'est pas une nouveauté, c'est une généralisation : la fin par vies à zéro se comporte **déjà** comme ça — `activity.md` dit qu'elle s'évalue après toutes les vagues, sur l'état stabilisé, ce qui laisse une règle remplir les vies dans le même moment sans que la fin tombe. Aujourd'hui `Effect.Finish` n'a pas cette propriété, donc l'app a deux sortes de fin qui ne se comportent pas pareil.

**La vague de clôture ne peut pas annuler la fin.** C'est une coda, pas un sursis. L'ordre est : vague de fermeture du passage, où une règle peut encore reprendre la fin en remplissant les vies ; puis, si ça finit quand même, vague de clôture, qui ne peut plus qu'ajouter un dernier mot et faire répondre aux questions restées ouvertes. Laisser la coda dé-finir rendrait *est-ce fini ?* indéterminé pendant sa propre vague.

**L'appel de clôture n'existe que si quelque chose l'a demandé** — un message qui provoque un tour, ou des questions ouvertes à balayer. Une séance dont l'auteur n'a écrit ni l'un ni l'autre se termine sans appel. Le dernier mot du personnage n'est donc jamais un comportement de l'app : c'est une règle qu'un auteur écrit.

**Une conversation libre n'atteint jamais la clôture non plus**, n'ayant aucune règle de fin : une question qui l'attend n'y partirait jamais. Une scène sans fin pose donc ses questions **tous les N passages**, ce qui rend un état vivant plutôt qu'un verdict et se lit sur l'écran de reprise (`tiles.md`).

**Une séance abandonnée n'atteint jamais la clôture**, donc ses questions restent sans réponse. C'est sans conséquence : sans fin, il n'y a pas d'issue, donc aucune scène suivante ne lit quoi que ce soit.

## La suite de réponses

**Une question à plusieurs moments porte une suite de réponses, chacune datée de son passage.** Rien ne s'écrase jamais.

**Une réponse tardive succède à la précédente, elle ne la corrige pas.** *La reine était en sûreté au passage 5 et ne l'est plus au 15* est une histoire, pas une erreur rattrapée. Sans cette phrase, quelqu'un implémentera « la dernière gagne, on jette le reste ».

**Un choix d'ouverture est gelé parce qu'il n'a qu'un moment**, et pas parce qu'une règle le verrouille. Aucune pièce ne sert au gel.

**Une scène qui lit une question dit laquelle elle lit** — la dernière réponse, ou la suite entière.

`Outcome.says` devient donc une table `clé de question → suite de réponses datées`, ce que le TODO demandait déjà pour une autre raison.

## Où ça va dans le prompt

**Le fait se pose là où il a été établi.** Une réponse d'ouverture va en partie 2, gelée et permanente avec le reste de ce qui est figé au lancement. Une réponse tombée au passage 12 va **dans le fil, au passage 12**.

Deux raisons indépendantes, et aucune n'est esthétique. Ça **porte la date gratuitement** : un fait posé au douzième passage, mis dans une section de tête, se lirait comme un donné de départ et le modèle jouerait quelqu'un qui l'a toujours su. Et ça **ne casse pas le préfixe caché** : l'historique est stable en tête et croissant en queue, là où une section qui grossit en partie 2 casserait la mise en cache à chaque ajout.

**L'objection à laquelle il faut répondre d'avance** : `ConversationPrompt` argumente que les consignes vont en partie 4 parce que, posées avant l'historique, elles seraient enterrées sous trente tours au moment même où elles doivent gouverner. Ça ne transporte pas ici — une consigne doit être **obéie**, un fait doit seulement être **su**, et être su survit à l'enfouissement.

**L'implémentation est une ligne de `ConversationPrompt.answered()`**, qui reconstruit un tour passé du modèle en ne gardant que `spoken` et `intended`, et jette délibérément les marques, l'écho et la difficulté — un modèle à qui on montre ses vingt derniers verdicts devient cohérent avec eux plutôt qu'avec le tour qu'il lit. Les réponses aux questions sont **exactement ce avec quoi on veut qu'il soit cohérent**, et ce ne sont pas des verdicts sur l'apprenant, donc les garder ne rouvre pas ce risque.

**Dans l'objet de retour, `established` vient avant `spoken`.** Le prompt dit déjà que le modèle écrit en séquence et que chaque champ conditionne le suivant : le personnage parle en sachant ce qui vient d'être établi, jamais l'inverse.

## Ce qui s'affiche

**Un seul drapeau par question, et il commande les deux surfaces.** Levé : l'apprenant est mis au courant quand le fait est établi, et l'écran de reprise le lui rappelle. Baissé : c'est de la plomberie pour le modèle, jamais affichée, ni sur le moment ni après.

**Un seul drapeau parce que les deux ensembles sont forcément les mêmes** : l'écran de reprise ne peut montrer que ce que l'apprenant a **déjà appris**, sinon rouvrir une conversation révèle ce que la jouer n'avait pas révélé. Et l'app ne peut pas savoir ce qu'il a découvert en parlant.

**C'est un vrai choix de mise en scène.** *Le conférencier est vexé, sa salle était à moitié vide* au drapeau levé est un **décor** qu'on donne au départ et qu'on rappelle à la reprise ; au drapeau baissé, c'est une chose qu'on **découvre en parlant**, et l'app ne la rappellera jamais.

**L'objection à écrire, parce qu'on citera le doc contre ça** : `activity.md` refuse qu'une phrase de mise en scène soit produite par le modèle, au motif qu'elle s'affiche à côté d'une affirmation mécanique qu'un texte inventé pourrait contredire. Ici la réponse n'est à côté d'aucune affirmation mécanique — aucun levier n'a bougé, le texte **est** le contenu. L'argument ne transporte pas.

## La forme d'une réponse

**Une réponse s'écrit comme la situation s'écrit** : l'apprenant est **tu**, tout le reste est nommé ou à la troisième personne. *« Sa salle était à moitié vide et il est encore vexé. »*

Trois raisons, et une seule convention pour les trois. Une réponse est relue **par quelqu'un d'autre que celui qui l'a écrite** — le barman de la scène 4 lit ce que le conférencier de la scène 2 a établi, et un *« je suis encore vexé »* n'y a plus de sujet ; même problème dans une distribution à plusieurs. Elle est relue **comme un donné** et non comme une réplique. Et elle **s'affiche à côté de la situation**, dont c'est déjà la convention.

**La question se pose *sur* le personnage, jamais *à* lui.** *« How did the speaker's talk go, and what mood has it left him in? »*, pas *« How did your talk go? »* — la posture est donnée par le texte de l'auteur, qui ne peut pas rater, plutôt que par une consigne de forme, qui peut. Quelques mots dans la partie permanente du prompt disent la convention.

**La langue.** Le texte de la question et la réponse partent au modèle en anglais, toujours. Ce qui s'affiche dépend du format :

- **texte libre** — la réponse s'affiche telle quelle, en anglais. Accepté : on apprend l'anglais, et une ligne d'anglais n'est pas une punition. L'écran devient bilingue par endroits.
- **liste fermée** — les options sont du **texte d'auteur**, donc une table langue → texte comme `title` et `short`. L'anglais est obligatoire : c'est la clé qui part au prompt, que le modèle rend, et sur laquelle la validation se fait par appartenance. L'écran affiche la traduction si elle existe.

## Ce que ça corrige ailleurs

À appliquer dans `activity.md` et dans le code quand ça s'implémente.

- **`Definition.opening` disparaît** au lieu de s'implémenter. Le TODO le porte comme « lue et jamais jouée » ; une ouverture devient une règle, et plusieurs règles peuvent y contribuer là où un champ unique ne porte qu'un paquet.
- **Les six sortes de déclencheur passent à huit**, avec l'argument de la borne à écrire à côté de la liste, que `rules/Rule.kt` déclare fermée.
- **`Effect.Finish` ne termine plus tout de suite**, et la vague de clôture s'écrit dans `rules/Engine.kt` avec sa preuve de terminaison.
- **`Outcome.says` devient une table** clé → suite de réponses datées.
- **« Le modèle répond aux questions en fin de séance » est remplacé** par les moments.
- **Le passage sur le début qui n'est pas un déclencheur garde sa portée mais perd sa raison**, qui était l'existence du champ d'ouverture.

## Ce qui reste ouvert

- **La question franchement rétrospective** — *qu'est-ce qui a coincé ?* — n'a de moment que la fermeture et n'est pas de la fiction. Reste à dire si elle passe par ce mécanisme ou si c'est un bilan à part.
- **Rien ne valide une réponse en texte libre**, même famille qu'`intended`. Les questions bornent la dérive du modèle sans la supprimer.
- **La surface de la notification** n'est pas décidée : une réponse au drapeau levé s'affiche-t-elle dans le même bandeau que la phrase d'une règle, ou ailleurs.
- **Ce que ça pèse** : à la douzième scène d'une histoire, les réponses de onze scènes voyagent. Borné par les questions déclarées, pas toujours assez.
