package io.medatarun.model.infra.db.events

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.model.domain.ModelId
import io.medatarun.model.infra.db.ModelEventRecordFactoryUnsupportedCommandException
import io.medatarun.model.infra.db.records.ModelEventRecord
import io.medatarun.model.ports.needs.ModelStorageCmd
import io.medatarun.model.ports.needs.ModelStorageCmdEnveloppe
import io.medatarun.model.ports.needs.ModelStorageCmdOnModel
import java.time.Instant

/**
 * Builds the SQL event envelope and delegates payload serialization to the
 * dedicated ModelRepoCmd event codec.
 */
class ModelEventRecordFactory(private val codec:ModelEventJsonCodec) {

    fun create(cmdEnv: ModelStorageCmdEnveloppe, streamRevision: Int, createdAt: Instant): ModelEventRecord {
        val encoded = codec.encode(cmdEnv.cmd)
        return ModelEventRecord(
            id = UuidUtils.generateV7().toString(),
            modelId = extractModelId(cmdEnv.cmd),
            streamRevision = streamRevision,
            eventType = encoded.eventType,
            eventVersion = encoded.eventVersion,
            modelVersion = null,
            actorId = cmdEnv.principalId,
            actionId = cmdEnv.actionId.value.toString(),
            createdAt = createdAt,
            payload = encoded.payload
        )
    }

    private fun extractModelId(cmd: ModelStorageCmd): ModelId {
        return when (cmd) {
            is ModelStorageCmd.CreateModel -> cmd.id
            is ModelStorageCmd.StoreModelAggregate -> cmd.model.id
            else -> (cmd as? ModelStorageCmdOnModel)?.modelId
                ?: throw ModelEventRecordFactoryUnsupportedCommandException(cmd::class.qualifiedName ?: "unknown")
        }
    }
}

