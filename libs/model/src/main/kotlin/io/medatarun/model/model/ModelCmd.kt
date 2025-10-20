package io.medatarun.model.model

interface ModelCmd {
    fun create(id: ModelId, name: LocalizedText, description: LocalizedMarkdown?, version: ModelVersion)
    fun createEntity(modelId: ModelId, entityId: ModelEntityId, name: LocalizedText?, description: LocalizedMarkdown?)
    fun createEntityAttribute(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId,
        type: ModelTypeId,
        optional: Boolean,
        name: LocalizedText?,
        description: LocalizedMarkdown?
    )
    fun updateEntityName(modelId: ModelId, entityId: ModelEntityId, newEntityId: ModelEntityId)
    fun updateEntityTitle(modelId: ModelId, entityId: ModelEntityId, title: LocalizedText?)
    fun updateEntityDescription(modelId: ModelId, entityId: ModelEntityId, description: LocalizedMarkdown?)
    fun updateEntityAttributeName(modelId: ModelId, entityId: ModelEntityId, attributeId: ModelAttributeId, newAttributeId: ModelAttributeId)
    fun updateEntityAttributeTitle(modelId: ModelId, entityId: ModelEntityId, attributeId: ModelAttributeId, title: LocalizedText?)
    fun updateEntityAttributeDescription(modelId: ModelId, entityId: ModelEntityId, attributeId: ModelAttributeId, description: LocalizedMarkdown?)
    fun delete(modelId: ModelId)
    fun deleteEntity(modelId: ModelId, entityId: ModelEntityId)
    fun deleteEntityAttribute(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId
    )
}
