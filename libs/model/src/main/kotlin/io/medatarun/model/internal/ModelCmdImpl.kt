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

    override fun updateEntityDef(modelId: ModelId, entityDefId: EntityDefId, cmd: EntityDefUpdateCmd) {
        ensureModelExists(modelId)
        val model = storage.findModelById(modelId)
        model.findEntityDef(entityDefId)
        if (cmd is EntityDefUpdateCmd.Id) {
            if (model.entityDefs.any { it.id == cmd.value && it.id != entityDefId }) {
                throw UpdateEntityDefIdDuplicateIdException(entityDefId)
            }
        }
        storage.updateEntityDef(modelId, entityDefId, cmd)
    }


    override fun createEntityDef(
        modelId: ModelId,
        entityDefInitializer: EntityDefInitializer


    ) {
        ensureModelExists(modelId)
        storage.createEntityDef(
            modelId, EntityDefInMemory(
                id = entityDefInitializer.entityDefId,
                name = entityDefInitializer.name,
                description = entityDefInitializer.description,
                identifierAttributeDefId = entityDefInitializer.identityAttribute.attributeDefId,
                attributes = listOf(
                    AttributeDefInMemory(
                        id = entityDefInitializer.identityAttribute.attributeDefId,
                        name = entityDefInitializer.identityAttribute.name,
                        description = entityDefInitializer.identityAttribute.description,
                        type = entityDefInitializer.identityAttribute.type,
                        optional = false // because it's identity, can never be optional
                    )
                )
            )
        )
    }

    override fun deleteEntityDef(modelId: ModelId, entityDefId: EntityDefId) {
        storage.deleteEntityDef(modelId, entityDefId)
    }

    override fun createEntityDefAttributeDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefInitializer: AttributeDefInitializer,
    ) {
        val e = findEntityDefById(modelId, entityDefId)
        if (e.hasAttributeDef(attributeDefInitializer.attributeDefId)) throw CreateAttributeDefDuplicateIdException(
            entityDefId,
            attributeDefInitializer.attributeDefId
        )
        storage.createEntityDefAttributeDef(
            modelId, entityDefId, AttributeDefInMemory(
                id = attributeDefInitializer.attributeDefId,
                name = attributeDefInitializer.name,
                description = attributeDefInitializer.description,
                type = attributeDefInitializer.type,
                optional = attributeDefInitializer.optional
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

    override fun updateEntityDefAttributeDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        target: AttributeDefUpdateCmd
    ) {
        val entity = findEntityDefById(modelId, entityDefId)
        entity.ensureAttributeDefExists(attributeDefId)
        if (target is AttributeDefUpdateCmd.Id) {
            if (entity.attributes.any { it.id == target.value && it.id != attributeDefId }) {
                throw UpdateAttributeDefDuplicateIdException(entityDefId, attributeDefId)
            }
        }
        storage.updateEntityDefAttributeDef(modelId, entityDefId, attributeDefId, target)
    }

    fun ensureModelExists(modelId: ModelId) {
        if (!storage.existsModelById(modelId)) throw ModelNotFoundException(modelId)
    }

    fun findEntityDefById(modelId: ModelId, entityDefId: EntityDefId): EntityDef {
        val m = storage.findModelByIdOptional(modelId) ?: throw ModelNotFoundException(modelId)
        return m.findEntityDef(entityDefId)
    }
}
