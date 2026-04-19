package io.medatarun.auth.domain.jwt

import io.medatarun.platform.kernel.Service

data class JwtConfig(
    val issuer: String,
    val audience: String,
    val ttlSeconds: Long
): Service