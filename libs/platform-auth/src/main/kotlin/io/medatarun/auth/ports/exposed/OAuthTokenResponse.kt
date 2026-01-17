package io.medatarun.auth.ports.exposed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response for OAuth token, not OIDC token.
 *
 * Contains access_token, token_type and expires_in but no id_token (because it's not OIDC)
 */
@Serializable
data class OAuthTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Long?
)