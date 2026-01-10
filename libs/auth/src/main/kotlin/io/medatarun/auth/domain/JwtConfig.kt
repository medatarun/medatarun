package io.medatarun.auth.domain

data class JwtConfig(
    val issuer: String,
    val audience: String,
    val ttlSeconds: Long
)