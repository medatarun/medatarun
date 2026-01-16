package io.medatarun.auth

import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.internal.jwk.JwksAdapter
import io.medatarun.auth.internal.jwk.JwtVerifierResolverImpl
import io.medatarun.auth.internal.oidc.OidcServiceImpl.Companion.JWKS_URI
import io.medatarun.auth.internal.oidc.OidcServiceImpl.Companion.OIDC_WELL_KNOWN_OPEN_ID_CONFIGURATION
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

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
        // Test to complete by assuring that the protocol is correct
        // (not based on the code but the protocol)
    }

}
