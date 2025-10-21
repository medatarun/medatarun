package io.medatarun.model.model

interface ModelCmd {
    fun create(id: ModelId, name: LocalizedText, description: LocalizedMarkdown?, version: ModelVersion)
    fun createEntityDef(modelId: ModelId, entityDefId: EntityDefId, name: LocalizedText?, description: LocalizedMarkdown?)
    fun createEntityDefAttributeDef(modelId: ModelId, entityDefId: EntityDefId, attributeDefId: AttributeDefId, type: ModelTypeId, optional: Boolean, name: LocalizedText?, description: LocalizedMarkdown?)
    fun updateEntityName(modelId: ModelId, entityDefId: EntityDefId, newEntityId: EntityDefId)
    fun updateEntityTitle(modelId: ModelId, entityId: EntityDefId, title: LocalizedText?)
    fun updateEntityDescription(modelId: ModelId, entityId: EntityDefId, description: LocalizedMarkdown?)
    fun updateEntityAttributeName(modelId: ModelId, entityId: EntityDefId, attributeId: AttributeDefId, newAttributeId: AttributeDefId)
    fun updateEntityAttributeTitle(modelId: ModelId, entityId: EntityDefId, attributeId: AttributeDefId, title: LocalizedText?)
    fun updateEntityAttributeDescription(modelId: ModelId, entityId: EntityDefId, attributeId: AttributeDefId, description: LocalizedMarkdown?)
    fun updateEntityAttributeType(modelId: ModelId, entityId: EntityDefId, attributeId: AttributeDefId, type: ModelTypeId)
    fun updateEntityAttributeOptional(modelId: ModelId, entityId: EntityDefId, attributeId: AttributeDefId, optional: Boolean)
    fun delete(modelId: ModelId)
    fun deleteEntity(modelId: ModelId, entityId: EntityDefId)
    fun deleteEntityAttribute(modelId: ModelId, entityId: EntityDefId, attributeId: AttributeDefId)
}
