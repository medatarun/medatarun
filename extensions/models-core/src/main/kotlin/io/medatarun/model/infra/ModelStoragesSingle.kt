package io.medatarun.model.infra

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelInvalidException
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelValidation
import io.medatarun.model.domain.ModelValidationState
import io.medatarun.model.ports.needs.ModelRepoCmd
import io.medatarun.model.ports.needs.ModelRepository
import io.medatarun.model.ports.needs.ModelStorages
import io.medatarun.model.ports.needs.RepositoryRef

/**
 * Model storage is now backed by a single repository selected by the extension itself.
 *
 * Repository references are kept in the command API for backward compatibility, but this implementation
 * always dispatches to the same repository.
 */
class ModelStoragesSingle(
    private val repository: ModelRepository,
    private val modelValidation: ModelValidation
) : ModelStorages {

    override fun findAllModelIds(): List<ModelId> {
        return repository.findAllModelIds()
    }

    override fun findModelByKey(key: ModelKey): Model {
        return findModelByKeyOptional(key) ?: throw ModelStoragesSingleRepositoryNotFoundInModelException(key.value)
    }

    override fun findModelByKeyOptional(modelKey: ModelKey): Model? {
        val found = repository.findModelByKeyOptional(modelKey) ?: return null
        return validate(found)
    }

    override fun findModelById(modelId: ModelId): Model {
        return findModelByIdOptional(modelId)
            ?: throw ModelStoragesSingleRepositoryNotFoundInModelException(modelId.asString())
    }

    override fun findModelByIdOptional(modelId: ModelId): Model? {
        val found = repository.findModelByIdOptional(modelId) ?: return null
        return validate(found)
    }

    override fun existsModelByKey(modelKey: ModelKey): Boolean {
        return repository.findModelByKeyOptional(modelKey) != null
    }

    override fun existsModelById(modelId: ModelId): Boolean {
        return repository.findModelByIdOptional(modelId) != null
    }

    override fun dispatch(cmd: ModelRepoCmd, repositoryRef: RepositoryRef) {
        repository.dispatch(cmd)
    }

    private fun validate(model: Model): Model {
        return when (val validation = modelValidation.validate(model)) {
            is ModelValidationState.Ok -> model
            is ModelValidationState.Error -> throw ModelInvalidException(model.id, validation.errors)
        }
    }
}

class ModelStoragesSingleRepositoryNotFoundInModelException(idOrKey: String) :
    MedatarunException("No repository currently has model $idOrKey")
