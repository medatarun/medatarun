package io.medatarun.actions.infra.db.tables

import org.jetbrains.exposed.v1.core.Table

object ActionAuditEventTable : Table("action_audit_event") {
    val actionInstanceId = varchar("action_instance_id", 36)
    val actionGroupKey = varchar("action_group_key", 255)
    val actionKey = varchar("action_key", 255)
    val principalId = varchar("principal_id", 255).nullable()
    val sourceValue = varchar("source", 255)
    val payloadSerialized = text("payload_serialized")
    val status = varchar("status", 32)
    val errorCode = varchar("error_code", 255).nullable()
    val errorMessage = text("error_message").nullable()

    override val primaryKey = PrimaryKey(actionInstanceId)
}
