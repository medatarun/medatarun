package io.medatarun.auth.internal

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.medatarun.auth.domain.JwtConfig
import io.medatarun.auth.domain.JwtKeyMaterial
import io.medatarun.auth.domain.User
import io.medatarun.auth.ports.exposed.OAuthService
import io.medatarun.auth.ports.exposed.OAuthTokenResponse
import io.medatarun.auth.ports.exposed.UserService
import java.time.Instant
import java.util.*

class OAuthServiceImpl(
    private val userService: UserService,
    private val jwtConfig: JwtConfig,
    private val keys: JwtKeyMaterial,
    private val userClaimsService: UserClaimsService
): OAuthService {

    override fun oauthLogin(username: String, password: String): OAuthTokenResponse {
        val user = userService.loginUser(username, password)
        return createOAuthAccessTokenForUser(user)
    }

    override fun createOAuthAccessTokenForUser(user: User): OAuthTokenResponse {
        val token = issueAccessToken(
            sub = user.login,
            claims = userClaimsService.createUserClaims(user)
        )
        return OAuthTokenResponse(token, "Bearer", jwtConfig.ttlSeconds)
    }


    fun issueAccessToken(sub: String, claims: Map<String, Any?>): String {
        val alg = Algorithm.RSA256(keys.publicKey, keys.privateKey)
        val now = Instant.now()

        val b = JWT.create()
            .withKeyId(keys.kid)
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .withSubject(sub)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(jwtConfig.ttlSeconds)))

        claims.forEach { (k, v) ->
            when (v) {
                null -> {}
                is String -> b.withClaim(k, v)
                is Boolean -> b.withClaim(k, v)
                is Int -> b.withClaim(k, v)
                is Long -> b.withClaim(k, v)
                is Double -> b.withClaim(k, v)
                else -> b.withClaim(k, v.toString())
            }
        }

        return b.sign(alg)
    }


}