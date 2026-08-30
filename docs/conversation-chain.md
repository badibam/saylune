# La chaîne de conversation — latence mesurée

Le chantier 2 demandait si la latence cumulée d'une chaîne STT → LLM → TTS est acceptable, là où une API voix-à-voix la rend native. Ça ne se discute pas, ça se mesure. Ce document porte les nombres ; la décision qu'ils appuient vit dans `reference.md`.

Maillons mesurés : Azure Speech pour la reconnaissance et la synthèse, DeepSeek (`deepseek-chat`) pour le modèle. Le banc est `tmp/bench/latency.py`. Ce ne sont pas des maillons retenus — ce sont ceux dont on a la clé, et la forme du résultat compte plus que le fournisseur.

## La méthode

On mesure des primitives et on dérive les régimes, plutôt que de chronométrer un régime et de ne rien savoir des autres. Chaque maillon est appelé en flux, et on note le premier jeton, la première phrase **achevée**, et la fin.

L'appel au modèle porte la vraie charge, pas un appel d'essai : il répond **et** reconstruit le texte de référence dans un seul objet json — c'est exactement l'argument qui faisait préférer la chaîne au voix-à-voix. Le champ `reply` est le premier de l'objet à dessein : un lecteur en flux peut donner la phrase d'ouverture au synthétiseur pendant que les champs d'analyse arrivent encore.

## Un tour court

Tour de 3,7 s (« Yesterday I go to the market. »), médiane de trois passes, en secondes.

| segment | médiane |
|---|---|
| STT, reconnaissance | 1,04 |
| LLM, premier jeton | 0,81 |
| LLM, première phrase achevée | 0,96 |
| LLM, objet complet | 1,11 |
| TTS, premier octet | 0,40 |
| TTS, audio complet | 0,85 |

Deux régimes en découlent :

| régime | premier son |
|---|---|
| naïf — chaque maillon finit avant que le suivant commence | **2,63** |
| pipeliné — la synthèse démarre sur la première phrase achevée | **2,47** |

**Le pipelinage ne rachète que 0,16 s, et ne vaut pas sa complexité.** La raison est que le modèle finit son objet 0,15 s après avoir fini sa première phrase : à cette longueur de réponse, il n'y a rien à recouvrir. L'écart ne s'ouvrirait que sur des réponses longues, que la pression conversationnelle ne vise pas.

Le jeton d'authentification du TTS (0,39 s) est mesuré à part et exclu : il vaut dix minutes chez Azure, donc il ne se paie pas au tour.

## La reconnaissance croît avec la durée du tour

Reconnaissance seule, même méthode :

| audio | STT |
|---|---|
| 3,7 s | 1,02 |
| 6,2 s | 1,43 |
| 12,0 s | 2,38 |
| 22,7 s | 4,10 |
| 36,2 s | 6,49 |

La relation est linéaire et sans surprise : **environ 0,40 s de constante plus un sixième de la durée de l'audio**. L'API REST ne transcrit pas pendant qu'on parle — elle reçoit un fichier fini et le traite.

**Le régime par fichier est retenu quand même.** Le tour de trente secondes est l'exception, pas le régime nominal, et la reconnaissance en flux se paierait d'une exigence neuve sur le maillon STT et d'un montage plus lourd — pour un inconfort dont rien ne dit encore qu'il gêne à l'usage. La pente est connue et acceptée ; c'est l'usage réel qui rouvrira la question, s'il la rouvre.

## Les maillons Replicate, mesurés à part (2026-08-30)

Le jour où Replicate est entré comme fournisseur, la question était : un maillon hébergé chez lui tient-il ce budget. Instrument : `tmp/bench/replicate_latency.py` et `tmp/bench/replicate_stt.py`, à reloger comme les autres sondes. Depuis le poste, pas depuis le téléphone. Appels espacés de 16 s : sous 5 $ de crédit, Replicate limite à six prédictions par minute, rafale de 1 — trois appels rapprochés prennent un 429.

Synthèse, un texte de 55 caractères, voix `Abigail` pour le turbo :

| | prédiction finie | premier octet audio |
|---|---|---|
| chatterbox-turbo, premier appel | 1,84 s | 2,31 s |
| chatterbox-turbo, chaud | 1,80–3,29 s | 2,42–5,41 s |
| chatterbox, premier boot | 6,73 s | 9,28 s |
| chatterbox, chaud | 3,92–8,45 s | 4,38–9,04 s |

Reconnaissance, trois prises réelles de 3 à 6 s enregistrées sur l'appareil, de la requête au téléchargement fini :

| | temps | bornes des mots |
|---|---|---|
| `victor-upmeet/whisperx`, `align_output` | 4,25 / 4,44 / 7,84 s | oui, mot par mot |
| `openai/whisper` | 2,60 / 3,41 s | non, segments seulement |

Et le modèle de langue, même charge que celle de l'app : `deepseek-v4-pro` rend son premier jeton à 12,5 et 24,9 s, son objet complet à 13,0 et 25,4 s.

**Deux lectures.** Le turbo tient le territoire d'Azure (1,2 s mesuré plus haut, sur l'appareil). Et **ce n'est pas la synthèse qui casse le budget des 2,6 s — c'est le modèle de langue à raisonnement**, d'un ordre de grandeur au-dessus de tout le reste réuni. Le choix entre les deux reconnaissances, 4 s contre 3 s, se joue donc sur les bornes des mots et pas sur le temps.

Ce que ces chiffres ne couvrent pas : un texte, une voix, le réseau du poste ; le « froid » du turbo à 1,84 s est suspect — le modèle était probablement déjà démarré — et ne se généralise pas ; les trois prises de reconnaissance disent la même phrase, donc rien de la fidélité verbatim sur de la parole variée ; la stabilité voix par voix et l'étalonnage relèvent du test de voix du chantier 2, toujours dus.

## Portée de la mesure

- Connexion de bureau, pas mobile. Trente-six secondes de PCM 16 kHz font 1,16 Mo, et le téléversement est dans ces nombres — sur un lien mobile la pente s'accentue, elle ne s'aplatit pas.
- Trois passes par point. L'écart entre passes reste sous 0,15 s partout sauf sur la première reconnaissance de chaque série, où l'établissement de la connexion se paie une fois.
- Un seul fournisseur par maillon. La forme du résultat — un STT proportionnel à la durée, un modèle qui achève sa phrase presque en même temps que son objet — est ce qu'on retient ; les valeurs bougeront avec les fournisseurs retenus.
