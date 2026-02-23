# TODO `tags-core`

Ce fichier sert de mémoire de conception et de pilotage pour `tags-core`.
Il liste :

- ce qu'il reste à implémenter
- les problèmes potentiels de logique déjà visibles
- les écarts entre le `README` et l'état réel du code

Le but est de pouvoir reprendre le sujet sans dépendre du contexte oral.

## 1) Bascule des objets métier de `Hashtag` vers `TagId` (pas encore implémenté)

Aujourd'hui les objets métier de `models-core` utilisent encore l'ancien système (`hashtags: List<Hashtag>`).

Le travail à faire n'est pas une migration de données : le logiciel est en cours de construction.
On peut faire évoluer le modèle et le code sans gérer de migration.

Découpage du travail :
- 1.1 ajouter le nouveau système à côté de l'ancien dans les objets métier (`tags: List<TagId>` en plus de `hashtags`)
- 1.2 gérer la persistance et les commandes/requêtes backend pour lire / attacher / retirer les `TagId` sur `Model`, `Entity`, etc.
- 1.3 brancher l'UI sur le nouveau système (c'est là que l'usage réel se joue pendant la transition)
- 1.4 supprimer l'ancien système `Hashtag` (modèle, persistance, commandes/requêtes, UI)

Règle technique :
- utiliser `TagId` comme référence dans les objets métier (pas `TagRef` par key), pour ne pas casser les liens si une key de tag change

## 2) Règles pour autoriser/refuser l'attachement d'un tag à un objet (pas encore implémenté)

Ce point concerne le moment où le point 1 existera (attacher un tag à un objet métier).

Quand on voudra faire :
- "attacher le tag T à l'objet O"

il faudra surtout vérifier qu'on n'attache pas à `O` un tag free qui vient d'un autre scope local.
(`managed` global pourra en général être autorisé, selon la règle du module métier.)

Le code actuel a déjà l'infrastructure de scope (`TagScopeRef`) mais pas encore la règle métier
qui dit quand refuser un tag parce qu'il vient du mauvais scope local.

Exemple de règle attendue (plus tard) :
- un objet de `model-core` accepte :
  - les tags managed (scope global)
  - les tags free du scope de son modèle
- il refuse les tags free d'un autre modèle

Description de ce qu'il faudra faire :
- définir la validation au moment de l'attachement (surtout refuser les tags free d'un autre scope local)
- décider où vit la règle (probablement dans les modules consommateurs, pas dans `tags-core`)
- brancher cette validation dans les futures commandes d'attachement de tags
