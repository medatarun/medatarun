package io.medatarun.auth.actions

import io.medatarun.lang.json.InstantAsIsoStringSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class RoleInfoDto(
    val id: String,
    val key: String,
    val name: String,
    val description: String?,
    val autoAssign: Boolean,
    val managedRole: Boolean,
    @Serializable(with = InstantAsIsoStringSerializer::class)
    val createdAt: Instant,
    @Serializable(with = InstantAsIsoStringSerializer::class)
    val lastUpdatedAt: Instant
)
