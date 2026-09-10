# Le tour : une suite d'énoncés, et ce qui suit un tour

Doc transitoire, ouvert le 2026-09-10. Il portait quatre choses qui se sont révélées être une seule : ce qu'un tour de l'IA contient, ce qui va au fil et ce qui va en notification, ce que l'armement automatique suit, et ce qu'une reprise rejoue. **La moitié est en place** ; ce qui est fait a été élagué d'ici et vit dans les commits, qui sont la carte. Reste ce qui suit. Les voix de ceux qui parlent sont dans `voices.md` (pas implémenté).

**Pourquoi les morceaux tiennent ensemble.** Ils partagent un instant : *la fin de la parole de l'app*. C'est là que la notification s'affiche, c'est là que le micro s'arme, c'est là qu'une séquence se termine, et c'est ce qu'une reprise doit recréer.

## Ce qui est en place

Six commits, du 2026-09-10, et rien ici n'est à relire pour comprendre le code — le code se lit.

- **Le retour à `Idle` est structurel**, un `withPhase(phase) { … }` en `try`/`finally`. Dix-neuf affectations de phase dispersées sont tombées à cinq, et le seul `Idle` du fichier est celui du `finally`. Ça tue toute la classe des conversations gelées.
- **Ce qui annulait l'appelant est trouvé**, et sans instrument : l'app s'annulait elle-même. Le juge et la voix étaient deux `async` dans un `coroutineScope`, donc un juge qui lâche annulait celui qui parlait. La prise est passée *dans* l'`async` — une panne rendue comme valeur n'emporte pas ses voisins. Les deux symptômes observés (la parole coupée net, puis plus rien) étaient un seul incident.
- **L'armement suit un événement**, écrit à un seul instant : l'app a fini de parler. Rien ne le dérive, donc un écran qui s'ouvre, un plantage et une reprise le trouvent vide. **Et l'événement ferme le passage** — l'appui qu'il remplace faisait deux choses et l'armement n'en rejouait qu'une, donc `Moment.PassageClosed` ne tombait jamais aux deux positions armées. Ce qui a rendu un second défaut atteignable : un passage reste le passage ouvert tant que l'apprenant n'a pas reparlé, donc deux fermetures sans rien dire entre les deux tombaient dessus deux fois. La fermeture est une fois par passage.
- **La reprise rejoue le dernier tour de l'IA, en entier**, au lieu d'appeler le modèle. Rien n'est inventé, seule la synthèse retravaille. `Provoked` perd son second cas et le prompt perd `RESUMED`.
- **Un tour est une suite d'énoncés**, chacun avec son type — parole ou didascalie — et sa clé de personnage, le narrateur étant un membre réservé de la distribution. L'écho ouvre la première **parole** et jamais une didascalie. **Une seule prise du haut-parleur**, du premier mot au dernier. Deux nombres posés à la main et dits comme tels : six énoncés au plus, une seconde de silence entre deux. Le plafond **refuse au lieu de tronquer**.
- **L'historique porte le tour entier**, sans quoi le modèle perdait sa propre narration de sa mémoire dès le passage suivant.

Et le banc a été remis debout au passage : rien du dossier de test ne compilait, trois changements l'ayant dépassé. 268 tests.

## Ce qui reste

### L'affichage d'une didascalie

**À l'écran** : chaque parole garde son cadre ; les didascalies ressortent autrement — elles ne sont pas dites *à* l'apprenant. Rien n'est fait : le fil dessine tout énoncé de l'IA de la même façon. Le champ qui les distingue est là (`Utterance.kind`), donc c'est un travail d'`ui.md` et rien d'autre.

### La coupure : le fil et la notification

Deux matières, deux surfaces, et on les confondait.

**La notification est un reçu sur la machinerie.** *« Cinq secondes par tour désormais »*, plus la phrase de fiction qui l'habille, au même endroit et au même instant. C'est le rythme d'un jeu : il se passe quelque chose dans l'histoire, puis on voit ce que ça change côté règles. Elle vient donc **après l'audio, toujours** — c'est-à-dire après la séquence entière.

**La didascalie est de la matière de fiction.** Elle va **dans le fil**, elle peut être dite, et elle se place avant ou après la réplique d'un personnage.

**Le drapeau `before` quitte la notification.** Il est sur `Staging` et le doc lui donnait un sens de place par rapport à la réplique, mais le code ne s'en sert que pour ordonner les lignes *à l'intérieur d'un seul bandeau*, lequel ne s'affiche qu'à un instant. Le « avant la réplique » n'existe donc pas. Ce n'est pas un bug d'implémentation : ce qu'on veut placer avant n'est pas une notification, c'est une didascalie. Le drapeau devient une propriété de l'ordre de la séquence — laquelle existe maintenant — et le trou se referme en changeant d'objet plutôt qu'en ajoutant un instant d'affichage.

### La notification, et son minuteur

**Après l'audio**, donc après la séquence entière.

**Elle se congédie au doigt, ou à l'horloge.** L'invariant écrit était *« dismissed by the finger and not by a clock : a notice nobody has seen is a change the learner cannot reconstruct »*. Il a été écrit contre une notification qui **s'évanouit sans être vue** — ce que le minuteur ne fait pas, dès lors que le décompte est visible. La portée est respectée ; ça vaut d'être noté ici, sinon la question se repose dans six mois.

**Un plancher plus un débit**, et non une durée strictement proportionnelle : une notification de quatre mots à durée proportionnelle clignote. Deux nombres de plus posés à la main.

**Le minuteur n'existe qu'en armement automatique**, et **le décompte est le délai avant que le micro s'arme**. Ce n'est pas un second compteur à côté : c'est le même, montré. Le délai existe (le levier de préparation) ; ce qui manque est de le montrer.

### Le piège à ne pas se prendre

Le moment de la fin de tentative court **deux fois** par tour, et `Engine.resolve` tient son ensemble `fired` par résolution et non par moment. L'événement ne doit pas être émis deux fois (voir `TODO.md`). La fermeture du passage, elle, est déjà bornée à une fois par passage.

## Ce qui reste ouvert

- **Couper une séquence : non, pas pour l'instant.** L'invariant *« un geste ne coupe jamais la parole de l'app »* tient tel quel — étant entendu qu'il ne dit rien de l'app se coupant elle-même. La question se rouvrira si une séquence longue devient pénible à l'usage ; la réponse préparée serait de sauter la séquence **entière** et jamais un énoncé sur cinq, sinon on retombe dans le cas que l'invariant interdit.
- **Le contrat ne touchait pas cinq fichiers de fournisseur.** Ce doc l'annonçait ; c'est faux, et mesuré en le faisant : les prompts et les lecteurs vivent en un seul endroit, aucun fournisseur ne déclare de schéma, et le changement a touché `ConversationPrompt.kt` et `ReplyReader.kt` et rien d'autre côté fournisseurs.
- **La fréquence des didascalies** se dit dans la fiche, en texte libre plus un préréglage (*au plus une par tour*, *une tous les trois tours*, *de temps en temps*). C'est une demande interprétée librement, pas une garantie : rien ne doit s'y adosser — aucune condition qui la lise, aucun banc qui l'éprouve, aucune phrase de l'app qui la présente à l'apprenant comme un fait. Un cran presque gratuit la rend tenable : donner au modèle **un fait** plutôt qu'une consigne seule — *« trois tours depuis la dernière didascalie »* — le contrat le faisant déjà pour le numéro de passage. Il compte très mal ses propres tours et très bien un nombre qu'on lui tend.
- **Le plafond et la pause n'ont jamais été entendus.** Six et une seconde sont posés à la main, et rien de livré ne fait encore parler deux personnages dans un tour.
- **Rien n'exerce encore le bandeau.** Aucune tuile livrée ne pose de patch, donc ni la notification ni son minuteur n'ont jamais eu de quoi s'afficher. C'est ce que l'enrichissement des tuiles refermera — et c'est là seulement que le minuteur pourra se régler à l'œil.
- **Le narrateur parle de la voix de tout le monde.** Sa clé est réservée et son régime de voix — un réglage que l'utilisateur choisit, qu'une tuile peut remplacer — attend `voices.md`, qui n'est pas implémenté.
