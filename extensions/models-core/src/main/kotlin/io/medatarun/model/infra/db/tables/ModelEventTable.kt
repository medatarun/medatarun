package io.medatarun.model.infra.db.tables

import io.medatarun.model.domain.ModelId
import org.jetbrains.exposed.v1.core.Table

object ModelEventTable : Table("model_event") {
    val id = text("id")
    val modelId = text("model_id").transform(IdTransformer(::ModelId))
    val streamRevision = integer("stream_revision")
    val eventType = text("event_type")
    val eventVersion = integer("event_version")
    val modelVersion = text("model_version").transform(ModelVersionTransformer).nullable()
    val actorId = text("actor_id").transform(AppActorIdTransformer())
    val traceabilityOrigin = text("traceability_origin")
    val createdAt = text("created_at")
    val payload = text("payload")

    override val primaryKey = PrimaryKey(id)
}
