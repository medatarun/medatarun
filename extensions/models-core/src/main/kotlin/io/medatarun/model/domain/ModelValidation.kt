package io.medatarun.model.domain

interface ModelValidation {
    fun validate(model: ModelAggregate): ModelValidationState
}