package io.medatarun.tags.core.infra.db.records

import io.medatarun.tags.core.domain.TagEventId
import io.medatarun.tags.core.domain.TagScopeId
import io.medatarun.security.AppActorId
import java.time.Instant

data class TagEventRecord(
    val id: TagEventId,
    val scopeType: String,
    val scopeId: TagScopeId?,
    val streamRevision: Int,
    val eventType: String,
    val eventVersion: Int,
    val actorId: AppActorId,
    val traceabilityOrigin: String,
    val createdAt: Instant,
    val payload: String,
)
