package io.medatarun.model.domain

sealed interface ModelValidationState {
    class Ok: ModelValidationState
    class Error(val errors: List<ModelValidationError>): ModelValidationState
}