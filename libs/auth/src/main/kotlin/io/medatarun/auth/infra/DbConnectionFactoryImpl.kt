package io.medatarun.auth.infra

import io.medatarun.auth.ports.needs.DbConnectionFactory
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

class DbConnectionFactoryImpl(private val dbPath: Path) : DbConnectionFactory {


    init {
        Files.createDirectories(dbPath.parent)
    }

    override fun getConnection(): Connection {
        return DriverManager.getConnection("jdbc:sqlite:${dbPath.toAbsolutePath()}")
    }

}