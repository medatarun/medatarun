package io.medatarun.auth.actions

import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionParamDoc
import io.medatarun.auth.domain.ActorId
import java.time.Instant

sealed interface AuthAction {


    @ActionDoc(
        key = "admin_bootstrap",
        title = "Creates admin user",
        description = "Creates admin user account and bootstrap credentials. Consumes the one-time secret generated at install. This will automatically make the admin available as an actor and able to connect with tokens.",
        uiLocation = ""
    )
    class AdminBootstrap(
        @ActionParamDoc(
            name = "secret",
            description = "Secret provided at bootstrap",
            order = 5
        )
        val secret: String,
        @ActionParamDoc(
            name = "username",
            description = "Admin user name",
            order = 2
        )
        val username: String,
        @ActionParamDoc(
            name = "fullname",
            description = "User full name (displayed name)",
            order = 3
        )
        val fullname: String,

        @ActionParamDoc(
            name = "password",
            description = "Admin password",
            order = 4
        )
        val password: String

    ) : AuthAction

    @ActionDoc(key = "create_user", title = "Create user", description = "Create a new user. This will automatically make this user available as an actor and able to connect with tokens.", uiLocation = "")
    class CreateUser(
        @ActionParamDoc(
            name = "username",
            description = "Admin user name",
            order = 1
        )
        val username: String,
        @ActionParamDoc(
            name = "fullname",
            description = "User full name (displayed name)",
            order = 2
        )
        val fullname: String,
        @ActionParamDoc(
            name = "password",
            description = "User password",
            order = 3
        )
        val password: String,
        @ActionParamDoc(
            name = "admin",
            description = "Is user admin",
            order = 4
        )
        val admin: Boolean

    ) : AuthAction

    @ActionDoc(
        key = "login",
        title = "Login user",
        description = "Generates a JWT Access Token for API calls that users can reuse to authenticate themselves in API or CLI calls (OAuth format, not OIDC)",
        uiLocation = ""
    )
    class Login(
        @ActionParamDoc(
            name = "username",
            description = "User name",
            order = 1
        )
        val username: String,

        @ActionParamDoc(
            name = "password",
            description = "Password",
            order = 2
        )
        val password: String
    ) : AuthAction

    @ActionDoc(
        key="whoami",
        title="Who am i",
        description = "Tells who is the connected user. Allow you to know if you have the credentials you need",
        uiLocation = ""
    )
    class WhoAmI(): AuthAction

    @ActionDoc(
        key="change_my_password",
        title="Change own password",
        description = "Change connected user password. Must provide current password and a new password. Only available to authentified user.",
        uiLocation = ""
    )
    class ChangeMyPassword(
        @ActionParamDoc(
            name = "currentPassword",
            description = "Current Password",
            order = 1
        )
        val oldPassword: String,
        @ActionParamDoc(
            name = "newPassword",
            description = "New Password",
            order = 2
        )
        val newPassword: String
    ): AuthAction

    @ActionDoc(
        key="change_user_password",
        title="Change user password",
        description = "Change a user password. Only available for admins.",
        uiLocation = ""
    )
    class ChangeUserPassword(
        @ActionParamDoc(
            name = "username",
            description = "User name",
            order = 1
        )
        val username: String,
        @ActionParamDoc(
            name = "password",
            description = "New password for this user",
            order = 2
        )
        val password: String
    ): AuthAction

    @ActionDoc(
        key="disable_user",
        title="Disable user",
        description = "Disable a user account. Only available for admins. This will automatically make the corresponding actor disabled and unable to connect with tokens.",
        uiLocation = ""
    )
    class DisableUser(
        @ActionParamDoc(
            name = "username",
            description = "User name",
            order = 1
        )
        val username: String,
    ): AuthAction

    @ActionDoc(
        key="change_user_fullname",
        title="Change user full name",
        description = "Change user full name. Only available for admins. This will automatically change the corresponding actor fullname.",
        uiLocation = ""
    )
    class ChangeUserFullname(
        @ActionParamDoc(
            name = "username",
            description = "User name",
            order = 1
        )
        val username: String,
        @ActionParamDoc(
            name = "fullname",
            description = "Full name (displayed name)",
            order = 2
        )
        val fullname: String,
    ): AuthAction

    @ActionDoc(
        key="list_actors",
        title="List actors",
        description = "List all known actors: all actors maintained by Medatarun and also all external actor that have connected at least once. Only available for admins.",
        uiLocation = ""
    )
    class ListActors(): AuthAction

    @ActionDoc(
        key="set_actor_roles",
        title="Set actor roles",
        description = "Replace roles for an actor. Only available for admins.",
        uiLocation = ""
    )
    class SetActorRoles(
        @ActionParamDoc(
            name = "actorId",
            description = "Actor identifier",
            order = 1
        )
        val actorId: ActorId,
        @ActionParamDoc(
            name = "roles",
            description = "Role names",
            order = 2
        )
        val roles: List<String>
    ): AuthAction

    @ActionDoc(
        key="disable_actor",
        title="Disable actor",
        description = "Disable an actor. Only available for admins.",
        uiLocation = ""
    )
    class DisableActor(
        @ActionParamDoc(
            name = "actorId",
            description = "Actor identifier",
            order = 1
        )
        val actorId: ActorId,
        @ActionParamDoc(
            name = "date",
            description = "Disabled date. If not provided, will be the current instant.",
            order = 1
        )
        val date: Instant? = null
    ): AuthAction

    @ActionDoc(
        key="enable_actor",
        title="Enable actor",
        description = "Enable an actor. Only available for admins.",
        uiLocation = ""
    )
    class EnableActor(
        @ActionParamDoc(
            name = "actorId",
            description = "Actor identifier",
            order = 1
        )
        val actorId: ActorId
    ): AuthAction
}
