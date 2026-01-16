package io.medatarun.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.medatarun.auth.domain.ActorNotFoundException
import io.medatarun.auth.domain.actor.Actor
import io.medatarun.auth.domain.oidc.OidcAuthorizeRequest
import io.medatarun.auth.domain.oidc.OidcTokenRequest
import io.medatarun.auth.domain.user.Fullname
import io.medatarun.auth.domain.user.PasswordClear
import io.medatarun.auth.domain.user.Username
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.infra.OidcStorageSQLite
import io.medatarun.auth.internal.actors.ActorClaimsAdapter
import io.medatarun.auth.internal.jwk.JwksAdapter
import io.medatarun.auth.internal.jwk.JwtExternalProvidersEmpty
import io.medatarun.auth.internal.jwk.JwtVerifierResolverImpl
import io.medatarun.auth.internal.oidc.OidcAuthorizeResult
import io.medatarun.auth.internal.oidc.OidcClientRegistry
import io.medatarun.auth.internal.oidc.OidcServiceImpl
import io.medatarun.auth.internal.oidc.OidcServiceImpl.Companion.JWKS_URI
import io.medatarun.auth.internal.oidc.OidcServiceImpl.Companion.OIDC_AUTHORIZE_URI
import io.medatarun.auth.internal.oidc.OidcServiceImpl.Companion.OIDC_WELL_KNOWN_OPEN_ID_CONFIGURATION
import io.medatarun.auth.ports.exposed.OIDCTokenResponseOrError
import io.medatarun.auth.ports.needs.OidcProviderConfig
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Nested
import java.net.URI
import java.net.URLDecoder
import java.security.MessageDigest
import java.util.*
import kotlin.test.*

class OidcServiceTest {

    val env = AuthEnvTest()
    val publicBaseUrl = URI("https://auth.example.test")


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

    @Test
    fun `oidcAuthority and oidcClientId use provider config when defined`() {
        /*
         * Goal: Prefer configured authority and client id over the public base URL.
         * Why: External IdP configuration must override the default internal endpoints.
         * How: Build a service with OidcProviderConfig and verify authority/clientId values.
         */
        val configuredAuthority = URI("https://issuer.example.test")
        val configuredClientId = "client-oidc"
        val providerConfig = OidcProviderConfig(configuredAuthority, configuredClientId)

        val service = OidcServiceImpl(
            oidcAuthCodeStorage = OidcStorageSQLite(env.dbConnectionFactory),
            actorClaimsAdapter = ActorClaimsAdapter(),
            oauthService = env.oauthService,
            authEmbeddedKeys = env.jwtKeyMaterial,
            jwtCfg = env.jwtConfig,
            clock = env.authClock,
            actorService = env.actorService,
            authCtxDurationSeconds = AuthExtension.DEFAULT_AUTH_CTX_DURATION_SECONDS,
            externalProviders = JwtExternalProvidersEmpty(),
            oidcProviderConfig = providerConfig
        )

        val authority = service.oidcAuthority(URI("https://public.example.test"))
        assertEquals(configuredAuthority, authority)
        assertEquals(configuredClientId, service.oidcClientId())
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

    @Nested inner class OidcAuthorizeErrorLocationTests {

        @Test
        fun `oidcAuthorizeErrorLocation encodes error and omits state when missing`() {
            /*
             * Goal: Build an error redirect URL without adding a state parameter.
             * Why: State is optional; the server must not invent it when absent.
             * How: Call oidcAuthorizeErrorLocation with null state and verify only the error appears.
             */
            val redirectUri = "https://client.example.test/callback"
            val error = "invalid_request"
            val result = OidcAuthorizeResult.RedirectError(
                redirectUri = redirectUri,
                error = error,
                state = null
            )

            val location = env.oidcService.oidcAuthorizeErrorLocation(result)

            assertEquals("$redirectUri?error=$error", location)
        }

        @Test
        fun `oidcAuthorizeErrorLocation encodes error and state`() {
            /*
             * Goal: Preserve error and state values while encoding them for URLs.
             * Why: OIDC requires returning error and optional state, and they must be URL-safe.
             * How: Use values with spaces and symbols and verify they are URL-encoded.
             */
            val redirectUri = "https://client.example.test/callback"
            val error = "invalid request"
            val state = "state: 123/456"
            val result = OidcAuthorizeResult.RedirectError(
                redirectUri = redirectUri,
                error = error,
                state = state
            )

            val location = env.oidcService.oidcAuthorizeErrorLocation(result)

            assertEquals(
                "$redirectUri?error=invalid+request&state=state%3A+123%2F456",
                location
            )
        }

    }

    @Nested inner class OidcAuthorizeCreateCodeTests {

        @Test
        fun `oidcAuthorizeCreateCode returns redirect with code and state`() {
            /*
             * Goal: Return a redirect URI containing a one-time authorization code and the original state.
             * Why: OIDC requires redirecting back to the client with code + state after user login.
             * How: Create an auth context, exchange it for a code, and verify redirect parameters and token exchange.
             */
            val fixedNow = env.authClock.now()
            val actor = createUserActor()
            val codeVerifier = "verifier-" + UUID.randomUUID()
            val codeChallenge = pkceChallengeForTest(codeVerifier)
            val request = buildAuthorizeRequest(codeChallenge = codeChallenge)

            val authorizeResult = env.oidcService.oidcAuthorize(request, publicBaseUrl)
            assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

            val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(
                authorizeResult.authCtxCode,
                actor.subject
            )
            val params = parseQueryParams(redirectLocation)
            val code = params["code"]

            assertNotNull(code)
            assertEquals("state-123", params["state"])

            val tokenResult = env.oidcService.oidcToken(
                OidcTokenRequest(
                    grantType = "authorization_code",
                    code = code,
                    redirectUri = publicBaseUrl.resolve("/authentication-callback").toString(),
                    clientId = OidcClientRegistry.oidcInternalClientId,
                    codeVerifier = codeVerifier
                )
            )
            assertIs<OIDCTokenResponseOrError.Success>(tokenResult)
            val token = tokenResult.token

            val algorithm = Algorithm.RSA256(env.jwtKeyMaterial.publicKey, env.jwtKeyMaterial.privateKey)
            val verifier = JWT.require(algorithm)
                .withIssuer(env.jwtConfig.issuer)
                .withAudience(OidcClientRegistry.oidcInternalClientId)
                .withSubject(actor.subject)
                .build()
            val decoded = verifier.verify(token.idToken)

            assertEquals("nonce-123", decoded.getClaim("nonce").asString())
            assertEquals(env.authClock.staticNow.epochSecond, decoded.getClaim("auth_time").asLong())
            assertEquals(actor.fullname, decoded.getClaim("name").asString())
            assertEquals(actor.id.value.toString(), decoded.getClaim("mid").asString())
            assertEquals(fixedNow.epochSecond, decoded.issuedAt.toInstant().epochSecond)
            assertEquals(
                fixedNow.plusSeconds(env.jwtConfig.ttlSeconds).epochSecond,
                decoded.expiresAt.toInstant().epochSecond
            )
        }

        @Test
        fun `oidcAuthorizeCreateCode omits state when missing`() {
            /*
             * Goal: Return a redirect URI without state when the original request did not include it.
             * Why: State is optional in OIDC and must not be added by the server.
             * How: Create an auth context with null state and verify the redirect only contains code.
             */
            val subject = createUserSubject()
            val codeVerifier = "verifier-" + UUID.randomUUID()
            val codeChallenge = pkceChallengeForTest(codeVerifier)
            val request = buildAuthorizeRequest(state = null, codeChallenge = codeChallenge)

            val authorizeResult = env.oidcService.oidcAuthorize(request, publicBaseUrl)
            assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

            val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(
                authorizeResult.authCtxCode,
                subject
            )
            val params = parseQueryParams(redirectLocation)

            assertNotNull(params["code"])
            assertEquals(null, params["state"])
        }

        @Test
        fun `oidcAuthorizeCreateCode removes auth context`() {
            /*
             * Goal: Ensure the authorization context is one-time use.
             * Why: Reusing the same auth context would allow code replay.
             * How: After createCode, assert that the auth context cannot be found anymore.
             */
            val subject = createUserSubject()
            val codeVerifier = "verifier-" + UUID.randomUUID()
            val codeChallenge = pkceChallengeForTest(codeVerifier)
            val request = buildAuthorizeRequest(codeChallenge = codeChallenge)

            val authorizeResult = env.oidcService.oidcAuthorize(request, publicBaseUrl)
            assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

            env.oidcService.oidcAuthorizeCreateCode(
                authorizeResult.authCtxCode,
                subject
            )

            assertFailsWith<NoSuchElementException> {
                env.oidcService.oidcAuthorizeFindAuthCtx(authorizeResult.authCtxCode)
            }
        }

        @Test
        fun `oidcAuthorizeCreateCode fails with unknown auth context`() {
            /*
             * Goal: Fail when the authorization context does not exist.
             * Why: Authorization codes must only be created from a valid authorization context.
             * How: Call createCode with a non-existent context id and expect a storage error.
             */
            val subject = "missing-subject-" + UUID.randomUUID()

            assertFailsWith<NoSuchElementException> {
                env.oidcService.oidcAuthorizeCreateCode("missing-auth-ctx", subject)
            }
        }

    }

    @Nested inner class OidcTokenTests {

        @Test
        fun `oidcToken rejects unsupported grant_type`() {
            /*
             * Goal: Reject token exchanges when grant_type is not authorization_code.
             * Why: OIDC token endpoint must only accept the authorization_code grant in our server.
             * How: Send a valid code with an unsupported grant_type and expect invalid_grant.
             */
            val codeVerifier = "verifier-" + UUID.randomUUID()
            val fixture = createAuthorizationCodeFixture(codeVerifier)

            val response = env.oidcService.oidcToken(
                OidcTokenRequest(
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
            /*
             * Goal: Reject unknown authorization codes.
             * Why: OIDC requires codes to be previously issued by the authorization endpoint.
             * How: Call oidcToken with a random code and expect invalid_grant.
             */
            val response = env.oidcService.oidcToken(
                OidcTokenRequest(
                    grantType = "authorization_code",
                    code = "missing-code",
                    redirectUri = publicBaseUrl.resolve("/authentication-callback").toString(),
                    clientId = OidcClientRegistry.oidcInternalClientId,
                    codeVerifier = "verifier"
                )
            )

            assertIs<OIDCTokenResponseOrError.Error>(response)
            assertEquals("invalid_grant", response.error)
        }

        @Test
        fun `oidcToken rejects expired code`() {
            /*
             * Goal: Reject expired authorization codes.
             * Why: OIDC requires codes to be short-lived and unusable after expiration.
             * How: Move the clock past expiration and expect invalid_grant.
             */
            val codeVerifier = "verifier-" + UUID.randomUUID()
            val fixture = createAuthorizationCodeFixture(codeVerifier)
            val originalNow = env.authClock.staticNow
            env.authClock.staticNow = env.authClock.staticNow.plusSeconds(60 * 60)

            val response = try {
                env.oidcService.oidcToken(
                    OidcTokenRequest(
                        grantType = "authorization_code",
                        code = fixture.code,
                        redirectUri = fixture.redirectUri,
                        clientId = fixture.clientId,
                        codeVerifier = codeVerifier
                    )
                )
            } finally {
                env.authClock.staticNow = originalNow
            }

            assertIs<OIDCTokenResponseOrError.Error>(response)
            assertEquals("invalid_grant", response.error)
        }

        @Test
        fun `oidcToken rejects mismatched client_id`() {
            /*
             * Goal: Reject token exchange when the client_id does not match the issued code.
             * Why: OIDC ties authorization codes to a client to prevent token theft.
             * How: Use a valid code but send a different client_id and expect invalid_grant.
             */
            val codeVerifier = "verifier-" + UUID.randomUUID()
            val fixture = createAuthorizationCodeFixture(codeVerifier)

            val response = env.oidcService.oidcToken(
                OidcTokenRequest(
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
            /*
             * Goal: Reject token exchange when redirect_uri does not match the issued code.
             * Why: OIDC binds the code to the redirect_uri to prevent code injection.
             * How: Use a valid code but send a different redirect_uri and expect invalid_grant.
             */
            val codeVerifier = "verifier-" + UUID.randomUUID()
            val fixture = createAuthorizationCodeFixture(codeVerifier)

            val response = env.oidcService.oidcToken(
                OidcTokenRequest(
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
            /*
             * Goal: Reject token exchange when PKCE verification fails.
             * Why: PKCE prevents authorization code interception attacks.
             * How: Use a valid code but provide an incorrect code_verifier and expect invalid_grant.
             */
            val codeVerifier = "verifier-" + UUID.randomUUID()
            val fixture = createAuthorizationCodeFixture(codeVerifier)

            val response = env.oidcService.oidcToken(
                OidcTokenRequest(
                    grantType = "authorization_code",
                    code = fixture.code,
                    redirectUri = fixture.redirectUri,
                    clientId = fixture.clientId,
                    codeVerifier = "wrong-" + UUID.randomUUID()
                )
            )

            assertIs<OIDCTokenResponseOrError.Error>(response)
            assertEquals("invalid_grant", response.error)
        }

        @Test
        fun `oidcToken returns error when actor is missing`() {
            /*
             * Goal: Fail when the subject behind the authorization code cannot be found.
             * Why: OIDC token issuance requires a valid subject to populate claims.
             * How: Create a code for a non-existent subject and expect ActorNotFoundException on token exchange.
             */
            val subject = "missing-subject-" + UUID.randomUUID()
            val codeVerifier = "verifier-" + UUID.randomUUID()
            val codeChallenge = pkceChallengeForTest(codeVerifier)
            val request = buildAuthorizeRequest(codeChallenge = codeChallenge)

            val authorizeResult = env.oidcService.oidcAuthorize(request, publicBaseUrl)
            assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

            val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(
                authorizeResult.authCtxCode,
                subject
            )
            val params = parseQueryParams(redirectLocation)
            val code = params["code"]
            assertNotNull(code)

            assertFailsWith<ActorNotFoundException> {
                env.oidcService.oidcToken(
                    OidcTokenRequest(
                        grantType = "authorization_code",
                        code = code,
                        redirectUri = publicBaseUrl.resolve("/authentication-callback").toString(),
                        clientId = OidcClientRegistry.oidcInternalClientId,
                        codeVerifier = codeVerifier
                    )
                )
            }
        }

        @Test
        fun `oidcToken returns expected token fields`() {
            /*
             * Goal: Return a complete OIDC token response with expected fields.
             * Why: Clients rely on token_type, expires_in, and access_token for subsequent API calls.
             * How: Exchange a valid code and verify response fields match the server configuration.
             */
            val codeVerifier = "verifier-" + UUID.randomUUID()
            val fixture = createAuthorizationCodeFixture(codeVerifier)

            val response = env.oidcService.oidcToken(
                OidcTokenRequest(
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
            /*
             * Goal: Ensure nonce is not added when the authorization request did not include it.
             * Why: OIDC requires nonce to be echoed only when provided by the client.
             * How: Create a code with null nonce and verify the ID token has no nonce claim.
             */
            val actor = createUserActor()
            val codeVerifier = "verifier-" + UUID.randomUUID()
            val request = buildAuthorizeRequest(
                nonce = null,
                state = null,
                codeChallenge = pkceChallengeForTest(codeVerifier)
            )

            val authorizeResult = env.oidcService.oidcAuthorize(request, publicBaseUrl)
            assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

            val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(
                authorizeResult.authCtxCode,
                actor.subject
            )
            val params = parseQueryParams(redirectLocation)
            val code = params["code"]
            assertNotNull(code)

            val response = env.oidcService.oidcToken(
                OidcTokenRequest(
                    grantType = "authorization_code",
                    code = code,
                    redirectUri = publicBaseUrl.resolve("/authentication-callback").toString(),
                    clientId = OidcClientRegistry.oidcInternalClientId,
                    codeVerifier = codeVerifier
                )
            )

            assertIs<OIDCTokenResponseOrError.Success>(response)
            val decoded = JWT.decode(response.token.idToken)
            assertTrue(decoded.getClaim("nonce").isMissing || decoded.getClaim("nonce").isNull)
        }

        @Test
        fun `oidcToken rejects reused code`() {
            /*
             * Goal: Reject reuse of the same authorization code.
             * Why: OIDC requires codes to be single-use to prevent replay attacks.
             * How: Exchange a valid code once, then reuse it and expect invalid_grant.
             */
            val codeVerifier = "verifier-" + UUID.randomUUID()
            val fixture = createAuthorizationCodeFixture(codeVerifier)

            val first = env.oidcService.oidcToken(
                OidcTokenRequest(
                    grantType = "authorization_code",
                    code = fixture.code,
                    redirectUri = fixture.redirectUri,
                    clientId = fixture.clientId,
                    codeVerifier = codeVerifier
                )
            )
            assertIs<OIDCTokenResponseOrError.Success>(first)

            val second = env.oidcService.oidcToken(
                OidcTokenRequest(
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

    }

    // ------------------------------------------------------------------------
    // Tests helpers
    // ------------------------------------------------------------------------

    data class AuthorizationCodeFixture(
        val code: String,
        val subject: String,
        val redirectUri: String,
        val clientId: String
    )

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

    private fun pkceChallengeForTest(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(verifier.toByteArray(Charsets.US_ASCII))
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(hash)
    }

    private fun parseQueryParams(uri: String): Map<String, String> {
        val query = URI(uri).rawQuery ?: return emptyMap()
        val params = mutableMapOf<String, String>()
        val pairs = query.split("&")
        for (pair in pairs) {
            if (pair.isEmpty()) {
                continue
            }
            val splitIndex = pair.indexOf("=")
            if (splitIndex < 0) {
                val key = URLDecoder.decode(pair, Charsets.UTF_8)
                params[key] = ""
            } else {
                val key = URLDecoder.decode(pair.substring(0, splitIndex), Charsets.UTF_8)
                val value = URLDecoder.decode(pair.substring(splitIndex + 1), Charsets.UTF_8)
                params[key] = value
            }
        }
        return params
    }

    private fun createUserActor(): Actor {
        val username = Username("oidc-user-" + UUID.randomUUID())
        val fullname = Fullname("Oidc User")
        val password = PasswordClear("oidc-pass-" + UUID.randomUUID())
        env.userService.createEmbeddedUser(username, fullname, password, false)
        val actor = env.actorService.findByIssuerAndSubjectOptional(env.jwtConfig.issuer, username.value)
        assertNotNull(actor)
        return actor
    }

    private fun createUserSubject(): String {
        return createUserActor().subject
    }

    private fun createAuthorizationCodeFixture(
        codeVerifier: String,
        state: String? = "state-123"
    ): AuthorizationCodeFixture {
        val actor = createUserActor()
        val codeChallenge = pkceChallengeForTest(codeVerifier)
        val request = buildAuthorizeRequest(
            state = state,
            codeChallenge = codeChallenge
        )

        val authorizeResult = env.oidcService.oidcAuthorize(request, publicBaseUrl)
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(
            authorizeResult.authCtxCode,
            actor.subject
        )
        val params = parseQueryParams(redirectLocation)
        val code = params["code"]
        assertNotNull(code)

        return AuthorizationCodeFixture(
            code = code,
            subject = actor.subject,
            redirectUri = publicBaseUrl.resolve("/authentication-callback").toString(),
            clientId = OidcClientRegistry.oidcInternalClientId
        )
    }
}
