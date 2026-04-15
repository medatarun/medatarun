package io.medatarun.ext.db.internal.connection

import io.medatarun.ext.db.domain.DbConnectionRegistry
import io.medatarun.ext.db.domain.DbConnectionSecret
import io.medatarun.ext.db.domain.DbDatasource
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * Repository for database connections stored in Json
 * file format
 */
internal class DbConnectionRegistryImpl(
    /** absolute path the "connections.json" file */
    val path: Path
): DbConnectionRegistry {
    /**
     * Hashmap connection name to real connection infos
     */
    var map = mapOf<String, DbDatasource>()

    init {
        val connectionsJson = readJson()
        this.map = connectionsJson.associateBy { it.name }
    }

    override fun listConnections():List<DbDatasource> {
        return map.values.toList()
    }

    private fun readJson(): List<DbDatasource> {
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

    override fun findByNameOptional(connectionName: String): DbDatasource? {
            return map[connectionName]
    }
}