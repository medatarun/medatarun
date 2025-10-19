package io.medatarun.model.infra

import io.medatarun.model.model.*
import java.nio.file.Path
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

    override fun createEntity(modelId: ModelId, e: ModelEntity) {
        val model = findByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        val next = model.copy(
            entities = model.entities + ModelEntityInMemory.of(e)
        )
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
                    attributes = e.attributes + ModelAttributeInMemory.of(
                        attr
                    )
                ) else e
            }
        )
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