package io.medatarun.auth.embedded

data class AuthEmbeddedJwtConfig(
    val issuer: String,
    val audience: String,
    val ttlSeconds: Long
)