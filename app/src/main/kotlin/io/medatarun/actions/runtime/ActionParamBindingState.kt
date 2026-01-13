package io.medatarun.actions.runtime

import io.ktor.http.*

sealed interface ActionParamBindingState {
    class Ok(val value: Any?) : ActionParamBindingState
    class Error(val statusCode: HttpStatusCode, val message: String) : ActionParamBindingState
    class Missing(val statusCode: HttpStatusCode) : ActionParamBindingState
}