package io.medatarun.runtime

import com.google.common.jimfs.Jimfs
import io.medatarun.actions.runtime.AppHttpServerServices
import io.medatarun.platform.db.sqlite.DbProviderSqlite
import io.medatarun.platform.db.sqlite.PlatformStorageDbSqliteExtension
import io.medatarun.runtime.internal.AppRuntimeBuilder
import io.medatarun.runtime.internal.AppRuntimeConfigFactory
import io.medatarun.runtime.internal.AppRuntimeConfigFactory.Companion.MEDATARUN_APPLICATION_DATA_ENV
import io.medatarun.runtime.internal.AppRuntimeConfigFactory.Companion.MEDATARUN_HOME_ENV
import java.nio.file.FileSystem
import kotlin.io.path.createDirectories
import kotlin.test.Test

class AppRuntimeTest {
    @Test
    fun `that app can be launched in server mode`() {
        val os = AppRuntimeOSBridgeTest()
        val config = AppRuntimeConfigFactory(cli = false, os).create()
        val runtime = AppRuntimeBuilder(config).build()
        val services = AppHttpServerServices(runtime)
        services.actionRegistry.findAllActions()


    }

    class AppRuntimeOSBridgeTest(
        val envMedatarunHomeDir: String? = null,
        val envMedatarunApplicationData: String? = null,
        val propertyUserDir: String = "/home/medatarun",
    ) : AppRuntimeOsBridge {

        val randomDbUrl = DbProviderSqlite.randomDbUrl()

        override val fileSystem: FileSystem = Jimfs.newFileSystem().also {
            it.getPath(propertyUserDir).createDirectories()
        }

        override fun getenv(name: String): String? {
            if (name == MEDATARUN_HOME_ENV) return envMedatarunHomeDir
            if (name == MEDATARUN_APPLICATION_DATA_ENV) return envMedatarunApplicationData
            return null
        }

        override fun getProperty(name: String): String? {
            if (name == AppRuntimeConfigFactory.USER_DIR_PROPERTY) return propertyUserDir

            return null
        }

        override fun builtInConfigProperties(): Map<String, String> {

            return mapOf(
                PlatformStorageDbSqliteExtension.JDBC_URL_PROPERTY to randomDbUrl
            )
        }
    }


}