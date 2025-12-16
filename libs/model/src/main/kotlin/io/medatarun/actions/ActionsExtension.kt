package io.medatarun.actions

import io.medatarun.actions.actions.BatchActionProvider
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx

class ActionsExtension : MedatarunExtension {
    override val id: String = "actions"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".providers", ActionProvider::class)
        ctx.register(ActionProvider::class, BatchActionProvider())
    }
}