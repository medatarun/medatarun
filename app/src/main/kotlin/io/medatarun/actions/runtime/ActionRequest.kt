package io.medatarun.actions.runtime

import kotlinx.serialization.json.JsonObject

data class ActionRequest(
    val group: String,
    val command: String,
    val payload: JsonObject
)