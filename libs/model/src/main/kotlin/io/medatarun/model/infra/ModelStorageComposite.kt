package io.medatarun.model.infra

import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelStorage

class ModelStorageComposite(
    val repositories: List<ModelRepository>
) : ModelStorage {
    override fun findById(id: ModelId): Model {
        for (repository in repositories) {
            val found = repository.findByIdOptional(id)
            if (found != null) return found
        }
        throw ModelNotFoundException(id)
    }

    override fun findAllIds(): List<ModelId> {
        return repositories.map { it.findAllIds() }.flatten()
    }

    override fun create(model: Model) {
        repositories.first().create(model)
    }

    override fun createEntity(modelId: ModelId, e: ModelEntity) {
        val repo = findRepoWithModel(modelId)
        repo.createEntity(modelId, e)
    }

    override fun createEntityAttribute(
        modelId: ModelId,
        entityId: ModelEntityId,
        attr: ModelAttribute
    ) {
        val repo = findRepoWithModel(modelId)
        repo.createEntityAttribute(modelId, entityId, attr)
    }

    private fun findRepoWithModel(id: ModelId): ModelRepository {
        for (repository in repositories) {
            val found = repository.findByIdOptional(id)
            if (found != null) return repository
        }
        throw RepositoryNotFoundException(id)
    }


}

class RepositoryNotFoundException(id: ModelId) : MedatarunException("No repository currently has model ${id.value}")