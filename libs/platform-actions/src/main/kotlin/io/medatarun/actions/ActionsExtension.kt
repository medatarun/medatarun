package io.medatarun.actions

import io.medatarun.actions.actions.BatchActionProvider
import io.medatarun.actions.adapters.ActionPlatform
import io.medatarun.actions.adapters.ActionPlatformLazy
import io.medatarun.actions.internal.ActionAuditRecorderLogger
import io.medatarun.actions.ports.needs.ActionAuditRecorder
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.actions.adapters.descriptors.TextMarkdownDescriptor
import io.medatarun.actions.adapters.descriptors.TextSingleLineDescriptor
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx
import io.medatarun.platform.telemetry.Telemetry
import io.medatarun.types.TypeDescriptor

class ActionsExtension : MedatarunExtension {
    override val id: String = "platform-actions"

    override fun initServices(ctx: MedatarunServiceCtx) {
        val extensionRegistry = ctx.getService(ExtensionRegistry::class)
        val telemetry = ctx.getService(Telemetry::class)
        ctx.register(ActionPlatform::class, ActionPlatformLazy(extensionRegistry, telemetry))
    }

    override fun initContributions(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".providers", ActionProvider::class)
        ctx.registerContributionPoint(this.id + ".auditRecorders", ActionAuditRecorder::class)
        ctx.registerContribution(ActionProvider::class, BatchActionProvider())
        ctx.registerContribution(ActionAuditRecorder::class, ActionAuditRecorderLogger())

        ctx.registerContribution(TypeDescriptor::class, TextMarkdownDescriptor())
        ctx.registerContribution(TypeDescriptor::class, TextSingleLineDescriptor())

    }

}
