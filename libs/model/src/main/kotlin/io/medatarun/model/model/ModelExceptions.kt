package io.medatarun.model.model

open class MedatarunException(message: String) : Exception(message)

class ModelNotFoundException(id: ModelId) :
    MedatarunException("Model with id $id was not found")

class EntityDefNotFoundException(modelId: ModelId, entityId: EntityDefId) :
    MedatarunException("Entity with id $entityId not found in model $modelId")

class AttributeDefNotFoundException(entityId: EntityDefId, attributeId: AttributeDefId) :
    MedatarunException("Attribute with id $attributeId not found in entity $entityId")

class LocalizedTextMapEmptyException :
    MedatarunException("When creating a LocalizedTextMap you must provide at least one language value or a 'default' key with a value")

class UpdateAttributeDefDuplicateIdException(entityDefId: EntityDefId, attributeDefId: AttributeDefId) :
    MedatarunException("Another attribute $attributeDefId already exists with the same id in entity $entityDefId")

class UpdateEntityDefIdDuplicateIdException(entityDefId: EntityDefId) :
    MedatarunException("Another entity $entityDefId already exists in the same model")

class CreateAttributeDefDuplicateIdException(entityDefId: EntityDefId, attributeDefId: AttributeDefId) :
    MedatarunException("Another attribute $attributeDefId already exists with the same id in entity $entityDefId")

class ModelTypeDeleteUsedException(typeId: ModelTypeId) :
    MedatarunException("Model with id $typeId could not be deleted as it's used in entities")

class TypeCreateDuplicateException(modelId: ModelId, typeId: ModelTypeId) :
    MedatarunException("Type with id $typeId already exists with the same id in model $modelId")

class TypeNotFoundException(modelId: ModelId, typeId: ModelTypeId) :
    MedatarunException("Type with id $typeId not found in model $modelId")

class DeleteAttributeIdentifierException(modelId: ModelId, entityId: EntityDefId, attributeId: AttributeDefId) :
        MedatarunException("Can not delete attribute $attributeId in entity $entityId of model $modelId because it is used as the entity's identifier")