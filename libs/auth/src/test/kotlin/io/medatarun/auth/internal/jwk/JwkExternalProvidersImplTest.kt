package io.medatarun.auth.internal.jwk

import io.medatarun.auth.domain.*
import io.medatarun.auth.domain.oidc.JwtIssuerConfig
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JwkExternalProvidersImplTest {

    @Test
    fun `should build providers from raw config`() {
        val providers = JwkExternalProvidersImpl.createJwtExternalProvidersFromConfigProperties(
            MapConfigResolver(
                mapOf(
                    ConfigProperties.Issuers.key to "google, azure",
                    ConfigProperties.JwksCacheDuration.key to "120",
                    ConfigProperties.IssuerIssuer.withName("google") to "https://accounts.google.com",
                    ConfigProperties.IssuerJWKS.withName("google") to "https://www.googleapis.com/oauth2/v3/certs",
                    ConfigProperties.IssuerAudiences.withName("google") to "client-id-1, client-id-2",
                    ConfigProperties.IssuerAlgorithms.withName("google") to "RS256, ES256",
                    ConfigProperties.IssuerIssuer.withName("azure") to "https://login.microsoftonline.com/common/v2.0",
                    ConfigProperties.IssuerJWKS.withName("azure") to "https://login.microsoftonline.com/common/discovery/v2.0/keys",
                    ConfigProperties.IssuerAlgorithms.withName("azure") to ""
                )
            ),
            "medatarun"
        )

        val google = providers.findExternalProvider("https://accounts.google.com")
        assertEquals(
            JwtIssuerConfig(
                name = "google",
                issuer = "https://accounts.google.com",
                jwksUri = "https://www.googleapis.com/oauth2/v3/certs",
                audiences = listOf("client-id-1", "client-id-2"),
                allowedAlgs = listOf(JwtSupportedAlgorithm.RS256, JwtSupportedAlgorithm.ES256)
            ),
            google
        )

        val azure = providers.findExternalProvider("https://login.microsoftonline.com/common/v2.0")
        assertEquals(
            JwtIssuerConfig(
                name = "azure",
                issuer = "https://login.microsoftonline.com/common/v2.0",
                jwksUri = "https://login.microsoftonline.com/common/discovery/v2.0/keys",
                audiences = emptyList(),
                allowedAlgs = listOf(JwtSupportedAlgorithm.RS256)
            ),
            azure
        )

        val jwkProvider = providers.findByIssuer("https://accounts.google.com")
        assertNotNull(jwkProvider)
    }

    @Test
    fun `should return empty providers when names are missing`() {
        val providers = JwkExternalProvidersImpl.createJwtExternalProvidersFromConfigProperties(
            MapConfigResolver(emptyMap()),
            "medatarun"
        )

        assertTrue(providers is JwtExternalProvidersEmpty)
    }

    @Test
    fun `should return empty providers when names are blank`() {
        val providers = JwkExternalProvidersImpl.createJwtExternalProvidersFromConfigProperties(
            MapConfigResolver(
                mapOf(ConfigProperties.Issuers.key to " ,  , ")
            ),
            "medatarun"
        )

        assertTrue(providers is JwtExternalProvidersEmpty)
    }

    @Test
    fun `should use default cache duration when not configured`() {

        // We just check there are no errors, we don't have ways to check that the Provider
        // has correct cache duration

        val providers = JwkExternalProvidersImpl.createJwtExternalProvidersFromConfigProperties(
            MapConfigResolver(
                mapOf(
                    ConfigProperties.Issuers.key to "google",
                    ConfigProperties.IssuerIssuer.withName("google") to "https://accounts.google.com",
                    ConfigProperties.IssuerJWKS.withName("google") to "https://www.googleapis.com/oauth2/v3/certs"
                )
            ),
            "medatarun"
        )

        assertNotNull(providers.findByIssuer("https://accounts.google.com"))
    }

    @Test
    fun `should reject invalid cache duration`() {
        assertFailsWith<ExternalOidcProviderCacheDurationInvalidException> {
            JwkExternalProvidersImpl.createJwtExternalProvidersFromConfigProperties(
                MapConfigResolver(
                    mapOf(
                        ConfigProperties.Issuers.key to "google",
                        ConfigProperties.JwksCacheDuration.key to "not-a-number",
                        ConfigProperties.IssuerIssuer.withName("google") to "https://accounts.google.com",
                        ConfigProperties.IssuerJWKS.withName("google") to "https://www.googleapis.com/oauth2/v3/certs"
                    )
                ),
                "medatarun"
            )
        }
    }

    @Test
    fun `should reject external issuer matching internal issuer`() {
        assertFailsWith<ExternalOidcProviderIssuerConflictException> {
            JwkExternalProvidersImpl.createJwtExternalProvidersFromConfigProperties(
                MapConfigResolver(
                    mapOf(
                        ConfigProperties.Issuers.key to "internal",
                        ConfigProperties.IssuerIssuer.withName("internal") to "medatarun",
                        ConfigProperties.IssuerJWKS.withName("internal") to "https://issuer.example/jwks"
                    )
                ),
                "medatarun"
            )
        }
    }

    @Test
    fun `should reject missing issuer config`() {
        assertFailsWith<ExternalOidcProviderMissingConfigException> {
            JwkExternalProvidersImpl.createJwtExternalProvidersFromConfigProperties(
                MapConfigResolver(
                    mapOf(
                        ConfigProperties.Issuers.key to "google",
                        ConfigProperties.IssuerJWKS.withName("google") to "https://www.googleapis.com/oauth2/v3/certs"
                    )
                ),
                "medatarun"
            )
        }
    }

    @Test
    fun `should reject missing jwks uri config`() {
        assertFailsWith<ExternalOidcProviderMissingConfigException> {
            JwkExternalProvidersImpl.createJwtExternalProvidersFromConfigProperties(
                MapConfigResolver(
                    mapOf(
                        ConfigProperties.Issuers.key to "google",
                        ConfigProperties.IssuerIssuer.withName("google") to "https://accounts.google.com"
                    )
                ),
                "medatarun"
            )
        }
    }

    @Test
    fun `should reject unsupported algorithms`() {
        assertFailsWith<JwtUnsupportedAlgException> {
            JwkExternalProvidersImpl.createJwtExternalProvidersFromConfigProperties(
                MapConfigResolver(
                    mapOf(
                        ConfigProperties.Issuers.key to "google",
                        ConfigProperties.IssuerIssuer.withName("google") to "https://accounts.google.com",
                        ConfigProperties.IssuerJWKS.withName("google") to "https://www.googleapis.com/oauth2/v3/certs",
                        ConfigProperties.IssuerAlgorithms.withName("google") to "RS256, HS256"
                    )
                ),
                "medatarun"
            )
        }
    }

    @Test
    fun `should trim csv values and ignore blanks`() {
        val providers = JwkExternalProvidersImpl.createJwtExternalProvidersFromConfigProperties(
            MapConfigResolver(
                mapOf(
                    ConfigProperties.Issuers.key to " google, azure, ",
                    ConfigProperties.IssuerIssuer.withName("google") to "https://accounts.google.com",
                    ConfigProperties.IssuerJWKS.withName("google") to "https://www.googleapis.com/oauth2/v3/certs",
                    ConfigProperties.IssuerAudiences.withName("google") to " audience-1, , audience-2, ",
                    ConfigProperties.IssuerAlgorithms.withName("google") to " RS256, ES256, ",
                    ConfigProperties.IssuerIssuer.withName("azure") to "https://login.microsoftonline.com/common/v2.0",
                    ConfigProperties.IssuerJWKS.withName("azure") to "https://login.microsoftonline.com/common/discovery/v2.0/keys"
                )
            ),
            "medatarun"
        )

        val google = providers.findExternalProvider("https://accounts.google.com")
        assertEquals(
            JwtIssuerConfig(
                name = "google",
                issuer = "https://accounts.google.com",
                jwksUri = "https://www.googleapis.com/oauth2/v3/certs",
                audiences = listOf("audience-1", "audience-2"),
                allowedAlgs = listOf(JwtSupportedAlgorithm.RS256, JwtSupportedAlgorithm.ES256)
            ),
            google
        )
        assertNotNull(providers.findByIssuer("https://login.microsoftonline.com/common/v2.0"))
    }

    @Test
    fun `should default algorithms when missing or blank`() {
        val providers = JwkExternalProvidersImpl.createJwtExternalProvidersFromConfigProperties(
            MapConfigResolver(
                mapOf(
                    ConfigProperties.Issuers.key to "google, azure",
                    ConfigProperties.IssuerIssuer.withName("google") to "https://accounts.google.com",
                    ConfigProperties.IssuerJWKS.withName("google") to "https://www.googleapis.com/oauth2/v3/certs",
                    ConfigProperties.IssuerIssuer.withName("azure") to "https://login.microsoftonline.com/common/v2.0",
                    ConfigProperties.IssuerJWKS.withName("azure") to "https://login.microsoftonline.com/common/discovery/v2.0/keys",
                    ConfigProperties.IssuerAlgorithms.withName("azure") to ""
                )
            ),
            "medatarun"
        )

        val google = providers.findExternalProvider("https://accounts.google.com")
        assertEquals(listOf(JwtSupportedAlgorithm.RS256), google.allowedAlgs)

        val azure = providers.findExternalProvider("https://login.microsoftonline.com/common/v2.0")
        assertEquals(listOf(JwtSupportedAlgorithm.RS256), azure.allowedAlgs)
    }

    @Test
    fun `should throw when issuer is unknown`() {
        val providers = JwkExternalProvidersImpl.createJwtExternalProvidersFromConfigProperties(
            MapConfigResolver(
                mapOf(
                    ConfigProperties.Issuers.key to "google",
                    ConfigProperties.IssuerIssuer.withName("google") to "https://accounts.google.com",
                    ConfigProperties.IssuerJWKS.withName("google") to "https://www.googleapis.com/oauth2/v3/certs"
                )
            ),
            "medatarun"
        )

        assertFailsWith<JwtJwksUnknownExternalProvider> {
            providers.findByIssuer("https://issuer.example/unknown")
        }
        assertFailsWith<JwtUnknownIssuerException> {
            providers.findExternalProvider("https://issuer.example/unknown")
        }
    }

}
