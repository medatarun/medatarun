package io.medatarun.resources

import io.medatarun.resources.actions.ConfigAgentInstructions
import io.medatarun.runtime.AppRuntime
import io.medatarun.runtime.getLogger
import kotlinx.serialization.json.JsonObject
import kotlin.reflect.KClass

class ConfigResource(private val runtime: AppRuntime): ResourceContainer<Unit>{

    @Suppress("unused")
    @ResourceCommandDoc(
        title = "AI Agents Instructions",
        description = "Each AI Agent should read that first. Returns a usage guide for AI Agents. Use it for your AGENTS.md files if your agent doesn't support instructions in MCP."
    )
    fun aiAgentsInstructions(): String {
        return ConfigAgentInstructions().process()
    }

    @Suppress("unused")
    @ResourceCommandDoc(
        title = "Inspect config",
        description = "Returns a human-readable list of the configuration, including extension contributions and contribution points, what provides what to whom."
    )
    fun inspect(): String {
        return runtime.extensionRegistry.inspectHumanReadable()
    }

    @Suppress("unused")
    @ResourceCommandDoc(
        title = "Inspect config Json",
        description = "Returns a Json representation of the configuration, including extension contributions and contribution points, what provides what to whom."
    )
    fun inspectJson(): JsonObject {
        return runtime.extensionRegistry.inspectJson()
    }

    override fun findCommandClass() = null
    override fun dispatch(cmd: Unit): Any?  = Unit

    companion object {
        private val logger = getLogger(ConfigResource::class)
    }
}
