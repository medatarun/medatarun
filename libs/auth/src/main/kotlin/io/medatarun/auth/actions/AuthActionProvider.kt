package io.medatarun.auth.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.ports.needs.getService
import io.medatarun.auth.domain.UserNotFoundException
import io.medatarun.auth.ports.exposed.OAuthService
import io.medatarun.auth.ports.exposed.OAuthTokenResponse
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.auth.ports.exposed.UserService
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class AuthEmbeddedActionsProvider : ActionProvider<AuthAction> {
    override val actionGroupKey: String = "auth"
    override fun findCommandClass(): KClass<AuthAction> {
        return AuthAction::class
    }

    override fun dispatch(
        cmd: AuthAction,
        actionCtx: ActionCtx
    ): Any? {
        val userService = actionCtx.getService<UserService>()
        val oidcService = actionCtx.getService<OidcService>()
        val oauthService = actionCtx.getService<OAuthService>()
        val launcher = AuthEmbeddedActionsLauncher(userService, oidcService, oauthService, actionCtx.principal)
        return when (cmd) {
            is AuthAction.AdminBootstrap -> launcher.adminBootstrap(cmd)
            is AuthAction.CreateUser -> launcher.createUser(cmd)
            is AuthAction.Login -> launcher.login(cmd)
            is AuthAction.WhoAmI -> launcher.whoami(cmd)
            is AuthAction.ChangeMyPassword -> launcher.changeOwnPassword(cmd)
            is AuthAction.ChangeUserPassword -> launcher.changeUserPassword(cmd)
            is AuthAction.DisableUser -> launcher.disableUser(cmd)
            is AuthAction.ChangeUserFullname -> launcher.changeFullname(cmd)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthEmbeddedActionsProvider::class.java)
    }

}


class AuthEmbeddedActionsLauncher(
    private val userService: UserService,
    private val oidcService: OidcService,
    private val oauthService: OAuthService,
    private val principal: ActionPrincipalCtx,

    ) {
    fun adminBootstrap(cmd: AuthAction.AdminBootstrap): OAuthTokenResponse {
        val user = userService.adminBootstrap(cmd.secret, cmd.username, cmd.fullname, cmd.password)
        return oauthService.createOAuthAccessTokenForUser(user)
    }

    fun createUser(cmd: AuthAction.CreateUser) {
        principal.ensureIsAdmin()
        userService.createEmbeddedUser(cmd.username, cmd.fullname, cmd.password, cmd.admin)

    }

    fun login(cmd: AuthAction.Login): OAuthTokenResponse {
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

    fun whoami(cmd: AuthAction.WhoAmI): WhoAmIResp {
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

    fun changeOwnPassword(cmd: AuthAction.ChangeMyPassword) {
        val actor = principal.ensureSignedIn()
        if (actor.issuer != oidcService.oidcIssuer()) throw UserNotFoundException()
        return userService.changeOwnPassword(actor.sub, cmd.oldPassword, cmd.newPassword)
    }

    fun changeUserPassword(cmd: AuthAction.ChangeUserPassword) {
        principal.ensureIsAdmin()
        return userService.changeUserPassword(cmd.username, cmd.password)
    }

    fun disableUser(cmd: AuthAction.DisableUser) {
        principal.ensureIsAdmin()
        return userService.disableUser(cmd.username)
    }

    fun changeFullname(cmd: AuthAction.ChangeUserFullname) {
        principal.ensureIsAdmin()
        return userService.changeUserFullname(cmd.username, cmd.fullname)
    }

}