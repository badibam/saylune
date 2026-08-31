# Prompt — génération en aveugle de la table des réductions

À coller dans une session neuve, comme `affinity-prompt.md` et `affinity-groups-prompt.md` avant elle. Elle ne doit connaître aucun de nos échecs constatés ni les mots sur lesquels ils tombent : c'est ce qui fait du test un test. Le fichier rendu se dépose dans `tmp/affinity-reductions.json`.

Troisième édition. L'édition 2 avait élargi la liste des phénomènes et celle des conditions, et ajouté une consigne sur la façon de conclure à une absence. L'édition 3 ajoute ce qui manquait encore : les phénomènes ne se lisent pas en colonnes séparées, et une passe sur leurs coutures. Tout l'élargissement est écrit au niveau des phénomènes, jamais des réponses — aucune lettre, aucun phonème, aucun mot n'y est nommé, pour que la session reste aveugle.

Joindre `bench/affinity.json` à la session — sans elle, la consigne « ne redonne pas les correspondances orthographiques » oblige à deviner ce que contient une table qu'on n'a pas sous les yeux, et fait retirer des entrées sur une supposition.

---

Tu construis une table pour l'anglais **parlé à débit courant**. C'est une table de données, mais elle se gagne par une exploration écrite avant d'être écrite : la partie qui compte est le balayage, le JSON n'en est que le dépôt.

INVENTAIRE DE PHONÈMES — exactement celui-ci, 38 symboles, aucun autre :

  ɑ æ ə aʊ aɪ b ʧ d ð ɾ ɛ ɝ eɪ f g h ɪ i ʤ k l m n ŋ oʊ ɔɪ p ɹ s ʃ t θ ʊ u v w j z

Un phénomène bien réel dont le produit n'est pas dans cette liste ne se note pas. Il n'y a pas de symbole de remplacement à lui trouver : il sort, simplement.

LETTRES — les 26 de a à z, plus l'apostrophe '.

CE QUE CETTE TABLE N'EST PAS. La table ci-jointe dit déjà à quels phonèmes une lettre participe **dans l'orthographe** : `s` participe à /ʃ/ par `sh`, `e` participe à /i/ par `ee`. Ne refais pas ce travail — mais ne t'auto-censure pas non plus par supposition : la table est jointe, vérifie. Ici on ne parle plus de comment un mot s'écrit, mais de ce que sa prononciation devient quand il est dit vite, sans soin, dans une phrase — et jamais isolé.

LA QUESTION EXACTE :

  « Quand un mot anglais est dit à débit courant, à quels phonèmes cette lettre
    peut-elle se retrouver associée, du fait des réductions et des assimilations
    de la parole ordinaire — et non du fait de l'orthographe ? »

## Phase 1 — le balayage, en prose, avant tout JSON

C'est la partie longue et c'est celle qui décide de la qualité. Écris-la.

**Balaye les phénomènes.** Pour chacun, énumère les cas de figure concrets qu'il produit en anglais, et dis quelles lettres s'y trouvent embarquées :

  - RÉDUCTION VOCALIQUE — une voyelle non accentuée se relâche, se centralise, ou disparaît.
  - MONOPHTONGAISON — une diphtongue perd sa cible finale et se rend sur un seul
    timbre. Elle ne se centralise pas et ne disparaît pas : elle se raccourcit.
    C'est un phénomène de vitesse distinct de la réduction vocalique, et il ne se
    trouve pas en la balayant.
  - ASSIMILATION — une consonne prend un trait de sa voisine : lieu d'articulation,
    voisement, nasalité. Pose la question dans les deux sens : de deux sons
    voisins, l'un et l'autre peuvent être celui qui cède, et un balayage qui
    n'en regarde qu'un rate la moitié des cas.
  - ÉLISION — une consonne ou une voyelle s'efface, et la lettre reste.
  - COALESCENCE — deux sons voisins fusionnent en un troisième qui n'est ni l'un ni l'autre.
  - BATTEMENT ET AFFAIBLISSEMENT — une occlusive se réalise en battement, en occlusion glottale, ou sans relâchement.
  - SYLLABES CONSONANTIQUES — une consonne porte la syllabe à la place d'une voyelle disparue.
  - CONTRACTION — ce qui reste d'une forme réduite soudée au mot d'avant, et ce que
    ce reste devient ensuite. Réduit à un ou deux segments, il n'a plus de mot pour
    le porter : il se comporte comme un segment nu entre ses voisins, et cède là où
    le même son, dans un mot plein, aurait tenu.

**Balaye les coutures.** Ces phénomènes ne se produisent pas en colonnes séparées :
la sortie de l'un est l'entrée d'un autre. Un son qui vient d'être réduit, élidé ou
soudé n'est plus dans la situation où tu l'as examiné la première fois — il a perdu
le mot qui le portait, ou ses voisins ne sont plus les mêmes —, et ce qu'il fait à
partir de là ne se trouve pas en balayant le phénomène qui l'y a amené. Reprends
donc la liste une seconde fois, en appliquant chaque phénomène non plus aux mots
dits lentement, mais à ce que les autres viennent de produire. Un cas qui
n'appartient en propre à aucune ligne de la liste appartient à cette passe-là, et
c'est le seul endroit où tu le trouveras.

**Balaye les conditions.** Le même phénomène ne se produit pas partout, et une table écrite sans ce balayage ne couvre que les cas qui viennent à l'esprit en premier. Pour chaque phénomène ci-dessus, passe en revue :

  - la POSITION dans le mot — initiale, médiane, finale ;
  - l'ACCENT — syllabe accentuée ou non, et quel est le rang de la syllabe.
    N'assimile pas « réduction » à « syllabe inaccentuée » : c'est le raccourci le
    plus coûteux de tout ce balayage. Un phénomène de vitesse s'installe très bien
    sur la syllabe accentuée d'un mot, et d'autant mieux que le mot est court et
    fréquent ;
  - le VOISINAGE — quel son précède, quel son suit, et par-dessus la frontière de mot autant qu'à l'intérieur ;
  - la CLASSE DU MOT — un mot outil (article, préposition, auxiliaire, pronom) se réduit là où un mot plein résiste ;
  - la FRÉQUENCE — un mot très fréquent porte des réductions qu'un mot rare ne porte jamais.

**Balaye les 26 lettres, une par une, sans en sauter.** Pour chacune, dis ce que le balayage ci-dessus lui a fait gagner, ou dis explicitement qu'elle ne gagne rien. Une lettre sans entrée doit être une **décision**, pas un oubli — et beaucoup n'en auront aucune, c'est normal. Traite l'apostrophe comme les autres.

**Et regarde comment tu conclus à une absence.** Un « cette lettre ne gagne rien »
qui s'appuie sur une règle générale de la langue — telle classe de sons ne
s'assimile pas, tel phénomène ne touche que telle position — est le genre
d'affirmation qui se croit bien et se vérifie mal, et c'est là que les cas manqués
se logent. Quand tu es sur le point d'écarter une lettre au nom d'une règle,
reviens d'abord aux environnements concrets où elle se trouve, et ne conclus
qu'après. Une absence tient très bien ; ce qui ne tient pas, c'est de la fonder sur
la règle seule.

**Puis relis dans l'autre sens.** Pour chacun des 38 phonèmes, demande-toi quelles lettres peuvent finir sur lui par réduction, et compare avec ce que la phase par lettre a produit. Les deux sens doivent s'accorder ; là où ils divergent, c'est qu'un cas a été manqué dans l'un des deux — ajoute-le ou justifie de le laisser.

## Phase 2 — la table

Seulement une fois la phase 1 écrite.

CE QUE TU NE DOIS PAS FAIRE :

  - Ne redonne pas une correspondance qui est déjà dans la table jointe au même titre. Si une lettre écrit déjà ce phonème dans un mot dit lentement, ce n'est pas une réduction.
  - Ne raisonne sur aucun mot particulier au moment d'écrire les poids. Les mots servent d'exemples pendant le balayage ; la table, elle, porte sur la lettre.
  - Ne traite pas un accent régional comme une réduction. Ce qui est visé est ce que fait la vitesse, pas ce que fait la géographie.
  - N'écris aucun mot d'exemple dans le JSON.

POIDS — un entier, jamais un booléen, même échelle que les deux autres tables :

  3 — réduction très courante, qu'on entend dans n'importe quelle phrase dite
      normalement.
  2 — courante, sans être systématique.
  1 — attestée mais occasionnelle, ou liée à un environnement étroit.
  absent — cette lettre ne se retrouve jamais sur ce phonème par réduction.

Sois avare de 3 et généreux de 1. Une table trop généreuse en 3 laisserait une réduction l'emporter sur une orthographe ordinaire, ce qui la rendrait nuisible. Mais un 1 ne coûte presque rien : dans le doute entre « absent » et « 1 », mets 1.

PORTÉE — anglais général américain ET britannique standard. Un phénomène présent dans l'un des deux compte ; ne tranche pas entre eux.

SORTIE — après la prose de la phase 1, du JSON seul, une clé par lettre concernée uniquement (les lettres sans réduction sont simplement absentes), dans l'ordre alphabétique, l'apostrophe en dernier si elle a des entrées :

{
  "a": {"ə": 3},
  "d": {"ɾ": 3}
}

L'exemple est là pour la forme, pas pour son contenu — ne le reprends pas tel quel. Chaque symbole que tu écris doit venir de la liste des 38.

Écris le JSON dans un fichier nommé `affinity-reductions.json`.
