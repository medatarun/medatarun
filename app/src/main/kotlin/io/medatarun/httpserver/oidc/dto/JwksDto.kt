package io.medatarun.httpserver.oidc.dto

import kotlinx.serialization.Serializable

@Serializable
data class JwksDto(val keys: List<JwkDto>)