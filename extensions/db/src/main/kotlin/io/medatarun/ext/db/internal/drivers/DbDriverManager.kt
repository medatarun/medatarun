package io.medatarun.ext.db.internal.drivers

import io.medatarun.ext.db.internal.drivers.DbDriverRegistry
import io.medatarun.ext.db.model.DbConnection
import org.slf4j.LoggerFactory
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager
import java.util.Properties

class DbDriverLoader(val registry: DbDriverRegistry) {
    private val loadedDrivers = mutableSetOf<String>()


    fun loadDriverIfNeeded(database: String) {
        val driverInfo = registry.find(database)
        if (!loadedDrivers.contains(database)) {

            val driverJar = driverInfo.jarPath
            val driverClassName = driverInfo.className
            val driverUrl = driverJar.toUri().toURL()
            logger.debug("Loading driver for [$database] from ${driverInfo.jarPath}, base class ${driverInfo.className}")
            val loader = URLClassLoader(
                arrayOf<URL>(driverUrl),
                Thread.currentThread().getContextClassLoader()
            )
            val driverClass = Class.forName(driverClassName, true, loader);
            val driverInstance = driverClass.getDeclaredConstructor().newInstance() as Driver
            logger.debug("Registering driver instance for [$database]")
            DriverManager.registerDriver(
                DriverRattachedToCurrentClassloaded(driverInstance)
            )
            loadedDrivers.add(database)
        }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(DbDriverLoader::class.java)
    }
}

class DriverRattachedToCurrentClassloaded(val driver: Driver) : Driver by driver

class DbDriverManager(driversJsonPath: Path, jdbcDriversPath: Path) {

    val driverRegistry: DbDriverRegistry = DbDriverRegistry(driversJsonPath, jdbcDriversPath)
    val driverLoader: DbDriverLoader = DbDriverLoader(driverRegistry)

    fun getConnection(connection: DbConnection): Connection {
        driverLoader.loadDriverIfNeeded(connection.driver)
        val info = Properties()
        info["user"] = connection.username
        info["password"] = connection.secret.value
        info.putAll(connection.properties)
        return DriverManager.getConnection(connection.url, info)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DbDriverManager::class.java)
    }
}

