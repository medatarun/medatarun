package io.medatarun.tags.core.infra.db.tables

import org.jetbrains.exposed.v1.core.Table

internal object TagGroupHistoryProjectionTable : Table("tag_group_history_projection") {
    val id = text("id")
    val tagEventId = text("tag_event_id")
    val tagGroupId = text("tag_group_id")
    val key = text("key")
    val name = text("name").nullable()
    val description = text("description").nullable()
    val validFrom = text("valid_from")
    val validTo = text("valid_to").nullable()

    override val primaryKey = PrimaryKey(id)
}
