package io.medatarun.model.domain

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.lang.exceptions.MedatarunTechnicalException
import io.medatarun.lang.exceptions.MedatarunUserException
import io.medatarun.lang.http.StatusCode
import io.medatarun.tags.core.domain.TagRef

// ----------------------------------------------------------------------------
// Not founds
// ----------------------------------------------------------------------------

class ModelNotFoundException(ref: ModelRef) :
    MedatarunUserException("Model [${ref.asString()}] was not found", StatusCode.NOT_FOUND)

class TypeNotFoundException(modelRef: ModelRef, typeRef: TypeRef) :
    MedatarunUserException("Type [${typeRef.asString()}] not found in model [${modelRef.asString()}]", StatusCode.NOT_FOUND)

class EntityNotFoundException(modelRef: ModelRef, entityRef: EntityRef) :
    MedatarunUserException(
        "Entity [${entityRef.asString()}] not found in model [${modelRef.asString()}]",
        StatusCode.NOT_FOUND
    )

class EntityAttributeNotFoundException(modelRef: ModelRef, entityRef: EntityRef, attributeRef: EntityAttributeRef) :
    MedatarunUserException(
        "Attribute [${attributeRef.asString()}] not found in entity [${entityRef.asString()}] and model [${modelRef.asString()}]",
        StatusCode.NOT_FOUND
    )

class RelationshipNotFoundException(modelRef: ModelRef, relationshipRef: RelationshipRef) :
    MedatarunUserException(
        "Relationship [${relationshipRef.asString()}] not found in model [${modelRef.asString()}]",
        StatusCode.NOT_FOUND
    )

class RelationshipRoleNotFoundException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef,
    roleRef: RelationshipRoleRef
) :
    MedatarunUserException(
        "Relationship role [${roleRef.asString()}] not found relationship [${relationshipRef.asString()}] and model [${modelRef.asString()}]",
        StatusCode.NOT_FOUND
    )

class RelationshipAttributeNotFoundException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef,
    attributeRef: RelationshipAttributeRef
) :
    MedatarunUserException(
        "Attribute [${attributeRef.asString()}] not found in relationship [${relationshipRef.asString()}] and model [${modelRef.asString()}]",
        StatusCode.NOT_FOUND
    )

class BusinessKeyNotFoundException(modelRef: ModelRef, businessKeyRef: BusinessKeyRef) :
    MedatarunUserException(
        "Business key [${businessKeyRef.asString()}] not found in model [${modelRef.asString()}]",
        StatusCode.NOT_FOUND
    )

// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------


class ModelNotFoundByKeyException(key: ModelKey) :
    MedatarunUserException("Model with key [${key.value}] was not found", StatusCode.NOT_FOUND)

class ModelNotFoundByIdException(id: ModelId) :
    MedatarunUserException("Model with id [${id.value}] was not found", StatusCode.NOT_FOUND)


class ModelDuplicateKeyException(key: ModelKey) :
    MedatarunUserException("Model with key [${key.value}] already exists", StatusCode.BAD_REQUEST)

class ModelReleaseVersionMustBeGreaterThanPreviousException(
    modelRef: ModelRef,
    version: ModelVersion,
    previousVersion: ModelVersion
) : MedatarunUserException(
    "Cannot release model [${modelRef.asString()}] with version [${version.asString()}] because it must be strictly greater than the previous released version [${previousVersion.asString()}].",
    StatusCode.BAD_REQUEST
)

class ModelVersionEmptyException :
    MedatarunUserException("Model version can not be empty", StatusCode.BAD_REQUEST)

class ModelVersionInvalidFormatException :
    MedatarunUserException("Model version invalid.", StatusCode.BAD_REQUEST)

class ModelVersionCoreLeadingZeroException :
    MedatarunUserException("Model version numeric identifiers must not include leading zeros.", StatusCode.BAD_REQUEST)

class ModelVersionPreReleaseLeadingZeroException :
    MedatarunUserException(
        "Model version pre-release numeric identifiers must not include leading zeros.",
        StatusCode.BAD_REQUEST
    )

class ModelEventConcurrentWriteException(
    modelId: ModelId,
    expectedRevision: Int,
    conflictingRevision: Int
) : MedatarunTechnicalException(
    "Cannot append a model event to model [${modelId.value}] at expected revision [$expectedRevision] because revision [$conflictingRevision] was written concurrently.",
    StatusCode.CONFLICT
)


class UpdateAttributeDuplicateKeyException(
    entityRef: EntityRef,
    attributeRef: EntityAttributeRef,
    newKey: AttributeKey
) :
    MedatarunUserException(
        "Can not change attribute [${attributeRef.asString()}] key to [${newKey.value}] because it is already used for another attribute in entity [${entityRef.asString()}]",
        StatusCode.BAD_REQUEST
    )

class EntityUpdateKeyDuplicateKeyException(entityKey: EntityKey) :
    MedatarunUserException(
        "Another entity uses key [${entityKey.value}] in the model",
        StatusCode.BAD_REQUEST
    )

class CreateAttributeDuplicateKeyException(entityKey: EntityKey, attributeKey: AttributeKey) :
    MedatarunUserException(
        "Another attribute uses key [${attributeKey.value}] in entity [${entityKey.value}]",
        StatusCode.BAD_REQUEST
    )

class ModelTypeDeleteUsedException(key: TypeKey) :
    MedatarunUserException(
        "Type with key [${key.value}] could not be deleted as it's used in entities",
        StatusCode.BAD_REQUEST
    )

class TypeCreateDuplicateException(modelKey: ModelKey, typeId: TypeKey) :
    MedatarunUserException(
        "Another type uses key [${typeId.value}] in model [${modelKey.value}]",
        StatusCode.BAD_REQUEST
    )

class TypeUpdateDuplicateKeyException(typeKey: TypeKey) :
    MedatarunUserException("Another type uses the key [${typeKey.value}].", StatusCode.BAD_REQUEST)


class ModelInvalidException(modelId: ModelId, errors: List<ModelValidationError>) :
    MedatarunUserException(
        "Model [${modelId.asString()}] could not be validated. " + errors.joinToString(". ") { it.message },
        StatusCode.UNPROCESSABLE_CONTENT
    )


class RelationshipDuplicateIdException(modelId: ModelId, relationshipKey: RelationshipKey) :
    MedatarunUserException(
        "Another relationship in model [${modelId.value}] already uses key [${relationshipKey.value}].",
        StatusCode.BAD_REQUEST
    )

class RelationshipDuplicateRoleIdException(roles: Collection<RelationshipRoleKey>) :
    MedatarunUserException(
        "A relationship can not have the same role keys. Duplicate roles keys: [${
            roles.joinToString(
                ", "
            )
        }]", StatusCode.BAD_REQUEST
    )

class RelationshipRoleCreateDuplicateKeyException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef,
    roleKey: RelationshipRoleKey
) :
    MedatarunUserException(
        "Cannot create relationship role in relationship [${relationshipRef.asString()}] of model [${modelRef.asString()}] because key [${roleKey.value}] already exists.",
        StatusCode.BAD_REQUEST
    )

class RelationshipRoleUpdateDuplicateKeyException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef,
    roleRef: RelationshipRoleRef,
    roleKey: RelationshipRoleKey
) : MedatarunUserException(
    "Cannot update relationship role [${roleRef.asString()}] in relationship [${relationshipRef.asString()}] of model [${modelRef.asString()}] to key [${roleKey.value}] because this key already exists.",
    StatusCode.BAD_REQUEST
)

class RelationshipRoleDeleteMinimumRolesException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef
) : MedatarunUserException(
    "Cannot delete a relationship role from relationship [${relationshipRef.asString()}] in model [${modelRef.asString()}] because a relationship must keep at least two roles.",
    StatusCode.BAD_REQUEST
)

class RelationshipAttributeCreateDuplicateKeyException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef,
    newKey: AttributeKey
) : MedatarunUserException(
    "Cannot attribute in relationship [${relationshipRef.asString()}] of model [${modelRef.asString()}] because the key [${newKey.value}] is already used by another attribute.",
    StatusCode.BAD_REQUEST
)

class RelationshipAttributeUpdateDuplicateKeyException(
    modelRef: ModelRef,
    relationshipRef: RelationshipRef,
    attributeRef: RelationshipAttributeRef,
    newKey: AttributeKey
) : MedatarunUserException(
    "Cannot change key of attribute [${attributeRef.asString()}] in relationship [${relationshipRef.asString()}] of model [${modelRef.asString()}] to value [${newKey.value}] because it is already used by another attribute.",
    StatusCode.BAD_REQUEST
)

class BusinessKeyCreateDuplicateKeyException(
    modelRef: ModelRef,
    key: BusinessKeyKey
) : MedatarunUserException(
    "Cannot create business key in model [${modelRef.asString()}] because key [${key.value}] already exists.",
    StatusCode.BAD_REQUEST
)

class BusinessKeyUpdateDuplicateKeyException(
    modelRef: ModelRef,
    businessKeyRef: BusinessKeyRef,
    key: BusinessKeyKey
) : MedatarunUserException(
    "Cannot update business key [${businessKeyRef.asString()}] in model [${modelRef.asString()}] to key [${key.value}] because this key already exists.",
    StatusCode.BAD_REQUEST
)

class ModelExportNoPluginFoundException : MedatarunUserException("No model exporters found in extensions", StatusCode.NOT_FOUND)

class ModelQuerySearchCouldNotResolveTagRef(tagRef: TagRef) :
    MedatarunUserException("Could not resolve tag reference [${tagRef.asString()}", StatusCode.BAD_REQUEST)

class ModelActionNotAuthenticatedException : MedatarunUserException("Not authenticated", StatusCode.UNAUTHORIZED)
class EntityDeleteInRelationshipException(val modelRef: ModelRef, val entityRef: EntityRef) :
    MedatarunUserException("You can not delete an entity used in relationship roles.", StatusCode.BAD_REQUEST)