# Les fournisseurs — ce qu'ils font, ce qu'ils coûtent

Les maillons distants de la chaîne — reconnaissance, modèle de langue, synthèse — sont substituables et se choisissent brique par brique (`reference.md`). Ce doc porte ce qu'on a mesuré d'eux et ce qu'ils exposent. Aucun n'est choisi : le chantier 2 les départagera par un banc, pas par un tableau de prix (`../TODO.md`).

## La latence

**Le fil est refermé pour le moment** sur ElevenLabs en direct pour la synthèse et un modèle de langue au cran d'effort bas. Ce n'est pas un choix imposé — le doc laisse la main à l'utilisateur — c'est ce sur quoi la suite du travail se fait.

**L'instrument** : chaque tour gardé porte ses temps dans `turn.json`, champ `steps`, un nom et un moment par étape depuis le début du tour. Il fallait qu'il existe pour que les leviers se comparent — le logcat, où les temps allaient, ne garde que **81 s** sur cet appareil, donc un tour y était effacé avant d'être lu. Deux points de mesure sont posés à des endroits qui comptent : le **premier son**, à l'instant où le premier échantillon sort (`Playback.play`, paramètre `started`) — ni la fin de l'appel de synthèse, trop tôt du transcodage et de la préparation du lecteur, ni le retour de la lecture, trop tard de toute la longueur de la réponse ; et le **début de l'analyse**, sans quoi son coût se lisait comme celui de la synthèse qui la précède.

### Sur l'appareil, contrat par contrat

| maillon | contrat d'origine (8 tours, 2026-08-31) | contrat enrichi (10 tours, 2026-09-06) |
|---|---|---|
| reconnaissance | 2,5 s (0,9 – 10,9) | 1,8 s (0,6 – 4,3) |
| modèle de langue | 4,4 s (3,2 – 6,9) | **13,2 s** (5,0 – 47,5) |
| synthèse → premier son | 2,7 s (0,3 – 6,8) | 1,4 s (1,3 – 1,7) |
| **jusqu'au premier son** | **11,0 s** (8,4 – 18,4) | **18,6 s** (9,4 – 51,9) |
| analyse sur l'appareil | 1,15 s | 2,9 s (1,0 – 4,9) |

Médianes, SM-G975F. Le premier contrat rendait une réponse, `intended` et un booléen ; le second ajoute trois marquages, la difficulté du tour, l'écho de reprise et le choix de menu, dont les empans qui pèsent à peu près la moitié de l'historique.

**Le contrat enrichi a triplé le maillon du modèle de langue**, et c'est tout ce que la mesure dit : le modèle, la route et le fournisseur n'ont pas bougé. Les deux autres maillons se sont améliorés, ce qui vient du choix ElevenLabs en direct et pas du contrat. L'analyse a doublé, ce qui est attendu — elle tourne après le premier son, hors de la latence ressentie, mais elle croît avec la longueur du tour.

**La cause n'était pas dans le contrat seul : l'app ne demandait aucun effort, et DeepSeek raisonne à `high` par défaut.** Sa doc le dit noir sur blanc (relevé le 2026-09-06) : sur l'endpoint de forme OpenAI, `thinking` vaut `enabled` et `reasoning_effort` vaut `high` si on n'envoie rien. **Ne rien envoyer n'est donc pas un défaut neutre, c'est le cran le plus cher.** Le levier est posé depuis (`providers/Effort.kt`).

**Deux tours sur `openai/gpt-5-mini` en direct au cran `low`** : maillon de langue 7,2 et 5,1 s, premier son **11,1 et 9,0 s**. Le premier son revient donc à peu près là où il était **avant** l'enrichissement, sur un contrat plus lourd. **Ce que ces deux tours ne disent pas est l'essentiel : trois choses ont bougé en même temps** — le fournisseur, le modèle et le cran. Aucun ne peut se voir créditer du gain, et deux tours ne couvrent aucune variance.

**Le pic tombe sur le tour fautif** — celui qui compte. Le raisonnement de `deepseek-v4-pro` l'explique : 83, 146, puis 638 jetons, l'essentiel dépensé à se demander si un mot était une erreur de reconnaissance.

### Le travail d'app entre les maillons

Trois trous silencieux séparaient les appels — 262 ms avant la reconnaissance, 365 avant le modèle, 726 avant la synthèse — sans une once de réseau dedans. C'était le **magasin de secrets** : le lire déchiffre chaque entrée, et un tour le lit **dix fois**, soit cent quarante déchiffrements par tour sur quatorze entrées.

**Deux corrections, et la première était la mauvaise moitié.** Garder la clé du Keystore pour la durée du processus n'a presque rien donné : les lectures coûtaient encore 92 à 241 ms. La clé est adossée au matériel, donc **chaque opération de chiffrement** est son propre aller-retour — garder la clé épargne la recherche et laisse les quatorze opérations. Ce qui a réglé la chose est de **ne pas redéchiffrer le même instantané**, le cache étant indexé sur l'identité de l'objet que DataStore rend : il est immuable et toute écriture en fabrique un neuf, donc rien de périmé ne peut être relu.

**Mesuré après** : 4 ms, 5 ms, 138 ms. Le travail d'app sur le chemin critique passe d'environ 1 350 ms à environ 150. Les 138 ms restants sont les deux écritures Room de l'énoncé et de la réponse plus l'état du pipeline ; les déplacer après le lancement de la synthèse échangerait 138 ms contre un tour qui ne survit pas à un plantage.

### Ce que le banc disait avant l'appareil

Le banc de poste (`tmp/bench/latency.py`, Azure + DeepSeek) donnait **2,63 s jusqu'au premier son** en régime naïf et **2,47 s** en pipeliné. L'appareil dit tout autre chose, et c'est lui qui compte ; deux résultats du banc survivent quand même.

**Le pipelinage de la synthèse ne rachète que 0,16 s**, parce que le modèle achève son objet un septième de seconde après sa première phrase : à cette longueur de réponse, il n'y a rien à recouvrir. **Ce chiffre ne borne rien sous le contrat enrichi** — il a été mesuré pour cette décision-là, sur le contrat d'alors.

**La reconnaissance par fichier coûtait un sixième de la durée de l'audio**, plus 0,40 s de constante, mesuré de 3,7 à 36,2 s. **L'appareil dit autre chose** : 1,8 s d'audio a coûté 10,9 s, 4,8 s en a coûté 7,2 et 2,2 s en a coûté 0,9. Ce qu'on paie n'est pas la longueur, c'est l'aller-retour, et sa variance est le vrai défaut. Ça ne condamne pas le fichier contre le flux ; ça retire l'argument de la durée du débat, et ça renforce la destination locale de ce maillon, qui la supprimerait entièrement.

### Une route coûte plus qu'un modèle

Mesuré le 2026-09-01 par le balayage de l'instrument, une seule répétition, donc **aucune variance couverte**. Une comparaison y est contrôlée et une seule : les **mêmes modèles ElevenLabs joints par deux routes**.

| | court | long |
|---|---|---|
| `flash-v2.5` en direct | 1,04 s | 0,66 s |
| `flash-v2.5` par Replicate | 8,94 s | 11,57 s |
| `turbo-v2.5` en direct | 0,45 s | 0,65 s |
| `turbo-v2.5` par Replicate | 9,54 s | 11,09 s |
| Azure | 0,51 s | 0,64 s |

Le modèle est le même des deux côtés, donc ce qui sépare les colonnes est la route. Côté modèle de langue, DeepSeek en direct donne 4,54 / 5,78 (`flash`) et 9,17 / 5,29 (`pro`), Replicate 13,71 / 11,69 (`gpt-5-mini`) et 12,13 / 11,95 (`gpt-5-nano`). `chatterbox-turbo` fait 2,44 en court et 11,20 en long, écart qu'aucune longueur de texte n'explique ; `chatterbox` ne liste aucune voix.

**Aucun modèle de langue n'a été joint par les deux routes**, donc que le surcoût de route vu sur la synthèse s'y retrouve est une déduction, pas un chiffre. **OpenAI est fournisseur direct de conversation depuis le 2026-09-06**, avec les deux mêmes modèles que Replicate offre, précisément pour qu'un modèle soit joignable des deux façons. Et la **reconnaissance n'a rien rendu du tout** — l'échantillon à faire entendre n'a pas été synthétisé.

**Les maillons Replicate mesurés à part** (2026-08-30, depuis le poste). Synthèse d'un texte de 55 caractères : `chatterbox-turbo` rend son premier octet audio en 2,3 à 5,4 s à chaud, `chatterbox` en 4,4 à 9,0 s, avec un boot à froid de 9,3 s. Reconnaissance sur trois prises réelles de 3 à 6 s : `victor-upmeet/whisperx` avec `align_output` en 4,25 / 4,44 / 7,84 s, bornes mot par mot ; `openai/whisper` en 2,60 / 3,41 s, segments seulement. Le choix entre les deux se joue donc sur les bornes et pas sur le temps.

### Les crans d'effort

**Ce qui n'est pas sourcé, côté OpenAI** : ses crans ne sont publiés nulle part d'exploitable — `/v1/models` ne rend que id, created et propriétaire, sans aucune capacité, et le guide de raisonnement dit seulement que « certains modèles n'en acceptent qu'un sous-ensemble, va voir la page du modèle », laquelle ne les liste pas non plus. Le vocabulaire entier est `none · minimal · low · medium · high · xhigh · max` ; le catalogue de l'app n'offre que `low · medium · high`, les trois que toute la famille gpt-5 porte depuis son lancement (relevé le 2026-09-06). **Le plancher coûte quelque chose de réel** : `none` serait le cran le plus rapide et cette route existe pour la latence. À rouvrir le jour où une page de modèle liste ses propres crans.

**Ce qui n'est pas mesuré** : les crans de Replicate ne sont pas déclarés — ses modèles sont ceux d'OpenAI, dont Replicate publie le `reasoning_effort` dans le schéma de chaque modèle, et rien ne l'a lu ; l'appel envoie `low` en dur en attendant, donc aucun défaut de personne n'est payé en silence, mais le choix n'est pas offert.

### Le plafond des comptes Replicate pauvres

**Ce n'est pas le débit qui gêne, c'est la rafale.** Sous 5 $ de crédit : 6 prédictions par minute, **rafale de 1**. Mesuré sur l'appareil, un tour dépense deux synthèses — la réponse à dire, puis le modèle d'`intended` — et elles partent à 5,7 s d'écart, la seconde prenant un 429 avec `retry_after: 5`. Un cycle de tour complet fait 25 à 30 s, donc environ 2 tours par minute : le débit de 6 tient largement, et c'est la rafale de 1 qui force une attente à chaque tour. Le client attend maintenant ce que le serveur annonce, ce qui répare la perte de marques. Deux choses à savoir avec : le levier est le crédit du compte, pas le code ; et le bouton d'écoute des voix dépense une prédiction par essai.

### Portée des mesures de banc

Connexion de bureau, pas mobile — trente-six secondes de PCM 16 kHz font 1,16 Mo, et le téléversement est dans ces nombres, donc sur un lien mobile la pente s'accentue. Trois passes par point, l'écart restant sous 0,15 s partout sauf sur la première reconnaissance de chaque série. Un seul fournisseur par maillon : c'est la **forme** du résultat qu'on retient, pas les valeurs.

## Chatterbox (Resemble AI)

Source : dépôt officiel `resemble-ai/chatterbox`, README et code (`tts.py`, `tts_turbo.py`, `mtl_tts.py`).

**Chatterbox classique** (anglais, 500M) — `generate(text, audio_prompt_path, exaggeration=0.5, cfg_weight=0.5, temperature=0.8, repetition_penalty=1.2, min_p=0.05, top_p=1.0)`. `exaggeration` ↑ donne un débit plus rapide et plus expressif, `cfg_weight` ↓ un débit plus lent : les deux se compensent, et le réglage recommandé pour du dramatique est `cfg_weight≈0.3` avec `exaggeration≈0.7+`. Astuce officielle : si le clip de référence n'est pas dans la langue du texte, `cfg_weight=0` évite que son accent contamine la sortie.

**Chatterbox-Turbo** (anglais, 350M) — `generate(text, audio_prompt_path, temperature=0.8, top_p=0.95, top_k=1000, repetition_penalty=1.2, norm_loudness=True)`. **Pas de `exaggeration` ni `cfg_weight`** : Turbo désactive `emotion_adv` et `use_perceiver_resampler` pour gagner en vitesse, et tout le contrôle expressif passe par les balises textuelles. La variante **Nano** est la même classe chargée avec `nano=True`.

**Chatterbox-Multilingual** (23 langues) — mêmes paramètres que le classique plus `language_id` obligatoire. Bug connu (issue #355) : `exaggeration` a un effet quasi nul sur les checkpoints v1/v2, la couche `emotion_adv_fc` étant sous-dimensionnée ; non confirmé réparé sur v3.

**Pas de SSML, pas de contrôle par mot.** Seule convention supportée, native sur Turbo : des **balises paralinguistiques entre crochets** dans le texte — `[clear throat] [sigh] [shush] [cough] [groan] [sniff] [gasp] [chuckle] [laugh]`.

Une fonction interne `punc_norm()` **nettoie la ponctuation avant synthèse** — remplace `...`, tirets et guillemets typographiques, ajoute un point final si absent. Non paramétrable, et ça explique certains ajustements silencieux du texte source.

**`chatterbox-turbo` normalise son intensité à -27 LUFS et Replicate n'expose aucune entrée pour l'en empêcher.** C'est un traitement d'un seul côté des deux enregistrements, ce que le projet interdit comme règle. **L'effet sur la mesure n'est pas mesuré** : la comparaison lit des répartitions de ressemblance et non des niveaux, donc il se peut que ça ne coûte rien — et « il se peut » n'est pas un résultat. Se mesure sans donnée neuve : le même texte rendu par une voix Azure et par le turbo, les deux grilles comparées.

## ElevenLabs

### Les modifieurs de livraison

Rien chez ce fournisseur ne transforme une voix en une autre : pas de décalage de hauteur, pas de vieillissement, pas de bascule de genre. Ce qui existe modifie la **livraison**.

- **Les réglages par requête** — `stability`, `similarity_boost`, `style`, `use_speaker_boost`, `speed`. Ils ne coûtent aucun emplacement et se posent par personnage, mais ils ne fabriquent pas d'identité : **deux personnages ne sortiront jamais d'une seule voix**.
- **Les balises audio du modèle v3**, inline dans le texte — `[whispers]`, `[shouts]`, `[laughs]`, `[sarcastic]`. C'est du modifieur **par réplique**, donc le grain d'un jeu.
- **Les dictionnaires de prononciation**, référencés par appel. Utiles pour un nom inventé, et **à réserver à la voix du personnage** : en imposer un à la voix modèle reviendrait à juger l'apprenant contre un dictionnaire.

**Le piège** : ces modifieurs se répartissent à l'envers de ce qui arrange. Les balises expressives vivent en v3, quand le fil de latence s'est refermé sur `flash-v2.5`, le plus rapide et le plus pauvre. **La latence de v3 en direct n'est pas mesurée** — et c'est ce chiffre qui dit si un personnage joué tient dans une conversation ou seulement hors du fil.

### Les plafonds du BYOK, si la bibliothèque communautaire s'ouvrait un jour

Le montage retenu ne les rencontre pas : **l'app n'ajoute jamais de voix** (`activity.md`, « La voix »). Ils restent au dossier pour le jour où quelqu'un rouvrirait cette porte.

Une voix de la bibliothèque a une **identité globale**, un couple `public_user_id` + `voice_id`, mais elle n'est pas appelable telle quelle : `POST /v1/voices/add/{public_user_id}/{voice_id}` la fait entrer dans la collection de qui l'appelle et rend un **identifiant local au compte**, qui est celui que la synthèse consomme.

- **Les emplacements de voix** : 3 sur le plan gratuit, 10 sur Starter, 160 sur Pro (lu hors documentation officielle). Une voix ajoutée en consomme un.
- **Le plan gratuit ne peut pas ajouter de voix de la bibliothèque** — donc BYOK gratuit égale aucun personnage à voix. Noté dans `../NOTES.md`, jamais retrouvé en source.
- **L'ajout écrit dans le compte de quelqu'un d'autre** : des voix qu'il n'a pas choisies apparaissent dans sa bibliothèque. C'est un effet de bord qui se demande, pas une lecture.
- **La sélection pourrit** : un auteur peut retirer sa voix, et l'ajout échoue alors pour tout nouvel utilisateur, silencieusement.

C'est de là que venait la forme « un personnage porte une liste classée de couples » : le premier disponible gagne, et le personnage garde son identité pendant que sa voix dégrade. Elle n'est plus nécessaire, les définitions ne nommant aucune voix générique.

### Ce que l'hébergement coûterait

Si la clé était celle du projet, tous les plafonds ci-dessus tombent : un plan Pro donne 160 emplacements sur un seul compte, la sélection est identique pour tout le monde, personne ne voit sa bibliothèque écrite.

**Le coût est le point dur.** La synthèse est presque tout : un tour dépense la réponse à dire plus `intended`, environ 300 caractères, quand la reconnaissance et le modèle de langue se comptent en millièmes. Facturé **0,10 $ les 1000 caractères, tarif plat sur tous les plans**, ça fait **3 centimes le tour**, 0,90 $ une séance de trente tours, de l'ordre de **18 $ par mois** pour vingt séances. Le coût marginal est trop haut et trop variable pour être forfaitisé : **des crédits, pas un abonnement**.

Ce qui pèse plus que l'argent : des comptes, de l'authentification et de la facturation, là où l'app n'en a aucun ; du contrôle d'abus, obligatoire dès que les sources sont publiques ; une disponibilité qui devient la sienne ; et si l'audio transite, la responsabilité de traitement d'une voix d'apprenant sur une app où des mineurs sont probables. Le code, lui, encaisse : un service hébergé n'est **qu'une entrée de plus dans `Provider`**, dont le `needs` est un jeton de compte au lieu d'une clé.

Deux conséquences se posent maintenant parce qu'elles coûtent cher rétroactivement. **`TetheredNet` se déclare** — F-Droid ne l'interdit pas, il exige la déclaration, et une anti-feature non déclarée est un motif de rejet. Et **le paiement se fait hors de l'app** : Google Play Billing est une dépendance propriétaire que `fdroid` interdit sans condition.

### Le jeton éphémère, et ce qu'il ne donne pas

L'audio n'a pas à transiter. Un **courtier de jetons** suffit : le serveur garde la clé, le client s'authentifie chez lui, reçoit un jeton court, et appelle le fournisseur en direct. Vérifié en source — l'endpoint existe, rend un jeton valable **15 minutes et consommé à l'usage**, et prend un `token_type` parmi `realtime_scribe`, `batch_scribe` et **`tts_websocket`**.

- **L'app ne porte aucun secret**, seulement un jeton qu'elle a obtenu. Un service payant reste compatible avec des sources publiques, ce qu'une clé embarquée interdit absolument.
- **Le jeton ne vaut que sur le websocket**, pas sur le `POST` HTTP qu'utilise `ElevenLabsSynthesis` : c'est une réécriture, et la doc du fournisseur avertit qu'envoyer tout le texte d'un coup y est *légèrement plus lent* qu'une requête HTTP ordinaire.
- **Le contrat d'entrée survit** : les formats du websocket comprennent `pcm_16000`, et seul `pcm_44100` demande un plan Pro. C'était le point qui pouvait tuer le montage.

**Ce que le jeton retire, c'est le compteur.** Le client appelant en direct, la consommation ne se voit qu'en agrégat sur la facture. La parade propre serait une clé par abonné avec un plafond mensuel : l'endpoint existe et prend un `character_limit`, mais il vit sous les *service accounts*, **réservés aux espaces multi-sièges, donc à partir de Scale (299 $/mois)**. En dessous, **le seul compteur est l'émission des jetons**, et sa granularité est la connexion — d'où un arbitrage non tranché : une connexion tenue ouverte pour toute la séance sert la latence mais ne compte que des séances de longueur inconnue ; **un jeton par réplique** rend le bon grain et un vrai robinet, au prix d'un aller-retour chez soi avant chaque son. La stratégie de connexion cesse donc d'être une question de latence, c'est celle du compteur, et les deux tirent en sens contraire.

## Deux choses qui valent pour tous

**La version d'un modèle Replicate n'est pas épinglée.** Elle est résolue au premier appel et gardée le temps du processus, ce qui évite un aller-retour par tour sans figer une empreinte que l'utilisateur n'a pas choisie. Le prix est réel et porte sur l'étalon : un modèle republié sous l'app rend une autre voix que celle contre laquelle la mesure a été lue, et rien ne le signalera. À trancher avec le test de voix.

**La reconnaissance envoie l'audio en data-URI dans le corps de la requête.** Simple et suffisant sur des tours de quelques dizaines de kilo-octets ; l'API de fichiers de Replicate existe à côté et coûte un aller-retour de plus. Le plafond de taille n'est pas mesuré, et il se rencontrera sur un tour long avant partout ailleurs.
