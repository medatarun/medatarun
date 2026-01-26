package io.medatarun.ext.modeljson.internal


import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.infra.ModelInMemoryReducer
import io.medatarun.model.ports.needs.ModelRepoCmd
import io.medatarun.model.ports.needs.ModelRepoCmdOnModel
import io.medatarun.model.ports.needs.ModelRepository
import io.medatarun.model.ports.needs.ModelRepositoryId
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.io.path.deleteIfExists


internal class ModelsStorageJsonRepository(
    private val files: ModelsStorageJsonFiles,
    private val modelJsonConverter: ModelJsonConverter
) : ModelRepository {

    /**
     * This repository currently handles only one storage.
     */
    private val repositoryId = REPOSITORY_ID

    private val discoveredModels = files.getAllModelFiles().toMutableMap()

    override fun matchesId(id: ModelRepositoryId): Boolean {
        return id == repositoryId
    }

    override fun findModelByKeyOptional(key: ModelKey): ModelInMemory? {
        discoveredModels[key] ?: return null
        return modelJsonConverter.fromJson(files.load(key))
    }

    override fun findModelByIdOptional(id: ModelId): ModelInMemory? {
        // VERY TIME CONSUMING
        return discoveredModels.keys
            .map { modelJsonConverter.fromJson(files.load(it)) }
            .firstOrNull { it.id == id }
    }


    override fun findAllModelIds(): List<ModelId> {
        return discoveredModels.map {
            // VERY TIME CONSUMING
            modelJsonConverter.fromJson(files.load(it.key)).id
        }
    }

    private fun createModel(model: Model) {
        persistModel(model)
    }

    private fun updateModel(modelId: ModelId, block: (model: ModelInMemory) -> ModelInMemory) {
        val model = findModelByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        val next = block(model)
        persistModel(next)
    }

    private fun deleteModel(modelId: ModelId) {
        val model = findModelByIdOptional(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        val path = discoveredModels.remove(model.key) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        if (!path.deleteIfExists()) {
            throw ModelJsonRepositoryException("Failed to delete model file for ${modelId.value} at $path")
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

    fun persistModel(model: Model) {
        val json = modelJsonConverter.toJsonString(model)
        val path = files.save(model.key.value, json)
        discoveredModels[model.key] = path
    }

    companion object {
        val REPOSITORY_ID = ModelRepositoryId("json")
        val logger: Logger = LoggerFactory.getLogger(ModelsStorageJsonRepository::class.java)
    }

}
