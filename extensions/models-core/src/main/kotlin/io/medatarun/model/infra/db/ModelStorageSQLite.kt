package io.medatarun.model.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import io.medatarun.model.ports.exposed.ModelTypeInitializer
import io.medatarun.model.ports.exposed.ModelTypeUpdateCmd
import io.medatarun.model.ports.needs.ModelRepoCmdAttributeUpdate
import io.medatarun.model.ports.needs.ModelRepoCmdEntityUpdate
import io.medatarun.model.ports.needs.ModelRepoCmdRelationshipUpdate
import io.medatarun.model.ports.needs.ModelRepoCmd
import io.medatarun.model.ports.needs.ModelStorage
import io.medatarun.model.ports.needs.ModelRepositoryId
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.tags.core.domain.TagId
import io.medatarun.type.commons.id.Id
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
import java.net.URI

class ModelStorageSQLite(
    private val dbConnectionFactory: DbConnectionFactory
) : ModelStorage {

    override fun matchesId(id: ModelRepositoryId): Boolean {
        return id == REPOSITORY_ID
    }

    override fun findAllModelIds(): List<ModelId> {
        return dbConnectionFactory.withExposed {
            ModelTable.selectAll()
                .map { ModelId.fromString(it[ModelTable.id]) }
        }
    }

    override fun existsModelByKey(key: ModelKey): Boolean {
        return dbConnectionFactory.withExposed {
            ModelTable.select(ModelTable.id)
                .where { ModelTable.key eq key.value }
                .limit(1)
                .any()
        }
    }

    override fun existsModelById(id: ModelId): Boolean {
        return dbConnectionFactory.withExposed {
            ModelTable.select(ModelTable.id)
                .where { ModelTable.id eq id.asString() }
                .limit(1)
                .any()
        }
    }

    override fun findModelByKeyOptional(key: ModelKey): Model? {
        return dbConnectionFactory.withExposed {
            val row = ModelTable.selectAll()
                .where { ModelTable.key eq key.value }
                .singleOrNull()
            if (row == null) null else loadModel(row)
        }
    }

    override fun findModelByIdOptional(id: ModelId): Model? {
        return dbConnectionFactory.withExposed {
            val row = ModelTable.selectAll()
                .where { ModelTable.id eq id.asString() }
                .singleOrNull()
            if (row == null) null else loadModel(row)
        }
    }

    override fun dispatch(cmd: ModelRepoCmd) {
        when (cmd) {
            is ModelRepoCmd.CreateModel -> createModel(cmd.model)
            is ModelRepoCmd.DeleteModel -> deleteModel(cmd.modelId)
            is ModelRepoCmd.UpdateModelName -> updateModelName(cmd.modelId, cmd.name)
            is ModelRepoCmd.UpdateModelDescription -> updateModelDescription(cmd.modelId, cmd.description)
            is ModelRepoCmd.UpdateModelVersion -> updateModelVersion(cmd.modelId, cmd.version)
            is ModelRepoCmd.UpdateModelDocumentationHome -> updateModelDocumentationHome(cmd.modelId, cmd.url)
            is ModelRepoCmd.UpdateModelTagAdd -> addModelTag(cmd.modelId, cmd.tagId)
            is ModelRepoCmd.UpdateModelTagDelete -> deleteModelTag(cmd.modelId, cmd.tagId)
            is ModelRepoCmd.CreateType -> createType(cmd.modelId, cmd.initializer)
            is ModelRepoCmd.UpdateType -> updateType(cmd.modelId, cmd.typeId, cmd.cmd)
            is ModelRepoCmd.DeleteType -> deleteType(cmd.modelId, cmd.typeId)
            is ModelRepoCmd.CreateEntity -> createEntity(cmd.modelId, cmd.entity)
            is ModelRepoCmd.UpdateEntity -> updateEntity(cmd.modelId, cmd.entityId, cmd.cmd)
            is ModelRepoCmd.UpdateEntityTagAdd -> addEntityTag(cmd.entityId, cmd.tagId)
            is ModelRepoCmd.UpdateEntityTagDelete -> deleteEntityTag(cmd.entityId, cmd.tagId)
            is ModelRepoCmd.DeleteEntity -> deleteEntity(cmd.modelId, cmd.entityId)
            is ModelRepoCmd.CreateEntityAttribute -> createEntityAttribute(cmd.entityId, cmd.attribute)
            is ModelRepoCmd.UpdateEntityAttribute -> updateEntityAttribute(cmd.entityId, cmd.attributeId, cmd.cmd)
            is ModelRepoCmd.UpdateEntityAttributeTagAdd -> addEntityAttributeTag(cmd.attributeId, cmd.tagId)
            is ModelRepoCmd.UpdateEntityAttributeTagDelete -> deleteEntityAttributeTag(cmd.attributeId, cmd.tagId)
            is ModelRepoCmd.DeleteEntityAttribute -> deleteEntityAttribute(cmd.entityId, cmd.attributeId)
            is ModelRepoCmd.CreateRelationship -> createRelationship(cmd.modelId, cmd.initializer)
            is ModelRepoCmd.UpdateRelationship -> updateRelationship(cmd.relationshipId, cmd.cmd)
            is ModelRepoCmd.UpdateRelationshipTagAdd -> addRelationshipTag(cmd.relationshipId, cmd.tagId)
            is ModelRepoCmd.UpdateRelationshipTagDelete -> deleteRelationshipTag(cmd.relationshipId, cmd.tagId)
            is ModelRepoCmd.DeleteRelationship -> deleteRelationship(cmd.modelId, cmd.relationshipId)
            is ModelRepoCmd.CreateRelationshipAttribute -> createRelationshipAttribute(cmd.relationshipId, cmd.attr)
            is ModelRepoCmd.UpdateRelationshipAttribute -> {
                updateRelationshipAttribute(cmd.relationshipId, cmd.attributeId, cmd.cmd)
            }

            is ModelRepoCmd.UpdateRelationshipAttributeTagAdd -> {
                addRelationshipAttributeTag(cmd.attributeId, cmd.tagId)
            }

            is ModelRepoCmd.UpdateRelationshipAttributeTagDelete -> {
                deleteRelationshipAttributeTag(cmd.attributeId, cmd.tagId)
            }

            is ModelRepoCmd.DeleteRelationshipAttribute -> {
                deleteRelationshipAttribute(cmd.relationshipId, cmd.attributeId)
            }
        }
    }

    private fun createModel(model: Model) {
        val inMemoryModel = ModelInMemory.of(model)
        dbConnectionFactory.withExposed {
            insertModel(inMemoryModel)
            insertModelTags(inMemoryModel.id, inMemoryModel.tags)
        }
    }

    private fun deleteModel(modelId: ModelId) {
        dbConnectionFactory.withExposed {
            ModelTable.deleteWhere { id eq modelId.asString() }
        }
    }

    private fun updateModelName(modelId: ModelId, name: LocalizedText) {
        dbConnectionFactory.withExposed {
            ModelTable.update(where = { ModelTable.id eq modelId.asString() }) { row ->
                row[ModelTable.name] = localizedTextToString(name)
            }
        }
    }

    private fun updateModelDescription(modelId: ModelId, description: LocalizedMarkdown?) {
        dbConnectionFactory.withExposed {
            ModelTable.update(where = { ModelTable.id eq modelId.asString() }) { row ->
                row[ModelTable.description] = localizedMarkdownToString(description)
            }
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
            val exists = ModelTagTable.select(ModelTagTable.modelId)
                .where {
                    (ModelTagTable.modelId eq modelId.asString()) and
                        (ModelTagTable.tagId eq tagId.asString())
                }
                .limit(1)
                .any()
            if (!exists) {
                ModelTagTable.insert { row ->
                    row[ModelTagTable.modelId] = modelId.asString()
                    row[ModelTagTable.tagId] = tagId.asString()
                }
            }
        }
    }

    private fun deleteModelTag(modelId: ModelId, tagId: TagId) {
        dbConnectionFactory.withExposed {
            ModelTagTable.deleteWhere {
                (ModelTagTable.modelId eq modelId.asString()) and
                    (ModelTagTable.tagId eq tagId.asString())
            }
        }
    }

    private fun createType(modelId: ModelId, initializer: ModelTypeInitializer) {
        dbConnectionFactory.withExposed {
            ModelTypeTable.insert { row ->
                row[ModelTypeTable.id] = TypeId.generate().asString()
                row[ModelTypeTable.modelId] = modelId.asString()
                row[ModelTypeTable.key] = initializer.id.asString()
                row[ModelTypeTable.name] = localizedTextToString(initializer.name)
                row[ModelTypeTable.description] = localizedMarkdownToString(initializer.description)
            }
        }
    }

    private fun updateType(modelId: ModelId, typeId: TypeId, cmd: ModelTypeUpdateCmd) {
        dbConnectionFactory.withExposed {
            ModelTypeTable.update(
                where = {
                    (ModelTypeTable.id eq typeId.asString()) and
                        (ModelTypeTable.modelId eq modelId.asString())
                }
            ) { row ->
                when (cmd) {
                    is ModelTypeUpdateCmd.Name -> {
                        row[ModelTypeTable.name] = localizedTextToString(cmd.value)
                    }

                    is ModelTypeUpdateCmd.Description -> {
                        row[ModelTypeTable.description] = localizedMarkdownToString(cmd.value)
                    }

                    is ModelTypeUpdateCmd.Key -> {
                        row[ModelTypeTable.key] = cmd.value.asString()
                    }
                }
            }
        }
    }

    private fun deleteType(modelId: ModelId, typeId: TypeId) {
        dbConnectionFactory.withExposed {
            ModelTypeTable.deleteWhere {
                (ModelTypeTable.id eq typeId.asString()) and
                    (ModelTypeTable.modelId eq modelId.asString())
            }
        }
    }

    private fun createEntity(modelId: ModelId, entity: EntityInMemory) {
        dbConnectionFactory.withExposed {
            insertEntity(modelId, entity)
        }
    }

    private fun updateEntity(modelId: ModelId, entityId: EntityId, cmd: ModelRepoCmdEntityUpdate) {
        dbConnectionFactory.withExposed {
            EntityTable.update(
                where = {
                    (EntityTable.id eq entityId.asString()) and
                        (EntityTable.modelId eq modelId.asString())
                }
            ) { row ->
                when (cmd) {
                    is ModelRepoCmdEntityUpdate.Key -> row[EntityTable.key] = cmd.value.asString()
                    is ModelRepoCmdEntityUpdate.Name -> row[EntityTable.name] = localizedTextToString(cmd.value)
                    is ModelRepoCmdEntityUpdate.Description -> {
                        row[EntityTable.description] = localizedMarkdownToString(cmd.value)
                    }

                    is ModelRepoCmdEntityUpdate.IdentifierAttribute -> {
                        row[EntityTable.identifierAttributeId] = cmd.value.asString()
                    }

                    is ModelRepoCmdEntityUpdate.DocumentationHome -> {
                        row[EntityTable.documentationHome] = cmd.value?.toExternalForm()
                    }
                }
            }
        }
    }

    private fun addEntityTag(entityId: EntityId, tagId: TagId) {
        dbConnectionFactory.withExposed {
            val exists = EntityTagTable.select(EntityTagTable.entityId)
                .where {
                    (EntityTagTable.entityId eq entityId.asString()) and
                        (EntityTagTable.tagId eq tagId.asString())
                }
                .limit(1)
                .any()
            if (!exists) {
                EntityTagTable.insert { row ->
                    row[EntityTagTable.entityId] = entityId.asString()
                    row[EntityTagTable.tagId] = tagId.asString()
                }
            }
        }
    }

    private fun deleteEntityTag(entityId: EntityId, tagId: TagId) {
        dbConnectionFactory.withExposed {
            EntityTagTable.deleteWhere {
                (EntityTagTable.entityId eq entityId.asString()) and
                    (EntityTagTable.tagId eq tagId.asString())
            }
        }
    }

    private fun deleteEntity(modelId: ModelId, entityId: EntityId) {
        dbConnectionFactory.withExposed {
            EntityTable.deleteWhere {
                (EntityTable.id eq entityId.asString()) and
                    (EntityTable.modelId eq modelId.asString())
            }
        }
    }

    private fun createEntityAttribute(entityId: EntityId, attribute: Attribute) {
        dbConnectionFactory.withExposed {
            insertEntityAttribute(entityId, AttributeInMemory.of(attribute))
        }
    }

    private fun updateEntityAttribute(entityId: EntityId, attributeId: AttributeId, cmd: ModelRepoCmdAttributeUpdate) {
        dbConnectionFactory.withExposed {
            EntityAttributeTable.update(
                where = {
                    (EntityAttributeTable.id eq attributeId.asString()) and
                        (EntityAttributeTable.entityId eq entityId.asString())
                }
            ) { row ->
                when (cmd) {
                    is ModelRepoCmdAttributeUpdate.Key -> row[EntityAttributeTable.key] = cmd.value.asString()
                    is ModelRepoCmdAttributeUpdate.Name -> row[EntityAttributeTable.name] = localizedTextToString(cmd.value)
                    is ModelRepoCmdAttributeUpdate.Description -> {
                        row[EntityAttributeTable.description] = localizedMarkdownToString(cmd.value)
                    }

                    is ModelRepoCmdAttributeUpdate.Type -> row[EntityAttributeTable.typeId] = cmd.value.asString()
                    is ModelRepoCmdAttributeUpdate.Optional -> row[EntityAttributeTable.optional] = cmd.value
                }
            }
        }
    }

    private fun addEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        dbConnectionFactory.withExposed {
            val exists = EntityAttributeTagTable.select(EntityAttributeTagTable.attributeId)
                .where {
                    (EntityAttributeTagTable.attributeId eq attributeId.asString()) and
                        (EntityAttributeTagTable.tagId eq tagId.asString())
                }
                .limit(1)
                .any()
            if (!exists) {
                EntityAttributeTagTable.insert { row ->
                    row[EntityAttributeTagTable.attributeId] = attributeId.asString()
                    row[EntityAttributeTagTable.tagId] = tagId.asString()
                }
            }
        }
    }

    private fun deleteEntityAttributeTag(attributeId: AttributeId, tagId: TagId) {
        dbConnectionFactory.withExposed {
            EntityAttributeTagTable.deleteWhere {
                (EntityAttributeTagTable.attributeId eq attributeId.asString()) and
                    (EntityAttributeTagTable.tagId eq tagId.asString())
            }
        }
    }

    private fun deleteEntityAttribute(entityId: EntityId, attributeId: AttributeId) {
        dbConnectionFactory.withExposed {
            EntityAttributeTable.deleteWhere {
                (EntityAttributeTable.id eq attributeId.asString()) and
                    (EntityAttributeTable.entityId eq entityId.asString())
            }
        }
    }

    private fun insertModel(model: ModelInMemory) {
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

    private fun insertEntity(modelId: ModelId, entity: EntityInMemory) {
        EntityTable.insert { row ->
            row[EntityTable.id] = entity.id.asString()
            row[EntityTable.modelId] = modelId.asString()
            row[EntityTable.key] = entity.key.asString()
            row[EntityTable.name] = localizedTextToString(entity.name)
            row[EntityTable.description] = localizedMarkdownToString(entity.description)
            row[EntityTable.identifierAttributeId] = null
            row[EntityTable.origin] = entityOriginToString(entity.origin)
            row[EntityTable.documentationHome] = entity.documentationHome?.toExternalForm()
        }

        insertEntityTags(entity.id, entity.tags)

        for (attribute in entity.attributes) {
            insertEntityAttribute(entity.id, attribute)
        }

        EntityTable.update(where = { EntityTable.id eq entity.id.asString() }) { row ->
            row[EntityTable.identifierAttributeId] = entity.identifierAttributeId.asString()
        }
    }

    private fun insertEntityTags(entityId: EntityId, tags: List<TagId>) {
        for (tagId in tags) {
            EntityTagTable.insert { row ->
                row[EntityTagTable.entityId] = entityId.asString()
                row[EntityTagTable.tagId] = tagId.asString()
            }
        }
    }

    private fun insertEntityAttribute(entityId: EntityId, attribute: AttributeInMemory) {
        EntityAttributeTable.insert { row ->
            row[EntityAttributeTable.id] = attribute.id.asString()
            row[EntityAttributeTable.entityId] = entityId.asString()
            row[EntityAttributeTable.key] = attribute.key.asString()
            row[EntityAttributeTable.name] = localizedTextToString(attribute.name)
            row[EntityAttributeTable.description] = localizedMarkdownToString(attribute.description)
            row[EntityAttributeTable.typeId] = attribute.typeId.asString()
            row[EntityAttributeTable.optional] = attribute.optional
        }

        insertEntityAttributeTags(attribute.id, attribute.tags)
    }

    private fun insertEntityAttributeTags(attributeId: AttributeId, tags: List<TagId>) {
        for (tagId in tags) {
            EntityAttributeTagTable.insert { row ->
                row[EntityAttributeTagTable.attributeId] = attributeId.asString()
                row[EntityAttributeTagTable.tagId] = tagId.asString()
            }
        }
    }

    private fun createRelationship(modelId: ModelId, relationship: Relationship) {
        dbConnectionFactory.withExposed {
            insertRelationship(modelId, RelationshipInMemory.of(relationship))
        }
    }

    private fun updateRelationship(relationshipId: RelationshipId, cmd: ModelRepoCmdRelationshipUpdate) {
        dbConnectionFactory.withExposed {
            when (cmd) {
                is ModelRepoCmdRelationshipUpdate.Key -> {
                    RelationshipTable.update(where = { RelationshipTable.id eq relationshipId.asString() }) { row ->
                        row[RelationshipTable.key] = cmd.value.asString()
                    }
                }

                is ModelRepoCmdRelationshipUpdate.Name -> {
                    RelationshipTable.update(where = { RelationshipTable.id eq relationshipId.asString() }) { row ->
                        row[RelationshipTable.name] = localizedTextToString(cmd.value)
                    }
                }

                is ModelRepoCmdRelationshipUpdate.Description -> {
                    RelationshipTable.update(where = { RelationshipTable.id eq relationshipId.asString() }) { row ->
                        row[RelationshipTable.description] = localizedMarkdownToString(cmd.value)
                    }
                }

                is ModelRepoCmdRelationshipUpdate.RoleKey -> {
                    RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq cmd.relationshipRoleId.asString() }) { row ->
                        row[RelationshipRoleTable.key] = cmd.value.asString()
                    }
                }

                is ModelRepoCmdRelationshipUpdate.RoleName -> {
                    RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq cmd.relationshipRoleId.asString() }) { row ->
                        row[RelationshipRoleTable.name] = localizedTextToString(cmd.value)
                    }
                }

                is ModelRepoCmdRelationshipUpdate.RoleEntity -> {
                    RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq cmd.relationshipRoleId.asString() }) { row ->
                        row[RelationshipRoleTable.entityId] = cmd.value.asString()
                    }
                }

                is ModelRepoCmdRelationshipUpdate.RoleCardinality -> {
                    RelationshipRoleTable.update(where = { RelationshipRoleTable.id eq cmd.relationshipRoleId.asString() }) { row ->
                        row[RelationshipRoleTable.cardinality] = cmd.value.code
                    }
                }
            }
        }
    }

    private fun addRelationshipTag(relationshipId: RelationshipId, tagId: TagId) {
        dbConnectionFactory.withExposed {
            val exists = RelationshipTagTable.select(RelationshipTagTable.relationshipId)
                .where {
                    (RelationshipTagTable.relationshipId eq relationshipId.asString()) and
                        (RelationshipTagTable.tagId eq tagId.asString())
                }
                .limit(1)
                .any()
            if (!exists) {
                RelationshipTagTable.insert { row ->
                    row[RelationshipTagTable.relationshipId] = relationshipId.asString()
                    row[RelationshipTagTable.tagId] = tagId.asString()
                }
            }
        }
    }

    private fun deleteRelationshipTag(relationshipId: RelationshipId, tagId: TagId) {
        dbConnectionFactory.withExposed {
            RelationshipTagTable.deleteWhere {
                (RelationshipTagTable.relationshipId eq relationshipId.asString()) and
                    (RelationshipTagTable.tagId eq tagId.asString())
            }
        }
    }

    private fun deleteRelationship(modelId: ModelId, relationshipId: RelationshipId) {
        dbConnectionFactory.withExposed {
            RelationshipTable.deleteWhere {
                (RelationshipTable.id eq relationshipId.asString()) and
                    (RelationshipTable.modelId eq modelId.asString())
            }
        }
    }

    private fun createRelationshipAttribute(relationshipId: RelationshipId, attribute: Attribute) {
        dbConnectionFactory.withExposed {
            insertRelationshipAttribute(relationshipId, AttributeInMemory.of(attribute))
        }
    }

    private fun updateRelationshipAttribute(
        relationshipId: RelationshipId,
        attributeId: AttributeId,
        cmd: ModelRepoCmdAttributeUpdate
    ) {
        dbConnectionFactory.withExposed {
            RelationshipAttributeTable.update(
                where = {
                    (RelationshipAttributeTable.id eq attributeId.asString()) and
                        (RelationshipAttributeTable.relationshipId eq relationshipId.asString())
                }
            ) { row ->
                when (cmd) {
                    is ModelRepoCmdAttributeUpdate.Key -> row[RelationshipAttributeTable.key] = cmd.value.asString()
                    is ModelRepoCmdAttributeUpdate.Name -> row[RelationshipAttributeTable.name] = localizedTextToString(cmd.value)
                    is ModelRepoCmdAttributeUpdate.Description -> {
                        row[RelationshipAttributeTable.description] = localizedMarkdownToString(cmd.value)
                    }

                    is ModelRepoCmdAttributeUpdate.Type -> row[RelationshipAttributeTable.typeId] = cmd.value.asString()
                    is ModelRepoCmdAttributeUpdate.Optional -> row[RelationshipAttributeTable.optional] = cmd.value
                }
            }
        }
    }

    private fun addRelationshipAttributeTag(attributeId: AttributeId, tagId: TagId) {
        dbConnectionFactory.withExposed {
            val exists = RelationshipAttributeTagTable.select(RelationshipAttributeTagTable.attributeId)
                .where {
                    (RelationshipAttributeTagTable.attributeId eq attributeId.asString()) and
                        (RelationshipAttributeTagTable.tagId eq tagId.asString())
                }
                .limit(1)
                .any()
            if (!exists) {
                RelationshipAttributeTagTable.insert { row ->
                    row[RelationshipAttributeTagTable.attributeId] = attributeId.asString()
                    row[RelationshipAttributeTagTable.tagId] = tagId.asString()
                }
            }
        }
    }

    private fun deleteRelationshipAttributeTag(attributeId: AttributeId, tagId: TagId) {
        dbConnectionFactory.withExposed {
            RelationshipAttributeTagTable.deleteWhere {
                (RelationshipAttributeTagTable.attributeId eq attributeId.asString()) and
                    (RelationshipAttributeTagTable.tagId eq tagId.asString())
            }
        }
    }

    private fun deleteRelationshipAttribute(relationshipId: RelationshipId, attributeId: AttributeId) {
        dbConnectionFactory.withExposed {
            RelationshipAttributeTable.deleteWhere {
                (RelationshipAttributeTable.id eq attributeId.asString()) and
                    (RelationshipAttributeTable.relationshipId eq relationshipId.asString())
            }
        }
    }

    private fun insertRelationship(modelId: ModelId, relationship: RelationshipInMemory) {
        RelationshipTable.insert { row ->
            row[RelationshipTable.id] = relationship.id.asString()
            row[RelationshipTable.modelId] = modelId.asString()
            row[RelationshipTable.key] = relationship.key.asString()
            row[RelationshipTable.name] = localizedTextToString(relationship.name)
            row[RelationshipTable.description] = localizedMarkdownToString(relationship.description)
        }

        insertRelationshipTags(relationship.id, relationship.tags)

        for (role in relationship.roles) {
            RelationshipRoleTable.insert { row ->
                row[RelationshipRoleTable.id] = role.id.asString()
                row[RelationshipRoleTable.relationshipId] = relationship.id.asString()
                row[RelationshipRoleTable.key] = role.key.asString()
                row[RelationshipRoleTable.entityId] = role.entityId.asString()
                row[RelationshipRoleTable.name] = localizedTextToString(role.name)
                row[RelationshipRoleTable.cardinality] = role.cardinality.code
            }
        }

        for (attribute in relationship.attributes) {
            insertRelationshipAttribute(relationship.id, attribute)
        }
    }

    private fun insertRelationshipTags(relationshipId: RelationshipId, tags: List<TagId>) {
        for (tagId in tags) {
            RelationshipTagTable.insert { row ->
                row[RelationshipTagTable.relationshipId] = relationshipId.asString()
                row[RelationshipTagTable.tagId] = tagId.asString()
            }
        }
    }

    private fun insertRelationshipAttribute(relationshipId: RelationshipId, attribute: AttributeInMemory) {
        RelationshipAttributeTable.insert { row ->
            row[RelationshipAttributeTable.id] = attribute.id.asString()
            row[RelationshipAttributeTable.relationshipId] = relationshipId.asString()
            row[RelationshipAttributeTable.key] = attribute.key.asString()
            row[RelationshipAttributeTable.name] = localizedTextToString(attribute.name)
            row[RelationshipAttributeTable.description] = localizedMarkdownToString(attribute.description)
            row[RelationshipAttributeTable.typeId] = attribute.typeId.asString()
            row[RelationshipAttributeTable.optional] = attribute.optional
        }

        insertRelationshipAttributeTags(attribute.id, attribute.tags)
    }

    private fun insertRelationshipAttributeTags(attributeId: AttributeId, tags: List<TagId>) {
        for (tagId in tags) {
            RelationshipAttributeTagTable.insert { row ->
                row[RelationshipAttributeTagTable.attributeId] = attributeId.asString()
                row[RelationshipAttributeTagTable.tagId] = tagId.asString()
            }
        }
    }

    private fun loadModel(row: ResultRow): ModelInMemory {
        val modelId = ModelId.fromString(row[ModelTable.id])
        val types = loadTypes(modelId)
        val entities = loadEntities(modelId)
        val relationships = loadRelationships(modelId)

        return ModelInMemory(
            id = modelId,
            key = ModelKey(row[ModelTable.key]),
            name = stringToLocalizedText(row[ModelTable.name]),
            description = stringToLocalizedMarkdown(row[ModelTable.description]),
            version = ModelVersion(row[ModelTable.version]),
            origin = stringToModelOrigin(row[ModelTable.origin]),
            types = types,
            entities = entities,
            relationships = relationships,
            documentationHome = row[ModelTable.documentationHome]?.let { URI(it).toURL() },
            tags = loadModelTags(modelId)
        )
    }

    private fun loadTypes(modelId: ModelId): List<ModelTypeInMemory> {
        return ModelTypeTable.selectAll()
            .where { ModelTypeTable.modelId eq modelId.asString() }
            .orderBy(ModelTypeTable.key to SortOrder.ASC)
            .map { row ->
                ModelTypeInMemory(
                    id = TypeId.fromString(row[ModelTypeTable.id]),
                    key = TypeKey(row[ModelTypeTable.key]),
                    name = stringToLocalizedText(row[ModelTypeTable.name]),
                    description = stringToLocalizedMarkdown(row[ModelTypeTable.description])
                )
            }
    }

    private fun loadEntities(modelId: ModelId): List<EntityInMemory> {
        val attributeRows = EntityAttributeTable.selectAll()
            .where {
                EntityAttributeTable.entityId inSubQuery EntityTable.select(EntityTable.id)
                    .where { EntityTable.modelId eq modelId.asString() }
            }
            .orderBy(EntityAttributeTable.key to SortOrder.ASC)
            .toList()
        val attributeRowsByEntityId = attributeRows.groupBy { it[EntityAttributeTable.entityId] }

        return EntityTable.selectAll()
            .where { EntityTable.modelId eq modelId.asString() }
            .orderBy(EntityTable.key to SortOrder.ASC)
            .map { row ->
                val entityId = EntityId.fromString(row[EntityTable.id])
                val attributes = (attributeRowsByEntityId[entityId.asString()] ?: emptyList()).map { attrRow ->
                    entityAttributeFromRow(attrRow)
                }
                val identifierAttributeIdString = row[EntityTable.identifierAttributeId]
                    ?: throw ModelStorageSQLiteInvalidIdentifierAttributeException(entityId.asString())

                EntityInMemory(
                    id = entityId,
                    key = EntityKey(row[EntityTable.key]),
                    name = stringToLocalizedText(row[EntityTable.name]),
                    description = stringToLocalizedMarkdown(row[EntityTable.description]),
                    identifierAttributeId = AttributeId.fromString(identifierAttributeIdString),
                    origin = stringToEntityOrigin(row[EntityTable.origin]),
                    attributes = attributes,
                    documentationHome = row[EntityTable.documentationHome]?.let { URI(it).toURL() },
                    tags = loadEntityTags(entityId)
                )
            }
    }

    private fun loadRelationships(modelId: ModelId): List<RelationshipInMemory> {
        val relationshipIds = RelationshipTable.select(RelationshipTable.id)
            .where { RelationshipTable.modelId eq modelId.asString() }

        val roleRowsByRelationshipId = RelationshipRoleTable.selectAll()
            .where { RelationshipRoleTable.relationshipId inSubQuery relationshipIds }
            .orderBy(RelationshipRoleTable.key to SortOrder.ASC)
            .toList()
            .groupBy { it[RelationshipRoleTable.relationshipId] }

        val attributeRowsByRelationshipId = RelationshipAttributeTable.selectAll()
            .where { RelationshipAttributeTable.relationshipId inSubQuery relationshipIds }
            .orderBy(RelationshipAttributeTable.key to SortOrder.ASC)
            .toList()
            .groupBy { it[RelationshipAttributeTable.relationshipId] }

        return RelationshipTable.selectAll()
            .where { RelationshipTable.modelId eq modelId.asString() }
            .orderBy(RelationshipTable.key to SortOrder.ASC)
            .map { row ->
                val relationshipId = RelationshipId.fromString(row[RelationshipTable.id])
                RelationshipInMemory(
                    id = relationshipId,
                    key = RelationshipKey(row[RelationshipTable.key]),
                    name = stringToLocalizedText(row[RelationshipTable.name]),
                    description = stringToLocalizedMarkdown(row[RelationshipTable.description]),
                    roles = (roleRowsByRelationshipId[relationshipId.asString()] ?: emptyList()).map { roleRow ->
                        relationshipRoleFromRow(roleRow)
                    },
                    attributes = (attributeRowsByRelationshipId[relationshipId.asString()] ?: emptyList()).map { attrRow ->
                        relationshipAttributeFromRow(attrRow)
                    },
                    tags = loadRelationshipTags(relationshipId)
                )
            }
    }

    private fun entityAttributeFromRow(row: ResultRow): AttributeInMemory {
        val attributeId = AttributeId.fromString(row[EntityAttributeTable.id])
        return AttributeInMemory(
            id = attributeId,
            key = AttributeKey(row[EntityAttributeTable.key]),
            name = stringToLocalizedText(row[EntityAttributeTable.name]),
            description = stringToLocalizedMarkdown(row[EntityAttributeTable.description]),
            typeId = TypeId.fromString(row[EntityAttributeTable.typeId]),
            optional = row[EntityAttributeTable.optional],
            tags = loadEntityAttributeTags(attributeId)
        )
    }

    private fun relationshipAttributeFromRow(row: ResultRow): AttributeInMemory {
        val attributeId = AttributeId.fromString(row[RelationshipAttributeTable.id])
        return AttributeInMemory(
            id = attributeId,
            key = AttributeKey(row[RelationshipAttributeTable.key]),
            name = stringToLocalizedText(row[RelationshipAttributeTable.name]),
            description = stringToLocalizedMarkdown(row[RelationshipAttributeTable.description]),
            typeId = TypeId.fromString(row[RelationshipAttributeTable.typeId]),
            optional = row[RelationshipAttributeTable.optional],
            tags = loadRelationshipAttributeTags(attributeId)
        )
    }

    private fun relationshipRoleFromRow(row: ResultRow): RelationshipRoleInMemory {
        return RelationshipRoleInMemory(
            id = RelationshipRoleId.fromString(row[RelationshipRoleTable.id]),
            key = RelationshipRoleKey(row[RelationshipRoleTable.key]),
            entityId = EntityId.fromString(row[RelationshipRoleTable.entityId]),
            name = stringToLocalizedText(row[RelationshipRoleTable.name]),
            cardinality = RelationshipCardinality.valueOfCode(row[RelationshipRoleTable.cardinality])
        )
    }

    private fun loadModelTags(modelId: ModelId): List<TagId> {
        return ModelTagTable.selectAll()
            .where { ModelTagTable.modelId eq modelId.asString() }
            .orderBy(ModelTagTable.tagId to SortOrder.ASC)
            .map { Id.fromString(it[ModelTagTable.tagId], ::TagId) }
    }

    private fun loadEntityTags(entityId: EntityId): List<TagId> {
        return EntityTagTable.selectAll()
            .where { EntityTagTable.entityId eq entityId.asString() }
            .orderBy(EntityTagTable.tagId to SortOrder.ASC)
            .map { Id.fromString(it[EntityTagTable.tagId], ::TagId) }
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

    companion object {
        val REPOSITORY_ID = ModelRepositoryId("sql")

        object ModelTable : Table("model") {
            val id = text("id")
            val key = text("key")
            val name = text("name").nullable()
            val description = text("description").nullable()
            val version = text("version")
            val origin = text("origin").nullable()
            val documentationHome = text("documentation_home").nullable()

            override val primaryKey = PrimaryKey(id)
        }

        object ModelTagTable : Table("model_tag") {
            val modelId = text("model_id")
            val tagId = text("tag_id")
        }

        object ModelTypeTable : Table("model_type") {
            val id = text("id")
            val modelId = text("model_id")
            val key = text("key")
            val name = text("name").nullable()
            val description = text("description").nullable()

            override val primaryKey = PrimaryKey(id)
        }

        object EntityTable : Table("entity") {
            val id = text("id")
            val modelId = text("model_id")
            val key = text("key")
            val name = text("name").nullable()
            val description = text("description").nullable()
            val identifierAttributeId = text("identifier_attribute_id").nullable()
            val origin = text("origin").nullable()
            val documentationHome = text("documentation_home").nullable()

            override val primaryKey = PrimaryKey(id)
        }

        object EntityTagTable : Table("entity_tag") {
            val entityId = text("entity_id")
            val tagId = text("tag_id")
        }

        object EntityAttributeTable : Table("entity_attribute") {
            val id = text("id")
            val entityId = text("entity_id")
            val key = text("key")
            val name = text("name").nullable()
            val description = text("description").nullable()
            val typeId = text("type_id")
            val optional = bool("optional")

            override val primaryKey = PrimaryKey(id)
        }

        object EntityAttributeTagTable : Table("entity_attribute_tag") {
            val attributeId = text("attribute_id")
            val tagId = text("tag_id")
        }

        object RelationshipTable : Table("relationship") {
            val id = text("id")
            val modelId = text("model_id")
            val key = text("key")
            val name = text("name").nullable()
            val description = text("description").nullable()

            override val primaryKey = PrimaryKey(id)
        }

        object RelationshipTagTable : Table("relationship_tag") {
            val relationshipId = text("relationship_id")
            val tagId = text("tag_id")
        }

        object RelationshipRoleTable : Table("relationship_role") {
            val id = text("id")
            val relationshipId = text("relationship_id")
            val key = text("key")
            val entityId = text("entity_id")
            val name = text("name").nullable()
            val cardinality = text("cardinality")

            override val primaryKey = PrimaryKey(id)
        }

        object RelationshipAttributeTable : Table("relationship_attribute") {
            val id = text("id")
            val relationshipId = text("relationship_id")
            val key = text("key")
            val name = text("name").nullable()
            val description = text("description").nullable()
            val typeId = text("type_id")
            val optional = bool("optional")

            override val primaryKey = PrimaryKey(id)
        }

        object RelationshipAttributeTagTable : Table("relationship_attribute_tag") {
            val attributeId = text("attribute_id")
            val tagId = text("tag_id")
        }
    }
}

class ModelStorageSQLiteModelNotFoundException(modelId: ModelId) :
    MedatarunException("Model not found in sqlite repository ${modelId.value}")

class ModelStorageSQLiteInvalidIdentifierAttributeException(entityId: String) :
    MedatarunException("Entity $entityId has no identifier attribute in sqlite repository")
