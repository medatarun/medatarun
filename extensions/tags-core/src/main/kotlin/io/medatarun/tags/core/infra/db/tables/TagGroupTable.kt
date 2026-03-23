package io.medatarun.tags.core.infra.db.tables

import org.jetbrains.exposed.v1.core.Table

internal object TagGroupTable : Table("tag_group") {
    val id = text("id")
    val key = text("key")
    val name = text("name").nullable()
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}