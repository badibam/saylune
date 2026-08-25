# Passation — dérouler l'entraînement du modèle de sons

Doc de passation pour la session qui lancera les runs. Tout ce qui suit est **acté et rodé** ; la session exécute, mesure, et ne rouvre pas les décisions. Le pourquoi vit dans `dense-grid.md`, les prix dans `gpu-pricing.md`, la logistique dans `gpu-runbook.md` — ce doc ne les répète pas.

## L'état au moment de la passation

- **L'atelier existe et a tourné à blanc** : `train/` (voir son README). Le corpus entier décode (3696 + 1344 énoncés hors SA, 4,3 h, 151 813 cibles sur 38 classes), et un pas d'entraînement CPU rend une perte finie.
- **TIMIT est acheté**, complet dans `tmp/TIMIT/` (hors git, licence LDC — jamais public, jamais dans un dépôt ni un notebook public). L'archive prête à monter sur MEGA : `tmp/timit.tar.gz` (417 Mo, 25 863 entrées).
- **La voie d'acheminement est MEGA par lien public**, un seul upload depuis la ligne de l'utilisateur (lente en montant — ne jamais lui faire re-téléverser le corpus). Kaggle se sert du même lien (le notebook `megadl` lui-même) ; détail des deux modes dans le runbook.

## Décisions actées — ne pas rouvrir

- **Alphabet de sortie = le `vocab.json` de `timit-ipa` (vitouphy), à l'identique**, ligatures comprises (ʧ U+02A7, ʤ U+02A4). C'est ce qui permet au banc de lire le modèle affiné sans modification. `train/timit.py::vocabulary()` le relit depuis le cache HF et échoue franchement s'il manque.
- **Repliement TIMIT 61 → 39 Lee & Hon** (`train/timit.py::FOLD`) ; silences (h#, pau, epi, fermetures) et `q` ne sont pas des cibles — le silence appartient au blank. Variante en réserve, à ne dégainer que si la qualification montre un problème aux frontières : silence → espace.
- **Escalier** : V1 tête seule sur oreille `base-960h` gelée (défaut du script) d'abord ; V2 (`--unfreeze`) seulement si la mesure condamne V1 ; V3 (`large`) seulement si V2 est condamné. Chaque marche se paie sur verdict de mesure, jamais sur intuition.
- **Ce qui qualifie un checkpoint est `bench/faults.py`**, jamais la loss ni le PER. Un checkpoint « qui converge mieux » ne vaut rien tant que la séparation fautes/témoins et la densité de la grille ne sont pas relues.

## Dérouler un run (Kaggle, V1)

1. Notebook Kaggle : Internet activé, persistance **Files only**, accélérateur P100 (ou T4 — une seule carte suffit pour V1, ne pas monter de `DataParallel`).
2. `apt-get install -y megatools && megadl '<lien MEGA>'` puis extraire vers un chemin qui reproduit `tmp/TIMIT/lisa/...` sous la racine du code.
3. Amener `train/*.py` (quatre petits fichiers — cellule de collage ou Dataset utilitaire privé). Poser `HF_HOME`, puis y faire entrer le `vocab.json` de vitouphy : `hf download vitouphy/wav2vec2-xls-r-300m-timit-phoneme vocab.json` (le cache prend la structure que `vocabulary()` globbe). Les poids `base-960h` se téléchargent seuls au premier `from_pretrained`.
4. `python train/manifest.py` — refait la preuve du décodage sur la machine du run.
5. `python train/train.py --out <un dossier PAR run>` — le script **refuse** un dossier qui porte déjà des checkpoints ; c'est voulu, ne pas contourner. Il n'a **aucune reprise** : un run part toujours de l'encodeur pré-entraîné.
6. Rapatrier les checkpoints (Save version → output), ménage du `working` entre variantes.

## Hyperparamètres — points de départ, pas des valeurs qualifiées

`--lr 3e-4`, `--epochs 30`, `--batch 8` (monter le batch sur GPU tant que la VRAM suit). Le paramètre qui mérite le balayage est `--prior-weight` (poids du log-prior soustrait) : essayer {0,1 ; 0,3 ; 1,0} — c'est lui qui achète la densité, et 3 à 5 runs par variante sont budgétés pour ça. Ajouter une lecture du PER sur le split `test` du manifeste est permis (confort de suivi en cours de run), tant qu'il ne devient pas critère de choix.

## Pièges déjà payés — ne pas les repayer

- **`masked_spec_embed` absent du checkpoint `base-960h`** : transformers remplit les paramètres manquants de NaN et SpecAugment empoisonne toute passe en mode train. Corrigé dans `train.py` (ré-init + refus de tout paramètre NaN au chargement) — si un NaN réapparaît, chercher là d'abord.
- **Les `.WAV` de TIMIT sont du NIST SPHERE** ; soundfile les lit, rien à convertir.
- Le vocabulaire utilise les **ligatures à codepoint unique** — tout symbole composé `t`+`ʃ` est un bug.
- Fin de session payante : **détruire** pod et volume, pas arrêter (facturation à l'arrêt) ; `tmux` pour tout lancement SSH ; le lien MEGA se supprime en fin de chantier.

## Après les runs — qualification, de retour au poste

1. Ajouter le checkpoint retenu comme entrée de la table de `bench/matrix.py` (`Candidate`, avec `vocabulary` pointant vers vitouphy) — lecture neuve, cache propre.
2. `faults.py` : la séparation fautes/témoins tient-elle. Puis la densité : les sons que la grille actuelle perd (un sur huit — le `k` de *picked*, les `t` d'*important*, cf. `dense-grid.md`) doivent porter de la masse à leur place ; les sondes `tmp/blank_penalty.py` / `tmp/missing_mass.py` savent le relire.
3. Si V1 tient : export ONNX (`bench/export.py`), concordance, mesure téléphone — le chemin déjà balisé du banc.
4. Élaguer ce doc et les docs GPU une fois le modèle en place — le code et les commits deviennent le registre.

## Bornes de la session d'exécution

- Commits granulaires, messages en anglais, l'utilisateur seul auteur — cf. `dev_base`.
- Ne pas toucher aux contrats du banc (`matrix.py`, `faults.py`) au-delà de l'ajout d'un candidat.
- Toute envie de « corriger » une décision actée ci-dessus se consigne comme question dans le TODO, elle ne s'exécute pas.
