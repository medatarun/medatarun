package io.medatarun.auth.internal

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.medatarun.auth.domain.*
import io.medatarun.auth.ports.exposed.OAuthService
import io.medatarun.auth.ports.exposed.OIDCTokenResponse
import io.medatarun.auth.ports.exposed.OIDCTokenResponseOrError
import io.medatarun.auth.ports.exposed.OidcService
import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.auth.ports.needs.OidcStorage
import io.medatarun.auth.ports.needs.UserStorage
import kotlinx.serialization.json.*
import java.net.URI
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.*

class OidcServiceImpl(
    private val userStorage: UserStorage,
    private val oidcAuthCodeStorage: OidcStorage,
    private val userClaimsService: UserClaimsService,
    private val oauthService: OAuthService,
    private val authEmbeddedKeys: JwtKeyMaterial,
    private val jwtCfg: JwtConfig,
    private val clock: AuthClock,
    private val authCtxDurationSeconds: Long
) : OidcService {

    val clients = listOf<OidcClient>(
        OidcClient(
            "medatarun-ui",
            listOf(
                "http://localhost:8080/authentication-callback",
                "http://localhost:5173/authentication-callback"
            ),
        ),
        OidcClient(
            "seij-gestion",
            listOf("http://localhost:4200/authentication-callback"),
        )
    ).associateBy { it.clientId }

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

    override fun oidcWellKnownOpenIdConfiguration(publicBaseUrl: URI): JsonObject {
        return buildJsonObject {
            put("issuer", jwtCfg.issuer)
            put("authorization_endpoint", publicBaseUrl.resolve("/oidc/authorize").toURL().toExternalForm())
            put("token_endpoint", publicBaseUrl.resolve("/oidc/token").toURL().toExternalForm())
            put("userinfo_endpoint", publicBaseUrl.resolve("/oidc/userinfo").toURL().toExternalForm())

            put("jwks_uri", publicBaseUrl.resolve("/oidc/jwks.json").toURL().toExternalForm())

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

    override fun oidcAuthorize(req: OidcAuthorizeRequest): OidcAuthorizeResult {

        val redirectUri = req.redirectUri
            ?: return OidcAuthorizeResult.FatalError("missing redirect_uri")

        if (req.responseType != "code") {
            return OidcAuthorizeResult.RedirectError(
                redirectUri, "unsupported_response_type", req.state
            )
        }

        val clientId = req.clientId
            ?: return OidcAuthorizeResult.RedirectError(
                redirectUri, "unauthorized_client", req.state
            )

        val client = clients[clientId]
            ?: return OidcAuthorizeResult.RedirectError(
                redirectUri, "unauthorized_client", req.state
            )

        if (!client.redirectUris.contains(redirectUri)) {
            return OidcAuthorizeResult.FatalError("invalid redirect_uri")
        }

        val scope = req.scope
            ?: return OidcAuthorizeResult.RedirectError(
                redirectUri, "invalid_scope", req.state
            )
        if (!scope.split(" ").contains("openid")) {
            return OidcAuthorizeResult.RedirectError(
                redirectUri, "invalid_scope", req.state
            )
        }

        val state = req.state

        val codeChallenge = req.codeChallenge
            ?: return OidcAuthorizeResult.RedirectError(
                redirectUri,
                "invalid_request",
                state
            )

        if (req.codeChallengeMethod != "S256") {
            return OidcAuthorizeResult.RedirectError(
                redirectUri,
                "invalid_request",
                state
            )
        }
        val codeChallengeMethod = "S256"

        val nonce = req.nonce

        val authorizeCtxCode = UUID.randomUUID().toString()

        val createdAt = clock.now()
        val expiresAt = createdAt.plusSeconds(authCtxDurationSeconds)

        oidcAuthCodeStorage.saveAuthCtx(
            OidcAuthorizeCtx(
                clientId = clientId,
                redirectUri = redirectUri,
                scope = scope,
                state = state,
                codeChallenge = codeChallenge,
                codeChallengeMethod = codeChallengeMethod,
                authCtxCode = authorizeCtxCode,
                nonce = nonce,
                createdAt = clock.now(),
                expiresAt = clock.now().plusSeconds(60 * 15),
            )
        )
        return OidcAuthorizeResult.Valid(authCtxCode = authorizeCtxCode)

    }

    override fun oidcAuthorizeErrorLocation(resp: OidcAuthorizeResult.RedirectError): String {
        return buildString {
            append(resp.redirectUri)
            append("?error=")
            append(URLEncoder.encode(resp.error, Charsets.UTF_8))
            if (resp.state != null) {
                append("&state=")
                append(URLEncoder.encode(resp.state, Charsets.UTF_8))
            }
        }
    }

    override fun oidcAuthorizeFindAuthCtx(authCtxCode: String): OidcAuthorizeCtx? {
        return oidcAuthCodeStorage.findAuthCtx(authCtxCode)
    }

    override fun oidcAuthorizeCreateCode(authorizeCtxCode: String, subject: String): String {
        val authorizeCtx = oidcAuthCodeStorage.findAuthCtx(authorizeCtxCode)

        val code = UUID.randomUUID().toString()

        val now = clock.now()

        val oidcAuthorizeCode = OidcAuthorizeCode(
            code = code,
            clientId = authorizeCtx.clientId,
            redirectUri = authorizeCtx.redirectUri,
            subject = subject,
            scope = authorizeCtx.scope,
            codeChallenge = authorizeCtx.codeChallenge,
            codeChallengeMethod = authorizeCtx.codeChallengeMethod,
            nonce = authorizeCtx.nonce,
            authTime = now,
            expiresAt = now.plusSeconds(120)
        )

        // tu stockes authCode dans une map / DB
        oidcAuthCodeStorage.deleteAuthCtx(authorizeCtxCode)
        oidcAuthCodeStorage.saveAuthCode(oidcAuthorizeCode)

        // tu envoies *uniquement*, la string au client
        return buildRedirectUri(oidcAuthorizeCode.redirectUri, authorizeCtx.state, code)
    }

    override fun oidcTokenUri(): String {
        return "/oidc/token"
    }

    fun buildRedirectUri(redirectUri: String, state: String?, authorizationCode: String): String {
        return buildString {
            append(redirectUri)
            append("?code=")
            append(URLEncoder.encode(authorizationCode, Charsets.UTF_8))
            if (state != null) {
                append("&state=")
                append(URLEncoder.encode(state, Charsets.UTF_8))
            }
        }
    }

    override fun oidcToken(req: OidcTokenRequest): OIDCTokenResponseOrError {

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
        val user = userStorage.findByLogin(subject) ?: throw UserNotFoundException()

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

    fun issueIdToken(
        sub: String,
        claims: Map<String, Any?>,
        clientId: String,
        authTime: Instant,
        nonce: String?
    ): String {
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

        if (nonce != null) {
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

