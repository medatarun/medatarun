package io.medatarun.platform.kernel

import kotlin.reflect.KClass


interface MedatarunServiceRegistry: Service {
    fun <T:Service> getService(klass: KClass<T>): T
}
inline fun <reified T : Service> MedatarunServiceRegistry.getService(): T = getService(T::class)