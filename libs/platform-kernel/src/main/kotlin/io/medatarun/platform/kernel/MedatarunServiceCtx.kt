package io.medatarun.platform.kernel

import kotlin.reflect.KClass

interface MedatarunServiceCtx : MedatarunExtensionCtxConfig {
    fun <T : Any> getService(klass: KClass<T>): T
    fun <T : Any> register(service: KClass<T>, implem: T)
}
