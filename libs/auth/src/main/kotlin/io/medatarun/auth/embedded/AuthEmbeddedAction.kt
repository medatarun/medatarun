package io.medatarun.auth.embedded

import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionParamDoc

sealed interface AuthEmbeddedAction {


    @ActionDoc(
        key = "admin_bootstrap",
        title = "Creates admin user",
        description = "Creates admin user account and boostrap credentials. Consumes the one-time secret generated at install.",
        uiLocation = ""
    )
    class AdminBootstrap(
        @ActionParamDoc(
            name = "secret",
            description = "Secret provided at bootstrap",
            order = 0
        )
        val secret: String,
        @ActionParamDoc(
            name = "username",
            description = "Admin user name",
            order = 0
        )
        val username: String,
        @ActionParamDoc(
            name = "fullName",
            description = "User full name (displayed name)",
            order = 0
        )
        val fullName: String,

        @ActionParamDoc(
            name = "password",
            description = "Admin password",
            order = 0
        )
        val password: String

    ): AuthEmbeddedAction

    @ActionDoc(key="create_user", title="Create user", description="Create a new user", uiLocation = "")
    class CreateUser(
        @ActionParamDoc(
            name = "username",
            description = "Admin user name",
            order = 1
        )
        val username: String,
        @ActionParamDoc(
            name = "fullName",
            description = "User full name (displayed name)",
            order = 2
        )
        val fullName: String,
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

    ): AuthEmbeddedAction
}