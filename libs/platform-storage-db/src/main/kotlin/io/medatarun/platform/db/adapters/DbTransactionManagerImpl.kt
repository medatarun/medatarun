package io.medatarun.platform.db.adapters

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.db.DbProvider
import io.medatarun.platform.db.DbTransactionManager
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class DbTransactionManagerImpl(
    private val dbProvider: DbProvider
) : DbTransactionManager {
    class DbTransactionRollbackOnlyException :
        MedatarunException("Transaction cannot commit because a nested transactional block failed earlier in the same thread")

    private val database = Database.connect(
        getNewConnection = { dbProvider.getConnection() }
    )

    internal fun currentTransactionOrNull(): JdbcTransaction? {
        return TransactionManager.currentOrNull() as? JdbcTransaction
    }

    override fun <T> runInTransaction(block: () -> T): T {
        return transaction(database) {
            block()
        }
    }
}
