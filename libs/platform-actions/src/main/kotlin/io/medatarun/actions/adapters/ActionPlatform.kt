package io.medatarun.actions.adapters

import io.medatarun.actions.internal.*
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.security.SecurityRulesProvider
import io.medatarun.types.TypeDescriptor

interface ActionPlatform {

    val registry: ActionRegistry
    val invoker: ActionInvoker

    companion object {

        fun build(
            typeDescriptors : List<TypeDescriptor<*>>,
            actionProviders: List<ActionProvider<*>>,
            securityRulesProviders : List<SecurityRulesProvider>
        ): ActionPlatform {

            val actionTypesRegistry = ActionTypesRegistry(typeDescriptors)

            val actionSecurityRuleEvaluators = ActionSecurityRuleEvaluators(securityRulesProviders.flatMap { it.getRules() })

            val registry = ActionRegistry(
                actionSecurityRuleEvaluators,
                actionTypesRegistry,
                actionProviders,
                ActionSemanticsResolver.buildDefaultVocabulary()
            )

            val actionInvoker = ActionInvoker(
                registry, actionTypesRegistry, actionSecurityRuleEvaluators
            )

            return object : ActionPlatform {
                override val registry = registry
                override val invoker = actionInvoker
            }
        }
    }
}