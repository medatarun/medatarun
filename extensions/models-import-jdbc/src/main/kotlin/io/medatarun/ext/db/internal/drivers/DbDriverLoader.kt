package io.medatarun.ext.db.internal.drivers

import io.medatarun.ext.db.domain.DbDriverRegistry
import org.slf4j.LoggerFactory
import java.net.URL
import java.net.URLClassLoader
import java.sql.Driver
import java.sql.DriverManager

internal class DbDriverLoader(val registry: DbDriverRegistry) {
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
            val driverClass = Class.forName(driverClassName, true, loader)
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

private class DriverRattachedToCurrentClassloaded(val driver: Driver) : Driver by driver

