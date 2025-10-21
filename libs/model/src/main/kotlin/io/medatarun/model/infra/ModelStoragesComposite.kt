package io.medatarun.model.infra

import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelRepository
import io.medatarun.model.ports.ModelStorages

/**
 * Default implementation of [ModelStorages] that acts by using all
 * available [ModelRepository] declared in the contribution point.
 */
class ModelStoragesComposite(
    val repositories: List<ModelRepository>
) : ModelStorages {

    override fun findModelById(id: ModelId): Model {
        for (repository in repositories) {
            val found = repository.findModelByIdOptional(id)
            if (found != null) return found
        }
        throw ModelNotFoundException(id)
    }

    override fun findAllModelIds(): List<ModelId> {
        return repositories.map { it.findAllModelIds() }.flatten()
    }

    override fun createModel(model: Model) {
        repositories.first().createModel(model)
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
        throw RepositoryNotFoundException(id)
    }


}

class RepositoryNotFoundException(id: ModelId) : MedatarunException("No repository currently has model ${id.value}")
