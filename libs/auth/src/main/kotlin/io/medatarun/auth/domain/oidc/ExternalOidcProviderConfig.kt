package io.medatarun.auth.domain.oidc

import io.medatarun.auth.internal.jwk.JwtSupportedAlgorithm

/**
 * External OIDC provider configuration used to validate JWTs.
 */
data class ExternalOidcProviderConfig(
    val name: String,
    /**
     * The issuer (iss) claim expected in JWTs for this provider.
     */
    val issuer: String,
    /**
     * The provider JWKS URI used to resolve signing keys by kid.
     */
    val jwksUri: String,
    /**
     * Accepted audiences. When empty, audience checks are skipped for this provider.
     * When non-empty, a token is valid if its aud claim contains at least one of these values.
     */
    val audiences: List<String>,
    /**
     * Allowed JWT algorithms for this provider (e.g. RS256).
     */
    val allowedAlgs: List<JwtSupportedAlgorithm>
)
