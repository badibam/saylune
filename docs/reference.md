# speakup — référence

App Android de pratique de l'anglais oral : conversation libre avec une IA, jamais interrompue, doublée d'un travail de la grammaire et de la prononciation à la demande. Ce document est le point d'entrée ; il porte les décisions **propres au projet**, celles qu'aucune facette de sagesse ne couvre. Le craft transverse vit dans les modules abonnés (cf. `manifest.md`).

## Le geste

L'utilisateur parle anglais à voix haute. L'IA lui répond en voix, dans l'accent choisi. Rien ne l'interrompt.

Quand il se trompe, l'IA reprend **dans sa réponse** au lieu de s'arrêter — à qui vient de dire *I have 25 years*, elle répond « Ah, you're 25! And where... ». La reprise passe dans le fil ; le tour fautif porte en plus une **marque discrète** (la portion concernée, colorée). Sans cette trace, la discrétion se retourne : on corrige, et personne ne l'apprend.

Un appui sur la marque ouvre une **parenthèse** de travail sur l'erreur. Un appui la referme, et la conversation reprend où elle en était.

Le contrôle du moment appartient à l'utilisateur, jamais à l'app. Seule exception, explicitement optionnelle : les seuils, qui peuvent ouvrir la parenthèse d'eux-mêmes.

## Les trois curseurs

Fluidité, grammaire, prononciation ne sont pas trois modes entre lesquels on bascule : ce sont trois axes réglables, et **les régler est la façon de déclarer l'intention d'une session** sans changer ni d'écran ni de mode. C'est le mécanisme qui tisse les trois dans une seule conversation.

- **Pression conversationnelle** — de « l'IA te laisse mener » à « elle relance dès que tu t'arrêtes et refuse les réponses en trois mots ». La fluidité a un contenu propre : sans ce curseur, elle ne serait qu'un mot pour « on ne t'embête pas ».
- **Sévérité grammaticale** — trois crans : ne rien marquer / marquer les fautes / marquer aussi les tournures correctes mais maladroites. Pas un pourcentage : une phrase est fautive ou ne l'est pas.
- **Seuil de prononciation** — score en deçà duquel un phonème est marqué. Facultatif : le poser à 70 %, c'est déclarer qu'on travaille cet aspect-là aujourd'hui.

Un curseur peut déclencher la parenthèse automatiquement au lieu de seulement marquer. C'est l'inversion assumée du principe ci-dessus, et elle reste un choix de l'utilisateur.

Écarté pour l'instant, gardé en réserve : déclencher sur un **motif récurrent** dans la session (« /θ/ manqué six fois en dix minutes ») plutôt que sur un score isolé. Plus juste — un raté isolé est du bruit — mais ça ajoute une couche de jugement à l'échelle de la conversation entière.

## La parenthèse

Une seule primitive, récursive : conversation → phrase → mot. Même geste à chaque étage, même bouton de sortie.

- **Ouverture** par la marque ou par un seuil. Si les deux marques tombent sur le même tour, la grammaire passe d'abord : on ne travaille pas la prononciation d'une phrase qu'on s'apprête à réécrire.
- **Drill** = explication, puis production. L'IA pose une question dont la réponse ne peut se formuler qu'avec la structure visée : les variations sont formulées par l'utilisateur, jamais récitées. Redire une fois la phrase qu'on vient de lui souffler n'apprend rien. Le drill est ainsi fait de la même matière que la conversation.
- **Prononciation à l'échelle de la phrase.** Un mot peut être zoomé — parenthèse dans la parenthèse, le temps de l'améliorer, puis retour à la phrase — avec la consigne d'articulation qui va avec. Le zoom ne descend pas au phonème isolé : un son réussi seul se rate encore dans le mot, parce que ce qui coince est la transition.
- **Sortie** disponible à tout moment en un appui. Quand l'IA juge la structure acquise, elle le dit et le bouton de sortie **change d'allure** : il devient le geste suggéré. Un seul contrôle, deux états — rien de neuf à l'écran, aucune interruption.
- L'IA sait que ces tours sont une **parenthèse et non du contenu** : elle reprend le sujet d'avant, elle n'enchaîne pas sur la grammaire.

## Les deux tuyaux

Deux circuits distincts partagent une seule ressource : le fichier audio du tour de parole.

**Tuyau A — la conversation.** Micro → tampon local (PCM 16 kHz mono, un fichier par tour, **conservé**) → fin de parole détectée → envoi au fournisseur de conversation → lecture de la réponse audio. Le fichier local est la condition d'existence du tuyau B : aucun service distant ne rend l'audio envoyé.

**Tuyau B — détecter, puis travailler.** Deux usages qu'il ne faut pas confondre :

- **Détecter** — le tour qui vient d'être dit est examiné pour savoir *s'il y a un problème*. C'est ce qui pose la marque et fait sonner le seuil. Ne demande pas une note fine, seulement de repérer que quelque chose cloche. Se fait sur l'enregistrement existant, sans jamais rien redemander.
- **Travailler** — une fois la parenthèse ouverte, tout ce qui est dit est **neuf** : la phrase réparée, les variations, le mot zoomé. Chaque énoncé est un nouvel enregistrement, noté contre un texte de référence **connu d'avance**, puisque c'est l'IA qui l'a proposé.

L'enregistrement d'origine ne sert donc qu'au diagnostic. Corollaire : réparer la grammaire **impose de redire la phrase** — on ne peut pas noter un audio contre un texte corrigé qu'il ne contient pas, l'alignement produirait un score plausible et faux.

## Le texte de référence — le piège, redimensionné

Un moteur de notation compare un audio à la prononciation attendue d'un **texte donné**. En conversation libre, ce texte n'est pas connu d'avance.

Le prendre tel quel de la reconnaissance vocale retourne le problème contre lui-même : qui dit *« I sink »* pour *« I think »* voit son erreur transcrite `sink`, puis notée contre un /s/ attendu — score parfait sur la faute exacte qu'il voulait corriger.

**Décision** : le LLM reconstruit la phrase visée à partir de ce qu'il a entendu et du contexte de la conversation (il a de toute façon lu la phrase pour y répondre).

La séparation détecter / travailler désamorce largement ce piège : le texte reconstruit ne sert plus qu'à la **détection**. S'il est un peu faux, la marque tombe un peu à côté — le travail, lui, se fait ensuite sur des textes sûrs. L'erreur ne se propage plus dans tout le drill.

Reste le cas ambigu : *« I sink »* est-il un `think` mal prononcé ou un mot faux ? L'IA tranche seule, sans demander. Elle peut se tromper, mais l'enjeu a rétréci — elle se trompe sur *quelle marque poser*, plus sur la note.

Cette décision rejoint la recommandation du fournisseur, qui conseille d'appeler d'abord la reconnaissance vocale pour obtenir le texte, puis de lancer l'évaluation en mode **scripté** — plus précis que le mode non scripté, et moins cher.

## L'affichage

La conversation s'affiche en texte : on ne colorie pas une portion de son.

Les tours de l'IA sont **floutables** — un réglage d'écoute, indépendant de tout le reste. Les lire au lieu de les écouter fait sortir la compréhension orale par la fenêtre.

Le flou ne gêne pas la correction : la marque est sur le tour de **l'utilisateur**, et la parenthèse s'ouvre dessus sans jamais avoir à déflouter la réponse de l'IA.

## L'accent

Réglage **global unique**, exposé à l'utilisateur. Il gouverne trois choses qui doivent rester cohérentes : la voix qui parle, la voix de référence à imiter, et le référentiel contre lequel la prononciation est notée. Noter un /r/ américain contre un modèle britannique produit du bruit, pas du retour.

La voix elle-même n'est pas exposée : l'app en choisit une par défaut pour l'accent retenu. Si la voix de référence doit venir d'un autre moteur que celle de la conversation, c'est accepté — les briques *conversation* et *analyse* sont indépendantes, unifiées par le seul paramètre d'accent.

## Les clés d'API

L'app est un **client vide** : l'utilisateur apporte ses propres clés (BYOK), qui ne partent qu'aux fournisseurs concernés. Aucun serveur, aucun compte, aucune consommation à la charge du projet.

Ce choix n'est pas qu'économique : il est la seule issue compatible avec la publication sur F-Droid, qui interdit toute clé embarquée dans une release — une clé dans le binaire est une clé publiée avec les sources.

Ce qui en découle et se décide au premier commit :

- Stockage chiffré adossé au Keystore Android. Jamais en clair, jamais dans un log, jamais dans une sauvegarde système (`android:allowBackup="false"`).
- Rien dans le build : pas de champ `BuildConfig` alimenté par un secret, pas de `local.properties` versionné, pas de secret en `gradle.properties`.
- Anti-feature **`NonFreeNet`** à déclarer à la soumission.
- Écran de configuration guidé, avec un bouton **« tester la clé »** qui valide immédiatement. C'est le vrai coût du BYOK : créer une ressource Azure est pénible, et sans validation immédiate toute panne ultérieure sera imputée à l'app.
- L'utilisateur paie sa consommation : l'app doit pouvoir dire ce qu'elle consomme. La détection tournant à chaque tour quand le seuil est actif, elle multiplie les appels — c'est le poste le plus lourd, et le curseur qui l'active doit le dire.

Limite connue et acceptée : le BYOK est un mur d'adoption pour un public qui ne créera pas de compte Azure pour essayer l'app. C'est le point où un backend hébergé redeviendrait la question — pas avant.

## Fournisseurs

L'analyse et la conversation sont deux briques **substituables**, jamais couplées.

- **Analyse** : Azure Speech Pronunciation Assessment, via **API REST** — pas le SDK, qui est un binaire propriétaire incompatible avec F-Droid. Retourne un score par phonème et, via `NBestPhonemes`, le phonème réellement produit face à l'attendu. Alphabet IPA. Le REST est limité à l'audio court, ce qui convient à des phrases de conversation.
- **Conversation** : non tranché (cf. `TODO.md`, chantier 2).

Restrictions connues du moteur d'analyse : la prosodie, le niveau syllabe et l'évaluation de contenu sont limités à `en-US`. Le niveau phonème l'est peut-être aussi — la doc du fournisseur se contredit sur ce point, une phrase l'annonçant sur toutes les locales supportées et une table de la même page ne listant l'alphabet phonétique que pour `en-US` et `zh-CN`, le phonème produit pour `en-US` seul. Les rapports terrain divergent entre eux. Mesuré au chantier 1 ; à défaut, la promesse « choix de l'accent » ne tient pas.

## Hors périmètre

Écarté délibérément de la première version, non par oubli :

- La **prosodie** comme axe à part entière : Azure la limite à `en-US` et la rend au niveau phrase, pas phonème. Un signal bonus sur une locale, pas une colonne de l'architecture.
- Le **phonème isolé** comme niveau de zoom (cf. « La parenthèse »).
- Pilotage de la conversation par les faiblesses phonétiques de l'utilisateur (l'IA orientant ses questions pour faire produire les sons ratés).
- Suivi longitudinal, statistiques, carte phonétique dans la durée.
- Leçons, niveaux, cursus, gamification.
- Fonctionnement hors ligne.
