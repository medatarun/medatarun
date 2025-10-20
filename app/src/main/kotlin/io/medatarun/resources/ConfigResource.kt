package io.medatarun.resources

import io.medatarun.runtime.AppRuntime
import io.medatarun.runtime.getLogger

class ConfigResource(private val runtime: AppRuntime) {
    @Suppress("unused")
    fun inspect() {
        logger.cli(runtime.extensionRegistry.inspectHumanReadable())
    }

    companion object {
        private val logger = getLogger(ConfigResource::class)
    }
}