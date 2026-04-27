package io.medatarun.auth.oidc

import com.auth0.jwt.JWT
import io.medatarun.auth.domain.ActorNotFoundException
import io.medatarun.auth.domain.oidc.AuthTokenRequest
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.fixtures.AuthTestUtils
import io.medatarun.auth.internal.oidc.AuthClientRegistry
import io.medatarun.auth.internal.oidc.OidcAuthorizeResult
import io.medatarun.auth.ports.exposed.OIDCTokenResponseOrError
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@EnableDatabaseTests
class OidcTokenTest() {

    @Test
    fun `oidcToken rejects unsupported grant_type`() {
        val env = AuthEnvTest()
        /*
         * Goal: Reject token exchanges when grant_type is not authorization_code.
         * Why: OIDC token endpoint must only accept the authorization_code grant in our server.
         * How: Send a valid code with an unsupported grant_type and expect invalid_grant.
         */
        val codeVerifier = "verifier-" + UuidUtils.generateV4String()
        val fixture = createAuthorizationCodeFixture(env, codeVerifier)

        val response = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = "refresh_token",
                code = fixture.code,
                redirectUri = fixture.redirectUri,
                clientId = fixture.clientId,
                codeVerifier = codeVerifier
            )
        )

        assertIs<OIDCTokenResponseOrError.Error>(response)
        assertEquals("invalid_grant", response.error)
    }

    @Test
    fun `oidcToken rejects unknown code`() {
        val env = AuthEnvTest()
        val publicBaseUrl = env.publicBaseUrl
        /*
         * Goal: Reject unknown authorization codes.
         * Why: OIDC requires codes to be previously issued by the authorization endpoint.
         * How: Call oidcToken with a random code and expect invalid_grant.
         */
        val response = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = "authorization_code",
                code = "missing-code",
                redirectUri = publicBaseUrl.resolve("/authentication-callback").toString(),
                clientId = AuthClientRegistry.oidcInternalClientId,
                codeVerifier = "verifier"
            )
        )

        assertIs<OIDCTokenResponseOrError.Error>(response)
        assertEquals("invalid_grant", response.error)
    }

    @Test
    fun `oidcToken rejects expired code`() {
        val env = AuthEnvTest()
        /*
         * Goal: Reject expired authorization codes.
         * Why: OIDC requires codes to be short-lived and unusable after expiration.
         * How: Move the clock past expiration and expect invalid_grant.
         */
        val codeVerifier = "verifier-" + UuidUtils.generateV4String()
        val fixture = createAuthorizationCodeFixture(env, codeVerifier)
        val originalNow = env.authClockTests.staticNow
        env.authClockTests.staticNow = env.authClockTests.staticNow.plusSeconds(60 * 60)

        val response = try {
            env.oidcService.oidcToken(
                AuthTokenRequest(
                    grantType = "authorization_code",
                    code = fixture.code,
                    redirectUri = fixture.redirectUri,
                    clientId = fixture.clientId,
                    codeVerifier = codeVerifier
                )
            )
        } finally {
            env.authClockTests.staticNow = originalNow
        }

        assertIs<OIDCTokenResponseOrError.Error>(response)
        assertEquals("invalid_grant", response.error)
    }

    @Test
    fun `oidcToken rejects mismatched client_id`() {
        val env = AuthEnvTest()
        /*
         * Goal: Reject token exchange when the client_id does not match the issued code.
         * Why: OIDC ties authorization codes to a client to prevent token theft.
         * How: Use a valid code but send a different client_id and expect invalid_grant.
         */
        val codeVerifier = "verifier-" + UuidUtils.generateV4String()
        val fixture = createAuthorizationCodeFixture(env, codeVerifier)

        val response = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = "authorization_code",
                code = fixture.code,
                redirectUri = fixture.redirectUri,
                clientId = "other-client",
                codeVerifier = codeVerifier
            )
        )

        assertIs<OIDCTokenResponseOrError.Error>(response)
        assertEquals("invalid_grant", response.error)
    }

    @Test
    fun `oidcToken rejects mismatched redirect_uri`() {
        val env = AuthEnvTest()
        val publicBaseUrl = env.publicBaseUrl
        /*
         * Goal: Reject token exchange when redirect_uri does not match the issued code.
         * Why: OIDC binds the code to the redirect_uri to prevent code injection.
         * How: Use a valid code but send a different redirect_uri and expect invalid_grant.
         */
        val codeVerifier = "verifier-" + UuidUtils.generateV4String()
        val fixture = createAuthorizationCodeFixture(env, codeVerifier)

        val response = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = "authorization_code",
                code = fixture.code,
                redirectUri = publicBaseUrl.resolve("/other-callback").toString(),
                clientId = fixture.clientId,
                codeVerifier = codeVerifier
            )
        )

        assertIs<OIDCTokenResponseOrError.Error>(response)
        assertEquals("invalid_grant", response.error)
    }

    @Test
    fun `oidcToken rejects invalid code_verifier`() {
        val env = AuthEnvTest()
        /*
         * Goal: Reject token exchange when PKCE verification fails.
         * Why: PKCE prevents authorization code interception attacks.
         * How: Use a valid code but provide an incorrect code_verifier and expect invalid_grant.
         */
        val codeVerifier = "verifier-" + UuidUtils.generateV4String()
        val fixture = createAuthorizationCodeFixture(env, codeVerifier)

        val response = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = "authorization_code",
                code = fixture.code,
                redirectUri = fixture.redirectUri,
                clientId = fixture.clientId,
                codeVerifier = "wrong-" + UuidUtils.generateV4String()
            )
        )

        assertIs<OIDCTokenResponseOrError.Error>(response)
        assertEquals("invalid_grant", response.error)
    }

    @Test
    fun `oidcToken returns error when actor is missing`() {
        val env = AuthEnvTest()
        val publicBaseUrl = env.publicBaseUrl
        /*
         * Goal: Fail when the subject behind the authorization code cannot be found.
         * Why: OIDC token issuance requires a valid subject to populate claims.
         * How: Create a code for a non-existent subject and expect ActorNotFoundException on token exchange.
         */
        val subject = "missing-subject-" + UuidUtils.generateV4String()
        val codeVerifier = "verifier-" + UuidUtils.generateV4String()
        val codeChallenge = AuthTestUtils.pkceChallengeForTest(codeVerifier)
        val request = env.buildAuthorizeRequest(codeChallenge = codeChallenge)

        val authorizeResult = env.oidcService.oidcAuthorize(request)
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(
            authorizeResult.authCtxCode,
            subject
        )
        val params = AuthTestUtils.parseQueryParams(redirectLocation)
        val code = params["code"]
        assertNotNull(code)

        assertFailsWith<ActorNotFoundException> {
            env.oidcService.oidcToken(
                AuthTokenRequest(
                    grantType = "authorization_code",
                    code = code,
                    redirectUri = publicBaseUrl.resolve("/authentication-callback").toString(),
                    clientId = AuthClientRegistry.oidcInternalClientId,
                    codeVerifier = codeVerifier
                )
            )
        }
    }

    @Test
    fun `oidcToken returns expected token fields`() {
        val env = AuthEnvTest()
        /*
         * Goal: Return a complete OIDC token response with expected fields.
         * Why: Clients rely on token_type, expires_in, and access_token for subsequent API calls.
         * How: Exchange a valid code and verify response fields match the server configuration.
         */
        val codeVerifier = "verifier-" + UuidUtils.generateV4String()
        val fixture = createAuthorizationCodeFixture(env, codeVerifier)

        val response = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = "authorization_code",
                code = fixture.code,
                redirectUri = fixture.redirectUri,
                clientId = fixture.clientId,
                codeVerifier = codeVerifier
            )
        )

        assertIs<OIDCTokenResponseOrError.Success>(response)
        val token = response.token
        assertEquals("Bearer", token.tokenType)
        assertEquals(env.jwtConfig.ttlSeconds, token.expiresIn)
        assertTrue(token.accessToken.isNotBlank())
        assertTrue(token.idToken.isNotBlank())
    }

    @Test
    fun `oidcToken omits nonce when not provided`() {
        val env = AuthEnvTest()
        val publicBaseUrl = env.publicBaseUrl
        /*
         * Goal: Ensure nonce is not added when the authorization request did not include it.
         * Why: OIDC requires nonce to be echoed only when provided by the client.
         * How: Create a code with null nonce and verify the ID token has no nonce claim.
         */
        val actor = env.createUserActor()
        val codeVerifier = "verifier-" + UuidUtils.generateV4String()
        val request = env.buildAuthorizeRequest(
            nonce = null,
            state = null,
            codeChallenge = AuthTestUtils.pkceChallengeForTest(codeVerifier)
        )

        val authorizeResult = env.oidcService.oidcAuthorize(request)
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(
            authorizeResult.authCtxCode,
            actor.subject
        )
        val params = AuthTestUtils.parseQueryParams(redirectLocation)
        val code = params["code"]
        assertNotNull(code)

        val response = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = "authorization_code",
                code = code,
                redirectUri = publicBaseUrl.resolve("/authentication-callback").toString(),
                clientId = AuthClientRegistry.oidcInternalClientId,
                codeVerifier = codeVerifier
            )
        )

        assertIs<OIDCTokenResponseOrError.Success>(response)
        val decoded = JWT.decode(response.token.idToken)
        assertTrue(decoded.getClaim("nonce").isMissing || decoded.getClaim("nonce").isNull)
    }

    @Test
    fun `oidcToken rejects reused code`() {
        val env = AuthEnvTest()
        /*
         * Goal: Reject reuse of the same authorization code.
         * Why: OIDC requires codes to be single-use to prevent replay attacks.
         * How: Exchange a valid code once, then reuse it and expect invalid_grant.
         */
        val codeVerifier = "verifier-" + UuidUtils.generateV4String()
        val fixture = createAuthorizationCodeFixture(env, codeVerifier)

        val first = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = "authorization_code",
                code = fixture.code,
                redirectUri = fixture.redirectUri,
                clientId = fixture.clientId,
                codeVerifier = codeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(first)

        val second = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = "authorization_code",
                code = fixture.code,
                redirectUri = fixture.redirectUri,
                clientId = fixture.clientId,
                codeVerifier = codeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Error>(second)
        assertEquals("invalid_grant", second.error)
    }
    private fun createAuthorizationCodeFixture(
        env: AuthEnvTest,
        codeVerifier: String,
        state: String? = "state-123"
    ): AuthorizationCodeFixture {
        val actor = env.createUserActor()
        val codeChallenge = AuthTestUtils.pkceChallengeForTest(codeVerifier)
        val request = env.buildAuthorizeRequest(
            state = state,
            codeChallenge = codeChallenge
        )

        val authorizeResult = env.oidcService.oidcAuthorize(request)
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(
            authorizeResult.authCtxCode,
            actor.subject
        )
        val params = AuthTestUtils.parseQueryParams(redirectLocation)
        val code = params["code"]
        assertNotNull(code)

        return AuthorizationCodeFixture(
            code = code,
            subject = actor.subject,
            redirectUri = env.publicBaseUrl.resolve("/authentication-callback").toString(),
            clientId = AuthClientRegistry.oidcInternalClientId
        )
    }


    data class AuthorizationCodeFixture(
        val code: String,
        val subject: String,
        val redirectUri: String,
        val clientId: String
    )

}