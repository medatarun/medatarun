package io.medatarun.model.actions.history

import io.medatarun.model.actions.tools.AppPrincipalResolver
import io.medatarun.model.domain.ModelChangeEvent
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
    val actionId: String,
    val modelVersion: String?,
    val principalId: String,
    val principalDisplayName: String,
    val payload: JsonObject
)

fun toModelChangeEventListDto(
    evts: List<ModelChangeEvent>,
    appPrincipalResolver: AppPrincipalResolver
): ModelChangeEventListDto {
    val list = evts.map { evt -> toModelChangeEventDto(evt, appPrincipalResolver) }
    return ModelChangeEventListDto(list)
}

fun toModelChangeEventDto(
    evt: ModelChangeEvent,
    appPrincipalResolver: AppPrincipalResolver
): ModelChangeEventDto {
    return ModelChangeEventDto(
        eventId = evt.eventId,
        eventType = evt.eventType,
        eventVersion = evt.eventVersion,
        eventSequenceNumber = evt.eventSequenceNumber,
        createdAt = evt.createdAt.toEpochMilli(),
        actionId = evt.actionId.asString(),
        modelVersion = evt.modelVersion?.asString(),
        principalId = evt.principalId.value,
        principalDisplayName = appPrincipalResolver.displayName(evt.principalId),
        payload = evt.payload
    )
}