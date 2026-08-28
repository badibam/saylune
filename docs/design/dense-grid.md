# La grille dense — diagnostic et plan

Carnet, à élaguer une fois le code en place. Le plan en fin de doc est validé.

## Les deux besoins, et leur état

- **A — l'analyse des phonèmes** (`timit-ipa`). Deux offices sur le même tableau de probabilités : dresser la **grille** du modèle TTS (quels sons, où) et **comparer** l'apprenant à cette grille. C'est le grand malade : la grille perd un son sur huit, et là où elle n'a pas de case, aucune marque ne peut naître.
- **B — l'alignement des lettres** (`wav2vec2-base-960h`). Ancre les mesures au texte affiché. À moitié malade seulement : les frontières de **mots** sont excellentes (9 ms médian contre ElevenLabs), mais dans le mot chaque lettre est un pic d'une ou deux trames, pas une étendue. Marier deux cartes en pointillés a longtemps coûté cher ; la géométrie mots-d'abord de la brique 4 a depuis retiré la convention de comblement (« à mi-chemin ») qui payait ce mariage.

## Le diagnostic : une seule cause, prouvée

Les deux maux sont la signature du comportement **peaky** du CTC (Zeyer et al. 2021) : un réseau entraîné en CTC n'est payé que pour rendre la bonne *séquence*, jamais pour couvrir le temps — le blank absorbe presque toutes les trames, chaque symbole ne surnage qu'en pic.

Mesuré, sur les matrices `timit-ipa` en cache (quinze phrases, `eleven-us-eric`, sondes `tmp/blank_penalty.py` et `tmp/missing_mass.py`) :

- **Pénalité de blank au décodage** : facteur balayé de 1 à 0,001 → 221/253 sons retrouvés → 226/253 au mieux, les parasites passant de 15 à 28.
- **La masse n'est pas dans la matrice** : chaque son manquant fenêtré par alignement forcé de la séquence attendue (annotation du banc), pic de sa propre colonne lu à ±100 ms. La grande majorité plafonne sous 0,2 (le `k` de *picked* à 0,00, les `t` d'*important* à 0,01) pendant que le blank tient 1,00.

- **L'étendue manque, et elle manque partout** (`bench/boundaries.py`, 200 énoncés du split TEST de TIMIT, 5971 sons, bornes `.PHN` au niveau de l'échantillon) : un son reçoit **26,9 %** de sa durée réelle chez `timit-ipa` — et 26,9 / 26,7 / 26,7 % aux époques 9, 19 et 29 d'un affinage complet sur `xls-r-300m`. Ni le dégel de l'oreille, ni le changement de dos, ni la durée d'entraînement n'en rendent un millième. **La peakiness est donc une propriété du régime CTC nu, pas un défaut des poids sortants** — ce que cette section posait en diagnostic et que rien ne mesurait directement.

Conséquence (déduite) : aucune lecture de cette matrice ne dépassera ~90 %. Le manque est dans les poids, pas dans la lecture.

Les 95 % de charsiu, réattribués : `fc` dans son nom signifie *frame classification* — une tête entraînée à étiqueter chaque trame, pas un CTC plus fin. Le gain vient de la **densité de la lecture**, pas des 10 ms ; c'est ce critère-là qui départage les modèles de grille.

## La correction connue : les label priors

Huang et al., ICASSP 2024 (*Less Peaky and More Accurate CTC Forced Alignment by Label Priors*, la recette derrière MMS_FA) : une pénalité ajoutée à la perte CTC pendant l'entraînement interdit de tout laisser au blank. Le réseau se met à étaler ses réponses sur la durée réelle de chaque son — **dense** — tout en restant entraîné sur des séquences, jamais sur des étiquettes canoniques par trame — donc **acoustique** : il dit ce qu'il entend, pas ce que le mot devrait contenir. C'est la seule recette connue qui achète la densité sans payer la divination, et le critère « dire quel son a été produit, jamais deviner celui qu'on voulait faire » est non négociable pour la comparaison. Recette et code libres (BSD, torchaudio) ; les poids MMS_FA, eux, sont CC-BY-NC.

## Ce qui a été envisagé et écarté

- **Un modèle framewise en troisième réseau, pour la seule grille** (charsiu, `wav2textgrid`). Écarté sur objection juste : ça répare la case, pas la mesure. La répartition du modèle *lue dans la case* viendrait toujours de la matrice CTC, vide à cet endroit (pic 0,00) — l'apprenant qui prononce bien le son y opposerait une répartition pleine à une répartition vide : divergence forte, **marqué pour avoir eu raison**. S'ajoutent une table de correspondance entre deux inventaires et +190 à 380 Mo.
- **Charsiu embarqué** : poids sans licence déclarée (code MIT), le reproche tient.
- **MMS_FA embarqué** : licence NC, et alphabet **lettres** (caractères romanisés, ~28 symboles) — même libre, il ne saurait pas dire « ici un /k/ ».
- **`wav2textgrid`** (framewise 10 ms, MIT, `pkadambi/wav2textgrid`) : seul framewise d'étagère à licence propre, rétrogradé en repli avec le montage à trois réseaux.

## Le plan

**A s'affine, B peut-être jamais.** L'app garde ses deux réseaux, chacun son alphabet.

1. **Affiner A** — le seul geste qui le comble, aucun modèle phonémique à label priors n'existant sur étagère. Pas un entraînement de zéro : une oreille pré-entraînée libre (wav2vec2, Apache-2.0), ré-entraînée avec la perte corrigée. Le régime retenu est l'**affinage complet, oreille dégelée, dos `xls-r-300m`** — celui du sortant, ce qui fait de la comparaison l'isolat le plus propre possible : mêmes données, même dos, même alphabet, notre recette en plus. Le régime à oreille gelée, qui aurait permis de partager l'encodeur entre les deux alphabets, est condamné par la mesure : une tête linéaire sur des features gelées ne redistribue pas la masse que l'oreille concentre sur le blank. Le partage était une préférence, la qualité est le critère. L'atelier est dans `../../train/README.md`, l'état du chantier dans `../../TODO.md`.

   **Le corpus : TIMIT, transcrit phonétiquement à la main.** `analysis.md` enregistre l'hypothèse (plausible, non isolée par expérience) que le modèle actuel gagne parce que TIMIT dit ce qui a été *prononcé*, quand un corpus aligné au dictionnaire enseigne à pardonner — la signature de charsiu. Reprendre TIMIT évite la question. Accès payant unique (LDC, ~250 $), le corpus reste à l'atelier et n'est jamais redistribué ; un dictionnaire à l'atelier resterait permis — le principe « pas de dictionnaire » vaut pour l'app qui tourne — mais TIMIT rend même ce recours inutile.

   **Licence des poids produits : à nous, déclarée.** La position de tout l'écosystème (pratique massive, non tranchée en droit) : les poids ne sont pas une copie des données, l'entraîneur les publie sous la licence qu'il choisit — c'est ce qu'a fait vitouphy (TIMIT → Apache-2.0), et le montage actuel repose déjà dessus. Publication sur Hugging Face, purement déclarative, avec la fiche complète que notre propre banc exige des autres : licence, corpus nommé, recette, dos cité. Côté F-Droid, `NonFreeAssets` juge la licence déclarée de l'actif ; précédent de genre : Sayboard, dépôt principal, télécharge des poids Vosk Apache-2.0 dont certains corpus d'entraînement sont des corpus LDC payants.

2. *(optionnel, dé-risquage)* **Bencher MMS_FA comme témoin de la recette, jamais comme pièce de l'app** — le mesurer localement est un usage que sa licence permet. Seul réseau publié entraîné avec les label priors, il permet d'observer sur nos propres rendus ce que la correction produit (densité, frontières). Son alphabet lettres le rend comparable trait pour trait à B. Preuve indirecte pour A (autre alphabet) : un confort à quelques heures, pas un pilier.
3. **Décider du sort de B sur le chiffre de la jointure.** La géométrie mots-d'abord de la brique 4 a été éprouvée contre une carte de sons dense empruntée, et B y tient son rôle : le sort de B penche vers « inchangé ». S'il doit quand même y passer : même recette, en plus facile — pour les lettres, le texte d'un corpus *est* sa séquence cible, pas même besoin de dictionnaire à l'atelier. Perspective lointaine : une seule oreille à deux têtes, un téléchargement au lieu de deux.
