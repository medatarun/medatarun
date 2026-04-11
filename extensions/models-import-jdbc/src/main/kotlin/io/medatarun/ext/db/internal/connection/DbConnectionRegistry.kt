package io.medatarun.ext.db.internal.connection

import io.medatarun.ext.db.domain.DbDatasource

interface DbConnectionRegistry {
    fun findByNameOptional(connectionName: String): DbDatasource?
    fun listConnections():List<DbDatasource>

}
