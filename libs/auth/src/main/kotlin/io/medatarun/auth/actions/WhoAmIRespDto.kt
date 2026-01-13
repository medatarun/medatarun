package io.medatarun.auth.actions

import kotlinx.serialization.Serializable

@Serializable
data class WhoAmIRespDto(
    val issuer: String,
    val sub: String,
    val fullname: String,
    val admin: Boolean,
    val roles: List<String>,
)