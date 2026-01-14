package io.medatarun.auth.ports.exposed

import com.auth0.jwt.interfaces.JWTVerifier

/**
 * Resolves a JWT verifier based on the token content.
 * This keeps HTTP auth thin while centralizing verification rules.
 */
interface JwtVerifierResolver {
    fun resolve(token: String): JWTVerifier
}
