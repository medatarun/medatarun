package io.medatarun.model.domain

sealed class ModelValidationError(val message: String)

class ModelValidationErrorTypeNotFound(
    modelKey: ModelKey,
    entityKey: EntityKey,
    attributeId: AttributeKey,
    typeId: TypeId
) : ModelValidationError("Unknown type [${typeId.value}] for attribute [${modelKey.value}.${entityKey.value}.${attributeId.value}]")

class ModelValidationErrorInvalidIdentityAttribute(
    modelKey: ModelKey,
    entityKey: EntityKey,
    entityIdAttributeKey: AttributeKey
) : ModelValidationError("Invalid identifier attribute [${entityIdAttributeKey.value}] for [${modelKey.value}.${entityKey.value}]")