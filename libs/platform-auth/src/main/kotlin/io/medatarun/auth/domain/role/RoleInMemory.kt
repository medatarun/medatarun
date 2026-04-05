package io.medatarun.auth.domain.role

import java.time.Instant

data class RoleInMemory(
    override val id: RoleId,
    override val key: RoleKey,
    override val name: String,
    override val description: String?,
    override val createdAt: Instant,
    override val lastUpdatedAt: Instant
) : Role