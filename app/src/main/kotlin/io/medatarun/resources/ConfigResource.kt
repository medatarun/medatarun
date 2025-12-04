package io.medatarun.resources

import io.medatarun.resources.actions.ConfigAgentInstructions

class ConfigResource() : ResourceContainer<ConfigResourceCmd> {

    override fun findCommandClass() = ConfigResourceCmd::class
    override fun dispatch(cmd: ConfigResourceCmd, actionCtx: ActionCtx): Any? {
        return when (cmd) {
            is ConfigResourceCmd.AIAgentsInstructions -> ConfigAgentInstructions().process()
            is ConfigResourceCmd.Inspect -> actionCtx.extensionRegistry.inspectHumanReadable()
            is ConfigResourceCmd.InspectJson -> actionCtx.extensionRegistry.inspectJson()
        }
    }

}
