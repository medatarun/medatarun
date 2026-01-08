package io.medatarun.actions.runtime

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.actions.ports.needs.MedatarunPrincipal
import io.medatarun.auth.domain.AuthEmbeddedBadCredentialsException
import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.kernel.MedatarunServiceRegistry
import io.medatarun.runtime.AppRuntime
import kotlin.reflect.KClass

class ActionCtxFactory(
    val runtime: AppRuntime,
    val actionRegistry: ActionRegistry,
    val services: MedatarunServiceRegistry
) {
    fun create(principal: MedatarunPrincipal?): ActionCtx {
        return object : ActionCtx {
            override val extensionRegistry: ExtensionRegistry = runtime.extensionRegistry
            override fun dispatchAction(req: ActionRequest): Any? {
                return actionRegistry.handleInvocation(req, this)
            }
            override fun <T : Any> getService(type: KClass<T>): T = services.getService(type)
            override val principal: ActionPrincipalCtx = object: ActionPrincipalCtx {

                override val actor: MedatarunPrincipal? = principal
                override fun ensureSignedIn(): MedatarunPrincipal {
                    if (principal == null) throw AuthEmbeddedBadCredentialsException()
                    return principal
                }

                override fun ensureIsAdmin() {
                    if (principal == null) throw AuthEmbeddedBadCredentialsException()
                    if (!principal.isAdmin) throw AuthEmbeddedBadCredentialsException()
                }
            }
        }
    }
}