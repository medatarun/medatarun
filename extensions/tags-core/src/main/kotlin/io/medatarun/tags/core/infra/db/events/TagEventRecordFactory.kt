package io.medatarun.tags.core.infra.db.events

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.storage.eventsourcing.StorageEventJsonCodec
import io.medatarun.tags.core.infra.db.records.TagEventRecord
import io.medatarun.tags.core.ports.needs.TagStorageCmd
import io.medatarun.tags.core.ports.needs.TagStorageCmdEnveloppe
import java.time.Instant

/**
 * Converts one tag command envelope into one row for `tag_event`.
 */
class TagEventRecordFactory(private val codec: StorageEventJsonCodec<TagStorageCmd>) {

    fun create(
        cmdEnv: TagStorageCmdEnveloppe,
        scopeType: String,
        scopeId: String?,
        streamRevision: Int,
        createdAt: Instant
    ): TagEventRecord {
        val encoded = codec.encode(cmdEnv.cmd)
        return TagEventRecord(
            id = UuidUtils.generateV7().toString(),
            scopeType = scopeType,
            scopeId = scopeId,
            streamRevision = streamRevision,
            eventType = encoded.eventType,
            eventVersion = encoded.eventVersion,
            actorId = cmdEnv.traceabilityRecord.actorId,
            traceabilityOrigin = cmdEnv.traceabilityRecord.origin,
            createdAt = createdAt,
            payload = encoded.payload
        )
    }
}
