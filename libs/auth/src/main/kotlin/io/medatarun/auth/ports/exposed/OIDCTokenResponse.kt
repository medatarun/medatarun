package io.medatarun.auth.ports.exposed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response for OIDC token request.
 *
 * Contains id_token and access_token, token_type and expires_in
 *
 * - id_token: for OIDC clients to know about session
 * - access_token: for further API calls
 */
@Serializable
data class OIDCTokenResponse(
    @SerialName("id_token")
    val idToken: String,
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Long?
)