package io.medatarun.platform.db.sqlite

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.DbProvider
import java.sql.Connection
import java.sql.DriverManager

class DbProviderSqlite(private val url: String) : DbProvider {

    // When an SQLite database is created in memory, we must keep one active connection on the
    // database during the application lifecycle. It is because if a new connection
    // is opened when no other connection is still alive, we won't have the same database.
    //
    // So we store here a "first connection" to in a memory database.
    //
    // As this class lifecycle is tied to the platform lifecycle, the resource will be released
    // when the platform ends.
    //
    // Note that in unit tests, the platform is re-created for each test, so each test will have
    // a fresh database, this is intended.
    private var persistentConnection: Connection? = null

    override fun getConnection(): Connection {


        // The "keep alive" connexion
        if (url.contains("mode=memory") && persistentConnection == null) {
            persistentConnection = DriverManager.getConnection(url)
        }

        val connection = DriverManager.getConnection(url)
        // The requested connexion
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
