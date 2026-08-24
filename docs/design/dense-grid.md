# La grille dense — diagnostic et voie de sortie

Carnet de la session, à élaguer. Répond aux deux problèmes ouverts par `letter-anchoring.md` : les sons manqués de la brique 3, la jointure fragile de la brique 4.

## Le diagnostic : une seule cause

Les deux problèmes sont la signature d'une même chose, connue et nommée dans la littérature : le comportement **peaky** du CTC (Zeyer et al. 2021). Un réseau entraîné en CTC n'est payé que pour la séquence, jamais pour la couverture : le blank absorbe presque toutes les trames, chaque son ne surnage qu'en un pic d'une ou deux trames. D'où une grille qui perd les sons brefs (brique 3), et deux cartes en pics qu'il faut élargir par convention pour les joindre (brique 4).

## Mesuré : les poids ne voient pas ces sons

Deux sondes (`tmp/blank_penalty.py`, `tmp/missing_mass.py`), sur les matrices `timit-ipa` en cache, quinze phrases, `eleven-us-eric`.

- **Pénalité de blank au décodage** : atténuer la colonne blank avant l'argmax, facteur balayé de 1 à 0,001. Retrouvés : 221/253 → 226/253 au mieux, les parasites passant de 15 à 28. Cinq sons gagnés, au prix d'un quasi-doublement du bruit.
- **La masse n'est pas dans la matrice** : alignement forcé de la séquence attendue (annotation du banc) pour fenêtrer chaque son manquant, puis pic de sa propre colonne à ±100 ms. La grande majorité plafonne sous 0,2 (le `k` de *picked* à 0,00, les `t` d'*important* à 0,01) pendant que le blank tient 1,00. Cinq sons portent 0,25–0,46 — ceux que la pénalité extrême repêchait.

Conséquence (déduite, pas mesurée) : aucune lecture de cette matrice n'ira au-delà de ~90 %. Le manque est dans les poids, pas dans la lecture.

## Les 95 % de charsiu, réattribués

`charsiu/en_w2v2_fc_10ms` : `fc` signifie *frame classification* — une tête entraînée à étiqueter chaque trame (étiquettes issues d'un alignement forcé), pas un CTC plus fin. Pas de blank, lecture dense. Le gain mesuré dans `letter-anchoring.md` vient très probablement de cet objectif d'entraînement, pas des 10 ms. **Le critère de choix du modèle de grille est la densité de la lecture (tête framewise contre tête CTC), pas le pas de temps.**

Nuance architecturale qui rouvre le jeu : la grille (où sont les cases) et la comparaison (les répartitions) peuvent venir de lectures différentes. Le reproche fait à charsiu — pardonner les substitutions — est rédhibitoire pour la comparaison, pas pour la segmentation. Et la grille ne se calcule que sur **l'audio de synthèse**, une fois par entrée de cache : un modèle de grille dédié peut se charger-passer-décharger, et son domaine est la parole propre d'un TTS, le cas facile.

## Les candidats recensés

| candidat | nature | licence | verdict |
|---|---|---|---|
| `charsiu/en_w2v2_fc_10ms` | framewise 10 ms | code MIT, **poids sans licence** | écarté, le reproche tient |
| MMS_FA (torchaudio) | CTC + *label priors* | poids **CC-BY-NC 4.0** | écarté |
| `pkadambi/wav2textgrid` | framewise 10 ms (Wav2TextGrid) | **MIT**, repo GitHub et poids | **à bencher** |
| affinage maison | framewise sur wav2vec2-base | propre par construction | repli chiffré ci-dessous |

**Wav2TextGrid**, le seul candidat sur étagère à licence propre. Réserves à lever au banc : architecture custom (`Wav2Vec2ForFrameClassificationSAT`, adaptation au locuteur — chargement non standard, pas de `vocab.json` dans le dépôt HF, l'inventaire vit dans le paquet pip) ; affiné sur de la parole d'enfants qui bégaient (corpus TOCS), domaine éloigné du nôtre — mais initialisé sur CommonVoice/LibriSpeech, et notre domaine est le TTS propre. La mesure est la même que pour charsiu : sons retrouvés sur les quinze phrases.

**L'affinage maison**, si Wav2TextGrid échoue. Le refus du G2P est un principe d'exécution : rien n'interdit un dictionnaire à l'entraînement, hors app, une fois. Recette charsiu reproduite chez nous : étiquettes de trames par alignement forcé (MFA, MIT) sur un corpus libre (LibriSpeech, CC-BY 4.0) — ou directement sur **nos propres rendus TTS en masse**, le domaine exact de la grille — puis tête framewise affinée sur `wav2vec2-base` (Apache-2.0). Poids à nous, licence propre par construction. Coût : un entraînement GPU (heures à jours sur une carte louée), pas un projet de recherche. Variante : CTC ré-entraîné avec *label priors* (recette du papier MMS_FA, code BSD dans torchaudio) — évite les étiquettes de trames, mais torchaudio passe en maintenance et la recette est à retrouver.

## Ce qui suit

1. Bencher `wav2textgrid` : chargement, inventaire, sons retrouvés contre `expected.SOUNDS`.
2. S'il passe : la jointure se refait sur deux cartes denses — par frontières de mots (fiables à 9 ms) puis appariement monotone dans le mot, plus d'élargissement à mi-chemin.
3. Poser ce dont une marque a besoin (tolérance au bord de syllabe) avant de recompter la jointure.
4. S'il échoue : l'affinage maison, sur rendus TTS de préférence.
