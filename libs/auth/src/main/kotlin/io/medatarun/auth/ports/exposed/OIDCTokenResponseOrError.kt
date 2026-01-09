package io.medatarun.auth.ports.exposed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface OIDCTokenResponseOrError {
    class Success(val token: OIDCTokenResponse): OIDCTokenResponseOrError
    @Serializable
    class Error(
        @SerialName("error")
        val error: String,
        @SerialName("error_description")
        val errorDescription: String? = null) : OIDCTokenResponseOrError
}
