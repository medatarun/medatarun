package io.medatarun.actions.ports.needs

import kotlinx.serialization.json.JsonObject

data class ActionRequest(
    val actionGroupKey: String,
    val actionKey: String,
    val payload: JsonObject
)