package io.medatarun.auth.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.ports.needs.getService
import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.UserNotFoundException
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.exposed.OAuthService
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.auth.ports.exposed.UserService
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.reflect.KClass

class AuthEmbeddedActionsProvider : ActionProvider<AuthAction<*>> {
    override val actionGroupKey: String = "auth"
    override fun findCommandClass(): KClass<AuthAction<*>> {
        return AuthAction::class
    }

    override fun dispatch(
        cmd: AuthAction<*>,
        actionCtx: ActionCtx
    ): Any? {
        val userService = actionCtx.getService<UserService>()
        val oidcService = actionCtx.getService<OidcService>()
        val oauthService = actionCtx.getService<OAuthService>()
        val actorService = actionCtx.getService<ActorService>()
        val launcher =
            AuthEmbeddedActionsLauncher(userService, oidcService, oauthService, actorService, actionCtx.principal)
        return when (cmd) {
            is AuthAction.AdminBootstrap -> launcher.adminBootstrap(cmd)
            is AuthAction.UserCreate -> launcher.createUser(cmd)
            is AuthAction.Login -> launcher.login(cmd)
            is AuthAction.WhoAmI -> launcher.whoami(cmd)
            is AuthAction.ChangeMyPassword -> launcher.changeOwnPassword(cmd)
            is AuthAction.UserChangePassword -> launcher.changeUserPassword(cmd)
            is AuthAction.UserDisable -> launcher.disableUser(cmd)
            is AuthAction.UserChangeFullname -> launcher.changeUserFullname(cmd)
            is AuthAction.ActorList -> launcher.listActors(cmd)
            is AuthAction.ActorGet -> launcher.getActor(cmd)
            is AuthAction.ActorSetRoles -> launcher.setActorRoles(cmd)
            is AuthAction.ActorDisable -> launcher.disableActor(cmd)
            is AuthAction.ActorEnable -> launcher.enableActor(cmd)
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
    private val actorService: ActorService,
    private val principal: ActionPrincipalCtx,

    ) {
    fun adminBootstrap(cmd: AuthAction.AdminBootstrap): OAuthTokenResponseDto {
        val user = userService.adminBootstrap(
            cmd.secret,
            cmd.username,
            cmd.fullname,
            cmd.password
        )
        return OAuthTokenResponseDto.valueOf(oauthService.createOAuthAccessTokenForUser(user))
    }

    fun createUser(cmd: AuthAction.UserCreate) {
        userService.createEmbeddedUser(
            cmd.username,
            cmd.fullname,
            cmd.password,
            cmd.admin
        )

    }

    fun login(cmd: AuthAction.Login): OAuthTokenResponseDto {
        val user = userService.loginUser(
            cmd.username,
            cmd.password
        )
        return OAuthTokenResponseDto.valueOf(
            oauthService.createOAuthAccessTokenForUser(user)
        )
    }

    @Suppress("unused")
    fun whoami(cmd: AuthAction.WhoAmI): WhoAmIRespDto {
        val actor = principal.ensureSignedIn()
        return WhoAmIRespDto(
            issuer = actor.issuer,
            sub = actor.subject,
            admin = actor.isAdmin,
            fullname = actor.fullname,
            roles = actor.roles.map { it.key },
        )

    }

    fun changeOwnPassword(cmd: AuthAction.ChangeMyPassword) {
        val actor = principal.ensureSignedIn()
        if (actor.issuer != oidcService.oidcIssuer()) throw UserNotFoundException()
        return userService.changeOwnPassword(
            Username(actor.subject).validate(),
            cmd.oldPassword, cmd.newPassword
        )
    }

    fun changeUserPassword(cmd: AuthAction.UserChangePassword) {
        return userService.changeUserPassword(
            cmd.username,
            cmd.password
        )
    }

    fun disableUser(cmd: AuthAction.UserDisable) {
        return userService.disableUser(
            cmd.username
        )
    }

    fun changeUserFullname(cmd: AuthAction.UserChangeFullname) {
        return userService.changeUserFullname(
            cmd.username,
            cmd.fullname
        )
    }


    fun listActors(@Suppress("UNUSED_PARAMETER") cmd: AuthAction.ActorList): List<ActorInfoDto> {
        return actorService.listActors().map { actor -> toActorInfo(actor) }
    }

    fun getActor(cmd: AuthAction.ActorGet): ActorInfoDto {
        return toActorInfo(actorService.findById(cmd.actorId))
    }

    fun setActorRoles(cmd: AuthAction.ActorSetRoles) {
        actorService.setRoles(cmd.actorId, cmd.roles.map { ActorRole(it) })
    }


    fun disableActor(cmd: AuthAction.ActorDisable) {
        val actor = actorService.findById(cmd.actorId)
        if (actor.issuer == oidcService.oidcIssuer()) {
            userService.disableUser(Username(actor.subject).validate())
        } else {
            actorService.disable(actor.id, Instant.now())
        }
    }

    fun enableActor(cmd: AuthAction.ActorEnable) {
        val actor = actorService.findById(cmd.actorId)
        if (actor.issuer == oidcService.oidcIssuer()) {
            userService.enableUser(Username(actor.subject).validate())
        } else {
            actorService.disable(actor.id, null)
        }
    }

    private fun toActorInfo(actor: Actor): ActorInfoDto {
        return ActorInfoDto(
            id = actor.id.value.toString(),
            issuer = actor.issuer,
            subject = actor.subject,
            fullname = actor.fullname,
            email = actor.email,
            roles = actor.roles.map { it.key },
            disabledAt = actor.disabledDate?.toString(),
            createdAt = actor.createdAt,
            lastSeenAt = actor.lastSeenAt
        )
    }

}
