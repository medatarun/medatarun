package io.medatarun.actions.runtime

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.actions.ports.needs.AppPrincipal
import io.medatarun.auth.adapters.ActionPrincipalCtxAdapter
import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.kernel.MedatarunServiceRegistry
import io.medatarun.runtime.AppRuntime
import kotlin.reflect.KClass

class ActionCtxFactory(
    val runtime: AppRuntime,
    val actionRegistry: ActionRegistry,
    val services: MedatarunServiceRegistry
) {
    fun create(principal: AppPrincipal?): ActionCtx {
        return object : ActionCtx {
            override val extensionRegistry: ExtensionRegistry = runtime.extensionRegistry
            override fun dispatchAction(req: ActionRequest): Any? {
                return actionRegistry.handleInvocation(req, this)
            }
            override fun <T : Any> getService(type: KClass<T>): T = services.getService(type)
            override val principal: ActionPrincipalCtx = ActionPrincipalCtxAdapter.toActionPrincipalCtx(principal)
        }
    }
}