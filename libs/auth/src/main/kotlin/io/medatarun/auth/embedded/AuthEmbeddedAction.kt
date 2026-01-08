package io.medatarun.auth.embedded

import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionParamDoc

sealed interface AuthEmbeddedAction {


    @ActionDoc(
        key = "admin_bootstrap",
        title = "Creates admin user",
        description = "Creates admin user account and bootstrap credentials. Consumes the one-time secret generated at install.",
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

    ) : AuthEmbeddedAction

    @ActionDoc(key = "create_user", title = "Create user", description = "Create a new user", uiLocation = "")
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

    ) : AuthEmbeddedAction

    @ActionDoc(
        key = "login",
        title = "Login user",
        description = "Generates a JWT Token that users can reuse to authenticate themselves",
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
    ) : AuthEmbeddedAction

    @ActionDoc(
        key="whoami",
        title="Who am i",
        description = "Tells who is the connected user. Allow you to know if you have the credentials you need",
        uiLocation = ""
    )
    class WhoAmI(): AuthEmbeddedAction

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
    ): AuthEmbeddedAction

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
    ): AuthEmbeddedAction
}