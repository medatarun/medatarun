package io.medatarun.auth.internal.oidc

import io.medatarun.auth.ports.needs.AuthClock
import java.net.URI

class AuthClientStorageInternal(
    private val publicBaseUrl: URI,
    private val clock: AuthClock
) : AuthClientStorage {
    override fun canRegister(): Boolean = false
    override fun register(client: AuthClient) {
        // do nothing
    }

    override fun findById(clientId: String): AuthClient? {
        if (clientId != AuthClientRegistry.oidcInternalClientId) return null
        val now = clock.now()
        return AuthClient(
            clientId = AuthClientRegistry.oidcInternalClientId,
            origin = OidcClientOrigin.INTERNAL,
            originalRegistrationPayload = null,
            createdAt = now,
            lastUsedAt = now,
            redirectUris = listOf(publicBaseUrl.resolve("/authentication-callback")),
            grantTypes = listOf(AuthClientRegistry.AUTHORIZATION_CODE_GRANT_TYPE),
            responseTypes = listOf(AuthClientRegistry.AUTHORIZATION_CODE_RESPONSE_TYPE),
            tokenEndpointAuthMethod = AuthClientRegistry.TOKEN_ENDPOINT_AUTH_METHOD_NONE,
            clientName = "Medatarun UI",
            clientUri = publicBaseUrl,
            logoUri = null,
            contacts = emptyList(),
            softwareId = null,
            softwareVersion = null,
            tosURI = null,
            policyURI = null
        )
    }

    override fun exists(clientId: String): Boolean {
        return clientId == AuthClientRegistry.oidcInternalClientId
    }

}
