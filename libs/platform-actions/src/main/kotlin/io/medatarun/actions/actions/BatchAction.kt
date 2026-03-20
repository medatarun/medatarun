package io.medatarun.actions.actions

import io.medatarun.actions.ports.needs.*
import io.medatarun.security.SecurityRuleNames
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

sealed interface BatchAction {
    @ActionDoc(
        key = "batch_run",
        title = "Batch commands",
        description = "Process a list of commands all at once",
        uiLocations = [ActionUILocation.global],
        securityRule = SecurityRuleNames.ADMIN,
        semantics = ActionDocSemantics(mode = ActionDocSemanticsMode.NONE)
    )
    class BatchRun(
        @ActionParamDoc(
            name = "Actions",
            description = "List of actions to run in batch. You should provide for each action `group`, `action`, and `payload`. Payload is required even if empty."
        )
        val actions: List<ActionWithPayload>,
    ) : BatchAction
}

@Serializable
data class ActionWithPayload(
    val group: String,
    val action: String,
    val payload: JsonObject? = null,
) {
    fun toResourceInvocationRequest(): ActionRequest {
        return ActionRequest(
            group, action, ActionPayload.AsJson(payload ?: EMPTY_JSON_OBJECT)
        )
    }

    companion object {
        private val EMPTY_JSON_OBJECT = buildJsonObject {}
    }
}