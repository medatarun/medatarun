package io.medatarun.actions.ports.needs

interface ActionCtx {
    fun dispatchAction(req: ActionRequest): Any?
    val principal: ActionPrincipalCtx
}
