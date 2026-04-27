package io.medatarun.auth.oidc

import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.internal.jwk.JwksAdapter
import io.medatarun.auth.internal.oidc.OidcServiceImpl.Companion.JWKS_URI
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@EnableDatabaseTests
class OidcJwksTest {

    @Test
    fun `oidcJwksUri is fixed`() {
        val env = AuthEnvTest()
        assertEquals(JWKS_URI, env.oidcService.oidcJwksUri())
    }


    @Test
    fun `oidcJwks responses matches standard JWKS`() {
        val env = AuthEnvTest()

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

}