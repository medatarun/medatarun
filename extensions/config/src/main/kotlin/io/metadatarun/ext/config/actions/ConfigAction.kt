package io.metadatarun.ext.config.actions

import io.medatarun.actions.ports.needs.ActionDoc

sealed interface ConfigAction {
    @ActionDoc(
        title = "AI Agents Instructions",
        description = "Each AI Agent should read that first. Returns a usage guide for AI Agents. Use it for your AGENTS.md files if your agent doesn't support instructions in MCP.",
        uiLocation = "global"
    )
    class AIAgentsInstructions() : ConfigAction

    @ActionDoc(
        title = "Inspect config",
        description = "Returns a human-readable list of the configuration, including extension contributions and contribution points, what provides what to whom.",
        uiLocation = "global"
    )
    class Inspect() : ConfigAction

    @ActionDoc(
        title = "Inspect config Json",
        description = "Returns a Json representation of the configuration, including extension contributions and contribution points, what provides what to whom.",
        uiLocation = "global",
    )
    class InspectJson() : ConfigAction

}