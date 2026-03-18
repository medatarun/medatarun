package io.medatarun.actions.internal

import io.medatarun.actions.ports.needs.ActionAuditFailed
import io.medatarun.actions.ports.needs.ActionAuditReceived
import io.medatarun.actions.ports.needs.ActionAuditRecorder
import io.medatarun.actions.ports.needs.ActionAuditRejected
import io.medatarun.actions.ports.needs.ActionAuditSucceeded
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class ActionAuditRecorderLogger : ActionAuditRecorder {
    override fun onActionReceived(event: ActionAuditReceived) {
        logger.info(
            "action.received actorDisplayName={} group={} key={} source={} actionInstanceId={} actorId={} payload={}",
            event.actorDisplayName,
            event.actionGroupKey,
            event.actionKey,
            event.source,
            event.actionInstanceId.value,
            event.actorId?.asString(),
            event.payloadSerialized
        )
    }

    override fun onActionRejected(event: ActionAuditRejected) {
        logger.info(
            "action.rejected actionInstanceId={} code={} message={}",
            event.actionInstanceId.value,
            event.code,
            event.message
        )
    }

    override fun onActionSucceeded(event: ActionAuditSucceeded) {
        logger.info(
            "action.succeeded actionInstanceId={}",
            event.actionInstanceId.value
        )
    }

    override fun onActionFailed(event: ActionAuditFailed) {
        logger.info(
            "action.failed actionInstanceId={} code={} message={}",
            event.actionInstanceId.value,
            event.code,
            event.message
        )
    }

    private companion object {
        private val logger: Logger = LoggerFactory.getLogger(ActionAuditRecorderLogger::class.java)
    }
}
