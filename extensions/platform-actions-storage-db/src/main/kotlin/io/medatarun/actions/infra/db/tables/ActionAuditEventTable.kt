package io.medatarun.actions.infra.db.tables

import io.medatarun.platform.db.exposed.instant
import io.medatarun.platform.db.exposed.jsonb
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID

object ActionAuditEventTable : Table("action_audit_event") {
    val actionInstanceId = javaUUID("action_instance_id")
    val actionGroupKey = varchar("action_group_key", 255)
    val actionKey = varchar("action_key", 255)
    val actorId = javaUUID("actor_id").transform(AppActorIdTransformer()).nullable()
    val sourceValue = varchar("source", 255)
    val payloadSerialized = jsonb("payload_serialized")
    val createdAt = instant("created_at")
    val status = varchar("status", 32)
    val errorCode = varchar("error_code", 255).nullable()
    val errorMessage = text("error_message").nullable()

    override val primaryKey = PrimaryKey(actionInstanceId)
}
