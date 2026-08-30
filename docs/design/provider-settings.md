# Le réglage des fournisseurs

Conception transitoire : écrite pour être implémentée, à élaguer une fois le code en place. Actée le 2026-08-30.

## La direction

Replicate devient un fournisseur BYOK de plus, couvrant **la reconnaissance et la synthèse derrière la même clé** — un compte au lieu de deux, le mur du BYOK que `docs/reference.md` nomme s'abaisse d'autant. Le **LLM reste chez DeepSeek** : deepseek-v4-pro n'est pas hébergé sur Replicate, aucun modèle Replicate n'a été mesuré comme remplaçant, et le banc du juge tranchera le jour où un candidat existe. Les fournisseurs se **mélangent par brique**, librement : c'est l'état normal, pas un cas particulier. L'analyse sur l'appareil n'est pas touchée.

Écarté, avec la raison : un backend tenu par le projet (l'exploration de départ le recommandait) — renverse « aucun serveur, aucun compte, aucune consommation à la charge du projet » ; un objet bundle portant les trois briques d'un coup — soude trois maillons que le doc tient substituables un par un ; l'attribution automatique des briques par sonde — retire à l'utilisateur la préférence entre deux fournisseurs qui marchent.

## Les coutures ne bougent pas

Les trois interfaces de `chain/` sont les contrats du doc — pas de clé dans la signature, pas de panne réseau au contrat, la synthèse ne rend qu'un wav. Rien ne se crée au-dessus d'elles. Replicate s'écrit **dessous** : deux implémentations (`ReplicateRecognition`, `ReplicateSynthesis`) lisant la même entrée du `SecretStore`, et un `ReplicateClient` partagé qui porte le cycle de vie d'une prédiction (créer, attendre, télécharger) — comme `Http.kt` l'est déjà pour les trois maillons distants. La troisième implémentation suivra le jour où un modèle Replicate passe le banc du juge.

La même clé servant deux tâches, le modèle est un **paramètre par brique** (constante provisoire, comme `MODEL` chez `DeepseekConversation`), jamais un attribut de la clé.

## L'écran de réglage

Deux sections.

**Les clés** — un champ par fournisseur connu (Replicate : une clé ; Azure : clé + région ; DeepSeek : une clé ; ElevenLabs : une clé). L'endpoint de chaque fournisseur est **configurable** — une URL par défaut, un champ de remplacement — ce que `Secret.AzureSpeechRegion` fait déjà pour Azure.

**Les tâches** — trois sélecteurs : STT, LLM, TTS. Chaque liste porte les fournisseurs qui **savent faire la tâche et dont la clé est entrée**, filtre local sans aucun appel. Le choix déplie la cascade du fournisseur : le modèle, puis la voix pour le TTS. Les paramètres sont ceux que le fournisseur expose, rien de plus — pour chatterbox, la graine du rendu est épinglée par l'app (cf. mesures).

**Le catalogue se récupère à chaque ouverture de l'écran, sans cache.** Le réglage change rarement, et un catalogue en cache périt en silence — la leçon déjà mesurée du doc vaut pour les listes : ce qu'un fournisseur annonce ne correspond pas à ce qu'une clé débloque. Le fetch est **non bloquant** : chaque sélecteur porte une indication de chargement en attendant le sien. Et il **est** la sonde du doc (« un appel par fonction optionnelle ») : une clé qui rend la liste des modèles est une clé qui marche, et la liste rendue est ce que cette clé débloque vraiment. Le bouton « tester » reste par tâche, sur le fournisseur choisi ; une clé dont la sonde échoue ne disparaît pas du sélecteur — la brique est éteinte et dit pourquoi, une option indisponible porte sa raison (`docs/reference.md`).

## Ce qui change dans le code actuel

- `Secret.allPresent` suppose une chaîne fixe de clés ; dès qu'une brique a deux fournisseurs possibles, la disponibilité devient **par brique** — chaque brique sait quelles clés elle exige.
- Le choix de fournisseur par brique est de la configuration, pas un secret : même magasin, non masqué.
- chatterbox-turbo rend un **wav flottant** et une durée qui varie d'un appel à l'autre (graine aléatoire) : conversion vers le contrat 16 kHz / 16 bits mono que l'analyse consomme, et graine épinglée — la stabilité d'un rendu à l'autre est une exigence du doc pour l'étalon.
- **Le plafond des comptes pauvres** : sous 5 $ de crédit, Replicate limite à 6 prédictions par minute, rafale de 1 (mesuré : trois appels rapprochés prennent un 429). Un tour de conversation en lance une par brique, l'usage normal vit dedans ; la sonde, elle, doit espacer ses appels.

## Les mesures (2026-08-30)

Un texte de 55 caractères, une voix (Abigail pour le turbo), téléchargement mesuré depuis le poste — pas depuis le téléphone —, appels espacés de 16 s à cause du plafond ci-dessus. Instrument : `tmp/bench/replicate_latency.py`, à reloger comme les autres sondes.

| | prédiction finie | premier octet audio |
|---|---|---|
| chatterbox-turbo, premier appel | 1,84 s | 2,31 s |
| chatterbox-turbo, chaud | 1,80–3,29 s | 2,42–5,41 s |
| chatterbox, premier boot | 6,73 s | 9,28 s |
| chatterbox, chaud | 3,92–8,45 s | 4,38–9,04 s |
| deepseek-v4-pro (LLM) | premier jeton 12,5 / 24,9 s | objet complet 13,0 / 25,4 s |

Deux lectures : le **turbo tient le territoire d'Azure** (~1,2 s mesuré sur l'appareil) ; et **ce n'est pas le TTS qui casse le budget des 2,6 s — c'est déjà le LLM** à raisonnement. La question de latence du TODO ne bouge pas.

Ce que ces chiffres ne couvrent pas : un texte, une voix, le réseau du poste ; le « froid » du turbo à 1,84 s est suspect (modèle probablement déjà démarré), à ne pas généraliser ; le **STT** (whisper sur Replicate) n'est pas mesuré du tout ; la stabilité voix par voix et l'étalonnage relèvent du test de voix du chantier 2, toujours dus.
