# La voix d'un personnage

Comment un personnage obtient sa voix, question laissée ouverte par `activity-model.md` (« Le personnage ») et par `../reference.md` (« L'accent »). Ce qui y était tranché ne bouge pas : **aucun nom de voix écrit en dur**, parce que ce qu'un fournisseur expose ne peut pas devenir une condition ; et **la voix d'un personnage ne sert jamais d'étalon**, ce qui la dispense de tout test.

Ce doc ajoute le mécanisme, vérifié chez ElevenLabs le 2026-09-04, et les plafonds chiffrés qu'il rencontre. Il montrait que la réponse **bifurque sur qui paye la clé** ; la bifurcation est tranchée depuis, et la réponse est **les deux**.

## La réponse : deux sortes de voix, deux modes qui coexistent (2026-09-05)

**Une voix est un fournisseur et un slug**, ce que le code fait déjà (`eleven-us-eric`, `azure-us-jenny`). Rien d'autre n'est nécessaire pour la nommer.

**Deux rôles, deux listes, et ils ne se croisent jamais.** La **voix de référence** — celle qui dit la phrase de l'apprenant, donc l'étalon de toute la mesure — est choisie **de son côté**, chez son fournisseur, et elle doit passer l'étalonnage. Les **voix de personnages** viennent du mode, et ne passent aucun test, puisqu'elles ne mesurent rien.

**Les voix génériques.** Un petit nombre — **quatre en première implémentation, deux d'homme et deux de femme** —, que l'apprenant choisit chez son fournisseur par le sélecteur qui existe déjà. Génériques veut dire **interchangeables** : un défi générique demande *une voix générique*, éventuellement d'un genre quand la scène le demande, et l'app en tire une au hasard sinon. **Une définition ne nomme donc aucune voix**, et l'invariant est tenu sans aucune machinerie de résolution — pas de propriétés déclarées, pas de catalogue à interroger, pas de liste classée de couples.

**Les voix spécifiques** sont collectées côté projet, chez le fournisseur qu'il choisit, et une définition les nomme par leur slug. **L'invariant ne s'applique pas à elles**, et c'est sa portée qui le dit : son problème était le roulement d'un tiers — un slug qui disparaît quand la clé change. Sur un fournisseur que le projet tient, le slug est stable par construction.

**Les deux modes.**

- **(a) L'apprenant met sa clé.** Voix génériques, donc accès aux défis génériques seuls.
- **(b) L'apprenant achète un pack de crédits.** Rien à configurer, tous les défis, avec les voix définies côté projet.

**Ce que ça supprime.** Toute la boucle d'ajout d'une voix partagée à une collection — `POST /v1/voices/add`, la correspondance couple public → identifiant local, les emplacements par plan, la sélection qui pourrit, la bibliothèque de l'utilisateur écrite sans qu'il l'ait demandé — **ne sert plus**. En (a) il n'y a pas de voix de bibliothèque ; en (b) les voix vivent dans le compte du projet et y sont ajoutées à la main, une fois. **L'app n'ajoute jamais de voix.** La section « Ce qui se fige au codage » et les plafonds ci-dessous restent au dossier pour le jour où quelqu'un rouvrirait cette porte ; ils ne décrivent plus le montage.

**En V1, il n'y a que le mode (a).** Ce qu'il faut prévoir est l'extension, et elle est mince : un fournisseur de plus dans `Provider`, dont le `needs` est un jeton de compte au lieu d'une clé — ce que ce doc écrivait déjà.

**Deux conséquences du mode (b), à poser maintenant parce qu'elles coûtent cher rétroactivement.**

- **`TetheredNet` se déclare.** Le mode (b) dépend d'un service que le projet seul fait tourner. F-Droid ne l'interdit pas, il exige la déclaration — et une anti-feature non déclarée est un motif de rejet.
- **Le paiement se fait hors de l'app.** Google Play Billing est une dépendance propriétaire, que la facette `fdroid` interdit sans condition. Le pack de crédits s'achète ailleurs, ou par un mécanisme libre.

## Ce qui se fige au codage, et ce qui se résout chez l'utilisateur

Une voix de la bibliothèque communautaire a une **identité globale** : un `public_user_id` (son auteur) et un `voice_id` public, les mêmes pour tout le monde. C'est ce couple qu'une définition d'activité peut porter — jamais un identifiant de collection, qui ne vaut que dans un compte.

Une voix partagée n'est pas appelable telle quelle : elle doit d'abord entrer dans la collection de qui l'appelle. L'ajout prend exactement ce couple, `POST /v1/voices/add/{public_user_id}/{voice_id}`, et rend un **nouvel identifiant local au compte**, qui est celui que la synthèse consomme. La boucle côté app, à la première rencontre d'un personnage : lister les voix du compte — ce que `providers/Catalogue.kt` fait déjà —, ajouter si la voix manque, et **garder la correspondance couple public → identifiant local**. Cette correspondance se garde parce que rien ne garantit que la liste des voix expose l'origine publique d'une voix ajoutée ; si elle l'expose, l'étape de mémoire disparaît.

La sélection en amont est du travail de banc, pas de l'app. Il existe un endpoint de voix partagées, distinct de la collection, avec des filtres — genre, âge, accent, langue, usage, descripteurs — et une URL d'aperçu par entrée. On balaie, on écoute, on retient les couples.

## Les plafonds sont ceux du BYOK, et ils sont durs

- **Les emplacements de voix** : 3 sur le plan gratuit, 10 sur Starter, 160 sur Pro. Une voix ajoutée en consomme un. Le nombre de voix distinctes qu'un bloc d'activités réclame est donc plafonné par le plan de **l'utilisateur**, pas par la conception.
- **Le plan gratuit ne peut pas ajouter de voix de la bibliothèque.** BYOK gratuit = aucun personnage à voix.
- **L'ajout écrit dans le compte de quelqu'un d'autre.** Douze voix qu'il n'a pas choisies apparaissent dans sa bibliothèque : c'est un effet de bord qui se demande, pas une lecture.
- **La sélection pourrit.** Un auteur peut retirer sa voix de la bibliothèque, et l'ajout échoue alors pour tout nouvel utilisateur — silencieusement, et sans qu'on le voie depuis le dépôt.

D'où la forme retenue pour la définition : **un personnage porte une liste classée de couples, pas une voix**. Elle répond aux deux derniers points d'un coup — le premier couple encore disponible gagne, et un utilisateur à dix emplacements joue un bloc qui en nomme trente en réutilisant ce qu'il a déjà. Le personnage garde son identité ; c'est sa voix qui dégrade, ce qui est le bon ordre de priorité.

## Les modifieurs

Rien chez ce fournisseur ne transforme une voix en une autre : pas de décalage de hauteur, pas de vieillissement, pas de bascule de genre. Ce qui existe modifie la **livraison**.

- **Les réglages par requête** — `stability`, `similarity_boost`, `style`, `use_speaker_boost`, `speed`. Ils ne coûtent aucun emplacement et se posent par personnage. Mais ils ne fabriquent pas d'identité : **deux personnages ne sortiront jamais d'une seule voix**, quel que soit le réglage.
- **Les balises audio du modèle v3**, inline dans le texte — `[whispers]`, `[shouts]`, `[laughs]`, `[sarcastic]`. C'est du modifieur **par réplique**, donc le grain d'un jeu : le même marchand murmure puis crie. C'est la forme que `../chatterbox-parameters.md` documente déjà pour l'autre fournisseur, et ce que `NOTES.md` appelle sous « Mise en valeur des sons ».
- **Les dictionnaires de prononciation**, référencés par appel. Utiles pour un nom inventé. À réserver à la voix du personnage : en imposer un à la voix modèle reviendrait à juger l'apprenant contre un dictionnaire, ce que toute l'architecture refuse.

**Le piège** : ces modifieurs se répartissent à l'envers de ce qui arrange. Les balises expressives vivent en v3, quand le fil de latence s'est refermé sur `flash-v2.5`, le plus rapide et le plus pauvre. **La latence de v3 en direct n'est pas mesurée** — et c'est ce chiffre qui dit si un personnage joué tient dans une conversation ou seulement hors du fil.

## Ce que l'hébergement change

Question arrivée par celle des voix, et qui y répond : **tous les plafonds ci-dessus sont des plafonds du BYOK.** Si la clé est celle du projet, un plan Pro à 99 $/mois donne 160 emplacements sur un seul compte, la sélection est identique pour tout le monde, aucun utilisateur n'a besoin d'un plan payant, et personne ne voit sa bibliothèque écrite.

**Le coût, lui, est le point dur.** La synthèse est presque tout : un tour dépense la réponse à dire plus `intended`, environ 300 caractères, quand la reconnaissance et le modèle de langue se comptent en millièmes. Facturé **0,10 $ les 1000 caractères, tarif plat sur tous les plans**, ça fait **3 centimes le tour**, 0,90 $ une séance de trente tours, et de l'ordre de **18 $ par mois** pour vingt séances. Le coût marginal est trop haut et trop variable pour être forfaitisé : **des crédits, pas un abonnement**.

Ce qui s'ajoute au passage, et qui pèse plus que l'argent : des comptes, de l'authentification et de la facturation, là où l'app n'en a aucun ; du contrôle d'abus, obligatoire dès que les sources sont publiques ; une disponibilité qui devient la sienne. Et si l'audio transite, la responsabilité de traitement d'une voix d'apprenant, sur une app où des mineurs sont probables.

Le code, lui, encaisse : les fournisseurs se choisissent déjà maillon par maillon, donc un service hébergé n'est **qu'une entrée de plus dans `Provider`**, dont le `needs` est un jeton de compte au lieu d'une clé. Les deux variantes tiennent dans le même dépôt.

## Le jeton éphémère, et ce qu'il ne donne pas

L'audio n'a pas à transiter. Un **courtier de jetons** suffit : le serveur garde la clé, le client s'authentifie chez lui, reçoit un jeton court, et appelle le fournisseur en direct. Vérifié — l'endpoint existe, rend un jeton valable **15 minutes et consommé à l'usage**, et prend un `token_type` parmi `realtime_scribe`, `batch_scribe` et **`tts_websocket`**.

Trois conséquences, dont une bonne :

- **L'app ne porte aucun secret**, seulement un jeton qu'elle a obtenu en s'authentifiant. Un service payant reste compatible avec des sources publiques, ce qu'une clé embarquée interdit absolument.
- **Le jeton ne vaut que sur le websocket**, pas sur le `POST` HTTP qu'utilise `ElevenLabsSynthesis`. C'est une réécriture de cette implémentation, et la doc du fournisseur avertit qu'envoyer tout le texte d'un coup y est *légèrement plus lent* qu'une requête HTTP ordinaire.
- **Le contrat d'entrée survit** : les formats du websocket comprennent `pcm_16000`, et seul `pcm_44100` demande un plan Pro. C'était le point qui pouvait tuer le montage ; il ne le tue pas.

**Ce que le jeton retire, c'est le compteur.** Le client appelant le fournisseur en direct, la consommation ne se voit ni en texte ni en caractères — seulement en agrégat sur la facture. La parade propre serait une clé par abonné avec un plafond mensuel : l'endpoint existe et prend un `character_limit`, mais il vit sous les *service accounts*, **réservés aux espaces multi-sièges, donc à partir de Scale (299 $/mois)**. Hors de portée avant le premier utilisateur.

En dessous, **le seul compteur est l'émission des jetons**, et sa granularité est la connexion. D'où un arbitrage qui n'est pas tranché : une connexion tenue ouverte pour toute la séance sert la latence mais ne compte que des séances de longueur inconnue ; **un jeton par réplique** rend le bon grain et un vrai robinet, au prix d'un aller-retour chez soi avant chaque son. La stratégie de connexion cesse donc d'être une question de latence — c'est devenue la question du compteur, et les deux tirent en sens contraire.

## Ce qui n'est pas décidé

*Les trois premiers ne concernent plus que le mode (b), donc ils attendent avec lui.*

- **La granularité du jeton** : par séance ou par réplique, latence contre compteur.
- **La latence de v3 en direct**, non mesurée, qui décide si les balises expressives sont jouables en conversation.
- **Ce que fait une voix retirée de la bibliothèque** pour qui l'avait déjà ajoutée. Non vérifié.
- **Les conditions d'usage** posées par l'auteur d'une voix communautaire, commercial ou non, si le dépôt public entre en jeu.

Ce qui est vérifié en source ici : les types de jeton et leur durée, les formats du websocket, le tarif au caractère, le seuil des *service accounts*. Ce qui ne l'est pas et est marqué comme tel : les emplacements par plan (lus hors documentation officielle) et le blocage de l'ajout sur le plan gratuit (noté dans `NOTES.md`, jamais retrouvé en source).
