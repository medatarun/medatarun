package io.medatarun.kernel

import kotlin.reflect.KClass

interface MedatarunServiceRegistry {
    fun <T:Any> getService(klass: KClass<T>): T
}
inline fun <reified T : Any> MedatarunServiceRegistry.getService(): T = getService(T::class)