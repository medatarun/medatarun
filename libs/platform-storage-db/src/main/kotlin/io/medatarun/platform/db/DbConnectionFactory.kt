package io.medatarun.platform.db

import io.medatarun.platform.kernel.Service
import java.sql.Connection

interface DbConnectionFactory: Service {
    val dialect: DbDialect

    /**
     * Runs [block] with a JDBC connection obtained from the transaction bridge.
     *
     * When a global transaction is active, the block receives the transaction-bound connection.
     * Otherwise it receives a short-lived raw connection owned by the infrastructure.
     */
    fun <T> withConnection(block: (Connection) -> T): T

    /**
     * Runs [block] inside the current Exposed transaction when one already exists on this thread.
     * Otherwise it opens a short Exposed transaction in the infrastructure so storage adapters never
     * have to manage transaction scopes themselves.
     */
    fun <T> withExposed(block: () -> T): T

}
