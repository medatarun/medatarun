package io.medatarun.auth.oidc

import com.auth0.jwt.JWT
import io.medatarun.auth.domain.ConfigProperties
import io.medatarun.auth.domain.oidc.AuthRefreshTokenRequest
import io.medatarun.auth.domain.oidc.AuthTokenRequest
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.fixtures.AuthTestUtils
import io.medatarun.auth.internal.oidc.AuthClientRegistry
import io.medatarun.auth.internal.oidc.OidcAuthorizeResult
import io.medatarun.auth.ports.exposed.OIDCTokenResponseOrError
import io.medatarun.auth.ports.exposed.OidcClientRegistrationResponseOrError
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for OAuth refresh tokens exposed through the OIDC token endpoint.
 */
@EnableDatabaseTests
class OidcRefreshTokenTest {

    /**
     * The first authorization code exchange must return a refresh token when
     * the original authorization request asked for `offline_access` and the
     * client allows the `refresh_token` grant.
     *
     * This proves that the server gives the client the long-lived credential
     * needed to renew access without asking the user to log in again.
     */
    @Test
    fun `oidcToken returns refresh token when offline_access is requested`() {
        val env = AuthEnvTest()
        val actor = env.createUserActor()

        val authorizeResult = env.oidcService.oidcAuthorize(
            env.buildAuthorizeRequest(
                clientId = env.oidcInternalClientId,
                redirectUri = env.clientDefaultRedirectUri,
                scope = env.clientScopeWithRefresh,
                codeChallenge = env.clientDefaultCodeChallenge,
                nonce = "nonce-123"
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(authorizeResult.authCtxCode, actor.subject)
        val authorizationCode = AuthTestUtils.parseQueryParams(redirectLocation)["code"]
        assertNotNull(authorizationCode)

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE,
                code = authorizationCode,
                redirectUri = env.clientDefaultRedirectUri,
                clientId = env.oidcInternalClientId,
                codeVerifier = env.clientDefaultCodeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)

        assertNotNull(tokenResult.token.refreshToken)
    }

    /**
     * The first authorization code exchange must not return a refresh token
     * when the original authorization request did not ask for `offline_access`.
     *
     * This proves that refresh-token issuance is tied to the requested OAuth
     * scope and is not silently enabled for every token response.
     */
    @Test
    fun `oidcToken does not return refresh token without offline_access`() {
        val env = AuthEnvTest()
        val actor = env.createUserActor()
        val scope = "openid profile email"

        val authorizeResult = env.oidcService.oidcAuthorize(
            env.buildAuthorizeRequest(
                clientId = env.oidcInternalClientId,
                redirectUri = env.clientDefaultRedirectUri,
                scope = scope,
                codeChallenge = env.clientDefaultCodeChallenge,
                nonce = "nonce-123"
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(authorizeResult.authCtxCode, actor.subject)
        val authorizationCode = AuthTestUtils.parseQueryParams(redirectLocation)["code"]
        assertNotNull(authorizationCode)

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE,
                code = authorizationCode,
                redirectUri = env.clientDefaultRedirectUri,
                clientId = env.oidcInternalClientId,
                codeVerifier = env.clientDefaultCodeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)

        assertNull(tokenResult.token.refreshToken)
    }

    /**
     * The first authorization code exchange must not return a refresh token
     * when the client does not allow the `refresh_token` grant.
     *
     * This proves that refresh-token issuance requires both parts of the
     * contract: the user-facing scope request and the server-side client
     * configuration.
     */
    @Test
    fun `oidcToken does not return refresh token when client does not allow refresh token grant`() {
        val env = AuthEnvTest()
        val actor = env.createUserActor()
        val redirectUri = "http://127.0.0.1:8788/callback-" + UuidUtils.generateV4String()
        val registrationResult = env.oidcService.oidcRegister(
            buildJsonObject {
                putJsonArray("redirect_uris") { add(redirectUri) }
                putJsonArray("grant_types") { add(AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE) }
                putJsonArray("response_types") { add(AuthClientRegistry.AUTHORIZATION_CODE_RESPONSE_TYPE) }
                put("token_endpoint_auth_method", AuthClientRegistry.TOKEN_ENDPOINT_AUTH_METHOD_NONE)
                put("client_name", "Refresh token test client")
            }
        )
        assertIs<OidcClientRegistrationResponseOrError.Success>(registrationResult)
        assertFalse(registrationResult.registration.grantTypes.contains(AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE))

        val clientId = registrationResult.registration.clientId

        val authorizeResult = env.oidcService.oidcAuthorize(
            env.buildAuthorizeRequest(
                clientId = clientId,
                redirectUri = redirectUri,
                scope = env.clientScopeWithRefresh,
                codeChallenge = env.clientDefaultCodeChallenge,
                nonce = "nonce-123"
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(authorizeResult.authCtxCode, actor.subject)
        val authorizationCode = AuthTestUtils.parseQueryParams(redirectLocation)["code"]
        assertNotNull(authorizationCode)

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE,
                code = authorizationCode,
                redirectUri = redirectUri,
                clientId = clientId,
                codeVerifier = env.clientDefaultCodeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)

        assertNull(tokenResult.token.refreshToken)
    }

    /**
     * A stored refresh token must be found from the hash of the value sent by
     * the client, not from the raw token value itself.
     *
     * This proves that the server can refresh a session while keeping the raw
     * refresh token out of storage.
     */
    @Test
    fun `oidcToken stores refresh token hash and not raw refresh token value`() {
        val env = AuthEnvTest()
        val actor = env.createUserActor()

        val authorizeResult = env.oidcService.oidcAuthorize(
            env.buildAuthorizeRequest(
                clientId = env.oidcInternalClientId,
                redirectUri = env.clientDefaultRedirectUri,
                scope = env.clientScopeWithRefresh,
                codeChallenge = env.clientDefaultCodeChallenge,
                nonce = "nonce-123"
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(authorizeResult.authCtxCode, actor.subject)
        val authorizationCode = AuthTestUtils.parseQueryParams(redirectLocation)["code"]
        assertNotNull(authorizationCode)

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE,
                code = authorizationCode,
                redirectUri = env.clientDefaultRedirectUri,
                clientId = env.oidcInternalClientId,
                codeVerifier = env.clientDefaultCodeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)

        val refreshTokenValue = assertNotNull(tokenResult.token.refreshToken)
        val storedRefreshTokens = env.oidcService.findRefreshTokenBySubject(actor.subject)
        assertEquals(1, storedRefreshTokens.size)
        assertNotEquals(refreshTokenValue, storedRefreshTokens.single().tokenHash)
        assertTrue(storedRefreshTokens.single().tokenHash.isNotBlank())
    }

    /**
     * A valid refresh token exchange must return a new access token, a new ID
     * token, and a new refresh token different from the one used in the request.
     *
     * This proves that `oidcTokenRefresh` renews the whole client session
     * payload expected by the UI, including refresh-token rotation.
     */
    @Test
    fun `oidcTokenRefresh returns new tokens and a new refresh token`() {
        val env = AuthEnvTest()
        val actor = env.createUserActor()

        val authorizeResult = env.oidcService.oidcAuthorize(
            env.buildAuthorizeRequest(
                clientId = env.oidcInternalClientId,
                redirectUri = env.clientDefaultRedirectUri,
                scope = env.clientScopeWithRefresh,
                codeChallenge = env.clientDefaultCodeChallenge,
                nonce = "nonce-123"
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(authorizeResult.authCtxCode, actor.subject)
        val authorizationCode = AuthTestUtils.parseQueryParams(redirectLocation)["code"]
        assertNotNull(authorizationCode)

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE,
                code = authorizationCode,
                redirectUri = env.clientDefaultRedirectUri,
                clientId = env.oidcInternalClientId,
                codeVerifier = env.clientDefaultCodeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)
        val firstRefreshToken = assertNotNull(tokenResult.token.refreshToken)

        val refreshResult = env.oidcService.oidcTokenRefresh(
            AuthRefreshTokenRequest(
                grantType = AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE,
                clientId = env.oidcInternalClientId,
                refreshToken = firstRefreshToken
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(refreshResult)

        assertTrue(refreshResult.token.accessToken.isNotBlank())
        assertTrue(refreshResult.token.idToken.isNotBlank())
        assertEquals("Bearer", refreshResult.token.tokenType)
        assertEquals(env.jwtConfig.ttlSeconds, refreshResult.token.expiresIn)
        val nextRefreshToken = assertNotNull(refreshResult.token.refreshToken)
        assertNotEquals(firstRefreshToken, nextRefreshToken)
    }

    /**
     * After a successful refresh, the refresh token used for that exchange must
     * stop working.
     *
     * This proves that refresh-token rotation is enforced: each refresh token
     * is single-use, and replaying an old value is rejected.
     */
    @Test
    fun `oidcTokenRefresh rejects previous refresh token after rotation`() {
        val env = AuthEnvTest()
        val actor = env.createUserActor()

        val authorizeResult = env.oidcService.oidcAuthorize(
            env.buildAuthorizeRequest(
                clientId = env.oidcInternalClientId,
                redirectUri = env.clientDefaultRedirectUri,
                scope = env.clientScopeWithRefresh,
                codeChallenge = env.clientDefaultCodeChallenge,
                nonce = "nonce-123"
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(authorizeResult.authCtxCode, actor.subject)
        val authorizationCode = AuthTestUtils.parseQueryParams(redirectLocation)["code"]
        assertNotNull(authorizationCode)

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE,
                code = authorizationCode,
                redirectUri = env.clientDefaultRedirectUri,
                clientId = env.oidcInternalClientId,
                codeVerifier = env.clientDefaultCodeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)
        val firstRefreshToken = assertNotNull(tokenResult.token.refreshToken)

        val firstRefreshResult = env.oidcService.oidcTokenRefresh(
            AuthRefreshTokenRequest(
                grantType = AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE,
                clientId = env.oidcInternalClientId,
                refreshToken = firstRefreshToken
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(firstRefreshResult)

        val replayResponse = env.oidcService.oidcTokenRefresh(
            AuthRefreshTokenRequest(
                grantType = AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE,
                clientId = env.oidcInternalClientId,
                refreshToken = firstRefreshToken
            )
        )
        assertIs<OIDCTokenResponseOrError.Error>(replayResponse)
        assertEquals("invalid_grant", replayResponse.error)
    }

    /**
     * A successful refresh must save the new refresh token and mark the old
     * refresh token as revoked with `replacedById` pointing to the new record.
     *
     * This proves that rotation keeps an explicit replacement link: the old
     * token is no longer usable, and the stored record says which token
     * replaced it.
     */
    @Test
    fun `oidcTokenRefresh saves new refresh token and revokes previous token with replacement id`() {
        val env = AuthEnvTest()
        val actor = env.createUserActor()

        val authorizeResult = env.oidcService.oidcAuthorize(
            env.buildAuthorizeRequest(
                clientId = env.oidcInternalClientId,
                redirectUri = env.clientDefaultRedirectUri,
                scope = env.clientScopeWithRefresh,
                codeChallenge = env.clientDefaultCodeChallenge,
                nonce = "nonce-123"
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(authorizeResult.authCtxCode, actor.subject)
        val authorizationCode = AuthTestUtils.parseQueryParams(redirectLocation)["code"]
        assertNotNull(authorizationCode)

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE,
                code = authorizationCode,
                redirectUri = env.clientDefaultRedirectUri,
                clientId = env.oidcInternalClientId,
                codeVerifier = env.clientDefaultCodeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)
        val firstRefreshToken = assertNotNull(tokenResult.token.refreshToken)
        val firstStoredToken = env.oidcService.findRefreshTokenBySubject(actor.subject).single()

        val refreshResult = env.oidcService.oidcTokenRefresh(
            AuthRefreshTokenRequest(
                grantType = AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE,
                clientId = env.oidcInternalClientId,
                refreshToken = firstRefreshToken
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(refreshResult)
        assertNotNull(refreshResult.token.refreshToken)

        val storedRefreshTokens = env.oidcService.findRefreshTokenBySubject(actor.subject)
        assertEquals(2, storedRefreshTokens.size)
        val revokedToken = assertNotNull(env.oidcService.findRefreshTokenById(firstStoredToken.id))
        val replacementToken = storedRefreshTokens.firstOrNull { it.id != firstStoredToken.id }
        assertNotNull(replacementToken)

        assertNotNull(revokedToken.revokedAt)
        assertEquals(replacementToken.id, revokedToken.replacedById)
        assertNull(replacementToken.revokedAt)
        assertNull(replacementToken.replacedById)
    }

    /**
     * A refresh request using a token that was never issued must fail with
     * `invalid_grant`.
     *
     * This proves that the server does not mint tokens from an arbitrary random
     * string sent as `refresh_token`.
     */
    @Test
    fun `oidcTokenRefresh rejects unknown refresh token`() {
        val env = AuthEnvTest()

        val response = env.oidcService.oidcTokenRefresh(
            AuthRefreshTokenRequest(
                grantType = AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE,
                clientId = env.oidcInternalClientId,
                refreshToken = "unknown-refresh-token-" + UuidUtils.generateV4String()
            )
        )

        assertIs<OIDCTokenResponseOrError.Error>(response)
        assertEquals("invalid_grant", response.error)
    }

    /**
     * A refresh request using a token that was already revoked by a previous
     * successful refresh must fail with `invalid_grant`.
     *
     * This proves that revocation is checked directly, not only as a side
     * effect of token lookup.
     */
    @Test
    fun `oidcTokenRefresh rejects revoked refresh token`() {
        val env = AuthEnvTest()
        val actor = env.createUserActor()

        val authorizeResult = env.oidcService.oidcAuthorize(
            env.buildAuthorizeRequest(
                clientId = env.oidcInternalClientId,
                redirectUri = env.clientDefaultRedirectUri,
                scope = env.clientScopeWithRefresh,
                codeChallenge = env.clientDefaultCodeChallenge,
                nonce = "nonce-123"
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(authorizeResult.authCtxCode, actor.subject)
        val authorizationCode = AuthTestUtils.parseQueryParams(redirectLocation)["code"]
        assertNotNull(authorizationCode)

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE,
                code = authorizationCode,
                redirectUri = env.clientDefaultRedirectUri,
                clientId = env.oidcInternalClientId,
                codeVerifier = env.clientDefaultCodeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)
        val firstRefreshToken = assertNotNull(tokenResult.token.refreshToken)

        val firstRefreshResult = env.oidcService.oidcTokenRefresh(
            AuthRefreshTokenRequest(
                grantType = AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE,
                clientId = env.oidcInternalClientId,
                refreshToken = firstRefreshToken
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(firstRefreshResult)

        val response = env.oidcService.oidcTokenRefresh(
            AuthRefreshTokenRequest(
                grantType = AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE,
                clientId = env.oidcInternalClientId,
                refreshToken = firstRefreshToken
            )
        )
        assertIs<OIDCTokenResponseOrError.Error>(response)
        assertEquals("invalid_grant", response.error)
    }

    /**
     * A refresh request using an expired refresh token must fail with
     * `invalid_grant`.
     *
     * This proves that refresh-token lifetime is actually enforced and that the
     * refresh TTL is not only stored as metadata.
     */
    @Test
    fun `oidcTokenRefresh rejects expired refresh token`() {
        val env = AuthEnvTest(
            extraProps = mapOf(ConfigProperties.OAuthRefreshTokenTtlSeconds.key to "1")
        )
        val actor = env.createUserActor()

        val authorizeResult = env.oidcService.oidcAuthorize(
            env.buildAuthorizeRequest(
                clientId = env.oidcInternalClientId,
                redirectUri = env.clientDefaultRedirectUri,
                scope = env.clientScopeWithRefresh,
                codeChallenge = env.clientDefaultCodeChallenge,
                nonce = "nonce-123"
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(authorizeResult.authCtxCode, actor.subject)
        val authorizationCode = AuthTestUtils.parseQueryParams(redirectLocation)["code"]
        assertNotNull(authorizationCode)

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE,
                code = authorizationCode,
                redirectUri = env.clientDefaultRedirectUri,
                clientId = env.oidcInternalClientId,
                codeVerifier = env.clientDefaultCodeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)
        val firstRefreshToken = assertNotNull(tokenResult.token.refreshToken)

        val storedToken = env.oidcService.findRefreshTokenBySubject(actor.subject).single()
        env.authClockTests.staticNow = storedToken.expiresAt.plusSeconds(1)

        val response = env.oidcService.oidcTokenRefresh(
            AuthRefreshTokenRequest(
                grantType = AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE,
                clientId = env.oidcInternalClientId,
                refreshToken = firstRefreshToken
            )
        )
        assertIs<OIDCTokenResponseOrError.Error>(response)
        assertEquals("invalid_grant", response.error)
    }

    /**
     * A refresh token issued to one client must not be usable by another
     * `client_id`.
     *
     * This proves that refresh tokens stay bound to the client that received
     * them during the initial authorization code exchange.
     */
    @Test
    fun `oidcTokenRefresh rejects refresh token used by another client`() {
        val env = AuthEnvTest()
        val actor = env.createUserActor()

        val authorizeResult = env.oidcService.oidcAuthorize(
            env.buildAuthorizeRequest(
                clientId = env.oidcInternalClientId,
                redirectUri = env.clientDefaultRedirectUri,
                scope = env.clientScopeWithRefresh,
                codeChallenge = env.clientDefaultCodeChallenge,
                nonce = "nonce-123"
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(authorizeResult.authCtxCode, actor.subject)
        val authorizationCode = AuthTestUtils.parseQueryParams(redirectLocation)["code"]
        assertNotNull(authorizationCode)

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE,
                code = authorizationCode,
                redirectUri = env.clientDefaultRedirectUri,
                clientId = env.oidcInternalClientId,
                codeVerifier = env.clientDefaultCodeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)
        val firstRefreshToken = assertNotNull(tokenResult.token.refreshToken)

        val response = env.oidcService.oidcTokenRefresh(
            AuthRefreshTokenRequest(
                grantType = AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE,
                clientId = "other-client",
                refreshToken = firstRefreshToken
            )
        )
        assertIs<OIDCTokenResponseOrError.Error>(response)
        assertEquals("invalid_grant", response.error)
    }

    /**
     * A refresh exchange must keep the same subject in the newly issued tokens
     * and in the replacement refresh token record.
     *
     * This proves that refresh renews tokens for the existing authenticated
     * session instead of changing the user behind the session.
     */
    @Test
    fun `oidcTokenRefresh preserves subject`() {
        val env = AuthEnvTest()
        val actor = env.createUserActor()

        val authorizeResult = env.oidcService.oidcAuthorize(
            env.buildAuthorizeRequest(
                clientId = env.oidcInternalClientId,
                redirectUri = env.clientDefaultRedirectUri,
                scope = env.clientScopeWithRefresh,
                codeChallenge = env.clientDefaultCodeChallenge,
                nonce = "nonce-123"
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(authorizeResult.authCtxCode, actor.subject)
        val authorizationCode = AuthTestUtils.parseQueryParams(redirectLocation)["code"]
        assertNotNull(authorizationCode)

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE,
                code = authorizationCode,
                redirectUri = env.clientDefaultRedirectUri,
                clientId = env.oidcInternalClientId,
                codeVerifier = env.clientDefaultCodeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)
        val firstRefreshToken = assertNotNull(tokenResult.token.refreshToken)
        val firstStoredToken = env.oidcService.findRefreshTokenBySubject(actor.subject).single()

        val refreshResult = env.oidcService.oidcTokenRefresh(
            AuthRefreshTokenRequest(
                grantType = AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE,
                clientId = env.oidcInternalClientId,
                refreshToken = firstRefreshToken
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(refreshResult)

        val refreshedIdToken = env.verifyToken(
            refreshResult.token.idToken,
            expectedSub = actor.subject,
            expectedAudience = env.oidcInternalClientId
        )
        val storedRefreshTokens = env.oidcService.findRefreshTokenBySubject(actor.subject)
        val replacementToken = storedRefreshTokens.firstOrNull { it.id != firstStoredToken.id }
        assertNotNull(replacementToken)

        assertEquals(actor.subject, refreshedIdToken.subject)
        assertEquals(actor.subject, replacementToken.subject)
    }

    /**
     * A refresh exchange must keep the original authentication time in the new
     * ID token and in the replacement refresh token record.
     *
     * This proves that refresh is not treated as a new login: the user gets new
     * tokens, but the authentication time remains the one from the original
     * authorization code flow.
     */
    @Test
    fun `oidcTokenRefresh preserves authentication time`() {
        val env = AuthEnvTest()
        val actor = env.createUserActor()

        val authorizeResult = env.oidcService.oidcAuthorize(
            env.buildAuthorizeRequest(
                clientId = env.oidcInternalClientId,
                redirectUri = env.clientDefaultRedirectUri,
                scope = env.clientScopeWithRefresh,
                codeChallenge = env.clientDefaultCodeChallenge,
                nonce = "nonce-123"
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(authorizeResult.authCtxCode, actor.subject)
        val authorizationCode = AuthTestUtils.parseQueryParams(redirectLocation)["code"]
        assertNotNull(authorizationCode)

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE,
                code = authorizationCode,
                redirectUri = env.clientDefaultRedirectUri,
                clientId = env.oidcInternalClientId,
                codeVerifier = env.clientDefaultCodeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)
        val firstRefreshToken = assertNotNull(tokenResult.token.refreshToken)
        val firstStoredToken = env.oidcService.findRefreshTokenBySubject(actor.subject).single()
        env.authClockTests.staticNow = env.authClockTests.staticNow.plusSeconds(30)

        val refreshResult = env.oidcService.oidcTokenRefresh(
            AuthRefreshTokenRequest(
                grantType = AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE,
                clientId = env.oidcInternalClientId,
                refreshToken = firstRefreshToken
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(refreshResult)

        val refreshedIdToken = JWT.decode(refreshResult.token.idToken)
        val storedRefreshTokens = env.oidcService.findRefreshTokenBySubject(actor.subject)
        val replacementToken = storedRefreshTokens.firstOrNull { it.id != firstStoredToken.id }
        assertNotNull(replacementToken)

        assertEquals(firstStoredToken.authTime.epochSecond, refreshedIdToken.getClaim("auth_time").asLong())
        assertEquals(firstStoredToken.authTime, replacementToken.authTime)
    }

    /**
     * A refresh exchange must keep the original scope in the replacement
     * refresh token record.
     *
     * This proves that rotation carries forward the authorization originally
     * granted instead of widening or dropping the stored scope.
     */
    @Test
    fun `oidcTokenRefresh preserves scope`() {
        val env = AuthEnvTest()
        val actor = env.createUserActor()

        val authorizeResult = env.oidcService.oidcAuthorize(
            env.buildAuthorizeRequest(
                clientId = env.oidcInternalClientId,
                redirectUri = env.clientDefaultRedirectUri,
                scope = env.clientScopeWithRefresh,
                codeChallenge = env.clientDefaultCodeChallenge,
                nonce = "nonce-123"
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(authorizeResult.authCtxCode, actor.subject)
        val authorizationCode = AuthTestUtils.parseQueryParams(redirectLocation)["code"]
        assertNotNull(authorizationCode)

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE,
                code = authorizationCode,
                redirectUri = env.clientDefaultRedirectUri,
                clientId = env.oidcInternalClientId,
                codeVerifier = env.clientDefaultCodeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)
        val firstRefreshToken = assertNotNull(tokenResult.token.refreshToken)
        val firstStoredToken = env.oidcService.findRefreshTokenBySubject(actor.subject).single()

        val refreshResult = env.oidcService.oidcTokenRefresh(
            AuthRefreshTokenRequest(
                grantType = AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE,
                clientId = env.oidcInternalClientId,
                refreshToken = firstRefreshToken
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(refreshResult)

        val storedRefreshTokens = env.oidcService.findRefreshTokenBySubject(actor.subject)
        val replacementToken = storedRefreshTokens.firstOrNull { it.id != firstStoredToken.id }
        assertNotNull(replacementToken)
        assertEquals(env.clientScopeWithRefresh, replacementToken.scope)
    }

    /**
     * A refresh exchange must keep the original nonce in the new ID token and
     * in the replacement refresh token record.
     *
     * This proves that the OIDC data captured during the authorization request
     * survives refresh-token rotation.
     */
    @Test
    fun `oidcTokenRefresh preserves nonce`() {
        val env = AuthEnvTest()
        val actor = env.createUserActor()
        val nonce = "nonce-" + UuidUtils.generateV4String()

        val authorizeResult = env.oidcService.oidcAuthorize(
            env.buildAuthorizeRequest(
                clientId = env.oidcInternalClientId,
                redirectUri = env.clientDefaultRedirectUri,
                scope = env.clientScopeWithRefresh,
                codeChallenge = env.clientDefaultCodeChallenge,
                nonce = nonce
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(authorizeResult.authCtxCode, actor.subject)
        val authorizationCode = AuthTestUtils.parseQueryParams(redirectLocation)["code"]
        assertNotNull(authorizationCode)

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE,
                code = authorizationCode,
                redirectUri = env.clientDefaultRedirectUri,
                clientId = env.oidcInternalClientId,
                codeVerifier = env.clientDefaultCodeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)
        val firstRefreshToken = assertNotNull(tokenResult.token.refreshToken)
        val firstStoredToken = env.oidcService.findRefreshTokenBySubject(actor.subject).single()

        val refreshResult = env.oidcService.oidcTokenRefresh(
            AuthRefreshTokenRequest(
                grantType = AuthClientRegistry.REFRESH_TOKEN_GRANT_TYPE,
                clientId = env.oidcInternalClientId,
                refreshToken = firstRefreshToken
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(refreshResult)

        val refreshedIdToken = JWT.decode(refreshResult.token.idToken)
        val storedRefreshTokens = env.oidcService.findRefreshTokenBySubject(actor.subject)
        val replacementToken = storedRefreshTokens.firstOrNull { it.id != firstStoredToken.id }
        assertNotNull(replacementToken)

        assertEquals(nonce, refreshedIdToken.getClaim("nonce").asString())
        assertEquals(nonce, replacementToken.nonce)
    }
}
