package io.medatarun.actions.runtime

import io.medatarun.actions.adapters.ActionPlatform
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.auth.ports.exposed.UserService
import io.medatarun.auth.ports.needs.PermissionsRegistry
import io.medatarun.httpserver.commons.AppPrincipalFactory
import io.medatarun.platform.kernel.PlatformRuntime
import io.medatarun.platform.kernel.getService
import io.medatarun.security.SecurityPermissionRegistry

/**
 * Services exposed to the HttpServer
 */
class AppHttpServerServices(
    val runtime: PlatformRuntime,
) {

    private val actionPlatform = runtime.services.getService<ActionPlatform>()
    val actionRegistry = actionPlatform.registry
    val actionInvoker = actionPlatform.invoker
    val actionRequestCtxFactory = ActionRequestCtxFactory()

    val userService = runtime.services.getService<UserService>()
    val oidcService = runtime.services.getService<OidcService>()
    val actorService = runtime.services.getService<ActorService>()
    val principalFactory = AppPrincipalFactory(actorService)


}
