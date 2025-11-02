package io.medatarun.ext.modeljson

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.infra.ModelInMemoryReducer
import io.medatarun.model.infra.ModelTypeInMemory
import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelRepository
import io.medatarun.model.ports.ModelRepositoryCmd
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
    private val repositoryId = REPOSITORY_ID

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


    override fun dispatch(cmd: ModelRepositoryCmd) {
        updateModel(cmd.modelId) { model ->
            ModelInMemoryReducer().dispatch(model, cmd)
        }
    }

    fun persistModel(model: Model) {
        val json = modelJsonConverter.toJson(model)
        val path = repositoryPath.resolve(model.id.value + ".json")
        path.writeText(json)
        discoveredModels[model.id] = path
    }

    companion object {
        val REPOSITORY_ID = ModelRepositoryId("json")
    }

}

class ModelJsonRepositoryException(message: String) : MedatarunException(message)
class ModelJsonRepositoryModelNotFoundException(modelId: ModelId) :
    MedatarunException("Model with id ${modelId.value} not found in Json repository")
