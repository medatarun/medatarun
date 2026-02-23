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

## 2) Validation simple de scope au moment d'attacher un tag (pas encore implémenté)

Quand les actions métier attacheront un tag à un objet, elles passeront probablement un `TagRef`
(et l'objet métier stockera un `TagId`).

Le contrôle attendu est simple :
- autoriser un tag global (si le module appelant l'autorise)
- autoriser un tag local du même scope que l'objet ciblé
- refuser un tag local d'un autre scope

Exemple `model-core` :
- objet dans le scope du modèle `M1`
- tag free du scope `M1` -> ok
- tag free du scope `M2` -> refusé
- tag managed global -> ok (si autorisé)

Ce qu'il faudra faire :
- ajouter dans `tags-core` un helper du type `ensureTagCanBeAttachedToScope(targetScopeRef, tagRef, authorizeGlobal = true)`
- utiliser ce helper dans les commandes des modules métiers avant d'attacher le tag
- stocker le `TagId` résolu dans les objets métier (pas le `TagRef`)
