# Affinage du modèle de sons — brief de chiffrage GPU

Document autoportant, destiné à une session dédiée. **Mission : bencher les fournisseurs de location GPU et chiffrer chaque variante ci-dessous** — tableau variante × fournisseur × carte × durée × coût, avec recommandation. Rien d'autre n'est demandé : pas de choix de recette, pas d'achat, pas de lancement.

## Le travail à chiffrer

Affinage d'un modèle wav2vec2 (encodeur speech auto-supervisé, poids pré-entraînés publics Apache-2.0) avec une tête CTC phonémique entraînée avec **label priors** (recette de Huang et al., ICASSP 2024, *Less Peaky and More Accurate CTC Forced Alignment by Label Priors* — code libre publié dans l'écosystème torchaudio). PyTorch, précision mixte fp16/bf16. Le contexte projet (pourquoi ce modèle, pourquoi cette recette) est dans `dense-grid.md` ; il n'est pas nécessaire au chiffrage.

**Corpus d'entraînement : TIMIT** — ~5 h d'audio 16 kHz, 630 locuteurs, ~1 Go à téléverser sur la machine louée. Corpus minuscule à l'échelle du domaine : les jobs sont courts.

## Les variantes (ordres de grandeur à vérifier, pas des mesures)

| variante | paramètres entraînés | VRAM estimée | durée estimée par run |
|---|---|---|---|
| V1 — tête seule, encodeur `base` gelé | ~30 k (la tête) sur 95 M gelés | ~8 Go | 1–3 h |
| V2 — affinage complet `base` | 95 M | 12–16 Go | 3–8 h |
| V3 — affinage complet `large`/`xls-r-300m` | ~315 M | 24–40 Go (ou accumulation de gradient sur carte plus petite, durée allongée) | 10–25 h |

**Multiplicateur d'essais : compter 3 à 5 runs par variante** (hyperparamètres, reprises) — le chiffrage doit le montrer, pas seulement le run unitaire. Le cas nominal du projet est V1 d'abord, V2/V3 seulement si la qualité de V1 est condamnée par la mesure ; le tableau doit donc permettre de lire chaque variante séparément.

## Contraintes du job

- **One-shot, aucune infra persistante** : on loue, on entraîne, on rapatrie les poids (0,4 à 1,3 Go), on rend la machine.
- **Interruptible/spot acceptable** si checkpoints — à intégrer dans la comparaison de prix.
- Téléversement entrant ~1–2 Go, sortant ~1,5 Go : les frais de trafic comptent s'il y en a.
- Utilisateur en France ; la localisation des données n'est pas critique (corpus de recherche, pas de donnée personnelle), le prix prime.

## Fournisseurs à comparer (liste indicative, à compléter)

Vast.ai, RunPod, Lambda Cloud, Paperspace, OVHcloud/Scaleway (option EU), Google Colab Pro/Pro+, Kaggle (gratuit, T4/P100, ~30 h/semaine — à évaluer sérieusement vu la taille des jobs V1/V2). Pour chacun : carte(s) adaptée(s) à la VRAM de chaque variante, prix horaire à la date du bench, mode spot/on-demand, frais annexes (stockage, trafic).

## Livrable attendu

1. Le tableau variante × fournisseur × carte × durée × coût unitaire × coût avec le multiplicateur d'essais.
2. Une recommandation par variante, et la sensibilité du choix (à partir de quel nombre de runs tel fournisseur bascule).
3. Les pièges relevés en route (quota d'inscription, cartes indisponibles, facturation minimale).
