package io.medatarun.actions.runtime

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.auth.actions.ActionPrincipalCtxAdapter
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.MedatarunServiceRegistry
import io.medatarun.runtime.AppRuntime
import io.medatarun.security.AppPrincipal
import kotlin.reflect.KClass

class ActionCtxFactory(
    val runtime: AppRuntime,
    val actionInvoker: ActionInvoker,
    val services: MedatarunServiceRegistry
) {
    fun create(principal: AppPrincipal?): ActionCtx {
        return object : ActionCtx {
            override val extensionRegistry: ExtensionRegistry = runtime.extensionRegistry
            override fun dispatchAction(req: ActionRequest): Any? {
                return actionInvoker.handleInvocation(req, this)
            }
            override fun <T : Any> getService(type: KClass<T>): T = services.getService(type)
            override val principal: ActionPrincipalCtx = ActionPrincipalCtxAdapter.toActionPrincipalCtx(principal)
        }
    }
}