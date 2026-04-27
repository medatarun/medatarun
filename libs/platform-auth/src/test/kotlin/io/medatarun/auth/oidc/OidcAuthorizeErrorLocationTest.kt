package io.medatarun.auth.oidc

import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.internal.oidc.OidcAuthorizeResult
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import kotlin.test.Test
import kotlin.test.assertEquals

@EnableDatabaseTests
class OidcAuthorizeErrorLocationTest {

    @Test
    fun `oidcAuthorizeErrorLocation encodes error and omits state when missing`() {
        val env = AuthEnvTest()
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
        val env = AuthEnvTest()
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