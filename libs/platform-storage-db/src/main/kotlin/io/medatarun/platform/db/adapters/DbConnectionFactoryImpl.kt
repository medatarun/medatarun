package io.medatarun.platform.db.adapters

import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbDialect
import io.medatarun.platform.db.DbProvider
import io.medatarun.platform.db.adapters.internal.DbTxBoundConnection
import java.sql.Connection

class DbConnectionFactoryImpl(
    private val dbProvider: DbProvider,
    private val txManager: DbTransactionManagerImpl
): DbConnectionFactory {
    override val dialect: DbDialect
        get() = txManager.dialect

    override fun <T> withConnection(block: (Connection) -> T): T {
        val tx = txManager.currentTransactionOrNull()
        if (tx != null) {
            return block(DbTxBoundConnection(tx.connection.connection as Connection))
        }

        val connection = dbProvider.getConnection()
        try {
            return block(connection)
        } finally {
            connection.close()
        }
    }

    override fun <T> withExposed(block: () -> T): T = txManager.withExposed(block)
}
