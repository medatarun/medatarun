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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.deleteIfExists


internal class ModelsStorageJsonRepository(
    private val files: ModelsStorageJsonFiles,
    private val modelJsonConverter: ModelJsonConverter
) : ModelRepository {

    /**
     * This repository currently handles only one storage.
     */
    private val repositoryId = REPOSITORY_ID

    private val json = Json { ignoreUnknownKeys = true }

    private val discoveredModels = files.getAllModelFiles().toMutableMap()
    private val index = ModelJsonIndex()

    init {
        refreshIndex()
    }

    override fun matchesId(id: ModelRepositoryId): Boolean {
        return id == repositoryId
    }

    override fun findModelByKeyOptional(key: ModelKey): ModelInMemory? {
        discoveredModels[key] ?: return null
        return modelJsonConverter.fromJson(files.load(key))
    }

    override fun findModelByIdOptional(id: ModelId): ModelInMemory? {
        val key = index.findKeyById(id) ?: return null
        return findModelByKeyOptional(key)
    }


    override fun findAllModelIds(): List<ModelId> {
        return index.findAllModelIds()
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
        val metadata = index.findMetadataById(modelId) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        val path = discoveredModels.remove(metadata.key) ?: throw ModelJsonRepositoryModelNotFoundException(modelId)
        index.remove(metadata)
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
        val previousMetadata = index.findMetadataById(model.id)
        if (previousMetadata != null && previousMetadata.key != model.key) {
            discoveredModels.remove(previousMetadata.key)
        }
        readMetadataOptional(model.key, path)?.let { metadata ->
            index.put(metadata)
        }
    }

    /**
     * Builds the lightweight index once at repository startup.
     *
     * Invalid files are ignored here and will fail only if somebody explicitly tries to read them by key.
     * This keeps unrelated repository operations usable even when one file is broken.
     */
    private fun refreshIndex() {
        index.clear()
        for (entry in discoveredModels.entries) {
            val metadata = readMetadataOptional(entry.key, entry.value)
            if (metadata != null) {
                index.put(metadata)
            }
        }
    }

    private fun readMetadataOptional(key: ModelKey, path: Path): ModelMetadata? {
        return try {
            val jsonObject = json.parseToJsonElement(files.load(key)).jsonObject
            val id = jsonObject["id"]?.jsonPrimitive?.content?.let { ModelId.fromString(it) }
            val jsonKey = jsonObject["key"]?.jsonPrimitive?.content?.let { ModelKey(it) } ?: key
            val schema = jsonObject[$$"$schema"]?.jsonPrimitive?.content
            if (id == null) {
                logger.warn("Ignoring model file without id in repository index: {}", path)
                return null
            }
            ModelMetadata(
                id = id,
                key = jsonKey,
                schema = schema,
                path = path
            )
        } catch (e: Exception) {
            logger.warn("Ignoring unreadable model file in repository index: {}", path, e)
            null
        }
    }

    companion object {
        val REPOSITORY_ID = ModelRepositoryId("json")
        val logger: Logger = LoggerFactory.getLogger(ModelsStorageJsonRepository::class.java)
    }

}

internal data class ModelMetadata(
    val id: ModelId,
    val key: ModelKey,
    val schema: String?,
    val path: Path
)

internal class ModelJsonIndex {
    private val metadataByKey = LinkedHashMap<ModelKey, ModelMetadata>()
    private val modelKeyById = LinkedHashMap<ModelId, ModelKey>()

    fun clear() {
        metadataByKey.clear()
        modelKeyById.clear()
    }

    fun put(metadata: ModelMetadata) {
        val previousByKey = metadataByKey.put(metadata.key, metadata)
        if (previousByKey != null) {
            modelKeyById.remove(previousByKey.id)
        }

        val previousKeyForId = modelKeyById[metadata.id]
        if (previousKeyForId != null && previousKeyForId != metadata.key) {
            val previousMetadata = metadataByKey[previousKeyForId]
                ?: throw ModelJsonRepositoryException("Repository index corrupted for model id ${metadata.id.value}")
            throw ModelJsonRepositoryDuplicateModelIdException(
                metadata.id,
                previousMetadata.path.toString(),
                metadata.path.toString()
            )
        }

        modelKeyById[metadata.id] = metadata.key
    }

    fun remove(metadata: ModelMetadata) {
        metadataByKey.remove(metadata.key)
        modelKeyById.remove(metadata.id)
    }

    fun findMetadataById(id: ModelId): ModelMetadata? {
        val key = modelKeyById[id] ?: return null
        return metadataByKey[key]
    }

    fun findKeyById(id: ModelId): ModelKey? = modelKeyById[id]

    fun findAllModelIds(): List<ModelId> = metadataByKey.values.map { it.id }
}
