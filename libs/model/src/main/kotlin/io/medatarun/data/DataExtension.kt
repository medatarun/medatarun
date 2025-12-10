package io.medatarun.data

import io.medatarun.data.ports.needs.DataRepository
import io.medatarun.kernel.ExtensionId
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx

class DataExtension() : MedatarunExtension {
    override val id: ExtensionId = "data"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint("$id.repository", DataRepository::class)
    }
}