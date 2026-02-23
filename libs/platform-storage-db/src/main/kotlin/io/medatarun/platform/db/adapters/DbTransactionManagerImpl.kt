package io.medatarun.platform.db.adapters

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.db.DbProvider
import io.medatarun.platform.db.DbTransactionManager
import java.sql.Connection

class DbTransactionManagerImpl(
    private val dbProvider: DbProvider
) : DbTransactionManager {
    class DbTransactionRollbackOnlyException :
        MedatarunException("Transaction cannot commit because a nested transactional block failed earlier in the same thread")

    private data class DbTransactionState(
        val connection: Connection,
        var depth: Int,
        var rollbackOnly: Boolean
    )

    private val currentState = ThreadLocal<DbTransactionState?>()

    internal fun currentTransactionConnectionOrNull(): Connection? {
        return currentState.get()?.connection
    }

    override fun <T> runInTransaction(block: () -> T): T {
        val existingState = currentState.get()
        if (existingState != null) {
            existingState.depth += 1
            try {
                return block()
            } catch (e: Throwable) {
                existingState.rollbackOnly = true
                throw e
            } finally {
                existingState.depth -= 1
            }
        }

        val connection = dbProvider.getConnection()
        connection.autoCommit = false
        val state = DbTransactionState(connection = connection, depth = 1, rollbackOnly = false)
        currentState.set(state)
        var failure: Throwable? = null
        try {
            val result = block()
            if (state.rollbackOnly) {
                throw DbTransactionRollbackOnlyException()
            }
            connection.commit()
            return result
        } catch (e: Throwable) {
            failure = e
            try {
                connection.rollback()
            } catch (rollbackError: Throwable) {
                // Preserve the original business/command failure and attach rollback failure as secondary context.
                e.addSuppressed(rollbackError)
            }
            throw e
        } finally {
            currentState.remove()
            try {
                connection.close()
            } catch (closeError: Throwable) {
                val existingFailure = failure
                if (existingFailure != null) {
                    // If the transactional work already failed, closing the connection is a secondary failure.
                    // Keep the original failure as the main exception and attach close() failure for diagnostics.
                    existingFailure.addSuppressed(closeError)
                } else {
                    // If nothing failed before, close() failure becomes the main failure.
                    throw closeError
                }
            }
        }
    }
}
