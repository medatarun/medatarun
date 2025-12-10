package io.medatarun.model.domain

sealed class ModelValidationError(val message: String)

class ModelValidationErrorTypeNotFound(
    modelId: ModelId,
    entityDefId: EntityDefId,
    attributeId: AttributeDefId,
    typeId: ModelTypeId
) : ModelValidationError("Unknown type [${typeId.value}] for attribute [${modelId.value}.${entityDefId.value}.${attributeId.value}]")

class ModelValidationErrorInvalidIdentityAttribute(
    modelId: ModelId,
    entityDefId: EntityDefId,
    entityIdAttributeDefId: AttributeDefId
) : ModelValidationError("Invalid identifier attribute [${entityIdAttributeDefId.value}] for [${modelId.value}.${entityDefId.value}]")