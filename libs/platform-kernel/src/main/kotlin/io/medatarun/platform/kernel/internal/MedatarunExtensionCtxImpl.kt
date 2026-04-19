package io.medatarun.platform.kernel.internal

import io.medatarun.platform.kernel.ContributionPointId
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunExtensionCtxConfig
import io.medatarun.platform.kernel.MedatarunServiceRegistry
import io.medatarun.platform.kernel.Service
import io.medatarun.platform.kernel.ServiceContributionPoint
import io.medatarun.platform.kernel.getService
import java.security.Provider
import kotlin.reflect.KClass

class MedatarunExtensionCtxImpl(
    private val extension: MedatarunExtension,
    private val registrar: ExtensionRegistryImpl.ExtensionRegistrar,
    private val cfg: MedatarunExtensionCtxConfig,
    private val services : MedatarunServiceRegistry

) : MedatarunExtensionCtx, MedatarunExtensionCtxConfig by cfg {

    override fun <CONTRIB : ServiceContributionPoint> registerContributionPoint(id: ContributionPointId, api: KClass<CONTRIB>) {
        registrar.internalRegisterContributionPoint(extension, ContributionPoint(id, api, extension.id))
    }

    override fun <INTERFACE : ServiceContributionPoint, IMPL : INTERFACE> registerContribution(api: KClass<INTERFACE>, instance: IMPL) {
        registrar.internalRegisterContribution(extension.id, api, instance)
    }

    override fun <T : Service> getService(clazz: KClass<T>): T {
        return services.getService(clazz)
    }

}