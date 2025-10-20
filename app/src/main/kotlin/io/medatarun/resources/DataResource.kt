package io.medatarun.resources

import io.medatarun.runtime.AppRuntime
import io.medatarun.runtime.getLogger

class DataResource(runtime: AppRuntime) {
    @ResourceCommandDoc(
        title = "Import data",
        description = "Registers a local data file with the runtime so subsequent commands can consume it."
    )
    @Suppress("unused")
    fun import(file: String) {
        logger.cli("Importing file $file")
    }

    companion object {
        private val logger = getLogger(DataResource::class)
    }
}
