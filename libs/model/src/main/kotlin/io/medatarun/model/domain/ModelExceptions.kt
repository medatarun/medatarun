package io.medatarun.model.domain

open class MedatarunException(message: String) : Exception(message)

class ModelNotFoundException(id: ModelKey) :
    MedatarunException("Model with id [${id.value}] was not found")
class ModelDuplicateIdException(id: ModelKey) :
        MedatarunException("Model with id [${id.value}] already exists")

class EntityDefNotFoundException(modelKey: ModelKey, entityId: EntityKey) :
    MedatarunException("Entity with id [${entityId.value}] not found in model [${modelKey.value}]")

class EntityAttributeDefNotFoundException(entityId: EntityKey, attributeId: AttributeKey) :
    MedatarunException("Attribute with id [${attributeId.value}] not found in entity [${entityId.value}]")

class RelationshipAttributeDefNotFoundException(relationshipKey: RelationshipKey, attributeId: AttributeKey) :
    MedatarunException("Attribute with id [${attributeId.value}] not found in relationship [${relationshipKey.value}]")

class LocalizedTextMapEmptyException :
    MedatarunException("When creating a LocalizedTextMap you must provide at least one language value or a 'default' key with a value")

class UpdateAttributeDefDuplicateIdException(entityKey: EntityKey, attributeKey: AttributeKey) :
    MedatarunException("Another attribute [${attributeKey.value}] already exists with the same id in entity [${entityKey.value}]")

class UpdateEntityDefIdDuplicateIdException(entityKey: EntityKey) :
    MedatarunException("Another entity [${entityKey.value}] already exists in the same model")

class CreateAttributeDefDuplicateIdException(entityKey: EntityKey, attributeKey: AttributeKey) :
    MedatarunException("Another attribute [${attributeKey.value}] already exists with the same id in entity [${entityKey.value}]")

class ModelTypeDeleteUsedException(typeId: TypeKey) :
    MedatarunException("Model with id [${typeId.value}] could not be deleted as it's used in entities")

class TypeCreateDuplicateException(modelKey: ModelKey, typeId: TypeKey) :
    MedatarunException("Type with id [${typeId.value}] already exists with the same id in model [${modelKey.value}]")

class TypeNotFoundException(modelKey: ModelKey, typeId: TypeKey) :
    MedatarunException("Type with id [${typeId.value}] not found in model [${modelKey.value}]")

class DeleteAttributeIdentifierException(modelKey: ModelKey, entityId: EntityKey, attributeId: AttributeKey) :
        MedatarunException("Can not delete attribute $attributeId in entity [${entityId.value}] of model [${modelKey.value}] because it is used as the entity's identifier")

class ModelInvalidException(modelKey: ModelKey, errors: List<ModelValidationError>) :
        MedatarunException("Model with id [${modelKey.value}] could not be validated. " + errors.joinToString(". ") { it.message })

class RelationshipDefNotFoundException(modelKey: ModelKey, relationshipKey: RelationshipKey) :
        MedatarunException("Relationship with id [${relationshipKey.value}] not found in model [${modelKey.value}]")

class RelationshipDuplicateIdException(modelKey: ModelKey, relationshipKey: RelationshipKey) :
    MedatarunException("Another relationship in model [${modelKey.value}] already has identifier [${relationshipKey.value}].")

class RelationshipDuplicateRoleIdException(roles: Collection<RelationshipRoleId>) :
    MedatarunException("A relationship can not have the same role ids. Duplicate roles ids: [${roles.joinToString(", ")}]")

class RelationshipDuplicateAttributeException(modelKey: ModelKey, relationshipKey: RelationshipKey, attributeId: AttributeKey) :
    MedatarunException("In model [${modelKey.value}], relationship [${relationshipKey.value}] already has another attribute with id [${attributeId.value}]")