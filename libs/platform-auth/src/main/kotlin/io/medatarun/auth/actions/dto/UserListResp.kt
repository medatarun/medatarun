package io.medatarun.auth.actions.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserListResp(val items: List<UserDto>)
