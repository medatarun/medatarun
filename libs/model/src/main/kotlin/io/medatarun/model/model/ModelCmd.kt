package io.medatarun.model.model

/**
 * Commands to change the model, entity definitions, entity definition's attributes definitions
 */
interface ModelCmd {

    // Model

    fun createModel(id: ModelId, name: LocalizedText, description: LocalizedMarkdown?, version: ModelVersion)
    fun deleteModel(modelId: ModelId)

    // Model -> EntityDef

    fun createEntityDef(modelId: ModelId, entityDefId: EntityDefId, name: LocalizedText?, description: LocalizedMarkdown?)
    fun updateEntityDefId(modelId: ModelId, entityDefId: EntityDefId, newEntityDefId: EntityDefId)
    fun updateEntityDefName(modelId: ModelId, entityDefId: EntityDefId, name: LocalizedText?)
    fun updateEntityDefDescription(modelId: ModelId, entityDefId: EntityDefId, description: LocalizedMarkdown?)
    fun deleteEntityDef(modelId: ModelId, entityDefId: EntityDefId)

    // Model -> EntityDef -> AttributeDef

    fun createEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, type: ModelTypeId, optional: Boolean, name: LocalizedText?, description: LocalizedMarkdown?)
    fun updateEntityDefAttributeDefId(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, newAttributeDefId: AttributeDefId)
    fun updateEntityDefAttributeDefName(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, name: LocalizedText?)
    fun updateEntityDefAttributeDefDescription(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, description: LocalizedMarkdown?)
    fun updateEntityDefAttributeDefType(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, type: ModelTypeId)
    fun updateEntityDefAttributeDefOptional(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, optional: Boolean)
    fun deleteEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId)
}
