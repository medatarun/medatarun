package io.medatarun.ext.datamdfile

import io.medatarun.data.ports.DataRepository
import io.medatarun.ext.datamdfile.internal.MdFileDataRepository
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx

class DataMdFileExtension: MedatarunExtension {
    override val id = "data-md-file"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.register(DataRepository::class, MdFileDataRepository(ctx.resolveExtensionStoragePath()))
    }

}