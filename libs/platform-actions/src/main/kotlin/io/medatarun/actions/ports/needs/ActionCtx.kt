package io.medatarun.actions.ports.needs

/**
 * Context given to action handlers (dispatch context) to be able to
 * get context and tooling to execute action.
 */
interface ActionCtx {
    fun dispatchAction(req: ActionRequest): Any?
    val principal: ActionPrincipalCtx
}
