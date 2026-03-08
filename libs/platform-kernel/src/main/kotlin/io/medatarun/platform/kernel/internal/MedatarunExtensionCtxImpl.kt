package io.medatarun.platform.kernel.internal

import io.medatarun.platform.kernel.ContributionPointId
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunExtensionCtxConfig
import io.medatarun.platform.kernel.MedatarunServiceRegistry
import io.medatarun.platform.kernel.getService
import kotlin.reflect.KClass

class MedatarunExtensionCtxImpl(
    private val extension: MedatarunExtension,
    private val registrar: ExtensionRegistryImpl.ExtensionRegistrar,
    private val cfg: MedatarunExtensionCtxConfig,
    private val services : MedatarunServiceRegistry

) : MedatarunExtensionCtx, MedatarunExtensionCtxConfig by cfg {

    override fun <CONTRIB : Any> registerContributionPoint(id: ContributionPointId, api: KClass<CONTRIB>) {
        registrar.internalRegisterContributionPoint(extension, ContributionPoint(id, api, extension.id))
    }

    override fun <INTERFACE : Any, IMPL : INTERFACE> registerContribution(api: KClass<INTERFACE>, instance: IMPL) {
        registrar.internalRegisterContribution(extension.id, api, instance)
    }

    override fun <T : Any> getService(clazz: KClass<T>): T {
        return services.getService(clazz)
    }

}