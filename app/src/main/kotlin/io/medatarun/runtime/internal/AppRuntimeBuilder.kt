package io.medatarun.runtime.internal

import io.medatarun.data.DataExtension
import io.medatarun.ext.datamdfile.DataMdFileExtension
import io.medatarun.ext.db.DbExtension
import io.medatarun.ext.frictionlessdata.FrictionlessdataExtension
import io.medatarun.ext.modeljson.ModelJsonExtension
import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.kernel.internal.ExtensionPlaformImpl
import io.medatarun.model.ModelExtension
import io.medatarun.model.domain.ModelCmd
import io.medatarun.model.domain.ModelCmds
import io.medatarun.model.domain.ModelHumanPrinter
import io.medatarun.model.domain.ModelQueries
import io.medatarun.model.infra.ModelHumanPrinterEmoji
import io.medatarun.model.infra.ModelStoragesComposite
import io.medatarun.model.internal.ModelAuditor
import io.medatarun.model.internal.ModelCmdsImpl
import io.medatarun.model.internal.ModelQueriesImpl
import io.medatarun.model.internal.ModelValidationImpl
import io.medatarun.model.ports.ModelRepository
import io.medatarun.runtime.AppRuntime
import org.slf4j.LoggerFactory

class AppRuntimeBuilder {

    // Things that are compile-build builds

    val scanner = AppRuntimeScanner()
    val config = scanner.scan()
    val extensions = listOf(
        ModelExtension(),
        ModelJsonExtension(),
        DataExtension(),
        DataMdFileExtension(),
        DbExtension(),
        FrictionlessdataExtension()
    )
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
            cmd,
            queries,
            platform.extensionRegistry,
            ModelHumanPrinterEmoji()
        )
    }

    class AppRuntimeImpl(
        override val modelCmds: ModelCmds,
        override val modelQueries: ModelQueries,
        override val extensionRegistry: ExtensionRegistry,
        override val modelHumanPrinter: ModelHumanPrinter,

    ) : AppRuntime

    companion object {
        private val logger = LoggerFactory.getLogger("audit")
    }
}