package io.medatarun.auth

import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.AuthUnknownRoleException
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.ports.exposed.AuthJwtExternalPrincipal
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ActorServiceTest {
    @Test
    fun `create stores actor and finds it by issuer and subject`() {
        val env = AuthEnvTest(createAdmin = false)
        val now = Instant.parse("2024-01-03T12:00:00Z")
        env.authClock.staticNow = now

        val actor = env.actorService.create(
            issuer = "issuer-a",
            subject = "subject-a",
            fullname = "Alice Example",
            email = "alice@example.com",
            roles = listOf(ActorRole.ADMIN),
            disabled = null
        )

        assertEquals("issuer-a", actor.issuer)
        assertEquals("subject-a", actor.subject)
        assertEquals("Alice Example", actor.fullname)
        assertEquals("alice@example.com", actor.email)
        assertEquals(listOf(ActorRole.ADMIN), actor.roles)
        assertEquals(now, actor.createdAt)
        assertEquals(now, actor.lastSeenAt)

        val found = env.actorService.findByIssuerAndSubjectOptional("issuer-a", "subject-a")
        assertNotNull(found)
        assertEquals(actor.id, found.id)
        assertNull(env.actorService.findByIssuerAndSubjectOptional("issuer-a", "missing"))
    }

    @Test
    fun `listActors returns all known actors`() {
        val env = AuthEnvTest(createAdmin = false)
        val actorA = env.actorService.create(
            issuer = "issuer-a",
            subject = "subject-a",
            fullname = "Alice Example",
            email = null,
            roles = emptyList(),
            disabled = null
        )
        val actorB = env.actorService.create(
            issuer = "issuer-b",
            subject = "subject-b",
            fullname = "Bob Example",
            email = "bob@example.com",
            roles = listOf(ActorRole("role-b")),
            disabled = null
        )

        val allActors = env.actorService.listActors()
        assertEquals(2, allActors.size)
        assertTrue(allActors.any { it.id == actorA.id })
        assertTrue(allActors.any { it.id == actorB.id })
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
        val env = AuthEnvTest(createAdmin = false)
        val initialTime = Instant.parse("2024-02-01T00:00:00Z")
        val updatedTime = Instant.parse("2024-02-02T00:00:00Z")
        env.authClock.staticNow = initialTime

        val created = env.actorService.create(
            issuer = "issuer-x",
            subject = "subject-x",
            fullname = "Initial Name",
            email = "initial@example.com",
            roles = listOf(ActorRole("role-x")),
            disabled = null
        )

        env.authClock.staticNow = updatedTime
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
        assertEquals(listOf(ActorRole("role-x")), updated.roles)
        assertEquals(updatedTime, updated.lastSeenAt)
    }

    @Test
    fun `setRoles updates roles and validates known roles`() {
        val env = AuthEnvTest(createAdmin = false, otherRoles = setOf("role-x"))
        val actor = env.actorService.create(
            issuer = "issuer-roles",
            subject = "subject-roles",
            fullname = "Role Holder",
            email = null,
            roles = emptyList(),
            disabled = null
        )

        env.actorService.setRoles(actor.id, listOf(ActorRole("role-x")))
        val updated = env.actorService.findByIssuerAndSubjectOptional("issuer-roles", "subject-roles")
        assertNotNull(updated)
        assertEquals(listOf(ActorRole("role-x")), updated.roles)

        assertThrows<AuthUnknownRoleException> {
            env.actorService.setRoles(actor.id, listOf(ActorRole("unknown-role")))
        }
    }

    @Test
    fun `disable and enable actor`() {
        val env = AuthEnvTest(createAdmin = false)
        val actor = env.actorService.create(
            issuer = "issuer-disable",
            subject = "subject-disable",
            fullname = "Disabled Actor",
            email = null,
            roles = emptyList(),
            disabled = null
        )

        val disabledAt = Instant.parse("2024-03-01T10:00:00Z")
        env.actorService.disable(actor.id, disabledAt)
        val disabled = env.actorService.findByIssuerAndSubjectOptional("issuer-disable", "subject-disable")
        assertNotNull(disabled)
        assertEquals(disabledAt, disabled.disabledDate)

        env.actorService.disable(actor.id, null)
        val enabled = env.actorService.findByIssuerAndSubjectOptional("issuer-disable", "subject-disable")
        assertNotNull(enabled)
        assertNull(enabled.disabledDate)
    }

    @Test
    fun `updateFullname keeps email and lastSeenAt`() {
        val env = AuthEnvTest(createAdmin = false)
        val initialTime = Instant.parse("2024-04-01T09:00:00Z")
        val laterTime = Instant.parse("2024-04-02T09:00:00Z")
        env.authClock.staticNow = initialTime

        val actor = env.actorService.create(
            issuer = "issuer-name",
            subject = "subject-name",
            fullname = "Initial Name",
            email = "initial@example.com",
            roles = emptyList(),
            disabled = null
        )

        env.authClock.staticNow = laterTime
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
            override val roles: List<String> = emptyList()
            override val name: String? = name
            override val fullname: String? = fullname
            override val preferredUsername: String? = preferredUsername
            override val email: String? = email
        }
    }
}
