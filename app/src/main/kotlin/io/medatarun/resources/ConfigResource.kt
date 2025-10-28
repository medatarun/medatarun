package io.medatarun.resources

import io.medatarun.runtime.AppRuntime
import io.medatarun.runtime.getLogger
import kotlin.reflect.KClass

class ConfigResource(private val runtime: AppRuntime): ResourceContainer {
    @Suppress("unused")
    @ResourceCommandDoc(
        title = "Inspect config",
        description = "Returns a human-readable list of the configuration, including extension contributions and contribution points, what provides what to whom."
    )
    fun inspect(): String {
        return runtime.extensionRegistry.inspectHumanReadable()
    }

    override fun findCommandClass() = null
    companion object {
        private val logger = getLogger(ConfigResource::class)
    }
}
