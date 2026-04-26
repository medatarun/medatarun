package io.medatarun.auth.actions

import io.medatarun.actions.domain.ActionInvocationException
import io.medatarun.auth.domain.ActorAddRoleAlreadyExistException
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
class Actor_AddRole_Test {

    /**
     * Verifies that an administrator can give a business role to an actor and that the actor details
     * expose this role after the assignment.
     */
    @Test
    fun `give a custom role to an actor`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role given to an actor"))
        val role = env.dispatch(AuthAction.Role_Get(roleRef)).role
        env.createJohn()
        val johnActor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            env.johnUsername.value
        )
        assertNotNull(johnActor)

        env.dispatch(AuthAction.Actor_AddRole(johnActor.id, roleRef))

        val actor = env.dispatch(AuthAction.Actor_Get(johnActor.id))
        assertTrue(actor.roles.contains(role.id))
    }

    /**
     * Verifies that assigning a role to an actor gives the actor the permissions carried by this role.
     */
    @Test
    fun `give role permissions to an actor through a role`() {
        val permission = "test.read"
        val env = AuthEnvTest(
            otherPermissions = setOf(
                AuthEnvTest.TestOtherPermission(permission, AppPermissionCategory.READ),
            )
        )
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role giving a permission to an actor"))
        env.dispatch(AuthAction.Role_AddPermission(roleRef, PermissionKey(permission)))
        env.createJohn()
        val johnActor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            env.johnUsername.value
        )
        assertNotNull(johnActor)

        env.dispatch(AuthAction.Actor_AddRole(johnActor.id, roleRef))

        val actor = env.dispatch(AuthAction.Actor_Get(johnActor.id))
        assertTrue(actor.permissions.contains(permission))
    }

    /**
     * Verifies that an actor cannot receive the same role twice and that the existing role assignment
     * is kept unchanged.
     */
    @Test
    fun `refuse to give the same role twice to an actor`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role given once to an actor"))
        val role = env.dispatch(AuthAction.Role_Get(roleRef)).role
        env.createJohn()
        val johnActor = env.actorService.findByIssuerAndSubjectOptional(
            env.oidcService.oidcIssuer(),
            env.johnUsername.value
        )
        assertNotNull(johnActor)
        env.dispatch(AuthAction.Actor_AddRole(johnActor.id, roleRef))

        assertThrows<ActorAddRoleAlreadyExistException> {
            env.dispatch(AuthAction.Actor_AddRole(johnActor.id, roleRef))
        }

        val actor = env.dispatch(AuthAction.Actor_Get(johnActor.id))
        assertEquals(1, actor.roles.count { roleId -> roleId == role.id })
    }

    /**
     * Verifies that an actor cannot receive a role that does not exist.
     */
    @Test
    fun `refuse to give a missing role to an actor`() {
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
            env.dispatch(AuthAction.Actor_AddRole(johnActor.id, roleRef))
        }
    }

    /**
     * Verifies that a role cannot be assigned to an actor that does not exist.
     */
    @Test
    fun `refuse to give a role to a missing actor`() {
        val env = AuthEnvTest()
        env.asAdmin()
        val roleRef = roleRefKey("custom-role")
        env.dispatch(AuthAction.Role_Create(roleRef.key, "Custom role", "Role assigned to an existing actor only"))

        assertThrows<ActorNotFoundException> {
            env.dispatch(AuthAction.Actor_AddRole(ActorId.generate(), roleRef))
        }
    }

    /**
     * Verifies that only administrators can give a role to an actor and that the actor keeps its
     * previous roles when a non-administrator tries to do it.
     */
    @Test
    fun `refuse to give actor role when user is not admin`() {
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
        env.asUser(env.johnUsername)

        val error = assertThrows<ActionInvocationException> {
            env.dispatch(AuthAction.Actor_AddRole(johnActor.id, roleRef))
        }

        assertEquals(StatusCode.FORBIDDEN, error.status)
        env.asAdmin()
        val actor = env.dispatch(AuthAction.Actor_Get(johnActor.id))
        assertFalse(actor.roles.contains(role.id))
    }
}
