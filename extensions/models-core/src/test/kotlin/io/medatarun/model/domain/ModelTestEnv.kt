package io.medatarun.model.domain


import com.google.common.jimfs.Jimfs
import io.medatarun.actions.ActionsExtension
import io.medatarun.model.ModelExtension
import io.medatarun.platform.db.PlatformStorageDbExtension
import io.medatarun.platform.db.sqlite.DbProviderSqlite
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension.Companion.JDBC_URL_PROPERTY
import io.medatarun.platform.kernel.MedatarunConfig
import io.medatarun.platform.kernel.PlatformBuilder
import io.medatarun.security.SecurityExtension
import io.medatarun.tags.core.TagsCoreExtension
import io.medatarun.types.TypeSystemExtension

class ModelTestEnv {
    private val extensions = listOf(
        TypeSystemExtension(),
        SecurityExtension(),
        ActionsExtension(),
        PlatformStorageDbExtension(),
        PlatformStorageDbSqliteExtension(),
        TagsCoreExtension(),
        ModelExtension(),
    )
    val platform = PlatformBuilder(
        config = MedatarunConfig.createTempConfig(
            Jimfs.newFileSystem(),
            mapOf(
                JDBC_URL_PROPERTY to DbProviderSqlite.randomDbUrl()
            )
        ),
        extensions = extensions
    ).buildAndStart()
}