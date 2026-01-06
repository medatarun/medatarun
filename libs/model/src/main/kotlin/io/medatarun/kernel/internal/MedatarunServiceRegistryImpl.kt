package io.medatarun.kernel.internal

import io.medatarun.kernel.MedatarunConfig
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunServiceRegistry
import kotlin.reflect.KClass

class MedatarunServiceRegistryImpl(
    val extensions: List<MedatarunExtension>,
    val config: MedatarunConfig
) : MedatarunServiceRegistry {

    private val services: MutableMap<KClass<*>, Any> = mutableMapOf()


    fun init() {
        val me = this
        extensions.forEach { extension ->
            extension.initServices(MedatarunServiceCtxImpl(me, MedatarunExtensionCtxConfigImpl(extension, config)))
        }
    }

    fun <T : Any> register(service: KClass<T>, implem: T) {
        services[service] = implem
    }

    override fun <T : Any> getService(klass: KClass<T>): T {
        @Suppress("UNCHECKED_CAST")
        return services[klass] as? T ?: error("No service registered for $klass")
    }
}