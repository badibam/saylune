# L'atelier — affiner le modèle de sons

Ce qui fabrique le remplaçant de `timit-ipa` : wav2vec2 + tête CTC phonémique entraînée avec label priors (Huang et al., ICASSP 2024). Le pourquoi et l'état du chantier sont dans `../TODO.md`, la peakiness qui l'avait motivé dans `../docs/analysis.md`. Rien de ce dossier ne part dans l'app : comme le banc, c'est l'instrument.

**L'alphabet de sortie est celui de `timit-ipa`, à l'identique** — son `vocab.json` est relu depuis le cache HF et le repliement TIMIT 61 → 39 (Lee & Hon) vise ses symboles, ligatures comprises (ʧ, ʤ). C'est ce qui permet à `bench/matrix.py` et `bench/faults.py` de lire le modèle affiné sans modification, et fait de la comparaison avant/après une comparaison à iso-alphabet. Les silences (h#, pau, epi, fermetures) et le coup de glotte q ne sont pas des cibles : le silence appartient au blank.

## Dérouler

Même venv et même cache que le banc (`../bench/README.md`) ; `HF_HOME` obligatoire, échec franc sinon.

```
HF_HOME=$PWD/tmp/hf tmp/venv/bin/python train/manifest.py       # décode tout le corpus, écrit tmp/train/manifest.json
HF_HOME=$PWD/tmp/hf tmp/venv/bin/python train/train.py --steps 2 --utterances 8   # pas à blanc CPU
HF_HOME=$PWD/tmp/hf tmp/venv/bin/python train/train.py --encoder facebook/wav2vec2-xls-r-300m --unfreeze --checkpointing --lr 1e-4   # le régime retenu (GPU)
```

Par défaut le script n'entraîne que la tête sur une oreille gelée — le régime des deux premières générations, que la mesure a condamné. Le régime retenu est l'affinage complet, dos `xls-r-300m`, trente époques, checkpoints aux époques 9, 19 et 29. L'extracteur convolutionnel reste gelé quoi qu'il arrive.

`train/fetch.py` rapatrie les checkpoints d'un notebook Kaggle, un fichier à la fois et de façon reprenable — `train/fetch.py --kernel <compte>/<notebook> --epoch 029`, la même commande relancée reprenant un transfert coupé, sans argument les trois époques du run. `--kernel` n'a pas de défaut : un notebook mémorisé désignerait toujours le run précédent, et l'erreur ressemblerait à un téléchargement déjà fini. **C'est l'utilisateur qui le lance**, pas la session d'IA — un transfert de plusieurs gigaoctets dure plus qu'un tour de session et occupe la ligne.

Le script prend une voie que le CLI Kaggle n'offre pas, parce que ses deux voies échouent **en silence** : `kaggle kernels output` sans filtre rend une liste tronquée et un `model.safetensors` de 0 octet sans code de retour non nul, et avec `--file-pattern` un checkpoint de 1,2 Go meurt sur `IncompleteRead` puis laisse un fichier de 0 octet en sortant avec le code 0. `fetch.py` passe par les **URL signées** de `GET /api/v1/kernels/output`, qui honorent `Range` — donc la reprise — mais refusent `HEAD` (la taille se demande par un GET d'un octet, dont le `Content-Range` porte le total) et expirent vite, d'où une relecture de la liste à chaque invocation. Trois contraintes payées : l'API refuse l'authentification basique dès qu'un jeton OAuth existe ; le transfert est forcé en **HTTP/1.1**, les URL signées coupant sur HTTP/2 (`INTERNAL_ERROR`) ; et le retry est celui du script, jamais celui de `curl`, dont `--continue-at -` relit l'offset une seule fois et jette ce qu'une tentative coupée avait reçu (717 Mo perdus d'un coup, mesuré). Aucune taille annoncée par le CLI ne vaut contrôle — `kaggle kernels files` rend 946 octets pour un fichier de 1,2 Go.

Le CLI s'installe dans le venv du projet (`tmp/venv/bin/pip install kaggle`) et s'authentifie par `kaggle auth login`, ou par un jeton déposé en `~/.kaggle/kaggle.json` en mode `600`.

**Le notebook est `kaggle/phonemes.ipynb`**, à importer dans un notebook neuf. Il porte le régime de la quatrième génération — encodeur `wav2vec2-base`, pénalité de fréquence à zéro, tout le reste tenu comme `v3` —, **il mesure ce qu'un pas coûte sur la machine du run avant d'engager trente époques**, et il attend deux entrées : le dataset privé du code (`train/*.py`) et le notebook qui porte l'archive du corpus. Les trois autres notebooks de ce dossier sont ceux de la tête à lettres, sur AMI, et n'ont rien à voir avec celui-ci. Les générations `v1`, `v1b` et `v3` ont tourné sur des notebooks qui ne vivaient que chez Kaggle ; c'est ce qui a rendu la quatrième plus chère à préparer qu'à lancer.

**Côté Kaggle, trois réglages qui ne se voient pas.** Le corpus se charge en **dataset privé** — TIMIT est sous licence LDC, pas librement redistribuable. La persistance de session se met sur « Files only », jamais « Variables » qui restaure l'état Python d'une session précédente ; elle retombe à « aucune » à chaque *Copy & Edit* et se repose à la main. Et `/kaggle/working` survit d'une session à l'autre, d'où un dossier de sortie **par run** — `train.py` refuse d'écrire dans un dossier qui porte déjà des checkpoints — et un ménage entre variantes. Ce que ce refus vise est **un dossier**, pas le répertoire de travail : la cellule qui débloque se place **juste avant celle qui entraîne**, et ne touche que la sortie du run.

```python
import pathlib, shutil

out = pathlib.Path("/kaggle/working/tmp/train/runs-v3/v3-pw0.3")   # le --out du run
if out.exists():
    shutil.rmtree(out)
    print("retiré :", out)
```

Un balayage de `/kaggle/working` entier ne convient pas ici : le notebook y dépose le code qu'il exécute — le dépôt n'est pas sur Kaggle — donc tout ce qui efface le répertoire doit précéder cette écriture, et une cellule rejouée hors d'ordre emporte le script. Une copie fraîche part de toute façon vide, la persistance retombant à « aucune » au *Copy & Edit* ; le cas qui demande vraiment cette cellule est le **relancement après un plantage**, où les checkpoints d'un run mort à l'époque 12 bloquent l'écriture.

Le manifeste et les checkpoints vivent sous `tmp/train/`, régénérables. Les hyperparamètres exposés (`--lr`, `--prior-weight`, `--epochs`, `--warmup`) sont des points de départ à balayer sur GPU, pas des valeurs qualifiées ; ce qui qualifie un checkpoint est la procédure de `../docs/qualification.md`, jamais la loss — et `faults.py` seul n'y suffit pas, un modèle plus pointu séparant mieux les fautes tout en donnant moins de matière à joindre.
