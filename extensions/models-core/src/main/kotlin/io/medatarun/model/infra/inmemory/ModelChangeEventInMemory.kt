package io.medatarun.model.infra.inmemory

import io.medatarun.model.domain.ModelChangeEvent
import io.medatarun.model.domain.ModelEventId
import io.medatarun.model.domain.ModelVersion
import io.medatarun.security.AppTraceabilityRecord
import kotlinx.serialization.json.JsonObject
import java.time.Instant

data class ModelChangeEventInMemory(
    override val eventId: ModelEventId,
    override val eventType: String,
    override val eventVersion: Int,
    override val eventSequenceNumber: Int,
    override val createdAt: Instant,
    override val traceabilityRecord: AppTraceabilityRecord,
    override val modelVersion: ModelVersion?,
    override val payload: JsonObject
) : ModelChangeEvent