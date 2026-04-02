package io.medatarun.platform.kernel

import java.net.URI
import java.nio.file.Path

interface MedatarunExtensionCtxConfig {
    /**
     * Resolves a path relative to the application home directory (where medatarun binary is located)
     */
    fun resolveApplicationHomePath(relativePath: String): Path

    /**
     * Resolves a path relative to the user project
     */
    fun resolveProjectPath(relativePath: String?): Path

    /**
     * Returns extension storage path and create.
     */
    fun resolveExtensionStoragePath(): Path

    fun getConfigProperty(key: String): String?
    fun getConfigProperty(key: String, defaultValue: String): String
    /**
     * Returns all matching configuration entries for [prefix], using sub-keys as map keys.
     *
     * Example:
     * - prefix: medatarun.storage.datasource.jdbc.properties.
     * - returned map: user -> medatarun, password -> secret
     */
    fun getConfigPropertyMapStartingWith(prefix: String): Map<String, String>

    /**
     * Creates a new resource locator to access files and URLs
     */
    fun createResourceLocator(): ResourceLocator

    /**
     * Public base URL of the application.
     */
    fun publicBaseURL(): URI

}
