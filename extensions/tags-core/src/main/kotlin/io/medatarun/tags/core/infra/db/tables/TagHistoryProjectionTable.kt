package io.medatarun.tags.core.infra.db.tables

import io.medatarun.tags.core.domain.TagGroupId
import io.medatarun.tags.core.domain.TagEventId
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagKey
import io.medatarun.tags.core.domain.TagScopeId
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestamp

internal object TagHistoryProjectionTable : Table("tag_history_projection") {
    val id = javaUUID("id").transform(IdTransformer(::TagHistoryProjectionId))
    val tagEventId = javaUUID("tag_event_id").transform(IdTransformer(::TagEventId))
    val tagId = javaUUID("tag_id").transform(IdTransformer(::TagId))
    val scopeType = text("scope_type")
    val scopeId = javaUUID("scope_id").transform(IdTransformer(::TagScopeId)).nullable()
    val tagGroupId = javaUUID("tag_group_id").transform(IdTransformer(::TagGroupId)).nullable()
    val key = text("key").transform(KeyTransformer(::TagKey))
    val name = text("name").nullable()
    val description = text("description").nullable()
    val validFrom = timestamp("valid_from")
    val validTo = timestamp("valid_to").nullable()

    override val primaryKey = PrimaryKey(id)
}
