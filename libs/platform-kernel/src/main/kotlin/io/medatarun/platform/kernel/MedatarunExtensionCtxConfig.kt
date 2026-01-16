package io.medatarun.platform.kernel

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
     * Creates a new resource locator to access files and URLs
     */
    fun createResourceLocator(): ResourceLocator

}