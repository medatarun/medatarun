package io.medatarun.model.model

open class MedatarunException(message: String) : Exception(message)
class ModelNotFoundException(id: ModelId) : MedatarunException("Model with id $id was not found")
class ModelEntityNotFoundException(modelId: ModelId, entityId: ModelEntityId) : MedatarunException("Entity with id $entityId not found in model $modelId")
class ModelEntityAttributeNotFoundException(entityId: ModelEntityId, attributeId: ModelAttributeId) : MedatarunException("Attribute with id $attributeId not found in entity $entityId")
