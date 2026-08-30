# Voir un son que l'apprenant ajoute — les portes fermées

La voie retenue est **écrite et en service** : les sons de l'apprenant posés sur les lettres du texte, ce qui ne trouve aucune lettre étant de la matière ajoutée. Ce qu'elle fait, ce qu'elle rend et ce qui a été mesuré dessus vivent dans `../analysis.md`, brique 12 ; l'instrument est `../../bench/placed.py`.

Ce document ne garde que les **portes fermées**, et il ne garde qu'elles. Chacune a coûté une demi-journée, chacune paraît raisonnable quand on la retrouve seule, et rien dans le code en service ne dit pourquoi elle a été refusée — la relire ici coûte deux minutes, la refaire coûte la journée.

## Ce que le cadre de départ interdisait, et qu'il faut avoir en tête

La grille est décodée du **modèle seul** et l'apprenant y est aligné de force. Il y a donc exactement autant de cases que le modèle a de sons, et **pas une de plus**. Tout ce qui suit part de là.

## Porte 1 — découper la suite de l'apprenant contre les *sons* du modèle

Cinq états successifs : distance d'édition à plat, puis partage entre les mots par les symboles seuls, plus une contrainte de temps, plus une vraie distance d'édition dans le mot, plus le prix d'un bloc n'appartenant à aucun mot.

**Le mur, et il n'a pas de porte de sortie** : une prononciation que le modèle contracte et que l'apprenant ne contracte pas a **exactement la même forme, en symboles, qu'un mot inséré**. Aucun réglage de coût ne les sépare, parce qu'il n'y a rien à séparer à ce niveau-là — le signal qui les distingue n'est pas dans les symboles.

C'est cette porte qui a fait changer de cadre : ce qui les distingue est l'**orthographe**, et l'orthographe se lit en posant les sons sur les lettres, pas en découpant contre des sons.

## Porte 2 — mesurer le temps que la matière ajoutée occupe

L'idée : la matière en trop occupe un trou dans le décodage libre, et un trou plus long que le mot signale un mot inséré. Sur cinq prises, ça sépare. **Sur 2 500 prises d'apprenants réels, ça se déclenche 1 704 fois pour 1 690 prises** — environ une par prise, 80 % des prises touchées, et toujours sur les mêmes petits mots outils (to, was, the, is, you, one, will, be, a, of).

La cause est le dénominateur : sur un mot outil que la synthèse réduit, la barre est nulle — `at` fait un seul son de 20 ms. **Trois dénominateurs de rechange ont été mesurés faux :**

1. **Ce que le modèle passe sur le même intervalle** — se déclenche sur 16 séries sur 18. Circulaire : l'intervalle est court *parce qu'*il y a des sons en plus dedans.
2. **De vraies durées, en alignant l'apprenant sur sa propre suite de sons** — tautologie, le chemin de l'argmax *est* l'optimum quand la cible est ce que l'argmax a produit. Mesuré, 12 sons sur 12 rendent le pic de départ.
3. **Les durées de l'alignement forcé** — rend 0 sur les deux prises au « hmm ». L'alignement forcé n'a pas de trous : il répartit toutes les trames bord à bord, et la matière ajoutée est avalée *dans* les fenêtres voisines, jamais *entre* elles.

**Ce que ces trois échecs disent ensemble, et qui ferme la porte pour de bon** : le décodage libre donne les étiquettes et la position des pics, sans aucune durée ; l'alignement forcé donne les durées et n'a aucune case pour un son ajouté. Aucun des deux ne dit combien de temps la matière ajoutée a duré, et **les combiner ne l'invente pas**. Il n'y aura pas de quatrième dénominateur.

## Porte 3 — le gonflement du voisin comme signal

L'alignement forcé doit couvrir toutes les trames, donc la matière sans case est avalée par les sons de part et d'autre, qui gonflent. C'était le signal des « longues » insertions dans la première version écrite.

Il marchait, à un seuil posé à la main (trois fois la durée médiane de la prise) que rien n'étalonnait — « une ligne tracée entre des observations », disait son propre commentaire. Il demandait en plus un filtre pause, un silence gonflant un voisin exactement comme un son. La découpe par les lettres rend le même service sans seuil, sans horloge et sans filtre : elle est partie avec.

## Ce qui reste ouvert

**Apparenter les sons qu'une même lettre ne relie pas mais qu'un locuteur d'une langue donnée confond couramment** — `f` pour `v` chez un francophone. Ça rattraperait le cas que la voie retenue rate : une substitution dont aucune lettre du mot ne sait écrire le son sort du mot et se lit comme de la matière ajoutée. Non bloquant et non prioritaire, pour la raison dite en brique 12 — le mot porte une marque de toute façon.

**Une prise portant une lettre muette prononcée** — le `b` de `comb`, le `k` de `know`, le `t` de `listen`. **Aucun jeu ne contient un seul mot de cette famille**, donc rien n'a jamais dit ce qu'un de ces canaux ferait dessus, ni l'ancien ni le nouveau. C'est une session d'enregistrement (`../../bench/take.py`), pas un calcul.

## La borne qui vaut pour tous les chiffres de ce document

Cinq prises d'une phrase par un locuteur, dix-sept du jeu étiqueté, 2 500 du corpus L2 : **le matériau situe une ampleur, il ne juge aucun cas.** Aucun nombre ici n'est réglé et aucun ne doit l'être sur ce matériau. Ce qui tranche une marque donnée est l'oreille de qui a parlé.
