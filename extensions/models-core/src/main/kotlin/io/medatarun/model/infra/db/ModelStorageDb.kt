package io.medatarun.model.infra.db

import io.medatarun.model.domain.*
import io.medatarun.model.domain.search.SearchResults
import io.medatarun.model.infra.*
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
import java.net.URI

class ModelStorageDb(
    private val dbConnectionFactory: DbConnectionFactory
) : ModelStorage {
    private val searchRead = ModelStorageDbSearchRead(dbConnectionFactory)
    private val searchWrite = ModelStorageDbSearchWrite(dbConnectionFactory)

    override fun search(query: ModelStorageSearchQuery): SearchResults {
        return searchRead.search(query)
    }

    override fun findAllModelIds(): List<ModelId> {
        return dbConnectionFactory.withExposed {
            ModelTable.selectAll().map { ModelId.fromString(it[ModelTable.id]) }
        }
    }

    override fun existsModelByKey(key: ModelKey): Boolean {
        return dbConnectionFactory.withExposed {
            ModelTable.select(ModelTable.id).where { ModelTable.key eq key.value }.limit(1).any()
        }
    }

    override fun existsModelById(id: ModelId): Boolean {
        return dbConnectionFactory.withExposed {
            ModelTable.select(ModelTable.id).where { ModelTable.id eq id.asString() }.limit(1).any()
        }
    }

    override fun findModelAggregateByKeyOptional(key: ModelKey): ModelAggregate? {
        return dbConnectionFactory.withExposed {
            val row = ModelTable.selectAll().where { ModelTable.key eq key.value }.singleOrNull()
            if (row == null) null else loadModelAggregate(row)
        }
    }

    override fun findModelAggregateByIdOptional(id: ModelId): ModelAggregate? {
        return dbConnectionFactory.withExposed {
            val row = ModelTable.selectAll().where { ModelTable.id eq id.asString() }.singleOrNull()
            if (row == null) null else loadModelAggregate(row)
        }
    }

    override fun findModelByKeyOptional(key: ModelKey): Model? {
        return dbConnectionFactory.withExposed {
            val row = ModelTable.selectAll().where { ModelTable.key eq key.value }.singleOrNull()
            if (row == null) null else loadModel(row)
        }
    }

    override fun findModelByIdOptional(id: ModelId): Model? {
        return dbConnectionFactory.withExposed {
            val row = ModelTable.selectAll().where { ModelTable.id eq id.asString() }.singleOrNull()
            if (row == null) null else loadModel(row)
        }
    }

    override fun findTypeByKeyOptional(
        modelId: ModelId, key: TypeKey
    ): ModelType? {
        return dbConnectionFactory.withExposed {
            ModelTypeTable.selectAll().where {
                (ModelTypeTable.modelId eq modelId.asString()) and (ModelTypeTable.key eq key.value)
            }.singleOrNull()?.let { row -> toType(ModelTypeRecord.read(row)) }
        }
    }

    override fun findTypeByIdOptional(
        modelId: ModelId,
        typeId: TypeId
    ): ModelType? {
        return dbConnectionFactory.withExposed {
            ModelTypeTable.selectAll().where {
                (ModelTypeTable.modelId eq modelId.asString()) and (ModelTypeTable.id eq typeId.asString())
            }.singleOrNull()?.let { row -> toType(ModelTypeRecord.read(row)) }
        }
    }

    override fun isTypeUsedInEntityAttributes(
        modelId: ModelId,
        typeId: TypeId
    ): Boolean {
        return dbConnectionFactory.withExposed {
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
        return dbConnectionFactory.withExposed {
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

    override fun dispatch(cmd: ModelRepoCmd) {
        when (cmd) {
            //@formatter:off
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

    private fun createModel(model: ModelAggregate) {
        val inMemoryModel = ModelAggregateInMemory.of(model)
        dbConnectionFactory.withExposed {
            insertModel(inMemoryModel)
            insertModelTags(inMemoryModel.id, inMemoryModel.tags)
            searchWrite.upsertModelSearchItem(inMemoryModel.id)
        }
    }

    private fun deleteModel(modelId: ModelId) {
        dbConnectionFactory.withExposed {
            searchWrite.deleteModelBranch(modelId)
            ModelTable.deleteWhere { id eq modelId.asString() }
        }
    }

    private fun updateModelName(modelId: ModelId, name: LocalizedText) {
        dbConnectionFactory.withExposed {
            ModelTable.update(where = { ModelTable.id eq modelId.asString() }) { row ->
                row[ModelTable.name] = localizedTextToString(name)
            }
            searchWrite.upsertModelSearchItem(modelId)
        }
    }

    private fun updateModelDescription(modelId: ModelId, description: LocalizedMarkdown?) {
        dbConnectionFactory.withExposed {
            ModelTable.update(where = { ModelTable.id eq modelId.asString() }) { row ->
                row[ModelTable.description] = localizedMarkdownToString(description)
            }
            searchWrite.upsertModelSearchItem(modelId)
        }
    }

    private fun updateModelVersion(modelId: ModelId, version: ModelVersion) {
        dbConnectionFactory.withExposed {
            ModelTable.update(where = { ModelTable.id eq modelId.asString() }) { row ->
                row[ModelTable.version] = version.value
            }
        }
    }

    private fun updateModelDocumentationHome(modelId: ModelId, documentationHome: java.net.URL?) {
        dbConnectionFactory.withExposed {
            ModelTable.update(where = { ModelTable.id eq modelId.asString() }) { row ->
                row[ModelTable.documentationHome] = documentationHome?.toExternalForm()
            }
        }
    }

    private fun addModelTag(modelId: ModelId, tagId: TagId) {
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
            ModelTagTable.deleteWhere {
                (ModelTagTable.modelId eq modelId.asString()) and (ModelTagTable.tagId eq tagId.asString())
            }
            searchWrite.upsertModelSearchItem(modelId)
        }
    }

    private fun createType(modelId: ModelId, initializer: ModelTypeInitializer) {
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
            ModelTypeTable.update(
                where = {
                    (ModelTypeTable.id eq typeId.asString()) and (ModelTypeTable.modelId eq modelId.asString())
                }) { row ->
                row[ModelTypeTable.key] = value.asString()
            }
        }
    }

    private fun updateTypeName(modelId: ModelId, typeId: TypeId, value: LocalizedText?) {
        dbConnectionFactory.withExposed {
            ModelTypeTable.update(
                where = {
                    (ModelTypeTable.id eq typeId.asString()) and (ModelTypeTable.modelId eq modelId.asString())
                }) { row ->
                row[ModelTypeTable.name] = localizedTextToString(value)
            }
        }
    }

    private fun updateTypeDescription(modelId: ModelId, typeId: TypeId, value: LocalizedMarkdown?) {
        dbConnectionFactory.withExposed {
            ModelTypeTable.update(
                where = {
                    (ModelTypeTable.id eq typeId.asString()) and (ModelTypeTable.modelId eq modelId.asString())
                }) { row ->
                row[ModelTypeTable.description] = localizedMarkdownToString(value)
            }
        }
    }

    private fun deleteType(modelId: ModelId, typeId: TypeId) {
        dbConnectionFactory.withExposed {
            ModelTypeTable.deleteWhere {
                (ModelTypeTable.id eq typeId.asString()) and (ModelTypeTable.modelId eq modelId.asString())
            }
        }
    }

    private fun createEntity(cmd: ModelRepoCmd.CreateEntity) {
        dbConnectionFactory.withExposed {
            insertEntity(cmd)
        }
    }

    private fun updateEntityKey(modelId: ModelId, entityId: EntityId, value: EntityKey) {
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
            EntityTable.update(
                where = {
                    (EntityTable.id eq entityId.asString()) and (EntityTable.modelId eq modelId.asString())
                }) { row ->
                row[EntityTable.identifierAttributeId] = value.asString()
            }
        }
    }

    private fun updateEntityDocumentationHome(modelId: ModelId, entityId: EntityId, value: java.net.URL?) {
        dbConnectionFactory.withExposed {
            EntityTable.update(
                where = {
                    (EntityTable.id eq entityId.asString()) and (EntityTable.modelId eq modelId.asString())
                }) { row ->
                row[EntityTable.documentationHome] = value?.toExternalForm()
            }
        }
    }

    private fun addEntityTag(entityId: EntityId, tagId: TagId) {
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
            EntityTagTable.deleteWhere {
                (EntityTagTable.entityId eq entityId.asString()) and (EntityTagTable.tagId eq tagId.asString())
            }
            searchWrite.upsertEntitySearchItem(entityId)
        }
    }

    private fun deleteEntity(modelId: ModelId, entityId: EntityId) {
        dbConnectionFactory.withExposed {
            searchWrite.deleteEntityBranch(entityId)
            EntityTable.deleteWhere {
                (EntityTable.id eq entityId.asString()) and (EntityTable.modelId eq modelId.asString())
            }
        }
    }

    private fun createEntityAttribute(cmd: ModelRepoCmd.CreateEntityAttribute) {
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
            EntityAttributeTable.update(
                where = {
                    (EntityAttributeTable.id eq attributeId.asString()) and (EntityAttributeTable.entityId eq entityId.asString())
                }) { row ->
                row[EntityAttributeTable.typeId] = value.asString()
            }
        }
    }

    private fun updateEntityAttributeOptional(entityId: EntityId, attributeId: AttributeId, value: Boolean) {
        dbConnectionFactory.withExposed {
            EntityAttributeTable.update(
                where = {
                    (EntityAttributeTable.id eq attributeId.asString()) and (EntityAttributeTable.entityId eq entityId.asString())
                }) { row ->
                row[EntityAttributeTable.optional] = value
            }
        }
    }

    private fun addEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
            EntityAttributeTagTable.deleteWhere {
                (EntityAttributeTagTable.attributeId eq attributeId.asString()) and (EntityAttributeTagTable.tagId eq tagId.asString())
            }
            searchWrite.upsertEntityAttributeSearchItem(attributeId)
        }
    }

    private fun deleteEntityAttribute(entityId: EntityId, attributeId: AttributeId) {
        dbConnectionFactory.withExposed {
            searchWrite.deleteEntityAttributeSearchItem(attributeId)
            EntityAttributeTable.deleteWhere {
                (EntityAttributeTable.id eq attributeId.asString()) and (EntityAttributeTable.entityId eq entityId.asString())
            }
        }
    }

    private fun insertModel(model: ModelAggregateInMemory) {
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

    private fun insertEntityTags(entityId: EntityId, tags: List<TagId>) {
        for (tagId in tags) {
            EntityTagTable.insert { row ->
                row[EntityTagTable.entityId] = entityId.asString()
                row[EntityTagTable.tagId] = tagId.asString()
            }
        }
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
        dbConnectionFactory.withExposed {
            insertRelationship(cmd)
        }
    }

    private fun updateRelationshipKey(relationshipId: RelationshipId, value: RelationshipKey) {
        dbConnectionFactory.withExposed {
            RelationshipTable.update(where = { RelationshipTable.id eq relationshipId.asString() }) { row ->
                row[RelationshipTable.key] = value.asString()
            }
            searchWrite.upsertRelationshipSearchItem(relationshipId)
        }
    }

    private fun updateRelationshipName(relationshipId: RelationshipId, value: LocalizedText?) {
        dbConnectionFactory.withExposed {
            RelationshipTable.update(where = { RelationshipTable.id eq relationshipId.asString() }) { row ->
                row[RelationshipTable.name] = localizedTextToString(value)
            }
            searchWrite.upsertRelationshipSearchItem(relationshipId)
        }
    }

    private fun updateRelationshipDescription(relationshipId: RelationshipId, value: LocalizedMarkdown?) {
        dbConnectionFactory.withExposed {
            RelationshipTable.update(where = { RelationshipTable.id eq relationshipId.asString() }) { row ->
                row[RelationshipTable.description] = localizedMarkdownToString(value)
            }
            searchWrite.upsertRelationshipSearchItem(relationshipId)
        }
    }

    private fun updateRelationshipRoleKey(relationshipRoleId: RelationshipRoleId, value: RelationshipRoleKey) {
        dbConnectionFactory.withExposed {
            RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq relationshipRoleId.asString() }) { row ->
                row[RelationshipRoleTable.key] = value.asString()
            }
        }
    }

    private fun updateRelationshipRoleName(relationshipRoleId: RelationshipRoleId, value: LocalizedText?) {
        dbConnectionFactory.withExposed {
            RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq relationshipRoleId.asString() }) { row ->
                row[RelationshipRoleTable.name] = localizedTextToString(value)
            }
        }
    }

    private fun updateRelationshipRoleEntity(relationshipRoleId: RelationshipRoleId, value: EntityId) {
        dbConnectionFactory.withExposed {
            RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq relationshipRoleId.asString() }) { row ->
                row[RelationshipRoleTable.entityId] = value.asString()
            }
        }
    }

    private fun updateRelationshipRoleCardinality(
        relationshipRoleId: RelationshipRoleId, value: RelationshipCardinality
    ) {
        dbConnectionFactory.withExposed {
            RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq relationshipRoleId.asString() }) { row ->
                row[RelationshipRoleTable.cardinality] = value.code
            }
        }
    }

    private fun addRelationshipTag(relationshipId: RelationshipId, tagId: TagId) {
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
            RelationshipTagTable.deleteWhere {
                (RelationshipTagTable.relationshipId eq relationshipId.asString()) and (RelationshipTagTable.tagId eq tagId.asString())
            }
            searchWrite.upsertRelationshipSearchItem(relationshipId)
        }
    }

    private fun deleteRelationship(modelId: ModelId, relationshipId: RelationshipId) {
        dbConnectionFactory.withExposed {
            searchWrite.deleteRelationshipBranch(relationshipId)
            RelationshipTable.deleteWhere {
                (RelationshipTable.id eq relationshipId.asString()) and (RelationshipTable.modelId eq modelId.asString())
            }
        }
    }

    private fun createRelationshipAttribute(cmd: ModelRepoCmd.CreateRelationshipAttribute) {
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
            RelationshipAttributeTable.update(
                where = {
                    (RelationshipAttributeTable.id eq attributeId.asString()) and (RelationshipAttributeTable.relationshipId eq relationshipId.asString())
                }) { row ->
                row[RelationshipAttributeTable.optional] = value
            }
        }
    }

    private fun addRelationshipAttributeTag(attributeId: AttributeId, tagId: TagId) {
        dbConnectionFactory.withExposed {
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
        dbConnectionFactory.withExposed {
            RelationshipAttributeTagTable.deleteWhere {
                (RelationshipAttributeTagTable.attributeId eq attributeId.asString()) and (RelationshipAttributeTagTable.tagId eq tagId.asString())
            }
            searchWrite.upsertRelationshipAttributeSearchItem(attributeId)
        }
    }

    private fun deleteRelationshipAttribute(relationshipId: RelationshipId, attributeId: AttributeId) {
        dbConnectionFactory.withExposed {
            searchWrite.deleteRelationshipAttributeSearchItem(attributeId)
            RelationshipAttributeTable.deleteWhere {
                (RelationshipAttributeTable.id eq attributeId.asString()) and (RelationshipAttributeTable.relationshipId eq relationshipId.asString())
            }
        }
    }

    private fun insertRelationship(cmd:ModelRepoCmd.CreateRelationship) {
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
        val relationships = loadRelationships(modelId)

        return ModelAggregateInMemory(
            model = ModelInMemory(
                id = modelId,
                key = ModelKey(record.key),
                name = stringToLocalizedText(record.name),
                description = stringToLocalizedMarkdown(record.description),
                version = ModelVersion(record.version),
                origin = stringToModelOrigin(record.origin),
                documentationHome = record.documentationHome?.let { URI(it).toURL() },
            ), types = types, entities = entities, relationships = relationships, tags = loadModelTags(modelId)
        )
    }

    private fun loadModel(row: ResultRow): ModelInMemory {
        val record = ModelRecord.read(row)
        val modelId = ModelId.fromString(record.id)
        return ModelInMemory(
            id = modelId,
            key = ModelKey(record.key),
            name = stringToLocalizedText(record.name),
            description = stringToLocalizedMarkdown(record.description),
            version = ModelVersion(record.version),
            origin = stringToModelOrigin(record.origin),
            documentationHome = record.documentationHome?.let { URI(it).toURL() },
        )
    }

    private fun loadTypes(modelId: ModelId): List<ModelTypeInMemory> {
        return ModelTypeTable.selectAll().where { ModelTypeTable.modelId eq modelId.asString() }
            .orderBy(ModelTypeTable.key to SortOrder.ASC).map { row ->
                val record = ModelTypeRecord.read(row)
                toType(record)
            }
    }

    private fun toType(record: ModelTypeRecord): ModelTypeInMemory = ModelTypeInMemory(
        id = TypeId.fromString(record.id),
        key = TypeKey(record.key),
        name = stringToLocalizedText(record.name),
        description = stringToLocalizedMarkdown(record.description)
    )

    private fun loadEntities(modelId: ModelId): List<EntityInMemory> {
        val attributeRows = EntityAttributeTable.selectAll().where {
            EntityAttributeTable.entityId inSubQuery EntityTable.select(EntityTable.id)
                .where { EntityTable.modelId eq modelId.asString() }
        }.orderBy(EntityAttributeTable.key to SortOrder.ASC).toList()
        val attributeRowsByEntityId = attributeRows.groupBy { it[EntityAttributeTable.entityId] }

        return EntityTable.selectAll().where { EntityTable.modelId eq modelId.asString() }
            .orderBy(EntityTable.key to SortOrder.ASC).map { row ->
                val record = EntityRecord.read(row)
                val entityId = EntityId.fromString(record.id)
                val attributes = (attributeRowsByEntityId[entityId.asString()] ?: emptyList()).map { attrRow ->
                    entityAttributeFromRow(attrRow)
                }
                val identifierAttributeIdString = record.identifierAttributeId
                    ?: throw ModelStorageDbInvalidIdentifierAttributeException(entityId.asString())

                EntityInMemory(
                    id = entityId,
                    key = EntityKey(record.key),
                    name = stringToLocalizedText(record.name),
                    description = stringToLocalizedMarkdown(record.description),
                    identifierAttributeId = AttributeId.fromString(identifierAttributeIdString),
                    origin = stringToEntityOrigin(record.origin),
                    attributes = attributes,
                    documentationHome = record.documentationHome?.let { URI(it).toURL() },
                    tags = loadEntityTags(entityId)
                )
            }
    }

    private fun loadRelationships(modelId: ModelId): List<RelationshipInMemory> {
        val relationshipIds =
            RelationshipTable.select(RelationshipTable.id).where { RelationshipTable.modelId eq modelId.asString() }

        val roleRowsByRelationshipId =
            RelationshipRoleTable.selectAll().where { RelationshipRoleTable.relationshipId inSubQuery relationshipIds }
                .orderBy(RelationshipRoleTable.key to SortOrder.ASC).toList()
                .groupBy { it[RelationshipRoleTable.relationshipId] }

        val attributeRowsByRelationshipId = RelationshipAttributeTable.selectAll()
            .where { RelationshipAttributeTable.relationshipId inSubQuery relationshipIds }
            .orderBy(RelationshipAttributeTable.key to SortOrder.ASC).toList()
            .groupBy { it[RelationshipAttributeTable.relationshipId] }

        return RelationshipTable.selectAll().where { RelationshipTable.modelId eq modelId.asString() }
            .orderBy(RelationshipTable.key to SortOrder.ASC).map { row ->
                val record = RelationshipRecord.read(row)
                val relationshipId = RelationshipId.fromString(record.id)
                RelationshipInMemory(
                    id = relationshipId,
                    key = RelationshipKey(record.key),
                    name = stringToLocalizedText(record.name),
                    description = stringToLocalizedMarkdown(record.description),
                    roles = (roleRowsByRelationshipId[relationshipId.asString()] ?: emptyList()).map { roleRow ->
                        relationshipRoleFromRow(roleRow)
                    },
                    attributes = (attributeRowsByRelationshipId[relationshipId.asString()]
                        ?: emptyList()).map { attrRow ->
                        relationshipAttributeFromRow(attrRow)
                    },
                    tags = loadRelationshipTags(relationshipId)
                )
            }
    }

    private fun entityAttributeFromRow(row: ResultRow): AttributeInMemory {
        val record = EntityAttributeRecord.read(row)
        val attributeId = AttributeId.fromString(record.id)
        return AttributeInMemory(
            id = attributeId,
            key = AttributeKey(record.key),
            name = stringToLocalizedText(record.name),
            description = stringToLocalizedMarkdown(record.description),
            typeId = TypeId.fromString(record.typeId),
            optional = record.optional,
            tags = loadEntityAttributeTags(attributeId)
        )
    }

    private fun relationshipAttributeFromRow(row: ResultRow): AttributeInMemory {
        val record = RelationshipAttributeRecord.read(row)
        val attributeId = AttributeId.fromString(record.id)
        return AttributeInMemory(
            id = attributeId,
            key = AttributeKey(record.key),
            name = stringToLocalizedText(record.name),
            description = stringToLocalizedMarkdown(record.description),
            typeId = TypeId.fromString(record.typeId),
            optional = record.optional,
            tags = loadRelationshipAttributeTags(attributeId)
        )
    }

    private fun relationshipRoleFromRow(row: ResultRow): RelationshipRoleInMemory {
        val record = RelationshipRoleRecord.read(row)
        return RelationshipRoleInMemory(
            id = RelationshipRoleId.fromString(record.id),
            key = RelationshipRoleKey(record.key),
            entityId = EntityId.fromString(record.entityId),
            name = stringToLocalizedText(record.name),
            cardinality = RelationshipCardinality.valueOfCode(record.cardinality)
        )
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

    private fun stringToLocalizedText(value: String?): LocalizedText? {
        return if (value == null) null else LocalizedTextNotLocalized(value)
    }

    private fun stringToLocalizedMarkdown(value: String?): LocalizedMarkdown? {
        return if (value == null) null else LocalizedMarkdownNotLocalized(value)
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

    private fun stringToModelOrigin(origin: String?): ModelOrigin {
        return if (origin == null) ModelOrigin.Manual else ModelOrigin.Uri(URI(origin))
    }

    private fun stringToEntityOrigin(origin: String?): EntityOrigin {
        return if (origin == null) EntityOrigin.Manual else EntityOrigin.Uri(URI(origin))
    }

}
