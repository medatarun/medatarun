package io.medatarun.ext.modeljson

import io.medatarun.ext.modeljson.ModelJsonRepositoryConfig.Companion.CONFIG_PRETTY_PRINT_DEFAULT
import io.medatarun.ext.modeljson.ModelJsonRepositoryConfig.Companion.CONFIG_PRETTY_PRINT_KEY
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.model.ports.ModelRepository

class ModelJsonExtension : MedatarunExtension {
    override val id: String = "modeljson"
    override fun init(ctx: MedatarunExtensionCtx) {

        val configPrettyPrint = ctx.config.getConfigProperty(CONFIG_PRETTY_PRINT_KEY, CONFIG_PRETTY_PRINT_DEFAULT)
        val configRepoPath = ctx.resolveExtensionStoragePath(init = true)

        val repo = ModelJsonRepository(
            configRepoPath, ModelJsonConverter(configPrettyPrint == "true")
        )

        ctx.register(ModelRepository::class, repo)

    }


}