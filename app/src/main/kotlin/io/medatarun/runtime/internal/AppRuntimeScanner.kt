package io.medatarun.runtime.internal

import io.medatarun.model.model.MedatarunException
import io.medatarun.runtime.getLogger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText

class AppRuntimeScanner {


    fun scan(): AppRuntimeConfig {
        val projectDirStr = System.getenv(envMEDATARUN_APPLICATION_DATA) ?: System.getProperty("user.dir")
        if (projectDirStr == null) {
            throw RootDirNotFoundException()
        }

        val projectDir = Path.of(projectDirStr).toAbsolutePath()
        val packageFile = projectDir.resolve("package.json")
        logger.info("Scanning app runtime config in $packageFile")

        if (!packageFile.isRegularFile()) {
            throw NotMedataProjectException()
        }

        val jsonContent = packageFile.readText()
        val jsonElement = Json.parseToJsonElement(jsonContent)
        val jsonObject = jsonElement.jsonObject

        val medatarunConfig = jsonObject["medatarun"]?.jsonObject ?: throw NotMedataProjectException()

        val modelJsonRepositoryPathStr = medatarunConfig["modelJsonRepository"]?.jsonPrimitive?.contentOrNull
            ?: throw ModelJsonRepositoryNotConfiguredException()

        val modelJsonRepositoryPath = projectDir.resolve(modelJsonRepositoryPathStr).toAbsolutePath()
        if (!modelJsonRepositoryPath.isDirectory()) {
            throw ModelJsonRepositoryNotFoundException(modelJsonRepositoryPath.toString())
        }

        return AppRuntimeConfig(projectDir, modelJsonRepositoryPath)

    }

    companion object {
        private val envMEDATARUN_APPLICATION_DATA = "MEDATARUN_APPLICATION_DATA"
        private val logger = getLogger(AppRuntimeScanner::class)
    }
}

class RootDirNotFoundException() :
    MedatarunException("Could not guess the current user directory. Configure MEDATARUN_APPLICATION_DATA if needed.")

class NotMedataProjectException() :
    MedatarunException("This project is not a Medatarun project. You must have a package.json file (the same as NodeJS projects) add a 'medatarun' key in your package.json")

class ModelJsonRepositoryNotConfiguredException() :
    MedatarunException("There should be an entry 'medatarun.modelJsonRepository' in your package.json the points to the directory where your models' Json files are stored.")

class ModelJsonRepositoryNotFoundException(path: String) :
    MedatarunException("medatarun.modelJsonRepository specifies path '$path' that does not point to a valid existing directory.")