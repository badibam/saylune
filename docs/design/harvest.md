# À moissonner vers la sagesse

Trois leçons d'établi qui vivaient dans `../reference.md` et n'y avaient pas leur place : elles ne parlent pas de l'app. Elles attendent ici d'être poussées vers leur facette par `/update push`, et **ce fichier se vide à mesure**.

## Vers `universel` — indexer des chemins nommés, jamais `git add -A`

`git add -A` et `git add .` indexent ce qui **se trouve** dans l'arbre, y compris ce que personne n'y a mis — et un intrus qui n'est pas un fichier ordinaire fait échouer l'indexation entière, avec un message qui nomme l'intrus sans dire d'où il vient (« ne peut ajouter que des fichiers normaux, des liens symboliques ou des répertoires »).

Cas rencontré : le bac à sable des commandes de l'agent neutralise les fichiers de configuration qu'il refuse de laisser lire — `.bash_profile`, `.gitconfig`, `.mcp.json`, `.vscode` et d'autres — en montant `/dev/null` par-dessus, **dans le dossier courant**. Ils apparaissent donc à la racine du projet comme périphériques caractère, et `git status` les voit non suivis. Ils n'existent que dans la vue du bac à sable : le dépôt sur le disque est propre, et il ne faut donc **pas** les gitignorer — ce serait mettre dans le projet une ligne qui parle d'un outil.

## Vers `universel` — une recherche aveuglée par le bac à sable rend zéro résultat, pas la preuve d'une absence

Le bac à sable des commandes de l'agent **interdit la lecture du home**. Un `find / -name adb` y rend une liste vide alors qu'`adb` est dans `~/adb/platform-tools/`, en PATH — et `which adb` échoue pour la même raison. Le vide ne dit pas « ça n'existe pas », il dit « je n'ai pas pu regarder là », et les deux se ressemblent exactement.

C'est la même forme que la règle de `dev_base` sur les tâches de fond, où un `pgrep` rend une liste vide que la tâche tourne ou non. La conséquence : **ce qui vit dans le home ne se cherche jamais depuis le bac à sable**. On relance avec le bac à sable désactivé avant de conclure quoi que ce soit.

Les outils d'un projet, eux, vivent dans le dépôt et se cherchent normalement — avec une exception à connaître : un venv rangé dans `tmp/` est gitignoré, donc absent de tout parcours de l'arbre versionné.

## Vers `android` — le build a besoin du réseau, le bac à sable le lui refuse

`./gradlew` va chercher la distribution Gradle, puis les dépendances, sur le réseau. Sous le bac à sable des commandes de l'agent, il échoue en `UnknownHostException: services.gradle.org`, une panne de nom qui ne ressemble en rien à une restriction. **Ces commandes se lancent avec le bac à sable désactivé**, sans passer par la boucle « essayer, lire l'erreur, réessayer ».
