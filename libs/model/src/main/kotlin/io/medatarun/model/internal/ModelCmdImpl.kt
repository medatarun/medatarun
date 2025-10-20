package io.medatarun.model.internal

import io.medatarun.model.infra.ModelAttributeInMemory
import io.medatarun.model.infra.ModelEntityInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelStorage

class ModelCmdImpl(val storage: ModelStorage) : ModelCmd {
    override fun create(id: ModelId, name: LocalizedText, description: LocalizedMarkdown?, version: ModelVersion) {
        val model = ModelInMemory(
            id = id,
            name = name,
            description = description,
            version = version,
            entities = emptyList()
        )
        storage.create(model)
    }

    override fun delete(modelId: ModelId) {
        storage.delete(modelId)
    }

    override fun updateEntityName(modelId: ModelId, entityId: ModelEntityId, newEntityId: ModelEntityId) {
        storage.updateEntityName(modelId, entityId, newEntityId)
    }

    override fun updateEntityTitle(modelId: ModelId, entityId: ModelEntityId, title: LocalizedText?) {
        storage.updateEntityTitle(modelId, entityId, title)
    }

    override fun updateEntityDescription(modelId: ModelId, entityId: ModelEntityId, description: LocalizedMarkdown?) {
        storage.updateEntityDescription(modelId, entityId, description)
    }

    override fun createEntity(modelId: ModelId, entityId: ModelEntityId, name: LocalizedText?, description: LocalizedMarkdown?) {
        storage.createEntity(modelId, ModelEntityInMemory(
            id = entityId,
            name = name,
            description = description,
            attributes = emptyList()
        ))
    }

    override fun updateEntityAttributeName(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        newAttributeId: ModelAttributeId
    ) {
        storage.updateEntityAttributeName(modelId, entityId, attributeId, newAttributeId)
    }

    override fun updateEntityAttributeTitle(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        title: LocalizedText?
    ) {
        storage.updateEntityAttributeTitle(modelId, entityId, attributeId, title)
    }

    override fun updateEntityAttributeDescription(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        description: LocalizedMarkdown?
    ) {
        storage.updateEntityAttributeDescription(modelId, entityId, attributeId, description)
    }

    override fun deleteEntity(modelId: ModelId, entityId: ModelEntityId) {
        storage.deleteEntity(modelId, entityId)
    }

    override fun createEntityAttribute(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        type: ModelTypeId,
        optional: Boolean,
        name: LocalizedText?,
        description: LocalizedMarkdown?
    ) {
        storage.createEntityAttribute(modelId, entityId, ModelAttributeInMemory(
            id = attributeId,
            name = name,
            description = description,
            type = type,
            optional = optional
        ))
    }

    override fun deleteEntityAttribute(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId
    ) {
        storage.deleteEntityAttribute(modelId, entityId, attributeId)
    }
}
