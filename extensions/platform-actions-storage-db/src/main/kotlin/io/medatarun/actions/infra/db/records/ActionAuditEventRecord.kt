package io.medatarun.actions.infra.db.records

import io.medatarun.actions.infra.db.tables.ActionAuditEventTable
import org.jetbrains.exposed.v1.core.ResultRow

data class ActionAuditEventRecord(
    val actionInstanceId: String,
    val actionGroupKey: String,
    val actionKey: String,
    val principalId: String?,
    val source: String,
    val payloadSerialized: String,
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
                principalId = row[ActionAuditEventTable.principalId],
                source = row[ActionAuditEventTable.sourceValue],
                payloadSerialized = row[ActionAuditEventTable.payloadSerialized],
                status = row[ActionAuditEventTable.status],
                errorCode = row[ActionAuditEventTable.errorCode],
                errorMessage = row[ActionAuditEventTable.errorMessage]
            )
        }
    }
}
