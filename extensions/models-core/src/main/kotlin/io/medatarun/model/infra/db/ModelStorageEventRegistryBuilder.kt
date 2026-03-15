package io.medatarun.model.infra.db

import io.medatarun.model.ports.needs.ModelEventContract
import io.medatarun.model.ports.needs.ModelStorageCmd
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.full.starProjectedType

/**
 * Builds the model event registry from the event contract declared directly on
 * ModelRepoCmd leaf classes.
 */
class ModelStorageEventRegistryBuilder {

    fun build(): ModelEventRegistry {
        val entries = collectEventCommandClasses(ModelStorageCmd::class).map(::entry)
        return ModelEventRegistry(entries)
    }

    private fun collectEventCommandClasses(root: KClass<out ModelStorageCmd>): List<KClass<out ModelStorageCmd>> {
        return root.sealedSubclasses.flatMap { subclass ->
            if (subclass.sealedSubclasses.isEmpty()) {
                val annotation = subclass.java.getAnnotation(ModelEventContract::class.java)
                if (annotation == null) {
                    emptyList()
                } else {
                    if (!subclass.isData) {
                        throw ModelRepoCmdEventContractOnNonDataClassException(
                            subclass.qualifiedName ?: subclass.simpleName ?: "unknown"
                        )
                    }
                    listOf(subclass)
                }
            } else {
                collectEventCommandClasses(subclass)
            }
        }
    }

    private fun entry(kClass: KClass<out ModelStorageCmd>): ModelEventRegistry.Entry<ModelStorageCmd> {
        val annotation = kClass.java.getAnnotation(ModelEventContract::class.java)
            ?: throw ModelRepoCmdEventMissingContractAnnotationException(kClass.qualifiedName ?: "unknown")
        @Suppress("UNCHECKED_CAST")
        val serializer = serializer(kClass.starProjectedType) as KSerializer<ModelStorageCmd>
        @Suppress("UNCHECKED_CAST")
        val commandClass = kClass as KClass<ModelStorageCmd>
        return ModelEventRegistry.Entry(
            kClass = commandClass,
            eventType = annotation.eventType,
            eventVersion = annotation.eventVersion,
            serializer = serializer
        )
    }
}
