# Jeu d'essai grammatical

Le contenu des deux bancs du chantier 2 (cf. `../../TODO.md`) : le **banc du juge** (LLM, texte seul) et le **banc de l'oreille** (STT, audio). Même esprit que `../qualification.md` : chaque énoncé vise un endroit où le jugement peut basculer, une phrase qui se juge sans ambiguïté ne mesure rien, et le contexte est collé parce que la moitié des cas ne se tranchent que par lui.

**Étiqueter, c'est décider la policy.** Ce jeu ne mesure pas seulement les candidats : il fixe ce que l'app considère comme une faute. La norme retenue est **le parlé natif informel, pas l'écrit scolaire** — l'app fait pratiquer l'oral, et marquer « gonna » ou une question sans inversion serait corriger l'anglais que les natifs parlent. C'est la décision la plus lourde du jeu, et elle est prise ici, pas laissée au LLM.

## Banc du juge — ce qu'on demande et ce qu'on mesure

Pour chaque énoncé, le candidat reçoit le contexte et la transcription, et rend son verdict (fautif / maladroit / correct), `intended` et `repaired`. Trois mesures :

- **Fausses alertes sur le bloc G2** — le taux qui compte le plus : une marque sur de l'informel correct mine la confiance exactement comme une fausse alerte de prononciation, et envoie redire ce qui n'avait rien à corriger.
- **Tenue de la frontière des crans** — au cran 2 (marquer les fautes), G1 est marqué et G3 ne l'est pas ; au cran 3, G3 s'ajoute. Un juge qui ne sépare pas G1 de G3 rend le troisième cran inutilisable.
- **Qualité d'`intended`** sur le bloc G4 — la reconstruction, reprise des blocs A et B du jeu de prononciation en version texte.

## Bloc G1 — fautifs, marqués dès le cran 2

Fautes typiques d'un francophone, la faute portée par un seul endroit de la phrase.

| # | énoncé | contexte (tour IA précédent) | faute | `repaired` |
|---|---|---|---|---|
| 1 | `He don't know the answer.` | *Did you ask your brother about it?* | accord | `He doesn't know the answer.` |
| 2 | `Yesterday I go to the market.` | *What did you do this weekend?* | temps | `Yesterday I went to the market.` |
| 3 | `I have 25 years.` | *Tell me a bit about yourself.* | calque, trois mots changent | `I am 25 years old.` |
| 4 | `She explained me the rule.` | *How did you finally understand it?* | construction verbale | `She explained the rule to me.` |
| 5 | `I am agree with you.` | *I think the book was better than the movie.* | calque « être d'accord » | `I agree with you.` |
| 6 | `I didn't went there in the end.` | *So, how was the concert?* | double marquage du passé | `I didn't go there in the end.` |
| 7 | `He must to finish it tonight.` | *When is your colleague's deadline?* | modal + infinitif | `He must finish it tonight.` |
| 8 | `There is many options on the menu.` | *Is that restaurant any good?* | accord there is/are | `There are many options on the menu.` |

## Bloc G2 — corrects informels, jamais marqués à aucun cran

Le bloc des fausses alertes. Tout y est de l'anglais natif parlé ordinaire ; un juge scolaire en marquerait la moitié.

| # | énoncé | contexte | piège tendu au juge |
|---|---|---|---|
| 9 | `Gonna grab a coffee first, want one?` | *Shall we start the exercise?* | contraction orale + ellipses du sujet |
| 10 | `You went there yesterday?` | *I visited the new museum.* | question sans inversion |
| 11 | `Me neither.` | *I've never been to Spain.* | fragment, « me » sujet apparent |
| 12 | `That's the guy I told you about.` | *Who's that in the photo?* | préposition finale, relative sans pronom |
| 13 | `If I was you, I'd take the job.` | *I can't decide whether to accept.* | « was » pour « were » — informel standard, décidé correct ici |
| 14 | `Who did you give it to?` | *I don't have the key anymore.* | « who » pour « whom » + préposition finale |
| 15 | `It's just me and my brother at home.` | *Do you live with your family?* | « me and X » en position d'attribut |
| 16 | `Sounds good, see you then.` | *Let's meet at six, okay?* | double ellipse du sujet |

## Bloc G3 — corrects mais maladroits, marqués seulement au cran 3

Grammatical, compréhensible, et aucun natif ne le dirait — le calque qui survit à la grammaire. C'est la matière du troisième cran, et la frontière avec G1 est ce que le cran 2 doit tenir.

| # | énoncé | contexte | tournure attendue |
|---|---|---|---|
| 17 | `How do you call this in English?` | *(l'utilisateur montre un objet du quotidien)* | `What do you call this...` |
| 18 | `I have the possibility to work from home.` | *Does your company allow remote work?* | `I can work from home.` |
| 19 | `I will profit from my holidays to visit my parents.` | *Any plans for the summer?* | `I'll take advantage of...` / `I'll use my holidays to...` |
| 20 | `We were three at the meeting.` | *Was the whole team there?* | `There were three of us...` |
| 21 | `The formation I followed was useful.` | *Where did you learn to code?* | `The training I took...` |

## Bloc G4 — la reconstruction d'`intended`

Reprise en version texte des cas de prononciation : la transcription porte un mot phonétiquement déformé, et le juge doit rendre le texte que la bouche visait — sans faute grammaticale à marquer.

| # | transcription | contexte | attendu |
|---|---|---|---|
| 22 | `I sink you are right.` | *(débat amical sur un film)* | `intended = "I think you are right"`, grammaire propre — la marque viendra du moteur phonémique |
| 23 | `The sink is broken.` | *(conversation sur des réparations dans la cuisine)* | `intended` = la transcription telle quelle. Échec si le juge « corrige » un mot juste |
| 24 | `Turn light at the corner.` | *(demande d'itinéraire, aucun indice gauche/droite)* | **cas d'observation, pas de réussite/échec** : les deux reconstructions sont plausibles, et ce qu'on regarde est si le candidat signale l'incertitude ou tranche avec aplomb — la rafale en aval reste le garde-fou |
| 25 | `I am walkin to the office.` | *How do you get to work?* | `intended = "I am walking to the office"`, grammaire propre |

## Banc de l'oreille — recette des prises

Critère central : **fidélité verbatim sur la faute** — un STT qui rend `He doesn't know` pour « He don't know » est disqualifié, et **un mot dit ne se répare jamais**. Formatage intelligent coupé quand le fournisseur le permet.

**Les disfluences comptent aussi, et ça a changé.** Ce banc les tolérait coupées, au motif qu'elles ne sont pas un signal d'apprentissage ; une feuille de fluidité les lit désormais, qui range chaque mot prononcé en `retenu`, `abandonné` ou `remplissage` (`../measures.md`). Un moteur qui les nettoie la rend muette sans le dire, et la fluidité paraîtra excellente. Elles se notent donc comme le reste : coupées, c'est un défaut du candidat, pas une commodité.

Les prises existantes se réutilisent — même voix, mêmes fichiers :

- **Bloc B du jeu de prononciation** (prises 2, 3, 11, 12) : quatre fautes grammaticales acoustiquement nettes, le cœur du critère verbatim.
- **Prises 21 et 22** (question montante / plat) : la ponctuation rendue — une question dite doit sortir avec « ? », c'est le contour du modèle TTS qui en dépend.

Deux prises neuves à enregistrer (contrainte de capture de `../qualification.md` : `trim 1 6`, deux secondes avant de parler) :

| # | à dire | ce qu'on observe |
|---|---|---|
| 26 | `I want to— uh, I mean, we want to go there together.` | le faux départ et le « uh » : rendus, coupés, ou réparés en phrase lisse. Les rendre est ce qu'on veut ; coupés est un défaut ; fusionnés en un seul énoncé propre est le pire des trois, la reprise n'étant alors même plus lisible |
| 27 | `I'm gonna call him tomorrow.` | observation sans verdict : `gonna` rendu tel quel ou étendu en `going to` — les deux se défendent, on note de quel côté chaque candidat tombe |

## Ce que ce jeu ne mesure pas

- La **qualité de la reprise conversationnelle** (la reformulation glissée dans la réponse) — c'est un jugement de naturel, pas d'exactitude, et il se fera à l'oreille sur les candidats finalistes, pas au banc.
- Le taux de fausse alerte du juge sur du **vrai spontané long** — les énoncés sont courts et ciblés ; un tour réel mêle plusieurs candidats-fautes, et c'est l'usage qui dira si le juge sur-marque en conditions réelles.
