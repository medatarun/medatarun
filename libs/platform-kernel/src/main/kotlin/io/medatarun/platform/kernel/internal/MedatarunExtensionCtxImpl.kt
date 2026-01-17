package io.medatarun.platform.kernel.internal

import io.medatarun.platform.kernel.ContributionPointId
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunExtensionCtxConfig
import kotlin.reflect.KClass

class MedatarunExtensionCtxImpl(
    private val extension: MedatarunExtension,
    private val registrar: ExtensionRegistryImpl.ExtensionRegistrar,
    private val cfg: MedatarunExtensionCtxConfig

) : MedatarunExtensionCtx, MedatarunExtensionCtxConfig by cfg {

    override fun <CONTRIB : Any> registerContributionPoint(id: ContributionPointId, api: KClass<CONTRIB>) {
        registrar.internalRegisterContributionPoint(extension, ContributionPoint(id, api, extension.id))
    }

    override fun <INTERFACE : Any, IMPL : INTERFACE> register(api: KClass<INTERFACE>, instance: IMPL) {
        registrar.internalRegisterContribution(extension.id, api, instance)
    }

}