package io.medatarun.actions.ports.needs

import io.medatarun.actions.domain.ActionInstanceId

/**
 * Context given to action handlers (dispatch context) to be able to
 * get context and tooling to execute action.
 */
interface ActionCtx {
    /**
     * Unique action instance identifier.
     *
     * Not to be confused with actionId (which is the unique identifier of the action).
     *
     * This one is the identifier resulting of a "call" (understand as a request or invocation)
     */
    val actionInstanceId: ActionInstanceId
    val principal: ActionPrincipalCtx
    fun dispatchAction(req: ActionRequest): Any?
}
