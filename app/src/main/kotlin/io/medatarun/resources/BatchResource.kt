package io.medatarun.resources

class BatchResource() : ResourceContainer<BatchResourceCmd> {

    /**
     * Returns the list of supported commands. Note that we NEVER return the business model's commands
     * but something mode user-facing so that the model can evolve with preserving maximum compatibility
     * with user facing actions.
     */
    override fun findCommandClass() = BatchResourceCmd::class
    override fun dispatch(cmd: BatchResourceCmd, actionCtx: ActionCtx): Any? {
        return when (cmd) {
            is BatchResourceCmd.Run -> runActions(cmd, actionCtx)
        }
    }

    private fun runActions(cmd: BatchResourceCmd.Run, actionCtx: ActionCtx) {
        cmd.actions.forEach { actionSerialized ->
            actionCtx.dispatchAction(actionSerialized.toResourceInvocationRequest())
        }
    }

}