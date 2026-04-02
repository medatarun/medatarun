# Plan de migration DDL par module

## Objectif

Aligner les 4 `DbMigration` pour qu'en **installation initiale** (`install()`):

- chaque module applique **un seul script SQL**,
- ce script contienne la **DDL finale** de ce module (tables + index),
- pour `auth`, le script inclue aussi la donnée technique de bootstrap `system maintenance actor`,
- le script soit généré automatiquement depuis une base SQLite de référence déjà migrée.

Base de référence à utiliser:

- `/Users/sjust/projects/medatarun-samples/medatarun-0.8.0-tmp/data/database.db`

Scripts cibles:

- `init__auth_sqlite.sql`
- `init__models_sqlite.sql`
- `init__tags_sqlite.sql`
- `init__actions_sqlite.sql`

## Constat actuel (vérifié dans le code)

- `AuthDbMigration.install()` exécute actuellement 5 scripts SQL + 1 migration Kotlin.
- `ModelStorageDbMigration.install()` exécute actuellement 3 scripts SQL.
- `TagsCoreDbMigration.install()` exécute actuellement 4 scripts SQL.
- `ActionAuditRecorderDbMigration.install()` exécute déjà 1 script SQL.

Donc 3 modules sur 4 doivent passer d'un mode multi-scripts à un mode script unique.

## Périmètre DDL par module

Le générateur doit extraire uniquement les objets SQL du module, à partir de `sqlite_master`.

### Auth (`platform-auth`)

Tables:

- `users`
- `actors`
- `auth_ctx`
- `auth_code`
- `auth_client`

Indexes attendus:

- `idx_actors_issuer_subject`
- `idx_actors_created_at`
- `idx_auth_ctx_expires_at`
- `idx_auth_code_expires_at`

Donnée technique attendue dans `init__auth_sqlite.sql`:

- insertion de l'actor système de maintenance (ID fixe défini par `AppActorSystemMaintenance`).

### Models (`models-core`)

Tables:

- `model`
- `model_event`
- `model_snapshot`
- `model_tag_snapshot`
- `model_type_snapshot`
- `model_entity_snapshot`
- `model_entity_tag_snapshot`
- `model_entity_attribute_snapshot`
- `model_entity_attribute_tag_snapshot`
- `model_relationship_snapshot`
- `model_relationship_tag_snapshot`
- `model_relationship_role_snapshot`
- `model_relationship_attribute_snapshot`
- `model_relationship_attribute_tag_snapshot`
- `model_search_item_snapshot`
- `model_search_item_tag_snapshot`

Indexes attendus:

- `idx_model_event_model_id`
- `idx_model_type_model_id`
- `ux_model_event_release_model_version`
- `ux_model_snapshot_current_head_key`
- `ux_model_snapshot_current_head_model_id`
- `ux_model_snapshot_version_snapshot_release_event_id`

### Tags (`tags-core`)

Tables:

- `tag_event`
- `tag_view_current_tag_group`
- `tag_view_current_tag`
- `tag_view_history_tag`
- `tag_view_history_tag_group`

Indexes attendus:

- `idx_tag_view_current_tag__group_key`
- `idx_tag_view_current_tag__scope_key`
- `idx_tag_view_history_tag__lookup`
- `idx_tag_view_history_tag__scope`
- `idx_tag_view_history_tag_group__lookup`

### Actions (`platform-actions-storage-db`)

Table:

- `action_audit_event`

Indexes attendus:

- `idx_action_audit_event_group_key`
- `idx_action_audit_event_action_key`
- `idx_action_audit_event_actor_id`
- `idx_action_audit_event_created_at`
- `idx_action_audit_event_status`

## Générateur Python à créer

Emplacement:

- `tools/database-baseline/src/database_baseline/generate_module_init_sql.py`

Entrée:

- argument CLI obligatoire: chemin SQLite source (`--db-path`).

Sortie:

- écrit directement les 4 scripts dans les ressources des modules:
  - `libs/platform-auth/src/main/resources/io/medatarun/auth/infra/db/init__auth_sqlite.sql`
  - `extensions/models-core/src/main/resources/io/medatarun/model/infra/db/init__models_sqlite.sql`
  - `extensions/tags-core/src/main/resources/io/medatarun/tags/core/infra/db/init__tags_sqlite.sql`
  - `extensions/platform-actions-storage-db/src/main/resources/io/medatarun/actions/infra/db/init__actions_sqlite.sql`

Logique du script:

1. Ouvrir la base SQLite en lecture.
2. Lire `sqlite_master` pour les `table` et `index` (hors `sqlite_%` et hors `schema_version_history`).
3. Répartir les objets selon une table de mapping module -> noms de tables.
4. Inclure les index attachés aux tables du module.
5. Pour `auth`, compléter le script avec l'`INSERT` de l'actor system maintenance.
6. Écrire un SQL stable et déterministe:
   - tables d'abord, puis indexes,
   - puis données techniques de bootstrap pour `auth`,
   - ordre alphabétique des noms,
   - séparation claire par blocs.
7. Vérifier qu'aucune table applicative de la base source n'est hors mapping; sinon échouer explicitement.

## Changements Kotlin prévus

Mettre à jour les 4 classes suivantes:

- `libs/platform-auth/.../AuthDbMigration.kt`
- `extensions/models-core/.../ModelStorageDbMigration.kt`
- `extensions/tags-core/.../TagsCoreDbMigration.kt`
- `extensions/platform-actions-storage-db/.../ActionAuditRecorderDbMigration.kt`

Règle:

- `install()` doit appeler **exactement une fois** `ctx.applySqlResource(<init_script>)`.
- `AuthDbMigration.install()` ne doit plus appeler `V002_CreateActorSystemMaintenance`; cette création est portée par `init__auth_sqlite.sql`.

Migrations versionnées:

- garder `latestVersion()` et `applyVersion(...)` tels quels (compatibilité des migrations incrémentales),
- ne pas supprimer les scripts `v001/v002` existants dans cette étape.

## Exécution prévue

Depuis la racine du repo:

```bash
cd tools/database-baseline
uv venv
uv sync
uv run database-baseline --dialect sqlite --db-path /path/to/data/database.db
```

## Validation

1. Vérifier que les 4 scripts `init__*.sql` existent et sont non vides.
2. Vérifier que `init__auth_sqlite.sql` contient l'insert du `system maintenance actor`.
3. Vérifier que `install()` de chaque migration n'appelle plus qu'un script.
4. Lancer les tests unitaires:

```bash
./gradlew test
```

5. Contrôler les tests de démarrage migrations:
   - `AuthDbMigrationStartupTest`
   - `ModelStorageDbMigrationStartupTest`
   - `TagsCoreDbMigrationStartupTest`
   - `ActionAuditRecorderDbMigrationStartupTest`
