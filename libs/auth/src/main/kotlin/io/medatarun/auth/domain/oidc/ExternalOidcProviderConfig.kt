package io.medatarun.auth.domain.oidc

/**
 * External OIDC provider configuration used to validate JWTs.
 */
data class ExternalOidcProviderConfig(
    val name: String,
    val issuer: String,
    val jwksUri: String,
    val audiences: List<String>,
    val allowedAlgs: List<String>
)
