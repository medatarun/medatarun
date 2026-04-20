package io.medatarun.model.domain

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.http.StatusCode
import io.medatarun.tags.core.domain.TagRef

// ----------------------------------------------------------------------------
// Not founds
// ----------------------------------------------------------------------------

class ModelNotFoundException(ref: ModelRef) :
    MedatarunException("Model [${ref.asString()}] was not found", StatusCode.NOT_FOUND)

class TypeNotFoundException(modelRef: ModelRef, typeRef: TypeRef) :
    MedatarunException("Type [${typeRef.asString()}] not found in model [${modelRef.asString()}]", StatusCode.NOT_FOUND)

class EntityNotFoundException(modelRef: ModelRef, entityRef: EntityRef) :
    MedatarunException(
        "Entity [${entityRef.asString()}] not found in model [${modelRef.asString()}]",
        StatusCode.NOT_FOUND
    )

class EntityAttributeNotFoundException(modelRef: ModelRef, entityRef: EntityRef, attributeRef: EntityAttributeRef) :
    MedatarunException(
        "Attribute [${attributeRef.asString()}] not found in entity [${entityRef.asString()}] and model [${modelRef.asString()}]",
        StatusCode.NOT_FOUND
    )

class RelationshipNotFoundException(modelRef: ModelRef, relationshipRef: RelationshipRef) :
    MedatarunException(
        "Relationship [${relationshipRef.asString()}] not found in model [${modelRef.asString()}]",
        StatusCode.NOT_FOUND
    )

class RelationshipRoleNotFoundException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef,
    roleRef: RelationshipRoleRef
) :
    MedatarunException(
        "Relationship role [${roleRef.asString()}] not found relationship [${relationshipRef.asString()}] and model [${modelRef.asString()}]",
        StatusCode.NOT_FOUND
    )

class RelationshipAttributeNotFoundException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef,
    attributeRef: RelationshipAttributeRef
) :
    MedatarunException(
        "Attribute [${attributeRef.asString()}] not found in relationship [${relationshipRef.asString()}] and model [${modelRef.asString()}]",
        StatusCode.NOT_FOUND
    )

class BusinessKeyNotFoundException(modelRef: ModelRef, businessKeyRef: BusinessKeyRef) :
    MedatarunException(
        "Business key [${businessKeyRef.asString()}] not found in model [${modelRef.asString()}]",
        StatusCode.NOT_FOUND
    )

// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------


class ModelNotFoundByKeyException(key: ModelKey) :
    MedatarunException("Model with key [${key.value}] was not found", StatusCode.NOT_FOUND)

class ModelNotFoundByIdException(id: ModelId) :
    MedatarunException("Model with id [${id.value}] was not found", StatusCode.NOT_FOUND)


class ModelDuplicateKeyException(key: ModelKey) :
    MedatarunException("Model with key [${key.value}] already exists", StatusCode.BAD_REQUEST)

class ModelReleaseVersionMustBeGreaterThanPreviousException(
    modelRef: ModelRef,
    version: ModelVersion,
    previousVersion: ModelVersion
) : MedatarunException(
    "Cannot release model [${modelRef.asString()}] with version [${version.asString()}] because it must be strictly greater than the previous released version [${previousVersion.asString()}].",
    StatusCode.BAD_REQUEST
)

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

class ModelEventConcurrentWriteException(
    modelId: ModelId,
    expectedRevision: Int,
    conflictingRevision: Int
) : MedatarunException(
    "Cannot append a model event to model [${modelId.value}] at expected revision [$expectedRevision] because revision [$conflictingRevision] was written concurrently.",
    StatusCode.CONFLICT
)


class LocalizedTextMapEmptyException :
    MedatarunException("When creating a LocalizedTextMap you must provide at least one language value or a 'default' key with a value")


class UpdateAttributeDuplicateKeyException(
    entityRef: EntityRef,
    attributeRef: EntityAttributeRef,
    newKey: AttributeKey
) :
    MedatarunException("Can not change attribute [${attributeRef.asString()}] key to [${newKey.value}] because it is already used for another attribute in entity [${entityRef.asString()}]")

class EntityUpdateKeyDuplicateKeyException(entityKey: EntityKey) :
    MedatarunException("Another entity with key [${entityKey.value}] already exists in the same model")

class CreateAttributeDuplicateKeyException(entityKey: EntityKey, attributeKey: AttributeKey) :
    MedatarunException("Another attribute with key [${attributeKey.value}] already exists with the same id in entity [${entityKey.value}]")

class ModelTypeDeleteUsedException(key: TypeKey) :
    MedatarunException(
        "Type with key [${key.value}] could not be deleted as it's used in entities",
        StatusCode.BAD_REQUEST
    )

class TypeCreateDuplicateException(modelKey: ModelKey, typeId: TypeKey) :
    MedatarunException("Type with id [${typeId.value}] already exists with the same id in model [${modelKey.value}]")

class TypeUpdateDuplicateKeyException(typeKey: TypeKey) :
    MedatarunException("Another type uses the key [${typeKey.value}].", StatusCode.BAD_REQUEST)


class ModelInvalidException(modelId: ModelId, errors: List<ModelValidationError>) :
    MedatarunException(
        "Model with id [${modelId.asString()}] could not be validated. " + errors.joinToString(". ") { it.message },
        StatusCode.UNPROCESSABLE_CONTENT
    )


class RelationshipDuplicateIdException(modelId: ModelId, relationshipKey: RelationshipKey) :
    MedatarunException("Another relationship in model [${modelId.value}] already has identifier [${relationshipKey.value}].")

class RelationshipDuplicateRoleIdException(roles: Collection<RelationshipRoleKey>) :
    MedatarunException("A relationship can not have the same role ids. Duplicate roles ids: [${roles.joinToString(", ")}]")

class RelationshipRoleCreateDuplicateKeyException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef,
    roleKey: RelationshipRoleKey
) :
    MedatarunException(
        "Cannot create relationship role in relationship [${relationshipRef.asString()}] of model [${modelRef.asString()}] because key [${roleKey.value}] already exists.",
        StatusCode.BAD_REQUEST
    )

class RelationshipRoleUpdateDuplicateKeyException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef,
    roleRef: RelationshipRoleRef,
    roleKey: RelationshipRoleKey
) : MedatarunException(
    "Cannot update relationship role [${roleRef.asString()}] in relationship [${relationshipRef.asString()}] of model [${modelRef.asString()}] to key [${roleKey.value}] because this key already exists.",
    StatusCode.BAD_REQUEST
)

class RelationshipRoleDeleteMinimumRolesException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef
) : MedatarunException(
    "Cannot delete a relationship role from relationship [${relationshipRef.asString()}] in model [${modelRef.asString()}] because a relationship must keep at least two roles.",
    StatusCode.BAD_REQUEST
)

class RelationshipAttributeCreateDuplicateKeyException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef,
    newKey: AttributeKey
) : MedatarunException(
    "Cannot attribute in relationship [${relationshipRef.asString()}] of model [${modelRef.asString()}] because the key [${newKey.value}] is already used by another attribute.",
    StatusCode.BAD_REQUEST
)

class RelationshipAttributeUpdateDuplicateKeyException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef,
    attributeRef: RelationshipAttributeRef,
    newKey: AttributeKey
) : MedatarunException(
    "Cannot change key of attribute [${attributeRef.asString()}] in relationship [${relationshipRef.asString()}] of model [${modelRef.asString()}] to value [${newKey.value}] because it is already used by another attribute.",
    StatusCode.BAD_REQUEST
)

class BusinessKeyCreateDuplicateKeyException(
    modelRef: ModelRef,
    key: BusinessKeyKey
) : MedatarunException(
    "Cannot create business key in model [${modelRef.asString()}] because key [${key.value}] already exists.",
    StatusCode.BAD_REQUEST
)

class BusinessKeyUpdateDuplicateKeyException(
    modelRef: ModelRef,
    businessKeyRef: BusinessKeyRef,
    key: BusinessKeyKey
) : MedatarunException(
    "Cannot update business key [${businessKeyRef.asString()}] in model [${modelRef.asString()}] to key [${key.value}] because this key already exists.",
    StatusCode.BAD_REQUEST
)

class ModelExportNoPluginFoundException : MedatarunException("No model exporters found in extensions")

class ModelQuerySearchCouldNotResolveTagRef(tagRef: TagRef) :
    MedatarunException("Could not resolve tag reference [${tagRef.asString()}")

class ModelActionNotAuthorizedException : MedatarunException("Not authorized", StatusCode.FORBIDDEN)
class ModelActionNotAuthenticatedException : MedatarunException("Not authenticated", StatusCode.UNAUTHORIZED)
class EntityDeleteInRelationshipException(val modelRef: ModelRef, val entityRef: EntityRef) :
    MedatarunException("You can not delete an entity used in relationship roles.", StatusCode.BAD_REQUEST)