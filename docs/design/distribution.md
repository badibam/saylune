# Deux builds, deux publics, un compteur

Doc transitoire, ouvert le 2026-09-10. Il porte ce qui entoure le mode payant : les deux versions distribuées, le compte qui tient les crédits, les deux rails de paiement, et la façon dont une histoire payante est livrée. Le montage technique du mode payant lui-même — les jetons, la pochette, la coupe de l'appel en deux — est dans `premium-chain.md` ; ce que les voix en font est dans `voices.md`.

## Les deux versions

**Sur F-Droid, l'app libre où l'utilisateur apporte ses clés.** BYOK par maillon, comme aujourd'hui, et le mode payant disponible en plus pour qui n'a pas envie d'ouvrir trois comptes.

**Sur le Play Store, le mode payant est le seul chemin.** L'écran des clés et les sélecteurs par maillon en sont **absents** : un utilisateur qui achète l'app dans un magasin ne va pas ensuite créer trois comptes chez trois fournisseurs. Techniquement c'est de la **soustraction**, ce qui est la bonne direction — une *flavor* Gradle qui retire, jamais qui ajoute une logique parallèle.

**Le mode payant est un mode, pas un fournisseur.** Il aurait pu entrer dans la liste des fournisseurs par maillon, puisque le service tient les trois ; il n'y entre pas. La raison est que la question *suis-je en mode payant* est posée par trois choses qui ne sont pas des maillons — les voix nommées d'une fiche, le registre ordinaire du catalogue interne, l'accès aux fiches marquées — et qu'un mode y répond directement là où un fournisseur aurait obligé à la déduire du maillon choisi.

**Deux `applicationId` distincts** — `app.saylune` et `app.saylune.play`. Android identifie une app par son identifiant **et sa signature**, et les deux canaux ne signent pas pareil : Google resigne ce qui passe par son signing, F-Droid signe de sa clé sauf build reproductible. Sous un même identifiant, installer l'une par-dessus l'autre serait refusé par le système et demanderait une désinstallation, donc la perte des données locales. Sous deux identifiants, elles cohabitent et personne ne rencontre ce refus.

**Effet secondaire des builds reproductibles**, que le TODO vise déjà pour la vérifiabilité : l'APK distribué par F-Droid et celui posé sur le site deviennent le **même**, donc interchangeables. Le nombre d'identités en circulation tombe à deux.

## L'analyse distante

**C'est une fonctionnalité du mode payant**, et elle est disponible dans les deux versions puisque le mode l'est. Elle est conçue ailleurs, dans sa propre session.

**Elle touche un invariant écrit** : `reference.md` tient que l'analyse tourne sur l'appareil, que c'est la colonne vertébrale de l'app, et que l'audio d'un tour ne quitte l'appareil **qu'une fois**, pour la conversation. L'analyse distante en fait deux. Ce n'est pas rédhibitoire — l'invariant a été écrit contre la dépendance réseau de la mesure, et un serveur qu'on tient n'est pas un fournisseur tiers — mais la portée se tranche explicitement dans `reference.md`, sinon la question se reposera.

**Le téléchargement du modèle local reste proposé là où il est**, sur l'écran des clés, et il est **honoré même en mode payant**. Ce n'est pas un repli : le mode payant n'a aucune raison de retirer une brique qui tourne sur l'appareil.

## Le compte

Le compteur de crédits est tenu par le serveur, donc il lui faut une clé pour savoir à qui appartient quoi. **Google n'en fournit pas** : sa facturation prouve un paiement, elle ne rend aucun identifiant d'utilisateur stable. Le compte est donc nécessaire quel que soit le rail.

**Un identifiant anonyme, une récupération facultative.** Tiré au hasard au premier lancement, visible dans les réglages sous forme de **code court**. Aucune inscription, aucun email obligatoire. Qui veut peut attacher un email ou noter le code, ce qui sert deux fois : retrouver ses crédits sur un autre téléphone — `android:allowBackup="false"` interdisant que la sauvegarde système le fasse — et donner de quoi identifier un compte quand quelqu'un écrit pour un problème.

**Le compte est de la même forme dans les deux versions.** Seule la façon d'y ajouter des crédits diffère.

## Les deux rails de paiement

**Sur le build Play, la facturation de Google et rien d'autre.** Vendre du contenu numérique dans une app Play en contournant leur facturation est refusé par leur politique, donc ce build n'offre aucun autre chemin. Sa bibliothèque de facturation est **non libre**, ce qui est admissible parce qu'elle vit dans cette *flavor* seule et n'entre jamais dans le build F-Droid — la facette `fdroid` recommande précisément la flavor dédiée pour isoler une dépendance non libre.

**Sur le build F-Droid, l'achat se fait sur le site, dans le navigateur.** L'app peut ouvrir elle-même la boutique avec le code du compte dans le lien, donc rien à recopier.

**Comment l'identifiant voyage, précisément.** Après un paiement Play, c'est **l'app qui appelle le serveur**, avec le jeton d'achat rendu par Google et son propre identifiant de compte ; le serveur vérifie le jeton auprès de Google, puis crédite ce compte. L'identifiant ne traverse donc jamais Google. En plus de ça, l'identifiant est **attaché à l'achat côté Google** dans le champ prévu pour ça, que le vendeur remplit lui-même : c'est le chemin de secours pour le cas où le paiement aboutit et où l'app perd le réseau avant d'avoir prévenu le serveur, et c'est aussi ce qui rattache un remboursement, dont Google notifie sans que l'app soit dans la boucle.

**Les prix diffèrent d'un rail à l'autre**, la commission n'existant que sur celui de Google.

**L'achat suit le build, la dépense est portable.** Interdire l'achat hors du rail du build est une obligation ; interdire la dépense d'un crédit acheté ailleurs serait un travail à faire exprès, le compteur étant indexé par compte et non par magasin. Et le cas est étroit : il faut avoir attaché une récupération à son compte, être allé sur le site par ses propres moyens, puis jouer sur le build Play — le build Play n'ayant aucun chemin vers le site. On ne construit donc rien pour l'empêcher, et **le compteur dira lui-même** combien de comptes achètent sur le web et jouent sur Play, si la question se pose un jour avec un chiffre.

## Les histoires payantes

**Elles ne sont pas livrées dans l'APK.** L'app est libre et recompilable, donc ce qui voyage dans le build est acquis à tout le monde. Elles se téléchargent après achat, depuis le service qui vend les crédits.

**Elles sont chiffrées au repos**, avec une clé du Keystore Android — le même mécanisme que celui qui garde les clés d'API. Ça arrête la copie ordinaire par un gestionnaire de fichiers, une sauvegarde ou une autre app ; un appareil rooté ou un build modifié passe outre, puisque l'app doit déchiffrer pour jouer.

**Rien au-delà.** Un marquage par acheteur, qui rendrait une fuite publique attribuable, reste possible et n'est pas retenu. Une livraison par morceaux liée à une session imposerait d'être en ligne pour jouer, casserait la reprise d'une séance et rendrait les histoires achetées inertes le jour où le service s'arrête. Un prompt assemblé côté serveur contredirait le socle du mode payant, qui est que le projet n'a jamais accès à ce qui passe.

**Un encodage propriétaire du format n'apporte rien par-dessus le chiffrement** : l'app étant libre, le décodeur est publié avec elle, donc le format est documenté par construction — la méthode est dans le dépôt là où une clé n'y est pas. Et il retirerait le texte clair de la chaîne d'écriture, où il sert à relire, à comparer et à valider.

## Ce que F-Droid en dit, vérifié

Lu le 2026-09-10 sur leur documentation. **Aucune de leurs dix anti-features ne vise le contenu payant** — `Ads`, `DisabledAlgorithm`, `KnownVuln`, `NonFreeAdd`, `NonFreeAssets`, `NonFreeDep`, `NonFreeNet`, `NoSourceSince`, `TetheredNet`, `Tracking`. Les trois qu'on pourrait croire concernées ne le sont pas au texte : `NonFreeAdd` vise les apps qui *promeuvent d'autres applications ou greffons non libres* ; `NonFreeAssets` vise celles qui **contiennent** des ressources non libres, ce qu'une histoire téléchargée n'est pas ; `TetheredNet` vise celles qui dépendent **entièrement** d'un service irremplaçable, ce que l'app ne fait pas puisqu'elle tourne en BYOK. `NonFreeNet` est déjà déclarée et couvre le service de crédits sans rien ajouter.

**La règle qui s'applique vraiment est dans leur politique d'inclusion** : une app ne doit pas télécharger de binaire exécutable supplémentaire sans consentement explicite, lequel doit être opt-in, pas plus difficile à refuser qu'à accepter, et dire clairement à l'utilisateur qu'il contourne les vérifications de F-Droid. Une définition d'histoire est de la donnée interprétée par le moteur et non un exécutable, mais le projet applique déjà cette forme aux 359 Mo de poids acoustiques ; la reprendre telle quelle coûte moins que de plaider la lettre.

Ces lectures sont les nôtres. **Le jugement est rendu par eux à la soumission**, comme pour la liberté réelle d'un binaire tiré d'un dépôt Maven.

## Ce qui reste à vérifier avant de construire

- **Ce que Google admet aujourd'hui en matière de mention d'un paiement externe.** Historiquement l'app ne pouvait pas dire qu'il existe moins cher ailleurs ; des décisions de justice récentes ont desserré cela, et pas partout de la même façon. La règle de conception ne bouge pas en attendant : le build Play ne mentionne pas le site.
- **Les règles de Play sur les apps qui produisent du contenu par un modèle de langue** — signalement par l'utilisateur, modération, classification d'âge. Une app où un modèle parle librement tombe dedans.
- **Le coût d'un tour**, qui n'est pas mesuré et sans lequel le prix d'un crédit n'a pas de plancher : reconnaissance, modèle de langue, synthèse, plus le GPU si l'analyse est distante. La commission de Google se pose par-dessus, pas dedans.
- **Le taux d'abus des crédits offerts**, qui décide de leur nombre. Le rail Play rend ici un service : l'identité de l'acheteur y est déjà, donc un signal existe sans construire d'authentification.
- **La responsabilité change de côté en mode payant** : ce n'est plus la clé de l'utilisateur et son contrat avec un fournisseur, c'est le compte du projet et ses conditions. Le réglage de ce dont l'apprenant veut rester loin existe déjà ; ce qui manque est ce qui l'entoure.
