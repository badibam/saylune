# Le tour : une suite d'énoncés, et ce qui suit un tour

Doc transitoire, ouvert le 2026-09-10. Il porte quatre choses qui se sont révélées être une seule : ce qu'un tour de l'IA contient, ce qui va au fil et ce qui va en notification, ce que l'armement automatique suit, et ce qu'une reprise rejoue. Il s'élague quand le code est en place et que `activity.md`, `ui.md` et `reference.md` ont repris ce qui leur revient. Les voix de ceux qui parlent sont dans `voices.md`.

**Pourquoi les quatre tiennent ensemble.** On a d'abord cru à quatre chantiers séparés. Ils partagent un instant : *la fin de la parole de l'app*. C'est là que la notification s'affiche, c'est là que le micro s'arme, c'est là qu'une séquence se termine, et c'est ce qu'une reprise doit recréer. Les traiter séparément recrée le trou qu'on vient de trouver.

## La coupure : le fil et la notification

Deux matières, deux surfaces, et on les confondait.

**La notification est un reçu sur la machinerie.** *« Cinq secondes par tour désormais »*, plus la phrase de fiction qui l'habille, au même endroit et au même instant. C'est le rythme d'un jeu : il se passe quelque chose dans l'histoire, puis on voit ce que ça change côté règles. Elle vient donc **après l'audio, toujours** — c'est-à-dire après la séquence entière.

**La didascalie est de la matière de fiction.** Elle va **dans le fil**, elle peut être dite, et elle se place avant ou après la réplique d'un personnage.

**Le drapeau `before` quitte la notification.** Il était sur `Staging` et le doc lui donnait un sens de place par rapport à la réplique, mais le code ne s'en servait que pour ordonner les lignes *à l'intérieur d'un seul bandeau*, lequel ne s'affiche qu'à un instant. Le « avant la réplique » n'existait donc pas. Ce n'était pas un bug d'implémentation : ce qu'on voulait placer avant n'était pas une notification, c'était une didascalie. Le drapeau devient une propriété de l'ordre de la séquence, et le trou se referme en changeant d'objet plutôt qu'en ajoutant un instant d'affichage.

## Un tour est une suite d'énoncés

Aujourd'hui le contrat rend `spoken` — une réplique — plus `echo` en option, qui n'est pas un second énoncé mais l'ouverture du même. Tous les tours sortent sous une seule identité.

**Le modèle rend une liste.** Chaque énoncé porte son **type** — parole ou didascalie — et sa **clé de personnage**. Ils sont joués à la suite, chacun avec sa voix, une pause d'une seconde entre deux.

**Aucun ordre n'est imposé.** Ce n'est pas *didascalie puis parole* : ce peut être une parole du personnage 1 puis une parole du personnage 2, une didascalie entre deux répliques, ou une réplique seule. La limitation n'est pas technique, elle est pratique — ne pas se retrouver avec quinze énoncés.

**L'écho s'accroche à la première *parole*, jamais à une didascalie.** C'est la seule vraie contrainte sur l'ordre libre. L'écho se lit d'un seul souffle avec ce qui suit ; posé sur une ligne de narrateur, il devient une remarque du narrateur sur la grammaire de l'apprenant, ce que le projet refuse partout. Une séquence peut donc commencer par une didascalie, mais l'écho saute jusqu'à la première réplique.

**Un plafond, généreux et déclaré.** Pas de restriction technique, mais un modèle qui part en vrille rend douze énoncés, donc douze synthèses et une minute de lecture. Ce qui se sent n'est pas la facture : c'est **l'attente avant que l'apprenant puisse parler**. Le projet borne partout — 45 caractères pour une remarque, 30 s pour un tour — et une borne qui porte sa raison vaut mieux qu'une dérive découverte à l'usage.

**Deux nombres posés à la main** : le plafond et la pause d'une seconde. Dits comme tels, à revoir à l'oreille.

**À l'écran** : chaque parole garde son cadre ; les didascalies ressortent autrement — elles ne sont pas dites *à* l'apprenant.

**Le narrateur est un membre de la distribution à clé réservée**, comme l'apprenant. Sa voix est un réglage que l'utilisateur choisit et qu'une tuile peut remplacer, ce qui en fait un troisième régime à côté de l'étalon et du tirage (`voices.md`).

## La panne : le tour entier tombe

La chaîne lâche à l'énoncé 3 sur 5. **On annule le tour entier.**

**Pourquoi pas garder ce qui a été dit.** Une séquence n'est pas une liste de phrases indépendantes : avec la seule didascalie d'ouverture lue, il n'y a pas d'histoire à suivre. Un demi-tour est moins lisible qu'un tour qui n'a pas eu lieu.

**Pourquoi pas tout synthétiser avant de jouer.** L'idée rendrait l'annulation propre — la panne tomberait avant le premier son, donc rien à retirer de l'oreille. Elle est **refusée** : elle pousse toute la latence des synthèses avant le premier mot, et l'attente est précisément ce qu'on ne veut pas payer.

**Donc le prix est assumé et nommé** : l'apprenant a entendu le début d'un tour qui n'aura pas lieu. Il réentendra un début différent au renvoi. C'est le moindre des deux maux, pas une élégance.

**Ce qui se passe** est ce que fait n'importe quel maillon qui lâche : la réponse est jetée entière, **l'enregistrement de l'apprenant est gardé**, on renvoie. Et surtout : **ça se dit**. Ce qui est inacceptable aujourd'hui n'est pas l'interruption, c'est le silence qui l'accompagne.

## La notification, et son minuteur

**Après l'audio**, donc après la séquence entière.

**Elle se congédie au doigt, ou à l'horloge.** L'invariant écrit était *« dismissed by the finger and not by a clock : a notice nobody has seen is a change the learner cannot reconstruct »*. Il a été écrit contre une notification qui **s'évanouit sans être vue** — ce que le minuteur ne fait pas, dès lors que le décompte est visible. La portée est respectée ; ça vaut d'être noté ici, sinon la question se repose dans six mois.

**Un plancher plus un débit**, et non une durée strictement proportionnelle : une notification de quatre mots à durée proportionnelle clignote. Deux nombres de plus posés à la main.

**Le minuteur n'existe qu'en armement automatique**, et **le décompte est le délai avant que le micro s'arme**. Ce n'est pas un second compteur à côté : c'est le même, montré.

## L'armement suit un événement, pas une forme

C'est le défaut central, et il explique deux bugs qu'on prenait pour trois.

Aujourd'hui l'armement suit `armsOn`, qui vaut *le dernier énoncé du fil, s'il n'est pas de l'apprenant*. C'est une **forme**. Ce qu'il faut lire est un **événement** : *une réponse vient de finir*.

La différence ne se voit pas en conversation courante et se voit partout ailleurs :

- **à la reprise**, la forme est vraie et l'événement est faux — le fil finit bien sur le personnage, mais cette réponse-là a fini hier. Le micro s'armerait à l'apparition de l'écran, sur quelqu'un qui relit son fil ;
- **après un crash**, `armedOn` ne survit pas, la forme redevient vraie et le même armement parasite repart ;
- **avec une séquence**, si les énoncés arrivent au fil de leur synthèse, la forme est vraie dès le premier : le micro s'ouvrirait pendant que le narrateur parle encore.

**L'événement est la fin de la séquence**, jamais l'arrivée d'un énoncé. Soit la séquence entre dans le fil d'un bloc, soit l'événement suit la fin de lecture — pas les deux à moitié.

**Et l'événement ferme le passage.** L'armement remplace l'appui sur le gros bouton aux deux positions automatiques ; or cet appui fait **deux** choses — fermer le passage précédent et ouvrir le micro — et l'armement n'en rejoue qu'une. Conséquence mesurée : aux deux positions armées, `Moment.PassageClosed` ne se déclenche **jamais**, donc ni patch, ni fin de séance par règle, ni question posée à ce moment. Une tuile livrée en souffre déjà en silence — `the-last-train` déclare une question tous les cinq passages qui n'est jamais posée hors de la capture au doigt. Si l'événement est bien *le tour de l'apprenant s'ouvre*, il fait les deux.

**Piège à éviter** : le moment de la fin de tentative court **deux fois** par tour, et `Engine.resolve` tient son ensemble `fired` par résolution et non par moment. L'événement ne doit pas être émis deux fois (voir `TODO.md`).

## La reprise

Aujourd'hui, rouvrir un fil déclenche **un appel au modèle** : le personnage invente une ligne qui reprend la conversation, et l'armement suit cette ligne. Ce n'est pas ce qu'on veut, et ça masquait le défaut ci-dessus plutôt que de le corriger — la provocation met la phase à *Thinking*, l'armement sort en occupé, et personne ne voit que la forme était fausse.

**On rejoue le dernier tour de l'IA, en entier.** En entier parce qu'un tour n'est plus une réplique : ce peut être une parole du personnage 1 suivie d'une parole du personnage 2, et n'en rejouer que la dernière perdrait l'échange. Rien n'est inventé, rien ne part au modèle : le texte est au dossier, seule la synthèse retravaille. Les réponses du modèle n'ayant pas d'audio stocké, rejouer veut dire resynthétiser.

**Puis l'événement tombe, et l'armement suit** — comme après n'importe quelle réponse.

Trois gestes, donc : neutraliser l'armement périmé à l'ouverture, rejouer, émettre l'événement à la fin de la lecture. L'appel de reprise disparaît.

## La fragilité, et ce qu'on en fait

Le constat qui a ouvert cette section : *si la chaîne lâche pendant que l'IA parle, ça interrompt le tour*, et le système casse à beaucoup d'endroits.

**Il y a déjà une machine à états** — `Phase { Idle, Hearing, Thinking, Speaking, Measuring }`. Le défaut n'est pas son absence : c'est qu'elle est **implicite et non totale**. Dix-neuf affectations de phase dispersées, quarante-deux mutations d'état, aucune table qui dise quelle transition est légale, et un retour à `Idle` qui est une **phrase en fin de fonction** plutôt qu'une **garantie**.

```
_state.update { phase = Speaking }
Playback.play(synthesis.speak(...))   // la chaîne lâche ici
_state.update { phase = Idle }        // ne s'exécute jamais
```

Tout l'écran lit `busy = phase != Idle`. La conversation est alors gelée pour de bon, sans un mot à l'écran. Trois chemins ont cette forme : `replay()`, `hear()`, et la continuation retenue de `close()`. Le bon motif existe pourtant déjà dans `provokeIfAsked()` — le `catch` couvre la lecture et le retour à `Idle` est hors du `try` — mais il ne rattrape que `ChainFailure`.

**Ce qu'on fait** : rendre le retour à `Idle` **structurel**, un `withPhase(Speaking) { … }` en `try`/`finally`. Aucun chemin ne peut plus laisser une phase coincée. C'est mécanique, ça tue toute la classe des conversations gelées, et c'est dans le style du projet — un invariant écrit en prose devient une garantie de structure, sans vocabulaire neuf.

**Ce qu'on ne fait pas** : une machine à états à l'échelle de l'app. Ce projet tient ses invariants en prose documentée, pas en types ; une grosse hiérarchie scellée se battrait contre ce style pour un coût sans rapport. Si les champs qui se contredisent — `pending`, `failure`, `held`, `unjudged` — veulent un jour un type somme, ce sera à l'échelle du **tour**.

**L'ordre des travaux en découle** : `withPhase` **d'abord**, les séquences ensuite. Une séquence multiplie par cinq les occasions de lâcher en cours de route, chacune laissant aujourd'hui la phase coincée. Bâtir les séquences sur le socle actuel, c'est bâtir sur du sable.

## Ce qui reste ouvert

- **Couper une séquence : non, pas pour l'instant.** L'invariant *« un geste ne coupe jamais la parole de l'app »* tient tel quel. La question se rouvrira si une séquence longue devient pénible à l'usage ; la réponse préparée serait de sauter la séquence **entière** et jamais un énoncé sur cinq, sinon on retombe dans le cas que l'invariant interdit.
- **`standingReply` est au singulier.** L'historique envoyé au modèle met une réponse par passage ; avec une séquence, le modèle perd sa propre narration de sa mémoire dès le passage suivant. C'est le vrai impact structurel du changement, plus que le contrat.
- **Le contrat touche cinq fichiers de fournisseur** là où `spoken` + `echo` deviennent une liste.
- **La fréquence des didascalies** se dit dans la fiche, en texte libre plus un préréglage (*au plus une par tour*, *une tous les trois tours*, *de temps en temps*). C'est une demande interprétée librement, pas une garantie : rien ne doit s'y adosser — aucune condition qui la lise, aucun banc qui l'éprouve, aucune phrase de l'app qui la présente à l'apprenant comme un fait. Un cran presque gratuit la rend tenable : donner au modèle **un fait** plutôt qu'une consigne seule — *« trois tours depuis la dernière didascalie »* — le contrat le faisant déjà pour le numéro de passage. Il compte très mal ses propres tours et très bien un nombre qu'on lui tend.
- **Rien n'exerce encore le bandeau.** Aucune tuile livrée ne pose de patch, donc ni la notification ni son minuteur n'ont jamais eu de quoi s'afficher. C'est ce que l'enrichissement des tuiles refermera — et c'est là seulement que le minuteur pourra se régler à l'œil. En attendant on reste en conversation libre, donc sans règles, ce qui laisse quand même de la marge pour éprouver les séquences et le narrateur.
