# Qualifier l'analyse

Comment on vérifie que l'analyse fait ce qu'elle prétend, et qu'une voix mérite de servir de modèle. Ce document assemble en procédure exécutable trois choses qui existent déjà : ce que l'app exige de son analyse (`../reference.md`), le jeu d'essai (`pronunciation-test-set.md`) et la méthode de mesure par écart au modèle (`embedded-analysis.md`). Il est écrit pour être rejouable par un tiers : c'est la porte d'entrée d'une contribution extérieure.

## Ce qui se rejoue, ce qui ne se publie pas

**Les enregistrements originaux ne sont pas publiés** — c'est la voix de l'auteur, et publier une voix est un choix qui ne se défait pas. Ils n'ont pas besoin de l'être : les étiquettes du jeu d'essai viennent du **protocole**, pas des fichiers. Une faute franche se vérifie en refaisant la prise avec le geste articulatoire correct (bloc D), un témoin d'accent est un calque d'un modèle entendu (bloc F) — n'importe quelle voix peut donc reconstituer un jeu étiqueté en suivant les recettes de `pronunciation-test-set.md`, contrainte de capture comprise.

Conséquence assumée : les tables chiffrées ne sont pas reproductibles au chiffre près par une autre voix. Ce n'est pas un défaut, parce qu'aucun critère ne porte sur un chiffre absolu — tous portent sur des **séparations internes** au jeu (témoins contre fautes), et celles-là se transportent d'une voix à l'autre.

## L'instrument se réétalonne à chaque fois

Le modèle à imiter est une voix de synthèse, et elle varie — par fournisseur, par voix, par accent. L'étalonnage de la voix n'est donc pas un préalable fait une fois : c'est **l'étape zéro de toute qualification**, rejouée avec la voix qu'on a. Tout le matériel synthétique se **régénère par script** plutôt que de vivre en fichiers : rendus du modèle pour chaque phrase du jeu, manipulation d'accent (accent forcé sur la mauvaise syllabe), manipulation de mélodie (dernier mot monté).

## Le banc

Le banc vit dans `bench/`, versionné. `synth.py` rend une phrase par n'importe quelle voix candidate, `matrix.py` calcule la matrice et ses deux lectures, `overlap.py` compare deux répartitions son par son, `faults.py` déroule le jeu étiqueté, `take.py` guide une session d'enregistrement. `export.py`, `concord.py` et `phone.py` portent la même mesure sur l'appareil.

Rendus et lectures sont mis en cache sous `bench/out/`, gitignoré : l'audio se paie en caractères, les lectures en calcul, et les deux se régénèrent par script. Le contrôle indépendant de f0 par autocorrélation (`audio_probe.py`) fait partie du banc : il juge le tracker de la brique 10, pas l'inverse.

## La procédure

1. **Étalonner la voix modèle.** Synthétiser les phrases du jeu et lire leur matrice. Une voix qualifie si sa grille est nette : aucun son écrasé, aucune zone où la répartition s'effondre, une suite de sons qui correspond à ce qu'on entend. **La forme exacte de ce test est à écrire** — celle qui existait notait une voix à l'échelle d'un service, et le montage n'en rend plus. Ce que l'étape empêche, en revanche, ne change pas : un modèle bâclé devient un mauvais étalon sans que rien ne le signale, puisqu'il est cru aveuglément.
2. **Vérifier le déterminisme.** Le même fichier lu deux fois rend les mêmes octets. Une analyse non déterministe est disqualifiée d'office : la mesure par écart lui est impossible, puisqu'elle mesurerait le bruit de la machine autant que l'écart réel.
3. **Vérifier que le locuteur ne survit pas.** Deux voix de synthèse différentes disant la même phrase doivent se recouvrir de près : si la machine gardait la trace de qui parle dans la forme de ses répartitions, deux prises impeccables s'écarteraient et l'app marquerait le vide.
4. **Enregistrer le jeu humain** en suivant les blocs A à F de `pronunciation-test-set.md`, dans l'ordre par paires, et **vérifier les fautes franches** à la méthode du bloc D : une faute non détectée n'accuse l'analyse qu'une fois établi qu'elle a été produite.
5. **Lire tout le jeu par écart au modèle**, en écartant les segments sans matière : un son dont les trames sont majoritairement du silence n'a rien à comparer, et une prise dont l'écart médian s'envole n'est pas une prise fautive mais une grille forcée sur une parole qui ne la contient pas (brique 11).
6. **Lire les séparations.**
7. **Refaire la lecture sur l'appareil.** Le poste ne suffit pas : les noyaux 8 bits ne sont pas le même code d'une architecture à l'autre. `phone.py` range la lecture de l'appareil dans le cache comme n'importe quelle autre, et `READING=<nom> python3 faults.py` rejoue le jeu dessus. C'est le verdict qui compte, pas l'identité des octets.

## Les critères

L'analyse qualifie si elle tient les propriétés suivantes — formulées sans seuil absolu, parce que l'échelle est propre à chaque modèle acoustique :

1. **Phonème** : les fautes franches (blocs A et D vérifiées) se séparent des témoins (bloc C) par une **bande vide** — les écarts des témoins se groupent près de zéro, ceux des fautes nettement au-dessus, rien entre les deux. Zéro fausse alerte sur les témoins.
2. **Accent** : au protocole du calque (bloc F), toute faute spontanée vue, aucune alerte sur les calques.
3. **Mélodie** : la question et le plat (bloc E) se séparent du modèle avec le bon signe, en demi-tons.
4. **Localisation** : chaque son a une position et une durée, et les alignements dégénérés sont **repérables** (durées absurdes visibles, pas lissées).
5. **Ancrage** : chaque son et chaque syllabe désignent leurs lettres dans le texte affiché, y compris les deux irrégularités (une lettre, deux sons ; une lettre, aucun son).
6. **Portabilité** : les critères 1 à 3 tiennent sur l'appareil, pas seulement au poste.

Une brique peut qualifier **séparément** : la mélodie est autonome — DSP pur, elle ne lit même pas la matrice — et se juge sans rien attendre des autres. Les trois échelles sont des briques indépendantes, et l'app allume ce qu'elle a.
