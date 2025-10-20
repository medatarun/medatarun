package io.medatarun.resources

import io.medatarun.runtime.AppRuntime
import io.medatarun.runtime.getLogger

class DataResource(runtime: AppRuntime) {
    @Suppress("unused")
    fun import(file: String) {
        logger.cli("Importing file $file")
    }

    companion object {
        private val logger = getLogger(DataResource::class)
    }
}