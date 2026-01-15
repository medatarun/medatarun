package io.medatarun.auth.domain.jwt

data class JwtConfig(
    val issuer: String,
    val audience: String,
    val ttlSeconds: Long
)