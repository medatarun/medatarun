package io.medatarun.auth.actions

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.auth.domain.RoleNotFoundByKeyException
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
class Role_UpdateDescription_Test {

    @Test
    fun `change custom role description`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Initial description"))

        env.dispatch(AuthAction.Role_UpdateDescription(roleRef, "Updated description"))

        val updatedRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals("Updated description", updatedRole.role.description)
    }

    @Test
    fun `remove custom role description`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Initial description"))

        env.dispatch(AuthAction.Role_UpdateDescription(roleRef, null))

        val updatedRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(null, updatedRole.role.description)
    }

    @Test
    fun `change managed role description`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = RoleRef.ByKey(ManagedRoles.READER_ROLE_KEY)

        env.dispatch(AuthAction.Role_UpdateDescription(roleRef, "Reader role updated by an admin"))

        val updatedRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals("Reader role updated by an admin", updatedRole.role.description)
    }

    @Test
    fun `refuse to change description of a missing role`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("missing-role")

        assertThrows<RoleNotFoundByKeyException> {
            env.dispatch(AuthAction.Role_UpdateDescription(roleRef, "Updated description"))
        }
    }

    @Test
    fun `refuse to change role description when user is not admin`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Initial description"))
        env.createJohn()
        env.asUser(env.johnUsername)

        val error = assertThrows<ActionInvocationException> {
            env.dispatch(AuthAction.Role_UpdateDescription(roleRef, "Updated by a non admin user"))
        }

        assertEquals(StatusCode.FORBIDDEN, error.status)
        env.asAdmin()
        val unchangedRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals("Initial description", unchangedRole.role.description)
    }
}
