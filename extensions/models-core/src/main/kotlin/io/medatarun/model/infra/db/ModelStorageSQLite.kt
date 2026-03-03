package io.medatarun.model.infra.db

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.*
import io.medatarun.model.infra.*
import io.medatarun.model.ports.needs.ModelRepoCmd
import io.medatarun.model.ports.needs.ModelRepoCmdOnModel
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
                ?: return@withExposed null
            loadModel(row)
        }
    }

    override fun findModelByIdOptional(id: ModelId): Model? {
        return dbConnectionFactory.withExposed {
            val row = ModelTable.selectAll()
                .where { ModelTable.id eq id.asString() }
                .singleOrNull()
                ?: return@withExposed null
            loadModel(row)
        }
    }

    override fun dispatch(cmd: ModelRepoCmd) {
        when (cmd) {
            is ModelRepoCmd.CreateModel -> createModel(cmd.model)
            is ModelRepoCmd.DeleteModel -> deleteModel(cmd.modelId)
            is ModelRepoCmdOnModel -> updateModel(cmd.modelId) { model ->
                ModelInMemoryReducer().dispatch(model, cmd)
            }
        }
    }

    private fun createModel(model: Model) {
        persistModelSnapshot(ModelInMemory.of(model))
    }

    private fun updateModel(modelId: ModelId, block: (model: ModelInMemory) -> ModelInMemory) {
        val model = findModelByIdOptional(modelId) as? ModelInMemory
            ?: throw ModelStorageSQLiteModelNotFoundException(modelId)
        persistModelSnapshot(block(model))
    }

    private fun deleteModel(modelId: ModelId) {
        dbConnectionFactory.withExposed {
            ModelTable.deleteWhere { id eq modelId.asString() }
        }
    }

    /**
     * Writes the full relational snapshot for one model.
     *
     * Commands still operate as incremental domain mutations, but persistence stays simple and consistent by
     * rewriting the model subtree inside the current transaction.
     */
    private fun persistModelSnapshot(model: ModelInMemory) {
        dbConnectionFactory.withExposed {
            upsertModel(model)
            clearModelSnapshot(model.id)
            replaceModelTags(model)
            replaceTypes(model)
            replaceEntities(model)
            replaceRelationships(model)
        }
    }

    private fun clearModelSnapshot(modelId: ModelId) {
        val entityIds = EntityTable.select(EntityTable.id)
            .where { EntityTable.modelId eq modelId.asString() }
        val relationshipIds = RelationshipTable.select(RelationshipTable.id)
            .where { RelationshipTable.modelId eq modelId.asString() }
        val entityAttributeIds = EntityAttributeTable.select(EntityAttributeTable.id)
            .where { EntityAttributeTable.entityId inSubQuery entityIds }
        val relationshipAttributeIds = RelationshipAttributeTable.select(RelationshipAttributeTable.id)
            .where { RelationshipAttributeTable.relationshipId inSubQuery relationshipIds }

        RelationshipAttributeTagTable.deleteWhere {
            RelationshipAttributeTagTable.attributeId inSubQuery relationshipAttributeIds
        }
        RelationshipAttributeTable.deleteWhere {
            RelationshipAttributeTable.relationshipId inSubQuery relationshipIds
        }
        RelationshipRoleTable.deleteWhere {
            RelationshipRoleTable.relationshipId inSubQuery relationshipIds
        }
        RelationshipTagTable.deleteWhere {
            RelationshipTagTable.relationshipId inSubQuery relationshipIds
        }
        RelationshipTable.deleteWhere { RelationshipTable.modelId eq modelId.asString() }

        EntityAttributeTagTable.deleteWhere {
            EntityAttributeTagTable.attributeId inSubQuery entityAttributeIds
        }
        EntityAttributeTable.deleteWhere {
            EntityAttributeTable.entityId inSubQuery entityIds
        }
        EntityTagTable.deleteWhere {
            EntityTagTable.entityId inSubQuery entityIds
        }
        EntityTable.deleteWhere { EntityTable.modelId eq modelId.asString() }

        ModelTypeTable.deleteWhere { ModelTypeTable.modelId eq modelId.asString() }
        ModelTagTable.deleteWhere { ModelTagTable.modelId eq modelId.asString() }
    }

    private fun upsertModel(model: ModelInMemory) {
        val existingCount = ModelTable.selectAll()
            .where { ModelTable.id eq model.id.asString() }
            .count()

        if (existingCount == 0L) {
            ModelTable.insert { row ->
                row[ModelTable.id] = model.id.asString()
                row[ModelTable.key] = model.key.asString()
                row[ModelTable.name] = localizedTextToString(model.name)
                row[ModelTable.description] = localizedMarkdownToString(model.description)
                row[ModelTable.version] = model.version.value
                row[ModelTable.origin] = modelOriginToString(model.origin)
                row[ModelTable.documentationHome] = model.documentationHome?.toExternalForm()
            }
        } else {
            ModelTable.update(where = { ModelTable.id eq model.id.asString() }) { row ->
                row[ModelTable.key] = model.key.asString()
                row[ModelTable.name] = localizedTextToString(model.name)
                row[ModelTable.description] = localizedMarkdownToString(model.description)
                row[ModelTable.version] = model.version.value
                row[ModelTable.origin] = modelOriginToString(model.origin)
                row[ModelTable.documentationHome] = model.documentationHome?.toExternalForm()
            }
        }
    }

    private fun replaceModelTags(model: ModelInMemory) {
        for (tagId in model.tags) {
            ModelTagTable.insert { row ->
                row[ModelTagTable.modelId] = model.id.asString()
                row[ModelTagTable.tagId] = tagId.asString()
            }
        }
    }

    private fun replaceTypes(model: ModelInMemory) {
        for (type in model.types) {
            ModelTypeTable.insert { row ->
                row[ModelTypeTable.id] = type.id.asString()
                row[ModelTypeTable.modelId] = model.id.asString()
                row[ModelTypeTable.key] = type.key.asString()
                row[ModelTypeTable.name] = localizedTextToString(type.name)
                row[ModelTypeTable.description] = localizedMarkdownToString(type.description)
            }
        }
    }

    private fun replaceEntities(model: ModelInMemory) {
        for (entity in model.entities) {
            EntityTable.insert { row ->
                row[EntityTable.id] = entity.id.asString()
                row[EntityTable.modelId] = model.id.asString()
                row[EntityTable.key] = entity.key.asString()
                row[EntityTable.name] = localizedTextToString(entity.name)
                row[EntityTable.description] = localizedMarkdownToString(entity.description)
                row[EntityTable.identifierAttributeId] = null
                row[EntityTable.origin] = entityOriginToString(entity.origin)
                row[EntityTable.documentationHome] = entity.documentationHome?.toExternalForm()
            }

            for (tagId in entity.tags) {
                EntityTagTable.insert { row ->
                    row[EntityTagTable.entityId] = entity.id.asString()
                    row[EntityTagTable.tagId] = tagId.asString()
                }
            }

            for (attribute in entity.attributes) {
                EntityAttributeTable.insert { row ->
                    row[EntityAttributeTable.id] = attribute.id.asString()
                    row[EntityAttributeTable.entityId] = entity.id.asString()
                    row[EntityAttributeTable.key] = attribute.key.asString()
                    row[EntityAttributeTable.name] = localizedTextToString(attribute.name)
                    row[EntityAttributeTable.description] = localizedMarkdownToString(attribute.description)
                    row[EntityAttributeTable.typeId] = attribute.typeId.asString()
                    row[EntityAttributeTable.optional] = attribute.optional
                }

                for (tagId in attribute.tags) {
                    EntityAttributeTagTable.insert { row ->
                        row[EntityAttributeTagTable.attributeId] = attribute.id.asString()
                        row[EntityAttributeTagTable.tagId] = tagId.asString()
                    }
                }
            }

            EntityTable.update(where = { EntityTable.id eq entity.id.asString() }) { row ->
                row[EntityTable.identifierAttributeId] = entity.identifierAttributeId.asString()
            }
        }
    }

    private fun replaceRelationships(model: ModelInMemory) {
        for (relationship in model.relationships) {
            RelationshipTable.insert { row ->
                row[RelationshipTable.id] = relationship.id.asString()
                row[RelationshipTable.modelId] = model.id.asString()
                row[RelationshipTable.key] = relationship.key.asString()
                row[RelationshipTable.name] = localizedTextToString(relationship.name)
                row[RelationshipTable.description] = localizedMarkdownToString(relationship.description)
            }

            for (tagId in relationship.tags) {
                RelationshipTagTable.insert { row ->
                    row[RelationshipTagTable.relationshipId] = relationship.id.asString()
                    row[RelationshipTagTable.tagId] = tagId.asString()
                }
            }

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
                RelationshipAttributeTable.insert { row ->
                    row[RelationshipAttributeTable.id] = attribute.id.asString()
                    row[RelationshipAttributeTable.relationshipId] = relationship.id.asString()
                    row[RelationshipAttributeTable.key] = attribute.key.asString()
                    row[RelationshipAttributeTable.name] = localizedTextToString(attribute.name)
                    row[RelationshipAttributeTable.description] = localizedMarkdownToString(attribute.description)
                    row[RelationshipAttributeTable.typeId] = attribute.typeId.asString()
                    row[RelationshipAttributeTable.optional] = attribute.optional
                }

                for (tagId in attribute.tags) {
                    RelationshipAttributeTagTable.insert { row ->
                        row[RelationshipAttributeTagTable.attributeId] = attribute.id.asString()
                        row[RelationshipAttributeTagTable.tagId] = tagId.asString()
                    }
                }
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
