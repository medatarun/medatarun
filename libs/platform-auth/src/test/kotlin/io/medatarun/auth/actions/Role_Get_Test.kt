package io.medatarun.auth.actions

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.auth.domain.PermissionKey
import io.medatarun.auth.domain.RoleNotFoundByKeyException
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@EnableDatabaseTests
class Role_Get_Test {

    @Test
    fun `can get custom role that do not have permissions`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(
            AuthAction.Role_Create(
                key = roleRef.key,
                name = "Custom role",
                description = "Role without permissions"
            )
        )

        val foundRole = env.dispatch(AuthAction.Role_Get(roleRef))

        assertEquals(roleRef.key.value, foundRole.role.key)
        assertEquals("Custom role", foundRole.role.name)
        assertEquals("Role without permissions", foundRole.role.description)
        assertFalse(foundRole.role.managedRole)
        assertEquals(emptyList(), foundRole.permissions)
    }

    @Test
    fun `can get custom role with its permissions`() {
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
        env.dispatch(
            AuthAction.Role_Create(
                key = roleRef.key,
                name = "Custom role",
                description = "Role with permissions"
            )
        )
        env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(readPermission)))
        env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(writePermission)))

        val foundRole = env.dispatch(AuthAction.Role_Get(roleRef))

        assertEquals(roleRef.key.value, foundRole.role.key)
        assertEquals(setOf(readPermission, writePermission), foundRole.permissions.toSet())
    }

    @Test
    fun `can get managed role with managed marker and permissions`() {
        val readPermission = "test.read"
        val env = AuthEnvTest(
            otherPermissions = setOf(
                AuthEnvTest.TestOtherPermission(readPermission, AppPermissionCategory.READ),
            )
        )
        env.asAdmin()
        val roleRef = RoleRef.ByKey(ManagedRoles.READER_ROLE_KEY)

        val foundRole = env.dispatch(AuthAction.Role_Get(roleRef))

        assertEquals(ManagedRoles.READER_ROLE_KEY.value, foundRole.role.key)
        assertEquals(ManagedRoles.readerRole.name, foundRole.role.name)
        assertTrue(foundRole.role.managedRole)
        assertEquals(setOf(readPermission), foundRole.permissions.toSet())
    }

    @Test
    fun `refuse to get a missing role`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("missing-role")

        assertThrows<RoleNotFoundByKeyException> {
            env.dispatch(AuthAction.Role_Get(roleRef))
        }
    }

    @Test
    fun `refuse to get role when user is not admin`() {
        val env = AuthEnvTest()
        env.createJohn()
        env.asUser(env.johnUsername)

        val error = assertThrows<ActionInvocationException> {
            env.dispatch(AuthAction.Role_Get(RoleRef.ByKey(ManagedRoles.ADMIN_ROLE_KEY)))
        }

        assertEquals(StatusCode.FORBIDDEN, error.status)
    }
}
