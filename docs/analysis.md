# L'analyse

L'analyse tourne **sur l'appareil**, et c'est la colonne vertébrale de l'app. Ce document la décrit brique par brique : ce qu'elle lit, ce qu'elle rend, ce qui est mesuré et ce qui reste à écrire.

## Les deux principes

Toute la forme de la chaîne découle des deux décisions de `reference.md` : **le modèle est la source de vérité et on le croit aveuglément** — rien d'extérieur aux deux enregistrements n'est jamais consulté — et **on ne compare jamais deux notes, mais deux formes**, la répartition entière de la ressemblance à chaque instant plutôt que son maximum.

Le second a une conséquence propre à la chaîne, née d'une objection. Tant qu'on comparait deux pics, le pic du modèle était le maximum de sa propre ligne — la grille étant tirée de cette ligne, la « mesure de référence » n'était qu'une relecture de ce maximum, et l'écart se réduisait à la valeur de l'apprenant. Le nombre n'est pas vide pour autant : il porte le **plafond local** — un son que le réseau distingue mal pique bas même sur une synthèse parfaite, et une syllabe floue pique bas parce que la voix était floue. Mais c'est une échelle, pas un terme de comparaison.

## Ce qui entre, ce qui sort

Entrent : l'audio du modèle, l'audio de l'apprenant, et le texte de ce qui a été dit.

Sortent les trois marques du projet — le son, le mot, la phrase — ancrées à des lettres précises du texte affiché.

Le texte ne juge rien. Il sert à deux choses seulement : afficher, et séparer les mots.

## Les briques

### 1. Le modèle — la synthèse

Le texte part au fournisseur de synthèse, qui rend un audio. C'est tout ce qui lui est demandé, et c'est ce que rend n'importe quel moteur de synthèse : un wav, sans métadonnée, sans horodatage, sans format propriétaire.

Cet audio sert trois fois — d'étalon pour la mesure, de modèle à écouter, de modèle à réécouter — et il est mis en cache, indexé par le texte, la voix et le dialecte.

### 2. La matrice — une passe par audio

Un modèle acoustique phonémique (`wav2vec2` affiné en reconnaissance de phonèmes par un entraînement à la seule suite des sons, sans jamais lui dire à quel instant chacun tombe — c'est ce qu'on appelle CTC) transforme un enregistrement en un tableau : toutes les 20 ms, la répartition de la ressemblance sur les quelque 45 sons de l'anglais, plus un symbole `∅` qui signifie « rien ne se prononce ici ». Chaque ligne somme à 1.

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

La brique 3 dit quels sons le modèle a produits, dans l'ordre. Le texte dit quels mots il devait dire, dans l'ordre. La jointure les apparie — et n'ouvre aucune horloge pour le faire.

```
The sheep is in the field

mots      The   |  sheep        |  is    |  in    |  the   |  field
sons      ð  ɪ  |  ʃ   i   p    |  ɪ  z  |  ɪ  n  |  ð  ɪ  |  f  i   l  d
lettres   Th e  |  sh  ee  p    |  i  s  |  i  n  |  th e  |  f  ie  l  d
```

C'est ainsi qu'une marque atterrit sur une lettre précise. Trois choses la décident, dans cet ordre :

- **Les mots d'abord.** La suite de sons est coupée en autant de groupes contigus que le texte a de mots, et une lettre ne peut atteindre que les sons du sien. La coupe retenue est celle qui épelle le mieux la phrase entière. Un mot peut recevoir zéro son — un mot outil escamoté est réel, et lui en forcer un le volerait à son voisin.
- **L'ordre.** L'appariement ne recule jamais, ni entre les mots ni à l'intérieur d'un mot.
- **L'orthographe.** Une table d'affinité dit à quels sons une lettre **participe** — `s` participe à /ʃ/ par `sh` —, pondérée de 0 à 3. Et un son laissé sans aucune lettre coûte : sans ce prix, l'appariement ne paie que les lettres, donc rien n'empêche une voyelle de happer les lettres de sa voisine et de la laisser vide. Un son sans lettre est un son sur lequel aucune marque ne peut se poser, c'est exactement le défaut à tarifer.

**Cette table n'est pas la table graphème-phonème que le principe 1 refuse.** Le refus protège le jugement : rien d'extérieur ne doit dire ce qui est correct. La brique 4 ne juge rien, elle décide où peindre. La table ne dit jamais comment un mot se prononce, ne porte ni liste de mots ni lexique de dialecte, et répond seulement « telle lettre participe-t-elle à tel son ». Elle vaut quelques kilo-octets et se refuse à se brancher sur un modèle dont elle ne nomme pas l'alphabet.

**Mesuré** (`bench/join.py -s`, contre l'annotation à la main d'`expected.py`) : **231 sons sur 236 portent les lettres qu'un humain leur attribue, soit 98 %**, sur les quinze phrases de calibration. Et **311 sur 326, soit 95 %**, sur les vingt phrases tenues à l'écart, qui n'ont réglé ni table ni prix — c'est le seul des deux chiffres que rien n'a ajusté.

Les deux irrégularités connues restent : une lettre peut porter deux sons (elle prend la couleur du pire), une lettre peut n'en porter aucun (elle reste neutre).

**Cette brique n'a jamais besoin de savoir *quand*.** Un second réseau, à sortie caractères, a été construit puis retiré : il rendait la même jointure à un son près, pour 191 Mo de poids et une passe de plus.

### 5. L'apprenant sur la grille — alignement forcé

Dans la matrice de l'apprenant, on cherche le meilleur chemin qui respecte la suite de la grille.

```
AO 0.19-0.27 s    R 0.27-0.29 s
```

Le même symbole désigne maintenant le même endroit chez les deux. C'est la seule chose dont on avait besoin pour comparer, et c'est ce qui remplace toute tentative de coller les deux audios l'un sur l'autre : la correspondance passe par un repère symbolique commun, pas par la ressemblance des signaux — donc la différence de voix n'y entre pas.

**Mesuré contre une vérité terrain** (`bench/boundaries.py`, 200 énoncés de la partition de test de TIMIT, 5971 sons, bornes annotées au niveau de l'échantillon) : le départ d'un son tombe à **25 ms** de sa borne réelle en médiane, 71 ms au 9e décile — la trame de 20 ms posant le plancher. Le treillis n'échoue jamais : aucun son sans place, aucun énoncé refusé.

**Mais la durée n'est pas rendue : un son reçoit 26,9 % de son étendue réelle.** C'est le prix de l'entraînement à la seule suite des sons (brique 2) : le réseau n'est payé que pour rendre la bonne *suite*, jamais pour couvrir le temps, donc le `∅` absorbe presque toutes les trames et chaque symbole ne surnage qu'en pic. Elle ne dépend pas des poids : quatre lectures, dont trois états d'un affinage complet sur un autre encodeur, tiennent dans 0,2 point. **C'est donc une propriété du régime, pas un défaut des poids retenus.**

**Et elle ne coûte rien, parce que l'étendue d'un son n'a aucun consommateur.** La jointure n'ouvre pas d'horloge (brique 4) ; l'accent et la mélodie lisent des durées de **syllabes**, qui se prennent entre deux débuts de sons — une soustraction de deux positions, jamais une étendue. La densité a longtemps été tenue pour le grand mal de ce montage ; elle l'était d'un montage qui n'existe plus, celui où un second réseau à sortie caractères devait être marié à celui-ci dans le temps.

**Cette soustraction se mesure à part, et elle ne se déduisait pas de la position** (mêmes 200 énoncés, 2133 syllabes, l'intervalle pris entre deux débuts de voyelle et jamais par-dessus une pause) : la durée d'une syllabe tombe à **29,1 ms** de la vraie en médiane, 71 ms au 9e décile, soit **14,7 % de sa propre durée** — 44 % au 9e décile. L'erreur sur l'intervalle est donc **plus grande** que sur un début, quand elle aurait été plus petite si le réseau glissait en bloc : les deux départs se trompent chacun pour son compte, et rien ne s'annule à la soustraction.

Ce que ce chiffre ne dit pas est ce qui déciderait : de combien deux syllabes d'un même mot diffèrent en durée quand l'accent tombe sur l'une plutôt que sur l'autre. Tant que cet écart n'est pas mesuré, 14,7 % ne se compare à rien.

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
- **Le son produit est nommé.** Là où la masse de l'apprenant s'est déplacée, c'est ce qu'il a dit. C'est l'enrichissement que le contrat de `reference.md` classe en confort et qu'aucun service mesuré ne rendait correctement.

### 7. Le mot — l'accent

**Ce qui se mesure : le noyau.** L'accent est une propriété de la **syllabe** — c'est la formulation linguistiquement correcte, et « voyelle accentuée » n'en est pas une. Mais sa réalisation acoustique est concentrée sur le noyau, qui en anglais est une voyelle dans la quasi-totalité des syllabes accentuées. Mesurer sur la voyelle est la pratique standard, pas un raccourci.

Quatre traits, mesurés un à un dans l'absolu sur TIMIT. Bien lire ce que ces chiffres valent : ils disent ce que chaque trait vaut pour **désigner** la syllabe forte d'un seul côté — or la brique ne désigne plus (voir plus bas). Leur seul usage restant est de classer les **dimensions du profil** que la brique compare au modèle, du plus porteur au plus faible :

- **La qualité de la voyelle** — le trait premier **quand il tranche**. En anglais une syllabe non accentuée se réduit en schwa, et il est déjà là : c'est la grille elle-même qui l'écrit. Mesuré sur TIMIT : quand un seul noyau du mot porte une voyelle pleine, la syllabe forte est celle-là dans **97,5 %** des mots. Mais l'alphabet du modèle ne sépare pas le `ɪ` réduit de `roses` du `ɪ` plein de `sink`, donc ce cas ne couvre que **15 %** des mots — 45 % avec l'inventaire complet de TIMIT, qui les distingue. Le repli de notre propre affinage fusionne en plus `ə` avec `ʌ` et `ɝ` avec `ɚ`, ce qui fait tomber la justesse à 59,9 % : c'est une propriété de l'alphabet visé, qu'aucun entraînement ne rattrape.
- **L'intensité du noyau** — ce qui tranche partout ailleurs, et une mesure l'a mise là. Sur 2 149 mots elle désigne la bonne syllabe dans **81,7 %** des cas contre 64,8 % à la durée, et **79,4 %** contre 60,0 % sur les seuls mots que la réduction ne tranche pas. L'objection évidente — une voyelle ouverte est plus forte qu'une fermée, accent ou non — ne tient pas : sur les mots dont les deux syllabes en concurrence portent la même voyelle, où la loudeur intrinsèque s'annule exactement, elle reste à **84,3 %** contre 65,3 %.
- **La durée du noyau** — en corroboration seulement, et c'est une mesure qui l'y a rangée. Sur les 3 601 mots pleins de deux syllabes ou plus du jeu de test de TIMIT, bornes posées à la main, la syllabe accentuée vaut 1,39 fois la moyenne de ses voisines mais n'est **la plus longue que dans 65,0 % des mots** (`bench/stress.py`). Une règle qui désignerait la plus longue se tromperait donc une fois sur trois. Ce chiffre ne doit rien au réseau — il est lu sur la vérité terrain —, donc la comparaison prévue à l'erreur de l'instrument n'a plus lieu d'être : la durée échoue avant que l'instrument entre en jeu. Normalisée dans le mot quand elle sert, faute de quoi un locuteur lent serait marqué partout.
- **Le pic de hauteur** — écarté. Une syllabe accentuée ne reçoit un pic que si le **mot** est mis en relief dans la phrase : dans « that's an important thing » dit vite, l'accent lexical est toujours sur `por` mais il n'y a plus de pic à voir. Peser dessus reviendrait à mesurer le relief de phrase en croyant mesurer l'accent du mot.

Comme tout se compare au modèle et non à une norme, un mot sans relief des deux côtés reste comparable : les deux manquent de pic ensemble.

**Combien de syllabes, et laquelle : deux sources, jamais la même.** Le **nombre** est une propriété du mot et se prend hors de l'audio ; **laquelle** porte l'accent se prend dans l'audio, des deux côtés. Le partage n'est pas une commodité : compter dans l'audio expose la mesure à ce que l'audio escamote, et lire la place dans un lexique ferait juger par une norme extérieure ce que la conception fait juger par le modèle.

D'où la règle du mot escamoté : quand la grille rend moins de noyaux que le mot n'en porte, **le noyau qui survit est l'accentué**. Elle vaut que la réduction soit dans l'audio du modèle ou dans notre seule lecture de lui — ce qui fait disparaître une voyelle est sa faiblesse, et sa faiblesse est l'absence d'accent (brique 8). Un mot que la grille ramène à une syllabe reste donc jugeable : `walking` dit `walKING` est une faute d'accent, nommable comme telle.

Non éprouvé chez nous : que la syllabe résistant à la réduction soit l'accentuée est solide en linguistique et n'a jamais été vérifié sur nos données (cf. `../TODO.md`).

**La brique compare des profils, elle ne désigne jamais une syllabe de chaque côté.** La première forme essayée désignait : chaque côté élit sa syllabe forte par la meilleure règle (84,1 % de justesse dans l'absolu), et la marque naît quand les deux élections diffèrent. Cette forme est condamnée par l'arithmétique : deux désignateurs justes à 84 %, lisant deux voix différentes, ne coïncident que dans environ 84 % × 84 % ≈ 71 % des cas — et le désaccord mesuré sur les mots correctement accentués du corpus L2 est bien de **28 à 36 %** (`bench/accent.py`), autant de marques fausses sur de la parole sans faute. Tenir 5 % de fausses marques par ce chemin exigerait ~97,5 % de justesse par côté, hors d'atteinte par accumulation d'indices. La forme retenue est celle du reste de l'analyse (« comparer des formes, pas des maximums ») : chaque côté rend un **profil** sur les syllabes du mot — masse de réduction, intensité relative, durée relative des noyaux — et la marque naît de la divergence des deux profils, par la brique 3. **Renversé le 2026-09-05.** La condamnation tenait toute entière à la justesse des désignateurs, 84 % chacun ; la sonde entraînée désigne à **96,7 %**, et le montage à deux élues a depuis été mesuré directement sur le montage du produit — 6 fausses marques sur 100 lectures, 3 avec la barre d'éligibilité — contre les 28 à 36 % qui avaient tué la forme. Le problème a changé, un instrument entraîné a remplacé des règles à la main, donc la condamnation tombe avec lui : la marque en service compare bien deux élues, sous la barre dont une oreille a validé la marge (ci-dessous).

**Les trois dimensions sont mesurées, et aucune ne porte l'accent.** La **masse de réduction** — celle que les mesures classent première quand elle parle — voit **6 fautes sur 70 à 5 % de fausse alerte**, entre la durée (3) et l'intensité (14). Ce qui reste de cet essai n'est pas la dimension mais deux acquis de montage. Lue comme une **masse** et non comme une étiquette, la réduction parle sur tous les mots — 8 muets sur 2 489, contre 15 % de mots tranchés par l'étiquette binaire. Et le **rattachement par les lettres** supprime le silence : le modèle compte les syllabes, chaque syllabe de l'apprenant rejoint celle du modèle avec qui elle partage le plus de lettres, et une syllabe insérée retombe dans celle où elle a été insérée au lieu de décaler le mot entier. Le montage lit alors 2 489 mots au lieu de 1 883, soit **606 mots que le compte discordant faisait jeter** — ce qui vaut pour toute forme à venir, la mesure 4 comprise.

**Les deux autres dimensions, et le détail de leur échec** (2026-08-31, `bench/accent.py -b 0 -m aligné`, tout le jeu `test` : 2 591 mots pleins, 70 fautes d'accent). Seuil posé sur les mots à l'accent correct, à 5 % de fausse alerte : la **durée** voit 3 fautes sur 70, l'**intensité** 13, les deux ensemble 10. Un mot correct diverge plus que la pire des fautes, et six fautes divergent à 0,000. Ce ne sont pas des dimensions d'appoint faibles, ce sont des dimensions muettes. Ce qui reste ouvert à la brique n'est donc plus une dimension à ajouter au profil — les trois y sont passées — mais le **montage à hypothèses concurrentes** (`../TODO.md`, mesure 4), qui ne compare pas des profils du tout.

Une erreur de diagnostic corrigée, à ne pas reprendre : il a d'abord été écrit que « le montage aligné prive la réduction de tout pouvoir », l'apprenant forcé sur la suite de sons du modèle ayant des plages mais pas de symboles. C'est vrai du test de réduction **par étiquettes**, et faux de la matrice : l'alignement force le **chemin**, pas les **répartitions** — sous chaque plage de noyau, les trames de l'apprenant portent toujours sa propre masse de schwa ou de voyelle pleine, lisible sans symbole. La réduction se mesure donc sur le montage aligné, comme masse et non comme étiquette. Le montage à deux décodages libres, lui, est abandonné pour cette brique : il faisait tourner la jointure sur la voix de l'apprenant, où elle n'a jamais été mesurée (`like` y reçoit `ɝ n l aɪ k h`, dont le début appartient au mot d'avant et la fin au suivant).

**Et le montage à hypothèses concurrentes est muet lui aussi** (2026-08-31, `bench/accent.py -b 0 -m concurrent`, tout le jeu `test` : 2 504 mots pleins, 74 fautes). Pour un mot à n noyaux, n motifs d'accent sont bâtis sur les sons du modèle — un noyau plein, les autres réduits en classes — et notés de chaque côté par le treillis de l'alignement, une case valant une classe au lieu d'un phone. La marque naît de l'écart entre les deux notations, jamais de l'égalité des deux gagnants : la **marge** — de combien la matrice de l'apprenant préfère un autre motif à celui que le modèle désigne — reste continue, donc un quasi ex æquo rend une petite marge au lieu de tirer à pile ou face. À 5 % de fausse alerte, elle voit **4 fautes sur 74**, la marge symétrique 4, la divergence des deux répartitions 5. Les deux gagnants, eux, désaccordent sur **31,1 %** des mots corrects, ce qui repose les 28–36 % de la forme condamnée sur exactement ces mots-là.

Y faire entrer l'intensité et la durée — chaque noyau apportant le log de sa part du mot, donc sans échelle à ajuster, et le poids balayé plutôt que choisi — double le résultat et pas davantage : **11 fautes sur 74** au mieux, toujours sous le profil d'intensité seul (13/70). Les trois dimensions sont donc passées dans les deux formes. La brique 7 n'a plus de montage en attente.

**La cause est l'alphabet, et elle était écrite à l'envers** (2026-08-31). Le montage note des suites de sons, donc il ne lit que la réduction — et le modèle en service ne peut pas la lire. `timit-ipa` écrit 42 colonnes, la même liste au banc et dans l'APK, où **`ʌ`, `ɚ` et `ɔ` sont absents** : son propre repli d'entraînement envoie `ah` et `ax` sur `ə`, `axr` et `er` sur `ɝ`. Le modèle qui tourne **est** le repli que `../TODO.md` tenait pour disqualifiant, et non un alphabet qui garderait les paires séparées comme `bench/stress.py` l'affirmait. La règle réduction-puis-intensité tombe alors à **79,4 %** de désignation au lieu des 85,1 % lus jusqu'ici, et les 63,3 % du montage sur la vérité de TIMIT sont le plafond de ce qu'on lui laisse lire, non un défaut du montage.

**Déplier l'alphabet ne rachète pas la brique** : la même règle sur l'inventaire complet de TIMIT fait **86,7 %**, soit 7,3 points de plus, dont 5,7 tiennent à la seule séparation `ə`/`ʌ` — distinguer les deux `ɪ`, présenté comme le manque décisif, en vaut 1,6. Sept points de désignation ne valent pas de remettre en jeu tout ce qui est mesuré sur le modèle acoustique, et la réduction ne tranche de toute façon que 477 mots sur 1 057 même avec l'inventaire complet ; sur les autres, c'est l'intensité qui porte. **Ce qui reste possible n'est plus un indice ni un montage, c'est un instrument entraîné** — un réseau rendant une probabilité d'accent par syllabe, à juger sur les mêmes bancs (`../TODO.md`).

**Un instrument entraîné double tout ce qui précède** (2026-09-01, `bench/accent.py -b 0 -m sonde`, même jeu : 2 504 mots, 76 fautes). Une régression logistique posée sur la **couche 19** du réseau déjà en service rend une probabilité d'accent par noyau ; chaque côté est lu sur son propre enregistrement, et la marque naît de l'écart des deux parts. À 5 % de fausse alerte elle voit **24 fautes sur 76**, contre 13/70 au meilleur profil et 11/74 aux hypothèses. Le désaccord des deux élues tombe de 31,1 % à **14,5 %**.

Sur TIMIT, où la vérité existe, la même sonde désigne la bonne syllabe dans **96,7 %** des mots — contre 86,7 % à la meilleure règle à la main et 79,4 % à ce que l'alphabet en service permet. Même la couche 0 fait 87,3 %. Ce n'est donc pas que les indices étaient mal choisis : des indices faits à la main ne valent pas une représentation apprise sur cette question, et l'affinage aux phonèmes n'a pas écrasé l'accent, ce qui était l'inconnue qui décidait.

**Ce que ça ne fait pas encore.** 24 sur 76 est 32 % des fautes, et à ce seuil 2 428 mots corrects donnent 121 marques fausses : la brique ne s'allume pas là-dessus. Mais la sonde est linéaire, entraînée sur 1 910 mots de parole **native** aux bornes posées à la main, et elle n'a **jamais vu une faute d'accent** ; elle est jugée sur de la parole d'apprenant aux bornes rendues par le réseau. Ce qui la sépare de l'état de l'art est exactement ce qui lui manque.

**Une oreille a jugé la sonde, et le corpus qui la condamnait n'était pas ce qu'on croyait** (2026-09-05, `bench/hear.py`). Tout ce qui précède est lu sur SpeechOcean762 — locuteurs de langue maternelle mandarin, moitié enfants, corpus abandonné — et non sur des natifs, ce que le texte laissait croire. La passe d'écoute produit la référence qui manquait : cinq phrases de TIMIT, chacune lue par deux natifs et rendue par deux voix de deux fournisseurs, chaque enregistrement lu seul, la syllabe forte demandée à l'aveugle avant que la sonde soit révélée. 99 mots jugés, dont 7 que l'oreille n'a pas su trancher.

- **La sonde désigne juste 87 mots sur 92** — 95,7 % sur les natifs, 93,5 % sur la synthèse. Pas d'écart machine/humain sur de la parole propre, contrairement à ce que SpeechOcean laissait lire.
- **Deux bouches accentuent la même syllabe dans 99,1 % des cas** (1 désaccord sur 117 paires). C'était l'inconnue qui décidait : les 18,9 % de désaccord de la sonde entre deux lectures d'un même mot venaient de l'instrument, pas de la parole. Il y a bien un fait à détecter.
- **Sur le montage du produit — l'apprenant aligné sur la grille du modèle — la brique peindrait une marque fausse sur 6 mots polysyllabiques sur 100** de parole native sans faute : 4 sur 50 avec `azure-us-jenny` pour modèle, **2 sur 50 avec `eleven-gb-daniel`**. La voix modèle vaut un facteur deux, d'où un critère de plus au test de voix.
- **L'alignement ne supprime aucune de ces marques.** Sur grille libre, 6 écarts sur 90 mots comparables ; alignés, les mêmes 6 sur 100. Ce qu'il apporte est de la couverture, pas de la justesse — les trois mots qui échouent (`increases`, `military`, `obey`) échouent avec les deux côtés lisant déjà les mêmes tranches de temps, donc l'erreur est dans ce que la sonde lit sur le noyau et non dans le placement du noyau.
- **La marque est binaire, pas graduée.** 86 % des lectures saturent au-dessus de 0,99, et sur les 90 paires natif-modèle la divergence sépare franchement : les 6 désaccords occupent le haut (0,505 à 0,773), la plus divergente des paires en accord est à 0,112. Il n'y a donc pas de « x % les plus marqués » à écrémer — un seuil posé dans ce trou ne fait que reproduire la comparaison des deux élues.
- **Écarter les mots que le modèle n'accentue pas nettement enlève une fausse marque sur deux, pas plus** (`bench/hear.py --couples`). Sur les mêmes 100 lectures, ne garder que celles dont la syllabe élue devance la suivante d'au moins 0,50 les garde toutes les 100 et laisse les 6 marques ; à 0,95 on jette 12 lectures et on en retire 3. Les trois qui survivent sont `obey` deux fois à une marge de 1,00 et `increases` à 0,99 — le modèle y est aussi sûr qu'il peut l'être.
- **Cette marge est pourtant ordonnée comme une oreille** (2026-09-05, dix clips joués à l'aveugle, jetés et gardés mêlés, les parts de la sonde révélées après la réponse). Les quatre mots gardés à 1,00 sont tous entendus « appui franc » ; des six jetés, trois s'entendent « aucun appui net », deux « appui audible », un seul « franc » — `miraculously` à 0,94, jeté de justesse. La marge n'est donc pas un artefact de saturation : elle trouve les mots dont l'appui ne s'entend pas. Ce qui s'en déduit et n'est pas mesuré : c'est la définition dont la feuille de note a besoin (`design/activity-model.md`), et non un filtre de fausses marques — les deux emplois ont été confondus ici même la première fois que le chiffre a été lu.
- **Et les trois marques que le seuil garde ont trois causes différentes** (2026-09-05, `bench/clarity.py --pairs`, les deux enregistrements de chacune joués à l'aveugle, modèle et natif mêlés, chacun jugé seul). Sur `obey` dit par `azure-us-jenny` contre le natif MREB0, aucun des deux enregistrements ne porte d'appui que l'oreille sache désigner — alors que la marge du modèle vaut 1,00. Sur le même mot dit par `eleven-gb-daniel`, les deux côtés appuient sur `bey` et la sonde lit `o` chez l'apprenant : une erreur franche, sans ambiguïté à invoquer. Sur `increases`, l'oreille désigne `crea` chez l'apprenant comme la sonde, et n'entend rien sur le modèle, où la sonde élit `in`. Donc une marge haute ne garantit pas un appui audible : des six enregistrements de modèle à marge ≥ 0,95 écoutés jusqu'ici, quatre portent un appui franc et deux n'en portent aucun. Les deux passes ne posent pas tout à fait la même question — « à quel point l'appui s'entend » contre « quelle syllabe le porte, ou aucune » — et leurs cases se recoupent sans se confondre.

- **Sur les 5 désaccords oreille/sonde, le dictionnaire donne raison à l'oreille 4 fois** et à la sonde une (`personnel`, où l'oreille a désigné l'accent secondaire, sur une découpe abîmée). Ce ne sont pas des mots au hasard : `increases` est le couple nom/verbe, et la sonde y choisit le motif du nom sur les deux voix.

**La suite est actée (2026-09-05), et les deux inconnues qui restent n'y décident plus rien.** La marque entre dans l'app sur le montage mesuré — les deux élues comparées, la barre d'éligibilité à 0,90 sur la marge côté modèle — et la feuille de note est actée avec elle (`design/activity-model.md`), jugée à l'usage. Ce qu'elles auraient décidé, c'est combien de marques seront justes sur un tour réel. La première est le **taux de faute d'accent d'un vrai apprenant** : à 4 % de fausse marque, la justesse des marques vaut 55 % si ce taux est de 5 % et 90 % s'il est de 28 %, et rien ne dit lequel. La seconde est le **régime d'imitation** : les 6 écarts sur 100 opposent des natifs qui n'ont jamais entendu le modèle, donc deux réalisations indépendantes. Quand l'apprenant redit après avoir entendu, il reproduit la réalisation du modèle, la sonde lit deux fois la même chose, et une erreur de sonde partagée s'annule sans marque — ce qui est le comportement voulu, même quand la sonde a tort. L'ampleur de cette compensation se mesure avec des calques (`bench/take.py`), jamais avec TIMIT. Un fait la borne sans la fermer : deux voix de synthèse disant le même texte avec le même accent voulu se désaccordent déjà 8,3 % du temps, donc le timbre seul déplace la sonde et la compensation ne peut pas être totale.

**L'alphabet coûte aussi au niveau du son, et personne ne l'avait écrit.** Puisque `ə` et `ʌ`, `ɚ` et `ɝ`, `ɔ` et `ɑ` partagent une colonne, une confusion entre ces voyelles-là **ne peut jamais être marquée**, quelle que soit la prise et quel que soit le seuil. C'est un angle mort de l'analyse en service, indépendant de l'accent tonique, et il se mesure au niveau du son plutôt qu'ici.

**Ce qui s'affiche : la syllabe.** La forme est tranchée et en service — graisse sur la syllabe que le modèle accentue, et sur une faute deux réglettes sous la ligne, rouge où l'accent est parti, verte où il devait tomber. Deux réglettes se comparent, deux points non ; et les trois canaux graphiques sont des étendues. Il faut donc une étendue syllabique exacte, pas approchée.

### 8. La syllabification — sur la grille, pas sur le texte

Elle se calcule par une règle phonotactique, sur des sons, sans aucun dictionnaire : entre deux voyelles, le groupe de consonnes reçoit la plus grande attaque possible qui reste prononçable en anglais, le reste retombant sur la syllabe précédente. La seule donnée est la liste des attaques légales de l'anglais, une cinquantaine de groupes.

```
IH M P AO R T AH N T
IH..AO : groupe M P -- "mp-" illégal, "p-" légal -> coupe après M
AO..AH : groupe R T -- "rt-" illégal, "t-" légal -> coupe après R
-> IH M / P AO R / T AH N T -> im / por / tant
```

**Les lettres d'une syllabe sont celles de ses sons**, réunies — la brique 4 les a déjà posées, et aucune horloge n'entre ici non plus. La syllabe se **compte** sur les sons et se **peint** sur les lettres.

**Écrite** : `bench/syllables.py`, fonction `cut` — la règle, plus la table des attaques légales (une cinquantaine de groupes, les consonnes seules, et l'attaque vide puisqu'une syllabe peut s'ouvrir sur une voyelle). Ce qui est noté est le **nombre** de syllabes : la coupe en rend exactement une par noyau, et les noyaux par mot sont comparés à l'annotation ci-dessous. **Où tombe la frontière n'est comparé à rien** — la règle ne consulte aucun audio, donc une coupe fausse serait une règle fausse et non une mauvaise lecture, et la prendre en défaut demanderait une annotation que personne n'a écrite.

**Mesuré** (`bench/syllables.py`, quinze phrases, voix `eleven-us-eric`) : **34 mots pleins sur 37 reçoivent le bon nombre de noyaux**, et 48 mots outils sur 49. Des trois désaccords, `chair` est une faute du compteur — `ɛ`+`ɝ` est un seul noyau, et c'est la grille qui a raison ; `comfortable` et `important` perdent chacun une syllabe finale non accentuée.

**Aucun noyau accentué n'est perdu sur tout le jeu.** La raison se dit : la voyelle qui disparaît est la voyelle **réduite**, et une voyelle réduite est par définition non accentuée. Ce qui s'en déduit et n'est pas mesuré : un mot dont la syllabe forte porterait une voyelle brève serait le cas à surveiller — il n'y en a pas dans ce jeu.

**Le dialecte se règle tout seul**, puisqu'on syllabe ce que le modèle a réellement prononcé :

```
market GB non rhotique : M AA K IH T   -> groupe K  -> M AA / K IH T  -> mar / ket
market US rhotique     : M AA R K IH T -> groupe R K -> M AA R / K IH T -> mar / ket
```

Deux grilles différentes, le même résultat à l'écran, aucun lexique consulté. C'est ce qui écarte définitivement la voie du dictionnaire : CMUdict ne porte pas de frontières de syllabes (seulement des marques d'accent, qui permettent de compter et non de placer) et il est américain seul ; les lexiques britanniques existent (BEEP, Unisyn, CUBE) avec des licences hétérogènes et des couvertures inégales. Deux fichiers, deux couvertures, deux jeux de mots absents — pour un résultat qu'une règle de cinquante lignes donne mieux, parce qu'elle lit l'audio au lieu de citer le mot isolé.

Réserve honnête : la syllabification phonologique ne coïncide pas toujours avec la coupure orthographique attendue. `happy` donne `HH AE / P IY`, donc `ha / ppy`, là où un tiret d'imprimeur écrirait `hap-py`. Le `p` appartient réellement aux deux — c'est une ambiguïté de l'anglais, et tout choix y est arbitraire. Si la graisse sur `ppy` heurte à la lecture, une règle de plus la corrige (une consonne doublée à l'écrit se partage) ; ça se juge sur du texte marqué, pas d'avance. Le repli, si le découpage produisait des étendues illisibles, est de marquer sur la voyelle seule — graisse sur la lettre du noyau, pastilles rouge et verte sur une faute.

### 9. Le découpage en mots

Nécessaire, parce que la comparaison des noyaux se fait **à l'intérieur d'un mot** : sans frontières, on comparerait la voyelle de `important` à celle de `the`.

Il tombe de la brique 4, qui partitionne déjà la suite de sons entre les mots : chaque son appartient à un mot et à un seul, donc chaque voyelle aussi. Il n'y a pas de voyelle à cheval, la partition étant sur les sons et non sur le temps.

Une règle à poser : un mot d'une seule voyelle n'a pas d'accent interne, donc ne porte **jamais** de marque d'accent.

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

Ce ne sont pas deux prononciations de la même chose. L'idée : si le texte a été mal reconstruit, le modèle synthétise autre chose que ce qui a été dit, l'alignement forcé plaque quand même la grille sur la voix de l'apprenant, et les deux suites divergent franchement — auquel cas on ne marque rien.

Rôle strictement à l'échelle de la phrase : nommer le son produit appartient à la brique 6.

**Deux mesures fragilisent cette brique, et elles ne sont pas de même poids.**

*Sa justification annonçait une rafale ; il n'y en a pas.* L'argument écrit ici était qu'un mot mal reconstruit « entraîne le suivant avec lui ». Mesuré sur le jeu d'essai, à texte égal et modèle égal, en excluant le mot en cause : une prise fautive laisse le reste de la phrase aussi propre qu'un témoin — `01-sink` à 0,002 contre 0,001 pour son témoin, `07-bear-pear` à 0,007 contre 0,018. **Une faute ne se propage pas.** Ce que ça ne mesure pas : le cas visé, un mot *réellement* mal transcrit, qu'aucune prise du jeu ne porte. La rafale n'est donc pas réfutée dans son cas propre — elle n'est plus étayée par les prises qu'on a.

*Le test ne distingue pas un texte faux d'un accent fort.* Sur `08-light-right`, écarté par le seuil du banc, les deux décodages diffèrent en sept endroits :

```
modèle    : t ɝ n   ɹ aɪ ɾ   ɪ   ð ɪ   k ɑ ɹ n ɝ
apprenant : t ʊ n   l aɪ t   ə   d ɪ   k ɝ n ə
```

Le `l` pour `ɹ` est la faute voulue. Les six autres sont des traits d'accent francophone — `d` pour `ð`, voyelles sans coloration r, `t` articulé là où le modèle bat. Le texte est correct ; c'est le locuteur qui s'écarte. Une prise acceptée (`01-sink`) diffère en quatre endroits sur onze : **une différence de degré, pas de nature.** Rien dans ce test ne sépare les deux causes, et la brique éteint alors l'analyse sur les apprenants les plus accentués — ceux à qui elle sert le plus. La lecture du corpus L2 va dans le même sens : des locuteurs y sont écartés en bloc (cf. `../TODO.md`), sans qu'on ait séparé les causes de ces rejets.

**Une piste, non éprouvée.** Marquer le mot suffit peut-être : un texte faux donne un mot très écarté, et l'écran affiche le texte transcrit — donc l'erreur est bornée à un mot et visible par qui la lit, là où éteindre tout le tour est muet. Ce qui resterait à contrôler serait alors autre chose : l'**alignement dégénéré**, que `../docs/reference.md` nomme déjà dans les exigences de l'analyse et qui se lit à des durées absurdes, sans rien inférer sur le texte. Rien de tout ça n'est écrit ni mesuré.

**Ce que le banc fait aujourd'hui n'est d'ailleurs pas cette brique** : `faults.py` écarte une prise sur l'**écart médian** de la phrase (seuil 0,20), pas sur la comparaison des deux décodages. C'est un raccourci, et la confusion entre les deux a déjà égaré une lecture.

### 12. La matière ajoutée — la jointure pointée vers l'apprenant

La grille est décodée du **modèle seul** et l'apprenant y est aligné de force : il y a autant de cases que le modèle a de sons, pas une de plus. Un son que l'apprenant ajoute n'est donc pas mal noté, il **n'est pas vu**.

Ce qui le trouve n'est pas une seconde comparaison au modèle, mais la lecture que le modèle reçoit déjà : la brique 4 prend une suite de sons et un texte, et dit quelles lettres chaque son écrit. Pointée vers le décodage libre de l'apprenant (brique 11), elle place l'apprenant. **Un son qu'aucune lettre d'aucun mot ne sait écrire, à sa place dans l'ordre, n'appartient à aucun mot** — et c'est toute la détection.

Les lettres portent alors les deux lectures à la fois, ce qui les fait se regarder **sans horloge** : le `ɝ` contracté du modèle couvre `ou're` d'un seul son, et l'apprenant qui ne contracte pas pose un `ʊ` sur le `o` et un `ɹ` sur le `r`, dans le même mot. Une prononciation plus pleine reste donc *dans* le mot, où les marques de son parlent déjà pour elle.

**Aucune durée n'est lue, et ce n'est pas une économie.** Mesuré : le décodage libre rend des étiquettes et des positions de pic, jamais des durées ; l'alignement forcé répartit toutes les trames entre les sons du modèle et n'a aucune case où loger un son ajouté. Aucun des deux ne dit combien de temps la matière ajoutée a duré, et les combiner ne l'invente pas — trois dénominateurs de temps ont été mesurés faux avant qu'on renonce à en chercher un quatrième.

La marque est **binaire, structurellement** : la matière ajoutée n'a pas de côté modèle, donc aucune répartition à comparer. Elle se pose **entre deux lettres**, comme la gouttière, et une rafale de sons voisins hors mot fait **une** marque — un mot entier dit en plus est une seule chose qui s'est produite.

**Où elle se pose est réglé par l'ordre, et par rien d'autre.** Une marque tombe strictement entre la dernière lettre réclamée avant elle et la première réclamée après elle, donc elle ne peut jamais se dessiner à côté d'une lettre dite de l'autre côté d'elle. Dans cette fenêtre elle va le plus tard possible, et le test est unique — le son placé qui suit est-il dans le même mot ?

- **Autre mot** : la rafale était entre deux mots, la marque va à la **fin du mot précédent**. C'est le cas ordinaire de la matière ajoutée, et c'est ce qui empêche un chevron de couper `ng` en deux.
- **Même mot** : la rafale était *dans* le mot, la marque reste sur la **dernière lettre réclamée**. Il n'y a pas de frontière où la pousser — l'y pousser la mettrait après des lettres dites avant elle.

Le second cas n'est pas hypothétique : la jointure vide un son *après* sa marche, quand les lettres qu'il avait reçues ne valent rien sur lui, et un son qui ne peut en emprunter aucune à un voisin est alors hors mot au milieu de son mot. Mesuré : **quatre des 95 rendus du banc** écrivent un mot ainsi.

Deux règles portent cet ordre, et elles se contrôlent au lieu de se supposer. Dans un mot, la marche ne passe **que d'un son à son voisin** — sauter un son au milieu reviendrait à dire que le locuteur a interrompu le mot pour dire autre chose puis l'a repris, ce dont rien ici n'a jamais la preuve ; le saut reste possible aux deux bords, où il dit seulement que le mot a commencé ou cessé d'être écrit. Et la fenêtre ci-dessus est **vérifiée à la fabrication de chaque marque**. Sans la première règle, mesuré sur un tour réel : le `aʊ` d'un `how` ajouté prenait le `g` de `trying` par-dessus le `ŋ`, et le `h` de la même syllabe se retrouvait de l'autre côté. La règle **ne coûte rien** contre l'annotation à la main — 231 sons sur 236 portent les bonnes lettres avec ou sans elle, aux mêmes cinq erreurs près.

**Mesuré** (2026-08-30, `bench/placed.py`, `timit-ipa`, voix `eleven-us-eric`) :

- **La jointure survit à la suite bruitée de l'apprenant** : 250 sons sur 267 trouvent une lettre sur les dix-sept prises du jeu étiqueté, **94 %**, contre 95 % pour la même brique côté modèle. C'était la crainte qui pouvait tuer la piste d'emblée.
- Sur `I think you're right` dit cinq fois : `very` ajouté **sort** des mots, un `you're` dit sans contraction **reste dedans**, un `I'm` mis pour `you're` laisse son `m` dehors. `I`, `think` et `right` sont propres sur les cinq.
- Les trois prises `comfortable` posent leur syllabe insérée **dans** le mot, et rien ne sort. **Trois des cinq témoins ne lèvent plus rien du tout.**
- Environ **une marque par prise** — dix-sept sons hors mot sur dix-sept prises. Le contrôle d'ordre passe sur **110 lectures** (15 prises d'apprenant, 95 rendus, 36 marques) sans une violation.

**Ce qu'elle rate, et qui est accepté.** Une substitution dont aucune lettre du mot ne sait écrire le son **sort du mot** et se lit comme de la matière ajoutée : `have` dit `h æ f`, la lettre `v` n'écrivant pas `f` ; le `s` de `I sink` pour `I think`. Le mot porte une marque de toute façon et l'analyse détaillée nomme le son en cause, donc la faute est vue — elle est seulement nommée deux fois. Et un « hmm » qui se décode en un `ə` se glisse dans une lettre qui sait écrire un `ə` ; quand il se décode en rien, aucune règle ne peut l'inventer.

**Deux bornes de lecture**, qui valent pour tous les chiffres ci-dessus. Les étiquettes du corpus L2 notent **chaque mot**, propre ou fautif ; elles ne disent rien de ce qui se passe **entre** deux mots, donc elles ne peuvent ni confirmer ni infirmer une de ces marques. Et marquer une hésitation est le comportement **voulu** — tout écart au modèle se marque —, donc une marque entre deux mots bien dits n'est pas une erreur par défaut. Aucune vérité terrain ne dit quels sons une prise a ajoutés : ce qui est compté ici est ce que le décodage revendique, jamais une faute confirmée.

### 13. Le runtime Android

**ONNX Runtime** (MIT) pour faire tourner le réseau sur le processeur — le CPU est déterministe, le GPU ne l'est pas toujours, et le déterminisme est un critère de qualification. **sherpa-onnx** (Apache-2.0) empaquette déjà pour Android le décodage libre et l'alignement forcé : à regarder avant d'écrire la plomberie soi-même.

Poids sous licence libre uniquement — un poids non libre serait un `NonFreeAssets` et tuerait l'intérêt de la brique. Téléchargement en opt-in explicite au premier usage, jamais au premier lancement ni en silence.

**Le changement de moteur d'exécution, lui, est mesuré et ne coûte rien.** Le réseau exporté en ONNX et relu par ONNX Runtime rend, en flottant, la lecture du banc à l'identique : pire cellule à 1,6 pour dix mille sur trente-deux fichiers, grille identique partout, et l'écart dont le marquage est fait qui bouge de 0,008 au pire. Le jeu d'essai étiqueté rejoue **ligne pour ligne**.

**Et le réseau tourne sur un téléphone.** Mesuré sur un appareil de 2019 (Galaxy S10+, huit cœurs, quatre fils de calcul), sur l'audio du banc :

| | flottant | entiers 8 bits |
|---|---|---|
| chargement des poids | 3,3 s | **0,8 s** |
| empreinte après chargement | 1263 Mo | **795 Mo** |
| empreinte maximale | 1563 Mo | **930 Mo** |
| passe sur 6 s d'audio | 4,2 s | **2,4 s** |
| coût rapporté à la durée du tour | ×0,70 | **×0,39** |

Le 8 bits est donc à la fois plus léger et **plus rapide** — l'arrondi n'achète pas du temps contre de la place, il achète les deux. La mémoire tient sans effort, et deux prises du même fichier rendent les mêmes octets : le déterminisme, qui est un critère de qualification, est vérifié et non supposé.

**Ce que coûte un tour, et non une passe.** Le montage en demande deux — l'apprenant et le modèle — mais celle du modèle est mise en cache avec la synthèse, donc elle ne se paie qu'à la première rencontre d'une phrase. Pour un tour de six secondes : environ **2,4 s** sur une phrase déjà entendue, **3 s** sur une phrase neuve, plus 0,85 s de chargement une fois par session. À mettre en face des 2,6 s de la chaîne de conversation jusqu'au premier son (`conversation-chain.md`) : l'analyse ne bloquant jamais la conversation, elle tourne pendant que l'IA répond et disparaît en pratique.

**Ce que coûte un tour long est mesuré, et ce qui croît est la mémoire plus que le temps.** Sur des prises du banc mises bout à bout :

| durée du tour | passe | rapport | empreinte |
|---|---|---|---|
| 3 s | 0,96 s | ×0,32 | 864 Mo |
| 6 s | 2,26 s | ×0,38 | 926 Mo |
| 12 s | 5,17 s | ×0,43 | 1057 Mo |
| 20 s | 9,9 s | ×0,50 | 1269 Mo |
| 30 s | 17,4 s | ×0,58 | 1704 Mo |
| 45 s | 31,3 s | ×0,69 | 2422 Mo |
| 60 s | 51,5 s | ×0,86 | **4316 Mo** |

Le temps croît comme la longueur puissance 1,3 environ. Mais au-dessus des 805 Mo qu'occupent les poids, le surcoût passe de 60 Mo à 3,5 Go, soit à peu près le **carré** de la longueur : c'est l'attention. Un tour d'une minute réclame 4,3 Go sur un appareil qui en a huit, et plante sur un appareil qui en a quatre.

Ce que le chiffre engage n'est pas tranché ici (cf. `../TODO.md`, chantier 1) : découper la passe en fenêtres, borner franchement la durée analysable en le disant, ou tenir que le tour d'une minute n'est pas un cas à servir sont trois issues, et rien dans la mesure ne les départage.

**Ce qui se raccourcit sans rien risquer, en revanche, c'est l'audio lui-même.** Retirer les plages vides — de bord comme intérieures — abrège la passe et réduit son empreinte plus que proportionnellement, et le gain est net : un tour de 30 s dont 12 s de pauses tombe à 18 s, soit environ 1200 Mo au lieu de 1704.

**La sûreté de la coupe tient au seuil de durée, pas à la marge.** Une occlusive *est* du silence — une fermeture muette suivie d'une explosion — et le jeu d'essai contient précisément cette faute (`07-bear-pear`, /p/ voisé en /b/, dont l'indice est le voisement pendant la fermeture). Mais une fermeture dure 50 à 120 ms : ne retirer que les plages franchement plus longues, de l'ordre de la demi-seconde, place la coupe hors du domaine où vivent les phonèmes. Une marge de part et d'autre s'ajoute par prudence ; elle ne fonde rien.

Les deux briques que la coupe pourrait toucher ne le sont pas. La mélodie (brique 10) ne lit que la région voisée **finale**, qu'aucune coupe interne n'atteint. L'alignement (brique 5) y gagne : moins de trames vides à traverser, moins d'occasions pour la grille de dériver. Reste le rejeu de l'extrait, dont le temps ne retombe plus sur celui de l'enregistrement — une correspondance par morceaux, et peut-être même pas nécessaire, puisque faire réentendre un mot sans les hésitations qui l'entouraient n'est pas une perte.

**Le point ouvert est le seuil de niveau**, pas le principe. Une plage vide dans un vrai enregistrement n'est pas du silence numérique : il y a du souffle et du bruit de pièce, et sur un locuteur qui parle bas un seuil mal posé mange de la parole. Ça ne se calibre pas sur le matériel du banc, qui est propre et sans hésitation — il y faut des tours spontanés enregistrés pour ça.

Un angle mort demeure : l'arithmétique au-dessus de la matrice — grille, alignement, recouvrement — n'est pas mesurée sur l'appareil, faute d'y être écrite ; au poste elle est négligeable devant la passe réseau.

**L'appareil ne rend pas les mêmes chiffres que le poste, et on sait exactement pourquoi.** En flottant, les deux sont la même lecture — pire cellule 5,6 pour cent mille, grille identique 32 fois sur 32 — ce qui disculpe d'un seul coup la préparation du signal réécrite en Kotlin, la lecture du wav, et l'arithmétique flottante d'ARM. Il ne reste que les **noyaux 8 bits**, qui ne sont pas le même code sur ARM et sur x86 : 0,74 % des trames y changent de son gagnant, toujours par bascule entre deux quasi-ex æquo, et un quart des trames s'écartent au-delà du millième.

**Rien de cela n'atteint le verdict.** Le jeu d'essai étiqueté rejoué sur les matrices de l'appareil donne pire témoin 0,004 contre 0,003, les mêmes fautes vues, la même manquée. C'est la mesure qui compte, et elle est passée.

**Mais l'accord n'est pas uniforme, et c'est là que ça engage la suite.** Il est excellent aux extrêmes — témoins à 0,001 près, fautes franches à 0,03 près — et il lâche exactement dans la zone grise :

| prise | poste | appareil |
|---|---|---|
| témoins (cinq) | 0,000 à 0,003 | 0,000 à 0,004 |
| fautes franches (cinq) | 0,93 à 0,98 | 0,93 à 0,98 |
| `01-sink`, /θ/ à mi-chemin | 0,129 | **0,235** |
| `18-walkin-full` | 0,214 | **0,254** |

L'appareil lit les demi-fautes **plus fort**, donc dans le sens favorable — mais l'écart atteint 0,1 en absolu, sur les seules valeurs qui décident quoi que ce soit. La règle du seuil s'en trouve durcie d'un cran : il ne suffit pas de le calibrer sur **les poids** qui tournent, il faut le calibrer sur **la lecture de l'appareil**. Un seuil posé à 0,15 ne classe pas `01-sink` du même côté selon la machine qui a produit le chiffre.

Portée de tout ceci, à ne pas surestimer : une architecture, un appareil, huit fautes étiquetées et cinq témoins. De quoi dire que le PoC passe, pas que l'accord est acquis sur le parc Android.

Le graphe exporté **normalise ses sorties à l'intérieur** : le fichier rend la matrice de la brique 2 — des parts qui somment à 1 — et non les scores bruts du réseau, et il ne reste au côté Android qu'une chose à réimplémenter — la préparation du signal, moyenne nulle et variance unité, que `bench/matrix.py` écrit en clair pour cette raison.

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
| 1 | Le modèle (la synthèse) | l'audio de ce qui aurait dû être dit | existe, tout TTS convient |
| 2 | La matrice | répartition sur les sons, toutes les 20 ms | codée (`bench/matrix.py`) |
| 3 | La grille | sons réellement produits par le modèle → temps | codée |
| 4 | La jointure lettres ↔ sons | quelle lettre porte quel son | codée au banc (`bench/join.py`), 94 % contre l'annotation |
| 5 | L'apprenant sur la grille | mêmes sons → temps chez lui | codée |
| 6 | Le son | recouvrement des deux formes, et le son produit | recouvrement codé (`bench/overlap.py`) ; le son produit à écrire |
| 7 | Le mot | quelle syllabe est la forte | à écrire |
| 8 | La syllabification | l'étendue en lettres de chaque syllabe | à écrire |
| 9 | Le découpage en mots | quelles voyelles appartiennent au même mot | tombe de la 4 |
| 10 | La phrase | la pente de fin d'énoncé | codée |
| 11 | Le contrôle | texte de référence faux | **à revoir** — prémisse non étayée, accent fort et texte faux non séparés |
| 12 | Le runtime Android | tout ça sur le téléphone | **mesuré sur l'appareil** (`bench/export.py`, `bench/phone.py`) |

**Deux fichiers extérieurs dans toute la chaîne** (le principe est dans `reference.md`) : les poids du modèle acoustique — 359 Mo une fois quantifiés en entiers 8 bits sur le périmètre qui préserve la lecture, contre 1,26 Go en flottant — et la table d'affinité de la brique 4, quelques kilo-octets.

Trois briques seulement demandent du travail neuf et non trivial : la jointure (4), l'accent (7), la syllabification (8). Quatre autres sont des lectures d'un calcul déjà fait.

## Les cas limites

Ce qui produirait un comportement inattendu, et où ça se traite.

### Le modèle escamote, lie, ou reste mou

- **La voix avale un son.** La grille ne le contient pas ; l'apprenant qui le prononce présente un son hors grille, que l'alignement forcé étale sur les voisins. Peut déplacer une marque d'un son. À surveiller.
- **La voix lie deux mots** (`want to` → `wanna`). Moins de sons que le texte n'a de lettres, et la coupe entre les deux mots se joue alors sur la seule orthographe. À surveiller.
- **La voix est molle partout** — débit rapide, tout réduit. Toutes les répartitions sont larges, tout se recouvre, et **l'app ne marque presque rien**. C'est le cas le plus vicieux : sous-détection totale et silencieuse, et c'est le prix exact du principe de confiance aveugle. Un contrôle simple existe et n'a pas besoin de norme extérieure — la netteté moyenne des pics de la grille. Une grille molle veut dire « cette prise n'est pas un bon étalon ».

### Le texte n'est pas fait de lettres qui se prononcent

- **Chiffres et symboles** (`25`, `3rd`, `$10`) : deux caractères pour six sons prononcés, et la table ne les écrit pas, donc les sons de « twenty-five » ne reçoivent aucune lettre. **À traiter** en normalisant le texte en toutes lettres avant synthèse, ce qui est de toute façon souhaitable pour le TTS.
- **Sigles** (`USA`, `OK`) : même problème, plus rare, même traitement.
- **Ponctuation et majuscules** : absentes de la table, donc muettes pour la jointure. Couvert par la règle de la lettre neutre.
- **Nom propre ou mot étranger** : le TTS l'invente et un réseau phonémique anglais le décode mal. Grille bruitée localement. À surveiller.

### L'apprenant ne dit pas exactement la grille

- **Hésitation, reprise, toux au milieu** : l'alignement forcé doit quand même caser la grille, donc il étire un son sur toute l'hésitation. Couvert par le filtre de durée absurde, qui est déjà une exigence du contrat.
- **Il s'arrête avant la fin** : la fin de la grille se tasse sur les derniers échantillons, produisant une rafale de fautes finales. Couvert par la brique 11.
- **Bruit ou silence en tête** : le premier son peut s'accrocher au bruit. À surveiller.
- **Débit très différent du modèle** : indifférent à l'alignement, qui est élastique ; mais la brique 7 compare des durées. C'est ce qui rend la normalisation dans le mot obligatoire et non optionnelle.
- **Il chuchote** : pas de fréquence fondamentale, donc mélodie muette et accent privé de son trait de hauteur. Doit dégrader proprement, jamais marquer.

### L'accent, spécifiquement

- **Mot d'une seule syllabe — au compte du mot, jamais au compte de la grille** : rien à comparer, ne jamais marquer. Un mot de deux syllabes dont la grille n'en rend qu'une n'est pas ce cas-là (brique 7).
- **Mot fonctionnel non mis en relief** (`the`, `of`) : les traits sont plats des deux côtés. Correct, mais demande un seuil « le modèle a-t-il un accent net » sous lequel on ne marque pas, sinon on marque du bruit.
- **Consonne syllabique** (`bottle`, `button`) : le noyau n'est pas une voyelle, le mot est compté à une syllabe, aucune marque d'accent n'y apparaît jamais. Silencieux, pas faux — et sans conséquence, ces noyaux n'apparaissant en anglais que dans des syllabes non accentuées.
- **Emphase de sens** (« I said it **IS** important ») : l'apprenant met le relief là où le modèle neutre ne l'a pas, et la divergence légitime est marquée comme faute. Connu et accepté (cf. `reference.md`) — pouvoir redire sur place rend la fausse alerte peu coûteuse.

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

La seconde est que le banc fait passer ici une épreuve **plus dure que ce que l'app fera jamais** : deux voix étrangères l'une à l'autre, sur un mot sans phrase pour le porter. Dans l'app, le modèle est la voix qu'on vient d'entendre, et le mot est une sous-étendue d'une phrase déjà synthétisée. Le seul cas réel est la reprise du mot seul par l'apprenant, en redisant.

## L'écart tombe sur le son fautif

Troisième mesure faite (`bench/faults.py`), sur les blocs A, C et D du jeu d'essai, lues par deux matrices et rien d'autre. La lecture est **ancrée** : chaque cas nomme le son de la grille du modèle où la faute a été faite, et le témoin du même son est lu contre le même modèle.

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

**Sept fautes sur huit vues, aucune fausse alerte, et les témoins à zéro.** Obtenu sans dictionnaire, sans lexique de dialecte et sans appel. Sur le modèle de voix américain, les six fautes lisibles sont vues et les témoins ne dépassent pas 0,002 — le modèle étant le seul référentiel, l'accent ne change rien, ce qui est le comportement attendu.

La seule manquée est `09-walkin` (/ŋ/ dit /n/), et le jeu d'essai le dit de lui-même : c'est une **demi-faute**, le bloc D existant précisément parce que les cas 1, 6 et 9 étaient restés à mi-chemin. À faute franche, signal franc — la reprise franche du même cas, `18-walkin-full`, est vue.

Deux séparations valent d'être lues séparément. Les fautes franches montent toutes au-dessus de 0,95, et les demi-fautes se rangent en dessous de 0,25 : **l'écart ne rend pas un verdict binaire, il rend un degré**, et c'est un degré qui correspond à ce qui a été produit. C'est de là que sortira un seuil de marquage, pas d'une constante posée d'avance.

### Ce que le pire son d'un énoncé révèle

Le maximum de l'énoncé et l'écart sur le son étiqueté ne désignent pas le même endroit, et la différence est instructive. Le pire son d'une prise « correcte » est régulièrement ailleurs que sur le son testé :

- le modèle dit *you are*, le locuteur dit *you're* ;
- le modèle dit `p ɛɹ`, le locuteur dit `p eɪ ɚ` (*payer*) ;
- le modèle réduit *to* en `t ə`, le locuteur dit `t ʊ`.

Ces prises étaient étiquetées « correctes » sur **un** son précis, celui qu'on testait ; rien n'avait jamais été vérifié du reste. Ce ne sont donc pas des fausses alertes du calcul, ce sont des fautes que le jeu d'essai n'avait pas notées — **si le texte dit *you are*, dire *you're* est une faute**, sans quoi il n'y a plus de référence du tout.

En conversation libre le cas ne se pose presque pas, le texte venant de la transcription de ce qui a été dit : qui dit *you're* aura un modèle qui dit *you're*. Il se pose en redisant, où la phrase à dire est celle que l'IA a proposée — et c'est justement là que la règle est la bonne.

Ce que la mesure valide reste **l'écart sur un son donné**. La sélection du son à marquer est un autre problème, et le pire de l'énoncé n'en est pas une mauvaise réponse — il pointe des divergences réelles.

### Le motif de la brique 11, et pourquoi son seuil ne peut pas être une constante

Sur le premier modèle mesuré, `18-walkin-full` se décodait `a j a ŋ u k i n ɐ t ɔ̃ z o o f i s` — `ɔ̃` étant une voyelle nasale française. L'énoncé était lu hors de l'anglais, l'alignement forcé plaquait la grille dessus quand même, et **tous** les sons décrochaient d'un coup : médiane à 0,711 là où toute prise qui s'aligne restait sous 0,022. C'est exactement le motif que la brique 11 doit attraper — un texte de référence qui ne correspond pas à ce qui a été dit produit une avalanche, pas une marque.

Le motif est bon ; le seuil qu'on en avait tiré ne l'est pas. Posé à 0,2 sur l'échelle de ce modèle-là, il se déclenche à tort sur le modèle retenu, où il a exclu une détection parfaitement saine. **Le seuil doit être relatif au modèle**, ou mieux, à la prise elle-même : ce qui signale l'avalanche n'est pas une valeur absolue mais le fait que la médiane cesse d'être négligeable devant le maximum.

## Ce qui départage un modèle acoustique

**Le critère est la fidélité : rendre les sons réellement prononcés.** Il ne se confond pas avec « rendre une belle suite de sons » — un modèle qui devine celle qu'on voulait produire la rend excellente, et c'est exactement celui qui ne verra jamais la faute. La fidélité s'éprouve donc dans les deux sens, sur les prises fautives **et** sur les prises correctes.

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

**L'affinage maison ne se départage pas du sortant sur ce qui se mesure aujourd'hui.** Quatre états d'un affinage complet sur l'encodeur du sortant, lus contre lui sur ce que l'app consomme : 162 à 164 lettres justes sur 166, contre 164 pour le sortant, et 32 à 34 mots au bon nombre de syllabes sur 37, contre 34. Deux sons d'écart entre le meilleur et le pire. Ce qui ne s'en déduit pas — et c'est l'essentiel : le jeu est de onze à seize phrases d'une seule voix, et rien n'a été lu au-delà. Que rien ne les sépare ici ne dit pas qu'ils se valent, et le chantier n'est pas clos (cf. `../TODO.md`).

### Ce qui a été examiné puis écarté

- **Un troisième réseau qui étiquette chaque trame, pour la seule grille** (`charsiu` et son outil `wav2textgrid`, des aligneurs qui posent un son sur chaque trame plutôt qu'une suite de sons). Écarté sur objection juste : ça répare la case, pas la mesure. La répartition du modèle *lue dans la case* viendrait toujours de la matrice de la brique 2, vide à cet endroit — l'apprenant qui prononce bien le son y opposerait une répartition pleine à une répartition vide, donc une divergence forte : **marqué pour avoir eu raison**. S'ajoutent une table de correspondance entre deux inventaires et 190 à 380 Mo.
- **`charsiu` embarqué** : poids publiés nus, sans fiche ni licence — le dépôt de code MIT ne couvre pas un artefact hébergé ailleurs.
- **MMS_FA embarqué** : licence CC-BY-NC, et alphabet **lettres** (~28 symboles romanisés) — même libre, il ne saurait pas dire « ici un /k/ ». Reste mesurable localement comme témoin d'une recette, jamais comme pièce de l'app.
- **Un second réseau à sortie caractères**, qui ancrait les mesures au texte : construit, mesuré, **retiré**. Il rendait la même jointure à un son près, pour 191 Mo de poids et une passe de plus.

### L'arrondi ne prend que le milieu

La quantification est la transformation à laquelle ce montage est le plus exposé, et pour une raison qui lui est propre : on ne lit pas le son gagnant, on lit ce qui reste autour de lui — l'information qui vit dans les décimales, la première que l'arrondi emporte. Un système qui ne regarde que le maximum y survit sans y penser ; celui-ci est le cas le plus fragile qui soit.

Mesuré plutôt qu'argumenté, en entiers 8 bits sur les couches linéaires :

| prise | flottant | entier 8 bits |
|---|---|---|
| `16-ship-lax` | 0,983 | 0,982 |
| `07-bear-pear` | 0,967 | 0,971 |
| `17-sink-full` | 0,966 | 0,964 |
| `06-ship-sheep` | 0,962 | 0,961 |
| `08-light-right` | 0,956 | 0,964 |
| `01-sink` (demi-faute) | 0,246 | **0,114** |
| témoins | 0,000 à 0,002 | 0,000 à 0,003 |

Les fautes franches bougent de huit millièmes au pire, dans les deux sens — du bruit. Les témoins ne bougent pas. Toutes les fautes vues en flottant le restent, sur les deux accents de modèle.

**Ce que l'arrondi prend, c'est le milieu.** La demi-faute perd plus de la moitié de son signal. Les extrêmes sont trop gros pour être érodés, un son laissé à mi-chemin ne l'est pas. D'où une règle pour plus tard : **le seuil de marquage se calibre sur les poids qui tourneront**, pas sur ceux qui ont été entraînés, faute de quoi il sera trop haut d'un facteur deux dans la zone grise.

**Ce test avait deux angles morts, et l'un des deux mordait.** Il arrondissait par PyTorch, sur les seules couches linéaires, alors que le moteur d'exécution sera ONNX Runtime et qu'il quantifie plus large.

Refait par ONNX Runtime, réglages par défaut, la lecture ne tient plus : le témoin `13-field-clean`, un /θ/ correctement prononcé, monte à **0,851** — en plein territoire de faute — la bande vide tombe de 0,111 à 0,091, et trois fautes passent inaperçues. La cause est nommable : l'outil arrondit aussi les convolutions d'entrée, c'est-à-dire la part du réseau où le signal est encore un signal.

Réparable, et réparé. Trois variantes mesurées au même jeu :

| arrondi | pire témoin | fautes vues | taille |
|---|---|---|---|
| par défaut | 0,851 | 5 / 8 | 320 Mo |
| produits matriciels seuls | 0,011 | 6 / 7 | 357 Mo |
| **+ échelle par canal** | **0,003** | **6 / 7** | **359 Mo** |
| *(PyTorch, pour mémoire)* | *0,003* | *6 / 7* | — |

La dernière retrouve la lecture qualifiée, verdict pour verdict, pour 39 Mo de plus. L'échelle par canal compte parce qu'une seule échelle pour toute une matrice est fixée par sa plus grosse colonne, et toutes les autres la paient.

Ce que ça dit au-delà du chiffre : **une mesure d'arrondi ne vaut que pour l'arrondisseur qui l'a faite.** Celle qui a fondé la décision 8 bits ne se transportait pas.

## Ce qui reste à mesurer

Le travail ouvert vit dans `../TODO.md` ; la façon de le juger dans `qualification.md`, sur le matériel déjà enregistré du banc — aucun appel d'API n'est nécessaire.

Une conséquence de méthode, tirée du portage sur l'appareil : **une lecture n'a pas à avoir été calculée ici.** Le téléphone range ses matrices dans le cache comme n'importe quelle autre lecture (`READING=<nom>`), et toutes les briques du banc tournent dessus sans le savoir. C'est ce qui permet de poser à l'appareil la question du verdict, et pas seulement celle des chiffres.
