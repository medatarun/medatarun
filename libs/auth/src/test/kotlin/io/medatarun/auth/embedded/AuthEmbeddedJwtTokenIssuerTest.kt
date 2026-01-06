package io.medatarun.auth.embedded

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import io.medatarun.auth.embedded.internal.AuthEmbeddedJwtTokenIssuerImpl
import io.medatarun.auth.embedded.internal.AuthEmbeddedKeyRegistryImpl.Companion.generateJwtKeyMaterial
import java.time.Instant
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthEmbeddedJwtTokenIssuerTest {
    private val jwtKeyMaterial = generateJwtKeyMaterial()
    private val defaultConfig = AuthEmbeddedJwtConfig(
        issuer = "urn:medatarun",
        audience = "medatarun",
        ttlSeconds = 3600
    )
    private val service = AuthEmbeddedJwtTokenIssuerImpl(
        keys = jwtKeyMaterial,
        cfg = defaultConfig
    )


    @Test
    fun `should issue token with default configuration and various claims`() {
        val sub = "user123"
        val claims = mapOf(
            "role" to "ADMIN",
            "active" to true,
            "score" to 100,
            "largeNumber" to 1234567890L,
            "ratio" to 0.75
        )

        val token = service.issueToken(sub, claims)
        verifyToken(token, sub, expectedClaims = claims)
    }

    @Test
    fun `should respect custom configuration`() {
        val customConfig = AuthEmbeddedJwtConfig(
            issuer = "custom-issuer",
            audience = "custom-audience",
            ttlSeconds = 60
        )
        val customService = AuthEmbeddedJwtTokenIssuerImpl(jwtKeyMaterial, customConfig)
        val sub = "custom-sub"

        val token = customService.issueToken(sub, emptyMap())
        val decoded = verifyToken(token, sub, expectedIssuer = "custom-issuer", expectedAudience = "custom-audience")

        val now = Instant.now()
        assertTrue(decoded.expiresAt.after(Date.from(now)), "Token should not be expired")
        assertTrue(decoded.expiresAt.before(Date.from(now.plusSeconds(61))), "Token should expire within TTL")
    }

    @Test
    fun `should include kid in header`() {
        val token = service.issueToken("sub", emptyMap())
        val decoded = JWT.decode(token)
        assertEquals(jwtKeyMaterial.kid, decoded.keyId)
    }

    @Test
    fun `should handle null claims by ignoring them`() {
        val sub = "user"
        val claims = mapOf(
            "present" to "value",
            "missing" to null
        )

        val token = service.issueToken(sub, claims)
        val decoded = verifyToken(token, sub)
        assertEquals("value", decoded.getClaim("present").asString())
        assertTrue(decoded.getClaim("missing").isMissing || decoded.getClaim("missing").isNull)
    }


    private fun verifyToken(
        token: String,
        expectedSub: String,
        expectedIssuer: String = defaultConfig.issuer,
        expectedAudience: String = defaultConfig.audience,
        expectedClaims: Map<String, Any?> = emptyMap(),
        keyMaterial: JwtKeyMaterial = jwtKeyMaterial
    ): DecodedJWT {
        val algorithm = Algorithm.RSA256(keyMaterial.publicKey, keyMaterial.privateKey)
        val verifier = JWT.require(algorithm)
            .withIssuer(expectedIssuer)
            .withAudience(expectedAudience)
            .withSubject(expectedSub)
            .build()

        val decodedJWT = verifier.verify(token)

        expectedClaims.forEach { (key, value) ->
            val claim = decodedJWT.getClaim(key)
            when (value) {
                is String -> assertEquals(value, claim.asString(), "Claim $key mismatch")
                is Boolean -> assertEquals(value, claim.asBoolean(), "Claim $key mismatch")
                is Int -> assertEquals(value, claim.asInt(), "Claim $key mismatch")
                is Long -> assertEquals(value, claim.asLong(), "Claim $key mismatch")
                is Double -> assertEquals(value, claim.asDouble(), "Claim $key mismatch")
                null -> assertTrue(claim.isNull, "Claim $key should be null")
                else -> assertEquals(value.toString(), claim.asString(), "Claim $key mismatch (stringified)")
            }
        }

        return decodedJWT
    }
}