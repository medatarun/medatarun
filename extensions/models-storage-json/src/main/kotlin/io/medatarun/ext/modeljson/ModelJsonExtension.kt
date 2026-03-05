package io.medatarun.ext.modeljson

import io.medatarun.ext.modeljson.internal.ModelExporterJson
import io.medatarun.ext.modeljson.internal.ModelJsonConverter
import io.medatarun.ext.modeljson.internal.ModelsStorageJsonRepositoryConfig.Companion.CONFIG_PRETTY_PRINT_DEFAULT
import io.medatarun.ext.modeljson.internal.ModelsStorageJsonRepositoryConfig.Companion.CONFIG_PRETTY_PRINT_KEY
import io.medatarun.model.ports.needs.ModelExporter
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx

class ModelJsonExtension : MedatarunExtension {
    override val id: String = "models-storage-json"
    override fun init(ctx: MedatarunExtensionCtx) {
        val configPrettyPrint = ctx.getConfigProperty(CONFIG_PRETTY_PRINT_KEY, CONFIG_PRETTY_PRINT_DEFAULT)
        val prettyPrint = configPrettyPrint == "true"
        val converter = ModelJsonConverter(prettyPrint)
        ctx.register(ModelExporter::class, ModelExporterJson(converter))
    }


}