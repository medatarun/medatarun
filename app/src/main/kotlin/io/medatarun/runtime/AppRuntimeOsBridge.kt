package io.medatarun.runtime


import java.nio.file.FileSystem
import java.nio.file.FileSystems

/**
 * Bridge interface to JVM/OS System features.
 *
 * Allow us to abstract OS calls in tests
 */
interface AppRuntimeOsBridge {
    /**
     * Gets OS filesystem (in tests, we will use an in-memory filesystem)
     */
    val fileSystem: FileSystem

    /**
     * Gets environment variable, in tests they will be fixed
     */
    fun getenv(name: String): String?

    /**
     * Gets System property, in tests they will be fixed
     */
    fun getProperty(name: String): String?

    /**
     * A set of properties, used by the configuration system, that have full precedence oon any other configuration
     * properties. This is mainly used in tests to override environment.
     */
    fun builtInConfigProperties(): Map<String, String>

    /**
     * Default implementation that uses System calls
     */
    class Default : AppRuntimeOsBridge {
        override val fileSystem: FileSystem = FileSystems.getDefault()
        override fun getenv(name: String): String? {
            return System.getenv(name)
        }

        override fun getProperty(name: String): String? {
            return System.getProperty(name)
        }

        override fun builtInConfigProperties(): Map<String, String> {
            // This is the default bridge, we are in a real mode so application shall not override anything
            return emptyMap()
        }
    }
}