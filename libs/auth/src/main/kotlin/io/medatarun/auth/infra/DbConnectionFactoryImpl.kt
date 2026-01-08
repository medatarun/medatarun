package io.medatarun.auth.infra

import io.medatarun.auth.ports.needs.DbConnectionFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

class DbConnectionFactoryImpl(private val dbPath: String) : DbConnectionFactory {


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