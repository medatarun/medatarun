package io.medatarun.auth.internal.oidc

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.medatarun.auth.domain.ActorNotFoundException
import io.medatarun.auth.domain.jwt.Jwk
import io.medatarun.auth.domain.jwt.Jwks
import io.medatarun.auth.domain.jwt.JwtConfig
import io.medatarun.auth.domain.jwt.JwtKeyMaterial
import io.medatarun.auth.domain.oidc.OidcAuthorizeCode
import io.medatarun.auth.domain.oidc.OidcAuthorizeCtx
import io.medatarun.auth.domain.oidc.OidcAuthorizeRequest
import io.medatarun.auth.domain.oidc.AuthRefreshToken
import io.medatarun.auth.domain.oidc.AuthRefreshTokenId
import io.medatarun.auth.domain.oidc.AuthRefreshTokenRequest
import io.medatarun.auth.domain.oidc.AuthTokenRequest
import io.medatarun.auth.internal.actors.ActorClaimsAdapter
import io.medatarun.auth.internal.jwk.JwkExternalProviders
import io.medatarun.auth.internal.jwk.JwksAdapter
import io.medatarun.auth.internal.jwk.JwtVerifierResolverImpl
import io.medatarun.auth.internal.oidc.AuthClientRegistry.Companion.oidcInternalClientId
import io.medatarun.auth.ports.exposed.*
import io.medatarun.auth.ports.needs.AuthClock
import io.medatarun.auth.ports.needs.OidcProviderConfig
import io.medatarun.auth.ports.needs.OidcStorage
import io.medatarun.lang.uuid.UuidUtils
import kotlinx.serialization.json.*
import java.net.URI
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.*

class OidcServiceImpl(
    private val oidcAuthCodeStorage: OidcStorage,
    private val actorClaimsAdapter: ActorClaimsAdapter,
    private val oauthService: OAuthService,
    private val authEmbeddedKeys: JwtKeyMaterial,
    private val jwtCfg: JwtConfig,
    private val clock: AuthClock,
    private val actorService: ActorService,
    private val authCtxDurationSeconds: Long,
    private val oauthRefreshTokenTtlSeconds: Long,
    private val authClientRegistry: AuthClientRegistry,
    private val externalProviders: JwkExternalProviders,
    private val oidcProviderConfig: OidcProviderConfig?,
    private val publicBaseUrl: URI
) : OidcService {

    private val jwtVerifierResolver: JwtVerifierResolver = JwtVerifierResolverImpl(
        internalIssuer = jwtCfg.issuer,
        internalAudience = jwtCfg.audience,
        internalPublicKey = authEmbeddedKeys.publicKey,
        externalJwkProviders = externalProviders
    )

    override fun oidcAuthority(): URI {
        return oidcProviderConfig?.authority ?: publicBaseUrl
    }

    override fun oidcClientId(): String {
        return oidcProviderConfig?.clientId ?: oidcInternalClientId
    }

    override fun oidcClientInfo( clientId: String): AuthClient? {
        return  authClientRegistry.find( clientId)
    }

    override fun findRefreshTokenById(id: AuthRefreshTokenId): AuthRefreshToken? {
        return oidcAuthCodeStorage.findRefreshTokenByIdOptional(id)
    }

    override fun findRefreshTokenBySubject(subject: String): List<AuthRefreshToken> {
        return oidcAuthCodeStorage.findRefreshTokensBySubject(subject)
    }

    override fun jwtVerifierResolver(): JwtVerifierResolver {
        return jwtVerifierResolver
    }

    override fun oidcIssuer(): String {
        return jwtCfg.issuer
    }

    override fun oidcJwksUri(): String {
        return JWKS_URI
    }

    override fun oidcWellKnownOpenIdConfigurationUri(): String {
        return OIDC_WELL_KNOWN_OPEN_ID_CONFIGURATION
    }

    override fun oidcWellKnownOpenIdConfiguration(): JsonObject {
        return buildJsonObject {
            put("issuer", jwtCfg.issuer)
            put("authorization_endpoint", publicBaseUrl.resolve(AUTH_AUTHORIZE_URI).toURL().toExternalForm())
            put("token_endpoint", publicBaseUrl.resolve(AUTH_TOKEN_URI).toURL().toExternalForm())
            put("registration_endpoint", publicBaseUrl.resolve(AUTH_REGISTER_URI).toURL().toExternalForm())
            put("userinfo_endpoint", publicBaseUrl.resolve(AUTH_USER_INFO_URI).toURL().toExternalForm())

            put("jwks_uri", publicBaseUrl.resolve(JWKS_URI).toURL().toExternalForm())

            // Current OIDC recommendation is to only support "code" flow,
            // not "token" flow anymore (token flow, is when you send the JDBC token in URLs)
            // This make us PKCE comparible
            putJsonArray("response_types_supported") { add("code") }

            putJsonArray("grant_types_supported") {
                add(AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE)
                add(AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE)
            }
            putJsonArray("token_endpoint_auth_methods_supported") { add(AuthClientRegistry.TOKEN_ENDPOINT_AUTH_METHOD_NONE) }

            // Indicates that the "sub" in the token is the same whatever the client who requests the token
            putJsonArray("subject_types_supported") { add("public") }
            putJsonArray("id_token_signing_alg_values_supported") { add(oidcJwkKeySingle().alg) }

            putJsonArray("scopes_supported") {
                add("openid")
                add("profile")
                add("email")
                add("offline_access")
            }
            putJsonArray("claims_supported") {
                add("sub")
                add("iss")
                add("aud")
                add("exp")
                add("iat")
                add("email")
                add("roles")
            }

            putJsonArray("code_challenge_methods_supported") { add("S256") }
        }
    }

    fun oidcJwksKeys(): Jwks {
        return JwksAdapter.toJwks(authEmbeddedKeys.publicKey, authEmbeddedKeys.kid)
    }

    fun oidcJwkKeySingle(): Jwk {
        return oidcJwksKeys().keys.first()
    }

    override fun oidcJwks(): JsonObject {
        val jwks = oidcJwksKeys()
        return buildJsonObject {
            putJsonArray("keys") {
                jwks.keys.forEach {
                    addJsonObject {
                        put("kty", it.kty)
                        put("use", it.use)
                        put("alg", it.alg)
                        put("kid", it.kid)
                        put("n", it.n)
                        put("e", it.e)
                    }
                }
            }
        }
    }

    override fun oidcAuthorizeUri(): String {
        return AUTH_AUTHORIZE_URI
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

        val redirectUriParsed = URI(redirectUri)
        if (redirectUriParsed.fragment != null) {
            return OidcAuthorizeResult.FatalError("invalid redirect_uri")
        }

        if (!authClientRegistry.exists(clientId)) return OidcAuthorizeResult.RedirectError(
            redirectUri, "unauthorized_client", req.state
        )

        if (!authClientRegistry.matchesUri(clientId, redirectUri)) {
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

        val authorizeCtxCode = UuidUtils.generateV4String()

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
                createdAt = createdAt,
                expiresAt = expiresAt,
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

    override fun oidcAuthorizeFindAuthCtx(authCtxCode: String): OidcAuthorizeCtx {
        return oidcAuthCodeStorage.findAuthCtx(authCtxCode)
    }

    override fun oidcAuthorizeCreateCode(authCtxCode: String, subject: String): String {
        val authorizeCtx = oidcAuthCodeStorage.findAuthCtx(authCtxCode)

        val code = UuidUtils.generateV4String()

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
        oidcAuthCodeStorage.deleteAuthCtx(authCtxCode)
        oidcAuthCodeStorage.saveAuthCode(oidcAuthorizeCode)

        // tu envoies *uniquement*, la string au client
        return buildRedirectUri(oidcAuthorizeCode.redirectUri, authorizeCtx.state, code)
    }

    override fun oidcTokenUri(): String {
        return AUTH_TOKEN_URI
    }

    override fun oidcRegisterUri(): String {
        return AUTH_REGISTER_URI
    }

    override fun oidcRegister(request: JsonObject): OidcClientRegistrationResponseOrError {
        return authClientRegistry.registerDynamicClient(request)
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

    override fun oidcToken(req: AuthTokenRequest): OIDCTokenResponseOrError {

        if (req.grantType != "authorization_code") {
            return OIDCTokenResponseOrError.Error("invalid_grant")
        }

        val authCode = oidcAuthCodeStorage.findAuthCode(req.code)
            ?: return OIDCTokenResponseOrError.Error("invalid_grant")

        if (clock.now().isAfter(authCode.expiresAt)) {
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

        val actor = actorService.findByIssuerAndSubjectOptional(oidcIssuer(), authCode.subject)
            ?: throw ActorNotFoundException()

        val idToken = issueIdToken(
            sub = authCode.subject,
            clientId = req.clientId,
            claims = actorClaimsAdapter.createUserClaims(actor),
            authTime = authCode.authTime,
            nonce = authCode.nonce
        )
        val oidcAccessTokenResp = oauthService.createOAuthAccessTokenForActor(actor)
        val refreshToken = createInitialRefreshTokenIfAllowed(
            clientId = req.clientId,
            subject = authCode.subject,
            scope = authCode.scope,
            authTime = authCode.authTime,
            nonce = authCode.nonce
        )

        return OIDCTokenResponseOrError.Success(
            OIDCTokenResponse(
                idToken = idToken,
                accessToken = oidcAccessTokenResp.accessToken,
                tokenType = "Bearer",
                expiresIn = jwtCfg.ttlSeconds,
                refreshToken = refreshToken
            )
        )

    }

    override fun oidcTokenRefresh(request: AuthRefreshTokenRequest): OIDCTokenResponseOrError {
        if (request.grantType != AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE) {
            return OIDCTokenResponseOrError.Error("invalid_grant")
        }

        // Converts the token received to a hash, because storage only stores the hashes and not the real value
        val tokenHash = refreshTokenHash(request.refreshToken)

        // Find matching token or fail
        val existingRefreshToken = oidcAuthCodeStorage.findRefreshTokenByTokenHashOptional(tokenHash)
            ?: return OIDCTokenResponseOrError.Error("invalid_grant")


        // cheks the token is not expired
        val now = clock.now()
        if (now.isAfter(existingRefreshToken.expiresAt)) {
            return OIDCTokenResponseOrError.Error("invalid_grant")
        }

        // cheks the token had not been revoked by another one
        if (existingRefreshToken.revokedAt != null) {
            return OIDCTokenResponseOrError.Error("invalid_grant")
        }

        // cheks the token had been generated by this client
        if (existingRefreshToken.clientId != request.clientId) {
            return OIDCTokenResponseOrError.Error("invalid_grant")
        }

        // From the refresh token get the actor to we know which subject to return
        val actor = actorService.findByIssuerAndSubjectOptional(oidcIssuer(), existingRefreshToken.subject)
            ?: throw ActorNotFoundException()

        val nextRefreshTokenWithRawValue = createRefreshTokenWithRawValue(
            clientId = existingRefreshToken.clientId,
            subject = existingRefreshToken.subject,
            scope = existingRefreshToken.scope,
            authTime = existingRefreshToken.authTime,
            nonce = existingRefreshToken.nonce
        )

        // Save the new token and marks old token replaced with new one
        oidcAuthCodeStorage.saveRefreshToken(nextRefreshTokenWithRawValue.refreshToken)
        oidcAuthCodeStorage.revokeRefreshToken(existingRefreshToken.id, now, nextRefreshTokenWithRawValue.refreshToken.id)

        // Generate claims and the response with new token value
        // It's like the original token creation
        val claims = actorClaimsAdapter.createUserClaims(actor)
        val idToken = issueIdToken(
            sub = existingRefreshToken.subject,
            clientId = existingRefreshToken.clientId,
            claims = claims,
            authTime = existingRefreshToken.authTime,
            nonce = existingRefreshToken.nonce
        )
        val accessTokenResponse = oauthService.createOAuthAccessTokenForActor(actor)

        return OIDCTokenResponseOrError.Success(
            OIDCTokenResponse(
                idToken = idToken,
                accessToken = accessTokenResponse.accessToken,
                tokenType = "Bearer",
                expiresIn = jwtCfg.ttlSeconds,
                refreshToken = nextRefreshTokenWithRawValue.refreshTokenValue
            )
        )
    }

    private fun createInitialRefreshTokenIfAllowed(
        clientId: String,
        subject: String,
        scope: String,
        authTime: Instant,
        nonce: String?
    ): String? {
        if (!scope.split(" ").contains("offline_access")) {
            return null
        }

        val client = authClientRegistry.find(clientId)
            ?: return null
        if (!client.grantTypes.contains(AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE)) {
            return null
        }

        val refreshToken = createRefreshTokenWithRawValue(
            clientId = clientId,
            subject = subject,
            scope = scope,
            authTime = authTime,
            nonce = nonce
        )
        oidcAuthCodeStorage.saveRefreshToken(refreshToken.refreshToken)
        return refreshToken.refreshTokenValue
    }

    private fun createRefreshTokenWithRawValue(
        clientId: String,
        subject: String,
        scope: String,
        authTime: Instant,
        nonce: String?
    ): AuthRefreshTokenWithRawValue {
        val refreshTokenValue = generateRefreshTokenValue()
        val now = clock.now()
        return AuthRefreshTokenWithRawValue(
            refreshTokenValue = refreshTokenValue,
            refreshToken = AuthRefreshToken(
                id = AuthRefreshTokenId.generate(),
                tokenHash = refreshTokenHash(refreshTokenValue),
                clientId = clientId,
                subject = subject,
                scope = scope,
                authTime = authTime,
                expiresAt = now.plusSeconds(oauthRefreshTokenTtlSeconds),
                revokedAt = null,
                replacedById = null,
                nonce = nonce
            )
        )
    }

    /**
     * A refresh token with its random value. Note that in AuthRefreshToken
     * we don't keep the value sent to the client, just the hash.
     */
    private data class AuthRefreshTokenWithRawValue(
        val refreshTokenValue: String,
        val refreshToken: AuthRefreshToken
    )

    private fun generateRefreshTokenValue(): String {
        val bytes = ByteArray(REFRESH_TOKEN_BYTES)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes)
    }

    private fun refreshTokenHash(refreshToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(refreshToken.toByteArray(Charsets.UTF_8))
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(hash)
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
        val now = clock.now()

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
                is List<*> -> b.withArrayClaim(k, v.filterIsInstance<String>().toTypedArray())
                is Array<*> -> b.withArrayClaim(k, v.filterIsInstance<String>().toTypedArray())
                else -> b.withClaim(k, v.toString())
            }
        }

        return b.sign(alg)
    }

    companion object {
        const val JWKS_URI = "/auth/jwks.json"
        const val OIDC_WELL_KNOWN_OPEN_ID_CONFIGURATION = "/.well-known/openid-configuration"
        const val AUTH_AUTHORIZE_URI = "/auth/authorize"
        const val AUTH_TOKEN_URI = "/auth/token"
        const val AUTH_REGISTER_URI = "/auth/register"
        const val AUTH_USER_INFO_URI = "/auth/userinfo"
        private const val REFRESH_TOKEN_BYTES = 32
        private val secureRandom = SecureRandom()
    }

}
