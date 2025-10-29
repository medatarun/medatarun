package io.medatarun.runtime.internal

import io.medatarun.data.DataExtension
import io.medatarun.ext.datamdfile.DataMdFileExtension
import io.medatarun.ext.frictionlessdata.FrictionlessdataExtension
import io.medatarun.ext.modeljson.ModelJsonExtension
import io.medatarun.kernel.internal.ExtensionPlaformImpl
import io.medatarun.model.ModelExtension
import io.medatarun.model.infra.ModelHumanPrinterEmoji
import io.medatarun.model.infra.ModelStoragesComposite
import io.medatarun.model.internal.ModelAuditor
import io.medatarun.model.internal.ModelCmdsImpl
import io.medatarun.model.internal.ModelQueriesImpl
import io.medatarun.model.internal.ModelValidationImpl
import io.medatarun.model.model.ModelCmd
import io.medatarun.model.model.ModelCmds
import io.medatarun.model.model.ModelHumanPrinter
import io.medatarun.model.model.ModelQueries
import io.medatarun.model.ports.ModelRepository
import io.medatarun.runtime.AppRuntime
import org.slf4j.LoggerFactory

class AppRuntimeBuilder {
    fun build(): AppRuntime {
        val scanner = AppRuntimeScanner()
        val config = scanner.scan()
        val extensions = listOf(
            ModelExtension(),
            ModelJsonExtension(),
            DataExtension(),
            DataMdFileExtension(),
            FrictionlessdataExtension()
        )
        val platform = ExtensionPlaformImpl(extensions, config)
        val repositories = platform.extensionRegistry.findContributionsFlat(ModelRepository::class)
        val validation = ModelValidationImpl()
        val storage = ModelStoragesComposite(repositories, validation)
        val queries = ModelQueriesImpl(storage)
        val auditor: ModelAuditor = object : ModelAuditor {
            override fun onCmdProcessed(cmd: ModelCmd) {
                logger.info("onCmdProcessed: $cmd")
            }
        }
        val cmd = ModelCmdsImpl(storage, auditor)
        return object : AppRuntime {
            override val modelCmds: ModelCmds = cmd
            override val modelQueries: ModelQueries = queries
            override val extensionRegistry = platform.extensionRegistry
            override val modelHumanPrinter: ModelHumanPrinter = ModelHumanPrinterEmoji()

        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger("audit")
    }
}