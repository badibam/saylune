# Jeu d'essai de prononciation

Dix-huit prises de la même voix, le même jour, dont quinze fautes délibérées et trois témoins. Il sert à **comparer des moteurs d'analyse entre eux** : les mêmes phrases, les mêmes enregistrements, un seul locuteur. Refaire les prises pour chaque fournisseur détruirait la comparabilité, donc les fichiers sont conservés (cf. « Où vivent les prises »).

Chaque phrase des blocs A et B vise l'endroit où faute phonétique et faute grammaticale se confondent, parce que c'est là que la reconstruction du texte de référence casse. Une phrase qui se lit sans ambiguïté ne mesure rien.

Le contexte de conversation est **collé à la main** : hors contexte un LLM devine dans le vide, et la moitié de ces cas ne se tranchent que par le contexte.

## Bloc A — prononciation, une famille de son par cas

Les concurrents d'une voyelle ne se répartissent pas comme ceux d'une occlusive, et une erreur sans rival lexical ne se répartit pas du tout : ce bloc balaie ces régimes.

| # | À dire | Contexte | Régime | Attendu |
|---|--------|----------|--------|---------|
| 1 | `I sink you are right` | `think.txt` | fricative, rival lexical réel | `intended = "I think you are right"`, donc /θ/ attendu et phonème marqué. Échec si `sink` est pris au mot : score parfait sur la faute exacte. |
| 4 | `I sink you are right`, /θ/ **correct** | `think.txt` | idem, sans faute | rien. La transcription peut encore rendre `sink` ; le phonème est bon. |
| 5 | `The sink is broken` | `sink.txt` | idem, décision inverse | rien — `sink` est le mot juste. Le cas qui dit si le contexte sert vraiment. **Jamais enregistré.** |
| 6 | `The ship is in the field` | `field.txt` | **voyelle** /iː/→/ɪ/, rival lexical réel | `intended = "The sheep is in the field"`. Cinq concurrents voisins : c'est là que le signal risque de s'écraser. |
| 7 | `I picked a bear from the tree` | `orchard.txt` | **occlusive** /p/→/b/, rival lexical réel | `intended = "... a pear ..."`. Voisement, pas lieu d'articulation — l'erreur la plus fréquente et la moins audible. |
| 8 | `Turn light at the corner` | `directions.txt` | **approximante** /r/→/l/, rival lexical réel | `intended = "Turn right at the corner"`. |
| 9 | `I am walkin to the office` | `office.txt` | **nasale** /ŋ/→/n/, **aucun rival lexical** | marque sur le phonème final. Rien vers quoi un modèle de langue puisse normaliser, donc la transcription ne peut pas masquer la faute. |
| 10 | `The chair is very comfortable`, `comfortable` en quatre syllabes | `chair.txt` | **réduction**, aucun rival | erreur interne au mot, pas un échange de phonème. Le cas qui dit où s'arrête le signal phonémique. **Ce n'est pas un cas d'accent** : l'appui reste sur la première syllabe, seule la réduction manque. Un moteur qui rend « accent correct » a raison, et le bloc E est là pour éprouver l'accent. |

## Bloc B — grammaire, prononciation propre

Deux échecs symétriques à surveiller : une marque de prononciation qui tombe en plus (fausse alerte), et un scoring fait contre `repaired` au lieu d'`intended`.

| # | À dire | Contexte | Attendu |
|---|--------|----------|---------|
| 2 | `He don't know` | `know.txt` | `intended` égale la transcription, `repaired = "He doesn't know"`. |
| 3 | `I have 25 years` | `age.txt` | les deux textes divergent (`repaired = "I am 25 years old"`). Scorer contre `intended` : l'audio ne contient pas *am*. Échec si le score reste élevé alors qu'on note contre un texte que la bouche n'a pas dit. |
| 11 | `I go to the school every day` | `school.txt` | `repaired = "I go to school every day"`. Article superflu : la faute la plus discrète, celle qui teste la sévérité plutôt que la détection. |
| 12 | `Yesterday I go to the market` | `market.txt` | `repaired = "Yesterday I went to the market"`. Divergence d'un seul mot, contre le cas 3 qui en change trois. |

## Bloc C — témoin, aucune faute

Un moteur ne se juge pas sur les cas adversariaux seuls : ce qu'il fait de ce qui est **bien** prononcé compte autant, puisqu'une fausse alerte coûte la confiance. Ces trois phrases reprennent les sons des cas 1, 6, 7 et 8, dites correctement — les mesures se comparent donc par paires, jamais dans l'absolu.

C'est aussi le bloc qui vérifie la méthode de mesure elle-même : un témoin doit ressortir **à égalité** avec le modèle de référence, et tout écart qu'on lit sur lui est du bruit qu'on lira partout ailleurs.

| # | À dire | Contexte | Couvre |
|---|--------|----------|--------|
| 13 | `I think the sheep are in the field` | `field.txt` | /θ/ et /iː/ propres — témoins des cas 1 et 6 |
| 14 | `I picked a pear from the tree` | `orchard.txt` | /p/ propre — témoin du cas 7 |
| 15 | `You have to turn right at the corner` | `directions.txt` | /r/ propre — témoin du cas 8 |

## Bloc D — fautes franches

Ajouté après coup, et c'est le bloc le plus instructif du jeu.

| # | À dire | Reprend | Ce qu'il corrige |
|---|--------|---------|------------------|
| 16 | `The ship is in the field`, voyelle **relâchée** et non raccourcie | cas 6 | la faute du cas 6 portait sur la **durée** ; le contraste anglais porte sur le **timbre** |
| 17 | `I sink you are right`, un vrai /s/ franc | cas 1 | le /s/ du cas 1 était resté à mi-chemin entre les deux sons |
| 18 | `I am walkin to the office`, un vrai /n/ franc | cas 9 | idem |

### La leçon de méthode

**Un jeu de fautes délibérées mesure autant la capacité du locuteur à faire des fautes franches que celle du moteur à les voir.** Les cas 1, 6 et 9 ont d'abord passé pour des échecs de détection ; ils étaient des demi-fautes, et le moteur les rapportait honnêtement comme des sons intermédiaires. Le bloc D l'a établi : à faute franche, signal franc.

Corollaire pour toute campagne de mesure future : **une faute non détectée n'est pas un échec du moteur tant qu'on n'a pas vérifié qu'elle a été réellement produite.** Le contrôle se fait en refaisant la prise avec le geste articulatoire correct — pas en écoutant l'enregistrement, où l'on entend ce qu'on croit avoir dit.

Autre effet, visible entre les prises : quand on articule avec soin, **tout le système vocalique se décale d'un coup**. La même voyelle correcte dans le même mot rend des mesures très différentes selon le degré d'attention du locuteur. Aucun seuil fixe ne survit à ça — c'est ce qui a conduit à mesurer par écart à un modèle plutôt que par note absolue.

## Bloc E — prosodie

À enregistrer. Les blocs A à D n'éprouvent que le phonème : le seul cas qui semblait prosodique, le 10, est une faute de réduction et laisse l'accent intact.

Deux échelles à séparer, parce qu'elles n'ont pas la même portée : l'**accent** est une propriété du mot, fixée par le dictionnaire ; la **mélodie** est une propriété de la phrase. Chacune avec son témoin, dans la même voix et le même jour.

| # | À dire | Échelle | Attendu |
|---|--------|---------|---------|
| 19 | `The chair is very comfortable`, accent sur **com** | mot, témoin | accent attendu et produit concordants |
| 20 | `The chair is very comfortable`, accent sur **for** | mot | l'accent attendu n'est pas réalisé sur la première syllabe |
| 21 | `You are going to the office`, dit comme une **question** montante | phrase, témoin | contour qui monte en fin d'énoncé |
| 22 | `You are going to the office`, dit **plat** — l'aplatissement français | phrase | contour final qui ne monte pas, à distinguer du 21 |

Les mêmes manipulations ont déjà été faites **en synthèse** contre SpeechAce, avec un résultat net (cf. `speechace.md`) : accent déplacé détecté sur la bonne syllabe, contour fidèle à la manipulation. Ce bloc sert donc à une question précise et unique — le moteur sépare-t-il aussi bien une faute **humaine** qu'une faute fabriquée.

## Les prises sont bonnes — vérifié

Question posée parce que le suivi de hauteur de SpeechAce ne se trompe jamais sur de l'audio synthétique et se trompe sur 19 % des valeurs de nos prises. Le soupçon portait sur le micro ; il était infondé.

Crête à −2,8 dBFS, RMS autour de −11 dBFS, **aucun échantillon saturé**. Une estimation de f0 par autocorrélation, faite sur les mêmes échantillons, concorde avec le moteur sur la totalité des syllabes de synthèse et sur 21 des 24 syllabes humaines : l'instrument de contrôle est donc valide, et il innocente les prises.

Le défaut est dans le moteur, qui s'accroche à la troisième ou quatrième harmonique sur certaines syllabes (cf. `speechace.md`). **Rien à réenregistrer** — le bloc E peut être pris tel quel, avec le même matériel.

Deux artefacts à connaître avant de lire un résultat, l'un et l'autre silencieux :

- un segment dont la durée rendue vaut 10 ms ou moins est un décrochage d'alignement, et il contamine la lecture du mot entier — c'est ce qui a fait apparaître un faux défaut d'accent sur `corner` dans la prise 15 ;
- une syllabe dont la hauteur rendue sort d'un facteur deux autour de la médiane du tour est un accrochage harmonique, et son étiquette de mélodie est fausse.

## Ordre d'enregistrement

Par paires, pour que chaque cas adversarial ait son témoin le même jour et dans la même voix : 13 puis 1, 4, 6 ; 14 puis 7 ; 15 puis 8 ; puis 9, 10 ; puis le bloc B (2, 3, 11, 12), qui ne dépend d'aucun témoin ; le bloc D en dernier, une fois qu'on sait quelle faute était molle. S'arrêter en cours de route laisse quand même des paires complètes.

## Où vivent les prises

Les `.wav` (16 kHz mono) et les contextes de conversation sont dans `tmp/bench/turns/` et `tmp/bench/contexts/`. Ce dossier est gitignoré et se voulait jetable ; **il cesse de l'être tant que le moteur d'analyse n'est pas choisi**, puisque comparer deux fournisseurs exige exactement ces enregistrements-là.

Le cas 5 n'a jamais été enregistré. À prendre avant toute campagne comparative : c'est le seul qui dise si le contexte de conversation sert vraiment.
