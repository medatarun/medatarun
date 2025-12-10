package io.medatarun.model.infra

import io.medatarun.model.domain.*
import io.medatarun.model.ports.needs.*

/**
 * Default implementation of [io.medatarun.model.ports.needs.ModelStorages] that acts by using all
 * available [io.medatarun.model.ports.needs.ModelRepository] declared in the contribution point.
 */
class ModelStoragesComposite(
    val repositories: List<ModelRepository>,
    val modelValidation: ModelValidation,
) : ModelStorages {

    init {
        if (repositories.isEmpty()) throw ModelStoragesCompositeNoRepositoryException()
    }

    override fun existsModelById(modelKey: ModelKey): Boolean {
        for (repository in repositories) {
            val found = repository.findModelByIdOptional(modelKey)
            if (found != null) return true
        }
        return false
    }

    override fun findModelByIdOptional(modelKey: ModelKey): Model? {
        for (repository in repositories) {
            val found = repository.findModelByIdOptional(modelKey)
            if (found != null) {
                when (val validation = modelValidation.validate(found)) {
                    is ModelValidationState.Ok -> return found
                    is ModelValidationState.Error -> throw ModelInvalidException(modelKey, validation.errors)
                }

            }
        }
        return null
    }

    override fun findModelById(id: ModelKey): Model {
        return findModelByIdOptional(id) ?: throw ModelStoragesCompositeRepositoryNotFoundInModelException(id)
    }

    override fun findAllModelIds(): List<ModelKey> {
        return repositories.map { it.findAllModelIds() }.flatten()
    }


    override fun dispatch(cmd: ModelRepositoryCmd, repositoryRef: RepositoryRef) {
        if (cmd is ModelRepositoryCmdOnModel) {
            val repo = findRepoWithModel(cmd.modelKey)
            repo.dispatch(cmd)
        } else {
            selectRepository(repositoryRef).dispatch(cmd)
        }
    }

    private fun findRepoWithModel(id: ModelKey): ModelRepository {
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

class ModelStoragesCompositeRepositoryNotFoundInModelException(id: ModelKey) :
    MedatarunException("No repository currently has model ${id.value}")

class ModelStoragesCompositeNoRepositoryException :
    MedatarunException("Could not find any repositories")

class ModelStoragesCompositeRepositoryNotFoundException(val id: ModelRepositoryId) :
    MedatarunException("Could not find repository with id ${id.value}")

class ModelStoragesAmbiguousRepositoryException :
    MedatarunException("Action should specify with which repository we must proceed.")


