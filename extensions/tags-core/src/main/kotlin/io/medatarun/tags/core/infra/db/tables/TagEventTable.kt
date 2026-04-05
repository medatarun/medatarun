package io.medatarun.tags.core.infra.db.tables

import io.medatarun.security.AppActorId
import io.medatarun.platform.db.exposed.jsonb
import io.medatarun.tags.core.domain.TagEventId
import io.medatarun.tags.core.domain.TagScopeId
import io.medatarun.platform.db.exposed.IdTransformer
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestamp

object TagEventTable : Table("tag_event") {
    val id = javaUUID("id").transform(IdTransformer(::TagEventId))
    val scopeType = text("scope_type")
    val scopeId = javaUUID("scope_id").transform(IdTransformer(::TagScopeId)).nullable()
    val streamRevision = integer("stream_revision")
    val eventType = text("event_type")
    val eventVersion = integer("event_version")
    val actorId = javaUUID("actor_id").transform(IdTransformer(::AppActorId))
    val traceabilityOrigin = text("traceability_origin")
    val createdAt = timestamp("created_at")
    val payload = jsonb("payload")

    override val primaryKey = PrimaryKey(id)
}
