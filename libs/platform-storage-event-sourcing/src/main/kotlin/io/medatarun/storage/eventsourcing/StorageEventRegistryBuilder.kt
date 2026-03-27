package io.medatarun.storage.eventsourcing

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.full.starProjectedType

class StorageEventRegistryBuilder<T: StorageCmd> {
    fun build(baseClass: KClass<T>): List<StorageEventDescriptor<T>> {
        val events = collectEventCommandClasses(baseClass)
        val entries = events.map(::toModelEventDescriptor)
        return entries

    }

    private fun collectEventCommandClasses(root: KClass<out T>): List<KClass<out T>> {
        return root.sealedSubclasses.flatMap { subclass ->
            if (subclass.sealedSubclasses.isEmpty()) {
                val annotation = subclass.java.getAnnotation(StorageEventContract::class.java)
                if (annotation == null) {
                    emptyList()
                } else {
                    if (!subclass.isData) {
                        throw StorageEventContractOnNonDataClassException(
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

    private fun toModelEventDescriptor(kClass: KClass<out T>): StorageEventDescriptor<T> {
        val annotation = kClass.java.getAnnotation(StorageEventContract::class.java)
            ?: throw StorageEventMissingContractAnnotationException(kClass.qualifiedName ?: "unknown")
        @Suppress("UNCHECKED_CAST")
        val serializer = serializer(kClass.starProjectedType) as KSerializer<T>
        @Suppress("UNCHECKED_CAST")
        val commandClass = kClass as KClass<T>
        return StorageEventDescriptor(
            kClass = commandClass,
            eventType = annotation.eventType,
            eventVersion = annotation.eventVersion,
            serializer = serializer
        )
    }
}