package io.medatarun.auth.actions

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.auth.domain.RoleAlreadyExistsException
import io.medatarun.auth.domain.RoleNotFoundByKeyException
import io.medatarun.auth.domain.RoleUpdateKeyConflictsWithManagedKeyException
import io.medatarun.auth.domain.role.RoleRef
import io.medatarun.auth.domain.role.RoleRef.Companion.roleRefKey
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.internal.actors.ManagedRoles
import io.medatarun.lang.http.StatusCode
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

@EnableDatabaseTests
class Role_UpdateKey_Test {

    @Test
    fun `change custom role key`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        val newRoleRef = roleRefKey("renamed-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role with a key to change"))

        env.dispatch(AuthAction.Role_UpdateKey(roleRef, newRoleRef.key))

        assertThrows<RoleNotFoundByKeyException> {
            env.dispatch(AuthAction.Role_Get(roleRef))
        }
        val updatedRole = env.dispatch(AuthAction.Role_Get(newRoleRef))
        assertEquals(newRoleRef.key.value, updatedRole.role.key)
        assertEquals("Custom role", updatedRole.role.name)
        assertEquals("Role with a key to change", updatedRole.role.description)
    }

    @Test
    fun `refuse to change custom role key to an existing role key`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        val existingRoleRef = roleRefKey("existing-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role to rename"))
        env.dispatch(AuthAction.Role_Create(existingRoleRef.key, "Existing role", "Role already using this key"))

        assertThrows<RoleAlreadyExistsException> {
            env.dispatch(AuthAction.Role_UpdateKey(roleRef, existingRoleRef.key))
        }

        val unchangedRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(roleRef.key.value, unchangedRole.role.key)
        val existingRole = env.dispatch(AuthAction.Role_Get(existingRoleRef))
        assertEquals("Existing role", existingRole.role.name)
    }

    @Test
    fun `refuse to change custom role key to a managed role key`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role to rename"))

        assertThrows<RoleUpdateKeyConflictsWithManagedKeyException> {
            env.dispatch(AuthAction.Role_UpdateKey(roleRef, ManagedRoles.ADMIN_ROLE_KEY))
        }

        val unchangedRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(roleRef.key.value, unchangedRole.role.key)
    }

    @Test
    fun `refuse to change key of a missing role`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("missing-role")
        val newRoleRef = roleRefKey("renamed-role")

        assertThrows<RoleNotFoundByKeyException> {
            env.dispatch(AuthAction.Role_UpdateKey(roleRef, newRoleRef.key))
        }
    }

    @Test
    fun `refuse to change role key when user is not admin`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        val newRoleRef = roleRefKey("renamed-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role to rename"))
        env.createJohn()
        env.asUser(env.johnUsername)

        val error = assertThrows<ActionInvocationException> {
            env.dispatch(AuthAction.Role_UpdateKey(roleRef, newRoleRef.key))
        }

        assertEquals(StatusCode.FORBIDDEN, error.status)
        env.asAdmin()
        val unchangedRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(roleRef.key.value, unchangedRole.role.key)
    }
}
