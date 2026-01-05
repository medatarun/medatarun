package io.medatarun.runtime.internal

import io.medatarun.kernel.internal.ResourceLocatorDefault
import io.medatarun.lang.trimToNull
import io.medatarun.runtime.internal.config.MicroProfileConfigLoader
import org.eclipse.microprofile.config.Config
import org.slf4j.LoggerFactory
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class AppRuntimeConfigFactory {

    private val fileSystem: FileSystem = FileSystems.getDefault()
    private val applicationHomeDir: Path = findApplicationHomeDir()
    private val config: Config = MicroProfileConfigLoader().load(applicationHomeDir)

    fun create(): AppRuntimeConfig {
        val projectDir = findProjectDir()
        logger.info("MEDATARUN_HOME directory: $applicationHomeDir")
        logger.info("MEDATARUN_APPLICATION_DATA directory: $projectDir")
        return AppRuntimeConfig(applicationHomeDir, projectDir, config) {
            ResourceLocatorDefault(rootPath = projectDir.toString(), fileSystem = fileSystem)
        }
    }

    private fun findApplicationHomeDir(): Path {
        val home = System.getenv(MEDATARUN_HOME_ENV)
        val homeSafe = home.trimToNull()
        if (homeSafe.isNullOrBlank()) return findProjectDirUserDir()
        val homePath = fileSystem.getPath(homeSafe)
        if (!homePath.exists()) {throw MedatarunHomeDoesNotExistException(homeSafe)}
        if (!homePath.isDirectory()) {throw MedatarunHomeNotADirectoryException(homeSafe)}
        return homePath
    }


    private fun findProjectDir(): Path {
        val projectDir = findProjectDirApplicationData() ?: findProjectDirUserDir()
        return projectDir
    }

    fun findProjectDirApplicationData(): Path? {
        val projectDirStr = config.getOptionalValue(MEDATARUN_APPLICATION_DATA_ENV, String::class.java).orElse(null)
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
        logger.debug("Found user directory $userDir via System.property")
        return userDir

    }

    companion object {
        const val MEDATARUN_APPLICATION_DATA_ENV = "MEDATARUN_APPLICATION_DATA"
        const val MEDATARUN_HOME_ENV = "MEDATARUN_HOME"

        private val logger = LoggerFactory.getLogger(AppRuntimeConfigFactory::class.java)
    }
}
