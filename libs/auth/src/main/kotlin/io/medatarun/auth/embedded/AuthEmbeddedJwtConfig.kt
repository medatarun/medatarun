package io.medatarun.auth.embedded

data class AuthEmbeddedJwtConfig(
    val issuer: String = "urn:medatarun",
    val audience: String = "medatarun",
    val ttlSeconds: Long = 3600
)