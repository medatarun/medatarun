package io.metadatarun.ext.config.actions

import io.medatarun.actions.internal.ActionRegistry
import io.medatarun.actions.internal.ActionSecurityRuleEvaluators
import io.medatarun.actions.internal.ActionTypesRegistry
import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.security.SecurityRulesProvider
import io.medatarun.types.TypeDescriptor
import io.metadatarun.ext.config.actions.dto.ActionDescriptorDto
import io.metadatarun.ext.config.actions.dto.ActionDescriptorSemanticsDto
import io.metadatarun.ext.config.actions.dto.ActionDescriptorSemanticsSubjectDto
import io.metadatarun.ext.config.actions.dto.ActionDescriptorSemanticsSubjectReferencingParamDto
import io.metadatarun.ext.config.actions.dto.ActionParamDescriptorDto
import io.metadatarun.ext.config.actions.dto.ActionRegistryDto
import kotlinx.serialization.Serializable

class ConfigActionProvider : ActionProvider<ConfigAction> {
    override val actionGroupKey: String = "config"


    override fun findCommandClass() = ConfigAction::class
    override fun dispatch(cmd: ConfigAction, actionCtx: ActionCtx): Any {
        return when (cmd) {
            is ConfigAction.AIAgentsInstructions -> ConfigAgentInstructions().process()
            is ConfigAction.Inspect -> actionCtx.extensionRegistry.inspectHumanReadable()
            is ConfigAction.InspectJson -> actionCtx.extensionRegistry.inspectJson()
            is ConfigAction.InspectActions -> inspectActions(actionCtx)
            is ConfigAction.InspectSecurityRules -> inspectSecurityRules(actionCtx)
        }
    }

    private fun inspectSecurityRules(actionCtx: ActionCtx): SecurityRulesDescriptionsResp = SecurityRulesDescriptionsResp(
        items = actionCtx.extensionRegistry
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
        val ruleEvaluators = ActionSecurityRuleEvaluators(
            actionCtx.extensionRegistry
                .findContributionsFlat(SecurityRulesProvider::class)
                .flatMap { it.getRules() }
        )
        val typeRegistry = ActionTypesRegistry(
            actionCtx.extensionRegistry.findContributionsFlat(TypeDescriptor::class)
        )
        val actionRegistry = ActionRegistry(
            actionSecurityRuleEvaluators = ruleEvaluators,
            actionTypesRegistry = typeRegistry,
            actionProviderContributions = actionCtx.extensionRegistry.findContributionsFlat(ActionProvider::class)
        )

        val items = actionRegistry.findAllActions().map { action ->
            ActionDescriptorDto(
                id = action.id.asString(),
                actionKey = action.key,
                groupKey = action.group,
                title = action.title ?: action.key,
                description = action.description,
                uiLocations = action.uiLocations,
                securityRule = action.securityRule,
                parameters = action.parameters.map { parameter ->
                    ActionParamDescriptorDto(
                        name = parameter.name,
                        type = parameter.multiplatformType,
                        jsonType = parameter.jsonType.code,
                        optional = parameter.optional,
                        title = parameter.title,
                        description = parameter.description,
                        order = parameter.order
                    )
                },
                semantics = actionRegistry.semantics(action.id).let { sem ->
                    ActionDescriptorSemanticsDto(
                        intent = sem.intent.code,
                        subjects = sem.subjects.map { subject ->
                            ActionDescriptorSemanticsSubjectDto(
                                type = subject.type,
                                referencingParams = subject.referencingParams.map { param ->
                                    ActionDescriptorSemanticsSubjectReferencingParamDto(
                                        name = param.name,
                                        kind = param.kind.name.lowercase()
                                    )
                                }
                            )
                        }
                    )
                }
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
