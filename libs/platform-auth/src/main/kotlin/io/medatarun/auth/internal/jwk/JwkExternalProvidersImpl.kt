package io.medatarun.auth.internal.jwk

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import io.medatarun.auth.domain.*
import io.medatarun.auth.domain.oidc.ExternalOidcProvidersConfig
import io.medatarun.auth.domain.oidc.JwtIssuerConfig
import java.net.URI

interface JwkExternalProviders {
    fun findByIssuer(issuer: String): JwkProvider
    fun findExternalProvider(issuer: String): JwtIssuerConfig
    fun firstOrNull(): JwtIssuerConfig?
}

class JwtExternalProvidersEmpty : JwkExternalProviders {
    override fun findByIssuer(issuer: String): JwkProvider {
        throw JwtJwksUnknownExternalProvider(issuer)
    }

    override fun findExternalProvider(issuer: String): JwtIssuerConfig {
        throw JwtJwksUnknownExternalProvider(issuer)
    }

    override fun firstOrNull(): JwtIssuerConfig? {
        return null
    }
}

class JwkExternalProvidersImpl(
    private val providersConfigs: ExternalOidcProvidersConfig,
) : JwkExternalProviders {
    private val providers: Map<String, JwkProvider> = buildJwkProviders(providersConfigs)

    override fun firstOrNull(): JwtIssuerConfig? {
        return providersConfigs.providers.firstOrNull()
    }

    override fun findByIssuer(issuer: String): JwkProvider {
        return providers[issuer]
            ?: throw JwtJwksUnknownExternalProvider(issuer)
    }

    override fun findExternalProvider(issuer: String): JwtIssuerConfig {
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
            val config = createConfig(ctx, internalIssuer)
            return if (config.providers.isEmpty()) {
                JwtExternalProvidersEmpty()
            } else JwkExternalProvidersImpl(config)
        }

        fun createConfig(
            ctx: ConfigResolver,
            internalIssuer: String
        ): ExternalOidcProvidersConfig {

            // The list of providers is explicit to keep configuration simple and avoid parsing JSON.
            val rawNames = ctx.getConfigProperty(ConfigProperties.Issuers.key)
                ?: return ExternalOidcProvidersConfig.empty()

            val names = parseCsv(rawNames)
            if (names.isEmpty()) {
                return ExternalOidcProvidersConfig.empty()
            }
            val durationRaw = ctx.getConfigProperty(
                ConfigProperties.JwksCacheDuration.key,
                ExternalOidcProvidersConfig.DEFAULT_CACHE_DURATION_SECONDS.toString()
            )
            val durationSeconds = durationRaw.toLongOrNull()
                ?: throw ExternalOidcProviderCacheDurationInvalidException(durationRaw)
            val issuers = mutableListOf<JwtIssuerConfig>()
            for (name in names) {

                val issuer = ctx.getConfigProperty(ConfigProperties.IssuerIssuer.withName(name))
                    ?: throw ExternalOidcProviderMissingConfigException(name, "issuer")
                if (issuer == internalIssuer) {
                    throw ExternalOidcProviderIssuerConflictException(issuer)
                }
                val jwksUri = ctx.getConfigProperty(ConfigProperties.IssuerJWKS.withName(name))
                    ?: throw ExternalOidcProviderMissingConfigException(name, "jwksUri")
                val audienceRaw = ctx.getConfigProperty(ConfigProperties.IssuerAudiences.withName(name))
                val audiences = parseCsv(audienceRaw ?: "")
                val algsRaw = ctx.getConfigProperty(ConfigProperties.IssuerAlgorithms.withName(name), "RS256")
                val algs = parseCsv(algsRaw)
                val allowedAlgs = if (algs.isEmpty()) listOf(JwtSupportedAlgorithm.RS256) else algs.map {
                    JwtSupportedAlgorithm.valueOfKey(it)
                }
                issuers.add(
                    JwtIssuerConfig(
                        name = name,
                        issuer = issuer,
                        jwksUri = jwksUri,
                        audiences = audiences,
                        allowedAlgs = allowedAlgs
                    )
                )
            }

            val config = ExternalOidcProvidersConfig(issuers, durationSeconds)
            return config

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