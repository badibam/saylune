# L'atelier — affiner le modèle de sons

Ce qui fabrique le remplaçant de `timit-ipa` : wav2vec2 + tête CTC phonémique entraînée avec label priors (Huang et al., ICASSP 2024). Le pourquoi est dans `../docs/design/dense-grid.md`, le chiffrage GPU dans `../docs/design/gpu-pricing.md`, la logistique de location dans `../docs/design/gpu-runbook.md`. Rien de ce dossier ne part dans l'app : comme le banc, c'est l'instrument.

**L'alphabet de sortie est celui de `timit-ipa`, à l'identique** — son `vocab.json` est relu depuis le cache HF et le repliement TIMIT 61 → 39 (Lee & Hon) vise ses symboles, ligatures comprises (ʧ, ʤ). C'est ce qui permet à `bench/matrix.py` et `bench/faults.py` de lire le modèle affiné sans modification, et fait de la comparaison avant/après une comparaison à iso-alphabet. Les silences (h#, pau, epi, fermetures) et le coup de glotte q ne sont pas des cibles : le silence appartient au blank.

## Dérouler

Même venv et même cache que le banc (`../bench/README.md`) ; `HF_HOME` obligatoire, échec franc sinon.

```
HF_HOME=$PWD/tmp/hf tmp/venv/bin/python train/manifest.py       # décode tout le corpus, écrit tmp/train/manifest.json
HF_HOME=$PWD/tmp/hf tmp/venv/bin/python train/train.py --steps 2 --utterances 8   # pas à blanc CPU
HF_HOME=$PWD/tmp/hf tmp/venv/bin/python train/train.py --encoder facebook/wav2vec2-xls-r-300m --unfreeze --checkpointing --lr 1e-4   # le régime en cours (GPU)
```

Par défaut le script n'entraîne que la tête sur une oreille gelée — le régime des deux premières générations, que la mesure a condamné (cf. `../docs/design/dense-grid.md`). Le régime en cours est l'affinage complet ; ses arguments et le mode opératoire Kaggle sont dans `../docs/design/training-handoff.md`. L'extracteur convolutionnel reste gelé quoi qu'il arrive.

Le manifeste et les checkpoints vivent sous `tmp/train/`, régénérables. Les hyperparamètres exposés (`--lr`, `--prior-weight`, `--epochs`, `--warmup`) sont des points de départ à balayer sur GPU, pas des valeurs qualifiées ; ce qui qualifie un checkpoint reste `bench/faults.py`, jamais la loss.
