package io.medatarun.auth.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OIDCTokenRequest(
    /**
     * Must be "authorization_code" only
     *
     * We don't support "refresh_token" on ne support pas autre chose
     */
    @SerialName("grant_type")
    val grantType:String,
    /**
     *
     */
    @SerialName("code")
    val code: String,

    @SerialName("redirect_uri")
    val redirectUri:String,

    /**
     * Identifier of the client application
     */
    @SerialName("client_id")
    val clientId:String,

    /**
     * Code that authorize request returned
     */
    @SerialName("code_verifier")
    val codeVerifier:String
)