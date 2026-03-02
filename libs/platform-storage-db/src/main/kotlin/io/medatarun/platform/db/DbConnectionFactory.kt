package io.medatarun.platform.db

import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import java.sql.Connection

interface DbConnectionFactory {
    fun getConnection(): Connection

    /**
     * Returns the current Exposed JDBC transaction when one is active on this thread.
     *
     * Repositories still using raw JDBC can keep using [getConnection], while repositories migrated
     * to Exposed can join the same transaction through this bridge.
     */
    fun currentExposedTransactionOrNull(): JdbcTransaction?
}
