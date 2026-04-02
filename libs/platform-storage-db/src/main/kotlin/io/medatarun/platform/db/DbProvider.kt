package io.medatarun.platform.db

import java.sql.Connection

interface DbProvider {
    val dialect: DbDialect
    fun getConnection(): Connection

}
