package io.medatarun.auth.infra.db.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestamp

internal object RoleTable : Table("auth_role") {
    val id = javaUUID("id")
    val name = varchar("name", 30)
    val description = text("description").nullable()
    val createdAt = timestamp("created_at")
    val lastUpdatedAt = timestamp("last_updated_at")
}