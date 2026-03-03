package io.medatarun.platform.db.sqlite

import io.medatarun.lang.exceptions.MedatarunException
import io.medatarun.platform.db.adapters.DbConnectionFactoryImpl
import io.medatarun.platform.db.adapters.DbTransactionManagerImpl
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import kotlin.test.*

class ExposedTransactionBridgeSqliteTest {
    private class ExposedTransactionBridgeOriginalNestedException :
        MedatarunException("Original nested transaction failure")

    private class ExposedTransactionBridgeRethrownNestedException(cause: Throwable) :
        MedatarunException("Nested transaction failure was translated") {
        init {
            initCause(cause)
        }
    }


    /**
     * Verifies the bridge exposes the Exposed transaction currently opened by [DbTransactionManagerImpl]
     * and that JDBC repositories using [DbConnectionFactoryImpl.getConnection] join that same global
     * transaction instead of opening their own one.
     *
     * We prove it in two ways:
     * - a nested call to [DbConnectionFactoryImpl.withExposed] reuses the exact same Exposed transaction
     * - data inserted through one JDBC connection is immediately readable through another JDBC connection
     *   obtained in the same transactional block, then remains committed after the outer transaction ends
     */
    @Test
    fun `runInTransaction exposes current Exposed transaction and keeps JDBC work in one transaction`() {
        val dbProvider = DbProviderSqlite.randomDb()
        val txManager = DbTransactionManagerImpl(dbProvider)
        val connectionFactory = DbConnectionFactoryImpl(dbProvider, txManager)

        connectionFactory.withConnection { connection ->
            connection.createStatement().use { statement ->
                statement.execute("CREATE TABLE test_item(id TEXT PRIMARY KEY, name TEXT NOT NULL)")
            }
        }

        txManager.runInTransaction {
            val currentTransaction = TransactionManager.currentOrNull()
            assertNotNull(currentTransaction)

            connectionFactory.withConnection { firstConnection ->
                firstConnection.prepareStatement(
                    "INSERT INTO test_item(id, name) VALUES (?, ?)"
                ).use { ps ->
                    ps.setString(1, "one")
                    ps.setString(2, "alpha")
                    ps.executeUpdate()
                }
            }

            val nestedTransaction = connectionFactory.withExposed {
                TransactionManager.currentOrNull()
            }

            assertSame(currentTransaction, nestedTransaction)

            connectionFactory.withConnection { secondConnection ->
                secondConnection.prepareStatement(
                    "SELECT COUNT(*) FROM test_item"
                ).use { ps ->
                    ps.executeQuery().use { rs ->
                        rs.next()
                        assertEquals(1, rs.getInt(1))
                    }
                }
            }
        }

        connectionFactory.withConnection { connection ->
            connection.prepareStatement("SELECT COUNT(*) FROM test_item").use { ps ->
                ps.executeQuery().use { rs ->
                    rs.next()
                    assertEquals(1, rs.getInt(1))
                }
            }
        }
    }

    /**
     * Verifies a failure raised inside a global transaction rolls back every JDBC write done in that block.
     *
     * The test inserts a row and then throws an exception before the transaction completes.
     * We then read the table outside the transaction and expect zero rows, which proves the insert
     * was part of the transaction and was rolled back with it.
     */
    @Test
    fun `runInTransaction rolls back JDBC work on failure`() {
        val dbProvider = DbProviderSqlite.randomDb()
        val txManager = DbTransactionManagerImpl(dbProvider)
        val connectionFactory = DbConnectionFactoryImpl(dbProvider, txManager)

        connectionFactory.withConnection { connection ->
            connection.createStatement().use { statement ->
                statement.execute("CREATE TABLE test_item(id TEXT PRIMARY KEY, name TEXT NOT NULL)")
            }
        }

        assertFailsWith<IllegalStateException> {
            txManager.runInTransaction {
                connectionFactory.withConnection { connection ->
                    connection.prepareStatement(
                        "INSERT INTO test_item(id, name) VALUES (?, ?)"
                    ).use { ps ->
                        ps.setString(1, "one")
                        ps.setString(2, "alpha")
                        ps.executeUpdate()
                    }
                }

                throw IllegalStateException("boom")
            }
        }

        connectionFactory.withConnection { connection ->
            connection.prepareStatement("SELECT COUNT(*) FROM test_item").use { ps ->
                ps.executeQuery().use { rs ->
                    rs.next()
                    assertEquals(0, rs.getInt(1))
                }
            }
        }
    }

    /**
     * Verifies an exception thrown from a nested transactional block reaches the top caller unchanged
     * and causes the whole global transaction to roll back.
     *
     * The outer block writes one row, the nested block writes a second row, then throws the original
     * exception. We assert that the same exception type is observed by the top-level caller and that the
     * table is empty afterwards, which proves both writes were rolled back together.
     */
    @Test
    fun `nested transaction failure bubbles up original exception and rolls back all JDBC work`() {
        val dbProvider = DbProviderSqlite.randomDb()
        val txManager = DbTransactionManagerImpl(dbProvider)
        val connectionFactory = DbConnectionFactoryImpl(dbProvider, txManager)

        connectionFactory.withConnection { connection ->
            connection.createStatement().use { statement ->
                statement.execute("CREATE TABLE test_item(id TEXT PRIMARY KEY, name TEXT NOT NULL)")
            }
        }

        val failure = assertFailsWith<ExposedTransactionBridgeOriginalNestedException> {
            txManager.runInTransaction {
                connectionFactory.withConnection { connection ->
                    connection.prepareStatement(
                        "INSERT INTO test_item(id, name) VALUES (?, ?)"
                    ).use { ps ->
                        ps.setString(1, "outer")
                        ps.setString(2, "alpha")
                        ps.executeUpdate()
                    }
                }

                txManager.runInTransaction {
                    connectionFactory.withConnection { connection ->
                        connection.prepareStatement(
                            "INSERT INTO test_item(id, name) VALUES (?, ?)"
                        ).use { ps ->
                            ps.setString(1, "inner")
                            ps.setString(2, "beta")
                            ps.executeUpdate()
                        }
                    }
                    throw ExposedTransactionBridgeOriginalNestedException()
                }
            }
        }
        assertEquals("Original nested transaction failure", failure.message)

        connectionFactory.withConnection { connection ->
            connection.prepareStatement("SELECT COUNT(*) FROM test_item").use { ps ->
                ps.executeQuery().use { rs ->
                    rs.next()
                    assertEquals(0, rs.getInt(1))
                }
            }
        }
    }

    /**
     * Verifies callers may translate a nested transaction failure to another exception and still get a
     * rollback of the whole global transaction.
     *
     * The nested block throws an original exception, the outer block catches it and immediately rethrows
     * a different exception. We assert the translated exception is the one seen at the top, that it keeps
     * the original cause, and that the database stays empty afterwards. This proves rollback depends on the
     * failure escaping the outer transaction, not on preserving the original exception type.
     */
    @Test
    fun `nested transaction failure can be rethrown as another exception and still rolls back all JDBC work`() {
        val dbProvider = DbProviderSqlite.randomDb()
        val txManager = DbTransactionManagerImpl(dbProvider)
        val connectionFactory = DbConnectionFactoryImpl(dbProvider, txManager)

        connectionFactory.withConnection { connection ->
            connection.createStatement().use { statement ->
                statement.execute("CREATE TABLE test_item(id TEXT PRIMARY KEY, name TEXT NOT NULL)")
            }
        }

        val failure = assertFailsWith<ExposedTransactionBridgeRethrownNestedException> {
            txManager.runInTransaction {
                connectionFactory.withConnection { connection ->
                    connection.prepareStatement(
                        "INSERT INTO test_item(id, name) VALUES (?, ?)"
                    ).use { ps ->
                        ps.setString(1, "outer")
                        ps.setString(2, "alpha")
                        ps.executeUpdate()
                    }
                }

                try {
                    txManager.runInTransaction {
                        connectionFactory.withConnection { connection ->
                            connection.prepareStatement(
                                "INSERT INTO test_item(id, name) VALUES (?, ?)"
                            ).use { ps ->
                                ps.setString(1, "inner")
                                ps.setString(2, "beta")
                                ps.executeUpdate()
                            }
                        }
                        throw ExposedTransactionBridgeOriginalNestedException()
                    }
                } catch (e: ExposedTransactionBridgeOriginalNestedException) {
                    throw ExposedTransactionBridgeRethrownNestedException(e)
                }
            }
        }

        assertNotNull(failure.cause)
        assertSame(ExposedTransactionBridgeOriginalNestedException::class, failure.cause!!::class)

        connectionFactory.withConnection { connection ->
            connection.prepareStatement("SELECT COUNT(*) FROM test_item").use { ps ->
                ps.executeQuery().use { rs ->
                    rs.next()
                    assertEquals(0, rs.getInt(1))
                }
            }
        }
    }
}
