# SpeechAce — instruction

Tout ce qui est propre à ce moteur, mesuré sur `pronunciation-test-set.md`. SpeechAce est **l'un des trois candidats** au rôle de brique d'analyse (cf. `../reference.md`, « Fournisseurs ») ; rien ici n'est un engagement. Ce document existe pour que le choix se fasse sur des mesures et non sur une impression, et pour que la mesure survive au choix.

Mesuré sur un essai au plan **Basic**, région EU West.

## Le contrat REST

Cinq endpoints, dont deux nous concernent. `POST <endpoint>/api/scoring/text/v9/json`, clé et `dialect` en paramètres d'URL, `text` et `user_audio_file` en multipart. `POST .../api/scoring/word/v9/json` fait la même chose pour un mot isolé, avec ses propres optimisations et 30 s au lieu de 15.

L'endpoint dépend de la région d'achat et **n'est pas devinable** : appeler la mauvaise région rend une erreur d'authentification qui se lit comme une clé invalide.

Drapeaux optionnels utiles : `include_intonation`, `include_interference_metrics`, `include_unknown_words`, `markup_language=arpa_mark`.

Un endpoint sans audio, `POST .../api/validating/text/v9/json`, dit si un texte est connu du lexique. Il ne coûte rien puisque la facturation porte sur la durée d'audio — c'est le bon appel pour valider une clé.

Les endpoints de parole spontanée (`score/speech`) sont au plan Premium et **ne documentent pas `sound_most_like`**. Le mode scripté est celui que le fournisseur recommande, et le seul qui rende ce dont l'app a besoin.

## Ce que rend la réponse

Par phonème : le son **attendu** (ARPABET), `extent` en unités de 10 ms, `quality_score` sur 100, `word_extent` (les indices de caractères du mot que ce phonème couvre) et `sound_most_like`.

Par syllabe : `letters`, `extent`, `quality_score`, `stress_level` (attendu, tiré du lexique), `predicted_stress_level` (entendu), `stress_score`. Avec `include_intonation`, s'ajoutent `intonation` et `pitch_range` en hertz.

**Les trois échelles s'ancrent aux mêmes caractères** : le phonème par `word_extent`, la syllabe par `letters`, la mélodie par la syllabe qui la porte. Deux irrégularités à prévoir — une lettre peut porter deux phonèmes (le `l` de `comfortable` reçoit `ah` et `l`), et une lettre peut n'en porter aucun (le `e` final).

L'alphabet est **ARPABET**, pas IPA ; la doc du fournisseur donne les tables de correspondance pour les six dialectes. L'ARPABET français est maison (`RW` pour `ʁ`, `AGN` pour `ɑ̃`) et ne se déduit pas.

Le moteur est **déterministe** : même audio, même texte, mêmes notes au centième et mêmes bornes à la milliseconde.

## `sound_most_like` renvoie l'écho du texte fourni

Le champ annonce le son réellement produit. Il le fait quand l'écart acoustique est franc, et se tait sinon — en répétant le phonème attendu.

Le **même segment audio** (un /s/ franc, 3,2 s de prise), noté contre deux textes rivaux :

| texte annoncé | phonème attendu | `sound_most_like` | note |
|---|---|---|---|
| `I think you are right` | th | **th** | 83,3 |
| `I sink you are right` | s | **s** | 99,3 |

Il ne dit jamais `s` quand on lui annonce `th`. Ce n'est donc pas une identification indépendante du son produit : c'est le phonème attendu, sauf quand l'écart dépasse un seuil interne.

**Il est donc informatif quand il diverge, muet quand il concorde.** Une divergence est une vraie détection ; une concordance ne distingue pas « c'était juste » de « pas assez d'écart pour contredire ».

## La note, elle, porte le signal

Sur les huit fautes délibérées du jeu d'essai, en écartant les segments dégénérés :

| prise | son | note | nommé | cas |
|---|---|---|---|---|
| 09 | ng | 55,5 | m | ng→n, demi-faute |
| 07 | p | 66,8 | b | p→b |
| 16 | iy | 70,4 | ih | iy→ih franche |
| 08 | r | 73,5 | l | r→l |
| 01 | th | 78,7 | s | th→s, demi-faute |
| 17 | th | 83,3 | — | th→s **franche** |
| 18 | ng | 95,1 | — | ng→n **franche** |
| 06 | iy | 94,7 | — | iy→ih, durée seule |

Six fautes nommées, deux manquées, et **aucune fausse alerte sur les trois témoins**. C'est nettement au-dessus du verdict de conformité d'Azure, inutilisable pour la détection.

Les deux manques sont réels et stables. La prise 17 est le cas instructif : le contrôle contre le texte réellement prononcé donne 99,3, donc la faute a bien été produite — c'est le moteur qui la laisse passer, à 83,3, en pleine bande « clairement intelligible » de son propre barème.

## Le modèle synthétique comme étalon

Faire dire la phrase par un TTS, passer ce rendu dans le même moteur avec le même texte, et comparer phonème à phonème. Le modèle note **100,0 de médiane sur 71 phonèmes**, un seul sous 90 : c'est un étalon valide.

| prise | humain | modèle | écart | cas |
|---|---|---|---|---|
| 13 | 100,0 | 99,0 | **+1,0** | témoin |
| 15 | 99,7 | 99,2 | **+0,4** | témoin |
| 14 | 98,7 | 99,5 | **−0,8** | témoin |
| 09 | 55,5 | 99,0 | −43,5 | ng→n |
| 07 | 66,8 | 99,5 | −32,7 | p→b |
| 16 | 70,4 | 99,7 | −29,3 | iy→ih |
| 08 | 73,5 | 99,0 | −25,5 | r→l |
| 01 | 78,7 | 98,3 | −19,6 | th→s |
| 17 | 83,3 | 98,3 | −15,1 | th→s franche |
| 06 | 94,7 | 99,7 | −5,0 | durée seule |
| 18 | 95,1 | 99,0 | −3,9 | ng→n franche |

**Les témoins tombent à ±1, les fautes vues à −15 ou plus bas, et il n'y a rien entre −5 et −15.**

L'étalon annule le bruit propre du moteur : un son que le moteur note mal en général est noté mal des deux côtés, et l'écart reste nul. Toute question de calibrage par phonème disparaît avec lui. La comparaison est **strictement interne au tour** — aucune mémoire, aucun état accumulé.

Un seuil absolu sur la note brute atteint la même détection (6/8) mais laisse les témoins dispersés au lieu de ±1.

## Étalonner la voix, et l'accorder au dialecte

Le débit d'échantillonnage est indifférent : 16, 24 et 48 kHz rendent les mêmes notes à 0,3 point près. Un seul rendu en haute qualité sert donc à l'écoute et à la mesure.

La voix, elle, compte :

| voix | médiane | min | écart moyen à l'étalon |
|---|---|---|---|
| Jenny (féminine) | 100,0 | 98,7 | étalon |
| Guy (masculine) | 100,0 | 96,8 | −0,20 |
| Ana (voix d'enfant) | 99,6 | **67,8** | −4,05, max **−32,2** |

Le genre de la voix ne perturbe pas la notation phonémique. Une voix atypique, si. **Toute voix promue étalon doit passer un test** : quelques phrases synthétisées, médiane au-dessus de 97, aucun phonème sous 90. Une voix qui échoue accuserait l'apprenant d'une faute commise par la machine.

Le dialecte de notation et celui de la voix doivent être **le même** :

| | médiane | min | phonèmes sous 90 |
|---|---|---|---|
| voix US notée `en-us` | 100,0 | 98,7 | — |
| voix US notée `en-gb` | 98,2 | **60,6** | trois |
| voix GB notée `en-gb` | 99,2 | 94,0 | — |
| voix GB notée `en-us` | 100,0 | 97,0 | — |

Un désaccord ne dégrade pas la mesure, il l'**inverse** : c'est le modèle qui se fait pénaliser, l'écart devient positif, et la faute de l'apprenant passe inaperçue. Les deux lexiques ne découpent pas non plus la phrase en autant de phonèmes (17 contre 18), donc les marques ne tombent pas sur les mêmes lettres.

## Le filtre des segments dégénérés

Sur 262 phonèmes mesurés, durée médiane 120 ms, l'aligneur décroche sur certains segments et leur colle une étiquette au hasard.

| durée du segment | divergences | note médiane |
|---|---|---|
| ≤ 40 ms | 5 | 35,0 |
| 40 à 90 ms | 18 | 61,6 |
| > 90 ms | 17 | 62,5 |

Tous les segments à **0 ou 10 ms** sont aberrants — `r`→`k`, `l`→`n`, `l`→`d`, un `ay` sans valeur — à des notes de 0 à 42. À 30 ms tout redevient normal.

**Écarter tout phonème dont l'`extent` fait 10 ms ou moins.** Ça retire 8 segments dont 7 sont des aberrations. Le décrochage peut aussi emporter un mot entier : sur la prise 18, le mot `I` revient à 0 ms et note 0, sans que rien d'autre dans la réponse ne signale que l'analyse est partiellement invalide.

## La prosodie

Deux choses de portée différente, toutes deux rapportées au niveau de la syllabe.

**L'accent lexical décrit le mot** — quelle syllabe est la forte, fixé par le dictionnaire. C'est pour ça que le moteur peut le juger : il a un attendu. Testé en synthèse, même voix, accent forcé sur la deuxième syllabe de `comfortable` :

| rendu | syllabe | attendu | produit | `stress_score` |
|---|---|---|---|---|
| normal | com | 1 | **1** | **100** |
| accent déplacé | com | 1 | **0** | **0** |

Détection nette, sur la bonne syllabe. Limite : le moteur voit que l'accent attendu n'a pas été réalisé, pas qu'il est parti ailleurs (`fort` reste à 0/0).

**La mélodie décrit la phrase** — le contour de hauteur qui court sur tout l'énoncé. Un `RISE` sur une syllabe isolée ne signifie rien hors de la suite. Le moteur la **mesure fidèlement** : la même phrase avec le dernier mot monté de 40 % passe de `of` 166 Hz / `FALL` à `of` 292 Hz / `RISE`.

Mais **il ne la juge pas** : aucun champ ne dit quel contour la phrase aurait dû avoir, et aucun dictionnaire ne pourrait le dire. La juger exige de fabriquer la référence, ce que le modèle synthétique permet.

### Éprouvé sur voix humaine

Bloc E du jeu d'essai, quatre prises, chaque faute avec son témoin.

**L'accent — le verdict du moteur est inutilisable, sa lecture ne l'est pas.**

Le champ `predicted_stress_level` est annoncé comme comparable à `stress_level`, l'attendu du lexique. Il ne l'est pas. Sur **22 mots polysyllabiques dits par un TTS parfait** :

| | | |
|---|---|---|
| accord exact avec le lexique | 10 | 45 % |
| désaccord dur (0 contre 1) | 9 | **41 %** — chacun produirait une marque |
| désaccord doux (1 contre 2) | 3 | 14 % |

`corner` et `very` **gagnent** un accent qu'ils n'ont pas ; `office`, `comfortable` et `necessary` **perdent** le leur. Et la lecture n'est pas stable : `office` se lit `(0,0)` ou `(1,0)` selon le rendu, et bascule quand on manipule la hauteur du **même** mot dans la **même** phrase. La lecture d'un mot dépend de ce qui se passe ailleurs dans l'énoncé.

Aucun raffinement ne sauve ce verdict — un discriminant restreint à « la syllabe qui gagne l'accent » se déclenche sur `corner` et `very`, prononcés parfaitement.

**Mais comparer sa lecture de l'apprenant à sa lecture du modèle fonctionne**, parce que le biais est le même des deux côtés. Éprouvé sur un lot construit pour ça — pour chaque mot, une prise spontanée puis un calque du modèle écouté :

| mot | modèle | spontané | calque |
|---|---|---|---|
| `important` | `[0,1,0]` | `[0,1,`**`1`**`]` — accent tiré vers la fin | `[0,1,0]` |
| `interesting` | `[1,0,0,0]` | `[`**`0`**`,`**`1`**`,0,`**`1`**`]` | `[1,0,0,0]` |

Deux fautes sur deux vues, aucune fausse alerte sur les calques, et `stress_score` tombe à 0 exactement aux syllabes divergentes.

**La portée de ce résultat est bornée au régime d'imitation.** Le témoin n'est valide que parce qu'il **copie** le modèle : « correct dans l'absolu » n'est pas mesurable ici, le moteur ne comparant pas à une norme mais à une réalisation. Sur une prise spontanée, aucune étiquette n'est disponible — et la prise 19, où l'appui était acoustiquement au bon endroit et où le moteur a lu `[0,0,0,0]` contre `[1,0,0,0]` au modèle, reste le seul indice de ce régime.

**La mélodie.** Séparation franche sur la syllabe finale :

| | hauteur rendue | étiquette | pente |
|---|---|---|---|
| modèle question (TTS) | 168 → 277 Hz | `RISE` | +8,6 demi-tons |
| humain question, témoin | 127 → 240 Hz | `RISE` | +11,0 — **écart +2,4** |
| humain plat, faute | 131 → 94 Hz | `FALL` | −5,8 — **écart −14,5** |

Exprimer la pente en **demi-tons** annule la différence de registre entre une voix de synthèse et celle de l'apprenant : c'est ce qui rend la comparaison au modèle utilisable malgré des hertz incomparables. Aucun accrochage harmonique sur ces deux prises.

## Le suivi de hauteur s'accroche aux harmoniques

| source | n | médiane | valeurs aberrantes |
|---|---|---|---|
| synthèse | 84 | 190 Hz | **0 / 84 (0 %)** |
| voix humaine | 52 | 144 Hz | **10 / 52 (19 %)** |

Les aberrations n'apparaissent que sur les voix humaines, et elles ne viennent pas des enregistrements : ceux-ci sont sains (crête à −2,8 dBFS, aucun échantillon saturé), et une estimation de f0 par autocorrélation faite sur les mêmes échantillons concorde avec le moteur sur toutes les syllabes de synthèse et sur 21 des 24 syllabes humaines.

Sur les 5 syllabes restantes, le moteur renvoie un **multiple** de la fondamentale réelle :

| syllabe | f0 mesurée | f0 rendue | rapport |
|---|---|---|---|
| think | 180 | 538 | ×3,0 |
| sheep | 186 | 551 | ×3,0 |
| field | 143 | 551 | ×3,9 |
| have | 172 | 554 | ×3,2 |
| to | 136 | 542 | ×4,0 |

Ce ne sont pas des doublements d'octave mais un accrochage à la **troisième ou quatrième harmonique**, et les segments concernés n'ont rien de dégradé — niveau normal, périodicité franche. Une voix de synthèse a une fondamentale stable et un spectre pauvre ; une voix réelle est plus riche, et le tracker y résiste moins bien.

Conséquence, puisque `intonation` dérive de la hauteur : une valeur accrochée produit une étiquette de contour fausse, et rien ne signale laquelle. Le filtre est le même que pour les segments dégénérés — **une syllabe dont la hauteur sort d'un facteur deux autour de la médiane du tour est une erreur de suivi**, et son étiquette de mélodie est à jeter. Ça retire jusqu'à un cinquième du contour sur certains tours.

## Les capacités se demandent, elles ne se déduisent pas

Sur une clé **Basic**, la grille tarifaire du fournisseur est inexacte :

| fonction | drapeau | réponse | grille |
|---|---|---|---|
| accent lexical | aucun, revient toujours | **répond** | annoncé Pro |
| intonation et hauteur | `include_intonation` | **répond** | annoncé Pro |
| interférence | `include_interference_metrics` | **répond** | non listé |
| fluidité et fidélité | `include_fluency` | **refusé** | annoncé Pro |

Le verrouillage est réel et appliqué fonction par fonction — la fluidité est refusée. Mais l'accent et l'intonation sont bien dans Basic malgré ce que la grille annonce.

Le refus est explicite et exploitable :

```json
{ "status": "error", "short_message": "error_feature_unavailable",
  "detail_message": "The requested feature is not available in your purchased plan." }
```

**C'est le fournisseur qui déclare ses capacités**, pas nous qui les devinons. Une sonde envoie un appel par drapeau et lit le `short_message`.

## Le coût

Facturation à la **tranche de 15 secondes, arrondie au-dessus**, seules les requêtes réussies comptant. Un tour de conversation de quelques secondes vaut une unité ; un rendu synthétique aussi.

Basic 0,008 $ l'unité, 5000 unités incluses. Un tour analysé des deux côtés en coûte deux, soit **2500 tours par mois** — une cinquantaine de sessions d'un quart d'heure.

Le plan est un **abonnement plancher à 40 $/mois**, pas un paiement à l'usage : consommé à 5 % ou à 100 %, c'est le même prix. Les crédits mensuels non consommés sont perdus. Concurrence garantie à 16 requêtes par seconde.

Rétention négociable au contrat, y compris zéro, mais par courriel et non en libre-service. CORS désactivé par défaut : l'API est conçue pour être appelée depuis un serveur, pas depuis un client.

## Ce qui reste incertain

- **Les deux fautes manquées** (durée seule, et une prise dont l'alignement était cassé en amont) le sont de façon déterministe. Aucun réglage de seuil ne les rattrapera.
- **L'accent déclenche sur un témoin correct.** Le discriminant retenu — la syllabe qui *gagne* l'accent — repose sur un seul cas et ne s'était pas allumé sur la faute synthétique. C'est le point le plus fragile de tout ce document.
- **Les 19 % d'aberrations de f0** sur nos enregistrements ne sont pas expliqués. Tant qu'ils le sont, la comparaison de contours est bancale.
- **Le bloc B du jeu d'essai** (grammaire fautive, prononciation censée propre) produit des divergences qu'on ne peut pas classer : les prises sont dites par un locuteur non natif et rien ne garantit que leur prononciation soit réellement propre. Trancher demanderait des témoins natifs.
- **Ce que rend `score/speech`** en parole spontanée n'a pas été observé : il est au plan Premium. La question ne se pose que si l'on renonce au montage scripté.
