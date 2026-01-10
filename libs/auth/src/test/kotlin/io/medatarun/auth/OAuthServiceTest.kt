package io.medatarun.auth

import com.auth0.jwt.JWT
import io.medatarun.auth.domain.JwtConfig
import io.medatarun.auth.fixtures.AuthEnvTest
import java.time.Instant
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OAuthServiceTest {

    @Test
    fun `should issue token with default configuration and various claims`() {
        val env = AuthEnvTest()
        val service = env.oauthService
        val sub = "user123"
        val claims = mapOf(
            "role" to "ADMIN",
            "active" to true,
            "score" to 100,
            "largeNumber" to 1234567890L,
            "ratio" to 0.75
        )

        val token = service.issueAccessToken(sub, claims)
        env.verifyToken(token, sub, expectedClaims = claims)
    }

    @Test
    fun `should respect custom configuration`() {
        val customConfig = JwtConfig(
            issuer = "custom-issuer",
            audience = "custom-audience",
            ttlSeconds = 60
        )
        val env = AuthEnvTest(customConfig)
        val sub = "custom-sub"

        val token = env.oauthService.issueAccessToken(sub, emptyMap())
        val decoded = env.verifyToken(
            token,
            sub,
            expectedIssuer = "custom-issuer",
            expectedAudience = "custom-audience",
            keyMaterial = env.jwtKeyMaterial
        )

        val now = Instant.now()
        assertTrue(decoded.expiresAt.after(Date.from(now)), "Token should not be expired")
        assertTrue(decoded.expiresAt.before(Date.from(now.plusSeconds(61))), "Token should expire within TTL")
    }

    @Test
    fun `should include kid in header`() {
        val env = AuthEnvTest()
        val service = env.oauthService
        val token = service.issueAccessToken("sub", emptyMap())
        val decoded = JWT.decode(token)
        assertEquals(env.jwtKeyMaterial.kid, decoded.keyId)
    }

    @Test
    fun `should handle null claims by ignoring them`() {
        val env = AuthEnvTest()
        val service = env.oauthService
        val sub = "user"
        val claims = mapOf(
            "present" to "value",
            "missing" to null
        )

        val token = service.issueAccessToken(sub, claims)
        val decoded = env.verifyToken(token, sub)
        assertEquals("value", decoded.getClaim("present").asString())
        assertTrue(decoded.getClaim("missing").isMissing || decoded.getClaim("missing").isNull)
    }


}