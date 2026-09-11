# Écrire une tuile de la porte Libre

Guide de génération, écrit pour être suivi tel quel par une session d'IA — `./run tile` en lance une dessus — et lisible par un humain qui écrit une tuile à la main.

Une tuile est une **définition d'activité livrée avec l'app** : un fichier de contenu, jamais du code.

## À lire avant d'écrire une ligne

- `design/tiles.md` — les douze scènes rédigées, chacune avec sa situation, son ancre, sa mise en scène anglaise, ses emplacements et sa question. **C'est la matière.** Une scène qui y est déjà écrite ne se réinvente pas : elle se rend en fichier.
- `design/questions.md` — le mécanisme des emplacements que le modèle remplit et des questions : les moments, les trois crans, le drapeau d'affichage.
- `activity.md` — la définition, le `brief` en deux moitiés, les trous, les règles.

## Le schéma ne se recopie jamais ici

**Le code se lit, à chaque fois, avant d'écrire le JSON.**

- `app/src/main/java/app/saylune/activity/Definitions.kt` — `parse()` dit **exactement** quels champs sont lus. On écrit ceux-là et aucun autre.
- `app/src/main/java/app/saylune/activity/Definition.kt` — ce qu'un fichier doit tenir : un seul personnage principal, pas deux trous sur une même clé, un nom court d'au plus dix caractères, l'anglais obligatoire dans chaque table de langue.
- L'arbre des feuilles, pour les chemins de `weights`. Même règle.
- `app/src/main/assets/definitions/free-conversation.json` — la seule tuile déjà écrite, à lire comme exemple de forme.

Un guide qui recopierait le schéma serait une seconde source qui dérive du parseur, et la tuile suivante ne chargerait plus.

## Ce qu'une tuile est

- **Un moteur de parole, pas une situation utile.** Libre n'a aucune règle de fin : la scène doit tenir quarante passages sans que la fiction craque. Une scène transactionnelle — commander, réserver, acheter — meurt au sixième tour et ne s'écrit pas.
- **Un seul trou, et il demande une ancre vraie sur l'apprenant.** *Un savoir-faire à transmettre*, pas *le métier tenu dans cette scène*. Sur une ancre vraie l'apprenant a quarante tours de matière qu'il possède déjà ; sur une fiction qui ne lui appartient pas il s'arrête au huitième. Deux questions avant de commencer, c'est un formulaire ; une seule, c'est une invitation.
- **Le personnage s'écrit contre la pente du modèle, qui est d'être agréable.** Il porte six choses : qui il est concrètement, ce qu'il veut de cette conversation, son défaut qui coûte quelque chose à l'apprenant, ce qu'il ne fera pas, comment il parle, et ce qu'il a déjà décidé sur l'apprenant. Un personnage chaleureux et curieux qui finit par *« and what about you? »* est un échec.
- **La situation situe l'apprenant à la deuxième personne**, se lit à l'écran, dans sa langue. **La mise en scène situe le personnage**, ne s'affiche jamais, s'écrit en anglais. Jamais l'une dans l'autre.
- **Une fiche déclare ce qu'elle est faite pour faire**, dans `triggers`, avec les clés de `activity/Cautions.kt` et aucune autre — une clé inconnue fait échouer le chargement, un avertissement perdu par une faute de frappe étant exactement ce contre quoi la liste existe. Deux familles : ce que le personnage **fait** (conflit, hostilité, mensonge, pression, examen, intimité) et ce dont il est **question** (deuil, violence, corps, discrimination, substances, isolement). La règle d'admission d'une clé : l'auteur doit pouvoir répondre oui ou non sur sa propre scène sans rien supposer de qui la jouera. Vide veut dire *rien de déclaré*, jamais *rien à signaler* — la plupart des scènes reposent sur un trou que l'apprenant remplit lui-même, donc l'auteur répond du cadre et pas de la séance. Rien ne filtre là-dessus : la liste s'affiche à qui l'a demandée, et ne retire jamais une tuile.
- **Le texte français d'une fiche tutoie l'apprenant** : c'est Saylune qui lui parle — *« Ce que Dana te reproche »*, jamais *« Le reproche de Dana »*. L'anglais dit `you`, qui ne marque pas l'adresse.
- **Aucun genre fixé, donc un nom non genré**, et plutôt un nom qui porte un âge qu'un nom à la mode.
- **Aucun levier** : `settings` reste vide. Aucun levier n'échappe à la pression — la durée du tour et les essais évidemment, mais aussi le texte des répliques, leur longueur et leur complexité, l'écho, l'avance, le bruit —, et en Libre les leviers sont à l'apprenant : une fiche qui en poserait un lui prendrait un réglage. Ce que la personne rencontrée a de difficile — un débit, un accent, des phrases longues — s'écrit dans sa mise en scène. Règle éditoriale aujourd'hui ; le moteur de `design/scene-state.md` la fera vérifier au chargement.
- **`"door": "free"`**, toujours. C'est ce qui dit par quelle porte la définition est offerte, donc quelle grille l'affiche ; le dossier des définitions porte les quatre portes mêlées. Le guide ne couvre que celle-ci.
- **Aucun enjeu** : pas de vies, pas de règle de fin, pas d'issue. Donc **aucune question à la fermeture**, la vague de clôture ne s'ouvrant jamais en Libre — les questions se posent tous les cinq passages.

## Combien, et avec qui

**Une session ne porte pas sur une tuile.** Elle commence par lire `app/src/main/assets/definitions/` et `design/tiles.md`, dire lesquelles des scènes rédigées sont déjà des fichiers et lesquelles ne le sont pas, et **demander combien on en fait dans cette passe** — une, quelques-unes, le reste. Ne jamais le supposer.

## Ce qui se demande et ce qui se propose

La règle est celle du projet : on interprète comme le ferait un collègue attentif, et on ne s'arrête que là où deux lectures donneraient un travail différent.

**Se propose, et on continue** — tout ce qui est du métier à l'intérieur d'un cadre déjà décidé : le nom, l'âge, le défaut, ce qu'il refuse, sa façon de parler, la formulation exacte de la situation, la tournure de l'ancre. Ça se montre en quelques lignes avant d'écrire le JSON — *qui / ce qu'il veut / son défaut / ce qu'il ne fera pas / comment il parle / ce qu'il a déjà décidé sur toi* — pour qu'une phrase suffise à le réorienter. Puis on écrit.

**Se demande, et on s'arrête** — tout ce qui déplace le cadre :

- une scène qui n'est pas dans `design/tiles.md` ;
- une définition qui ne serait pas de la porte Libre — ce guide ne l'écrit pas ;
- une scène dont le moteur fait double emploi avec une déjà écrite ;
- une règle éditoriale qu'il faudrait enfreindre — un levier, un genre fixé, deux trous, une question à la fermeture ;
- un champ que le parseur ne lit pas ;
- un `id` ou un nom court qui entre en collision avec un existant.

**Écrire une scène neuve est de la conception, pas de la transcription**, et ça suit la méthode de brainstorming du projet : énoncer d'abord la version la plus bête qui pourrait marcher et pourquoi elle ne suffit pas, puis **une seule question à la fois, avec un avis à chaque fois**, du global au spécifique. La scène entre dans `design/tiles.md` avant d'entrer dans un fichier.

## Le geste, tuile par tuile

1. Choisir la fiche dans `design/tiles.md`.
2. Lire `Definitions.parse` et le catalogue.
3. Proposer le personnage en quelques lignes, attendre le feu vert.
4. Écrire `app/src/main/assets/definitions/<id>.json`. Le nom du fichier et le champ `id` doivent coïncider, le parseur le vérifie.
5. Committer — **une tuile par commit**, message en anglais.

Puis, une fois la passe finie : `./gradlew :app:testDebugUnitTest --tests '*DefinitionTest*'`. Ce test relit **tous** les fichiers livrés depuis le dossier même que l'app empaquette, sans appareil ni réseau, donc une seule exécution couvre le lot.

## Ce que le parseur ne lit pas encore reste dans le doc

Le mécanisme de `design/questions.md` n'est pas implémenté : les moments, les crans, le drapeau d'affichage et les emplacements que le modèle remplit n'ont aucun champ. **Ils restent écrits dans `design/tiles.md` et n'entrent pas dans le JSON.** Une tuile se joue sans eux : elle perd sa rejouabilité, pas son caractère.

La règle générale, et elle se maintient toute seule : **si `Definitions.parse` ne lit pas le champ, il ne s'écrit pas.** Le jour où le parseur grandit, ce guide le voit en le relisant.

## Les fautes à ne pas faire

- Poser un levier.
- Inventer un chemin de poids ou un nom de cran — tout se lit dans le code.
- Deux trous dans une tuile.
- Un nom genré alors que la fiche ne fixe pas le genre.
- Un `short` de plus de dix caractères.
- Une question à la fermeture.
- Un personnage qui corrige l'anglais de l'apprenant : ce n'est pas son travail, c'est celui de l'app.

## À la fin

Dire quelles tuiles ont été écrites, lesquelles restent à faire, ce qui est resté dans `design/tiles.md` faute de champ pour le porter, et donner le résultat du test.
