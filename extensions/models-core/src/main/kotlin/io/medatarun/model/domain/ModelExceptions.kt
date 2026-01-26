package io.medatarun.model.domain

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode

class ModelNotFoundByKeyException(key: ModelKey) :
    MedatarunException("Model with key [${key.value}] was not found", StatusCode.NOT_FOUND)

class ModelNotFoundByIdException(id: ModelId) :
    MedatarunException("Model with id [${id.value}] was not found", StatusCode.NOT_FOUND)

class ModelNotFoundException(ref: ModelRef) :
    MedatarunException("Model with ref [${ref.asString()}] was not found", StatusCode.NOT_FOUND)

class ModelDuplicateIdException(id: ModelKey) :
    MedatarunException("Model with id [${id.value}] already exists")

class ModelVersionEmptyException :
    MedatarunException("Model version can not be empty", StatusCode.BAD_REQUEST)

class ModelVersionInvalidFormatException :
    MedatarunException("Model version invalid.", StatusCode.BAD_REQUEST)

class ModelVersionCoreLeadingZeroException :
    MedatarunException("Model version numeric identifiers must not include leading zeros.", StatusCode.BAD_REQUEST)

class ModelVersionPreReleaseLeadingZeroException :
    MedatarunException(
        "Model version pre-release numeric identifiers must not include leading zeros.",
        StatusCode.BAD_REQUEST
    )

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

class TypeNotFoundException(modelRef: ModelRef, typeRef: TypeRef) :
    MedatarunException("Type with id [${typeRef.asString()}] not found in model [${modelRef.asString()}]", StatusCode.NOT_FOUND)

class TypeNotFoundByIdException(modelKey: ModelKey, typeId: TypeId) :
    MedatarunException("Type with id [${typeId.value}] not found in model [${modelKey.value}]")

class TypeNotFoundByKeyException(modelKey: ModelKey, typeKey: TypeKey) :
    MedatarunException("Type with id [${typeKey.value}] not found in model [${modelKey.value}]")

class DeleteAttributeIdentifierException(modelId: ModelId, entityId: EntityKey, attributeId: AttributeKey) :
    MedatarunException("Can not delete attribute $attributeId in entity [${entityId.value}] of model [${modelId.value}] because it is used as the entity's identifier")

class ModelInvalidException(modelId: ModelId, errors: List<ModelValidationError>) :
    MedatarunException("Model with id [${modelId.asString()}] could not be validated. " + errors.joinToString(". ") { it.message })

class RelationshipDefNotFoundException(modelKey: ModelKey, relationshipKey: RelationshipKey) :
    MedatarunException("Relationship with id [${relationshipKey.value}] not found in model [${modelKey.value}]")

class RelationshipDuplicateIdException(modelId: ModelId, relationshipKey: RelationshipKey) :
    MedatarunException("Another relationship in model [${modelId.value}] already has identifier [${relationshipKey.value}].")

class RelationshipDuplicateRoleIdException(roles: Collection<RelationshipRoleKey>) :
    MedatarunException("A relationship can not have the same role ids. Duplicate roles ids: [${roles.joinToString(", ")}]")

class RelationshipDuplicateAttributeException(
    modelId: ModelId,
    relationshipKey: RelationshipKey,
    attributeId: AttributeKey
) :
    MedatarunException("In model [${modelId.asString()}], relationship [${relationshipKey.value}] already has another attribute with id [${attributeId.value}]")

class KeyInvalidFormatException :
    MedatarunException("Invalid key format", StatusCode.BAD_REQUEST)

class KeyEmptyException :
    MedatarunException("Invalid key format, a key can not be empty", StatusCode.BAD_REQUEST)

class KeyTooLongException(maxsize: Int) :
    MedatarunException("Key size can not exceed $maxsize characters", StatusCode.BAD_REQUEST)

class ModelExportNoPluginFoundException(): MedatarunException("No model exporters found in extensions")