package io.medatarun.auth.internal.oidc

import java.net.URI
/**
 * public base URL must come from the application configuration
 * (environment variable or config properties)
 */
class OidcClientRegistry(private val publicBaseUrl: URI) {



    private val clients = listOf<OidcClient>(
        OidcClient(oidcInternalClientId, listOf(publicBaseUrl.resolve("/authentication-callback")))
    ).associateBy { it.clientId }

    fun find(clientId: String): OidcClient? {
        return clients[clientId]
    }

    fun exists(clientId: String): Boolean {
        return clients.containsKey(clientId)
    }

    fun matchesUri(clientId: String, redirectUriStr: String): Boolean {
        val client = clients[clientId] ?: return false
        val redirectUri: URI = URI(redirectUriStr)
        return client.redirectUris.any {
            it.scheme == redirectUri.scheme
                    && it.fragment == null
                    && it.host == redirectUri.host
                    && it.path == redirectUri.path
                    && (redirectUri.host == "localhost" || it.port == redirectUri.port)
        }
    }

    companion object {
        const val oidcInternalClientId = "medatarun-ui"
    }
}