# TODO `models-core`

Ce fichier liste uniquement le travail restant et les écarts entre le contrat
cible du module (décrit dans `README.md`) et l'état actuel du code.

Ce qui est implémenté doit rester dans le README.
Ce qui ne l'est pas encore doit être suivi ici, puis supprimé une fois livré.

## 1) Actions `ModelAction` non implémentées dans `ModelActionProvider`

État actuel visible:

- `Model_UpdateKey`: non implémenté
- `RelationshipRole_Create`: non implémenté
- `RelationshipRole_UpdateKey`: non implémenté
- `RelationshipRole_UpdateName`: non implémenté
- `RelationshipRole_UpdateEntity`: non implémenté
- `RelationshipRole_UpdateCardinality`: non implémenté
- `RelationshipRole_Delete`: non implémenté

Attendu cible:

- `ModelActionProvider` route aussi ces actions vers des commandes métier
  (`ModelCmds` / `ModelQueries`) comme les autres actions déjà branchées.

## 2) Éviter les tags dupliqués sur un même objet

Écart actuel à traiter:

- un même objet (`Model`, `Entity`, `Relationship`, `Attribute`) ne doit pas
  pouvoir recevoir deux fois le même tag (`TagId`)
