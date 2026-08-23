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

Les poids mesurés sont `facebook/wav2vec2-lv-60-espeak-cv-ft`, Apache-2.0, 1,2 Go en flottant, 20,1 ms par trame vérifiées. Une réserve : son inventaire n'est pas de 45 sons mais de **392, multilingue**. Sans conséquence sur la comparaison, qui porte sur les lignes entières et voit la même masse hors-anglais des deux côtés — mais c'est lui qui produit le décrochage sur le mot isolé, plus bas.

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

| comparé au modèle | médian | moyen | pire |
|---|---|---|---|
| une autre voix de synthèse | 0,002 | 0,008 à 0,021 | 0,12 à 0,55 |
| l'apprenant qui calque le modèle entendu | 0,009 | 0,062 à 0,136 | 0,65 à 0,99 |
| l'apprenant à froid, avant d'avoir rien entendu | 0,006 à 0,008 | 0,064 à 0,079 | 0,85 |

**C'est la réponse que le montage attendait.** Deux voix de synthèse différentes, disant correctement la même phrase, se recouvrent au point que la médiane est à deux millièmes : le timbre ne survit pas dans la forme de la répartition. Le principe qui fait comparer deux formes plutôt que deux notes tient donc son pari, et la difficulté propre d'un son se paie effectivement des deux côtés à la fois.

Ce que la mesure ne dit **pas** encore, et qu'il faut lire honnêtement : les médianes de l'humain sont du même ordre que celles d'une voix (0,009 contre 0,002), et tout l'écart vit dans la queue — quelques sons montent à 0,99 là où deux synthèses ne dépassent jamais 0,55. C'est cohérent avec ce qu'on cherche, une faute étant rare et franche, mais **ça reste à départager faute par faute** : c'est la troisième mesure, pas celle-ci. Deux artefacts à ne pas prendre pour du signal : les prises à froid sont lues sur 83 sons contre 101 (une capture vide dans le jeu), et la moyenne du calque US dépasse celle de la prise à froid, ce qui n'a pas de sens tant que les fautes ne sont pas étiquetées une à une.

### Le mot isolé décroche

Sur les cinq mots dits seuls, le socle s'effondre : deux voix de synthèse s'écartent de 0,039 en médiane contre 0,002 sur la phrase. La grille de la voix britannique d'ElevenLabs en donne la raison :

```
water  -> w uo5 ts.h ɚ
dance  -> tɕ a n s
market -> n ɑː k ɪ t
```

`uo5` est une voyelle à ton mandarin, `tɕ` et `ts.h` des affriquées absentes de l'anglais. L'inventaire multilingue gagne l'argmax sur des sons qui ne sont pas de la langue, et sur un mot isolé rien dans le contexte ne l'en empêche. Sur les mêmes mots, la voix Azure reste propre (`w oː t ɚ`, `d ɑː n s`).

Restreindre l'inventaire aux symboles vus en anglais connecté **n'est pas le remède** : essayé, ça déplace la masse vers d'autres mauvaises colonnes (`water` devient `w w t ɚ`) et ça casse des mots qui passaient. La piste sérieuse est un modèle acoustique anglais seul, à mesurer contre celui-ci.

À relativiser cependant : dans l'app, le mot n'est **pas** un audio isolé côté modèle — c'est une sous-étendue d'une phrase déjà synthétisée. Seule la reprise du mot seul par l'apprenant, dans la parenthèse, tombe vraiment dans ce cas.

## L'écart tombe sur le son fautif

Troisième mesure faite (`bench/faults.py`), sur les blocs A, C et D du jeu d'essai — les mêmes prises qui ont départagé les services distants, lues cette fois par deux matrices et aucun service. La lecture est **ancrée** : chaque cas nomme le son de la grille du modèle où la faute a été faite, et le témoin du même son est lu contre le même modèle.

| prise | rôle | son | écart |
|---|---|---|---|
| `07-bear-pear` | faute | /p/ | 0,965 |
| `08-light-right` | faute | /ɹ/ | 0,945 |
| `16-ship-lax` | franche | /iː/ | 0,903 |
| `06-ship-sheep` | faute | /iː/ | 0,813 |
| `01-sink` | faute | /θ/ | 0,422 |
| `04-th-franc` | témoin | /θ/ | 0,055 |
| `13-field-clean` | témoin | /θ/ | 0,044 |
| `13-field-clean` | témoin | /iː/ | 0,009 |
| `14-pear-clean` | témoin | /p/ | 0,007 |
| `15-right-clean` | témoin | /ɹ/ | 0,001 |

**Cinq fautes sur sept vues, aucune fausse alerte, et une bande vide de 0,37** entre le pire témoin et la plus faible faute vue. C'est la forme exacte de ce que rendait SpeechAce (six sur huit, témoins à ±1, fautes à −15 ou moins), obtenue sans dictionnaire, sans lexique de dialecte et sans appel. Les deux accents de modèle donnent le même résultat au millième près, ce qui était attendu puisque le modèle est le seul référentiel.

Les deux fautes manquées ne se ressemblent pas :

- `09-walkin` (/ŋ/ dit /n/) est une **demi-faute**, et le jeu d'essai le dit de lui-même : le bloc D existe parce que les cas 1, 6 et 9 étaient restés à mi-chemin. À faute franche, signal franc — c'est ce que fait `16-ship-lax`.
- `17-sink-full` est étiqueté « /s/ franc » et le réseau y entend `aɪ θ ɪ ŋ k`, alors qu'il entend `aɪ s ɪ ŋ k` dans `01-sink`. **L'étiquette et l'audio se contredisent**, et rien dans ce qu'on a ne tranche lequel a tort. À rejouer si le jeu d'essai est réenregistré.

### Le pire son d'un énoncé n'est pas une politique de marquage

Le résultat compte autant que sa mise en garde. Le maximum de la phrase et l'écart sur le son étiqueté ne disent pas la même chose, et l'écart entre les deux vient de divergences **légitimes** :

- le modèle dit *you are*, le locuteur dit *you're* : 0,95 sur la prise fautive **et** 0,97 sur le témoin ;
- le modèle dit `p ɛɹ`, le locuteur dit `p eɪ ɚ` (*payer*) : 0,56 sur un témoin dont le /p/ est à 0,007 ;
- le modèle réduit *to* en `t ə`, le locuteur dit `t ʊ` : 0,70 sur un témoin.

Aucune n'est une faute de prononciation, toutes crèvent le plafond. Marquer le son le plus divergent d'un tour produirait donc trois fausses alertes sur cinq témoins. Ce que la mesure valide est **l'écart sur un son donné**, pas la sélection du son à marquer — et celle-ci demande d'abord la brique 11, pas un seuil.

### La brique 11 se déclenche toute seule

`18-walkin-full` se décode `a j a ŋ u k i n ɐ t ɔ̃ z o o f i s` — `ɔ̃` est une voyelle nasale française. Le réseau a lu l'énoncé hors de l'anglais, l'alignement forcé a plaqué la grille dessus quand même, et **tous** les sons décrochent d'un coup : médiane à 0,711 là où toute prise qui s'aligne reste sous 0,022. C'est exactement le motif que la brique 11 doit attraper, et un seuil de médiane le sépare sans ambiguïté. La prise sort de la comparaison au lieu de compter comme une faute vue.

Deuxième apparition du même défaut de fond : l'inventaire multilingue part hors de l'anglais dès que le signal est un peu moins net. Ça renforce la piste d'un modèle acoustique anglais seul.

## Ce qui reste à mesurer, dans l'ordre

Chaque étape se juge au protocole de `engine-qualification.md`, sur le matériel déjà enregistré du banc — aucun appel d'API n'est nécessaire.

1. **Un modèle acoustique anglais seul fait-il mieux ?** Deux décrochages sur trois lui sont imputables — le mot isolé, et l'énoncé décodé hors de l'anglais. Se compare au même banc, mêmes fichiers.
2. **La grille est-elle stable ?** Deux rendus du même texte par la même voix doivent donner la même suite de sons. Le cache de synthèse neutralise en partie la question, mais une grille instable rendrait la mesure irreproductible.
3. **Quel son marquer ?** La mesure valide l'écart sur un son donné ; elle ne dit pas lequel mérite une marque, et le pire de l'énoncé n'est pas la réponse. C'est la brique 11 d'abord, un seuil ensuite.

La brique 10 ne dépend de rien et pourrait exister avant tout le reste.
