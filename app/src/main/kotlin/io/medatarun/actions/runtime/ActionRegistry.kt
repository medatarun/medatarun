package io.medatarun.actions.runtime

import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionParamDoc
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.kernel.ExtensionRegistry
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

class ActionRegistry(
    private val extensionRegistry: ExtensionRegistry,
    private val actionSecurityRuleEvaluators: ActionSecurityRuleEvaluators,
    private val actionTypesRegistry: ActionTypesRegistry
) {
    private val actionProviderContributions = extensionRegistry.findContributionsFlat(ActionProvider::class)

    private val actionGroupDescriptors: List<ActionGroupDescriptor> =
        actionProviderContributions.map {
            ActionGroupDescriptor(
                key = it.actionGroupKey,
                providerInstance = it,
                actions = toActions(it)
            )
        }

    private val actionGroupDescriptorsMap: Map<String, ActionGroupDescriptor> =
        actionGroupDescriptors.associateBy { it.key }

    private val actionDescriptors: List<ActionCmdDescriptor> =
        actionGroupDescriptors.flatMap { it.actions }


    private fun toActions(actionProviderInstance: ActionProvider<*>): List<ActionCmdDescriptor> {

        val cmds = actionProviderInstance.findCommandClass()
            ?.sealedSubclasses
            ?.map { sealed -> buildActionsDescriptions(sealed, actionProviderInstance.actionGroupKey) }
            ?: emptyList()

        return cmds

    }

    /**
     * Builds a [ActionCmdDescriptor] based on a ModelCmd.
     *
     * At invocation time, commands are launched via the dispatch() method
     */
    private fun buildActionsDescriptions(sealed: KClass<out Any>, actionGroup: String): ActionCmdDescriptor {
        val doc = sealed.findAnnotation<ActionDoc>() ?: throw ActionDefinitionWithoutDocException(
            actionGroup,
            sealed.simpleName ?: "unknown"
        )

        // Checks that all security rules are resolved
        val securityRule = doc.securityRule
        actionSecurityRuleEvaluators.findEvaluatorOptional(securityRule)
            ?: throw ActionDefinitionWithUnknownSecurityRule(actionGroup, doc.key, securityRule)

        return ActionCmdDescriptor(
            accessType = ActionCmdAccessType.DISPATCH,
            key = doc.key,
            actionClassName = sealed.simpleName ?: "",
            group = actionGroup,
            title = doc.title,
            description = doc.description,
            resultType = typeOf<Unit>(),
            parameters = sealed.memberProperties.mapIndexed { index, property ->
                val paramdoc = property.findAnnotation<ActionParamDoc>()
                ActionCmdParamDescriptor(
                    name = property.name,
                    title = paramdoc?.name,
                    description = paramdoc?.description?.trimIndent(),
                    optional = property.returnType.isMarkedNullable,
                    type = property.returnType,
                    multiplatformType = actionTypesRegistry.toMultiplatformType(property.returnType),
                    jsonType = actionTypesRegistry.toJsonType(property.returnType),
                    order = paramdoc?.order ?: index
                )
            },
            uiLocation = doc.uiLocation ?: "",
            securityRule = securityRule

        )
    }


    fun findAllGroupDescriptors(): Collection<ActionGroupDescriptor> {
        return actionGroupDescriptorsMap.values
    }

    fun findGroupDescriptorByIdOptional(actionGroup: String): ActionGroupDescriptor? {
        return actionGroupDescriptorsMap[actionGroup]
    }

    fun findAllActions(): Collection<ActionCmdDescriptor> {
        return actionDescriptors
    }


    companion object {
        private val logger = LoggerFactory.getLogger(ActionRegistry::class.java)
    }
}