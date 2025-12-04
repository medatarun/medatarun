package io.medatarun.resources

sealed interface ConfigResourceCmd {
    @ResourceCommandDoc(
        title = "AI Agents Instructions",
        description = "Each AI Agent should read that first. Returns a usage guide for AI Agents. Use it for your AGENTS.md files if your agent doesn't support instructions in MCP."
    )
    class AIAgentsInstructions() : ConfigResourceCmd

    @ResourceCommandDoc(
        title = "Inspect config",
        description = "Returns a human-readable list of the configuration, including extension contributions and contribution points, what provides what to whom."
    )
    class Inspect() : ConfigResourceCmd

    @ResourceCommandDoc(
        title = "Inspect config Json",
        description = "Returns a Json representation of the configuration, including extension contributions and contribution points, what provides what to whom."
    )
    class InspectJson() : ConfigResourceCmd
}