# TAGS_UI

Ce document centralise les décisions UI et le travail restant lié à
l'intégration de `tags-core`.

## Contexte

Depuis la mise en place de `tags-core`, l'UI du projet ne fonctionne plus.
Le projet est passé d'un système de hashtags libres (strings) au système
structuré de tags (`TagId`, `TagRef`, `scope`, `TagGroup`).

## Décisions UI déjà prises

- l'UI se base sur `TagAction` pour gérer les tags et récupérer les listes
- pour modifier les tags côté `models`, l'UI utilise les actions de
  `models-core`
- le composant d'attache/détache de tags doit être réutilisable pour d'autres
  modules (ex: `prompts`) avec le même comportement visuel
- ce composant reçoit les fonctions backend à appeler (attache/détache, create,
  update)
- ce composant doit permettre d'attacher/détacher des tags globaux et des tags
  du scope de l'objet qui l'invoque
- l'UI affiche des tags métiers (name, key, scope, group), pas uniquement des
  IDs
- quand un tag est affiché, `name` est prioritaire et la description doit être
  consultable (avec key/groupe/scope) dans une vue de détail
- toutes les communications UI -> backend utilisent `TagRef.ById`
  (`id:<TagId>`) et jamais `TagRef.ByKey`

## Travail restant UI

- charger et afficher des listes de tags réels (name, key, scope, groupe si
  pertinent)
- adapter l'affichage des tags dans les écrans model/entity/relationship/
  attribute
- adapter la sélection de tags (ajout/suppression) pour une sélection métier
  correcte à l'écran
- conserver `TagId` uniquement pour le stockage interne des objets métier
- adapter la recherche UI pour sélectionner des tags proprement et construire
  les filtres correspondants
- implémenter un écran de gestion des tags globaux et de leurs groupes
  (structure d'écran à définir)
- implémenter un écran de recherche de tags (globaux + locaux)
  (structure d'écran à définir)

## Questions ouvertes

- UX cible de sélection de tags dans `models-core`: chargement, affichage,
  recherche, création éventuelle à la volée
