package io.medatarun.auth.actions

import io.medatarun.lang.json.InstantAsIsoStringSerializer
import kotlinx.serialization.Serializable
import java.time.Instant


@Serializable
data class ActorDetailDto(
    val id: String,
    val issuer: String,
    val subject: String,
    val fullname: String,
    val email: String?,
    val roles: Set<String>,
    val permissions: Set<String>,
    val disabledAt: String?,
    @Serializable(with = InstantAsIsoStringSerializer::class)
    val createdAt: Instant,
    @Serializable(with = InstantAsIsoStringSerializer::class)
    val lastSeenAt: Instant
)
