# models-core

## Note pour IA (style de rédaction attendu)

Quand on rédige ce README:

- écrire des faits concrets et vérifiables
- dire explicitement qui fait quoi, où, quand, avec quelles règles
- nommer directement les problèmes et les effets opérationnels ou logiques
- éviter le style blog, le ton marketing, les généralités et les formulations
  vagues
- éviter l'enrobage; préférer des phrases courtes, directes, actionnables
- poser des questions sur les éléments qui paraissent flous avant de rédiger

## Convention README / TODO

Ce README décrit le contrat fonctionnel cible du module (comportement attendu
une fois le lot finalisé).

Quand un fichier `TODO.md` existe pour ce module, il liste uniquement le
travail restant et les écarts entre ce contrat cible et l'état actuel du code.

## But du module

`models-core` porte le modèle métier principal de Medatarun.

Il gère:

- la structure d'un modèle de domaine (`Model`)
- les types, entités, relations et attributs
- la documentation métier associée (name, description, documentationHome)
- la recherche sur les objets du modèle
- les tags attachés aux objets du modèle

Ce module expose les actions `ModelAction` et les traduit en commandes métier via
`ModelActionProvider`.

## Principe de `Model`

Le contrat `Model` (voir `Model.kt`) décrit un modèle de domaine complet avec:

- identité et métadonnées: `id`, `key`, `name`, `description`, `version`,
  `origin`
- contenu structurel: `types`, `entities`, `relationships`
- métadonnées complémentaires: `documentationHome`
- tags du modèle: `tags: List<TagId>`

Les entités, relations et attributs sont adressables par clés et identifiants
grâce aux méthodes `find*`.

## Actions exposées (`ModelAction`)

`ModelActionProvider` route les actions déclarées dans `ModelAction` vers `ModelCmds` / `ModelQueries`.

`ModelAction` regroupe, entre autres:

- import/inspection/liste/export/recherche (`Import`, `Inspect_*`, `Model_List`,
  `Model_Export`, `Search`)
- opérations sur modèle (`Model_Create`, `Model_Copy`, `Model_Update*`,
  `Model_Delete`, tags add/delete)
- opérations sur types (`Type_*`)
- opérations sur entités (`Entity_*`, tags add/delete)
- opérations sur attributs d'entité (`EntityAttribute_*`, tags add/delete)
- opérations sur relations (`Relationship_*`, tags add/delete)
- opérations sur rôles de relation (`RelationshipRole_*`)
- opérations sur attributs de relation (`RelationshipAttribute_*`, tags
  add/delete)

Les paramètres de tags dans `ModelAction` sont toujours des `TagRef`.

`Search` renvoie une liste d'items avec `id` et `location` typée
(`model`, `entity`, `entityAttribute`, `relationship`,
`relationshipAttribute`).

## Interaction avec `tags-core`

`models-core` consomme `tags-core` de la façon suivante:

- les actions `ModelAction` de tagging utilisent des `TagRef`
  (`model`, `entity`, `entity attribute`, `relationship`,
  `relationship attribute`)
- `ModelCmdsImpl` convertit `TagRef -> TagId` via
  `ModelTagResolverWithQueries`
- les objets `Model`, `Entity`, `Relationship`, `Attribute` stockent des
  `TagId`
- la recherche de `models-core` accepte des filtres tags en `TagRef`, si un tag spécifié n'existe pas, une erreur de type NotFound est levée. 
- règle `models-core` de compatibilité des tags:
  - `resolveTagIdCompatible(modelId, tagRef)` accepte:
    - tags globaux (managed)
    - tags locaux du scope `model/<modelId>`
  - rejette un tag local d'un autre scope avec
    `TagAttachScopeMismatchException`

Le détail des règles de tags (types de tags, format des refs, cycle de vie,
permissions, événements) est documenté dans
`extensions/tags-core/README.md`.



## Références source

- `README.md` (racine projet)
- `extensions/models-core/src/main/kotlin/io/medatarun/model/domain/Model.kt`
- `extensions/models-core/src/main/kotlin/io/medatarun/model/actions/ModelAction.kt`
- `extensions/models-core/src/main/kotlin/io/medatarun/model/actions/ModelActionProvider.kt`
