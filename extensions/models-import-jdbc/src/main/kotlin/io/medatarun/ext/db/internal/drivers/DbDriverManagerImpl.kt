package io.medatarun.ext.db.internal.drivers

import io.medatarun.ext.db.domain.DbDriverManager
import io.medatarun.ext.db.domain.DbDriverRegistry
import io.medatarun.ext.db.domain.DbDatasource
import io.medatarun.ext.db.domain.DbDriverInfo
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

internal class DbDriverManagerImpl(driversJsonPath: Path, jdbcDriversPath: Path): DbDriverManager {

    private val driverRegistry: DbDriverRegistry = DbDriverRegistryImpl(driversJsonPath, jdbcDriversPath)
    private val driverLoader: DbDriverLoader = DbDriverLoader(driverRegistry)

    override fun listDrivers(): List<DbDriverInfo> {
        return driverRegistry.listDrivers()
    }

    override fun getConnection(connection: DbDatasource): Connection {
        driverLoader.loadDriverIfNeeded(connection.driver)
        val info = Properties()
        info["user"] = connection.username
        info["password"] = connection.secret.value
        info.putAll(connection.properties)
        return DriverManager.getConnection(connection.url, info)
    }

}