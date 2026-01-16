package io.medatarun.auth

import io.medatarun.auth.domain.oidc.OidcAuthorizeRequest
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.internal.jwk.JwksAdapter
import io.medatarun.auth.internal.jwk.JwtVerifierResolverImpl
import io.medatarun.auth.internal.oidc.OidcAuthorizeResult
import io.medatarun.auth.internal.oidc.OidcClientRegistry
import io.medatarun.auth.internal.oidc.OidcServiceImpl.Companion.JWKS_URI
import io.medatarun.auth.internal.oidc.OidcServiceImpl.Companion.OIDC_AUTHORIZE_URI
import io.medatarun.auth.internal.oidc.OidcServiceImpl.Companion.OIDC_WELL_KNOWN_OPEN_ID_CONFIGURATION
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.test.*

class OidcServiceTest {

    val env = AuthEnvTest()
    val publicBaseUrl = URI("https://auth.example.test")

    private fun buildAuthorizeRequest(
        responseType: String? = "code",
        clientId: String? = OidcClientRegistry.oidcInternalClientId,
        redirectUri: String? = publicBaseUrl.resolve("/authentication-callback").toString(),
        scope: String? = "openid",
        state: String? = "state-123",
        codeChallenge: String? = "challenge-123",
        codeChallengeMethod: String? = "S256",
        nonce: String? = "nonce-123"
    ): OidcAuthorizeRequest {
        return OidcAuthorizeRequest(
            responseType = responseType,
            clientId = clientId,
            redirectUri = redirectUri,
            scope = scope,
            state = state,
            codeChallenge = codeChallenge,
            codeChallengeMethod = codeChallengeMethod,
            nonce = nonce
        )
    }

    @Test
    fun `oidcJwks responses matches standard JWKS`() {

        val expected = JwksAdapter.toJwks(env.jwtKeyMaterial.publicKey, env.jwtKeyMaterial.kid)

        val json = env.oidcService.oidcJwks()

        assertEquals(1, env.oidcService.oidcJwks().keys.size)

        // Make sure this follows JSON Web Key (JWK) RFC7517

        val keysElement = json["keys"]
        assertNotNull(keysElement)
        val keysArray = keysElement as JsonArray
        assertEquals(1, keysArray.size)
        assertEquals(1, json.size)
        val jwkElement = keysArray[0]
        val jwkObject = jwkElement as JsonObject
        assertEquals(6, jwkObject.size)

        val expectedKey = expected.keys[0]
        assertEquals(JsonPrimitive("RSA"), jwkObject["kty"])
        assertEquals(JsonPrimitive("sig"), jwkObject["use"])
        assertEquals(JsonPrimitive("RS256"), jwkObject["alg"])
        assertEquals(JsonPrimitive(expectedKey.kid), jwkObject["kid"])
        assertEquals(JsonPrimitive(expectedKey.n), jwkObject["n"])
        assertEquals(JsonPrimitive(expectedKey.e), jwkObject["e"])
    }

    @Test
    fun `oidcJwksUri is fixed`() {
        assertEquals(JWKS_URI, env.oidcService.oidcJwksUri())
    }

    @Test
    fun `oidcIssuer is fixed`() {
        assertEquals(env.jwtConfig.issuer, env.oidcService.oidcIssuer())
    }

    @Test
    fun `jwtVerifierResolver is the one we tested`() {
        // just checks that the JWTVerifier is the one we implement
        // The verifier is complex and tested independently in its own
        // test suite
        //
        // We also check that we didn't mix bad parameters when
        // configuring it because it's a very sensitive part
        val resolver = env.oidcService.jwtVerifierResolver()
        assertIs<JwtVerifierResolverImpl>(resolver)
        assertEquals(env.jwtConfig.issuer, resolver.internalIssuer)
        assertEquals(env.jwtConfig.audience, resolver.internalAudience)
        assertEquals(env.jwtKeyMaterial.publicKey, resolver.internalPublicKey)

    }

    @Test
    fun `wellknown uri is fixed`() {
        assertEquals(
            OIDC_WELL_KNOWN_OPEN_ID_CONFIGURATION,
            env.oidcService.oidcWellKnownOpenIdConfigurationUri()
        )
    }

    @Test
    fun `oidcWellKnownOpenIdConfiguration  correct`() {
        // Validate OIDC Discovery metadata required fields and advertised capabilities.
        val json = env.oidcService.oidcWellKnownOpenIdConfiguration(publicBaseUrl)

        fun requireStringField(source: JsonObject, key: String): String {
            val element = source[key]
            assertNotNull(element)
            assertIs<JsonPrimitive>(element)
            val primitive = element as JsonPrimitive
            assertTrue(primitive.isString)
            return primitive.content
        }

        fun requireStringArrayField(source: JsonObject, key: String): List<String> {
            val element = source[key]
            assertNotNull(element)
            assertIs<JsonArray>(element)
            val array = element as JsonArray
            val values = mutableListOf<String>()
            for (item in array) {
                assertIs<JsonPrimitive>(item)
                val primitive = item as JsonPrimitive
                assertTrue(primitive.isString)
                values.add(primitive.content)
            }
            return values
        }

        val issuer = requireStringField(json, "issuer")
        assertEquals(env.jwtConfig.issuer, issuer)

        val authorizationEndpoint = requireStringField(json, "authorization_endpoint")
        val tokenEndpoint = requireStringField(json, "token_endpoint")
        val userinfoEndpoint = requireStringField(json, "userinfo_endpoint")
        val jwksUri = requireStringField(json, "jwks_uri")

        assertEquals(publicBaseUrl.resolve("/oidc/authorize").toString(), authorizationEndpoint)
        assertEquals(publicBaseUrl.resolve("/oidc/token").toString(), tokenEndpoint)
        assertEquals(publicBaseUrl.resolve("/oidc/userinfo").toString(), userinfoEndpoint)
        assertEquals(publicBaseUrl.resolve(env.oidcService.oidcJwksUri()).toString(), jwksUri)

        // Ensure we have only "code"
        val responseTypesSupported = requireStringArrayField(json, "response_types_supported")
        assertEquals(1, responseTypesSupported.size)
        assertTrue(responseTypesSupported.contains("code"))

        // Ensure we have only "authorization_code" (and no refresh token)
        val grantTypesSupported = requireStringArrayField(json, "grant_types_supported")
        assertEquals(1, grantTypesSupported.size)
        assertTrue(grantTypesSupported.contains("authorization_code"))
        assertEquals(false, grantTypesSupported.contains("refresh_token"))

        // Ensure we have only "public"
        val subjectTypesSupported = requireStringArrayField(json, "subject_types_supported")
        assertEquals(1, subjectTypesSupported.size)
        assertTrue(subjectTypesSupported.contains("public"))

        val idTokenAlgs = requireStringArrayField(json, "id_token_signing_alg_values_supported")
        val expectedAlg = JwksAdapter.toJwks(env.jwtKeyMaterial.publicKey, env.jwtKeyMaterial.kid).keys[0].alg
        assertEquals(1, idTokenAlgs.size)
        assertTrue(idTokenAlgs.contains(expectedAlg))

        val scopesSupported = requireStringArrayField(json, "scopes_supported")
        assertTrue(scopesSupported.contains("openid"))

        val claimsSupported = requireStringArrayField(json, "claims_supported")
        assertTrue(claimsSupported.containsAll(listOf("sub", "iss", "aud", "exp", "iat", "email", "roles")))

        val pkceMethods = requireStringArrayField(json, "code_challenge_methods_supported")
        assertEquals(1, pkceMethods.size)
        assertTrue(pkceMethods.contains("S256"))
    }

    @Test
    fun `oidcAuthorizeUri fixed`() {
        assertEquals(OIDC_AUTHORIZE_URI, env.oidcService.oidcAuthorizeUri())
    }

    @Nested inner class OidcAuthorizeTests {

        @Test
        fun `oidcAuthorize accepts code flow with PKCE`() {
            /*
             * Goal: Ensure the authorization endpoint accepts the standard OIDC authorization code flow.
             * Why: OIDC requires response_type=code and (for public clients) PKCE to mitigate interception.
             * How: Send a request with a registered client, matching redirect_uri, openid scope, and S256 PKCE,
             *      then verify we get a Valid result and a stored context with the same parameters.
             */
            val request = buildAuthorizeRequest()

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.Valid>(result)
            val authCtx = env.oidcService.oidcAuthorizeFindAuthCtx(result.authCtxCode)
            assertNotNull(authCtx)
            assertEquals(OidcClientRegistry.oidcInternalClientId, authCtx.clientId)
            assertEquals(publicBaseUrl.resolve("/authentication-callback").toString(), authCtx.redirectUri)
            assertEquals("openid", authCtx.scope)
            assertEquals("state-123", authCtx.state)
            assertEquals("challenge-123", authCtx.codeChallenge)
            assertEquals("S256", authCtx.codeChallengeMethod)
            assertEquals("nonce-123", authCtx.nonce)
        }

        @Test
        fun `oidcAuthorize requires redirect_uri`() {
            /*
             * Goal: Reject requests that do not provide a redirect_uri.
             * Why: OIDC forbids redirecting without a verified redirect_uri to prevent open redirects.
             * How: Send a request with null redirect_uri and assert we return a fatal error (no redirect).
             */
            val request = buildAuthorizeRequest(redirectUri = null)

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.FatalError>(result)
        }

        @Test
        fun `oidcAuthorize rejects unsupported response_type`() {
            /*
             * Goal: Enforce OIDC response_type support.
             * Why: The authorization code flow is the only supported flow; others must be rejected.
             * How: Send response_type=token and expect an unsupported_response_type error with preserved state.
             */
            val request = buildAuthorizeRequest(responseType = "token")

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.RedirectError>(result)
            assertEquals("unsupported_response_type", result.error)
            assertEquals("state-123", result.state)
        }

        @Test
        fun `oidcAuthorize requires openid scope`() {
            /*
             * Goal: Reject authorization requests that are not for OIDC.
             * Why: The openid scope is mandatory for OIDC; missing it is an invalid scope error.
             * How: Send a request with a non-openid scope and expect invalid_scope.
             */
            val request = buildAuthorizeRequest(scope = "profile")

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.RedirectError>(result)
            assertEquals("invalid_scope", result.error)
            assertEquals("state-123", result.state)
        }

        @Test
        fun `oidcAuthorize keeps missing state when returning error`() {
            /*
             * Goal: Keep missing state as null when an error response is returned.
             * Why: OIDC specifies state is optional and must not be invented by the server.
             * How: Send an invalid response_type with no state and verify the error has null state.
             */
            val request = buildAuthorizeRequest(responseType = "token", state = null)

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.RedirectError>(result)
            assertEquals("unsupported_response_type", result.error)
            assertEquals(null, result.state)
        }

        @Test
        fun `oidcAuthorize accepts missing nonce`() {
            /*
             * Goal: Accept requests without a nonce.
             * Why: Nonce is optional for authorization requests and often omitted by some clients.
             * How: Send a valid request with no nonce and verify it succeeds and stores null.
             */
            val request = buildAuthorizeRequest(nonce = null)

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.Valid>(result)
            val authCtx = env.oidcService.oidcAuthorizeFindAuthCtx(result.authCtxCode)
            assertNotNull(authCtx)
            assertEquals(null, authCtx.nonce)
        }

        @Test
        fun `oidcAuthorize accepts openid scope with extra values`() {
            /*
             * Goal: Accept a scope list as long as it includes "openid".
             * Why: OIDC allows additional scopes like profile/email alongside openid.
             * How: Send a scope with multiple values and verify it is accepted and stored.
             */
            val request = buildAuthorizeRequest(scope = "openid profile email")

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.Valid>(result)
            val authCtx = env.oidcService.oidcAuthorizeFindAuthCtx(result.authCtxCode)
            assertNotNull(authCtx)
            assertEquals("openid profile email", authCtx.scope)
        }

        @Test
        fun `oidcAuthorize rejects response_type casing mismatch`() {
            /*
             * Goal: Reject response_type values that do not exactly match "code".
             * Why: OIDC defines response_type tokens as case-sensitive.
             * How: Send response_type=Code and expect unsupported_response_type.
             */
            val request = buildAuthorizeRequest(responseType = "Code")

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.RedirectError>(result)
            assertEquals("unsupported_response_type", result.error)
        }

        @Test
        fun `oidcAuthorize rejects composite response_type`() {
            /*
             * Goal: Reject response_type values that imply implicit or hybrid flows.
             * Why: The server only supports authorization code flow (response_type=code).
             * How: Send response_type="code id_token" and expect unsupported_response_type.
             */
            val request = buildAuthorizeRequest(responseType = "code id_token")

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.RedirectError>(result)
            assertEquals("unsupported_response_type", result.error)
        }

        @Test
        fun `oidcAuthorize requires PKCE code challenge`() {
            /*
             * Goal: Enforce PKCE for the authorization code flow.
             * Why: Public clients should not exchange codes without a proof key to prevent interception.
             * How: Send a request without code_challenge and expect invalid_request.
             */
            val request = buildAuthorizeRequest(codeChallenge = null)

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.RedirectError>(result)
            assertEquals("invalid_request", result.error)
            assertEquals("state-123", result.state)
        }

        @Test
        fun `oidcAuthorize requires S256 PKCE method`() {
            /*
             * Goal: Validate the PKCE method advertised by the server.
             * Why: If the server only supports S256, other methods must be rejected.
             * How: Send code_challenge_method=plain and expect invalid_request.
             */
            val request = buildAuthorizeRequest(codeChallengeMethod = "plain")

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.RedirectError>(result)
            assertEquals("invalid_request", result.error)
            assertEquals("state-123", result.state)
        }

        @Test
        fun `oidcAuthorize rejects missing PKCE method`() {
            /*
             * Goal: Require an explicit PKCE method when a code challenge is provided.
             * Why: The server only supports S256 and must reject unspecified methods.
             * How: Send a request with no code_challenge_method and expect invalid_request.
             */
            val request = buildAuthorizeRequest(codeChallengeMethod = null)

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.RedirectError>(result)
            assertEquals("invalid_request", result.error)
            assertEquals("state-123", result.state)
        }

        @Test
        fun `oidcAuthorize rejects unknown client`() {
            /*
             * Goal: Reject clients that are not registered with the authorization server.
             * Why: OIDC requires the AS to authenticate/authorize clients before issuing codes.
             * How: Send a request with an unknown client_id and expect unauthorized_client.
             */
            val request = buildAuthorizeRequest(clientId = "unknown-client")

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.RedirectError>(result)
            assertEquals("unauthorized_client", result.error)
            assertEquals("state-123", result.state)
        }

        @Test
        fun `oidcAuthorize rejects unregistered redirect_uri`() {
            /*
             * Goal: Refuse redirect_uri values that do not match the registered client metadata.
             * Why: OIDC requires strict redirect_uri validation to prevent token leakage.
             * How: Send a request with a foreign redirect_uri and assert a fatal error (no redirect).
             */
            val request = buildAuthorizeRequest(redirectUri = "https://evil.example.test/callback")

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.FatalError>(result)
        }

        @Test
        fun `oidcAuthorize rejects redirect_uri with fragment`() {
            /*
             * Goal: Reject redirect_uri values containing fragments.
             * Why: OIDC forbids fragments in redirect_uri to avoid token leakage.
             * How: Send a redirect_uri with a fragment and assert a fatal error.
             */
            val request = buildAuthorizeRequest(
                redirectUri = publicBaseUrl.resolve("/authentication-callback#frag").toString()
            )

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.FatalError>(result)
        }

        @Test
        fun `oidcAuthorize rejects redirect_uri with different path`() {
            /*
             * Goal: Reject redirect_uri values that do not match the registered path.
             * Why: OIDC requires strict redirect_uri matching to prevent open redirects.
             * How: Send a redirect_uri with the same host but different path and assert a fatal error.
             */
            val request = buildAuthorizeRequest(
                redirectUri = publicBaseUrl.resolve("/other-callback").toString()
            )

            val result = env.oidcService.oidcAuthorize(request, publicBaseUrl)

            assertIs<OidcAuthorizeResult.FatalError>(result)
        }

        @Test
        fun `oidcAuthorize allows localhost redirect even when port differs`() {
            /*
             * Goal: Allow localhost redirect_uri for development tools even when the port differs.
             * Why: We explicitly relax port matching for localhost to reduce friction in local flows.
             * How: Use a localhost redirect_uri with a non-default port and verify it is accepted.
             */
            val request = buildAuthorizeRequest(
                redirectUri = "http://localhost:5678/authentication-callback"
            )

            val result = env.oidcService.oidcAuthorize(request, URI("http://localhost:8080"))

            assertIs<OidcAuthorizeResult.Valid>(result)
        }

    }

}
