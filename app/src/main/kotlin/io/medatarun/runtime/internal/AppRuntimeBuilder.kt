package io.medatarun.runtime.internal

import io.medatarun.actions.ActionsExtension
import io.medatarun.auth.AuthExtension
import io.medatarun.ext.db.ModelsImportJdbcExtension
import io.medatarun.ext.frictionlessdata.FrictionlessdataExtension
import io.medatarun.ext.modeljson.ModelJsonExtension
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
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.MedatarunServiceRegistry
import io.medatarun.platform.kernel.PlatformRuntime
import io.medatarun.platform.kernel.internal.ExtensionPlaformImpl
import io.medatarun.platform.kernel.internal.MedatarunServiceRegistryImpl
import io.medatarun.runtime.AppRuntime
import io.medatarun.security.SecurityExtension
import io.medatarun.security.SecurityRolesRegistry
import io.medatarun.security.SecurityRolesRegistryImpl
import io.medatarun.types.TypeSystemExtension
import io.metadatarun.ext.config.SysopsConfigInspectorExtension
import org.slf4j.LoggerFactory

class AppRuntimeBuilder(private val config: AppRuntimeConfig) {

    // Things that are compile-build builds
    //
    // BE CAREFUL: must be done in correct order, we don't have
    // dependency graphs that launch them in correct order for now

    val extensions = listOf(
        TypeSystemExtension(),
        SecurityExtension(),
        ActionsExtension(),
        AuthExtension(),
        ModelExtension(),
        SysopsConfigInspectorExtension(),
        ModelJsonExtension(),
        ModelsImportJdbcExtension(),
        FrictionlessdataExtension()
    )
    val serviceRegistry = MedatarunServiceRegistryImpl(extensions, config)
    val extensionPlatform = ExtensionPlaformImpl(extensions, config)

    // 🤔 🤔 🤔
    // Little dirty here
    // Everything should be done in the "model" plugin or not?
    //
    // 🤔 not sure yet, if we define implems in plugins directly
    //    we wouldn't be able to change them at runtime and we
    //    would fall in the JavaEE/JarkataEE/Spring and other DI traps
    //    to define what is default, what is not...
    //
    // 🤔 I don't see a good solution for now, let's think, we'll see that
    //    later.
    //
    // 🤔 Note that the "auth" plugin declares its own services in Service
    //    Registry, so it's a different behavior.
    //    I think it relates on this inconsistency: not being sure which
    //    initialization model is right or not
    //
    // 🤔 Final word: it's right to say that it's the main app role to do the
    //    plumbing. So maybe plugins shall just provide default implementation
    //    that we choose here. But forcing plugin to register implements is
    //    maybe wrong. At the same time, making the "glue" here means the
    //    plugin system is not really pluggable 🤯
    //
    val auditor: ModelAuditor = object : ModelAuditor {
        override fun onCmdProcessed(cmd: ModelCmd) {
            logger.info("onCmdProcessed: $cmd")
        }
    }
    val repositories = extensionPlatform.extensionRegistry.findContributionsFlat(ModelRepository::class)
    val validation = ModelValidationImpl()
    val storage = ModelStoragesComposite(repositories, validation)
    val modelQueriesImpl = ModelQueriesImpl(storage)
    val modelCmdsImpl = ModelCmdsImpl(storage, auditor)
    val modelHumanPrinterEmoji = ModelHumanPrinterEmoji()
    val securityRolesRegistry = SecurityRolesRegistryImpl(extensionPlatform.extensionRegistry)
    val platformRuntime = PlatformRuntime(extensionPlatform, serviceRegistry)

    // especially that:
    init {
        serviceRegistry.register(ModelCmds::class, modelCmdsImpl)
        serviceRegistry.register(ModelQueries::class, modelQueriesImpl)
        serviceRegistry.register(ModelHumanPrinter::class, modelHumanPrinterEmoji)
        serviceRegistry.register(SecurityRolesRegistry::class, securityRolesRegistry)
    }

    // End of 🤔 🤔 🤔 🤯

    fun build(): AppRuntime {
        // Launches a startup sequence. Usually useful to do some stuff like data migrations
        this.platformRuntime.start()
        return AppRuntimeImpl(
            config,
            extensionPlatform.extensionRegistry,
            serviceRegistry
        )
    }

    class AppRuntimeImpl(
        override val config: AppRuntimeConfig,
        override val extensionRegistry: ExtensionRegistry,
        override val services: MedatarunServiceRegistry
    ) : AppRuntime

    companion object {
        private val logger = LoggerFactory.getLogger("audit")
    }
}