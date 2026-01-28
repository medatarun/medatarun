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
        model.entities.forEach { e ->
            e.attributes.forEach { attr ->
                if (model.findTypeOptional(attr.typeId) == null) {
                    errors.add(ModelValidationErrorTypeNotFound(model.key, e.key, attr.key, attr.typeId))
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

        model.entities.forEach { e ->
            if (e.attributes.none { it.id == e.identifierAttributeId }) {
                errors.add(ModelValidationErrorInvalidIdentityAttribute(model.key, e.key, e.identifierAttributeId))
            }
        }
        return errors
    }
}

