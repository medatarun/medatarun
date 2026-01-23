package io.medatarun.ext.modeljson.internal


import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.infra.ModelInMemoryReducer
import io.medatarun.model.ports.needs.ModelRepository
import io.medatarun.model.ports.needs.ModelRepositoryCmd
import io.medatarun.model.ports.needs.ModelRepositoryCmdOnModel
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

    override fun findAllModelKeys(): List<ModelKey> {
        return discoveredModels.keys.toList()
    }

    private fun createModel(model: Model) {
        persistModel(model)
    }

    private fun updateModel(modelKey: ModelKey, block: (model: ModelInMemory) -> ModelInMemory) {
        val model = findModelByKeyOptional(modelKey) ?: throw ModelJsonRepositoryModelNotFoundException(modelKey)
        val next = block(model)
        persistModel(next)
    }

    private fun deleteModel(modelKey: ModelKey) {
        val path = discoveredModels.remove(modelKey) ?: throw ModelJsonRepositoryModelNotFoundException(modelKey)
        if (!path.deleteIfExists()) {
            throw ModelJsonRepositoryException("Failed to delete model file for ${modelKey.value} at $path")
        }
    }


    override fun dispatch(cmd: ModelRepositoryCmd) {
        when (cmd) {
            is ModelRepositoryCmd.CreateModel -> createModel(cmd.model)
            is ModelRepositoryCmd.DeleteModel -> deleteModel(cmd.modelKey)
            is ModelRepositoryCmdOnModel -> updateModel(cmd.modelKey) { model ->
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
