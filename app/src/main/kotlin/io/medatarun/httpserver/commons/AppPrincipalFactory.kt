package io.medatarun.httpserver.commons

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.medatarun.actions.ports.needs.AppPrincipal
import io.medatarun.actions.ports.needs.AppPrincipalId
import io.medatarun.actions.ports.needs.AppPrincipalRole
import io.medatarun.auth.domain.Actor
import io.medatarun.auth.domain.ActorDisabledException
import io.medatarun.auth.domain.ActorRole
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.exposed.AuthJwtExternalPrincipal
import java.time.Instant

/**
 * Extracts an [AppPrincipal] from an [ApplicationCall]
 *
 * Use this at the entrance of the serveur routes where you need to
 * extract the principal from a validated JWT.
 *
 * If there is no principal on the system given this validated JWT
 * it will be created.
 *
 * If it exists, we extract some infos and update its profile from
 * the newest info.
 */
class AppPrincipalFactory(
    private val actorService: ActorService
) {

    fun getAndSync(call: ApplicationCall): AppPrincipal? {
        val externalPrincipal = toAuthJwtExternalPrincipal(call) ?: return null
        val actor = actorService.syncFromJwtExternalPrincipal(externalPrincipal)
        if (actor.disabledDate != null) {
            throw ActorDisabledException()
        }
        return toAppPrincipal(actor)
    }

    private fun toAuthJwtExternalPrincipal(call: ApplicationCall): AuthJwtExternalPrincipal? {
        val principal = call.authentication.principal<JWTPrincipal>() ?: return null
        return AuthJwtExternalPrincipalImpl(principal)
    }

    class AuthJwtExternalPrincipalImpl(principal: JWTPrincipal) : AuthJwtExternalPrincipal {
        val principalIssuer = principal.issuer ?: throw JwtInvalidTokenException()
        val principalSubject = principal.subject ?: throw JwtInvalidTokenException()
        val principalRoles = emptyList<String>()
        val principalClaims = principal.payload.claims?.map { it.key to it.value?.asString() }?.toMap() ?: emptyMap()
        override val issuer: String = principalIssuer
        override val subject: String = principalSubject
        override val issuedAt: Instant? = principal.issuedAt?.toInstant()
        override val expiresAt: Instant? = principal.expiresAt?.toInstant()
        override val audience: List<String> = principal.audience
        override val roles: List<String> = principalRoles
        override val name: String? = principalClaims["name"]
        override val fullname: String? = principalClaims["fullname"]
        override val preferredUsername: String? = principalClaims["preferred_username"]
        override val email: String? = principalClaims["email"]
    }


    private fun toAppPrincipal(actor: Actor): AppPrincipal {
        return object : AppPrincipal {
            override val id: AppPrincipalId = AppPrincipalId(actor.id.value.toString())
            override val issuer: String = actor.issuer
            override val subject: String = actor.subject
            override val isAdmin: Boolean = actor.roles.any { it.isAdminRole() }
            override val roles: List<AppPrincipalRole> = actor.roles.map(::toMedatarunPrincipalRole)
            override val fullname: String = actor.fullname
        }
    }

    private fun toMedatarunPrincipalRole(role: ActorRole): AppPrincipalRole {
        return object : AppPrincipalRole {
            override val key = role.key
        }
    }
}
