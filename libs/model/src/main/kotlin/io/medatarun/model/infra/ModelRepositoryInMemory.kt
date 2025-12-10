package io.medatarun.model.infra

import io.medatarun.model.domain.MedatarunException
import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelId
import io.medatarun.model.ports.needs.ModelRepository
import io.medatarun.model.ports.needs.ModelRepositoryCmd
import io.medatarun.model.ports.needs.ModelRepositoryCmdOnModel
import io.medatarun.model.ports.needs.ModelRepositoryId

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

    private fun createModel(model: Model) {
        models[model.id] = ModelInMemory.of(model)
    }

    private fun updateModel(modelId: ModelId, block: (model: ModelInMemory) -> ModelInMemory) {
        val model = models[modelId] ?: throw ModelRepositoryInMemoryExceptions(modelId)
        models[modelId] = block(model)
    }

    private fun deleteModel(modelId: ModelId) {
        models.remove(modelId)
    }


    override fun dispatch(cmd: ModelRepositoryCmd) {
        when(cmd) {
            is ModelRepositoryCmd.CreateModel -> createModel(cmd.model)
            is ModelRepositoryCmd.DeleteModel -> deleteModel(cmd.modelId)
            is ModelRepositoryCmdOnModel -> updateModel(cmd.modelId) { model -> ModelInMemoryReducer().dispatch(model, cmd) }
        }

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