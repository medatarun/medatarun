package io.metadatarun.ext.config.actions

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.security.SecurityRulesProvider
import kotlinx.serialization.Serializable

class ConfigActionProvider : ActionProvider<ConfigAction> {
    override val actionGroupKey: String = "config"


    override fun findCommandClass() = ConfigAction::class
    override fun dispatch(cmd: ConfigAction, actionCtx: ActionCtx): Any {
        return when (cmd) {
            is ConfigAction.AIAgentsInstructions -> ConfigAgentInstructions().process()
            is ConfigAction.Inspect -> actionCtx.extensionRegistry.inspectHumanReadable()
            is ConfigAction.InspectJson -> actionCtx.extensionRegistry.inspectJson()
            is ConfigAction.SecurityRulesDescriptions -> SecurityRulesDescriptionsResp(
                items = actionCtx.extensionRegistry
                    .findContributionsFlat(SecurityRulesProvider::class)
                    .flatMap { it.getRules() }
                    // Keep one entry per key if multiple providers expose same rule.
                    .distinctBy { it.key }
                    .sortedBy { it.key }
                    .map { SecurityRuleDescriptionDto(key = it.key, description = it.description) }
            )
        }
    }

}

@Serializable
data class SecurityRulesDescriptionsResp(
    val items: List<SecurityRuleDescriptionDto>
)

@Serializable
data class SecurityRuleDescriptionDto(
    val key: String,
    val description: String
)
