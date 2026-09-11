# L'analyse sur une machine distante

Doc transitoire, ouvert le 2026-09-10. Il porte deux choses : **ce qu'une passe d'analyse coûte sur une machine qui n'est pas le téléphone**, et **comment remonter la machine qui l'a rendu**. Il s'élague quand la question de déporter est tranchée — ce qui reste alors vit dans `../analysis.md`.

Rien ici ne décide de déporter quoi que ce soit. Ce sont des mesures, prises pour que la question se pose sur des chiffres.

## Où la coupe tomberait, si elle tombait

L'analyse n'est pas un bloc : **une seule brique coûte, la passe du réseau** (brique 2 d'`../analysis.md`). Tout le reste — grille, jointure, alignement, recouvrement, mélodie, contrôle — est de l'arithmétique au-dessus de la matrice, mesurée négligeable. Ce qui se déporterait est donc étroit : un audio monte, une matrice descend, et la marque, le seuil et la note restent sur l'appareil. Aucun jugement ne voyage, aucune norme extérieure n'entre par là.

Deux contraintes déjà écrites bornent toute forme à venir, et elles ne se négocient pas ici. **Les deux audios passent par la même machine, ou aucun** : le biais de la machine ne s'annule que s'il est le même des deux côtés (`../reference.md`), donc une prise lue à distance et un modèle lu sur l'appareil fabriqueraient une partie de l'écart mesuré. Et **le choix est relu à chaque tour, comme le sont les trois autres maillons, et une prise en réglage vaut dès le tour suivant.** Ce qui reste interdit, c'est le mélange **à l'intérieur d'un tour** : les deux audios passent par le même moteur, un changement en reconstruit un neuf avant de lire, et chaque tour porte l'estampille du moteur qui l'a produit — deux époques dans un même fil sont donc visibles, jamais silencieuses.

## Ce qui est mesuré

Même graphe, même préparation, même runtime des trois côtés ; seule la machine change. `bench/cost.py` est l'instrument, `bench/phone.py` a rendu la colonne du téléphone.

**Entiers 8 bits, 4 fils :**

| tour | serveur `c3-8` | poste i7-10510U | SM-G975F |
|---|---|---|---|
| 3 s | 0,28 s (×0,09) | 0,34 s | 0,96 s (×0,32) |
| 6 s | 0,56 s (×0,09) | 0,76 s | 2,26 s (×0,38) |
| 12 s | 1,23 s (×0,10) | 1,84 s | 5,17 s (×0,43) |
| 20 s | 2,41 s (×0,12) | 3,60 s | 9,9 s (×0,50) |
| 30 s | 4,15 s (×0,14) | 7,78 s | 17,4 s (×0,58) |

**Le serveur va de 3,4 à 4,2 fois plus vite que le téléphone**, et 1,5 fois plus vite que le poste.

**Les fils, sur le serveur** (8 bits) : 1,61 s sur un fil à 6 s, 1,01 sur deux, 0,56 sur quatre — 2,9 fois sur quatre cœurs. **Le chiffre par cœur est celui qui dimensionne** : un cœur traite un tour de 6 s en 1,61 s.

**Et les fils logiques ralentissent la passe** : mesuré sur le poste, 1,19 s à huit fils sur quatre cœurs physiques contre 0,76 s à quatre. Ce qui s'achète est un cœur physique, jamais un compte de vCPU.

**L'arrondi 8 bits gagne aussi sur x86 sans VNNI** (serveur, 4 fils) : 0,56 s contre 0,81 s en flottant à 6 s, 4,15 contre 5,31 à 30 s — de 28 à 45 % plus rapide, pour 359 Mo de poids au lieu de 1266. La recette qualifiée sur le téléphone se transporte donc, ce qui n'était jusque-là qu'une supposition tirée d'une mesure ARM.

**La mémoire est une propriété du graphe, pas de la machine** : 2218 Mo de pic à 30 s sur le serveur contre 2201 sur le poste. Lue au-dessus des 359 Mo de poids, elle donne la règle de dimensionnement : **environ 0,4 Go d'activations par tour de 6 s traité en même temps, 1,8 Go par tour de 30 s**. Les poids se partagent entre requêtes simultanées, les activations non.

**Le ré-export ne coûte rien à la comparabilité.** Le graphe a été refabriqué sur le serveur plutôt qu'envoyé, et `export.py` s'est prouvé lui-même : préparation numpy contre l'extracteur à 0,00e+00, graphe flottant contre la lecture PyTorch à 4,05e-06 — sous la barre de 1e-3 qu'il refuse de franchir —, et les mêmes tailles de fichier au mégaoctet près. Les versions de bibliothèque diffèrent pourtant (torch 2.14 et transformers 5.17 sur le serveur, 2.13 et 5.15 sur le poste).

## La latence, mesurée sur l'appareil

Le montage a tourné en vrai le 2026-09-10 : l'app parle au serveur, les deux audios d'un tour y passent, les marques reviennent. Ce que ça coûte, lu dans la trace du téléphone contre le journal du serveur :

| | audio | passe serveur | total | réseau |
|---|---|---|---|---|
| tour court, redite | 2,1 + 2,6 s | 0,44 s | 4,9 s | ~4,5 s |
| tour long | 8,5 + 21,3 s | 3,65 s | 32,5 s | ~28,8 s |
| tour long, redite | 8,5 + 13,2 s | 2,32 s | 54,9 s | ~52,6 s |

**Le calcul tient la promesse du banc — ×0,10 de la durée de l'audio — et il ne pèse que 4 à 7 % du temps.** Tout le reste est du transfert, sur un lien montant mesuré entre 120 et 300 kbit/s. En prolongeant les chiffres du téléphone, le tour long aurait coûté ~14,6 s en local contre 32,5 s ici : **le distant est aujourd'hui deux à six fois plus lent que l'appareil qu'il devait soulager.**

Ça ne condamne pas le montage, ça déplace ce qu'il faut travailler : **l'envoi, jamais les cœurs.** Trois choses s'y attaquent, elles se cumulent, et aucune ne touche à ce que le réseau entend.

### 1. Envoyer pendant que la personne parle

L'envoi commençait quand le tour était fini, alors que le lien montant n'avait rien fait pendant les vingt secondes de parole. **C'est écrit le 2026-09-11** (`analysis/StreamedTake.kt`, `server/serve.py`) : la prise part par morceaux d'une seconde, chacun portant sa position, et le serveur répond ce qu'il tient. Un morceau perdu se renvoie donc depuis là, et une pause ne tient rien ouvert. À la fermeture partent la dernière seconde, la longueur et l'empreinte des échantillons, et un serveur qui tient autre chose refuse au lieu de lire.

**Et la passe de la prise sort du chemin critique avec son envoi** : le serveur la lance dès la fermeture, pendant que le modèle de langue et la synthèse travaillent. Après la synthèse il ne reste que le modèle, et sur une redite il est gardé (n° 2).

Vérifié sur le poste, sur une prise de 33,3 s : la matrice rendue par morceaux est la même au bit près que celle de l'envoi entier ; un morceau renvoyé ne change rien ; un morceau après un trou est refusé puis renvoyé d'où le serveur le dit ; une seconde fermeture rend 404, une prise incomplète 409. **Rien de ça n'a tourné sur le téléphone, et le gain n'est pas mesuré.** Le serveur tient désormais un état : les prises inachevées, seize au plus, oubliées au bout de dix minutes.

Deux prix, à écrire maintenant. Une prise qu'on jette aura été envoyée pour rien, ce qui est un levier existant qui devient payant. Et le silence part au réseau comme le reste, ce que le tuyau A assume déjà.

### 2. Garder la matrice du modèle avec sa synthèse

Le modèle était réexpédié à chaque redite : mesuré, `8,5 s → 0,92 s` puis `8,5 s → 0,88 s` pour le même audio. **C'est écrit le 2026-09-11** (`analysis/ModelReadings.kt`) : la lecture du modèle est gardée dans le cache des synthèses, nommée par les octets du wav et l'estampille du moteur, donc une lecture d'ici et une de là-bas ne se confondent pas. Elle sert aussi en local, où le téléphone refaisait la passe du modèle à chaque redite. Elle tombe sous le même plafond que les wav, et avec eux.

**Elle est gardée repliée** : un nombre par trame pour la couche 19, la forme que le serveur renvoie déjà. Entière, la couche d'une phrase de 6 s pèse 1,2 Mo, douze fois le wav. Sur une redite locale le modèle est donc replié et la prise entière, le même calcul dans deux ordres. Mesuré avant d'écrire, sur les 96 prises du jeu d'essai et 28 500 mots aux syllabes tirées au hasard : aucune syllabe élue ne change, et aucun score ne bouge de plus de 9,5e-07. Les syllabes tirées ne sont pas celles de vrais mots, ce qui ne compte pas pour un arrondi. Ce qui a été gardé est aussi ce qui sert au premier tour, donc un modèle rend les mêmes chiffres à chaque tour mesuré contre lui.

Rien de tout ça n'a encore tourné sur le téléphone.

### 3. μ-law 8 bits sur ce qui monte

Deux fois moins d'octets, et c'est le seul mécanisme de perte qui a survécu à la mesure.

**Opus tombe, à tous les débits testés.** La bande vide qui sépare la plus faible faute du pire témoin s'effondre de 0,028 à 0,002 — fautes et témoins se rapprochent d'un facteur dix, donc la mesure cesse de trancher ce qu'elle existe pour trancher. La cause est nommable : un codec de parole jette le détail spectral fin, qui est exactement ce que ce réseau lit.

**Le μ-law tient**, parce qu'il ne fait rien de tel — une loi d'échelle échantillon par échantillon, sans transformée, sans bande supprimée, dont l'erreur est un bruit large bande. Il n'a en plus ni rééchantillonnage ni amorce, donc l'audio revient sur les échantillons dont il est parti.

Lu sur le jeu d'essai étiqueté, ce que la profondeur coûte, plancher des témoins en tête parce que c'est le seul chiffre monotone :

| | pire témoin | plus faible faute | bande vide |
|---|---|---|---|
| PCM 16 bits | 0,003 | 0,031 | 0,028 |
| μ-law 8 bits | 0,004 | 0,021–0,027 | 0,017–0,023 |
| μ-law 7 bits | 0,010 | 0,011–0,017 | 0,001–0,007 |
| μ-law 6 bits | 0,013 | 0,026–0,154 | 0,012–0,141 |

**Les poids complets ne rachètent pas ce que la compression coûte** (mesuré le 2026-09-10, la question s'étant posée parce que le serveur, lui, n'a aucune raison de porter l'arrondi du téléphone). Sous μ-law 8 bits, le plancher des témoins est de 0,004 avec les poids arrondis **comme avec les poids complets** : identique. La raison est mécanique — le μ-law met du bruit dans le signal, et des poids plus précis ne retirent pas un bruit déjà entré, ils le lisent plus fidèlement. Ce que le flottant achète est ailleurs et reste vrai : sur audio propre le plancher descend de 0,003 à 0,002.

Ça ne ferme pas la question des poids du serveur, ça la sépare de celle de la compression. Et elle a son propre prix, écrit dans `../analysis.md` : le seuil de marquage se calibre sur les poids qui tournent, donc un serveur en flottant demanderait **son seuil à lui**, à qualifier, là où l'app n'en tient qu'un.

**Huit bits est le point de fonctionnement, et sept ne l'est pas** : à 8 bits le plancher reste dix fois sous la plus faible faute, à 7 il la touche et il n'y a plus de bande.

**L'échelle fixe est la seule possible depuis l'envoi en flux** (n° 1) : une prise partie une seconde à la fois ne connaît pas son maximum. La mesure ci-dessus normalisait chaque fichier sur le sien, ce qui est un gain par fichier donc un traitement. Remesuré le 2026-09-11 à la pleine échelle du 16 bits (`SQUEEZE=ulawf8` et `linf8`, `faults.py`, poids arrondis), les deux normalisées rejouées à côté comme contrôle et retombées sur leurs chiffres :

| | pire témoin | `09-walkin`, voix `eleven-us-eric` | bande vide sur ce cas |
|---|---|---|---|
| PCM 16 bits | 0,003 | 0,031 | 0,028 |
| μ-law 8 bits, normalisé | 0,004 | 0,022 | 0,018 |
| linéaire 8 bits, normalisé | 0,003 | 0,023 | 0,020 |
| μ-law 8 bits, échelle fixe | 0,006 | 0,018 | 0,012 |
| linéaire 8 bits, échelle fixe | 0,003 | 0,018 | 0,015 |

La bande se lit sur `09-walkin` parce que c'est la seule faute lue dans toutes les variantes : `18-walkin-full` et `17-sink-full` ont un écart médian autour de la barre de 0,20 et entrent ou sortent de la comparaison selon la variante, donc « la plus faible faute » n'était pas le même cas d'une ligne à l'autre.

**Aucune des deux ne tient la bande du PCM, et le linéaire n'est pas propre non plus** : à échelle fixe, `01-sink` lu contre `eleven-us-eric` voit son écart médian passer de 0,003 à 0,046, et `17-sink-full` sort de la comparaison.

**Et ce jeu ne peut pas juger l'échelle fixe pour un appareil faible.** Ses prises ont toutes leur crête entre −4,6 et −2,5 dBFS, donc presque la pleine échelle ; les rendus des deux voix sortent 10 dB plus bas en moyenne (−25,8 à −20,8 dBFS, contre −16,7 à −10,3). Que le plancher du μ-law monte de 0,004 à 0,006 alors que les prises ont à peine bougé d'échelle va dans le sens d'un côté modèle qui paie ; ce n'est pas isolé. Ce qui manque est le niveau des vraies prises du téléphone, capturées sans gain automatique, qui vivent sur l'appareil.

**Ce que la compression achèterait a changé depuis les n° 1 et 2** (non mesuré) : la prise part pendant qu'on parle, et le modèle d'une redite ne repart pas. Ce qui reste sur le chemin critique est l'envoi du modèle au premier tour d'une phrase.

**Le FLAC remplace tout ça, écrit le 2026-09-11** (`capture/Flac.kt`). Sans perte, donc le serveur lit les échantillons mêmes et la matrice est la même au bit près : ni réglage, ni estampille, c'est du transport. Il part toujours, pour le modèle comme pour chaque morceau d'une prise. L'encodeur est écrit dans l'app plutôt que pris à la plateforme, dont l'encodeur varie d'un appareil à l'autre et ne tourne pas sur le poste ; `bench/flac.py` le tient contre le décodeur de référence, sur des cas limites et sur l'audio du jeu d'essai.

Mesuré, taille du FLAC rapportée aux échantillons bruts, les 87 fichiers se relisant identiques :

| | encodeur de l'app | libFLAC |
|---|---|---|
| rendus `eleven-gb-daniel` (16) | 53,4 % | 53,2 % |
| rendus `eleven-us-eric` (36) | 52,7 % | 52,1 % |
| prises du jeu d'essai (27) | 78,4 % | 76,9 % |

Découpé en morceaux d'une seconde, chacun encodé seul, libFLAC ne perd que 0,3 point. Le rendu, qui est l'envoi resté sur le chemin critique, passe donc à peu près à moitié, ce que le μ-law donnait au prix de la bande. **Le μ-law est écarté.** Les prises réelles portent leurs silences, qui se compressent presque entièrement ; le jeu d'essai en porte peu, et ce que ça vaut sur le téléphone n'est pas mesuré. Et **`09-walkin` n'est pas une victime de la compression**, contrairement à ce que ce doc a d'abord écrit : la voix `eleven-gb-daniel` la manque aussi en flottant, sans un bit retiré. C'est un cas marginal de cette voix-là, et le compter contre le codec aurait fait payer à la compression une faute qui ne lui appartient pas.

**Écarté et parké** : couper les silences avant d'envoyer. `../analysis.md` en donne l'argument — au-dessus d'une demi-seconde on est hors du domaine des phonèmes — mais l'essai en a déjà été fait dans ce projet et s'est mal passé, et rien ne dit aujourd'hui si le seuil était en cause. À reprendre par le seuil, pas par le principe.

## Ce qui n'est pas mesuré, et qui décide autant

**Ce que les trois valent ensemble n'est pas mesuré**, et l'addition qu'on serait tenté d'en faire n'est pas une mesure : le premier déplace du temps sans retirer d'octets, les deux autres retirent des octets sans changer le chemin. Elles se lisent une fois écrites, sur les mêmes prises.

**Et rien ici ne dit ce que le déterminisme devient à distance.** L'exigence 4 d'`../reference.md` — deux lectures du même fichier rendent les mêmes octets — a été vérifiée sur l'appareil, jamais sur une machine louée dont on ne choisit pas le matériel d'une instance à l'autre.

## La machine, et comment en remonter une

Ce qui a rendu la colonne « serveur » ci-dessus, avec la raison de chaque choix. Instance OVH Public Cloud, montée le 2026-09-10.

| choix | valeur | pourquoi |
|---|---|---|
| gabarit | `c3-8` — 8 Go, 4 vCPU | les deux seuils : 4 cœurs, et de quoi tenir le pic de 2,2 Go d'un tour de 30 s. Le compute optimized parce qu'on mesure une passe à la fois, donc des cœurs et non des gigaoctets |
| région | `eu-west-par-b` | Paris — le plus près, et le bon point de départ pour la latence depuis un téléphone en France |
| image | Debian 13 | la même que le poste : même glibc, même Python, mêmes roues `pip`. Une variable de moins entre deux colonnes |
| réseau | Basic Public IP | gratuite, et elle **disparaît avec l'instance** — donc ni gateway ni Floating IP à supprimer derrière, qui sont des ressources séparées et continuent de facturer une fois la machine détruite |
| facturation | à l'heure | 0,0913 €/h, soit ~40 centimes l'après-midi. Le mensuel est un engagement |
| script de post-installation | aucun | il tourne sans témoin, et sa panne se lit par l'absence de quelque chose. Tant que rien n'est gardé, il n'y a rien à reproduire |

**Ce qui s'est confirmé à l'arrivée**, et qui se relit à chaque nouvelle machine avant tout chiffre :

```
lscpu | grep -E "Model name|^CPU\(s\)|Thread\(s\) per core"
grep -o "avx512_vnni\|avx2" /proc/cpuinfo | sort -u
free -g
vmstat 1 5      # la colonne st : ce que le voisin prend
```

Rendu ici : AMD EPYC-Milan, **1 fil par cœur** — donc 4 cœurs réels, et `--threads 4` est le bon réglage ; **avx2 sans `avx512_vnni`**, Milan étant du Zen 3 ; 7 Go utilisables sans swap ; et **temps volé à 0**, donc les chiffres sont bien ceux de la machine.

**Le provisionnement**, une fois connecté en `debian@<ip>` (jamais `root`) :

```
sudo apt-get install -y python3-venv python3-dev git libsndfile1
git clone https://github.com/badibam/saylune.git ~/saylune
python3 -m venv ~/saylune/tmp/venv
~/saylune/tmp/venv/bin/pip install torch --index-url https://download.pytorch.org/whl/cpu
~/saylune/tmp/venv/bin/pip install numpy soundfile onnxruntime onnx onnxscript transformers huggingface_hub
```

`torch` vient de l'index CPU : l'index par défaut tire plusieurs gigaoctets de roues CUDA dont rien ici ne se sert.

**Ce que le clone ne porte pas**, `bench/out/` étant gitignoré — trois fichiers à envoyer par `scp`, tous petits :

- `bench/out/renders/azure-gb-sonia/sentences/think.wav`, à ce chemin exact : `export.py` trace le graphe dessus et échoue franchement sans ;
- un wav de parole à 16 kHz, que `cost.py` prend par `--audio` et répète jusqu'à la durée voulue ;
- `bench/cost.py` lui-même tant qu'il n'est pas poussé sur le dépôt public.

**Puis le graphe et les mesures :**

```
cd ~/saylune/bench
export HF_HOME=~/saylune/tmp/hf ACOUSTIC_MODEL=timit-ipa
~/saylune/tmp/venv/bin/python3 export.py                       # 1,26 Go tirés, puis l'arrondi
~/saylune/tmp/venv/bin/python3 cost.py --audio speech.wav --threads 4
```

`HF_HOME` est obligatoire et `matrix.py` le réclame explicitement plutôt que de choisir un défaut. `export.py` prend plusieurs minutes : il passe par la tâche de fond du harnais, et **sans tube filtrant dans la commande**, faute de quoi le journal reste vide jusqu'à la fin.

**Refabriquer le graphe sur place plutôt que l'envoyer** est ce qui a été fait, et c'est le bon geste : 342 Mo sur un lien montant domestique contre 1,26 Go tirés à vitesse de datacentre, pour un résultat que le garde-fou d'`export.py` prouve fidèle.

### Le serveur en service

**L'adresse ne vit pas ici**, le dépôt étant public : elle est derrière l'alias `saylune-analysis` du `~/.ssh/config` du poste, seule ligne à changer quand l'instance est remontée.

Sur la machine, dans `~/saylune` : `serve.py` est une copie posée à la racine du clone, pas le `server/serve.py` du clone ; `.serve.env`, lisible par son seul propriétaire, porte les quatre variables du lancement, jeton compris ; `serve.log` reçoit la sortie ; `weights/` les poids, que `serve.py` tire lui-même de la release.

**Mettre à jour**, depuis la racine du projet :

```
scp server/serve.py saylune-analysis:saylune/serve.py.new
ssh saylune-analysis
cd ~/saylune && pkill -f "python3 -u serve.py"
mv serve.py serve.py.old && mv serve.py.new serve.py
set -a; . ./.serve.env; set +a
setsid nohup tmp/venv/bin/python3 -u serve.py >> serve.log 2>&1 < /dev/null &
```

Vérifier de l'extérieur : `/health` répond `ok`, et `POST /take/0123456789abcdef?at=0` sans jeton répond `401`.
