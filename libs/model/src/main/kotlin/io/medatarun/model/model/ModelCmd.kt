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
    fun deleteEntityAttribute(
        modelId: ModelId,
        entityId: ModelEntityId,
        attributeId: ModelAttributeId
    )
}
