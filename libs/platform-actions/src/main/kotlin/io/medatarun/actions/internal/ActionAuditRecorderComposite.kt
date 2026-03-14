package io.medatarun.actions.internal

import io.medatarun.actions.ports.needs.ActionAuditFailed
import io.medatarun.actions.ports.needs.ActionAuditReceived
import io.medatarun.actions.ports.needs.ActionAuditRecorder
import io.medatarun.actions.ports.needs.ActionAuditRejected
import io.medatarun.actions.ports.needs.ActionAuditSucceeded

internal class ActionAuditRecorderComposite(
    private val recorders: List<ActionAuditRecorder>
) : ActionAuditRecorder {
    override fun recordReceived(event: ActionAuditReceived) {
        for (recorder in recorders) {
            recorder.recordReceived(event)
        }
    }

    override fun recordRejected(event: ActionAuditRejected) {
        for (recorder in recorders) {
            recorder.recordRejected(event)
        }
    }

    override fun recordSucceeded(event: ActionAuditSucceeded) {
        for (recorder in recorders) {
            recorder.recordSucceeded(event)
        }
    }

    override fun recordFailed(event: ActionAuditFailed) {
        for (recorder in recorders) {
            recorder.recordFailed(event)
        }
    }
}
