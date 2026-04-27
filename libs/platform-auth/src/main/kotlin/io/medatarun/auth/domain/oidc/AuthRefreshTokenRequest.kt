package io.medatarun.auth.domain.oidc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthRefreshTokenRequest(
    /**
     * Must be "authorization_code" only
     *
     * We don't support "refresh_token" on ne support pas autre chose
     */
    @SerialName("grant_type")
    val grantType: String,

    /**
     * Identifier of the client application
     */
    @SerialName("client_id")
    val clientId: String,

    /**
     * "Refresh token" previously sent with the previous token so we can rotate tokens
     */
    @SerialName("refresh_token")
    val refreshToken: String
)