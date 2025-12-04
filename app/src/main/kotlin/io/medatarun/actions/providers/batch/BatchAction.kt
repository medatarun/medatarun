package io.medatarun.actions.providers.batch

import io.medatarun.actions.runtime.ActionDoc
import io.medatarun.actions.runtime.ActionRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

sealed interface BatchAction {
    @ActionDoc(
        title = "Batch commands",
        description = "Process a list of commands all at once"
    )
    class Run(
        val actions: List<ActionWithPayload>,
    ) : BatchAction
}

@Serializable
data class ActionWithPayload(
    val action: String,
    val payload: JsonObject? = null,
) {
    fun toResourceInvocationRequest(): ActionRequest {
        val (r, c) = action.split("/")
        return ActionRequest(
            r, c, payload ?: EMPTY_JSON_OBJECT
        )
    }

    companion object {
        private val EMPTY_JSON_OBJECT = buildJsonObject{}
    }
}