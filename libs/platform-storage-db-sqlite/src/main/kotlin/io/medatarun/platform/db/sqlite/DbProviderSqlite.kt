package io.medatarun.platform.db.sqlite

import io.medatarun.lang.uuid.UuidUtils
import io.medatarun.platform.db.DbProvider
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import kotlin.io.path.createDirectories

class DbProviderSqlite(private val url: String) : DbProvider {


    override fun getConnection(): Connection {
        return DriverManager.getConnection(url)
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