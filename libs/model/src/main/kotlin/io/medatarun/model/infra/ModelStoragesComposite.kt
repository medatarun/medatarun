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
        if (repositories.isEmpty()) throw ModelStorageCompositeNoRepositoryException()
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
        return findModelByIdOptional(id) ?: throw RepositoryNotFoundForModelException(id)
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

    override fun createEntityDef(modelId: ModelId, e: EntityDef) {
        val repo = findRepoWithModel(modelId)
        repo.createEntityDef(modelId, e)
    }

    override fun updateEntityDefId(modelId: ModelId, entityDefId: EntityDefId, newEntityDefId: EntityDefId) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityDefId(modelId, entityDefId, newEntityDefId)
    }

    override fun updateEntityDefName(modelId: ModelId, entityDefId: EntityDefId, name: LocalizedText?) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityDefName(modelId, entityDefId, name)
    }

    override fun updateEntityDefDescription(
        modelId: ModelId,
        entityDefId: EntityDefId,
        description: LocalizedMarkdown?
    ) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityDefDescription(modelId, entityDefId, description)
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

    override fun updateEntityDefAttributeDefId(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        newAttributeDefId: AttributeDefId
    ) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityDefAttributeDefId(modelId, entityDefId, attributeDefId, newAttributeDefId)
    }

    override fun updateEntityDefAttributeDefName(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        name: LocalizedText?
    ) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityDefAttributeDefName(modelId, entityDefId, attributeDefId, name)
    }

    override fun updateEntityDefAttributeDefDescription(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        description: LocalizedMarkdown?
    ) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityDefAttributeDefDescription(modelId, entityDefId, attributeDefId, description)
    }

    override fun updateEntityDefAttributeDefType(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        type: ModelTypeId
    ) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityDefAttributeDefType(modelId, entityDefId, attributeDefId, type)
    }

    override fun updateEntityDefAttributeDefOptional(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        optional: Boolean
    ) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityDefAttributeDefOptional(modelId, entityDefId, attributeDefId, optional)
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
        throw RepositoryNotFoundForModelException(id)
    }

    fun selectRepository(ref: RepositoryRef): ModelRepository {
        return when (ref) {
            is RepositoryRef.Auto -> {
                if (repositories.size > 1) throw ModelStorageAmbiguousException()
                else repositories.first()
            }

            is RepositoryRef.Id -> {
                repositories.firstOrNull { it.matchesId(ref.id) }
                    ?: throw ModelStorageCompositeRepositoryNotFoundException(ref.id)
            }

        }
    }

}

class RepositoryNotFoundForModelException(id: ModelId) :
    MedatarunException("No repository currently has model ${id.value}")

class ModelStorageCompositeNoRepositoryException :
    MedatarunException("Could not find any repositories")

class ModelStorageCompositeRepositoryNotFoundException(val id: ModelRepositoryId) :
    MedatarunException("Could not find repository with id ${id.value}")

class ModelStorageAmbiguousException :
    MedatarunException("Action should specify with which repository we must proceed.")


