package io.medatarun.auth.oidc

import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.internal.oidc.AuthClientRegistry
import io.medatarun.auth.internal.oidc.OidcAuthorizeResult
import io.medatarun.auth.internal.oidc.OidcServiceImpl.Companion.AUTH_AUTHORIZE_URI
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@EnableDatabaseTests
class OidcAuthorizeTest {

    @Test
    fun `oidcAuthorizeUri fixed`() {
        val env = AuthEnvTest()
        assertEquals(AUTH_AUTHORIZE_URI, env.oidcService.oidcAuthorizeUri())
    }

    @Test
    fun `oidcAuthorize accepts code flow with PKCE`() {
        val env = AuthEnvTest()
        val publicBaseUrl = env.publicBaseUrl
        /*
         * Goal: Ensure the authorization endpoint accepts the standard OIDC authorization code flow.
         * Why: OIDC requires response_type=code and (for public clients) PKCE to mitigate interception.
         * How: Send a request with a registered client, matching redirect_uri, openid scope, and S256 PKCE,
         *      then verify we get a Valid result and a stored context with the same parameters.
         */
        val request = env.buildAuthorizeRequest()

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.Valid>(result)
        val authCtx = env.oidcService.oidcAuthorizeFindAuthCtx(result.authCtxCode)
        assertNotNull(authCtx)
        assertEquals(AuthClientRegistry.oidcInternalClientId, authCtx.clientId)
        assertEquals(publicBaseUrl.resolve("/authentication-callback").toString(), authCtx.redirectUri)
        assertEquals("openid", authCtx.scope)
        assertEquals("state-123", authCtx.state)
        assertEquals("challenge-123", authCtx.codeChallenge)
        assertEquals("S256", authCtx.codeChallengeMethod)
        assertEquals("nonce-123", authCtx.nonce)
    }

    @Test
    fun `oidcAuthorize requires redirect_uri`() {
        val env = AuthEnvTest()
        /*
         * Goal: Reject requests that do not provide a redirect_uri.
         * Why: OIDC forbids redirecting without a verified redirect_uri to prevent open redirects.
         * How: Send a request with null redirect_uri and assert we return a fatal error (no redirect).
         */
        val request = env.buildAuthorizeRequest(redirectUri = null)

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.FatalError>(result)
    }

    @Test
    fun `oidcAuthorize rejects unsupported response_type`() {
        val env = AuthEnvTest()
        /*
         * Goal: Enforce OIDC response_type support.
         * Why: The authorization code flow is the only supported flow; others must be rejected.
         * How: Send response_type=token and expect an unsupported_response_type error with a preserved state.
         */
        val request = env.buildAuthorizeRequest(responseType = "token")

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.RedirectError>(result)
        assertEquals("unsupported_response_type", result.error)
        assertEquals("state-123", result.state)
    }

    @Test
    fun `oidcAuthorize requires openid scope`() {
        val env = AuthEnvTest()
        /*
         * Goal: Reject authorization requests that are not for OIDC.
         * Why: The openid scope is mandatory for OIDC; missing it is an invalid scope error.
         * How: Send a request with a non-openid scope and expect invalid_scope.
         */
        val request = env.buildAuthorizeRequest(scope = "profile")

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.RedirectError>(result)
        assertEquals("invalid_scope", result.error)
        assertEquals("state-123", result.state)
    }

    @Test
    fun `oidcAuthorize keeps missing state when returning error`() {
        val env = AuthEnvTest()
        /*
         * Goal: Keep missing state as null when an error response is returned.
         * Why: OIDC specifies state is optional and must not be invented by the server.
         * How: Send an invalid response_type with no state and verify the error has null state.
         */
        val request = env.buildAuthorizeRequest(responseType = "token", state = null)

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.RedirectError>(result)
        assertEquals("unsupported_response_type", result.error)
        assertEquals(null, result.state)
    }

    @Test
    fun `oidcAuthorize accepts missing nonce`() {
        val env = AuthEnvTest()
        /*
         * Goal: Accept requests without a nonce.
         * Why: Nonce is optional for authorization requests and often omitted by some clients.
         * How: Send a valid request with no nonce and verify it succeeds and stores null.
         */
        val request = env.buildAuthorizeRequest(nonce = null)

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.Valid>(result)
        val authCtx = env.oidcService.oidcAuthorizeFindAuthCtx(result.authCtxCode)
        assertNotNull(authCtx)
        assertEquals(null, authCtx.nonce)
    }

    @Test
    fun `oidcAuthorize accepts openid scope with extra values`() {
        val env = AuthEnvTest()
        /*
         * Goal: Accept a scope list as long as it includes "openid".
         * Why: OIDC allows additional scopes like profile/email alongside openid.
         * How: Send a scope with multiple values and verify it is accepted and stored.
         */
        val request = env.buildAuthorizeRequest(scope = "openid profile email")

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.Valid>(result)
        val authCtx = env.oidcService.oidcAuthorizeFindAuthCtx(result.authCtxCode)
        assertNotNull(authCtx)
        assertEquals("openid profile email", authCtx.scope)
    }

    @Test
    fun `oidcAuthorize rejects response_type casing mismatch`() {
        val env = AuthEnvTest()
        /*
         * Goal: Reject response_type values that do not exactly match "code".
         * Why: OIDC defines response_type tokens as case-sensitive.
         * How: Send response_type=Code and expect unsupported_response_type.
         */
        val request = env.buildAuthorizeRequest(responseType = "Code")

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.RedirectError>(result)
        assertEquals("unsupported_response_type", result.error)
    }

    @Test
    fun `oidcAuthorize rejects composite response_type`() {
        val env = AuthEnvTest()
        /*
         * Goal: Reject response_type values that imply implicit or hybrid flows.
         * Why: The server only supports authorization code flow (response_type=code).
         * How: Send response_type="code id_token" and expect unsupported_response_type.
         */
        val request = env.buildAuthorizeRequest(responseType = "code id_token")

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.RedirectError>(result)
        assertEquals("unsupported_response_type", result.error)
    }

    @Test
    fun `oidcAuthorize requires PKCE code challenge`() {
        val env = AuthEnvTest()
        /*
         * Goal: Enforce PKCE for the authorization code flow.
         * Why: Public clients should not exchange codes without a proof key to prevent interception.
         * How: Send a request without code_challenge and expect invalid_request.
         */
        val request = env.buildAuthorizeRequest(codeChallenge = null)

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.RedirectError>(result)
        assertEquals("invalid_request", result.error)
        assertEquals("state-123", result.state)
    }

    @Test
    fun `oidcAuthorize requires S256 PKCE method`() {
        val env = AuthEnvTest()
        /*
         * Goal: Validate the PKCE method advertised by the server.
         * Why: If the server only supports S256, other methods must be rejected.
         * How: Send code_challenge_method=plain and expect invalid_request.
         */
        val request = env.buildAuthorizeRequest(codeChallengeMethod = "plain")

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.RedirectError>(result)
        assertEquals("invalid_request", result.error)
        assertEquals("state-123", result.state)
    }

    @Test
    fun `oidcAuthorize rejects missing PKCE method`() {
        val env = AuthEnvTest()
        /*
         * Goal: Require an explicit PKCE method when a code challenge is provided.
         * Why: The server only supports S256 and must reject unspecified methods.
         * How: Send a request with no code_challenge_method and expect invalid_request.
         */
        val request = env.buildAuthorizeRequest(codeChallengeMethod = null)

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.RedirectError>(result)
        assertEquals("invalid_request", result.error)
        assertEquals("state-123", result.state)
    }

    @Test
    fun `oidcAuthorize rejects unknown client`() {
        val env = AuthEnvTest()
        /*
         * Goal: Reject clients that are not registered with the authorization server.
         * Why: OIDC requires the AS to authenticate/authorize clients before issuing codes.
         * How: Send a request with an unknown client_id and expect unauthorized_client.
         */
        val request = env.buildAuthorizeRequest(clientId = "unknown-client")

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.RedirectError>(result)
        assertEquals("unauthorized_client", result.error)
        assertEquals("state-123", result.state)
    }

    @Test
    fun `oidcAuthorize rejects unregistered redirect_uri`() {
        val env = AuthEnvTest()
        /*
         * Goal: Refuse redirect_uri values that do not match the registered client metadata.
         * Why: OIDC requires strict redirect_uri validation to prevent token leakage.
         * How: Send a request with a foreign redirect_uri and assert a fatal error (no redirect).
         */
        val request = env.buildAuthorizeRequest(redirectUri = "https://evil.example.test/callback")

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.FatalError>(result)
    }

    @Test
    fun `oidcAuthorize rejects redirect_uri with fragment`() {
        val env = AuthEnvTest()
        val publicBaseUrl = env.publicBaseUrl
        /*
         * Goal: Reject redirect_uri values containing fragments.
         * Why: OIDC forbids fragments in redirect_uri to avoid token leakage.
         * How: Send a redirect_uri with a fragment and assert a fatal error.
         */
        val request = env.buildAuthorizeRequest(
            redirectUri = publicBaseUrl.resolve("/authentication-callback#frag").toString()
        )

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.FatalError>(result)
    }

    @Test
    fun `oidcAuthorize rejects redirect_uri with different path`() {
        val env = AuthEnvTest()
        val publicBaseUrl = env.publicBaseUrl
        /*
         * Goal: Reject redirect_uri values that do not match the registered path.
         * Why: OIDC requires strict redirect_uri matching to prevent open redirects.
         * How: Send a redirect_uri with the same host but different path and assert a fatal error.
         */
        val request = env.buildAuthorizeRequest(
            redirectUri = publicBaseUrl.resolve("/other-callback").toString()
        )

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.FatalError>(result)
    }

    @Test
    fun `oidcAuthorize allows localhost redirect even when port differs`() {
        val env = AuthEnvTest(
            publicBaseUrl = URI("http://localhost:8080")
        )
        /*
         * Goal: Allow localhost redirect_uri for development tools even when the port differs.
         * Why: We explicitly relax port matching for localhost to reduce friction in local flows.
         * How: Use a localhost redirect_uri with a non-default port and verify it is accepted.
         */
        val request = env.buildAuthorizeRequest(
            redirectUri = "http://localhost:5678/authentication-callback"
        )

        val result = env.oidcService.oidcAuthorize(request)

        assertIs<OidcAuthorizeResult.Valid>(result)
    }

}