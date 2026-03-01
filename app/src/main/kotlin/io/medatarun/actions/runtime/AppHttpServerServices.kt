package io.medatarun.actions.runtime

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

    val actionSecurityRuleEvaluators = ActionSecurityRuleEvaluators(
        runtime.extensions.findContributionsFlat(SecurityRulesProvider::class)
            .flatMap { it.getRules() }
    )
    val actionTypesRegistry = ActionTypesRegistry(
        runtime.extensions.findContributionsFlat(TypeDescriptor::class)
    )
    val actionRegistry = ActionRegistry(
        actionSecurityRuleEvaluators,
        actionTypesRegistry,
        runtime.extensions.findContributionsFlat(ActionProvider::class)
    )

    val actionInvoker = ActionInvoker(
        actionRegistry,
        actionTypesRegistry,
        actionSecurityRuleEvaluators
    )

    val actionCtxFactory = ActionCtxFactory(runtime, actionInvoker, runtime.services)

    val userService = runtime.services.getService<UserService>()
    val oidcService = runtime.services.getService<OidcService>()
    val actorService = runtime.services.getService<ActorService>()
    val principalFactory = AppPrincipalFactory(actorService)


}