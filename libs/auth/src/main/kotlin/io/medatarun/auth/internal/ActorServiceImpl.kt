package io.medatarun.auth.internal

import io.medatarun.auth.domain.ActorCreateFailedWithNotFoundException
import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.actor.ActorId
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.exposed.AuthJwtExternalPrincipal
import io.medatarun.auth.ports.needs.ActorStorage
import io.medatarun.auth.ports.needs.AuthClock
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

class ActorServiceImpl(
    private val actorStorage: ActorStorage,
    private val clock: AuthClock
) : ActorService {

    override fun syncFromJwtExternalPrincipal(principal: AuthJwtExternalPrincipal): Actor {
        val existing = actorStorage.findByIssuerAndSubjectOptional(principal.issuer, principal.subject)

        val actor = if (existing == null) {
            val created = create(
                issuer = principal.issuer,
                subject = principal.subject,
                fullname = actorDisplayName(principal),
                email = principal.email,
                roles = emptyList(),
                disabled = null
            )
            logger.info("Registered actor {} from issuer {}", created.id, created.issuer)
            created
        } else {
            updateActorProfile(existing, principal)
        }

        return actor
    }

    fun updateActorProfile(
        existing: Actor,
        principal: AuthJwtExternalPrincipal
    ): Actor {
        actorStorage.updateProfile(
            id = existing.id,
            fullname = actorDisplayName(principal),
            email = principal.email,
            lastSeenAt = clock.now()
        )
        return actorStorage.findById(existing.id)
    }

    override fun updateFullname(actorId: ActorId, fullname: String) {
        val actor = actorStorage.findById(actorId)

        actorStorage.updateProfile(actor.id, fullname, email = actor.email, lastSeenAt = actor.lastSeenAt)
    }

    override fun create(
        issuer: String,
        subject: String,
        fullname: String,
        email: String?,
        roles: List<ActorRole>,
        disabled: Instant?
    ): Actor {
        val id = ActorId(UUID.randomUUID())
        actorStorage.insert(
            id = id,
            issuer = issuer,
            subject = subject,
            fullname = fullname,
            email = email,
            roles = roles,
            createdAt = clock.now(),
            lastSeenAt = clock.now()
        )
        return actorStorage.findByIdOptional(id) ?: throw ActorCreateFailedWithNotFoundException()
    }


    override fun listActors(): List<Actor> {
        return actorStorage.listAll()
    }

    override fun findByIssuerAndSubjectOptional(issuer: String, subject: String): Actor? {
        return actorStorage.findByIssuerAndSubjectOptional(issuer, subject)
    }

    override fun setRoles(actorId: ActorId, roles: List<ActorRole>) {
        val existing = actorStorage.findById(actorId)
        actorStorage.updateRoles(existing.id, roles)
    }

    override fun disable(actorId: ActorId, at: Instant?) {
        val existing = actorStorage.findById(actorId)
        if (at == null) actorStorage.enable(existing.id)
        else actorStorage.disable(existing.id, at)
    }

    private fun actorDisplayName(principal: AuthJwtExternalPrincipal): String {
        val name = principal.name
        if (!name.isNullOrBlank()) {
            return name
        }

        val fullname = principal.fullname
        if (!fullname.isNullOrBlank()) {
            return fullname
        }

        val preferredUsername = principal.preferredUsername
        if (!preferredUsername.isNullOrBlank()) {
            return preferredUsername
        }

        val email = principal.email
        if (!email.isNullOrBlank()) {
            return email
        }
        return principal.subject
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ActorServiceImpl::class.java)
    }
}
