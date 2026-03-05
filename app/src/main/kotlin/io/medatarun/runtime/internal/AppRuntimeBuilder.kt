package io.medatarun.runtime.internal

import io.medatarun.actions.ActionsExtension
import io.medatarun.auth.AuthExtension
import io.medatarun.ext.db.ModelsImportJdbcExtension
import io.medatarun.ext.frictionlessdata.FrictionlessdataExtension
import io.medatarun.ext.modeljson.ModelJsonExtension
import io.medatarun.model.ModelExtension
import io.medatarun.platform.db.PlatformStorageDbExtension
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension
import io.medatarun.platform.kernel.PlatformBuilder
import io.medatarun.platform.kernel.PlatformRuntime
import io.medatarun.security.SecurityExtension
import io.medatarun.tags.core.TagsCoreExtension
import io.medatarun.types.TypeSystemExtension
import io.metadatarun.ext.config.SysopsConfigInspectorExtension

class AppRuntimeBuilder(private val config: AppRuntimeConfig) {


    fun build(): PlatformRuntime {

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
            ModelJsonExtension(),
            SysopsConfigInspectorExtension(),
            ModelsImportJdbcExtension(),
            FrictionlessdataExtension()
        )

        val platform = PlatformBuilder(config, extensions).buildAndStart()

        return platform
    }
}
