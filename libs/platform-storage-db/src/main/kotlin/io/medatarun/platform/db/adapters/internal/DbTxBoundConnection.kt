package io.medatarun.platform.db.adapters.internal

import io.medatarun.lang.exceptions.MedatarunException
import java.sql.Connection
import java.sql.Savepoint
import java.sql.ShardingKey

/**
 * Transaction-bound JDBC connection wrapper.
 * It prevents repositories using `use {}` from closing the underlying transaction connection.
 */
class DbTxBoundConnection(
    private val delegateConnection: Connection
) : Connection by delegateConnection {
    class DbTxBoundConnectionCommitNotAllowedException :
        MedatarunException("Manual commit is not allowed on a transaction-bound connection")

    class DbTxBoundConnectionRollbackNotAllowedException :
        MedatarunException("Manual rollback is not allowed on a transaction-bound connection")

    class DbTxBoundConnectionSetAutoCommitNotAllowedException :
        MedatarunException("Changing autoCommit is not allowed on a transaction-bound connection")

    override fun close() {
        // No-op: the transaction manager owns the lifecycle of the underlying connection.
    }

    override fun commit() {
        throw DbTxBoundConnectionCommitNotAllowedException()
    }

    override fun rollback() {
        throw DbTxBoundConnectionRollbackNotAllowedException()
    }

    override fun rollback(savepoint: Savepoint?) {
        throw DbTxBoundConnectionRollbackNotAllowedException()
    }

    override fun beginRequest() {
        delegateConnection.beginRequest()
    }

    override fun endRequest() {
        delegateConnection.endRequest()
    }

    override fun setShardingKeyIfValid(
        shardingKey: ShardingKey?,
        superShardingKey: ShardingKey?,
        timeout: Int
    ): Boolean {
        return delegateConnection.setShardingKeyIfValid(shardingKey, superShardingKey, timeout)
    }

    override fun setShardingKeyIfValid(shardingKey: ShardingKey?, timeout: Int): Boolean {
        return delegateConnection.setShardingKeyIfValid(shardingKey, timeout)
    }

    override fun setShardingKey(shardingKey: ShardingKey?, superShardingKey: ShardingKey?) {
        delegateConnection.setShardingKey(shardingKey, superShardingKey)
    }

    override fun setShardingKey(shardingKey: ShardingKey?) {
        delegateConnection.setShardingKey(shardingKey)
    }

    override fun setAutoCommit(autoCommit: Boolean) {
        throw DbTxBoundConnectionSetAutoCommitNotAllowedException()
    }
}
