package io.medatarun.data

import io.medatarun.kernel.ExtensionId
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx

class DataMdFileExtension() : MedatarunExtension {
    override val id: ExtensionId = "data-md-file"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint("$id.repository", DataRepository::class)
    }
}