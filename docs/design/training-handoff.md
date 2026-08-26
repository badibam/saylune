# Passation — la marche 3, affinage complet du modèle de sons

Doc de passation pour la session qui déroule les runs. Tout ce qui suit est **acté** ; la session exécute, mesure, et ne rouvre pas les décisions. Le pourquoi vit dans `dense-grid.md`, les prix dans `gpu-pricing.md`, la logistique dans `gpu-runbook.md` — ce doc ne les répète pas.

## L'état au moment de la passation

- **L'atelier a produit deux générations et les deux sont condamnées.** V1 l'était par un bug de perte (scores pénalisés renormalisés, le biais de la tête annulant le prior à coût nul) ; V1b a rejoué le balayage sur le script corrigé, contrôle sans prior compris, et c'est ce contrôle qui condamne le **régime** : oreille gelée, tête seule sur `base-960h`, pire témoin de `faults.py` entre 0,126 et 0,221 quand le sortant `timit-ipa` tient 0,002 à 0,004, et 37 à 48 sons perdus contre 32. Mesures brutes dans `tmp/v1b-sweep-measures.md`.
- **Les checkpoints V1 et V1b ne servent plus qu'à l'archive.** Ils restent déclarés dans `bench/matrix.py` sous `v1b-pw<poids>-e<époque>` ; rien n'oblige à les relire.
- **TIMIT est acheté**, complet dans `tmp/TIMIT/` (hors git, licence LDC — jamais public, jamais dans un dépôt ni un notebook public). L'archive prête à monter sur MEGA : `tmp/timit.tar.gz` (417 Mo, 25 863 entrées). Le split d'entraînement fait 3696 énoncés, 3,15 h, médiane 2,91 s, maximum 7,79 s.
- **La voie d'acheminement est MEGA par lien public**, un seul upload depuis la ligne de l'utilisateur (lente en montant — ne jamais lui faire re-téléverser le corpus). Kaggle se sert du même lien (le notebook `megadl` lui-même) ; détail des deux modes dans le runbook.

## Décisions actées — ne pas rouvrir

- **Le régime est l'affinage complet** (`--unfreeze`), oreilles séparées, tête-lettres intacte. Le partage d'oreille est abandonné : c'était une préférence, la qualité est le critère, et son coût — deux téléchargements, deux passes, deux oreilles en RAM — est déjà chiffré dans `dense-grid.md`. L'extracteur convolutionnel reste gelé dans tous les cas : c'est la recette de wav2vec2 et c'est ce qui maintient le contrat de signal du graphe exporté.
- **Le dos est `facebook/wav2vec2-xls-r-300m`**, celui du sortant. L'oreille n'ayant plus à se partager, le meilleur isolat est atteignable : mêmes données, même dos, même alphabet, notre recette en plus. Tout écart mesuré s'impute alors à la recette et à rien d'autre. C'est aussi le dos dont la taille embarquée est déjà budgétée par l'app (342 Mo int8).
- **Alphabet de sortie = le `vocab.json` de `timit-ipa` (vitouphy), à l'identique**, ligatures comprises (ʧ U+02A7, ʤ U+02A4). C'est ce qui permet au banc de lire le modèle affiné sans modification. `train/timit.py::vocabulary()` le relit depuis le cache HF et échoue franchement s'il manque.
- **Repliement TIMIT 61 → 39 Lee & Hon** (`train/timit.py::FOLD`) ; silences (h#, pau, epi, fermetures) et `q` ne sont pas des cibles — le silence appartient au blank. Variante en réserve, à ne dégainer que si la qualification montre un problème aux frontières : silence → espace.
- **`layerdrop` est épinglé à 0,0** dans `train.py`, la valeur du sortant. Ça retire une source de bruit, pas toutes : dropout et SpecAugment restent stochastiques, donc deux runs de mêmes arguments ne rendront pas les mêmes poids.
- **Ce qui qualifie un checkpoint est `bench/faults.py`**, jamais la perte ni le PER. Un checkpoint « qui converge mieux » ne vaut rien tant que la séparation fautes/témoins et la densité de la grille ne sont pas relues.

## Ce que le script sait faire depuis la marche 3

`train.py` a reçu ce qu'un affinage complet exige et qu'une tête seule tolérait de ne pas avoir. Les arguments neufs, tous éprouvés au pas à blanc CPU sur un dos `large` du cache (chemin layer-norm, masque d'attention) :

- `--unfreeze` — entraîne tout sauf l'extracteur convolutionnel.
- `--warmup` (défaut 0,1) — fraction des pas d'optimiseur passée à monter le pas d'apprentissage, décroissance linéaire vers zéro ensuite. Une tête froide sur 300 M de paramètres à pas constant diverge dans les premières centaines de pas.
- `--clip` (défaut 1,0) — norme de gradient maximale ; 0 désactive.
- `--accumulate` — pas d'optimiseur tous les N lots, pour tenir un lot effectif sur une carte trop petite.
- `--checkpointing` — recalcul des activations, échange du calcul contre de la VRAM.
- `--save-every` — un checkpoint toutes les N époques, la dernière toujours. Nécessaire : un checkpoint complet pèse ~1,2 Go, contre ~360 Mo en tête seule.
- `--amp` — précision mixte sur CUDA, la perte restant en fp32. **Non éprouvé sur GPU** : à n'allumer qu'après avoir comparé quelques centaines de pas à un run fp32, un CTC sur des log-probabilités étant précisément l'endroit où la précision réduite devient un NaN silencieux plutôt qu'une erreur.
- `--seed` — graine de l'initialisation de la tête et de l'ordre des lots.

Changement de comportement à connaître : les lots restent groupés par durée (le remplissage coûte moins cher entre voisins), mais **l'ordre des lots est mélangé à chaque époque**. Les générations précédentes voyaient le corpus du plus court au plus long, dans le même ordre à chaque époque.

## Où ça tourne

**Ça tourne sur le T4 de Kaggle, mesuré, contre ce qu'annonçait `gpu-pricing.md`.** La grille V3 y écartait le T4 sur 16 Go de VRAM insuffisants, mais ce chiffrage supposait les activations stockées. Avec `--checkpointing` elles cessent d'être le poste dominant, et le pas à blanc rend **7943 Mo de pic sur 15 360, à 1,00 s le pas** — `xls-r-300m` dégelé, lots de 8, accumulation 2. Soit 7,7 min l'époque de 462 lots, 3,9 h les 30 époques, quand la session en autorise 12 et le quota hebdomadaire 30. Réserve : le pic est relevé sur 30 lots tirés au hasard, qui n'incluent pas forcément le groupe des énoncés de 7,8 s ; les 7,4 Go de marge le couvrent sans que ce soit mesuré.

Le repli **RunPod Community RTX 4090 24 Go** (~13 € les quatre runs) garde son intérêt si une variante future sort de l'enveloppe, mais il n'est plus le chemin.

Règle qui reste, quelle que soit la machine : **baisser `--epochs` jusqu'à tenir dans la session**, jamais couper un run en cours — `train.py` n'a aucune reprise.

## Dérouler un run sur Kaggle

Deux notebooks, tous deux **privés** : le lien MEGA porte sa clé de déchiffrement dans son propre source, et TIMIT est sous licence LDC.

**Le notebook de préparation**, une fois pour tout le chantier, est inchangé — il existe déjà si le chantier V1b a été mené. Accélérateur `none`, internet activé, `apt-get install -y megatools` puis `megadl --path /kaggle/working/ '<lien>'` — guillemets simples obligatoires, sinon bash coupe l'URL au `#` et la clé est perdue. Vérifier l'empreinte contre `tmp/timit.tar.gz` du poste, puis **Save Version → Quick Save en demandant la sauvegarde de la sortie**. L'archive reste **une archive** : 26 000 petits fichiers montés depuis `/kaggle/input` étranglent la lecture d'une époque, là où une extraction par run sur le disque local coûte moins d'une minute.

**Le notebook d'entraînement se copie, il ne se crée pas.** `speakup-training-reboot` (celui de la V1b) porte déjà les bons réglages — privé, T4 ×2, internet activé, persistance *Files only*, image Docker **épinglée** — et ses deux entrées branchées. Un *Copy & Edit* préserve tout ça ; une création de zéro rejouerait chaque piège. Renommer la copie, puis vérifier dans le panneau *Input* que le Dataset du code pointe la **dernière version** : une copie peut rester accrochée à celle qu'utilisait l'original, et le run tournerait alors sur un `train.py` périmé sans le dire.

Les entrées se montent à des chemins que Kaggle fabrique et qu'on lit à la cellule 1 :

- le code — Dataset `gilleslandrin/speakup-train`, monté sous `/kaggle/input/datasets/gilleslandrin/speakup-train`, les trois `.py` à plat ;
- le corpus — **sortie du notebook** `gilleslandrin/timit` (un *kernel source*, pas un Dataset), donc `/kaggle/input/notebooks/gilleslandrin/timit/timit.tar.gz`.

Les cellules 1 à 4 sont celles de la V1b, inchangées : inventaire des montages ; copie des `.py` vers `/kaggle/working/train` et extraction du tar vers `/kaggle/working/tmp` (`ROOT` est le parent de `train/`, et le tar porte `TIMIT/` à sa racine) ; téléchargement du `vocab.json` de vitouphy dans un cache sur `/kaggle/temp` ; puis `train/manifest.py`, qui doit annoncer **3696 + 1344 énoncés et 151 813 cibles sur 38 classes** — trois nombres qui, s'ils diffèrent, disent que le corpus monté n'est pas celui sur lequel tout le reste a été mesuré.

Deux avertissements sans conséquence à cette étape : `tar` signale un horodatage de 1881 (artefact du CD TIMIT d'origine, code de retour zéro), et le Hub signale des requêtes non authentifiées (sans effet sur quelques kilo-octets ; poser un `HF_TOKEN` en secret Kaggle si le téléchargement de 1,2 Go venait à se faire limiter).

**Toute cellule qui lance un entraînement se lit ligne par ligne, jamais par `subprocess.run`.** Python tamponne sa sortie quand elle n'est pas un terminal : une cellule qui attend la fin du sous-processus reste muette pendant toute sa durée, et rien ne distingue alors un run qui avance d'un run bloqué. La forme qui marche est `Popen` avec `python -u`, `stdout=PIPE`, `stderr=STDOUT`, et une boucle qui imprime chaque ligne avec `flush=True`. Sur un run de plusieurs heures ce n'est pas un confort, c'est le seul moyen de savoir qu'il vit.

**Le pic de VRAM ne se mesure pas après coup** : `nvidia-smi` lancé une fois le sous-processus terminé rend `0 MiB`, la mémoire ayant été rendue à sa sortie. Il faut l'interroger *pendant*, depuis un fil de surveillance qui garde le maximum.

La cellule de run, enfin — un seul `--prior-weight` par version du notebook :

```python
run = subprocess.Popen(
    ["python", "-u", "train/train.py",
     "--encoder", "facebook/wav2vec2-xls-r-300m",
     "--unfreeze", "--checkpointing",
     "--lr", "1e-4", "--epochs", "30", "--batch", "8", "--accumulate", "2",
     "--warmup", "0.1", "--save-every", "10",
     "--prior-weight", "0",
     "--out", "/kaggle/working/tmp/train/runs-v3/v3-pw0.0"],
    cwd="/kaggle/working", env={**os.environ, "HF_HOME": "/kaggle/temp/hf"},
    stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
```

462 lots par époque au lot de 8 : n'imprimer qu'une ligne de perte sur 462 suffit à suivre, et évite un notebook de 14 000 lignes. Une dernière cellule retire `tmp/TIMIT` — la sortie d'une version est tout `/kaggle/working`, corpus extrait compris.

Les cellules de pas à blanc sont des instruments, pas des étapes : **les supprimer avant le Save & Run All**, sinon elles recoûtent leurs pas à chaque exécution complète.

Puis **Save & Run All** : le conteneur repart vierge et rejoue tout.

## Hyperparamètres — points de départ, pas des valeurs qualifiées

`--lr 1e-4` avec `--warmup 0.1` : 3e-4 était le réglage d'une tête seule, il est agressif pour 300 M de paramètres pré-entraînés. `--batch 8 --accumulate 2` donne un lot effectif de 16, celui des générations précédentes — à garder pour que l'échelle d'optimisation reste comparable ; monter `--batch` et baisser `--accumulate` d'autant si la VRAM le permet, le produit étant ce qui compte. `--epochs 30 --save-every 10` : 3,9 h mesurées, et les checkpoints tombent aux époques 009, 019 et 029, les points de lecture de la V1b — la comparaison entre générations se fait ainsi aux mêmes époques, pour 3,6 Go dans un `/kaggle/working` plafonné à 20.

**L'ordre des runs est une décision, pas un détail.** Le contrôle `--prior-weight 0.0` passe **en premier** : c'est lui qui dit si le régime atteint la parité avec le sortant, question qui prime sur le réglage du prior. S'il échoue aussi, c'est la direction entière de l'affinage qui se rediscute, et les runs suivants sont de l'argent jeté. S'il tient, balayer `0.1` puis `0.3` — et `1.0` seulement si la densité progresse encore à `0.3`, ce que les deux générations précédentes rendent peu probable.

Une lecture du PER sur le split `test` du manifeste est permise en cours de run, tant qu'elle ne devient pas critère de choix.

**Un réglage laissé ouvert, à connaître** : le sortant a été entraîné avec `mask_time_prob` à 0,75, un SpecAugment très agressif, là où la config de `xls-r-300m` porte la valeur par défaut. Le run ne la reprend pas — ce serait un second écart à l'isolat — mais si le contrôle sous-apprend ou surapprend visiblement, c'est le premier bouton à examiner.

## Pièges déjà payés — ne pas les repayer

- **Le cache HF ne peut pas vivre dans `/kaggle/working`** : la persistance conserve l'arborescence et perd les blobs, donc les liens de `snapshots/` pendent dans le vide au redémarrage de session. L'échec est trompeur — `glob` teste l'existence et rend « vocab.json absent » quand `find` voit le fichier, et un `config.json` se lit comme vide. Le cache va sur `/kaggle/temp`, ce qui lui évite en outre d'alourdir la sortie de chaque version.
- **`HF_HOME` ne se pose pas dans le noyau du notebook** : `huggingface_hub` le fige à l'import, donc un `os.environ` postérieur est ignoré en silence. Toute commande le reçoit en préfixe de son sous-processus, ce qui rend les cellules indépendantes de l'ordre d'exécution — ce qu'exige un Save & Run All.
- **Renormaliser les scores pénalisés annule le prior** : un `log_softmax` après la soustraction de `w·log prior` laisse le biais de la tête absorber la pénalité exactement, à coût nul — la perte redevient un CTC nu et la lecture brute hérite du décalage. Prouvé sur les checkpoints de la première génération (corrélation biais ↔ pénalité jusqu'à +0,89). Corrigé dans `train.py` ; les scores vont à la perte non normalisés, comme dans le papier.
- **Le P100 de Kaggle est mort pour nous** : le PyTorch de l'image ne compile plus que pour `sm_70` et au-delà, quand la carte est `sm_60`. Le modèle charge, l'entête annonce `cuda`, puis le premier tenseur alloué lève `no kernel image is available for execution on the device`. Le T4 (`sm_75`) passe.
- **`masked_spec_embed` absent d'un checkpoint** : transformers remplit les paramètres manquants de NaN et SpecAugment empoisonne toute passe en mode train. Corrigé dans `train.py` (ré-init + refus de tout paramètre NaN au chargement) — si un NaN réapparaît, chercher là d'abord. Le cas s'était produit sur `base-960h` ; `xls-r-300m` porte le paramètre, la garde reste.
- **La sortie d'une version est tout `/kaggle/working`**, corpus extrait compris (630 Mo) : le supprimer en fin de cellule, sinon un « Download All » rapatrie ce qu'on a déjà.
- **Rapatrier les poids se fait fichier par fichier** — `kaggle kernels output` écrit un `model.safetensors` de 0 octet sans le moindre message, et le téléchargement d'archive complète répond 404 sur un dataset que l'API dit pourtant `ready`. La procédure qui marche est dans `gpu-runbook.md` ; adapter la boucle aux noms `v3-pw<poids>/epoch-<NNN>`, et vérifier que chaque fichier est non vide.
- **Les `.WAV` de TIMIT sont du NIST SPHERE** ; soundfile les lit, rien à convertir.
- Le vocabulaire utilise les **ligatures à codepoint unique** — tout symbole composé `t`+`ʃ` est un bug.
- Fin de session payante : **détruire** pod et volume, pas arrêter (facturation à l'arrêt) ; `tmux` pour tout lancement SSH ; le lien MEGA se supprime en fin de chantier.

## Après les runs — qualification, de retour au poste

1. **Rejouer le sortant `timit-ipa` dans la même passe.** La génération V1b ne l'a pas fait (poids absents du cache, Hub refusé) et ses valeurs de repère sont citées, pas mesurées ; la marche 3 se juge contre un étalon relu dans les mêmes conditions. C'est un accès Hub à restaurer et deux lectures.
2. Ajouter le checkpoint retenu comme entrée de la table de `bench/matrix.py` (`Candidate`, avec `vocabulary` pointant vers vitouphy) — lecture neuve, cache propre.
3. `faults.py` : la séparation fautes/témoins tient-elle, et sur combien de fautes **ancrables** — une bande large sur deux fautes survivantes est un artefact de grille clairsemée, pas un succès ; c'est ce qui a failli sauver les runs `pw0.3` de la V1b. Puis `tmp/missing_mass.py` pour la densité : les sons que la grille actuelle perd (un sur huit — le `k` de *picked*, les `t` d'*important*, cf. `dense-grid.md`) doivent porter de la masse à leur place.
4. Si la marche 3 tient : export ONNX (`bench/export.py`), concordance, mesure téléphone — le chemin déjà balisé du banc. La quantification int8 est à revérifier sur ce dos, le budget de 342 Mo de l'app en dépendant.
5. Élaguer ce doc et les docs GPU une fois le modèle en place — le code et les commits deviennent le registre.

## Bornes de la session d'exécution

- Commits granulaires, messages en anglais, l'utilisateur seul auteur — cf. `dev_base`.
- Ne pas toucher aux contrats du banc (`matrix.py`, `faults.py`) au-delà de l'ajout d'un candidat.
- Toute envie de « corriger » une décision actée ci-dessus se consigne comme question dans le TODO et se discute, elle ne s'exécute pas.
