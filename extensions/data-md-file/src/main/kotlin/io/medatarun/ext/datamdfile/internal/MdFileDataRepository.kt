package io.medatarun.ext.datamdfile.internal

import io.medatarun.data.*
import io.medatarun.model.model.*
import java.nio.file.Path

/**
 * Repository implementation backed by Markdown files stored per entity definition.
 */
class MdFileDataRepository(private val repositoryRoot: Path) : DataRepository {

    private val markdownAdapter = MarkdownAdapter()
    private val fileManager = RepositoryFileManager(repositoryRoot)

    override fun managedEntityDefs(modelId: ModelId): Set<EntityDefId> {
        return fileManager.listEntityDefinitionIds()
    }

    override fun findAllEntities(
        model: Model,
        entityDefId: EntityDefId
    ): List<Entity> {
        val entityDef = model.findEntityDefOptional(entityDefId) ?: return emptyList()
        val entityFiles = fileManager.listEntityFiles(entityDefId)
        return entityFiles.map { entityFile ->
            val content = fileManager.read(entityFile)
            val parsed = markdownAdapter.parseEntity(
                entityDef = entityDef,
                entityDefId = entityDefId,
                entityIdAttribute = ENTITY_ID_ATTRIBUTE,
                content = content
            )
            MdEntity(
                id = StringEntityInstanceId(parsed.entityId),
                entityTypeId = entityDefId,
                attributes = parsed.values
                    .filterValues { it != null }
                    .mapValues { it.value as Any }
                    .mapKeys { it.key.value }
            )
        }
    }



    override fun createEntity(
        model: Model,
        entityDefId: EntityDefId,
        entityInitializer: EntityInitializer
    ) {
        val entityDef = model.findEntityDef(entityDefId)
        val values = mutableMapOf<AttributeDefId, Any?>()

        entityDef.attributes.forEach { attribute ->
            val value = entityInitializer.get<Any?>(attribute.id)
            values[attribute.id] = value
        }

        val entityIdValue = values[ENTITY_ID_ATTRIBUTE]?.toString()
            ?: throw MdFileEntityIdMissingException(entityDefId)

        val content = markdownAdapter.createMarkdownString(entityDef, values)
        fileManager.write(entityDefId, entityIdValue, content)
    }

    override fun updateEntity(
        model: Model,
        entityDefId: EntityDefId,
        entityUpdater: EntityUpdater
    ) {
        val entityDef = model.findEntityDef(entityDefId)

        val currentEntityId = entityUpdater.id.asString()
        if (!fileManager.exists(entityDefId, currentEntityId)) {
            throw MdFileEntityNotFoundException(entityDefId, currentEntityId)
        }

        val existingContent = fileManager.read(entityDefId, currentEntityId)
        val parsed = markdownAdapter.parseEntity(
            entityDef = entityDef,
            entityDefId = entityDefId,
            entityIdAttribute = ENTITY_ID_ATTRIBUTE,
            content = existingContent
        )
        val values = parsed.values
        entityUpdater.list().forEach { instruction ->
            when (instruction) {
                is EntityUpdater.Instruction.InstructionUpdate -> {
                    entityDef.ensureAttributeDefExists(instruction.attributeId)
                    values[instruction.attributeId] = instruction.value
                }
                is EntityUpdater.Instruction.InstructionNone -> {
                    entityDef.ensureAttributeDefExists(instruction.attributeId)
                }
            }
        }

        val updatedEntityId = values[ENTITY_ID_ATTRIBUTE]?.toString()
            ?: throw MdFileEntityIdMissingException(entityDefId)

        val content = markdownAdapter.createMarkdownString(entityDef, values)
        fileManager.write(entityDefId, updatedEntityId, content)

        if (updatedEntityId != currentEntityId) {
            fileManager.delete(entityDefId, currentEntityId)
        }
    }

    override fun deleteEntity(
        model: Model,
        entityDefId: EntityDefId,
        entityId: EntityInstanceId
    ) {
        fileManager.delete(entityDefId, entityId.asString())
    }


    private data class StringEntityInstanceId(
        private val value: String
    ) : EntityInstanceId {
        override fun asString(): String = value
    }

    companion object {
        private val ENTITY_ID_ATTRIBUTE = AttributeDefId("id")
    }
}
