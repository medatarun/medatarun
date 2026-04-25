package io.medatarun.auth.infra.db.tables

import io.medatarun.auth.domain.role.RoleId
import io.medatarun.auth.domain.role.RoleKey
import io.medatarun.platform.db.exposed.IdTransformer
import io.medatarun.platform.db.exposed.instant
import io.medatarun.platform.db.exposed.KeyTransformer
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

internal object RoleTable : Table("auth_role") {
    val id = javaUUID("id").transform(IdTransformer(::RoleId))
    val key = varchar("key", 30).transform(KeyTransformer(::RoleKey))
    val name = varchar("name", 30)
    val description = text("description").nullable()
    val autoAssign = bool("auto_assign")
    val createdAt = instant("created_at")
    val lastUpdatedAt = instant("last_updated_at")
    override val primaryKey = PrimaryKey(id)
}
