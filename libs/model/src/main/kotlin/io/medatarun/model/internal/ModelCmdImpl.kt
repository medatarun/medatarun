package io.medatarun.model.internal

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
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
            entityDefs = emptyList()
        )
        storage.createModel(model)
    }

    override fun delete(modelId: ModelId) {
        storage.deleteModel(modelId)
    }

    override fun updateEntityName(modelId: ModelId, entityDefId: EntityDefId, newEntityId: EntityDefId) {
        storage.updateEntityDefId(modelId, entityDefId, newEntityId)
    }

    override fun updateEntityTitle(modelId: ModelId, entityId: EntityDefId, title: LocalizedText?) {
        storage.updateEntityDefName(modelId, entityId, title)
    }

    override fun updateEntityDescription(modelId: ModelId, entityId: EntityDefId, description: LocalizedMarkdown?) {
        storage.updateEntityDefDescription(modelId, entityId, description)
    }

    override fun createEntityDef(modelId: ModelId, entityDefId: EntityDefId, name: LocalizedText?, description: LocalizedMarkdown?) {
        storage.createEntityDef(modelId, EntityDefInMemory(
            id = entityDefId,
            name = name,
            description = description,
            attributes = emptyList()
        ))
    }

    override fun updateEntityAttributeName(
        modelId: ModelId,
        entityId: EntityDefId,
        attributeId: AttributeDefId,
        newAttributeId: AttributeDefId
    ) {
        storage.updateEntityDefAttributeDefId(modelId, entityId, attributeId, newAttributeId)
    }

    override fun updateEntityAttributeTitle(
        modelId: ModelId,
        entityId: EntityDefId,
        attributeId: AttributeDefId,
        title: LocalizedText?
    ) {
        storage.updateEntityDefAttributeDefName(modelId, entityId, attributeId, title)
    }

    override fun updateEntityAttributeDescription(
        modelId: ModelId,
        entityId: EntityDefId,
        attributeId: AttributeDefId,
        description: LocalizedMarkdown?
    ) {
        storage.updateEntityDefAttributeDefDescription(modelId, entityId, attributeId, description)
    }

    override fun updateEntityAttributeType(
        modelId: ModelId,
        entityId: EntityDefId,
        attributeId: AttributeDefId,
        type: ModelTypeId
    ) {
        storage.updateEntityDefAttributeDefType(modelId, entityId, attributeId, type)
    }

    override fun updateEntityAttributeOptional(
        modelId: ModelId,
        entityId: EntityDefId,
        attributeId: AttributeDefId,
        optional: Boolean
    ) {
        storage.updateEntityDefAttributeDefOptional(modelId, entityId, attributeId, optional)
    }

    override fun deleteEntity(modelId: ModelId, entityId: EntityDefId) {
        storage.deleteEntityDef(modelId, entityId)
    }

    override fun createEntityDefAttributeDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        type: ModelTypeId,
        optional: Boolean,
        name: LocalizedText?,
        description: LocalizedMarkdown?
    ) {
        storage.createEntityDefAttributeDef(modelId, entityDefId, AttributeDefInMemory(
            id = attributeDefId,
            name = name,
            description = description,
            type = type,
            optional = optional
        ))
    }

    override fun deleteEntityAttribute(
        modelId: ModelId,
        entityId: EntityDefId,
        attributeId: AttributeDefId
    ) {
        storage.deleteEntityDefAttributeDef(modelId, entityId, attributeId)
    }
}
