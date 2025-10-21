package io.medatarun.model.internal

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelStorages
import io.medatarun.model.ports.RepositoryRef

class ModelCmdImpl(val storage: ModelStorages) : ModelCmd {
    override fun createModel(id: ModelId, name: LocalizedText, description: LocalizedMarkdown?, version: ModelVersion, repositoryRef: RepositoryRef) {
        val model = ModelInMemory(
            id = id,
            name = name,
            description = description,
            version = version,
            types = emptyList(),
            entityDefs = emptyList(),

        )
        storage.createModel(model, repositoryRef)
    }

    override fun deleteModel(modelId: ModelId) {
        storage.deleteModel(modelId)
    }

    override fun updateEntityDefId(modelId: ModelId, entityDefId: EntityDefId, newEntityDefId: EntityDefId) {
        storage.updateEntityDefId(modelId, entityDefId, newEntityDefId)
    }

    override fun updateEntityDefName(modelId: ModelId, entityDefId: EntityDefId, name: LocalizedText?) {
        storage.updateEntityDefName(modelId, entityDefId, name)
    }

    override fun updateEntityDefDescription(modelId: ModelId, entityDefId: EntityDefId, description: LocalizedMarkdown?) {
        storage.updateEntityDefDescription(modelId, entityDefId, description)
    }

    override fun createEntityDef(modelId: ModelId, entityDefId: EntityDefId, name: LocalizedText?, description: LocalizedMarkdown?) {
        storage.createEntityDef(modelId, EntityDefInMemory(
            id = entityDefId,
            name = name,
            description = description,
            attributes = emptyList()
        ))
    }

    override fun updateEntityDefAttributeDefId(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        newAttributeDefId: AttributeDefId
    ) {
        storage.updateEntityDefAttributeDefId(modelId, entityDefId, attributeDefId, newAttributeDefId)
    }

    override fun updateEntityDefAttributeDefName(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        name: LocalizedText?
    ) {
        storage.updateEntityDefAttributeDefName(modelId, entityDefId, attributeDefId, name)
    }

    override fun updateEntityDefAttributeDefDescription(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        description: LocalizedMarkdown?
    ) {
        storage.updateEntityDefAttributeDefDescription(modelId, entityDefId, attributeDefId, description)
    }

    override fun updateEntityDefAttributeDefType(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        type: ModelTypeId
    ) {
        storage.updateEntityDefAttributeDefType(modelId, entityDefId, attributeDefId, type)
    }

    override fun updateEntityDefAttributeDefOptional(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        optional: Boolean
    ) {
        storage.updateEntityDefAttributeDefOptional(modelId, entityDefId, attributeDefId, optional)
    }

    override fun deleteEntityDef(modelId: ModelId, entityDefId: EntityDefId) {
        storage.deleteEntityDef(modelId, entityDefId)
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

    override fun deleteEntityDefAttributeDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId
    ) {
        storage.deleteEntityDefAttributeDef(modelId, entityDefId, attributeDefId)
    }
}
