# Jeu d'essai de prononciation

Vingt-sept prises d'une même voix : les blocs A à D en une session, le cas 5 et les blocs E et F dans une seconde. Chaque faute délibérée a son témoin, pris le même jour qu'elle. Le jeu sert à **comparer des moteurs d'analyse entre eux** : les mêmes phrases, les mêmes enregistrements, un seul locuteur. Refaire les prises pour chaque fournisseur détruirait la comparabilité, donc les fichiers sont conservés (cf. « Où vivent les prises »).

Chaque phrase des blocs A et B vise l'endroit où faute phonétique et faute grammaticale se confondent, parce que c'est là que la reconstruction du texte de référence casse. Une phrase qui se lit sans ambiguïté ne mesure rien.

Le contexte de conversation est **collé à la main** : hors contexte un LLM devine dans le vide, et la moitié de ces cas ne se tranchent que par le contexte.

## Bloc A — prononciation, une famille de son par cas

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

Enregistré et passé aux deux moteurs. Les blocs A à D n'éprouvaient que le phonème : le seul cas qui semblait prosodique, le 10, est une faute de réduction et laisse l'accent intact.

Deux échelles à séparer, parce qu'elles n'ont pas la même portée : l'**accent** est une propriété du mot, fixée par le dictionnaire ; la **mélodie** est une propriété de la phrase. Chacune avec son témoin, dans la même voix et le même jour.

| # | À dire | Échelle | Attendu |
|---|--------|---------|---------|
| 19 | `The chair is very comfortable`, accent sur **com** | mot, témoin | accent attendu et produit concordants |
| 20 | `The chair is very comfortable`, accent sur **for** | mot | l'accent attendu n'est pas réalisé sur la première syllabe |
| 21 | `You are going to the office`, dit comme une **question** montante | phrase, témoin | contour qui monte en fin d'énoncé |
| 22 | `You are going to the office`, dit **plat** — l'aplatissement français | phrase | contour final qui ne monte pas, à distinguer du 21 |

Résultat : la mélodie sépare franchement — dix-sept demi-tons entre la question et le plat. L'accent, lui, est resté indécis — le bloc F reprend la question avec un protocole qui définit ce qu'est un témoin.

Ce bloc a établi au passage une chose que la synthèse ne pouvait pas donner : **un témoin de prosodie doit être vérifié sur les échantillons**, durée, énergie et hauteur, avant de conclure quoi que ce soit. Sans cette vérification, la fausse alerte du témoin 19 passait pour une faute du locuteur.

## Bloc F — l'accent, par imitation d'un modèle

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

## Les prises sont bonnes — vérifié

Question posée parce qu'un suivi de hauteur qui ne se trompe jamais sur de l'audio synthétique se trompait sur 19 % des valeurs de nos prises. Le soupçon portait sur le micro ; il était infondé.

Crête à −2,8 dBFS, RMS autour de −11 dBFS, **aucun échantillon saturé**. Une estimation de f0 par autocorrélation, faite sur les mêmes échantillons, concorde sur la totalité des syllabes de synthèse et sur 21 des 24 syllabes humaines : l'instrument de contrôle est valide, et il innocente les prises. **Rien à réenregistrer** — le bloc E se prend tel quel, avec le même matériel.

Deux artefacts à connaître avant de lire un résultat, l'un et l'autre silencieux :

- un segment dont la durée rendue est absurdement courte est un décrochage d'alignement, et il contamine la lecture du mot entier — c'est ce qui a fait apparaître un faux défaut d'accent sur `corner` dans la prise 15 ;
- une syllabe dont la hauteur rendue sort d'un facteur deux autour de la médiane du tour est un accrochage harmonique, et son étiquette de mélodie est fausse. C'est la raison d'être du contrôle de f0 indépendant du banc.

## Ordre d'enregistrement

Par paires, pour que chaque cas adversarial ait son témoin le même jour et dans la même voix : 13 puis 1, 4, 6 ; 14 puis 7 ; 15 puis 8 ; puis 9, 10 ; puis le bloc B (2, 3, 11, 12), qui ne dépend d'aucun témoin ; le bloc D en dernier, une fois qu'on sait quelle faute était molle. S'arrêter en cours de route laisse quand même des paires complètes.

## Où vivent les prises

Les `.wav` (16 kHz mono) et les contextes de conversation sont dans `tmp/bench/turns/` et `tmp/bench/contexts/`. Ce dossier est gitignoré et se voulait jetable ; **il cesse de l'être**, puisque toute qualification rejouée exige exactement ces enregistrements-là.

Le lot est complet : les vingt-sept prises existent.

Une contrainte de prise apprise à la seconde session : la parole ne doit pas commencer à l'instant zéro du fichier. `rec` laisse passer un transitoire d'ouverture qui sature la première demi-seconde, et couper ce transitoire emporte l'attaque du premier mot si l'on a parlé trop tôt — l'aligneur rend alors des segments de durée nulle sur tout le début. Enregistrer avec `trim 1 6` et attendre deux secondes avant de parler.
