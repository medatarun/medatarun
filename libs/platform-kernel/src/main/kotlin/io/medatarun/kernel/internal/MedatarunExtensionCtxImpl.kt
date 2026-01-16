package io.medatarun.kernel.internal

import io.medatarun.kernel.ContributionPointId
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.kernel.MedatarunExtensionCtxConfig
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