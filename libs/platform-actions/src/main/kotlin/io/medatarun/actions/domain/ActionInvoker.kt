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
}