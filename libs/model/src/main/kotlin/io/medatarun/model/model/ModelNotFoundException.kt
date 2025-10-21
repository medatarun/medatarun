package io.medatarun.model.model

open class MedatarunException(message: String) : Exception(message)
class ModelNotFoundException(id: ModelId) : MedatarunException("Model with id $id was not found")
class ModelEntityNotFoundException(modelId: ModelId, entityId: EntityDefId) : MedatarunException("Entity with id $entityId not found in model $modelId")
class ModelEntityAttributeNotFoundException(entityId: EntityDefId, attributeId: AttributeDefId) : MedatarunException("Attribute with id $attributeId not found in entity $entityId")
class ModelEntityAttributeUnknownException(entityId: EntityDefId, attributeId: AttributeDefId) : MedatarunException("Unknown attribute $attributeId for entity $entityId")
class EntityDefNotInModelException(modelId:ModelId, entityDefId: EntityDefId) : MedatarunException("Entity definition with id $entityDefId does not exist in model $modelId")