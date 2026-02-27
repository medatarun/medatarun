# tags-core

## Note pour IA (style de rédaction attendu)

Quand on rédige ce README:

- écrire des faits concrets et vérifiables
- dire explicitement qui fait quoi, où, quand, avec quelles règles
- nommer directement les problèmes et les effets opérationnels ou logiques
- éviter le style blog, le ton marketing, les généralités et les formulations
  vagues
- éviter l'enrobage; préférer des phrases courtes, directes, actionnables
- poser des questions sur les éléments qui paraissent flous avant de rédiger

## But du module

`tags-core` est le module transversal qui centralise la gestion des tags dans
Medatarun.

Il est utilisé aujourd'hui par le module [models-core](../models-core/README.md).
Roadmap (lot ultérieur, non implémenté, décision actée) : intégration à `prompts-core` et aux autres modules métiers.

D'où le besoin d'avoir un systeme générique et sans focus sur Models.


Il expose:

- un modèle unique (`Tag`, `TagId`, `TagRef`, `TagScopeRef`)
- des commandes de gestion de tags/groupes (via `TagAction`, `TagCmds` et
  `TagQueries`)
- outils communs que les autres extensions peuvent utiliser pour travailler avec
  les tags

Les modules consommateurs (ex: `models-core`) gèrent l'attache/détache sur leurs
objets et stockent des `TagId`.

## Problème initial

On constate deux modes de gestion des tags en général dans les applications

- Modèle global/centralisé:
    - Par exemple : OpenMetadata, DataHub, Atlan, Alation, Collibra
    - Effet: création/modification plus contrôlée, cycle de changement plus
      long, une équipe locale ne peut pas créer/ajuster vite un tag pour un
      besoin immédiat
- Modèle local/flexible:
    - Par exemple : Jira labels
    - Effet: création rapide, vocabulaire hétérogène (doublons, synonymes,
      homonymes), deux équipes taggent le même sujet différemment, donc la
      recherche transversale rate des résultats ou renvoie du bruit

- Dans plusieurs outils, ce sens n'est pas porté directement par le tag utilisé:
    - Jira labels: pas de description métier attachée au label
    - dbt tags: liste de chaînes sans champ natif de description sur le tag
      lui-même
    - OpenLineage tag facets: paires clef/valeur/source, sans champ de
      description de la signification du tag
    - CKAN free tags: tags libres orientés mot-clef, sans description métier
      directement attachée au tag
    - outils de catalogage avec glossaire séparé (DataHub, Atlan, Alation,
      Collibra, OpenMetadata): le sens est souvent porté par un objet de
      glossaire/terme distinct, pas par le tag appliqué lui-même

Sans description de tag, l'utilisateur ne sait pas:

- quel tag choisir
- sur quels objets il doit l'appliquer
- quand il doit l'appliquer
- quand il ne doit pas l'appliquer

Résultat:

- tags appliqués au hasard selon les personnes
- mêmes objets taggés différemment
- recherche par tags non fiable
- règles de gouvernance non applicables de manière consistante

## Résumé métier

Le système distingue deux usages métier de tags:

1. Tags `managed`

- Portée globale (`TagScopeRef.Global`)
- Gouvernance forte (rôle dédié `tag_managed_manage`)
- Organisés par groupes (`TagGroup`)
- Créés via `TagManagedCreate`
- utilisés pour les usages transversaux (gouvernance globale)

2. Tags `free`

- Portée locale à un objet ou un groupe d'objets (`TagScopeRef.Local(type, scopeId)`). Aujourd'hui une entité `Model` au sens `models-core` est un scope de type `model` avec pour id l'id du `Model`. Tous les objets taggables que possèdent un `Model` (donc `Model` lui-même, `Entity`, `Relationship`, `Attribute`, etc.) peuvent être taggé avec les tags du scope (`model`+ `Model.id`) en plus de tous les tags globaux. 
- Gouvernance par le rôle `tag_free_manage` (que l'on peut attribuer à plus d'acteurs que `tag_managed_manage`)
- Sans groupe
- Créés via `TagFreeCreate`
- utilisés pour les usages locaux (contexte d’un scope)

Les deux usages partagent le même objet `Tag` et le même espace d'identifiants (
`TagId`). La différence est portée par les règles métier et les commandes, pas
par deux modèles techniques séparés.

Les tags ont deux champs de documentation: `name` et `description`.
Ces champs sont facultatifs dans tout le système:
- dans le modèle
- dans les actions
- dans l’UI

`tags-core` autorise donc la création et l’usage de tags sans name/description.

Effets:
- création rapide possible avec une simple key
- moins de contexte disponible pour choisir et interpréter correctement le tag

Position produit:
- pas d’obligation, pas de blocage
- enrichissement recommandé quand le tag est partagé ou réutilisé
- la décision d’enrichir appartient aux utilisateurs/modules qui exploitent les tags

On peut référencer un Tag via API grâce à `TagRef`

- soit une clef de type `id:<TagId>`
- soit une clef de type `key:<scopeType>/<groupe|id>/<TagKey>`

En quoi cela répond au problème

`tags-core` sépare explicitement les deux besoins dans un même langage
technique:

- `managed` pour le vocabulaire transversal stable (gouvernance globale)
- `free` pour le local rapide

Avec ce systeme chaque tag a un `scope` explicite et un `TagId` stable.
Si la clef (`TagKey`) change, les liens stockés par ID restent valides.

Un tag n'est pas seulement une clef (`key`). Il porte `name` et `description`, donc on
peut documenter le sens et les usages.

Conséquences supplémentaires

- sur la possibilité d'avoir `name` et `description` et le fait qu'ils soient accessibles via les actions:
  - CLI et API: on peut s'en servir de base pour créer de la documentation ou alimenter des systèmes tiers. 
  - MCP/agents IA: ils peuvent tagger ou comprendre le sens des tags créés et ceux ajoutés aux objets métiers sans qu'elles aient à inférer.
    Conséquence directe : on peut demander à une IA de tagger automatiquement des modèles, ou l'inverse, prompter une IA sur les modèles taggés avec les tags qui lui fournissent des compléments d'explication.
  - Sur l'UI on peut aider les utilisateurs à tagger ou à
    parcourir les tags existants pour qu'ils en comprennent le sens.
- Le fait de pouvoir référencer les tags par `TagRef` avec des keys stables simplifie l'écriture des scripts d'automatisation en évitant les boucles de type "liste + récupération de l'id pour pouvoir agir".

## Concepts liés à l'identité et au référencement des tags

- `TagId`: identifiant stable d'un tag. C'est la référence de stockage dans les
  objets métiers.
- `TagRef`: référence d'entrée/sortie API (action, JSON, recherche), sous forme:
    - `id:<uuid>`
    - `key:<scopeType>/<middle>/<tagKey>`
- `TagScopeRef`:
    - `Global`
    - `Local(type, scopeId)`
- `TagGroup`: groupe de vocabulaire contrôlé pour les tags managed.

Règle de format pour `TagRef.ByKey`:

- global/managed: `key:global/<groupKey>/<tagKey>`
- local/free: `key:<scopeType>/<scopeId>/<tagKey>`

## Fonctionnement des commandes `tags-core`

Entrées principales exposées côté actions (`TagAction`):

- free: create/update/delete
- managed group: create/update/delete
- managed tag: create/update/delete
- queries: `TagList`, `TagGroupList`

Règles métier actuellement codées (`TagCmdsImpl`):

- `TagFreeCreate` refuse un scope global.
- Un tag free est unique par `(scope local, key)`.
- Un tag managed est unique par `(group, key)`.
- Les commandes free refusent les refs vers managed (
  `TagFreeCommandIncompatibleTagRefException`).
- Les commandes managed refusent les refs vers local (
  `TagManagedCommandIncompatibleTagRefException`).
- Toute mutation tag passe par `DbTransactionManager.runInTransaction`.

## Scopes et cycle de vie

`tags-core` possède un registre de scopes (`TagScopeManager`) alimenté par les
autres modules.

Exemple déjà branché:

- `models-core` déclare un `TagScopeManager` de type `model`.
- `localScopeExists` vérifie l'existence réelle du model via `ModelQueries`.

Suppression:

1. Suppression explicite de tag (free/managed)

- `TagBeforeDeleteEvt(tagId)` est émis avant suppression
- Permet aux modules consommateurs de nettoyer les références ou bloquer la
  suppression

2. Suppression de groupe managed

- Supprime tous les tags du groupe
- Émet `TagBeforeDeleteEvt` pour chaque tag supprimé

3. Suppression de scope local

- Le module propriétaire du scope émet `TagScopeBeforeDeleteEvent(scopeRef)`
- `tags-core` supprime en masse les tags du scope (`TagScopeDelete`)
- Ce chemin ne garantit pas d'événement `TagBeforeDeleteEvt` pour chaque tag

## Sécurité

Rôles enregistrés par `tags-core`:

- `tag_free_manage`
- `tag_managed_manage`
- `tag_group_manage`

Règles actions:

- free tag actions -> `TAG_FREE_MANAGE`
- managed tag actions -> `TAG_MANAGED_MANAGE`
- group actions -> `TAG_GROUP_MANAGE`
- listes (`tag_list`, `tag_group_list`) -> utilisateur connecté (`SIGNED_IN`)

## Fonctionnement UI (état actuel)

Le backend expose déjà des emplacements UI (`TagActionUILocation`):

- `tag_free_list`, `tag_free_detail`
- `tag_managed_group_list`, `tag_managed_group_detail`
- `tag_managed_list`, `tag_managed_detail`

État:

- Backend CRUD tags/groupes: implémenté
- UI métier complète tags: non implémentée

État d'avancement connu (TODO projet):

- l'UI doit afficher des tags métiers (nom, key, scope, groupe), pas uniquement
  des IDs
- la sélection de tags doit rester orientée utilisateur mais transporter des
  `TagId` côté attache/détache
- la recherche UI doit produire des filtres tags basés sur `TagRef`

## Usage dans les autres modules (focus `models-core`)

`models-core` consomme `tags-core` sur deux axes:

1. Commandes d'attache/détache

- Les actions `ModelAction` utilisent `TagRef` pour:
    - model
    - entity
    - entity attribute
    - relationship
    - relationship attribute
- `ModelCmdsImpl` convertit `TagRef -> TagId` via `ModelTagResolverWithQueries`.

2. Règles de compatibilité de scope

- `resolveTagIdCompatible(modelId, tagRef)` accepte:
    - tags globaux (managed)
    - tags locaux du scope `model/<modelId>`
- rejette un tag local d'un autre scope avec `TagAttachScopeMismatchException`.

Stockage métier:

- les objets `Model`, `Entity`, `Relationship`, `Attribute` stockent
  `List<TagId>`.
- le transport API/actions reste en `TagRef`.

Recherche:

- `SearchFilterTags` utilise `TagRef` (`anyOf`, `noneOf`, `allOf`, `empty`,
  `notEmpty`).
- la résolution en `TagId` est faite dans `ModelQueriesImpl`.

## Décisions prises dans `tags-core`

1. Modèle tag unifié

- un seul type `Tag` pour free + managed
- différence pilotée par `scope` et règles de commande

2. Référence unifiée

- `TagRef` accepte ID ou clef portée (`scope` explicite)

3. Politique d'unicité au niveau métier

- contrôlée dans le code (`TagCmdsImpl`)
- pas de contrainte SQL unique transverse entre free et managed

4. Validation de scope locale déléguée

- chaque module propriétaire fournit un `TagScopeManager`
- `tags-core` ne connaît pas la logique d'existence des objets externes

5. Séparation suppression tag vs suppression scope

- événements et responsabilités séparés pour éviter les ambiguïtés

## Contrat pratique pour les modules consommateurs

Contrat requis pour un module consommateur:

- déclarer un `TagScopeManager` s'il introduit un scope local
- utiliser `TagRef` dans ses actions/DTO/API d'entrée
- résoudre vers `TagId` avant persistance de ses objets
- stocker `TagId` (pas des clés de tags) pour ne pas casser les liens lors de
  renommage de key
- écouter `TagBeforeDeleteEvt` si le module stocke des références de tags

## Questions ouvertes (à traiter)

- [Q-01] Politique de conversion `keyword -> TagKey` pendant les imports (
  normalisation stricte, rejet, mapping, reporting): règle non finalisée.
- [Q-02] Contrat exact API/UI quand un `TagRef` de filtre recherche est
  introuvable: erreur explicite ou filtre ignoré.
- [Q-03] UX cible de sélection de tags dans l'UI `models-core`: chargement,
  affichage, recherche, création éventuelle à la volée.
- [Q-04] Stratégie d'import quand le scope local n'existe pas encore au moment
  de pré-création des tags (cas Frictionless): alignement final à décider.
- [Q-05] Lors d'un `TagScopeBeforeDeleteEvent`, faut-il (ou non) émettre aussi
  des événements unitaires par tag supprimé pour les modules consommateurs.
- [Q-06] Politique de permissions fine sur les tags free locaux: seulement rôle
  global `tag_free_manage` ou contrôle complémentaire par scope propriétaire.
- [Q-07] Contrat de sortie de `TagList`: faut-il inclure explicitement `scope`
  et `isManaged` dans le JSON pour simplifier l'UI.

## Références source


- `extensions/tags-core/src/main/kotlin/io/medatarun/tags/core/actions/TagAction.kt`
- `extensions/tags-core/src/main/kotlin/io/medatarun/tags/core/internal/TagCmdsImpl.kt`
- `extensions/tags-core/src/main/kotlin/io/medatarun/tags/core/domain/TagRef.kt`
- `extensions/tags-core/src/main/kotlin/io/medatarun/tags/core/domain/TagScopeRef.kt`
- `extensions/tags-core/src/main/kotlin/io/medatarun/tags/core/domain/TagBeforeDeleteEvt.kt`
- `extensions/tags-core/src/main/kotlin/io/medatarun/tags/core/domain/TagScopeBeforeDeleteEvent.kt`
- `extensions/models-core/src/main/kotlin/io/medatarun/model/actions/ModelAction.kt`
- `extensions/models-core/src/main/kotlin/io/medatarun/model/internal/ModelCmdsImpl.kt`
- `extensions/models-core/src/main/kotlin/io/medatarun/model/ModelTagResolverWithQueries.kt`
- `extensions/models-core/src/main/kotlin/io/medatarun/model/ModelExtension.kt`
- `extensions/tags-core/README_old.md`
- `extensions/tags-core/TODO.md`
