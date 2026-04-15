package io.medatarun.model.ports.needs

import io.medatarun.storage.eventsourcing.StorageCmd
import kotlinx.serialization.Serializable

@Serializable
sealed interface ModelStorageCmdAnyVersion: StorageCmd {

}