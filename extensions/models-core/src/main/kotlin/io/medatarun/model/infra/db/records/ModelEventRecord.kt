package io.medatarun.model.infra.db.records

import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.infra.db.tables.ModelEventTable
import io.medatarun.security.AppPrincipalId
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.Instant

data class ModelEventRecord(
    val id: String,
    val modelId: ModelId,
    val streamRevision: Int,
    val eventType: String,
    val eventVersion: Int,
    val modelVersion: ModelVersion?,
    val actorId: AppPrincipalId,
    val actionId: String,
    val createdAt: Instant,
    val payload: String,
) {
    companion object {
        fun read(row: ResultRow): ModelEventRecord {
            return ModelEventRecord(
                id = row[ModelEventTable.id],
                modelId = row[ModelEventTable.modelId],
                streamRevision = row[ModelEventTable.streamRevision],
                eventType = row[ModelEventTable.eventType],
                eventVersion = row[ModelEventTable.eventVersion],
                modelVersion = row[ModelEventTable.modelVersion],
                actorId = row[ModelEventTable.actorId],
                actionId = row[ModelEventTable.actionId],
                createdAt = Instant.parse(row[ModelEventTable.createdAt]),
                payload = row[ModelEventTable.payload]
            )
        }
    }
}
