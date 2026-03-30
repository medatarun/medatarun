package io.medatarun.platform.db.adapters

import io.medatarun.platform.db.DbProvider
import io.medatarun.platform.db.DbTransactionManager
import org.jetbrains.exposed.v1.core.Slf4jSqlDebugLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class DbTransactionManagerImpl(
    private val dbProvider: DbProvider
) : DbTransactionManager {

    private val database = Database.connect(
        getNewConnection = { dbProvider.getConnection() }
    )

    internal fun currentTransactionOrNull(): JdbcTransaction? {
        return TransactionManager.currentOrNull()
    }

    internal fun <T> withExposed(block: () -> T): T {
        if (currentTransactionOrNull() != null) {
            return block()
        }
        return transaction(database) {
            addLogger(Slf4jSqlDebugLogger)
            block()
        }
    }

    override fun <T> runInTransaction(block: () -> T): T {
        return transaction(database) {
            addLogger(Slf4jSqlDebugLogger)
            block()
        }
    }
}
