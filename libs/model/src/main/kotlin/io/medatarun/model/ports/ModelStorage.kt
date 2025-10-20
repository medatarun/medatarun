package io.medatarun.model.ports

import io.medatarun.model.model.LocalizedMarkdown
import io.medatarun.model.model.LocalizedText
import io.medatarun.model.model.Model
import io.medatarun.model.model.ModelAttribute
import io.medatarun.model.model.ModelAttributeId
import io.medatarun.model.model.ModelEntity
import io.medatarun.model.model.ModelEntityId
import io.medatarun.model.model.ModelId

interface ModelStorage {
    fun findById(id: ModelId): Model
    fun create(mode: Model)
    fun delete(modelId: ModelId)
    fun findAllIds(): List<ModelId>
    fun createEntity(modelId: ModelId, e: ModelEntity)
    fun updateEntityName(modelId: ModelId, entityId: ModelEntityId, newEntityId: ModelEntityId)
    fun updateEntityTitle(modelId: ModelId, entityId: ModelEntityId, title: LocalizedText?)
    fun updateEntityDescription(modelId: ModelId, entityId: ModelEntityId, description: LocalizedMarkdown?)
    fun deleteEntity(modelId: ModelId, entityId: ModelEntityId)
    fun createEntityAttribute(modelId: ModelId, entityId: ModelEntityId, attr: ModelAttribute)
    fun updateEntityAttributeName(modelId: ModelId, entityId: ModelEntityId, attributeId: ModelAttributeId, newAttributeId: ModelAttributeId)
    fun updateEntityAttributeTitle(modelId: ModelId, entityId: ModelEntityId, attributeId: ModelAttributeId, title: LocalizedText?)
    fun updateEntityAttributeDescription(modelId: ModelId, entityId: ModelEntityId, attributeId: ModelAttributeId, description: LocalizedMarkdown?)
    fun deleteEntityAttribute(modelId: ModelId, entityId: ModelEntityId, attributeId: ModelAttributeId)
}
