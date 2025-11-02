package io.medatarun.model.infra

import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelRepository
import io.medatarun.model.ports.ModelRepositoryCmd
import io.medatarun.model.ports.ModelRepositoryCmdWithId
import io.medatarun.model.ports.ModelRepositoryId
import io.medatarun.model.ports.ModelStorages
import io.medatarun.model.ports.RepositoryRef

/**
 * Default implementation of [ModelStorages] that acts by using all
 * available [ModelRepository] declared in the contribution point.
 */
class ModelStoragesComposite(
    val repositories: List<ModelRepository>,
    val modelValidation: ModelValidation,
) : ModelStorages {

    init {
        if (repositories.isEmpty()) throw ModelStoragesCompositeNoRepositoryException()
    }

    override fun existsModelById(modelId: ModelId): Boolean {
        for (repository in repositories) {
            val found = repository.findModelByIdOptional(modelId)
            if (found != null) return true
        }
        return false
    }

    override fun findModelByIdOptional(modelId: ModelId): Model? {
        for (repository in repositories) {
            val found = repository.findModelByIdOptional(modelId)
            if (found != null) {
                when (val validation = modelValidation.validate(found)) {
                    is ModelValidationState.Ok -> return found
                    is ModelValidationState.Error -> throw ModelInvalidException(modelId, validation.errors)
                }

            }
        }
        return null
    }

    override fun findModelById(id: ModelId): Model {
        return findModelByIdOptional(id) ?: throw ModelStoragesCompositeRepositoryNotFoundInModelException(id)
    }

    override fun findAllModelIds(): List<ModelId> {
        return repositories.map { it.findAllModelIds() }.flatten()
    }

    override fun createModel(model: Model, repositoryRef: RepositoryRef) {
        selectRepository(repositoryRef).createModel(model)
    }


    override fun dispatch(cmd: ModelRepositoryCmd, repositoryRef: RepositoryRef) {
        if (cmd is ModelRepositoryCmdWithId) {
            val repo = findRepoWithModel(cmd.modelId)
            repo.dispatch(cmd)
        } else {
            selectRepository(repositoryRef).dispatch(cmd)
        }
    }

    private fun findRepoWithModel(id: ModelId): ModelRepository {
        for (repository in repositories) {
            val found = repository.findModelByIdOptional(id)
            if (found != null) return repository
        }
        throw ModelStoragesCompositeRepositoryNotFoundInModelException(id)
    }

    fun selectRepository(ref: RepositoryRef): ModelRepository {
        return when (ref) {
            is RepositoryRef.Auto -> {
                if (repositories.size > 1) throw ModelStoragesAmbiguousRepositoryException()
                else repositories.first()
            }

            is RepositoryRef.Id -> {
                repositories.firstOrNull { it.matchesId(ref.id) }
                    ?: throw ModelStoragesCompositeRepositoryNotFoundException(ref.id)
            }

        }
    }

}

class ModelStoragesCompositeRepositoryNotFoundInModelException(id: ModelId) :
    MedatarunException("No repository currently has model ${id.value}")

class ModelStoragesCompositeNoRepositoryException :
    MedatarunException("Could not find any repositories")

class ModelStoragesCompositeRepositoryNotFoundException(val id: ModelRepositoryId) :
    MedatarunException("Could not find repository with id ${id.value}")

class ModelStoragesAmbiguousRepositoryException :
    MedatarunException("Action should specify with which repository we must proceed.")


