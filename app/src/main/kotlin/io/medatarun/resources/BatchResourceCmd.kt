package io.medatarun.resources

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

sealed interface BatchResourceCmd {
    @ResourceCommandDoc(
        title = "Batch commands",
        description = "Process a list of commands all at once"
    )
    class Run(
        val actions: List<ActionWithPayload>,
    ) : BatchResourceCmd
}

@Serializable
data class ActionWithPayload(
    val action: String,
    val payload: JsonObject? = null,
) {
    fun toResourceInvocationRequest(): ResourceInvocationRequest {
        val (r, c) = action.split("/")
        return ResourceInvocationRequest(
            r, c, payload ?: EMPTY_JSON_OBJECT
        )
    }

    companion object {
        private val EMPTY_JSON_OBJECT = buildJsonObject{}
    }
}