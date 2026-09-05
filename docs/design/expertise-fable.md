# Expertise — le modèle d'activité et l'UI pixel, avant implémentation

## 1. La direction générale

Elle est bonne, et elle est bonne pour une raison précise : les décisions les plus dures du projet — le modèle synthétisé comme seule norme, l'analyse locale, la marque invariante, l'énoncé comme unité unique — sont déjà implémentées et mesurées. Ce qui reste à construire est de la structure au-dessus d'un socle qui tourne de bout en bout sur l'appareil. C'est l'ordre inverse de la plupart des projets qui meurent, où le méta-système s'écrit avant que la boucle de base marche.

Le risque principal n'est pas la direction, c'est la masse. activity-model.md fait 1374 lignes de design tranché avec quasi zéro code en face : leviers déclarés, règles à vagues, drapeaux, arbre des poids, séries de sensibilité, définitions en fichiers, patchs, consignes à durée, questions inter-scènes. C'est un moteur de jeu à contenu déclaratif — un second projet à l'intérieur du premier. La conception est de qualité inhabituelle, mais elle a été itérée en profondeur (le paragraphe d'historique en tête du doc compte une trentaine de passes) sans aucun retour d'implémentation ni d'usage. Le projet applique ailleurs une discipline stricte « mesure avant conclusion » ; le modèle d'activité est, par nature, une pile de conclusions non mesurées. C'est défendable pour ce qui est structurel (schéma de données, coûteux à changer après), pas pour ce qui dépend du comportement du modèle de langue — et une grosse part du système en dépend (voir §3).

Ce qui sauve la mise : le doc a pris soin que la conversation libre soit elle-même une définition livrée. Le chemin incrémental existe donc — on peut implémenter réglages → feuilles/notes → portes → règles dans cet ordre, chaque étage servant la conversation libre avant que le premier défi existe. Je recommande de s'y tenir et de ne pas écrire le moteur de règles avant que le juge soit éprouvé.

## 2. La cohérence interne des specs

Elle est remarquablement haute. Le doc s'auto-audite (les « quatre choses à ne pas confondre », les pièges notés, les portées d'invariants relues), et je n'ai trouvé aucune contradiction majeure. Quatre frictions réelles, cependant :

### a) Patch = position ou déplacement ?

Les deux, selon la section. « Un patch porte des positions de leviers » (Les règles) — donc absolu. Mais les vies : « un patch la déplace d'un cran » — donc relatif. Ça mord dès qu'on l'écrit : deux règles d'une même vague qui retirent chacune une vie écriraient toutes deux n−1 en absolu (une vie perdue au lieu de deux), et le garde-fou « même clé, valeurs différentes = erreur d'écriture » ne s'applique plus proprement si les patchs sont des déltas. À trancher avant d'écrire le moteur : soit les patchs sont absolus et les décrements passent par un mécanisme dédié, soit ils portent une direction et la règle de conflit se réécrit.

### b) Le brouillage contredit le levier d'affichage.

pixel-ui.md : « le tour de l'IA s'affiche brouillé par défaut, le réglage brouillé/net reste à l'apprenant ». Le catalogue des leviers : tour-ia.affichage, marches « le texte / seulement qui parle / rien », défaut « le texte ». Le brouillé est de fait une quatrième position d'affichage qui n'existe pas dans le levier, avec un décideur différent (préférence apprenant vs levier d'activité). Les deux docs affirment chacun leur défaut. À réconcilier — probablement en faisant du brouillage une marche du levier, ou en disant explicitement que la marche « le texte » a deux rendus dont l'apprenant choisit.

### c) « Une décision du modèle ne produit jamais une marque ni une note » est écrit trop large.

La phrase (section des déclencheurs) est vraie pour le déclencheur « le modèle juge que oui », mais trois feuilles entières — correction, pertinence, suivi — sont des décisions du modèle qui produisent marques et notes, et la difficulté du tour, auto-évaluée par le modèle sur son propre texte, pondère la note de suivi que le même modèle vient de rendre. Doublement auto-référentiel. Ce n'est pas une incohérence de conception (c'est assumé ailleurs), mais la phrase telle qu'écrite sera citée un jour hors de sa portée — exactement le piège que dev_base nomme.

### d) L'argument de latence de l'appel unique est daté.

« Le coût en latence est borné : le modèle achève son objet 0,16 s après sa première phrase » — mesuré sur le contrat actuel (réponse + intended + faulty). Le contrat spécifié ajoute trois marquages par empans, la difficulté, l'écho de reprise, le choix de menu. Les empans marqués sont estimés à « la moitié » de l'historique en taille ; en sortie, séquentielle par nature, c'est directement de la latence. Or la latence est le premier défaut mesuré du projet (11 s de médiane). Le 0,16 s ne borne plus rien ; à re-mesurer dès que le contrat enrichi existe, avant d'empiler dessus.

## 3. Le point aveugle central : l'asymétrie des garanties entre les deux moitiés du marquage

C'est le point le plus important de cette expertise.

Le côté son a des garanties fortes, mesurées : déterminisme vérifié sur l'appareil, biais annulé par l'étalon, seuils éprouvés au banc, fausses alertes comptées. Le côté jugé (correction, pertinence, suivi, bafouillage) n'a structurellement aucune de ces garanties : un juge LLM est non déterministe, dépend du fournisseur, et sa stabilité inter-appels n'est mesurée nulle part. « La même faute doit produire la même marque à n'importe quel moment » est un invariant que l'architecture ne peut pas tenir pour les marquages jugés — la même phrase resoumise peut rendre des empans différents. Et l'écran affiche les deux moitiés avec la même autorité visuelle.

Ce n'est pas un détail, parce que tout l'étage supérieur s'assoit dessus : la porte des mots, le blocage (avance.mots = attend rend une fausse marque très coûteuse — le gros bouton indisponible, des tentatives à brûler sur un verdict faux), les vies, le déverrouillage de niveaux, l'arcade. Toute la pyramide des notes repose sur un juge dont la variance n'a jamais été mesurée. Le banc grammatical du chantier 2 n'est donc pas « un banc parmi d'autres » : c'est le prérequis de tout le modèle d'activité au-delà de la conversation libre, et il devrait mesurer non seulement la justesse du juge mais sa répétabilité (même phrase, même contexte, dix appels — combien de verdicts distincts ?).

S'y ajoute le problème que le doc nomme lui-même (le cas walk/walked) mais dont la portée est plus large que notée : le juge juge sa propre reconstruction. intended est écrit par le même appel qui pose les empans ; un modèle qui répare en reconstruisant a effacé la faute avant de la juger, et rien en aval ne peut le voir. La fuite est mesurée une fois (deepseek-v4-flash a réparé, pro non, un cas chacun). Tant que ce n'est pas borné au banc, chaque note de correction est conditionnelle à une fidélité non vérifiée.

## 4. Problèmes en puissance non identifiés (ou identifiés à moitié)

- L'éviction du cache casse des références stockées. Le plafond de 500 Mo avec éviction LRU est décidé ; or Utterance.model (colonne en base) pointe un fichier de cache/renders/. Une éviction rend le chemin pendouillant : « écouter le modèle » sur un vieux tour échoue en silence, et une re-synthèse n'est pas le même audio que l'étalon de la mesure (version Replicate non épinglée, noté ailleurs). La couture entre « le cache est jetable » et « la base nomme des fichiers du cache » n'est traitée nulle part — c'est un cas typique pour la passe de garde-fous du point 11 du TODO.
- Dénominateurs vides. Un tour entièrement bafouillé (zéro mot retenu) : correction et pertinence n'ont pas d'éléments (feuille absente, ça c'est prévu), mais le débit divise par le temps de parole des mots retenus — zéro. La continuité compare à un modèle qui n'existe pas (rien à synthétiser). Le cas « tout est remplissage » doit être spécifié comme le cas « rien entendu » l'a été.
- Tour interrompu × correction. Un tour tronqué ne se complète jamais — bien. Mais que fait le juge d'une phrase coupée ? « I went to the » est mal formé par troncature, pas par faute. Rien ne dit si l'interruption suspend le marquage de correction, le borne, ou l'ignore. La feuille du tour interrompu existe ; son interaction avec les empans n'est pas écrite.
- La validation des définitions n'existe dans aucune liste. Le doc répète « ça se voit à l'écriture, sans exécuter » (conflits de clés, phrases de mise en scène manquantes, séries à bords impossibles, direction narratif/mécanique contradictoire) — mais qui regarde ? Aucun validateur, linter ou écran d'auteur n'est spécifié, et « Ce qui reste à spécifier » ne le liste pas. Sans lui, toutes ces vérifications « à l'écriture » se feront à l'exécution, sur un apprenant.
- Le recalcul du dérivé dépend de la version du moteur. L'état effectif se recalcule depuis réglages + règles + journal. C'est de l'event-sourcing : rejouer le journal avec une sémantique de règles qui a changé (une release plus tard) rend un autre état. Le doc voit ce problème pour la graine de tirage et pas pour lui-même. La règle du projet existe déjà — « tout ce qui est stocké porte la version de ce qui l'a produit » — il suffit de l'appliquer à l'exécution (version du moteur de règles sur la ligne).
- « Une note ne se lit jamais sans la combinaison qui l'a produite » n'a pas d'écran. C'est le mantra qui absout la moitié des choix du doc (notes non comparables entre fournisseurs, effets cumulés des curseurs, poids par défi), et aucune spec ne dit où l'apprenant voit cette combinaison. L'écran d'avant-partie est spécifié ; l'écran de résultat, celui qui devrait porter la combinaison à côté de la note, est dans les ouverts (« le score ») sans que ce besoin y soit nommé.

## 5. Cas limites à spécifier ou surveiller

En plus des cas ci-dessus, en vrac mais tous réels :

- Consignes multiples qui interagissent (« parle au passé » + « registre soutenu ») : le juge les reçoit toutes — la combinatoire n'est éprouvable par aucun banc, et la spec dit seulement qu'elles « ne peuvent qu'endurcir » individuellement.
- L'apprenant qui parle français (ou n'importe quoi) : intended reconstruit quoi ? Tout ne se dit pas ? Le cas borde toutes les feuilles à la fois.
- Reprise d'une conversation des semaines plus tard : les consignes en vigueur, les durées restantes, les leviers patchés se recalculent du journal — sur une activité de bloc c'est tout l'état d'une partie ; le premier test de reprise devrait être une séance à mi-boss.
- La note de passage = dernière tentative, avec la proposition non tranchée (fluidité/pertinence sur la première) : à trancher avant que le stockage des tentatives fige quoi que ce soit.
- Deux avance + deux compteurs de tentatives + deux portes : la matrice complète des combinaisons (attend/poursuit × mots/son × épuisements) est déroulée pour le cas nominal dans « Le déroulé d'un passage », mais pas pour un tour qui déclenche les deux portes à la fois (reformulation faite, puis redite due sur la reformulation : les compteurs se croisent).
- Le facteur d'échelle entier sur écrans étroits : 21 colonnes sur un 720 — le tour marqué et la ligne d'étiquette (déjà notée juste sur 16 colonnes) sont à vérifier au banc à cette largeur, pas seulement à 32.

## 6. Ce qui pourrait limiter la créativité du système

Le système est expressif — la fausse mort, les embranchements par questions fermées, les récompenses, la surcharge par interrupteurs montrent qu'il couvre déjà du scénario non trivial. Les limites que je vois, par ordre d'importance :

- Pas de compteur généralisé. On compte via un levier à nombre patché (les vies), mais l'ambiguïté patch/position (§2a) rend ça fragile, et un drapeau ne se compte pas — « la troisième fois qu'il mentionne le dragon » est inécrivable autrement qu'en chaîne de drapeaux R1→R2→R3, verbeux mais possible. Acceptable si l'ambiguïté du patch est tranchée côté déplacement.
- Aucun déclencheur temporel hors enregistrement. Les horloges lisibles sont celles du tour ; « après deux minutes de conversation » ou « si l'apprenant met plus de dix secondes à répondre trois fois de suite » (le second se ferait par condition + drapeaux) n'ont pas de sorte. Pour un système par tours c'est cohérent — à noter comme choix, pas comme oubli.
- Les poids constants interdisent le défi qui s'élargit, et le doc l'assume avec le contournement par conditions. Le contournement change la nature de l'exercice (bloquant au lieu de noté) — c'est le compromis le plus visible pour un futur auteur, et il est bien documenté.
- Le texte des définitions est statique. Pas d'interpolation, pas de variation aléatoire dans les phrases de mise en scène — la rejouabilité d'une scène repose entièrement sur l'improvisation du modèle autour de textes fixes. Probablement le bon choix v1 ; à garder en tête pour l'arcade rejouée « sans fin ».
- Une consigne ne peut qu'endurcir, donc « conversation détendue où le remplissage ne compte pas » ne s'écrit pas en consigne — mais s'écrit en poids à zéro sur la feuille. Le système couvre le cas ; c'est la doc d'auteur qui devra dire par où.

Globalement : les bornes sont presque toutes des bornes choisies (liste fermée de sortes, catalogue déclaré), et le doc a raison que c'est ce qui garde une définition lisible sans exécution. Je ne vois pas de mur qui invaliderait une famille entière d'activités.

## 7. UI — les questions à poser maintenant

- Le daltonisme n'est mentionné nulle part, et c'est le trou le plus sérieux des deux docs UI. La forme porte l'échelle — très bien, c'est déjà la moitié du travail — mais la couleur porte le côté et la distance, et le système repose sur des paires rouge/vert (crochets à côté vs juste, pastille de suivi) et sur une rampe ambre→rouge dont les crans voisins sont « peu contrastés » par choix esthétique. Une passe deutéranopie/protanopie au banc HTML coûterait une heure et pourrait déplacer le vert de la palette pendant que c'est gratuit.
- La densité du tour marqué. La maquette le montre : mélodie + teintes + filets + vaguelettes + crochets sur presque chaque groupe + points de pause + pastille + chevrons. Chaque canal est justifié isolément ; leur somme sur un tour d'apprenant réel (où « la majorité des mots portent quelque chose », mesuré) est l'écran le plus chargé que je connaisse dans une app de langue. La question à trancher tôt : y a-t-il un ordre de révélation (les marques apparaissent-elles d'un coup à la fin de l'analyse, ou canal par canal ?), et l'apprenant peut-il éteindre l'affichage d'un canal (pas la mesure) ? Rien ne le dit.
- Ce que l'écran fait pendant les 11 secondes. Phase existe (Hearing/Thinking/Speaking) mais rien n'est spécifié du temps d'attente, qui est aujourd'hui le défaut numéro un ressenti. Et les marques arrivent après que la réponse a été jouée : le moment où l'attention de l'apprenant rencontre les marques (il écoute déjà la suite) est un problème de chorégraphie que ni pixel-ui.md ni le modèle d'activité ne traitent — le doc dit lui-même que le flux d'écran s'écrira avec le déroulé d'un module, mais la conversation libre, elle, existe déjà.
- L'accessibilité lecteur d'écran. MarkedTurn est un Canvas ; toute la sémantique du marquage est invisible à TalkBack. Pour une app F-Droid destinée à autrui à terme, décider explicitement « hors périmètre v1 » vaudrait mieux qu'un silence.
- Le point §2b (brouillage vs levier) est aussi une question d'UI, déjà dit.

## 8. La qualité du code

Elle est haute, et d'un style singulier qui fonctionne : les doc-comments portent le pourquoi de design avec renvoi au doc, les noms sont nets (Utterance, readings, modelOf, repeats), les fichiers sont petits, les coutures sont propres (interface Analysis sans réseau dans son contrat, fournisseurs par maillon, doublure release en source set séparé), les échecs gardent l'enregistrement et se tracent au lieu de se taire. La migration Room est faite à la main correctement (rebuild pour SQLite 3.18). Le choix « pas de ViewModel/DI tant que rien ne le demande » est tenu sans dégât visible.

Trois réserves, toutes tournées vers ce qui arrive :

- L'adressage par index de liste (at: Int partout dans TurnPipeline, size - 2 après double append) est fragile exactement de la façon que le projet a déjà nommée pour la persistance (« une place ne survit pas à l'écriture »). En mémoire ça tient tant qu'un seul écrivain mute l'état ; or rien ne garantit l'écrivain unique — submit, redo, open sont des suspend qui font des read-copy-write sur le MutableStateFlow sans mutex. Deux gestes rapprochés (un redo pendant qu'un submit attend le réseau) peuvent perdre une mise à jour ou décaler un index. Avant que les règles et les tentatives complexifient l'état, passer l'adressage sur les identités et sérialiser les mutations (un Mutex, ou un seul flux d'événements) est le refactor le moins cher aujourd'hui et le plus cher dans six mois.
- TurnPipeline (575 lignes) est déjà l'endroit où tout converge — état, orchestration, persistance, traces. L'arrivée des réglages-leviers, des deux portes et du moteur de règles va le faire éclater ; le moteur de règles mérite son propre module dès le départ, d'autant que c'est de la logique pure, testable en JVM sans appareil — et la résolution par vagues est précisément le genre de chose à prouver par des tests dans le dépôt plutôt qu'au banc (le projet teste peu en dehors des bancs, cohérent avec sa philosophie, mais ici c'est un interpréteur).
- Settings(Map<Aptitude, Float>) est le connu-faux que le doc et le code signalent tous deux — rien à redire, sinon que c'est bien la première étape et qu'elle conditionne le reste.

Détail de doc : le paragraphe d'historique en tête d'activity-model.md (une phrase de 30 lignes) contredit deux règles maison — « les commits sont la carte » et « écrire au plus juste ». Il peut se réduire à sa dernière phrase sans rien perdre.

## 9. Ce que je ferais, dans l'ordre

1. Trancher les trois frictions de spec avant toute ligne : patch position/déplacement, brouillage vs levier d'affichage, et la portée exacte de « une décision du modèle ne produit jamais une note ».
2. Écrire le banc du juge en priorité absolue — justesse et répétabilité des trois marquages, plus la fidélité d'intended — parce que tout l'étage notes/portes/règles est un pari sur lui. S'il est instable, mieux vaut le savoir avant le moteur de règles qu'après.
3. Implémenter dans l'ordre déjà prévu (réglages → empans → feuilles/notes), en faisant de la conversation libre le banc d'essai de chaque étage, et en sécurisant l'écrivain unique de TurnPipeline au passage de l'étape réglages.
4. Ajouter à « Ce qui reste à spécifier » : le validateur de définitions, le cas du tour sans mots retenus, tour interrompu × correction, la couture cache/base, l'écran qui porte la combinaison d'une note, et la passe daltonisme au banc UI.

L'ensemble est un projet en état inhabituel : le plus dur est fait et mesuré, le plus gros est écrit et cohérent, et le vrai risque est concentré en un seul endroit — la fiabilité du juge — que le plan de travail actuel n'attaque qu'en chantier 2. C'est la seule inversion de priorité que je défendrais fermement.
