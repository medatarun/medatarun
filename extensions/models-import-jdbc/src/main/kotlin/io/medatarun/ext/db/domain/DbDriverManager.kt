package io.medatarun.ext.db.domain

import java.sql.Connection

interface DbDriverManager {
    fun getConnection(connection: DbDatasource): Connection
    fun listDrivers(): List<DbDriverInfo>
}