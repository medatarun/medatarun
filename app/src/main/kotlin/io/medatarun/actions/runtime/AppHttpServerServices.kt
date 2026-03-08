package io.medatarun.actions.runtime

import io.medatarun.actions.adapters.ActionPlatform
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.auth.ports.exposed.UserService
import io.medatarun.httpserver.commons.AppPrincipalFactory
import io.medatarun.platform.kernel.PlatformRuntime
import io.medatarun.platform.kernel.getService
import io.medatarun.security.SecurityRulesProvider
import io.medatarun.types.TypeDescriptor

/**
 * Services exposed to the HttpServer
 */
class AppHttpServerServices(
    val runtime: PlatformRuntime,
) {

    private val actionPlatform = ActionPlatform.build(
        runtime.extensions.findContributionsFlat(TypeDescriptor::class),
        runtime.extensions.findContributionsFlat(ActionProvider::class),
        runtime.extensions.findContributionsFlat(SecurityRulesProvider::class),

        )

    val actionRegistry = actionPlatform.registry
    val actionInvoker = actionPlatform.invoker
    val actionCtxFactory = ActionCtxFactory(runtime, actionInvoker, runtime.services)

    val userService = runtime.services.getService<UserService>()
    val oidcService = runtime.services.getService<OidcService>()
    val actorService = runtime.services.getService<ActorService>()
    val principalFactory = AppPrincipalFactory(actorService)


}