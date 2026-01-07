package io.medatarun.actions.runtime

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionRequest
import io.medatarun.actions.ports.needs.MedatarunPrincipal
import io.medatarun.auth.embedded.AuthEmbeddedBadCredentialsException
import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.kernel.MedatarunServiceRegistry
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.exposed.ModelHumanPrinter
import io.medatarun.model.ports.exposed.ModelQueries
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
            override val modelCmds: ModelCmds = runtime.modelCmds
            override val modelQueries: ModelQueries = runtime.modelQueries
            override val modelHumanPrinter: ModelHumanPrinter = runtime.modelHumanPrinter
            override fun dispatchAction(req: ActionRequest): Any? {
                return actionRegistry.handleInvocation(req, this)
            }
            override fun <T : Any> getService(type: KClass<T>): T = services.getService(type)
            override val principal: ActionPrincipalCtx = object: ActionPrincipalCtx {
                override fun ensureIsAdmin() {
                    if (principal == null) throw AuthEmbeddedBadCredentialsException()
                    if (!principal.isAdmin) throw AuthEmbeddedBadCredentialsException()
                }
            }
        }
    }
}