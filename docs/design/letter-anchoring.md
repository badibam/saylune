# L'ancrage aux lettres — état de l'exploration

Séance d'exploration, non conclue. Rien de ce qui suit n'est acté dans `analysis.md` ni dans `reference.md` : c'est un carnet, à élaguer quand les décisions seront prises.

**Le point de départ** : dissoudre l'attache à ElevenLabs. L'ancrage aux lettres vient aujourd'hui de ses horodatages au caractère, seul fournisseur à les rendre en REST nu. La direction tranchée est de le calculer sur l'appareil.

Tout ce qui suit tourne sur les seize phrases de `phrases.CALIBRATION`, voix `eleven-us-eric`, sauf `years-old` — *« I am 25 years old »* — écartée parce que le réseau lettres n'a pas de chiffres dans son vocabulaire. Le seul écart intérieur monstre du jeu venait d'elle : `years` ouvrait 242 ms trop tôt, le treillis n'ayant rien où dépenser les deux syllabes de « twenty-five ». **Un garde-fou reste à écrire dans l'app** : avant d'ancrer, vérifier que le texte est épelable, sinon ne pas ancrer et le dire. Sans lui, un chiffre oublié ne produit pas une marque absente mais une rafale de marques fausses.

## Ce qui est construit

- `bench/letters.py` — l'aligneur caractères. `facebook/wav2vec2-base-960h` (Apache-2.0), CTC à sortie lettres, contraint au texte connu. Rend un instant par caractère.
- `bench/anchor.py` — l'écart à ElevenLabs, caractère par caractère et mot par mot.
- `bench/join.py` — la brique 4 : quelles lettres chaque son couvre.
- `bench/expected.py` — l'annotation à la main : les lettres attendues par son, et les sons attendus par phrase. Ne rentre jamais dans l'app.
- `export.py` et `phone.py` portent maintenant l'un ou l'autre réseau.

## Ce qui est mesuré

**L'aligneur contre ElevenLabs.** Sur 363 lettres intérieures : médian **9 ms**, pire **59 ms**, et quatre des cinq pires sont des espaces. Le réseau lit par tranches de 20 ms, donc c'est la limite de l'instrument. Les bords de l'énoncé s'écartent de 160 ms, et c'est du silence — le fournisseur étire la première et la dernière lettre jusqu'aux bouts du fichier.

**L'arrondi.** Le fp16 rend exactement les chiffres du 32 bits ; l'entier 8 bits les détruit.

| lecture | taille | début de mot | fin de mot |
|---|---|---|---|
| PyTorch 32 bits | — | 8 ms | 11 ms |
| ONNX 32 bits | 379 Mo | 8 ms | 11 ms |
| ONNX 16 bits | 191 Mo | 8 ms | 11 ms |
| ONNX entiers 8 bits | 124 Mo | 144 ms | 103 ms |

Laisser la projection de sortie en flottant ne rattrape rien : le dommage est réparti dans tout l'encodeur.

**Sur l'appareil.** Chargement 282 ms, passe à 0,28 fois la durée de l'audio, relecture identique, pointe à **962 Mo**. Le verdict sur les matrices du téléphone est celui du poste, à la milliseconde près. Le réseau sons plafonnait à 930 Mo ; **ce que les deux font chargés ensemble n'est pas mesuré**, et l'ancrage se calculant une fois par entrée du cache de synthèse, rien n'oblige à les tenir chargés en même temps.

## Ce que la mesure contre ElevenLabs ne dit pas

Elle compare **deux approximations de la même chose**. La carte des caractères d'ElevenLabs n'est pas la vérité : c'est ce que leur moteur croit avoir dit, avec sa propre imprécision, que personne n'a chiffrée. Un accord à 9 ms ne dit pas qu'on a raison — il dit qu'on se trompe pareil, ou qu'on a tous les deux raison, et rien ne départage.

Et surtout, ce n'est pas la question. Ce dont l'app a besoin, c'est que les lettres s'accrochent à **notre propre carte des phonèmes**, celle sur laquelle le marquage se décide. Les deux cartes viennent de chez nous, lues sur le même audio, sur la même grille : aucune convention étrangère entre les deux.

## La jointure

Premier essai : demander quelles lettres tombent dans la fenêtre d'un son. Neuf sons sur quinze n'en recevaient aucune. **Les deux cartes sont des pics**, pas des étendues — le réseau pose un son sur deux trames et laisse le reste au blanc, l'aligneur fait pareil pour les lettres, et les deux pics ne tombent pas au même endroit.

Correction : élargir chaque pic jusqu'à mi-chemin de son voisin, des deux côtés, puis joindre par recouvrement. Résultat contre l'annotation : **135 sons sur 236 portent exactement les lettres attendues, soit 57 %.**

Ce chiffre est à prendre avec deux réserves. Une part inconnue des 101 ratés vient de mon annotation, discutable là où le réseau découpe autrement : sur `The`, j'ai écrit `ð→Th` puis `ɪ→e`, le réseau rend `ð→T` puis `ɪ→he`, et les deux se défendent — ce motif seul revient dans neuf phrases. Et 57 % ne devient un verdict que si l'on sait ce dont une marque a besoin : un décalage d'une lettre au bord d'une marque de syllabe n'a peut-être aucun effet visible, un son qui ne reçoit rien ne peut pas être marqué du tout, et **les deux cas sont mélangés dans le même chiffre**.

## Le vrai problème : la grille perd des sons

En amont de la jointure, la brique 3. Le décodage libre du modèle rend **223 des 253 sons attendus, soit 88 %** — 30 sons n'ont aucune case.

Les trous se concentrent sur les mots outils inaccentués : `to` perd sa voyelle quatre fois, `at` disparaît entièrement dans *« Turn right at the corner »*. Restent de vraies pertes de contenu : le `k` de *picked*, le `k` de *think* dans `think-sheep`, les deux `t` de *important*, le `d` de *Yesterday*.

**Là où la grille n'a pas de case, aucune marque ne peut se poser.** Si l'apprenant rate le `k` de *picked* et que la grille du modèle n'a pas de `k`, il n'y a rien à marquer.

## Deux pistes essayées

**Dilater l'audio** pour donner plus de trames à chaque son, avec `sox tempo` qui préserve la hauteur. Mesuré : 0,8 → aucun changement ; 0,7 → pire ; 0,5 → nettement pire. Les pics gagnés ne sont pas les sons manquants, ce sont d'autres sons ailleurs. Un seul algorithme testé, sur une seule voix.

**Lire plus fin.** `charsiu` lit à 10 ms au lieu de 20, sur le signal intact.

| modèle | sons retrouvés à l'identique | sons en trop |
|---|---|---|
| `timit-ipa` (20 ms) | 223/253 (88 %) | 13 |
| `charsiu` (10 ms) | 240/253 (95 %) | 12 |

Dix-sept sons de plus, sans produire davantage de parasites. Le compte et l'identité disent la même chose.

Ça ne désigne pas `charsiu` : il a été écarté pour deux raisons qui tiennent — il pardonne les substitutions, rédhibitoire pour ce qu'on lui demande, et ses poids n'ont pas de licence vérifiable. Ce que la mesure désigne, c'est **la finesse de lecture comme critère de choix du modèle acoustique**, qui n'en était pas un : `analysis.md` départage les candidats sur la séparation des fautes et sur la licence, jamais sur le pas de temps.

## Ce que valent ces chiffres

L'annotation des sons attendus est la mienne, et la table d'équivalences de `expected.py` est indulgente : elle déclare `ɛ`, `ɪ` et `ə` interchangeables, donc ferme les yeux sur la qualité des voyelles inaccentuées. Elle l'est pour les deux modèles de la même façon, la comparaison reste juste, mais le niveau absolu est optimiste.

## Ce qui reste ouvert

Le versant grille — pourquoi elle perd des sons, et la voie de sortie — a été diagnostiqué et planifié depuis : cf. `dense-grid.md`, qui porte aussi la géométrie de jointure rodée et la tolérance de la marque. Reste ouvert ici, côté lettres :

- **Corriger l'annotation** de `expected.py` là où elle est discutable, et recompter.
- **Les deux réseaux chargés ensemble**, ou la discipline charge-passe-décharge.
- **Le garde-fou des textes non épelables**, côté app.

## Rejouer

```
cd bench
python3 anchor.py -v                    # l'aligneur contre ElevenLabs
LETTERS_QUANTISED=1 LETTERS_RUNTIME=onnx python3 anchor.py
python3 export.py -n letters            # 32 bits puis 16 bits
python3 phone.py -n letters             # sur l'appareil
LETTERS_READING=phone-letters-fp16 python3 anchor.py
ACOUSTIC_MODEL=timit-ipa python3 join.py sheep-field
ACOUSTIC_MODEL=timit-ipa python3 join.py -a
```
