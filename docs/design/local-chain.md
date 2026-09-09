# La chaîne locale — le montage cible et le plan de mesure

Doc transitoire : il porte une conception écrite pour être implémentée, et il **s'élague quand le code est en place** — les commits deviennent alors la carte.

Ce qu'il ne porte pas, et qui vit ailleurs : la distribution de personnages et les deux sortes de voix, tranchées dans `../activity.md` ; le fenêtrage de la passe d'analyse, qui est une tâche du chantier 1 avec ses trois issues (`../../TODO.md`). Ce doc **dépend** du fenêtrage — le budget mémoire du local en découle — et ne le porte pas : deux endroits à tenir à jour, c'est celui qu'on oublie qui décide.

## Ce que ça change, et ce n'est pas la qualité

Les deux synthèses de l'app ne se séparent pas par leur exigence de qualité mais par **la structure de leur prix**.

Le **modèle à imiter** est une voix, distante, payée à l'usage, et incompressible : c'est l'étalon de toute la mesure, il doit s'étalonner, il ne descend pas en local.

La **voix du personnage** est une **distribution**, et une distribution est ce qu'un catalogue commercial fait payer par abonnement, pas à la consommation. Les deux issues par le distant sont donc de monter le mur BYOK d'un cran que le doc n'a jamais budgété, ou de vendre des jetons — ce que `../reference.md` exclut, et qui est aussi ce qui rend l'app publiable sur F-Droid. Le local casse ce dilemme : une voix locale se paie une fois, au téléchargement.

**D'où l'inversion du défaut.** Pour tous les autres maillons, le distant est le défaut et le local une option déclarée. **Pour la voix des personnages, le local est le défaut et le distant le luxe** de qui a l'abonnement. Sans ça, la distribution n'existe pour personne d'autre que l'auteur du projet : ce n'est pas une optimisation, c'est la condition d'existence de la fonction.

**Et ça supprime du travail annoncé plutôt que d'en ajouter.** `../activity.md` pose deux modes — l'apprenant met sa clé et n'a que les génériques, ou il achète des crédits et a tout. Le second est la revente. Le local en ouvre un troisième qui rend le second inutile : voix spécifiques, sans crédits, chez tout le monde. Le mode payant, les crédits et leurs plafonds n'existent pas encore ; ils n'ont plus à être écrits.

## Le montage cible

Maillon par maillon, en partant de ce qui est branché aujourd'hui.

- **Reconnaissance** — local candidat. C'est déjà la destination écrite (`../reference.md`) et la couture est déjà actée : un fichier en entrée, des mots avec leurs bornes en sortie, ni clé ni panne réseau dans le contrat. Ce qui bloque est une mesure, pas une conception. Gain d'argent quasi nul, la reconnaissance étant le maillon le moins cher ; ce qu'elle achète est que **l'audio d'un tour ne quitte plus jamais l'appareil**.
- **Modèle de langue** — inchangé pour l'instant, un seul appel. Le découpage en deux appels est **en réserve** et ne s'ouvre que sous condition (ci-dessous).
- **Synthèse de l'étalon** — distante, inchangée.
- **Voix des personnages** — moteur local multi-voix, plus un traitement du signal ordinaire : décalage de hauteur, décalage de formants, grain, réverbération. **Pas de conversion de voix neuronale** en première intention.
- **Diffusion** — la voix du personnage se joue par morceaux au lieu d'attendre la synthèse complète, et le fichier s'écrit pendant qu'elle joue, parce que réécouter la réponse de l'IA le réclame.

**Pourquoi le traitement du signal plutôt qu'un modèle de conversion.** Une conversion de voix garde la prosodie et les phonèmes de la source et remplace le timbre : elle donne un gobelin, jamais un accent écossais. Or c'est le timbre qui fait la distribution d'heroic fantasy, et le timbre est aussi ce que quelques transformations de signal font pour rien — sans poids à télécharger, sans licence à vérifier, et **en diffusion**, là où une conversion neuronale veut le plus souvent l'énoncé entier. S'y ajoute un argument de registre : l'app est du pixel doux, une police dessinée à la main, un jeu rétro. Une voix visiblement traitée y est du même bois ; une voix neuronale parfaitement humaine y serait le seul élément qui prétend au réel.

**Ce qui rouvre la conversion neuronale** : vouloir un timbre qui n'est pas à portée de décalage depuis la voix source. Vouloir la voix de **quelqu'un** en est le cas nommé, parce que la cible a un nom ; il y en a deux autres, la distance qu'un filtre ne franchit pas sans casser, et une distribution que le moteur ne fournit pas. La première écriture n'en gardait qu'un et le raisonnement était trop étroit.

Ce qui la referme, en revanche, tient : **une conversion a besoin d'une cible qu'elle puisse entendre**, et il n'existe aucun corpus de parole de gobelin. Sur de l'heroic fantasy elle n'a rien à viser, sauf à enregistrer quelqu'un — le cas nommé.

### Le parallélisme

Aujourd'hui la chaîne est strictement séquentielle : la synthèse entière est ramenée, jouée jusqu'au bout, et l'analyse ne part qu'ensuite (`conversation/TurnPipeline.kt`). **Pendant que le personnage parle, l'appareil ne fait rien**, et ça dure plusieurs secondes.

**Décidé** : la synthèse de l'étalon et la passe d'analyse tournent dans cette fenêtre. Le calcul se cache derrière la voix.

**Et l'affichage attend qu'elle se taise.** Le tour du personnage est un exercice d'écoute — c'est une des cinq aptitudes, et le levier de flou existe pour qu'on l'écoute au lieu de le lire. Des marques qui s'allument pendant qu'il parle tirent l'œil vers la ligne de l'apprenant au moment où il devrait tendre l'oreille. Ça ne coûte rien : les marques sont prêtes à l'instant où la voix s'arrête.

### Le découpage du modèle de langue, en réserve

L'idée : un appel A ne rend qu'`intended`, sur un modèle léger, ce qui laisse partir la synthèse de l'étalon en parallèle de l'appel B qui porte tout le reste.

**A ne rend qu'`intended`.** La réplique du personnage demande tout le contexte et c'est de la prose de jeu de rôle — la dernière chose à confier à un petit modèle.

**Et le découpage n'accélère pas la parole.** C'est la note qui décide si l'app joue la continuation ou l'écho de reprise (`TurnPipeline.kt`, `echoing`), donc la voix ne peut pas partir avant B, quel que soit le découpage. Il accélère la **mesure** — laquelle est déjà gratuite dès qu'elle se cache derrière la voix. **Il ne s'ouvre donc que dans un cas : quand la mesure ne tient pas dans la fenêtre de la réplique**, ce que la mesure 6 tranche.

Une exception exploitable : quand le réglage d'attente est éteint, il n'y a jamais d'écho, le choix n'en est plus un, et la continuation peut partir dès son retour.

Deux coûts à ne pas oublier le jour où il s'ouvre. Le tour dont la porte des mots se ferme paie une synthèse d'étalon qu'on n'a pas le droit de faire entendre — rien n'est violé, le doc interdit de *donner à imiter* une non-phrase et non de la calculer, mais c'est de l'argent, et la fraction se mesure. Et deux appels renvoient l'historique deux fois, sauf à ce qu'ils partagent leur préfixe **à l'octet près** et ne diffèrent que par leur dernière instruction. **Ce second point se prépare tout de suite** : écrire le prompt en une partie permanente et une queue courte ne coûte rien aujourd'hui et éviterait une réécriture.

### La mémoire

Deux choses différentes portent ce nom, et une seule se règle par l'ordre d'exécution.

**Les poids résidents** : un moteur ONNX les charge à la création de sa session et les rend à sa fermeture. En séquentiel **avec libération**, un seul jeu occupe la mémoire à la fois. C'est la fermeture qui compte, pas l'ordre ; le prix est le rechargement, que la mesure 5 chiffre.

**Le pic de calcul** : les 4316 Mo mesurés sur une minute d'audio sont les activations de la passe d'analyse, pas des poids, et rien de tout ceci ne les touche. Seul le fenêtrage les réduit.

**La règle qui en découle : le pic d'analyse ne coexiste jamais avec autre chose de gros.** L'ordre de la chaîne l'assure seul — l'appel A produit `intended`, dont l'analyse a besoin pour exister, donc A précède l'analyse par construction. Le seul recouvrement voulu est l'analyse derrière la voix du personnage, où un moteur de synthèse de quelques centaines de mégaoctets face à un pic de plusieurs gigaoctets est du bruit.

**Le local est une option déclarée**, au même titre que les poids d'analyse : un maillon local qui ne tient pas dans l'appareil s'éteint, **porte sa raison**, et retombe sur le fournisseur distant. On peut donc le livrer sans le faire tenir partout.

## Les critères, posés avant les chiffres

Chacun est un choix, écrit maintenant pour ne pas être négocié devant le nombre. Aucun n'est mesuré.

1. **Licence des poids.** Libre reconnue (DFSG, FSF ou OSI) ou le candidat est écarté. Un modèle *ouvert* n'est pas un modèle *libre* : les licences communautaires de Llama et de Gemma ne passent pas, Apache-2.0 et MIT passent. Ce n'est pas une mesure, c'est un filtre, et il passe en premier parce qu'il ne coûte rien.
2. **Voix de personnage.** Elle se **transcrit sans effort à la première écoute** par une oreille anglophone en aveugle, ou elle est écartée. Pas « elle est belle », pas « elle sonne humaine ». Une voix **étrange** passe — grave, éraillée, monstrueuse, lente : c'est de la difficulté d'écoute, que le doc accueille comme levier de compréhension. Une voix à **défauts de machine** ne passe pas — métallique, bourdonnante, saturée en sortie de filtre, mots avalés. La ligne est *une bouche pourrait produire ça ou non*, jamais *facile ou difficile*.
3. **Rapport au temps réel** de la synthèse locale plus filtre, sur le SM-G975F. La diffusion achète le premier son, **pas le débit** : un moteur plus lent que le temps réel déplace l'attente au milieu de la phrase, et une voix qui hoquette est pire qu'une voix qui démarre tard. Il faut de la marge, pas l'égalité.
4. **Nombre de timbres distincts** que le moteur multi-voix plus le traitement du signal rend vraiment — distincts jugés à l'oreille, pas comptés dans le catalogue. S'il y en a assez pour peupler des scènes écrites, **la conversion neuronale est abandonnée** et ne se rouvre que sur le cas nommé plus haut.
5. **Chargement et libération d'une session**, par modèle. Court, on ferme après chaque usage et la question de la mémoire disparaît. Long, il faut choisir qui reste résident. À vérifier plutôt qu'à croire, parce que ça changerait le calcul : si les poids sont projetés depuis le fichier plutôt que copiés, des pages en lecture seule se récupèrent sous pression au lieu de tuer le processus.
6. **La mesure tient-elle dans la fenêtre de la réplique** — durée de la synthèse de l'étalon plus la passe d'analyse, contre la durée d'une réponse jouée. Si oui, **le découpage du modèle de langue reste fermé**. Si non, il s'ouvre pour les tours longs.
7. **Fidélité d'`intended` d'un petit modèle**, au banc du juge (`grammar-test-set.md`, 25 énoncés étiquetés). Le critère est **asymétrique et c'est délibéré** : réparer une faute de l'apprenant est fatal — ça efface le signal d'apprentissage avant tout jugement —, ne pas réparer une méprise de la reconnaissance coûte un tour. Donc **zéro réparation de grammaire**, et pas plus de méprises laissées que le modèle de référence. S'y ajoutent trois consignes que la même sortie doit tenir : jamais de chiffres, ne jamais compléter un tour interrompu, ponctuer selon l'intention.
8. **Fidélité verbatim de la reconnaissance locale**, au banc de l'oreille. Whisper nu **répare** — sur *I sink* il rendra *think* — et c'est exactement ce que le projet ne veut pas. Un Whisper local n'est donc pas « le problème est résolu », c'est un candidat à passer au banc, et CrisperWhisper attend là pour le jour où le nu réécrit trop.

## L'ordre, et ce que chaque porte ferme

La suite est ordonnée par ce que chaque étape coûte, et chacune ferme des options avant que la suivante commence. Ce n'est pas un arbre : quatre binaires feraient seize branches, illisibles et périmées en une semaine.

1. **L'audit de licence** (1). Une lecture de fiches, pas une heure, et il peut écarter la moitié des candidats avant qu'on ait lancé quoi que ce soit. À faire aussi sur la façon dont les fournisseurs commerciaux bornent leur catalogue par palier — c'est une lecture de tarifs, et l'argument du prix par abonnement s'appuie dessus.
2. **La voix du personnage** (2, 3, 4), qui est ce que la session voulait. Un moteur local, quelques réglages de filtre, une oreille. Ferme la conversion neuronale ou l'ouvre.
3. **Le chargement des sessions** (5), qui ne dépend d'aucune des précédentes et décide de la forme du code partout.
4. **La fenêtre** (6), qui ferme ou ouvre le découpage du modèle de langue.
5. **Les deux bancs** (7, 8), les plus chers, et qui sont **les deux tiers restants du chantier 2** : ils sont écrits et n'ont jamais tourné. Ce doc ne crée donc pas un chantier neuf, il donne au chantier 2 une raison de démarrer.

Le téléchargement croît avec chaque maillon local — 359 Mo aujourd'hui pour l'analyse seule. Ce n'est pas une porte : c'est un opt-in, il est déjà là, et il est franchi une fois.


## Ce qui a été mesuré, et ce que ça a déplacé

Première session sur ce doc, 2026-09-08. Elle a mené les critères 1, 2 et 4, et ouvert une dimension que le doc n'avait pas.

### La distribution : le moteur ne la donne pas, le filtre si

**Le critère 1 ne trie presque rien** : un candidat écarté sur onze, `MMS-TTS` en CC-BY-NC. Ce qui trie est la taille du modèle contre un appareil de 2019. La GPL passe — le projet est lui-même en GPL-3.0-or-later —, ce que la première écriture du critère laissait croire l'inverse en ne nommant que les licences communautaires.

**Les 904 locuteurs d'un VITS ne font pas une distribution.** Écoutés, ce sont des voix humaines normales, moins variées que des humains, et rien pour de l'heroic fantasy. Le compte du catalogue n'est pas le compte des timbres, ce que le critère 4 disait déjà.

**Le filtre, lui, en fait une** (`../../bench/creature.py`). Sept boutons — hauteur, formants seuls, doublage désaccordé, modulation en anneau, saturation, souffle, réverbération — sur une voix ordinaire. Jugé à l'oreille : *pas ouf, mais pas mal, très raisonnable*. Le doc en nommait quatre ; les trois autres viennent du son de jeu, où la transposition est *l'ingrédient principal de toute voix de monstre*.

**Le moteur n'offre qu'un levier de diction, le débit.** Ses deux autres réglages d'échantillonnage sont mesurés sans effet audible sur cette voix, et l'irrégularité des durées **a une falaise** : 2,8 s de parole à zéro, 3,5 s à 1,2, 5,3 s à 2,0, **448 s à 3,5**. Un personnage qui porterait ce réglage dans sa fiche aurait besoin d'une borne dure.

### Le clonage est écarté, avec ses chiffres

**Chatterbox** transporte le timbre et une part du jeu — le clonage tire un style de sa référence, là où une conversion ne prend que le timbre. Distinction qui compte : c'est le clonage, pas la conversion, qui achète de la diction.

**ZipVoice en local** (Apache-2.0, 123 M de paramètres, ONNX 8 bits à 124 Mo) : mesuré à **2,0 fois plus lent que le temps réel** sur un poste, et **le coût croît comme le carré de la durée** — `1,0 × durée + 0,063 × durée²`, même forme que la passe d'analyse. À comparer aux **1,4 s** qu'ElevenLabs en direct met jusqu'au premier son sur l'appareil (`../providers.md`) : le local perd d'un facteur six, sur un poste, avant la pénalité du téléphone. Et il ne diffuse pas.

### Le grain, et sa cause

**Un modèle acoustique et son vocodeur entraînés séparément laissent un grain sur toute la parole.** Mesuré sur FastSpeech2 : la part d'énergie entre 5 et 10,5 kHz vaut trois à dix fois celle de Piper, **y compris sur une phrase sans aucune sifflante** — donc ce n'est pas un défaut des `s`, qui ne font que le révéler.

Ce n'est pas non plus le choix du vocodeur : trois essayés, le générique livré, celui affiné sur une voix, et BigVGAN qui ajoute ses propres parasites. Et deux traitements aval — dé-esseur, remplacement de la bande par du bruit — aident sans suffire.

**La cause est la couture.** Piper est propre, JETS est propre, FastSpeech2 gratte, et ce qui sépare les deux premiers du troisième est qu'ils sont **entraînés d'un bloc**. Le spectrogramme mel qui joint deux moitiés n'ayant jamais appris l'une de l'autre est où le défaut naît.

### La dimension que le doc n'avait pas : écrire la diction

Un filtre travaille sur de la parole déjà prononcée, donc **aucun réglage ne lui fait porter une intention**. Ce qui la porte est la prosodie, et elle se pilote si le moteur expose ses prédictions par phonème.

**Le montage, éprouvé de bout en bout** : un seul appel au modèle de langue rend la réplique **et ses marques** ; le phonémiseur donne les phonèmes, les frontières de mots et les accents ; des règles en tirent un facteur par phonème ; le modèle prédit, on multiplie, il synthétise.

Trois choses tranchées en chemin. **On module, on n'écrase pas** — les facteurs se calculent depuis les prédictions du modèle, qui garde ainsi tout ce qu'il sait de la langue ; des valeurs inventées le sortent de son domaine. **Le modèle de langue marque des mots, jamais des positions** dans une liste de phonèmes, où il se décale. Et **une borne sur chaque facteur** rend l'intelligibilité garantie par construction plutôt que surveillée après coup.

**Deux langages de marquage essayés, tous deux concluants.** Des balises dans la ligne — poids, contour, voix, silences — et un petit objet de nombres : débit, étendue de hauteur, niveau, puis un poids, une hauteur et un volume par mot qui s'écarte. **Un vocabulaire pauvre ne séparait pas les personnages** — trois fiches sur cinq rendaient un balisage identique au caractère près ; un vocabulaire riche les sépare toutes les cinq. Les nombres vont mieux là où le moteur parle déjà en nombres, et c'est le modèle qui choisit l'amplitude au lieu d'une table de constantes.

### Le mur

**Aucun modèle publié n'a les trois à la fois** — son propre, contrôle par phonème, plusieurs voix.

| | son propre | rythme par phonème | hauteur par phonème | voix |
|---|---|---|---|---|
| Piper, VITS en ONNX | oui | lisible seulement | non | 904 |
| VITS d'ESPnet | oui | **`dur` est un argument publié** | non, VITS n'en a pas | 108, ou par vecteur |
| JETS, LJSpeech | oui | par crochet sur ses prédicteurs | par crochet | **une seule** |
| FastSpeech2, LibriTTS | **non** | oui | oui | 2 456 |

Il n'existe **aucun JETS multi-locuteurs publié** : LJSpeech en anglais, deux modèles coréens, rien d'autre.

**Les deux pistes restantes.** Prendre le VITS d'ESPnet, qui donne les voix et le rythme et laisse la hauteur au traitement après coup. Ou entraîner un JETS multi-voix, qui donnerait les trois.

**Et une troisième, jamais essayée, qui ne coûte aucun téléchargement** : faire de `w_ceil` une **entrée** du fichier ONNX de Piper, comme son propre correctif en a fait une sortie. Si ça marche, c'est les 904 voix, le son propre et le rythme par phonème avec ce qui est déjà sur le disque.
