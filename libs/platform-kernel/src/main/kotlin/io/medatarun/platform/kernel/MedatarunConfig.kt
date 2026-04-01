package io.medatarun.platform.kernel

import io.medatarun.platform.kernel.internal.ResourceLocatorDefault
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.Path
import kotlin.io.path.createDirectories

interface MedatarunConfig {
    /**
     * Home of Medatarun runtime (where the application is installed)
     */
    val applicationHomeDir: Path

    /**
     * Project directory, meaning the location where Medatarun is run, where the user
     * stores its files
     */
    val projectDir: Path

    /**
     * Public URL where this server instance is accessible from
     */
    val publicBaseURL: URI

    fun getProperty(key: String): String?
    fun getProperty(key: String, defaultValue: String): String

    /**
     * Creates a new resource locator
     */
    fun createResourceLocator(): ResourceLocator

    companion object {
        fun createTempConfig(
            fs: FileSystem,
            props: Map<String, String>,
            appDir: String = "/tmp/medatarun",
            projectDir: String = "/tmp/medatarun-project",
            publicBaseURL: URI = URI("http://localhost:8080"),
        ): MedatarunConfig {

            val appDirPath = fs.getPath(appDir).also { it.createDirectories() }
            val projectDirPath = fs.getPath(projectDir).also { it.createDirectories() }
            val config = object : MedatarunConfig {
                override val applicationHomeDir: Path = appDirPath
                override val projectDir: Path = projectDirPath
                override val publicBaseURL: URI = publicBaseURL
                override fun getProperty(key: String): String? {
                    return props[key]
                }

                override fun getProperty(key: String, defaultValue: String): String {
                    return getProperty(key) ?: defaultValue
                }

                override fun createResourceLocator(): ResourceLocator =
                    ResourceLocatorDefault(
                        rootPath = projectDir,
                        fileSystem = fs
                    )
            }
            return config
        }
    }
}