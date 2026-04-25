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
    /**
     * At most one role may be flagged for automatic assignment to newly created actors.
     */
    val autoAssign: Boolean
    val createdAt: Instant
    val lastUpdatedAt: Instant
}
