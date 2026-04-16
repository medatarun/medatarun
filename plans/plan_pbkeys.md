# New entity primary keys and business keys

Project follow-up document.

## Goal

Today, in an Entity we have an `idenfierAttribute` referencing one of the model
attributes, saying it's the entity primary key.
We don't want that anymore because it restricts modelization too much and goes
against the application's intent.

We have to implement two new concepts in `ModelAggregate`

- `val entityPrimaryKeys: List<EntityPrimaryKey>`: list of entity primary keys
  with participant attributes in order
- `val businessKeys: List<BusinessKey>`: list of business keys with participant
  attributes in order. A business key has a name, description and itself a `key`
  for external references.

Then we need to remove `identifierAttribute` from Entity and everywhere.

Two phases:

- Compatibility phase: first we implement entityPrimaryKeys and businessKeys
  everywere, removing usages of identifierAttribute as much as possible
- Cleanup phase: then we remove if from everywhere else

## ModelAggregate

Compatibility phase:

- [x] implement entityPrimaryKeys and businessKeys in ModelAggregate and
  ModelAggregateInMemory

Cleanup phase:

- [X] remove `identifierAttribute` from Entity

## Model storage

### Compatibility phase:

- [X] create type `EntityPKSnapshotId`
- [X] create type `BusinessKeySnapshotId`
- [X] create snapshot tables in init scripts to store primary keys and business
  keys in `init__models_postgresql.sql` and `init__models_sqlite.sql` (
  description below)
- [x] make a migration version `version_models_v003_01_pk_bk_sqlite.sql` that
  creates tables in SQLite when dialect is SQLite
- [x] make a migration version `version_models_v003_01_pk_bk_postgresql.sql`
  that creates tables in Postgresql when dialect is Postgresql
- [x] make a version `V003_IdentifierAttributeToPrimaryKeys` in
  `ModelStorageDbMigration` that takes `identifierAttribute` column in snapshots
  and convert it to snapshot primary keys. Should run whatever the dialect
  Postgresql and SQLite

Those are commands in `ModelStorageCmd` that need to be interpreted differently
in `ModelStorageDbProjection`

- [X] `model_aggregate_stored`: especially the entity creation section, we need
  to create the corresponding primary key in `model_entity_pk_snapshot`
- [X] `model_release` command shall handle the cloning for
  `model_entity_pk_snapshot` and `model_entity_pk_attribute_snapshot`
- [X] `model_deleted` command already deletes via foreign keys the entities;
  therefore, we must be sure that `model_entity_pk_snapshot` and
  `model_entity_pk_attribute_snapshot` and
  `model_business_key_attribute_snapshot`
  and `model_business_key_snapshot` are deleted too when entities are deleted
- [X] `entity_created`: we need to create the corresponding primary key in
  `model_entity_pk_snapshot`
- [X] `entity_deleted`: we need to make sure that matching
  `model_entity_pk_attribute_snapshot` is deleted (should be done with foreign
  keys)
- [X] `entity_attribute_deleted`: we need to make sure that matching
  `model_entity_pk_attribute_snapshot` is deleted (should be done with foreign
  keys)
- [X] `entity_identifier_attribute_updated` command shall be interpreted as
  removing matching rows in model_entity_pk_attribute_snapshot based on the
  entity and recreating one row with the entity and attribute's snapshot_id
  based on the command.

➡️ at this point we know how to write

Next operations

- [X] adjust reads in `ModelStorageDbRead` to read identifierAttribute from
  primary keys. If there is no primary key that matches throw exception (it's a
  transitory compatibility state anyway)
- [X] adjust reads in `ModelStorageDbRead` to read primary keys and business
  keys and put them in `ModelAggregate`

➡️ at this point we know how to read and don't use identifierAttribute to read

Next operations

- [X] Create new command
  `entity_primary_key_set { entityId: EntityId, attributeIds: List<AttributeId> }`
  and interpret in projection.
    - If list is empty then pk is deleted if exists.
    - If matching pk doesn't exist, create it with attributes.
    - If matching pk exists
        - If attributes are the same in same order, do nothing
        - Or else recreate the rows in the pk's attributes table
- [X] switch `ModelCmd` to use command `entity_primary_key_set` instead of
  `entity_identifier_attribute_updated`. `ModelCmd` shall ensure not sending the
  command to storage if pk is already good (same attributes in same order)

➡️ at this point identifierAttribute column should be useless

- [X] Replace `model_entity_snapshot.identifier_attribute_snapshot_id` with a
  new
  `version_models_v003_02_remove_identifier_attribute.sql` to remove the old
  column. Remove it from `EntityTable`.

- [X] Create version 2 of command `entity_created` where we don't specify
  identifierAttribute, use it in projection. Don't use `entity_created` version
  1 anymore (upscale to newer versions)

- [X] make command `entity_identifier_attribute_updated` deprecated and don't
  use it anymore (upscale to newer versions)

- [X] make command `model_aggregate_stored` V1 deprecated and create a v2 that
  doest require `identifierAttribute` but accepts bk and pk

➡️ at this point identifierAttribute column is removed we can begin to remove
identifierAttribute from the business class `Entity`

### Cleanup phase:

- [X] changer l'interprétation de `model_aggregate_stored` pour aller vers la
  nouvelle table
- [X] changer l'interprétation de `entity_created` pour aller vers la nouvelle
  table

We can do that much later

- [ ] completely delete `entity_created` v1 from event stack
- [ ] completely delete `entity_identifier_attribute_updated` v1 from event
  stack
- [ ] completely delete `model_aggregate_stored` v1 from event stack

### Post cleanup

- [ ] unit tests that update PK reads pk
- [ ] unit tests that update bk reads bk
- [ ] unit tests that model copy with complex bk and pk
- [ ] unit tests that model imports with complex bk and pk
- [ ] in Model_Release_Test add test with all possible objects, bk et pk, to
  verify that we don't have old pointers somewhere
- [ ] `EntityAttribute_Delete_Test` we had a test `delete entity attribute used as identifier throws error` but is now useless because the rules changed. It had been deleted. But, now, we need to test that if attributes are removed, business keys and primary keys don't have the attribute anymore. We need to decide if attribute as business key can be deleted or not and if yes, it we delete the bk if empty. 

### Table specs

table `model_entity_pk_snapshot`, `EntityPKTable` in kotlin:

- `id` with type `EntityPKSnapshotId`, not null
- `lineage_id` with type `EntityPrimaryKeyId`, not null
- `model_entity_snapshot_id` with type `EntitySnapshotId`, not null, foreign key
  on
  `model_entity_snapshot.id`

table `model_entity_pk_attribute_snapshot`: like an id bag ()

- `model_entity_pk_snapshot_id` with type `EntityPKSnapshotId`, not null
- `priority`: Integer, not null
- `model_entity_attribute_snapshot_id` with type `AttributeSnapshotId`, not
  null, foreign key on `model_entity_attribute_snapshot.id`

Must be unique in `model_entity_pk_attribute_snapshot` :
`(model_entity_pk_snapshot_id, model_entity_attribute_snapshot_id)`
and
`(model_entity_pk_snapshot_id, priority)`

table `model_business_key_snapshot`, `BusinessKeyTable` in kotlin:

- `id` with type `BusinessKeySnapshotId`, not null
- `lineage_id` with type `BusinessKeyId`, not null
- `model_entity_snapshot_id` with type `EntitySnapshotId`, not null, foreign key
  on
  `model_entity_snapshot.id`
- `key` TEXT NOT NULL
- `name` TEXT nullable
- `description` TEXT nullable

Must be unique in `model_business_key_snapshot` :
`(model_entity_snapshot_id, key)`

table `model_business_key_attribute_snapshot`, `BusinessKeyAttributeTable` in
kotlin:

- `model_business_key_snapshot_id` with type `BusinessKeySnapshotId`, not null
- `priority`: Integer, not null
- `model_entity_attribute_snapshot_id` with type `AttributeSnapshotId`, not
  null, foreign key on `model_entity_attribute_snapshot.id`

Must be unique in `model_business_key_attribute_snapshot` :
`(model_business_key_snapshot_id, model_entity_attribute_snapshot_id)`
and
`(model_business_key_snapshot_id, priority)`

## ModelAction

Ajustements to do once there is no more `identifierAttribute` in Entity and everything is ok in database. 

- [ ] `ModelAction.Inspect_Json` remove identifier attribute from result, add pk and bk, test. Tests still to do.  
- [ ] `ModelAction.Compare` do something (i don't know what), but we must see business key changes and pk changes  
- [ ] `ModelAction.MaintenanceRebuildCaches` need to test manually  
- [ ] `ModelAction.Model_Export` need to test manually  
- [ ] `ModelAction.Model_Export_Version` need to test manually  
- [ ] `ModelAction.Model_Create` adapt creation to remove required identifier attribute if there is one   
- [ ] `ModelAction.Model_Copy` check if tests are done and bk and pk are copied  
- [ ] `ModelAction.Model_Release` check if tests are done and bk and pk are fixed  
- [ ] `ModelAction.Model_Delete` manual test  
- [ ] `ModelAction.Entity_Create` : remove identifier attribute, implement and test  
- [ ] `ModelAction.Entity_Delete` : manual test and database check
- [ ] `ModelAction.EntityAttribute_Delete` : manual test
- [ ] `ModelAction.HistoryVersionChanges` : adapt

To create

- [ ] `ModelAction.EntityPrimaryKey_Update` : implement and test each 
- [ ] `ModelAction.BusinessKey_Create` : implement and test each 
- [ ] `ModelAction.BusinessKey_Update_Key` : implement and test each 
- [ ] `ModelAction.BusinessKey_Update_Name` : implement and test each 
- [ ] `ModelAction.BusinessKey_Update_Description` : implement and test each 
- [ ] `ModelAction.BusinessKey_Update_Participants` : implement and test each 
- [ ] `ModelAction.BusinessKey_Delete` : implement and test each 

## External databases JDBC imports

- [X] when reading table columns from JDBC metadata, interpret primary keys as
  `entityPrimaryKeys:EntityPrimaryKey`, even with composite keys

Cleanup phase

- [X] remove old autodetection from compatibility
  `toOldDeprecatedEntityIdentifierAttribute`, and
  `val pkAttributeId = toOldDeprecatedEntityIdentifierAttribute(table, collector, entityId)`

## Json import / export

Compatibility phase:

- [x] Create a new schema version 3.0.0 without `identifierAttribute`, where we
  add `primaryKeys` in entity and `businessKeys` in model root.

## Json import

Compatibility phase:

- [x] interpret imported JSON's `entityPrimaryKeys` and `businessKeys` as
  `entityPrimaryKeys` and `businessKeys`
- [x] In schema version 2.0.0, interpret JSON `identifierAttribute` as
  primarykey
- [x] Deserialize JSON from schema 3.0.0 and import primary and business keys

Cleanup phase:

- [X] delete all usage of `ModelAggregate.identifierAttribute` in
  JsonDeserializerV2
- [X] delete all usage of `ModelAggregate.identifierAttribute` in
  JsonDeserializerV3

## Json export

Compatibility phase:

- [x] schema 3.0.0 serializer, write `entityPrimaryKeys` and `businessKeys`, no
  `identifierAttribute`
- [x] schéma 2.0.0 serializer, write `identifierAttribute` from
  `entityPrimaryKeys`

## Frictionless converter

Compatibility phase:

- [x] interpret `identifierAttribute` in frictionless format as
  `entityPrimaryKeys`

Cleanup phase:

- [X] remove usages of `identifierAttribute` in FrictionlessConverter
- [X] supprimer toute notion de `identifierAttribute`

## Elements impactés

- change storage events

## models-storage-json

- Upgrade JSON version to 3.0,
- add "primaryKey" to entity with list of ordered attributes
- add "businessKeys" to attributes { id: string, entityId: string, attributeIds:
  string[], name: string?, description:string? }
- remove "identifierAttribute" from entity,
- add tests for pbkeys

## Documentation

- [x] Documentation on database imports pk
- [x] Documentation on tableschema imports pk
- [ ] Documentation on business keys
- [ ] Documentation on primary keys

## UI

- [ ] Adapt in Model history: entity_identifier_attribute_updated
- [ ] Adapt in Model comparison. `isEntityStructuralChanged` from backend must say if pk changed (not the bk, just the PK)
- [ ] display and manage the entity PK
- [ ] display and manage business keys

## Model validation

- [x] suppression du check specifique
- [ ] revoir les TU
- [ ] au niveau TU ajouter des controles sur les relations pk/bk vers entityId,
  attributeId pour voir si on pointe bien sur des références valides
