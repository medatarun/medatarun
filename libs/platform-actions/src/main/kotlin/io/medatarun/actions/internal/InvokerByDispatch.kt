package io.medatarun.actions.internal

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider

internal class InvokerByDispatch(
    private val actionProviderInstance: ActionProvider<Any>,
) : ActionRegistryImpl.Invoker {
    override fun invoke(cmd: Any, actionCtx: ActionCtx): Any? {
        return actionProviderInstance.dispatch(cmd, actionCtx)
    }
}