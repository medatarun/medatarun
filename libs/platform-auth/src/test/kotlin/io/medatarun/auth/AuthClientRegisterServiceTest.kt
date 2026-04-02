package io.medatarun.auth

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.auth.fixtures.AuthEnvTest
import io.medatarun.auth.internal.oidc.OidcAuthorizeResult
import io.medatarun.auth.domain.ConfigProperties
import io.medatarun.auth.ports.exposed.OidcClientRegistrationResponseOrError
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.net.URI
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@EnableDatabaseTests
class AuthClientRegisterServiceTest {



    @Test
    fun `oidcRegister stores dynamic client in memory and allows authorize`() {
        val env = AuthEnvTest()
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

        val authorizeResult = env.oidcService.oidcAuthorize(authorizeRequest)

        assertIs<OidcAuthorizeResult.Valid>(authorizeResult)
    }

    @Test
    fun `oidcRegister accepts metadata that includes authorization_code and code among extra values`() {
        val env = AuthEnvTest()
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

    @Test
    fun `oidcRegister purges inactive dynamic clients based on retention days`() {
        val envWithRetention = AuthEnvTest(
            extraProps = mapOf(
                ConfigProperties.ClientRegistrationRetentionDays.key to "7"
            ),
            publicBaseUrl = URI("https://auth.example.test")
        )

        val firstNow = Instant.parse("2026-01-01T00:00:00Z")
        envWithRetention.authClockTests.staticNow = firstNow

        val firstClientRedirectUri = "http://127.0.0.1:8788/first-callback"
        val firstRegistrationResult = envWithRetention.oidcService.oidcRegister(
            buildJsonObject {
                putJsonArray("redirect_uris") { add(firstClientRedirectUri) }
                putJsonArray("grant_types") { add("authorization_code") }
                putJsonArray("response_types") { add("code") }
                put("token_endpoint_auth_method", "none")
                put("client_name", "first client")
            }
        )
        assertIs<OidcClientRegistrationResponseOrError.Success>(firstRegistrationResult)

        val firstAuthorizeResultBeforePurge = envWithRetention.oidcService.oidcAuthorize(
            envWithRetention.buildAuthorizeRequest(
                clientId = firstRegistrationResult.registration.clientId,
                redirectUri = firstClientRedirectUri
            )
        )
        assertIs<OidcAuthorizeResult.Valid>(firstAuthorizeResultBeforePurge)

        envWithRetention.authClockTests.staticNow = firstNow.plusSeconds(8L * 24 * 60 * 60)

        val secondClientRedirectUri = "http://127.0.0.1:8788/second-callback"
        val secondRegistrationResult = envWithRetention.oidcService.oidcRegister(
            buildJsonObject {
                putJsonArray("redirect_uris") { add(secondClientRedirectUri) }
                putJsonArray("grant_types") { add("authorization_code") }
                putJsonArray("response_types") { add("code") }
                put("token_endpoint_auth_method", "none")
                put("client_name", "second client")
            }
        )
        assertIs<OidcClientRegistrationResponseOrError.Success>(secondRegistrationResult)

        val firstAuthorizeResultAfterPurge = envWithRetention.oidcService.oidcAuthorize(
            envWithRetention.buildAuthorizeRequest(
                clientId = firstRegistrationResult.registration.clientId,
                redirectUri = firstClientRedirectUri
            )
        )
        assertIs<OidcAuthorizeResult.RedirectError>(firstAuthorizeResultAfterPurge)
        assertEquals("unauthorized_client", firstAuthorizeResultAfterPurge.error)
    }

}
