package io.medatarun.ext.db.internal.connection

import io.medatarun.ext.db.model.DbConnectionSecret
import io.medatarun.ext.db.model.DbDatasource
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * Repository for database connections stored in Json
 * file format
 */
class DbConnectionRegistry(
    /** absolute path the "connections.json" file */
    val path: Path
) {
    /**
     * Hashmap connection name to real connection infos
     */
    var map = mapOf<String, DbDatasource>()

    init {
        val connectionsJson = readJson()
        this.map = connectionsJson.associateBy { it.name }
    }

    fun listConnections():List<DbDatasource> {
        return map.values.toList()
    }

    fun readJson(): List<DbDatasource> {
        if (!path.exists()) return emptyList()
        val connections = Json.decodeFromString<DbDatasourcesJson>(path.readText())
        return connections.datasources
            .map {
                DbDatasource(
                    name = it.name,
                    driver = it.driver,
                    url = it.url,
                    secret = DbConnectionSecret(storage=it.secret.storage, value= it.secret.value),
                    username = it.username,
                    properties = it.properties
                )
            }

    }

    fun findByNameOptional(connectionName: String): DbDatasource? {
            return map[connectionName]
    }
}