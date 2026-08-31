# Prompt — génération en aveugle de la table des graphèmes multi-lettres

À coller dans une session neuve, comme `affinity-prompt.md` avant elle. Elle ne doit rien savoir de nos quinze ratés, ni des mots sur lesquels ils tombent : c'est ce qui fait du test un test. Le fichier rendu se dépose dans `tmp/affinity-groups.json`.

---

Tu construis une table d'affinité graphème↔phonème pour l'anglais, restreinte aux **graphèmes de plusieurs lettres**. C'est une table de données : tu la génères de tête, sans rien mesurer, sans code.

INVENTAIRE DE PHONÈMES — exactement celui-ci, 38 symboles, aucun autre :

  ɑ æ ə aʊ aɪ b ʧ d ð ɾ ɛ ɝ eɪ f g h ɪ i ʤ k l m n ŋ oʊ ɔɪ p ɹ s ʃ t θ ʊ u v w j z

CE QU'EST UN GRAPHÈME MULTI-LETTRES ICI — un groupe de **deux à quatre lettres contiguës** qui, dans l'orthographe anglaise, écrit **un seul phonème**. Le critère est là et nulle part ailleurs :

  - `sh` est un graphème : il écrit /ʃ/, un phonème.
  - `bl` n'en est pas un : il écrit /b/ puis /l/, deux phonèmes qui gardent chacun leur lettre.

Énumère les graphèmes productifs de l'anglais — consonantiques et vocaliques, les digrammes courants comme les groupes plus longs et plus rares. Vise la couverture : un graphème oublié est un mot que la table ne saura pas écrire. N'invente pas de groupe qui n'existe que dans un ou deux mots d'emprunt.

LA QUESTION EXACTE :

  « Quand ce groupe de lettres apparaît dans l'orthographe d'un mot anglais,
    à la réalisation de quels phonèmes PARTICIPE-T-IL ? »

Un même groupe en écrit souvent plusieurs selon le mot, et c'est attendu : donne-les tous, pondérés. Un groupe dont une lettre est muette compte comme un seul graphème — c'est le groupe entier qui écrit le phonème, et c'est précisément ce que cette table apporte que la table par lettre ne peut pas dire.

POIDS — un entier, jamais un booléen, même échelle que la table par lettre :

  3 — la façon principale dont ce groupe se prononce, ou la façon principale
      dont ce phonème s'écrit avec ce groupe. Fréquent et typique.
  2 — courant, sans être le cas principal.
  1 — attesté mais marginal, ou limité à quelques mots.
  absent — ce groupe n'écrit jamais ce phonème.

Sois avare de 3 et généreux de 1.

PORTÉE — anglais général américain ET britannique standard. Une correspondance valable dans l'un des deux accents compte ; ne tranche pas entre eux.

CE QU'IL NE FAUT PAS FAIRE — ne donne aucune entrée pour une lettre seule : la table par lettre existe déjà et celle-ci ne la remplace pas. Ne donne pas de mot d'exemple dans le JSON. Ne classe pas par fréquence.

SORTIE — du JSON et rien d'autre, aucune prose avant ni après, une clé par graphème, dans l'ordre alphabétique :

{
  "ai": {"eɪ": 3, "ɛ": 2},
  "sh": {"ʃ": 3},
  "ough": {"ə": 2, "u": 1, "oʊ": 1, "ɔ": 1}
}

Attention : `ɔ` ne fait pas partie de l'inventaire ci-dessus — l'exemple est là pour la forme, pas pour son contenu. Chaque symbole que tu écris doit venir de la liste des 38.

Écris-le dans un fichier nommé `affinity-groups.json`.
