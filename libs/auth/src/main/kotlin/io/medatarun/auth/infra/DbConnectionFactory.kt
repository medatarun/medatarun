package io.medatarun.auth.infra

import java.sql.Connection

interface DbConnectionFactory {


    fun getConnection(): Connection
}