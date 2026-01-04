package io.medatarun.runtime.internal

import io.medatarun.kernel.internal.ResourceLocatorDefault
import io.medatarun.lang.trimToNull
import io.medatarun.runtime.internal.config.MicroProfileConfigLoader
import org.eclipse.microprofile.config.Config
import org.slf4j.LoggerFactory
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class AppRuntimeConfigFactory {

    private val applicationHomeDir: Path = findApplicationHomeDir()
    private val config: Config = MicroProfileConfigLoader().load(applicationHomeDir)

    fun create(): AppRuntimeConfig {
        val projectDir = findProjectDir()
        val medatarunDir = findMedatarunDir(projectDir)
        return AppRuntimeConfig(applicationHomeDir, projectDir, medatarunDir, config) {
            ResourceLocatorDefault(rootPath = projectDir.toString(), fileSystem = FileSystems.getDefault())
        }
    }

    private fun findApplicationHomeDir(): Path {
        return findProjectDirUserDir()
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
        private const val MEDATARUN_DEFAULT_SUBDIR = ".medatarun"
        private val logger = LoggerFactory.getLogger(AppRuntimeConfigFactory::class.java)
    }
}
