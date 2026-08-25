# Passation — dérouler l'entraînement du modèle de sons

Doc de passation pour la session qui lancera les runs. Tout ce qui suit est **acté et rodé** ; la session exécute, mesure, et ne rouvre pas les décisions. Le pourquoi vit dans `dense-grid.md`, les prix dans `gpu-pricing.md`, la logistique dans `gpu-runbook.md` — ce doc ne les répète pas.

## L'état au moment de la passation

- **L'atelier existe et a tourné à blanc** : `train/` (voir son README). Le corpus entier décode (3696 + 1344 énoncés hors SA, 4,3 h, 151 813 cibles sur 38 classes), et un pas d'entraînement CPU rend une perte finie.
- **TIMIT est acheté**, complet dans `tmp/TIMIT/` (hors git, licence LDC — jamais public, jamais dans un dépôt ni un notebook public). L'archive prête à monter sur MEGA : `tmp/timit.tar.gz` (417 Mo, 25 863 entrées).
- **La voie d'acheminement est MEGA par lien public**, un seul upload depuis la ligne de l'utilisateur (lente en montant — ne jamais lui faire re-téléverser le corpus). Kaggle se sert du même lien (le notebook `megadl` lui-même) ; détail des deux modes dans le runbook.

## Décisions actées — ne pas rouvrir

- **Alphabet de sortie = le `vocab.json` de `timit-ipa` (vitouphy), à l'identique**, ligatures comprises (ʧ U+02A7, ʤ U+02A4). C'est ce qui permet au banc de lire le modèle affiné sans modification. `train/timit.py::vocabulary()` le relit depuis le cache HF et échoue franchement s'il manque.
- **Repliement TIMIT 61 → 39 Lee & Hon** (`train/timit.py::FOLD`) ; silences (h#, pau, epi, fermetures) et `q` ne sont pas des cibles — le silence appartient au blank. Variante en réserve, à ne dégainer que si la qualification montre un problème aux frontières : silence → espace.
- **Escalier** : V1 tête seule sur oreille `base-960h` gelée (défaut du script) d'abord ; V2 (`--unfreeze`) seulement si la mesure condamne V1 ; V3 (`large`, par `--encoder`) seulement si V2 est condamné. Chaque marche se paie sur verdict de mesure, jamais sur intuition.
- **Le gel de l'oreille est une préférence, pas une condition.** `dense-grid.md` le présentait comme la condition du partage, et le partage vaut ce qu'il vaut — un téléchargement au lieu de deux, une passe au lieu de deux, ~1 Go de RAM. Mais le critère est la qualité que `faults.py` mesure : une oreille dégelée qui sépare mieux se garde, et le partage se paie alors comme un coût, non comme un interdit.
- **Ce qui qualifie un checkpoint est `bench/faults.py`**, jamais la loss ni le PER. Un checkpoint « qui converge mieux » ne vaut rien tant que la séparation fautes/témoins et la densité de la grille ne sont pas relues.

## Dérouler un run (Kaggle, V1)

Deux notebooks, tous deux **privés** : le lien MEGA porte sa clé de déchiffrement dans son propre source, et TIMIT est sous licence LDC.

**Le notebook de préparation**, une fois pour tout le chantier. Accélérateur `none` (une session draft consomme le quota GPU pendant qu'on lit l'écran), internet activé, `apt-get install -y megatools` puis `megadl --path /kaggle/working/ '<lien>'` — guillemets simples obligatoires, sinon bash coupe l'URL au `#` et la clé est perdue. Vérifier l'empreinte contre `tmp/timit.tar.gz` du poste, puis **Save Version → Quick Save en demandant la sauvegarde de la sortie** : la version devient une source de données attachable, et le corpus ne se retéléchargera plus. L'archive reste **une archive** — 26 000 petits fichiers montés depuis `/kaggle/input` étranglent la lecture d'une époque, là où une extraction par run sur le disque local coûte moins d'une minute.

**Le notebook d'entraînement**, un par run :

1. Internet activé, persistance **Files only**, environnement **épinglé** (une mise à jour d'image en cours de balayage rendrait les checkpoints incomparables sans que rien ne le signale), accélérateur **T4 ×2** — une seule carte sert, `train.py` ne monte pas de `DataParallel` et le quota se compte en heures de session, pas en cartes ; le TPU exigerait de réécrire la boucle en XLA.
2. `Add Input` : la version du notebook de préparation, et un Dataset privé portant `train/*.py`. **Lancer la boucle `os.walk('/kaggle/input')` de la cellule par défaut avant de la supprimer** — Kaggle fabrique les noms des dossiers montés, ils ne se devinent pas et tout le reste s'y réfère.
3. Extraire l'archive vers `tmp/` sous la racine du code : `ROOT` est le parent de `train/`, donc `/kaggle/working`, et le tar porte `TIMIT/` à sa racine, ce qui reproduit `tmp/TIMIT/lisa/...`.
4. Faire entrer le `vocab.json` de vitouphy dans un cache posé sur `/kaggle/temp/hf`, par un `hf_hub_download` lancé en sous-processus avec `HF_HOME` en préfixe — l'appel python dépose la structure que `vocabulary()` globbe sans dépendre de la version du CLI installée sur l'image. Les poids `base-960h` se téléchargent seuls au premier `from_pretrained`.
5. `python train/manifest.py` — refait la preuve du décodage sur la machine du run.
6. Pas à blanc GPU (`--steps 20 --utterances 64`), puis chronométrer : la session est plafonnée à 12 h et le quota hebdomadaire à 30 h. Si les 30 époques n'y tiennent pas, baisser `--epochs` — jamais couper un run en cours, `train.py` n'ayant aucune reprise.
7. `python train/train.py --out <un dossier PAR run>` — le script **refuse** un dossier qui porte déjà des checkpoints ; c'est voulu, ne pas contourner. Un run part toujours de l'encodeur pré-entraîné.
8. **Save & Run All**, le conteneur repart vierge et rejoue tout. Une époque coûtant ~52 s sur T4 (231 pas au batch 16), les 30 époques font une demi-heure et le balayage entier des trois `--prior-weight` tient dans **une seule** version, à condition d'élaguer chaque run **dès qu'il finit** : trois runs entiers cumuleraient 32 Go avant qu'un élagage final n'ait lieu.

## Hyperparamètres — points de départ, pas des valeurs qualifiées

`--lr 3e-4`, `--epochs 30`, `--batch 8` (monter le batch sur GPU tant que la VRAM suit). Le paramètre qui mérite le balayage est `--prior-weight` (poids du log-prior soustrait) : essayer {0,1 ; 0,3 ; 1,0} — c'est lui qui achète la densité, et 3 à 5 runs par variante sont budgétés pour ça. Ajouter une lecture du PER sur le split `test` du manifeste est permis (confort de suivi en cours de run), tant qu'il ne devient pas critère de choix.

## Pièges déjà payés — ne pas les repayer

- **`masked_spec_embed` absent du checkpoint `base-960h`** : transformers remplit les paramètres manquants de NaN et SpecAugment empoisonne toute passe en mode train. Corrigé dans `train.py` (ré-init + refus de tout paramètre NaN au chargement) — si un NaN réapparaît, chercher là d'abord.
- **Le P100 de Kaggle est mort pour nous** : le PyTorch de l'image ne compile plus que pour `sm_70` et au-delà, quand la carte est `sm_60`. Le modèle charge, l'entête annonce `cuda`, puis le premier tenseur alloué lève `no kernel image is available for execution on the device`. Le T4 (`sm_75`) passe.
- **La sortie d'une version est tout `/kaggle/working`**, corpus extrait compris (630 Mo) : le supprimer en fin de cellule, sinon un « Download All » rapatrie ce qu'on a déjà. Ne descendre que `tmp/train/runs/`, et d'abord les seules dernières époques — les intermédiaires ne servent qu'à diagnostiquer une dérive.
- **Le cache HF ne peut pas vivre dans `/kaggle/working`** : la persistance conserve l'arborescence et perd les blobs, donc les liens de `snapshots/` pendent dans le vide au redémarrage de session. L'échec est trompeur — `glob` teste l'existence et rend « vocab.json absent » quand `find` voit le fichier, et un `config.json` se lit comme vide. Le cache va sur `/kaggle/temp`, ce qui lui évite en outre d'alourdir de 378 Mo la sortie de chaque version.
- **`HF_HOME` ne se pose pas dans le noyau du notebook** : `huggingface_hub` le fige à l'import, donc un `os.environ` postérieur est ignoré en silence. Toute commande le reçoit en préfixe de son sous-processus, ce qui rend les cellules indépendantes de l'ordre d'exécution — ce qu'exige un Save & Run All.
- **Les `.WAV` de TIMIT sont du NIST SPHERE** ; soundfile les lit, rien à convertir.
- Le vocabulaire utilise les **ligatures à codepoint unique** — tout symbole composé `t`+`ʃ` est un bug.
- **Trente époques écrivent trente modèles entiers** : `save_pretrained` ne sait pas ne sauver que la tête, soit ~360 Mo × 30 ≈ 10,8 Go pour un `/kaggle/working` plafonné à 20 Go. En V1 l'encodeur est gelé, donc ces copies ne diffèrent que par la tête — élaguer avant de sauver la version.
- Fin de session payante : **détruire** pod et volume, pas arrêter (facturation à l'arrêt) ; `tmux` pour tout lancement SSH ; le lien MEGA se supprime en fin de chantier.

## Après les runs — qualification, de retour au poste

1. Ajouter le checkpoint retenu comme entrée de la table de `bench/matrix.py` (`Candidate`, avec `vocabulary` pointant vers vitouphy) — lecture neuve, cache propre.
2. `faults.py` : la séparation fautes/témoins tient-elle. Puis la densité : les sons que la grille actuelle perd (un sur huit — le `k` de *picked*, les `t` d'*important*, cf. `dense-grid.md`) doivent porter de la masse à leur place ; les sondes `tmp/blank_penalty.py` / `tmp/missing_mass.py` savent le relire.
3. Si V1 tient : export ONNX (`bench/export.py`), concordance, mesure téléphone — le chemin déjà balisé du banc.
4. Élaguer ce doc et les docs GPU une fois le modèle en place — le code et les commits deviennent le registre.

## Bornes de la session d'exécution

- Commits granulaires, messages en anglais, l'utilisateur seul auteur — cf. `dev_base`.
- Ne pas toucher aux contrats du banc (`matrix.py`, `faults.py`) au-delà de l'ajout d'un candidat.
- Toute envie de « corriger » une décision actée ci-dessus se consigne comme question dans le TODO et se discute, elle ne s'exécute pas.
