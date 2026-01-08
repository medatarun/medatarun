package io.medatarun.auth.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.ports.needs.getService
import io.medatarun.auth.domain.AuthEmbeddedUserNotFoundException
import io.medatarun.auth.ports.exposed.AuthEmbeddedService
import io.medatarun.auth.ports.exposed.JwtTokenResponse
import kotlinx.serialization.Serializable
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
            is AuthEmbeddedAction.Login -> launcher.login(cmd)
            is AuthEmbeddedAction.WhoAmI -> launcher.whoami(cmd)
            is AuthEmbeddedAction.ChangeMyPassword -> launcher.changeOwnPassword(cmd)
            is AuthEmbeddedAction.ChangeUserPassword -> launcher.changeUserPassword(cmd)
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
        service.adminBootstrap(cmd.secret, cmd.username, cmd.fullname, cmd.password)
    }

    fun createUser(cmd: AuthEmbeddedAction.CreateUser) {
        principal.ensureIsAdmin()
        service.createEmbeddedUser(cmd.username, cmd.fullname, cmd.password, cmd.admin)
    }

    fun login(cmd: AuthEmbeddedAction.Login): JwtTokenResponse {
        return service.oidcLogin(cmd.username, cmd.password)
    }

    @Serializable
    data class WhoAmIResp(
        val issuer: String,
        val sub: String,
        val admin: Boolean,
        val issuedAt: String?,
        val expiresAt: String?,
        val audience: List<String>,
        val claims: Map<String, String?>,
    )

    fun whoami(cmd: AuthEmbeddedAction.WhoAmI): WhoAmIResp {
        val actor = principal.ensureSignedIn()
        return WhoAmIResp(
            issuer = actor.issuer,
            sub = actor.sub,
            admin = actor.isAdmin,
            issuedAt = actor.issuedAt?.toString(),
            expiresAt = actor.expiresAt?.toString(),
            audience = actor.audience,
            claims = actor.claims
        )

    }

    fun changeOwnPassword(cmd: AuthEmbeddedAction.ChangeMyPassword) {
        val actor = principal.ensureSignedIn()
        if (actor.issuer != service.oidcIssuer()) throw AuthEmbeddedUserNotFoundException()
        return service.changeOwnPassword(actor.sub, cmd.oldPassword, cmd.newPassword)
    }

    fun changeUserPassword(cmd: AuthEmbeddedAction.ChangeUserPassword) {
        principal.ensureIsAdmin()
        return service.changeUserPassword(cmd.username, cmd.password)
    }

}