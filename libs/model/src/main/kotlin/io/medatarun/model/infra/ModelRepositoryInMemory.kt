package io.medatarun.model.infra

import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelRepository
import io.medatarun.model.ports.ModelRepositoryCmd
import io.medatarun.model.ports.ModelRepositoryId

/**
 * A model repository suitable in memory, mostly used in tests
 */
class ModelRepositoryInMemory(val identifier: String) : ModelRepository {
    val repositoryId = ModelRepositoryId(identifier)

    val models = mutableMapOf<ModelId, ModelInMemory>()

    override fun matchesId(id: ModelRepositoryId): Boolean {
        return id == repositoryId
    }

    override fun findAllModelIds(): List<ModelId> {
        return models.keys.toList()
    }

    override fun findModelByIdOptional(id: ModelId): Model? {
        return models[id]
    }

    override fun createModel(model: Model) {
        models[model.id] = ModelInMemory.of(model)
    }

    private fun updateModel(modelId: ModelId, block: (model: ModelInMemory) -> ModelInMemory) {
        val model = models[modelId] ?: throw ModelRepositoryInMemoryExceptions(modelId)
        models[modelId] = block(model)
    }

    override fun updateModelName(modelId: ModelId, name: LocalizedTextNotLocalized) {
        updateModel(modelId) { it.copy(name = name) }
    }

    override fun updateModelDescription(
        modelId: ModelId,
        description: LocalizedTextNotLocalized?
    ) {
        updateModel(modelId) { it.copy(description = description) }
    }

    override fun updateModelVersion(
        modelId: ModelId,
        version: ModelVersion
    ) {
        updateModel(modelId) { it.copy(version = version) }
    }

    override fun deleteModel(modelId: ModelId) {
        models.remove(modelId)
    }


    override fun dispatch(cmd: ModelRepositoryCmd) {
        updateModel(cmd.modelId) { model -> ModelInMemoryReducer().dispatch(model, cmd) }
    }

    /**
     * Pushes a model in the list of known models. Don't check if it's valid or not, on purpose.
     * Very dangerous to use, mostly for tests
     */
    fun push(model: ModelInMemory) {
        this.models[model.id] = model
    }
}

class ModelRepositoryInMemoryExceptions(modelId: ModelId) :
    MedatarunException("Model not found in repository ${modelId.value}")