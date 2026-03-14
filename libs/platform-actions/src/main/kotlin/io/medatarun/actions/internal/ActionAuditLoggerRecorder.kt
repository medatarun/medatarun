package io.medatarun.actions.internal

import io.medatarun.actions.ports.needs.ActionAuditFailed
import io.medatarun.actions.ports.needs.ActionAuditReceived
import io.medatarun.actions.ports.needs.ActionAuditRecorder
import io.medatarun.actions.ports.needs.ActionAuditRejected
import io.medatarun.actions.ports.needs.ActionAuditSucceeded
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class ActionAuditLoggerRecorder : ActionAuditRecorder {
    override fun recordReceived(event: ActionAuditReceived) {
        logger.info(
            "action.received actionInstanceId={} group={} key={} principalId={} source={} payload={}",
            event.actionInstanceId.value,
            event.actionGroupKey,
            event.actionKey,
            event.principalId?.value,
            event.source,
            event.payloadSerialized
        )
    }

    override fun recordRejected(event: ActionAuditRejected) {
        logger.info(
            "action.rejected actionInstanceId={} code={} message={}",
            event.actionInstanceId.value,
            event.code,
            event.message
        )
    }

    override fun recordSucceeded(event: ActionAuditSucceeded) {
        logger.info(
            "action.succeeded actionInstanceId={}",
            event.actionInstanceId.value
        )
    }

    override fun recordFailed(event: ActionAuditFailed) {
        logger.info(
            "action.failed actionInstanceId={} code={} message={}",
            event.actionInstanceId.value,
            event.code,
            event.message
        )
    }

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(ActionAuditLoggerRecorder::class.java)
    }
}
