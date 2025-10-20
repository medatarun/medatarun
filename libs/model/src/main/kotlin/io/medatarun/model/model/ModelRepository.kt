package io.medatarun.model.model

interface ModelRepository {
    fun findByIdOptional(id: ModelId): Model?
    fun create(model: Model)
    fun createEntity(modelId: ModelId, e: ModelEntity)
    fun findAllIds(): List<ModelId>
    fun createEntityAttribute(modelId: ModelId, entityId: ModelEntityId, attr: ModelAttribute)
    fun deleteEntityAttribute(modelId: ModelId, entityId: ModelEntityId, attributeId: ModelAttributeId)
}
