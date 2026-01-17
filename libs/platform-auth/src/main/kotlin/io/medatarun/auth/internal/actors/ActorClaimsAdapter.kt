package io.medatarun.auth.internal.actors

import io.medatarun.auth.domain.actor.Actor

/**
 * Utility class to build claims for an [Actor].
 *
 * This makes sure that claims are consistent between OIDC token_id and access_tokens
 * as well as OAuth tokens.
 */
class ActorClaimsAdapter {

    fun createUserClaims(actor: Actor): Map<String, Any?> = mapOf(
        "name" to actor.fullname,
        "email" to actor.email,
        "roles" to actor.roles,
        "mid" to actor.id.value.toString()
    )

}
