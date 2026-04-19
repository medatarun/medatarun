package io.medatarun.actions.adapters

import io.medatarun.actions.domain.ActionInvoker
import io.medatarun.actions.domain.ActionRegistry
import io.medatarun.actions.internal.*
import io.medatarun.actions.ports.needs.ActionAuditRecorder
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.security.SecurityRulesProvider
import io.medatarun.types.TypeDescriptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class ActionPlatformLazy(
    extensionRegistry: ExtensionRegistry
) : ActionPlatform {

    private val delegate: ActionPlatformBuilt by lazy {
        build(
            typeDescriptors = extensionRegistry.findContributionsFlat(TypeDescriptor::class),
            actionProviders = extensionRegistry.findContributionsFlat(ActionProvider::class),
            securityRulesProviders = extensionRegistry.findContributionsFlat(SecurityRulesProvider::class),
            actionAuditRecorders = extensionRegistry.findContributionsFlat(ActionAuditRecorder::class)
        )
    }

    override val registry
        get() = delegate.registry
    override val invoker
        get() = delegate.invoker

    private class ActionPlatformBuilt(
        val registry: ActionRegistry,
        val invoker: ActionInvoker
    )

    /**
     * Builds an action platform so you can get a running registry and invoker at once, based on things provided externally
     * (action descriptors, type descriptors, security rule evaluators, etc.)
     */
    private fun build(
        typeDescriptors: List<TypeDescriptor<*>>,
        actionProviders: List<ActionProvider<*>>,
        securityRulesProviders: List<SecurityRulesProvider>,
        actionAuditRecorders: List<ActionAuditRecorder>
    ): ActionPlatformBuilt {


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

        return ActionPlatformBuilt(registry, actionInvoker)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ActionPlatform::class.java)
    }
}