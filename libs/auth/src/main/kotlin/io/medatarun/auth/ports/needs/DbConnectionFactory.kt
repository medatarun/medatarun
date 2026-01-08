package io.medatarun.auth.ports.needs

import java.sql.Connection

interface DbConnectionFactory {


    fun getConnection(): Connection
}