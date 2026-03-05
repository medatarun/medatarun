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
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
            ModelTable.select(ModelTable.id).where { ModelTable.id eq id }.limit(1).any()
        }
    }

    override fun existsModelByKey(key: ModelKey): Boolean {
        return db.withExposed {
            ModelTable.select(ModelTable.id).where { ModelTable.key eq key }.limit(1).any()
        }
    }

    override fun findAllModelIds(): List<ModelId> {
        return db.withExposed {
            ModelTable.selectAll().map { it[ModelTable.id] }
        }
    }

    override fun findModelAggregateByKeyOptional(key: ModelKey): ModelAggregate? {
        return db.withExposed {
            val row = ModelTable.selectAll().where { ModelTable.key eq key }.singleOrNull()
            if (row == null) null else loadModelAggregate(row)
        }
    }

    override fun findModelAggregateByIdOptional(id: ModelId): ModelAggregate? {
        return db.withExposed {
            val row = ModelTable.selectAll().where { ModelTable.id eq id }.singleOrNull()
            if (row == null) null else loadModelAggregate(row)
        }
    }

    override fun findModelByKeyOptional(key: ModelKey): Model? {
        return db.withExposed {
            val row = ModelTable.selectAll().where { ModelTable.key eq key }.singleOrNull()
            if (row == null) null else toModel(ModelRecord.read(row))
        }
    }

    override fun findModelByIdOptional(id: ModelId): Model? {
        return db.withExposed {
            val row = ModelTable.selectAll().where { ModelTable.id eq id }.singleOrNull()
            if (row == null) null else toModel(ModelRecord.read(row))
        }
    }

    override fun findTypeByKeyOptional(
        modelId: ModelId, key: TypeKey
    ): ModelType? {
        return db.withExposed {
            ModelTypeTable.selectAll().where {
                (ModelTypeTable.modelId eq modelId) and (ModelTypeTable.key eq key)
            }.singleOrNull()?.let { row -> toType(ModelTypeRecord.read(row)) }
        }
    }

    override fun findTypeByIdOptional(
        modelId: ModelId, typeId: TypeId
    ): ModelType? {
        return db.withExposed {
            ModelTypeTable.selectAll().where {
                (ModelTypeTable.modelId eq modelId) and (ModelTypeTable.id eq typeId)
            }.singleOrNull()?.let { row -> toType(ModelTypeRecord.read(row)) }
        }
    }

    override fun findEntityByIdOptional(
        modelId: ModelId, entityId: EntityId
    ): Entity? {
        return db.withExposed {
            EntityTable.selectAll().where {
                (EntityTable.modelId eq modelId) and (EntityTable.id eq entityId)
            }.singleOrNull()?.let { row -> toEntity(EntityRecord.read(row), loadEntityTags(entityId)) }
        }
    }

    override fun findEntityByKeyOptional(
        modelId: ModelId, entityKey: EntityKey
    ): Entity? {
        return db.withExposed {
            EntityTable.selectAll().where {
                (EntityTable.modelId eq modelId) and (EntityTable.key eq entityKey)
            }.singleOrNull()?.let { row ->
                val record = EntityRecord.read(row)
                val tags = loadEntityTags(record.id)
                toEntity(record, tags)
            }
        }
    }

    override fun findEntityAttributeByIdOptional(
        modelId: ModelId, entityId: EntityId, attributeId: AttributeId
    ): Attribute? {
        return db.withExposed {
            EntityAttributeTable.join(
                EntityTable, JoinType.INNER, EntityAttributeTable.entityId, EntityTable.id
            ).selectAll()
                .where { (EntityTable.modelId eq modelId) and (EntityAttributeTable.entityId eq entityId) and (EntityAttributeTable.id eq attributeId) }
                .singleOrNull()?.let { row ->
                    val record = EntityAttributeRecord.read(row)
                    val tags = loadEntityAttributeTags(record.id)
                    toEntityAttribute(record, tags)
                }
        }
    }

    override fun findEntityAttributeByKeyOptional(
        modelId: ModelId, entityId: EntityId, key: AttributeKey
    ): Attribute? {
        return db.withExposed {
            EntityAttributeTable.join(
                EntityTable, JoinType.INNER, EntityAttributeTable.entityId, EntityTable.id
            ).selectAll()
                .where { (EntityTable.modelId eq modelId) and (EntityAttributeTable.entityId eq entityId) and (EntityAttributeTable.key eq key) }
                .singleOrNull()?.let { row ->
                    val record = EntityAttributeRecord.read(row)
                    val tags = loadEntityAttributeTags(record.id)
                    toEntityAttribute(record, tags)
                }
        }
    }

    override fun findRelationshipByIdOptional(modelId: ModelId, relationshipId: RelationshipId): Relationship? {
        return findRelationshipByOptional(modelId, RelationshipTable.id eq relationshipId)
    }

    override fun findRelationshipByKeyOptional(modelId: ModelId, relationshipKey: RelationshipKey): Relationship? {
        return findRelationshipByOptional(modelId, RelationshipTable.key eq relationshipKey)
    }


    private fun findRelationshipByOptional(modelId: ModelId, criterion: Expression<Boolean>): Relationship? {
        return db.withExposed {
            val roleRecords = RelationshipRoleTable.join(
                RelationshipTable,
                JoinType.INNER,
                onColumn = RelationshipRoleTable.relationshipId,
                otherColumn = RelationshipTable.id
            ).selectAll().where { (RelationshipTable.modelId eq modelId) and criterion }
                .map { RelationshipRoleRecord.read(it) }

            RelationshipTable.selectAll().where { (RelationshipTable.modelId eq modelId) and criterion }.singleOrNull()
                ?.let { row ->
                    val record = RelationshipRecord.read(row)
                    val tags = loadRelationshipTags(record.id)
                    toRelationship(record, roleRecords, tags)
                }
        }
    }

    override fun findRelationshipRoleByIdOptional(
        modelId: ModelId, relationshipId: RelationshipId, roleId: RelationshipRoleId
    ): RelationshipRole? {
        return findRelationshipRoleByOptional(modelId, relationshipId, RelationshipRoleTable.id eq roleId)
    }

    override fun findRelationshipRoleByKeyOptional(
        modelId: ModelId, relationshipId: RelationshipId, roleKey: RelationshipRoleKey
    ): RelationshipRole? {
        return findRelationshipRoleByOptional(modelId, relationshipId, RelationshipRoleTable.key eq roleKey)
    }

    private fun findRelationshipRoleByOptional(
        modelId: ModelId, relationshipId: RelationshipId, criterion: Op<Boolean>
    ): RelationshipRole? {
        return RelationshipRoleTable.join(
            RelationshipTable,
            JoinType.INNER,
            onColumn = RelationshipRoleTable.relationshipId,
            otherColumn = RelationshipTable.id
        ).selectAll().where {
            (RelationshipTable.modelId eq modelId) and (RelationshipTable.id eq relationshipId) and criterion
        }.singleOrNull()?.let { toRelationshipRole(RelationshipRoleRecord.read(it)) }
    }


    override fun findRelationshipAttributeByIdOptional(
        modelId: ModelId, relationshipId: RelationshipId, attributeId: AttributeId
    ): Attribute? {
        return findRelationshipAttributeByOptional(
            modelId, relationshipId, RelationshipAttributeTable.id eq attributeId
        )
    }

    override fun findRelationshipAttributeByKeyOptional(
        modelId: ModelId, relationshipId: RelationshipId, key: AttributeKey
    ): Attribute? {
        return findRelationshipAttributeByOptional(
            modelId, relationshipId, RelationshipAttributeTable.key eq key
        )
    }


    fun findRelationshipAttributeByOptional(
        modelId: ModelId, relationshipId: RelationshipId, criterion: Expression<Boolean>
    ): Attribute? {
        return db.withExposed {
            RelationshipAttributeTable.join(
                RelationshipTable, JoinType.INNER, RelationshipAttributeTable.relationshipId, RelationshipTable.id
            ).selectAll()
                .where { (RelationshipTable.modelId eq modelId) and (RelationshipAttributeTable.relationshipId eq relationshipId) and criterion }
                .singleOrNull()?.let { row ->
                    val record = RelationshipAttributeRecord.read(row)
                    val tags = loadRelationshipAttributeTags(record.id)
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
        modelId: ModelId, typeId: TypeId
    ): Boolean {
        return db.withExposed {
            EntityAttributeTable.join(
                EntityTable, JoinType.INNER, onColumn = EntityAttributeTable.entityId, otherColumn = EntityTable.id
            ).selectAll().where {
                (EntityAttributeTable.typeId eq typeId) and (EntityTable.modelId eq modelId)
            }.any()
        }
    }

    override fun isTypeUsedInRelationshipAttributes(
        modelId: ModelId, typeId: TypeId
    ): Boolean {
        return db.withExposed {
            RelationshipAttributeTable.join(
                RelationshipTable,
                JoinType.INNER,
                onColumn = RelationshipAttributeTable.relationshipId,
                otherColumn = RelationshipTable.id
            ).selectAll().where {
                (RelationshipAttributeTable.typeId eq typeId) and (RelationshipTable.modelId eq modelId)
            }.any()
        }

    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    override fun dispatch(cmd: ModelRepoCmd) {
        db.withExposed { dispatchExposed(cmd) }
    }

    private fun dispatchExposed(cmd: ModelRepoCmd) {
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


    // Model
    // ------------------------------------------------------------------------

    private fun loadModelAggregate(row: ResultRow): ModelAggregateInMemory {
        val record = ModelRecord.read(row)
        val types = loadTypes(record.id)
        val entities = loadEntities(record.id)
        val entityAttributes = loadEntityAttributes(record.id)
        val relationships = loadRelationships(record.id)
        val relationshipAttributes = loadRelationshipAttributes(record.id)

        return ModelAggregateInMemory(
            model = toModel(record),
            types = types,
            entities = entities,
            relationships = relationships,
            tags = loadModelTags(record.id),
            attributes = entityAttributes + relationshipAttributes
        )
    }

    private fun loadModelTags(modelId: ModelId): List<TagId> {
        return ModelTagTable.selectAll().where { ModelTagTable.modelId eq modelId }
            .orderBy(ModelTagTable.tagId to SortOrder.ASC).map { it[ModelTagTable.tagId] }
    }

    private fun modelOriginToString(origin: ModelOrigin): String? {
        return when (origin) {
            is ModelOrigin.Manual -> null
            is ModelOrigin.Uri -> origin.uri.toString()
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

        logger.warn("Storing full aggregate {}", model)

        val inMemoryModel = ModelInMemory.of(model)

        insertModel(model.model)
        insertModelTags(model.id, model.tags)

        for (type in model.types) {
            insertType(
                ModelTypeRecord(
                    id = type.id,
                    modelId = model.id,
                    key = type.key,
                    name = type.name,
                    description = type.description
                )
            )
        }

        for (entity in model.entities) {
            insertEntity(
                EntityRecord(
                    id = entity.id,
                    modelId = model.id,
                    key = entity.key,
                    name = entity.name,
                    description = entity.description,
                    identifierAttributeId = entity.identifierAttributeId,
                    origin = entityOriginToString(entity.origin),
                    documentationHome = entity.documentationHome?.toExternalForm(),
                )
            )
            insertEntityTags(entity.id, entity.tags)
            searchWrite.upsertEntitySearchItem(entity.id)

            for (attr in model.attributes.filter { it.ownedBy(entity.id) }) {
                insertEntityAttribute(
                    EntityAttributeRecord(
                        id = attr.id,
                        entityId = entity.id,
                        key = attr.key,
                        name = attr.name,
                        description = attr.description,
                        typeId = attr.typeId,
                        optional = attr.optional
                    )
                )
                insertEntityAttributeTags(attr.id, attr.tags)
                searchWrite.upsertEntityAttributeSearchItem(attr.id)
            }
        }

        for (relationship in model.relationships) {
            insertRelationship(
                record = RelationshipRecord(
                    id = relationship.id,
                    modelId = model.id,
                    key = relationship.key,
                    name = relationship.name,
                    description = relationship.description
                ),
                roles = relationship.roles.map { role ->
                    RelationshipRoleRecord(
                        id = role.id,
                        relationshipId = relationship.id,
                        key = role.key,
                        entityId = role.entityId,
                        name = role.name,
                        cardinality = role.cardinality.code
                    )
                }
            )
            insertRelationshipTags(relationship.id, relationship.tags)
            searchWrite.upsertRelationshipSearchItem(relationship.id)

            for (attr in model.attributes.filter { it.ownedBy(relationship.id) }) {
                insertRelationshipAttribute(
                    RelationshipAttributeRecord(
                        id = attr.id,
                        relationshipId = relationship.id,
                        key = attr.key,
                        name = attr.name,
                        description = attr.description,
                        typeId = attr.typeId,
                        optional = attr.optional
                    )
                )
                insertRelationshipAttributeTags(attr.id, attr.tags)
                searchWrite.upsertRelationshipAttributeSearchItem(attr.id)
            }
        }

        searchWrite.upsertModelSearchItem(inMemoryModel.id)

    }

    private fun deleteModel(modelId: ModelId) {
        searchWrite.deleteModelBranch(modelId)
        ModelTable.deleteWhere { id eq modelId }
    }

    private fun updateModelName(modelId: ModelId, name: LocalizedText) {
        ModelTable.update(where = { ModelTable.id eq modelId }) { row ->
            row[ModelTable.name] = name
        }
        searchWrite.upsertModelSearchItem(modelId)
    }

    private fun updateModelDescription(modelId: ModelId, description: LocalizedMarkdown?) {
        ModelTable.update(where = { ModelTable.id eq modelId }) { row ->
            row[ModelTable.description] = description
        }
        searchWrite.upsertModelSearchItem(modelId)
    }

    private fun updateModelVersion(modelId: ModelId, version: ModelVersion) {
        ModelTable.update(where = { ModelTable.id eq modelId }) { row ->
            row[ModelTable.version] = version.value
        }
    }

    private fun updateModelDocumentationHome(modelId: ModelId, documentationHome: java.net.URL?) {
        ModelTable.update(where = { ModelTable.id eq modelId }) { row ->
            row[ModelTable.documentationHome] = documentationHome?.toExternalForm()
        }
    }

    private fun addModelTag(modelId: ModelId, tagId: TagId) {
        val exists = ModelTagTable.select(ModelTagTable.modelId).where {
            (ModelTagTable.modelId eq modelId) and (ModelTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            ModelTagTable.insert { row ->
                row[ModelTagTable.modelId] = modelId
                row[ModelTagTable.tagId] = tagId
            }
        }
        searchWrite.upsertModelSearchItem(modelId)
    }

    private fun deleteModelTag(modelId: ModelId, tagId: TagId) {
        ModelTagTable.deleteWhere {
            (ModelTagTable.modelId eq modelId) and (ModelTagTable.tagId eq tagId)
        }
        searchWrite.upsertModelSearchItem(modelId)
    }

    private fun insertModel(model: Model) {
        ModelTable.insert { row ->
            row[ModelTable.id] = model.id
            row[ModelTable.key] = model.key
            row[ModelTable.name] = model.name
            row[ModelTable.description] = model.description
            row[ModelTable.version] = model.version.value
            row[ModelTable.origin] = modelOriginToString(model.origin)
            row[ModelTable.documentationHome] = model.documentationHome?.toExternalForm()
        }
    }

    private fun insertModelTags(modelId: ModelId, tags: List<TagId>) {
        for (tagId in tags) {
            ModelTagTable.insert { row ->
                row[ModelTagTable.modelId] = modelId
                row[ModelTagTable.tagId] = tagId
            }
        }
    }


    // Types
    // ------------------------------------------------------------------------

    private fun loadTypes(modelId: ModelId): List<ModelTypeInMemory> {
        return ModelTypeTable.selectAll().where { ModelTypeTable.modelId eq modelId }
            .orderBy(ModelTypeTable.key to SortOrder.ASC).map { row ->
                toType(ModelTypeRecord.read(row))
            }
    }

    private fun createType(modelId: ModelId, initializer: ModelTypeInitializer) {
        val record = ModelTypeRecord(
            id = TypeId.generate(),
            modelId = modelId,
            key = initializer.key,
            name = initializer.name,
            description = initializer.description
        )
        insertType(record)
    }

    private fun insertType(record: ModelTypeRecord) {
        ModelTypeTable.insert { row ->
            row[ModelTypeTable.id] = record.id
            row[ModelTypeTable.modelId] = record.modelId
            row[ModelTypeTable.key] = record.key
            row[ModelTypeTable.name] = record.name
            row[ModelTypeTable.description] = record.description
        }
    }

    private fun updateTypeKey(modelId: ModelId, typeId: TypeId, value: TypeKey) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.id eq typeId) and (ModelTypeTable.modelId eq modelId)
            }) { row ->
            row[ModelTypeTable.key] = value
        }
    }

    private fun updateTypeName(modelId: ModelId, typeId: TypeId, value: LocalizedText?) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.id eq typeId) and (ModelTypeTable.modelId eq modelId)
            }) { row ->
            row[ModelTypeTable.name] = value
        }
    }

    private fun updateTypeDescription(modelId: ModelId, typeId: TypeId, value: LocalizedMarkdown?) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.id eq typeId) and (ModelTypeTable.modelId eq modelId)
            }) { row ->
            row[ModelTypeTable.description] = value
        }
    }

    private fun deleteType(modelId: ModelId, typeId: TypeId) {
        ModelTypeTable.deleteWhere {
            (ModelTypeTable.id eq typeId) and (ModelTypeTable.modelId eq modelId)
        }
    }

    // Entity
    // ------------------------------------------------------------------------

    private fun loadEntities(modelId: ModelId): List<EntityInMemory> {

        return EntityTable.selectAll().where { EntityTable.modelId eq modelId }
            .orderBy(EntityTable.key to SortOrder.ASC).map { row ->
                val record = EntityRecord.read(row)
                val tags = loadEntityTags(record.id)
                toEntity(record, tags)
            }
    }

    private fun loadEntityTags(entityId: EntityId): List<TagId> {
        return EntityTagTable.selectAll().where { EntityTagTable.entityId eq entityId }
            .orderBy(EntityTagTable.tagId to SortOrder.ASC).map { it[EntityTagTable.tagId] }
    }

    private fun entityOriginToString(origin: EntityOrigin): String? {
        return when (origin) {
            is EntityOrigin.Manual -> null
            is EntityOrigin.Uri -> origin.uri.toString()
        }
    }

    private fun createEntity(cmd: ModelRepoCmd.CreateEntity) {
        insertEntity(cmd)
        searchWrite.upsertEntitySearchItem(cmd.entityId)
    }

    private fun updateEntityKey(modelId: ModelId, entityId: EntityId, value: EntityKey) {
        EntityTable.update(
            where = {
                (EntityTable.id eq entityId) and (EntityTable.modelId eq modelId)
            }) { row ->
            row[EntityTable.key] = value
        }
        searchWrite.upsertEntitySearchItem(entityId)
    }

    private fun updateEntityName(modelId: ModelId, entityId: EntityId, value: LocalizedText?) {
        EntityTable.update(
            where = {
                (EntityTable.id eq entityId) and (EntityTable.modelId eq modelId)
            }) { row ->
            row[EntityTable.name] = value
        }
        searchWrite.upsertEntitySearchItem(entityId)
    }

    private fun updateEntityDescription(modelId: ModelId, entityId: EntityId, value: LocalizedMarkdown?) {
        EntityTable.update(
            where = {
                (EntityTable.id eq entityId) and (EntityTable.modelId eq modelId)
            }) { row ->
            row[EntityTable.description] = value
        }
        searchWrite.upsertEntitySearchItem(entityId)
    }

    private fun updateEntityIdentifierAttribute(modelId: ModelId, entityId: EntityId, value: AttributeId) {
        EntityTable.update(
            where = {
                (EntityTable.id eq entityId) and (EntityTable.modelId eq modelId)
            }) { row ->
            row[EntityTable.identifierAttributeId] = value
        }
    }

    private fun updateEntityDocumentationHome(modelId: ModelId, entityId: EntityId, value: java.net.URL?) {
        EntityTable.update(
            where = {
                (EntityTable.id eq entityId) and (EntityTable.modelId eq modelId)
            }) { row ->
            row[EntityTable.documentationHome] = value?.toExternalForm()
        }
    }

    private fun addEntityTag(entityId: EntityId, tagId: TagId) {
        val exists = EntityTagTable.select(EntityTagTable.entityId).where {
            (EntityTagTable.entityId eq entityId) and (EntityTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertEntityTag(entityId, tagId)
        }
        searchWrite.upsertEntitySearchItem(entityId)
    }

    private fun insertEntityTags(entityId: EntityId, tags: List<TagId>) {
        for (tag in tags) {
            insertEntityTag(entityId, tag)
        }
    }

    private fun insertEntityTag(entityId: EntityId, tagId: TagId) {
        EntityTagTable.insert { row ->
            row[EntityTagTable.entityId] = entityId
            row[EntityTagTable.tagId] = tagId
        }
    }

    private fun deleteEntityTag(entityId: EntityId, tagId: TagId) {
        EntityTagTable.deleteWhere {
            (EntityTagTable.entityId eq entityId) and (EntityTagTable.tagId eq tagId)
        }
        searchWrite.upsertEntitySearchItem(entityId)
    }

    private fun deleteEntity(modelId: ModelId, entityId: EntityId) {
        searchWrite.deleteEntityBranch(entityId)
        EntityTable.deleteWhere {
            (EntityTable.id eq entityId) and (EntityTable.modelId eq modelId)
        }
    }

    private fun insertEntity(cmd: ModelRepoCmd.CreateEntity) {

        val record = EntityRecord(
            id = cmd.entityId,
            modelId = cmd.modelId,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description,
            identifierAttributeId = cmd.identityAttributeId,
            origin = entityOriginToString(cmd.origin),
            documentationHome = cmd.documentationHome?.toExternalForm()
        )

        insertEntity(record)

        insertEntityAttribute(
            EntityAttributeRecord(
                id = cmd.identityAttributeId,
                entityId = cmd.entityId,
                key = cmd.identityAttributeKey,
                name = cmd.identityAttributeName,
                description = cmd.identityAttributeDescription,
                typeId = cmd.identityAttributeTypeId,
                optional = cmd.identityAttributeIdOptional
            )
        )
    }

    private fun insertEntity(record: EntityRecord) {
        EntityTable.insert { row ->
            row[EntityTable.id] = record.id
            row[EntityTable.modelId] = record.modelId
            row[EntityTable.key] = record.key
            row[EntityTable.name] = record.name
            row[EntityTable.description] = record.description
            row[EntityTable.identifierAttributeId] = record.identifierAttributeId
            row[EntityTable.origin] = record.origin
            row[EntityTable.documentationHome] = record.documentationHome
        }
    }

    // Entity attribute
    // ------------------------------------------------------------------------


    private fun loadEntityAttributes(modelId: ModelId): List<AttributeInMemory> {
        return EntityAttributeTable.join(
            EntityTable,
            joinType = JoinType.INNER,
            onColumn = EntityAttributeTable.entityId,
            otherColumn = EntityTable.id
        ).selectAll().where { EntityTable.modelId eq modelId }.map { row ->
            val record = EntityAttributeRecord.read(row)
            val tags = loadEntityAttributeTags(record.id)
            toEntityAttribute(record, tags)
        }
    }

    private fun loadEntityAttributeTags(attributeId: AttributeId): List<TagId> {
        return EntityAttributeTagTable.selectAll().where { EntityAttributeTagTable.attributeId eq attributeId }
            .orderBy(EntityAttributeTagTable.tagId to SortOrder.ASC).map { it[EntityAttributeTagTable.tagId] }
    }

    private fun createEntityAttribute(cmd: ModelRepoCmd.CreateEntityAttribute) {
        insertEntityAttribute(
            EntityAttributeRecord(
                id = cmd.attributeId,
                entityId = cmd.entityId,
                key = cmd.key,
                name = cmd.name,
                description = cmd.description,
                typeId = cmd.typeId,
                optional = cmd.optional
            )
        )
    }

    private fun updateEntityAttributeKey(entityId: EntityId, attributeId: AttributeId, value: AttributeKey) {
        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.id eq attributeId) and (EntityAttributeTable.entityId eq entityId)
            }) { row ->
            row[EntityAttributeTable.key] = value
        }
        searchWrite.upsertEntityAttributeSearchItem(attributeId)
    }

    private fun updateEntityAttributeName(entityId: EntityId, attributeId: AttributeId, value: LocalizedText?) {
        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.id eq attributeId) and (EntityAttributeTable.entityId eq entityId)
            }) { row ->
            row[EntityAttributeTable.name] = value
        }
        searchWrite.upsertEntityAttributeSearchItem(attributeId)
    }

    private fun updateEntityAttributeDescription(
        entityId: EntityId, attributeId: AttributeId, value: LocalizedMarkdown?
    ) {

        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.id eq attributeId) and (EntityAttributeTable.entityId eq entityId)
            }) { row ->
            row[EntityAttributeTable.description] = value
        }
        searchWrite.upsertEntityAttributeSearchItem(attributeId)

    }

    private fun updateEntityAttributeType(entityId: EntityId, attributeId: AttributeId, value: TypeId) {

        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.id eq attributeId) and (EntityAttributeTable.entityId eq entityId)
            }) { row ->
            row[EntityAttributeTable.typeId] = value
        }

    }

    private fun updateEntityAttributeOptional(entityId: EntityId, attributeId: AttributeId, value: Boolean) {
        EntityAttributeTable.update(
            where = {
                (EntityAttributeTable.id eq attributeId) and (EntityAttributeTable.entityId eq entityId)
            }) { row ->
            row[EntityAttributeTable.optional] = value
        }
    }


    private fun insertEntityAttribute(
        record: EntityAttributeRecord

    ) {
        EntityAttributeTable.insert { row ->
            row[EntityAttributeTable.id] = record.id
            row[EntityAttributeTable.entityId] = record.entityId
            row[EntityAttributeTable.key] = record.key
            row[EntityAttributeTable.name] = record.name
            row[EntityAttributeTable.description] = record.description
            row[EntityAttributeTable.typeId] = record.typeId
            row[EntityAttributeTable.optional] = record.optional
        }
        searchWrite.upsertEntityAttributeSearchItem(record.id)
    }


    private fun addEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        val exists = EntityAttributeTagTable.select(EntityAttributeTagTable.attributeId).where {
            (EntityAttributeTagTable.attributeId eq attributeId) and (EntityAttributeTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertEntityAttributeTag(attributeId, tagId)
        }
        searchWrite.upsertEntityAttributeSearchItem(attributeId)
    }

    private fun insertEntityAttributeTags(attributeId: AttributeId, tags: List<TagId>) {
        for (tag in tags) {
            insertEntityAttributeTag(attributeId, tag)
        }
    }

    private fun insertEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        EntityAttributeTagTable.insert { row ->
            row[EntityAttributeTagTable.attributeId] = attributeId
            row[EntityAttributeTagTable.tagId] = tagId
        }
    }

    private fun deleteEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        EntityAttributeTagTable.deleteWhere {
            (EntityAttributeTagTable.attributeId eq attributeId) and (EntityAttributeTagTable.tagId eq tagId)
        }
        searchWrite.upsertEntityAttributeSearchItem(attributeId)
    }

    private fun deleteEntityAttribute(entityId: EntityId, attributeId: AttributeId) {
        searchWrite.deleteEntityAttributeSearchItem(attributeId)
        EntityAttributeTable.deleteWhere {
            (EntityAttributeTable.id eq attributeId) and (EntityAttributeTable.entityId eq entityId)
        }
    }


    // Relationship
    // ------------------------------------------------------------------------


    private fun loadRelationships(modelId: ModelId): List<RelationshipInMemory> {
        val relationshipIds =
            RelationshipTable.select(RelationshipTable.id).where { RelationshipTable.modelId eq modelId }

        val roleRowsByRelationshipId =
            RelationshipRoleTable.selectAll().where { RelationshipRoleTable.relationshipId inSubQuery relationshipIds }
                .orderBy(RelationshipRoleTable.key to SortOrder.ASC).toList()
                .groupBy { it[RelationshipRoleTable.relationshipId] }

        return RelationshipTable.selectAll().where { RelationshipTable.modelId eq modelId }
            .orderBy(RelationshipTable.key to SortOrder.ASC).map { row ->
                val relationshipRecord = RelationshipRecord.read(row)
                val relationshipId = relationshipRecord.id
                val roleRecords =
                    (roleRowsByRelationshipId[relationshipId] ?: emptyList()).map { RelationshipRoleRecord.read(it) }
                val tags = loadRelationshipTags(relationshipRecord.id)
                toRelationship(relationshipRecord, roleRecords, tags)
            }
    }

    private fun loadRelationshipTags(relationshipId: RelationshipId): List<TagId> {
        return RelationshipTagTable.selectAll().where { RelationshipTagTable.relationshipId eq relationshipId }
            .orderBy(RelationshipTagTable.tagId to SortOrder.ASC).map { it[RelationshipTagTable.tagId] }
    }

    private fun createRelationship(cmd: ModelRepoCmd.CreateRelationship) {
        val record = RelationshipRecord(
            id = cmd.relationshipId,
            modelId = cmd.modelId,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description
        )
        val roles = cmd.roles.map { role ->
            RelationshipRoleRecord(
                id = role.id,
                relationshipId = cmd.relationshipId,
                key = role.key,
                name = role.name,
                entityId = role.entityId,
                cardinality = role.cardinality.code
            )
        }
        insertRelationship(record, roles)
        searchWrite.upsertRelationshipSearchItem(cmd.relationshipId)
    }

    private fun updateRelationshipKey(relationshipId: RelationshipId, value: RelationshipKey) {
        RelationshipTable.update(where = { RelationshipTable.id eq relationshipId }) { row ->
            row[RelationshipTable.key] = value
        }
        searchWrite.upsertRelationshipSearchItem(relationshipId)
    }

    private fun updateRelationshipName(relationshipId: RelationshipId, value: LocalizedText?) {
        RelationshipTable.update(where = { RelationshipTable.id eq relationshipId }) { row ->
            row[RelationshipTable.name] = value
        }
        searchWrite.upsertRelationshipSearchItem(relationshipId)
    }

    private fun updateRelationshipDescription(relationshipId: RelationshipId, value: LocalizedMarkdown?) {
        RelationshipTable.update(where = { RelationshipTable.id eq relationshipId }) { row ->
            row[RelationshipTable.description] = value
        }
        searchWrite.upsertRelationshipSearchItem(relationshipId)
    }

    private fun updateRelationshipRoleKey(relationshipRoleId: RelationshipRoleId, value: RelationshipRoleKey) {
        RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq relationshipRoleId }) { row ->
            row[RelationshipRoleTable.key] = value
        }
    }

    private fun updateRelationshipRoleName(relationshipRoleId: RelationshipRoleId, value: LocalizedText?) {
        RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq relationshipRoleId }) { row ->
            row[RelationshipRoleTable.name] = value
        }
    }

    private fun updateRelationshipRoleEntity(relationshipRoleId: RelationshipRoleId, value: EntityId) {
        RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq relationshipRoleId }) { row ->
            row[RelationshipRoleTable.entityId] = value
        }
    }

    private fun updateRelationshipRoleCardinality(
        relationshipRoleId: RelationshipRoleId, value: RelationshipCardinality
    ) {
        RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq relationshipRoleId }) { row ->
            row[RelationshipRoleTable.cardinality] = value.code
        }
    }

    private fun addRelationshipTag(relationshipId: RelationshipId, tagId: TagId) {
        val exists = RelationshipTagTable.select(RelationshipTagTable.relationshipId).where {
            (RelationshipTagTable.relationshipId eq relationshipId) and (RelationshipTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertRelationshipTag(relationshipId, tagId)
        }
        searchWrite.upsertRelationshipSearchItem(relationshipId)
    }

    private fun insertRelationshipTags(
        relationshipId: RelationshipId,
        tags: List<TagId>
    ) {
        for (tag in tags) {
            insertRelationshipTag(relationshipId, tag)
        }
    }

    private fun insertRelationshipTag(
        relationshipId: RelationshipId,
        tagId: TagId
    ) {
        RelationshipTagTable.insert { row ->
            row[RelationshipTagTable.relationshipId] = relationshipId
            row[RelationshipTagTable.tagId] = tagId
        }
    }

    private fun insertRelationship(record: RelationshipRecord, roles: List<RelationshipRoleRecord>) {

        RelationshipTable.insert { row ->
            row[RelationshipTable.id] = record.id
            row[RelationshipTable.modelId] = record.modelId
            row[RelationshipTable.key] = record.key
            row[RelationshipTable.name] = record.name
            row[RelationshipTable.description] = record.description
        }
        for (roleRecord in roles) {
            RelationshipRoleTable.insert { row ->
                row[RelationshipRoleTable.id] = roleRecord.id
                row[RelationshipRoleTable.relationshipId] = roleRecord.relationshipId
                row[RelationshipRoleTable.key] = roleRecord.key
                row[RelationshipRoleTable.entityId] = roleRecord.entityId
                row[RelationshipRoleTable.name] = roleRecord.name
                row[RelationshipRoleTable.cardinality] = roleRecord.cardinality
            }
        }


    }

    private fun deleteRelationship(modelId: ModelId, relationshipId: RelationshipId) {
        searchWrite.deleteRelationshipBranch(relationshipId)
        RelationshipTable.deleteWhere {
            (RelationshipTable.id eq relationshipId) and (RelationshipTable.modelId eq modelId)
        }
    }

    private fun deleteRelationshipTag(relationshipId: RelationshipId, tagId: TagId) {
        RelationshipTagTable.deleteWhere {
            (RelationshipTagTable.relationshipId eq relationshipId) and (RelationshipTagTable.tagId eq tagId)
        }
        searchWrite.upsertRelationshipSearchItem(relationshipId)
    }
    // Relationship attribute
    // ------------------------------------------------------------------------

    private fun loadRelationshipAttributes(modelId: ModelId): List<AttributeInMemory> {
        return RelationshipTable.join(
            RelationshipAttributeTable,
            joinType = JoinType.INNER,
            onColumn = RelationshipTable.id,
            otherColumn = RelationshipAttributeTable.relationshipId
        ).selectAll().where { RelationshipTable.modelId eq modelId }.map { row ->
            val record = RelationshipAttributeRecord.read(row)
            val tags = loadRelationshipAttributeTags(record.id)
            toRelationshipAttribute(record, tags)
        }
    }

    private fun loadRelationshipAttributeTags(attributeId: AttributeId): List<TagId> {
        return RelationshipAttributeTagTable.selectAll()
            .where { RelationshipAttributeTagTable.attributeId eq attributeId }
            .orderBy(RelationshipAttributeTagTable.tagId to SortOrder.ASC)
            .map { it[RelationshipAttributeTagTable.tagId] }
    }

    private fun createRelationshipAttribute(cmd: ModelRepoCmd.CreateRelationshipAttribute) {
        val record = RelationshipAttributeRecord(
            id = cmd.attributeId,
            relationshipId = cmd.relationshipId,
            name = cmd.name,
            key = cmd.key,
            description = cmd.description,
            typeId = cmd.typeId,
            optional = cmd.optional
        )
        insertRelationshipAttribute(record)
        searchWrite.upsertRelationshipAttributeSearchItem(record.id)
    }

    private fun updateRelationshipAttributeKey(
        relationshipId: RelationshipId, attributeId: AttributeId, value: AttributeKey
    ) {
        RelationshipAttributeTable.update(where = {
            (RelationshipAttributeTable.id eq attributeId) and (RelationshipAttributeTable.relationshipId eq relationshipId)
        }) { row ->
            row[RelationshipAttributeTable.key] = value
        }
        searchWrite.upsertRelationshipAttributeSearchItem(attributeId)

    }

    private fun updateRelationshipAttributeName(
        relationshipId: RelationshipId, attributeId: AttributeId, value: LocalizedText?
    ) {
        RelationshipAttributeTable.update(
            where = {
                (RelationshipAttributeTable.id eq attributeId) and (RelationshipAttributeTable.relationshipId eq relationshipId)
            }) { row ->
            row[RelationshipAttributeTable.name] = value
        }
        searchWrite.upsertRelationshipAttributeSearchItem(attributeId)
    }

    private fun updateRelationshipAttributeDescription(
        relationshipId: RelationshipId, attributeId: AttributeId, value: LocalizedMarkdown?
    ) {
        RelationshipAttributeTable.update(
            where = {
                (RelationshipAttributeTable.id eq attributeId) and (RelationshipAttributeTable.relationshipId eq relationshipId)
            }) { row ->
            row[RelationshipAttributeTable.description] = value
        }
        searchWrite.upsertRelationshipAttributeSearchItem(attributeId)
    }

    private fun updateRelationshipAttributeType(
        relationshipId: RelationshipId, attributeId: AttributeId, value: TypeId
    ) {
        RelationshipAttributeTable.update(
            where = {
                (RelationshipAttributeTable.id eq attributeId) and (RelationshipAttributeTable.relationshipId eq relationshipId)
            }) { row ->
            row[RelationshipAttributeTable.typeId] = value
        }
    }

    private fun updateRelationshipAttributeOptional(
        relationshipId: RelationshipId, attributeId: AttributeId, value: Boolean
    ) {
        RelationshipAttributeTable.update(
            where = {
                (RelationshipAttributeTable.id eq attributeId) and (RelationshipAttributeTable.relationshipId eq relationshipId)
            }) { row ->
            row[RelationshipAttributeTable.optional] = value
        }
    }

    private fun addRelationshipAttributeTag(attributeId: AttributeId, tagId: TagId) {
        val exists = RelationshipAttributeTagTable.select(RelationshipAttributeTagTable.attributeId).where {
            (RelationshipAttributeTagTable.attributeId eq attributeId) and (RelationshipAttributeTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertRelationshipAttributeTag(attributeId, tagId)
        }
        searchWrite.upsertRelationshipAttributeSearchItem(attributeId)
    }

    private fun insertRelationshipAttributeTags(attributeId: AttributeId, tags: List<TagId>) {
        for (tag in tags) {
            insertRelationshipAttributeTag(attributeId, tag)
        }
    }
    private fun insertRelationshipAttributeTag(attributeId: AttributeId, tagId: TagId) {
        RelationshipAttributeTagTable.insert { row ->
            row[RelationshipAttributeTagTable.attributeId] = attributeId
            row[RelationshipAttributeTagTable.tagId] = tagId
        }
    }

    private fun insertRelationshipAttribute(
        record: RelationshipAttributeRecord
    ) {
        RelationshipAttributeTable.insert { row ->
            row[RelationshipAttributeTable.id] = record.id
            row[RelationshipAttributeTable.relationshipId] = record.relationshipId
            row[RelationshipAttributeTable.key] = record.key
            row[RelationshipAttributeTable.name] = record.name
            row[RelationshipAttributeTable.description] = record.description
            row[RelationshipAttributeTable.typeId] = record.typeId
            row[RelationshipAttributeTable.optional] = record.optional
        }

    }

    private fun deleteRelationshipAttribute(relationshipId: RelationshipId, attributeId: AttributeId) {
        searchWrite.deleteRelationshipAttributeSearchItem(attributeId)
        RelationshipAttributeTable.deleteWhere {
            (RelationshipAttributeTable.id eq attributeId) and (RelationshipAttributeTable.relationshipId eq relationshipId)
        }
    }

    private fun deleteRelationshipAttributeTag(attributeId: AttributeId, tagId: TagId) {
        RelationshipAttributeTagTable.deleteWhere {
            (RelationshipAttributeTagTable.attributeId eq attributeId) and (RelationshipAttributeTagTable.tagId eq tagId)
        }
        searchWrite.upsertRelationshipAttributeSearchItem(attributeId)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ModelStorageDb::class.java)
    }


}
