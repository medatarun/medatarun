package io.medatarun.auth.internal.jwk

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.medatarun.auth.domain.*
import io.medatarun.auth.domain.oidc.ExternalOidcProviderConfig
import io.medatarun.auth.domain.oidc.ExternalOidcProvidersConfig
import java.net.URI

interface JwkExternalProviders {
    fun findByIssuer(issuer: String): JwkProvider
    fun findExternalProvider(issuer: String): ExternalOidcProviderConfig
}

class JwtExternalProvidersEmpty : JwkExternalProviders {
    override fun findByIssuer(issuer: String): JwkProvider {
        throw JwtJwksUnknownExternalProvider(issuer)
    }

    override fun findExternalProvider(issuer: String): ExternalOidcProviderConfig {
        throw JwtJwksUnknownExternalProvider(issuer)
    }
}

class JwkExternalProvidersImpl(
    private val providersConfigs: ExternalOidcProvidersConfig,
) : JwkExternalProviders {
    private val providers: Map<String, JwkProvider> = buildJwkProviders(providersConfigs)


    override fun findByIssuer(issuer: String): JwkProvider {
        return providers[issuer]
            ?: throw JwtJwksUnknownExternalProvider(issuer)
    }

    override fun findExternalProvider(issuer: String): ExternalOidcProviderConfig {
        val provider = providersConfigs.providers.firstOrNull { it.issuer == issuer }
        if (provider == null) {
            throw JwtUnknownIssuerException(issuer)
        }
        return provider
    }

    private fun buildJwkProviders(
        externalOidcProviders: ExternalOidcProvidersConfig
    ): Map<String, JwkProvider> {
        val providers = mutableMapOf<String, JwkProvider>()
        val cacheDuration = externalOidcProviders.getCacheDuration()
        for (provider in externalOidcProviders.providers) {
            val uri = URI.create(provider.jwksUri).toURL()
            val jwkProvider = JwkProviderBuilder(uri)
                .cached(10, cacheDuration)
                .build()
            providers[provider.issuer] = jwkProvider
        }
        return providers
    }

    companion object {

        interface ConfigResolver {
            fun getConfigProperty(key: String, defaultValue: String): String
            fun getConfigProperty(key: String): String?
        }

        fun createJwtExternalProvidersFromConfigProperties(
            ctx: ConfigResolver,
            internalIssuer: String
        ): JwkExternalProviders {

            // The list of providers is explicit to keep configuration simple and avoid parsing JSON.
            val rawNames = ctx.getConfigProperty("medatarun.auth.oidc.external.names")
                ?: return JwtExternalProvidersEmpty()

            val names = parseCsv(rawNames)
            if (names.isEmpty()) {
                return JwtExternalProvidersEmpty()
            }
            val durationRaw = ctx.getConfigProperty(
                "medatarun.auth.oidc.jwkscache.duration",
                ExternalOidcProvidersConfig.DEFAULT_CACHE_DURATION_SECONDS.toString()
            )
            val durationSeconds = durationRaw.toLongOrNull()
                ?: throw ExternalOidcProviderCacheDurationInvalidException(durationRaw)
            val providers = mutableListOf<ExternalOidcProviderConfig>()
            for (name in names) {
                val prefix = "medatarun.auth.oidc.external.$name"
                val issuer = ctx.getConfigProperty("$prefix.issuer")
                    ?: throw ExternalOidcProviderMissingConfigException(name, "issuer")
                if (issuer == internalIssuer) {
                    throw ExternalOidcProviderIssuerConflictException(issuer)
                }
                val jwksUri = ctx.getConfigProperty("$prefix.jwksUri")
                    ?: throw ExternalOidcProviderMissingConfigException(name, "jwksUri")
                val audienceRaw = ctx.getConfigProperty("$prefix.audience")
                val audiences = parseCsv(audienceRaw ?: "")
                val algsRaw = ctx.getConfigProperty("$prefix.algs", "RS256")
                val algs = parseCsv(algsRaw)
                val allowedAlgs = if (algs.isEmpty()) listOf(JwtSupportedAlgorithm.RS256) else algs.map {
                    JwtSupportedAlgorithm.valueOfKey(it)
                }
                providers.add(
                    ExternalOidcProviderConfig(
                        name = name,
                        issuer = issuer,
                        jwksUri = jwksUri,
                        audiences = audiences,
                        allowedAlgs = allowedAlgs
                    )
                )
            }
            return JwkExternalProvidersImpl(ExternalOidcProvidersConfig(providers, durationSeconds))
        }

        private fun parseCsv(value: String): List<String> {
            if (value.isBlank()) {
                return emptyList()
            }
            val parts = value.split(",")
            val trimmed = mutableListOf<String>()
            for (part in parts) {
                val token = part.trim()
                if (token.isNotEmpty()) {
                    trimmed.add(token)
                }
            }
            return trimmed
        }
    }
}