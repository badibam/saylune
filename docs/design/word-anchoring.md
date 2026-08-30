# L'ancrage par mot — exploration du 2026-08-30

**Rien de ceci n'est dans l'app.** C'est une exploration, laissée en l'état pour être reprise. L'instrument est `../../bench/anchor.py` ; il lit des prises réelles et rien d'autre — pas de modèle acoustique, pas d'audio, tout ce qu'il consomme est déjà dans `files/takes/<horodatage>/turn.json`.

## Le problème de départ

L'app repère un son ajouté par une **distance d'édition à plat** entre deux suites de symboles : la grille décodée du modèle, et le décodage libre de l'apprenant. Elle minimise un coût total et ne sait rien des mots. Quand une divergence apparaît tôt, il lui revient moins cher de réétiqueter des sons légitimes plus loin que de payer une substitution — mesuré, elle a déclaré « insérés » le `u` et le `ɹ` d'un `you're` correctement prononcé, pour équilibrer son compte ailleurs.

L'idée éprouvée ici : donner à l'appariement la structure qui lui manque. Les sons librement décodés de l'apprenant sont **partagés entre les mots du modèle**, les coupes choisies pour concorder au mieux. Ce qui reste dans un mot y reste. C'est la forme de `join.partition`, qui coupe déjà les sons du modèle entre ces mêmes mots — un étage au-dessus, du côté de l'apprenant.

## Le matériau

Cinq prises consécutives d'un même tour, un locuteur, texte de référence `I think you're right.`

| | ce qui a été dit |
|---|---|
| att1 | la phrase, sans ajout |
| att2 | la phrase + un « hmm », que le réseau décode `ə` |
| att3 | la phrase + un « hmm », que le réseau décode **en rien** — 500 ms sans aucun symbole |
| att4 | `I think you're very right` — un mot entier ajouté |
| att5 | `I think I'm right` — un mot **substitué**, pas ajouté |

Cinq prises d'une phrase par un locuteur : c'est un banc d'essai pour une idée, pas une mesure. Aucun nombre ci-dessous n'est réglé, et aucun ne doit l'être sur ce matériau.

## État 1 — la distance d'édition à plat (ce que l'app fait)

Sur une prise antérieure du même tour, avec un « hmm » :

```
added: [{symbol: ʊ, at: null, after: 8, afterSound: 5},
        {symbol: ɹ, at: null, after: 8, afterSound: 5}]
```

Le chevron tombe après le caractère 8, c'est-à-dire **à l'intérieur de `you're`, entre le `y` et le `o`**, et il accuse deux sons que le locuteur a bel et bien prononcés. Le « hmm », lui, était entre `you're` et `right`.

## État 2 — partage entre les mots, par les symboles seuls

Score d'un bloc contre un mot : `2 × (plus longue sous-suite commune) − longueur du mot − longueur du bloc`. Nul quand les deux concordent exactement, négatif pour chaque son que l'un a et l'autre pas.

```
== attempt 3  "I think you're right."
   mot        modèle           apprenant          reste
   I          aɪ               aɪ
   think      θ i ŋ k          θ i ŋ k
   you're     j ɝ              j                             <== manque
   right.     ɹ aɪ t           ʊ ɹ ɹ aɪ t         ʊ ɹ        <== en trop
```

**Défaut : la coupe est indécise.** Donner `j` à `you're` et le reste à `right` vaut −3 ; donner `j ʊ ɹ` à `you're` et `ɹ aɪ t` à `right` vaut −3 aussi. Ex æquo exact, tranché par l'ordre de parcours et par rien d'autre.

## État 3 — plus la contrainte de temps

Chaque mot du modèle a une fenêtre dans l'enregistrement de l'apprenant, lue de l'alignement forcé : du début de son premier son à la fin de son dernier. Un son du décodage libre porte son instant. Le score gagne `+1` par son qui **concorde et** tombe dans la fenêtre du mot, et `−1` par son ramené de l'extérieur.

Les coupes deviennent justes sur les cinq prises. Tout ce qui se passe reste dans le mot où ça s'est passé, et `I`, `think`, `right` sortent propres partout (sauf `right` sur att4, voir plus bas).

**Défaut restant : « reste » est calculé par un parcours glouton**, qui ne distingue pas un son dit *autrement* d'un son dit *en plus*. Sur att1, prise sans ajout, il déclare `ɑ` « en trop » alors que c'est une substitution de `ɝ`.

## État 4 — plus une vraie distance d'édition à l'intérieur du mot

Trois colonnes au lieu d'une : **en trop** (insertions), **manque** (omissions), **autrement** (substitutions, notées `modèle>apprenant`).

```
== attempt 1  "I think you're right."          (sans ajout)
   mot        modèle         apprenant        en trop   manque    autrement rouge
   I          aɪ             aɪ
   think      θ i ŋ k        θ i ŋ k
   you're     j ɝ            j ɑ                                  ɝ>ɑ       oui
   right.     ɹ aɪ t         ɹ aɪ t

== attempt 2  "I think you're right."          (hmm, décodé ə)
   you're     j ɝ            j ʊ ɹ ə          ʊ ɹ                 ɝ>ə
   right.     ɹ aɪ t         ɹ aɪ t

== attempt 3  "I think you're right."          (hmm, décodé en rien)
   you're     j ɝ            j ʊ ɹ            ʊ                   ɝ>ɹ
   right.     ɹ aɪ t         ɹ aɪ t

== attempt 4  "I think you're right."          (very)
   you're     j ɝ            j ʊ ɹ v ɛ        ʊ ɹ v               ɝ>ɛ
   right.     ɹ aɪ t         ɹ i ɹ aɪ t       ɹ i

== attempt 5  "I think you're right."          (I'm)
   you're     j ɝ            aɪ m                                 j>aɪ ɝ>m  oui
   right.     ɹ aɪ t         ɹ aɪ t
```

C'est l'état retenu, et c'est ce que `bench/anchor.py` rend par défaut.

**Ce qui est gagné :**

- **Rien ne fuit d'un mot à l'autre.** `I` et `think` propres sur les cinq prises, `right` sur quatre. Le défaut de départ a disparu.
- **La substitution se distingue de l'ajout.** att1 et att5 rendent des substitutions et **rien en trop** : un mot dit autrement n'est pas un mot en plus. Et l'écart au modèle les marque rouge de son côté — les deux mécanismes concordent, par des chemins indépendants.

**Ce qui n'est pas gagné :**

- **Le « hmm » n'est pas isolé.** Sur att2, le vrai partage serait `ʊ ɹ` = la prononciation non contractée de `you're`, `ə` = le hmm. L'alignement fait l'inverse : il met le hmm dans la case substitution et déclare les vrais sons « en trop ».
- **`very` est coupé en deux**, `ʊ ɹ v` sur `you're` et `ɹ i` sur `right`, parce que chaque son de l'apprenant doit appartenir à un mot du modèle.

## État 5 — plus un bloc n'appartenant à aucun mot

Symétrique de `join.partition`, où un mot peut ne recevoir aucun son : ici une suite de sons peut n'appartenir à aucun mot. Chaque son ainsi laissé de côté coûte un prix fixe.

Balayé sur les cinq prises :

| prix d'un son intercalé | att1 `ɑ` (substitution) | att3 `ʊ ɹ` (prononciation) | att4 `v ɛ ɹ i` (mot inséré) | att5 `aɪ m` (mot substitué) |
|---|---|---|---|---|
| 1,0 | **sorti** ✗ | **sorti** ✗ | sorti ✓ | **sorti** ✗ |
| 1,5 | gardé ✓ | gardé ✓ | **gardé** ✗ | gardé ✓ |
| 2,0 | gardé ✓ | gardé ✓ | **gardé** ✗ | gardé ✓ |

**Aucun prix ne marche.** Trop bas, tout ce qui diverge sort, deux substitutions pures comprises. Trop haut, plus rien ne sort jamais. Il n'y a pas de valeur intermédiaire, et ce n'est pas un défaut de réglage.

## Le mur, qui est le même à chaque état

`ʊ ɹ` (une prononciation non contractée) et `v ɛ ɹ i` (un mot en plus) ont **la même forme au niveau des symboles** : des sons que le mot du modèle n'a pas. Et le temps ne les sépare pas non plus — l'alignement forcé a étiré l'unique `ɝ` du modèle par-dessus le mot inséré, si bien que « very » tombe dans la fenêtre de `you're`.

La cause première est en amont : **le modèle prononce `you're` en un seul son `ɝ`, très contracté, et le locuteur en deux ou trois.** Il y a donc un surplus permanent dans ce mot, et tout ce qui s'y ajoute vraiment s'y cache. Que ce surplus soit marqué est correct — le modèle est la source de vérité, cf. `../reference.md`. Ce qui ne l'est pas, c'est d'en déduire « un son a été ajouté ».

## Pour reprendre

- L'instrument tourne tel quel : `cd bench && python3 anchor.py <turn.json>...`, et `-1.5` rallume le bloc intercalé à ce prix.
- Ce qu'il faudrait pour aller plus loin n'est pas un nombre mieux réglé, c'est **un troisième signal** qui distingue un mot en plus d'une réalisation plus longue. Deux pistes non éprouvées : un mot inséré occupe un temps comparable à un mot du modèle, là où une syllabe non contractée est plus courte ; et la matière intercalée est encadrée de part et d'autre par des mots qui, eux, concordent exactement.
- Et du matériau : cinq prises d'une phrase ne peuvent rien trancher. Il en faut portant des mots insérés variés, et des mots du modèle dont la contraction ne crée pas de surplus permanent.
