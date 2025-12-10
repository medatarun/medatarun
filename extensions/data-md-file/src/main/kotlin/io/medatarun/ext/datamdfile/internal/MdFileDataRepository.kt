package io.medatarun.ext.datamdfile.internal

import io.medatarun.data.adapters.EntityIdString
import io.medatarun.data.domain.Entity
import io.medatarun.data.domain.EntityId
import io.medatarun.data.ports.exposed.EntityInitializer
import io.medatarun.data.ports.exposed.EntityUpdater
import io.medatarun.data.ports.needs.DataRepository
import io.medatarun.model.domain.AttributeDefId
import io.medatarun.model.domain.EntityDefId
import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelId
import java.nio.file.Path

/**
 * Repository implementation backed by Markdown files stored per entity definition.
 */
class MdFileDataRepository(private val repositoryRoot: Path) : DataRepository {

    private val markdownAdapter = MarkdownAdapter()
    private val fileManager = RepositoryFileManager(repositoryRoot)
    override fun matches(
        modelId: ModelId,
        entityDefId: EntityDefId
    ): Boolean {
        return true
    }

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
            markdownAdapter.toEntityMarkdown(
                entityDef = entityDef,
                content = content
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

        val entityIdValue = values[entityDef.entityIdAttributeDefId()]?.toString()
            ?: throw MdFileEntityIdMissingException(entityDefId)

        val entity = EntityMarkdownMutable(
            EntityIdString(entityIdValue),
            entityDefId,
            values
        )

        val content = markdownAdapter.toMarkdownString(entityDef, entity)
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
        val entity = markdownAdapter.toEntityMarkdown(
            entityDef = entityDef,
            content = existingContent
        )

        entityUpdater.list().forEach { instruction ->
            when (instruction) {
                is EntityUpdater.Instruction.InstructionUpdate -> {
                    entityDef.ensureAttributeDefExists(instruction.attributeId)
                    entity.attributes[instruction.attributeId] = instruction.value
                }

                is EntityUpdater.Instruction.InstructionNone -> {
                    entityDef.ensureAttributeDefExists(instruction.attributeId)
                }
            }
        }

        val updatedEntityId = entity.attributes[entityDef.entityIdAttributeDefId()]?.toString()
            ?: throw MdFileEntityIdMissingException(entityDefId)

        val content = markdownAdapter.toMarkdownString(entityDef, entity)
        fileManager.write(entityDefId, updatedEntityId, content)

        if (updatedEntityId != currentEntityId) {
            fileManager.delete(entityDefId, currentEntityId)
        }
    }

    override fun deleteEntity(
        model: Model,
        entityDefId: EntityDefId,
        entityId: EntityId
    ) {
        fileManager.delete(entityDefId, entityId.asString())
    }


}
