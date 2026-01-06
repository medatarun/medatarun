package io.medatarun.auth.bootstrap.internal


import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.medatarun.auth.bootstrap.AuthEmbeddedJwtConfig
import io.medatarun.auth.bootstrap.AuthEmbeddedJwtTokenIssuer
import io.medatarun.auth.bootstrap.JwtKeyMaterial
import java.time.Instant
import java.util.*


class AuthEmbeddedJwtTokenIssuerImpl(
    private val keys: JwtKeyMaterial,
    private val cfg: AuthEmbeddedJwtConfig,
): AuthEmbeddedJwtTokenIssuer {
    override fun issueToken(sub: String, claims: Map<String, Any?>): String {
        val alg = Algorithm.RSA256(keys.publicKey, keys.privateKey)
        val now = Instant.now()

        val b = JWT.create()
            .withKeyId(keys.kid)
            .withIssuer(cfg.issuer)
            .withAudience(cfg.audience)
            .withSubject(sub)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(cfg.ttlSeconds)))

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