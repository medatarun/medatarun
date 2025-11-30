package io.medatarun.kernel.internal

import io.medatarun.kernel.ContributionPointId
import io.medatarun.kernel.MedatarunConfig
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.lang.trimToNull
import java.nio.file.Path
import javax.management.relation.RelationException
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