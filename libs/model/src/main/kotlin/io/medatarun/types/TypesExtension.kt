package io.medatarun.types

import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx

class TypesExtension : MedatarunExtension {
    override val id: String = "types"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".types", TypeDescriptor::class)
    }
}