package io.medatarun.auth.actions

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.auth.domain.RoleAlreadyExistsException
import io.medatarun.auth.domain.RoleCreateConflictsWithManagedKeyException
import io.medatarun.auth.domain.role.RoleRef
import io.medatarun.auth.domain.role.RoleRef.Companion.roleRefKey
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.internal.actors.ManagedRoles
import io.medatarun.lang.http.StatusCode
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@EnableDatabaseTests
class Role_Create_Test {

    @Test
    fun `create custom role with name key and description`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")

        env.dispatch(
            AuthAction.Role_Create(
                key = roleRef.key,
                name = "Custom role",
                description = "Role created for a business team"
            )
        )

        val createdRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(roleRef.key.value, createdRole.role.key)
        assertEquals("Custom role", createdRole.role.name)
        assertEquals("Role created for a business team", createdRole.role.description)
        assertFalse(createdRole.role.managedRole)
        assertEquals(emptyList(), createdRole.permissions)
    }

    @Test
    fun `create custom role without description`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")

        env.dispatch(
            AuthAction.Role_Create(
                key = roleRef.key,
                name = "Custom role",
                description = null
            )
        )

        val createdRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(roleRef.key.value, createdRole.role.key)
        assertEquals("Custom role", createdRole.role.name)
        assertEquals(null, createdRole.role.description)
        assertFalse(createdRole.role.managedRole)
    }

    @Test
    fun `refuse to create two roles with the same key`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")

        env.dispatch(
            AuthAction.Role_Create(
                key = roleRef.key,
                name = "Custom role",
                description = "First role with this key"
            )
        )

        assertThrows<RoleAlreadyExistsException> {
            env.dispatch(
                AuthAction.Role_Create(
                    key = roleRef.key,
                    name = "Other custom role",
                    description = "Second role with this key"
                )
            )
        }

        val existingRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals("Custom role", existingRole.role.name)
        assertEquals("First role with this key", existingRole.role.description)
    }

    @Test
    fun `refuse to create a role with a managed role key`() {
        val env = AuthEnvTest()
        env.asAdmin()

        assertThrows<RoleCreateConflictsWithManagedKeyException> {
            env.dispatch(
                AuthAction.Role_Create(
                    key = ManagedRoles.ADMIN_ROLE_KEY,
                    name = "Other admin role",
                    description = "Role using a key reserved for application-managed roles"
                )
            )
        }

        val managedRole = env.dispatch(AuthAction.Role_Get(RoleRef.ByKey(ManagedRoles.ADMIN_ROLE_KEY)))
        assertEquals(ManagedRoles.adminRole.name, managedRole.role.name)
        assertEquals(ManagedRoles.adminRole.description, managedRole.role.description)
    }

    @Test
    fun `refuse to create role when user is not admin`() {
        val env = AuthEnvTest()
        env.createJohn()
        env.asUser(env.johnUsername)

        val error = assertThrows<ActionInvocationException> {
            env.dispatch(
                AuthAction.Role_Create(
                    key = roleRefKey("custom-role").key,
                    name = "Custom role",
                    description = "Role created by a non admin user"
                )
            )
        }
        assertEquals(StatusCode.FORBIDDEN, error.status)
    }
}
