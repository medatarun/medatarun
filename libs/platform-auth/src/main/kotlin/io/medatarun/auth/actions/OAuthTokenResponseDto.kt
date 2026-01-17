package io.medatarun.auth.actions

import io.medatarun.auth.ports.exposed.OAuthTokenResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class OAuthTokenResponseDto(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Long?
) {
    companion object {
        fun valueOf(other: OAuthTokenResponse): OAuthTokenResponseDto {
            return OAuthTokenResponseDto(
                accessToken = other.accessToken,
                tokenType = other.tokenType,
                expiresIn = other.expiresIn
            )
        }
    }


}