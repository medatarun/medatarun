package io.medatarun.auth.internal.jwk

import io.medatarun.auth.domain.*
import io.medatarun.auth.domain.oidc.ExternalOidcProviderConfig
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
                    "medatarun.auth.oidc.external.names" to "google, azure",
                    "medatarun.auth.oidc.jwkscache.duration" to "120",
                    "medatarun.auth.oidc.external.google.issuer" to "https://accounts.google.com",
                    "medatarun.auth.oidc.external.google.jwksUri" to "https://www.googleapis.com/oauth2/v3/certs",
                    "medatarun.auth.oidc.external.google.audience" to "client-id-1, client-id-2",
                    "medatarun.auth.oidc.external.google.algs" to "RS256, ES256",
                    "medatarun.auth.oidc.external.azure.issuer" to "https://login.microsoftonline.com/common/v2.0",
                    "medatarun.auth.oidc.external.azure.jwksUri" to "https://login.microsoftonline.com/common/discovery/v2.0/keys",
                    "medatarun.auth.oidc.external.azure.algs" to ""
                )
            ),
            "medatarun"
        )

        val google = providers.findExternalProvider("https://accounts.google.com")
        assertEquals(
            ExternalOidcProviderConfig(
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
            ExternalOidcProviderConfig(
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
                mapOf("medatarun.auth.oidc.external.names" to " ,  , ")
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
                    "medatarun.auth.oidc.external.names" to "google",
                    "medatarun.auth.oidc.external.google.issuer" to "https://accounts.google.com",
                    "medatarun.auth.oidc.external.google.jwksUri" to "https://www.googleapis.com/oauth2/v3/certs"
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
                        "medatarun.auth.oidc.external.names" to "google",
                        "medatarun.auth.oidc.jwkscache.duration" to "not-a-number",
                        "medatarun.auth.oidc.external.google.issuer" to "https://accounts.google.com",
                        "medatarun.auth.oidc.external.google.jwksUri" to "https://www.googleapis.com/oauth2/v3/certs"
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
                        "medatarun.auth.oidc.external.names" to "internal",
                        "medatarun.auth.oidc.external.internal.issuer" to "medatarun",
                        "medatarun.auth.oidc.external.internal.jwksUri" to "https://issuer.example/jwks"
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
                        "medatarun.auth.oidc.external.names" to "google",
                        "medatarun.auth.oidc.external.google.jwksUri" to "https://www.googleapis.com/oauth2/v3/certs"
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
                        "medatarun.auth.oidc.external.names" to "google",
                        "medatarun.auth.oidc.external.google.issuer" to "https://accounts.google.com"
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
                        "medatarun.auth.oidc.external.names" to "google",
                        "medatarun.auth.oidc.external.google.issuer" to "https://accounts.google.com",
                        "medatarun.auth.oidc.external.google.jwksUri" to "https://www.googleapis.com/oauth2/v3/certs",
                        "medatarun.auth.oidc.external.google.algs" to "RS256, HS256"
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
                    "medatarun.auth.oidc.external.names" to " google, azure, ",
                    "medatarun.auth.oidc.external.google.issuer" to "https://accounts.google.com",
                    "medatarun.auth.oidc.external.google.jwksUri" to "https://www.googleapis.com/oauth2/v3/certs",
                    "medatarun.auth.oidc.external.google.audience" to " audience-1, , audience-2, ",
                    "medatarun.auth.oidc.external.google.algs" to " RS256, ES256, ",
                    "medatarun.auth.oidc.external.azure.issuer" to "https://login.microsoftonline.com/common/v2.0",
                    "medatarun.auth.oidc.external.azure.jwksUri" to "https://login.microsoftonline.com/common/discovery/v2.0/keys"
                )
            ),
            "medatarun"
        )

        val google = providers.findExternalProvider("https://accounts.google.com")
        assertEquals(
            ExternalOidcProviderConfig(
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
                    "medatarun.auth.oidc.external.names" to "google, azure",
                    "medatarun.auth.oidc.external.google.issuer" to "https://accounts.google.com",
                    "medatarun.auth.oidc.external.google.jwksUri" to "https://www.googleapis.com/oauth2/v3/certs",
                    "medatarun.auth.oidc.external.azure.issuer" to "https://login.microsoftonline.com/common/v2.0",
                    "medatarun.auth.oidc.external.azure.jwksUri" to "https://login.microsoftonline.com/common/discovery/v2.0/keys",
                    "medatarun.auth.oidc.external.azure.algs" to ""
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
                    "medatarun.auth.oidc.external.names" to "google",
                    "medatarun.auth.oidc.external.google.issuer" to "https://accounts.google.com",
                    "medatarun.auth.oidc.external.google.jwksUri" to "https://www.googleapis.com/oauth2/v3/certs"
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
