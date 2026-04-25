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
class Role_UpdateName_Test {

    @Test
    fun `change custom role name`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role with a name to change"))

        env.dispatch(AuthAction.Role_UpdateName(roleRef, "Updated role"))

        val updatedRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals("Updated role", updatedRole.role.name)
    }

    @Test
    fun `change managed role name`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = RoleRef.ByKey(ManagedRoles.READER_ROLE_KEY)

        env.dispatch(AuthAction.Role_UpdateName(roleRef, "Updated reader"))

        val updatedRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals("Updated reader", updatedRole.role.name)
    }

    @Test
    fun `refuse to change name of a missing role`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("missing-role")

        assertThrows<RoleNotFoundByKeyException> {
            env.dispatch(AuthAction.Role_UpdateName(roleRef, "Updated role"))
        }
    }

    @Test
    fun `refuse to change role name when user is not admin`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role with a name to change"))
        env.createJohn()
        env.asUser(env.johnUsername)

        val error = assertThrows<ActionInvocationException> {
            env.dispatch(AuthAction.Role_UpdateName(roleRef, "Updated by a non admin user"))
        }

        assertEquals(StatusCode.FORBIDDEN, error.status)
        env.asAdmin()
        val unchangedRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals("Custom role", unchangedRole.role.name)
    }
}
