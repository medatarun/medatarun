package io.medatarun.auth.ports.exposed

import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.actor.ActorId
import java.time.Instant

/**
 * Service to manage actors in our system.
 *
 * An [Actor] is either a physical person (synonym to "user" or "user account")
 * or a tool (synonym of "service", "tool" or "service account").
 *
 * Roles in our system are declared on [Actor] (not users).
 */
interface ActorService {
    /**
     * When actors appear via a validated Jwt (API, MCP, UI, etc.) it means
     * there is a trusted relationship with an external IdP.
     *
     * Actor may or not exist in our system. We need to create or update the actor in our system
     * based on what we found in the Jwt.
     *
     * When the actor needs to be created, we extract the issuer, subject, fullname, email.
     * Roles are defined internally by our role policy.
     *
     * When the actor exists, we extract the fullname and email and update it (because they may
     * have changed in external IdP.)
     */
    fun syncFromJwtExternalPrincipal(principal: AuthJwtExternalPrincipal): Actor

    /**
     * Finds an actor based on its issuer and subject
     */
    fun findByIssuerAndSubjectOptional(issuer: String, subject: String): Actor?

    /**
     * Finds an actor based on its unique identifier
     */
    fun findById(actorId: ActorId): Actor

    /**
     * Lists all actors currently known
     */
    fun listActors(): List<Actor>

    /**
     * Update roles for an actor
     */
    fun setRoles(actorId: ActorId, roles: List<ActorRole>)

    /**
     * Disable or enable an actor. A null [at] is "enabled". When a [at] is provided then
     * actor is disabled [at] this time
     */
    fun disable(actorId: ActorId, at: Instant?)

    /**
     * Updates an actor fullname
     */
    fun updateFullname(actorId: ActorId, fullname: String)

    /**
     * Creates a new actor. This shall be used in only two cases:
     *
     * - When our internal IdP creates a new user to make it immediately available as [Actor].
     * - When we need batch operations to prefill [Actor] repository or to resync systems
     */
    fun create(
        issuer: String,
        subject: String,
        fullname: String,
        email: String?,
        roles: List<ActorRole>,
        disabled: Instant?
    ): Actor

}
