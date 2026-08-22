# Azure Speech Pronunciation Assessment — instruction

Tout ce qui est propre à ce moteur, mesuré sur `pronunciation-test-set.md`. Azure est **l'un des trois candidats** au rôle de brique d'analyse (cf. `../reference.md`, « Fournisseurs ») ; rien ici n'est un engagement. Ce document existe pour que le choix se fasse sur des mesures et non sur une impression, et pour que la mesure survive au choix.

## Le contrat REST

Le SDK est un binaire propriétaire, incompatible F-Droid. Donc REST, limité à l'audio court — ce qui convient à des phrases de conversation.

`POST https://<region>.stt.speech.microsoft.com/speech/recognition/conversation/cognitiveservices/v1?language=<locale>&format=detailed`, wav brut en corps, clé en `Ocp-Apim-Subscription-Key`, `Content-Type: audio/wav; codecs=audio/pcm; samplerate=16000`.

L'évaluation se greffe par l'en-tête **`Pronunciation-Assessment`, dont la valeur est un JSON compact encodé en base64** — la partie contre-intuitive de cette API. Deux défauts sont contre nous et doivent être posés explicitement : `GradingSystem` vaut `FivePoint` et `PhonemeAlphabet` vaut `SAPI`. On envoie `Granularity: Phoneme`, `Dimension: Comprehensive`, `EnableMiscue: true`, `NBestPhonemeCount: 5`.

Deux plafonds distincts : **60 s** pour la reconnaissance seule, **30 s** dès que l'évaluation est demandée.

Forme de la réponse : les scores sont **à plat sur l'entrée**, pas nichés sous `PronunciationAssessment` comme le montre l'exemple de la doc du fournisseur. Chaque mot porte `AccuracyScore`, `Offset`, `Duration`, `ErrorType`, plus deux tableaux : `Phonemes` et `Syllables`.

## `AccuracyScore` est complaisant, et parfois inversé

Le même mot `think`, trois prises de la même voix :

| prise | `AccuracyScore` du /θ/ |
|---|---|
| /s/ à la place du /θ/ (faute) | **100** |
| /θ/ appuyé (correct) | 77 |
| /θ/ naturel (correct) | 93 |

La prise fautive obtient la meilleure note des trois. Ce n'est pas de l'indulgence, c'est un classement à l'envers : le score mesure la **typicité** du son face au phonème annoncé, et un son fautif mais proche peut être plus typique qu'un son correct mais exagéré.

La preuve directe, en notant le **même audio** contre deux textes rivaux :

| audio | annoncé « I think… » | annoncé « I sink… » |
|---|---|---|
| tu as dit `sink` | /θ/ = **100** | /s/ = **59** |

Deux verdicts opposés sur une seule seconde d'audio. `AccuracyScore` n'est pas une mesure : c'est la vérification d'une réponse qu'on vient de souffler au moteur. **Il est inutilisable pour la détection.** Il reste dans son élément quand le texte est réellement connu d'avance — mais même là il ne prouve rien, il acquiesce.

Ce défaut n'est pas propre à notre lecture : la recommandation qui circule chez les développeurs de cette API est d'examiner les alternatives `NBest` plutôt que le score d'un phonème, et le fournisseur lui-même admet des incohérences d'alignement.

## `NBestPhonemes` ne dépend pas du texte annoncé

C'est le résultat central, et il fonde tout le reste. Le même audio, la même position, deux textes de référence différents :

| audio | annoncé | liste des candidats |
|---|---|---|
| `sink` dit | « I **think**… » | θ:100 · s:90 · t:58 · aɪ:24 · z:11 |
| `sink` dit | « I **sink**… » | θ:100 · s:92 · t:53 · aɪ:27 · z:12 |
| `bear` dit | « a **pear** » | b:100 · ɛ:20 · p:19 · ə:14 · v:8 |
| `bear` dit | « a **bear** » | b:100 · ɛ:20 · p:19 · ə:14 · v:8 |

La note fait le grand écart pendant que la liste ne bouge pas. **La liste est une mesure du son produit ; la note en est la relecture à travers le texte annoncé.**

Conséquence pratique : noter le même audio contre deux hypothèses rivales pour les départager **ne sert à rien** — les deux appels rendent la même liste. L'idée a été testée et écartée.

Autre conséquence, plus importante : le texte de référence sert à savoir **ce qui aurait dû être produit**, jamais **ce qui l'a été**. Un texte reconstruit faux ne dégrade donc pas l'identification du son ; il déplace la cible à laquelle on la compare.

## Lire le rival confusable, pas le second de la liste

Première formulation, écartée : « le score du phonème attendu moins celui du second de la liste ». Elle échoue parce que le second est n'importe qui — dans les /ŋ/ corrects c'est un `k` à 56 ou un `ɪ` à 42, des sons sans rapport qui font un plancher de bruit. Sur une faute /ŋ/→/n/ franche, cette marge donne **+41**, à l'intérieur de la plage des prises correctes (+44 à +76). Non détectée.

Formulation retenue : lire directement **le score du phonème confusable**, celui que le locuteur risque de produire à la place.

| contraste | témoins propres | fautes | écart |
|---|---|---|---|
| /θ/ → /s/ | s à 28, 36 | s à 90 (molle), **100** (franche) | 54 |
| /ŋ/ → /n/ | n à 7, 9, 9 | n à 72 (molle), **59** (franche) | 50 |
| /p/ → /b/ | b à 10 | b à **100** | 90 |
| /r/ → /l/ | l à 0 | l à **100** | 100 |

Quatre contrastes, aucun recouvrement.

**Le prix** : il faut savoir d'avance quel son est le rival, donc embarquer une table des confusions **propre à la langue maternelle de l'utilisateur**. Pour un francophone apprenant l'anglais elle est courte et bien connue — `θ/s`, `ð/z`, `iː/ɪ`, `p/b`, `r/l`, `ŋ/n`, le `h` avalé. C'est un paramètre de plus, et une app qui cesse d'être neutre quant à qui l'utilise.

## Le niveau syllabe

`Syllables` est présent dans chaque réponse, à côté de `Phonemes`, avec symbole IPA, offset, durée et note. Il était là depuis le début sans qu'on le regarde.

Sur `comfortable` dit en quatre syllabes :

```
kʌmf   490 ms   67
tɚ     150 ms   31     <- la syllabe parasite
bəl    410 ms   83
```

Distribution sur les prises témoins : 24 syllabes, minimum **82**, médiane 100. Sur l'ensemble du jeu, seules **3 syllabes sur 90** tombent sous 70 — les deux de `comfortable`, et une prise notée contre un texte de référence faux.

C'est le seul endroit du moteur où **un seuil fixe fonctionne** : pas de table de confusion, pas de ligne de base, un plancher autour de 75-80 sépare proprement.

Mais il est **aveugle aux substitutions de son** :

| faute | syllabe | témoin propre |
|---|---|---|
| /s/ pour /θ/ | θɪŋk : 100 | 92 |
| /ɪ/ pour /iː/ | ʃip : 90 | 91 |
| /b/ pour /p/ | pɛɹ : 82 | 92 |
| /n/ pour /ŋ/ | ɪŋ : 100 | 100 |

Rien, y compris là où le niveau phonème hurle. Exception : `/r/`→`/l/` fait tomber la syllabe à 80, sous le plancher des témoins — un `l` à la place d'un `r` déforme toute la syllabe, un `b` à la place d'un `p` ne déforme qu'un instant.

**Les deux niveaux sont complémentaires, jamais substituables** : la syllabe voit le rythme et la réduction, le phonème voit les substitutions.

## Les locales

**Le niveau phonème n'est nommé qu'en `en-US`** — mesuré, pas déduit. En `en-GB`, le moteur note chaque phonème et classe bien cinq candidats pour chacun, mais renvoie la **chaîne vide** comme symbole partout : il sait quel son a été produit et refuse de le nommer. Donc `en-GB` **détecte** (position et score suffisent à poser une marque) mais ne peut ni nommer le son attendu ni dire lequel a été produit — pas de consigne d'articulation, qui est la matière même de la parenthèse.

La prosodie, le niveau syllabe et l'évaluation de contenu sont eux aussi limités à `en-US`.

Conséquence : chez ce fournisseur, une v1 américaine tient entièrement ; les autres accents n'offriraient qu'une détection muette. Piste si l'accent devient prioritaire : les phonèmes non nommés arrivent **ordonnés et en nombre correct**, donc un lexique de phonémisation de la locale visée permettrait de recoller les étiquettes par alignement. Ça ajoute une ressource à embarquer.

## Le coût

Un tour détecté coûte **deux fois sa durée d'audio** : reconnaissance d'abord, évaluation scriptée ensuite, le texte de référence ne pouvant se construire qu'après la première. Le doublement est structurel, pas une maladresse d'implémentation.

Mesuré sur le jeu : 4,1 s de parole par tour en moyenne, donc environ 8 s facturées par tour détecté, plus un appel LLM court dont le coût est négligeable devant.

## La reconstruction du texte de référence

Mesurée sur les trois cas où le piège s'est réellement déclenché : deux justes (`ship` → `sheep`, `bear` → `pear`, cette dernière plaçant la marque exactement sur le /p/), une fausse — « Turn light at the corner » reconstruit en `left` au lieu de `right`, le contexte admettant les deux.

Le prix d'une reconstruction fausse est lourd : noté contre `left`, le mot rend quatre phonèmes aberrants d'un coup et entraîne `corner` avec lui. **Une reconstruction fausse ne décale pas une marque, elle en produit une rafale.**

À l'inverse, le piège se déclenche moins souvent qu'on le craignait : sur « I sink » comme sur « I am walkin », la reconnaissance a rendu `think` et `walking` d'elle-même. Le modèle de langue normalise vers le mot plausible, et la reconstruction n'a rien à corriger — mais le mot juste revenu, `AccuracyScore` absout la faute. Le piège change de main plus qu'il ne disparaît.

## À armes égales : l'écart au modèle

Azure avait été jugé sur ses notes brutes, SpeechAce sur l'écart à un modèle synthétique. La comparaison n'était donc pas équitable. Voici Azure passé à la même méthode : la même voix de synthèse dit la phrase, Azure la note, et on compare son par son.

| prise | humain | modèle | écart | cas |
|---|---|---|---|---|
| 15 | 100,0 | 100,0 | **+0,0** | témoin |
| 13 | 93,0 | 100,0 | **−7,0** | témoin |
| 14 | 74,0 | 97,0 | **−23,0** | témoin |
| 01 | 100,0 | 100,0 | 0,0 | th→s, demi-faute |
| 06 | 100,0 | 100,0 | 0,0 | iy→ih, durée |
| 09 | 100,0 | 100,0 | 0,0 | ng→n, demi-faute |
| 18 | 100,0 | 100,0 | 0,0 | ng→n **franche** |
| 17 | 80,0 | 100,0 | −20,0 | th→s **franche** |
| 07 | 51,0 | 97,0 | −46,0 | p→b |
| 08 | 48,0 | 100,0 | −52,0 | r→l |
| 16 | 48,0 | 100,0 | −52,0 | iy→ih **franche** |

**Les deux populations se chevauchent.** Le pire témoin tombe à −23, plus bas qu'une faute franche à −20 ; et quatre fautes, dont deux franches, rendent exactement 0,0. Aucun seuil ne les sépare : placé sous le pire témoin, il voit 3 fautes sur 8 ; relevé pour en voir une quatrième, il crie au loup sur une prise propre.

Le témoin 14 dit pourquoi : un /p/ correctement prononcé y est noté **74**, quand le modèle est à 97. La fausse alerte naît de la notation elle-même, pas de la méthode.

L'étalon est d'ailleurs moins net chez Azure : médiane 97,0 et 11 phonèmes sur 172 sous 90, là où le même audio passé à SpeechAce rend 100,0 de médiane et un seul sous 90.

**Pour mémoire, la comparaison sur le même jeu et la même méthode :**

| | fautes vues sans fausse alerte | dispersion des témoins |
|---|---|---|
| Azure | 3 / 8 | de 0 à −23 |
| SpeechAce | 6 / 8 | ±1 |

**Ce qui reste à l'avantage d'Azure**, et qu'il ne faut pas perdre en lisant ce tableau : `NBestPhonemes` identifie le son produit **indépendamment du texte annoncé**, ce que SpeechAce ne fait pas — son `sound_most_like` renvoie l'écho du texte fourni. Azure est donc le seul des deux à savoir dire ce qui a réellement été prononcé. Mais cette qualité porte sur un critère que le projet a rétrogradé : le remède d'une faute est désormais d'entendre un modèle, ce qui demande de savoir **où**, pas **quoi**.

## Ce qui reste incertain

Rien de ce qui précède n'a la solidité d'un résultat établi. Les limites, dans l'ordre de gravité :

- **Un seul locuteur, quatre contrastes, une à deux prises chacun.** La table des écarts ci-dessus est une indication, pas une distribution.
- **La table des confusions n'est pas éprouvée.** Elle est plausible pour un francophone, jamais mesurée, et elle n'existe pour aucune autre langue maternelle.
- **La ligne de base par session n'a jamais tourné en usage.** On ignore combien de tours il faut pour qu'un écart veuille dire quelque chose, et ce qui se passe quand un phonème n'apparaît que deux fois dans une conversation.
- **La ligne de base est instable avec l'attention du locuteur.** La même voyelle /iː/ correcte, dans le même mot, a rendu +9, +10 et +63 sur trois prises correctes. Quand on articule avec soin, tout le système vocalique se décale.
- **Le cas 5 n'a jamais été enregistré** : on ne sait donc pas si le contexte de conversation sert vraiment à la reconstruction, ou si elle corrige dès qu'un mot voisin existe.
- **Les premières lectures ont été faussées par des fautes molles.** Trois cas ont passé pour des échecs de détection avant qu'on établisse qu'ils étaient des sons intermédiaires, honnêtement rapportés comme tels. Toute campagne future doit vérifier la faute avant d'accuser le moteur.
