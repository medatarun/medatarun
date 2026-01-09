package io.medatarun.auth.embedded

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import io.medatarun.auth.domain.AuthEmbeddedJwtConfig
import io.medatarun.auth.domain.JwtKeyMaterial
import io.medatarun.auth.domain.User
import io.medatarun.auth.internal.AuthEmbeddedKeyRegistryImpl.Companion.generateJwtKeyMaterial
import io.medatarun.auth.internal.OAuthServiceImpl
import io.medatarun.auth.internal.UserClaimsService
import io.medatarun.auth.ports.exposed.AuthEmbeddedUserService
import java.time.Instant
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OAuthServiceTest {

    class TestEnv(
        val jwtConfig: AuthEmbeddedJwtConfig = AuthEmbeddedJwtConfig(
            issuer = "urn:medatarun",
            audience = "medatarun",
            ttlSeconds = 3600
        )
    ) {

        val jwtKeyMaterial = generateJwtKeyMaterial()
        private val userService: AuthEmbeddedUserService = object : AuthEmbeddedUserService {
            override fun loadOrCreateBootstrapSecret(runOnce: (secret: String) -> Unit) = TODO("Not yet implemented")
            override fun adminBootstrap(secret: String, login: String, fullname: String, password: String): User =
                TODO("Not yet implemented")

            override fun createEmbeddedUser(
                login: String,
                fullname: String,
                clearPassword: String,
                admin: Boolean
            ): User = TODO("Not yet implemented")

            override fun changeOwnPassword(username: String, oldPassword: String, newPassword: String) =
                TODO("Not yet implemented")

            override fun changeUserPassword(login: String, newPassword: String) = TODO("Not yet implemented")
            override fun disableUser(username: String) = TODO("Not yet implemented")
            override fun changeUserFullname(username: String, fullname: String) = TODO("Not yet implemented")
            override fun loginUser(username: String, password: String): User = TODO("Not yet implemented")
        }

        val service = OAuthServiceImpl(
            userService = userService,
            jwtConfig = jwtConfig,
            keys = jwtKeyMaterial,
            userClaimsService = UserClaimsService(),
        )
    }


    val env = TestEnv()
    val service = env.service

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

        val token = service.issueAccessToken(sub, claims)
        verifyToken(token, sub, expectedClaims = claims)
    }

    @Test
    fun `should respect custom configuration`() {
        val customConfig = AuthEmbeddedJwtConfig(
            issuer = "custom-issuer",
            audience = "custom-audience",
            ttlSeconds = 60
        )
        val customEnv = TestEnv(customConfig)
        val customService = customEnv.service
        val sub = "custom-sub"

        val token = customService.issueAccessToken(sub, emptyMap())
        val decoded = verifyToken(token, sub, expectedIssuer = "custom-issuer", expectedAudience = "custom-audience", keyMaterial = customEnv.jwtKeyMaterial)

        val now = Instant.now()
        assertTrue(decoded.expiresAt.after(Date.from(now)), "Token should not be expired")
        assertTrue(decoded.expiresAt.before(Date.from(now.plusSeconds(61))), "Token should expire within TTL")
    }

    @Test
    fun `should include kid in header`() {
        val token = service.issueAccessToken("sub", emptyMap())
        val decoded = JWT.decode(token)
        assertEquals(env.jwtKeyMaterial.kid, decoded.keyId)
    }

    @Test
    fun `should handle null claims by ignoring them`() {
        val sub = "user"
        val claims = mapOf(
            "present" to "value",
            "missing" to null
        )

        val token = service.issueAccessToken(sub, claims)
        val decoded = verifyToken(token, sub)
        assertEquals("value", decoded.getClaim("present").asString())
        assertTrue(decoded.getClaim("missing").isMissing || decoded.getClaim("missing").isNull)
    }


    private fun verifyToken(
        token: String,
        expectedSub: String,
        expectedIssuer: String = env.jwtConfig.issuer,
        expectedAudience: String = env.jwtConfig.audience,
        expectedClaims: Map<String, Any?> = emptyMap(),
        keyMaterial: JwtKeyMaterial = env.jwtKeyMaterial
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