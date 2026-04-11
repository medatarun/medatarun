package io.medatarun.ext.db.domain

interface DbConnectionRegistry {
    fun findByNameOptional(connectionName: String): DbDatasource?
    fun listConnections():List<DbDatasource>

}