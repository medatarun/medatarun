package io.metadatarun.ext.config

import io.medatarun.actions.adapters.ActionPlatform
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.getService
import io.metadatarun.ext.config.actions.ConfigActionProvider

class SysopsConfigInspectorExtension : MedatarunExtension {

    override val id = "sysops-config-inspector"
    override fun initContributions(ctx: MedatarunExtensionCtx) {
        val extensionRegistry = ctx.getService(ExtensionRegistry::class)
        val actionPlatform = ctx.getService<ActionPlatform>()
        val actionRegistry by lazy { actionPlatform.registry }
        ctx.registerContribution(ActionProvider::class, ConfigActionProvider(extensionRegistry, lazy { actionRegistry }))
    }

}
