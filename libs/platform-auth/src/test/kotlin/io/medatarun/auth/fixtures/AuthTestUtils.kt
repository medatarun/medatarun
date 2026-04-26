package io.medatarun.auth.fixtures

import io.medatarun.auth.ports.exposed.AuthJwtExternalPrincipal
import java.time.Instant

object AuthTestUtils {
    fun createJwtExternalPrincipal(
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