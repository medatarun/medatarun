package io.medatarun.kernel.internal

import io.medatarun.kernel.ContributionPointId
import io.medatarun.kernel.MedatarunConfig
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.lang.trimToNull
import java.nio.file.Path
import kotlin.reflect.KClass

class MedatarunExtensionCtxImpl(
    private val extension: MedatarunExtension,
    private val _config: MedatarunConfig,
    private val registrar: ExtensionRegistryImpl.ExtensionRegistrar
) : MedatarunExtensionCtx {

    override val config = MedatarunExtensionCtxConfigImpl(_config)

    override fun resolveProjectPath(relativePath: String?): Path {
        val trimmed = relativePath?.trimToNull()
        return _config.projectDir.resolve(trimmed).toAbsolutePath()
    }

    override fun <CONTRIB : Any> registerContributionPoint(id: ContributionPointId, api: KClass<CONTRIB>) {
        registrar.internalRegisterContributionPoint(extension, ContributionPoint(id, api, extension.id))
    }

    override fun <INTERFACE : Any, IMPL : INTERFACE> register(api: KClass<INTERFACE>, instance: IMPL) {
        registrar.internalRegisterContribution(extension.id, api, instance)
    }
}