package io.medatarun.auth.internal

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.medatarun.auth.domain.ActorNotFoundException
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.jwt.JwtConfig
import io.medatarun.auth.domain.jwt.JwtKeyMaterial
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.User
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.ports.exposed.ActorService
import io.medatarun.auth.ports.exposed.OAuthService
import io.medatarun.auth.ports.exposed.OAuthTokenResponse
import io.medatarun.auth.ports.exposed.UserService
import java.time.Instant
import java.util.*

class OAuthServiceImpl(
    private val userService: UserService,
    private val jwtConfig: JwtConfig,
    private val keys: JwtKeyMaterial,
    private val actorClaimsAdapter: ActorClaimsAdapter,
    private val actorService: ActorService
) : OAuthService {

    override fun oauthLogin(username: Username, password: PasswordClear): OAuthTokenResponse {
        val user = userService.loginUser(username, password)
        return createOAuthAccessTokenForUser(user)
    }

    override fun createOAuthAccessTokenForUser(user: User): OAuthTokenResponse {
        val actor = actorService.findByIssuerAndSubjectOptional(jwtConfig.issuer, user.username.value)
            ?: throw ActorNotFoundException()
        val token = issueAccessToken(
            sub = user.username.value,
            claims = actorClaimsAdapter.createUserClaims(actor)
        )
        return OAuthTokenResponse(token, "Bearer", jwtConfig.ttlSeconds)
    }


    override fun createOAuthAccessTokenForActor(actor: Actor): OAuthTokenResponse {
        val token = issueAccessToken(
            sub = actor.subject,
            claims = actorClaimsAdapter.createUserClaims(actor)
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
                is List<*> -> b.withArrayClaim(k, v.filterIsInstance<String>().toTypedArray())
                is Array<*> -> b.withArrayClaim(k, v.filterIsInstance<String>().toTypedArray())
                else -> b.withClaim(k, v.toString())
            }
        }

        return b.sign(alg)
    }

}
