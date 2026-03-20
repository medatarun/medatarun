package io.medatarun.model.infra.inmemory

import io.medatarun.actions.domain.ActionInstanceId
import io.medatarun.model.domain.ModelChangeEvent
import io.medatarun.model.domain.ModelVersion
import io.medatarun.security.AppActorId
import kotlinx.serialization.json.JsonObject
import java.time.Instant

data class ModelChangeEventInMemory(
    override val eventId: String,
    override val eventType: String,
    override val eventVersion: Int,
    override val eventSequenceNumber: Int,
    override val createdAt: Instant,
    override val actionId: ActionInstanceId,
    override val modelVersion: ModelVersion?,
    override val actorId: AppActorId,
    override val payload: JsonObject
) : ModelChangeEvent