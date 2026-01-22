package io.medatarun.ext.modeljson


import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.infra.ModelInMemoryReducer
import io.medatarun.model.ports.needs.ModelRepository
import io.medatarun.model.ports.needs.ModelRepositoryCmd
import io.medatarun.model.ports.needs.ModelRepositoryCmdOnModel
import io.medatarun.model.ports.needs.ModelRepositoryId
import kotlinx.serialization.SerializationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.*


class ModelJsonRepository(
    private val repositoryPath: Path,
    private val modelJsonConverter: ModelJsonConverter
) : ModelRepository {

    /**
     * This repository currently handles only one storage.
     */
    private val repositoryId = REPOSITORY_ID

    private val discoveredModels = mutableMapOf<ModelKey, Path>()

    init {
        if (!repositoryPath.isDirectory())
            throw ModelJsonRepositoryException("Model repository [$repositoryPath] doesn't exist or is not a repository")

        val paths = repositoryPath.listDirectoryEntries("*.json").filter { it.isRegularFile() }
        paths.forEach { path ->
            try {
                val model = modelJsonConverter.fromJson(path.readText())
                discoveredModels[model.key] = path.toAbsolutePath()
            } catch (e: SerializationException) {
                logger.error("File ${path.toAbsolutePath()} is not a valid Medatarun model, skipped. Cause: {}", e.message)
            }

        }
    }

    override fun matchesId(id: ModelRepositoryId): Boolean {
        return id == repositoryId
    }

    override fun findModelByIdOptional(id: ModelKey): ModelInMemory? {
        val modelPath = discoveredModels[id] ?: return null
        return modelJsonConverter.fromJson(modelPath.readText())
    }

    override fun findAllModelIds(): List<ModelKey> {
        return discoveredModels.keys.toList()
    }

    private fun createModel(model: Model) {
        persistModel(model)
    }

    private fun updateModel(modelKey: ModelKey, block: (model: ModelInMemory) -> ModelInMemory) {
        val model = findModelByIdOptional(modelKey) ?: throw ModelJsonRepositoryModelNotFoundException(modelKey)
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
        val path = repositoryPath.resolve(model.key.value + ".json")
        path.writeText(json)
        discoveredModels[model.key] = path
    }

    companion object {
        val REPOSITORY_ID = ModelRepositoryId("json")
        val logger: Logger = LoggerFactory.getLogger(ModelJsonRepository::class.java)
    }

}

class ModelJsonRepositoryException(message: String) : MedatarunException(message)
class ModelJsonRepositoryModelNotFoundException(modelKey: ModelKey) :
    MedatarunException("Model with id ${modelKey.value} not found in Json repository")
