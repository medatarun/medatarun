package io.medatarun.auth

import io.medatarun.auth.adapters.AppActorIdAdapter
import io.medatarun.auth.domain.ActorPermission
import io.medatarun.auth.domain.role.RoleKey
import io.medatarun.auth.domain.role.RoleRef
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.ports.exposed.AuthJwtExternalPrincipal
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.security.AppActorSystemMaintenance
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@EnableDatabaseTests
class ActorServiceTest {
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


    @Test
    fun `listActors returns all known actors`() {
        val env = AuthEnvTest(
            createAdmin = false,
            otherPermissions = setOf(AuthEnvTest.TestOtherPermission("role-b"))
        )
        val actorA = env.actorService.create(
            issuer = "issuer-a",
            subject = "subject-a",
            fullname = "Alice Example",
            email = null,
            disabled = null
        )
        val actorB = env.actorService.create(
            issuer = "issuer-b",
            subject = "subject-b",
            fullname = "Bob Example",
            email = "bob@example.com",
            disabled = null
        )

        val allActors = env.actorService.listActors()
        assertEquals(3, allActors.size)
        assertTrue(allActors.any { it.id == actorA.id })
        assertTrue(allActors.any { it.id == actorB.id })
        assertTrue(allActors.any { it.id == AppActorIdAdapter.fromAppActorId(AppActorSystemMaintenance.id) })
    }

    @Test
    fun `sync creates actor with display name precedence`() {
        val env = AuthEnvTest(createAdmin = false)
        // Display name selection falls back through name/fullname/preferredUsername/email/subject.
        val createdWithName = env.actorService.syncFromJwtExternalPrincipal(
            createPrincipal(issuer = "issuer-1", subject = "sub-1", name = "Name Value")
        )
        assertEquals("Name Value", createdWithName.fullname)

        val createdWithFullname = env.actorService.syncFromJwtExternalPrincipal(
            createPrincipal(issuer = "issuer-2", subject = "sub-2", fullname = "Fullname Value")
        )
        assertEquals("Fullname Value", createdWithFullname.fullname)

        val createdWithPreferred = env.actorService.syncFromJwtExternalPrincipal(
            createPrincipal(issuer = "issuer-3", subject = "sub-3", preferredUsername = "Preferred Value")
        )
        assertEquals("Preferred Value", createdWithPreferred.fullname)

        val createdWithEmail = env.actorService.syncFromJwtExternalPrincipal(
            createPrincipal(issuer = "issuer-4", subject = "sub-4", email = "mail@example.com")
        )
        assertEquals("mail@example.com", createdWithEmail.fullname)

        val createdWithSubject = env.actorService.syncFromJwtExternalPrincipal(
            createPrincipal(issuer = "issuer-5", subject = "sub-5")
        )
        assertEquals("sub-5", createdWithSubject.fullname)
    }

    @Test
    fun `sync updates existing actor profile`() {
        val specialPermissionKey = "role-x"
        val env = AuthEnvTest(
            createAdmin = false,
            otherPermissions = setOf(AuthEnvTest.TestOtherPermission(specialPermissionKey))
        )
        val initialTime = Instant.parse("2024-02-01T00:00:00Z")
        val updatedTime = Instant.parse("2024-02-02T00:00:00Z")
        env.authClockTests.staticNow = initialTime

        val created = env.actorService.create(
            issuer = "issuer-x",
            subject = "subject-x",
            fullname = "Initial Name",
            email = "initial@example.com",
            disabled = null
        )

        val roleId = env.actorService.createRole(RoleKey("role"), name ="Role X", description = null)
        env.actorService.addRolePermission(RoleRef.ById(roleId), ActorPermission(specialPermissionKey))
        env.actorService.actorAddRole(created.id, RoleRef.ById(roleId))

        env.authClockTests.staticNow = updatedTime
        val updated = env.actorService.syncFromJwtExternalPrincipal(
            createPrincipal(
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
    }


    @Test
    fun `disable and enable actor`() {
        val env = AuthEnvTest(createAdmin = false)
        val actor = env.actorService.create(
            issuer = "issuer-disable",
            subject = "subject-disable",
            fullname = "Disabled Actor",
            email = null,
            disabled = null
        )

        val disabledAt = Instant.parse("2024-03-01T10:00:00Z")
        env.actorService.actorDisable(actor.id, disabledAt)
        val disabled = env.actorService.findByIssuerAndSubjectOptional("issuer-disable", "subject-disable")
        assertNotNull(disabled)
        assertEquals(disabledAt, disabled.disabledDate)

        env.actorService.actorDisable(actor.id, null)
        val enabled = env.actorService.findByIssuerAndSubjectOptional("issuer-disable", "subject-disable")
        assertNotNull(enabled)
        assertNull(enabled.disabledDate)
    }

    @Test
    fun `updateFullname keeps email and lastSeenAt`() {
        val env = AuthEnvTest(createAdmin = false)
        val initialTime = Instant.parse("2024-04-01T09:00:00Z")
        val laterTime = Instant.parse("2024-04-02T09:00:00Z")
        env.authClockTests.staticNow = initialTime

        val actor = env.actorService.create(
            issuer = "issuer-name",
            subject = "subject-name",
            fullname = "Initial Name",
            email = "initial@example.com",
            disabled = null
        )

        env.authClockTests.staticNow = laterTime
        env.actorService.updateFullname(actor.id, "Updated Name")
        val updated = env.actorService.findByIssuerAndSubjectOptional("issuer-name", "subject-name")
        assertNotNull(updated)
        assertEquals("Updated Name", updated.fullname)
        assertEquals("initial@example.com", updated.email)
        assertEquals(actor.lastSeenAt, updated.lastSeenAt)
    }

    private fun createPrincipal(
        issuer: String,
        subject: String,
        name: String? = null,
        fullname: String? = null,
        preferredUsername: String? = null,
        email: String? = null
    ): AuthJwtExternalPrincipal {
        return object : AuthJwtExternalPrincipal {
            override val issuer: String = issuer
            override val subject: String = subject
            override val issuedAt: Instant? = null
            override val expiresAt: Instant? = null
            override val audience: List<String> = emptyList()
            override val name: String? = name
            override val fullname: String? = fullname
            override val preferredUsername: String? = preferredUsername
            override val email: String? = email
        }
    }
}
