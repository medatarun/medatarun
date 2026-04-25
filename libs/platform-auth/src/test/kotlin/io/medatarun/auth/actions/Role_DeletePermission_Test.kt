package io.medatarun.auth.actions

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.auth.domain.PermissionKey
import io.medatarun.auth.domain.RolePermissionNotFoundException
import io.medatarun.auth.domain.RoleUpdatePermissionsManagedRoleException
import io.medatarun.auth.domain.role.RoleRef
import io.medatarun.auth.domain.role.RoleRef.Companion.roleRefKey
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.internal.actors.ManagedRoles
import io.medatarun.lang.http.StatusCode
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.security.AppPermissionCategory
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

@EnableDatabaseTests
class Role_DeletePermission_Test {

    @Test
    fun `delete existing permission from a custom role`() {
        val permission = "test.read"
        val env = AuthEnvTest(
            otherPermissions = setOf(
                AuthEnvTest.TestOtherPermission(permission, AppPermissionCategory.READ),
            )
        )
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role losing a permission"))
        env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(permission)))

        env.dispatch(AuthAction.Role_DeletePermission(roleRef, PermissionKey(permission)))

        val role = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(emptySet(), role.permissions.toSet())
    }

    @Test
    fun `delete one permission without deleting the other role permissions`() {
        val readPermission = "test.read"
        val writePermission = "test.write"
        val env = AuthEnvTest(
            otherPermissions = setOf(
                AuthEnvTest.TestOtherPermission(readPermission, AppPermissionCategory.READ),
                AuthEnvTest.TestOtherPermission(writePermission, AppPermissionCategory.WRITE),
            )
        )
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role losing one permission"))
        env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(readPermission)))
        env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(writePermission)))

        env.dispatch(AuthAction.Role_DeletePermission(roleRef, PermissionKey(readPermission)))

        val role = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(setOf(writePermission), role.permissions.toSet())
    }

    @Test
    fun `refuse to delete a permission that the role does not have`() {
        val permission = "test.read"
        val env = AuthEnvTest(
            otherPermissions = setOf(
                AuthEnvTest.TestOtherPermission(permission, AppPermissionCategory.READ),
            )
        )
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role without this permission"))

        assertThrows<RolePermissionNotFoundException> {
            env.dispatch(AuthAction.Role_DeletePermission(roleRef, PermissionKey(permission)))
        }
    }

    @Test
    fun `refuse to change permissions of a managed role`() {
        val readPermission = "test.read"
        val env = AuthEnvTest(
            otherPermissions = setOf(
                AuthEnvTest.TestOtherPermission(readPermission, AppPermissionCategory.READ),
            )
        )
        env.asAdmin()
        val roleRef = RoleRef.ByKey(ManagedRoles.READER_ROLE_KEY)

        assertThrows<RoleUpdatePermissionsManagedRoleException> {
            env.dispatch(AuthAction.Role_DeletePermission(roleRef, PermissionKey(readPermission)))
        }
    }

    @Test
    fun `refuse to delete role permission when user is not admin`() {
        val permission = "test.read"
        val env = AuthEnvTest(
            otherPermissions = setOf(
                AuthEnvTest.TestOtherPermission(permission, AppPermissionCategory.READ),
            )
        )
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role keeping its permission"))
        env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(permission)))
        env.createJohn()
        env.asUser(env.johnUsername)

        val error = assertThrows<ActionInvocationException> {
            env.dispatch(AuthAction.Role_DeletePermission(roleRef, PermissionKey(permission)))
        }

        assertEquals(StatusCode.FORBIDDEN, error.status)
        env.asAdmin()
        val role = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(setOf(permission), role.permissions.toSet())
    }
}
