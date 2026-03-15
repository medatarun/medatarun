# DATABASE - Spécification SQL (V1)

Ce document définit la structure SQL cible (tables, colonnes, clés, index) pour
le versionning.

Il ne décrit pas l'UX ni les politiques produit hors base.

## Conventions

- La table d'un Modèle s'appelle `model`. Elle sert de base au graphe d'une
  entité logique Modèle (modele + events + snapshots, etc.)
- Les tables des constituants d'un modèle commencent toutes par `model_` car
  elles appartiennent au module `models-core`
- Une table qui represente un snapshot est suffixées `_snapshot`. On sait ainsi
  que cette table est reconstituable

Foreign Keys

- les références internes au module `models-core` sont déclarées en Foreign Key
  SQL sauf exceptions documentées (attribut identifiant d'une entité ou actors
  par exemple)
- `tag_id` reste sans FK SQL vers `tags-core` (pas de contrainte cross-zone).
- quand une colonne d'une table référence une autre colonne de table, la colonne
  s'appelle toujours `autre_table_id`

Identifiants

- Les tables ont généralement un `id` sauf les tables dites d'`id bag` censées
  représenter des arrays d'id n'ont pas d'identifiant de ligne (table
  d’association pure, sans attribut métier propre)
- Quand une table a un `id`, cet `id` est l'identifiant technique de la ligne
  SQL. Ce n'est pas l'identité métier versionnée de l'objet contenu dans la
  ligne.
- Les identifiants techniques de lignes SQL sont des UUIDv7 et s'appellent
  toujours `id` quand ils existent.
- L'identité métier stable d'un objet versionnable (`type`, `entity`,
  `attribute`, `relationship`, `role`) est portée en stockage par
  `lineage_id`.
- `lineage_id` est stable à travers les snapshots pour un même objet. Il sert à
  rattacher entre elles les différentes incarnations stockées du même objet à
  travers le temps.
- `lineage_id` n'est pas une deuxième identité métier en plus de l'`id` du
  domaine. Dans les tables de snapshot, `lineage_id` stocke la même identité
  stable que celle exposée par `item.id` dans le modèle de domaine.
- Règle de mapping: quand on reconstruit un objet du domaine depuis une table
  versionnée, on lit `record.lineage_id` pour alimenter `item.id`.

## Principes

Principe d’architecture:

- La base garantit la fiabilité structurelle des tables maître (`model`,
  `model_event`): identité, rattachement au modèle, ordre canonique, unicité
  technique.
- Les règles métier (ex: validité fonctionnelle d’une release, cohérence métier
  SemVer, interdictions liées au workflow) sont validées par l’application, pas
  par la base.
- Les tables `_snapshot` sont des projections dénormalisées, reconstruites
  depuis `model_event`. Elles sont considérées comme scratchables: en cas
  d’incident ou de recalcul, l’application peut les supprimer/recréer.
- Les contraintes SQL sur les `_snapshot` restent volontairement minimales et
  orientées intégrité de reconstruction, sans déplacer le moteur métier dans la
  base.

Conséquence:

- Une donnée peut être techniquement stockable en SQL mais rejetée par la couche
  applicative si elle viole une règle métier.

## Tables maître (sources de vérité)

### `model`

Rôle: identité stable du modèle.

Colonnes:

- `id` TEXT PRIMARY KEY: identifiant technique stable du modèle. C'est aussi l'
  id du lineage intégral du modele.

### `model_event`

Rôle: source de vérité append-only.

Colonnes:

- `id` TEXT PRIMARY KEY: identifiant technique unique de l'event.
- `model_id` TEXT NOT NULL REFERENCES `model(id)` ON DELETE CASCADE: modèle
  auquel l'event appartient.
- `stream_revision` INTEGER NOT NULL: ordre canonique de l'event dans le flux du
  modèle.
- `event_type` TEXT NOT NULL: type d'event (`create`, `update`, `model_release`,
  etc.).
- `event_version` INTEGER NOT NULL: numéro de version de l'event, permet d'upcaster les vieux event lors du replay quand ils évoluent
- `model_version` TEXT NULL: version SemVer du modèle, portée uniquement pour
  les events `event_type='model_release'`.
- `model_version` doit toujours être une valeur acceptée par `ModelVersion`.
- L'ordre métier entre deux valeurs de `model_version` est celui défini par
  `ModelVersion`.
- `actor_id` TEXT NOT NULL: identifiant stable de l'acteur qui a initié l'
  action.
- `action_id` TEXT NOT NULL: identifiant système de l'action source, fourni par
  la plateforme d'actions.
- `created_at` TEXT NOT NULL: date/heure de création de l'event.
- `payload` TEXT NOT NULL: contenu détaillé de l'event (JSON texte).

Règle de mapping:

- `event_type` et le `payload` sont dérivés des commandes décrites dans
  `ModelRepoCmd.kt`.
- Le mapping doit être explicite et stable; il ne doit pas dépendre
  implicitement des noms de classes/propriétés Kotlin.
- `actor_id` ne porte pas de FK vers le module auth (pas de contrainte
  cross-zone).
- `action_id` vient des identifiants d'actions de la plateforme et sert la
  traçabilité opérationnelle (relier un `model_event` à l'action source).

Contraintes:

- `UNIQUE(model_id, stream_revision)` (ordre canonique)
- `UNIQUE(model_id, model_version)` avec filtre logique
  `event_type='model_release'`
  si le moteur SQL le permet; sinon garantir l'unicité côté application.

## Tables projection métier

Cette section décrit le contenu des tables projetées du snapshot.

### `model_snapshot`

Rôle: racine d'un état projeté du modèle.

Colonnes:

- `id` TEXT PRIMARY KEY: identifiant technique du snapshot.
- `model_id` TEXT NOT NULL REFERENCES `model(id)` ON DELETE CASCADE: modèle
  auquel le snapshot appartient.
- `key` TEXT NOT NULL: clé métier du modèle dans ce snapshot.
- `name` TEXT NULL: nom du modèle dans ce snapshot.
- `description` TEXT NULL: description du modèle dans ce snapshot.
- `origin` TEXT NOT NULL: source métier/technique du modèle dans ce snapshot.
- `authority` TEXT NOT NULL: autorité/référentiel de ce snapshot.
- `documentation_home` TEXT NULL: lien de documentation racine du modèle dans
  ce snapshot.
- `snapshot_kind` TEXT NOT NULL CHECK (`snapshot_kind` IN ('CURRENT_HEAD', '
  VERSION_SNAPSHOT')):
  nature du snapshot (`CURRENT_HEAD` mutable ou `VERSION_SNAPSHOT` figé).
- `up_to_revision` INTEGER NOT NULL: dernière `stream_revision` incluse dans ce
  snapshot.
- `model_event_release_id` TEXT NULL REFERENCES `model_event(id)` ON DELETE
  CASCADE: event `model_release` associé (obligatoire pour
  `VERSION_SNAPSHOT`, null
  pour `CURRENT_HEAD`). S'appelle bien `model_event_release_id` pas
  `model_event_id`
  car on pointe sur un type d'event spécifique.
- `version` TEXT NULL: version SemVer dénormalisée stockée par praticité dans
  le snapshot.
- Pour un `VERSION_SNAPSHOT`, elle reflète la version portée par le
  `model_event_release_id` associé.
- Pour un `CURRENT_HEAD`, elle peut contenir la dernière version publiée connue
  sans devenir la source de vérité de la version.
- `created_at` TEXT NOT NULL: date/heure de création du snapshot.
- `updated_at` TEXT NOT NULL: date/heure de dernière mise à jour (utile surtout
  pour `CURRENT_HEAD`).

Contraintes:

- Un seul `CURRENT_HEAD` par `model` (index unique partiel recommandé).
- Un seul `VERSION_SNAPSHOT` par `model_event_release_id`.
- `version` obligatoire pour `VERSION_SNAPSHOT`.
- Pour `CURRENT_HEAD`, `version` reste facultative et purement pratique; elle
  ne doit pas être traitée comme une contrainte métier de frontière historique.
- Unicité de la clé courante: `key` doit être unique globalement entre tous les
  snapshots `CURRENT_HEAD` (index unique partiel recommandé).

### `model_tag_snapshot`

Rôle: tags du modèle pour un snapshot.

Colonnes:

- `model_snapshot_id` TEXT NOT NULL REFERENCES `model_snapshot(id)` ON DELETE
  CASCADE
- `tag_id` TEXT NOT NULL

Unicité:

- `PRIMARY KEY (model_snapshot_id, tag_id)`

### `model_type_snapshot`

Rôle: types du modèle dans un snapshot.

Colonnes:

- `id` TEXT PRIMARY KEY
- `lineage_id` TEXT NOT NULL
- `model_snapshot_id` TEXT NOT NULL REFERENCES `model_snapshot(id)` ON DELETE
  CASCADE
- `key` TEXT NOT NULL
- `name` TEXT NULL
- `description` TEXT NULL

Unicité:

- `UNIQUE(model_snapshot_id, lineage_id)`
- `UNIQUE(model_snapshot_id, key)`

### `model_entity_snapshot`

Rôle: entités d'un modele dans un snapshot.

Colonnes:

- `id` TEXT PRIMARY KEY
- `lineage_id` TEXT NOT NULL
- `model_snapshot_id` TEXT NOT NULL REFERENCES `model_snapshot(id)` ON DELETE
  CASCADE
- `key` TEXT NOT NULL
- `name` TEXT NULL
- `description` TEXT NULL
- `identifier_attribute_snapshot_id` TEXT NOT NULL
- `origin` TEXT NULL
- `documentation_home` TEXT NULL

Unicité:

- `UNIQUE(model_snapshot_id, lineage_id)`
- `UNIQUE(model_snapshot_id, key)`

Attention:

- `identifier_attribute_snapshot_id` est l'id de
  `model_entity_attribute_snapshot(id)` mais mettre une Foreign Key ici créerait
  une référence circulaire qui peut être compliquée à gérer selon le moteur de
  base de données utilisé. On choisit donc de ne pas mettre de Foreign Key ici.

### `model_entity_tag_snapshot`

Rôle: tags des entités d'un modele dans un snapshot.

Colonnes:

- `model_entity_snapshot_id` TEXT NOT NULL REFERENCES
  `model_entity_snapshot(id)` ON DELETE CASCADE
- `tag_id` TEXT NOT NULL

Unicité:

- `PRIMARY KEY (model_entity_snapshot_id, tag_id)`

### `model_entity_attribute_snapshot`

Rôle: attributs d'entité d'un modèle dans un snapshot.

Colonnes:

- `id` TEXT PRIMARY KEY
- `lineage_id` TEXT NOT NULL
- `model_entity_snapshot_id` TEXT NOT NULL REFERENCES
  `model_entity_snapshot(id)` ON DELETE CASCADE
- `key` TEXT NOT NULL
- `name` TEXT NULL
- `description` TEXT NULL
- `model_type_snapshot_id` TEXT NOT NULL REFERENCES `model_type_snapshot(id)`
- `optional` INTEGER NOT NULL

Unicité:

- `UNIQUE(model_entity_snapshot_id, lineage_id)`
- `UNIQUE(model_entity_snapshot_id, key)`

### `model_entity_attribute_tag_snapshot`

Rôle: tags des attributs d'entité d'un modele dans un snapshot.

Colonnes:

- `model_entity_attribute_snapshot_id` TEXT NOT NULL REFERENCES
  `model_entity_attribute_snapshot(id)` ON DELETE CASCADE
- `tag_id` TEXT NOT NULL

Unicité:

- `PRIMARY KEY (model_entity_attribute_snapshot_id, tag_id)`

### `model_relationship_snapshot`

Rôle: relations d'un modele dans un snapshot.

Colonnes:

- `id` TEXT PRIMARY KEY
- `lineage_id` TEXT NOT NULL
- `model_snapshot_id` TEXT NOT NULL REFERENCES `model_snapshot(id)` ON DELETE
  CASCADE
- `key` TEXT NOT NULL
- `name` TEXT NULL
- `description` TEXT NULL

Unicité:

- `UNIQUE(model_snapshot_id, lineage_id)`
- `UNIQUE(model_snapshot_id, key)`

### `model_relationship_tag_snapshot`

Rôle: tags des relations d'un modele dans un snapshot.

Colonnes:

- `model_relationship_snapshot_id` TEXT NOT NULL REFERENCES
  `model_relationship_snapshot(id)` ON DELETE CASCADE
- `tag_id` TEXT NOT NULL

Unicité:

- `PRIMARY KEY (model_relationship_snapshot_id, tag_id)`

### `model_relationship_role_snapshot`

Rôle: rôles d'une relation d'un modele dans un snapshot.

Colonnes:

- `id` TEXT PRIMARY KEY
- `lineage_id` TEXT NOT NULL
- `model_relationship_snapshot_id` TEXT NOT NULL REFERENCES
  `model_relationship_snapshot(id)` ON DELETE CASCADE
- `key` TEXT NOT NULL
- `model_entity_snapshot_id` TEXT NOT NULL REFERENCES
  `model_entity_snapshot(id)`
- `name` TEXT NULL
- `cardinality` TEXT NOT NULL

Unicité:

- `UNIQUE(model_relationship_snapshot_id, lineage_id)`
- `UNIQUE(model_relationship_snapshot_id, key)`

### `model_relationship_attribute_snapshot`

Rôle: attributs d'une relation d'un modele dans un snapshot.

Colonnes:

- `id` TEXT PRIMARY KEY
- `lineage_id` TEXT NOT NULL
- `model_relationship_snapshot_id` TEXT NOT NULL REFERENCES
  `model_relationship_snapshot(id)` ON DELETE CASCADE
- `key` TEXT NOT NULL
- `name` TEXT NULL
- `description` TEXT NULL
- `model_type_snapshot_id` TEXT NOT NULL REFERENCES `model_type_snapshot(id)`
- `optional` INTEGER NOT NULL

Unicité:

- `UNIQUE(model_relationship_snapshot_id, lineage_id)`
- `UNIQUE(model_relationship_snapshot_id, key)`

### `model_relationship_attribute_tag_snapshot`

Rôle: tags des attributs de relation d'un modele dans un snapshot.

Colonnes:

- `model_relationship_attribute_snapshot_id` TEXT NOT NULL REFERENCES
  `model_relationship_attribute_snapshot(id)` ON DELETE CASCADE
- `tag_id` TEXT NOT NULL

Unicité:

- `PRIMARY KEY (model_relationship_attribute_snapshot_id, tag_id)`

### Recherche

La recherche indexe uniquement l'état `CURRENT_HEAD`.
Elle n'indexe pas les snapshots de version.

Tables cibles:

- `model_search_item_snapshot`
- `model_search_item_tag_snapshot`

Transformations depuis `v000_init_db_sqlite.sql`:

- renommer `denorm_model_search_item` en `model_search_item_snapshot`;
- renommer `denorm_model_search_item_tag` en `model_search_item_tag_snapshot`;
- renommer la colonne `model_id` en `model_snapshot_id` dans
  `model_search_item_snapshot`;
- `model_search_item_snapshot.model_snapshot_id` référence `model_snapshot(id)`;
- conserver la relation tag:
  `model_search_item_tag_snapshot.search_item_id` référence
  `model_search_item_snapshot.id`;
- conserver les autres colonnes de recherche (item_type, clés/labels,
  search_text)
  avec le même sens fonctionnel.
