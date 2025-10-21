package io.medatarun.model.internal

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelStorages
import io.medatarun.model.ports.RepositoryRef

class ModelCmdImpl(val storage: ModelStorages) : ModelCmd {
    override fun createModel(
        id: ModelId,
        name: LocalizedText,
        description: LocalizedMarkdown?,
        version: ModelVersion,
        repositoryRef: RepositoryRef
    ) {
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
        ensureModelExists(modelId)
        storage.deleteModel(modelId)
    }

    override fun updateModelName(modelId: ModelId, name: LocalizedTextNotLocalized) {
        ensureModelExists(modelId)
        storage.updateModelName(modelId, name)
    }

    override fun updateModelDescription(modelId: ModelId, description: LocalizedTextNotLocalized?) {
        ensureModelExists(modelId)
        storage.updateModelDescription(modelId, description)
    }

    override fun updateModelVersion(modelId: ModelId, version: ModelVersion) {
        ensureModelExists(modelId)
        storage.updateModelVersion(modelId, version)
    }

    override fun updateEntityDefId(modelId: ModelId, entityDefId: EntityDefId, newEntityDefId: EntityDefId) {
        ensureModelExists(modelId)
        storage.updateEntityDefId(modelId, entityDefId, newEntityDefId)
    }

    override fun updateEntityDefName(modelId: ModelId, entityDefId: EntityDefId, name: LocalizedText?) {
        ensureModelExists(modelId)
        storage.updateEntityDefName(modelId, entityDefId, name)
    }

    override fun updateEntityDefDescription(
        modelId: ModelId,
        entityDefId: EntityDefId,
        description: LocalizedMarkdown?
    ) {
        ensureModelExists(modelId)
        storage.updateEntityDefDescription(modelId, entityDefId, description)
    }

    override fun createEntityDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        name: LocalizedText?,
        description: LocalizedMarkdown?
    ) {
        ensureModelExists(modelId)
        storage.createEntityDef(
            modelId, EntityDefInMemory(
                id = entityDefId,
                name = name,
                description = description,
                attributes = emptyList()
            )
        )
    }

    override fun updateEntityDefAttributeDefId(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        newAttributeDefId: AttributeDefId
    ) {
        ensureModelExists(modelId)
        storage.updateEntityDefAttributeDefId(modelId, entityDefId, attributeDefId, newAttributeDefId)
    }

    override fun updateEntityDefAttributeDefName(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        name: LocalizedText?
    ) {
        ensureModelExists(modelId)
        storage.updateEntityDefAttributeDefName(modelId, entityDefId, attributeDefId, name)
    }

    override fun updateEntityDefAttributeDefDescription(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        description: LocalizedMarkdown?
    ) {
        ensureModelExists(modelId)
        storage.updateEntityDefAttributeDefDescription(modelId, entityDefId, attributeDefId, description)
    }

    override fun updateEntityDefAttributeDefType(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        type: ModelTypeId
    ) {
        ensureModelExists(modelId)
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
        storage.createEntityDefAttributeDef(
            modelId, entityDefId, AttributeDefInMemory(
                id = attributeDefId,
                name = name,
                description = description,
                type = type,
                optional = optional
            )
        )
    }

    override fun deleteEntityDefAttributeDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId
    ) {
        storage.deleteEntityDefAttributeDef(modelId, entityDefId, attributeDefId)
    }

    fun ensureModelExists(modelId: ModelId) {
        if (!storage.existsModelById(modelId)) throw ModelNotFoundException(modelId)
    }
}
