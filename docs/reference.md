# speakup — référence

App Android de pratique de l'anglais oral : conversation libre avec une IA, jamais interrompue, doublée d'un travail de la grammaire et de la prononciation à la demande. Ce document est le point d'entrée ; il porte les décisions **propres au projet**, celles qu'aucune facette de sagesse ne couvre. Le craft transverse vit dans les modules abonnés (cf. `manifest.md`).

Docs complémentaires, à ouvrir au besoin : `design/speechace.md` (le moteur d'analyse retenu — ce qu'il rend, ses limites mesurées), `design/pronunciation-test-set.md` (le jeu d'essai et la méthode de mesure), `design/azure-speech.md` (le candidat écarté, gardé comme base de comparaison), `design/conversation-chain.md` (la latence mesurée de la chaîne STT → LLM → TTS), `design/engine-qualification.md` (le protocole par lequel tout moteur candidat qualifie, rejouable par un tiers), `design/embedded-analysis.md` (le montage d'une analyse qui tourne sur l'appareil, sans service d'analyse distant), `design/grammar-test-set.md` (le jeu d'essai des deux bancs du chantier 2 — juge grammatical et fidélité du STT) et `design/ui-flow.md` (le flux et l'écran, de bout en bout — posture, micro, marquage, chorégraphie du tour).

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

La raison est qu'on ne travaille pas la prononciation d'une phrase qu'on s'apprête à réécrire : elle va disparaître. La conséquence technique suit — **sur un tour grammaticalement fautif, on n'appelle pas le moteur d'analyse du tout.** L'analyse n'est pas cachée, elle n'est pas calculée.

**La porte suit le marquage, pas la grammaire absolue** : un tour est « fautif » pour la porte si et seulement s'il est marqué au cran de sévérité courant. Au cran 3, une tournure maladroite ferme la porte comme une faute — elle va être réécrite. Au cran 2, elle passe, et la prononciation se travaille sur elle : régler la sévérité, c'est déclarer que l'idiomatique n'est pas le sujet du jour, et rien ne va réécrire cette phrase. Coût assumé : le modèle dira la tournure maladroite d'une voix native.

Effet de bord précieux : la branche corrigée s'analyse contre un texte **certain**, celui que l'IA vient de proposer. Toute l'incertitude du texte de référence disparaît de cette branche.

## Les trois échelles du son

Sous « prononciation » il y a trois choses de portée différente, à bien distinguer.

| échelle | ce que ça décrit | comment ça se juge |
|---|---|---|
| **le son** | un phonème dans un mot | note du moteur |
| **le mot** | l'accent lexical — quelle syllabe est la forte | comparaison au modèle |
| **la phrase** | la mélodie — le contour de hauteur sur l'énoncé | comparaison à un modèle |

La syllabe **porte** l'accent et la hauteur, mais elle n'est la portée d'aucun des deux : l'accent est une propriété du mot, fixée par le dictionnaire indépendamment de la phrase ; la mélodie n'existe qu'à l'échelle de l'énoncé, une montée sur une syllabe isolée ne signifiant rien.

**Les trois se jugent par comparaison au modèle, jamais dans l'absolu** — y compris l'accent, dont le moteur annonce pourtant un attendu tiré de son dictionnaire. Cet attendu est inutilisable : sur de l'audio synthétique parfait, le moteur contredit son propre lexique sur 41 % des mots polysyllabiques, et sa lecture d'un mot change selon ce qui se passe ailleurs dans la phrase (cf. `design/speechace.md`). Ce qu'on compare est donc **sa lecture de l'apprenant à sa lecture du modèle** : le biais est le même des deux côtés et s'annule.

**Les trois s'ancrent aux mêmes caractères du texte affiché** — le moteur donne pour chaque son les indices de lettres qu'il couvre, pour chaque syllabe sa sous-chaîne, et la mélodie se pose sur ces mêmes groupes. Ce sont trois propriétés d'une seule chaîne, pas trois analyses à superposer. C'est ce qui rend possible de les marquer **d'un seul mouvement**, et la forme de ce marquage reste à trouver.

Deux irrégularités à prévoir : une lettre peut porter deux sons, et une lettre peut n'en porter aucun.

**Les trois échelles ne sont pas disponibles partout.** En `en-gb`, SpeechAce ne rend aucune donnée d'accent réalisé, et le mot cesse d'être une échelle mesurable — le son et la phrase, eux, survivent (cf. `design/speechace.md`). Le jeu de fonctionnalités dépend donc aussi de **l'accent choisi**, pas seulement du fournisseur, et l'échelle éteinte doit porter sa raison comme toute option absente.

## Marquer par écart au modèle, pas par note absolue

Une note de prononciation ne veut rien dire seule : chaque son a sa note « normale » propre au moteur, et une note basse peut être une particularité de l'outil plutôt qu'une faute.

**Décision** : le modèle à imiter est synthétisé pour la phrase que l'apprenant vient de dire, passé dans le même moteur avec le même texte, et on compare son par son. La marque naît de l'**écart entre l'humain et le modèle**, jamais d'un seuil sur la note brute.

Mesuré (cf. `design/speechace.md`) : les prises témoins tombent à moins d'un point du modèle, les fautes détectées à quinze points ou plus en dessous, et rien entre les deux. L'étalon annule le bruit propre du moteur, ce qui supprime tout besoin de calibrer son par son.

Deux propriétés qui comptent autant que la précision : la comparaison est **interne au tour**, elle n'accumule rien ; et le modèle est de toute façon nécessaire, puisque c'est lui qu'on fait entendre.

**Une marque est une proposition d'aller voir, pas un verdict.** L'écart au modèle est validé là où l'apprenant a **entendu** ce modèle avant de parler : deux fautes d'accent sur deux vues, aucune fausse alerte sur les calques (cf. `design/pronunciation-test-set.md`, bloc F). Sur un tour de conversation il n'a rien entendu, et une divergence peut alors être une vraie faute ou une réalisation légitime que la synthèse ne reproduit pas — l'emphase de sens en est le cas type : « I said it **IS** important » divergera toujours d'un modèle neutre. La mélodie a ses équivalents — montée de continuation, déclarative montante — le contour légitime d'une phrase spontanée n'étant pas unique, là où l'accent lexical est fixé par le dictionnaire.

Ce risque n'est pas chiffré et ne peut pas l'être, puisque étiqueter une prise spontanée exigerait le modèle qu'elle n'a justement pas entendu. Ce qui le rend tenable est la forme de la parenthèse : une marque infondée mène à écouter le modèle et à redire, le calque concorde, et la parenthèse se referme sur une réussite. Une fausse alerte coûte un détour court, jamais une leçon fausse.

**Un modèle n'est un étalon que s'il s'étalonne.** Une voix de synthèse qui note mal sur ce moteur accuserait l'apprenant d'une faute commise par la machine. Toute voix promue modèle passe donc un test — quelques phrases, une médiane haute, aucun son décroché — et une voix qui échoue est écartée quelle que soit sa beauté.

## Écouter plutôt que se faire expliquer

Le remède d'une faute sonore est **d'entendre le modèle et de redire**, aux trois échelles : le mot, la syllabe, la phrase. Pas une consigne d'articulation écrite.

Ça a une conséquence sur ce qu'on exige du moteur. Une consigne (« la langue passe entre les dents ») a besoin de savoir **quel** son a été produit ; jouer un modèle n'a besoin que de savoir **où** ça cloche. Identifier le son produit devient donc un enrichissement, pas une condition — ce qui est heureux, puisque aucun moteur mesuré ne le fait de façon fiable.

L'`extent` rendu par le moteur donne les deux gratuitement : la position du modèle à jouer, et celle de **l'enregistrement de l'apprenant** au même endroit, à faire entendre juste après.

**Les audios de synthèse sont gardés en cache**, indexés par le texte, la voix et le dialecte. Une même synthèse sert alors trois fois : d'étalon pour la mesure, de modèle à écouter, et de modèle à réécouter autant de fois qu'on redit le mot ou la phrase.

## La parenthèse

Une seule primitive, récursive : conversation → phrase → mot. Même geste à chaque étage, même bouton de sortie.

- **Ouverture** par la marque ou par un seuil. Le genre de la marque tient à la **nature** de la faute, jamais à sa gravité — une faute de prononciation est sévère sans être grammaticale, et confondre les deux ouvre la mauvaise parenthèse.
- **Prononciation à l'échelle de la phrase.** Un mot peut être zoomé — parenthèse dans la parenthèse, le temps de l'améliorer, puis retour à la phrase.
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

Un critère pèse sur ce choix et ne relève pas de l'ergonomie : sur l'analyse embarquée, la mémoire d'une passe croît comme le **carré** de la durée du tour (`design/embedded-analysis.md`). Un mode qui borne naturellement la durée vaut donc mieux qu'un mode qui la laisse filer — ça ne désigne pas de gagnant, ça interdit de choisir sur le seul confort.

Contrepartie commune à connaître tant qu'un service d'analyse distant reste en jeu : un tour long coûte cher, l'analyse se facturant à la tranche de quinze secondes arrondie au-dessus.

**Tuyau B — analyser.** Le tour est examiné pour savoir s'il y a un problème et où. Se fait sur l'enregistrement existant, sans jamais rien redemander.

L'audio d'un tour part donc **deux fois**, chez deux fournisseurs distincts.

## La panne

Chaque tour dépend de services distants, et chacun peut manquer — réseau coupé, quota épuisé, fournisseur en panne, clé expirée. La règle se décline par brique, pas globalement :

- **La chaîne de conversation est bloquante, mais réparable par construction** : le fichier conservé fait qu'un envoi raté se **réessaie sans redire la phrase**. L'échec coûte un bouton, jamais une parole perdue. La parenthèse grammaticale, qui ne consomme que cette chaîne, hérite du même traitement.
- **L'analyse ne bloque jamais la conversation.** Un tour dont l'analyse échoue est un tour sans marques, pas un tour en erreur — et il **porte sa raison** (« non analysé — quota »), comme une option éteinte porte la sienne : la panne au runtime est le même cas que la capacité absente.
- **La parenthèse de prononciation sans moteur n'a plus d'objet.** Redire sans verdict n'est pas un exercice dégradé, c'est de l'auto-évaluation à l'oreille — ce que l'architecture refuse partout ailleurs. Si le moteur manque en cours de parenthèse, l'app le dit franchement et propose la sortie ; la phrase initiale reste telle qu'elle a été dite, comme pour toute sortie sans réussite. Ce cas est rare par construction : la marque ne naît pas sur un tour non analysé, donc cette parenthèse ne peut manquer de moteur qu'en le perdant en cours de route.
- **Ni file hors-ligne, ni réanalyse différée** : analyser un tour trois tours plus tard produirait des marques sur du passé, ce que l'indépendance des tours interdit. Ce qui n'a pas été analysé sur le moment ne le sera pas.

## Le texte de référence

Le moteur d'analyse compare un audio à la prononciation attendue d'un **texte donné**. En conversation libre, ce texte n'est pas connu d'avance, et c'est la seule contrainte de la chaîne qu'on ne peut pas contourner : les mesures fines n'existent que sur les endpoints scriptés.

La porte grammaticale en retire déjà la moitié du problème — sur un tour fautif, il n'y a pas d'appel, et la phrase corrigée est redite contre un texte certain. Reste le tour grammaticalement propre.

**Décision** : le texte vient de la transcription, que le LLM peut corriger à partir du contexte de la conversation. La répartition des rôles est une règle : **le STT transcrit la bouche, le LLM décide l'intention.** La normalisation appartient au LLM, qui a le contexte et une instruction — jamais au STT, qui la ferait en silence et sans contexte ; un STT qui répare la grammaire d'office efface le signal d'apprentissage avant tout jugement, et sa fidélité verbatim est un critère de choix (cf. `TODO.md`, chantier 2). La ponctuation d'`intended` fait partie de cette tâche : le LLM ponctue selon l'intention à laquelle il répond — s'il répond à une question, il a lu une question — et le contour du modèle TTS en dépend. Deux faits mesurés encadrent ce choix.

Le piège se déclenche moins souvent qu'on le craignait : sur *« I sink »* comme sur *« I am walkin »*, la reconnaissance a rendu `think` et `walking` d'elle-même — le modèle de langue normalise vers le mot plausible, et il n'y avait rien à corriger.

Mais **une reconstruction fausse coûte cher**. *« Turn light at the corner »* reconstruit en `left` au lieu de `right`, le contexte admettant les deux : noté contre `left`, le mot rend quatre sons aberrants d'un coup et entraîne le suivant avec lui. Une reconstruction fausse ne décale pas une marque, elle en produit une **rafale**.

D'où un garde-fou, qui est une vérification **interne au tour** : un énoncé où beaucoup de sons s'effondrent en même temps signale plus probablement un mauvais texte qu'un mauvais locuteur... à décider : comment on indique ça ?

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

Réglage **global unique**, exposé à l'utilisateur. Il gouverne trois choses qui doivent rester alignées : la voix qui parle, la voix du modèle à imiter, et le référentiel contre lequel la prononciation est notée.

Ce n'est pas une exigence de cohérence esthétique. Mesuré : une voix américaine notée au référentiel britannique tombe à 61 sur un son parfaitement prononcé, et les deux lexiques ne découpent pas la phrase en autant de sons — les marques ne tombent donc même pas sur les mêmes lettres. Comme le modèle sert d'étalon, un désaccord **inverse** la mesure : c'est le modèle qui se fait pénaliser, l'écart devient positif, et la faute de l'apprenant passe inaperçue.

**La voix se choisit.** Deux sélecteurs — le fournisseur de synthèse, puis la voix chez ce fournisseur — et un **étalonnage à la demande** qui avertit si elle échoue. Rien n'est imposé et rien n'est deviné : une voix qui ne s'étalonne pas reste utilisable pour parler, mais elle est signalée comme impropre à servir de modèle.

Mesuré (cf. `design/speechace.md`) : **Jenny en `en-us` et Sonia en `en-gb`, toutes deux chez Azure, s'étalonnent sans un seul trou et n'inversent jamais.** Aucune voix ElevenLabs ne tient l'accent britannique — leur plafond s'effondre en `en-gb` et redevient propre en `en-us` — alors que leurs voix américaines sont recevables. L'accent GB repose donc sur **une seule voix chez un seul fournisseur**, et c'est une fragilité à porter, pas un détail. Cette fragilité est cependant un verdict du moteur retenu, pas une propriété des voix : elle se remesure entièrement si l'analyse change de moteur.

L'étalonnage fait alors double emploi, et c'est ce qui le rend intéressant : il **détecte aussi l'incohérence d'accent**. Une voix américaine soumise au référentiel britannique échoue le test, et le dit, au lieu de dégrader les mesures en silence pendant toute une session.

Par défaut, **le modèle à imiter est la voix de la conversation** — c'est celle qu'on entend déjà, et rien ne justifie d'en présenter une autre. Les dissocier reste possible pour qui le veut, et le cas où le modèle vient d'un autre fournisseur que la conversation est accepté : les briques *conversation*, *synthèse* et *analyse* sont indépendantes, unifiées par le seul paramètre d'accent.

## Les clés d'API

L'app est un **client vide** : l'utilisateur apporte ses propres clés (BYOK), qui ne partent qu'aux fournisseurs concernés. Aucun serveur, aucun compte, aucune consommation à la charge du projet.

Ce choix n'est pas qu'économique : il est la seule issue compatible avec la publication sur F-Droid, qui interdit toute clé embarquée dans une release — une clé dans le binaire est une clé publiée avec les sources.

Ce qui en découle et se décide au premier commit :

- Stockage chiffré adossé au Keystore Android. Jamais en clair, jamais dans un log, jamais dans une sauvegarde système (`android:allowBackup="false"`).
- Rien dans le build : pas de champ `BuildConfig` alimenté par un secret, pas de `local.properties` versionné, pas de secret en `gradle.properties`.
- Anti-feature **`NonFreeNet`** à déclarer à la soumission.
- Écran de configuration guidé, avec un bouton **« tester la clé »** qui valide immédiatement. C'est le vrai coût du BYOK : créer une ressource chez un fournisseur est pénible, et sans validation immédiate toute panne ultérieure sera imputée à l'app.
- **La sonde de capacités se fait là aussi**, une fois : un appel par fonction optionnelle, et l'app allume ou éteint les briques selon les réponses. Elle n'a pas à deviner d'après le plan souscrit, dont on a mesuré qu'il ne correspond pas à ce que le fournisseur annonce.
- L'utilisateur paie sa consommation : l'app doit pouvoir dire ce qu'elle consomme. L'analyse tournant à chaque tour propre, et deux fois par tour puisqu'elle note aussi le modèle, c'est le poste le plus lourd.

Limite connue et acceptée : le BYOK est un mur d'adoption, d'autant que SpeechAce impose un abonnement plancher de 40 $/mois indépendant de l'usage. Le mur est tenable parce que **l'app est d'abord pour son auteur** : la publication F-Droid est une générosité et une discipline, pas une stratégie d'adoption. Ce qui la rendrait adoptable sans mur est l'analyse embarquée (`design/embedded-analysis.md`), ouverte à la contribution via le protocole de qualification (`design/engine-qualification.md`).

### Le relais : possible, pas construit

L'écran de configuration expose donc **deux façons d'atteindre un moteur** : la clé du fournisseur, ou **une URL de relais et un jeton**, tous deux saisis par l'utilisateur.

C'est un point de configuration, pas un service. L'app ne sait pas qui paie ni qui héberge : elle parle à une URL. Quelqu'un peut héberger son propre relais, et rien n'interdit qu'un relais payant existe un jour sans que l'app change.

Trois raisons de l'écrire dès maintenant plutôt que de le rouvrir plus tard :

- **Un relais est la seule forme possible.** L'authentification de SpeechAce est une clé statique en paramètre d'URL — pas de jeton éphémère, pas de credential délégué. On ne peut donc pas confier à l'app un accès restreint : l'audio doit transiter par un tiers de confiance ou par personne. C'est une contrainte du fournisseur, pas un choix.
- **Ça ne heurte pas F-Droid.** La règle porte sur les secrets embarqués, et un relais y satisfait mieux que le BYOK puisque aucune clé n'est dans l'APK. `NonFreeNet` reste déclarée dans les deux cas. Un paiement, s'il existait, devrait être **externe à l'app** — les bibliothèques de facturation propriétaires sont incompatibles avec une compilation depuis les sources.
- **L'abstraction existe déjà.** Clé directe et relais sont deux authentifications du même contrat ; la sonde de capacités fonctionne à l'identique.

Ce qui n'est **pas** décidé, et ne l'est pas par défaut : héberger un tel relais. Faire transiter des enregistrements de voix fait de l'hébergeur un responsable de traitement, sur une donnée personnelle, produite par des apprenants dont certains seront mineurs. Ça change la nature du projet, pas seulement son infrastructure — et le public de F-Droid est précisément celui qui fuit ce transit.

## Fournisseurs

Analyse, conversation et synthèse sont des briques **substituables**, jamais couplées. Le contrat ci-dessous existe pour que ça reste vrai après le choix, pas seulement avant.

**Analyse — SpeechAce, retenu pour la v1** (`design/speechace.md`). Mesuré contre le jeu d'essai complet : six fautes de son sur huit sans fausse alerte sur les témoins, un accent lu par syllabe, une mélodie qui sépare de dix-sept demi-tons. Ses limites sont connues et écrites — deux manques déterministes au phonème, une fausse alerte d'accent sur un témoin correct, un plancher d'abonnement de 40 $/mois à la charge de l'utilisateur.

**Azure Speech est écarté**, et pas seulement classé second : sur les mêmes prises et la même méthode il voit trois fautes sur huit avec des témoins qui descendent plus bas que de vraies fautes, et son score de prosodie s'inverse sur la mélodie. Sa mesure reste dans `design/azure-speech.md` — c'est la seule référence dont on dispose pour juger un futur candidat, et elle a servi à déformer le contrat ci-dessous.

**Le champ des candidats distants n'a jamais été élargi.** Deux services ont été mesurés, SpeechAce et Azure ; aucun troisième n'a été regardé. Ce n'est pas une conclusion, c'est un manque — et il pèse d'autant plus que ce que l'app prend réellement à SpeechAce s'est réduit à l'usage : la mélodie se mesure mieux en local, le verdict d'accent est faux dans un dialecte et absent dans l'autre, et `sound_most_like` renvoie l'écho du texte fourni. Restent la **note au phonème** et l'**ancrage aux lettres**, qui sont réels et coûteux à remplacer. Le banc de `bench/` rend désormais l'essai d'un candidat bon marché : une fonction dans `engine.py`.

**L'analyse embarquée** — la seule alternative encore ouverte, et pas un engagement de la v1 ; le montage est instruit (`design/embedded-analysis.md`) et toute brique se juge au protocole de qualification (`design/engine-qualification.md`). Le gain visé est la disparition du poste à abonnement plancher — l'analyse — pendant que conversation et synthèse restent distantes en BYOK multi-fournisseurs et se paient aux centimes ; `NonFreeNet` reste déclarée tant qu'un maillon distant subsiste. Le montage ne ressemble pas à ce que vend le domaine, et pour une raison de situation plus que d'astuce : un service d'évaluation n'a que l'audio du candidat et un texte, donc il lui faut un dictionnaire pour avoir une norme, alors que l'app possède **deux enregistrements du même énoncé** — elle synthétise le modèle de toute façon. Elle n'a donc besoin d'aucune norme et n'en consulte aucune : le modèle est la source de vérité, on lui fait confiance aveuglément, et rien d'extérieur aux deux enregistrements ne juge quoi que ce soit. Un seul fichier extérieur dans tout le pipeline, les poids d'un modèle acoustique libre ; ni dictionnaire, ni lexique de dialecte, ni table graphème-phonème. La mélodie en reste la brique détachable la plus mûre : DSP pur, autonome, et le contrôle indépendant a montré qu'elle peut battre le tracker du service. La porte reste ouverte et rien de ce qui s'écrit d'ici là ne doit la fermer.

**Le montage est la chaîne STT → LLM → TTS**, tranchée, plutôt qu'une API voix-à-voix. Trois raisons, dont la dernière est mesurée : chaque maillon reste substituable ; la reconstruction du texte de référence voyage dans l'appel LLM qu'on fait de toute façon, là où le voix-à-voix exigerait un appel supplémentaire par tour rien que pour l'obtenir ; et la latence est bonne — **2,6 s jusqu'au premier son** sur un tour court, de bout en bout (cf. `design/conversation-chain.md`).

Deux points de montage réglés par la même mesure. La synthèse **n'est pas pipelinée** sur la première phrase du modèle : le gain est de 0,16 s, parce que le modèle achève son objet un septième de seconde après sa première phrase. Et la reconnaissance se fait **par fichier, pas en flux** : elle coûte un sixième de la durée de l'audio, ce qui pèse sur le tour long — qui est l'exception, pas le régime nominal. Rien ne se complique tant que l'usage n'a pas montré que ça gêne.

**La synthèse a une contrainte que le chantier 1 ne voyait pas : l'ancrage horodaté.** ElevenLabs rend, en REST nu, l'horodatage **caractère par caractère** de ce qu'il synthétise. Azure rend l'équivalent — frontières de mots, visèmes — **uniquement par son SDK**, l'endpoint REST ne renvoyant que l'audio : or ce SDK est propriétaire, ce qui heurte de front la publication F-Droid. Aucun des deux n'a donc à la fois l'étalon des deux accents et l'ancrage en REST. Cet ancrage ne sert encore rien de décidé ; il devient structurel si l'analyse embarquée aboutit, puisque c'est de lui seul que vient l'ancrage aux lettres (cf. `design/embedded-analysis.md`). À savoir dans ce cas : le verdict qui écarte les voix britanniques d'ElevenLabs est celui du lexique `en-gb` de SpeechAce, pas une propriété des voix — il tombe avec lui, et se remesure contre le pipeline embarqué.

**Les fournisseurs de conversation et de synthèse restent à choisir** (cf. `TODO.md`, chantier 2). Ceux du banc — Azure Speech, DeepSeek — ont servi à mesurer, pas à décider.

### Ce que l'app exige de n'importe quel moteur d'analyse

1. **Localiser** chaque son dans l'audio — position et durée — et permettre de repérer quand il n'y parvient pas. Les alignements dégénérés existent et se trahissent par des durées absurdes ; un moteur qui n'en laisse rien voir est inutilisable, parce que ses aberrations passeraient pour des fautes de l'apprenant.
2. **Ancrer ses mesures au texte** : quelles lettres porte ce son, quelles lettres forme cette syllabe. Sans cet ancrage il n'y a pas de marque, seulement des chiffres.
3. Rendre son jugement aux **trois échelles** — le son, le mot, la phrase.
4. **Noter un fichier audio quelconque**, pas seulement une prise de micro. C'est ce qui permet de lui soumettre le modèle et de mesurer par écart plutôt que par seuil absolu.
5. Être **déterministe** : sans ça, comparer un humain à un modèle mesure le bruit du moteur autant que l'écart réel.
6. **Déclarer ses capacités**, de préférence en refusant explicitement ce qu'il ne sait pas faire.

**Dire quel son a été produit** est un enrichissement, non une exigence : il corrobore une détection et ouvrirait la porte à une consigne d'articulation. Les deux moteurs mesurés le font mal, et l'app n'en dépend pas.

**Ce contrat a été dérivé d'un moteur puis déformé par un second.** Ce qui a bougé à la lecture du second : l'identification du son produit est descendue d'exigence à confort, et trois points sont apparus — l'ancrage au texte, l'acceptation d'un audio arbitraire, le déterminisme — dont aucun ne se voyait tant qu'on n'avait qu'un fournisseur.

### Capacités déclarées, pas plus petit dénominateur commun

Il y a deux manières d'être agnostique et elles sont opposées. La première n'expose que ce que *tous* les moteurs savent faire : on n'exploite alors jamais ce que le meilleur a de mieux, et le progrès d'un fournisseur ne profite à personne. La seconde, retenue : **chaque moteur déclare ce qu'il sait faire, et l'app allume ou éteint les briques en conséquence.**

Contrepartie assumée : le jeu de fonctionnalités **dépend du fournisseur choisi**. Un utilisateur verra des options éteintes qu'un autre a. Une option indisponible doit donc **porter sa raison** dans l'interface — sinon elle passe pour un bug, et c'est l'app qu'on accusera, pas le service.

## Hors périmètre

Écarté délibérément de la première version, non par oubli :

- Tout ce qui **s'accumule** : ligne de base par session, motif récurrent, tendance, suivi longitudinal (cf. « Chaque tour est indépendant »).
- Pilotage de la conversation par les faiblesses phonétiques de l'utilisateur.
- La consigne d'articulation écrite, remplacée par l'écoute d'un modèle.

Ouvert, à trancher : **le son isolé comme niveau d'écoute**. L'objection tenait à la production — un son réussi seul se rate encore dans le mot, parce que ce qui coince est la transition. Elle ne s'applique pas forcément à l'écoute, qui ne demande rien à la bouche.
