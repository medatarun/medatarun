package io.medatarun.model.model

interface ModelValidation {
    fun validate(model:Model): ModelValidationState
}