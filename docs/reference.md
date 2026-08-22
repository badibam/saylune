# speakup — référence

App Android de pratique de l'anglais oral : conversation libre avec une IA, jamais interrompue, doublée d'un travail de la grammaire et de la prononciation à la demande. Ce document est le point d'entrée ; il porte les décisions **propres au projet**, celles qu'aucune facette de sagesse ne couvre. Le craft transverse vit dans les modules abonnés (cf. `manifest.md`).

Docs complémentaires, à ouvrir au besoin : `design/speechace.md` et `design/azure-speech.md` (instruction des candidats moteur d'analyse, mesures et incertitudes) et `design/pronunciation-test-set.md` (le jeu d'essai qui sert à les comparer).

## Le geste

L'utilisateur parle anglais à voix haute. L'IA lui répond en voix, dans l'accent choisi. Rien ne l'interrompt.

Quand il se trompe, l'IA reprend **dans sa réponse** au lieu de s'arrêter — à qui vient de dire *I have 25 years*, elle répond « Ah, you're 25! And where... ». La reprise passe dans le fil ; le tour fautif porte en plus une **marque discrète** (la portion concernée, colorée). Sans cette trace, la discrétion se retourne : on corrige, et personne ne l'apprend.

Un appui sur la marque ouvre une **parenthèse** de travail sur l'erreur. Un appui la referme, et la conversation reprend où elle en était.

Le contrôle du moment appartient à l'utilisateur, jamais à l'app. Seule exception, explicitement optionnelle : les seuils, qui peuvent ouvrir la parenthèse d'eux-mêmes.

## Chaque tour est indépendant

Aucune analyse ne s'accumule d'un tour au suivant. Un tour est examiné avec ce qu'il contient, jugé, marqué, et rien de ce jugement ne survit pour peser sur le tour d'après.

Ce n'est pas une simplification d'implémentation, c'est ce qui rend le marquage lisible : une marque qui apparaît parce que *le même son a déjà été raté trois fois* est incompréhensible pour qui la reçoit. La même faute doit produire la même marque, à n'importe quel moment de la session.

Ça vaut aussi comme borne : ni suivi longitudinal, ni tendance affichée, ni pilotage de la conversation par les faiblesses accumulées.

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

Effet de bord précieux : la branche corrigée s'analyse contre un texte **certain**, celui que l'IA vient de proposer. Toute l'incertitude du texte de référence disparaît de cette branche.

## Les trois échelles du son

Sous « prononciation » il y a trois choses de portée différente, et les confondre embrouille l'interface autant que la mesure.

| échelle | ce que ça décrit | comment ça se juge |
|---|---|---|
| **le son** | un phonème dans un mot | note du moteur |
| **le mot** | l'accent lexical — quelle syllabe est la forte | comparaison au dictionnaire |
| **la phrase** | la mélodie — le contour de hauteur sur l'énoncé | comparaison à un modèle |

La syllabe **porte** l'accent et la hauteur, mais elle n'est la portée d'aucun des deux : l'accent est une propriété du mot, fixée par le dictionnaire indépendamment de la phrase ; la mélodie n'existe qu'à l'échelle de l'énoncé, une montée sur une syllabe isolée ne signifiant rien.

**Les trois s'ancrent aux mêmes caractères du texte affiché** — le moteur donne pour chaque son les indices de lettres qu'il couvre, pour chaque syllabe sa sous-chaîne, et la mélodie se pose sur ces mêmes groupes. Ce sont trois propriétés d'une seule chaîne, pas trois analyses à superposer. C'est ce qui rend possible de les marquer **d'un seul mouvement**, et la forme de ce marquage reste à trouver.

Deux irrégularités à prévoir : une lettre peut porter deux sons, et une lettre peut n'en porter aucun.

## Marquer par écart au modèle, pas par note absolue

Une note de prononciation ne veut rien dire seule : chaque son a sa note « normale » propre au moteur, et une note basse peut être une particularité de l'outil plutôt qu'une faute.

**Décision** : le modèle à imiter est synthétisé pour la phrase que l'apprenant vient de dire, passé dans le même moteur avec le même texte, et on compare son par son. La marque naît de l'**écart entre l'humain et le modèle**, jamais d'un seuil sur la note brute.

Mesuré (cf. `design/speechace.md`) : les prises témoins tombent à moins d'un point du modèle, les fautes détectées à quinze points ou plus en dessous, et rien entre les deux. L'étalon annule le bruit propre du moteur, ce qui supprime tout besoin de calibrer son par son.

Deux propriétés qui comptent autant que la précision : la comparaison est **interne au tour**, elle n'accumule rien ; et le modèle est de toute façon nécessaire, puisque c'est lui qu'on fait entendre.

**Un modèle n'est un étalon que s'il s'étalonne.** Une voix de synthèse qui note mal sur ce moteur accuserait l'apprenant d'une faute commise par la machine. Toute voix promue modèle passe donc un test — quelques phrases, une médiane haute, aucun son décroché — et une voix qui échoue est écartée quelle que soit sa beauté.

## Écouter plutôt que se faire expliquer

Le remède d'une faute sonore est **d'entendre le modèle et de redire**, aux trois échelles : le mot, la syllabe, la phrase. Pas une consigne d'articulation écrite.

Ça a une conséquence sur ce qu'on exige du moteur. Une consigne (« la langue passe entre les dents ») a besoin de savoir **quel** son a été produit ; jouer un modèle n'a besoin que de savoir **où** ça cloche. Identifier le son produit devient donc un enrichissement, pas une condition — ce qui est heureux, puisque aucun moteur mesuré ne le fait de façon fiable.

L'`extent` rendu par le moteur donne les deux gratuitement : la position du modèle à jouer, et celle de **l'enregistrement de l'apprenant** au même endroit, à faire entendre juste après.

## La parenthèse

Une seule primitive, récursive : conversation → phrase → mot. Même geste à chaque étage, même bouton de sortie.

- **Ouverture** par la marque ou par un seuil. Le genre de la marque tient à la **nature** de la faute, jamais à sa gravité — une faute de prononciation est sévère sans être grammaticale, et confondre les deux ouvre la mauvaise parenthèse.
- **Drill** = explication, puis production. L'IA pose une question dont la réponse ne peut se formuler qu'avec la structure visée : les variations sont formulées par l'utilisateur, jamais récitées. Redire une fois la phrase qu'on vient de lui souffler n'apprend rien. Le drill est ainsi fait de la même matière que la conversation.
- **Prononciation à l'échelle de la phrase.** Un mot peut être zoomé — parenthèse dans la parenthèse, le temps de l'améliorer, puis retour à la phrase.
- **Sortie** disponible à tout moment en un appui. Quand l'IA juge la structure acquise, elle le dit et le bouton de sortie **change d'allure** : il devient le geste suggéré. Un seul contrôle, deux états — rien de neuf à l'écran, aucune interruption.
- L'IA sait que ces tours sont une **parenthèse et non du contenu** : elle reprend le sujet d'avant, elle n'enchaîne pas sur la grammaire.

Dans la parenthèse, tout ce qui est dit est **neuf** — la phrase réparée, les variations, le mot zoomé — et chaque énoncé est noté contre un texte de référence **connu d'avance**, puisque c'est l'IA qui l'a proposé.

## Les deux tuyaux

Deux circuits distincts partagent une seule ressource : le fichier audio du tour de parole.

**Tuyau A — la conversation.** Micro → tampon local (PCM 16 kHz mono, un fichier par tour, **conservé**) → fin de parole détectée → envoi au fournisseur de conversation → lecture de la réponse audio. Le fichier local est la condition d'existence du tuyau B : aucun service distant ne rend l'audio envoyé.

**Tuyau B — analyser.** Le tour est examiné pour savoir s'il y a un problème et où. Se fait sur l'enregistrement existant, sans jamais rien redemander.

L'audio d'un tour part donc **deux fois**, chez deux fournisseurs distincts.

## Le texte de référence

Le moteur d'analyse compare un audio à la prononciation attendue d'un **texte donné**. En conversation libre, ce texte n'est pas connu d'avance, et c'est la seule contrainte de la chaîne qu'on ne peut pas contourner : les mesures fines n'existent que sur les endpoints scriptés.

La porte grammaticale en retire déjà la moitié du problème — sur un tour fautif, il n'y a pas d'appel, et la phrase corrigée est redite contre un texte certain. Reste le tour grammaticalement propre.

**Décision** : le texte vient de la transcription, que le LLM peut corriger à partir du contexte de la conversation. Deux faits mesurés encadrent ce choix.

Le piège se déclenche moins souvent qu'on le craignait : sur *« I sink »* comme sur *« I am walkin »*, la reconnaissance a rendu `think` et `walking` d'elle-même — le modèle de langue normalise vers le mot plausible, et il n'y avait rien à corriger.

Mais **une reconstruction fausse coûte cher**. *« Turn light at the corner »* reconstruit en `left` au lieu de `right`, le contexte admettant les deux : noté contre `left`, le mot rend quatre sons aberrants d'un coup et entraîne le suivant avec lui. Une reconstruction fausse ne décale pas une marque, elle en produit une **rafale**.

D'où un garde-fou, qui est une vérification **interne au tour** : un énoncé où beaucoup de sons s'effondrent en même temps signale plus probablement un mauvais texte qu'un mauvais locuteur, et on s'abstient de marquer plutôt que de marquer partout.

## L'affichage

La conversation s'affiche en texte : on ne colorie pas une portion de son.

Les tours de l'IA sont **floutables** — un réglage d'écoute, indépendant de tout le reste. Les lire au lieu de les écouter fait sortir la compréhension orale par la fenêtre.

Le flou ne gêne pas la correction : la marque est sur le tour de **l'utilisateur**, et la parenthèse s'ouvre dessus sans jamais avoir à déflouter la réponse de l'IA.

## L'accent

Réglage **global unique**, exposé à l'utilisateur. Il gouverne trois choses qui doivent rester alignées : la voix qui parle, la voix du modèle à imiter, et le référentiel contre lequel la prononciation est notée.

Ce n'est pas une exigence de cohérence esthétique. Mesuré : une voix américaine notée au référentiel britannique tombe à 61 sur un son parfaitement prononcé, et les deux lexiques ne découpent pas la phrase en autant de sons — les marques ne tombent donc même pas sur les mêmes lettres. Comme le modèle sert d'étalon, un désaccord **inverse** la mesure : c'est le modèle qui se fait pénaliser, l'écart devient positif, et la faute de l'apprenant passe inaperçue.

La voix elle-même n'est pas exposée : l'app en choisit une par défaut pour l'accent retenu, parmi celles qui ont passé l'étalonnage. Si la voix du modèle doit venir d'un autre moteur que celle de la conversation, c'est accepté — les briques *conversation*, *synthèse* et *analyse* sont indépendantes, unifiées par le seul paramètre d'accent.

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

Limite connue et acceptée : le BYOK est un mur d'adoption, d'autant qu'un fournisseur peut imposer un abonnement plancher indépendant de l'usage. C'est le point où un backend hébergé redeviendrait la question — pas avant.

## Fournisseurs

Analyse, conversation et synthèse sont des briques **substituables**, jamais couplées. Aucune n'est tranchée.

**Analyse** — trois candidats (cf. `TODO.md`, chantier 1) :

- **Azure Speech Pronunciation Assessment** — instruit et mesuré (`design/azure-speech.md`). Son verdict de conformité est inutilisable pour la détection.
- **SpeechAce** — instruit et mesuré (`design/speechace.md`). Détecte six fautes sur huit en nommant le son, sept sur huit par écart au modèle, sans fausse alerte sur les témoins. Deux manques déterministes.
- **Reconnaissance phonétique embarquée** — un modèle sur l'appareil au lieu d'un service. Retirerait d'un coup le coût par tour, le BYOK et l'anti-feature `NonFreeNet`. Pas la v1, mais la porte reste ouverte et le reste de l'architecture ne doit pas la fermer.

**Conversation** et **synthèse** — non tranchés (cf. `TODO.md`, chantier 2).

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
