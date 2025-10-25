package io.medatarun.model.internal

import io.medatarun.model.infra.AttributeDefInMemory
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.infra.ModelInMemory
import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelStorages
import io.medatarun.model.ports.RepositoryRef
import javax.xml.validation.TypeInfoProvider

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

    // -----------------------------------------------------------------------------------------------------------------
    // Types
    // -----------------------------------------------------------------------------------------------------------------

    override fun createType(modelId: ModelId, initializer: ModelTypeInitializer) {
        // Can not create a type if another has the same type
        val model = storage.findModelByIdOptional(modelId) ?: throw ModelNotFoundException(modelId)
        val existing = model.findTypeOptional(initializer.id)
        if (existing != null) throw TypeCreateDuplicateException(modelId, initializer.id)
        storage.createType(modelId, initializer)
    }

    override fun updateType(modelId: ModelId, typeId: ModelTypeId, cmd: ModelTypeUpdateCmd) {
        val model = storage.findModelByIdOptional(modelId) ?: throw ModelNotFoundException(modelId)
        model.findTypeOptional(typeId) ?: throw TypeNotFoundException(modelId, typeId)
        storage.updateType(modelId, typeId, cmd)
    }

    override fun deleteType(modelId: ModelId, typeId: ModelTypeId) {
        // Can not delete type used in any entity
        val model = storage.findModelByIdOptional(modelId) ?: throw ModelNotFoundException(modelId)
        val used = model.entityDefs.any { entityDef -> entityDef.attributes.any { attr -> attr.type == typeId } }
        if (used) throw ModelTypeDeleteUsedException(typeId)
        model.findTypeOptional(typeId) ?: throw TypeNotFoundException(modelId, typeId)
        storage.deleteType(modelId, typeId)
    }
    // -----------------------------------------------------------------------------------------------------------------
    // Entities
    // -----------------------------------------------------------------------------------------------------------------

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

        ensureTypeExists(modelId, entityDefInitializer.identityAttribute.type)
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
        // TODO can not delete attribute used as identifier

        storage.deleteEntityDefAttributeDef(modelId, entityDefId, attributeDefId)
    }

    override fun updateEntityDefAttributeDef(
        modelId: ModelId,
        entityDefId: EntityDefId,
        attributeDefId: AttributeDefId,
        cmd: AttributeDefUpdateCmd
    ) {
        val entity = findEntityDefById(modelId, entityDefId)
        entity.ensureAttributeDefExists(attributeDefId)

        // TODO how do we ensure transactions here ?

        // We can not have two attributes with the same id
        if (cmd is AttributeDefUpdateCmd.Id) {
            if (entity.attributes.any { it.id == cmd.value && it.id != attributeDefId }) {
                throw UpdateAttributeDefDuplicateIdException(entityDefId, attributeDefId)
            }
        }

        // If user wants to rename the Entity's identity attribute, we must rename in entity
        // as well as the attribute's id, then apply changes on entity
        if (cmd is AttributeDefUpdateCmd.Id) {
            if (entity.identifierAttributeDefId == attributeDefId) {
                storage.updateEntityDef(
                    modelId,
                    entityDefId,
                    EntityDefUpdateCmd.IdentifierAttribute(cmd.value)
                )
            }
        }

        // Apply changes on attribute
        storage.updateEntityDefAttributeDef(modelId, entityDefId, attributeDefId, cmd)
    }

    fun ensureModelExists(modelId: ModelId) {
        if (!storage.existsModelById(modelId)) throw ModelNotFoundException(modelId)
    }

    fun ensureTypeExists(modelId: ModelId, typeId: ModelTypeId): ModelType {
        val model = storage.findModelByIdOptional(modelId) ?: throw ModelNotFoundException(modelId)
        return model.findTypeOptional(typeId) ?: throw TypeNotFoundException(modelId, typeId)
    }

    fun findEntityDefById(modelId: ModelId, entityDefId: EntityDefId): EntityDef {
        val m = storage.findModelByIdOptional(modelId) ?: throw ModelNotFoundException(modelId)
        return m.findEntityDef(entityDefId)
    }
}
