package io.medatarun.auth.domain.role

import java.time.Instant

/**
 * Read model for an authorization role.
 */
interface Role {
    val id: RoleId
    val key: RoleKey
    val name: String
    val description: String?
    val createdAt: Instant
    val lastUpdatedAt: Instant
}
