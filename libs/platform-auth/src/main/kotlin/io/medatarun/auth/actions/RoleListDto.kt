package io.medatarun.auth.actions

import kotlinx.serialization.Serializable

@Serializable
data class RoleListDto(
    val items: List<RoleInfoDto>
)
