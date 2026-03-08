package io.medatarun.platform.kernel

import kotlin.reflect.KClass

/**
 * Context passed to extensions with tooling in the [MedatarunExtension.initContributions] phase.
 */
interface MedatarunExtensionCtx : MedatarunExtensionCtxConfig {
    /**
     * Creates a new contribution point and declares the interface that contributors must implement
     */
    fun <CONTRIB : Any> registerContributionPoint(id: ContributionPointId, api: KClass<CONTRIB>)

    /**
     * Registers a new contribution by providing an implementation to an API contract of a contribution point
     */
    fun <INTERFACE : Any, IMPL : INTERFACE> registerContribution(api: KClass<INTERFACE>, instance: IMPL)

    /**
     * Simple method to get registered services. Note that your own services, that you may have declared in [MedatarunExtension.initServices] are now available (as well as the ones of you dependencies).
     */
    fun <T : Any> getService(clazz: KClass<T>): T

}