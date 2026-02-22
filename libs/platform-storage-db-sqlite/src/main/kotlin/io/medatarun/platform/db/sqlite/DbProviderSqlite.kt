package io.medatarun.platform.db.sqlite

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.DbProvider
import java.sql.Connection
import java.sql.DriverManager

class DbProviderSqlite(private val url: String) : DbProvider {


    override fun getConnection(): Connection {
        val connection = DriverManager.getConnection(url)
        connection.createStatement().use { statement ->
            // SQLite applies foreign key checks per connection, so enable it every time.
            statement.execute("PRAGMA foreign_keys = ON")
        }
        return connection
    }

    companion object {
        fun randomDb(): DbProviderSqlite {
            return DbProviderSqlite(randomDbUrl())
        }

        fun randomDbUrl(): String {
            return "jdbc:sqlite:file:test_${UuidUtils.generateV4String()}?mode=memory&cache=shared"
        }
    }
}
