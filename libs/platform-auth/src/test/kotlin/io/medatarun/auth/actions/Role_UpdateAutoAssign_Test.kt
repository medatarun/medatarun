package io.medatarun.auth.actions

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.auth.domain.RoleNotFoundByKeyException
import io.medatarun.auth.domain.RoleUpdateAutoAssignAdminRoleForbiddenException
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
import kotlin.test.assertTrue

@EnableDatabaseTests
class Role_UpdateAutoAssign_Test {

    @Test
    fun `assign custom role automatically when no role is auto assigned`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role to assign automatically"))
        val createdRole = env.dispatch(AuthAction.Role_Get(roleRef))
        val updatedAt = createdRole.role.lastUpdatedAt.plusSeconds(60)
        env.authClockTests.staticNow = updatedAt

        env.dispatch(AuthAction.Role_UpdateAutoAssign(roleRef, true))

        val updatedRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertTrue(updatedRole.role.autoAssign)
        assertEquals(updatedAt, updatedRole.role.lastUpdatedAt)
    }

    @Test
    fun `move auto assign from existing role to custom role`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val firstRoleRef = roleRefKey("first-role")
        val secondRoleRef = roleRefKey("second-role")
        env.dispatch(AuthAction.Role_Create(firstRoleRef.key, "First role", "Initially assigned automatically"))
        env.dispatch(AuthAction.Role_Create(secondRoleRef.key, "Second role", "Role to assign automatically"))
        env.dispatch(AuthAction.Role_UpdateAutoAssign(firstRoleRef, true))

        env.dispatch(AuthAction.Role_UpdateAutoAssign(secondRoleRef, true))

        val firstRole = env.dispatch(AuthAction.Role_Get(firstRoleRef))
        val secondRole = env.dispatch(AuthAction.Role_Get(secondRoleRef))
        assertFalse(firstRole.role.autoAssign)
        assertTrue(secondRole.role.autoAssign)
    }

    @Test
    fun `remove auto assign from custom role`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role to assign automatically"))
        env.dispatch(AuthAction.Role_UpdateAutoAssign(roleRef, true))

        env.dispatch(AuthAction.Role_UpdateAutoAssign(roleRef, false))

        val updatedRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertFalse(updatedRole.role.autoAssign)
    }

    @Test
    fun `remove auto assign from another role does not change current auto assign role`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val assignedRoleRef = roleRefKey("assigned-role")
        val targetRoleRef = roleRefKey("target-role")
        env.dispatch(AuthAction.Role_Create(assignedRoleRef.key, "Assigned role", "Currently assigned automatically"))
        env.dispatch(AuthAction.Role_Create(targetRoleRef.key, "Target role", "Receives the false update"))
        env.dispatch(AuthAction.Role_UpdateAutoAssign(assignedRoleRef, true))

        env.dispatch(AuthAction.Role_UpdateAutoAssign(targetRoleRef, false))

        val assignedRole = env.dispatch(AuthAction.Role_Get(assignedRoleRef))
        val targetRole = env.dispatch(AuthAction.Role_Get(targetRoleRef))
        assertTrue(assignedRole.role.autoAssign)
        assertFalse(targetRole.role.autoAssign)
    }

    @Test
    fun `change managed role auto assign`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = RoleRef.ByKey(ManagedRoles.READER_ROLE_KEY)

        env.dispatch(AuthAction.Role_UpdateAutoAssign(roleRef, true))

        val updatedRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertTrue(updatedRole.role.autoAssign)
    }

    @Test
    fun `refuse to assign admin role automatically`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = RoleRef.ByKey(ManagedRoles.ADMIN_ROLE_KEY)

        assertThrows<RoleUpdateAutoAssignAdminRoleForbiddenException> {
            env.dispatch(AuthAction.Role_UpdateAutoAssign(roleRef, true))
        }

        val adminRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertFalse(adminRole.role.autoAssign)
    }

    @Test
    fun `refuse to change auto assign of a missing role`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("missing-role")

        assertThrows<RoleNotFoundByKeyException> {
            env.dispatch(AuthAction.Role_UpdateAutoAssign(roleRef, true))
        }
    }

    @Test
    fun `refuse to change role auto assign when user is not admin`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role to assign automatically"))
        env.createJohn()
        env.asUser(env.johnUsername)

        val error = assertThrows<ActionInvocationException> {
            env.dispatch(AuthAction.Role_UpdateAutoAssign(roleRef, true))
        }

        assertEquals(StatusCode.FORBIDDEN, error.status)
        env.asAdmin()
        val unchangedRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertFalse(unchangedRole.role.autoAssign)
    }
}
