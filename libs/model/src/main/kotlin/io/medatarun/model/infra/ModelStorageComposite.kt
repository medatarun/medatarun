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

    override fun delete(modelId: ModelId) {
        val repo = findRepoWithModel(modelId)
        repo.delete(modelId)
    }

    override fun createEntity(modelId: ModelId, e: ModelEntity) {
        val repo = findRepoWithModel(modelId)
        repo.createEntity(modelId, e)
    }

    override fun updateEntityName(modelId: ModelId, entityId: ModelEntityId, newEntityId: ModelEntityId) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityName(modelId, entityId, newEntityId)
    }

    override fun updateEntityTitle(modelId: ModelId, entityId: ModelEntityId, title: LocalizedText?) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityTitle(modelId, entityId, title)
    }

    override fun updateEntityDescription(modelId: ModelId, entityId: ModelEntityId, description: LocalizedMarkdown?) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityDescription(modelId, entityId, description)
    }

    override fun deleteEntity(modelId: ModelId, entityId: ModelEntityId) {
        val repo = findRepoWithModel(modelId)
        repo.deleteEntity(modelId, entityId)
    }

    override fun createEntityAttribute(
        modelId: ModelId,
        entityId: ModelEntityId,
        attr: ModelAttribute
    ) {
        val repo = findRepoWithModel(modelId)
        repo.createEntityAttribute(modelId, entityId, attr)
    }

    override fun updateEntityAttributeName(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        newAttributeId: ModelAttributeId
    ) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityAttributeName(modelId, entityId, attributeId, newAttributeId)
    }

    override fun updateEntityAttributeTitle(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        title: LocalizedText?
    ) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityAttributeTitle(modelId, entityId, attributeId, title)
    }

    override fun updateEntityAttributeDescription(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        description: LocalizedMarkdown?
    ) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityAttributeDescription(modelId, entityId, attributeId, description)
    }

    override fun updateEntityAttributeType(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        type: ModelTypeId
    ) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityAttributeType(modelId, entityId, attributeId, type)
    }

    override fun updateEntityAttributeOptional(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        optional: Boolean
    ) {
        val repo = findRepoWithModel(modelId)
        repo.updateEntityAttributeOptional(modelId, entityId, attributeId, optional)
    }

    override fun deleteEntityAttribute(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId
    ) {
        val repo = findRepoWithModel(modelId)
        repo.deleteEntityAttribute(modelId, entityId, attributeId)
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
