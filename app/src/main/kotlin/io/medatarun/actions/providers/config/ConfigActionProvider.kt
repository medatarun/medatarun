package io.medatarun.actions.providers.config

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider

class ConfigActionProvider() : ActionProvider<ConfigAction> {
    override val actionGroupKey: String = "config"


    override fun findCommandClass() = ConfigAction::class
    override fun dispatch(cmd: ConfigAction, actionCtx: ActionCtx): Any? {
        return when (cmd) {
            is ConfigAction.AIAgentsInstructions -> ConfigAgentInstructions().process()
            is ConfigAction.Inspect -> actionCtx.extensionRegistry.inspectHumanReadable()
            is ConfigAction.InspectJson -> actionCtx.extensionRegistry.inspectJson()
        }
    }

}
