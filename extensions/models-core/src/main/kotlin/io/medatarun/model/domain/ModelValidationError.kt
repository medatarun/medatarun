package io.medatarun.model.domain

import io.medatarun.type.commons.key.Key

sealed class ModelValidationError(val message: String)

class ModelValidationErrorTypeNotFound(
    modelKey: ModelKey,
    entityKey: Key<*>,
    attributeId: AttributeKey,
    typeId: TypeId
) : ModelValidationError("Unknown type [${typeId.value}] for attribute [${modelKey.value}.${entityKey.value}.${attributeId.value}]")

class ModelValidationErrorInvalidIdentityAttribute(
    modelKey: ModelKey,
    entityKey: EntityKey,
    entityIdAttributeKey: AttributeId
) : ModelValidationError("Invalid identifier attribute [${entityIdAttributeKey.value}] for [${modelKey.value}.${entityKey.value}]")

class ModelValidationErrorDuplicateTypeKey(
    modelKey: ModelKey,
    typeKey: TypeKey
) : ModelValidationError("Duplicate type key [${typeKey.value}] in model [${modelKey.value}]")

class ModelValidationErrorDuplicateEntityKey(
    modelKey: ModelKey,
    entityKey: EntityKey
) : ModelValidationError("Duplicate entity key [${entityKey.value}] in model [${modelKey.value}]")

class ModelValidationErrorDuplicateEntityAttributeKey(
    modelKey: ModelKey,
    entityKey: EntityKey,
    attributeKey: AttributeKey
) : ModelValidationError("Duplicate attribute key [${attributeKey.value}] in entity [${modelKey.value}.${entityKey.value}]")

class ModelValidationErrorDuplicateRelationshipKey(
    modelKey: ModelKey,
    relationshipKey: RelationshipKey
) : ModelValidationError("Duplicate relationship key [${relationshipKey.value}] in model [${modelKey.value}]")

class ModelValidationErrorDuplicateRelationshipAttributeKey(
    modelKey: ModelKey,
    relationshipKey: RelationshipKey,
    attributeKey: AttributeKey
) : ModelValidationError("Duplicate attribute key [${attributeKey.value}] in relationship [${modelKey.value}.${relationshipKey.value}]")
