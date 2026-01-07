package io.medatarun.auth.embedded

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.ports.needs.getService
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class AuthEmbeddedActionsProvider(

) : ActionProvider<AuthEmbeddedAction> {
    override val actionGroupKey: String = "auth"
    override fun findCommandClass(): KClass<AuthEmbeddedAction> {
        return AuthEmbeddedAction::class
    }

    override fun dispatch(
        cmd: AuthEmbeddedAction,
        actionCtx: ActionCtx
    ): Any? {
        val service = actionCtx.getService<AuthEmbeddedService>()
        val launcher = AuthEmbeddedActionsLauncher(service, actionCtx.principal)
        return when (cmd) {
            is AuthEmbeddedAction.AdminBootstrap -> launcher.adminBootstrap(cmd)
            is AuthEmbeddedAction.CreateUser -> launcher.createUser(cmd)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthEmbeddedActionsProvider::class.java)
    }

}


class AuthEmbeddedActionsLauncher(
    private val service: AuthEmbeddedService,
    private val principal: ActionPrincipalCtx
) {
    fun adminBootstrap(cmd: AuthEmbeddedAction.AdminBootstrap) {
        service.adminBootstrap(cmd.secret, cmd.username, cmd.fullName, cmd.password)
    }

    fun createUser(cmd: AuthEmbeddedAction.CreateUser) {
        principal.ensureIsAdmin()
        service.createEmbeddedUser(cmd.username, cmd.fullName, cmd.password, cmd.admin)
    }

}