package io.medatarun.model.model

open class MedatarunException(message: String) : Exception(message)

class ModelNotFoundException(id: ModelId) :
    MedatarunException("Model with id [${id.value}] was not found")

class EntityDefNotFoundException(modelId: ModelId, entityId: EntityDefId) :
    MedatarunException("Entity with id [${entityId.value}] not found in model [${modelId.value}]")

class EntityAttributeDefNotFoundException(entityId: EntityDefId, attributeId: AttributeDefId) :
    MedatarunException("Attribute with id [${attributeId.value}] not found in entity [${entityId.value}]")

class RelationshipAttributeDefNotFoundException(relationshipDefId: RelationshipDefId, attributeId: AttributeDefId) :
    MedatarunException("Attribute with id [${attributeId.value}] not found in relationship [${relationshipDefId.value}]")

class LocalizedTextMapEmptyException :
    MedatarunException("When creating a LocalizedTextMap you must provide at least one language value or a 'default' key with a value")

class UpdateAttributeDefDuplicateIdException(entityDefId: EntityDefId, attributeDefId: AttributeDefId) :
    MedatarunException("Another attribute [${attributeDefId.value}] already exists with the same id in entity [${entityDefId.value}]")

class UpdateEntityDefIdDuplicateIdException(entityDefId: EntityDefId) :
    MedatarunException("Another entity [${entityDefId.value}] already exists in the same model")

class CreateAttributeDefDuplicateIdException(entityDefId: EntityDefId, attributeDefId: AttributeDefId) :
    MedatarunException("Another attribute [${attributeDefId.value}] already exists with the same id in entity [${entityDefId.value}]")

class ModelTypeDeleteUsedException(typeId: ModelTypeId) :
    MedatarunException("Model with id [${typeId.value}] could not be deleted as it's used in entities")

class TypeCreateDuplicateException(modelId: ModelId, typeId: ModelTypeId) :
    MedatarunException("Type with id [${typeId.value}] already exists with the same id in model [${modelId.value}]")

class TypeNotFoundException(modelId: ModelId, typeId: ModelTypeId) :
    MedatarunException("Type with id [${typeId.value}] not found in model [${modelId.value}]")

class DeleteAttributeIdentifierException(modelId: ModelId, entityId: EntityDefId, attributeId: AttributeDefId) :
        MedatarunException("Can not delete attribute $attributeId in entity [${entityId.value}] of model [${modelId.value}] because it is used as the entity's identifier")

class ModelInvalidException(modelId: ModelId, errors: List<ModelValidationError>) :
        MedatarunException("Model with id [${modelId.value}] could not be validated. " + errors.joinToString(". ") { it.message })

class RelationshipDefNotFoundException(modelId: ModelId, relationshipDefId: RelationshipDefId) :
        MedatarunException("Relationship with id [${relationshipDefId.value}] not found in model [${modelId.value}]")

class RelationshipDuplicateIdException(modelId: ModelId, relationshipDefId: RelationshipDefId) :
    MedatarunException("Another relationship in model [${modelId.value}] already has identifier [${relationshipDefId.value}].")

class RelationshipDuplicateRoleIdException(roles: Collection<RelationshipRoleId>) :
    MedatarunException("A relationship can not have the same role ids. Duplicate roles ids: [${roles.joinToString(", ")}]")

class RelationshipDuplicateAttributeException(modelId: ModelId, relationshipDefId: RelationshipDefId, attributeId: AttributeDefId) :
    MedatarunException("In model [${modelId.value}], relationship [${relationshipDefId.value}] already has another attribute with id [${attributeId.value}]")