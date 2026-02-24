package io.medatarun.runtime.internal

import io.medatarun.actions.ActionsExtension
import io.medatarun.auth.AuthExtension
import io.medatarun.ext.db.ModelsImportJdbcExtension
import io.medatarun.ext.frictionlessdata.FrictionlessdataExtension
import io.medatarun.ext.modeljson.ModelJsonExtension
import io.medatarun.model.ModelExtension
import io.medatarun.platform.db.PlatformStorageDbExtension
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.MedatarunServiceRegistry
import io.medatarun.platform.kernel.PlatformRuntime
import io.medatarun.platform.kernel.internal.ExtensionPlaformImpl
import io.medatarun.platform.kernel.internal.MedatarunServiceRegistryImpl
import io.medatarun.runtime.AppRuntime
import io.medatarun.security.SecurityExtension
import io.medatarun.tags.core.TagsCoreExtension
import io.medatarun.types.TypeSystemExtension
import io.metadatarun.ext.config.SysopsConfigInspectorExtension

class AppRuntimeBuilder(private val config: AppRuntimeConfig) {


    fun build(): AppRuntime {

        // BE CAREFUL: must be done in correct order, we don't have
        // dependency graphs that launch them in correct order for now
        val extensions = listOf(
            TypeSystemExtension(),
            SecurityExtension(),
            ActionsExtension(),
            PlatformStorageDbExtension(),
            PlatformStorageDbSqliteExtension(),
            AuthExtension(),
            TagsCoreExtension(),
            ModelExtension(),
            SysopsConfigInspectorExtension(),
            ModelJsonExtension(),
            ModelsImportJdbcExtension(),
            FrictionlessdataExtension()
        )

        val serviceRegistry = MedatarunServiceRegistryImpl()
        val extensionPlatform = ExtensionPlaformImpl(extensions, serviceRegistry, config)
        val platformRuntime = PlatformRuntime(extensionPlatform, serviceRegistry)

        // Launches a startup sequence. Usually useful to do some stuff like data migrations
        platformRuntime.start()

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

}