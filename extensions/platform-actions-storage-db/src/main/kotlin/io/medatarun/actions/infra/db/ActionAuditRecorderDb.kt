package io.medatarun.actions.infra.db

import io.medatarun.actions.ports.needs.ActionAuditFailed
import io.medatarun.actions.ports.needs.ActionAuditReceived
import io.medatarun.actions.ports.needs.ActionAuditRecorder
import io.medatarun.actions.ports.needs.ActionAuditRejected
import io.medatarun.actions.ports.needs.ActionAuditSucceeded
import io.medatarun.actions.infra.db.records.ActionAuditEventRecord
import io.medatarun.actions.infra.db.tables.ActionAuditEventTable
import io.medatarun.platform.db.DbConnectionFactory
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class ActionAuditRecorderDb(
    private val dbConnectionFactory: DbConnectionFactory,
    private val clock: ActionAuditClock
) : ActionAuditRecorder {

    override fun onActionReceived(event: ActionAuditReceived) {
        dbConnectionFactory.withExposed {
            ActionAuditEventTable.insert {
                it[actionInstanceId] = event.actionInstanceId.value.toString()
                it[actionGroupKey] = event.actionGroupKey
                it[actionKey] = event.actionKey
                it[actorId] = event.actorId
                it[sourceValue] = event.source
                it[payloadSerialized] = event.payloadSerialized
                it[createdAt] = clock.now()
                it[status] = STATUS_RECEIVED
                it[errorCode] = null
                it[errorMessage] = null
            }
        }
    }

    override fun onActionRejected(event: ActionAuditRejected) {
        updateTerminalStatus(event.actionInstanceId.value.toString(), STATUS_REJECTED, event.code, event.message)
    }

    override fun onActionSucceeded(event: ActionAuditSucceeded) {
        updateTerminalStatus(event.actionInstanceId.value.toString(), STATUS_SUCCEEDED, null, null)
    }

    override fun onActionFailed(event: ActionAuditFailed) {
        updateTerminalStatus(event.actionInstanceId.value.toString(), STATUS_FAILED, event.code, event.message)
    }

    fun findAll(): List<ActionAuditEventRecord> {
        return dbConnectionFactory.withExposed {
            ActionAuditEventTable
                .selectAll()
                .orderBy(ActionAuditEventTable.actionInstanceId)
                .map { ActionAuditEventRecord.read(it) }
        }
    }

    private fun updateTerminalStatus(
        actionInstanceIdValue: String,
        newStatus: String,
        newErrorCode: String?,
        newErrorMessage: String?
    ) {
        dbConnectionFactory.withExposed {
            ActionAuditEventTable.update({ ActionAuditEventTable.actionInstanceId eq actionInstanceIdValue }) {
                it[status] = newStatus
                it[errorCode] = newErrorCode
                it[errorMessage] = newErrorMessage
            }
        }
    }

    private companion object {
        const val STATUS_RECEIVED = "RECEIVED"
        const val STATUS_REJECTED = "REJECTED"
        const val STATUS_SUCCEEDED = "SUCCEEDED"
        const val STATUS_FAILED = "FAILED"
    }
}
