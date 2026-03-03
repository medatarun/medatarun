package io.medatarun.platform.db

import io.medatarun.lang.exceptions.MedatarunException

/**
 * Loads SQL scripts from the application classpath and executes them through the DB bridge.
 *
 * The runner keeps SQL parsing deliberately simple because current migrations are plain DDL scripts.
 * If later scripts need procedural SQL or vendor specific delimiters, this helper can be extended in one place.
 */
object DbSqlResources {
    fun executeClasspathResource(dbConnectionFactory: DbConnectionFactory, resourcePath: String) {
        val sql = readRequired(resourcePath)
        dbConnectionFactory.withConnection { connection ->
            splitStatements(sql).forEach { statement ->
                connection.createStatement().use { jdbcStatement ->
                    jdbcStatement.execute(statement)
                }
            }
        }
    }

    fun readRequired(resourcePath: String): String {
        val normalizedPath = resourcePath.removePrefix("/")
        val loader = Thread.currentThread().contextClassLoader ?: DbSqlResources::class.java.classLoader
        val stream = loader.getResourceAsStream(normalizedPath)
            ?: throw DbSqlResourceNotFoundException(resourcePath)
        return stream.bufferedReader().use { it.readText() }
    }

    private fun splitStatements(sql: String): List<String> {
        return sql.split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    class DbSqlResourceNotFoundException(path: String) :
        MedatarunException("SQL resource not found on classpath: $path")
}
