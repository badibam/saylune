# Voir un son que l'apprenant ajoute — le fil du 2026-08-30

Le fil entier d'une journée : ce qui a été mesuré, ce qui est passé dans l'app, ce qui en a été retiré, et ce qui a été exploré puis laissé de côté. Les faux pas y sont, parce qu'ils ont chacun coûté une livraison et que les refaire coûterait autant.

Instruments : `../../bench/insertions.py` (la mesure préalable) et `../../bench/anchor.py` (l'exploration finale). Tous deux lisent des fichiers déjà écrits — pas de modèle acoustique, pas d'audio.

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

## Ce qui est dans l'app à la fin de la journée

Le canal de l'état 2 plus les corrections des états 4 et 5 : gonflement pour les insertions longues, distance d'édition pour ce qu'une lettre muette peut porter, filtre pause. Rien de l'ancrage.

## Pour reprendre

- `cd bench && python3 anchor.py <turn.json>...` ; `-1.5` rallume le bloc intercalé à ce prix.
- Ce qu'il faut n'est pas un nombre mieux réglé, c'est **un troisième signal** distinguant un mot en plus d'une réalisation plus longue. Deux pistes non éprouvées : un mot inséré occupe un temps comparable à un mot du modèle, là où une syllabe non contractée est plus courte ; et la matière intercalée est encadrée de mots qui, eux, concordent exactement.
- Et du matériau. **Cinq prises d'une phrase par un locuteur, dix-sept du jeu étiqueté : c'est un banc d'essai pour une idée, pas une mesure.** Aucun nombre de ce document n'est réglé, et aucun ne doit l'être sur ce matériau.
