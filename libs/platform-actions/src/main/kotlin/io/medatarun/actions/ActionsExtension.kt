package io.medatarun.actions

import io.medatarun.actions.actions.BatchActionProvider
import io.medatarun.actions.adapters.ActionPlatform
import io.medatarun.actions.adapters.ActionPlatformLazy
import io.medatarun.actions.internal.ActionAuditRecorderLogger
import io.medatarun.actions.ports.needs.ActionAuditRecorder
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx

class ActionsExtension : MedatarunExtension {
    override val id: String = "platform-actions"

    override fun initServices(ctx: MedatarunServiceCtx) {
        val extensionRegistry = ctx.getService(ExtensionRegistry::class)
        ctx.register(ActionPlatform::class, ActionPlatformLazy(extensionRegistry))
    }

    override fun initContributions(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".providers", ActionProvider::class)
        ctx.registerContributionPoint(this.id + ".auditRecorders", ActionAuditRecorder::class)
        ctx.registerContribution(ActionProvider::class, BatchActionProvider())
        ctx.registerContribution(ActionAuditRecorder::class, ActionAuditRecorderLogger())

    }

}
