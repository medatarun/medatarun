package io.medatarun.model.domain

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode

// ----------------------------------------------------------------------------
// Not founds
// ----------------------------------------------------------------------------

class ModelNotFoundException(ref: ModelRef) :
    MedatarunException("Model [${ref.asString()}] was not found", StatusCode.NOT_FOUND)

class TypeNotFoundException(modelRef: ModelRef, typeRef: TypeRef) :
    MedatarunException("Type [${typeRef.asString()}] not found in model [${modelRef.asString()}]", StatusCode.NOT_FOUND)

class EntityNotFoundException(modelRef: ModelRef, entityRef: EntityRef) :
    MedatarunException("Entity [${entityRef.asString()}] not found in model [${modelRef.asString()}]", StatusCode.NOT_FOUND)

class EntityAttributeNotFoundException(modelRef: ModelRef, entityRef: EntityRef, attributeRef: EntityAttributeRef) :
    MedatarunException("Attribute [${attributeRef.asString()}] not found in entity [${entityRef.asString()}] and model [${modelRef.asString()}]", StatusCode.NOT_FOUND)

class RelationshipNotFoundException(modelRef: ModelRef, relationshipRef: RelationshipRef) :
    MedatarunException("Relationship [${relationshipRef.asString()}] not found in model [${modelRef.asString()}]", StatusCode.NOT_FOUND)

class RelationshipRoleNotFoundException(modelRef: ModelRef, relationshipRef: RelationshipRef, roleRef: RelationshipRoleRef) :
    MedatarunException("Relationship role [${roleRef.asString()}] not found relationship [${relationshipRef.asString()}] and model [${modelRef.asString()}]", StatusCode.NOT_FOUND)

class RelationshipAttributeNotFoundException(modelRef: ModelRef, relationshipRef: RelationshipRef, attributeRef: RelationshipAttributeRef) :
    MedatarunException("Attribute [${attributeRef.asString()}] not found in relationship [${relationshipRef.asString()}] and model [${modelRef.asString()}]", StatusCode.NOT_FOUND)

// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------


class ModelNotFoundByKeyException(key: ModelKey) :
    MedatarunException("Model with key [${key.value}] was not found", StatusCode.NOT_FOUND)

class ModelNotFoundByIdException(id: ModelId) :
    MedatarunException("Model with id [${id.value}] was not found", StatusCode.NOT_FOUND)


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




class LocalizedTextMapEmptyException :
    MedatarunException("When creating a LocalizedTextMap you must provide at least one language value or a 'default' key with a value")

class UpdateAttributeDuplicateKeyException(entityRef: EntityRef, attributeRef: EntityAttributeRef, newKey: AttributeKey) :
    MedatarunException("Can not change attribute [${attributeRef.asString()}] key to [${newKey.value}] because it is already used for another attribute in entity [${entityRef.asString()}]")

class UpdateEntityDefIdDuplicateIdException(entityKey: EntityKey) :
    MedatarunException("Another entity [${entityKey.value}] already exists in the same model")

class CreateAttributeDuplicateIdException(entityKey: EntityKey, attributeKey: AttributeKey) :
    MedatarunException("Another attribute [${attributeKey.value}] already exists with the same id in entity [${entityKey.value}]")

class ModelTypeDeleteUsedException(typeId: TypeKey) :
    MedatarunException("Model with id [${typeId.value}] could not be deleted as it's used in entities")

class TypeCreateDuplicateException(modelKey: ModelKey, typeId: TypeKey) :
    MedatarunException("Type with id [${typeId.value}] already exists with the same id in model [${modelKey.value}]")


class TypeNotFoundByKeyException(modelKey: ModelKey, typeKey: TypeKey) :
    MedatarunException("Type with id [${typeKey.value}] not found in model [${modelKey.value}]")

class DeleteAttributeIdentifierException(modelId: ModelRef, entityId: EntityRef, attributeRef: EntityAttributeRef) :
    MedatarunException("Can not delete attribute [${attributeRef.asString()}] in entity [${entityId.asString()}] of model [${modelId.asString()}] because it is used as the entity's identifier")

class ModelInvalidException(modelId: ModelId, errors: List<ModelValidationError>) :
    MedatarunException("Model with id [${modelId.asString()}] could not be validated. " + errors.joinToString(". ") { it.message })


class RelationshipDuplicateIdException(modelId: ModelId, relationshipKey: RelationshipKey) :
    MedatarunException("Another relationship in model [${modelId.value}] already has identifier [${relationshipKey.value}].")

class RelationshipDuplicateRoleIdException(roles: Collection<RelationshipRoleKey>) :
    MedatarunException("A relationship can not have the same role ids. Duplicate roles ids: [${roles.joinToString(", ")}]")

class RelationshipAttributeCreateDuplicateKeyException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef,
    newKey: AttributeKey
) :
    MedatarunException("Cannot attribute in relationship [${relationshipRef.asString()}] of model [${modelRef.asString()}] because the key [${newKey.value}] is already used by another attribute.")

class RelationshipAttributeUpdateDuplicateKeyException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef,
    attributeRef: RelationshipAttributeRef,
    newKey: AttributeKey
) :
    MedatarunException("Cannot change key of attribute [${attributeRef.asString()}] in relationship [${relationshipRef.asString()}] of model [${modelRef.asString()}] to value [${newKey.value}] because it is already used by another attribute.")

class KeyInvalidFormatException :
    MedatarunException("Invalid key format", StatusCode.BAD_REQUEST)

class KeyEmptyException :
    MedatarunException("Invalid key format, a key can not be empty", StatusCode.BAD_REQUEST)

class KeyTooLongException(maxsize: Int) :
    MedatarunException("Key size can not exceed $maxsize characters", StatusCode.BAD_REQUEST)

class ModelExportNoPluginFoundException(): MedatarunException("No model exporters found in extensions")
class CopyModelIdConversionFailedException(name: String, oldId: String):
        MedatarunException("While copying model, could not get new $name identifier for old id $oldId")