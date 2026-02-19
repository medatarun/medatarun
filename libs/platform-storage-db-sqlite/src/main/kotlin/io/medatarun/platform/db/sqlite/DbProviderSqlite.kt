package io.medatarun.platform.db.sqlite

import io.medatarun.platform.db.DbProvider
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

class DbProviderSqlite(private val dbPath: String) : DbProvider {


    init {
        // WARNING: SQLite doesn't support Java Filesystems, any database created will be created for real
        // on the hard drive.
        if (dbPath.startsWith("/"))
            Files.createDirectories(Paths.get(dbPath).toAbsolutePath().parent)
    }

    override fun getConnection(): Connection {
        return DriverManager.getConnection("jdbc:sqlite:${dbPath}")
    }

}