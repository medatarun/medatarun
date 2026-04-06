package io.medatarun.auth.actions.dto

import io.medatarun.lang.json.InstantAsIsoStringSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val fullname: String,
    val admin: Boolean,
    @Serializable(with = InstantAsIsoStringSerializer::class)
    val disabledDate: Instant?
)