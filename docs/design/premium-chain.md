# La chaîne premium, et la coupe de l'appel en deux

Doc transitoire, ouvert le 2026-09-10. Deux choses y vivent : une **réécriture de l'appel au modèle** qui ne doit rien à personne et vaut telle quelle, et un **mode payant** adossé à Inworld qui s'appuie dessus. La première s'élague quand elle est dans le code et que `activity.md` l'a reprise ; la seconde quand le service tourne.

## Ce qui change dans le projet, et qui est acté

`docs/reference.md` écrit que l'app est un **client vide** : aucun serveur, aucun compte, aucune consommation à la charge du projet. **Cette décision est rouverte.** Sa portée le permettait — elle a été écrite contre l'idée que le projet paie la consommation des autres, pas contre l'idée qu'il vende quelque chose. Ce qui la remplace est plus étroit et se tient : **le projet tient un service qui vend des crédits et n'a jamais accès à ce qui passe.**

L'axe des modes n'est pas l'argent, c'est **qui choisit les maillons** : l'app en local, l'utilisateur en custom, **l'auteur en premium**. La valeur du premium est précisément que l'utilisateur ne choisit pas — c'est ce qui permet de promettre une expérience, donc d'y réserver du contenu.

**Périmètre : les trois maillons distants chez Inworld.** L'analyse reste sur l'appareil. Un seul maillon laissé en BYOK obligerait encore à ouvrir un compte ailleurs, et le mur que le mode existe pour abattre serait toujours là.

## Ce qu'Inworld apporte, et ce qui est refusé

**Retenu.** Le *voice design* — décrire une voix en mots et la recevoir — qui est le vrai motif : des personnages écrits plutôt que choisis dans un catalogue. Les contrôles de pause, qui réparent un manque nommé dans `local-chain.md`, Piper ne sachant pas fabriquer une pause. Le Router, compatible OpenAI et Anthropic, et son **cache de préfixe**.

**Refusé, et chaque refus tient à un invariant déjà écrit.**

- **L'API Realtime**, qui ferait pourtant la chaîne entière côté serveur. Elle **refuse les jetons à usage unique** — sa doc le dit, une session ouvre plusieurs flux donc elle exige la vraie clé — et tout le montage repose sur le fait que le téléphone ne détient jamais la clé. Elle décide en outre de la fin du tour, ce qui retourne l'invariant de capture.
- **La détection de fin de tour**, pour la même raison : rien ne coupe la parole de quelqu'un qui parle encore.
- **La compression de prompt**, qui réécrit en silence ce que le juge reçoit. Une mesure dont on ne sait plus contre quoi elle a été rendue n'est plus une mesure.

**Ni condition ni promesse** : leurs horodatages de synthèse et leurs *voice profile signals* — âge, hauteur, émotion, accent — sont des capacités déclarées d'un fournisseur, jamais des conditions, au sens que `reference.md` pose déjà.

## La clé ne descend jamais sur le téléphone

Un **serveur d'émission** vend des crédits, décompte, et rend un jeton ; le téléphone parle ensuite **directement** à Inworld. L'audio de l'apprenant ne traverse aucune machine du projet, donc « l'audio d'un tour ne quitte l'appareil qu'une fois » reste vrai.

**Ce que leur jeton est, mesuré sur leur doc** : `single_use: true` est obligatoire, un jeton vaut **une seule authentification** et il est consommé au moment où il s'authentifie, que la requête réussisse ou non. Il vit 15 minutes par défaut, une heure au plus. Il porte un `client_reference_id` de 256 caractères, attaché aux requêtes. Il ne se restreint ni à un service ni à un modèle, et **ne porte aucun plafond de dépense**.

**L'usage unique referme la peur principale.** L'app est libre, donc n'importe qui la recompile ; un jeton volé ne vaut **qu'une requête**.

**La pochette** répare le reste. Un jeton par appel voudrait dire trois allers-retours par tour ; le téléphone en demande donc une poignée d'avance et les garde, avec de la marge pour les appels ratés, qui brûlent leur jeton quand même.

**Le décompte est parké.** Leur rapport de consommation par jeton est annoncé et n'existe pas, donc rien ne dit aujourd'hui ce qu'un appel a coûté. Ce qui se fait **dès maintenant et ne coûte rien** : remplir le `client_reference_id` avec qui, pour quoi, et ce qui a été déclaré — le jour où le rapport arrive, tout l'historique devient vérifiable rétroactivement.

## La coupe de l'appel en deux

**Cette partie ne doit rien à Inworld.** Elle vaut avec n'importe quel fournisseur, et les deux appels peuvent vivre chez deux fournisseurs différents.

Aujourd'hui un seul appel rend un contrat entier : la réponse, `intended`, les quatre marquages, la difficulté, l'écho, la clé de menu. Il se coupe en deux, distillés d'un même état :

- **celui qui parle** — écrit la réplique, et rien d'autre ;
- **celui qui juge** — `intended`, les marquages, la difficulté, la clé de menu ; il tourne **pendant que l'apprenant écoute**.

### Le contrat gagne un champ

Deux champs au lieu d'un texte de réponse. Ce ne sont pas deux phrases concurrentes, ce sont **deux portions d'une même phrase** :

- **`continuation`** — ce qui fait avancer : *« And where do you work? »*
- **`echo`** — le préfixe de reprise, ou vide : *« Ah, you're 25! »* (indirect), *« Ah, you mean you ARE 25. »* (explicite)

**Le levier d'écho cesse d'être une consigne et devient une règle de composition dans l'app.** Il gouverne la formulation d'`echo`, ou le laisse vide ; il ne dit plus quoi jouer. C'est le levier d'**avance** qui décide.

**Et deux seuils entrent ici, qu'un premier tableau confondait** (corrigé le 2026-09-10). *S'être trompé* est jugé par **celui qui parle**, et c'est ce qui produit un écho ou non. *Tomber sous la barre* est jugé par **le juge**, avec les poids et la sévérité, et c'est ce qui ferme la porte des mots. Une petite faute dans une longue phrase, à sévérité clémente, fait les deux à la fois : un écho **et** une note qui passe — et c'est précisément le geste fondateur du projet, la reprise qui passe dans le fil sans rien arrêter. Le tableau porte donc les deux colonnes.

| avance | écho | porte des mots | ce qui se joue |
|---|---|---|---|
| poursuit | absent | quelle qu'elle soit | la continuation |
| poursuit | présent | quelle qu'elle soit | **écho + continuation** |
| attend | absent | quelle qu'elle soit | la continuation |
| attend | présent | ouverte | **écho + continuation** |
| attend | présent | fermée, tentatives restantes | **écho seul** |
| attend | présent | fermée, épuisées | **écho + continuation** |

**Quand la conversation poursuit, la porte ne dit rien de ce qui s'entend** — l'écho est l'ouverture de la réplique et il se joue avec elle quel que soit le cran. C'est ce qui rend la position par défaut insensible à l'ordre des deux appels, et le tableau le montre en n'ayant que deux lignes là où l'autre position en a quatre.

**Le dernier cas est un gain sur le montage actuel**, où la continuation nue se joue à l'épuisement : la conversation s'était arrêtée sur une phrase, les tentatives s'épuisent, et l'IA repart comme si de rien n'était. L'écho devant reconnaît la sortie avant de la franchir, et il est déjà là.

**Où la conversation attend, la synthèse part quand même avant le verdict** (écrit le 2026-09-10). Ce qui se joue dépend de la porte, donc le verdict doit arriver avant le premier son — mais l'audio de `écho + continuation`, lui, se fabrique sans le savoir. Il sert dans les deux premières lignes d'« attend » ; dans la troisième il n'est pas joué, et il n'est pas perdu pour autant : c'est **exactement la chaîne mise de côté pour la sortie**, et le cache des synthèses étant indexé par le texte, l'épuisement la retrouve sur place. Le seul vrai déchet est l'apprenant qui reformule avec succès, et il se paie en caractères facturés, jamais en attente — la fabrication tournait à côté du juge. **Où la conversation poursuit, il n'y a rien à décider** : la synthèse et le juge tournent déjà côte à côte et rien n'est jamais jeté.

**La continuation jouée à l'épuisement est celle de la dernière tentative**, jamais de la première — c'est la seule lecture compatible avec « rien n'est jamais contredit », jouer la première ferait répondre à une phrase qui n'existe plus.

### Ce que le prompt du juge perd, et les deux dettes qui se ferment

Il perd la consigne de répondre, et surtout **deux choses qui n'avaient rien à faire là** :

- **la mise en scène.** `activity.md` l'écrit comme **le prix de l'appel unique** : elle est dans le contexte du juge, où elle n'a rien à faire, et ce qui l'en tient à distance est une phrase de prompt que rien ne vérifie. Le juge ne joue plus de personnage : elle sort, et **la dette se ferme au lieu d'être surveillée**.
- **ce dont l'apprenant veut rester loin.** Même famille, même parade non vérifiée (`../TODO.md`). Le juge ne parle pas, donc il n'a pas à savoir ce qu'il faut contourner. Sort aussi.

Sortent également la **persona** et le **numéro de passage**, qui règlent comment le personnage parle et à quel rythme.

**Il garde** les règles de jugement et le contrat de marquage, l'historique, le tour de l'apprenant, et **la situation** — la moitié du `brief` adressée à l'apprenant, qui porte la consigne, et que `reference.md` exige puisque c'est elle que la pertinence juge.

**Il gagne une chose, en fin de queue : la réplique qui vient d'être dite.** C'est la parade contre le seul défaut que la coupe introduit — en un appel, la reprise à l'oral et les marques sortaient du même acte de jugement, donc elles s'accordaient par construction ; en deux, elles peuvent diverger. Une reprise entendue sans marque à l'écran est précisément le cas contre lequel `reference.md` écrit que sans la trace, la discrétion se retourne. Le juge marquant en sachant ce qui a déjà été repris, la divergence devient à sens unique, et c'est le bon sens.

### Le cache, et pourquoi le critère de préfixe change de portée

`../TODO.md` demandait que les deux appels partagent leur préfixe à l'octet près. **Ce n'était vrai que s'ils vivaient chez le même fournisseur**, un cache étant propre à un fournisseur et à une clé. Le vrai partage est ailleurs et il s'applique **de chaque côté indépendamment** : d'un tour au suivant, chaque appel renvoie tout l'historique, dont presque tout était déjà là au tour d'avant.

La forme à tenir des deux côtés est celle que `ConversationPrompt` porte déjà : une tête stable, un historique qui ne grandit que par la fin, le tour de l'apprenant, une queue courte propre à l'appel. Le choix documenté de poser les consignes **après** l'historique — au plus près du tour qu'elles gouvernent, contre un préfixe caché compté en millièmes — reste valable, et chaque côté le tranche pour lui.

### `intended` reste chez celui qui parle, et c'est lui qui le donne au juge

`ConversationPrompt.APP` impose l'ordre des champs et dit pourquoi : `intended` s'écrit **avant** la réplique parce que « les mots de l'apprenant doivent être arrêtés avant qu'on prenne la voix du personnage ». Devant `i had a nice conversation with my ant yesterday`, c'est ce champ qui force le modèle à s'engager sur `aunt` avant de répondre.

Retirer cet échafaudage au répondeur casse deux choses. Il peut répondre à côté, et il ne peut plus épingler la faute que l'écho doit reprendre. Mais le vrai dégât est ailleurs : s'il devine `aunt` pendant que le juge écrit `ant`, **la voix modèle prononce un mot que personne n'a dit** et toutes les marques du tour tombent à côté.

**Donc `intended` est écrit par celui qui parle, et l'app le passe au juge tel quel.** Un seul `intended` dans tout le système, produit là où l'échafaudage sert, consommé par le juge et par l'analyse ; le juge ne reconstruit plus, il reçoit. C'est le même sens unique que la réplique passée au juge, et c'est ce que `../TODO.md` anticipait déjà de sa propre coupe — « un modèle léger pour `intended`, un fort pour le jugement ». Le répondeur **est** ce modèle léger.

**Ce que ça coûte est un point d'attention et non une dette** (`../TODO.md`) : la qualité d'`intended` dépend désormais du modèle choisi pour **parler**, qu'on aura peut-être pris pour sa voix ou sa vitesse plutôt que pour son soin ; et le juge perd la possibilité de rattraper une reconnaissance ratée que le répondeur aurait manquée.

## La fusion langue + voix, écartée

**Tranchée le 2026-09-10 : elle tombe.** Elle n'a jamais été écrite, et l'étape 5 disparaît du plan.

Ce qu'elle était. Le Router fait **modèle de langue et synthèse en une requête** : le même `POST /v1/chat/completions`, avec un paramètre `audio` nommant la voix et le modèle de synthèse ; la réponse rend le texte dans `message.audio.transcript` et l'audio PCM dans `message.audio.data`. Ce qu'elle achetait n'était pas un appel de moins mais du temps — l'audio part pendant que la réplique s'écrit, au lieu d'être livré fini.

**Ce qui la tue tient en une phrase de leur doc** (lue sur `docs.inworld.ai/router/guides/llm-plus-tts`) : le Router **synthétise la sortie du modèle en entier**, et rien ne permet de désigner la portion à dire — pas de champ, pas de balise, pas de convention. Le `transcript` n'est pas un texte posé à côté de l'audio, c'est le texte qui a été prononcé.

Or celui qui parle rend un objet JSON — `intended`, `established`, `echo`, `spoken` —, donc la voix dirait l'objet. La fusion exige de lui de la **prose nue**, et `intended` n'a alors plus de véhicule ; or le faire écrire par celui qui parle est ce que l'étape 4a a tranché, contre le cas où la voix modèle prononce un mot que personne n'a dit.

Les deux échappatoires cherchées, et pourquoi aucune ne tient :

- **Faire voyager le contrat par un appel d'outil.** Fermée par leur doc, qui dit que « quand le modèle décide d'appeler un outil, l'appel revient en `delta.tool_calls` ordinaires — **aucun audio n'est produit pour ce tour** ». L'appel d'outil n'échappe pas à la synthèse, il l'éteint : la réplique parlée arriverait au tour d'après, une fois le résultat renvoyé, soit un aller-retour de plus — exactement l'appel que la fusion existait pour supprimer. La sortie structurée (`response_format`) ne sauve rien non plus : c'est toute la sortie qui devient l'objet, donc toute la sortie qui serait dite.
- **Rendre `intended` au juge.** Techniquement suffisant, et refusé : ça rouvre 4a, dont l'argument entier est que la réplique et la marque ne peuvent pas porter sur deux phrases différentes.

Deux choses que la chute rend caduques et qui valaient d'être notées tant qu'elle était vivante. La fusion aurait **jeté un audio par tour** dans le cas « attend, sous la barre, tentatives restantes », où il faut ne jouer que l'écho : on ne découpe jamais un écho, il a été prononcé dans un souffle qui appelle la suite, donc il faudrait resynthétiser. Et il aurait fallu un critère d'allumage automatique — conversation et synthèse chez le même fournisseur qui l'expose — donc une condition de plus dans la couche fournisseur. **En appels séparés, l'app ne synthétise que ce qu'elle joue**, et rien ne s'allume tout seul.

Ce qui reste d'Inworld ne dépendait pas d'elle : le *voice design*, les contrôles de pause, le cache de préfixe, et les trois maillons sur une seule clé.

## Ce qui reste séparé quoi qu'il arrive

- **la reconnaissance** — l'app a besoin du transcript **comme texte**, c'est la référence de l'analyse ;
- **la synthèse du modèle à imiter** — c'est la phrase de **l'apprenant**, dans la **voix de référence**, qui est une liste à part et doit passer l'étalonnage. Aucun pipeline de conversation ne la produira jamais.

## Le plan

**Phase 1 — dans l'app, sans rien de neuf dehors.** Chaque étape vaut seule.

1. **Fait.** `ConversationPrompt` porte déjà la forme voulue — tête stable, historique par la fin, consignes en queue — et la décision de poser les consignes *après* l'historique y est écrite avec son prix.
2. **Fait le 2026-09-10.** `echo` / `continuation` dans le contrat, la règle de composition dans l'app, la sortie d'un passage bloqué réparée avec.
3. **Fait le 2026-09-10.** Inworld sur les trois maillons. Trois faits y reposent sur de la doc et pas sur un appel, inscrits au `../TODO.md`.
4. **Couper l'appel en deux**, et ça se coupe en trois parce que ce sont trois intentions.
5. Mesurer — le banc du juge (`grammar-test-set.md`, écrit, jamais tourné) dans les deux montages, et la latence jusqu'au premier son.

**Il n'y a plus de point de décision au bout.** Il y en avait un — la fusion reste ou tombe — et elle est tombée avant d'être écrite. Ce qui reste à mesurer se mesure pour soi : que les deux prompts marquent comme le prompt unique marquait, et ce que la coupe a rendu de secondes.

### L'étape 4, en trois

**4a — deux prompts, deux appels, même ordonnancement.** Fait le 2026-09-10. Le juge est appelé juste après le répondeur, tout avant la lecture. Rien d'observable ne change sauf le nombre d'appels, donc c'est l'étape qui **prouve que les deux prompts tiennent** sans rien devoir à l'ordre.

**4b — le juge passe pendant l'écoute.** Fait le 2026-09-10. C'est le gain de latence, et c'est là que l'ordre porte / joue / marque bouge. Se juge seul, et se défait seul.

Deux choses que ce doc n'avait pas vues, tranchées en l'écrivant.

**Le juge ne peut pas passer sous l'écoute quand la conversation attend.** Ce qui se joue alors est ce que la note désigne — écho seul si le passage est à reformuler et qu'il reste des tentatives —, donc la note doit être là avant le premier son. À l'inverse, quand elle poursuit, le tableau plus haut joue l'écho et la continuation quel que soit le cran : rien de ce que le juge dit ne change ce qui s'entend. **C'est donc le levier d'avance qui décide si le jugement s'attend**, et le gain de latence est sur la position par défaut.

**Un juge qui donne sa langue au chat après que la voix est partie ne coûte plus le tour.** En un appel, un échec renvoyait l'enregistrement gardé ; ici le personnage a déjà répondu, et le renvoyer le ferait répondre deux fois. Ce qui manque n'est donc pas une parole mais **un appel**, et tout ce qu'il prend est encore en main : le tour reste avec son texte et sans marque, et le même bouton redemande le jugement seul au lieu de renvoyer une prise.

**Et le passage est retenu tant qu'il manque.** Un tour auquel on a répondu et que personne n'a lu glisserait sinon dans le passé derrière deux ou trois autres, et les marques y arriveraient devant quelqu'un qui parle d'autre chose — ce que `reference.md` refuse en écartant toute réanalyse différée. Un tour enregistré pendant la retenue est gardé, jamais jeté, et part de lui-même une fois le passage libre.

**4c — le juge gagne son propre sélecteur.** Fait le 2026-09-10. Une entrée de plus au catalogue, pour qu'il vive chez un autre fournisseur que la voix, ce que ce doc veut.

**Rien ne se replie sur le choix de la conversation** : un jugement non réglé est un maillon non réglé, et il échoue en le disant, comme les trois autres. Le prix est qu'une installation qui portait les anciens réglages a une ligne de plus à remplir avant de pouvoir parler. Et **l'effort se stocke par maillon** : le répondeur peut être pris pour sa vitesse, le juge pour son soin, et une seule entrée pour les deux aurait fait régler par le menu de l'un ce que l'autre dépense.

### Ce que 4a touche, relevé sur le code

- **`ConversationPrompt.APP`** — un seul texte porte les consignes de réponse *et* le contrat de marquage ; il se coupe en deux, plus deux assembleurs `system()` au lieu d'un. **C'est de la prose de prompt, donc du matériau qui se mesure** : la réécrire déplace ce qui produit les marques, et le banc du juge est ce qui le dira.
- **`ReplyReader.read()`** — se scinde en deux lecteurs. Le contrôle des empans contre les mots d'`intended` reste côté juge, mais `intended` lui arrive d'ailleurs.
- **`Reply`** perd son `judged`, que le juge rend à part.
- **`Conversation`** gagne une seconde méthode, et quatre implémentations la suivent — dont Replicate, qui ne passe pas par `ChatCompletions`.
- **`TurnPipeline`** fait deux appels.
- **`ConversationPrompt.PROVOKED` et `answered()`**, qui supposent tous deux un appel unique.

### Trois choses que la lecture du code a fait remonter

- **Le tour provoqué se simplifie tout seul.** `PROVOKED` demande aujourd'hui au modèle de laisser tomber huit champs qui n'ont rien à quoi se rattacher. Avec la coupe, un tour que personne n'a provoqué **n'appelle pas le juge**, et ces huit lignes disparaissent au lieu d'être entretenues.
- **La relecture de l'historique se dédouble, et ce n'est pas tranché.** `answered()` rejoue chaque tour passé comme l'objet que le modèle avait émis — `established`, puis `spoken`, puis l'`intended` du tour d'avant. Les deux côtés n'ont plus besoin des mêmes champs, donc chacun veut sa relecture. À décider en écrivant 4a.
- **Deux mesures de la même journée sont à ne pas perdre** : rejouer les réponses passées en prose plutôt qu'en JSON fait revenir le troisième tour vide, et les marques restent hors de la relecture parce qu'un modèle à qui l'on montre ses vingt derniers verdicts devient cohérent avec eux plutôt qu'avec le tour qu'il lit. Les deux valent pour les deux côtés.

**Phase 2 — le service.**

6. Serveur d'émission minimal : il émet, liste blanche, `client_reference_id` rempli dès le premier jeton. Éprouve la pochette, la durée de vie, et le jeton brûlé par un appel raté.
7. Le décompte, quand leur rapport par jeton existe.
8. Crédits et achat. **Question ouverte, non traitée** : ce que fait l'app quand le solde tombe à zéro en pleine conversation.

**Phase 3 — ce que ça sert.**

9. Les trois modes et leur écran.
10. Le contenu réservé : une fiche déclare ce qu'elle exige, et une tuile fermée **se voit et porte sa raison** — `reference.md` interdit d'en retirer une en silence.

**La coupure qui compte est entre 5 et 6** : avant, l'app s'améliore pour tout le monde sans engagement ; après, le projet tient un service.

## Écarté en chemin

- **Un cache de synthèses partagé côté serveur**, qui aurait fait payer une fois pour tous les apprenants les répliques écrites des scènes. Il demandait que le texte passe par le serveur du projet ; refusé. *(Leur synthèse par lot reste l'outil qui conviendrait pour pré-fabriquer ces répliques à l'avance — autre sujet, non ouvert.)*
- **La location de GPU**, à partir de 5 $ l'heure, provisionnée « en quelques jours », sans rien de documenté sur l'extinction à l'inactivité. C'est une machine louée et non un service à la requête. Et rien dans cette app n'en veut : le modèle de langue, la reconnaissance et la synthèse se vendent au jeton et au caractère ; l'analyse tourne sur un téléphone de 2019 ; les voix de personnages fabriquées sont du VITS, plus rapide que le temps réel sur un processeur.
- **L'analyse des sons déportée**, en attente : elle se rouvre ou disparaît selon ce que rend `small-acoustic-model.md`.
- **La réglementation d'un service payant** — support, remboursement, conditions. Hors de ce doc, et à ne pas découvrir après le premier paiement.
