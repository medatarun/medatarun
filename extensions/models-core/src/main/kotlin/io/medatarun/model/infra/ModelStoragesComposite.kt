package io.medatarun.model.infra

import io.medatarun.lang.exceptions.MedatarunException
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

    override fun existsModelByKey(modelKey: ModelKey): Boolean {
        for (repository in repositories) {
            val found = repository.findModelByKeyOptional(modelKey)
            if (found != null) return true
        }
        return false
    }

    override fun findModelByKeyOptional(modelKey: ModelKey): Model? {
        for (repository in repositories) {
            val found = repository.findModelByKeyOptional(modelKey)
            if (found != null) {
                when (val validation = modelValidation.validate(found)) {
                    is ModelValidationState.Ok -> return found
                    is ModelValidationState.Error -> throw ModelInvalidException(found.id, validation.errors)
                }

            }
        }
        return null
    }

    override fun findModelByIdOptional(modelId: ModelId): Model? {
        for (repository in repositories) {
            val found = repository.findModelByIdOptional(modelId)
            if (found != null) {
                when (val validation = modelValidation.validate(found)) {
                    is ModelValidationState.Ok -> return found
                    is ModelValidationState.Error -> throw ModelInvalidException(found.id, validation.errors)
                }

            }
        }
        return null
    }

    override fun findModelByKey(key: ModelKey): Model {
        return findModelByKeyOptional(key) ?: throw ModelStoragesCompositeRepositoryNotFoundInModelException(key.value)
    }

    override fun findModelById(modelId: ModelId): Model {
        return findModelByIdOptional(modelId) ?: throw ModelStoragesCompositeRepositoryNotFoundInModelException(modelId.asString())
    }

    override fun findAllModelIds(): List<ModelId> {
        return repositories.flatMap { it.findAllModelIds() }
    }


    override fun dispatch(cmd: ModelRepositoryCmd, repositoryRef: RepositoryRef) {
        if (cmd is ModelRepositoryCmdOnModel) {
            val repo = findRepoWithModel(cmd.modelKey)
            repo.dispatch(cmd)
        } else {
            selectRepository(repositoryRef).dispatch(cmd)
        }
    }

    private fun findRepoWithModel(key: ModelKey): ModelRepository {
        for (repository in repositories) {
            val found = repository.findModelByKeyOptional(key)
            if (found != null) return repository
        }
        throw ModelStoragesCompositeRepositoryNotFoundInModelException(key.value)
    }

    fun selectRepository(ref: RepositoryRef): ModelRepository {
        return when (ref) {
            is RepositoryRef.Auto -> {
                if (repositories.size > 1) throw ModelStoragesAmbiguousRepositoryException()
                else repositories.first()
            }

            is RepositoryRef.Id -> {
                repositories.firstOrNull { it.matchesId(ref.id) }
                    ?: throw ModelStoragesCompositeRepositoryNotFoundException(ref.id.value)
            }

        }
    }

}

class ModelStoragesCompositeRepositoryNotFoundInModelException(idOrKey: String) :
    MedatarunException("No repository currently has model $idOrKey")

class ModelStoragesCompositeNoRepositoryException :
    MedatarunException("Could not find any repositories")

class ModelStoragesCompositeRepositoryNotFoundException(idOrKey: String) :
    MedatarunException("Could not find repository with id $idOrKey")

class ModelStoragesAmbiguousRepositoryException :
    MedatarunException("Action should specify with which repository we must proceed.")


