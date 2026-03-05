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