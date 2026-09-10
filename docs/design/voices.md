# Les voix : l'étalon se choisit, les personnages se tirent

Doc transitoire, ouvert le 2026-09-10. Il porte la séparation entre la voix qui sert de modèle à imiter et les voix de ceux qui parlent, le tirage qui donne les secondes, et le catalogue interne dont les fiches du mode payant nomment les entrées. Il s'élague quand le code est en place et que `activity.md` et `reference.md` ont repris ce qui leur revient. Le mode payant lui-même est décrit dans `premium-chain.md` et `distribution.md`.

## Ce qui est en place aujourd'hui, et ce qui change

Une seule voix sert tout : `TurnPipeline` appelle `synthesis.voice()` pour la réplique de l'IA comme pour le modèle à imiter, et elle vient du réglage `Secret.SynthesisVoice`. `reference.md` avait déjà écrit la séparation — *une activité peut porter sa propre voix de conversation, et la voix d'un personnage ne sert jamais d'étalon* — sans rien derrière.

Ce qui change tient en une phrase. **La voix de référence reste choisie par l'utilisateur et ne dit plus que la phrase à imiter ; les répliques des personnages sont dites par des voix que l'utilisateur ne choisit pas.** L'étalon garde donc son test d'étalonnage et son écran ; une voix de personnage n'en passe aucun, n'ayant aucune mesure à porter.

## Ce que les fournisseurs publient

Mesuré le 2026-09-10, sur ce que chaque listage rend :

| fournisseur | source de la liste | genre | accent |
|---|---|---|---|
| Azure | `/voices/list` de la région | oui, `Gender` | oui, `Locale` |
| ElevenLabs | `/voices` de la clé | oui, `labels.gender`, parfois vide | texte libre, `labels.accent` |
| Inworld | `/voices/v1/voices` | oui, `male`/`female`, aucune vide | oui, `languageCode` |
| Replicate | l'énumération du schéma du modèle | non | non |

Le cas Replicate n'est pas un manque du code : la liste est une enum de schéma, vingt prénoms nus, et rien d'autre n'y sera jamais publié.

**Le relevé Inworld, qui sert plus loin** : 282 voix, dont 159 anglaises sur sept locales (en-US 96, en-GB 23, en-IN 11, en-AU 11, en-SG 7, en-PH 6, en-NG 5), 91 hommes et 68 femmes, un `ageGroup` sur trois valeurs (`young` 37, `middle_aged` 111, `elderly` 9, deux vides), une phrase de description par voix, et **226 tags distincts** dont plus de la moitié n'apparaissent qu'une fois. Ces tags mélangent quatre choses sans rapport dans une seule liste : des cas d'usage (`customer support`, `rpg`), du grain (`gravelly`, `breathy`), du caractère (`menacing`, `flirtatious`) et de l'accent (`british`, `posh`, `new york`). C'est du texte de vitrine ; aucune app ne peut interroger ça.

**Ce que les adaptateurs doivent gagner.** `VoiceOption` ne porte aujourd'hui qu'un `id` et un `label`, où le genre et l'accent sont fondus dans du texte d'affichage. Il lui faut **deux champs structurés**, remplis par chaque adaptateur et nuls là où le fournisseur ne publie rien. Celui d'ElevenLabs demandera une normalisation de notre côté, son accent étant du texte libre — c'est le seul des quatre où la lecture est une supposition.

Non mesuré, à confirmer au premier essai : **ce qui marque une voix conçue sur un compte**. Les 282 rendues portent toutes `source: SYSTEM`, ce compte n'ayant encore conçu aucune voix.

## Les quatre cas

Deux régimes et deux états de la fiche, donc quatre viviers, et rien d'autre :

| régime | ce que la fiche dit | où on pioche |
|---|---|---|
| hors mode payant | rien | **le vivier sûr du fournisseur**, filtré genre + accent |
| hors mode payant | une entrée, une liste ou des tags | ignoré, donc même chose que ci-dessus |
| mode payant | rien | **le catalogue interne, registre ordinaire**, filtré genre + accent |
| mode payant | une entrée, une liste ou des tags | **le catalogue interne**, vivier selon ce qui est écrit |

**Deux filtres différents portent le mot ordinaire, et ils ne vivent pas au même endroit.** Le **registre** est notre axe, posé sur une identité de notre catalogue, donc il ne présume aucun fournisseur et survit à un changement de correspondance. Le **vivier sûr** est implémenté dans l'adaptateur d'un fournisseur : celui d'Inworld le construit en lisant son champ `categories`, les autres rendent leur liste anglaise entière, n'ayant rien de figuratif à écarter. Le premier est à nous et voyage ; le second est à eux et meurt avec eux.

**Le vivier sûr d'Inworld, mesuré.** Leur champ `categories` est un vocabulaire fermé de six valeurs : `enterprise`, `education_training`, `healthcare`, `developer_assistants`, `companions`, `interactive_media`. Aucune ne veut dire *ordinaire*, donc le sous-ensemble est notre choix : les quatre premières donnent **79 voix anglaises portant un seul marqueur figuratif**, quand `interactive_media` et `companions` en portent 30 sur 33. Le genre y reste équilibré (44 hommes, 35 femmes) et les sept locales survivent, chacune avec les deux genres — en-AU au minimum, à une voix de chaque.

## Le tirage

**Deux filtres**, chacun appliqué seulement là où le fournisseur publie de quoi le trancher :

- **le genre du personnage**, qui existe toujours — la fiche le déclare (`Character.gender`, déjà écrit), sinon l'écran de lancement le demande, sinon il se tire — et qui est déjà dit au personnage dans sa mise en scène ;
- **l'accent**, qui est celui de la **voix de référence**, et qui ne s'applique que si l'utilisateur a coché de préférer que ses interlocuteurs parlent comme son modèle.

**L'accent n'est jamais écrit dans une fiche hors du mode payant**, et il l'est dedans seulement comme critère du vivier ou comme coordonnée d'une ligne. Ce que la fiche déclare s'impose, ce qu'elle laisse ouvert suit le réglage — même forme que le genre, donc aucune question de priorité à trancher.

**Un filtre peut vider la liste.** On relâche alors l'accent d'abord, le genre ensuite ; une liste encore vide est un `ChainFailure` et jamais un tirage dans une autre langue. L'ordre dit ce qui compte : le genre est dit au personnage dans sa mise en scène, donc un désaccord s'entend comme une faute de l'app, là où un accent qui diverge est une nuance dont `activity.md` fait même un levier de compréhension. **L'ordre de relâchement est défini dans l'app et porte sur les axes** ; l'ordre d'écriture des tags dans une fiche n'a aucun effet, sans quoi un auteur qui réordonne changerait le comportement sans le voir.

**Un tirage par personnage, sans remise** : deux personnages d'une même scène ne peuvent pas recevoir la même voix, sinon rien ne dit plus qui parle.

**Ce qui est tiré entre au journal** (`Activity.chosen`), au même titre que le tirage de genre. C'est le critère du projet appliqué : ce qui dépend d'un aléa que rien ne reproduit se stocke.

## Ce qui est stocké, et quand une voix se retire

Deux endroits, et l'un des deux n'existe pas encore.

**Sur l'écran des réglages**, au moment où l'utilisateur choisit sa voix de référence, le `VoiceOption` complet est sous la main : on stocke **son accent et son genre à côté de son slug**. Le catalogue est déjà chargé là, donc c'est gratuit — et sans ça, connaître l'accent exigé demanderait un appel de catalogue à chaque tour.

**Sur chaque personnage de la conversation** : le fournisseur, le slug, et **l'accent exigé au tirage** — pas celui de la voix — ou la marque qu'il avait été relâché. Dans le cas honoré les deux coïncident ; dans le cas relâché, la marque évite de retirer à chaque tour pour rien. Le vivier de la fiche est copié là aussi, avec le reste de la distribution : `cast` est déjà recopié sur la séance pour qu'un fil se relise sans la version qui l'a produite, et sans le vivier une séance ne sait plus dans quoi elle a tiré ni dans quoi retirer.

Une voix se retire dans trois cas, et la règle générale est que **relâcher une contrainte ne retire jamais, resserrer retire** — une voix tirée sous une contrainte reste membre légitime d'un vivier plus large.

- Aucune voix n'a encore été tirée pour ce personnage.
- Le fournisseur de synthèse a changé : le slug ne veut plus rien dire ailleurs.
- L'accent exigé diffère de celui sous lequel on a tiré.

Toutes ces vérifications sont **locales**, sans aucun appel.

**Une voix disparue chez le fournisseur n'est pas détectée, et c'est délibéré.** La vérifier demanderait un appel de catalogue par tour, pour un cas dont la fréquence est basse et qui vient toujours d'un geste de l'utilisateur — retirer une voix de sa bibliothèque, changer de modèle. La synthèse échoue alors comme n'importe quel maillon : l'enregistrement du tour est gardé, on renvoie. Le jour où ça mord, on aura un vrai message d'erreur sous les yeux, ce qui est exactement le matériau qui manque pour écrire mieux.

**Le prix, à ne pas cacher** : une conversation dont un personnage a perdu sa voix est bloquée, et l'apprenant n'a aucun geste pour la débloquer puisqu'il ne choisit pas ces voix. C'est le coût de « les voix des personnages ne sont pas au choix ». Ce qui se fait tout de suite et ne coûte rien : que l'échec de synthèse **d'une réplique de personnage** soit distinguable de celui du modèle à imiter, pour que la réparation future ait un endroit où s'accrocher.

## Le catalogue interne

Un catalogue tenu par l'app, qui ne décrit que ce qui est le même pour tout le monde. Il couvre les **voix système d'Inworld**, aujourd'hui le seul fournisseur du mode payant. La bibliothèque ElevenLabs est celle de l'utilisateur et ne peut par nature rien recevoir de nous ; rien n'interdit d'y ajouter un jour une colonne Azure ou chatterbox, c'est une question de coût de curation et pas de structure.

**Il couvre les 159 voix système anglaises, et pas seulement les entrées qu'on nomme.** C'est le troisième des quatre cas qui l'impose : une séance payante dont la fiche ne dit rien pioche dans le registre ordinaire du catalogue, donc il lui faut un vivier et non une poignée de personnages écrits pour des histoires précises.

**Quatre axes décrivent une identité, à vocabulaire fermé.** Les 226 tags d'Inworld sont ce qui tranche : une liste libre ne garantit pas qu'une voix grave soit dite `deep` plutôt que `resonant` ou `rich`, donc rien ne s'y interroge.

- **genre** et **âge** — des faits que le fournisseur publie, et qu'on **recopie** dans l'entrée. La symétrie a un effet pratique : une entrée répond à ses axes de la même façon quelle que soit leur provenance, et un changement de fournisseur ne vide pas le catalogue de sa description.
- **grain** — le timbre, jugé à l'oreille.
- **tempérament** — le caractère par défaut, jugé à l'oreille.

Les deux axes jugés sont **facultatifs** : le catalogue se remplit par vagues au lieu d'exiger 159 écoutes avant sa première utilité.

**Le registre du monde** est un cinquième champ, de nature différente — il ne décrit pas une nuance de la voix mais dit dans quel monde elle a sa place. Trois valeurs : **ordinaire** (une voix qu'on croise dans la rue), **de métier** (humaine mais posée, radio, documentaire, annonce), **figurée** (créature, caricature, théâtral, menaçant). La médiane existe parce que `polished`, `authoritative`, `news` et `corporate` sont massifs chez Inworld et ne sont ni des voix de la rue ni des voix de fiction. Il se lit de la phrase de description que le fournisseur publie pour chaque voix, donc il se classe sans écouter, et se corrige à l'oreille là où la lecture hésite.

La description en prose du fournisseur se conserve telle quelle à côté, lisible par un humain et matière du voice design ; rien ne l'interroge jamais.

**La correspondance d'une entrée est une table dont la clé est (fournisseur, accent)**, jamais un slug unique. Chaque ligne porte en plus sa **portée** : voix système, atteignable par n'importe quelle clé du fournisseur, ou voix de compte, atteignable par le seul compte du projet. La résolution descend : le couple exact, à défaut une ligne système du même fournisseur, à défaut l'entrée n'est pas jouable là.

**L'accent n'est pas un axe, c'est la clé de la table**, et c'est ce qui le sépare des quatre autres : les axes disent qui est le personnage, la clé dit selon quelle dimension une même identité a plusieurs enregistrements. L'accent est la seule parce que c'est la seule dimension que l'app impose de l'extérieur, venant d'un réglage de l'utilisateur et non de l'auteur. Changer le genre, l'âge, le grain, le tempérament ou le registre, c'est changer de personne — donc une autre entrée, qu'une liste écrite dans la fiche rassemble.

**Un auteur ne distingue jamais une ligne système d'une ligne de compte** : il nomme un personnage. C'est le catalogue qui sait ce qui est atteignable par qui, et c'est le bon endroit — l'auteur d'une fiche n'a pas à savoir dans quel compte tourne l'app qui la jouera.

## Ce qu'une fiche écrit

**Une fiche porte un drapeau, déclaré.** Levée, elle ne se joue que dans le mode payant ; baissée, ce qu'elle dit des voix est une préférence, honorée là où elle se résout et remplacée par le tirage ordinaire sinon. La promesse — *cette voix ne se remplace pas* — est donc portée par le drapeau, au niveau de la fiche.

**Trois façons de remplir le vivier, un seul mécanisme de tirage** — celui d'au-dessus, dont seule la source change :

- une **entrée nommée**, qui est un vivier de un : le personnage est le même à chaque partie. Nommer une entrée fixe du même geste son genre, son âge, son grain et son tempérament ; seul l'accent choisit encore une ligne.
- une **liste d'entrées nommées**, pour ce qu'aucun filtre ne sait rassembler — un personnage très typé jouable en deux genres est deux entrées, et le tirage entre elles honore le genre du personnage.
- un **jeu de tags**, dont le vivier est le catalogue interne filtré.

**Rejouer une tuile qui nomme une entrée redonne la même personne**, là où `activity.md` tient que rejouer un thème ne doit pas la redonner. Ce n'est pas une contradiction mais une frontière : le tirage sert la rencontre là où rien n'est écrit, la fiche qui nomme sert l'inverse, un personnage qu'on retrouve.

**Un vivier vide se voit à l'écriture, pas en jeu** : une entrée nommée qui n'existe pas, un jeu de tags qui ne trouve rien. C'est ce que `Definitions.validate` existe pour attraper, et il est un stub.

## Ce qui reste ouvert

- **Une passe de vérification hors ligne du catalogue interne**, côté dev : parcourir les entrées, redemander la liste au fournisseur, nommer les slugs disparus et les voix apparues depuis la dernière passe. Un slug qui meurt là casse notre contenu pour tout le monde, ce qui n'est pas le cas d'une voix retirée par un utilisateur de sa propre bibliothèque. Une option de `./run`, à passer avant une livraison, et à nommer dans la discipline de release pour qu'elle ne dépende pas de qui s'en souvient.
- **`inworldVoices` lit un champ `accent` qui n'existe pas** dans leur réponse ; le repli sur `languageCode` sauve l'affichage, donc rien ne s'est vu. Et l'étiquette Inworld ne montre ni le genre ni l'âge, là où Azure et ElevenLabs montrent le genre.
- **Où vit le catalogue interne** — sa forme de fichier, sa place, sa version — n'est pas décidé, et se décidera en l'écrivant.
