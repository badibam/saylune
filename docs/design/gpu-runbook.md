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

## 3. Acheminer le corpus (~1 Go)

### Depuis votre machine : ça dépend de votre débit montant

| débit montant | durée pour 1 Go |
|---|---|
| fibre 500 Mb/s | ~20 s |
| fibre 100 Mb/s | ~1,5 min |
| VDSL ~8 Mb/s | ~18 min |
| ADSL ~1 Mb/s | ~2 h 20 |

Si vous êtes dans le bas du tableau, **ne transférez jamais depuis votre poste vers une instance allumée** : vous paieriez le GPU à ne rien faire pendant tout le transfert, et vous recommenceriez à chaque run.

### La bonne méthode : sortir votre connexion du circuit

Déposer TIMIT **une seule fois** sur un stockage objet, puis faire télécharger la machine louée directement depuis là. Le transfert devient datacenter-à-datacenter, typiquement 100 Mb/s à 1 Gb/s : 1 Go en quelques dizaines de secondes, quelle que soit votre ligne. Votre débit montant ne compte plus qu'une seule fois, au dépôt initial.

Options de bucket S3-compatible, toutes équivalentes ici :

| service | remarque |
|---|---|
| Scaleway Object Storage | facturé en euros, quelques centimes pour 1 Go, Paris |
| OVH Object Storage | idem, egress annoncé gratuit |
| Cloudflare R2 | egress gratuit |
| Backblaze B2 | très bon marché |

Sur la machine louée, ça se réduit à un `wget` d'une **URL présignée valable quelques heures** — aucun identifiant à déposer sur l'hôte.

### Ordre des opérations qui évite de payer pour rien

1. Déposer le corpus sur le bucket (depuis chez vous, GPU éteint).
2. Démarrer le pod avec une image Docker contenant déjà PyTorch + torchaudio.
3. `wget` l'URL présignée, décompresser.
4. Entraîner dans `tmux`, checkpoints sur `/workspace`.
5. Pousser les poids finaux (0,4–1,3 Go) vers le bucket.
6. **Détruire** le pod — pas seulement l'arrêter. Le stockage persistant continue d'être facturé sur un pod arrêté (voir les pièges du chiffrage).

### Cas particulier Kaggle — encore plus simple

Vous chargez TIMIT une seule fois comme **Dataset privé** (via le navigateur ou la CLI `kaggle`), et il est ensuite monté en lecture seule dans tous vos notebooks, gratuitement, sans jamais retransférer. Pour V1 et V2 c'est de loin le moins de friction : un seul upload depuis votre ligne, et c'est réglé pour tout le projet.

⚠ **Vérifier que le dataset est bien privé.** TIMIT est sous licence LDC, pas librement redistribuable.

### Les poids pré-entraînés ne vous concernent pas

~360 Mo pour `wav2vec2-base`, ~1,2 Go pour `xls-r-300m`. `transformers` les télécharge depuis le Hub **côté serveur**, automatiquement, à pleine bande passante datacenter. Rien à préparer.

---

## 4. Le cas Proton Drive

Techniquement possible, pratiquement déconseillé.

**Ce qui existe.** rclone dispose d'un backend `protondrive`. Il est en bêta et implémenté par rétro-ingénierie, Proton ne publiant pas la documentation de son API. En octobre 2025, le projet rclone a ouvert une discussion sur l'opportunité de le marquer comme non supporté, faute de mainteneur actif et avec un arriéré de tickets ouverts.

**Le problème de fond.** Proton Drive est chiffré de bout en bout : il n'existe pas de lien HTTP direct que la machine distante puisse récupérer avec un `wget`. Le déchiffrement se fait côté client. Il faut donc authentifier rclone avec **vos identifiants Proton complets** sur la machine louée — qui, chez Vast.ai ou RunPod Community, appartient à un particulier inconnu. C'est votre compte mail Proton que vous exposez pour économiser trois minutes de transfert.

**Si vous tenez au cloud grand public.** rclone gère proprement Google Drive, Dropbox ou Mega avec des jetons OAuth **révocables** — nettement plus acceptable qu'un mot de passe maître. Mais un bucket S3 avec une clé en lecture seule et courte durée de vie reste la réponse propre : rien de personnel ne touche la machine, révocation immédiate, et on reste dans l'esprit *one-shot, aucune infra persistante* du brief.

**Hygiène générale, quel que soit le stockage.** Sur une machine de marketplace : pas de clé SSH personnelle réutilisée ailleurs, pas de token Hugging Face en écriture, pas de credentials cloud à longue durée de vie. Générer ce qui est nécessaire, avec le périmètre minimal, et le révoquer après.

---

## 5. Récapitulatif — checklist avant le premier run

- [ ] TIMIT déposé sur un bucket (ou en Dataset **privé** Kaggle)
- [ ] Clé SSH publique enregistrée chez le fournisseur
- [ ] Image Docker retenue contenant déjà PyTorch + torchaudio (gain de plusieurs minutes facturées **par run** — poste de coût plus lourd que l'écart de prix entre deux fournisseurs)
- [ ] Script d'entraînement écrivant des checkpoints périodiques sur `/workspace`
- [ ] `tmux` systématique pour tout lancement
- [ ] URL présignée générée, valable quelques heures
- [ ] Procédure de fin claire : pousser les poids → **détruire** le pod et le volume
