# Mode opératoire — louer et piloter une machine GPU

Document compagnon de `gpu-pricing.md`. Répond à : quelle connexion faut-il, par quelle interface on accède à la machine, comment y acheminer le corpus, et d'où.

Rédigé le 24 août 2026.

---

## 0. Le principe à avoir en tête

**Rien ne tourne chez vous.** Le corpus, les poids et le calcul vivent sur la machine louée. Votre poste ne sert qu'à la piloter.

Votre connexion Internet n'intervient donc que sur deux points :

1. le **confort de pilotage** (SSH, Jupyter) — exigences très faibles ;
2. le **premier acheminement du corpus** (~1 Go) — le seul endroit où le débit mord vraiment, et qu'on peut contourner entièrement (§3).

Une conséquence pratique importante : la machine louée **continue de tourner et d'être facturée** quand vous vous déconnectez. La déconnexion ne met rien en pause.

---

## 1. Comment on accède à la machine

### RunPod / Vast.ai — UI web pour déployer, puis au choix

Le déploiement se fait dans une **console web** : choix de la carte, choix d'une image Docker (« template »), clic sur *Deploy*. La machine est prête en une trentaine de secondes. Ensuite, trois portes d'entrée, non exclusives :

| accès | mise en place | quand l'utiliser |
|---|---|---|
| **JupyterLab dans le navigateur** | inclus dans les images PyTorch standard, un clic depuis la console | itérer sur des hyperparamètres, inspecter des tenseurs, tracer une courbe |
| **SSH depuis votre terminal** | coller sa clé publique dans les réglages du compte ; la console fournit la commande `ssh` | lancer un entraînement long — le mode recommandé |
| **Terminal web** dans la console | rien à faire | dépannage quand le SSH ne passe pas |

Il existe aussi des CLI (`runpodctl`, `vastai`) permettant de créer, surveiller et détruire les instances en ligne de commande. Utile si vous voulez scripter les 4 runs d'une variante, inutile pour un usage manuel.

### Kaggle / Colab — navigateur uniquement

Notebook dans le navigateur, **pas d'accès root, pas de SSH**. Vous ne choisissez pas la machine, vous choisissez un type d'accélérateur et Google/Kaggle vous en attribue un. Environnement pré-installé (PyTorch, torchaudio présents). C'est le moins de friction pour V1, au prix d'un contrôle nul sur l'environnement.

### Scaleway / OVHcloud — VM Linux nue

Console web pour créer l'instance, puis **SSH**, et vous installez tout vous-même : pilotes selon l'image choisie, CUDA, PyTorch, dépendances. Comptez du temps de mise en place à chaque instanciation si vous ne préparez pas d'image. En contrepartie, machine classique, IP fixe, comportement prévisible.

---

## 2. Qualité de connexion requise

### Pour le pilotage : quasiment n'importe quoi suffit

SSH consomme quelques dizaines de ko/s. JupyterLab est plus lourd au chargement initial mais reste modeste ensuite. Aucun besoin de fibre.

Ce qui compte est la **latence**, pour le confort de frappe. Un serveur US depuis la France tourne autour de 100–150 ms : utilisable, mais on sent le décalage dans un terminal interactif. Une instance européenne (Scaleway Paris, OVH Gravelines, ou un hôte EU sur RunPod/Vast — le filtre existe dans les deux consoles) descend sous 30 ms. Agréable, non décisif.

### Le vrai risque : la coupure, pas le débit

Si votre session SSH tombe, **le processus lancé dedans meurt avec elle** — et le pod continue de facturer dans le vide. Une micro-coupure Wi-Fi à la 6ᵉ heure d'un run V3 vous fait perdre le run entier.

Deux réflexes non négociables :

1. **Lancer l'entraînement dans `tmux`** (ou `screen`, ou à défaut `nohup … &`). Le processus est détaché de la session : vous vous reconnectez plus tard et retrouvez votre run en cours.

   ```bash
   ssh root@<hôte> -p <port>
   tmux new -s train
   python train.py 2>&1 | tee /workspace/train.log
   # Ctrl-b puis d pour détacher ; tmux attach -t train pour revenir
   ```

2. **Checkpoints réguliers sur disque.** De toute façon obligatoires si vous prenez de l'interruptible, et c'est aussi ce qui rend une coupure réseau bénigne.

JupyterLab est un peu plus tolérant — le kernel survit à la fermeture de l'onglet — mais la sortie console produite pendant la déconnexion est perdue, ce qui est pénible pour suivre une courbe de loss. Pour un run long, `tmux` + `tee` reste supérieur.

---

## 3. Acheminer le corpus (630 Mo réels)

Le TIMIT acheté pèse **630 Mo pour 25 208 fichiers** (`tmp/TIMIT/`, arborescence `lisa/data/timit/raw/TIMIT` — celle qu'attendent les loaders standard). Deux conséquences pratiques :

- **Toujours transférer en une seule archive** (`tar.gz`) : 25 000 petits fichiers à l'unité, c'est l'enfer de n'importe quel protocole. Le PCM se compresse mal, compter ~500 Mo archivés.
- **Les `.WAV` sont du NIST SPHERE, pas du RIFF** (en-tête `NIST_1A`) malgré l'extension. libsndfile — donc `soundfile` et le backend correspondant de torchaudio — les lit, mais **vérifier la chaîne de décodage en local avant de louer** : un décodage qui coince se débogue gratuitement sur le poste, pas au tarif GPU. Plus généralement, dérouler à blanc la préparation des données (manifeste, décodage de quelques fichiers, un pas d'entraînement sur CPU) avant le premier run loué.

### Depuis votre machine : ça dépend de votre débit montant

| débit montant | durée pour 1 Go |
|---|---|
| fibre 500 Mb/s | ~20 s |
| fibre 100 Mb/s | ~1,5 min |
| VDSL ~8 Mb/s | ~18 min |
| ADSL ~1 Mb/s | ~2 h 20 |

Si vous êtes dans le bas du tableau, **ne transférez jamais depuis votre poste vers une instance allumée** : vous paieriez le GPU à ne rien faire pendant tout le transfert, et vous recommenceriez à chaque run.

### La bonne méthode : sortir votre connexion du circuit

Déposer TIMIT **une seule fois** sur un stockage que la machine louée télécharge elle-même. Le transfert devient datacenter-à-datacenter, typiquement 100 Mb/s à 1 Gb/s : quelques dizaines de secondes, quelle que soit votre ligne. Votre débit montant ne compte plus qu'une seule fois, au dépôt initial.

**La voie retenue est MEGA par lien public** (§4) — le compte existe déjà, aucun bucket à créer. Les buckets S3-compatibles ci-dessous restent le repli si le quota MEGA de l'IP du pod est épuisé :

| service | remarque |
|---|---|
| Scaleway Object Storage | facturé en euros, quelques centimes pour 1 Go, Paris |
| OVH Object Storage | idem, egress annoncé gratuit |
| Cloudflare R2 | egress gratuit |
| Backblaze B2 | très bon marché |

Dans les deux cas, rien du compte ne touche l'hôte : la machine récupère une URL — lien MEGA ou URL présignée valable quelques heures — sans identifiant.

### Ordre des opérations qui évite de payer pour rien

1. Déposer l'archive du corpus sur MEGA (depuis chez vous, GPU éteint), générer le lien.
2. Démarrer le pod avec une image Docker contenant déjà PyTorch + torchaudio.
3. `megadl` le lien, décompresser.
4. Entraîner dans `tmux`, checkpoints sur `/workspace`.
5. Rapatrier les poids finaux (0,4–1,3 Go) par `scp` depuis votre poste — c'est le débit **descendant** de votre ligne qui compte, bien meilleur que le montant. MEGA ne sert pas au retour : téléverser vers le compte exigerait ses identifiants sur l'hôte.
6. **Détruire** le pod — pas seulement l'arrêter. Le stockage persistant continue d'être facturé sur un pod arrêté (voir les pièges du chiffrage).

### Cas particulier Kaggle — sans second upload

Le Dataset privé Kaggle n'exige pas de téléverser TIMIT une deuxième fois depuis votre ligne : un notebook Kaggle a Internet et les droits root, donc il peut `apt-get install megatools` et `megadl` le lien MEGA lui-même, à débit datacenter. Deux façons de s'en servir :

- **Directe** : chaque session de notebook re-télécharge l'archive au démarrage (~1 min) — zéro mise en place, le lien reste dans un notebook **privé** uniquement.
- **Dataset** : une fois le notebook exécuté, son output se convertit en **Dataset privé** dans l'interface Kaggle (« create dataset from notebook output »), monté ensuite en lecture seule dans tous les notebooks sans retélécharger.

Dans les deux cas, un seul upload depuis chez vous — celui vers MEGA — sert toutes les voies, Kaggle comme RunPod/Vast.

Persistance de session Kaggle, deux réglages qui ne se voient pas : le mode **« Files only »** (jamais « Variables », qui restaure l'état Python d'une session précédente — modèle déjà chargé, imports fantômes, l'inverse du reproductible), et le fait que `/kaggle/working` **survit d'une session à l'autre** — d'où un dossier de sortie **par run** (`train.py` refuse d'écrire dans un dossier qui porte déjà des checkpoints) et un ménage entre variantes, les fichiers persistés comptant dans le quota de stockage du compte.

⚠ **Vérifier que le dataset est bien privé.** TIMIT est sous licence LDC, pas librement redistribuable.

### Rapatrier la sortie d'un notebook — fichier par fichier, jamais l'archive

Trois voies existent pour récupérer les poids, deux échouent, et la première échoue **en silence**. Mesuré sur la génération V1b, 12 checkpoints de 377 641 832 o chacun :

- `kaggle kernels output <notebook>` rend une liste tronquée à trois entrées et écrit un `model.safetensors` de **0 octet**, sans message et sans code de retour non nul. Un fichier vide qui se fait passer pour un téléchargement abouti est le pire des deux échecs : rien ne prévient, et c'est au chargement des poids qu'on l'apprendrait.
- `kaggle datasets download -d <dataset>` — l'archive complète — répond **404** sur `DownloadDataset`, alors même que `datasets status` dit `ready` et que `datasets files` liste les 41 fichiers. Déclarer une licence sur le dataset n'y change rien : essayé, le 404 est identique une fois passé en `apache-2.0`.
- `kaggle datasets download -d <dataset> -f <chemin>` — un fichier à la fois — fonctionne, rend le fichier **nu** (jamais emballé en `.zip`) et affiche une barre de progression.

Le procédé retenu suit de là. Convertir d'abord la sortie du notebook en **Dataset privé** (bouton *New Dataset* du panneau Output, visibilité à vérifier), attendre que `datasets status` rende `ready`, puis lister pour vérifier que le dernier run du balayage y est — `datasets files` pagine à 20 lignes, d'où `--page-size 100`, faute de quoi la fin de la liste passe pour absente. Enfin, boucler :

```bash
for run in v1b-pw0.0 v1b-pw0.1 v1b-pw0.3 v1b-pw1.0; do
  for ep in 009 019 029; do
    for f in config.json model.safetensors prior.pt; do
      dest="tmp/train/runs-v1b/$run/epoch-$ep"
      [ -s "$dest/$f" ] && continue
      mkdir -p "$dest"
      tmp/venv/bin/kaggle datasets download \
        -d <compte>/<dataset> -f "tmp/train/runs/$run/epoch-$ep/$f" -p "$dest"
    done
  done
done
```

Le `[ -s ]` teste le fichier **non vide**, pas sa seule présence : c'est ce qui rend la boucle reprenable après une coupure, et c'est exactement le piège du fichier de 0 octet ci-dessus. Il ne couvre pas le fichier interrompu à mi-course, qui serait pris pour complet — d'où la vérification finale, qui ne doit rien afficher :

```bash
find tmp/train/runs-v1b -name model.safetensors -size -377641832c
```

Le CLI s'installe dans le venv du projet (`tmp/venv/bin/pip install kaggle`) et s'authentifie par un jeton créé dans Settings → API, déposé en `~/.kaggle/` — le mode `600` est obligatoire, le CLI refuse de démarrer sinon. Un transfert de plusieurs gigaoctets se lance dans `tmux`, la barre de progression n'existant qu'en avant-plan.

### Les poids pré-entraînés ne vous concernent pas

~360 Mo pour `wav2vec2-base`, ~1,2 Go pour `xls-r-300m`. `transformers` les télécharge depuis le Hub **côté serveur**, automatiquement, à pleine bande passante datacenter. Rien à préparer.

---

## 4. Les drives personnels — MEGA retenu, Proton écarté

**MEGA convient, par le lien public — c'est la voie retenue.** Un fichier MEGA partagé par lien se télécharge **sans aucun identifiant** : la clé de déchiffrement voyage dans le fragment de l'URL, et `megadl` (paquet `megatools`, dans Debian/Ubuntu) ou `mega-get` (MEGAcmd) la consomment côté machine louée. Rien du compte ne touche l'hôte — même profil de sécurité qu'une URL présignée S3. Ordre des opérations : archiver TIMIT, le déposer une fois sur MEGA depuis chez soi, générer le lien, `megadl '<lien>'` sur le pod. Deux réserves :

- **Licence LDC** : un lien public MEGA est accessible à quiconque le détient. Le lien est indevinable, mais il ne va **ni dans un notebook, ni dans un dépôt, ni dans un chat** — et il se supprime (ou la clé du dossier se régénère) une fois le projet fini.
- **Quota de transfert par IP** : le tier gratuit MEGA plafonne le téléchargement par adresse IP (~quelques Go par fenêtre de quelques heures). 630 Mo passent large, mais l'IP d'un hôte de marketplace est **partagée** et peut arriver déjà épuisée. Si `megadl` cale sur un quota, le repli est le bucket S3 du tableau ci-dessus — pas la peine de le monter d'avance.

Attention : ce qui précède vaut pour le **lien public**. Monter le compte MEGA lui-même via rclone (backend `mega`) exige **l'identifiant et le mot de passe complets** du compte — pas un jeton révocable — et n'a rien à faire sur une machine de marketplace.

**Proton Drive, lui, est écarté.** Son chiffrement de bout en bout ne laisse aucun lien HTTP qu'une machine distante puisse récupérer seul : il faudrait authentifier rclone (backend `protondrive`, bêta rétro-ingéniérée, quasi non maintenue) avec les identifiants Proton complets sur une machine appartenant à un inconnu. C'est le compte mail qu'on exposerait pour économiser trois minutes de transfert.

**Hygiène générale, quel que soit le stockage.** Sur une machine de marketplace : pas de clé SSH personnelle réutilisée ailleurs, pas de token Hugging Face en écriture, pas de credentials cloud à longue durée de vie. Générer ce qui est nécessaire, avec le périmètre minimal, et le révoquer après.

---

## 5. Récapitulatif — checklist avant le premier run

- [ ] Chaîne de données déroulée à blanc en local : décodage SPHERE vérifié, manifeste construit, un pas d'entraînement sur CPU
- [ ] Archive TIMIT déposée sur MEGA, lien généré (ou Dataset **privé** Kaggle pour V1/V2)
- [ ] Clé SSH publique enregistrée chez le fournisseur
- [ ] Image Docker retenue contenant déjà PyTorch + torchaudio (gain de plusieurs minutes facturées **par run** — poste de coût plus lourd que l'écart de prix entre deux fournisseurs)
- [ ] Script d'entraînement écrivant des checkpoints périodiques sur `/workspace`
- [ ] `tmux` systématique pour tout lancement
- [ ] Procédure de fin claire : rapatrier les poids par `scp` → **détruire** le pod et le volume → supprimer le lien MEGA
