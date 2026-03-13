package io.medatarun.actions.domain

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionRequest

/**
 * Entry point to execute action on the platform
 */
interface ActionInvoker {
    /**
     * Executes this action
     */
    fun handleInvocation(invocation: ActionRequest, actionCtx: ActionCtx): Any?
}