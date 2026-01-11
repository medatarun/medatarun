package io.medatarun.auth.domain

import java.time.Instant

/**
 * An [Actor] represents a physical user or a "service account" or a machine
 * that can act or the application.
 *
 * Actors are uniquely defined by their [id] which is persistent in the system,
 * stable and unique.
 *
 * Actors may come from identity providers: our own provider and maybe others.
 * The [issuer] tells the identity provider used and the [subject] represents
 * the "login" or "identity" or maybe "email" of the actor in this [issuer].
 *
 * So, the couple [issuer] + [subject] is unique.
 *
 * **NEVER** rely on [issuer]+[subject] in apps. The only guaranteed
 * identifier is [id]
 *
 *
 *
 */
data class Actor(
    /**
     * Unique identifier.
     */
    val id: ActorId,
    /**
     * Issuer of the actor
     */
    val issuer: String,
    /**
     * Subject of the actor in this issuer.
     */
    val subject: String,
    /**
     * Fullname of the actor. Be careful that it changes and sometimes we don't have it.
     * If we don't have it, we use the [subject] instead so you can always display something.
     */
    val fullname: String,
    /**
     * Email of the actor. Be careful that it changes and sometimes we don't have it.
     */
    val email: String?,
    /**
     * List of roles known and stored for this actor.
     *
     * Roles in Actor are the official source of roles (roles are not on User but on Actor).
     */
    val roles: List<ActorRole>,
    /**
     * Date this actor was disabled, if disabled. Otherwise null if actor is enabled.
     */
    val disabledDate: Instant?,
    /**
     * Instant actor was created
     */
    val createdAt: Instant,
    /**
     * Instant actor was last seen (exactly, last time he used tokens and acted on the system)
     */
    val lastSeenAt: Instant
)
