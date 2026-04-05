package io.medatarun.auth.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionPrincipalCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.auth.domain.ActorPermission
import io.medatarun.auth.domain.UserNotFoundException
import io.medatarun.auth.domain.role.Role
import io.medatarun.auth.domain.role.RoleId
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.exposed.OAuthService
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.auth.ports.exposed.UserService
import io.medatarun.auth.ports.needs.AuthClock
import kotlin.reflect.KClass

class AuthEmbeddedActionsProvider(
    private val userService: UserService,
    private val oidcService: OidcService,
    private val oauthService: OAuthService,
    private val actorService: ActorService,
    private val clock: AuthClock
) : ActionProvider<AuthAction<*>> {
    override val actionGroupKey: String = "auth"
    override fun findCommandClass(): KClass<AuthAction<*>> {
        return AuthAction::class
    }

    override fun dispatch(
        action: AuthAction<*>,
        actionCtx: ActionCtx
    ): Any {
        val launcher =
            AuthEmbeddedActionsLauncher(
                userService,
                oidcService,
                oauthService,
                actorService,
                actionCtx.principal,
                clock
            )
        return when (action) {
            is AuthAction.AdminBootstrap -> launcher.adminBootstrap(action)
            is AuthAction.UserCreate -> launcher.createUser(action)
            is AuthAction.Login -> launcher.login(action)
            is AuthAction.WhoAmI -> launcher.whoami(action)
            is AuthAction.ChangeMyPassword -> launcher.changeOwnPassword(action)
            is AuthAction.UserChangePassword -> launcher.changeUserPassword(action)
            is AuthAction.UserDisable -> launcher.disableUser(action)
            is AuthAction.UserEnable -> launcher.enableUser(action)
            is AuthAction.UserChangeFullname -> launcher.changeUserFullname(action)
            is AuthAction.RoleCreate -> launcher.roleCreate(action)
            is AuthAction.RoleList -> launcher.roleList(action)
            is AuthAction.RoleGet -> launcher.roleGet(action)
            is AuthAction.RoleUpdateName -> launcher.roleUpdateName(action)
            is AuthAction.RoleUpdateKey -> launcher.roleUpdateKey(action)
            is AuthAction.RoleUpdateDescription -> launcher.roleUpdateDescription(action)
            is AuthAction.RoleAddPermission -> launcher.roleAddPermission(action)
            is AuthAction.RoleDeletePermission -> launcher.roleDeletePermission(action)
            is AuthAction.RoleDelete -> launcher.roleDelete(action)
            is AuthAction.ActorList -> launcher.listActors(action)
            is AuthAction.ActorGet -> launcher.getActor(action)
            is AuthAction.Actor_AddRole -> launcher.actorAddRole(action)
            is AuthAction.Actor_DeleteRole -> launcher.actorDeleteRole(action)
            is AuthAction.ActorDisable -> launcher.disableActor(action)
            is AuthAction.ActorEnable -> launcher.enableActor(action)
        }
    }

}


class AuthEmbeddedActionsLauncher(
    private val userService: UserService,
    private val oidcService: OidcService,
    private val oauthService: OAuthService,
    private val actorService: ActorService,
    private val principal: ActionPrincipalCtx,
    private val clock: AuthClock,

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
            roles = actor.permissions.map { it.key },
            permissions = actor.permissions.map { it.key },
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

    fun enableUser(cmd: AuthAction.UserEnable) {
        return userService.enableUser(
            cmd.username
        )
    }

    fun changeUserFullname(cmd: AuthAction.UserChangeFullname) {
        return userService.changeUserFullname(
            cmd.username,
            cmd.fullname
        )
    }

    fun roleCreate(cmd: AuthAction.RoleCreate): RoleId {
        return actorService.createRole(
            key = cmd.key,
            name = cmd.name,
            description = cmd.description
        )
    }

    fun roleList(@Suppress("UNUSED_PARAMETER") cmd: AuthAction.RoleList): RoleListDto {
        return RoleListDto(
            items = actorService.listRoles().map { role -> toRoleInfo(role) }
        )
    }

    fun roleGet(cmd: AuthAction.RoleGet): RoleDetailsDto {
        val role = actorService.findRoleByRef(cmd.roleRef)
        return RoleDetailsDto(
            role = toRoleInfo(role),
            permissions = actorService.listRolePermissions(cmd.roleRef).map { it.key }
        )
    }

    fun roleUpdateName(cmd: AuthAction.RoleUpdateName) {
        actorService.updateRoleName(
            roleRef = cmd.roleRef,
            name = cmd.value
        )
    }

    fun roleUpdateKey(cmd: AuthAction.RoleUpdateKey) {
        actorService.updateRoleKey(
            roleRef = cmd.roleRef,
            key = cmd.value
        )
    }

    fun roleUpdateDescription(cmd: AuthAction.RoleUpdateDescription) {
        actorService.updateRoleDescription(
            roleRef = cmd.roleRef,
            description = cmd.value
        )
    }

    fun roleAddPermission(cmd: AuthAction.RoleAddPermission) {
        actorService.addRolePermission(
            roleRef = cmd.roleRef,
            permission = ActorPermission(cmd.permissionKey)
        )
    }

    fun roleDeletePermission(cmd: AuthAction.RoleDeletePermission) {
        actorService.deleteRolePermission(
            roleRef = cmd.roleRef,
            permission = ActorPermission(cmd.permissionKey)
        )
    }

    fun roleDelete(cmd: AuthAction.RoleDelete) {
        actorService.deleteRole(cmd.roleRef)
    }


    fun listActors(@Suppress("UNUSED_PARAMETER") cmd: AuthAction.ActorList): List<ActorInfoDto> {
        return actorService.listActors().map { actor ->
            ActorInfoDto(
                id = actor.id.value.toString(),
                issuer = actor.issuer,
                subject = actor.subject,
                fullname = actor.fullname,
                email = actor.email,
                disabledAt = actor.disabledDate?.toString(),
                createdAt = actor.createdAt,
                lastSeenAt = actor.lastSeenAt
            )
        }
    }

    fun getActor(cmd: AuthAction.ActorGet): ActorDetailDto {
        val actor = actorService.findById(cmd.actorId)
        val roles = actorService.findActorRoleIdSet(cmd.actorId)
        val permissions = actorService.findActorPermissionSet(cmd.actorId)
        return ActorDetailDto(
            id = actor.id.value.toString(),
            issuer = actor.issuer,
            subject = actor.subject,
            fullname = actor.fullname,
            email = actor.email,
            roles = roles.map { it.asString() }.toSet(),
            permissions = permissions.map { it.key }.toSortedSet(),
            disabledAt = actor.disabledDate?.toString(),
            createdAt = actor.createdAt,
            lastSeenAt = actor.lastSeenAt
        )
    }

    fun actorAddRole(action: AuthAction.Actor_AddRole) {
        actorService.actorAddRole(action.actorId, action.roleRef)
    }

    fun actorDeleteRole(action: AuthAction.Actor_DeleteRole) {
        actorService.actorDeleteRole(action.actorId, action.roleRef)
    }

    fun disableActor(cmd: AuthAction.ActorDisable) {
        val actor = actorService.findById(cmd.actorId)
        if (actor.issuer == oidcService.oidcIssuer()) {
            userService.disableUser(Username(actor.subject).validate())
        } else {
            actorService.actorDisable(actor.id, clock.now())
        }
    }

    fun enableActor(cmd: AuthAction.ActorEnable) {
        val actor = actorService.findById(cmd.actorId)
        if (actor.issuer == oidcService.oidcIssuer()) {
            userService.enableUser(Username(actor.subject).validate())
        } else {
            actorService.actorDisable(actor.id, null)
        }
    }


    private fun toRoleInfo(role: Role): RoleInfoDto {
        return RoleInfoDto(
            id = role.id.asString(),
            key = role.key.asString(),
            name = role.name,
            description = role.description,
            createdAt = role.createdAt,
            lastUpdatedAt = role.lastUpdatedAt
        )
    }


}
