package io.medatarun.ext.modeljson

import io.medatarun.ext.modeljson.ModelJsonRepositoryConfig.Companion.CONFIG_PRETTY_PRINT_DEFAULT
import io.medatarun.ext.modeljson.ModelJsonRepositoryConfig.Companion.CONFIG_PRETTY_PRINT_KEY
import io.medatarun.model.ports.needs.ModelRepository
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx

class ModelJsonExtension : MedatarunExtension {
    override val id: String = "models-storage-json"
    override fun init(ctx: MedatarunExtensionCtx) {

        val configPrettyPrint = ctx.getConfigProperty(CONFIG_PRETTY_PRINT_KEY, CONFIG_PRETTY_PRINT_DEFAULT)
        val configRepoPath = ctx.resolveExtensionStoragePath()

        val repo = ModelJsonRepository(
            configRepoPath, ModelJsonConverter(configPrettyPrint == "true")
        )

        ctx.register(ModelRepository::class, repo)

    }


}