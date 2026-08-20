# speakup — référence

App Android de pratique de l'anglais oral : conversation libre avec une IA, jamais interrompue, doublée d'une analyse phonétique à la demande. Ce document est le point d'entrée ; il porte les décisions **propres au projet**, celles qu'aucune facette de sagesse ne couvre. Le craft transverse vit dans les modules abonnés (cf. `manifest.md`).

## Le geste

L'utilisateur parle anglais à voix haute. L'IA lui répond en voix, dans l'accent choisi. Rien ne l'interrompt.

À tout moment, un appui déclenche l'analyse phonétique de **la phrase qu'il vient de dire** : quels phonèmes ont manqué, et lequel a été produit à la place. Il répète la phrase jusqu'à ce que tous passent le seuil, puis reprend la conversation où elle en était.

Le contrôle du moment de la correction appartient à l'utilisateur, jamais à l'app. C'est ce qui distingue speakup des applications de drill, qui imposent la phrase et le moment.

## Les deux tuyaux

Deux circuits distincts partagent une seule ressource : le fichier audio du tour de parole.

**Tuyau A — la conversation.** Micro → tampon local (PCM 16 kHz mono, un fichier par tour, **conservé**) → fin de parole détectée → envoi au fournisseur de conversation → lecture de la réponse audio. Le fichier local est la condition d'existence du tuyau B : aucun service distant ne rend l'audio envoyé.

**Tuyau B — l'analyse, sur demande.** Appui → récupération du fichier du dernier tour (rien n'est réenregistré) → obtention du **texte de référence** → envoi audio + texte au moteur de notation, avec la locale de l'accent choisi → affichage par phonème → répétition, notée contre le **même** texte de référence, jusqu'à validation.

## Le texte de référence — le piège central

Un moteur de notation compare un audio à la prononciation attendue d'un **texte donné**. Sans texte, pas de score par phonème. Or en conversation libre, ce texte n'est pas connu d'avance.

Le prendre tel quel de la reconnaissance vocale retourne le problème contre lui-même : qui dit *« I sink »* pour *« I think »* voit son erreur transcrite `sink`, puis notée contre un /s/ attendu — score parfait sur la faute exacte qu'il voulait corriger. L'outil félicite l'erreur.

**Décision** : le LLM reconstruit la phrase visée à partir de ce qu'il a entendu et du contexte de la conversation (il a de toute façon lu la phrase pour y répondre) ; cette reconstruction s'affiche sur une ligne **éditable** avant l'analyse. Le LLM tranche l'ambiguïté dans l'immense majorité des cas, l'édition reste le filet.

Cas non couvert, et assumé : une prononciation assez éloignée pour que le mot visé soit indevinable. Il faudra alors un mode où l'utilisateur pose la phrase lui-même.

Cette décision rejoint la recommandation explicite du fournisseur, qui conseille d'appeler d'abord la reconnaissance vocale pour obtenir le texte, puis de lancer l'évaluation en mode **scripté** — plus précis que le mode non scripté, et moins cher.

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
- L'utilisateur paie sa consommation : l'app doit pouvoir dire ce qu'elle consomme.

Limite connue et acceptée : le BYOK est un mur d'adoption pour un public qui ne créera pas de compte Azure pour essayer l'app. C'est le point où un backend hébergé redeviendrait la question — pas avant.

## Fournisseurs

L'analyse et la conversation sont deux briques **substituables**, jamais couplées.

- **Analyse** : Azure Speech Pronunciation Assessment, via **API REST** — pas le SDK, qui est un binaire propriétaire incompatible avec F-Droid. Retourne un score par phonème et, via `NBestPhonemes`, le phonème réellement produit face à l'attendu. Alphabet IPA. Le REST est limité à l'audio court, ce qui convient à des phrases de conversation.
- **Conversation** : non tranché (cf. `TODO.md`, chantier 2).

Restrictions connues du moteur d'analyse : la prosodie, le niveau syllabe et l'évaluation de contenu sont limités à `en-US`. Le niveau phonème est documenté pour toutes les locales supportées, mais **non vérifié** hors `en-US` — c'est le chantier 1.

## Hors périmètre

Écarté délibérément de la première version, non par oubli :

- Pilotage de la conversation par les faiblesses phonétiques de l'utilisateur (l'IA orientant ses questions pour faire produire les sons ratés).
- Suivi longitudinal, statistiques, carte phonétique dans la durée.
- Leçons, niveaux, cursus, gamification.
- Fonctionnement hors ligne.
