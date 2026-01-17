package io.medatarun.model.infra

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.ports.needs.ModelRepository
import io.medatarun.model.ports.needs.ModelRepositoryCmd
import io.medatarun.model.ports.needs.ModelRepositoryCmdOnModel
import io.medatarun.model.ports.needs.ModelRepositoryId

/**
 * A model repository suitable in memory, mostly used in tests
 */
class ModelRepositoryInMemory(val identifier: String) : ModelRepository {
    val repositoryId = ModelRepositoryId(identifier)

    val models = mutableMapOf<ModelKey, ModelInMemory>()

    override fun matchesId(id: ModelRepositoryId): Boolean {
        return id == repositoryId
    }

    override fun findAllModelIds(): List<ModelKey> {
        return models.keys.toList()
    }

    override fun findModelByIdOptional(id: ModelKey): Model? {
        return models[id]
    }

    private fun createModel(model: Model) {
        models[model.id] = ModelInMemory.of(model)
    }

    private fun updateModel(modelKey: ModelKey, block: (model: ModelInMemory) -> ModelInMemory) {
        val model = models[modelKey] ?: throw ModelRepositoryInMemoryExceptions(modelKey)
        models[modelKey] = block(model)
    }

    private fun deleteModel(modelKey: ModelKey) {
        models.remove(modelKey)
    }


    override fun dispatch(cmd: ModelRepositoryCmd) {
        when(cmd) {
            is ModelRepositoryCmd.CreateModel -> createModel(cmd.model)
            is ModelRepositoryCmd.DeleteModel -> deleteModel(cmd.modelKey)
            is ModelRepositoryCmdOnModel -> updateModel(cmd.modelKey) { model -> ModelInMemoryReducer().dispatch(model, cmd) }
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

class ModelRepositoryInMemoryExceptions(modelKey: ModelKey) :
    MedatarunException("Model not found in repository ${modelKey.value}")