package io.medatarun.actions.ports.needs

/**
 * Context to give to the main action invoker (entrance context)
 * to be able to invoke actions.
 *
 * The context of the action caller, so named "Request" context
 */
interface ActionRequestCtx {
    val principal: ActionPrincipalCtx
    val source: String
}
