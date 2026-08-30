# Voir un son que l'apprenant ajoute — le fil du 2026-08-30

Le fil entier d'une journée : ce qui a été mesuré, ce qui est passé dans l'app, ce qui en a été retiré, et ce qui a été exploré puis laissé de côté. Les faux pas y sont, parce qu'ils ont chacun coûté une livraison et que les refaire coûterait autant.

Instruments, dans l'ordre où ils sont venus : `../../bench/insertions.py` (la mesure préalable) et `../../bench/anchor.py` (la découpe contre les sons du modèle, explorée et abandonnée), qui lisent tous deux des fichiers déjà écrits ; puis `../../bench/placed.py`, **la voie retenue**, qui relit l'audio de l'apprenant pour poser ses sons sur les lettres du texte.

## Le problème

L'analyse décode la grille **du modèle seul** et y aligne l'apprenant de force. Il y a donc exactement autant de cases que le modèle a de sons, et **pas une de plus**. Un son que l'apprenant ajoute n'est pas mal noté : il n'est pas vu. Sa seule trace est qu'il occupe du temps dans la plage d'un son voisin, dont il gonfle l'écart — si bien que la marque tombe sur une lettre correctement prononcée.

Et une lettre muette n'a aucun repère : `Join.trimmed` retire aux extrémités ce qui ne rapporte rien, donc rien ne peut la teindre. Prononcer le `b` de `comb` était doublement invisible — non vu, et impeignable.

## 1. Mesuré avant d'écrire — `bench/insertions.py`

Décodage libre de l'apprenant (l'argmax d'une matrice déjà calculée, pas de seconde passe), distance d'édition contre la grille du modèle, et chaque insertion se voit offrir les lettres de son mot qu'aucun son ne réclame — exactement les lettres muettes — bornées à celles situées entre les deux sons voisins.

Sur 17 prises du jeu étiqueté, `timit-ipa`, voix `eleven-us-eric` :

- **1,3 insertion par prise** sur 251 sons de grille. La crainte qui pouvait tuer la piste d'emblée — un décodage qui revendique une insertion un son sur deux — ne se réalise pas.
- **4 insertions sur 22 s'ancrent** sur une lettre (18 %). Sans la borne de position on monte à 5, la cinquième étant un `ʊ` inséré au milieu de `comfortable` qui allait éclairer une lettre de l'autre bout du mot.
- **Les quatre ancrages tombent dans le même mot**, `comfortable`, sur le `o` et le `r` — les lettres de la syllabe que l'orthographe écrit et que le modèle réduit.
- **Mais il ne sépare pas la faute de ses témoins** : `10-comfortable` (syllabe insérée étiquetée) rend 1 ancrage, `19-comfortable-com` 1, `20-comfortable-for` 2.

**Ce que la mesure ne couvre pas** : aucune des 17 prises ne prononce une lettre muette. Le cas pour lequel le canal a été conçu n'est pas dans le matériau. Et aucune vérité terrain ne dit quels sons une prise a ajoutés — une « insertion » est ce que le décodage revendique.

Écrit quand même, décision prise en connaissance de cause.

## 2. Écrit — `Added.kt`

Distance d'édition, ancrage sur une lettre muette, et un **chevron dans la couture** entre deux lettres quand aucune lettre ne peut porter. Binaire, sans points : une insertion n'a pas de côté modèle, donc rien à comparer, donc aucun degré à donner.

## 3. Faux pas — le contrôle de glissement

Une prise réelle, texte de référence `I think you're right.`, lue à tort comme « le locuteur a dit un autre mot ». D'où la conclusion : l'aligneur a glissé, le `/θ/` noté 0,0 est un faux accord. **Prémisse fausse** — la phrase avait été dite fidèlement, et 0,0 était le bon résultat.

Livré quand même : un son dépassant trois fois la médiane des sons de la prise → lecture refusée, marques éteintes. Les chiffres semblaient nets :

| prise | pire son | médiane | rapport |
|---|---|---|---|
| dite fidèlement | 222 ms | 121 | **1,8** |
| avec un « hmm » | 666 ms | 142 | 4,7 |
| avec un « hmm » | 767 ms | 182 | 4,2 |

Ils l'étaient. Ce qu'ils mesuraient n'était pas ce que je croyais : **un son ajouté fait toujours gonfler ses voisins**, c'est mécanique, donc le seuil se déclenchait sur toute insertion et éteignait des marques correctes. Le contrôle punissait exactement le cas qu'il devait marquer. Retiré.

**La leçon, qui vaut au-delà de ce cas** : ne pas construire sur une lecture de ce que l'utilisateur a dit sans la lui faire confirmer.

## 4. Les durées, relues correctement

L'alignement CTC doit couvrir **toutes** les trames de l'apprenant — il n'a pas le droit d'en laisser tomber. Une matière sans son de modèle à qui appartenir est donc avalée par les deux sons qui l'encadrent, moitié chacun. Sur trois prises d'une phrase avec un « hmm » : tout entre 80 et 240 ms sauf les deux sons à cheval sur le trou, à 505/565, 626/666 et 767/726.

Le gonflement ne diagnostique pas une panne — **il localise l'insertion**, et il la localise là où un lecteur la mettrait.

D'où le partage à deux signaux :

| | trouve | ne voit pas | lit |
|---|---|---|---|
| **gonflement** | les insertions longues | une lettre muette, qui ajoute 60 ms | les plages, aucune étiquette |
| **distance d'édition** | les insertions courtes | rien des mots | les étiquettes |

De la distance d'édition, seul ce qu'une lettre muette peut porter est gardé — le `s` fantôme d'un tour gelé disparaît de lui-même.

## 5. Le filtre pause

Un tour gelé l'a montré aussitôt : `06-ship-sheep`, une couture entre `sheep` et `is`, les deux voisins à 0,54 s et 0,52 s, et le décodage libre ne nommant **rien** dans l'intervalle. Une **pause**, pas un son. Règle ajoutée : le gonflement dit où regarder, le décodage libre dit s'il y a quelque chose — rien de nommé, pas de marque.

## 6. Cinq prises, et ce qu'elles disent des deux signaux

Un tour, cinq tentatives, texte de référence `I think you're right.`

| | ce qui a été dit |
|---|---|
| att1 | la phrase, sans ajout |
| att2 | + un « hmm », décodé `ə` |
| att3 | + un « hmm », décodé **en rien** — 500 ms sans aucun symbole |
| att4 | `I think you're very right` — un mot entier ajouté |
| att5 | `I think I'm right` — un mot **substitué**, pas ajouté |

**Le gonflement ne se déclenche presque jamais.** Seul att3 passe les 3× la médiane. att2 rate de 19 ms. att4 — un mot entier — n'en approche pas, parce que « very » se répartit sur trois cases au lieu d'en faire exploser deux. Et le seuil relatif à la médiane se sabote : l'insertion fait monter la médiane aussi.

**Le filtre pause tue le seul cas qui passait.** Dans att3 le « hmm » se décode en rien ; la couture n'a aucun symbole, la marque est écartée. La distinction pause/hmm n'existe pas à ce niveau — les deux sont du silence pour le réseau.

**Une piste écartée en chemin : compter les sons libres par case.** Un par case = propre, plus d'un = matière en trop. Ça sépare les cinq prises… mais **att5 en donne la limite** : un mot substitué occupe le même nombre de sons que celui qu'il remplace, donc le compte voit un par case et ne voit rien. Structurellement aveugle à la substitution, là où l'ancrage par mot voit les deux. C'est ce qui a fait renoncer à cette piste au profit de la suivante.

## 7. L'ancrage par mot — exploré, non retenu

**Rien de ce chapitre n'est dans l'app.** Instrument : `../../bench/anchor.py`.

Le défaut visé : la distance d'édition est **à plat**, elle minimise un coût total et ne sait rien des mots. Quand une divergence apparaît tôt, il lui revient moins cher de réétiqueter des sons légitimes plus loin que de payer une substitution — mesuré, elle a déclaré « insérés » le `u` et le `ɹ` d'un `you're` correctement prononcé.

L'idée : partager les sons librement décodés de l'apprenant **entre les mots du modèle**, coupes choisies pour concorder au mieux. C'est la forme de `join.partition`, un étage au-dessus et du côté de l'apprenant.

### État 1 — la distance d'édition à plat (ce que l'app fait)

```
added: [{symbol: ʊ, at: null, after: 8, afterSound: 5},
        {symbol: ɹ, at: null, after: 8, afterSound: 5}]
```

Le chevron tombe après le caractère 8, **à l'intérieur de `you're` entre le `y` et le `o`**, et il accuse deux sons bel et bien prononcés. Le « hmm » était entre `you're` et `right`.

### État 2 — partage entre les mots, par les symboles seuls

Score d'un bloc contre un mot : `2 × (plus longue sous-suite commune) − longueur du mot − longueur du bloc`.

```
== attempt 3
   mot        modèle           apprenant          reste
   I          aɪ               aɪ
   think      θ i ŋ k          θ i ŋ k
   you're     j ɝ              j                             <== manque
   right.     ɹ aɪ t           ʊ ɹ ɹ aɪ t         ʊ ɹ        <== en trop
```

**La coupe est indécise.** `j` à `you're` et le reste à `right` vaut −3 ; `j ʊ ɹ` à `you're` et `ɹ aɪ t` à `right` vaut −3 aussi. Ex æquo exact, tranché par l'ordre de parcours.

### État 3 — plus la contrainte de temps

Chaque mot a une fenêtre dans l'enregistrement de l'apprenant, lue de l'alignement forcé. `+1` par son qui **concorde et** tombe dans la fenêtre, `−1` par son ramené de l'extérieur.

Les coupes deviennent justes sur les cinq prises. Deux versions intermédiaires ont échoué et méritent d'être notées : récompenser **tout** son dans la fenêtre payait un mot pour avaler de la matière étrangère (« very » y venait gratuitement) ; ne récompenser que les sons concordants sans pénaliser les autres laissait tout se déverser sur le dernier mot. Il faut les deux.

**Défaut restant** : « reste » est calculé par un parcours glouton, qui déclare `ɑ` « en trop » sur att1 alors que c'est une substitution de `ɝ`.

### État 4 — plus une vraie distance d'édition dans le mot

Trois colonnes : **en trop**, **manque**, **autrement** (`modèle>apprenant`). C'est l'état retenu, ce que `bench/anchor.py` rend par défaut.

```
== attempt 1  (sans ajout)
   mot        modèle         apprenant        en trop   manque    autrement rouge
   I          aɪ             aɪ
   think      θ i ŋ k        θ i ŋ k
   you're     j ɝ            j ɑ                                  ɝ>ɑ       oui
   right.     ɹ aɪ t         ɹ aɪ t

== attempt 2  (hmm, décodé ə)
   you're     j ɝ            j ʊ ɹ ə          ʊ ɹ                 ɝ>ə
   right.     ɹ aɪ t         ɹ aɪ t

== attempt 3  (hmm, décodé en rien)
   you're     j ɝ            j ʊ ɹ            ʊ                   ɝ>ɹ
   right.     ɹ aɪ t         ɹ aɪ t

== attempt 4  (very)
   you're     j ɝ            j ʊ ɹ v ɛ        ʊ ɹ v               ɝ>ɛ
   right.     ɹ aɪ t         ɹ i ɹ aɪ t       ɹ i

== attempt 5  (I'm)
   you're     j ɝ            aɪ m                                 j>aɪ ɝ>m  oui
   right.     ɹ aɪ t         ɹ aɪ t
```

**Gagné :**

- **Rien ne fuit d'un mot à l'autre.** `I` et `think` propres sur les cinq prises, `right` sur quatre. Le défaut de départ a disparu.
- **La substitution se distingue de l'ajout.** att1 et att5 rendent des substitutions et **rien en trop**. Et l'écart au modèle les marque rouge de son côté : deux mécanismes indépendants qui concordent.

**Pas gagné :**

- **Le « hmm » n'est pas isolé.** Sur att2 le vrai partage serait `ʊ ɹ` = prononciation non contractée, `ə` = le hmm. L'alignement fait l'inverse.
- **`very` est coupé en deux**, parce que chaque son de l'apprenant doit appartenir à un mot du modèle.

### État 5 — plus un bloc n'appartenant à aucun mot

Symétrique de `join.partition`, où un mot peut ne recevoir aucun son. Chaque son laissé de côté coûte un prix fixe. Balayé :

| prix | att1 `ɑ` (substitution) | att3 `ʊ ɹ` (prononciation) | att4 `v ɛ ɹ i` (mot inséré) | att5 `aɪ m` (mot substitué) |
|---|---|---|---|---|
| 1,0 | **sorti** ✗ | **sorti** ✗ | sorti ✓ | **sorti** ✗ |
| 1,5 | gardé ✓ | gardé ✓ | **gardé** ✗ | gardé ✓ |
| 2,0 | gardé ✓ | gardé ✓ | **gardé** ✗ | gardé ✓ |

**Aucun prix ne marche**, et il n'y a pas de valeur intermédiaire.

## Le mur

`ʊ ɹ` (une prononciation non contractée) et `v ɛ ɹ i` (un mot en plus) ont **la même forme au niveau des symboles** : des sons que le mot du modèle n'a pas. Et le temps ne les sépare pas — l'alignement forcé a étiré l'unique `ɝ` du modèle par-dessus le mot inséré, si bien que « very » tombe dans la fenêtre de `you're`.

La cause première est en amont : **le modèle prononce `you're` en un seul son `ɝ`, très contracté, et le locuteur en deux ou trois.** Surplus permanent dans ce mot, et tout ce qui s'y ajoute vraiment s'y cache. Que ce surplus soit marqué est correct — le modèle est la source de vérité, cf. `../reference.md`, « la règle vaut aux trois échelles ». Ce qui ne l'est pas, c'est d'en déduire « un son a été ajouté ».

## 8. Le troisième signal — l'orthographe et le temps

Ce que « Pour reprendre » cherchait a été trouvé, écrit, mesuré, et **écarté**. Le fil entier tient ici, parce que chaque impasse a coûté une demi-journée.

L'idée : deux signaux que les symboles ne portent pas.

- **L'orthographe.** Un mot dit plus pleinement est fait de sons que ses propres lettres écrivent — le `ʊ` payé par le `o`/`u` de `you're`, le `ɹ` par son `r`. Un mot inséré apporte au moins un son qu'aucune lettre voisine n'écrit : le `v` de `very` réclame une lettre `v`, absente de `you're` comme de `right`. La table d'affinité (`../../bench/affinity.json`) répond, dans la licence qu'elle a déjà — dire où un son peut s'écrire, jamais s'il est faux.
- **Le temps.** Le trou que la série de sons en trop occupe dans le **décodage libre**, jamais dans l'alignement forcé, comparé au mot du modèle mis à l'échelle du débit de l'apprenant.

Trois branches : un son étranger → matière intercalée ; un run payable et un trou court → le mot dit plus pleinement ; un run payable et un trou plus long que le mot entier → matière intercalée quand même, ce qui attrape le « hmm ».

**Sur les cinq prises, ça sépare.** `very` est seul à lever un son étranger ; les deux « hmm » se partagent sur le temps, 464 ms contre un mot de 276, et 141 contre 308.

**Sur dix-sept prises, ça se déclenche partout.** 20 séries de sons en trop, dont 9 lues « matière intercalée » — et trois des cinq témoins en portent une.

**Sur 2500 prises d'apprenants réels** (1690 lisibles) : **1704 « matière intercalée » pour 1690 prises**, environ une par prise, 80 % des prises portant au moins une série. Les mots qui les reçoivent sont toujours les mêmes : to, was, the, is, you, one, will, be, a, of.

### Pourquoi, et les trois réparations qui ont échoué

La cause tient au dénominateur. La branche temps demande « le trou est-il plus long que le mot ? », et le mot est celui que la découpe a donné au run, mesuré côté modèle. Sur un petit mot outil que la synthèse réduit, la barre est nulle — `at` fait un seul son de 20 ms, `to` 66 ms au débit de l'apprenant. N'importe quel trou la franchit, et le corpus dit que c'est le cas général.

Trois dénominateurs de rechange, tous mesurés faux :

1. **Ce que le modèle passe sur le même intervalle**, d'un son concordant à l'autre. Se déclenche sur 16 séries sur 18. C'est circulaire : le trou existe *parce qu'*il y a des sons en plus dedans, donc l'intervalle du modèle entre les deux mêmes sons est court par construction.
2. **De vraies durées, en alignant l'apprenant sur sa propre suite de sons.** Tautologie : le chemin de l'argmax *est* l'optimum quand la cible est la suite que l'argmax a produite. Mesuré, 12 sons sur 12 rendent exactement le pic de départ. Le décodage libre ne donne pas de durées et n'en donnera pas.
3. **Les durées de l'alignement forcé**, lues comme un écart entre deux sons concordants. Rend **0** sur les deux prises au « hmm ». L'alignement forcé n'a pas de trous : il répartit toutes les trames entre les sons du modèle, bord à bord. La matière ajoutée ne se loge jamais *entre* deux fenêtres, elle est avalée *dans* les fenêtres voisines — c'est le gonflement de l'état 4, déjà mesuré inutilisable.

**Ce que ces trois échecs disent ensemble**, et qui vaut au-delà de ce chantier : le décodage libre donne les étiquettes et la position des pics, sans aucune durée ; l'alignement forcé donne les durées et n'a aucune case pour un son ajouté. Aucun des deux ne dit combien de temps la matière ajoutée a duré, et les combiner ne l'invente pas.

### Une réparation qui, elle, tient

Ta question sur le `t` a trouvé un vrai défaut de la découpe, indépendant du reste. `right` dit `ɹ aɪ t` là où le modèle bat un `ɾ` : le `t` partait dans le mot suivant. Mesuré, les trois coupes valaient **exactement −5** — égalité à trois, tranchée par l'ordre du parcours. Le terme de placement, écrit pour fermer ce genre d'égalité, ne l'atteint pas ici : le `t` est un son en trop pour qui que ce soit qui le prenne, et il ne tombe dans la fenêtre d'aucun mot.

La table apparente pourtant les deux : la lettre `t` écrit `t` **et** `ɾ`. Une paire dont un même caractère du mot sait écrire les deux sons vaut donc **une demi-concordance** — pas une concordance, le modèle restant la norme et l'écart restant à marquer ; pas rien, puisque c'est une case d'un mot dite de deux façons. Les cinq prises sont inchangées, les neuf « intercalé » des dix-sept deviennent huit, et les deux coupes réparées sont ce `t` et le `f` de `from` qui était tiré dans `pear`.

C'est acquis, et ça ne sauve pas la branche temps : le corpus la déclenche une fois par prise.

## 9. Les lettres découpent — retenu

**Changer le cadre.** Tout ce qui précède découpe la suite de l'apprenant en la confrontant aux **sons** du modèle, et n'appelle les lettres qu'après coup. On fait l'inverse : on pose les sons de l'apprenant sur les **lettres du texte**, et les mots tombent de là. Instrument : `../../bench/placed.py`.

Rien de neuf n'est écrit. `join.joined` prend un audio et un texte et fait exactement ça ; le banc ne l'a jamais pointé que vers `model.wav`. Pointé vers `said.wav`, il place l'apprenant.

Les lettres portent alors les deux lectures à la fois, et c'est ce qui les fait se regarder **sans horloge** : le `ɝ` contracté du modèle couvre `ou're` d'un seul son, et l'apprenant qui ne contracte pas pose un `ʊ` sur le `o` et un `ɹ` sur le `r`, dans le même mot. Un son qu'aucune lettre d'aucun mot ne sait écrire, à sa place dans l'ordre, n'appartient à aucun mot — **et c'est toute la détection.**

Deux raisons de fond, qui ne sont pas de commodité. `../reference.md` pose déjà les lettres comme coordonnée commune du marquage — les trois échelles « s'ancrent aux mêmes caractères du texte affiché », ce sont « trois propriétés d'une seule chaîne ». Les faire porter la découpe n'ajoute pas une idée, ça en retire une. Et ça rend les deux lectures symétriques : l'ancrage aux lettres était fait d'un seul côté, alors que le projet refuse partout de traiter un seul des deux audios.

Le modèle reste la source de vérité sans changement. Les lettres ne jugent rien ; elles disent seulement où les deux lectures se rencontrent.

### Mesuré

**`join` survit à la suite bruitée de l'apprenant** — la crainte qui pouvait tuer la piste d'emblée. **250 sons sur 267 trouvent une lettre sur les dix-sept prises, 94 %**, contre les 95 % que la même brique obtient côté modèle.

Sur `I think you're right.` dit cinq fois :

| | apprenant sur les lettres de `you're` | hors mot |
|---|---|---|
| att1 (sans ajout) | `j ɑ` | — |
| att2 (hmm → `ə`) | `j ʊ ɹ ə` | — |
| att3 (hmm muet) | `j ʊ ɹ` | — |
| att4 (`very`) | `j ʊ ɹ i` | **`ɹ v ɛ`** |
| att5 (`I'm`) | `aɪ` | **`m`** |

Et les mots ne fuient plus : `I`, `think`, `right` sont propres sur les cinq.

**Les trois prises `comfortable` sont réglées, gratuitement** — le verdict qui échappait à tous les états précédents :

```
modèle       comfortable = k ə m f   t ɝ b l
apprenant    comfortable = k ə m f ə t ɪ b l      <- la syllabe insérée, dans le mot
```

Rien ne sort, et les deux pires fausses alertes de l'état 8 — le `i` de `is` sur les prises 19 et 20, aux rapports 4,1 et 4,4 — disparaissent : `is` se lit `i z` contre `ɝ z`, une substitution propre. **Trois des cinq témoins ne lèvent plus rien du tout.**

Dix-sept sons tombent hors mot sur les dix-sept prises, environ un par prise.

### Ce qu'elle rate, et pourquoi ça ne bloque pas

**Une substitution dont aucune lettre du mot ne sait écrire le son sort du mot** et se lit comme de la matière ajoutée. `have` dit `h æ f` : le `f` sort, parce que la lettre `v` n'écrit pas `f`. Le `m` de `I'm` sort pour la même raison.

Décidé, et non bloquant : **le mot porte une marque de toute façon**, et l'analyse détaillée nomme le `f` comme le son en cause. La personne qui la lit fait le reste. Un chevron de plus à côté d'un mot déjà signalé ne demande rien — l'unité de réparation est la phrase, le mot au plus, jamais le son (`../reference.md`). Le raffinement possible est noté dans `../../TODO.md` et rien ne presse.

**Et le « hmm » d'att2 se glisse dans le `e` de `you're`**, qui sait écrire un `ə`. Cas raté, accepté d'avance : on ne l'attrape pas, et att3 dit pourquoi il ne faut pas essayer plus fort — quand le « hmm » se décode en rien, aucune règle ne peut inventer un son.

**Deux bornes de lecture**, qui valent pour tous les chiffres ci-dessus. Les étiquettes du corpus L2 notent **chaque mot**, propre ou fautif ; elles ne disent rien de ce qui se passe **entre** deux mots, donc elles ne peuvent ni confirmer ni infirmer un chevron. Et marquer une hésitation est le comportement **voulu** — tout écart au modèle se marque —, donc un chevron entre deux mots bien dits n'est pas une erreur par défaut.

## Ce qui est dans l'app à la fin de la journée

Le canal des états 1 à 5 : gonflement pour les insertions longues, distance d'édition pour ce qu'une lettre muette peut porter, filtre pause. **Ni l'ancrage par mot, ni la découpe par les lettres** — la voie retenue n'est pas écrite en Kotlin.

## Pour reprendre

- La voie retenue est le chapitre 9. `cd bench && python3 placed.py <said.wav> <turn.json>...`, par paires. Ce qui reste à faire est de l'écrire en Kotlin ; côté banc, il n'y a rien à inventer, `join.joined` fait déjà tout et il suffit de le pointer vers l'audio de l'apprenant.
- Les chapitres 7 et 8 sont **fermés** : la découpe contre les sons du modèle, avec ou sans prix du bloc intercalé, avec ou sans signal d'orthographe et de temps. Trois dénominateurs y ont été mesurés faux, et le chapitre 8 dit pourquoi aucun quatrième ne viendra — le décodage libre n'a pas de durées, l'alignement forcé n'a pas de cases pour un son ajouté.
- **Cinq prises d'une phrase par un locuteur, dix-sept du jeu étiqueté, 2500 du corpus L2 : le matériau situe une ampleur, il ne juge aucun cas.** Aucun nombre de ce document n'est réglé, et aucun ne doit l'être sur ce matériau. Ce qui tranche un chevron donné est l'oreille de qui a parlé.
