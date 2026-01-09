package io.medatarun.auth.internal

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.medatarun.auth.domain.*
import io.medatarun.auth.ports.exposed.AuthEmbeddedOIDCService
import io.medatarun.auth.ports.exposed.OAuthService
import io.medatarun.auth.ports.exposed.OIDCTokenResponse
import io.medatarun.auth.ports.exposed.OIDCTokenResponseOrError
import io.medatarun.auth.ports.needs.AuthorizeStorage
import io.medatarun.auth.ports.needs.UserStore
import kotlinx.serialization.json.*
import java.net.URI
import java.security.MessageDigest
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.*

class AuthEmbeddedOIDCServiceImpl(
    val oidcAuthorizeService: AuthEmbeddedOIDCAuthorizeService,
    val userStorage: UserStore,
    val oidcAuthCodeStorage: AuthorizeStorage,
    val userClaimsService: UserClaimsService,
    val oauthService: OAuthService,
    val authEmbeddedKeys: JwtKeyMaterial,
    val jwtCfg: AuthEmbeddedJwtConfig
) : AuthEmbeddedOIDCService {



    override fun oidcPublicKey(): RSAPublicKey {
        return authEmbeddedKeys.publicKey
    }

    override fun oidcIssuer(): String {
        return jwtCfg.issuer
    }

    override fun oidcAudience(): String {
        return jwtCfg.audience
    }

    override fun oidcJwksUri(): String {
        return "/oidc/jwks.json"
    }

    override fun oidcWellKnownOpenIdConfigurationUri(): String {
        return "/oidc/.well-known/openid-configuration"
    }

    override fun oidcWellKnownOpenIdConfiguration(baseUri: URI): JsonObject {
        return buildJsonObject {
            put("issuer", jwtCfg.issuer)
            put("authorization_endpoint", baseUri.resolve("/oidc/authorize").toURL().toExternalForm())
            put("token_endpoint", baseUri.resolve("/oidc/token").toURL().toExternalForm())
            put("userinfo_endpoint", baseUri.resolve("/oidc/userinfo").toURL().toExternalForm())

            put("jwks_uri", baseUri.resolve("/oidc/jwks.json").toURL().toExternalForm())

            // Current OIDC recommendation is to only support "code" flow,
            // not "token" flow anymore (token flow, is when you send the JDBC token in URLs)
            // This make us PKCE comparible
            putJsonArray("response_types_supported") { add("code") }

            // Our OIDC doesn't support token refresh, so no "refresh_token"
            putJsonArray("grant_types_supported") { addAll(listOf("authorization_code")) }

            // Indicates that the "sub" in the token is the same whatever the client who requests the token
            putJsonArray("subject_types_supported") { add("public") }
            putJsonArray("id_token_signing_alg_values_supported") { add(oidcJwks().keys.first().alg) }

            putJsonArray("scopes_supported") { addAll(listOf("openid", "profile", "email")) }
            putJsonArray("claims_supported") { addAll(listOf("sub", "iss", "aud", "exp", "iat", "email", "role")) }

            putJsonArray("code_challenge_methods_supported") { add("S256") }
        }
    }


    override fun oidcJwks(): Jwks {
        return JwksAdapter.toJwks(authEmbeddedKeys.publicKey, authEmbeddedKeys.kid)
    }

    override fun oidcAuthorizeUri(): String {
        return "/oidc/authorize"
    }

    override fun oidcAuthorize(req: OIDCAuthorizeRequest): AuthorizeResult {
        return oidcAuthorizeService.authorize(req)
    }

    override fun oidcAuthorizeErrorLocation(resp: AuthorizeResult.RedirectError): String {
        return oidcAuthorizeService.createRedirectErrorLocation(resp)
    }

    override fun oidcAuthorizeFindAuthCtx(authCtxCode: String): AuthCtx? {
        return oidcAuthorizeService.findAuthCtx(authCtxCode)
    }

    override fun oidcAuthorizeCreateCode(authCtxCode: String, subject: String): String {
        return oidcAuthorizeService.authorize(authCtxCode, subject)
    }

    override fun oidcTokenUri(): String {
        return "/oidc/token"
    }


    override fun oidcToken(req: OIDCTokenRequest): OIDCTokenResponseOrError {

        val authCode = oidcAuthCodeStorage.findAuthCode(req.code)
            ?: return OIDCTokenResponseOrError.Error("invalid_grant")

        if (Instant.now().isAfter(authCode.expiresAt)) {
            return OIDCTokenResponseOrError.Error("invalid_grant")
        }
        if (authCode.clientId != req.clientId) {
            return OIDCTokenResponseOrError.Error("invalid_grant")
        }
        if (authCode.redirectUri != req.redirectUri) {
            return OIDCTokenResponseOrError.Error("invalid_grant")
        }
        if (authCode.codeChallengeMethod != "S256") {
            return OIDCTokenResponseOrError.Error("invalid_grant")
        }
        val expected = authCode.codeChallenge
        val actual = pkceChallenge(req.codeVerifier)

        if (actual != expected) {
            return OIDCTokenResponseOrError.Error("invalid_grant")
        }

        oidcAuthCodeStorage.deleteAuthCode(authCode.code)

        val subject = authCode.subject
        val user = userStorage.findByLogin(subject) ?: throw AuthEmbeddedUserNotFoundException()

        val idToken = issueIdToken(
            sub = user.login,
            clientId = req.clientId,
            claims = userClaimsService.createUserClaims(user),
            authTime = authCode.authTime,
            nonce = authCode.nonce
        )
        val oidcAccessTokenResp = oauthService.createOAuthAccessTokenForUser(user)

        return OIDCTokenResponseOrError.Success(
            OIDCTokenResponse(
                idToken = idToken,
                accessToken = oidcAccessTokenResp.accessToken,
                tokenType = "Bearer",
                expiresIn = Instant.now().plusSeconds(3600).epochSecond,
            )
        )

    }

    fun pkceChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(verifier.toByteArray(Charsets.US_ASCII))
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(hash)
    }

    fun issueIdToken(sub: String, claims: Map<String, Any?>, clientId: String, authTime: Instant, nonce: String?): String {
        val alg = Algorithm.RSA256(authEmbeddedKeys.publicKey, authEmbeddedKeys.privateKey)
        val now = Instant.now()

        val b = JWT.create()
            .withKeyId(authEmbeddedKeys.kid)
            .withIssuer(jwtCfg.issuer)
            .withAudience(clientId)
            .withSubject(sub)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusSeconds(jwtCfg.ttlSeconds)))
            .withClaim("auth_time", authTime.epochSecond)

        if (nonce!=null) {
            b.withClaim("nonce", nonce)
        }

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

