package io.medatarun.auth.ports.exposed

import java.time.Instant

/**
 * JWT data coming from an external source (HTTP headers, OAuth clients, etc.).
 * This type is intentionally minimal so that actors remain the source of truth.
 */
interface AuthJwtExternalPrincipal {
    val issuer: String
    val subject: String
    val issuedAt: Instant?
    val expiresAt: Instant?
    val audience: List<String>
    val roles: List<String>

    // Strings that may contain user (actor) display name
    val name: String?
    val fullname: String?
    val preferredUsername: String?
    val email: String?
}
