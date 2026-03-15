package io.medatarun.model.infra.db

import io.medatarun.model.ports.needs.ModelStorageCmd
import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

class ModelEventRegistry(
    entries: List<Entry<out ModelStorageCmd>>
) {

    private val entriesByClass: Map<KClass<out ModelStorageCmd>, Entry<out ModelStorageCmd>>
    private val entriesByContract: Map<ContractKey, Entry<out ModelStorageCmd>>

    init {
        val duplicates = entries.groupBy { ContractKey(it.eventType, it.eventVersion) }
            .filterValues { it.size > 1 }
        if (duplicates.isNotEmpty()) {
            val duplicate = duplicates.entries.first()
            throw ModelRepoCmdEventDuplicateContractException(duplicate.key.eventType, duplicate.key.eventVersion)
        }
        entriesByClass = entries.associateBy { it.kClass }
        entriesByContract = entries.associateBy { ContractKey(it.eventType, it.eventVersion) }
    }

    fun findEntryByCmd(cmd: ModelStorageCmd): Entry<ModelStorageCmd> {
        val kClass = cmd::class
        val entry = entriesByClass[kClass]
            ?: throw ModelRepoCmdEventCommandNotRegisteredException(
                kClass.qualifiedName ?: kClass.simpleName ?: "unknown"
            )
        @Suppress("UNCHECKED_CAST")
        return entry as Entry<ModelStorageCmd>
    }

    fun findEntryByContract(eventType: String, eventVersion: Int): Entry<ModelStorageCmd> {
        val entry = entriesByContract[ContractKey(eventType, eventVersion)]
            ?: throw ModelRepoCmdEventUnknownContractException(eventType, eventVersion)
        @Suppress("UNCHECKED_CAST")
        return entry as Entry<ModelStorageCmd>
    }

    data class Entry<T : ModelStorageCmd>(
        val kClass: KClass<T>,
        val eventType: String,
        val eventVersion: Int,
        val serializer: KSerializer<T>
    )

    private data class ContractKey(
        val eventType: String,
        val eventVersion: Int
    )
}
