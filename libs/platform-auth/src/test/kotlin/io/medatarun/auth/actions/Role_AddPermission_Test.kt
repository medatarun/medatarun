package io.medatarun.auth.actions

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.auth.domain.AuthUnknownPermissionException
import io.medatarun.auth.domain.PermissionKey
import io.medatarun.auth.domain.RolePermissionAlreadyExistsException
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
class Role_AddPermission_Test {

    @Test
    fun `add known permission to a custom role`() {
        val permission = "test.read"
        val env = AuthEnvTest(
            otherPermissions = setOf(
                AuthEnvTest.TestOtherPermission(permission, AppPermissionCategory.READ),
            )
        )
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role receiving a permission"))

        env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(permission)))

        val role = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(setOf(permission), role.permissions.toSet())
    }

    @Test
    fun `add several known permissions to the same custom role`() {
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
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role receiving permissions"))

        env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(readPermission)))
        env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(writePermission)))

        val role = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(setOf(readPermission, writePermission), role.permissions.toSet())
    }

    @Test
    fun `refuse to add the same permission twice to a role`() {
        val permission = "test.read"
        val env = AuthEnvTest(
            otherPermissions = setOf(
                AuthEnvTest.TestOtherPermission(permission, AppPermissionCategory.READ),
            )
        )
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role receiving a permission"))
        env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(permission)))

        assertThrows<RolePermissionAlreadyExistsException> {
            env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(permission)))
        }

        val role = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(setOf(permission), role.permissions.toSet())
    }

    @Test
    fun `refuse to add an unknown permission to a role`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role receiving a permission"))

        assertThrows<AuthUnknownPermissionException> {
            env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey("test.unknown")))
        }

        val role = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(emptySet(), role.permissions.toSet())
    }

    @Test
    fun `refuse to change permissions of a managed role`() {
        val permission = "test.read"
        val env = AuthEnvTest(
            otherPermissions = setOf(
                AuthEnvTest.TestOtherPermission(permission, AppPermissionCategory.READ),
            )
        )
        env.asAdmin()
        val roleRef = RoleRef.ByKey(ManagedRoles.READER_ROLE_KEY)

        assertThrows<RoleUpdatePermissionsManagedRoleException> {
            env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(permission)))
        }
    }

    @Test
    fun `refuse to add role permission when user is not admin`() {
        val permission = "test.read"
        val env = AuthEnvTest(
            otherPermissions = setOf(
                AuthEnvTest.TestOtherPermission(permission, AppPermissionCategory.READ),
            )
        )
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role receiving a permission"))
        env.createJohn()
        env.asUser(env.johnUsername)

        val error = assertThrows<ActionInvocationException> {
            env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(permission)))
        }

        assertEquals(StatusCode.FORBIDDEN, error.status)
        env.asAdmin()
        val role = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(emptySet(), role.permissions.toSet())
    }
}
