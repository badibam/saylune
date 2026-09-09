# La reconnaissance locale — ce que le verbatim tient, et ce qu'on entraîne

Doc transitoire : il porte une conception écrite pour être implémentée, et il **s'élague quand le code est en place** — les commits deviennent alors la carte.

Ce qu'il ne porte pas : la forme de la couture, actée dans `../reference.md` — un fichier en entrée, des mots avec leurs bornes en sortie, ni clé ni panne réseau dans le contrat. Et la place de ce maillon dans la chaîne locale, qui est dans `local-chain.md`, dont ce doc est le critère 8 déplié.

## Ce que le verbatim tient, et ce n'est pas une feuille

Trois choses lisent ce que la reconnaissance écrit d'une hésitation, et la troisième est celle qu'on oublie.

Le **bafouillage** est un des trois marquages rendus par le modèle de langue (`../measures.md`) : il lit le transcript, donc un transcript sans hésitation ne peut rien marquer. Le **débit** ne compte que les mots retenus, ce qui suppose de savoir lesquels ne le sont pas.

Et surtout **l'alignement du son**. Le doc écrit *on aligne sur les quatorze, on compare sur les huit* : l'apprenant s'aligne sur tout ce qu'il a dit, hésitations comprises, et seuls les mots retenus se comparent au modèle. Une reconnaissance qui avale un *euh* laisse ce bout d'audio **sans aucune lettre en face**, donc en sons en trop, donc en gros ratés. **Hésiter coûterait alors une note de prononciation** — l'inverse exact de ce que l'app est là pour mesurer.

Le critère 8 de `local-chain.md` est donc plus dur qu'il n'était écrit : ce n'est pas un critère de choix parmi d'autres, c'est une condition de l'analyse du son.

## Le paysage est vide, et on sait pourquoi

**Rien n'existe qui soit à la fois local, libre et verbatim, en anglais.** Trois candidats, trois sorties différentes :

- **CrisperWhisper** — techniquement exactement ça, jusqu'au bouton verbatim/intended. Licence non commerciale (CC-BY-NC pour la 1.0, licence maison pour la 2.0), donc écarté par le critère 1 et classé `NonFreeAssets` chez F-Droid. **Ce n'est pas une réserve, c'est une impasse**, contrairement à ce que `local-chain.md` et `../../TODO.md` écrivaient.
- **Pingala V1 Verbatim** — anglais, 1,5 milliard de paramètres, licence RAIL-M : usage commercial permis mais restrictions d'usage, donc pas une licence libre reconnue.
- **NB-Whisper verbatim** — licence bonne, corpus norvégien.

**La cause tient en une phrase : un modèle écrit comme le corpus qui l'a entraîné.** GigaSpeech traite un `UH` écrit comme une faute de transcripteur et réécrit sa référence pour la retirer ; LibriSpeech est de la parole lue, où personne n'hésite. Toute la famille des petits modèles libres — zipformer, moonshine, parakeet — est nourrie de ça.

Mesuré ailleurs, sur le banc de verbatimité de nyra, en hésitations retrouvées sur cent : Whisper large-v3 en rend 10, Canary 1B en rend 27, CrisperWhisper 1.0 en rend 71 et la 2.0 en rend 91. Whisper et CrisperWhisper sont le même modèle : seul l'entraînement change. **La taille n'achète pas le verbatim.**

## Ce qui est décidé

**Une seconde tête CTC, entraînée sur AMI.** L'app fait déjà tourner sur le téléphone un wav2vec2 à tête CTC qui sort des phonèmes ; une reconnaissance de mots est le même réseau avec une tête qui sort des lettres. `train/train.py` ne sait rien des phonèmes — il prend un manifeste et une liste de symboles —, donc le changement est le vocabulaire et les cibles.

**Sur `wav2vec2-large-960h-lv60-self`, et découplé du modèle acoustique.** Le régime retenu pour les phonèmes est `xls-r-300m` en affinage complet, et le choix du modèle acoustique reste ouvert, bloqué derrière L2-ARCTIC. Coupler les deux têtes à une seule oreille enchaînerait la reconnaissance à une tâche coincée. Le prix du découplage est un second modèle sur le téléphone — un chargement de plus et une passe de plus, tous deux mesurés — et le téléchargement n'est pas un problème. **L'oreille partagée reste possible plus tard** : c'est un réentraînement, pas une réécriture.

**Et le décodage reste brut, sans modèle de langue.** C'est ce qui fait qu'un CTC écrit ce qu'il entend au lieu de le réparer. La tentation viendra le jour où les chiffres décevront : un modèle de langue au décodage remonterait n'importe quel score public **en réintroduisant exactement le nettoyage qu'on supprime**.

**AMI est le corpus.** 100 h en CC-BY-4.0, tirées en flux depuis Hugging Face, locuteurs majoritairement non natifs, texte en majuscules sans ponctuation avec les hésitations écrites — le format de sortie du modèle, donc aucun vocabulaire à changer : `UH` s'écrit déjà avec les lettres qu'il a. LearnerVoice (50 h de vraie parole d'apprenant) reste une note pour le jour où AMI ne suffirait pas ; sa licence n'a pas été lue. Speak & Improve est mieux annoté et sort du chemin, sa licence non commerciale suivant les poids qu'on distribue.

## Le banc

`bench/stumbles.py` porte le jeu, `bench/record.py` l'enregistre, `bench/verbatim.py` écrit les références du bloc spontané, `bench/fidelity.py` note. Convention prise sur le banc de nyra — tiret final sur un mot coupé, remplissage en mot à part — pour que nos chiffres se lisent contre les leurs sans traduction.

**Le bloc A est un plancher et rien d'autre.** Vingt-quatre phrases dont l'hésitation est écrite et lue telle quelle. Un *uh* joué est propre, articulé, détaché ; un vrai est court et collé au mot d'après. Un modèle qui échoue ici échoue partout ; qu'il réussisse ici ne dit rien. **Le bloc B est la mesure** : douze questions inattendues, répondues à froid, où les hésitations ne sont la décision de personne.

**La note est par sorte, jamais globale.** Un taux d'erreur compte tous les mots pareil, donc un modèle qui avale chaque `uh` y score **mieux** qu'un modèle qui les écrit — exactement à l'envers ici. Et les sortes ne ratent pas de la même façon : un mot coupé rate en devenant un mot entier plausible, ce qu'aucun total ne montre. Les faux départs et la voyelle tenue n'ont aucun jeton à eux et ne se voient que par le taux d'écart de leurs prises.

## Ce qui est mesuré

Sur le bloc A, 24 prises, le 2026-09-09 :

| | remplissages | fragments | répétitions | inventés |
|---|---|---|---|---|
| `wav2vec2-large-960h-lv60-self` | 0/7 | 1/4 | 2/2 | 0 |
| CrisperWhisper 2.0 | 7/7 | 4/4 | 2/2 | 0 |

Le second est le **plafond** et pas un candidat : sa licence l'écarte de l'app, et il sert ici de brouillon de référence et de repère.

**Le modèle de l'étagère n'écrit pas un mot faux à la place du remplissage, il n'écrit rien** — *I think you are right* pour *I think you are uh right*. Il n'a aucun modèle de langue qui pourrait nettoyer : il n'a simplement jamais vu de transcription qui contienne une hésitation. **Le manque est en données, pas en capacité**, et c'est ce qui rend l'affinage crédible.

**Les répétitions passent déjà, groupes compris** — *I want to I want to turn right*, *The chair is very the chair is very comfortable* reviennent au mot près. Un CTC n'a aucune raison de supprimer un mot répété. C'est la propriété qu'on est venu chercher.

**Les fragments sont la vraie difficulté** : `comf-` devient `CA`, `tr-` disparaît, et `inter-` fait dérailler la phrase entière.

**Les témoins ne coûtent rien** : zéro écart sur les cinq phrases dites d'un trait, aucun mot inventé dans les deux blancs de deux secondes.

**Sur AMI**, tranche de 20 000 énoncés, 17 h : 21 % portent un `UH` ou un `UM`, et 11 % portaient un symbole hors du vocabulaire du modèle — les points de `S. S. H.` —, qui deviennent des espaces faute de quoi le réseau apprend à écrire un jeton inconnu.

**Un fait à part, qui n'est pas une mesure** : sur *I picked a pear*, l'étagère écrit `BEAR`. Selon la doctrine du projet — ce que le réseau entend est la source de vérité — ce serait une faute de prononciation correctement entendue, pas une erreur de la machine. À vérifier à l'oreille sur cette prise avant d'en faire quoi que ce soit.

## La voie légère

Rien de gros ne descend sur la ligne de l'auteur. Trois gestes, dans cet ordre d'importance.

**Juger sur des kilo-octets.** Les prises montent une fois en dataset Kaggle privé ; un notebook transcrit et rapporte un JSON. Le tri entre plusieurs états se fait donc sur quelques kilo-octets, jamais sur des checkpoints. C'est le même acquis que pour l'analyse, où le banc lit des matrices calculées ailleurs.

**Exporter dans le notebook.** Seul l'état gagnant s'exporte en ONNX 8 bits, une fois.

**Ne pas rapatrier du tout.** L'app va chercher ses poids dans une release GitHub ; le notebook peut les y pousser directement. Datacenter → GitHub → téléphone.

Les notebooks sont dans `train/kaggle/` : `baseline.ipynb` (le plancher), `prefill.ipynb` (le brouillon de référence), `finetune.ipynb` (l'affinage, `FULL` en tête pour la tête seule ou le réseau entier).

## Ce qui reste

- **Les deux runs sont partis le 2026-09-09** — tête seule et réseau entier, 20 000 énoncés, deux époques — et **rien de leur résultat n'est connu**.
- **Les références du bloc B ne sont pas écrites.** Le brouillon existe (`tmp/prefill.json`), `bench/verbatim.py` attend. Une prise demandera du travail : sur `failed-repair`, CrisperWhisper part en boucle et écrit environ deux cents *I* d'affilée — la panne classique de Whisper sur une hésitation longue. L'audio est bon ; c'est la seule prise qui ait fait tomber le modèle.
- **Une divergence du bloc A à trancher à l'oreille** : sur `repeat-group`, les deux modèles écrivent *I want to **I** I want to*. Soit la référence écrite est fausse, soit c'est la seule invention du bloc.
- **Rien n'est mesuré sur l'appareil** : ni la durée d'une passe, ni la mémoire, ni ce que la quantification 8 bits coûte au verbatim.
- **La composition des deux modèles n'est pas pensée** : deux réseaux de 300 millions de paramètres, un pour les sons et un pour les mots, dont un seul peut être résident. L'ordre de la chaîne le décide, et il n'est pas écrit.
