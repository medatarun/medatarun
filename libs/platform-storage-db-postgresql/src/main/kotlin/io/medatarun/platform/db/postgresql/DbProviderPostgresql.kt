package io.medatarun.platform.db.postgresql

import io.medatarun.platform.db.DbProvider
import io.medatarun.platform.db.DbDialect
import java.sql.Connection
import java.sql.DriverManager
import java.util.Properties

class DbProviderPostgresql(
    private val url: String,
    private val properties: Properties
) : DbProvider {
    override val dialect: DbDialect = DbDialect.POSTGRESQL

    override fun getConnection(): Connection {
        return DriverManager.getConnection(url, properties)
    }
}
