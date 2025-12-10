package io.medatarun.model.internal

import io.medatarun.model.domain.*

class ModelValidationImpl : ModelValidation {

    override fun validate(model: Model): ModelValidationState {
        val errors = ensureEachAttributeHasKnownType(model) + ensureIdentityAttributeExists(model)
        return if (errors.isEmpty()) ModelValidationState.Ok() else ModelValidationState.Error(errors)
    }

    /**
     * each attribute must have a known type
     */
    private fun ensureEachAttributeHasKnownType(model: Model): List<ModelValidationError> {
        val errors = mutableListOf<ModelValidationError>()
        // each attribute must have a known type
        model.entityDefs.forEach { e ->
            e.attributes.forEach { attr ->
                if (model.findTypeOptional(attr.type) == null) {
                    errors.add(ModelValidationErrorTypeNotFound(model.id, e.id, attr.id, attr.type))
                }
            }
        }
        return errors
    }

    /**
     * each entity identity attribute must exist
     */
    private fun ensureIdentityAttributeExists(model: Model): List<ModelValidationError> {
        val errors = mutableListOf<ModelValidationError>()

        model.entityDefs.forEach { e ->
            if (e.getAttributeDefOptional(e.identifierAttributeDefId) == null) {
                errors.add(ModelValidationErrorInvalidIdentityAttribute(model.id, e.id, e.entityIdAttributeDefId()))
            }
        }
        return errors
    }
}

