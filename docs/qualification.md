# Qualifier l'analyse

Comment on vérifie que l'analyse fait ce qu'elle prétend, et qu'une voix mérite de servir de modèle. Ce document porte les deux moitiés d'une même chose : la **procédure**, et le **jeu d'essai étiqueté** qu'elle déroule. Il s'appuie sur ce que l'app exige de son analyse (`reference.md`) et sur la méthode de mesure par écart au modèle (`analysis.md`). Il est écrit pour être rejouable par un tiers : c'est la porte d'entrée d'une contribution extérieure.

## Ce qui se rejoue, ce qui ne se publie pas

**Les enregistrements originaux ne sont pas publiés** — c'est la voix de l'auteur, et publier une voix est un choix qui ne se défait pas. Ils n'ont pas besoin de l'être : les étiquettes du jeu d'essai viennent du **protocole**, pas des fichiers. Une faute franche se vérifie en refaisant la prise avec le geste articulatoire correct (bloc D), un témoin d'accent est un calque d'un modèle entendu (bloc F) — n'importe quelle voix peut donc reconstituer un jeu étiqueté en suivant les recettes du jeu d'essai, contrainte de capture comprise.

Conséquence assumée : les tables chiffrées ne sont pas reproductibles au chiffre près par une autre voix. Ce n'est pas un défaut, parce qu'aucun critère ne porte sur un chiffre absolu — tous portent sur des **séparations internes** au jeu (témoins contre fautes), et celles-là se transportent d'une voix à l'autre.

## L'instrument se réétalonne à chaque fois

Le modèle à imiter est une voix de synthèse, et elle varie — par fournisseur, par voix, par accent. L'étalonnage de la voix n'est donc pas un préalable fait une fois : c'est **l'étape zéro de toute qualification**, rejouée avec la voix qu'on a. Tout le matériel synthétique se **régénère par script** plutôt que de vivre en fichiers : rendus du modèle pour chaque phrase du jeu, manipulation d'accent (accent forcé sur la mauvaise syllabe), manipulation de mélodie (dernier mot monté).

## Le banc

Le banc vit dans `bench/`, versionné. `synth.py` rend une phrase par n'importe quelle voix candidate, `matrix.py` calcule la matrice et ses deux lectures, `overlap.py` compare deux répartitions son par son, `faults.py` déroule le jeu étiqueté, `take.py` guide une session d'enregistrement. `export.py`, `concord.py` et `phone.py` portent la même mesure sur l'appareil.

Rendus et lectures sont mis en cache sous `bench/out/`, gitignoré : l'audio se paie en caractères, les lectures en calcul, et les deux se régénèrent par script.

**Une matrice en cache porte l'empreinte de l'audio dont elle vient**, parce que la synthèse n'est pas reproductible : le même texte par la même voix ne rend pas deux fois le même fichier, et un cache rangé sous le nom d'une phrase survit à l'audio qu'il décrit. La nôtre se recalcule, une lecture empruntée est refusée — la lire reviendrait à comparer deux enregistrements qui n'en sont pas un seul. **Et chaque brique termine ses chiffres par la ligne des audios qu'elle a lus** : combien, et leur empreinte jointe — le hachage des noms et des octets réunis. C'est ce qui rend deux tableaux séparés dans le temps comparables : même empreinte, mêmes sons ; empreinte différente, les chiffres ne se confrontent pas. Une brique qui ne lit que des matrices en cache (`concord.py`) nomme les empreintes que ces matrices portent, et compte à part celles calculées avant qu'on les y range. Les verdicts écrits à la main (`review.py`) et le tour rendu à l'écran (`turn.py`) portent la même empreinte dans leur fichier, puisqu'ils survivent à l'audio. Le contrôle indépendant de f0 par autocorrélation (`audio_probe.py`) fait partie du banc : il juge le tracker de la brique 10, pas l'inverse.

## La procédure

1. **Étalonner la voix modèle.** Synthétiser les phrases du jeu et lire leur matrice. Une voix qualifie si sa grille est nette : aucun son écrasé, aucune zone où la répartition s'effondre, une suite de sons qui correspond à ce qu'on entend. **La forme exacte de ce test est à écrire** — celle qui existait notait une voix à l'échelle d'un service, et le montage n'en rend plus. Ce que l'étape empêche, en revanche, ne change pas : un modèle bâclé devient un mauvais étalon sans que rien ne le signale, puisqu'il est cru aveuglément. Un critère mesuré s'y ajoute (2026-09-05, `bench/hear.py --couples`) : **la lisibilité de l'accent**, c'est-à-dire sur combien de mots polysyllabiques la sonde d'accent élit une autre syllabe que sur une lecture native sans faute — 4 % pour `eleven-gb-daniel`, 8 % pour `azure-us-jenny`. Ces marques-là tombent sur un apprenant qui n'a rien fait, et le facteur deux entre deux voix est gratuit à récupérer.
2. **Vérifier le déterminisme.** Le même fichier lu deux fois rend les mêmes octets. Une analyse non déterministe est disqualifiée d'office : la mesure par écart lui est impossible, puisqu'elle mesurerait le bruit de la machine autant que l'écart réel.
3. **Vérifier que le locuteur ne survit pas.** Deux voix de synthèse différentes disant la même phrase doivent se recouvrir de près : si la machine gardait la trace de qui parle dans la forme de ses répartitions, deux prises impeccables s'écarteraient et l'app marquerait le vide.
4. **Enregistrer le jeu humain** en suivant les blocs A à F du jeu d'essai, dans l'ordre par paires, et **vérifier les fautes franches** à la méthode du bloc D : une faute non détectée n'accuse l'analyse qu'une fois établi qu'elle a été produite.
5. **Lire tout le jeu par écart au modèle**, en écartant les segments sans matière : un son dont les trames sont majoritairement du silence n'a rien à comparer, et une prise dont l'écart médian s'envole n'est pas une prise fautive mais une grille forcée sur une parole qui ne la contient pas (brique 11).
6. **Lire les séparations.**
7. **Lire la jointure** (`join.py -s`) : la part des sons qui portent les lettres qu'un humain leur attribue, contre l'annotation d'`expected.py`. Une séparation ne qualifie pas seule — là où un son ne reçoit pas les bonnes lettres, la marque tombe à côté, quelle que soit la netteté de l'écart. Les deux instruments peuvent diverger, et la divergence est réelle : un modèle acoustique plus pointu sépare mieux deux répartitions au pic tout en laissant moins de matière à joindre.
8. **Refaire la lecture sur l'appareil.** Le poste ne suffit pas : les noyaux 8 bits ne sont pas le même code d'une architecture à l'autre. `phone.py` range la lecture de l'appareil dans le cache comme n'importe quelle autre, et `READING=<nom> python3 faults.py` rejoue le jeu dessus. C'est le verdict qui compte, pas l'identité des octets.
9. **Écouter ce qui est marqué** (`review.py`) : le jeu d'essai répond pour **un** son par prise, celui autour duquel il a été bâti, et l'écran les peint tous. Les autres marques d'une prise ne sont ni des fautes ni des fausses alertes tant que personne ne les a entendues.

## Les critères

L'analyse qualifie si elle tient les propriétés suivantes — formulées sans seuil absolu, parce que l'échelle est propre à chaque modèle acoustique :

1. **Phonème** : les fautes franches (blocs A et D vérifiées) se séparent des témoins (bloc C) par une **bande vide** — les écarts des témoins se groupent près de zéro, ceux des fautes nettement au-dessus, rien entre les deux. Zéro fausse alerte sur les témoins.
2. **Accent** : au protocole du calque (bloc F), toute faute spontanée vue, aucune alerte sur les calques.
3. **Mélodie** : la question et le plat (bloc E) se séparent du modèle avec le bon signe, en demi-tons.
4. **Localisation** : chaque son a une position et une durée, et les alignements dégénérés sont **repérables** (durées absurdes visibles, pas lissées).
5. **Ancrage** : chaque son et chaque syllabe désignent leurs lettres dans le texte affiché, y compris les deux irrégularités (une lettre, deux sons ; une lettre, aucun son). **Se lit à `join.py -s`**, contre l'annotation à la main d'`expected.py` : la part des sons qui portent les lettres qu'un humain leur a attribuées. L'annotation indexe les sons **réellement décodés** sur ces rendus : elle note le modèle pour lequel elle a été écrite et aucun autre, et une grille d'une autre forme sort de la mesure au lieu d'y entrer de travers.
6. **Portabilité** : les critères 1 à 3 tiennent sur l'appareil, pas seulement au poste.

Une brique peut qualifier **séparément** : la mélodie est autonome — DSP pur, elle ne lit même pas la matrice — et se juge sans rien attendre des autres. Les trois échelles sont des briques indépendantes, et l'app allume ce qu'elle a.

## Ce que chaque instrument répond, et ce qu'il ne répond pas

Aucun de ces instruments ne qualifie seul, et chacun a un angle mort nommable.

- `faults.py` — l'écart tombe-t-il sur le son fautif et reste-t-il à zéro sur le témoin. C'est la **fidélité éprouvée dans les deux sens** : un modèle qui devine le son voulu plutôt que le son produit rend une belle suite de sons et ne voit aucune faute, donc les témoins comptent autant que les fautes. Il compare des répartitions et dit explicitement que l'étiquette peut être fausse sans dommage. Il ne lit qu'**un son par prise** : une prise dite témoin est un témoin sur ce son-là et sur rien d'autre, donc le taux de fausse alerte **hors** des sons étiquetés ne s'y lit pas — ce qui pèse dès qu'on compare deux modèles dont l'un marque plus que l'autre.
- `join.py -s` — les sons portent-ils les lettres qu'un humain leur attribue. Le seul instrument que rien n'automatise : sa règle est `expected.py`, écrite à la main et indexée sur les sons **réellement décodés**, donc elle note le modèle pour lequel elle a été écrite et aucun autre. Le compteur de trous qui l'avait précédée rapportait huit défauts là où elle en trouve quinze ; c'est pour ça qu'il n'existe plus.
- `boundaries.py` — où le réseau place chaque son dans le temps. La seule qualification adossée à une **vérité terrain** plutôt qu'à notre jeu étiqueté : TIMIT annote chaque phone au niveau de l'échantillon. Elle rend trois choses de portée très différente : la **position** d'un son, bonne à 25 ms ; la **durée d'une syllabe**, prise entre deux débuts de voyelle, juste à 29,1 ms soit 14,7 % d'elle-même, qui est ce que la brique 7 lira ; et la **durée couverte** d'un son, à 26,9 %, qui **ne juge plus rien** — aucune brique ne lit l'étendue d'un son. La seconde ne se déduit pas de la première et se mesure donc à part : les erreurs de départ ne s'annulent pas entre elles. Elle a longtemps servi à instruire le procès de la densité ; ce procès n'a plus de partie civile.
- `recognition.py` — le décodage libre nomme-t-il les bons sons. La seule question qu'aucune autre brique ne pose, et elle porte : la brique 3 de l'analyse **est** un décodage libre, la grille à laquelle tout s'ancre. Réserve : il lit la même matrice que les autres, c'est une quatrième question et pas un témoin extérieur.
- `syllables.py` — la grille sait-elle combien de syllabes a un mot. Deux comptages : les sons que la grille ne porte pas, voyelles à part des consonnes ; et le nombre de noyaux par mot, contre l'annotation. C'est ce qui paie le choix de la brique 8 — syllaber sur les sons et non sur les lettres. Angle mort : la faute inverse, une syllabe que l'apprenant **ajoute**, ne s'y voit pas, et rien ne la voit encore.
- `turn.py` — le tour entier, ce qu'aucune qualification ne montrait. Un tour porte 2 à 6 marques, témoins compris.
- `divergence.py` — lequel de deux modèles entend ce qui a été dit, là où ils ne nomment pas le même son. C'est l'instrument de la **fidélité**, et il ne juge que des désaccords : les modèles s'accordent la plupart du temps, et une suite de sons ne se note pas contre une transcription canonique sans récompenser la divination. Sur les rendus **et** sur les prises. Deux angles morts, et le second est plus lourd que le premier. Un son que les deux modèles ratent ensemble ne produit aucun désaccord — `syllables.py` et `missing_mass.py` le comptent. Et **un désaccord de voyelles voisines n'est pas jugeable** : deux modèles qui posent leur pic à deux endroits d'un même continuum ne donnent pas deux réponses à une question fermée. Neuf des dix désaccords des rendus sont de cette forme, et le jugement à l'aveugle les rend 5-5. **Il se tient à l'aveugle**, et c'est une condition, pas un raffinement : lequel des deux modèles est A se tire au sort par cas, les noms ne sont jamais affichés, le tirage part dans le verdict. La passe à découvert qui l'avait précédé donnait 6-4, et trois de ses dix verdicts ont basculé une fois masqués — même oreille, même audio. Deux chiffres bornent ce que l'instrument vaut : cette instabilité d'un tiers, et un penchant pour la seconde ligne relevé 8 fois sur 10, quel que soit le modèle qui s'y trouvait, que seul le tirage annule dans le décompte. Le mot arrive à mi-vitesse, hauteur conservée, et se rejoue à 0,5, 0,33 ou vitesse pleine. L'étiquette est de toute façon ce dont la conception dit qu'elle ne dépend pas — elle n'est consommée qu'à la jointure, qui se mesure sans oreille.
- `review.py` — la marque est-elle une faute, jugée à l'oreille par l'auteur de la prise. Il joue le **mot entier**, modèle puis prise : soixante millisecondes de phonème ne se jugent pas isolées. Si rien ne joue, la session s'arrête — juger une marque sans l'entendre serait une supposition écrite comme une réponse. Les verdicts vivent dans `bench/reviews/`, versionnés : même matière qu'`expected.py`, écrits une fois à la main et régénérables par rien.

## Le jeu d'essai

Vingt-sept prises d'une même voix : les blocs A à D en une session, le cas 5 et les blocs E et F dans une seconde. Chaque faute délibérée a son témoin, pris le même jour qu'elle. Les mêmes phrases, les mêmes enregistrements, un seul locuteur — refaire les prises entre deux mesures détruirait la comparabilité, donc les fichiers sont conservés (cf. « Où vivent les prises »).

Chaque phrase des blocs A et B vise l'endroit où faute phonétique et faute grammaticale se confondent, parce que c'est là que la reconstruction du texte de référence casse. Une phrase qui se lit sans ambiguïté ne mesure rien.

Le contexte de conversation est **collé à la main** : hors contexte un LLM devine dans le vide, et la moitié de ces cas ne se tranchent que par le contexte.

### Bloc A — prononciation, une famille de son par cas

Les concurrents d'une voyelle ne se répartissent pas comme ceux d'une occlusive, et une erreur sans rival lexical ne se répartit pas du tout : ce bloc balaie ces régimes.

| # | À dire | Contexte | Régime | Attendu |
|---|--------|----------|--------|---------|
| 1 | `I sink you are right` | `think.txt` | fricative, rival lexical réel | `intended = "I think you are right"`, donc /θ/ attendu et phonème marqué. Échec si `sink` est pris au mot : score parfait sur la faute exacte. |
| 4 | `I sink you are right`, /θ/ **correct** | `think.txt` | idem, sans faute | rien. La transcription peut encore rendre `sink` ; le phonème est bon. |
| 5 | `The sink is broken` | `sink.txt` | idem, décision inverse | rien — `sink` est le mot juste. **Enregistré et passé** : la reconnaissance rend `The sink is broken` sans qu'on lui souffle rien. Le contexte n'a donc pas eu à servir, et le cas 1 s'était résolu tout seul dans l'autre sens. Le piège se déclenche moins souvent qu'aucune des deux hypothèses ne le prévoyait. |
| 6 | `The ship is in the field` | `field.txt` | **voyelle** /iː/→/ɪ/, rival lexical réel | `intended = "The sheep is in the field"`. Cinq concurrents voisins : c'est là que le signal risque de s'écraser. |
| 7 | `I picked a bear from the tree` | `orchard.txt` | **occlusive** /p/→/b/, rival lexical réel | `intended = "... a pear ..."`. Voisement, pas lieu d'articulation — l'erreur la plus fréquente et la moins audible. |
| 8 | `Turn light at the corner` | `directions.txt` | **approximante** /r/→/l/, rival lexical réel | `intended = "Turn right at the corner"`. |
| 9 | `I am walkin to the office` | `office.txt` | **nasale** /ŋ/→/n/, **aucun rival lexical** | marque sur le phonème final. Rien vers quoi un modèle de langue puisse normaliser, donc la transcription ne peut pas masquer la faute. |
| 10 | `The chair is very comfortable`, `comfortable` en quatre syllabes | `chair.txt` | **réduction**, aucun rival | erreur interne au mot, pas un échange de phonème. Le cas qui dit où s'arrête le signal phonémique. **Ce n'est pas un cas d'accent** : l'appui reste sur la première syllabe, seule la réduction manque. Un moteur qui rend « accent correct » a raison, et le bloc E est là pour éprouver l'accent. |

### Bloc B — grammaire, prononciation propre

Deux échecs symétriques à surveiller : une marque de prononciation qui tombe en plus (fausse alerte), et un scoring fait contre `repaired` au lieu d'`intended`.

| # | À dire | Contexte | Attendu |
|---|--------|----------|---------|
| 2 | `He don't know` | `know.txt` | `intended` égale la transcription, `repaired = "He doesn't know"`. |
| 3 | `I have 25 years` | `age.txt` | les deux textes divergent (`repaired = "I am 25 years old"`). Scorer contre `intended` : l'audio ne contient pas *am*. Échec si le score reste élevé alors qu'on note contre un texte que la bouche n'a pas dit. |
| 11 | `I go to the school every day` | `school.txt` | `repaired = "I go to school every day"`. Article superflu : la faute la plus discrète, celle qui teste la sévérité plutôt que la détection. |
| 12 | `Yesterday I go to the market` | `market.txt` | `repaired = "Yesterday I went to the market"`. Divergence d'un seul mot, contre le cas 3 qui en change trois. |

### Bloc C — témoin, aucune faute

L'analyse ne se juge pas sur les cas adversariaux seuls : ce qu'elle fait de ce qui est **bien** prononcé compte autant, puisqu'une fausse alerte coûte la confiance. Ces trois phrases reprennent les sons des cas 1, 6, 7 et 8, dites correctement — les mesures se comparent donc par paires, jamais dans l'absolu.

C'est aussi le bloc qui vérifie la méthode de mesure elle-même : un témoin doit ressortir **à égalité** avec le modèle de référence, et tout écart qu'on lit sur lui est du bruit qu'on lira partout ailleurs.

| # | À dire | Contexte | Couvre |
|---|--------|----------|--------|
| 13 | `I think the sheep are in the field` | `field.txt` | /θ/ et /iː/ propres — témoins des cas 1 et 6 |
| 14 | `I picked a pear from the tree` | `orchard.txt` | /p/ propre — témoin du cas 7 |
| 15 | `You have to turn right at the corner` | `directions.txt` | /r/ propre — témoin du cas 8 |

### Bloc D — fautes franches

Ajouté après coup, et c'est le bloc le plus instructif du jeu.

| # | À dire | Reprend | Ce qu'il corrige |
|---|--------|---------|------------------|
| 16 | `The ship is in the field`, voyelle **relâchée** et non raccourcie | cas 6 | la faute du cas 6 portait sur la **durée** ; le contraste anglais porte sur le **timbre** |
| 17 | `I sink you are right`, un vrai /s/ franc | cas 1 | le /s/ du cas 1 était resté à mi-chemin entre les deux sons |
| 18 | `I am walkin to the office`, un vrai /n/ franc | cas 9 | idem |

#### La leçon de méthode

**Un jeu de fautes délibérées mesure autant la capacité du locuteur à faire des fautes franches que celle de la machine à les voir.** Les cas 1, 6 et 9 ont d'abord passé pour des échecs de détection ; ils étaient des demi-fautes, rapportées honnêtement comme des sons intermédiaires. Le bloc D l'a établi : à faute franche, signal franc.

Corollaire pour toute campagne de mesure future : **une faute non détectée n'est pas un échec de l'analyse tant qu'on n'a pas vérifié qu'elle a été réellement produite.** Le contrôle se fait en refaisant la prise avec le geste articulatoire correct — pas en écoutant l'enregistrement, où l'on entend ce qu'on croit avoir dit.

Autre effet, visible entre les prises : quand on articule avec soin, **tout le système vocalique se décale d'un coup**. La même voyelle correcte dans le même mot rend des mesures très différentes selon le degré d'attention du locuteur. Aucun seuil fixe ne survit à ça — c'est ce qui a conduit à mesurer par écart à un modèle plutôt que par note absolue.

### Bloc E — prosodie

Les blocs A à D n'éprouvaient que le phonème : le seul cas qui semblait prosodique, le 10, est une faute de réduction et laisse l'accent intact.

Deux échelles à séparer, parce qu'elles n'ont pas la même portée : l'**accent** est une propriété du mot, fixée par la langue ; la **mélodie** est une propriété de la phrase. Chacune avec son témoin, dans la même voix et le même jour.

| # | À dire | Échelle | Attendu |
|---|--------|---------|---------|
| 19 | `The chair is very comfortable`, accent sur **com** | mot, témoin | accent attendu et produit concordants |
| 20 | `The chair is very comfortable`, accent sur **for** | mot | l'accent attendu n'est pas réalisé sur la première syllabe |
| 21 | `You are going to the office`, dit comme une **question** montante | phrase, témoin | contour qui monte en fin d'énoncé |
| 22 | `You are going to the office`, dit **plat** — l'aplatissement français | phrase | contour final qui ne monte pas, à distinguer du 21 |

Résultat : la mélodie sépare franchement — dix-sept demi-tons entre la question et le plat. L'accent, lui, est resté indécis — le bloc F reprend la question avec un protocole qui définit ce qu'est un témoin.

Ce bloc a établi au passage une chose que la synthèse ne pouvait pas donner : **un témoin de prosodie doit être vérifié sur les échantillons**, durée, énergie et hauteur, avant de conclure quoi que ce soit. Sans cette vérification, la fausse alerte du témoin 19 passait pour une faute du locuteur.

### Bloc F — l'accent, par imitation d'un modèle

Le bloc E laissait l'accent indécis : la faute était vue, mais le témoin se déclenchait aussi. La cause était le protocole, pas la mesure — on avait demandé au locuteur de prononcer *correctement*, ce qui n'est pas une consigne mesurable. L'analyse ne compare pas à une norme, elle compare à une réalisation.

**Un témoin d'accent est donc un calque d'un modèle entendu, jamais une prononciation « correcte ».**

Protocole, qui s'auto-étiquette et se répète sur n'importe quel mot :

1. synthétiser la phrase, et **vérifier d'abord que le modèle se lit proprement** — sinon la comparaison part faussée ;
2. enregistrer une prise **spontanée**, avant toute écoute (une fois le modèle entendu, on ne peut plus le désentendre) ;
3. écouter le modèle ;
4. enregistrer le **calque**.

| # | À dire | Rôle |
|---|--------|------|
| 23 | `It is important for me`, spontanément | faute naturelle attendue — le français tire l'accent vers la fin |
| 24 | idem, en calquant le modèle | témoin |
| 25 | `This lesson is interesting for me`, spontanément | idem |
| 26 | idem, en calquant le modèle | témoin |

Mots choisis pour deux raisons : leur accent tombe sur des syllabes différentes (2 pour `important`, 1 pour `interesting`), et ce sont des quasi-cognats du français, donc l'interférence est maximale.

Résultat : deux fautes sur deux vues, aucune fausse alerte sur les calques.

**Ce que ce bloc ne mesure pas**, et ne peut pas mesurer : le taux de fausse alerte sur un tour spontané. Étiqueter une prise spontanée exigerait le modèle qu'elle n'a précisément pas entendu. C'est une limite de méthode, pas d'échantillon — aucune quantité de prises supplémentaires ne la lèvera.

### Les prises sont bonnes — vérifié

Question posée parce qu'un suivi de hauteur qui ne se trompe jamais sur de l'audio synthétique se trompait sur 19 % des valeurs de nos prises. Le soupçon portait sur le micro ; il était infondé.

Crête à −2,8 dBFS, RMS autour de −11 dBFS, **aucun échantillon saturé**. Une estimation de f0 par autocorrélation, faite sur les mêmes échantillons, concorde sur la totalité des syllabes de synthèse et sur 21 des 24 syllabes humaines : l'instrument de contrôle est valide, et il innocente les prises. **Rien à réenregistrer** — le bloc E se prend tel quel, avec le même matériel.

Deux artefacts à connaître avant de lire un résultat, l'un et l'autre silencieux :

- un segment dont la durée rendue est absurdement courte est un décrochage d'alignement, et il contamine la lecture du mot entier — c'est ce qui a fait apparaître un faux défaut d'accent sur `corner` dans la prise 15 ;
- une syllabe dont la hauteur rendue sort d'un facteur deux autour de la médiane du tour est un accrochage harmonique, et son étiquette de mélodie est fausse. C'est la raison d'être du contrôle de f0 indépendant du banc.

### Ordre d'enregistrement

Par paires, pour que chaque cas adversarial ait son témoin le même jour et dans la même voix : 13 puis 1, 4, 6 ; 14 puis 7 ; 15 puis 8 ; puis 9, 10 ; puis le bloc B (2, 3, 11, 12), qui ne dépend d'aucun témoin ; le bloc D en dernier, une fois qu'on sait quelle faute était molle. S'arrêter en cours de route laisse quand même des paires complètes.

### Où vivent les prises

Les vingt-sept `.wav` (16 kHz mono) vivent dans `bench/out/takes/set/`, gitignoré comme tout ce dossier : c'est une voix, et publier une voix ne se défait pas. Mais elles ne se régénèrent pas non plus, et toute qualification rejouée exige exactement ces enregistrements-là : le dossier entier est donc **copié dans `/mnt/data/BAK/speakup-takes/`**, seul exemplaire qui survive à un `git clean -xdf`. Même disque que le dépôt — la copie couvre l'effacement, pas la panne. Elle est une photo, à refaire quand une prise s'ajoute. Les contextes de conversation qui les accompagnent sont dans `tmp/bench/contexts/`.

Une contrainte de prise apprise à la seconde session : la parole ne doit pas commencer à l'instant zéro du fichier. `rec` laisse passer un transitoire d'ouverture qui sature la première demi-seconde, et couper ce transitoire emporte l'attaque du premier mot si l'on a parlé trop tôt — l'aligneur rend alors des segments de durée nulle sur tout le début. Enregistrer avec `trim 1 6` et attendre deux secondes avant de parler.
