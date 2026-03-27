package io.medatarun.tags.core.infra.db.tables

import org.jetbrains.exposed.v1.core.Table

object TagEventTable : Table("tag_event") {
    val id = text("id")
    val scopeType = text("scope_type")
    val scopeId = text("scope_id").nullable()
    val streamRevision = integer("stream_revision")
    val eventType = text("event_type")
    val eventVersion = integer("event_version")
    val actorId = text("actor_id")
    val traceabilityOrigin = text("traceability_origin")
    val createdAt = text("created_at")
    val payload = text("payload")

    override val primaryKey = PrimaryKey(id)
}
