package io.medatarun.model.infra.db.events

import io.medatarun.model.infra.db.ModelRepoCmdEventCommandNotRegisteredException
import io.medatarun.model.infra.db.ModelRepoCmdEventDuplicateContractException
import io.medatarun.model.infra.db.ModelRepoCmdEventUnknownContractException
import io.medatarun.model.ports.needs.ModelStorageCmd
import kotlin.reflect.KClass

/**
 * Holds the descriptors of all model events.
 *
 * Their contracts are declared [ModelStorageCmd] and we hold corresponding
 * [ModelEventDescriptor] here, allowing indexing and search.
 */
class ModelEventRegistry(
    entries: List<ModelEventDescriptor<out ModelStorageCmd>>
) {

    private val entriesByClass: Map<KClass<out ModelStorageCmd>, ModelEventDescriptor<out ModelStorageCmd>>
    private val entriesByContract: Map<ContractKey, ModelEventDescriptor<out ModelStorageCmd>>

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

    fun findEntryByCmd(cmd: ModelStorageCmd): ModelEventDescriptor<ModelStorageCmd> {
        val kClass = cmd::class
        val entry = entriesByClass[kClass]
            ?: throw ModelRepoCmdEventCommandNotRegisteredException(
                kClass.qualifiedName ?: kClass.simpleName ?: "unknown"
            )
        @Suppress("UNCHECKED_CAST")
        return entry as ModelEventDescriptor<ModelStorageCmd>
    }

    fun findEntryByContract(eventType: String, eventVersion: Int): ModelEventDescriptor<ModelStorageCmd> {
        val entry = entriesByContract[ContractKey(eventType, eventVersion)]
            ?: throw ModelRepoCmdEventUnknownContractException(eventType, eventVersion)
        @Suppress("UNCHECKED_CAST")
        return entry as ModelEventDescriptor<ModelStorageCmd>
    }

    private data class ContractKey(
        val eventType: String,
        val eventVersion: Int
    )
}