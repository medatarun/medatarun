package io.medatarun.platform.db

import java.sql.Connection

interface DbConnectionFactory {
    fun getConnection(): Connection

}
