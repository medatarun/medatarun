package io.medatarun.model.internal

import io.medatarun.model.domain.*

class ModelValidationImpl : ModelValidation {

    override fun validate(model: ModelAggregate): ModelValidationState {
        val errors = ensureEachAttributeHasKnownType(model) + ensureIdentityAttributeExists(model)
        return if (errors.isEmpty()) ModelValidationState.Ok() else ModelValidationState.Error(errors)
    }

    /**
     * each attribute must have a known type
     */
    private fun ensureEachAttributeHasKnownType(model: ModelAggregate): List<ModelValidationError> {
        val errors = mutableListOf<ModelValidationError>()
        // each attribute must have a known type
        model.attributes.forEach { attr ->
            if (model.findTypeOptional(attr.typeId) == null) {
                val key = when(val ownerId = attr.ownerId) {
                    is AttributeOwnerId.OwnerEntityId -> model.findEntity(ownerId.id).key
                    is AttributeOwnerId.OwnerRelationshipId -> model.findRelationship(ownerId.id).key
                }
                errors.add(ModelValidationErrorTypeNotFound(model.key, key, attr.key, attr.typeId))
            }
        }
        return errors
    }

    /**
     * each entity identity attribute must exist
     */
    private fun ensureIdentityAttributeExists(model: ModelAggregate): List<ModelValidationError> {
        val errors = mutableListOf<ModelValidationError>()
        model.entities.forEach { e ->
            if (model.findEntityAttributeOptional(e.ref, e.identifierAttributeId) == null) {
                errors.add(ModelValidationErrorInvalidIdentityAttribute(model.key, e.key, e.identifierAttributeId))
            }
        }
        return errors
    }
}

