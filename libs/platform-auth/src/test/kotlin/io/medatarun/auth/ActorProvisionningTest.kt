package io.medatarun.auth

import io.medatarun.auth.domain.ActorPermission
import io.medatarun.auth.domain.role.RoleKey
import io.medatarun.auth.domain.role.RoleRef
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.fixtures.AuthTestUtils.createJwtExternalPrincipal
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.security.AppPermissionCategory
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.*

/**
 * Tests that actor provisionning works.
 *
 * This is different from the regular action tests as actors are provisionned
 * by side effects when a JWT Token is encountered and not by calling
 * actions.
 *
 */
@EnableDatabaseTests
class ActorProvisionningTest {

    /**
     * Low-level test for creating actors. We directly call the
     * actor service. This is a first step to ensure the behavior
     * of creating an actor.
     */
    @Test
    fun `create stores actor and finds it by issuer and subject`() {
        val env = AuthEnvTest(createAdmin = false)
        val now = Instant.parse("2024-01-03T12:00:00Z")
        env.authClockTests.staticNow = now

        val actor = env.actorService.create(
            issuer = "issuer-a",
            subject = "subject-a",
            fullname = "Alice Example",
            email = "alice@example.com",
            disabled = null
        )

        assertEquals("issuer-a", actor.issuer)
        assertEquals("subject-a", actor.subject)
        assertEquals("Alice Example", actor.fullname)
        assertEquals("alice@example.com", actor.email)
        assertEquals(now, actor.createdAt)
        assertEquals(now, actor.lastSeenAt)

        val found = env.actorService.findByIssuerAndSubjectOptional("issuer-a", "subject-a")
        assertNotNull(found)
        assertEquals(actor.id, found.id)
        assertNull(env.actorService.findByIssuerAndSubjectOptional("issuer-a", "missing"))
    }

    /**
     * If an actor is created directly with a disabled date, then we must
     * keep it.
     */
    @Test
    fun `create stores disabled date when provided`() {
        val env = AuthEnvTest(createAdmin = false)
        val disabledAt = Instant.parse("2024-01-05T08:30:00Z")

        val actor = env.actorService.create(
            issuer = "issuer-disabled",
            subject = "subject-disabled",
            fullname = "Disabled User",
            email = null,
            disabled = disabledAt
        )

        assertEquals(disabledAt, actor.disabledDate)
    }

    /**
     * The real sync test: creates many actors from JWT tokens.
     *
     * We will check that actor names are taken from the JWT token claims. We look in this order
     *
     * - name
     * - fullname
     * - preferredUsername
     *
     * If we don't find anything, we fall back to:
     *
     * - email
     * - subject
     */
    @Test
    fun `sync creates actor with display name precedence`() {
        val env = AuthEnvTest(createAdmin = false)


        // Display name selection falls back through name/fullname/preferredUsername/email/subject.
        val createdWithName = env.actorService.syncFromJwtExternalPrincipal(
            createJwtExternalPrincipal(issuer = "issuer-1", subject = "sub-1", name = "Name Value")
        )
        assertEquals("Name Value", createdWithName.fullname)

        val createdWithFullname = env.actorService.syncFromJwtExternalPrincipal(
            createJwtExternalPrincipal(issuer = "issuer-2", subject = "sub-2", fullname = "Fullname Value")
        )
        assertEquals("Fullname Value", createdWithFullname.fullname)

        val createdWithPreferred = env.actorService.syncFromJwtExternalPrincipal(
            createJwtExternalPrincipal(issuer = "issuer-3", subject = "sub-3", preferredUsername = "Preferred Value")
        )
        assertEquals("Preferred Value", createdWithPreferred.fullname)

        val createdWithEmail = env.actorService.syncFromJwtExternalPrincipal(
            createJwtExternalPrincipal(issuer = "issuer-4", subject = "sub-4", email = "mail@example.com")
        )
        assertEquals("mail@example.com", createdWithEmail.fullname)

        val createdWithSubject = env.actorService.syncFromJwtExternalPrincipal(
            createJwtExternalPrincipal(issuer = "issuer-5", subject = "sub-5")
        )
        assertEquals("sub-5", createdWithSubject.fullname)
    }

    /**
     * Tests that sync actors get the auto assign role
     */
    @Test
    fun `sync actor gets the auto assign role`() {
        val env = AuthEnvTest(createAdmin = false)
        val createdWithSubject = env.actorService.syncFromJwtExternalPrincipal(
            createJwtExternalPrincipal(issuer = "issuer-5", subject = "sub-5")
        )

        // There is an auto-assign role created by the application at startup
        // so that actors have a role. We need to keep it so we can check that
        // provisionned actors have this role.
        val autoAssignRole = env.actorService.listRoles().single { it.autoAssign }

        assertEquals("sub-5", createdWithSubject.fullname)
        assertTrue(env.actorService.actorHasRole(createdWithSubject.id, autoAssignRole.id))
    }

    /**
     * Tests that sync actors do not get the auto assign role if there is none,
     * and that the lack of an auto-assign role does not cause issues
     */
    @Test
    fun `sync actor when no auto assign role`() {
        val env = AuthEnvTest(createAdmin = false)

        // There is an auto-assign role created by the application at startup
        // so that actors have a role. We need to keep track of it
        val autoAssignRole = env.actorService.listRoles().single { it.autoAssign }

        // Now we remove the auto assign role and check it was removed
        env.actorService.roleUpdateAutoAssign(RoleRef.ById(autoAssignRole.id), false)
        val autoAssignRoleAfter = env.actorService.listRoles().firstOrNull { it.autoAssign }
        assertNull(
            autoAssignRoleAfter,
            "Error in auto-assign removal, the role which had auto assign still have it, the test can not continue."
        )

        // Provisions the user
        val createdWithSubject = env.actorService.syncFromJwtExternalPrincipal(
            createJwtExternalPrincipal(issuer = "issuer-5", subject = "sub-5")
        )
        assertEquals("sub-5", createdWithSubject.fullname)
        assertFalse(env.actorService.actorHasRole(createdWithSubject.id, autoAssignRole.id))
        assertTrue(env.actorService.findActorRoleIdSet(createdWithSubject.id).isEmpty())
    }

    /**
     * Tests that if the actor we want to sync already exists in the system
     * updates it but doesn't remove customization:
     * - name and email must be updated
     * - updated time must change
     * - permissions are preserved
     *
     * And even if the auto assign role changed, the user keeps its previous roles.
     *
     */
    @Test
    fun `sync updates existing actor profile`() {
        val specialPermissionKey = "role-x"
        val env = AuthEnvTest(
            createAdmin = false,
            otherPermissions = setOf(AuthEnvTest.TestOtherPermission(specialPermissionKey, AppPermissionCategory.READ))
        )
        val initialTime = Instant.parse("2024-02-01T00:00:00Z")
        val updatedTime = Instant.parse("2024-02-02T00:00:00Z")
        env.authClockTests.staticNow = initialTime

        // There is an auto-assign role created by the application at startup
        // so that actors have a role. We need to keep track of it
        val autoAssignRole = env.actorService.listRoles().single { it.autoAssign }

        // Create an actor directly with the actor service. At this point it should **not**
        // have the auto assign role (we did not provision using JWT)
        val created = env.actorService.create(
            issuer = "issuer-x",
            subject = "subject-x",
            fullname = "Initial Name",
            email = "initial@example.com",
            disabled = null
        )

        // check that the actor doesn't have the role otherwise it would make no sense in this test
        assertFalse(env.actorService.actorHasRole(created.id, autoAssignRole.id))

        // Create new role and add it to the actor
        val roleCustomId = env.actorService.createRole(RoleKey("role"), name = "Role X", description = null)
        env.actorService.addRolePermission(RoleRef.ById(roleCustomId), ActorPermission(specialPermissionKey))
        env.actorService.actorAddRole(created.id, RoleRef.ById(roleCustomId))

        // Create a new auto-assign role
        val roleAutoNextId = env.actorService.createRole(RoleKey("role_auto"), name = "Role Auto", description = null)
        env.actorService.roleUpdateAutoAssign(RoleRef.ById(roleAutoNextId), true)

        env.authClockTests.staticNow = updatedTime
        val updated = env.actorService.syncFromJwtExternalPrincipal(
            createJwtExternalPrincipal(
                issuer = "issuer-x",
                subject = "subject-x",
                name = "Updated Name",
                email = "updated@example.com"
            )
        )

        assertEquals(created.id, updated.id)
        assertEquals("Updated Name", updated.fullname)
        assertEquals("updated@example.com", updated.email)
        assertEquals(setOf(ActorPermission(specialPermissionKey)), updated.permissions)
        assertEquals(updatedTime, updated.lastSeenAt)

        // Actor still has its custom role but not the new auto assign role.
        assertTrue(env.actorService.actorHasRole(updated.id, roleCustomId))
        assertFalse(env.actorService.actorHasRole(updated.id, roleAutoNextId))
    }

}
