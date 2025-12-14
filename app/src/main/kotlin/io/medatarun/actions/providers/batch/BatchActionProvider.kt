package io.medatarun.actions.providers.batch

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider

class BatchActionProvider() : ActionProvider<BatchAction> {
    override val actionGroupKey: String = "batch"


    /**
     * Returns the list of supported commands. Note that we NEVER return the business model's commands
     * but something mode user-facing so that the model can evolve with preserving maximum compatibility
     * with user facing actions.
     */
    override fun findCommandClass() = BatchAction::class
    override fun dispatch(cmd: BatchAction, actionCtx: ActionCtx): Any? {
        return when (cmd) {
            is BatchAction.Run -> runActions(cmd, actionCtx)
        }
    }

    private fun runActions(cmd: BatchAction.Run, actionCtx: ActionCtx) {
        cmd.actions.forEach { actionSerialized ->
            actionCtx.dispatchAction(actionSerialized.toResourceInvocationRequest())
        }
    }

}