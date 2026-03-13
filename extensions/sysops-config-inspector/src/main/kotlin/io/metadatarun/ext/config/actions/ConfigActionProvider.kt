package io.metadatarun.ext.config.actions

import io.medatarun.actions.domain.ActionRegistry
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.security.SecurityRulesProvider
import io.metadatarun.ext.config.actions.dto.*
import kotlinx.serialization.Serializable

class ConfigActionProvider(
    private val extensionRegistry: ExtensionRegistry,
    private val actionRegistry: ActionRegistry
) : ActionProvider<ConfigAction> {
    override val actionGroupKey: String = "config"


    override fun findCommandClass() = ConfigAction::class
    override fun dispatch(action: ConfigAction, actionCtx: ActionCtx): Any {
        return when (action) {
            is ConfigAction.AIAgentsInstructions -> ConfigAgentInstructions().process()
            is ConfigAction.Inspect -> extensionRegistry.inspectHumanReadable()
            is ConfigAction.InspectJson -> extensionRegistry.inspectJson()
            is ConfigAction.InspectActions -> inspectActions(actionCtx)
            is ConfigAction.InspectSecurityRules -> inspectSecurityRules(actionCtx)
        }
    }

    private fun inspectSecurityRules(actionCtx: ActionCtx): SecurityRulesDescriptionsResp =
        SecurityRulesDescriptionsResp(
            items = extensionRegistry
                .findContributionsFlat(SecurityRulesProvider::class)
                .flatMap { it.getRules() }
                // Keep one entry per key if multiple providers expose same rule.
                .distinctBy { it.key }
                .sortedBy { it.key }
                .map {
                    SecurityRuleDescriptionDto(
                        key = it.key,
                        name = it.name,
                        description = it.description
                    )
                }
        )

    /**
     * Rebuilds descriptors from extension contributions so UI and CLI rely on one action entry-point.
     */
    private fun inspectActions(actionCtx: ActionCtx): ActionRegistryDto {

        val items = actionRegistry.findAllActions().map { actionRegistered ->
            val descriptor = actionRegistered.descriptor
            val semantics = actionRegistered.semantics
            ActionDescriptorDto(
                id = descriptor.id.asString(),
                actionKey = descriptor.key,
                groupKey = descriptor.group,
                title = descriptor.title ?: descriptor.key,
                description = descriptor.description,
                uiLocations = descriptor.uiLocations,
                securityRule = descriptor.securityRule,
                parameters = descriptor.parameters.map { parameter ->
                    ActionParamDescriptorDto(
                        name = parameter.key,
                        type = parameter.multiplatformType,
                        jsonType = parameter.jsonType.code,
                        optional = parameter.optional,
                        title = parameter.title,
                        description = parameter.description,
                        order = parameter.order
                    )
                },
                semantics = ActionDescriptorSemanticsDto(
                    intent = semantics.intent.code,
                    subjects = semantics.subjects.map { subject ->
                        ActionDescriptorSemanticsSubjectDto(
                            type = subject.type,
                            referencingParams = subject.referencingParams.map { param ->
                                ActionDescriptorSemanticsSubjectReferencingParamDto(
                                    name = param.name,
                                    kind = param.kind.name.lowercase()
                                )
                            }
                        )
                    },
                    returns = semantics.returns
                )

            )
        }

        return ActionRegistryDto(items = items)
    }
}

@Serializable
data class SecurityRulesDescriptionsResp(
    val items: List<SecurityRuleDescriptionDto>
)

@Serializable
data class SecurityRuleDescriptionDto(
    val key: String,
    val name: String,
    val description: String
)
