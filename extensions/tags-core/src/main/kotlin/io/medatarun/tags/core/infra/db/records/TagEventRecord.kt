package io.medatarun.tags.core.infra.db.records

import io.medatarun.security.AppActorId
import java.time.Instant

data class TagEventRecord(
    val id: String,
    val scopeType: String,
    val scopeId: String?,
    val streamRevision: Int,
    val eventType: String,
    val eventVersion: Int,
    val actorId: AppActorId,
    val traceabilityOrigin: String,
    val createdAt: Instant,
    val payload: String,
)
