package io.medatarun.platform.kernel.internal

import io.medatarun.platform.kernel.MedatarunConfig
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunServiceRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class MedatarunServiceRegistryImpl(
    extensions: List<MedatarunExtension>,
    val config: MedatarunConfig
) : MedatarunServiceRegistry {

    private val services: MutableMap<KClass<*>, Any> = mutableMapOf()


    init {
        val me = this
        extensions.forEach { extension ->
            extension.initServices(
                MedatarunServiceCtxImpl(me, MedatarunExtensionCtxConfigImpl(extension, config))
            )
        }
    }

    fun <T : Any> register(service: KClass<T>, implem: T) {
        logger.info("Registering ${service.qualifiedName} for ${implem::class.qualifiedName}")
        services[service] = implem
    }

    override fun <T : Any> getService(klass: KClass<T>): T {
        @Suppress("UNCHECKED_CAST")
        return services[klass] as? T ?: error("No service registered for $klass")
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MedatarunServiceRegistryImpl::class.java)
    }
}