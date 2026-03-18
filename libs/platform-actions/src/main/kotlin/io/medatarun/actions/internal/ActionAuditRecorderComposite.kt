package io.medatarun.actions.internal

import io.medatarun.actions.ports.needs.ActionAuditFailed
import io.medatarun.actions.ports.needs.ActionAuditReceived
import io.medatarun.actions.ports.needs.ActionAuditRecorder
import io.medatarun.actions.ports.needs.ActionAuditRejected
import io.medatarun.actions.ports.needs.ActionAuditSucceeded

internal class ActionAuditRecorderComposite(
    private val recorders: List<ActionAuditRecorder>
) : ActionAuditRecorder {
    override fun onActionReceived(event: ActionAuditReceived) {
        for (recorder in recorders) {
            recorder.onActionReceived(event)
        }
    }

    override fun onActionRejected(event: ActionAuditRejected) {
        for (recorder in recorders) {
            recorder.onActionRejected(event)
        }
    }

    override fun onActionSucceeded(event: ActionAuditSucceeded) {
        for (recorder in recorders) {
            recorder.onActionSucceeded(event)
        }
    }

    override fun onActionFailed(event: ActionAuditFailed) {
        for (recorder in recorders) {
            recorder.onActionFailed(event)
        }
    }
}
