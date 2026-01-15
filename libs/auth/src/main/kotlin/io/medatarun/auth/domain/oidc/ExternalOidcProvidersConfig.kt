package io.medatarun.auth.domain.oidc

import java.time.Duration

/**
 * Aggregates external OIDC providers and JWKS cache configuration.
 */
data class ExternalOidcProvidersConfig(
    val providers: List<JwtIssuerConfig>,
    val cacheDurationSeconds: Long
) {
    fun getCacheDuration(): Duration {
        return Duration.ofSeconds(cacheDurationSeconds)
    }

    companion object {
        const val DEFAULT_CACHE_DURATION_SECONDS: Long = 600

        fun empty(): ExternalOidcProvidersConfig {
            return ExternalOidcProvidersConfig(emptyList(), DEFAULT_CACHE_DURATION_SECONDS)
        }
    }
}
