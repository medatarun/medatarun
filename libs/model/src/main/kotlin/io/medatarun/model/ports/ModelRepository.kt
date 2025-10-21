package io.medatarun.model.ports

import io.medatarun.model.model.*

/**
 * Model repository allow accessing and modifying stored models.
 *
 * This is the one extensions should implement to offer model storage capabilities.
 *
 * Do not confuse it with [ModelStorages] that acts as a composite around all [ModelRepository]
 *
 * [ModelStorages] contains multiple [ModelRepository]
 *
 */
interface ModelRepository {

    // Manage models -> entity def

    fun findAllModelIds(): List<ModelId>
    fun findModelByIdOptional(id: ModelId): Model?
    fun createModel(model: Model)
    fun deleteModel(modelId: ModelId)

    // Models -> EntityDef

    //@formatter:off
    fun createEntityDef(modelId: ModelId, e: EntityDef)
    fun updateEntityDefId(modelId: ModelId, entityDefId: EntityDefId, newEntityDefId: EntityDefId)
    fun updateEntityDefName(modelId: ModelId, entityDefId: EntityDefId, name: LocalizedText?)
    fun updateEntityDefDescription(modelId: ModelId, entityDefId: EntityDefId, description: LocalizedMarkdown?)
    fun deleteEntityDef(modelId: ModelId, entityDefId: EntityDefId)
    //@formatter:on

    // Model -> EntityDef -> AttributeDef

    //@formatter:off
    fun createEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attr: AttributeDef)
    fun updateEntityDefAttributeDefId(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, newAttributeDefId: AttributeDefId)
    fun updateEntityDefAttributeDefName(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, name: LocalizedText?)
    fun updateEntityDefAttributeDefDescription(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, description: LocalizedMarkdown?)
    fun updateEntityDefAttributeDefType(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, type: ModelTypeId)
    fun updateEntityDefAttributeDefOptional(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, optional: Boolean)
    fun deleteEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId)
    //@formatter:on
}