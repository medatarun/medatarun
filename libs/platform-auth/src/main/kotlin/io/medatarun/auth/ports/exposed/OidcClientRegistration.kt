package io.medatarun.auth.ports.exposed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class OidcClientRegistrationResponse(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("redirect_uris")
    val redirectUris: List<String>,
    @SerialName("grant_types")
    val grantTypes: List<String>,
    @SerialName("response_types")
    val responseTypes: List<String>,
    @SerialName("token_endpoint_auth_method")
    val tokenEndpointAuthMethod: String,
    @SerialName("client_name")
    val clientName: String?,
    @SerialName("client_id_issued_at")
    val clientIdIssuedAt: Long
)

sealed interface OidcClientRegistrationResponseOrError {
    class Success(val registration: OidcClientRegistrationResponse) : OidcClientRegistrationResponseOrError

    @Serializable
    class Error(
        @SerialName("error")
        val error: String,
        @SerialName("error_description")
        val errorDescription: String
    ) : OidcClientRegistrationResponseOrError
}
