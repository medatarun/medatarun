package io.medatarun.auth.oidc

import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.internal.jwk.JwksAdapter
import io.medatarun.auth.internal.oidc.OidcServiceImpl.Companion.OIDC_WELL_KNOWN_OPEN_ID_CONFIGURATION
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.*

@EnableDatabaseTests
class OidcWellKnownTest {

    @Test
    fun `wellknown uri is fixed`() {
        val env = AuthEnvTest()
        assertEquals(
            OIDC_WELL_KNOWN_OPEN_ID_CONFIGURATION,
            env.oidcService.oidcWellKnownOpenIdConfigurationUri()
        )
    }


    @Test
    fun `oidcWellKnownOpenIdConfiguration  correct`() {
        val env = AuthEnvTest()
        val publicBaseUrl = env.publicBaseUrl
        // Validate OIDC Discovery metadata required fields and advertised capabilities.
        val json = env.oidcService.oidcWellKnownOpenIdConfiguration()

        fun requireStringField(source: JsonObject, key: String): String {
            val element = source[key]
            assertNotNull(element)
            assertIs<JsonPrimitive>(element)
            assertTrue(element.isString)
            return element.content
        }

        fun requireStringArrayField(source: JsonObject, key: String): List<String> {
            val element = source[key]
            assertNotNull(element)
            assertIs<JsonArray>(element)
            val values = mutableListOf<String>()
            for (item in element) {
                assertIs<JsonPrimitive>(item)
                assertTrue(item.isString)
                values.add(item.content)
            }
            return values
        }

        val issuer = requireStringField(json, "issuer")
        assertEquals(env.jwtConfig.issuer, issuer)

        val authorizationEndpoint = requireStringField(json, "authorization_endpoint")
        val tokenEndpoint = requireStringField(json, "token_endpoint")
        val userinfoEndpoint = requireStringField(json, "userinfo_endpoint")
        val jwksUri = requireStringField(json, "jwks_uri")

        assertEquals(publicBaseUrl.resolve("/auth/authorize").toString(), authorizationEndpoint)
        assertEquals(publicBaseUrl.resolve("/auth/token").toString(), tokenEndpoint)
        assertEquals(
            publicBaseUrl.resolve("/auth/register").toString(),
            requireStringField(json, "registration_endpoint")
        )
        assertEquals(publicBaseUrl.resolve("/auth/userinfo").toString(), userinfoEndpoint)
        assertEquals(publicBaseUrl.resolve(env.oidcService.oidcJwksUri()).toString(), jwksUri)

        // Ensure we have only "code"
        val responseTypesSupported = requireStringArrayField(json, "response_types_supported")
        assertEquals(1, responseTypesSupported.size)
        assertTrue(responseTypesSupported.contains("code"))

        // Ensure we have only "authorization_code" and "refresh_token"
        val grantTypesSupported = requireStringArrayField(json, "grant_types_supported")
        assertEquals(2, grantTypesSupported.size)
        assertTrue(grantTypesSupported.contains("authorization_code"))
        assertTrue(grantTypesSupported.contains("refresh_token"))

        // Ensure we have only "public"
        val subjectTypesSupported = requireStringArrayField(json, "subject_types_supported")
        assertEquals(1, subjectTypesSupported.size)
        assertTrue(subjectTypesSupported.contains("public"))

        val idTokenAlgs = requireStringArrayField(json, "id_token_signing_alg_values_supported")
        val expectedAlg = JwksAdapter.toJwks(env.jwtKeyMaterial.publicKey, env.jwtKeyMaterial.kid).keys[0].alg
        assertEquals(1, idTokenAlgs.size)
        assertTrue(idTokenAlgs.contains(expectedAlg))

        val scopesSupported = requireStringArrayField(json, "scopes_supported")
        assertTrue(scopesSupported.containsAll(listOf("openid", "profile", "email", "offline_access")))

        val claimsSupported = requireStringArrayField(json, "claims_supported")
        assertTrue(claimsSupported.containsAll(listOf("sub", "iss", "aud", "exp", "iat", "email", "roles")))

        val pkceMethods = requireStringArrayField(json, "code_challenge_methods_supported")
        assertEquals(1, pkceMethods.size)
        assertTrue(pkceMethods.contains("S256"))

        val tokenEndpointAuthMethods = requireStringArrayField(json, "token_endpoint_auth_methods_supported")
        assertEquals(1, tokenEndpointAuthMethods.size)
        assertTrue(tokenEndpointAuthMethods.contains("none"))
    }


}