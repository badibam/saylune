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

| avance | note | ce qui se joue |
|---|---|---|
| poursuit | A ou B | continuation (`echo` vide) |
| poursuit | sous la barre | **écho + continuation** |
| attend | A ou B | continuation (`echo` vide) |
| attend | sous la barre, tentatives restantes | **écho seul** |
| attend | sous la barre, épuisées | **écho + continuation** |

**Le dernier cas est un gain sur le montage actuel**, où la continuation nue se joue à l'épuisement : la conversation s'était arrêtée sur une phrase, les tentatives s'épuisent, et l'IA repart comme si de rien n'était. L'écho devant reconnaît la sortie avant de la franchir, et il est déjà là.

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

### Une chose que la coupe retire au répondeur, et qui n'est pas mesurée

`ConversationPrompt.APP` impose l'ordre des champs et dit pourquoi : `intended` s'écrit **avant** la réplique parce que « les mots de l'apprenant doivent être arrêtés avant qu'on prenne la voix du personnage ». Coupé, le répondeur perd cet échafaudage — il répond au transcript brut sans avoir écrit ce qu'il a compris.

Deux issues, non départagées : lui faire écrire `intended` quand même, court et jeté ensuite ; ou ne pas le lui faire écrire et mesurer si la réplique se dégrade. **À trancher au banc, pas ici.**

## La fusion langue + voix

Le Router fait **modèle de langue et synthèse en une requête** : le même `POST /v1/chat/completions`, avec un paramètre `audio` nommant la voix. La réponse rend les deux séparément — le texte dans `delta.audio.transcript`, l'audio PCM dans `delta.audio.data` — en flux, découpés indépendamment.

**Ce qu'elle achète n'est pas un appel de moins, c'est du temps.** L'audio part pendant que la réplique s'écrit, au lieu d'être livré fini ; et le jugement, qui passait avant le premier son, passe pendant l'écoute.

**Elle s'allume seule, à une condition unique : conversation et synthèse chez le même fournisseur qui l'expose.** Le juge est ailleurs si l'utilisateur le veut, ça ne la regarde pas. **Le moteur de règles n'entre jamais dans la décision, et la couche fournisseur ne lit aucun levier** — elle rend un texte et son audio, l'app en fait ce qu'elle veut.

L'appel fusionné écrit `echo` puis `continuation` **comme un seul texte** et le synthétise d'un trait : une synthèse, un audio, et l'intonation coule d'un morceau à l'autre parce que la voix n'a jamais su qu'il y avait deux morceaux.

**Le cas « attend, sous la barre, tentatives restantes » est le seul qui paie.** Il faudrait ne jouer que le début ; on jette l'audio et on resynthétise l'écho seul. **On ne découpe jamais.** Leurs horodatages existent — `timestampType` à `WORD` ou `CHARACTER` — mais ils s'activent à la requête donc ils ralentiraient les quatre cas qui n'en ont pas besoin, rien ne dit qu'ils sont exposés sur l'appel fusionné, et surtout un écho coupé **pend** : il a été prononcé dans un souffle qui appelle la suite. Ce déchet n'existe **que sous fusion** — en appels séparés, l'app ne synthétise que ce qu'elle joue.

Un paramètre « écris seulement » a été envisagé pour l'éviter, et il est écarté sur le compte : il coûte un appel de plus dans les deux autres cas d'« attend », et fait jeu égal dans celui qu'il devait réparer.

## Ce qui reste séparé quoi qu'il arrive

- **la reconnaissance** — l'app a besoin du transcript **comme texte**, c'est la référence de l'analyse ;
- **la synthèse du modèle à imiter** — c'est la phrase de **l'apprenant**, dans la **voix de référence**, qui est une liste à part et doit passer l'étalonnage. Aucun pipeline de conversation ne la produira jamais.

## Le plan

**Phase 1 — dans l'app, sans rien de neuf dehors.** Chaque étape vaut seule.

1. Rendre explicite et tenir la forme du prompt : tête stable, historique par la fin, queue courte.
2. `echo` / `continuation` dans le contrat, et la règle de composition dans l'app.
3. Inworld comme fournisseur, sur les trois maillons, dans l'écran des clés existant — le catalogue servant de sonde.
4. Couper l'appel en deux, distillés d'un même état, chaque côté chez le fournisseur qu'on veut.
5. La fusion : l'appel qui parle prend `audio`.
6. Mesurer — le banc du juge (`grammar-test-set.md`, écrit, jamais tourné) dans les deux montages, et la latence jusqu'au premier son.

**Point de décision après 6** : la fusion reste ou tombe. Tout ce qui précède est acquis dans les deux cas.

**Phase 2 — le service.**

7. Serveur d'émission minimal : il émet, liste blanche, `client_reference_id` rempli dès le premier jeton. Éprouve la pochette, la durée de vie, et le jeton brûlé par un appel raté.
8. Le décompte, quand leur rapport par jeton existe.
9. Crédits et achat. **Question ouverte, non traitée** : ce que fait l'app quand le solde tombe à zéro en pleine conversation.

**Phase 3 — ce que ça sert.**

10. Les trois modes et leur écran.
11. Le contenu réservé : une fiche déclare ce qu'elle exige, et une tuile fermée **se voit et porte sa raison** — `reference.md` interdit d'en retirer une en silence.

**La coupure qui compte est entre 6 et 7** : avant, l'app s'améliore pour tout le monde sans engagement ; après, le projet tient un service.

## Écarté en chemin

- **Un cache de synthèses partagé côté serveur**, qui aurait fait payer une fois pour tous les apprenants les répliques écrites des scènes. Il demandait que le texte passe par le serveur du projet ; refusé. *(Leur synthèse par lot reste l'outil qui conviendrait pour pré-fabriquer ces répliques à l'avance — autre sujet, non ouvert.)*
- **La location de GPU**, à partir de 5 $ l'heure, provisionnée « en quelques jours », sans rien de documenté sur l'extinction à l'inactivité. C'est une machine louée et non un service à la requête. Et rien dans cette app n'en veut : le modèle de langue, la reconnaissance et la synthèse se vendent au jeton et au caractère ; l'analyse tourne sur un téléphone de 2019 ; les voix de personnages fabriquées sont du VITS, plus rapide que le temps réel sur un processeur.
- **L'analyse des sons déportée**, en attente : elle se rouvre ou disparaît selon ce que rend `small-acoustic-model.md`.
- **La réglementation d'un service payant** — support, remboursement, conditions. Hors de ce doc, et à ne pas découvrir après le premier paiement.
