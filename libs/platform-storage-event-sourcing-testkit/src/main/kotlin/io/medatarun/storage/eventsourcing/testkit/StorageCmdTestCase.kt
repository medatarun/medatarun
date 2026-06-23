package io.medatarun.storage.eventsourcing.testkit

import io.medatarun.storage.eventsourcing.StorageCmd

data class StorageCmdTestCase<CMD : StorageCmd>(
    val eventType: String,
    val eventVersion: Int,
    val cmd: CMD,
    val json: String,
    val upscaled: List<CMD> = listOf(cmd),
) {
    val id = listOf(eventType, ""+eventVersion, ""+hashCode()).joinToString("_")
}
