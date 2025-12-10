package io.medatarun.ext.datamdfile.internal

import io.medatarun.data.adapters.EntityIdString
import io.medatarun.data.domain.Entity
import io.medatarun.data.domain.EntityId
import io.medatarun.data.ports.exposed.EntityInitializer
import io.medatarun.data.ports.exposed.EntityUpdater
import io.medatarun.data.ports.needs.DataRepository
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelKey
import java.nio.file.Path

/**
 * Repository implementation backed by Markdown files stored per entity definition.
 */
class MdFileDataRepository(private val repositoryRoot: Path) : DataRepository {

    private val markdownAdapter = MarkdownAdapter()
    private val fileManager = RepositoryFileManager(repositoryRoot)
    override fun matches(
        modelKey: ModelKey,
        entityKey: EntityKey
    ): Boolean {
        return true
    }

    override fun managedEntityDefs(modelKey: ModelKey): Set<EntityKey> {
        return fileManager.listEntityDefinitionIds()
    }

    override fun findAllEntities(
        model: Model,
        entityKey: EntityKey
    ): List<Entity> {
        val entityDef = model.findEntityDefOptional(entityKey) ?: return emptyList()
        val entityFiles = fileManager.listEntityFiles(entityKey)
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
        entityKey: EntityKey,
        entityInitializer: EntityInitializer
    ) {
        val entityDef = model.findEntityDef(entityKey)
        val values = mutableMapOf<AttributeKey, Any?>()

        entityDef.attributes.forEach { attribute ->
            val value = entityInitializer.get<Any?>(attribute.id)
            values[attribute.id] = value
        }

        val entityIdValue = values[entityDef.entityIdAttributeDefId()]?.toString()
            ?: throw MdFileEntityIdMissingException(entityKey)

        val entity = EntityMarkdownMutable(
            EntityIdString(entityIdValue),
            entityKey,
            values
        )

        val content = markdownAdapter.toMarkdownString(entityDef, entity)
        fileManager.write(entityKey, entityIdValue, content)
    }

    override fun updateEntity(
        model: Model,
        entityKey: EntityKey,
        entityUpdater: EntityUpdater
    ) {
        val entityDef = model.findEntityDef(entityKey)

        val currentEntityId = entityUpdater.id.asString()
        if (!fileManager.exists(entityKey, currentEntityId)) {
            throw MdFileEntityNotFoundException(entityKey, currentEntityId)
        }

        val existingContent = fileManager.read(entityKey, currentEntityId)
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
            ?: throw MdFileEntityIdMissingException(entityKey)

        val content = markdownAdapter.toMarkdownString(entityDef, entity)
        fileManager.write(entityKey, updatedEntityId, content)

        if (updatedEntityId != currentEntityId) {
            fileManager.delete(entityKey, currentEntityId)
        }
    }

    override fun deleteEntity(
        model: Model,
        entityKey: EntityKey,
        entityId: EntityId
    ) {
        fileManager.delete(entityKey, entityId.asString())
    }


}
