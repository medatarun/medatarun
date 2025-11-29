package io.medatarun.runtime.internal

import io.medatarun.model.model.MedatarunException
import io.medatarun.runtime.getLogger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

class AppRuntimeScanner {


    fun scan(): AppRuntimeConfig {
        val projectDirStr = System.getenv(envMEDATARUN_APPLICATION_DATA) ?: System.getProperty("user.dir")
        if (projectDirStr == null) {
            throw RootDirNotFoundException()
        }

        val projectDir = Path.of(projectDirStr).toAbsolutePath()

        val config = findConfigInPackageJson(projectDir) ?: buildJsonObject {}

        return AppRuntimeConfig(projectDir, config)

    }

    private fun findConfigInPackageJson(projectDir: Path): JsonObject? {
        logger.debug("Trying to find configuration in package.json")
        val packageFile = projectDir.resolve("package.json")
        if (!packageFile.exists()) {
            logger.debug("No package.json file found")
            return null
        }

        logger.debug("Found package.json in $packageFile, reading")

        val jsonContent = packageFile.readText()
        val jsonElement = Json.parseToJsonElement(jsonContent)
        val jsonObject = jsonElement.jsonObject

        logger.debug("Found package.json in $packageFile, reading")

        val config = jsonObject["medatarun"]?.jsonObject
        if (config == null) {
            logger.debug("Medatarun configuration key [medatarun] not found. No configuration loaded.")
        } else {
            logger.debug("Medatarun configuration key [medatarun] found. Using this configuration.")
        }

        return config
    }

    companion object {
        public val envMEDATARUN_APPLICATION_DATA = "MEDATARUN_APPLICATION_DATA"
        private val logger = getLogger(AppRuntimeScanner::class)
    }
}

class RootDirNotFoundException() :
    MedatarunException("Could not guess the current user directory. Configure MEDATARUN_APPLICATION_DATA if needed.")







