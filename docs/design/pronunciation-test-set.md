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
| 10 | `The chair is very comfortable`, `comfortable` en quatre syllabes | `chair.txt` | **réduction / accent**, aucun rival | erreur interne au mot, pas un échange de phonème. Le cas qui dit où s'arrête le signal phonémique et où la prosodie doit prendre le relais. |

## Bloc B — grammaire, prononciation propre

Deux échecs symétriques à surveiller : une marque de prononciation qui tombe en plus (fausse alerte), et un scoring fait contre `repaired` au lieu d'`intended`.

| # | À dire | Contexte | Attendu |
|---|--------|----------|---------|
| 2 | `He don't know` | `know.txt` | `intended` égale la transcription, `repaired = "He doesn't know"`. |
| 3 | `I have 25 years` | `age.txt` | les deux textes divergent (`repaired = "I am 25 years old"`). Scorer contre `intended` : l'audio ne contient pas *am*. Échec si le score reste élevé alors qu'on note contre un texte que la bouche n'a pas dit. |
| 11 | `I go to the school every day` | `school.txt` | `repaired = "I go to school every day"`. Article superflu : la faute la plus discrète, celle qui teste la sévérité plutôt que la détection. |
| 12 | `Yesterday I go to the market` | `market.txt` | `repaired = "Yesterday I went to the market"`. Divergence d'un seul mot, contre le cas 3 qui en change trois. |

## Bloc C — témoin, aucune faute

Une ligne de base ne se lit pas sur des cas adversariaux : elle se lit sur ce qui est **bien** prononcé. Ces trois phrases reprennent les sons des cas 1, 6, 7 et 8, dites correctement — les mesures se comparent donc par paires, pas dans l'absolu.

Chaque phonème correct de **toutes** les prises alimente aussi cette ligne de base. Le bloc C n'ajoute que la lecture propre, sans faute ailleurs dans la phrase pour déplacer le modèle de langue.

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

Autre effet, visible entre les prises : quand on articule avec soin, **tout le système vocalique se décale d'un coup**. La même voyelle correcte dans le même mot rend des mesures très différentes selon le degré d'attention du locuteur. Aucune ligne de base fixe ne survit à ça.

## Ordre d'enregistrement

Par paires, pour que chaque cas adversarial ait son témoin le même jour et dans la même voix : 13 puis 1, 4, 6 ; 14 puis 7 ; 15 puis 8 ; puis 9, 10 ; puis le bloc B (2, 3, 11, 12), qui ne dépend d'aucun témoin ; le bloc D en dernier, une fois qu'on sait quelle faute était molle. S'arrêter en cours de route laisse quand même des paires complètes.

## Où vivent les prises

Les `.wav` (16 kHz mono) et les contextes de conversation sont dans `tmp/bench/turns/` et `tmp/bench/contexts/`. Ce dossier est gitignoré et se voulait jetable ; **il cesse de l'être tant que le moteur d'analyse n'est pas choisi**, puisque comparer deux fournisseurs exige exactement ces enregistrements-là.

Le cas 5 n'a jamais été enregistré. À prendre avant toute campagne comparative : c'est le seul qui dise si le contexte de conversation sert vraiment.
