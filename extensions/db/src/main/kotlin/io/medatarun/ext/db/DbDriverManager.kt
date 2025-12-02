package io.medatarun.ext.db

import io.medatarun.model.model.MedatarunException
import org.slf4j.LoggerFactory
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.sql.Connection
import java.sql.Driver
import java.sql.DriverManager

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
            val driverInstance = driverClass.getDeclaredConstructor().newInstance() as java.sql.Driver
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

class DbDriverManager(jdbcDriversPath: Path) {

    val driverRegistry: DbDriverRegistry = DbDriverRegistry(jdbcDriversPath)
    val driverLoader: DbDriverLoader = DbDriverLoader(driverRegistry)

    fun getConnection(jdbcUrl: String): Connection {
        val database = jdbcUrl.split(":")[1]
        driverLoader.loadDriverIfNeeded(database)
        return DriverManager.getConnection(jdbcUrl)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DbDriverManager::class.java)
    }
}

