package io.medatarun.actions.internal

import io.medatarun.actions.domain.*
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
    private val actionProviderContributions: List<ActionProvider<*>>,
    private val vocabulary: SemanticsVocabulary
) {

    private val semanticsResolver = ActionSemanticsResolver(vocabulary)

    private val actionGroupDescriptors: List<ActionGroupDescriptor> =
        actionProviderContributions.map {
            ActionGroupDescriptor(
                key = it.actionGroupKey,
                providerInstance = it,

                )
        }

    private val actionGroupDescriptorsMap: Map<String, ActionGroupDescriptor> =
        actionGroupDescriptors.associateBy { it.key }

    private val actionDescriptors: List<ActionRegistered> = actionProviderContributions.flatMap { toActions(it) }
    private val actionMap: Map<ActionId, ActionRegistered> = actionDescriptors.associateBy { it.descriptor.id }
    private val actionByKeys: Map<String, ActionRegistered> =
        actionDescriptors.associateBy { it.descriptor.group + "/" + it.descriptor.key }


    private fun toActions(actionProviderInstance: ActionProvider<*>): List<ActionRegistered> {

        val cmds = actionProviderInstance.findCommandClass()
            ?.sealedSubclasses
            ?.map { sealed -> buildActionsDescriptions(sealed, actionProviderInstance.actionGroupKey) }
            ?: emptyList()

        val direct = actionProviderInstance.findActions()
            .map {
                ActionRegistered(
                    descriptor = it,
                    semantics = semanticsResolver.createSemantics(it)
                )
            }

        return cmds + direct

    }

    /**
     * Builds a [ActionCmdDescriptor] based on a ModelCmd.
     *
     * At invocation time, commands are launched via the dispatch() method
     */
    private fun buildActionsDescriptions(sealed: KClass<out Any>, actionGroup: String): ActionRegistered {
        val doc = sealed.findAnnotation<ActionDoc>() ?: throw ActionDefinitionWithoutDocException(
            actionGroup,
            sealed.simpleName ?: "unknown"
        )

        // Checks that all security rules are resolved
        val securityRule = doc.securityRule
        actionSecurityRuleEvaluators.findEvaluatorOptional(securityRule)
            ?: throw ActionDefinitionWithUnknownSecurityRule(actionGroup, doc.key, securityRule)

        val base = ActionDescriptorBase(
            id = Id.generate(::ActionId),
            accessType = ActionCmdAccessType.DISPATCH,
            key = doc.key,
            actionClassName = sealed.simpleName ?: "",
            group = actionGroup,
            title = doc.title,
            description = doc.description,
            resultType = typeOf<Unit>(),
            uiLocations = doc.uiLocations.toSet(),
            securityRule = securityRule,
        )

        val parameters = sealed.memberProperties.mapIndexed { index, property ->
            val paramdoc = property.findAnnotation<ActionParamDoc>()
            ActionParamDescriptorImpl(
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
        val semanticsDescription = toSemanticsDescription(doc)
        val descriptor = ActionDescriptorImpl(
            base = base,
            params = parameters,
            semantics = semanticsDescription,
        )
        val semanticsResolved = semanticsResolver.createSemantics(descriptor)
        val registered = ActionRegistered(
            descriptor, semanticsResolved
        )
        return registered
    }

    private fun toSemanticsDescription(doc: ActionDoc): ActionSemanticsConfig {
        val mode = doc.semantics.mode
        return when (mode) {
            ActionDocSemanticsMode.NONE -> ActionSemanticsConfig.None
            ActionDocSemanticsMode.AUTO -> ActionSemanticsConfig.Auto
            ActionDocSemanticsMode.DECLARED -> ActionSemanticsConfig.Declared(
                intent = doc.semantics.intent,
                subjects = doc.semantics.subjects.toList(),
                returns = doc.semantics.returns.toList()
            )
        }
    }

    fun findAllActions(): Collection<ActionRegistered> {
        return actionDescriptors
    }

    fun findActionOptional(actionGroupKey: String, actionKey: String): ActionRegistered? {
        return actionByKeys["$actionGroupKey/$actionKey"]
    }

    fun findProviderOptional(actionGroupKey: String, actionKey: String): ActionProvider<Any>? {
        return actionGroupDescriptorsMap[actionGroupKey]?.providerInstance as ActionProvider<Any>?
    }

}
