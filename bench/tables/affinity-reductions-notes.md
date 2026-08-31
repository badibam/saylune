# Notes de génération — `affinity-reductions.json`

Compagnon de `tmp/affinity-reductions.json`, généré depuis `tmp/affinity-reductions-prompt.md`
(édition 3), avec `bench/affinity.json` joint. Ce fichier consigne *pourquoi* chaque entrée
est là, pour qu'un relecteur puisse contester une entrée précise plutôt que la table entière.

Résultat : 20 lettres, 58 entrées. Deux poids 3 seulement (`n → m` et `' → z`).

## Règle de décision appliquée

- Un phonème déjà présent pour la lettre dans `bench/affinity.json` n'est **jamais** redonné :
  il relève de l'orthographe, pas de la réduction. Vérifié mécaniquement, pas de mémoire.
- Un phénomène réel dont le produit n'est pas dans les 38 symboles est écarté sans substitut.
- Poids : 3 = s'entend dans n'importe quelle phrase ; 2 = courant sans être systématique ;
  1 = attesté mais occasionnel ou lié à un environnement étroit. Dans le doute entre absent
  et 1, j'ai mis 1.

## Ce que chaque phénomène a rendu

| Phénomène | Rendement |
|---|---|
| Réduction vocalique | **rien en propre** — a e i o u y portent déjà ə et ɪ. Son rendement passe entièrement par les syllabes consonantiques. |
| Monophtongaison | `i → ɑ`, `y → ɑ` (/aɪ/ raccourci) ; `y → ɛ` (/eɪ/ raccourci) ; `w → ʊ, ə` (/oʊ/ final de « -ow »). Les autres lettres qui écrivent ces diphtongues portaient déjà la cible. |
| Assimilation régressive, lieu | `n → m` (3), `t → p, k`, `d → b, g`. |
| Assimilation régressive, nasalité | `d → n`. |
| Assimilation progressive | le /ð/ des mots outils cède au son précédent : `t, h → n, l, z, d`. C'est le sens qu'un balayage à sens unique rate. |
| Voisement | `v → f` (2) ; `b → p`, `g → k`, `k → g`, `c → g` (1). |
| Élision | **rien directement** : la lettre reste, aucun phonème gagné. Tout son rendement est dans les coutures. |
| Coalescence | côté gauche déjà couvert (t:ʧ, d:ʤ, s:ʃ) ; **côté droit absent partout** → `y → ʧ, ʤ, ʃ` et `u → ʧ, ʤ`. |
| Battement / affaiblissement | t→ɾ et d→ɾ déjà là ; reste `g → n` (chute du vélaire de « -ing »). |
| Syllabes consonantiques | le plus productif pour les voyelles : `a e i o u → l, n, m`. |
| Vocalisation du l | `l → ʊ, u`. Phénomène de vitesse, pas de géographie. |
| Contraction | l'apostrophe était **vide** dans la table jointe → 10 entrées, la plus grosse lacune trouvée. |

## Les coutures (2e passe : phénomène appliqué à la sortie d'un autre)

Six cas qui n'appartiennent en propre à aucune ligne ci-dessus :

- « you » → /jə/ **puis** coalescence avec /t d s/ précédent. Sans cette passe, `y` ne gagne rien.
- « and » → /ənd/ → /ən/ → /n̩/ : élision puis syllabe consonantique → `a → n`, `d → n`.
- « going/want to » → /gənə, wɑnə/ : le /t/ tombe, sa place est reprise par le /n/ → renforce `t → n`.
- « give me » → /gɪmi/ : le /v/ réduit, sans mot porteur, cède à la nasale → `v → m`.
- « is she » → /ɪʃʃi/ : devoisement **puis** assimilation de lieu → `z → ʃ`.
- « -ow » final : monophtongaison sur une syllabe déjà inaccentuée → `w → ə, ʊ`.

## Absences — décidées, pas oubliées

Écartées après retour aux environnements concrets, jamais sur la règle générale seule :

- **f** — dans « of » réduit, le /v/ tombe et ne laisse rien à la lettre ; f porte déjà v.
- **j** — ni source ni cible d'assimilation en anglais.
- **p** — dans *open, happen*, c'est le /n/ qui devient /m/, le /p/ tient.
- **q** — aucun environnement.
- **r** — la chute du r est géographique (exclue par la portée) ; r porte déjà ə et ɝ.
- **s** — porte déjà s, z, ʃ : ses trois sorties possibles.
- **x** — porte déjà k, s, z, g, ʃ.

À l'inverse, **b c k m v w z** ne gagnaient rien tant que je m'en tenais aux règles générales ;
ce sont les environnements concrets qui les ont fait entrer. C'est exactement le piège que
l'édition 3 signale.

## Sorties hors inventaire (écartées sans substitut)

- occlusion glottale : `t → ʔ` — pas de symbole dans les 38.
- `/z/ + /j/ → /ʒ/` — /ʒ/ absent des 38.
- voyelles nasalisées, /ɔ/, /ʌ/ distinct de /ə/ — absents.

## Relecture par phonème (sens inverse)

/m/ ← n(3), a, e, o, u, v, ' — /n/ ← t, d, g, h, a, e, i, o, u, m, ' — /l/ ← t, h, a, e, i, o, u, ' —
/ʊ/ ← l, w — /p k b g/ ← t, d, b, c, k, g — /ɑ/ ← i, y (monophtongaison seule).
Aucune divergence non résolue entre le balayage par lettre et le balayage par phonème.

## Points contestables en priorité

Si la table doit être resserrée, c'est ici que je regarderais d'abord :

1. `a e i o u → l, n, m` (syllabes consonantiques) — 12 entrées d'un coup, sur un raisonnement
   d'alignement lettre↔syllabe plutôt que lettre↔phonème.
2. `l → ʊ` à 2 — frontière ténue entre vitesse et registre régional.
3. `t, h → l, z, d` — l'assimilation progressive du /ð/ est réelle mais l'attribution au
   digramme *th* est une convention de ma part.
4. `' → z` à 3 — seul 3 de l'apostrophe ; justifié par la fréquence de « it's / he's / that's ».
