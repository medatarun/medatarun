package io.medatarun.actions.infra.db.records

import io.medatarun.actions.infra.db.tables.ActionAuditEventTable
import io.medatarun.security.AppActorId
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.Instant

data class ActionAuditEventRecord(
    val actionInstanceId: String,
    val actionGroupKey: String,
    val actionKey: String,
    val actorId: AppActorId?,
    val source: String,
    val payloadSerialized: String,
    val createdAt: Instant,
    val status: String,
    val errorCode: String?,
    val errorMessage: String?
) {
    companion object {
        fun read(row: ResultRow): ActionAuditEventRecord {
            return ActionAuditEventRecord(
                actionInstanceId = row[ActionAuditEventTable.actionInstanceId],
                actionGroupKey = row[ActionAuditEventTable.actionGroupKey],
                actionKey = row[ActionAuditEventTable.actionKey],
                actorId = row[ActionAuditEventTable.actorId],
                source = row[ActionAuditEventTable.sourceValue],
                payloadSerialized = row[ActionAuditEventTable.payloadSerialized],
                createdAt = row[ActionAuditEventTable.createdAt],
                status = row[ActionAuditEventTable.status],
                errorCode = row[ActionAuditEventTable.errorCode],
                errorMessage = row[ActionAuditEventTable.errorMessage]
            )
        }
    }
}
