package io.medatarun.platform.kernel

import kotlin.reflect.KClass

/**
 * Tooling provided to extensions when [MedatarunExtension.initServices] is called
 */
interface MedatarunServiceCtx : MedatarunExtensionCtxConfig {
    /**
     * You can get services from other previously registered extensions (dependent extensions).
     * They are initialized and usable. Be careful still, if they need contributions, yet, contributions
     * are not yet populated. So reference them in your tools, avoid calling them unless you know what they do exactly.
     */
    fun <T : Any> getService(klass: KClass<T>): T

    /**
     * Register one of your own services.
     *
     * Provide interface in [service] and concrete implementation in [implem].
     *
     * You can also pass [implem] in [service] too if you don't have interface.
     */
    fun <T : Any> register(service: KClass<T>, implem: T)
}
