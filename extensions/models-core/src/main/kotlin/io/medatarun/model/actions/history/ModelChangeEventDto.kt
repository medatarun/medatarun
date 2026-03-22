package io.medatarun.model.actions.history


import io.medatarun.model.domain.ModelChangeEvent
import io.medatarun.security.AppActorId
import io.medatarun.security.AppActorResolver
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ModelChangeEventListDto(
    val items: List<ModelChangeEventDto>
)

@Serializable
data class ModelChangeEventDto(
    val eventId: String,
    val eventType: String,
    val eventVersion: Int,
    val eventSequenceNumber: Int,
    val createdAt: Long,
    val modelVersion: String?,
    val actorId: String,
    val actorDisplayName: String,
    val payload: JsonObject
)

fun toModelChangeEventListDto(
    evts: List<ModelChangeEvent>,
    actorResolver: AppActorResolver
): ModelChangeEventListDto {
    val actorMap = mutableMapOf<AppActorId, String>()
    fun resolve(actorId: AppActorId) =
        actorMap.getOrPut(actorId) { actorResolver.resolve(actorId)?.displayName ?: "??" }

    val list = evts.map { evt -> toModelChangeEventDto(evt, ::resolve) }
    return ModelChangeEventListDto(list)
}

fun toModelChangeEventDto(
    evt: ModelChangeEvent,
    actorName: (actorId: AppActorId) -> String
): ModelChangeEventDto {
    return ModelChangeEventDto(
        eventId = evt.eventId,
        eventType = evt.eventType,
        eventVersion = evt.eventVersion,
        eventSequenceNumber = evt.eventSequenceNumber,
        createdAt = evt.createdAt.toEpochMilli(),
        modelVersion = evt.modelVersion?.asString(),
        actorId = evt.traceabilityRecord.actorId.asString(),
        actorDisplayName = actorName(evt.traceabilityRecord.actorId),
        payload = evt.payload
    )
}