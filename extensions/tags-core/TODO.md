# TODO `tags-core`

Ce fichier sert de mémoire de conception et de pilotage pour `tags-core`.
Il liste :

- ce qu'il reste à implémenter
- les problèmes potentiels de logique déjà visibles
- les écarts entre le `README` et l'état réel du code

Le but est de pouvoir reprendre le sujet sans dépendre du contexte oral.

## 1) Bascule des objets métier de `Hashtag` vers `TagId` (en cours, largement avancée)

La bascule `models-core` -> `TagId` est déjà engagée et le flux principal backend est en place.
Le domaine modèle (`Model`, `Entity`, `Relationship`, `Attribute`) stocke désormais des `tags: List<TagId>`.

L'ancien système `Hashtag` a déjà été retiré du domaine `models-core` et des commandes/réducteurs concernés.
Il reste du nettoyage/alignement autour de modules adjacents et de l'expérience UI/recherche.

Le travail à faire n'est pas une migration de données : le logiciel est en cours de construction.
On peut faire évoluer le modèle et le code sans gérer de migration.

Découpage du travail (état actuel) :
- 1.1 fait : objets métier `models-core` en `TagId`
- 1.2 fait : persistance + commandes backend pour attacher/retirer des `TagId` sur `Model`, `Entity`, `Relationship`, `Attribute`
- 1.3 à finaliser : UI sur le nouveau système, avec usage métier complet des tags (pas seulement IDs bruts)
- 1.4 en cours : suppression totale de l'ancien système `Hashtag` (reste du code mort / des adaptateurs éventuels à nettoyer)

Règle technique :
- utiliser `TagId` comme référence dans les objets métier (pas `TagRef` par key), pour ne pas casser les liens si une key de tag change

### 1.3 UI tags (à faire)

L'UI ne doit pas manipuler seulement des IDs bruts.

À faire :
- charger et afficher des listes de tags réels (nom, key, scope, groupe si pertinent)
- adapter l'affichage des tags dans les écrans modèle/entité/relation/attribut
- adapter la sélection de tags (ajout/suppression) pour une sélection métier correcte à l'écran
- envoyer les IDs via l'API pour les opérations d'attache/détache (transport = `TagId`, UX = tags réels)
- adapter l'UI de recherche pour sélectionner des tags proprement et construire les filtres correspondants

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

## 3) Recherche par tags dans `model-core` (migration vers `TagRef`) à consolider

La recherche `SearchFilterTags` a été migrée vers des `TagRef` (au lieu de strings de hashtags), mais il reste du travail
de validation métier et de test.

À faire :
- confirmer le contrat API/UI pour les filtres de tags (format `TagRef` attendu)
- vérifier les comportements en cas de `TagRef` introuvable (erreur vs ignoré) et les documenter
- ajouter des tests dédiés sur `SearchFilterTags` (anyOf / noneOf / allOf / empty / notEmpty)

## 4) Import Frictionless : création de tags à partir des `keywords` (métier à clarifier)

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
  - le `TagScopeManager` utilisé pour `model` est actuellement permissif
  - il faut décider si cette permissivité reste locale à l'import ou si on met en place un mécanisme plus propre/centralisé

## 5) Couverture de tests à compléter

### 5.1 Validation des scopes d'attache
- tests d'acceptation/refus : tag local même modèle / autre modèle / tag global
- tests sur tous les points d'attache (`Model`, `Entity`, `Relationship`, `Attribute`)

### 5.2 Recherche par tags
- tests de parsing JSON des filtres de tags (`TagRef`)
- tests de recherche backend (`SearchFilterTags`) avec résolution de `TagRef`
- tests de comportement sur tags inconnus / refs invalides

### 5.3 Import Frictionless
- tests de création effective des tags via `TagCmds`
- tests de déduplication des keywords
- tests de propagation des `TagId` dans `Model.tags` / `Entity.tags`
- tests des keywords incompatibles avec `TagKey` (règle à définir)
