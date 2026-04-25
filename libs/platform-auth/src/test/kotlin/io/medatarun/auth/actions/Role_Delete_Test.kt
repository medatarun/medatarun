package io.medatarun.auth.actions

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.auth.domain.RoleDeleteManagedForbiddenException
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class Role_Delete_Test {

    @Test
    fun `delete custom role`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(
            AuthAction.Role_Create(
                key = roleRef.key,
                name = "Custom role",
                description = "Role that can be deleted"
            )
        )

        env.dispatch(AuthAction.Role_Delete(roleRef))

        assertThrows<RoleNotFoundByKeyException> {
            env.dispatch(AuthAction.Role_Get(roleRef))
        }

        val roleAfterDelete = env.dispatch(AuthAction.Role_List()).items.firstOrNull { it.key == roleRef.key.value }
        assertNull(roleAfterDelete)
    }

    @Test
    fun `delete role also removes it from actors that had this role`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(
            AuthAction.Role_Create(
                key = roleRef.key,
                name = "Custom role",
                description = "Role assigned to a user before deletion"
            )
        )
        val roleBeforeDelete = env.dispatch(AuthAction.Role_Get(roleRef)).role
        env.createJohn()
        val johnActor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            env.johnUsername.value
        )
        assertNotNull(johnActor)
        env.dispatch(AuthAction.Actor_AddRole(johnActor.id, roleRef))

        val actorBeforeDelete = env.dispatch(AuthAction.ActorGet(johnActor.id))
        assertEquals(setOf(roleBeforeDelete.id), actorBeforeDelete.roles)

        env.dispatch(AuthAction.Role_Delete(roleRef))

        val actorAfterDelete = env.dispatch(AuthAction.ActorGet(johnActor.id))
        assertEquals(emptySet(), actorAfterDelete.roles)
    }

    @Test
    fun `refuse to delete a missing role`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("missing-role")

        assertThrows<RoleNotFoundByKeyException> {
            env.dispatch(AuthAction.Role_Delete(roleRef))
        }
    }

    @Test
    fun `refuse to delete a managed role`() {
        val env = AuthEnvTest()
        env.asAdmin()

        assertThrows<RoleDeleteManagedForbiddenException> {
            env.dispatch(AuthAction.Role_Delete(RoleRef.ByKey(ManagedRoles.ADMIN_ROLE_KEY)))
        }

        val managedRole = env.dispatch(AuthAction.Role_Get(RoleRef.ByKey(ManagedRoles.ADMIN_ROLE_KEY)))
        assertEquals(ManagedRoles.ADMIN_ROLE_KEY.value, managedRole.role.key)
    }

    @Test
    fun `refuse to delete role when user is not admin`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(
            AuthAction.Role_Create(
                key = roleRef.key,
                name = "Custom role",
                description = "Role created before switching to a non admin user"
            )
        )
        env.createJohn()
        env.asUser(env.johnUsername)

        val error = assertThrows<ActionInvocationException> {
            env.dispatch(AuthAction.Role_Delete(roleRef))
        }
        assertEquals(StatusCode.FORBIDDEN, error.status)

        env.asAdmin()
        val existingRole = env.dispatch(AuthAction.Role_Get(roleRef))
        assertEquals(roleRef.key.value, existingRole.role.key)
    }
}
