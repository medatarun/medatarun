package io.medatarun.actions.ports.needs

import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx

class ActionsExtension : MedatarunExtension {
    override val id: String = "actions"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".providers", ActionProvider::class)
    }
}