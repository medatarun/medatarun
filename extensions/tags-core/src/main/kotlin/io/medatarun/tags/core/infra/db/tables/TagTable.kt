package io.medatarun.tags.core.infra.db.tables

import org.jetbrains.exposed.v1.core.Table

internal object TagTable : Table("tag_projection") {
    val id = text("id")
    val scopeType = text("scope_type")
    val scopeId = text("scope_id").nullable()
    val tagGroupId = text("tag_group_id").nullable()
    val key = text("key")
    val name = text("name").nullable()
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}