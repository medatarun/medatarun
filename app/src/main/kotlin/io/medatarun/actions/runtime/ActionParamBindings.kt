package io.medatarun.actions.runtime

import io.ktor.http.*
import kotlin.reflect.KParameter

data class ActionParamBindings(
    val paramStates: Map<KParameter, ActionParamBindingState>,
) {

    fun technicalValidation() {
        for (paramState in paramStates) {
            val param = paramState.key
            val state = paramState.value
            when (state) {
                is ActionParamBindingState.Ok -> {

                }
                is ActionParamBindingState.Error -> {
                    throw ActionInvocationException(state.statusCode, state.message, mapOf("details" to state.message))
                }

                is ActionParamBindingState.Missing -> {
                    throw ActionInvocationException(
                        state.statusCode,
                        "Parameter [${param.name}] is missing", mapOf("details" to "Missing parameter [${param.name}]")
                    )
                }
            }
        }
    }

    fun toCallArgs(): Map<KParameter, Any?> {
        val callArgs = mutableMapOf<KParameter, Any?>()
        for (paramState in paramStates) {
            val param = paramState.key
            val state = paramState.value
            when (state) {
                is ActionParamBindingState.Ok -> {
                    callArgs[param] = state.value
                }

                is ActionParamBindingState.Error, is ActionParamBindingState.Missing -> {
                    throw ActionInvocationException(
                        HttpStatusCode.Companion.InternalServerError,
                        "Parameter [${param.name}] is invalid. Error should have been thrown before."
                    )
                }
            }
        }
        return callArgs
    }
}