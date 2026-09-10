# L'analyse sur une machine distante

Doc transitoire, ouvert le 2026-09-10. Il porte deux choses : **ce qu'une passe d'analyse coûte sur une machine qui n'est pas le téléphone**, et **comment remonter la machine qui l'a rendu**. Il s'élague quand la question de déporter est tranchée — ce qui reste alors vit dans `../analysis.md`.

Rien ici ne décide de déporter quoi que ce soit. Ce sont des mesures, prises pour que la question se pose sur des chiffres.

## Où la coupe tomberait, si elle tombait

L'analyse n'est pas un bloc : **une seule brique coûte, la passe du réseau** (brique 2 d'`../analysis.md`). Tout le reste — grille, jointure, alignement, recouvrement, mélodie, contrôle — est de l'arithmétique au-dessus de la matrice, mesurée négligeable. Ce qui se déporterait est donc étroit : un audio monte, une matrice descend, et la marque, le seuil et la note restent sur l'appareil. Aucun jugement ne voyage, aucune norme extérieure n'entre par là.

Deux contraintes déjà écrites bornent toute forme à venir, et elles ne se négocient pas ici. **Les deux audios passent par la même machine, ou aucun** : le biais de la machine ne s'annule que s'il est le même des deux côtés (`../reference.md`), donc une prise lue à distance et un modèle lu sur l'appareil fabriqueraient une partie de l'écart mesuré. Et **le choix se fait au démarrage d'une conversation, jamais au tour** : une matrice distante et une matrice locale sont deux époques de mesure, que rien n'autorise à mélanger dans un même fil.

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

## Ce qui n'est pas mesuré, et qui décide autant

**La latence réseau, entièrement.** Rien ci-dessus ne dit ce que coûte l'envoi de l'audio et le retour de la matrice depuis un téléphone en 4G. La mesure viendra des prises enregistrées avec le montage réel, pas d'un instrument à part.

**Une conséquence qui n'est donc pas acquise, et qui oriente si elle se confirme** : une passe de 0,56 s pour un tour de 6 s est probablement plus courte que l'aller-retour qui l'entoure. Si c'est le cas, ce qui vaut d'être travaillé est la compression de l'audio envoyé, pas l'achat de cœurs. Ce n'est pas une conclusion, c'est ce que la mesure manquante trancherait.

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
