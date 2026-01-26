package io.medatarun.model.infra

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelId
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

    val models = mutableMapOf<ModelId, ModelInMemory>()

    override fun matchesId(id: ModelRepositoryId): Boolean {
        return id == repositoryId
    }

    override fun findModelByKeyOptional(key: ModelKey): ModelInMemory? {
        return models.values.firstOrNull {it.key == key }
    }

    override fun findAllModelIds(): List<ModelId> {
        return models.keys.toList()
    }

    override fun findModelByIdOptional(id: ModelId): ModelInMemory? {
        return models[id]
    }

    private fun createModel(model: Model) {
        models[model.id] = ModelInMemory.of(model)
    }

    private fun updateModel(modelId: ModelId, block: (model: ModelInMemory) -> ModelInMemory) {

        val model = findModelByIdOptional(modelId) ?: throw ModelRepositoryInMemoryExceptions(modelId)
        models[model.id] = block(model)
    }

    private fun deleteModel(modelId: ModelId) {
        val toDelete = findModelByIdOptional(modelId) ?: throw ModelRepositoryInMemoryExceptions(modelId)
        models.remove(toDelete.id)
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