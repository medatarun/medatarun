package io.medatarun.model.model

interface ModelRepository {
    fun findByIdOptional(id: ModelId): Model?
    fun create(model: Model)
    fun delete(modelId: ModelId)
    fun createEntity(modelId: ModelId, e: ModelEntity)
    fun updateEntityName(modelId: ModelId, entityId: ModelEntityId, newEntityId: ModelEntityId)
    fun updateEntityTitle(modelId: ModelId, entityId: ModelEntityId, title: LocalizedText?)
    fun updateEntityDescription(modelId: ModelId, entityId: ModelEntityId, description: LocalizedMarkdown?)
    fun deleteEntity(modelId: ModelId, entityId: ModelEntityId)
    fun findAllIds(): List<ModelId>
    fun createEntityAttribute(modelId: ModelId, entityId: ModelEntityId, attr: ModelAttribute)
    fun updateEntityAttributeName(modelId: ModelId, entityId: ModelEntityId, attributeId: ModelAttributeId, newAttributeId: ModelAttributeId)
    fun updateEntityAttributeTitle(modelId: ModelId, entityId: ModelEntityId, attributeId: ModelAttributeId, title: LocalizedText?)
    fun updateEntityAttributeDescription(modelId: ModelId, entityId: ModelEntityId, attributeId: ModelAttributeId, description: LocalizedMarkdown?)
    fun deleteEntityAttribute(modelId: ModelId, entityId: ModelEntityId, attributeId: ModelAttributeId)
}
