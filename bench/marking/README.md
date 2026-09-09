# Le balisage — ce qu'on a demandé au modèle, et ce qu'il a rendu

Gardé pour la même raison que `../tables/` : **la méthode est la garantie.** Un balisage produit par un modèle ne vaut que si l'on peut relire ce qu'on lui avait donné — la fiche du personnage, la conversation, le vocabulaire offert, et ce qu'on lui interdisait.

Le langage lui-même, et ce que chaque marque vaut en nombres, sont dans `../../docs/design/local-chain.md`. Ici il n'y a que la matière.

## Le montage

**Une seule réplique, la même pour les cinq** — *« I asked you twice, and you said nothing. »* — et le modèle ne l'écrit pas, il la **marque**. Cinq fiches réelles de la porte Libre plus une inventée, chacune avec sa propre conversation menant à cette phrase. Toute différence de balisage vient donc du personnage, et de rien d'autre.

C'est aussi la seconde moitié de la phrase du banc de créatures, donc les rendus se comparent à ceux qui existent déjà.

| dossier | vocabulaire offert |
|---|---|
| `prompts-plain/` | poids, traîné, deux silences — et deux garde-fous de comptage (« jamais plus de deux mots ») |
| `prompts-rich/` | trois familles séparées : poids, contour, voix ; les silences ; aucun garde-fou de comptage |
| `prompts-numbers/` | pas de balise : un débit, une étendue de hauteur, un niveau, puis un poids, une hauteur et un volume par mot qui s'écarte |

`answers/` porte les quinze réponses, une par fiche et par vocabulaire, dans la forme que chaque prompt demandait. Chacune garde **la ligne d'origine à côté de la ligne balisée** : retirer les marques doit rendre l'originale caractère pour caractère, ce qui est le seul contrôle automatique contre le défaut le plus silencieux — un modèle qui réécrit la phrase en la marquant. Vérifié sur les quinze, aucune ligne n'a bougé.

## Ce que la comparaison a montré

**Le vocabulaire pauvre ne sépare pas les personnages.** Un employé de guichet, un conteur qui divague et un voyageur de 1893 ont rendu **le même balisage au caractère près**. Seuls les deux extrêmes se distinguaient.

**Le riche les sépare tous les cinq**, et les nombres aussi. Ce qui manquait n'était donc pas la sensibilité du modèle, c'était le vocabulaire — et les deux garde-fous de comptage, retirés dans les deux autres versions, y contribuaient en poussant tout le monde vers la même réponse minimale.

**Les nombres portent deux grandeurs qu'aucune balise ne portait** : l'étendue de la hauteur et le niveau de la voix. Grukk resserre à 0,35 et descend de trois demi-tons, Dana reste à 0,55 et descend de deux — presque monocorde d'un côté, mélodie gardée de l'autre.

**Et c'est le modèle qui a choisi les amplitudes.** Avec les balises, l'amplitude est dans une table de constantes écrite à la main ; avec les nombres, elle vient de la fiche. Les cinq débits demandés vont de 1,15 à 1,45, dans l'ordre qu'on attendrait de leurs fiches.

## Une réserve sur ce que ce test ne dit pas

Il manque **le témoin** : les mêmes répliques rendues sans aucune marque, mélangées aux autres, pour savoir si les personnages s'entendent aussi bien sans. Sans lui, on ne sait pas si le balisage porte quelque chose ou si tout le travail est fait par le texte. Le fichier existe (`control` dans les rendus) mais l'écoute en aveugle n'a pas été menée.

Et la place du poids semble appartenir **à la phrase** plus qu'au personnage : les cinq ont marqué les deux mêmes mots. Ce qui varie est la manière, pas l'endroit.
