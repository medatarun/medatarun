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
import io.medatarun.model.infra.db.events.ModelEventStreamNumberContext
import io.medatarun.model.infra.db.events.ModelEventSystem
import io.medatarun.model.infra.db.records.*
import io.medatarun.model.infra.db.tables.*
import io.medatarun.model.infra.inmemory.ModelInMemory
import io.medatarun.model.ports.needs.*
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.tags.core.domain.TagId
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ModelStorageDb(
    private val db: DbConnectionFactory,
    private val clock: ModelClock
) : ModelStorage {

    private val searchRead = ModelStorageDbSearchRead(db)
    private val searchWrite = ModelStorageDbSearchWrite(db)
    private val eventSystem = ModelEventSystem()
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
            ModelSnapshotTable.select(ModelSnapshotTable.id).where {
                (ModelSnapshotTable.snapshotKind eq CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.key eq key)
            }.limit(1).any()
        }
    }

    override fun findAllModelIds(): List<ModelId> {
        return db.withExposed {
            ModelTable.selectAll().map { it[ModelTable.id] }
        }
    }

    override fun findModelAggregateByKeyOptional(key: ModelKey): ModelAggregate? {
        return db.withExposed {
            val row = ModelSnapshotTable.selectAll().where {
                (ModelSnapshotTable.snapshotKind eq CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.key eq key)
            }.singleOrNull()
            if (row == null) null else loadModelAggregate(row)
        }
    }

    override fun findModelAggregateByIdOptional(id: ModelId): ModelAggregate? {
        return db.withExposed {
            val row = ModelSnapshotTable.selectAll().where {
                (ModelSnapshotTable.snapshotKind eq CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.modelId eq id)
            }.singleOrNull()
            if (row == null) null else loadModelAggregate(row)
        }
    }

    fun findAllModelEvents(modelId: ModelId): List<ModelEventRecord> {
        return db.withExposed {
            ModelEventTable.selectAll()
                .where { ModelEventTable.modelId eq modelId }
                .orderBy(ModelEventTable.streamRevision to SortOrder.ASC)
                .map(ModelEventRecord::read)
        }
    }

    override fun findModelByKeyOptional(key: ModelKey): Model? {
        return db.withExposed {
            val row = ModelSnapshotTable.selectAll().where {
                (ModelSnapshotTable.snapshotKind eq CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.key eq key)
            }.singleOrNull()
            if (row == null) null else toModel(ModelRecord.read(row))
        }
    }

    override fun findModelByIdOptional(id: ModelId): Model? {
        return db.withExposed {
            val row = ModelSnapshotTable.selectAll().where {
                (ModelSnapshotTable.snapshotKind eq CURRENT_HEAD_SNAPSHOT_KIND) and (ModelSnapshotTable.modelId eq id)
            }.singleOrNull()
            if (row == null) null else toModel(ModelRecord.read(row))
        }
    }

    override fun findTypeByKeyOptional(
        modelId: ModelId, key: TypeKey
    ): ModelType? {
        return db.withExposed {
            ModelTypeTable.selectAll().where {
                (ModelTypeTable.modelSnapshotId eq modelId) and (ModelTypeTable.key eq key)
            }.singleOrNull()?.let { row -> toType(ModelTypeRecord.read(row)) }
        }
    }

    override fun findTypeByIdOptional(
        modelId: ModelId, typeId: TypeId
    ): ModelType? {
        return db.withExposed {
            ModelTypeTable.selectAll().where {
                (ModelTypeTable.modelSnapshotId eq modelId) and (ModelTypeTable.lineageId eq typeId)
            }.singleOrNull()?.let { row -> toType(ModelTypeRecord.read(row)) }
        }
    }

    override fun findEntityByIdOptional(
        modelId: ModelId, entityId: EntityId
    ): Entity? {
        return db.withExposed {
            EntityTable.selectAll().where {
                (EntityTable.modelSnapshotId eq modelId) and (EntityTable.lineageId eq entityId)
            }.singleOrNull()?.let { row ->
                val record = EntityRecord.read(row)
                toEntity(record, loadEntityTags(record.snapshotId))
            }
        }
    }

    override fun findEntityByKeyOptional(
        modelId: ModelId, entityKey: EntityKey
    ): Entity? {
        return db.withExposed {
            EntityTable.selectAll().where {
                (EntityTable.modelSnapshotId eq modelId) and (EntityTable.key eq entityKey)
            }.singleOrNull()?.let { row ->
                val record = EntityRecord.read(row)
                val tags = loadEntityTags(record.snapshotId)
                toEntity(record, tags)
            }
        }
    }

    override fun findEntityAttributeByIdOptional(
        modelId: ModelId, entityId: EntityId, attributeId: AttributeId
    ): Attribute? {
        return db.withExposed {
            EntityAttributeTable.join(
                EntityTable, JoinType.INNER, EntityAttributeTable.entitySnapshotId, EntityTable.id
            ).selectAll()
                .where { (EntityTable.modelSnapshotId eq modelId) and (EntityTable.lineageId eq entityId) and (EntityAttributeTable.lineageId eq attributeId) }
                .singleOrNull()?.let { row ->
                    val record = EntityAttributeRecord.read(row)
                    val tags = loadEntityAttributeTags(record.snapshotId)
                    toEntityAttribute(record, tags)
                }
        }
    }

    override fun findEntityAttributeByKeyOptional(
        modelId: ModelId, entityId: EntityId, key: AttributeKey
    ): Attribute? {
        return db.withExposed {
            EntityAttributeTable.join(
                EntityTable, JoinType.INNER, EntityAttributeTable.entitySnapshotId, EntityTable.id
            ).selectAll()
                .where { (EntityTable.modelSnapshotId eq modelId) and (EntityTable.lineageId eq entityId) and (EntityAttributeTable.key eq key) }
                .singleOrNull()?.let { row ->
                    val record = EntityAttributeRecord.read(row)
                    val tags = loadEntityAttributeTags(record.snapshotId)
                    toEntityAttribute(record, tags)
                }
        }
    }

    override fun findRelationshipByIdOptional(modelId: ModelId, relationshipId: RelationshipId): Relationship? {
        return findRelationshipByOptional(modelId, RelationshipTable.lineageId eq relationshipId)
    }

    override fun findRelationshipByKeyOptional(modelId: ModelId, relationshipKey: RelationshipKey): Relationship? {
        return findRelationshipByOptional(modelId, RelationshipTable.key eq relationshipKey)
    }


    private fun findRelationshipByOptional(modelId: ModelId, criterion: Expression<Boolean>): Relationship? {
        return db.withExposed {
            val roleRecords = RelationshipRoleTable.join(
                RelationshipTable,
                JoinType.INNER,
                onColumn = RelationshipRoleTable.relationshipSnapshotId,
                otherColumn = RelationshipTable.id
            ).selectAll().where { (RelationshipTable.modelSnapshotId eq modelId) and criterion }
                .map { RelationshipRoleRecord.read(it) }

            RelationshipTable.selectAll().where { (RelationshipTable.modelSnapshotId eq modelId) and criterion }.singleOrNull()
                ?.let { row ->
                    val record = RelationshipRecord.read(row)
                    val tags = loadRelationshipTags(record.snapshotId)
                    toRelationship(record, roleRecords, tags)
                }
        }
    }

    override fun findRelationshipRoleByIdOptional(
        modelId: ModelId, relationshipId: RelationshipId, roleId: RelationshipRoleId
    ): RelationshipRole? {
        return findRelationshipRoleByOptional(modelId, relationshipId, RelationshipRoleTable.lineageId eq roleId)
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
            onColumn = RelationshipRoleTable.relationshipSnapshotId,
            otherColumn = RelationshipTable.id
        ).selectAll().where {
            (RelationshipTable.modelSnapshotId eq modelId) and (RelationshipTable.lineageId eq relationshipId) and criterion
        }.singleOrNull()?.let { toRelationshipRole(RelationshipRoleRecord.read(it)) }
    }


    override fun findRelationshipAttributeByIdOptional(
        modelId: ModelId, relationshipId: RelationshipId, attributeId: AttributeId
    ): Attribute? {
        return findRelationshipAttributeByOptional(
            modelId, relationshipId, RelationshipAttributeTable.lineageId eq attributeId
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
                RelationshipTable, JoinType.INNER, RelationshipAttributeTable.relationshipSnapshotId, RelationshipTable.id
            ).selectAll()
                .where { (RelationshipTable.modelSnapshotId eq modelId) and (RelationshipTable.lineageId eq relationshipId) and criterion }
                .singleOrNull()?.let { row ->
                    val record = RelationshipAttributeRecord.read(row)
                    val tags = loadRelationshipAttributeTags(record.snapshotId)
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
                EntityTable, JoinType.INNER, onColumn = EntityAttributeTable.entitySnapshotId, otherColumn = EntityTable.id
            ).selectAll().where {
                (EntityAttributeTable.typeSnapshotId eq typeId) and (EntityTable.modelSnapshotId eq modelId)
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
                onColumn = RelationshipAttributeTable.relationshipSnapshotId,
                otherColumn = RelationshipTable.id
            ).selectAll().where {
                (RelationshipAttributeTable.typeSnapshotId eq typeId) and (RelationshipTable.modelSnapshotId eq modelId)
            }.any()
        }

    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    override fun dispatch(cmdEnv: ModelStorageCmdEnveloppe) {
        db.withExposed {
            val modelId = extractModelId(cmdEnv.cmd)
            val streamNumberCtx = eventSystem.eventStreamNumberManager.createNumberContext(modelId)
            dispatchExposed(cmdEnv, streamNumberCtx)
        }
    }

    private fun dispatchExposed(
        cmdEnv: ModelStorageCmdEnveloppe,
        streamNumberCtx: ModelEventStreamNumberContext
    ) {
        val cmd = cmdEnv.cmd
        if (cmd is ModelStorageCmd.CreateModel || cmd is ModelStorageCmd.StoreModelAggregate) {
            dispatchCommand(cmd)
            appendModelEvent(cmdEnv, streamNumberCtx)
        } else if (cmd is ModelStorageCmd.DeleteModel) {
            appendModelEvent(cmdEnv, streamNumberCtx)
            dispatchCommand(cmd)
            return
        } else {
            appendModelEvent(cmdEnv, streamNumberCtx)
            dispatchCommand(cmd)
        }
    }

    private fun dispatchCommand(cmd: ModelStorageCmd) {
        when (cmd) {
            //@formatter:off
            is ModelStorageCmd.StoreModelAggregate -> storeModelAggregate(cmd)
            is ModelStorageCmd.CreateModel -> createModel(cmd)
            is ModelStorageCmd.DeleteModel -> deleteModel(cmd.modelId)
            is ModelStorageCmd.UpdateModelName -> updateModelName(cmd.modelId, cmd.name)
            is ModelStorageCmd.UpdateModelKey -> updateModelKey(cmd.modelId, cmd.key)
            is ModelStorageCmd.UpdateModelDescription -> updateModelDescription(cmd.modelId, cmd.description)
            is ModelStorageCmd.UpdateModelAuthority -> updateModelAuthority(cmd.modelId, cmd.authority)
            is ModelStorageCmd.ModelRelease -> releaseModel(cmd.modelId, cmd.version)
            is ModelStorageCmd.UpdateModelDocumentationHome -> updateModelDocumentationHome(cmd.modelId, cmd.url)
            is ModelStorageCmd.UpdateModelTagAdd -> addModelTag(cmd.modelId, cmd.tagId)
            is ModelStorageCmd.UpdateModelTagDelete -> deleteModelTag(cmd.modelId, cmd.tagId)
            is ModelStorageCmd.CreateType -> createType(cmd)
            is ModelStorageCmd.UpdateTypeKey -> updateTypeKey(cmd.modelId, cmd.typeId, cmd.value)
            is ModelStorageCmd.UpdateTypeName -> updateTypeName(cmd.modelId, cmd.typeId, cmd.value)
            is ModelStorageCmd.UpdateTypeDescription -> updateTypeDescription(cmd.modelId, cmd.typeId, cmd.value)
            is ModelStorageCmd.DeleteType -> deleteType(cmd.modelId, cmd.typeId)
            is ModelStorageCmd.CreateEntity -> createEntity(cmd)
            is ModelStorageCmd.UpdateEntityKey -> updateEntityKey(cmd.modelId, cmd.entityId, cmd.value)
            is ModelStorageCmd.UpdateEntityName -> updateEntityName(cmd.modelId, cmd.entityId, cmd.value)
            is ModelStorageCmd.UpdateEntityDescription -> updateEntityDescription(cmd.modelId, cmd.entityId, cmd.value)
            is ModelStorageCmd.UpdateEntityIdentifierAttribute -> updateEntityIdentifierAttribute(cmd.modelId, cmd.entityId, cmd.value)
            is ModelStorageCmd.UpdateEntityDocumentationHome -> updateEntityDocumentationHome(cmd.modelId, cmd.entityId, cmd.value)
            is ModelStorageCmd.UpdateEntityTagAdd -> addEntityTag(cmd.entityId, cmd.tagId)
            is ModelStorageCmd.UpdateEntityTagDelete -> deleteEntityTag(cmd.entityId, cmd.tagId)
            is ModelStorageCmd.DeleteEntity -> deleteEntity(cmd.modelId, cmd.entityId)
            is ModelStorageCmd.CreateEntityAttribute -> createEntityAttribute(cmd)
            is ModelStorageCmd.UpdateEntityAttributeKey -> updateEntityAttributeKey(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateEntityAttributeName -> updateEntityAttributeName(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateEntityAttributeDescription -> updateEntityAttributeDescription(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateEntityAttributeType -> updateEntityAttributeType(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateEntityAttributeOptional -> updateEntityAttributeOptional(cmd.entityId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateEntityAttributeTagAdd -> addEntityAttributeTag(cmd.attributeId, cmd.tagId)
            is ModelStorageCmd.UpdateEntityAttributeTagDelete -> deleteEntityAttributeTag(cmd.attributeId, cmd.tagId)
            is ModelStorageCmd.DeleteEntityAttribute -> deleteEntityAttribute(cmd.entityId, cmd.attributeId)
            is ModelStorageCmd.CreateRelationship -> createRelationship(cmd)
            is ModelStorageCmd.UpdateRelationshipKey -> updateRelationshipKey(cmd.relationshipId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipName -> updateRelationshipName(cmd.relationshipId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipDescription -> updateRelationshipDescription(cmd.relationshipId, cmd.value)
            is ModelStorageCmd.CreateRelationshipRole -> createRelationshipRole(cmd)
            is ModelStorageCmd.UpdateRelationshipRoleKey -> updateRelationshipRoleKey(cmd.relationshipRoleId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipRoleName -> updateRelationshipRoleName(cmd.relationshipRoleId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipRoleEntity -> updateRelationshipRoleEntity(cmd.relationshipRoleId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipRoleCardinality -> updateRelationshipRoleCardinality(cmd.relationshipRoleId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipTagAdd -> addRelationshipTag(cmd.relationshipId, cmd.tagId)
            is ModelStorageCmd.UpdateRelationshipTagDelete -> deleteRelationshipTag(cmd.relationshipId, cmd.tagId)
            is ModelStorageCmd.DeleteRelationship -> deleteRelationship(cmd.modelId, cmd.relationshipId)
            is ModelStorageCmd.DeleteRelationshipRole -> deleteRelationshipRole(cmd.relationshipId, cmd.relationshipRoleId)
            is ModelStorageCmd.CreateRelationshipAttribute -> createRelationshipAttribute(cmd)
            is ModelStorageCmd.UpdateRelationshipAttributeKey -> updateRelationshipAttributeKey(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipAttributeName -> updateRelationshipAttributeName(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipAttributeDescription -> updateRelationshipAttributeDescription(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipAttributeType -> updateRelationshipAttributeType(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipAttributeOptional -> updateRelationshipAttributeOptional(cmd.relationshipId, cmd.attributeId, cmd.value)
            is ModelStorageCmd.UpdateRelationshipAttributeTagAdd -> addRelationshipAttributeTag(cmd.attributeId, cmd.tagId)
            is ModelStorageCmd.UpdateRelationshipAttributeTagDelete -> deleteRelationshipAttributeTag(cmd.attributeId, cmd.tagId)
            is ModelStorageCmd.DeleteRelationshipAttribute -> deleteRelationshipAttribute(cmd.relationshipId, cmd.attributeId)
            //@formatter:on
        }
    }

    private fun appendModelEvent(
        cmdEnv: ModelStorageCmdEnveloppe,
        streamNumberCtx: ModelEventStreamNumberContext
    ) {
        val record = eventSystem.recordFactory.create(
            cmdEnv = cmdEnv,
            streamRevision = streamNumberCtx.nextRevision(),
            createdAt = clock.now()
        )
        try {
            ModelEventTable.insert { row ->
                row[ModelEventTable.id] = record.id
                row[ModelEventTable.modelId] = record.modelId
                row[ModelEventTable.streamRevision] = record.streamRevision
                row[ModelEventTable.eventType] = record.eventType
                row[ModelEventTable.eventVersion] = record.eventVersion
                row[ModelEventTable.modelVersion] = record.modelVersion
                row[ModelEventTable.actorId] = record.actorId
                row[ModelEventTable.actionId] = record.actionId
                row[ModelEventTable.createdAt] = record.createdAt.toString()
                row[ModelEventTable.payload] = record.payload
            }
        } catch (e: Exception) {
            eventSystem.eventStreamNumberManager.rethrowIfStreamRevisionConflict(
                exception = e,
                numberContext = streamNumberCtx,
                conflictingRevision = record.streamRevision
            )
            throw e
        }
        eventSystem.eventStreamNumberManager.onAppendCommitted(streamNumberCtx, record.streamRevision)
    }

    private fun extractModelId(cmd: ModelStorageCmd): ModelId {
        return when (cmd) {
            is ModelStorageCmd.CreateModel -> cmd.id
            is ModelStorageCmd.StoreModelAggregate -> cmd.model.id
            is ModelStorageCmdOnModel -> cmd.modelId
        }
    }

    // Model
    // ------------------------------------------------------------------------

    private fun loadModelAggregate(row: ResultRow): ModelAggregateInMemory {
        val record = ModelRecord.read(row)
        val types = loadTypes(record.modelId)
        val entities = loadEntities(record.modelId)
        val entityAttributes = loadEntityAttributes(record.modelId)
        val relationships = loadRelationships(record.modelId)
        val relationshipAttributes = loadRelationshipAttributes(record.modelId)

        return ModelAggregateInMemory(
            model = toModel(record),
            types = types,
            entities = entities,
            relationships = relationships,
            tags = loadModelTags(record.modelId),
            attributes = entityAttributes + relationshipAttributes
        )
    }

    private fun loadModelTags(modelId: ModelId): List<TagId> {
        return ModelTagTable.selectAll().where { ModelTagTable.modelSnapshotId eq modelId }
            .orderBy(ModelTagTable.tagId to SortOrder.ASC).map { it[ModelTagTable.tagId] }
    }

    private fun createModel(cmd: ModelStorageCmd.CreateModel) {
        val inMemoryModel = ModelInMemory(
            id = cmd.id,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description,
            version = cmd.version,
            origin = cmd.origin,
            authority = cmd.authority,
            documentationHome = cmd.documentationHome,
        )
        db.withExposed {
            insertModel(inMemoryModel)
            searchWrite.upsertModelSearchItem(inMemoryModel.id)
        }
    }

    private fun storeModelAggregate(model: ModelStorageCmd.StoreModelAggregate) {

        logger.warn("Storing full aggregate {}", model)

        val modelId = model.model.id

        insertModel(
            ModelInMemory(
                id = model.model.id,
                key = model.model.key,
                name = model.model.name,
                description = model.model.description,
                version = model.model.version,
                origin = model.model.origin,
                authority = model.model.authority,
                documentationHome = model.model.documentationHome,
            )
        )

        for (type in model.types) {
            insertType(
                ModelTypeRecord(
                    snapshotId = type.id,
                    lineageId = type.id,
                    modelSnapshotId = modelId,
                    key = type.key,
                    name = type.name,
                    description = type.description
                )
            )
        }

        for (entity in model.entities) {
            insertEntity(
                EntityRecord(
                    snapshotId = entity.id,
                    lineageId = entity.id,
                    modelSnapshotId = modelId,
                    key = entity.key,
                    name = entity.name,
                    description = entity.description,
                    identifierAttributeSnapshotId = entity.identifierAttributeId,
                    origin = entity.origin,
                    documentationHome = entity.documentationHome?.toExternalForm(),
                )
            )
            searchWrite.upsertEntitySearchItem(entity.id)

            for (attr in model.entityAttributes.filter { it.entityId == entity.id }) {
                insertEntityAttribute(
                    EntityAttributeRecord(
                        snapshotId = attr.id,
                        lineageId = attr.id,
                        entitySnapshotId = entity.id,
                        key = attr.key,
                        name = attr.name,
                        description = attr.description,
                        typeSnapshotId = attr.typeId,
                        optional = attr.optional
                    )
                )
                searchWrite.upsertEntityAttributeSearchItem(attr.id)
            }
        }

        for (relationship in model.relationships) {
            insertRelationship(
                record = RelationshipRecord(
                    snapshotId = relationship.id,
                    lineageId = relationship.id,
                    modelSnapshotId = modelId,
                    key = relationship.key,
                    name = relationship.name,
                    description = relationship.description
                ),
                roles = relationship.roles.map { role ->
                    RelationshipRoleRecord(
                        snapshotId = role.id,
                        lineageId = role.id,
                        relationshipSnapshotId = relationship.id,
                        key = role.key,
                        entitySnapshotId = role.entityId,
                        name = role.name,
                        cardinality = role.cardinality.code
                    )
                }
            )
            searchWrite.upsertRelationshipSearchItem(relationship.id)

            for (attr in model.relationshipAttributes.filter { it.relationshipId == relationship.id }) {
                insertRelationshipAttribute(
                    RelationshipAttributeRecord(
                        snapshotId = attr.id,
                        lineageId = attr.id,
                        relationshipSnapshotId = relationship.id,
                        key = attr.key,
                        name = attr.name,
                        description = attr.description,
                        typeSnapshotId = attr.typeId,
                        optional = attr.optional
                    )
                )
                searchWrite.upsertRelationshipAttributeSearchItem(attr.id)
            }
        }

        searchWrite.upsertModelSearchItem(modelId)

    }

    private fun deleteModel(modelId: ModelId) {
        searchWrite.deleteModelBranch(modelId)
        ModelTable.deleteWhere { id eq modelId }
    }

    private fun updateModelName(modelId: ModelId, name: LocalizedText) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.modelId eq modelId }) { row ->
            row[ModelSnapshotTable.name] = name
        }
        searchWrite.upsertModelSearchItem(modelId)
    }

    private fun updateModelKey(modelId: ModelId, key: ModelKey) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.modelId eq modelId }) { row ->
            row[ModelSnapshotTable.key] = key
        }
        searchWrite.upsertModelSearchItem(modelId)
    }

    private fun updateModelDescription(modelId: ModelId, description: LocalizedMarkdown?) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.modelId eq modelId }) { row ->
            row[ModelSnapshotTable.description] = description
        }
        searchWrite.upsertModelSearchItem(modelId)
    }

    private fun updateModelAuthority(modelId: ModelId, authority: ModelAuthority) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.modelId eq modelId }) { row ->
            row[ModelSnapshotTable.authority] = authority
        }
    }

    private fun releaseModel(modelId: ModelId, version: ModelVersion) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.modelId eq modelId }) { row ->
            row[ModelSnapshotTable.version] = version.value
        }
    }

    private fun updateModelDocumentationHome(modelId: ModelId, documentationHome: java.net.URL?) {
        ModelSnapshotTable.update(where = { ModelSnapshotTable.modelId eq modelId }) { row ->
            row[ModelSnapshotTable.documentationHome] = documentationHome?.toExternalForm()
        }
    }

    private fun addModelTag(modelId: ModelId, tagId: TagId) {
        val exists = ModelTagTable.select(ModelTagTable.modelSnapshotId).where {
            (ModelTagTable.modelSnapshotId eq modelId) and (ModelTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            ModelTagTable.insert { row ->
                row[ModelTagTable.modelSnapshotId] = modelId
                row[ModelTagTable.tagId] = tagId
            }
        }
        searchWrite.upsertModelSearchItem(modelId)
    }

    private fun deleteModelTag(modelId: ModelId, tagId: TagId) {
        ModelTagTable.deleteWhere {
            (ModelTagTable.modelSnapshotId eq modelId) and (ModelTagTable.tagId eq tagId)
        }
        searchWrite.upsertModelSearchItem(modelId)
    }

    private fun insertModel(model: Model) {
        ModelTable.insert { row ->
            row[ModelTable.id] = model.id
        }
        ModelSnapshotTable.insert { row ->
            row[ModelSnapshotTable.id] = model.id.asString()
            row[ModelSnapshotTable.modelId] = model.id
            row[ModelSnapshotTable.key] = model.key
            row[ModelSnapshotTable.name] = model.name
            row[ModelSnapshotTable.description] = model.description
            row[ModelSnapshotTable.origin] = model.origin
            row[ModelSnapshotTable.authority] = model.authority
            row[ModelSnapshotTable.documentationHome] = model.documentationHome?.toExternalForm()
            row[ModelSnapshotTable.snapshotKind] = CURRENT_HEAD_SNAPSHOT_KIND
            row[ModelSnapshotTable.upToRevision] = 0
            row[ModelSnapshotTable.modelEventReleaseId] = null
            row[ModelSnapshotTable.version] = model.version.value
            row[ModelSnapshotTable.createdAt] = clock.now().toString()
            row[ModelSnapshotTable.updatedAt] = clock.now().toString()
        }
    }


    // Types
    // ------------------------------------------------------------------------

    private fun loadTypes(modelId: ModelId): List<ModelTypeInMemory> {
        return ModelTypeTable.selectAll().where { ModelTypeTable.modelSnapshotId eq modelId }
            .orderBy(ModelTypeTable.key to SortOrder.ASC).map { row ->
                toType(ModelTypeRecord.read(row))
            }
    }

    private fun createType(cmd: ModelStorageCmd.CreateType) {
        val typeId = TypeId.generate()
        val record = ModelTypeRecord(
            snapshotId = typeId,
            lineageId = typeId,
            modelSnapshotId = cmd.modelId,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description
        )
        insertType(record)
    }

    private fun insertType(record: ModelTypeRecord) {
        ModelTypeTable.insert { row ->
            row[ModelTypeTable.id] = record.snapshotId
            row[ModelTypeTable.lineageId] = record.lineageId
            row[ModelTypeTable.modelSnapshotId] = record.modelSnapshotId
            row[ModelTypeTable.key] = record.key
            row[ModelTypeTable.name] = record.name
            row[ModelTypeTable.description] = record.description
        }
    }

    private fun updateTypeKey(modelId: ModelId, typeId: TypeId, value: TypeKey) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelId)
            }) { row ->
            row[ModelTypeTable.key] = value
        }
    }

    private fun updateTypeName(modelId: ModelId, typeId: TypeId, value: LocalizedText?) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelId)
            }) { row ->
            row[ModelTypeTable.name] = value
        }
    }

    private fun updateTypeDescription(modelId: ModelId, typeId: TypeId, value: LocalizedMarkdown?) {
        ModelTypeTable.update(
            where = {
                (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelId)
            }) { row ->
            row[ModelTypeTable.description] = value
        }
    }

    private fun deleteType(modelId: ModelId, typeId: TypeId) {
        ModelTypeTable.deleteWhere {
            (ModelTypeTable.lineageId eq typeId) and (ModelTypeTable.modelSnapshotId eq modelId)
        }
    }

    // Entity
    // ------------------------------------------------------------------------

    private fun loadEntities(modelId: ModelId): List<EntityInMemory> {

        return EntityTable.selectAll().where { EntityTable.modelSnapshotId eq modelId }
            .orderBy(EntityTable.key to SortOrder.ASC).map { row ->
                val record = EntityRecord.read(row)
                val tags = loadEntityTags(record.snapshotId)
                toEntity(record, tags)
            }
    }

    private fun loadEntityTags(entityId: EntityId): List<TagId> {
        return EntityTagTable.selectAll().where { EntityTagTable.entitySnapshotId eq entityId }
            .orderBy(EntityTagTable.tagId to SortOrder.ASC).map { it[EntityTagTable.tagId] }
    }

    private fun createEntity(cmd: ModelStorageCmd.CreateEntity) {
        insertEntity(cmd)
        searchWrite.upsertEntitySearchItem(cmd.entityId)
    }

    private fun updateEntityKey(modelId: ModelId, entityId: EntityId, value: EntityKey) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelId)
            }) { row ->
            row[EntityTable.key] = value
        }
        searchWrite.upsertEntitySearchItem(entityId)
    }

    private fun updateEntityName(modelId: ModelId, entityId: EntityId, value: LocalizedText?) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelId)
            }) { row ->
            row[EntityTable.name] = value
        }
        searchWrite.upsertEntitySearchItem(entityId)
    }

    private fun updateEntityDescription(modelId: ModelId, entityId: EntityId, value: LocalizedMarkdown?) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelId)
            }) { row ->
            row[EntityTable.description] = value
        }
        searchWrite.upsertEntitySearchItem(entityId)
    }

    private fun updateEntityIdentifierAttribute(modelId: ModelId, entityId: EntityId, value: AttributeId) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelId)
            }) { row ->
            row[EntityTable.identifierAttributeSnapshotId] = value
        }
    }

    private fun updateEntityDocumentationHome(modelId: ModelId, entityId: EntityId, value: java.net.URL?) {
        EntityTable.update(
            where = {
                (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelId)
            }) { row ->
            row[EntityTable.documentationHome] = value?.toExternalForm()
        }
    }

    private fun addEntityTag(entityId: EntityId, tagId: TagId) {
        val exists = EntityTagTable.select(EntityTagTable.entitySnapshotId).where {
            (EntityTagTable.entitySnapshotId eq entityId) and (EntityTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertEntityTag(entityId, tagId)
        }
        searchWrite.upsertEntitySearchItem(entityId)
    }

    private fun insertEntityTag(entityId: EntityId, tagId: TagId) {
        EntityTagTable.insert { row ->
            row[EntityTagTable.entitySnapshotId] = entityId
            row[EntityTagTable.tagId] = tagId
        }
    }

    private fun deleteEntityTag(entityId: EntityId, tagId: TagId) {
        EntityTagTable.deleteWhere {
            (EntityTagTable.entitySnapshotId eq entityId) and (EntityTagTable.tagId eq tagId)
        }
        searchWrite.upsertEntitySearchItem(entityId)
    }

    private fun deleteEntity(modelId: ModelId, entityId: EntityId) {
        searchWrite.deleteEntityBranch(entityId)
        EntityTable.deleteWhere {
            (EntityTable.lineageId eq entityId) and (EntityTable.modelSnapshotId eq modelId)
        }
    }

    private fun insertEntity(cmd: ModelStorageCmd.CreateEntity) {

        val record = EntityRecord(
            snapshotId = cmd.entityId,
            lineageId = cmd.entityId,
            modelSnapshotId = cmd.modelId,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description,
            identifierAttributeSnapshotId = cmd.identityAttributeId,
            origin = cmd.origin,
            documentationHome = cmd.documentationHome?.toExternalForm()
        )

        insertEntity(record)

        insertEntityAttribute(
            EntityAttributeRecord(
                snapshotId = cmd.identityAttributeId,
                lineageId = cmd.identityAttributeId,
                entitySnapshotId = cmd.entityId,
                key = cmd.identityAttributeKey,
                name = cmd.identityAttributeName,
                description = cmd.identityAttributeDescription,
                typeSnapshotId = cmd.identityAttributeTypeId,
                optional = cmd.identityAttributeIdOptional
            )
        )
    }

    private fun insertEntity(record: EntityRecord) {
        EntityTable.insert { row ->
            row[EntityTable.id] = record.snapshotId
            row[EntityTable.lineageId] = record.lineageId
            row[EntityTable.modelSnapshotId] = record.modelSnapshotId
            row[EntityTable.key] = record.key
            row[EntityTable.name] = record.name
            row[EntityTable.description] = record.description
            row[EntityTable.identifierAttributeSnapshotId] = record.identifierAttributeSnapshotId
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
            onColumn = EntityAttributeTable.entitySnapshotId,
            otherColumn = EntityTable.id
        ).selectAll().where { EntityTable.modelSnapshotId eq modelId }.map { row ->
            val record = EntityAttributeRecord.read(row)
            val tags = loadEntityAttributeTags(record.snapshotId)
            toEntityAttribute(record, tags)
        }
    }

    private fun loadEntityAttributeTags(attributeId: AttributeId): List<TagId> {
        return EntityAttributeTagTable.selectAll().where { EntityAttributeTagTable.attributeSnapshotId eq attributeId }
            .orderBy(EntityAttributeTagTable.tagId to SortOrder.ASC).map { it[EntityAttributeTagTable.tagId] }
    }

    private fun createEntityAttribute(cmd: ModelStorageCmd.CreateEntityAttribute) {
        insertEntityAttribute(
            EntityAttributeRecord(
                snapshotId = cmd.attributeId,
                lineageId = cmd.attributeId,
                entitySnapshotId = cmd.entityId,
                key = cmd.key,
                name = cmd.name,
                description = cmd.description,
                typeSnapshotId = cmd.typeId,
                optional = cmd.optional
            )
        )
    }

    private fun updateEntityAttributeKey(entityId: EntityId, attributeId: AttributeId, value: AttributeKey) {
        EntityAttributeTable.update(
            where = {
                EntityAttributeTable.lineageId eq attributeId
            }) { row ->
            row[EntityAttributeTable.key] = value
        }
        searchWrite.upsertEntityAttributeSearchItem(attributeId)
    }

    private fun updateEntityAttributeName(entityId: EntityId, attributeId: AttributeId, value: LocalizedText?) {
        EntityAttributeTable.update(
            where = {
                EntityAttributeTable.lineageId eq attributeId
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
                EntityAttributeTable.lineageId eq attributeId
            }) { row ->
            row[EntityAttributeTable.description] = value
        }
        searchWrite.upsertEntityAttributeSearchItem(attributeId)

    }

    private fun updateEntityAttributeType(entityId: EntityId, attributeId: AttributeId, value: TypeId) {

        EntityAttributeTable.update(
            where = {
                EntityAttributeTable.lineageId eq attributeId
            }) { row ->
            row[EntityAttributeTable.typeSnapshotId] = value
        }

    }

    private fun updateEntityAttributeOptional(entityId: EntityId, attributeId: AttributeId, value: Boolean) {
        EntityAttributeTable.update(
            where = {
                EntityAttributeTable.lineageId eq attributeId
            }) { row ->
            row[EntityAttributeTable.optional] = value
        }
    }


    private fun insertEntityAttribute(
        record: EntityAttributeRecord

    ) {
        EntityAttributeTable.insert { row ->
            row[EntityAttributeTable.id] = record.snapshotId
            row[EntityAttributeTable.lineageId] = record.lineageId
            row[EntityAttributeTable.entitySnapshotId] = record.entitySnapshotId
            row[EntityAttributeTable.key] = record.key
            row[EntityAttributeTable.name] = record.name
            row[EntityAttributeTable.description] = record.description
            row[EntityAttributeTable.typeSnapshotId] = record.typeSnapshotId
            row[EntityAttributeTable.optional] = record.optional
        }
        searchWrite.upsertEntityAttributeSearchItem(record.lineageId)
    }


    private fun addEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        val exists = EntityAttributeTagTable.select(EntityAttributeTagTable.attributeSnapshotId).where {
            (EntityAttributeTagTable.attributeSnapshotId eq attributeId) and (EntityAttributeTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertEntityAttributeTag(attributeId, tagId)
        }
        searchWrite.upsertEntityAttributeSearchItem(attributeId)
    }

    private fun insertEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        EntityAttributeTagTable.insert { row ->
            row[EntityAttributeTagTable.attributeSnapshotId] = attributeId
            row[EntityAttributeTagTable.tagId] = tagId
        }
    }

    private fun deleteEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        EntityAttributeTagTable.deleteWhere {
            (EntityAttributeTagTable.attributeSnapshotId eq attributeId) and (EntityAttributeTagTable.tagId eq tagId)
        }
        searchWrite.upsertEntityAttributeSearchItem(attributeId)
    }

    private fun deleteEntityAttribute(entityId: EntityId, attributeId: AttributeId) {
        searchWrite.deleteEntityAttributeSearchItem(attributeId)
        EntityAttributeTable.deleteWhere {
            EntityAttributeTable.lineageId eq attributeId
        }
    }


    // Relationship
    // ------------------------------------------------------------------------


    private fun loadRelationships(modelId: ModelId): List<RelationshipInMemory> {
        val relationshipIds =
            RelationshipTable.select(RelationshipTable.id).where { RelationshipTable.modelSnapshotId eq modelId }

        val roleRowsByRelationshipId =
            RelationshipRoleTable.selectAll().where { RelationshipRoleTable.relationshipSnapshotId inSubQuery relationshipIds }
                .orderBy(RelationshipRoleTable.key to SortOrder.ASC).toList()
                .groupBy { it[RelationshipRoleTable.relationshipSnapshotId] }

        return RelationshipTable.selectAll().where { RelationshipTable.modelSnapshotId eq modelId }
            .orderBy(RelationshipTable.key to SortOrder.ASC).map { row ->
                val relationshipRecord = RelationshipRecord.read(row)
                val relationshipId = relationshipRecord.snapshotId
                val roleRecords =
                    (roleRowsByRelationshipId[relationshipId] ?: emptyList()).map { RelationshipRoleRecord.read(it) }
                val tags = loadRelationshipTags(relationshipRecord.snapshotId)
                toRelationship(relationshipRecord, roleRecords, tags)
            }
    }

    private fun loadRelationshipTags(relationshipId: RelationshipId): List<TagId> {
        return RelationshipTagTable.selectAll().where { RelationshipTagTable.relationshipSnapshotId eq relationshipId }
            .orderBy(RelationshipTagTable.tagId to SortOrder.ASC).map { it[RelationshipTagTable.tagId] }
    }

    private fun createRelationship(cmd: ModelStorageCmd.CreateRelationship) {
        val record = RelationshipRecord(
            snapshotId = cmd.relationshipId,
            lineageId = cmd.relationshipId,
            modelSnapshotId = cmd.modelId,
            key = cmd.key,
            name = cmd.name,
            description = cmd.description
        )
        val roles = cmd.roles.map { role ->
            RelationshipRoleRecord(
                snapshotId = role.id,
                lineageId = role.id,
                relationshipSnapshotId = cmd.relationshipId,
                key = role.key,
                name = role.name,
                entitySnapshotId = role.entityId,
                cardinality = role.cardinality.code
            )
        }
        insertRelationship(record, roles)
        searchWrite.upsertRelationshipSearchItem(cmd.relationshipId)
    }

    private fun updateRelationshipKey(relationshipId: RelationshipId, value: RelationshipKey) {
        RelationshipTable.update(where = { RelationshipTable.lineageId eq relationshipId }) { row ->
            row[RelationshipTable.key] = value
        }
        searchWrite.upsertRelationshipSearchItem(relationshipId)
    }

    private fun updateRelationshipName(relationshipId: RelationshipId, value: LocalizedText?) {
        RelationshipTable.update(where = { RelationshipTable.lineageId eq relationshipId }) { row ->
            row[RelationshipTable.name] = value
        }
        searchWrite.upsertRelationshipSearchItem(relationshipId)
    }

    private fun updateRelationshipDescription(relationshipId: RelationshipId, value: LocalizedMarkdown?) {
        RelationshipTable.update(where = { RelationshipTable.lineageId eq relationshipId }) { row ->
            row[RelationshipTable.description] = value
        }
        searchWrite.upsertRelationshipSearchItem(relationshipId)
    }

    private fun createRelationshipRole(cmd: ModelStorageCmd.CreateRelationshipRole) {
        RelationshipRoleTable.insert { row ->
            row[RelationshipRoleTable.id] = cmd.relationshipRoleId
            row[RelationshipRoleTable.lineageId] = cmd.relationshipRoleId
            row[RelationshipRoleTable.relationshipSnapshotId] = cmd.relationshipId
            row[RelationshipRoleTable.key] = cmd.key
            row[RelationshipRoleTable.entitySnapshotId] = cmd.entityId
            row[RelationshipRoleTable.name] = cmd.name
            row[RelationshipRoleTable.cardinality] = cmd.cardinality.code
        }
    }

    private fun updateRelationshipRoleKey(relationshipRoleId: RelationshipRoleId, value: RelationshipRoleKey) {
        RelationshipRoleTable.update(where = { RelationshipRoleTable.lineageId eq relationshipRoleId }) { row ->
            row[RelationshipRoleTable.key] = value
        }
    }

    private fun updateRelationshipRoleName(relationshipRoleId: RelationshipRoleId, value: LocalizedText?) {
        RelationshipRoleTable.update(where = { RelationshipRoleTable.lineageId eq relationshipRoleId }) { row ->
            row[RelationshipRoleTable.name] = value
        }
    }

    private fun updateRelationshipRoleEntity(relationshipRoleId: RelationshipRoleId, value: EntityId) {
        RelationshipRoleTable.update(where = { RelationshipRoleTable.lineageId eq relationshipRoleId }) { row ->
            row[RelationshipRoleTable.entitySnapshotId] = value
        }
    }

    private fun updateRelationshipRoleCardinality(
        relationshipRoleId: RelationshipRoleId, value: RelationshipCardinality
    ) {
        RelationshipRoleTable.update(where = { RelationshipRoleTable.lineageId eq relationshipRoleId }) { row ->
            row[RelationshipRoleTable.cardinality] = value.code
        }
    }

    private fun deleteRelationshipRole(relationshipId: RelationshipId, relationshipRoleId: RelationshipRoleId) {
        RelationshipRoleTable.deleteWhere {
            (RelationshipRoleTable.lineageId eq relationshipRoleId) and (RelationshipRoleTable.relationshipSnapshotId eq relationshipId)
        }
    }

    private fun addRelationshipTag(relationshipId: RelationshipId, tagId: TagId) {
        val exists = RelationshipTagTable.select(RelationshipTagTable.relationshipSnapshotId).where {
            (RelationshipTagTable.relationshipSnapshotId eq relationshipId) and (RelationshipTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertRelationshipTag(relationshipId, tagId)
        }
        searchWrite.upsertRelationshipSearchItem(relationshipId)
    }

    private fun insertRelationshipTag(
        relationshipId: RelationshipId,
        tagId: TagId
    ) {
        RelationshipTagTable.insert { row ->
            row[RelationshipTagTable.relationshipSnapshotId] = relationshipId
            row[RelationshipTagTable.tagId] = tagId
        }
    }

    private fun insertRelationship(record: RelationshipRecord, roles: List<RelationshipRoleRecord>) {

        RelationshipTable.insert { row ->
            row[RelationshipTable.id] = record.snapshotId
            row[RelationshipTable.lineageId] = record.lineageId
            row[RelationshipTable.modelSnapshotId] = record.modelSnapshotId
            row[RelationshipTable.key] = record.key
            row[RelationshipTable.name] = record.name
            row[RelationshipTable.description] = record.description
        }
        for (roleRecord in roles) {
            RelationshipRoleTable.insert { row ->
                row[RelationshipRoleTable.id] = roleRecord.snapshotId
                row[RelationshipRoleTable.lineageId] = roleRecord.lineageId
                row[RelationshipRoleTable.relationshipSnapshotId] = roleRecord.relationshipSnapshotId
                row[RelationshipRoleTable.key] = roleRecord.key
                row[RelationshipRoleTable.entitySnapshotId] = roleRecord.entitySnapshotId
                row[RelationshipRoleTable.name] = roleRecord.name
                row[RelationshipRoleTable.cardinality] = roleRecord.cardinality
            }
        }


    }

    private fun deleteRelationship(modelId: ModelId, relationshipId: RelationshipId) {
        searchWrite.deleteRelationshipBranch(relationshipId)
        RelationshipTable.deleteWhere {
            (RelationshipTable.lineageId eq relationshipId) and (RelationshipTable.modelSnapshotId eq modelId)
        }
    }

    private fun deleteRelationshipTag(relationshipId: RelationshipId, tagId: TagId) {
        RelationshipTagTable.deleteWhere {
            (RelationshipTagTable.relationshipSnapshotId eq relationshipId) and (RelationshipTagTable.tagId eq tagId)
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
            otherColumn = RelationshipAttributeTable.relationshipSnapshotId
        ).selectAll().where { RelationshipTable.modelSnapshotId eq modelId }.map { row ->
            val record = RelationshipAttributeRecord.read(row)
            val tags = loadRelationshipAttributeTags(record.snapshotId)
            toRelationshipAttribute(record, tags)
        }
    }

    private fun loadRelationshipAttributeTags(attributeId: AttributeId): List<TagId> {
        return RelationshipAttributeTagTable.selectAll()
            .where { RelationshipAttributeTagTable.attributeSnapshotId eq attributeId }
            .orderBy(RelationshipAttributeTagTable.tagId to SortOrder.ASC)
            .map { it[RelationshipAttributeTagTable.tagId] }
    }

    private fun createRelationshipAttribute(cmd: ModelStorageCmd.CreateRelationshipAttribute) {
        val record = RelationshipAttributeRecord(
            snapshotId = cmd.attributeId,
            lineageId = cmd.attributeId,
            relationshipSnapshotId = cmd.relationshipId,
            name = cmd.name,
            key = cmd.key,
            description = cmd.description,
            typeSnapshotId = cmd.typeId,
            optional = cmd.optional
        )
        insertRelationshipAttribute(record)
        searchWrite.upsertRelationshipAttributeSearchItem(record.lineageId)
    }

    private fun updateRelationshipAttributeKey(
        relationshipId: RelationshipId, attributeId: AttributeId, value: AttributeKey
    ) {
        RelationshipAttributeTable.update(where = {
            RelationshipAttributeTable.lineageId eq attributeId
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
                RelationshipAttributeTable.lineageId eq attributeId
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
                RelationshipAttributeTable.lineageId eq attributeId
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
                RelationshipAttributeTable.lineageId eq attributeId
            }) { row ->
            row[RelationshipAttributeTable.typeSnapshotId] = value
        }
    }

    private fun updateRelationshipAttributeOptional(
        relationshipId: RelationshipId, attributeId: AttributeId, value: Boolean
    ) {
        RelationshipAttributeTable.update(
            where = {
                RelationshipAttributeTable.lineageId eq attributeId
            }) { row ->
            row[RelationshipAttributeTable.optional] = value
        }
    }

    private fun addRelationshipAttributeTag(attributeId: AttributeId, tagId: TagId) {
        val exists = RelationshipAttributeTagTable.select(RelationshipAttributeTagTable.attributeSnapshotId).where {
            (RelationshipAttributeTagTable.attributeSnapshotId eq attributeId) and (RelationshipAttributeTagTable.tagId eq tagId)
        }.limit(1).any()
        if (!exists) {
            insertRelationshipAttributeTag(attributeId, tagId)
        }
        searchWrite.upsertRelationshipAttributeSearchItem(attributeId)
    }

    private fun insertRelationshipAttributeTag(attributeId: AttributeId, tagId: TagId) {
        RelationshipAttributeTagTable.insert { row ->
            row[RelationshipAttributeTagTable.attributeSnapshotId] = attributeId
            row[RelationshipAttributeTagTable.tagId] = tagId
        }
    }

    private fun insertRelationshipAttribute(
        record: RelationshipAttributeRecord
    ) {
        RelationshipAttributeTable.insert { row ->
            row[RelationshipAttributeTable.id] = record.snapshotId
            row[RelationshipAttributeTable.lineageId] = record.lineageId
            row[RelationshipAttributeTable.relationshipSnapshotId] = record.relationshipSnapshotId
            row[RelationshipAttributeTable.key] = record.key
            row[RelationshipAttributeTable.name] = record.name
            row[RelationshipAttributeTable.description] = record.description
            row[RelationshipAttributeTable.typeSnapshotId] = record.typeSnapshotId
            row[RelationshipAttributeTable.optional] = record.optional
        }

    }

    private fun deleteRelationshipAttribute(relationshipId: RelationshipId, attributeId: AttributeId) {
        searchWrite.deleteRelationshipAttributeSearchItem(attributeId)
        RelationshipAttributeTable.deleteWhere {
            RelationshipAttributeTable.lineageId eq attributeId
        }
    }

    private fun deleteRelationshipAttributeTag(attributeId: AttributeId, tagId: TagId) {
        RelationshipAttributeTagTable.deleteWhere {
            (RelationshipAttributeTagTable.attributeSnapshotId eq attributeId) and (RelationshipAttributeTagTable.tagId eq tagId)
        }
        searchWrite.upsertRelationshipAttributeSearchItem(attributeId)
    }

    companion object {
        private const val CURRENT_HEAD_SNAPSHOT_KIND = "CURRENT_HEAD"
        private val logger: Logger = LoggerFactory.getLogger(ModelStorageDb::class.java)
    }


}
