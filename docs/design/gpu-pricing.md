# Chiffrage GPU — affinage wav2vec2 + tête CTC à label priors sur TIMIT

Bench réalisé le **24 août 2026**. Prix relevés en USD (tarifs publics à la date du bench), conversion à **1 EUR = 1,168 USD**.

---

## 0. Résultat en une ligne

**Le projet entier — les trois variantes, 4 runs chacune — tient sous 30 € chez le meilleur fournisseur payant, et V1+V2 tiennent à 0 € sur Kaggle.** Le corpus est trop petit pour que l'arbitrage de prix ait un enjeu réel : entre le fournisseur le moins cher et le plus cher du tableau, l'écart cumulé sur tout le projet est de l'ordre de 200 €. Le critère de choix n'est donc pas le prix horaire mais **la friction d'accès et la fiabilité** (quota, disponibilité des cartes, abonnement obligatoire, risque de préemption). La suite du document justifie ce résultat et donne quand même la grille complète demandée.

---

## 1. Révision des durées

Les durées du brief sont **surestimées d'un facteur 2 à 4**. Point d'ancrage publié : un affinage complet `wav2vec2-base` sur TIMIT prend **≈ 1,25 h-GPU sur un seul A100 80 Go** (Implementation details, arXiv:2203.09829 — la même source situe LJSpeech à ~6 h et LibriSpeech-10h à ~5,5 h, ce qui donne confiance sur l'ordre de grandeur). TIMIT ne fait que 5 h d'audio ; à ~30-50 époques on reste dans l'heure sur carte moderne.

Base retenue pour le chiffrage, **temps de calcul pur sur A100 80 Go** :

| variante | durée retenue | vs brief |
|---|---|---|
| V1 — tête seule, encodeur gelé | 0,75 h | brief : 1–3 h |
| V2 — affinage complet `base` | 1,5 h | brief : 3–8 h |
| V3 — affinage complet `large`/`xls-r-300m` | 7 h | brief : 10–25 h |

V1 est plus rapide que V2 sans être 100× plus rapide malgré les 30 k paramètres entraînés : la passe avant traverse quand même les 95 M gelés, seule la rétropropagation est économisée. Compter un facteur 2, pas plus.

**Facteurs de vitesse relatifs** appliqués aux autres cartes (transformer fp16/bf16 de cette taille, A100 80 Go = 1,00) :

| carte | facteur | | carte | facteur |
|---|---|---|---|---|
| H100 PCIe | 1,80 | | RTX A6000 48 Go / A40 48 Go | 0,50 |
| RTX 5090 | 1,20 | | RTX 3090 24 Go | 0,45 |
| A100 80 / 40 Go | 1,00 / 0,95 | | RTX A5000 24 Go | 0,35 |
| RTX 4090 24 Go | 0,95 | | L4 24 Go | 0,30 |
| L40S 48 Go | 0,85 | | RTX A4000 16 Go | 0,25 |
| | | | T4 16 Go / P100 16 Go | 0,15 / 0,12 |

Deux corrections appliquées dans les tableaux :

- **+0,3 h facturée par run** : boot de l'instance, `pip install`, téléversement de ~1 Go, téléchargement des poids pré-entraînés, rapatriement du checkpoint. Sur des jobs d'une heure, cet overhead n'est pas négligeable — il représente jusqu'à 30 % de la facture V1.
- **V3 sur carte 24 Go** : accumulation de gradient + *gradient checkpointing* obligatoires, pénalité de **×1,45** sur la durée. V3 est infaisable sous 24 Go.

**Multiplicateur d'essais : 4 runs** (milieu de la fourchette 3–5 du brief).

Ces facteurs sont des estimations d'ordre de grandeur, pas des mesures. La marge d'erreur réelle est de ±50 % — ce qui, vu les montants en jeu, ne change aucune conclusion.

---

## 2. Grille variante × fournisseur × carte × durée × coût

Trié par coût croissant sur 4 runs. « h fact. » = durée facturée par run, overhead inclus.

### V1 — tête seule, encodeur `base` gelé (~8 Go VRAM)

| fournisseur | mode | carte | $/h | h fact. | $/run | $ × 4 | € × 4 | note |
|---|---|---|---|---|---|---|---|---|
| **Kaggle** | gratuit | P100 16 Go | 0,00 | 6,5 | 0,00 | **0,00** | **0** | 30 h/sem, session 12 h |
| **Kaggle** | gratuit | T4 ×2 16 Go | 0,00 | 5,3 | 0,00 | **0,00** | **0** | 30 h/sem |
| Vast.ai | interruptible | RTX 4090 24 Go | 0,16 | 1,1 | 0,17 | 0,70 | 1 | prix plancher marché |
| Vast.ai | interruptible | RTX 3090 24 Go | 0,12 | 2,0 | 0,24 | 0,94 | 1 | prix plancher marché |
| **RunPod** | Community | RTX 4090 24 Go | 0,34 | 1,1 | 0,37 | **1,48** | **1** | |
| Vast.ai | on-demand vérifié | RTX 4090 24 Go | 0,35 | 1,1 | 0,38 | 1,53 | 1 | |
| RunPod | Community | RTX A5000 24 Go | 0,16 | 2,4 | 0,39 | 1,56 | 1 | |
| RunPod | Community | RTX 3090 24 Go | 0,22 | 2,0 | 0,43 | 1,73 | 1 | |
| RunPod | Community | RTX A6000 48 Go | 0,33 | 1,8 | 0,59 | 2,38 | 2 | |
| RunPod | Community | A40 48 Go | 0,35 | 1,8 | 0,63 | 2,52 | 2 | |
| RunPod | Secure | RTX 4090 24 Go | 0,69 | 1,1 | 0,75 | 3,01 | 3 | |
| Colab Pro | compute units | T4 16 Go | 0,20 | 5,3 | 1,06 | 4,24 | 4 | ~2 CU/h, carte non garantie |
| Vast.ai | on-demand vérifié | A100 80 Go | 1,09 | 1,1 | 1,14 | 4,58 | 4 | |
| Lambda | on-demand | RTX A6000 48 Go | 0,80 | 1,8 | 1,44 | 5,76 | 5 | pas de spot |
| RunPod | Secure | A100 80 Go | 1,39 | 1,1 | 1,46 | 5,84 | 5 | |
| Colab Pro | compute units | A100 40 Go | 1,50 | 1,1 | 1,63 | 6,54 | 6 | ~15 CU/h, carte non garantie |
| Scaleway | on-demand EU | L40S 48 Go | 1,47 | 1,2 | 1,74 | 6,95 | 6 | facturation horaire |
| OVHcloud | on-demand EU | L40S 48 Go | 1,69 | 1,2 | 2,00 | 7,99 | 7 | Gravelines |
| Lambda | on-demand | A100 40 Go | 1,99 | 1,1 | 2,17 | 8,67 | 7 | pas de spot |
| Scaleway | on-demand EU | L4 24 Go | 0,79 | 2,8 | 2,21 | 8,85 | 8 | facturation horaire |
| Lambda | on-demand | H100 PCIe 80 Go | 3,29 | 0,7 | 2,36 | 9,43 | 8 | pas de spot |
| Paperspace | Core | RTX A4000 16 Go | 0,76 | 3,3 | 2,51 | 10,03 | 9 | + plan Pro 8 $/mois |
| OVHcloud | on-demand EU | L4 24 Go | 0,91 | 2,8 | 2,55 | 10,19 | 9 | Gravelines |
| Lambda | on-demand | A100 80 Go | 2,79 | 1,1 | 2,93 | 11,72 | 10 | vendu en nœud 8× |
| OVHcloud | on-demand EU | A100 80 Go | 3,07 | 1,1 | 3,22 | 12,89 | 11 | Gravelines |
| Paperspace | Core | A100 80 Go | 3,18 | 1,1 | 3,34 | 13,36 | 11 | + plan Growth |
| Paperspace | Core | RTX A5000 24 Go | 1,38 | 2,4 | 3,37 | 13,48 | 12 | + plan Growth |
| Paperspace | Core | RTX A6000 48 Go | 1,89 | 1,8 | 3,40 | 13,61 | 12 | + plan Growth |

### V2 — affinage complet `base`, 95 M (12–16 Go VRAM)

| fournisseur | mode | carte | $/h | h fact. | $/run | $ × 4 | € × 4 | note |
|---|---|---|---|---|---|---|---|---|
| **Kaggle** | gratuit | T4 ×2 16 Go | 0,00 | 10,3 | 0,00 | **0,00** | **0** | ⚠ 2 runs/semaine max (quota 30 h) |
| Kaggle | gratuit | P100 16 Go | 0,00 | 12,8 | 0,00 | 0,00 | 0 | ⚠ **dépasse la session de 12 h** |
| Vast.ai | interruptible | RTX 4090 24 Go | 0,16 | 1,9 | 0,30 | 1,20 | 1 | prix plancher marché |
| Vast.ai | interruptible | RTX 3090 24 Go | 0,12 | 3,6 | 0,44 | 1,74 | 1 | prix plancher marché |
| **RunPod** | Community | RTX 4090 24 Go | 0,34 | 1,9 | 0,64 | **2,56** | **2** | |
| Vast.ai | on-demand vérifié | RTX 4090 24 Go | 0,35 | 1,9 | 0,66 | 2,63 | 2 | |
| RunPod | Community | RTX A5000 24 Go | 0,16 | 4,6 | 0,73 | 2,93 | 3 | |
| RunPod | Community | RTX 3090 24 Go | 0,22 | 3,6 | 0,80 | 3,20 | 3 | |
| RunPod | Community | RTX A6000 48 Go | 0,33 | 3,3 | 1,09 | 4,36 | 4 | |
| RunPod | Community | A40 48 Go | 0,35 | 3,3 | 1,15 | 4,62 | 4 | |
| RunPod | Secure | RTX 4090 24 Go | 0,69 | 1,9 | 1,30 | 5,19 | 4 | |
| Vast.ai | on-demand vérifié | A100 80 Go | 1,09 | 1,8 | 1,96 | 7,85 | 7 | |
| Colab Pro | compute units | T4 16 Go | 0,20 | 10,3 | 2,06 | 8,24 | 7 | carte non garantie |
| RunPod | Secure | A100 80 Go | 1,39 | 1,8 | 2,50 | 10,01 | 9 | |
| Lambda | on-demand | RTX A6000 48 Go | 0,80 | 3,3 | 2,64 | 10,56 | 9 | pas de spot |
| Colab Pro | compute units | A100 40 Go | 1,50 | 1,9 | 2,82 | 11,27 | 10 | carte non garantie |
| Scaleway | on-demand EU | L40S 48 Go | 1,47 | 2,1 | 3,04 | 12,14 | 10 | facturation horaire |
| OVHcloud | on-demand EU | L40S 48 Go | 1,69 | 2,1 | 3,49 | 13,96 | 12 | Gravelines |
| Lambda | on-demand | H100 PCIe 80 Go | 3,29 | 1,1 | 3,73 | 14,91 | 13 | pas de spot |
| Lambda | on-demand | A100 40 Go | 1,99 | 1,9 | 3,74 | 14,96 | 13 | pas de spot |
| Scaleway | on-demand EU | L4 24 Go | 0,79 | 5,3 | 4,19 | 16,75 | 14 | facturation horaire |
| Paperspace | Core | RTX A4000 16 Go | 0,76 | 6,3 | 4,79 | 19,15 | 16 | + plan Pro |
| OVHcloud | on-demand EU | L4 24 Go | 0,91 | 5,3 | 4,82 | 19,29 | 17 | Gravelines |
| Lambda | on-demand | A100 80 Go | 2,79 | 1,8 | 5,02 | 20,09 | 17 | vendu en nœud 8× |
| OVHcloud | on-demand EU | A100 80 Go | 3,07 | 1,8 | 5,53 | 22,10 | 19 | Gravelines |
| Paperspace | Core | A100 80 Go | 3,18 | 1,8 | 5,72 | 22,90 | 20 | + plan Growth |
| Paperspace | Core | RTX A6000 48 Go | 1,89 | 3,3 | 6,24 | 24,95 | 21 | + plan Growth |
| Paperspace | Core | RTX A5000 24 Go | 1,38 | 4,6 | 6,33 | 25,31 | 22 | + plan Growth |

### V3 — affinage complet `large`/`xls-r-300m`, ~315 M (24–40 Go VRAM)

Kaggle et Colab-T4 sortent de la grille : 16 Go de VRAM insuffisants. Les lignes marquées **(acc.)** utilisent l'accumulation de gradient sur carte 24 Go, avec la pénalité de ×1,45 déjà intégrée.

**Le verdict sur les 16 Go suppose que les activations sont stockées ; avec recalcul (`--checkpointing`) elles cessent de dominer, et le T4 redevient candidat.** Ce qui reste — poids, gradients, états AdamW pour 315 M de paramètres en fp32 — est de l'ordre de 5 Go, déduit et non mesuré ; `training-handoff.md` porte la cellule de pas à blanc qui le vérifie, et le temps par époque qui décidera vraiment.

| fournisseur | mode | carte | $/h | h fact. | $/run | $ × 4 | € × 4 | note |
|---|---|---|---|---|---|---|---|---|
| Vast.ai | interruptible | RTX 4090 24 Go | 0,16 | 11,0 | 1,76 | 7,03 | 6 | (acc.) prix plancher |
| Vast.ai | interruptible | RTX 3090 24 Go | 0,12 | 22,9 | 2,74 | 10,97 | 9 | (acc.) |
| **RunPod** | Community | RTX 4090 24 Go | 0,34 | 11,0 | 3,73 | **14,94** | **13** | (acc.) |
| Vast.ai | on-demand vérifié | RTX 4090 24 Go | 0,35 | 11,0 | 3,84 | 15,38 | 13 | (acc.) |
| RunPod | Community | RTX A5000 24 Go | 0,16 | 29,3 | 4,69 | 18,75 | 16 | (acc.) |
| **RunPod** | Community | RTX A6000 48 Go | 0,33 | 14,3 | 4,72 | **18,88** | **16** | pas d'accumulation |
| RunPod | Community | A40 48 Go | 0,35 | 14,3 | 5,00 | 20,02 | 17 | pas d'accumulation |
| RunPod | Community | RTX 3090 24 Go | 0,22 | 22,9 | 5,03 | 20,11 | 17 | (acc.) |
| RunPod | Secure | RTX 4090 24 Go | 0,69 | 11,0 | 7,58 | 30,32 | 26 | (acc.) |
| Vast.ai | on-demand vérifié | A100 80 Go | 1,09 | 7,3 | 7,96 | 31,83 | 27 | |
| RunPod | Secure | A100 80 Go | 1,39 | 7,3 | 10,15 | 40,59 | 35 | |
| Lambda | on-demand | RTX A6000 48 Go | 0,80 | 14,3 | 11,44 | 45,76 | 39 | pas de spot |
| Colab Pro | compute units | A100 40 Go | 1,50 | 7,7 | 11,50 | 46,01 | 39 | ⚠ session/carte non garanties |
| Scaleway | on-demand EU | L40S 48 Go | 1,47 | 8,5 | 12,55 | 50,19 | 43 | meilleure option EU |
| Lambda | on-demand | H100 PCIe 80 Go | 3,29 | 4,2 | 13,78 | 55,13 | 47 | le plus rapide en horloge |
| OVHcloud | on-demand EU | L40S 48 Go | 1,69 | 8,5 | 14,42 | 57,70 | 49 | Gravelines |
| Lambda | on-demand | A100 40 Go | 1,99 | 7,7 | 15,26 | 61,04 | 52 | |
| Lambda | on-demand | A100 80 Go | 2,79 | 7,3 | 20,37 | 81,47 | 70 | vendu en nœud 8× |
| OVHcloud | on-demand EU | A100 80 Go | 3,07 | 7,3 | 22,41 | 89,64 | 77 | |
| Paperspace | Core | A100 80 Go | 3,18 | 7,3 | 23,21 | 92,86 | 80 | + plan Growth |
| Scaleway | on-demand EU | L4 24 Go | 0,79 | 34,1 | 26,97 | 107,86 | 92 | (acc.) à éviter |
| Paperspace | Core | RTX A6000 48 Go | 1,89 | 14,3 | 27,03 | 108,11 | 93 | + plan Growth |
| OVHcloud | on-demand EU | L4 24 Go | 0,91 | 34,1 | 31,06 | 124,25 | 106 | (acc.) à éviter |
| Paperspace | Core | RTX A5000 24 Go | 1,38 | 29,3 | 40,43 | 161,74 | 138 | + plan Growth, à éviter |

---

## 3. Recommandation par variante

### V1 → **Kaggle**, gratuit
4 runs = ~26 h de P100, soit un quota hebdomadaire (30 h). Une session tient largement sous le plafond de 12 h. Aucune raison de payer. Repli si le quota est saturé ou si l'environnement Kaggle résiste : **RunPod Community, RTX 4090** — 1,48 $ les 4 runs.

### V2 → **Kaggle en T4 ×2 si la cadence le permet, sinon RunPod Community 4090**
Le P100 est disqualifié : ~12,8 h par run, au-dessus du plafond de session de 12 h — le run mourrait avant la fin. Le T4 ×2 passe à ~10,3 h, mais le quota de 30 h/semaine ne laisse que **2 runs par semaine**. Si les 3–5 runs doivent s'enchaîner en une session de travail, Kaggle ne convient pas et le bon choix est **RunPod Community sur RTX 4090 : 2,56 $ les 4 runs**. Payer 2,50 $ pour éliminer le quota et les contraintes de session est évident.

### V3 → **RunPod Community, RTX A6000 ou A40 48 Go** — ~19–20 $ les 4 runs
Le 4090 à 24 Go est nominalement moins cher (14,94 $) mais impose accumulation de gradient et *gradient checkpointing* : réglages en plus, comportement d'entraînement modifié, risque d'OOM à mi-run. Les 5 $ d'écart sur tout V3 ne justifient pas cette complication. Prendre les 48 Go et entraîner sans contorsion.

Si V3 devient la variante centrale du projet plutôt qu'un recours, **Scaleway L40S** (50 $ les 4 runs, Paris/Varsovie, facturation horaire) devient défendable : matériel datacenter, hébergement EU, pas de marketplace à auditer, pour ~30 $ de plus sur l'ensemble.

### Budget total du projet
| scénario | coût |
|---|---|
| Cas nominal (V1 seul, 4 runs) | **0 €** sur Kaggle, ou 1,50 $ sur RunPod |
| V1 + V2, 4 runs chacun | 0 € (Kaggle) à **4 $** (RunPod 4090) |
| V1 + V2 + V3, 4 runs chacun | **~23 $ (≈ 20 €)** en tout-RunPod Community |
| Même chose, 5 runs, chez le plus cher raisonnable (Scaleway L40S) | ~87 $ (≈ 75 €) |

---

## 4. Sensibilité — à partir de combien de runs le choix bascule-t-il ?

La réponse honnête : **jamais, dans la plage utile.** Les classements des trois tableaux sont invariants par changement du multiplicateur d'essais, puisque le coût est strictement linéaire en nombre de runs et qu'aucun fournisseur de la liste n'a de tarif dégressif au volume à cette échelle. Multiplier les runs par 5 multiplie tous les coûts par 5 sans réordonner quoi que ce soit.

Les seuls véritables points de bascule sont **non tarifaires** :

| seuil | effet |
|---|---|
| **V2, > 2 runs par semaine** | Le quota Kaggle (30 h/sem) sature. Bascule vers RunPod, coût marginal ~0,64 $/run. |
| **V2 sur P100 Kaggle, dès le 1er run** | Le run (12,8 h) dépasse la session de 12 h. Le P100 est éliminé quel que soit le nombre de runs. |
| **V3, quelle que soit la cadence** | 16 Go insuffisants : Kaggle et Colab-T4 hors jeu dès le run 1. |
| **Paperspace, à partir de A5000** | Le plan Growth (métré 0,058 $/h, plafonné à 39 $/mois) s'ajoute. Sur un projet one-shot de quelques heures, le coût métré est négligeable, mais Paperspace reste 4 à 10× plus cher à la carte équivalente. Aucun scénario ne le fait gagner ici. |
| **Vast.ai interruptible vs RunPod Community** | Vast est ~2× moins cher, soit une économie de **8 $ sur tout le projet**. Une seule préemption non checkpointée sur un run V3 de 11 h annule le gain. Le calcul penche vers RunPod tant que le budget total reste sous ~100 $. |
| **Localisation EU (Scaleway/OVH) vs US** | Surcoût de ~30–70 $ sur l'ensemble du projet. Le brief indique que la localisation n'est pas critique — mais l'écart absolu est si faible que le choix EU reste défendable pour d'autres raisons (facturation en euros, TVA, absence de dépôt en devise). |

En clair : **arrêter d'optimiser le prix horaire et optimiser le temps humain.** Une heure de mise au point d'une instance Vast récalcitrante coûte plus cher que l'intégralité du budget GPU du projet.

---

## 5. Pièges relevés en route

**Licence du corpus — le point le plus important.** TIMIT est distribué par le LDC sous licence restrictive, ce n'est pas un corpus librement redistribuable. Conséquences concrètes : sur Kaggle, il faut le charger comme **dataset privé**, jamais public (les notebooks du tier gratuit sont publics chez certains fournisseurs — vérifier le réglage). Ne pas le pousser sur un bucket public ni sur un hôte Vast.ai non vérifié. Ce risque juridique est réel là où le risque financier ne l'est pas.

**RunPod.** Deux tiers de prix très différents pour la même carte : Community (hôtes vérifiés pairs) et Secure (datacenters Tier 3/4), souvent du simple au double. Les tableaux ci-dessus utilisent Community, ce qui est adapté à un job one-shot sans données sensibles. Piège de facturation : le stockage persistant est facturé **même pod arrêté** (0,10 $/Go/mois en marche, 0,20 $/Go/mois à l'arrêt) — détruire le volume à la fin, pas seulement arrêter le pod. Bon point : ingress et egress gratuits sur les Pods, donc les 1–2 Go entrants et 1,5 Go sortants ne coûtent rien. Crédit minimum ~10 $ à l'inscription.

**Vast.ai.** Marketplace : la qualité d'hôte varie énormément. Filtrer sur score de fiabilité > 98 % et nombre de locations élevé avant de réserver. Piège récurrent signalé par les utilisateurs : **arrêter une instance sans la détruire continue de facturer** — il faut détruire explicitement. Des cas de facturation d'instances non fonctionnelles sont documentés. Les prix planchers affichés ($0,12–0,16/h) correspondent à des hôtes non vérifiés ; sur hôte vérifié compter 2 à 3× plus.

**Lambda.** Pas d'offre spot du tout, et le catalogue ne descend pas sous les cartes datacenter — le moins cher utilisable ici est un A6000 à 0,80 $/h. Surtout : **les A100 SXM sont vendus en nœuds 8×**, ce qui rend le tarif « 2,79 $/GPU/h » trompeur pour un job mono-carte. Les tarifs on-demand ont augmenté sur 2025–2026 (H100 SXM passé de 2,99 à 3,99 $/h) sous l'effet de la demande, et la disponibilité est irrégulière.

**Paperspace (DigitalOcean).** À partir de l'A5000, les cartes sont **gated derrière l'abonnement Growth**. Bonne nouvelle : Growth est métré à 0,058 $/h avec un plafond à 39 $/mois, pas un forfait sec — sur un projet de quelques heures c'est quelques centimes. Mauvaise nouvelle : les tarifs horaires eux-mêmes sont 4 à 10× ceux de RunPod Community à carte équivalente. Trois régions seulement (NY2, CA1, AMS1). Contrairement à RunPod, l'egress est facturé.

**Scaleway et OVHcloud.** Les deux facturent **à l'heure entamée**, pas à la seconde — sur un run V1 d'1,1 h on paie 2 h. Ni l'un ni l'autre ne propose de spot. Catalogue étroit : L4, L40S, H100 chez Scaleway ; L4, L40S, A100, V100 chez OVH, tous en Gravelines (GRA11) sans choix de région pour les cartes récentes. Coûts annexes à ne pas oublier : chez Scaleway le Block Storage et l'IP flexible sont **en sus** du prix affiché, et le Scratch Storage disparaît à l'extinction de la machine (attention aux checkpoints). Disponibilité effective faible — un tracker relevait 6 instances GPU en stock sur 32 chez Scaleway. Anomalie tarifaire chez OVH : l'A100 y est affiché **plus cher que le H100**, inversion à vérifier directement avant de budgéter.

**Google Colab.** Le modèle en *compute units* (0,10 $/CU) masque le vrai coût : un T4 brûle ~2 CU/h, un A100 ~15 CU/h — soit ~1,50 $/h, sans que **la carte obtenue soit garantie**. Les CU se consomment aussi pendant l'installation des dépendances. Pro = 9,99 $/100 CU, Pro+ = 49,99 $/500 CU. Sur un job reproductible où l'on veut comparer des hyperparamètres entre runs, l'attribution aléatoire de carte est disqualifiante : deux runs « identiques » ne tourneront pas sur le même matériel.

**Kaggle.** Quota ~30 h/semaine (parfois « flottant » à la hausse selon la charge), sessions plafonnées à **12 h**, pas d'accès root, environnement figé. C'est ce plafond de 12 h qui élimine le P100 pour V2, pas le quota. Le T4 ×2 impose de gérer deux cartes (`DataParallel` ou ne se servir que d'une, auquel cas le facteur de vitesse chute). Kaggle permet désormais de lier un abonnement Colab Pro pour augmenter le quota hebdomadaire — piste si V1/V2 doivent être itérés intensément.

**Piège transversal — l'overhead écrase le calcul.** Sur V1, une heure de calcul s'accompagne de ~0,3 h de mise en place facturée. Choisir une image Docker pré-équipée PyTorch + torchaudio plutôt qu'installer à chaud change le coût de V1 de 20–30 %, davantage que le choix du fournisseur. Sur des jobs aussi courts, **le temps de démarrage est un poste de coût réel**.

---

## 6. Ce qui reste à vérifier avant de lancer

- Les prix ci-dessus sont des tarifs publics affichés au 24/08/2026 ; certains proviennent d'agrégateurs tiers plutôt que des pages officielles. **Revérifier le tarif exact de la carte retenue dans la console au moment de louer** — les prix marketplace bougent à l'heure.
- La disponibilité réelle des cartes 48 Go chez RunPod Community fluctue ; prévoir A40 comme substitut à l'A6000 (même VRAM, même prix, facteur de vitesse identique).
- Les facteurs de vitesse sont des estimations. Le seul chiffre solide est l'ancrage A100/V2 ≈ 1,25 h. **Chronométrer le premier run et recalibrer** — sur des jobs de cette taille, le chargement du corpus et le *dataloader* CPU peuvent devenir le goulot, ce qui aplatirait les écarts entre cartes et rendrait les cartes bas de gamme encore plus compétitives.
