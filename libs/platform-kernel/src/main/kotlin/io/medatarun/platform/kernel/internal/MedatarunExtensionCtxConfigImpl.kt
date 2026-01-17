package io.medatarun.platform.kernel.internal

import io.medatarun.lang.strings.trimToNull
import io.medatarun.platform.kernel.*
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

class MedatarunExtensionCtxConfigImpl(
    private val extension: MedatarunExtension,
    private val _config: MedatarunConfig
): MedatarunExtensionCtxConfig {

    override fun resolveProjectPath(relativePath: String?): Path {
        return resolvePath(_config.projectDir, relativePath)
    }

    override fun resolveApplicationHomePath(relativePath: String): Path {
        return resolvePath(_config.applicationHomeDir, relativePath)
    }

    override fun resolveExtensionStoragePath(): Path {
        val path = resolvePath(_config.applicationHomeDir.resolve("data").resolve("extensions"), extension.id)
        if (!path.exists()) path.createDirectories()
        if (!path.isDirectory()) throw ExtensionStoragePathNotDirectoryException(path)
        return path
    }

    override fun getConfigProperty(key: String): String? {
        return _config.getProperty(key)
    }

    override fun getConfigProperty(key: String, defaultValue: String): String {
        return _config.getProperty(key, defaultValue)
    }

    private fun resolvePath(basePath: Path, relativePath: String?): Path {
        val trimmed = relativePath?.trimToNull() ?: return basePath
        return basePath.resolve(trimmed).toAbsolutePath()
    }

    override fun createResourceLocator(): ResourceLocator {
        return _config.createResourceLocator()
    }
}