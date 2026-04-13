package io.medatarun.model.infra.db.events

import io.medatarun.model.ports.needs.ModelStorageCmd
import io.medatarun.model.ports.needs.ModelStorageCmdAnyVersion
import io.medatarun.storage.eventsourcing.StorageEventRegistry

/**
 * Holds the descriptors of specific model events.
 *
 * Descriptors are taken from the [StorageEventRegistry]
 */
class ModelEventKnownTypes(
    private val storageEventRegistry: StorageEventRegistry<ModelStorageCmdAnyVersion>

) {

    private val releaseEventType: String =
        storageEventRegistry.findEntryByCmdClass(ModelStorageCmd.ModelRelease::class).eventType

    fun modelReleaseEventType(): String {
        return releaseEventType
    }

    fun findAllDescripors() = storageEventRegistry.findAllDescriptor()

}
