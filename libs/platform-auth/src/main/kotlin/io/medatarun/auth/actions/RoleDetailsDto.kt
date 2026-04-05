package io.medatarun.auth.actions

import kotlinx.serialization.Serializable

@Serializable
data class RoleDetailsDto(
    val role: RoleInfoDto,
    val permissions: List<String>
)
