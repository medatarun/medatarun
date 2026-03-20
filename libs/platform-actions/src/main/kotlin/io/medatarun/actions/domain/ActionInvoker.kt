package io.medatarun.actions.domain

import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.actions.ports.needs.ActionRequestCtx

/**
 * Entry point to execute action on the platform
 */
interface ActionInvoker {
    /**
     * Executes this action
     */
    fun handleInvocation(invocation: ActionRequest, actionRequestCtx: ActionRequestCtx): Any?

    /**
     * Returns true if in the current action request context, the user is authorized to launch action
     */
    fun evaluateSecurity(actionGroupKey: String, actionKey: String, actionRequestCtx: ActionRequestCtx): Boolean
}