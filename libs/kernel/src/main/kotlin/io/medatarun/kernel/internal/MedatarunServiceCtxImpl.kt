package io.medatarun.kernel.internal

import io.medatarun.kernel.MedatarunExtensionCtxConfig
import io.medatarun.kernel.MedatarunServiceCtx
import kotlin.reflect.KClass

class MedatarunServiceCtxImpl(
    private val me: MedatarunServiceRegistryImpl,
    private val cfg: MedatarunExtensionCtxConfig
) : MedatarunServiceCtx, MedatarunExtensionCtxConfig by cfg {

    override fun <T : Any> getService(klass: KClass<T>): T = me.getService(klass)
    override fun <T : Any> register(service: KClass<T>, implem: T) = me.register(service, implem)

}