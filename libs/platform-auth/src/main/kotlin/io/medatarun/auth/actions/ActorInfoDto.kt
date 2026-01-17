package io.medatarun.auth.actions

import io.medatarun.json.InstantAsIsoStringSerializer
import kotlinx.serialization.Serializable
import java.time.Instant


@Serializable
data class ActorInfoDto(
    val id: String,
    val issuer: String,
    val subject: String,
    val fullname: String,
    val email: String?,
    val roles: List<String>,
    val disabledAt: String?,
    @Serializable(with = InstantAsIsoStringSerializer::class)
    val createdAt: Instant,
    @Serializable(with = InstantAsIsoStringSerializer::class)
    val lastSeenAt: Instant
)