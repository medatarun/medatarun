package io.medatarun.actions.ports.needs

import kotlinx.serialization.json.JsonObject

data class ActionRequest(
    val actionGroupKey: String,
    val actionKey: String,
    val payload: ActionPayload
)

sealed interface ActionPayload {
    data class AsJson(val value: JsonObject): ActionPayload
    data class AsRaw(val value: Any): ActionPayload
}