package io.medatarun.types

import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx

class TypeSystemExtension : MedatarunExtension {
    override val id: String = "platform-type-system"
    override fun initContributions(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".types", TypeDescriptor::class)
    }
}