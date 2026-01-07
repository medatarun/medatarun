package io.medatarun.auth.embedded

import java.sql.Connection

interface DbConnectionFactory {


    fun getConnection(): Connection
}
