package io.medatarun.actions.ports.needs

import java.time.Instant

interface MedatarunPrincipal {
    val sub: String
    val issuer: String
    val isAdmin: Boolean
    val issuedAt: Instant?
    val expiresAt: Instant?
    val audience: List<String>
    val claims: Map<String, String?>
}
