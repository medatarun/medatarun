package io.medatarun.ext.modeljson

import io.medatarun.ext.modeljson.ModelJsonRepositoryConfig.Companion.CONFIG_PRETTY_PRINT_DEFAULT
import io.medatarun.ext.modeljson.ModelJsonRepositoryConfig.Companion.CONFIG_PRETTY_PRINT_KEY
import io.medatarun.ext.modeljson.ModelJsonRepositoryConfig.Companion.CONFIG_REPOSITORY_PATH_KEY
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.model.model.ModelRepository
import kotlin.io.path.isDirectory

class ModelJsonExtension : MedatarunExtension {
    override val id: String = "modeljson"
    override fun init(ctx: MedatarunExtensionCtx) {

        val configPrettyPrint = ctx.getConfigProperty(CONFIG_PRETTY_PRINT_KEY, CONFIG_PRETTY_PRINT_DEFAULT)
        val configRepo = ctx.getConfigProperty(CONFIG_REPOSITORY_PATH_KEY)
        if (configRepo == null || configRepo.isBlank()) {
            throw ModelJsonRepositoryNotFoundException(CONFIG_REPOSITORY_PATH_KEY, "")
        }
        val configRepoPath = ctx.resolveProjectPath(configRepo)
        if (!configRepoPath.isDirectory()) {
            throw ModelJsonRepositoryNotFoundException(CONFIG_REPOSITORY_PATH_KEY, configRepoPath.toString())
        }

        val repo = ModelJsonRepository(
            configRepoPath, ModelJsonConverter(configPrettyPrint == "true")
        )

        ctx.register(ModelRepository::class, repo)

    }


}