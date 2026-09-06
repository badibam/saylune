# La police de l'app

Source de vérité : les **cartes de pixels** de `glyphs/`, une par graisse. Le TTF est un produit, pas une source — il se refait par `build.py` et se commite à côté pour que le build Gradle n'appelle aucun outil hors de l'arbre, ce qu'exige la facette `fdroid`.

La boîte fait **11 colonnes sur 14 rangées**, de y=11 à y=-2 : les capitales tiennent les rangées 0 à 9, les accents 10 et 11, les descendantes -1 et -2. La colonne 10 est l'interlettre et reste vide. Une ligne de texte se pose donc à un pas de 15, que le thème déclare — jamais le défaut de la police, qui vaut 14.

Un pixel de dessin vaut 64 unités, le cadratin 1024, l'avance 704.

## Les scripts

- `extract.py` — lit les TTF de Mono10 et écrit les cartes. Ne se relance que pour repartir de zéro : il écrase les cartes.
- `descenders.py` — redessine `g j p q y` avec leur queue sous la ligne de base.
- `accents.py` — compose les 36 lettres accentuées et pose les glyphes de `drawn.py`.
- `build.py` — compile les cartes en TTF, et refuse un glyphe qui déborde de la boîte.
- `check.py` — prouve que tout ce qu'on n'a pas touché rend au pixel près comme Mono10.
- `plank.py` — dessine une planche PNG pour juger en regardant.

Ajouter un caractère : écrire son bloc `@nom U+XXXX` dans les deux cartes, relancer `build.py`.

## Licence

Dérivée de Mono10 (Michael Vieth, Community Pack), sous SIL OFL 1.1 — voir `OFL.txt`. La police dérivée porte un autre nom, `Speakup Tile`, et reste sous la même licence.
