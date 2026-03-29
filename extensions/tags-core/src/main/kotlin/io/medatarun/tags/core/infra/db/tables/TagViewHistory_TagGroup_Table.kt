package io.medatarun.tags.core.infra.db.tables

import io.medatarun.tags.core.domain.TagEventId
import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagGroupKey
import io.medatarun.tags.core.infra.db.types.IdTransformer
import io.medatarun.tags.core.infra.db.types.KeyTransformer
import io.medatarun.tags.core.infra.db.types.TagGroupHistoryProjectionId
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestamp

internal object TagViewHistory_TagGroup_Table : Table("tag_view_history_tag_group") {
    val id = javaUUID("id").transform(IdTransformer(::TagGroupHistoryProjectionId))
    val tagEventId = javaUUID("tag_event_id").transform(IdTransformer(::TagEventId))
    val tagGroupId = javaUUID("tag_group_id").transform(IdTransformer(::TagGroupId))
    val key = text("key").transform(KeyTransformer(::TagGroupKey))
    val name = text("name").nullable()
    val description = text("description").nullable()
    val validFrom = timestamp("valid_from")
    val validTo = timestamp("valid_to").nullable()

    override val primaryKey = PrimaryKey(id)
}
