package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.ModelEventId
import io.medatarun.model.domain.ModelId
import io.medatarun.security.AppActorId
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

object ModelEventTable : Table("model_event") {
    val id = javaUUID("id").transform(IdTransformer(::ModelEventId))
    val modelId = javaUUID("model_id").transform(IdTransformer(::ModelId))
    val streamRevision = integer("stream_revision")
    val eventType = text("event_type")
    val eventVersion = integer("event_version")
    val modelVersion = text("model_version").transform(ModelVersionTransformer).nullable()
    val actorId = javaUUID("actor_id").transform(IdTransformer(::AppActorId))
    val traceabilityOrigin = text("traceability_origin")
    val createdAt = text("created_at")
    val payload = text("payload")

    override val primaryKey = PrimaryKey(id)
}
