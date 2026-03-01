package io.medatarun.platform.db.adapters

import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbProvider
import io.medatarun.platform.db.adapters.internal.DbTxBoundConnection
import java.sql.Connection

class DbConnectionFactoryImpl(
    private val dbProvider: DbProvider,
    private val txManager: DbTransactionManagerImpl
): DbConnectionFactory {

    override fun getConnection(): Connection {
        val txConnection = txManager.currentTransactionConnectionOrNull()
        if (txConnection != null) {
            return DbTxBoundConnection(txConnection)
        }
        return dbProvider.getConnection()
    }
}
