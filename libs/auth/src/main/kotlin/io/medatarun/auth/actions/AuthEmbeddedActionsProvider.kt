package io.medatarun.auth.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.ports.needs.getService
import io.medatarun.auth.domain.AuthEmbeddedUserNotFoundException
import io.medatarun.auth.ports.exposed.AuthEmbeddedOIDCService
import io.medatarun.auth.ports.exposed.AuthEmbeddedUserService
import io.medatarun.auth.ports.exposed.OAuthService
import io.medatarun.auth.ports.exposed.OAuthTokenResponse
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class AuthEmbeddedActionsProvider : ActionProvider<AuthEmbeddedAction> {
    override val actionGroupKey: String = "auth"
    override fun findCommandClass(): KClass<AuthEmbeddedAction> {
        return AuthEmbeddedAction::class
    }

    override fun dispatch(
        cmd: AuthEmbeddedAction,
        actionCtx: ActionCtx
    ): Any? {
        val userService = actionCtx.getService<AuthEmbeddedUserService>()
        val oidcService = actionCtx.getService<AuthEmbeddedOIDCService>()
        val oauthService = actionCtx.getService<OAuthService>()
        val launcher = AuthEmbeddedActionsLauncher(userService, oidcService, oauthService, actionCtx.principal)
        return when (cmd) {
            is AuthEmbeddedAction.AdminBootstrap -> launcher.adminBootstrap(cmd)
            is AuthEmbeddedAction.CreateUser -> launcher.createUser(cmd)
            is AuthEmbeddedAction.Login -> launcher.login(cmd)
            is AuthEmbeddedAction.WhoAmI -> launcher.whoami(cmd)
            is AuthEmbeddedAction.ChangeMyPassword -> launcher.changeOwnPassword(cmd)
            is AuthEmbeddedAction.ChangeUserPassword -> launcher.changeUserPassword(cmd)
            is AuthEmbeddedAction.DisableUser -> launcher.disableUser(cmd)
            is AuthEmbeddedAction.ChangeUserFullname -> launcher.changeFullname(cmd)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthEmbeddedActionsProvider::class.java)
    }

}


class AuthEmbeddedActionsLauncher(
    private val userService: AuthEmbeddedUserService,
    private val oidcService: AuthEmbeddedOIDCService,
    private val oauthService: OAuthService,
    private val principal: ActionPrincipalCtx,

    ) {
    fun adminBootstrap(cmd: AuthEmbeddedAction.AdminBootstrap): OAuthTokenResponse {
        val user = userService.adminBootstrap(cmd.secret, cmd.username, cmd.fullname, cmd.password)
        return oauthService.createOAuthAccessTokenForUser(user)
    }

    fun createUser(cmd: AuthEmbeddedAction.CreateUser) {
        principal.ensureIsAdmin()
        userService.createEmbeddedUser(cmd.username, cmd.fullname, cmd.password, cmd.admin)

    }

    fun login(cmd: AuthEmbeddedAction.Login): OAuthTokenResponse {
        val user = userService.loginUser(cmd.username, cmd.password)
        return oauthService.createOAuthAccessTokenForUser(user)
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
        if (actor.issuer != oidcService.oidcIssuer()) throw AuthEmbeddedUserNotFoundException()
        return userService.changeOwnPassword(actor.sub, cmd.oldPassword, cmd.newPassword)
    }

    fun changeUserPassword(cmd: AuthEmbeddedAction.ChangeUserPassword) {
        principal.ensureIsAdmin()
        return userService.changeUserPassword(cmd.username, cmd.password)
    }

    fun disableUser(cmd: AuthEmbeddedAction.DisableUser) {
        principal.ensureIsAdmin()
        return userService.disableUser(cmd.username)
    }

    fun changeFullname(cmd: AuthEmbeddedAction.ChangeUserFullname) {
        principal.ensureIsAdmin()
        return userService.changeUserFullname(cmd.username, cmd.fullname)
    }

}