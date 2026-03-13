package io.medatarun.actions.internal

import io.medatarun.lang.http.StatusCode

internal sealed interface ActionParamBindingState {
    class Ok(val value: Any?) : ActionParamBindingState
    class Error(val statusCode: StatusCode, val message: String) : ActionParamBindingState
    class Missing(val statusCode: StatusCode) : ActionParamBindingState
}