# Les voix : l'étalon se choisit, les personnages se tirent

Doc transitoire, ouvert le 2026-09-10. Il porte la séparation entre la voix qui sert de modèle à imiter et les voix de ceux qui parlent, le tirage qui donne les secondes, et le catalogue interne dont les fiches premium nomment les entrées. Il s'élague quand le code est en place et que `activity.md` et `reference.md` ont repris ce qui leur revient.

## Ce qui est en place aujourd'hui, et ce qui change

Une seule voix sert tout : `TurnPipeline` appelle `synthesis.voice()` pour la réplique de l'IA comme pour le modèle à imiter, et elle vient du réglage `Secret.SynthesisVoice`. `reference.md` avait déjà écrit la séparation — *une activité peut porter sa propre voix de conversation, et la voix d'un personnage ne sert jamais d'étalon* — sans rien derrière.

Ce qui change tient en une phrase. **La voix de référence reste choisie par l'utilisateur et ne dit plus que la phrase à imiter ; les répliques des personnages sont dites par des voix que l'utilisateur ne choisit pas.** L'étalon garde donc son test d'étalonnage et son écran ; une voix de personnage n'en passe aucun, n'ayant aucune mesure à porter.

## Ce que les fournisseurs publient

Mesuré le 2026-09-10, sur ce que chaque listage rend :

| fournisseur | source de la liste | genre | accent |
|---|---|---|---|
| Azure | `/voices/list` de la région | oui, `Gender` | oui, la locale |
| ElevenLabs | `/voices` de la clé | oui, `labels.gender`, parfois vide | texte libre, `labels.accent` |
| Inworld | `/voices/v1/voices` | oui, `male`/`female`, aucune vide | oui, `languageCode` |
| Replicate | l'énumération du schéma du modèle | non | non |

Le cas Replicate n'est pas un manque du code : la liste est une enum de schéma, vingt prénoms nus, et rien d'autre n'y sera jamais publié.

**Le relevé Inworld, qui sert plus loin** : 282 voix, dont 159 anglaises sur sept locales (en-US 96, en-GB 23, en-IN 11, en-AU 11, en-SG 7, en-PH 6, en-NG 5), 91 hommes et 68 femmes, un `ageGroup` sur trois valeurs (`young` 37, `middle_aged` 111, `elderly` 9, deux vides), une phrase de description par voix, et **226 tags distincts** dont plus de la moitié n'apparaissent qu'une fois. Ces tags mélangent quatre choses sans rapport dans une seule liste : des cas d'usage (`customer support`, `rpg`), du grain (`gravelly`, `breathy`), du caractère (`menacing`, `flirtatious`) et de l'accent (`british`, `posh`, `new york`). C'est du texte de vitrine ; aucune app ne peut interroger ça.

## Le tirage, hors premium

Le vivier est le catalogue du fournisseur en cours, restreint aux voix anglaises. Deux filtres s'y appliquent, chacun **seulement là où le fournisseur publie de quoi le trancher** :

- **le genre du personnage**, qui existe toujours — la fiche le déclare, sinon l'écran de lancement le demande, sinon il se tire (`Activity.GENDERS`) — et qui est déjà dit au personnage dans sa mise en scène ;
- **l'accent**, qui est celui de la **voix de référence**, jamais saisi ailleurs, et qui ne s'applique que si l'utilisateur a demandé que ses interlocuteurs parlent comme son modèle.

Lire l'accent de la voix de référence ne coûte aucun appel de plus : le tirage va chercher le catalogue de toute façon, et la voix de référence y est, avec sa locale.

**Un filtre peut vider la liste.** On relâche alors l'accent d'abord, le genre ensuite ; une liste encore vide est un `ChainFailure` et jamais un tirage dans une autre langue. L'ordre dit ce qui compte : le genre est dit au personnage dans sa mise en scène, donc un désaccord s'entend comme une faute de l'app, là où un accent qui diverge est une nuance dont `activity.md` fait même un levier de compréhension.

**Un tirage par personnage, sans remise** : deux personnages d'une même scène ne peuvent pas recevoir la même voix, sinon rien ne dit plus qui parle.

**Ce qui est tiré entre au journal** (`Activity.chosen`), au même titre que le tirage de genre. C'est le critère du projet appliqué : ce qui dépend d'un aléa que rien ne reproduit se stocke.

## Ce qui est stocké, et quand une voix se retire

Sur chaque personnage de la conversation, on ne garde pas la voix seule mais **ce sous quoi elle a été tirée** : le fournisseur, le slug, le genre honoré, l'accent honoré. C'est ce qui rend toutes les vérifications locales — sans ce relevé, savoir si la voix stockée a encore le bon accent demanderait un appel de catalogue à chaque tour.

Une voix se retire dans trois cas, et la règle générale est que **relâcher une contrainte ne retire jamais, resserrer retire** — une voix tirée sous une contrainte reste membre légitime d'un vivier plus large.

- Aucune voix n'a encore été tirée pour ce personnage.
- Le fournisseur de synthèse a changé : le slug ne veut plus rien dire ailleurs.
- Une contrainte s'est resserrée : la préférence d'accent vient d'être cochée, ou la voix de référence a changé d'accent.

**Une voix disparue chez le fournisseur n'est pas détectée, et c'est délibéré.** La vérifier demanderait un appel de catalogue par tour, pour un cas dont la fréquence est basse et qui vient toujours d'un geste de l'utilisateur — retirer une voix de sa bibliothèque, changer de modèle. La synthèse échoue alors comme n'importe quel maillon : l'enregistrement du tour est gardé, on renvoie. Le jour où ça mord, on aura un vrai message d'erreur sous les yeux, ce qui est exactement le matériau qui manque pour écrire mieux.

**Le prix, à ne pas cacher** : une conversation dont un personnage a perdu sa voix est bloquée, et l'apprenant n'a aucun geste pour la débloquer puisqu'il ne choisit pas ces voix. C'est le coût de « les voix des personnages ne sont pas au choix ». Ce qui se fait tout de suite et ne coûte rien : que l'échec de synthèse **d'une réplique de personnage** soit distinguable de celui du modèle à imiter, pour que la réparation future ait un endroit où s'accrocher.

## Le catalogue interne

Un catalogue tenu par l'app, qui ne décrit que ce qui est le même pour tout le monde. **Il ne couvre que le premium**, donc les voix système d'Inworld. La bibliothèque ElevenLabs est celle de l'utilisateur et ne peut par nature rien recevoir de nous ; les autres fournisseurs restent au tirage grossier ci-dessus.

**Cinq axes nommés, à vocabulaire fermé.** Les 226 tags d'Inworld sont ce qui tranche : une liste libre ne garantit pas qu'une voix grave soit dite `deep` plutôt que `resonant` ou `rich`, donc rien ne s'y interroge.

- **genre**, **âge**, **accent** — des faits que le fournisseur publie, et qu'on **recopie** dans l'entrée par souci de symétrie : une entrée répond à ses cinq axes de la même façon, sans qu'on ait à savoir lesquels viennent d'un tiers.
- **grain** — le timbre, jugé à l'oreille.
- **registre du monde** — trois valeurs : **ordinaire** (une voix qu'on croise dans la rue), **de métier** (humaine mais posée, radio, documentaire, annonce), **figurée** (les mondes imaginaires : créature, caricature, théâtral, menaçant). La médiane existe parce que `polished`, `authoritative`, `news` et `corporate` sont massifs chez Inworld et ne sont ni des voix de la rue ni des voix de fiction.
- **tempérament** — le caractère par défaut, jugé à l'oreille.

Les deux axes jugés sont **facultatifs** : le catalogue se remplit par vagues au lieu d'exiger 159 écoutes avant sa première utilité. La description en prose du fournisseur se conserve telle quelle à côté, lisible par un humain et matière du voice design ; rien ne l'interroge jamais.

**La correspondance d'une entrée est une table indexée par (fournisseur, accent), jamais un slug unique.** La résolution descend : le couple exact, à défaut n'importe quel slug de ce fournisseur, à défaut l'entrée n'est pas jouable là. Rien n'est à remplir d'avance — une entrée à une seule ligne se comporte comme si la table n'existait pas.

**L'accent est le seul axe qui varie à l'intérieur d'une entrée**, et la raison tient en une phrase : c'est le seul que l'app impose de l'extérieur, venant d'un réglage de l'utilisateur et non de l'auteur. Faire varier n'importe quel autre axe, c'est changer de personne. Un vieux bourru et une vieille bourrue sont deux entrées, qu'aucun filtre ne sait rassembler et qu'une liste écrite dans la fiche rassemble.

## Les fiches premium

**Une fiche porte un drapeau premium**, et elle nomme ses voix.

**Trois façons de remplir le vivier, un seul mécanisme de tirage** — celui d'au-dessus, dont seule la source change :

- une **entrée nommée**, qui est un vivier de un : le personnage est le même à chaque partie, ce qui est la promesse que le premium vend ;
- une **liste d'entrées nommées**, pour garder de la variabilité sur ce qu'aucun filtre ne cible ;
- un **jeu de tags**, dont le vivier est le catalogue interne filtré.

**Une fiche premium peut imposer l'accent d'un personnage**, là où hors premium il est dérivé de la voix de référence. Une réceptionniste nigériane est un choix d'auteur, et le doc tient déjà qu'une voix difficile à suivre est un levier de compréhension.

**Rejouer une tuile premium redonne la même personne**, là où `activity.md` tient que rejouer un thème ne doit pas la redonner. Ce n'est pas une contradiction mais une frontière : le tirage sert la rencontre là où rien n'est écrit, la fiche premium sert l'inverse, un personnage qu'on retrouve.

**Une tuile premium ouverte hors premium s'affiche, ne se joue pas, et dit pourquoi.** Elle ne dégrade pas en tirant au hasard : la voix nommée *est* la promesse, la remplacer en silence la casse.

**Un vivier vide se voit à l'écriture, pas en jeu** : une entrée nommée qui n'existe pas, un jeu de tags qui ne trouve rien. C'est ce que `Definitions.validate` existe pour attraper, et il est un stub.

## Ce qui reste ouvert

- **Une passe de vérification hors ligne du catalogue interne**, côté dev : parcourir les entrées, redemander la liste au fournisseur, nommer les slugs disparus. Un slug qui meurt là casse notre contenu pour tout le monde, ce qui n'est pas le cas d'une voix retirée par un utilisateur de sa propre bibliothèque. Une option de `./run`, à lancer avant une livraison.
- **L'apprenant va entendre deux voix du côté de l'IA** — le personnage qui parle, l'étalon qui redit la phrase à imiter — là où c'est la même aujourd'hui. C'est voulu, et personne ne sait si ça se comprend à l'oreille ou si ça se lit comme un défaut. Ça se juge en parlant dedans.
- **`inworldVoices` lit un champ `accent` qui n'existe pas** dans leur réponse ; le repli sur `languageCode` sauve l'affichage, donc rien ne s'est vu. Et l'étiquette Inworld ne montre ni le genre ni l'âge, là où Azure et ElevenLabs montrent le genre.
- **Où vit le catalogue interne** — sa forme de fichier, sa place, sa version — n'est pas décidé, et se décidera en l'écrivant.
