package io.medatarun.model.infra

import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelRepository
import io.medatarun.model.ports.ModelRepositoryId
import io.medatarun.model.ports.ModelStorages
import io.medatarun.model.ports.RepositoryRef

/**
 * Default implementation of [ModelStorages] that acts by using all
 * available [ModelRepository] declared in the contribution point.
 */
class ModelStoragesComposite(
    val repositories: List<ModelRepository>
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
            if (found != null) return found
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

    override fun updateModelName(modelId: ModelId, name: LocalizedTextNotLocalized) {
        val repo = findRepoWithModel(modelId)
        repo.updateModelName(modelId, name)
    }

    override fun updateModelDescription(modelId: ModelId, description: LocalizedTextNotLocalized?) {
        val repo = findRepoWithModel(modelId)
        repo.updateModelDescription(modelId, description)
    }

    override fun updateModelVersion(modelId: ModelId, version: ModelVersion) {
        val repo = findRepoWithModel(modelId)
        repo.updateModelVersion(modelId, version)
    }

    override fun deleteModel(modelId: ModelId) {
        val repo = findRepoWithModel(modelId)
        repo.deleteModel(modelId)
    }

    override fun createType(modelId: ModelId, initializer: ModelTypeInitializer) {
        val repo = findRepoWithModel(modelId)
        repo.createType(modelId, initializer)
    }

    override fun updateType(modelId: ModelId, typeId: ModelTypeId, cmd: ModelTypeUpdateCmd) {
        val repo = findRepoWithModel(modelId)
        repo.updateType(modelId, typeId, cmd)
    }

    override fun deleteType(modelId: ModelId, typeId: ModelTypeId) {
        val repo = findRepoWithModel(modelId)
        repo.deleteType(modelId, typeId)
    }

    override fun createEntityDef(modelId: ModelId, e: EntityDef) {
        val repo = findRepoWithModel(modelId)
        repo.createEntityDef(modelId, e)
    }

    override fun updateEntityDef(modelId: ModelId, entityDefId: EntityDefId, cmd: EntityDefUpdateCmd) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityDef(modelId, entityDefId, cmd)
    }

    override fun deleteEntityDef(modelId: ModelId, entityDefId: EntityDefId) {
        val repo = findRepoWithModel(modelId)
        repo.deleteEntityDef(modelId, entityDefId)
    }

    override fun createEntityDefAttributeDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attr: AttributeDef
    ) {
        val repo = findRepoWithModel(modelId)
        repo.createEntityDefAttributeDef(modelId, entityDefId, attr)
    }

    override fun updateEntityDefAttributeDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        cmd: AttributeDefUpdateCmd
    ) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityDefAttributeDef(modelId, entityDefId, attributeDefId, cmd)
    }

    override fun deleteEntityDefAttributeDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId
    ) {
        val repo = findRepoWithModel(modelId)
        repo.deleteEntityDefAttributeDef(modelId, entityDefId, attributeDefId)
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


