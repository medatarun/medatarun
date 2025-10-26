package io.medatarun.model.model

sealed interface ModelValidationState {
    class Ok: ModelValidationState
    class Error(val errors: List<ModelValidationError>): ModelValidationState
}