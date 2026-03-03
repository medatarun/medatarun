package io.medatarun.auth.fixtures

import com.google.common.jimfs.Jimfs
import io.medatarun.actions.ActionsExtension
import io.medatarun.auth.AuthExtension
import io.medatarun.platform.db.DbMigrationChecker
import io.medatarun.platform.db.PlatformStorageDbExtension
import io.medatarun.platform.db.sqlite.DbProviderSqlite
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension
import io.medatarun.platform.kernel.MedatarunConfig
import io.medatarun.platform.kernel.PlatformBuilder
import io.medatarun.platform.kernel.getService
import io.medatarun.security.SecurityExtension
import io.medatarun.types.TypeSystemExtension

class AuthEnvPlatformTest() {
    val config = MedatarunConfig.createTempConfig(
        fs = Jimfs.newFileSystem(),
        props = mapOf(
            PlatformStorageDbSqliteExtension.JDBC_URL_PROPERTY to DbProviderSqlite.randomDbUrl()
        )
    )

    val runtime = PlatformBuilder(
        config,
        listOf(
            TypeSystemExtension(),
            ActionsExtension(),
            SecurityExtension(),
            PlatformStorageDbExtension(),
            PlatformStorageDbSqliteExtension(),
            AuthExtension()
        )
    ).buildAndStart()

    val dbMigrationChecker = runtime.services.getService<DbMigrationChecker>()

}