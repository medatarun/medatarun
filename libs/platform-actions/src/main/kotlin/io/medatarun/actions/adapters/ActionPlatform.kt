package io.medatarun.actions.adapters

import io.medatarun.actions.domain.ActionInvoker
import io.medatarun.actions.domain.ActionRegistry
import io.medatarun.actions.internal.*
import io.medatarun.actions.ports.needs.ActionAuditRecorder
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.security.SecurityRulesProvider
import io.medatarun.types.TypeDescriptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Action platform holds the public runnable tooling necessary to use actions.
 *
 * Exposed tools are the [ActionRegistryImpl] and [ActionInvoker], used to discover registered actions and invoke them.
 */
interface ActionPlatform {

    val registry: ActionRegistry
    val invoker: ActionInvoker

    companion object {

        val logger: Logger = LoggerFactory.getLogger(ActionPlatform::class.java)

        /**
         * Builds an action platform so you can get a running registry and invoker at once, based on things provided externally
         * (action descriptors, type descriptors, security rule evaluators, etc.)
         */
        fun build(
            typeDescriptors: List<TypeDescriptor<*>>,
            actionProviders: List<ActionProvider<*>>,
            securityRulesProviders: List<SecurityRulesProvider>,
            actionAuditRecorders: List<ActionAuditRecorder>
        ): ActionPlatform {


            logger.info("Building action platform with providers {}", actionProviders.map { it.actionGroupKey })

            val actionTypesRegistry = ActionTypesRegistry(typeDescriptors)

            val actionSecurityRuleEvaluators =
                ActionSecurityRuleEvaluators(securityRulesProviders.flatMap { it.getRules() })

            val registry = ActionRegistryImpl(
                actionSecurityRuleEvaluators,
                actionTypesRegistry,
                actionProviders,
                ActionSemanticsResolver.buildDefaultVocabulary()
            )

            val actionAuditRecorder = ActionAuditRecorderComposite(actionAuditRecorders)

            val actionInvoker = ActionInvokerImpl(
                registry,
                actionSecurityRuleEvaluators,
                actionAuditRecorder
            )

            return object : ActionPlatform {
                override val registry = registry
                override val invoker = actionInvoker
            }
        }
    }


}
