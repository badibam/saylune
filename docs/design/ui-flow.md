# Le flux et l'écran

Ce que la conception d'interface a tranché, de bout en bout. Les principes qu'elle applique vivent dans `../reference.md` ; ici, leur forme.

**Ce doc précède `activity-model.md` et n'a pas été refait dessus.** Ce qui y est périmé est le **flux** : tout ce qui ouvre, traverse ou referme une **parenthèse** — « Ce qu'un tour affiche et où mène l'appui », l'ouverture automatique, et la chorégraphie du tour qui en dépend. Le remplacement, à écrire, est *redire sur place* ou une activité suggérée, et il attend le catalogue des activités, que le design range dans ce qui reste à spécifier. Ce qui y est **intact et se lit** : la posture, le micro et le rognage des plages vides, le marquage et sa gradation avec ses seuils mesurés, les chiffres de latence.

## La posture d'usage

**Mixte assumé** : on converse en mode appel — téléphone posé ou en poche, les yeux ailleurs — et on revient à l'écran quand une marque mérite le détour. Les deux conséquences structurantes :

- **Les marques attendent bien.** Rien ne presse jamais ; ce qu'on trouve en ramenant les yeux est l'état du tour, pas une notification périmée.
- **Le travail d'une marque est traversable à l'oreille seule.** Reformuler après une phrase de l'IA comme redire un mot après son modèle : une conversation peut se vivre sans regarder l'écran, qui ne sert qu'au détail des marques et aux réglages.

## Le micro

**Armement automatique** : le micro s'ouvre dès que l'IA finit de parler, un seul tap clôt le tour — le rythme d'une conversation, un geste par tour, faisable à l'aveugle (grande cible en bas d'écran). L'état « micro ouvert » est non ambigu, visuellement et sonorement. **L'armement manuel reste une option d'app** pour qui refuse tout enregistrement non voulu.

**Ce mode est une piste parmi quatre**, pas un tranché (cf. `../reference.md`, « Les deux tuyaux ») : ce qui est décidé est l'invariant — rien ne coupe qui parle encore, l'audio du tour est conservé — pas le geste qui l'applique.

**Les plages vides sont rognées localement avant envoi**, de tête, de queue et d'intérieur — rien ne distingue le milieu du bord une fois posé un **seuil de durée**, qui est ce qui rend la coupe sûre : une occlusive est du silence, mais elle dure 50 à 120 ms, et ne retirer que les plages de l'ordre de la demi-seconde place la coupe hors du domaine des phonèmes. Le gain n'est pas que financier : la mémoire d'une passe d'analyse croît comme le carré de la durée (`../analysis.md`). Le **seuil de niveau** reste à poser, et il demande des tours spontanés hésitants que le banc n'a pas.

## L'écran de conversation

**Vue au présent** : l'échange courant occupe la majeure partie ou la totalité de l'écran — le tour de l'utilisateur en grand avec ses marques, la réponse de l'IA (floutée si le réglage l'est). Le fil complet est accessible **en scrollant**, trace et non théâtre. L'écran d'entrée est **les réglages par direction** : les régler, c'est démarrer (`activity-model.md`).

## Ce qu'un tour affiche et où mène l'appui

La porte grammaticale garantit qu'un tour ne porte jamais les deux genres de marques :

- **Tour marqué en formulation** → la portion fautive colorée, aucune marque sonore (l'analyse n'a pas tourné). Appui → parenthèse de formulation.
- **Tour propre** → marques de prononciation éventuelles. Appui n'importe où sur le tour → parenthèse de prononciation, **à l'échelle de la phrase**. Aucun mot n'est désigné en entrant : toutes les fautes sont marquées et se retravaillent dans le même énoncé, donc en élire une trierait ce que rien ne trie.

L'appui ne peut donc pas ouvrir la mauvaise parenthèse : le genre est une propriété du tour.

## Le marquage — silence sous le seuil, gradation au-delà

Rien n'est teinté dans la zone de bruit (±5 d'écart au modèle) : une nuance affichée là serait un mensonge de précision. **Le seuil est bien placé et c'est mesuré** — sur les prises étiquetées, les sons calmes valent 0,01 à 2,2 points et la plus faible marque en vaut 25, donc la bande tombe au milieu d'un vide franc. Ce qui ne se vérifie pas, en revanche, c'est qu'un tour appliqué reste calme : la prise `15-right-clean` en porte cinq, jugées réelles par son auteur. Au-delà du seuil, **la marque porte son intensité** — écart faible en teinte douce, écart profond en teinte franche. Par échelle : **phonème** gradation pleine (points d'écart), **accent** binaire par nature (la syllabe forte est au bon endroit ou pas), **mélodie** sans seuil ni teinte, son silence étant géométrique — cf. ci-dessous.

**Le dégradé sature à 30 points, et la matière le dépasse largement.** Les marques relevées valent 25, 30, 63, 88, 93, 96 : au-delà de 30, tout est du même rouge, donc la gradation cesse de porter là où la plupart des marques tombent. Ce n'est un défaut que si l'on veut distinguer un écart de 40 d'un écart de 95 ; si le rouge dit « beaucoup de travail ici », la fenêtre actuelle suffit. À trancher en regardant l'écran, pas d'avance — c'est un paramètre de `MarkingColors.kt` (`NOISE_BAND` et le 25 qui le suit), pas une question de conception.

### La forme retenue : le fil derrière le texte

Tranchée sur prototype — maquette HTML sur de vrais tours multi-fautes, puis rendu Compose. Les trois échelles cohabitent sur la même chaîne, chacune dans son canal graphique :

- le **phonème** colore ses lettres, en rampe de teinte à clarté fixe (ambre → rouge selon les points d'écart) — une lettre à deux sons prend la couleur du pire, une lettre muette reste neutre. **Un son qui ne porte aucune lettre se peint quand même** : sur la lettre que le voisin lui a prise quand elle existe — le `x` de `boxes` s'allume pour son /k/ comme pour son /s/, la contrainte d'une lettre posée une seule fois étant celle de l'appariement et non celle de l'affichage —, et sinon sur **l'intervalle** entre ses deux voisines, un trait fin dans la gouttière. C'est le seul geste qui ne mente pas sur un son que rien n'écrit, et c'est justement le message qu'un schwa appelle : il se passe ici quelque chose que l'orthographe ne note pas. La rampe a sa clarté propre, distincte de celle de l'encre neutre : posée à la clarté du texte, une teinte s'éteint et la faute se peint invisible.
- l'**accent** met en graisse la syllabe que **le modèle** accentue, et n'ajoute rien tant que l'apprenant tombe juste ; sur une faute, deux réglettes sous la ligne — rouge sous la syllabe où l'accent est parti, verte sous celle où il devait tomber. Le vert ne valide donc jamais rien, il désigne une destination.
- la **mélodie** est un fil qui passe **derrière le texte**, dans la bande de la ligne : contour du modèle en trait désaturé, contour de l'apprenant en trait vif, même épaisseur, teinte froide propre au canal et hors de la rampe phonémique. Les lettres sont détourées de la couleur du fond, si bien que le fil passe dessous au lieu de les barrer.

Trois propriétés ont décidé la forme :

- **la continuité traverse les espaces.** La mélodie court sur l'énoncé entier ; le fil ne s'interrompt qu'au retour à la ligne, et là où le filtre d'accrochage a jeté la hauteur d'une syllabe — un trou plutôt qu'un raccord inventé.
- **le juste est silencieux par géométrie.** Quand l'apprenant colle au modèle, les deux traits se superposent en un seul. Aucun seuil n'est nécessaire pour qu'un tour propre soit calme, et deux traits neutres n'affirment rien : la règle du silence protège d'une teinte qui accuse, pas d'une référence qui montre.
- **aucun coût de hauteur.** Passer derrière le texte plutôt qu'au-dessus laisse la conversation compacte, ce qui compte sur la vue au présent.

**La graisse décrit le modèle, jamais la production.** Elle n'accuse personne, ne change pas de place d'un tour à l'autre pour le même énoncé, et se lit sans légende. Elle suit néanmoins le sort des autres marques : elle naît de l'analyse, donc un tour non analysé — quota, panne, porte grammaticale fermée — n'a pas de gras non plus.

**Le réglage d'élocution ne pilote plus l'affichage de la mélodie** : il gouverne la rampe phonémique et le **déclenchement automatique**, qui a bien besoin d'un seuil en demi-tons pour savoir quand se déclencher. Ce qu'il déclenche est du flux périmé ; le seuil, lui, est mesuré et tient. À l'écran, l'écart mélodique se lit tel qu'il est.

### Le seuil de la mélodie — un choix, pas une mesure

Le fil n'a pas de seuil : les deux contours sont toujours tracés et l'apprenant juge la distance. Un seul cas en réclame un — le réglage en **mode auto-déclencheur**, où l'app décide seule d'interrompre le cours ordinaire avant de jouer la réponse.

La grandeur mesurée est l'écart en demi-tons par syllabe, dont on retire d'abord **la médiane du tour** : une transposition d'ensemble n'est pas une faute, la mélodie étant une forme et non une hauteur. La médiane plutôt que la moyenne, parce que la moyenne se laisse définir par les fautes qu'on cherche — sur le tour multi-fautes du prototype les deux diffèrent de 0,9 demi-ton, et centrer sur la moyenne rétrécirait la faute tout en accusant d'un demi-ton les dix syllabes correctes. Rien n'est divisé ensuite : diviser par la dispersion effacerait l'amplitude, or une phrase juste de forme mais dite plate est précisément une faute de mélodie.

Ce qui reste se résume en **racine des carrés moyens** sur les syllabes valides, celles que le filtre d'accrochage n'a pas jetées. Le carré pèse ce qui décroche franchement, là où une moyenne noierait une montée finale ratée dans dix syllabes correctes. Aucun résidu ne compte pour plus de 8 demi-tons — sans ce plafond, un artefact de suivi survivant au filtre porterait la décision à lui seul, puisque le carré l'amplifie.

Deux conditions, la racine des carrés étant aveugle à la position : elle donne le même chiffre à un écart au milieu de la phrase et au même écart sur la dernière syllabe, alors que seul le second transforme une affirmation en question.

- le déclenchement a lieu si la racine des carrés du tour dépasse **2,5 demi-tons** ;
- ou si l'écart sur les **deux dernières syllabes** dépasse **4 demi-tons**.

**Ces deux valeurs sont choisies, pas mesurées.** Les calibrer exigerait des tours spontanés étiquetés corrects, ce que l'architecture rend impossible : étiqueter une prise spontanée demanderait le modèle qu'elle n'a justement pas entendu. Elles sont posées haut délibérément — en auto-déclencheur, une fausse alerte n'est pas une marque qu'on ignore, c'est une interruption avant la réponse. Deux repères les encadrent : 8,6 demi-tons séparent une déclarative de la même phrase en question sur la syllabe finale, et le tour multi-fautes du prototype rend 3,2. L'usage les corrigera ; rien ne les rendra exactes.

## Les signaux sonores

Deux natures de faute, deux natures de son : la marque de formulation s'annonce par un **earcon discret** ; pour la prononciation, **le modèle qui se joue est le signal** — précédé d'un préambule d'**un mot** nommant l'échelle à écouter (celle qui dépasse le plus son propre seuil, en relatif). Pas d'earcon par-dessus un modèle.

## La chorégraphie du tour — elle suit l'intention déclarée

Le verdict grammatical et la réponse sortent du **même appel LLM** (~2 s) ; l'analyse sonore, elle, arrive après (synthèse du modèle + deux appels moteur). Ce qu'on joue dépend du mode du curseur concerné, et la règle est la même pour les deux genres :

- **Curseur en mode marquage** (le défaut) : la réponse se joue immédiatement — reprise glissée dedans pour la grammaire — et les marques se **révèlent à la fin de la réponse**, dans le silence où l'on reprend la parole. Rien n'interrompt jamais la voix de l'IA.
- **Curseur en mode auto-déclencheur** (= « je travaille cet aspect aujourd'hui ») : **le travail passe avant la réponse.** Grammaire : la parenthèse s'ouvre avant toute réponse jouée, et l'IA répond ensuite au tour réparé — la reprise dans la réponse n'a plus d'objet. Prononciation : la réponse attend le verdict d'analyse (générée en parallèle, retenue à la lecture) ; sous le seuil, la parenthèse s'ouvre — préambule + modèle — avant toute réponse ; sinon la réponse part, avec quelques secondes de plus que les 2,6 s nominales, prix assumé de l'intention déclarée.

L'ouverture automatique **est la parenthèse ordinaire** — même primitive, même sortie, mêmes règles de remplacement — jamais un mécanisme séparé.
