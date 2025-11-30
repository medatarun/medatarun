package io.medatarun.runtime.internal

import io.medatarun.lang.trimToNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.readText

class AppRuntimeScanner {


    fun scan(): AppRuntimeConfig {

        val projectDir = findProjectDir()
        val medatarunDir = findMedatarunDir(projectDir)

        val config = findConfigInPackageJson(projectDir) ?: buildJsonObject {}

        return AppRuntimeConfig(projectDir, medatarunDir, config)

    }

    private fun findMedatarunDir(projectDir: Path): Path {
        val medatarunDir = projectDir.resolve(MEDATARUN_DEFAULT_SUBDIR)
        if (!medatarunDir.exists()) {
            medatarunDir.createDirectories()
        } else if (!medatarunDir.isDirectory()) {
            throw MedatarunDirAlreadyExistsAsRegularFileException(medatarunDir.toString())
        }
        return medatarunDir
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

    private fun findProjectDir(): Path {
        val projectDir = findProjectDirApplicationData() ?: findProjectDirUserDir()
        return projectDir
    }

    fun findProjectDirApplicationData(): Path? {
        val projectDirStr = System.getenv(MEDATARUN_APPLICATION_DATA_ENV)
        val projectDirStrSafe = projectDirStr?.trimToNull() ?: return null
        val projectDir = Path.of(projectDirStrSafe).toAbsolutePath()
        if (!projectDir.exists()) {
            throw ProjectDirApplicationDataDoesNotExistException(projectDir.toString(), MEDATARUN_APPLICATION_DATA_ENV)
        }
        if (!projectDir.isDirectory()) {
            throw ProjectDirNotAdirectoryException(projectDir.toString())
        }
        logger.debug("Found project directory in $projectDir via $MEDATARUN_APPLICATION_DATA_ENV")
        return projectDir
    }

    fun findProjectDirUserDir(): Path {
        val userDirStr = System.getProperty("user.dir")
        val userDir = Path.of(userDirStr).toAbsolutePath()
        if (!userDir.exists()) {
            throw RootDirNotFoundException()
        }

        if (!userDir.isDirectory()) {
            throw ProjectDirNotAdirectoryException(userDirStr.toString())
        }
        return userDir

    }

    companion object {
        const val MEDATARUN_APPLICATION_DATA_ENV = "MEDATARUN_APPLICATION_DATA"
        private const val MEDATARUN_DEFAULT_SUBDIR = ".medatarun"
        private val logger = LoggerFactory.getLogger(AppRuntimeScanner::class.java)
    }
}
