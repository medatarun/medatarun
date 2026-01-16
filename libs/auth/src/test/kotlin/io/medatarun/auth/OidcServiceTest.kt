package io.medatarun.auth

import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.internal.jwk.JwksAdapter
import io.medatarun.auth.internal.jwk.JwtVerifierResolverImpl
import io.medatarun.auth.internal.oidc.OidcServiceImpl.Companion.JWKS_URI
import io.medatarun.auth.internal.oidc.OidcServiceImpl.Companion.OIDC_AUTHORIZE_URI
import io.medatarun.auth.internal.oidc.OidcServiceImpl.Companion.OIDC_WELL_KNOWN_OPEN_ID_CONFIGURATION
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.*

class OidcServiceTest {

    val env = AuthEnvTest()

    @Test
    fun `oidcJwks responses matches standard JWKS`() {

        val expected = JwksAdapter.toJwks(env.jwtKeyMaterial.publicKey, env.jwtKeyMaterial.kid)

        val json = env.oidcService.oidcJwks()

        assertEquals(1, env.oidcService.oidcJwks().keys.size)

        // Make sure this follows JSON Web Key (JWK) RFC7517

        val keysElement = json["keys"]
        assertNotNull(keysElement)
        val keysArray = keysElement as JsonArray
        assertEquals(1, keysArray.size)
        assertEquals(1, json.size)
        val jwkElement = keysArray[0]
        val jwkObject = jwkElement as JsonObject
        assertEquals(6, jwkObject.size)

        val expectedKey = expected.keys[0]
        assertEquals(JsonPrimitive("RSA"), jwkObject["kty"])
        assertEquals(JsonPrimitive("sig"), jwkObject["use"])
        assertEquals(JsonPrimitive("RS256"), jwkObject["alg"])
        assertEquals(JsonPrimitive(expectedKey.kid), jwkObject["kid"])
        assertEquals(JsonPrimitive(expectedKey.n), jwkObject["n"])
        assertEquals(JsonPrimitive(expectedKey.e), jwkObject["e"])
    }

    @Test
    fun `oidcJwksUri is fixed`() {
        assertEquals(JWKS_URI, env.oidcService.oidcJwksUri())
    }

    @Test
    fun `oidcIssuer is fixed`() {
        assertEquals(env.jwtConfig.issuer, env.oidcService.oidcIssuer())
    }

    @Test
    fun `jwtVerifierResolver is the one we tested`() {
        // just checks that the JWTVerifier is the one we implement
        // The verifier is complex and tested independently in its own
        // test suite
        //
        // We also check that we didn't mix bad parameters when
        // configuring it because it's a very sensitive part
        val resolver = env.oidcService.jwtVerifierResolver()
        assertIs<JwtVerifierResolverImpl>(resolver)
        assertEquals(env.jwtConfig.issuer, resolver.internalIssuer)
        assertEquals(env.jwtConfig.audience, resolver.internalAudience)
        assertEquals(env.jwtKeyMaterial.publicKey, resolver.internalPublicKey)

    }

    @Test
    fun `wellknown uri is fixed`() {
        assertEquals(
            OIDC_WELL_KNOWN_OPEN_ID_CONFIGURATION,
            env.oidcService.oidcWellKnownOpenIdConfigurationUri()
        )
    }

    @Test
    fun `oidcWellKnownOpenIdConfiguration  correct`() {
        // Validate OIDC Discovery metadata required fields and advertised capabilities.
        val publicBaseUrl = URI("https://auth.example.test")

        val json = env.oidcService.oidcWellKnownOpenIdConfiguration(publicBaseUrl)

        fun requireStringField(source: JsonObject, key: String): String {
            val element = source[key]
            assertNotNull(element)
            assertIs<JsonPrimitive>(element)
            val primitive = element as JsonPrimitive
            assertTrue(primitive.isString)
            return primitive.content
        }

        fun requireStringArrayField(source: JsonObject, key: String): List<String> {
            val element = source[key]
            assertNotNull(element)
            assertIs<JsonArray>(element)
            val array = element as JsonArray
            val values = mutableListOf<String>()
            for (item in array) {
                assertIs<JsonPrimitive>(item)
                val primitive = item as JsonPrimitive
                assertTrue(primitive.isString)
                values.add(primitive.content)
            }
            return values
        }

        val issuer = requireStringField(json, "issuer")
        assertEquals(env.jwtConfig.issuer, issuer)

        val authorizationEndpoint = requireStringField(json, "authorization_endpoint")
        val tokenEndpoint = requireStringField(json, "token_endpoint")
        val userinfoEndpoint = requireStringField(json, "userinfo_endpoint")
        val jwksUri = requireStringField(json, "jwks_uri")

        assertEquals(publicBaseUrl.resolve("/oidc/authorize").toString(), authorizationEndpoint)
        assertEquals(publicBaseUrl.resolve("/oidc/token").toString(), tokenEndpoint)
        assertEquals(publicBaseUrl.resolve("/oidc/userinfo").toString(), userinfoEndpoint)
        assertEquals(publicBaseUrl.resolve(env.oidcService.oidcJwksUri()).toString(), jwksUri)

        // Ensure we have only "code"
        val responseTypesSupported = requireStringArrayField(json, "response_types_supported")
        assertEquals(1, responseTypesSupported.size)
        assertTrue(responseTypesSupported.contains("code"))

        // Ensure we have only "authorization_code" (and no refresh token)
        val grantTypesSupported = requireStringArrayField(json, "grant_types_supported")
        assertEquals(1, grantTypesSupported.size)
        assertTrue(grantTypesSupported.contains("authorization_code"))
        assertEquals(false, grantTypesSupported.contains("refresh_token"))

        // Ensure we have only "public"
        val subjectTypesSupported = requireStringArrayField(json, "subject_types_supported")
        assertEquals(1, subjectTypesSupported.size)
        assertTrue(subjectTypesSupported.contains("public"))

        val idTokenAlgs = requireStringArrayField(json, "id_token_signing_alg_values_supported")
        val expectedAlg = JwksAdapter.toJwks(env.jwtKeyMaterial.publicKey, env.jwtKeyMaterial.kid).keys[0].alg
        assertEquals(1, idTokenAlgs.size)
        assertTrue(idTokenAlgs.contains(expectedAlg))

        val scopesSupported = requireStringArrayField(json, "scopes_supported")
        assertTrue(scopesSupported.contains("openid"))

        val claimsSupported = requireStringArrayField(json, "claims_supported")
        assertTrue(claimsSupported.containsAll(listOf("sub", "iss", "aud", "exp", "iat", "email", "roles")))

        val pkceMethods = requireStringArrayField(json, "code_challenge_methods_supported")
        assertEquals(1, pkceMethods.size)
        assertTrue(pkceMethods.contains("S256"))
    }

    @Test
    fun `oidcAuthorizeUri fixed`() {
        assertEquals(OIDC_AUTHORIZE_URI, env.oidcService.oidcAuthorizeUri())
    }

    @Nested inner class OidcAuthorizeTests {

    }

}
