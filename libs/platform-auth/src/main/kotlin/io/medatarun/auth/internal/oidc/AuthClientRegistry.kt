package io.medatarun.auth.internal.oidc

import io.medatarun.auth.infra.db.AuthClientStorageDb
import io.medatarun.auth.ports.exposed.OidcClientRegistrationResponse
import io.medatarun.auth.ports.exposed.OidcClientRegistrationResponseOrError
import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.type.commons.json.getStringListOrNull
import io.medatarun.type.commons.json.getStringOrNull
import io.medatarun.type.commons.json.getURIListOrNull
import io.medatarun.type.commons.json.getURIOrNull
import kotlinx.serialization.json.JsonObject
import java.net.URI

/**
 * Dynamic registrations are kept in memory and the built-in Medatarun UI client is re-derived
 * from the current public base URL when needed so redirects keep matching the active deployment URL.
 */
class AuthClientRegistry(
    private val storages: List<AuthClientStorage>
) {


    @Synchronized
    fun find(clientId: String): AuthClient? {
        return storages.firstNotNullOfOrNull { it.findById(clientId) }
    }

    @Synchronized
    fun exists(clientId: String): Boolean {
        return storages.any { it.exists(clientId) }
    }

    @Synchronized
    fun matchesUri(clientId: String, redirectUriStr: String): Boolean {
        val client = find(clientId) ?: return false
        val redirectUri = URI(redirectUriStr)
        return client.redirectUris.any {
            it.scheme == redirectUri.scheme
                    && it.fragment == null
                    && it.host == redirectUri.host
                    && it.path == redirectUri.path
                    && (redirectUri.host == "localhost" || it.port == redirectUri.port)
        }
    }

    /**
     * Registration is intentionally in-memory for now so we can validate the OAuth wiring end-to-end
     * without committing to a storage format before the protocol contract stabilizes.
     */
    @Synchronized
    fun registerDynamicClient(
        request: JsonObject,
        nowEpochSeconds: Long
    ): OidcClientRegistrationResponseOrError {

        // Spec says that clients using flows with redirection MUST register
        // their redirection URI values
        val redirectUris = request.getURIListOrNull("redirect_uris")
            ?: return OidcClientRegistrationResponseOrError.Error("invalid_client_metadata", "redirect_uris")
        if (redirectUris.isEmpty()) {
            return OidcClientRegistrationResponseOrError.Error("invalid_client_metadata", "redirect_uris")
        }
        for (redirectUri in redirectUris) {
            if (!redirectUri.isAbsolute) {
                return OidcClientRegistrationResponseOrError.Error("invalid_redirect_uri", redirectUri.toString())
            }
            if (redirectUri.fragment != null) {
                return OidcClientRegistrationResponseOrError.Error("invalid_redirect_uri", redirectUri.toString())
            }
        }

        // Spec says If unspecified or omitted, the default is "client_secret_basic"
        // But we don't know how to handle that
        val tokenEndpointAuthMethod =
            request.getStringOrNull("token_endpoint_auth_method") ?: TOKEN_ENDPOINT_AUTH_METHOD_NONE
        if (tokenEndpointAuthMethod != TOKEN_ENDPOINT_AUTH_METHOD_NONE) {
            return OidcClientRegistrationResponseOrError.Error("invalid_client_metadata", "token_endpoint_auth_method")
        }

        // Spec explicitly says that If omitted, the default behavior is that the
        // client will use only the "authorization_code" Grant Type.
        val grantTypes = request.getStringListOrNull("grant_types") ?: listOf(AUTHORIZATION_CODE_GRANT_TYPE)
        if (!grantTypes.contains(AUTHORIZATION_CODE_GRANT_TYPE)) {
            return OidcClientRegistrationResponseOrError.Error("invalid_client_metadata", "grant_types")
        }

        // Spec says: if omitted, the default is that the client will use only the "code" response type.
        val responseTypes = request.getStringListOrNull("response_types") ?: listOf(AUTHORIZATION_CODE_RESPONSE_TYPE)
        if (!responseTypes.contains(AUTHORIZATION_CODE_RESPONSE_TYPE)) {
            return OidcClientRegistrationResponseOrError.Error("invalid_client_metadata", "response_types")
        }


        val clientName = request.getStringOrNull("client_name")
        val clientUri = request.getURIOrNull("client_uri")
        val logoUri = request.getURIOrNull("logo_uri")
        val contacts = request.getStringListOrNull("contacts") ?: emptyList()
        val softwareId = request.getStringOrNull("software_id")
        val softwareVersion = request.getStringOrNull("software_version")
        val tosUri = request.getURIOrNull("tos_uri")
        val policyUri = request.getURIOrNull("policy_uri")
        val originalPayload = request


        val client = AuthClient(
            clientId = "dcpr-" + UuidUtils.generateV4String(),
            origin = OidcClientOrigin.DCRP,
            originalRegistrationPayload = originalPayload,
            redirectUris = redirectUris,
            grantTypes = grantTypes,
            responseTypes = responseTypes,
            tokenEndpointAuthMethod = tokenEndpointAuthMethod,
            clientName = clientName,
            clientUri = clientUri,
            logoUri = logoUri,
            contacts = contacts,
            softwareId = softwareId,
            softwareVersion = softwareVersion,
            tosURI = tosUri,
            policyURI = policyUri

        )
        register(client)
        return OidcClientRegistrationResponseOrError.Success(
            OidcClientRegistrationResponse(
                clientId = client.clientId,
                redirectUris = redirectUris.map { it.toString() },
                grantTypes = client.grantTypes,
                responseTypes = client.responseTypes,
                tokenEndpointAuthMethod = client.tokenEndpointAuthMethod,
                clientName = client.clientName,
                clientIdIssuedAt = nowEpochSeconds
            )
        )
    }

    @Synchronized
    private fun register(client: AuthClient) {
        storages.filter { it.canRegister() }.firstOrNull()?.register(client)

    }

    companion object {
        const val oidcInternalClientId = "medatarun-ui"
        const val AUTHORIZATION_CODE_GRANT_TYPE = "authorization_code"
        const val AUTHORIZATION_CODE_RESPONSE_TYPE = "code"
        const val TOKEN_ENDPOINT_AUTH_METHOD_NONE = "none"
    }
}
