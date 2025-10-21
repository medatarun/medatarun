package io.medatarun.ext.modeljson

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelRepository
import io.medatarun.model.ports.ModelRepositoryId
import java.nio.file.Path
import kotlin.collections.plus
import kotlin.io.path.*


class ModelJsonRepository(
    private val repositoryPath: Path,
    private val modelJsonConverter: ModelJsonConverter
) : ModelRepository {

    /**
     * This repository currently handles only one storage.
     */
    private val repositoryId = ModelRepositoryId("json")

    private val discoveredModels = mutableMapOf<ModelId, Path>()

    init {
        if (!repositoryPath.isDirectory())
            throw ModelJsonRepositoryException("Model repository [$repositoryPath] doesn't exist or is not a repository")

        val paths = repositoryPath.listDirectoryEntries("*.json").filter { it.isRegularFile() }
        paths.forEach { path ->
            val model = modelJsonConverter.fromJson(path.readText())
            discoveredModels[model.id] = path.toAbsolutePath()
        }
    }

    override fun matchesId(id: ModelRepositoryId): Boolean {
        return id == repositoryId
    }

    override fun findModelByIdOptional(id: ModelId): ModelInMemory? {
        val modelPath = discoveredModels[id] ?: return null
        return modelJsonConverter.fromJson(modelPath.readText())
    }

    override fun findAllModelIds(): List<ModelId> {
        return discoveredModels.keys.toList()
    }

    override fun createModel(model: Model) {
        persistModel(model)
    }

    private fun updateModel(modelId: ModelId, block:(model: ModelInMemory) -> ModelInMemory) {
        val model = findModelByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        val next = block(model)
        persistModel(next)
    }

    override fun updateModelName(modelId: ModelId, name: LocalizedTextNotLocalized) {
        updateModel(modelId) { it.copy(name = name) }
    }

    override fun updateModelDescription(modelId: ModelId, description: LocalizedTextNotLocalized?) {
        updateModel(modelId) { it.copy(description = description) }
    }

    override fun updateModelVersion(modelId: ModelId, version: ModelVersion) {
        updateModel(modelId) { it.copy(version = version) }
    }

    override fun deleteModel(modelId: ModelId) {
        val path = discoveredModels.remove(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        if (!path.deleteIfExists()) {
            throw ModelJsonRepositoryException("Failed to delete model file for ${modelId.value} at $path")
        }
    }

    override fun createEntityDef(modelId: ModelId, e: EntityDef) {
        updateModel(modelId) {
            it.copy(entityDefs = it.entityDefs + EntityDefInMemory.of(e))
        }
    }

    override fun updateEntityDefId(modelId: ModelId, entityDefId: EntityDefId, newEntityDefId: EntityDefId) {
        updateModel(modelId) { model ->
            if (model.entityDefs.any { it.id == newEntityDefId && it.id != entityDefId }) {
                throw ModelJsonRepositoryException("Entity with id ${newEntityDefId.value} already exists in model ${modelId.value}")
            }
            var updated = false
            val nextEntities = model.entityDefs.map { entity ->
                if (entity.id != entityDefId) return@map entity
                updated = true
                entity.copy(id = newEntityDefId)
            }
            if (!updated) {
                throw ModelEntityNotFoundException(modelId, entityDefId)
            }
            model.copy(entityDefs = nextEntities)
        }
    }

    override fun updateEntityDefName(modelId: ModelId, entityDefId: EntityDefId, name: LocalizedText?) {
        updateModel(modelId) { model ->
            var updated = false
            val nextEntities = model.entityDefs.map { entity ->
                if (entity.id != entityDefId) return@map entity
                updated = true
                entity.copy(name = name)
            }
            if (!updated) {
                throw ModelEntityNotFoundException(modelId, entityDefId)
            }
            model.copy(entityDefs = nextEntities)
        }
    }

    override fun updateEntityDefDescription(modelId: ModelId, entityDefId: EntityDefId, description: LocalizedMarkdown?) {
        updateModel(modelId) { model ->
            var updated = false
            val nextEntities = model.entityDefs.map { entity ->
                if (entity.id != entityDefId) return@map entity
                updated = true
                entity.copy(description = description)
            }
            if (!updated) {
                throw ModelEntityNotFoundException(modelId, entityDefId)
            }
            model.copy(entityDefs = nextEntities)
        }
    }

    override fun deleteEntityDef(modelId: ModelId, entityDefId: EntityDefId) {
        updateModel(modelId) { model ->
            var removed = false
            val nextEntities = model.entityDefs.filterNot { entity ->
                if (entity.id == entityDefId) {
                    removed = true
                    true
                } else {
                    false
                }
            }
            if (!removed) {
                throw ModelEntityNotFoundException(modelId, entityDefId)
            }
            model.copy(entityDefs = nextEntities)
        }
    }

    override fun createEntityDefAttributeDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attr: AttributeDef
    ) {
        updateModel(modelId) { model ->
            model.copy(
                entityDefs = model.entityDefs.map { e ->
                    if (e.id == entityDefId) e.copy(
                        attributes = e.attributes + AttributeDefInMemory.Companion.of(
                            attr
                        )
                    ) else e
                }
            )
        }
    }

    override fun updateEntityDefAttributeDefId(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        newAttributeDefId: AttributeDefId
    ) {
        updateModel(modelId) { model ->
            var entityFound = false
            var attributeUpdated = false
            val nextEntities = model.entityDefs.map { entity ->
                if (entity.id != entityDefId) return@map entity
                entityFound = true
                if (entity.attributes.any { it.id == newAttributeDefId && it.id != attributeDefId }) {
                    throw ModelJsonRepositoryException("Attribute with id ${newAttributeDefId.value} already exists in entity ${entityDefId.value}")
                }
                val nextAttributes = entity.attributes.map { attribute ->
                    if (attribute.id != attributeDefId) return@map attribute
                    attributeUpdated = true
                    attribute.copy(id = newAttributeDefId)
                }
                entity.copy(attributes = nextAttributes)
            }
            if (!entityFound) {
                throw ModelEntityNotFoundException(modelId, entityDefId)
            }
            if (!attributeUpdated) {
                throw ModelEntityAttributeNotFoundException(entityDefId, attributeDefId)
            }
            model.copy(entityDefs = nextEntities)
        }
    }

    override fun updateEntityDefAttributeDefName(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        name: LocalizedText?
    ) {
        updateModel(modelId) { model ->
            var entityFound = false
            var attributeUpdated = false
            val nextEntities = model.entityDefs.map { entity ->
                if (entity.id != entityDefId) return@map entity
                entityFound = true
                val nextAttributes = entity.attributes.map { attribute ->
                    if (attribute.id != attributeDefId) return@map attribute
                    attributeUpdated = true
                    attribute.copy(name = name)
                }
                entity.copy(attributes = nextAttributes)
            }
            if (!entityFound) {
                throw ModelEntityNotFoundException(modelId, entityDefId)
            }
            if (!attributeUpdated) {
                throw ModelEntityAttributeNotFoundException(entityDefId, attributeDefId)
            }
            model.copy(entityDefs = nextEntities)
        }
    }

    override fun updateEntityDefAttributeDefDescription(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        description: LocalizedMarkdown?
    ) {
        updateModel(modelId) { model ->
            var entityFound = false
            var attributeUpdated = false
            val nextEntities = model.entityDefs.map { entity ->
                if (entity.id != entityDefId) return@map entity
                entityFound = true
                val nextAttributes = entity.attributes.map { attribute ->
                    if (attribute.id != attributeDefId) return@map attribute
                    attributeUpdated = true
                    attribute.copy(description = description)
                }
                entity.copy(attributes = nextAttributes)
            }
            if (!entityFound) {
                throw ModelEntityNotFoundException(modelId, entityDefId)
            }
            if (!attributeUpdated) {
                throw ModelEntityAttributeNotFoundException(entityDefId, attributeDefId)
            }
            model.copy(entityDefs = nextEntities)
        }
    }

    override fun updateEntityDefAttributeDefType(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        type: ModelTypeId
    ) {
        updateModel(modelId) { model ->
            var entityFound = false
            var attributeUpdated = false
            val nextEntities = model.entityDefs.map { entity ->
                if (entity.id != entityDefId) return@map entity
                entityFound = true
                val nextAttributes = entity.attributes.map { attribute ->
                    if (attribute.id != attributeDefId) return@map attribute
                    attributeUpdated = true
                    attribute.copy(type = type)
                }
                entity.copy(attributes = nextAttributes)
            }
            if (!entityFound) {
                throw ModelEntityNotFoundException(modelId, entityDefId)
            }
            if (!attributeUpdated) {
                throw ModelEntityAttributeNotFoundException(entityDefId, attributeDefId)
            }
            model.copy(entityDefs = nextEntities)
        }
    }

    override fun updateEntityDefAttributeDefOptional(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        optional: Boolean
    ) {
        updateModel(modelId) { model ->
            var entityFound = false
            var attributeUpdated = false
            val nextEntities = model.entityDefs.map { entity ->
                if (entity.id != entityDefId) return@map entity
                entityFound = true
                val nextAttributes = entity.attributes.map { attribute ->
                    if (attribute.id != attributeDefId) return@map attribute
                    attributeUpdated = true
                    attribute.copy(optional = optional)
                }
                entity.copy(attributes = nextAttributes)
            }
            if (!entityFound) {
                throw ModelEntityNotFoundException(modelId, entityDefId)
            }
            if (!attributeUpdated) {
                throw ModelEntityAttributeNotFoundException(entityDefId, attributeDefId)
            }
            model.copy(entityDefs = nextEntities)
        }
    }

    override fun deleteEntityDefAttributeDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId
    ) {
        updateModel(modelId) { model ->
            var entityFound = false
            var attributeRemoved = false
            val nextEntities = model.entityDefs.map { entity ->
                if (entity.id != entityDefId) return@map entity
                entityFound = true
                val nextAttributes = entity.attributes.filterNot { attribute ->
                    if (attribute.id == attributeDefId) {
                        attributeRemoved = true
                        true
                    } else {
                        false
                    }
                }
                entity.copy(attributes = nextAttributes)
            }
            if (!entityFound || !attributeRemoved) {
                throw ModelEntityAttributeNotFoundException(entityDefId, attributeDefId)
            }
            model.copy(entityDefs = nextEntities)
        }
    }

    fun persistModel(model: Model) {
        val json = modelJsonConverter.toJson(model)
        val path = repositoryPath.resolve(model.id.value + ".json")
        path.writeText(json)
        discoveredModels[model.id] = path
    }

}

class ModelJsonRepositoryException(message: String) : MedatarunException(message)
class ModelJsonRepositoryModelNotFoundException(modelId: ModelId) :
    MedatarunException("Model with id ${modelId.value} not found in Json repository")
