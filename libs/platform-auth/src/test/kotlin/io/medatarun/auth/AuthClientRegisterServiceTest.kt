package io.medatarun.auth

import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.internal.oidc.OidcAuthorizeResult
import io.medatarun.auth.ports.exposed.OidcClientRegistrationResponseOrError
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addAll
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthClientRegisterServiceTest {

    val env = AuthEnvTest()
    val publicBaseUrl = URI("https://auth.example.test")

    @Test
    fun `oidcRegister stores dynamic client in memory and allows authorize`() {
        val redirectUri = "http://127.0.0.1:8788/callback"
        val registrationRequest = buildJsonObject {
            putJsonArray("redirect_uris") { add(redirectUri) }
            putJsonArray("grant_types") { add("authorization_code") }
            putJsonArray("response_types") { add("code") }
            put("token_endpoint_auth_method", "none")
            put("client_name", "Codex local")

        }

        val registrationResult = env.oidcService.oidcRegister(registrationRequest)

        assertIs<OidcClientRegistrationResponseOrError.Success>(registrationResult)
        assertEquals(listOf(redirectUri), registrationResult.registration.redirectUris)
        assertEquals(listOf("authorization_code"), registrationResult.registration.grantTypes)
        assertEquals(listOf("code"), registrationResult.registration.responseTypes)
        assertEquals("none", registrationResult.registration.tokenEndpointAuthMethod)
        assertEquals("Codex local", registrationResult.registration.clientName)

        val authorizeRequest = env.buildAuthorizeRequest(
            clientId = registrationResult.registration.clientId,
            redirectUri = redirectUri
        )

        val authorizeResult = env.oidcService.oidcAuthorize(authorizeRequest, publicBaseUrl)

        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)
    }

    @Test
    fun `oidcRegister accepts metadata that includes authorization_code and code among extra values`() {
        val registrationResult = env.oidcService.oidcRegister(
            buildJsonObject {
                putJsonArray("redirect_uris") { add("http://127.0.0.1:8788/callback") }
                putJsonArray("grant_types") { add("authorization_code"); add("refresh_token") }
                putJsonArray("response_types") { add("code"); add( "id_token") }
                put("token_endpoint_auth_method", "none")
                put("client_name", "Codex local")
            }
        )

        assertIs<OidcClientRegistrationResponseOrError.Success>(registrationResult)
        assertTrue(registrationResult.registration.grantTypes.contains("authorization_code"))
        assertTrue(registrationResult.registration.responseTypes.contains("code"))
    }

}