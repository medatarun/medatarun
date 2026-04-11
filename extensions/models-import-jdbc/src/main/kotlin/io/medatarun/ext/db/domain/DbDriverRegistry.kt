package io.medatarun.ext.db.domain

interface DbDriverRegistry {
    fun find(database: String): DbDriverInfo
    fun listDrivers(): List<DbDriverInfo>
}