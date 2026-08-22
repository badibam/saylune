# Qualifier un moteur d'analyse

Comment un candidat — service distant ou brique locale — gagne le rôle de moteur d'analyse. Ce document assemble en procédure exécutable trois choses qui existent déjà : le contrat (`../reference.md`, « Ce que l'app exige de n'importe quel moteur d'analyse »), le jeu d'essai (`pronunciation-test-set.md`) et la méthode de mesure par écart au modèle (`speechace.md`). Il est écrit pour être rejouable par un tiers : c'est la porte d'entrée d'une contribution extérieure, notamment d'un moteur libre.

## Ce qui se rejoue, ce qui ne se publie pas

**Les enregistrements originaux ne sont pas publiés** — c'est la voix de l'auteur, et publier une voix est un choix qui ne se défait pas. Ils n'ont pas besoin de l'être : les étiquettes du jeu d'essai viennent du **protocole**, pas des fichiers. Une faute franche se vérifie en refaisant la prise avec le geste articulatoire correct (bloc D), un témoin d'accent est un calque d'un modèle entendu (bloc F) — n'importe quelle voix peut donc reconstituer un jeu étiqueté en suivant les recettes de `pronunciation-test-set.md`, contrainte de capture comprise.

Conséquence assumée : les tables chiffrées de `speechace.md` et `azure-speech.md` ne sont pas reproductibles au chiffre près par une autre voix. Ce n'est pas un défaut, parce qu'aucun critère ne porte sur un chiffre absolu — tous portent sur des **séparations internes** au jeu (témoins contre fautes), et celles-là se transportent d'une voix à l'autre.

## L'instrument se réétalonne à chaque fois

Le modèle à imiter est une voix de synthèse, et elle varie — par fournisseur, par voix, par dialecte. L'étalonnage de la voix n'est donc pas un préalable fait une fois : c'est **l'étape zéro de toute qualification**, rejouée avec la voix qu'on a. Tout le matériel synthétique se **régénère par script** plutôt que de vivre en fichiers : rendus du modèle pour chaque phrase du jeu, manipulation d'accent (accent forcé sur la mauvaise syllabe), manipulation de mélodie (dernier mot monté), rendus croisés US/GB pour l'accord de dialecte.

## Le banc

Les sondes de `tmp/bench/` (`speechace.py`, `tts.py`, `audio_probe.py`) sont l'embryon du banc ; elles se relogent en `bench/`, versionnées, derrière une commande unique. Le banc prend un adaptateur de moteur (un appel : audio + texte + dialecte → phonèmes localisés et notés, syllabes, hauteur) et déroule la procédure ci-dessous. Le contrôle indépendant de f0 par autocorrélation (`audio_probe.py`) en fait partie : il juge le tracker du moteur, pas l'inverse.

## La procédure

1. **Étalonner la voix modèle.** Synthétiser les phrases du jeu, les passer au moteur avec leur propre texte. La voix qualifie si la médiane est haute et qu'aucun phonème ne décroche (chez SpeechAce : médiane ≥ 97, aucun son < 90 — à transposer à l'échelle du candidat). Une voix qui échoue est écartée, quelle que soit sa beauté.
2. **Vérifier le déterminisme.** Le même fichier envoyé deux fois rend les mêmes notes et les mêmes bornes. Un moteur non déterministe est disqualifié d'office : la mesure par écart lui est impossible.
3. **Vérifier l'accord de dialecte.** Une voix US notée au référentiel GB doit échouer l'étape 1 — c'est le comportement attendu, qui prouve que l'étalonnage détecte l'incohérence au lieu de la laisser inverser la mesure.
4. **Enregistrer le jeu humain** en suivant les blocs A à F de `pronunciation-test-set.md`, dans l'ordre par paires, et **vérifier les fautes franches** à la méthode du bloc D : une faute non détectée n'accuse le moteur qu'une fois établi qu'elle a été produite.
5. **Noter tout le jeu par écart au modèle**, en appliquant les deux filtres d'artefacts silencieux : segment ≤ 10 ms = décrochage d'alignement, hauteur hors d'un facteur deux de la médiane du tour = accrochage harmonique.
6. **Lire les séparations.**

## Les critères

Un candidat qualifie s'il tient les propriétés suivantes — formulées sans seuil absolu, parce que l'échelle de notes est propre à chaque moteur :

1. **Phonème** : les fautes franches (blocs A et D vérifiées) se séparent des témoins (bloc C) par une **bande vide** — les écarts des témoins se groupent près de zéro, ceux des fautes nettement en dessous, rien entre les deux. Zéro fausse alerte sur les témoins.
2. **Accent** : au protocole du calque (bloc F), toute faute spontanée vue, aucune alerte sur les calques.
3. **Mélodie** : la question et le plat (bloc E) se séparent du modèle avec le bon signe, en demi-tons.
4. **Localisation** : chaque son a une position et une durée, et les alignements dégénérés sont **repérables** (durées absurdes visibles, pas lissées).
5. **Ancrage** : chaque son et chaque syllabe désignent leurs lettres dans le texte affiché, y compris les deux irrégularités (une lettre, deux sons ; une lettre, aucun son).
6. **Fichier arbitraire** : le moteur note un audio quelconque, pas seulement une prise de micro — c'est ce qui permet de lui soumettre le modèle.
7. **Capacités déclarées** : ce que le moteur ne sait pas faire, il le refuse explicitement ; le banc imprime la carte des capacités.

Un candidat peut qualifier **partiellement** — une brique locale qui ne tient que la mélodie est recevable telle quelle : les trois échelles sont des briques indépendantes, et l'app allume ce qui est déclaré.
