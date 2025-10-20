package io.medatarun.ext.modeljson

import io.medatarun.model.infra.ModelAttributeInMemory
import io.medatarun.model.infra.ModelEntityInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.model.*
import java.nio.file.Path
import kotlin.collections.plus
import kotlin.io.path.*


class ModelJsonRepository(
    private val repositoryPath: Path,
    private val modelJsonConverter: ModelJsonConverter
) : ModelRepository {


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

    override fun findByIdOptional(id: ModelId): ModelInMemory? {
        val modelPath = discoveredModels[id] ?: return null
        return modelJsonConverter.fromJson(modelPath.readText())
    }

    override fun findAllIds(): List<ModelId> {
        return discoveredModels.keys.toList()
    }

    override fun create(model: Model) {
        persistModel(model)
    }

    override fun delete(modelId: ModelId) {
        val path = discoveredModels.remove(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        if (!path.deleteIfExists()) {
            throw ModelJsonRepositoryException("Failed to delete model file for ${modelId.value} at $path")
        }
    }

    override fun createEntity(modelId: ModelId, e: ModelEntity) {
        val model = findByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        val next = model.copy(
            entities = model.entities + ModelEntityInMemory.Companion.of(e)
        )
        persistModel(next)

    }

    override fun updateEntityName(modelId: ModelId, entityId: ModelEntityId, newEntityId: ModelEntityId) {
        val model = findByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        if (model.entities.any { it.id == newEntityId && it.id != entityId }) {
            throw ModelJsonRepositoryException("Entity with id ${newEntityId.value} already exists in model ${modelId.value}")
        }
        var updated = false
        val nextEntities = model.entities.map { entity ->
            if (entity.id != entityId) return@map entity
            updated = true
            entity.copy(id = newEntityId)
        }
        if (!updated) {
            throw ModelEntityNotFoundException(modelId, entityId)
        }
        val next = model.copy(entities = nextEntities)
        persistModel(next)
    }

    override fun updateEntityTitle(modelId: ModelId, entityId: ModelEntityId, title: LocalizedText?) {
        val model = findByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        var updated = false
        val nextEntities = model.entities.map { entity ->
            if (entity.id != entityId) return@map entity
            updated = true
            entity.copy(name = title)
        }
        if (!updated) {
            throw ModelEntityNotFoundException(modelId, entityId)
        }
        val next = model.copy(entities = nextEntities)
        persistModel(next)
    }

    override fun updateEntityDescription(modelId: ModelId, entityId: ModelEntityId, description: LocalizedMarkdown?) {
        val model = findByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        var updated = false
        val nextEntities = model.entities.map { entity ->
            if (entity.id != entityId) return@map entity
            updated = true
            entity.copy(description = description)
        }
        if (!updated) {
            throw ModelEntityNotFoundException(modelId, entityId)
        }
        val next = model.copy(entities = nextEntities)
        persistModel(next)
    }

    override fun deleteEntity(modelId: ModelId, entityId: ModelEntityId) {
        val model = findByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        var removed = false
        val nextEntities = model.entities.filterNot { entity ->
            if (entity.id == entityId) {
                removed = true
                true
            } else {
                false
            }
        }
        if (!removed) {
            throw ModelEntityNotFoundException(modelId, entityId)
        }
        val next = model.copy(entities = nextEntities)
        persistModel(next)
    }

    override fun createEntityAttribute(
        modelId: ModelId,
        entityId: ModelEntityId,
        attr: ModelAttribute
    ) {
        val model = findByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        val next = model.copy(
            entities = model.entities.map { e ->
                if (e.id == entityId) e.copy(
                    attributes = e.attributes + ModelAttributeInMemory.Companion.of(
                        attr
                    )
                ) else e
            }
        )
        persistModel(next)
    }

    override fun updateEntityAttributeName(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        newAttributeId: ModelAttributeId
    ) {
        val model = findByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        var entityFound = false
        var attributeUpdated = false
        val nextEntities = model.entities.map { entity ->
            if (entity.id != entityId) return@map entity
            entityFound = true
            if (entity.attributes.any { it.id == newAttributeId && it.id != attributeId }) {
                throw ModelJsonRepositoryException("Attribute with id ${newAttributeId.value} already exists in entity ${entityId.value}")
            }
            val nextAttributes = entity.attributes.map { attribute ->
                if (attribute.id != attributeId) return@map attribute
                attributeUpdated = true
                attribute.copy(id = newAttributeId)
            }
            entity.copy(attributes = nextAttributes)
        }
        if (!entityFound) {
            throw ModelEntityNotFoundException(modelId, entityId)
        }
        if (!attributeUpdated) {
            throw ModelEntityAttributeNotFoundException(entityId, attributeId)
        }
        val next = model.copy(entities = nextEntities)
        persistModel(next)
    }

    override fun updateEntityAttributeTitle(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        title: LocalizedText?
    ) {
        val model = findByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        var entityFound = false
        var attributeUpdated = false
        val nextEntities = model.entities.map { entity ->
            if (entity.id != entityId) return@map entity
            entityFound = true
            val nextAttributes = entity.attributes.map { attribute ->
                if (attribute.id != attributeId) return@map attribute
                attributeUpdated = true
                attribute.copy(name = title)
            }
            entity.copy(attributes = nextAttributes)
        }
        if (!entityFound) {
            throw ModelEntityNotFoundException(modelId, entityId)
        }
        if (!attributeUpdated) {
            throw ModelEntityAttributeNotFoundException(entityId, attributeId)
        }
        val next = model.copy(entities = nextEntities)
        persistModel(next)
    }

    override fun updateEntityAttributeDescription(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        description: LocalizedMarkdown?
    ) {
        val model = findByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        var entityFound = false
        var attributeUpdated = false
        val nextEntities = model.entities.map { entity ->
            if (entity.id != entityId) return@map entity
            entityFound = true
            val nextAttributes = entity.attributes.map { attribute ->
                if (attribute.id != attributeId) return@map attribute
                attributeUpdated = true
                attribute.copy(description = description)
            }
            entity.copy(attributes = nextAttributes)
        }
        if (!entityFound) {
            throw ModelEntityNotFoundException(modelId, entityId)
        }
        if (!attributeUpdated) {
            throw ModelEntityAttributeNotFoundException(entityId, attributeId)
        }
        val next = model.copy(entities = nextEntities)
        persistModel(next)
    }

    override fun updateEntityAttributeType(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        type: ModelTypeId
    ) {
        val model = findByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        var entityFound = false
        var attributeUpdated = false
        val nextEntities = model.entities.map { entity ->
            if (entity.id != entityId) return@map entity
            entityFound = true
            val nextAttributes = entity.attributes.map { attribute ->
                if (attribute.id != attributeId) return@map attribute
                attributeUpdated = true
                attribute.copy(type = type)
            }
            entity.copy(attributes = nextAttributes)
        }
        if (!entityFound) {
            throw ModelEntityNotFoundException(modelId, entityId)
        }
        if (!attributeUpdated) {
            throw ModelEntityAttributeNotFoundException(entityId, attributeId)
        }
        val next = model.copy(entities = nextEntities)
        persistModel(next)
    }

    override fun updateEntityAttributeOptional(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        optional: Boolean
    ) {
        val model = findByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        var entityFound = false
        var attributeUpdated = false
        val nextEntities = model.entities.map { entity ->
            if (entity.id != entityId) return@map entity
            entityFound = true
            val nextAttributes = entity.attributes.map { attribute ->
                if (attribute.id != attributeId) return@map attribute
                attributeUpdated = true
                attribute.copy(optional = optional)
            }
            entity.copy(attributes = nextAttributes)
        }
        if (!entityFound) {
            throw ModelEntityNotFoundException(modelId, entityId)
        }
        if (!attributeUpdated) {
            throw ModelEntityAttributeNotFoundException(entityId, attributeId)
        }
        val next = model.copy(entities = nextEntities)
        persistModel(next)
    }

    override fun deleteEntityAttribute(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId
    ) {
        val model = findByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        var entityFound = false
        var attributeRemoved = false
        val nextEntities = model.entities.map { entity ->
            if (entity.id != entityId) return@map entity
            entityFound = true
            val nextAttributes = entity.attributes.filterNot { attribute ->
                if (attribute.id == attributeId) {
                    attributeRemoved = true
                    true
                } else {
                    false
                }
            }
            entity.copy(attributes = nextAttributes)
        }
        if (!entityFound || !attributeRemoved) {
            throw ModelEntityAttributeNotFoundException(entityId, attributeId)
        }
        val next = model.copy(entities = nextEntities)
        persistModel(next)
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
