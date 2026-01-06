package io.medatarun.runtime.internal

import io.medatarun.actions.ActionsExtension
import io.medatarun.auth.embedded.AuthEmbeddedExtension
import io.medatarun.data.DataExtension
import io.medatarun.ext.datamdfile.DataMdFileExtension
import io.medatarun.ext.db.DbExtension
import io.medatarun.ext.frictionlessdata.FrictionlessdataExtension
import io.medatarun.ext.modeljson.ModelJsonExtension
import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.kernel.MedatarunServiceRegistry
import io.medatarun.kernel.internal.ExtensionPlaformImpl
import io.medatarun.kernel.internal.MedatarunServiceRegistryImpl
import io.medatarun.model.ModelExtension
import io.medatarun.model.infra.ModelHumanPrinterEmoji
import io.medatarun.model.infra.ModelStoragesComposite
import io.medatarun.model.internal.ModelAuditor
import io.medatarun.model.internal.ModelCmdsImpl
import io.medatarun.model.internal.ModelQueriesImpl
import io.medatarun.model.internal.ModelValidationImpl
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.exposed.ModelHumanPrinter
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.model.ports.needs.ModelRepository
import io.medatarun.runtime.AppRuntime
import io.metadatarun.ext.config.ConfigExtension
import org.slf4j.LoggerFactory

class AppRuntimeBuilder(private val config: AppRuntimeConfig) {

    // Things that are compile-build builds

    val extensions = listOf(
        ActionsExtension(),
        AuthEmbeddedExtension(),
        ModelExtension(),
        ConfigExtension(),
        ModelJsonExtension(),
        DataExtension(),
        DataMdFileExtension(),
        DbExtension(),
        FrictionlessdataExtension()
    )
    val serviceRegistry = MedatarunServiceRegistryImpl(extensions, config)
    val platform = ExtensionPlaformImpl(extensions, config)
    val repositories = platform.extensionRegistry.findContributionsFlat(ModelRepository::class)
    val validation = ModelValidationImpl()
    val storage = ModelStoragesComposite(repositories, validation)
    val auditor: ModelAuditor = object : ModelAuditor {
        override fun onCmdProcessed(cmd: ModelCmd) {
            logger.info("onCmdProcessed: $cmd")
        }
    }

    fun build(): AppRuntime {
        val queries = ModelQueriesImpl(storage)
        val cmd = ModelCmdsImpl(storage, auditor)
        return AppRuntimeImpl(
            config,
            cmd,
            queries,
            platform.extensionRegistry,
            ModelHumanPrinterEmoji(),
            serviceRegistry
        )
    }

    class AppRuntimeImpl(
        override val config: AppRuntimeConfig,
        override val modelCmds: ModelCmds,
        override val modelQueries: ModelQueries,
        override val extensionRegistry: ExtensionRegistry,
        override val modelHumanPrinter: ModelHumanPrinter,
        override val services: MedatarunServiceRegistry
    ) : AppRuntime

    companion object {
        private val logger = LoggerFactory.getLogger("audit")
    }
}