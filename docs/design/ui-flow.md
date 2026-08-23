# Le flux et l'écran

Ce que la conception d'interface a tranché, de bout en bout. Les principes qu'elle applique vivent dans `../reference.md` ; ici, leur forme.

## La posture d'usage

**Mixte assumé** : on converse en mode appel — téléphone posé ou en poche, les yeux ailleurs — et on revient à l'écran quand une marque mérite le détour. Les deux conséquences structurantes :

- **Les marques attendent bien.** Rien ne presse jamais ; ce qu'on trouve en ramenant les yeux est l'état du tour, pas une notification périmée.
- **Les parenthèses sont traversables à l'oreille seule.** La grammaticale (l'IA introduit en une phrase, on reformule) comme celle de prononciation (préambule d'un mot, modèle, redite, verdict sonore) : une session peut se vivre sans regarder l'écran, qui ne sert qu'au détail des marques et aux réglages.

## Le micro

**Armement automatique** : le micro s'ouvre dès que l'IA finit de parler, un seul tap clôt le tour — le rythme d'une conversation, un geste par tour, faisable à l'aveugle (grande cible en bas d'écran). Les silences de tête et de queue sont **rognés localement** avant envoi (trim à l'énergie, jamais de VAD en cours de parole) : l'analyse se facture à la durée, les silences de réflexion ne se paient pas. L'état « micro ouvert » est non ambigu, visuellement et sonorement. **L'armement manuel reste une option d'app** pour qui refuse tout enregistrement non voulu.

## L'écran de conversation

**Vue au présent** : l'échange courant occupe la majeure partie ou la totalité de l'écran — le tour de l'utilisateur en grand avec ses marques, la réponse de l'IA (floutée si le réglage l'est). Le fil complet est accessible **en scrollant**, trace et non théâtre. L'écran d'entrée de session est **les trois curseurs** : les régler, c'est démarrer.

## Ce qu'un tour affiche et où mène l'appui

La porte grammaticale garantit qu'un tour ne porte jamais les deux genres de marques :

- **Tour marqué en formulation** → la portion fautive colorée, aucune marque sonore (l'analyse n'a pas tourné). Appui → parenthèse de formulation.
- **Tour propre** → marques de prononciation éventuelles. Appui n'importe où sur le tour → parenthèse de prononciation, **à l'échelle de la phrase**, le mot le plus divergent pré-focalisé.

L'appui ne peut donc pas ouvrir la mauvaise parenthèse : le genre est une propriété du tour.

## Le marquage — silence sous le seuil, gradation au-delà

Rien n'est teinté dans la zone de bruit (±5 d'écart au modèle) : un tour réussi est visuellement calme, et une nuance affichée là serait un mensonge de précision. Au-delà du seuil, **la marque porte son intensité** — écart faible en teinte douce, écart profond en teinte franche. Par échelle : **phonème** gradation pleine (points d'écart), **accent** binaire par nature (la syllabe forte est au bon endroit ou pas), **mélodie** sans seuil ni teinte, son silence étant géométrique — cf. ci-dessous.

### La forme retenue : le fil derrière le texte

Tranchée sur prototype — maquette HTML sur de vrais tours multi-fautes, puis rendu Compose. Les trois échelles cohabitent sur la même chaîne, chacune dans son canal graphique :

- le **phonème** colore ses lettres, en rampe de teinte à clarté fixe (ambre → rouge selon les points d'écart) — une lettre à deux sons prend la couleur du pire, une lettre muette reste neutre. La rampe a sa clarté propre, distincte de celle de l'encre neutre : posée à la clarté du texte, une teinte s'éteint et la faute se peint invisible.
- l'**accent** met en graisse la syllabe que **le modèle** accentue, et n'ajoute rien tant que l'apprenant tombe juste ; sur une faute, deux réglettes sous la ligne — rouge sous la syllabe où l'accent est parti, verte sous celle où il devait tomber. Le vert ne valide donc jamais rien, il désigne une destination.
- la **mélodie** est un fil qui passe **derrière le texte**, dans la bande de la ligne : contour du modèle en trait désaturé, contour de l'apprenant en trait vif, même épaisseur, teinte froide propre au canal et hors de la rampe phonémique. Les lettres sont détourées de la couleur du fond, si bien que le fil passe dessous au lieu de les barrer.

Trois propriétés ont décidé la forme :

- **la continuité traverse les espaces.** La mélodie court sur l'énoncé entier ; le fil ne s'interrompt qu'au retour à la ligne, et là où le filtre d'accrochage a jeté la hauteur d'une syllabe — un trou plutôt qu'un raccord inventé.
- **le juste est silencieux par géométrie.** Quand l'apprenant colle au modèle, les deux traits se superposent en un seul. Aucun seuil n'est nécessaire pour qu'un tour propre soit calme, et deux traits neutres n'affirment rien : la règle du silence protège d'une teinte qui accuse, pas d'une référence qui montre.
- **aucun coût de hauteur.** Passer derrière le texte plutôt qu'au-dessus laisse la conversation compacte, ce qui compte sur la vue au présent.

**La graisse décrit le modèle, jamais la production.** Elle n'accuse personne, ne change pas de place d'un tour à l'autre pour le même énoncé, et se lit sans légende. Elle suit néanmoins le sort des autres marques : elle naît de l'analyse, donc un tour non analysé — quota, panne, porte grammaticale fermée — n'a pas de gras non plus.

**Le curseur de seuil ne pilote plus l'affichage de la mélodie** : il gouverne la rampe phonémique et l'ouverture automatique de la parenthèse, qui a bien besoin d'un seuil en demi-tons pour savoir quand se déclencher. À l'écran, l'écart mélodique se lit tel qu'il est.

## Les signaux sonores

Deux natures de faute, deux natures de son : la marque de formulation s'annonce par un **earcon discret** ; pour la prononciation, **le modèle qui se joue est le signal** — précédé d'un préambule d'**un mot** nommant l'échelle à écouter (celle qui dépasse le plus son propre seuil, en relatif). Pas d'earcon par-dessus un modèle.

## La chorégraphie du tour — elle suit l'intention déclarée

Le verdict grammatical et la réponse sortent du **même appel LLM** (~2 s) ; l'analyse sonore, elle, arrive après (synthèse du modèle + deux appels moteur). Ce qu'on joue dépend du mode du curseur concerné, et la règle est la même pour les deux genres :

- **Curseur en mode marquage** (le défaut) : la réponse se joue immédiatement — reprise glissée dedans pour la grammaire — et les marques se **révèlent à la fin de la réponse**, dans le silence où l'on reprend la parole. Rien n'interrompt jamais la voix de l'IA.
- **Curseur en mode auto-déclencheur** (= « je travaille cet aspect aujourd'hui ») : **le travail passe avant la réponse.** Grammaire : la parenthèse s'ouvre avant toute réponse jouée, et l'IA répond ensuite au tour réparé — la reprise dans la réponse n'a plus d'objet. Prononciation : la réponse attend le verdict d'analyse (générée en parallèle, retenue à la lecture) ; sous le seuil, la parenthèse s'ouvre — préambule + modèle — avant toute réponse ; sinon la réponse part, avec quelques secondes de plus que les 2,6 s nominales, prix assumé de l'intention déclarée.

L'ouverture automatique **est la parenthèse ordinaire** — même primitive, même sortie, mêmes règles de remplacement — jamais un mécanisme séparé.
