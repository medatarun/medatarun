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
- UI sur le nouveau système, avec usage métier complet des tags (pas seulement IDs bruts)

Règle technique :
- utiliser `TagId` comme référence dans les objets métier (pas `TagRef` par key), pour ne pas casser les liens si une key de tag change

### Détail 1.3 UI tags

L'UI ne doit pas manipuler seulement des IDs bruts.

À faire :
- charger et afficher des listes de tags réels (nom, key, scope, groupe si pertinent)
- adapter l'affichage des tags dans les écrans modèle/entité/relation/attribut
- adapter la sélection de tags (ajout/suppression) pour une sélection métier correcte à l'écran
- envoyer des `TagRef` via l'API pour les opérations qui attachent ou détachent un tag à un objet (transport API = `TagRef`, UX = tags réels)
- côté UI, utiliser uniquement des `TagRef` au format `id:<TagId>` pour les opérations d'attache/détache
- conserver `TagId` uniquement pour le stockage interne des objets métier
- adapter l'UI de recherche pour sélectionner des tags proprement et construire les filtres correspondants

## 2) Recherche de `models-core` avec filtres tags (migration vers `TagRef`) à consolider

L'action `ModelAction.Search` a été migrée vers des filtres tags `SearchFilterTags` en `TagRef` (au lieu de strings de hashtags) et la couverture de tests backend
est maintenant en place dans `ModelSearchTest`.
Décision actée :
- quand un `TagRef` de filtre est introuvable, la recherche échoue avec `TagNotFoundException` (pas d'ignorance silencieuse).

## 3) Import Frictionless : création de tags à partir des `keywords` (métier à clarifier)

L'import Frictionless pré-crée maintenant des tags free en scope local `model/<modelId>` et injecte leurs `TagId` dans le modèle.
C'est la bonne direction métier, mais plusieurs règles doivent être clarifiées/renforcées.

Décision actée (ordre du flux) :
- 1/ on résout / crée les tags dans le scope du futur `ModelId`
- 2/ on crée le modèle avec les `TagId` déjà attachés
- le flux restant transactionnel, un échec rollbacke l'ensemble ; il n'y a pas de gain métier à inverser l'ordre

Points à traiter ensemble :
- politique `keyword` -> `TagKey` :
  - aujourd'hui on réutilise le `keyword` presque brut (trim + distinct)
  - il faut décider une règle explicite (normalisation, rejet, mapping, reporting)
- validation du scope local `model/<modelId>` pendant l'import :
  - l'import crée les tags avant persistance du modèle
  - `ModelExtension` déclare maintenant le scope `model` avec existence réelle via `ModelQueries`
  - `FrictionlessdataExtension` a encore un comportement permissif pour permettre la précréation avant persistance
  - il faut décider comment unifier / encadrer proprement cette coexistence

## 4) Couverture de tests à compléter

## 4.1 Liste de tags `TagAction.TagList`

État actuel:
- la sortie de `TagList` inclut `scope` et n'inclut pas `isManaged`.

À faire (avant UI tags):
- faire évoluer `TagList` pour accepter en entrée `scopeType` et `scopeId`
- appliquer une validation stricte: seuls les cas listés ci-dessous sont
  valides; toute autre combinaison de paramètres ou tout objet/scope introuvable
  retourne une erreur explicite
- comportement attendu:
  - si `scopeType` et `scopeId` sont absents, retourner tous les tags connus
  - en mode sans filtre, si un tag local référence un scope introuvable,
    retourner une erreur explicite avec échec global (pas de résultat partiel)
  - si `scopeType = global` et `scopeId` est absent, retourner uniquement les
    tags du scope global
  - si `scopeType = global` et `scopeId` est présent, retourner une erreur
    explicite
  - si `scopeType` ne correspond ni à `global` ni à un scope local enregistré,
    retourner une erreur explicite
  - si `scopeType` correspond à un scope local et `scopeId` est absent,
    retourner une erreur
  - si `scopeType` correspond à un scope local, `scopeId` est fourni et ce
    scope existe, retourner uniquement les tags de ce scope local
  - si `scopeType` correspond à un scope local, `scopeId` est fourni mais le
    scope n'existe pas, retourner une erreur explicite
- utiliser ce filtrage pour que l'UI charge des listes de tags pertinentes
  selon le contexte d'affichage

### 4.2 Recherche ModelAction.search
- tests de parsing JSON des filtres de tags (`TagRef`)
- tests de refs invalides (parsing / format API)


### 4.3 Import Frictionless
- tests de création effective des tags via `TagCmds`
- tests de déduplication des keywords
- tests de propagation des `TagId` dans `Model.tags` / `Entity.tags`
- tests des keywords incompatibles avec `TagKey` (règle à définir)

## 5) Permissions locales par scope (lot ultérieur, non implémenté)

Décision actée :
- état actuel: les opérations free create/update/delete vérifient le rôle global `tag_free_manage`
- futur: après ce contrôle global, déclencher `onBeforeTagFreeCreate` et `onBeforeTagFreeUpdate` avec le `TagRef` concerné
- les modules propriétaires de scope pourront veto l'opération selon leurs permissions locales
- ce mécanisme devra être appliqué de la même manière aux autres types de scopes

## 6) Improvements later

- certains `TagGroup` doivent pouvoir imposer qu'un seul de leurs tags soit
  présent à la fois sur un objet donné (exemple: on ne peut pas avoir
  `security:public` et `security:internal` en même temps)
- d'autres groupes doivent rester multi-valeurs (exemple: `country:fr` et
  `country:de` autorisés en même temps)
