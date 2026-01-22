package io.medatarun.ext.modeljson

import io.medatarun.ext.modeljson.ModelJsonRepositoryConfig.Companion.CONFIG_PRETTY_PRINT_DEFAULT
import io.medatarun.ext.modeljson.ModelJsonRepositoryConfig.Companion.CONFIG_PRETTY_PRINT_KEY
import io.medatarun.model.ports.needs.ModelExporter
import io.medatarun.model.ports.needs.ModelRepository
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.PlatformStartedListener

class ModelJsonExtension : MedatarunExtension {
    override val id: String = "models-storage-json"
    override fun init(ctx: MedatarunExtensionCtx) {

        val configPrettyPrint = ctx.getConfigProperty(CONFIG_PRETTY_PRINT_KEY, CONFIG_PRETTY_PRINT_DEFAULT)
        val configRepoPath = ctx.resolveExtensionStoragePath()
        val files = ModelsJsonStorageFiles(configRepoPath)
        val converter = ModelJsonConverter(configPrettyPrint == "true")
        val repo = ModelJsonRepository(
            files, converter
        )
        val migrations = ModelsStorageJsonMigrations(files)

        ctx.register(ModelRepository::class, repo)
        ctx.register(ModelExporter::class, ModelExporterJson(converter))
        ctx.register(PlatformStartedListener::class, migrations)

    }


}