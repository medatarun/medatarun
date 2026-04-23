package io.medatarun.auth.actions

import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionDocSemantics
import io.medatarun.actions.ports.needs.ActionDocSemanticsIntent
import io.medatarun.actions.ports.needs.ActionDocSemanticsMode
import io.medatarun.actions.ports.needs.ActionParamDoc
import io.medatarun.auth.actions.dto.UserListResp
import io.medatarun.auth.domain.PermissionKey
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.domain.role.RoleKey
import io.medatarun.auth.domain.role.RoleRef
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.security.SecurityRuleNames

sealed interface AuthAction<R> {

    @ActionDoc(
        key = "admin_bootstrap",
        title = "Creates admin user",
        description = "Creates admin user account and bootstrap credentials. Consumes the one-time secret generated at install. This will automatically make the admin available as an actor and able to connect with tokens.",
        securityRule = SecurityRuleNames.PUBLIC,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
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
        val username: Username,
        @ActionParamDoc(
            name = "fullname",
            description = "User full name (displayed name)",
            order = 3
        )
        val fullname: Fullname,

        @ActionParamDoc(
            name = "password",
            description = "Admin password",
            order = 4
        )
        val password: PasswordClear

    ) : AuthAction<OAuthTokenResponseDto>

    @ActionDoc(
        key = "user_create",
        title = "Create user",
        description = "Create a new user. This will automatically make this user available as an actor and able to connect with security tokens.",
        securityRule = SecurityRuleNames.ADMIN
    )
    class UserCreate(
        @ActionParamDoc(
            name = "User or tool name (login)",
            description = "User name or tool name used on the login page or to get security tokens. Avoid special characters.",
            order = 1
        )
        val username: Username,
        @ActionParamDoc(
            name = "Displayed name",
            description = "How this user or tool is named on the screens. Usually the first and last name of a person, or the full name of the tool.",
            order = 2
        )
        val fullname: Fullname,
        @ActionParamDoc(
            name = "password",
            description = "User password",
            order = 3
        )
        val password: PasswordClear,
        @ActionParamDoc(
            name = "Administrator privileges",
            description = "Gives this user or tool administrator privileges. This gives or removes the special admin role in the corresponding actor.",
            order = 4
        )
        val admin: Boolean

    ) : AuthAction<Unit>

    @ActionDoc(
        key = "login",
        title = "Login user",
        description = "Generates a JWT Access Token for API calls that users can reuse to authenticate themselves in API or CLI calls (OAuth format, not OIDC)",
        securityRule = SecurityRuleNames.PUBLIC,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    class Login(
        @ActionParamDoc(
            name = "username",
            description = "User name",
            order = 1
        )
        val username: Username,

        @ActionParamDoc(
            name = "password",
            description = "Password",
            order = 2
        )
        val password: PasswordClear
    ) : AuthAction<OAuthTokenResponseDto>

    @ActionDoc(
        key="whoami",
        title="Who am i",
        description = "Tells who is the connected user. Allow you to know if you have the credentials you need",
        securityRule = SecurityRuleNames.SIGNED_IN,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    class WhoAmI : AuthAction<WhoAmIRespDto>

    @ActionDoc(
        key="change_my_password",
        title="Change own password",
        description = "Change connected user password. Must provide current password and a new password. Only available to authentified user.",
        securityRule = SecurityRuleNames.SIGNED_IN,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    class ChangeMyPassword(
        @ActionParamDoc(
            name = "currentPassword",
            description = "Current Password",
            order = 1
        )
        val oldPassword: PasswordClear,
        @ActionParamDoc(
            name = "newPassword",
            description = "New Password",
            order = 2
        )
        val newPassword: PasswordClear
    ): AuthAction<Unit>

    @ActionDoc(
        key="user_change_password",
        title="Change user password",
        description = "Change a user password. Only available for admins.",
        securityRule = SecurityRuleNames.ADMIN,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    class UserChangePassword(
        @ActionParamDoc(
            name = "username",
            description = "User name",
            order = 1
        )
        val username: Username,
        @ActionParamDoc(
            name = "password",
            description = "New password for this user",
            order = 2
        )
        val password: PasswordClear
    ): AuthAction<Unit>

    @ActionDoc(
        key="user_disable",
        title="Disable user",
        description = "Disable a user account. Only available for admins. This will automatically make the corresponding actor disabled and unable to connect with tokens.",
        securityRule = SecurityRuleNames.ADMIN,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.DECLARED, ActionDocSemanticsIntent.UPDATE, ["user(username)"])

    )
    class UserDisable(
        @ActionParamDoc(
            name = "username",
            description = "User name",
            order = 1
        )
        val username: Username,
    ): AuthAction<Unit>

    @ActionDoc(
        key="user_enable",
        title="Enable user",
        description = "Enable a user account. Only available for admins. This will automatically make the corresponding actor enabled and able to connect with tokens.",
        securityRule = SecurityRuleNames.ADMIN,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.DECLARED, ActionDocSemanticsIntent.UPDATE, ["user(username)"])

    )
    class UserEnable(
        @ActionParamDoc(
            name = "username",
            description = "User name",
            order = 1
        )
        val username: Username,
    ): AuthAction<Unit>

    @ActionDoc(
        key="user_change_fullname",
        title="Change user full name",
        description = "Change user full name. Only available for admins. This will automatically change the corresponding actor fullname.",
        securityRule = SecurityRuleNames.ADMIN,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.DECLARED, ActionDocSemanticsIntent.UPDATE, subjects=["user(username)"], returns=[])
    )
    class UserChangeFullname(
        @ActionParamDoc(
            name = "username",
            description = "User name",
            order = 1
        )
        val username: Username,
        @ActionParamDoc(
            name = "fullname",
            description = "Full name (displayed name)",
            order = 2
        )
        val fullname: Fullname,
    ): AuthAction<Unit>

    @ActionDoc(
        key="user_list",
        title="User list",
        description = "Lists available users. Only available for admins.",
        securityRule = SecurityRuleNames.ADMIN,
        semantics = ActionDocSemantics(mode = ActionDocSemanticsMode.DECLARED, intent = ActionDocSemanticsIntent.READ, subjects = [], returns = ["user"])
    )
    class UserList: AuthAction<UserListResp>

    @ActionDoc(
        key = "role_create",
        title = "Create role",
        description = "Create a new role.",
        securityRule = SecurityRuleNames.ADMIN
    )
    class RoleCreate(
        @ActionParamDoc(
            name = "Key",
            description = "Role key. Must be unique across all roles. Serves as a technical identifier for integrations.",
            order = 2
        )
        val key: RoleKey,
        @ActionParamDoc(
            name = "Name",
            description = "Role display name.",
            order = 1
        )
        val name: String,
        @ActionParamDoc(
            name = "Description",
            description = "Role description, explain what this role is for and when it can be used.",
            order = 3
        )
        val description: String?
    ) : AuthAction<Unit>

    @ActionDoc(
        key = "role_list",
        title = "List roles",
        description = "List all roles.",
        securityRule = SecurityRuleNames.ADMIN
    )
    class RoleList : AuthAction<RoleListDto>

    @ActionDoc(
        key = "role_get",
        title = "Get role",
        description = "Get a role and its permissions.",
        securityRule = SecurityRuleNames.ADMIN
    )
    class RoleGet(
        @ActionParamDoc(
            name = "Role",
            description = "Role reference",
            order = 1
        )
        val roleRef: RoleRef
    ) : AuthAction<RoleDetailsDto>

    @ActionDoc(
        key = "role_update_name",
        title = "Update role name",
        description = "Update a role name.",
        securityRule = SecurityRuleNames.ADMIN
    )
    class RoleUpdateName(
        @ActionParamDoc(
            name = "Role",
            description = "Role reference",
            order = 1
        )
        val roleRef: RoleRef,
        @ActionParamDoc(
            name = "Name",
            description = "Role name",
            order = 2
        )
        val value: String
    ) : AuthAction<Unit>

    @ActionDoc(
        key = "role_update_key",
        title = "Update role key",
        description = "Update a role key.",
        securityRule = SecurityRuleNames.ADMIN
    )
    class RoleUpdateKey(
        @ActionParamDoc(
            name = "Role",
            description = "Role reference",
            order = 1
        )
        val roleRef: RoleRef,
        @ActionParamDoc(
            name = "Key",
            description = "Role key",
            order = 2
        )
        val value: RoleKey
    ) : AuthAction<Unit>

    @ActionDoc(
        key = "role_update_description",
        title = "Update role description",
        description = "Update a role description.",
        securityRule = SecurityRuleNames.ADMIN
    )
    class RoleUpdateDescription(
        @ActionParamDoc(
            name = "Role",
            description = "Role reference",
            order = 1
        )
        val roleRef: RoleRef,
        @ActionParamDoc(
            name = "Description",
            description = "Role description",
            order = 2
        )
        val value: String?
    ) : AuthAction<Unit>

    @ActionDoc(
        key = "role_add_permission",
        title = "Add role permission",
        description = "Add a permission to a role.",
        securityRule = SecurityRuleNames.ADMIN
    )
    class RoleAddPermission(
        @ActionParamDoc(
            name = "Role",
            description = "Role reference",
            order = 1
        )
        val roleRef: RoleRef,
        @ActionParamDoc(
            name = "Permission",
            description = "Permission key",
            order = 2
        )
        val permissionKey: PermissionKey
    ) : AuthAction<Unit>

    @ActionDoc(
        key = "role_delete_permission",
        title = "Delete role permission",
        description = "Delete a permission from a role.",
        securityRule = SecurityRuleNames.ADMIN
    )
    class RoleDeletePermission(
        @ActionParamDoc(
            name = "Role",
            description = "Role reference",
            order = 1
        )
        val roleRef: RoleRef,
        @ActionParamDoc(
            name = "Permission",
            description = "Permission key",
            order = 2
        )
        val permissionKey: PermissionKey
    ) : AuthAction<Unit>

    @ActionDoc(
        key = "role_delete",
        title = "Delete role",
        description = "Delete a role.",
        securityRule = SecurityRuleNames.ADMIN
    )
    class RoleDelete(
        @ActionParamDoc(
            name = "Role",
            description = "Role reference",
            order = 1
        )
        val roleRef: RoleRef
    ) : AuthAction<Unit>

    @ActionDoc(
        key="actor_list",
        title="List actors",
        description = "List all known actors: all actors maintained by Medatarun and also all external actor that have connected at least once. Only available for admins.",
        securityRule = SecurityRuleNames.ADMIN
    )
    class ActorList : AuthAction<List<ActorInfoDto>>

    @ActionDoc(
        key="actor_get",
        title="Get actor",
        description = "Get an actor by identifier. Only available for admins.",
        securityRule = SecurityRuleNames.ADMIN
    )
    class ActorGet(
        @ActionParamDoc(
            name = "actorId",
            description = "Actor identifier",
            order = 1
        )
        val actorId: ActorId
    ): AuthAction<ActorDetailDto>

    @ActionDoc(
        key="actor_add_role",
        title="Add actor role",
        description = "Add role to actor.",
        securityRule = SecurityRuleNames.ADMIN
    )
    class Actor_AddRole(
        @ActionParamDoc(
            name = "actorId",
            description = "Actor identifier",
            order = 1
        )
        val actorId: ActorId,
        @ActionParamDoc(
            name = "roleRef",
            description = "Role to add",
            order = 2
        )
        val roleRef: RoleRef
    ): AuthAction<Unit>

    @ActionDoc(
        key="actor_delete_role",
        title="Delete actor role",
        description = "Delete role from actor.",
        securityRule = SecurityRuleNames.ADMIN
    )
    class Actor_DeleteRole(
        @ActionParamDoc(
            name = "actorId",
            description = "Actor identifier",
            order = 1
        )
        val actorId: ActorId,
        @ActionParamDoc(
            name = "roleRef",
            description = "Role to delete",
            order = 2
        )
        val roleRef: RoleRef
    ): AuthAction<Unit>


    @ActionDoc(
        key="actor_disable",
        title="Disable actor",
        description = "Disable an actor. Only available for admins.",
        securityRule = SecurityRuleNames.ADMIN,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.DECLARED, ActionDocSemanticsIntent.UPDATE, ["actor(actorId)"])
    )
    class ActorDisable(
        @ActionParamDoc(
            name = "actorId",
            description = "Actor identifier",
            order = 1
        )
        val actorId: ActorId
    ): AuthAction<Unit>

    @ActionDoc(
        key="actor_enable",
        title="Enable actor",
        description = "Enable an actor. Only available for admins.",
        securityRule = SecurityRuleNames.ADMIN,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.DECLARED, ActionDocSemanticsIntent.UPDATE, ["actor(actorId)"])
    )
    class ActorEnable(
        @ActionParamDoc(
            name = "actorId",
            description = "Actor identifier",
            order = 1
        )
        val actorId: ActorId
    ): AuthAction<Unit>
}
