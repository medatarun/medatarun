package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.domain.search.SearchResults
import io.medatarun.model.infra.*
import io.medatarun.model.infra.db.ModelStorageAdapters.toEntity
import io.medatarun.model.infra.db.ModelStorageAdapters.toEntityAttribute
import io.medatarun.model.infra.db.ModelStorageAdapters.toModel
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationship
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationshipAttribute
import io.medatarun.model.infra.db.ModelStorageAdapters.toRelationshipRole
import io.medatarun.model.infra.db.ModelStorageAdapters.toType
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.db.tables.*
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.ports.exposed.ModelTypeInitializer
import io.medatarun.model.ports.needs.ModelRepoCmd
import io.medatarun.model.ports.needs.ModelStorage
import io.medatarun.model.ports.needs.ModelStorageSearchQuery
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.id.Id
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*

class ModelStorageDb(
    private val db: DbConnectionFactory
) : ModelStorage {
    private val searchRead = ModelStorageDbSearchRead(db)
    private val searchWrite = ModelStorageDbSearchWrite(db)

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    // Model

    override fun existsModelById(id: ModelId): Boolean {
        return db.withExposed {
            ModelTable.select(ModelTable.id).where { ModelTable.id eq id.asString() }.limit(1).any()
        }
    }

    override fun existsModelByKey(key: ModelKey): Boolean {
        return db.withExposed {
            ModelTable.select(ModelTable.id).where { ModelTable.key eq key.value }.limit(1).any()
        }
    }

    override fun findAllModelIds(): List<ModelId> {
        return db.withExposed {
            ModelTable.selectAll().map { ModelId.fromString(it[ModelTable.id]) }
        }
    }

    override fun findModelAggregateByKeyOptional(key: ModelKey): ModelAggregate? {
        return db.withExposed {
            val row = ModelTable.selectAll().where { ModelTable.key eq key.value }.singleOrNull()
            if (row == null) null else loadModelAggregate(row)
        }
    }

    override fun findModelAggregateByIdOptional(id: ModelId): ModelAggregate? {
        return db.withExposed {
            val row = ModelTable.selectAll().where { ModelTable.id eq id.asString() }.singleOrNull()
            if (row == null) null else loadModelAggregate(row)
        }
    }

    override fun findModelByKeyOptional(key: ModelKey): Model? {
        return db.withExposed {
            val row = ModelTable.selectAll().where { ModelTable.key eq key.value }.singleOrNull()
            if (row == null) null else toModel(ModelRecord.read(row))
        }
    }

    override fun findModelByIdOptional(id: ModelId): Model? {
        return db.withExposed {
            val row = ModelTable.selectAll().where { ModelTable.id eq id.asString() }.singleOrNull()
            if (row == null) null else toModel(ModelRecord.read(row))
        }
    }

    override fun findTypeByKeyOptional(
        modelId: ModelId, key: TypeKey
    ): ModelType? {
        return db.withExposed {
            ModelTypeTable.selectAll().where {
                (ModelTypeTable.modelId eq modelId.asString()) and (ModelTypeTable.key eq key.value)
            }.singleOrNull()?.let { row -> toType(ModelTypeRecord.read(row)) }
        }
    }

    override fun findTypeByIdOptional(
        modelId: ModelId,
        typeId: TypeId
    ): ModelType? {
        return db.withExposed {
            ModelTypeTable.selectAll().where {
                (ModelTypeTable.modelId eq modelId.asString()) and (ModelTypeTable.id eq typeId.asString())
            }.singleOrNull()?.let { row -> toType(ModelTypeRecord.read(row)) }
        }
    }

    override fun findEntityByIdOptional(
        modelId: ModelId,
        entityId: EntityId
    ): Entity? {
        return db.withExposed {
            EntityTable.selectAll().where {
                (EntityTable.modelId eq modelId.asString()) and
                        (EntityTable.id eq entityId.asString())
            }.singleOrNull()?.let { row -> toEntity(EntityRecord.read(row), loadEntityTags(entityId)) }
        }
    }

    override fun findEntityByKeyOptional(
        modelId: ModelId,
        entityKey: EntityKey
    ): Entity? {
        return db.withExposed {
            EntityTable.selectAll().where {
                (EntityTable.modelId eq modelId.asString()) and
                        (EntityTable.key eq entityKey.asString())
            }.singleOrNull()?.let { row ->
                val record = EntityRecord.read(row)
                val tags = loadEntityTags(EntityId.fromString(record.id))
                toEntity(record, tags)
            }
        }
    }

    override fun findEntityAttributeByIdOptional(
        modelId: ModelId,
        entityId: EntityId,
        attributeId: AttributeId
    ): Attribute? {
        return db.withExposed {
            EntityAttributeTable.join(
                EntityTable,
                JoinType.INNER,
                EntityAttributeTable.entityId,
                EntityTable.id
            ).selectAll()
                .where { (EntityTable.modelId eq modelId.asString()) and (EntityAttributeTable.entityId eq entityId.asString()) and (EntityAttributeTable.id eq attributeId.asString()) }
                .singleOrNull()
                ?.let { row ->
                    val record = EntityAttributeRecord.read(row)
                    val tags = loadEntityAttributeTags(AttributeId.fromString(record.id))
                    toEntityAttribute(record, tags)
                }
        }
    }

    override fun findEntityAttributeByKeyOptional(
        modelId: ModelId,
        entityId: EntityId,
        key: AttributeKey
    ): Attribute? {
        return db.withExposed {
            EntityAttributeTable.join(
                EntityTable,
                JoinType.INNER,
                EntityAttributeTable.entityId,
                EntityTable.id
            ).selectAll()
                .where { (EntityTable.modelId eq modelId.asString()) and (EntityAttributeTable.entityId eq entityId.asString()) and (EntityAttributeTable.key eq key.asString()) }
                .singleOrNull()
                ?.let { row ->
                    val record = EntityAttributeRecord.read(row)
                    val tags = loadEntityAttributeTags(AttributeId.fromString(record.id))
                    toEntityAttribute(record, tags)
                }
        }
    }

    override fun findRelationshipByIdOptional(modelId: ModelId, relationshipId: RelationshipId): Relationship? {
        return findRelationshipByOptional(modelId, RelationshipTable.id eq relationshipId.asString())
    }

    override fun findRelationshipByKeyOptional(modelId: ModelId, relationshipKey: RelationshipKey): Relationship? {
        return findRelationshipByOptional(modelId, RelationshipTable.key eq relationshipKey.asString())
    }


    private fun findRelationshipByOptional(modelId: ModelId, criterion: Expression<Boolean>): Relationship? {
        return db.withExposed {
            val roleRecords = RelationshipRoleTable.join(
                RelationshipTable,
                JoinType.INNER,
                onColumn = RelationshipRoleTable.relationshipId,
                otherColumn = RelationshipTable.id
            ).selectAll()
                .where { (RelationshipTable.modelId eq modelId.asString()) and criterion }
                .map { RelationshipRoleRecord.read(it) }

            RelationshipTable.selectAll()
                .where { (RelationshipTable.modelId eq modelId.asString()) and criterion }
                .singleOrNull()
                ?.let { row ->
                    val record = RelationshipRecord.read(row)
                    val tags = loadRelationshipTags(RelationshipId.fromString(record.id))
                    toRelationship(record, roleRecords, tags)
                }
        }
    }

    override fun findRelationshipRoleByIdOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        roleId: RelationshipRoleId
    ): RelationshipRole? {
        return findRelationshipRoleByOptional(modelId, relationshipId, RelationshipRoleTable.id eq roleId.asString())
    }

    override fun findRelationshipRoleByKeyOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        roleKey: RelationshipRoleKey
    ): RelationshipRole? {
        return findRelationshipRoleByOptional(modelId, relationshipId, RelationshipRoleTable.key eq roleKey.asString())
    }

    private fun findRelationshipRoleByOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        criterion: Op<Boolean>
    ): RelationshipRole? {
        return RelationshipRoleTable.join(
            RelationshipTable,
            JoinType.INNER,
            onColumn = RelationshipRoleTable.relationshipId,
            otherColumn = RelationshipTable.id
        )
            .selectAll()
            .where {
                (RelationshipTable.modelId eq modelId.asString()) and
                        (RelationshipTable.id eq relationshipId.asString()) and
                        criterion
            }
            .singleOrNull()
            ?.let { toRelationshipRole(RelationshipRoleRecord.read(it)) }
    }


    override fun findRelationshipAttributeByIdOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        attributeId: AttributeId
    ): Attribute? {
        return findRelationshipAttributeByOptional(
            modelId,
            relationshipId,
            RelationshipAttributeTable.id eq attributeId.asString()
        )
    }

    override fun findRelationshipAttributeByKeyOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        key: AttributeKey
    ): Attribute? {
        return findRelationshipAttributeByOptional(
            modelId,
            relationshipId,
            RelationshipAttributeTable.key eq key.asString()
        )
    }


    fun findRelationshipAttributeByOptional(
        modelId: ModelId,
        relationshipId: RelationshipId,
        criterion: Expression<Boolean>
    ): Attribute? {
        return db.withExposed {
            RelationshipAttributeTable.join(
                RelationshipTable,
                JoinType.INNER,
                RelationshipAttributeTable.relationshipId,
                RelationshipTable.id
            ).selectAll()
                .where { (RelationshipTable.modelId eq modelId.asString()) and (RelationshipAttributeTable.relationshipId eq relationshipId.asString()) and criterion }
                .singleOrNull()
                ?.let { row ->
                    val record = RelationshipAttributeRecord.read(row)
                    val tags = loadRelationshipAttributeTags(AttributeId.fromString(record.id))
                    toRelationshipAttribute(record, tags)
                }
        }
    }

    // -------------------------------------------------------------------------
    // Search
    // -------------------------------------------------------------------------

    override fun search(query: ModelStorageSearchQuery): SearchResults {
        return searchRead.search(query)
    }

    // -------------------------------------------------------------------------
    // Analytics
    // -------------------------------------------------------------------------

    override fun isTypeUsedInEntityAttributes(
        modelId: ModelId,
        typeId: TypeId
    ): Boolean {
        return db.withExposed {
            EntityAttributeTable.join(
                EntityTable,
                JoinType.INNER,
                onColumn = EntityAttributeTable.entityId,
                otherColumn = EntityTable.id
            ).selectAll()
                .where {
                    (EntityAttributeTable.typeId eq typeId.asString()) and
                            (EntityTable.modelId eq modelId.asString())
                }
                .any()
        }
    }

    override fun isTypeUsedInRelationshipAttributes(
        modelId: ModelId,
        typeId: TypeId
    ): Boolean {
        return db.withExposed {
            RelationshipAttributeTable.join(
                RelationshipTable,
                JoinType.INNER,
                onColumn = RelationshipAttributeTable.relationshipId,
                otherColumn = RelationshipTable.id
            ).selectAll()
                .where {
                    (RelationshipAttributeTable.typeId eq typeId.asString()) and
                            (RelationshipTable.modelId eq modelId.asString())
                }
                .any()
        }

    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    override fun dispatch(cmd: ModelRepoCmd) {
        when (cmd) {
            //@formatter:off
            is ModelRepoCmd.StoreModelAggregate -> storeModelAggregate(cmd.model)
            is ModelRepoCmd.CreateModel -> createModel(cmd.model)
            is ModelRepoCmd.DeleteModel -> deleteModel(cmd.modelId)
            is ModelRepoCmd.UpdateModelName -> updateModelName(cmd.modelId, cmd.name)
            is ModelRepoCmd.UpdateModelDescription -> updateModelDescription(cmd.modelId, cmd.description)
            is ModelRepoCmd.UpdateModelVersion -> updateModelVersion(cmd.modelId, cmd.version)
            is ModelRepoCmd.UpdateModelDocumentationHome -> updateModelDocumentationHome(cmd.modelId, cmd.url)
            is ModelRepoCmd.UpdateModelTagAdd -> addModelTag(cmd.modelId, cmd.tagId)
            is ModelRepoCmd.UpdateModelTagDelete -> deleteModelTag(cmd.modelId, cmd.tagId)
            is ModelRepoCmd.CreateType -> createType(cmd.modelId, cmd.initializer)
            is ModelRepoCmd.UpdateTypeKey -> updateTypeKey(cmd.modelId, cmd.typeId, cmd.value)
            is ModelRepoCmd.UpdateTypeName -> updateTypeName(cmd.modelId, cmd.typeId, cmd.value)
            is ModelRepoCmd.UpdateTypeDescription -> updateTypeDescription(cmd.modelId, cmd.typeId, cmd.value)
            is ModelRepoCmd.DeleteType -> deleteType(cmd.modelId, cmd.typeId)
            is ModelRepoCmd.CreateEntity -> createEntity(cmd)
            is ModelRepoCmd.UpdateEntityKey -> updateEntityKey(cmd.modelId, cmd.entityId, cmd.value)
            is ModelRepoCmd.UpdateEntityName -> updateEntityName(cmd.modelId, cmd.entityId, cmd.value)
            is ModelRepoCmd.UpdateEntityDescription -> updateEntityDescription(cmd.modelId, cmd.entityId, cmd.value)
            is ModelRepoCmd.UpdateEntityIdentifierAttribute -> updateEntityIdentifierAttribute(cmd.modelId, cmd.entityId, cmd.value)
            is ModelRepoCmd.UpdateEntityDocumentationHome -> updateEntityDocumentationHome(cmd.modelId, cmd.entityId, cmd.value)
            is ModelRepoCmd.UpdateEntityTagAdd -> addEntityTag(cmd.entityId, cmd.tagId)
            is ModelRepoCmd.UpdateEntityTagDelete -> deleteEntityTag(cmd.entityId, cmd.tagId)
            is ModelRepoCmd.DeleteEntity -> deleteEntity(cmd.modelId, cmd.entityId)
            is ModelRepoCmd.CreateEntityAttribute -> createEntityAttribute(cmd)
            is ModelRepoCmd.UpdateEntityAttributeKey -> updateEntityAttributeKey(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelRepoCmd.UpdateEntityAttributeName -> updateEntityAttributeName(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelRepoCmd.UpdateEntityAttributeDescription -> updateEntityAttributeDescription(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelRepoCmd.UpdateEntityAttributeType -> updateEntityAttributeType(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelRepoCmd.UpdateEntityAttributeOptional -> updateEntityAttributeOptional(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelRepoCmd.UpdateEntityAttributeTagAdd -> addEntityAttributeTag(cmd.attributeId, cmd.tagId)
            is ModelRepoCmd.UpdateEntityAttributeTagDelete -> deleteEntityAttributeTag(cmd.attributeId, cmd.tagId)
            is ModelRepoCmd.DeleteEntityAttribute -> deleteEntityAttribute(cmd.entityId, cmd.attributeId)
            is ModelRepoCmd.CreateRelationship -> createRelationship(cmd)
            is ModelRepoCmd.UpdateRelationshipKey -> updateRelationshipKey(cmd.relationshipId, cmd.value)
            is ModelRepoCmd.UpdateRelationshipName -> updateRelationshipName(cmd.relationshipId, cmd.value)
            is ModelRepoCmd.UpdateRelationshipDescription -> updateRelationshipDescription(cmd.relationshipId, cmd.value)
            is ModelRepoCmd.UpdateRelationshipRoleKey -> updateRelationshipRoleKey(cmd.relationshipRoleId, cmd.value)
            is ModelRepoCmd.UpdateRelationshipRoleName -> updateRelationshipRoleName(cmd.relationshipRoleId, cmd.value)
            is ModelRepoCmd.UpdateRelationshipRoleEntity -> updateRelationshipRoleEntity(cmd.relationshipRoleId, cmd.value)
            is ModelRepoCmd.UpdateRelationshipRoleCardinality -> updateRelationshipRoleCardinality(cmd.relationshipRoleId, cmd.value)
            is ModelRepoCmd.UpdateRelationshipTagAdd -> addRelationshipTag(cmd.relationshipId, cmd.tagId)
            is ModelRepoCmd.UpdateRelationshipTagDelete -> deleteRelationshipTag(cmd.relationshipId, cmd.tagId)
            is ModelRepoCmd.DeleteRelationship -> deleteRelationship(cmd.modelId, cmd.relationshipId)
            is ModelRepoCmd.CreateRelationshipAttribute -> createRelationshipAttribute(cmd)
            is ModelRepoCmd.UpdateRelationshipAttributeKey -> updateRelationshipAttributeKey(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelRepoCmd.UpdateRelationshipAttributeName -> updateRelationshipAttributeName(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelRepoCmd.UpdateRelationshipAttributeDescription -> updateRelationshipAttributeDescription(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelRepoCmd.UpdateRelationshipAttributeType -> updateRelationshipAttributeType(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelRepoCmd.UpdateRelationshipAttributeOptional -> updateRelationshipAttributeOptional(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelRepoCmd.UpdateRelationshipAttributeTagAdd -> addRelationshipAttributeTag(cmd.attributeId, cmd.tagId)
            is ModelRepoCmd.UpdateRelationshipAttributeTagDelete -> deleteRelationshipAttributeTag(cmd.attributeId, cmd.tagId)
            is ModelRepoCmd.DeleteRelationshipAttribute -> deleteRelationshipAttribute(cmd.relationshipId, cmd.attributeId)
            //@formatter:on
        }
    }

    private fun createModel(model: Model) {
        val inMemoryModel = ModelInMemory.of(model)
        db.withExposed {
            insertModel(inMemoryModel)
            searchWrite.upsertModelSearchItem(inMemoryModel.id)
        }
    }

    private fun storeModelAggregate(model: ModelAggregate) {
        // TODO finish this
        val inMemoryModel = ModelInMemory.of(model)
        db.withExposed {
            insertModel(model.model)
            insertModelTags(model.id, model.tags)
            searchWrite.upsertModelSearchItem(inMemoryModel.id)
        }
    }

    private fun deleteModel(modelId: ModelId) {
        db.withExposed {
            searchWrite.deleteModelBranch(modelId)
            ModelTable.deleteWhere { id eq modelId.asString() }
        }
    }

    private fun updateModelName(modelId: ModelId, name: LocalizedText) {
        db.withExposed {
            ModelTable.update(where = { ModelTable.id eq modelId.asString() }) { row ->
                row[ModelTable.name] = localizedTextToString(name)
            }
            searchWrite.upsertModelSearchItem(modelId)
        }
    }

    private fun updateModelDescription(modelId: ModelId, description: LocalizedMarkdown?) {
        db.withExposed {
            ModelTable.update(where = { ModelTable.id eq modelId.asString() }) { row ->
                row[ModelTable.description] = localizedMarkdownToString(description)
            }
            searchWrite.upsertModelSearchItem(modelId)
        }
    }

    private fun updateModelVersion(modelId: ModelId, version: ModelVersion) {
        db.withExposed {
            ModelTable.update(where = { ModelTable.id eq modelId.asString() }) { row ->
                row[ModelTable.version] = version.value
            }
        }
    }

    private fun updateModelDocumentationHome(modelId: ModelId, documentationHome: java.net.URL?) {
        db.withExposed {
            ModelTable.update(where = { ModelTable.id eq modelId.asString() }) { row ->
                row[ModelTable.documentationHome] = documentationHome?.toExternalForm()
            }
        }
    }

    private fun addModelTag(modelId: ModelId, tagId: TagId) {
        db.withExposed {
            val exists = ModelTagTable.select(ModelTagTable.modelId).where {
                (ModelTagTable.modelId eq modelId.asString()) and (ModelTagTable.tagId eq tagId.asString())
            }.limit(1).any()
            if (!exists) {
                ModelTagTable.insert { row ->
                    row[ModelTagTable.modelId] = modelId.asString()
                    row[ModelTagTable.tagId] = tagId.asString()
                }
            }
            searchWrite.upsertModelSearchItem(modelId)
        }
    }

    private fun deleteModelTag(modelId: ModelId, tagId: TagId) {
        db.withExposed {
            ModelTagTable.deleteWhere {
                (ModelTagTable.modelId eq modelId.asString()) and (ModelTagTable.tagId eq tagId.asString())
            }
            searchWrite.upsertModelSearchItem(modelId)
        }
    }

    private fun createType(modelId: ModelId, initializer: ModelTypeInitializer) {
        db.withExposed {
            ModelTypeTable.insert { row ->
                row[ModelTypeTable.id] = TypeId.generate().asString()
                row[ModelTypeTable.modelId] = modelId.asString()
                row[ModelTypeTable.key] = initializer.key.asString()
                row[ModelTypeTable.name] = localizedTextToString(initializer.name)
                row[ModelTypeTable.description] = localizedMarkdownToString(initializer.description)
            }
        }
    }

    private fun updateTypeKey(modelId: ModelId, typeId: TypeId, value: TypeKey) {
        db.withExposed {
            ModelTypeTable.update(
                where = {
                    (ModelTypeTable.id eq typeId.asString()) and (ModelTypeTable.modelId eq modelId.asString())
                }) { row ->
                row[ModelTypeTable.key] = value.asString()
            }
        }
    }

    private fun updateTypeName(modelId: ModelId, typeId: TypeId, value: LocalizedText?) {
        db.withExposed {
            ModelTypeTable.update(
                where = {
                    (ModelTypeTable.id eq typeId.asString()) and (ModelTypeTable.modelId eq modelId.asString())
                }) { row ->
                row[ModelTypeTable.name] = localizedTextToString(value)
            }
        }
    }

    private fun updateTypeDescription(modelId: ModelId, typeId: TypeId, value: LocalizedMarkdown?) {
        db.withExposed {
            ModelTypeTable.update(
                where = {
                    (ModelTypeTable.id eq typeId.asString()) and (ModelTypeTable.modelId eq modelId.asString())
                }) { row ->
                row[ModelTypeTable.description] = localizedMarkdownToString(value)
            }
        }
    }

    private fun deleteType(modelId: ModelId, typeId: TypeId) {
        db.withExposed {
            ModelTypeTable.deleteWhere {
                (ModelTypeTable.id eq typeId.asString()) and (ModelTypeTable.modelId eq modelId.asString())
            }
        }
    }

    private fun createEntity(cmd: ModelRepoCmd.CreateEntity) {
        db.withExposed {
            insertEntity(cmd)
        }
    }

    private fun updateEntityKey(modelId: ModelId, entityId: EntityId, value: EntityKey) {
        db.withExposed {
            EntityTable.update(
                where = {
                    (EntityTable.id eq entityId.asString()) and (EntityTable.modelId eq modelId.asString())
                }) { row ->
                row[EntityTable.key] = value.asString()
            }
            searchWrite.upsertEntitySearchItem(entityId)
        }
    }

    private fun updateEntityName(modelId: ModelId, entityId: EntityId, value: LocalizedText?) {
        db.withExposed {
            EntityTable.update(
                where = {
                    (EntityTable.id eq entityId.asString()) and (EntityTable.modelId eq modelId.asString())
                }) { row ->
                row[EntityTable.name] = localizedTextToString(value)
            }
            searchWrite.upsertEntitySearchItem(entityId)
        }
    }

    private fun updateEntityDescription(modelId: ModelId, entityId: EntityId, value: LocalizedMarkdown?) {
        db.withExposed {
            EntityTable.update(
                where = {
                    (EntityTable.id eq entityId.asString()) and (EntityTable.modelId eq modelId.asString())
                }) { row ->
                row[EntityTable.description] = localizedMarkdownToString(value)
            }
            searchWrite.upsertEntitySearchItem(entityId)
        }
    }

    private fun updateEntityIdentifierAttribute(modelId: ModelId, entityId: EntityId, value: AttributeId) {
        db.withExposed {
            EntityTable.update(
                where = {
                    (EntityTable.id eq entityId.asString()) and (EntityTable.modelId eq modelId.asString())
                }) { row ->
                row[EntityTable.identifierAttributeId] = value.asString()
            }
        }
    }

    private fun updateEntityDocumentationHome(modelId: ModelId, entityId: EntityId, value: java.net.URL?) {
        db.withExposed {
            EntityTable.update(
                where = {
                    (EntityTable.id eq entityId.asString()) and (EntityTable.modelId eq modelId.asString())
                }) { row ->
                row[EntityTable.documentationHome] = value?.toExternalForm()
            }
        }
    }

    private fun addEntityTag(entityId: EntityId, tagId: TagId) {
        db.withExposed {
            val exists = EntityTagTable.select(EntityTagTable.entityId).where {
                (EntityTagTable.entityId eq entityId.asString()) and (EntityTagTable.tagId eq tagId.asString())
            }.limit(1).any()
            if (!exists) {
                EntityTagTable.insert { row ->
                    row[EntityTagTable.entityId] = entityId.asString()
                    row[EntityTagTable.tagId] = tagId.asString()
                }
            }
            searchWrite.upsertEntitySearchItem(entityId)
        }
    }

    private fun deleteEntityTag(entityId: EntityId, tagId: TagId) {
        db.withExposed {
            EntityTagTable.deleteWhere {
                (EntityTagTable.entityId eq entityId.asString()) and (EntityTagTable.tagId eq tagId.asString())
            }
            searchWrite.upsertEntitySearchItem(entityId)
        }
    }

    private fun deleteEntity(modelId: ModelId, entityId: EntityId) {
        db.withExposed {
            searchWrite.deleteEntityBranch(entityId)
            EntityTable.deleteWhere {
                (EntityTable.id eq entityId.asString()) and (EntityTable.modelId eq modelId.asString())
            }
        }
    }

    private fun createEntityAttribute(cmd: ModelRepoCmd.CreateEntityAttribute) {
        db.withExposed {
            insertEntityAttribute(
                attributeId = cmd.attributeId,
                entityId = cmd.entityId,
                key = cmd.key,
                name = cmd.name,
                description = cmd.description,
                typeId = cmd.typeId,
                optional = cmd.optional
            )
        }
    }

    private fun updateEntityAttributeKey(entityId: EntityId, attributeId: AttributeId, value: AttributeKey) {
        db.withExposed {
            EntityAttributeTable.update(
                where = {
                    (EntityAttributeTable.id eq attributeId.asString()) and (EntityAttributeTable.entityId eq entityId.asString())
                }) { row ->
                row[EntityAttributeTable.key] = value.asString()
            }
            searchWrite.upsertEntityAttributeSearchItem(attributeId)
        }
    }

    private fun updateEntityAttributeName(entityId: EntityId, attributeId: AttributeId, value: LocalizedText?) {
        db.withExposed {
            EntityAttributeTable.update(
                where = {
                    (EntityAttributeTable.id eq attributeId.asString()) and (EntityAttributeTable.entityId eq entityId.asString())
                }) { row ->
                row[EntityAttributeTable.name] = localizedTextToString(value)
            }
            searchWrite.upsertEntityAttributeSearchItem(attributeId)
        }
    }

    private fun updateEntityAttributeDescription(
        entityId: EntityId, attributeId: AttributeId, value: LocalizedMarkdown?
    ) {
        db.withExposed {
            EntityAttributeTable.update(
                where = {
                    (EntityAttributeTable.id eq attributeId.asString()) and (EntityAttributeTable.entityId eq entityId.asString())
                }) { row ->
                row[EntityAttributeTable.description] = localizedMarkdownToString(value)
            }
            searchWrite.upsertEntityAttributeSearchItem(attributeId)
        }
    }

    private fun updateEntityAttributeType(entityId: EntityId, attributeId: AttributeId, value: TypeId) {
        db.withExposed {
            EntityAttributeTable.update(
                where = {
                    (EntityAttributeTable.id eq attributeId.asString()) and (EntityAttributeTable.entityId eq entityId.asString())
                }) { row ->
                row[EntityAttributeTable.typeId] = value.asString()
            }
        }
    }

    private fun updateEntityAttributeOptional(entityId: EntityId, attributeId: AttributeId, value: Boolean) {
        db.withExposed {
            EntityAttributeTable.update(
                where = {
                    (EntityAttributeTable.id eq attributeId.asString()) and (EntityAttributeTable.entityId eq entityId.asString())
                }) { row ->
                row[EntityAttributeTable.optional] = value
            }
        }
    }

    private fun addEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        db.withExposed {
            val exists = EntityAttributeTagTable.select(EntityAttributeTagTable.attributeId).where {
                (EntityAttributeTagTable.attributeId eq attributeId.asString()) and (EntityAttributeTagTable.tagId eq tagId.asString())
            }.limit(1).any()
            if (!exists) {
                EntityAttributeTagTable.insert { row ->
                    row[EntityAttributeTagTable.attributeId] = attributeId.asString()
                    row[EntityAttributeTagTable.tagId] = tagId.asString()
                }
            }
            searchWrite.upsertEntityAttributeSearchItem(attributeId)
        }
    }

    private fun deleteEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        db.withExposed {
            EntityAttributeTagTable.deleteWhere {
                (EntityAttributeTagTable.attributeId eq attributeId.asString()) and (EntityAttributeTagTable.tagId eq tagId.asString())
            }
            searchWrite.upsertEntityAttributeSearchItem(attributeId)
        }
    }

    private fun deleteEntityAttribute(entityId: EntityId, attributeId: AttributeId) {
        db.withExposed {
            searchWrite.deleteEntityAttributeSearchItem(attributeId)
            EntityAttributeTable.deleteWhere {
                (EntityAttributeTable.id eq attributeId.asString()) and (EntityAttributeTable.entityId eq entityId.asString())
            }
        }
    }

    private fun insertModel(model: Model) {
        ModelTable.insert { row ->
            row[ModelTable.id] = model.id.asString()
            row[ModelTable.key] = model.key.asString()
            row[ModelTable.name] = localizedTextToString(model.name)
            row[ModelTable.description] = localizedMarkdownToString(model.description)
            row[ModelTable.version] = model.version.value
            row[ModelTable.origin] = modelOriginToString(model.origin)
            row[ModelTable.documentationHome] = model.documentationHome?.toExternalForm()
        }
    }

    private fun insertModelTags(modelId: ModelId, tags: List<TagId>) {
        for (tagId in tags) {
            ModelTagTable.insert { row ->
                row[ModelTagTable.modelId] = modelId.asString()
                row[ModelTagTable.tagId] = tagId.asString()
            }
        }
    }

    private fun insertEntity(cmd: ModelRepoCmd.CreateEntity) {
        EntityTable.insert { row ->
            row[EntityTable.id] = cmd.entityId.asString()
            row[EntityTable.modelId] = cmd.modelId.asString()
            row[EntityTable.key] = cmd.key.asString()
            row[EntityTable.name] = localizedTextToString(cmd.name)
            row[EntityTable.description] = localizedMarkdownToString(cmd.description)
            row[EntityTable.identifierAttributeId] = cmd.identityAttributeId.asString()
            row[EntityTable.origin] = entityOriginToString(cmd.origin)
            row[EntityTable.documentationHome] = cmd.documentationHome?.toExternalForm()
        }

        insertEntityAttribute(
            attributeId = cmd.identityAttributeId,
            entityId = cmd.entityId,
            key = cmd.identityAttributeKey,
            name = cmd.identityAttributeName,
            description = cmd.identityAttributeDescription,
            typeId = cmd.identityAttributeTypeId,
            optional = cmd.identityAttributeIdOptional
        )

        searchWrite.upsertEntitySearchItem(cmd.entityId)
    }

    private fun insertEntityAttribute(
        attributeId: AttributeId,
        entityId: EntityId,
        key: AttributeKey,
        name: LocalizedText?,
        description: LocalizedMarkdown?,
        typeId: TypeId,
        optional: Boolean
    ) {
        EntityAttributeTable.insert { row ->
            row[EntityAttributeTable.id] = attributeId.asString()
            row[EntityAttributeTable.entityId] = entityId.asString()
            row[EntityAttributeTable.key] = key.asString()
            row[EntityAttributeTable.name] = localizedTextToString(name)
            row[EntityAttributeTable.description] = localizedMarkdownToString(description)
            row[EntityAttributeTable.typeId] = typeId.asString()
            row[EntityAttributeTable.optional] = optional
        }
        searchWrite.upsertEntityAttributeSearchItem(attributeId)
    }

    private fun createRelationship(cmd: ModelRepoCmd.CreateRelationship) {
        db.withExposed {
            insertRelationship(cmd)
        }
    }

    private fun updateRelationshipKey(relationshipId: RelationshipId, value: RelationshipKey) {
        db.withExposed {
            RelationshipTable.update(where = { RelationshipTable.id eq relationshipId.asString() }) { row ->
                row[RelationshipTable.key] = value.asString()
            }
            searchWrite.upsertRelationshipSearchItem(relationshipId)
        }
    }

    private fun updateRelationshipName(relationshipId: RelationshipId, value: LocalizedText?) {
        db.withExposed {
            RelationshipTable.update(where = { RelationshipTable.id eq relationshipId.asString() }) { row ->
                row[RelationshipTable.name] = localizedTextToString(value)
            }
            searchWrite.upsertRelationshipSearchItem(relationshipId)
        }
    }

    private fun updateRelationshipDescription(relationshipId: RelationshipId, value: LocalizedMarkdown?) {
        db.withExposed {
            RelationshipTable.update(where = { RelationshipTable.id eq relationshipId.asString() }) { row ->
                row[RelationshipTable.description] = localizedMarkdownToString(value)
            }
            searchWrite.upsertRelationshipSearchItem(relationshipId)
        }
    }

    private fun updateRelationshipRoleKey(relationshipRoleId: RelationshipRoleId, value: RelationshipRoleKey) {
        db.withExposed {
            RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq relationshipRoleId.asString() }) { row ->
                row[RelationshipRoleTable.key] = value.asString()
            }
        }
    }

    private fun updateRelationshipRoleName(relationshipRoleId: RelationshipRoleId, value: LocalizedText?) {
        db.withExposed {
            RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq relationshipRoleId.asString() }) { row ->
                row[RelationshipRoleTable.name] = localizedTextToString(value)
            }
        }
    }

    private fun updateRelationshipRoleEntity(relationshipRoleId: RelationshipRoleId, value: EntityId) {
        db.withExposed {
            RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq relationshipRoleId.asString() }) { row ->
                row[RelationshipRoleTable.entityId] = value.asString()
            }
        }
    }

    private fun updateRelationshipRoleCardinality(
        relationshipRoleId: RelationshipRoleId, value: RelationshipCardinality
    ) {
        db.withExposed {
            RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq relationshipRoleId.asString() }) { row ->
                row[RelationshipRoleTable.cardinality] = value.code
            }
        }
    }

    private fun addRelationshipTag(relationshipId: RelationshipId, tagId: TagId) {
        db.withExposed {
            val exists = RelationshipTagTable.select(RelationshipTagTable.relationshipId).where {
                (RelationshipTagTable.relationshipId eq relationshipId.asString()) and (RelationshipTagTable.tagId eq tagId.asString())
            }.limit(1).any()
            if (!exists) {
                RelationshipTagTable.insert { row ->
                    row[RelationshipTagTable.relationshipId] = relationshipId.asString()
                    row[RelationshipTagTable.tagId] = tagId.asString()
                }
            }
            searchWrite.upsertRelationshipSearchItem(relationshipId)
        }
    }

    private fun deleteRelationshipTag(relationshipId: RelationshipId, tagId: TagId) {
        db.withExposed {
            RelationshipTagTable.deleteWhere {
                (RelationshipTagTable.relationshipId eq relationshipId.asString()) and (RelationshipTagTable.tagId eq tagId.asString())
            }
            searchWrite.upsertRelationshipSearchItem(relationshipId)
        }
    }

    private fun deleteRelationship(modelId: ModelId, relationshipId: RelationshipId) {
        db.withExposed {
            searchWrite.deleteRelationshipBranch(relationshipId)
            RelationshipTable.deleteWhere {
                (RelationshipTable.id eq relationshipId.asString()) and (RelationshipTable.modelId eq modelId.asString())
            }
        }
    }

    private fun createRelationshipAttribute(cmd: ModelRepoCmd.CreateRelationshipAttribute) {
        db.withExposed {
            insertRelationshipAttribute(
                relationshipId = cmd.relationshipId,
                attributeId = cmd.attributeId,
                name = cmd.name,
                key = cmd.key,
                description = cmd.description,
                typeId = cmd.typeId,
                optional = cmd.optional
            )
        }
    }

    private fun updateRelationshipAttributeKey(
        relationshipId: RelationshipId, attributeId: AttributeId, value: AttributeKey
    ) {
        db.withExposed {
            RelationshipAttributeTable.update(
                where = {
                    (RelationshipAttributeTable.id eq attributeId.asString()) and (RelationshipAttributeTable.relationshipId eq relationshipId.asString())
                }) { row ->
                row[RelationshipAttributeTable.key] = value.asString()
            }
            searchWrite.upsertRelationshipAttributeSearchItem(attributeId)
        }
    }

    private fun updateRelationshipAttributeName(
        relationshipId: RelationshipId, attributeId: AttributeId, value: LocalizedText?
    ) {
        db.withExposed {
            RelationshipAttributeTable.update(
                where = {
                    (RelationshipAttributeTable.id eq attributeId.asString()) and (RelationshipAttributeTable.relationshipId eq relationshipId.asString())
                }) { row ->
                row[RelationshipAttributeTable.name] = localizedTextToString(value)
            }
            searchWrite.upsertRelationshipAttributeSearchItem(attributeId)
        }
    }

    private fun updateRelationshipAttributeDescription(
        relationshipId: RelationshipId, attributeId: AttributeId, value: LocalizedMarkdown?
    ) {
        db.withExposed {
            RelationshipAttributeTable.update(
                where = {
                    (RelationshipAttributeTable.id eq attributeId.asString()) and (RelationshipAttributeTable.relationshipId eq relationshipId.asString())
                }) { row ->
                row[RelationshipAttributeTable.description] = localizedMarkdownToString(value)
            }
            searchWrite.upsertRelationshipAttributeSearchItem(attributeId)
        }
    }

    private fun updateRelationshipAttributeType(
        relationshipId: RelationshipId, attributeId: AttributeId, value: TypeId
    ) {
        db.withExposed {
            RelationshipAttributeTable.update(
                where = {
                    (RelationshipAttributeTable.id eq attributeId.asString()) and (RelationshipAttributeTable.relationshipId eq relationshipId.asString())
                }) { row ->
                row[RelationshipAttributeTable.typeId] = value.asString()
            }
        }
    }

    private fun updateRelationshipAttributeOptional(
        relationshipId: RelationshipId, attributeId: AttributeId, value: Boolean
    ) {
        db.withExposed {
            RelationshipAttributeTable.update(
                where = {
                    (RelationshipAttributeTable.id eq attributeId.asString()) and (RelationshipAttributeTable.relationshipId eq relationshipId.asString())
                }) { row ->
                row[RelationshipAttributeTable.optional] = value
            }
        }
    }

    private fun addRelationshipAttributeTag(attributeId: AttributeId, tagId: TagId) {
        db.withExposed {
            val exists = RelationshipAttributeTagTable.select(RelationshipAttributeTagTable.attributeId).where {
                (RelationshipAttributeTagTable.attributeId eq attributeId.asString()) and (RelationshipAttributeTagTable.tagId eq tagId.asString())
            }.limit(1).any()
            if (!exists) {
                RelationshipAttributeTagTable.insert { row ->
                    row[RelationshipAttributeTagTable.attributeId] = attributeId.asString()
                    row[RelationshipAttributeTagTable.tagId] = tagId.asString()
                }
            }
            searchWrite.upsertRelationshipAttributeSearchItem(attributeId)
        }
    }

    private fun deleteRelationshipAttributeTag(attributeId: AttributeId, tagId: TagId) {
        db.withExposed {
            RelationshipAttributeTagTable.deleteWhere {
                (RelationshipAttributeTagTable.attributeId eq attributeId.asString()) and (RelationshipAttributeTagTable.tagId eq tagId.asString())
            }
            searchWrite.upsertRelationshipAttributeSearchItem(attributeId)
        }
    }

    private fun deleteRelationshipAttribute(relationshipId: RelationshipId, attributeId: AttributeId) {
        db.withExposed {
            searchWrite.deleteRelationshipAttributeSearchItem(attributeId)
            RelationshipAttributeTable.deleteWhere {
                (RelationshipAttributeTable.id eq attributeId.asString()) and (RelationshipAttributeTable.relationshipId eq relationshipId.asString())
            }
        }
    }

    private fun insertRelationship(cmd: ModelRepoCmd.CreateRelationship) {
        RelationshipTable.insert { row ->
            row[RelationshipTable.id] = cmd.relationshipId.asString()
            row[RelationshipTable.modelId] = cmd.modelId.asString()
            row[RelationshipTable.key] = cmd.key.asString()
            row[RelationshipTable.name] = localizedTextToString(cmd.name)
            row[RelationshipTable.description] = localizedMarkdownToString(cmd.description)
        }



        for (role in cmd.roles) {
            RelationshipRoleTable.insert { row ->
                row[RelationshipRoleTable.id] = role.id.asString()
                row[RelationshipRoleTable.relationshipId] = cmd.relationshipId.asString()
                row[RelationshipRoleTable.key] = role.key.asString()
                row[RelationshipRoleTable.entityId] = role.entityId.asString()
                row[RelationshipRoleTable.name] = localizedTextToString(role.name)
                row[RelationshipRoleTable.cardinality] = role.cardinality.code
            }
        }

        searchWrite.upsertRelationshipSearchItem(cmd.relationshipId)
    }

    private fun insertRelationshipAttribute(
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        key: AttributeKey,
        name: LocalizedText?,
        description: LocalizedMarkdown?,
        typeId: TypeId,
        optional: Boolean
    ) {
        RelationshipAttributeTable.insert { row ->
            row[RelationshipAttributeTable.id] = attributeId.asString()
            row[RelationshipAttributeTable.relationshipId] = relationshipId.asString()
            row[RelationshipAttributeTable.key] = key.asString()
            row[RelationshipAttributeTable.name] = localizedTextToString(name)
            row[RelationshipAttributeTable.description] = localizedMarkdownToString(description)
            row[RelationshipAttributeTable.typeId] = typeId.asString()
            row[RelationshipAttributeTable.optional] = optional
        }
        searchWrite.upsertRelationshipAttributeSearchItem(attributeId)
    }

    private fun loadModelAggregate(row: ResultRow): ModelAggregateInMemory {
        val record = ModelRecord.read(row)
        val modelId = ModelId.fromString(record.id)
        val types = loadTypes(modelId)
        val entities = loadEntities(modelId)
        val entityAttributes = loadEntityAttributes(modelId)
        val relationships = loadRelationships(modelId)
        val relationshipAttributes = loadRelationshipAttributes(modelId)

        return ModelAggregateInMemory(
            model = toModel(record),
            types = types,
            entities = entities,
            relationships = relationships,
            tags = loadModelTags(modelId),
            attributes = entityAttributes + relationshipAttributes
        )
    }


    private fun loadTypes(modelId: ModelId): List<ModelTypeInMemory> {
        return ModelTypeTable.selectAll().where { ModelTypeTable.modelId eq modelId.asString() }
            .orderBy(ModelTypeTable.key to SortOrder.ASC).map { row ->
                toType(ModelTypeRecord.read(row))
            }
    }


    private fun loadEntities(modelId: ModelId): List<EntityInMemory> {

        return EntityTable.selectAll().where { EntityTable.modelId eq modelId.asString() }
            .orderBy(EntityTable.key to SortOrder.ASC).map { row ->
                val record = EntityRecord.read(row)
                val tags = loadEntityTags(EntityId.fromString(record.id))
                toEntity(record, tags)
            }
    }

    private fun loadEntityAttributes(modelId: ModelId): List<AttributeInMemory> {
        return EntityAttributeTable.join(
            EntityTable,
            joinType = JoinType.INNER,
            onColumn = EntityAttributeTable.entityId,
            otherColumn = EntityTable.id
        ).selectAll()
            .where { EntityTable.modelId eq modelId.asString() }
            .map { row ->
                val record = EntityAttributeRecord.read(row)
                val tags = loadEntityAttributeTags(AttributeId.fromString(record.id))
                toEntityAttribute(record, tags)
            }
    }

    private fun loadRelationshipAttributes(modelId: ModelId): List<AttributeInMemory> {
        return RelationshipTable.join(
            RelationshipAttributeTable,
            joinType = JoinType.INNER,
            onColumn = RelationshipTable.id,
            otherColumn = RelationshipAttributeTable.relationshipId
        ).selectAll()
            .where { RelationshipTable.modelId eq modelId.asString() }
            .map { row ->
                val record = RelationshipAttributeRecord.read(row)
                val tags = loadRelationshipAttributeTags(AttributeId.fromString(record.id))
                toRelationshipAttribute(record, tags)
            }
    }

    private fun loadRelationships(modelId: ModelId): List<RelationshipInMemory> {
        val relationshipIds =
            RelationshipTable.select(RelationshipTable.id)
                .where { RelationshipTable.modelId eq modelId.asString() }

        val roleRowsByRelationshipId =
            RelationshipRoleTable.selectAll()
                .where { RelationshipRoleTable.relationshipId inSubQuery relationshipIds }
                .orderBy(RelationshipRoleTable.key to SortOrder.ASC).toList()
                .groupBy { it[RelationshipRoleTable.relationshipId] }

        return RelationshipTable.selectAll()
            .where { RelationshipTable.modelId eq modelId.asString() }
            .orderBy(RelationshipTable.key to SortOrder.ASC).map { row ->
                val relationshipRecord = RelationshipRecord.read(row)
                val relationshipId = RelationshipId.fromString(relationshipRecord.id)
                val roleRecords = (roleRowsByRelationshipId[relationshipId.asString()] ?: emptyList())
                    .map { RelationshipRoleRecord.read(it) }
                val tags = loadRelationshipTags(RelationshipId.fromString(relationshipRecord.id))
                toRelationship(relationshipRecord, roleRecords, tags)
            }
    }


    private fun loadModelTags(modelId: ModelId): List<TagId> {
        return ModelTagTable.selectAll().where { ModelTagTable.modelId eq modelId.asString() }
            .orderBy(ModelTagTable.tagId to SortOrder.ASC).map { Id.fromString(it[ModelTagTable.tagId], ::TagId) }
    }

    private fun loadEntityTags(entityId: EntityId): List<TagId> {
        return EntityTagTable.selectAll().where { EntityTagTable.entityId eq entityId.asString() }
            .orderBy(EntityTagTable.tagId to SortOrder.ASC).map { Id.fromString(it[EntityTagTable.tagId], ::TagId) }
    }

    private fun loadEntityAttributeTags(attributeId: AttributeId): List<TagId> {
        return EntityAttributeTagTable.selectAll()
            .where { EntityAttributeTagTable.attributeId eq attributeId.asString() }
            .orderBy(EntityAttributeTagTable.tagId to SortOrder.ASC)
            .map { Id.fromString(it[EntityAttributeTagTable.tagId], ::TagId) }
    }

    private fun loadRelationshipTags(relationshipId: RelationshipId): List<TagId> {
        return RelationshipTagTable.selectAll()
            .where { RelationshipTagTable.relationshipId eq relationshipId.asString() }
            .orderBy(RelationshipTagTable.tagId to SortOrder.ASC)
            .map { Id.fromString(it[RelationshipTagTable.tagId], ::TagId) }
    }

    private fun loadRelationshipAttributeTags(attributeId: AttributeId): List<TagId> {
        return RelationshipAttributeTagTable.selectAll()
            .where { RelationshipAttributeTagTable.attributeId eq attributeId.asString() }
            .orderBy(RelationshipAttributeTagTable.tagId to SortOrder.ASC)
            .map { Id.fromString(it[RelationshipAttributeTagTable.tagId], ::TagId) }
    }

    private fun localizedTextToString(value: LocalizedText?): String? {
        return value?.name
    }

    private fun localizedMarkdownToString(value: LocalizedMarkdown?): String? {
        return value?.name
    }

    private fun modelOriginToString(origin: ModelOrigin): String? {
        return when (origin) {
            is ModelOrigin.Manual -> null
            is ModelOrigin.Uri -> origin.uri.toString()
        }
    }

    private fun entityOriginToString(origin: EntityOrigin): String? {
        return when (origin) {
            is EntityOrigin.Manual -> null
            is EntityOrigin.Uri -> origin.uri.toString()
        }
    }


}
