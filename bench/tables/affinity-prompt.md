# Prompt — génération en aveugle de la table d'affinité

À coller dans une session neuve. Elle ne doit rien savoir de nos huit ratés : c'est ce qui fait du test un test. Le fichier rendu se dépose dans `tmp/affinity.json`.

---

Tu construis une table d'affinité graphème↔phonème pour l'anglais. C'est une table de données : tu la génères de tête, sans rien mesurer, sans code.

INVENTAIRE DE PHONÈMES — exactement celui-ci, 38 symboles, aucun autre :

  ɑ æ ə aʊ aɪ b ʧ d ð ɾ ɛ ɝ eɪ f g h ɪ i ʤ k l m n ŋ oʊ ɔɪ p ɹ s ʃ t θ ʊ u v w j z

LETTRES — les 26 de a à z, plus l'apostrophe '.

LA QUESTION EXACTE, et elle n'est pas celle qu'on pose d'habitude :

  « Quand cette lettre fait partie de l'orthographe d'un mot anglais, à la
    réalisation de quels phonèmes PARTICIPE-T-ELLE ? »

Participer, pas égaler. Une lettre peut participer à un phonème qu'elle ne produit pas seule, parce qu'elle appartient au groupe de lettres qui l'écrit. Exemples du raisonnement attendu :

  - `s` participe à /ʃ/ (par `sh`), pas seulement à /s/ et /z/.
  - `h` participe à /ʃ/, /ʧ/, /θ/, /ð/ (par `sh`, `ch`, `th`), pas seulement /h/.
  - `g` participe à /aɪ/ et /f/ (par `igh`, `gh`), pas seulement /g/ et /ʤ/.
  - `e` participe à /i/ (par `ee`, `ea`), à /ɛ/, à /ɝ/ (par `er`).

Manquer ces participations rend la table inutile : c'est son intérêt principal.

POIDS — un entier, jamais un booléen :

  3 — la façon principale dont ce phonème s'écrit avec cette lettre, ou la
      façon principale dont cette lettre se prononce. Fréquent et typique.
  2 — courant, sans être le cas principal.
  1 — attesté mais marginal, ou limité à quelques mots.
  absent — cette lettre ne participe jamais à ce phonème.

Sois avare de 3 et généreux de 1. Une table où tout vaut 3 ne sert à rien.

PORTÉE — anglais général américain ET britannique standard. Une correspondance valable dans l'un des deux accents compte ; ne tranche pas entre eux.

LETTRES MUETTES — n'invente aucune entrée pour dire où une lettre muette est « absorbée ». Le `e` de *love* ne reçoit pas d'entrée vers /v/. Donne à chaque lettre ses affinités propres et rien de plus ; une lettre souvent muette aura simplement peu d'entrées, et c'est correct.

SORTIE — du JSON et rien d'autre, aucune prose avant ni après, une clé par lettre, dans l'ordre alphabétique, l'apostrophe en dernier :

{
  "a": {"æ": 3, "eɪ": 3, "ɑ": 3, "ə": 3, "ɛ": 2},
  "b": {"b": 3},
  "'": {}
}

Écris-le dans un fichier nommé `affinity.json`.
