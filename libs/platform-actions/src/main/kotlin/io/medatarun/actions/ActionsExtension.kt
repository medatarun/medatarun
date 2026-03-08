package io.medatarun.actions

import io.medatarun.actions.actions.BatchActionProvider
import io.medatarun.actions.adapters.ActionPlatform
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx
import io.medatarun.security.SecurityRulesProvider
import io.medatarun.types.TypeDescriptor

class ActionsExtension : MedatarunExtension {
    override val id: String = "platform-actions"
    override fun initContributions(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".providers", ActionProvider::class)
        ctx.registerContribution(ActionProvider::class, BatchActionProvider())

    }

    override fun initServices(ctx: MedatarunServiceCtx) {
        val extensionRegistry = ctx.getService(ExtensionRegistry::class)
        ctx.register(ActionPlatform::class, createActionPlatform(extensionRegistry))
    }

    /**
     * Lazy build is required so ActionPlatform sees contributions registered by all extensions during init().
     */
    private fun createActionPlatform(extensionRegistry: ExtensionRegistry): ActionPlatform {
        return object : ActionPlatform {
            private val delegate: ActionPlatform by lazy {
                ActionPlatform.build(
                    typeDescriptors = extensionRegistry.findContributionsFlat(TypeDescriptor::class),
                    actionProviders = extensionRegistry.findContributionsFlat(ActionProvider::class),
                    securityRulesProviders = extensionRegistry.findContributionsFlat(SecurityRulesProvider::class)
                )
            }

            override val registry
                get() = delegate.registry
            override val invoker
                get() = delegate.invoker
        }
    }
}
