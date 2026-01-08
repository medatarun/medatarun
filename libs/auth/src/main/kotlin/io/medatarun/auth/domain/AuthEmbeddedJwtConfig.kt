package io.medatarun.auth.domain

data class AuthEmbeddedJwtConfig(
    val issuer: String,
    val audience: String,
    val ttlSeconds: Long
)