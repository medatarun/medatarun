package io.medatarun.platform.db

import java.sql.Connection

interface DbProvider {
    fun getConnection(): Connection

}