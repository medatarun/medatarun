package io.medatarun.storage.eventsourcing

import kotlin.reflect.KClass

/**
 * Holds the descriptors of all storage events.
 *
 * Their contracts are declared as [StorageCmd] and we hold corresponding
 * [StorageEventDescriptor] here for indexing and search.
 */
class StorageEventRegistry<T : StorageCmd>(
    /**
     * Registry name, useful in exceptions and logs
     */
    val name: String,
    /**
     * Entries found
     */
    entries: List<StorageEventDescriptor<out T>>
) {

    private val entriesByClass: Map<KClass<out T>, StorageEventDescriptor<out T>>
    private val entriesByContract: Map<ContractKey, StorageEventDescriptor<out T>>


    init {
        val duplicates = entries.groupBy { ContractKey(it.eventType, it.eventVersion) }
            .filterValues { it.size > 1 }
        if (duplicates.isNotEmpty()) {
            val duplicate = duplicates.entries.first()
            throw StorageEventDuplicateContractException(name, duplicate.key.eventType, duplicate.key.eventVersion)
        }
        entriesByClass = entries.associateBy { it.kClass }
        entriesByContract = entries.associateBy { ContractKey(it.eventType, it.eventVersion) }

    }

    fun findEntryByCmd(cmd: T): StorageEventDescriptor<T> {
        val kClass = cmd::class
        return findEntryByCmdClass(kClass)
    }

    fun findEntryByCmdClass(kClass: KClass<out T>): StorageEventDescriptor<T> {
        val entry = entriesByClass[kClass]
            ?: throw StorageEventCommandNotRegisteredException(name, kClass)
        @Suppress("UNCHECKED_CAST")
        return entry as StorageEventDescriptor<T>
    }

    fun findEntryByContract(eventType: String, eventVersion: Int): StorageEventDescriptor<T> {
        val entry = entriesByContract[ContractKey(eventType, eventVersion)]
            ?: throw StorageEventUnknownContractException(name, eventType, eventVersion)
        @Suppress("UNCHECKED_CAST")
        return entry as StorageEventDescriptor<T>
    }

    private data class ContractKey(
        val eventType: String,
        val eventVersion: Int
    )

}