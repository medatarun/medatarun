package io.medatarun.actions.providers.config

import io.medatarun.actions.runtime.ActionCtx
import io.medatarun.actions.runtime.ActionProvider

class ConfigActionProvider() : ActionProvider<ConfigAction> {

    override fun findCommandClass() = ConfigAction::class
    override fun dispatch(cmd: ConfigAction, actionCtx: ActionCtx): Any? {
        return when (cmd) {
            is ConfigAction.AIAgentsInstructions -> ConfigAgentInstructions().process()
            is ConfigAction.Inspect -> actionCtx.extensionRegistry.inspectHumanReadable()
            is ConfigAction.InspectJson -> actionCtx.extensionRegistry.inspectJson()
        }
    }

}
