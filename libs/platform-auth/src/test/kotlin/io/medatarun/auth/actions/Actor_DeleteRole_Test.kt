package io.medatarun.auth.actions

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.auth.domain.ActorDeleteRoleNotFoundException
import io.medatarun.auth.domain.ActorNotFoundException
import io.medatarun.auth.domain.PermissionKey
import io.medatarun.auth.domain.RoleNotFoundByKeyException
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.domain.role.RoleRef.Companion.roleRefKey
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.lang.http.StatusCode
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.security.AppPermissionCategory
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@EnableDatabaseTests
class Actor_DeleteRole_Test {

    /**
     * Verifies that an administrator can remove a role from an actor and that the actor
     * details no longer expose this role.
     */
    @Test
    fun `remove a custom role from an actor`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role removed from an actor"))
        val role = env.dispatch(AuthAction.Role_Get(roleRef)).role
        env.createJohn()
        val johnActor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            env.johnUsername.value
        )
        assertNotNull(johnActor)
        env.dispatch(AuthAction.Actor_AddRole(johnActor.id, roleRef))

        env.dispatch(AuthAction.Actor_DeleteRole(johnActor.id, roleRef))

        val actor = env.dispatch(AuthAction.Actor_Get(johnActor.id))
        assertFalse(actor.roles.contains(role.id))
    }

    /**
     * Verifies that removing a role from an actor also removes the permissions the actor received
     * through this role.
     */
    @Test
    fun `remove role permissions from an actor through a role`() {
        val permission = "test.write"
        val env = AuthEnvTest(
            otherPermissions = setOf(
                AuthEnvTest.TestOtherPermission(permission, AppPermissionCategory.WRITE),
            )
        )
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role giving a removable permission"))
        env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(permission)))
        env.createJohn()
        val johnActor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            env.johnUsername.value
        )
        assertNotNull(johnActor)
        env.dispatch(AuthAction.Actor_AddRole(johnActor.id, roleRef))

        env.dispatch(AuthAction.Actor_DeleteRole(johnActor.id, roleRef))

        val actor = env.dispatch(AuthAction.Actor_Get(johnActor.id))
        assertFalse(actor.permissions.contains(permission))
    }

    /**
     * Verifies that removing one role from an actor does not remove the actor's other roles.
     */
    @Test
    fun `remove one role without removing the actor other roles`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val removedRoleRef = roleRefKey("removed-role")
        val keptRoleRef = roleRefKey("kept-role")
        env.dispatch(AuthAction.Role_Create(removedRoleRef.key, "Removed role", "Role removed from the actor"))
        env.dispatch(AuthAction.Role_Create(keptRoleRef.key, "Kept role", "Role that must stay on the actor"))
        val removedRole = env.dispatch(AuthAction.Role_Get(removedRoleRef)).role
        val keptRole = env.dispatch(AuthAction.Role_Get(keptRoleRef)).role
        env.createJohn()
        val johnActor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            env.johnUsername.value
        )
        assertNotNull(johnActor)
        env.dispatch(AuthAction.Actor_AddRole(johnActor.id, removedRoleRef))
        env.dispatch(AuthAction.Actor_AddRole(johnActor.id, keptRoleRef))

        env.dispatch(AuthAction.Actor_DeleteRole(johnActor.id, removedRoleRef))

        val actor = env.dispatch(AuthAction.Actor_Get(johnActor.id))
        assertFalse(actor.roles.contains(removedRole.id))
        assertTrue(actor.roles.contains(keptRole.id))
    }

    /**
     * Verifies that removing a role from an actor is refused when the actor does not have this role.
     */
    @Test
    fun `refuse to remove a role the actor does not have`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role not assigned to the actor"))
        env.createJohn()
        val johnActor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            env.johnUsername.value
        )
        assertNotNull(johnActor)

        assertThrows<ActorDeleteRoleNotFoundException> {
            env.dispatch(AuthAction.Actor_DeleteRole(johnActor.id, roleRef))
        }
    }

    /**
     * Verifies that an actor role removal cannot target a role that does not exist.
     */
    @Test
    fun `refuse to remove a missing role from an actor`() {
        val env = AuthEnvTest()
        env.asAdmin()
        env.createJohn()
        val johnActor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            env.johnUsername.value
        )
        assertNotNull(johnActor)
        val roleRef = roleRefKey("missing-role")

        assertThrows<RoleNotFoundByKeyException> {
            env.dispatch(AuthAction.Actor_DeleteRole(johnActor.id, roleRef))
        }
    }

    /**
     * Verifies that a role cannot be removed from an actor that does not exist.
     */
    @Test
    fun `refuse to remove a role from a missing actor`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role removed from an existing actor only"))

        assertThrows<ActorNotFoundException> {
            env.dispatch(AuthAction.Actor_DeleteRole(ActorId.generate(), roleRef))
        }
    }

    /**
     * Verifies that only administrators can remove a role from an actor and that the actor keeps the
     * role when a non-administrator tries to remove it.
     */
    @Test
    fun `refuse to remove actor role when user is not admin`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role protected by admin rights"))
        val role = env.dispatch(AuthAction.Role_Get(roleRef)).role
        env.createJohn()
        val johnActor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            env.johnUsername.value
        )
        assertNotNull(johnActor)
        env.dispatch(AuthAction.Actor_AddRole(johnActor.id, roleRef))
        env.asUser(env.johnUsername)

        val error = assertThrows<ActionInvocationException> {
            env.dispatch(AuthAction.Actor_DeleteRole(johnActor.id, roleRef))
        }

        assertEquals(StatusCode.FORBIDDEN, error.status)
        env.asAdmin()
        val actor = env.dispatch(AuthAction.Actor_Get(johnActor.id))
        assertTrue(actor.roles.contains(role.id))
    }
}
