package io.medatarun.actions.internal

import io.medatarun.actions.domain.*
import io.medatarun.actions.ports.needs.*
import io.medatarun.lang.http.StatusCode
import io.medatarun.type.commons.id.Id
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

internal class ActionRegistryImpl(
    private val actionSecurityRuleEvaluators: ActionSecurityRuleEvaluators,
    private val actionTypesRegistry: ActionTypesRegistry,
    private val actionProviderContributions: List<ActionProvider<*>>,
    private val vocabulary: SemanticsVocabulary
): ActionRegistry {

    private val semanticsResolver = ActionSemanticsResolver(vocabulary)

    private val actionGroupDescriptors: List<ActionGroupDescriptor> =
        actionProviderContributions.map { ActionGroupDescriptor(key = it.actionGroupKey) }

    private val actionDescriptors: List<ActionRegisteredWithRuntime> =
        actionProviderContributions.flatMap { toActions(it) }

    private val actionMap: Map<ActionId, ActionRegisteredWithRuntime> =
        actionDescriptors.associateBy { it.descriptor.id }

    private val actionByKeys: Map<String, ActionRegisteredWithRuntime> =
        actionDescriptors.associateBy { it.descriptor.group + "/" + it.descriptor.key }


    private fun toActions(actionProviderInstance: ActionProvider<*>): List<ActionRegisteredWithRuntime> {

        val cmds = actionProviderInstance.findCommandClass()
            ?.sealedSubclasses
            ?.map { sealed ->
                buildActionsDescriptions(
                    actionProviderInstance,
                    sealed,
                    actionProviderInstance.actionGroupKey
                )
            }
            ?: emptyList()

        val direct = actionProviderInstance.findActions()
            .map {
                ActionRegisteredWithRuntime(
                    descriptor = it,
                    semantics = semanticsResolver.createSemantics(it),
                    provider = actionProviderInstance
                )
            }

        return cmds + direct

    }

    /**
     * Builds a [ActionDescriptor] based on a ModelCmd.
     *
     * At invocation time, commands are launched via the dispatch() method
     */
    private fun buildActionsDescriptions(
        actionProvider: ActionProvider<*>,
        sealed: KClass<out Any>,
        actionGroup: String
    ): ActionRegisteredWithRuntime {
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
            accessType = ActionAccessType.DISPATCH,
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
            ActionDescriptorParamImpl(
                key = property.name,
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
        val registered = ActionRegisteredWithRuntime(
            descriptor = descriptor, semantics = semanticsResolved, provider = actionProvider
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

    private fun findActionById(actionId: ActionId): ActionRegisteredWithRuntime {
        return actionMap[actionId] ?: throw ActionNotFoundInternalException(actionId)
    }

    override fun findAllActions(): Collection<ActionRegistered> {
        return actionDescriptors
    }

    fun findActionOptional(actionGroupKey: String, actionKey: String): ActionRegistered? {
        return actionByKeys["$actionGroupKey/$actionKey"]
    }

    override fun findAction(actionGroupKey: String, actionKey: String): ActionRegistered {
        return findActionOptional(actionGroupKey, actionKey) ?: throw ActionNotFoundByKeysInternalException(
            actionGroupKey,
            actionKey
        )
    }

    @Suppress("UNCHECKED_CAST")
    fun findProviderOptional(actionId: ActionId): ActionProvider<Any>? {
        return findActionById(actionId).provider as ActionProvider<Any>?
    }

    fun findInvoker(actionId: ActionId): Invoker {
        val action = findActionById(actionId)
        val provider = when (action.descriptor.accessType) {
            ActionAccessType.DISPATCH -> {
                findProviderOptional(actionId) ?: throw ActionInvokerNotFoundInternalException(actionId)
            }
        }
        return InvokerByDispatch(provider)
    }

    fun findDeserializer(actionId: ActionId): ActionPayloadJsonDeserializer {
        val action = findActionById(actionId)
        when (action.descriptor.accessType) {
            ActionAccessType.DISPATCH -> {
                val actionProviderInstance: ActionProvider<Any> = findProviderOptional(action.descriptor.id)
                    ?: throw ActionInvocationException(StatusCode.NOT_FOUND, "Action provider not found for [${action.descriptor.id}]")
                return ActionPayloadDeserializerFromProviders(
                    actionProviderInstance,
                    actionTypesRegistry
                )

            }
        }

    }

    interface Invoker {
        fun invoke(cmd: Any, actionCtx: ActionCtx): Any?
    }

}
