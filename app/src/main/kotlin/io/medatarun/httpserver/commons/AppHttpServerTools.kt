package io.medatarun.httpserver.commons

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.medatarun.actions.ports.needs.MedatarunPrincipal
import java.time.Instant
import java.util.*

object AppHttpServerTools {
    fun detectLocale(call: ApplicationCall): Locale {
        val header = call.request.headers["Accept-Language"]
        val firstTag = header
            ?.split(",")
            ?.map { it.substringBefore(";").trim() }
            ?.firstOrNull { it.isNotEmpty() }

        return firstTag?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
    }

    fun toMedatarunPrincipal(call: ApplicationCall): MedatarunPrincipal? {
        val principal = call.authentication.principal<JWTPrincipal>() ?: return null
        val principalIssuer = principal.issuer ?: return null
        val principalSubject = principal.subject ?: return null
        val principalAdmin = principal.getClaim("role", String::class) == "admin"
        return object : MedatarunPrincipal {
            override val sub: String = principalSubject
            override val issuer: String = principalIssuer
            override val isAdmin: Boolean = principalAdmin
            override val issuedAt: Instant? = principal.issuedAt?.toInstant()
            override val expiresAt: Instant? = principal.expiresAt?.toInstant()
            override val audience: List<String> = principal.audience
            override val claims =
                principal.payload.claims?.map { it.key to it.value?.asString() }?.toMap() ?: emptyMap()

        }
    }

}