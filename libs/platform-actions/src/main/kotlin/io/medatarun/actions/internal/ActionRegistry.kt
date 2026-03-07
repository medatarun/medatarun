package io.medatarun.actions.internal

import io.medatarun.actions.domain.ActionCmdAccessType
import io.medatarun.actions.domain.ActionCmdDescriptor
import io.medatarun.actions.domain.ActionCmdParamDescriptor
import io.medatarun.actions.domain.ActionDefinitionWithUnknownSecurityRule
import io.medatarun.actions.domain.ActionDefinitionWithoutDocException
import io.medatarun.actions.domain.ActionGroupDescriptor
import io.medatarun.actions.domain.ActionId
import io.medatarun.actions.domain.ActionNotFoundInternalException
import io.medatarun.actions.domain.ActionSemantics
import io.medatarun.actions.domain.ActionSemanticsConfig
import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionDocSemanticsMode
import io.medatarun.actions.ports.needs.ActionParamDoc
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.type.commons.id.Id
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

class ActionRegistry(
    private val actionSecurityRuleEvaluators: ActionSecurityRuleEvaluators,
    private val actionTypesRegistry: ActionTypesRegistry,
    private val actionProviderContributions: List<ActionProvider<*>>
) {


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

    private val actionMap: Map<ActionId, ActionCmdDescriptor> = actionDescriptors.associateBy { it.id }


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

        val parameters = sealed.memberProperties.mapIndexed { index, property ->
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
        }
        return ActionCmdDescriptor(
            id = Id.generate(::ActionId),
            accessType = ActionCmdAccessType.DISPATCH,
            key = doc.key,
            actionClassName = sealed.simpleName ?: "",
            group = actionGroup,
            title = doc.title,
            description = doc.description,
            resultType = typeOf<Unit>(),
            parameters = parameters,
            uiLocations = doc.uiLocations.toSet(),
            securityRule = securityRule,
            semantics = toSemanticsDescription(doc)
        )
    }

    private fun toSemanticsDescription(doc: ActionDoc): ActionSemanticsConfig {
        val mode = doc.semantics.mode
        return when(mode) {
            ActionDocSemanticsMode.NONE -> ActionSemanticsConfig.None
            ActionDocSemanticsMode.AUTO -> ActionSemanticsConfig.Auto
            ActionDocSemanticsMode.UNKNOWN -> ActionSemanticsConfig.Unknown
            ActionDocSemanticsMode.DECLARED -> ActionSemanticsConfig.Declared(
                intent = doc.semantics.intent,
                subjects = doc.semantics.subjects.toList(),
                returns = doc.semantics.returns.toList()
            )
        }
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

    fun findAction(id: ActionId): ActionCmdDescriptor {
        return actionMap[id] ?: throw ActionNotFoundInternalException(id)
    }

    fun semantics(id: ActionId): ActionSemantics {
        val action = findAction(id)
        return ActionSemanticsResolver().createSemantics(action)
    }


    companion object {
        private val logger = LoggerFactory.getLogger(ActionRegistry::class.java)
    }
}
