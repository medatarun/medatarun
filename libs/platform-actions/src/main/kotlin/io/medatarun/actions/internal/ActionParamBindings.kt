package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionInvocationValidationErrorException
import io.medatarun.actions.domain.ActionInvocationValidationMissingException
import io.medatarun.actions.domain.ActionInvocationValidationOverflowException
import kotlin.reflect.KParameter

internal data class ActionParamBindings(
    val paramStates: Map<KParameter, ActionParamBindingState>,
) {

    fun technicalValidation() {
        for (paramState in paramStates) {
            val param = paramState.key
            when (val state = paramState.value) {
                is ActionParamBindingState.Ok -> {

                }

                is ActionParamBindingState.Error -> {
                    throw ActionInvocationValidationErrorException(state)
                }

                is ActionParamBindingState.Missing -> {
                    throw ActionInvocationValidationMissingException(param.name ?: "unknown")
                }
            }
        }
    }

    fun toCallArgs(): Map<KParameter, Any?> {
        val callArgs = mutableMapOf<KParameter, Any?>()
        for (paramState in paramStates) {
            val param = paramState.key
            when (val state = paramState.value) {
                is ActionParamBindingState.Ok -> {
                    callArgs[param] = state.value
                }

                is ActionParamBindingState.Error, is ActionParamBindingState.Missing -> {
                    throw ActionInvocationValidationOverflowException(param)
                }
            }
        }
        return callArgs
    }
}