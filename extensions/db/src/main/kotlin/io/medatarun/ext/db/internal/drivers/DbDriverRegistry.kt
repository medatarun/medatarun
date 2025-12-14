package io.medatarun.ext.db.internal.drivers

import io.medatarun.ext.db.model.DbDriverInfo
import io.medatarun.ext.db.model.DbDriverManagerUnknownDatabaseException
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

class DbDriverRegistry(val driversJsonPath: Path, val jdbcDriversPath: Path) {
    private val descriptionFile = driversJsonPath
    private val knownDrivers: MutableList<DbDriverInfo> = mutableListOf()
    init {
        knownDrivers.addAll(loadDriverDescriptions())
    }
    fun loadDriverDescriptions():List<DbDriverInfo> {
        if (!descriptionFile.exists()) return emptyList()
        val d = Json.Default.decodeFromString<DriversJson>(descriptionFile.readText())
        val drivers = d.drivers.map {
            DbDriverInfo(
                id = it.id,
                name = it.name,
                className = it.className,
                jarPath = jdbcDriversPath.resolve(it.jar)
            )
        }
        logger.debug("Declared JDBC drivers: {}", drivers.map { it.id })
        return drivers

    }

    fun listDrivers(): List<DbDriverInfo> {
        return knownDrivers
    }

    fun isKnown(database: String): Boolean {
        return knownDrivers.any { it.id == database }
    }

    fun find(database: String): DbDriverInfo {
        return knownDrivers.firstOrNull { it.id == database } ?: throw DbDriverManagerUnknownDatabaseException(database)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DbDriverRegistry::class.java)
    }
}