package io.medatarun.auth.oidc

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.medatarun.auth.domain.oidc.AuthTokenRequest
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.fixtures.AuthTestUtils
import io.medatarun.auth.internal.oidc.AuthClientRegistry
import io.medatarun.auth.internal.oidc.OidcAuthorizeResult
import io.medatarun.auth.ports.exposed.OIDCTokenResponseOrError
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import java.util.NoSuchElementException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@EnableDatabaseTests
class OidcAuthorizeCreateCodeTest {

    @Test
    fun `oidcAuthorizeCreateCode returns redirect with code and state`() {
        val env = AuthEnvTest()
        val publicBaseUrl = env.publicBaseUrl
        /*
         * Goal: Return a redirect URI containing a one-time authorization code and the original state.
         * Why: OIDC requires redirecting back to the client with code + state after user login.
         * How: Create an auth context, exchange it for a code, and verify redirect parameters and token exchange.
         */
        val fixedNow = env.authClockTests.now()
        val actor = env.createUserActor()
        val codeVerifier = "verifier-" + UuidUtils.generateV4String()
        val codeChallenge = AuthTestUtils.pkceChallengeForTest(codeVerifier)
        val request = env.buildAuthorizeRequest(codeChallenge = codeChallenge)

        val authorizeResult = env.oidcService.oidcAuthorize(request)
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(
            authorizeResult.authCtxCode,
            actor.subject
        )
        val params = AuthTestUtils.parseQueryParams(redirectLocation)
        val code = params["code"]

        assertNotNull(code)
        assertEquals("state-123", params["state"])

        val tokenResult = env.oidcService.oidcToken(
            AuthTokenRequest(
                grantType = "authorization_code",
                code = code,
                redirectUri = publicBaseUrl.resolve("/authentication-callback").toString(),
                clientId = AuthClientRegistry.oidcInternalClientId,
                codeVerifier = codeVerifier
            )
        )
        assertIs<OIDCTokenResponseOrError.Success>(tokenResult)
        val token = tokenResult.token

        val algorithm = Algorithm.RSA256(env.jwtKeyMaterial.publicKey, env.jwtKeyMaterial.privateKey)
        val verifier = JWT.require(algorithm)
            .withIssuer(env.jwtConfig.issuer)
            .withAudience(AuthClientRegistry.oidcInternalClientId)
            .withSubject(actor.subject)
            .build()
        val decoded = verifier.verify(token.idToken)

        assertEquals("nonce-123", decoded.getClaim("nonce").asString())
        assertEquals(env.authClockTests.staticNow.epochSecond, decoded.getClaim("auth_time").asLong())
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
        val env = AuthEnvTest()
        /*
         * Goal: Return a redirect URI without state when the original request did not include it.
         * Why: State is optional in OIDC and must not be added by the server.
         * How: Create an auth context with null state and verify the redirect only contains code.
         */
        val subject = env.createUserSubject()
        val codeVerifier = "verifier-" + UuidUtils.generateV4String()
        val codeChallenge = AuthTestUtils.pkceChallengeForTest(codeVerifier)
        val request = env.buildAuthorizeRequest(state = null, codeChallenge = codeChallenge)

        val authorizeResult = env.oidcService.oidcAuthorize(request)
        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)

        val redirectLocation = env.oidcService.oidcAuthorizeCreateCode(
            authorizeResult.authCtxCode,
            subject
        )
        val params = AuthTestUtils.parseQueryParams(redirectLocation)

        assertNotNull(params["code"])
        assertEquals(null, params["state"])
    }

    @Test
    fun `oidcAuthorizeCreateCode removes auth context`() {
        val env = AuthEnvTest()
        /*
         * Goal: Ensure the authorization context is one-time use.
         * Why: Reusing the same auth context would allow code replay.
         * How: After createCode, assert that the auth context cannot be found anymore.
         */
        val subject = env.createUserSubject()
        val codeVerifier = "verifier-" + UuidUtils.generateV4String()
        val codeChallenge = AuthTestUtils.pkceChallengeForTest(codeVerifier)
        val request = env.buildAuthorizeRequest(codeChallenge = codeChallenge)

        val authorizeResult = env.oidcService.oidcAuthorize(request)
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
        val env = AuthEnvTest()
        /*
         * Goal: Fail when the authorization context does not exist.
         * Why: Authorization codes must only be created from a valid authorization context.
         * How: Call createCode with a non-existent context id and expect a storage error.
         */
        val subject = "missing-subject-" + UuidUtils.generateV4String()

        assertFailsWith<NoSuchElementException> {
            env.oidcService.oidcAuthorizeCreateCode("missing-auth-ctx", subject)
        }
    }


}