# TODO `tags-core`

Ce fichier sert de mémoire de conception et de pilotage pour `tags-core`.
Il liste :

- ce qu'il reste à implémenter
- les problèmes potentiels de logique déjà visibles
- les écarts entre le `README` et l'état réel du code

Le but est de pouvoir reprendre le sujet sans dépendre du contexte oral.
Il ne doit contenir que du travail restant : ce qui est fait doit être supprimé du `TODO`.

## 1) Bascule des objets métier de `Hashtag` vers `TagId` (en cours, largement avancée)

La bascule `models-core` -> `TagId` est déjà engagée et le flux principal backend est en place.
Le domaine modèle (`Model`, `Entity`, `Relationship`, `Attribute`) stocke désormais des `tags: List<TagId>`.

L'ancien système `Hashtag` a déjà été retiré du domaine `models-core` et des commandes/réducteurs concernés.
Il reste du nettoyage/alignement autour de modules adjacents et de l'expérience UI/recherche.

Le travail à faire n'est pas une migration de données : le logiciel est en cours de construction.
On peut faire évoluer le modèle et le code sans gérer de migration.

Travail restant sur ce point :
- UI sur le nouveau système: détails dans [TAGS_UI.md](./TAGS_UI.md)

Voir [TAGS_UI.md](./TAGS_UI.md) pour le détail UI (décisions, tâches, questions ouvertes).

## 2) Recherche de `models-core` avec filtres tags (migration vers `TagRef`) à consolider

L'action `ModelAction.Search` a été migrée vers des filtres tags `SearchFilterTags` en `TagRef` (au lieu de strings de hashtags) et la couverture de tests backend
est maintenant en place dans `ModelSearchTest`.
Décision actée :
- quand un `TagRef` de filtre est introuvable, la recherche échoue avec `TagNotFoundException` (pas d'ignorance silencieuse).

## 3) Couverture de tests à compléter

## 3.1 Recherche de tags `TagAction.TagSearch`

Décisions actées:
- `TagSearch` remplace l'ancien `TagList`
- le premier filtre supporté est `scopeRef`
- si `filters` est absent ou vide, la recherche retourne tous les tags connus

Travail restant:
- ajouter des tests de parsing JSON pour `TagSearchFilters`
- ajouter des tests de filtres invalides (type inconnu, condition inconnue,
  valeur manquante, structure JSON invalide)
- décider si `TagSearch` doit valider strictement l'existence et la validité du
  scope référencé par un filtre `scopeRef`
- si cette validation stricte est retenue, implémenter les erreurs explicites
  correspondantes dans `TagQueries.search`
- utiliser `TagSearch` comme contrat backend de chargement des tags pour l'UI
  selon le contexte d'affichage

### 3.2 Recherche ModelAction.search
- tests de parsing JSON des filtres de tags (`TagRef`)
- tests de refs invalides (parsing / format API)

## 4) Permissions locales par scope (lot ultérieur, non implémenté)

Décision actée :
- état actuel: les opérations free create/update/delete vérifient le rôle global `tag_free_manage`
- futur: après ce contrôle global, déclencher `onBeforeTagFreeCreate` et `onBeforeTagFreeUpdate` avec le `TagRef` concerné (le `onBeforeTagFreeDelete` est déjà implémenté ainsi)
- les modules propriétaires de scope pourront veto l'opération selon leurs permissions locales
- ce mécanisme devra être appliqué de la même manière aux autres types de scopes

## 5) Improvements later

- certains `TagGroup` doivent pouvoir imposer qu'un seul de leurs tags soit
  présent à la fois sur un objet donné (exemple: on ne peut pas avoir
  `security:public` et `security:internal` en même temps)
- d'autres groupes doivent rester multi-valeurs (exemple: `country:fr` et
  `country:de` autorisés en même temps)
- offrir des opérations massives sur les tags pour faciliter la gouvernance:
  - promotion d'un tag local vers un tag global
  - fusion de tags
  - remplacement d'un tag par un autre
