package io.medatarun.kernel.internal

import io.medatarun.kernel.*
import io.medatarun.lang.trimToNull
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.reflect.KClass

class MedatarunExtensionCtxImpl(
    private val extension: MedatarunExtension,
    private val _config: MedatarunConfig,
    private val registrar: ExtensionRegistryImpl.ExtensionRegistrar
) : MedatarunExtensionCtx {

    override val config = MedatarunExtensionCtxConfigImpl(_config)

    override fun resolveProjectPath(relativePath: String?): Path {
        return resolvePath(_config.projectDir, relativePath)
    }

    override fun resolveMedatarunPath(relativePath: String?): Path {
        return resolvePath(_config.medatarunDir, relativePath)
    }

    override fun resolveExtensionStoragePath(init: Boolean): Path {
        val path = resolvePath(_config.medatarunDir, extension.id)
        if (!path.exists()) path.createDirectories()
        if (!path.isDirectory()) throw ExtensionStoragePathNotDirectoryException(path)
        return path
    }

    private fun resolvePath(basePath: Path, relativePath: String?): Path {
        val trimmed = relativePath?.trimToNull() ?: return basePath
        return basePath.resolve(trimmed).toAbsolutePath()
    }

    override fun <CONTRIB : Any> registerContributionPoint(id: ContributionPointId, api: KClass<CONTRIB>) {
        registrar.internalRegisterContributionPoint(extension, ContributionPoint(id, api, extension.id))
    }

    override fun <INTERFACE : Any, IMPL : INTERFACE> register(api: KClass<INTERFACE>, instance: IMPL) {
        registrar.internalRegisterContribution(extension.id, api, instance)
    }
}