# L'analyse embarquée

Exploration d'un moteur d'analyse qui tourne **sur l'appareil**, sans service d'analyse distant. Ce n'est pas une décision : c'est une piste instruite jusqu'au point où elle devient mesurable, écrite pour qu'un banc puisse la juger et pas seulement l'imaginer.

Ce qu'elle retirerait : le poste à abonnement plancher. Ce qu'elle ne retire pas : la conversation et la synthèse, qui restent distantes en BYOK et se paient aux centimes. `NonFreeNet` reste déclarée tant qu'un maillon distant subsiste.

## Les deux principes

Toute la forme du pipeline découle de deux décisions, et elles ne sont pas des choix d'implémentation.

**1. Le modèle est la source de vérité, et on lui fait confiance aveuglément.** Rien d'extérieur aux deux enregistrements n'est jamais consulté pour juger : ni dictionnaire de prononciation, ni lexique de dialecte, ni phonémiseur, ni référentiel de justesse. La seule question posée est *en quoi cette prise s'écarte-t-elle de celle-là*.

Ce principe se paie et se gagne. Il se gagne parce que l'apprenant imite un enregistrement, pas un livre : si la voix lie, réduit ou escamote, c'est ça la cible, et un dictionnaire l'aurait mesuré contre une prononciation que personne ne lui a fait entendre. Il se gagne aussi parce que le problème du dialecte disparaît à la racine — le modèle est britannique, donc tout ce qui en dérive l'est, mécaniquement, sans référentiel à choisir ni accord à vérifier. Il se paie parce qu'un modèle bâclé devient un mauvais étalon sans que rien ne le signale (cf. « Les cas limites »).

**2. On ne compare jamais deux notes, mais deux formes.** Le modèle acoustique ne rend pas un score : il rend, à chaque instant, la répartition de la ressemblance sur tous les sons de l'anglais. `R 0.90 / W 0.10` et `R 0.90 / ER 0.10` ont le même pic et ne disent pas la même chose. Comparer les répartitions entières plutôt que leur maximum est ce qui rend la mesure honnête.

Ce principe est né d'une objection. Tant qu'on comparait deux pics, le pic du modèle était le maximum de sa propre ligne — la grille étant tirée de cette ligne, la « mesure de référence » n'était qu'une relecture d'un argmax, et l'écart se réduisait à la valeur de l'apprenant. Le nombre n'est pas vide pour autant : il porte le **plafond local** — un son que le réseau distingue mal pique bas même sur une synthèse parfaite, et une syllabe floue pique bas parce que la voix était floue. Mais c'est une échelle, pas un terme de comparaison. Comparer les répartitions le remet à sa place et donne trois choses gratuitement, dites plus bas.

## Ce qui entre, ce qui sort

Entrent : l'audio du modèle, l'audio de l'apprenant, et le texte de ce qui a été dit.

Sortent les trois marques du projet — le son, le mot, la phrase — ancrées à des lettres précises du texte affiché.

Le texte ne juge rien. Il sert à trois choses seulement : afficher, situer les lettres dans le temps, séparer les mots.

## Les briques

### 1. Le modèle — ElevenLabs

L'endpoint de synthèse `with-timestamps` rend l'audio **et** la position de chaque caractère, en REST nu.

```
p 0.11-0.16   o 0.16-0.21   r 0.21-0.26   t 0.26-0.34
```

Ce n'est pas une estimation : c'est ce que le synthétiseur a fabriqué. C'est le seul endroit du pipeline où le texte touche l'audio.

### 2. La matrice — une passe par audio

Un modèle acoustique phonémique (`wav2vec2` affiné en reconnaissance de phonèmes, entraînement CTC) transforme un enregistrement en un tableau : toutes les 20 ms, la répartition de la ressemblance sur les quelque 45 sons de l'anglais, plus un symbole `∅` qui signifie « rien ne se prononce ici ». Chaque ligne somme à 1.

```
à 0.22 s :   R 0.94   ER 0.03   W 0.01   ∅ 0.01   reste 0.01
```

Le `∅` est majoritaire : le réseau le sort la plupart du temps et **pique** brièvement sur un son quand il est sûr. La matrice est donc surtout du blanc avec des pics nets, ce qui la rend lisible.

Tout ce qui suit — briques 3, 5, 6, 9 — est une **lecture de ce tableau**. Il n'est calculé qu'une fois par audio, et celui du modèle est mis en cache avec sa synthèse : en conversation, c'est une passe par tour de parole.

Propriétés qui comptent : déterministe en CPU (même fichier, même matrice), résolution 20 ms (un phonème dure 50 à 150 ms, c'est confortable), aucun mot ni grammaire — le réseau ne connaît que des sons.

Les poids retenus sont `vitouphy/wav2vec2-xls-r-300m-timit-phoneme`, Apache-2.0, 1,26 Go en flottant, 315 M de paramètres, 20 ms par trame vérifiées, 39 sons anglais en notation IPA — les diphtongues et les affriquées y sont des unités, pas des morceaux. Cinq candidats ont été mesurés sur le même jeu avant celui-là (cf. « Ce qui départage un modèle acoustique »).

### 3. La grille — décodage libre du modèle

Le plus fort de chaque ligne, les `∅` jetés, les répétitions fusionnées.

```
IH 0.00-0.05   M 0.05-0.11   P 0.11-0.16   AO 0.16-0.22   R 0.22-0.26   T 0.26-0.34
```

Ce ne sont pas les sons « corrects » du mot : ce sont ceux que cette voix-là a produits dans cette phrase-là.

**La grille ne sert qu'à savoir quelles lignes se font face.** Elle ne note rien, et son étiquette n'entre pas dans la mesure : si le réseau écrit `AH` là où le modèle a dit `AO`, l'apprenant est aligné sur ce `AH`, les instants restent bons, et la comparaison porte sur les deux lignes complètes — le symbole n'y apparaît nulle part. La grille peut donc se tromper sur **quel** son c'est, tant qu'elle ne se trompe pas sur **où** il est.

Les étiquettes servent malgré tout à trois tâches d'échafaudage, où elles comptent vraiment : repérer les voyelles (brique 7), syllaber (règle des attaques légales), comparer deux phrases (brique 9).

### 4. La jointure lettres ↔ sons

Les briques 1 et 3 décrivent le même fichier sur le même axe de temps. On les superpose.

```
lettres  p o r  = 0.11-0.26 s
sons     P 0.11-0.16   AO 0.16-0.22   R 0.22-0.26
-> le p porte le P, le o porte le AO, le r porte le R
```

C'est ainsi qu'une marque atterrit sur une lettre précise. Deux horloges lues ensemble, rien d'appris, aucun aligneur graphème-phonème à construire — c'est l'horodatage caractère du synthétiseur qui l'offre.

Les deux irrégularités connues restent : une lettre peut porter deux sons (elle prend la couleur du pire), une lettre peut n'en porter aucun (elle reste neutre).

### 5. L'apprenant sur la grille — alignement forcé

Dans la matrice de l'apprenant, on cherche le meilleur chemin qui respecte la suite de la grille.

```
AO 0.19-0.27 s    R 0.27-0.29 s
```

Le même symbole désigne maintenant le même endroit chez les deux. C'est la seule chose dont on avait besoin pour comparer, et c'est ce qui remplace toute tentative de coller les deux audios l'un sur l'autre : la correspondance passe par un repère symbolique commun, pas par la ressemblance des signaux — donc la différence de voix n'y entre pas.

### 6. Le son — recouvrement de deux formes

On met face à face les deux répartitions, aux instants qui se correspondent.

```
modèle,    sur son R :  R 0.94   ER 0.03   W 0.01   reste 0.02
apprenant, au même endroit :  W 0.70   AO 0.15   R 0.05   reste 0.10
```

Les deux formes ne se recouvrent presque pas : marque, posée sur le `r` de `important`. Et la masse est partie sur `W`, donc on sait vers quoi il a dérivé.

Une distance entre distributions rend un seul nombre calculé sur toute la ligne — Jensen-Shannon fait l'affaire : bornée, symétrique, sans paramètre.

Trois propriétés tombent gratuitement de cette forme de comparaison :

- **La difficulté propre d'un son ne se paie pas.** Un son que le réseau distingue mal donne une forme large des deux côtés, et deux formes larges se recouvrent bien. Aucun calibrage par phonème n'est nécessaire.
- **Le flou du modèle devient de la tolérance, sans seuil.** Si le modèle hésite lui-même (`AO 0.44 / AH 0.38`, parce qu'il a réduit la syllabe), un apprenant qui atterrit sur l'un ou l'autre reste proche de sa forme et n'est pas marqué. Là où le modèle est net, la même divergence marque. Le « on ne sait rien ici » se règle mécaniquement.
- **Le son produit est nommé.** Là où la masse de l'apprenant s'est déplacée, c'est ce qu'il a dit. C'est l'enrichissement que le contrat de `../reference.md` classe en confort et qu'aucun service mesuré ne rendait correctement.

### 7. Le mot — l'accent

**Ce qui se mesure : le noyau.** L'accent est une propriété de la **syllabe** — c'est la formulation linguistiquement correcte, et « voyelle accentuée » n'en est pas une. Mais sa réalisation acoustique est concentrée sur le noyau, qui en anglais est une voyelle dans la quasi-totalité des syllabes accentuées. Mesurer sur la voyelle est la pratique standard, pas un raccourci.

Trois traits, et ils n'ont pas le même poids :

- **La durée du noyau** — marqueur d'accent lexical, robuste. Normalisée dans le mot, faute de quoi un locuteur lent serait marqué partout.
- **La qualité de la voyelle** — le plus fort en anglais, où une syllabe non accentuée se réduit en schwa. Et il est déjà là : c'est la grille elle-même qui l'écrit. `IH · AO · AH` chez le modèle contre `IH · AH · AH` chez l'apprenant dit que la voyelle qui devait être pleine a été réduite, sans qu'on ait rien mesuré.
- **Le pic de hauteur** — en appoint seulement. Une syllabe accentuée ne reçoit un pic que si le **mot** est mis en relief dans la phrase : dans « that's an important thing » dit vite, l'accent lexical est toujours sur `por` mais il n'y a plus de pic à voir. Peser dessus reviendrait à mesurer le relief de phrase en croyant mesurer l'accent du mot.

Comme tout se compare au modèle et non à une norme, un mot sans relief des deux côtés reste comparable : les deux manquent de pic ensemble.

**Ce qui s'affiche : la syllabe.** `ui-flow.md` a tranché la forme — graisse sur la syllabe que le modèle accentue, et sur une faute deux réglettes sous la ligne, rouge où l'accent est parti, verte où il devait tomber. Deux réglettes se comparent, deux points non ; et les trois canaux graphiques sont des étendues. Il faut donc une étendue syllabique exacte, pas approchée.

### 8. La syllabification — sur la grille, pas sur le texte

Elle se calcule par une règle phonotactique, sur des sons, sans aucun dictionnaire : entre deux voyelles, le groupe de consonnes reçoit la plus grande attaque possible qui reste prononçable en anglais, le reste retombant sur la syllabe précédente. La seule donnée est la liste des attaques légales de l'anglais, une cinquantaine de groupes.

```
IH M P AO R T AH N T
IH..AO : groupe M P -- "mp-" illégal, "p-" légal -> coupe après M
AO..AH : groupe R T -- "rt-" illégal, "t-" légal -> coupe après R
-> IH M / P AO R / T AH N T -> im / por / tant
```

Les frontières sont des instants, et les instants donnent des lettres par la brique 4. L'étendue est exacte, pas heuristique.

**Le dialecte se règle tout seul**, puisqu'on syllabe ce que le modèle a réellement prononcé :

```
market GB non rhotique : M AA K IH T   -> groupe K  -> M AA / K IH T  -> mar / ket
market US rhotique     : M AA R K IH T -> groupe R K -> M AA R / K IH T -> mar / ket
```

Deux grilles différentes, le même résultat à l'écran, aucun lexique consulté. C'est ce qui écarte définitivement la voie du dictionnaire : CMUdict ne porte pas de frontières de syllabes (seulement des marques d'accent, qui permettent de compter et non de placer) et il est américain seul ; les lexiques britanniques existent (BEEP, Unisyn, CUBE) avec des licences hétérogènes et des couvertures inégales. Deux fichiers, deux couvertures, deux jeux de mots absents — pour un résultat qu'une règle de cinquante lignes donne mieux, parce qu'elle lit l'audio au lieu de citer le mot isolé.

Réserve honnête : la syllabification phonologique ne coïncide pas toujours avec la coupure orthographique attendue. `happy` donne `HH AE / P IY`, donc `ha / ppy`, là où un tiret d'imprimeur écrirait `hap-py`. Le `p` appartient réellement aux deux — c'est une ambiguïté de l'anglais, et tout choix y est arbitraire. Si la graisse sur `ppy` heurte à la lecture, une règle de plus la corrige (une consonne doublée à l'écrit se partage) ; ça se juge sur du texte marqué, pas d'avance. Le repli, si le découpage produisait des étendues illisibles, est de marquer sur la voyelle seule — graisse sur la lettre du noyau, pastilles rouge et verte sur une faute.

### 9. Le découpage en mots

Nécessaire, parce que la comparaison des noyaux se fait **à l'intérieur d'un mot** : sans frontières, on comparerait la voyelle de `important` à celle de `the`.

Il tombe de la brique 1 : le texte a des espaces, chaque caractère a un instant, donc chaque mot a une plage de temps, et chaque voyelle appartient au mot dont la plage la contient.

Deux règles à poser : une voyelle à cheval sur deux mots appartient à celui où tombe **son milieu** ; un mot d'une seule voyelle n'a pas d'accent interne, donc ne porte **jamais** de marque d'accent.

### 10. La phrase — la mélodie

Hauteur de voix toutes les 10 ms par autocorrélation, région voisée finale, pente en demi-tons, des deux côtés.

```
modèle « You're going to the market? » : +7 demi-tons sur les 300 dernières ms
apprenant : -3   ->   dix demi-tons d'écart
```

Autonome : elle ne lit même pas la matrice, ne dépend d'aucune autre brique, et son code existe déjà (`tmp/bench/audio_probe.py`, numpy seul). C'est la brique la plus mûre du lot et la seule envisageable indépendamment du reste.

### 11. Le contrôle — décodage libre de l'apprenant

Le geste de la brique 3 appliqué à l'autre matrice, et les deux suites comparées.

```
modèle    : L EH F T
apprenant : R AY T
```

Ce ne sont pas deux prononciations de la même chose. C'est le garde-fou du texte de référence faux : si le texte a été mal reconstruit, le modèle synthétise autre chose que ce qui a été dit, l'alignement forcé plaque quand même la grille sur la voix de l'apprenant, et on marquerait une rafale de fautes sur quelqu'un d'irréprochable. Ici les deux suites divergent franchement, et on ne marque rien.

Rôle strictement à l'échelle de la phrase : nommer le son produit appartient à la brique 6.

### 12. Le runtime Android

**ONNX Runtime** (MIT) en inférence CPU — le CPU est déterministe, le GPU ne l'est pas toujours, et le déterminisme est un critère de qualification. **sherpa-onnx** (Apache-2.0) empaquette déjà pour Android le décodage CTC et l'alignement forcé : à regarder avant d'écrire la plomberie soi-même.

Poids sous licence libre uniquement — un poids non libre serait un `NonFreeAssets` et tuerait l'intérêt de la brique. Téléchargement en opt-in explicite au premier usage, jamais au premier lancement ni en silence.

## Une seule machine, plusieurs lectures

Les briques 3, 5, 6 et 11 ne sont pas quatre traitements : c'est **une passe par audio**, dont on tire plusieurs lectures.

| audio | passes | ce qu'on en tire |
|---|---|---|
| modèle | une, mise en cache avec la synthèse | la grille (3), la syllabification (8), les répartitions de référence (6) |
| apprenant | une, par tour de parole | l'alignement sur la grille (5), les répartitions comparées (6), le décodage libre (11) |

L'alignement forcé et le décodage libre lisent la même matrice, seule la façon de la parcourir change.

## Le bilan matériel

| # | brique | ce qu'elle rend | état |
|---|---|---|---|
| 1 | Le modèle (ElevenLabs) | audio + lettres → temps | existe, REST nu |
| 2 | La matrice | répartition sur les sons, toutes les 20 ms | codée (`bench/matrix.py`) |
| 3 | La grille | sons réellement produits par le modèle → temps | codée |
| 4 | La jointure lettres ↔ sons | quelle lettre porte quel son | à écrire |
| 5 | L'apprenant sur la grille | mêmes sons → temps chez lui | codée |
| 6 | Le son | recouvrement des deux formes, et le son produit | recouvrement codé (`bench/overlap.py`) ; le son produit à écrire |
| 7 | Le mot | quelle syllabe est la forte | à écrire |
| 8 | La syllabification | l'étendue en lettres de chaque syllabe | à écrire |
| 9 | Le découpage en mots | quelles voyelles appartiennent au même mot | tombe de la 1 |
| 10 | La phrase | la pente de fin d'énoncé | codée |
| 11 | Le contrôle | texte de référence faux | motif observé, seuil à poser |
| 12 | Le runtime Android | tout ça sur le téléphone | à câbler |

**Un seul fichier extérieur dans tout le pipeline : les poids du modèle acoustique**, une centaine de Mo une fois quantifiés en entiers 8 bits, contre environ 350 en flottant. Aucune donnée linguistique, aucun dictionnaire, aucun lexique de dialecte, aucune table graphème-phonème.

Trois briques seulement demandent du travail neuf et non trivial : la jointure (4), l'accent (7), la syllabification (8). Quatre autres sont des lectures d'un calcul déjà fait.

## Les cas limites

Ce qui produirait un comportement inattendu, et où ça se traite.

### Le modèle escamote, lie, ou reste mou

- **La voix avale un son.** La grille ne le contient pas ; l'apprenant qui le prononce présente un son hors grille, que l'alignement forcé étale sur les voisins. Peut déplacer une marque d'un son. À surveiller.
- **La voix lie deux mots** (`want to` → `wanna`). Moins de sons que le texte n'a de lettres, et la frontière de mots devient floue à la jointure. À surveiller.
- **La voix est molle partout** — débit rapide, tout réduit. Toutes les répartitions sont larges, tout se recouvre, et **l'app ne marque presque rien**. C'est le cas le plus vicieux : sous-détection totale et silencieuse, et c'est le prix exact du principe de confiance aveugle. Un contrôle simple existe et n'a pas besoin de norme extérieure — la netteté moyenne des pics de la grille. Une grille molle veut dire « cette prise n'est pas un bon étalon ».

### Le texte n'est pas fait de lettres qui se prononcent

- **Chiffres et symboles** (`25`, `3rd`, `$10`) : deux caractères horodatés pour six sons prononcés, la jointure lettres ↔ sons s'effondre localement. **À traiter** en normalisant le texte en toutes lettres avant synthèse, ce qui est de toute façon souhaitable pour le TTS.
- **Sigles** (`USA`, `OK`) : même problème, plus rare, même traitement.
- **Ponctuation et majuscules** : horodatées mais muettes. Couvert par la règle de la lettre neutre.
- **Nom propre ou mot étranger** : le TTS l'invente et un réseau phonémique anglais le décode mal. Grille bruitée localement. À surveiller.

### L'apprenant ne dit pas exactement la grille

- **Hésitation, reprise, toux au milieu** : l'alignement forcé doit quand même caser la grille, donc il étire un son sur toute l'hésitation. Couvert par le filtre de durée absurde, qui est déjà une exigence du contrat.
- **Il s'arrête avant la fin** : la fin de la grille se tasse sur les derniers échantillons, produisant une rafale de fautes finales. Couvert par la brique 11.
- **Bruit ou silence en tête** : le premier son peut s'accrocher au bruit. À surveiller.
- **Débit très différent du modèle** : indifférent à l'alignement, qui est élastique ; mais la brique 7 compare des durées. C'est ce qui rend la normalisation dans le mot obligatoire et non optionnelle.
- **Il chuchote** : pas de fréquence fondamentale, donc mélodie muette et accent privé de son trait de hauteur. Doit dégrader proprement, jamais marquer.

### L'accent, spécifiquement

- **Mot d'une syllabe** : rien à comparer, ne jamais marquer. Règle explicite.
- **Mot fonctionnel non mis en relief** (`the`, `of`) : les traits sont plats des deux côtés. Correct, mais demande un seuil « le modèle a-t-il un accent net » sous lequel on ne marque pas, sinon on marque du bruit.
- **Consonne syllabique** (`bottle`, `button`) : le noyau n'est pas une voyelle, le mot est compté à une syllabe, aucune marque d'accent n'y apparaît jamais. Silencieux, pas faux — et sans conséquence, ces noyaux n'apparaissant en anglais que dans des syllabes non accentuées.
- **Emphase de sens** (« I said it **IS** important ») : l'apprenant met le relief là où le modèle neutre ne l'a pas, et la divergence légitime est marquée comme faute. Connu et accepté (cf. `../reference.md`) — la forme de la parenthèse rend la fausse alerte peu coûteuse.

### La comparaison elle-même

- **Deux voix très différentes** : c'était l'inconnu majeur. Mesuré, et le timbre ne passe pas dans la répartition — cf. « Le locuteur ne laisse pas de trace ».
- **Un son aligné sur des trames majoritairement `∅`** : la comparaison porterait sur du vide. Filtre à prévoir.
- **Tour très long** : le coût de calcul reste linéaire, mais une erreur d'alignement au milieu se propage vers la fin. À surveiller.

### Trois décisions qui n'attendent pas l'implémentation

La normalisation des chiffres avant synthèse, le contrôle de netteté de la grille, et le seuil d'accent net sous lequel on ne marque pas.

## Le locuteur ne laisse pas de trace

Première mesure faite (`bench/overlap.py`), sur les six phrases du banc. Écart de Jensen-Shannon par son entre la répartition du modèle et celle qui lui fait face, 0 pour deux formes identiques, 1 pour deux formes disjointes :

| comparé au modèle | médian | pire |
|---|---|---|
| une autre voix de synthèse | 0,000 à 0,001 | 0,89 à 0,93 |
| l'apprenant qui calque le modèle entendu | 0,002 à 0,004 | 0,91 à 0,99 |
| l'apprenant à froid, avant d'avoir rien entendu | 0,002 à 0,003 | 0,97 |

**C'est la réponse que le montage attendait.** Deux voix de synthèse différentes, disant correctement la même phrase, se recouvrent au point que la médiane est nulle : le timbre ne survit pas dans la forme de la répartition. Le principe qui fait comparer deux formes plutôt que deux notes tient donc son pari, et la difficulté propre d'un son se paie effectivement des deux côtés à la fois.

La moyenne n'est pas dans le tableau et c'est délibéré : la répartition est presque toujours nulle et parfois saturée, donc une moyenne ne décrit aucun cas réel. **Tout ce qui compte vit dans la queue** — quelques sons montent à 0,9 là où l'immense majorité est à zéro. C'est cohérent avec ce qu'on cherche, une faute étant rare et franche, mais ça ne se départage **pas** ici : il faut savoir quel son porte une faute étiquetée, ce que fait la mesure suivante. Un artefact à ne pas prendre pour du signal : les prises à froid sont lues sur 78 sons contre 96, une capture du jeu étant vide.

### Le mot isolé, et ce qu'il disait vraiment

Le mot dit seul est le cas où deux voix s'accordent le moins : 0,005 de médiane contre 0,000 sur la phrase. Sur le premier modèle mesuré, l'écart était huit fois pire encore (0,039), et sa grille semblait donner la raison — `water` décodé `w uo5 ts.h ɚ`, où `uo5` est une voyelle à ton mandarin. On a d'abord conclu que l'inventaire multilingue partait hors de la langue.

**C'était un mauvais diagnostic**, et le démonter valait le détour, parce que ce qu'il cachait est une propriété du problème et pas d'un modèle. En regardant non plus le symbole gagnant mais toute la répartition, quatre sons de deux mots isolés se répartissent ainsi :

| son | ce que dit Daniel | ce que dit Sonia | écart |
|---|---|---|---|
| voyelle de *water* | `uo5` 0,20 · `uoɜ` 0,11 · `ou5` 0,11 | `oː` 0,19 · `oʊ` 0,11 · `uː` 0,05 | 0,43 |
| /t/ de *water* | `ts.h` 0,35 · `ts.` 0,14 · `th` 0,11 | `t` 0,93 | 0,75 |
| attaque de *dance* | `tɕ` 0,41 · `dʒ` 0,24 | `d` 0,97 | 0,89 |
| finale de *corner* | `ə` 0,89 | `a` 0,65 · `ɑː` 0,11 | 0,69 |

Un seul de ces quatre est un problème de notation : la voyelle de *water*, où les deux voix disent la même chose arrondie postérieure et où le réseau l'écrit dans deux systèmes qui ne se rejoignent pas. **Les trois autres sont de vraies différences de prononciation** — Daniel affrique ses occlusives, Sonia non ; leurs voyelles finales diffèrent. Le réseau les rapporte fidèlement. Et là où les deux voix prononcent pareil, il s'accorde à trois millièmes : `w` à 0,005, `n` à 0,001, `k` à 0,008.

Deux conséquences. La première est que **fusionner les familles de symboles ne réglerait qu'un cas sur quatre**, et demanderait la table phonétique que le montage refuse — enlever les marques de ton ne rapproche pas `uo` de `ou`, et encore moins de `oː`. Écarté.

La seconde est que le banc fait passer ici une épreuve **plus dure que ce que l'app fera jamais** : deux voix étrangères l'une à l'autre, sur un mot sans phrase pour le porter. Dans l'app, le modèle est la voix qu'on vient d'entendre, et le mot est une sous-étendue d'une phrase déjà synthétisée. Le seul cas réel est la reprise du mot seul par l'apprenant, dans la parenthèse.

## L'écart tombe sur le son fautif

Troisième mesure faite (`bench/faults.py`), sur les blocs A, C et D du jeu d'essai — les mêmes prises qui ont départagé les services distants, lues cette fois par deux matrices et aucun service. La lecture est **ancrée** : chaque cas nomme le son de la grille du modèle où la faute a été faite, et le témoin du même son est lu contre le même modèle.

| prise | rôle | son | écart |
|---|---|---|---|
| `16-ship-lax` | franche | /iː/ | 0,983 |
| `07-bear-pear` | faute | /p/ | 0,967 |
| `17-sink-full` | franche | /θ/ | 0,966 |
| `06-ship-sheep` | faute | /iː/ | 0,962 |
| `08-light-right` | faute | /ɹ/ | 0,956 |
| `01-sink` | faute | /θ/ | 0,246 |
| `18-walkin-full` | franche | /ŋ/ | 0,236 |
| `14-pear-clean` | témoin | /p/ | 0,002 |
| `04-th-franc` | témoin | /θ/ | 0,001 |
| `13-field-clean` | témoin | /θ/ | 0,000 |
| `13-field-clean` | témoin | /iː/ | 0,000 |
| `15-right-clean` | témoin | /ɹ/ | 0,000 |

**Sept fautes sur huit vues, aucune fausse alerte, et les témoins à zéro.** SpeechAce en voyait six sur huit avec des témoins à ±1 : la forme est la même et la séparation est plus nette, obtenue sans dictionnaire, sans lexique de dialecte et sans appel. Sur le modèle de voix américain, les six fautes lisibles sont vues et les témoins ne dépassent pas 0,002 — le modèle étant le seul référentiel, l'accent ne change rien, ce qui est le comportement attendu.

La seule manquée est `09-walkin` (/ŋ/ dit /n/), et le jeu d'essai le dit de lui-même : c'est une **demi-faute**, le bloc D existant précisément parce que les cas 1, 6 et 9 étaient restés à mi-chemin. À faute franche, signal franc — la reprise franche du même cas, `18-walkin-full`, est vue.

Deux séparations valent d'être lues séparément. Les fautes franches montent toutes au-dessus de 0,95, et les demi-fautes se rangent en dessous de 0,25 : **l'écart ne rend pas un verdict binaire, il rend un degré**, et c'est un degré qui correspond à ce qui a été produit. C'est de là que sortira un seuil de marquage, pas d'une constante posée d'avance.

### Ce que le pire son d'un énoncé révèle

Le maximum de l'énoncé et l'écart sur le son étiqueté ne désignent pas le même endroit, et la différence est instructive. Le pire son d'une prise « correcte » est régulièrement ailleurs que sur le son testé :

- le modèle dit *you are*, le locuteur dit *you're* ;
- le modèle dit `p ɛɹ`, le locuteur dit `p eɪ ɚ` (*payer*) ;
- le modèle réduit *to* en `t ə`, le locuteur dit `t ʊ`.

Ces prises étaient étiquetées « correctes » sur **un** son précis, celui qu'on testait ; rien n'avait jamais été vérifié du reste. Ce ne sont donc pas des fausses alertes du calcul, ce sont des fautes que le jeu d'essai n'avait pas notées — **si le texte dit *you are*, dire *you're* est une faute**, sans quoi il n'y a plus de référence du tout.

En conversation libre le cas ne se pose presque pas, le texte venant de la transcription de ce qui a été dit : qui dit *you're* aura un modèle qui dit *you're*. Il se pose dans la parenthèse, où la phrase à dire est imposée par l'IA — et c'est justement là que la règle est la bonne.

Ce que la mesure valide reste **l'écart sur un son donné**. La sélection du son à marquer est un autre problème, et le pire de l'énoncé n'en est pas une mauvaise réponse — il pointe des divergences réelles.

### Le motif de la brique 11, et pourquoi son seuil ne peut pas être une constante

Sur le premier modèle mesuré, `18-walkin-full` se décodait `a j a ŋ u k i n ɐ t ɔ̃ z o o f i s` — `ɔ̃` étant une voyelle nasale française. L'énoncé était lu hors de l'anglais, l'alignement forcé plaquait la grille dessus quand même, et **tous** les sons décrochaient d'un coup : médiane à 0,711 là où toute prise qui s'aligne restait sous 0,022. C'est exactement le motif que la brique 11 doit attraper — un texte de référence qui ne correspond pas à ce qui a été dit produit une avalanche, pas une marque.

Le motif est bon ; le seuil qu'on en avait tiré ne l'est pas. Posé à 0,2 sur l'échelle de ce modèle-là, il se déclenche à tort sur le modèle retenu, où il a exclu une détection parfaitement saine. **Le seuil doit être relatif au modèle**, ou mieux, à la prise elle-même : ce qui signale l'avalanche n'est pas une valeur absolue mais le fait que la médiane cesse d'être négligeable devant le maximum.

## Ce qui départage un modèle acoustique

Cinq candidats mesurés sur les mêmes prises et le même modèle de voix. Le classement importe moins que ce qui l'explique : deux critères sont sortis des échecs, et ils se vérifient tous les deux **avant** de lancer quoi que ce soit.

| modèle | inventaire | certitude | témoins | fautes vues |
|---|---|---|---|---|
| `vitouphy/…-timit-phoneme` | 39 sons anglais IPA | 0,926 | ≤ 0,002 | 7 / 8 |
| `facebook/…-espeak-cv-ft` | 392 symboles multilingues | 0,870 | ≤ 0,055 | 5 / 7 |
| `excalibur12/…timit-4k_simplified` | 51 unités TIMIT | 0,929 | ≤ 0,371 | 4 / 8 |
| `charsiu/en_w2v2_fc_10ms` | 39 sons ARPAbet | 0,907 | ≤ 0,796 | 4 / 8 |
| `bookbot/wav2vec2-ljspeech-gruut` | 43 sons anglais IPA | 0,976 | ≤ 0,960 | 3 / 7 |

**Un modèle trop sûr de lui est disqualifié d'office.** La comparaison lit des formes, et la tolérance vit dans ce qui entoure le pic : si le modèle met 97,6 % de sa masse sur un seul son, il ne reste rien à comparer et la lecture redevient un test d'étiquettes — exactement ce que le principe 2 refuse. Ça se mesure en trois secondes sur n'importe quel fichier du banc, et `bench/pull.py` l'imprime avant tout le reste. Au-delà de 0,95, inutile d'aller plus loin. La cause est identifiable : ce modèle-là a été affiné sur **une seule voix** lisant proprement, donc il n'a jamais eu de raison d'hésiter.

**Un modèle entraîné sur des alignements de dictionnaire apprend à pardonner.** On lui a montré des enregistrements où quelqu'un dit *think*, en lui disant « ici c'est un /θ/ », que la personne l'ait prononcé ainsi ou non. Il a donc appris à ramener une réalisation approximative vers le son canonique — et devient aveugle à la substitution qu'on lui demande justement de rapporter. C'est la signature de `charsiu`, à 0,015 sur un /s/ mis pour un /θ/. Le remède est un corpus **transcrit phonétiquement à la main**, où l'annotation dit ce qui a été prononcé : c'est ce qu'est TIMIT, et c'est ce qui fait gagner le modèle retenu.

Deux critères de forme, moins profonds mais éliminatoires. L'inventaire doit être fait de **sons entiers** et non de caractères : `mrrubino` et `speech31`, écartés sur pièces, coupent `aɪ` en deux, ce qui prive la syllabification de toute unité à quoi se raccrocher. Et les poids doivent porter une **licence libre vérifiable** : `charsiu` est publié nu, sans fiche ni licence, le dépôt de code MIT ne couvrant pas un artefact hébergé ailleurs.

Ce que le classement ne dit pas : `excalibur12` part du **même encodeur pré-entraîné** que le multilingue et se fait battre par lui. La différence tient entièrement à l'affinage, pas à l'architecture.

## Ce qui reste à mesurer, dans l'ordre

Chaque étape se juge au protocole de `engine-qualification.md`, sur le matériel déjà enregistré du banc — aucun appel d'API n'est nécessaire.

1. **La quantification abîme-t-elle ce qu'on lit ?** C'est la mesure la plus exposée du lot, et pour une raison propre à ce montage : on ne lit pas le son gagnant, on lit ce qui reste autour de lui, c'est-à-dire l'information qui vit dans les décimales — la première que l'arrondi emporte. Un système qui ne regarde que le maximum survit à la quantification ; celui-ci est le cas le plus fragile qui soit. Joue en notre faveur le fait que les deux enregistrements passent par le **même** modèle, donc qu'un biais systématique s'annule des deux côtés. Se vérifie en rejouant `faults.py` sur les poids quantifiés : les témoins doivent rester à zéro et les fautes franches au-dessus de 0,9.
2. **Le seuil de la brique 11**, à exprimer relativement à la prise plutôt qu'en constante.
3. **La grille est-elle stable ?** Deux rendus du même texte par la même voix doivent donner la même suite de sons. Le cache de synthèse neutralise en partie la question, mais une grille instable rendrait la mesure irreproductible.
4. **Quel son marquer ?** La mesure valide l'écart sur un son donné ; elle ne dit pas lequel mérite une marque. Les fautes franches au-dessus de 0,95 et les demi-fautes sous 0,25 donnent la matière d'un seuil, à condition de le tirer des prises et non de le poser.

La brique 10 ne dépend de rien et pourrait exister avant tout le reste.
