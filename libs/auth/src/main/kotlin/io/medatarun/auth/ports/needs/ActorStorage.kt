package io.medatarun.auth.ports.needs

import io.medatarun.auth.domain.ActorNotFoundException
import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.actor.ActorId
import java.time.Instant

/**
 * Interface used to provide storage of [Actor]
 */
interface ActorStorage {

    /**
     * Inserts a new actor.
     *
     * Should raise an error (whatever error) if another actor exists with the same [id],
     * or the same couple ([issuer], [subject]).
     */
    fun insert(
        id: ActorId,
        issuer: String,
        subject: String,
        fullname: String,
        email: String?,
        roles: List<ActorRole>,
        disabled: Instant?,
        createdAt: Instant,
        lastSeenAt: Instant
    )

    /**
     * Updates an actor's profile information.
     */
    fun updateProfile(
        id: ActorId,
        fullname: String,
        email: String?,
        lastSeenAt: Instant
    )

    /**
     * Replaces roles for a given actor.
     */
    fun updateRoles(id: ActorId, roles: List<ActorRole>)

    /**
     * Marks an actor disabled with this date.
     */
    fun disable(id: ActorId, at: Instant)

    /**
     * Unmark "disabled" for an actor, making it enabled.
     */
    fun enable(id: ActorId)

    /**
     * Find an actor by [issuer] and [subject] (unique key).
     *
     * Returns null if not found
     */
    fun findByIssuerAndSubjectOptional(issuer: String, subject: String): Actor?

    /**
     * Find an actor by its unique identifier [id]
     *
     * Returns null if not found
     */
    fun findByIdOptional(id: ActorId): Actor?

    /**
     * Find an actor by its unique identifier [id].
     *
     * Throws an [ActorNotFoundException] if not found
     */
    fun findById(id: ActorId): Actor = findByIdOptional(id) ?: throw ActorNotFoundException()

    /**
     * List all known actors
     */
    fun listAll(): List<Actor>
}
