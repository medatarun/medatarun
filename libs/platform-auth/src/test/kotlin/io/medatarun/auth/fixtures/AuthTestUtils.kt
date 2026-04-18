package io.medatarun.auth.fixtures

import io.medatarun.auth.ports.exposed.AuthJwtExternalPrincipal
import java.time.Instant

object AuthTestUtils {
    fun createActorJwt(
        issuer: String,
        subject: String,
        name: String? = null,
        email: String? = null
    ): AuthJwtExternalPrincipal {
        return object : AuthJwtExternalPrincipal {
            override val issuer: String = issuer
            override val subject: String = subject
            override val issuedAt: Instant? = null
            override val expiresAt: Instant? = null
            override val audience: List<String> = emptyList()
            override val name: String? = name
            override val fullname: String? = null
            override val preferredUsername: String? = null
            override val email: String? = email
        }
    }

}