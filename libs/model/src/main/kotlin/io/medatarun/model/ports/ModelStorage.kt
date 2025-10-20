package io.medatarun.model.ports

import io.medatarun.model.infra.ModelAttributeInMemory
import io.medatarun.model.infra.ModelEntityInMemory
import io.medatarun.model.model.Model
import io.medatarun.model.model.ModelAttribute
import io.medatarun.model.model.ModelAttributeId
import io.medatarun.model.model.ModelEntity
import io.medatarun.model.model.ModelEntityId
import io.medatarun.model.model.ModelId

interface ModelStorage {
    fun findById(id: ModelId): Model
    fun create(mode:Model)
    fun findAllIds(): List<ModelId>
    fun createEntity(modelId: ModelId, e: ModelEntity)
    fun deleteEntity(modelId: ModelId, entityId: ModelEntityId)
    fun createEntityAttribute(modelId: ModelId, entityId: ModelEntityId, attr: ModelAttribute)
    fun deleteEntityAttribute(modelId: ModelId, entityId: ModelEntityId, attributeId: ModelAttributeId)
}
