package io.medatarun.platform.db

import io.medatarun.platform.kernel.Service

interface DbTransactionManager: Service {
    val dialect: DbDialect

    /**
     * Runs [block] in a JDBC transaction.
     *
     * Nested calls join the current transaction on the same thread.
     * This implementation does not create JDBC savepoints for nested blocks.
     *
     * If a nested block fails, the joined transaction is marked rollback-only.
     * The outermost transactional block will then fail on commit with a rollback-only exception.
     */
    fun <T> runInTransaction(block: () -> T): T
}
